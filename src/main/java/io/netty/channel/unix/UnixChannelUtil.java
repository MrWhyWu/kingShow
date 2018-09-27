/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ import java.net.InetAddress;
/*  6:   */ import java.net.InetSocketAddress;
/*  7:   */ import java.net.UnknownHostException;
/*  8:   */ 
/*  9:   */ public final class UnixChannelUtil
/* 10:   */ {
/* 11:   */   public static boolean isBufferCopyNeededForWrite(ByteBuf byteBuf)
/* 12:   */   {
/* 13:37 */     return isBufferCopyNeededForWrite(byteBuf, Limits.IOV_MAX);
/* 14:   */   }
/* 15:   */   
/* 16:   */   static boolean isBufferCopyNeededForWrite(ByteBuf byteBuf, int iovMax)
/* 17:   */   {
/* 18:41 */     return (!byteBuf.hasMemoryAddress()) && ((!byteBuf.isDirect()) || (byteBuf.nioBufferCount() > iovMax));
/* 19:   */   }
/* 20:   */   
/* 21:   */   public static InetSocketAddress computeRemoteAddr(InetSocketAddress remoteAddr, InetSocketAddress osRemoteAddr)
/* 22:   */   {
/* 23:45 */     if (osRemoteAddr != null)
/* 24:   */     {
/* 25:46 */       if (PlatformDependent.javaVersion() >= 7) {
/* 26:   */         try
/* 27:   */         {
/* 28:51 */           return new InetSocketAddress(InetAddress.getByAddress(remoteAddr.getHostString(), osRemoteAddr
/* 29:52 */             .getAddress().getAddress()), osRemoteAddr
/* 30:53 */             .getPort());
/* 31:   */         }
/* 32:   */         catch (UnknownHostException localUnknownHostException) {}
/* 33:   */       }
/* 34:58 */       return osRemoteAddr;
/* 35:   */     }
/* 36:60 */     return remoteAddr;
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.UnixChannelUtil
 * JD-Core Version:    0.7.0.1
 */