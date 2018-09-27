/*    1:     */ package io.protostuff;
/*    2:     */ 
/*    3:     */ import java.io.UTFDataFormatException;
/*    4:     */ import java.io.UnsupportedEncodingException;
/*    5:     */ 
/*    6:     */ public final class StringSerializer
/*    7:     */ {
/*    8:  26 */   static final int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, 2147483647 };
/*    9:  30 */   static final char[] DigitTens = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9' };
/*   10:  43 */   static final char[] DigitOnes = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
/*   11:  56 */   static final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
/*   12:  65 */   static final byte[] INT_MIN_VALUE = { 45, 50, 49, 52, 55, 52, 56, 51, 54, 52, 56 };
/*   13:  73 */   static final byte[] LONG_MIN_VALUE = { 45, 57, 50, 50, 51, 51, 55, 50, 48, 51, 54, 56, 53, 52, 55, 55, 53, 56, 48, 56 };
/*   14:     */   static final int TWO_BYTE_LOWER_LIMIT = 128;
/*   15:     */   static final int ONE_BYTE_EXCLUSIVE = 43;
/*   16:     */   static final int THREE_BYTE_LOWER_LIMIT = 16384;
/*   17:     */   static final int TWO_BYTE_EXCLUSIVE = 5462;
/*   18:     */   static final int FOUR_BYTE_LOWER_LIMIT = 2097152;
/*   19:     */   static final int THREE_BYTE_EXCLUSIVE = 699051;
/*   20:     */   static final int FIVE_BYTE_LOWER_LIMIT = 268435456;
/*   21:     */   static final int FOUR_BYTE_EXCLUSIVE = 89478486;
/*   22:     */   
/*   23:     */   static void putBytesFromInt(int i, int offset, int size, byte[] buf)
/*   24:     */   {
/*   25: 103 */     int charPos = offset + size;
/*   26: 104 */     char sign = '\000';
/*   27: 106 */     if (i < 0)
/*   28:     */     {
/*   29: 108 */       sign = '-';
/*   30: 109 */       i = -i;
/*   31:     */     }
/*   32: 113 */     while (i >= 65536)
/*   33:     */     {
/*   34: 115 */       int q = i / 100;
/*   35:     */       
/*   36: 117 */       int r = i - ((q << 6) + (q << 5) + (q << 2));
/*   37: 118 */       i = q;
/*   38: 119 */       buf[(--charPos)] = ((byte)DigitOnes[r]);
/*   39: 120 */       buf[(--charPos)] = ((byte)DigitTens[r]);
/*   40:     */     }
/*   41:     */     for (;;)
/*   42:     */     {
/*   43: 127 */       int q = i * 52429 >>> 19;
/*   44: 128 */       int r = i - ((q << 3) + (q << 1));
/*   45: 129 */       buf[(--charPos)] = ((byte)digits[r]);
/*   46: 130 */       i = q;
/*   47: 131 */       if (i == 0) {
/*   48:     */         break;
/*   49:     */       }
/*   50:     */     }
/*   51: 134 */     if (sign != 0) {
/*   52: 136 */       buf[(--charPos)] = ((byte)sign);
/*   53:     */     }
/*   54:     */   }
/*   55:     */   
/*   56:     */   static void putBytesFromLong(long i, int offset, int size, byte[] buf)
/*   57:     */   {
/*   58: 144 */     int charPos = offset + size;
/*   59: 145 */     char sign = '\000';
/*   60: 147 */     if (i < 0L)
/*   61:     */     {
/*   62: 149 */       sign = '-';
/*   63: 150 */       i = -i;
/*   64:     */     }
/*   65: 154 */     while (i > 2147483647L)
/*   66:     */     {
/*   67: 156 */       long q = i / 100L;
/*   68:     */       
/*   69: 158 */       int r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
/*   70: 159 */       i = q;
/*   71: 160 */       buf[(--charPos)] = ((byte)DigitOnes[r]);
/*   72: 161 */       buf[(--charPos)] = ((byte)DigitTens[r]);
/*   73:     */     }
/*   74: 166 */     int i2 = (int)i;
/*   75: 167 */     while (i2 >= 65536)
/*   76:     */     {
/*   77: 169 */       int q2 = i2 / 100;
/*   78:     */       
/*   79: 171 */       int r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
/*   80: 172 */       i2 = q2;
/*   81: 173 */       buf[(--charPos)] = ((byte)DigitOnes[r]);
/*   82: 174 */       buf[(--charPos)] = ((byte)DigitTens[r]);
/*   83:     */     }
/*   84:     */     for (;;)
/*   85:     */     {
/*   86: 181 */       int q2 = i2 * 52429 >>> 19;
/*   87: 182 */       int r = i2 - ((q2 << 3) + (q2 << 1));
/*   88: 183 */       buf[(--charPos)] = ((byte)digits[r]);
/*   89: 184 */       i2 = q2;
/*   90: 185 */       if (i2 == 0) {
/*   91:     */         break;
/*   92:     */       }
/*   93:     */     }
/*   94: 188 */     if (sign != 0) {
/*   95: 190 */       buf[(--charPos)] = ((byte)sign);
/*   96:     */     }
/*   97:     */   }
/*   98:     */   
/*   99:     */   static int stringSize(int x)
/*  100:     */   {
/*  101: 197 */     for (int i = 0;; i++) {
/*  102: 199 */       if (x <= sizeTable[i]) {
/*  103: 200 */         return i + 1;
/*  104:     */       }
/*  105:     */     }
/*  106:     */   }
/*  107:     */   
/*  108:     */   static int stringSize(long x)
/*  109:     */   {
/*  110: 207 */     long p = 10L;
/*  111: 208 */     for (int i = 1; i < 19; i++)
/*  112:     */     {
/*  113: 210 */       if (x < p) {
/*  114: 211 */         return i;
/*  115:     */       }
/*  116: 212 */       p = 10L * p;
/*  117:     */     }
/*  118: 214 */     return 19;
/*  119:     */   }
/*  120:     */   
/*  121:     */   public static LinkedBuffer writeInt(int value, WriteSession session, LinkedBuffer lb)
/*  122:     */   {
/*  123: 223 */     if (value == -2147483648)
/*  124:     */     {
/*  125: 225 */       int valueLen = INT_MIN_VALUE.length;
/*  126: 226 */       if (lb.offset + valueLen > lb.buffer.length) {
/*  127: 229 */         lb = new LinkedBuffer(session.nextBufferSize, lb);
/*  128:     */       }
/*  129: 232 */       System.arraycopy(INT_MIN_VALUE, 0, lb.buffer, lb.offset, valueLen);
/*  130:     */       
/*  131: 234 */       lb.offset += valueLen;
/*  132: 235 */       session.size += valueLen;
/*  133:     */       
/*  134: 237 */       return lb;
/*  135:     */     }
/*  136: 240 */     int size = value < 0 ? stringSize(-value) + 1 : stringSize(value);
/*  137: 242 */     if (lb.offset + size > lb.buffer.length) {
/*  138: 245 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/*  139:     */     }
/*  140: 248 */     putBytesFromInt(value, lb.offset, size, lb.buffer);
/*  141:     */     
/*  142: 250 */     lb.offset += size;
/*  143: 251 */     session.size += size;
/*  144:     */     
/*  145: 253 */     return lb;
/*  146:     */   }
/*  147:     */   
/*  148:     */   public static LinkedBuffer writeLong(long value, WriteSession session, LinkedBuffer lb)
/*  149:     */   {
/*  150: 262 */     if (value == -9223372036854775808L)
/*  151:     */     {
/*  152: 264 */       int valueLen = LONG_MIN_VALUE.length;
/*  153: 265 */       if (lb.offset + valueLen > lb.buffer.length) {
/*  154: 269 */         lb = new LinkedBuffer(session.nextBufferSize, lb);
/*  155:     */       }
/*  156: 272 */       System.arraycopy(LONG_MIN_VALUE, 0, lb.buffer, lb.offset, valueLen);
/*  157:     */       
/*  158: 274 */       lb.offset += valueLen;
/*  159: 275 */       session.size += valueLen;
/*  160:     */       
/*  161: 277 */       return lb;
/*  162:     */     }
/*  163: 280 */     int size = value < 0L ? stringSize(-value) + 1 : stringSize(value);
/*  164: 282 */     if (lb.offset + size > lb.buffer.length) {
/*  165: 286 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/*  166:     */     }
/*  167: 289 */     putBytesFromLong(value, lb.offset, size, lb.buffer);
/*  168:     */     
/*  169: 291 */     lb.offset += size;
/*  170: 292 */     session.size += size;
/*  171:     */     
/*  172: 294 */     return lb;
/*  173:     */   }
/*  174:     */   
/*  175:     */   public static LinkedBuffer writeFloat(float value, WriteSession session, LinkedBuffer lb)
/*  176:     */   {
/*  177: 304 */     return writeAscii(Float.toString(value), session, lb);
/*  178:     */   }
/*  179:     */   
/*  180:     */   public static LinkedBuffer writeDouble(double value, WriteSession session, LinkedBuffer lb)
/*  181:     */   {
/*  182: 314 */     return writeAscii(Double.toString(value), session, lb);
/*  183:     */   }
/*  184:     */   
/*  185:     */   public static int computeUTF8Size(CharSequence str, int index, int len)
/*  186:     */   {
/*  187: 322 */     int size = len;
/*  188: 323 */     for (int i = index; i < len; i++)
/*  189:     */     {
/*  190: 325 */       char c = str.charAt(i);
/*  191: 326 */       if (c >= '') {
/*  192: 329 */         if (c < 'ࠀ') {
/*  193: 330 */           size++;
/*  194:     */         } else {
/*  195: 332 */           size += 2;
/*  196:     */         }
/*  197:     */       }
/*  198:     */     }
/*  199: 334 */     return size;
/*  200:     */   }
/*  201:     */   
/*  202:     */   static LinkedBuffer writeUTF8(CharSequence str, int i, int len, byte[] buffer, int offset, int limit, WriteSession session, LinkedBuffer lb)
/*  203:     */   {
/*  204: 344 */     char c = '\000';
/*  205:     */     for (;;)
/*  206:     */     {
/*  207: 346 */       if ((i != len) && (offset != limit) && ((c = str.charAt(i++)) < ''))
/*  208:     */       {
/*  209: 347 */         buffer[(offset++)] = ((byte)c);
/*  210:     */       }
/*  211:     */       else
/*  212:     */       {
/*  213: 349 */         if ((i == len) && (c < ''))
/*  214:     */         {
/*  215: 351 */           session.size += offset - lb.offset;
/*  216: 352 */           lb.offset = offset;
/*  217: 353 */           return lb;
/*  218:     */         }
/*  219: 356 */         if (offset == limit)
/*  220:     */         {
/*  221: 359 */           session.size += offset - lb.offset;
/*  222: 360 */           lb.offset = offset;
/*  223: 362 */           if (lb.next == null)
/*  224:     */           {
/*  225: 365 */             offset = 0;
/*  226: 366 */             limit = session.nextBufferSize;
/*  227: 367 */             buffer = new byte[limit];
/*  228:     */             
/*  229: 369 */             lb = new LinkedBuffer(buffer, 0, lb);
/*  230:     */           }
/*  231:     */           else
/*  232:     */           {
/*  233: 375 */             lb = lb.next;
/*  234:     */             
/*  235: 377 */             lb.offset = (offset = lb.start);
/*  236: 378 */             buffer = lb.buffer;
/*  237: 379 */             limit = buffer.length;
/*  238:     */           }
/*  239:     */         }
/*  240: 385 */         else if (c < 'ࠀ')
/*  241:     */         {
/*  242: 387 */           if (offset == limit)
/*  243:     */           {
/*  244: 390 */             session.size += offset - lb.offset;
/*  245: 391 */             lb.offset = offset;
/*  246: 393 */             if (lb.next == null)
/*  247:     */             {
/*  248: 396 */               offset = 0;
/*  249: 397 */               limit = session.nextBufferSize;
/*  250: 398 */               buffer = new byte[limit];
/*  251:     */               
/*  252: 400 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  253:     */             }
/*  254:     */             else
/*  255:     */             {
/*  256: 406 */               lb = lb.next;
/*  257:     */               
/*  258: 408 */               lb.offset = (offset = lb.start);
/*  259: 409 */               buffer = lb.buffer;
/*  260: 410 */               limit = buffer.length;
/*  261:     */             }
/*  262:     */           }
/*  263: 414 */           buffer[(offset++)] = ((byte)(0xC0 | c >> '\006' & 0x1F));
/*  264: 416 */           if (offset == limit)
/*  265:     */           {
/*  266: 419 */             session.size += offset - lb.offset;
/*  267: 420 */             lb.offset = offset;
/*  268: 422 */             if (lb.next == null)
/*  269:     */             {
/*  270: 425 */               offset = 0;
/*  271: 426 */               limit = session.nextBufferSize;
/*  272: 427 */               buffer = new byte[limit];
/*  273:     */               
/*  274: 429 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  275:     */             }
/*  276:     */             else
/*  277:     */             {
/*  278: 435 */               lb = lb.next;
/*  279:     */               
/*  280: 437 */               lb.offset = (offset = lb.start);
/*  281: 438 */               buffer = lb.buffer;
/*  282: 439 */               limit = buffer.length;
/*  283:     */             }
/*  284:     */           }
/*  285: 443 */           buffer[(offset++)] = ((byte)(0x80 | c >> '\000' & 0x3F));
/*  286:     */         }
/*  287: 445 */         else if ((Character.isHighSurrogate(c)) && (i < len) && (Character.isLowSurrogate(str.charAt(i))))
/*  288:     */         {
/*  289: 448 */           if (offset == limit)
/*  290:     */           {
/*  291: 451 */             session.size += offset - lb.offset;
/*  292: 452 */             lb.offset = offset;
/*  293: 454 */             if (lb.next == null)
/*  294:     */             {
/*  295: 457 */               offset = 0;
/*  296: 458 */               limit = session.nextBufferSize;
/*  297: 459 */               buffer = new byte[limit];
/*  298:     */               
/*  299: 461 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  300:     */             }
/*  301:     */             else
/*  302:     */             {
/*  303: 467 */               lb = lb.next;
/*  304:     */               
/*  305: 469 */               lb.offset = (offset = lb.start);
/*  306: 470 */               buffer = lb.buffer;
/*  307: 471 */               limit = buffer.length;
/*  308:     */             }
/*  309:     */           }
/*  310: 475 */           int codePoint = Character.toCodePoint(c, str.charAt(i));
/*  311:     */           
/*  312: 477 */           buffer[(offset++)] = ((byte)(0xF0 | codePoint >> 18 & 0x7));
/*  313: 479 */           if (offset == limit)
/*  314:     */           {
/*  315: 482 */             session.size += offset - lb.offset;
/*  316: 483 */             lb.offset = offset;
/*  317: 485 */             if (lb.next == null)
/*  318:     */             {
/*  319: 488 */               offset = 0;
/*  320: 489 */               limit = session.nextBufferSize;
/*  321: 490 */               buffer = new byte[limit];
/*  322:     */               
/*  323: 492 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  324:     */             }
/*  325:     */             else
/*  326:     */             {
/*  327: 498 */               lb = lb.next;
/*  328:     */               
/*  329: 500 */               lb.offset = (offset = lb.start);
/*  330: 501 */               buffer = lb.buffer;
/*  331: 502 */               limit = buffer.length;
/*  332:     */             }
/*  333:     */           }
/*  334: 506 */           buffer[(offset++)] = ((byte)(0x80 | codePoint >> 12 & 0x3F));
/*  335: 508 */           if (offset == limit)
/*  336:     */           {
/*  337: 511 */             session.size += offset - lb.offset;
/*  338: 512 */             lb.offset = offset;
/*  339: 514 */             if (lb.next == null)
/*  340:     */             {
/*  341: 517 */               offset = 0;
/*  342: 518 */               limit = session.nextBufferSize;
/*  343: 519 */               buffer = new byte[limit];
/*  344:     */               
/*  345: 521 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  346:     */             }
/*  347:     */             else
/*  348:     */             {
/*  349: 527 */               lb = lb.next;
/*  350:     */               
/*  351: 529 */               lb.offset = (offset = lb.start);
/*  352: 530 */               buffer = lb.buffer;
/*  353: 531 */               limit = buffer.length;
/*  354:     */             }
/*  355:     */           }
/*  356: 535 */           buffer[(offset++)] = ((byte)(0x80 | codePoint >> 6 & 0x3F));
/*  357: 537 */           if (offset == limit)
/*  358:     */           {
/*  359: 540 */             session.size += offset - lb.offset;
/*  360: 541 */             lb.offset = offset;
/*  361: 543 */             if (lb.next == null)
/*  362:     */             {
/*  363: 546 */               offset = 0;
/*  364: 547 */               limit = session.nextBufferSize;
/*  365: 548 */               buffer = new byte[limit];
/*  366:     */               
/*  367: 550 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  368:     */             }
/*  369:     */             else
/*  370:     */             {
/*  371: 556 */               lb = lb.next;
/*  372:     */               
/*  373: 558 */               lb.offset = (offset = lb.start);
/*  374: 559 */               buffer = lb.buffer;
/*  375: 560 */               limit = buffer.length;
/*  376:     */             }
/*  377:     */           }
/*  378: 564 */           buffer[(offset++)] = ((byte)(0x80 | codePoint >> 0 & 0x3F));
/*  379:     */           
/*  380: 566 */           i++;
/*  381:     */         }
/*  382:     */         else
/*  383:     */         {
/*  384: 570 */           if (offset == limit)
/*  385:     */           {
/*  386: 573 */             session.size += offset - lb.offset;
/*  387: 574 */             lb.offset = offset;
/*  388: 576 */             if (lb.next == null)
/*  389:     */             {
/*  390: 579 */               offset = 0;
/*  391: 580 */               limit = session.nextBufferSize;
/*  392: 581 */               buffer = new byte[limit];
/*  393:     */               
/*  394: 583 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  395:     */             }
/*  396:     */             else
/*  397:     */             {
/*  398: 589 */               lb = lb.next;
/*  399:     */               
/*  400: 591 */               lb.offset = (offset = lb.start);
/*  401: 592 */               buffer = lb.buffer;
/*  402: 593 */               limit = buffer.length;
/*  403:     */             }
/*  404:     */           }
/*  405: 597 */           buffer[(offset++)] = ((byte)(0xE0 | c >> '\f' & 0xF));
/*  406: 599 */           if (offset == limit)
/*  407:     */           {
/*  408: 602 */             session.size += offset - lb.offset;
/*  409: 603 */             lb.offset = offset;
/*  410: 605 */             if (lb.next == null)
/*  411:     */             {
/*  412: 608 */               offset = 0;
/*  413: 609 */               limit = session.nextBufferSize;
/*  414: 610 */               buffer = new byte[limit];
/*  415:     */               
/*  416: 612 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  417:     */             }
/*  418:     */             else
/*  419:     */             {
/*  420: 618 */               lb = lb.next;
/*  421:     */               
/*  422: 620 */               lb.offset = (offset = lb.start);
/*  423: 621 */               buffer = lb.buffer;
/*  424: 622 */               limit = buffer.length;
/*  425:     */             }
/*  426:     */           }
/*  427: 626 */           buffer[(offset++)] = ((byte)(0x80 | c >> '\006' & 0x3F));
/*  428: 628 */           if (offset == limit)
/*  429:     */           {
/*  430: 631 */             session.size += offset - lb.offset;
/*  431: 632 */             lb.offset = offset;
/*  432: 634 */             if (lb.next == null)
/*  433:     */             {
/*  434: 637 */               offset = 0;
/*  435: 638 */               limit = session.nextBufferSize;
/*  436: 639 */               buffer = new byte[limit];
/*  437:     */               
/*  438: 641 */               lb = new LinkedBuffer(buffer, 0, lb);
/*  439:     */             }
/*  440:     */             else
/*  441:     */             {
/*  442: 647 */               lb = lb.next;
/*  443:     */               
/*  444: 649 */               lb.offset = (offset = lb.start);
/*  445: 650 */               buffer = lb.buffer;
/*  446: 651 */               limit = buffer.length;
/*  447:     */             }
/*  448:     */           }
/*  449: 655 */           buffer[(offset++)] = ((byte)(0x80 | c >> '\000' & 0x3F));
/*  450:     */         }
/*  451: 344 */         c = '\000';
/*  452:     */       }
/*  453:     */     }
/*  454:     */   }
/*  455:     */   
/*  456:     */   static LinkedBuffer writeUTF8(CharSequence str, int i, int len, WriteSession session, LinkedBuffer lb)
/*  457:     */   {
/*  458: 666 */     byte[] buffer = lb.buffer;
/*  459: 667 */     int c = 0;int offset = lb.offset;int adjustableLimit = offset + len;
/*  460:     */     for (;;)
/*  461:     */     {
/*  462: 669 */       if ((i != len) && ((c = str.charAt(i++)) < ''))
/*  463:     */       {
/*  464: 670 */         buffer[(offset++)] = ((byte)c);
/*  465:     */       }
/*  466:     */       else
/*  467:     */       {
/*  468: 672 */         if ((i == len) && (c < 128))
/*  469:     */         {
/*  470: 674 */           session.size += offset - lb.offset;
/*  471: 675 */           lb.offset = offset;
/*  472: 676 */           return lb;
/*  473:     */         }
/*  474: 679 */         if (c < 2048)
/*  475:     */         {
/*  476: 681 */           adjustableLimit++;
/*  477: 681 */           if (adjustableLimit > buffer.length)
/*  478:     */           {
/*  479: 683 */             session.size += offset - lb.offset;
/*  480: 684 */             lb.offset = offset;
/*  481: 685 */             return writeUTF8(str, i - 1, len, buffer, offset, buffer.length, session, lb);
/*  482:     */           }
/*  483: 688 */           buffer[(offset++)] = ((byte)(0xC0 | c >> 6 & 0x1F));
/*  484: 689 */           buffer[(offset++)] = ((byte)(0x80 | c >> 0 & 0x3F));
/*  485:     */         }
/*  486: 691 */         else if ((Character.isHighSurrogate((char)c)) && (i < len) && (Character.isLowSurrogate(str.charAt(i))))
/*  487:     */         {
/*  488: 694 */           adjustableLimit += 3;
/*  489: 695 */           if (adjustableLimit > buffer.length)
/*  490:     */           {
/*  491: 697 */             session.size += offset - lb.offset;
/*  492: 698 */             lb.offset = offset;
/*  493: 699 */             return writeUTF8(str, i - 1, len, buffer, offset, buffer.length, session, lb);
/*  494:     */           }
/*  495: 702 */           int codePoint = Character.toCodePoint((char)c, str.charAt(i));
/*  496: 703 */           buffer[(offset++)] = ((byte)(0xF0 | codePoint >> 18 & 0x7));
/*  497: 704 */           buffer[(offset++)] = ((byte)(0x80 | codePoint >> 12 & 0x3F));
/*  498: 705 */           buffer[(offset++)] = ((byte)(0x80 | codePoint >> 6 & 0x3F));
/*  499: 706 */           buffer[(offset++)] = ((byte)(0x80 | codePoint >> 0 & 0x3F));
/*  500:     */           
/*  501: 708 */           i++;
/*  502:     */         }
/*  503:     */         else
/*  504:     */         {
/*  505: 712 */           adjustableLimit += 2;
/*  506: 713 */           if (adjustableLimit > buffer.length)
/*  507:     */           {
/*  508: 715 */             session.size += offset - lb.offset;
/*  509: 716 */             lb.offset = offset;
/*  510: 717 */             return writeUTF8(str, i - 1, len, buffer, offset, buffer.length, session, lb);
/*  511:     */           }
/*  512: 720 */           buffer[(offset++)] = ((byte)(0xE0 | c >> 12 & 0xF));
/*  513: 721 */           buffer[(offset++)] = ((byte)(0x80 | c >> 6 & 0x3F));
/*  514: 722 */           buffer[(offset++)] = ((byte)(0x80 | c >> 0 & 0x3F));
/*  515:     */         }
/*  516: 667 */         c = 0;
/*  517:     */       }
/*  518:     */     }
/*  519:     */   }
/*  520:     */   
/*  521:     */   public static LinkedBuffer writeUTF8(CharSequence str, WriteSession session, LinkedBuffer lb)
/*  522:     */   {
/*  523: 733 */     int len = str.length();
/*  524: 734 */     if (len == 0) {
/*  525: 735 */       return lb;
/*  526:     */     }
/*  527: 737 */     return lb.offset + len > lb.buffer.length ? writeUTF8(str, 0, len, lb.buffer, lb.offset, lb.buffer.length, session, lb) : 
/*  528: 738 */       writeUTF8(str, 0, len, session, lb);
/*  529:     */   }
/*  530:     */   
/*  531:     */   public static LinkedBuffer writeAscii(CharSequence str, WriteSession session, LinkedBuffer lb)
/*  532:     */   {
/*  533: 749 */     int len = str.length();
/*  534: 750 */     if (len == 0) {
/*  535: 751 */       return lb;
/*  536:     */     }
/*  537: 753 */     byte[] buffer = lb.buffer;
/*  538: 754 */     int offset = lb.offset;int limit = lb.buffer.length;
/*  539:     */     
/*  540:     */ 
/*  541: 757 */     session.size += len;
/*  542: 759 */     if (offset + len > limit) {
/*  543: 762 */       for (int i = 0; i < len; i++)
/*  544:     */       {
/*  545: 764 */         if (offset == limit)
/*  546:     */         {
/*  547: 767 */           lb.offset = offset;
/*  548:     */           
/*  549: 769 */           offset = 0;
/*  550: 770 */           limit = session.nextBufferSize;
/*  551: 771 */           buffer = new byte[limit];
/*  552:     */           
/*  553: 773 */           lb = new LinkedBuffer(buffer, 0, lb);
/*  554:     */         }
/*  555: 775 */         buffer[(offset++)] = ((byte)str.charAt(i));
/*  556:     */       }
/*  557:     */     } else {
/*  558: 781 */       for (int i = 0; i < len; i++) {
/*  559: 782 */         buffer[(offset++)] = ((byte)str.charAt(i));
/*  560:     */       }
/*  561:     */     }
/*  562: 785 */     lb.offset = offset;
/*  563:     */     
/*  564: 787 */     return lb;
/*  565:     */   }
/*  566:     */   
/*  567:     */   static void writeFixed2ByteInt(int value, byte[] buffer, int offset, boolean littleEndian)
/*  568:     */   {
/*  569: 793 */     if (littleEndian)
/*  570:     */     {
/*  571: 795 */       buffer[(offset++)] = ((byte)value);
/*  572: 796 */       buffer[offset] = ((byte)(value >>> 8 & 0xFF));
/*  573:     */     }
/*  574:     */     else
/*  575:     */     {
/*  576: 800 */       buffer[(offset++)] = ((byte)(value >>> 8 & 0xFF));
/*  577: 801 */       buffer[offset] = ((byte)value);
/*  578:     */     }
/*  579:     */   }
/*  580:     */   
/*  581:     */   public static LinkedBuffer writeUTF8FixedDelimited(CharSequence str, WriteSession session, LinkedBuffer lb)
/*  582:     */   {
/*  583: 812 */     return writeUTF8FixedDelimited(str, false, session, lb);
/*  584:     */   }
/*  585:     */   
/*  586:     */   public static LinkedBuffer writeUTF8FixedDelimited(CharSequence str, boolean littleEndian, WriteSession session, LinkedBuffer lb)
/*  587:     */   {
/*  588: 821 */     int lastSize = session.size;int len = str.length();int withIntOffset = lb.offset + 2;
/*  589: 823 */     if (withIntOffset > lb.buffer.length)
/*  590:     */     {
/*  591: 827 */       lb = new LinkedBuffer(len + 2 > session.nextBufferSize ? len + 2 : session.nextBufferSize, lb);
/*  592:     */       
/*  593:     */ 
/*  594: 830 */       lb.offset = 2;
/*  595: 832 */       if (len == 0)
/*  596:     */       {
/*  597: 834 */         writeFixed2ByteInt(0, lb.buffer, 0, littleEndian);
/*  598:     */         
/*  599: 836 */         session.size += 2;
/*  600: 837 */         return lb;
/*  601:     */       }
/*  602: 841 */       LinkedBuffer rb = writeUTF8(str, 0, len, session, lb);
/*  603:     */       
/*  604: 843 */       writeFixed2ByteInt(session.size - lastSize, lb.buffer, 0, littleEndian);
/*  605:     */       
/*  606:     */ 
/*  607: 846 */       session.size += 2;
/*  608:     */       
/*  609: 848 */       return rb;
/*  610:     */     }
/*  611: 851 */     if (len == 0)
/*  612:     */     {
/*  613: 853 */       writeFixed2ByteInt(0, lb.buffer, lb.offset, littleEndian);
/*  614: 854 */       lb.offset = withIntOffset;
/*  615:     */       
/*  616: 856 */       session.size += 2;
/*  617: 857 */       return lb;
/*  618:     */     }
/*  619: 860 */     if (withIntOffset + len > lb.buffer.length)
/*  620:     */     {
/*  621: 863 */       lb.offset = withIntOffset;
/*  622:     */       
/*  623:     */ 
/*  624: 866 */       LinkedBuffer rb = writeUTF8(str, 0, len, lb.buffer, withIntOffset, lb.buffer.length, session, lb);
/*  625:     */       
/*  626:     */ 
/*  627: 869 */       writeFixed2ByteInt(session.size - lastSize, lb.buffer, withIntOffset - 2, littleEndian);
/*  628:     */       
/*  629:     */ 
/*  630:     */ 
/*  631: 873 */       session.size += 2;
/*  632:     */       
/*  633: 875 */       return rb;
/*  634:     */     }
/*  635: 879 */     lb.offset = withIntOffset;
/*  636:     */     
/*  637: 881 */     LinkedBuffer rb = writeUTF8(str, 0, len, session, lb);
/*  638:     */     
/*  639: 883 */     writeFixed2ByteInt(session.size - lastSize, lb.buffer, withIntOffset - 2, littleEndian);
/*  640:     */     
/*  641:     */ 
/*  642:     */ 
/*  643: 887 */     session.size += 2;
/*  644:     */     
/*  645: 889 */     return rb;
/*  646:     */   }
/*  647:     */   
/*  648:     */   private static LinkedBuffer writeUTF8OneByteDelimited(CharSequence str, int index, int len, WriteSession session, LinkedBuffer lb)
/*  649:     */   {
/*  650: 895 */     int lastSize = session.size;
/*  651: 897 */     if (lb.offset == lb.buffer.length)
/*  652:     */     {
/*  653: 900 */       lb = new LinkedBuffer(len + 1 > session.nextBufferSize ? len + 1 : session.nextBufferSize, lb);
/*  654:     */       
/*  655:     */ 
/*  656: 903 */       lb.offset = 1;
/*  657:     */       
/*  658:     */ 
/*  659: 906 */       LinkedBuffer rb = writeUTF8(str, index, len, session, lb);
/*  660:     */       
/*  661: 908 */       lb.buffer[0] = ((byte)(session.size - lastSize));
/*  662:     */       
/*  663:     */ 
/*  664: 911 */       session.size += 1;
/*  665:     */       
/*  666: 913 */       return rb;
/*  667:     */     }
/*  668: 916 */     int withIntOffset = lb.offset + 1;
/*  669: 917 */     if (withIntOffset + len > lb.buffer.length)
/*  670:     */     {
/*  671: 920 */       lb.offset = withIntOffset;
/*  672:     */       
/*  673: 922 */       byte[] buffer = lb.buffer;
/*  674:     */       
/*  675:     */ 
/*  676: 925 */       LinkedBuffer rb = writeUTF8(str, index, len, buffer, withIntOffset, buffer.length, session, lb);
/*  677:     */       
/*  678:     */ 
/*  679: 928 */       buffer[(withIntOffset - 1)] = ((byte)(session.size - lastSize));
/*  680:     */       
/*  681:     */ 
/*  682: 931 */       session.size += 1;
/*  683:     */       
/*  684: 933 */       return rb;
/*  685:     */     }
/*  686: 937 */     lb.offset = withIntOffset;
/*  687:     */     
/*  688: 939 */     LinkedBuffer rb = writeUTF8(str, index, len, session, lb);
/*  689:     */     
/*  690: 941 */     lb.buffer[(withIntOffset - 1)] = ((byte)(session.size - lastSize));
/*  691:     */     
/*  692:     */ 
/*  693: 944 */     session.size += 1;
/*  694:     */     
/*  695: 946 */     return rb;
/*  696:     */   }
/*  697:     */   
/*  698:     */   private static LinkedBuffer writeUTF8VarDelimited(CharSequence str, int index, int len, int lowerLimit, int expectedSize, WriteSession session, LinkedBuffer lb)
/*  699:     */   {
/*  700: 953 */     int lastSize = session.size;int offset = lb.offset;int withIntOffset = offset + expectedSize;
/*  701: 955 */     if (withIntOffset > lb.buffer.length)
/*  702:     */     {
/*  703: 959 */       lb = new LinkedBuffer(len + expectedSize > session.nextBufferSize ? len + expectedSize : session.nextBufferSize, lb);
/*  704:     */       
/*  705:     */ 
/*  706: 962 */       offset = lb.start;
/*  707: 963 */       lb.offset = (withIntOffset = offset + expectedSize);
/*  708:     */       
/*  709:     */ 
/*  710: 966 */       LinkedBuffer rb = writeUTF8(str, index, len, session, lb);
/*  711:     */       
/*  712: 968 */       int size = session.size - lastSize;
/*  713: 970 */       if (size < lowerLimit)
/*  714:     */       {
/*  715: 973 */         System.arraycopy(lb.buffer, withIntOffset, lb.buffer, withIntOffset - 1, lb.offset - withIntOffset);
/*  716:     */         
/*  717:     */ 
/*  718: 976 */         expectedSize--;
/*  719: 977 */         lb.offset -= 1;
/*  720:     */       }
/*  721: 981 */       session.size += expectedSize;
/*  722: 983 */       for (;; size >>>= 7)
/*  723:     */       {
/*  724: 983 */         expectedSize--;
/*  725: 983 */         if (expectedSize <= 0) {
/*  726:     */           break;
/*  727:     */         }
/*  728: 984 */         lb.buffer[(offset++)] = ((byte)(size & 0x7F | 0x80));
/*  729:     */       }
/*  730: 986 */       lb.buffer[offset] = ((byte)size);
/*  731:     */       
/*  732: 988 */       return rb;
/*  733:     */     }
/*  734: 991 */     if (withIntOffset + len > lb.buffer.length)
/*  735:     */     {
/*  736: 994 */       lb.offset = withIntOffset;
/*  737:     */       
/*  738:     */ 
/*  739: 997 */       LinkedBuffer rb = writeUTF8(str, index, len, lb.buffer, withIntOffset, lb.buffer.length, session, lb);
/*  740:     */       
/*  741:     */ 
/*  742:1000 */       int size = session.size - lastSize;
/*  743:1002 */       if (size < lowerLimit)
/*  744:     */       {
/*  745:1005 */         System.arraycopy(lb.buffer, withIntOffset, lb.buffer, withIntOffset - 1, lb.offset - withIntOffset);
/*  746:     */         
/*  747:     */ 
/*  748:1008 */         expectedSize--;
/*  749:1009 */         lb.offset -= 1;
/*  750:     */       }
/*  751:1013 */       session.size += expectedSize;
/*  752:1015 */       for (;; size >>>= 7)
/*  753:     */       {
/*  754:1015 */         expectedSize--;
/*  755:1015 */         if (expectedSize <= 0) {
/*  756:     */           break;
/*  757:     */         }
/*  758:1016 */         lb.buffer[(offset++)] = ((byte)(size & 0x7F | 0x80));
/*  759:     */       }
/*  760:1018 */       lb.buffer[offset] = ((byte)size);
/*  761:     */       
/*  762:1020 */       return rb;
/*  763:     */     }
/*  764:1024 */     lb.offset = withIntOffset;
/*  765:     */     
/*  766:1026 */     LinkedBuffer rb = writeUTF8(str, index, len, session, lb);
/*  767:     */     
/*  768:1028 */     int size = session.size - lastSize;
/*  769:1030 */     if (size < lowerLimit)
/*  770:     */     {
/*  771:1033 */       System.arraycopy(lb.buffer, withIntOffset, lb.buffer, withIntOffset - 1, lb.offset - withIntOffset);
/*  772:     */       
/*  773:     */ 
/*  774:1036 */       expectedSize--;
/*  775:1037 */       lb.offset -= 1;
/*  776:     */     }
/*  777:1041 */     session.size += expectedSize;
/*  778:1043 */     for (;; size >>>= 7)
/*  779:     */     {
/*  780:1043 */       expectedSize--;
/*  781:1043 */       if (expectedSize <= 0) {
/*  782:     */         break;
/*  783:     */       }
/*  784:1044 */       lb.buffer[(offset++)] = ((byte)(size & 0x7F | 0x80));
/*  785:     */     }
/*  786:1046 */     lb.buffer[offset] = ((byte)size);
/*  787:     */     
/*  788:1048 */     return rb;
/*  789:     */   }
/*  790:     */   
/*  791:     */   public static LinkedBuffer writeUTF8VarDelimited(CharSequence str, WriteSession session, LinkedBuffer lb)
/*  792:     */   {
/*  793:1057 */     int len = str.length();
/*  794:1058 */     if (len == 0)
/*  795:     */     {
/*  796:1060 */       if (lb.offset == lb.buffer.length) {
/*  797:1063 */         lb = new LinkedBuffer(session.nextBufferSize, lb);
/*  798:     */       }
/*  799:1067 */       lb.buffer[(lb.offset++)] = 0;
/*  800:     */       
/*  801:1069 */       session.size += 1;
/*  802:1070 */       return lb;
/*  803:     */     }
/*  804:1073 */     if (len < 43) {
/*  805:1076 */       return writeUTF8OneByteDelimited(str, 0, len, session, lb);
/*  806:     */     }
/*  807:1079 */     if (len < 5462) {
/*  808:1082 */       return writeUTF8VarDelimited(str, 0, len, 128, 2, session, lb);
/*  809:     */     }
/*  810:1086 */     if (len < 699051) {
/*  811:1089 */       return writeUTF8VarDelimited(str, 0, len, 16384, 3, session, lb);
/*  812:     */     }
/*  813:1093 */     if (len < 89478486) {
/*  814:1096 */       return writeUTF8VarDelimited(str, 0, len, 2097152, 4, session, lb);
/*  815:     */     }
/*  816:1101 */     return writeUTF8VarDelimited(str, 0, len, 268435456, 5, session, lb);
/*  817:     */   }
/*  818:     */   
/*  819:     */   public static final class STRING
/*  820:     */   {
/*  821:1106 */     static final boolean CESU8_COMPAT = Boolean.getBoolean("io.protostuff.cesu8_compat");
/*  822:     */     
/*  823:     */     public static String deser(byte[] nonNullValue)
/*  824:     */     {
/*  825:1114 */       return deser(nonNullValue, 0, nonNullValue.length);
/*  826:     */     }
/*  827:     */     
/*  828:     */     public static String deser(byte[] nonNullValue, int offset, int len)
/*  829:     */     {
/*  830:     */       try
/*  831:     */       {
/*  832:1147 */         String result = new String(nonNullValue, offset, len, "UTF-8");
/*  833:1155 */         if (CESU8_COMPAT) {
/*  834:1160 */           if (result.indexOf(65533) != -1) {
/*  835:     */             try
/*  836:     */             {
/*  837:1164 */               return readUTF(nonNullValue, offset, len);
/*  838:     */             }
/*  839:     */             catch (UTFDataFormatException e)
/*  840:     */             {
/*  841:1171 */               return result;
/*  842:     */             }
/*  843:     */           }
/*  844:     */         }
/*  845:     */       }
/*  846:     */       catch (UnsupportedEncodingException e)
/*  847:     */       {
/*  848:1178 */         throw new RuntimeException(e);
/*  849:     */       }
/*  850:     */       String result;
/*  851:1181 */       return result;
/*  852:     */     }
/*  853:     */     
/*  854:     */     static String deserCustomOnly(byte[] nonNullValue)
/*  855:     */     {
/*  856:     */       try
/*  857:     */       {
/*  858:1196 */         return readUTF(nonNullValue, 0, nonNullValue.length);
/*  859:     */       }
/*  860:     */       catch (UTFDataFormatException e)
/*  861:     */       {
/*  862:1200 */         throw new RuntimeException(e);
/*  863:     */       }
/*  864:     */     }
/*  865:     */     
/*  866:     */     public static byte[] ser(String nonNullValue)
/*  867:     */     {
/*  868:     */       try
/*  869:     */       {
/*  870:1208 */         return nonNullValue.getBytes("UTF-8");
/*  871:     */       }
/*  872:     */       catch (UnsupportedEncodingException e)
/*  873:     */       {
/*  874:1212 */         throw new RuntimeException(e);
/*  875:     */       }
/*  876:     */     }
/*  877:     */     
/*  878:     */     private static String readUTF(byte[] buffer, int offset, int len)
/*  879:     */       throws UTFDataFormatException
/*  880:     */     {
/*  881:1224 */       char[] charArray = new char[len];
/*  882:     */       
/*  883:1226 */       int i = 0;
/*  884:1227 */       int c = 0;
/*  885:1232 */       for (; i < len; i++)
/*  886:     */       {
/*  887:1234 */         int ch = buffer[(offset + i)] & 0xFF;
/*  888:1237 */         if (ch > 127) {
/*  889:     */           break;
/*  890:     */         }
/*  891:1240 */         charArray[(c++)] = ((char)ch);
/*  892:     */       }
/*  893:1244 */       while (i < len)
/*  894:     */       {
/*  895:1246 */         int ch = buffer[(offset + i)] & 0xFF;
/*  896:     */         
/*  897:     */ 
/*  898:     */ 
/*  899:1250 */         int upperBits = ch >> 4;
/*  900:1252 */         if (upperBits <= 7)
/*  901:     */         {
/*  902:1255 */           charArray[(c++)] = ((char)ch);
/*  903:1256 */           i++;
/*  904:     */         }
/*  905:1258 */         else if ((upperBits == 12) || (upperBits == 13))
/*  906:     */         {
/*  907:1261 */           i += 2;
/*  908:1263 */           if (i > len) {
/*  909:1264 */             throw new UTFDataFormatException("Malformed input: Partial character at end");
/*  910:     */           }
/*  911:1266 */           int ch2 = buffer[(offset + i - 1)];
/*  912:1269 */           if ((ch2 & 0xC0) != 128) {
/*  913:1270 */             throw new UTFDataFormatException("Malformed input around byte " + i);
/*  914:     */           }
/*  915:1272 */           charArray[(c++)] = ((char)((ch & 0x1F) << 6 | ch2 & 0x3F));
/*  916:     */         }
/*  917:1274 */         else if (upperBits == 14)
/*  918:     */         {
/*  919:1277 */           i += 3;
/*  920:1279 */           if (i > len) {
/*  921:1280 */             throw new UTFDataFormatException("Malformed input: Partial character at end");
/*  922:     */           }
/*  923:1282 */           int ch2 = buffer[(offset + i - 2)];
/*  924:1283 */           int ch3 = buffer[(offset + i - 1)];
/*  925:1286 */           if (((ch2 & 0xC0) != 128) || ((ch3 & 0xC0) != 128)) {
/*  926:1287 */             throw new UTFDataFormatException("Malformed input around byte " + (i - 1));
/*  927:     */           }
/*  928:1289 */           charArray[(c++)] = ((char)((ch & 0xF) << 12 | (ch2 & 0x3F) << 6 | ch3 & 0x3F));
/*  929:     */         }
/*  930:     */         else
/*  931:     */         {
/*  932:1294 */           upperBits = ch >> 3;
/*  933:1295 */           if (upperBits == 30)
/*  934:     */           {
/*  935:1301 */             i += 4;
/*  936:1302 */             if (i > len) {
/*  937:1303 */               throw new UTFDataFormatException("Malformed input: Partial character at end");
/*  938:     */             }
/*  939:1305 */             int ch2 = buffer[(offset + i - 3)];
/*  940:1306 */             int ch3 = buffer[(offset + i - 2)];
/*  941:1307 */             int ch4 = buffer[(offset + i - 1)];
/*  942:     */             
/*  943:1309 */             int value = (ch & 0x7) << 18 | (ch2 & 0x3F) << 12 | (ch3 & 0x3F) << 6 | ch4 & 0x3F;
/*  944:     */             
/*  945:     */ 
/*  946:     */ 
/*  947:     */ 
/*  948:     */ 
/*  949:1315 */             charArray[(c++)] = StringSerializer.highSurrogate(value);
/*  950:1316 */             charArray[(c++)] = StringSerializer.lowSurrogate(value);
/*  951:     */           }
/*  952:     */           else
/*  953:     */           {
/*  954:1321 */             throw new UTFDataFormatException("Malformed input at byte " + i);
/*  955:     */           }
/*  956:     */         }
/*  957:     */       }
/*  958:1326 */       return new String(charArray, 0, c);
/*  959:     */     }
/*  960:     */   }
/*  961:     */   
/*  962:     */   public static char highSurrogate(int codePoint)
/*  963:     */   {
/*  964:1332 */     return (char)((codePoint >>> 10) + 55232);
/*  965:     */   }
/*  966:     */   
/*  967:     */   public static char lowSurrogate(int codePoint)
/*  968:     */   {
/*  969:1337 */     return (char)((codePoint & 0x3FF) + 56320);
/*  970:     */   }
/*  971:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.StringSerializer
 * JD-Core Version:    0.7.0.1
 */