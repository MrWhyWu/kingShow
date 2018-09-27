/*  1:   */ package io.netty.handler.codec.compression;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.DecoderException;
/*  4:   */ 
/*  5:   */ public class DecompressionException
/*  6:   */   extends DecoderException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 3546272712208105199L;
/*  9:   */   
/* 10:   */   public DecompressionException() {}
/* 11:   */   
/* 12:   */   public DecompressionException(String message, Throwable cause)
/* 13:   */   {
/* 14:37 */     super(message, cause);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public DecompressionException(String message)
/* 18:   */   {
/* 19:44 */     super(message);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public DecompressionException(Throwable cause)
/* 23:   */   {
/* 24:51 */     super(cause);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.DecompressionException
 * JD-Core Version:    0.7.0.1
 */