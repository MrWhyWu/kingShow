/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.Executor;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ public final class ThreadPerTaskExecutor
/*  7:   */   implements Executor
/*  8:   */ {
/*  9:   */   private final ThreadFactory threadFactory;
/* 10:   */   
/* 11:   */   public ThreadPerTaskExecutor(ThreadFactory threadFactory)
/* 12:   */   {
/* 13:25 */     if (threadFactory == null) {
/* 14:26 */       throw new NullPointerException("threadFactory");
/* 15:   */     }
/* 16:28 */     this.threadFactory = threadFactory;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public void execute(Runnable command)
/* 20:   */   {
/* 21:33 */     this.threadFactory.newThread(command).start();
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.ThreadPerTaskExecutor
 * JD-Core Version:    0.7.0.1
 */