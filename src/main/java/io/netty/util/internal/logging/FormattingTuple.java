/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ final class FormattingTuple
/*  4:   */ {
/*  5:   */   private final String message;
/*  6:   */   private final Throwable throwable;
/*  7:   */   
/*  8:   */   FormattingTuple(String message, Throwable throwable)
/*  9:   */   {
/* 10:51 */     this.message = message;
/* 11:52 */     this.throwable = throwable;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public String getMessage()
/* 15:   */   {
/* 16:56 */     return this.message;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public Throwable getThrowable()
/* 20:   */   {
/* 21:60 */     return this.throwable;
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.FormattingTuple
 * JD-Core Version:    0.7.0.1
 */