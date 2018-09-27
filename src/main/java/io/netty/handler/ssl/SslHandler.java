/*    1:     */ package io.netty.handler.ssl;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.buffer.ByteBufUtil;
/*    6:     */ import io.netty.buffer.CompositeByteBuf;
/*    7:     */ import io.netty.buffer.Unpooled;
/*    8:     */ import io.netty.channel.AbstractCoalescingBufferQueue;
/*    9:     */ import io.netty.channel.Channel;
/*   10:     */ import io.netty.channel.ChannelConfig;
/*   11:     */ import io.netty.channel.ChannelException;
/*   12:     */ import io.netty.channel.ChannelFuture;
/*   13:     */ import io.netty.channel.ChannelFutureListener;
/*   14:     */ import io.netty.channel.ChannelHandlerContext;
/*   15:     */ import io.netty.channel.ChannelOutboundHandler;
/*   16:     */ import io.netty.channel.ChannelPromise;
/*   17:     */ import io.netty.channel.ChannelPromiseNotifier;
/*   18:     */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   19:     */ import io.netty.handler.codec.ByteToMessageDecoder.Cumulator;
/*   20:     */ import io.netty.handler.codec.UnsupportedMessageTypeException;
/*   21:     */ import io.netty.util.ReferenceCountUtil;
/*   22:     */ import io.netty.util.ReferenceCounted;
/*   23:     */ import io.netty.util.concurrent.DefaultPromise;
/*   24:     */ import io.netty.util.concurrent.EventExecutor;
/*   25:     */ import io.netty.util.concurrent.Future;
/*   26:     */ import io.netty.util.concurrent.FutureListener;
/*   27:     */ import io.netty.util.concurrent.ImmediateExecutor;
/*   28:     */ import io.netty.util.concurrent.Promise;
/*   29:     */ import io.netty.util.internal.PlatformDependent;
/*   30:     */ import io.netty.util.internal.ThrowableUtil;
/*   31:     */ import io.netty.util.internal.logging.InternalLogger;
/*   32:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   33:     */ import java.io.IOException;
/*   34:     */ import java.net.SocketAddress;
/*   35:     */ import java.nio.ByteBuffer;
/*   36:     */ import java.nio.channels.ClosedChannelException;
/*   37:     */ import java.nio.channels.DatagramChannel;
/*   38:     */ import java.nio.channels.SocketChannel;
/*   39:     */ import java.util.ArrayList;
/*   40:     */ import java.util.List;
/*   41:     */ import java.util.concurrent.CountDownLatch;
/*   42:     */ import java.util.concurrent.Executor;
/*   43:     */ import java.util.concurrent.ScheduledFuture;
/*   44:     */ import java.util.concurrent.TimeUnit;
/*   45:     */ import java.util.regex.Matcher;
/*   46:     */ import java.util.regex.Pattern;
/*   47:     */ import javax.net.ssl.SSLEngine;
/*   48:     */ import javax.net.ssl.SSLEngineResult;
/*   49:     */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*   50:     */ import javax.net.ssl.SSLEngineResult.Status;
/*   51:     */ import javax.net.ssl.SSLException;
/*   52:     */ import javax.net.ssl.SSLSession;
/*   53:     */ 
/*   54:     */ public class SslHandler
/*   55:     */   extends ByteToMessageDecoder
/*   56:     */   implements ChannelOutboundHandler
/*   57:     */ {
/*   58: 168 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslHandler.class);
/*   59: 170 */   private static final Pattern IGNORABLE_CLASS_IN_STACK = Pattern.compile("^.*(?:Socket|Datagram|Sctp|Udt)Channel.*$");
/*   60: 172 */   private static final Pattern IGNORABLE_ERROR_MESSAGE = Pattern.compile("^.*(?:connection.*(?:reset|closed|abort|broken)|broken.*pipe).*$", 2);
/*   61: 180 */   private static final SSLException SSLENGINE_CLOSED = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("SSLEngine closed already"), SslHandler.class, "wrap(...)");
/*   62: 182 */   private static final SSLException HANDSHAKE_TIMED_OUT = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("handshake timed out"), SslHandler.class, "handshake(...)");
/*   63: 184 */   private static final ClosedChannelException CHANNEL_CLOSED = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), SslHandler.class, "channelInactive(...)");
/*   64:     */   private static final int MAX_PLAINTEXT_LENGTH = 16384;
/*   65:     */   private volatile ChannelHandlerContext ctx;
/*   66:     */   private final SSLEngine engine;
/*   67:     */   private final SslEngineType engineType;
/*   68:     */   private final Executor delegatedTaskExecutor;
/*   69:     */   private final boolean jdkCompatibilityMode;
/*   70:     */   
/*   71:     */   private static abstract enum SslEngineType
/*   72:     */   {
/*   73: 194 */     TCNATIVE(true, ByteToMessageDecoder.COMPOSITE_CUMULATOR),  CONSCRYPT(true, ByteToMessageDecoder.COMPOSITE_CUMULATOR),  JDK(false, ByteToMessageDecoder.MERGE_CUMULATOR);
/*   74:     */     
/*   75:     */     final boolean wantsDirectBuffer;
/*   76:     */     final ByteToMessageDecoder.Cumulator cumulator;
/*   77:     */     
/*   78:     */     static SslEngineType forEngine(SSLEngine engine)
/*   79:     */     {
/*   80: 315 */       return (engine instanceof ConscryptAlpnSslEngine) ? CONSCRYPT : (engine instanceof ReferenceCountedOpenSslEngine) ? TCNATIVE : JDK;
/*   81:     */     }
/*   82:     */     
/*   83:     */     private SslEngineType(boolean wantsDirectBuffer, ByteToMessageDecoder.Cumulator cumulator)
/*   84:     */     {
/*   85: 320 */       this.wantsDirectBuffer = wantsDirectBuffer;
/*   86: 321 */       this.cumulator = cumulator;
/*   87:     */     }
/*   88:     */     
/*   89:     */     int getPacketBufferSize(SslHandler handler)
/*   90:     */     {
/*   91: 325 */       return handler.engine.getSession().getPacketBufferSize();
/*   92:     */     }
/*   93:     */     
/*   94:     */     abstract SSLEngineResult unwrap(SslHandler paramSslHandler, ByteBuf paramByteBuf1, int paramInt1, int paramInt2, ByteBuf paramByteBuf2)
/*   95:     */       throws SSLException;
/*   96:     */     
/*   97:     */     abstract int calculateWrapBufferCapacity(SslHandler paramSslHandler, int paramInt1, int paramInt2);
/*   98:     */     
/*   99:     */     abstract int calculatePendingData(SslHandler paramSslHandler, int paramInt);
/*  100:     */     
/*  101:     */     abstract boolean jdkCompatibilityMode(SSLEngine paramSSLEngine);
/*  102:     */   }
/*  103:     */   
/*  104: 368 */   private final ByteBuffer[] singleBuffer = new ByteBuffer[1];
/*  105:     */   private final boolean startTls;
/*  106:     */   private boolean sentFirstMessage;
/*  107:     */   private boolean flushedBeforeHandshake;
/*  108:     */   private boolean readDuringHandshake;
/*  109:     */   private boolean handshakeStarted;
/*  110:     */   private SslHandlerCoalescingBufferQueue pendingUnencryptedWrites;
/*  111: 376 */   private Promise<Channel> handshakePromise = new LazyChannelPromise(null);
/*  112: 377 */   private final LazyChannelPromise sslClosePromise = new LazyChannelPromise(null);
/*  113:     */   private boolean needsFlush;
/*  114:     */   private boolean outboundClosed;
/*  115:     */   private int packetLength;
/*  116:     */   private boolean firedChannelRead;
/*  117: 395 */   private volatile long handshakeTimeoutMillis = 10000L;
/*  118: 396 */   private volatile long closeNotifyFlushTimeoutMillis = 3000L;
/*  119:     */   private volatile long closeNotifyReadTimeoutMillis;
/*  120: 398 */   volatile int wrapDataSize = 16384;
/*  121:     */   
/*  122:     */   public SslHandler(SSLEngine engine)
/*  123:     */   {
/*  124: 406 */     this(engine, false);
/*  125:     */   }
/*  126:     */   
/*  127:     */   public SslHandler(SSLEngine engine, boolean startTls)
/*  128:     */   {
/*  129: 418 */     this(engine, startTls, ImmediateExecutor.INSTANCE);
/*  130:     */   }
/*  131:     */   
/*  132:     */   @Deprecated
/*  133:     */   public SslHandler(SSLEngine engine, Executor delegatedTaskExecutor)
/*  134:     */   {
/*  135: 426 */     this(engine, false, delegatedTaskExecutor);
/*  136:     */   }
/*  137:     */   
/*  138:     */   @Deprecated
/*  139:     */   public SslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor)
/*  140:     */   {
/*  141: 434 */     if (engine == null) {
/*  142: 435 */       throw new NullPointerException("engine");
/*  143:     */     }
/*  144: 437 */     if (delegatedTaskExecutor == null) {
/*  145: 438 */       throw new NullPointerException("delegatedTaskExecutor");
/*  146:     */     }
/*  147: 440 */     this.engine = engine;
/*  148: 441 */     this.engineType = SslEngineType.forEngine(engine);
/*  149: 442 */     this.delegatedTaskExecutor = delegatedTaskExecutor;
/*  150: 443 */     this.startTls = startTls;
/*  151: 444 */     this.jdkCompatibilityMode = this.engineType.jdkCompatibilityMode(engine);
/*  152: 445 */     setCumulator(this.engineType.cumulator);
/*  153:     */   }
/*  154:     */   
/*  155:     */   public long getHandshakeTimeoutMillis()
/*  156:     */   {
/*  157: 449 */     return this.handshakeTimeoutMillis;
/*  158:     */   }
/*  159:     */   
/*  160:     */   public void setHandshakeTimeout(long handshakeTimeout, TimeUnit unit)
/*  161:     */   {
/*  162: 453 */     if (unit == null) {
/*  163: 454 */       throw new NullPointerException("unit");
/*  164:     */     }
/*  165: 457 */     setHandshakeTimeoutMillis(unit.toMillis(handshakeTimeout));
/*  166:     */   }
/*  167:     */   
/*  168:     */   public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis)
/*  169:     */   {
/*  170: 461 */     if (handshakeTimeoutMillis < 0L) {
/*  171: 462 */       throw new IllegalArgumentException("handshakeTimeoutMillis: " + handshakeTimeoutMillis + " (expected: >= 0)");
/*  172:     */     }
/*  173: 465 */     this.handshakeTimeoutMillis = handshakeTimeoutMillis;
/*  174:     */   }
/*  175:     */   
/*  176:     */   public final void setWrapDataSize(int wrapDataSize)
/*  177:     */   {
/*  178: 490 */     this.wrapDataSize = wrapDataSize;
/*  179:     */   }
/*  180:     */   
/*  181:     */   @Deprecated
/*  182:     */   public long getCloseNotifyTimeoutMillis()
/*  183:     */   {
/*  184: 498 */     return getCloseNotifyFlushTimeoutMillis();
/*  185:     */   }
/*  186:     */   
/*  187:     */   @Deprecated
/*  188:     */   public void setCloseNotifyTimeout(long closeNotifyTimeout, TimeUnit unit)
/*  189:     */   {
/*  190: 506 */     setCloseNotifyFlushTimeout(closeNotifyTimeout, unit);
/*  191:     */   }
/*  192:     */   
/*  193:     */   @Deprecated
/*  194:     */   public void setCloseNotifyTimeoutMillis(long closeNotifyFlushTimeoutMillis)
/*  195:     */   {
/*  196: 514 */     setCloseNotifyFlushTimeoutMillis(closeNotifyFlushTimeoutMillis);
/*  197:     */   }
/*  198:     */   
/*  199:     */   public final long getCloseNotifyFlushTimeoutMillis()
/*  200:     */   {
/*  201: 523 */     return this.closeNotifyFlushTimeoutMillis;
/*  202:     */   }
/*  203:     */   
/*  204:     */   public final void setCloseNotifyFlushTimeout(long closeNotifyFlushTimeout, TimeUnit unit)
/*  205:     */   {
/*  206: 532 */     setCloseNotifyFlushTimeoutMillis(unit.toMillis(closeNotifyFlushTimeout));
/*  207:     */   }
/*  208:     */   
/*  209:     */   public final void setCloseNotifyFlushTimeoutMillis(long closeNotifyFlushTimeoutMillis)
/*  210:     */   {
/*  211: 539 */     if (closeNotifyFlushTimeoutMillis < 0L) {
/*  212: 540 */       throw new IllegalArgumentException("closeNotifyFlushTimeoutMillis: " + closeNotifyFlushTimeoutMillis + " (expected: >= 0)");
/*  213:     */     }
/*  214: 543 */     this.closeNotifyFlushTimeoutMillis = closeNotifyFlushTimeoutMillis;
/*  215:     */   }
/*  216:     */   
/*  217:     */   public final long getCloseNotifyReadTimeoutMillis()
/*  218:     */   {
/*  219: 552 */     return this.closeNotifyReadTimeoutMillis;
/*  220:     */   }
/*  221:     */   
/*  222:     */   public final void setCloseNotifyReadTimeout(long closeNotifyReadTimeout, TimeUnit unit)
/*  223:     */   {
/*  224: 561 */     setCloseNotifyReadTimeoutMillis(unit.toMillis(closeNotifyReadTimeout));
/*  225:     */   }
/*  226:     */   
/*  227:     */   public final void setCloseNotifyReadTimeoutMillis(long closeNotifyReadTimeoutMillis)
/*  228:     */   {
/*  229: 568 */     if (closeNotifyReadTimeoutMillis < 0L) {
/*  230: 569 */       throw new IllegalArgumentException("closeNotifyReadTimeoutMillis: " + closeNotifyReadTimeoutMillis + " (expected: >= 0)");
/*  231:     */     }
/*  232: 572 */     this.closeNotifyReadTimeoutMillis = closeNotifyReadTimeoutMillis;
/*  233:     */   }
/*  234:     */   
/*  235:     */   public SSLEngine engine()
/*  236:     */   {
/*  237: 579 */     return this.engine;
/*  238:     */   }
/*  239:     */   
/*  240:     */   public String applicationProtocol()
/*  241:     */   {
/*  242: 588 */     SSLEngine engine = engine();
/*  243: 589 */     if (!(engine instanceof ApplicationProtocolAccessor)) {
/*  244: 590 */       return null;
/*  245:     */     }
/*  246: 593 */     return ((ApplicationProtocolAccessor)engine).getNegotiatedApplicationProtocol();
/*  247:     */   }
/*  248:     */   
/*  249:     */   public Future<Channel> handshakeFuture()
/*  250:     */   {
/*  251: 603 */     return this.handshakePromise;
/*  252:     */   }
/*  253:     */   
/*  254:     */   @Deprecated
/*  255:     */   public ChannelFuture close()
/*  256:     */   {
/*  257: 614 */     return close(this.ctx.newPromise());
/*  258:     */   }
/*  259:     */   
/*  260:     */   @Deprecated
/*  261:     */   public ChannelFuture close(final ChannelPromise promise)
/*  262:     */   {
/*  263: 624 */     final ChannelHandlerContext ctx = this.ctx;
/*  264: 625 */     ctx.executor().execute(new Runnable()
/*  265:     */     {
/*  266:     */       public void run()
/*  267:     */       {
/*  268: 628 */         SslHandler.this.outboundClosed = true;
/*  269: 629 */         SslHandler.this.engine.closeOutbound();
/*  270:     */         try
/*  271:     */         {
/*  272: 631 */           SslHandler.this.flush(ctx, promise);
/*  273:     */         }
/*  274:     */         catch (Exception e)
/*  275:     */         {
/*  276: 633 */           if (!promise.tryFailure(e)) {
/*  277: 634 */             SslHandler.logger.warn("{} flush() raised a masked exception.", ctx.channel(), e);
/*  278:     */           }
/*  279:     */         }
/*  280:     */       }
/*  281: 639 */     });
/*  282: 640 */     return promise;
/*  283:     */   }
/*  284:     */   
/*  285:     */   public Future<Channel> sslCloseFuture()
/*  286:     */   {
/*  287: 651 */     return this.sslClosePromise;
/*  288:     */   }
/*  289:     */   
/*  290:     */   public void handlerRemoved0(ChannelHandlerContext ctx)
/*  291:     */     throws Exception
/*  292:     */   {
/*  293: 656 */     if (!this.pendingUnencryptedWrites.isEmpty()) {
/*  294: 658 */       this.pendingUnencryptedWrites.releaseAndFailAll(ctx, new ChannelException("Pending write on removal of SslHandler"));
/*  295:     */     }
/*  296: 661 */     this.pendingUnencryptedWrites = null;
/*  297: 662 */     if ((this.engine instanceof ReferenceCounted)) {
/*  298: 663 */       ((ReferenceCounted)this.engine).release();
/*  299:     */     }
/*  300:     */   }
/*  301:     */   
/*  302:     */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  303:     */     throws Exception
/*  304:     */   {
/*  305: 669 */     ctx.bind(localAddress, promise);
/*  306:     */   }
/*  307:     */   
/*  308:     */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  309:     */     throws Exception
/*  310:     */   {
/*  311: 675 */     ctx.connect(remoteAddress, localAddress, promise);
/*  312:     */   }
/*  313:     */   
/*  314:     */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/*  315:     */     throws Exception
/*  316:     */   {
/*  317: 680 */     ctx.deregister(promise);
/*  318:     */   }
/*  319:     */   
/*  320:     */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  321:     */     throws Exception
/*  322:     */   {
/*  323: 686 */     closeOutboundAndChannel(ctx, promise, true);
/*  324:     */   }
/*  325:     */   
/*  326:     */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  327:     */     throws Exception
/*  328:     */   {
/*  329: 692 */     closeOutboundAndChannel(ctx, promise, false);
/*  330:     */   }
/*  331:     */   
/*  332:     */   public void read(ChannelHandlerContext ctx)
/*  333:     */     throws Exception
/*  334:     */   {
/*  335: 697 */     if (!this.handshakePromise.isDone()) {
/*  336: 698 */       this.readDuringHandshake = true;
/*  337:     */     }
/*  338: 701 */     ctx.read();
/*  339:     */   }
/*  340:     */   
/*  341:     */   private static IllegalStateException newPendingWritesNullException()
/*  342:     */   {
/*  343: 705 */     return new IllegalStateException("pendingUnencryptedWrites is null, handlerRemoved0 called?");
/*  344:     */   }
/*  345:     */   
/*  346:     */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  347:     */     throws Exception
/*  348:     */   {
/*  349: 710 */     if (!(msg instanceof ByteBuf))
/*  350:     */     {
/*  351: 711 */       UnsupportedMessageTypeException exception = new UnsupportedMessageTypeException(msg, new Class[] { ByteBuf.class });
/*  352: 712 */       ReferenceCountUtil.safeRelease(msg);
/*  353: 713 */       promise.setFailure(exception);
/*  354:     */     }
/*  355: 714 */     else if (this.pendingUnencryptedWrites == null)
/*  356:     */     {
/*  357: 715 */       ReferenceCountUtil.safeRelease(msg);
/*  358: 716 */       promise.setFailure(newPendingWritesNullException());
/*  359:     */     }
/*  360:     */     else
/*  361:     */     {
/*  362: 718 */       this.pendingUnencryptedWrites.add((ByteBuf)msg, promise);
/*  363:     */     }
/*  364:     */   }
/*  365:     */   
/*  366:     */   public void flush(ChannelHandlerContext ctx)
/*  367:     */     throws Exception
/*  368:     */   {
/*  369: 726 */     if ((this.startTls) && (!this.sentFirstMessage))
/*  370:     */     {
/*  371: 727 */       this.sentFirstMessage = true;
/*  372: 728 */       this.pendingUnencryptedWrites.writeAndRemoveAll(ctx);
/*  373: 729 */       forceFlush(ctx);
/*  374: 730 */       return;
/*  375:     */     }
/*  376:     */     try
/*  377:     */     {
/*  378: 734 */       wrapAndFlush(ctx);
/*  379:     */     }
/*  380:     */     catch (Throwable cause)
/*  381:     */     {
/*  382: 736 */       setHandshakeFailure(ctx, cause);
/*  383: 737 */       PlatformDependent.throwException(cause);
/*  384:     */     }
/*  385:     */   }
/*  386:     */   
/*  387:     */   private void wrapAndFlush(ChannelHandlerContext ctx)
/*  388:     */     throws SSLException
/*  389:     */   {
/*  390: 742 */     if (this.pendingUnencryptedWrites.isEmpty()) {
/*  391: 747 */       this.pendingUnencryptedWrites.add(Unpooled.EMPTY_BUFFER, ctx.newPromise());
/*  392:     */     }
/*  393: 749 */     if (!this.handshakePromise.isDone()) {
/*  394: 750 */       this.flushedBeforeHandshake = true;
/*  395:     */     }
/*  396:     */     try
/*  397:     */     {
/*  398: 753 */       wrap(ctx, false);
/*  399:     */       
/*  400:     */ 
/*  401:     */ 
/*  402: 757 */       forceFlush(ctx);
/*  403:     */     }
/*  404:     */     finally
/*  405:     */     {
/*  406: 757 */       forceFlush(ctx);
/*  407:     */     }
/*  408:     */   }
/*  409:     */   
/*  410:     */   private void wrap(ChannelHandlerContext ctx, boolean inUnwrap)
/*  411:     */     throws SSLException
/*  412:     */   {
/*  413: 763 */     ByteBuf out = null;
/*  414: 764 */     ChannelPromise promise = null;
/*  415: 765 */     ByteBufAllocator alloc = ctx.alloc();
/*  416: 766 */     boolean needUnwrap = false;
/*  417: 767 */     ByteBuf buf = null;
/*  418:     */     try
/*  419:     */     {
/*  420: 769 */       int wrapDataSize = this.wrapDataSize;
/*  421: 772 */       while (!ctx.isRemoved())
/*  422:     */       {
/*  423: 773 */         promise = ctx.newPromise();
/*  424:     */         
/*  425:     */ 
/*  426: 776 */         buf = wrapDataSize > 0 ? this.pendingUnencryptedWrites.remove(alloc, wrapDataSize, promise) : this.pendingUnencryptedWrites.removeFirst(promise);
/*  427: 777 */         if (buf == null) {
/*  428:     */           break;
/*  429:     */         }
/*  430: 781 */         if (out == null) {
/*  431: 782 */           out = allocateOutNetBuf(ctx, buf.readableBytes(), buf.nioBufferCount());
/*  432:     */         }
/*  433: 785 */         SSLEngineResult result = wrap(alloc, this.engine, buf, out);
/*  434: 787 */         if (result.getStatus() == SSLEngineResult.Status.CLOSED)
/*  435:     */         {
/*  436: 788 */           buf.release();
/*  437: 789 */           buf = null;
/*  438: 790 */           promise.tryFailure(SSLENGINE_CLOSED);
/*  439: 791 */           promise = null;
/*  440:     */           
/*  441:     */ 
/*  442: 794 */           this.pendingUnencryptedWrites.releaseAndFailAll(ctx, SSLENGINE_CLOSED);
/*  443: 795 */           return;
/*  444:     */         }
/*  445: 797 */         if (buf.isReadable())
/*  446:     */         {
/*  447: 798 */           this.pendingUnencryptedWrites.addFirst(buf, promise);
/*  448:     */           
/*  449:     */ 
/*  450: 801 */           promise = null;
/*  451:     */         }
/*  452:     */         else
/*  453:     */         {
/*  454: 803 */           buf.release();
/*  455:     */         }
/*  456: 805 */         buf = null;
/*  457: 807 */         switch (9.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[result.getHandshakeStatus().ordinal()])
/*  458:     */         {
/*  459:     */         case 1: 
/*  460: 809 */           runDelegatedTasks();
/*  461: 810 */           break;
/*  462:     */         case 2: 
/*  463: 812 */           setHandshakeSuccess();
/*  464:     */         case 3: 
/*  465: 815 */           setHandshakeSuccessIfStillHandshaking();
/*  466:     */         case 4: 
/*  467: 818 */           finishWrap(ctx, out, promise, inUnwrap, false);
/*  468: 819 */           promise = null;
/*  469: 820 */           out = null;
/*  470: 821 */           break;
/*  471:     */         case 5: 
/*  472: 823 */           needUnwrap = true;
/*  473: 824 */           return;
/*  474:     */         default: 
/*  475: 827 */           throw new IllegalStateException("Unknown handshake status: " + result.getHandshakeStatus());
/*  476:     */         }
/*  477:     */       }
/*  478:     */     }
/*  479:     */     finally
/*  480:     */     {
/*  481: 833 */       if (buf != null) {
/*  482: 834 */         buf.release();
/*  483:     */       }
/*  484: 836 */       finishWrap(ctx, out, promise, inUnwrap, needUnwrap);
/*  485:     */     }
/*  486:     */   }
/*  487:     */   
/*  488:     */   private void finishWrap(ChannelHandlerContext ctx, ByteBuf out, ChannelPromise promise, boolean inUnwrap, boolean needUnwrap)
/*  489:     */   {
/*  490: 842 */     if (out == null)
/*  491:     */     {
/*  492: 843 */       out = Unpooled.EMPTY_BUFFER;
/*  493:     */     }
/*  494: 844 */     else if (!out.isReadable())
/*  495:     */     {
/*  496: 845 */       out.release();
/*  497: 846 */       out = Unpooled.EMPTY_BUFFER;
/*  498:     */     }
/*  499: 849 */     if (promise != null) {
/*  500: 850 */       ctx.write(out, promise);
/*  501:     */     } else {
/*  502: 852 */       ctx.write(out);
/*  503:     */     }
/*  504: 855 */     if (inUnwrap) {
/*  505: 856 */       this.needsFlush = true;
/*  506:     */     }
/*  507: 859 */     if (needUnwrap) {
/*  508: 862 */       readIfNeeded(ctx);
/*  509:     */     }
/*  510:     */   }
/*  511:     */   
/*  512:     */   private boolean wrapNonAppData(ChannelHandlerContext ctx, boolean inUnwrap)
/*  513:     */     throws SSLException
/*  514:     */   {
/*  515: 873 */     ByteBuf out = null;
/*  516: 874 */     ByteBufAllocator alloc = ctx.alloc();
/*  517:     */     try
/*  518:     */     {
/*  519: 878 */       while (!ctx.isRemoved())
/*  520:     */       {
/*  521: 879 */         if (out == null) {
/*  522: 883 */           out = allocateOutNetBuf(ctx, 2048, 1);
/*  523:     */         }
/*  524: 885 */         SSLEngineResult result = wrap(alloc, this.engine, Unpooled.EMPTY_BUFFER, out);
/*  525: 887 */         if (result.bytesProduced() > 0)
/*  526:     */         {
/*  527: 888 */           ctx.write(out);
/*  528: 889 */           if (inUnwrap) {
/*  529: 890 */             this.needsFlush = true;
/*  530:     */           }
/*  531: 892 */           out = null;
/*  532:     */         }
/*  533:     */         boolean bool;
/*  534: 895 */         switch (9.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[result.getHandshakeStatus().ordinal()])
/*  535:     */         {
/*  536:     */         case 2: 
/*  537: 897 */           setHandshakeSuccess();
/*  538: 898 */           return false;
/*  539:     */         case 1: 
/*  540: 900 */           runDelegatedTasks();
/*  541: 901 */           break;
/*  542:     */         case 5: 
/*  543: 903 */           if (inUnwrap) {
/*  544: 907 */             return false;
/*  545:     */           }
/*  546: 910 */           unwrapNonAppData(ctx);
/*  547: 911 */           break;
/*  548:     */         case 4: 
/*  549:     */           break;
/*  550:     */         case 3: 
/*  551: 915 */           setHandshakeSuccessIfStillHandshaking();
/*  552: 918 */           if (!inUnwrap) {
/*  553: 919 */             unwrapNonAppData(ctx);
/*  554:     */           }
/*  555: 921 */           return true;
/*  556:     */         default: 
/*  557: 923 */           throw new IllegalStateException("Unknown handshake status: " + result.getHandshakeStatus());
/*  558:     */         }
/*  559: 926 */         if (result.bytesProduced() == 0) {
/*  560:     */           break;
/*  561:     */         }
/*  562: 932 */         if ((result.bytesConsumed() == 0) && (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)) {
/*  563:     */           break;
/*  564:     */         }
/*  565:     */       }
/*  566:     */     }
/*  567:     */     finally
/*  568:     */     {
/*  569: 937 */       if (out != null) {
/*  570: 938 */         out.release();
/*  571:     */       }
/*  572:     */     }
/*  573: 941 */     return false;
/*  574:     */   }
/*  575:     */   
/*  576:     */   /* Error */
/*  577:     */   private SSLEngineResult wrap(ByteBufAllocator alloc, SSLEngine engine, ByteBuf in, ByteBuf out)
/*  578:     */     throws SSLException
/*  579:     */   {
/*  580:     */     // Byte code:
/*  581:     */     //   0: aconst_null
/*  582:     */     //   1: astore 5
/*  583:     */     //   3: aload_3
/*  584:     */     //   4: invokevirtual 139	io/netty/buffer/ByteBuf:readerIndex	()I
/*  585:     */     //   7: istore 6
/*  586:     */     //   9: aload_3
/*  587:     */     //   10: invokevirtual 111	io/netty/buffer/ByteBuf:readableBytes	()I
/*  588:     */     //   13: istore 7
/*  589:     */     //   15: aload_3
/*  590:     */     //   16: invokevirtual 140	io/netty/buffer/ByteBuf:isDirect	()Z
/*  591:     */     //   19: ifne +13 -> 32
/*  592:     */     //   22: aload_0
/*  593:     */     //   23: getfield 37	io/netty/handler/ssl/SslHandler:engineType	Lio/netty/handler/ssl/SslHandler$SslEngineType;
/*  594:     */     //   26: getfield 141	io/netty/handler/ssl/SslHandler$SslEngineType:wantsDirectBuffer	Z
/*  595:     */     //   29: ifne +48 -> 77
/*  596:     */     //   32: aload_3
/*  597:     */     //   33: instanceof 142
/*  598:     */     //   36: ifne +32 -> 68
/*  599:     */     //   39: aload_3
/*  600:     */     //   40: invokevirtual 112	io/netty/buffer/ByteBuf:nioBufferCount	()I
/*  601:     */     //   43: iconst_1
/*  602:     */     //   44: if_icmpne +24 -> 68
/*  603:     */     //   47: aload_0
/*  604:     */     //   48: getfield 14	io/netty/handler/ssl/SslHandler:singleBuffer	[Ljava/nio/ByteBuffer;
/*  605:     */     //   51: astore 8
/*  606:     */     //   53: aload 8
/*  607:     */     //   55: iconst_0
/*  608:     */     //   56: aload_3
/*  609:     */     //   57: iload 6
/*  610:     */     //   59: iload 7
/*  611:     */     //   61: invokevirtual 143	io/netty/buffer/ByteBuf:internalNioBuffer	(II)Ljava/nio/ByteBuffer;
/*  612:     */     //   64: aastore
/*  613:     */     //   65: goto +55 -> 120
/*  614:     */     //   68: aload_3
/*  615:     */     //   69: invokevirtual 144	io/netty/buffer/ByteBuf:nioBuffers	()[Ljava/nio/ByteBuffer;
/*  616:     */     //   72: astore 8
/*  617:     */     //   74: goto +46 -> 120
/*  618:     */     //   77: aload_1
/*  619:     */     //   78: iload 7
/*  620:     */     //   80: invokeinterface 145 2 0
/*  621:     */     //   85: astore 5
/*  622:     */     //   87: aload 5
/*  623:     */     //   89: aload_3
/*  624:     */     //   90: iload 6
/*  625:     */     //   92: iload 7
/*  626:     */     //   94: invokevirtual 146	io/netty/buffer/ByteBuf:writeBytes	(Lio/netty/buffer/ByteBuf;II)Lio/netty/buffer/ByteBuf;
/*  627:     */     //   97: pop
/*  628:     */     //   98: aload_0
/*  629:     */     //   99: getfield 14	io/netty/handler/ssl/SslHandler:singleBuffer	[Ljava/nio/ByteBuffer;
/*  630:     */     //   102: astore 8
/*  631:     */     //   104: aload 8
/*  632:     */     //   106: iconst_0
/*  633:     */     //   107: aload 5
/*  634:     */     //   109: aload 5
/*  635:     */     //   111: invokevirtual 139	io/netty/buffer/ByteBuf:readerIndex	()I
/*  636:     */     //   114: iload 7
/*  637:     */     //   116: invokevirtual 143	io/netty/buffer/ByteBuf:internalNioBuffer	(II)Ljava/nio/ByteBuffer;
/*  638:     */     //   119: aastore
/*  639:     */     //   120: aload 4
/*  640:     */     //   122: aload 4
/*  641:     */     //   124: invokevirtual 147	io/netty/buffer/ByteBuf:writerIndex	()I
/*  642:     */     //   127: aload 4
/*  643:     */     //   129: invokevirtual 148	io/netty/buffer/ByteBuf:writableBytes	()I
/*  644:     */     //   132: invokevirtual 149	io/netty/buffer/ByteBuf:nioBuffer	(II)Ljava/nio/ByteBuffer;
/*  645:     */     //   135: astore 9
/*  646:     */     //   137: aload_2
/*  647:     */     //   138: aload 8
/*  648:     */     //   140: aload 9
/*  649:     */     //   142: invokevirtual 150	javax/net/ssl/SSLEngine:wrap	([Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)Ljavax/net/ssl/SSLEngineResult;
/*  650:     */     //   145: astore 10
/*  651:     */     //   147: aload_3
/*  652:     */     //   148: aload 10
/*  653:     */     //   150: invokevirtual 137	javax/net/ssl/SSLEngineResult:bytesConsumed	()I
/*  654:     */     //   153: invokevirtual 151	io/netty/buffer/ByteBuf:skipBytes	(I)Lio/netty/buffer/ByteBuf;
/*  655:     */     //   156: pop
/*  656:     */     //   157: aload 4
/*  657:     */     //   159: aload 4
/*  658:     */     //   161: invokevirtual 147	io/netty/buffer/ByteBuf:writerIndex	()I
/*  659:     */     //   164: aload 10
/*  660:     */     //   166: invokevirtual 135	javax/net/ssl/SSLEngineResult:bytesProduced	()I
/*  661:     */     //   169: iadd
/*  662:     */     //   170: invokevirtual 152	io/netty/buffer/ByteBuf:writerIndex	(I)Lio/netty/buffer/ByteBuf;
/*  663:     */     //   173: pop
/*  664:     */     //   174: getstatic 153	io/netty/handler/ssl/SslHandler$9:$SwitchMap$javax$net$ssl$SSLEngineResult$Status	[I
/*  665:     */     //   177: aload 10
/*  666:     */     //   179: invokevirtual 115	javax/net/ssl/SSLEngineResult:getStatus	()Ljavax/net/ssl/SSLEngineResult$Status;
/*  667:     */     //   182: invokevirtual 154	javax/net/ssl/SSLEngineResult$Status:ordinal	()I
/*  668:     */     //   185: iaload
/*  669:     */     //   186: lookupswitch	default:+36->222, 1:+18->204
/*  670:     */     //   205: iconst_1
/*  671:     */     //   206: aload_2
/*  672:     */     //   207: invokevirtual 155	javax/net/ssl/SSLEngine:getSession	()Ljavax/net/ssl/SSLSession;
/*  673:     */     //   210: invokeinterface 156 1 0
/*  674:     */     //   215: invokevirtual 157	io/netty/buffer/ByteBuf:ensureWritable	(I)Lio/netty/buffer/ByteBuf;
/*  675:     */     //   218: pop
/*  676:     */     //   219: goto +28 -> 247
/*  677:     */     //   222: aload 10
/*  678:     */     //   224: astore 11
/*  679:     */     //   226: aload_0
/*  680:     */     //   227: getfield 14	io/netty/handler/ssl/SslHandler:singleBuffer	[Ljava/nio/ByteBuffer;
/*  681:     */     //   230: iconst_0
/*  682:     */     //   231: aconst_null
/*  683:     */     //   232: aastore
/*  684:     */     //   233: aload 5
/*  685:     */     //   235: ifnull +9 -> 244
/*  686:     */     //   238: aload 5
/*  687:     */     //   240: invokevirtual 117	io/netty/buffer/ByteBuf:release	()Z
/*  688:     */     //   243: pop
/*  689:     */     //   244: aload 11
/*  690:     */     //   246: areturn
/*  691:     */     //   247: goto -127 -> 120
/*  692:     */     //   250: astore 12
/*  693:     */     //   252: aload_0
/*  694:     */     //   253: getfield 14	io/netty/handler/ssl/SslHandler:singleBuffer	[Ljava/nio/ByteBuffer;
/*  695:     */     //   256: iconst_0
/*  696:     */     //   257: aconst_null
/*  697:     */     //   258: aastore
/*  698:     */     //   259: aload 5
/*  699:     */     //   261: ifnull +9 -> 270
/*  700:     */     //   264: aload 5
/*  701:     */     //   266: invokevirtual 117	io/netty/buffer/ByteBuf:release	()Z
/*  702:     */     //   269: pop
/*  703:     */     //   270: aload 12
/*  704:     */     //   272: athrow
/*  705:     */     // Line number table:
/*  706:     */     //   Java source line #946	-> byte code offset #0
/*  707:     */     //   Java source line #948	-> byte code offset #3
/*  708:     */     //   Java source line #949	-> byte code offset #9
/*  709:     */     //   Java source line #954	-> byte code offset #15
/*  710:     */     //   Java source line #959	-> byte code offset #32
/*  711:     */     //   Java source line #960	-> byte code offset #47
/*  712:     */     //   Java source line #963	-> byte code offset #53
/*  713:     */     //   Java source line #965	-> byte code offset #68
/*  714:     */     //   Java source line #971	-> byte code offset #77
/*  715:     */     //   Java source line #972	-> byte code offset #87
/*  716:     */     //   Java source line #973	-> byte code offset #98
/*  717:     */     //   Java source line #974	-> byte code offset #104
/*  718:     */     //   Java source line #978	-> byte code offset #120
/*  719:     */     //   Java source line #979	-> byte code offset #137
/*  720:     */     //   Java source line #980	-> byte code offset #147
/*  721:     */     //   Java source line #981	-> byte code offset #157
/*  722:     */     //   Java source line #983	-> byte code offset #174
/*  723:     */     //   Java source line #985	-> byte code offset #204
/*  724:     */     //   Java source line #986	-> byte code offset #219
/*  725:     */     //   Java source line #988	-> byte code offset #222
/*  726:     */     //   Java source line #993	-> byte code offset #226
/*  727:     */     //   Java source line #995	-> byte code offset #233
/*  728:     */     //   Java source line #996	-> byte code offset #238
/*  729:     */     //   Java source line #988	-> byte code offset #244
/*  730:     */     //   Java source line #990	-> byte code offset #247
/*  731:     */     //   Java source line #993	-> byte code offset #250
/*  732:     */     //   Java source line #995	-> byte code offset #259
/*  733:     */     //   Java source line #996	-> byte code offset #264
/*  734:     */     // Local variable table:
/*  735:     */     //   start	length	slot	name	signature
/*  736:     */     //   0	273	0	this	SslHandler
/*  737:     */     //   0	273	1	alloc	ByteBufAllocator
/*  738:     */     //   0	273	2	engine	SSLEngine
/*  739:     */     //   0	273	3	in	ByteBuf
/*  740:     */     //   0	273	4	out	ByteBuf
/*  741:     */     //   1	264	5	newDirectIn	ByteBuf
/*  742:     */     //   7	84	6	readerIndex	int
/*  743:     */     //   13	102	7	readableBytes	int
/*  744:     */     //   51	3	8	in0	ByteBuffer[]
/*  745:     */     //   72	3	8	in0	ByteBuffer[]
/*  746:     */     //   102	37	8	in0	ByteBuffer[]
/*  747:     */     //   135	6	9	out0	ByteBuffer
/*  748:     */     //   145	78	10	result	SSLEngineResult
/*  749:     */     //   224	21	11	localSSLEngineResult1	SSLEngineResult
/*  750:     */     //   250	21	12	localObject	Object
/*  751:     */     // Exception table:
/*  752:     */     //   from	to	target	type
/*  753:     */     //   3	226	250	finally
/*  754:     */     //   247	252	250	finally
/*  755:     */   }
/*  756:     */   
/*  757:     */   public void channelInactive(ChannelHandlerContext ctx)
/*  758:     */     throws Exception
/*  759:     */   {
/*  760:1005 */     setHandshakeFailure(ctx, CHANNEL_CLOSED, !this.outboundClosed, this.handshakeStarted);
/*  761:     */     
/*  762:     */ 
/*  763:1008 */     notifyClosePromise(CHANNEL_CLOSED);
/*  764:     */     
/*  765:1010 */     super.channelInactive(ctx);
/*  766:     */   }
/*  767:     */   
/*  768:     */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  769:     */     throws Exception
/*  770:     */   {
/*  771:1015 */     if (ignoreException(cause))
/*  772:     */     {
/*  773:1018 */       if (logger.isDebugEnabled()) {
/*  774:1019 */         logger.debug("{} Swallowing a harmless 'connection reset by peer / broken pipe' error that occurred while writing close_notify in response to the peer's close_notify", ctx
/*  775:     */         
/*  776:1021 */           .channel(), cause);
/*  777:     */       }
/*  778:1026 */       if (ctx.channel().isActive()) {
/*  779:1027 */         ctx.close();
/*  780:     */       }
/*  781:     */     }
/*  782:     */     else
/*  783:     */     {
/*  784:1030 */       ctx.fireExceptionCaught(cause);
/*  785:     */     }
/*  786:     */   }
/*  787:     */   
/*  788:     */   private boolean ignoreException(Throwable t)
/*  789:     */   {
/*  790:1044 */     if ((!(t instanceof SSLException)) && ((t instanceof IOException)) && (this.sslClosePromise.isDone()))
/*  791:     */     {
/*  792:1045 */       String message = t.getMessage();
/*  793:1049 */       if ((message != null) && (IGNORABLE_ERROR_MESSAGE.matcher(message).matches())) {
/*  794:1050 */         return true;
/*  795:     */       }
/*  796:1054 */       StackTraceElement[] elements = t.getStackTrace();
/*  797:1055 */       for (StackTraceElement element : elements)
/*  798:     */       {
/*  799:1056 */         String classname = element.getClassName();
/*  800:1057 */         String methodname = element.getMethodName();
/*  801:1060 */         if (!classname.startsWith("io.netty.")) {
/*  802:1065 */           if ("read".equals(methodname))
/*  803:     */           {
/*  804:1071 */             if (IGNORABLE_CLASS_IN_STACK.matcher(classname).matches()) {
/*  805:1072 */               return true;
/*  806:     */             }
/*  807:     */             try
/*  808:     */             {
/*  809:1079 */               Class<?> clazz = PlatformDependent.getClassLoader(getClass()).loadClass(classname);
/*  810:1081 */               if ((SocketChannel.class.isAssignableFrom(clazz)) || 
/*  811:1082 */                 (DatagramChannel.class.isAssignableFrom(clazz))) {
/*  812:1083 */                 return true;
/*  813:     */               }
/*  814:1087 */               if ((PlatformDependent.javaVersion() >= 7) && 
/*  815:1088 */                 ("com.sun.nio.sctp.SctpChannel".equals(clazz.getSuperclass().getName()))) {
/*  816:1089 */                 return true;
/*  817:     */               }
/*  818:     */             }
/*  819:     */             catch (Throwable cause)
/*  820:     */             {
/*  821:1092 */               logger.debug("Unexpected exception while loading class {} classname {}", new Object[] {
/*  822:1093 */                 getClass(), classname, cause });
/*  823:     */             }
/*  824:     */           }
/*  825:     */         }
/*  826:     */       }
/*  827:     */     }
/*  828:1098 */     return false;
/*  829:     */   }
/*  830:     */   
/*  831:     */   public static boolean isEncrypted(ByteBuf buffer)
/*  832:     */   {
/*  833:1114 */     if (buffer.readableBytes() < 5) {
/*  834:1115 */       throw new IllegalArgumentException("buffer must have at least 5 readable bytes");
/*  835:     */     }
/*  836:1118 */     return SslUtils.getEncryptedPacketLength(buffer, buffer.readerIndex()) != -2;
/*  837:     */   }
/*  838:     */   
/*  839:     */   private void decodeJdkCompatible(ChannelHandlerContext ctx, ByteBuf in)
/*  840:     */     throws NotSslRecordException
/*  841:     */   {
/*  842:1122 */     int packetLength = this.packetLength;
/*  843:1124 */     if (packetLength > 0)
/*  844:     */     {
/*  845:1125 */       if (in.readableBytes() >= packetLength) {}
/*  846:     */     }
/*  847:     */     else
/*  848:     */     {
/*  849:1130 */       int readableBytes = in.readableBytes();
/*  850:1131 */       if (readableBytes < 5) {
/*  851:1132 */         return;
/*  852:     */       }
/*  853:1134 */       packetLength = SslUtils.getEncryptedPacketLength(in, in.readerIndex());
/*  854:1135 */       if (packetLength == -2)
/*  855:     */       {
/*  856:1138 */         NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
/*  857:1139 */         in.skipBytes(in.readableBytes());
/*  858:     */         
/*  859:     */ 
/*  860:     */ 
/*  861:1143 */         setHandshakeFailure(ctx, e);
/*  862:     */         
/*  863:1145 */         throw e;
/*  864:     */       }
/*  865:1147 */       assert (packetLength > 0);
/*  866:1148 */       if (packetLength > readableBytes)
/*  867:     */       {
/*  868:1150 */         this.packetLength = packetLength;
/*  869:1151 */         return;
/*  870:     */       }
/*  871:     */     }
/*  872:1157 */     this.packetLength = 0;
/*  873:     */     try
/*  874:     */     {
/*  875:1159 */       int bytesConsumed = unwrap(ctx, in, in.readerIndex(), packetLength);
/*  876:1160 */       assert ((bytesConsumed == packetLength) || (this.engine.isInboundDone())) : ("we feed the SSLEngine a packets worth of data: " + packetLength + " but it only consumed: " + bytesConsumed);
/*  877:     */       
/*  878:     */ 
/*  879:1163 */       in.skipBytes(bytesConsumed);
/*  880:     */     }
/*  881:     */     catch (Throwable cause)
/*  882:     */     {
/*  883:1165 */       handleUnwrapThrowable(ctx, cause);
/*  884:     */     }
/*  885:     */   }
/*  886:     */   
/*  887:     */   private void decodeNonJdkCompatible(ChannelHandlerContext ctx, ByteBuf in)
/*  888:     */   {
/*  889:     */     try
/*  890:     */     {
/*  891:1171 */       in.skipBytes(unwrap(ctx, in, in.readerIndex(), in.readableBytes()));
/*  892:     */     }
/*  893:     */     catch (Throwable cause)
/*  894:     */     {
/*  895:1173 */       handleUnwrapThrowable(ctx, cause);
/*  896:     */     }
/*  897:     */   }
/*  898:     */   
/*  899:     */   private void handleUnwrapThrowable(ChannelHandlerContext ctx, Throwable cause)
/*  900:     */   {
/*  901:     */     try
/*  902:     */     {
/*  903:1181 */       wrapAndFlush(ctx);
/*  904:     */     }
/*  905:     */     catch (SSLException ex)
/*  906:     */     {
/*  907:1183 */       logger.debug("SSLException during trying to call SSLEngine.wrap(...) because of an previous SSLException, ignoring...", ex);
/*  908:     */     }
/*  909:     */     finally
/*  910:     */     {
/*  911:1186 */       setHandshakeFailure(ctx, cause);
/*  912:     */     }
/*  913:1188 */     PlatformDependent.throwException(cause);
/*  914:     */   }
/*  915:     */   
/*  916:     */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  917:     */     throws SSLException
/*  918:     */   {
/*  919:1193 */     if (this.jdkCompatibilityMode) {
/*  920:1194 */       decodeJdkCompatible(ctx, in);
/*  921:     */     } else {
/*  922:1196 */       decodeNonJdkCompatible(ctx, in);
/*  923:     */     }
/*  924:     */   }
/*  925:     */   
/*  926:     */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  927:     */     throws Exception
/*  928:     */   {
/*  929:1203 */     discardSomeReadBytes();
/*  930:     */     
/*  931:1205 */     flushIfNeeded(ctx);
/*  932:1206 */     readIfNeeded(ctx);
/*  933:     */     
/*  934:1208 */     this.firedChannelRead = false;
/*  935:1209 */     ctx.fireChannelReadComplete();
/*  936:     */   }
/*  937:     */   
/*  938:     */   private void readIfNeeded(ChannelHandlerContext ctx)
/*  939:     */   {
/*  940:1214 */     if ((!ctx.channel().config().isAutoRead()) && ((!this.firedChannelRead) || (!this.handshakePromise.isDone()))) {
/*  941:1217 */       ctx.read();
/*  942:     */     }
/*  943:     */   }
/*  944:     */   
/*  945:     */   private void flushIfNeeded(ChannelHandlerContext ctx)
/*  946:     */   {
/*  947:1222 */     if (this.needsFlush) {
/*  948:1223 */       forceFlush(ctx);
/*  949:     */     }
/*  950:     */   }
/*  951:     */   
/*  952:     */   private void unwrapNonAppData(ChannelHandlerContext ctx)
/*  953:     */     throws SSLException
/*  954:     */   {
/*  955:1231 */     unwrap(ctx, Unpooled.EMPTY_BUFFER, 0, 0);
/*  956:     */   }
/*  957:     */   
/*  958:     */   private int unwrap(ChannelHandlerContext ctx, ByteBuf packet, int offset, int length)
/*  959:     */     throws SSLException
/*  960:     */   {
/*  961:1239 */     int originalLength = length;
/*  962:1240 */     boolean wrapLater = false;
/*  963:1241 */     boolean notifyClosure = false;
/*  964:1242 */     int overflowReadableBytes = -1;
/*  965:1243 */     ByteBuf decodeOut = allocate(ctx, length);
/*  966:     */     try
/*  967:     */     {
/*  968:1247 */       while (!ctx.isRemoved())
/*  969:     */       {
/*  970:1248 */         SSLEngineResult result = this.engineType.unwrap(this, packet, offset, length, decodeOut);
/*  971:1249 */         SSLEngineResult.Status status = result.getStatus();
/*  972:1250 */         SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();
/*  973:1251 */         int produced = result.bytesProduced();
/*  974:1252 */         int consumed = result.bytesConsumed();
/*  975:     */         
/*  976:     */ 
/*  977:1255 */         offset += consumed;
/*  978:1256 */         length -= consumed;
/*  979:1258 */         switch (status)
/*  980:     */         {
/*  981:     */         case BUFFER_OVERFLOW: 
/*  982:1260 */           int readableBytes = decodeOut.readableBytes();
/*  983:1261 */           int previousOverflowReadableBytes = overflowReadableBytes;
/*  984:1262 */           overflowReadableBytes = readableBytes;
/*  985:1263 */           int bufferSize = this.engine.getSession().getApplicationBufferSize() - readableBytes;
/*  986:1264 */           if (readableBytes > 0)
/*  987:     */           {
/*  988:1265 */             this.firedChannelRead = true;
/*  989:1266 */             ctx.fireChannelRead(decodeOut);
/*  990:     */             
/*  991:     */ 
/*  992:1269 */             decodeOut = null;
/*  993:1270 */             if (bufferSize <= 0) {
/*  994:1275 */               bufferSize = this.engine.getSession().getApplicationBufferSize();
/*  995:     */             }
/*  996:     */           }
/*  997:     */           else
/*  998:     */           {
/*  999:1279 */             decodeOut.release();
/* 1000:1280 */             decodeOut = null;
/* 1001:     */           }
/* 1002:1282 */           if ((readableBytes == 0) && (previousOverflowReadableBytes == 0)) {
/* 1003:1287 */             throw new IllegalStateException("Two consecutive overflows but no content was consumed. " + SSLSession.class.getSimpleName() + " getApplicationBufferSize: " + this.engine.getSession().getApplicationBufferSize() + " maybe too small.");
/* 1004:     */           }
/* 1005:1292 */           decodeOut = allocate(ctx, this.engineType.calculatePendingData(this, bufferSize));
/* 1006:1293 */           break;
/* 1007:     */         case CLOSED: 
/* 1008:1296 */           notifyClosure = true;
/* 1009:1297 */           overflowReadableBytes = -1;
/* 1010:1298 */           break;
/* 1011:     */         default: 
/* 1012:1300 */           overflowReadableBytes = -1;
/* 1013:1304 */           switch (9.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[handshakeStatus.ordinal()])
/* 1014:     */           {
/* 1015:     */           case 5: 
/* 1016:     */             break;
/* 1017:     */           case 4: 
/* 1018:1311 */             if ((!wrapNonAppData(ctx, true)) || (length != 0)) {}
/* 1019:1312 */             break;
/* 1020:     */           case 1: 
/* 1021:1316 */             runDelegatedTasks();
/* 1022:1317 */             break;
/* 1023:     */           case 2: 
/* 1024:1319 */             setHandshakeSuccess();
/* 1025:1320 */             wrapLater = true;
/* 1026:     */             
/* 1027:     */ 
/* 1028:     */ 
/* 1029:     */ 
/* 1030:     */ 
/* 1031:     */ 
/* 1032:     */ 
/* 1033:     */ 
/* 1034:     */ 
/* 1035:     */ 
/* 1036:     */ 
/* 1037:     */ 
/* 1038:1333 */             break;
/* 1039:     */           case 3: 
/* 1040:1335 */             if (setHandshakeSuccessIfStillHandshaking())
/* 1041:     */             {
/* 1042:1336 */               wrapLater = true;
/* 1043:     */             }
/* 1044:     */             else
/* 1045:     */             {
/* 1046:1339 */               if (this.flushedBeforeHandshake)
/* 1047:     */               {
/* 1048:1343 */                 this.flushedBeforeHandshake = false;
/* 1049:1344 */                 wrapLater = true;
/* 1050:     */               }
/* 1051:1349 */               if (length != 0) {}
/* 1052:     */             }
/* 1053:1350 */             break;
/* 1054:     */           default: 
/* 1055:1354 */             throw new IllegalStateException("unknown handshake status: " + handshakeStatus);
/* 1056:1357 */             if ((status == SSLEngineResult.Status.BUFFER_UNDERFLOW) || ((consumed == 0) && (produced == 0)))
/* 1057:     */             {
/* 1058:1358 */               if (handshakeStatus != SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
/* 1059:     */                 break label490;
/* 1060:     */               }
/* 1061:1361 */               readIfNeeded(ctx);
/* 1062:     */               break label490;
/* 1063:     */             }
/* 1064:     */             break;
/* 1065:     */           }
/* 1066:     */           break;
/* 1067:     */         }
/* 1068:     */       }
/* 1069:     */       label490:
/* 1070:1368 */       if (wrapLater) {
/* 1071:1369 */         wrap(ctx, true);
/* 1072:     */       }
/* 1073:1372 */       if (notifyClosure) {
/* 1074:1373 */         notifyClosePromise(null);
/* 1075:     */       }
/* 1076:     */     }
/* 1077:     */     finally
/* 1078:     */     {
/* 1079:1376 */       if (decodeOut != null) {
/* 1080:1377 */         if (decodeOut.isReadable())
/* 1081:     */         {
/* 1082:1378 */           this.firedChannelRead = true;
/* 1083:     */           
/* 1084:1380 */           ctx.fireChannelRead(decodeOut);
/* 1085:     */         }
/* 1086:     */         else
/* 1087:     */         {
/* 1088:1382 */           decodeOut.release();
/* 1089:     */         }
/* 1090:     */       }
/* 1091:     */     }
/* 1092:1386 */     return originalLength - length;
/* 1093:     */   }
/* 1094:     */   
/* 1095:     */   private static ByteBuffer toByteBuffer(ByteBuf out, int index, int len)
/* 1096:     */   {
/* 1097:1390 */     return out.nioBufferCount() == 1 ? out.internalNioBuffer(index, len) : out
/* 1098:1391 */       .nioBuffer(index, len);
/* 1099:     */   }
/* 1100:     */   
/* 1101:     */   private void runDelegatedTasks()
/* 1102:     */   {
/* 1103:1401 */     if (this.delegatedTaskExecutor == ImmediateExecutor.INSTANCE) {
/* 1104:     */       for (;;)
/* 1105:     */       {
/* 1106:1403 */         Runnable task = this.engine.getDelegatedTask();
/* 1107:1404 */         if (task == null) {
/* 1108:     */           break;
/* 1109:     */         }
/* 1110:1408 */         task.run();
/* 1111:     */       }
/* 1112:     */     }
/* 1113:1411 */     final List<Runnable> tasks = new ArrayList(2);
/* 1114:     */     for (;;)
/* 1115:     */     {
/* 1116:1413 */       Runnable task = this.engine.getDelegatedTask();
/* 1117:1414 */       if (task == null) {
/* 1118:     */         break;
/* 1119:     */       }
/* 1120:1418 */       tasks.add(task);
/* 1121:     */     }
/* 1122:1421 */     if (tasks.isEmpty()) {
/* 1123:1422 */       return;
/* 1124:     */     }
/* 1125:1425 */     final CountDownLatch latch = new CountDownLatch(1);
/* 1126:1426 */     this.delegatedTaskExecutor.execute(new Runnable()
/* 1127:     */     {
/* 1128:     */       public void run()
/* 1129:     */       {
/* 1130:     */         try
/* 1131:     */         {
/* 1132:1430 */           for (Runnable task : tasks) {
/* 1133:1431 */             task.run();
/* 1134:     */           }
/* 1135:     */         }
/* 1136:     */         catch (Exception e)
/* 1137:     */         {
/* 1138:1434 */           SslHandler.this.ctx.fireExceptionCaught(e);
/* 1139:     */         }
/* 1140:     */         finally
/* 1141:     */         {
/* 1142:1436 */           latch.countDown();
/* 1143:     */         }
/* 1144:     */       }
/* 1145:1440 */     });
/* 1146:1441 */     boolean interrupted = false;
/* 1147:1442 */     while (latch.getCount() != 0L) {
/* 1148:     */       try
/* 1149:     */       {
/* 1150:1444 */         latch.await();
/* 1151:     */       }
/* 1152:     */       catch (InterruptedException e)
/* 1153:     */       {
/* 1154:1447 */         interrupted = true;
/* 1155:     */       }
/* 1156:     */     }
/* 1157:1451 */     if (interrupted) {
/* 1158:1452 */       Thread.currentThread().interrupt();
/* 1159:     */     }
/* 1160:     */   }
/* 1161:     */   
/* 1162:     */   private boolean setHandshakeSuccessIfStillHandshaking()
/* 1163:     */   {
/* 1164:1465 */     if (!this.handshakePromise.isDone())
/* 1165:     */     {
/* 1166:1466 */       setHandshakeSuccess();
/* 1167:1467 */       return true;
/* 1168:     */     }
/* 1169:1469 */     return false;
/* 1170:     */   }
/* 1171:     */   
/* 1172:     */   private void setHandshakeSuccess()
/* 1173:     */   {
/* 1174:1476 */     this.handshakePromise.trySuccess(this.ctx.channel());
/* 1175:1478 */     if (logger.isDebugEnabled()) {
/* 1176:1479 */       logger.debug("{} HANDSHAKEN: {}", this.ctx.channel(), this.engine.getSession().getCipherSuite());
/* 1177:     */     }
/* 1178:1481 */     this.ctx.fireUserEventTriggered(SslHandshakeCompletionEvent.SUCCESS);
/* 1179:1483 */     if ((this.readDuringHandshake) && (!this.ctx.channel().config().isAutoRead()))
/* 1180:     */     {
/* 1181:1484 */       this.readDuringHandshake = false;
/* 1182:1485 */       this.ctx.read();
/* 1183:     */     }
/* 1184:     */   }
/* 1185:     */   
/* 1186:     */   private void setHandshakeFailure(ChannelHandlerContext ctx, Throwable cause)
/* 1187:     */   {
/* 1188:1493 */     setHandshakeFailure(ctx, cause, true, true);
/* 1189:     */   }
/* 1190:     */   
/* 1191:     */   private void setHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean closeInbound, boolean notify)
/* 1192:     */   {
/* 1193:     */     try
/* 1194:     */     {
/* 1195:1503 */       this.engine.closeOutbound();
/* 1196:1505 */       if (closeInbound) {
/* 1197:     */         try
/* 1198:     */         {
/* 1199:1507 */           this.engine.closeInbound();
/* 1200:     */         }
/* 1201:     */         catch (SSLException e)
/* 1202:     */         {
/* 1203:1509 */           if (logger.isDebugEnabled())
/* 1204:     */           {
/* 1205:1514 */             String msg = e.getMessage();
/* 1206:1515 */             if ((msg == null) || (!msg.contains("possible truncation attack"))) {
/* 1207:1516 */               logger.debug("{} SSLEngine.closeInbound() raised an exception.", ctx.channel(), e);
/* 1208:     */             }
/* 1209:     */           }
/* 1210:     */         }
/* 1211:     */       }
/* 1212:1521 */       notifyHandshakeFailure(cause, notify);
/* 1213:     */     }
/* 1214:     */     finally
/* 1215:     */     {
/* 1216:1524 */       releaseAndFailAll(cause);
/* 1217:     */     }
/* 1218:     */   }
/* 1219:     */   
/* 1220:     */   private void releaseAndFailAll(Throwable cause)
/* 1221:     */   {
/* 1222:1529 */     if (this.pendingUnencryptedWrites != null) {
/* 1223:1530 */       this.pendingUnencryptedWrites.releaseAndFailAll(this.ctx, cause);
/* 1224:     */     }
/* 1225:     */   }
/* 1226:     */   
/* 1227:     */   private void notifyHandshakeFailure(Throwable cause, boolean notify)
/* 1228:     */   {
/* 1229:1535 */     if (this.handshakePromise.tryFailure(cause)) {
/* 1230:1536 */       SslUtils.notifyHandshakeFailure(this.ctx, cause, notify);
/* 1231:     */     }
/* 1232:     */   }
/* 1233:     */   
/* 1234:     */   private void notifyClosePromise(Throwable cause)
/* 1235:     */   {
/* 1236:1541 */     if (cause == null)
/* 1237:     */     {
/* 1238:1542 */       if (this.sslClosePromise.trySuccess(this.ctx.channel())) {
/* 1239:1543 */         this.ctx.fireUserEventTriggered(SslCloseCompletionEvent.SUCCESS);
/* 1240:     */       }
/* 1241:     */     }
/* 1242:1546 */     else if (this.sslClosePromise.tryFailure(cause)) {
/* 1243:1547 */       this.ctx.fireUserEventTriggered(new SslCloseCompletionEvent(cause));
/* 1244:     */     }
/* 1245:     */   }
/* 1246:     */   
/* 1247:     */   private void closeOutboundAndChannel(ChannelHandlerContext ctx, ChannelPromise promise, boolean disconnect)
/* 1248:     */     throws Exception
/* 1249:     */   {
/* 1250:1554 */     if (!ctx.channel().isActive())
/* 1251:     */     {
/* 1252:1555 */       if (disconnect) {
/* 1253:1556 */         ctx.disconnect(promise);
/* 1254:     */       } else {
/* 1255:1558 */         ctx.close(promise);
/* 1256:     */       }
/* 1257:1560 */       return;
/* 1258:     */     }
/* 1259:1563 */     this.outboundClosed = true;
/* 1260:1564 */     this.engine.closeOutbound();
/* 1261:     */     
/* 1262:1566 */     ChannelPromise closeNotifyPromise = ctx.newPromise();
/* 1263:     */     try
/* 1264:     */     {
/* 1265:1568 */       flush(ctx, closeNotifyPromise);
/* 1266:     */     }
/* 1267:     */     finally
/* 1268:     */     {
/* 1269:1578 */       safeClose(ctx, closeNotifyPromise, ctx.newPromise().addListener(new ChannelPromiseNotifier(false, new ChannelPromise[] { promise })));
/* 1270:     */     }
/* 1271:     */   }
/* 1272:     */   
/* 1273:     */   private void flush(ChannelHandlerContext ctx, ChannelPromise promise)
/* 1274:     */     throws Exception
/* 1275:     */   {
/* 1276:1584 */     if (this.pendingUnencryptedWrites != null) {
/* 1277:1585 */       this.pendingUnencryptedWrites.add(Unpooled.EMPTY_BUFFER, promise);
/* 1278:     */     } else {
/* 1279:1587 */       promise.setFailure(newPendingWritesNullException());
/* 1280:     */     }
/* 1281:1589 */     flush(ctx);
/* 1282:     */   }
/* 1283:     */   
/* 1284:     */   public void handlerAdded(ChannelHandlerContext ctx)
/* 1285:     */     throws Exception
/* 1286:     */   {
/* 1287:1594 */     this.ctx = ctx;
/* 1288:     */     
/* 1289:1596 */     this.pendingUnencryptedWrites = new SslHandlerCoalescingBufferQueue(ctx.channel(), 16);
/* 1290:1597 */     if (ctx.channel().isActive()) {
/* 1291:1598 */       startHandshakeProcessing();
/* 1292:     */     }
/* 1293:     */   }
/* 1294:     */   
/* 1295:     */   private void startHandshakeProcessing()
/* 1296:     */   {
/* 1297:1603 */     this.handshakeStarted = true;
/* 1298:1604 */     if (this.engine.getUseClientMode()) {
/* 1299:1608 */       handshake(null);
/* 1300:     */     } else {
/* 1301:1610 */       applyHandshakeTimeout(null);
/* 1302:     */     }
/* 1303:     */   }
/* 1304:     */   
/* 1305:     */   public Future<Channel> renegotiate()
/* 1306:     */   {
/* 1307:1618 */     ChannelHandlerContext ctx = this.ctx;
/* 1308:1619 */     if (ctx == null) {
/* 1309:1620 */       throw new IllegalStateException();
/* 1310:     */     }
/* 1311:1623 */     return renegotiate(ctx.executor().newPromise());
/* 1312:     */   }
/* 1313:     */   
/* 1314:     */   public Future<Channel> renegotiate(final Promise<Channel> promise)
/* 1315:     */   {
/* 1316:1630 */     if (promise == null) {
/* 1317:1631 */       throw new NullPointerException("promise");
/* 1318:     */     }
/* 1319:1634 */     ChannelHandlerContext ctx = this.ctx;
/* 1320:1635 */     if (ctx == null) {
/* 1321:1636 */       throw new IllegalStateException();
/* 1322:     */     }
/* 1323:1639 */     EventExecutor executor = ctx.executor();
/* 1324:1640 */     if (!executor.inEventLoop())
/* 1325:     */     {
/* 1326:1641 */       executor.execute(new Runnable()
/* 1327:     */       {
/* 1328:     */         public void run()
/* 1329:     */         {
/* 1330:1644 */           SslHandler.this.handshake(promise);
/* 1331:     */         }
/* 1332:1646 */       });
/* 1333:1647 */       return promise;
/* 1334:     */     }
/* 1335:1650 */     handshake(promise);
/* 1336:1651 */     return promise;
/* 1337:     */   }
/* 1338:     */   
/* 1339:     */   private void handshake(final Promise<Channel> newHandshakePromise)
/* 1340:     */   {
/* 1341:     */     Promise<Channel> p;
/* 1342:1663 */     if (newHandshakePromise != null)
/* 1343:     */     {
/* 1344:1664 */       Promise<Channel> oldHandshakePromise = this.handshakePromise;
/* 1345:1665 */       if (!oldHandshakePromise.isDone())
/* 1346:     */       {
/* 1347:1668 */         oldHandshakePromise.addListener(new FutureListener()
/* 1348:     */         {
/* 1349:     */           public void operationComplete(Future<Channel> future)
/* 1350:     */             throws Exception
/* 1351:     */           {
/* 1352:1671 */             if (future.isSuccess()) {
/* 1353:1672 */               newHandshakePromise.setSuccess(future.getNow());
/* 1354:     */             } else {
/* 1355:1674 */               newHandshakePromise.setFailure(future.cause());
/* 1356:     */             }
/* 1357:     */           }
/* 1358:     */         }); return;
/* 1359:     */       }
/* 1360:     */       Promise<Channel> p;
/* 1361:1681 */       this.handshakePromise = (p = newHandshakePromise);
/* 1362:     */     }
/* 1363:     */     else
/* 1364:     */     {
/* 1365:1682 */       if (this.engine.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
/* 1366:1685 */         return;
/* 1367:     */       }
/* 1368:1688 */       p = this.handshakePromise;
/* 1369:1689 */       assert (!p.isDone());
/* 1370:     */     }
/* 1371:1693 */     ChannelHandlerContext ctx = this.ctx;
/* 1372:     */     try
/* 1373:     */     {
/* 1374:1695 */       this.engine.beginHandshake();
/* 1375:1696 */       wrapNonAppData(ctx, false);
/* 1376:     */     }
/* 1377:     */     catch (Throwable e)
/* 1378:     */     {
/* 1379:1698 */       setHandshakeFailure(ctx, e);
/* 1380:     */     }
/* 1381:     */     finally
/* 1382:     */     {
/* 1383:1700 */       forceFlush(ctx);
/* 1384:     */     }
/* 1385:1702 */     applyHandshakeTimeout(p);
/* 1386:     */   }
/* 1387:     */   
/* 1388:     */   private void applyHandshakeTimeout(Promise<Channel> p)
/* 1389:     */   {
/* 1390:1706 */     final Promise<Channel> promise = p == null ? this.handshakePromise : p;
/* 1391:     */     
/* 1392:1708 */     long handshakeTimeoutMillis = this.handshakeTimeoutMillis;
/* 1393:1709 */     if ((handshakeTimeoutMillis <= 0L) || (promise.isDone())) {
/* 1394:1710 */       return;
/* 1395:     */     }
/* 1396:1713 */     final ScheduledFuture<?> timeoutFuture = this.ctx.executor().schedule(new Runnable()
/* 1397:     */     {
/* 1398:     */       public void run()
/* 1399:     */       {
/* 1400:1716 */         if (promise.isDone()) {
/* 1401:1717 */           return;
/* 1402:     */         }
/* 1403:     */         try
/* 1404:     */         {
/* 1405:1720 */           SslHandler.this.notifyHandshakeFailure(SslHandler.HANDSHAKE_TIMED_OUT, true);
/* 1406:     */           
/* 1407:1722 */           SslHandler.this.releaseAndFailAll(SslHandler.HANDSHAKE_TIMED_OUT);
/* 1408:     */         }
/* 1409:     */         finally
/* 1410:     */         {
/* 1411:1722 */           SslHandler.this.releaseAndFailAll(SslHandler.HANDSHAKE_TIMED_OUT);
/* 1412:     */         }
/* 1413:     */       }
/* 1414:1722 */     }, handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
/* 1415:     */     
/* 1416:     */ 
/* 1417:     */ 
/* 1418:     */ 
/* 1419:     */ 
/* 1420:1728 */     promise.addListener(new FutureListener()
/* 1421:     */     {
/* 1422:     */       public void operationComplete(Future<Channel> f)
/* 1423:     */         throws Exception
/* 1424:     */       {
/* 1425:1731 */         timeoutFuture.cancel(false);
/* 1426:     */       }
/* 1427:     */     });
/* 1428:     */   }
/* 1429:     */   
/* 1430:     */   private void forceFlush(ChannelHandlerContext ctx)
/* 1431:     */   {
/* 1432:1737 */     this.needsFlush = false;
/* 1433:1738 */     ctx.flush();
/* 1434:     */   }
/* 1435:     */   
/* 1436:     */   public void channelActive(ChannelHandlerContext ctx)
/* 1437:     */     throws Exception
/* 1438:     */   {
/* 1439:1746 */     if (!this.startTls) {
/* 1440:1747 */       startHandshakeProcessing();
/* 1441:     */     }
/* 1442:1749 */     ctx.fireChannelActive();
/* 1443:     */   }
/* 1444:     */   
/* 1445:     */   private void safeClose(final ChannelHandlerContext ctx, final ChannelFuture flushFuture, final ChannelPromise promise)
/* 1446:     */   {
/* 1447:1755 */     if (!ctx.channel().isActive())
/* 1448:     */     {
/* 1449:1756 */       ctx.close(promise); return;
/* 1450:     */     }
/* 1451:     */     ScheduledFuture<?> timeoutFuture;
/* 1452:     */     final ScheduledFuture<?> timeoutFuture;
/* 1453:1761 */     if (!flushFuture.isDone())
/* 1454:     */     {
/* 1455:1762 */       long closeNotifyTimeout = this.closeNotifyFlushTimeoutMillis;
/* 1456:     */       ScheduledFuture<?> timeoutFuture;
/* 1457:1763 */       if (closeNotifyTimeout > 0L) {
/* 1458:1765 */         timeoutFuture = ctx.executor().schedule(new Runnable()
/* 1459:     */         {
/* 1460:     */           public void run()
/* 1461:     */           {
/* 1462:1769 */             if (!flushFuture.isDone())
/* 1463:     */             {
/* 1464:1770 */               SslHandler.logger.warn("{} Last write attempt timed out; force-closing the connection.", ctx
/* 1465:1771 */                 .channel());
/* 1466:1772 */               SslHandler.addCloseListener(ctx.close(ctx.newPromise()), promise);
/* 1467:     */             }
/* 1468:     */           }
/* 1469:1772 */         }, closeNotifyTimeout, TimeUnit.MILLISECONDS);
/* 1470:     */       } else {
/* 1471:1777 */         timeoutFuture = null;
/* 1472:     */       }
/* 1473:     */     }
/* 1474:     */     else
/* 1475:     */     {
/* 1476:1780 */       timeoutFuture = null;
/* 1477:     */     }
/* 1478:1784 */     flushFuture.addListener(new ChannelFutureListener()
/* 1479:     */     {
/* 1480:     */       public void operationComplete(ChannelFuture f)
/* 1481:     */         throws Exception
/* 1482:     */       {
/* 1483:1788 */         if (timeoutFuture != null) {
/* 1484:1789 */           timeoutFuture.cancel(false);
/* 1485:     */         }
/* 1486:1791 */         final long closeNotifyReadTimeout = SslHandler.this.closeNotifyReadTimeoutMillis;
/* 1487:1792 */         if (closeNotifyReadTimeout <= 0L)
/* 1488:     */         {
/* 1489:1795 */           SslHandler.addCloseListener(ctx.close(ctx.newPromise()), promise);
/* 1490:     */         }
/* 1491:     */         else
/* 1492:     */         {
/* 1493:     */           ScheduledFuture<?> closeNotifyReadTimeoutFuture;
/* 1494:     */           final ScheduledFuture<?> closeNotifyReadTimeoutFuture;
/* 1495:1799 */           if (!SslHandler.this.sslClosePromise.isDone()) {
/* 1496:1800 */             closeNotifyReadTimeoutFuture = ctx.executor().schedule(new Runnable()
/* 1497:     */             {
/* 1498:     */               public void run()
/* 1499:     */               {
/* 1500:1803 */                 if (!SslHandler.this.sslClosePromise.isDone())
/* 1501:     */                 {
/* 1502:1804 */                   SslHandler.logger.debug("{} did not receive close_notify in {}ms; force-closing the connection.", SslHandler.8.this.val$ctx
/* 1503:     */                   
/* 1504:1806 */                     .channel(), Long.valueOf(closeNotifyReadTimeout));
/* 1505:     */                   
/* 1506:     */ 
/* 1507:1809 */                   SslHandler.addCloseListener(SslHandler.8.this.val$ctx.close(SslHandler.8.this.val$ctx.newPromise()), SslHandler.8.this.val$promise);
/* 1508:     */                 }
/* 1509:     */               }
/* 1510:1809 */             }, closeNotifyReadTimeout, TimeUnit.MILLISECONDS);
/* 1511:     */           } else {
/* 1512:1814 */             closeNotifyReadTimeoutFuture = null;
/* 1513:     */           }
/* 1514:1818 */           SslHandler.this.sslClosePromise.addListener(new FutureListener()
/* 1515:     */           {
/* 1516:     */             public void operationComplete(Future<Channel> future)
/* 1517:     */               throws Exception
/* 1518:     */             {
/* 1519:1821 */               if (closeNotifyReadTimeoutFuture != null) {
/* 1520:1822 */                 closeNotifyReadTimeoutFuture.cancel(false);
/* 1521:     */               }
/* 1522:1824 */               SslHandler.addCloseListener(SslHandler.8.this.val$ctx.close(SslHandler.8.this.val$ctx.newPromise()), SslHandler.8.this.val$promise);
/* 1523:     */             }
/* 1524:     */           });
/* 1525:     */         }
/* 1526:     */       }
/* 1527:     */     });
/* 1528:     */   }
/* 1529:     */   
/* 1530:     */   private static void addCloseListener(ChannelFuture future, ChannelPromise promise)
/* 1531:     */   {
/* 1532:1839 */     future.addListener(new ChannelPromiseNotifier(false, new ChannelPromise[] { promise }));
/* 1533:     */   }
/* 1534:     */   
/* 1535:     */   private ByteBuf allocate(ChannelHandlerContext ctx, int capacity)
/* 1536:     */   {
/* 1537:1847 */     ByteBufAllocator alloc = ctx.alloc();
/* 1538:1848 */     if (this.engineType.wantsDirectBuffer) {
/* 1539:1849 */       return alloc.directBuffer(capacity);
/* 1540:     */     }
/* 1541:1851 */     return alloc.buffer(capacity);
/* 1542:     */   }
/* 1543:     */   
/* 1544:     */   private ByteBuf allocateOutNetBuf(ChannelHandlerContext ctx, int pendingBytes, int numComponents)
/* 1545:     */   {
/* 1546:1860 */     return allocate(ctx, this.engineType.calculateWrapBufferCapacity(this, pendingBytes, numComponents));
/* 1547:     */   }
/* 1548:     */   
/* 1549:     */   private final class SslHandlerCoalescingBufferQueue
/* 1550:     */     extends AbstractCoalescingBufferQueue
/* 1551:     */   {
/* 1552:     */     SslHandlerCoalescingBufferQueue(Channel channel, int initSize)
/* 1553:     */     {
/* 1554:1871 */       super(initSize);
/* 1555:     */     }
/* 1556:     */     
/* 1557:     */     protected ByteBuf compose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next)
/* 1558:     */     {
/* 1559:1876 */       int wrapDataSize = SslHandler.this.wrapDataSize;
/* 1560:1877 */       if ((cumulation instanceof CompositeByteBuf))
/* 1561:     */       {
/* 1562:1878 */         CompositeByteBuf composite = (CompositeByteBuf)cumulation;
/* 1563:1879 */         int numComponents = composite.numComponents();
/* 1564:1880 */         if ((numComponents == 0) || 
/* 1565:1881 */           (!SslHandler.attemptCopyToCumulation(composite.internalComponent(numComponents - 1), next, wrapDataSize))) {
/* 1566:1882 */           composite.addComponent(true, next);
/* 1567:     */         }
/* 1568:1884 */         return composite;
/* 1569:     */       }
/* 1570:1886 */       return SslHandler.attemptCopyToCumulation(cumulation, next, wrapDataSize) ? cumulation : 
/* 1571:1887 */         copyAndCompose(alloc, cumulation, next);
/* 1572:     */     }
/* 1573:     */     
/* 1574:     */     protected ByteBuf composeFirst(ByteBufAllocator allocator, ByteBuf first)
/* 1575:     */     {
/* 1576:1892 */       if ((first instanceof CompositeByteBuf))
/* 1577:     */       {
/* 1578:1893 */         CompositeByteBuf composite = (CompositeByteBuf)first;
/* 1579:1894 */         first = allocator.directBuffer(composite.readableBytes());
/* 1580:     */         try
/* 1581:     */         {
/* 1582:1896 */           first.writeBytes(composite);
/* 1583:     */         }
/* 1584:     */         catch (Throwable cause)
/* 1585:     */         {
/* 1586:1898 */           first.release();
/* 1587:1899 */           PlatformDependent.throwException(cause);
/* 1588:     */         }
/* 1589:1901 */         composite.release();
/* 1590:     */       }
/* 1591:1903 */       return first;
/* 1592:     */     }
/* 1593:     */     
/* 1594:     */     protected ByteBuf removeEmptyValue()
/* 1595:     */     {
/* 1596:1908 */       return null;
/* 1597:     */     }
/* 1598:     */   }
/* 1599:     */   
/* 1600:     */   private static boolean attemptCopyToCumulation(ByteBuf cumulation, ByteBuf next, int wrapDataSize)
/* 1601:     */   {
/* 1602:1913 */     int inReadableBytes = next.readableBytes();
/* 1603:1914 */     int cumulationCapacity = cumulation.capacity();
/* 1604:1915 */     if (wrapDataSize - cumulation.readableBytes() >= inReadableBytes) {
/* 1605:1919 */       if ((!cumulation.isWritable(inReadableBytes)) || (cumulationCapacity < wrapDataSize))
/* 1606:     */       {
/* 1607:1919 */         if (cumulationCapacity < wrapDataSize) {
/* 1608:1921 */           if (!ByteBufUtil.ensureWritableSuccess(cumulation.ensureWritable(inReadableBytes, false))) {}
/* 1609:     */         }
/* 1610:     */       }
/* 1611:     */       else
/* 1612:     */       {
/* 1613:1922 */         cumulation.writeBytes(next);
/* 1614:1923 */         next.release();
/* 1615:1924 */         return true;
/* 1616:     */       }
/* 1617:     */     }
/* 1618:1926 */     return false;
/* 1619:     */   }
/* 1620:     */   
/* 1621:     */   private final class LazyChannelPromise
/* 1622:     */     extends DefaultPromise<Channel>
/* 1623:     */   {
/* 1624:     */     private LazyChannelPromise() {}
/* 1625:     */     
/* 1626:     */     protected EventExecutor executor()
/* 1627:     */     {
/* 1628:1933 */       if (SslHandler.this.ctx == null) {
/* 1629:1934 */         throw new IllegalStateException();
/* 1630:     */       }
/* 1631:1936 */       return SslHandler.this.ctx.executor();
/* 1632:     */     }
/* 1633:     */     
/* 1634:     */     protected void checkDeadLock()
/* 1635:     */     {
/* 1636:1941 */       if (SslHandler.this.ctx == null) {
/* 1637:1948 */         return;
/* 1638:     */       }
/* 1639:1950 */       super.checkDeadLock();
/* 1640:     */     }
/* 1641:     */   }
/* 1642:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslHandler
 * JD-Core Version:    0.7.0.1
 */