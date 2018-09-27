/*  1:   */ package io.netty.channel.local;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.ChannelException;
/*  5:   */ import io.netty.util.internal.PlatformDependent;
/*  6:   */ import io.netty.util.internal.StringUtil;
/*  7:   */ import java.net.SocketAddress;
/*  8:   */ import java.util.concurrent.ConcurrentMap;
/*  9:   */ 
/* 10:   */ final class LocalChannelRegistry
/* 11:   */ {
/* 12:28 */   private static final ConcurrentMap<LocalAddress, Channel> boundChannels = ;
/* 13:   */   
/* 14:   */   static LocalAddress register(Channel channel, LocalAddress oldLocalAddress, SocketAddress localAddress)
/* 15:   */   {
/* 16:32 */     if (oldLocalAddress != null) {
/* 17:33 */       throw new ChannelException("already bound");
/* 18:   */     }
/* 19:35 */     if (!(localAddress instanceof LocalAddress)) {
/* 20:36 */       throw new ChannelException("unsupported address type: " + StringUtil.simpleClassName(localAddress));
/* 21:   */     }
/* 22:39 */     LocalAddress addr = (LocalAddress)localAddress;
/* 23:40 */     if (LocalAddress.ANY.equals(addr)) {
/* 24:41 */       addr = new LocalAddress(channel);
/* 25:   */     }
/* 26:44 */     Channel boundChannel = (Channel)boundChannels.putIfAbsent(addr, channel);
/* 27:45 */     if (boundChannel != null) {
/* 28:46 */       throw new ChannelException("address already in use by: " + boundChannel);
/* 29:   */     }
/* 30:48 */     return addr;
/* 31:   */   }
/* 32:   */   
/* 33:   */   static Channel get(SocketAddress localAddress)
/* 34:   */   {
/* 35:52 */     return (Channel)boundChannels.get(localAddress);
/* 36:   */   }
/* 37:   */   
/* 38:   */   static void unregister(LocalAddress localAddress)
/* 39:   */   {
/* 40:56 */     boundChannels.remove(localAddress);
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.local.LocalChannelRegistry
 * JD-Core Version:    0.7.0.1
 */