/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ByteProcessor;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.nio.ByteOrder;
/*   9:    */ import java.nio.channels.FileChannel;
/*  10:    */ import java.nio.channels.GatheringByteChannel;
/*  11:    */ import java.nio.channels.ScatteringByteChannel;
/*  12:    */ 
/*  13:    */ @Deprecated
/*  14:    */ public class DuplicatedByteBuf
/*  15:    */   extends AbstractDerivedByteBuf
/*  16:    */ {
/*  17:    */   private final ByteBuf buffer;
/*  18:    */   
/*  19:    */   public DuplicatedByteBuf(ByteBuf buffer)
/*  20:    */   {
/*  21: 42 */     this(buffer, buffer.readerIndex(), buffer.writerIndex());
/*  22:    */   }
/*  23:    */   
/*  24:    */   DuplicatedByteBuf(ByteBuf buffer, int readerIndex, int writerIndex)
/*  25:    */   {
/*  26: 46 */     super(buffer.maxCapacity());
/*  27: 48 */     if ((buffer instanceof DuplicatedByteBuf)) {
/*  28: 49 */       this.buffer = ((DuplicatedByteBuf)buffer).buffer;
/*  29: 50 */     } else if ((buffer instanceof AbstractPooledDerivedByteBuf)) {
/*  30: 51 */       this.buffer = buffer.unwrap();
/*  31:    */     } else {
/*  32: 53 */       this.buffer = buffer;
/*  33:    */     }
/*  34: 56 */     setIndex(readerIndex, writerIndex);
/*  35: 57 */     markReaderIndex();
/*  36: 58 */     markWriterIndex();
/*  37:    */   }
/*  38:    */   
/*  39:    */   public ByteBuf unwrap()
/*  40:    */   {
/*  41: 63 */     return this.buffer;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ByteBufAllocator alloc()
/*  45:    */   {
/*  46: 68 */     return unwrap().alloc();
/*  47:    */   }
/*  48:    */   
/*  49:    */   @Deprecated
/*  50:    */   public ByteOrder order()
/*  51:    */   {
/*  52: 74 */     return unwrap().order();
/*  53:    */   }
/*  54:    */   
/*  55:    */   public boolean isDirect()
/*  56:    */   {
/*  57: 79 */     return unwrap().isDirect();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int capacity()
/*  61:    */   {
/*  62: 84 */     return unwrap().capacity();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public ByteBuf capacity(int newCapacity)
/*  66:    */   {
/*  67: 89 */     unwrap().capacity(newCapacity);
/*  68: 90 */     return this;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public boolean hasArray()
/*  72:    */   {
/*  73: 95 */     return unwrap().hasArray();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public byte[] array()
/*  77:    */   {
/*  78:100 */     return unwrap().array();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public int arrayOffset()
/*  82:    */   {
/*  83:105 */     return unwrap().arrayOffset();
/*  84:    */   }
/*  85:    */   
/*  86:    */   public boolean hasMemoryAddress()
/*  87:    */   {
/*  88:110 */     return unwrap().hasMemoryAddress();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public long memoryAddress()
/*  92:    */   {
/*  93:115 */     return unwrap().memoryAddress();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public byte getByte(int index)
/*  97:    */   {
/*  98:120 */     return unwrap().getByte(index);
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected byte _getByte(int index)
/* 102:    */   {
/* 103:125 */     return unwrap().getByte(index);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public short getShort(int index)
/* 107:    */   {
/* 108:130 */     return unwrap().getShort(index);
/* 109:    */   }
/* 110:    */   
/* 111:    */   protected short _getShort(int index)
/* 112:    */   {
/* 113:135 */     return unwrap().getShort(index);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public short getShortLE(int index)
/* 117:    */   {
/* 118:140 */     return unwrap().getShortLE(index);
/* 119:    */   }
/* 120:    */   
/* 121:    */   protected short _getShortLE(int index)
/* 122:    */   {
/* 123:145 */     return unwrap().getShortLE(index);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public int getUnsignedMedium(int index)
/* 127:    */   {
/* 128:150 */     return unwrap().getUnsignedMedium(index);
/* 129:    */   }
/* 130:    */   
/* 131:    */   protected int _getUnsignedMedium(int index)
/* 132:    */   {
/* 133:155 */     return unwrap().getUnsignedMedium(index);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public int getUnsignedMediumLE(int index)
/* 137:    */   {
/* 138:160 */     return unwrap().getUnsignedMediumLE(index);
/* 139:    */   }
/* 140:    */   
/* 141:    */   protected int _getUnsignedMediumLE(int index)
/* 142:    */   {
/* 143:165 */     return unwrap().getUnsignedMediumLE(index);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public int getInt(int index)
/* 147:    */   {
/* 148:170 */     return unwrap().getInt(index);
/* 149:    */   }
/* 150:    */   
/* 151:    */   protected int _getInt(int index)
/* 152:    */   {
/* 153:175 */     return unwrap().getInt(index);
/* 154:    */   }
/* 155:    */   
/* 156:    */   public int getIntLE(int index)
/* 157:    */   {
/* 158:180 */     return unwrap().getIntLE(index);
/* 159:    */   }
/* 160:    */   
/* 161:    */   protected int _getIntLE(int index)
/* 162:    */   {
/* 163:185 */     return unwrap().getIntLE(index);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public long getLong(int index)
/* 167:    */   {
/* 168:190 */     return unwrap().getLong(index);
/* 169:    */   }
/* 170:    */   
/* 171:    */   protected long _getLong(int index)
/* 172:    */   {
/* 173:195 */     return unwrap().getLong(index);
/* 174:    */   }
/* 175:    */   
/* 176:    */   public long getLongLE(int index)
/* 177:    */   {
/* 178:200 */     return unwrap().getLongLE(index);
/* 179:    */   }
/* 180:    */   
/* 181:    */   protected long _getLongLE(int index)
/* 182:    */   {
/* 183:205 */     return unwrap().getLongLE(index);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public ByteBuf copy(int index, int length)
/* 187:    */   {
/* 188:210 */     return unwrap().copy(index, length);
/* 189:    */   }
/* 190:    */   
/* 191:    */   public ByteBuf slice(int index, int length)
/* 192:    */   {
/* 193:215 */     return unwrap().slice(index, length);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 197:    */   {
/* 198:220 */     unwrap().getBytes(index, dst, dstIndex, length);
/* 199:221 */     return this;
/* 200:    */   }
/* 201:    */   
/* 202:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 203:    */   {
/* 204:226 */     unwrap().getBytes(index, dst, dstIndex, length);
/* 205:227 */     return this;
/* 206:    */   }
/* 207:    */   
/* 208:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 209:    */   {
/* 210:232 */     unwrap().getBytes(index, dst);
/* 211:233 */     return this;
/* 212:    */   }
/* 213:    */   
/* 214:    */   public ByteBuf setByte(int index, int value)
/* 215:    */   {
/* 216:238 */     unwrap().setByte(index, value);
/* 217:239 */     return this;
/* 218:    */   }
/* 219:    */   
/* 220:    */   protected void _setByte(int index, int value)
/* 221:    */   {
/* 222:244 */     unwrap().setByte(index, value);
/* 223:    */   }
/* 224:    */   
/* 225:    */   public ByteBuf setShort(int index, int value)
/* 226:    */   {
/* 227:249 */     unwrap().setShort(index, value);
/* 228:250 */     return this;
/* 229:    */   }
/* 230:    */   
/* 231:    */   protected void _setShort(int index, int value)
/* 232:    */   {
/* 233:255 */     unwrap().setShort(index, value);
/* 234:    */   }
/* 235:    */   
/* 236:    */   public ByteBuf setShortLE(int index, int value)
/* 237:    */   {
/* 238:260 */     unwrap().setShortLE(index, value);
/* 239:261 */     return this;
/* 240:    */   }
/* 241:    */   
/* 242:    */   protected void _setShortLE(int index, int value)
/* 243:    */   {
/* 244:266 */     unwrap().setShortLE(index, value);
/* 245:    */   }
/* 246:    */   
/* 247:    */   public ByteBuf setMedium(int index, int value)
/* 248:    */   {
/* 249:271 */     unwrap().setMedium(index, value);
/* 250:272 */     return this;
/* 251:    */   }
/* 252:    */   
/* 253:    */   protected void _setMedium(int index, int value)
/* 254:    */   {
/* 255:277 */     unwrap().setMedium(index, value);
/* 256:    */   }
/* 257:    */   
/* 258:    */   public ByteBuf setMediumLE(int index, int value)
/* 259:    */   {
/* 260:282 */     unwrap().setMediumLE(index, value);
/* 261:283 */     return this;
/* 262:    */   }
/* 263:    */   
/* 264:    */   protected void _setMediumLE(int index, int value)
/* 265:    */   {
/* 266:288 */     unwrap().setMediumLE(index, value);
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ByteBuf setInt(int index, int value)
/* 270:    */   {
/* 271:293 */     unwrap().setInt(index, value);
/* 272:294 */     return this;
/* 273:    */   }
/* 274:    */   
/* 275:    */   protected void _setInt(int index, int value)
/* 276:    */   {
/* 277:299 */     unwrap().setInt(index, value);
/* 278:    */   }
/* 279:    */   
/* 280:    */   public ByteBuf setIntLE(int index, int value)
/* 281:    */   {
/* 282:304 */     unwrap().setIntLE(index, value);
/* 283:305 */     return this;
/* 284:    */   }
/* 285:    */   
/* 286:    */   protected void _setIntLE(int index, int value)
/* 287:    */   {
/* 288:310 */     unwrap().setIntLE(index, value);
/* 289:    */   }
/* 290:    */   
/* 291:    */   public ByteBuf setLong(int index, long value)
/* 292:    */   {
/* 293:315 */     unwrap().setLong(index, value);
/* 294:316 */     return this;
/* 295:    */   }
/* 296:    */   
/* 297:    */   protected void _setLong(int index, long value)
/* 298:    */   {
/* 299:321 */     unwrap().setLong(index, value);
/* 300:    */   }
/* 301:    */   
/* 302:    */   public ByteBuf setLongLE(int index, long value)
/* 303:    */   {
/* 304:326 */     unwrap().setLongLE(index, value);
/* 305:327 */     return this;
/* 306:    */   }
/* 307:    */   
/* 308:    */   protected void _setLongLE(int index, long value)
/* 309:    */   {
/* 310:332 */     unwrap().setLongLE(index, value);
/* 311:    */   }
/* 312:    */   
/* 313:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 314:    */   {
/* 315:337 */     unwrap().setBytes(index, src, srcIndex, length);
/* 316:338 */     return this;
/* 317:    */   }
/* 318:    */   
/* 319:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 320:    */   {
/* 321:343 */     unwrap().setBytes(index, src, srcIndex, length);
/* 322:344 */     return this;
/* 323:    */   }
/* 324:    */   
/* 325:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 326:    */   {
/* 327:349 */     unwrap().setBytes(index, src);
/* 328:350 */     return this;
/* 329:    */   }
/* 330:    */   
/* 331:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 332:    */     throws IOException
/* 333:    */   {
/* 334:356 */     unwrap().getBytes(index, out, length);
/* 335:357 */     return this;
/* 336:    */   }
/* 337:    */   
/* 338:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 339:    */     throws IOException
/* 340:    */   {
/* 341:363 */     return unwrap().getBytes(index, out, length);
/* 342:    */   }
/* 343:    */   
/* 344:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 345:    */     throws IOException
/* 346:    */   {
/* 347:369 */     return unwrap().getBytes(index, out, position, length);
/* 348:    */   }
/* 349:    */   
/* 350:    */   public int setBytes(int index, InputStream in, int length)
/* 351:    */     throws IOException
/* 352:    */   {
/* 353:375 */     return unwrap().setBytes(index, in, length);
/* 354:    */   }
/* 355:    */   
/* 356:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 357:    */     throws IOException
/* 358:    */   {
/* 359:381 */     return unwrap().setBytes(index, in, length);
/* 360:    */   }
/* 361:    */   
/* 362:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 363:    */     throws IOException
/* 364:    */   {
/* 365:387 */     return unwrap().setBytes(index, in, position, length);
/* 366:    */   }
/* 367:    */   
/* 368:    */   public int nioBufferCount()
/* 369:    */   {
/* 370:392 */     return unwrap().nioBufferCount();
/* 371:    */   }
/* 372:    */   
/* 373:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 374:    */   {
/* 375:397 */     return unwrap().nioBuffers(index, length);
/* 376:    */   }
/* 377:    */   
/* 378:    */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 379:    */   {
/* 380:402 */     return unwrap().forEachByte(index, length, processor);
/* 381:    */   }
/* 382:    */   
/* 383:    */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 384:    */   {
/* 385:407 */     return unwrap().forEachByteDesc(index, length, processor);
/* 386:    */   }
/* 387:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.DuplicatedByteBuf
 * JD-Core Version:    0.7.0.1
 */