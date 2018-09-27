/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.Buffer;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.ReadOnlyBufferException;
/*  11:    */ import java.nio.channels.FileChannel;
/*  12:    */ import java.nio.channels.GatheringByteChannel;
/*  13:    */ import java.nio.channels.ScatteringByteChannel;
/*  14:    */ 
/*  15:    */ class ReadOnlyByteBufferBuf
/*  16:    */   extends AbstractReferenceCountedByteBuf
/*  17:    */ {
/*  18:    */   protected final ByteBuffer buffer;
/*  19:    */   private final ByteBufAllocator allocator;
/*  20:    */   private ByteBuffer tmpNioBuf;
/*  21:    */   
/*  22:    */   ReadOnlyByteBufferBuf(ByteBufAllocator allocator, ByteBuffer buffer)
/*  23:    */   {
/*  24: 41 */     super(buffer.remaining());
/*  25: 42 */     if (!buffer.isReadOnly()) {
/*  26: 43 */       throw new IllegalArgumentException("must be a readonly buffer: " + StringUtil.simpleClassName(buffer));
/*  27:    */     }
/*  28: 46 */     this.allocator = allocator;
/*  29: 47 */     this.buffer = buffer.slice().order(ByteOrder.BIG_ENDIAN);
/*  30: 48 */     writerIndex(this.buffer.limit());
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected void deallocate() {}
/*  34:    */   
/*  35:    */   public byte getByte(int index)
/*  36:    */   {
/*  37: 56 */     ensureAccessible();
/*  38: 57 */     return _getByte(index);
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected byte _getByte(int index)
/*  42:    */   {
/*  43: 62 */     return this.buffer.get(index);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public short getShort(int index)
/*  47:    */   {
/*  48: 67 */     ensureAccessible();
/*  49: 68 */     return _getShort(index);
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected short _getShort(int index)
/*  53:    */   {
/*  54: 73 */     return this.buffer.getShort(index);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public short getShortLE(int index)
/*  58:    */   {
/*  59: 78 */     ensureAccessible();
/*  60: 79 */     return _getShortLE(index);
/*  61:    */   }
/*  62:    */   
/*  63:    */   protected short _getShortLE(int index)
/*  64:    */   {
/*  65: 84 */     return ByteBufUtil.swapShort(this.buffer.getShort(index));
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int getUnsignedMedium(int index)
/*  69:    */   {
/*  70: 89 */     ensureAccessible();
/*  71: 90 */     return _getUnsignedMedium(index);
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected int _getUnsignedMedium(int index)
/*  75:    */   {
/*  76: 95 */     return 
/*  77:    */     
/*  78: 97 */       (getByte(index) & 0xFF) << 16 | (getByte(index + 1) & 0xFF) << 8 | getByte(index + 2) & 0xFF;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public int getUnsignedMediumLE(int index)
/*  82:    */   {
/*  83:102 */     ensureAccessible();
/*  84:103 */     return _getUnsignedMediumLE(index);
/*  85:    */   }
/*  86:    */   
/*  87:    */   protected int _getUnsignedMediumLE(int index)
/*  88:    */   {
/*  89:108 */     return 
/*  90:    */     
/*  91:110 */       getByte(index) & 0xFF | (getByte(index + 1) & 0xFF) << 8 | (getByte(index + 2) & 0xFF) << 16;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public int getInt(int index)
/*  95:    */   {
/*  96:115 */     ensureAccessible();
/*  97:116 */     return _getInt(index);
/*  98:    */   }
/*  99:    */   
/* 100:    */   protected int _getInt(int index)
/* 101:    */   {
/* 102:121 */     return this.buffer.getInt(index);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public int getIntLE(int index)
/* 106:    */   {
/* 107:126 */     ensureAccessible();
/* 108:127 */     return _getIntLE(index);
/* 109:    */   }
/* 110:    */   
/* 111:    */   protected int _getIntLE(int index)
/* 112:    */   {
/* 113:132 */     return ByteBufUtil.swapInt(this.buffer.getInt(index));
/* 114:    */   }
/* 115:    */   
/* 116:    */   public long getLong(int index)
/* 117:    */   {
/* 118:137 */     ensureAccessible();
/* 119:138 */     return _getLong(index);
/* 120:    */   }
/* 121:    */   
/* 122:    */   protected long _getLong(int index)
/* 123:    */   {
/* 124:143 */     return this.buffer.getLong(index);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public long getLongLE(int index)
/* 128:    */   {
/* 129:148 */     ensureAccessible();
/* 130:149 */     return _getLongLE(index);
/* 131:    */   }
/* 132:    */   
/* 133:    */   protected long _getLongLE(int index)
/* 134:    */   {
/* 135:154 */     return ByteBufUtil.swapLong(this.buffer.getLong(index));
/* 136:    */   }
/* 137:    */   
/* 138:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 139:    */   {
/* 140:159 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 141:160 */     if (dst.hasArray()) {
/* 142:161 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 143:162 */     } else if (dst.nioBufferCount() > 0) {
/* 144:163 */       for (ByteBuffer bb : dst.nioBuffers(dstIndex, length))
/* 145:    */       {
/* 146:164 */         int bbLen = bb.remaining();
/* 147:165 */         getBytes(index, bb);
/* 148:166 */         index += bbLen;
/* 149:    */       }
/* 150:    */     } else {
/* 151:169 */       dst.setBytes(dstIndex, this, index, length);
/* 152:    */     }
/* 153:171 */     return this;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 157:    */   {
/* 158:176 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 159:178 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/* 160:179 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] {
/* 161:180 */         Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) }));
/* 162:    */     }
/* 163:183 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 164:184 */     tmpBuf.clear().position(index).limit(index + length);
/* 165:185 */     tmpBuf.get(dst, dstIndex, length);
/* 166:186 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 170:    */   {
/* 171:191 */     checkIndex(index);
/* 172:192 */     if (dst == null) {
/* 173:193 */       throw new NullPointerException("dst");
/* 174:    */     }
/* 175:196 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/* 176:197 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 177:198 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/* 178:199 */     dst.put(tmpBuf);
/* 179:200 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public ByteBuf setByte(int index, int value)
/* 183:    */   {
/* 184:205 */     throw new ReadOnlyBufferException();
/* 185:    */   }
/* 186:    */   
/* 187:    */   protected void _setByte(int index, int value)
/* 188:    */   {
/* 189:210 */     throw new ReadOnlyBufferException();
/* 190:    */   }
/* 191:    */   
/* 192:    */   public ByteBuf setShort(int index, int value)
/* 193:    */   {
/* 194:215 */     throw new ReadOnlyBufferException();
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected void _setShort(int index, int value)
/* 198:    */   {
/* 199:220 */     throw new ReadOnlyBufferException();
/* 200:    */   }
/* 201:    */   
/* 202:    */   public ByteBuf setShortLE(int index, int value)
/* 203:    */   {
/* 204:225 */     throw new ReadOnlyBufferException();
/* 205:    */   }
/* 206:    */   
/* 207:    */   protected void _setShortLE(int index, int value)
/* 208:    */   {
/* 209:230 */     throw new ReadOnlyBufferException();
/* 210:    */   }
/* 211:    */   
/* 212:    */   public ByteBuf setMedium(int index, int value)
/* 213:    */   {
/* 214:235 */     throw new ReadOnlyBufferException();
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected void _setMedium(int index, int value)
/* 218:    */   {
/* 219:240 */     throw new ReadOnlyBufferException();
/* 220:    */   }
/* 221:    */   
/* 222:    */   public ByteBuf setMediumLE(int index, int value)
/* 223:    */   {
/* 224:245 */     throw new ReadOnlyBufferException();
/* 225:    */   }
/* 226:    */   
/* 227:    */   protected void _setMediumLE(int index, int value)
/* 228:    */   {
/* 229:250 */     throw new ReadOnlyBufferException();
/* 230:    */   }
/* 231:    */   
/* 232:    */   public ByteBuf setInt(int index, int value)
/* 233:    */   {
/* 234:255 */     throw new ReadOnlyBufferException();
/* 235:    */   }
/* 236:    */   
/* 237:    */   protected void _setInt(int index, int value)
/* 238:    */   {
/* 239:260 */     throw new ReadOnlyBufferException();
/* 240:    */   }
/* 241:    */   
/* 242:    */   public ByteBuf setIntLE(int index, int value)
/* 243:    */   {
/* 244:265 */     throw new ReadOnlyBufferException();
/* 245:    */   }
/* 246:    */   
/* 247:    */   protected void _setIntLE(int index, int value)
/* 248:    */   {
/* 249:270 */     throw new ReadOnlyBufferException();
/* 250:    */   }
/* 251:    */   
/* 252:    */   public ByteBuf setLong(int index, long value)
/* 253:    */   {
/* 254:275 */     throw new ReadOnlyBufferException();
/* 255:    */   }
/* 256:    */   
/* 257:    */   protected void _setLong(int index, long value)
/* 258:    */   {
/* 259:280 */     throw new ReadOnlyBufferException();
/* 260:    */   }
/* 261:    */   
/* 262:    */   public ByteBuf setLongLE(int index, long value)
/* 263:    */   {
/* 264:285 */     throw new ReadOnlyBufferException();
/* 265:    */   }
/* 266:    */   
/* 267:    */   protected void _setLongLE(int index, long value)
/* 268:    */   {
/* 269:290 */     throw new ReadOnlyBufferException();
/* 270:    */   }
/* 271:    */   
/* 272:    */   public int capacity()
/* 273:    */   {
/* 274:295 */     return maxCapacity();
/* 275:    */   }
/* 276:    */   
/* 277:    */   public ByteBuf capacity(int newCapacity)
/* 278:    */   {
/* 279:300 */     throw new ReadOnlyBufferException();
/* 280:    */   }
/* 281:    */   
/* 282:    */   public ByteBufAllocator alloc()
/* 283:    */   {
/* 284:305 */     return this.allocator;
/* 285:    */   }
/* 286:    */   
/* 287:    */   public ByteOrder order()
/* 288:    */   {
/* 289:310 */     return ByteOrder.BIG_ENDIAN;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public ByteBuf unwrap()
/* 293:    */   {
/* 294:315 */     return null;
/* 295:    */   }
/* 296:    */   
/* 297:    */   public boolean isReadOnly()
/* 298:    */   {
/* 299:320 */     return this.buffer.isReadOnly();
/* 300:    */   }
/* 301:    */   
/* 302:    */   public boolean isDirect()
/* 303:    */   {
/* 304:325 */     return this.buffer.isDirect();
/* 305:    */   }
/* 306:    */   
/* 307:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 308:    */     throws IOException
/* 309:    */   {
/* 310:330 */     ensureAccessible();
/* 311:331 */     if (length == 0) {
/* 312:332 */       return this;
/* 313:    */     }
/* 314:335 */     if (this.buffer.hasArray())
/* 315:    */     {
/* 316:336 */       out.write(this.buffer.array(), index + this.buffer.arrayOffset(), length);
/* 317:    */     }
/* 318:    */     else
/* 319:    */     {
/* 320:338 */       byte[] tmp = new byte[length];
/* 321:339 */       ByteBuffer tmpBuf = internalNioBuffer();
/* 322:340 */       tmpBuf.clear().position(index);
/* 323:341 */       tmpBuf.get(tmp);
/* 324:342 */       out.write(tmp);
/* 325:    */     }
/* 326:344 */     return this;
/* 327:    */   }
/* 328:    */   
/* 329:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 330:    */     throws IOException
/* 331:    */   {
/* 332:349 */     ensureAccessible();
/* 333:350 */     if (length == 0) {
/* 334:351 */       return 0;
/* 335:    */     }
/* 336:354 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 337:355 */     tmpBuf.clear().position(index).limit(index + length);
/* 338:356 */     return out.write(tmpBuf);
/* 339:    */   }
/* 340:    */   
/* 341:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 342:    */     throws IOException
/* 343:    */   {
/* 344:361 */     ensureAccessible();
/* 345:362 */     if (length == 0) {
/* 346:363 */       return 0;
/* 347:    */     }
/* 348:366 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 349:367 */     tmpBuf.clear().position(index).limit(index + length);
/* 350:368 */     return out.write(tmpBuf, position);
/* 351:    */   }
/* 352:    */   
/* 353:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 354:    */   {
/* 355:373 */     throw new ReadOnlyBufferException();
/* 356:    */   }
/* 357:    */   
/* 358:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 359:    */   {
/* 360:378 */     throw new ReadOnlyBufferException();
/* 361:    */   }
/* 362:    */   
/* 363:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 364:    */   {
/* 365:383 */     throw new ReadOnlyBufferException();
/* 366:    */   }
/* 367:    */   
/* 368:    */   public int setBytes(int index, InputStream in, int length)
/* 369:    */     throws IOException
/* 370:    */   {
/* 371:388 */     throw new ReadOnlyBufferException();
/* 372:    */   }
/* 373:    */   
/* 374:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 375:    */     throws IOException
/* 376:    */   {
/* 377:393 */     throw new ReadOnlyBufferException();
/* 378:    */   }
/* 379:    */   
/* 380:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 381:    */     throws IOException
/* 382:    */   {
/* 383:398 */     throw new ReadOnlyBufferException();
/* 384:    */   }
/* 385:    */   
/* 386:    */   protected final ByteBuffer internalNioBuffer()
/* 387:    */   {
/* 388:402 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 389:403 */     if (tmpNioBuf == null) {
/* 390:404 */       this.tmpNioBuf = (tmpNioBuf = this.buffer.duplicate());
/* 391:    */     }
/* 392:406 */     return tmpNioBuf;
/* 393:    */   }
/* 394:    */   
/* 395:    */   public ByteBuf copy(int index, int length)
/* 396:    */   {
/* 397:411 */     ensureAccessible();
/* 398:    */     try
/* 399:    */     {
/* 400:414 */       src = (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 401:    */     }
/* 402:    */     catch (IllegalArgumentException ignored)
/* 403:    */     {
/* 404:    */       ByteBuffer src;
/* 405:416 */       throw new IndexOutOfBoundsException("Too many bytes to read - Need " + (index + length));
/* 406:    */     }
/* 407:    */     ByteBuffer src;
/* 408:419 */     ByteBuf dst = src.isDirect() ? alloc().directBuffer(length) : alloc().heapBuffer(length);
/* 409:420 */     dst.writeBytes(src);
/* 410:421 */     return dst;
/* 411:    */   }
/* 412:    */   
/* 413:    */   public int nioBufferCount()
/* 414:    */   {
/* 415:426 */     return 1;
/* 416:    */   }
/* 417:    */   
/* 418:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 419:    */   {
/* 420:431 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 421:    */   }
/* 422:    */   
/* 423:    */   public ByteBuffer nioBuffer(int index, int length)
/* 424:    */   {
/* 425:436 */     return (ByteBuffer)this.buffer.duplicate().position(index).limit(index + length);
/* 426:    */   }
/* 427:    */   
/* 428:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 429:    */   {
/* 430:441 */     ensureAccessible();
/* 431:442 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 432:    */   }
/* 433:    */   
/* 434:    */   public boolean hasArray()
/* 435:    */   {
/* 436:447 */     return this.buffer.hasArray();
/* 437:    */   }
/* 438:    */   
/* 439:    */   public byte[] array()
/* 440:    */   {
/* 441:452 */     return this.buffer.array();
/* 442:    */   }
/* 443:    */   
/* 444:    */   public int arrayOffset()
/* 445:    */   {
/* 446:457 */     return this.buffer.arrayOffset();
/* 447:    */   }
/* 448:    */   
/* 449:    */   public boolean hasMemoryAddress()
/* 450:    */   {
/* 451:462 */     return false;
/* 452:    */   }
/* 453:    */   
/* 454:    */   public long memoryAddress()
/* 455:    */   {
/* 456:467 */     throw new UnsupportedOperationException();
/* 457:    */   }
/* 458:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ReadOnlyByteBufferBuf
 * JD-Core Version:    0.7.0.1
 */