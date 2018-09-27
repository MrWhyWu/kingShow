/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ 
/*   6:    */ public final class ByteArrayInput
/*   7:    */   implements Input
/*   8:    */ {
/*   9:    */   private final byte[] buffer;
/*  10:    */   private int offset;
/*  11:    */   private int limit;
/*  12: 47 */   private int lastTag = 0;
/*  13: 48 */   private int packedLimit = 0;
/*  14:    */   public final boolean decodeNestedMessageAsGroup;
/*  15:    */   
/*  16:    */   public ByteArrayInput(byte[] buffer, boolean decodeNestedMessageAsGroup)
/*  17:    */   {
/*  18: 57 */     this(buffer, 0, buffer.length, decodeNestedMessageAsGroup);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public ByteArrayInput(byte[] buffer, int offset, int len, boolean decodeNestedMessageAsGroup)
/*  22:    */   {
/*  23: 62 */     this.buffer = buffer;
/*  24: 63 */     this.offset = offset;
/*  25: 64 */     this.limit = (offset + len);
/*  26: 65 */     this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public ByteArrayInput reset(int offset, int len)
/*  30:    */   {
/*  31: 73 */     if (len < 0) {
/*  32: 74 */       throw new IllegalArgumentException("length cannot be negative.");
/*  33:    */     }
/*  34: 76 */     this.offset = offset;
/*  35: 77 */     this.limit = (offset + len);
/*  36: 78 */     this.packedLimit = 0;
/*  37: 79 */     return this;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ByteArrayInput setBounds(int offset, int limit)
/*  41:    */   {
/*  42: 87 */     this.offset = offset;
/*  43: 88 */     this.limit = limit;
/*  44: 89 */     this.packedLimit = 0;
/*  45: 90 */     return this;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public int currentOffset()
/*  49:    */   {
/*  50: 98 */     return this.offset;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public int currentLimit()
/*  54:    */   {
/*  55:106 */     return this.limit;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public boolean isCurrentFieldPacked()
/*  59:    */   {
/*  60:114 */     return (this.packedLimit != 0) && (this.packedLimit != this.offset);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public int getLastTag()
/*  64:    */   {
/*  65:122 */     return this.lastTag;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int readTag()
/*  69:    */     throws IOException
/*  70:    */   {
/*  71:131 */     if (this.offset == this.limit)
/*  72:    */     {
/*  73:133 */       this.lastTag = 0;
/*  74:134 */       return 0;
/*  75:    */     }
/*  76:137 */     int tag = readRawVarint32();
/*  77:138 */     if (tag >>> 3 == 0) {
/*  78:141 */       throw ProtobufException.invalidTag();
/*  79:    */     }
/*  80:143 */     this.lastTag = tag;
/*  81:144 */     return tag;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void checkLastTagWas(int value)
/*  85:    */     throws ProtobufException
/*  86:    */   {
/*  87:156 */     if (this.lastTag != value) {
/*  88:158 */       throw ProtobufException.invalidEndTag();
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean skipField(int tag)
/*  93:    */     throws IOException
/*  94:    */   {
/*  95:170 */     switch (WireFormat.getTagWireType(tag))
/*  96:    */     {
/*  97:    */     case 0: 
/*  98:173 */       readInt32();
/*  99:174 */       return true;
/* 100:    */     case 1: 
/* 101:176 */       readRawLittleEndian64();
/* 102:177 */       return true;
/* 103:    */     case 2: 
/* 104:179 */       int size = readRawVarint32();
/* 105:180 */       if (size < 0) {
/* 106:181 */         throw ProtobufException.negativeSize();
/* 107:    */       }
/* 108:182 */       this.offset += size;
/* 109:183 */       return true;
/* 110:    */     case 3: 
/* 111:185 */       skipMessage();
/* 112:186 */       checkLastTagWas(WireFormat.makeTag(WireFormat.getTagFieldNumber(tag), 4));
/* 113:187 */       return true;
/* 114:    */     case 4: 
/* 115:189 */       return false;
/* 116:    */     case 5: 
/* 117:191 */       readRawLittleEndian32();
/* 118:192 */       return true;
/* 119:    */     }
/* 120:194 */     throw ProtobufException.invalidWireType();
/* 121:    */   }
/* 122:    */   
/* 123:    */   public void skipMessage()
/* 124:    */     throws IOException
/* 125:    */   {
/* 126:    */     for (;;)
/* 127:    */     {
/* 128:206 */       int tag = readTag();
/* 129:207 */       if ((tag == 0) || (!skipField(tag))) {
/* 130:209 */         return;
/* 131:    */       }
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   public <T> void handleUnknownField(int fieldNumber, Schema<T> schema)
/* 136:    */     throws IOException
/* 137:    */   {
/* 138:217 */     skipField(this.lastTag);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public <T> int readFieldNumber(Schema<T> schema)
/* 142:    */     throws IOException
/* 143:    */   {
/* 144:223 */     if (this.offset == this.limit)
/* 145:    */     {
/* 146:225 */       this.lastTag = 0;
/* 147:226 */       return 0;
/* 148:    */     }
/* 149:230 */     if (isCurrentFieldPacked())
/* 150:    */     {
/* 151:232 */       if (this.packedLimit < this.offset) {
/* 152:233 */         throw ProtobufException.misreportedSize();
/* 153:    */       }
/* 154:236 */       return this.lastTag >>> 3;
/* 155:    */     }
/* 156:239 */     this.packedLimit = 0;
/* 157:240 */     int tag = readRawVarint32();
/* 158:241 */     int fieldNumber = tag >>> 3;
/* 159:242 */     if (fieldNumber == 0)
/* 160:    */     {
/* 161:244 */       if ((this.decodeNestedMessageAsGroup) && (7 == (tag & 0x7)))
/* 162:    */       {
/* 163:249 */         this.lastTag = 0;
/* 164:250 */         return 0;
/* 165:    */       }
/* 166:253 */       throw ProtobufException.invalidTag();
/* 167:    */     }
/* 168:255 */     if ((this.decodeNestedMessageAsGroup) && (4 == (tag & 0x7)))
/* 169:    */     {
/* 170:257 */       this.lastTag = 0;
/* 171:258 */       return 0;
/* 172:    */     }
/* 173:261 */     this.lastTag = tag;
/* 174:262 */     return fieldNumber;
/* 175:    */   }
/* 176:    */   
/* 177:    */   private void checkIfPackedField()
/* 178:    */     throws IOException
/* 179:    */   {
/* 180:274 */     if ((this.packedLimit == 0) && (WireFormat.getTagWireType(this.lastTag) == 2))
/* 181:    */     {
/* 182:276 */       int length = readRawVarint32();
/* 183:277 */       if (length < 0) {
/* 184:278 */         throw ProtobufException.negativeSize();
/* 185:    */       }
/* 186:280 */       if (this.offset + length > this.limit) {
/* 187:281 */         throw ProtobufException.misreportedSize();
/* 188:    */       }
/* 189:283 */       this.packedLimit = (this.offset + length);
/* 190:    */     }
/* 191:    */   }
/* 192:    */   
/* 193:    */   public double readDouble()
/* 194:    */     throws IOException
/* 195:    */   {
/* 196:293 */     checkIfPackedField();
/* 197:294 */     return Double.longBitsToDouble(readRawLittleEndian64());
/* 198:    */   }
/* 199:    */   
/* 200:    */   public float readFloat()
/* 201:    */     throws IOException
/* 202:    */   {
/* 203:303 */     checkIfPackedField();
/* 204:304 */     return Float.intBitsToFloat(readRawLittleEndian32());
/* 205:    */   }
/* 206:    */   
/* 207:    */   public long readUInt64()
/* 208:    */     throws IOException
/* 209:    */   {
/* 210:313 */     checkIfPackedField();
/* 211:314 */     return readRawVarint64();
/* 212:    */   }
/* 213:    */   
/* 214:    */   public long readInt64()
/* 215:    */     throws IOException
/* 216:    */   {
/* 217:323 */     checkIfPackedField();
/* 218:324 */     return readRawVarint64();
/* 219:    */   }
/* 220:    */   
/* 221:    */   public int readInt32()
/* 222:    */     throws IOException
/* 223:    */   {
/* 224:333 */     checkIfPackedField();
/* 225:334 */     return readRawVarint32();
/* 226:    */   }
/* 227:    */   
/* 228:    */   public long readFixed64()
/* 229:    */     throws IOException
/* 230:    */   {
/* 231:343 */     checkIfPackedField();
/* 232:344 */     return readRawLittleEndian64();
/* 233:    */   }
/* 234:    */   
/* 235:    */   public int readFixed32()
/* 236:    */     throws IOException
/* 237:    */   {
/* 238:353 */     checkIfPackedField();
/* 239:354 */     return readRawLittleEndian32();
/* 240:    */   }
/* 241:    */   
/* 242:    */   public boolean readBool()
/* 243:    */     throws IOException
/* 244:    */   {
/* 245:363 */     checkIfPackedField();
/* 246:364 */     return this.buffer[(this.offset++)] != 0;
/* 247:    */   }
/* 248:    */   
/* 249:    */   public int readUInt32()
/* 250:    */     throws IOException
/* 251:    */   {
/* 252:373 */     checkIfPackedField();
/* 253:374 */     return readRawVarint32();
/* 254:    */   }
/* 255:    */   
/* 256:    */   public int readEnum()
/* 257:    */     throws IOException
/* 258:    */   {
/* 259:384 */     checkIfPackedField();
/* 260:385 */     return readRawVarint32();
/* 261:    */   }
/* 262:    */   
/* 263:    */   public int readSFixed32()
/* 264:    */     throws IOException
/* 265:    */   {
/* 266:394 */     checkIfPackedField();
/* 267:395 */     return readRawLittleEndian32();
/* 268:    */   }
/* 269:    */   
/* 270:    */   public long readSFixed64()
/* 271:    */     throws IOException
/* 272:    */   {
/* 273:404 */     checkIfPackedField();
/* 274:405 */     return readRawLittleEndian64();
/* 275:    */   }
/* 276:    */   
/* 277:    */   public int readSInt32()
/* 278:    */     throws IOException
/* 279:    */   {
/* 280:414 */     checkIfPackedField();
/* 281:415 */     int n = readRawVarint32();
/* 282:416 */     return n >>> 1 ^ -(n & 0x1);
/* 283:    */   }
/* 284:    */   
/* 285:    */   public long readSInt64()
/* 286:    */     throws IOException
/* 287:    */   {
/* 288:425 */     checkIfPackedField();
/* 289:426 */     long n = readRawVarint64();
/* 290:427 */     return n >>> 1 ^ -(n & 1L);
/* 291:    */   }
/* 292:    */   
/* 293:    */   public String readString()
/* 294:    */     throws IOException
/* 295:    */   {
/* 296:433 */     int length = readRawVarint32();
/* 297:434 */     if (length < 0) {
/* 298:435 */       throw ProtobufException.negativeSize();
/* 299:    */     }
/* 300:437 */     if (this.offset + length > this.limit) {
/* 301:438 */       throw ProtobufException.misreportedSize();
/* 302:    */     }
/* 303:440 */     int offset = this.offset;
/* 304:    */     
/* 305:442 */     this.offset += length;
/* 306:    */     
/* 307:444 */     return StringSerializer.STRING.deser(this.buffer, offset, length);
/* 308:    */   }
/* 309:    */   
/* 310:    */   public ByteString readBytes()
/* 311:    */     throws IOException
/* 312:    */   {
/* 313:450 */     return ByteString.wrap(readByteArray());
/* 314:    */   }
/* 315:    */   
/* 316:    */   public void readBytes(ByteBuffer bb)
/* 317:    */     throws IOException
/* 318:    */   {
/* 319:456 */     int length = readRawVarint32();
/* 320:457 */     if (length < 0) {
/* 321:458 */       throw ProtobufException.negativeSize();
/* 322:    */     }
/* 323:460 */     if (this.offset + length > this.limit) {
/* 324:461 */       throw ProtobufException.misreportedSize();
/* 325:    */     }
/* 326:463 */     bb.put(this.buffer, this.offset, length);
/* 327:    */     
/* 328:465 */     this.offset += length;
/* 329:    */   }
/* 330:    */   
/* 331:    */   public byte[] readByteArray()
/* 332:    */     throws IOException
/* 333:    */   {
/* 334:471 */     int length = readRawVarint32();
/* 335:472 */     if (length < 0) {
/* 336:473 */       throw ProtobufException.negativeSize();
/* 337:    */     }
/* 338:475 */     if (this.offset + length > this.limit) {
/* 339:476 */       throw ProtobufException.misreportedSize();
/* 340:    */     }
/* 341:478 */     byte[] copy = new byte[length];
/* 342:479 */     System.arraycopy(this.buffer, this.offset, copy, 0, length);
/* 343:    */     
/* 344:481 */     this.offset += length;
/* 345:    */     
/* 346:483 */     return copy;
/* 347:    */   }
/* 348:    */   
/* 349:    */   public <T> T mergeObject(T value, Schema<T> schema)
/* 350:    */     throws IOException
/* 351:    */   {
/* 352:489 */     if (this.decodeNestedMessageAsGroup) {
/* 353:490 */       return mergeObjectEncodedAsGroup(value, schema);
/* 354:    */     }
/* 355:492 */     int length = readRawVarint32();
/* 356:493 */     if (length < 0) {
/* 357:494 */       throw ProtobufException.negativeSize();
/* 358:    */     }
/* 359:497 */     int oldLimit = this.limit;
/* 360:    */     
/* 361:499 */     this.limit = (this.offset + length);
/* 362:501 */     if (value == null) {
/* 363:502 */       value = schema.newMessage();
/* 364:    */     }
/* 365:503 */     schema.mergeFrom(this, value);
/* 366:504 */     if (!schema.isInitialized(value)) {
/* 367:505 */       throw new UninitializedMessageException(value, schema);
/* 368:    */     }
/* 369:506 */     checkLastTagWas(0);
/* 370:    */     
/* 371:    */ 
/* 372:509 */     this.limit = oldLimit;
/* 373:    */     
/* 374:511 */     return value;
/* 375:    */   }
/* 376:    */   
/* 377:    */   private <T> T mergeObjectEncodedAsGroup(T value, Schema<T> schema)
/* 378:    */     throws IOException
/* 379:    */   {
/* 380:516 */     if (value == null) {
/* 381:517 */       value = schema.newMessage();
/* 382:    */     }
/* 383:518 */     schema.mergeFrom(this, value);
/* 384:519 */     if (!schema.isInitialized(value)) {
/* 385:520 */       throw new UninitializedMessageException(value, schema);
/* 386:    */     }
/* 387:522 */     checkLastTagWas(0);
/* 388:523 */     return value;
/* 389:    */   }
/* 390:    */   
/* 391:    */   public int readRawVarint32()
/* 392:    */     throws IOException
/* 393:    */   {
/* 394:531 */     byte tmp = this.buffer[(this.offset++)];
/* 395:532 */     if (tmp >= 0) {
/* 396:534 */       return tmp;
/* 397:    */     }
/* 398:536 */     int result = tmp & 0x7F;
/* 399:537 */     if ((tmp = this.buffer[(this.offset++)]) >= 0)
/* 400:    */     {
/* 401:539 */       result |= tmp << 7;
/* 402:    */     }
/* 403:    */     else
/* 404:    */     {
/* 405:543 */       result |= (tmp & 0x7F) << 7;
/* 406:544 */       if ((tmp = this.buffer[(this.offset++)]) >= 0)
/* 407:    */       {
/* 408:546 */         result |= tmp << 14;
/* 409:    */       }
/* 410:    */       else
/* 411:    */       {
/* 412:550 */         result |= (tmp & 0x7F) << 14;
/* 413:551 */         if ((tmp = this.buffer[(this.offset++)]) >= 0)
/* 414:    */         {
/* 415:553 */           result |= tmp << 21;
/* 416:    */         }
/* 417:    */         else
/* 418:    */         {
/* 419:557 */           result |= (tmp & 0x7F) << 21;
/* 420:558 */           result |= (tmp = this.buffer[(this.offset++)]) << 28;
/* 421:559 */           if (tmp < 0)
/* 422:    */           {
/* 423:562 */             for (int i = 0; i < 5; i++) {
/* 424:564 */               if (this.buffer[(this.offset++)] >= 0) {
/* 425:566 */                 return result;
/* 426:    */               }
/* 427:    */             }
/* 428:569 */             throw ProtobufException.malformedVarint();
/* 429:    */           }
/* 430:    */         }
/* 431:    */       }
/* 432:    */     }
/* 433:574 */     return result;
/* 434:    */   }
/* 435:    */   
/* 436:    */   public long readRawVarint64()
/* 437:    */     throws IOException
/* 438:    */   {
/* 439:582 */     byte[] buffer = this.buffer;
/* 440:583 */     int offset = this.offset;
/* 441:    */     
/* 442:585 */     int shift = 0;
/* 443:586 */     long result = 0L;
/* 444:587 */     while (shift < 64)
/* 445:    */     {
/* 446:589 */       byte b = buffer[(offset++)];
/* 447:590 */       result |= (b & 0x7F) << shift;
/* 448:591 */       if ((b & 0x80) == 0)
/* 449:    */       {
/* 450:593 */         this.offset = offset;
/* 451:594 */         return result;
/* 452:    */       }
/* 453:596 */       shift += 7;
/* 454:    */     }
/* 455:598 */     throw ProtobufException.malformedVarint();
/* 456:    */   }
/* 457:    */   
/* 458:    */   public int readRawLittleEndian32()
/* 459:    */     throws IOException
/* 460:    */   {
/* 461:606 */     byte[] buffer = this.buffer;
/* 462:607 */     int offset = this.offset;
/* 463:    */     
/* 464:609 */     byte b1 = buffer[(offset++)];
/* 465:610 */     byte b2 = buffer[(offset++)];
/* 466:611 */     byte b3 = buffer[(offset++)];
/* 467:612 */     byte b4 = buffer[(offset++)];
/* 468:    */     
/* 469:614 */     this.offset = offset;
/* 470:    */     
/* 471:616 */     return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24;
/* 472:    */   }
/* 473:    */   
/* 474:    */   public long readRawLittleEndian64()
/* 475:    */     throws IOException
/* 476:    */   {
/* 477:627 */     byte[] buffer = this.buffer;
/* 478:628 */     int offset = this.offset;
/* 479:    */     
/* 480:630 */     byte b1 = buffer[(offset++)];
/* 481:631 */     byte b2 = buffer[(offset++)];
/* 482:632 */     byte b3 = buffer[(offset++)];
/* 483:633 */     byte b4 = buffer[(offset++)];
/* 484:634 */     byte b5 = buffer[(offset++)];
/* 485:635 */     byte b6 = buffer[(offset++)];
/* 486:636 */     byte b7 = buffer[(offset++)];
/* 487:637 */     byte b8 = buffer[(offset++)];
/* 488:    */     
/* 489:639 */     this.offset = offset;
/* 490:    */     
/* 491:641 */     return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24 | (b5 & 0xFF) << 32 | (b6 & 0xFF) << 40 | (b7 & 0xFF) << 48 | (b8 & 0xFF) << 56;
/* 492:    */   }
/* 493:    */   
/* 494:    */   public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated)
/* 495:    */     throws IOException
/* 496:    */   {
/* 497:655 */     int length = readRawVarint32();
/* 498:656 */     if (length < 0) {
/* 499:657 */       throw ProtobufException.negativeSize();
/* 500:    */     }
/* 501:659 */     output.writeByteRange(utf8String, fieldNumber, this.buffer, this.offset, length, repeated);
/* 502:    */     
/* 503:661 */     this.offset += length;
/* 504:    */   }
/* 505:    */   
/* 506:    */   public ByteBuffer readByteBuffer()
/* 507:    */     throws IOException
/* 508:    */   {
/* 509:670 */     return ByteBuffer.wrap(readByteArray());
/* 510:    */   }
/* 511:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ByteArrayInput
 * JD-Core Version:    0.7.0.1
 */