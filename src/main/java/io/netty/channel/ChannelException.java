/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public class ChannelException
/*  4:   */   extends RuntimeException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 2908618315971075004L;
/*  7:   */   
/*  8:   */   public ChannelException() {}
/*  9:   */   
/* 10:   */   public ChannelException(String message, Throwable cause)
/* 11:   */   {
/* 12:35 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ChannelException(String message)
/* 16:   */   {
/* 17:42 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ChannelException(Throwable cause)
/* 21:   */   {
/* 22:49 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelException
 * JD-Core Version:    0.7.0.1
 */