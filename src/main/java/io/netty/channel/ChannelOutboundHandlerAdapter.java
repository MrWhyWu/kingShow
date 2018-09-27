/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import java.net.SocketAddress;
/*   4:    */ 
/*   5:    */ public class ChannelOutboundHandlerAdapter
/*   6:    */   extends ChannelHandlerAdapter
/*   7:    */   implements ChannelOutboundHandler
/*   8:    */ {
/*   9:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/*  10:    */     throws Exception
/*  11:    */   {
/*  12: 35 */     ctx.bind(localAddress, promise);
/*  13:    */   }
/*  14:    */   
/*  15:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/*  16:    */     throws Exception
/*  17:    */   {
/*  18: 47 */     ctx.connect(remoteAddress, localAddress, promise);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/*  22:    */     throws Exception
/*  23:    */   {
/*  24: 59 */     ctx.disconnect(promise);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/*  28:    */     throws Exception
/*  29:    */   {
/*  30: 71 */     ctx.close(promise);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/*  34:    */     throws Exception
/*  35:    */   {
/*  36: 82 */     ctx.deregister(promise);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void read(ChannelHandlerContext ctx)
/*  40:    */     throws Exception
/*  41:    */   {
/*  42: 93 */     ctx.read();
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  46:    */     throws Exception
/*  47:    */   {
/*  48:104 */     ctx.write(msg, promise);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void flush(ChannelHandlerContext ctx)
/*  52:    */     throws Exception
/*  53:    */   {
/*  54:115 */     ctx.flush();
/*  55:    */   }
/*  56:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelOutboundHandlerAdapter
 * JD-Core Version:    0.7.0.1
 */