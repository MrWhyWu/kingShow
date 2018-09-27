/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public final class B64Code
/*   6:    */ {
/*   7:    */   static final byte pad = 61;
/*   8:    */   static final byte[] nibble2code;
/*   9:    */   static final byte[] code2nibble;
/*  10:    */   
/*  11:    */   static
/*  12:    */   {
/*  13: 54 */     nibble2code = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
/*  14:    */     
/*  15:    */ 
/*  16:    */ 
/*  17:    */ 
/*  18:    */ 
/*  19:    */ 
/*  20:    */ 
/*  21:    */ 
/*  22:    */ 
/*  23:    */ 
/*  24:    */ 
/*  25: 66 */     code2nibble = new byte[256];
/*  26: 67 */     for (int i = 0; i < 256; i++) {
/*  27: 68 */       code2nibble[i] = -1;
/*  28:    */     }
/*  29: 69 */     for (byte b = 0; b < 64; b = (byte)(b + 1)) {
/*  30: 70 */       code2nibble[nibble2code[b]] = b;
/*  31:    */     }
/*  32: 71 */     code2nibble[61] = 0;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static byte[] encode(byte[] input)
/*  36:    */   {
/*  37: 83 */     return encode(input, 0, input.length);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public static byte[] encode(byte[] input, int inOffset, int inLen)
/*  41:    */   {
/*  42: 91 */     byte[] output = new byte[(inLen + 2) / 3 * 4];
/*  43: 92 */     encode(input, inOffset, inLen, output, 0);
/*  44: 93 */     return output;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public static char[] cencode(byte[] input)
/*  48:    */   {
/*  49:101 */     return cencode(input, 0, input.length);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public static char[] cencode(byte[] input, int inOffset, int inLen)
/*  53:    */   {
/*  54:109 */     char[] output = new char[(inLen + 2) / 3 * 4];
/*  55:110 */     cencode(input, inOffset, inLen, output, 0);
/*  56:111 */     return output;
/*  57:    */   }
/*  58:    */   
/*  59:    */   private static void encode(byte[] input, int inOffset, int inLen, byte[] output, int outOffset)
/*  60:    */   {
/*  61:127 */     int remaining = inLen % 3;int stop = inOffset + (inLen - remaining);
/*  62:128 */     while (inOffset < stop)
/*  63:    */     {
/*  64:130 */       byte b0 = input[(inOffset++)];
/*  65:131 */       byte b1 = input[(inOffset++)];
/*  66:132 */       byte b2 = input[(inOffset++)];
/*  67:133 */       output[(outOffset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/*  68:134 */       output[(outOffset++)] = nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)];
/*  69:135 */       output[(outOffset++)] = nibble2code[(b1 << 2 & 0x3F | b2 >>> 6 & 0x3)];
/*  70:136 */       output[(outOffset++)] = nibble2code[(b2 & 0x3F)];
/*  71:    */     }
/*  72:139 */     switch (remaining)
/*  73:    */     {
/*  74:    */     case 0: 
/*  75:    */       break;
/*  76:    */     case 1: 
/*  77:144 */       byte b0 = input[(inOffset++)];
/*  78:145 */       output[(outOffset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/*  79:146 */       output[(outOffset++)] = nibble2code[(b0 << 4 & 0x3F)];
/*  80:147 */       output[(outOffset++)] = 61;
/*  81:148 */       output[(outOffset++)] = 61;
/*  82:149 */       break;
/*  83:    */     case 2: 
/*  84:151 */       byte b0 = input[(inOffset++)];
/*  85:152 */       byte b1 = input[(inOffset++)];
/*  86:153 */       output[(outOffset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/*  87:154 */       output[(outOffset++)] = nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)];
/*  88:155 */       output[(outOffset++)] = nibble2code[(b1 << 2 & 0x3F)];
/*  89:156 */       output[(outOffset++)] = 61;
/*  90:157 */       break;
/*  91:    */     default: 
/*  92:160 */       throw new IllegalStateException("should not happen");
/*  93:    */     }
/*  94:    */   }
/*  95:    */   
/*  96:    */   private static void cencode(byte[] input, int inOffset, int inLen, char[] output, int outOffset)
/*  97:    */   {
/*  98:177 */     int remaining = inLen % 3;int stop = inOffset + (inLen - remaining);
/*  99:178 */     while (inOffset < stop)
/* 100:    */     {
/* 101:180 */       byte b0 = input[(inOffset++)];
/* 102:181 */       byte b1 = input[(inOffset++)];
/* 103:182 */       byte b2 = input[(inOffset++)];
/* 104:183 */       output[(outOffset++)] = ((char)nibble2code[(b0 >>> 2 & 0x3F)]);
/* 105:184 */       output[(outOffset++)] = ((char)nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)]);
/* 106:185 */       output[(outOffset++)] = ((char)nibble2code[(b1 << 2 & 0x3F | b2 >>> 6 & 0x3)]);
/* 107:186 */       output[(outOffset++)] = ((char)nibble2code[(b2 & 0x3F)]);
/* 108:    */     }
/* 109:189 */     switch (remaining)
/* 110:    */     {
/* 111:    */     case 0: 
/* 112:    */       break;
/* 113:    */     case 1: 
/* 114:194 */       byte b0 = input[(inOffset++)];
/* 115:195 */       output[(outOffset++)] = ((char)nibble2code[(b0 >>> 2 & 0x3F)]);
/* 116:196 */       output[(outOffset++)] = ((char)nibble2code[(b0 << 4 & 0x3F)]);
/* 117:197 */       output[(outOffset++)] = '=';
/* 118:198 */       output[(outOffset++)] = '=';
/* 119:199 */       break;
/* 120:    */     case 2: 
/* 121:201 */       byte b0 = input[(inOffset++)];
/* 122:202 */       byte b1 = input[(inOffset++)];
/* 123:203 */       output[(outOffset++)] = ((char)nibble2code[(b0 >>> 2 & 0x3F)]);
/* 124:204 */       output[(outOffset++)] = ((char)nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)]);
/* 125:205 */       output[(outOffset++)] = ((char)nibble2code[(b1 << 2 & 0x3F)]);
/* 126:206 */       output[(outOffset++)] = '=';
/* 127:207 */       break;
/* 128:    */     default: 
/* 129:210 */       throw new IllegalStateException("should not happen");
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   public static LinkedBuffer encode(byte[] input, int inOffset, int inLen, WriteSession session, LinkedBuffer lb)
/* 134:    */     throws IOException
/* 135:    */   {
/* 136:230 */     int outputSize = (inLen + 2) / 3 * 4;
/* 137:231 */     session.size += outputSize;
/* 138:    */     
/* 139:233 */     int available = lb.buffer.length - lb.offset;
/* 140:234 */     if (outputSize > available)
/* 141:    */     {
/* 142:236 */       int chunks = available / 4;
/* 143:237 */       if (chunks == 0)
/* 144:    */       {
/* 145:240 */         if (outputSize > session.nextBufferSize)
/* 146:    */         {
/* 147:242 */           byte[] encoded = new byte[outputSize];
/* 148:243 */           encode(input, inOffset, inLen, encoded, 0);
/* 149:    */           
/* 150:245 */           return new LinkedBuffer(session.nextBufferSize, new LinkedBuffer(encoded, 0, outputSize, lb));
/* 151:    */         }
/* 152:249 */         byte[] encoded = new byte[session.nextBufferSize];
/* 153:250 */         encode(input, inOffset, inLen, encoded, 0);
/* 154:    */         
/* 155:252 */         return new LinkedBuffer(encoded, 0, outputSize, lb);
/* 156:    */       }
/* 157:255 */       int inBefore = inOffset;
/* 158:256 */       byte[] buffer = lb.buffer;
/* 159:257 */       int offset = lb.offset;
/* 160:261 */       while (chunks-- > 0)
/* 161:    */       {
/* 162:263 */         byte b0 = input[(inOffset++)];
/* 163:264 */         byte b1 = input[(inOffset++)];
/* 164:265 */         byte b2 = input[(inOffset++)];
/* 165:266 */         buffer[(offset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/* 166:267 */         buffer[(offset++)] = nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)];
/* 167:268 */         buffer[(offset++)] = nibble2code[(b1 << 2 & 0x3F | b2 >>> 6 & 0x3)];
/* 168:269 */         buffer[(offset++)] = nibble2code[(b2 & 0x3F)];
/* 169:    */       }
/* 170:272 */       inLen -= inOffset - inBefore;
/* 171:    */       
/* 172:274 */       outputSize -= offset - lb.offset;
/* 173:    */       
/* 174:276 */       lb.offset = offset;
/* 175:278 */       if (outputSize > session.nextBufferSize)
/* 176:    */       {
/* 177:280 */         byte[] encoded = new byte[outputSize];
/* 178:281 */         encode(input, inOffset, inLen, encoded, 0);
/* 179:    */         
/* 180:283 */         return new LinkedBuffer(session.nextBufferSize, new LinkedBuffer(encoded, 0, outputSize, lb));
/* 181:    */       }
/* 182:287 */       byte[] encoded = new byte[session.nextBufferSize];
/* 183:288 */       encode(input, inOffset, inLen, encoded, 0);
/* 184:    */       
/* 185:290 */       return new LinkedBuffer(encoded, 0, outputSize, lb);
/* 186:    */     }
/* 187:293 */     encode(input, inOffset, inLen, lb.buffer, lb.offset);
/* 188:294 */     lb.offset += outputSize;
/* 189:    */     
/* 190:296 */     return lb;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public static LinkedBuffer sencode(byte[] input, int inOffset, int inLen, WriteSession session, LinkedBuffer lb)
/* 194:    */     throws IOException
/* 195:    */   {
/* 196:306 */     int outputSize = (inLen + 2) / 3 * 4;
/* 197:307 */     session.size += outputSize;
/* 198:    */     
/* 199:309 */     int available = lb.buffer.length - lb.offset;
/* 200:310 */     if (outputSize > available)
/* 201:    */     {
/* 202:312 */       byte[] buffer = lb.buffer;
/* 203:313 */       int offset = lb.offset;int remaining = inLen % 3;int chunks = available / 4;
/* 204:316 */       for (int stop = inOffset + (inLen - remaining); inOffset < stop; chunks--)
/* 205:    */       {
/* 206:318 */         if (chunks == 0)
/* 207:    */         {
/* 208:322 */           offset = session.flush(buffer, lb.start, offset - lb.start);
/* 209:    */           
/* 210:324 */           chunks = (buffer.length - offset) / 4;
/* 211:    */         }
/* 212:327 */         byte b0 = input[(inOffset++)];
/* 213:328 */         byte b1 = input[(inOffset++)];
/* 214:329 */         byte b2 = input[(inOffset++)];
/* 215:330 */         buffer[(offset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/* 216:331 */         buffer[(offset++)] = nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)];
/* 217:332 */         buffer[(offset++)] = nibble2code[(b1 << 2 & 0x3F | b2 >>> 6 & 0x3)];
/* 218:333 */         buffer[(offset++)] = nibble2code[(b2 & 0x3F)];
/* 219:    */       }
/* 220:336 */       switch (remaining)
/* 221:    */       {
/* 222:    */       case 0: 
/* 223:    */         break;
/* 224:    */       case 1: 
/* 225:341 */         if (chunks == 0) {
/* 226:342 */           offset = session.flush(buffer, lb.start, offset - lb.start);
/* 227:    */         }
/* 228:344 */         byte b0 = input[(inOffset++)];
/* 229:345 */         buffer[(offset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/* 230:346 */         buffer[(offset++)] = nibble2code[(b0 << 4 & 0x3F)];
/* 231:347 */         buffer[(offset++)] = 61;
/* 232:348 */         buffer[(offset++)] = 61;
/* 233:349 */         break;
/* 234:    */       case 2: 
/* 235:351 */         if (chunks == 0) {
/* 236:352 */           offset = session.flush(buffer, lb.start, offset - lb.start);
/* 237:    */         }
/* 238:354 */         byte b0 = input[(inOffset++)];
/* 239:355 */         byte b1 = input[(inOffset++)];
/* 240:356 */         buffer[(offset++)] = nibble2code[(b0 >>> 2 & 0x3F)];
/* 241:357 */         buffer[(offset++)] = nibble2code[(b0 << 4 & 0x3F | b1 >>> 4 & 0xF)];
/* 242:358 */         buffer[(offset++)] = nibble2code[(b1 << 2 & 0x3F)];
/* 243:359 */         buffer[(offset++)] = 61;
/* 244:360 */         break;
/* 245:    */       default: 
/* 246:363 */         throw new IllegalStateException("should not happen");
/* 247:    */       }
/* 248:366 */       lb.offset = offset;
/* 249:367 */       return lb;
/* 250:    */     }
/* 251:370 */     encode(input, inOffset, inLen, lb.buffer, lb.offset);
/* 252:371 */     lb.offset += outputSize;
/* 253:    */     
/* 254:373 */     return lb;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public static byte[] decode(byte[] b)
/* 258:    */   {
/* 259:381 */     return decode(b, 0, b.length);
/* 260:    */   }
/* 261:    */   
/* 262:    */   public static byte[] cdecode(char[] b)
/* 263:    */   {
/* 264:389 */     return cdecode(b, 0, b.length);
/* 265:    */   }
/* 266:    */   
/* 267:    */   public static byte[] decode(byte[] input, int inOffset, int inLen)
/* 268:    */   {
/* 269:415 */     if (inLen == 0) {
/* 270:416 */       return ByteString.EMPTY_BYTE_ARRAY;
/* 271:    */     }
/* 272:418 */     if (inLen % 4 != 0) {
/* 273:419 */       throw new IllegalArgumentException("Input block size is not 4");
/* 274:    */     }
/* 275:421 */     int withoutPaddingLen = inLen;int limit = inOffset + inLen;
/* 276:422 */     while (input[(--limit)] == 61) {
/* 277:423 */       withoutPaddingLen--;
/* 278:    */     }
/* 279:426 */     int outLen = withoutPaddingLen * 3 / 4;
/* 280:427 */     byte[] output = new byte[outLen];
/* 281:    */     
/* 282:429 */     decode(input, inOffset, inLen, output, 0, outLen);
/* 283:    */     
/* 284:431 */     return output;
/* 285:    */   }
/* 286:    */   
/* 287:    */   public static byte[] cdecode(char[] input, int inOffset, int inLen)
/* 288:    */   {
/* 289:455 */     if (inLen == 0) {
/* 290:456 */       return ByteString.EMPTY_BYTE_ARRAY;
/* 291:    */     }
/* 292:458 */     if (inLen % 4 != 0) {
/* 293:459 */       throw new IllegalArgumentException("Input block size is not 4");
/* 294:    */     }
/* 295:461 */     int withoutPaddingLen = inLen;int limit = inOffset + inLen;
/* 296:462 */     while (input[(--limit)] == '=') {
/* 297:463 */       withoutPaddingLen--;
/* 298:    */     }
/* 299:466 */     int outLen = withoutPaddingLen * 3 / 4;
/* 300:467 */     byte[] output = new byte[outLen];
/* 301:    */     
/* 302:469 */     cdecode(input, inOffset, inLen, output, 0, outLen);
/* 303:    */     
/* 304:471 */     return output;
/* 305:    */   }
/* 306:    */   
/* 307:    */   public static int decodeTo(byte[] output, int outOffset, byte[] input, int inOffset, int inLen)
/* 308:    */   {
/* 309:481 */     if (inLen == 0) {
/* 310:482 */       return 0;
/* 311:    */     }
/* 312:484 */     if (inLen % 4 != 0) {
/* 313:485 */       throw new IllegalArgumentException("Input block size is not 4");
/* 314:    */     }
/* 315:487 */     int withoutPaddingLen = inLen;int limit = inOffset + inLen;
/* 316:488 */     while (input[(--limit)] == 61) {
/* 317:489 */       withoutPaddingLen--;
/* 318:    */     }
/* 319:492 */     int outLen = withoutPaddingLen * 3 / 4;
/* 320:493 */     assert (output.length - outOffset >= outLen);
/* 321:    */     
/* 322:495 */     decode(input, inOffset, inLen, output, outOffset, outLen);
/* 323:    */     
/* 324:497 */     return outLen;
/* 325:    */   }
/* 326:    */   
/* 327:    */   private static void decode(byte[] input, int inOffset, int inLen, byte[] output, int outOffset, int outLen)
/* 328:    */   {
/* 329:503 */     int stop = outLen / 3 * 3;
/* 330:    */     try
/* 331:    */     {
/* 332:507 */       while (outOffset < stop)
/* 333:    */       {
/* 334:509 */         byte b0 = code2nibble[input[(inOffset++)]];
/* 335:510 */         byte b1 = code2nibble[input[(inOffset++)]];
/* 336:511 */         byte b2 = code2nibble[input[(inOffset++)]];
/* 337:512 */         byte b3 = code2nibble[input[(inOffset++)]];
/* 338:513 */         if ((b0 < 0) || (b1 < 0) || (b2 < 0) || (b3 < 0)) {
/* 339:514 */           throw new IllegalArgumentException("Not B64 encoded");
/* 340:    */         }
/* 341:516 */         output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 342:517 */         output[(outOffset++)] = ((byte)(b1 << 4 | b2 >>> 2));
/* 343:518 */         output[(outOffset++)] = ((byte)(b2 << 6 | b3));
/* 344:    */       }
/* 345:521 */       if (outLen != outOffset) {
/* 346:523 */         switch (outLen % 3)
/* 347:    */         {
/* 348:    */         case 0: 
/* 349:    */           break;
/* 350:    */         case 1: 
/* 351:528 */           byte b0 = code2nibble[input[(inOffset++)]];
/* 352:529 */           byte b1 = code2nibble[input[(inOffset++)]];
/* 353:530 */           if ((b0 < 0) || (b1 < 0)) {
/* 354:531 */             throw new IllegalArgumentException("Not B64 encoded");
/* 355:    */           }
/* 356:532 */           output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 357:533 */           break;
/* 358:    */         case 2: 
/* 359:535 */           byte b0 = code2nibble[input[(inOffset++)]];
/* 360:536 */           byte b1 = code2nibble[input[(inOffset++)]];
/* 361:537 */           byte b2 = code2nibble[input[(inOffset++)]];
/* 362:538 */           if ((b0 < 0) || (b1 < 0) || (b2 < 0)) {
/* 363:539 */             throw new IllegalArgumentException("Not B64 encoded");
/* 364:    */           }
/* 365:540 */           output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 366:541 */           output[(outOffset++)] = ((byte)(b1 << 4 | b2 >>> 2));
/* 367:542 */           break;
/* 368:    */         default: 
/* 369:545 */           throw new IllegalStateException("should not happen");
/* 370:    */         }
/* 371:    */       }
/* 372:    */     }
/* 373:    */     catch (IndexOutOfBoundsException e)
/* 374:    */     {
/* 375:551 */       throw new IllegalArgumentException("char " + inOffset + " was not B64 encoded");
/* 376:    */     }
/* 377:    */   }
/* 378:    */   
/* 379:    */   private static void cdecode(char[] input, int inOffset, int inLen, byte[] output, int outOffset, int outLen)
/* 380:    */   {
/* 381:559 */     int stop = outLen / 3 * 3;
/* 382:    */     try
/* 383:    */     {
/* 384:563 */       while (outOffset < stop)
/* 385:    */       {
/* 386:565 */         byte b0 = code2nibble[input[(inOffset++)]];
/* 387:566 */         byte b1 = code2nibble[input[(inOffset++)]];
/* 388:567 */         byte b2 = code2nibble[input[(inOffset++)]];
/* 389:568 */         byte b3 = code2nibble[input[(inOffset++)]];
/* 390:569 */         if ((b0 < 0) || (b1 < 0) || (b2 < 0) || (b3 < 0)) {
/* 391:570 */           throw new IllegalArgumentException("Not B64 encoded");
/* 392:    */         }
/* 393:572 */         output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 394:573 */         output[(outOffset++)] = ((byte)(b1 << 4 | b2 >>> 2));
/* 395:574 */         output[(outOffset++)] = ((byte)(b2 << 6 | b3));
/* 396:    */       }
/* 397:577 */       if (outLen != outOffset) {
/* 398:579 */         switch (outLen % 3)
/* 399:    */         {
/* 400:    */         case 0: 
/* 401:    */           break;
/* 402:    */         case 1: 
/* 403:584 */           byte b0 = code2nibble[input[(inOffset++)]];
/* 404:585 */           byte b1 = code2nibble[input[(inOffset++)]];
/* 405:586 */           if ((b0 < 0) || (b1 < 0)) {
/* 406:587 */             throw new IllegalArgumentException("Not B64 encoded");
/* 407:    */           }
/* 408:588 */           output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 409:589 */           break;
/* 410:    */         case 2: 
/* 411:591 */           byte b0 = code2nibble[input[(inOffset++)]];
/* 412:592 */           byte b1 = code2nibble[input[(inOffset++)]];
/* 413:593 */           byte b2 = code2nibble[input[(inOffset++)]];
/* 414:594 */           if ((b0 < 0) || (b1 < 0) || (b2 < 0)) {
/* 415:595 */             throw new IllegalArgumentException("Not B64 encoded");
/* 416:    */           }
/* 417:596 */           output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 418:597 */           output[(outOffset++)] = ((byte)(b1 << 4 | b2 >>> 2));
/* 419:598 */           break;
/* 420:    */         default: 
/* 421:601 */           throw new IllegalStateException("should not happen");
/* 422:    */         }
/* 423:    */       }
/* 424:    */     }
/* 425:    */     catch (IndexOutOfBoundsException e)
/* 426:    */     {
/* 427:607 */       throw new IllegalArgumentException("char " + inOffset + " was not B64 encoded");
/* 428:    */     }
/* 429:    */   }
/* 430:    */   
/* 431:    */   public static byte[] decode(String str)
/* 432:    */   {
/* 433:617 */     return decode(str, 0, str.length());
/* 434:    */   }
/* 435:    */   
/* 436:    */   public static byte[] decode(String str, int inOffset, int inLen)
/* 437:    */   {
/* 438:625 */     if (inLen == 0) {
/* 439:626 */       return new byte[0];
/* 440:    */     }
/* 441:628 */     if (inLen % 4 != 0) {
/* 442:629 */       throw new IllegalArgumentException("Input block size is not 4");
/* 443:    */     }
/* 444:631 */     int withoutPaddingLen = inLen;int limit = inOffset + inLen;
/* 445:632 */     while (str.charAt(--limit) == '=') {
/* 446:633 */       withoutPaddingLen--;
/* 447:    */     }
/* 448:636 */     int outLen = withoutPaddingLen * 3 / 4;
/* 449:637 */     byte[] output = new byte[outLen];
/* 450:    */     
/* 451:639 */     decode(str, inOffset, inLen, output, 0, outLen);
/* 452:    */     
/* 453:641 */     return output;
/* 454:    */   }
/* 455:    */   
/* 456:    */   public static int decodeTo(byte[] output, int outOffset, String str, int inOffset, int inLen)
/* 457:    */   {
/* 458:651 */     if (inLen == 0) {
/* 459:652 */       return 0;
/* 460:    */     }
/* 461:654 */     if (inLen % 4 != 0) {
/* 462:655 */       throw new IllegalArgumentException("Input block size is not 4");
/* 463:    */     }
/* 464:657 */     int withoutPaddingLen = inLen;int limit = inOffset + inLen;
/* 465:658 */     while (str.charAt(--limit) == '=') {
/* 466:659 */       withoutPaddingLen--;
/* 467:    */     }
/* 468:662 */     int outLen = withoutPaddingLen * 3 / 4;
/* 469:663 */     assert (output.length - outOffset >= outLen);
/* 470:    */     
/* 471:665 */     decode(str, inOffset, inLen, output, outOffset, outLen);
/* 472:    */     
/* 473:667 */     return outLen;
/* 474:    */   }
/* 475:    */   
/* 476:    */   private static void decode(String str, int inOffset, int inLen, byte[] output, int outOffset, int outLen)
/* 477:    */   {
/* 478:673 */     int stop = outLen / 3 * 3;
/* 479:    */     try
/* 480:    */     {
/* 481:677 */       while (outOffset < stop)
/* 482:    */       {
/* 483:679 */         byte b0 = code2nibble[str.charAt(inOffset++)];
/* 484:680 */         byte b1 = code2nibble[str.charAt(inOffset++)];
/* 485:681 */         byte b2 = code2nibble[str.charAt(inOffset++)];
/* 486:682 */         byte b3 = code2nibble[str.charAt(inOffset++)];
/* 487:683 */         if ((b0 < 0) || (b1 < 0) || (b2 < 0) || (b3 < 0)) {
/* 488:684 */           throw new IllegalArgumentException("Not B64 encoded");
/* 489:    */         }
/* 490:686 */         output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 491:687 */         output[(outOffset++)] = ((byte)(b1 << 4 | b2 >>> 2));
/* 492:688 */         output[(outOffset++)] = ((byte)(b2 << 6 | b3));
/* 493:    */       }
/* 494:691 */       if (outLen != outOffset) {
/* 495:693 */         switch (outLen % 3)
/* 496:    */         {
/* 497:    */         case 0: 
/* 498:    */           break;
/* 499:    */         case 1: 
/* 500:698 */           byte b0 = code2nibble[str.charAt(inOffset++)];
/* 501:699 */           byte b1 = code2nibble[str.charAt(inOffset++)];
/* 502:700 */           if ((b0 < 0) || (b1 < 0)) {
/* 503:701 */             throw new IllegalArgumentException("Not B64 encoded");
/* 504:    */           }
/* 505:702 */           output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 506:703 */           break;
/* 507:    */         case 2: 
/* 508:705 */           byte b0 = code2nibble[str.charAt(inOffset++)];
/* 509:706 */           byte b1 = code2nibble[str.charAt(inOffset++)];
/* 510:707 */           byte b2 = code2nibble[str.charAt(inOffset++)];
/* 511:708 */           if ((b0 < 0) || (b1 < 0) || (b2 < 0)) {
/* 512:709 */             throw new IllegalArgumentException("Not B64 encoded");
/* 513:    */           }
/* 514:710 */           output[(outOffset++)] = ((byte)(b0 << 2 | b1 >>> 4));
/* 515:711 */           output[(outOffset++)] = ((byte)(b1 << 4 | b2 >>> 2));
/* 516:712 */           break;
/* 517:    */         default: 
/* 518:715 */           throw new IllegalStateException("should not happen");
/* 519:    */         }
/* 520:    */       }
/* 521:    */     }
/* 522:    */     catch (IndexOutOfBoundsException e)
/* 523:    */     {
/* 524:721 */       throw new IllegalArgumentException("char " + inOffset + " was not B64 encoded");
/* 525:    */     }
/* 526:    */   }
/* 527:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.B64Code
 * JD-Core Version:    0.7.0.1
 */