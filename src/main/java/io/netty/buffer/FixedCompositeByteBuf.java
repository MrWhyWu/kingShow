/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.EmptyArrays;
/*   4:    */ import io.netty.util.internal.RecyclableArrayList;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.ReadOnlyBufferException;
/*  11:    */ import java.nio.channels.FileChannel;
/*  12:    */ import java.nio.channels.GatheringByteChannel;
/*  13:    */ import java.nio.channels.ScatteringByteChannel;
/*  14:    */ import java.util.Collections;
/*  15:    */ 
/*  16:    */ final class FixedCompositeByteBuf
/*  17:    */   extends AbstractReferenceCountedByteBuf
/*  18:    */ {
/*  19: 37 */   private static final ByteBuf[] EMPTY = { Unpooled.EMPTY_BUFFER };
/*  20:    */   private final int nioBufferCount;
/*  21:    */   private final int capacity;
/*  22:    */   private final ByteBufAllocator allocator;
/*  23:    */   private final ByteOrder order;
/*  24:    */   private final Object[] buffers;
/*  25:    */   private final boolean direct;
/*  26:    */   
/*  27:    */   FixedCompositeByteBuf(ByteBufAllocator allocator, ByteBuf... buffers)
/*  28:    */   {
/*  29: 46 */     super(2147483647);
/*  30: 47 */     if (buffers.length == 0)
/*  31:    */     {
/*  32: 48 */       this.buffers = EMPTY;
/*  33: 49 */       this.order = ByteOrder.BIG_ENDIAN;
/*  34: 50 */       this.nioBufferCount = 1;
/*  35: 51 */       this.capacity = 0;
/*  36: 52 */       this.direct = false;
/*  37:    */     }
/*  38:    */     else
/*  39:    */     {
/*  40: 54 */       ByteBuf b = buffers[0];
/*  41: 55 */       this.buffers = new Object[buffers.length];
/*  42: 56 */       this.buffers[0] = b;
/*  43: 57 */       boolean direct = true;
/*  44: 58 */       int nioBufferCount = b.nioBufferCount();
/*  45: 59 */       int capacity = b.readableBytes();
/*  46: 60 */       this.order = b.order();
/*  47: 61 */       for (int i = 1; i < buffers.length; i++)
/*  48:    */       {
/*  49: 62 */         b = buffers[i];
/*  50: 63 */         if (buffers[i].order() != this.order) {
/*  51: 64 */           throw new IllegalArgumentException("All ByteBufs need to have same ByteOrder");
/*  52:    */         }
/*  53: 66 */         nioBufferCount += b.nioBufferCount();
/*  54: 67 */         capacity += b.readableBytes();
/*  55: 68 */         if (!b.isDirect()) {
/*  56: 69 */           direct = false;
/*  57:    */         }
/*  58: 71 */         this.buffers[i] = b;
/*  59:    */       }
/*  60: 73 */       this.nioBufferCount = nioBufferCount;
/*  61: 74 */       this.capacity = capacity;
/*  62: 75 */       this.direct = direct;
/*  63:    */     }
/*  64: 77 */     setIndex(0, capacity());
/*  65: 78 */     this.allocator = allocator;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean isWritable()
/*  69:    */   {
/*  70: 83 */     return false;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isWritable(int size)
/*  74:    */   {
/*  75: 88 */     return false;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public ByteBuf discardReadBytes()
/*  79:    */   {
/*  80: 93 */     throw new ReadOnlyBufferException();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  84:    */   {
/*  85: 98 */     throw new ReadOnlyBufferException();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  89:    */   {
/*  90:103 */     throw new ReadOnlyBufferException();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  94:    */   {
/*  95:108 */     throw new ReadOnlyBufferException();
/*  96:    */   }
/*  97:    */   
/*  98:    */   public ByteBuf setByte(int index, int value)
/*  99:    */   {
/* 100:113 */     throw new ReadOnlyBufferException();
/* 101:    */   }
/* 102:    */   
/* 103:    */   protected void _setByte(int index, int value)
/* 104:    */   {
/* 105:118 */     throw new ReadOnlyBufferException();
/* 106:    */   }
/* 107:    */   
/* 108:    */   public ByteBuf setShort(int index, int value)
/* 109:    */   {
/* 110:123 */     throw new ReadOnlyBufferException();
/* 111:    */   }
/* 112:    */   
/* 113:    */   protected void _setShort(int index, int value)
/* 114:    */   {
/* 115:128 */     throw new ReadOnlyBufferException();
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected void _setShortLE(int index, int value)
/* 119:    */   {
/* 120:133 */     throw new ReadOnlyBufferException();
/* 121:    */   }
/* 122:    */   
/* 123:    */   public ByteBuf setMedium(int index, int value)
/* 124:    */   {
/* 125:138 */     throw new ReadOnlyBufferException();
/* 126:    */   }
/* 127:    */   
/* 128:    */   protected void _setMedium(int index, int value)
/* 129:    */   {
/* 130:143 */     throw new ReadOnlyBufferException();
/* 131:    */   }
/* 132:    */   
/* 133:    */   protected void _setMediumLE(int index, int value)
/* 134:    */   {
/* 135:148 */     throw new ReadOnlyBufferException();
/* 136:    */   }
/* 137:    */   
/* 138:    */   public ByteBuf setInt(int index, int value)
/* 139:    */   {
/* 140:153 */     throw new ReadOnlyBufferException();
/* 141:    */   }
/* 142:    */   
/* 143:    */   protected void _setInt(int index, int value)
/* 144:    */   {
/* 145:158 */     throw new ReadOnlyBufferException();
/* 146:    */   }
/* 147:    */   
/* 148:    */   protected void _setIntLE(int index, int value)
/* 149:    */   {
/* 150:163 */     throw new ReadOnlyBufferException();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public ByteBuf setLong(int index, long value)
/* 154:    */   {
/* 155:168 */     throw new ReadOnlyBufferException();
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected void _setLong(int index, long value)
/* 159:    */   {
/* 160:173 */     throw new ReadOnlyBufferException();
/* 161:    */   }
/* 162:    */   
/* 163:    */   protected void _setLongLE(int index, long value)
/* 164:    */   {
/* 165:178 */     throw new ReadOnlyBufferException();
/* 166:    */   }
/* 167:    */   
/* 168:    */   public int setBytes(int index, InputStream in, int length)
/* 169:    */   {
/* 170:183 */     throw new ReadOnlyBufferException();
/* 171:    */   }
/* 172:    */   
/* 173:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 174:    */   {
/* 175:188 */     throw new ReadOnlyBufferException();
/* 176:    */   }
/* 177:    */   
/* 178:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 179:    */   {
/* 180:193 */     throw new ReadOnlyBufferException();
/* 181:    */   }
/* 182:    */   
/* 183:    */   public int capacity()
/* 184:    */   {
/* 185:198 */     return this.capacity;
/* 186:    */   }
/* 187:    */   
/* 188:    */   public int maxCapacity()
/* 189:    */   {
/* 190:203 */     return this.capacity;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public ByteBuf capacity(int newCapacity)
/* 194:    */   {
/* 195:208 */     throw new ReadOnlyBufferException();
/* 196:    */   }
/* 197:    */   
/* 198:    */   public ByteBufAllocator alloc()
/* 199:    */   {
/* 200:213 */     return this.allocator;
/* 201:    */   }
/* 202:    */   
/* 203:    */   public ByteOrder order()
/* 204:    */   {
/* 205:218 */     return this.order;
/* 206:    */   }
/* 207:    */   
/* 208:    */   public ByteBuf unwrap()
/* 209:    */   {
/* 210:223 */     return null;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public boolean isDirect()
/* 214:    */   {
/* 215:228 */     return this.direct;
/* 216:    */   }
/* 217:    */   
/* 218:    */   private Component findComponent(int index)
/* 219:    */   {
/* 220:232 */     int readable = 0;
/* 221:233 */     for (int i = 0; i < this.buffers.length; i++)
/* 222:    */     {
/* 223:234 */       Component comp = null;
/* 224:    */       
/* 225:236 */       Object obj = this.buffers[i];
/* 226:    */       boolean isBuffer;
/* 227:    */       ByteBuf b;
/* 228:    */       boolean isBuffer;
/* 229:238 */       if ((obj instanceof ByteBuf))
/* 230:    */       {
/* 231:239 */         ByteBuf b = (ByteBuf)obj;
/* 232:240 */         isBuffer = true;
/* 233:    */       }
/* 234:    */       else
/* 235:    */       {
/* 236:242 */         comp = (Component)obj;
/* 237:243 */         b = comp.buf;
/* 238:244 */         isBuffer = false;
/* 239:    */       }
/* 240:246 */       readable += b.readableBytes();
/* 241:247 */       if (index < readable)
/* 242:    */       {
/* 243:248 */         if (isBuffer)
/* 244:    */         {
/* 245:251 */           comp = new Component(i, readable - b.readableBytes(), b);
/* 246:252 */           this.buffers[i] = comp;
/* 247:    */         }
/* 248:254 */         return comp;
/* 249:    */       }
/* 250:    */     }
/* 251:257 */     throw new IllegalStateException();
/* 252:    */   }
/* 253:    */   
/* 254:    */   private ByteBuf buffer(int i)
/* 255:    */   {
/* 256:264 */     Object obj = this.buffers[i];
/* 257:265 */     if ((obj instanceof ByteBuf)) {
/* 258:266 */       return (ByteBuf)obj;
/* 259:    */     }
/* 260:268 */     return ((Component)obj).buf;
/* 261:    */   }
/* 262:    */   
/* 263:    */   public byte getByte(int index)
/* 264:    */   {
/* 265:273 */     return _getByte(index);
/* 266:    */   }
/* 267:    */   
/* 268:    */   protected byte _getByte(int index)
/* 269:    */   {
/* 270:278 */     Component c = findComponent(index);
/* 271:279 */     return c.buf.getByte(index - c.offset);
/* 272:    */   }
/* 273:    */   
/* 274:    */   protected short _getShort(int index)
/* 275:    */   {
/* 276:284 */     Component c = findComponent(index);
/* 277:285 */     if (index + 2 <= c.endOffset) {
/* 278:286 */       return c.buf.getShort(index - c.offset);
/* 279:    */     }
/* 280:287 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 281:288 */       return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF);
/* 282:    */     }
/* 283:290 */     return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8);
/* 284:    */   }
/* 285:    */   
/* 286:    */   protected short _getShortLE(int index)
/* 287:    */   {
/* 288:296 */     Component c = findComponent(index);
/* 289:297 */     if (index + 2 <= c.endOffset) {
/* 290:298 */       return c.buf.getShortLE(index - c.offset);
/* 291:    */     }
/* 292:299 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 293:300 */       return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8);
/* 294:    */     }
/* 295:302 */     return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF);
/* 296:    */   }
/* 297:    */   
/* 298:    */   protected int _getUnsignedMedium(int index)
/* 299:    */   {
/* 300:308 */     Component c = findComponent(index);
/* 301:309 */     if (index + 3 <= c.endOffset) {
/* 302:310 */       return c.buf.getUnsignedMedium(index - c.offset);
/* 303:    */     }
/* 304:311 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 305:312 */       return (_getShort(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF;
/* 306:    */     }
/* 307:314 */     return _getShort(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
/* 308:    */   }
/* 309:    */   
/* 310:    */   protected int _getUnsignedMediumLE(int index)
/* 311:    */   {
/* 312:320 */     Component c = findComponent(index);
/* 313:321 */     if (index + 3 <= c.endOffset) {
/* 314:322 */       return c.buf.getUnsignedMediumLE(index - c.offset);
/* 315:    */     }
/* 316:323 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 317:324 */       return _getShortLE(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
/* 318:    */     }
/* 319:326 */     return (_getShortLE(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF;
/* 320:    */   }
/* 321:    */   
/* 322:    */   protected int _getInt(int index)
/* 323:    */   {
/* 324:332 */     Component c = findComponent(index);
/* 325:333 */     if (index + 4 <= c.endOffset) {
/* 326:334 */       return c.buf.getInt(index - c.offset);
/* 327:    */     }
/* 328:335 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 329:336 */       return (_getShort(index) & 0xFFFF) << 16 | _getShort(index + 2) & 0xFFFF;
/* 330:    */     }
/* 331:338 */     return _getShort(index) & 0xFFFF | (_getShort(index + 2) & 0xFFFF) << 16;
/* 332:    */   }
/* 333:    */   
/* 334:    */   protected int _getIntLE(int index)
/* 335:    */   {
/* 336:344 */     Component c = findComponent(index);
/* 337:345 */     if (index + 4 <= c.endOffset) {
/* 338:346 */       return c.buf.getIntLE(index - c.offset);
/* 339:    */     }
/* 340:347 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 341:348 */       return _getShortLE(index) & 0xFFFF | (_getShortLE(index + 2) & 0xFFFF) << 16;
/* 342:    */     }
/* 343:350 */     return (_getShortLE(index) & 0xFFFF) << 16 | _getShortLE(index + 2) & 0xFFFF;
/* 344:    */   }
/* 345:    */   
/* 346:    */   protected long _getLong(int index)
/* 347:    */   {
/* 348:356 */     Component c = findComponent(index);
/* 349:357 */     if (index + 8 <= c.endOffset) {
/* 350:358 */       return c.buf.getLong(index - c.offset);
/* 351:    */     }
/* 352:359 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 353:360 */       return (_getInt(index) & 0xFFFFFFFF) << 32 | _getInt(index + 4) & 0xFFFFFFFF;
/* 354:    */     }
/* 355:362 */     return _getInt(index) & 0xFFFFFFFF | (_getInt(index + 4) & 0xFFFFFFFF) << 32;
/* 356:    */   }
/* 357:    */   
/* 358:    */   protected long _getLongLE(int index)
/* 359:    */   {
/* 360:368 */     Component c = findComponent(index);
/* 361:369 */     if (index + 8 <= c.endOffset) {
/* 362:370 */       return c.buf.getLongLE(index - c.offset);
/* 363:    */     }
/* 364:371 */     if (order() == ByteOrder.BIG_ENDIAN) {
/* 365:372 */       return _getIntLE(index) & 0xFFFFFFFF | (_getIntLE(index + 4) & 0xFFFFFFFF) << 32;
/* 366:    */     }
/* 367:374 */     return (_getIntLE(index) & 0xFFFFFFFF) << 32 | _getIntLE(index + 4) & 0xFFFFFFFF;
/* 368:    */   }
/* 369:    */   
/* 370:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 371:    */   {
/* 372:380 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 373:381 */     if (length == 0) {
/* 374:382 */       return this;
/* 375:    */     }
/* 376:385 */     Component c = findComponent(index);
/* 377:386 */     int i = c.index;
/* 378:387 */     int adjustment = c.offset;
/* 379:388 */     ByteBuf s = c.buf;
/* 380:    */     for (;;)
/* 381:    */     {
/* 382:390 */       int localLength = Math.min(length, s.readableBytes() - (index - adjustment));
/* 383:391 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/* 384:392 */       index += localLength;
/* 385:393 */       dstIndex += localLength;
/* 386:394 */       length -= localLength;
/* 387:395 */       adjustment += s.readableBytes();
/* 388:396 */       if (length <= 0) {
/* 389:    */         break;
/* 390:    */       }
/* 391:399 */       s = buffer(++i);
/* 392:    */     }
/* 393:401 */     return this;
/* 394:    */   }
/* 395:    */   
/* 396:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 397:    */   {
/* 398:406 */     int limit = dst.limit();
/* 399:407 */     int length = dst.remaining();
/* 400:    */     
/* 401:409 */     checkIndex(index, length);
/* 402:410 */     if (length == 0) {
/* 403:411 */       return this;
/* 404:    */     }
/* 405:    */     try
/* 406:    */     {
/* 407:415 */       Component c = findComponent(index);
/* 408:416 */       int i = c.index;
/* 409:417 */       int adjustment = c.offset;
/* 410:418 */       ByteBuf s = c.buf;
/* 411:    */       for (;;)
/* 412:    */       {
/* 413:420 */         int localLength = Math.min(length, s.readableBytes() - (index - adjustment));
/* 414:421 */         dst.limit(dst.position() + localLength);
/* 415:422 */         s.getBytes(index - adjustment, dst);
/* 416:423 */         index += localLength;
/* 417:424 */         length -= localLength;
/* 418:425 */         adjustment += s.readableBytes();
/* 419:426 */         if (length <= 0) {
/* 420:    */           break;
/* 421:    */         }
/* 422:429 */         s = buffer(++i);
/* 423:    */       }
/* 424:    */     }
/* 425:    */     finally
/* 426:    */     {
/* 427:432 */       dst.limit(limit);
/* 428:    */     }
/* 429:434 */     return this;
/* 430:    */   }
/* 431:    */   
/* 432:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 433:    */   {
/* 434:439 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 435:440 */     if (length == 0) {
/* 436:441 */       return this;
/* 437:    */     }
/* 438:444 */     Component c = findComponent(index);
/* 439:445 */     int i = c.index;
/* 440:446 */     int adjustment = c.offset;
/* 441:447 */     ByteBuf s = c.buf;
/* 442:    */     for (;;)
/* 443:    */     {
/* 444:449 */       int localLength = Math.min(length, s.readableBytes() - (index - adjustment));
/* 445:450 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/* 446:451 */       index += localLength;
/* 447:452 */       dstIndex += localLength;
/* 448:453 */       length -= localLength;
/* 449:454 */       adjustment += s.readableBytes();
/* 450:455 */       if (length <= 0) {
/* 451:    */         break;
/* 452:    */       }
/* 453:458 */       s = buffer(++i);
/* 454:    */     }
/* 455:460 */     return this;
/* 456:    */   }
/* 457:    */   
/* 458:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 459:    */     throws IOException
/* 460:    */   {
/* 461:466 */     int count = nioBufferCount();
/* 462:467 */     if (count == 1) {
/* 463:468 */       return out.write(internalNioBuffer(index, length));
/* 464:    */     }
/* 465:470 */     long writtenBytes = out.write(nioBuffers(index, length));
/* 466:471 */     if (writtenBytes > 2147483647L) {
/* 467:472 */       return 2147483647;
/* 468:    */     }
/* 469:474 */     return (int)writtenBytes;
/* 470:    */   }
/* 471:    */   
/* 472:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 473:    */     throws IOException
/* 474:    */   {
/* 475:482 */     int count = nioBufferCount();
/* 476:483 */     if (count == 1) {
/* 477:484 */       return out.write(internalNioBuffer(index, length), position);
/* 478:    */     }
/* 479:486 */     long writtenBytes = 0L;
/* 480:487 */     for (ByteBuffer buf : nioBuffers(index, length)) {
/* 481:488 */       writtenBytes += out.write(buf, position + writtenBytes);
/* 482:    */     }
/* 483:490 */     if (writtenBytes > 2147483647L) {
/* 484:491 */       return 2147483647;
/* 485:    */     }
/* 486:493 */     return (int)writtenBytes;
/* 487:    */   }
/* 488:    */   
/* 489:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 490:    */     throws IOException
/* 491:    */   {
/* 492:500 */     checkIndex(index, length);
/* 493:501 */     if (length == 0) {
/* 494:502 */       return this;
/* 495:    */     }
/* 496:505 */     Component c = findComponent(index);
/* 497:506 */     int i = c.index;
/* 498:507 */     int adjustment = c.offset;
/* 499:508 */     ByteBuf s = c.buf;
/* 500:    */     for (;;)
/* 501:    */     {
/* 502:510 */       int localLength = Math.min(length, s.readableBytes() - (index - adjustment));
/* 503:511 */       s.getBytes(index - adjustment, out, localLength);
/* 504:512 */       index += localLength;
/* 505:513 */       length -= localLength;
/* 506:514 */       adjustment += s.readableBytes();
/* 507:515 */       if (length <= 0) {
/* 508:    */         break;
/* 509:    */       }
/* 510:518 */       s = buffer(++i);
/* 511:    */     }
/* 512:520 */     return this;
/* 513:    */   }
/* 514:    */   
/* 515:    */   public ByteBuf copy(int index, int length)
/* 516:    */   {
/* 517:525 */     checkIndex(index, length);
/* 518:526 */     boolean release = true;
/* 519:527 */     ByteBuf buf = alloc().buffer(length);
/* 520:    */     try
/* 521:    */     {
/* 522:529 */       buf.writeBytes(this, index, length);
/* 523:530 */       release = false;
/* 524:531 */       return buf;
/* 525:    */     }
/* 526:    */     finally
/* 527:    */     {
/* 528:533 */       if (release) {
/* 529:534 */         buf.release();
/* 530:    */       }
/* 531:    */     }
/* 532:    */   }
/* 533:    */   
/* 534:    */   public int nioBufferCount()
/* 535:    */   {
/* 536:541 */     return this.nioBufferCount;
/* 537:    */   }
/* 538:    */   
/* 539:    */   public ByteBuffer nioBuffer(int index, int length)
/* 540:    */   {
/* 541:546 */     checkIndex(index, length);
/* 542:547 */     if (this.buffers.length == 1)
/* 543:    */     {
/* 544:548 */       ByteBuf buf = buffer(0);
/* 545:549 */       if (buf.nioBufferCount() == 1) {
/* 546:550 */         return buf.nioBuffer(index, length);
/* 547:    */       }
/* 548:    */     }
/* 549:553 */     ByteBuffer merged = ByteBuffer.allocate(length).order(order());
/* 550:554 */     ByteBuffer[] buffers = nioBuffers(index, length);
/* 551:557 */     for (int i = 0; i < buffers.length; i++) {
/* 552:558 */       merged.put(buffers[i]);
/* 553:    */     }
/* 554:561 */     merged.flip();
/* 555:562 */     return merged;
/* 556:    */   }
/* 557:    */   
/* 558:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 559:    */   {
/* 560:567 */     if (this.buffers.length == 1) {
/* 561:568 */       return buffer(0).internalNioBuffer(index, length);
/* 562:    */     }
/* 563:570 */     throw new UnsupportedOperationException();
/* 564:    */   }
/* 565:    */   
/* 566:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 567:    */   {
/* 568:575 */     checkIndex(index, length);
/* 569:576 */     if (length == 0) {
/* 570:577 */       return EmptyArrays.EMPTY_BYTE_BUFFERS;
/* 571:    */     }
/* 572:580 */     RecyclableArrayList array = RecyclableArrayList.newInstance(this.buffers.length);
/* 573:    */     try
/* 574:    */     {
/* 575:582 */       Component c = findComponent(index);
/* 576:583 */       int i = c.index;
/* 577:584 */       int adjustment = c.offset;
/* 578:585 */       ByteBuf s = c.buf;
/* 579:    */       int localLength;
/* 580:    */       for (;;)
/* 581:    */       {
/* 582:587 */         localLength = Math.min(length, s.readableBytes() - (index - adjustment));
/* 583:588 */         switch (s.nioBufferCount())
/* 584:    */         {
/* 585:    */         case 0: 
/* 586:590 */           throw new UnsupportedOperationException();
/* 587:    */         case 1: 
/* 588:592 */           array.add(s.nioBuffer(index - adjustment, localLength));
/* 589:593 */           break;
/* 590:    */         default: 
/* 591:595 */           Collections.addAll(array, s.nioBuffers(index - adjustment, localLength));
/* 592:    */         }
/* 593:598 */         index += localLength;
/* 594:599 */         length -= localLength;
/* 595:600 */         adjustment += s.readableBytes();
/* 596:601 */         if (length <= 0) {
/* 597:    */           break;
/* 598:    */         }
/* 599:604 */         s = buffer(++i);
/* 600:    */       }
/* 601:607 */       return (ByteBuffer[])array.toArray(new ByteBuffer[array.size()]);
/* 602:    */     }
/* 603:    */     finally
/* 604:    */     {
/* 605:609 */       array.recycle();
/* 606:    */     }
/* 607:    */   }
/* 608:    */   
/* 609:    */   public boolean hasArray()
/* 610:    */   {
/* 611:615 */     return false;
/* 612:    */   }
/* 613:    */   
/* 614:    */   public byte[] array()
/* 615:    */   {
/* 616:620 */     throw new UnsupportedOperationException();
/* 617:    */   }
/* 618:    */   
/* 619:    */   public int arrayOffset()
/* 620:    */   {
/* 621:625 */     throw new UnsupportedOperationException();
/* 622:    */   }
/* 623:    */   
/* 624:    */   public boolean hasMemoryAddress()
/* 625:    */   {
/* 626:630 */     return false;
/* 627:    */   }
/* 628:    */   
/* 629:    */   public long memoryAddress()
/* 630:    */   {
/* 631:635 */     throw new UnsupportedOperationException();
/* 632:    */   }
/* 633:    */   
/* 634:    */   protected void deallocate()
/* 635:    */   {
/* 636:640 */     for (int i = 0; i < this.buffers.length; i++) {
/* 637:641 */       buffer(i).release();
/* 638:    */     }
/* 639:    */   }
/* 640:    */   
/* 641:    */   public String toString()
/* 642:    */   {
/* 643:647 */     String result = super.toString();
/* 644:648 */     result = result.substring(0, result.length() - 1);
/* 645:649 */     return result + ", components=" + this.buffers.length + ')';
/* 646:    */   }
/* 647:    */   
/* 648:    */   private static final class Component
/* 649:    */   {
/* 650:    */     private final int index;
/* 651:    */     private final int offset;
/* 652:    */     private final ByteBuf buf;
/* 653:    */     private final int endOffset;
/* 654:    */     
/* 655:    */     Component(int index, int offset, ByteBuf buf)
/* 656:    */     {
/* 657:659 */       this.index = index;
/* 658:660 */       this.offset = offset;
/* 659:661 */       this.endOffset = (offset + buf.readableBytes());
/* 660:662 */       this.buf = buf;
/* 661:    */     }
/* 662:    */   }
/* 663:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.FixedCompositeByteBuf
 * JD-Core Version:    0.7.0.1
 */