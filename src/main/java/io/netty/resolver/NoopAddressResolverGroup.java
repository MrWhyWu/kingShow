/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import java.net.SocketAddress;
/*  5:   */ 
/*  6:   */ public final class NoopAddressResolverGroup
/*  7:   */   extends AddressResolverGroup<SocketAddress>
/*  8:   */ {
/*  9:30 */   public static final NoopAddressResolverGroup INSTANCE = new NoopAddressResolverGroup();
/* 10:   */   
/* 11:   */   protected AddressResolver<SocketAddress> newResolver(EventExecutor executor)
/* 12:   */     throws Exception
/* 13:   */   {
/* 14:36 */     return new NoopAddressResolver(executor);
/* 15:   */   }
/* 16:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.NoopAddressResolverGroup
 * JD-Core Version:    0.7.0.1
 */