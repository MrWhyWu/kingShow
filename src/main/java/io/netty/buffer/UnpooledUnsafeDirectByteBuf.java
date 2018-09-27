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
/*  15:    */ public class UnpooledUnsafeDirectByteBuf
/*  16:    */   extends AbstractReferenceCountedByteBuf
/*  17:    */ {
/*  18:    */   private final ByteBufAllocator alloc;
/*  19:    */   private ByteBuffer tmpNioBuf;
/*  20:    */   private int capacity;
/*  21:    */   private boolean doNotFree;
/*  22:    */   ByteBuffer buffer;
/*  23:    */   long memoryAddress;
/*  24:    */   
/*  25:    */   public UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  26:    */   {
/*  27: 52 */     super(maxCapacity);
/*  28: 53 */     if (alloc == null) {
/*  29: 54 */       throw new NullPointerException("alloc");
/*  30:    */     }
/*  31: 56 */     if (initialCapacity < 0) {
/*  32: 57 */       throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
/*  33:    */     }
/*  34: 59 */     if (maxCapacity < 0) {
/*  35: 60 */       throw new IllegalArgumentException("maxCapacity: " + maxCapacity);
/*  36:    */     }
/*  37: 62 */     if (initialCapacity > maxCapacity) {
/*  38: 63 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] {
/*  39: 64 */         Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  40:    */     }
/*  41: 67 */     this.alloc = alloc;
/*  42: 68 */     setByteBuffer(allocateDirect(initialCapacity), false);
/*  43:    */   }
/*  44:    */   
/*  45:    */   protected UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity)
/*  46:    */   {
/*  47: 86 */     this(alloc, initialBuffer.slice(), maxCapacity, false);
/*  48:    */   }
/*  49:    */   
/*  50:    */   UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity, boolean doFree)
/*  51:    */   {
/*  52: 90 */     super(maxCapacity);
/*  53: 91 */     if (alloc == null) {
/*  54: 92 */       throw new NullPointerException("alloc");
/*  55:    */     }
/*  56: 94 */     if (initialBuffer == null) {
/*  57: 95 */       throw new NullPointerException("initialBuffer");
/*  58:    */     }
/*  59: 97 */     if (!initialBuffer.isDirect()) {
/*  60: 98 */       throw new IllegalArgumentException("initialBuffer is not a direct buffer.");
/*  61:    */     }
/*  62:100 */     if (initialBuffer.isReadOnly()) {
/*  63:101 */       throw new IllegalArgumentException("initialBuffer is a read-only buffer.");
/*  64:    */     }
/*  65:104 */     int initialCapacity = initialBuffer.remaining();
/*  66:105 */     if (initialCapacity > maxCapacity) {
/*  67:106 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] {
/*  68:107 */         Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  69:    */     }
/*  70:110 */     this.alloc = alloc;
/*  71:111 */     this.doNotFree = (!doFree);
/*  72:112 */     setByteBuffer(initialBuffer.order(ByteOrder.BIG_ENDIAN), false);
/*  73:113 */     writerIndex(initialCapacity);
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected ByteBuffer allocateDirect(int initialCapacity)
/*  77:    */   {
/*  78:120 */     return ByteBuffer.allocateDirect(initialCapacity);
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected void freeDirect(ByteBuffer buffer)
/*  82:    */   {
/*  83:127 */     PlatformDependent.freeDirectBuffer(buffer);
/*  84:    */   }
/*  85:    */   
/*  86:    */   final void setByteBuffer(ByteBuffer buffer, boolean tryFree)
/*  87:    */   {
/*  88:131 */     if (tryFree)
/*  89:    */     {
/*  90:132 */       ByteBuffer oldBuffer = this.buffer;
/*  91:133 */       if (oldBuffer != null) {
/*  92:134 */         if (this.doNotFree) {
/*  93:135 */           this.doNotFree = false;
/*  94:    */         } else {
/*  95:137 */           freeDirect(oldBuffer);
/*  96:    */         }
/*  97:    */       }
/*  98:    */     }
/*  99:141 */     this.buffer = buffer;
/* 100:142 */     this.memoryAddress = PlatformDependent.directBufferAddress(buffer);
/* 101:143 */     this.tmpNioBuf = null;
/* 102:144 */     this.capacity = buffer.remaining();
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isDirect()
/* 106:    */   {
/* 107:149 */     return true;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public int capacity()
/* 111:    */   {
/* 112:154 */     return this.capacity;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public ByteBuf capacity(int newCapacity)
/* 116:    */   {
/* 117:159 */     checkNewCapacity(newCapacity);
/* 118:    */     
/* 119:161 */     int readerIndex = readerIndex();
/* 120:162 */     int writerIndex = writerIndex();
/* 121:    */     
/* 122:164 */     int oldCapacity = this.capacity;
/* 123:165 */     if (newCapacity > oldCapacity)
/* 124:    */     {
/* 125:166 */       ByteBuffer oldBuffer = this.buffer;
/* 126:167 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 127:168 */       oldBuffer.position(0).limit(oldBuffer.capacity());
/* 128:169 */       newBuffer.position(0).limit(oldBuffer.capacity());
/* 129:170 */       newBuffer.put(oldBuffer);
/* 130:171 */       newBuffer.clear();
/* 131:172 */       setByteBuffer(newBuffer, true);
/* 132:    */     }
/* 133:173 */     else if (newCapacity < oldCapacity)
/* 134:    */     {
/* 135:174 */       ByteBuffer oldBuffer = this.buffer;
/* 136:175 */       ByteBuffer newBuffer = allocateDirect(newCapacity);
/* 137:176 */       if (readerIndex < newCapacity)
/* 138:    */       {
/* 139:177 */         if (writerIndex > newCapacity) {
/* 140:178 */           writerIndex(writerIndex = newCapacity);
/* 141:    */         }
/* 142:180 */         oldBuffer.position(readerIndex).limit(writerIndex);
/* 143:181 */         newBuffer.position(readerIndex).limit(writerIndex);
/* 144:182 */         newBuffer.put(oldBuffer);
/* 145:183 */         newBuffer.clear();
/* 146:    */       }
/* 147:    */       else
/* 148:    */       {
/* 149:185 */         setIndex(newCapacity, newCapacity);
/* 150:    */       }
/* 151:187 */       setByteBuffer(newBuffer, true);
/* 152:    */     }
/* 153:189 */     return this;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public ByteBufAllocator alloc()
/* 157:    */   {
/* 158:194 */     return this.alloc;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public ByteOrder order()
/* 162:    */   {
/* 163:199 */     return ByteOrder.BIG_ENDIAN;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public boolean hasArray()
/* 167:    */   {
/* 168:204 */     return false;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public byte[] array()
/* 172:    */   {
/* 173:209 */     throw new UnsupportedOperationException("direct buffer");
/* 174:    */   }
/* 175:    */   
/* 176:    */   public int arrayOffset()
/* 177:    */   {
/* 178:214 */     throw new UnsupportedOperationException("direct buffer");
/* 179:    */   }
/* 180:    */   
/* 181:    */   public boolean hasMemoryAddress()
/* 182:    */   {
/* 183:219 */     return true;
/* 184:    */   }
/* 185:    */   
/* 186:    */   public long memoryAddress()
/* 187:    */   {
/* 188:224 */     ensureAccessible();
/* 189:225 */     return this.memoryAddress;
/* 190:    */   }
/* 191:    */   
/* 192:    */   protected byte _getByte(int index)
/* 193:    */   {
/* 194:230 */     return UnsafeByteBufUtil.getByte(addr(index));
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected short _getShort(int index)
/* 198:    */   {
/* 199:235 */     return UnsafeByteBufUtil.getShort(addr(index));
/* 200:    */   }
/* 201:    */   
/* 202:    */   protected short _getShortLE(int index)
/* 203:    */   {
/* 204:240 */     return UnsafeByteBufUtil.getShortLE(addr(index));
/* 205:    */   }
/* 206:    */   
/* 207:    */   protected int _getUnsignedMedium(int index)
/* 208:    */   {
/* 209:245 */     return UnsafeByteBufUtil.getUnsignedMedium(addr(index));
/* 210:    */   }
/* 211:    */   
/* 212:    */   protected int _getUnsignedMediumLE(int index)
/* 213:    */   {
/* 214:250 */     return UnsafeByteBufUtil.getUnsignedMediumLE(addr(index));
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected int _getInt(int index)
/* 218:    */   {
/* 219:255 */     return UnsafeByteBufUtil.getInt(addr(index));
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected int _getIntLE(int index)
/* 223:    */   {
/* 224:260 */     return UnsafeByteBufUtil.getIntLE(addr(index));
/* 225:    */   }
/* 226:    */   
/* 227:    */   protected long _getLong(int index)
/* 228:    */   {
/* 229:265 */     return UnsafeByteBufUtil.getLong(addr(index));
/* 230:    */   }
/* 231:    */   
/* 232:    */   protected long _getLongLE(int index)
/* 233:    */   {
/* 234:270 */     return UnsafeByteBufUtil.getLongLE(addr(index));
/* 235:    */   }
/* 236:    */   
/* 237:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 238:    */   {
/* 239:275 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, dst, dstIndex, length);
/* 240:276 */     return this;
/* 241:    */   }
/* 242:    */   
/* 243:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 244:    */   {
/* 245:281 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, dst, dstIndex, length);
/* 246:282 */     return this;
/* 247:    */   }
/* 248:    */   
/* 249:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 250:    */   {
/* 251:287 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, dst);
/* 252:288 */     return this;
/* 253:    */   }
/* 254:    */   
/* 255:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 256:    */   {
/* 257:293 */     int length = dst.remaining();
/* 258:294 */     checkReadableBytes(length);
/* 259:295 */     getBytes(this.readerIndex, dst);
/* 260:296 */     this.readerIndex += length;
/* 261:297 */     return this;
/* 262:    */   }
/* 263:    */   
/* 264:    */   protected void _setByte(int index, int value)
/* 265:    */   {
/* 266:302 */     UnsafeByteBufUtil.setByte(addr(index), value);
/* 267:    */   }
/* 268:    */   
/* 269:    */   protected void _setShort(int index, int value)
/* 270:    */   {
/* 271:307 */     UnsafeByteBufUtil.setShort(addr(index), value);
/* 272:    */   }
/* 273:    */   
/* 274:    */   protected void _setShortLE(int index, int value)
/* 275:    */   {
/* 276:312 */     UnsafeByteBufUtil.setShortLE(addr(index), value);
/* 277:    */   }
/* 278:    */   
/* 279:    */   protected void _setMedium(int index, int value)
/* 280:    */   {
/* 281:317 */     UnsafeByteBufUtil.setMedium(addr(index), value);
/* 282:    */   }
/* 283:    */   
/* 284:    */   protected void _setMediumLE(int index, int value)
/* 285:    */   {
/* 286:322 */     UnsafeByteBufUtil.setMediumLE(addr(index), value);
/* 287:    */   }
/* 288:    */   
/* 289:    */   protected void _setInt(int index, int value)
/* 290:    */   {
/* 291:327 */     UnsafeByteBufUtil.setInt(addr(index), value);
/* 292:    */   }
/* 293:    */   
/* 294:    */   protected void _setIntLE(int index, int value)
/* 295:    */   {
/* 296:332 */     UnsafeByteBufUtil.setIntLE(addr(index), value);
/* 297:    */   }
/* 298:    */   
/* 299:    */   protected void _setLong(int index, long value)
/* 300:    */   {
/* 301:337 */     UnsafeByteBufUtil.setLong(addr(index), value);
/* 302:    */   }
/* 303:    */   
/* 304:    */   protected void _setLongLE(int index, long value)
/* 305:    */   {
/* 306:342 */     UnsafeByteBufUtil.setLongLE(addr(index), value);
/* 307:    */   }
/* 308:    */   
/* 309:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 310:    */   {
/* 311:347 */     UnsafeByteBufUtil.setBytes(this, addr(index), index, src, srcIndex, length);
/* 312:348 */     return this;
/* 313:    */   }
/* 314:    */   
/* 315:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 316:    */   {
/* 317:353 */     UnsafeByteBufUtil.setBytes(this, addr(index), index, src, srcIndex, length);
/* 318:354 */     return this;
/* 319:    */   }
/* 320:    */   
/* 321:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 322:    */   {
/* 323:359 */     UnsafeByteBufUtil.setBytes(this, addr(index), index, src);
/* 324:360 */     return this;
/* 325:    */   }
/* 326:    */   
/* 327:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 328:    */     throws IOException
/* 329:    */   {
/* 330:365 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, out, length);
/* 331:366 */     return this;
/* 332:    */   }
/* 333:    */   
/* 334:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 335:    */     throws IOException
/* 336:    */   {
/* 337:371 */     return getBytes(index, out, length, false);
/* 338:    */   }
/* 339:    */   
/* 340:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 341:    */     throws IOException
/* 342:    */   {
/* 343:375 */     ensureAccessible();
/* 344:376 */     if (length == 0) {
/* 345:377 */       return 0;
/* 346:    */     }
/* 347:    */     ByteBuffer tmpBuf;
/* 348:    */     ByteBuffer tmpBuf;
/* 349:381 */     if (internal) {
/* 350:382 */       tmpBuf = internalNioBuffer();
/* 351:    */     } else {
/* 352:384 */       tmpBuf = this.buffer.duplicate();
/* 353:    */     }
/* 354:386 */     tmpBuf.clear().position(index).limit(index + length);
/* 355:387 */     return out.write(tmpBuf);
/* 356:    */   }
/* 357:    */   
/* 358:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 359:    */     throws IOException
/* 360:    */   {
/* 361:392 */     return getBytes(index, out, position, length, false);
/* 362:    */   }
/* 363:    */   
/* 364:    */   private int getBytes(int index, FileChannel out, long position, int length, boolean internal)
/* 365:    */     throws IOException
/* 366:    */   {
/* 367:396 */     ensureAccessible();
/* 368:397 */     if (length == 0) {
/* 369:398 */       return 0;
/* 370:    */     }
/* 371:401 */     ByteBuffer tmpBuf = internal ? internalNioBuffer() : this.buffer.duplicate();
/* 372:402 */     tmpBuf.clear().position(index).limit(index + length);
/* 373:403 */     return out.write(tmpBuf, position);
/* 374:    */   }
/* 375:    */   
/* 376:    */   public int readBytes(GatheringByteChannel out, int length)
/* 377:    */     throws IOException
/* 378:    */   {
/* 379:408 */     checkReadableBytes(length);
/* 380:409 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 381:410 */     this.readerIndex += readBytes;
/* 382:411 */     return readBytes;
/* 383:    */   }
/* 384:    */   
/* 385:    */   public int readBytes(FileChannel out, long position, int length)
/* 386:    */     throws IOException
/* 387:    */   {
/* 388:416 */     checkReadableBytes(length);
/* 389:417 */     int readBytes = getBytes(this.readerIndex, out, position, length, true);
/* 390:418 */     this.readerIndex += readBytes;
/* 391:419 */     return readBytes;
/* 392:    */   }
/* 393:    */   
/* 394:    */   public int setBytes(int index, InputStream in, int length)
/* 395:    */     throws IOException
/* 396:    */   {
/* 397:424 */     return UnsafeByteBufUtil.setBytes(this, addr(index), index, in, length);
/* 398:    */   }
/* 399:    */   
/* 400:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 401:    */     throws IOException
/* 402:    */   {
/* 403:429 */     ensureAccessible();
/* 404:430 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 405:431 */     tmpBuf.clear().position(index).limit(index + length);
/* 406:    */     try
/* 407:    */     {
/* 408:433 */       return in.read(tmpBuf);
/* 409:    */     }
/* 410:    */     catch (ClosedChannelException ignored) {}
/* 411:435 */     return -1;
/* 412:    */   }
/* 413:    */   
/* 414:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 415:    */     throws IOException
/* 416:    */   {
/* 417:441 */     ensureAccessible();
/* 418:442 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 419:443 */     tmpBuf.clear().position(index).limit(index + length);
/* 420:    */     try
/* 421:    */     {
/* 422:445 */       return in.read(tmpBuf, position);
/* 423:    */     }
/* 424:    */     catch (ClosedChannelException ignored) {}
/* 425:447 */     return -1;
/* 426:    */   }
/* 427:    */   
/* 428:    */   public int nioBufferCount()
/* 429:    */   {
/* 430:453 */     return 1;
/* 431:    */   }
/* 432:    */   
/* 433:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 434:    */   {
/* 435:458 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 436:    */   }
/* 437:    */   
/* 438:    */   public ByteBuf copy(int index, int length)
/* 439:    */   {
/* 440:463 */     return UnsafeByteBufUtil.copy(this, addr(index), index, length);
/* 441:    */   }
/* 442:    */   
/* 443:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 444:    */   {
/* 445:468 */     checkIndex(index, length);
/* 446:469 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 447:    */   }
/* 448:    */   
/* 449:    */   private ByteBuffer internalNioBuffer()
/* 450:    */   {
/* 451:473 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 452:474 */     if (tmpNioBuf == null) {
/* 453:475 */       this.tmpNioBuf = (tmpNioBuf = this.buffer.duplicate());
/* 454:    */     }
/* 455:477 */     return tmpNioBuf;
/* 456:    */   }
/* 457:    */   
/* 458:    */   public ByteBuffer nioBuffer(int index, int length)
/* 459:    */   {
/* 460:482 */     checkIndex(index, length);
/* 461:483 */     return ((ByteBuffer)this.buffer.duplicate().position(index).limit(index + length)).slice();
/* 462:    */   }
/* 463:    */   
/* 464:    */   protected void deallocate()
/* 465:    */   {
/* 466:488 */     ByteBuffer buffer = this.buffer;
/* 467:489 */     if (buffer == null) {
/* 468:490 */       return;
/* 469:    */     }
/* 470:493 */     this.buffer = null;
/* 471:495 */     if (!this.doNotFree) {
/* 472:496 */       freeDirect(buffer);
/* 473:    */     }
/* 474:    */   }
/* 475:    */   
/* 476:    */   public ByteBuf unwrap()
/* 477:    */   {
/* 478:502 */     return null;
/* 479:    */   }
/* 480:    */   
/* 481:    */   long addr(int index)
/* 482:    */   {
/* 483:506 */     return this.memoryAddress + index;
/* 484:    */   }
/* 485:    */   
/* 486:    */   protected SwappedByteBuf newSwappedByteBuf()
/* 487:    */   {
/* 488:511 */     if (PlatformDependent.isUnaligned()) {
/* 489:513 */       return new UnsafeDirectSwappedByteBuf(this);
/* 490:    */     }
/* 491:515 */     return super.newSwappedByteBuf();
/* 492:    */   }
/* 493:    */   
/* 494:    */   public ByteBuf setZero(int index, int length)
/* 495:    */   {
/* 496:520 */     checkIndex(index, length);
/* 497:521 */     UnsafeByteBufUtil.setZero(addr(index), length);
/* 498:522 */     return this;
/* 499:    */   }
/* 500:    */   
/* 501:    */   public ByteBuf writeZero(int length)
/* 502:    */   {
/* 503:527 */     ensureWritable(length);
/* 504:528 */     int wIndex = this.writerIndex;
/* 505:529 */     UnsafeByteBufUtil.setZero(addr(wIndex), length);
/* 506:530 */     this.writerIndex = (wIndex + length);
/* 507:531 */     return this;
/* 508:    */   }
/* 509:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */