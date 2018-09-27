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
/*   13:     */ import java.util.Iterator;
/*   14:     */ import java.util.List;
/*   15:     */ 
/*   16:     */ class WrappedCompositeByteBuf
/*   17:     */   extends CompositeByteBuf
/*   18:     */ {
/*   19:     */   private final CompositeByteBuf wrapped;
/*   20:     */   
/*   21:     */   WrappedCompositeByteBuf(CompositeByteBuf wrapped)
/*   22:     */   {
/*   23:  37 */     super(wrapped.alloc());
/*   24:  38 */     this.wrapped = wrapped;
/*   25:     */   }
/*   26:     */   
/*   27:     */   public boolean release()
/*   28:     */   {
/*   29:  43 */     return this.wrapped.release();
/*   30:     */   }
/*   31:     */   
/*   32:     */   public boolean release(int decrement)
/*   33:     */   {
/*   34:  48 */     return this.wrapped.release(decrement);
/*   35:     */   }
/*   36:     */   
/*   37:     */   public final int maxCapacity()
/*   38:     */   {
/*   39:  53 */     return this.wrapped.maxCapacity();
/*   40:     */   }
/*   41:     */   
/*   42:     */   public final int readerIndex()
/*   43:     */   {
/*   44:  58 */     return this.wrapped.readerIndex();
/*   45:     */   }
/*   46:     */   
/*   47:     */   public final int writerIndex()
/*   48:     */   {
/*   49:  63 */     return this.wrapped.writerIndex();
/*   50:     */   }
/*   51:     */   
/*   52:     */   public final boolean isReadable()
/*   53:     */   {
/*   54:  68 */     return this.wrapped.isReadable();
/*   55:     */   }
/*   56:     */   
/*   57:     */   public final boolean isReadable(int numBytes)
/*   58:     */   {
/*   59:  73 */     return this.wrapped.isReadable(numBytes);
/*   60:     */   }
/*   61:     */   
/*   62:     */   public final boolean isWritable()
/*   63:     */   {
/*   64:  78 */     return this.wrapped.isWritable();
/*   65:     */   }
/*   66:     */   
/*   67:     */   public final boolean isWritable(int numBytes)
/*   68:     */   {
/*   69:  83 */     return this.wrapped.isWritable(numBytes);
/*   70:     */   }
/*   71:     */   
/*   72:     */   public final int readableBytes()
/*   73:     */   {
/*   74:  88 */     return this.wrapped.readableBytes();
/*   75:     */   }
/*   76:     */   
/*   77:     */   public final int writableBytes()
/*   78:     */   {
/*   79:  93 */     return this.wrapped.writableBytes();
/*   80:     */   }
/*   81:     */   
/*   82:     */   public final int maxWritableBytes()
/*   83:     */   {
/*   84:  98 */     return this.wrapped.maxWritableBytes();
/*   85:     */   }
/*   86:     */   
/*   87:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*   88:     */   {
/*   89: 103 */     return this.wrapped.ensureWritable(minWritableBytes, force);
/*   90:     */   }
/*   91:     */   
/*   92:     */   public ByteBuf order(ByteOrder endianness)
/*   93:     */   {
/*   94: 108 */     return this.wrapped.order(endianness);
/*   95:     */   }
/*   96:     */   
/*   97:     */   public boolean getBoolean(int index)
/*   98:     */   {
/*   99: 113 */     return this.wrapped.getBoolean(index);
/*  100:     */   }
/*  101:     */   
/*  102:     */   public short getUnsignedByte(int index)
/*  103:     */   {
/*  104: 118 */     return this.wrapped.getUnsignedByte(index);
/*  105:     */   }
/*  106:     */   
/*  107:     */   public short getShort(int index)
/*  108:     */   {
/*  109: 123 */     return this.wrapped.getShort(index);
/*  110:     */   }
/*  111:     */   
/*  112:     */   public short getShortLE(int index)
/*  113:     */   {
/*  114: 128 */     return this.wrapped.getShortLE(index);
/*  115:     */   }
/*  116:     */   
/*  117:     */   public int getUnsignedShort(int index)
/*  118:     */   {
/*  119: 133 */     return this.wrapped.getUnsignedShort(index);
/*  120:     */   }
/*  121:     */   
/*  122:     */   public int getUnsignedShortLE(int index)
/*  123:     */   {
/*  124: 138 */     return this.wrapped.getUnsignedShortLE(index);
/*  125:     */   }
/*  126:     */   
/*  127:     */   public int getUnsignedMedium(int index)
/*  128:     */   {
/*  129: 143 */     return this.wrapped.getUnsignedMedium(index);
/*  130:     */   }
/*  131:     */   
/*  132:     */   public int getUnsignedMediumLE(int index)
/*  133:     */   {
/*  134: 148 */     return this.wrapped.getUnsignedMediumLE(index);
/*  135:     */   }
/*  136:     */   
/*  137:     */   public int getMedium(int index)
/*  138:     */   {
/*  139: 153 */     return this.wrapped.getMedium(index);
/*  140:     */   }
/*  141:     */   
/*  142:     */   public int getMediumLE(int index)
/*  143:     */   {
/*  144: 158 */     return this.wrapped.getMediumLE(index);
/*  145:     */   }
/*  146:     */   
/*  147:     */   public int getInt(int index)
/*  148:     */   {
/*  149: 163 */     return this.wrapped.getInt(index);
/*  150:     */   }
/*  151:     */   
/*  152:     */   public int getIntLE(int index)
/*  153:     */   {
/*  154: 168 */     return this.wrapped.getIntLE(index);
/*  155:     */   }
/*  156:     */   
/*  157:     */   public long getUnsignedInt(int index)
/*  158:     */   {
/*  159: 173 */     return this.wrapped.getUnsignedInt(index);
/*  160:     */   }
/*  161:     */   
/*  162:     */   public long getUnsignedIntLE(int index)
/*  163:     */   {
/*  164: 178 */     return this.wrapped.getUnsignedIntLE(index);
/*  165:     */   }
/*  166:     */   
/*  167:     */   public long getLong(int index)
/*  168:     */   {
/*  169: 183 */     return this.wrapped.getLong(index);
/*  170:     */   }
/*  171:     */   
/*  172:     */   public long getLongLE(int index)
/*  173:     */   {
/*  174: 188 */     return this.wrapped.getLongLE(index);
/*  175:     */   }
/*  176:     */   
/*  177:     */   public char getChar(int index)
/*  178:     */   {
/*  179: 193 */     return this.wrapped.getChar(index);
/*  180:     */   }
/*  181:     */   
/*  182:     */   public float getFloat(int index)
/*  183:     */   {
/*  184: 198 */     return this.wrapped.getFloat(index);
/*  185:     */   }
/*  186:     */   
/*  187:     */   public double getDouble(int index)
/*  188:     */   {
/*  189: 203 */     return this.wrapped.getDouble(index);
/*  190:     */   }
/*  191:     */   
/*  192:     */   public ByteBuf setShortLE(int index, int value)
/*  193:     */   {
/*  194: 208 */     return this.wrapped.setShortLE(index, value);
/*  195:     */   }
/*  196:     */   
/*  197:     */   public ByteBuf setMediumLE(int index, int value)
/*  198:     */   {
/*  199: 213 */     return this.wrapped.setMediumLE(index, value);
/*  200:     */   }
/*  201:     */   
/*  202:     */   public ByteBuf setIntLE(int index, int value)
/*  203:     */   {
/*  204: 218 */     return this.wrapped.setIntLE(index, value);
/*  205:     */   }
/*  206:     */   
/*  207:     */   public ByteBuf setLongLE(int index, long value)
/*  208:     */   {
/*  209: 223 */     return this.wrapped.setLongLE(index, value);
/*  210:     */   }
/*  211:     */   
/*  212:     */   public byte readByte()
/*  213:     */   {
/*  214: 228 */     return this.wrapped.readByte();
/*  215:     */   }
/*  216:     */   
/*  217:     */   public boolean readBoolean()
/*  218:     */   {
/*  219: 233 */     return this.wrapped.readBoolean();
/*  220:     */   }
/*  221:     */   
/*  222:     */   public short readUnsignedByte()
/*  223:     */   {
/*  224: 238 */     return this.wrapped.readUnsignedByte();
/*  225:     */   }
/*  226:     */   
/*  227:     */   public short readShort()
/*  228:     */   {
/*  229: 243 */     return this.wrapped.readShort();
/*  230:     */   }
/*  231:     */   
/*  232:     */   public short readShortLE()
/*  233:     */   {
/*  234: 248 */     return this.wrapped.readShortLE();
/*  235:     */   }
/*  236:     */   
/*  237:     */   public int readUnsignedShort()
/*  238:     */   {
/*  239: 253 */     return this.wrapped.readUnsignedShort();
/*  240:     */   }
/*  241:     */   
/*  242:     */   public int readUnsignedShortLE()
/*  243:     */   {
/*  244: 258 */     return this.wrapped.readUnsignedShortLE();
/*  245:     */   }
/*  246:     */   
/*  247:     */   public int readMedium()
/*  248:     */   {
/*  249: 263 */     return this.wrapped.readMedium();
/*  250:     */   }
/*  251:     */   
/*  252:     */   public int readMediumLE()
/*  253:     */   {
/*  254: 268 */     return this.wrapped.readMediumLE();
/*  255:     */   }
/*  256:     */   
/*  257:     */   public int readUnsignedMedium()
/*  258:     */   {
/*  259: 273 */     return this.wrapped.readUnsignedMedium();
/*  260:     */   }
/*  261:     */   
/*  262:     */   public int readUnsignedMediumLE()
/*  263:     */   {
/*  264: 278 */     return this.wrapped.readUnsignedMediumLE();
/*  265:     */   }
/*  266:     */   
/*  267:     */   public int readInt()
/*  268:     */   {
/*  269: 283 */     return this.wrapped.readInt();
/*  270:     */   }
/*  271:     */   
/*  272:     */   public int readIntLE()
/*  273:     */   {
/*  274: 288 */     return this.wrapped.readIntLE();
/*  275:     */   }
/*  276:     */   
/*  277:     */   public long readUnsignedInt()
/*  278:     */   {
/*  279: 293 */     return this.wrapped.readUnsignedInt();
/*  280:     */   }
/*  281:     */   
/*  282:     */   public long readUnsignedIntLE()
/*  283:     */   {
/*  284: 298 */     return this.wrapped.readUnsignedIntLE();
/*  285:     */   }
/*  286:     */   
/*  287:     */   public long readLong()
/*  288:     */   {
/*  289: 303 */     return this.wrapped.readLong();
/*  290:     */   }
/*  291:     */   
/*  292:     */   public long readLongLE()
/*  293:     */   {
/*  294: 308 */     return this.wrapped.readLongLE();
/*  295:     */   }
/*  296:     */   
/*  297:     */   public char readChar()
/*  298:     */   {
/*  299: 313 */     return this.wrapped.readChar();
/*  300:     */   }
/*  301:     */   
/*  302:     */   public float readFloat()
/*  303:     */   {
/*  304: 318 */     return this.wrapped.readFloat();
/*  305:     */   }
/*  306:     */   
/*  307:     */   public double readDouble()
/*  308:     */   {
/*  309: 323 */     return this.wrapped.readDouble();
/*  310:     */   }
/*  311:     */   
/*  312:     */   public ByteBuf readBytes(int length)
/*  313:     */   {
/*  314: 328 */     return this.wrapped.readBytes(length);
/*  315:     */   }
/*  316:     */   
/*  317:     */   public ByteBuf slice()
/*  318:     */   {
/*  319: 333 */     return this.wrapped.slice();
/*  320:     */   }
/*  321:     */   
/*  322:     */   public ByteBuf retainedSlice()
/*  323:     */   {
/*  324: 338 */     return this.wrapped.retainedSlice();
/*  325:     */   }
/*  326:     */   
/*  327:     */   public ByteBuf slice(int index, int length)
/*  328:     */   {
/*  329: 343 */     return this.wrapped.slice(index, length);
/*  330:     */   }
/*  331:     */   
/*  332:     */   public ByteBuf retainedSlice(int index, int length)
/*  333:     */   {
/*  334: 348 */     return this.wrapped.retainedSlice(index, length);
/*  335:     */   }
/*  336:     */   
/*  337:     */   public ByteBuffer nioBuffer()
/*  338:     */   {
/*  339: 353 */     return this.wrapped.nioBuffer();
/*  340:     */   }
/*  341:     */   
/*  342:     */   public String toString(Charset charset)
/*  343:     */   {
/*  344: 358 */     return this.wrapped.toString(charset);
/*  345:     */   }
/*  346:     */   
/*  347:     */   public String toString(int index, int length, Charset charset)
/*  348:     */   {
/*  349: 363 */     return this.wrapped.toString(index, length, charset);
/*  350:     */   }
/*  351:     */   
/*  352:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  353:     */   {
/*  354: 368 */     return this.wrapped.indexOf(fromIndex, toIndex, value);
/*  355:     */   }
/*  356:     */   
/*  357:     */   public int bytesBefore(byte value)
/*  358:     */   {
/*  359: 373 */     return this.wrapped.bytesBefore(value);
/*  360:     */   }
/*  361:     */   
/*  362:     */   public int bytesBefore(int length, byte value)
/*  363:     */   {
/*  364: 378 */     return this.wrapped.bytesBefore(length, value);
/*  365:     */   }
/*  366:     */   
/*  367:     */   public int bytesBefore(int index, int length, byte value)
/*  368:     */   {
/*  369: 383 */     return this.wrapped.bytesBefore(index, length, value);
/*  370:     */   }
/*  371:     */   
/*  372:     */   public int forEachByte(ByteProcessor processor)
/*  373:     */   {
/*  374: 388 */     return this.wrapped.forEachByte(processor);
/*  375:     */   }
/*  376:     */   
/*  377:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/*  378:     */   {
/*  379: 393 */     return this.wrapped.forEachByte(index, length, processor);
/*  380:     */   }
/*  381:     */   
/*  382:     */   public int forEachByteDesc(ByteProcessor processor)
/*  383:     */   {
/*  384: 398 */     return this.wrapped.forEachByteDesc(processor);
/*  385:     */   }
/*  386:     */   
/*  387:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/*  388:     */   {
/*  389: 403 */     return this.wrapped.forEachByteDesc(index, length, processor);
/*  390:     */   }
/*  391:     */   
/*  392:     */   public final int hashCode()
/*  393:     */   {
/*  394: 408 */     return this.wrapped.hashCode();
/*  395:     */   }
/*  396:     */   
/*  397:     */   public final boolean equals(Object o)
/*  398:     */   {
/*  399: 413 */     return this.wrapped.equals(o);
/*  400:     */   }
/*  401:     */   
/*  402:     */   public final int compareTo(ByteBuf that)
/*  403:     */   {
/*  404: 418 */     return this.wrapped.compareTo(that);
/*  405:     */   }
/*  406:     */   
/*  407:     */   public final int refCnt()
/*  408:     */   {
/*  409: 423 */     return this.wrapped.refCnt();
/*  410:     */   }
/*  411:     */   
/*  412:     */   public ByteBuf duplicate()
/*  413:     */   {
/*  414: 428 */     return this.wrapped.duplicate();
/*  415:     */   }
/*  416:     */   
/*  417:     */   public ByteBuf retainedDuplicate()
/*  418:     */   {
/*  419: 433 */     return this.wrapped.retainedDuplicate();
/*  420:     */   }
/*  421:     */   
/*  422:     */   public ByteBuf readSlice(int length)
/*  423:     */   {
/*  424: 438 */     return this.wrapped.readSlice(length);
/*  425:     */   }
/*  426:     */   
/*  427:     */   public ByteBuf readRetainedSlice(int length)
/*  428:     */   {
/*  429: 443 */     return this.wrapped.readRetainedSlice(length);
/*  430:     */   }
/*  431:     */   
/*  432:     */   public int readBytes(GatheringByteChannel out, int length)
/*  433:     */     throws IOException
/*  434:     */   {
/*  435: 448 */     return this.wrapped.readBytes(out, length);
/*  436:     */   }
/*  437:     */   
/*  438:     */   public ByteBuf writeShortLE(int value)
/*  439:     */   {
/*  440: 453 */     return this.wrapped.writeShortLE(value);
/*  441:     */   }
/*  442:     */   
/*  443:     */   public ByteBuf writeMediumLE(int value)
/*  444:     */   {
/*  445: 458 */     return this.wrapped.writeMediumLE(value);
/*  446:     */   }
/*  447:     */   
/*  448:     */   public ByteBuf writeIntLE(int value)
/*  449:     */   {
/*  450: 463 */     return this.wrapped.writeIntLE(value);
/*  451:     */   }
/*  452:     */   
/*  453:     */   public ByteBuf writeLongLE(long value)
/*  454:     */   {
/*  455: 468 */     return this.wrapped.writeLongLE(value);
/*  456:     */   }
/*  457:     */   
/*  458:     */   public int writeBytes(InputStream in, int length)
/*  459:     */     throws IOException
/*  460:     */   {
/*  461: 473 */     return this.wrapped.writeBytes(in, length);
/*  462:     */   }
/*  463:     */   
/*  464:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  465:     */     throws IOException
/*  466:     */   {
/*  467: 478 */     return this.wrapped.writeBytes(in, length);
/*  468:     */   }
/*  469:     */   
/*  470:     */   public ByteBuf copy()
/*  471:     */   {
/*  472: 483 */     return this.wrapped.copy();
/*  473:     */   }
/*  474:     */   
/*  475:     */   public CompositeByteBuf addComponent(ByteBuf buffer)
/*  476:     */   {
/*  477: 488 */     this.wrapped.addComponent(buffer);
/*  478: 489 */     return this;
/*  479:     */   }
/*  480:     */   
/*  481:     */   public CompositeByteBuf addComponents(ByteBuf... buffers)
/*  482:     */   {
/*  483: 494 */     this.wrapped.addComponents(buffers);
/*  484: 495 */     return this;
/*  485:     */   }
/*  486:     */   
/*  487:     */   public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers)
/*  488:     */   {
/*  489: 500 */     this.wrapped.addComponents(buffers);
/*  490: 501 */     return this;
/*  491:     */   }
/*  492:     */   
/*  493:     */   public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer)
/*  494:     */   {
/*  495: 506 */     this.wrapped.addComponent(cIndex, buffer);
/*  496: 507 */     return this;
/*  497:     */   }
/*  498:     */   
/*  499:     */   public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers)
/*  500:     */   {
/*  501: 512 */     this.wrapped.addComponents(cIndex, buffers);
/*  502: 513 */     return this;
/*  503:     */   }
/*  504:     */   
/*  505:     */   public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers)
/*  506:     */   {
/*  507: 518 */     this.wrapped.addComponents(cIndex, buffers);
/*  508: 519 */     return this;
/*  509:     */   }
/*  510:     */   
/*  511:     */   public CompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer)
/*  512:     */   {
/*  513: 524 */     this.wrapped.addComponent(increaseWriterIndex, buffer);
/*  514: 525 */     return this;
/*  515:     */   }
/*  516:     */   
/*  517:     */   public CompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers)
/*  518:     */   {
/*  519: 530 */     this.wrapped.addComponents(increaseWriterIndex, buffers);
/*  520: 531 */     return this;
/*  521:     */   }
/*  522:     */   
/*  523:     */   public CompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers)
/*  524:     */   {
/*  525: 536 */     this.wrapped.addComponents(increaseWriterIndex, buffers);
/*  526: 537 */     return this;
/*  527:     */   }
/*  528:     */   
/*  529:     */   public CompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer)
/*  530:     */   {
/*  531: 542 */     this.wrapped.addComponent(increaseWriterIndex, cIndex, buffer);
/*  532: 543 */     return this;
/*  533:     */   }
/*  534:     */   
/*  535:     */   public CompositeByteBuf removeComponent(int cIndex)
/*  536:     */   {
/*  537: 548 */     this.wrapped.removeComponent(cIndex);
/*  538: 549 */     return this;
/*  539:     */   }
/*  540:     */   
/*  541:     */   public CompositeByteBuf removeComponents(int cIndex, int numComponents)
/*  542:     */   {
/*  543: 554 */     this.wrapped.removeComponents(cIndex, numComponents);
/*  544: 555 */     return this;
/*  545:     */   }
/*  546:     */   
/*  547:     */   public Iterator<ByteBuf> iterator()
/*  548:     */   {
/*  549: 560 */     return this.wrapped.iterator();
/*  550:     */   }
/*  551:     */   
/*  552:     */   public List<ByteBuf> decompose(int offset, int length)
/*  553:     */   {
/*  554: 565 */     return this.wrapped.decompose(offset, length);
/*  555:     */   }
/*  556:     */   
/*  557:     */   public final boolean isDirect()
/*  558:     */   {
/*  559: 570 */     return this.wrapped.isDirect();
/*  560:     */   }
/*  561:     */   
/*  562:     */   public final boolean hasArray()
/*  563:     */   {
/*  564: 575 */     return this.wrapped.hasArray();
/*  565:     */   }
/*  566:     */   
/*  567:     */   public final byte[] array()
/*  568:     */   {
/*  569: 580 */     return this.wrapped.array();
/*  570:     */   }
/*  571:     */   
/*  572:     */   public final int arrayOffset()
/*  573:     */   {
/*  574: 585 */     return this.wrapped.arrayOffset();
/*  575:     */   }
/*  576:     */   
/*  577:     */   public final boolean hasMemoryAddress()
/*  578:     */   {
/*  579: 590 */     return this.wrapped.hasMemoryAddress();
/*  580:     */   }
/*  581:     */   
/*  582:     */   public final long memoryAddress()
/*  583:     */   {
/*  584: 595 */     return this.wrapped.memoryAddress();
/*  585:     */   }
/*  586:     */   
/*  587:     */   public final int capacity()
/*  588:     */   {
/*  589: 600 */     return this.wrapped.capacity();
/*  590:     */   }
/*  591:     */   
/*  592:     */   public CompositeByteBuf capacity(int newCapacity)
/*  593:     */   {
/*  594: 605 */     this.wrapped.capacity(newCapacity);
/*  595: 606 */     return this;
/*  596:     */   }
/*  597:     */   
/*  598:     */   public final ByteBufAllocator alloc()
/*  599:     */   {
/*  600: 611 */     return this.wrapped.alloc();
/*  601:     */   }
/*  602:     */   
/*  603:     */   public final ByteOrder order()
/*  604:     */   {
/*  605: 616 */     return this.wrapped.order();
/*  606:     */   }
/*  607:     */   
/*  608:     */   public final int numComponents()
/*  609:     */   {
/*  610: 621 */     return this.wrapped.numComponents();
/*  611:     */   }
/*  612:     */   
/*  613:     */   public final int maxNumComponents()
/*  614:     */   {
/*  615: 626 */     return this.wrapped.maxNumComponents();
/*  616:     */   }
/*  617:     */   
/*  618:     */   public final int toComponentIndex(int offset)
/*  619:     */   {
/*  620: 631 */     return this.wrapped.toComponentIndex(offset);
/*  621:     */   }
/*  622:     */   
/*  623:     */   public final int toByteIndex(int cIndex)
/*  624:     */   {
/*  625: 636 */     return this.wrapped.toByteIndex(cIndex);
/*  626:     */   }
/*  627:     */   
/*  628:     */   public byte getByte(int index)
/*  629:     */   {
/*  630: 641 */     return this.wrapped.getByte(index);
/*  631:     */   }
/*  632:     */   
/*  633:     */   protected final byte _getByte(int index)
/*  634:     */   {
/*  635: 646 */     return this.wrapped._getByte(index);
/*  636:     */   }
/*  637:     */   
/*  638:     */   protected final short _getShort(int index)
/*  639:     */   {
/*  640: 651 */     return this.wrapped._getShort(index);
/*  641:     */   }
/*  642:     */   
/*  643:     */   protected final short _getShortLE(int index)
/*  644:     */   {
/*  645: 656 */     return this.wrapped._getShortLE(index);
/*  646:     */   }
/*  647:     */   
/*  648:     */   protected final int _getUnsignedMedium(int index)
/*  649:     */   {
/*  650: 661 */     return this.wrapped._getUnsignedMedium(index);
/*  651:     */   }
/*  652:     */   
/*  653:     */   protected final int _getUnsignedMediumLE(int index)
/*  654:     */   {
/*  655: 666 */     return this.wrapped._getUnsignedMediumLE(index);
/*  656:     */   }
/*  657:     */   
/*  658:     */   protected final int _getInt(int index)
/*  659:     */   {
/*  660: 671 */     return this.wrapped._getInt(index);
/*  661:     */   }
/*  662:     */   
/*  663:     */   protected final int _getIntLE(int index)
/*  664:     */   {
/*  665: 676 */     return this.wrapped._getIntLE(index);
/*  666:     */   }
/*  667:     */   
/*  668:     */   protected final long _getLong(int index)
/*  669:     */   {
/*  670: 681 */     return this.wrapped._getLong(index);
/*  671:     */   }
/*  672:     */   
/*  673:     */   protected final long _getLongLE(int index)
/*  674:     */   {
/*  675: 686 */     return this.wrapped._getLongLE(index);
/*  676:     */   }
/*  677:     */   
/*  678:     */   public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  679:     */   {
/*  680: 691 */     this.wrapped.getBytes(index, dst, dstIndex, length);
/*  681: 692 */     return this;
/*  682:     */   }
/*  683:     */   
/*  684:     */   public CompositeByteBuf getBytes(int index, ByteBuffer dst)
/*  685:     */   {
/*  686: 697 */     this.wrapped.getBytes(index, dst);
/*  687: 698 */     return this;
/*  688:     */   }
/*  689:     */   
/*  690:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  691:     */   {
/*  692: 703 */     this.wrapped.getBytes(index, dst, dstIndex, length);
/*  693: 704 */     return this;
/*  694:     */   }
/*  695:     */   
/*  696:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  697:     */     throws IOException
/*  698:     */   {
/*  699: 709 */     return this.wrapped.getBytes(index, out, length);
/*  700:     */   }
/*  701:     */   
/*  702:     */   public CompositeByteBuf getBytes(int index, OutputStream out, int length)
/*  703:     */     throws IOException
/*  704:     */   {
/*  705: 714 */     this.wrapped.getBytes(index, out, length);
/*  706: 715 */     return this;
/*  707:     */   }
/*  708:     */   
/*  709:     */   public CompositeByteBuf setByte(int index, int value)
/*  710:     */   {
/*  711: 720 */     this.wrapped.setByte(index, value);
/*  712: 721 */     return this;
/*  713:     */   }
/*  714:     */   
/*  715:     */   protected final void _setByte(int index, int value)
/*  716:     */   {
/*  717: 726 */     this.wrapped._setByte(index, value);
/*  718:     */   }
/*  719:     */   
/*  720:     */   public CompositeByteBuf setShort(int index, int value)
/*  721:     */   {
/*  722: 731 */     this.wrapped.setShort(index, value);
/*  723: 732 */     return this;
/*  724:     */   }
/*  725:     */   
/*  726:     */   protected final void _setShort(int index, int value)
/*  727:     */   {
/*  728: 737 */     this.wrapped._setShort(index, value);
/*  729:     */   }
/*  730:     */   
/*  731:     */   protected final void _setShortLE(int index, int value)
/*  732:     */   {
/*  733: 742 */     this.wrapped._setShortLE(index, value);
/*  734:     */   }
/*  735:     */   
/*  736:     */   public CompositeByteBuf setMedium(int index, int value)
/*  737:     */   {
/*  738: 747 */     this.wrapped.setMedium(index, value);
/*  739: 748 */     return this;
/*  740:     */   }
/*  741:     */   
/*  742:     */   protected final void _setMedium(int index, int value)
/*  743:     */   {
/*  744: 753 */     this.wrapped._setMedium(index, value);
/*  745:     */   }
/*  746:     */   
/*  747:     */   protected final void _setMediumLE(int index, int value)
/*  748:     */   {
/*  749: 758 */     this.wrapped._setMediumLE(index, value);
/*  750:     */   }
/*  751:     */   
/*  752:     */   public CompositeByteBuf setInt(int index, int value)
/*  753:     */   {
/*  754: 763 */     this.wrapped.setInt(index, value);
/*  755: 764 */     return this;
/*  756:     */   }
/*  757:     */   
/*  758:     */   protected final void _setInt(int index, int value)
/*  759:     */   {
/*  760: 769 */     this.wrapped._setInt(index, value);
/*  761:     */   }
/*  762:     */   
/*  763:     */   protected final void _setIntLE(int index, int value)
/*  764:     */   {
/*  765: 774 */     this.wrapped._setIntLE(index, value);
/*  766:     */   }
/*  767:     */   
/*  768:     */   public CompositeByteBuf setLong(int index, long value)
/*  769:     */   {
/*  770: 779 */     this.wrapped.setLong(index, value);
/*  771: 780 */     return this;
/*  772:     */   }
/*  773:     */   
/*  774:     */   protected final void _setLong(int index, long value)
/*  775:     */   {
/*  776: 785 */     this.wrapped._setLong(index, value);
/*  777:     */   }
/*  778:     */   
/*  779:     */   protected final void _setLongLE(int index, long value)
/*  780:     */   {
/*  781: 790 */     this.wrapped._setLongLE(index, value);
/*  782:     */   }
/*  783:     */   
/*  784:     */   public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  785:     */   {
/*  786: 795 */     this.wrapped.setBytes(index, src, srcIndex, length);
/*  787: 796 */     return this;
/*  788:     */   }
/*  789:     */   
/*  790:     */   public CompositeByteBuf setBytes(int index, ByteBuffer src)
/*  791:     */   {
/*  792: 801 */     this.wrapped.setBytes(index, src);
/*  793: 802 */     return this;
/*  794:     */   }
/*  795:     */   
/*  796:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  797:     */   {
/*  798: 807 */     this.wrapped.setBytes(index, src, srcIndex, length);
/*  799: 808 */     return this;
/*  800:     */   }
/*  801:     */   
/*  802:     */   public int setBytes(int index, InputStream in, int length)
/*  803:     */     throws IOException
/*  804:     */   {
/*  805: 813 */     return this.wrapped.setBytes(index, in, length);
/*  806:     */   }
/*  807:     */   
/*  808:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  809:     */     throws IOException
/*  810:     */   {
/*  811: 818 */     return this.wrapped.setBytes(index, in, length);
/*  812:     */   }
/*  813:     */   
/*  814:     */   public ByteBuf copy(int index, int length)
/*  815:     */   {
/*  816: 823 */     return this.wrapped.copy(index, length);
/*  817:     */   }
/*  818:     */   
/*  819:     */   public final ByteBuf component(int cIndex)
/*  820:     */   {
/*  821: 828 */     return this.wrapped.component(cIndex);
/*  822:     */   }
/*  823:     */   
/*  824:     */   public final ByteBuf componentAtOffset(int offset)
/*  825:     */   {
/*  826: 833 */     return this.wrapped.componentAtOffset(offset);
/*  827:     */   }
/*  828:     */   
/*  829:     */   public final ByteBuf internalComponent(int cIndex)
/*  830:     */   {
/*  831: 838 */     return this.wrapped.internalComponent(cIndex);
/*  832:     */   }
/*  833:     */   
/*  834:     */   public final ByteBuf internalComponentAtOffset(int offset)
/*  835:     */   {
/*  836: 843 */     return this.wrapped.internalComponentAtOffset(offset);
/*  837:     */   }
/*  838:     */   
/*  839:     */   public int nioBufferCount()
/*  840:     */   {
/*  841: 848 */     return this.wrapped.nioBufferCount();
/*  842:     */   }
/*  843:     */   
/*  844:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  845:     */   {
/*  846: 853 */     return this.wrapped.internalNioBuffer(index, length);
/*  847:     */   }
/*  848:     */   
/*  849:     */   public ByteBuffer nioBuffer(int index, int length)
/*  850:     */   {
/*  851: 858 */     return this.wrapped.nioBuffer(index, length);
/*  852:     */   }
/*  853:     */   
/*  854:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  855:     */   {
/*  856: 863 */     return this.wrapped.nioBuffers(index, length);
/*  857:     */   }
/*  858:     */   
/*  859:     */   public CompositeByteBuf consolidate()
/*  860:     */   {
/*  861: 868 */     this.wrapped.consolidate();
/*  862: 869 */     return this;
/*  863:     */   }
/*  864:     */   
/*  865:     */   public CompositeByteBuf consolidate(int cIndex, int numComponents)
/*  866:     */   {
/*  867: 874 */     this.wrapped.consolidate(cIndex, numComponents);
/*  868: 875 */     return this;
/*  869:     */   }
/*  870:     */   
/*  871:     */   public CompositeByteBuf discardReadComponents()
/*  872:     */   {
/*  873: 880 */     this.wrapped.discardReadComponents();
/*  874: 881 */     return this;
/*  875:     */   }
/*  876:     */   
/*  877:     */   public CompositeByteBuf discardReadBytes()
/*  878:     */   {
/*  879: 886 */     this.wrapped.discardReadBytes();
/*  880: 887 */     return this;
/*  881:     */   }
/*  882:     */   
/*  883:     */   public final String toString()
/*  884:     */   {
/*  885: 892 */     return this.wrapped.toString();
/*  886:     */   }
/*  887:     */   
/*  888:     */   public final CompositeByteBuf readerIndex(int readerIndex)
/*  889:     */   {
/*  890: 897 */     this.wrapped.readerIndex(readerIndex);
/*  891: 898 */     return this;
/*  892:     */   }
/*  893:     */   
/*  894:     */   public final CompositeByteBuf writerIndex(int writerIndex)
/*  895:     */   {
/*  896: 903 */     this.wrapped.writerIndex(writerIndex);
/*  897: 904 */     return this;
/*  898:     */   }
/*  899:     */   
/*  900:     */   public final CompositeByteBuf setIndex(int readerIndex, int writerIndex)
/*  901:     */   {
/*  902: 909 */     this.wrapped.setIndex(readerIndex, writerIndex);
/*  903: 910 */     return this;
/*  904:     */   }
/*  905:     */   
/*  906:     */   public final CompositeByteBuf clear()
/*  907:     */   {
/*  908: 915 */     this.wrapped.clear();
/*  909: 916 */     return this;
/*  910:     */   }
/*  911:     */   
/*  912:     */   public final CompositeByteBuf markReaderIndex()
/*  913:     */   {
/*  914: 921 */     this.wrapped.markReaderIndex();
/*  915: 922 */     return this;
/*  916:     */   }
/*  917:     */   
/*  918:     */   public final CompositeByteBuf resetReaderIndex()
/*  919:     */   {
/*  920: 927 */     this.wrapped.resetReaderIndex();
/*  921: 928 */     return this;
/*  922:     */   }
/*  923:     */   
/*  924:     */   public final CompositeByteBuf markWriterIndex()
/*  925:     */   {
/*  926: 933 */     this.wrapped.markWriterIndex();
/*  927: 934 */     return this;
/*  928:     */   }
/*  929:     */   
/*  930:     */   public final CompositeByteBuf resetWriterIndex()
/*  931:     */   {
/*  932: 939 */     this.wrapped.resetWriterIndex();
/*  933: 940 */     return this;
/*  934:     */   }
/*  935:     */   
/*  936:     */   public CompositeByteBuf ensureWritable(int minWritableBytes)
/*  937:     */   {
/*  938: 945 */     this.wrapped.ensureWritable(minWritableBytes);
/*  939: 946 */     return this;
/*  940:     */   }
/*  941:     */   
/*  942:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst)
/*  943:     */   {
/*  944: 951 */     this.wrapped.getBytes(index, dst);
/*  945: 952 */     return this;
/*  946:     */   }
/*  947:     */   
/*  948:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int length)
/*  949:     */   {
/*  950: 957 */     this.wrapped.getBytes(index, dst, length);
/*  951: 958 */     return this;
/*  952:     */   }
/*  953:     */   
/*  954:     */   public CompositeByteBuf getBytes(int index, byte[] dst)
/*  955:     */   {
/*  956: 963 */     this.wrapped.getBytes(index, dst);
/*  957: 964 */     return this;
/*  958:     */   }
/*  959:     */   
/*  960:     */   public CompositeByteBuf setBoolean(int index, boolean value)
/*  961:     */   {
/*  962: 969 */     this.wrapped.setBoolean(index, value);
/*  963: 970 */     return this;
/*  964:     */   }
/*  965:     */   
/*  966:     */   public CompositeByteBuf setChar(int index, int value)
/*  967:     */   {
/*  968: 975 */     this.wrapped.setChar(index, value);
/*  969: 976 */     return this;
/*  970:     */   }
/*  971:     */   
/*  972:     */   public CompositeByteBuf setFloat(int index, float value)
/*  973:     */   {
/*  974: 981 */     this.wrapped.setFloat(index, value);
/*  975: 982 */     return this;
/*  976:     */   }
/*  977:     */   
/*  978:     */   public CompositeByteBuf setDouble(int index, double value)
/*  979:     */   {
/*  980: 987 */     this.wrapped.setDouble(index, value);
/*  981: 988 */     return this;
/*  982:     */   }
/*  983:     */   
/*  984:     */   public CompositeByteBuf setBytes(int index, ByteBuf src)
/*  985:     */   {
/*  986: 993 */     this.wrapped.setBytes(index, src);
/*  987: 994 */     return this;
/*  988:     */   }
/*  989:     */   
/*  990:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int length)
/*  991:     */   {
/*  992: 999 */     this.wrapped.setBytes(index, src, length);
/*  993:1000 */     return this;
/*  994:     */   }
/*  995:     */   
/*  996:     */   public CompositeByteBuf setBytes(int index, byte[] src)
/*  997:     */   {
/*  998:1005 */     this.wrapped.setBytes(index, src);
/*  999:1006 */     return this;
/* 1000:     */   }
/* 1001:     */   
/* 1002:     */   public CompositeByteBuf setZero(int index, int length)
/* 1003:     */   {
/* 1004:1011 */     this.wrapped.setZero(index, length);
/* 1005:1012 */     return this;
/* 1006:     */   }
/* 1007:     */   
/* 1008:     */   public CompositeByteBuf readBytes(ByteBuf dst)
/* 1009:     */   {
/* 1010:1017 */     this.wrapped.readBytes(dst);
/* 1011:1018 */     return this;
/* 1012:     */   }
/* 1013:     */   
/* 1014:     */   public CompositeByteBuf readBytes(ByteBuf dst, int length)
/* 1015:     */   {
/* 1016:1023 */     this.wrapped.readBytes(dst, length);
/* 1017:1024 */     return this;
/* 1018:     */   }
/* 1019:     */   
/* 1020:     */   public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 1021:     */   {
/* 1022:1029 */     this.wrapped.readBytes(dst, dstIndex, length);
/* 1023:1030 */     return this;
/* 1024:     */   }
/* 1025:     */   
/* 1026:     */   public CompositeByteBuf readBytes(byte[] dst)
/* 1027:     */   {
/* 1028:1035 */     this.wrapped.readBytes(dst);
/* 1029:1036 */     return this;
/* 1030:     */   }
/* 1031:     */   
/* 1032:     */   public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 1033:     */   {
/* 1034:1041 */     this.wrapped.readBytes(dst, dstIndex, length);
/* 1035:1042 */     return this;
/* 1036:     */   }
/* 1037:     */   
/* 1038:     */   public CompositeByteBuf readBytes(ByteBuffer dst)
/* 1039:     */   {
/* 1040:1047 */     this.wrapped.readBytes(dst);
/* 1041:1048 */     return this;
/* 1042:     */   }
/* 1043:     */   
/* 1044:     */   public CompositeByteBuf readBytes(OutputStream out, int length)
/* 1045:     */     throws IOException
/* 1046:     */   {
/* 1047:1053 */     this.wrapped.readBytes(out, length);
/* 1048:1054 */     return this;
/* 1049:     */   }
/* 1050:     */   
/* 1051:     */   public int getBytes(int index, FileChannel out, long position, int length)
/* 1052:     */     throws IOException
/* 1053:     */   {
/* 1054:1059 */     return this.wrapped.getBytes(index, out, position, length);
/* 1055:     */   }
/* 1056:     */   
/* 1057:     */   public int setBytes(int index, FileChannel in, long position, int length)
/* 1058:     */     throws IOException
/* 1059:     */   {
/* 1060:1064 */     return this.wrapped.setBytes(index, in, position, length);
/* 1061:     */   }
/* 1062:     */   
/* 1063:     */   public boolean isReadOnly()
/* 1064:     */   {
/* 1065:1069 */     return this.wrapped.isReadOnly();
/* 1066:     */   }
/* 1067:     */   
/* 1068:     */   public ByteBuf asReadOnly()
/* 1069:     */   {
/* 1070:1074 */     return this.wrapped.asReadOnly();
/* 1071:     */   }
/* 1072:     */   
/* 1073:     */   protected SwappedByteBuf newSwappedByteBuf()
/* 1074:     */   {
/* 1075:1079 */     return this.wrapped.newSwappedByteBuf();
/* 1076:     */   }
/* 1077:     */   
/* 1078:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/* 1079:     */   {
/* 1080:1084 */     return this.wrapped.getCharSequence(index, length, charset);
/* 1081:     */   }
/* 1082:     */   
/* 1083:     */   public CharSequence readCharSequence(int length, Charset charset)
/* 1084:     */   {
/* 1085:1089 */     return this.wrapped.readCharSequence(length, charset);
/* 1086:     */   }
/* 1087:     */   
/* 1088:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/* 1089:     */   {
/* 1090:1094 */     return this.wrapped.setCharSequence(index, sequence, charset);
/* 1091:     */   }
/* 1092:     */   
/* 1093:     */   public int readBytes(FileChannel out, long position, int length)
/* 1094:     */     throws IOException
/* 1095:     */   {
/* 1096:1099 */     return this.wrapped.readBytes(out, position, length);
/* 1097:     */   }
/* 1098:     */   
/* 1099:     */   public int writeBytes(FileChannel in, long position, int length)
/* 1100:     */     throws IOException
/* 1101:     */   {
/* 1102:1104 */     return this.wrapped.writeBytes(in, position, length);
/* 1103:     */   }
/* 1104:     */   
/* 1105:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/* 1106:     */   {
/* 1107:1109 */     return this.wrapped.writeCharSequence(sequence, charset);
/* 1108:     */   }
/* 1109:     */   
/* 1110:     */   public CompositeByteBuf skipBytes(int length)
/* 1111:     */   {
/* 1112:1114 */     this.wrapped.skipBytes(length);
/* 1113:1115 */     return this;
/* 1114:     */   }
/* 1115:     */   
/* 1116:     */   public CompositeByteBuf writeBoolean(boolean value)
/* 1117:     */   {
/* 1118:1120 */     this.wrapped.writeBoolean(value);
/* 1119:1121 */     return this;
/* 1120:     */   }
/* 1121:     */   
/* 1122:     */   public CompositeByteBuf writeByte(int value)
/* 1123:     */   {
/* 1124:1126 */     this.wrapped.writeByte(value);
/* 1125:1127 */     return this;
/* 1126:     */   }
/* 1127:     */   
/* 1128:     */   public CompositeByteBuf writeShort(int value)
/* 1129:     */   {
/* 1130:1132 */     this.wrapped.writeShort(value);
/* 1131:1133 */     return this;
/* 1132:     */   }
/* 1133:     */   
/* 1134:     */   public CompositeByteBuf writeMedium(int value)
/* 1135:     */   {
/* 1136:1138 */     this.wrapped.writeMedium(value);
/* 1137:1139 */     return this;
/* 1138:     */   }
/* 1139:     */   
/* 1140:     */   public CompositeByteBuf writeInt(int value)
/* 1141:     */   {
/* 1142:1144 */     this.wrapped.writeInt(value);
/* 1143:1145 */     return this;
/* 1144:     */   }
/* 1145:     */   
/* 1146:     */   public CompositeByteBuf writeLong(long value)
/* 1147:     */   {
/* 1148:1150 */     this.wrapped.writeLong(value);
/* 1149:1151 */     return this;
/* 1150:     */   }
/* 1151:     */   
/* 1152:     */   public CompositeByteBuf writeChar(int value)
/* 1153:     */   {
/* 1154:1156 */     this.wrapped.writeChar(value);
/* 1155:1157 */     return this;
/* 1156:     */   }
/* 1157:     */   
/* 1158:     */   public CompositeByteBuf writeFloat(float value)
/* 1159:     */   {
/* 1160:1162 */     this.wrapped.writeFloat(value);
/* 1161:1163 */     return this;
/* 1162:     */   }
/* 1163:     */   
/* 1164:     */   public CompositeByteBuf writeDouble(double value)
/* 1165:     */   {
/* 1166:1168 */     this.wrapped.writeDouble(value);
/* 1167:1169 */     return this;
/* 1168:     */   }
/* 1169:     */   
/* 1170:     */   public CompositeByteBuf writeBytes(ByteBuf src)
/* 1171:     */   {
/* 1172:1174 */     this.wrapped.writeBytes(src);
/* 1173:1175 */     return this;
/* 1174:     */   }
/* 1175:     */   
/* 1176:     */   public CompositeByteBuf writeBytes(ByteBuf src, int length)
/* 1177:     */   {
/* 1178:1180 */     this.wrapped.writeBytes(src, length);
/* 1179:1181 */     return this;
/* 1180:     */   }
/* 1181:     */   
/* 1182:     */   public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 1183:     */   {
/* 1184:1186 */     this.wrapped.writeBytes(src, srcIndex, length);
/* 1185:1187 */     return this;
/* 1186:     */   }
/* 1187:     */   
/* 1188:     */   public CompositeByteBuf writeBytes(byte[] src)
/* 1189:     */   {
/* 1190:1192 */     this.wrapped.writeBytes(src);
/* 1191:1193 */     return this;
/* 1192:     */   }
/* 1193:     */   
/* 1194:     */   public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 1195:     */   {
/* 1196:1198 */     this.wrapped.writeBytes(src, srcIndex, length);
/* 1197:1199 */     return this;
/* 1198:     */   }
/* 1199:     */   
/* 1200:     */   public CompositeByteBuf writeBytes(ByteBuffer src)
/* 1201:     */   {
/* 1202:1204 */     this.wrapped.writeBytes(src);
/* 1203:1205 */     return this;
/* 1204:     */   }
/* 1205:     */   
/* 1206:     */   public CompositeByteBuf writeZero(int length)
/* 1207:     */   {
/* 1208:1210 */     this.wrapped.writeZero(length);
/* 1209:1211 */     return this;
/* 1210:     */   }
/* 1211:     */   
/* 1212:     */   public CompositeByteBuf retain(int increment)
/* 1213:     */   {
/* 1214:1216 */     this.wrapped.retain(increment);
/* 1215:1217 */     return this;
/* 1216:     */   }
/* 1217:     */   
/* 1218:     */   public CompositeByteBuf retain()
/* 1219:     */   {
/* 1220:1222 */     this.wrapped.retain();
/* 1221:1223 */     return this;
/* 1222:     */   }
/* 1223:     */   
/* 1224:     */   public CompositeByteBuf touch()
/* 1225:     */   {
/* 1226:1228 */     this.wrapped.touch();
/* 1227:1229 */     return this;
/* 1228:     */   }
/* 1229:     */   
/* 1230:     */   public CompositeByteBuf touch(Object hint)
/* 1231:     */   {
/* 1232:1234 */     this.wrapped.touch(hint);
/* 1233:1235 */     return this;
/* 1234:     */   }
/* 1235:     */   
/* 1236:     */   public ByteBuffer[] nioBuffers()
/* 1237:     */   {
/* 1238:1240 */     return this.wrapped.nioBuffers();
/* 1239:     */   }
/* 1240:     */   
/* 1241:     */   public CompositeByteBuf discardSomeReadBytes()
/* 1242:     */   {
/* 1243:1245 */     this.wrapped.discardSomeReadBytes();
/* 1244:1246 */     return this;
/* 1245:     */   }
/* 1246:     */   
/* 1247:     */   public final void deallocate()
/* 1248:     */   {
/* 1249:1251 */     this.wrapped.deallocate();
/* 1250:     */   }
/* 1251:     */   
/* 1252:     */   public final ByteBuf unwrap()
/* 1253:     */   {
/* 1254:1256 */     return this.wrapped;
/* 1255:     */   }
/* 1256:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.WrappedCompositeByteBuf
 * JD-Core Version:    0.7.0.1
 */