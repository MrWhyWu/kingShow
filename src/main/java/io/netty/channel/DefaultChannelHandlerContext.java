/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ 
/*  5:   */ final class DefaultChannelHandlerContext
/*  6:   */   extends AbstractChannelHandlerContext
/*  7:   */ {
/*  8:   */   private final ChannelHandler handler;
/*  9:   */   
/* 10:   */   DefaultChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor, String name, ChannelHandler handler)
/* 11:   */   {
/* 12:26 */     super(pipeline, executor, name, isInbound(handler), isOutbound(handler));
/* 13:27 */     if (handler == null) {
/* 14:28 */       throw new NullPointerException("handler");
/* 15:   */     }
/* 16:30 */     this.handler = handler;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public ChannelHandler handler()
/* 20:   */   {
/* 21:35 */     return this.handler;
/* 22:   */   }
/* 23:   */   
/* 24:   */   private static boolean isInbound(ChannelHandler handler)
/* 25:   */   {
/* 26:39 */     return handler instanceof ChannelInboundHandler;
/* 27:   */   }
/* 28:   */   
/* 29:   */   private static boolean isOutbound(ChannelHandler handler)
/* 30:   */   {
/* 31:43 */     return handler instanceof ChannelOutboundHandler;
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultChannelHandlerContext
 * JD-Core Version:    0.7.0.1
 */