/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ 
/*  5:   */ final class FastThreadLocalRunnable
/*  6:   */   implements Runnable
/*  7:   */ {
/*  8:   */   private final Runnable runnable;
/*  9:   */   
/* 10:   */   private FastThreadLocalRunnable(Runnable runnable)
/* 11:   */   {
/* 12:24 */     this.runnable = ((Runnable)ObjectUtil.checkNotNull(runnable, "runnable"));
/* 13:   */   }
/* 14:   */   
/* 15:   */   public void run()
/* 16:   */   {
/* 17:   */     try
/* 18:   */     {
/* 19:30 */       this.runnable.run();
/* 20:   */       
/* 21:32 */       FastThreadLocal.removeAll();
/* 22:   */     }
/* 23:   */     finally
/* 24:   */     {
/* 25:32 */       FastThreadLocal.removeAll();
/* 26:   */     }
/* 27:   */   }
/* 28:   */   
/* 29:   */   static Runnable wrap(Runnable runnable)
/* 30:   */   {
/* 31:37 */     return (runnable instanceof FastThreadLocalRunnable) ? runnable : new FastThreadLocalRunnable(runnable);
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.FastThreadLocalRunnable
 * JD-Core Version:    0.7.0.1
 */