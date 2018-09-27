/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public final class StreamedStringSerializer
/*   6:    */ {
/*   7:    */   public static LinkedBuffer writeInt(int value, WriteSession session, LinkedBuffer lb)
/*   8:    */     throws IOException
/*   9:    */   {
/*  10: 38 */     if (value == -2147483648)
/*  11:    */     {
/*  12: 40 */       int valueLen = StringSerializer.INT_MIN_VALUE.length;
/*  13: 41 */       session.size += valueLen;
/*  14: 43 */       if (lb.offset + valueLen > lb.buffer.length) {
/*  15: 46 */         lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/*  16:    */       }
/*  17: 50 */       System.arraycopy(StringSerializer.INT_MIN_VALUE, 0, lb.buffer, lb.offset, valueLen);
/*  18:    */       
/*  19: 52 */       lb.offset += valueLen;
/*  20:    */       
/*  21: 54 */       return lb;
/*  22:    */     }
/*  23: 57 */     int size = value < 0 ? StringSerializer.stringSize(-value) + 1 : StringSerializer.stringSize(value);
/*  24: 58 */     session.size += size;
/*  25: 60 */     if (lb.offset + size > lb.buffer.length) {
/*  26: 63 */       lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/*  27:    */     }
/*  28: 67 */     StringSerializer.putBytesFromInt(value, lb.offset, size, lb.buffer);
/*  29:    */     
/*  30: 69 */     lb.offset += size;
/*  31:    */     
/*  32: 71 */     return lb;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static LinkedBuffer writeLong(long value, WriteSession session, LinkedBuffer lb)
/*  36:    */     throws IOException
/*  37:    */   {
/*  38: 80 */     if (value == -9223372036854775808L)
/*  39:    */     {
/*  40: 82 */       int valueLen = StringSerializer.LONG_MIN_VALUE.length;
/*  41: 83 */       session.size += valueLen;
/*  42: 85 */       if (lb.offset + valueLen > lb.buffer.length) {
/*  43: 89 */         lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/*  44:    */       }
/*  45: 93 */       System.arraycopy(StringSerializer.LONG_MIN_VALUE, 0, lb.buffer, lb.offset, valueLen);
/*  46:    */       
/*  47: 95 */       lb.offset += valueLen;
/*  48:    */       
/*  49: 97 */       return lb;
/*  50:    */     }
/*  51:100 */     int size = value < 0L ? StringSerializer.stringSize(-value) + 1 : StringSerializer.stringSize(value);
/*  52:101 */     session.size += size;
/*  53:103 */     if (lb.offset + size > lb.buffer.length) {
/*  54:107 */       lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/*  55:    */     }
/*  56:111 */     StringSerializer.putBytesFromLong(value, lb.offset, size, lb.buffer);
/*  57:    */     
/*  58:113 */     lb.offset += size;
/*  59:    */     
/*  60:115 */     return lb;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public static LinkedBuffer writeFloat(float value, WriteSession session, LinkedBuffer lb)
/*  64:    */     throws IOException
/*  65:    */   {
/*  66:125 */     return writeAscii(Float.toString(value), session, lb);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public static LinkedBuffer writeDouble(double value, WriteSession session, LinkedBuffer lb)
/*  70:    */     throws IOException
/*  71:    */   {
/*  72:135 */     return writeAscii(Double.toString(value), session, lb);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public static LinkedBuffer writeUTF8(CharSequence str, WriteSession session, LinkedBuffer lb)
/*  76:    */     throws IOException
/*  77:    */   {
/*  78:144 */     int len = str.length();
/*  79:145 */     if (len == 0) {
/*  80:146 */       return lb;
/*  81:    */     }
/*  82:148 */     byte[] buffer = lb.buffer;
/*  83:149 */     int limit = buffer.length;int offset = lb.offset;int i = 0;
/*  84:    */     do
/*  85:    */     {
/*  86:154 */       char c = str.charAt(i++);
/*  87:155 */       if (c < '')
/*  88:    */       {
/*  89:157 */         if (offset == limit)
/*  90:    */         {
/*  91:159 */           session.size += offset - lb.offset;
/*  92:160 */           lb.offset = (offset = session.flush(buffer, lb.start, offset - lb.start));
/*  93:    */         }
/*  94:163 */         buffer[(offset++)] = ((byte)c);
/*  95:    */       }
/*  96:165 */       else if (c < 'ࠀ')
/*  97:    */       {
/*  98:167 */         if (offset + 2 > limit)
/*  99:    */         {
/* 100:169 */           session.size += offset - lb.offset;
/* 101:170 */           lb.offset = (offset = session.flush(buffer, lb.start, offset - lb.start));
/* 102:    */         }
/* 103:173 */         buffer[(offset++)] = ((byte)(0xC0 | c >> '\006' & 0x1F));
/* 104:174 */         buffer[(offset++)] = ((byte)(0x80 | c >> '\000' & 0x3F));
/* 105:    */       }
/* 106:176 */       else if ((Character.isHighSurrogate(c)) && (i < len) && (Character.isLowSurrogate(str.charAt(i))))
/* 107:    */       {
/* 108:179 */         if (offset + 4 > buffer.length)
/* 109:    */         {
/* 110:181 */           session.size += offset - lb.offset;
/* 111:182 */           lb.offset = (offset = session.flush(buffer, lb.start, offset - lb.start));
/* 112:    */         }
/* 113:185 */         int codePoint = Character.toCodePoint(c, str.charAt(i));
/* 114:186 */         buffer[(offset++)] = ((byte)(0xF0 | codePoint >> 18 & 0x7));
/* 115:187 */         buffer[(offset++)] = ((byte)(0x80 | codePoint >> 12 & 0x3F));
/* 116:188 */         buffer[(offset++)] = ((byte)(0x80 | codePoint >> 6 & 0x3F));
/* 117:189 */         buffer[(offset++)] = ((byte)(0x80 | codePoint >> 0 & 0x3F));
/* 118:    */         
/* 119:191 */         i++;
/* 120:    */       }
/* 121:    */       else
/* 122:    */       {
/* 123:195 */         if (offset + 3 > limit)
/* 124:    */         {
/* 125:197 */           session.size += offset - lb.offset;
/* 126:198 */           lb.offset = (offset = session.flush(buffer, lb.start, offset - lb.start));
/* 127:    */         }
/* 128:201 */         buffer[(offset++)] = ((byte)(0xE0 | c >> '\f' & 0xF));
/* 129:202 */         buffer[(offset++)] = ((byte)(0x80 | c >> '\006' & 0x3F));
/* 130:203 */         buffer[(offset++)] = ((byte)(0x80 | c >> '\000' & 0x3F));
/* 131:    */       }
/* 132:205 */     } while (i < len);
/* 133:207 */     session.size += offset - lb.offset;
/* 134:208 */     lb.offset = offset;
/* 135:    */     
/* 136:210 */     return lb;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public static LinkedBuffer writeAscii(CharSequence str, WriteSession session, LinkedBuffer lb)
/* 140:    */     throws IOException
/* 141:    */   {
/* 142:221 */     int len = str.length();
/* 143:222 */     if (len == 0) {
/* 144:223 */       return lb;
/* 145:    */     }
/* 146:225 */     int offset = lb.offset;
/* 147:226 */     int limit = lb.buffer.length;
/* 148:227 */     byte[] buffer = lb.buffer;
/* 149:    */     
/* 150:    */ 
/* 151:230 */     session.size += len;
/* 152:232 */     if (offset + len > limit)
/* 153:    */     {
/* 154:235 */       int index = 0;int start = lb.start;int bufSize = limit - start;int available = limit - offset;int remaining = len - available;
/* 155:239 */       while (available-- > 0) {
/* 156:240 */         buffer[(offset++)] = ((byte)str.charAt(index++));
/* 157:    */       }
/* 158:243 */       offset = session.flush(buffer, start, bufSize);
/* 159:245 */       while (remaining-- > 0)
/* 160:    */       {
/* 161:247 */         if (offset == limit) {
/* 162:248 */           offset = session.flush(buffer, start, bufSize);
/* 163:    */         }
/* 164:250 */         buffer[(offset++)] = ((byte)str.charAt(index++));
/* 165:    */       }
/* 166:    */     }
/* 167:    */     else
/* 168:    */     {
/* 169:256 */       for (int i = 0; i < len; i++) {
/* 170:257 */         buffer[(offset++)] = ((byte)str.charAt(i));
/* 171:    */       }
/* 172:    */     }
/* 173:260 */     lb.offset = offset;
/* 174:    */     
/* 175:262 */     return lb;
/* 176:    */   }
/* 177:    */   
/* 178:    */   private static void flushAndReset(LinkedBuffer node, WriteSession session)
/* 179:    */     throws IOException
/* 180:    */   {
/* 181:    */     do
/* 182:    */     {
/* 183:    */       int len;
/* 184:271 */       if ((len = node.offset - node.start) > 0) {
/* 185:272 */         node.offset = session.flush(node, node.buffer, node.start, len);
/* 186:    */       }
/* 187:273 */     } while ((node = node.next) != null);
/* 188:    */   }
/* 189:    */   
/* 190:    */   public static LinkedBuffer writeUTF8FixedDelimited(CharSequence str, WriteSession session, LinkedBuffer lb)
/* 191:    */     throws IOException
/* 192:    */   {
/* 193:284 */     return writeUTF8FixedDelimited(str, false, session, lb);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public static LinkedBuffer writeUTF8FixedDelimited(CharSequence str, boolean littleEndian, WriteSession session, LinkedBuffer lb)
/* 197:    */     throws IOException
/* 198:    */   {
/* 199:294 */     int lastSize = session.size;int len = str.length();int withIntOffset = lb.offset + 2;
/* 200:297 */     if (withIntOffset + len > lb.buffer.length)
/* 201:    */     {
/* 202:300 */       lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/* 203:301 */       withIntOffset = lb.offset + 2;
/* 204:303 */       if (len == 0)
/* 205:    */       {
/* 206:305 */         StringSerializer.writeFixed2ByteInt(0, lb.buffer, withIntOffset - 2, littleEndian);
/* 207:306 */         lb.offset = withIntOffset;
/* 208:    */         
/* 209:308 */         session.size += 2;
/* 210:309 */         return lb;
/* 211:    */       }
/* 212:313 */       if (withIntOffset + len > lb.buffer.length)
/* 213:    */       {
/* 214:315 */         lb.offset = withIntOffset;
/* 215:    */         
/* 216:    */ 
/* 217:318 */         LinkedBuffer rb = StringSerializer.writeUTF8(str, 0, len, lb.buffer, withIntOffset, lb.buffer.length, session, lb);
/* 218:    */         
/* 219:    */ 
/* 220:321 */         StringSerializer.writeFixed2ByteInt(session.size - lastSize, lb.buffer, withIntOffset - 2, littleEndian);
/* 221:    */         
/* 222:    */ 
/* 223:    */ 
/* 224:325 */         session.size += 2;
/* 225:    */         
/* 226:327 */         assert (rb != lb);
/* 227:    */         
/* 228:329 */         flushAndReset(lb, session);
/* 229:    */         
/* 230:331 */         return lb;
/* 231:    */       }
/* 232:    */     }
/* 233:334 */     else if (len == 0)
/* 234:    */     {
/* 235:336 */       StringSerializer.writeFixed2ByteInt(0, lb.buffer, withIntOffset - 2, littleEndian);
/* 236:337 */       lb.offset = withIntOffset;
/* 237:    */       
/* 238:339 */       session.size += 2;
/* 239:340 */       return lb;
/* 240:    */     }
/* 241:344 */     lb.offset = withIntOffset;
/* 242:    */     
/* 243:346 */     LinkedBuffer rb = StringSerializer.writeUTF8(str, 0, len, session, lb);
/* 244:    */     
/* 245:348 */     StringSerializer.writeFixed2ByteInt(session.size - lastSize, lb.buffer, withIntOffset - 2, littleEndian);
/* 246:    */     
/* 247:    */ 
/* 248:    */ 
/* 249:352 */     session.size += 2;
/* 250:354 */     if (rb != lb) {
/* 251:357 */       flushAndReset(lb, session);
/* 252:    */     }
/* 253:360 */     return lb;
/* 254:    */   }
/* 255:    */   
/* 256:    */   private static LinkedBuffer writeUTF8OneByteDelimited(CharSequence str, int index, int len, WriteSession session, LinkedBuffer lb)
/* 257:    */     throws IOException
/* 258:    */   {
/* 259:367 */     int lastSize = session.size;int withIntOffset = lb.offset + 1;
/* 260:370 */     if (withIntOffset + len > lb.buffer.length)
/* 261:    */     {
/* 262:373 */       lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/* 263:374 */       withIntOffset = lb.offset + 1;
/* 264:    */     }
/* 265:378 */     lb.offset = withIntOffset;
/* 266:    */     
/* 267:380 */     LinkedBuffer rb = StringSerializer.writeUTF8(str, index, len, session, lb);
/* 268:    */     
/* 269:382 */     lb.buffer[(withIntOffset - 1)] = ((byte)(session.size - lastSize));
/* 270:    */     
/* 271:    */ 
/* 272:385 */     session.size += 1;
/* 273:387 */     if (rb != lb) {
/* 274:390 */       flushAndReset(lb, session);
/* 275:    */     }
/* 276:393 */     return lb;
/* 277:    */   }
/* 278:    */   
/* 279:    */   private static LinkedBuffer writeUTF8VarDelimited(CharSequence str, int index, int len, int lowerLimit, int expectedSize, WriteSession session, LinkedBuffer lb)
/* 280:    */     throws IOException
/* 281:    */   {
/* 282:401 */     int lastSize = session.size;int offset = lb.offset;int withIntOffset = offset + expectedSize;
/* 283:404 */     if (withIntOffset + len > lb.buffer.length)
/* 284:    */     {
/* 285:407 */       offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/* 286:408 */       withIntOffset = offset + expectedSize;
/* 287:411 */       if (withIntOffset + len > lb.buffer.length)
/* 288:    */       {
/* 289:414 */         lb.offset = withIntOffset;
/* 290:    */         
/* 291:    */ 
/* 292:417 */         LinkedBuffer rb = StringSerializer.writeUTF8(str, index, len, lb.buffer, withIntOffset, lb.buffer.length, session, lb);
/* 293:    */         
/* 294:    */ 
/* 295:420 */         int size = session.size - lastSize;
/* 296:422 */         if (size < lowerLimit)
/* 297:    */         {
/* 298:424 */           session.size += --expectedSize;
/* 299:    */           
/* 300:    */ 
/* 301:    */ 
/* 302:428 */           offset++;int o = offset;
/* 303:430 */           for (;; size >>>= 7)
/* 304:    */           {
/* 305:430 */             expectedSize--;
/* 306:430 */             if (expectedSize <= 0) {
/* 307:    */               break;
/* 308:    */             }
/* 309:431 */             lb.buffer[(o++)] = ((byte)(size & 0x7F | 0x80));
/* 310:    */           }
/* 311:433 */           lb.buffer[o] = ((byte)size);
/* 312:    */           
/* 313:    */ 
/* 314:436 */           lb.offset = session.flush(lb, lb.buffer, offset, lb.offset - offset);
/* 315:    */           
/* 316:    */ 
/* 317:439 */           assert (rb != lb);
/* 318:    */           
/* 319:441 */           flushAndReset(lb.next, session);
/* 320:    */           
/* 321:443 */           return lb;
/* 322:    */         }
/* 323:447 */         session.size += expectedSize;
/* 324:449 */         for (;; size >>>= 7)
/* 325:    */         {
/* 326:449 */           expectedSize--;
/* 327:449 */           if (expectedSize <= 0) {
/* 328:    */             break;
/* 329:    */           }
/* 330:450 */           lb.buffer[(offset++)] = ((byte)(size & 0x7F | 0x80));
/* 331:    */         }
/* 332:452 */         lb.buffer[offset] = ((byte)size);
/* 333:    */         
/* 334:454 */         assert (rb != lb);
/* 335:    */         
/* 336:456 */         flushAndReset(lb, session);
/* 337:    */         
/* 338:458 */         return lb;
/* 339:    */       }
/* 340:    */     }
/* 341:463 */     lb.offset = withIntOffset;
/* 342:    */     
/* 343:465 */     LinkedBuffer rb = StringSerializer.writeUTF8(str, index, len, session, lb);
/* 344:    */     
/* 345:467 */     int size = session.size - lastSize;
/* 346:469 */     if (size < lowerLimit)
/* 347:    */     {
/* 348:474 */       if ((rb != lb) || (expectedSize != 2))
/* 349:    */       {
/* 350:477 */         session.size += --expectedSize;
/* 351:    */         
/* 352:    */ 
/* 353:480 */         int existingOffset = offset;offset++;int o = offset;
/* 354:482 */         for (;; size >>>= 7)
/* 355:    */         {
/* 356:482 */           expectedSize--;
/* 357:482 */           if (expectedSize <= 0) {
/* 358:    */             break;
/* 359:    */           }
/* 360:483 */           lb.buffer[(o++)] = ((byte)(size & 0x7F | 0x80));
/* 361:    */         }
/* 362:485 */         lb.buffer[o] = ((byte)size);
/* 363:487 */         if (existingOffset == lb.start) {
/* 364:491 */           lb.offset = session.flush(lb, lb.buffer, offset, lb.offset - offset);
/* 365:    */         } else {
/* 366:496 */           lb.offset = session.flush(lb.buffer, lb.start, existingOffset - lb.start, lb.buffer, offset, lb.offset - offset);
/* 367:    */         }
/* 368:500 */         if (rb != lb) {
/* 369:503 */           flushAndReset(lb.next, session);
/* 370:    */         }
/* 371:506 */         return lb;
/* 372:    */       }
/* 373:510 */       System.arraycopy(lb.buffer, withIntOffset, lb.buffer, withIntOffset - 1, lb.offset - withIntOffset);
/* 374:    */       
/* 375:    */ 
/* 376:513 */       expectedSize--;
/* 377:514 */       lb.offset -= 1;
/* 378:    */     }
/* 379:518 */     session.size += expectedSize;
/* 380:520 */     for (;; size >>>= 7)
/* 381:    */     {
/* 382:520 */       expectedSize--;
/* 383:520 */       if (expectedSize <= 0) {
/* 384:    */         break;
/* 385:    */       }
/* 386:521 */       lb.buffer[(offset++)] = ((byte)(size & 0x7F | 0x80));
/* 387:    */     }
/* 388:523 */     lb.buffer[offset] = ((byte)size);
/* 389:525 */     if (rb != lb) {
/* 390:528 */       flushAndReset(lb, session);
/* 391:    */     }
/* 392:531 */     return lb;
/* 393:    */   }
/* 394:    */   
/* 395:    */   public static LinkedBuffer writeUTF8VarDelimited(CharSequence str, WriteSession session, LinkedBuffer lb)
/* 396:    */     throws IOException
/* 397:    */   {
/* 398:540 */     int len = str.length();
/* 399:541 */     if (len == 0)
/* 400:    */     {
/* 401:543 */       if (lb.offset == lb.buffer.length) {
/* 402:547 */         lb.offset = session.flush(lb.buffer, lb.start, lb.offset - lb.start);
/* 403:    */       }
/* 404:551 */       lb.buffer[(lb.offset++)] = 0;
/* 405:    */       
/* 406:553 */       session.size += 1;
/* 407:554 */       return lb;
/* 408:    */     }
/* 409:557 */     if (len < 43) {
/* 410:560 */       return writeUTF8OneByteDelimited(str, 0, len, session, lb);
/* 411:    */     }
/* 412:563 */     if (len < 5462) {
/* 413:566 */       return writeUTF8VarDelimited(str, 0, len, 128, 2, session, lb);
/* 414:    */     }
/* 415:570 */     if (len < 699051) {
/* 416:573 */       return writeUTF8VarDelimited(str, 0, len, 16384, 3, session, lb);
/* 417:    */     }
/* 418:577 */     if (len < 89478486) {
/* 419:580 */       return writeUTF8VarDelimited(str, 0, len, 2097152, 4, session, lb);
/* 420:    */     }
/* 421:585 */     return writeUTF8VarDelimited(str, 0, len, 268435456, 5, session, lb);
/* 422:    */   }
/* 423:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.StreamedStringSerializer
 * JD-Core Version:    0.7.0.1
 */