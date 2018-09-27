/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import javax.net.ssl.SSLException;
/*  4:   */ 
/*  5:   */ public class NotSslRecordException
/*  6:   */   extends SSLException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = -4316784434770656841L;
/*  9:   */   
/* 10:   */   public NotSslRecordException()
/* 11:   */   {
/* 12:33 */     super("");
/* 13:   */   }
/* 14:   */   
/* 15:   */   public NotSslRecordException(String message)
/* 16:   */   {
/* 17:37 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public NotSslRecordException(Throwable cause)
/* 21:   */   {
/* 22:41 */     super(cause);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public NotSslRecordException(String message, Throwable cause)
/* 26:   */   {
/* 27:45 */     super(message, cause);
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.NotSslRecordException
 * JD-Core Version:    0.7.0.1
 */