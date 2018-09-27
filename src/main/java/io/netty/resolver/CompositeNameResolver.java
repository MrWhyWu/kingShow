/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import io.netty.util.concurrent.Future;
/*  5:   */ import io.netty.util.concurrent.FutureListener;
/*  6:   */ import io.netty.util.concurrent.Promise;
/*  7:   */ import io.netty.util.internal.ObjectUtil;
/*  8:   */ import java.util.Arrays;
/*  9:   */ import java.util.List;
/* 10:   */ 
/* 11:   */ public final class CompositeNameResolver<T>
/* 12:   */   extends SimpleNameResolver<T>
/* 13:   */ {
/* 14:   */   private final NameResolver<T>[] resolvers;
/* 15:   */   
/* 16:   */   public CompositeNameResolver(EventExecutor executor, NameResolver<T>... resolvers)
/* 17:   */   {
/* 18:45 */     super(executor);
/* 19:46 */     ObjectUtil.checkNotNull(resolvers, "resolvers");
/* 20:47 */     for (int i = 0; i < resolvers.length; i++) {
/* 21:48 */       if (resolvers[i] == null) {
/* 22:49 */         throw new NullPointerException("resolvers[" + i + ']');
/* 23:   */       }
/* 24:   */     }
/* 25:52 */     if (resolvers.length < 2) {
/* 26:53 */       throw new IllegalArgumentException("resolvers: " + Arrays.asList(resolvers) + " (expected: at least 2 resolvers)");
/* 27:   */     }
/* 28:56 */     this.resolvers = ((NameResolver[])resolvers.clone());
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected void doResolve(String inetHost, Promise<T> promise)
/* 32:   */     throws Exception
/* 33:   */   {
/* 34:61 */     doResolveRec(inetHost, promise, 0, null);
/* 35:   */   }
/* 36:   */   
/* 37:   */   private void doResolveRec(final String inetHost, final Promise<T> promise, final int resolverIndex, Throwable lastFailure)
/* 38:   */     throws Exception
/* 39:   */   {
/* 40:68 */     if (resolverIndex >= this.resolvers.length)
/* 41:   */     {
/* 42:69 */       promise.setFailure(lastFailure);
/* 43:   */     }
/* 44:   */     else
/* 45:   */     {
/* 46:71 */       NameResolver<T> resolver = this.resolvers[resolverIndex];
/* 47:72 */       resolver.resolve(inetHost).addListener(new FutureListener()
/* 48:   */       {
/* 49:   */         public void operationComplete(Future<T> future)
/* 50:   */           throws Exception
/* 51:   */         {
/* 52:75 */           if (future.isSuccess()) {
/* 53:76 */             promise.setSuccess(future.getNow());
/* 54:   */           } else {
/* 55:78 */             CompositeNameResolver.this.doResolveRec(inetHost, promise, resolverIndex + 1, future.cause());
/* 56:   */           }
/* 57:   */         }
/* 58:   */       });
/* 59:   */     }
/* 60:   */   }
/* 61:   */   
/* 62:   */   protected void doResolveAll(String inetHost, Promise<List<T>> promise)
/* 63:   */     throws Exception
/* 64:   */   {
/* 65:87 */     doResolveAllRec(inetHost, promise, 0, null);
/* 66:   */   }
/* 67:   */   
/* 68:   */   private void doResolveAllRec(final String inetHost, final Promise<List<T>> promise, final int resolverIndex, Throwable lastFailure)
/* 69:   */     throws Exception
/* 70:   */   {
/* 71:94 */     if (resolverIndex >= this.resolvers.length)
/* 72:   */     {
/* 73:95 */       promise.setFailure(lastFailure);
/* 74:   */     }
/* 75:   */     else
/* 76:   */     {
/* 77:97 */       NameResolver<T> resolver = this.resolvers[resolverIndex];
/* 78:98 */       resolver.resolveAll(inetHost).addListener(new FutureListener()
/* 79:   */       {
/* 80:   */         public void operationComplete(Future<List<T>> future)
/* 81:   */           throws Exception
/* 82:   */         {
/* 83::1 */           if (future.isSuccess()) {
/* 84::2 */             promise.setSuccess(future.getNow());
/* 85:   */           } else {
/* 86::4 */             CompositeNameResolver.this.doResolveAllRec(inetHost, promise, resolverIndex + 1, future.cause());
/* 87:   */           }
/* 88:   */         }
/* 89:   */       });
/* 90:   */     }
/* 91:   */   }
/* 92:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.CompositeNameResolver
 * JD-Core Version:    0.7.0.1
 */