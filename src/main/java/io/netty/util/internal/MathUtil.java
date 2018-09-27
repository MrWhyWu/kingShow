/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ public final class MathUtil
/*  4:   */ {
/*  5:   */   public static int findNextPositivePowerOfTwo(int value)
/*  6:   */   {
/*  7:35 */     assert ((value > -2147483648) && (value < 1073741824));
/*  8:36 */     return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
/*  9:   */   }
/* 10:   */   
/* 11:   */   public static int safeFindNextPositivePowerOfTwo(int value)
/* 12:   */   {
/* 13:52 */     return value >= 1073741824 ? 1073741824 : value <= 0 ? 1 : findNextPositivePowerOfTwo(value);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public static boolean isOutOfBounds(int index, int length, int capacity)
/* 17:   */   {
/* 18:64 */     return (index | length | index + length | capacity - (index + length)) < 0;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public static int compare(int x, int y)
/* 22:   */   {
/* 23:78 */     return x > y ? 1 : x < y ? -1 : 0;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public static int compare(long x, long y)
/* 27:   */   {
/* 28:93 */     return x > y ? 1 : x < y ? -1 : 0;
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.MathUtil
 * JD-Core Version:    0.7.0.1
 */