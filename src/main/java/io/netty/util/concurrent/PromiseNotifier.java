/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import io.netty.util.internal.PromiseNotificationUtil;
/*  5:   */ import io.netty.util.internal.logging.InternalLogger;
/*  6:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  7:   */ 
/*  8:   */ public class PromiseNotifier<V, F extends Future<V>>
/*  9:   */   implements GenericFutureListener<F>
/* 10:   */ {
/* 11:33 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PromiseNotifier.class);
/* 12:   */   private final Promise<? super V>[] promises;
/* 13:   */   private final boolean logNotifyFailure;
/* 14:   */   
/* 15:   */   @SafeVarargs
/* 16:   */   public PromiseNotifier(Promise<? super V>... promises)
/* 17:   */   {
/* 18:44 */     this(true, promises);
/* 19:   */   }
/* 20:   */   
/* 21:   */   @SafeVarargs
/* 22:   */   public PromiseNotifier(boolean logNotifyFailure, Promise<? super V>... promises)
/* 23:   */   {
/* 24:55 */     ObjectUtil.checkNotNull(promises, "promises");
/* 25:56 */     for (Promise<? super V> promise : promises) {
/* 26:57 */       if (promise == null) {
/* 27:58 */         throw new IllegalArgumentException("promises contains null Promise");
/* 28:   */       }
/* 29:   */     }
/* 30:61 */     this.promises = ((Promise[])promises.clone());
/* 31:62 */     this.logNotifyFailure = logNotifyFailure;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void operationComplete(F future)
/* 35:   */     throws Exception
/* 36:   */   {
/* 37:67 */     InternalLogger internalLogger = this.logNotifyFailure ? logger : null;
/* 38:   */     V result;
/* 39:   */     Promise<? super V> localPromise1;
/* 40:68 */     if (future.isSuccess())
/* 41:   */     {
/* 42:69 */       result = future.get();
/* 43:70 */       Promise[] arrayOfPromise1 = this.promises;localPromise1 = arrayOfPromise1.length;
/* 44:70 */       for (Promise<? super V> localPromise2 = 0; localPromise2 < localPromise1; localPromise2++)
/* 45:   */       {
/* 46:70 */         Promise<? super V> p = arrayOfPromise1[localPromise2];
/* 47:71 */         PromiseNotificationUtil.trySuccess(p, result, internalLogger);
/* 48:   */       }
/* 49:   */     }
/* 50:   */     else
/* 51:   */     {
/* 52:   */       Promise<? super V> p;
/* 53:73 */       if (future.isCancelled())
/* 54:   */       {
/* 55:74 */         result = this.promises;int i = result.length;
/* 56:74 */         for (localPromise1 = 0; localPromise1 < i; localPromise1++)
/* 57:   */         {
/* 58:74 */           p = result[localPromise1];
/* 59:75 */           PromiseNotificationUtil.tryCancel(p, internalLogger);
/* 60:   */         }
/* 61:   */       }
/* 62:   */       else
/* 63:   */       {
/* 64:78 */         Throwable cause = future.cause();
/* 65:79 */         Promise[] arrayOfPromise2 = this.promises;localPromise1 = arrayOfPromise2.length;
/* 66:79 */         for (Promise<? super V> localPromise3 = 0; localPromise3 < localPromise1; localPromise3++)
/* 67:   */         {
/* 68:79 */           Promise<? super V> p = arrayOfPromise2[localPromise3];
/* 69:80 */           PromiseNotificationUtil.tryFailure(p, cause, internalLogger);
/* 70:   */         }
/* 71:   */       }
/* 72:   */     }
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.PromiseNotifier
 * JD-Core Version:    0.7.0.1
 */