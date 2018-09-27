/*   1:    */ package io.netty.channel.local;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelMetadata;
/*   9:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  10:    */ import io.netty.channel.ChannelPipeline;
/*  11:    */ import io.netty.channel.ChannelPromise;
/*  12:    */ import io.netty.channel.DefaultChannelConfig;
/*  13:    */ import io.netty.channel.EventLoop;
/*  14:    */ import io.netty.channel.PreferHeapByteBufAllocator;
/*  15:    */ import io.netty.channel.SingleThreadEventLoop;
/*  16:    */ import io.netty.util.ReferenceCountUtil;
/*  17:    */ import io.netty.util.concurrent.Future;
/*  18:    */ import io.netty.util.concurrent.SingleThreadEventExecutor;
/*  19:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*  20:    */ import io.netty.util.internal.PlatformDependent;
/*  21:    */ import io.netty.util.internal.ThrowableUtil;
/*  22:    */ import io.netty.util.internal.logging.InternalLogger;
/*  23:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  24:    */ import java.net.ConnectException;
/*  25:    */ import java.net.SocketAddress;
/*  26:    */ import java.nio.channels.AlreadyConnectedException;
/*  27:    */ import java.nio.channels.ClosedChannelException;
/*  28:    */ import java.nio.channels.ConnectionPendingException;
/*  29:    */ import java.nio.channels.NotYetConnectedException;
/*  30:    */ import java.util.Queue;
/*  31:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  32:    */ 
/*  33:    */ public class LocalChannel
/*  34:    */   extends AbstractChannel
/*  35:    */ {
/*  36: 51 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(LocalChannel.class);
/*  37: 54 */   private static final AtomicReferenceFieldUpdater<LocalChannel, Future> FINISH_READ_FUTURE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(LocalChannel.class, Future.class, "finishReadFuture");
/*  38: 55 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  39:    */   private static final int MAX_READER_STACK_DEPTH = 8;
/*  40: 57 */   private static final ClosedChannelException DO_WRITE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), LocalChannel.class, "doWrite(...)");
/*  41: 59 */   private static final ClosedChannelException DO_CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), LocalChannel.class, "doClose()");
/*  42:    */   
/*  43:    */   private static enum State
/*  44:    */   {
/*  45: 62 */     OPEN,  BOUND,  CONNECTED,  CLOSED;
/*  46:    */     
/*  47:    */     private State() {}
/*  48:    */   }
/*  49:    */   
/*  50: 64 */   private final ChannelConfig config = new DefaultChannelConfig(this);
/*  51: 66 */   final Queue<Object> inboundBuffer = PlatformDependent.newSpscQueue();
/*  52: 67 */   private final Runnable readTask = new Runnable()
/*  53:    */   {
/*  54:    */     public void run()
/*  55:    */     {
/*  56: 70 */       ChannelPipeline pipeline = LocalChannel.this.pipeline();
/*  57:    */       for (;;)
/*  58:    */       {
/*  59: 72 */         Object m = LocalChannel.this.inboundBuffer.poll();
/*  60: 73 */         if (m == null) {
/*  61:    */           break;
/*  62:    */         }
/*  63: 76 */         pipeline.fireChannelRead(m);
/*  64:    */       }
/*  65: 78 */       pipeline.fireChannelReadComplete();
/*  66:    */     }
/*  67:    */   };
/*  68: 81 */   private final Runnable shutdownHook = new Runnable()
/*  69:    */   {
/*  70:    */     public void run()
/*  71:    */     {
/*  72: 84 */       LocalChannel.this.unsafe().close(LocalChannel.this.unsafe().voidPromise());
/*  73:    */     }
/*  74:    */   };
/*  75:    */   private volatile State state;
/*  76:    */   private volatile LocalChannel peer;
/*  77:    */   private volatile LocalAddress localAddress;
/*  78:    */   private volatile LocalAddress remoteAddress;
/*  79:    */   private volatile ChannelPromise connectPromise;
/*  80:    */   private volatile boolean readInProgress;
/*  81:    */   private volatile boolean writeInProgress;
/*  82:    */   private volatile Future<?> finishReadFuture;
/*  83:    */   
/*  84:    */   public LocalChannel()
/*  85:    */   {
/*  86: 98 */     super(null);
/*  87: 99 */     config().setAllocator(new PreferHeapByteBufAllocator(this.config.getAllocator()));
/*  88:    */   }
/*  89:    */   
/*  90:    */   protected LocalChannel(LocalServerChannel parent, LocalChannel peer)
/*  91:    */   {
/*  92:103 */     super(parent);
/*  93:104 */     config().setAllocator(new PreferHeapByteBufAllocator(this.config.getAllocator()));
/*  94:105 */     this.peer = peer;
/*  95:106 */     this.localAddress = parent.localAddress();
/*  96:107 */     this.remoteAddress = peer.localAddress();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public ChannelMetadata metadata()
/* 100:    */   {
/* 101:112 */     return METADATA;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ChannelConfig config()
/* 105:    */   {
/* 106:117 */     return this.config;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public LocalServerChannel parent()
/* 110:    */   {
/* 111:122 */     return (LocalServerChannel)super.parent();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public LocalAddress localAddress()
/* 115:    */   {
/* 116:127 */     return (LocalAddress)super.localAddress();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public LocalAddress remoteAddress()
/* 120:    */   {
/* 121:132 */     return (LocalAddress)super.remoteAddress();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public boolean isOpen()
/* 125:    */   {
/* 126:137 */     return this.state != State.CLOSED;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public boolean isActive()
/* 130:    */   {
/* 131:142 */     return this.state == State.CONNECTED;
/* 132:    */   }
/* 133:    */   
/* 134:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/* 135:    */   {
/* 136:147 */     return new LocalUnsafe(null);
/* 137:    */   }
/* 138:    */   
/* 139:    */   protected boolean isCompatible(EventLoop loop)
/* 140:    */   {
/* 141:152 */     return loop instanceof SingleThreadEventLoop;
/* 142:    */   }
/* 143:    */   
/* 144:    */   protected SocketAddress localAddress0()
/* 145:    */   {
/* 146:157 */     return this.localAddress;
/* 147:    */   }
/* 148:    */   
/* 149:    */   protected SocketAddress remoteAddress0()
/* 150:    */   {
/* 151:162 */     return this.remoteAddress;
/* 152:    */   }
/* 153:    */   
/* 154:    */   protected void doRegister()
/* 155:    */     throws Exception
/* 156:    */   {
/* 157:172 */     if ((this.peer != null) && (parent() != null))
/* 158:    */     {
/* 159:175 */       final LocalChannel peer = this.peer;
/* 160:176 */       this.state = State.CONNECTED;
/* 161:    */       
/* 162:178 */       peer.remoteAddress = (parent() == null ? null : parent().localAddress());
/* 163:179 */       peer.state = State.CONNECTED;
/* 164:    */       
/* 165:    */ 
/* 166:    */ 
/* 167:    */ 
/* 168:    */ 
/* 169:185 */       peer.eventLoop().execute(new Runnable()
/* 170:    */       {
/* 171:    */         public void run()
/* 172:    */         {
/* 173:188 */           ChannelPromise promise = peer.connectPromise;
/* 174:192 */           if ((promise != null) && (promise.trySuccess())) {
/* 175:193 */             peer.pipeline().fireChannelActive();
/* 176:    */           }
/* 177:    */         }
/* 178:    */       });
/* 179:    */     }
/* 180:198 */     ((SingleThreadEventExecutor)eventLoop()).addShutdownHook(this.shutdownHook);
/* 181:    */   }
/* 182:    */   
/* 183:    */   protected void doBind(SocketAddress localAddress)
/* 184:    */     throws Exception
/* 185:    */   {
/* 186:204 */     this.localAddress = LocalChannelRegistry.register(this, this.localAddress, localAddress);
/* 187:    */     
/* 188:206 */     this.state = State.BOUND;
/* 189:    */   }
/* 190:    */   
/* 191:    */   protected void doDisconnect()
/* 192:    */     throws Exception
/* 193:    */   {
/* 194:211 */     doClose();
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected void doClose()
/* 198:    */     throws Exception
/* 199:    */   {
/* 200:216 */     final LocalChannel peer = this.peer;
/* 201:217 */     State oldState = this.state;
/* 202:    */     try
/* 203:    */     {
/* 204:219 */       if (oldState != State.CLOSED)
/* 205:    */       {
/* 206:221 */         if (this.localAddress != null)
/* 207:    */         {
/* 208:222 */           if (parent() == null) {
/* 209:223 */             LocalChannelRegistry.unregister(this.localAddress);
/* 210:    */           }
/* 211:225 */           this.localAddress = null;
/* 212:    */         }
/* 213:230 */         this.state = State.CLOSED;
/* 214:233 */         if ((this.writeInProgress) && (peer != null)) {
/* 215:234 */           finishPeerRead(peer);
/* 216:    */         }
/* 217:237 */         ChannelPromise promise = this.connectPromise;
/* 218:238 */         if (promise != null)
/* 219:    */         {
/* 220:240 */           promise.tryFailure(DO_CLOSE_CLOSED_CHANNEL_EXCEPTION);
/* 221:241 */           this.connectPromise = null;
/* 222:    */         }
/* 223:    */       }
/* 224:245 */       if (peer != null)
/* 225:    */       {
/* 226:246 */         this.peer = null;
/* 227:    */         
/* 228:    */ 
/* 229:    */ 
/* 230:250 */         EventLoop peerEventLoop = peer.eventLoop();
/* 231:251 */         final boolean peerIsActive = peer.isActive();
/* 232:    */         try
/* 233:    */         {
/* 234:253 */           peerEventLoop.execute(new Runnable()
/* 235:    */           {
/* 236:    */             public void run()
/* 237:    */             {
/* 238:256 */               peer.tryClose(peerIsActive);
/* 239:    */             }
/* 240:    */           });
/* 241:    */         }
/* 242:    */         catch (Throwable cause)
/* 243:    */         {
/* 244:260 */           logger.warn("Releasing Inbound Queues for channels {}-{} because exception occurred!", new Object[] { this, peer, cause });
/* 245:262 */           if (peerEventLoop.inEventLoop()) {
/* 246:263 */             peer.releaseInboundBuffers();
/* 247:    */           } else {
/* 248:267 */             peer.close();
/* 249:    */           }
/* 250:269 */           PlatformDependent.throwException(cause);
/* 251:    */         }
/* 252:    */       }
/* 253:274 */       if ((oldState != null) && (oldState != State.CLOSED)) {
/* 254:279 */         releaseInboundBuffers();
/* 255:    */       }
/* 256:    */     }
/* 257:    */     finally
/* 258:    */     {
/* 259:274 */       if ((oldState != null) && (oldState != State.CLOSED)) {
/* 260:279 */         releaseInboundBuffers();
/* 261:    */       }
/* 262:    */     }
/* 263:    */   }
/* 264:    */   
/* 265:    */   private void tryClose(boolean isActive)
/* 266:    */   {
/* 267:285 */     if (isActive) {
/* 268:286 */       unsafe().close(unsafe().voidPromise());
/* 269:    */     } else {
/* 270:288 */       releaseInboundBuffers();
/* 271:    */     }
/* 272:    */   }
/* 273:    */   
/* 274:    */   protected void doDeregister()
/* 275:    */     throws Exception
/* 276:    */   {
/* 277:295 */     ((SingleThreadEventExecutor)eventLoop()).removeShutdownHook(this.shutdownHook);
/* 278:    */   }
/* 279:    */   
/* 280:    */   protected void doBeginRead()
/* 281:    */     throws Exception
/* 282:    */   {
/* 283:300 */     if (this.readInProgress) {
/* 284:301 */       return;
/* 285:    */     }
/* 286:304 */     ChannelPipeline pipeline = pipeline();
/* 287:305 */     Queue<Object> inboundBuffer = this.inboundBuffer;
/* 288:306 */     if (inboundBuffer.isEmpty())
/* 289:    */     {
/* 290:307 */       this.readInProgress = true;
/* 291:308 */       return;
/* 292:    */     }
/* 293:311 */     InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 294:312 */     Integer stackDepth = Integer.valueOf(threadLocals.localChannelReaderStackDepth());
/* 295:313 */     if (stackDepth.intValue() < 8)
/* 296:    */     {
/* 297:314 */       threadLocals.setLocalChannelReaderStackDepth(stackDepth.intValue() + 1);
/* 298:    */       try
/* 299:    */       {
/* 300:    */         for (;;)
/* 301:    */         {
/* 302:317 */           Object received = inboundBuffer.poll();
/* 303:318 */           if (received == null) {
/* 304:    */             break;
/* 305:    */           }
/* 306:321 */           pipeline.fireChannelRead(received);
/* 307:    */         }
/* 308:323 */         pipeline.fireChannelReadComplete();
/* 309:    */       }
/* 310:    */       finally
/* 311:    */       {
/* 312:325 */         threadLocals.setLocalChannelReaderStackDepth(stackDepth.intValue());
/* 313:    */       }
/* 314:    */     }
/* 315:    */     else
/* 316:    */     {
/* 317:    */       try
/* 318:    */       {
/* 319:329 */         eventLoop().execute(this.readTask);
/* 320:    */       }
/* 321:    */       catch (Throwable cause)
/* 322:    */       {
/* 323:331 */         logger.warn("Closing Local channels {}-{} because exception occurred!", new Object[] { this, this.peer, cause });
/* 324:332 */         close();
/* 325:333 */         this.peer.close();
/* 326:334 */         PlatformDependent.throwException(cause);
/* 327:    */       }
/* 328:    */     }
/* 329:    */   }
/* 330:    */   
/* 331:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 332:    */     throws Exception
/* 333:    */   {
/* 334:341 */     switch (6.$SwitchMap$io$netty$channel$local$LocalChannel$State[this.state.ordinal()])
/* 335:    */     {
/* 336:    */     case 1: 
/* 337:    */     case 2: 
/* 338:344 */       throw new NotYetConnectedException();
/* 339:    */     case 3: 
/* 340:346 */       throw DO_WRITE_CLOSED_CHANNEL_EXCEPTION;
/* 341:    */     }
/* 342:351 */     LocalChannel peer = this.peer;
/* 343:    */     
/* 344:353 */     this.writeInProgress = true;
/* 345:    */     try
/* 346:    */     {
/* 347:    */       for (;;)
/* 348:    */       {
/* 349:356 */         Object msg = in.current();
/* 350:357 */         if (msg == null) {
/* 351:    */           break;
/* 352:    */         }
/* 353:    */         try
/* 354:    */         {
/* 355:363 */           if (peer.state == State.CONNECTED)
/* 356:    */           {
/* 357:364 */             peer.inboundBuffer.add(ReferenceCountUtil.retain(msg));
/* 358:365 */             in.remove();
/* 359:    */           }
/* 360:    */           else
/* 361:    */           {
/* 362:367 */             in.remove(DO_WRITE_CLOSED_CHANNEL_EXCEPTION);
/* 363:    */           }
/* 364:    */         }
/* 365:    */         catch (Throwable cause)
/* 366:    */         {
/* 367:370 */           in.remove(cause);
/* 368:    */         }
/* 369:    */       }
/* 370:    */     }
/* 371:    */     finally
/* 372:    */     {
/* 373:379 */       this.writeInProgress = false;
/* 374:    */     }
/* 375:382 */     finishPeerRead(peer);
/* 376:    */   }
/* 377:    */   
/* 378:    */   private void finishPeerRead(LocalChannel peer)
/* 379:    */   {
/* 380:387 */     if ((peer.eventLoop() == eventLoop()) && (!peer.writeInProgress)) {
/* 381:388 */       finishPeerRead0(peer);
/* 382:    */     } else {
/* 383:390 */       runFinishPeerReadTask(peer);
/* 384:    */     }
/* 385:    */   }
/* 386:    */   
/* 387:    */   private void runFinishPeerReadTask(final LocalChannel peer)
/* 388:    */   {
/* 389:397 */     Runnable finishPeerReadTask = new Runnable()
/* 390:    */     {
/* 391:    */       public void run()
/* 392:    */       {
/* 393:400 */         LocalChannel.this.finishPeerRead0(peer);
/* 394:    */       }
/* 395:    */     };
/* 396:    */     try
/* 397:    */     {
/* 398:404 */       if (peer.writeInProgress) {
/* 399:405 */         peer.finishReadFuture = peer.eventLoop().submit(finishPeerReadTask);
/* 400:    */       } else {
/* 401:407 */         peer.eventLoop().execute(finishPeerReadTask);
/* 402:    */       }
/* 403:    */     }
/* 404:    */     catch (Throwable cause)
/* 405:    */     {
/* 406:410 */       logger.warn("Closing Local channels {}-{} because exception occurred!", new Object[] { this, peer, cause });
/* 407:411 */       close();
/* 408:412 */       peer.close();
/* 409:413 */       PlatformDependent.throwException(cause);
/* 410:    */     }
/* 411:    */   }
/* 412:    */   
/* 413:    */   private void releaseInboundBuffers()
/* 414:    */   {
/* 415:418 */     assert ((eventLoop() == null) || (eventLoop().inEventLoop()));
/* 416:419 */     this.readInProgress = false;
/* 417:420 */     Queue<Object> inboundBuffer = this.inboundBuffer;
/* 418:    */     Object msg;
/* 419:422 */     while ((msg = inboundBuffer.poll()) != null) {
/* 420:423 */       ReferenceCountUtil.release(msg);
/* 421:    */     }
/* 422:    */   }
/* 423:    */   
/* 424:    */   private void finishPeerRead0(LocalChannel peer)
/* 425:    */   {
/* 426:428 */     Future<?> peerFinishReadFuture = peer.finishReadFuture;
/* 427:429 */     if (peerFinishReadFuture != null)
/* 428:    */     {
/* 429:430 */       if (!peerFinishReadFuture.isDone())
/* 430:    */       {
/* 431:431 */         runFinishPeerReadTask(peer);
/* 432:432 */         return;
/* 433:    */       }
/* 434:435 */       FINISH_READ_FUTURE_UPDATER.compareAndSet(peer, peerFinishReadFuture, null);
/* 435:    */     }
/* 436:438 */     ChannelPipeline peerPipeline = peer.pipeline();
/* 437:439 */     if (peer.readInProgress)
/* 438:    */     {
/* 439:440 */       peer.readInProgress = false;
/* 440:    */       for (;;)
/* 441:    */       {
/* 442:442 */         Object received = peer.inboundBuffer.poll();
/* 443:443 */         if (received == null) {
/* 444:    */           break;
/* 445:    */         }
/* 446:446 */         peerPipeline.fireChannelRead(received);
/* 447:    */       }
/* 448:448 */       peerPipeline.fireChannelReadComplete();
/* 449:    */     }
/* 450:    */   }
/* 451:    */   
/* 452:    */   private class LocalUnsafe
/* 453:    */     extends AbstractChannel.AbstractUnsafe
/* 454:    */   {
/* 455:    */     private LocalUnsafe()
/* 456:    */     {
/* 457:452 */       super();
/* 458:    */     }
/* 459:    */     
/* 460:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 461:    */     {
/* 462:457 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 463:458 */         return;
/* 464:    */       }
/* 465:461 */       if (LocalChannel.this.state == LocalChannel.State.CONNECTED)
/* 466:    */       {
/* 467:462 */         Exception cause = new AlreadyConnectedException();
/* 468:463 */         safeSetFailure(promise, cause);
/* 469:464 */         LocalChannel.this.pipeline().fireExceptionCaught(cause);
/* 470:465 */         return;
/* 471:    */       }
/* 472:468 */       if (LocalChannel.this.connectPromise != null) {
/* 473:469 */         throw new ConnectionPendingException();
/* 474:    */       }
/* 475:472 */       LocalChannel.this.connectPromise = promise;
/* 476:474 */       if (LocalChannel.this.state != LocalChannel.State.BOUND) {
/* 477:476 */         if (localAddress == null) {
/* 478:477 */           localAddress = new LocalAddress(LocalChannel.this);
/* 479:    */         }
/* 480:    */       }
/* 481:481 */       if (localAddress != null) {
/* 482:    */         try
/* 483:    */         {
/* 484:483 */           LocalChannel.this.doBind(localAddress);
/* 485:    */         }
/* 486:    */         catch (Throwable t)
/* 487:    */         {
/* 488:485 */           safeSetFailure(promise, t);
/* 489:486 */           close(voidPromise());
/* 490:487 */           return;
/* 491:    */         }
/* 492:    */       }
/* 493:491 */       Channel boundChannel = LocalChannelRegistry.get(remoteAddress);
/* 494:492 */       if (!(boundChannel instanceof LocalServerChannel))
/* 495:    */       {
/* 496:493 */         Exception cause = new ConnectException("connection refused: " + remoteAddress);
/* 497:494 */         safeSetFailure(promise, cause);
/* 498:495 */         close(voidPromise());
/* 499:496 */         return;
/* 500:    */       }
/* 501:499 */       LocalServerChannel serverChannel = (LocalServerChannel)boundChannel;
/* 502:500 */       LocalChannel.this.peer = serverChannel.serve(LocalChannel.this);
/* 503:    */     }
/* 504:    */   }
/* 505:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.local.LocalChannel
 * JD-Core Version:    0.7.0.1
 */