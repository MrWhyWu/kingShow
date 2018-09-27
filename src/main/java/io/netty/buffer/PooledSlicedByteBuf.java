/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ByteProcessor;
/*   4:    */ import io.netty.util.Recycler;
/*   5:    */ import io.netty.util.Recycler.Handle;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ import java.io.OutputStream;
/*   9:    */ import java.nio.ByteBuffer;
/*  10:    */ import java.nio.channels.FileChannel;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ 
/*  14:    */ final class PooledSlicedByteBuf
/*  15:    */   extends AbstractPooledDerivedByteBuf
/*  16:    */ {
/*  17: 35 */   private static final Recycler<PooledSlicedByteBuf> RECYCLER = new Recycler()
/*  18:    */   {
/*  19:    */     protected PooledSlicedByteBuf newObject(Recycler.Handle<PooledSlicedByteBuf> handle)
/*  20:    */     {
/*  21: 38 */       return new PooledSlicedByteBuf(handle, null);
/*  22:    */     }
/*  23:    */   };
/*  24:    */   int adjustment;
/*  25:    */   
/*  26:    */   static PooledSlicedByteBuf newInstance(AbstractByteBuf unwrapped, ByteBuf wrapped, int index, int length)
/*  27:    */   {
/*  28: 44 */     AbstractUnpooledSlicedByteBuf.checkSliceOutOfBounds(index, length, unwrapped);
/*  29: 45 */     return newInstance0(unwrapped, wrapped, index, length);
/*  30:    */   }
/*  31:    */   
/*  32:    */   private static PooledSlicedByteBuf newInstance0(AbstractByteBuf unwrapped, ByteBuf wrapped, int adjustment, int length)
/*  33:    */   {
/*  34: 50 */     PooledSlicedByteBuf slice = (PooledSlicedByteBuf)RECYCLER.get();
/*  35: 51 */     slice.init(unwrapped, wrapped, 0, length, length);
/*  36: 52 */     slice.discardMarks();
/*  37: 53 */     slice.adjustment = adjustment;
/*  38:    */     
/*  39: 55 */     return slice;
/*  40:    */   }
/*  41:    */   
/*  42:    */   private PooledSlicedByteBuf(Recycler.Handle<PooledSlicedByteBuf> handle)
/*  43:    */   {
/*  44: 61 */     super(handle);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int capacity()
/*  48:    */   {
/*  49: 66 */     return maxCapacity();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ByteBuf capacity(int newCapacity)
/*  53:    */   {
/*  54: 71 */     throw new UnsupportedOperationException("sliced buffer");
/*  55:    */   }
/*  56:    */   
/*  57:    */   public int arrayOffset()
/*  58:    */   {
/*  59: 76 */     return idx(unwrap().arrayOffset());
/*  60:    */   }
/*  61:    */   
/*  62:    */   public long memoryAddress()
/*  63:    */   {
/*  64: 81 */     return unwrap().memoryAddress() + this.adjustment;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public ByteBuffer nioBuffer(int index, int length)
/*  68:    */   {
/*  69: 86 */     checkIndex0(index, length);
/*  70: 87 */     return unwrap().nioBuffer(idx(index), length);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ByteBuffer[] nioBuffers(int index, int length)
/*  74:    */   {
/*  75: 92 */     checkIndex0(index, length);
/*  76: 93 */     return unwrap().nioBuffers(idx(index), length);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ByteBuf copy(int index, int length)
/*  80:    */   {
/*  81: 98 */     checkIndex0(index, length);
/*  82: 99 */     return unwrap().copy(idx(index), length);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public ByteBuf slice(int index, int length)
/*  86:    */   {
/*  87:104 */     checkIndex0(index, length);
/*  88:105 */     return super.slice(idx(index), length);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public ByteBuf retainedSlice(int index, int length)
/*  92:    */   {
/*  93:110 */     checkIndex0(index, length);
/*  94:111 */     return newInstance0(unwrap(), this, idx(index), length);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public ByteBuf duplicate()
/*  98:    */   {
/*  99:116 */     return duplicate0().setIndex(idx(readerIndex()), idx(writerIndex()));
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ByteBuf retainedDuplicate()
/* 103:    */   {
/* 104:121 */     return PooledDuplicatedByteBuf.newInstance(unwrap(), this, idx(readerIndex()), idx(writerIndex()));
/* 105:    */   }
/* 106:    */   
/* 107:    */   public byte getByte(int index)
/* 108:    */   {
/* 109:126 */     checkIndex0(index, 1);
/* 110:127 */     return unwrap().getByte(idx(index));
/* 111:    */   }
/* 112:    */   
/* 113:    */   protected byte _getByte(int index)
/* 114:    */   {
/* 115:132 */     return unwrap()._getByte(idx(index));
/* 116:    */   }
/* 117:    */   
/* 118:    */   public short getShort(int index)
/* 119:    */   {
/* 120:137 */     checkIndex0(index, 2);
/* 121:138 */     return unwrap().getShort(idx(index));
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected short _getShort(int index)
/* 125:    */   {
/* 126:143 */     return unwrap()._getShort(idx(index));
/* 127:    */   }
/* 128:    */   
/* 129:    */   public short getShortLE(int index)
/* 130:    */   {
/* 131:148 */     checkIndex0(index, 2);
/* 132:149 */     return unwrap().getShortLE(idx(index));
/* 133:    */   }
/* 134:    */   
/* 135:    */   protected short _getShortLE(int index)
/* 136:    */   {
/* 137:154 */     return unwrap()._getShortLE(idx(index));
/* 138:    */   }
/* 139:    */   
/* 140:    */   public int getUnsignedMedium(int index)
/* 141:    */   {
/* 142:159 */     checkIndex0(index, 3);
/* 143:160 */     return unwrap().getUnsignedMedium(idx(index));
/* 144:    */   }
/* 145:    */   
/* 146:    */   protected int _getUnsignedMedium(int index)
/* 147:    */   {
/* 148:165 */     return unwrap()._getUnsignedMedium(idx(index));
/* 149:    */   }
/* 150:    */   
/* 151:    */   public int getUnsignedMediumLE(int index)
/* 152:    */   {
/* 153:170 */     checkIndex0(index, 3);
/* 154:171 */     return unwrap().getUnsignedMediumLE(idx(index));
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected int _getUnsignedMediumLE(int index)
/* 158:    */   {
/* 159:176 */     return unwrap()._getUnsignedMediumLE(idx(index));
/* 160:    */   }
/* 161:    */   
/* 162:    */   public int getInt(int index)
/* 163:    */   {
/* 164:181 */     checkIndex0(index, 4);
/* 165:182 */     return unwrap().getInt(idx(index));
/* 166:    */   }
/* 167:    */   
/* 168:    */   protected int _getInt(int index)
/* 169:    */   {
/* 170:187 */     return unwrap()._getInt(idx(index));
/* 171:    */   }
/* 172:    */   
/* 173:    */   public int getIntLE(int index)
/* 174:    */   {
/* 175:192 */     checkIndex0(index, 4);
/* 176:193 */     return unwrap().getIntLE(idx(index));
/* 177:    */   }
/* 178:    */   
/* 179:    */   protected int _getIntLE(int index)
/* 180:    */   {
/* 181:198 */     return unwrap()._getIntLE(idx(index));
/* 182:    */   }
/* 183:    */   
/* 184:    */   public long getLong(int index)
/* 185:    */   {
/* 186:203 */     checkIndex0(index, 8);
/* 187:204 */     return unwrap().getLong(idx(index));
/* 188:    */   }
/* 189:    */   
/* 190:    */   protected long _getLong(int index)
/* 191:    */   {
/* 192:209 */     return unwrap()._getLong(idx(index));
/* 193:    */   }
/* 194:    */   
/* 195:    */   public long getLongLE(int index)
/* 196:    */   {
/* 197:214 */     checkIndex0(index, 8);
/* 198:215 */     return unwrap().getLongLE(idx(index));
/* 199:    */   }
/* 200:    */   
/* 201:    */   protected long _getLongLE(int index)
/* 202:    */   {
/* 203:220 */     return unwrap()._getLongLE(idx(index));
/* 204:    */   }
/* 205:    */   
/* 206:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 207:    */   {
/* 208:225 */     checkIndex0(index, length);
/* 209:226 */     unwrap().getBytes(idx(index), dst, dstIndex, length);
/* 210:227 */     return this;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 214:    */   {
/* 215:232 */     checkIndex0(index, length);
/* 216:233 */     unwrap().getBytes(idx(index), dst, dstIndex, length);
/* 217:234 */     return this;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 221:    */   {
/* 222:239 */     checkIndex0(index, dst.remaining());
/* 223:240 */     unwrap().getBytes(idx(index), dst);
/* 224:241 */     return this;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public ByteBuf setByte(int index, int value)
/* 228:    */   {
/* 229:246 */     checkIndex0(index, 1);
/* 230:247 */     unwrap().setByte(idx(index), value);
/* 231:248 */     return this;
/* 232:    */   }
/* 233:    */   
/* 234:    */   protected void _setByte(int index, int value)
/* 235:    */   {
/* 236:253 */     unwrap()._setByte(idx(index), value);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public ByteBuf setShort(int index, int value)
/* 240:    */   {
/* 241:258 */     checkIndex0(index, 2);
/* 242:259 */     unwrap().setShort(idx(index), value);
/* 243:260 */     return this;
/* 244:    */   }
/* 245:    */   
/* 246:    */   protected void _setShort(int index, int value)
/* 247:    */   {
/* 248:265 */     unwrap()._setShort(idx(index), value);
/* 249:    */   }
/* 250:    */   
/* 251:    */   public ByteBuf setShortLE(int index, int value)
/* 252:    */   {
/* 253:270 */     checkIndex0(index, 2);
/* 254:271 */     unwrap().setShortLE(idx(index), value);
/* 255:272 */     return this;
/* 256:    */   }
/* 257:    */   
/* 258:    */   protected void _setShortLE(int index, int value)
/* 259:    */   {
/* 260:277 */     unwrap()._setShortLE(idx(index), value);
/* 261:    */   }
/* 262:    */   
/* 263:    */   public ByteBuf setMedium(int index, int value)
/* 264:    */   {
/* 265:282 */     checkIndex0(index, 3);
/* 266:283 */     unwrap().setMedium(idx(index), value);
/* 267:284 */     return this;
/* 268:    */   }
/* 269:    */   
/* 270:    */   protected void _setMedium(int index, int value)
/* 271:    */   {
/* 272:289 */     unwrap()._setMedium(idx(index), value);
/* 273:    */   }
/* 274:    */   
/* 275:    */   public ByteBuf setMediumLE(int index, int value)
/* 276:    */   {
/* 277:294 */     checkIndex0(index, 3);
/* 278:295 */     unwrap().setMediumLE(idx(index), value);
/* 279:296 */     return this;
/* 280:    */   }
/* 281:    */   
/* 282:    */   protected void _setMediumLE(int index, int value)
/* 283:    */   {
/* 284:301 */     unwrap()._setMediumLE(idx(index), value);
/* 285:    */   }
/* 286:    */   
/* 287:    */   public ByteBuf setInt(int index, int value)
/* 288:    */   {
/* 289:306 */     checkIndex0(index, 4);
/* 290:307 */     unwrap().setInt(idx(index), value);
/* 291:308 */     return this;
/* 292:    */   }
/* 293:    */   
/* 294:    */   protected void _setInt(int index, int value)
/* 295:    */   {
/* 296:313 */     unwrap()._setInt(idx(index), value);
/* 297:    */   }
/* 298:    */   
/* 299:    */   public ByteBuf setIntLE(int index, int value)
/* 300:    */   {
/* 301:318 */     checkIndex0(index, 4);
/* 302:319 */     unwrap().setIntLE(idx(index), value);
/* 303:320 */     return this;
/* 304:    */   }
/* 305:    */   
/* 306:    */   protected void _setIntLE(int index, int value)
/* 307:    */   {
/* 308:325 */     unwrap()._setIntLE(idx(index), value);
/* 309:    */   }
/* 310:    */   
/* 311:    */   public ByteBuf setLong(int index, long value)
/* 312:    */   {
/* 313:330 */     checkIndex0(index, 8);
/* 314:331 */     unwrap().setLong(idx(index), value);
/* 315:332 */     return this;
/* 316:    */   }
/* 317:    */   
/* 318:    */   protected void _setLong(int index, long value)
/* 319:    */   {
/* 320:337 */     unwrap()._setLong(idx(index), value);
/* 321:    */   }
/* 322:    */   
/* 323:    */   public ByteBuf setLongLE(int index, long value)
/* 324:    */   {
/* 325:342 */     checkIndex0(index, 8);
/* 326:343 */     unwrap().setLongLE(idx(index), value);
/* 327:344 */     return this;
/* 328:    */   }
/* 329:    */   
/* 330:    */   protected void _setLongLE(int index, long value)
/* 331:    */   {
/* 332:349 */     unwrap().setLongLE(idx(index), value);
/* 333:    */   }
/* 334:    */   
/* 335:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 336:    */   {
/* 337:354 */     checkIndex0(index, length);
/* 338:355 */     unwrap().setBytes(idx(index), src, srcIndex, length);
/* 339:356 */     return this;
/* 340:    */   }
/* 341:    */   
/* 342:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 343:    */   {
/* 344:361 */     checkIndex0(index, length);
/* 345:362 */     unwrap().setBytes(idx(index), src, srcIndex, length);
/* 346:363 */     return this;
/* 347:    */   }
/* 348:    */   
/* 349:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 350:    */   {
/* 351:368 */     checkIndex0(index, src.remaining());
/* 352:369 */     unwrap().setBytes(idx(index), src);
/* 353:370 */     return this;
/* 354:    */   }
/* 355:    */   
/* 356:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 357:    */     throws IOException
/* 358:    */   {
/* 359:376 */     checkIndex0(index, length);
/* 360:377 */     unwrap().getBytes(idx(index), out, length);
/* 361:378 */     return this;
/* 362:    */   }
/* 363:    */   
/* 364:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 365:    */     throws IOException
/* 366:    */   {
/* 367:384 */     checkIndex0(index, length);
/* 368:385 */     return unwrap().getBytes(idx(index), out, length);
/* 369:    */   }
/* 370:    */   
/* 371:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 372:    */     throws IOException
/* 373:    */   {
/* 374:391 */     checkIndex0(index, length);
/* 375:392 */     return unwrap().getBytes(idx(index), out, position, length);
/* 376:    */   }
/* 377:    */   
/* 378:    */   public int setBytes(int index, InputStream in, int length)
/* 379:    */     throws IOException
/* 380:    */   {
/* 381:398 */     checkIndex0(index, length);
/* 382:399 */     return unwrap().setBytes(idx(index), in, length);
/* 383:    */   }
/* 384:    */   
/* 385:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 386:    */     throws IOException
/* 387:    */   {
/* 388:405 */     checkIndex0(index, length);
/* 389:406 */     return unwrap().setBytes(idx(index), in, length);
/* 390:    */   }
/* 391:    */   
/* 392:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 393:    */     throws IOException
/* 394:    */   {
/* 395:412 */     checkIndex0(index, length);
/* 396:413 */     return unwrap().setBytes(idx(index), in, position, length);
/* 397:    */   }
/* 398:    */   
/* 399:    */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 400:    */   {
/* 401:418 */     checkIndex0(index, length);
/* 402:419 */     int ret = unwrap().forEachByte(idx(index), length, processor);
/* 403:420 */     if (ret < this.adjustment) {
/* 404:421 */       return -1;
/* 405:    */     }
/* 406:423 */     return ret - this.adjustment;
/* 407:    */   }
/* 408:    */   
/* 409:    */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 410:    */   {
/* 411:428 */     checkIndex0(index, length);
/* 412:429 */     int ret = unwrap().forEachByteDesc(idx(index), length, processor);
/* 413:430 */     if (ret < this.adjustment) {
/* 414:431 */       return -1;
/* 415:    */     }
/* 416:433 */     return ret - this.adjustment;
/* 417:    */   }
/* 418:    */   
/* 419:    */   private int idx(int index)
/* 420:    */   {
/* 421:437 */     return index + this.adjustment;
/* 422:    */   }
/* 423:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledSlicedByteBuf
 * JD-Core Version:    0.7.0.1
 */