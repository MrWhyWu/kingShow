/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ByteProcessor;
/*    4:     */ import java.io.IOException;
/*    5:     */ import java.io.InputStream;
/*    6:     */ import java.io.OutputStream;
/*    7:     */ import java.nio.ByteBuffer;
/*    8:     */ import java.nio.ByteOrder;
/*    9:     */ import java.nio.channels.FileChannel;
/*   10:     */ import java.nio.channels.GatheringByteChannel;
/*   11:     */ import java.nio.channels.ScatteringByteChannel;
/*   12:     */ import java.nio.charset.Charset;
/*   13:     */ 
/*   14:     */ @Deprecated
/*   15:     */ public class SwappedByteBuf
/*   16:     */   extends ByteBuf
/*   17:     */ {
/*   18:     */   private final ByteBuf buf;
/*   19:     */   private final ByteOrder order;
/*   20:     */   
/*   21:     */   public SwappedByteBuf(ByteBuf buf)
/*   22:     */   {
/*   23:  43 */     if (buf == null) {
/*   24:  44 */       throw new NullPointerException("buf");
/*   25:     */     }
/*   26:  46 */     this.buf = buf;
/*   27:  47 */     if (buf.order() == ByteOrder.BIG_ENDIAN) {
/*   28:  48 */       this.order = ByteOrder.LITTLE_ENDIAN;
/*   29:     */     } else {
/*   30:  50 */       this.order = ByteOrder.BIG_ENDIAN;
/*   31:     */     }
/*   32:     */   }
/*   33:     */   
/*   34:     */   public ByteOrder order()
/*   35:     */   {
/*   36:  56 */     return this.order;
/*   37:     */   }
/*   38:     */   
/*   39:     */   public ByteBuf order(ByteOrder endianness)
/*   40:     */   {
/*   41:  61 */     if (endianness == null) {
/*   42:  62 */       throw new NullPointerException("endianness");
/*   43:     */     }
/*   44:  64 */     if (endianness == this.order) {
/*   45:  65 */       return this;
/*   46:     */     }
/*   47:  67 */     return this.buf;
/*   48:     */   }
/*   49:     */   
/*   50:     */   public ByteBuf unwrap()
/*   51:     */   {
/*   52:  72 */     return this.buf;
/*   53:     */   }
/*   54:     */   
/*   55:     */   public ByteBufAllocator alloc()
/*   56:     */   {
/*   57:  77 */     return this.buf.alloc();
/*   58:     */   }
/*   59:     */   
/*   60:     */   public int capacity()
/*   61:     */   {
/*   62:  82 */     return this.buf.capacity();
/*   63:     */   }
/*   64:     */   
/*   65:     */   public ByteBuf capacity(int newCapacity)
/*   66:     */   {
/*   67:  87 */     this.buf.capacity(newCapacity);
/*   68:  88 */     return this;
/*   69:     */   }
/*   70:     */   
/*   71:     */   public int maxCapacity()
/*   72:     */   {
/*   73:  93 */     return this.buf.maxCapacity();
/*   74:     */   }
/*   75:     */   
/*   76:     */   public boolean isReadOnly()
/*   77:     */   {
/*   78:  98 */     return this.buf.isReadOnly();
/*   79:     */   }
/*   80:     */   
/*   81:     */   public ByteBuf asReadOnly()
/*   82:     */   {
/*   83: 103 */     return Unpooled.unmodifiableBuffer(this);
/*   84:     */   }
/*   85:     */   
/*   86:     */   public boolean isDirect()
/*   87:     */   {
/*   88: 108 */     return this.buf.isDirect();
/*   89:     */   }
/*   90:     */   
/*   91:     */   public int readerIndex()
/*   92:     */   {
/*   93: 113 */     return this.buf.readerIndex();
/*   94:     */   }
/*   95:     */   
/*   96:     */   public ByteBuf readerIndex(int readerIndex)
/*   97:     */   {
/*   98: 118 */     this.buf.readerIndex(readerIndex);
/*   99: 119 */     return this;
/*  100:     */   }
/*  101:     */   
/*  102:     */   public int writerIndex()
/*  103:     */   {
/*  104: 124 */     return this.buf.writerIndex();
/*  105:     */   }
/*  106:     */   
/*  107:     */   public ByteBuf writerIndex(int writerIndex)
/*  108:     */   {
/*  109: 129 */     this.buf.writerIndex(writerIndex);
/*  110: 130 */     return this;
/*  111:     */   }
/*  112:     */   
/*  113:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*  114:     */   {
/*  115: 135 */     this.buf.setIndex(readerIndex, writerIndex);
/*  116: 136 */     return this;
/*  117:     */   }
/*  118:     */   
/*  119:     */   public int readableBytes()
/*  120:     */   {
/*  121: 141 */     return this.buf.readableBytes();
/*  122:     */   }
/*  123:     */   
/*  124:     */   public int writableBytes()
/*  125:     */   {
/*  126: 146 */     return this.buf.writableBytes();
/*  127:     */   }
/*  128:     */   
/*  129:     */   public int maxWritableBytes()
/*  130:     */   {
/*  131: 151 */     return this.buf.maxWritableBytes();
/*  132:     */   }
/*  133:     */   
/*  134:     */   public boolean isReadable()
/*  135:     */   {
/*  136: 156 */     return this.buf.isReadable();
/*  137:     */   }
/*  138:     */   
/*  139:     */   public boolean isReadable(int size)
/*  140:     */   {
/*  141: 161 */     return this.buf.isReadable(size);
/*  142:     */   }
/*  143:     */   
/*  144:     */   public boolean isWritable()
/*  145:     */   {
/*  146: 166 */     return this.buf.isWritable();
/*  147:     */   }
/*  148:     */   
/*  149:     */   public boolean isWritable(int size)
/*  150:     */   {
/*  151: 171 */     return this.buf.isWritable(size);
/*  152:     */   }
/*  153:     */   
/*  154:     */   public ByteBuf clear()
/*  155:     */   {
/*  156: 176 */     this.buf.clear();
/*  157: 177 */     return this;
/*  158:     */   }
/*  159:     */   
/*  160:     */   public ByteBuf markReaderIndex()
/*  161:     */   {
/*  162: 182 */     this.buf.markReaderIndex();
/*  163: 183 */     return this;
/*  164:     */   }
/*  165:     */   
/*  166:     */   public ByteBuf resetReaderIndex()
/*  167:     */   {
/*  168: 188 */     this.buf.resetReaderIndex();
/*  169: 189 */     return this;
/*  170:     */   }
/*  171:     */   
/*  172:     */   public ByteBuf markWriterIndex()
/*  173:     */   {
/*  174: 194 */     this.buf.markWriterIndex();
/*  175: 195 */     return this;
/*  176:     */   }
/*  177:     */   
/*  178:     */   public ByteBuf resetWriterIndex()
/*  179:     */   {
/*  180: 200 */     this.buf.resetWriterIndex();
/*  181: 201 */     return this;
/*  182:     */   }
/*  183:     */   
/*  184:     */   public ByteBuf discardReadBytes()
/*  185:     */   {
/*  186: 206 */     this.buf.discardReadBytes();
/*  187: 207 */     return this;
/*  188:     */   }
/*  189:     */   
/*  190:     */   public ByteBuf discardSomeReadBytes()
/*  191:     */   {
/*  192: 212 */     this.buf.discardSomeReadBytes();
/*  193: 213 */     return this;
/*  194:     */   }
/*  195:     */   
/*  196:     */   public ByteBuf ensureWritable(int writableBytes)
/*  197:     */   {
/*  198: 218 */     this.buf.ensureWritable(writableBytes);
/*  199: 219 */     return this;
/*  200:     */   }
/*  201:     */   
/*  202:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  203:     */   {
/*  204: 224 */     return this.buf.ensureWritable(minWritableBytes, force);
/*  205:     */   }
/*  206:     */   
/*  207:     */   public boolean getBoolean(int index)
/*  208:     */   {
/*  209: 229 */     return this.buf.getBoolean(index);
/*  210:     */   }
/*  211:     */   
/*  212:     */   public byte getByte(int index)
/*  213:     */   {
/*  214: 234 */     return this.buf.getByte(index);
/*  215:     */   }
/*  216:     */   
/*  217:     */   public short getUnsignedByte(int index)
/*  218:     */   {
/*  219: 239 */     return this.buf.getUnsignedByte(index);
/*  220:     */   }
/*  221:     */   
/*  222:     */   public short getShort(int index)
/*  223:     */   {
/*  224: 244 */     return ByteBufUtil.swapShort(this.buf.getShort(index));
/*  225:     */   }
/*  226:     */   
/*  227:     */   public short getShortLE(int index)
/*  228:     */   {
/*  229: 249 */     return this.buf.getShort(index);
/*  230:     */   }
/*  231:     */   
/*  232:     */   public int getUnsignedShort(int index)
/*  233:     */   {
/*  234: 254 */     return getShort(index) & 0xFFFF;
/*  235:     */   }
/*  236:     */   
/*  237:     */   public int getUnsignedShortLE(int index)
/*  238:     */   {
/*  239: 259 */     return getShortLE(index) & 0xFFFF;
/*  240:     */   }
/*  241:     */   
/*  242:     */   public int getMedium(int index)
/*  243:     */   {
/*  244: 264 */     return ByteBufUtil.swapMedium(this.buf.getMedium(index));
/*  245:     */   }
/*  246:     */   
/*  247:     */   public int getMediumLE(int index)
/*  248:     */   {
/*  249: 269 */     return this.buf.getMedium(index);
/*  250:     */   }
/*  251:     */   
/*  252:     */   public int getUnsignedMedium(int index)
/*  253:     */   {
/*  254: 274 */     return getMedium(index) & 0xFFFFFF;
/*  255:     */   }
/*  256:     */   
/*  257:     */   public int getUnsignedMediumLE(int index)
/*  258:     */   {
/*  259: 279 */     return getMediumLE(index) & 0xFFFFFF;
/*  260:     */   }
/*  261:     */   
/*  262:     */   public int getInt(int index)
/*  263:     */   {
/*  264: 284 */     return ByteBufUtil.swapInt(this.buf.getInt(index));
/*  265:     */   }
/*  266:     */   
/*  267:     */   public int getIntLE(int index)
/*  268:     */   {
/*  269: 289 */     return this.buf.getInt(index);
/*  270:     */   }
/*  271:     */   
/*  272:     */   public long getUnsignedInt(int index)
/*  273:     */   {
/*  274: 294 */     return getInt(index) & 0xFFFFFFFF;
/*  275:     */   }
/*  276:     */   
/*  277:     */   public long getUnsignedIntLE(int index)
/*  278:     */   {
/*  279: 299 */     return getIntLE(index) & 0xFFFFFFFF;
/*  280:     */   }
/*  281:     */   
/*  282:     */   public long getLong(int index)
/*  283:     */   {
/*  284: 304 */     return ByteBufUtil.swapLong(this.buf.getLong(index));
/*  285:     */   }
/*  286:     */   
/*  287:     */   public long getLongLE(int index)
/*  288:     */   {
/*  289: 309 */     return this.buf.getLong(index);
/*  290:     */   }
/*  291:     */   
/*  292:     */   public char getChar(int index)
/*  293:     */   {
/*  294: 314 */     return (char)getShort(index);
/*  295:     */   }
/*  296:     */   
/*  297:     */   public float getFloat(int index)
/*  298:     */   {
/*  299: 319 */     return Float.intBitsToFloat(getInt(index));
/*  300:     */   }
/*  301:     */   
/*  302:     */   public double getDouble(int index)
/*  303:     */   {
/*  304: 324 */     return Double.longBitsToDouble(getLong(index));
/*  305:     */   }
/*  306:     */   
/*  307:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  308:     */   {
/*  309: 329 */     this.buf.getBytes(index, dst);
/*  310: 330 */     return this;
/*  311:     */   }
/*  312:     */   
/*  313:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  314:     */   {
/*  315: 335 */     this.buf.getBytes(index, dst, length);
/*  316: 336 */     return this;
/*  317:     */   }
/*  318:     */   
/*  319:     */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  320:     */   {
/*  321: 341 */     this.buf.getBytes(index, dst, dstIndex, length);
/*  322: 342 */     return this;
/*  323:     */   }
/*  324:     */   
/*  325:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  326:     */   {
/*  327: 347 */     this.buf.getBytes(index, dst);
/*  328: 348 */     return this;
/*  329:     */   }
/*  330:     */   
/*  331:     */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  332:     */   {
/*  333: 353 */     this.buf.getBytes(index, dst, dstIndex, length);
/*  334: 354 */     return this;
/*  335:     */   }
/*  336:     */   
/*  337:     */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  338:     */   {
/*  339: 359 */     this.buf.getBytes(index, dst);
/*  340: 360 */     return this;
/*  341:     */   }
/*  342:     */   
/*  343:     */   public ByteBuf getBytes(int index, OutputStream out, int length)
/*  344:     */     throws IOException
/*  345:     */   {
/*  346: 365 */     this.buf.getBytes(index, out, length);
/*  347: 366 */     return this;
/*  348:     */   }
/*  349:     */   
/*  350:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  351:     */     throws IOException
/*  352:     */   {
/*  353: 371 */     return this.buf.getBytes(index, out, length);
/*  354:     */   }
/*  355:     */   
/*  356:     */   public int getBytes(int index, FileChannel out, long position, int length)
/*  357:     */     throws IOException
/*  358:     */   {
/*  359: 376 */     return this.buf.getBytes(index, out, position, length);
/*  360:     */   }
/*  361:     */   
/*  362:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/*  363:     */   {
/*  364: 381 */     return this.buf.getCharSequence(index, length, charset);
/*  365:     */   }
/*  366:     */   
/*  367:     */   public ByteBuf setBoolean(int index, boolean value)
/*  368:     */   {
/*  369: 386 */     this.buf.setBoolean(index, value);
/*  370: 387 */     return this;
/*  371:     */   }
/*  372:     */   
/*  373:     */   public ByteBuf setByte(int index, int value)
/*  374:     */   {
/*  375: 392 */     this.buf.setByte(index, value);
/*  376: 393 */     return this;
/*  377:     */   }
/*  378:     */   
/*  379:     */   public ByteBuf setShort(int index, int value)
/*  380:     */   {
/*  381: 398 */     this.buf.setShort(index, ByteBufUtil.swapShort((short)value));
/*  382: 399 */     return this;
/*  383:     */   }
/*  384:     */   
/*  385:     */   public ByteBuf setShortLE(int index, int value)
/*  386:     */   {
/*  387: 404 */     this.buf.setShort(index, (short)value);
/*  388: 405 */     return this;
/*  389:     */   }
/*  390:     */   
/*  391:     */   public ByteBuf setMedium(int index, int value)
/*  392:     */   {
/*  393: 410 */     this.buf.setMedium(index, ByteBufUtil.swapMedium(value));
/*  394: 411 */     return this;
/*  395:     */   }
/*  396:     */   
/*  397:     */   public ByteBuf setMediumLE(int index, int value)
/*  398:     */   {
/*  399: 416 */     this.buf.setMedium(index, value);
/*  400: 417 */     return this;
/*  401:     */   }
/*  402:     */   
/*  403:     */   public ByteBuf setInt(int index, int value)
/*  404:     */   {
/*  405: 422 */     this.buf.setInt(index, ByteBufUtil.swapInt(value));
/*  406: 423 */     return this;
/*  407:     */   }
/*  408:     */   
/*  409:     */   public ByteBuf setIntLE(int index, int value)
/*  410:     */   {
/*  411: 428 */     this.buf.setInt(index, value);
/*  412: 429 */     return this;
/*  413:     */   }
/*  414:     */   
/*  415:     */   public ByteBuf setLong(int index, long value)
/*  416:     */   {
/*  417: 434 */     this.buf.setLong(index, ByteBufUtil.swapLong(value));
/*  418: 435 */     return this;
/*  419:     */   }
/*  420:     */   
/*  421:     */   public ByteBuf setLongLE(int index, long value)
/*  422:     */   {
/*  423: 440 */     this.buf.setLong(index, value);
/*  424: 441 */     return this;
/*  425:     */   }
/*  426:     */   
/*  427:     */   public ByteBuf setChar(int index, int value)
/*  428:     */   {
/*  429: 446 */     setShort(index, value);
/*  430: 447 */     return this;
/*  431:     */   }
/*  432:     */   
/*  433:     */   public ByteBuf setFloat(int index, float value)
/*  434:     */   {
/*  435: 452 */     setInt(index, Float.floatToRawIntBits(value));
/*  436: 453 */     return this;
/*  437:     */   }
/*  438:     */   
/*  439:     */   public ByteBuf setDouble(int index, double value)
/*  440:     */   {
/*  441: 458 */     setLong(index, Double.doubleToRawLongBits(value));
/*  442: 459 */     return this;
/*  443:     */   }
/*  444:     */   
/*  445:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  446:     */   {
/*  447: 464 */     this.buf.setBytes(index, src);
/*  448: 465 */     return this;
/*  449:     */   }
/*  450:     */   
/*  451:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  452:     */   {
/*  453: 470 */     this.buf.setBytes(index, src, length);
/*  454: 471 */     return this;
/*  455:     */   }
/*  456:     */   
/*  457:     */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  458:     */   {
/*  459: 476 */     this.buf.setBytes(index, src, srcIndex, length);
/*  460: 477 */     return this;
/*  461:     */   }
/*  462:     */   
/*  463:     */   public ByteBuf setBytes(int index, byte[] src)
/*  464:     */   {
/*  465: 482 */     this.buf.setBytes(index, src);
/*  466: 483 */     return this;
/*  467:     */   }
/*  468:     */   
/*  469:     */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  470:     */   {
/*  471: 488 */     this.buf.setBytes(index, src, srcIndex, length);
/*  472: 489 */     return this;
/*  473:     */   }
/*  474:     */   
/*  475:     */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  476:     */   {
/*  477: 494 */     this.buf.setBytes(index, src);
/*  478: 495 */     return this;
/*  479:     */   }
/*  480:     */   
/*  481:     */   public int setBytes(int index, InputStream in, int length)
/*  482:     */     throws IOException
/*  483:     */   {
/*  484: 500 */     return this.buf.setBytes(index, in, length);
/*  485:     */   }
/*  486:     */   
/*  487:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  488:     */     throws IOException
/*  489:     */   {
/*  490: 505 */     return this.buf.setBytes(index, in, length);
/*  491:     */   }
/*  492:     */   
/*  493:     */   public int setBytes(int index, FileChannel in, long position, int length)
/*  494:     */     throws IOException
/*  495:     */   {
/*  496: 510 */     return this.buf.setBytes(index, in, position, length);
/*  497:     */   }
/*  498:     */   
/*  499:     */   public ByteBuf setZero(int index, int length)
/*  500:     */   {
/*  501: 515 */     this.buf.setZero(index, length);
/*  502: 516 */     return this;
/*  503:     */   }
/*  504:     */   
/*  505:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/*  506:     */   {
/*  507: 521 */     return this.buf.setCharSequence(index, sequence, charset);
/*  508:     */   }
/*  509:     */   
/*  510:     */   public boolean readBoolean()
/*  511:     */   {
/*  512: 526 */     return this.buf.readBoolean();
/*  513:     */   }
/*  514:     */   
/*  515:     */   public byte readByte()
/*  516:     */   {
/*  517: 531 */     return this.buf.readByte();
/*  518:     */   }
/*  519:     */   
/*  520:     */   public short readUnsignedByte()
/*  521:     */   {
/*  522: 536 */     return this.buf.readUnsignedByte();
/*  523:     */   }
/*  524:     */   
/*  525:     */   public short readShort()
/*  526:     */   {
/*  527: 541 */     return ByteBufUtil.swapShort(this.buf.readShort());
/*  528:     */   }
/*  529:     */   
/*  530:     */   public short readShortLE()
/*  531:     */   {
/*  532: 546 */     return this.buf.readShort();
/*  533:     */   }
/*  534:     */   
/*  535:     */   public int readUnsignedShort()
/*  536:     */   {
/*  537: 551 */     return readShort() & 0xFFFF;
/*  538:     */   }
/*  539:     */   
/*  540:     */   public int readUnsignedShortLE()
/*  541:     */   {
/*  542: 556 */     return readShortLE() & 0xFFFF;
/*  543:     */   }
/*  544:     */   
/*  545:     */   public int readMedium()
/*  546:     */   {
/*  547: 561 */     return ByteBufUtil.swapMedium(this.buf.readMedium());
/*  548:     */   }
/*  549:     */   
/*  550:     */   public int readMediumLE()
/*  551:     */   {
/*  552: 566 */     return this.buf.readMedium();
/*  553:     */   }
/*  554:     */   
/*  555:     */   public int readUnsignedMedium()
/*  556:     */   {
/*  557: 571 */     return readMedium() & 0xFFFFFF;
/*  558:     */   }
/*  559:     */   
/*  560:     */   public int readUnsignedMediumLE()
/*  561:     */   {
/*  562: 576 */     return readMediumLE() & 0xFFFFFF;
/*  563:     */   }
/*  564:     */   
/*  565:     */   public int readInt()
/*  566:     */   {
/*  567: 581 */     return ByteBufUtil.swapInt(this.buf.readInt());
/*  568:     */   }
/*  569:     */   
/*  570:     */   public int readIntLE()
/*  571:     */   {
/*  572: 586 */     return this.buf.readInt();
/*  573:     */   }
/*  574:     */   
/*  575:     */   public long readUnsignedInt()
/*  576:     */   {
/*  577: 591 */     return readInt() & 0xFFFFFFFF;
/*  578:     */   }
/*  579:     */   
/*  580:     */   public long readUnsignedIntLE()
/*  581:     */   {
/*  582: 596 */     return readIntLE() & 0xFFFFFFFF;
/*  583:     */   }
/*  584:     */   
/*  585:     */   public long readLong()
/*  586:     */   {
/*  587: 601 */     return ByteBufUtil.swapLong(this.buf.readLong());
/*  588:     */   }
/*  589:     */   
/*  590:     */   public long readLongLE()
/*  591:     */   {
/*  592: 606 */     return this.buf.readLong();
/*  593:     */   }
/*  594:     */   
/*  595:     */   public char readChar()
/*  596:     */   {
/*  597: 611 */     return (char)readShort();
/*  598:     */   }
/*  599:     */   
/*  600:     */   public float readFloat()
/*  601:     */   {
/*  602: 616 */     return Float.intBitsToFloat(readInt());
/*  603:     */   }
/*  604:     */   
/*  605:     */   public double readDouble()
/*  606:     */   {
/*  607: 621 */     return Double.longBitsToDouble(readLong());
/*  608:     */   }
/*  609:     */   
/*  610:     */   public ByteBuf readBytes(int length)
/*  611:     */   {
/*  612: 626 */     return this.buf.readBytes(length).order(order());
/*  613:     */   }
/*  614:     */   
/*  615:     */   public ByteBuf readSlice(int length)
/*  616:     */   {
/*  617: 631 */     return this.buf.readSlice(length).order(this.order);
/*  618:     */   }
/*  619:     */   
/*  620:     */   public ByteBuf readRetainedSlice(int length)
/*  621:     */   {
/*  622: 636 */     return this.buf.readRetainedSlice(length).order(this.order);
/*  623:     */   }
/*  624:     */   
/*  625:     */   public ByteBuf readBytes(ByteBuf dst)
/*  626:     */   {
/*  627: 641 */     this.buf.readBytes(dst);
/*  628: 642 */     return this;
/*  629:     */   }
/*  630:     */   
/*  631:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  632:     */   {
/*  633: 647 */     this.buf.readBytes(dst, length);
/*  634: 648 */     return this;
/*  635:     */   }
/*  636:     */   
/*  637:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  638:     */   {
/*  639: 653 */     this.buf.readBytes(dst, dstIndex, length);
/*  640: 654 */     return this;
/*  641:     */   }
/*  642:     */   
/*  643:     */   public ByteBuf readBytes(byte[] dst)
/*  644:     */   {
/*  645: 659 */     this.buf.readBytes(dst);
/*  646: 660 */     return this;
/*  647:     */   }
/*  648:     */   
/*  649:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  650:     */   {
/*  651: 665 */     this.buf.readBytes(dst, dstIndex, length);
/*  652: 666 */     return this;
/*  653:     */   }
/*  654:     */   
/*  655:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  656:     */   {
/*  657: 671 */     this.buf.readBytes(dst);
/*  658: 672 */     return this;
/*  659:     */   }
/*  660:     */   
/*  661:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  662:     */     throws IOException
/*  663:     */   {
/*  664: 677 */     this.buf.readBytes(out, length);
/*  665: 678 */     return this;
/*  666:     */   }
/*  667:     */   
/*  668:     */   public int readBytes(GatheringByteChannel out, int length)
/*  669:     */     throws IOException
/*  670:     */   {
/*  671: 683 */     return this.buf.readBytes(out, length);
/*  672:     */   }
/*  673:     */   
/*  674:     */   public int readBytes(FileChannel out, long position, int length)
/*  675:     */     throws IOException
/*  676:     */   {
/*  677: 688 */     return this.buf.readBytes(out, position, length);
/*  678:     */   }
/*  679:     */   
/*  680:     */   public CharSequence readCharSequence(int length, Charset charset)
/*  681:     */   {
/*  682: 693 */     return this.buf.readCharSequence(length, charset);
/*  683:     */   }
/*  684:     */   
/*  685:     */   public ByteBuf skipBytes(int length)
/*  686:     */   {
/*  687: 698 */     this.buf.skipBytes(length);
/*  688: 699 */     return this;
/*  689:     */   }
/*  690:     */   
/*  691:     */   public ByteBuf writeBoolean(boolean value)
/*  692:     */   {
/*  693: 704 */     this.buf.writeBoolean(value);
/*  694: 705 */     return this;
/*  695:     */   }
/*  696:     */   
/*  697:     */   public ByteBuf writeByte(int value)
/*  698:     */   {
/*  699: 710 */     this.buf.writeByte(value);
/*  700: 711 */     return this;
/*  701:     */   }
/*  702:     */   
/*  703:     */   public ByteBuf writeShort(int value)
/*  704:     */   {
/*  705: 716 */     this.buf.writeShort(ByteBufUtil.swapShort((short)value));
/*  706: 717 */     return this;
/*  707:     */   }
/*  708:     */   
/*  709:     */   public ByteBuf writeShortLE(int value)
/*  710:     */   {
/*  711: 722 */     this.buf.writeShort((short)value);
/*  712: 723 */     return this;
/*  713:     */   }
/*  714:     */   
/*  715:     */   public ByteBuf writeMedium(int value)
/*  716:     */   {
/*  717: 728 */     this.buf.writeMedium(ByteBufUtil.swapMedium(value));
/*  718: 729 */     return this;
/*  719:     */   }
/*  720:     */   
/*  721:     */   public ByteBuf writeMediumLE(int value)
/*  722:     */   {
/*  723: 734 */     this.buf.writeMedium(value);
/*  724: 735 */     return this;
/*  725:     */   }
/*  726:     */   
/*  727:     */   public ByteBuf writeInt(int value)
/*  728:     */   {
/*  729: 740 */     this.buf.writeInt(ByteBufUtil.swapInt(value));
/*  730: 741 */     return this;
/*  731:     */   }
/*  732:     */   
/*  733:     */   public ByteBuf writeIntLE(int value)
/*  734:     */   {
/*  735: 746 */     this.buf.writeInt(value);
/*  736: 747 */     return this;
/*  737:     */   }
/*  738:     */   
/*  739:     */   public ByteBuf writeLong(long value)
/*  740:     */   {
/*  741: 752 */     this.buf.writeLong(ByteBufUtil.swapLong(value));
/*  742: 753 */     return this;
/*  743:     */   }
/*  744:     */   
/*  745:     */   public ByteBuf writeLongLE(long value)
/*  746:     */   {
/*  747: 758 */     this.buf.writeLong(value);
/*  748: 759 */     return this;
/*  749:     */   }
/*  750:     */   
/*  751:     */   public ByteBuf writeChar(int value)
/*  752:     */   {
/*  753: 764 */     writeShort(value);
/*  754: 765 */     return this;
/*  755:     */   }
/*  756:     */   
/*  757:     */   public ByteBuf writeFloat(float value)
/*  758:     */   {
/*  759: 770 */     writeInt(Float.floatToRawIntBits(value));
/*  760: 771 */     return this;
/*  761:     */   }
/*  762:     */   
/*  763:     */   public ByteBuf writeDouble(double value)
/*  764:     */   {
/*  765: 776 */     writeLong(Double.doubleToRawLongBits(value));
/*  766: 777 */     return this;
/*  767:     */   }
/*  768:     */   
/*  769:     */   public ByteBuf writeBytes(ByteBuf src)
/*  770:     */   {
/*  771: 782 */     this.buf.writeBytes(src);
/*  772: 783 */     return this;
/*  773:     */   }
/*  774:     */   
/*  775:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/*  776:     */   {
/*  777: 788 */     this.buf.writeBytes(src, length);
/*  778: 789 */     return this;
/*  779:     */   }
/*  780:     */   
/*  781:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  782:     */   {
/*  783: 794 */     this.buf.writeBytes(src, srcIndex, length);
/*  784: 795 */     return this;
/*  785:     */   }
/*  786:     */   
/*  787:     */   public ByteBuf writeBytes(byte[] src)
/*  788:     */   {
/*  789: 800 */     this.buf.writeBytes(src);
/*  790: 801 */     return this;
/*  791:     */   }
/*  792:     */   
/*  793:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  794:     */   {
/*  795: 806 */     this.buf.writeBytes(src, srcIndex, length);
/*  796: 807 */     return this;
/*  797:     */   }
/*  798:     */   
/*  799:     */   public ByteBuf writeBytes(ByteBuffer src)
/*  800:     */   {
/*  801: 812 */     this.buf.writeBytes(src);
/*  802: 813 */     return this;
/*  803:     */   }
/*  804:     */   
/*  805:     */   public int writeBytes(InputStream in, int length)
/*  806:     */     throws IOException
/*  807:     */   {
/*  808: 818 */     return this.buf.writeBytes(in, length);
/*  809:     */   }
/*  810:     */   
/*  811:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  812:     */     throws IOException
/*  813:     */   {
/*  814: 823 */     return this.buf.writeBytes(in, length);
/*  815:     */   }
/*  816:     */   
/*  817:     */   public int writeBytes(FileChannel in, long position, int length)
/*  818:     */     throws IOException
/*  819:     */   {
/*  820: 828 */     return this.buf.writeBytes(in, position, length);
/*  821:     */   }
/*  822:     */   
/*  823:     */   public ByteBuf writeZero(int length)
/*  824:     */   {
/*  825: 833 */     this.buf.writeZero(length);
/*  826: 834 */     return this;
/*  827:     */   }
/*  828:     */   
/*  829:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/*  830:     */   {
/*  831: 839 */     return this.buf.writeCharSequence(sequence, charset);
/*  832:     */   }
/*  833:     */   
/*  834:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  835:     */   {
/*  836: 844 */     return this.buf.indexOf(fromIndex, toIndex, value);
/*  837:     */   }
/*  838:     */   
/*  839:     */   public int bytesBefore(byte value)
/*  840:     */   {
/*  841: 849 */     return this.buf.bytesBefore(value);
/*  842:     */   }
/*  843:     */   
/*  844:     */   public int bytesBefore(int length, byte value)
/*  845:     */   {
/*  846: 854 */     return this.buf.bytesBefore(length, value);
/*  847:     */   }
/*  848:     */   
/*  849:     */   public int bytesBefore(int index, int length, byte value)
/*  850:     */   {
/*  851: 859 */     return this.buf.bytesBefore(index, length, value);
/*  852:     */   }
/*  853:     */   
/*  854:     */   public int forEachByte(ByteProcessor processor)
/*  855:     */   {
/*  856: 864 */     return this.buf.forEachByte(processor);
/*  857:     */   }
/*  858:     */   
/*  859:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/*  860:     */   {
/*  861: 869 */     return this.buf.forEachByte(index, length, processor);
/*  862:     */   }
/*  863:     */   
/*  864:     */   public int forEachByteDesc(ByteProcessor processor)
/*  865:     */   {
/*  866: 874 */     return this.buf.forEachByteDesc(processor);
/*  867:     */   }
/*  868:     */   
/*  869:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/*  870:     */   {
/*  871: 879 */     return this.buf.forEachByteDesc(index, length, processor);
/*  872:     */   }
/*  873:     */   
/*  874:     */   public ByteBuf copy()
/*  875:     */   {
/*  876: 884 */     return this.buf.copy().order(this.order);
/*  877:     */   }
/*  878:     */   
/*  879:     */   public ByteBuf copy(int index, int length)
/*  880:     */   {
/*  881: 889 */     return this.buf.copy(index, length).order(this.order);
/*  882:     */   }
/*  883:     */   
/*  884:     */   public ByteBuf slice()
/*  885:     */   {
/*  886: 894 */     return this.buf.slice().order(this.order);
/*  887:     */   }
/*  888:     */   
/*  889:     */   public ByteBuf retainedSlice()
/*  890:     */   {
/*  891: 899 */     return this.buf.retainedSlice().order(this.order);
/*  892:     */   }
/*  893:     */   
/*  894:     */   public ByteBuf slice(int index, int length)
/*  895:     */   {
/*  896: 904 */     return this.buf.slice(index, length).order(this.order);
/*  897:     */   }
/*  898:     */   
/*  899:     */   public ByteBuf retainedSlice(int index, int length)
/*  900:     */   {
/*  901: 909 */     return this.buf.retainedSlice(index, length).order(this.order);
/*  902:     */   }
/*  903:     */   
/*  904:     */   public ByteBuf duplicate()
/*  905:     */   {
/*  906: 914 */     return this.buf.duplicate().order(this.order);
/*  907:     */   }
/*  908:     */   
/*  909:     */   public ByteBuf retainedDuplicate()
/*  910:     */   {
/*  911: 919 */     return this.buf.retainedDuplicate().order(this.order);
/*  912:     */   }
/*  913:     */   
/*  914:     */   public int nioBufferCount()
/*  915:     */   {
/*  916: 924 */     return this.buf.nioBufferCount();
/*  917:     */   }
/*  918:     */   
/*  919:     */   public ByteBuffer nioBuffer()
/*  920:     */   {
/*  921: 929 */     return this.buf.nioBuffer().order(this.order);
/*  922:     */   }
/*  923:     */   
/*  924:     */   public ByteBuffer nioBuffer(int index, int length)
/*  925:     */   {
/*  926: 934 */     return this.buf.nioBuffer(index, length).order(this.order);
/*  927:     */   }
/*  928:     */   
/*  929:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  930:     */   {
/*  931: 939 */     return nioBuffer(index, length);
/*  932:     */   }
/*  933:     */   
/*  934:     */   public ByteBuffer[] nioBuffers()
/*  935:     */   {
/*  936: 944 */     ByteBuffer[] nioBuffers = this.buf.nioBuffers();
/*  937: 945 */     for (int i = 0; i < nioBuffers.length; i++) {
/*  938: 946 */       nioBuffers[i] = nioBuffers[i].order(this.order);
/*  939:     */     }
/*  940: 948 */     return nioBuffers;
/*  941:     */   }
/*  942:     */   
/*  943:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  944:     */   {
/*  945: 953 */     ByteBuffer[] nioBuffers = this.buf.nioBuffers(index, length);
/*  946: 954 */     for (int i = 0; i < nioBuffers.length; i++) {
/*  947: 955 */       nioBuffers[i] = nioBuffers[i].order(this.order);
/*  948:     */     }
/*  949: 957 */     return nioBuffers;
/*  950:     */   }
/*  951:     */   
/*  952:     */   public boolean hasArray()
/*  953:     */   {
/*  954: 962 */     return this.buf.hasArray();
/*  955:     */   }
/*  956:     */   
/*  957:     */   public byte[] array()
/*  958:     */   {
/*  959: 967 */     return this.buf.array();
/*  960:     */   }
/*  961:     */   
/*  962:     */   public int arrayOffset()
/*  963:     */   {
/*  964: 972 */     return this.buf.arrayOffset();
/*  965:     */   }
/*  966:     */   
/*  967:     */   public boolean hasMemoryAddress()
/*  968:     */   {
/*  969: 977 */     return this.buf.hasMemoryAddress();
/*  970:     */   }
/*  971:     */   
/*  972:     */   public long memoryAddress()
/*  973:     */   {
/*  974: 982 */     return this.buf.memoryAddress();
/*  975:     */   }
/*  976:     */   
/*  977:     */   public String toString(Charset charset)
/*  978:     */   {
/*  979: 987 */     return this.buf.toString(charset);
/*  980:     */   }
/*  981:     */   
/*  982:     */   public String toString(int index, int length, Charset charset)
/*  983:     */   {
/*  984: 992 */     return this.buf.toString(index, length, charset);
/*  985:     */   }
/*  986:     */   
/*  987:     */   public int refCnt()
/*  988:     */   {
/*  989: 997 */     return this.buf.refCnt();
/*  990:     */   }
/*  991:     */   
/*  992:     */   public ByteBuf retain()
/*  993:     */   {
/*  994:1002 */     this.buf.retain();
/*  995:1003 */     return this;
/*  996:     */   }
/*  997:     */   
/*  998:     */   public ByteBuf retain(int increment)
/*  999:     */   {
/* 1000:1008 */     this.buf.retain(increment);
/* 1001:1009 */     return this;
/* 1002:     */   }
/* 1003:     */   
/* 1004:     */   public ByteBuf touch()
/* 1005:     */   {
/* 1006:1014 */     this.buf.touch();
/* 1007:1015 */     return this;
/* 1008:     */   }
/* 1009:     */   
/* 1010:     */   public ByteBuf touch(Object hint)
/* 1011:     */   {
/* 1012:1020 */     this.buf.touch(hint);
/* 1013:1021 */     return this;
/* 1014:     */   }
/* 1015:     */   
/* 1016:     */   public boolean release()
/* 1017:     */   {
/* 1018:1026 */     return this.buf.release();
/* 1019:     */   }
/* 1020:     */   
/* 1021:     */   public boolean release(int decrement)
/* 1022:     */   {
/* 1023:1031 */     return this.buf.release(decrement);
/* 1024:     */   }
/* 1025:     */   
/* 1026:     */   public int hashCode()
/* 1027:     */   {
/* 1028:1036 */     return this.buf.hashCode();
/* 1029:     */   }
/* 1030:     */   
/* 1031:     */   public boolean equals(Object obj)
/* 1032:     */   {
/* 1033:1041 */     if (this == obj) {
/* 1034:1042 */       return true;
/* 1035:     */     }
/* 1036:1044 */     if ((obj instanceof ByteBuf)) {
/* 1037:1045 */       return ByteBufUtil.equals(this, (ByteBuf)obj);
/* 1038:     */     }
/* 1039:1047 */     return false;
/* 1040:     */   }
/* 1041:     */   
/* 1042:     */   public int compareTo(ByteBuf buffer)
/* 1043:     */   {
/* 1044:1052 */     return ByteBufUtil.compare(this, buffer);
/* 1045:     */   }
/* 1046:     */   
/* 1047:     */   public String toString()
/* 1048:     */   {
/* 1049:1057 */     return "Swapped(" + this.buf + ')';
/* 1050:     */   }
/* 1051:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.SwappedByteBuf
 * JD-Core Version:    0.7.0.1
 */