/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import java.net.InetAddress;
/*  5:   */ import java.net.InetSocketAddress;
/*  6:   */ 
/*  7:   */ public abstract class InetNameResolver
/*  8:   */   extends SimpleNameResolver<InetAddress>
/*  9:   */ {
/* 10:   */   private volatile AddressResolver<InetSocketAddress> addressResolver;
/* 11:   */   
/* 12:   */   protected InetNameResolver(EventExecutor executor)
/* 13:   */   {
/* 14:37 */     super(executor);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public AddressResolver<InetSocketAddress> asAddressResolver()
/* 18:   */   {
/* 19:45 */     AddressResolver<InetSocketAddress> result = this.addressResolver;
/* 20:46 */     if (result == null) {
/* 21:47 */       synchronized (this)
/* 22:   */       {
/* 23:48 */         result = this.addressResolver;
/* 24:49 */         if (result == null) {
/* 25:50 */           this.addressResolver = (result = new InetSocketAddressResolver(executor(), this));
/* 26:   */         }
/* 27:   */       }
/* 28:   */     }
/* 29:54 */     return result;
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.InetNameResolver
 * JD-Core Version:    0.7.0.1
 */