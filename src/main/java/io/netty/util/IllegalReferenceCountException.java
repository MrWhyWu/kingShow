/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ public class IllegalReferenceCountException
/*  4:   */   extends IllegalStateException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -2507492394288153468L;
/*  7:   */   
/*  8:   */   public IllegalReferenceCountException() {}
/*  9:   */   
/* 10:   */   public IllegalReferenceCountException(int refCnt)
/* 11:   */   {
/* 12:30 */     this("refCnt: " + refCnt);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public IllegalReferenceCountException(int refCnt, int increment)
/* 16:   */   {
/* 17:34 */     this("refCnt: " + refCnt + ", " + (increment > 0 ? "increment: " + increment : new StringBuilder().append("decrement: ").append(-increment).toString()));
/* 18:   */   }
/* 19:   */   
/* 20:   */   public IllegalReferenceCountException(String message)
/* 21:   */   {
/* 22:38 */     super(message);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public IllegalReferenceCountException(String message, Throwable cause)
/* 26:   */   {
/* 27:42 */     super(message, cause);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public IllegalReferenceCountException(Throwable cause)
/* 31:   */   {
/* 32:46 */     super(cause);
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.IllegalReferenceCountException
 * JD-Core Version:    0.7.0.1
 */