/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ public class BlockingOperationException
/*  4:   */   extends IllegalStateException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 2462223247762460301L;
/*  7:   */   
/*  8:   */   public BlockingOperationException() {}
/*  9:   */   
/* 10:   */   public BlockingOperationException(String s)
/* 11:   */   {
/* 12:31 */     super(s);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public BlockingOperationException(Throwable cause)
/* 16:   */   {
/* 17:35 */     super(cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public BlockingOperationException(String message, Throwable cause)
/* 21:   */   {
/* 22:39 */     super(message, cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.BlockingOperationException
 * JD-Core Version:    0.7.0.1
 */