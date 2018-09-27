/*    1:     */ package io.netty.handler.codec;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.buffer.SwappedByteBuf;
/*    6:     */ import io.netty.buffer.Unpooled;
/*    7:     */ import io.netty.util.ByteProcessor;
/*    8:     */ import io.netty.util.Signal;
/*    9:     */ import io.netty.util.internal.StringUtil;
/*   10:     */ import java.io.InputStream;
/*   11:     */ import java.io.OutputStream;
/*   12:     */ import java.nio.ByteBuffer;
/*   13:     */ import java.nio.ByteOrder;
/*   14:     */ import java.nio.channels.FileChannel;
/*   15:     */ import java.nio.channels.GatheringByteChannel;
/*   16:     */ import java.nio.channels.ScatteringByteChannel;
/*   17:     */ import java.nio.charset.Charset;
/*   18:     */ 
/*   19:     */ final class ReplayingDecoderByteBuf
/*   20:     */   extends ByteBuf
/*   21:     */ {
/*   22:  40 */   private static final Signal REPLAY = ReplayingDecoder.REPLAY;
/*   23:     */   private ByteBuf buffer;
/*   24:     */   private boolean terminated;
/*   25:     */   private SwappedByteBuf swapped;
/*   26:  46 */   static final ReplayingDecoderByteBuf EMPTY_BUFFER = new ReplayingDecoderByteBuf(Unpooled.EMPTY_BUFFER);
/*   27:     */   
/*   28:     */   static
/*   29:     */   {
/*   30:  49 */     EMPTY_BUFFER.terminate();
/*   31:     */   }
/*   32:     */   
/*   33:     */   ReplayingDecoderByteBuf(ByteBuf buffer)
/*   34:     */   {
/*   35:  55 */     setCumulation(buffer);
/*   36:     */   }
/*   37:     */   
/*   38:     */   void setCumulation(ByteBuf buffer)
/*   39:     */   {
/*   40:  59 */     this.buffer = buffer;
/*   41:     */   }
/*   42:     */   
/*   43:     */   void terminate()
/*   44:     */   {
/*   45:  63 */     this.terminated = true;
/*   46:     */   }
/*   47:     */   
/*   48:     */   public int capacity()
/*   49:     */   {
/*   50:  68 */     if (this.terminated) {
/*   51:  69 */       return this.buffer.capacity();
/*   52:     */     }
/*   53:  71 */     return 2147483647;
/*   54:     */   }
/*   55:     */   
/*   56:     */   public ByteBuf capacity(int newCapacity)
/*   57:     */   {
/*   58:  77 */     throw reject();
/*   59:     */   }
/*   60:     */   
/*   61:     */   public int maxCapacity()
/*   62:     */   {
/*   63:  82 */     return capacity();
/*   64:     */   }
/*   65:     */   
/*   66:     */   public ByteBufAllocator alloc()
/*   67:     */   {
/*   68:  87 */     return this.buffer.alloc();
/*   69:     */   }
/*   70:     */   
/*   71:     */   public boolean isReadOnly()
/*   72:     */   {
/*   73:  92 */     return false;
/*   74:     */   }
/*   75:     */   
/*   76:     */   public ByteBuf asReadOnly()
/*   77:     */   {
/*   78:  98 */     return Unpooled.unmodifiableBuffer(this);
/*   79:     */   }
/*   80:     */   
/*   81:     */   public boolean isDirect()
/*   82:     */   {
/*   83: 103 */     return this.buffer.isDirect();
/*   84:     */   }
/*   85:     */   
/*   86:     */   public boolean hasArray()
/*   87:     */   {
/*   88: 108 */     return false;
/*   89:     */   }
/*   90:     */   
/*   91:     */   public byte[] array()
/*   92:     */   {
/*   93: 113 */     throw new UnsupportedOperationException();
/*   94:     */   }
/*   95:     */   
/*   96:     */   public int arrayOffset()
/*   97:     */   {
/*   98: 118 */     throw new UnsupportedOperationException();
/*   99:     */   }
/*  100:     */   
/*  101:     */   public boolean hasMemoryAddress()
/*  102:     */   {
/*  103: 123 */     return false;
/*  104:     */   }
/*  105:     */   
/*  106:     */   public long memoryAddress()
/*  107:     */   {
/*  108: 128 */     throw new UnsupportedOperationException();
/*  109:     */   }
/*  110:     */   
/*  111:     */   public ByteBuf clear()
/*  112:     */   {
/*  113: 133 */     throw reject();
/*  114:     */   }
/*  115:     */   
/*  116:     */   public boolean equals(Object obj)
/*  117:     */   {
/*  118: 138 */     return this == obj;
/*  119:     */   }
/*  120:     */   
/*  121:     */   public int compareTo(ByteBuf buffer)
/*  122:     */   {
/*  123: 143 */     throw reject();
/*  124:     */   }
/*  125:     */   
/*  126:     */   public ByteBuf copy()
/*  127:     */   {
/*  128: 148 */     throw reject();
/*  129:     */   }
/*  130:     */   
/*  131:     */   public ByteBuf copy(int index, int length)
/*  132:     */   {
/*  133: 153 */     checkIndex(index, length);
/*  134: 154 */     return this.buffer.copy(index, length);
/*  135:     */   }
/*  136:     */   
/*  137:     */   public ByteBuf discardReadBytes()
/*  138:     */   {
/*  139: 159 */     throw reject();
/*  140:     */   }
/*  141:     */   
/*  142:     */   public ByteBuf ensureWritable(int writableBytes)
/*  143:     */   {
/*  144: 164 */     throw reject();
/*  145:     */   }
/*  146:     */   
/*  147:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  148:     */   {
/*  149: 169 */     throw reject();
/*  150:     */   }
/*  151:     */   
/*  152:     */   public ByteBuf duplicate()
/*  153:     */   {
/*  154: 174 */     throw reject();
/*  155:     */   }
/*  156:     */   
/*  157:     */   public ByteBuf retainedDuplicate()
/*  158:     */   {
/*  159: 179 */     throw reject();
/*  160:     */   }
/*  161:     */   
/*  162:     */   public boolean getBoolean(int index)
/*  163:     */   {
/*  164: 184 */     checkIndex(index, 1);
/*  165: 185 */     return this.buffer.getBoolean(index);
/*  166:     */   }
/*  167:     */   
/*  168:     */   public byte getByte(int index)
/*  169:     */   {
/*  170: 190 */     checkIndex(index, 1);
/*  171: 191 */     return this.buffer.getByte(index);
/*  172:     */   }
/*  173:     */   
/*  174:     */   public short getUnsignedByte(int index)
/*  175:     */   {
/*  176: 196 */     checkIndex(index, 1);
/*  177: 197 */     return this.buffer.getUnsignedByte(index);
/*  178:     */   }
/*  179:     */   
/*  180:     */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  181:     */   {
/*  182: 202 */     checkIndex(index, length);
/*  183: 203 */     this.buffer.getBytes(index, dst, dstIndex, length);
/*  184: 204 */     return this;
/*  185:     */   }
/*  186:     */   
/*  187:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  188:     */   {
/*  189: 209 */     checkIndex(index, dst.length);
/*  190: 210 */     this.buffer.getBytes(index, dst);
/*  191: 211 */     return this;
/*  192:     */   }
/*  193:     */   
/*  194:     */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  195:     */   {
/*  196: 216 */     throw reject();
/*  197:     */   }
/*  198:     */   
/*  199:     */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  200:     */   {
/*  201: 221 */     checkIndex(index, length);
/*  202: 222 */     this.buffer.getBytes(index, dst, dstIndex, length);
/*  203: 223 */     return this;
/*  204:     */   }
/*  205:     */   
/*  206:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  207:     */   {
/*  208: 228 */     throw reject();
/*  209:     */   }
/*  210:     */   
/*  211:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  212:     */   {
/*  213: 233 */     throw reject();
/*  214:     */   }
/*  215:     */   
/*  216:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  217:     */   {
/*  218: 238 */     throw reject();
/*  219:     */   }
/*  220:     */   
/*  221:     */   public int getBytes(int index, FileChannel out, long position, int length)
/*  222:     */   {
/*  223: 243 */     throw reject();
/*  224:     */   }
/*  225:     */   
/*  226:     */   public ByteBuf getBytes(int index, OutputStream out, int length)
/*  227:     */   {
/*  228: 248 */     throw reject();
/*  229:     */   }
/*  230:     */   
/*  231:     */   public int getInt(int index)
/*  232:     */   {
/*  233: 253 */     checkIndex(index, 4);
/*  234: 254 */     return this.buffer.getInt(index);
/*  235:     */   }
/*  236:     */   
/*  237:     */   public int getIntLE(int index)
/*  238:     */   {
/*  239: 259 */     checkIndex(index, 4);
/*  240: 260 */     return this.buffer.getIntLE(index);
/*  241:     */   }
/*  242:     */   
/*  243:     */   public long getUnsignedInt(int index)
/*  244:     */   {
/*  245: 265 */     checkIndex(index, 4);
/*  246: 266 */     return this.buffer.getUnsignedInt(index);
/*  247:     */   }
/*  248:     */   
/*  249:     */   public long getUnsignedIntLE(int index)
/*  250:     */   {
/*  251: 271 */     checkIndex(index, 4);
/*  252: 272 */     return this.buffer.getUnsignedIntLE(index);
/*  253:     */   }
/*  254:     */   
/*  255:     */   public long getLong(int index)
/*  256:     */   {
/*  257: 277 */     checkIndex(index, 8);
/*  258: 278 */     return this.buffer.getLong(index);
/*  259:     */   }
/*  260:     */   
/*  261:     */   public long getLongLE(int index)
/*  262:     */   {
/*  263: 283 */     checkIndex(index, 8);
/*  264: 284 */     return this.buffer.getLongLE(index);
/*  265:     */   }
/*  266:     */   
/*  267:     */   public int getMedium(int index)
/*  268:     */   {
/*  269: 289 */     checkIndex(index, 3);
/*  270: 290 */     return this.buffer.getMedium(index);
/*  271:     */   }
/*  272:     */   
/*  273:     */   public int getMediumLE(int index)
/*  274:     */   {
/*  275: 295 */     checkIndex(index, 3);
/*  276: 296 */     return this.buffer.getMediumLE(index);
/*  277:     */   }
/*  278:     */   
/*  279:     */   public int getUnsignedMedium(int index)
/*  280:     */   {
/*  281: 301 */     checkIndex(index, 3);
/*  282: 302 */     return this.buffer.getUnsignedMedium(index);
/*  283:     */   }
/*  284:     */   
/*  285:     */   public int getUnsignedMediumLE(int index)
/*  286:     */   {
/*  287: 307 */     checkIndex(index, 3);
/*  288: 308 */     return this.buffer.getUnsignedMediumLE(index);
/*  289:     */   }
/*  290:     */   
/*  291:     */   public short getShort(int index)
/*  292:     */   {
/*  293: 313 */     checkIndex(index, 2);
/*  294: 314 */     return this.buffer.getShort(index);
/*  295:     */   }
/*  296:     */   
/*  297:     */   public short getShortLE(int index)
/*  298:     */   {
/*  299: 319 */     checkIndex(index, 2);
/*  300: 320 */     return this.buffer.getShortLE(index);
/*  301:     */   }
/*  302:     */   
/*  303:     */   public int getUnsignedShort(int index)
/*  304:     */   {
/*  305: 325 */     checkIndex(index, 2);
/*  306: 326 */     return this.buffer.getUnsignedShort(index);
/*  307:     */   }
/*  308:     */   
/*  309:     */   public int getUnsignedShortLE(int index)
/*  310:     */   {
/*  311: 331 */     checkIndex(index, 2);
/*  312: 332 */     return this.buffer.getUnsignedShortLE(index);
/*  313:     */   }
/*  314:     */   
/*  315:     */   public char getChar(int index)
/*  316:     */   {
/*  317: 337 */     checkIndex(index, 2);
/*  318: 338 */     return this.buffer.getChar(index);
/*  319:     */   }
/*  320:     */   
/*  321:     */   public float getFloat(int index)
/*  322:     */   {
/*  323: 343 */     checkIndex(index, 4);
/*  324: 344 */     return this.buffer.getFloat(index);
/*  325:     */   }
/*  326:     */   
/*  327:     */   public double getDouble(int index)
/*  328:     */   {
/*  329: 349 */     checkIndex(index, 8);
/*  330: 350 */     return this.buffer.getDouble(index);
/*  331:     */   }
/*  332:     */   
/*  333:     */   public CharSequence getCharSequence(int index, int length, Charset charset)
/*  334:     */   {
/*  335: 355 */     checkIndex(index, length);
/*  336: 356 */     return this.buffer.getCharSequence(index, length, charset);
/*  337:     */   }
/*  338:     */   
/*  339:     */   public int hashCode()
/*  340:     */   {
/*  341: 361 */     throw reject();
/*  342:     */   }
/*  343:     */   
/*  344:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  345:     */   {
/*  346: 366 */     if (fromIndex == toIndex) {
/*  347: 367 */       return -1;
/*  348:     */     }
/*  349: 370 */     if (Math.max(fromIndex, toIndex) > this.buffer.writerIndex()) {
/*  350: 371 */       throw REPLAY;
/*  351:     */     }
/*  352: 374 */     return this.buffer.indexOf(fromIndex, toIndex, value);
/*  353:     */   }
/*  354:     */   
/*  355:     */   public int bytesBefore(byte value)
/*  356:     */   {
/*  357: 379 */     int bytes = this.buffer.bytesBefore(value);
/*  358: 380 */     if (bytes < 0) {
/*  359: 381 */       throw REPLAY;
/*  360:     */     }
/*  361: 383 */     return bytes;
/*  362:     */   }
/*  363:     */   
/*  364:     */   public int bytesBefore(int length, byte value)
/*  365:     */   {
/*  366: 388 */     return bytesBefore(this.buffer.readerIndex(), length, value);
/*  367:     */   }
/*  368:     */   
/*  369:     */   public int bytesBefore(int index, int length, byte value)
/*  370:     */   {
/*  371: 393 */     int writerIndex = this.buffer.writerIndex();
/*  372: 394 */     if (index >= writerIndex) {
/*  373: 395 */       throw REPLAY;
/*  374:     */     }
/*  375: 398 */     if (index <= writerIndex - length) {
/*  376: 399 */       return this.buffer.bytesBefore(index, length, value);
/*  377:     */     }
/*  378: 402 */     int res = this.buffer.bytesBefore(index, writerIndex - index, value);
/*  379: 403 */     if (res < 0) {
/*  380: 404 */       throw REPLAY;
/*  381:     */     }
/*  382: 406 */     return res;
/*  383:     */   }
/*  384:     */   
/*  385:     */   public int forEachByte(ByteProcessor processor)
/*  386:     */   {
/*  387: 412 */     int ret = this.buffer.forEachByte(processor);
/*  388: 413 */     if (ret < 0) {
/*  389: 414 */       throw REPLAY;
/*  390:     */     }
/*  391: 416 */     return ret;
/*  392:     */   }
/*  393:     */   
/*  394:     */   public int forEachByte(int index, int length, ByteProcessor processor)
/*  395:     */   {
/*  396: 422 */     int writerIndex = this.buffer.writerIndex();
/*  397: 423 */     if (index >= writerIndex) {
/*  398: 424 */       throw REPLAY;
/*  399:     */     }
/*  400: 427 */     if (index <= writerIndex - length) {
/*  401: 428 */       return this.buffer.forEachByte(index, length, processor);
/*  402:     */     }
/*  403: 431 */     int ret = this.buffer.forEachByte(index, writerIndex - index, processor);
/*  404: 432 */     if (ret < 0) {
/*  405: 433 */       throw REPLAY;
/*  406:     */     }
/*  407: 435 */     return ret;
/*  408:     */   }
/*  409:     */   
/*  410:     */   public int forEachByteDesc(ByteProcessor processor)
/*  411:     */   {
/*  412: 441 */     if (this.terminated) {
/*  413: 442 */       return this.buffer.forEachByteDesc(processor);
/*  414:     */     }
/*  415: 444 */     throw reject();
/*  416:     */   }
/*  417:     */   
/*  418:     */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/*  419:     */   {
/*  420: 450 */     if (index + length > this.buffer.writerIndex()) {
/*  421: 451 */       throw REPLAY;
/*  422:     */     }
/*  423: 454 */     return this.buffer.forEachByteDesc(index, length, processor);
/*  424:     */   }
/*  425:     */   
/*  426:     */   public ByteBuf markReaderIndex()
/*  427:     */   {
/*  428: 459 */     this.buffer.markReaderIndex();
/*  429: 460 */     return this;
/*  430:     */   }
/*  431:     */   
/*  432:     */   public ByteBuf markWriterIndex()
/*  433:     */   {
/*  434: 465 */     throw reject();
/*  435:     */   }
/*  436:     */   
/*  437:     */   public ByteOrder order()
/*  438:     */   {
/*  439: 470 */     return this.buffer.order();
/*  440:     */   }
/*  441:     */   
/*  442:     */   public ByteBuf order(ByteOrder endianness)
/*  443:     */   {
/*  444: 475 */     if (endianness == null) {
/*  445: 476 */       throw new NullPointerException("endianness");
/*  446:     */     }
/*  447: 478 */     if (endianness == order()) {
/*  448: 479 */       return this;
/*  449:     */     }
/*  450: 482 */     SwappedByteBuf swapped = this.swapped;
/*  451: 483 */     if (swapped == null) {
/*  452: 484 */       this.swapped = (swapped = new SwappedByteBuf(this));
/*  453:     */     }
/*  454: 486 */     return swapped;
/*  455:     */   }
/*  456:     */   
/*  457:     */   public boolean isReadable()
/*  458:     */   {
/*  459: 491 */     return this.terminated ? this.buffer.isReadable() : true;
/*  460:     */   }
/*  461:     */   
/*  462:     */   public boolean isReadable(int size)
/*  463:     */   {
/*  464: 496 */     return this.terminated ? this.buffer.isReadable(size) : true;
/*  465:     */   }
/*  466:     */   
/*  467:     */   public int readableBytes()
/*  468:     */   {
/*  469: 501 */     if (this.terminated) {
/*  470: 502 */       return this.buffer.readableBytes();
/*  471:     */     }
/*  472: 504 */     return 2147483647 - this.buffer.readerIndex();
/*  473:     */   }
/*  474:     */   
/*  475:     */   public boolean readBoolean()
/*  476:     */   {
/*  477: 510 */     checkReadableBytes(1);
/*  478: 511 */     return this.buffer.readBoolean();
/*  479:     */   }
/*  480:     */   
/*  481:     */   public byte readByte()
/*  482:     */   {
/*  483: 516 */     checkReadableBytes(1);
/*  484: 517 */     return this.buffer.readByte();
/*  485:     */   }
/*  486:     */   
/*  487:     */   public short readUnsignedByte()
/*  488:     */   {
/*  489: 522 */     checkReadableBytes(1);
/*  490: 523 */     return this.buffer.readUnsignedByte();
/*  491:     */   }
/*  492:     */   
/*  493:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  494:     */   {
/*  495: 528 */     checkReadableBytes(length);
/*  496: 529 */     this.buffer.readBytes(dst, dstIndex, length);
/*  497: 530 */     return this;
/*  498:     */   }
/*  499:     */   
/*  500:     */   public ByteBuf readBytes(byte[] dst)
/*  501:     */   {
/*  502: 535 */     checkReadableBytes(dst.length);
/*  503: 536 */     this.buffer.readBytes(dst);
/*  504: 537 */     return this;
/*  505:     */   }
/*  506:     */   
/*  507:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  508:     */   {
/*  509: 542 */     throw reject();
/*  510:     */   }
/*  511:     */   
/*  512:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  513:     */   {
/*  514: 547 */     checkReadableBytes(length);
/*  515: 548 */     this.buffer.readBytes(dst, dstIndex, length);
/*  516: 549 */     return this;
/*  517:     */   }
/*  518:     */   
/*  519:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  520:     */   {
/*  521: 554 */     throw reject();
/*  522:     */   }
/*  523:     */   
/*  524:     */   public ByteBuf readBytes(ByteBuf dst)
/*  525:     */   {
/*  526: 559 */     checkReadableBytes(dst.writableBytes());
/*  527: 560 */     this.buffer.readBytes(dst);
/*  528: 561 */     return this;
/*  529:     */   }
/*  530:     */   
/*  531:     */   public int readBytes(GatheringByteChannel out, int length)
/*  532:     */   {
/*  533: 566 */     throw reject();
/*  534:     */   }
/*  535:     */   
/*  536:     */   public int readBytes(FileChannel out, long position, int length)
/*  537:     */   {
/*  538: 571 */     throw reject();
/*  539:     */   }
/*  540:     */   
/*  541:     */   public ByteBuf readBytes(int length)
/*  542:     */   {
/*  543: 576 */     checkReadableBytes(length);
/*  544: 577 */     return this.buffer.readBytes(length);
/*  545:     */   }
/*  546:     */   
/*  547:     */   public ByteBuf readSlice(int length)
/*  548:     */   {
/*  549: 582 */     checkReadableBytes(length);
/*  550: 583 */     return this.buffer.readSlice(length);
/*  551:     */   }
/*  552:     */   
/*  553:     */   public ByteBuf readRetainedSlice(int length)
/*  554:     */   {
/*  555: 588 */     checkReadableBytes(length);
/*  556: 589 */     return this.buffer.readRetainedSlice(length);
/*  557:     */   }
/*  558:     */   
/*  559:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  560:     */   {
/*  561: 594 */     throw reject();
/*  562:     */   }
/*  563:     */   
/*  564:     */   public int readerIndex()
/*  565:     */   {
/*  566: 599 */     return this.buffer.readerIndex();
/*  567:     */   }
/*  568:     */   
/*  569:     */   public ByteBuf readerIndex(int readerIndex)
/*  570:     */   {
/*  571: 604 */     this.buffer.readerIndex(readerIndex);
/*  572: 605 */     return this;
/*  573:     */   }
/*  574:     */   
/*  575:     */   public int readInt()
/*  576:     */   {
/*  577: 610 */     checkReadableBytes(4);
/*  578: 611 */     return this.buffer.readInt();
/*  579:     */   }
/*  580:     */   
/*  581:     */   public int readIntLE()
/*  582:     */   {
/*  583: 616 */     checkReadableBytes(4);
/*  584: 617 */     return this.buffer.readIntLE();
/*  585:     */   }
/*  586:     */   
/*  587:     */   public long readUnsignedInt()
/*  588:     */   {
/*  589: 622 */     checkReadableBytes(4);
/*  590: 623 */     return this.buffer.readUnsignedInt();
/*  591:     */   }
/*  592:     */   
/*  593:     */   public long readUnsignedIntLE()
/*  594:     */   {
/*  595: 628 */     checkReadableBytes(4);
/*  596: 629 */     return this.buffer.readUnsignedIntLE();
/*  597:     */   }
/*  598:     */   
/*  599:     */   public long readLong()
/*  600:     */   {
/*  601: 634 */     checkReadableBytes(8);
/*  602: 635 */     return this.buffer.readLong();
/*  603:     */   }
/*  604:     */   
/*  605:     */   public long readLongLE()
/*  606:     */   {
/*  607: 640 */     checkReadableBytes(8);
/*  608: 641 */     return this.buffer.readLongLE();
/*  609:     */   }
/*  610:     */   
/*  611:     */   public int readMedium()
/*  612:     */   {
/*  613: 646 */     checkReadableBytes(3);
/*  614: 647 */     return this.buffer.readMedium();
/*  615:     */   }
/*  616:     */   
/*  617:     */   public int readMediumLE()
/*  618:     */   {
/*  619: 652 */     checkReadableBytes(3);
/*  620: 653 */     return this.buffer.readMediumLE();
/*  621:     */   }
/*  622:     */   
/*  623:     */   public int readUnsignedMedium()
/*  624:     */   {
/*  625: 658 */     checkReadableBytes(3);
/*  626: 659 */     return this.buffer.readUnsignedMedium();
/*  627:     */   }
/*  628:     */   
/*  629:     */   public int readUnsignedMediumLE()
/*  630:     */   {
/*  631: 664 */     checkReadableBytes(3);
/*  632: 665 */     return this.buffer.readUnsignedMediumLE();
/*  633:     */   }
/*  634:     */   
/*  635:     */   public short readShort()
/*  636:     */   {
/*  637: 670 */     checkReadableBytes(2);
/*  638: 671 */     return this.buffer.readShort();
/*  639:     */   }
/*  640:     */   
/*  641:     */   public short readShortLE()
/*  642:     */   {
/*  643: 676 */     checkReadableBytes(2);
/*  644: 677 */     return this.buffer.readShortLE();
/*  645:     */   }
/*  646:     */   
/*  647:     */   public int readUnsignedShort()
/*  648:     */   {
/*  649: 682 */     checkReadableBytes(2);
/*  650: 683 */     return this.buffer.readUnsignedShort();
/*  651:     */   }
/*  652:     */   
/*  653:     */   public int readUnsignedShortLE()
/*  654:     */   {
/*  655: 688 */     checkReadableBytes(2);
/*  656: 689 */     return this.buffer.readUnsignedShortLE();
/*  657:     */   }
/*  658:     */   
/*  659:     */   public char readChar()
/*  660:     */   {
/*  661: 694 */     checkReadableBytes(2);
/*  662: 695 */     return this.buffer.readChar();
/*  663:     */   }
/*  664:     */   
/*  665:     */   public float readFloat()
/*  666:     */   {
/*  667: 700 */     checkReadableBytes(4);
/*  668: 701 */     return this.buffer.readFloat();
/*  669:     */   }
/*  670:     */   
/*  671:     */   public double readDouble()
/*  672:     */   {
/*  673: 706 */     checkReadableBytes(8);
/*  674: 707 */     return this.buffer.readDouble();
/*  675:     */   }
/*  676:     */   
/*  677:     */   public CharSequence readCharSequence(int length, Charset charset)
/*  678:     */   {
/*  679: 712 */     checkReadableBytes(length);
/*  680: 713 */     return this.buffer.readCharSequence(length, charset);
/*  681:     */   }
/*  682:     */   
/*  683:     */   public ByteBuf resetReaderIndex()
/*  684:     */   {
/*  685: 718 */     this.buffer.resetReaderIndex();
/*  686: 719 */     return this;
/*  687:     */   }
/*  688:     */   
/*  689:     */   public ByteBuf resetWriterIndex()
/*  690:     */   {
/*  691: 724 */     throw reject();
/*  692:     */   }
/*  693:     */   
/*  694:     */   public ByteBuf setBoolean(int index, boolean value)
/*  695:     */   {
/*  696: 729 */     throw reject();
/*  697:     */   }
/*  698:     */   
/*  699:     */   public ByteBuf setByte(int index, int value)
/*  700:     */   {
/*  701: 734 */     throw reject();
/*  702:     */   }
/*  703:     */   
/*  704:     */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  705:     */   {
/*  706: 739 */     throw reject();
/*  707:     */   }
/*  708:     */   
/*  709:     */   public ByteBuf setBytes(int index, byte[] src)
/*  710:     */   {
/*  711: 744 */     throw reject();
/*  712:     */   }
/*  713:     */   
/*  714:     */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  715:     */   {
/*  716: 749 */     throw reject();
/*  717:     */   }
/*  718:     */   
/*  719:     */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  720:     */   {
/*  721: 754 */     throw reject();
/*  722:     */   }
/*  723:     */   
/*  724:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  725:     */   {
/*  726: 759 */     throw reject();
/*  727:     */   }
/*  728:     */   
/*  729:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  730:     */   {
/*  731: 764 */     throw reject();
/*  732:     */   }
/*  733:     */   
/*  734:     */   public int setBytes(int index, InputStream in, int length)
/*  735:     */   {
/*  736: 769 */     throw reject();
/*  737:     */   }
/*  738:     */   
/*  739:     */   public ByteBuf setZero(int index, int length)
/*  740:     */   {
/*  741: 774 */     throw reject();
/*  742:     */   }
/*  743:     */   
/*  744:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  745:     */   {
/*  746: 779 */     throw reject();
/*  747:     */   }
/*  748:     */   
/*  749:     */   public int setBytes(int index, FileChannel in, long position, int length)
/*  750:     */   {
/*  751: 784 */     throw reject();
/*  752:     */   }
/*  753:     */   
/*  754:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*  755:     */   {
/*  756: 789 */     throw reject();
/*  757:     */   }
/*  758:     */   
/*  759:     */   public ByteBuf setInt(int index, int value)
/*  760:     */   {
/*  761: 794 */     throw reject();
/*  762:     */   }
/*  763:     */   
/*  764:     */   public ByteBuf setIntLE(int index, int value)
/*  765:     */   {
/*  766: 799 */     throw reject();
/*  767:     */   }
/*  768:     */   
/*  769:     */   public ByteBuf setLong(int index, long value)
/*  770:     */   {
/*  771: 804 */     throw reject();
/*  772:     */   }
/*  773:     */   
/*  774:     */   public ByteBuf setLongLE(int index, long value)
/*  775:     */   {
/*  776: 809 */     throw reject();
/*  777:     */   }
/*  778:     */   
/*  779:     */   public ByteBuf setMedium(int index, int value)
/*  780:     */   {
/*  781: 814 */     throw reject();
/*  782:     */   }
/*  783:     */   
/*  784:     */   public ByteBuf setMediumLE(int index, int value)
/*  785:     */   {
/*  786: 819 */     throw reject();
/*  787:     */   }
/*  788:     */   
/*  789:     */   public ByteBuf setShort(int index, int value)
/*  790:     */   {
/*  791: 824 */     throw reject();
/*  792:     */   }
/*  793:     */   
/*  794:     */   public ByteBuf setShortLE(int index, int value)
/*  795:     */   {
/*  796: 829 */     throw reject();
/*  797:     */   }
/*  798:     */   
/*  799:     */   public ByteBuf setChar(int index, int value)
/*  800:     */   {
/*  801: 834 */     throw reject();
/*  802:     */   }
/*  803:     */   
/*  804:     */   public ByteBuf setFloat(int index, float value)
/*  805:     */   {
/*  806: 839 */     throw reject();
/*  807:     */   }
/*  808:     */   
/*  809:     */   public ByteBuf setDouble(int index, double value)
/*  810:     */   {
/*  811: 844 */     throw reject();
/*  812:     */   }
/*  813:     */   
/*  814:     */   public ByteBuf skipBytes(int length)
/*  815:     */   {
/*  816: 849 */     checkReadableBytes(length);
/*  817: 850 */     this.buffer.skipBytes(length);
/*  818: 851 */     return this;
/*  819:     */   }
/*  820:     */   
/*  821:     */   public ByteBuf slice()
/*  822:     */   {
/*  823: 856 */     throw reject();
/*  824:     */   }
/*  825:     */   
/*  826:     */   public ByteBuf retainedSlice()
/*  827:     */   {
/*  828: 861 */     throw reject();
/*  829:     */   }
/*  830:     */   
/*  831:     */   public ByteBuf slice(int index, int length)
/*  832:     */   {
/*  833: 866 */     checkIndex(index, length);
/*  834: 867 */     return this.buffer.slice(index, length);
/*  835:     */   }
/*  836:     */   
/*  837:     */   public ByteBuf retainedSlice(int index, int length)
/*  838:     */   {
/*  839: 872 */     checkIndex(index, length);
/*  840: 873 */     return this.buffer.slice(index, length);
/*  841:     */   }
/*  842:     */   
/*  843:     */   public int nioBufferCount()
/*  844:     */   {
/*  845: 878 */     return this.buffer.nioBufferCount();
/*  846:     */   }
/*  847:     */   
/*  848:     */   public ByteBuffer nioBuffer()
/*  849:     */   {
/*  850: 883 */     throw reject();
/*  851:     */   }
/*  852:     */   
/*  853:     */   public ByteBuffer nioBuffer(int index, int length)
/*  854:     */   {
/*  855: 888 */     checkIndex(index, length);
/*  856: 889 */     return this.buffer.nioBuffer(index, length);
/*  857:     */   }
/*  858:     */   
/*  859:     */   public ByteBuffer[] nioBuffers()
/*  860:     */   {
/*  861: 894 */     throw reject();
/*  862:     */   }
/*  863:     */   
/*  864:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  865:     */   {
/*  866: 899 */     checkIndex(index, length);
/*  867: 900 */     return this.buffer.nioBuffers(index, length);
/*  868:     */   }
/*  869:     */   
/*  870:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  871:     */   {
/*  872: 905 */     checkIndex(index, length);
/*  873: 906 */     return this.buffer.internalNioBuffer(index, length);
/*  874:     */   }
/*  875:     */   
/*  876:     */   public String toString(int index, int length, Charset charset)
/*  877:     */   {
/*  878: 911 */     checkIndex(index, length);
/*  879: 912 */     return this.buffer.toString(index, length, charset);
/*  880:     */   }
/*  881:     */   
/*  882:     */   public String toString(Charset charsetName)
/*  883:     */   {
/*  884: 917 */     throw reject();
/*  885:     */   }
/*  886:     */   
/*  887:     */   public String toString()
/*  888:     */   {
/*  889: 922 */     return 
/*  890:     */     
/*  891:     */ 
/*  892:     */ 
/*  893:     */ 
/*  894: 927 */       StringUtil.simpleClassName(this) + '(' + "ridx=" + readerIndex() + ", widx=" + writerIndex() + ')';
/*  895:     */   }
/*  896:     */   
/*  897:     */   public boolean isWritable()
/*  898:     */   {
/*  899: 933 */     return false;
/*  900:     */   }
/*  901:     */   
/*  902:     */   public boolean isWritable(int size)
/*  903:     */   {
/*  904: 938 */     return false;
/*  905:     */   }
/*  906:     */   
/*  907:     */   public int writableBytes()
/*  908:     */   {
/*  909: 943 */     return 0;
/*  910:     */   }
/*  911:     */   
/*  912:     */   public int maxWritableBytes()
/*  913:     */   {
/*  914: 948 */     return 0;
/*  915:     */   }
/*  916:     */   
/*  917:     */   public ByteBuf writeBoolean(boolean value)
/*  918:     */   {
/*  919: 953 */     throw reject();
/*  920:     */   }
/*  921:     */   
/*  922:     */   public ByteBuf writeByte(int value)
/*  923:     */   {
/*  924: 958 */     throw reject();
/*  925:     */   }
/*  926:     */   
/*  927:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  928:     */   {
/*  929: 963 */     throw reject();
/*  930:     */   }
/*  931:     */   
/*  932:     */   public ByteBuf writeBytes(byte[] src)
/*  933:     */   {
/*  934: 968 */     throw reject();
/*  935:     */   }
/*  936:     */   
/*  937:     */   public ByteBuf writeBytes(ByteBuffer src)
/*  938:     */   {
/*  939: 973 */     throw reject();
/*  940:     */   }
/*  941:     */   
/*  942:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  943:     */   {
/*  944: 978 */     throw reject();
/*  945:     */   }
/*  946:     */   
/*  947:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/*  948:     */   {
/*  949: 983 */     throw reject();
/*  950:     */   }
/*  951:     */   
/*  952:     */   public ByteBuf writeBytes(ByteBuf src)
/*  953:     */   {
/*  954: 988 */     throw reject();
/*  955:     */   }
/*  956:     */   
/*  957:     */   public int writeBytes(InputStream in, int length)
/*  958:     */   {
/*  959: 993 */     throw reject();
/*  960:     */   }
/*  961:     */   
/*  962:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  963:     */   {
/*  964: 998 */     throw reject();
/*  965:     */   }
/*  966:     */   
/*  967:     */   public int writeBytes(FileChannel in, long position, int length)
/*  968:     */   {
/*  969:1003 */     throw reject();
/*  970:     */   }
/*  971:     */   
/*  972:     */   public ByteBuf writeInt(int value)
/*  973:     */   {
/*  974:1008 */     throw reject();
/*  975:     */   }
/*  976:     */   
/*  977:     */   public ByteBuf writeIntLE(int value)
/*  978:     */   {
/*  979:1013 */     throw reject();
/*  980:     */   }
/*  981:     */   
/*  982:     */   public ByteBuf writeLong(long value)
/*  983:     */   {
/*  984:1018 */     throw reject();
/*  985:     */   }
/*  986:     */   
/*  987:     */   public ByteBuf writeLongLE(long value)
/*  988:     */   {
/*  989:1023 */     throw reject();
/*  990:     */   }
/*  991:     */   
/*  992:     */   public ByteBuf writeMedium(int value)
/*  993:     */   {
/*  994:1028 */     throw reject();
/*  995:     */   }
/*  996:     */   
/*  997:     */   public ByteBuf writeMediumLE(int value)
/*  998:     */   {
/*  999:1033 */     throw reject();
/* 1000:     */   }
/* 1001:     */   
/* 1002:     */   public ByteBuf writeZero(int length)
/* 1003:     */   {
/* 1004:1038 */     throw reject();
/* 1005:     */   }
/* 1006:     */   
/* 1007:     */   public int writerIndex()
/* 1008:     */   {
/* 1009:1043 */     return this.buffer.writerIndex();
/* 1010:     */   }
/* 1011:     */   
/* 1012:     */   public ByteBuf writerIndex(int writerIndex)
/* 1013:     */   {
/* 1014:1048 */     throw reject();
/* 1015:     */   }
/* 1016:     */   
/* 1017:     */   public ByteBuf writeShort(int value)
/* 1018:     */   {
/* 1019:1053 */     throw reject();
/* 1020:     */   }
/* 1021:     */   
/* 1022:     */   public ByteBuf writeShortLE(int value)
/* 1023:     */   {
/* 1024:1058 */     throw reject();
/* 1025:     */   }
/* 1026:     */   
/* 1027:     */   public ByteBuf writeChar(int value)
/* 1028:     */   {
/* 1029:1063 */     throw reject();
/* 1030:     */   }
/* 1031:     */   
/* 1032:     */   public ByteBuf writeFloat(float value)
/* 1033:     */   {
/* 1034:1068 */     throw reject();
/* 1035:     */   }
/* 1036:     */   
/* 1037:     */   public ByteBuf writeDouble(double value)
/* 1038:     */   {
/* 1039:1073 */     throw reject();
/* 1040:     */   }
/* 1041:     */   
/* 1042:     */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/* 1043:     */   {
/* 1044:1078 */     throw reject();
/* 1045:     */   }
/* 1046:     */   
/* 1047:     */   public int writeCharSequence(CharSequence sequence, Charset charset)
/* 1048:     */   {
/* 1049:1083 */     throw reject();
/* 1050:     */   }
/* 1051:     */   
/* 1052:     */   private void checkIndex(int index, int length)
/* 1053:     */   {
/* 1054:1087 */     if (index + length > this.buffer.writerIndex()) {
/* 1055:1088 */       throw REPLAY;
/* 1056:     */     }
/* 1057:     */   }
/* 1058:     */   
/* 1059:     */   private void checkReadableBytes(int readableBytes)
/* 1060:     */   {
/* 1061:1093 */     if (this.buffer.readableBytes() < readableBytes) {
/* 1062:1094 */       throw REPLAY;
/* 1063:     */     }
/* 1064:     */   }
/* 1065:     */   
/* 1066:     */   public ByteBuf discardSomeReadBytes()
/* 1067:     */   {
/* 1068:1100 */     throw reject();
/* 1069:     */   }
/* 1070:     */   
/* 1071:     */   public int refCnt()
/* 1072:     */   {
/* 1073:1105 */     return this.buffer.refCnt();
/* 1074:     */   }
/* 1075:     */   
/* 1076:     */   public ByteBuf retain()
/* 1077:     */   {
/* 1078:1110 */     throw reject();
/* 1079:     */   }
/* 1080:     */   
/* 1081:     */   public ByteBuf retain(int increment)
/* 1082:     */   {
/* 1083:1115 */     throw reject();
/* 1084:     */   }
/* 1085:     */   
/* 1086:     */   public ByteBuf touch()
/* 1087:     */   {
/* 1088:1120 */     this.buffer.touch();
/* 1089:1121 */     return this;
/* 1090:     */   }
/* 1091:     */   
/* 1092:     */   public ByteBuf touch(Object hint)
/* 1093:     */   {
/* 1094:1126 */     this.buffer.touch(hint);
/* 1095:1127 */     return this;
/* 1096:     */   }
/* 1097:     */   
/* 1098:     */   public boolean release()
/* 1099:     */   {
/* 1100:1132 */     throw reject();
/* 1101:     */   }
/* 1102:     */   
/* 1103:     */   public boolean release(int decrement)
/* 1104:     */   {
/* 1105:1137 */     throw reject();
/* 1106:     */   }
/* 1107:     */   
/* 1108:     */   public ByteBuf unwrap()
/* 1109:     */   {
/* 1110:1142 */     throw reject();
/* 1111:     */   }
/* 1112:     */   
/* 1113:     */   private static UnsupportedOperationException reject()
/* 1114:     */   {
/* 1115:1146 */     return new UnsupportedOperationException("not a replayable operation");
/* 1116:     */   }
/* 1117:     */   
/* 1118:     */   ReplayingDecoderByteBuf() {}
/* 1119:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.ReplayingDecoderByteBuf
 * JD-Core Version:    0.7.0.1
 */