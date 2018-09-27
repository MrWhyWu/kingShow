/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import java.io.PrintStream;
/*   4:    */ import java.util.HashSet;
/*   5:    */ import java.util.Set;
/*   6:    */ 
/*   7:    */ final class MessageFormatter
/*   8:    */ {
/*   9:    */   private static final String DELIM_STR = "{}";
/*  10:    */   private static final char ESCAPE_CHAR = '\\';
/*  11:    */   
/*  12:    */   static FormattingTuple format(String messagePattern, Object arg)
/*  13:    */   {
/*  14:133 */     return arrayFormat(messagePattern, new Object[] { arg });
/*  15:    */   }
/*  16:    */   
/*  17:    */   static FormattingTuple format(String messagePattern, Object argA, Object argB)
/*  18:    */   {
/*  19:157 */     return arrayFormat(messagePattern, new Object[] { argA, argB });
/*  20:    */   }
/*  21:    */   
/*  22:    */   static FormattingTuple arrayFormat(String messagePattern, Object[] argArray)
/*  23:    */   {
/*  24:172 */     if ((argArray == null) || (argArray.length == 0)) {
/*  25:173 */       return new FormattingTuple(messagePattern, null);
/*  26:    */     }
/*  27:176 */     int lastArrIdx = argArray.length - 1;
/*  28:177 */     Object lastEntry = argArray[lastArrIdx];
/*  29:178 */     Throwable throwable = (lastEntry instanceof Throwable) ? (Throwable)lastEntry : null;
/*  30:180 */     if (messagePattern == null) {
/*  31:181 */       return new FormattingTuple(null, throwable);
/*  32:    */     }
/*  33:184 */     int j = messagePattern.indexOf("{}");
/*  34:185 */     if (j == -1) {
/*  35:187 */       return new FormattingTuple(messagePattern, throwable);
/*  36:    */     }
/*  37:190 */     StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);
/*  38:191 */     int i = 0;
/*  39:192 */     int L = 0;
/*  40:    */     do
/*  41:    */     {
/*  42:194 */       boolean notEscaped = (j == 0) || (messagePattern.charAt(j - 1) != '\\');
/*  43:195 */       if (notEscaped)
/*  44:    */       {
/*  45:197 */         sbuf.append(messagePattern, i, j);
/*  46:    */       }
/*  47:    */       else
/*  48:    */       {
/*  49:199 */         sbuf.append(messagePattern, i, j - 1);
/*  50:    */         
/*  51:201 */         notEscaped = (j >= 2) && (messagePattern.charAt(j - 2) == '\\');
/*  52:    */       }
/*  53:204 */       i = j + 2;
/*  54:205 */       if (notEscaped)
/*  55:    */       {
/*  56:206 */         deeplyAppendParameter(sbuf, argArray[L], null);
/*  57:207 */         L++;
/*  58:208 */         if (L > lastArrIdx) {
/*  59:    */           break;
/*  60:    */         }
/*  61:    */       }
/*  62:    */       else
/*  63:    */       {
/*  64:212 */         sbuf.append("{}");
/*  65:    */       }
/*  66:214 */       j = messagePattern.indexOf("{}", i);
/*  67:215 */     } while (j != -1);
/*  68:218 */     sbuf.append(messagePattern, i, messagePattern.length());
/*  69:219 */     return new FormattingTuple(sbuf.toString(), L <= lastArrIdx ? throwable : null);
/*  70:    */   }
/*  71:    */   
/*  72:    */   private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Set<Object[]> seenSet)
/*  73:    */   {
/*  74:225 */     if (o == null)
/*  75:    */     {
/*  76:226 */       sbuf.append("null");
/*  77:227 */       return;
/*  78:    */     }
/*  79:229 */     Class<?> objClass = o.getClass();
/*  80:230 */     if (!objClass.isArray())
/*  81:    */     {
/*  82:231 */       if (Number.class.isAssignableFrom(objClass))
/*  83:    */       {
/*  84:233 */         if (objClass == Long.class) {
/*  85:234 */           sbuf.append(((Long)o).longValue());
/*  86:235 */         } else if ((objClass == Integer.class) || (objClass == Short.class) || (objClass == Byte.class)) {
/*  87:236 */           sbuf.append(((Number)o).intValue());
/*  88:237 */         } else if (objClass == Double.class) {
/*  89:238 */           sbuf.append(((Double)o).doubleValue());
/*  90:239 */         } else if (objClass == Float.class) {
/*  91:240 */           sbuf.append(((Float)o).floatValue());
/*  92:    */         } else {
/*  93:242 */           safeObjectAppend(sbuf, o);
/*  94:    */         }
/*  95:    */       }
/*  96:    */       else {
/*  97:245 */         safeObjectAppend(sbuf, o);
/*  98:    */       }
/*  99:    */     }
/* 100:    */     else
/* 101:    */     {
/* 102:250 */       sbuf.append('[');
/* 103:251 */       if (objClass == [Z.class) {
/* 104:252 */         booleanArrayAppend(sbuf, (boolean[])o);
/* 105:253 */       } else if (objClass == [B.class) {
/* 106:254 */         byteArrayAppend(sbuf, (byte[])o);
/* 107:255 */       } else if (objClass == [C.class) {
/* 108:256 */         charArrayAppend(sbuf, (char[])o);
/* 109:257 */       } else if (objClass == [S.class) {
/* 110:258 */         shortArrayAppend(sbuf, (short[])o);
/* 111:259 */       } else if (objClass == [I.class) {
/* 112:260 */         intArrayAppend(sbuf, (int[])o);
/* 113:261 */       } else if (objClass == [J.class) {
/* 114:262 */         longArrayAppend(sbuf, (long[])o);
/* 115:263 */       } else if (objClass == [F.class) {
/* 116:264 */         floatArrayAppend(sbuf, (float[])o);
/* 117:265 */       } else if (objClass == [D.class) {
/* 118:266 */         doubleArrayAppend(sbuf, (double[])o);
/* 119:    */       } else {
/* 120:268 */         objectArrayAppend(sbuf, (Object[])o, seenSet);
/* 121:    */       }
/* 122:270 */       sbuf.append(']');
/* 123:    */     }
/* 124:    */   }
/* 125:    */   
/* 126:    */   private static void safeObjectAppend(StringBuilder sbuf, Object o)
/* 127:    */   {
/* 128:    */     try
/* 129:    */     {
/* 130:276 */       String oAsString = o.toString();
/* 131:277 */       sbuf.append(oAsString);
/* 132:    */     }
/* 133:    */     catch (Throwable t)
/* 134:    */     {
/* 135:280 */       System.err.println("SLF4J: Failed toString() invocation on an object of type [" + o
/* 136:281 */         .getClass().getName() + ']');
/* 137:282 */       t.printStackTrace();
/* 138:283 */       sbuf.append("[FAILED toString()]");
/* 139:    */     }
/* 140:    */   }
/* 141:    */   
/* 142:    */   private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Set<Object[]> seenSet)
/* 143:    */   {
/* 144:288 */     if (a.length == 0) {
/* 145:289 */       return;
/* 146:    */     }
/* 147:291 */     if (seenSet == null) {
/* 148:292 */       seenSet = new HashSet(a.length);
/* 149:    */     }
/* 150:294 */     if (seenSet.add(a))
/* 151:    */     {
/* 152:295 */       deeplyAppendParameter(sbuf, a[0], seenSet);
/* 153:296 */       for (int i = 1; i < a.length; i++)
/* 154:    */       {
/* 155:297 */         sbuf.append(", ");
/* 156:298 */         deeplyAppendParameter(sbuf, a[i], seenSet);
/* 157:    */       }
/* 158:301 */       seenSet.remove(a);
/* 159:    */     }
/* 160:    */     else
/* 161:    */     {
/* 162:303 */       sbuf.append("...");
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a)
/* 167:    */   {
/* 168:308 */     if (a.length == 0) {
/* 169:309 */       return;
/* 170:    */     }
/* 171:311 */     sbuf.append(a[0]);
/* 172:312 */     for (int i = 1; i < a.length; i++)
/* 173:    */     {
/* 174:313 */       sbuf.append(", ");
/* 175:314 */       sbuf.append(a[i]);
/* 176:    */     }
/* 177:    */   }
/* 178:    */   
/* 179:    */   private static void byteArrayAppend(StringBuilder sbuf, byte[] a)
/* 180:    */   {
/* 181:319 */     if (a.length == 0) {
/* 182:320 */       return;
/* 183:    */     }
/* 184:322 */     sbuf.append(a[0]);
/* 185:323 */     for (int i = 1; i < a.length; i++)
/* 186:    */     {
/* 187:324 */       sbuf.append(", ");
/* 188:325 */       sbuf.append(a[i]);
/* 189:    */     }
/* 190:    */   }
/* 191:    */   
/* 192:    */   private static void charArrayAppend(StringBuilder sbuf, char[] a)
/* 193:    */   {
/* 194:330 */     if (a.length == 0) {
/* 195:331 */       return;
/* 196:    */     }
/* 197:333 */     sbuf.append(a[0]);
/* 198:334 */     for (int i = 1; i < a.length; i++)
/* 199:    */     {
/* 200:335 */       sbuf.append(", ");
/* 201:336 */       sbuf.append(a[i]);
/* 202:    */     }
/* 203:    */   }
/* 204:    */   
/* 205:    */   private static void shortArrayAppend(StringBuilder sbuf, short[] a)
/* 206:    */   {
/* 207:341 */     if (a.length == 0) {
/* 208:342 */       return;
/* 209:    */     }
/* 210:344 */     sbuf.append(a[0]);
/* 211:345 */     for (int i = 1; i < a.length; i++)
/* 212:    */     {
/* 213:346 */       sbuf.append(", ");
/* 214:347 */       sbuf.append(a[i]);
/* 215:    */     }
/* 216:    */   }
/* 217:    */   
/* 218:    */   private static void intArrayAppend(StringBuilder sbuf, int[] a)
/* 219:    */   {
/* 220:352 */     if (a.length == 0) {
/* 221:353 */       return;
/* 222:    */     }
/* 223:355 */     sbuf.append(a[0]);
/* 224:356 */     for (int i = 1; i < a.length; i++)
/* 225:    */     {
/* 226:357 */       sbuf.append(", ");
/* 227:358 */       sbuf.append(a[i]);
/* 228:    */     }
/* 229:    */   }
/* 230:    */   
/* 231:    */   private static void longArrayAppend(StringBuilder sbuf, long[] a)
/* 232:    */   {
/* 233:363 */     if (a.length == 0) {
/* 234:364 */       return;
/* 235:    */     }
/* 236:366 */     sbuf.append(a[0]);
/* 237:367 */     for (int i = 1; i < a.length; i++)
/* 238:    */     {
/* 239:368 */       sbuf.append(", ");
/* 240:369 */       sbuf.append(a[i]);
/* 241:    */     }
/* 242:    */   }
/* 243:    */   
/* 244:    */   private static void floatArrayAppend(StringBuilder sbuf, float[] a)
/* 245:    */   {
/* 246:374 */     if (a.length == 0) {
/* 247:375 */       return;
/* 248:    */     }
/* 249:377 */     sbuf.append(a[0]);
/* 250:378 */     for (int i = 1; i < a.length; i++)
/* 251:    */     {
/* 252:379 */       sbuf.append(", ");
/* 253:380 */       sbuf.append(a[i]);
/* 254:    */     }
/* 255:    */   }
/* 256:    */   
/* 257:    */   private static void doubleArrayAppend(StringBuilder sbuf, double[] a)
/* 258:    */   {
/* 259:385 */     if (a.length == 0) {
/* 260:386 */       return;
/* 261:    */     }
/* 262:388 */     sbuf.append(a[0]);
/* 263:389 */     for (int i = 1; i < a.length; i++)
/* 264:    */     {
/* 265:390 */       sbuf.append(", ");
/* 266:391 */       sbuf.append(a[i]);
/* 267:    */     }
/* 268:    */   }
/* 269:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.MessageFormatter
 * JD-Core Version:    0.7.0.1
 */