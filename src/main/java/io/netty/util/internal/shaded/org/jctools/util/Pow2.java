/*  1:   */ package io.netty.util.internal.shaded.org.jctools.util;
/*  2:   */ 
/*  3:   */ public final class Pow2
/*  4:   */ {
/*  5:   */   public static final int MAX_POW2 = 1073741824;
/*  6:   */   
/*  7:   */   public static int roundToPowerOfTwo(int value)
/*  8:   */   {
/*  9:29 */     if (value > 1073741824) {
/* 10:30 */       throw new IllegalArgumentException("There is no larger power of 2 int for value:" + value + " since it exceeds 2^31.");
/* 11:   */     }
/* 12:32 */     if (value < 0) {
/* 13:33 */       throw new IllegalArgumentException("Given value:" + value + ". Expecting value >= 0.");
/* 14:   */     }
/* 15:35 */     int nextPow2 = 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
/* 16:36 */     return nextPow2;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public static boolean isPowerOfTwo(int value)
/* 20:   */   {
/* 21:44 */     return (value & value - 1) == 0;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public static long align(long value, int alignment)
/* 25:   */   {
/* 26:56 */     if (!isPowerOfTwo(alignment)) {
/* 27:57 */       throw new IllegalArgumentException("alignment must be a power of 2:" + alignment);
/* 28:   */     }
/* 29:59 */     return value + (alignment - 1) & (alignment - 1 ^ 0xFFFFFFFF);
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.util.Pow2
 * JD-Core Version:    0.7.0.1
 */