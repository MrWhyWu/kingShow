/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import java.net.Inet4Address;
/*  4:   */ import java.net.Inet6Address;
/*  5:   */ import java.util.Collections;
/*  6:   */ import java.util.HashMap;
/*  7:   */ import java.util.Map;
/*  8:   */ 
/*  9:   */ public final class HostsFileEntries
/* 10:   */ {
/* 11:35 */   static final HostsFileEntries EMPTY = new HostsFileEntries(
/* 12:   */   
/* 13:37 */     Collections.emptyMap(), 
/* 14:38 */     Collections.emptyMap());
/* 15:   */   private final Map<String, Inet4Address> inet4Entries;
/* 16:   */   private final Map<String, Inet6Address> inet6Entries;
/* 17:   */   
/* 18:   */   public HostsFileEntries(Map<String, Inet4Address> inet4Entries, Map<String, Inet6Address> inet6Entries)
/* 19:   */   {
/* 20:44 */     this.inet4Entries = Collections.unmodifiableMap(new HashMap(inet4Entries));
/* 21:45 */     this.inet6Entries = Collections.unmodifiableMap(new HashMap(inet6Entries));
/* 22:   */   }
/* 23:   */   
/* 24:   */   public Map<String, Inet4Address> inet4Entries()
/* 25:   */   {
/* 26:53 */     return this.inet4Entries;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public Map<String, Inet6Address> inet6Entries()
/* 30:   */   {
/* 31:61 */     return this.inet6Entries;
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.HostsFileEntries
 * JD-Core Version:    0.7.0.1
 */