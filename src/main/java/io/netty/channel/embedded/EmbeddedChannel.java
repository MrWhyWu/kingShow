/*   1:    */ package io.netty.channel.embedded;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelFutureListener;
/*  10:    */ import io.netty.channel.ChannelHandler;
/*  11:    */ import io.netty.channel.ChannelId;
/*  12:    */ import io.netty.channel.ChannelInitializer;
/*  13:    */ import io.netty.channel.ChannelMetadata;
/*  14:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  15:    */ import io.netty.channel.ChannelPipeline;
/*  16:    */ import io.netty.channel.ChannelPromise;
/*  17:    */ import io.netty.channel.DefaultChannelConfig;
/*  18:    */ import io.netty.channel.DefaultChannelPipeline;
/*  19:    */ import io.netty.channel.EventLoop;
/*  20:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  21:    */ import io.netty.util.ReferenceCountUtil;
/*  22:    */ import io.netty.util.internal.ObjectUtil;
/*  23:    */ import io.netty.util.internal.PlatformDependent;
/*  24:    */ import io.netty.util.internal.RecyclableArrayList;
/*  25:    */ import io.netty.util.internal.logging.InternalLogger;
/*  26:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  27:    */ import java.net.SocketAddress;
/*  28:    */ import java.nio.channels.ClosedChannelException;
/*  29:    */ import java.util.ArrayDeque;
/*  30:    */ import java.util.Queue;
/*  31:    */ 
/*  32:    */ public class EmbeddedChannel
/*  33:    */   extends AbstractChannel
/*  34:    */ {
/*  35: 52 */   private static final SocketAddress LOCAL_ADDRESS = new EmbeddedSocketAddress();
/*  36: 53 */   private static final SocketAddress REMOTE_ADDRESS = new EmbeddedSocketAddress();
/*  37: 55 */   private static final ChannelHandler[] EMPTY_HANDLERS = new ChannelHandler[0];
/*  38:    */   
/*  39:    */   private static enum State
/*  40:    */   {
/*  41: 56 */     OPEN,  ACTIVE,  CLOSED;
/*  42:    */     
/*  43:    */     private State() {}
/*  44:    */   }
/*  45:    */   
/*  46: 58 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(EmbeddedChannel.class);
/*  47: 60 */   private static final ChannelMetadata METADATA_NO_DISCONNECT = new ChannelMetadata(false);
/*  48: 61 */   private static final ChannelMetadata METADATA_DISCONNECT = new ChannelMetadata(true);
/*  49: 63 */   private final EmbeddedEventLoop loop = new EmbeddedEventLoop();
/*  50: 64 */   private final ChannelFutureListener recordExceptionListener = new ChannelFutureListener()
/*  51:    */   {
/*  52:    */     public void operationComplete(ChannelFuture future)
/*  53:    */       throws Exception
/*  54:    */     {
/*  55: 67 */       EmbeddedChannel.this.recordException(future);
/*  56:    */     }
/*  57:    */   };
/*  58:    */   private final ChannelMetadata metadata;
/*  59:    */   private final ChannelConfig config;
/*  60:    */   private Queue<Object> inboundMessages;
/*  61:    */   private Queue<Object> outboundMessages;
/*  62:    */   private Throwable lastException;
/*  63:    */   private State state;
/*  64:    */   
/*  65:    */   public EmbeddedChannel()
/*  66:    */   {
/*  67: 83 */     this(EMPTY_HANDLERS);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public EmbeddedChannel(ChannelId channelId)
/*  71:    */   {
/*  72: 92 */     this(channelId, EMPTY_HANDLERS);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public EmbeddedChannel(ChannelHandler... handlers)
/*  76:    */   {
/*  77:101 */     this(EmbeddedChannelId.INSTANCE, handlers);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public EmbeddedChannel(boolean hasDisconnect, ChannelHandler... handlers)
/*  81:    */   {
/*  82:112 */     this(EmbeddedChannelId.INSTANCE, hasDisconnect, handlers);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public EmbeddedChannel(boolean register, boolean hasDisconnect, ChannelHandler... handlers)
/*  86:    */   {
/*  87:125 */     this(EmbeddedChannelId.INSTANCE, register, hasDisconnect, handlers);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public EmbeddedChannel(ChannelId channelId, ChannelHandler... handlers)
/*  91:    */   {
/*  92:136 */     this(channelId, false, handlers);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public EmbeddedChannel(ChannelId channelId, boolean hasDisconnect, ChannelHandler... handlers)
/*  96:    */   {
/*  97:149 */     this(channelId, true, hasDisconnect, handlers);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public EmbeddedChannel(ChannelId channelId, boolean register, boolean hasDisconnect, ChannelHandler... handlers)
/* 101:    */   {
/* 102:165 */     super(null, channelId);
/* 103:166 */     this.metadata = metadata(hasDisconnect);
/* 104:167 */     this.config = new DefaultChannelConfig(this);
/* 105:168 */     setup(register, handlers);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public EmbeddedChannel(ChannelId channelId, boolean hasDisconnect, ChannelConfig config, ChannelHandler... handlers)
/* 109:    */   {
/* 110:183 */     super(null, channelId);
/* 111:184 */     this.metadata = metadata(hasDisconnect);
/* 112:185 */     this.config = ((ChannelConfig)ObjectUtil.checkNotNull(config, "config"));
/* 113:186 */     setup(true, handlers);
/* 114:    */   }
/* 115:    */   
/* 116:    */   private static ChannelMetadata metadata(boolean hasDisconnect)
/* 117:    */   {
/* 118:190 */     return hasDisconnect ? METADATA_DISCONNECT : METADATA_NO_DISCONNECT;
/* 119:    */   }
/* 120:    */   
/* 121:    */   private void setup(boolean register, final ChannelHandler... handlers)
/* 122:    */   {
/* 123:194 */     ObjectUtil.checkNotNull(handlers, "handlers");
/* 124:195 */     ChannelPipeline p = pipeline();
/* 125:196 */     p.addLast(new ChannelHandler[] { new ChannelInitializer()
/* 126:    */     {
/* 127:    */       protected void initChannel(Channel ch)
/* 128:    */         throws Exception
/* 129:    */       {
/* 130:199 */         ChannelPipeline pipeline = ch.pipeline();
/* 131:200 */         for (ChannelHandler h : handlers)
/* 132:    */         {
/* 133:201 */           if (h == null) {
/* 134:    */             break;
/* 135:    */           }
/* 136:204 */           pipeline.addLast(new ChannelHandler[] { h });
/* 137:    */         }
/* 138:    */       }
/* 139:    */     } });
/* 140:208 */     if (register)
/* 141:    */     {
/* 142:209 */       ChannelFuture future = this.loop.register(this);
/* 143:210 */       assert (future.isDone());
/* 144:    */     }
/* 145:    */   }
/* 146:    */   
/* 147:    */   public void register()
/* 148:    */     throws Exception
/* 149:    */   {
/* 150:218 */     ChannelFuture future = this.loop.register(this);
/* 151:219 */     assert (future.isDone());
/* 152:220 */     Throwable cause = future.cause();
/* 153:221 */     if (cause != null) {
/* 154:222 */       PlatformDependent.throwException(cause);
/* 155:    */     }
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected final DefaultChannelPipeline newChannelPipeline()
/* 159:    */   {
/* 160:228 */     return new EmbeddedChannelPipeline(this);
/* 161:    */   }
/* 162:    */   
/* 163:    */   public ChannelMetadata metadata()
/* 164:    */   {
/* 165:233 */     return this.metadata;
/* 166:    */   }
/* 167:    */   
/* 168:    */   public ChannelConfig config()
/* 169:    */   {
/* 170:238 */     return this.config;
/* 171:    */   }
/* 172:    */   
/* 173:    */   public boolean isOpen()
/* 174:    */   {
/* 175:243 */     return this.state != State.CLOSED;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public boolean isActive()
/* 179:    */   {
/* 180:248 */     return this.state == State.ACTIVE;
/* 181:    */   }
/* 182:    */   
/* 183:    */   public Queue<Object> inboundMessages()
/* 184:    */   {
/* 185:255 */     if (this.inboundMessages == null) {
/* 186:256 */       this.inboundMessages = new ArrayDeque();
/* 187:    */     }
/* 188:258 */     return this.inboundMessages;
/* 189:    */   }
/* 190:    */   
/* 191:    */   @Deprecated
/* 192:    */   public Queue<Object> lastInboundBuffer()
/* 193:    */   {
/* 194:266 */     return inboundMessages();
/* 195:    */   }
/* 196:    */   
/* 197:    */   public Queue<Object> outboundMessages()
/* 198:    */   {
/* 199:273 */     if (this.outboundMessages == null) {
/* 200:274 */       this.outboundMessages = new ArrayDeque();
/* 201:    */     }
/* 202:276 */     return this.outboundMessages;
/* 203:    */   }
/* 204:    */   
/* 205:    */   @Deprecated
/* 206:    */   public Queue<Object> lastOutboundBuffer()
/* 207:    */   {
/* 208:284 */     return outboundMessages();
/* 209:    */   }
/* 210:    */   
/* 211:    */   public <T> T readInbound()
/* 212:    */   {
/* 213:292 */     return poll(this.inboundMessages);
/* 214:    */   }
/* 215:    */   
/* 216:    */   public <T> T readOutbound()
/* 217:    */   {
/* 218:300 */     return poll(this.outboundMessages);
/* 219:    */   }
/* 220:    */   
/* 221:    */   public boolean writeInbound(Object... msgs)
/* 222:    */   {
/* 223:311 */     ensureOpen();
/* 224:312 */     if (msgs.length == 0) {
/* 225:313 */       return isNotEmpty(this.inboundMessages);
/* 226:    */     }
/* 227:316 */     ChannelPipeline p = pipeline();
/* 228:317 */     for (Object m : msgs) {
/* 229:318 */       p.fireChannelRead(m);
/* 230:    */     }
/* 231:321 */     flushInbound(false, voidPromise());
/* 232:322 */     return isNotEmpty(this.inboundMessages);
/* 233:    */   }
/* 234:    */   
/* 235:    */   public ChannelFuture writeOneInbound(Object msg)
/* 236:    */   {
/* 237:332 */     return writeOneInbound(msg, newPromise());
/* 238:    */   }
/* 239:    */   
/* 240:    */   public ChannelFuture writeOneInbound(Object msg, ChannelPromise promise)
/* 241:    */   {
/* 242:342 */     if (checkOpen(true)) {
/* 243:343 */       pipeline().fireChannelRead(msg);
/* 244:    */     }
/* 245:345 */     return checkException(promise);
/* 246:    */   }
/* 247:    */   
/* 248:    */   public EmbeddedChannel flushInbound()
/* 249:    */   {
/* 250:354 */     flushInbound(true, voidPromise());
/* 251:355 */     return this;
/* 252:    */   }
/* 253:    */   
/* 254:    */   private ChannelFuture flushInbound(boolean recordException, ChannelPromise promise)
/* 255:    */   {
/* 256:359 */     if (checkOpen(recordException))
/* 257:    */     {
/* 258:360 */       pipeline().fireChannelReadComplete();
/* 259:361 */       runPendingTasks();
/* 260:    */     }
/* 261:364 */     return checkException(promise);
/* 262:    */   }
/* 263:    */   
/* 264:    */   public boolean writeOutbound(Object... msgs)
/* 265:    */   {
/* 266:374 */     ensureOpen();
/* 267:375 */     if (msgs.length == 0) {
/* 268:376 */       return isNotEmpty(this.outboundMessages);
/* 269:    */     }
/* 270:379 */     RecyclableArrayList futures = RecyclableArrayList.newInstance(msgs.length);
/* 271:    */     try
/* 272:    */     {
/* 273:381 */       for (Object m : msgs)
/* 274:    */       {
/* 275:382 */         if (m == null) {
/* 276:    */           break;
/* 277:    */         }
/* 278:385 */         futures.add(write(m));
/* 279:    */       }
/* 280:388 */       flushOutbound0();
/* 281:    */       
/* 282:390 */       int size = futures.size();
/* 283:391 */       for (int i = 0; i < size; i++)
/* 284:    */       {
/* 285:392 */         ChannelFuture future = (ChannelFuture)futures.get(i);
/* 286:393 */         if (future.isDone()) {
/* 287:394 */           recordException(future);
/* 288:    */         } else {
/* 289:397 */           future.addListener(this.recordExceptionListener);
/* 290:    */         }
/* 291:    */       }
/* 292:401 */       checkException();
/* 293:402 */       return isNotEmpty(this.outboundMessages);
/* 294:    */     }
/* 295:    */     finally
/* 296:    */     {
/* 297:404 */       futures.recycle();
/* 298:    */     }
/* 299:    */   }
/* 300:    */   
/* 301:    */   public ChannelFuture writeOneOutbound(Object msg)
/* 302:    */   {
/* 303:415 */     return writeOneOutbound(msg, newPromise());
/* 304:    */   }
/* 305:    */   
/* 306:    */   public ChannelFuture writeOneOutbound(Object msg, ChannelPromise promise)
/* 307:    */   {
/* 308:425 */     if (checkOpen(true)) {
/* 309:426 */       return write(msg, promise);
/* 310:    */     }
/* 311:428 */     return checkException(promise);
/* 312:    */   }
/* 313:    */   
/* 314:    */   public EmbeddedChannel flushOutbound()
/* 315:    */   {
/* 316:437 */     if (checkOpen(true)) {
/* 317:438 */       flushOutbound0();
/* 318:    */     }
/* 319:440 */     checkException(voidPromise());
/* 320:441 */     return this;
/* 321:    */   }
/* 322:    */   
/* 323:    */   private void flushOutbound0()
/* 324:    */   {
/* 325:447 */     runPendingTasks();
/* 326:    */     
/* 327:449 */     flush();
/* 328:    */   }
/* 329:    */   
/* 330:    */   public boolean finish()
/* 331:    */   {
/* 332:458 */     return finish(false);
/* 333:    */   }
/* 334:    */   
/* 335:    */   public boolean finishAndReleaseAll()
/* 336:    */   {
/* 337:468 */     return finish(true);
/* 338:    */   }
/* 339:    */   
/* 340:    */   private boolean finish(boolean releaseAll)
/* 341:    */   {
/* 342:478 */     close();
/* 343:    */     try
/* 344:    */     {
/* 345:480 */       checkException();
/* 346:481 */       return (isNotEmpty(this.inboundMessages)) || (isNotEmpty(this.outboundMessages));
/* 347:    */     }
/* 348:    */     finally
/* 349:    */     {
/* 350:483 */       if (releaseAll)
/* 351:    */       {
/* 352:484 */         releaseAll(this.inboundMessages);
/* 353:485 */         releaseAll(this.outboundMessages);
/* 354:    */       }
/* 355:    */     }
/* 356:    */   }
/* 357:    */   
/* 358:    */   public boolean releaseInbound()
/* 359:    */   {
/* 360:495 */     return releaseAll(this.inboundMessages);
/* 361:    */   }
/* 362:    */   
/* 363:    */   public boolean releaseOutbound()
/* 364:    */   {
/* 365:503 */     return releaseAll(this.outboundMessages);
/* 366:    */   }
/* 367:    */   
/* 368:    */   private static boolean releaseAll(Queue<Object> queue)
/* 369:    */   {
/* 370:507 */     if (isNotEmpty(queue))
/* 371:    */     {
/* 372:    */       for (;;)
/* 373:    */       {
/* 374:509 */         Object msg = queue.poll();
/* 375:510 */         if (msg == null) {
/* 376:    */           break;
/* 377:    */         }
/* 378:513 */         ReferenceCountUtil.release(msg);
/* 379:    */       }
/* 380:515 */       return true;
/* 381:    */     }
/* 382:517 */     return false;
/* 383:    */   }
/* 384:    */   
/* 385:    */   private void finishPendingTasks(boolean cancel)
/* 386:    */   {
/* 387:521 */     runPendingTasks();
/* 388:522 */     if (cancel) {
/* 389:524 */       this.loop.cancelScheduledTasks();
/* 390:    */     }
/* 391:    */   }
/* 392:    */   
/* 393:    */   public final ChannelFuture close()
/* 394:    */   {
/* 395:530 */     return close(newPromise());
/* 396:    */   }
/* 397:    */   
/* 398:    */   public final ChannelFuture disconnect()
/* 399:    */   {
/* 400:535 */     return disconnect(newPromise());
/* 401:    */   }
/* 402:    */   
/* 403:    */   public final ChannelFuture close(ChannelPromise promise)
/* 404:    */   {
/* 405:542 */     runPendingTasks();
/* 406:543 */     ChannelFuture future = super.close(promise);
/* 407:    */     
/* 408:    */ 
/* 409:546 */     finishPendingTasks(true);
/* 410:547 */     return future;
/* 411:    */   }
/* 412:    */   
/* 413:    */   public final ChannelFuture disconnect(ChannelPromise promise)
/* 414:    */   {
/* 415:552 */     ChannelFuture future = super.disconnect(promise);
/* 416:553 */     finishPendingTasks(!this.metadata.hasDisconnect());
/* 417:554 */     return future;
/* 418:    */   }
/* 419:    */   
/* 420:    */   private static boolean isNotEmpty(Queue<Object> queue)
/* 421:    */   {
/* 422:558 */     return (queue != null) && (!queue.isEmpty());
/* 423:    */   }
/* 424:    */   
/* 425:    */   private static Object poll(Queue<Object> queue)
/* 426:    */   {
/* 427:562 */     return queue != null ? queue.poll() : null;
/* 428:    */   }
/* 429:    */   
/* 430:    */   public void runPendingTasks()
/* 431:    */   {
/* 432:    */     try
/* 433:    */     {
/* 434:571 */       this.loop.runTasks();
/* 435:    */     }
/* 436:    */     catch (Exception e)
/* 437:    */     {
/* 438:573 */       recordException(e);
/* 439:    */     }
/* 440:    */     try
/* 441:    */     {
/* 442:577 */       this.loop.runScheduledTasks();
/* 443:    */     }
/* 444:    */     catch (Exception e)
/* 445:    */     {
/* 446:579 */       recordException(e);
/* 447:    */     }
/* 448:    */   }
/* 449:    */   
/* 450:    */   public long runScheduledPendingTasks()
/* 451:    */   {
/* 452:    */     try
/* 453:    */     {
/* 454:590 */       return this.loop.runScheduledTasks();
/* 455:    */     }
/* 456:    */     catch (Exception e)
/* 457:    */     {
/* 458:592 */       recordException(e);
/* 459:    */     }
/* 460:593 */     return this.loop.nextScheduledTask();
/* 461:    */   }
/* 462:    */   
/* 463:    */   private void recordException(ChannelFuture future)
/* 464:    */   {
/* 465:598 */     if (!future.isSuccess()) {
/* 466:599 */       recordException(future.cause());
/* 467:    */     }
/* 468:    */   }
/* 469:    */   
/* 470:    */   private void recordException(Throwable cause)
/* 471:    */   {
/* 472:604 */     if (this.lastException == null) {
/* 473:605 */       this.lastException = cause;
/* 474:    */     } else {
/* 475:607 */       logger.warn("More than one exception was raised. Will report only the first one and log others.", cause);
/* 476:    */     }
/* 477:    */   }
/* 478:    */   
/* 479:    */   private ChannelFuture checkException(ChannelPromise promise)
/* 480:    */   {
/* 481:617 */     Throwable t = this.lastException;
/* 482:618 */     if (t != null)
/* 483:    */     {
/* 484:619 */       this.lastException = null;
/* 485:621 */       if (promise.isVoid()) {
/* 486:622 */         PlatformDependent.throwException(t);
/* 487:    */       }
/* 488:625 */       return promise.setFailure(t);
/* 489:    */     }
/* 490:628 */     return promise.setSuccess();
/* 491:    */   }
/* 492:    */   
/* 493:    */   public void checkException()
/* 494:    */   {
/* 495:635 */     checkException(voidPromise());
/* 496:    */   }
/* 497:    */   
/* 498:    */   private boolean checkOpen(boolean recordException)
/* 499:    */   {
/* 500:643 */     if (!isOpen())
/* 501:    */     {
/* 502:644 */       if (recordException) {
/* 503:645 */         recordException(new ClosedChannelException());
/* 504:    */       }
/* 505:647 */       return false;
/* 506:    */     }
/* 507:650 */     return true;
/* 508:    */   }
/* 509:    */   
/* 510:    */   protected final void ensureOpen()
/* 511:    */   {
/* 512:657 */     if (!checkOpen(true)) {
/* 513:658 */       checkException();
/* 514:    */     }
/* 515:    */   }
/* 516:    */   
/* 517:    */   protected boolean isCompatible(EventLoop loop)
/* 518:    */   {
/* 519:664 */     return loop instanceof EmbeddedEventLoop;
/* 520:    */   }
/* 521:    */   
/* 522:    */   protected SocketAddress localAddress0()
/* 523:    */   {
/* 524:669 */     return isActive() ? LOCAL_ADDRESS : null;
/* 525:    */   }
/* 526:    */   
/* 527:    */   protected SocketAddress remoteAddress0()
/* 528:    */   {
/* 529:674 */     return isActive() ? REMOTE_ADDRESS : null;
/* 530:    */   }
/* 531:    */   
/* 532:    */   protected void doRegister()
/* 533:    */     throws Exception
/* 534:    */   {
/* 535:679 */     this.state = State.ACTIVE;
/* 536:    */   }
/* 537:    */   
/* 538:    */   protected void doBind(SocketAddress localAddress)
/* 539:    */     throws Exception
/* 540:    */   {}
/* 541:    */   
/* 542:    */   protected void doDisconnect()
/* 543:    */     throws Exception
/* 544:    */   {
/* 545:689 */     if (!this.metadata.hasDisconnect()) {
/* 546:690 */       doClose();
/* 547:    */     }
/* 548:    */   }
/* 549:    */   
/* 550:    */   protected void doClose()
/* 551:    */     throws Exception
/* 552:    */   {
/* 553:696 */     this.state = State.CLOSED;
/* 554:    */   }
/* 555:    */   
/* 556:    */   protected void doBeginRead()
/* 557:    */     throws Exception
/* 558:    */   {}
/* 559:    */   
/* 560:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/* 561:    */   {
/* 562:706 */     return new EmbeddedUnsafe(null);
/* 563:    */   }
/* 564:    */   
/* 565:    */   public Channel.Unsafe unsafe()
/* 566:    */   {
/* 567:711 */     return ((EmbeddedUnsafe)super.unsafe()).wrapped;
/* 568:    */   }
/* 569:    */   
/* 570:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 571:    */     throws Exception
/* 572:    */   {
/* 573:    */     for (;;)
/* 574:    */     {
/* 575:717 */       Object msg = in.current();
/* 576:718 */       if (msg == null) {
/* 577:    */         break;
/* 578:    */       }
/* 579:722 */       ReferenceCountUtil.retain(msg);
/* 580:723 */       handleOutboundMessage(msg);
/* 581:724 */       in.remove();
/* 582:    */     }
/* 583:    */   }
/* 584:    */   
/* 585:    */   protected void handleOutboundMessage(Object msg)
/* 586:    */   {
/* 587:734 */     outboundMessages().add(msg);
/* 588:    */   }
/* 589:    */   
/* 590:    */   protected void handleInboundMessage(Object msg)
/* 591:    */   {
/* 592:741 */     inboundMessages().add(msg);
/* 593:    */   }
/* 594:    */   
/* 595:    */   private final class EmbeddedUnsafe
/* 596:    */     extends AbstractChannel.AbstractUnsafe
/* 597:    */   {
/* 598:    */     private EmbeddedUnsafe()
/* 599:    */     {
/* 600:744 */       super();
/* 601:    */     }
/* 602:    */     
/* 603:748 */     final Channel.Unsafe wrapped = new Channel.Unsafe()
/* 604:    */     {
/* 605:    */       public RecvByteBufAllocator.Handle recvBufAllocHandle()
/* 606:    */       {
/* 607:751 */         return EmbeddedChannel.EmbeddedUnsafe.this.recvBufAllocHandle();
/* 608:    */       }
/* 609:    */       
/* 610:    */       public SocketAddress localAddress()
/* 611:    */       {
/* 612:756 */         return EmbeddedChannel.EmbeddedUnsafe.this.localAddress();
/* 613:    */       }
/* 614:    */       
/* 615:    */       public SocketAddress remoteAddress()
/* 616:    */       {
/* 617:761 */         return EmbeddedChannel.EmbeddedUnsafe.this.remoteAddress();
/* 618:    */       }
/* 619:    */       
/* 620:    */       public void register(EventLoop eventLoop, ChannelPromise promise)
/* 621:    */       {
/* 622:766 */         EmbeddedChannel.EmbeddedUnsafe.this.register(eventLoop, promise);
/* 623:767 */         EmbeddedChannel.this.runPendingTasks();
/* 624:    */       }
/* 625:    */       
/* 626:    */       public void bind(SocketAddress localAddress, ChannelPromise promise)
/* 627:    */       {
/* 628:772 */         EmbeddedChannel.EmbeddedUnsafe.this.bind(localAddress, promise);
/* 629:773 */         EmbeddedChannel.this.runPendingTasks();
/* 630:    */       }
/* 631:    */       
/* 632:    */       public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 633:    */       {
/* 634:778 */         EmbeddedChannel.EmbeddedUnsafe.this.connect(remoteAddress, localAddress, promise);
/* 635:779 */         EmbeddedChannel.this.runPendingTasks();
/* 636:    */       }
/* 637:    */       
/* 638:    */       public void disconnect(ChannelPromise promise)
/* 639:    */       {
/* 640:784 */         EmbeddedChannel.EmbeddedUnsafe.this.disconnect(promise);
/* 641:785 */         EmbeddedChannel.this.runPendingTasks();
/* 642:    */       }
/* 643:    */       
/* 644:    */       public void close(ChannelPromise promise)
/* 645:    */       {
/* 646:790 */         EmbeddedChannel.EmbeddedUnsafe.this.close(promise);
/* 647:791 */         EmbeddedChannel.this.runPendingTasks();
/* 648:    */       }
/* 649:    */       
/* 650:    */       public void closeForcibly()
/* 651:    */       {
/* 652:796 */         EmbeddedChannel.EmbeddedUnsafe.this.closeForcibly();
/* 653:797 */         EmbeddedChannel.this.runPendingTasks();
/* 654:    */       }
/* 655:    */       
/* 656:    */       public void deregister(ChannelPromise promise)
/* 657:    */       {
/* 658:802 */         EmbeddedChannel.EmbeddedUnsafe.this.deregister(promise);
/* 659:803 */         EmbeddedChannel.this.runPendingTasks();
/* 660:    */       }
/* 661:    */       
/* 662:    */       public void beginRead()
/* 663:    */       {
/* 664:808 */         EmbeddedChannel.EmbeddedUnsafe.this.beginRead();
/* 665:809 */         EmbeddedChannel.this.runPendingTasks();
/* 666:    */       }
/* 667:    */       
/* 668:    */       public void write(Object msg, ChannelPromise promise)
/* 669:    */       {
/* 670:814 */         EmbeddedChannel.EmbeddedUnsafe.this.write(msg, promise);
/* 671:815 */         EmbeddedChannel.this.runPendingTasks();
/* 672:    */       }
/* 673:    */       
/* 674:    */       public void flush()
/* 675:    */       {
/* 676:820 */         EmbeddedChannel.EmbeddedUnsafe.this.flush();
/* 677:821 */         EmbeddedChannel.this.runPendingTasks();
/* 678:    */       }
/* 679:    */       
/* 680:    */       public ChannelPromise voidPromise()
/* 681:    */       {
/* 682:826 */         return EmbeddedChannel.EmbeddedUnsafe.this.voidPromise();
/* 683:    */       }
/* 684:    */       
/* 685:    */       public ChannelOutboundBuffer outboundBuffer()
/* 686:    */       {
/* 687:831 */         return EmbeddedChannel.EmbeddedUnsafe.this.outboundBuffer();
/* 688:    */       }
/* 689:    */     };
/* 690:    */     
/* 691:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 692:    */     {
/* 693:837 */       safeSetSuccess(promise);
/* 694:    */     }
/* 695:    */   }
/* 696:    */   
/* 697:    */   private final class EmbeddedChannelPipeline
/* 698:    */     extends DefaultChannelPipeline
/* 699:    */   {
/* 700:    */     EmbeddedChannelPipeline(EmbeddedChannel channel)
/* 701:    */     {
/* 702:843 */       super();
/* 703:    */     }
/* 704:    */     
/* 705:    */     protected void onUnhandledInboundException(Throwable cause)
/* 706:    */     {
/* 707:848 */       EmbeddedChannel.this.recordException(cause);
/* 708:    */     }
/* 709:    */     
/* 710:    */     protected void onUnhandledInboundMessage(Object msg)
/* 711:    */     {
/* 712:853 */       EmbeddedChannel.this.handleInboundMessage(msg);
/* 713:    */     }
/* 714:    */   }
/* 715:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.embedded.EmbeddedChannel
 * JD-Core Version:    0.7.0.1
 */