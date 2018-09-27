/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ByteProcessor;
/*   4:    */ import io.netty.util.internal.MathUtil;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.channels.FileChannel;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ import java.nio.charset.Charset;
/*  14:    */ 
/*  15:    */ abstract class AbstractUnpooledSlicedByteBuf
/*  16:    */   extends AbstractDerivedByteBuf
/*  17:    */ {
/*  18:    */   private final ByteBuf buffer;
/*  19:    */   private final int adjustment;
/*  20:    */   
/*  21:    */   AbstractUnpooledSlicedByteBuf(ByteBuf buffer, int index, int length)
/*  22:    */   {
/*  23: 37 */     super(length);
/*  24: 38 */     checkSliceOutOfBounds(index, length, buffer);
/*  25: 40 */     if ((buffer instanceof AbstractUnpooledSlicedByteBuf))
/*  26:    */     {
/*  27: 41 */       this.buffer = ((AbstractUnpooledSlicedByteBuf)buffer).buffer;
/*  28: 42 */       this.adjustment = (((AbstractUnpooledSlicedByteBuf)buffer).adjustment + index);
/*  29:    */     }
/*  30: 43 */     else if ((buffer instanceof DuplicatedByteBuf))
/*  31:    */     {
/*  32: 44 */       this.buffer = buffer.unwrap();
/*  33: 45 */       this.adjustment = index;
/*  34:    */     }
/*  35:    */     else
/*  36:    */     {
/*  37: 47 */       this.buffer = buffer;
/*  38: 48 */       this.adjustment = index;
/*  39:    */     }
/*  40: 51 */     initLength(length);
/*  41: 52 */     writerIndex(length);
/*  42:    */   }
/*  43:    */   
/*  44:    */   void initLength(int length) {}
/*  45:    */   
/*  46:    */   int length()
/*  47:    */   {
/*  48: 63 */     return capacity();
/*  49:    */   }
/*  50:    */   
/*  51:    */   public ByteBuf unwrap()
/*  52:    */   {
/*  53: 68 */     return this.buffer;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ByteBufAllocator alloc()
/*  57:    */   {
/*  58: 73 */     return unwrap().alloc();
/*  59:    */   }
/*  60:    */   
/*  61:    */   @Deprecated
/*  62:    */   public ByteOrder order()
/*  63:    */   {
/*  64: 79 */     return unwrap().order();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public boolean isDirect()
/*  68:    */   {
/*  69: 84 */     return unwrap().isDirect();
/*  70:    */   }
/*  71:    */   
/*  72:    */   public ByteBuf capacity(int newCapacity)
/*  73:    */   {
/*  74: 89 */     throw new UnsupportedOperationException("sliced buffer");
/*  75:    */   }
/*  76:    */   
/*  77:    */   public boolean hasArray()
/*  78:    */   {
/*  79: 94 */     return unwrap().hasArray();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public byte[] array()
/*  83:    */   {
/*  84: 99 */     return unwrap().array();
/*  85:    */   }
/*  86:    */   
/*  87:    */   public int arrayOffset()
/*  88:    */   {
/*  89:104 */     return idx(unwrap().arrayOffset());
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean hasMemoryAddress()
/*  93:    */   {
/*  94:109 */     return unwrap().hasMemoryAddress();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public long memoryAddress()
/*  98:    */   {
/*  99:114 */     return unwrap().memoryAddress() + this.adjustment;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public byte getByte(int index)
/* 103:    */   {
/* 104:119 */     checkIndex0(index, 1);
/* 105:120 */     return unwrap().getByte(idx(index));
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected byte _getByte(int index)
/* 109:    */   {
/* 110:125 */     return unwrap().getByte(idx(index));
/* 111:    */   }
/* 112:    */   
/* 113:    */   public short getShort(int index)
/* 114:    */   {
/* 115:130 */     checkIndex0(index, 2);
/* 116:131 */     return unwrap().getShort(idx(index));
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected short _getShort(int index)
/* 120:    */   {
/* 121:136 */     return unwrap().getShort(idx(index));
/* 122:    */   }
/* 123:    */   
/* 124:    */   public short getShortLE(int index)
/* 125:    */   {
/* 126:141 */     checkIndex0(index, 2);
/* 127:142 */     return unwrap().getShortLE(idx(index));
/* 128:    */   }
/* 129:    */   
/* 130:    */   protected short _getShortLE(int index)
/* 131:    */   {
/* 132:147 */     return unwrap().getShortLE(idx(index));
/* 133:    */   }
/* 134:    */   
/* 135:    */   public int getUnsignedMedium(int index)
/* 136:    */   {
/* 137:152 */     checkIndex0(index, 3);
/* 138:153 */     return unwrap().getUnsignedMedium(idx(index));
/* 139:    */   }
/* 140:    */   
/* 141:    */   protected int _getUnsignedMedium(int index)
/* 142:    */   {
/* 143:158 */     return unwrap().getUnsignedMedium(idx(index));
/* 144:    */   }
/* 145:    */   
/* 146:    */   public int getUnsignedMediumLE(int index)
/* 147:    */   {
/* 148:163 */     checkIndex0(index, 3);
/* 149:164 */     return unwrap().getUnsignedMediumLE(idx(index));
/* 150:    */   }
/* 151:    */   
/* 152:    */   protected int _getUnsignedMediumLE(int index)
/* 153:    */   {
/* 154:169 */     return unwrap().getUnsignedMediumLE(idx(index));
/* 155:    */   }
/* 156:    */   
/* 157:    */   public int getInt(int index)
/* 158:    */   {
/* 159:174 */     checkIndex0(index, 4);
/* 160:175 */     return unwrap().getInt(idx(index));
/* 161:    */   }
/* 162:    */   
/* 163:    */   protected int _getInt(int index)
/* 164:    */   {
/* 165:180 */     return unwrap().getInt(idx(index));
/* 166:    */   }
/* 167:    */   
/* 168:    */   public int getIntLE(int index)
/* 169:    */   {
/* 170:185 */     checkIndex0(index, 4);
/* 171:186 */     return unwrap().getIntLE(idx(index));
/* 172:    */   }
/* 173:    */   
/* 174:    */   protected int _getIntLE(int index)
/* 175:    */   {
/* 176:191 */     return unwrap().getIntLE(idx(index));
/* 177:    */   }
/* 178:    */   
/* 179:    */   public long getLong(int index)
/* 180:    */   {
/* 181:196 */     checkIndex0(index, 8);
/* 182:197 */     return unwrap().getLong(idx(index));
/* 183:    */   }
/* 184:    */   
/* 185:    */   protected long _getLong(int index)
/* 186:    */   {
/* 187:202 */     return unwrap().getLong(idx(index));
/* 188:    */   }
/* 189:    */   
/* 190:    */   public long getLongLE(int index)
/* 191:    */   {
/* 192:207 */     checkIndex0(index, 8);
/* 193:208 */     return unwrap().getLongLE(idx(index));
/* 194:    */   }
/* 195:    */   
/* 196:    */   protected long _getLongLE(int index)
/* 197:    */   {
/* 198:213 */     return unwrap().getLongLE(idx(index));
/* 199:    */   }
/* 200:    */   
/* 201:    */   public ByteBuf duplicate()
/* 202:    */   {
/* 203:218 */     return unwrap().duplicate().setIndex(idx(readerIndex()), idx(writerIndex()));
/* 204:    */   }
/* 205:    */   
/* 206:    */   public ByteBuf copy(int index, int length)
/* 207:    */   {
/* 208:223 */     checkIndex0(index, length);
/* 209:224 */     return unwrap().copy(idx(index), length);
/* 210:    */   }
/* 211:    */   
/* 212:    */   public ByteBuf slice(int index, int length)
/* 213:    */   {
/* 214:229 */     checkIndex0(index, length);
/* 215:230 */     return unwrap().slice(idx(index), length);
/* 216:    */   }
/* 217:    */   
/* 218:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 219:    */   {
/* 220:235 */     checkIndex0(index, length);
/* 221:236 */     unwrap().getBytes(idx(index), dst, dstIndex, length);
/* 222:237 */     return this;
/* 223:    */   }
/* 224:    */   
/* 225:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 226:    */   {
/* 227:242 */     checkIndex0(index, length);
/* 228:243 */     unwrap().getBytes(idx(index), dst, dstIndex, length);
/* 229:244 */     return this;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 233:    */   {
/* 234:249 */     checkIndex0(index, dst.remaining());
/* 235:250 */     unwrap().getBytes(idx(index), dst);
/* 236:251 */     return this;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public ByteBuf setByte(int index, int value)
/* 240:    */   {
/* 241:256 */     checkIndex0(index, 1);
/* 242:257 */     unwrap().setByte(idx(index), value);
/* 243:258 */     return this;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public CharSequence getCharSequence(int index, int length, Charset charset)
/* 247:    */   {
/* 248:263 */     checkIndex0(index, length);
/* 249:264 */     return unwrap().getCharSequence(idx(index), length, charset);
/* 250:    */   }
/* 251:    */   
/* 252:    */   protected void _setByte(int index, int value)
/* 253:    */   {
/* 254:269 */     unwrap().setByte(idx(index), value);
/* 255:    */   }
/* 256:    */   
/* 257:    */   public ByteBuf setShort(int index, int value)
/* 258:    */   {
/* 259:274 */     checkIndex0(index, 2);
/* 260:275 */     unwrap().setShort(idx(index), value);
/* 261:276 */     return this;
/* 262:    */   }
/* 263:    */   
/* 264:    */   protected void _setShort(int index, int value)
/* 265:    */   {
/* 266:281 */     unwrap().setShort(idx(index), value);
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ByteBuf setShortLE(int index, int value)
/* 270:    */   {
/* 271:286 */     checkIndex0(index, 2);
/* 272:287 */     unwrap().setShortLE(idx(index), value);
/* 273:288 */     return this;
/* 274:    */   }
/* 275:    */   
/* 276:    */   protected void _setShortLE(int index, int value)
/* 277:    */   {
/* 278:293 */     unwrap().setShortLE(idx(index), value);
/* 279:    */   }
/* 280:    */   
/* 281:    */   public ByteBuf setMedium(int index, int value)
/* 282:    */   {
/* 283:298 */     checkIndex0(index, 3);
/* 284:299 */     unwrap().setMedium(idx(index), value);
/* 285:300 */     return this;
/* 286:    */   }
/* 287:    */   
/* 288:    */   protected void _setMedium(int index, int value)
/* 289:    */   {
/* 290:305 */     unwrap().setMedium(idx(index), value);
/* 291:    */   }
/* 292:    */   
/* 293:    */   public ByteBuf setMediumLE(int index, int value)
/* 294:    */   {
/* 295:310 */     checkIndex0(index, 3);
/* 296:311 */     unwrap().setMediumLE(idx(index), value);
/* 297:312 */     return this;
/* 298:    */   }
/* 299:    */   
/* 300:    */   protected void _setMediumLE(int index, int value)
/* 301:    */   {
/* 302:317 */     unwrap().setMediumLE(idx(index), value);
/* 303:    */   }
/* 304:    */   
/* 305:    */   public ByteBuf setInt(int index, int value)
/* 306:    */   {
/* 307:322 */     checkIndex0(index, 4);
/* 308:323 */     unwrap().setInt(idx(index), value);
/* 309:324 */     return this;
/* 310:    */   }
/* 311:    */   
/* 312:    */   protected void _setInt(int index, int value)
/* 313:    */   {
/* 314:329 */     unwrap().setInt(idx(index), value);
/* 315:    */   }
/* 316:    */   
/* 317:    */   public ByteBuf setIntLE(int index, int value)
/* 318:    */   {
/* 319:334 */     checkIndex0(index, 4);
/* 320:335 */     unwrap().setIntLE(idx(index), value);
/* 321:336 */     return this;
/* 322:    */   }
/* 323:    */   
/* 324:    */   protected void _setIntLE(int index, int value)
/* 325:    */   {
/* 326:341 */     unwrap().setIntLE(idx(index), value);
/* 327:    */   }
/* 328:    */   
/* 329:    */   public ByteBuf setLong(int index, long value)
/* 330:    */   {
/* 331:346 */     checkIndex0(index, 8);
/* 332:347 */     unwrap().setLong(idx(index), value);
/* 333:348 */     return this;
/* 334:    */   }
/* 335:    */   
/* 336:    */   protected void _setLong(int index, long value)
/* 337:    */   {
/* 338:353 */     unwrap().setLong(idx(index), value);
/* 339:    */   }
/* 340:    */   
/* 341:    */   public ByteBuf setLongLE(int index, long value)
/* 342:    */   {
/* 343:358 */     checkIndex0(index, 8);
/* 344:359 */     unwrap().setLongLE(idx(index), value);
/* 345:360 */     return this;
/* 346:    */   }
/* 347:    */   
/* 348:    */   protected void _setLongLE(int index, long value)
/* 349:    */   {
/* 350:365 */     unwrap().setLongLE(idx(index), value);
/* 351:    */   }
/* 352:    */   
/* 353:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 354:    */   {
/* 355:370 */     checkIndex0(index, length);
/* 356:371 */     unwrap().setBytes(idx(index), src, srcIndex, length);
/* 357:372 */     return this;
/* 358:    */   }
/* 359:    */   
/* 360:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 361:    */   {
/* 362:377 */     checkIndex0(index, length);
/* 363:378 */     unwrap().setBytes(idx(index), src, srcIndex, length);
/* 364:379 */     return this;
/* 365:    */   }
/* 366:    */   
/* 367:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 368:    */   {
/* 369:384 */     checkIndex0(index, src.remaining());
/* 370:385 */     unwrap().setBytes(idx(index), src);
/* 371:386 */     return this;
/* 372:    */   }
/* 373:    */   
/* 374:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 375:    */     throws IOException
/* 376:    */   {
/* 377:391 */     checkIndex0(index, length);
/* 378:392 */     unwrap().getBytes(idx(index), out, length);
/* 379:393 */     return this;
/* 380:    */   }
/* 381:    */   
/* 382:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 383:    */     throws IOException
/* 384:    */   {
/* 385:398 */     checkIndex0(index, length);
/* 386:399 */     return unwrap().getBytes(idx(index), out, length);
/* 387:    */   }
/* 388:    */   
/* 389:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 390:    */     throws IOException
/* 391:    */   {
/* 392:404 */     checkIndex0(index, length);
/* 393:405 */     return unwrap().getBytes(idx(index), out, position, length);
/* 394:    */   }
/* 395:    */   
/* 396:    */   public int setBytes(int index, InputStream in, int length)
/* 397:    */     throws IOException
/* 398:    */   {
/* 399:410 */     checkIndex0(index, length);
/* 400:411 */     return unwrap().setBytes(idx(index), in, length);
/* 401:    */   }
/* 402:    */   
/* 403:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 404:    */     throws IOException
/* 405:    */   {
/* 406:416 */     checkIndex0(index, length);
/* 407:417 */     return unwrap().setBytes(idx(index), in, length);
/* 408:    */   }
/* 409:    */   
/* 410:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 411:    */     throws IOException
/* 412:    */   {
/* 413:422 */     checkIndex0(index, length);
/* 414:423 */     return unwrap().setBytes(idx(index), in, position, length);
/* 415:    */   }
/* 416:    */   
/* 417:    */   public int nioBufferCount()
/* 418:    */   {
/* 419:428 */     return unwrap().nioBufferCount();
/* 420:    */   }
/* 421:    */   
/* 422:    */   public ByteBuffer nioBuffer(int index, int length)
/* 423:    */   {
/* 424:433 */     checkIndex0(index, length);
/* 425:434 */     return unwrap().nioBuffer(idx(index), length);
/* 426:    */   }
/* 427:    */   
/* 428:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 429:    */   {
/* 430:439 */     checkIndex0(index, length);
/* 431:440 */     return unwrap().nioBuffers(idx(index), length);
/* 432:    */   }
/* 433:    */   
/* 434:    */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 435:    */   {
/* 436:445 */     checkIndex0(index, length);
/* 437:446 */     int ret = unwrap().forEachByte(idx(index), length, processor);
/* 438:447 */     if (ret >= this.adjustment) {
/* 439:448 */       return ret - this.adjustment;
/* 440:    */     }
/* 441:450 */     return -1;
/* 442:    */   }
/* 443:    */   
/* 444:    */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 445:    */   {
/* 446:456 */     checkIndex0(index, length);
/* 447:457 */     int ret = unwrap().forEachByteDesc(idx(index), length, processor);
/* 448:458 */     if (ret >= this.adjustment) {
/* 449:459 */       return ret - this.adjustment;
/* 450:    */     }
/* 451:461 */     return -1;
/* 452:    */   }
/* 453:    */   
/* 454:    */   final int idx(int index)
/* 455:    */   {
/* 456:469 */     return index + this.adjustment;
/* 457:    */   }
/* 458:    */   
/* 459:    */   static void checkSliceOutOfBounds(int index, int length, ByteBuf buffer)
/* 460:    */   {
/* 461:473 */     if (MathUtil.isOutOfBounds(index, length, buffer.capacity())) {
/* 462:474 */       throw new IndexOutOfBoundsException(buffer + ".slice(" + index + ", " + length + ')');
/* 463:    */     }
/* 464:    */   }
/* 465:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractUnpooledSlicedByteBuf
 * JD-Core Version:    0.7.0.1
 */