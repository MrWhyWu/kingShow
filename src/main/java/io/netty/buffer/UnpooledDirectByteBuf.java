/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.Buffer;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ import java.nio.channels.FileChannel;
/*  12:    */ import java.nio.channels.GatheringByteChannel;
/*  13:    */ import java.nio.channels.ScatteringByteChannel;
/*  14:    */ 
/*  15:    */ public class UnpooledDirectByteBuf
/*  16:    */   extends AbstractReferenceCountedByteBuf
/*  17:    */ {
/*  18:    */   private final ByteBufAllocator alloc;
/*  19:    */   private ByteBuffer buffer;
/*  20:    */   private ByteBuffer tmpNioBuf;
/*  21:    */   private int capacity;
/*  22:    */   private boolean doNotFree;
/*  23:    */   
/*  24:    */   public UnpooledDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  25:    */   {
/*  26: 51 */     super(maxCapacity);
/*  27: 52 */     if (alloc == null) {
/*  28: 53 */       throw new NullPointerException("alloc");
/*  29:    */     }
/*  30: 55 */     if (initialCapacity < 0) {
/*  31: 56 */       throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
/*  32:    */     }
/*  33: 58 */     if (maxCapacity < 0) {
/*  34: 59 */       throw new IllegalArgumentException("maxCapacity: " + maxCapacity);
/*  35:    */     }
/*  36: 61 */     if (initialCapacity > maxCapacity) {
/*  37: 62 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] {
/*  38: 63 */         Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  39:    */     }
/*  40: 66 */     this.alloc = alloc;
/*  41: 67 */     setByteBuffer(ByteBuffer.allocateDirect(initialCapacity));
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected UnpooledDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity)
/*  45:    */   {
/*  46: 76 */     super(maxCapacity);
/*  47: 77 */     if (alloc == null) {
/*  48: 78 */       throw new NullPointerException("alloc");
/*  49:    */     }
/*  50: 80 */     if (initialBuffer == null) {
/*  51: 81 */       throw new NullPointerException("initialBuffer");
/*  52:    */     }
/*  53: 83 */     if (!initialBuffer.isDirect()) {
/*  54: 84 */       throw new IllegalArgumentException("initialBuffer is not a direct buffer.");
/*  55:    */     }
/*  56: 86 */     if (initialBuffer.isReadOnly()) {
/*  57: 87 */       throw new IllegalArgumentException("initialBuffer is a read-only buffer.");
/*  58:    */     }
/*  59: 90 */     int initialCapacity = initialBuffer.remaining();
/*  60: 91 */     if (initialCapacity > maxCapacity) {
/*  61: 92 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] {
/*  62: 93 */         Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  63:    */     }
/*  64: 96 */     this.alloc = alloc;
/*  65: 97 */     this.doNotFree = true;
/*  66: 98 */     setByteBuffer(initialBuffer.slice().order(ByteOrder.BIG_ENDIAN));
/*  67: 99 */     writerIndex(initialCapacity);
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected ByteBuffer allocateDirect(int initialCapacity)
/*  71:    */   {
/*  72:106 */     return ByteBuffer.allocateDirect(initialCapacity);
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected void freeDirect(ByteBuffer buffer)
/*  76:    */   {
/*  77:113 */     PlatformDependent.freeDirectBuffer(buffer);
/*  78:    */   }
/*  79:    */   
/*  80:    */   private void setByteBuffer(ByteBuffer buffer)
/*  81:    */   {
/*  82:117 */     ByteBuffer oldBuffer = this.buffer;
/*  83:118 */     if (oldBuffer != null) {
/*  84:119 */       if (this.doNotFree) {
/*  85:120 */         this.doNotFree = false;
/*  86:    */       } else {
/*  87:122 */         freeDirect(oldBuffer);
/*  88:    */       }
/*  89:    */     }
/*  90:126 */     this.buffer = buffer;
/*  91:127 */     this.tmpNioBuf = null;
/*  92:128 */     this.capacity = buffer.remaining();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public boolean isDirect()
/*  96:    */   {
/*  97:133 */     return true;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public int capacity()
/* 101:    */   {
/* 102:138 */     return this.capacity;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public ByteBuf capacity(int newCapacity)
/* 106:    */   {
/* 107:143 */     checkNewCapacity(newCapacity);
/* 108:    */     
/* 109:145 */     int readerIndex = readerIndex();
/* 110:146 */     int writerIndex = writerIndex();
/* 111:    */     
/* 112:148 */     int oldCapacity = this.capacity;
/* 113:149 */     if (newCapacity > oldCapacity)
/* 114:    */     {
/* 115:150 */       ByteBuffer oldBuffer = this.buffer;
/* 116:151 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 117:152 */       oldBuffer.position(0).limit(oldBuffer.capacity());
/* 118:153 */       newBuffer.position(0).limit(oldBuffer.capacity());
/* 119:154 */       newBuffer.put(oldBuffer);
/* 120:155 */       newBuffer.clear();
/* 121:156 */       setByteBuffer(newBuffer);
/* 122:    */     }
/* 123:157 */     else if (newCapacity < oldCapacity)
/* 124:    */     {
/* 125:158 */       ByteBuffer oldBuffer = this.buffer;
/* 126:159 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 127:160 */       if (readerIndex < newCapacity)
/* 128:    */       {
/* 129:161 */         if (writerIndex > newCapacity) {
/* 130:162 */           writerIndex(writerIndex = newCapacity);
/* 131:    */         }
/* 132:164 */         oldBuffer.position(readerIndex).limit(writerIndex);
/* 133:165 */         newBuffer.position(readerIndex).limit(writerIndex);
/* 134:166 */         newBuffer.put(oldBuffer);
/* 135:167 */         newBuffer.clear();
/* 136:    */       }
/* 137:    */       else
/* 138:    */       {
/* 139:169 */         setIndex(newCapacity, newCapacity);
/* 140:    */       }
/* 141:171 */       setByteBuffer(newBuffer);
/* 142:    */     }
/* 143:173 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public ByteBufAllocator alloc()
/* 147:    */   {
/* 148:178 */     return this.alloc;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public ByteOrder order()
/* 152:    */   {
/* 153:183 */     return ByteOrder.BIG_ENDIAN;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public boolean hasArray()
/* 157:    */   {
/* 158:188 */     return false;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public byte[] array()
/* 162:    */   {
/* 163:193 */     throw new UnsupportedOperationException("direct buffer");
/* 164:    */   }
/* 165:    */   
/* 166:    */   public int arrayOffset()
/* 167:    */   {
/* 168:198 */     throw new UnsupportedOperationException("direct buffer");
/* 169:    */   }
/* 170:    */   
/* 171:    */   public boolean hasMemoryAddress()
/* 172:    */   {
/* 173:203 */     return false;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public long memoryAddress()
/* 177:    */   {
/* 178:208 */     throw new UnsupportedOperationException();
/* 179:    */   }
/* 180:    */   
/* 181:    */   public byte getByte(int index)
/* 182:    */   {
/* 183:213 */     ensureAccessible();
/* 184:214 */     return _getByte(index);
/* 185:    */   }
/* 186:    */   
/* 187:    */   protected byte _getByte(int index)
/* 188:    */   {
/* 189:219 */     return this.buffer.get(index);
/* 190:    */   }
/* 191:    */   
/* 192:    */   public short getShort(int index)
/* 193:    */   {
/* 194:224 */     ensureAccessible();
/* 195:225 */     return _getShort(index);
/* 196:    */   }
/* 197:    */   
/* 198:    */   protected short _getShort(int index)
/* 199:    */   {
/* 200:230 */     return this.buffer.getShort(index);
/* 201:    */   }
/* 202:    */   
/* 203:    */   protected short _getShortLE(int index)
/* 204:    */   {
/* 205:235 */     return ByteBufUtil.swapShort(this.buffer.getShort(index));
/* 206:    */   }
/* 207:    */   
/* 208:    */   public int getUnsignedMedium(int index)
/* 209:    */   {
/* 210:240 */     ensureAccessible();
/* 211:241 */     return _getUnsignedMedium(index);
/* 212:    */   }
/* 213:    */   
/* 214:    */   protected int _getUnsignedMedium(int index)
/* 215:    */   {
/* 216:246 */     return 
/* 217:    */     
/* 218:248 */       (getByte(index) & 0xFF) << 16 | (getByte(index + 1) & 0xFF) << 8 | getByte(index + 2) & 0xFF;
/* 219:    */   }
/* 220:    */   
/* 221:    */   protected int _getUnsignedMediumLE(int index)
/* 222:    */   {
/* 223:253 */     return 
/* 224:    */     
/* 225:255 */       getByte(index) & 0xFF | (getByte(index + 1) & 0xFF) << 8 | (getByte(index + 2) & 0xFF) << 16;
/* 226:    */   }
/* 227:    */   
/* 228:    */   public int getInt(int index)
/* 229:    */   {
/* 230:260 */     ensureAccessible();
/* 231:261 */     return _getInt(index);
/* 232:    */   }
/* 233:    */   
/* 234:    */   protected int _getInt(int index)
/* 235:    */   {
/* 236:266 */     return this.buffer.getInt(index);
/* 237:    */   }
/* 238:    */   
/* 239:    */   protected int _getIntLE(int index)
/* 240:    */   {
/* 241:271 */     return ByteBufUtil.swapInt(this.buffer.getInt(index));
/* 242:    */   }
/* 243:    */   
/* 244:    */   public long getLong(int index)
/* 245:    */   {
/* 246:276 */     ensureAccessible();
/* 247:277 */     return _getLong(index);
/* 248:    */   }
/* 249:    */   
/* 250:    */   protected long _getLong(int index)
/* 251:    */   {
/* 252:282 */     return this.buffer.getLong(index);
/* 253:    */   }
/* 254:    */   
/* 255:    */   protected long _getLongLE(int index)
/* 256:    */   {
/* 257:287 */     return ByteBufUtil.swapLong(this.buffer.getLong(index));
/* 258:    */   }
/* 259:    */   
/* 260:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 261:    */   {
/* 262:292 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 263:293 */     if (dst.hasArray()) {
/* 264:294 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 265:295 */     } else if (dst.nioBufferCount() > 0) {
/* 266:296 */       for (ByteBuffer bb : dst.nioBuffers(dstIndex, length))
/* 267:    */       {
/* 268:297 */         int bbLen = bb.remaining();
/* 269:298 */         getBytes(index, bb);
/* 270:299 */         index += bbLen;
/* 271:    */       }
/* 272:    */     } else {
/* 273:302 */       dst.setBytes(dstIndex, this, index, length);
/* 274:    */     }
/* 275:304 */     return this;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 279:    */   {
/* 280:309 */     getBytes(index, dst, dstIndex, length, false);
/* 281:310 */     return this;
/* 282:    */   }
/* 283:    */   
/* 284:    */   private void getBytes(int index, byte[] dst, int dstIndex, int length, boolean internal)
/* 285:    */   {
/* 286:314 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 287:    */     ByteBuffer tmpBuf;
/* 288:    */     ByteBuffer tmpBuf;
/* 289:317 */     if (internal) {
/* 290:318 */       tmpBuf = internalNioBuffer();
/* 291:    */     } else {
/* 292:320 */       tmpBuf = this.buffer.duplicate();
/* 293:    */     }
/* 294:322 */     tmpBuf.clear().position(index).limit(index + length);
/* 295:323 */     tmpBuf.get(dst, dstIndex, length);
/* 296:    */   }
/* 297:    */   
/* 298:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 299:    */   {
/* 300:328 */     checkReadableBytes(length);
/* 301:329 */     getBytes(this.readerIndex, dst, dstIndex, length, true);
/* 302:330 */     this.readerIndex += length;
/* 303:331 */     return this;
/* 304:    */   }
/* 305:    */   
/* 306:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 307:    */   {
/* 308:336 */     getBytes(index, dst, false);
/* 309:337 */     return this;
/* 310:    */   }
/* 311:    */   
/* 312:    */   private void getBytes(int index, ByteBuffer dst, boolean internal)
/* 313:    */   {
/* 314:341 */     checkIndex(index, dst.remaining());
/* 315:    */     ByteBuffer tmpBuf;
/* 316:    */     ByteBuffer tmpBuf;
/* 317:344 */     if (internal) {
/* 318:345 */       tmpBuf = internalNioBuffer();
/* 319:    */     } else {
/* 320:347 */       tmpBuf = this.buffer.duplicate();
/* 321:    */     }
/* 322:349 */     tmpBuf.clear().position(index).limit(index + dst.remaining());
/* 323:350 */     dst.put(tmpBuf);
/* 324:    */   }
/* 325:    */   
/* 326:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 327:    */   {
/* 328:355 */     int length = dst.remaining();
/* 329:356 */     checkReadableBytes(length);
/* 330:357 */     getBytes(this.readerIndex, dst, true);
/* 331:358 */     this.readerIndex += length;
/* 332:359 */     return this;
/* 333:    */   }
/* 334:    */   
/* 335:    */   public ByteBuf setByte(int index, int value)
/* 336:    */   {
/* 337:364 */     ensureAccessible();
/* 338:365 */     _setByte(index, value);
/* 339:366 */     return this;
/* 340:    */   }
/* 341:    */   
/* 342:    */   protected void _setByte(int index, int value)
/* 343:    */   {
/* 344:371 */     this.buffer.put(index, (byte)value);
/* 345:    */   }
/* 346:    */   
/* 347:    */   public ByteBuf setShort(int index, int value)
/* 348:    */   {
/* 349:376 */     ensureAccessible();
/* 350:377 */     _setShort(index, value);
/* 351:378 */     return this;
/* 352:    */   }
/* 353:    */   
/* 354:    */   protected void _setShort(int index, int value)
/* 355:    */   {
/* 356:383 */     this.buffer.putShort(index, (short)value);
/* 357:    */   }
/* 358:    */   
/* 359:    */   protected void _setShortLE(int index, int value)
/* 360:    */   {
/* 361:388 */     this.buffer.putShort(index, ByteBufUtil.swapShort((short)value));
/* 362:    */   }
/* 363:    */   
/* 364:    */   public ByteBuf setMedium(int index, int value)
/* 365:    */   {
/* 366:393 */     ensureAccessible();
/* 367:394 */     _setMedium(index, value);
/* 368:395 */     return this;
/* 369:    */   }
/* 370:    */   
/* 371:    */   protected void _setMedium(int index, int value)
/* 372:    */   {
/* 373:400 */     setByte(index, (byte)(value >>> 16));
/* 374:401 */     setByte(index + 1, (byte)(value >>> 8));
/* 375:402 */     setByte(index + 2, (byte)value);
/* 376:    */   }
/* 377:    */   
/* 378:    */   protected void _setMediumLE(int index, int value)
/* 379:    */   {
/* 380:407 */     setByte(index, (byte)value);
/* 381:408 */     setByte(index + 1, (byte)(value >>> 8));
/* 382:409 */     setByte(index + 2, (byte)(value >>> 16));
/* 383:    */   }
/* 384:    */   
/* 385:    */   public ByteBuf setInt(int index, int value)
/* 386:    */   {
/* 387:414 */     ensureAccessible();
/* 388:415 */     _setInt(index, value);
/* 389:416 */     return this;
/* 390:    */   }
/* 391:    */   
/* 392:    */   protected void _setInt(int index, int value)
/* 393:    */   {
/* 394:421 */     this.buffer.putInt(index, value);
/* 395:    */   }
/* 396:    */   
/* 397:    */   protected void _setIntLE(int index, int value)
/* 398:    */   {
/* 399:426 */     this.buffer.putInt(index, ByteBufUtil.swapInt(value));
/* 400:    */   }
/* 401:    */   
/* 402:    */   public ByteBuf setLong(int index, long value)
/* 403:    */   {
/* 404:431 */     ensureAccessible();
/* 405:432 */     _setLong(index, value);
/* 406:433 */     return this;
/* 407:    */   }
/* 408:    */   
/* 409:    */   protected void _setLong(int index, long value)
/* 410:    */   {
/* 411:438 */     this.buffer.putLong(index, value);
/* 412:    */   }
/* 413:    */   
/* 414:    */   protected void _setLongLE(int index, long value)
/* 415:    */   {
/* 416:443 */     this.buffer.putLong(index, ByteBufUtil.swapLong(value));
/* 417:    */   }
/* 418:    */   
/* 419:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 420:    */   {
/* 421:448 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 422:449 */     if (src.nioBufferCount() > 0) {
/* 423:450 */       for (ByteBuffer bb : src.nioBuffers(srcIndex, length))
/* 424:    */       {
/* 425:451 */         int bbLen = bb.remaining();
/* 426:452 */         setBytes(index, bb);
/* 427:453 */         index += bbLen;
/* 428:    */       }
/* 429:    */     } else {
/* 430:456 */       src.getBytes(srcIndex, this, index, length);
/* 431:    */     }
/* 432:458 */     return this;
/* 433:    */   }
/* 434:    */   
/* 435:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 436:    */   {
/* 437:463 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 438:464 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 439:465 */     tmpBuf.clear().position(index).limit(index + length);
/* 440:466 */     tmpBuf.put(src, srcIndex, length);
/* 441:467 */     return this;
/* 442:    */   }
/* 443:    */   
/* 444:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 445:    */   {
/* 446:472 */     ensureAccessible();
/* 447:473 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 448:474 */     if (src == tmpBuf) {
/* 449:475 */       src = src.duplicate();
/* 450:    */     }
/* 451:478 */     tmpBuf.clear().position(index).limit(index + src.remaining());
/* 452:479 */     tmpBuf.put(src);
/* 453:480 */     return this;
/* 454:    */   }
/* 455:    */   
/* 456:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 457:    */     throws IOException
/* 458:    */   {
/* 459:485 */     getBytes(index, out, length, false);
/* 460:486 */     return this;
/* 461:    */   }
/* 462:    */   
/* 463:    */   private void getBytes(int index, OutputStream out, int length, boolean internal)
/* 464:    */     throws IOException
/* 465:    */   {
/* 466:490 */     ensureAccessible();
/* 467:491 */     if (length == 0) {
/* 468:492 */       return;
/* 469:    */     }
/* 470:495 */     if (this.buffer.hasArray())
/* 471:    */     {
/* 472:496 */       out.write(this.buffer.array(), index + this.buffer.arrayOffset(), length);
/* 473:    */     }
/* 474:    */     else
/* 475:    */     {
/* 476:498 */       byte[] tmp = new byte[length];
/* 477:    */       ByteBuffer tmpBuf;
/* 478:    */       ByteBuffer tmpBuf;
/* 479:500 */       if (internal) {
/* 480:501 */         tmpBuf = internalNioBuffer();
/* 481:    */       } else {
/* 482:503 */         tmpBuf = this.buffer.duplicate();
/* 483:    */       }
/* 484:505 */       tmpBuf.clear().position(index);
/* 485:506 */       tmpBuf.get(tmp);
/* 486:507 */       out.write(tmp);
/* 487:    */     }
/* 488:    */   }
/* 489:    */   
/* 490:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 491:    */     throws IOException
/* 492:    */   {
/* 493:513 */     checkReadableBytes(length);
/* 494:514 */     getBytes(this.readerIndex, out, length, true);
/* 495:515 */     this.readerIndex += length;
/* 496:516 */     return this;
/* 497:    */   }
/* 498:    */   
/* 499:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 500:    */     throws IOException
/* 501:    */   {
/* 502:521 */     return getBytes(index, out, length, false);
/* 503:    */   }
/* 504:    */   
/* 505:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 506:    */     throws IOException
/* 507:    */   {
/* 508:525 */     ensureAccessible();
/* 509:526 */     if (length == 0) {
/* 510:527 */       return 0;
/* 511:    */     }
/* 512:    */     ByteBuffer tmpBuf;
/* 513:    */     ByteBuffer tmpBuf;
/* 514:531 */     if (internal) {
/* 515:532 */       tmpBuf = internalNioBuffer();
/* 516:    */     } else {
/* 517:534 */       tmpBuf = this.buffer.duplicate();
/* 518:    */     }
/* 519:536 */     tmpBuf.clear().position(index).limit(index + length);
/* 520:537 */     return out.write(tmpBuf);
/* 521:    */   }
/* 522:    */   
/* 523:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 524:    */     throws IOException
/* 525:    */   {
/* 526:542 */     return getBytes(index, out, position, length, false);
/* 527:    */   }
/* 528:    */   
/* 529:    */   private int getBytes(int index, FileChannel out, long position, int length, boolean internal)
/* 530:    */     throws IOException
/* 531:    */   {
/* 532:546 */     ensureAccessible();
/* 533:547 */     if (length == 0) {
/* 534:548 */       return 0;
/* 535:    */     }
/* 536:551 */     ByteBuffer tmpBuf = internal ? internalNioBuffer() : this.buffer.duplicate();
/* 537:552 */     tmpBuf.clear().position(index).limit(index + length);
/* 538:553 */     return out.write(tmpBuf, position);
/* 539:    */   }
/* 540:    */   
/* 541:    */   public int readBytes(GatheringByteChannel out, int length)
/* 542:    */     throws IOException
/* 543:    */   {
/* 544:558 */     checkReadableBytes(length);
/* 545:559 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 546:560 */     this.readerIndex += readBytes;
/* 547:561 */     return readBytes;
/* 548:    */   }
/* 549:    */   
/* 550:    */   public int readBytes(FileChannel out, long position, int length)
/* 551:    */     throws IOException
/* 552:    */   {
/* 553:566 */     checkReadableBytes(length);
/* 554:567 */     int readBytes = getBytes(this.readerIndex, out, position, length, true);
/* 555:568 */     this.readerIndex += readBytes;
/* 556:569 */     return readBytes;
/* 557:    */   }
/* 558:    */   
/* 559:    */   public int setBytes(int index, InputStream in, int length)
/* 560:    */     throws IOException
/* 561:    */   {
/* 562:574 */     ensureAccessible();
/* 563:575 */     if (this.buffer.hasArray()) {
/* 564:576 */       return in.read(this.buffer.array(), this.buffer.arrayOffset() + index, length);
/* 565:    */     }
/* 566:578 */     byte[] tmp = new byte[length];
/* 567:579 */     int readBytes = in.read(tmp);
/* 568:580 */     if (readBytes <= 0) {
/* 569:581 */       return readBytes;
/* 570:    */     }
/* 571:583 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 572:584 */     tmpBuf.clear().position(index);
/* 573:585 */     tmpBuf.put(tmp, 0, readBytes);
/* 574:586 */     return readBytes;
/* 575:    */   }
/* 576:    */   
/* 577:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 578:    */     throws IOException
/* 579:    */   {
/* 580:592 */     ensureAccessible();
/* 581:593 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 582:594 */     tmpBuf.clear().position(index).limit(index + length);
/* 583:    */     try
/* 584:    */     {
/* 585:596 */       return in.read(this.tmpNioBuf);
/* 586:    */     }
/* 587:    */     catch (ClosedChannelException ignored) {}
/* 588:598 */     return -1;
/* 589:    */   }
/* 590:    */   
/* 591:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 592:    */     throws IOException
/* 593:    */   {
/* 594:604 */     ensureAccessible();
/* 595:605 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 596:606 */     tmpBuf.clear().position(index).limit(index + length);
/* 597:    */     try
/* 598:    */     {
/* 599:608 */       return in.read(this.tmpNioBuf, position);
/* 600:    */     }
/* 601:    */     catch (ClosedChannelException ignored) {}
/* 602:610 */     return -1;
/* 603:    */   }
/* 604:    */   
/* 605:    */   public int nioBufferCount()
/* 606:    */   {
/* 607:616 */     return 1;
/* 608:    */   }
/* 609:    */   
/* 610:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 611:    */   {
/* 612:621 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 613:    */   }
/* 614:    */   
/* 615:    */   public ByteBuf copy(int index, int length)
/* 616:    */   {
/* 617:626 */     ensureAccessible();
/* 618:    */     try
/* 619:    */     {
/* 620:629 */       src = (ByteBuffer)this.buffer.duplicate().clear().position(index).limit(index + length);
/* 621:    */     }
/* 622:    */     catch (IllegalArgumentException ignored)
/* 623:    */     {
/* 624:    */       ByteBuffer src;
/* 625:631 */       throw new IndexOutOfBoundsException("Too many bytes to read - Need " + (index + length));
/* 626:    */     }
/* 627:    */     ByteBuffer src;
/* 628:634 */     return alloc().directBuffer(length, maxCapacity()).writeBytes(src);
/* 629:    */   }
/* 630:    */   
/* 631:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 632:    */   {
/* 633:639 */     checkIndex(index, length);
/* 634:640 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 635:    */   }
/* 636:    */   
/* 637:    */   private ByteBuffer internalNioBuffer()
/* 638:    */   {
/* 639:644 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 640:645 */     if (tmpNioBuf == null) {
/* 641:646 */       this.tmpNioBuf = (tmpNioBuf = this.buffer.duplicate());
/* 642:    */     }
/* 643:648 */     return tmpNioBuf;
/* 644:    */   }
/* 645:    */   
/* 646:    */   public ByteBuffer nioBuffer(int index, int length)
/* 647:    */   {
/* 648:653 */     checkIndex(index, length);
/* 649:654 */     return ((ByteBuffer)this.buffer.duplicate().position(index).limit(index + length)).slice();
/* 650:    */   }
/* 651:    */   
/* 652:    */   protected void deallocate()
/* 653:    */   {
/* 654:659 */     ByteBuffer buffer = this.buffer;
/* 655:660 */     if (buffer == null) {
/* 656:661 */       return;
/* 657:    */     }
/* 658:664 */     this.buffer = null;
/* 659:666 */     if (!this.doNotFree) {
/* 660:667 */       freeDirect(buffer);
/* 661:    */     }
/* 662:    */   }
/* 663:    */   
/* 664:    */   public ByteBuf unwrap()
/* 665:    */   {
/* 666:673 */     return null;
/* 667:    */   }
/* 668:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */