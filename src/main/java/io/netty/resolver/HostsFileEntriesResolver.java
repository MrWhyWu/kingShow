/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import java.net.InetAddress;
/*  4:   */ 
/*  5:   */ public abstract interface HostsFileEntriesResolver
/*  6:   */ {
/*  7:31 */   public static final HostsFileEntriesResolver DEFAULT = new DefaultHostsFileEntriesResolver();
/*  8:   */   
/*  9:   */   public abstract InetAddress address(String paramString, ResolvedAddressTypes paramResolvedAddressTypes);
/* 10:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.HostsFileEntriesResolver
 * JD-Core Version:    0.7.0.1
 */