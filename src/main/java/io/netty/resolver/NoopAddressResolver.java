/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import io.netty.util.concurrent.Promise;
/*  5:   */ import java.net.SocketAddress;
/*  6:   */ import java.util.Collections;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ public class NoopAddressResolver
/* 10:   */   extends AbstractAddressResolver<SocketAddress>
/* 11:   */ {
/* 12:   */   public NoopAddressResolver(EventExecutor executor)
/* 13:   */   {
/* 14:35 */     super(executor);
/* 15:   */   }
/* 16:   */   
/* 17:   */   protected boolean doIsResolved(SocketAddress address)
/* 18:   */   {
/* 19:40 */     return true;
/* 20:   */   }
/* 21:   */   
/* 22:   */   protected void doResolve(SocketAddress unresolvedAddress, Promise<SocketAddress> promise)
/* 23:   */     throws Exception
/* 24:   */   {
/* 25:45 */     promise.setSuccess(unresolvedAddress);
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected void doResolveAll(SocketAddress unresolvedAddress, Promise<List<SocketAddress>> promise)
/* 29:   */     throws Exception
/* 30:   */   {
/* 31:51 */     promise.setSuccess(Collections.singletonList(unresolvedAddress));
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.NoopAddressResolver
 * JD-Core Version:    0.7.0.1
 */