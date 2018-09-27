/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ 
/*  5:   */ final class SucceededChannelFuture
/*  6:   */   extends CompleteChannelFuture
/*  7:   */ {
/*  8:   */   SucceededChannelFuture(Channel channel, EventExecutor executor)
/*  9:   */   {
/* 10:33 */     super(channel, executor);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public Throwable cause()
/* 14:   */   {
/* 15:38 */     return null;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public boolean isSuccess()
/* 19:   */   {
/* 20:43 */     return true;
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.SucceededChannelFuture
 * JD-Core Version:    0.7.0.1
 */