/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.Promise;
/*  4:   */ import io.netty.util.internal.logging.InternalLogger;
/*  5:   */ 
/*  6:   */ public final class PromiseNotificationUtil
/*  7:   */ {
/*  8:   */   public static void tryCancel(Promise<?> p, InternalLogger logger)
/*  9:   */   {
/* 10:32 */     if ((!p.cancel(false)) && (logger != null))
/* 11:   */     {
/* 12:33 */       Throwable err = p.cause();
/* 13:34 */       if (err == null) {
/* 14:35 */         logger.warn("Failed to cancel promise because it has succeeded already: {}", p);
/* 15:   */       } else {
/* 16:37 */         logger.warn("Failed to cancel promise because it has failed already: {}, unnotified cause:", p, err);
/* 17:   */       }
/* 18:   */     }
/* 19:   */   }
/* 20:   */   
/* 21:   */   public static <V> void trySuccess(Promise<? super V> p, V result, InternalLogger logger)
/* 22:   */   {
/* 23:48 */     if ((!p.trySuccess(result)) && (logger != null))
/* 24:   */     {
/* 25:49 */       Throwable err = p.cause();
/* 26:50 */       if (err == null) {
/* 27:51 */         logger.warn("Failed to mark a promise as success because it has succeeded already: {}", p);
/* 28:   */       } else {
/* 29:53 */         logger.warn("Failed to mark a promise as success because it has failed already: {}, unnotified cause:", p, err);
/* 30:   */       }
/* 31:   */     }
/* 32:   */   }
/* 33:   */   
/* 34:   */   public static void tryFailure(Promise<?> p, Throwable cause, InternalLogger logger)
/* 35:   */   {
/* 36:64 */     if ((!p.tryFailure(cause)) && (logger != null))
/* 37:   */     {
/* 38:65 */       Throwable err = p.cause();
/* 39:66 */       if (err == null) {
/* 40:67 */         logger.warn("Failed to mark a promise as failure because it has succeeded already: {}", p, cause);
/* 41:   */       } else {
/* 42:69 */         logger.warn("Failed to mark a promise as failure because it has failed already: {}, unnotified cause: {}", new Object[] { p, 
/* 43:   */         
/* 44:71 */           ThrowableUtil.stackTraceToString(err), cause });
/* 45:   */       }
/* 46:   */     }
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.PromiseNotificationUtil
 * JD-Core Version:    0.7.0.1
 */