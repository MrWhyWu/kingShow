/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import java.net.Inet4Address;
/*  4:   */ import java.net.Inet6Address;
/*  5:   */ import java.net.InetAddress;
/*  6:   */ import java.util.Locale;
/*  7:   */ import java.util.Map;
/*  8:   */ 
/*  9:   */ public final class DefaultHostsFileEntriesResolver
/* 10:   */   implements HostsFileEntriesResolver
/* 11:   */ {
/* 12:   */   private final Map<String, Inet4Address> inet4Entries;
/* 13:   */   private final Map<String, Inet6Address> inet6Entries;
/* 14:   */   
/* 15:   */   public DefaultHostsFileEntriesResolver()
/* 16:   */   {
/* 17:36 */     this(HostsFileParser.parseSilently());
/* 18:   */   }
/* 19:   */   
/* 20:   */   DefaultHostsFileEntriesResolver(HostsFileEntries entries)
/* 21:   */   {
/* 22:41 */     this.inet4Entries = entries.inet4Entries();
/* 23:42 */     this.inet6Entries = entries.inet6Entries();
/* 24:   */   }
/* 25:   */   
/* 26:   */   public InetAddress address(String inetHost, ResolvedAddressTypes resolvedAddressTypes)
/* 27:   */   {
/* 28:47 */     String normalized = normalize(inetHost);
/* 29:48 */     switch (1.$SwitchMap$io$netty$resolver$ResolvedAddressTypes[resolvedAddressTypes.ordinal()])
/* 30:   */     {
/* 31:   */     case 1: 
/* 32:50 */       return (InetAddress)this.inet4Entries.get(normalized);
/* 33:   */     case 2: 
/* 34:52 */       return (InetAddress)this.inet6Entries.get(normalized);
/* 35:   */     case 3: 
/* 36:54 */       Inet4Address inet4Address = (Inet4Address)this.inet4Entries.get(normalized);
/* 37:55 */       return inet4Address != null ? inet4Address : (InetAddress)this.inet6Entries.get(normalized);
/* 38:   */     case 4: 
/* 39:57 */       Inet6Address inet6Address = (Inet6Address)this.inet6Entries.get(normalized);
/* 40:58 */       return inet6Address != null ? inet6Address : (InetAddress)this.inet4Entries.get(normalized);
/* 41:   */     }
/* 42:60 */     throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
/* 43:   */   }
/* 44:   */   
/* 45:   */   String normalize(String inetHost)
/* 46:   */   {
/* 47:66 */     return inetHost.toLowerCase(Locale.ENGLISH);
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.DefaultHostsFileEntriesResolver
 * JD-Core Version:    0.7.0.1
 */