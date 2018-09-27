/*   1:    */ package io.netty.channel.epoll;
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
/*  14:    */ public final class EpollEventLoopGroup
/*  15:    */   extends MultithreadEventLoopGroup
/*  16:    */ {
/*  17:    */   public EpollEventLoopGroup()
/*  18:    */   {
/*  19: 45 */     this(0);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public EpollEventLoopGroup(int nThreads)
/*  23:    */   {
/*  24: 52 */     this(nThreads, (ThreadFactory)null);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public EpollEventLoopGroup(int nThreads, SelectStrategyFactory selectStrategyFactory)
/*  28:    */   {
/*  29: 60 */     this(nThreads, (ThreadFactory)null, selectStrategyFactory);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/*  33:    */   {
/*  34: 68 */     this(nThreads, threadFactory, 0);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public EpollEventLoopGroup(int nThreads, Executor executor)
/*  38:    */   {
/*  39: 72 */     this(nThreads, executor, DefaultSelectStrategyFactory.INSTANCE);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectStrategyFactory selectStrategyFactory)
/*  43:    */   {
/*  44: 80 */     this(nThreads, threadFactory, 0, selectStrategyFactory);
/*  45:    */   }
/*  46:    */   
/*  47:    */   @Deprecated
/*  48:    */   public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce)
/*  49:    */   {
/*  50: 91 */     this(nThreads, threadFactory, maxEventsAtOnce, DefaultSelectStrategyFactory.INSTANCE);
/*  51:    */   }
/*  52:    */   
/*  53:    */   @Deprecated
/*  54:    */   public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce, SelectStrategyFactory selectStrategyFactory)
/*  55:    */   {
/*  56:104 */     super(nThreads, threadFactory, new Object[] { Integer.valueOf(maxEventsAtOnce), selectStrategyFactory, RejectedExecutionHandlers.reject() });Epoll.ensureAvailability();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public EpollEventLoopGroup(int nThreads, Executor executor, SelectStrategyFactory selectStrategyFactory)
/*  60:    */   {
/*  61:108 */     super(nThreads, executor, new Object[] { Integer.valueOf(0), selectStrategyFactory, RejectedExecutionHandlers.reject() });Epoll.ensureAvailability();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory)
/*  65:    */   {
/*  66:113 */     super(nThreads, executor, chooserFactory, new Object[] { Integer.valueOf(0), selectStrategyFactory, RejectedExecutionHandlers.reject() });Epoll.ensureAvailability();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler)
/*  70:    */   {
/*  71:119 */     super(nThreads, executor, chooserFactory, new Object[] { Integer.valueOf(0), selectStrategyFactory, rejectedExecutionHandler });Epoll.ensureAvailability();
/*  72:    */   }
/*  73:    */   
/*  74:    */   public void setIoRatio(int ioRatio)
/*  75:    */   {
/*  76:127 */     for (EventExecutor e : this) {
/*  77:128 */       ((EpollEventLoop)e).setIoRatio(ioRatio);
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected EventLoop newChild(Executor executor, Object... args)
/*  82:    */     throws Exception
/*  83:    */   {
/*  84:134 */     return new EpollEventLoop(this, executor, ((Integer)args[0]).intValue(), ((SelectStrategyFactory)args[1])
/*  85:135 */       .newSelectStrategy(), (RejectedExecutionHandler)args[2]);
/*  86:    */   }
/*  87:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */