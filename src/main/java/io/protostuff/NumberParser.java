/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ public final class NumberParser
/*   4:    */ {
/*   5:    */   public static int parseInt(byte[] buffer, int start, int length, int radix)
/*   6:    */     throws NumberFormatException
/*   7:    */   {
/*   8: 38 */     if (length == 0) {
/*   9: 39 */       throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  10:    */     }
/*  11: 41 */     if (buffer[start] == 45)
/*  12:    */     {
/*  13: 43 */       if (length == 1) {
/*  14: 44 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  15:    */       }
/*  16: 46 */       return parseInt(buffer, start + 1, length - 1, radix, false);
/*  17:    */     }
/*  18: 49 */     return parseInt(buffer, start, length, radix, true);
/*  19:    */   }
/*  20:    */   
/*  21:    */   static int parseInt(byte[] buffer, int start, int length, int radix, boolean positive)
/*  22:    */     throws NumberFormatException
/*  23:    */   {
/*  24: 55 */     int max = -2147483648 / radix;
/*  25: 56 */     int result = 0;
/*  26: 57 */     int offset = start;
/*  27: 57 */     for (int limit = start + length; offset < limit;)
/*  28:    */     {
/*  29: 59 */       int digit = Character.digit(buffer[(offset++)], radix);
/*  30: 60 */       if (digit == -1) {
/*  31: 62 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  32:    */       }
/*  33: 64 */       if (max > result) {
/*  34: 66 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  35:    */       }
/*  36: 68 */       int next = result * radix - digit;
/*  37: 69 */       if (next > result) {
/*  38: 71 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  39:    */       }
/*  40: 73 */       result = next;
/*  41:    */     }
/*  42: 75 */     if (positive)
/*  43:    */     {
/*  44: 77 */       result = -result;
/*  45: 78 */       if (result < 0) {
/*  46: 80 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  47:    */       }
/*  48:    */     }
/*  49: 83 */     return result;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public static long parseLong(byte[] buffer, int start, int length, int radix)
/*  53:    */     throws NumberFormatException
/*  54:    */   {
/*  55: 92 */     if (length == 0) {
/*  56: 93 */       throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  57:    */     }
/*  58: 95 */     if (buffer[start] == 45)
/*  59:    */     {
/*  60: 97 */       if (length == 1) {
/*  61: 98 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  62:    */       }
/*  63:100 */       return parseLong(buffer, start + 1, length - 1, radix, false);
/*  64:    */     }
/*  65:103 */     return parseLong(buffer, start, length, radix, true);
/*  66:    */   }
/*  67:    */   
/*  68:    */   static long parseLong(byte[] buffer, int start, int length, int radix, boolean positive)
/*  69:    */     throws NumberFormatException
/*  70:    */   {
/*  71:109 */     long max = -9223372036854775808L / radix;
/*  72:110 */     long result = 0L;
/*  73:111 */     int offset = start;
/*  74:111 */     for (int limit = start + length; offset < limit;)
/*  75:    */     {
/*  76:113 */       int digit = Character.digit(buffer[(offset++)], radix);
/*  77:114 */       if (digit == -1) {
/*  78:116 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  79:    */       }
/*  80:118 */       if (max > result) {
/*  81:120 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  82:    */       }
/*  83:122 */       long next = result * radix - digit;
/*  84:123 */       if (next > result) {
/*  85:125 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  86:    */       }
/*  87:127 */       result = next;
/*  88:    */     }
/*  89:129 */     if (positive)
/*  90:    */     {
/*  91:131 */       result = -result;
/*  92:132 */       if (result < 0L) {
/*  93:134 */         throw new NumberFormatException(StringSerializer.STRING.deser(buffer, start, length));
/*  94:    */       }
/*  95:    */     }
/*  96:138 */     return result;
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.NumberParser
 * JD-Core Version:    0.7.0.1
 */