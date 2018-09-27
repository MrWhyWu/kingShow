/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ByteProcessor;
/*    4:     */ import io.netty.util.internal.EmptyArrays;
/*    5:     */ import io.netty.util.internal.PlatformDependent;
/*    6:     */ import io.netty.util.internal.StringUtil;
/*    7:     */ import java.io.InputStream;
/*    8:     */ import java.io.OutputStream;
/*    9:     */ import java.nio.ByteBuffer;
/*   10:     */ import java.nio.ByteOrder;
/*   11:     */ import java.nio.ReadOnlyBufferException;
/*   12:     */ import java.nio.channels.FileChannel;
/*   13:     */ import java.nio.channels.GatheringByteChannel;
/*   14:     */ import java.nio.channels.ScatteringByteChannel;
/*   15:     */ import java.nio.charset.Charset;
/*   16:     */ 
/*   17:     */ public final class EmptyByteBuf
/*   18:     */   extends ByteBuf
/*   19:     */ {
/*   20:  39 */   private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocateDirect(0);
/*   21:     */   private static final long EMPTY_BYTE_BUFFER_ADDRESS;
/*   22:     */   private final ByteBufAllocator alloc;
/*   23:     */   private final ByteOrder order;
/*   24:     */   private final String str;
/*   25:     */   private EmptyByteBuf swapped;
/*   26:     */   
/*   27:     */   static
/*   28:     */   {
/*   29:  43 */     long emptyByteBufferAddress = 0L;
/*   30:     */     try
/*   31:     */     {
/*   32:  45 */       if (PlatformDependent.hasUnsafe()) {
/*   33:  46 */         emptyByteBufferAddress = PlatformDependent.directBufferAddress(EMPTY_BYTE_BUFFER);
/*   34:     */       }
/*   35:     */     }
/*   36:     */     catch (Throwable localThrowable) {}
/*   37:  51 */     EMPTY_BYTE_BUFFER_ADDRESS = emptyByteBufferAddress;
/*   38:     */   }
/*   39:     */   
/*   40:     */   public EmptyByteBuf(ByteBufAllocator alloc)
/*   41:     */   {
/*   42:  60 */     this(alloc, ByteOrder.BIG_ENDIAN);
/*   43:     */   }
/*   44:     */   
/*   45:     */   private EmptyByteBuf(ByteBufAllocator alloc, ByteOrder order)
/*   46:     */   {
/*   47:  64 */     if (alloc == null) {
/*   48:  65 */       throw new NullPointerException("alloc");
/*   49:     */     }
/*   50:  68 */     this.alloc = alloc;
/*   51:  69 */     this.order = order;
/*   52:  70 */     this.str = (StringUtil.simpleClassName(this) + (order == ByteOrder.BIG_ENDIAN ? "BE" : "LE"));
/*   53:     */   }
/*   54:     */   
/*   55:     */   public int capacity()
/*   56:     */   {
/*   57:  75 */     return 0;
/*   58:     */   }
/*   59:     */   
/*   60:     */   public ByteBuf capacity(int newCapacity)
/*   61:     */   {
/*   62:  80 */     throw new ReadOnlyBufferException();
/*   63:     */   }
/*   64:     */   
/*   65:     */   public ByteBufAllocator alloc()
/*   66:     */   {
/*   67:  85 */     return this.alloc;
/*   68:     */   }
/*   69:     */   
/*   70:     */   public ByteOrder order()
/*   71:     */   {
/*   72:  90 */     return this.order;
/*   73:     */   }
/*   74:     */   
/*   75:     */   public ByteBuf unwrap()
/*   76:     */   {
/*   77:  95 */     return null;
/*   78:     */   }
/*   79:     */   
/*   80:     */   public ByteBuf asReadOnly()
/*   81:     */   {
/*   82: 100 */     return Unpooled.unmodifiableBuffer(this);
/*   83:     */   }
/*   84:     */   
/*   85:     */   public boolean isReadOnly()
/*   86:     */   {
/*   87: 105 */     return false;
/*   88:     */   }
/*   89:     */   
/*   90:     */   public boolean isDirect()
/*   91:     */   {
/*   92: 110 */     return true;
/*   93:     */   }
/*   94:     */   
/*   95:     */   public int maxCapacity()
/*   96:     */   {
/*   97: 115 */     return 0;
/*   98:     */   }
/*   99:     */   
/*  100:     */   public ByteBuf order(ByteOrder endianness)
/*  101:     */   {
/*  102: 120 */     if (endianness == null) {
/*  103: 121 */       throw new NullPointerException("endianness");
/*  104:     */     }
/*  105: 123 */     if (endianness == order()) {
/*  106: 124 */       return this;
/*  107:     */     }
/*  108: 127 */     EmptyByteBuf swapped = this.swapped;
/*  109: 128 */     if (swapped != null) {
/*  110: 129 */       return swapped;
/*  111:     */     }
/*  112: 132 */     this.swapped = (swapped = new EmptyByteBuf(alloc(), endianness));
/*  113: 133 */     return swapped;
/*  114:     */   }
/*  115:     */   
/*  116:     */   public int readerIndex()
/*  117:     */   {
/*  118: 138 */     return 0;
/*  119:     */   }
/*  120:     */   
/*  121:     */   public ByteBuf readerIndex(int readerIndex)
/*  122:     */   {
/*  123: 143 */     return checkIndex(readerIndex);
/*  124:     */   }
/*  125:     */   
/*  126:     */   public int writerIndex()
/*  127:     */   {
/*  128: 148 */     return 0;
/*  129:     */   }
/*  130:     */   
/*  131:     */   public ByteBuf writerIndex(int writerIndex)
/*  132:     */   {
/*  133: 153 */     return checkIndex(writerIndex);
/*  134:     */   }
/*  135:     */   
/*  136:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*  137:     */   {
/*  138: 158 */     checkIndex(readerIndex);
/*  139: 159 */     checkIndex(writerIndex);
/*  140: 160 */     return this;
/*  141:     */   }
/*  142:     */   
/*  143:     */   public int readableBytes()
/*  144:     */   {
/*  145: 165 */     return 0;
/*  146:     */   }
/*  147:     */   
/*  148:     */   public int writableBytes()
/*  149:     */   {
/*  150: 170 */     return 0;
/*  151:     */   }
/*  152:     */   
/*  153:     */   public int maxWritableBytes()
/*  154:     */   {
/*  155: 175 */     return 0;
/*  156:     */   }
/*  157:     */   
/*  158:     */   public boolean isReadable()
/*  159:     */   {
/*  160: 180 */     return false;
/*  161:     */   }
/*  162:     */   
/*  163:     */   public boolean isWritable()
/*  164:     */   {
/*  165: 185 */     return false;
/*  166:     */   }
/*  167:     */   
/*  168:     */   public ByteBuf clear()
/*  169:     */   {
/*  170: 190 */     return this;
/*  171:     */   }
/*  172:     */   
/*  173:     */   public ByteBuf markReaderIndex()
/*  174:     */   {
/*  175: 195 */     return this;
/*  176:     */   }
/*  177:     */   
/*  178:     */   public ByteBuf resetReaderIndex()
/*  179:     */   {
/*  180: 200 */     return this;
/*  181:     */   }
/*  182:     */   
/*  183:     */   public ByteBuf markWriterIndex()
/*  184:     */   {
/*  185: 205 */     return this;
/*  186:     */   }
/*  187:     */   
/*  188:     */   public ByteBuf resetWriterIndex()
/*  189:     */   {
/*  190: 210 */     return this;
/*  191:     */   }
/*  192:     */   
/*  193:     */   public ByteBuf discardReadBytes()
/*  194:     */   {
/*  195: 215 */     return this;
/*  196:     */   }
/*  197:     */   
/*  198:     */   public ByteBuf discardSomeReadBytes()
/*  199:     */   {
/*  200: 220 */     return this;
/*  201:     */   }
/*  202:     */   
/*  203:     */   public ByteBuf ensureWritable(int minWritableBytes)
/*  204:     */   {
/*  205: 225 */     if (minWritableBytes < 0) {
/*  206: 226 */       throw new IllegalArgumentException("minWritableBytes: " + minWritableBytes + " (expected: >= 0)");
/*  207:     */     }
/*  208: 228 */     if (minWritableBytes != 0) {
/*  209: 229 */       throw new IndexOutOfBoundsException();
/*  210:     */     }
/*  211: 231 */     return this;
/*  212:     */   }
/*  213:     */   
/*  214:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  215:     */   {
/*  216: 236 */     if (minWritableBytes < 0) {
/*  217: 237 */       throw new IllegalArgumentException("minWritableBytes: " + minWritableBytes + " (expected: >= 0)");
/*  218:     */     }
/*  219: 240 */     if (minWritableBytes == 0) {
/*  220: 241 */       return 0;
/*  221:     */     }
/*  222: 244 */     return 1;
/*  223:     */   }
/*  224:     */   
/*  225:     */   public boolean getBoolean(int index)
/*  226:     */   {
/*  227: 249 */     throw new IndexOutOfBoundsException();
/*  228:     */   }
/*  229:     */   
/*  230:     */   public byte getByte(int index)
/*  231:     */   {
/*  232: 254 */     throw new IndexOutOfBoundsException();
/*  233:     */   }
/*  234:     */   
/*  235:     */   public short getUnsignedByte(int index)
/*  236:     */   {
/*  237: 259 */     throw new IndexOutOfBoundsException();
/*  238:     */   }
/*  239:     */   
/*  240:     */   public short getShort(int index)
/*  241:     */   {
/*  242: 264 */     throw new IndexOutOfBoundsException();
/*  243:     */   }
/*  244:     */   
/*  245:     */   public short getShortLE(int index)
/*  246:     */   {
/*  247: 269 */     throw new IndexOutOfBoundsException();
/*  248:     */   }
/*  249:     */   
/*  250:     */   public int getUnsignedShort(int index)
/*  251:     */   {
/*  252: 274 */     throw new IndexOutOfBoundsException();
/*  253:     */   }
/*  254:     */   
/*  255:     */   public int getUnsignedShortLE(int index)
/*  256:     */   {
/*  257: 279 */     throw new IndexOutOfBoundsException();
/*  258:     */   }
/*  259:     */   
/*  260:     */   public int getMedium(int index)
/*  261:     */   {
/*  262: 284 */     throw new IndexOutOfBoundsException();
/*  263:     */   }
/*  264:     */   
/*  265:     */   public int getMediumLE(int index)
/*  266:     */   {
/*  267: 289 */     throw new IndexOutOfBoundsException();
/*  268:     */   }
/*  269:     */   
/*  270:     */   public int getUnsignedMedium(int index)
/*  271:     */   {
/*  272: 294 */     throw new IndexOutOfBoundsException();
/*  273:     */   }
/*  274:     */   
/*  275:     */   public int getUnsignedMediumLE(int index)
/*  276:     */   {
/*  277: 299 */     throw new IndexOutOfBoundsException();
/*  278:     */   }
/*  279:     */   
/*  280:     */   public int getInt(int index)
/*  281:     */   {
/*  282: 304 */     throw new IndexOutOfBoundsException();
/*  283:     */   }
/*  284:     */   
/*  285:     */   public int getIntLE(int index)
/*  286:     */   {
/*  287: 309 */     throw new IndexOutOfBoundsException();
/*  288:     */   }
/*  289:     */   
/*  290:     */   public long getUnsignedInt(int index)
/*  291:     */   {
/*  292: 314 */     throw new IndexOutOfBoundsException();
/*  293:     */   }
/*  294:     */   
/*  295:     */   public long getUnsignedIntLE(int index)
/*  296:     */   {
/*  297: 319 */     throw new IndexOutOfBoundsException();
/*  298:     */   }
/*  299:     */   
/*  300:     */   public long getLong(int index)
/*  301:     */   {
/*  302: 324 */     throw new IndexOutOfBoundsException();
/*  303:     */   }
/*  304:     */   
/*  305:     */   public long getLongLE(int index)
/*  306:     */   {
/*  307: 329 */     throw new IndexOutOfBoundsException();
/*  308:     */   }
/*  309:     */   
/*  310:     */   public char getChar(int index)
/*  311:     */   {
/*  312: 334 */     throw new IndexOutOfBoundsException();
/*  313:     */   }
/*  314:     */   
/*  315:     */   public float getFloat(int index)
/*  316:     */   {
/*  317: 339 */     throw new IndexOutOfBoundsException();
/*  318:     */   }
/*  319:     */   
/*  320:     */   public double getDouble(int index)
/*  321:     */   {
/*  322: 344 */     throw new IndexOutOfBoundsException();
/*  323:     */   }
/*  324:     */   
/*  325:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  326:     */   {
/*  327: 349 */     return checkIndex(index, dst.writableBytes());
/*  328:     */   }
/*  329:     */   
/*  330:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  331:     */   {
/*  332: 354 */     return checkIndex(index, length);
/*  333:     */   }
/*  334:     */   
/*  335:     */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  336:     */   {
/*  337: 359 */     return checkIndex(index, length);
/*  338:     */   }
/*  339:     */   
/*  340:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  341:     */   {
/*  342: 364 */     return checkIndex(index, dst.length);
/*  343:     */   }
/*  344:     */   
/*  345:     */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  346:     */   {
/*  347: 369 */     return checkIndex(index, length);
/*  348:     */   }
/*  349:     */   
/*  350:     */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  351:     */   {
/*  352: 374 */     return checkIndex(index, dst.remaining());
/*  353:     */   }
/*  354:     */   
/*  355:     */   public ByteBuf getBytes(int index, OutputStream out, int length)
/*  356:     */   {
/*  357: 379 */     return checkIndex(index, length);
/*  358:     */   }
/*  359:     */   
/*  360:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  361:     */   {
/*  362: 384 */     checkIndex(index, length);
/*  363: 385 */     return 0;
/*  364:     */   }
/*  365:     */   
/*  366:     */   public int getBytes(int index, FileChannel out, long position, int length)
/*  367:     */   {
/*  368: 390 */     checkIndex(index, length);
/*  369: 391 */     return 0;
/*  370:     */   }
/*  371:     */   
/*  372:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/*  373:     */   {
/*  374: 396 */     checkIndex(index, length);
/*  375: 397 */     return null;
/*  376:     */   }
/*  377:     */   
/*  378:     */   public ByteBuf setBoolean(int index, boolean value)
/*  379:     */   {
/*  380: 402 */     throw new IndexOutOfBoundsException();
/*  381:     */   }
/*  382:     */   
/*  383:     */   public ByteBuf setByte(int index, int value)
/*  384:     */   {
/*  385: 407 */     throw new IndexOutOfBoundsException();
/*  386:     */   }
/*  387:     */   
/*  388:     */   public ByteBuf setShort(int index, int value)
/*  389:     */   {
/*  390: 412 */     throw new IndexOutOfBoundsException();
/*  391:     */   }
/*  392:     */   
/*  393:     */   public ByteBuf setShortLE(int index, int value)
/*  394:     */   {
/*  395: 417 */     throw new IndexOutOfBoundsException();
/*  396:     */   }
/*  397:     */   
/*  398:     */   public ByteBuf setMedium(int index, int value)
/*  399:     */   {
/*  400: 422 */     throw new IndexOutOfBoundsException();
/*  401:     */   }
/*  402:     */   
/*  403:     */   public ByteBuf setMediumLE(int index, int value)
/*  404:     */   {
/*  405: 427 */     throw new IndexOutOfBoundsException();
/*  406:     */   }
/*  407:     */   
/*  408:     */   public ByteBuf setInt(int index, int value)
/*  409:     */   {
/*  410: 432 */     throw new IndexOutOfBoundsException();
/*  411:     */   }
/*  412:     */   
/*  413:     */   public ByteBuf setIntLE(int index, int value)
/*  414:     */   {
/*  415: 437 */     throw new IndexOutOfBoundsException();
/*  416:     */   }
/*  417:     */   
/*  418:     */   public ByteBuf setLong(int index, long value)
/*  419:     */   {
/*  420: 442 */     throw new IndexOutOfBoundsException();
/*  421:     */   }
/*  422:     */   
/*  423:     */   public ByteBuf setLongLE(int index, long value)
/*  424:     */   {
/*  425: 447 */     throw new IndexOutOfBoundsException();
/*  426:     */   }
/*  427:     */   
/*  428:     */   public ByteBuf setChar(int index, int value)
/*  429:     */   {
/*  430: 452 */     throw new IndexOutOfBoundsException();
/*  431:     */   }
/*  432:     */   
/*  433:     */   public ByteBuf setFloat(int index, float value)
/*  434:     */   {
/*  435: 457 */     throw new IndexOutOfBoundsException();
/*  436:     */   }
/*  437:     */   
/*  438:     */   public ByteBuf setDouble(int index, double value)
/*  439:     */   {
/*  440: 462 */     throw new IndexOutOfBoundsException();
/*  441:     */   }
/*  442:     */   
/*  443:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  444:     */   {
/*  445: 467 */     throw new IndexOutOfBoundsException();
/*  446:     */   }
/*  447:     */   
/*  448:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  449:     */   {
/*  450: 472 */     return checkIndex(index, length);
/*  451:     */   }
/*  452:     */   
/*  453:     */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  454:     */   {
/*  455: 477 */     return checkIndex(index, length);
/*  456:     */   }
/*  457:     */   
/*  458:     */   public ByteBuf setBytes(int index, byte[] src)
/*  459:     */   {
/*  460: 482 */     return checkIndex(index, src.length);
/*  461:     */   }
/*  462:     */   
/*  463:     */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  464:     */   {
/*  465: 487 */     return checkIndex(index, length);
/*  466:     */   }
/*  467:     */   
/*  468:     */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  469:     */   {
/*  470: 492 */     return checkIndex(index, src.remaining());
/*  471:     */   }
/*  472:     */   
/*  473:     */   public int setBytes(int index, InputStream in, int length)
/*  474:     */   {
/*  475: 497 */     checkIndex(index, length);
/*  476: 498 */     return 0;
/*  477:     */   }
/*  478:     */   
/*  479:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  480:     */   {
/*  481: 503 */     checkIndex(index, length);
/*  482: 504 */     return 0;
/*  483:     */   }
/*  484:     */   
/*  485:     */   public int setBytes(int index, FileChannel in, long position, int length)
/*  486:     */   {
/*  487: 509 */     checkIndex(index, length);
/*  488: 510 */     return 0;
/*  489:     */   }
/*  490:     */   
/*  491:     */   public ByteBuf setZero(int index, int length)
/*  492:     */   {
/*  493: 515 */     return checkIndex(index, length);
/*  494:     */   }
/*  495:     */   
/*  496:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/*  497:     */   {
/*  498: 520 */     throw new IndexOutOfBoundsException();
/*  499:     */   }
/*  500:     */   
/*  501:     */   public boolean readBoolean()
/*  502:     */   {
/*  503: 525 */     throw new IndexOutOfBoundsException();
/*  504:     */   }
/*  505:     */   
/*  506:     */   public byte readByte()
/*  507:     */   {
/*  508: 530 */     throw new IndexOutOfBoundsException();
/*  509:     */   }
/*  510:     */   
/*  511:     */   public short readUnsignedByte()
/*  512:     */   {
/*  513: 535 */     throw new IndexOutOfBoundsException();
/*  514:     */   }
/*  515:     */   
/*  516:     */   public short readShort()
/*  517:     */   {
/*  518: 540 */     throw new IndexOutOfBoundsException();
/*  519:     */   }
/*  520:     */   
/*  521:     */   public short readShortLE()
/*  522:     */   {
/*  523: 545 */     throw new IndexOutOfBoundsException();
/*  524:     */   }
/*  525:     */   
/*  526:     */   public int readUnsignedShort()
/*  527:     */   {
/*  528: 550 */     throw new IndexOutOfBoundsException();
/*  529:     */   }
/*  530:     */   
/*  531:     */   public int readUnsignedShortLE()
/*  532:     */   {
/*  533: 555 */     throw new IndexOutOfBoundsException();
/*  534:     */   }
/*  535:     */   
/*  536:     */   public int readMedium()
/*  537:     */   {
/*  538: 560 */     throw new IndexOutOfBoundsException();
/*  539:     */   }
/*  540:     */   
/*  541:     */   public int readMediumLE()
/*  542:     */   {
/*  543: 565 */     throw new IndexOutOfBoundsException();
/*  544:     */   }
/*  545:     */   
/*  546:     */   public int readUnsignedMedium()
/*  547:     */   {
/*  548: 570 */     throw new IndexOutOfBoundsException();
/*  549:     */   }
/*  550:     */   
/*  551:     */   public int readUnsignedMediumLE()
/*  552:     */   {
/*  553: 575 */     throw new IndexOutOfBoundsException();
/*  554:     */   }
/*  555:     */   
/*  556:     */   public int readInt()
/*  557:     */   {
/*  558: 580 */     throw new IndexOutOfBoundsException();
/*  559:     */   }
/*  560:     */   
/*  561:     */   public int readIntLE()
/*  562:     */   {
/*  563: 585 */     throw new IndexOutOfBoundsException();
/*  564:     */   }
/*  565:     */   
/*  566:     */   public long readUnsignedInt()
/*  567:     */   {
/*  568: 590 */     throw new IndexOutOfBoundsException();
/*  569:     */   }
/*  570:     */   
/*  571:     */   public long readUnsignedIntLE()
/*  572:     */   {
/*  573: 595 */     throw new IndexOutOfBoundsException();
/*  574:     */   }
/*  575:     */   
/*  576:     */   public long readLong()
/*  577:     */   {
/*  578: 600 */     throw new IndexOutOfBoundsException();
/*  579:     */   }
/*  580:     */   
/*  581:     */   public long readLongLE()
/*  582:     */   {
/*  583: 605 */     throw new IndexOutOfBoundsException();
/*  584:     */   }
/*  585:     */   
/*  586:     */   public char readChar()
/*  587:     */   {
/*  588: 610 */     throw new IndexOutOfBoundsException();
/*  589:     */   }
/*  590:     */   
/*  591:     */   public float readFloat()
/*  592:     */   {
/*  593: 615 */     throw new IndexOutOfBoundsException();
/*  594:     */   }
/*  595:     */   
/*  596:     */   public double readDouble()
/*  597:     */   {
/*  598: 620 */     throw new IndexOutOfBoundsException();
/*  599:     */   }
/*  600:     */   
/*  601:     */   public ByteBuf readBytes(int length)
/*  602:     */   {
/*  603: 625 */     return checkLength(length);
/*  604:     */   }
/*  605:     */   
/*  606:     */   public ByteBuf readSlice(int length)
/*  607:     */   {
/*  608: 630 */     return checkLength(length);
/*  609:     */   }
/*  610:     */   
/*  611:     */   public ByteBuf readRetainedSlice(int length)
/*  612:     */   {
/*  613: 635 */     return checkLength(length);
/*  614:     */   }
/*  615:     */   
/*  616:     */   public ByteBuf readBytes(ByteBuf dst)
/*  617:     */   {
/*  618: 640 */     return checkLength(dst.writableBytes());
/*  619:     */   }
/*  620:     */   
/*  621:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  622:     */   {
/*  623: 645 */     return checkLength(length);
/*  624:     */   }
/*  625:     */   
/*  626:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  627:     */   {
/*  628: 650 */     return checkLength(length);
/*  629:     */   }
/*  630:     */   
/*  631:     */   public ByteBuf readBytes(byte[] dst)
/*  632:     */   {
/*  633: 655 */     return checkLength(dst.length);
/*  634:     */   }
/*  635:     */   
/*  636:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  637:     */   {
/*  638: 660 */     return checkLength(length);
/*  639:     */   }
/*  640:     */   
/*  641:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  642:     */   {
/*  643: 665 */     return checkLength(dst.remaining());
/*  644:     */   }
/*  645:     */   
/*  646:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  647:     */   {
/*  648: 670 */     return checkLength(length);
/*  649:     */   }
/*  650:     */   
/*  651:     */   public int readBytes(GatheringByteChannel out, int length)
/*  652:     */   {
/*  653: 675 */     checkLength(length);
/*  654: 676 */     return 0;
/*  655:     */   }
/*  656:     */   
/*  657:     */   public int readBytes(FileChannel out, long position, int length)
/*  658:     */   {
/*  659: 681 */     checkLength(length);
/*  660: 682 */     return 0;
/*  661:     */   }
/*  662:     */   
/*  663:     */   public CharSequence readCharSequence(int length, Charset charset)
/*  664:     */   {
/*  665: 687 */     checkLength(length);
/*  666: 688 */     return null;
/*  667:     */   }
/*  668:     */   
/*  669:     */   public ByteBuf skipBytes(int length)
/*  670:     */   {
/*  671: 693 */     return checkLength(length);
/*  672:     */   }
/*  673:     */   
/*  674:     */   public ByteBuf writeBoolean(boolean value)
/*  675:     */   {
/*  676: 698 */     throw new IndexOutOfBoundsException();
/*  677:     */   }
/*  678:     */   
/*  679:     */   public ByteBuf writeByte(int value)
/*  680:     */   {
/*  681: 703 */     throw new IndexOutOfBoundsException();
/*  682:     */   }
/*  683:     */   
/*  684:     */   public ByteBuf writeShort(int value)
/*  685:     */   {
/*  686: 708 */     throw new IndexOutOfBoundsException();
/*  687:     */   }
/*  688:     */   
/*  689:     */   public ByteBuf writeShortLE(int value)
/*  690:     */   {
/*  691: 713 */     throw new IndexOutOfBoundsException();
/*  692:     */   }
/*  693:     */   
/*  694:     */   public ByteBuf writeMedium(int value)
/*  695:     */   {
/*  696: 718 */     throw new IndexOutOfBoundsException();
/*  697:     */   }
/*  698:     */   
/*  699:     */   public ByteBuf writeMediumLE(int value)
/*  700:     */   {
/*  701: 723 */     throw new IndexOutOfBoundsException();
/*  702:     */   }
/*  703:     */   
/*  704:     */   public ByteBuf writeInt(int value)
/*  705:     */   {
/*  706: 728 */     throw new IndexOutOfBoundsException();
/*  707:     */   }
/*  708:     */   
/*  709:     */   public ByteBuf writeIntLE(int value)
/*  710:     */   {
/*  711: 733 */     throw new IndexOutOfBoundsException();
/*  712:     */   }
/*  713:     */   
/*  714:     */   public ByteBuf writeLong(long value)
/*  715:     */   {
/*  716: 738 */     throw new IndexOutOfBoundsException();
/*  717:     */   }
/*  718:     */   
/*  719:     */   public ByteBuf writeLongLE(long value)
/*  720:     */   {
/*  721: 743 */     throw new IndexOutOfBoundsException();
/*  722:     */   }
/*  723:     */   
/*  724:     */   public ByteBuf writeChar(int value)
/*  725:     */   {
/*  726: 748 */     throw new IndexOutOfBoundsException();
/*  727:     */   }
/*  728:     */   
/*  729:     */   public ByteBuf writeFloat(float value)
/*  730:     */   {
/*  731: 753 */     throw new IndexOutOfBoundsException();
/*  732:     */   }
/*  733:     */   
/*  734:     */   public ByteBuf writeDouble(double value)
/*  735:     */   {
/*  736: 758 */     throw new IndexOutOfBoundsException();
/*  737:     */   }
/*  738:     */   
/*  739:     */   public ByteBuf writeBytes(ByteBuf src)
/*  740:     */   {
/*  741: 763 */     return checkLength(src.readableBytes());
/*  742:     */   }
/*  743:     */   
/*  744:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/*  745:     */   {
/*  746: 768 */     return checkLength(length);
/*  747:     */   }
/*  748:     */   
/*  749:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  750:     */   {
/*  751: 773 */     return checkLength(length);
/*  752:     */   }
/*  753:     */   
/*  754:     */   public ByteBuf writeBytes(byte[] src)
/*  755:     */   {
/*  756: 778 */     return checkLength(src.length);
/*  757:     */   }
/*  758:     */   
/*  759:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  760:     */   {
/*  761: 783 */     return checkLength(length);
/*  762:     */   }
/*  763:     */   
/*  764:     */   public ByteBuf writeBytes(ByteBuffer src)
/*  765:     */   {
/*  766: 788 */     return checkLength(src.remaining());
/*  767:     */   }
/*  768:     */   
/*  769:     */   public int writeBytes(InputStream in, int length)
/*  770:     */   {
/*  771: 793 */     checkLength(length);
/*  772: 794 */     return 0;
/*  773:     */   }
/*  774:     */   
/*  775:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  776:     */   {
/*  777: 799 */     checkLength(length);
/*  778: 800 */     return 0;
/*  779:     */   }
/*  780:     */   
/*  781:     */   public int writeBytes(FileChannel in, long position, int length)
/*  782:     */   {
/*  783: 805 */     checkLength(length);
/*  784: 806 */     return 0;
/*  785:     */   }
/*  786:     */   
/*  787:     */   public ByteBuf writeZero(int length)
/*  788:     */   {
/*  789: 811 */     return checkLength(length);
/*  790:     */   }
/*  791:     */   
/*  792:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/*  793:     */   {
/*  794: 816 */     throw new IndexOutOfBoundsException();
/*  795:     */   }
/*  796:     */   
/*  797:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  798:     */   {
/*  799: 821 */     checkIndex(fromIndex);
/*  800: 822 */     checkIndex(toIndex);
/*  801: 823 */     return -1;
/*  802:     */   }
/*  803:     */   
/*  804:     */   public int bytesBefore(byte value)
/*  805:     */   {
/*  806: 828 */     return -1;
/*  807:     */   }
/*  808:     */   
/*  809:     */   public int bytesBefore(int length, byte value)
/*  810:     */   {
/*  811: 833 */     checkLength(length);
/*  812: 834 */     return -1;
/*  813:     */   }
/*  814:     */   
/*  815:     */   public int bytesBefore(int index, int length, byte value)
/*  816:     */   {
/*  817: 839 */     checkIndex(index, length);
/*  818: 840 */     return -1;
/*  819:     */   }
/*  820:     */   
/*  821:     */   public int forEachByte(ByteProcessor processor)
/*  822:     */   {
/*  823: 845 */     return -1;
/*  824:     */   }
/*  825:     */   
/*  826:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/*  827:     */   {
/*  828: 850 */     checkIndex(index, length);
/*  829: 851 */     return -1;
/*  830:     */   }
/*  831:     */   
/*  832:     */   public int forEachByteDesc(ByteProcessor processor)
/*  833:     */   {
/*  834: 856 */     return -1;
/*  835:     */   }
/*  836:     */   
/*  837:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/*  838:     */   {
/*  839: 861 */     checkIndex(index, length);
/*  840: 862 */     return -1;
/*  841:     */   }
/*  842:     */   
/*  843:     */   public ByteBuf copy()
/*  844:     */   {
/*  845: 867 */     return this;
/*  846:     */   }
/*  847:     */   
/*  848:     */   public ByteBuf copy(int index, int length)
/*  849:     */   {
/*  850: 872 */     return checkIndex(index, length);
/*  851:     */   }
/*  852:     */   
/*  853:     */   public ByteBuf slice()
/*  854:     */   {
/*  855: 877 */     return this;
/*  856:     */   }
/*  857:     */   
/*  858:     */   public ByteBuf retainedSlice()
/*  859:     */   {
/*  860: 882 */     return this;
/*  861:     */   }
/*  862:     */   
/*  863:     */   public ByteBuf slice(int index, int length)
/*  864:     */   {
/*  865: 887 */     return checkIndex(index, length);
/*  866:     */   }
/*  867:     */   
/*  868:     */   public ByteBuf retainedSlice(int index, int length)
/*  869:     */   {
/*  870: 892 */     return checkIndex(index, length);
/*  871:     */   }
/*  872:     */   
/*  873:     */   public ByteBuf duplicate()
/*  874:     */   {
/*  875: 897 */     return this;
/*  876:     */   }
/*  877:     */   
/*  878:     */   public ByteBuf retainedDuplicate()
/*  879:     */   {
/*  880: 902 */     return this;
/*  881:     */   }
/*  882:     */   
/*  883:     */   public int nioBufferCount()
/*  884:     */   {
/*  885: 907 */     return 1;
/*  886:     */   }
/*  887:     */   
/*  888:     */   public ByteBuffer nioBuffer()
/*  889:     */   {
/*  890: 912 */     return EMPTY_BYTE_BUFFER;
/*  891:     */   }
/*  892:     */   
/*  893:     */   public ByteBuffer nioBuffer(int index, int length)
/*  894:     */   {
/*  895: 917 */     checkIndex(index, length);
/*  896: 918 */     return nioBuffer();
/*  897:     */   }
/*  898:     */   
/*  899:     */   public ByteBuffer[] nioBuffers()
/*  900:     */   {
/*  901: 923 */     return new ByteBuffer[] { EMPTY_BYTE_BUFFER };
/*  902:     */   }
/*  903:     */   
/*  904:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  905:     */   {
/*  906: 928 */     checkIndex(index, length);
/*  907: 929 */     return nioBuffers();
/*  908:     */   }
/*  909:     */   
/*  910:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  911:     */   {
/*  912: 934 */     return EMPTY_BYTE_BUFFER;
/*  913:     */   }
/*  914:     */   
/*  915:     */   public boolean hasArray()
/*  916:     */   {
/*  917: 939 */     return true;
/*  918:     */   }
/*  919:     */   
/*  920:     */   public byte[] array()
/*  921:     */   {
/*  922: 944 */     return EmptyArrays.EMPTY_BYTES;
/*  923:     */   }
/*  924:     */   
/*  925:     */   public int arrayOffset()
/*  926:     */   {
/*  927: 949 */     return 0;
/*  928:     */   }
/*  929:     */   
/*  930:     */   public boolean hasMemoryAddress()
/*  931:     */   {
/*  932: 954 */     return EMPTY_BYTE_BUFFER_ADDRESS != 0L;
/*  933:     */   }
/*  934:     */   
/*  935:     */   public long memoryAddress()
/*  936:     */   {
/*  937: 959 */     if (hasMemoryAddress()) {
/*  938: 960 */       return EMPTY_BYTE_BUFFER_ADDRESS;
/*  939:     */     }
/*  940: 962 */     throw new UnsupportedOperationException();
/*  941:     */   }
/*  942:     */   
/*  943:     */   public String toString(Charset charset)
/*  944:     */   {
/*  945: 968 */     return "";
/*  946:     */   }
/*  947:     */   
/*  948:     */   public String toString(int index, int length, Charset charset)
/*  949:     */   {
/*  950: 973 */     checkIndex(index, length);
/*  951: 974 */     return toString(charset);
/*  952:     */   }
/*  953:     */   
/*  954:     */   public int hashCode()
/*  955:     */   {
/*  956: 979 */     return 0;
/*  957:     */   }
/*  958:     */   
/*  959:     */   public boolean equals(Object obj)
/*  960:     */   {
/*  961: 984 */     return ((obj instanceof ByteBuf)) && (!((ByteBuf)obj).isReadable());
/*  962:     */   }
/*  963:     */   
/*  964:     */   public int compareTo(ByteBuf buffer)
/*  965:     */   {
/*  966: 989 */     return buffer.isReadable() ? -1 : 0;
/*  967:     */   }
/*  968:     */   
/*  969:     */   public String toString()
/*  970:     */   {
/*  971: 994 */     return this.str;
/*  972:     */   }
/*  973:     */   
/*  974:     */   public boolean isReadable(int size)
/*  975:     */   {
/*  976: 999 */     return false;
/*  977:     */   }
/*  978:     */   
/*  979:     */   public boolean isWritable(int size)
/*  980:     */   {
/*  981:1004 */     return false;
/*  982:     */   }
/*  983:     */   
/*  984:     */   public int refCnt()
/*  985:     */   {
/*  986:1009 */     return 1;
/*  987:     */   }
/*  988:     */   
/*  989:     */   public ByteBuf retain()
/*  990:     */   {
/*  991:1014 */     return this;
/*  992:     */   }
/*  993:     */   
/*  994:     */   public ByteBuf retain(int increment)
/*  995:     */   {
/*  996:1019 */     return this;
/*  997:     */   }
/*  998:     */   
/*  999:     */   public ByteBuf touch()
/* 1000:     */   {
/* 1001:1024 */     return this;
/* 1002:     */   }
/* 1003:     */   
/* 1004:     */   public ByteBuf touch(Object hint)
/* 1005:     */   {
/* 1006:1029 */     return this;
/* 1007:     */   }
/* 1008:     */   
/* 1009:     */   public boolean release()
/* 1010:     */   {
/* 1011:1034 */     return false;
/* 1012:     */   }
/* 1013:     */   
/* 1014:     */   public boolean release(int decrement)
/* 1015:     */   {
/* 1016:1039 */     return false;
/* 1017:     */   }
/* 1018:     */   
/* 1019:     */   private ByteBuf checkIndex(int index)
/* 1020:     */   {
/* 1021:1043 */     if (index != 0) {
/* 1022:1044 */       throw new IndexOutOfBoundsException();
/* 1023:     */     }
/* 1024:1046 */     return this;
/* 1025:     */   }
/* 1026:     */   
/* 1027:     */   private ByteBuf checkIndex(int index, int length)
/* 1028:     */   {
/* 1029:1050 */     if (length < 0) {
/* 1030:1051 */       throw new IllegalArgumentException("length: " + length);
/* 1031:     */     }
/* 1032:1053 */     if ((index != 0) || (length != 0)) {
/* 1033:1054 */       throw new IndexOutOfBoundsException();
/* 1034:     */     }
/* 1035:1056 */     return this;
/* 1036:     */   }
/* 1037:     */   
/* 1038:     */   private ByteBuf checkLength(int length)
/* 1039:     */   {
/* 1040:1060 */     if (length < 0) {
/* 1041:1061 */       throw new IllegalArgumentException("length: " + length + " (expected: >= 0)");
/* 1042:     */     }
/* 1043:1063 */     if (length != 0) {
/* 1044:1064 */       throw new IndexOutOfBoundsException();
/* 1045:     */     }
/* 1046:1066 */     return this;
/* 1047:     */   }
/* 1048:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.EmptyByteBuf
 * JD-Core Version:    0.7.0.1
 */