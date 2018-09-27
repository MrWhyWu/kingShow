/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class PrematureChannelClosureException
/*  4:   */   extends CodecException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 4907642202594703094L;
/*  7:   */   
/*  8:   */   public PrematureChannelClosureException() {}
/*  9:   */   
/* 10:   */   public PrematureChannelClosureException(String message, Throwable cause)
/* 11:   */   {
/* 12:38 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public PrematureChannelClosureException(String message)
/* 16:   */   {
/* 17:45 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public PrematureChannelClosureException(Throwable cause)
/* 21:   */   {
/* 22:52 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.PrematureChannelClosureException
 * JD-Core Version:    0.7.0.1
 */