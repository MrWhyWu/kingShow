/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.nio.Buffer;
/*   9:    */ import java.nio.ByteBuffer;
/*  10:    */ import java.nio.ByteOrder;
/*  11:    */ import java.nio.channels.ClosedChannelException;
/*  12:    */ import java.nio.channels.FileChannel;
/*  13:    */ import java.nio.channels.GatheringByteChannel;
/*  14:    */ import java.nio.channels.ScatteringByteChannel;
/*  15:    */ 
/*  16:    */ public class UnpooledHeapByteBuf
/*  17:    */   extends AbstractReferenceCountedByteBuf
/*  18:    */ {
/*  19:    */   private final ByteBufAllocator alloc;
/*  20:    */   byte[] array;
/*  21:    */   private ByteBuffer tmpNioBuf;
/*  22:    */   
/*  23:    */   public UnpooledHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  24:    */   {
/*  25: 50 */     super(maxCapacity);
/*  26:    */     
/*  27: 52 */     ObjectUtil.checkNotNull(alloc, "alloc");
/*  28: 54 */     if (initialCapacity > maxCapacity) {
/*  29: 55 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] {
/*  30: 56 */         Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) }));
/*  31:    */     }
/*  32: 59 */     this.alloc = alloc;
/*  33: 60 */     setArray(allocateArray(initialCapacity));
/*  34: 61 */     setIndex(0, 0);
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected UnpooledHeapByteBuf(ByteBufAllocator alloc, byte[] initialArray, int maxCapacity)
/*  38:    */   {
/*  39: 71 */     super(maxCapacity);
/*  40:    */     
/*  41: 73 */     ObjectUtil.checkNotNull(alloc, "alloc");
/*  42: 74 */     ObjectUtil.checkNotNull(initialArray, "initialArray");
/*  43: 76 */     if (initialArray.length > maxCapacity) {
/*  44: 77 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] {
/*  45: 78 */         Integer.valueOf(initialArray.length), Integer.valueOf(maxCapacity) }));
/*  46:    */     }
/*  47: 81 */     this.alloc = alloc;
/*  48: 82 */     setArray(initialArray);
/*  49: 83 */     setIndex(0, initialArray.length);
/*  50:    */   }
/*  51:    */   
/*  52:    */   byte[] allocateArray(int initialCapacity)
/*  53:    */   {
/*  54: 87 */     return new byte[initialCapacity];
/*  55:    */   }
/*  56:    */   
/*  57:    */   void freeArray(byte[] array) {}
/*  58:    */   
/*  59:    */   private void setArray(byte[] initialArray)
/*  60:    */   {
/*  61: 95 */     this.array = initialArray;
/*  62: 96 */     this.tmpNioBuf = null;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public ByteBufAllocator alloc()
/*  66:    */   {
/*  67:101 */     return this.alloc;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public ByteOrder order()
/*  71:    */   {
/*  72:106 */     return ByteOrder.BIG_ENDIAN;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public boolean isDirect()
/*  76:    */   {
/*  77:111 */     return false;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public int capacity()
/*  81:    */   {
/*  82:116 */     ensureAccessible();
/*  83:117 */     return this.array.length;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public ByteBuf capacity(int newCapacity)
/*  87:    */   {
/*  88:122 */     checkNewCapacity(newCapacity);
/*  89:    */     
/*  90:124 */     int oldCapacity = this.array.length;
/*  91:125 */     byte[] oldArray = this.array;
/*  92:126 */     if (newCapacity > oldCapacity)
/*  93:    */     {
/*  94:127 */       byte[] newArray = allocateArray(newCapacity);
/*  95:128 */       System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
/*  96:129 */       setArray(newArray);
/*  97:130 */       freeArray(oldArray);
/*  98:    */     }
/*  99:131 */     else if (newCapacity < oldCapacity)
/* 100:    */     {
/* 101:132 */       byte[] newArray = allocateArray(newCapacity);
/* 102:133 */       int readerIndex = readerIndex();
/* 103:134 */       if (readerIndex < newCapacity)
/* 104:    */       {
/* 105:135 */         int writerIndex = writerIndex();
/* 106:136 */         if (writerIndex > newCapacity) {
/* 107:137 */           writerIndex(writerIndex = newCapacity);
/* 108:    */         }
/* 109:139 */         System.arraycopy(oldArray, readerIndex, newArray, readerIndex, writerIndex - readerIndex);
/* 110:    */       }
/* 111:    */       else
/* 112:    */       {
/* 113:141 */         setIndex(newCapacity, newCapacity);
/* 114:    */       }
/* 115:143 */       setArray(newArray);
/* 116:144 */       freeArray(oldArray);
/* 117:    */     }
/* 118:146 */     return this;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public boolean hasArray()
/* 122:    */   {
/* 123:151 */     return true;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public byte[] array()
/* 127:    */   {
/* 128:156 */     ensureAccessible();
/* 129:157 */     return this.array;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public int arrayOffset()
/* 133:    */   {
/* 134:162 */     return 0;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public boolean hasMemoryAddress()
/* 138:    */   {
/* 139:167 */     return false;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public long memoryAddress()
/* 143:    */   {
/* 144:172 */     throw new UnsupportedOperationException();
/* 145:    */   }
/* 146:    */   
/* 147:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 148:    */   {
/* 149:177 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 150:178 */     if (dst.hasMemoryAddress()) {
/* 151:179 */       PlatformDependent.copyMemory(this.array, index, dst.memoryAddress() + dstIndex, length);
/* 152:180 */     } else if (dst.hasArray()) {
/* 153:181 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 154:    */     } else {
/* 155:183 */       dst.setBytes(dstIndex, this.array, index, length);
/* 156:    */     }
/* 157:185 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 161:    */   {
/* 162:190 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 163:191 */     System.arraycopy(this.array, index, dst, dstIndex, length);
/* 164:192 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 168:    */   {
/* 169:197 */     checkIndex(index, dst.remaining());
/* 170:198 */     dst.put(this.array, index, dst.remaining());
/* 171:199 */     return this;
/* 172:    */   }
/* 173:    */   
/* 174:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 175:    */     throws IOException
/* 176:    */   {
/* 177:204 */     ensureAccessible();
/* 178:205 */     out.write(this.array, index, length);
/* 179:206 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 183:    */     throws IOException
/* 184:    */   {
/* 185:211 */     ensureAccessible();
/* 186:212 */     return getBytes(index, out, length, false);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 190:    */     throws IOException
/* 191:    */   {
/* 192:217 */     ensureAccessible();
/* 193:218 */     return getBytes(index, out, position, length, false);
/* 194:    */   }
/* 195:    */   
/* 196:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 197:    */     throws IOException
/* 198:    */   {
/* 199:222 */     ensureAccessible();
/* 200:    */     ByteBuffer tmpBuf;
/* 201:    */     ByteBuffer tmpBuf;
/* 202:224 */     if (internal) {
/* 203:225 */       tmpBuf = internalNioBuffer();
/* 204:    */     } else {
/* 205:227 */       tmpBuf = ByteBuffer.wrap(this.array);
/* 206:    */     }
/* 207:229 */     return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length));
/* 208:    */   }
/* 209:    */   
/* 210:    */   private int getBytes(int index, FileChannel out, long position, int length, boolean internal)
/* 211:    */     throws IOException
/* 212:    */   {
/* 213:233 */     ensureAccessible();
/* 214:234 */     ByteBuffer tmpBuf = internal ? internalNioBuffer() : ByteBuffer.wrap(this.array);
/* 215:235 */     return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length), position);
/* 216:    */   }
/* 217:    */   
/* 218:    */   public int readBytes(GatheringByteChannel out, int length)
/* 219:    */     throws IOException
/* 220:    */   {
/* 221:240 */     checkReadableBytes(length);
/* 222:241 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 223:242 */     this.readerIndex += readBytes;
/* 224:243 */     return readBytes;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public int readBytes(FileChannel out, long position, int length)
/* 228:    */     throws IOException
/* 229:    */   {
/* 230:248 */     checkReadableBytes(length);
/* 231:249 */     int readBytes = getBytes(this.readerIndex, out, position, length, true);
/* 232:250 */     this.readerIndex += readBytes;
/* 233:251 */     return readBytes;
/* 234:    */   }
/* 235:    */   
/* 236:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 237:    */   {
/* 238:256 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 239:257 */     if (src.hasMemoryAddress()) {
/* 240:258 */       PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, this.array, index, length);
/* 241:259 */     } else if (src.hasArray()) {
/* 242:260 */       setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
/* 243:    */     } else {
/* 244:262 */       src.getBytes(srcIndex, this.array, index, length);
/* 245:    */     }
/* 246:264 */     return this;
/* 247:    */   }
/* 248:    */   
/* 249:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 250:    */   {
/* 251:269 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 252:270 */     System.arraycopy(src, srcIndex, this.array, index, length);
/* 253:271 */     return this;
/* 254:    */   }
/* 255:    */   
/* 256:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 257:    */   {
/* 258:276 */     ensureAccessible();
/* 259:277 */     src.get(this.array, index, src.remaining());
/* 260:278 */     return this;
/* 261:    */   }
/* 262:    */   
/* 263:    */   public int setBytes(int index, InputStream in, int length)
/* 264:    */     throws IOException
/* 265:    */   {
/* 266:283 */     ensureAccessible();
/* 267:284 */     return in.read(this.array, index, length);
/* 268:    */   }
/* 269:    */   
/* 270:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 271:    */     throws IOException
/* 272:    */   {
/* 273:289 */     ensureAccessible();
/* 274:    */     try
/* 275:    */     {
/* 276:291 */       return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length));
/* 277:    */     }
/* 278:    */     catch (ClosedChannelException ignored) {}
/* 279:293 */     return -1;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 283:    */     throws IOException
/* 284:    */   {
/* 285:299 */     ensureAccessible();
/* 286:    */     try
/* 287:    */     {
/* 288:301 */       return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length), position);
/* 289:    */     }
/* 290:    */     catch (ClosedChannelException ignored) {}
/* 291:303 */     return -1;
/* 292:    */   }
/* 293:    */   
/* 294:    */   public int nioBufferCount()
/* 295:    */   {
/* 296:309 */     return 1;
/* 297:    */   }
/* 298:    */   
/* 299:    */   public ByteBuffer nioBuffer(int index, int length)
/* 300:    */   {
/* 301:314 */     ensureAccessible();
/* 302:315 */     return ByteBuffer.wrap(this.array, index, length).slice();
/* 303:    */   }
/* 304:    */   
/* 305:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 306:    */   {
/* 307:320 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 308:    */   }
/* 309:    */   
/* 310:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 311:    */   {
/* 312:325 */     checkIndex(index, length);
/* 313:326 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 314:    */   }
/* 315:    */   
/* 316:    */   public byte getByte(int index)
/* 317:    */   {
/* 318:331 */     ensureAccessible();
/* 319:332 */     return _getByte(index);
/* 320:    */   }
/* 321:    */   
/* 322:    */   protected byte _getByte(int index)
/* 323:    */   {
/* 324:337 */     return HeapByteBufUtil.getByte(this.array, index);
/* 325:    */   }
/* 326:    */   
/* 327:    */   public short getShort(int index)
/* 328:    */   {
/* 329:342 */     ensureAccessible();
/* 330:343 */     return _getShort(index);
/* 331:    */   }
/* 332:    */   
/* 333:    */   protected short _getShort(int index)
/* 334:    */   {
/* 335:348 */     return HeapByteBufUtil.getShort(this.array, index);
/* 336:    */   }
/* 337:    */   
/* 338:    */   public short getShortLE(int index)
/* 339:    */   {
/* 340:353 */     ensureAccessible();
/* 341:354 */     return _getShortLE(index);
/* 342:    */   }
/* 343:    */   
/* 344:    */   protected short _getShortLE(int index)
/* 345:    */   {
/* 346:359 */     return HeapByteBufUtil.getShortLE(this.array, index);
/* 347:    */   }
/* 348:    */   
/* 349:    */   public int getUnsignedMedium(int index)
/* 350:    */   {
/* 351:364 */     ensureAccessible();
/* 352:365 */     return _getUnsignedMedium(index);
/* 353:    */   }
/* 354:    */   
/* 355:    */   protected int _getUnsignedMedium(int index)
/* 356:    */   {
/* 357:370 */     return HeapByteBufUtil.getUnsignedMedium(this.array, index);
/* 358:    */   }
/* 359:    */   
/* 360:    */   public int getUnsignedMediumLE(int index)
/* 361:    */   {
/* 362:375 */     ensureAccessible();
/* 363:376 */     return _getUnsignedMediumLE(index);
/* 364:    */   }
/* 365:    */   
/* 366:    */   protected int _getUnsignedMediumLE(int index)
/* 367:    */   {
/* 368:381 */     return HeapByteBufUtil.getUnsignedMediumLE(this.array, index);
/* 369:    */   }
/* 370:    */   
/* 371:    */   public int getInt(int index)
/* 372:    */   {
/* 373:386 */     ensureAccessible();
/* 374:387 */     return _getInt(index);
/* 375:    */   }
/* 376:    */   
/* 377:    */   protected int _getInt(int index)
/* 378:    */   {
/* 379:392 */     return HeapByteBufUtil.getInt(this.array, index);
/* 380:    */   }
/* 381:    */   
/* 382:    */   public int getIntLE(int index)
/* 383:    */   {
/* 384:397 */     ensureAccessible();
/* 385:398 */     return _getIntLE(index);
/* 386:    */   }
/* 387:    */   
/* 388:    */   protected int _getIntLE(int index)
/* 389:    */   {
/* 390:403 */     return HeapByteBufUtil.getIntLE(this.array, index);
/* 391:    */   }
/* 392:    */   
/* 393:    */   public long getLong(int index)
/* 394:    */   {
/* 395:408 */     ensureAccessible();
/* 396:409 */     return _getLong(index);
/* 397:    */   }
/* 398:    */   
/* 399:    */   protected long _getLong(int index)
/* 400:    */   {
/* 401:414 */     return HeapByteBufUtil.getLong(this.array, index);
/* 402:    */   }
/* 403:    */   
/* 404:    */   public long getLongLE(int index)
/* 405:    */   {
/* 406:419 */     ensureAccessible();
/* 407:420 */     return _getLongLE(index);
/* 408:    */   }
/* 409:    */   
/* 410:    */   protected long _getLongLE(int index)
/* 411:    */   {
/* 412:425 */     return HeapByteBufUtil.getLongLE(this.array, index);
/* 413:    */   }
/* 414:    */   
/* 415:    */   public ByteBuf setByte(int index, int value)
/* 416:    */   {
/* 417:430 */     ensureAccessible();
/* 418:431 */     _setByte(index, value);
/* 419:432 */     return this;
/* 420:    */   }
/* 421:    */   
/* 422:    */   protected void _setByte(int index, int value)
/* 423:    */   {
/* 424:437 */     HeapByteBufUtil.setByte(this.array, index, value);
/* 425:    */   }
/* 426:    */   
/* 427:    */   public ByteBuf setShort(int index, int value)
/* 428:    */   {
/* 429:442 */     ensureAccessible();
/* 430:443 */     _setShort(index, value);
/* 431:444 */     return this;
/* 432:    */   }
/* 433:    */   
/* 434:    */   protected void _setShort(int index, int value)
/* 435:    */   {
/* 436:449 */     HeapByteBufUtil.setShort(this.array, index, value);
/* 437:    */   }
/* 438:    */   
/* 439:    */   public ByteBuf setShortLE(int index, int value)
/* 440:    */   {
/* 441:454 */     ensureAccessible();
/* 442:455 */     _setShortLE(index, value);
/* 443:456 */     return this;
/* 444:    */   }
/* 445:    */   
/* 446:    */   protected void _setShortLE(int index, int value)
/* 447:    */   {
/* 448:461 */     HeapByteBufUtil.setShortLE(this.array, index, value);
/* 449:    */   }
/* 450:    */   
/* 451:    */   public ByteBuf setMedium(int index, int value)
/* 452:    */   {
/* 453:466 */     ensureAccessible();
/* 454:467 */     _setMedium(index, value);
/* 455:468 */     return this;
/* 456:    */   }
/* 457:    */   
/* 458:    */   protected void _setMedium(int index, int value)
/* 459:    */   {
/* 460:473 */     HeapByteBufUtil.setMedium(this.array, index, value);
/* 461:    */   }
/* 462:    */   
/* 463:    */   public ByteBuf setMediumLE(int index, int value)
/* 464:    */   {
/* 465:478 */     ensureAccessible();
/* 466:479 */     _setMediumLE(index, value);
/* 467:480 */     return this;
/* 468:    */   }
/* 469:    */   
/* 470:    */   protected void _setMediumLE(int index, int value)
/* 471:    */   {
/* 472:485 */     HeapByteBufUtil.setMediumLE(this.array, index, value);
/* 473:    */   }
/* 474:    */   
/* 475:    */   public ByteBuf setInt(int index, int value)
/* 476:    */   {
/* 477:490 */     ensureAccessible();
/* 478:491 */     _setInt(index, value);
/* 479:492 */     return this;
/* 480:    */   }
/* 481:    */   
/* 482:    */   protected void _setInt(int index, int value)
/* 483:    */   {
/* 484:497 */     HeapByteBufUtil.setInt(this.array, index, value);
/* 485:    */   }
/* 486:    */   
/* 487:    */   public ByteBuf setIntLE(int index, int value)
/* 488:    */   {
/* 489:502 */     ensureAccessible();
/* 490:503 */     _setIntLE(index, value);
/* 491:504 */     return this;
/* 492:    */   }
/* 493:    */   
/* 494:    */   protected void _setIntLE(int index, int value)
/* 495:    */   {
/* 496:509 */     HeapByteBufUtil.setIntLE(this.array, index, value);
/* 497:    */   }
/* 498:    */   
/* 499:    */   public ByteBuf setLong(int index, long value)
/* 500:    */   {
/* 501:514 */     ensureAccessible();
/* 502:515 */     _setLong(index, value);
/* 503:516 */     return this;
/* 504:    */   }
/* 505:    */   
/* 506:    */   protected void _setLong(int index, long value)
/* 507:    */   {
/* 508:521 */     HeapByteBufUtil.setLong(this.array, index, value);
/* 509:    */   }
/* 510:    */   
/* 511:    */   public ByteBuf setLongLE(int index, long value)
/* 512:    */   {
/* 513:526 */     ensureAccessible();
/* 514:527 */     _setLongLE(index, value);
/* 515:528 */     return this;
/* 516:    */   }
/* 517:    */   
/* 518:    */   protected void _setLongLE(int index, long value)
/* 519:    */   {
/* 520:533 */     HeapByteBufUtil.setLongLE(this.array, index, value);
/* 521:    */   }
/* 522:    */   
/* 523:    */   public ByteBuf copy(int index, int length)
/* 524:    */   {
/* 525:538 */     checkIndex(index, length);
/* 526:539 */     byte[] copiedArray = new byte[length];
/* 527:540 */     System.arraycopy(this.array, index, copiedArray, 0, length);
/* 528:541 */     return new UnpooledHeapByteBuf(alloc(), copiedArray, maxCapacity());
/* 529:    */   }
/* 530:    */   
/* 531:    */   private ByteBuffer internalNioBuffer()
/* 532:    */   {
/* 533:545 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 534:546 */     if (tmpNioBuf == null) {
/* 535:547 */       this.tmpNioBuf = (tmpNioBuf = ByteBuffer.wrap(this.array));
/* 536:    */     }
/* 537:549 */     return tmpNioBuf;
/* 538:    */   }
/* 539:    */   
/* 540:    */   protected void deallocate()
/* 541:    */   {
/* 542:554 */     freeArray(this.array);
/* 543:555 */     this.array = null;
/* 544:    */   }
/* 545:    */   
/* 546:    */   public ByteBuf unwrap()
/* 547:    */   {
/* 548:560 */     return null;
/* 549:    */   }
/* 550:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledHeapByteBuf
 * JD-Core Version:    0.7.0.1
 */