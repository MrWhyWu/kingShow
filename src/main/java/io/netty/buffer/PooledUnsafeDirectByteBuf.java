/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ import java.io.OutputStream;
/*   9:    */ import java.nio.Buffer;
/*  10:    */ import java.nio.ByteBuffer;
/*  11:    */ import java.nio.channels.ClosedChannelException;
/*  12:    */ import java.nio.channels.FileChannel;
/*  13:    */ import java.nio.channels.GatheringByteChannel;
/*  14:    */ import java.nio.channels.ScatteringByteChannel;
/*  15:    */ 
/*  16:    */ final class PooledUnsafeDirectByteBuf
/*  17:    */   extends PooledByteBuf<ByteBuffer>
/*  18:    */ {
/*  19: 32 */   private static final Recycler<PooledUnsafeDirectByteBuf> RECYCLER = new Recycler()
/*  20:    */   {
/*  21:    */     protected PooledUnsafeDirectByteBuf newObject(Recycler.Handle<PooledUnsafeDirectByteBuf> handle)
/*  22:    */     {
/*  23: 35 */       return new PooledUnsafeDirectByteBuf(handle, 0, null);
/*  24:    */     }
/*  25:    */   };
/*  26:    */   private long memoryAddress;
/*  27:    */   
/*  28:    */   static PooledUnsafeDirectByteBuf newInstance(int maxCapacity)
/*  29:    */   {
/*  30: 40 */     PooledUnsafeDirectByteBuf buf = (PooledUnsafeDirectByteBuf)RECYCLER.get();
/*  31: 41 */     buf.reuse(maxCapacity);
/*  32: 42 */     return buf;
/*  33:    */   }
/*  34:    */   
/*  35:    */   private PooledUnsafeDirectByteBuf(Recycler.Handle<PooledUnsafeDirectByteBuf> recyclerHandle, int maxCapacity)
/*  36:    */   {
/*  37: 48 */     super(recyclerHandle, maxCapacity);
/*  38:    */   }
/*  39:    */   
/*  40:    */   void init(PoolChunk<ByteBuffer> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache)
/*  41:    */   {
/*  42: 54 */     super.init(chunk, handle, offset, length, maxLength, cache);
/*  43: 55 */     initMemoryAddress();
/*  44:    */   }
/*  45:    */   
/*  46:    */   void initUnpooled(PoolChunk<ByteBuffer> chunk, int length)
/*  47:    */   {
/*  48: 60 */     super.initUnpooled(chunk, length);
/*  49: 61 */     initMemoryAddress();
/*  50:    */   }
/*  51:    */   
/*  52:    */   private void initMemoryAddress()
/*  53:    */   {
/*  54: 65 */     this.memoryAddress = (PlatformDependent.directBufferAddress((ByteBuffer)this.memory) + this.offset);
/*  55:    */   }
/*  56:    */   
/*  57:    */   protected ByteBuffer newInternalNioBuffer(ByteBuffer memory)
/*  58:    */   {
/*  59: 70 */     return memory.duplicate();
/*  60:    */   }
/*  61:    */   
/*  62:    */   public boolean isDirect()
/*  63:    */   {
/*  64: 75 */     return true;
/*  65:    */   }
/*  66:    */   
/*  67:    */   protected byte _getByte(int index)
/*  68:    */   {
/*  69: 80 */     return UnsafeByteBufUtil.getByte(addr(index));
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected short _getShort(int index)
/*  73:    */   {
/*  74: 85 */     return UnsafeByteBufUtil.getShort(addr(index));
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected short _getShortLE(int index)
/*  78:    */   {
/*  79: 90 */     return UnsafeByteBufUtil.getShortLE(addr(index));
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected int _getUnsignedMedium(int index)
/*  83:    */   {
/*  84: 95 */     return UnsafeByteBufUtil.getUnsignedMedium(addr(index));
/*  85:    */   }
/*  86:    */   
/*  87:    */   protected int _getUnsignedMediumLE(int index)
/*  88:    */   {
/*  89:100 */     return UnsafeByteBufUtil.getUnsignedMediumLE(addr(index));
/*  90:    */   }
/*  91:    */   
/*  92:    */   protected int _getInt(int index)
/*  93:    */   {
/*  94:105 */     return UnsafeByteBufUtil.getInt(addr(index));
/*  95:    */   }
/*  96:    */   
/*  97:    */   protected int _getIntLE(int index)
/*  98:    */   {
/*  99:110 */     return UnsafeByteBufUtil.getIntLE(addr(index));
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected long _getLong(int index)
/* 103:    */   {
/* 104:115 */     return UnsafeByteBufUtil.getLong(addr(index));
/* 105:    */   }
/* 106:    */   
/* 107:    */   protected long _getLongLE(int index)
/* 108:    */   {
/* 109:120 */     return UnsafeByteBufUtil.getLongLE(addr(index));
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 113:    */   {
/* 114:125 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, dst, dstIndex, length);
/* 115:126 */     return this;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 119:    */   {
/* 120:131 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, dst, dstIndex, length);
/* 121:132 */     return this;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 125:    */   {
/* 126:137 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, dst);
/* 127:138 */     return this;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 131:    */   {
/* 132:143 */     int length = dst.remaining();
/* 133:144 */     checkReadableBytes(length);
/* 134:145 */     getBytes(this.readerIndex, dst);
/* 135:146 */     this.readerIndex += length;
/* 136:147 */     return this;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 140:    */     throws IOException
/* 141:    */   {
/* 142:152 */     UnsafeByteBufUtil.getBytes(this, addr(index), index, out, length);
/* 143:153 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 147:    */     throws IOException
/* 148:    */   {
/* 149:158 */     return getBytes(index, out, length, false);
/* 150:    */   }
/* 151:    */   
/* 152:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 153:    */     throws IOException
/* 154:    */   {
/* 155:162 */     checkIndex(index, length);
/* 156:163 */     if (length == 0) {
/* 157:164 */       return 0;
/* 158:    */     }
/* 159:    */     ByteBuffer tmpBuf;
/* 160:    */     ByteBuffer tmpBuf;
/* 161:168 */     if (internal) {
/* 162:169 */       tmpBuf = internalNioBuffer();
/* 163:    */     } else {
/* 164:171 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 165:    */     }
/* 166:173 */     index = idx(index);
/* 167:174 */     tmpBuf.clear().position(index).limit(index + length);
/* 168:175 */     return out.write(tmpBuf);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 172:    */     throws IOException
/* 173:    */   {
/* 174:180 */     return getBytes(index, out, position, length, false);
/* 175:    */   }
/* 176:    */   
/* 177:    */   private int getBytes(int index, FileChannel out, long position, int length, boolean internal)
/* 178:    */     throws IOException
/* 179:    */   {
/* 180:184 */     checkIndex(index, length);
/* 181:185 */     if (length == 0) {
/* 182:186 */       return 0;
/* 183:    */     }
/* 184:189 */     ByteBuffer tmpBuf = internal ? internalNioBuffer() : ((ByteBuffer)this.memory).duplicate();
/* 185:190 */     index = idx(index);
/* 186:191 */     tmpBuf.clear().position(index).limit(index + length);
/* 187:192 */     return out.write(tmpBuf, position);
/* 188:    */   }
/* 189:    */   
/* 190:    */   public int readBytes(GatheringByteChannel out, int length)
/* 191:    */     throws IOException
/* 192:    */   {
/* 193:198 */     checkReadableBytes(length);
/* 194:199 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 195:200 */     this.readerIndex += readBytes;
/* 196:201 */     return readBytes;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public int readBytes(FileChannel out, long position, int length)
/* 200:    */     throws IOException
/* 201:    */   {
/* 202:207 */     checkReadableBytes(length);
/* 203:208 */     int readBytes = getBytes(this.readerIndex, out, position, length, true);
/* 204:209 */     this.readerIndex += readBytes;
/* 205:210 */     return readBytes;
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected void _setByte(int index, int value)
/* 209:    */   {
/* 210:215 */     UnsafeByteBufUtil.setByte(addr(index), (byte)value);
/* 211:    */   }
/* 212:    */   
/* 213:    */   protected void _setShort(int index, int value)
/* 214:    */   {
/* 215:220 */     UnsafeByteBufUtil.setShort(addr(index), value);
/* 216:    */   }
/* 217:    */   
/* 218:    */   protected void _setShortLE(int index, int value)
/* 219:    */   {
/* 220:225 */     UnsafeByteBufUtil.setShortLE(addr(index), value);
/* 221:    */   }
/* 222:    */   
/* 223:    */   protected void _setMedium(int index, int value)
/* 224:    */   {
/* 225:230 */     UnsafeByteBufUtil.setMedium(addr(index), value);
/* 226:    */   }
/* 227:    */   
/* 228:    */   protected void _setMediumLE(int index, int value)
/* 229:    */   {
/* 230:235 */     UnsafeByteBufUtil.setMediumLE(addr(index), value);
/* 231:    */   }
/* 232:    */   
/* 233:    */   protected void _setInt(int index, int value)
/* 234:    */   {
/* 235:240 */     UnsafeByteBufUtil.setInt(addr(index), value);
/* 236:    */   }
/* 237:    */   
/* 238:    */   protected void _setIntLE(int index, int value)
/* 239:    */   {
/* 240:245 */     UnsafeByteBufUtil.setIntLE(addr(index), value);
/* 241:    */   }
/* 242:    */   
/* 243:    */   protected void _setLong(int index, long value)
/* 244:    */   {
/* 245:250 */     UnsafeByteBufUtil.setLong(addr(index), value);
/* 246:    */   }
/* 247:    */   
/* 248:    */   protected void _setLongLE(int index, long value)
/* 249:    */   {
/* 250:255 */     UnsafeByteBufUtil.setLongLE(addr(index), value);
/* 251:    */   }
/* 252:    */   
/* 253:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 254:    */   {
/* 255:260 */     UnsafeByteBufUtil.setBytes(this, addr(index), index, src, srcIndex, length);
/* 256:261 */     return this;
/* 257:    */   }
/* 258:    */   
/* 259:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 260:    */   {
/* 261:266 */     UnsafeByteBufUtil.setBytes(this, addr(index), index, src, srcIndex, length);
/* 262:267 */     return this;
/* 263:    */   }
/* 264:    */   
/* 265:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 266:    */   {
/* 267:272 */     UnsafeByteBufUtil.setBytes(this, addr(index), index, src);
/* 268:273 */     return this;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public int setBytes(int index, InputStream in, int length)
/* 272:    */     throws IOException
/* 273:    */   {
/* 274:278 */     return UnsafeByteBufUtil.setBytes(this, addr(index), index, in, length);
/* 275:    */   }
/* 276:    */   
/* 277:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 278:    */     throws IOException
/* 279:    */   {
/* 280:283 */     checkIndex(index, length);
/* 281:284 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 282:285 */     index = idx(index);
/* 283:286 */     tmpBuf.clear().position(index).limit(index + length);
/* 284:    */     try
/* 285:    */     {
/* 286:288 */       return in.read(tmpBuf);
/* 287:    */     }
/* 288:    */     catch (ClosedChannelException ignored) {}
/* 289:290 */     return -1;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 293:    */     throws IOException
/* 294:    */   {
/* 295:296 */     checkIndex(index, length);
/* 296:297 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 297:298 */     index = idx(index);
/* 298:299 */     tmpBuf.clear().position(index).limit(index + length);
/* 299:    */     try
/* 300:    */     {
/* 301:301 */       return in.read(tmpBuf, position);
/* 302:    */     }
/* 303:    */     catch (ClosedChannelException ignored) {}
/* 304:303 */     return -1;
/* 305:    */   }
/* 306:    */   
/* 307:    */   public ByteBuf copy(int index, int length)
/* 308:    */   {
/* 309:309 */     return UnsafeByteBufUtil.copy(this, addr(index), index, length);
/* 310:    */   }
/* 311:    */   
/* 312:    */   public int nioBufferCount()
/* 313:    */   {
/* 314:314 */     return 1;
/* 315:    */   }
/* 316:    */   
/* 317:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 318:    */   {
/* 319:319 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 320:    */   }
/* 321:    */   
/* 322:    */   public ByteBuffer nioBuffer(int index, int length)
/* 323:    */   {
/* 324:324 */     checkIndex(index, length);
/* 325:325 */     index = idx(index);
/* 326:326 */     return ((ByteBuffer)((ByteBuffer)this.memory).duplicate().position(index).limit(index + length)).slice();
/* 327:    */   }
/* 328:    */   
/* 329:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 330:    */   {
/* 331:331 */     checkIndex(index, length);
/* 332:332 */     index = idx(index);
/* 333:333 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 334:    */   }
/* 335:    */   
/* 336:    */   public boolean hasArray()
/* 337:    */   {
/* 338:338 */     return false;
/* 339:    */   }
/* 340:    */   
/* 341:    */   public byte[] array()
/* 342:    */   {
/* 343:343 */     throw new UnsupportedOperationException("direct buffer");
/* 344:    */   }
/* 345:    */   
/* 346:    */   public int arrayOffset()
/* 347:    */   {
/* 348:348 */     throw new UnsupportedOperationException("direct buffer");
/* 349:    */   }
/* 350:    */   
/* 351:    */   public boolean hasMemoryAddress()
/* 352:    */   {
/* 353:353 */     return true;
/* 354:    */   }
/* 355:    */   
/* 356:    */   public long memoryAddress()
/* 357:    */   {
/* 358:358 */     ensureAccessible();
/* 359:359 */     return this.memoryAddress;
/* 360:    */   }
/* 361:    */   
/* 362:    */   private long addr(int index)
/* 363:    */   {
/* 364:363 */     return this.memoryAddress + index;
/* 365:    */   }
/* 366:    */   
/* 367:    */   protected SwappedByteBuf newSwappedByteBuf()
/* 368:    */   {
/* 369:368 */     if (PlatformDependent.isUnaligned()) {
/* 370:370 */       return new UnsafeDirectSwappedByteBuf(this);
/* 371:    */     }
/* 372:372 */     return super.newSwappedByteBuf();
/* 373:    */   }
/* 374:    */   
/* 375:    */   public ByteBuf setZero(int index, int length)
/* 376:    */   {
/* 377:377 */     checkIndex(index, length);
/* 378:378 */     UnsafeByteBufUtil.setZero(addr(index), length);
/* 379:379 */     return this;
/* 380:    */   }
/* 381:    */   
/* 382:    */   public ByteBuf writeZero(int length)
/* 383:    */   {
/* 384:384 */     ensureWritable(length);
/* 385:385 */     int wIndex = this.writerIndex;
/* 386:386 */     UnsafeByteBufUtil.setZero(addr(wIndex), length);
/* 387:387 */     this.writerIndex = (wIndex + length);
/* 388:388 */     return this;
/* 389:    */   }
/* 390:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */