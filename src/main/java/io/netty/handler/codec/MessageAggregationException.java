/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ public class MessageAggregationException
/*  4:   */   extends IllegalStateException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -1995826182950310255L;
/*  7:   */   
/*  8:   */   public MessageAggregationException() {}
/*  9:   */   
/* 10:   */   public MessageAggregationException(String s)
/* 11:   */   {
/* 12:29 */     super(s);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public MessageAggregationException(String message, Throwable cause)
/* 16:   */   {
/* 17:33 */     super(message, cause);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public MessageAggregationException(Throwable cause)
/* 21:   */   {
/* 22:37 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.MessageAggregationException
 * JD-Core Version:    0.7.0.1
 */