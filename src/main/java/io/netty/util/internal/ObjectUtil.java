/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ public final class ObjectUtil
/*   4:    */ {
/*   5:    */   public static <T> T checkNotNull(T arg, String text)
/*   6:    */   {
/*   7: 30 */     if (arg == null) {
/*   8: 31 */       throw new NullPointerException(text);
/*   9:    */     }
/*  10: 33 */     return arg;
/*  11:    */   }
/*  12:    */   
/*  13:    */   public static int checkPositive(int i, String name)
/*  14:    */   {
/*  15: 41 */     if (i <= 0) {
/*  16: 42 */       throw new IllegalArgumentException(name + ": " + i + " (expected: > 0)");
/*  17:    */     }
/*  18: 44 */     return i;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public static long checkPositive(long i, String name)
/*  22:    */   {
/*  23: 52 */     if (i <= 0L) {
/*  24: 53 */       throw new IllegalArgumentException(name + ": " + i + " (expected: > 0)");
/*  25:    */     }
/*  26: 55 */     return i;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public static int checkPositiveOrZero(int i, String name)
/*  30:    */   {
/*  31: 63 */     if (i < 0) {
/*  32: 64 */       throw new IllegalArgumentException(name + ": " + i + " (expected: >= 0)");
/*  33:    */     }
/*  34: 66 */     return i;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public static long checkPositiveOrZero(long i, String name)
/*  38:    */   {
/*  39: 74 */     if (i < 0L) {
/*  40: 75 */       throw new IllegalArgumentException(name + ": " + i + " (expected: >= 0)");
/*  41:    */     }
/*  42: 77 */     return i;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public static <T> T[] checkNonEmpty(T[] array, String name)
/*  46:    */   {
/*  47: 86 */     checkNotNull(array, name);
/*  48: 87 */     checkPositive(array.length, name + ".length");
/*  49: 88 */     return array;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public static int intValue(Integer wrapper, int defaultValue)
/*  53:    */   {
/*  54: 98 */     return wrapper != null ? wrapper.intValue() : defaultValue;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public static long longValue(Long wrapper, long defaultValue)
/*  58:    */   {
/*  59:108 */     return wrapper != null ? wrapper.longValue() : defaultValue;
/*  60:    */   }
/*  61:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ObjectUtil
 * JD-Core Version:    0.7.0.1
 */