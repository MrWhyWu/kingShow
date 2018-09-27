/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import java.net.SocketAddress;
/*   4:    */ 
/*   5:    */ public class ChannelDuplexHandler
/*   6:    */   extends ChannelInboundHandlerAdapter
/*   7:    */   implements ChannelOutboundHandler
/*   8:    */ {
/*   9:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  10:    */     throws Exception
/*  11:    */   {
/*  12: 38 */     ctx.bind(localAddress, promise);
/*  13:    */   }
/*  14:    */   
/*  15:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  16:    */     throws Exception
/*  17:    */   {
/*  18: 50 */     ctx.connect(remoteAddress, localAddress, promise);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  22:    */     throws Exception
/*  23:    */   {
/*  24: 62 */     ctx.disconnect(promise);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  28:    */     throws Exception
/*  29:    */   {
/*  30: 73 */     ctx.close(promise);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/*  34:    */     throws Exception
/*  35:    */   {
/*  36: 84 */     ctx.deregister(promise);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void read(ChannelHandlerContext ctx)
/*  40:    */     throws Exception
/*  41:    */   {
/*  42: 95 */     ctx.read();
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  46:    */     throws Exception
/*  47:    */   {
/*  48:106 */     ctx.write(msg, promise);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void flush(ChannelHandlerContext ctx)
/*  52:    */     throws Exception
/*  53:    */   {
/*  54:117 */     ctx.flush();
/*  55:    */   }
/*  56:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelDuplexHandler
 * JD-Core Version:    0.7.0.1
 */