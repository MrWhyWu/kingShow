/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import java.net.InetSocketAddress;
/*  5:   */ 
/*  6:   */ public final class DefaultAddressResolverGroup
/*  7:   */   extends AddressResolverGroup<InetSocketAddress>
/*  8:   */ {
/*  9:30 */   public static final DefaultAddressResolverGroup INSTANCE = new DefaultAddressResolverGroup();
/* 10:   */   
/* 11:   */   protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor)
/* 12:   */     throws Exception
/* 13:   */   {
/* 14:36 */     return new DefaultNameResolver(executor).asAddressResolver();
/* 15:   */   }
/* 16:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.DefaultAddressResolverGroup
 * JD-Core Version:    0.7.0.1
 */