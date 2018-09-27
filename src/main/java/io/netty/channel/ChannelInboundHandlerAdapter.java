/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ public class ChannelInboundHandlerAdapter
/*   4:    */   extends ChannelHandlerAdapter
/*   5:    */   implements ChannelInboundHandler
/*   6:    */ {
/*   7:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*   8:    */     throws Exception
/*   9:    */   {
/*  10: 42 */     ctx.fireChannelRegistered();
/*  11:    */   }
/*  12:    */   
/*  13:    */   public void channelUnregistered(ChannelHandlerContext ctx)
/*  14:    */     throws Exception
/*  15:    */   {
/*  16: 53 */     ctx.fireChannelUnregistered();
/*  17:    */   }
/*  18:    */   
/*  19:    */   public void channelActive(ChannelHandlerContext ctx)
/*  20:    */     throws Exception
/*  21:    */   {
/*  22: 64 */     ctx.fireChannelActive();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  26:    */     throws Exception
/*  27:    */   {
/*  28: 75 */     ctx.fireChannelInactive();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  32:    */     throws Exception
/*  33:    */   {
/*  34: 86 */     ctx.fireChannelRead(msg);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40: 97 */     ctx.fireChannelReadComplete();
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/*  44:    */     throws Exception
/*  45:    */   {
/*  46:108 */     ctx.fireUserEventTriggered(evt);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/*  50:    */     throws Exception
/*  51:    */   {
/*  52:119 */     ctx.fireChannelWritabilityChanged();
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  56:    */     throws Exception
/*  57:    */   {
/*  58:131 */     ctx.fireExceptionCaught(cause);
/*  59:    */   }
/*  60:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelInboundHandlerAdapter
 * JD-Core Version:    0.7.0.1
 */