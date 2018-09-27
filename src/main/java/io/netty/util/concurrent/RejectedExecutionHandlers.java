/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import java.util.concurrent.RejectedExecutionException;
/*  5:   */ import java.util.concurrent.TimeUnit;
/*  6:   */ import java.util.concurrent.locks.LockSupport;
/*  7:   */ 
/*  8:   */ public final class RejectedExecutionHandlers
/*  9:   */ {
/* 10:28 */   private static final RejectedExecutionHandler REJECT = new RejectedExecutionHandler()
/* 11:   */   {
/* 12:   */     public void rejected(Runnable task, SingleThreadEventExecutor executor)
/* 13:   */     {
/* 14:31 */       throw new RejectedExecutionException();
/* 15:   */     }
/* 16:   */   };
/* 17:   */   
/* 18:   */   public static RejectedExecutionHandler reject()
/* 19:   */   {
/* 20:41 */     return REJECT;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public static RejectedExecutionHandler backoff(int retries, long backoffAmount, TimeUnit unit)
/* 24:   */   {
/* 25:50 */     ObjectUtil.checkPositive(retries, "retries");
/* 26:51 */     final long backOffNanos = unit.toNanos(backoffAmount);
/* 27:52 */     new RejectedExecutionHandler()
/* 28:   */     {
/* 29:   */       public void rejected(Runnable task, SingleThreadEventExecutor executor)
/* 30:   */       {
/* 31:55 */         if (!executor.inEventLoop()) {
/* 32:56 */           for (int i = 0; i < this.val$retries; i++)
/* 33:   */           {
/* 34:58 */             executor.wakeup(false);
/* 35:   */             
/* 36:60 */             LockSupport.parkNanos(backOffNanos);
/* 37:61 */             if (executor.offerTask(task)) {
/* 38:62 */               return;
/* 39:   */             }
/* 40:   */           }
/* 41:   */         }
/* 42:68 */         throw new RejectedExecutionException();
/* 43:   */       }
/* 44:   */     };
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.RejectedExecutionHandlers
 * JD-Core Version:    0.7.0.1
 */