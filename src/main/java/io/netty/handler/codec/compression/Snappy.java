/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ 
/*   5:    */ public final class Snappy
/*   6:    */ {
/*   7:    */   private static final int MAX_HT_SIZE = 16384;
/*   8:    */   private static final int MIN_COMPRESSIBLE_BYTES = 15;
/*   9:    */   private static final int PREAMBLE_NOT_FULL = -1;
/*  10:    */   private static final int NOT_ENOUGH_INPUT = -1;
/*  11:    */   private static final int LITERAL = 0;
/*  12:    */   private static final int COPY_1_BYTE_OFFSET = 1;
/*  13:    */   private static final int COPY_2_BYTE_OFFSET = 2;
/*  14:    */   private static final int COPY_4_BYTE_OFFSET = 3;
/*  15: 41 */   private State state = State.READY;
/*  16:    */   private byte tag;
/*  17:    */   private int written;
/*  18:    */   
/*  19:    */   private static enum State
/*  20:    */   {
/*  21: 46 */     READY,  READING_PREAMBLE,  READING_TAG,  READING_LITERAL,  READING_COPY;
/*  22:    */     
/*  23:    */     private State() {}
/*  24:    */   }
/*  25:    */   
/*  26:    */   public void reset()
/*  27:    */   {
/*  28: 54 */     this.state = State.READY;
/*  29: 55 */     this.tag = 0;
/*  30: 56 */     this.written = 0;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void encode(ByteBuf in, ByteBuf out, int length)
/*  34:    */   {
/*  35: 61 */     for (int i = 0;; i++)
/*  36:    */     {
/*  37: 62 */       int b = length >>> i * 7;
/*  38: 63 */       if ((b & 0xFFFFFF80) != 0)
/*  39:    */       {
/*  40: 64 */         out.writeByte(b & 0x7F | 0x80);
/*  41:    */       }
/*  42:    */       else
/*  43:    */       {
/*  44: 66 */         out.writeByte(b);
/*  45: 67 */         break;
/*  46:    */       }
/*  47:    */     }
/*  48: 71 */     int inIndex = in.readerIndex();
/*  49: 72 */     int baseIndex = inIndex;
/*  50:    */     
/*  51: 74 */     short[] table = getHashTable(length);
/*  52: 75 */     int shift = Integer.numberOfLeadingZeros(table.length) + 1;
/*  53:    */     
/*  54: 77 */     int nextEmit = inIndex;
/*  55: 79 */     if (length - inIndex >= 15)
/*  56:    */     {
/*  57: 80 */       int nextHash = hash(in, ++inIndex, shift);
/*  58:    */       for (;;)
/*  59:    */       {
/*  60: 82 */         int skip = 32;
/*  61:    */         
/*  62:    */ 
/*  63: 85 */         int nextIndex = inIndex;
/*  64:    */         int candidate;
/*  65:    */         do
/*  66:    */         {
/*  67: 87 */           inIndex = nextIndex;
/*  68: 88 */           int hash = nextHash;
/*  69: 89 */           int bytesBetweenHashLookups = skip++ >> 5;
/*  70: 90 */           nextIndex = inIndex + bytesBetweenHashLookups;
/*  71: 93 */           if (nextIndex > length - 4) {
/*  72:    */             break;
/*  73:    */           }
/*  74: 97 */           nextHash = hash(in, nextIndex, shift);
/*  75:    */           
/*  76: 99 */           candidate = baseIndex + table[hash];
/*  77:    */           
/*  78:101 */           table[hash] = ((short)(inIndex - baseIndex));
/*  79:103 */         } while (in.getInt(inIndex) != in.getInt(candidate));
/*  80:105 */         encodeLiteral(in, out, inIndex - nextEmit);
/*  81:    */         int insertTail;
/*  82:    */         do
/*  83:    */         {
/*  84:109 */           int base = inIndex;
/*  85:110 */           int matched = 4 + findMatchingLength(in, candidate + 4, inIndex + 4, length);
/*  86:111 */           inIndex += matched;
/*  87:112 */           int offset = base - candidate;
/*  88:113 */           encodeCopy(out, offset, matched);
/*  89:114 */           in.readerIndex(in.readerIndex() + matched);
/*  90:115 */           insertTail = inIndex - 1;
/*  91:116 */           nextEmit = inIndex;
/*  92:117 */           if (inIndex >= length - 4) {
/*  93:    */             break;
/*  94:    */           }
/*  95:121 */           int prevHash = hash(in, insertTail, shift);
/*  96:122 */           table[prevHash] = ((short)(inIndex - baseIndex - 1));
/*  97:123 */           int currentHash = hash(in, insertTail + 1, shift);
/*  98:124 */           candidate = baseIndex + table[currentHash];
/*  99:125 */           table[currentHash] = ((short)(inIndex - baseIndex));
/* 100:127 */         } while (in.getInt(insertTail + 1) == in.getInt(candidate));
/* 101:129 */         nextHash = hash(in, insertTail + 2, shift);
/* 102:130 */         inIndex++;
/* 103:    */       }
/* 104:    */     }
/* 105:135 */     if (nextEmit < length) {
/* 106:136 */       encodeLiteral(in, out, length - nextEmit);
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   private static int hash(ByteBuf in, int index, int shift)
/* 111:    */   {
/* 112:151 */     return in.getInt(index) * 506832829 >>> shift;
/* 113:    */   }
/* 114:    */   
/* 115:    */   private static short[] getHashTable(int inputSize)
/* 116:    */   {
/* 117:161 */     int htSize = 256;
/* 118:162 */     while ((htSize < 16384) && (htSize < inputSize)) {
/* 119:163 */       htSize <<= 1;
/* 120:    */     }
/* 121:165 */     return new short[htSize];
/* 122:    */   }
/* 123:    */   
/* 124:    */   private static int findMatchingLength(ByteBuf in, int minIndex, int inIndex, int maxIndex)
/* 125:    */   {
/* 126:180 */     int matched = 0;
/* 127:182 */     while ((inIndex <= maxIndex - 4) && 
/* 128:183 */       (in.getInt(inIndex) == in.getInt(minIndex + matched)))
/* 129:    */     {
/* 130:184 */       inIndex += 4;
/* 131:185 */       matched += 4;
/* 132:    */     }
/* 133:188 */     while ((inIndex < maxIndex) && (in.getByte(minIndex + matched) == in.getByte(inIndex)))
/* 134:    */     {
/* 135:189 */       inIndex++;
/* 136:190 */       matched++;
/* 137:    */     }
/* 138:193 */     return matched;
/* 139:    */   }
/* 140:    */   
/* 141:    */   private static int bitsToEncode(int value)
/* 142:    */   {
/* 143:205 */     int highestOneBit = Integer.highestOneBit(value);
/* 144:206 */     int bitLength = 0;
/* 145:207 */     while (highestOneBit >>= 1 != 0) {
/* 146:208 */       bitLength++;
/* 147:    */     }
/* 148:211 */     return bitLength;
/* 149:    */   }
/* 150:    */   
/* 151:    */   static void encodeLiteral(ByteBuf in, ByteBuf out, int length)
/* 152:    */   {
/* 153:224 */     if (length < 61)
/* 154:    */     {
/* 155:225 */       out.writeByte(length - 1 << 2);
/* 156:    */     }
/* 157:    */     else
/* 158:    */     {
/* 159:227 */       int bitLength = bitsToEncode(length - 1);
/* 160:228 */       int bytesToEncode = 1 + bitLength / 8;
/* 161:229 */       out.writeByte(59 + bytesToEncode << 2);
/* 162:230 */       for (int i = 0; i < bytesToEncode; i++) {
/* 163:231 */         out.writeByte(length - 1 >> i * 8 & 0xFF);
/* 164:    */       }
/* 165:    */     }
/* 166:235 */     out.writeBytes(in, length);
/* 167:    */   }
/* 168:    */   
/* 169:    */   private static void encodeCopyWithOffset(ByteBuf out, int offset, int length)
/* 170:    */   {
/* 171:239 */     if ((length < 12) && (offset < 2048))
/* 172:    */     {
/* 173:240 */       out.writeByte(0x1 | length - 4 << 2 | offset >> 8 << 5);
/* 174:241 */       out.writeByte(offset & 0xFF);
/* 175:    */     }
/* 176:    */     else
/* 177:    */     {
/* 178:243 */       out.writeByte(0x2 | length - 1 << 2);
/* 179:244 */       out.writeByte(offset & 0xFF);
/* 180:245 */       out.writeByte(offset >> 8 & 0xFF);
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   private static void encodeCopy(ByteBuf out, int offset, int length)
/* 185:    */   {
/* 186:257 */     while (length >= 68)
/* 187:    */     {
/* 188:258 */       encodeCopyWithOffset(out, offset, 64);
/* 189:259 */       length -= 64;
/* 190:    */     }
/* 191:262 */     if (length > 64)
/* 192:    */     {
/* 193:263 */       encodeCopyWithOffset(out, offset, 60);
/* 194:264 */       length -= 60;
/* 195:    */     }
/* 196:267 */     encodeCopyWithOffset(out, offset, length);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public void decode(ByteBuf in, ByteBuf out)
/* 200:    */   {
/* 201:271 */     while (in.isReadable()) {
/* 202:272 */       switch (1.$SwitchMap$io$netty$handler$codec$compression$Snappy$State[this.state.ordinal()])
/* 203:    */       {
/* 204:    */       case 1: 
/* 205:274 */         this.state = State.READING_PREAMBLE;
/* 206:    */       case 2: 
/* 207:277 */         int uncompressedLength = readPreamble(in);
/* 208:278 */         if (uncompressedLength == -1) {
/* 209:280 */           return;
/* 210:    */         }
/* 211:282 */         if (uncompressedLength == 0)
/* 212:    */         {
/* 213:284 */           this.state = State.READY;
/* 214:285 */           return;
/* 215:    */         }
/* 216:287 */         out.ensureWritable(uncompressedLength);
/* 217:288 */         this.state = State.READING_TAG;
/* 218:    */       case 3: 
/* 219:291 */         if (!in.isReadable()) {
/* 220:292 */           return;
/* 221:    */         }
/* 222:294 */         this.tag = in.readByte();
/* 223:295 */         switch (this.tag & 0x3)
/* 224:    */         {
/* 225:    */         case 0: 
/* 226:297 */           this.state = State.READING_LITERAL;
/* 227:298 */           break;
/* 228:    */         case 1: 
/* 229:    */         case 2: 
/* 230:    */         case 3: 
/* 231:302 */           this.state = State.READING_COPY;
/* 232:    */         }
/* 233:305 */         break;
/* 234:    */       case 4: 
/* 235:307 */         int literalWritten = decodeLiteral(this.tag, in, out);
/* 236:308 */         if (literalWritten != -1)
/* 237:    */         {
/* 238:309 */           this.state = State.READING_TAG;
/* 239:310 */           this.written += literalWritten;
/* 240:    */         }
/* 241:    */         else
/* 242:    */         {
/* 243:    */           return;
/* 244:    */         }
/* 245:    */         break;
/* 246:    */       case 5: 
/* 247:318 */         switch (this.tag & 0x3)
/* 248:    */         {
/* 249:    */         case 1: 
/* 250:320 */           int decodeWritten = decodeCopyWith1ByteOffset(this.tag, in, out, this.written);
/* 251:321 */           if (decodeWritten != -1)
/* 252:    */           {
/* 253:322 */             this.state = State.READING_TAG;
/* 254:323 */             this.written += decodeWritten;
/* 255:    */           }
/* 256:    */           else
/* 257:    */           {
/* 258:    */             return;
/* 259:    */           }
/* 260:    */           break;
/* 261:    */         case 2: 
/* 262:330 */           int decodeWritten = decodeCopyWith2ByteOffset(this.tag, in, out, this.written);
/* 263:331 */           if (decodeWritten != -1)
/* 264:    */           {
/* 265:332 */             this.state = State.READING_TAG;
/* 266:333 */             this.written += decodeWritten;
/* 267:    */           }
/* 268:    */           else
/* 269:    */           {
/* 270:    */             return;
/* 271:    */           }
/* 272:    */           break;
/* 273:    */         case 3: 
/* 274:340 */           int decodeWritten = decodeCopyWith4ByteOffset(this.tag, in, out, this.written);
/* 275:341 */           if (decodeWritten != -1)
/* 276:    */           {
/* 277:342 */             this.state = State.READING_TAG;
/* 278:343 */             this.written += decodeWritten;
/* 279:    */           }
/* 280:    */           else
/* 281:    */           {
/* 282:    */             return;
/* 283:    */           }
/* 284:    */           break;
/* 285:    */         }
/* 286:    */         break;
/* 287:    */       }
/* 288:    */     }
/* 289:    */   }
/* 290:    */   
/* 291:    */   private static int readPreamble(ByteBuf in)
/* 292:    */   {
/* 293:364 */     int length = 0;
/* 294:365 */     int byteIndex = 0;
/* 295:366 */     while (in.isReadable())
/* 296:    */     {
/* 297:367 */       int current = in.readUnsignedByte();
/* 298:368 */       length |= (current & 0x7F) << byteIndex++ * 7;
/* 299:369 */       if ((current & 0x80) == 0) {
/* 300:370 */         return length;
/* 301:    */       }
/* 302:373 */       if (byteIndex >= 4) {
/* 303:374 */         throw new DecompressionException("Preamble is greater than 4 bytes");
/* 304:    */       }
/* 305:    */     }
/* 306:378 */     return 0;
/* 307:    */   }
/* 308:    */   
/* 309:    */   static int decodeLiteral(byte tag, ByteBuf in, ByteBuf out)
/* 310:    */   {
/* 311:393 */     in.markReaderIndex();
/* 312:    */     int length;
/* 313:    */     int length;
/* 314:    */     int length;
/* 315:    */     int length;
/* 316:    */     int length;
/* 317:395 */     switch (tag >> 2 & 0x3F)
/* 318:    */     {
/* 319:    */     case 60: 
/* 320:397 */       if (!in.isReadable()) {
/* 321:398 */         return -1;
/* 322:    */       }
/* 323:400 */       length = in.readUnsignedByte();
/* 324:401 */       break;
/* 325:    */     case 61: 
/* 326:403 */       if (in.readableBytes() < 2) {
/* 327:404 */         return -1;
/* 328:    */       }
/* 329:406 */       length = in.readShortLE();
/* 330:407 */       break;
/* 331:    */     case 62: 
/* 332:409 */       if (in.readableBytes() < 3) {
/* 333:410 */         return -1;
/* 334:    */       }
/* 335:412 */       length = in.readUnsignedMediumLE();
/* 336:413 */       break;
/* 337:    */     case 63: 
/* 338:415 */       if (in.readableBytes() < 4) {
/* 339:416 */         return -1;
/* 340:    */       }
/* 341:418 */       length = in.readIntLE();
/* 342:419 */       break;
/* 343:    */     default: 
/* 344:421 */       length = tag >> 2 & 0x3F;
/* 345:    */     }
/* 346:423 */     length++;
/* 347:425 */     if (in.readableBytes() < length)
/* 348:    */     {
/* 349:426 */       in.resetReaderIndex();
/* 350:427 */       return -1;
/* 351:    */     }
/* 352:430 */     out.writeBytes(in, length);
/* 353:431 */     return length;
/* 354:    */   }
/* 355:    */   
/* 356:    */   private static int decodeCopyWith1ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar)
/* 357:    */   {
/* 358:448 */     if (!in.isReadable()) {
/* 359:449 */       return -1;
/* 360:    */     }
/* 361:452 */     int initialIndex = out.writerIndex();
/* 362:453 */     int length = 4 + ((tag & 0x1C) >> 2);
/* 363:454 */     int offset = (tag & 0xE0) << 8 >> 5 | in.readUnsignedByte();
/* 364:    */     
/* 365:456 */     validateOffset(offset, writtenSoFar);
/* 366:    */     
/* 367:458 */     out.markReaderIndex();
/* 368:459 */     if (offset < length)
/* 369:    */     {
/* 370:460 */       for (int copies = length / offset; copies > 0; copies--)
/* 371:    */       {
/* 372:462 */         out.readerIndex(initialIndex - offset);
/* 373:463 */         out.readBytes(out, offset);
/* 374:    */       }
/* 375:465 */       if (length % offset != 0)
/* 376:    */       {
/* 377:466 */         out.readerIndex(initialIndex - offset);
/* 378:467 */         out.readBytes(out, length % offset);
/* 379:    */       }
/* 380:    */     }
/* 381:    */     else
/* 382:    */     {
/* 383:470 */       out.readerIndex(initialIndex - offset);
/* 384:471 */       out.readBytes(out, length);
/* 385:    */     }
/* 386:473 */     out.resetReaderIndex();
/* 387:    */     
/* 388:475 */     return length;
/* 389:    */   }
/* 390:    */   
/* 391:    */   private static int decodeCopyWith2ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar)
/* 392:    */   {
/* 393:492 */     if (in.readableBytes() < 2) {
/* 394:493 */       return -1;
/* 395:    */     }
/* 396:496 */     int initialIndex = out.writerIndex();
/* 397:497 */     int length = 1 + (tag >> 2 & 0x3F);
/* 398:498 */     int offset = in.readShortLE();
/* 399:    */     
/* 400:500 */     validateOffset(offset, writtenSoFar);
/* 401:    */     
/* 402:502 */     out.markReaderIndex();
/* 403:503 */     if (offset < length)
/* 404:    */     {
/* 405:504 */       for (int copies = length / offset; copies > 0; copies--)
/* 406:    */       {
/* 407:506 */         out.readerIndex(initialIndex - offset);
/* 408:507 */         out.readBytes(out, offset);
/* 409:    */       }
/* 410:509 */       if (length % offset != 0)
/* 411:    */       {
/* 412:510 */         out.readerIndex(initialIndex - offset);
/* 413:511 */         out.readBytes(out, length % offset);
/* 414:    */       }
/* 415:    */     }
/* 416:    */     else
/* 417:    */     {
/* 418:514 */       out.readerIndex(initialIndex - offset);
/* 419:515 */       out.readBytes(out, length);
/* 420:    */     }
/* 421:517 */     out.resetReaderIndex();
/* 422:    */     
/* 423:519 */     return length;
/* 424:    */   }
/* 425:    */   
/* 426:    */   private static int decodeCopyWith4ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar)
/* 427:    */   {
/* 428:536 */     if (in.readableBytes() < 4) {
/* 429:537 */       return -1;
/* 430:    */     }
/* 431:540 */     int initialIndex = out.writerIndex();
/* 432:541 */     int length = 1 + (tag >> 2 & 0x3F);
/* 433:542 */     int offset = in.readIntLE();
/* 434:    */     
/* 435:544 */     validateOffset(offset, writtenSoFar);
/* 436:    */     
/* 437:546 */     out.markReaderIndex();
/* 438:547 */     if (offset < length)
/* 439:    */     {
/* 440:548 */       for (int copies = length / offset; copies > 0; copies--)
/* 441:    */       {
/* 442:550 */         out.readerIndex(initialIndex - offset);
/* 443:551 */         out.readBytes(out, offset);
/* 444:    */       }
/* 445:553 */       if (length % offset != 0)
/* 446:    */       {
/* 447:554 */         out.readerIndex(initialIndex - offset);
/* 448:555 */         out.readBytes(out, length % offset);
/* 449:    */       }
/* 450:    */     }
/* 451:    */     else
/* 452:    */     {
/* 453:558 */       out.readerIndex(initialIndex - offset);
/* 454:559 */       out.readBytes(out, length);
/* 455:    */     }
/* 456:561 */     out.resetReaderIndex();
/* 457:    */     
/* 458:563 */     return length;
/* 459:    */   }
/* 460:    */   
/* 461:    */   private static void validateOffset(int offset, int chunkSizeSoFar)
/* 462:    */   {
/* 463:576 */     if (offset > 32767) {
/* 464:577 */       throw new DecompressionException("Offset exceeds maximum permissible value");
/* 465:    */     }
/* 466:580 */     if (offset <= 0) {
/* 467:581 */       throw new DecompressionException("Offset is less than minimum permissible value");
/* 468:    */     }
/* 469:584 */     if (offset > chunkSizeSoFar) {
/* 470:585 */       throw new DecompressionException("Offset exceeds size of chunk");
/* 471:    */     }
/* 472:    */   }
/* 473:    */   
/* 474:    */   static int calculateChecksum(ByteBuf data)
/* 475:    */   {
/* 476:596 */     return calculateChecksum(data, data.readerIndex(), data.readableBytes());
/* 477:    */   }
/* 478:    */   
/* 479:    */   static int calculateChecksum(ByteBuf data, int offset, int length)
/* 480:    */   {
/* 481:606 */     Crc32c crc32 = new Crc32c();
/* 482:    */     try
/* 483:    */     {
/* 484:608 */       crc32.update(data, offset, length);
/* 485:609 */       return maskChecksum((int)crc32.getValue());
/* 486:    */     }
/* 487:    */     finally
/* 488:    */     {
/* 489:611 */       crc32.reset();
/* 490:    */     }
/* 491:    */   }
/* 492:    */   
/* 493:    */   static void validateChecksum(int expectedChecksum, ByteBuf data)
/* 494:    */   {
/* 495:625 */     validateChecksum(expectedChecksum, data, data.readerIndex(), data.readableBytes());
/* 496:    */   }
/* 497:    */   
/* 498:    */   static void validateChecksum(int expectedChecksum, ByteBuf data, int offset, int length)
/* 499:    */   {
/* 500:638 */     int actualChecksum = calculateChecksum(data, offset, length);
/* 501:639 */     if (actualChecksum != expectedChecksum) {
/* 502:642 */       throw new DecompressionException("mismatching checksum: " + Integer.toHexString(actualChecksum) + " (expected: " + Integer.toHexString(expectedChecksum) + ')');
/* 503:    */     }
/* 504:    */   }
/* 505:    */   
/* 506:    */   static int maskChecksum(int checksum)
/* 507:    */   {
/* 508:658 */     return (checksum >> 15 | checksum << 17) + -1568478504;
/* 509:    */   }
/* 510:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.Snappy
 * JD-Core Version:    0.7.0.1
 */