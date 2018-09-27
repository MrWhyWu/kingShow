/*    1:     */ package io.netty.channel.epoll;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*    6:     */ import io.netty.channel.Channel;
/*    7:     */ import io.netty.channel.Channel.Unsafe;
/*    8:     */ import io.netty.channel.ChannelConfig;
/*    9:     */ import io.netty.channel.ChannelFuture;
/*   10:     */ import io.netty.channel.ChannelFutureListener;
/*   11:     */ import io.netty.channel.ChannelMetadata;
/*   12:     */ import io.netty.channel.ChannelOutboundBuffer;
/*   13:     */ import io.netty.channel.ChannelPipeline;
/*   14:     */ import io.netty.channel.ChannelPromise;
/*   15:     */ import io.netty.channel.DefaultFileRegion;
/*   16:     */ import io.netty.channel.EventLoop;
/*   17:     */ import io.netty.channel.FileRegion;
/*   18:     */ import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
/*   19:     */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*   20:     */ import io.netty.channel.socket.DuplexChannel;
/*   21:     */ import io.netty.channel.unix.FileDescriptor;
/*   22:     */ import io.netty.channel.unix.IovArray;
/*   23:     */ import io.netty.channel.unix.SocketWritableByteChannel;
/*   24:     */ import io.netty.channel.unix.UnixChannelUtil;
/*   25:     */ import io.netty.util.internal.ObjectUtil;
/*   26:     */ import io.netty.util.internal.PlatformDependent;
/*   27:     */ import io.netty.util.internal.StringUtil;
/*   28:     */ import io.netty.util.internal.ThrowableUtil;
/*   29:     */ import io.netty.util.internal.logging.InternalLogger;
/*   30:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   31:     */ import java.io.IOException;
/*   32:     */ import java.net.SocketAddress;
/*   33:     */ import java.nio.ByteBuffer;
/*   34:     */ import java.nio.channels.ClosedChannelException;
/*   35:     */ import java.nio.channels.WritableByteChannel;
/*   36:     */ import java.util.Queue;
/*   37:     */ import java.util.concurrent.Executor;
/*   38:     */ 
/*   39:     */ public abstract class AbstractEpollStreamChannel
/*   40:     */   extends AbstractEpollChannel
/*   41:     */   implements DuplexChannel
/*   42:     */ {
/*   43:  59 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/*   44:  60 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*   45:  61 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*   46:  62 */     StringUtil.simpleClassName(DefaultFileRegion.class) + ')';
/*   47:  63 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractEpollStreamChannel.class);
/*   48:  65 */   private static final ClosedChannelException CLEAR_SPLICE_QUEUE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollStreamChannel.class, "clearSpliceQueue()");
/*   49:  67 */   private static final ClosedChannelException SPLICE_TO_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollStreamChannel.class, "spliceTo(...)");
/*   50:  71 */   private static final ClosedChannelException FAIL_SPLICE_IF_CLOSED_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollStreamChannel.class, "failSpliceIfClosed(...)");
/*   51:  73 */   private final Runnable flushTask = new Runnable()
/*   52:     */   {
/*   53:     */     public void run()
/*   54:     */     {
/*   55:  76 */       AbstractEpollStreamChannel.this.flush();
/*   56:     */     }
/*   57:     */   };
/*   58:     */   private Queue<SpliceInTask> spliceQueue;
/*   59:     */   private FileDescriptor pipeIn;
/*   60:     */   private FileDescriptor pipeOut;
/*   61:     */   private WritableByteChannel byteChannel;
/*   62:     */   
/*   63:     */   protected AbstractEpollStreamChannel(Channel parent, int fd)
/*   64:     */   {
/*   65:  88 */     this(parent, new LinuxSocket(fd));
/*   66:     */   }
/*   67:     */   
/*   68:     */   protected AbstractEpollStreamChannel(int fd)
/*   69:     */   {
/*   70:  92 */     this(new LinuxSocket(fd));
/*   71:     */   }
/*   72:     */   
/*   73:     */   AbstractEpollStreamChannel(LinuxSocket fd)
/*   74:     */   {
/*   75:  96 */     this(fd, isSoErrorZero(fd));
/*   76:     */   }
/*   77:     */   
/*   78:     */   AbstractEpollStreamChannel(Channel parent, LinuxSocket fd)
/*   79:     */   {
/*   80: 100 */     super(parent, fd, Native.EPOLLIN, true);
/*   81:     */     
/*   82: 102 */     this.flags |= Native.EPOLLRDHUP;
/*   83:     */   }
/*   84:     */   
/*   85:     */   AbstractEpollStreamChannel(Channel parent, LinuxSocket fd, SocketAddress remote)
/*   86:     */   {
/*   87: 106 */     super(parent, fd, Native.EPOLLIN, remote);
/*   88:     */     
/*   89: 108 */     this.flags |= Native.EPOLLRDHUP;
/*   90:     */   }
/*   91:     */   
/*   92:     */   protected AbstractEpollStreamChannel(LinuxSocket fd, boolean active)
/*   93:     */   {
/*   94: 112 */     super(null, fd, Native.EPOLLIN, active);
/*   95:     */     
/*   96: 114 */     this.flags |= Native.EPOLLRDHUP;
/*   97:     */   }
/*   98:     */   
/*   99:     */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/*  100:     */   {
/*  101: 119 */     return new EpollStreamUnsafe();
/*  102:     */   }
/*  103:     */   
/*  104:     */   public ChannelMetadata metadata()
/*  105:     */   {
/*  106: 124 */     return METADATA;
/*  107:     */   }
/*  108:     */   
/*  109:     */   public final ChannelFuture spliceTo(AbstractEpollStreamChannel ch, int len)
/*  110:     */   {
/*  111: 142 */     return spliceTo(ch, len, newPromise());
/*  112:     */   }
/*  113:     */   
/*  114:     */   public final ChannelFuture spliceTo(AbstractEpollStreamChannel ch, int len, ChannelPromise promise)
/*  115:     */   {
/*  116: 161 */     if (ch.eventLoop() != eventLoop()) {
/*  117: 162 */       throw new IllegalArgumentException("EventLoops are not the same.");
/*  118:     */     }
/*  119: 164 */     if (len < 0) {
/*  120: 165 */       throw new IllegalArgumentException("len: " + len + " (expected: >= 0)");
/*  121:     */     }
/*  122: 167 */     if ((ch.config().getEpollMode() != EpollMode.LEVEL_TRIGGERED) || 
/*  123: 168 */       (config().getEpollMode() != EpollMode.LEVEL_TRIGGERED)) {
/*  124: 169 */       throw new IllegalStateException("spliceTo() supported only when using " + EpollMode.LEVEL_TRIGGERED);
/*  125:     */     }
/*  126: 171 */     ObjectUtil.checkNotNull(promise, "promise");
/*  127: 172 */     if (!isOpen())
/*  128:     */     {
/*  129: 173 */       promise.tryFailure(SPLICE_TO_CLOSED_CHANNEL_EXCEPTION);
/*  130:     */     }
/*  131:     */     else
/*  132:     */     {
/*  133: 175 */       addToSpliceQueue(new SpliceInChannelTask(ch, len, promise));
/*  134: 176 */       failSpliceIfClosed(promise);
/*  135:     */     }
/*  136: 178 */     return promise;
/*  137:     */   }
/*  138:     */   
/*  139:     */   public final ChannelFuture spliceTo(FileDescriptor ch, int offset, int len)
/*  140:     */   {
/*  141: 196 */     return spliceTo(ch, offset, len, newPromise());
/*  142:     */   }
/*  143:     */   
/*  144:     */   public final ChannelFuture spliceTo(FileDescriptor ch, int offset, int len, ChannelPromise promise)
/*  145:     */   {
/*  146: 215 */     if (len < 0) {
/*  147: 216 */       throw new IllegalArgumentException("len: " + len + " (expected: >= 0)");
/*  148:     */     }
/*  149: 218 */     if (offset < 0) {
/*  150: 219 */       throw new IllegalArgumentException("offset must be >= 0 but was " + offset);
/*  151:     */     }
/*  152: 221 */     if (config().getEpollMode() != EpollMode.LEVEL_TRIGGERED) {
/*  153: 222 */       throw new IllegalStateException("spliceTo() supported only when using " + EpollMode.LEVEL_TRIGGERED);
/*  154:     */     }
/*  155: 224 */     ObjectUtil.checkNotNull(promise, "promise");
/*  156: 225 */     if (!isOpen())
/*  157:     */     {
/*  158: 226 */       promise.tryFailure(SPLICE_TO_CLOSED_CHANNEL_EXCEPTION);
/*  159:     */     }
/*  160:     */     else
/*  161:     */     {
/*  162: 228 */       addToSpliceQueue(new SpliceFdTask(ch, offset, len, promise));
/*  163: 229 */       failSpliceIfClosed(promise);
/*  164:     */     }
/*  165: 231 */     return promise;
/*  166:     */   }
/*  167:     */   
/*  168:     */   private void failSpliceIfClosed(ChannelPromise promise)
/*  169:     */   {
/*  170: 235 */     if (!isOpen()) {
/*  171: 238 */       if (promise.tryFailure(FAIL_SPLICE_IF_CLOSED_CLOSED_CHANNEL_EXCEPTION)) {
/*  172: 239 */         eventLoop().execute(new Runnable()
/*  173:     */         {
/*  174:     */           public void run()
/*  175:     */           {
/*  176: 243 */             AbstractEpollStreamChannel.this.clearSpliceQueue();
/*  177:     */           }
/*  178:     */         });
/*  179:     */       }
/*  180:     */     }
/*  181:     */   }
/*  182:     */   
/*  183:     */   private int writeBytes(ChannelOutboundBuffer in, ByteBuf buf)
/*  184:     */     throws Exception
/*  185:     */   {
/*  186: 265 */     int readableBytes = buf.readableBytes();
/*  187: 266 */     if (readableBytes == 0)
/*  188:     */     {
/*  189: 267 */       in.remove();
/*  190: 268 */       return 0;
/*  191:     */     }
/*  192: 271 */     if ((buf.hasMemoryAddress()) || (buf.nioBufferCount() == 1)) {
/*  193: 272 */       return doWriteBytes(in, buf);
/*  194:     */     }
/*  195: 274 */     ByteBuffer[] nioBuffers = buf.nioBuffers();
/*  196: 275 */     return writeBytesMultiple(in, nioBuffers, nioBuffers.length, readableBytes, 
/*  197: 276 */       config().getMaxBytesPerGatheringWrite());
/*  198:     */   }
/*  199:     */   
/*  200:     */   private void adjustMaxBytesPerGatheringWrite(long attempted, long written, long oldMaxBytesPerGatheringWrite)
/*  201:     */   {
/*  202: 284 */     if (attempted == written)
/*  203:     */     {
/*  204: 285 */       if (attempted << 1 > oldMaxBytesPerGatheringWrite) {
/*  205: 286 */         config().setMaxBytesPerGatheringWrite(attempted << 1);
/*  206:     */       }
/*  207:     */     }
/*  208: 288 */     else if ((attempted > 4096L) && (written < attempted >>> 1)) {
/*  209: 289 */       config().setMaxBytesPerGatheringWrite(attempted >>> 1);
/*  210:     */     }
/*  211:     */   }
/*  212:     */   
/*  213:     */   private int writeBytesMultiple(ChannelOutboundBuffer in, IovArray array)
/*  214:     */     throws IOException
/*  215:     */   {
/*  216: 309 */     long expectedWrittenBytes = array.size();
/*  217: 310 */     assert (expectedWrittenBytes != 0L);
/*  218: 311 */     int cnt = array.count();
/*  219: 312 */     assert (cnt != 0);
/*  220:     */     
/*  221: 314 */     long localWrittenBytes = this.socket.writevAddresses(array.memoryAddress(0), cnt);
/*  222: 315 */     if (localWrittenBytes > 0L)
/*  223:     */     {
/*  224: 316 */       adjustMaxBytesPerGatheringWrite(expectedWrittenBytes, localWrittenBytes, array.maxBytes());
/*  225: 317 */       in.removeBytes(localWrittenBytes);
/*  226: 318 */       return 1;
/*  227:     */     }
/*  228: 320 */     return 2147483647;
/*  229:     */   }
/*  230:     */   
/*  231:     */   private int writeBytesMultiple(ChannelOutboundBuffer in, ByteBuffer[] nioBuffers, int nioBufferCnt, long expectedWrittenBytes, long maxBytesPerGatheringWrite)
/*  232:     */     throws IOException
/*  233:     */   {
/*  234: 344 */     assert (expectedWrittenBytes != 0L);
/*  235: 345 */     if (expectedWrittenBytes > maxBytesPerGatheringWrite) {
/*  236: 346 */       expectedWrittenBytes = maxBytesPerGatheringWrite;
/*  237:     */     }
/*  238: 349 */     long localWrittenBytes = this.socket.writev(nioBuffers, 0, nioBufferCnt, expectedWrittenBytes);
/*  239: 350 */     if (localWrittenBytes > 0L)
/*  240:     */     {
/*  241: 351 */       adjustMaxBytesPerGatheringWrite(expectedWrittenBytes, localWrittenBytes, maxBytesPerGatheringWrite);
/*  242: 352 */       in.removeBytes(localWrittenBytes);
/*  243: 353 */       return 1;
/*  244:     */     }
/*  245: 355 */     return 2147483647;
/*  246:     */   }
/*  247:     */   
/*  248:     */   private int writeDefaultFileRegion(ChannelOutboundBuffer in, DefaultFileRegion region)
/*  249:     */     throws Exception
/*  250:     */   {
/*  251: 373 */     long regionCount = region.count();
/*  252: 374 */     if (region.transferred() >= regionCount)
/*  253:     */     {
/*  254: 375 */       in.remove();
/*  255: 376 */       return 0;
/*  256:     */     }
/*  257: 379 */     long offset = region.transferred();
/*  258: 380 */     long flushedAmount = this.socket.sendFile(region, region.position(), offset, regionCount - offset);
/*  259: 381 */     if (flushedAmount > 0L)
/*  260:     */     {
/*  261: 382 */       in.progress(flushedAmount);
/*  262: 383 */       if (region.transferred() >= regionCount) {
/*  263: 384 */         in.remove();
/*  264:     */       }
/*  265: 386 */       return 1;
/*  266:     */     }
/*  267: 388 */     return 2147483647;
/*  268:     */   }
/*  269:     */   
/*  270:     */   private int writeFileRegion(ChannelOutboundBuffer in, FileRegion region)
/*  271:     */     throws Exception
/*  272:     */   {
/*  273: 406 */     if (region.transferred() >= region.count())
/*  274:     */     {
/*  275: 407 */       in.remove();
/*  276: 408 */       return 0;
/*  277:     */     }
/*  278: 411 */     if (this.byteChannel == null) {
/*  279: 412 */       this.byteChannel = new EpollSocketWritableByteChannel();
/*  280:     */     }
/*  281: 414 */     long flushedAmount = region.transferTo(this.byteChannel, region.transferred());
/*  282: 415 */     if (flushedAmount > 0L)
/*  283:     */     {
/*  284: 416 */       in.progress(flushedAmount);
/*  285: 417 */       if (region.transferred() >= region.count()) {
/*  286: 418 */         in.remove();
/*  287:     */       }
/*  288: 420 */       return 1;
/*  289:     */     }
/*  290: 422 */     return 2147483647;
/*  291:     */   }
/*  292:     */   
/*  293:     */   protected void doWrite(ChannelOutboundBuffer in)
/*  294:     */     throws Exception
/*  295:     */   {
/*  296: 427 */     int writeSpinCount = config().getWriteSpinCount();
/*  297:     */     do
/*  298:     */     {
/*  299: 429 */       int msgCount = in.size();
/*  300: 431 */       if ((msgCount > 1) && ((in.current() instanceof ByteBuf)))
/*  301:     */       {
/*  302: 432 */         writeSpinCount -= doWriteMultiple(in);
/*  303:     */       }
/*  304:     */       else
/*  305:     */       {
/*  306: 433 */         if (msgCount == 0)
/*  307:     */         {
/*  308: 435 */           clearFlag(Native.EPOLLOUT);
/*  309:     */           
/*  310: 437 */           return;
/*  311:     */         }
/*  312: 439 */         writeSpinCount -= doWriteSingle(in);
/*  313:     */       }
/*  314: 445 */     } while (writeSpinCount > 0);
/*  315: 447 */     if (writeSpinCount == 0) {
/*  316: 449 */       eventLoop().execute(this.flushTask);
/*  317:     */     } else {
/*  318: 453 */       setFlag(Native.EPOLLOUT);
/*  319:     */     }
/*  320:     */   }
/*  321:     */   
/*  322:     */   protected int doWriteSingle(ChannelOutboundBuffer in)
/*  323:     */     throws Exception
/*  324:     */   {
/*  325: 473 */     Object msg = in.current();
/*  326: 474 */     if ((msg instanceof ByteBuf)) {
/*  327: 475 */       return writeBytes(in, (ByteBuf)msg);
/*  328:     */     }
/*  329: 476 */     if ((msg instanceof DefaultFileRegion)) {
/*  330: 477 */       return writeDefaultFileRegion(in, (DefaultFileRegion)msg);
/*  331:     */     }
/*  332: 478 */     if ((msg instanceof FileRegion)) {
/*  333: 479 */       return writeFileRegion(in, (FileRegion)msg);
/*  334:     */     }
/*  335: 480 */     if ((msg instanceof SpliceOutTask))
/*  336:     */     {
/*  337: 481 */       if (!((SpliceOutTask)msg).spliceOut()) {
/*  338: 482 */         return 2147483647;
/*  339:     */       }
/*  340: 484 */       in.remove();
/*  341: 485 */       return 1;
/*  342:     */     }
/*  343: 488 */     throw new Error();
/*  344:     */   }
/*  345:     */   
/*  346:     */   private int doWriteMultiple(ChannelOutboundBuffer in)
/*  347:     */     throws Exception
/*  348:     */   {
/*  349: 507 */     long maxBytesPerGatheringWrite = config().getMaxBytesPerGatheringWrite();
/*  350: 508 */     if (PlatformDependent.hasUnsafe())
/*  351:     */     {
/*  352: 509 */       IovArray array = ((EpollEventLoop)eventLoop()).cleanArray();
/*  353: 510 */       array.maxBytes(maxBytesPerGatheringWrite);
/*  354: 511 */       in.forEachFlushedMessage(array);
/*  355: 513 */       if (array.count() >= 1) {
/*  356: 515 */         return writeBytesMultiple(in, array);
/*  357:     */       }
/*  358:     */     }
/*  359:     */     else
/*  360:     */     {
/*  361: 518 */       ByteBuffer[] buffers = in.nioBuffers();
/*  362: 519 */       int cnt = in.nioBufferCount();
/*  363: 520 */       if (cnt >= 1) {
/*  364: 522 */         return writeBytesMultiple(in, buffers, cnt, in.nioBufferSize(), maxBytesPerGatheringWrite);
/*  365:     */       }
/*  366:     */     }
/*  367: 526 */     in.removeBytes(0L);
/*  368: 527 */     return 0;
/*  369:     */   }
/*  370:     */   
/*  371:     */   protected Object filterOutboundMessage(Object msg)
/*  372:     */   {
/*  373: 532 */     if ((msg instanceof ByteBuf))
/*  374:     */     {
/*  375: 533 */       ByteBuf buf = (ByteBuf)msg;
/*  376: 534 */       return UnixChannelUtil.isBufferCopyNeededForWrite(buf) ? newDirectBuffer(buf) : buf;
/*  377:     */     }
/*  378: 537 */     if (((msg instanceof FileRegion)) || ((msg instanceof SpliceOutTask))) {
/*  379: 538 */       return msg;
/*  380:     */     }
/*  381: 542 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/*  382:     */   }
/*  383:     */   
/*  384:     */   protected final void doShutdownOutput()
/*  385:     */     throws Exception
/*  386:     */   {
/*  387: 548 */     this.socket.shutdown(false, true);
/*  388:     */   }
/*  389:     */   
/*  390:     */   private void shutdownInput0(ChannelPromise promise)
/*  391:     */   {
/*  392:     */     try
/*  393:     */     {
/*  394: 553 */       this.socket.shutdown(true, false);
/*  395: 554 */       promise.setSuccess();
/*  396:     */     }
/*  397:     */     catch (Throwable cause)
/*  398:     */     {
/*  399: 556 */       promise.setFailure(cause);
/*  400:     */     }
/*  401:     */   }
/*  402:     */   
/*  403:     */   public boolean isOutputShutdown()
/*  404:     */   {
/*  405: 562 */     return this.socket.isOutputShutdown();
/*  406:     */   }
/*  407:     */   
/*  408:     */   public boolean isInputShutdown()
/*  409:     */   {
/*  410: 567 */     return this.socket.isInputShutdown();
/*  411:     */   }
/*  412:     */   
/*  413:     */   public boolean isShutdown()
/*  414:     */   {
/*  415: 572 */     return this.socket.isShutdown();
/*  416:     */   }
/*  417:     */   
/*  418:     */   public ChannelFuture shutdownOutput()
/*  419:     */   {
/*  420: 577 */     return shutdownOutput(newPromise());
/*  421:     */   }
/*  422:     */   
/*  423:     */   public ChannelFuture shutdownOutput(final ChannelPromise promise)
/*  424:     */   {
/*  425: 582 */     EventLoop loop = eventLoop();
/*  426: 583 */     if (loop.inEventLoop()) {
/*  427: 584 */       ((AbstractChannel.AbstractUnsafe)unsafe()).shutdownOutput(promise);
/*  428:     */     } else {
/*  429: 586 */       loop.execute(new Runnable()
/*  430:     */       {
/*  431:     */         public void run()
/*  432:     */         {
/*  433: 589 */           ((AbstractChannel.AbstractUnsafe)AbstractEpollStreamChannel.this.unsafe()).shutdownOutput(promise);
/*  434:     */         }
/*  435:     */       });
/*  436:     */     }
/*  437: 594 */     return promise;
/*  438:     */   }
/*  439:     */   
/*  440:     */   public ChannelFuture shutdownInput()
/*  441:     */   {
/*  442: 599 */     return shutdownInput(newPromise());
/*  443:     */   }
/*  444:     */   
/*  445:     */   public ChannelFuture shutdownInput(final ChannelPromise promise)
/*  446:     */   {
/*  447: 604 */     Executor closeExecutor = ((EpollStreamUnsafe)unsafe()).prepareToClose();
/*  448: 605 */     if (closeExecutor != null)
/*  449:     */     {
/*  450: 606 */       closeExecutor.execute(new Runnable()
/*  451:     */       {
/*  452:     */         public void run()
/*  453:     */         {
/*  454: 609 */           AbstractEpollStreamChannel.this.shutdownInput0(promise);
/*  455:     */         }
/*  456:     */       });
/*  457:     */     }
/*  458:     */     else
/*  459:     */     {
/*  460: 613 */       EventLoop loop = eventLoop();
/*  461: 614 */       if (loop.inEventLoop()) {
/*  462: 615 */         shutdownInput0(promise);
/*  463:     */       } else {
/*  464: 617 */         loop.execute(new Runnable()
/*  465:     */         {
/*  466:     */           public void run()
/*  467:     */           {
/*  468: 620 */             AbstractEpollStreamChannel.this.shutdownInput0(promise);
/*  469:     */           }
/*  470:     */         });
/*  471:     */       }
/*  472:     */     }
/*  473: 625 */     return promise;
/*  474:     */   }
/*  475:     */   
/*  476:     */   public ChannelFuture shutdown()
/*  477:     */   {
/*  478: 630 */     return shutdown(newPromise());
/*  479:     */   }
/*  480:     */   
/*  481:     */   public ChannelFuture shutdown(final ChannelPromise promise)
/*  482:     */   {
/*  483: 635 */     ChannelFuture shutdownOutputFuture = shutdownOutput();
/*  484: 636 */     if (shutdownOutputFuture.isDone()) {
/*  485: 637 */       shutdownOutputDone(shutdownOutputFuture, promise);
/*  486:     */     } else {
/*  487: 639 */       shutdownOutputFuture.addListener(new ChannelFutureListener()
/*  488:     */       {
/*  489:     */         public void operationComplete(ChannelFuture shutdownOutputFuture)
/*  490:     */           throws Exception
/*  491:     */         {
/*  492: 642 */           AbstractEpollStreamChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
/*  493:     */         }
/*  494:     */       });
/*  495:     */     }
/*  496: 646 */     return promise;
/*  497:     */   }
/*  498:     */   
/*  499:     */   private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture, final ChannelPromise promise)
/*  500:     */   {
/*  501: 650 */     ChannelFuture shutdownInputFuture = shutdownInput();
/*  502: 651 */     if (shutdownInputFuture.isDone()) {
/*  503: 652 */       shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/*  504:     */     } else {
/*  505: 654 */       shutdownInputFuture.addListener(new ChannelFutureListener()
/*  506:     */       {
/*  507:     */         public void operationComplete(ChannelFuture shutdownInputFuture)
/*  508:     */           throws Exception
/*  509:     */         {
/*  510: 657 */           AbstractEpollStreamChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/*  511:     */         }
/*  512:     */       });
/*  513:     */     }
/*  514:     */   }
/*  515:     */   
/*  516:     */   private static void shutdownDone(ChannelFuture shutdownOutputFuture, ChannelFuture shutdownInputFuture, ChannelPromise promise)
/*  517:     */   {
/*  518: 666 */     Throwable shutdownOutputCause = shutdownOutputFuture.cause();
/*  519: 667 */     Throwable shutdownInputCause = shutdownInputFuture.cause();
/*  520: 668 */     if (shutdownOutputCause != null)
/*  521:     */     {
/*  522: 669 */       if (shutdownInputCause != null) {
/*  523: 670 */         logger.debug("Exception suppressed because a previous exception occurred.", shutdownInputCause);
/*  524:     */       }
/*  525: 673 */       promise.setFailure(shutdownOutputCause);
/*  526:     */     }
/*  527: 674 */     else if (shutdownInputCause != null)
/*  528:     */     {
/*  529: 675 */       promise.setFailure(shutdownInputCause);
/*  530:     */     }
/*  531:     */     else
/*  532:     */     {
/*  533: 677 */       promise.setSuccess();
/*  534:     */     }
/*  535:     */   }
/*  536:     */   
/*  537:     */   protected void doClose()
/*  538:     */     throws Exception
/*  539:     */   {
/*  540:     */     try
/*  541:     */     {
/*  542: 685 */       super.doClose();
/*  543:     */       
/*  544: 687 */       safeClosePipe(this.pipeIn);
/*  545: 688 */       safeClosePipe(this.pipeOut);
/*  546: 689 */       clearSpliceQueue();
/*  547:     */     }
/*  548:     */     finally
/*  549:     */     {
/*  550: 687 */       safeClosePipe(this.pipeIn);
/*  551: 688 */       safeClosePipe(this.pipeOut);
/*  552: 689 */       clearSpliceQueue();
/*  553:     */     }
/*  554:     */   }
/*  555:     */   
/*  556:     */   private void clearSpliceQueue()
/*  557:     */   {
/*  558: 694 */     if (this.spliceQueue == null) {
/*  559:     */       return;
/*  560:     */     }
/*  561:     */     for (;;)
/*  562:     */     {
/*  563: 698 */       SpliceInTask task = (SpliceInTask)this.spliceQueue.poll();
/*  564: 699 */       if (task == null) {
/*  565:     */         break;
/*  566:     */       }
/*  567: 702 */       task.promise.tryFailure(CLEAR_SPLICE_QUEUE_CLOSED_CHANNEL_EXCEPTION);
/*  568:     */     }
/*  569:     */   }
/*  570:     */   
/*  571:     */   private static void safeClosePipe(FileDescriptor fd)
/*  572:     */   {
/*  573: 707 */     if (fd != null) {
/*  574:     */       try
/*  575:     */       {
/*  576: 709 */         fd.close();
/*  577:     */       }
/*  578:     */       catch (IOException e)
/*  579:     */       {
/*  580: 711 */         if (logger.isWarnEnabled()) {
/*  581: 712 */           logger.warn("Error while closing a pipe", e);
/*  582:     */         }
/*  583:     */       }
/*  584:     */     }
/*  585:     */   }
/*  586:     */   
/*  587:     */   class EpollStreamUnsafe
/*  588:     */     extends AbstractEpollChannel.AbstractEpollUnsafe
/*  589:     */   {
/*  590:     */     EpollStreamUnsafe()
/*  591:     */     {
/*  592: 718 */       super();
/*  593:     */     }
/*  594:     */     
/*  595:     */     protected Executor prepareToClose()
/*  596:     */     {
/*  597: 722 */       return super.prepareToClose();
/*  598:     */     }
/*  599:     */     
/*  600:     */     private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, EpollRecvByteAllocatorHandle allocHandle)
/*  601:     */     {
/*  602: 727 */       if (byteBuf != null) {
/*  603: 728 */         if (byteBuf.isReadable())
/*  604:     */         {
/*  605: 729 */           this.readPending = false;
/*  606: 730 */           pipeline.fireChannelRead(byteBuf);
/*  607:     */         }
/*  608:     */         else
/*  609:     */         {
/*  610: 732 */           byteBuf.release();
/*  611:     */         }
/*  612:     */       }
/*  613: 735 */       allocHandle.readComplete();
/*  614: 736 */       pipeline.fireChannelReadComplete();
/*  615: 737 */       pipeline.fireExceptionCaught(cause);
/*  616: 738 */       if ((close) || ((cause instanceof IOException))) {
/*  617: 739 */         shutdownInput(false);
/*  618:     */       }
/*  619:     */     }
/*  620:     */     
/*  621:     */     EpollRecvByteAllocatorHandle newEpollHandle(RecvByteBufAllocator.ExtendedHandle handle)
/*  622:     */     {
/*  623: 745 */       return new EpollRecvByteAllocatorStreamingHandle(handle);
/*  624:     */     }
/*  625:     */     
/*  626:     */     void epollInReady()
/*  627:     */     {
/*  628: 750 */       ChannelConfig config = AbstractEpollStreamChannel.this.config();
/*  629: 751 */       if (AbstractEpollStreamChannel.this.shouldBreakEpollInReady(config))
/*  630:     */       {
/*  631: 752 */         clearEpollIn0();
/*  632: 753 */         return;
/*  633:     */       }
/*  634: 755 */       EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/*  635: 756 */       allocHandle.edgeTriggered(AbstractEpollStreamChannel.this.isFlagSet(Native.EPOLLET));
/*  636:     */       
/*  637: 758 */       ChannelPipeline pipeline = AbstractEpollStreamChannel.this.pipeline();
/*  638: 759 */       ByteBufAllocator allocator = config.getAllocator();
/*  639: 760 */       allocHandle.reset(config);
/*  640: 761 */       epollInBefore();
/*  641:     */       
/*  642: 763 */       ByteBuf byteBuf = null;
/*  643: 764 */       boolean close = false;
/*  644:     */       try
/*  645:     */       {
/*  646:     */         do
/*  647:     */         {
/*  648: 767 */           if (AbstractEpollStreamChannel.this.spliceQueue != null)
/*  649:     */           {
/*  650: 768 */             AbstractEpollStreamChannel.SpliceInTask spliceTask = (AbstractEpollStreamChannel.SpliceInTask)AbstractEpollStreamChannel.this.spliceQueue.peek();
/*  651: 769 */             if (spliceTask != null)
/*  652:     */             {
/*  653: 770 */               if (!spliceTask.spliceIn(allocHandle)) {
/*  654:     */                 break;
/*  655:     */               }
/*  656: 773 */               if (!AbstractEpollStreamChannel.this.isActive()) {
/*  657:     */                 continue;
/*  658:     */               }
/*  659: 774 */               AbstractEpollStreamChannel.this.spliceQueue.remove(); continue;
/*  660:     */             }
/*  661:     */           }
/*  662: 785 */           byteBuf = allocHandle.allocate(allocator);
/*  663: 786 */           allocHandle.lastBytesRead(AbstractEpollStreamChannel.this.doReadBytes(byteBuf));
/*  664: 787 */           if (allocHandle.lastBytesRead() <= 0)
/*  665:     */           {
/*  666: 789 */             byteBuf.release();
/*  667: 790 */             byteBuf = null;
/*  668: 791 */             close = allocHandle.lastBytesRead() < 0;
/*  669: 792 */             if (close) {
/*  670: 794 */               this.readPending = false;
/*  671:     */             }
/*  672:     */           }
/*  673:     */           else
/*  674:     */           {
/*  675: 798 */             allocHandle.incMessagesRead(1);
/*  676: 799 */             this.readPending = false;
/*  677: 800 */             pipeline.fireChannelRead(byteBuf);
/*  678: 801 */             byteBuf = null;
/*  679: 803 */             if (AbstractEpollStreamChannel.this.shouldBreakEpollInReady(config)) {
/*  680:     */               break;
/*  681:     */             }
/*  682:     */           }
/*  683: 817 */         } while (allocHandle.continueReading());
/*  684: 819 */         allocHandle.readComplete();
/*  685: 820 */         pipeline.fireChannelReadComplete();
/*  686: 822 */         if (close) {
/*  687: 823 */           shutdownInput(false);
/*  688:     */         }
/*  689:     */       }
/*  690:     */       catch (Throwable t)
/*  691:     */       {
/*  692: 826 */         handleReadException(pipeline, byteBuf, t, close, allocHandle);
/*  693:     */       }
/*  694:     */       finally
/*  695:     */       {
/*  696: 828 */         epollInFinally(config);
/*  697:     */       }
/*  698:     */     }
/*  699:     */   }
/*  700:     */   
/*  701:     */   private void addToSpliceQueue(final SpliceInTask task)
/*  702:     */   {
/*  703: 834 */     EventLoop eventLoop = eventLoop();
/*  704: 835 */     if (eventLoop.inEventLoop()) {
/*  705: 836 */       addToSpliceQueue0(task);
/*  706:     */     } else {
/*  707: 838 */       eventLoop.execute(new Runnable()
/*  708:     */       {
/*  709:     */         public void run()
/*  710:     */         {
/*  711: 841 */           AbstractEpollStreamChannel.this.addToSpliceQueue0(task);
/*  712:     */         }
/*  713:     */       });
/*  714:     */     }
/*  715:     */   }
/*  716:     */   
/*  717:     */   private void addToSpliceQueue0(SpliceInTask task)
/*  718:     */   {
/*  719: 848 */     if (this.spliceQueue == null) {
/*  720: 849 */       this.spliceQueue = PlatformDependent.newMpscQueue();
/*  721:     */     }
/*  722: 851 */     this.spliceQueue.add(task);
/*  723:     */   }
/*  724:     */   
/*  725:     */   protected abstract class SpliceInTask
/*  726:     */   {
/*  727:     */     final ChannelPromise promise;
/*  728:     */     int len;
/*  729:     */     
/*  730:     */     protected SpliceInTask(int len, ChannelPromise promise)
/*  731:     */     {
/*  732: 859 */       this.promise = promise;
/*  733: 860 */       this.len = len;
/*  734:     */     }
/*  735:     */     
/*  736:     */     abstract boolean spliceIn(RecvByteBufAllocator.Handle paramHandle);
/*  737:     */     
/*  738:     */     protected final int spliceIn(FileDescriptor pipeOut, RecvByteBufAllocator.Handle handle)
/*  739:     */       throws IOException
/*  740:     */     {
/*  741: 867 */       int length = Math.min(handle.guess(), this.len);
/*  742: 868 */       int splicedIn = 0;
/*  743:     */       for (;;)
/*  744:     */       {
/*  745: 871 */         int localSplicedIn = Native.splice(AbstractEpollStreamChannel.this.socket.intValue(), -1L, pipeOut.intValue(), -1L, length);
/*  746: 872 */         if (localSplicedIn == 0) {
/*  747:     */           break;
/*  748:     */         }
/*  749: 875 */         splicedIn += localSplicedIn;
/*  750: 876 */         length -= localSplicedIn;
/*  751:     */       }
/*  752: 879 */       return splicedIn;
/*  753:     */     }
/*  754:     */   }
/*  755:     */   
/*  756:     */   private final class SpliceInChannelTask
/*  757:     */     extends AbstractEpollStreamChannel.SpliceInTask
/*  758:     */     implements ChannelFutureListener
/*  759:     */   {
/*  760:     */     private final AbstractEpollStreamChannel ch;
/*  761:     */     
/*  762:     */     SpliceInChannelTask(AbstractEpollStreamChannel ch, int len, ChannelPromise promise)
/*  763:     */     {
/*  764: 888 */       super(len, promise);
/*  765: 889 */       this.ch = ch;
/*  766:     */     }
/*  767:     */     
/*  768:     */     public void operationComplete(ChannelFuture future)
/*  769:     */       throws Exception
/*  770:     */     {
/*  771: 894 */       if (!future.isSuccess()) {
/*  772: 895 */         this.promise.setFailure(future.cause());
/*  773:     */       }
/*  774:     */     }
/*  775:     */     
/*  776:     */     public boolean spliceIn(RecvByteBufAllocator.Handle handle)
/*  777:     */     {
/*  778: 901 */       assert (this.ch.eventLoop().inEventLoop());
/*  779: 902 */       if (this.len == 0)
/*  780:     */       {
/*  781: 903 */         this.promise.setSuccess();
/*  782: 904 */         return true;
/*  783:     */       }
/*  784:     */       try
/*  785:     */       {
/*  786: 910 */         FileDescriptor pipeOut = this.ch.pipeOut;
/*  787: 911 */         if (pipeOut == null)
/*  788:     */         {
/*  789: 913 */           FileDescriptor[] pipe = FileDescriptor.pipe();
/*  790: 914 */           this.ch.pipeIn = pipe[0];
/*  791: 915 */           pipeOut = this.ch.pipeOut = pipe[1];
/*  792:     */         }
/*  793: 918 */         int splicedIn = spliceIn(pipeOut, handle);
/*  794: 919 */         if (splicedIn > 0)
/*  795:     */         {
/*  796: 921 */           if (this.len != 2147483647) {
/*  797: 922 */             this.len -= splicedIn;
/*  798:     */           }
/*  799:     */           ChannelPromise splicePromise;
/*  800:     */           ChannelPromise splicePromise;
/*  801: 928 */           if (this.len == 0) {
/*  802: 929 */             splicePromise = this.promise;
/*  803:     */           } else {
/*  804: 931 */             splicePromise = this.ch.newPromise().addListener(this);
/*  805:     */           }
/*  806: 934 */           boolean autoRead = AbstractEpollStreamChannel.this.config().isAutoRead();
/*  807:     */           
/*  808:     */ 
/*  809:     */ 
/*  810: 938 */           this.ch.unsafe().write(new AbstractEpollStreamChannel.SpliceOutTask(AbstractEpollStreamChannel.this, this.ch, splicedIn, autoRead), splicePromise);
/*  811: 939 */           this.ch.unsafe().flush();
/*  812: 940 */           if ((autoRead) && (!splicePromise.isDone())) {
/*  813: 945 */             AbstractEpollStreamChannel.this.config().setAutoRead(false);
/*  814:     */           }
/*  815:     */         }
/*  816: 949 */         return this.len == 0;
/*  817:     */       }
/*  818:     */       catch (Throwable cause)
/*  819:     */       {
/*  820: 951 */         this.promise.setFailure(cause);
/*  821:     */       }
/*  822: 952 */       return true;
/*  823:     */     }
/*  824:     */   }
/*  825:     */   
/*  826:     */   private final class SpliceOutTask
/*  827:     */   {
/*  828:     */     private final AbstractEpollStreamChannel ch;
/*  829:     */     private final boolean autoRead;
/*  830:     */     private int len;
/*  831:     */     
/*  832:     */     SpliceOutTask(AbstractEpollStreamChannel ch, int len, boolean autoRead)
/*  833:     */     {
/*  834: 963 */       this.ch = ch;
/*  835: 964 */       this.len = len;
/*  836: 965 */       this.autoRead = autoRead;
/*  837:     */     }
/*  838:     */     
/*  839:     */     public boolean spliceOut()
/*  840:     */       throws Exception
/*  841:     */     {
/*  842: 969 */       assert (this.ch.eventLoop().inEventLoop());
/*  843:     */       try
/*  844:     */       {
/*  845: 971 */         int splicedOut = Native.splice(this.ch.pipeIn.intValue(), -1L, this.ch.socket.intValue(), -1L, this.len);
/*  846: 972 */         this.len -= splicedOut;
/*  847: 973 */         if (this.len == 0)
/*  848:     */         {
/*  849: 974 */           if (this.autoRead) {
/*  850: 976 */             AbstractEpollStreamChannel.this.config().setAutoRead(true);
/*  851:     */           }
/*  852: 978 */           return true;
/*  853:     */         }
/*  854: 980 */         return false;
/*  855:     */       }
/*  856:     */       catch (IOException e)
/*  857:     */       {
/*  858: 982 */         if (this.autoRead) {
/*  859: 984 */           AbstractEpollStreamChannel.this.config().setAutoRead(true);
/*  860:     */         }
/*  861: 986 */         throw e;
/*  862:     */       }
/*  863:     */     }
/*  864:     */   }
/*  865:     */   
/*  866:     */   private final class SpliceFdTask
/*  867:     */     extends AbstractEpollStreamChannel.SpliceInTask
/*  868:     */   {
/*  869:     */     private final FileDescriptor fd;
/*  870:     */     private final ChannelPromise promise;
/*  871:     */     private final int offset;
/*  872:     */     
/*  873:     */     SpliceFdTask(FileDescriptor fd, int offset, int len, ChannelPromise promise)
/*  874:     */     {
/*  875: 997 */       super(len, promise);
/*  876: 998 */       this.fd = fd;
/*  877: 999 */       this.promise = promise;
/*  878:1000 */       this.offset = offset;
/*  879:     */     }
/*  880:     */     
/*  881:     */     public boolean spliceIn(RecvByteBufAllocator.Handle handle)
/*  882:     */     {
/*  883:1005 */       assert (AbstractEpollStreamChannel.this.eventLoop().inEventLoop());
/*  884:1006 */       if (this.len == 0)
/*  885:     */       {
/*  886:1007 */         this.promise.setSuccess();
/*  887:1008 */         return true;
/*  888:     */       }
/*  889:     */       try
/*  890:     */       {
/*  891:1012 */         FileDescriptor[] pipe = FileDescriptor.pipe();
/*  892:1013 */         FileDescriptor pipeIn = pipe[0];
/*  893:1014 */         FileDescriptor pipeOut = pipe[1];
/*  894:     */         try
/*  895:     */         {
/*  896:1016 */           int splicedIn = spliceIn(pipeOut, handle);
/*  897:     */           int splicedOut;
/*  898:1017 */           if (splicedIn > 0)
/*  899:     */           {
/*  900:1019 */             if (this.len != 2147483647) {
/*  901:1020 */               this.len -= splicedIn;
/*  902:     */             }
/*  903:     */             do
/*  904:     */             {
/*  905:1023 */               splicedOut = Native.splice(pipeIn.intValue(), -1L, this.fd.intValue(), this.offset, splicedIn);
/*  906:1024 */               splicedIn -= splicedOut;
/*  907:1025 */             } while (splicedIn > 0);
/*  908:1026 */             if (this.len == 0)
/*  909:     */             {
/*  910:1027 */               this.promise.setSuccess();
/*  911:1028 */               return 1;
/*  912:     */             }
/*  913:     */           }
/*  914:1031 */           return 0;
/*  915:     */         }
/*  916:     */         finally
/*  917:     */         {
/*  918:1033 */           AbstractEpollStreamChannel.safeClosePipe(pipeIn);
/*  919:1034 */           AbstractEpollStreamChannel.safeClosePipe(pipeOut);
/*  920:     */         }
/*  921:1038 */         return true;
/*  922:     */       }
/*  923:     */       catch (Throwable cause)
/*  924:     */       {
/*  925:1037 */         this.promise.setFailure(cause);
/*  926:     */       }
/*  927:     */     }
/*  928:     */   }
/*  929:     */   
/*  930:     */   private final class EpollSocketWritableByteChannel
/*  931:     */     extends SocketWritableByteChannel
/*  932:     */   {
/*  933:     */     EpollSocketWritableByteChannel()
/*  934:     */     {
/*  935:1045 */       super();
/*  936:     */     }
/*  937:     */     
/*  938:     */     protected ByteBufAllocator alloc()
/*  939:     */     {
/*  940:1050 */       return AbstractEpollStreamChannel.this.alloc();
/*  941:     */     }
/*  942:     */   }
/*  943:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.AbstractEpollStreamChannel
 * JD-Core Version:    0.7.0.1
 */