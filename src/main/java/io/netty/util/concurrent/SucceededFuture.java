/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ public final class SucceededFuture<V>
/*  4:   */   extends CompleteFuture<V>
/*  5:   */ {
/*  6:   */   private final V result;
/*  7:   */   
/*  8:   */   public SucceededFuture(EventExecutor executor, V result)
/*  9:   */   {
/* 10:32 */     super(executor);
/* 11:33 */     this.result = result;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public Throwable cause()
/* 15:   */   {
/* 16:38 */     return null;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public boolean isSuccess()
/* 20:   */   {
/* 21:43 */     return true;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public V getNow()
/* 25:   */   {
/* 26:48 */     return this.result;
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.SucceededFuture
 * JD-Core Version:    0.7.0.1
 */