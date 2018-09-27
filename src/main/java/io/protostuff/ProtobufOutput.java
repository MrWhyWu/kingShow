/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.DataOutput;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ 
/*   8:    */ public final class ProtobufOutput
/*   9:    */   extends WriteSession
/*  10:    */   implements Output
/*  11:    */ {
/*  12:    */   public static final int LITTLE_ENDIAN_32_SIZE = 4;
/*  13:    */   public static final int LITTLE_ENDIAN_64_SIZE = 8;
/*  14:    */   
/*  15:    */   public ProtobufOutput(LinkedBuffer buffer)
/*  16:    */   {
/*  17: 43 */     super(buffer);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public ProtobufOutput(LinkedBuffer buffer, int nextBufferSize)
/*  21:    */   {
/*  22: 48 */     super(buffer, nextBufferSize);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public ProtobufOutput clear()
/*  26:    */   {
/*  27: 57 */     super.clear();
/*  28: 58 */     return this;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void writeInt32(int fieldNumber, int value, boolean repeated)
/*  32:    */     throws IOException
/*  33:    */   {
/*  34: 64 */     if (value < 0) {
/*  35: 66 */       this.tail = writeTagAndRawVarInt64(
/*  36: 67 */         WireFormat.makeTag(fieldNumber, 0), value, this, this.tail);
/*  37:    */     } else {
/*  38: 74 */       this.tail = writeTagAndRawVarInt32(
/*  39: 75 */         WireFormat.makeTag(fieldNumber, 0), value, this, this.tail);
/*  40:    */     }
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void writeUInt32(int fieldNumber, int value, boolean repeated)
/*  44:    */     throws IOException
/*  45:    */   {
/*  46: 85 */     this.tail = writeTagAndRawVarInt32(
/*  47: 86 */       WireFormat.makeTag(fieldNumber, 0), value, this, this.tail);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void writeSInt32(int fieldNumber, int value, boolean repeated)
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 95 */     this.tail = writeTagAndRawVarInt32(
/*  54: 96 */       WireFormat.makeTag(fieldNumber, 0), 
/*  55: 97 */       encodeZigZag32(value), this, this.tail);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void writeFixed32(int fieldNumber, int value, boolean repeated)
/*  59:    */     throws IOException
/*  60:    */   {
/*  61:105 */     this.tail = writeTagAndRawLittleEndian32(
/*  62:106 */       WireFormat.makeTag(fieldNumber, 5), value, this, this.tail);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void writeSFixed32(int fieldNumber, int value, boolean repeated)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68:115 */     this.tail = writeTagAndRawLittleEndian32(
/*  69:116 */       WireFormat.makeTag(fieldNumber, 5), value, this, this.tail);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void writeInt64(int fieldNumber, long value, boolean repeated)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75:125 */     this.tail = writeTagAndRawVarInt64(
/*  76:126 */       WireFormat.makeTag(fieldNumber, 0), value, this, this.tail);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public void writeUInt64(int fieldNumber, long value, boolean repeated)
/*  80:    */     throws IOException
/*  81:    */   {
/*  82:135 */     this.tail = writeTagAndRawVarInt64(
/*  83:136 */       WireFormat.makeTag(fieldNumber, 0), value, this, this.tail);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public void writeSInt64(int fieldNumber, long value, boolean repeated)
/*  87:    */     throws IOException
/*  88:    */   {
/*  89:145 */     this.tail = writeTagAndRawVarInt64(
/*  90:146 */       WireFormat.makeTag(fieldNumber, 0), 
/*  91:147 */       encodeZigZag64(value), this, this.tail);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void writeFixed64(int fieldNumber, long value, boolean repeated)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:155 */     this.tail = writeTagAndRawLittleEndian64(
/*  98:156 */       WireFormat.makeTag(fieldNumber, 1), value, this, this.tail);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public void writeSFixed64(int fieldNumber, long value, boolean repeated)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104:165 */     this.tail = writeTagAndRawLittleEndian64(
/* 105:166 */       WireFormat.makeTag(fieldNumber, 1), value, this, this.tail);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void writeFloat(int fieldNumber, float value, boolean repeated)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:175 */     this.tail = writeTagAndRawLittleEndian32(
/* 112:176 */       WireFormat.makeTag(fieldNumber, 5), 
/* 113:177 */       Float.floatToRawIntBits(value), this, this.tail);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void writeDouble(int fieldNumber, double value, boolean repeated)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:185 */     this.tail = writeTagAndRawLittleEndian64(
/* 120:186 */       WireFormat.makeTag(fieldNumber, 1), 
/* 121:187 */       Double.doubleToRawLongBits(value), this, this.tail);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void writeBool(int fieldNumber, boolean value, boolean repeated)
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:195 */     this.tail = writeTagAndRawVarInt32(
/* 128:196 */       WireFormat.makeTag(fieldNumber, 0), value ? 1 : 0, this, this.tail);
/* 129:    */   }
/* 130:    */   
/* 131:    */   public void writeEnum(int fieldNumber, int number, boolean repeated)
/* 132:    */     throws IOException
/* 133:    */   {
/* 134:205 */     writeInt32(fieldNumber, number, repeated);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void writeString(int fieldNumber, CharSequence value, boolean repeated)
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:211 */     this.tail = StringSerializer.writeUTF8VarDelimited(value, this, 
/* 141:    */     
/* 142:    */ 
/* 143:214 */       writeRawVarInt32(WireFormat.makeTag(fieldNumber, 2), this, this.tail));
/* 144:    */   }
/* 145:    */   
/* 146:    */   public void writeBytes(int fieldNumber, ByteString value, boolean repeated)
/* 147:    */     throws IOException
/* 148:    */   {
/* 149:220 */     writeByteArray(fieldNumber, value.getBytes(), repeated);
/* 150:    */   }
/* 151:    */   
/* 152:    */   public void writeByteArray(int fieldNumber, byte[] bytes, boolean repeated)
/* 153:    */     throws IOException
/* 154:    */   {
/* 155:226 */     this.tail = writeTagAndByteArray(
/* 156:227 */       WireFormat.makeTag(fieldNumber, 2), bytes, 0, bytes.length, this, this.tail);
/* 157:    */   }
/* 158:    */   
/* 159:    */   public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated)
/* 160:    */     throws IOException
/* 161:    */   {
/* 162:237 */     this.tail = writeTagAndByteArray(
/* 163:238 */       WireFormat.makeTag(fieldNumber, 2), value, offset, length, this, this.tail);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated)
/* 167:    */     throws IOException
/* 168:    */   {
/* 169:    */     LinkedBuffer lastBuffer;
/* 170:251 */     if ((fieldNumber < 16) && (this.tail.offset != this.tail.buffer.length))
/* 171:    */     {
/* 172:253 */       LinkedBuffer lastBuffer = this.tail;
/* 173:254 */       this.size += 1;
/* 174:255 */       lastBuffer.buffer[(lastBuffer.offset++)] = 
/* 175:256 */         ((byte)WireFormat.makeTag(fieldNumber, 2));
/* 176:    */     }
/* 177:    */     else
/* 178:    */     {
/* 179:260 */       this.tail = (lastBuffer = writeRawVarInt32(
/* 180:261 */         WireFormat.makeTag(fieldNumber, 2), this, this.tail));
/* 181:    */     }
/* 182:264 */     int lastOffset = this.tail.offset;int lastSize = this.size;
/* 183:266 */     if (lastOffset == lastBuffer.buffer.length)
/* 184:    */     {
/* 185:269 */       LinkedBuffer nextBuffer = new LinkedBuffer(this.nextBufferSize);
/* 186:    */       
/* 187:271 */       this.tail = nextBuffer;
/* 188:    */       
/* 189:273 */       schema.writeTo(this, value);
/* 190:    */       
/* 191:275 */       int msgSize = this.size - lastSize;
/* 192:    */       
/* 193:277 */       byte[] delimited = new byte[computeRawVarint32Size(msgSize)];
/* 194:278 */       writeRawVarInt32(msgSize, delimited, 0);
/* 195:    */       
/* 196:280 */       this.size += delimited.length;
/* 197:    */       
/* 198:    */ 
/* 199:283 */       new LinkedBuffer(delimited, 0, delimited.length, lastBuffer).next = nextBuffer;
/* 200:284 */       return;
/* 201:    */     }
/* 202:288 */     lastBuffer.offset += 1;
/* 203:289 */     this.size += 1;
/* 204:    */     
/* 205:291 */     schema.writeTo(this, value);
/* 206:    */     
/* 207:293 */     int msgSize = this.size - lastSize - 1;
/* 208:296 */     if (msgSize < 128)
/* 209:    */     {
/* 210:299 */       lastBuffer.buffer[lastOffset] = ((byte)msgSize);
/* 211:300 */       return;
/* 212:    */     }
/* 213:306 */     LinkedBuffer view = new LinkedBuffer(lastBuffer.buffer, lastOffset + 1, lastBuffer.offset);
/* 214:309 */     if (lastBuffer == this.tail) {
/* 215:310 */       this.tail = view;
/* 216:    */     } else {
/* 217:312 */       view.next = lastBuffer.next;
/* 218:    */     }
/* 219:315 */     lastBuffer.offset = lastOffset;
/* 220:    */     
/* 221:317 */     byte[] delimited = new byte[computeRawVarint32Size(msgSize)];
/* 222:318 */     writeRawVarInt32(msgSize, delimited, 0);
/* 223:    */     
/* 224:    */ 
/* 225:321 */     this.size += delimited.length - 1;
/* 226:    */     
/* 227:    */ 
/* 228:324 */     new LinkedBuffer(delimited, 0, delimited.length, lastBuffer).next = view;
/* 229:    */   }
/* 230:    */   
/* 231:    */   public static LinkedBuffer writeRawVarInt32(int value, WriteSession session, LinkedBuffer lb)
/* 232:    */   {
/* 233:346 */     int size = computeRawVarint32Size(value);
/* 234:348 */     if (lb.offset + size > lb.buffer.length) {
/* 235:349 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/* 236:    */     }
/* 237:351 */     byte[] buffer = lb.buffer;
/* 238:352 */     int offset = lb.offset;
/* 239:353 */     lb.offset += size;
/* 240:354 */     session.size += size;
/* 241:356 */     if (size == 1)
/* 242:    */     {
/* 243:357 */       buffer[offset] = ((byte)value);
/* 244:    */     }
/* 245:    */     else
/* 246:    */     {
/* 247:360 */       int i = 0;
/* 248:360 */       for (int last = size - 1; i < last; value >>>= 7)
/* 249:    */       {
/* 250:361 */         buffer[(offset++)] = ((byte)(value & 0x7F | 0x80));i++;
/* 251:    */       }
/* 252:363 */       buffer[offset] = ((byte)value);
/* 253:    */     }
/* 254:366 */     return lb;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public static LinkedBuffer writeTagAndLinkedBuffer(int tag, LinkedBuffer buffer, WriteSession session, LinkedBuffer lb)
/* 258:    */   {
/* 259:375 */     int valueLen = buffer.offset - buffer.start;
/* 260:376 */     if (valueLen == 0) {
/* 261:379 */       return writeTagAndRawVarInt32(tag, valueLen, session, lb);
/* 262:    */     }
/* 263:382 */     lb = writeTagAndRawVarInt32(tag, valueLen, session, lb);
/* 264:    */     
/* 265:384 */     lb.next = buffer;
/* 266:    */     
/* 267:386 */     int remaining = lb.buffer.length - lb.offset;
/* 268:    */     
/* 269:388 */     return remaining == 0 ? new LinkedBuffer(session.nextBufferSize, buffer) : new LinkedBuffer(lb, buffer);
/* 270:    */   }
/* 271:    */   
/* 272:    */   public static LinkedBuffer writeTagAndByteArray(int tag, byte[] value, int offset, int valueLen, WriteSession session, LinkedBuffer lb)
/* 273:    */   {
/* 274:399 */     if (valueLen == 0) {
/* 275:402 */       return writeTagAndRawVarInt32(tag, valueLen, session, lb);
/* 276:    */     }
/* 277:405 */     lb = writeTagAndRawVarInt32(tag, valueLen, session, lb);
/* 278:    */     
/* 279:407 */     session.size += valueLen;
/* 280:    */     
/* 281:409 */     int available = lb.buffer.length - lb.offset;
/* 282:410 */     if (valueLen > available)
/* 283:    */     {
/* 284:412 */       if (available + session.nextBufferSize < valueLen)
/* 285:    */       {
/* 286:415 */         if (available == 0) {
/* 287:418 */           return new LinkedBuffer(session.nextBufferSize, new LinkedBuffer(value, offset, offset + valueLen, lb));
/* 288:    */         }
/* 289:423 */         return new LinkedBuffer(lb, new LinkedBuffer(value, offset, offset + valueLen, lb));
/* 290:    */       }
/* 291:428 */       System.arraycopy(value, offset, lb.buffer, lb.offset, available);
/* 292:    */       
/* 293:430 */       lb.offset += available;
/* 294:    */       
/* 295:    */ 
/* 296:433 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/* 297:    */       
/* 298:435 */       int leftover = valueLen - available;
/* 299:    */       
/* 300:    */ 
/* 301:438 */       System.arraycopy(value, offset + available, lb.buffer, 0, leftover);
/* 302:    */       
/* 303:440 */       lb.offset += leftover;
/* 304:    */       
/* 305:442 */       return lb;
/* 306:    */     }
/* 307:446 */     System.arraycopy(value, offset, lb.buffer, lb.offset, valueLen);
/* 308:    */     
/* 309:448 */     lb.offset += valueLen;
/* 310:    */     
/* 311:450 */     return lb;
/* 312:    */   }
/* 313:    */   
/* 314:    */   public static LinkedBuffer writeTagAndRawVarInt32(int tag, int value, WriteSession session, LinkedBuffer lb)
/* 315:    */   {
/* 316:459 */     int tagSize = computeRawVarint32Size(tag);
/* 317:460 */     int size = computeRawVarint32Size(value);
/* 318:461 */     int totalSize = tagSize + size;
/* 319:463 */     if (lb.offset + totalSize > lb.buffer.length) {
/* 320:464 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/* 321:    */     }
/* 322:466 */     byte[] buffer = lb.buffer;
/* 323:467 */     int offset = lb.offset;
/* 324:468 */     lb.offset += totalSize;
/* 325:469 */     session.size += totalSize;
/* 326:471 */     if (tagSize == 1)
/* 327:    */     {
/* 328:472 */       buffer[(offset++)] = ((byte)tag);
/* 329:    */     }
/* 330:    */     else
/* 331:    */     {
/* 332:475 */       int i = 0;
/* 333:475 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 334:    */       {
/* 335:476 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 336:    */       }
/* 337:478 */       buffer[(offset++)] = ((byte)tag);
/* 338:    */     }
/* 339:481 */     if (size == 1)
/* 340:    */     {
/* 341:482 */       buffer[offset] = ((byte)value);
/* 342:    */     }
/* 343:    */     else
/* 344:    */     {
/* 345:485 */       int i = 0;
/* 346:485 */       for (int last = size - 1; i < last; value >>>= 7)
/* 347:    */       {
/* 348:486 */         buffer[(offset++)] = ((byte)(value & 0x7F | 0x80));i++;
/* 349:    */       }
/* 350:488 */       buffer[offset] = ((byte)value);
/* 351:    */     }
/* 352:491 */     return lb;
/* 353:    */   }
/* 354:    */   
/* 355:    */   public static LinkedBuffer writeTagAndRawVarInt64(int tag, long value, WriteSession session, LinkedBuffer lb)
/* 356:    */   {
/* 357:500 */     int tagSize = computeRawVarint32Size(tag);
/* 358:501 */     int size = computeRawVarint64Size(value);
/* 359:502 */     int totalSize = tagSize + size;
/* 360:504 */     if (lb.offset + totalSize > lb.buffer.length) {
/* 361:505 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/* 362:    */     }
/* 363:507 */     byte[] buffer = lb.buffer;
/* 364:508 */     int offset = lb.offset;
/* 365:509 */     lb.offset += totalSize;
/* 366:510 */     session.size += totalSize;
/* 367:512 */     if (tagSize == 1)
/* 368:    */     {
/* 369:513 */       buffer[(offset++)] = ((byte)tag);
/* 370:    */     }
/* 371:    */     else
/* 372:    */     {
/* 373:516 */       int i = 0;
/* 374:516 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 375:    */       {
/* 376:517 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 377:    */       }
/* 378:519 */       buffer[(offset++)] = ((byte)tag);
/* 379:    */     }
/* 380:522 */     if (size == 1)
/* 381:    */     {
/* 382:523 */       buffer[offset] = ((byte)(int)value);
/* 383:    */     }
/* 384:    */     else
/* 385:    */     {
/* 386:526 */       int i = 0;
/* 387:526 */       for (int last = size - 1; i < last; value >>>= 7)
/* 388:    */       {
/* 389:527 */         buffer[(offset++)] = ((byte)((int)value & 0x7F | 0x80));i++;
/* 390:    */       }
/* 391:529 */       buffer[offset] = ((byte)(int)value);
/* 392:    */     }
/* 393:532 */     return lb;
/* 394:    */   }
/* 395:    */   
/* 396:    */   public static LinkedBuffer writeTagAndRawLittleEndian32(int tag, int value, WriteSession session, LinkedBuffer lb)
/* 397:    */   {
/* 398:541 */     int tagSize = computeRawVarint32Size(tag);
/* 399:542 */     int totalSize = tagSize + 4;
/* 400:544 */     if (lb.offset + totalSize > lb.buffer.length) {
/* 401:545 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/* 402:    */     }
/* 403:547 */     byte[] buffer = lb.buffer;
/* 404:548 */     int offset = lb.offset;
/* 405:549 */     lb.offset += totalSize;
/* 406:550 */     session.size += totalSize;
/* 407:552 */     if (tagSize == 1)
/* 408:    */     {
/* 409:553 */       buffer[(offset++)] = ((byte)tag);
/* 410:    */     }
/* 411:    */     else
/* 412:    */     {
/* 413:556 */       int i = 0;
/* 414:556 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 415:    */       {
/* 416:557 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 417:    */       }
/* 418:559 */       buffer[(offset++)] = ((byte)tag);
/* 419:    */     }
/* 420:562 */     writeRawLittleEndian32(value, buffer, offset);
/* 421:    */     
/* 422:564 */     return lb;
/* 423:    */   }
/* 424:    */   
/* 425:    */   public static LinkedBuffer writeTagAndRawLittleEndian64(int tag, long value, WriteSession session, LinkedBuffer lb)
/* 426:    */   {
/* 427:573 */     int tagSize = computeRawVarint32Size(tag);
/* 428:574 */     int totalSize = tagSize + 8;
/* 429:576 */     if (lb.offset + totalSize > lb.buffer.length) {
/* 430:577 */       lb = new LinkedBuffer(session.nextBufferSize, lb);
/* 431:    */     }
/* 432:579 */     byte[] buffer = lb.buffer;
/* 433:580 */     int offset = lb.offset;
/* 434:581 */     lb.offset += totalSize;
/* 435:582 */     session.size += totalSize;
/* 436:584 */     if (tagSize == 1)
/* 437:    */     {
/* 438:585 */       buffer[(offset++)] = ((byte)tag);
/* 439:    */     }
/* 440:    */     else
/* 441:    */     {
/* 442:588 */       int i = 0;
/* 443:588 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 444:    */       {
/* 445:589 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 446:    */       }
/* 447:591 */       buffer[(offset++)] = ((byte)tag);
/* 448:    */     }
/* 449:594 */     writeRawLittleEndian64(value, buffer, offset);
/* 450:    */     
/* 451:596 */     return lb;
/* 452:    */   }
/* 453:    */   
/* 454:    */   public static void writeRawVarInt32(int value, byte[] buf, int offset)
/* 455:    */     throws IOException
/* 456:    */   {
/* 457:    */     for (;;)
/* 458:    */     {
/* 459:604 */       if ((value & 0xFFFFFF80) == 0)
/* 460:    */       {
/* 461:606 */         buf[offset] = ((byte)value);
/* 462:607 */         return;
/* 463:    */       }
/* 464:611 */       buf[(offset++)] = ((byte)(value & 0x7F | 0x80));
/* 465:612 */       value >>>= 7;
/* 466:    */     }
/* 467:    */   }
/* 468:    */   
/* 469:    */   public static void writeRawVarInt32Bytes(OutputStream out, int value)
/* 470:    */     throws IOException
/* 471:    */   {
/* 472:    */     for (;;)
/* 473:    */     {
/* 474:624 */       if ((value & 0xFFFFFF80) == 0)
/* 475:    */       {
/* 476:626 */         out.write(value);
/* 477:627 */         return;
/* 478:    */       }
/* 479:631 */       out.write(value & 0x7F | 0x80);
/* 480:632 */       value >>>= 7;
/* 481:    */     }
/* 482:    */   }
/* 483:    */   
/* 484:    */   public static void writeRawVarInt32Bytes(DataOutput out, int value)
/* 485:    */     throws IOException
/* 486:    */   {
/* 487:    */     for (;;)
/* 488:    */     {
/* 489:644 */       if ((value & 0xFFFFFF80) == 0)
/* 490:    */       {
/* 491:646 */         out.write(value);
/* 492:647 */         return;
/* 493:    */       }
/* 494:651 */       out.write(value & 0x7F | 0x80);
/* 495:652 */       value >>>= 7;
/* 496:    */     }
/* 497:    */   }
/* 498:    */   
/* 499:    */   public static byte[] getTagAndRawVarInt32Bytes(int tag, int value)
/* 500:    */   {
/* 501:662 */     int tagSize = computeRawVarint32Size(tag);
/* 502:663 */     int size = computeRawVarint32Size(value);
/* 503:664 */     int offset = 0;
/* 504:665 */     byte[] buffer = new byte[tagSize + size];
/* 505:666 */     if (tagSize == 1)
/* 506:    */     {
/* 507:667 */       buffer[(offset++)] = ((byte)tag);
/* 508:    */     }
/* 509:    */     else
/* 510:    */     {
/* 511:670 */       int i = 0;
/* 512:670 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 513:    */       {
/* 514:671 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 515:    */       }
/* 516:673 */       buffer[(offset++)] = ((byte)tag);
/* 517:    */     }
/* 518:676 */     if (size == 1)
/* 519:    */     {
/* 520:677 */       buffer[offset] = ((byte)value);
/* 521:    */     }
/* 522:    */     else
/* 523:    */     {
/* 524:680 */       int i = 0;
/* 525:680 */       for (int last = size - 1; i < last; value >>>= 7)
/* 526:    */       {
/* 527:681 */         buffer[(offset++)] = ((byte)(value & 0x7F | 0x80));i++;
/* 528:    */       }
/* 529:683 */       buffer[offset] = ((byte)value);
/* 530:    */     }
/* 531:686 */     return buffer;
/* 532:    */   }
/* 533:    */   
/* 534:    */   public static byte[] getTagAndRawVarInt64Bytes(int tag, long value)
/* 535:    */   {
/* 536:694 */     int tagSize = computeRawVarint32Size(tag);
/* 537:695 */     int size = computeRawVarint64Size(value);
/* 538:696 */     int offset = 0;
/* 539:697 */     byte[] buffer = new byte[tagSize + size];
/* 540:699 */     if (tagSize == 1)
/* 541:    */     {
/* 542:701 */       buffer[(offset++)] = ((byte)tag);
/* 543:    */     }
/* 544:    */     else
/* 545:    */     {
/* 546:705 */       int i = 0;
/* 547:705 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 548:    */       {
/* 549:706 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 550:    */       }
/* 551:708 */       buffer[(offset++)] = ((byte)tag);
/* 552:    */     }
/* 553:711 */     if (size == 1)
/* 554:    */     {
/* 555:713 */       buffer[offset] = ((byte)(int)value);
/* 556:    */     }
/* 557:    */     else
/* 558:    */     {
/* 559:717 */       int i = 0;
/* 560:717 */       for (int last = size - 1; i < last; value >>>= 7)
/* 561:    */       {
/* 562:718 */         buffer[(offset++)] = ((byte)((int)value & 0x7F | 0x80));i++;
/* 563:    */       }
/* 564:720 */       buffer[offset] = ((byte)(int)value);
/* 565:    */     }
/* 566:723 */     return buffer;
/* 567:    */   }
/* 568:    */   
/* 569:    */   public static byte[] getTagAndRawLittleEndian32Bytes(int tag, int value)
/* 570:    */   {
/* 571:731 */     int tagSize = computeRawVarint32Size(tag);
/* 572:732 */     int offset = 0;
/* 573:733 */     byte[] buffer = new byte[tagSize + 4];
/* 574:735 */     if (tagSize == 1)
/* 575:    */     {
/* 576:737 */       buffer[(offset++)] = ((byte)tag);
/* 577:    */     }
/* 578:    */     else
/* 579:    */     {
/* 580:741 */       int i = 0;
/* 581:741 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 582:    */       {
/* 583:742 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 584:    */       }
/* 585:744 */       buffer[(offset++)] = ((byte)tag);
/* 586:    */     }
/* 587:747 */     writeRawLittleEndian32(value, buffer, offset);
/* 588:    */     
/* 589:749 */     return buffer;
/* 590:    */   }
/* 591:    */   
/* 592:    */   public static byte[] getTagAndRawLittleEndian64Bytes(int tag, long value)
/* 593:    */   {
/* 594:757 */     int tagSize = computeRawVarint32Size(tag);
/* 595:758 */     int offset = 0;
/* 596:759 */     byte[] buffer = new byte[tagSize + 8];
/* 597:761 */     if (tagSize == 1)
/* 598:    */     {
/* 599:763 */       buffer[(offset++)] = ((byte)tag);
/* 600:    */     }
/* 601:    */     else
/* 602:    */     {
/* 603:767 */       int i = 0;
/* 604:767 */       for (int last = tagSize - 1; i < last; tag >>>= 7)
/* 605:    */       {
/* 606:768 */         buffer[(offset++)] = ((byte)(tag & 0x7F | 0x80));i++;
/* 607:    */       }
/* 608:770 */       buffer[(offset++)] = ((byte)tag);
/* 609:    */     }
/* 610:773 */     writeRawLittleEndian64(value, buffer, offset);
/* 611:    */     
/* 612:775 */     return buffer;
/* 613:    */   }
/* 614:    */   
/* 615:    */   public static int writeRawLittleEndian32(int value, byte[] buffer, int offset)
/* 616:    */   {
/* 617:783 */     if (buffer.length - offset < 4) {
/* 618:784 */       throw new IllegalArgumentException("buffer capacity not enough.");
/* 619:    */     }
/* 620:786 */     buffer[(offset++)] = ((byte)(value & 0xFF));
/* 621:787 */     buffer[(offset++)] = ((byte)(value >> 8 & 0xFF));
/* 622:788 */     buffer[(offset++)] = ((byte)(value >> 16 & 0xFF));
/* 623:789 */     buffer[offset] = ((byte)(value >> 24 & 0xFF));
/* 624:    */     
/* 625:791 */     return 4;
/* 626:    */   }
/* 627:    */   
/* 628:    */   public static int writeRawLittleEndian64(long value, byte[] buffer, int offset)
/* 629:    */   {
/* 630:799 */     if (buffer.length - offset < 8) {
/* 631:800 */       throw new IllegalArgumentException("buffer capacity not enough.");
/* 632:    */     }
/* 633:802 */     buffer[(offset++)] = ((byte)(int)(value & 0xFF));
/* 634:803 */     buffer[(offset++)] = ((byte)(int)(value >> 8 & 0xFF));
/* 635:804 */     buffer[(offset++)] = ((byte)(int)(value >> 16 & 0xFF));
/* 636:805 */     buffer[(offset++)] = ((byte)(int)(value >> 24 & 0xFF));
/* 637:806 */     buffer[(offset++)] = ((byte)(int)(value >> 32 & 0xFF));
/* 638:807 */     buffer[(offset++)] = ((byte)(int)(value >> 40 & 0xFF));
/* 639:808 */     buffer[(offset++)] = ((byte)(int)(value >> 48 & 0xFF));
/* 640:809 */     buffer[offset] = ((byte)(int)(value >> 56 & 0xFF));
/* 641:    */     
/* 642:811 */     return 8;
/* 643:    */   }
/* 644:    */   
/* 645:    */   public static byte[] getRawVarInt32Bytes(int value)
/* 646:    */   {
/* 647:819 */     int size = computeRawVarint32Size(value);
/* 648:820 */     if (size == 1) {
/* 649:821 */       return new byte[] { (byte)value };
/* 650:    */     }
/* 651:823 */     int offset = 0;
/* 652:824 */     byte[] buffer = new byte[size];
/* 653:825 */     int i = 0;
/* 654:825 */     for (int last = size - 1; i < last; value >>>= 7)
/* 655:    */     {
/* 656:826 */       buffer[(offset++)] = ((byte)(value & 0x7F | 0x80));i++;
/* 657:    */     }
/* 658:828 */     buffer[offset] = ((byte)value);
/* 659:829 */     return buffer;
/* 660:    */   }
/* 661:    */   
/* 662:    */   public static int computeRawVarint32Size(int value)
/* 663:    */   {
/* 664:870 */     if ((value & 0xFFFFFF80) == 0) {
/* 665:871 */       return 1;
/* 666:    */     }
/* 667:872 */     if ((value & 0xFFFFC000) == 0) {
/* 668:873 */       return 2;
/* 669:    */     }
/* 670:874 */     if ((value & 0xFFE00000) == 0) {
/* 671:875 */       return 3;
/* 672:    */     }
/* 673:876 */     if ((value & 0xF0000000) == 0) {
/* 674:877 */       return 4;
/* 675:    */     }
/* 676:878 */     return 5;
/* 677:    */   }
/* 678:    */   
/* 679:    */   public static int computeRawVarint64Size(long value)
/* 680:    */   {
/* 681:886 */     if ((value & 0xFFFFFF80) == 0L) {
/* 682:887 */       return 1;
/* 683:    */     }
/* 684:888 */     if ((value & 0xFFFFC000) == 0L) {
/* 685:889 */       return 2;
/* 686:    */     }
/* 687:890 */     if ((value & 0xFFE00000) == 0L) {
/* 688:891 */       return 3;
/* 689:    */     }
/* 690:892 */     if ((value & 0xF0000000) == 0L) {
/* 691:893 */       return 4;
/* 692:    */     }
/* 693:894 */     if ((value & 0x0) == 0L) {
/* 694:895 */       return 5;
/* 695:    */     }
/* 696:896 */     if ((value & 0x0) == 0L) {
/* 697:897 */       return 6;
/* 698:    */     }
/* 699:898 */     if ((value & 0x0) == 0L) {
/* 700:899 */       return 7;
/* 701:    */     }
/* 702:900 */     if ((value & 0x0) == 0L) {
/* 703:901 */       return 8;
/* 704:    */     }
/* 705:902 */     if ((value & 0x0) == 0L) {
/* 706:903 */       return 9;
/* 707:    */     }
/* 708:904 */     return 10;
/* 709:    */   }
/* 710:    */   
/* 711:    */   public static int encodeZigZag32(int n)
/* 712:    */   {
/* 713:919 */     return n << 1 ^ n >> 31;
/* 714:    */   }
/* 715:    */   
/* 716:    */   public static long encodeZigZag64(long n)
/* 717:    */   {
/* 718:934 */     return n << 1 ^ n >> 63;
/* 719:    */   }
/* 720:    */   
/* 721:    */   public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated)
/* 722:    */     throws IOException
/* 723:    */   {
/* 724:943 */     writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(), value
/* 725:944 */       .remaining(), repeated);
/* 726:    */   }
/* 727:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.ProtobufOutput
 * JD-Core Version:    0.7.0.1
 */