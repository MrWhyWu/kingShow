/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class TooLongFrameException
/*  4:   */   extends DecoderException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -1995801950698951640L;
/*  7:   */   
/*  8:   */   public TooLongFrameException() {}
/*  9:   */   
/* 10:   */   public TooLongFrameException(String message, Throwable cause)
/* 11:   */   {
/* 12:36 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public TooLongFrameException(String message)
/* 16:   */   {
/* 17:43 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public TooLongFrameException(Throwable cause)
/* 21:   */   {
/* 22:50 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.TooLongFrameException
 * JD-Core Version:    0.7.0.1
 */