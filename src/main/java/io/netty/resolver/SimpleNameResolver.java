/*  1:   */ package io.netty.resolver;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import io.netty.util.concurrent.Future;
/*  5:   */ import io.netty.util.concurrent.Promise;
/*  6:   */ import io.netty.util.internal.ObjectUtil;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ public abstract class SimpleNameResolver<T>
/* 10:   */   implements NameResolver<T>
/* 11:   */ {
/* 12:   */   private final EventExecutor executor;
/* 13:   */   
/* 14:   */   protected SimpleNameResolver(EventExecutor executor)
/* 15:   */   {
/* 16:41 */     this.executor = ((EventExecutor)ObjectUtil.checkNotNull(executor, "executor"));
/* 17:   */   }
/* 18:   */   
/* 19:   */   protected EventExecutor executor()
/* 20:   */   {
/* 21:49 */     return this.executor;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public final Future<T> resolve(String inetHost)
/* 25:   */   {
/* 26:54 */     Promise<T> promise = executor().newPromise();
/* 27:55 */     return resolve(inetHost, promise);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public Future<T> resolve(String inetHost, Promise<T> promise)
/* 31:   */   {
/* 32:60 */     ObjectUtil.checkNotNull(promise, "promise");
/* 33:   */     try
/* 34:   */     {
/* 35:63 */       doResolve(inetHost, promise);
/* 36:64 */       return promise;
/* 37:   */     }
/* 38:   */     catch (Exception e)
/* 39:   */     {
/* 40:66 */       return promise.setFailure(e);
/* 41:   */     }
/* 42:   */   }
/* 43:   */   
/* 44:   */   public final Future<List<T>> resolveAll(String inetHost)
/* 45:   */   {
/* 46:72 */     Promise<List<T>> promise = executor().newPromise();
/* 47:73 */     return resolveAll(inetHost, promise);
/* 48:   */   }
/* 49:   */   
/* 50:   */   public Future<List<T>> resolveAll(String inetHost, Promise<List<T>> promise)
/* 51:   */   {
/* 52:78 */     ObjectUtil.checkNotNull(promise, "promise");
/* 53:   */     try
/* 54:   */     {
/* 55:81 */       doResolveAll(inetHost, promise);
/* 56:82 */       return promise;
/* 57:   */     }
/* 58:   */     catch (Exception e)
/* 59:   */     {
/* 60:84 */       return promise.setFailure(e);
/* 61:   */     }
/* 62:   */   }
/* 63:   */   
/* 64:   */   protected abstract void doResolve(String paramString, Promise<T> paramPromise)
/* 65:   */     throws Exception;
/* 66:   */   
/* 67:   */   protected abstract void doResolveAll(String paramString, Promise<List<T>> paramPromise)
/* 68:   */     throws Exception;
/* 69:   */   
/* 70:   */   public void close() {}
/* 71:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.resolver.SimpleNameResolver
 * JD-Core Version:    0.7.0.1
 */