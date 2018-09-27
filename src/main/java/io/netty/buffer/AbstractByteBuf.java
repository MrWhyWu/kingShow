/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ByteProcessor;
/*    4:     */ import io.netty.util.CharsetUtil;
/*    5:     */ import io.netty.util.IllegalReferenceCountException;
/*    6:     */ import io.netty.util.ResourceLeakDetector;
/*    7:     */ import io.netty.util.ResourceLeakDetectorFactory;
/*    8:     */ import io.netty.util.internal.MathUtil;
/*    9:     */ import io.netty.util.internal.PlatformDependent;
/*   10:     */ import io.netty.util.internal.StringUtil;
/*   11:     */ import io.netty.util.internal.SystemPropertyUtil;
/*   12:     */ import io.netty.util.internal.logging.InternalLogger;
/*   13:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   14:     */ import java.io.IOException;
/*   15:     */ import java.io.InputStream;
/*   16:     */ import java.io.OutputStream;
/*   17:     */ import java.nio.ByteBuffer;
/*   18:     */ import java.nio.ByteOrder;
/*   19:     */ import java.nio.channels.FileChannel;
/*   20:     */ import java.nio.channels.GatheringByteChannel;
/*   21:     */ import java.nio.channels.ScatteringByteChannel;
/*   22:     */ import java.nio.charset.Charset;
/*   23:     */ 
/*   24:     */ public abstract class AbstractByteBuf
/*   25:     */   extends ByteBuf
/*   26:     */ {
/*   27:  45 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractByteBuf.class);
/*   28:     */   private static final String PROP_MODE = "io.netty.buffer.bytebuf.checkAccessible";
/*   29:  50 */   private static final boolean checkAccessible = SystemPropertyUtil.getBoolean("io.netty.buffer.bytebuf.checkAccessible", true);
/*   30:     */   
/*   31:     */   static
/*   32:     */   {
/*   33:  51 */     if (logger.isDebugEnabled()) {
/*   34:  52 */       logger.debug("-D{}: {}", "io.netty.buffer.bytebuf.checkAccessible", Boolean.valueOf(checkAccessible));
/*   35:     */     }
/*   36:     */   }
/*   37:     */   
/*   38:  57 */   static final ResourceLeakDetector<ByteBuf> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ByteBuf.class);
/*   39:     */   int readerIndex;
/*   40:     */   int writerIndex;
/*   41:     */   private int markedReaderIndex;
/*   42:     */   private int markedWriterIndex;
/*   43:     */   private int maxCapacity;
/*   44:     */   
/*   45:     */   protected AbstractByteBuf(int maxCapacity)
/*   46:     */   {
/*   47:  66 */     if (maxCapacity < 0) {
/*   48:  67 */       throw new IllegalArgumentException("maxCapacity: " + maxCapacity + " (expected: >= 0)");
/*   49:     */     }
/*   50:  69 */     this.maxCapacity = maxCapacity;
/*   51:     */   }
/*   52:     */   
/*   53:     */   public boolean isReadOnly()
/*   54:     */   {
/*   55:  74 */     return false;
/*   56:     */   }
/*   57:     */   
/*   58:     */   public ByteBuf asReadOnly()
/*   59:     */   {
/*   60:  80 */     if (isReadOnly()) {
/*   61:  81 */       return this;
/*   62:     */     }
/*   63:  83 */     return Unpooled.unmodifiableBuffer(this);
/*   64:     */   }
/*   65:     */   
/*   66:     */   public int maxCapacity()
/*   67:     */   {
/*   68:  88 */     return this.maxCapacity;
/*   69:     */   }
/*   70:     */   
/*   71:     */   protected final void maxCapacity(int maxCapacity)
/*   72:     */   {
/*   73:  92 */     this.maxCapacity = maxCapacity;
/*   74:     */   }
/*   75:     */   
/*   76:     */   public int readerIndex()
/*   77:     */   {
/*   78:  97 */     return this.readerIndex;
/*   79:     */   }
/*   80:     */   
/*   81:     */   public ByteBuf readerIndex(int readerIndex)
/*   82:     */   {
/*   83: 102 */     if ((readerIndex < 0) || (readerIndex > this.writerIndex)) {
/*   84: 103 */       throw new IndexOutOfBoundsException(String.format("readerIndex: %d (expected: 0 <= readerIndex <= writerIndex(%d))", new Object[] {
/*   85: 104 */         Integer.valueOf(readerIndex), Integer.valueOf(this.writerIndex) }));
/*   86:     */     }
/*   87: 106 */     this.readerIndex = readerIndex;
/*   88: 107 */     return this;
/*   89:     */   }
/*   90:     */   
/*   91:     */   public int writerIndex()
/*   92:     */   {
/*   93: 112 */     return this.writerIndex;
/*   94:     */   }
/*   95:     */   
/*   96:     */   public ByteBuf writerIndex(int writerIndex)
/*   97:     */   {
/*   98: 117 */     if ((writerIndex < this.readerIndex) || (writerIndex > capacity())) {
/*   99: 118 */       throw new IndexOutOfBoundsException(String.format("writerIndex: %d (expected: readerIndex(%d) <= writerIndex <= capacity(%d))", new Object[] {
/*  100:     */       
/*  101: 120 */         Integer.valueOf(writerIndex), Integer.valueOf(this.readerIndex), Integer.valueOf(capacity()) }));
/*  102:     */     }
/*  103: 122 */     this.writerIndex = writerIndex;
/*  104: 123 */     return this;
/*  105:     */   }
/*  106:     */   
/*  107:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*  108:     */   {
/*  109: 128 */     if ((readerIndex < 0) || (readerIndex > writerIndex) || (writerIndex > capacity())) {
/*  110: 129 */       throw new IndexOutOfBoundsException(String.format("readerIndex: %d, writerIndex: %d (expected: 0 <= readerIndex <= writerIndex <= capacity(%d))", new Object[] {
/*  111:     */       
/*  112: 131 */         Integer.valueOf(readerIndex), Integer.valueOf(writerIndex), Integer.valueOf(capacity()) }));
/*  113:     */     }
/*  114: 133 */     setIndex0(readerIndex, writerIndex);
/*  115: 134 */     return this;
/*  116:     */   }
/*  117:     */   
/*  118:     */   public ByteBuf clear()
/*  119:     */   {
/*  120: 139 */     this.readerIndex = (this.writerIndex = 0);
/*  121: 140 */     return this;
/*  122:     */   }
/*  123:     */   
/*  124:     */   public boolean isReadable()
/*  125:     */   {
/*  126: 145 */     return this.writerIndex > this.readerIndex;
/*  127:     */   }
/*  128:     */   
/*  129:     */   public boolean isReadable(int numBytes)
/*  130:     */   {
/*  131: 150 */     return this.writerIndex - this.readerIndex >= numBytes;
/*  132:     */   }
/*  133:     */   
/*  134:     */   public boolean isWritable()
/*  135:     */   {
/*  136: 155 */     return capacity() > this.writerIndex;
/*  137:     */   }
/*  138:     */   
/*  139:     */   public boolean isWritable(int numBytes)
/*  140:     */   {
/*  141: 160 */     return capacity() - this.writerIndex >= numBytes;
/*  142:     */   }
/*  143:     */   
/*  144:     */   public int readableBytes()
/*  145:     */   {
/*  146: 165 */     return this.writerIndex - this.readerIndex;
/*  147:     */   }
/*  148:     */   
/*  149:     */   public int writableBytes()
/*  150:     */   {
/*  151: 170 */     return capacity() - this.writerIndex;
/*  152:     */   }
/*  153:     */   
/*  154:     */   public int maxWritableBytes()
/*  155:     */   {
/*  156: 175 */     return maxCapacity() - this.writerIndex;
/*  157:     */   }
/*  158:     */   
/*  159:     */   public ByteBuf markReaderIndex()
/*  160:     */   {
/*  161: 180 */     this.markedReaderIndex = this.readerIndex;
/*  162: 181 */     return this;
/*  163:     */   }
/*  164:     */   
/*  165:     */   public ByteBuf resetReaderIndex()
/*  166:     */   {
/*  167: 186 */     readerIndex(this.markedReaderIndex);
/*  168: 187 */     return this;
/*  169:     */   }
/*  170:     */   
/*  171:     */   public ByteBuf markWriterIndex()
/*  172:     */   {
/*  173: 192 */     this.markedWriterIndex = this.writerIndex;
/*  174: 193 */     return this;
/*  175:     */   }
/*  176:     */   
/*  177:     */   public ByteBuf resetWriterIndex()
/*  178:     */   {
/*  179: 198 */     this.writerIndex = this.markedWriterIndex;
/*  180: 199 */     return this;
/*  181:     */   }
/*  182:     */   
/*  183:     */   public ByteBuf discardReadBytes()
/*  184:     */   {
/*  185: 204 */     ensureAccessible();
/*  186: 205 */     if (this.readerIndex == 0) {
/*  187: 206 */       return this;
/*  188:     */     }
/*  189: 209 */     if (this.readerIndex != this.writerIndex)
/*  190:     */     {
/*  191: 210 */       setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
/*  192: 211 */       this.writerIndex -= this.readerIndex;
/*  193: 212 */       adjustMarkers(this.readerIndex);
/*  194: 213 */       this.readerIndex = 0;
/*  195:     */     }
/*  196:     */     else
/*  197:     */     {
/*  198: 215 */       adjustMarkers(this.readerIndex);
/*  199: 216 */       this.writerIndex = (this.readerIndex = 0);
/*  200:     */     }
/*  201: 218 */     return this;
/*  202:     */   }
/*  203:     */   
/*  204:     */   public ByteBuf discardSomeReadBytes()
/*  205:     */   {
/*  206: 223 */     ensureAccessible();
/*  207: 224 */     if (this.readerIndex == 0) {
/*  208: 225 */       return this;
/*  209:     */     }
/*  210: 228 */     if (this.readerIndex == this.writerIndex)
/*  211:     */     {
/*  212: 229 */       adjustMarkers(this.readerIndex);
/*  213: 230 */       this.writerIndex = (this.readerIndex = 0);
/*  214: 231 */       return this;
/*  215:     */     }
/*  216: 234 */     if (this.readerIndex >= capacity() >>> 1)
/*  217:     */     {
/*  218: 235 */       setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
/*  219: 236 */       this.writerIndex -= this.readerIndex;
/*  220: 237 */       adjustMarkers(this.readerIndex);
/*  221: 238 */       this.readerIndex = 0;
/*  222:     */     }
/*  223: 240 */     return this;
/*  224:     */   }
/*  225:     */   
/*  226:     */   protected final void adjustMarkers(int decrement)
/*  227:     */   {
/*  228: 244 */     int markedReaderIndex = this.markedReaderIndex;
/*  229: 245 */     if (markedReaderIndex <= decrement)
/*  230:     */     {
/*  231: 246 */       this.markedReaderIndex = 0;
/*  232: 247 */       int markedWriterIndex = this.markedWriterIndex;
/*  233: 248 */       if (markedWriterIndex <= decrement) {
/*  234: 249 */         this.markedWriterIndex = 0;
/*  235:     */       } else {
/*  236: 251 */         this.markedWriterIndex = (markedWriterIndex - decrement);
/*  237:     */       }
/*  238:     */     }
/*  239:     */     else
/*  240:     */     {
/*  241: 254 */       this.markedReaderIndex = (markedReaderIndex - decrement);
/*  242: 255 */       this.markedWriterIndex -= decrement;
/*  243:     */     }
/*  244:     */   }
/*  245:     */   
/*  246:     */   public ByteBuf ensureWritable(int minWritableBytes)
/*  247:     */   {
/*  248: 261 */     if (minWritableBytes < 0) {
/*  249: 262 */       throw new IllegalArgumentException(String.format("minWritableBytes: %d (expected: >= 0)", new Object[] {
/*  250: 263 */         Integer.valueOf(minWritableBytes) }));
/*  251:     */     }
/*  252: 265 */     ensureWritable0(minWritableBytes);
/*  253: 266 */     return this;
/*  254:     */   }
/*  255:     */   
/*  256:     */   final void ensureWritable0(int minWritableBytes)
/*  257:     */   {
/*  258: 270 */     ensureAccessible();
/*  259: 271 */     if (minWritableBytes <= writableBytes()) {
/*  260: 272 */       return;
/*  261:     */     }
/*  262: 275 */     if (minWritableBytes > this.maxCapacity - this.writerIndex) {
/*  263: 276 */       throw new IndexOutOfBoundsException(String.format("writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s", new Object[] {
/*  264:     */       
/*  265: 278 */         Integer.valueOf(this.writerIndex), Integer.valueOf(minWritableBytes), Integer.valueOf(this.maxCapacity), this }));
/*  266:     */     }
/*  267: 282 */     int newCapacity = alloc().calculateNewCapacity(this.writerIndex + minWritableBytes, this.maxCapacity);
/*  268:     */     
/*  269:     */ 
/*  270: 285 */     capacity(newCapacity);
/*  271:     */   }
/*  272:     */   
/*  273:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  274:     */   {
/*  275: 290 */     ensureAccessible();
/*  276: 291 */     if (minWritableBytes < 0) {
/*  277: 292 */       throw new IllegalArgumentException(String.format("minWritableBytes: %d (expected: >= 0)", new Object[] {
/*  278: 293 */         Integer.valueOf(minWritableBytes) }));
/*  279:     */     }
/*  280: 296 */     if (minWritableBytes <= writableBytes()) {
/*  281: 297 */       return 0;
/*  282:     */     }
/*  283: 300 */     int maxCapacity = maxCapacity();
/*  284: 301 */     int writerIndex = writerIndex();
/*  285: 302 */     if (minWritableBytes > maxCapacity - writerIndex)
/*  286:     */     {
/*  287: 303 */       if ((!force) || (capacity() == maxCapacity)) {
/*  288: 304 */         return 1;
/*  289:     */       }
/*  290: 307 */       capacity(maxCapacity);
/*  291: 308 */       return 3;
/*  292:     */     }
/*  293: 312 */     int newCapacity = alloc().calculateNewCapacity(writerIndex + minWritableBytes, maxCapacity);
/*  294:     */     
/*  295:     */ 
/*  296: 315 */     capacity(newCapacity);
/*  297: 316 */     return 2;
/*  298:     */   }
/*  299:     */   
/*  300:     */   public ByteBuf order(ByteOrder endianness)
/*  301:     */   {
/*  302: 321 */     if (endianness == null) {
/*  303: 322 */       throw new NullPointerException("endianness");
/*  304:     */     }
/*  305: 324 */     if (endianness == order()) {
/*  306: 325 */       return this;
/*  307:     */     }
/*  308: 327 */     return newSwappedByteBuf();
/*  309:     */   }
/*  310:     */   
/*  311:     */   protected SwappedByteBuf newSwappedByteBuf()
/*  312:     */   {
/*  313: 334 */     return new SwappedByteBuf(this);
/*  314:     */   }
/*  315:     */   
/*  316:     */   public byte getByte(int index)
/*  317:     */   {
/*  318: 339 */     checkIndex(index);
/*  319: 340 */     return _getByte(index);
/*  320:     */   }
/*  321:     */   
/*  322:     */   public boolean getBoolean(int index)
/*  323:     */   {
/*  324: 347 */     return getByte(index) != 0;
/*  325:     */   }
/*  326:     */   
/*  327:     */   public short getUnsignedByte(int index)
/*  328:     */   {
/*  329: 352 */     return (short)(getByte(index) & 0xFF);
/*  330:     */   }
/*  331:     */   
/*  332:     */   public short getShort(int index)
/*  333:     */   {
/*  334: 357 */     checkIndex(index, 2);
/*  335: 358 */     return _getShort(index);
/*  336:     */   }
/*  337:     */   
/*  338:     */   public short getShortLE(int index)
/*  339:     */   {
/*  340: 365 */     checkIndex(index, 2);
/*  341: 366 */     return _getShortLE(index);
/*  342:     */   }
/*  343:     */   
/*  344:     */   public int getUnsignedShort(int index)
/*  345:     */   {
/*  346: 373 */     return getShort(index) & 0xFFFF;
/*  347:     */   }
/*  348:     */   
/*  349:     */   public int getUnsignedShortLE(int index)
/*  350:     */   {
/*  351: 378 */     return getShortLE(index) & 0xFFFF;
/*  352:     */   }
/*  353:     */   
/*  354:     */   public int getUnsignedMedium(int index)
/*  355:     */   {
/*  356: 383 */     checkIndex(index, 3);
/*  357: 384 */     return _getUnsignedMedium(index);
/*  358:     */   }
/*  359:     */   
/*  360:     */   public int getUnsignedMediumLE(int index)
/*  361:     */   {
/*  362: 391 */     checkIndex(index, 3);
/*  363: 392 */     return _getUnsignedMediumLE(index);
/*  364:     */   }
/*  365:     */   
/*  366:     */   public int getMedium(int index)
/*  367:     */   {
/*  368: 399 */     int value = getUnsignedMedium(index);
/*  369: 400 */     if ((value & 0x800000) != 0) {
/*  370: 401 */       value |= 0xFF000000;
/*  371:     */     }
/*  372: 403 */     return value;
/*  373:     */   }
/*  374:     */   
/*  375:     */   public int getMediumLE(int index)
/*  376:     */   {
/*  377: 408 */     int value = getUnsignedMediumLE(index);
/*  378: 409 */     if ((value & 0x800000) != 0) {
/*  379: 410 */       value |= 0xFF000000;
/*  380:     */     }
/*  381: 412 */     return value;
/*  382:     */   }
/*  383:     */   
/*  384:     */   public int getInt(int index)
/*  385:     */   {
/*  386: 417 */     checkIndex(index, 4);
/*  387: 418 */     return _getInt(index);
/*  388:     */   }
/*  389:     */   
/*  390:     */   public int getIntLE(int index)
/*  391:     */   {
/*  392: 425 */     checkIndex(index, 4);
/*  393: 426 */     return _getIntLE(index);
/*  394:     */   }
/*  395:     */   
/*  396:     */   public long getUnsignedInt(int index)
/*  397:     */   {
/*  398: 433 */     return getInt(index) & 0xFFFFFFFF;
/*  399:     */   }
/*  400:     */   
/*  401:     */   public long getUnsignedIntLE(int index)
/*  402:     */   {
/*  403: 438 */     return getIntLE(index) & 0xFFFFFFFF;
/*  404:     */   }
/*  405:     */   
/*  406:     */   public long getLong(int index)
/*  407:     */   {
/*  408: 443 */     checkIndex(index, 8);
/*  409: 444 */     return _getLong(index);
/*  410:     */   }
/*  411:     */   
/*  412:     */   public long getLongLE(int index)
/*  413:     */   {
/*  414: 451 */     checkIndex(index, 8);
/*  415: 452 */     return _getLongLE(index);
/*  416:     */   }
/*  417:     */   
/*  418:     */   public char getChar(int index)
/*  419:     */   {
/*  420: 459 */     return (char)getShort(index);
/*  421:     */   }
/*  422:     */   
/*  423:     */   public float getFloat(int index)
/*  424:     */   {
/*  425: 464 */     return Float.intBitsToFloat(getInt(index));
/*  426:     */   }
/*  427:     */   
/*  428:     */   public double getDouble(int index)
/*  429:     */   {
/*  430: 469 */     return Double.longBitsToDouble(getLong(index));
/*  431:     */   }
/*  432:     */   
/*  433:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  434:     */   {
/*  435: 474 */     getBytes(index, dst, 0, dst.length);
/*  436: 475 */     return this;
/*  437:     */   }
/*  438:     */   
/*  439:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  440:     */   {
/*  441: 480 */     getBytes(index, dst, dst.writableBytes());
/*  442: 481 */     return this;
/*  443:     */   }
/*  444:     */   
/*  445:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  446:     */   {
/*  447: 486 */     getBytes(index, dst, dst.writerIndex(), length);
/*  448: 487 */     dst.writerIndex(dst.writerIndex() + length);
/*  449: 488 */     return this;
/*  450:     */   }
/*  451:     */   
/*  452:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/*  453:     */   {
/*  454: 494 */     return toString(index, length, charset);
/*  455:     */   }
/*  456:     */   
/*  457:     */   public CharSequence readCharSequence(int length, Charset charset)
/*  458:     */   {
/*  459: 499 */     CharSequence sequence = getCharSequence(this.readerIndex, length, charset);
/*  460: 500 */     this.readerIndex += length;
/*  461: 501 */     return sequence;
/*  462:     */   }
/*  463:     */   
/*  464:     */   public ByteBuf setByte(int index, int value)
/*  465:     */   {
/*  466: 506 */     checkIndex(index);
/*  467: 507 */     _setByte(index, value);
/*  468: 508 */     return this;
/*  469:     */   }
/*  470:     */   
/*  471:     */   public ByteBuf setBoolean(int index, boolean value)
/*  472:     */   {
/*  473: 515 */     setByte(index, value ? 1 : 0);
/*  474: 516 */     return this;
/*  475:     */   }
/*  476:     */   
/*  477:     */   public ByteBuf setShort(int index, int value)
/*  478:     */   {
/*  479: 521 */     checkIndex(index, 2);
/*  480: 522 */     _setShort(index, value);
/*  481: 523 */     return this;
/*  482:     */   }
/*  483:     */   
/*  484:     */   public ByteBuf setShortLE(int index, int value)
/*  485:     */   {
/*  486: 530 */     checkIndex(index, 2);
/*  487: 531 */     _setShortLE(index, value);
/*  488: 532 */     return this;
/*  489:     */   }
/*  490:     */   
/*  491:     */   public ByteBuf setChar(int index, int value)
/*  492:     */   {
/*  493: 539 */     setShort(index, value);
/*  494: 540 */     return this;
/*  495:     */   }
/*  496:     */   
/*  497:     */   public ByteBuf setMedium(int index, int value)
/*  498:     */   {
/*  499: 545 */     checkIndex(index, 3);
/*  500: 546 */     _setMedium(index, value);
/*  501: 547 */     return this;
/*  502:     */   }
/*  503:     */   
/*  504:     */   public ByteBuf setMediumLE(int index, int value)
/*  505:     */   {
/*  506: 554 */     checkIndex(index, 3);
/*  507: 555 */     _setMediumLE(index, value);
/*  508: 556 */     return this;
/*  509:     */   }
/*  510:     */   
/*  511:     */   public ByteBuf setInt(int index, int value)
/*  512:     */   {
/*  513: 563 */     checkIndex(index, 4);
/*  514: 564 */     _setInt(index, value);
/*  515: 565 */     return this;
/*  516:     */   }
/*  517:     */   
/*  518:     */   public ByteBuf setIntLE(int index, int value)
/*  519:     */   {
/*  520: 572 */     checkIndex(index, 4);
/*  521: 573 */     _setIntLE(index, value);
/*  522: 574 */     return this;
/*  523:     */   }
/*  524:     */   
/*  525:     */   public ByteBuf setFloat(int index, float value)
/*  526:     */   {
/*  527: 581 */     setInt(index, Float.floatToRawIntBits(value));
/*  528: 582 */     return this;
/*  529:     */   }
/*  530:     */   
/*  531:     */   public ByteBuf setLong(int index, long value)
/*  532:     */   {
/*  533: 587 */     checkIndex(index, 8);
/*  534: 588 */     _setLong(index, value);
/*  535: 589 */     return this;
/*  536:     */   }
/*  537:     */   
/*  538:     */   public ByteBuf setLongLE(int index, long value)
/*  539:     */   {
/*  540: 596 */     checkIndex(index, 8);
/*  541: 597 */     _setLongLE(index, value);
/*  542: 598 */     return this;
/*  543:     */   }
/*  544:     */   
/*  545:     */   public ByteBuf setDouble(int index, double value)
/*  546:     */   {
/*  547: 605 */     setLong(index, Double.doubleToRawLongBits(value));
/*  548: 606 */     return this;
/*  549:     */   }
/*  550:     */   
/*  551:     */   public ByteBuf setBytes(int index, byte[] src)
/*  552:     */   {
/*  553: 611 */     setBytes(index, src, 0, src.length);
/*  554: 612 */     return this;
/*  555:     */   }
/*  556:     */   
/*  557:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  558:     */   {
/*  559: 617 */     setBytes(index, src, src.readableBytes());
/*  560: 618 */     return this;
/*  561:     */   }
/*  562:     */   
/*  563:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  564:     */   {
/*  565: 623 */     checkIndex(index, length);
/*  566: 624 */     if (src == null) {
/*  567: 625 */       throw new NullPointerException("src");
/*  568:     */     }
/*  569: 627 */     if (length > src.readableBytes()) {
/*  570: 628 */       throw new IndexOutOfBoundsException(String.format("length(%d) exceeds src.readableBytes(%d) where src is: %s", new Object[] {
/*  571: 629 */         Integer.valueOf(length), Integer.valueOf(src.readableBytes()), src }));
/*  572:     */     }
/*  573: 632 */     setBytes(index, src, src.readerIndex(), length);
/*  574: 633 */     src.readerIndex(src.readerIndex() + length);
/*  575: 634 */     return this;
/*  576:     */   }
/*  577:     */   
/*  578:     */   public ByteBuf setZero(int index, int length)
/*  579:     */   {
/*  580: 639 */     if (length == 0) {
/*  581: 640 */       return this;
/*  582:     */     }
/*  583: 643 */     checkIndex(index, length);
/*  584:     */     
/*  585: 645 */     int nLong = length >>> 3;
/*  586: 646 */     int nBytes = length & 0x7;
/*  587: 647 */     for (int i = nLong; i > 0; i--)
/*  588:     */     {
/*  589: 648 */       _setLong(index, 0L);
/*  590: 649 */       index += 8;
/*  591:     */     }
/*  592: 651 */     if (nBytes == 4)
/*  593:     */     {
/*  594: 652 */       _setInt(index, 0);
/*  595:     */     }
/*  596: 654 */     else if (nBytes < 4)
/*  597:     */     {
/*  598: 655 */       for (int i = nBytes; i > 0; i--)
/*  599:     */       {
/*  600: 656 */         _setByte(index, 0);
/*  601: 657 */         index++;
/*  602:     */       }
/*  603:     */     }
/*  604:     */     else
/*  605:     */     {
/*  606: 660 */       _setInt(index, 0);
/*  607: 661 */       index += 4;
/*  608: 662 */       for (int i = nBytes - 4; i > 0; i--)
/*  609:     */       {
/*  610: 663 */         _setByte(index, 0);
/*  611: 664 */         index++;
/*  612:     */       }
/*  613:     */     }
/*  614: 667 */     return this;
/*  615:     */   }
/*  616:     */   
/*  617:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/*  618:     */   {
/*  619: 672 */     return setCharSequence0(index, sequence, charset, false);
/*  620:     */   }
/*  621:     */   
/*  622:     */   private int setCharSequence0(int index, CharSequence sequence, Charset charset, boolean expand)
/*  623:     */   {
/*  624: 676 */     if (charset.equals(CharsetUtil.UTF_8))
/*  625:     */     {
/*  626: 677 */       int length = ByteBufUtil.utf8MaxBytes(sequence);
/*  627: 678 */       if (expand)
/*  628:     */       {
/*  629: 679 */         ensureWritable0(length);
/*  630: 680 */         checkIndex0(index, length);
/*  631:     */       }
/*  632:     */       else
/*  633:     */       {
/*  634: 682 */         checkIndex(index, length);
/*  635:     */       }
/*  636: 684 */       return ByteBufUtil.writeUtf8(this, index, sequence, sequence.length());
/*  637:     */     }
/*  638: 686 */     if ((charset.equals(CharsetUtil.US_ASCII)) || (charset.equals(CharsetUtil.ISO_8859_1)))
/*  639:     */     {
/*  640: 687 */       int length = sequence.length();
/*  641: 688 */       if (expand)
/*  642:     */       {
/*  643: 689 */         ensureWritable0(length);
/*  644: 690 */         checkIndex0(index, length);
/*  645:     */       }
/*  646:     */       else
/*  647:     */       {
/*  648: 692 */         checkIndex(index, length);
/*  649:     */       }
/*  650: 694 */       return ByteBufUtil.writeAscii(this, index, sequence, length);
/*  651:     */     }
/*  652: 696 */     byte[] bytes = sequence.toString().getBytes(charset);
/*  653: 697 */     if (expand) {
/*  654: 698 */       ensureWritable0(bytes.length);
/*  655:     */     }
/*  656: 701 */     setBytes(index, bytes);
/*  657: 702 */     return bytes.length;
/*  658:     */   }
/*  659:     */   
/*  660:     */   public byte readByte()
/*  661:     */   {
/*  662: 707 */     checkReadableBytes0(1);
/*  663: 708 */     int i = this.readerIndex;
/*  664: 709 */     byte b = _getByte(i);
/*  665: 710 */     this.readerIndex = (i + 1);
/*  666: 711 */     return b;
/*  667:     */   }
/*  668:     */   
/*  669:     */   public boolean readBoolean()
/*  670:     */   {
/*  671: 716 */     return readByte() != 0;
/*  672:     */   }
/*  673:     */   
/*  674:     */   public short readUnsignedByte()
/*  675:     */   {
/*  676: 721 */     return (short)(readByte() & 0xFF);
/*  677:     */   }
/*  678:     */   
/*  679:     */   public short readShort()
/*  680:     */   {
/*  681: 726 */     checkReadableBytes0(2);
/*  682: 727 */     short v = _getShort(this.readerIndex);
/*  683: 728 */     this.readerIndex += 2;
/*  684: 729 */     return v;
/*  685:     */   }
/*  686:     */   
/*  687:     */   public short readShortLE()
/*  688:     */   {
/*  689: 734 */     checkReadableBytes0(2);
/*  690: 735 */     short v = _getShortLE(this.readerIndex);
/*  691: 736 */     this.readerIndex += 2;
/*  692: 737 */     return v;
/*  693:     */   }
/*  694:     */   
/*  695:     */   public int readUnsignedShort()
/*  696:     */   {
/*  697: 742 */     return readShort() & 0xFFFF;
/*  698:     */   }
/*  699:     */   
/*  700:     */   public int readUnsignedShortLE()
/*  701:     */   {
/*  702: 747 */     return readShortLE() & 0xFFFF;
/*  703:     */   }
/*  704:     */   
/*  705:     */   public int readMedium()
/*  706:     */   {
/*  707: 752 */     int value = readUnsignedMedium();
/*  708: 753 */     if ((value & 0x800000) != 0) {
/*  709: 754 */       value |= 0xFF000000;
/*  710:     */     }
/*  711: 756 */     return value;
/*  712:     */   }
/*  713:     */   
/*  714:     */   public int readMediumLE()
/*  715:     */   {
/*  716: 761 */     int value = readUnsignedMediumLE();
/*  717: 762 */     if ((value & 0x800000) != 0) {
/*  718: 763 */       value |= 0xFF000000;
/*  719:     */     }
/*  720: 765 */     return value;
/*  721:     */   }
/*  722:     */   
/*  723:     */   public int readUnsignedMedium()
/*  724:     */   {
/*  725: 770 */     checkReadableBytes0(3);
/*  726: 771 */     int v = _getUnsignedMedium(this.readerIndex);
/*  727: 772 */     this.readerIndex += 3;
/*  728: 773 */     return v;
/*  729:     */   }
/*  730:     */   
/*  731:     */   public int readUnsignedMediumLE()
/*  732:     */   {
/*  733: 778 */     checkReadableBytes0(3);
/*  734: 779 */     int v = _getUnsignedMediumLE(this.readerIndex);
/*  735: 780 */     this.readerIndex += 3;
/*  736: 781 */     return v;
/*  737:     */   }
/*  738:     */   
/*  739:     */   public int readInt()
/*  740:     */   {
/*  741: 786 */     checkReadableBytes0(4);
/*  742: 787 */     int v = _getInt(this.readerIndex);
/*  743: 788 */     this.readerIndex += 4;
/*  744: 789 */     return v;
/*  745:     */   }
/*  746:     */   
/*  747:     */   public int readIntLE()
/*  748:     */   {
/*  749: 794 */     checkReadableBytes0(4);
/*  750: 795 */     int v = _getIntLE(this.readerIndex);
/*  751: 796 */     this.readerIndex += 4;
/*  752: 797 */     return v;
/*  753:     */   }
/*  754:     */   
/*  755:     */   public long readUnsignedInt()
/*  756:     */   {
/*  757: 802 */     return readInt() & 0xFFFFFFFF;
/*  758:     */   }
/*  759:     */   
/*  760:     */   public long readUnsignedIntLE()
/*  761:     */   {
/*  762: 807 */     return readIntLE() & 0xFFFFFFFF;
/*  763:     */   }
/*  764:     */   
/*  765:     */   public long readLong()
/*  766:     */   {
/*  767: 812 */     checkReadableBytes0(8);
/*  768: 813 */     long v = _getLong(this.readerIndex);
/*  769: 814 */     this.readerIndex += 8;
/*  770: 815 */     return v;
/*  771:     */   }
/*  772:     */   
/*  773:     */   public long readLongLE()
/*  774:     */   {
/*  775: 820 */     checkReadableBytes0(8);
/*  776: 821 */     long v = _getLongLE(this.readerIndex);
/*  777: 822 */     this.readerIndex += 8;
/*  778: 823 */     return v;
/*  779:     */   }
/*  780:     */   
/*  781:     */   public char readChar()
/*  782:     */   {
/*  783: 828 */     return (char)readShort();
/*  784:     */   }
/*  785:     */   
/*  786:     */   public float readFloat()
/*  787:     */   {
/*  788: 833 */     return Float.intBitsToFloat(readInt());
/*  789:     */   }
/*  790:     */   
/*  791:     */   public double readDouble()
/*  792:     */   {
/*  793: 838 */     return Double.longBitsToDouble(readLong());
/*  794:     */   }
/*  795:     */   
/*  796:     */   public ByteBuf readBytes(int length)
/*  797:     */   {
/*  798: 843 */     checkReadableBytes(length);
/*  799: 844 */     if (length == 0) {
/*  800: 845 */       return Unpooled.EMPTY_BUFFER;
/*  801:     */     }
/*  802: 848 */     ByteBuf buf = alloc().buffer(length, this.maxCapacity);
/*  803: 849 */     buf.writeBytes(this, this.readerIndex, length);
/*  804: 850 */     this.readerIndex += length;
/*  805: 851 */     return buf;
/*  806:     */   }
/*  807:     */   
/*  808:     */   public ByteBuf readSlice(int length)
/*  809:     */   {
/*  810: 856 */     checkReadableBytes(length);
/*  811: 857 */     ByteBuf slice = slice(this.readerIndex, length);
/*  812: 858 */     this.readerIndex += length;
/*  813: 859 */     return slice;
/*  814:     */   }
/*  815:     */   
/*  816:     */   public ByteBuf readRetainedSlice(int length)
/*  817:     */   {
/*  818: 864 */     checkReadableBytes(length);
/*  819: 865 */     ByteBuf slice = retainedSlice(this.readerIndex, length);
/*  820: 866 */     this.readerIndex += length;
/*  821: 867 */     return slice;
/*  822:     */   }
/*  823:     */   
/*  824:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  825:     */   {
/*  826: 872 */     checkReadableBytes(length);
/*  827: 873 */     getBytes(this.readerIndex, dst, dstIndex, length);
/*  828: 874 */     this.readerIndex += length;
/*  829: 875 */     return this;
/*  830:     */   }
/*  831:     */   
/*  832:     */   public ByteBuf readBytes(byte[] dst)
/*  833:     */   {
/*  834: 880 */     readBytes(dst, 0, dst.length);
/*  835: 881 */     return this;
/*  836:     */   }
/*  837:     */   
/*  838:     */   public ByteBuf readBytes(ByteBuf dst)
/*  839:     */   {
/*  840: 886 */     readBytes(dst, dst.writableBytes());
/*  841: 887 */     return this;
/*  842:     */   }
/*  843:     */   
/*  844:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  845:     */   {
/*  846: 892 */     if (length > dst.writableBytes()) {
/*  847: 893 */       throw new IndexOutOfBoundsException(String.format("length(%d) exceeds dst.writableBytes(%d) where dst is: %s", new Object[] {
/*  848: 894 */         Integer.valueOf(length), Integer.valueOf(dst.writableBytes()), dst }));
/*  849:     */     }
/*  850: 896 */     readBytes(dst, dst.writerIndex(), length);
/*  851: 897 */     dst.writerIndex(dst.writerIndex() + length);
/*  852: 898 */     return this;
/*  853:     */   }
/*  854:     */   
/*  855:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  856:     */   {
/*  857: 903 */     checkReadableBytes(length);
/*  858: 904 */     getBytes(this.readerIndex, dst, dstIndex, length);
/*  859: 905 */     this.readerIndex += length;
/*  860: 906 */     return this;
/*  861:     */   }
/*  862:     */   
/*  863:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  864:     */   {
/*  865: 911 */     int length = dst.remaining();
/*  866: 912 */     checkReadableBytes(length);
/*  867: 913 */     getBytes(this.readerIndex, dst);
/*  868: 914 */     this.readerIndex += length;
/*  869: 915 */     return this;
/*  870:     */   }
/*  871:     */   
/*  872:     */   public int readBytes(GatheringByteChannel out, int length)
/*  873:     */     throws IOException
/*  874:     */   {
/*  875: 921 */     checkReadableBytes(length);
/*  876: 922 */     int readBytes = getBytes(this.readerIndex, out, length);
/*  877: 923 */     this.readerIndex += readBytes;
/*  878: 924 */     return readBytes;
/*  879:     */   }
/*  880:     */   
/*  881:     */   public int readBytes(FileChannel out, long position, int length)
/*  882:     */     throws IOException
/*  883:     */   {
/*  884: 930 */     checkReadableBytes(length);
/*  885: 931 */     int readBytes = getBytes(this.readerIndex, out, position, length);
/*  886: 932 */     this.readerIndex += readBytes;
/*  887: 933 */     return readBytes;
/*  888:     */   }
/*  889:     */   
/*  890:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  891:     */     throws IOException
/*  892:     */   {
/*  893: 938 */     checkReadableBytes(length);
/*  894: 939 */     getBytes(this.readerIndex, out, length);
/*  895: 940 */     this.readerIndex += length;
/*  896: 941 */     return this;
/*  897:     */   }
/*  898:     */   
/*  899:     */   public ByteBuf skipBytes(int length)
/*  900:     */   {
/*  901: 946 */     checkReadableBytes(length);
/*  902: 947 */     this.readerIndex += length;
/*  903: 948 */     return this;
/*  904:     */   }
/*  905:     */   
/*  906:     */   public ByteBuf writeBoolean(boolean value)
/*  907:     */   {
/*  908: 953 */     writeByte(value ? 1 : 0);
/*  909: 954 */     return this;
/*  910:     */   }
/*  911:     */   
/*  912:     */   public ByteBuf writeByte(int value)
/*  913:     */   {
/*  914: 959 */     ensureWritable0(1);
/*  915: 960 */     _setByte(this.writerIndex++, value);
/*  916: 961 */     return this;
/*  917:     */   }
/*  918:     */   
/*  919:     */   public ByteBuf writeShort(int value)
/*  920:     */   {
/*  921: 966 */     ensureWritable0(2);
/*  922: 967 */     _setShort(this.writerIndex, value);
/*  923: 968 */     this.writerIndex += 2;
/*  924: 969 */     return this;
/*  925:     */   }
/*  926:     */   
/*  927:     */   public ByteBuf writeShortLE(int value)
/*  928:     */   {
/*  929: 974 */     ensureWritable0(2);
/*  930: 975 */     _setShortLE(this.writerIndex, value);
/*  931: 976 */     this.writerIndex += 2;
/*  932: 977 */     return this;
/*  933:     */   }
/*  934:     */   
/*  935:     */   public ByteBuf writeMedium(int value)
/*  936:     */   {
/*  937: 982 */     ensureWritable0(3);
/*  938: 983 */     _setMedium(this.writerIndex, value);
/*  939: 984 */     this.writerIndex += 3;
/*  940: 985 */     return this;
/*  941:     */   }
/*  942:     */   
/*  943:     */   public ByteBuf writeMediumLE(int value)
/*  944:     */   {
/*  945: 990 */     ensureWritable0(3);
/*  946: 991 */     _setMediumLE(this.writerIndex, value);
/*  947: 992 */     this.writerIndex += 3;
/*  948: 993 */     return this;
/*  949:     */   }
/*  950:     */   
/*  951:     */   public ByteBuf writeInt(int value)
/*  952:     */   {
/*  953: 998 */     ensureWritable0(4);
/*  954: 999 */     _setInt(this.writerIndex, value);
/*  955:1000 */     this.writerIndex += 4;
/*  956:1001 */     return this;
/*  957:     */   }
/*  958:     */   
/*  959:     */   public ByteBuf writeIntLE(int value)
/*  960:     */   {
/*  961:1006 */     ensureWritable0(4);
/*  962:1007 */     _setIntLE(this.writerIndex, value);
/*  963:1008 */     this.writerIndex += 4;
/*  964:1009 */     return this;
/*  965:     */   }
/*  966:     */   
/*  967:     */   public ByteBuf writeLong(long value)
/*  968:     */   {
/*  969:1014 */     ensureWritable0(8);
/*  970:1015 */     _setLong(this.writerIndex, value);
/*  971:1016 */     this.writerIndex += 8;
/*  972:1017 */     return this;
/*  973:     */   }
/*  974:     */   
/*  975:     */   public ByteBuf writeLongLE(long value)
/*  976:     */   {
/*  977:1022 */     ensureWritable0(8);
/*  978:1023 */     _setLongLE(this.writerIndex, value);
/*  979:1024 */     this.writerIndex += 8;
/*  980:1025 */     return this;
/*  981:     */   }
/*  982:     */   
/*  983:     */   public ByteBuf writeChar(int value)
/*  984:     */   {
/*  985:1030 */     writeShort(value);
/*  986:1031 */     return this;
/*  987:     */   }
/*  988:     */   
/*  989:     */   public ByteBuf writeFloat(float value)
/*  990:     */   {
/*  991:1036 */     writeInt(Float.floatToRawIntBits(value));
/*  992:1037 */     return this;
/*  993:     */   }
/*  994:     */   
/*  995:     */   public ByteBuf writeDouble(double value)
/*  996:     */   {
/*  997:1042 */     writeLong(Double.doubleToRawLongBits(value));
/*  998:1043 */     return this;
/*  999:     */   }
/* 1000:     */   
/* 1001:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 1002:     */   {
/* 1003:1048 */     ensureWritable(length);
/* 1004:1049 */     setBytes(this.writerIndex, src, srcIndex, length);
/* 1005:1050 */     this.writerIndex += length;
/* 1006:1051 */     return this;
/* 1007:     */   }
/* 1008:     */   
/* 1009:     */   public ByteBuf writeBytes(byte[] src)
/* 1010:     */   {
/* 1011:1056 */     writeBytes(src, 0, src.length);
/* 1012:1057 */     return this;
/* 1013:     */   }
/* 1014:     */   
/* 1015:     */   public ByteBuf writeBytes(ByteBuf src)
/* 1016:     */   {
/* 1017:1062 */     writeBytes(src, src.readableBytes());
/* 1018:1063 */     return this;
/* 1019:     */   }
/* 1020:     */   
/* 1021:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/* 1022:     */   {
/* 1023:1068 */     if (length > src.readableBytes()) {
/* 1024:1069 */       throw new IndexOutOfBoundsException(String.format("length(%d) exceeds src.readableBytes(%d) where src is: %s", new Object[] {
/* 1025:1070 */         Integer.valueOf(length), Integer.valueOf(src.readableBytes()), src }));
/* 1026:     */     }
/* 1027:1072 */     writeBytes(src, src.readerIndex(), length);
/* 1028:1073 */     src.readerIndex(src.readerIndex() + length);
/* 1029:1074 */     return this;
/* 1030:     */   }
/* 1031:     */   
/* 1032:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 1033:     */   {
/* 1034:1079 */     ensureWritable(length);
/* 1035:1080 */     setBytes(this.writerIndex, src, srcIndex, length);
/* 1036:1081 */     this.writerIndex += length;
/* 1037:1082 */     return this;
/* 1038:     */   }
/* 1039:     */   
/* 1040:     */   public ByteBuf writeBytes(ByteBuffer src)
/* 1041:     */   {
/* 1042:1087 */     int length = src.remaining();
/* 1043:1088 */     ensureWritable0(length);
/* 1044:1089 */     setBytes(this.writerIndex, src);
/* 1045:1090 */     this.writerIndex += length;
/* 1046:1091 */     return this;
/* 1047:     */   }
/* 1048:     */   
/* 1049:     */   public int writeBytes(InputStream in, int length)
/* 1050:     */     throws IOException
/* 1051:     */   {
/* 1052:1097 */     ensureWritable(length);
/* 1053:1098 */     int writtenBytes = setBytes(this.writerIndex, in, length);
/* 1054:1099 */     if (writtenBytes > 0) {
/* 1055:1100 */       this.writerIndex += writtenBytes;
/* 1056:     */     }
/* 1057:1102 */     return writtenBytes;
/* 1058:     */   }
/* 1059:     */   
/* 1060:     */   public int writeBytes(ScatteringByteChannel in, int length)
/* 1061:     */     throws IOException
/* 1062:     */   {
/* 1063:1107 */     ensureWritable(length);
/* 1064:1108 */     int writtenBytes = setBytes(this.writerIndex, in, length);
/* 1065:1109 */     if (writtenBytes > 0) {
/* 1066:1110 */       this.writerIndex += writtenBytes;
/* 1067:     */     }
/* 1068:1112 */     return writtenBytes;
/* 1069:     */   }
/* 1070:     */   
/* 1071:     */   public int writeBytes(FileChannel in, long position, int length)
/* 1072:     */     throws IOException
/* 1073:     */   {
/* 1074:1117 */     ensureWritable(length);
/* 1075:1118 */     int writtenBytes = setBytes(this.writerIndex, in, position, length);
/* 1076:1119 */     if (writtenBytes > 0) {
/* 1077:1120 */       this.writerIndex += writtenBytes;
/* 1078:     */     }
/* 1079:1122 */     return writtenBytes;
/* 1080:     */   }
/* 1081:     */   
/* 1082:     */   public ByteBuf writeZero(int length)
/* 1083:     */   {
/* 1084:1127 */     if (length == 0) {
/* 1085:1128 */       return this;
/* 1086:     */     }
/* 1087:1131 */     ensureWritable(length);
/* 1088:1132 */     int wIndex = this.writerIndex;
/* 1089:1133 */     checkIndex0(wIndex, length);
/* 1090:     */     
/* 1091:1135 */     int nLong = length >>> 3;
/* 1092:1136 */     int nBytes = length & 0x7;
/* 1093:1137 */     for (int i = nLong; i > 0; i--)
/* 1094:     */     {
/* 1095:1138 */       _setLong(wIndex, 0L);
/* 1096:1139 */       wIndex += 8;
/* 1097:     */     }
/* 1098:1141 */     if (nBytes == 4)
/* 1099:     */     {
/* 1100:1142 */       _setInt(wIndex, 0);
/* 1101:1143 */       wIndex += 4;
/* 1102:     */     }
/* 1103:1144 */     else if (nBytes < 4)
/* 1104:     */     {
/* 1105:1145 */       for (int i = nBytes; i > 0; i--)
/* 1106:     */       {
/* 1107:1146 */         _setByte(wIndex, 0);
/* 1108:1147 */         wIndex++;
/* 1109:     */       }
/* 1110:     */     }
/* 1111:     */     else
/* 1112:     */     {
/* 1113:1150 */       _setInt(wIndex, 0);
/* 1114:1151 */       wIndex += 4;
/* 1115:1152 */       for (int i = nBytes - 4; i > 0; i--)
/* 1116:     */       {
/* 1117:1153 */         _setByte(wIndex, 0);
/* 1118:1154 */         wIndex++;
/* 1119:     */       }
/* 1120:     */     }
/* 1121:1157 */     this.writerIndex = wIndex;
/* 1122:1158 */     return this;
/* 1123:     */   }
/* 1124:     */   
/* 1125:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/* 1126:     */   {
/* 1127:1163 */     int written = setCharSequence0(this.writerIndex, sequence, charset, true);
/* 1128:1164 */     this.writerIndex += written;
/* 1129:1165 */     return written;
/* 1130:     */   }
/* 1131:     */   
/* 1132:     */   public ByteBuf copy()
/* 1133:     */   {
/* 1134:1170 */     return copy(this.readerIndex, readableBytes());
/* 1135:     */   }
/* 1136:     */   
/* 1137:     */   public ByteBuf duplicate()
/* 1138:     */   {
/* 1139:1175 */     return new UnpooledDuplicatedByteBuf(this);
/* 1140:     */   }
/* 1141:     */   
/* 1142:     */   public ByteBuf retainedDuplicate()
/* 1143:     */   {
/* 1144:1180 */     return duplicate().retain();
/* 1145:     */   }
/* 1146:     */   
/* 1147:     */   public ByteBuf slice()
/* 1148:     */   {
/* 1149:1185 */     return slice(this.readerIndex, readableBytes());
/* 1150:     */   }
/* 1151:     */   
/* 1152:     */   public ByteBuf retainedSlice()
/* 1153:     */   {
/* 1154:1190 */     return slice().retain();
/* 1155:     */   }
/* 1156:     */   
/* 1157:     */   public ByteBuf slice(int index, int length)
/* 1158:     */   {
/* 1159:1195 */     return new UnpooledSlicedByteBuf(this, index, length);
/* 1160:     */   }
/* 1161:     */   
/* 1162:     */   public ByteBuf retainedSlice(int index, int length)
/* 1163:     */   {
/* 1164:1200 */     return slice(index, length).retain();
/* 1165:     */   }
/* 1166:     */   
/* 1167:     */   public ByteBuffer nioBuffer()
/* 1168:     */   {
/* 1169:1205 */     return nioBuffer(this.readerIndex, readableBytes());
/* 1170:     */   }
/* 1171:     */   
/* 1172:     */   public ByteBuffer[] nioBuffers()
/* 1173:     */   {
/* 1174:1210 */     return nioBuffers(this.readerIndex, readableBytes());
/* 1175:     */   }
/* 1176:     */   
/* 1177:     */   public String toString(Charset charset)
/* 1178:     */   {
/* 1179:1215 */     return toString(this.readerIndex, readableBytes(), charset);
/* 1180:     */   }
/* 1181:     */   
/* 1182:     */   public String toString(int index, int length, Charset charset)
/* 1183:     */   {
/* 1184:1220 */     return ByteBufUtil.decodeString(this, index, length, charset);
/* 1185:     */   }
/* 1186:     */   
/* 1187:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/* 1188:     */   {
/* 1189:1225 */     return ByteBufUtil.indexOf(this, fromIndex, toIndex, value);
/* 1190:     */   }
/* 1191:     */   
/* 1192:     */   public int bytesBefore(byte value)
/* 1193:     */   {
/* 1194:1230 */     return bytesBefore(readerIndex(), readableBytes(), value);
/* 1195:     */   }
/* 1196:     */   
/* 1197:     */   public int bytesBefore(int length, byte value)
/* 1198:     */   {
/* 1199:1235 */     checkReadableBytes(length);
/* 1200:1236 */     return bytesBefore(readerIndex(), length, value);
/* 1201:     */   }
/* 1202:     */   
/* 1203:     */   public int bytesBefore(int index, int length, byte value)
/* 1204:     */   {
/* 1205:1241 */     int endIndex = indexOf(index, index + length, value);
/* 1206:1242 */     if (endIndex < 0) {
/* 1207:1243 */       return -1;
/* 1208:     */     }
/* 1209:1245 */     return endIndex - index;
/* 1210:     */   }
/* 1211:     */   
/* 1212:     */   public int forEachByte(ByteProcessor processor)
/* 1213:     */   {
/* 1214:1250 */     ensureAccessible();
/* 1215:     */     try
/* 1216:     */     {
/* 1217:1252 */       return forEachByteAsc0(this.readerIndex, this.writerIndex, processor);
/* 1218:     */     }
/* 1219:     */     catch (Exception e)
/* 1220:     */     {
/* 1221:1254 */       PlatformDependent.throwException(e);
/* 1222:     */     }
/* 1223:1255 */     return -1;
/* 1224:     */   }
/* 1225:     */   
/* 1226:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 1227:     */   {
/* 1228:1261 */     checkIndex(index, length);
/* 1229:     */     try
/* 1230:     */     {
/* 1231:1263 */       return forEachByteAsc0(index, index + length, processor);
/* 1232:     */     }
/* 1233:     */     catch (Exception e)
/* 1234:     */     {
/* 1235:1265 */       PlatformDependent.throwException(e);
/* 1236:     */     }
/* 1237:1266 */     return -1;
/* 1238:     */   }
/* 1239:     */   
/* 1240:     */   private int forEachByteAsc0(int start, int end, ByteProcessor processor)
/* 1241:     */     throws Exception
/* 1242:     */   {
/* 1243:1271 */     for (; start < end; start++) {
/* 1244:1272 */       if (!processor.process(_getByte(start))) {
/* 1245:1273 */         return start;
/* 1246:     */       }
/* 1247:     */     }
/* 1248:1277 */     return -1;
/* 1249:     */   }
/* 1250:     */   
/* 1251:     */   public int forEachByteDesc(ByteProcessor processor)
/* 1252:     */   {
/* 1253:1282 */     ensureAccessible();
/* 1254:     */     try
/* 1255:     */     {
/* 1256:1284 */       return forEachByteDesc0(this.writerIndex - 1, this.readerIndex, processor);
/* 1257:     */     }
/* 1258:     */     catch (Exception e)
/* 1259:     */     {
/* 1260:1286 */       PlatformDependent.throwException(e);
/* 1261:     */     }
/* 1262:1287 */     return -1;
/* 1263:     */   }
/* 1264:     */   
/* 1265:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 1266:     */   {
/* 1267:1293 */     checkIndex(index, length);
/* 1268:     */     try
/* 1269:     */     {
/* 1270:1295 */       return forEachByteDesc0(index + length - 1, index, processor);
/* 1271:     */     }
/* 1272:     */     catch (Exception e)
/* 1273:     */     {
/* 1274:1297 */       PlatformDependent.throwException(e);
/* 1275:     */     }
/* 1276:1298 */     return -1;
/* 1277:     */   }
/* 1278:     */   
/* 1279:     */   private int forEachByteDesc0(int rStart, int rEnd, ByteProcessor processor)
/* 1280:     */     throws Exception
/* 1281:     */   {
/* 1282:1303 */     for (; rStart >= rEnd; rStart--) {
/* 1283:1304 */       if (!processor.process(_getByte(rStart))) {
/* 1284:1305 */         return rStart;
/* 1285:     */       }
/* 1286:     */     }
/* 1287:1308 */     return -1;
/* 1288:     */   }
/* 1289:     */   
/* 1290:     */   public int hashCode()
/* 1291:     */   {
/* 1292:1313 */     return ByteBufUtil.hashCode(this);
/* 1293:     */   }
/* 1294:     */   
/* 1295:     */   public boolean equals(Object o)
/* 1296:     */   {
/* 1297:1318 */     return (this == o) || (((o instanceof ByteBuf)) && (ByteBufUtil.equals(this, (ByteBuf)o)));
/* 1298:     */   }
/* 1299:     */   
/* 1300:     */   public int compareTo(ByteBuf that)
/* 1301:     */   {
/* 1302:1323 */     return ByteBufUtil.compare(this, that);
/* 1303:     */   }
/* 1304:     */   
/* 1305:     */   public String toString()
/* 1306:     */   {
/* 1307:1328 */     if (refCnt() == 0) {
/* 1308:1329 */       return StringUtil.simpleClassName(this) + "(freed)";
/* 1309:     */     }
/* 1310:1336 */     StringBuilder buf = new StringBuilder().append(StringUtil.simpleClassName(this)).append("(ridx: ").append(this.readerIndex).append(", widx: ").append(this.writerIndex).append(", cap: ").append(capacity());
/* 1311:1337 */     if (this.maxCapacity != 2147483647) {
/* 1312:1338 */       buf.append('/').append(this.maxCapacity);
/* 1313:     */     }
/* 1314:1341 */     ByteBuf unwrapped = unwrap();
/* 1315:1342 */     if (unwrapped != null) {
/* 1316:1343 */       buf.append(", unwrapped: ").append(unwrapped);
/* 1317:     */     }
/* 1318:1345 */     buf.append(')');
/* 1319:1346 */     return buf.toString();
/* 1320:     */   }
/* 1321:     */   
/* 1322:     */   protected final void checkIndex(int index)
/* 1323:     */   {
/* 1324:1350 */     checkIndex(index, 1);
/* 1325:     */   }
/* 1326:     */   
/* 1327:     */   protected final void checkIndex(int index, int fieldLength)
/* 1328:     */   {
/* 1329:1354 */     ensureAccessible();
/* 1330:1355 */     checkIndex0(index, fieldLength);
/* 1331:     */   }
/* 1332:     */   
/* 1333:     */   final void checkIndex0(int index, int fieldLength)
/* 1334:     */   {
/* 1335:1359 */     if (MathUtil.isOutOfBounds(index, fieldLength, capacity())) {
/* 1336:1360 */       throw new IndexOutOfBoundsException(String.format("index: %d, length: %d (expected: range(0, %d))", new Object[] {
/* 1337:1361 */         Integer.valueOf(index), Integer.valueOf(fieldLength), Integer.valueOf(capacity()) }));
/* 1338:     */     }
/* 1339:     */   }
/* 1340:     */   
/* 1341:     */   protected final void checkSrcIndex(int index, int length, int srcIndex, int srcCapacity)
/* 1342:     */   {
/* 1343:1366 */     checkIndex(index, length);
/* 1344:1367 */     if (MathUtil.isOutOfBounds(srcIndex, length, srcCapacity)) {
/* 1345:1368 */       throw new IndexOutOfBoundsException(String.format("srcIndex: %d, length: %d (expected: range(0, %d))", new Object[] {
/* 1346:1369 */         Integer.valueOf(srcIndex), Integer.valueOf(length), Integer.valueOf(srcCapacity) }));
/* 1347:     */     }
/* 1348:     */   }
/* 1349:     */   
/* 1350:     */   protected final void checkDstIndex(int index, int length, int dstIndex, int dstCapacity)
/* 1351:     */   {
/* 1352:1374 */     checkIndex(index, length);
/* 1353:1375 */     if (MathUtil.isOutOfBounds(dstIndex, length, dstCapacity)) {
/* 1354:1376 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] {
/* 1355:1377 */         Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dstCapacity) }));
/* 1356:     */     }
/* 1357:     */   }
/* 1358:     */   
/* 1359:     */   protected final void checkReadableBytes(int minimumReadableBytes)
/* 1360:     */   {
/* 1361:1387 */     if (minimumReadableBytes < 0) {
/* 1362:1388 */       throw new IllegalArgumentException("minimumReadableBytes: " + minimumReadableBytes + " (expected: >= 0)");
/* 1363:     */     }
/* 1364:1390 */     checkReadableBytes0(minimumReadableBytes);
/* 1365:     */   }
/* 1366:     */   
/* 1367:     */   protected final void checkNewCapacity(int newCapacity)
/* 1368:     */   {
/* 1369:1394 */     ensureAccessible();
/* 1370:1395 */     if ((newCapacity < 0) || (newCapacity > maxCapacity())) {
/* 1371:1396 */       throw new IllegalArgumentException("newCapacity: " + newCapacity + " (expected: 0-" + maxCapacity() + ')');
/* 1372:     */     }
/* 1373:     */   }
/* 1374:     */   
/* 1375:     */   private void checkReadableBytes0(int minimumReadableBytes)
/* 1376:     */   {
/* 1377:1401 */     ensureAccessible();
/* 1378:1402 */     if (this.readerIndex > this.writerIndex - minimumReadableBytes) {
/* 1379:1403 */       throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s", new Object[] {
/* 1380:     */       
/* 1381:1405 */         Integer.valueOf(this.readerIndex), Integer.valueOf(minimumReadableBytes), Integer.valueOf(this.writerIndex), this }));
/* 1382:     */     }
/* 1383:     */   }
/* 1384:     */   
/* 1385:     */   protected final void ensureAccessible()
/* 1386:     */   {
/* 1387:1414 */     if ((checkAccessible) && (refCnt() == 0)) {
/* 1388:1415 */       throw new IllegalReferenceCountException(0);
/* 1389:     */     }
/* 1390:     */   }
/* 1391:     */   
/* 1392:     */   final void setIndex0(int readerIndex, int writerIndex)
/* 1393:     */   {
/* 1394:1420 */     this.readerIndex = readerIndex;
/* 1395:1421 */     this.writerIndex = writerIndex;
/* 1396:     */   }
/* 1397:     */   
/* 1398:     */   final void discardMarks()
/* 1399:     */   {
/* 1400:1425 */     this.markedReaderIndex = (this.markedWriterIndex = 0);
/* 1401:     */   }
/* 1402:     */   
/* 1403:     */   protected abstract byte _getByte(int paramInt);
/* 1404:     */   
/* 1405:     */   protected abstract short _getShort(int paramInt);
/* 1406:     */   
/* 1407:     */   protected abstract short _getShortLE(int paramInt);
/* 1408:     */   
/* 1409:     */   protected abstract int _getUnsignedMedium(int paramInt);
/* 1410:     */   
/* 1411:     */   protected abstract int _getUnsignedMediumLE(int paramInt);
/* 1412:     */   
/* 1413:     */   protected abstract int _getInt(int paramInt);
/* 1414:     */   
/* 1415:     */   protected abstract int _getIntLE(int paramInt);
/* 1416:     */   
/* 1417:     */   protected abstract long _getLong(int paramInt);
/* 1418:     */   
/* 1419:     */   protected abstract long _getLongLE(int paramInt);
/* 1420:     */   
/* 1421:     */   protected abstract void _setByte(int paramInt1, int paramInt2);
/* 1422:     */   
/* 1423:     */   protected abstract void _setShort(int paramInt1, int paramInt2);
/* 1424:     */   
/* 1425:     */   protected abstract void _setShortLE(int paramInt1, int paramInt2);
/* 1426:     */   
/* 1427:     */   protected abstract void _setMedium(int paramInt1, int paramInt2);
/* 1428:     */   
/* 1429:     */   protected abstract void _setMediumLE(int paramInt1, int paramInt2);
/* 1430:     */   
/* 1431:     */   protected abstract void _setInt(int paramInt1, int paramInt2);
/* 1432:     */   
/* 1433:     */   protected abstract void _setIntLE(int paramInt1, int paramInt2);
/* 1434:     */   
/* 1435:     */   protected abstract void _setLong(int paramInt, long paramLong);
/* 1436:     */   
/* 1437:     */   protected abstract void _setLongLE(int paramInt, long paramLong);
/* 1438:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AbstractByteBuf
 * JD-Core Version:    0.7.0.1
 */