/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public final class StringUtil
/*   8:    */ {
/*   9:    */   public static final String EMPTY_STRING = "";
/*  10:    */   public static final String NEWLINE;
/*  11:    */   public static final char DOUBLE_QUOTE = '"';
/*  12:    */   public static final char COMMA = ',';
/*  13:    */   public static final char LINE_FEED = '\n';
/*  14:    */   public static final char CARRIAGE_RETURN = '\r';
/*  15:    */   public static final char TAB = '\t';
/*  16:    */   public static final char SPACE = ' ';
/*  17:    */   private static final String[] BYTE2HEX_PAD;
/*  18:    */   private static final String[] BYTE2HEX_NOPAD;
/*  19:    */   private static final int CSV_NUMBER_ESCAPE_CHARACTERS = 7;
/*  20:    */   private static final char PACKAGE_SEPARATOR_CHAR = '.';
/*  21:    */   
/*  22:    */   static
/*  23:    */   {
/*  24: 30 */     NEWLINE = SystemPropertyUtil.get("line.separator", "\n");
/*  25:    */     
/*  26:    */ 
/*  27:    */ 
/*  28:    */ 
/*  29:    */ 
/*  30:    */ 
/*  31:    */ 
/*  32:    */ 
/*  33: 39 */     BYTE2HEX_PAD = new String[256];
/*  34: 40 */     BYTE2HEX_NOPAD = new String[256];
/*  35: 52 */     for (int i = 0; i < 10; i++)
/*  36:    */     {
/*  37: 53 */       BYTE2HEX_PAD[i] = ("0" + i);
/*  38: 54 */       BYTE2HEX_NOPAD[i] = String.valueOf(i);
/*  39:    */     }
/*  40: 56 */     for (; i < 16; i++)
/*  41:    */     {
/*  42: 57 */       char c = (char)(97 + i - 10);
/*  43: 58 */       BYTE2HEX_PAD[i] = ("0" + c);
/*  44: 59 */       BYTE2HEX_NOPAD[i] = String.valueOf(c);
/*  45:    */     }
/*  46: 61 */     for (; i < BYTE2HEX_PAD.length; i++)
/*  47:    */     {
/*  48: 62 */       String str = Integer.toHexString(i);
/*  49: 63 */       BYTE2HEX_PAD[i] = str;
/*  50: 64 */       BYTE2HEX_NOPAD[i] = str;
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public static String substringAfter(String value, char delim)
/*  55:    */   {
/*  56: 78 */     int pos = value.indexOf(delim);
/*  57: 79 */     if (pos >= 0) {
/*  58: 80 */       return value.substring(pos + 1);
/*  59:    */     }
/*  60: 82 */     return null;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public static boolean commonSuffixOfLength(String s, String p, int len)
/*  64:    */   {
/*  65: 94 */     return (s != null) && (p != null) && (len >= 0) && (s.regionMatches(s.length() - len, p, p.length() - len, len));
/*  66:    */   }
/*  67:    */   
/*  68:    */   public static String byteToHexStringPadded(int value)
/*  69:    */   {
/*  70:101 */     return BYTE2HEX_PAD[(value & 0xFF)];
/*  71:    */   }
/*  72:    */   
/*  73:    */   public static <T extends Appendable> T byteToHexStringPadded(T buf, int value)
/*  74:    */   {
/*  75:    */     try
/*  76:    */     {
/*  77:109 */       buf.append(byteToHexStringPadded(value));
/*  78:    */     }
/*  79:    */     catch (IOException e)
/*  80:    */     {
/*  81:111 */       PlatformDependent.throwException(e);
/*  82:    */     }
/*  83:113 */     return buf;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public static String toHexStringPadded(byte[] src)
/*  87:    */   {
/*  88:120 */     return toHexStringPadded(src, 0, src.length);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public static String toHexStringPadded(byte[] src, int offset, int length)
/*  92:    */   {
/*  93:127 */     return ((StringBuilder)toHexStringPadded(new StringBuilder(length << 1), src, offset, length)).toString();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src)
/*  97:    */   {
/*  98:134 */     return toHexStringPadded(dst, src, 0, src.length);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src, int offset, int length)
/* 102:    */   {
/* 103:141 */     int end = offset + length;
/* 104:142 */     for (int i = offset; i < end; i++) {
/* 105:143 */       byteToHexStringPadded(dst, src[i]);
/* 106:    */     }
/* 107:145 */     return dst;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public static String byteToHexString(int value)
/* 111:    */   {
/* 112:152 */     return BYTE2HEX_NOPAD[(value & 0xFF)];
/* 113:    */   }
/* 114:    */   
/* 115:    */   public static <T extends Appendable> T byteToHexString(T buf, int value)
/* 116:    */   {
/* 117:    */     try
/* 118:    */     {
/* 119:160 */       buf.append(byteToHexString(value));
/* 120:    */     }
/* 121:    */     catch (IOException e)
/* 122:    */     {
/* 123:162 */       PlatformDependent.throwException(e);
/* 124:    */     }
/* 125:164 */     return buf;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public static String toHexString(byte[] src)
/* 129:    */   {
/* 130:171 */     return toHexString(src, 0, src.length);
/* 131:    */   }
/* 132:    */   
/* 133:    */   public static String toHexString(byte[] src, int offset, int length)
/* 134:    */   {
/* 135:178 */     return ((StringBuilder)toHexString(new StringBuilder(length << 1), src, offset, length)).toString();
/* 136:    */   }
/* 137:    */   
/* 138:    */   public static <T extends Appendable> T toHexString(T dst, byte[] src)
/* 139:    */   {
/* 140:185 */     return toHexString(dst, src, 0, src.length);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public static <T extends Appendable> T toHexString(T dst, byte[] src, int offset, int length)
/* 144:    */   {
/* 145:192 */     assert (length >= 0);
/* 146:193 */     if (length == 0) {
/* 147:194 */       return dst;
/* 148:    */     }
/* 149:197 */     int end = offset + length;
/* 150:198 */     int endMinusOne = end - 1;
/* 151:202 */     for (int i = offset; i < endMinusOne; i++) {
/* 152:203 */       if (src[i] != 0) {
/* 153:    */         break;
/* 154:    */       }
/* 155:    */     }
/* 156:208 */     byteToHexString(dst, src[(i++)]);
/* 157:209 */     int remaining = end - i;
/* 158:210 */     toHexStringPadded(dst, src, i, remaining);
/* 159:    */     
/* 160:212 */     return dst;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public static int decodeHexNibble(char c)
/* 164:    */   {
/* 165:225 */     if ((c >= '0') && (c <= '9')) {
/* 166:226 */       return c - '0';
/* 167:    */     }
/* 168:228 */     if ((c >= 'A') && (c <= 'F')) {
/* 169:229 */       return c - 'A' + 10;
/* 170:    */     }
/* 171:231 */     if ((c >= 'a') && (c <= 'f')) {
/* 172:232 */       return c - 'a' + 10;
/* 173:    */     }
/* 174:234 */     return -1;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public static byte decodeHexByte(CharSequence s, int pos)
/* 178:    */   {
/* 179:241 */     int hi = decodeHexNibble(s.charAt(pos));
/* 180:242 */     int lo = decodeHexNibble(s.charAt(pos + 1));
/* 181:243 */     if ((hi == -1) || (lo == -1)) {
/* 182:244 */       throw new IllegalArgumentException(String.format("invalid hex byte '%s' at index %d of '%s'", new Object[] {s
/* 183:245 */         .subSequence(pos, pos + 2), Integer.valueOf(pos), s }));
/* 184:    */     }
/* 185:247 */     return (byte)((hi << 4) + lo);
/* 186:    */   }
/* 187:    */   
/* 188:    */   public static byte[] decodeHexDump(CharSequence hexDump, int fromIndex, int length)
/* 189:    */   {
/* 190:258 */     if ((length < 0) || ((length & 0x1) != 0)) {
/* 191:259 */       throw new IllegalArgumentException("length: " + length);
/* 192:    */     }
/* 193:261 */     if (length == 0) {
/* 194:262 */       return EmptyArrays.EMPTY_BYTES;
/* 195:    */     }
/* 196:264 */     byte[] bytes = new byte[length >>> 1];
/* 197:265 */     for (int i = 0; i < length; i += 2) {
/* 198:266 */       bytes[(i >>> 1)] = decodeHexByte(hexDump, fromIndex + i);
/* 199:    */     }
/* 200:268 */     return bytes;
/* 201:    */   }
/* 202:    */   
/* 203:    */   public static byte[] decodeHexDump(CharSequence hexDump)
/* 204:    */   {
/* 205:275 */     return decodeHexDump(hexDump, 0, hexDump.length());
/* 206:    */   }
/* 207:    */   
/* 208:    */   public static String simpleClassName(Object o)
/* 209:    */   {
/* 210:282 */     if (o == null) {
/* 211:283 */       return "null_object";
/* 212:    */     }
/* 213:285 */     return simpleClassName(o.getClass());
/* 214:    */   }
/* 215:    */   
/* 216:    */   public static String simpleClassName(Class<?> clazz)
/* 217:    */   {
/* 218:294 */     String className = ((Class)ObjectUtil.checkNotNull(clazz, "clazz")).getName();
/* 219:295 */     int lastDotIdx = className.lastIndexOf('.');
/* 220:296 */     if (lastDotIdx > -1) {
/* 221:297 */       return className.substring(lastDotIdx + 1);
/* 222:    */     }
/* 223:299 */     return className;
/* 224:    */   }
/* 225:    */   
/* 226:    */   public static CharSequence escapeCsv(CharSequence value)
/* 227:    */   {
/* 228:311 */     return escapeCsv(value, false);
/* 229:    */   }
/* 230:    */   
/* 231:    */   public static CharSequence escapeCsv(CharSequence value, boolean trimWhiteSpace)
/* 232:    */   {
/* 233:325 */     int length = ((CharSequence)ObjectUtil.checkNotNull(value, "value")).length();
/* 234:    */     int last;
/* 235:    */     int start;
/* 236:    */     int last;
/* 237:328 */     if (trimWhiteSpace)
/* 238:    */     {
/* 239:329 */       int start = indexOfFirstNonOwsChar(value, length);
/* 240:330 */       last = indexOfLastNonOwsChar(value, start, length);
/* 241:    */     }
/* 242:    */     else
/* 243:    */     {
/* 244:332 */       start = 0;
/* 245:333 */       last = length - 1;
/* 246:    */     }
/* 247:335 */     if (start > last) {
/* 248:336 */       return "";
/* 249:    */     }
/* 250:339 */     int firstUnescapedSpecial = -1;
/* 251:340 */     boolean quoted = false;
/* 252:341 */     if (isDoubleQuote(value.charAt(start)))
/* 253:    */     {
/* 254:342 */       quoted = (isDoubleQuote(value.charAt(last))) && (last > start);
/* 255:343 */       if (quoted)
/* 256:    */       {
/* 257:344 */         start++;
/* 258:345 */         last--;
/* 259:    */       }
/* 260:    */       else
/* 261:    */       {
/* 262:347 */         firstUnescapedSpecial = start;
/* 263:    */       }
/* 264:    */     }
/* 265:351 */     if (firstUnescapedSpecial < 0)
/* 266:    */     {
/* 267:352 */       if (quoted) {
/* 268:353 */         for (int i = start; i <= last; i++) {
/* 269:354 */           if (isDoubleQuote(value.charAt(i)))
/* 270:    */           {
/* 271:355 */             if ((i == last) || (!isDoubleQuote(value.charAt(i + 1))))
/* 272:    */             {
/* 273:356 */               firstUnescapedSpecial = i;
/* 274:357 */               break;
/* 275:    */             }
/* 276:359 */             i++;
/* 277:    */           }
/* 278:    */         }
/* 279:    */       } else {
/* 280:363 */         for (int i = start; i <= last; i++)
/* 281:    */         {
/* 282:364 */           char c = value.charAt(i);
/* 283:365 */           if ((c == '\n') || (c == '\r') || (c == ','))
/* 284:    */           {
/* 285:366 */             firstUnescapedSpecial = i;
/* 286:367 */             break;
/* 287:    */           }
/* 288:369 */           if (isDoubleQuote(c))
/* 289:    */           {
/* 290:370 */             if ((i == last) || (!isDoubleQuote(value.charAt(i + 1))))
/* 291:    */             {
/* 292:371 */               firstUnescapedSpecial = i;
/* 293:372 */               break;
/* 294:    */             }
/* 295:374 */             i++;
/* 296:    */           }
/* 297:    */         }
/* 298:    */       }
/* 299:379 */       if (firstUnescapedSpecial < 0) {
/* 300:383 */         return quoted ? value.subSequence(start - 1, last + 2) : value.subSequence(start, last + 1);
/* 301:    */       }
/* 302:    */     }
/* 303:387 */     StringBuilder result = new StringBuilder(last - start + 1 + 7);
/* 304:388 */     result.append('"').append(value, start, firstUnescapedSpecial);
/* 305:389 */     for (int i = firstUnescapedSpecial; i <= last; i++)
/* 306:    */     {
/* 307:390 */       char c = value.charAt(i);
/* 308:391 */       if (isDoubleQuote(c))
/* 309:    */       {
/* 310:392 */         result.append('"');
/* 311:393 */         if ((i < last) && (isDoubleQuote(value.charAt(i + 1)))) {
/* 312:394 */           i++;
/* 313:    */         }
/* 314:    */       }
/* 315:397 */       result.append(c);
/* 316:    */     }
/* 317:399 */     return result.append('"');
/* 318:    */   }
/* 319:    */   
/* 320:    */   public static CharSequence unescapeCsv(CharSequence value)
/* 321:    */   {
/* 322:411 */     int length = ((CharSequence)ObjectUtil.checkNotNull(value, "value")).length();
/* 323:412 */     if (length == 0) {
/* 324:413 */       return value;
/* 325:    */     }
/* 326:415 */     int last = length - 1;
/* 327:416 */     boolean quoted = (isDoubleQuote(value.charAt(0))) && (isDoubleQuote(value.charAt(last))) && (length != 1);
/* 328:417 */     if (!quoted)
/* 329:    */     {
/* 330:418 */       validateCsvFormat(value);
/* 331:419 */       return value;
/* 332:    */     }
/* 333:421 */     StringBuilder unescaped = InternalThreadLocalMap.get().stringBuilder();
/* 334:422 */     for (int i = 1; i < last; i++)
/* 335:    */     {
/* 336:423 */       char current = value.charAt(i);
/* 337:424 */       if (current == '"') {
/* 338:425 */         if ((isDoubleQuote(value.charAt(i + 1))) && (i + 1 != last)) {
/* 339:428 */           i++;
/* 340:    */         } else {
/* 341:431 */           throw newInvalidEscapedCsvFieldException(value, i);
/* 342:    */         }
/* 343:    */       }
/* 344:434 */       unescaped.append(current);
/* 345:    */     }
/* 346:436 */     return unescaped.toString();
/* 347:    */   }
/* 348:    */   
/* 349:    */   public static List<CharSequence> unescapeCsvFields(CharSequence value)
/* 350:    */   {
/* 351:448 */     List<CharSequence> unescaped = new ArrayList(2);
/* 352:449 */     StringBuilder current = InternalThreadLocalMap.get().stringBuilder();
/* 353:450 */     boolean quoted = false;
/* 354:451 */     int last = value.length() - 1;
/* 355:452 */     for (int i = 0; i <= last; i++)
/* 356:    */     {
/* 357:453 */       char c = value.charAt(i);
/* 358:454 */       if (quoted)
/* 359:    */       {
/* 360:455 */         switch (c)
/* 361:    */         {
/* 362:    */         case '"': 
/* 363:457 */           if (i == last)
/* 364:    */           {
/* 365:459 */             unescaped.add(current.toString());
/* 366:460 */             return unescaped;
/* 367:    */           }
/* 368:462 */           char next = value.charAt(++i);
/* 369:463 */           if (next == '"')
/* 370:    */           {
/* 371:465 */             current.append('"');
/* 372:466 */             continue;
/* 373:    */           }
/* 374:468 */           if (next == ',')
/* 375:    */           {
/* 376:470 */             quoted = false;
/* 377:471 */             unescaped.add(current.toString());
/* 378:472 */             current.setLength(0);
/* 379:473 */             continue;
/* 380:    */           }
/* 381:476 */           throw newInvalidEscapedCsvFieldException(value, i - 1);
/* 382:    */         default: 
/* 383:478 */           current.append(c); break;
/* 384:    */         }
/* 385:    */       }
/* 386:    */       else
/* 387:    */       {
/* 388:481 */         switch (c)
/* 389:    */         {
/* 390:    */         case ',': 
/* 391:484 */           unescaped.add(current.toString());
/* 392:485 */           current.setLength(0);
/* 393:486 */           break;
/* 394:    */         case '"': 
/* 395:488 */           if (current.length() == 0) {
/* 396:489 */             quoted = true;
/* 397:    */           }
/* 398:490 */           break;
/* 399:    */         case '\n': 
/* 400:    */         case '\r': 
/* 401:498 */           throw newInvalidEscapedCsvFieldException(value, i);
/* 402:    */         }
/* 403:500 */         current.append(c);
/* 404:    */       }
/* 405:    */     }
/* 406:504 */     if (quoted) {
/* 407:505 */       throw newInvalidEscapedCsvFieldException(value, last);
/* 408:    */     }
/* 409:507 */     unescaped.add(current.toString());
/* 410:508 */     return unescaped;
/* 411:    */   }
/* 412:    */   
/* 413:    */   private static void validateCsvFormat(CharSequence value)
/* 414:    */   {
/* 415:517 */     int length = value.length();
/* 416:518 */     for (int i = 0; i < length; i++) {
/* 417:519 */       switch (value.charAt(i))
/* 418:    */       {
/* 419:    */       case '\n': 
/* 420:    */       case '\r': 
/* 421:    */       case '"': 
/* 422:    */       case ',': 
/* 423:525 */         throw newInvalidEscapedCsvFieldException(value, i);
/* 424:    */       }
/* 425:    */     }
/* 426:    */   }
/* 427:    */   
/* 428:    */   private static IllegalArgumentException newInvalidEscapedCsvFieldException(CharSequence value, int index)
/* 429:    */   {
/* 430:532 */     return new IllegalArgumentException("invalid escaped CSV field: " + value + " index: " + index);
/* 431:    */   }
/* 432:    */   
/* 433:    */   public static int length(String s)
/* 434:    */   {
/* 435:539 */     return s == null ? 0 : s.length();
/* 436:    */   }
/* 437:    */   
/* 438:    */   public static boolean isNullOrEmpty(String s)
/* 439:    */   {
/* 440:546 */     return (s == null) || (s.isEmpty());
/* 441:    */   }
/* 442:    */   
/* 443:    */   public static int indexOfNonWhiteSpace(CharSequence seq, int offset)
/* 444:    */   {
/* 445:557 */     for (; offset < seq.length(); offset++) {
/* 446:558 */       if (!Character.isWhitespace(seq.charAt(offset))) {
/* 447:559 */         return offset;
/* 448:    */       }
/* 449:    */     }
/* 450:562 */     return -1;
/* 451:    */   }
/* 452:    */   
/* 453:    */   public static boolean isSurrogate(char c)
/* 454:    */   {
/* 455:574 */     return (c >= 55296) && (c <= 57343);
/* 456:    */   }
/* 457:    */   
/* 458:    */   private static boolean isDoubleQuote(char c)
/* 459:    */   {
/* 460:578 */     return c == '"';
/* 461:    */   }
/* 462:    */   
/* 463:    */   public static boolean endsWith(CharSequence s, char c)
/* 464:    */   {
/* 465:589 */     int len = s.length();
/* 466:590 */     return (len > 0) && (s.charAt(len - 1) == c);
/* 467:    */   }
/* 468:    */   
/* 469:    */   public static CharSequence trimOws(CharSequence value)
/* 470:    */   {
/* 471:601 */     int length = value.length();
/* 472:602 */     if (length == 0) {
/* 473:603 */       return value;
/* 474:    */     }
/* 475:605 */     int start = indexOfFirstNonOwsChar(value, length);
/* 476:606 */     int end = indexOfLastNonOwsChar(value, start, length);
/* 477:607 */     return (start == 0) && (end == length - 1) ? value : value.subSequence(start, end + 1);
/* 478:    */   }
/* 479:    */   
/* 480:    */   private static int indexOfFirstNonOwsChar(CharSequence value, int length)
/* 481:    */   {
/* 482:614 */     int i = 0;
/* 483:615 */     while ((i < length) && (isOws(value.charAt(i)))) {
/* 484:616 */       i++;
/* 485:    */     }
/* 486:618 */     return i;
/* 487:    */   }
/* 488:    */   
/* 489:    */   private static int indexOfLastNonOwsChar(CharSequence value, int start, int length)
/* 490:    */   {
/* 491:625 */     int i = length - 1;
/* 492:626 */     while ((i > start) && (isOws(value.charAt(i)))) {
/* 493:627 */       i--;
/* 494:    */     }
/* 495:629 */     return i;
/* 496:    */   }
/* 497:    */   
/* 498:    */   private static boolean isOws(char c)
/* 499:    */   {
/* 500:633 */     return (c == ' ') || (c == '\t');
/* 501:    */   }
/* 502:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.StringUtil
 * JD-Core Version:    0.7.0.1
 */