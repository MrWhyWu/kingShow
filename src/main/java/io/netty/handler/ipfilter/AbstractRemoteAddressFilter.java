/*   1:    */ package io.netty.handler.ipfilter;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.ChannelFuture;
/*   5:    */ import io.netty.channel.ChannelFutureListener;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*   8:    */ import io.netty.channel.ChannelPipeline;
/*   9:    */ import java.net.SocketAddress;
/*  10:    */ 
/*  11:    */ public abstract class AbstractRemoteAddressFilter<T extends SocketAddress>
/*  12:    */   extends ChannelInboundHandlerAdapter
/*  13:    */ {
/*  14:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*  15:    */     throws Exception
/*  16:    */   {
/*  17: 42 */     handleNewChannel(ctx);
/*  18: 43 */     ctx.fireChannelRegistered();
/*  19:    */   }
/*  20:    */   
/*  21:    */   public void channelActive(ChannelHandlerContext ctx)
/*  22:    */     throws Exception
/*  23:    */   {
/*  24: 48 */     if (!handleNewChannel(ctx)) {
/*  25: 49 */       throw new IllegalStateException("cannot determine to accept or reject a channel: " + ctx.channel());
/*  26:    */     }
/*  27: 51 */     ctx.fireChannelActive();
/*  28:    */   }
/*  29:    */   
/*  30:    */   private boolean handleNewChannel(ChannelHandlerContext ctx)
/*  31:    */     throws Exception
/*  32:    */   {
/*  33: 57 */     T remoteAddress = ctx.channel().remoteAddress();
/*  34: 60 */     if (remoteAddress == null) {
/*  35: 61 */       return false;
/*  36:    */     }
/*  37: 66 */     ctx.pipeline().remove(this);
/*  38: 68 */     if (accept(ctx, remoteAddress))
/*  39:    */     {
/*  40: 69 */       channelAccepted(ctx, remoteAddress);
/*  41:    */     }
/*  42:    */     else
/*  43:    */     {
/*  44: 71 */       ChannelFuture rejectedFuture = channelRejected(ctx, remoteAddress);
/*  45: 72 */       if (rejectedFuture != null) {
/*  46: 73 */         rejectedFuture.addListener(ChannelFutureListener.CLOSE);
/*  47:    */       } else {
/*  48: 75 */         ctx.close();
/*  49:    */       }
/*  50:    */     }
/*  51: 79 */     return true;
/*  52:    */   }
/*  53:    */   
/*  54:    */   protected abstract boolean accept(ChannelHandlerContext paramChannelHandlerContext, T paramT)
/*  55:    */     throws Exception;
/*  56:    */   
/*  57:    */   protected void channelAccepted(ChannelHandlerContext ctx, T remoteAddress) {}
/*  58:    */   
/*  59:    */   protected ChannelFuture channelRejected(ChannelHandlerContext ctx, T remoteAddress)
/*  60:    */   {
/*  61:107 */     return null;
/*  62:    */   }
/*  63:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ipfilter.AbstractRemoteAddressFilter
 * JD-Core Version:    0.7.0.1
 */