/*  1:   */ package io.netty.handler.codec.compression;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.EncoderException;
/*  4:   */ 
/*  5:   */ public class CompressionException
/*  6:   */   extends EncoderException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 5603413481274811897L;
/*  9:   */   
/* 10:   */   public CompressionException() {}
/* 11:   */   
/* 12:   */   public CompressionException(String message, Throwable cause)
/* 13:   */   {
/* 14:37 */     super(message, cause);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public CompressionException(String message)
/* 18:   */   {
/* 19:44 */     super(message);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public CompressionException(Throwable cause)
/* 23:   */   {
/* 24:51 */     super(cause);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.CompressionException
 * JD-Core Version:    0.7.0.1
 */