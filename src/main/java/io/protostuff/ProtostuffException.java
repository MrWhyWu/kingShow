/*  1:   */ package io.protostuff;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ 
/*  5:   */ public class ProtostuffException
/*  6:   */   extends IOException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 3969366848110070516L;
/*  9:   */   
/* 10:   */   public ProtostuffException() {}
/* 11:   */   
/* 12:   */   public ProtostuffException(String message)
/* 13:   */   {
/* 14:37 */     super(message);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public ProtostuffException(String message, Throwable cause)
/* 18:   */   {
/* 19:42 */     super(message, cause);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public ProtostuffException(Throwable cause)
/* 23:   */   {
/* 24:47 */     super(cause);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ProtostuffException
 * JD-Core Version:    0.7.0.1
 */