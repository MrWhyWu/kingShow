/*   1:    */ package io.netty.channel.unix;
/*   2:    */ 
/*   3:    */ import java.net.Inet6Address;
/*   4:    */ import java.net.InetAddress;
/*   5:    */ import java.net.InetSocketAddress;
/*   6:    */ import java.net.UnknownHostException;
/*   7:    */ 
/*   8:    */ public final class NativeInetAddress
/*   9:    */ {
/*  10: 27 */   private static final byte[] IPV4_MAPPED_IPV6_PREFIX = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1 };
/*  11:    */   final byte[] address;
/*  12:    */   final int scopeId;
/*  13:    */   
/*  14:    */   public static NativeInetAddress newInstance(InetAddress addr)
/*  15:    */   {
/*  16: 33 */     byte[] bytes = addr.getAddress();
/*  17: 34 */     if ((addr instanceof Inet6Address)) {
/*  18: 35 */       return new NativeInetAddress(bytes, ((Inet6Address)addr).getScopeId());
/*  19:    */     }
/*  20: 38 */     return new NativeInetAddress(ipv4MappedIpv6Address(bytes));
/*  21:    */   }
/*  22:    */   
/*  23:    */   public NativeInetAddress(byte[] address, int scopeId)
/*  24:    */   {
/*  25: 43 */     this.address = address;
/*  26: 44 */     this.scopeId = scopeId;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public NativeInetAddress(byte[] address)
/*  30:    */   {
/*  31: 48 */     this(address, 0);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public byte[] address()
/*  35:    */   {
/*  36: 52 */     return this.address;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public int scopeId()
/*  40:    */   {
/*  41: 56 */     return this.scopeId;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public static byte[] ipv4MappedIpv6Address(byte[] ipv4)
/*  45:    */   {
/*  46: 60 */     byte[] address = new byte[16];
/*  47: 61 */     System.arraycopy(IPV4_MAPPED_IPV6_PREFIX, 0, address, 0, IPV4_MAPPED_IPV6_PREFIX.length);
/*  48: 62 */     System.arraycopy(ipv4, 0, address, 12, ipv4.length);
/*  49: 63 */     return address;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public static InetSocketAddress address(byte[] addr, int offset, int len)
/*  53:    */   {
/*  54: 68 */     int port = decodeInt(addr, offset + len - 4);
/*  55:    */     try
/*  56:    */     {
/*  57:    */       InetAddress address;
/*  58:    */       InetAddress address;
/*  59: 72 */       switch (len)
/*  60:    */       {
/*  61:    */       case 8: 
/*  62: 77 */         byte[] ipv4 = new byte[4];
/*  63: 78 */         System.arraycopy(addr, offset, ipv4, 0, 4);
/*  64: 79 */         address = InetAddress.getByAddress(ipv4);
/*  65: 80 */         break;
/*  66:    */       case 24: 
/*  67: 87 */         byte[] ipv6 = new byte[16];
/*  68: 88 */         System.arraycopy(addr, offset, ipv6, 0, 16);
/*  69: 89 */         int scopeId = decodeInt(addr, offset + len - 8);
/*  70: 90 */         address = Inet6Address.getByAddress(null, ipv6, scopeId);
/*  71: 91 */         break;
/*  72:    */       default: 
/*  73: 93 */         throw new Error();
/*  74:    */       }
/*  75:    */       InetAddress address;
/*  76: 95 */       return new InetSocketAddress(address, port);
/*  77:    */     }
/*  78:    */     catch (UnknownHostException e)
/*  79:    */     {
/*  80: 97 */       throw new Error("Should never happen", e);
/*  81:    */     }
/*  82:    */   }
/*  83:    */   
/*  84:    */   static int decodeInt(byte[] addr, int index)
/*  85:    */   {
/*  86:102 */     return (addr[index] & 0xFF) << 24 | (addr[(index + 1)] & 0xFF) << 16 | (addr[(index + 2)] & 0xFF) << 8 | addr[(index + 3)] & 0xFF;
/*  87:    */   }
/*  88:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.NativeInetAddress
 * JD-Core Version:    0.7.0.1
 */