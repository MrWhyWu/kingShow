/*  1:   */ package io.netty.handler.ipfilter;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.ChannelFuture;
/*  5:   */ import io.netty.channel.ChannelFutureListener;
/*  6:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  7:   */ import io.netty.channel.ChannelHandlerContext;
/*  8:   */ import io.netty.util.internal.ConcurrentSet;
/*  9:   */ import java.net.InetAddress;
/* 10:   */ import java.net.InetSocketAddress;
/* 11:   */ import java.util.Set;
/* 12:   */ 
/* 13:   */ @ChannelHandler.Sharable
/* 14:   */ public class UniqueIpFilter
/* 15:   */   extends AbstractRemoteAddressFilter<InetSocketAddress>
/* 16:   */ {
/* 17:36 */   private final Set<InetAddress> connected = new ConcurrentSet();
/* 18:   */   
/* 19:   */   protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress)
/* 20:   */     throws Exception
/* 21:   */   {
/* 22:40 */     final InetAddress remoteIp = remoteAddress.getAddress();
/* 23:41 */     if (this.connected.contains(remoteIp)) {
/* 24:42 */       return false;
/* 25:   */     }
/* 26:44 */     this.connected.add(remoteIp);
/* 27:45 */     ctx.channel().closeFuture().addListener(new ChannelFutureListener()
/* 28:   */     {
/* 29:   */       public void operationComplete(ChannelFuture future)
/* 30:   */         throws Exception
/* 31:   */       {
/* 32:48 */         UniqueIpFilter.this.connected.remove(remoteIp);
/* 33:   */       }
/* 34:50 */     });
/* 35:51 */     return true;
/* 36:   */   }
/* 37:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ipfilter.UniqueIpFilter
 * JD-Core Version:    0.7.0.1
 */