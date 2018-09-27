/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public class ChannelPipelineException
/*  4:   */   extends ChannelException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 3379174210419885980L;
/*  7:   */   
/*  8:   */   public ChannelPipelineException() {}
/*  9:   */   
/* 10:   */   public ChannelPipelineException(String message, Throwable cause)
/* 11:   */   {
/* 12:36 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ChannelPipelineException(String message)
/* 16:   */   {
/* 17:43 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ChannelPipelineException(Throwable cause)
/* 21:   */   {
/* 22:50 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelPipelineException
 * JD-Core Version:    0.7.0.1
 */