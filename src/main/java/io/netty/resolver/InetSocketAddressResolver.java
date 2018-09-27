/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import io.netty.util.concurrent.Future;
/*  5:   */ import io.netty.util.concurrent.FutureListener;
/*  6:   */ import io.netty.util.concurrent.Promise;
/*  7:   */ import java.net.InetAddress;
/*  8:   */ import java.net.InetSocketAddress;
/*  9:   */ import java.util.ArrayList;
/* 10:   */ import java.util.List;
/* 11:   */ 
/* 12:   */ public class InetSocketAddressResolver
/* 13:   */   extends AbstractAddressResolver<InetSocketAddress>
/* 14:   */ {
/* 15:   */   final NameResolver<InetAddress> nameResolver;
/* 16:   */   
/* 17:   */   public InetSocketAddressResolver(EventExecutor executor, NameResolver<InetAddress> nameResolver)
/* 18:   */   {
/* 19:43 */     super(executor, InetSocketAddress.class);
/* 20:44 */     this.nameResolver = nameResolver;
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected boolean doIsResolved(InetSocketAddress address)
/* 24:   */   {
/* 25:49 */     return !address.isUnresolved();
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected void doResolve(final InetSocketAddress unresolvedAddress, final Promise<InetSocketAddress> promise)
/* 29:   */     throws Exception
/* 30:   */   {
/* 31:58 */     this.nameResolver.resolve(unresolvedAddress.getHostName()).addListener(new FutureListener()
/* 32:   */     {
/* 33:   */       public void operationComplete(Future<InetAddress> future)
/* 34:   */         throws Exception
/* 35:   */       {
/* 36:61 */         if (future.isSuccess()) {
/* 37:62 */           promise.setSuccess(new InetSocketAddress((InetAddress)future.getNow(), unresolvedAddress.getPort()));
/* 38:   */         } else {
/* 39:64 */           promise.setFailure(future.cause());
/* 40:   */         }
/* 41:   */       }
/* 42:   */     });
/* 43:   */   }
/* 44:   */   
/* 45:   */   protected void doResolveAll(final InetSocketAddress unresolvedAddress, final Promise<List<InetSocketAddress>> promise)
/* 46:   */     throws Exception
/* 47:   */   {
/* 48:76 */     this.nameResolver.resolveAll(unresolvedAddress.getHostName()).addListener(new FutureListener()
/* 49:   */     {
/* 50:   */       public void operationComplete(Future<List<InetAddress>> future)
/* 51:   */         throws Exception
/* 52:   */       {
/* 53:79 */         if (future.isSuccess())
/* 54:   */         {
/* 55:80 */           List<InetAddress> inetAddresses = (List)future.getNow();
/* 56:   */           
/* 57:82 */           List<InetSocketAddress> socketAddresses = new ArrayList(inetAddresses.size());
/* 58:83 */           for (InetAddress inetAddress : inetAddresses) {
/* 59:84 */             socketAddresses.add(new InetSocketAddress(inetAddress, unresolvedAddress.getPort()));
/* 60:   */           }
/* 61:86 */           promise.setSuccess(socketAddresses);
/* 62:   */         }
/* 63:   */         else
/* 64:   */         {
/* 65:88 */           promise.setFailure(future.cause());
/* 66:   */         }
/* 67:   */       }
/* 68:   */     });
/* 69:   */   }
/* 70:   */   
/* 71:   */   public void close()
/* 72:   */   {
/* 73:96 */     this.nameResolver.close();
/* 74:   */   }
/* 75:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.InetSocketAddressResolver
 * JD-Core Version:    0.7.0.1
 */