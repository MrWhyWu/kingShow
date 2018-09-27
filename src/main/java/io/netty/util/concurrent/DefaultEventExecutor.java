/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.Executor;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ public final class DefaultEventExecutor
/*  7:   */   extends SingleThreadEventExecutor
/*  8:   */ {
/*  9:   */   public DefaultEventExecutor()
/* 10:   */   {
/* 11:28 */     this((EventExecutorGroup)null);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public DefaultEventExecutor(ThreadFactory threadFactory)
/* 15:   */   {
/* 16:32 */     this(null, threadFactory);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public DefaultEventExecutor(Executor executor)
/* 20:   */   {
/* 21:36 */     this(null, executor);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public DefaultEventExecutor(EventExecutorGroup parent)
/* 25:   */   {
/* 26:40 */     this(parent, new DefaultThreadFactory(DefaultEventExecutor.class));
/* 27:   */   }
/* 28:   */   
/* 29:   */   public DefaultEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory)
/* 30:   */   {
/* 31:44 */     super(parent, threadFactory, true);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public DefaultEventExecutor(EventExecutorGroup parent, Executor executor)
/* 35:   */   {
/* 36:48 */     super(parent, executor, true);
/* 37:   */   }
/* 38:   */   
/* 39:   */   public DefaultEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler)
/* 40:   */   {
/* 41:53 */     super(parent, threadFactory, true, maxPendingTasks, rejectedExecutionHandler);
/* 42:   */   }
/* 43:   */   
/* 44:   */   public DefaultEventExecutor(EventExecutorGroup parent, Executor executor, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler)
/* 45:   */   {
/* 46:58 */     super(parent, executor, true, maxPendingTasks, rejectedExecutionHandler);
/* 47:   */   }
/* 48:   */   
/* 49:   */   protected void run()
/* 50:   */   {
/* 51:   */     for (;;)
/* 52:   */     {
/* 53:64 */       Runnable task = takeTask();
/* 54:65 */       if (task != null)
/* 55:   */       {
/* 56:66 */         task.run();
/* 57:67 */         updateLastExecutionTime();
/* 58:   */       }
/* 59:70 */       if (confirmShutdown()) {
/* 60:   */         break;
/* 61:   */       }
/* 62:   */     }
/* 63:   */   }
/* 64:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultEventExecutor
 * JD-Core Version:    0.7.0.1
 */