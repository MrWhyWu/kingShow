/*    1:     */ package io.netty.channel;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBufAllocator;
/*    4:     */ import io.netty.util.Attribute;
/*    5:     */ import io.netty.util.AttributeKey;
/*    6:     */ import io.netty.util.DefaultAttributeMap;
/*    7:     */ import io.netty.util.Recycler;
/*    8:     */ import io.netty.util.Recycler.Handle;
/*    9:     */ import io.netty.util.ReferenceCountUtil;
/*   10:     */ import io.netty.util.ResourceLeakHint;
/*   11:     */ import io.netty.util.concurrent.EventExecutor;
/*   12:     */ import io.netty.util.concurrent.OrderedEventExecutor;
/*   13:     */ import io.netty.util.internal.ObjectUtil;
/*   14:     */ import io.netty.util.internal.PromiseNotificationUtil;
/*   15:     */ import io.netty.util.internal.StringUtil;
/*   16:     */ import io.netty.util.internal.SystemPropertyUtil;
/*   17:     */ import io.netty.util.internal.ThrowableUtil;
/*   18:     */ import io.netty.util.internal.logging.InternalLogger;
/*   19:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   20:     */ import java.net.SocketAddress;
/*   21:     */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*   22:     */ 
/*   23:     */ abstract class AbstractChannelHandlerContext
/*   24:     */   extends DefaultAttributeMap
/*   25:     */   implements ChannelHandlerContext, ResourceLeakHint
/*   26:     */ {
/*   27:  41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannelHandlerContext.class);
/*   28:     */   volatile AbstractChannelHandlerContext next;
/*   29:     */   volatile AbstractChannelHandlerContext prev;
/*   30:  46 */   private static final AtomicIntegerFieldUpdater<AbstractChannelHandlerContext> HANDLER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractChannelHandlerContext.class, "handlerState");
/*   31:     */   private static final int ADD_PENDING = 1;
/*   32:     */   private static final int ADD_COMPLETE = 2;
/*   33:     */   private static final int REMOVE_COMPLETE = 3;
/*   34:     */   private static final int INIT = 0;
/*   35:     */   private final boolean inbound;
/*   36:     */   private final boolean outbound;
/*   37:     */   private final DefaultChannelPipeline pipeline;
/*   38:     */   private final String name;
/*   39:     */   private final boolean ordered;
/*   40:     */   final EventExecutor executor;
/*   41:     */   private ChannelFuture succeededFuture;
/*   42:     */   private Runnable invokeChannelReadCompleteTask;
/*   43:     */   private Runnable invokeReadTask;
/*   44:     */   private Runnable invokeChannelWritableStateChangedTask;
/*   45:     */   private Runnable invokeFlushTask;
/*   46:  84 */   private volatile int handlerState = 0;
/*   47:     */   
/*   48:     */   AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor, String name, boolean inbound, boolean outbound)
/*   49:     */   {
/*   50:  88 */     this.name = ((String)ObjectUtil.checkNotNull(name, "name"));
/*   51:  89 */     this.pipeline = pipeline;
/*   52:  90 */     this.executor = executor;
/*   53:  91 */     this.inbound = inbound;
/*   54:  92 */     this.outbound = outbound;
/*   55:     */     
/*   56:  94 */     this.ordered = ((executor == null) || ((executor instanceof OrderedEventExecutor)));
/*   57:     */   }
/*   58:     */   
/*   59:     */   public Channel channel()
/*   60:     */   {
/*   61:  99 */     return this.pipeline.channel();
/*   62:     */   }
/*   63:     */   
/*   64:     */   public ChannelPipeline pipeline()
/*   65:     */   {
/*   66: 104 */     return this.pipeline;
/*   67:     */   }
/*   68:     */   
/*   69:     */   public ByteBufAllocator alloc()
/*   70:     */   {
/*   71: 109 */     return channel().config().getAllocator();
/*   72:     */   }
/*   73:     */   
/*   74:     */   public EventExecutor executor()
/*   75:     */   {
/*   76: 114 */     if (this.executor == null) {
/*   77: 115 */       return channel().eventLoop();
/*   78:     */     }
/*   79: 117 */     return this.executor;
/*   80:     */   }
/*   81:     */   
/*   82:     */   public String name()
/*   83:     */   {
/*   84: 123 */     return this.name;
/*   85:     */   }
/*   86:     */   
/*   87:     */   public ChannelHandlerContext fireChannelRegistered()
/*   88:     */   {
/*   89: 128 */     invokeChannelRegistered(findContextInbound());
/*   90: 129 */     return this;
/*   91:     */   }
/*   92:     */   
/*   93:     */   static void invokeChannelRegistered(AbstractChannelHandlerContext next)
/*   94:     */   {
/*   95: 133 */     EventExecutor executor = next.executor();
/*   96: 134 */     if (executor.inEventLoop()) {
/*   97: 135 */       next.invokeChannelRegistered();
/*   98:     */     } else {
/*   99: 137 */       executor.execute(new Runnable()
/*  100:     */       {
/*  101:     */         public void run()
/*  102:     */         {
/*  103: 140 */           this.val$next.invokeChannelRegistered();
/*  104:     */         }
/*  105:     */       });
/*  106:     */     }
/*  107:     */   }
/*  108:     */   
/*  109:     */   private void invokeChannelRegistered()
/*  110:     */   {
/*  111: 147 */     if (invokeHandler()) {
/*  112:     */       try
/*  113:     */       {
/*  114: 149 */         ((ChannelInboundHandler)handler()).channelRegistered(this);
/*  115:     */       }
/*  116:     */       catch (Throwable t)
/*  117:     */       {
/*  118: 151 */         notifyHandlerException(t);
/*  119:     */       }
/*  120:     */     } else {
/*  121: 154 */       fireChannelRegistered();
/*  122:     */     }
/*  123:     */   }
/*  124:     */   
/*  125:     */   public ChannelHandlerContext fireChannelUnregistered()
/*  126:     */   {
/*  127: 160 */     invokeChannelUnregistered(findContextInbound());
/*  128: 161 */     return this;
/*  129:     */   }
/*  130:     */   
/*  131:     */   static void invokeChannelUnregistered(AbstractChannelHandlerContext next)
/*  132:     */   {
/*  133: 165 */     EventExecutor executor = next.executor();
/*  134: 166 */     if (executor.inEventLoop()) {
/*  135: 167 */       next.invokeChannelUnregistered();
/*  136:     */     } else {
/*  137: 169 */       executor.execute(new Runnable()
/*  138:     */       {
/*  139:     */         public void run()
/*  140:     */         {
/*  141: 172 */           this.val$next.invokeChannelUnregistered();
/*  142:     */         }
/*  143:     */       });
/*  144:     */     }
/*  145:     */   }
/*  146:     */   
/*  147:     */   private void invokeChannelUnregistered()
/*  148:     */   {
/*  149: 179 */     if (invokeHandler()) {
/*  150:     */       try
/*  151:     */       {
/*  152: 181 */         ((ChannelInboundHandler)handler()).channelUnregistered(this);
/*  153:     */       }
/*  154:     */       catch (Throwable t)
/*  155:     */       {
/*  156: 183 */         notifyHandlerException(t);
/*  157:     */       }
/*  158:     */     } else {
/*  159: 186 */       fireChannelUnregistered();
/*  160:     */     }
/*  161:     */   }
/*  162:     */   
/*  163:     */   public ChannelHandlerContext fireChannelActive()
/*  164:     */   {
/*  165: 192 */     invokeChannelActive(findContextInbound());
/*  166: 193 */     return this;
/*  167:     */   }
/*  168:     */   
/*  169:     */   static void invokeChannelActive(AbstractChannelHandlerContext next)
/*  170:     */   {
/*  171: 197 */     EventExecutor executor = next.executor();
/*  172: 198 */     if (executor.inEventLoop()) {
/*  173: 199 */       next.invokeChannelActive();
/*  174:     */     } else {
/*  175: 201 */       executor.execute(new Runnable()
/*  176:     */       {
/*  177:     */         public void run()
/*  178:     */         {
/*  179: 204 */           this.val$next.invokeChannelActive();
/*  180:     */         }
/*  181:     */       });
/*  182:     */     }
/*  183:     */   }
/*  184:     */   
/*  185:     */   private void invokeChannelActive()
/*  186:     */   {
/*  187: 211 */     if (invokeHandler()) {
/*  188:     */       try
/*  189:     */       {
/*  190: 213 */         ((ChannelInboundHandler)handler()).channelActive(this);
/*  191:     */       }
/*  192:     */       catch (Throwable t)
/*  193:     */       {
/*  194: 215 */         notifyHandlerException(t);
/*  195:     */       }
/*  196:     */     } else {
/*  197: 218 */       fireChannelActive();
/*  198:     */     }
/*  199:     */   }
/*  200:     */   
/*  201:     */   public ChannelHandlerContext fireChannelInactive()
/*  202:     */   {
/*  203: 224 */     invokeChannelInactive(findContextInbound());
/*  204: 225 */     return this;
/*  205:     */   }
/*  206:     */   
/*  207:     */   static void invokeChannelInactive(AbstractChannelHandlerContext next)
/*  208:     */   {
/*  209: 229 */     EventExecutor executor = next.executor();
/*  210: 230 */     if (executor.inEventLoop()) {
/*  211: 231 */       next.invokeChannelInactive();
/*  212:     */     } else {
/*  213: 233 */       executor.execute(new Runnable()
/*  214:     */       {
/*  215:     */         public void run()
/*  216:     */         {
/*  217: 236 */           this.val$next.invokeChannelInactive();
/*  218:     */         }
/*  219:     */       });
/*  220:     */     }
/*  221:     */   }
/*  222:     */   
/*  223:     */   private void invokeChannelInactive()
/*  224:     */   {
/*  225: 243 */     if (invokeHandler()) {
/*  226:     */       try
/*  227:     */       {
/*  228: 245 */         ((ChannelInboundHandler)handler()).channelInactive(this);
/*  229:     */       }
/*  230:     */       catch (Throwable t)
/*  231:     */       {
/*  232: 247 */         notifyHandlerException(t);
/*  233:     */       }
/*  234:     */     } else {
/*  235: 250 */       fireChannelInactive();
/*  236:     */     }
/*  237:     */   }
/*  238:     */   
/*  239:     */   public ChannelHandlerContext fireExceptionCaught(Throwable cause)
/*  240:     */   {
/*  241: 256 */     invokeExceptionCaught(this.next, cause);
/*  242: 257 */     return this;
/*  243:     */   }
/*  244:     */   
/*  245:     */   static void invokeExceptionCaught(AbstractChannelHandlerContext next, final Throwable cause)
/*  246:     */   {
/*  247: 261 */     ObjectUtil.checkNotNull(cause, "cause");
/*  248: 262 */     EventExecutor executor = next.executor();
/*  249: 263 */     if (executor.inEventLoop()) {
/*  250: 264 */       next.invokeExceptionCaught(cause);
/*  251:     */     } else {
/*  252:     */       try
/*  253:     */       {
/*  254: 267 */         executor.execute(new Runnable()
/*  255:     */         {
/*  256:     */           public void run()
/*  257:     */           {
/*  258: 270 */             this.val$next.invokeExceptionCaught(cause);
/*  259:     */           }
/*  260:     */         });
/*  261:     */       }
/*  262:     */       catch (Throwable t)
/*  263:     */       {
/*  264: 274 */         if (logger.isWarnEnabled())
/*  265:     */         {
/*  266: 275 */           logger.warn("Failed to submit an exceptionCaught() event.", t);
/*  267: 276 */           logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
/*  268:     */         }
/*  269:     */       }
/*  270:     */     }
/*  271:     */   }
/*  272:     */   
/*  273:     */   private void invokeExceptionCaught(Throwable cause)
/*  274:     */   {
/*  275: 283 */     if (invokeHandler()) {
/*  276:     */       try
/*  277:     */       {
/*  278: 285 */         handler().exceptionCaught(this, cause);
/*  279:     */       }
/*  280:     */       catch (Throwable error)
/*  281:     */       {
/*  282: 287 */         if (logger.isDebugEnabled()) {
/*  283: 288 */           logger.debug("An exception {}was thrown by a user handler's exceptionCaught() method while handling the following exception:", 
/*  284:     */           
/*  285:     */ 
/*  286:     */ 
/*  287: 292 */             ThrowableUtil.stackTraceToString(error), cause);
/*  288: 293 */         } else if (logger.isWarnEnabled()) {
/*  289: 294 */           logger.warn("An exception '{}' [enable DEBUG level for full stacktrace] was thrown by a user handler's exceptionCaught() method while handling the following exception:", error, cause);
/*  290:     */         }
/*  291:     */       }
/*  292:     */     } else {
/*  293: 301 */       fireExceptionCaught(cause);
/*  294:     */     }
/*  295:     */   }
/*  296:     */   
/*  297:     */   public ChannelHandlerContext fireUserEventTriggered(Object event)
/*  298:     */   {
/*  299: 307 */     invokeUserEventTriggered(findContextInbound(), event);
/*  300: 308 */     return this;
/*  301:     */   }
/*  302:     */   
/*  303:     */   static void invokeUserEventTriggered(AbstractChannelHandlerContext next, final Object event)
/*  304:     */   {
/*  305: 312 */     ObjectUtil.checkNotNull(event, "event");
/*  306: 313 */     EventExecutor executor = next.executor();
/*  307: 314 */     if (executor.inEventLoop()) {
/*  308: 315 */       next.invokeUserEventTriggered(event);
/*  309:     */     } else {
/*  310: 317 */       executor.execute(new Runnable()
/*  311:     */       {
/*  312:     */         public void run()
/*  313:     */         {
/*  314: 320 */           this.val$next.invokeUserEventTriggered(event);
/*  315:     */         }
/*  316:     */       });
/*  317:     */     }
/*  318:     */   }
/*  319:     */   
/*  320:     */   private void invokeUserEventTriggered(Object event)
/*  321:     */   {
/*  322: 327 */     if (invokeHandler()) {
/*  323:     */       try
/*  324:     */       {
/*  325: 329 */         ((ChannelInboundHandler)handler()).userEventTriggered(this, event);
/*  326:     */       }
/*  327:     */       catch (Throwable t)
/*  328:     */       {
/*  329: 331 */         notifyHandlerException(t);
/*  330:     */       }
/*  331:     */     } else {
/*  332: 334 */       fireUserEventTriggered(event);
/*  333:     */     }
/*  334:     */   }
/*  335:     */   
/*  336:     */   public ChannelHandlerContext fireChannelRead(Object msg)
/*  337:     */   {
/*  338: 340 */     invokeChannelRead(findContextInbound(), msg);
/*  339: 341 */     return this;
/*  340:     */   }
/*  341:     */   
/*  342:     */   static void invokeChannelRead(AbstractChannelHandlerContext next, Object msg)
/*  343:     */   {
/*  344: 345 */     final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
/*  345: 346 */     EventExecutor executor = next.executor();
/*  346: 347 */     if (executor.inEventLoop()) {
/*  347: 348 */       next.invokeChannelRead(m);
/*  348:     */     } else {
/*  349: 350 */       executor.execute(new Runnable()
/*  350:     */       {
/*  351:     */         public void run()
/*  352:     */         {
/*  353: 353 */           this.val$next.invokeChannelRead(m);
/*  354:     */         }
/*  355:     */       });
/*  356:     */     }
/*  357:     */   }
/*  358:     */   
/*  359:     */   private void invokeChannelRead(Object msg)
/*  360:     */   {
/*  361: 360 */     if (invokeHandler()) {
/*  362:     */       try
/*  363:     */       {
/*  364: 362 */         ((ChannelInboundHandler)handler()).channelRead(this, msg);
/*  365:     */       }
/*  366:     */       catch (Throwable t)
/*  367:     */       {
/*  368: 364 */         notifyHandlerException(t);
/*  369:     */       }
/*  370:     */     } else {
/*  371: 367 */       fireChannelRead(msg);
/*  372:     */     }
/*  373:     */   }
/*  374:     */   
/*  375:     */   public ChannelHandlerContext fireChannelReadComplete()
/*  376:     */   {
/*  377: 373 */     invokeChannelReadComplete(findContextInbound());
/*  378: 374 */     return this;
/*  379:     */   }
/*  380:     */   
/*  381:     */   static void invokeChannelReadComplete(AbstractChannelHandlerContext next)
/*  382:     */   {
/*  383: 378 */     EventExecutor executor = next.executor();
/*  384: 379 */     if (executor.inEventLoop())
/*  385:     */     {
/*  386: 380 */       next.invokeChannelReadComplete();
/*  387:     */     }
/*  388:     */     else
/*  389:     */     {
/*  390: 382 */       Runnable task = next.invokeChannelReadCompleteTask;
/*  391: 383 */       if (task == null) {
/*  392: 384 */         next.invokeChannelReadCompleteTask = (task = new Runnable()
/*  393:     */         {
/*  394:     */           public void run()
/*  395:     */           {
/*  396: 387 */             this.val$next.invokeChannelReadComplete();
/*  397:     */           }
/*  398:     */         });
/*  399:     */       }
/*  400: 391 */       executor.execute(task);
/*  401:     */     }
/*  402:     */   }
/*  403:     */   
/*  404:     */   private void invokeChannelReadComplete()
/*  405:     */   {
/*  406: 396 */     if (invokeHandler()) {
/*  407:     */       try
/*  408:     */       {
/*  409: 398 */         ((ChannelInboundHandler)handler()).channelReadComplete(this);
/*  410:     */       }
/*  411:     */       catch (Throwable t)
/*  412:     */       {
/*  413: 400 */         notifyHandlerException(t);
/*  414:     */       }
/*  415:     */     } else {
/*  416: 403 */       fireChannelReadComplete();
/*  417:     */     }
/*  418:     */   }
/*  419:     */   
/*  420:     */   public ChannelHandlerContext fireChannelWritabilityChanged()
/*  421:     */   {
/*  422: 409 */     invokeChannelWritabilityChanged(findContextInbound());
/*  423: 410 */     return this;
/*  424:     */   }
/*  425:     */   
/*  426:     */   static void invokeChannelWritabilityChanged(AbstractChannelHandlerContext next)
/*  427:     */   {
/*  428: 414 */     EventExecutor executor = next.executor();
/*  429: 415 */     if (executor.inEventLoop())
/*  430:     */     {
/*  431: 416 */       next.invokeChannelWritabilityChanged();
/*  432:     */     }
/*  433:     */     else
/*  434:     */     {
/*  435: 418 */       Runnable task = next.invokeChannelWritableStateChangedTask;
/*  436: 419 */       if (task == null) {
/*  437: 420 */         next.invokeChannelWritableStateChangedTask = (task = new Runnable()
/*  438:     */         {
/*  439:     */           public void run()
/*  440:     */           {
/*  441: 423 */             this.val$next.invokeChannelWritabilityChanged();
/*  442:     */           }
/*  443:     */         });
/*  444:     */       }
/*  445: 427 */       executor.execute(task);
/*  446:     */     }
/*  447:     */   }
/*  448:     */   
/*  449:     */   private void invokeChannelWritabilityChanged()
/*  450:     */   {
/*  451: 432 */     if (invokeHandler()) {
/*  452:     */       try
/*  453:     */       {
/*  454: 434 */         ((ChannelInboundHandler)handler()).channelWritabilityChanged(this);
/*  455:     */       }
/*  456:     */       catch (Throwable t)
/*  457:     */       {
/*  458: 436 */         notifyHandlerException(t);
/*  459:     */       }
/*  460:     */     } else {
/*  461: 439 */       fireChannelWritabilityChanged();
/*  462:     */     }
/*  463:     */   }
/*  464:     */   
/*  465:     */   public ChannelFuture bind(SocketAddress localAddress)
/*  466:     */   {
/*  467: 445 */     return bind(localAddress, newPromise());
/*  468:     */   }
/*  469:     */   
/*  470:     */   public ChannelFuture connect(SocketAddress remoteAddress)
/*  471:     */   {
/*  472: 450 */     return connect(remoteAddress, newPromise());
/*  473:     */   }
/*  474:     */   
/*  475:     */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  476:     */   {
/*  477: 455 */     return connect(remoteAddress, localAddress, newPromise());
/*  478:     */   }
/*  479:     */   
/*  480:     */   public ChannelFuture disconnect()
/*  481:     */   {
/*  482: 460 */     return disconnect(newPromise());
/*  483:     */   }
/*  484:     */   
/*  485:     */   public ChannelFuture close()
/*  486:     */   {
/*  487: 465 */     return close(newPromise());
/*  488:     */   }
/*  489:     */   
/*  490:     */   public ChannelFuture deregister()
/*  491:     */   {
/*  492: 470 */     return deregister(newPromise());
/*  493:     */   }
/*  494:     */   
/*  495:     */   public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise)
/*  496:     */   {
/*  497: 475 */     if (localAddress == null) {
/*  498: 476 */       throw new NullPointerException("localAddress");
/*  499:     */     }
/*  500: 478 */     if (isNotValidPromise(promise, false)) {
/*  501: 480 */       return promise;
/*  502:     */     }
/*  503: 483 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  504: 484 */     EventExecutor executor = next.executor();
/*  505: 485 */     if (executor.inEventLoop()) {
/*  506: 486 */       next.invokeBind(localAddress, promise);
/*  507:     */     } else {
/*  508: 488 */       safeExecute(executor, new Runnable()
/*  509:     */       {
/*  510:     */         public void run()
/*  511:     */         {
/*  512: 491 */           next.invokeBind(localAddress, promise);
/*  513:     */         }
/*  514: 491 */       }, promise, null);
/*  515:     */     }
/*  516: 495 */     return promise;
/*  517:     */   }
/*  518:     */   
/*  519:     */   private void invokeBind(SocketAddress localAddress, ChannelPromise promise)
/*  520:     */   {
/*  521: 499 */     if (invokeHandler()) {
/*  522:     */       try
/*  523:     */       {
/*  524: 501 */         ((ChannelOutboundHandler)handler()).bind(this, localAddress, promise);
/*  525:     */       }
/*  526:     */       catch (Throwable t)
/*  527:     */       {
/*  528: 503 */         notifyOutboundHandlerException(t, promise);
/*  529:     */       }
/*  530:     */     } else {
/*  531: 506 */       bind(localAddress, promise);
/*  532:     */     }
/*  533:     */   }
/*  534:     */   
/*  535:     */   public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/*  536:     */   {
/*  537: 512 */     return connect(remoteAddress, null, promise);
/*  538:     */   }
/*  539:     */   
/*  540:     */   public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise)
/*  541:     */   {
/*  542: 519 */     if (remoteAddress == null) {
/*  543: 520 */       throw new NullPointerException("remoteAddress");
/*  544:     */     }
/*  545: 522 */     if (isNotValidPromise(promise, false)) {
/*  546: 524 */       return promise;
/*  547:     */     }
/*  548: 527 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  549: 528 */     EventExecutor executor = next.executor();
/*  550: 529 */     if (executor.inEventLoop()) {
/*  551: 530 */       next.invokeConnect(remoteAddress, localAddress, promise);
/*  552:     */     } else {
/*  553: 532 */       safeExecute(executor, new Runnable()
/*  554:     */       {
/*  555:     */         public void run()
/*  556:     */         {
/*  557: 535 */           next.invokeConnect(remoteAddress, localAddress, promise);
/*  558:     */         }
/*  559: 535 */       }, promise, null);
/*  560:     */     }
/*  561: 539 */     return promise;
/*  562:     */   }
/*  563:     */   
/*  564:     */   private void invokeConnect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  565:     */   {
/*  566: 543 */     if (invokeHandler()) {
/*  567:     */       try
/*  568:     */       {
/*  569: 545 */         ((ChannelOutboundHandler)handler()).connect(this, remoteAddress, localAddress, promise);
/*  570:     */       }
/*  571:     */       catch (Throwable t)
/*  572:     */       {
/*  573: 547 */         notifyOutboundHandlerException(t, promise);
/*  574:     */       }
/*  575:     */     } else {
/*  576: 550 */       connect(remoteAddress, localAddress, promise);
/*  577:     */     }
/*  578:     */   }
/*  579:     */   
/*  580:     */   public ChannelFuture disconnect(final ChannelPromise promise)
/*  581:     */   {
/*  582: 556 */     if (isNotValidPromise(promise, false)) {
/*  583: 558 */       return promise;
/*  584:     */     }
/*  585: 561 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  586: 562 */     EventExecutor executor = next.executor();
/*  587: 563 */     if (executor.inEventLoop())
/*  588:     */     {
/*  589: 566 */       if (!channel().metadata().hasDisconnect()) {
/*  590: 567 */         next.invokeClose(promise);
/*  591:     */       } else {
/*  592: 569 */         next.invokeDisconnect(promise);
/*  593:     */       }
/*  594:     */     }
/*  595:     */     else {
/*  596: 572 */       safeExecute(executor, new Runnable()
/*  597:     */       {
/*  598:     */         public void run()
/*  599:     */         {
/*  600: 575 */           if (!AbstractChannelHandlerContext.this.channel().metadata().hasDisconnect()) {
/*  601: 576 */             next.invokeClose(promise);
/*  602:     */           } else {
/*  603: 578 */             next.invokeDisconnect(promise);
/*  604:     */           }
/*  605:     */         }
/*  606: 578 */       }, promise, null);
/*  607:     */     }
/*  608: 583 */     return promise;
/*  609:     */   }
/*  610:     */   
/*  611:     */   private void invokeDisconnect(ChannelPromise promise)
/*  612:     */   {
/*  613: 587 */     if (invokeHandler()) {
/*  614:     */       try
/*  615:     */       {
/*  616: 589 */         ((ChannelOutboundHandler)handler()).disconnect(this, promise);
/*  617:     */       }
/*  618:     */       catch (Throwable t)
/*  619:     */       {
/*  620: 591 */         notifyOutboundHandlerException(t, promise);
/*  621:     */       }
/*  622:     */     } else {
/*  623: 594 */       disconnect(promise);
/*  624:     */     }
/*  625:     */   }
/*  626:     */   
/*  627:     */   public ChannelFuture close(final ChannelPromise promise)
/*  628:     */   {
/*  629: 600 */     if (isNotValidPromise(promise, false)) {
/*  630: 602 */       return promise;
/*  631:     */     }
/*  632: 605 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  633: 606 */     EventExecutor executor = next.executor();
/*  634: 607 */     if (executor.inEventLoop()) {
/*  635: 608 */       next.invokeClose(promise);
/*  636:     */     } else {
/*  637: 610 */       safeExecute(executor, new Runnable()
/*  638:     */       {
/*  639:     */         public void run()
/*  640:     */         {
/*  641: 613 */           next.invokeClose(promise);
/*  642:     */         }
/*  643: 613 */       }, promise, null);
/*  644:     */     }
/*  645: 618 */     return promise;
/*  646:     */   }
/*  647:     */   
/*  648:     */   private void invokeClose(ChannelPromise promise)
/*  649:     */   {
/*  650: 622 */     if (invokeHandler()) {
/*  651:     */       try
/*  652:     */       {
/*  653: 624 */         ((ChannelOutboundHandler)handler()).close(this, promise);
/*  654:     */       }
/*  655:     */       catch (Throwable t)
/*  656:     */       {
/*  657: 626 */         notifyOutboundHandlerException(t, promise);
/*  658:     */       }
/*  659:     */     } else {
/*  660: 629 */       close(promise);
/*  661:     */     }
/*  662:     */   }
/*  663:     */   
/*  664:     */   public ChannelFuture deregister(final ChannelPromise promise)
/*  665:     */   {
/*  666: 635 */     if (isNotValidPromise(promise, false)) {
/*  667: 637 */       return promise;
/*  668:     */     }
/*  669: 640 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  670: 641 */     EventExecutor executor = next.executor();
/*  671: 642 */     if (executor.inEventLoop()) {
/*  672: 643 */       next.invokeDeregister(promise);
/*  673:     */     } else {
/*  674: 645 */       safeExecute(executor, new Runnable()
/*  675:     */       {
/*  676:     */         public void run()
/*  677:     */         {
/*  678: 648 */           next.invokeDeregister(promise);
/*  679:     */         }
/*  680: 648 */       }, promise, null);
/*  681:     */     }
/*  682: 653 */     return promise;
/*  683:     */   }
/*  684:     */   
/*  685:     */   private void invokeDeregister(ChannelPromise promise)
/*  686:     */   {
/*  687: 657 */     if (invokeHandler()) {
/*  688:     */       try
/*  689:     */       {
/*  690: 659 */         ((ChannelOutboundHandler)handler()).deregister(this, promise);
/*  691:     */       }
/*  692:     */       catch (Throwable t)
/*  693:     */       {
/*  694: 661 */         notifyOutboundHandlerException(t, promise);
/*  695:     */       }
/*  696:     */     } else {
/*  697: 664 */       deregister(promise);
/*  698:     */     }
/*  699:     */   }
/*  700:     */   
/*  701:     */   public ChannelHandlerContext read()
/*  702:     */   {
/*  703: 670 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  704: 671 */     EventExecutor executor = next.executor();
/*  705: 672 */     if (executor.inEventLoop())
/*  706:     */     {
/*  707: 673 */       next.invokeRead();
/*  708:     */     }
/*  709:     */     else
/*  710:     */     {
/*  711: 675 */       Runnable task = next.invokeReadTask;
/*  712: 676 */       if (task == null) {
/*  713: 677 */         next.invokeReadTask = (task = new Runnable()
/*  714:     */         {
/*  715:     */           public void run()
/*  716:     */           {
/*  717: 680 */             next.invokeRead();
/*  718:     */           }
/*  719:     */         });
/*  720:     */       }
/*  721: 684 */       executor.execute(task);
/*  722:     */     }
/*  723: 687 */     return this;
/*  724:     */   }
/*  725:     */   
/*  726:     */   private void invokeRead()
/*  727:     */   {
/*  728: 691 */     if (invokeHandler()) {
/*  729:     */       try
/*  730:     */       {
/*  731: 693 */         ((ChannelOutboundHandler)handler()).read(this);
/*  732:     */       }
/*  733:     */       catch (Throwable t)
/*  734:     */       {
/*  735: 695 */         notifyHandlerException(t);
/*  736:     */       }
/*  737:     */     } else {
/*  738: 698 */       read();
/*  739:     */     }
/*  740:     */   }
/*  741:     */   
/*  742:     */   public ChannelFuture write(Object msg)
/*  743:     */   {
/*  744: 704 */     return write(msg, newPromise());
/*  745:     */   }
/*  746:     */   
/*  747:     */   public ChannelFuture write(Object msg, ChannelPromise promise)
/*  748:     */   {
/*  749: 709 */     if (msg == null) {
/*  750: 710 */       throw new NullPointerException("msg");
/*  751:     */     }
/*  752:     */     try
/*  753:     */     {
/*  754: 714 */       if (isNotValidPromise(promise, true))
/*  755:     */       {
/*  756: 715 */         ReferenceCountUtil.release(msg);
/*  757:     */         
/*  758: 717 */         return promise;
/*  759:     */       }
/*  760:     */     }
/*  761:     */     catch (RuntimeException e)
/*  762:     */     {
/*  763: 720 */       ReferenceCountUtil.release(msg);
/*  764: 721 */       throw e;
/*  765:     */     }
/*  766: 723 */     write(msg, false, promise);
/*  767:     */     
/*  768: 725 */     return promise;
/*  769:     */   }
/*  770:     */   
/*  771:     */   private void invokeWrite(Object msg, ChannelPromise promise)
/*  772:     */   {
/*  773: 729 */     if (invokeHandler()) {
/*  774: 730 */       invokeWrite0(msg, promise);
/*  775:     */     } else {
/*  776: 732 */       write(msg, promise);
/*  777:     */     }
/*  778:     */   }
/*  779:     */   
/*  780:     */   private void invokeWrite0(Object msg, ChannelPromise promise)
/*  781:     */   {
/*  782:     */     try
/*  783:     */     {
/*  784: 738 */       ((ChannelOutboundHandler)handler()).write(this, msg, promise);
/*  785:     */     }
/*  786:     */     catch (Throwable t)
/*  787:     */     {
/*  788: 740 */       notifyOutboundHandlerException(t, promise);
/*  789:     */     }
/*  790:     */   }
/*  791:     */   
/*  792:     */   public ChannelHandlerContext flush()
/*  793:     */   {
/*  794: 746 */     final AbstractChannelHandlerContext next = findContextOutbound();
/*  795: 747 */     EventExecutor executor = next.executor();
/*  796: 748 */     if (executor.inEventLoop())
/*  797:     */     {
/*  798: 749 */       next.invokeFlush();
/*  799:     */     }
/*  800:     */     else
/*  801:     */     {
/*  802: 751 */       Runnable task = next.invokeFlushTask;
/*  803: 752 */       if (task == null) {
/*  804: 753 */         next.invokeFlushTask = (task = new Runnable()
/*  805:     */         {
/*  806:     */           public void run()
/*  807:     */           {
/*  808: 756 */             next.invokeFlush();
/*  809:     */           }
/*  810:     */         });
/*  811:     */       }
/*  812: 760 */       safeExecute(executor, task, channel().voidPromise(), null);
/*  813:     */     }
/*  814: 763 */     return this;
/*  815:     */   }
/*  816:     */   
/*  817:     */   private void invokeFlush()
/*  818:     */   {
/*  819: 767 */     if (invokeHandler()) {
/*  820: 768 */       invokeFlush0();
/*  821:     */     } else {
/*  822: 770 */       flush();
/*  823:     */     }
/*  824:     */   }
/*  825:     */   
/*  826:     */   private void invokeFlush0()
/*  827:     */   {
/*  828:     */     try
/*  829:     */     {
/*  830: 776 */       ((ChannelOutboundHandler)handler()).flush(this);
/*  831:     */     }
/*  832:     */     catch (Throwable t)
/*  833:     */     {
/*  834: 778 */       notifyHandlerException(t);
/*  835:     */     }
/*  836:     */   }
/*  837:     */   
/*  838:     */   public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/*  839:     */   {
/*  840: 784 */     if (msg == null) {
/*  841: 785 */       throw new NullPointerException("msg");
/*  842:     */     }
/*  843: 788 */     if (isNotValidPromise(promise, true))
/*  844:     */     {
/*  845: 789 */       ReferenceCountUtil.release(msg);
/*  846:     */       
/*  847: 791 */       return promise;
/*  848:     */     }
/*  849: 794 */     write(msg, true, promise);
/*  850:     */     
/*  851: 796 */     return promise;
/*  852:     */   }
/*  853:     */   
/*  854:     */   private void invokeWriteAndFlush(Object msg, ChannelPromise promise)
/*  855:     */   {
/*  856: 800 */     if (invokeHandler())
/*  857:     */     {
/*  858: 801 */       invokeWrite0(msg, promise);
/*  859: 802 */       invokeFlush0();
/*  860:     */     }
/*  861:     */     else
/*  862:     */     {
/*  863: 804 */       writeAndFlush(msg, promise);
/*  864:     */     }
/*  865:     */   }
/*  866:     */   
/*  867:     */   private void write(Object msg, boolean flush, ChannelPromise promise)
/*  868:     */   {
/*  869: 809 */     AbstractChannelHandlerContext next = findContextOutbound();
/*  870: 810 */     Object m = this.pipeline.touch(msg, next);
/*  871: 811 */     EventExecutor executor = next.executor();
/*  872: 812 */     if (executor.inEventLoop())
/*  873:     */     {
/*  874: 813 */       if (flush) {
/*  875: 814 */         next.invokeWriteAndFlush(m, promise);
/*  876:     */       } else {
/*  877: 816 */         next.invokeWrite(m, promise);
/*  878:     */       }
/*  879:     */     }
/*  880:     */     else
/*  881:     */     {
/*  882:     */       AbstractWriteTask task;
/*  883:     */       AbstractWriteTask task;
/*  884: 820 */       if (flush) {
/*  885: 821 */         task = WriteAndFlushTask.newInstance(next, m, promise);
/*  886:     */       } else {
/*  887: 823 */         task = WriteTask.newInstance(next, m, promise);
/*  888:     */       }
/*  889: 825 */       safeExecute(executor, task, promise, m);
/*  890:     */     }
/*  891:     */   }
/*  892:     */   
/*  893:     */   public ChannelFuture writeAndFlush(Object msg)
/*  894:     */   {
/*  895: 831 */     return writeAndFlush(msg, newPromise());
/*  896:     */   }
/*  897:     */   
/*  898:     */   private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise)
/*  899:     */   {
/*  900: 837 */     PromiseNotificationUtil.tryFailure(promise, cause, (promise instanceof VoidChannelPromise) ? null : logger);
/*  901:     */   }
/*  902:     */   
/*  903:     */   private void notifyHandlerException(Throwable cause)
/*  904:     */   {
/*  905: 841 */     if (inExceptionCaught(cause))
/*  906:     */     {
/*  907: 842 */       if (logger.isWarnEnabled()) {
/*  908: 843 */         logger.warn("An exception was thrown by a user handler while handling an exceptionCaught event", cause);
/*  909:     */       }
/*  910: 847 */       return;
/*  911:     */     }
/*  912: 850 */     invokeExceptionCaught(cause);
/*  913:     */   }
/*  914:     */   
/*  915:     */   private static boolean inExceptionCaught(Throwable cause)
/*  916:     */   {
/*  917:     */     do
/*  918:     */     {
/*  919: 855 */       StackTraceElement[] trace = cause.getStackTrace();
/*  920: 856 */       if (trace != null) {
/*  921: 857 */         for (StackTraceElement t : trace)
/*  922:     */         {
/*  923: 858 */           if (t == null) {
/*  924:     */             break;
/*  925:     */           }
/*  926: 861 */           if ("exceptionCaught".equals(t.getMethodName())) {
/*  927: 862 */             return true;
/*  928:     */           }
/*  929:     */         }
/*  930:     */       }
/*  931: 867 */       cause = cause.getCause();
/*  932: 868 */     } while (cause != null);
/*  933: 870 */     return false;
/*  934:     */   }
/*  935:     */   
/*  936:     */   public ChannelPromise newPromise()
/*  937:     */   {
/*  938: 875 */     return new DefaultChannelPromise(channel(), executor());
/*  939:     */   }
/*  940:     */   
/*  941:     */   public ChannelProgressivePromise newProgressivePromise()
/*  942:     */   {
/*  943: 880 */     return new DefaultChannelProgressivePromise(channel(), executor());
/*  944:     */   }
/*  945:     */   
/*  946:     */   public ChannelFuture newSucceededFuture()
/*  947:     */   {
/*  948: 885 */     ChannelFuture succeededFuture = this.succeededFuture;
/*  949: 886 */     if (succeededFuture == null) {
/*  950: 887 */       this.succeededFuture = (succeededFuture = new SucceededChannelFuture(channel(), executor()));
/*  951:     */     }
/*  952: 889 */     return succeededFuture;
/*  953:     */   }
/*  954:     */   
/*  955:     */   public ChannelFuture newFailedFuture(Throwable cause)
/*  956:     */   {
/*  957: 894 */     return new FailedChannelFuture(channel(), executor(), cause);
/*  958:     */   }
/*  959:     */   
/*  960:     */   private boolean isNotValidPromise(ChannelPromise promise, boolean allowVoidPromise)
/*  961:     */   {
/*  962: 898 */     if (promise == null) {
/*  963: 899 */       throw new NullPointerException("promise");
/*  964:     */     }
/*  965: 902 */     if (promise.isDone())
/*  966:     */     {
/*  967: 907 */       if (promise.isCancelled()) {
/*  968: 908 */         return true;
/*  969:     */       }
/*  970: 910 */       throw new IllegalArgumentException("promise already done: " + promise);
/*  971:     */     }
/*  972: 913 */     if (promise.channel() != channel()) {
/*  973: 914 */       throw new IllegalArgumentException(String.format("promise.channel does not match: %s (expected: %s)", new Object[] {promise
/*  974: 915 */         .channel(), channel() }));
/*  975:     */     }
/*  976: 918 */     if (promise.getClass() == DefaultChannelPromise.class) {
/*  977: 919 */       return false;
/*  978:     */     }
/*  979: 922 */     if ((!allowVoidPromise) && ((promise instanceof VoidChannelPromise))) {
/*  980: 924 */       throw new IllegalArgumentException(StringUtil.simpleClassName(VoidChannelPromise.class) + " not allowed for this operation");
/*  981:     */     }
/*  982: 927 */     if ((promise instanceof AbstractChannel.CloseFuture)) {
/*  983: 929 */       throw new IllegalArgumentException(StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not allowed in a pipeline");
/*  984:     */     }
/*  985: 931 */     return false;
/*  986:     */   }
/*  987:     */   
/*  988:     */   private AbstractChannelHandlerContext findContextInbound()
/*  989:     */   {
/*  990: 935 */     AbstractChannelHandlerContext ctx = this;
/*  991:     */     do
/*  992:     */     {
/*  993: 937 */       ctx = ctx.next;
/*  994: 938 */     } while (!ctx.inbound);
/*  995: 939 */     return ctx;
/*  996:     */   }
/*  997:     */   
/*  998:     */   private AbstractChannelHandlerContext findContextOutbound()
/*  999:     */   {
/* 1000: 943 */     AbstractChannelHandlerContext ctx = this;
/* 1001:     */     do
/* 1002:     */     {
/* 1003: 945 */       ctx = ctx.prev;
/* 1004: 946 */     } while (!ctx.outbound);
/* 1005: 947 */     return ctx;
/* 1006:     */   }
/* 1007:     */   
/* 1008:     */   public ChannelPromise voidPromise()
/* 1009:     */   {
/* 1010: 952 */     return channel().voidPromise();
/* 1011:     */   }
/* 1012:     */   
/* 1013:     */   final void setRemoved()
/* 1014:     */   {
/* 1015: 956 */     this.handlerState = 3;
/* 1016:     */   }
/* 1017:     */   
/* 1018:     */   final void setAddComplete()
/* 1019:     */   {
/* 1020:     */     for (;;)
/* 1021:     */     {
/* 1022: 961 */       int oldState = this.handlerState;
/* 1023: 965 */       if ((oldState == 3) || (HANDLER_STATE_UPDATER.compareAndSet(this, oldState, 2))) {
/* 1024: 966 */         return;
/* 1025:     */       }
/* 1026:     */     }
/* 1027:     */   }
/* 1028:     */   
/* 1029:     */   final void setAddPending()
/* 1030:     */   {
/* 1031: 972 */     boolean updated = HANDLER_STATE_UPDATER.compareAndSet(this, 0, 1);
/* 1032: 973 */     assert (updated);
/* 1033:     */   }
/* 1034:     */   
/* 1035:     */   private boolean invokeHandler()
/* 1036:     */   {
/* 1037: 986 */     int handlerState = this.handlerState;
/* 1038: 987 */     return (handlerState == 2) || ((!this.ordered) && (handlerState == 1));
/* 1039:     */   }
/* 1040:     */   
/* 1041:     */   public boolean isRemoved()
/* 1042:     */   {
/* 1043: 992 */     return this.handlerState == 3;
/* 1044:     */   }
/* 1045:     */   
/* 1046:     */   public <T> Attribute<T> attr(AttributeKey<T> key)
/* 1047:     */   {
/* 1048: 997 */     return channel().attr(key);
/* 1049:     */   }
/* 1050:     */   
/* 1051:     */   public <T> boolean hasAttr(AttributeKey<T> key)
/* 1052:     */   {
/* 1053:1002 */     return channel().hasAttr(key);
/* 1054:     */   }
/* 1055:     */   
/* 1056:     */   private static void safeExecute(EventExecutor executor, Runnable runnable, ChannelPromise promise, Object msg)
/* 1057:     */   {
/* 1058:     */     try
/* 1059:     */     {
/* 1060:1007 */       executor.execute(runnable);
/* 1061:     */     }
/* 1062:     */     catch (Throwable cause)
/* 1063:     */     {
/* 1064:     */       try
/* 1065:     */       {
/* 1066:1010 */         promise.setFailure(cause);
/* 1067:     */       }
/* 1068:     */       finally
/* 1069:     */       {
/* 1070:1012 */         if (msg != null) {
/* 1071:1013 */           ReferenceCountUtil.release(msg);
/* 1072:     */         }
/* 1073:     */       }
/* 1074:     */     }
/* 1075:     */   }
/* 1076:     */   
/* 1077:     */   public String toHintString()
/* 1078:     */   {
/* 1079:1021 */     return '\'' + this.name + "' will handle the message from this point.";
/* 1080:     */   }
/* 1081:     */   
/* 1082:     */   public String toString()
/* 1083:     */   {
/* 1084:1026 */     return StringUtil.simpleClassName(ChannelHandlerContext.class) + '(' + this.name + ", " + channel() + ')';
/* 1085:     */   }
/* 1086:     */   
/* 1087:     */   static abstract class AbstractWriteTask
/* 1088:     */     implements Runnable
/* 1089:     */   {
/* 1090:1032 */     private static final boolean ESTIMATE_TASK_SIZE_ON_SUBMIT = SystemPropertyUtil.getBoolean("io.netty.transport.estimateSizeOnSubmit", true);
/* 1091:1036 */     private static final int WRITE_TASK_OVERHEAD = SystemPropertyUtil.getInt("io.netty.transport.writeTaskSizeOverhead", 48);
/* 1092:     */     private final Recycler.Handle<AbstractWriteTask> handle;
/* 1093:     */     private AbstractChannelHandlerContext ctx;
/* 1094:     */     private Object msg;
/* 1095:     */     private ChannelPromise promise;
/* 1096:     */     private int size;
/* 1097:     */     
/* 1098:     */     private AbstractWriteTask(Recycler.Handle<? extends AbstractWriteTask> handle)
/* 1099:     */     {
/* 1100:1046 */       this.handle = handle;
/* 1101:     */     }
/* 1102:     */     
/* 1103:     */     protected static void init(AbstractWriteTask task, AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1104:     */     {
/* 1105:1051 */       task.ctx = ctx;
/* 1106:1052 */       task.msg = msg;
/* 1107:1053 */       task.promise = promise;
/* 1108:1055 */       if (ESTIMATE_TASK_SIZE_ON_SUBMIT)
/* 1109:     */       {
/* 1110:1056 */         task.size = (ctx.pipeline.estimatorHandle().size(msg) + WRITE_TASK_OVERHEAD);
/* 1111:1057 */         ctx.pipeline.incrementPendingOutboundBytes(task.size);
/* 1112:     */       }
/* 1113:     */       else
/* 1114:     */       {
/* 1115:1059 */         task.size = 0;
/* 1116:     */       }
/* 1117:     */     }
/* 1118:     */     
/* 1119:     */     public final void run()
/* 1120:     */     {
/* 1121:     */       try
/* 1122:     */       {
/* 1123:1067 */         if (ESTIMATE_TASK_SIZE_ON_SUBMIT) {
/* 1124:1068 */           this.ctx.pipeline.decrementPendingOutboundBytes(this.size);
/* 1125:     */         }
/* 1126:1070 */         write(this.ctx, this.msg, this.promise);
/* 1127:     */         
/* 1128:     */ 
/* 1129:1073 */         this.ctx = null;
/* 1130:1074 */         this.msg = null;
/* 1131:1075 */         this.promise = null;
/* 1132:1076 */         this.handle.recycle(this);
/* 1133:     */       }
/* 1134:     */       finally
/* 1135:     */       {
/* 1136:1073 */         this.ctx = null;
/* 1137:1074 */         this.msg = null;
/* 1138:1075 */         this.promise = null;
/* 1139:1076 */         this.handle.recycle(this);
/* 1140:     */       }
/* 1141:     */     }
/* 1142:     */     
/* 1143:     */     protected void write(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1144:     */     {
/* 1145:1081 */       ctx.invokeWrite(msg, promise);
/* 1146:     */     }
/* 1147:     */   }
/* 1148:     */   
/* 1149:     */   static final class WriteTask
/* 1150:     */     extends AbstractChannelHandlerContext.AbstractWriteTask
/* 1151:     */     implements SingleThreadEventLoop.NonWakeupRunnable
/* 1152:     */   {
/* 1153:1087 */     private static final Recycler<WriteTask> RECYCLER = new Recycler()
/* 1154:     */     {
/* 1155:     */       protected AbstractChannelHandlerContext.WriteTask newObject(Recycler.Handle<AbstractChannelHandlerContext.WriteTask> handle)
/* 1156:     */       {
/* 1157:1090 */         return new AbstractChannelHandlerContext.WriteTask(handle, null);
/* 1158:     */       }
/* 1159:     */     };
/* 1160:     */     
/* 1161:     */     private static WriteTask newInstance(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1162:     */     {
/* 1163:1096 */       WriteTask task = (WriteTask)RECYCLER.get();
/* 1164:1097 */       init(task, ctx, msg, promise);
/* 1165:1098 */       return task;
/* 1166:     */     }
/* 1167:     */     
/* 1168:     */     private WriteTask(Recycler.Handle<WriteTask> handle)
/* 1169:     */     {
/* 1170:1102 */       super(null);
/* 1171:     */     }
/* 1172:     */   }
/* 1173:     */   
/* 1174:     */   static final class WriteAndFlushTask
/* 1175:     */     extends AbstractChannelHandlerContext.AbstractWriteTask
/* 1176:     */   {
/* 1177:1108 */     private static final Recycler<WriteAndFlushTask> RECYCLER = new Recycler()
/* 1178:     */     {
/* 1179:     */       protected AbstractChannelHandlerContext.WriteAndFlushTask newObject(Recycler.Handle<AbstractChannelHandlerContext.WriteAndFlushTask> handle)
/* 1180:     */       {
/* 1181:1111 */         return new AbstractChannelHandlerContext.WriteAndFlushTask(handle, null);
/* 1182:     */       }
/* 1183:     */     };
/* 1184:     */     
/* 1185:     */     private static WriteAndFlushTask newInstance(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1186:     */     {
/* 1187:1117 */       WriteAndFlushTask task = (WriteAndFlushTask)RECYCLER.get();
/* 1188:1118 */       init(task, ctx, msg, promise);
/* 1189:1119 */       return task;
/* 1190:     */     }
/* 1191:     */     
/* 1192:     */     private WriteAndFlushTask(Recycler.Handle<WriteAndFlushTask> handle)
/* 1193:     */     {
/* 1194:1123 */       super(null);
/* 1195:     */     }
/* 1196:     */     
/* 1197:     */     public void write(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 1198:     */     {
/* 1199:1128 */       super.write(ctx, msg, promise);
/* 1200:1129 */       ctx.invokeFlush();
/* 1201:     */     }
/* 1202:     */   }
/* 1203:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AbstractChannelHandlerContext
 * JD-Core Version:    0.7.0.1
 */