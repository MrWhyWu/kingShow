/*   1:    */ package io.netty.channel.nio;
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
/*  15:    */ import io.netty.channel.ChannelPipeline;
/*  16:    */ import io.netty.channel.ChannelPromise;
/*  17:    */ import io.netty.channel.ConnectTimeoutException;
/*  18:    */ import io.netty.channel.EventLoop;
/*  19:    */ import io.netty.util.ReferenceCountUtil;
/*  20:    */ import io.netty.util.ReferenceCounted;
/*  21:    */ import io.netty.util.internal.ThrowableUtil;
/*  22:    */ import io.netty.util.internal.logging.InternalLogger;
/*  23:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  24:    */ import java.io.IOException;
/*  25:    */ import java.net.SocketAddress;
/*  26:    */ import java.nio.channels.CancelledKeyException;
/*  27:    */ import java.nio.channels.ClosedChannelException;
/*  28:    */ import java.nio.channels.ConnectionPendingException;
/*  29:    */ import java.nio.channels.SelectableChannel;
/*  30:    */ import java.nio.channels.SelectionKey;
/*  31:    */ import java.util.concurrent.ScheduledFuture;
/*  32:    */ import java.util.concurrent.TimeUnit;
/*  33:    */ 
/*  34:    */ public abstract class AbstractNioChannel
/*  35:    */   extends AbstractChannel
/*  36:    */ {
/*  37: 52 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractNioChannel.class);
/*  38: 54 */   private static final ClosedChannelException DO_CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractNioChannel.class, "doClose()");
/*  39:    */   private final SelectableChannel ch;
/*  40:    */   protected final int readInterestOp;
/*  41:    */   volatile SelectionKey selectionKey;
/*  42:    */   boolean readPending;
/*  43: 61 */   private final Runnable clearReadPendingRunnable = new Runnable()
/*  44:    */   {
/*  45:    */     public void run()
/*  46:    */     {
/*  47: 64 */       AbstractNioChannel.this.clearReadPending0();
/*  48:    */     }
/*  49:    */   };
/*  50:    */   private ChannelPromise connectPromise;
/*  51:    */   private ScheduledFuture<?> connectTimeoutFuture;
/*  52:    */   private SocketAddress requestedRemoteAddress;
/*  53:    */   
/*  54:    */   protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp)
/*  55:    */   {
/*  56: 84 */     super(parent);
/*  57: 85 */     this.ch = ch;
/*  58: 86 */     this.readInterestOp = readInterestOp;
/*  59:    */     try
/*  60:    */     {
/*  61: 88 */       ch.configureBlocking(false);
/*  62:    */     }
/*  63:    */     catch (IOException e)
/*  64:    */     {
/*  65:    */       try
/*  66:    */       {
/*  67: 91 */         ch.close();
/*  68:    */       }
/*  69:    */       catch (IOException e2)
/*  70:    */       {
/*  71: 93 */         if (logger.isWarnEnabled()) {
/*  72: 94 */           logger.warn("Failed to close a partially initialized socket.", e2);
/*  73:    */         }
/*  74:    */       }
/*  75: 99 */       throw new ChannelException("Failed to enter non-blocking mode.", e);
/*  76:    */     }
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean isOpen()
/*  80:    */   {
/*  81:105 */     return this.ch.isOpen();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public NioUnsafe unsafe()
/*  85:    */   {
/*  86:110 */     return (NioUnsafe)super.unsafe();
/*  87:    */   }
/*  88:    */   
/*  89:    */   protected SelectableChannel javaChannel()
/*  90:    */   {
/*  91:114 */     return this.ch;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public NioEventLoop eventLoop()
/*  95:    */   {
/*  96:119 */     return (NioEventLoop)super.eventLoop();
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected SelectionKey selectionKey()
/* 100:    */   {
/* 101:126 */     assert (this.selectionKey != null);
/* 102:127 */     return this.selectionKey;
/* 103:    */   }
/* 104:    */   
/* 105:    */   @Deprecated
/* 106:    */   protected boolean isReadPending()
/* 107:    */   {
/* 108:136 */     return this.readPending;
/* 109:    */   }
/* 110:    */   
/* 111:    */   @Deprecated
/* 112:    */   protected void setReadPending(final boolean readPending)
/* 113:    */   {
/* 114:145 */     if (isRegistered())
/* 115:    */     {
/* 116:146 */       EventLoop eventLoop = eventLoop();
/* 117:147 */       if (eventLoop.inEventLoop()) {
/* 118:148 */         setReadPending0(readPending);
/* 119:    */       } else {
/* 120:150 */         eventLoop.execute(new Runnable()
/* 121:    */         {
/* 122:    */           public void run()
/* 123:    */           {
/* 124:153 */             AbstractNioChannel.this.setReadPending0(readPending);
/* 125:    */           }
/* 126:    */         });
/* 127:    */       }
/* 128:    */     }
/* 129:    */     else
/* 130:    */     {
/* 131:161 */       this.readPending = readPending;
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   protected final void clearReadPending()
/* 136:    */   {
/* 137:169 */     if (isRegistered())
/* 138:    */     {
/* 139:170 */       EventLoop eventLoop = eventLoop();
/* 140:171 */       if (eventLoop.inEventLoop()) {
/* 141:172 */         clearReadPending0();
/* 142:    */       } else {
/* 143:174 */         eventLoop.execute(this.clearReadPendingRunnable);
/* 144:    */       }
/* 145:    */     }
/* 146:    */     else
/* 147:    */     {
/* 148:180 */       this.readPending = false;
/* 149:    */     }
/* 150:    */   }
/* 151:    */   
/* 152:    */   private void setReadPending0(boolean readPending)
/* 153:    */   {
/* 154:185 */     this.readPending = readPending;
/* 155:186 */     if (!readPending) {
/* 156:187 */       ((AbstractNioUnsafe)unsafe()).removeReadOp();
/* 157:    */     }
/* 158:    */   }
/* 159:    */   
/* 160:    */   private void clearReadPending0()
/* 161:    */   {
/* 162:192 */     this.readPending = false;
/* 163:193 */     ((AbstractNioUnsafe)unsafe()).removeReadOp();
/* 164:    */   }
/* 165:    */   
/* 166:    */   protected abstract class AbstractNioUnsafe
/* 167:    */     extends AbstractChannel.AbstractUnsafe
/* 168:    */     implements AbstractNioChannel.NioUnsafe
/* 169:    */   {
/* 170:    */     protected AbstractNioUnsafe()
/* 171:    */     {
/* 172:218 */       super();
/* 173:    */     }
/* 174:    */     
/* 175:    */     protected final void removeReadOp()
/* 176:    */     {
/* 177:221 */       SelectionKey key = AbstractNioChannel.this.selectionKey();
/* 178:225 */       if (!key.isValid()) {
/* 179:226 */         return;
/* 180:    */       }
/* 181:228 */       int interestOps = key.interestOps();
/* 182:229 */       if ((interestOps & AbstractNioChannel.this.readInterestOp) != 0) {
/* 183:231 */         key.interestOps(interestOps & (AbstractNioChannel.this.readInterestOp ^ 0xFFFFFFFF));
/* 184:    */       }
/* 185:    */     }
/* 186:    */     
/* 187:    */     public final SelectableChannel ch()
/* 188:    */     {
/* 189:237 */       return AbstractNioChannel.this.javaChannel();
/* 190:    */     }
/* 191:    */     
/* 192:    */     public final void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 193:    */     {
/* 194:243 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 195:244 */         return;
/* 196:    */       }
/* 197:    */       try
/* 198:    */       {
/* 199:248 */         if (AbstractNioChannel.this.connectPromise != null) {
/* 200:250 */           throw new ConnectionPendingException();
/* 201:    */         }
/* 202:253 */         boolean wasActive = AbstractNioChannel.this.isActive();
/* 203:254 */         if (AbstractNioChannel.this.doConnect(remoteAddress, localAddress))
/* 204:    */         {
/* 205:255 */           fulfillConnectPromise(promise, wasActive);
/* 206:    */         }
/* 207:    */         else
/* 208:    */         {
/* 209:257 */           AbstractNioChannel.this.connectPromise = promise;
/* 210:258 */           AbstractNioChannel.this.requestedRemoteAddress = remoteAddress;
/* 211:    */           
/* 212:    */ 
/* 213:261 */           int connectTimeoutMillis = AbstractNioChannel.this.config().getConnectTimeoutMillis();
/* 214:262 */           if (connectTimeoutMillis > 0) {
/* 215:263 */             AbstractNioChannel.this.connectTimeoutFuture = AbstractNioChannel.this.eventLoop().schedule(new Runnable()
/* 216:    */             {
/* 217:    */               public void run()
/* 218:    */               {
/* 219:266 */                 ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
/* 220:267 */                 ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
/* 221:269 */                 if ((connectPromise != null) && (connectPromise.tryFailure(cause))) {
/* 222:270 */                   AbstractNioChannel.AbstractNioUnsafe.this.close(AbstractNioChannel.AbstractNioUnsafe.this.voidPromise());
/* 223:    */                 }
/* 224:    */               }
/* 225:270 */             }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
/* 226:    */           }
/* 227:276 */           promise.addListener(new ChannelFutureListener()
/* 228:    */           {
/* 229:    */             public void operationComplete(ChannelFuture future)
/* 230:    */               throws Exception
/* 231:    */             {
/* 232:279 */               if (future.isCancelled())
/* 233:    */               {
/* 234:280 */                 if (AbstractNioChannel.this.connectTimeoutFuture != null) {
/* 235:281 */                   AbstractNioChannel.this.connectTimeoutFuture.cancel(false);
/* 236:    */                 }
/* 237:283 */                 AbstractNioChannel.this.connectPromise = null;
/* 238:284 */                 AbstractNioChannel.AbstractNioUnsafe.this.close(AbstractNioChannel.AbstractNioUnsafe.this.voidPromise());
/* 239:    */               }
/* 240:    */             }
/* 241:    */           });
/* 242:    */         }
/* 243:    */       }
/* 244:    */       catch (Throwable t)
/* 245:    */       {
/* 246:290 */         promise.tryFailure(annotateConnectException(t, remoteAddress));
/* 247:291 */         closeIfClosed();
/* 248:    */       }
/* 249:    */     }
/* 250:    */     
/* 251:    */     private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive)
/* 252:    */     {
/* 253:296 */       if (promise == null) {
/* 254:298 */         return;
/* 255:    */       }
/* 256:303 */       boolean active = AbstractNioChannel.this.isActive();
/* 257:    */       
/* 258:    */ 
/* 259:306 */       boolean promiseSet = promise.trySuccess();
/* 260:310 */       if ((!wasActive) && (active)) {
/* 261:311 */         AbstractNioChannel.this.pipeline().fireChannelActive();
/* 262:    */       }
/* 263:315 */       if (!promiseSet) {
/* 264:316 */         close(voidPromise());
/* 265:    */       }
/* 266:    */     }
/* 267:    */     
/* 268:    */     private void fulfillConnectPromise(ChannelPromise promise, Throwable cause)
/* 269:    */     {
/* 270:321 */       if (promise == null) {
/* 271:323 */         return;
/* 272:    */       }
/* 273:327 */       promise.tryFailure(cause);
/* 274:328 */       closeIfClosed();
/* 275:    */     }
/* 276:    */     
/* 277:    */     public final void finishConnect()
/* 278:    */     {
/* 279:336 */       assert (AbstractNioChannel.this.eventLoop().inEventLoop());
/* 280:    */       try
/* 281:    */       {
/* 282:339 */         boolean wasActive = AbstractNioChannel.this.isActive();
/* 283:340 */         AbstractNioChannel.this.doFinishConnect();
/* 284:341 */         fulfillConnectPromise(AbstractNioChannel.this.connectPromise, wasActive);
/* 285:    */       }
/* 286:    */       catch (Throwable t)
/* 287:    */       {
/* 288:343 */         fulfillConnectPromise(AbstractNioChannel.this.connectPromise, annotateConnectException(t, AbstractNioChannel.this.requestedRemoteAddress));
/* 289:    */       }
/* 290:    */       finally
/* 291:    */       {
/* 292:347 */         if (AbstractNioChannel.this.connectTimeoutFuture != null) {
/* 293:348 */           AbstractNioChannel.this.connectTimeoutFuture.cancel(false);
/* 294:    */         }
/* 295:350 */         AbstractNioChannel.this.connectPromise = null;
/* 296:    */       }
/* 297:    */     }
/* 298:    */     
/* 299:    */     protected final void flush0()
/* 300:    */     {
/* 301:359 */       if (isFlushPending()) {
/* 302:360 */         return;
/* 303:    */       }
/* 304:362 */       super.flush0();
/* 305:    */     }
/* 306:    */     
/* 307:    */     public final void forceFlush()
/* 308:    */     {
/* 309:368 */       super.flush0();
/* 310:    */     }
/* 311:    */     
/* 312:    */     private boolean isFlushPending()
/* 313:    */     {
/* 314:372 */       SelectionKey selectionKey = AbstractNioChannel.this.selectionKey();
/* 315:373 */       return (selectionKey.isValid()) && ((selectionKey.interestOps() & 0x4) != 0);
/* 316:    */     }
/* 317:    */   }
/* 318:    */   
/* 319:    */   protected boolean isCompatible(EventLoop loop)
/* 320:    */   {
/* 321:379 */     return loop instanceof NioEventLoop;
/* 322:    */   }
/* 323:    */   
/* 324:    */   protected void doRegister()
/* 325:    */     throws Exception
/* 326:    */   {
/* 327:384 */     boolean selected = false;
/* 328:    */     for (;;)
/* 329:    */     {
/* 330:    */       try
/* 331:    */       {
/* 332:387 */         this.selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
/* 333:388 */         return;
/* 334:    */       }
/* 335:    */       catch (CancelledKeyException e)
/* 336:    */       {
/* 337:390 */         if (!selected)
/* 338:    */         {
/* 339:393 */           eventLoop().selectNow();
/* 340:394 */           selected = true;
/* 341:    */         }
/* 342:    */         else
/* 343:    */         {
/* 344:398 */           throw e;
/* 345:    */         }
/* 346:    */       }
/* 347:    */     }
/* 348:    */   }
/* 349:    */   
/* 350:    */   protected void doDeregister()
/* 351:    */     throws Exception
/* 352:    */   {
/* 353:406 */     eventLoop().cancel(selectionKey());
/* 354:    */   }
/* 355:    */   
/* 356:    */   protected void doBeginRead()
/* 357:    */     throws Exception
/* 358:    */   {
/* 359:412 */     SelectionKey selectionKey = this.selectionKey;
/* 360:413 */     if (!selectionKey.isValid()) {
/* 361:414 */       return;
/* 362:    */     }
/* 363:417 */     this.readPending = true;
/* 364:    */     
/* 365:419 */     int interestOps = selectionKey.interestOps();
/* 366:420 */     if ((interestOps & this.readInterestOp) == 0) {
/* 367:421 */       selectionKey.interestOps(interestOps | this.readInterestOp);
/* 368:    */     }
/* 369:    */   }
/* 370:    */   
/* 371:    */   protected abstract boolean doConnect(SocketAddress paramSocketAddress1, SocketAddress paramSocketAddress2)
/* 372:    */     throws Exception;
/* 373:    */   
/* 374:    */   protected abstract void doFinishConnect()
/* 375:    */     throws Exception;
/* 376:    */   
/* 377:    */   protected final ByteBuf newDirectBuffer(ByteBuf buf)
/* 378:    */   {
/* 379:441 */     int readableBytes = buf.readableBytes();
/* 380:442 */     if (readableBytes == 0)
/* 381:    */     {
/* 382:443 */       ReferenceCountUtil.safeRelease(buf);
/* 383:444 */       return Unpooled.EMPTY_BUFFER;
/* 384:    */     }
/* 385:447 */     ByteBufAllocator alloc = alloc();
/* 386:448 */     if (alloc.isDirectBufferPooled())
/* 387:    */     {
/* 388:449 */       ByteBuf directBuf = alloc.directBuffer(readableBytes);
/* 389:450 */       directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 390:451 */       ReferenceCountUtil.safeRelease(buf);
/* 391:452 */       return directBuf;
/* 392:    */     }
/* 393:455 */     ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
/* 394:456 */     if (directBuf != null)
/* 395:    */     {
/* 396:457 */       directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 397:458 */       ReferenceCountUtil.safeRelease(buf);
/* 398:459 */       return directBuf;
/* 399:    */     }
/* 400:463 */     return buf;
/* 401:    */   }
/* 402:    */   
/* 403:    */   protected final ByteBuf newDirectBuffer(ReferenceCounted holder, ByteBuf buf)
/* 404:    */   {
/* 405:473 */     int readableBytes = buf.readableBytes();
/* 406:474 */     if (readableBytes == 0)
/* 407:    */     {
/* 408:475 */       ReferenceCountUtil.safeRelease(holder);
/* 409:476 */       return Unpooled.EMPTY_BUFFER;
/* 410:    */     }
/* 411:479 */     ByteBufAllocator alloc = alloc();
/* 412:480 */     if (alloc.isDirectBufferPooled())
/* 413:    */     {
/* 414:481 */       ByteBuf directBuf = alloc.directBuffer(readableBytes);
/* 415:482 */       directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 416:483 */       ReferenceCountUtil.safeRelease(holder);
/* 417:484 */       return directBuf;
/* 418:    */     }
/* 419:487 */     ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
/* 420:488 */     if (directBuf != null)
/* 421:    */     {
/* 422:489 */       directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
/* 423:490 */       ReferenceCountUtil.safeRelease(holder);
/* 424:491 */       return directBuf;
/* 425:    */     }
/* 426:495 */     if (holder != buf)
/* 427:    */     {
/* 428:497 */       buf.retain();
/* 429:498 */       ReferenceCountUtil.safeRelease(holder);
/* 430:    */     }
/* 431:501 */     return buf;
/* 432:    */   }
/* 433:    */   
/* 434:    */   protected void doClose()
/* 435:    */     throws Exception
/* 436:    */   {
/* 437:506 */     ChannelPromise promise = this.connectPromise;
/* 438:507 */     if (promise != null)
/* 439:    */     {
/* 440:509 */       promise.tryFailure(DO_CLOSE_CLOSED_CHANNEL_EXCEPTION);
/* 441:510 */       this.connectPromise = null;
/* 442:    */     }
/* 443:513 */     ScheduledFuture<?> future = this.connectTimeoutFuture;
/* 444:514 */     if (future != null)
/* 445:    */     {
/* 446:515 */       future.cancel(false);
/* 447:516 */       this.connectTimeoutFuture = null;
/* 448:    */     }
/* 449:    */   }
/* 450:    */   
/* 451:    */   public static abstract interface NioUnsafe
/* 452:    */     extends Channel.Unsafe
/* 453:    */   {
/* 454:    */     public abstract SelectableChannel ch();
/* 455:    */     
/* 456:    */     public abstract void finishConnect();
/* 457:    */     
/* 458:    */     public abstract void read();
/* 459:    */     
/* 460:    */     public abstract void forceFlush();
/* 461:    */   }
/* 462:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.AbstractNioChannel
 * JD-Core Version:    0.7.0.1
 */