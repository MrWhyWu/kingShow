/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.math.BigInteger;
/*   4:    */ 
/*   5:    */ public final class UnsignedNumberUtil
/*   6:    */ {
/*   7:    */   public static final long MAX_VALUE = -1L;
/*   8:    */   static final long INT_MASK = 4294967295L;
/*   9: 38 */   private static final long[] maxValueDivs = new long[37];
/*  10: 39 */   private static final int[] maxValueMods = new int[37];
/*  11: 40 */   private static final int[] maxSafeDigits = new int[37];
/*  12:    */   
/*  13:    */   static
/*  14:    */   {
/*  15: 44 */     BigInteger overflow = new BigInteger("10000000000000000", 16);
/*  16: 45 */     for (int i = 2; i <= 36; i++)
/*  17:    */     {
/*  18: 47 */       maxValueDivs[i] = divide(-1L, i);
/*  19: 48 */       maxValueMods[i] = ((int)remainder(-1L, i));
/*  20: 49 */       maxSafeDigits[i] = (overflow.toString(i).length() - 1);
/*  21:    */     }
/*  22:    */   }
/*  23:    */   
/*  24:    */   private static int flip(int value)
/*  25:    */   {
/*  26: 59 */     return value ^ 0x80000000;
/*  27:    */   }
/*  28:    */   
/*  29:    */   private static long flip(long a)
/*  30:    */   {
/*  31: 68 */     return a ^ 0x0;
/*  32:    */   }
/*  33:    */   
/*  34:    */   private static long toLong(int value)
/*  35:    */   {
/*  36: 76 */     return value & 0xFFFFFFFF;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public static int parseUnsignedInt(String s)
/*  40:    */   {
/*  41: 89 */     return parseUnsignedInt(s, 10);
/*  42:    */   }
/*  43:    */   
/*  44:    */   private static int parseUnsignedInt(String string, int radix)
/*  45:    */   {
/*  46:107 */     long result = Long.parseLong(string, radix);
/*  47:108 */     if ((result & 0xFFFFFFFF) != result) {
/*  48:110 */       throw new NumberFormatException("Input " + string + " in base " + radix + " is not in the range of an unsigned integer");
/*  49:    */     }
/*  50:113 */     return (int)result;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public static String unsignedIntToString(int x)
/*  54:    */   {
/*  55:121 */     return unsignedIntToString(x, 10);
/*  56:    */   }
/*  57:    */   
/*  58:    */   private static int compareUnsigned(long a, long b)
/*  59:    */   {
/*  60:137 */     return compareSigned(flip(a), flip(b));
/*  61:    */   }
/*  62:    */   
/*  63:    */   private static int compareSigned(long a, long b)
/*  64:    */   {
/*  65:157 */     return a > b ? 1 : a < b ? -1 : 0;
/*  66:    */   }
/*  67:    */   
/*  68:    */   private static String unsignedIntToString(int x, int radix)
/*  69:    */   {
/*  70:172 */     long asLong = x & 0xFFFFFFFF;
/*  71:173 */     return Long.toString(asLong, radix);
/*  72:    */   }
/*  73:    */   
/*  74:    */   private static long divide(long dividend, long divisor)
/*  75:    */   {
/*  76:188 */     if (divisor < 0L)
/*  77:    */     {
/*  78:190 */       if (compareUnsigned(dividend, divisor) < 0) {
/*  79:192 */         return 0L;
/*  80:    */       }
/*  81:196 */       return 1L;
/*  82:    */     }
/*  83:201 */     if (dividend >= 0L) {
/*  84:203 */       return dividend / divisor;
/*  85:    */     }
/*  86:211 */     long quotient = (dividend >>> 1) / divisor << 1;
/*  87:212 */     long rem = dividend - quotient * divisor;
/*  88:213 */     return quotient + (compareUnsigned(rem, divisor) >= 0 ? 1 : 0);
/*  89:    */   }
/*  90:    */   
/*  91:    */   private static long remainder(long dividend, long divisor)
/*  92:    */   {
/*  93:229 */     if (divisor < 0L)
/*  94:    */     {
/*  95:231 */       if (compareUnsigned(dividend, divisor) < 0) {
/*  96:233 */         return dividend;
/*  97:    */       }
/*  98:237 */       return dividend - divisor;
/*  99:    */     }
/* 100:242 */     if (dividend >= 0L) {
/* 101:244 */       return dividend % divisor;
/* 102:    */     }
/* 103:252 */     long quotient = (dividend >>> 1) / divisor << 1;
/* 104:253 */     long rem = dividend - quotient * divisor;
/* 105:254 */     return rem - (compareUnsigned(rem, divisor) >= 0 ? divisor : 0L);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public static long parseUnsignedLong(String s)
/* 109:    */   {
/* 110:266 */     return parseUnsignedLong(s, 10);
/* 111:    */   }
/* 112:    */   
/* 113:    */   private static long parseUnsignedLong(String s, int radix)
/* 114:    */   {
/* 115:284 */     if (s.length() == 0) {
/* 116:286 */       throw new NumberFormatException("empty string");
/* 117:    */     }
/* 118:288 */     if ((radix < 2) || (radix > 36)) {
/* 119:290 */       throw new NumberFormatException("illegal radix: " + radix);
/* 120:    */     }
/* 121:293 */     int max_safe_pos = maxSafeDigits[radix] - 1;
/* 122:294 */     long value = 0L;
/* 123:295 */     for (int pos = 0; pos < s.length(); pos++)
/* 124:    */     {
/* 125:297 */       int digit = Character.digit(s.charAt(pos), radix);
/* 126:298 */       if (digit == -1) {
/* 127:300 */         throw new NumberFormatException(s);
/* 128:    */       }
/* 129:302 */       if ((pos > max_safe_pos) && (overflowInParse(value, digit, radix))) {
/* 130:304 */         throw new NumberFormatException("Too large for unsigned long: " + s);
/* 131:    */       }
/* 132:306 */       value = value * radix + digit;
/* 133:    */     }
/* 134:309 */     return value;
/* 135:    */   }
/* 136:    */   
/* 137:    */   private static boolean overflowInParse(long current, int digit, int radix)
/* 138:    */   {
/* 139:319 */     if (current >= 0L)
/* 140:    */     {
/* 141:321 */       if (current < maxValueDivs[radix]) {
/* 142:323 */         return false;
/* 143:    */       }
/* 144:325 */       if (current > maxValueDivs[radix]) {
/* 145:327 */         return true;
/* 146:    */       }
/* 147:330 */       return digit > maxValueMods[radix];
/* 148:    */     }
/* 149:334 */     return true;
/* 150:    */   }
/* 151:    */   
/* 152:    */   public static String unsignedLongToString(long x)
/* 153:    */   {
/* 154:342 */     return unsignedLongToString(x, 10);
/* 155:    */   }
/* 156:    */   
/* 157:    */   private static String unsignedLongToString(long x, int radix)
/* 158:    */   {
/* 159:357 */     if ((radix < 2) || (radix > 36)) {
/* 160:359 */       throw new IllegalArgumentException("Invalid radix: " + radix);
/* 161:    */     }
/* 162:361 */     if (x == 0L) {
/* 163:364 */       return "0";
/* 164:    */     }
/* 165:368 */     char[] buf = new char[64];
/* 166:369 */     int i = buf.length;
/* 167:370 */     if (x < 0L)
/* 168:    */     {
/* 169:374 */       long quotient = divide(x, radix);
/* 170:375 */       long rem = x - quotient * radix;
/* 171:376 */       buf[(--i)] = Character.forDigit((int)rem, radix);
/* 172:377 */       x = quotient;
/* 173:    */     }
/* 174:380 */     while (x > 0L)
/* 175:    */     {
/* 176:382 */       buf[(--i)] = Character.forDigit((int)(x % radix), radix);
/* 177:383 */       x /= radix;
/* 178:    */     }
/* 179:386 */     return new String(buf, i, buf.length - i);
/* 180:    */   }
/* 181:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.UnsignedNumberUtil
 * JD-Core Version:    0.7.0.1
 */