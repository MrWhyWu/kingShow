/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.nio.Buffer;
/*   9:    */ import java.nio.ByteBuffer;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ import java.nio.channels.FileChannel;
/*  12:    */ import java.nio.channels.GatheringByteChannel;
/*  13:    */ import java.nio.channels.ScatteringByteChannel;
/*  14:    */ 
/*  15:    */ final class PooledDirectByteBuf
/*  16:    */   extends PooledByteBuf<ByteBuffer>
/*  17:    */ {
/*  18: 32 */   private static final Recycler<PooledDirectByteBuf> RECYCLER = new Recycler()
/*  19:    */   {
/*  20:    */     protected PooledDirectByteBuf newObject(Recycler.Handle<PooledDirectByteBuf> handle)
/*  21:    */     {
/*  22: 35 */       return new PooledDirectByteBuf(handle, 0, null);
/*  23:    */     }
/*  24:    */   };
/*  25:    */   
/*  26:    */   static PooledDirectByteBuf newInstance(int maxCapacity)
/*  27:    */   {
/*  28: 40 */     PooledDirectByteBuf buf = (PooledDirectByteBuf)RECYCLER.get();
/*  29: 41 */     buf.reuse(maxCapacity);
/*  30: 42 */     return buf;
/*  31:    */   }
/*  32:    */   
/*  33:    */   private PooledDirectByteBuf(Recycler.Handle<PooledDirectByteBuf> recyclerHandle, int maxCapacity)
/*  34:    */   {
/*  35: 46 */     super(recyclerHandle, maxCapacity);
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected ByteBuffer newInternalNioBuffer(ByteBuffer memory)
/*  39:    */   {
/*  40: 51 */     return memory.duplicate();
/*  41:    */   }
/*  42:    */   
/*  43:    */   public boolean isDirect()
/*  44:    */   {
/*  45: 56 */     return true;
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected byte _getByte(int index)
/*  49:    */   {
/*  50: 61 */     return ((ByteBuffer)this.memory).get(idx(index));
/*  51:    */   }
/*  52:    */   
/*  53:    */   protected short _getShort(int index)
/*  54:    */   {
/*  55: 66 */     return ((ByteBuffer)this.memory).getShort(idx(index));
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected short _getShortLE(int index)
/*  59:    */   {
/*  60: 71 */     return ByteBufUtil.swapShort(_getShort(index));
/*  61:    */   }
/*  62:    */   
/*  63:    */   protected int _getUnsignedMedium(int index)
/*  64:    */   {
/*  65: 76 */     index = idx(index);
/*  66: 77 */     return (((ByteBuffer)this.memory).get(index) & 0xFF) << 16 | 
/*  67: 78 */       (((ByteBuffer)this.memory).get(index + 1) & 0xFF) << 8 | ((ByteBuffer)this.memory)
/*  68: 79 */       .get(index + 2) & 0xFF;
/*  69:    */   }
/*  70:    */   
/*  71:    */   protected int _getUnsignedMediumLE(int index)
/*  72:    */   {
/*  73: 84 */     index = idx(index);
/*  74: 85 */     return ((ByteBuffer)this.memory).get(index) & 0xFF | 
/*  75: 86 */       (((ByteBuffer)this.memory).get(index + 1) & 0xFF) << 8 | 
/*  76: 87 */       (((ByteBuffer)this.memory).get(index + 2) & 0xFF) << 16;
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected int _getInt(int index)
/*  80:    */   {
/*  81: 92 */     return ((ByteBuffer)this.memory).getInt(idx(index));
/*  82:    */   }
/*  83:    */   
/*  84:    */   protected int _getIntLE(int index)
/*  85:    */   {
/*  86: 97 */     return ByteBufUtil.swapInt(_getInt(index));
/*  87:    */   }
/*  88:    */   
/*  89:    */   protected long _getLong(int index)
/*  90:    */   {
/*  91:102 */     return ((ByteBuffer)this.memory).getLong(idx(index));
/*  92:    */   }
/*  93:    */   
/*  94:    */   protected long _getLongLE(int index)
/*  95:    */   {
/*  96:107 */     return ByteBufUtil.swapLong(_getLong(index));
/*  97:    */   }
/*  98:    */   
/*  99:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 100:    */   {
/* 101:112 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 102:113 */     if (dst.hasArray()) {
/* 103:114 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 104:115 */     } else if (dst.nioBufferCount() > 0) {
/* 105:116 */       for (ByteBuffer bb : dst.nioBuffers(dstIndex, length))
/* 106:    */       {
/* 107:117 */         int bbLen = bb.remaining();
/* 108:118 */         getBytes(index, bb);
/* 109:119 */         index += bbLen;
/* 110:    */       }
/* 111:    */     } else {
/* 112:122 */       dst.setBytes(dstIndex, this, index, length);
/* 113:    */     }
/* 114:124 */     return this;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 118:    */   {
/* 119:129 */     getBytes(index, dst, dstIndex, length, false);
/* 120:130 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   private void getBytes(int index, byte[] dst, int dstIndex, int length, boolean internal)
/* 124:    */   {
/* 125:134 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 126:    */     ByteBuffer tmpBuf;
/* 127:    */     ByteBuffer tmpBuf;
/* 128:136 */     if (internal) {
/* 129:137 */       tmpBuf = internalNioBuffer();
/* 130:    */     } else {
/* 131:139 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 132:    */     }
/* 133:141 */     index = idx(index);
/* 134:142 */     tmpBuf.clear().position(index).limit(index + length);
/* 135:143 */     tmpBuf.get(dst, dstIndex, length);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 139:    */   {
/* 140:148 */     checkReadableBytes(length);
/* 141:149 */     getBytes(this.readerIndex, dst, dstIndex, length, true);
/* 142:150 */     this.readerIndex += length;
/* 143:151 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 147:    */   {
/* 148:156 */     getBytes(index, dst, false);
/* 149:157 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   private void getBytes(int index, ByteBuffer dst, boolean internal)
/* 153:    */   {
/* 154:161 */     checkIndex(index, dst.remaining());
/* 155:    */     ByteBuffer tmpBuf;
/* 156:    */     ByteBuffer tmpBuf;
/* 157:163 */     if (internal) {
/* 158:164 */       tmpBuf = internalNioBuffer();
/* 159:    */     } else {
/* 160:166 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 161:    */     }
/* 162:168 */     index = idx(index);
/* 163:169 */     tmpBuf.clear().position(index).limit(index + dst.remaining());
/* 164:170 */     dst.put(tmpBuf);
/* 165:    */   }
/* 166:    */   
/* 167:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 168:    */   {
/* 169:175 */     int length = dst.remaining();
/* 170:176 */     checkReadableBytes(length);
/* 171:177 */     getBytes(this.readerIndex, dst, true);
/* 172:178 */     this.readerIndex += length;
/* 173:179 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 177:    */     throws IOException
/* 178:    */   {
/* 179:184 */     getBytes(index, out, length, false);
/* 180:185 */     return this;
/* 181:    */   }
/* 182:    */   
/* 183:    */   private void getBytes(int index, OutputStream out, int length, boolean internal)
/* 184:    */     throws IOException
/* 185:    */   {
/* 186:189 */     checkIndex(index, length);
/* 187:190 */     if (length == 0) {
/* 188:191 */       return;
/* 189:    */     }
/* 190:194 */     byte[] tmp = new byte[length];
/* 191:    */     ByteBuffer tmpBuf;
/* 192:    */     ByteBuffer tmpBuf;
/* 193:196 */     if (internal) {
/* 194:197 */       tmpBuf = internalNioBuffer();
/* 195:    */     } else {
/* 196:199 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 197:    */     }
/* 198:201 */     tmpBuf.clear().position(idx(index));
/* 199:202 */     tmpBuf.get(tmp);
/* 200:203 */     out.write(tmp);
/* 201:    */   }
/* 202:    */   
/* 203:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 204:    */     throws IOException
/* 205:    */   {
/* 206:208 */     checkReadableBytes(length);
/* 207:209 */     getBytes(this.readerIndex, out, length, true);
/* 208:210 */     this.readerIndex += length;
/* 209:211 */     return this;
/* 210:    */   }
/* 211:    */   
/* 212:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 213:    */     throws IOException
/* 214:    */   {
/* 215:216 */     return getBytes(index, out, length, false);
/* 216:    */   }
/* 217:    */   
/* 218:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 219:    */     throws IOException
/* 220:    */   {
/* 221:220 */     checkIndex(index, length);
/* 222:221 */     if (length == 0) {
/* 223:222 */       return 0;
/* 224:    */     }
/* 225:    */     ByteBuffer tmpBuf;
/* 226:    */     ByteBuffer tmpBuf;
/* 227:226 */     if (internal) {
/* 228:227 */       tmpBuf = internalNioBuffer();
/* 229:    */     } else {
/* 230:229 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 231:    */     }
/* 232:231 */     index = idx(index);
/* 233:232 */     tmpBuf.clear().position(index).limit(index + length);
/* 234:233 */     return out.write(tmpBuf);
/* 235:    */   }
/* 236:    */   
/* 237:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 238:    */     throws IOException
/* 239:    */   {
/* 240:238 */     return getBytes(index, out, position, length, false);
/* 241:    */   }
/* 242:    */   
/* 243:    */   private int getBytes(int index, FileChannel out, long position, int length, boolean internal)
/* 244:    */     throws IOException
/* 245:    */   {
/* 246:242 */     checkIndex(index, length);
/* 247:243 */     if (length == 0) {
/* 248:244 */       return 0;
/* 249:    */     }
/* 250:247 */     ByteBuffer tmpBuf = internal ? internalNioBuffer() : ((ByteBuffer)this.memory).duplicate();
/* 251:248 */     index = idx(index);
/* 252:249 */     tmpBuf.clear().position(index).limit(index + length);
/* 253:250 */     return out.write(tmpBuf, position);
/* 254:    */   }
/* 255:    */   
/* 256:    */   public int readBytes(GatheringByteChannel out, int length)
/* 257:    */     throws IOException
/* 258:    */   {
/* 259:255 */     checkReadableBytes(length);
/* 260:256 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 261:257 */     this.readerIndex += readBytes;
/* 262:258 */     return readBytes;
/* 263:    */   }
/* 264:    */   
/* 265:    */   public int readBytes(FileChannel out, long position, int length)
/* 266:    */     throws IOException
/* 267:    */   {
/* 268:263 */     checkReadableBytes(length);
/* 269:264 */     int readBytes = getBytes(this.readerIndex, out, position, length, true);
/* 270:265 */     this.readerIndex += readBytes;
/* 271:266 */     return readBytes;
/* 272:    */   }
/* 273:    */   
/* 274:    */   protected void _setByte(int index, int value)
/* 275:    */   {
/* 276:271 */     ((ByteBuffer)this.memory).put(idx(index), (byte)value);
/* 277:    */   }
/* 278:    */   
/* 279:    */   protected void _setShort(int index, int value)
/* 280:    */   {
/* 281:276 */     ((ByteBuffer)this.memory).putShort(idx(index), (short)value);
/* 282:    */   }
/* 283:    */   
/* 284:    */   protected void _setShortLE(int index, int value)
/* 285:    */   {
/* 286:281 */     _setShort(index, ByteBufUtil.swapShort((short)value));
/* 287:    */   }
/* 288:    */   
/* 289:    */   protected void _setMedium(int index, int value)
/* 290:    */   {
/* 291:286 */     index = idx(index);
/* 292:287 */     ((ByteBuffer)this.memory).put(index, (byte)(value >>> 16));
/* 293:288 */     ((ByteBuffer)this.memory).put(index + 1, (byte)(value >>> 8));
/* 294:289 */     ((ByteBuffer)this.memory).put(index + 2, (byte)value);
/* 295:    */   }
/* 296:    */   
/* 297:    */   protected void _setMediumLE(int index, int value)
/* 298:    */   {
/* 299:294 */     index = idx(index);
/* 300:295 */     ((ByteBuffer)this.memory).put(index, (byte)value);
/* 301:296 */     ((ByteBuffer)this.memory).put(index + 1, (byte)(value >>> 8));
/* 302:297 */     ((ByteBuffer)this.memory).put(index + 2, (byte)(value >>> 16));
/* 303:    */   }
/* 304:    */   
/* 305:    */   protected void _setInt(int index, int value)
/* 306:    */   {
/* 307:302 */     ((ByteBuffer)this.memory).putInt(idx(index), value);
/* 308:    */   }
/* 309:    */   
/* 310:    */   protected void _setIntLE(int index, int value)
/* 311:    */   {
/* 312:307 */     _setInt(index, ByteBufUtil.swapInt(value));
/* 313:    */   }
/* 314:    */   
/* 315:    */   protected void _setLong(int index, long value)
/* 316:    */   {
/* 317:312 */     ((ByteBuffer)this.memory).putLong(idx(index), value);
/* 318:    */   }
/* 319:    */   
/* 320:    */   protected void _setLongLE(int index, long value)
/* 321:    */   {
/* 322:317 */     _setLong(index, ByteBufUtil.swapLong(value));
/* 323:    */   }
/* 324:    */   
/* 325:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 326:    */   {
/* 327:322 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 328:323 */     if (src.hasArray()) {
/* 329:324 */       setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
/* 330:325 */     } else if (src.nioBufferCount() > 0) {
/* 331:326 */       for (ByteBuffer bb : src.nioBuffers(srcIndex, length))
/* 332:    */       {
/* 333:327 */         int bbLen = bb.remaining();
/* 334:328 */         setBytes(index, bb);
/* 335:329 */         index += bbLen;
/* 336:    */       }
/* 337:    */     } else {
/* 338:332 */       src.getBytes(srcIndex, this, index, length);
/* 339:    */     }
/* 340:334 */     return this;
/* 341:    */   }
/* 342:    */   
/* 343:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 344:    */   {
/* 345:339 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 346:340 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 347:341 */     index = idx(index);
/* 348:342 */     tmpBuf.clear().position(index).limit(index + length);
/* 349:343 */     tmpBuf.put(src, srcIndex, length);
/* 350:344 */     return this;
/* 351:    */   }
/* 352:    */   
/* 353:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 354:    */   {
/* 355:349 */     checkIndex(index, src.remaining());
/* 356:350 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 357:351 */     if (src == tmpBuf) {
/* 358:352 */       src = src.duplicate();
/* 359:    */     }
/* 360:355 */     index = idx(index);
/* 361:356 */     tmpBuf.clear().position(index).limit(index + src.remaining());
/* 362:357 */     tmpBuf.put(src);
/* 363:358 */     return this;
/* 364:    */   }
/* 365:    */   
/* 366:    */   public int setBytes(int index, InputStream in, int length)
/* 367:    */     throws IOException
/* 368:    */   {
/* 369:363 */     checkIndex(index, length);
/* 370:364 */     byte[] tmp = new byte[length];
/* 371:365 */     int readBytes = in.read(tmp);
/* 372:366 */     if (readBytes <= 0) {
/* 373:367 */       return readBytes;
/* 374:    */     }
/* 375:369 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 376:370 */     tmpBuf.clear().position(idx(index));
/* 377:371 */     tmpBuf.put(tmp, 0, readBytes);
/* 378:372 */     return readBytes;
/* 379:    */   }
/* 380:    */   
/* 381:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 382:    */     throws IOException
/* 383:    */   {
/* 384:377 */     checkIndex(index, length);
/* 385:378 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 386:379 */     index = idx(index);
/* 387:380 */     tmpBuf.clear().position(index).limit(index + length);
/* 388:    */     try
/* 389:    */     {
/* 390:382 */       return in.read(tmpBuf);
/* 391:    */     }
/* 392:    */     catch (ClosedChannelException ignored) {}
/* 393:384 */     return -1;
/* 394:    */   }
/* 395:    */   
/* 396:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 397:    */     throws IOException
/* 398:    */   {
/* 399:390 */     checkIndex(index, length);
/* 400:391 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 401:392 */     index = idx(index);
/* 402:393 */     tmpBuf.clear().position(index).limit(index + length);
/* 403:    */     try
/* 404:    */     {
/* 405:395 */       return in.read(tmpBuf, position);
/* 406:    */     }
/* 407:    */     catch (ClosedChannelException ignored) {}
/* 408:397 */     return -1;
/* 409:    */   }
/* 410:    */   
/* 411:    */   public ByteBuf copy(int index, int length)
/* 412:    */   {
/* 413:403 */     checkIndex(index, length);
/* 414:404 */     ByteBuf copy = alloc().directBuffer(length, maxCapacity());
/* 415:405 */     copy.writeBytes(this, index, length);
/* 416:406 */     return copy;
/* 417:    */   }
/* 418:    */   
/* 419:    */   public int nioBufferCount()
/* 420:    */   {
/* 421:411 */     return 1;
/* 422:    */   }
/* 423:    */   
/* 424:    */   public ByteBuffer nioBuffer(int index, int length)
/* 425:    */   {
/* 426:416 */     checkIndex(index, length);
/* 427:417 */     index = idx(index);
/* 428:418 */     return ((ByteBuffer)((ByteBuffer)this.memory).duplicate().position(index).limit(index + length)).slice();
/* 429:    */   }
/* 430:    */   
/* 431:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 432:    */   {
/* 433:423 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 434:    */   }
/* 435:    */   
/* 436:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 437:    */   {
/* 438:428 */     checkIndex(index, length);
/* 439:429 */     index = idx(index);
/* 440:430 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 441:    */   }
/* 442:    */   
/* 443:    */   public boolean hasArray()
/* 444:    */   {
/* 445:435 */     return false;
/* 446:    */   }
/* 447:    */   
/* 448:    */   public byte[] array()
/* 449:    */   {
/* 450:440 */     throw new UnsupportedOperationException("direct buffer");
/* 451:    */   }
/* 452:    */   
/* 453:    */   public int arrayOffset()
/* 454:    */   {
/* 455:445 */     throw new UnsupportedOperationException("direct buffer");
/* 456:    */   }
/* 457:    */   
/* 458:    */   public boolean hasMemoryAddress()
/* 459:    */   {
/* 460:450 */     return false;
/* 461:    */   }
/* 462:    */   
/* 463:    */   public long memoryAddress()
/* 464:    */   {
/* 465:455 */     throw new UnsupportedOperationException();
/* 466:    */   }
/* 467:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */