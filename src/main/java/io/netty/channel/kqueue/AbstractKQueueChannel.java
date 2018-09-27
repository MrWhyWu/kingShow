/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.ByteBufUtil;
/*   6:    */ import io.netty.buffer.Unpooled;
/*   7:    */ import io.netty.channel.AbstractChannel;
/*   8:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   9:    */ import io.netty.channel.Channel;
/*  10:    */ import io.netty.channel.Channel.Unsafe;
/*  11:    */ import io.netty.channel.ChannelConfig;
/*  12:    */ import io.netty.channel.ChannelException;
/*  13:    */ import io.netty.channel.ChannelFuture;
/*  14:    */ import io.netty.channel.ChannelFutureListener;
/*  15:    */ import io.netty.channel.ChannelMetadata;
/*  16:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  17:    */ import io.netty.channel.ChannelPipeline;
/*  18:    */ import io.netty.channel.ChannelPromise;
/*  19:    */ import io.netty.channel.ConnectTimeoutException;
/*  20:    */ import io.netty.channel.EventLoop;
/*  21:    */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*  22:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  23:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  24:    */ import io.netty.channel.socket.ChannelInputShutdownReadComplete;
/*  25:    */ import io.netty.channel.unix.FileDescriptor;
/*  26:    */ import io.netty.channel.unix.UnixChannel;
/*  27:    */ import io.netty.channel.unix.UnixChannelUtil;
/*  28:    */ import io.netty.util.ReferenceCountUtil;
/*  29:    */ import io.netty.util.internal.ObjectUtil;
/*  30:    */ import java.io.IOException;
/*  31:    */ import java.net.InetSocketAddress;
/*  32:    */ import java.net.SocketAddress;
/*  33:    */ import java.nio.ByteBuffer;
/*  34:    */ import java.nio.channels.AlreadyConnectedException;
/*  35:    */ import java.nio.channels.ConnectionPendingException;
/*  36:    */ import java.nio.channels.NotYetConnectedException;
/*  37:    */ import java.nio.channels.UnresolvedAddressException;
/*  38:    */ import java.util.concurrent.ScheduledFuture;
/*  39:    */ import java.util.concurrent.TimeUnit;
/*  40:    */ 
/*  41:    */ abstract class AbstractKQueueChannel
/*  42:    */   extends AbstractChannel
/*  43:    */   implements UnixChannel
/*  44:    */ {
/*  45: 56 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  46:    */   private ChannelPromise connectPromise;
/*  47:    */   private ScheduledFuture<?> connectTimeoutFuture;
/*  48:    */   private SocketAddress requestedRemoteAddress;
/*  49:    */   final BsdSocket socket;
/*  50: 66 */   private boolean readFilterEnabled = true;
/*  51:    */   private boolean writeFilterEnabled;
/*  52:    */   boolean readReadyRunnablePending;
/*  53:    */   boolean inputClosedSeenErrorOnRead;
/*  54:    */   long jniSelfPtr;
/*  55:    */   protected volatile boolean active;
/*  56:    */   private volatile SocketAddress local;
/*  57:    */   private volatile SocketAddress remote;
/*  58:    */   
/*  59:    */   AbstractKQueueChannel(Channel parent, BsdSocket fd, boolean active)
/*  60:    */   {
/*  61: 84 */     this(parent, fd, active, false);
/*  62:    */   }
/*  63:    */   
/*  64:    */   AbstractKQueueChannel(Channel parent, BsdSocket fd, boolean active, boolean writeFilterEnabled)
/*  65:    */   {
/*  66: 88 */     super(parent);
/*  67: 89 */     this.socket = ((BsdSocket)ObjectUtil.checkNotNull(fd, "fd"));
/*  68: 90 */     this.active = active;
/*  69: 91 */     this.writeFilterEnabled = writeFilterEnabled;
/*  70: 92 */     if (active)
/*  71:    */     {
/*  72: 95 */       this.local = fd.localAddress();
/*  73: 96 */       this.remote = fd.remoteAddress();
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   AbstractKQueueChannel(Channel parent, BsdSocket fd, SocketAddress remote)
/*  78:    */   {
/*  79:101 */     super(parent);
/*  80:102 */     this.socket = ((BsdSocket)ObjectUtil.checkNotNull(fd, "fd"));
/*  81:103 */     this.active = true;
/*  82:    */     
/*  83:    */ 
/*  84:106 */     this.remote = remote;
/*  85:107 */     this.local = fd.localAddress();
/*  86:    */   }
/*  87:    */   
/*  88:    */   static boolean isSoErrorZero(BsdSocket fd)
/*  89:    */   {
/*  90:    */     try
/*  91:    */     {
/*  92:112 */       return fd.getSoError() == 0;
/*  93:    */     }
/*  94:    */     catch (IOException e)
/*  95:    */     {
/*  96:114 */       throw new ChannelException(e);
/*  97:    */     }
/*  98:    */   }
/*  99:    */   
/* 100:    */   public final FileDescriptor fd()
/* 101:    */   {
/* 102:120 */     return this.socket;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isActive()
/* 106:    */   {
/* 107:125 */     return this.active;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public ChannelMetadata metadata()
/* 111:    */   {
/* 112:130 */     return METADATA;
/* 113:    */   }
/* 114:    */   
/* 115:    */   protected void doClose()
/* 116:    */     throws Exception
/* 117:    */   {
/* 118:135 */     this.active = false;
/* 119:    */     
/* 120:    */ 
/* 121:138 */     this.inputClosedSeenErrorOnRead = true;
/* 122:    */     try
/* 123:    */     {
/* 124:140 */       if (isRegistered())
/* 125:    */       {
/* 126:149 */         EventLoop loop = eventLoop();
/* 127:150 */         if (loop.inEventLoop()) {
/* 128:151 */           doDeregister();
/* 129:    */         } else {
/* 130:153 */           loop.execute(new Runnable()
/* 131:    */           {
/* 132:    */             public void run()
/* 133:    */             {
/* 134:    */               try
/* 135:    */               {
/* 136:157 */                 AbstractKQueueChannel.this.doDeregister();
/* 137:    */               }
/* 138:    */               catch (Throwable cause)
/* 139:    */               {
/* 140:159 */                 AbstractKQueueChannel.this.pipeline().fireExceptionCaught(cause);
/* 141:    */               }
/* 142:    */             }
/* 143:    */           });
/* 144:    */         }
/* 145:    */       }
/* 146:166 */       this.socket.close();
/* 147:    */     }
/* 148:    */     finally
/* 149:    */     {
/* 150:166 */       this.socket.close();
/* 151:    */     }
/* 152:    */   }
/* 153:    */   
/* 154:    */   protected void doDisconnect()
/* 155:    */     throws Exception
/* 156:    */   {
/* 157:172 */     doClose();
/* 158:    */   }
/* 159:    */   
/* 160:    */   protected boolean isCompatible(EventLoop loop)
/* 161:    */   {
/* 162:177 */     return loop instanceof KQueueEventLoop;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public boolean isOpen()
/* 166:    */   {
/* 167:182 */     return this.socket.isOpen();
/* 168:    */   }
/* 169:    */   
/* 170:    */   protected void doDeregister()
/* 171:    */     throws Exception
/* 172:    */   {
/* 173:188 */     readFilter(false);
/* 174:189 */     writeFilter(false);
/* 175:190 */     evSet0(Native.EVFILT_SOCK, Native.EV_DELETE, 0);
/* 176:    */     
/* 177:192 */     ((KQueueEventLoop)eventLoop()).remove(this);
/* 178:    */     
/* 179:    */ 
/* 180:195 */     this.readFilterEnabled = true;
/* 181:    */   }
/* 182:    */   
/* 183:    */   protected final void doBeginRead()
/* 184:    */     throws Exception
/* 185:    */   {
/* 186:201 */     AbstractKQueueUnsafe unsafe = (AbstractKQueueUnsafe)unsafe();
/* 187:202 */     unsafe.readPending = true;
/* 188:    */     
/* 189:    */ 
/* 190:    */ 
/* 191:    */ 
/* 192:207 */     readFilter(true);
/* 193:211 */     if (unsafe.maybeMoreDataToRead) {
/* 194:212 */       unsafe.executeReadReadyRunnable(config());
/* 195:    */     }
/* 196:    */   }
/* 197:    */   
/* 198:    */   protected void doRegister()
/* 199:    */     throws Exception
/* 200:    */   {
/* 201:221 */     this.readReadyRunnablePending = false;
/* 202:223 */     if (this.writeFilterEnabled) {
/* 203:224 */       evSet0(Native.EVFILT_WRITE, Native.EV_ADD_CLEAR_ENABLE);
/* 204:    */     }
/* 205:226 */     if (this.readFilterEnabled) {
/* 206:227 */       evSet0(Native.EVFILT_READ, Native.EV_ADD_CLEAR_ENABLE);
/* 207:    */     }
/* 208:229 */     evSet0(Native.EVFILT_SOCK, Native.EV_ADD, Native.NOTE_RDHUP);
/* 209:    */   }
/* 210:    */   
/* 211:    */   protected abstract AbstractKQueueUnsafe newUnsafe();
/* 212:    */   
/* 213:    */   public abstract KQueueChannelConfig config();
/* 214:    */   
/* 215:    */   protected final ByteBuf newDirectBuffer(ByteBuf buf)
/* 216:    */   {
/* 217:242 */     return newDirectBuffer(buf, buf);
/* 218:    */   }
/* 219:    */   
/* 220:    */   protected final ByteBuf newDirectBuffer(Object holder, ByteBuf buf)
/* 221:    */   {
/* 222:251 */     int readableBytes = buf.readableBytes();
/* 223:252 */     if (readableBytes == 0)
/* 224:    */     {
/* 225:253 */       ReferenceCountUtil.release(holder);
/* 226:254 */       return Unpooled.EMPTY_BUFFER;
/* 227:    */     }
/* 228:257 */     ByteBufAllocator alloc = alloc();
/* 229:258 */     if (alloc.isDirectBufferPooled()) {
/* 230:259 */       return newDirectBuffer0(holder, buf, alloc, readableBytes);
/* 231:    */     }
/* 232:262 */     ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
/* 233:263 */     if (directBuf == null) {
/* 234:264 */       return newDirectBuffer0(holder, buf, alloc, readableBytes);
/* 235:    */     }
/* 236:267 */     directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 237:268 */     ReferenceCountUtil.safeRelease(holder);
/* 238:269 */     return directBuf;
/* 239:    */   }
/* 240:    */   
/* 241:    */   private static ByteBuf newDirectBuffer0(Object holder, ByteBuf buf, ByteBufAllocator alloc, int capacity)
/* 242:    */   {
/* 243:273 */     ByteBuf directBuf = alloc.directBuffer(capacity);
/* 244:274 */     directBuf.writeBytes(buf, buf.readerIndex(), capacity);
/* 245:275 */     ReferenceCountUtil.safeRelease(holder);
/* 246:276 */     return directBuf;
/* 247:    */   }
/* 248:    */   
/* 249:    */   protected static void checkResolvable(InetSocketAddress addr)
/* 250:    */   {
/* 251:280 */     if (addr.isUnresolved()) {
/* 252:281 */       throw new UnresolvedAddressException();
/* 253:    */     }
/* 254:    */   }
/* 255:    */   
/* 256:    */   protected final int doReadBytes(ByteBuf byteBuf)
/* 257:    */     throws Exception
/* 258:    */   {
/* 259:289 */     int writerIndex = byteBuf.writerIndex();
/* 260:    */     
/* 261:291 */     unsafe().recvBufAllocHandle().attemptedBytesRead(byteBuf.writableBytes());
/* 262:    */     int localReadAmount;
/* 263:    */     int localReadAmount;
/* 264:292 */     if (byteBuf.hasMemoryAddress())
/* 265:    */     {
/* 266:293 */       localReadAmount = this.socket.readAddress(byteBuf.memoryAddress(), writerIndex, byteBuf.capacity());
/* 267:    */     }
/* 268:    */     else
/* 269:    */     {
/* 270:295 */       ByteBuffer buf = byteBuf.internalNioBuffer(writerIndex, byteBuf.writableBytes());
/* 271:296 */       localReadAmount = this.socket.read(buf, buf.position(), buf.limit());
/* 272:    */     }
/* 273:298 */     if (localReadAmount > 0) {
/* 274:299 */       byteBuf.writerIndex(writerIndex + localReadAmount);
/* 275:    */     }
/* 276:301 */     return localReadAmount;
/* 277:    */   }
/* 278:    */   
/* 279:    */   protected final int doWriteBytes(ChannelOutboundBuffer in, ByteBuf buf)
/* 280:    */     throws Exception
/* 281:    */   {
/* 282:305 */     if (buf.hasMemoryAddress())
/* 283:    */     {
/* 284:306 */       int localFlushedAmount = this.socket.writeAddress(buf.memoryAddress(), buf.readerIndex(), buf.writerIndex());
/* 285:307 */       if (localFlushedAmount > 0)
/* 286:    */       {
/* 287:308 */         in.removeBytes(localFlushedAmount);
/* 288:309 */         return 1;
/* 289:    */       }
/* 290:    */     }
/* 291:    */     else
/* 292:    */     {
/* 293:313 */       ByteBuffer nioBuf = buf.nioBufferCount() == 1 ? buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()) : buf.nioBuffer();
/* 294:314 */       int localFlushedAmount = this.socket.write(nioBuf, nioBuf.position(), nioBuf.limit());
/* 295:315 */       if (localFlushedAmount > 0)
/* 296:    */       {
/* 297:316 */         nioBuf.position(nioBuf.position() + localFlushedAmount);
/* 298:317 */         in.removeBytes(localFlushedAmount);
/* 299:318 */         return 1;
/* 300:    */       }
/* 301:    */     }
/* 302:321 */     return 2147483647;
/* 303:    */   }
/* 304:    */   
/* 305:    */   final boolean shouldBreakReadReady(ChannelConfig config)
/* 306:    */   {
/* 307:325 */     return (this.socket.isInputShutdown()) && ((this.inputClosedSeenErrorOnRead) || (!isAllowHalfClosure(config)));
/* 308:    */   }
/* 309:    */   
/* 310:    */   private static boolean isAllowHalfClosure(ChannelConfig config)
/* 311:    */   {
/* 312:329 */     return ((config instanceof KQueueSocketChannelConfig)) && 
/* 313:330 */       (((KQueueSocketChannelConfig)config).isAllowHalfClosure());
/* 314:    */   }
/* 315:    */   
/* 316:    */   final void clearReadFilter()
/* 317:    */   {
/* 318:335 */     if (isRegistered())
/* 319:    */     {
/* 320:336 */       EventLoop loop = eventLoop();
/* 321:337 */       final AbstractKQueueUnsafe unsafe = (AbstractKQueueUnsafe)unsafe();
/* 322:338 */       if (loop.inEventLoop()) {
/* 323:339 */         unsafe.clearReadFilter0();
/* 324:    */       } else {
/* 325:342 */         loop.execute(new Runnable()
/* 326:    */         {
/* 327:    */           public void run()
/* 328:    */           {
/* 329:345 */             if ((!unsafe.readPending) && (!AbstractKQueueChannel.this.config().isAutoRead())) {
/* 330:347 */               unsafe.clearReadFilter0();
/* 331:    */             }
/* 332:    */           }
/* 333:    */         });
/* 334:    */       }
/* 335:    */     }
/* 336:    */     else
/* 337:    */     {
/* 338:355 */       this.readFilterEnabled = false;
/* 339:    */     }
/* 340:    */   }
/* 341:    */   
/* 342:    */   void readFilter(boolean readFilterEnabled)
/* 343:    */     throws IOException
/* 344:    */   {
/* 345:360 */     if (this.readFilterEnabled != readFilterEnabled)
/* 346:    */     {
/* 347:361 */       this.readFilterEnabled = readFilterEnabled;
/* 348:362 */       evSet(Native.EVFILT_READ, readFilterEnabled ? Native.EV_ADD_CLEAR_ENABLE : Native.EV_DELETE_DISABLE);
/* 349:    */     }
/* 350:    */   }
/* 351:    */   
/* 352:    */   void writeFilter(boolean writeFilterEnabled)
/* 353:    */     throws IOException
/* 354:    */   {
/* 355:367 */     if (this.writeFilterEnabled != writeFilterEnabled)
/* 356:    */     {
/* 357:368 */       this.writeFilterEnabled = writeFilterEnabled;
/* 358:369 */       evSet(Native.EVFILT_WRITE, writeFilterEnabled ? Native.EV_ADD_CLEAR_ENABLE : Native.EV_DELETE_DISABLE);
/* 359:    */     }
/* 360:    */   }
/* 361:    */   
/* 362:    */   private void evSet(short filter, short flags)
/* 363:    */   {
/* 364:374 */     if ((isOpen()) && (isRegistered())) {
/* 365:375 */       evSet0(filter, flags);
/* 366:    */     }
/* 367:    */   }
/* 368:    */   
/* 369:    */   private void evSet0(short filter, short flags)
/* 370:    */   {
/* 371:380 */     evSet0(filter, flags, 0);
/* 372:    */   }
/* 373:    */   
/* 374:    */   private void evSet0(short filter, short flags, int fflags)
/* 375:    */   {
/* 376:384 */     ((KQueueEventLoop)eventLoop()).evSet(this, filter, flags, fflags);
/* 377:    */   }
/* 378:    */   
/* 379:    */   abstract class AbstractKQueueUnsafe
/* 380:    */     extends AbstractChannel.AbstractUnsafe
/* 381:    */   {
/* 382:    */     boolean readPending;
/* 383:    */     boolean maybeMoreDataToRead;
/* 384:    */     private KQueueRecvByteAllocatorHandle allocHandle;
/* 385:    */     
/* 386:    */     AbstractKQueueUnsafe()
/* 387:    */     {
/* 388:387 */       super();
/* 389:    */     }
/* 390:    */     
/* 391:391 */     private final Runnable readReadyRunnable = new Runnable()
/* 392:    */     {
/* 393:    */       public void run()
/* 394:    */       {
/* 395:394 */         AbstractKQueueChannel.this.readReadyRunnablePending = false;
/* 396:395 */         AbstractKQueueChannel.AbstractKQueueUnsafe.this.readReady(AbstractKQueueChannel.AbstractKQueueUnsafe.this.recvBufAllocHandle());
/* 397:    */       }
/* 398:    */     };
/* 399:    */     
/* 400:    */     final void readReady(long numberBytesPending)
/* 401:    */     {
/* 402:400 */       KQueueRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/* 403:401 */       allocHandle.numberBytesPending(numberBytesPending);
/* 404:402 */       readReady(allocHandle);
/* 405:    */     }
/* 406:    */     
/* 407:    */     abstract void readReady(KQueueRecvByteAllocatorHandle paramKQueueRecvByteAllocatorHandle);
/* 408:    */     
/* 409:    */     final void readReadyBefore()
/* 410:    */     {
/* 411:407 */       this.maybeMoreDataToRead = false;
/* 412:    */     }
/* 413:    */     
/* 414:    */     final void readReadyFinally(ChannelConfig config)
/* 415:    */     {
/* 416:410 */       this.maybeMoreDataToRead = this.allocHandle.maybeMoreDataToRead();
/* 417:417 */       if ((!this.readPending) && (!config.isAutoRead())) {
/* 418:418 */         clearReadFilter0();
/* 419:419 */       } else if ((this.readPending) && (this.maybeMoreDataToRead)) {
/* 420:427 */         executeReadReadyRunnable(config);
/* 421:    */       }
/* 422:    */     }
/* 423:    */     
/* 424:    */     final void writeReady()
/* 425:    */     {
/* 426:432 */       if (AbstractKQueueChannel.this.connectPromise != null) {
/* 427:434 */         finishConnect();
/* 428:435 */       } else if (!AbstractKQueueChannel.this.socket.isOutputShutdown()) {
/* 429:437 */         super.flush0();
/* 430:    */       }
/* 431:    */     }
/* 432:    */     
/* 433:    */     void shutdownInput(boolean readEOF)
/* 434:    */     {
/* 435:450 */       if ((readEOF) && (AbstractKQueueChannel.this.connectPromise != null)) {
/* 436:451 */         finishConnect();
/* 437:    */       }
/* 438:453 */       if (!AbstractKQueueChannel.this.socket.isInputShutdown())
/* 439:    */       {
/* 440:454 */         if (AbstractKQueueChannel.isAllowHalfClosure(AbstractKQueueChannel.this.config()))
/* 441:    */         {
/* 442:    */           try
/* 443:    */           {
/* 444:456 */             AbstractKQueueChannel.this.socket.shutdown(true, false);
/* 445:    */           }
/* 446:    */           catch (IOException ignored)
/* 447:    */           {
/* 448:460 */             fireEventAndClose(ChannelInputShutdownEvent.INSTANCE);
/* 449:461 */             return;
/* 450:    */           }
/* 451:    */           catch (NotYetConnectedException localNotYetConnectedException) {}
/* 452:466 */           AbstractKQueueChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/* 453:    */         }
/* 454:    */         else
/* 455:    */         {
/* 456:468 */           close(voidPromise());
/* 457:    */         }
/* 458:    */       }
/* 459:470 */       else if (!readEOF)
/* 460:    */       {
/* 461:471 */         AbstractKQueueChannel.this.inputClosedSeenErrorOnRead = true;
/* 462:472 */         AbstractKQueueChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
/* 463:    */       }
/* 464:    */     }
/* 465:    */     
/* 466:    */     final void readEOF()
/* 467:    */     {
/* 468:478 */       KQueueRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/* 469:479 */       allocHandle.readEOF();
/* 470:481 */       if (AbstractKQueueChannel.this.isActive()) {
/* 471:485 */         readReady(allocHandle);
/* 472:    */       } else {
/* 473:488 */         shutdownInput(true);
/* 474:    */       }
/* 475:    */     }
/* 476:    */     
/* 477:    */     public KQueueRecvByteAllocatorHandle recvBufAllocHandle()
/* 478:    */     {
/* 479:494 */       if (this.allocHandle == null) {
/* 480:496 */         this.allocHandle = new KQueueRecvByteAllocatorHandle((RecvByteBufAllocator.ExtendedHandle)super.recvBufAllocHandle());
/* 481:    */       }
/* 482:498 */       return this.allocHandle;
/* 483:    */     }
/* 484:    */     
/* 485:    */     final void executeReadReadyRunnable(ChannelConfig config)
/* 486:    */     {
/* 487:502 */       if ((AbstractKQueueChannel.this.readReadyRunnablePending) || (!AbstractKQueueChannel.this.isActive()) || (AbstractKQueueChannel.this.shouldBreakReadReady(config))) {
/* 488:503 */         return;
/* 489:    */       }
/* 490:505 */       AbstractKQueueChannel.this.readReadyRunnablePending = true;
/* 491:506 */       AbstractKQueueChannel.this.eventLoop().execute(this.readReadyRunnable);
/* 492:    */     }
/* 493:    */     
/* 494:    */     protected final void clearReadFilter0()
/* 495:    */     {
/* 496:510 */       assert (AbstractKQueueChannel.this.eventLoop().inEventLoop());
/* 497:    */       try
/* 498:    */       {
/* 499:512 */         this.readPending = false;
/* 500:513 */         AbstractKQueueChannel.this.readFilter(false);
/* 501:    */       }
/* 502:    */       catch (IOException e)
/* 503:    */       {
/* 504:517 */         AbstractKQueueChannel.this.pipeline().fireExceptionCaught(e);
/* 505:518 */         AbstractKQueueChannel.this.unsafe().close(AbstractKQueueChannel.this.unsafe().voidPromise());
/* 506:    */       }
/* 507:    */     }
/* 508:    */     
/* 509:    */     private void fireEventAndClose(Object evt)
/* 510:    */     {
/* 511:523 */       AbstractKQueueChannel.this.pipeline().fireUserEventTriggered(evt);
/* 512:524 */       close(voidPromise());
/* 513:    */     }
/* 514:    */     
/* 515:    */     public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 516:    */     {
/* 517:530 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 518:531 */         return;
/* 519:    */       }
/* 520:    */       try
/* 521:    */       {
/* 522:535 */         if (AbstractKQueueChannel.this.connectPromise != null) {
/* 523:536 */           throw new ConnectionPendingException();
/* 524:    */         }
/* 525:539 */         boolean wasActive = AbstractKQueueChannel.this.isActive();
/* 526:540 */         if (AbstractKQueueChannel.this.doConnect(remoteAddress, localAddress))
/* 527:    */         {
/* 528:541 */           fulfillConnectPromise(promise, wasActive);
/* 529:    */         }
/* 530:    */         else
/* 531:    */         {
/* 532:543 */           AbstractKQueueChannel.this.connectPromise = promise;
/* 533:544 */           AbstractKQueueChannel.this.requestedRemoteAddress = remoteAddress;
/* 534:    */           
/* 535:    */ 
/* 536:547 */           int connectTimeoutMillis = AbstractKQueueChannel.this.config().getConnectTimeoutMillis();
/* 537:548 */           if (connectTimeoutMillis > 0) {
/* 538:549 */             AbstractKQueueChannel.this.connectTimeoutFuture = AbstractKQueueChannel.this.eventLoop().schedule(new Runnable()
/* 539:    */             {
/* 540:    */               public void run()
/* 541:    */               {
/* 542:552 */                 ChannelPromise connectPromise = AbstractKQueueChannel.this.connectPromise;
/* 543:553 */                 ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 544:555 */                 if ((connectPromise != null) && (connectPromise.tryFailure(cause))) {
/* 545:556 */                   AbstractKQueueChannel.AbstractKQueueUnsafe.this.close(AbstractKQueueChannel.AbstractKQueueUnsafe.this.voidPromise());
/* 546:    */                 }
/* 547:    */               }
/* 548:556 */             }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
/* 549:    */           }
/* 550:562 */           promise.addListener(new ChannelFutureListener()
/* 551:    */           {
/* 552:    */             public void operationComplete(ChannelFuture future)
/* 553:    */               throws Exception
/* 554:    */             {
/* 555:565 */               if (future.isCancelled())
/* 556:    */               {
/* 557:566 */                 if (AbstractKQueueChannel.this.connectTimeoutFuture != null) {
/* 558:567 */                   AbstractKQueueChannel.this.connectTimeoutFuture.cancel(false);
/* 559:    */                 }
/* 560:569 */                 AbstractKQueueChannel.this.connectPromise = null;
/* 561:570 */                 AbstractKQueueChannel.AbstractKQueueUnsafe.this.close(AbstractKQueueChannel.AbstractKQueueUnsafe.this.voidPromise());
/* 562:    */               }
/* 563:    */             }
/* 564:    */           });
/* 565:    */         }
/* 566:    */       }
/* 567:    */       catch (Throwable t)
/* 568:    */       {
/* 569:576 */         closeIfClosed();
/* 570:577 */         promise.tryFailure(annotateConnectException(t, remoteAddress));
/* 571:    */       }
/* 572:    */     }
/* 573:    */     
/* 574:    */     private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
/* 575:    */     {
/* 576:582 */       if (promise == null) {
/* 577:584 */         return;
/* 578:    */       }
/* 579:586 */       AbstractKQueueChannel.this.active = true;
/* 580:    */       
/* 581:    */ 
/* 582:    */ 
/* 583:590 */       boolean active = AbstractKQueueChannel.this.isActive();
/* 584:    */       
/* 585:    */ 
/* 586:593 */       boolean promiseSet = promise.trySuccess();
/* 587:597 */       if ((!wasActive) && (active)) {
/* 588:598 */         AbstractKQueueChannel.this.pipeline().fireChannelActive();
/* 589:    */       }
/* 590:602 */       if (!promiseSet) {
/* 591:603 */         close(voidPromise());
/* 592:    */       }
/* 593:    */     }
/* 594:    */     
/* 595:    */     private void fulfillConnectPromise(ChannelPromise promise, Throwable cause)
/* 596:    */     {
/* 597:608 */       if (promise == null) {
/* 598:610 */         return;
/* 599:    */       }
/* 600:614 */       promise.tryFailure(cause);
/* 601:615 */       closeIfClosed();
/* 602:    */     }
/* 603:    */     
/* 604:    */     private void finishConnect()
/* 605:    */     {
/* 606:622 */       assert (AbstractKQueueChannel.this.eventLoop().inEventLoop());
/* 607:    */       
/* 608:624 */       boolean connectStillInProgress = false;
/* 609:    */       try
/* 610:    */       {
/* 611:626 */         boolean wasActive = AbstractKQueueChannel.this.isActive();
/* 612:627 */         if (!doFinishConnect())
/* 613:    */         {
/* 614:628 */           connectStillInProgress = true;
/* 615:629 */           return;
/* 616:    */         }
/* 617:631 */         fulfillConnectPromise(AbstractKQueueChannel.this.connectPromise, wasActive);
/* 618:    */       }
/* 619:    */       catch (Throwable t)
/* 620:    */       {
/* 621:633 */         fulfillConnectPromise(AbstractKQueueChannel.this.connectPromise, annotateConnectException(t, AbstractKQueueChannel.this.requestedRemoteAddress));
/* 622:    */       }
/* 623:    */       finally
/* 624:    */       {
/* 625:635 */         if (!connectStillInProgress)
/* 626:    */         {
/* 627:638 */           if (AbstractKQueueChannel.this.connectTimeoutFuture != null) {
/* 628:639 */             AbstractKQueueChannel.this.connectTimeoutFuture.cancel(false);
/* 629:    */           }
/* 630:641 */           AbstractKQueueChannel.this.connectPromise = null;
/* 631:    */         }
/* 632:    */       }
/* 633:    */     }
/* 634:    */     
/* 635:    */     private boolean doFinishConnect()
/* 636:    */       throws Exception
/* 637:    */     {
/* 638:647 */       if (AbstractKQueueChannel.this.socket.finishConnect())
/* 639:    */       {
/* 640:648 */         AbstractKQueueChannel.this.writeFilter(false);
/* 641:649 */         if ((AbstractKQueueChannel.this.requestedRemoteAddress instanceof InetSocketAddress)) {
/* 642:650 */           AbstractKQueueChannel.this.remote = UnixChannelUtil.computeRemoteAddr((InetSocketAddress)AbstractKQueueChannel.this.requestedRemoteAddress, AbstractKQueueChannel.this.socket.remoteAddress());
/* 643:    */         }
/* 644:652 */         AbstractKQueueChannel.this.requestedRemoteAddress = null;
/* 645:653 */         return true;
/* 646:    */       }
/* 647:655 */       AbstractKQueueChannel.this.writeFilter(true);
/* 648:656 */       return false;
/* 649:    */     }
/* 650:    */   }
/* 651:    */   
/* 652:    */   protected void doBind(SocketAddress local)
/* 653:    */     throws Exception
/* 654:    */   {
/* 655:662 */     if ((local instanceof InetSocketAddress)) {
/* 656:663 */       checkResolvable((InetSocketAddress)local);
/* 657:    */     }
/* 658:665 */     this.socket.bind(local);
/* 659:666 */     this.local = this.socket.localAddress();
/* 660:    */   }
/* 661:    */   
/* 662:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 663:    */     throws Exception
/* 664:    */   {
/* 665:673 */     if ((localAddress instanceof InetSocketAddress)) {
/* 666:674 */       checkResolvable((InetSocketAddress)localAddress);
/* 667:    */     }
/* 668:677 */     InetSocketAddress remoteSocketAddr = (remoteAddress instanceof InetSocketAddress) ? (InetSocketAddress)remoteAddress : null;
/* 669:679 */     if (remoteSocketAddr != null) {
/* 670:680 */       checkResolvable(remoteSocketAddr);
/* 671:    */     }
/* 672:683 */     if (this.remote != null) {
/* 673:687 */       throw new AlreadyConnectedException();
/* 674:    */     }
/* 675:690 */     if (localAddress != null) {
/* 676:691 */       this.socket.bind(localAddress);
/* 677:    */     }
/* 678:694 */     boolean connected = doConnect0(remoteAddress);
/* 679:695 */     if (connected) {
/* 680:697 */       this.remote = (remoteSocketAddr == null ? remoteAddress : UnixChannelUtil.computeRemoteAddr(remoteSocketAddr, this.socket.remoteAddress()));
/* 681:    */     }
/* 682:702 */     this.local = this.socket.localAddress();
/* 683:703 */     return connected;
/* 684:    */   }
/* 685:    */   
/* 686:    */   private boolean doConnect0(SocketAddress remote)
/* 687:    */     throws Exception
/* 688:    */   {
/* 689:707 */     boolean success = false;
/* 690:    */     try
/* 691:    */     {
/* 692:709 */       boolean connected = this.socket.connect(remote);
/* 693:710 */       if (!connected) {
/* 694:711 */         writeFilter(true);
/* 695:    */       }
/* 696:713 */       success = true;
/* 697:714 */       return connected;
/* 698:    */     }
/* 699:    */     finally
/* 700:    */     {
/* 701:716 */       if (!success) {
/* 702:717 */         doClose();
/* 703:    */       }
/* 704:    */     }
/* 705:    */   }
/* 706:    */   
/* 707:    */   protected SocketAddress localAddress0()
/* 708:    */   {
/* 709:724 */     return this.local;
/* 710:    */   }
/* 711:    */   
/* 712:    */   protected SocketAddress remoteAddress0()
/* 713:    */   {
/* 714:729 */     return this.remote;
/* 715:    */   }
/* 716:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.AbstractKQueueChannel
 * JD-Core Version:    0.7.0.1
 */