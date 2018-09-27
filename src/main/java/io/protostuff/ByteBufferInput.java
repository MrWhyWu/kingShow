/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ 
/*   6:    */ public final class ByteBufferInput
/*   7:    */   implements Input
/*   8:    */ {
/*   9:    */   private final ByteBuffer buffer;
/*  10: 48 */   private int lastTag = 0;
/*  11: 50 */   private int packedLimit = 0;
/*  12:    */   public final boolean decodeNestedMessageAsGroup;
/*  13:    */   
/*  14:    */   public ByteBufferInput(ByteBuffer buffer, boolean protostuffMessage)
/*  15:    */   {
/*  16: 67 */     this.buffer = buffer.slice();
/*  17: 68 */     this.decodeNestedMessageAsGroup = protostuffMessage;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public ByteBufferInput reset(int offset, int len)
/*  21:    */   {
/*  22: 76 */     this.buffer.rewind();
/*  23:    */     
/*  24: 78 */     return this;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public int currentOffset()
/*  28:    */   {
/*  29: 86 */     return this.buffer.position();
/*  30:    */   }
/*  31:    */   
/*  32:    */   public int currentLimit()
/*  33:    */   {
/*  34: 94 */     return this.buffer.limit();
/*  35:    */   }
/*  36:    */   
/*  37:    */   public boolean isCurrentFieldPacked()
/*  38:    */   {
/*  39:102 */     return (this.packedLimit != 0) && (this.packedLimit != this.buffer.position());
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int getLastTag()
/*  43:    */   {
/*  44:110 */     return this.lastTag;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int readTag()
/*  48:    */     throws IOException
/*  49:    */   {
/*  50:119 */     if (!this.buffer.hasRemaining())
/*  51:    */     {
/*  52:121 */       this.lastTag = 0;
/*  53:122 */       return 0;
/*  54:    */     }
/*  55:125 */     int tag = readRawVarint32();
/*  56:126 */     if (tag >>> 3 == 0) {
/*  57:129 */       throw ProtobufException.invalidTag();
/*  58:    */     }
/*  59:131 */     this.lastTag = tag;
/*  60:132 */     return tag;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public void checkLastTagWas(int value)
/*  64:    */     throws ProtobufException
/*  65:    */   {
/*  66:144 */     if (this.lastTag != value) {
/*  67:146 */       throw ProtobufException.invalidEndTag();
/*  68:    */     }
/*  69:    */   }
/*  70:    */   
/*  71:    */   public boolean skipField(int tag)
/*  72:    */     throws IOException
/*  73:    */   {
/*  74:158 */     switch (WireFormat.getTagWireType(tag))
/*  75:    */     {
/*  76:    */     case 0: 
/*  77:161 */       readInt32();
/*  78:162 */       return true;
/*  79:    */     case 1: 
/*  80:164 */       readRawLittleEndian64();
/*  81:165 */       return true;
/*  82:    */     case 2: 
/*  83:167 */       int size = readRawVarint32();
/*  84:168 */       if (size < 0) {
/*  85:169 */         throw ProtobufException.negativeSize();
/*  86:    */       }
/*  87:170 */       this.buffer.position(this.buffer.position() + size);
/*  88:    */       
/*  89:172 */       return true;
/*  90:    */     case 3: 
/*  91:174 */       skipMessage();
/*  92:175 */       checkLastTagWas(WireFormat.makeTag(WireFormat.getTagFieldNumber(tag), 4));
/*  93:176 */       return true;
/*  94:    */     case 4: 
/*  95:178 */       return false;
/*  96:    */     case 5: 
/*  97:180 */       readRawLittleEndian32();
/*  98:181 */       return true;
/*  99:    */     }
/* 100:183 */     throw ProtobufException.invalidWireType();
/* 101:    */   }
/* 102:    */   
/* 103:    */   public void skipMessage()
/* 104:    */     throws IOException
/* 105:    */   {
/* 106:    */     for (;;)
/* 107:    */     {
/* 108:195 */       int tag = readTag();
/* 109:196 */       if ((tag == 0) || (!skipField(tag))) {
/* 110:198 */         return;
/* 111:    */       }
/* 112:    */     }
/* 113:    */   }
/* 114:    */   
/* 115:    */   public <T> void handleUnknownField(int fieldNumber, Schema<T> schema)
/* 116:    */     throws IOException
/* 117:    */   {
/* 118:206 */     skipField(this.lastTag);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public <T> int readFieldNumber(Schema<T> schema)
/* 122:    */     throws IOException
/* 123:    */   {
/* 124:212 */     if (!this.buffer.hasRemaining())
/* 125:    */     {
/* 126:214 */       this.lastTag = 0;
/* 127:215 */       return 0;
/* 128:    */     }
/* 129:219 */     if (isCurrentFieldPacked())
/* 130:    */     {
/* 131:221 */       if (this.packedLimit < this.buffer.position()) {
/* 132:222 */         throw ProtobufException.misreportedSize();
/* 133:    */       }
/* 134:225 */       return this.lastTag >>> 3;
/* 135:    */     }
/* 136:228 */     this.packedLimit = 0;
/* 137:229 */     int tag = readRawVarint32();
/* 138:230 */     int fieldNumber = tag >>> 3;
/* 139:231 */     if (fieldNumber == 0)
/* 140:    */     {
/* 141:233 */       if ((this.decodeNestedMessageAsGroup) && (7 == (tag & 0x7)))
/* 142:    */       {
/* 143:238 */         this.lastTag = 0;
/* 144:239 */         return 0;
/* 145:    */       }
/* 146:242 */       throw ProtobufException.invalidTag();
/* 147:    */     }
/* 148:244 */     if ((this.decodeNestedMessageAsGroup) && (4 == (tag & 0x7)))
/* 149:    */     {
/* 150:246 */       this.lastTag = 0;
/* 151:247 */       return 0;
/* 152:    */     }
/* 153:250 */     this.lastTag = tag;
/* 154:251 */     return fieldNumber;
/* 155:    */   }
/* 156:    */   
/* 157:    */   private void checkIfPackedField()
/* 158:    */     throws IOException
/* 159:    */   {
/* 160:263 */     if ((this.packedLimit == 0) && (WireFormat.getTagWireType(this.lastTag) == 2))
/* 161:    */     {
/* 162:265 */       int length = readRawVarint32();
/* 163:266 */       if (length < 0) {
/* 164:267 */         throw ProtobufException.negativeSize();
/* 165:    */       }
/* 166:269 */       if (this.buffer.position() + length > this.buffer.limit()) {
/* 167:270 */         throw ProtobufException.misreportedSize();
/* 168:    */       }
/* 169:272 */       this.packedLimit = (this.buffer.position() + length);
/* 170:    */     }
/* 171:    */   }
/* 172:    */   
/* 173:    */   public double readDouble()
/* 174:    */     throws IOException
/* 175:    */   {
/* 176:282 */     checkIfPackedField();
/* 177:283 */     return Double.longBitsToDouble(readRawLittleEndian64());
/* 178:    */   }
/* 179:    */   
/* 180:    */   public float readFloat()
/* 181:    */     throws IOException
/* 182:    */   {
/* 183:292 */     checkIfPackedField();
/* 184:293 */     return Float.intBitsToFloat(readRawLittleEndian32());
/* 185:    */   }
/* 186:    */   
/* 187:    */   public long readUInt64()
/* 188:    */     throws IOException
/* 189:    */   {
/* 190:302 */     checkIfPackedField();
/* 191:303 */     return readRawVarint64();
/* 192:    */   }
/* 193:    */   
/* 194:    */   public long readInt64()
/* 195:    */     throws IOException
/* 196:    */   {
/* 197:312 */     checkIfPackedField();
/* 198:313 */     return readRawVarint64();
/* 199:    */   }
/* 200:    */   
/* 201:    */   public int readInt32()
/* 202:    */     throws IOException
/* 203:    */   {
/* 204:322 */     checkIfPackedField();
/* 205:323 */     return readRawVarint32();
/* 206:    */   }
/* 207:    */   
/* 208:    */   public long readFixed64()
/* 209:    */     throws IOException
/* 210:    */   {
/* 211:332 */     checkIfPackedField();
/* 212:333 */     return readRawLittleEndian64();
/* 213:    */   }
/* 214:    */   
/* 215:    */   public int readFixed32()
/* 216:    */     throws IOException
/* 217:    */   {
/* 218:342 */     checkIfPackedField();
/* 219:343 */     return readRawLittleEndian32();
/* 220:    */   }
/* 221:    */   
/* 222:    */   public boolean readBool()
/* 223:    */     throws IOException
/* 224:    */   {
/* 225:352 */     checkIfPackedField();
/* 226:353 */     return this.buffer.get() != 0;
/* 227:    */   }
/* 228:    */   
/* 229:    */   public int readUInt32()
/* 230:    */     throws IOException
/* 231:    */   {
/* 232:362 */     checkIfPackedField();
/* 233:363 */     return readRawVarint32();
/* 234:    */   }
/* 235:    */   
/* 236:    */   public int readEnum()
/* 237:    */     throws IOException
/* 238:    */   {
/* 239:373 */     checkIfPackedField();
/* 240:374 */     return readRawVarint32();
/* 241:    */   }
/* 242:    */   
/* 243:    */   public int readSFixed32()
/* 244:    */     throws IOException
/* 245:    */   {
/* 246:383 */     checkIfPackedField();
/* 247:384 */     return readRawLittleEndian32();
/* 248:    */   }
/* 249:    */   
/* 250:    */   public long readSFixed64()
/* 251:    */     throws IOException
/* 252:    */   {
/* 253:393 */     checkIfPackedField();
/* 254:394 */     return readRawLittleEndian64();
/* 255:    */   }
/* 256:    */   
/* 257:    */   public int readSInt32()
/* 258:    */     throws IOException
/* 259:    */   {
/* 260:403 */     checkIfPackedField();
/* 261:404 */     int n = readRawVarint32();
/* 262:405 */     return n >>> 1 ^ -(n & 0x1);
/* 263:    */   }
/* 264:    */   
/* 265:    */   public long readSInt64()
/* 266:    */     throws IOException
/* 267:    */   {
/* 268:414 */     checkIfPackedField();
/* 269:415 */     long n = readRawVarint64();
/* 270:416 */     return n >>> 1 ^ -(n & 1L);
/* 271:    */   }
/* 272:    */   
/* 273:    */   public String readString()
/* 274:    */     throws IOException
/* 275:    */   {
/* 276:422 */     int length = readRawVarint32();
/* 277:423 */     if (length < 0) {
/* 278:424 */       throw ProtobufException.negativeSize();
/* 279:    */     }
/* 280:426 */     if (this.buffer.remaining() < length) {
/* 281:427 */       throw ProtobufException.misreportedSize();
/* 282:    */     }
/* 283:431 */     if (this.buffer.hasArray())
/* 284:    */     {
/* 285:433 */       int currPosition = this.buffer.position();
/* 286:434 */       this.buffer.position(this.buffer.position() + length);
/* 287:435 */       return StringSerializer.STRING.deser(this.buffer.array(), this.buffer
/* 288:436 */         .arrayOffset() + currPosition, length);
/* 289:    */     }
/* 290:441 */     byte[] tmp = new byte[length];
/* 291:442 */     this.buffer.get(tmp);
/* 292:443 */     return StringSerializer.STRING.deser(tmp);
/* 293:    */   }
/* 294:    */   
/* 295:    */   public ByteString readBytes()
/* 296:    */     throws IOException
/* 297:    */   {
/* 298:456 */     return ByteString.wrap(readByteArray());
/* 299:    */   }
/* 300:    */   
/* 301:    */   public void readBytes(ByteBuffer bb)
/* 302:    */     throws IOException
/* 303:    */   {
/* 304:462 */     int length = readRawVarint32();
/* 305:463 */     if (length < 0) {
/* 306:464 */       throw ProtobufException.negativeSize();
/* 307:    */     }
/* 308:466 */     if (this.buffer.remaining() < length) {
/* 309:468 */       throw ProtobufException.misreportedSize();
/* 310:    */     }
/* 311:470 */     bb.put(this.buffer);
/* 312:    */   }
/* 313:    */   
/* 314:    */   public byte[] readByteArray()
/* 315:    */     throws IOException
/* 316:    */   {
/* 317:476 */     int length = readRawVarint32();
/* 318:477 */     if (length < 0) {
/* 319:478 */       throw ProtobufException.negativeSize();
/* 320:    */     }
/* 321:480 */     if (this.buffer.remaining() < length) {
/* 322:482 */       throw ProtobufException.misreportedSize();
/* 323:    */     }
/* 324:484 */     byte[] copy = new byte[length];
/* 325:485 */     this.buffer.get(copy);
/* 326:486 */     return copy;
/* 327:    */   }
/* 328:    */   
/* 329:    */   public <T> T mergeObject(T value, Schema<T> schema)
/* 330:    */     throws IOException
/* 331:    */   {
/* 332:492 */     if (this.decodeNestedMessageAsGroup) {
/* 333:493 */       return mergeObjectEncodedAsGroup(value, schema);
/* 334:    */     }
/* 335:495 */     int length = readRawVarint32();
/* 336:496 */     if (length < 0) {
/* 337:497 */       throw ProtobufException.negativeSize();
/* 338:    */     }
/* 339:499 */     if (this.buffer.remaining() < length) {
/* 340:500 */       throw ProtobufException.misreportedSize();
/* 341:    */     }
/* 342:502 */     ByteBuffer dup = this.buffer.slice();
/* 343:503 */     dup.limit(length);
/* 344:510 */     if (value == null) {
/* 345:511 */       value = schema.newMessage();
/* 346:    */     }
/* 347:512 */     ByteBufferInput nestedInput = new ByteBufferInput(dup, this.decodeNestedMessageAsGroup);
/* 348:513 */     schema.mergeFrom(nestedInput, value);
/* 349:514 */     if (!schema.isInitialized(value)) {
/* 350:515 */       throw new UninitializedMessageException(value, schema);
/* 351:    */     }
/* 352:516 */     nestedInput.checkLastTagWas(0);
/* 353:    */     
/* 354:    */ 
/* 355:    */ 
/* 356:    */ 
/* 357:    */ 
/* 358:522 */     this.buffer.position(this.buffer.position() + length);
/* 359:523 */     return value;
/* 360:    */   }
/* 361:    */   
/* 362:    */   private <T> T mergeObjectEncodedAsGroup(T value, Schema<T> schema)
/* 363:    */     throws IOException
/* 364:    */   {
/* 365:528 */     if (value == null) {
/* 366:529 */       value = schema.newMessage();
/* 367:    */     }
/* 368:530 */     schema.mergeFrom(this, value);
/* 369:531 */     if (!schema.isInitialized(value)) {
/* 370:532 */       throw new UninitializedMessageException(value, schema);
/* 371:    */     }
/* 372:534 */     checkLastTagWas(0);
/* 373:535 */     return value;
/* 374:    */   }
/* 375:    */   
/* 376:    */   public int readRawVarint32()
/* 377:    */     throws IOException
/* 378:    */   {
/* 379:543 */     byte tmp = this.buffer.get();
/* 380:544 */     if (tmp >= 0) {
/* 381:546 */       return tmp;
/* 382:    */     }
/* 383:548 */     int result = tmp & 0x7F;
/* 384:549 */     if ((tmp = this.buffer.get()) >= 0)
/* 385:    */     {
/* 386:551 */       result |= tmp << 7;
/* 387:    */     }
/* 388:    */     else
/* 389:    */     {
/* 390:555 */       result |= (tmp & 0x7F) << 7;
/* 391:556 */       if ((tmp = this.buffer.get()) >= 0)
/* 392:    */       {
/* 393:558 */         result |= tmp << 14;
/* 394:    */       }
/* 395:    */       else
/* 396:    */       {
/* 397:562 */         result |= (tmp & 0x7F) << 14;
/* 398:563 */         if ((tmp = this.buffer.get()) >= 0)
/* 399:    */         {
/* 400:565 */           result |= tmp << 21;
/* 401:    */         }
/* 402:    */         else
/* 403:    */         {
/* 404:569 */           result |= (tmp & 0x7F) << 21;
/* 405:570 */           result |= (tmp = this.buffer.get()) << 28;
/* 406:571 */           if (tmp < 0)
/* 407:    */           {
/* 408:574 */             for (int i = 0; i < 5; i++) {
/* 409:576 */               if (this.buffer.get() >= 0) {
/* 410:578 */                 return result;
/* 411:    */               }
/* 412:    */             }
/* 413:581 */             throw ProtobufException.malformedVarint();
/* 414:    */           }
/* 415:    */         }
/* 416:    */       }
/* 417:    */     }
/* 418:586 */     return result;
/* 419:    */   }
/* 420:    */   
/* 421:    */   public long readRawVarint64()
/* 422:    */     throws IOException
/* 423:    */   {
/* 424:597 */     int shift = 0;
/* 425:598 */     long result = 0L;
/* 426:599 */     while (shift < 64)
/* 427:    */     {
/* 428:601 */       byte b = this.buffer.get();
/* 429:602 */       result |= (b & 0x7F) << shift;
/* 430:603 */       if ((b & 0x80) == 0) {
/* 431:606 */         return result;
/* 432:    */       }
/* 433:608 */       shift += 7;
/* 434:    */     }
/* 435:610 */     throw ProtobufException.malformedVarint();
/* 436:    */   }
/* 437:    */   
/* 438:    */   public int readRawLittleEndian32()
/* 439:    */     throws IOException
/* 440:    */   {
/* 441:621 */     byte[] bs = new byte[4];
/* 442:622 */     this.buffer.get(bs);
/* 443:    */     
/* 444:    */ 
/* 445:    */ 
/* 446:    */ 
/* 447:    */ 
/* 448:    */ 
/* 449:    */ 
/* 450:    */ 
/* 451:631 */     return bs[0] & 0xFF | (bs[1] & 0xFF) << 8 | (bs[2] & 0xFF) << 16 | (bs[3] & 0xFF) << 24;
/* 452:    */   }
/* 453:    */   
/* 454:    */   public long readRawLittleEndian64()
/* 455:    */     throws IOException
/* 456:    */   {
/* 457:645 */     byte[] bs = new byte[8];
/* 458:646 */     this.buffer.get(bs);
/* 459:    */     
/* 460:    */ 
/* 461:    */ 
/* 462:    */ 
/* 463:    */ 
/* 464:    */ 
/* 465:    */ 
/* 466:    */ 
/* 467:    */ 
/* 468:    */ 
/* 469:    */ 
/* 470:    */ 
/* 471:659 */     return bs[0] & 0xFF | (bs[1] & 0xFF) << 8 | (bs[2] & 0xFF) << 16 | (bs[3] & 0xFF) << 24 | (bs[4] & 0xFF) << 32 | (bs[5] & 0xFF) << 40 | (bs[6] & 0xFF) << 48 | (bs[7] & 0xFF) << 56;
/* 472:    */   }
/* 473:    */   
/* 474:    */   public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated)
/* 475:    */     throws IOException
/* 476:    */   {
/* 477:673 */     int length = readRawVarint32();
/* 478:674 */     if (length < 0) {
/* 479:675 */       throw ProtobufException.negativeSize();
/* 480:    */     }
/* 481:677 */     if (utf8String)
/* 482:    */     {
/* 483:681 */       if (this.buffer.hasArray())
/* 484:    */       {
/* 485:683 */         output.writeByteRange(true, fieldNumber, this.buffer.array(), this.buffer
/* 486:684 */           .arrayOffset() + this.buffer.position(), length, repeated);
/* 487:685 */         this.buffer.position(this.buffer.position() + length);
/* 488:    */       }
/* 489:    */       else
/* 490:    */       {
/* 491:689 */         byte[] bytes = new byte[length];
/* 492:690 */         this.buffer.get(bytes);
/* 493:691 */         output.writeByteRange(true, fieldNumber, bytes, 0, bytes.length, repeated);
/* 494:    */       }
/* 495:    */     }
/* 496:    */     else
/* 497:    */     {
/* 498:697 */       if (this.buffer.remaining() < length) {
/* 499:698 */         throw ProtobufException.misreportedSize();
/* 500:    */       }
/* 501:700 */       ByteBuffer dup = this.buffer.slice();
/* 502:701 */       dup.limit(length);
/* 503:    */       
/* 504:703 */       output.writeBytes(fieldNumber, dup, repeated);
/* 505:    */       
/* 506:705 */       this.buffer.position(this.buffer.position() + length);
/* 507:    */     }
/* 508:    */   }
/* 509:    */   
/* 510:    */   public ByteBuffer readByteBuffer()
/* 511:    */     throws IOException
/* 512:    */   {
/* 513:719 */     return ByteBuffer.wrap(readByteArray());
/* 514:    */   }
/* 515:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ByteBufferInput
 * JD-Core Version:    0.7.0.1
 */