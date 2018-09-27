/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class CorruptedFrameException
/*  4:   */   extends DecoderException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 3918052232492988408L;
/*  7:   */   
/*  8:   */   public CorruptedFrameException() {}
/*  9:   */   
/* 10:   */   public CorruptedFrameException(String message, Throwable cause)
/* 11:   */   {
/* 12:36 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public CorruptedFrameException(String message)
/* 16:   */   {
/* 17:43 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public CorruptedFrameException(Throwable cause)
/* 21:   */   {
/* 22:50 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.CorruptedFrameException
 * JD-Core Version:    0.7.0.1
 */