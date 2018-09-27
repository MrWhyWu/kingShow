/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ByteProcessor;
/*    4:     */ import io.netty.util.ReferenceCounted;
/*    5:     */ import java.io.IOException;
/*    6:     */ import java.io.InputStream;
/*    7:     */ import java.io.OutputStream;
/*    8:     */ import java.nio.ByteBuffer;
/*    9:     */ import java.nio.ByteOrder;
/*   10:     */ import java.nio.channels.FileChannel;
/*   11:     */ import java.nio.channels.GatheringByteChannel;
/*   12:     */ import java.nio.channels.ScatteringByteChannel;
/*   13:     */ import java.nio.charset.Charset;
/*   14:     */ 
/*   15:     */ public abstract class ByteBuf
/*   16:     */   implements ReferenceCounted, Comparable<ByteBuf>
/*   17:     */ {
/*   18:     */   public abstract int capacity();
/*   19:     */   
/*   20:     */   public abstract ByteBuf capacity(int paramInt);
/*   21:     */   
/*   22:     */   public abstract int maxCapacity();
/*   23:     */   
/*   24:     */   public abstract ByteBufAllocator alloc();
/*   25:     */   
/*   26:     */   @Deprecated
/*   27:     */   public abstract ByteOrder order();
/*   28:     */   
/*   29:     */   @Deprecated
/*   30:     */   public abstract ByteBuf order(ByteOrder paramByteOrder);
/*   31:     */   
/*   32:     */   public abstract ByteBuf unwrap();
/*   33:     */   
/*   34:     */   public abstract boolean isDirect();
/*   35:     */   
/*   36:     */   public abstract boolean isReadOnly();
/*   37:     */   
/*   38:     */   public abstract ByteBuf asReadOnly();
/*   39:     */   
/*   40:     */   public abstract int readerIndex();
/*   41:     */   
/*   42:     */   public abstract ByteBuf readerIndex(int paramInt);
/*   43:     */   
/*   44:     */   public abstract int writerIndex();
/*   45:     */   
/*   46:     */   public abstract ByteBuf writerIndex(int paramInt);
/*   47:     */   
/*   48:     */   public abstract ByteBuf setIndex(int paramInt1, int paramInt2);
/*   49:     */   
/*   50:     */   public abstract int readableBytes();
/*   51:     */   
/*   52:     */   public abstract int writableBytes();
/*   53:     */   
/*   54:     */   public abstract int maxWritableBytes();
/*   55:     */   
/*   56:     */   public abstract boolean isReadable();
/*   57:     */   
/*   58:     */   public abstract boolean isReadable(int paramInt);
/*   59:     */   
/*   60:     */   public abstract boolean isWritable();
/*   61:     */   
/*   62:     */   public abstract boolean isWritable(int paramInt);
/*   63:     */   
/*   64:     */   public abstract ByteBuf clear();
/*   65:     */   
/*   66:     */   public abstract ByteBuf markReaderIndex();
/*   67:     */   
/*   68:     */   public abstract ByteBuf resetReaderIndex();
/*   69:     */   
/*   70:     */   public abstract ByteBuf markWriterIndex();
/*   71:     */   
/*   72:     */   public abstract ByteBuf resetWriterIndex();
/*   73:     */   
/*   74:     */   public abstract ByteBuf discardReadBytes();
/*   75:     */   
/*   76:     */   public abstract ByteBuf discardSomeReadBytes();
/*   77:     */   
/*   78:     */   public abstract ByteBuf ensureWritable(int paramInt);
/*   79:     */   
/*   80:     */   public abstract int ensureWritable(int paramInt, boolean paramBoolean);
/*   81:     */   
/*   82:     */   public abstract boolean getBoolean(int paramInt);
/*   83:     */   
/*   84:     */   public abstract byte getByte(int paramInt);
/*   85:     */   
/*   86:     */   public abstract short getUnsignedByte(int paramInt);
/*   87:     */   
/*   88:     */   public abstract short getShort(int paramInt);
/*   89:     */   
/*   90:     */   public abstract short getShortLE(int paramInt);
/*   91:     */   
/*   92:     */   public abstract int getUnsignedShort(int paramInt);
/*   93:     */   
/*   94:     */   public abstract int getUnsignedShortLE(int paramInt);
/*   95:     */   
/*   96:     */   public abstract int getMedium(int paramInt);
/*   97:     */   
/*   98:     */   public abstract int getMediumLE(int paramInt);
/*   99:     */   
/*  100:     */   public abstract int getUnsignedMedium(int paramInt);
/*  101:     */   
/*  102:     */   public abstract int getUnsignedMediumLE(int paramInt);
/*  103:     */   
/*  104:     */   public abstract int getInt(int paramInt);
/*  105:     */   
/*  106:     */   public abstract int getIntLE(int paramInt);
/*  107:     */   
/*  108:     */   public abstract long getUnsignedInt(int paramInt);
/*  109:     */   
/*  110:     */   public abstract long getUnsignedIntLE(int paramInt);
/*  111:     */   
/*  112:     */   public abstract long getLong(int paramInt);
/*  113:     */   
/*  114:     */   public abstract long getLongLE(int paramInt);
/*  115:     */   
/*  116:     */   public abstract char getChar(int paramInt);
/*  117:     */   
/*  118:     */   public abstract float getFloat(int paramInt);
/*  119:     */   
/*  120:     */   public float getFloatLE(int index)
/*  121:     */   {
/*  122: 771 */     return Float.intBitsToFloat(getIntLE(index));
/*  123:     */   }
/*  124:     */   
/*  125:     */   public abstract double getDouble(int paramInt);
/*  126:     */   
/*  127:     */   public double getDoubleLE(int index)
/*  128:     */   {
/*  129: 796 */     return Double.longBitsToDouble(getLongLE(index));
/*  130:     */   }
/*  131:     */   
/*  132:     */   public abstract ByteBuf getBytes(int paramInt, ByteBuf paramByteBuf);
/*  133:     */   
/*  134:     */   public abstract ByteBuf getBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2);
/*  135:     */   
/*  136:     */   public abstract ByteBuf getBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2, int paramInt3);
/*  137:     */   
/*  138:     */   public abstract ByteBuf getBytes(int paramInt, byte[] paramArrayOfByte);
/*  139:     */   
/*  140:     */   public abstract ByteBuf getBytes(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3);
/*  141:     */   
/*  142:     */   public abstract ByteBuf getBytes(int paramInt, ByteBuffer paramByteBuffer);
/*  143:     */   
/*  144:     */   public abstract ByteBuf getBytes(int paramInt1, OutputStream paramOutputStream, int paramInt2)
/*  145:     */     throws IOException;
/*  146:     */   
/*  147:     */   public abstract int getBytes(int paramInt1, GatheringByteChannel paramGatheringByteChannel, int paramInt2)
/*  148:     */     throws IOException;
/*  149:     */   
/*  150:     */   public abstract int getBytes(int paramInt1, FileChannel paramFileChannel, long paramLong, int paramInt2)
/*  151:     */     throws IOException;
/*  152:     */   
/*  153:     */   public abstract CharSequence getCharSequence(int paramInt1, int paramInt2, Charset paramCharset);
/*  154:     */   
/*  155:     */   public abstract ByteBuf setBoolean(int paramInt, boolean paramBoolean);
/*  156:     */   
/*  157:     */   public abstract ByteBuf setByte(int paramInt1, int paramInt2);
/*  158:     */   
/*  159:     */   public abstract ByteBuf setShort(int paramInt1, int paramInt2);
/*  160:     */   
/*  161:     */   public abstract ByteBuf setShortLE(int paramInt1, int paramInt2);
/*  162:     */   
/*  163:     */   public abstract ByteBuf setMedium(int paramInt1, int paramInt2);
/*  164:     */   
/*  165:     */   public abstract ByteBuf setMediumLE(int paramInt1, int paramInt2);
/*  166:     */   
/*  167:     */   public abstract ByteBuf setInt(int paramInt1, int paramInt2);
/*  168:     */   
/*  169:     */   public abstract ByteBuf setIntLE(int paramInt1, int paramInt2);
/*  170:     */   
/*  171:     */   public abstract ByteBuf setLong(int paramInt, long paramLong);
/*  172:     */   
/*  173:     */   public abstract ByteBuf setLongLE(int paramInt, long paramLong);
/*  174:     */   
/*  175:     */   public abstract ByteBuf setChar(int paramInt1, int paramInt2);
/*  176:     */   
/*  177:     */   public abstract ByteBuf setFloat(int paramInt, float paramFloat);
/*  178:     */   
/*  179:     */   public ByteBuf setFloatLE(int index, float value)
/*  180:     */   {
/*  181:1131 */     return setIntLE(index, Float.floatToRawIntBits(value));
/*  182:     */   }
/*  183:     */   
/*  184:     */   public abstract ByteBuf setDouble(int paramInt, double paramDouble);
/*  185:     */   
/*  186:     */   public ByteBuf setDoubleLE(int index, double value)
/*  187:     */   {
/*  188:1157 */     return setLongLE(index, Double.doubleToRawLongBits(value));
/*  189:     */   }
/*  190:     */   
/*  191:     */   public abstract ByteBuf setBytes(int paramInt, ByteBuf paramByteBuf);
/*  192:     */   
/*  193:     */   public abstract ByteBuf setBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2);
/*  194:     */   
/*  195:     */   public abstract ByteBuf setBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2, int paramInt3);
/*  196:     */   
/*  197:     */   public abstract ByteBuf setBytes(int paramInt, byte[] paramArrayOfByte);
/*  198:     */   
/*  199:     */   public abstract ByteBuf setBytes(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3);
/*  200:     */   
/*  201:     */   public abstract ByteBuf setBytes(int paramInt, ByteBuffer paramByteBuffer);
/*  202:     */   
/*  203:     */   public abstract int setBytes(int paramInt1, InputStream paramInputStream, int paramInt2)
/*  204:     */     throws IOException;
/*  205:     */   
/*  206:     */   public abstract int setBytes(int paramInt1, ScatteringByteChannel paramScatteringByteChannel, int paramInt2)
/*  207:     */     throws IOException;
/*  208:     */   
/*  209:     */   public abstract int setBytes(int paramInt1, FileChannel paramFileChannel, long paramLong, int paramInt2)
/*  210:     */     throws IOException;
/*  211:     */   
/*  212:     */   public abstract ByteBuf setZero(int paramInt1, int paramInt2);
/*  213:     */   
/*  214:     */   public abstract int setCharSequence(int paramInt, CharSequence paramCharSequence, Charset paramCharset);
/*  215:     */   
/*  216:     */   public abstract boolean readBoolean();
/*  217:     */   
/*  218:     */   public abstract byte readByte();
/*  219:     */   
/*  220:     */   public abstract short readUnsignedByte();
/*  221:     */   
/*  222:     */   public abstract short readShort();
/*  223:     */   
/*  224:     */   public abstract short readShortLE();
/*  225:     */   
/*  226:     */   public abstract int readUnsignedShort();
/*  227:     */   
/*  228:     */   public abstract int readUnsignedShortLE();
/*  229:     */   
/*  230:     */   public abstract int readMedium();
/*  231:     */   
/*  232:     */   public abstract int readMediumLE();
/*  233:     */   
/*  234:     */   public abstract int readUnsignedMedium();
/*  235:     */   
/*  236:     */   public abstract int readUnsignedMediumLE();
/*  237:     */   
/*  238:     */   public abstract int readInt();
/*  239:     */   
/*  240:     */   public abstract int readIntLE();
/*  241:     */   
/*  242:     */   public abstract long readUnsignedInt();
/*  243:     */   
/*  244:     */   public abstract long readUnsignedIntLE();
/*  245:     */   
/*  246:     */   public abstract long readLong();
/*  247:     */   
/*  248:     */   public abstract long readLongLE();
/*  249:     */   
/*  250:     */   public abstract char readChar();
/*  251:     */   
/*  252:     */   public abstract float readFloat();
/*  253:     */   
/*  254:     */   public float readFloatLE()
/*  255:     */   {
/*  256:1531 */     return Float.intBitsToFloat(readIntLE());
/*  257:     */   }
/*  258:     */   
/*  259:     */   public abstract double readDouble();
/*  260:     */   
/*  261:     */   public double readDoubleLE()
/*  262:     */   {
/*  263:1552 */     return Double.longBitsToDouble(readLongLE());
/*  264:     */   }
/*  265:     */   
/*  266:     */   public abstract ByteBuf readBytes(int paramInt);
/*  267:     */   
/*  268:     */   public abstract ByteBuf readSlice(int paramInt);
/*  269:     */   
/*  270:     */   public abstract ByteBuf readRetainedSlice(int paramInt);
/*  271:     */   
/*  272:     */   public abstract ByteBuf readBytes(ByteBuf paramByteBuf);
/*  273:     */   
/*  274:     */   public abstract ByteBuf readBytes(ByteBuf paramByteBuf, int paramInt);
/*  275:     */   
/*  276:     */   public abstract ByteBuf readBytes(ByteBuf paramByteBuf, int paramInt1, int paramInt2);
/*  277:     */   
/*  278:     */   public abstract ByteBuf readBytes(byte[] paramArrayOfByte);
/*  279:     */   
/*  280:     */   public abstract ByteBuf readBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2);
/*  281:     */   
/*  282:     */   public abstract ByteBuf readBytes(ByteBuffer paramByteBuffer);
/*  283:     */   
/*  284:     */   public abstract ByteBuf readBytes(OutputStream paramOutputStream, int paramInt)
/*  285:     */     throws IOException;
/*  286:     */   
/*  287:     */   public abstract int readBytes(GatheringByteChannel paramGatheringByteChannel, int paramInt)
/*  288:     */     throws IOException;
/*  289:     */   
/*  290:     */   public abstract CharSequence readCharSequence(int paramInt, Charset paramCharset);
/*  291:     */   
/*  292:     */   public abstract int readBytes(FileChannel paramFileChannel, long paramLong, int paramInt)
/*  293:     */     throws IOException;
/*  294:     */   
/*  295:     */   public abstract ByteBuf skipBytes(int paramInt);
/*  296:     */   
/*  297:     */   public abstract ByteBuf writeBoolean(boolean paramBoolean);
/*  298:     */   
/*  299:     */   public abstract ByteBuf writeByte(int paramInt);
/*  300:     */   
/*  301:     */   public abstract ByteBuf writeShort(int paramInt);
/*  302:     */   
/*  303:     */   public abstract ByteBuf writeShortLE(int paramInt);
/*  304:     */   
/*  305:     */   public abstract ByteBuf writeMedium(int paramInt);
/*  306:     */   
/*  307:     */   public abstract ByteBuf writeMediumLE(int paramInt);
/*  308:     */   
/*  309:     */   public abstract ByteBuf writeInt(int paramInt);
/*  310:     */   
/*  311:     */   public abstract ByteBuf writeIntLE(int paramInt);
/*  312:     */   
/*  313:     */   public abstract ByteBuf writeLong(long paramLong);
/*  314:     */   
/*  315:     */   public abstract ByteBuf writeLongLE(long paramLong);
/*  316:     */   
/*  317:     */   public abstract ByteBuf writeChar(int paramInt);
/*  318:     */   
/*  319:     */   public abstract ByteBuf writeFloat(float paramFloat);
/*  320:     */   
/*  321:     */   public ByteBuf writeFloatLE(float value)
/*  322:     */   {
/*  323:1886 */     return writeIntLE(Float.floatToRawIntBits(value));
/*  324:     */   }
/*  325:     */   
/*  326:     */   public abstract ByteBuf writeDouble(double paramDouble);
/*  327:     */   
/*  328:     */   public ByteBuf writeDoubleLE(double value)
/*  329:     */   {
/*  330:1908 */     return writeLongLE(Double.doubleToRawLongBits(value));
/*  331:     */   }
/*  332:     */   
/*  333:     */   public abstract ByteBuf writeBytes(ByteBuf paramByteBuf);
/*  334:     */   
/*  335:     */   public abstract ByteBuf writeBytes(ByteBuf paramByteBuf, int paramInt);
/*  336:     */   
/*  337:     */   public abstract ByteBuf writeBytes(ByteBuf paramByteBuf, int paramInt1, int paramInt2);
/*  338:     */   
/*  339:     */   public abstract ByteBuf writeBytes(byte[] paramArrayOfByte);
/*  340:     */   
/*  341:     */   public abstract ByteBuf writeBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2);
/*  342:     */   
/*  343:     */   public abstract ByteBuf writeBytes(ByteBuffer paramByteBuffer);
/*  344:     */   
/*  345:     */   public abstract int writeBytes(InputStream paramInputStream, int paramInt)
/*  346:     */     throws IOException;
/*  347:     */   
/*  348:     */   public abstract int writeBytes(ScatteringByteChannel paramScatteringByteChannel, int paramInt)
/*  349:     */     throws IOException;
/*  350:     */   
/*  351:     */   public abstract int writeBytes(FileChannel paramFileChannel, long paramLong, int paramInt)
/*  352:     */     throws IOException;
/*  353:     */   
/*  354:     */   public abstract ByteBuf writeZero(int paramInt);
/*  355:     */   
/*  356:     */   public abstract int writeCharSequence(CharSequence paramCharSequence, Charset paramCharset);
/*  357:     */   
/*  358:     */   public abstract int indexOf(int paramInt1, int paramInt2, byte paramByte);
/*  359:     */   
/*  360:     */   public abstract int bytesBefore(byte paramByte);
/*  361:     */   
/*  362:     */   public abstract int bytesBefore(int paramInt, byte paramByte);
/*  363:     */   
/*  364:     */   public abstract int bytesBefore(int paramInt1, int paramInt2, byte paramByte);
/*  365:     */   
/*  366:     */   public abstract int forEachByte(ByteProcessor paramByteProcessor);
/*  367:     */   
/*  368:     */   public abstract int forEachByte(int paramInt1, int paramInt2, ByteProcessor paramByteProcessor);
/*  369:     */   
/*  370:     */   public abstract int forEachByteDesc(ByteProcessor paramByteProcessor);
/*  371:     */   
/*  372:     */   public abstract int forEachByteDesc(int paramInt1, int paramInt2, ByteProcessor paramByteProcessor);
/*  373:     */   
/*  374:     */   public abstract ByteBuf copy();
/*  375:     */   
/*  376:     */   public abstract ByteBuf copy(int paramInt1, int paramInt2);
/*  377:     */   
/*  378:     */   public abstract ByteBuf slice();
/*  379:     */   
/*  380:     */   public abstract ByteBuf retainedSlice();
/*  381:     */   
/*  382:     */   public abstract ByteBuf slice(int paramInt1, int paramInt2);
/*  383:     */   
/*  384:     */   public abstract ByteBuf retainedSlice(int paramInt1, int paramInt2);
/*  385:     */   
/*  386:     */   public abstract ByteBuf duplicate();
/*  387:     */   
/*  388:     */   public abstract ByteBuf retainedDuplicate();
/*  389:     */   
/*  390:     */   public abstract int nioBufferCount();
/*  391:     */   
/*  392:     */   public abstract ByteBuffer nioBuffer();
/*  393:     */   
/*  394:     */   public abstract ByteBuffer nioBuffer(int paramInt1, int paramInt2);
/*  395:     */   
/*  396:     */   public abstract ByteBuffer internalNioBuffer(int paramInt1, int paramInt2);
/*  397:     */   
/*  398:     */   public abstract ByteBuffer[] nioBuffers();
/*  399:     */   
/*  400:     */   public abstract ByteBuffer[] nioBuffers(int paramInt1, int paramInt2);
/*  401:     */   
/*  402:     */   public abstract boolean hasArray();
/*  403:     */   
/*  404:     */   public abstract byte[] array();
/*  405:     */   
/*  406:     */   public abstract int arrayOffset();
/*  407:     */   
/*  408:     */   public abstract boolean hasMemoryAddress();
/*  409:     */   
/*  410:     */   public abstract long memoryAddress();
/*  411:     */   
/*  412:     */   public abstract String toString(Charset paramCharset);
/*  413:     */   
/*  414:     */   public abstract String toString(int paramInt1, int paramInt2, Charset paramCharset);
/*  415:     */   
/*  416:     */   public abstract int hashCode();
/*  417:     */   
/*  418:     */   public abstract boolean equals(Object paramObject);
/*  419:     */   
/*  420:     */   public abstract int compareTo(ByteBuf paramByteBuf);
/*  421:     */   
/*  422:     */   public abstract String toString();
/*  423:     */   
/*  424:     */   public abstract ByteBuf retain(int paramInt);
/*  425:     */   
/*  426:     */   public abstract ByteBuf retain();
/*  427:     */   
/*  428:     */   public abstract ByteBuf touch();
/*  429:     */   
/*  430:     */   public abstract ByteBuf touch(Object paramObject);
/*  431:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBuf
 * JD-Core Version:    0.7.0.1
 */