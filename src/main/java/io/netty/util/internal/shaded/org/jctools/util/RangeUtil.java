/*  1:   */ package io.netty.util.internal.shaded.org.jctools.util;
/*  2:   */ 
/*  3:   */ public final class RangeUtil
/*  4:   */ {
/*  5:   */   public static long checkPositive(long n, String name)
/*  6:   */   {
/*  7:21 */     if (n <= 0L) {
/*  8:23 */       throw new IllegalArgumentException(name + ": " + n + " (expected: > 0)");
/*  9:   */     }
/* 10:26 */     return n;
/* 11:   */   }
/* 12:   */   
/* 13:   */   public static int checkPositiveOrZero(int n, String name)
/* 14:   */   {
/* 15:31 */     if (n < 0) {
/* 16:33 */       throw new IllegalArgumentException(name + ": " + n + " (expected: >= 0)");
/* 17:   */     }
/* 18:36 */     return n;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public static int checkLessThan(int n, int expected, String name)
/* 22:   */   {
/* 23:41 */     if (n >= expected) {
/* 24:43 */       throw new IllegalArgumentException(name + ": " + n + " (expected: < " + expected + ')');
/* 25:   */     }
/* 26:46 */     return n;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public static int checkLessThanOrEqual(int n, long expected, String name)
/* 30:   */   {
/* 31:51 */     if (n > expected) {
/* 32:53 */       throw new IllegalArgumentException(name + ": " + n + " (expected: <= " + expected + ')');
/* 33:   */     }
/* 34:56 */     return n;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public static int checkGreaterThanOrEqual(int n, int expected, String name)
/* 38:   */   {
/* 39:61 */     if (n < expected) {
/* 40:63 */       throw new IllegalArgumentException(name + ": " + n + " (expected: >= " + expected + ')');
/* 41:   */     }
/* 42:66 */     return n;
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.util.RangeUtil
 * JD-Core Version:    0.7.0.1
 */