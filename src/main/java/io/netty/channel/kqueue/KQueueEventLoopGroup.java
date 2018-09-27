/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.channel.DefaultSelectStrategyFactory;
/*   4:    */ import io.netty.channel.EventLoop;
/*   5:    */ import io.netty.channel.MultithreadEventLoopGroup;
/*   6:    */ import io.netty.channel.SelectStrategyFactory;
/*   7:    */ import io.netty.util.concurrent.EventExecutor;
/*   8:    */ import io.netty.util.concurrent.EventExecutorChooserFactory;
/*   9:    */ import io.netty.util.concurrent.RejectedExecutionHandler;
/*  10:    */ import io.netty.util.concurrent.RejectedExecutionHandlers;
/*  11:    */ import java.util.concurrent.Executor;
/*  12:    */ import java.util.concurrent.ThreadFactory;
/*  13:    */ 
/*  14:    */ public final class KQueueEventLoopGroup
/*  15:    */   extends MultithreadEventLoopGroup
/*  16:    */ {
/*  17:    */   public KQueueEventLoopGroup()
/*  18:    */   {
/*  19: 41 */     this(0);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public KQueueEventLoopGroup(int nThreads)
/*  23:    */   {
/*  24: 48 */     this(nThreads, (ThreadFactory)null);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public KQueueEventLoopGroup(int nThreads, SelectStrategyFactory selectStrategyFactory)
/*  28:    */   {
/*  29: 56 */     this(nThreads, (ThreadFactory)null, selectStrategyFactory);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/*  33:    */   {
/*  34: 64 */     this(nThreads, threadFactory, 0);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public KQueueEventLoopGroup(int nThreads, Executor executor)
/*  38:    */   {
/*  39: 68 */     this(nThreads, executor, DefaultSelectStrategyFactory.INSTANCE);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectStrategyFactory selectStrategyFactory)
/*  43:    */   {
/*  44: 77 */     this(nThreads, threadFactory, 0, selectStrategyFactory);
/*  45:    */   }
/*  46:    */   
/*  47:    */   @Deprecated
/*  48:    */   public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce)
/*  49:    */   {
/*  50: 88 */     this(nThreads, threadFactory, maxEventsAtOnce, DefaultSelectStrategyFactory.INSTANCE);
/*  51:    */   }
/*  52:    */   
/*  53:    */   @Deprecated
/*  54:    */   public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce, SelectStrategyFactory selectStrategyFactory)
/*  55:    */   {
/*  56:101 */     super(nThreads, threadFactory, new Object[] { Integer.valueOf(maxEventsAtOnce), selectStrategyFactory, RejectedExecutionHandlers.reject() });KQueue.ensureAvailability();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public KQueueEventLoopGroup(int nThreads, Executor executor, SelectStrategyFactory selectStrategyFactory)
/*  60:    */   {
/*  61:105 */     super(nThreads, executor, new Object[] { Integer.valueOf(0), selectStrategyFactory, RejectedExecutionHandlers.reject() });KQueue.ensureAvailability();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public KQueueEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory)
/*  65:    */   {
/*  66:110 */     super(nThreads, executor, chooserFactory, new Object[] { Integer.valueOf(0), selectStrategyFactory, RejectedExecutionHandlers.reject() });KQueue.ensureAvailability();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public KQueueEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler)
/*  70:    */   {
/*  71:116 */     super(nThreads, executor, chooserFactory, new Object[] { Integer.valueOf(0), selectStrategyFactory, rejectedExecutionHandler });KQueue.ensureAvailability();
/*  72:    */   }
/*  73:    */   
/*  74:    */   public void setIoRatio(int ioRatio)
/*  75:    */   {
/*  76:124 */     for (EventExecutor e : this) {
/*  77:125 */       ((KQueueEventLoop)e).setIoRatio(ioRatio);
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected EventLoop newChild(Executor executor, Object... args)
/*  82:    */     throws Exception
/*  83:    */   {
/*  84:131 */     return new KQueueEventLoop(this, executor, ((Integer)args[0]).intValue(), ((SelectStrategyFactory)args[1])
/*  85:132 */       .newSelectStrategy(), (RejectedExecutionHandler)args[2]);
/*  86:    */   }
/*  87:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */