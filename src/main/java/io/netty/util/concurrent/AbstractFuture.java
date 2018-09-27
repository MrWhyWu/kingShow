/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.CancellationException;
/*  4:   */ import java.util.concurrent.ExecutionException;
/*  5:   */ import java.util.concurrent.TimeUnit;
/*  6:   */ import java.util.concurrent.TimeoutException;
/*  7:   */ 
/*  8:   */ public abstract class AbstractFuture<V>
/*  9:   */   implements Future<V>
/* 10:   */ {
/* 11:   */   public V get()
/* 12:   */     throws InterruptedException, ExecutionException
/* 13:   */   {
/* 14:32 */     await();
/* 15:   */     
/* 16:34 */     Throwable cause = cause();
/* 17:35 */     if (cause == null) {
/* 18:36 */       return getNow();
/* 19:   */     }
/* 20:38 */     if ((cause instanceof CancellationException)) {
/* 21:39 */       throw ((CancellationException)cause);
/* 22:   */     }
/* 23:41 */     throw new ExecutionException(cause);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public V get(long timeout, TimeUnit unit)
/* 27:   */     throws InterruptedException, ExecutionException, TimeoutException
/* 28:   */   {
/* 29:46 */     if (await(timeout, unit))
/* 30:   */     {
/* 31:47 */       Throwable cause = cause();
/* 32:48 */       if (cause == null) {
/* 33:49 */         return getNow();
/* 34:   */       }
/* 35:51 */       if ((cause instanceof CancellationException)) {
/* 36:52 */         throw ((CancellationException)cause);
/* 37:   */       }
/* 38:54 */       throw new ExecutionException(cause);
/* 39:   */     }
/* 40:56 */     throw new TimeoutException();
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.AbstractFuture
 * JD-Core Version:    0.7.0.1
 */