/*    1:     */ package io.protostuff;
/*    2:     */ 
/*    3:     */ import java.io.DataInput;
/*    4:     */ import java.io.IOException;
/*    5:     */ import java.io.InputStream;
/*    6:     */ import java.nio.ByteBuffer;
/*    7:     */ import java.util.ArrayList;
/*    8:     */ import java.util.List;
/*    9:     */ 
/*   10:     */ public final class CodedInput
/*   11:     */   implements Input
/*   12:     */ {
/*   13:     */   private final byte[] buffer;
/*   14:     */   private int bufferSize;
/*   15:     */   private int bufferSizeAfterLimit;
/*   16:     */   private int bufferPos;
/*   17:     */   private final InputStream input;
/*   18:     */   private int lastTag;
/*   19:     */   
/*   20:     */   public static CodedInput newInstance(InputStream input)
/*   21:     */   {
/*   22:  80 */     return new CodedInput(input, false);
/*   23:     */   }
/*   24:     */   
/*   25:     */   public static CodedInput newInstance(byte[] buf)
/*   26:     */   {
/*   27:  88 */     return newInstance(buf, 0, buf.length);
/*   28:     */   }
/*   29:     */   
/*   30:     */   public static CodedInput newInstance(byte[] buf, int off, int len)
/*   31:     */   {
/*   32:  97 */     return new CodedInput(buf, off, len, false);
/*   33:     */   }
/*   34:     */   
/*   35:     */   public int readTag()
/*   36:     */     throws IOException
/*   37:     */   {
/*   38: 108 */     if (isAtEnd())
/*   39:     */     {
/*   40: 110 */       this.lastTag = 0;
/*   41: 111 */       return 0;
/*   42:     */     }
/*   43: 114 */     int tag = readRawVarint32();
/*   44: 115 */     if (tag >>> 3 == 0) {
/*   45: 118 */       throw ProtobufException.invalidTag();
/*   46:     */     }
/*   47: 120 */     this.lastTag = tag;
/*   48: 121 */     return tag;
/*   49:     */   }
/*   50:     */   
/*   51:     */   public void checkLastTagWas(int value)
/*   52:     */     throws ProtobufException
/*   53:     */   {
/*   54: 134 */     if (this.lastTag != value) {
/*   55: 136 */       throw ProtobufException.invalidEndTag();
/*   56:     */     }
/*   57:     */   }
/*   58:     */   
/*   59:     */   public boolean skipField(int tag)
/*   60:     */     throws IOException
/*   61:     */   {
/*   62: 148 */     switch (WireFormat.getTagWireType(tag))
/*   63:     */     {
/*   64:     */     case 0: 
/*   65: 151 */       readInt32();
/*   66: 152 */       return true;
/*   67:     */     case 1: 
/*   68: 154 */       readRawLittleEndian64();
/*   69: 155 */       return true;
/*   70:     */     case 2: 
/*   71: 157 */       skipRawBytes(readRawVarint32());
/*   72: 158 */       return true;
/*   73:     */     case 3: 
/*   74: 160 */       skipMessage();
/*   75: 161 */       checkLastTagWas(WireFormat.makeTag(WireFormat.getTagFieldNumber(tag), 4));
/*   76:     */       
/*   77: 163 */       return true;
/*   78:     */     case 4: 
/*   79: 165 */       return false;
/*   80:     */     case 5: 
/*   81: 167 */       readRawLittleEndian32();
/*   82: 168 */       return true;
/*   83:     */     }
/*   84: 170 */     throw ProtobufException.invalidWireType();
/*   85:     */   }
/*   86:     */   
/*   87:     */   public void skipMessage()
/*   88:     */     throws IOException
/*   89:     */   {
/*   90:     */     for (;;)
/*   91:     */     {
/*   92: 182 */       int tag = readTag();
/*   93: 183 */       if ((tag == 0) || (!skipField(tag))) {
/*   94: 185 */         return;
/*   95:     */       }
/*   96:     */     }
/*   97:     */   }
/*   98:     */   
/*   99:     */   public double readDouble()
/*  100:     */     throws IOException
/*  101:     */   {
/*  102: 198 */     checkIfPackedField();
/*  103: 199 */     return Double.longBitsToDouble(readRawLittleEndian64());
/*  104:     */   }
/*  105:     */   
/*  106:     */   public float readFloat()
/*  107:     */     throws IOException
/*  108:     */   {
/*  109: 208 */     checkIfPackedField();
/*  110: 209 */     return Float.intBitsToFloat(readRawLittleEndian32());
/*  111:     */   }
/*  112:     */   
/*  113:     */   public long readUInt64()
/*  114:     */     throws IOException
/*  115:     */   {
/*  116: 218 */     checkIfPackedField();
/*  117: 219 */     return readRawVarint64();
/*  118:     */   }
/*  119:     */   
/*  120:     */   public long readInt64()
/*  121:     */     throws IOException
/*  122:     */   {
/*  123: 228 */     checkIfPackedField();
/*  124: 229 */     return readRawVarint64();
/*  125:     */   }
/*  126:     */   
/*  127:     */   public int readInt32()
/*  128:     */     throws IOException
/*  129:     */   {
/*  130: 238 */     checkIfPackedField();
/*  131: 239 */     return readRawVarint32();
/*  132:     */   }
/*  133:     */   
/*  134:     */   public long readFixed64()
/*  135:     */     throws IOException
/*  136:     */   {
/*  137: 248 */     checkIfPackedField();
/*  138: 249 */     return readRawLittleEndian64();
/*  139:     */   }
/*  140:     */   
/*  141:     */   public int readFixed32()
/*  142:     */     throws IOException
/*  143:     */   {
/*  144: 258 */     checkIfPackedField();
/*  145: 259 */     return readRawLittleEndian32();
/*  146:     */   }
/*  147:     */   
/*  148:     */   public boolean readBool()
/*  149:     */     throws IOException
/*  150:     */   {
/*  151: 268 */     checkIfPackedField();
/*  152: 269 */     return readRawVarint32() != 0;
/*  153:     */   }
/*  154:     */   
/*  155:     */   public String readString()
/*  156:     */     throws IOException
/*  157:     */   {
/*  158: 278 */     int size = readRawVarint32();
/*  159: 279 */     if ((size <= this.bufferSize - this.bufferPos) && (size > 0))
/*  160:     */     {
/*  161: 283 */       String result = StringSerializer.STRING.deser(this.buffer, this.bufferPos, size);
/*  162: 284 */       this.bufferPos += size;
/*  163: 285 */       return result;
/*  164:     */     }
/*  165: 290 */     return StringSerializer.STRING.deser(readRawBytes(size));
/*  166:     */   }
/*  167:     */   
/*  168:     */   public void readBytes(ByteBuffer bb)
/*  169:     */     throws IOException
/*  170:     */   {
/*  171: 300 */     int size = readRawVarint32();
/*  172: 302 */     if ((size <= this.bufferSize - this.bufferPos) && (size > 0))
/*  173:     */     {
/*  174: 306 */       bb.limit(size);
/*  175: 307 */       bb.put(this.buffer, this.bufferPos, size);
/*  176: 308 */       this.bufferPos += size;
/*  177:     */     }
/*  178:     */     else
/*  179:     */     {
/*  180: 313 */       bb.put(readRawBytes(size));
/*  181:     */     }
/*  182:     */   }
/*  183:     */   
/*  184:     */   public <T> T mergeObject(T value, Schema<T> schema)
/*  185:     */     throws IOException
/*  186:     */   {
/*  187: 320 */     if (this.decodeNestedMessageAsGroup) {
/*  188: 321 */       return mergeObjectEncodedAsGroup(value, schema);
/*  189:     */     }
/*  190: 323 */     int length = readRawVarint32();
/*  191:     */     
/*  192:     */ 
/*  193:     */ 
/*  194: 327 */     int oldLimit = pushLimit(length);
/*  195: 330 */     if (value == null) {
/*  196: 332 */       value = schema.newMessage();
/*  197:     */     }
/*  198: 334 */     schema.mergeFrom(this, value);
/*  199: 335 */     if (!schema.isInitialized(value)) {
/*  200: 337 */       throw new UninitializedMessageException(value, schema);
/*  201:     */     }
/*  202: 339 */     checkLastTagWas(0);
/*  203:     */     
/*  204: 341 */     popLimit(oldLimit);
/*  205: 342 */     return value;
/*  206:     */   }
/*  207:     */   
/*  208:     */   private <T> T mergeObjectEncodedAsGroup(T value, Schema<T> schema)
/*  209:     */     throws IOException
/*  210:     */   {
/*  211: 355 */     if (value == null) {
/*  212: 357 */       value = schema.newMessage();
/*  213:     */     }
/*  214: 359 */     schema.mergeFrom(this, value);
/*  215: 360 */     if (!schema.isInitialized(value)) {
/*  216: 362 */       throw new UninitializedMessageException(value, schema);
/*  217:     */     }
/*  218: 365 */     checkLastTagWas(0);
/*  219:     */     
/*  220: 367 */     return value;
/*  221:     */   }
/*  222:     */   
/*  223:     */   public ByteString readBytes()
/*  224:     */     throws IOException
/*  225:     */   {
/*  226: 388 */     int size = readRawVarint32();
/*  227: 389 */     if (size == 0) {
/*  228: 391 */       return ByteString.EMPTY;
/*  229:     */     }
/*  230: 394 */     if ((size <= this.bufferSize - this.bufferPos) && (size > 0))
/*  231:     */     {
/*  232: 398 */       ByteString result = ByteString.copyFrom(this.buffer, this.bufferPos, size);
/*  233: 399 */       this.bufferPos += size;
/*  234: 400 */       return result;
/*  235:     */     }
/*  236: 406 */     return ByteString.wrap(readRawBytes(size));
/*  237:     */   }
/*  238:     */   
/*  239:     */   public int readUInt32()
/*  240:     */     throws IOException
/*  241:     */   {
/*  242: 416 */     checkIfPackedField();
/*  243: 417 */     return readRawVarint32();
/*  244:     */   }
/*  245:     */   
/*  246:     */   public int readEnum()
/*  247:     */     throws IOException
/*  248:     */   {
/*  249: 427 */     checkIfPackedField();
/*  250: 428 */     return readRawVarint32();
/*  251:     */   }
/*  252:     */   
/*  253:     */   public int readSFixed32()
/*  254:     */     throws IOException
/*  255:     */   {
/*  256: 437 */     checkIfPackedField();
/*  257: 438 */     return readRawLittleEndian32();
/*  258:     */   }
/*  259:     */   
/*  260:     */   public long readSFixed64()
/*  261:     */     throws IOException
/*  262:     */   {
/*  263: 447 */     checkIfPackedField();
/*  264: 448 */     return readRawLittleEndian64();
/*  265:     */   }
/*  266:     */   
/*  267:     */   public int readSInt32()
/*  268:     */     throws IOException
/*  269:     */   {
/*  270: 457 */     checkIfPackedField();
/*  271: 458 */     return decodeZigZag32(readRawVarint32());
/*  272:     */   }
/*  273:     */   
/*  274:     */   public long readSInt64()
/*  275:     */     throws IOException
/*  276:     */   {
/*  277: 467 */     checkIfPackedField();
/*  278: 468 */     return decodeZigZag64(readRawVarint64());
/*  279:     */   }
/*  280:     */   
/*  281:     */   public int readRawVarint32()
/*  282:     */     throws IOException
/*  283:     */   {
/*  284: 478 */     byte tmp = readRawByte();
/*  285: 479 */     if (tmp >= 0) {
/*  286: 481 */       return tmp;
/*  287:     */     }
/*  288: 483 */     int result = tmp & 0x7F;
/*  289: 484 */     if ((tmp = readRawByte()) >= 0)
/*  290:     */     {
/*  291: 486 */       result |= tmp << 7;
/*  292:     */     }
/*  293:     */     else
/*  294:     */     {
/*  295: 490 */       result |= (tmp & 0x7F) << 7;
/*  296: 491 */       if ((tmp = readRawByte()) >= 0)
/*  297:     */       {
/*  298: 493 */         result |= tmp << 14;
/*  299:     */       }
/*  300:     */       else
/*  301:     */       {
/*  302: 497 */         result |= (tmp & 0x7F) << 14;
/*  303: 498 */         if ((tmp = readRawByte()) >= 0)
/*  304:     */         {
/*  305: 500 */           result |= tmp << 21;
/*  306:     */         }
/*  307:     */         else
/*  308:     */         {
/*  309: 504 */           result |= (tmp & 0x7F) << 21;
/*  310: 505 */           result |= (tmp = readRawByte()) << 28;
/*  311: 506 */           if (tmp < 0)
/*  312:     */           {
/*  313: 509 */             for (int i = 0; i < 5; i++) {
/*  314: 511 */               if (readRawByte() >= 0) {
/*  315: 513 */                 return result;
/*  316:     */               }
/*  317:     */             }
/*  318: 516 */             throw ProtobufException.malformedVarint();
/*  319:     */           }
/*  320:     */         }
/*  321:     */       }
/*  322:     */     }
/*  323: 521 */     return result;
/*  324:     */   }
/*  325:     */   
/*  326:     */   static int readRawVarint32(InputStream input)
/*  327:     */     throws IOException
/*  328:     */   {
/*  329: 531 */     int firstByte = input.read();
/*  330: 532 */     if (firstByte == -1) {
/*  331: 534 */       throw ProtobufException.truncatedMessage();
/*  332:     */     }
/*  333: 537 */     if ((firstByte & 0x80) == 0) {
/*  334: 539 */       return firstByte;
/*  335:     */     }
/*  336: 541 */     return readRawVarint32(input, firstByte);
/*  337:     */   }
/*  338:     */   
/*  339:     */   static int readRawVarint32(InputStream input, int firstByte)
/*  340:     */     throws IOException
/*  341:     */   {
/*  342: 551 */     int result = firstByte & 0x7F;
/*  343: 552 */     for (int offset = 7; offset < 32; offset += 7)
/*  344:     */     {
/*  345: 555 */       int b = input.read();
/*  346: 556 */       if (b == -1) {
/*  347: 558 */         throw ProtobufException.truncatedMessage();
/*  348:     */       }
/*  349: 560 */       result |= (b & 0x7F) << offset;
/*  350: 561 */       if ((b & 0x80) == 0) {
/*  351: 563 */         return result;
/*  352:     */       }
/*  353:     */     }
/*  354: 567 */     for (; offset < 64; offset += 7)
/*  355:     */     {
/*  356: 569 */       int b = input.read();
/*  357: 570 */       if (b == -1) {
/*  358: 572 */         throw ProtobufException.truncatedMessage();
/*  359:     */       }
/*  360: 574 */       if ((b & 0x80) == 0) {
/*  361: 576 */         return result;
/*  362:     */       }
/*  363:     */     }
/*  364: 579 */     throw ProtobufException.malformedVarint();
/*  365:     */   }
/*  366:     */   
/*  367:     */   static int readRawVarint32(DataInput input, byte firstByte)
/*  368:     */     throws IOException
/*  369:     */   {
/*  370: 588 */     int result = firstByte & 0x7F;
/*  371: 589 */     for (int offset = 7; offset < 32; offset += 7)
/*  372:     */     {
/*  373: 592 */       byte b = input.readByte();
/*  374: 593 */       result |= (b & 0x7F) << offset;
/*  375: 594 */       if ((b & 0x80) == 0) {
/*  376: 596 */         return result;
/*  377:     */       }
/*  378:     */     }
/*  379: 600 */     for (; offset < 64; offset += 7)
/*  380:     */     {
/*  381: 602 */       byte b = input.readByte();
/*  382: 603 */       if ((b & 0x80) == 0) {
/*  383: 605 */         return result;
/*  384:     */       }
/*  385:     */     }
/*  386: 608 */     throw ProtobufException.malformedVarint();
/*  387:     */   }
/*  388:     */   
/*  389:     */   public long readRawVarint64()
/*  390:     */     throws IOException
/*  391:     */   {
/*  392: 616 */     int shift = 0;
/*  393: 617 */     long result = 0L;
/*  394: 618 */     while (shift < 64)
/*  395:     */     {
/*  396: 620 */       byte b = readRawByte();
/*  397: 621 */       result |= (b & 0x7F) << shift;
/*  398: 622 */       if ((b & 0x80) == 0) {
/*  399: 624 */         return result;
/*  400:     */       }
/*  401: 626 */       shift += 7;
/*  402:     */     }
/*  403: 628 */     throw ProtobufException.malformedVarint();
/*  404:     */   }
/*  405:     */   
/*  406:     */   public int readRawLittleEndian32()
/*  407:     */     throws IOException
/*  408:     */   {
/*  409: 636 */     byte b1 = readRawByte();
/*  410: 637 */     byte b2 = readRawByte();
/*  411: 638 */     byte b3 = readRawByte();
/*  412: 639 */     byte b4 = readRawByte();
/*  413: 640 */     return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24;
/*  414:     */   }
/*  415:     */   
/*  416:     */   public long readRawLittleEndian64()
/*  417:     */     throws IOException
/*  418:     */   {
/*  419: 651 */     byte b1 = readRawByte();
/*  420: 652 */     byte b2 = readRawByte();
/*  421: 653 */     byte b3 = readRawByte();
/*  422: 654 */     byte b4 = readRawByte();
/*  423: 655 */     byte b5 = readRawByte();
/*  424: 656 */     byte b6 = readRawByte();
/*  425: 657 */     byte b7 = readRawByte();
/*  426: 658 */     byte b8 = readRawByte();
/*  427: 659 */     return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24 | (b5 & 0xFF) << 32 | (b6 & 0xFF) << 40 | (b7 & 0xFF) << 48 | (b8 & 0xFF) << 56;
/*  428:     */   }
/*  429:     */   
/*  430:     */   public static int decodeZigZag32(int n)
/*  431:     */   {
/*  432: 680 */     return n >>> 1 ^ -(n & 0x1);
/*  433:     */   }
/*  434:     */   
/*  435:     */   public static long decodeZigZag64(long n)
/*  436:     */   {
/*  437: 694 */     return n >>> 1 ^ -(n & 1L);
/*  438:     */   }
/*  439:     */   
/*  440: 705 */   private int packedLimit = 0;
/*  441:     */   private int totalBytesRetired;
/*  442: 717 */   private int currentLimit = 2147483647;
/*  443:     */   public final boolean decodeNestedMessageAsGroup;
/*  444: 731 */   private int sizeLimit = 67108864;
/*  445:     */   static final int DEFAULT_SIZE_LIMIT = 67108864;
/*  446:     */   static final int DEFAULT_BUFFER_SIZE = 4096;
/*  447:     */   
/*  448:     */   public CodedInput(byte[] buffer, int off, int len, boolean decodeNestedMessageAsGroup)
/*  449:     */   {
/*  450: 740 */     this.buffer = buffer;
/*  451: 741 */     this.bufferSize = (off + len);
/*  452: 742 */     this.bufferPos = off;
/*  453: 743 */     this.totalBytesRetired = (-off);
/*  454: 744 */     this.input = null;
/*  455: 745 */     this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
/*  456:     */   }
/*  457:     */   
/*  458:     */   public CodedInput(InputStream input, boolean decodeNestedMessageAsGroup)
/*  459:     */   {
/*  460: 750 */     this(input, new byte[4096], 0, 0, decodeNestedMessageAsGroup);
/*  461:     */   }
/*  462:     */   
/*  463:     */   public CodedInput(InputStream input, byte[] buffer, boolean decodeNestedMessageAsGroup)
/*  464:     */   {
/*  465: 756 */     this(input, buffer, 0, 0, decodeNestedMessageAsGroup);
/*  466:     */   }
/*  467:     */   
/*  468:     */   public CodedInput(InputStream input, byte[] buffer, int offset, int limit, boolean decodeNestedMessageAsGroup)
/*  469:     */   {
/*  470: 762 */     this.buffer = buffer;
/*  471: 763 */     this.bufferSize = limit;
/*  472: 764 */     this.bufferPos = offset;
/*  473: 765 */     this.totalBytesRetired = (-offset);
/*  474: 766 */     this.input = input;
/*  475: 767 */     this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
/*  476:     */   }
/*  477:     */   
/*  478:     */   public int setSizeLimit(int limit)
/*  479:     */   {
/*  480: 794 */     if (limit < 0) {
/*  481: 796 */       throw new IllegalArgumentException("Size limit cannot be negative: " + limit);
/*  482:     */     }
/*  483: 799 */     int oldLimit = this.sizeLimit;
/*  484: 800 */     this.sizeLimit = limit;
/*  485: 801 */     return oldLimit;
/*  486:     */   }
/*  487:     */   
/*  488:     */   public void resetSizeCounter()
/*  489:     */   {
/*  490: 810 */     this.totalBytesRetired = (-this.bufferPos);
/*  491:     */   }
/*  492:     */   
/*  493:     */   public void reset()
/*  494:     */   {
/*  495: 818 */     this.bufferSize = 0;
/*  496: 819 */     this.bufferPos = 0;
/*  497: 820 */     this.bufferSizeAfterLimit = 0;
/*  498: 821 */     this.currentLimit = 2147483647;
/*  499: 822 */     this.lastTag = 0;
/*  500: 823 */     this.packedLimit = 0;
/*  501: 824 */     this.sizeLimit = 67108864;
/*  502: 825 */     resetSizeCounter();
/*  503:     */   }
/*  504:     */   
/*  505:     */   public int pushLimit(int byteLimit)
/*  506:     */     throws ProtobufException
/*  507:     */   {
/*  508: 839 */     if (byteLimit < 0) {
/*  509: 841 */       throw ProtobufException.negativeSize();
/*  510:     */     }
/*  511: 843 */     byteLimit += this.totalBytesRetired + this.bufferPos;
/*  512: 844 */     int oldLimit = this.currentLimit;
/*  513: 845 */     if (byteLimit > oldLimit) {
/*  514: 847 */       throw ProtobufException.truncatedMessage();
/*  515:     */     }
/*  516: 849 */     this.currentLimit = byteLimit;
/*  517:     */     
/*  518: 851 */     recomputeBufferSizeAfterLimit();
/*  519:     */     
/*  520: 853 */     return oldLimit;
/*  521:     */   }
/*  522:     */   
/*  523:     */   private void recomputeBufferSizeAfterLimit()
/*  524:     */   {
/*  525: 858 */     this.bufferSize += this.bufferSizeAfterLimit;
/*  526: 859 */     int bufferEnd = this.totalBytesRetired + this.bufferSize;
/*  527: 860 */     if (bufferEnd > this.currentLimit)
/*  528:     */     {
/*  529: 863 */       this.bufferSizeAfterLimit = (bufferEnd - this.currentLimit);
/*  530: 864 */       this.bufferSize -= this.bufferSizeAfterLimit;
/*  531:     */     }
/*  532:     */     else
/*  533:     */     {
/*  534: 868 */       this.bufferSizeAfterLimit = 0;
/*  535:     */     }
/*  536:     */   }
/*  537:     */   
/*  538:     */   public void popLimit(int oldLimit)
/*  539:     */   {
/*  540: 880 */     this.currentLimit = oldLimit;
/*  541: 881 */     recomputeBufferSizeAfterLimit();
/*  542:     */   }
/*  543:     */   
/*  544:     */   public int getBytesUntilLimit()
/*  545:     */   {
/*  546: 889 */     if (this.currentLimit == 2147483647) {
/*  547: 891 */       return -1;
/*  548:     */     }
/*  549: 894 */     int currentAbsolutePosition = this.totalBytesRetired + this.bufferPos;
/*  550: 895 */     return this.currentLimit - currentAbsolutePosition;
/*  551:     */   }
/*  552:     */   
/*  553:     */   public boolean isCurrentFieldPacked()
/*  554:     */   {
/*  555: 903 */     return (this.packedLimit != 0) && (this.packedLimit != getTotalBytesRead());
/*  556:     */   }
/*  557:     */   
/*  558:     */   public boolean isAtEnd()
/*  559:     */     throws IOException
/*  560:     */   {
/*  561: 912 */     return (this.bufferPos == this.bufferSize) && (!refillBuffer(false));
/*  562:     */   }
/*  563:     */   
/*  564:     */   public int getTotalBytesRead()
/*  565:     */   {
/*  566: 920 */     return this.totalBytesRetired + this.bufferPos;
/*  567:     */   }
/*  568:     */   
/*  569:     */   private boolean refillBuffer(boolean mustSucceed)
/*  570:     */     throws IOException
/*  571:     */   {
/*  572: 931 */     if (this.bufferPos < this.bufferSize) {
/*  573: 933 */       throw new IllegalStateException("refillBuffer() called when buffer wasn't empty.");
/*  574:     */     }
/*  575: 937 */     if (this.totalBytesRetired + this.bufferSize == this.currentLimit)
/*  576:     */     {
/*  577: 940 */       if (mustSucceed) {
/*  578: 942 */         throw ProtobufException.truncatedMessage();
/*  579:     */       }
/*  580: 946 */       return false;
/*  581:     */     }
/*  582: 950 */     this.totalBytesRetired += this.bufferSize;
/*  583:     */     
/*  584: 952 */     this.bufferPos = 0;
/*  585: 953 */     this.bufferSize = (this.input == null ? -1 : this.input.read(this.buffer));
/*  586: 954 */     if ((this.bufferSize == 0) || (this.bufferSize < -1)) {
/*  587: 956 */       throw new IllegalStateException("InputStream#read(byte[]) returned invalid result: " + this.bufferSize + "\nThe InputStream implementation is buggy.");
/*  588:     */     }
/*  589: 960 */     if (this.bufferSize == -1)
/*  590:     */     {
/*  591: 962 */       this.bufferSize = 0;
/*  592: 963 */       if (mustSucceed) {
/*  593: 965 */         throw ProtobufException.truncatedMessage();
/*  594:     */       }
/*  595: 969 */       return false;
/*  596:     */     }
/*  597: 974 */     recomputeBufferSizeAfterLimit();
/*  598: 975 */     int totalBytesRead = this.totalBytesRetired + this.bufferSize + this.bufferSizeAfterLimit;
/*  599: 977 */     if ((totalBytesRead > this.sizeLimit) || (totalBytesRead < 0)) {
/*  600: 979 */       throw ProtobufException.sizeLimitExceeded();
/*  601:     */     }
/*  602: 981 */     return true;
/*  603:     */   }
/*  604:     */   
/*  605:     */   public byte readRawByte()
/*  606:     */     throws IOException
/*  607:     */   {
/*  608: 993 */     if (this.bufferPos == this.bufferSize) {
/*  609: 995 */       refillBuffer(true);
/*  610:     */     }
/*  611: 997 */     return this.buffer[(this.bufferPos++)];
/*  612:     */   }
/*  613:     */   
/*  614:     */   public byte[] readRawBytes(int size)
/*  615:     */     throws IOException
/*  616:     */   {
/*  617:1008 */     if (size < 0) {
/*  618:1010 */       throw ProtobufException.negativeSize();
/*  619:     */     }
/*  620:1013 */     if (this.totalBytesRetired + this.bufferPos + size > this.currentLimit)
/*  621:     */     {
/*  622:1016 */       skipRawBytes(this.currentLimit - this.totalBytesRetired - this.bufferPos);
/*  623:     */       
/*  624:1018 */       throw ProtobufException.truncatedMessage();
/*  625:     */     }
/*  626:1021 */     if (size <= this.bufferSize - this.bufferPos)
/*  627:     */     {
/*  628:1024 */       byte[] bytes = new byte[size];
/*  629:1025 */       System.arraycopy(this.buffer, this.bufferPos, bytes, 0, size);
/*  630:1026 */       this.bufferPos += size;
/*  631:1027 */       return bytes;
/*  632:     */     }
/*  633:1029 */     if (size < this.buffer.length)
/*  634:     */     {
/*  635:1035 */       byte[] bytes = new byte[size];
/*  636:1036 */       int pos = this.bufferSize - this.bufferPos;
/*  637:1037 */       System.arraycopy(this.buffer, this.bufferPos, bytes, 0, pos);
/*  638:1038 */       this.bufferPos = this.bufferSize;
/*  639:     */       
/*  640:     */ 
/*  641:     */ 
/*  642:     */ 
/*  643:1043 */       refillBuffer(true);
/*  644:1045 */       while (size - pos > this.bufferSize)
/*  645:     */       {
/*  646:1047 */         System.arraycopy(this.buffer, 0, bytes, pos, this.bufferSize);
/*  647:1048 */         pos += this.bufferSize;
/*  648:1049 */         this.bufferPos = this.bufferSize;
/*  649:1050 */         refillBuffer(true);
/*  650:     */       }
/*  651:1053 */       System.arraycopy(this.buffer, 0, bytes, pos, size - pos);
/*  652:1054 */       this.bufferPos = (size - pos);
/*  653:     */       
/*  654:1056 */       return bytes;
/*  655:     */     }
/*  656:1070 */     int originalBufferPos = this.bufferPos;
/*  657:1071 */     int originalBufferSize = this.bufferSize;
/*  658:     */     
/*  659:     */ 
/*  660:1074 */     this.totalBytesRetired += this.bufferSize;
/*  661:1075 */     this.bufferPos = 0;
/*  662:1076 */     this.bufferSize = 0;
/*  663:     */     
/*  664:     */ 
/*  665:1079 */     int sizeLeft = size - (originalBufferSize - originalBufferPos);
/*  666:1080 */     List<byte[]> chunks = new ArrayList();
/*  667:     */     int n;
/*  668:1082 */     while (sizeLeft > 0)
/*  669:     */     {
/*  670:1084 */       byte[] chunk = new byte[Math.min(sizeLeft, this.buffer.length)];
/*  671:1085 */       int pos = 0;
/*  672:1086 */       while (pos < chunk.length)
/*  673:     */       {
/*  674:1089 */         n = this.input == null ? -1 : this.input.read(chunk, pos, chunk.length - pos);
/*  675:1090 */         if (n == -1) {
/*  676:1092 */           throw ProtobufException.truncatedMessage();
/*  677:     */         }
/*  678:1094 */         this.totalBytesRetired += n;
/*  679:1095 */         pos += n;
/*  680:     */       }
/*  681:1097 */       sizeLeft -= chunk.length;
/*  682:1098 */       chunks.add(chunk);
/*  683:     */     }
/*  684:1102 */     byte[] bytes = new byte[size];
/*  685:     */     
/*  686:     */ 
/*  687:1105 */     int pos = originalBufferSize - originalBufferPos;
/*  688:1106 */     System.arraycopy(this.buffer, originalBufferPos, bytes, 0, pos);
/*  689:1109 */     for (byte[] chunk : chunks)
/*  690:     */     {
/*  691:1111 */       System.arraycopy(chunk, 0, bytes, pos, chunk.length);
/*  692:1112 */       pos += chunk.length;
/*  693:     */     }
/*  694:1116 */     return bytes;
/*  695:     */   }
/*  696:     */   
/*  697:     */   public void skipRawBytes(int size)
/*  698:     */     throws IOException
/*  699:     */   {
/*  700:1128 */     if (size < 0) {
/*  701:1130 */       throw ProtobufException.negativeSize();
/*  702:     */     }
/*  703:1133 */     if (this.totalBytesRetired + this.bufferPos + size > this.currentLimit)
/*  704:     */     {
/*  705:1136 */       skipRawBytes(this.currentLimit - this.totalBytesRetired - this.bufferPos);
/*  706:     */       
/*  707:1138 */       throw ProtobufException.truncatedMessage();
/*  708:     */     }
/*  709:1141 */     if (size <= this.bufferSize - this.bufferPos)
/*  710:     */     {
/*  711:1144 */       this.bufferPos += size;
/*  712:     */     }
/*  713:     */     else
/*  714:     */     {
/*  715:1149 */       int pos = this.bufferSize - this.bufferPos;
/*  716:1150 */       this.bufferPos = this.bufferSize;
/*  717:     */       
/*  718:     */ 
/*  719:     */ 
/*  720:     */ 
/*  721:1155 */       refillBuffer(true);
/*  722:1156 */       while (size - pos > this.bufferSize)
/*  723:     */       {
/*  724:1158 */         pos += this.bufferSize;
/*  725:1159 */         this.bufferPos = this.bufferSize;
/*  726:1160 */         refillBuffer(true);
/*  727:     */       }
/*  728:1163 */       this.bufferPos = (size - pos);
/*  729:     */     }
/*  730:     */   }
/*  731:     */   
/*  732:     */   public <T> int readFieldNumber(Schema<T> schema)
/*  733:     */     throws IOException
/*  734:     */   {
/*  735:1171 */     if (isAtEnd())
/*  736:     */     {
/*  737:1173 */       this.lastTag = 0;
/*  738:1174 */       return 0;
/*  739:     */     }
/*  740:1178 */     if (isCurrentFieldPacked())
/*  741:     */     {
/*  742:1180 */       if (this.packedLimit < getTotalBytesRead()) {
/*  743:1181 */         throw ProtobufException.misreportedSize();
/*  744:     */       }
/*  745:1184 */       return this.lastTag >>> 3;
/*  746:     */     }
/*  747:1187 */     this.packedLimit = 0;
/*  748:1188 */     int tag = readRawVarint32();
/*  749:1189 */     int fieldNumber = tag >>> 3;
/*  750:1190 */     if (fieldNumber == 0)
/*  751:     */     {
/*  752:1192 */       if ((this.decodeNestedMessageAsGroup) && (7 == (tag & 0x7)))
/*  753:     */       {
/*  754:1196 */         this.lastTag = 0;
/*  755:1197 */         return 0;
/*  756:     */       }
/*  757:1200 */       throw ProtobufException.invalidTag();
/*  758:     */     }
/*  759:1202 */     if ((this.decodeNestedMessageAsGroup) && (4 == (tag & 0x7)))
/*  760:     */     {
/*  761:1204 */       this.lastTag = 0;
/*  762:1205 */       return 0;
/*  763:     */     }
/*  764:1208 */     this.lastTag = tag;
/*  765:1209 */     return fieldNumber;
/*  766:     */   }
/*  767:     */   
/*  768:     */   private void checkIfPackedField()
/*  769:     */     throws IOException
/*  770:     */   {
/*  771:1221 */     if ((this.packedLimit == 0) && (WireFormat.getTagWireType(this.lastTag) == 2))
/*  772:     */     {
/*  773:1223 */       int length = readRawVarint32();
/*  774:1224 */       if (length < 0) {
/*  775:1225 */         throw ProtobufException.negativeSize();
/*  776:     */       }
/*  777:1227 */       this.packedLimit = (getTotalBytesRead() + length);
/*  778:     */     }
/*  779:     */   }
/*  780:     */   
/*  781:     */   public byte[] readByteArray()
/*  782:     */     throws IOException
/*  783:     */   {
/*  784:1234 */     int size = readRawVarint32();
/*  785:1235 */     if ((size <= this.bufferSize - this.bufferPos) && (size > 0))
/*  786:     */     {
/*  787:1239 */       byte[] copy = new byte[size];
/*  788:1240 */       System.arraycopy(this.buffer, this.bufferPos, copy, 0, size);
/*  789:1241 */       this.bufferPos += size;
/*  790:1242 */       return copy;
/*  791:     */     }
/*  792:1247 */     return readRawBytes(size);
/*  793:     */   }
/*  794:     */   
/*  795:     */   public <T> void handleUnknownField(int fieldNumber, Schema<T> schema)
/*  796:     */     throws IOException
/*  797:     */   {
/*  798:1254 */     skipField(this.lastTag);
/*  799:     */   }
/*  800:     */   
/*  801:     */   public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated)
/*  802:     */     throws IOException
/*  803:     */   {
/*  804:1261 */     int size = readRawVarint32();
/*  805:1262 */     if ((size <= this.bufferSize - this.bufferPos) && (size > 0))
/*  806:     */     {
/*  807:1265 */       output.writeByteRange(utf8String, fieldNumber, this.buffer, this.bufferPos, size, repeated);
/*  808:1266 */       this.bufferPos += size;
/*  809:     */     }
/*  810:     */     else
/*  811:     */     {
/*  812:1271 */       output.writeByteRange(utf8String, fieldNumber, readRawBytes(size), 0, size, repeated);
/*  813:     */     }
/*  814:     */   }
/*  815:     */   
/*  816:     */   public int getLastTag()
/*  817:     */   {
/*  818:1280 */     return this.lastTag;
/*  819:     */   }
/*  820:     */   
/*  821:     */   public ByteBuffer readByteBuffer()
/*  822:     */     throws IOException
/*  823:     */   {
/*  824:1289 */     return ByteBuffer.wrap(readByteArray());
/*  825:     */   }
/*  826:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.CodedInput
 * JD-Core Version:    0.7.0.1
 */