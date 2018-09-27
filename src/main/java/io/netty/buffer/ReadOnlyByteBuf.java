/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ByteProcessor;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.nio.ByteOrder;
/*   9:    */ import java.nio.ReadOnlyBufferException;
/*  10:    */ import java.nio.channels.FileChannel;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ 
/*  14:    */ @Deprecated
/*  15:    */ public class ReadOnlyByteBuf
/*  16:    */   extends AbstractDerivedByteBuf
/*  17:    */ {
/*  18:    */   private final ByteBuf buffer;
/*  19:    */   
/*  20:    */   public ReadOnlyByteBuf(ByteBuf buffer)
/*  21:    */   {
/*  22: 43 */     super(buffer.maxCapacity());
/*  23: 45 */     if (((buffer instanceof ReadOnlyByteBuf)) || ((buffer instanceof DuplicatedByteBuf))) {
/*  24: 46 */       this.buffer = buffer.unwrap();
/*  25:    */     } else {
/*  26: 48 */       this.buffer = buffer;
/*  27:    */     }
/*  28: 50 */     setIndex(buffer.readerIndex(), buffer.writerIndex());
/*  29:    */   }
/*  30:    */   
/*  31:    */   public boolean isReadOnly()
/*  32:    */   {
/*  33: 55 */     return true;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public boolean isWritable()
/*  37:    */   {
/*  38: 60 */     return false;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public boolean isWritable(int numBytes)
/*  42:    */   {
/*  43: 65 */     return false;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public int ensureWritable(int minWritableBytes, boolean force)
/*  47:    */   {
/*  48: 70 */     return 1;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public ByteBuf ensureWritable(int minWritableBytes)
/*  52:    */   {
/*  53: 75 */     throw new ReadOnlyBufferException();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public ByteBuf unwrap()
/*  57:    */   {
/*  58: 80 */     return this.buffer;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ByteBufAllocator alloc()
/*  62:    */   {
/*  63: 85 */     return unwrap().alloc();
/*  64:    */   }
/*  65:    */   
/*  66:    */   @Deprecated
/*  67:    */   public ByteOrder order()
/*  68:    */   {
/*  69: 91 */     return unwrap().order();
/*  70:    */   }
/*  71:    */   
/*  72:    */   public boolean isDirect()
/*  73:    */   {
/*  74: 96 */     return unwrap().isDirect();
/*  75:    */   }
/*  76:    */   
/*  77:    */   public boolean hasArray()
/*  78:    */   {
/*  79:101 */     return false;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public byte[] array()
/*  83:    */   {
/*  84:106 */     throw new ReadOnlyBufferException();
/*  85:    */   }
/*  86:    */   
/*  87:    */   public int arrayOffset()
/*  88:    */   {
/*  89:111 */     throw new ReadOnlyBufferException();
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean hasMemoryAddress()
/*  93:    */   {
/*  94:116 */     return unwrap().hasMemoryAddress();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public long memoryAddress()
/*  98:    */   {
/*  99:121 */     return unwrap().memoryAddress();
/* 100:    */   }
/* 101:    */   
/* 102:    */   public ByteBuf discardReadBytes()
/* 103:    */   {
/* 104:126 */     throw new ReadOnlyBufferException();
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 108:    */   {
/* 109:131 */     throw new ReadOnlyBufferException();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 113:    */   {
/* 114:136 */     throw new ReadOnlyBufferException();
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 118:    */   {
/* 119:141 */     throw new ReadOnlyBufferException();
/* 120:    */   }
/* 121:    */   
/* 122:    */   public ByteBuf setByte(int index, int value)
/* 123:    */   {
/* 124:146 */     throw new ReadOnlyBufferException();
/* 125:    */   }
/* 126:    */   
/* 127:    */   protected void _setByte(int index, int value)
/* 128:    */   {
/* 129:151 */     throw new ReadOnlyBufferException();
/* 130:    */   }
/* 131:    */   
/* 132:    */   public ByteBuf setShort(int index, int value)
/* 133:    */   {
/* 134:156 */     throw new ReadOnlyBufferException();
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected void _setShort(int index, int value)
/* 138:    */   {
/* 139:161 */     throw new ReadOnlyBufferException();
/* 140:    */   }
/* 141:    */   
/* 142:    */   public ByteBuf setShortLE(int index, int value)
/* 143:    */   {
/* 144:166 */     throw new ReadOnlyBufferException();
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected void _setShortLE(int index, int value)
/* 148:    */   {
/* 149:171 */     throw new ReadOnlyBufferException();
/* 150:    */   }
/* 151:    */   
/* 152:    */   public ByteBuf setMedium(int index, int value)
/* 153:    */   {
/* 154:176 */     throw new ReadOnlyBufferException();
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected void _setMedium(int index, int value)
/* 158:    */   {
/* 159:181 */     throw new ReadOnlyBufferException();
/* 160:    */   }
/* 161:    */   
/* 162:    */   public ByteBuf setMediumLE(int index, int value)
/* 163:    */   {
/* 164:186 */     throw new ReadOnlyBufferException();
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected void _setMediumLE(int index, int value)
/* 168:    */   {
/* 169:191 */     throw new ReadOnlyBufferException();
/* 170:    */   }
/* 171:    */   
/* 172:    */   public ByteBuf setInt(int index, int value)
/* 173:    */   {
/* 174:196 */     throw new ReadOnlyBufferException();
/* 175:    */   }
/* 176:    */   
/* 177:    */   protected void _setInt(int index, int value)
/* 178:    */   {
/* 179:201 */     throw new ReadOnlyBufferException();
/* 180:    */   }
/* 181:    */   
/* 182:    */   public ByteBuf setIntLE(int index, int value)
/* 183:    */   {
/* 184:206 */     throw new ReadOnlyBufferException();
/* 185:    */   }
/* 186:    */   
/* 187:    */   protected void _setIntLE(int index, int value)
/* 188:    */   {
/* 189:211 */     throw new ReadOnlyBufferException();
/* 190:    */   }
/* 191:    */   
/* 192:    */   public ByteBuf setLong(int index, long value)
/* 193:    */   {
/* 194:216 */     throw new ReadOnlyBufferException();
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected void _setLong(int index, long value)
/* 198:    */   {
/* 199:221 */     throw new ReadOnlyBufferException();
/* 200:    */   }
/* 201:    */   
/* 202:    */   public ByteBuf setLongLE(int index, long value)
/* 203:    */   {
/* 204:226 */     throw new ReadOnlyBufferException();
/* 205:    */   }
/* 206:    */   
/* 207:    */   protected void _setLongLE(int index, long value)
/* 208:    */   {
/* 209:231 */     throw new ReadOnlyBufferException();
/* 210:    */   }
/* 211:    */   
/* 212:    */   public int setBytes(int index, InputStream in, int length)
/* 213:    */   {
/* 214:236 */     throw new ReadOnlyBufferException();
/* 215:    */   }
/* 216:    */   
/* 217:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 218:    */   {
/* 219:241 */     throw new ReadOnlyBufferException();
/* 220:    */   }
/* 221:    */   
/* 222:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 223:    */   {
/* 224:246 */     throw new ReadOnlyBufferException();
/* 225:    */   }
/* 226:    */   
/* 227:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 228:    */     throws IOException
/* 229:    */   {
/* 230:252 */     return unwrap().getBytes(index, out, length);
/* 231:    */   }
/* 232:    */   
/* 233:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 234:    */     throws IOException
/* 235:    */   {
/* 236:258 */     return unwrap().getBytes(index, out, position, length);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 240:    */     throws IOException
/* 241:    */   {
/* 242:264 */     unwrap().getBytes(index, out, length);
/* 243:265 */     return this;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 247:    */   {
/* 248:270 */     unwrap().getBytes(index, dst, dstIndex, length);
/* 249:271 */     return this;
/* 250:    */   }
/* 251:    */   
/* 252:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 253:    */   {
/* 254:276 */     unwrap().getBytes(index, dst, dstIndex, length);
/* 255:277 */     return this;
/* 256:    */   }
/* 257:    */   
/* 258:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 259:    */   {
/* 260:282 */     unwrap().getBytes(index, dst);
/* 261:283 */     return this;
/* 262:    */   }
/* 263:    */   
/* 264:    */   public ByteBuf duplicate()
/* 265:    */   {
/* 266:288 */     return new ReadOnlyByteBuf(this);
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ByteBuf copy(int index, int length)
/* 270:    */   {
/* 271:293 */     return unwrap().copy(index, length);
/* 272:    */   }
/* 273:    */   
/* 274:    */   public ByteBuf slice(int index, int length)
/* 275:    */   {
/* 276:298 */     return Unpooled.unmodifiableBuffer(unwrap().slice(index, length));
/* 277:    */   }
/* 278:    */   
/* 279:    */   public byte getByte(int index)
/* 280:    */   {
/* 281:303 */     return unwrap().getByte(index);
/* 282:    */   }
/* 283:    */   
/* 284:    */   protected byte _getByte(int index)
/* 285:    */   {
/* 286:308 */     return unwrap().getByte(index);
/* 287:    */   }
/* 288:    */   
/* 289:    */   public short getShort(int index)
/* 290:    */   {
/* 291:313 */     return unwrap().getShort(index);
/* 292:    */   }
/* 293:    */   
/* 294:    */   protected short _getShort(int index)
/* 295:    */   {
/* 296:318 */     return unwrap().getShort(index);
/* 297:    */   }
/* 298:    */   
/* 299:    */   public short getShortLE(int index)
/* 300:    */   {
/* 301:323 */     return unwrap().getShortLE(index);
/* 302:    */   }
/* 303:    */   
/* 304:    */   protected short _getShortLE(int index)
/* 305:    */   {
/* 306:328 */     return unwrap().getShortLE(index);
/* 307:    */   }
/* 308:    */   
/* 309:    */   public int getUnsignedMedium(int index)
/* 310:    */   {
/* 311:333 */     return unwrap().getUnsignedMedium(index);
/* 312:    */   }
/* 313:    */   
/* 314:    */   protected int _getUnsignedMedium(int index)
/* 315:    */   {
/* 316:338 */     return unwrap().getUnsignedMedium(index);
/* 317:    */   }
/* 318:    */   
/* 319:    */   public int getUnsignedMediumLE(int index)
/* 320:    */   {
/* 321:343 */     return unwrap().getUnsignedMediumLE(index);
/* 322:    */   }
/* 323:    */   
/* 324:    */   protected int _getUnsignedMediumLE(int index)
/* 325:    */   {
/* 326:348 */     return unwrap().getUnsignedMediumLE(index);
/* 327:    */   }
/* 328:    */   
/* 329:    */   public int getInt(int index)
/* 330:    */   {
/* 331:353 */     return unwrap().getInt(index);
/* 332:    */   }
/* 333:    */   
/* 334:    */   protected int _getInt(int index)
/* 335:    */   {
/* 336:358 */     return unwrap().getInt(index);
/* 337:    */   }
/* 338:    */   
/* 339:    */   public int getIntLE(int index)
/* 340:    */   {
/* 341:363 */     return unwrap().getIntLE(index);
/* 342:    */   }
/* 343:    */   
/* 344:    */   protected int _getIntLE(int index)
/* 345:    */   {
/* 346:368 */     return unwrap().getIntLE(index);
/* 347:    */   }
/* 348:    */   
/* 349:    */   public long getLong(int index)
/* 350:    */   {
/* 351:373 */     return unwrap().getLong(index);
/* 352:    */   }
/* 353:    */   
/* 354:    */   protected long _getLong(int index)
/* 355:    */   {
/* 356:378 */     return unwrap().getLong(index);
/* 357:    */   }
/* 358:    */   
/* 359:    */   public long getLongLE(int index)
/* 360:    */   {
/* 361:383 */     return unwrap().getLongLE(index);
/* 362:    */   }
/* 363:    */   
/* 364:    */   protected long _getLongLE(int index)
/* 365:    */   {
/* 366:388 */     return unwrap().getLongLE(index);
/* 367:    */   }
/* 368:    */   
/* 369:    */   public int nioBufferCount()
/* 370:    */   {
/* 371:393 */     return unwrap().nioBufferCount();
/* 372:    */   }
/* 373:    */   
/* 374:    */   public ByteBuffer nioBuffer(int index, int length)
/* 375:    */   {
/* 376:398 */     return unwrap().nioBuffer(index, length).asReadOnlyBuffer();
/* 377:    */   }
/* 378:    */   
/* 379:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 380:    */   {
/* 381:403 */     return unwrap().nioBuffers(index, length);
/* 382:    */   }
/* 383:    */   
/* 384:    */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 385:    */   {
/* 386:408 */     return unwrap().forEachByte(index, length, processor);
/* 387:    */   }
/* 388:    */   
/* 389:    */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 390:    */   {
/* 391:413 */     return unwrap().forEachByteDesc(index, length, processor);
/* 392:    */   }
/* 393:    */   
/* 394:    */   public int capacity()
/* 395:    */   {
/* 396:418 */     return unwrap().capacity();
/* 397:    */   }
/* 398:    */   
/* 399:    */   public ByteBuf capacity(int newCapacity)
/* 400:    */   {
/* 401:423 */     throw new ReadOnlyBufferException();
/* 402:    */   }
/* 403:    */   
/* 404:    */   public ByteBuf asReadOnly()
/* 405:    */   {
/* 406:428 */     return this;
/* 407:    */   }
/* 408:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ReadOnlyByteBuf
 * JD-Core Version:    0.7.0.1
 */