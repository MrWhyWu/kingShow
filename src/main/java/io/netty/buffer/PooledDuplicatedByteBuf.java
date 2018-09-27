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
/*  14:    */ final class PooledDuplicatedByteBuf
/*  15:    */   extends AbstractPooledDerivedByteBuf
/*  16:    */ {
/*  17: 33 */   private static final Recycler<PooledDuplicatedByteBuf> RECYCLER = new Recycler()
/*  18:    */   {
/*  19:    */     protected PooledDuplicatedByteBuf newObject(Recycler.Handle<PooledDuplicatedByteBuf> handle)
/*  20:    */     {
/*  21: 36 */       return new PooledDuplicatedByteBuf(handle, null);
/*  22:    */     }
/*  23:    */   };
/*  24:    */   
/*  25:    */   static PooledDuplicatedByteBuf newInstance(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex)
/*  26:    */   {
/*  27: 42 */     PooledDuplicatedByteBuf duplicate = (PooledDuplicatedByteBuf)RECYCLER.get();
/*  28: 43 */     duplicate.init(unwrapped, wrapped, readerIndex, writerIndex, unwrapped.maxCapacity());
/*  29: 44 */     duplicate.markReaderIndex();
/*  30: 45 */     duplicate.markWriterIndex();
/*  31:    */     
/*  32: 47 */     return duplicate;
/*  33:    */   }
/*  34:    */   
/*  35:    */   private PooledDuplicatedByteBuf(Recycler.Handle<PooledDuplicatedByteBuf> handle)
/*  36:    */   {
/*  37: 51 */     super(handle);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public int capacity()
/*  41:    */   {
/*  42: 56 */     return unwrap().capacity();
/*  43:    */   }
/*  44:    */   
/*  45:    */   public ByteBuf capacity(int newCapacity)
/*  46:    */   {
/*  47: 61 */     unwrap().capacity(newCapacity);
/*  48: 62 */     return this;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public int arrayOffset()
/*  52:    */   {
/*  53: 67 */     return unwrap().arrayOffset();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public long memoryAddress()
/*  57:    */   {
/*  58: 72 */     return unwrap().memoryAddress();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ByteBuffer nioBuffer(int index, int length)
/*  62:    */   {
/*  63: 77 */     return unwrap().nioBuffer(index, length);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public ByteBuffer[] nioBuffers(int index, int length)
/*  67:    */   {
/*  68: 82 */     return unwrap().nioBuffers(index, length);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public ByteBuf copy(int index, int length)
/*  72:    */   {
/*  73: 87 */     return unwrap().copy(index, length);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ByteBuf retainedSlice(int index, int length)
/*  77:    */   {
/*  78: 92 */     return PooledSlicedByteBuf.newInstance(unwrap(), this, index, length);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public ByteBuf duplicate()
/*  82:    */   {
/*  83: 97 */     return duplicate0().setIndex(readerIndex(), writerIndex());
/*  84:    */   }
/*  85:    */   
/*  86:    */   public ByteBuf retainedDuplicate()
/*  87:    */   {
/*  88:102 */     return newInstance(unwrap(), this, readerIndex(), writerIndex());
/*  89:    */   }
/*  90:    */   
/*  91:    */   public byte getByte(int index)
/*  92:    */   {
/*  93:107 */     return unwrap().getByte(index);
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected byte _getByte(int index)
/*  97:    */   {
/*  98:112 */     return unwrap()._getByte(index);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public short getShort(int index)
/* 102:    */   {
/* 103:117 */     return unwrap().getShort(index);
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected short _getShort(int index)
/* 107:    */   {
/* 108:122 */     return unwrap()._getShort(index);
/* 109:    */   }
/* 110:    */   
/* 111:    */   public short getShortLE(int index)
/* 112:    */   {
/* 113:127 */     return unwrap().getShortLE(index);
/* 114:    */   }
/* 115:    */   
/* 116:    */   protected short _getShortLE(int index)
/* 117:    */   {
/* 118:132 */     return unwrap()._getShortLE(index);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public int getUnsignedMedium(int index)
/* 122:    */   {
/* 123:137 */     return unwrap().getUnsignedMedium(index);
/* 124:    */   }
/* 125:    */   
/* 126:    */   protected int _getUnsignedMedium(int index)
/* 127:    */   {
/* 128:142 */     return unwrap()._getUnsignedMedium(index);
/* 129:    */   }
/* 130:    */   
/* 131:    */   public int getUnsignedMediumLE(int index)
/* 132:    */   {
/* 133:147 */     return unwrap().getUnsignedMediumLE(index);
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected int _getUnsignedMediumLE(int index)
/* 137:    */   {
/* 138:152 */     return unwrap()._getUnsignedMediumLE(index);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public int getInt(int index)
/* 142:    */   {
/* 143:157 */     return unwrap().getInt(index);
/* 144:    */   }
/* 145:    */   
/* 146:    */   protected int _getInt(int index)
/* 147:    */   {
/* 148:162 */     return unwrap()._getInt(index);
/* 149:    */   }
/* 150:    */   
/* 151:    */   public int getIntLE(int index)
/* 152:    */   {
/* 153:167 */     return unwrap().getIntLE(index);
/* 154:    */   }
/* 155:    */   
/* 156:    */   protected int _getIntLE(int index)
/* 157:    */   {
/* 158:172 */     return unwrap()._getIntLE(index);
/* 159:    */   }
/* 160:    */   
/* 161:    */   public long getLong(int index)
/* 162:    */   {
/* 163:177 */     return unwrap().getLong(index);
/* 164:    */   }
/* 165:    */   
/* 166:    */   protected long _getLong(int index)
/* 167:    */   {
/* 168:182 */     return unwrap()._getLong(index);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public long getLongLE(int index)
/* 172:    */   {
/* 173:187 */     return unwrap().getLongLE(index);
/* 174:    */   }
/* 175:    */   
/* 176:    */   protected long _getLongLE(int index)
/* 177:    */   {
/* 178:192 */     return unwrap()._getLongLE(index);
/* 179:    */   }
/* 180:    */   
/* 181:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 182:    */   {
/* 183:197 */     unwrap().getBytes(index, dst, dstIndex, length);
/* 184:198 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 188:    */   {
/* 189:203 */     unwrap().getBytes(index, dst, dstIndex, length);
/* 190:204 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 194:    */   {
/* 195:209 */     unwrap().getBytes(index, dst);
/* 196:210 */     return this;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public ByteBuf setByte(int index, int value)
/* 200:    */   {
/* 201:215 */     unwrap().setByte(index, value);
/* 202:216 */     return this;
/* 203:    */   }
/* 204:    */   
/* 205:    */   protected void _setByte(int index, int value)
/* 206:    */   {
/* 207:221 */     unwrap()._setByte(index, value);
/* 208:    */   }
/* 209:    */   
/* 210:    */   public ByteBuf setShort(int index, int value)
/* 211:    */   {
/* 212:226 */     unwrap().setShort(index, value);
/* 213:227 */     return this;
/* 214:    */   }
/* 215:    */   
/* 216:    */   protected void _setShort(int index, int value)
/* 217:    */   {
/* 218:232 */     unwrap()._setShort(index, value);
/* 219:    */   }
/* 220:    */   
/* 221:    */   public ByteBuf setShortLE(int index, int value)
/* 222:    */   {
/* 223:237 */     unwrap().setShortLE(index, value);
/* 224:238 */     return this;
/* 225:    */   }
/* 226:    */   
/* 227:    */   protected void _setShortLE(int index, int value)
/* 228:    */   {
/* 229:243 */     unwrap()._setShortLE(index, value);
/* 230:    */   }
/* 231:    */   
/* 232:    */   public ByteBuf setMedium(int index, int value)
/* 233:    */   {
/* 234:248 */     unwrap().setMedium(index, value);
/* 235:249 */     return this;
/* 236:    */   }
/* 237:    */   
/* 238:    */   protected void _setMedium(int index, int value)
/* 239:    */   {
/* 240:254 */     unwrap()._setMedium(index, value);
/* 241:    */   }
/* 242:    */   
/* 243:    */   public ByteBuf setMediumLE(int index, int value)
/* 244:    */   {
/* 245:259 */     unwrap().setMediumLE(index, value);
/* 246:260 */     return this;
/* 247:    */   }
/* 248:    */   
/* 249:    */   protected void _setMediumLE(int index, int value)
/* 250:    */   {
/* 251:265 */     unwrap()._setMediumLE(index, value);
/* 252:    */   }
/* 253:    */   
/* 254:    */   public ByteBuf setInt(int index, int value)
/* 255:    */   {
/* 256:270 */     unwrap().setInt(index, value);
/* 257:271 */     return this;
/* 258:    */   }
/* 259:    */   
/* 260:    */   protected void _setInt(int index, int value)
/* 261:    */   {
/* 262:276 */     unwrap()._setInt(index, value);
/* 263:    */   }
/* 264:    */   
/* 265:    */   public ByteBuf setIntLE(int index, int value)
/* 266:    */   {
/* 267:281 */     unwrap().setIntLE(index, value);
/* 268:282 */     return this;
/* 269:    */   }
/* 270:    */   
/* 271:    */   protected void _setIntLE(int index, int value)
/* 272:    */   {
/* 273:287 */     unwrap()._setIntLE(index, value);
/* 274:    */   }
/* 275:    */   
/* 276:    */   public ByteBuf setLong(int index, long value)
/* 277:    */   {
/* 278:292 */     unwrap().setLong(index, value);
/* 279:293 */     return this;
/* 280:    */   }
/* 281:    */   
/* 282:    */   protected void _setLong(int index, long value)
/* 283:    */   {
/* 284:298 */     unwrap()._setLong(index, value);
/* 285:    */   }
/* 286:    */   
/* 287:    */   public ByteBuf setLongLE(int index, long value)
/* 288:    */   {
/* 289:303 */     unwrap().setLongLE(index, value);
/* 290:304 */     return this;
/* 291:    */   }
/* 292:    */   
/* 293:    */   protected void _setLongLE(int index, long value)
/* 294:    */   {
/* 295:309 */     unwrap().setLongLE(index, value);
/* 296:    */   }
/* 297:    */   
/* 298:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 299:    */   {
/* 300:314 */     unwrap().setBytes(index, src, srcIndex, length);
/* 301:315 */     return this;
/* 302:    */   }
/* 303:    */   
/* 304:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 305:    */   {
/* 306:320 */     unwrap().setBytes(index, src, srcIndex, length);
/* 307:321 */     return this;
/* 308:    */   }
/* 309:    */   
/* 310:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 311:    */   {
/* 312:326 */     unwrap().setBytes(index, src);
/* 313:327 */     return this;
/* 314:    */   }
/* 315:    */   
/* 316:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 317:    */     throws IOException
/* 318:    */   {
/* 319:333 */     unwrap().getBytes(index, out, length);
/* 320:334 */     return this;
/* 321:    */   }
/* 322:    */   
/* 323:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 324:    */     throws IOException
/* 325:    */   {
/* 326:340 */     return unwrap().getBytes(index, out, length);
/* 327:    */   }
/* 328:    */   
/* 329:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 330:    */     throws IOException
/* 331:    */   {
/* 332:346 */     return unwrap().getBytes(index, out, position, length);
/* 333:    */   }
/* 334:    */   
/* 335:    */   public int setBytes(int index, InputStream in, int length)
/* 336:    */     throws IOException
/* 337:    */   {
/* 338:352 */     return unwrap().setBytes(index, in, length);
/* 339:    */   }
/* 340:    */   
/* 341:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 342:    */     throws IOException
/* 343:    */   {
/* 344:358 */     return unwrap().setBytes(index, in, length);
/* 345:    */   }
/* 346:    */   
/* 347:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 348:    */     throws IOException
/* 349:    */   {
/* 350:364 */     return unwrap().setBytes(index, in, position, length);
/* 351:    */   }
/* 352:    */   
/* 353:    */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 354:    */   {
/* 355:369 */     return unwrap().forEachByte(index, length, processor);
/* 356:    */   }
/* 357:    */   
/* 358:    */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 359:    */   {
/* 360:374 */     return unwrap().forEachByteDesc(index, length, processor);
/* 361:    */   }
/* 362:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledDuplicatedByteBuf
 * JD-Core Version:    0.7.0.1
 */