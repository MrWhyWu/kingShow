/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.GenericFutureListener;
/*  4:   */ 
/*  5:   */ public abstract interface ChannelFutureListener
/*  6:   */   extends GenericFutureListener<ChannelFuture>
/*  7:   */ {
/*  8:41 */   public static final ChannelFutureListener CLOSE = new ChannelFutureListener()
/*  9:   */   {
/* 10:   */     public void operationComplete(ChannelFuture future)
/* 11:   */     {
/* 12:44 */       future.channel().close();
/* 13:   */     }
/* 14:   */   };
/* 15:52 */   public static final ChannelFutureListener CLOSE_ON_FAILURE = new ChannelFutureListener()
/* 16:   */   {
/* 17:   */     public void operationComplete(ChannelFuture future)
/* 18:   */     {
/* 19:55 */       if (!future.isSuccess()) {
/* 20:56 */         future.channel().close();
/* 21:   */       }
/* 22:   */     }
/* 23:   */   };
/* 24:65 */   public static final ChannelFutureListener FIRE_EXCEPTION_ON_FAILURE = new ChannelFutureListener()
/* 25:   */   {
/* 26:   */     public void operationComplete(ChannelFuture future)
/* 27:   */     {
/* 28:68 */       if (!future.isSuccess()) {
/* 29:69 */         future.channel().pipeline().fireExceptionCaught(future.cause());
/* 30:   */       }
/* 31:   */     }
/* 32:   */   };
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelFutureListener
 * JD-Core Version:    0.7.0.1
 */