/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ 
/*  5:   */ public final class FailedFuture<V>
/*  6:   */   extends CompleteFuture<V>
/*  7:   */ {
/*  8:   */   private final Throwable cause;
/*  9:   */   
/* 10:   */   public FailedFuture(EventExecutor executor, Throwable cause)
/* 11:   */   {
/* 12:36 */     super(executor);
/* 13:37 */     if (cause == null) {
/* 14:38 */       throw new NullPointerException("cause");
/* 15:   */     }
/* 16:40 */     this.cause = cause;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public Throwable cause()
/* 20:   */   {
/* 21:45 */     return this.cause;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public boolean isSuccess()
/* 25:   */   {
/* 26:50 */     return false;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public Future<V> sync()
/* 30:   */   {
/* 31:55 */     PlatformDependent.throwException(this.cause);
/* 32:56 */     return this;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public Future<V> syncUninterruptibly()
/* 36:   */   {
/* 37:61 */     PlatformDependent.throwException(this.cause);
/* 38:62 */     return this;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public V getNow()
/* 42:   */   {
/* 43:67 */     return null;
/* 44:   */   }
/* 45:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.FailedFuture
 * JD-Core Version:    0.7.0.1
 */