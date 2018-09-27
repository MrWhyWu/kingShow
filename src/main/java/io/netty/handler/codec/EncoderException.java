/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class EncoderException
/*  4:   */   extends CodecException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -5086121160476476774L;
/*  7:   */   
/*  8:   */   public EncoderException() {}
/*  9:   */   
/* 10:   */   public EncoderException(String message, Throwable cause)
/* 11:   */   {
/* 12:35 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public EncoderException(String message)
/* 16:   */   {
/* 17:42 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public EncoderException(Throwable cause)
/* 21:   */   {
/* 22:49 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.EncoderException
 * JD-Core Version:    0.7.0.1
 */