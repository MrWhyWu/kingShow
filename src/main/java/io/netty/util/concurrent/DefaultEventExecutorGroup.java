/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.Executor;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ public class DefaultEventExecutorGroup
/*  7:   */   extends MultithreadEventExecutorGroup
/*  8:   */ {
/*  9:   */   public DefaultEventExecutorGroup(int nThreads)
/* 10:   */   {
/* 11:30 */     this(nThreads, null);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public DefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory)
/* 15:   */   {
/* 16:40 */     this(nThreads, threadFactory, SingleThreadEventExecutor.DEFAULT_MAX_PENDING_EXECUTOR_TASKS, 
/* 17:41 */       RejectedExecutionHandlers.reject());
/* 18:   */   }
/* 19:   */   
/* 20:   */   public DefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedHandler)
/* 21:   */   {
/* 22:54 */     super(nThreads, threadFactory, new Object[] { Integer.valueOf(maxPendingTasks), rejectedHandler });
/* 23:   */   }
/* 24:   */   
/* 25:   */   protected EventExecutor newChild(Executor executor, Object... args)
/* 26:   */     throws Exception
/* 27:   */   {
/* 28:59 */     return new DefaultEventExecutor(this, executor, ((Integer)args[0]).intValue(), (RejectedExecutionHandler)args[1]);
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultEventExecutorGroup
 * JD-Core Version:    0.7.0.1
 */