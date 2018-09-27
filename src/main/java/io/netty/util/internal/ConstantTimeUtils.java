/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ public final class ConstantTimeUtils
/*   4:    */ {
/*   5:    */   public static int equalsConstantTime(int x, int y)
/*   6:    */   {
/*   7: 37 */     int z = 0xFFFFFFFF ^ x ^ y;
/*   8: 38 */     z &= z >> 16;
/*   9: 39 */     z &= z >> 8;
/*  10: 40 */     z &= z >> 4;
/*  11: 41 */     z &= z >> 2;
/*  12: 42 */     z &= z >> 1;
/*  13: 43 */     return z & 0x1;
/*  14:    */   }
/*  15:    */   
/*  16:    */   public static int equalsConstantTime(long x, long y)
/*  17:    */   {
/*  18: 62 */     long z = 0xFFFFFFFF ^ x ^ y;
/*  19: 63 */     z &= z >> 32;
/*  20: 64 */     z &= z >> 16;
/*  21: 65 */     z &= z >> 8;
/*  22: 66 */     z &= z >> 4;
/*  23: 67 */     z &= z >> 2;
/*  24: 68 */     z &= z >> 1;
/*  25: 69 */     return (int)(z & 1L);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public static int equalsConstantTime(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length)
/*  29:    */   {
/*  30: 96 */     int b = 0;
/*  31: 97 */     int end = startPos1 + length;
/*  32: 98 */     for (; startPos1 < end; startPos2++)
/*  33:    */     {
/*  34: 99 */       b |= bytes1[startPos1] ^ bytes2[startPos2];startPos1++;
/*  35:    */     }
/*  36:101 */     return equalsConstantTime(b, 0);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public static int equalsConstantTime(CharSequence s1, CharSequence s2)
/*  40:    */   {
/*  41:120 */     if (s1.length() != s2.length()) {
/*  42:121 */       return 0;
/*  43:    */     }
/*  44:125 */     int c = 0;
/*  45:126 */     for (int i = 0; i < s1.length(); i++) {
/*  46:127 */       c |= s1.charAt(i) ^ s2.charAt(i);
/*  47:    */     }
/*  48:129 */     return equalsConstantTime(c, 0);
/*  49:    */   }
/*  50:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ConstantTimeUtils
 * JD-Core Version:    0.7.0.1
 */