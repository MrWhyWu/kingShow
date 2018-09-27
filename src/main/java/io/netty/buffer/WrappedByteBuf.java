/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ByteProcessor;
/*    4:     */ import io.netty.util.internal.StringUtil;
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
/*   15:     */ class WrappedByteBuf
/*   16:     */   extends ByteBuf
/*   17:     */ {
/*   18:     */   protected final ByteBuf buf;
/*   19:     */   
/*   20:     */   protected WrappedByteBuf(ByteBuf buf)
/*   21:     */   {
/*   22:  44 */     if (buf == null) {
/*   23:  45 */       throw new NullPointerException("buf");
/*   24:     */     }
/*   25:  47 */     this.buf = buf;
/*   26:     */   }
/*   27:     */   
/*   28:     */   public final boolean hasMemoryAddress()
/*   29:     */   {
/*   30:  52 */     return this.buf.hasMemoryAddress();
/*   31:     */   }
/*   32:     */   
/*   33:     */   public final long memoryAddress()
/*   34:     */   {
/*   35:  57 */     return this.buf.memoryAddress();
/*   36:     */   }
/*   37:     */   
/*   38:     */   public final int capacity()
/*   39:     */   {
/*   40:  62 */     return this.buf.capacity();
/*   41:     */   }
/*   42:     */   
/*   43:     */   public ByteBuf capacity(int newCapacity)
/*   44:     */   {
/*   45:  67 */     this.buf.capacity(newCapacity);
/*   46:  68 */     return this;
/*   47:     */   }
/*   48:     */   
/*   49:     */   public final int maxCapacity()
/*   50:     */   {
/*   51:  73 */     return this.buf.maxCapacity();
/*   52:     */   }
/*   53:     */   
/*   54:     */   public final ByteBufAllocator alloc()
/*   55:     */   {
/*   56:  78 */     return this.buf.alloc();
/*   57:     */   }
/*   58:     */   
/*   59:     */   public final ByteOrder order()
/*   60:     */   {
/*   61:  83 */     return this.buf.order();
/*   62:     */   }
/*   63:     */   
/*   64:     */   public ByteBuf order(ByteOrder endianness)
/*   65:     */   {
/*   66:  88 */     return this.buf.order(endianness);
/*   67:     */   }
/*   68:     */   
/*   69:     */   public final ByteBuf unwrap()
/*   70:     */   {
/*   71:  93 */     return this.buf;
/*   72:     */   }
/*   73:     */   
/*   74:     */   public ByteBuf asReadOnly()
/*   75:     */   {
/*   76:  98 */     return this.buf.asReadOnly();
/*   77:     */   }
/*   78:     */   
/*   79:     */   public boolean isReadOnly()
/*   80:     */   {
/*   81: 103 */     return this.buf.isReadOnly();
/*   82:     */   }
/*   83:     */   
/*   84:     */   public final boolean isDirect()
/*   85:     */   {
/*   86: 108 */     return this.buf.isDirect();
/*   87:     */   }
/*   88:     */   
/*   89:     */   public final int readerIndex()
/*   90:     */   {
/*   91: 113 */     return this.buf.readerIndex();
/*   92:     */   }
/*   93:     */   
/*   94:     */   public final ByteBuf readerIndex(int readerIndex)
/*   95:     */   {
/*   96: 118 */     this.buf.readerIndex(readerIndex);
/*   97: 119 */     return this;
/*   98:     */   }
/*   99:     */   
/*  100:     */   public final int writerIndex()
/*  101:     */   {
/*  102: 124 */     return this.buf.writerIndex();
/*  103:     */   }
/*  104:     */   
/*  105:     */   public final ByteBuf writerIndex(int writerIndex)
/*  106:     */   {
/*  107: 129 */     this.buf.writerIndex(writerIndex);
/*  108: 130 */     return this;
/*  109:     */   }
/*  110:     */   
/*  111:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*  112:     */   {
/*  113: 135 */     this.buf.setIndex(readerIndex, writerIndex);
/*  114: 136 */     return this;
/*  115:     */   }
/*  116:     */   
/*  117:     */   public final int readableBytes()
/*  118:     */   {
/*  119: 141 */     return this.buf.readableBytes();
/*  120:     */   }
/*  121:     */   
/*  122:     */   public final int writableBytes()
/*  123:     */   {
/*  124: 146 */     return this.buf.writableBytes();
/*  125:     */   }
/*  126:     */   
/*  127:     */   public final int maxWritableBytes()
/*  128:     */   {
/*  129: 151 */     return this.buf.maxWritableBytes();
/*  130:     */   }
/*  131:     */   
/*  132:     */   public final boolean isReadable()
/*  133:     */   {
/*  134: 156 */     return this.buf.isReadable();
/*  135:     */   }
/*  136:     */   
/*  137:     */   public final boolean isWritable()
/*  138:     */   {
/*  139: 161 */     return this.buf.isWritable();
/*  140:     */   }
/*  141:     */   
/*  142:     */   public final ByteBuf clear()
/*  143:     */   {
/*  144: 166 */     this.buf.clear();
/*  145: 167 */     return this;
/*  146:     */   }
/*  147:     */   
/*  148:     */   public final ByteBuf markReaderIndex()
/*  149:     */   {
/*  150: 172 */     this.buf.markReaderIndex();
/*  151: 173 */     return this;
/*  152:     */   }
/*  153:     */   
/*  154:     */   public final ByteBuf resetReaderIndex()
/*  155:     */   {
/*  156: 178 */     this.buf.resetReaderIndex();
/*  157: 179 */     return this;
/*  158:     */   }
/*  159:     */   
/*  160:     */   public final ByteBuf markWriterIndex()
/*  161:     */   {
/*  162: 184 */     this.buf.markWriterIndex();
/*  163: 185 */     return this;
/*  164:     */   }
/*  165:     */   
/*  166:     */   public final ByteBuf resetWriterIndex()
/*  167:     */   {
/*  168: 190 */     this.buf.resetWriterIndex();
/*  169: 191 */     return this;
/*  170:     */   }
/*  171:     */   
/*  172:     */   public ByteBuf discardReadBytes()
/*  173:     */   {
/*  174: 196 */     this.buf.discardReadBytes();
/*  175: 197 */     return this;
/*  176:     */   }
/*  177:     */   
/*  178:     */   public ByteBuf discardSomeReadBytes()
/*  179:     */   {
/*  180: 202 */     this.buf.discardSomeReadBytes();
/*  181: 203 */     return this;
/*  182:     */   }
/*  183:     */   
/*  184:     */   public ByteBuf ensureWritable(int minWritableBytes)
/*  185:     */   {
/*  186: 208 */     this.buf.ensureWritable(minWritableBytes);
/*  187: 209 */     return this;
/*  188:     */   }
/*  189:     */   
/*  190:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  191:     */   {
/*  192: 214 */     return this.buf.ensureWritable(minWritableBytes, force);
/*  193:     */   }
/*  194:     */   
/*  195:     */   public boolean getBoolean(int index)
/*  196:     */   {
/*  197: 219 */     return this.buf.getBoolean(index);
/*  198:     */   }
/*  199:     */   
/*  200:     */   public byte getByte(int index)
/*  201:     */   {
/*  202: 224 */     return this.buf.getByte(index);
/*  203:     */   }
/*  204:     */   
/*  205:     */   public short getUnsignedByte(int index)
/*  206:     */   {
/*  207: 229 */     return this.buf.getUnsignedByte(index);
/*  208:     */   }
/*  209:     */   
/*  210:     */   public short getShort(int index)
/*  211:     */   {
/*  212: 234 */     return this.buf.getShort(index);
/*  213:     */   }
/*  214:     */   
/*  215:     */   public short getShortLE(int index)
/*  216:     */   {
/*  217: 239 */     return this.buf.getShortLE(index);
/*  218:     */   }
/*  219:     */   
/*  220:     */   public int getUnsignedShort(int index)
/*  221:     */   {
/*  222: 244 */     return this.buf.getUnsignedShort(index);
/*  223:     */   }
/*  224:     */   
/*  225:     */   public int getUnsignedShortLE(int index)
/*  226:     */   {
/*  227: 249 */     return this.buf.getUnsignedShortLE(index);
/*  228:     */   }
/*  229:     */   
/*  230:     */   public int getMedium(int index)
/*  231:     */   {
/*  232: 254 */     return this.buf.getMedium(index);
/*  233:     */   }
/*  234:     */   
/*  235:     */   public int getMediumLE(int index)
/*  236:     */   {
/*  237: 259 */     return this.buf.getMediumLE(index);
/*  238:     */   }
/*  239:     */   
/*  240:     */   public int getUnsignedMedium(int index)
/*  241:     */   {
/*  242: 264 */     return this.buf.getUnsignedMedium(index);
/*  243:     */   }
/*  244:     */   
/*  245:     */   public int getUnsignedMediumLE(int index)
/*  246:     */   {
/*  247: 269 */     return this.buf.getUnsignedMediumLE(index);
/*  248:     */   }
/*  249:     */   
/*  250:     */   public int getInt(int index)
/*  251:     */   {
/*  252: 274 */     return this.buf.getInt(index);
/*  253:     */   }
/*  254:     */   
/*  255:     */   public int getIntLE(int index)
/*  256:     */   {
/*  257: 279 */     return this.buf.getIntLE(index);
/*  258:     */   }
/*  259:     */   
/*  260:     */   public long getUnsignedInt(int index)
/*  261:     */   {
/*  262: 284 */     return this.buf.getUnsignedInt(index);
/*  263:     */   }
/*  264:     */   
/*  265:     */   public long getUnsignedIntLE(int index)
/*  266:     */   {
/*  267: 289 */     return this.buf.getUnsignedIntLE(index);
/*  268:     */   }
/*  269:     */   
/*  270:     */   public long getLong(int index)
/*  271:     */   {
/*  272: 294 */     return this.buf.getLong(index);
/*  273:     */   }
/*  274:     */   
/*  275:     */   public long getLongLE(int index)
/*  276:     */   {
/*  277: 299 */     return this.buf.getLongLE(index);
/*  278:     */   }
/*  279:     */   
/*  280:     */   public char getChar(int index)
/*  281:     */   {
/*  282: 304 */     return this.buf.getChar(index);
/*  283:     */   }
/*  284:     */   
/*  285:     */   public float getFloat(int index)
/*  286:     */   {
/*  287: 309 */     return this.buf.getFloat(index);
/*  288:     */   }
/*  289:     */   
/*  290:     */   public double getDouble(int index)
/*  291:     */   {
/*  292: 314 */     return this.buf.getDouble(index);
/*  293:     */   }
/*  294:     */   
/*  295:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  296:     */   {
/*  297: 319 */     this.buf.getBytes(index, dst);
/*  298: 320 */     return this;
/*  299:     */   }
/*  300:     */   
/*  301:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  302:     */   {
/*  303: 325 */     this.buf.getBytes(index, dst, length);
/*  304: 326 */     return this;
/*  305:     */   }
/*  306:     */   
/*  307:     */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  308:     */   {
/*  309: 331 */     this.buf.getBytes(index, dst, dstIndex, length);
/*  310: 332 */     return this;
/*  311:     */   }
/*  312:     */   
/*  313:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  314:     */   {
/*  315: 337 */     this.buf.getBytes(index, dst);
/*  316: 338 */     return this;
/*  317:     */   }
/*  318:     */   
/*  319:     */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  320:     */   {
/*  321: 343 */     this.buf.getBytes(index, dst, dstIndex, length);
/*  322: 344 */     return this;
/*  323:     */   }
/*  324:     */   
/*  325:     */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  326:     */   {
/*  327: 349 */     this.buf.getBytes(index, dst);
/*  328: 350 */     return this;
/*  329:     */   }
/*  330:     */   
/*  331:     */   public ByteBuf getBytes(int index, OutputStream out, int length)
/*  332:     */     throws IOException
/*  333:     */   {
/*  334: 355 */     this.buf.getBytes(index, out, length);
/*  335: 356 */     return this;
/*  336:     */   }
/*  337:     */   
/*  338:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  339:     */     throws IOException
/*  340:     */   {
/*  341: 361 */     return this.buf.getBytes(index, out, length);
/*  342:     */   }
/*  343:     */   
/*  344:     */   public int getBytes(int index, FileChannel out, long position, int length)
/*  345:     */     throws IOException
/*  346:     */   {
/*  347: 366 */     return this.buf.getBytes(index, out, position, length);
/*  348:     */   }
/*  349:     */   
/*  350:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/*  351:     */   {
/*  352: 371 */     return this.buf.getCharSequence(index, length, charset);
/*  353:     */   }
/*  354:     */   
/*  355:     */   public ByteBuf setBoolean(int index, boolean value)
/*  356:     */   {
/*  357: 376 */     this.buf.setBoolean(index, value);
/*  358: 377 */     return this;
/*  359:     */   }
/*  360:     */   
/*  361:     */   public ByteBuf setByte(int index, int value)
/*  362:     */   {
/*  363: 382 */     this.buf.setByte(index, value);
/*  364: 383 */     return this;
/*  365:     */   }
/*  366:     */   
/*  367:     */   public ByteBuf setShort(int index, int value)
/*  368:     */   {
/*  369: 388 */     this.buf.setShort(index, value);
/*  370: 389 */     return this;
/*  371:     */   }
/*  372:     */   
/*  373:     */   public ByteBuf setShortLE(int index, int value)
/*  374:     */   {
/*  375: 394 */     this.buf.setShortLE(index, value);
/*  376: 395 */     return this;
/*  377:     */   }
/*  378:     */   
/*  379:     */   public ByteBuf setMedium(int index, int value)
/*  380:     */   {
/*  381: 400 */     this.buf.setMedium(index, value);
/*  382: 401 */     return this;
/*  383:     */   }
/*  384:     */   
/*  385:     */   public ByteBuf setMediumLE(int index, int value)
/*  386:     */   {
/*  387: 406 */     this.buf.setMediumLE(index, value);
/*  388: 407 */     return this;
/*  389:     */   }
/*  390:     */   
/*  391:     */   public ByteBuf setInt(int index, int value)
/*  392:     */   {
/*  393: 412 */     this.buf.setInt(index, value);
/*  394: 413 */     return this;
/*  395:     */   }
/*  396:     */   
/*  397:     */   public ByteBuf setIntLE(int index, int value)
/*  398:     */   {
/*  399: 418 */     this.buf.setIntLE(index, value);
/*  400: 419 */     return this;
/*  401:     */   }
/*  402:     */   
/*  403:     */   public ByteBuf setLong(int index, long value)
/*  404:     */   {
/*  405: 424 */     this.buf.setLong(index, value);
/*  406: 425 */     return this;
/*  407:     */   }
/*  408:     */   
/*  409:     */   public ByteBuf setLongLE(int index, long value)
/*  410:     */   {
/*  411: 430 */     this.buf.setLongLE(index, value);
/*  412: 431 */     return this;
/*  413:     */   }
/*  414:     */   
/*  415:     */   public ByteBuf setChar(int index, int value)
/*  416:     */   {
/*  417: 436 */     this.buf.setChar(index, value);
/*  418: 437 */     return this;
/*  419:     */   }
/*  420:     */   
/*  421:     */   public ByteBuf setFloat(int index, float value)
/*  422:     */   {
/*  423: 442 */     this.buf.setFloat(index, value);
/*  424: 443 */     return this;
/*  425:     */   }
/*  426:     */   
/*  427:     */   public ByteBuf setDouble(int index, double value)
/*  428:     */   {
/*  429: 448 */     this.buf.setDouble(index, value);
/*  430: 449 */     return this;
/*  431:     */   }
/*  432:     */   
/*  433:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  434:     */   {
/*  435: 454 */     this.buf.setBytes(index, src);
/*  436: 455 */     return this;
/*  437:     */   }
/*  438:     */   
/*  439:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  440:     */   {
/*  441: 460 */     this.buf.setBytes(index, src, length);
/*  442: 461 */     return this;
/*  443:     */   }
/*  444:     */   
/*  445:     */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  446:     */   {
/*  447: 466 */     this.buf.setBytes(index, src, srcIndex, length);
/*  448: 467 */     return this;
/*  449:     */   }
/*  450:     */   
/*  451:     */   public ByteBuf setBytes(int index, byte[] src)
/*  452:     */   {
/*  453: 472 */     this.buf.setBytes(index, src);
/*  454: 473 */     return this;
/*  455:     */   }
/*  456:     */   
/*  457:     */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  458:     */   {
/*  459: 478 */     this.buf.setBytes(index, src, srcIndex, length);
/*  460: 479 */     return this;
/*  461:     */   }
/*  462:     */   
/*  463:     */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  464:     */   {
/*  465: 484 */     this.buf.setBytes(index, src);
/*  466: 485 */     return this;
/*  467:     */   }
/*  468:     */   
/*  469:     */   public int setBytes(int index, InputStream in, int length)
/*  470:     */     throws IOException
/*  471:     */   {
/*  472: 490 */     return this.buf.setBytes(index, in, length);
/*  473:     */   }
/*  474:     */   
/*  475:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  476:     */     throws IOException
/*  477:     */   {
/*  478: 495 */     return this.buf.setBytes(index, in, length);
/*  479:     */   }
/*  480:     */   
/*  481:     */   public int setBytes(int index, FileChannel in, long position, int length)
/*  482:     */     throws IOException
/*  483:     */   {
/*  484: 500 */     return this.buf.setBytes(index, in, position, length);
/*  485:     */   }
/*  486:     */   
/*  487:     */   public ByteBuf setZero(int index, int length)
/*  488:     */   {
/*  489: 505 */     this.buf.setZero(index, length);
/*  490: 506 */     return this;
/*  491:     */   }
/*  492:     */   
/*  493:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/*  494:     */   {
/*  495: 511 */     return this.buf.setCharSequence(index, sequence, charset);
/*  496:     */   }
/*  497:     */   
/*  498:     */   public boolean readBoolean()
/*  499:     */   {
/*  500: 516 */     return this.buf.readBoolean();
/*  501:     */   }
/*  502:     */   
/*  503:     */   public byte readByte()
/*  504:     */   {
/*  505: 521 */     return this.buf.readByte();
/*  506:     */   }
/*  507:     */   
/*  508:     */   public short readUnsignedByte()
/*  509:     */   {
/*  510: 526 */     return this.buf.readUnsignedByte();
/*  511:     */   }
/*  512:     */   
/*  513:     */   public short readShort()
/*  514:     */   {
/*  515: 531 */     return this.buf.readShort();
/*  516:     */   }
/*  517:     */   
/*  518:     */   public short readShortLE()
/*  519:     */   {
/*  520: 536 */     return this.buf.readShortLE();
/*  521:     */   }
/*  522:     */   
/*  523:     */   public int readUnsignedShort()
/*  524:     */   {
/*  525: 541 */     return this.buf.readUnsignedShort();
/*  526:     */   }
/*  527:     */   
/*  528:     */   public int readUnsignedShortLE()
/*  529:     */   {
/*  530: 546 */     return this.buf.readUnsignedShortLE();
/*  531:     */   }
/*  532:     */   
/*  533:     */   public int readMedium()
/*  534:     */   {
/*  535: 551 */     return this.buf.readMedium();
/*  536:     */   }
/*  537:     */   
/*  538:     */   public int readMediumLE()
/*  539:     */   {
/*  540: 556 */     return this.buf.readMediumLE();
/*  541:     */   }
/*  542:     */   
/*  543:     */   public int readUnsignedMedium()
/*  544:     */   {
/*  545: 561 */     return this.buf.readUnsignedMedium();
/*  546:     */   }
/*  547:     */   
/*  548:     */   public int readUnsignedMediumLE()
/*  549:     */   {
/*  550: 566 */     return this.buf.readUnsignedMediumLE();
/*  551:     */   }
/*  552:     */   
/*  553:     */   public int readInt()
/*  554:     */   {
/*  555: 571 */     return this.buf.readInt();
/*  556:     */   }
/*  557:     */   
/*  558:     */   public int readIntLE()
/*  559:     */   {
/*  560: 576 */     return this.buf.readIntLE();
/*  561:     */   }
/*  562:     */   
/*  563:     */   public long readUnsignedInt()
/*  564:     */   {
/*  565: 581 */     return this.buf.readUnsignedInt();
/*  566:     */   }
/*  567:     */   
/*  568:     */   public long readUnsignedIntLE()
/*  569:     */   {
/*  570: 586 */     return this.buf.readUnsignedIntLE();
/*  571:     */   }
/*  572:     */   
/*  573:     */   public long readLong()
/*  574:     */   {
/*  575: 591 */     return this.buf.readLong();
/*  576:     */   }
/*  577:     */   
/*  578:     */   public long readLongLE()
/*  579:     */   {
/*  580: 596 */     return this.buf.readLongLE();
/*  581:     */   }
/*  582:     */   
/*  583:     */   public char readChar()
/*  584:     */   {
/*  585: 601 */     return this.buf.readChar();
/*  586:     */   }
/*  587:     */   
/*  588:     */   public float readFloat()
/*  589:     */   {
/*  590: 606 */     return this.buf.readFloat();
/*  591:     */   }
/*  592:     */   
/*  593:     */   public double readDouble()
/*  594:     */   {
/*  595: 611 */     return this.buf.readDouble();
/*  596:     */   }
/*  597:     */   
/*  598:     */   public ByteBuf readBytes(int length)
/*  599:     */   {
/*  600: 616 */     return this.buf.readBytes(length);
/*  601:     */   }
/*  602:     */   
/*  603:     */   public ByteBuf readSlice(int length)
/*  604:     */   {
/*  605: 621 */     return this.buf.readSlice(length);
/*  606:     */   }
/*  607:     */   
/*  608:     */   public ByteBuf readRetainedSlice(int length)
/*  609:     */   {
/*  610: 626 */     return this.buf.readRetainedSlice(length);
/*  611:     */   }
/*  612:     */   
/*  613:     */   public ByteBuf readBytes(ByteBuf dst)
/*  614:     */   {
/*  615: 631 */     this.buf.readBytes(dst);
/*  616: 632 */     return this;
/*  617:     */   }
/*  618:     */   
/*  619:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  620:     */   {
/*  621: 637 */     this.buf.readBytes(dst, length);
/*  622: 638 */     return this;
/*  623:     */   }
/*  624:     */   
/*  625:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  626:     */   {
/*  627: 643 */     this.buf.readBytes(dst, dstIndex, length);
/*  628: 644 */     return this;
/*  629:     */   }
/*  630:     */   
/*  631:     */   public ByteBuf readBytes(byte[] dst)
/*  632:     */   {
/*  633: 649 */     this.buf.readBytes(dst);
/*  634: 650 */     return this;
/*  635:     */   }
/*  636:     */   
/*  637:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  638:     */   {
/*  639: 655 */     this.buf.readBytes(dst, dstIndex, length);
/*  640: 656 */     return this;
/*  641:     */   }
/*  642:     */   
/*  643:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  644:     */   {
/*  645: 661 */     this.buf.readBytes(dst);
/*  646: 662 */     return this;
/*  647:     */   }
/*  648:     */   
/*  649:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  650:     */     throws IOException
/*  651:     */   {
/*  652: 667 */     this.buf.readBytes(out, length);
/*  653: 668 */     return this;
/*  654:     */   }
/*  655:     */   
/*  656:     */   public int readBytes(GatheringByteChannel out, int length)
/*  657:     */     throws IOException
/*  658:     */   {
/*  659: 673 */     return this.buf.readBytes(out, length);
/*  660:     */   }
/*  661:     */   
/*  662:     */   public int readBytes(FileChannel out, long position, int length)
/*  663:     */     throws IOException
/*  664:     */   {
/*  665: 678 */     return this.buf.readBytes(out, position, length);
/*  666:     */   }
/*  667:     */   
/*  668:     */   public CharSequence readCharSequence(int length, Charset charset)
/*  669:     */   {
/*  670: 683 */     return this.buf.readCharSequence(length, charset);
/*  671:     */   }
/*  672:     */   
/*  673:     */   public ByteBuf skipBytes(int length)
/*  674:     */   {
/*  675: 688 */     this.buf.skipBytes(length);
/*  676: 689 */     return this;
/*  677:     */   }
/*  678:     */   
/*  679:     */   public ByteBuf writeBoolean(boolean value)
/*  680:     */   {
/*  681: 694 */     this.buf.writeBoolean(value);
/*  682: 695 */     return this;
/*  683:     */   }
/*  684:     */   
/*  685:     */   public ByteBuf writeByte(int value)
/*  686:     */   {
/*  687: 700 */     this.buf.writeByte(value);
/*  688: 701 */     return this;
/*  689:     */   }
/*  690:     */   
/*  691:     */   public ByteBuf writeShort(int value)
/*  692:     */   {
/*  693: 706 */     this.buf.writeShort(value);
/*  694: 707 */     return this;
/*  695:     */   }
/*  696:     */   
/*  697:     */   public ByteBuf writeShortLE(int value)
/*  698:     */   {
/*  699: 712 */     this.buf.writeShortLE(value);
/*  700: 713 */     return this;
/*  701:     */   }
/*  702:     */   
/*  703:     */   public ByteBuf writeMedium(int value)
/*  704:     */   {
/*  705: 718 */     this.buf.writeMedium(value);
/*  706: 719 */     return this;
/*  707:     */   }
/*  708:     */   
/*  709:     */   public ByteBuf writeMediumLE(int value)
/*  710:     */   {
/*  711: 724 */     this.buf.writeMediumLE(value);
/*  712: 725 */     return this;
/*  713:     */   }
/*  714:     */   
/*  715:     */   public ByteBuf writeInt(int value)
/*  716:     */   {
/*  717: 730 */     this.buf.writeInt(value);
/*  718: 731 */     return this;
/*  719:     */   }
/*  720:     */   
/*  721:     */   public ByteBuf writeIntLE(int value)
/*  722:     */   {
/*  723: 736 */     this.buf.writeIntLE(value);
/*  724: 737 */     return this;
/*  725:     */   }
/*  726:     */   
/*  727:     */   public ByteBuf writeLong(long value)
/*  728:     */   {
/*  729: 742 */     this.buf.writeLong(value);
/*  730: 743 */     return this;
/*  731:     */   }
/*  732:     */   
/*  733:     */   public ByteBuf writeLongLE(long value)
/*  734:     */   {
/*  735: 748 */     this.buf.writeLongLE(value);
/*  736: 749 */     return this;
/*  737:     */   }
/*  738:     */   
/*  739:     */   public ByteBuf writeChar(int value)
/*  740:     */   {
/*  741: 754 */     this.buf.writeChar(value);
/*  742: 755 */     return this;
/*  743:     */   }
/*  744:     */   
/*  745:     */   public ByteBuf writeFloat(float value)
/*  746:     */   {
/*  747: 760 */     this.buf.writeFloat(value);
/*  748: 761 */     return this;
/*  749:     */   }
/*  750:     */   
/*  751:     */   public ByteBuf writeDouble(double value)
/*  752:     */   {
/*  753: 766 */     this.buf.writeDouble(value);
/*  754: 767 */     return this;
/*  755:     */   }
/*  756:     */   
/*  757:     */   public ByteBuf writeBytes(ByteBuf src)
/*  758:     */   {
/*  759: 772 */     this.buf.writeBytes(src);
/*  760: 773 */     return this;
/*  761:     */   }
/*  762:     */   
/*  763:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/*  764:     */   {
/*  765: 778 */     this.buf.writeBytes(src, length);
/*  766: 779 */     return this;
/*  767:     */   }
/*  768:     */   
/*  769:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  770:     */   {
/*  771: 784 */     this.buf.writeBytes(src, srcIndex, length);
/*  772: 785 */     return this;
/*  773:     */   }
/*  774:     */   
/*  775:     */   public ByteBuf writeBytes(byte[] src)
/*  776:     */   {
/*  777: 790 */     this.buf.writeBytes(src);
/*  778: 791 */     return this;
/*  779:     */   }
/*  780:     */   
/*  781:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  782:     */   {
/*  783: 796 */     this.buf.writeBytes(src, srcIndex, length);
/*  784: 797 */     return this;
/*  785:     */   }
/*  786:     */   
/*  787:     */   public ByteBuf writeBytes(ByteBuffer src)
/*  788:     */   {
/*  789: 802 */     this.buf.writeBytes(src);
/*  790: 803 */     return this;
/*  791:     */   }
/*  792:     */   
/*  793:     */   public int writeBytes(InputStream in, int length)
/*  794:     */     throws IOException
/*  795:     */   {
/*  796: 808 */     return this.buf.writeBytes(in, length);
/*  797:     */   }
/*  798:     */   
/*  799:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  800:     */     throws IOException
/*  801:     */   {
/*  802: 813 */     return this.buf.writeBytes(in, length);
/*  803:     */   }
/*  804:     */   
/*  805:     */   public int writeBytes(FileChannel in, long position, int length)
/*  806:     */     throws IOException
/*  807:     */   {
/*  808: 818 */     return this.buf.writeBytes(in, position, length);
/*  809:     */   }
/*  810:     */   
/*  811:     */   public ByteBuf writeZero(int length)
/*  812:     */   {
/*  813: 823 */     this.buf.writeZero(length);
/*  814: 824 */     return this;
/*  815:     */   }
/*  816:     */   
/*  817:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/*  818:     */   {
/*  819: 829 */     return this.buf.writeCharSequence(sequence, charset);
/*  820:     */   }
/*  821:     */   
/*  822:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  823:     */   {
/*  824: 834 */     return this.buf.indexOf(fromIndex, toIndex, value);
/*  825:     */   }
/*  826:     */   
/*  827:     */   public int bytesBefore(byte value)
/*  828:     */   {
/*  829: 839 */     return this.buf.bytesBefore(value);
/*  830:     */   }
/*  831:     */   
/*  832:     */   public int bytesBefore(int length, byte value)
/*  833:     */   {
/*  834: 844 */     return this.buf.bytesBefore(length, value);
/*  835:     */   }
/*  836:     */   
/*  837:     */   public int bytesBefore(int index, int length, byte value)
/*  838:     */   {
/*  839: 849 */     return this.buf.bytesBefore(index, length, value);
/*  840:     */   }
/*  841:     */   
/*  842:     */   public int forEachByte(ByteProcessor processor)
/*  843:     */   {
/*  844: 854 */     return this.buf.forEachByte(processor);
/*  845:     */   }
/*  846:     */   
/*  847:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/*  848:     */   {
/*  849: 859 */     return this.buf.forEachByte(index, length, processor);
/*  850:     */   }
/*  851:     */   
/*  852:     */   public int forEachByteDesc(ByteProcessor processor)
/*  853:     */   {
/*  854: 864 */     return this.buf.forEachByteDesc(processor);
/*  855:     */   }
/*  856:     */   
/*  857:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/*  858:     */   {
/*  859: 869 */     return this.buf.forEachByteDesc(index, length, processor);
/*  860:     */   }
/*  861:     */   
/*  862:     */   public ByteBuf copy()
/*  863:     */   {
/*  864: 874 */     return this.buf.copy();
/*  865:     */   }
/*  866:     */   
/*  867:     */   public ByteBuf copy(int index, int length)
/*  868:     */   {
/*  869: 879 */     return this.buf.copy(index, length);
/*  870:     */   }
/*  871:     */   
/*  872:     */   public ByteBuf slice()
/*  873:     */   {
/*  874: 884 */     return this.buf.slice();
/*  875:     */   }
/*  876:     */   
/*  877:     */   public ByteBuf retainedSlice()
/*  878:     */   {
/*  879: 889 */     return this.buf.retainedSlice();
/*  880:     */   }
/*  881:     */   
/*  882:     */   public ByteBuf slice(int index, int length)
/*  883:     */   {
/*  884: 894 */     return this.buf.slice(index, length);
/*  885:     */   }
/*  886:     */   
/*  887:     */   public ByteBuf retainedSlice(int index, int length)
/*  888:     */   {
/*  889: 899 */     return this.buf.retainedSlice(index, length);
/*  890:     */   }
/*  891:     */   
/*  892:     */   public ByteBuf duplicate()
/*  893:     */   {
/*  894: 904 */     return this.buf.duplicate();
/*  895:     */   }
/*  896:     */   
/*  897:     */   public ByteBuf retainedDuplicate()
/*  898:     */   {
/*  899: 909 */     return this.buf.retainedDuplicate();
/*  900:     */   }
/*  901:     */   
/*  902:     */   public int nioBufferCount()
/*  903:     */   {
/*  904: 914 */     return this.buf.nioBufferCount();
/*  905:     */   }
/*  906:     */   
/*  907:     */   public ByteBuffer nioBuffer()
/*  908:     */   {
/*  909: 919 */     return this.buf.nioBuffer();
/*  910:     */   }
/*  911:     */   
/*  912:     */   public ByteBuffer nioBuffer(int index, int length)
/*  913:     */   {
/*  914: 924 */     return this.buf.nioBuffer(index, length);
/*  915:     */   }
/*  916:     */   
/*  917:     */   public ByteBuffer[] nioBuffers()
/*  918:     */   {
/*  919: 929 */     return this.buf.nioBuffers();
/*  920:     */   }
/*  921:     */   
/*  922:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  923:     */   {
/*  924: 934 */     return this.buf.nioBuffers(index, length);
/*  925:     */   }
/*  926:     */   
/*  927:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  928:     */   {
/*  929: 939 */     return this.buf.internalNioBuffer(index, length);
/*  930:     */   }
/*  931:     */   
/*  932:     */   public boolean hasArray()
/*  933:     */   {
/*  934: 944 */     return this.buf.hasArray();
/*  935:     */   }
/*  936:     */   
/*  937:     */   public byte[] array()
/*  938:     */   {
/*  939: 949 */     return this.buf.array();
/*  940:     */   }
/*  941:     */   
/*  942:     */   public int arrayOffset()
/*  943:     */   {
/*  944: 954 */     return this.buf.arrayOffset();
/*  945:     */   }
/*  946:     */   
/*  947:     */   public String toString(Charset charset)
/*  948:     */   {
/*  949: 959 */     return this.buf.toString(charset);
/*  950:     */   }
/*  951:     */   
/*  952:     */   public String toString(int index, int length, Charset charset)
/*  953:     */   {
/*  954: 964 */     return this.buf.toString(index, length, charset);
/*  955:     */   }
/*  956:     */   
/*  957:     */   public int hashCode()
/*  958:     */   {
/*  959: 969 */     return this.buf.hashCode();
/*  960:     */   }
/*  961:     */   
/*  962:     */   public boolean equals(Object obj)
/*  963:     */   {
/*  964: 975 */     return this.buf.equals(obj);
/*  965:     */   }
/*  966:     */   
/*  967:     */   public int compareTo(ByteBuf buffer)
/*  968:     */   {
/*  969: 980 */     return this.buf.compareTo(buffer);
/*  970:     */   }
/*  971:     */   
/*  972:     */   public String toString()
/*  973:     */   {
/*  974: 985 */     return StringUtil.simpleClassName(this) + '(' + this.buf.toString() + ')';
/*  975:     */   }
/*  976:     */   
/*  977:     */   public ByteBuf retain(int increment)
/*  978:     */   {
/*  979: 990 */     this.buf.retain(increment);
/*  980: 991 */     return this;
/*  981:     */   }
/*  982:     */   
/*  983:     */   public ByteBuf retain()
/*  984:     */   {
/*  985: 996 */     this.buf.retain();
/*  986: 997 */     return this;
/*  987:     */   }
/*  988:     */   
/*  989:     */   public ByteBuf touch()
/*  990:     */   {
/*  991:1002 */     this.buf.touch();
/*  992:1003 */     return this;
/*  993:     */   }
/*  994:     */   
/*  995:     */   public ByteBuf touch(Object hint)
/*  996:     */   {
/*  997:1008 */     this.buf.touch(hint);
/*  998:1009 */     return this;
/*  999:     */   }
/* 1000:     */   
/* 1001:     */   public final boolean isReadable(int size)
/* 1002:     */   {
/* 1003:1014 */     return this.buf.isReadable(size);
/* 1004:     */   }
/* 1005:     */   
/* 1006:     */   public final boolean isWritable(int size)
/* 1007:     */   {
/* 1008:1019 */     return this.buf.isWritable(size);
/* 1009:     */   }
/* 1010:     */   
/* 1011:     */   public final int refCnt()
/* 1012:     */   {
/* 1013:1024 */     return this.buf.refCnt();
/* 1014:     */   }
/* 1015:     */   
/* 1016:     */   public boolean release()
/* 1017:     */   {
/* 1018:1029 */     return this.buf.release();
/* 1019:     */   }
/* 1020:     */   
/* 1021:     */   public boolean release(int decrement)
/* 1022:     */   {
/* 1023:1034 */     return this.buf.release(decrement);
/* 1024:     */   }
/* 1025:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.WrappedByteBuf
 * JD-Core Version:    0.7.0.1
 */