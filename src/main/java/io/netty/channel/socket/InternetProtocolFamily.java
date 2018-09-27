/*  1:   */ package io.netty.channel.socket;
/*  2:   */ 
/*  3:   */ import io.netty.util.NetUtil;
/*  4:   */ import java.net.Inet4Address;
/*  5:   */ import java.net.Inet6Address;
/*  6:   */ import java.net.InetAddress;
/*  7:   */ 
/*  8:   */ public enum InternetProtocolFamily
/*  9:   */ {
/* 10:28 */   IPv4(Inet4Address.class, 1, NetUtil.LOCALHOST4),  IPv6(Inet6Address.class, 2, NetUtil.LOCALHOST6);
/* 11:   */   
/* 12:   */   private final Class<? extends InetAddress> addressType;
/* 13:   */   private final int addressNumber;
/* 14:   */   private final InetAddress localHost;
/* 15:   */   
/* 16:   */   private InternetProtocolFamily(Class<? extends InetAddress> addressType, int addressNumber, InetAddress localHost)
/* 17:   */   {
/* 18:36 */     this.addressType = addressType;
/* 19:37 */     this.addressNumber = addressNumber;
/* 20:38 */     this.localHost = localHost;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public Class<? extends InetAddress> addressType()
/* 24:   */   {
/* 25:45 */     return this.addressType;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public int addressNumber()
/* 29:   */   {
/* 30:54 */     return this.addressNumber;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public InetAddress localhost()
/* 34:   */   {
/* 35:61 */     return this.localHost;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public static InternetProtocolFamily of(InetAddress address)
/* 39:   */   {
/* 40:68 */     if ((address instanceof Inet4Address)) {
/* 41:69 */       return IPv4;
/* 42:   */     }
/* 43:71 */     if ((address instanceof Inet6Address)) {
/* 44:72 */       return IPv6;
/* 45:   */     }
/* 46:74 */     throw new IllegalArgumentException("address " + address + " not supported");
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.InternetProtocolFamily
 * JD-Core Version:    0.7.0.1
 */