/*    1:     */ package io.netty.channel;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBufAllocator;
/*    4:     */ import io.netty.channel.socket.ChannelOutputShutdownEvent;
/*    5:     */ import io.netty.channel.socket.ChannelOutputShutdownException;
/*    6:     */ import io.netty.util.DefaultAttributeMap;
/*    7:     */ import io.netty.util.ReferenceCountUtil;
/*    8:     */ import io.netty.util.internal.PlatformDependent;
/*    9:     */ import io.netty.util.internal.ThrowableUtil;
/*   10:     */ import io.netty.util.internal.logging.InternalLogger;
/*   11:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   12:     */ import java.io.IOException;
/*   13:     */ import java.net.ConnectException;
/*   14:     */ import java.net.InetAddress;
/*   15:     */ import java.net.InetSocketAddress;
/*   16:     */ import java.net.NoRouteToHostException;
/*   17:     */ import java.net.SocketAddress;
/*   18:     */ import java.net.SocketException;
/*   19:     */ import java.nio.channels.ClosedChannelException;
/*   20:     */ import java.nio.channels.NotYetConnectedException;
/*   21:     */ import java.util.concurrent.Executor;
/*   22:     */ import java.util.concurrent.RejectedExecutionException;
/*   23:     */ 
/*   24:     */ public abstract class AbstractChannel
/*   25:     */   extends DefaultAttributeMap
/*   26:     */   implements Channel
/*   27:     */ {
/*   28:  45 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannel.class);
/*   29:  47 */   private static final ClosedChannelException FLUSH0_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "flush0()");
/*   30:  49 */   private static final ClosedChannelException ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "ensureOpen(...)");
/*   31:  51 */   private static final ClosedChannelException CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "close(...)");
/*   32:  53 */   private static final ClosedChannelException WRITE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "write(...)");
/*   33:  55 */   private static final NotYetConnectedException FLUSH0_NOT_YET_CONNECTED_EXCEPTION = (NotYetConnectedException)ThrowableUtil.unknownStackTrace(new NotYetConnectedException(), AbstractUnsafe.class, "flush0()");
/*   34:     */   private final Channel parent;
/*   35:     */   private final ChannelId id;
/*   36:     */   private final Channel.Unsafe unsafe;
/*   37:     */   private final DefaultChannelPipeline pipeline;
/*   38:  62 */   private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(this, false);
/*   39:  63 */   private final CloseFuture closeFuture = new CloseFuture(this);
/*   40:     */   private volatile SocketAddress localAddress;
/*   41:     */   private volatile SocketAddress remoteAddress;
/*   42:     */   private volatile EventLoop eventLoop;
/*   43:     */   private volatile boolean registered;
/*   44:     */   private boolean closeInitiated;
/*   45:     */   private boolean strValActive;
/*   46:     */   private String strVal;
/*   47:     */   
/*   48:     */   protected AbstractChannel(Channel parent)
/*   49:     */   {
/*   50:  82 */     this.parent = parent;
/*   51:  83 */     this.id = newId();
/*   52:  84 */     this.unsafe = newUnsafe();
/*   53:  85 */     this.pipeline = newChannelPipeline();
/*   54:     */   }
/*   55:     */   
/*   56:     */   protected AbstractChannel(Channel parent, ChannelId id)
/*   57:     */   {
/*   58:  95 */     this.parent = parent;
/*   59:  96 */     this.id = id;
/*   60:  97 */     this.unsafe = newUnsafe();
/*   61:  98 */     this.pipeline = newChannelPipeline();
/*   62:     */   }
/*   63:     */   
/*   64:     */   public final ChannelId id()
/*   65:     */   {
/*   66: 103 */     return this.id;
/*   67:     */   }
/*   68:     */   
/*   69:     */   protected ChannelId newId()
/*   70:     */   {
/*   71: 111 */     return DefaultChannelId.newInstance();
/*   72:     */   }
/*   73:     */   
/*   74:     */   protected DefaultChannelPipeline newChannelPipeline()
/*   75:     */   {
/*   76: 118 */     return new DefaultChannelPipeline(this);
/*   77:     */   }
/*   78:     */   
/*   79:     */   public boolean isWritable()
/*   80:     */   {
/*   81: 123 */     ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
/*   82: 124 */     return (buf != null) && (buf.isWritable());
/*   83:     */   }
/*   84:     */   
/*   85:     */   public long bytesBeforeUnwritable()
/*   86:     */   {
/*   87: 129 */     ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
/*   88:     */     
/*   89:     */ 
/*   90: 132 */     return buf != null ? buf.bytesBeforeUnwritable() : 0L;
/*   91:     */   }
/*   92:     */   
/*   93:     */   public long bytesBeforeWritable()
/*   94:     */   {
/*   95: 137 */     ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
/*   96:     */     
/*   97:     */ 
/*   98: 140 */     return buf != null ? buf.bytesBeforeWritable() : 9223372036854775807L;
/*   99:     */   }
/*  100:     */   
/*  101:     */   public Channel parent()
/*  102:     */   {
/*  103: 145 */     return this.parent;
/*  104:     */   }
/*  105:     */   
/*  106:     */   public ChannelPipeline pipeline()
/*  107:     */   {
/*  108: 150 */     return this.pipeline;
/*  109:     */   }
/*  110:     */   
/*  111:     */   public ByteBufAllocator alloc()
/*  112:     */   {
/*  113: 155 */     return config().getAllocator();
/*  114:     */   }
/*  115:     */   
/*  116:     */   public EventLoop eventLoop()
/*  117:     */   {
/*  118: 160 */     EventLoop eventLoop = this.eventLoop;
/*  119: 161 */     if (eventLoop == null) {
/*  120: 162 */       throw new IllegalStateException("channel not registered to an event loop");
/*  121:     */     }
/*  122: 164 */     return eventLoop;
/*  123:     */   }
/*  124:     */   
/*  125:     */   public SocketAddress localAddress()
/*  126:     */   {
/*  127: 169 */     SocketAddress localAddress = this.localAddress;
/*  128: 170 */     if (localAddress == null) {
/*  129:     */       try
/*  130:     */       {
/*  131: 172 */         this.localAddress = (localAddress = unsafe().localAddress());
/*  132:     */       }
/*  133:     */       catch (Throwable t)
/*  134:     */       {
/*  135: 175 */         return null;
/*  136:     */       }
/*  137:     */     }
/*  138: 178 */     return localAddress;
/*  139:     */   }
/*  140:     */   
/*  141:     */   @Deprecated
/*  142:     */   protected void invalidateLocalAddress()
/*  143:     */   {
/*  144: 186 */     this.localAddress = null;
/*  145:     */   }
/*  146:     */   
/*  147:     */   public SocketAddress remoteAddress()
/*  148:     */   {
/*  149: 191 */     SocketAddress remoteAddress = this.remoteAddress;
/*  150: 192 */     if (remoteAddress == null) {
/*  151:     */       try
/*  152:     */       {
/*  153: 194 */         this.remoteAddress = (remoteAddress = unsafe().remoteAddress());
/*  154:     */       }
/*  155:     */       catch (Throwable t)
/*  156:     */       {
/*  157: 197 */         return null;
/*  158:     */       }
/*  159:     */     }
/*  160: 200 */     return remoteAddress;
/*  161:     */   }
/*  162:     */   
/*  163:     */   @Deprecated
/*  164:     */   protected void invalidateRemoteAddress()
/*  165:     */   {
/*  166: 208 */     this.remoteAddress = null;
/*  167:     */   }
/*  168:     */   
/*  169:     */   public boolean isRegistered()
/*  170:     */   {
/*  171: 213 */     return this.registered;
/*  172:     */   }
/*  173:     */   
/*  174:     */   public ChannelFuture bind(SocketAddress localAddress)
/*  175:     */   {
/*  176: 218 */     return this.pipeline.bind(localAddress);
/*  177:     */   }
/*  178:     */   
/*  179:     */   public ChannelFuture connect(SocketAddress remoteAddress)
/*  180:     */   {
/*  181: 223 */     return this.pipeline.connect(remoteAddress);
/*  182:     */   }
/*  183:     */   
/*  184:     */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  185:     */   {
/*  186: 228 */     return this.pipeline.connect(remoteAddress, localAddress);
/*  187:     */   }
/*  188:     */   
/*  189:     */   public ChannelFuture disconnect()
/*  190:     */   {
/*  191: 233 */     return this.pipeline.disconnect();
/*  192:     */   }
/*  193:     */   
/*  194:     */   public ChannelFuture close()
/*  195:     */   {
/*  196: 238 */     return this.pipeline.close();
/*  197:     */   }
/*  198:     */   
/*  199:     */   public ChannelFuture deregister()
/*  200:     */   {
/*  201: 243 */     return this.pipeline.deregister();
/*  202:     */   }
/*  203:     */   
/*  204:     */   public Channel flush()
/*  205:     */   {
/*  206: 248 */     this.pipeline.flush();
/*  207: 249 */     return this;
/*  208:     */   }
/*  209:     */   
/*  210:     */   public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise)
/*  211:     */   {
/*  212: 254 */     return this.pipeline.bind(localAddress, promise);
/*  213:     */   }
/*  214:     */   
/*  215:     */   public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/*  216:     */   {
/*  217: 259 */     return this.pipeline.connect(remoteAddress, promise);
/*  218:     */   }
/*  219:     */   
/*  220:     */   public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  221:     */   {
/*  222: 264 */     return this.pipeline.connect(remoteAddress, localAddress, promise);
/*  223:     */   }
/*  224:     */   
/*  225:     */   public ChannelFuture disconnect(ChannelPromise promise)
/*  226:     */   {
/*  227: 269 */     return this.pipeline.disconnect(promise);
/*  228:     */   }
/*  229:     */   
/*  230:     */   public ChannelFuture close(ChannelPromise promise)
/*  231:     */   {
/*  232: 274 */     return this.pipeline.close(promise);
/*  233:     */   }
/*  234:     */   
/*  235:     */   public ChannelFuture deregister(ChannelPromise promise)
/*  236:     */   {
/*  237: 279 */     return this.pipeline.deregister(promise);
/*  238:     */   }
/*  239:     */   
/*  240:     */   public Channel read()
/*  241:     */   {
/*  242: 284 */     this.pipeline.read();
/*  243: 285 */     return this;
/*  244:     */   }
/*  245:     */   
/*  246:     */   public ChannelFuture write(Object msg)
/*  247:     */   {
/*  248: 290 */     return this.pipeline.write(msg);
/*  249:     */   }
/*  250:     */   
/*  251:     */   public ChannelFuture write(Object msg, ChannelPromise promise)
/*  252:     */   {
/*  253: 295 */     return this.pipeline.write(msg, promise);
/*  254:     */   }
/*  255:     */   
/*  256:     */   public ChannelFuture writeAndFlush(Object msg)
/*  257:     */   {
/*  258: 300 */     return this.pipeline.writeAndFlush(msg);
/*  259:     */   }
/*  260:     */   
/*  261:     */   public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/*  262:     */   {
/*  263: 305 */     return this.pipeline.writeAndFlush(msg, promise);
/*  264:     */   }
/*  265:     */   
/*  266:     */   public ChannelPromise newPromise()
/*  267:     */   {
/*  268: 310 */     return this.pipeline.newPromise();
/*  269:     */   }
/*  270:     */   
/*  271:     */   public ChannelProgressivePromise newProgressivePromise()
/*  272:     */   {
/*  273: 315 */     return this.pipeline.newProgressivePromise();
/*  274:     */   }
/*  275:     */   
/*  276:     */   public ChannelFuture newSucceededFuture()
/*  277:     */   {
/*  278: 320 */     return this.pipeline.newSucceededFuture();
/*  279:     */   }
/*  280:     */   
/*  281:     */   public ChannelFuture newFailedFuture(Throwable cause)
/*  282:     */   {
/*  283: 325 */     return this.pipeline.newFailedFuture(cause);
/*  284:     */   }
/*  285:     */   
/*  286:     */   public ChannelFuture closeFuture()
/*  287:     */   {
/*  288: 330 */     return this.closeFuture;
/*  289:     */   }
/*  290:     */   
/*  291:     */   public Channel.Unsafe unsafe()
/*  292:     */   {
/*  293: 335 */     return this.unsafe;
/*  294:     */   }
/*  295:     */   
/*  296:     */   protected abstract AbstractUnsafe newUnsafe();
/*  297:     */   
/*  298:     */   public final int hashCode()
/*  299:     */   {
/*  300: 348 */     return this.id.hashCode();
/*  301:     */   }
/*  302:     */   
/*  303:     */   public final boolean equals(Object o)
/*  304:     */   {
/*  305: 357 */     return this == o;
/*  306:     */   }
/*  307:     */   
/*  308:     */   public final int compareTo(Channel o)
/*  309:     */   {
/*  310: 362 */     if (this == o) {
/*  311: 363 */       return 0;
/*  312:     */     }
/*  313: 366 */     return id().compareTo(o.id());
/*  314:     */   }
/*  315:     */   
/*  316:     */   public String toString()
/*  317:     */   {
/*  318: 377 */     boolean active = isActive();
/*  319: 378 */     if ((this.strValActive == active) && (this.strVal != null)) {
/*  320: 379 */       return this.strVal;
/*  321:     */     }
/*  322: 382 */     SocketAddress remoteAddr = remoteAddress();
/*  323: 383 */     SocketAddress localAddr = localAddress();
/*  324: 384 */     if (remoteAddr != null)
/*  325:     */     {
/*  326: 393 */       StringBuilder buf = new StringBuilder(96).append("[id: 0x").append(this.id.asShortText()).append(", L:").append(localAddr).append(active ? " - " : " ! ").append("R:").append(remoteAddr).append(']');
/*  327: 394 */       this.strVal = buf.toString();
/*  328:     */     }
/*  329: 395 */     else if (localAddr != null)
/*  330:     */     {
/*  331: 401 */       StringBuilder buf = new StringBuilder(64).append("[id: 0x").append(this.id.asShortText()).append(", L:").append(localAddr).append(']');
/*  332: 402 */       this.strVal = buf.toString();
/*  333:     */     }
/*  334:     */     else
/*  335:     */     {
/*  336: 407 */       StringBuilder buf = new StringBuilder(16).append("[id: 0x").append(this.id.asShortText()).append(']');
/*  337: 408 */       this.strVal = buf.toString();
/*  338:     */     }
/*  339: 411 */     this.strValActive = active;
/*  340: 412 */     return this.strVal;
/*  341:     */   }
/*  342:     */   
/*  343:     */   public final ChannelPromise voidPromise()
/*  344:     */   {
/*  345: 417 */     return this.pipeline.voidPromise();
/*  346:     */   }
/*  347:     */   
/*  348:     */   protected abstract boolean isCompatible(EventLoop paramEventLoop);
/*  349:     */   
/*  350:     */   protected abstract SocketAddress localAddress0();
/*  351:     */   
/*  352:     */   protected abstract SocketAddress remoteAddress0();
/*  353:     */   
/*  354:     */   protected void doRegister()
/*  355:     */     throws Exception
/*  356:     */   {}
/*  357:     */   
/*  358:     */   protected abstract void doBind(SocketAddress paramSocketAddress)
/*  359:     */     throws Exception;
/*  360:     */   
/*  361:     */   protected abstract void doDisconnect()
/*  362:     */     throws Exception;
/*  363:     */   
/*  364:     */   protected abstract void doClose()
/*  365:     */     throws Exception;
/*  366:     */   
/*  367:     */   protected abstract class AbstractUnsafe
/*  368:     */     implements Channel.Unsafe
/*  369:     */   {
/*  370: 425 */     private volatile ChannelOutboundBuffer outboundBuffer = new ChannelOutboundBuffer(AbstractChannel.this);
/*  371:     */     private RecvByteBufAllocator.Handle recvHandle;
/*  372:     */     private boolean inFlush0;
/*  373: 429 */     private boolean neverRegistered = true;
/*  374:     */     
/*  375:     */     protected AbstractUnsafe() {}
/*  376:     */     
/*  377:     */     private void assertEventLoop()
/*  378:     */     {
/*  379: 432 */       assert ((!AbstractChannel.this.registered) || (AbstractChannel.this.eventLoop.inEventLoop()));
/*  380:     */     }
/*  381:     */     
/*  382:     */     public RecvByteBufAllocator.Handle recvBufAllocHandle()
/*  383:     */     {
/*  384: 437 */       if (this.recvHandle == null) {
/*  385: 438 */         this.recvHandle = AbstractChannel.this.config().getRecvByteBufAllocator().newHandle();
/*  386:     */       }
/*  387: 440 */       return this.recvHandle;
/*  388:     */     }
/*  389:     */     
/*  390:     */     public final ChannelOutboundBuffer outboundBuffer()
/*  391:     */     {
/*  392: 445 */       return this.outboundBuffer;
/*  393:     */     }
/*  394:     */     
/*  395:     */     public final SocketAddress localAddress()
/*  396:     */     {
/*  397: 450 */       return AbstractChannel.this.localAddress0();
/*  398:     */     }
/*  399:     */     
/*  400:     */     public final SocketAddress remoteAddress()
/*  401:     */     {
/*  402: 455 */       return AbstractChannel.this.remoteAddress0();
/*  403:     */     }
/*  404:     */     
/*  405:     */     public final void register(EventLoop eventLoop, final ChannelPromise promise)
/*  406:     */     {
/*  407: 460 */       if (eventLoop == null) {
/*  408: 461 */         throw new NullPointerException("eventLoop");
/*  409:     */       }
/*  410: 463 */       if (AbstractChannel.this.isRegistered())
/*  411:     */       {
/*  412: 464 */         promise.setFailure(new IllegalStateException("registered to an event loop already"));
/*  413: 465 */         return;
/*  414:     */       }
/*  415: 467 */       if (!AbstractChannel.this.isCompatible(eventLoop))
/*  416:     */       {
/*  417: 468 */         promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop
/*  418: 469 */           .getClass().getName()));
/*  419: 470 */         return;
/*  420:     */       }
/*  421: 473 */       AbstractChannel.this.eventLoop = eventLoop;
/*  422: 475 */       if (eventLoop.inEventLoop()) {
/*  423: 476 */         register0(promise);
/*  424:     */       } else {
/*  425:     */         try
/*  426:     */         {
/*  427: 479 */           eventLoop.execute(new Runnable()
/*  428:     */           {
/*  429:     */             public void run()
/*  430:     */             {
/*  431: 482 */               AbstractChannel.AbstractUnsafe.this.register0(promise);
/*  432:     */             }
/*  433:     */           });
/*  434:     */         }
/*  435:     */         catch (Throwable t)
/*  436:     */         {
/*  437: 486 */           AbstractChannel.logger.warn("Force-closing a channel whose registration task was not accepted by an event loop: {}", AbstractChannel.this, t);
/*  438:     */           
/*  439:     */ 
/*  440: 489 */           closeForcibly();
/*  441: 490 */           AbstractChannel.this.closeFuture.setClosed();
/*  442: 491 */           safeSetFailure(promise, t);
/*  443:     */         }
/*  444:     */       }
/*  445:     */     }
/*  446:     */     
/*  447:     */     private void register0(ChannelPromise promise)
/*  448:     */     {
/*  449:     */       try
/*  450:     */       {
/*  451: 500 */         if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/*  452: 501 */           return;
/*  453:     */         }
/*  454: 503 */         boolean firstRegistration = this.neverRegistered;
/*  455: 504 */         AbstractChannel.this.doRegister();
/*  456: 505 */         this.neverRegistered = false;
/*  457: 506 */         AbstractChannel.this.registered = true;
/*  458:     */         
/*  459:     */ 
/*  460:     */ 
/*  461: 510 */         AbstractChannel.this.pipeline.invokeHandlerAddedIfNeeded();
/*  462:     */         
/*  463: 512 */         safeSetSuccess(promise);
/*  464: 513 */         AbstractChannel.this.pipeline.fireChannelRegistered();
/*  465: 516 */         if (AbstractChannel.this.isActive()) {
/*  466: 517 */           if (firstRegistration) {
/*  467: 518 */             AbstractChannel.this.pipeline.fireChannelActive();
/*  468: 519 */           } else if (AbstractChannel.this.config().isAutoRead()) {
/*  469: 524 */             beginRead();
/*  470:     */           }
/*  471:     */         }
/*  472:     */       }
/*  473:     */       catch (Throwable t)
/*  474:     */       {
/*  475: 529 */         closeForcibly();
/*  476: 530 */         AbstractChannel.this.closeFuture.setClosed();
/*  477: 531 */         safeSetFailure(promise, t);
/*  478:     */       }
/*  479:     */     }
/*  480:     */     
/*  481:     */     public final void bind(SocketAddress localAddress, ChannelPromise promise)
/*  482:     */     {
/*  483: 537 */       assertEventLoop();
/*  484: 539 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/*  485: 540 */         return;
/*  486:     */       }
/*  487: 544 */       if ((Boolean.TRUE.equals(AbstractChannel.this.config().getOption(ChannelOption.SO_BROADCAST))) && ((localAddress instanceof InetSocketAddress))) {
/*  488: 546 */         if ((!((InetSocketAddress)localAddress).getAddress().isAnyLocalAddress()) && 
/*  489: 547 */           (!PlatformDependent.isWindows()) && (!PlatformDependent.maybeSuperUser())) {
/*  490: 550 */           AbstractChannel.logger.warn("A non-root user can't receive a broadcast packet if the socket is not bound to a wildcard address; binding to a non-wildcard address (" + localAddress + ") anyway as requested.");
/*  491:     */         }
/*  492:     */       }
/*  493: 556 */       boolean wasActive = AbstractChannel.this.isActive();
/*  494:     */       try
/*  495:     */       {
/*  496: 558 */         AbstractChannel.this.doBind(localAddress);
/*  497:     */       }
/*  498:     */       catch (Throwable t)
/*  499:     */       {
/*  500: 560 */         safeSetFailure(promise, t);
/*  501: 561 */         closeIfClosed();
/*  502: 562 */         return;
/*  503:     */       }
/*  504: 565 */       if ((!wasActive) && (AbstractChannel.this.isActive())) {
/*  505: 566 */         invokeLater(new Runnable()
/*  506:     */         {
/*  507:     */           public void run()
/*  508:     */           {
/*  509: 569 */             AbstractChannel.this.pipeline.fireChannelActive();
/*  510:     */           }
/*  511:     */         });
/*  512:     */       }
/*  513: 574 */       safeSetSuccess(promise);
/*  514:     */     }
/*  515:     */     
/*  516:     */     public final void disconnect(ChannelPromise promise)
/*  517:     */     {
/*  518: 579 */       assertEventLoop();
/*  519: 581 */       if (!promise.setUncancellable()) {
/*  520: 582 */         return;
/*  521:     */       }
/*  522: 585 */       boolean wasActive = AbstractChannel.this.isActive();
/*  523:     */       try
/*  524:     */       {
/*  525: 587 */         AbstractChannel.this.doDisconnect();
/*  526:     */       }
/*  527:     */       catch (Throwable t)
/*  528:     */       {
/*  529: 589 */         safeSetFailure(promise, t);
/*  530: 590 */         closeIfClosed();
/*  531: 591 */         return;
/*  532:     */       }
/*  533: 594 */       if ((wasActive) && (!AbstractChannel.this.isActive())) {
/*  534: 595 */         invokeLater(new Runnable()
/*  535:     */         {
/*  536:     */           public void run()
/*  537:     */           {
/*  538: 598 */             AbstractChannel.this.pipeline.fireChannelInactive();
/*  539:     */           }
/*  540:     */         });
/*  541:     */       }
/*  542: 603 */       safeSetSuccess(promise);
/*  543: 604 */       closeIfClosed();
/*  544:     */     }
/*  545:     */     
/*  546:     */     public final void close(ChannelPromise promise)
/*  547:     */     {
/*  548: 609 */       assertEventLoop();
/*  549:     */       
/*  550: 611 */       close(promise, AbstractChannel.CLOSE_CLOSED_CHANNEL_EXCEPTION, AbstractChannel.CLOSE_CLOSED_CHANNEL_EXCEPTION, false);
/*  551:     */     }
/*  552:     */     
/*  553:     */     public final void shutdownOutput(ChannelPromise promise)
/*  554:     */     {
/*  555: 620 */       assertEventLoop();
/*  556: 621 */       shutdownOutput(promise, null);
/*  557:     */     }
/*  558:     */     
/*  559:     */     private void shutdownOutput(final ChannelPromise promise, Throwable cause)
/*  560:     */     {
/*  561: 630 */       if (!promise.setUncancellable()) {
/*  562: 631 */         return;
/*  563:     */       }
/*  564: 634 */       final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/*  565: 635 */       if (outboundBuffer == null)
/*  566:     */       {
/*  567: 636 */         promise.setFailure(AbstractChannel.CLOSE_CLOSED_CHANNEL_EXCEPTION);
/*  568: 637 */         return;
/*  569:     */       }
/*  570: 639 */       this.outboundBuffer = null;
/*  571:     */       
/*  572: 641 */       final Throwable shutdownCause = cause == null ? new ChannelOutputShutdownException("Channel output shutdown") : new ChannelOutputShutdownException("Channel output shutdown", cause);
/*  573:     */       
/*  574:     */ 
/*  575: 644 */       Executor closeExecutor = prepareToClose();
/*  576: 645 */       if (closeExecutor != null) {
/*  577: 646 */         closeExecutor.execute(new Runnable()
/*  578:     */         {
/*  579:     */           public void run()
/*  580:     */           {
/*  581:     */             try
/*  582:     */             {
/*  583: 651 */               AbstractChannel.this.doShutdownOutput();
/*  584: 652 */               promise.setSuccess();
/*  585:     */             }
/*  586:     */             catch (Throwable err)
/*  587:     */             {
/*  588: 654 */               promise.setFailure(err);
/*  589:     */             }
/*  590:     */             finally
/*  591:     */             {
/*  592: 657 */               AbstractChannel.this.eventLoop().execute(new Runnable()
/*  593:     */               {
/*  594:     */                 public void run()
/*  595:     */                 {
/*  596: 660 */                   AbstractChannel.AbstractUnsafe.this.closeOutboundBufferForShutdown(AbstractChannel.this.pipeline, AbstractChannel.AbstractUnsafe.4.this.val$outboundBuffer, AbstractChannel.AbstractUnsafe.4.this.val$shutdownCause);
/*  597:     */                 }
/*  598:     */               });
/*  599:     */             }
/*  600:     */           }
/*  601:     */         });
/*  602:     */       } else {
/*  603:     */         try
/*  604:     */         {
/*  605: 669 */           AbstractChannel.this.doShutdownOutput();
/*  606: 670 */           promise.setSuccess();
/*  607:     */         }
/*  608:     */         catch (Throwable err)
/*  609:     */         {
/*  610: 672 */           promise.setFailure(err);
/*  611:     */         }
/*  612:     */         finally
/*  613:     */         {
/*  614: 674 */           closeOutboundBufferForShutdown(AbstractChannel.this.pipeline, outboundBuffer, shutdownCause);
/*  615:     */         }
/*  616:     */       }
/*  617:     */     }
/*  618:     */     
/*  619:     */     private void closeOutboundBufferForShutdown(ChannelPipeline pipeline, ChannelOutboundBuffer buffer, Throwable cause)
/*  620:     */     {
/*  621: 681 */       buffer.failFlushed(cause, false);
/*  622: 682 */       buffer.close(cause, true);
/*  623: 683 */       pipeline.fireUserEventTriggered(ChannelOutputShutdownEvent.INSTANCE);
/*  624:     */     }
/*  625:     */     
/*  626:     */     private void close(final ChannelPromise promise, final Throwable cause, final ClosedChannelException closeCause, final boolean notify)
/*  627:     */     {
/*  628: 688 */       if (!promise.setUncancellable()) {
/*  629: 689 */         return;
/*  630:     */       }
/*  631: 692 */       if (AbstractChannel.this.closeInitiated)
/*  632:     */       {
/*  633: 693 */         if (AbstractChannel.this.closeFuture.isDone()) {
/*  634: 695 */           safeSetSuccess(promise);
/*  635: 696 */         } else if (!(promise instanceof VoidChannelPromise)) {
/*  636: 698 */           AbstractChannel.this.closeFuture.addListener(new ChannelFutureListener()
/*  637:     */           {
/*  638:     */             public void operationComplete(ChannelFuture future)
/*  639:     */               throws Exception
/*  640:     */             {
/*  641: 701 */               promise.setSuccess();
/*  642:     */             }
/*  643:     */           });
/*  644:     */         }
/*  645: 705 */         return;
/*  646:     */       }
/*  647: 708 */       AbstractChannel.this.closeInitiated = true;
/*  648:     */       
/*  649: 710 */       final boolean wasActive = AbstractChannel.this.isActive();
/*  650: 711 */       final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/*  651: 712 */       this.outboundBuffer = null;
/*  652: 713 */       Executor closeExecutor = prepareToClose();
/*  653: 714 */       if (closeExecutor != null)
/*  654:     */       {
/*  655: 715 */         closeExecutor.execute(new Runnable()
/*  656:     */         {
/*  657:     */           public void run()
/*  658:     */           {
/*  659:     */             try
/*  660:     */             {
/*  661: 720 */               AbstractChannel.AbstractUnsafe.this.doClose0(promise);
/*  662:     */               
/*  663:     */ 
/*  664: 723 */               AbstractChannel.AbstractUnsafe.this.invokeLater(new Runnable()
/*  665:     */               {
/*  666:     */                 public void run()
/*  667:     */                 {
/*  668: 726 */                   if (AbstractChannel.AbstractUnsafe.6.this.val$outboundBuffer != null)
/*  669:     */                   {
/*  670: 728 */                     AbstractChannel.AbstractUnsafe.6.this.val$outboundBuffer.failFlushed(AbstractChannel.AbstractUnsafe.6.this.val$cause, AbstractChannel.AbstractUnsafe.6.this.val$notify);
/*  671: 729 */                     AbstractChannel.AbstractUnsafe.6.this.val$outboundBuffer.close(AbstractChannel.AbstractUnsafe.6.this.val$closeCause);
/*  672:     */                   }
/*  673: 731 */                   AbstractChannel.AbstractUnsafe.this.fireChannelInactiveAndDeregister(AbstractChannel.AbstractUnsafe.6.this.val$wasActive);
/*  674:     */                 }
/*  675:     */               });
/*  676:     */             }
/*  677:     */             finally
/*  678:     */             {
/*  679: 723 */               AbstractChannel.AbstractUnsafe.this.invokeLater(new Runnable()
/*  680:     */               {
/*  681:     */                 public void run()
/*  682:     */                 {
/*  683: 726 */                   if (AbstractChannel.AbstractUnsafe.6.this.val$outboundBuffer != null)
/*  684:     */                   {
/*  685: 728 */                     AbstractChannel.AbstractUnsafe.6.this.val$outboundBuffer.failFlushed(AbstractChannel.AbstractUnsafe.6.this.val$cause, AbstractChannel.AbstractUnsafe.6.this.val$notify);
/*  686: 729 */                     AbstractChannel.AbstractUnsafe.6.this.val$outboundBuffer.close(AbstractChannel.AbstractUnsafe.6.this.val$closeCause);
/*  687:     */                   }
/*  688: 731 */                   AbstractChannel.AbstractUnsafe.this.fireChannelInactiveAndDeregister(AbstractChannel.AbstractUnsafe.6.this.val$wasActive);
/*  689:     */                 }
/*  690:     */               });
/*  691:     */             }
/*  692:     */           }
/*  693:     */         });
/*  694:     */       }
/*  695:     */       else
/*  696:     */       {
/*  697:     */         try
/*  698:     */         {
/*  699: 740 */           doClose0(promise);
/*  700:     */         }
/*  701:     */         finally
/*  702:     */         {
/*  703: 742 */           if (outboundBuffer != null)
/*  704:     */           {
/*  705: 744 */             outboundBuffer.failFlushed(cause, notify);
/*  706: 745 */             outboundBuffer.close(closeCause);
/*  707:     */           }
/*  708:     */         }
/*  709: 748 */         if (this.inFlush0) {
/*  710: 749 */           invokeLater(new Runnable()
/*  711:     */           {
/*  712:     */             public void run()
/*  713:     */             {
/*  714: 752 */               AbstractChannel.AbstractUnsafe.this.fireChannelInactiveAndDeregister(wasActive);
/*  715:     */             }
/*  716:     */           });
/*  717:     */         } else {
/*  718: 756 */           fireChannelInactiveAndDeregister(wasActive);
/*  719:     */         }
/*  720:     */       }
/*  721:     */     }
/*  722:     */     
/*  723:     */     private void doClose0(ChannelPromise promise)
/*  724:     */     {
/*  725:     */       try
/*  726:     */       {
/*  727: 763 */         AbstractChannel.this.doClose();
/*  728: 764 */         AbstractChannel.this.closeFuture.setClosed();
/*  729: 765 */         safeSetSuccess(promise);
/*  730:     */       }
/*  731:     */       catch (Throwable t)
/*  732:     */       {
/*  733: 767 */         AbstractChannel.this.closeFuture.setClosed();
/*  734: 768 */         safeSetFailure(promise, t);
/*  735:     */       }
/*  736:     */     }
/*  737:     */     
/*  738:     */     private void fireChannelInactiveAndDeregister(boolean wasActive)
/*  739:     */     {
/*  740: 773 */       deregister(voidPromise(), (wasActive) && (!AbstractChannel.this.isActive()));
/*  741:     */     }
/*  742:     */     
/*  743:     */     public final void closeForcibly()
/*  744:     */     {
/*  745: 778 */       assertEventLoop();
/*  746:     */       try
/*  747:     */       {
/*  748: 781 */         AbstractChannel.this.doClose();
/*  749:     */       }
/*  750:     */       catch (Exception e)
/*  751:     */       {
/*  752: 783 */         AbstractChannel.logger.warn("Failed to close a channel.", e);
/*  753:     */       }
/*  754:     */     }
/*  755:     */     
/*  756:     */     public final void deregister(ChannelPromise promise)
/*  757:     */     {
/*  758: 789 */       assertEventLoop();
/*  759:     */       
/*  760: 791 */       deregister(promise, false);
/*  761:     */     }
/*  762:     */     
/*  763:     */     private void deregister(final ChannelPromise promise, final boolean fireChannelInactive)
/*  764:     */     {
/*  765: 795 */       if (!promise.setUncancellable()) {
/*  766: 796 */         return;
/*  767:     */       }
/*  768: 799 */       if (!AbstractChannel.this.registered)
/*  769:     */       {
/*  770: 800 */         safeSetSuccess(promise);
/*  771: 801 */         return;
/*  772:     */       }
/*  773: 813 */       invokeLater(new Runnable()
/*  774:     */       {
/*  775:     */         public void run()
/*  776:     */         {
/*  777:     */           try
/*  778:     */           {
/*  779: 817 */             AbstractChannel.this.doDeregister();
/*  780:     */           }
/*  781:     */           catch (Throwable t)
/*  782:     */           {
/*  783: 819 */             AbstractChannel.logger.warn("Unexpected exception occurred while deregistering a channel.", t);
/*  784:     */           }
/*  785:     */           finally
/*  786:     */           {
/*  787: 821 */             if (fireChannelInactive) {
/*  788: 822 */               AbstractChannel.this.pipeline.fireChannelInactive();
/*  789:     */             }
/*  790: 828 */             if (AbstractChannel.this.registered)
/*  791:     */             {
/*  792: 829 */               AbstractChannel.this.registered = false;
/*  793: 830 */               AbstractChannel.this.pipeline.fireChannelUnregistered();
/*  794:     */             }
/*  795: 832 */             AbstractChannel.AbstractUnsafe.this.safeSetSuccess(promise);
/*  796:     */           }
/*  797:     */         }
/*  798:     */       });
/*  799:     */     }
/*  800:     */     
/*  801:     */     public final void beginRead()
/*  802:     */     {
/*  803: 840 */       assertEventLoop();
/*  804: 842 */       if (!AbstractChannel.this.isActive()) {
/*  805: 843 */         return;
/*  806:     */       }
/*  807:     */       try
/*  808:     */       {
/*  809: 847 */         AbstractChannel.this.doBeginRead();
/*  810:     */       }
/*  811:     */       catch (Exception e)
/*  812:     */       {
/*  813: 849 */         invokeLater(new Runnable()
/*  814:     */         {
/*  815:     */           public void run()
/*  816:     */           {
/*  817: 852 */             AbstractChannel.this.pipeline.fireExceptionCaught(e);
/*  818:     */           }
/*  819: 854 */         });
/*  820: 855 */         close(voidPromise());
/*  821:     */       }
/*  822:     */     }
/*  823:     */     
/*  824:     */     public final void write(Object msg, ChannelPromise promise)
/*  825:     */     {
/*  826: 861 */       assertEventLoop();
/*  827:     */       
/*  828: 863 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/*  829: 864 */       if (outboundBuffer == null)
/*  830:     */       {
/*  831: 869 */         safeSetFailure(promise, AbstractChannel.WRITE_CLOSED_CHANNEL_EXCEPTION);
/*  832:     */         
/*  833: 871 */         ReferenceCountUtil.release(msg);
/*  834: 872 */         return;
/*  835:     */       }
/*  836:     */       try
/*  837:     */       {
/*  838: 877 */         msg = AbstractChannel.this.filterOutboundMessage(msg);
/*  839: 878 */         int size = AbstractChannel.this.pipeline.estimatorHandle().size(msg);
/*  840: 879 */         if (size < 0) {
/*  841: 880 */           size = 0;
/*  842:     */         }
/*  843:     */       }
/*  844:     */       catch (Throwable t)
/*  845:     */       {
/*  846: 883 */         safeSetFailure(promise, t);
/*  847: 884 */         ReferenceCountUtil.release(msg); return;
/*  848:     */       }
/*  849:     */       int size;
/*  850: 888 */       outboundBuffer.addMessage(msg, size, promise);
/*  851:     */     }
/*  852:     */     
/*  853:     */     public final void flush()
/*  854:     */     {
/*  855: 893 */       assertEventLoop();
/*  856:     */       
/*  857: 895 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/*  858: 896 */       if (outboundBuffer == null) {
/*  859: 897 */         return;
/*  860:     */       }
/*  861: 900 */       outboundBuffer.addFlush();
/*  862: 901 */       flush0();
/*  863:     */     }
/*  864:     */     
/*  865:     */     protected void flush0()
/*  866:     */     {
/*  867: 906 */       if (this.inFlush0) {
/*  868: 908 */         return;
/*  869:     */       }
/*  870: 911 */       ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
/*  871: 912 */       if ((outboundBuffer == null) || (outboundBuffer.isEmpty())) {
/*  872: 913 */         return;
/*  873:     */       }
/*  874: 916 */       this.inFlush0 = true;
/*  875: 919 */       if (!AbstractChannel.this.isActive()) {
/*  876:     */         try
/*  877:     */         {
/*  878: 921 */           if (AbstractChannel.this.isOpen()) {
/*  879: 922 */             outboundBuffer.failFlushed(AbstractChannel.FLUSH0_NOT_YET_CONNECTED_EXCEPTION, true);
/*  880:     */           } else {
/*  881: 925 */             outboundBuffer.failFlushed(AbstractChannel.FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
/*  882:     */           }
/*  883: 928 */           this.inFlush0 = false;
/*  884:     */         }
/*  885:     */         finally
/*  886:     */         {
/*  887: 928 */           this.inFlush0 = false;
/*  888:     */         }
/*  889:     */       }
/*  890:     */       try
/*  891:     */       {
/*  892: 934 */         AbstractChannel.this.doWrite(outboundBuffer);
/*  893:     */       }
/*  894:     */       catch (Throwable t)
/*  895:     */       {
/*  896: 936 */         if (((t instanceof IOException)) && (AbstractChannel.this.config().isAutoClose())) {
/*  897: 945 */           close(voidPromise(), t, AbstractChannel.FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
/*  898:     */         } else {
/*  899:     */           try
/*  900:     */           {
/*  901: 948 */             shutdownOutput(voidPromise(), t);
/*  902:     */           }
/*  903:     */           catch (Throwable t2)
/*  904:     */           {
/*  905: 950 */             close(voidPromise(), t2, AbstractChannel.FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
/*  906:     */           }
/*  907:     */         }
/*  908:     */       }
/*  909:     */       finally
/*  910:     */       {
/*  911: 954 */         this.inFlush0 = false;
/*  912:     */       }
/*  913:     */     }
/*  914:     */     
/*  915:     */     public final ChannelPromise voidPromise()
/*  916:     */     {
/*  917: 960 */       assertEventLoop();
/*  918:     */       
/*  919: 962 */       return AbstractChannel.this.unsafeVoidPromise;
/*  920:     */     }
/*  921:     */     
/*  922:     */     protected final boolean ensureOpen(ChannelPromise promise)
/*  923:     */     {
/*  924: 966 */       if (AbstractChannel.this.isOpen()) {
/*  925: 967 */         return true;
/*  926:     */       }
/*  927: 970 */       safeSetFailure(promise, AbstractChannel.ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION);
/*  928: 971 */       return false;
/*  929:     */     }
/*  930:     */     
/*  931:     */     protected final void safeSetSuccess(ChannelPromise promise)
/*  932:     */     {
/*  933: 978 */       if ((!(promise instanceof VoidChannelPromise)) && (!promise.trySuccess())) {
/*  934: 979 */         AbstractChannel.logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
/*  935:     */       }
/*  936:     */     }
/*  937:     */     
/*  938:     */     protected final void safeSetFailure(ChannelPromise promise, Throwable cause)
/*  939:     */     {
/*  940: 987 */       if ((!(promise instanceof VoidChannelPromise)) && (!promise.tryFailure(cause))) {
/*  941: 988 */         AbstractChannel.logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
/*  942:     */       }
/*  943:     */     }
/*  944:     */     
/*  945:     */     protected final void closeIfClosed()
/*  946:     */     {
/*  947: 993 */       if (AbstractChannel.this.isOpen()) {
/*  948: 994 */         return;
/*  949:     */       }
/*  950: 996 */       close(voidPromise());
/*  951:     */     }
/*  952:     */     
/*  953:     */     private void invokeLater(Runnable task)
/*  954:     */     {
/*  955:     */       try
/*  956:     */       {
/*  957:1012 */         AbstractChannel.this.eventLoop().execute(task);
/*  958:     */       }
/*  959:     */       catch (RejectedExecutionException e)
/*  960:     */       {
/*  961:1014 */         AbstractChannel.logger.warn("Can't invoke task later as EventLoop rejected it", e);
/*  962:     */       }
/*  963:     */     }
/*  964:     */     
/*  965:     */     protected final Throwable annotateConnectException(Throwable cause, SocketAddress remoteAddress)
/*  966:     */     {
/*  967:1022 */       if ((cause instanceof ConnectException)) {
/*  968:1023 */         return new AbstractChannel.AnnotatedConnectException((ConnectException)cause, remoteAddress);
/*  969:     */       }
/*  970:1025 */       if ((cause instanceof NoRouteToHostException)) {
/*  971:1026 */         return new AbstractChannel.AnnotatedNoRouteToHostException((NoRouteToHostException)cause, remoteAddress);
/*  972:     */       }
/*  973:1028 */       if ((cause instanceof SocketException)) {
/*  974:1029 */         return new AbstractChannel.AnnotatedSocketException((SocketException)cause, remoteAddress);
/*  975:     */       }
/*  976:1032 */       return cause;
/*  977:     */     }
/*  978:     */     
/*  979:     */     protected Executor prepareToClose()
/*  980:     */     {
/*  981:1042 */       return null;
/*  982:     */     }
/*  983:     */   }
/*  984:     */   
/*  985:     */   protected void doShutdownOutput()
/*  986:     */     throws Exception
/*  987:     */   {
/*  988:1091 */     doClose();
/*  989:     */   }
/*  990:     */   
/*  991:     */   protected void doDeregister()
/*  992:     */     throws Exception
/*  993:     */   {}
/*  994:     */   
/*  995:     */   protected abstract void doBeginRead()
/*  996:     */     throws Exception;
/*  997:     */   
/*  998:     */   protected abstract void doWrite(ChannelOutboundBuffer paramChannelOutboundBuffer)
/*  999:     */     throws Exception;
/* 1000:     */   
/* 1001:     */   protected Object filterOutboundMessage(Object msg)
/* 1002:     */     throws Exception
/* 1003:     */   {
/* 1004:1118 */     return msg;
/* 1005:     */   }
/* 1006:     */   
/* 1007:     */   static final class CloseFuture
/* 1008:     */     extends DefaultChannelPromise
/* 1009:     */   {
/* 1010:     */     CloseFuture(AbstractChannel ch)
/* 1011:     */     {
/* 1012:1124 */       super();
/* 1013:     */     }
/* 1014:     */     
/* 1015:     */     public ChannelPromise setSuccess()
/* 1016:     */     {
/* 1017:1129 */       throw new IllegalStateException();
/* 1018:     */     }
/* 1019:     */     
/* 1020:     */     public ChannelPromise setFailure(Throwable cause)
/* 1021:     */     {
/* 1022:1134 */       throw new IllegalStateException();
/* 1023:     */     }
/* 1024:     */     
/* 1025:     */     public boolean trySuccess()
/* 1026:     */     {
/* 1027:1139 */       throw new IllegalStateException();
/* 1028:     */     }
/* 1029:     */     
/* 1030:     */     public boolean tryFailure(Throwable cause)
/* 1031:     */     {
/* 1032:1144 */       throw new IllegalStateException();
/* 1033:     */     }
/* 1034:     */     
/* 1035:     */     boolean setClosed()
/* 1036:     */     {
/* 1037:1148 */       return super.trySuccess();
/* 1038:     */     }
/* 1039:     */   }
/* 1040:     */   
/* 1041:     */   private static final class AnnotatedConnectException
/* 1042:     */     extends ConnectException
/* 1043:     */   {
/* 1044:     */     private static final long serialVersionUID = 3901958112696433556L;
/* 1045:     */     
/* 1046:     */     AnnotatedConnectException(ConnectException exception, SocketAddress remoteAddress)
/* 1047:     */     {
/* 1048:1157 */       super();
/* 1049:1158 */       initCause(exception);
/* 1050:1159 */       setStackTrace(exception.getStackTrace());
/* 1051:     */     }
/* 1052:     */     
/* 1053:     */     public Throwable fillInStackTrace()
/* 1054:     */     {
/* 1055:1164 */       return this;
/* 1056:     */     }
/* 1057:     */   }
/* 1058:     */   
/* 1059:     */   private static final class AnnotatedNoRouteToHostException
/* 1060:     */     extends NoRouteToHostException
/* 1061:     */   {
/* 1062:     */     private static final long serialVersionUID = -6801433937592080623L;
/* 1063:     */     
/* 1064:     */     AnnotatedNoRouteToHostException(NoRouteToHostException exception, SocketAddress remoteAddress)
/* 1065:     */     {
/* 1066:1173 */       super();
/* 1067:1174 */       initCause(exception);
/* 1068:1175 */       setStackTrace(exception.getStackTrace());
/* 1069:     */     }
/* 1070:     */     
/* 1071:     */     public Throwable fillInStackTrace()
/* 1072:     */     {
/* 1073:1180 */       return this;
/* 1074:     */     }
/* 1075:     */   }
/* 1076:     */   
/* 1077:     */   private static final class AnnotatedSocketException
/* 1078:     */     extends SocketException
/* 1079:     */   {
/* 1080:     */     private static final long serialVersionUID = 3896743275010454039L;
/* 1081:     */     
/* 1082:     */     AnnotatedSocketException(SocketException exception, SocketAddress remoteAddress)
/* 1083:     */     {
/* 1084:1189 */       super();
/* 1085:1190 */       initCause(exception);
/* 1086:1191 */       setStackTrace(exception.getStackTrace());
/* 1087:     */     }
/* 1088:     */     
/* 1089:     */     public Throwable fillInStackTrace()
/* 1090:     */     {
/* 1091:1196 */       return this;
/* 1092:     */     }
/* 1093:     */   }
/* 1094:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AbstractChannel
 * JD-Core Version:    0.7.0.1
 */