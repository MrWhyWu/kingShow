/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import io.netty.util.internal.logging.InternalLogger;
/*  5:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  6:   */ 
/*  7:   */ public final class UnaryPromiseNotifier<T>
/*  8:   */   implements FutureListener<T>
/*  9:   */ {
/* 10:23 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnaryPromiseNotifier.class);
/* 11:   */   private final Promise<? super T> promise;
/* 12:   */   
/* 13:   */   public UnaryPromiseNotifier(Promise<? super T> promise)
/* 14:   */   {
/* 15:27 */     this.promise = ((Promise)ObjectUtil.checkNotNull(promise, "promise"));
/* 16:   */   }
/* 17:   */   
/* 18:   */   public void operationComplete(Future<T> future)
/* 19:   */     throws Exception
/* 20:   */   {
/* 21:32 */     cascadeTo(future, this.promise);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public static <X> void cascadeTo(Future<X> completedFuture, Promise<? super X> promise)
/* 25:   */   {
/* 26:36 */     if (completedFuture.isSuccess())
/* 27:   */     {
/* 28:37 */       if (!promise.trySuccess(completedFuture.getNow())) {
/* 29:38 */         logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
/* 30:   */       }
/* 31:   */     }
/* 32:40 */     else if (completedFuture.isCancelled())
/* 33:   */     {
/* 34:41 */       if (!promise.cancel(false)) {
/* 35:42 */         logger.warn("Failed to cancel a promise because it is done already: {}", promise);
/* 36:   */       }
/* 37:   */     }
/* 38:45 */     else if (!promise.tryFailure(completedFuture.cause())) {
/* 39:46 */       logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, completedFuture
/* 40:47 */         .cause());
/* 41:   */     }
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.UnaryPromiseNotifier
 * JD-Core Version:    0.7.0.1
 */