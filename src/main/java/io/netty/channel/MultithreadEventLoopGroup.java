/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.NettyRuntime;
/*  4:   */ import io.netty.util.concurrent.DefaultThreadFactory;
/*  5:   */ import io.netty.util.concurrent.EventExecutorChooserFactory;
/*  6:   */ import io.netty.util.concurrent.MultithreadEventExecutorGroup;
/*  7:   */ import io.netty.util.internal.SystemPropertyUtil;
/*  8:   */ import io.netty.util.internal.logging.InternalLogger;
/*  9:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/* 10:   */ import java.util.concurrent.Executor;
/* 11:   */ import java.util.concurrent.ThreadFactory;
/* 12:   */ 
/* 13:   */ public abstract class MultithreadEventLoopGroup
/* 14:   */   extends MultithreadEventExecutorGroup
/* 15:   */   implements EventLoopGroup
/* 16:   */ {
/* 17:35 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultithreadEventLoopGroup.class);
/* 18:40 */   private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", 
/* 19:41 */     NettyRuntime.availableProcessors() * 2));
/* 20:   */   
/* 21:   */   static
/* 22:   */   {
/* 23:43 */     if (logger.isDebugEnabled()) {
/* 24:44 */       logger.debug("-Dio.netty.eventLoopThreads: {}", Integer.valueOf(DEFAULT_EVENT_LOOP_THREADS));
/* 25:   */     }
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args)
/* 29:   */   {
/* 30:52 */     super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
/* 31:   */   }
/* 32:   */   
/* 33:   */   protected MultithreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args)
/* 34:   */   {
/* 35:59 */     super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, threadFactory, args);
/* 36:   */   }
/* 37:   */   
/* 38:   */   protected MultithreadEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args)
/* 39:   */   {
/* 40:68 */     super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, chooserFactory, args);
/* 41:   */   }
/* 42:   */   
/* 43:   */   protected ThreadFactory newDefaultThreadFactory()
/* 44:   */   {
/* 45:73 */     return new DefaultThreadFactory(getClass(), 10);
/* 46:   */   }
/* 47:   */   
/* 48:   */   public EventLoop next()
/* 49:   */   {
/* 50:78 */     return (EventLoop)super.next();
/* 51:   */   }
/* 52:   */   
/* 53:   */   public ChannelFuture register(Channel channel)
/* 54:   */   {
/* 55:86 */     return next().register(channel);
/* 56:   */   }
/* 57:   */   
/* 58:   */   public ChannelFuture register(ChannelPromise promise)
/* 59:   */   {
/* 60:91 */     return next().register(promise);
/* 61:   */   }
/* 62:   */   
/* 63:   */   @Deprecated
/* 64:   */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 65:   */   {
/* 66:97 */     return next().register(channel, promise);
/* 67:   */   }
/* 68:   */   
/* 69:   */   protected abstract EventLoop newChild(Executor paramExecutor, Object... paramVarArgs)
/* 70:   */     throws Exception;
/* 71:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.MultithreadEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */