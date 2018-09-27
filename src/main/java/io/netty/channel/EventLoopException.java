/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public class EventLoopException
/*  4:   */   extends ChannelException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = -8969100344583703616L;
/*  7:   */   
/*  8:   */   public EventLoopException() {}
/*  9:   */   
/* 10:   */   public EventLoopException(String message, Throwable cause)
/* 11:   */   {
/* 12:30 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public EventLoopException(String message)
/* 16:   */   {
/* 17:34 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public EventLoopException(Throwable cause)
/* 21:   */   {
/* 22:38 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.EventLoopException
 * JD-Core Version:    0.7.0.1
 */