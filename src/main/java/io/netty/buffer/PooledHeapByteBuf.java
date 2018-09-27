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
/*  16:    */ class PooledHeapByteBuf
/*  17:    */   extends PooledByteBuf<byte[]>
/*  18:    */ {
/*  19: 31 */   private static final Recycler<PooledHeapByteBuf> RECYCLER = new Recycler()
/*  20:    */   {
/*  21:    */     protected PooledHeapByteBuf newObject(Recycler.Handle<PooledHeapByteBuf> handle)
/*  22:    */     {
/*  23: 34 */       return new PooledHeapByteBuf(handle, 0);
/*  24:    */     }
/*  25:    */   };
/*  26:    */   
/*  27:    */   static PooledHeapByteBuf newInstance(int maxCapacity)
/*  28:    */   {
/*  29: 39 */     PooledHeapByteBuf buf = (PooledHeapByteBuf)RECYCLER.get();
/*  30: 40 */     buf.reuse(maxCapacity);
/*  31: 41 */     return buf;
/*  32:    */   }
/*  33:    */   
/*  34:    */   PooledHeapByteBuf(Recycler.Handle<? extends PooledHeapByteBuf> recyclerHandle, int maxCapacity)
/*  35:    */   {
/*  36: 45 */     super(recyclerHandle, maxCapacity);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public final boolean isDirect()
/*  40:    */   {
/*  41: 50 */     return false;
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected byte _getByte(int index)
/*  45:    */   {
/*  46: 55 */     return HeapByteBufUtil.getByte((byte[])this.memory, idx(index));
/*  47:    */   }
/*  48:    */   
/*  49:    */   protected short _getShort(int index)
/*  50:    */   {
/*  51: 60 */     return HeapByteBufUtil.getShort((byte[])this.memory, idx(index));
/*  52:    */   }
/*  53:    */   
/*  54:    */   protected short _getShortLE(int index)
/*  55:    */   {
/*  56: 65 */     return HeapByteBufUtil.getShortLE((byte[])this.memory, idx(index));
/*  57:    */   }
/*  58:    */   
/*  59:    */   protected int _getUnsignedMedium(int index)
/*  60:    */   {
/*  61: 70 */     return HeapByteBufUtil.getUnsignedMedium((byte[])this.memory, idx(index));
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected int _getUnsignedMediumLE(int index)
/*  65:    */   {
/*  66: 75 */     return HeapByteBufUtil.getUnsignedMediumLE((byte[])this.memory, idx(index));
/*  67:    */   }
/*  68:    */   
/*  69:    */   protected int _getInt(int index)
/*  70:    */   {
/*  71: 80 */     return HeapByteBufUtil.getInt((byte[])this.memory, idx(index));
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected int _getIntLE(int index)
/*  75:    */   {
/*  76: 85 */     return HeapByteBufUtil.getIntLE((byte[])this.memory, idx(index));
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected long _getLong(int index)
/*  80:    */   {
/*  81: 90 */     return HeapByteBufUtil.getLong((byte[])this.memory, idx(index));
/*  82:    */   }
/*  83:    */   
/*  84:    */   protected long _getLongLE(int index)
/*  85:    */   {
/*  86: 95 */     return HeapByteBufUtil.getLongLE((byte[])this.memory, idx(index));
/*  87:    */   }
/*  88:    */   
/*  89:    */   public final ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  90:    */   {
/*  91:100 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/*  92:101 */     if (dst.hasMemoryAddress()) {
/*  93:102 */       PlatformDependent.copyMemory((byte[])this.memory, idx(index), dst.memoryAddress() + dstIndex, length);
/*  94:103 */     } else if (dst.hasArray()) {
/*  95:104 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/*  96:    */     } else {
/*  97:106 */       dst.setBytes(dstIndex, (byte[])this.memory, idx(index), length);
/*  98:    */     }
/*  99:108 */     return this;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public final ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 103:    */   {
/* 104:113 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 105:114 */     System.arraycopy(this.memory, idx(index), dst, dstIndex, length);
/* 106:115 */     return this;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public final ByteBuf getBytes(int index, ByteBuffer dst)
/* 110:    */   {
/* 111:120 */     checkIndex(index, dst.remaining());
/* 112:121 */     dst.put((byte[])this.memory, idx(index), dst.remaining());
/* 113:122 */     return this;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public final ByteBuf getBytes(int index, OutputStream out, int length)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:127 */     checkIndex(index, length);
/* 120:128 */     out.write((byte[])this.memory, idx(index), length);
/* 121:129 */     return this;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public final int getBytes(int index, GatheringByteChannel out, int length)
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:134 */     return getBytes(index, out, length, false);
/* 128:    */   }
/* 129:    */   
/* 130:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:138 */     checkIndex(index, length);
/* 134:139 */     index = idx(index);
/* 135:    */     ByteBuffer tmpBuf;
/* 136:    */     ByteBuffer tmpBuf;
/* 137:141 */     if (internal) {
/* 138:142 */       tmpBuf = internalNioBuffer();
/* 139:    */     } else {
/* 140:144 */       tmpBuf = ByteBuffer.wrap((byte[])this.memory);
/* 141:    */     }
/* 142:146 */     return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length));
/* 143:    */   }
/* 144:    */   
/* 145:    */   public final int getBytes(int index, FileChannel out, long position, int length)
/* 146:    */     throws IOException
/* 147:    */   {
/* 148:151 */     return getBytes(index, out, position, length, false);
/* 149:    */   }
/* 150:    */   
/* 151:    */   private int getBytes(int index, FileChannel out, long position, int length, boolean internal)
/* 152:    */     throws IOException
/* 153:    */   {
/* 154:155 */     checkIndex(index, length);
/* 155:156 */     index = idx(index);
/* 156:157 */     ByteBuffer tmpBuf = internal ? internalNioBuffer() : ByteBuffer.wrap((byte[])this.memory);
/* 157:158 */     return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length), position);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public final int readBytes(GatheringByteChannel out, int length)
/* 161:    */     throws IOException
/* 162:    */   {
/* 163:163 */     checkReadableBytes(length);
/* 164:164 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 165:165 */     this.readerIndex += readBytes;
/* 166:166 */     return readBytes;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public final int readBytes(FileChannel out, long position, int length)
/* 170:    */     throws IOException
/* 171:    */   {
/* 172:171 */     checkReadableBytes(length);
/* 173:172 */     int readBytes = getBytes(this.readerIndex, out, position, length, true);
/* 174:173 */     this.readerIndex += readBytes;
/* 175:174 */     return readBytes;
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected void _setByte(int index, int value)
/* 179:    */   {
/* 180:179 */     HeapByteBufUtil.setByte((byte[])this.memory, idx(index), value);
/* 181:    */   }
/* 182:    */   
/* 183:    */   protected void _setShort(int index, int value)
/* 184:    */   {
/* 185:184 */     HeapByteBufUtil.setShort((byte[])this.memory, idx(index), value);
/* 186:    */   }
/* 187:    */   
/* 188:    */   protected void _setShortLE(int index, int value)
/* 189:    */   {
/* 190:189 */     HeapByteBufUtil.setShortLE((byte[])this.memory, idx(index), value);
/* 191:    */   }
/* 192:    */   
/* 193:    */   protected void _setMedium(int index, int value)
/* 194:    */   {
/* 195:194 */     HeapByteBufUtil.setMedium((byte[])this.memory, idx(index), value);
/* 196:    */   }
/* 197:    */   
/* 198:    */   protected void _setMediumLE(int index, int value)
/* 199:    */   {
/* 200:199 */     HeapByteBufUtil.setMediumLE((byte[])this.memory, idx(index), value);
/* 201:    */   }
/* 202:    */   
/* 203:    */   protected void _setInt(int index, int value)
/* 204:    */   {
/* 205:204 */     HeapByteBufUtil.setInt((byte[])this.memory, idx(index), value);
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected void _setIntLE(int index, int value)
/* 209:    */   {
/* 210:209 */     HeapByteBufUtil.setIntLE((byte[])this.memory, idx(index), value);
/* 211:    */   }
/* 212:    */   
/* 213:    */   protected void _setLong(int index, long value)
/* 214:    */   {
/* 215:214 */     HeapByteBufUtil.setLong((byte[])this.memory, idx(index), value);
/* 216:    */   }
/* 217:    */   
/* 218:    */   protected void _setLongLE(int index, long value)
/* 219:    */   {
/* 220:219 */     HeapByteBufUtil.setLongLE((byte[])this.memory, idx(index), value);
/* 221:    */   }
/* 222:    */   
/* 223:    */   public final ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 224:    */   {
/* 225:224 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 226:225 */     if (src.hasMemoryAddress()) {
/* 227:226 */       PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, (byte[])this.memory, idx(index), length);
/* 228:227 */     } else if (src.hasArray()) {
/* 229:228 */       setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
/* 230:    */     } else {
/* 231:230 */       src.getBytes(srcIndex, (byte[])this.memory, idx(index), length);
/* 232:    */     }
/* 233:232 */     return this;
/* 234:    */   }
/* 235:    */   
/* 236:    */   public final ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 237:    */   {
/* 238:237 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 239:238 */     System.arraycopy(src, srcIndex, this.memory, idx(index), length);
/* 240:239 */     return this;
/* 241:    */   }
/* 242:    */   
/* 243:    */   public final ByteBuf setBytes(int index, ByteBuffer src)
/* 244:    */   {
/* 245:244 */     int length = src.remaining();
/* 246:245 */     checkIndex(index, length);
/* 247:246 */     src.get((byte[])this.memory, idx(index), length);
/* 248:247 */     return this;
/* 249:    */   }
/* 250:    */   
/* 251:    */   public final int setBytes(int index, InputStream in, int length)
/* 252:    */     throws IOException
/* 253:    */   {
/* 254:252 */     checkIndex(index, length);
/* 255:253 */     return in.read((byte[])this.memory, idx(index), length);
/* 256:    */   }
/* 257:    */   
/* 258:    */   public final int setBytes(int index, ScatteringByteChannel in, int length)
/* 259:    */     throws IOException
/* 260:    */   {
/* 261:258 */     checkIndex(index, length);
/* 262:259 */     index = idx(index);
/* 263:    */     try
/* 264:    */     {
/* 265:261 */       return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length));
/* 266:    */     }
/* 267:    */     catch (ClosedChannelException ignored) {}
/* 268:263 */     return -1;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public final int setBytes(int index, FileChannel in, long position, int length)
/* 272:    */     throws IOException
/* 273:    */   {
/* 274:269 */     checkIndex(index, length);
/* 275:270 */     index = idx(index);
/* 276:    */     try
/* 277:    */     {
/* 278:272 */       return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length), position);
/* 279:    */     }
/* 280:    */     catch (ClosedChannelException ignored) {}
/* 281:274 */     return -1;
/* 282:    */   }
/* 283:    */   
/* 284:    */   public final ByteBuf copy(int index, int length)
/* 285:    */   {
/* 286:280 */     checkIndex(index, length);
/* 287:281 */     ByteBuf copy = alloc().heapBuffer(length, maxCapacity());
/* 288:282 */     copy.writeBytes((byte[])this.memory, idx(index), length);
/* 289:283 */     return copy;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public final int nioBufferCount()
/* 293:    */   {
/* 294:288 */     return 1;
/* 295:    */   }
/* 296:    */   
/* 297:    */   public final ByteBuffer[] nioBuffers(int index, int length)
/* 298:    */   {
/* 299:293 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 300:    */   }
/* 301:    */   
/* 302:    */   public final ByteBuffer nioBuffer(int index, int length)
/* 303:    */   {
/* 304:298 */     checkIndex(index, length);
/* 305:299 */     index = idx(index);
/* 306:300 */     ByteBuffer buf = ByteBuffer.wrap((byte[])this.memory, index, length);
/* 307:301 */     return buf.slice();
/* 308:    */   }
/* 309:    */   
/* 310:    */   public final ByteBuffer internalNioBuffer(int index, int length)
/* 311:    */   {
/* 312:306 */     checkIndex(index, length);
/* 313:307 */     index = idx(index);
/* 314:308 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 315:    */   }
/* 316:    */   
/* 317:    */   public final boolean hasArray()
/* 318:    */   {
/* 319:313 */     return true;
/* 320:    */   }
/* 321:    */   
/* 322:    */   public final byte[] array()
/* 323:    */   {
/* 324:318 */     ensureAccessible();
/* 325:319 */     return (byte[])this.memory;
/* 326:    */   }
/* 327:    */   
/* 328:    */   public final int arrayOffset()
/* 329:    */   {
/* 330:324 */     return this.offset;
/* 331:    */   }
/* 332:    */   
/* 333:    */   public final boolean hasMemoryAddress()
/* 334:    */   {
/* 335:329 */     return false;
/* 336:    */   }
/* 337:    */   
/* 338:    */   public final long memoryAddress()
/* 339:    */   {
/* 340:334 */     throw new UnsupportedOperationException();
/* 341:    */   }
/* 342:    */   
/* 343:    */   protected final ByteBuffer newInternalNioBuffer(byte[] memory)
/* 344:    */   {
/* 345:339 */     return ByteBuffer.wrap(memory);
/* 346:    */   }
/* 347:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledHeapByteBuf
 * JD-Core Version:    0.7.0.1
 */