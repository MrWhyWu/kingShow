/*   1:    */ package io.netty.channel.epoll;
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
/*  26:    */ import io.netty.channel.unix.Socket;
/*  27:    */ import io.netty.channel.unix.UnixChannel;
/*  28:    */ import io.netty.channel.unix.UnixChannelUtil;
/*  29:    */ import io.netty.util.ReferenceCountUtil;
/*  30:    */ import io.netty.util.internal.ObjectUtil;
/*  31:    */ import io.netty.util.internal.ThrowableUtil;
/*  32:    */ import java.io.IOException;
/*  33:    */ import java.net.InetSocketAddress;
/*  34:    */ import java.net.SocketAddress;
/*  35:    */ import java.nio.ByteBuffer;
/*  36:    */ import java.nio.channels.AlreadyConnectedException;
/*  37:    */ import java.nio.channels.ClosedChannelException;
/*  38:    */ import java.nio.channels.ConnectionPendingException;
/*  39:    */ import java.nio.channels.NotYetConnectedException;
/*  40:    */ import java.nio.channels.UnresolvedAddressException;
/*  41:    */ import java.util.concurrent.ScheduledFuture;
/*  42:    */ import java.util.concurrent.TimeUnit;
/*  43:    */ 
/*  44:    */ abstract class AbstractEpollChannel
/*  45:    */   extends AbstractChannel
/*  46:    */   implements UnixChannel
/*  47:    */ {
/*  48: 59 */   private static final ClosedChannelException DO_CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollChannel.class, "doClose()");
/*  49: 61 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  50:    */   private final int readFlag;
/*  51:    */   final LinuxSocket socket;
/*  52:    */   private ChannelPromise connectPromise;
/*  53:    */   private ScheduledFuture<?> connectTimeoutFuture;
/*  54:    */   private SocketAddress requestedRemoteAddress;
/*  55:    */   private volatile SocketAddress local;
/*  56:    */   private volatile SocketAddress remote;
/*  57: 75 */   protected int flags = Native.EPOLLET;
/*  58:    */   boolean inputClosedSeenErrorOnRead;
/*  59:    */   boolean epollInReadyRunnablePending;
/*  60:    */   protected volatile boolean active;
/*  61:    */   
/*  62:    */   AbstractEpollChannel(LinuxSocket fd, int flag)
/*  63:    */   {
/*  64: 82 */     this(null, fd, flag, false);
/*  65:    */   }
/*  66:    */   
/*  67:    */   AbstractEpollChannel(Channel parent, LinuxSocket fd, int flag, boolean active)
/*  68:    */   {
/*  69: 86 */     super(parent);
/*  70: 87 */     this.socket = ((LinuxSocket)ObjectUtil.checkNotNull(fd, "fd"));
/*  71: 88 */     this.readFlag = flag;
/*  72: 89 */     this.flags |= flag;
/*  73: 90 */     this.active = active;
/*  74: 91 */     if (active)
/*  75:    */     {
/*  76: 94 */       this.local = fd.localAddress();
/*  77: 95 */       this.remote = fd.remoteAddress();
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   AbstractEpollChannel(Channel parent, LinuxSocket fd, int flag, SocketAddress remote)
/*  82:    */   {
/*  83:100 */     super(parent);
/*  84:101 */     this.socket = ((LinuxSocket)ObjectUtil.checkNotNull(fd, "fd"));
/*  85:102 */     this.readFlag = flag;
/*  86:103 */     this.flags |= flag;
/*  87:104 */     this.active = true;
/*  88:    */     
/*  89:    */ 
/*  90:107 */     this.remote = remote;
/*  91:108 */     this.local = fd.localAddress();
/*  92:    */   }
/*  93:    */   
/*  94:    */   static boolean isSoErrorZero(Socket fd)
/*  95:    */   {
/*  96:    */     try
/*  97:    */     {
/*  98:113 */       return fd.getSoError() == 0;
/*  99:    */     }
/* 100:    */     catch (IOException e)
/* 101:    */     {
/* 102:115 */       throw new ChannelException(e);
/* 103:    */     }
/* 104:    */   }
/* 105:    */   
/* 106:    */   void setFlag(int flag)
/* 107:    */     throws IOException
/* 108:    */   {
/* 109:120 */     if (!isFlagSet(flag))
/* 110:    */     {
/* 111:121 */       this.flags |= flag;
/* 112:122 */       modifyEvents();
/* 113:    */     }
/* 114:    */   }
/* 115:    */   
/* 116:    */   void clearFlag(int flag)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:127 */     if (isFlagSet(flag))
/* 120:    */     {
/* 121:128 */       this.flags &= (flag ^ 0xFFFFFFFF);
/* 122:129 */       modifyEvents();
/* 123:    */     }
/* 124:    */   }
/* 125:    */   
/* 126:    */   boolean isFlagSet(int flag)
/* 127:    */   {
/* 128:134 */     return (this.flags & flag) != 0;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public final FileDescriptor fd()
/* 132:    */   {
/* 133:139 */     return this.socket;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public abstract EpollChannelConfig config();
/* 137:    */   
/* 138:    */   public boolean isActive()
/* 139:    */   {
/* 140:147 */     return this.active;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public ChannelMetadata metadata()
/* 144:    */   {
/* 145:152 */     return METADATA;
/* 146:    */   }
/* 147:    */   
/* 148:    */   protected void doClose()
/* 149:    */     throws Exception
/* 150:    */   {
/* 151:157 */     this.active = false;
/* 152:    */     
/* 153:    */ 
/* 154:160 */     this.inputClosedSeenErrorOnRead = true;
/* 155:    */     try
/* 156:    */     {
/* 157:162 */       ChannelPromise promise = this.connectPromise;
/* 158:163 */       if (promise != null)
/* 159:    */       {
/* 160:165 */         promise.tryFailure(DO_CLOSE_CLOSED_CHANNEL_EXCEPTION);
/* 161:166 */         this.connectPromise = null;
/* 162:    */       }
/* 163:169 */       ScheduledFuture<?> future = this.connectTimeoutFuture;
/* 164:170 */       if (future != null)
/* 165:    */       {
/* 166:171 */         future.cancel(false);
/* 167:172 */         this.connectTimeoutFuture = null;
/* 168:    */       }
/* 169:175 */       if (isRegistered())
/* 170:    */       {
/* 171:180 */         EventLoop loop = eventLoop();
/* 172:181 */         if (loop.inEventLoop()) {
/* 173:182 */           doDeregister();
/* 174:    */         } else {
/* 175:184 */           loop.execute(new Runnable()
/* 176:    */           {
/* 177:    */             public void run()
/* 178:    */             {
/* 179:    */               try
/* 180:    */               {
/* 181:188 */                 AbstractEpollChannel.this.doDeregister();
/* 182:    */               }
/* 183:    */               catch (Throwable cause)
/* 184:    */               {
/* 185:190 */                 AbstractEpollChannel.this.pipeline().fireExceptionCaught(cause);
/* 186:    */               }
/* 187:    */             }
/* 188:    */           });
/* 189:    */         }
/* 190:    */       }
/* 191:    */     }
/* 192:    */     finally
/* 193:    */     {
/* 194:197 */       this.socket.close();
/* 195:    */     }
/* 196:    */   }
/* 197:    */   
/* 198:    */   protected void doDisconnect()
/* 199:    */     throws Exception
/* 200:    */   {
/* 201:203 */     doClose();
/* 202:    */   }
/* 203:    */   
/* 204:    */   protected boolean isCompatible(EventLoop loop)
/* 205:    */   {
/* 206:208 */     return loop instanceof EpollEventLoop;
/* 207:    */   }
/* 208:    */   
/* 209:    */   public boolean isOpen()
/* 210:    */   {
/* 211:213 */     return this.socket.isOpen();
/* 212:    */   }
/* 213:    */   
/* 214:    */   protected void doDeregister()
/* 215:    */     throws Exception
/* 216:    */   {
/* 217:218 */     ((EpollEventLoop)eventLoop()).remove(this);
/* 218:    */   }
/* 219:    */   
/* 220:    */   protected final void doBeginRead()
/* 221:    */     throws Exception
/* 222:    */   {
/* 223:224 */     AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)unsafe();
/* 224:225 */     unsafe.readPending = true;
/* 225:    */     
/* 226:    */ 
/* 227:    */ 
/* 228:    */ 
/* 229:230 */     setFlag(this.readFlag);
/* 230:234 */     if (unsafe.maybeMoreDataToRead) {
/* 231:235 */       unsafe.executeEpollInReadyRunnable(config());
/* 232:    */     }
/* 233:    */   }
/* 234:    */   
/* 235:    */   final boolean shouldBreakEpollInReady(ChannelConfig config)
/* 236:    */   {
/* 237:240 */     return (this.socket.isInputShutdown()) && ((this.inputClosedSeenErrorOnRead) || (!isAllowHalfClosure(config)));
/* 238:    */   }
/* 239:    */   
/* 240:    */   private static boolean isAllowHalfClosure(ChannelConfig config)
/* 241:    */   {
/* 242:244 */     return ((config instanceof EpollSocketChannelConfig)) && 
/* 243:245 */       (((EpollSocketChannelConfig)config).isAllowHalfClosure());
/* 244:    */   }
/* 245:    */   
/* 246:    */   final void clearEpollIn()
/* 247:    */   {
/* 248:250 */     if (isRegistered())
/* 249:    */     {
/* 250:251 */       EventLoop loop = eventLoop();
/* 251:252 */       final AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)unsafe();
/* 252:253 */       if (loop.inEventLoop()) {
/* 253:254 */         unsafe.clearEpollIn0();
/* 254:    */       } else {
/* 255:257 */         loop.execute(new Runnable()
/* 256:    */         {
/* 257:    */           public void run()
/* 258:    */           {
/* 259:260 */             if ((!unsafe.readPending) && (!AbstractEpollChannel.this.config().isAutoRead())) {
/* 260:262 */               unsafe.clearEpollIn0();
/* 261:    */             }
/* 262:    */           }
/* 263:    */         });
/* 264:    */       }
/* 265:    */     }
/* 266:    */     else
/* 267:    */     {
/* 268:270 */       this.flags &= (this.readFlag ^ 0xFFFFFFFF);
/* 269:    */     }
/* 270:    */   }
/* 271:    */   
/* 272:    */   private void modifyEvents()
/* 273:    */     throws IOException
/* 274:    */   {
/* 275:275 */     if ((isOpen()) && (isRegistered())) {
/* 276:276 */       ((EpollEventLoop)eventLoop()).modify(this);
/* 277:    */     }
/* 278:    */   }
/* 279:    */   
/* 280:    */   protected void doRegister()
/* 281:    */     throws Exception
/* 282:    */   {
/* 283:285 */     this.epollInReadyRunnablePending = false;
/* 284:286 */     ((EpollEventLoop)eventLoop()).add(this);
/* 285:    */   }
/* 286:    */   
/* 287:    */   protected abstract AbstractEpollUnsafe newUnsafe();
/* 288:    */   
/* 289:    */   protected final ByteBuf newDirectBuffer(ByteBuf buf)
/* 290:    */   {
/* 291:296 */     return newDirectBuffer(buf, buf);
/* 292:    */   }
/* 293:    */   
/* 294:    */   protected final ByteBuf newDirectBuffer(Object holder, ByteBuf buf)
/* 295:    */   {
/* 296:305 */     int readableBytes = buf.readableBytes();
/* 297:306 */     if (readableBytes == 0)
/* 298:    */     {
/* 299:307 */       ReferenceCountUtil.release(holder);
/* 300:308 */       return Unpooled.EMPTY_BUFFER;
/* 301:    */     }
/* 302:311 */     ByteBufAllocator alloc = alloc();
/* 303:312 */     if (alloc.isDirectBufferPooled()) {
/* 304:313 */       return newDirectBuffer0(holder, buf, alloc, readableBytes);
/* 305:    */     }
/* 306:316 */     ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
/* 307:317 */     if (directBuf == null) {
/* 308:318 */       return newDirectBuffer0(holder, buf, alloc, readableBytes);
/* 309:    */     }
/* 310:321 */     directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 311:322 */     ReferenceCountUtil.safeRelease(holder);
/* 312:323 */     return directBuf;
/* 313:    */   }
/* 314:    */   
/* 315:    */   private static ByteBuf newDirectBuffer0(Object holder, ByteBuf buf, ByteBufAllocator alloc, int capacity)
/* 316:    */   {
/* 317:327 */     ByteBuf directBuf = alloc.directBuffer(capacity);
/* 318:328 */     directBuf.writeBytes(buf, buf.readerIndex(), capacity);
/* 319:329 */     ReferenceCountUtil.safeRelease(holder);
/* 320:330 */     return directBuf;
/* 321:    */   }
/* 322:    */   
/* 323:    */   protected static void checkResolvable(InetSocketAddress addr)
/* 324:    */   {
/* 325:334 */     if (addr.isUnresolved()) {
/* 326:335 */       throw new UnresolvedAddressException();
/* 327:    */     }
/* 328:    */   }
/* 329:    */   
/* 330:    */   protected final int doReadBytes(ByteBuf byteBuf)
/* 331:    */     throws Exception
/* 332:    */   {
/* 333:343 */     int writerIndex = byteBuf.writerIndex();
/* 334:    */     
/* 335:345 */     unsafe().recvBufAllocHandle().attemptedBytesRead(byteBuf.writableBytes());
/* 336:    */     int localReadAmount;
/* 337:    */     int localReadAmount;
/* 338:346 */     if (byteBuf.hasMemoryAddress())
/* 339:    */     {
/* 340:347 */       localReadAmount = this.socket.readAddress(byteBuf.memoryAddress(), writerIndex, byteBuf.capacity());
/* 341:    */     }
/* 342:    */     else
/* 343:    */     {
/* 344:349 */       ByteBuffer buf = byteBuf.internalNioBuffer(writerIndex, byteBuf.writableBytes());
/* 345:350 */       localReadAmount = this.socket.read(buf, buf.position(), buf.limit());
/* 346:    */     }
/* 347:352 */     if (localReadAmount > 0) {
/* 348:353 */       byteBuf.writerIndex(writerIndex + localReadAmount);
/* 349:    */     }
/* 350:355 */     return localReadAmount;
/* 351:    */   }
/* 352:    */   
/* 353:    */   protected final int doWriteBytes(ChannelOutboundBuffer in, ByteBuf buf)
/* 354:    */     throws Exception
/* 355:    */   {
/* 356:359 */     if (buf.hasMemoryAddress())
/* 357:    */     {
/* 358:360 */       int localFlushedAmount = this.socket.writeAddress(buf.memoryAddress(), buf.readerIndex(), buf.writerIndex());
/* 359:361 */       if (localFlushedAmount > 0)
/* 360:    */       {
/* 361:362 */         in.removeBytes(localFlushedAmount);
/* 362:363 */         return 1;
/* 363:    */       }
/* 364:    */     }
/* 365:    */     else
/* 366:    */     {
/* 367:367 */       ByteBuffer nioBuf = buf.nioBufferCount() == 1 ? buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()) : buf.nioBuffer();
/* 368:368 */       int localFlushedAmount = this.socket.write(nioBuf, nioBuf.position(), nioBuf.limit());
/* 369:369 */       if (localFlushedAmount > 0)
/* 370:    */       {
/* 371:370 */         nioBuf.position(nioBuf.position() + localFlushedAmount);
/* 372:371 */         in.removeBytes(localFlushedAmount);
/* 373:372 */         return 1;
/* 374:    */       }
/* 375:    */     }
/* 376:375 */     return 2147483647;
/* 377:    */   }
/* 378:    */   
/* 379:    */   protected abstract class AbstractEpollUnsafe
/* 380:    */     extends AbstractChannel.AbstractUnsafe
/* 381:    */   {
/* 382:    */     boolean readPending;
/* 383:    */     boolean maybeMoreDataToRead;
/* 384:    */     private EpollRecvByteAllocatorHandle allocHandle;
/* 385:    */     
/* 386:    */     protected AbstractEpollUnsafe()
/* 387:    */     {
/* 388:378 */       super();
/* 389:    */     }
/* 390:    */     
/* 391:382 */     private final Runnable epollInReadyRunnable = new Runnable()
/* 392:    */     {
/* 393:    */       public void run()
/* 394:    */       {
/* 395:385 */         AbstractEpollChannel.this.epollInReadyRunnablePending = false;
/* 396:386 */         AbstractEpollChannel.AbstractEpollUnsafe.this.epollInReady();
/* 397:    */       }
/* 398:    */     };
/* 399:    */     
/* 400:    */     abstract void epollInReady();
/* 401:    */     
/* 402:    */     final void epollInBefore()
/* 403:    */     {
/* 404:395 */       this.maybeMoreDataToRead = false;
/* 405:    */     }
/* 406:    */     
/* 407:    */     final void epollInFinally(ChannelConfig config)
/* 408:    */     {
/* 409:398 */       this.maybeMoreDataToRead = ((this.allocHandle.isEdgeTriggered()) && (this.allocHandle.maybeMoreDataToRead()));
/* 410:405 */       if ((!this.readPending) && (!config.isAutoRead())) {
/* 411:406 */         AbstractEpollChannel.this.clearEpollIn();
/* 412:407 */       } else if ((this.readPending) && (this.maybeMoreDataToRead)) {
/* 413:415 */         executeEpollInReadyRunnable(config);
/* 414:    */       }
/* 415:    */     }
/* 416:    */     
/* 417:    */     final void executeEpollInReadyRunnable(ChannelConfig config)
/* 418:    */     {
/* 419:420 */       if ((AbstractEpollChannel.this.epollInReadyRunnablePending) || (!AbstractEpollChannel.this.isActive()) || (AbstractEpollChannel.this.shouldBreakEpollInReady(config))) {
/* 420:421 */         return;
/* 421:    */       }
/* 422:423 */       AbstractEpollChannel.this.epollInReadyRunnablePending = true;
/* 423:424 */       AbstractEpollChannel.this.eventLoop().execute(this.epollInReadyRunnable);
/* 424:    */     }
/* 425:    */     
/* 426:    */     final void epollRdHupReady()
/* 427:    */     {
/* 428:432 */       recvBufAllocHandle().receivedRdHup();
/* 429:434 */       if (AbstractEpollChannel.this.isActive()) {
/* 430:438 */         epollInReady();
/* 431:    */       } else {
/* 432:441 */         shutdownInput(true);
/* 433:    */       }
/* 434:445 */       clearEpollRdHup();
/* 435:    */     }
/* 436:    */     
/* 437:    */     private void clearEpollRdHup()
/* 438:    */     {
/* 439:    */       try
/* 440:    */       {
/* 441:453 */         AbstractEpollChannel.this.clearFlag(Native.EPOLLRDHUP);
/* 442:    */       }
/* 443:    */       catch (IOException e)
/* 444:    */       {
/* 445:455 */         AbstractEpollChannel.this.pipeline().fireExceptionCaught(e);
/* 446:456 */         close(voidPromise());
/* 447:    */       }
/* 448:    */     }
/* 449:    */     
/* 450:    */     void shutdownInput(boolean rdHup)
/* 451:    */     {
/* 452:464 */       if (!AbstractEpollChannel.this.socket.isInputShutdown())
/* 453:    */       {
/* 454:465 */         if (AbstractEpollChannel.isAllowHalfClosure(AbstractEpollChannel.this.config()))
/* 455:    */         {
/* 456:    */           try
/* 457:    */           {
/* 458:467 */             AbstractEpollChannel.this.socket.shutdown(true, false);
/* 459:    */           }
/* 460:    */           catch (IOException ignored)
/* 461:    */           {
/* 462:471 */             fireEventAndClose(ChannelInputShutdownEvent.INSTANCE);
/* 463:472 */             return;
/* 464:    */           }
/* 465:    */           catch (NotYetConnectedException localNotYetConnectedException) {}
/* 466:477 */           AbstractEpollChannel.this.clearEpollIn();
/* 467:478 */           AbstractEpollChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/* 468:    */         }
/* 469:    */         else
/* 470:    */         {
/* 471:480 */           close(voidPromise());
/* 472:    */         }
/* 473:    */       }
/* 474:482 */       else if (!rdHup)
/* 475:    */       {
/* 476:483 */         AbstractEpollChannel.this.inputClosedSeenErrorOnRead = true;
/* 477:484 */         AbstractEpollChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
/* 478:    */       }
/* 479:    */     }
/* 480:    */     
/* 481:    */     private void fireEventAndClose(Object evt)
/* 482:    */     {
/* 483:489 */       AbstractEpollChannel.this.pipeline().fireUserEventTriggered(evt);
/* 484:490 */       close(voidPromise());
/* 485:    */     }
/* 486:    */     
/* 487:    */     public EpollRecvByteAllocatorHandle recvBufAllocHandle()
/* 488:    */     {
/* 489:495 */       if (this.allocHandle == null) {
/* 490:496 */         this.allocHandle = newEpollHandle((RecvByteBufAllocator.ExtendedHandle)super.recvBufAllocHandle());
/* 491:    */       }
/* 492:498 */       return this.allocHandle;
/* 493:    */     }
/* 494:    */     
/* 495:    */     EpollRecvByteAllocatorHandle newEpollHandle(RecvByteBufAllocator.ExtendedHandle handle)
/* 496:    */     {
/* 497:506 */       return new EpollRecvByteAllocatorHandle(handle);
/* 498:    */     }
/* 499:    */     
/* 500:    */     protected void flush0()
/* 501:    */     {
/* 502:514 */       if (AbstractEpollChannel.this.isFlagSet(Native.EPOLLOUT)) {
/* 503:515 */         return;
/* 504:    */       }
/* 505:517 */       super.flush0();
/* 506:    */     }
/* 507:    */     
/* 508:    */     final void epollOutReady()
/* 509:    */     {
/* 510:524 */       if (AbstractEpollChannel.this.connectPromise != null) {
/* 511:526 */         finishConnect();
/* 512:527 */       } else if (!AbstractEpollChannel.this.socket.isOutputShutdown()) {
/* 513:529 */         super.flush0();
/* 514:    */       }
/* 515:    */     }
/* 516:    */     
/* 517:    */     protected final void clearEpollIn0()
/* 518:    */     {
/* 519:534 */       assert (AbstractEpollChannel.this.eventLoop().inEventLoop());
/* 520:    */       try
/* 521:    */       {
/* 522:536 */         this.readPending = false;
/* 523:537 */         AbstractEpollChannel.this.clearFlag(AbstractEpollChannel.this.readFlag);
/* 524:    */       }
/* 525:    */       catch (IOException e)
/* 526:    */       {
/* 527:541 */         AbstractEpollChannel.this.pipeline().fireExceptionCaught(e);
/* 528:542 */         AbstractEpollChannel.this.unsafe().close(AbstractEpollChannel.this.unsafe().voidPromise());
/* 529:    */       }
/* 530:    */     }
/* 531:    */     
/* 532:    */     public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 533:    */     {
/* 534:549 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 535:550 */         return;
/* 536:    */       }
/* 537:    */       try
/* 538:    */       {
/* 539:554 */         if (AbstractEpollChannel.this.connectPromise != null) {
/* 540:555 */           throw new ConnectionPendingException();
/* 541:    */         }
/* 542:558 */         boolean wasActive = AbstractEpollChannel.this.isActive();
/* 543:559 */         if (AbstractEpollChannel.this.doConnect(remoteAddress, localAddress))
/* 544:    */         {
/* 545:560 */           fulfillConnectPromise(promise, wasActive);
/* 546:    */         }
/* 547:    */         else
/* 548:    */         {
/* 549:562 */           AbstractEpollChannel.this.connectPromise = promise;
/* 550:563 */           AbstractEpollChannel.this.requestedRemoteAddress = remoteAddress;
/* 551:    */           
/* 552:    */ 
/* 553:566 */           int connectTimeoutMillis = AbstractEpollChannel.this.config().getConnectTimeoutMillis();
/* 554:567 */           if (connectTimeoutMillis > 0) {
/* 555:568 */             AbstractEpollChannel.this.connectTimeoutFuture = AbstractEpollChannel.this.eventLoop().schedule(new Runnable()
/* 556:    */             {
/* 557:    */               public void run()
/* 558:    */               {
/* 559:571 */                 ChannelPromise connectPromise = AbstractEpollChannel.this.connectPromise;
/* 560:572 */                 ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 561:574 */                 if ((connectPromise != null) && (connectPromise.tryFailure(cause))) {
/* 562:575 */                   AbstractEpollChannel.AbstractEpollUnsafe.this.close(AbstractEpollChannel.AbstractEpollUnsafe.this.voidPromise());
/* 563:    */                 }
/* 564:    */               }
/* 565:575 */             }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
/* 566:    */           }
/* 567:581 */           promise.addListener(new ChannelFutureListener()
/* 568:    */           {
/* 569:    */             public void operationComplete(ChannelFuture future)
/* 570:    */               throws Exception
/* 571:    */             {
/* 572:584 */               if (future.isCancelled())
/* 573:    */               {
/* 574:585 */                 if (AbstractEpollChannel.this.connectTimeoutFuture != null) {
/* 575:586 */                   AbstractEpollChannel.this.connectTimeoutFuture.cancel(false);
/* 576:    */                 }
/* 577:588 */                 AbstractEpollChannel.this.connectPromise = null;
/* 578:589 */                 AbstractEpollChannel.AbstractEpollUnsafe.this.close(AbstractEpollChannel.AbstractEpollUnsafe.this.voidPromise());
/* 579:    */               }
/* 580:    */             }
/* 581:    */           });
/* 582:    */         }
/* 583:    */       }
/* 584:    */       catch (Throwable t)
/* 585:    */       {
/* 586:595 */         closeIfClosed();
/* 587:596 */         promise.tryFailure(annotateConnectException(t, remoteAddress));
/* 588:    */       }
/* 589:    */     }
/* 590:    */     
/* 591:    */     private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
/* 592:    */     {
/* 593:601 */       if (promise == null) {
/* 594:603 */         return;
/* 595:    */       }
/* 596:605 */       AbstractEpollChannel.this.active = true;
/* 597:    */       
/* 598:    */ 
/* 599:    */ 
/* 600:609 */       boolean active = AbstractEpollChannel.this.isActive();
/* 601:    */       
/* 602:    */ 
/* 603:612 */       boolean promiseSet = promise.trySuccess();
/* 604:616 */       if ((!wasActive) && (active)) {
/* 605:617 */         AbstractEpollChannel.this.pipeline().fireChannelActive();
/* 606:    */       }
/* 607:621 */       if (!promiseSet) {
/* 608:622 */         close(voidPromise());
/* 609:    */       }
/* 610:    */     }
/* 611:    */     
/* 612:    */     private void fulfillConnectPromise(ChannelPromise promise, Throwable cause)
/* 613:    */     {
/* 614:627 */       if (promise == null) {
/* 615:629 */         return;
/* 616:    */       }
/* 617:633 */       promise.tryFailure(cause);
/* 618:634 */       closeIfClosed();
/* 619:    */     }
/* 620:    */     
/* 621:    */     private void finishConnect()
/* 622:    */     {
/* 623:641 */       assert (AbstractEpollChannel.this.eventLoop().inEventLoop());
/* 624:    */       
/* 625:643 */       boolean connectStillInProgress = false;
/* 626:    */       try
/* 627:    */       {
/* 628:645 */         boolean wasActive = AbstractEpollChannel.this.isActive();
/* 629:646 */         if (!doFinishConnect())
/* 630:    */         {
/* 631:647 */           connectStillInProgress = true;
/* 632:648 */           return;
/* 633:    */         }
/* 634:650 */         fulfillConnectPromise(AbstractEpollChannel.this.connectPromise, wasActive);
/* 635:    */       }
/* 636:    */       catch (Throwable t)
/* 637:    */       {
/* 638:652 */         fulfillConnectPromise(AbstractEpollChannel.this.connectPromise, annotateConnectException(t, AbstractEpollChannel.this.requestedRemoteAddress));
/* 639:    */       }
/* 640:    */       finally
/* 641:    */       {
/* 642:654 */         if (!connectStillInProgress)
/* 643:    */         {
/* 644:657 */           if (AbstractEpollChannel.this.connectTimeoutFuture != null) {
/* 645:658 */             AbstractEpollChannel.this.connectTimeoutFuture.cancel(false);
/* 646:    */           }
/* 647:660 */           AbstractEpollChannel.this.connectPromise = null;
/* 648:    */         }
/* 649:    */       }
/* 650:    */     }
/* 651:    */     
/* 652:    */     private boolean doFinishConnect()
/* 653:    */       throws Exception
/* 654:    */     {
/* 655:669 */       if (AbstractEpollChannel.this.socket.finishConnect())
/* 656:    */       {
/* 657:670 */         AbstractEpollChannel.this.clearFlag(Native.EPOLLOUT);
/* 658:671 */         if ((AbstractEpollChannel.this.requestedRemoteAddress instanceof InetSocketAddress)) {
/* 659:672 */           AbstractEpollChannel.this.remote = UnixChannelUtil.computeRemoteAddr((InetSocketAddress)AbstractEpollChannel.this.requestedRemoteAddress, AbstractEpollChannel.this.socket.remoteAddress());
/* 660:    */         }
/* 661:674 */         AbstractEpollChannel.this.requestedRemoteAddress = null;
/* 662:    */         
/* 663:676 */         return true;
/* 664:    */       }
/* 665:678 */       AbstractEpollChannel.this.setFlag(Native.EPOLLOUT);
/* 666:679 */       return false;
/* 667:    */     }
/* 668:    */   }
/* 669:    */   
/* 670:    */   protected void doBind(SocketAddress local)
/* 671:    */     throws Exception
/* 672:    */   {
/* 673:685 */     if ((local instanceof InetSocketAddress)) {
/* 674:686 */       checkResolvable((InetSocketAddress)local);
/* 675:    */     }
/* 676:688 */     this.socket.bind(local);
/* 677:689 */     this.local = this.socket.localAddress();
/* 678:    */   }
/* 679:    */   
/* 680:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 681:    */     throws Exception
/* 682:    */   {
/* 683:696 */     if ((localAddress instanceof InetSocketAddress)) {
/* 684:697 */       checkResolvable((InetSocketAddress)localAddress);
/* 685:    */     }
/* 686:700 */     InetSocketAddress remoteSocketAddr = (remoteAddress instanceof InetSocketAddress) ? (InetSocketAddress)remoteAddress : null;
/* 687:702 */     if (remoteSocketAddr != null) {
/* 688:703 */       checkResolvable(remoteSocketAddr);
/* 689:    */     }
/* 690:706 */     if (this.remote != null) {
/* 691:710 */       throw new AlreadyConnectedException();
/* 692:    */     }
/* 693:713 */     if (localAddress != null) {
/* 694:714 */       this.socket.bind(localAddress);
/* 695:    */     }
/* 696:717 */     boolean connected = doConnect0(remoteAddress);
/* 697:718 */     if (connected) {
/* 698:720 */       this.remote = (remoteSocketAddr == null ? remoteAddress : UnixChannelUtil.computeRemoteAddr(remoteSocketAddr, this.socket.remoteAddress()));
/* 699:    */     }
/* 700:725 */     this.local = this.socket.localAddress();
/* 701:726 */     return connected;
/* 702:    */   }
/* 703:    */   
/* 704:    */   private boolean doConnect0(SocketAddress remote)
/* 705:    */     throws Exception
/* 706:    */   {
/* 707:730 */     boolean success = false;
/* 708:    */     try
/* 709:    */     {
/* 710:732 */       boolean connected = this.socket.connect(remote);
/* 711:733 */       if (!connected) {
/* 712:734 */         setFlag(Native.EPOLLOUT);
/* 713:    */       }
/* 714:736 */       success = true;
/* 715:737 */       return connected;
/* 716:    */     }
/* 717:    */     finally
/* 718:    */     {
/* 719:739 */       if (!success) {
/* 720:740 */         doClose();
/* 721:    */       }
/* 722:    */     }
/* 723:    */   }
/* 724:    */   
/* 725:    */   protected SocketAddress localAddress0()
/* 726:    */   {
/* 727:747 */     return this.local;
/* 728:    */   }
/* 729:    */   
/* 730:    */   protected SocketAddress remoteAddress0()
/* 731:    */   {
/* 732:752 */     return this.remote;
/* 733:    */   }
/* 734:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.AbstractEpollChannel
 * JD-Core Version:    0.7.0.1
 */