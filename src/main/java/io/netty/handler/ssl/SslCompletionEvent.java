/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ 
/*  5:   */ public abstract class SslCompletionEvent
/*  6:   */ {
/*  7:   */   private final Throwable cause;
/*  8:   */   
/*  9:   */   SslCompletionEvent()
/* 10:   */   {
/* 11:25 */     this.cause = null;
/* 12:   */   }
/* 13:   */   
/* 14:   */   SslCompletionEvent(Throwable cause)
/* 15:   */   {
/* 16:29 */     this.cause = ((Throwable)ObjectUtil.checkNotNull(cause, "cause"));
/* 17:   */   }
/* 18:   */   
/* 19:   */   public final boolean isSuccess()
/* 20:   */   {
/* 21:36 */     return this.cause == null;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public final Throwable cause()
/* 25:   */   {
/* 26:44 */     return this.cause;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public String toString()
/* 30:   */   {
/* 31:49 */     Throwable cause = cause();
/* 32:50 */     return 
/* 33:51 */       getClass().getSimpleName() + '(' + cause + ')';
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslCompletionEvent
 * JD-Core Version:    0.7.0.1
 */