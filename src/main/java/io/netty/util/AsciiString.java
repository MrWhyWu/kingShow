/*    1:     */ package io.netty.util;
/*    2:     */ 
/*    3:     */ import io.netty.util.internal.EmptyArrays;
/*    4:     */ import io.netty.util.internal.InternalThreadLocalMap;
/*    5:     */ import io.netty.util.internal.MathUtil;
/*    6:     */ import io.netty.util.internal.ObjectUtil;
/*    7:     */ import io.netty.util.internal.PlatformDependent;
/*    8:     */ import java.nio.ByteBuffer;
/*    9:     */ import java.nio.CharBuffer;
/*   10:     */ import java.nio.charset.Charset;
/*   11:     */ import java.nio.charset.CharsetEncoder;
/*   12:     */ import java.util.Arrays;
/*   13:     */ import java.util.Collection;
/*   14:     */ import java.util.List;
/*   15:     */ import java.util.regex.Pattern;
/*   16:     */ 
/*   17:     */ public final class AsciiString
/*   18:     */   implements CharSequence, Comparable<CharSequence>
/*   19:     */ {
/*   20:  48 */   public static final AsciiString EMPTY_STRING = cached("");
/*   21:     */   private static final char MAX_CHAR_VALUE = 'ÿ';
/*   22:     */   public static final int INDEX_NOT_FOUND = -1;
/*   23:     */   private final byte[] value;
/*   24:     */   private final int offset;
/*   25:     */   private final int length;
/*   26:     */   private int hash;
/*   27:     */   private String string;
/*   28:     */   
/*   29:     */   public AsciiString(byte[] value)
/*   30:     */   {
/*   31:  79 */     this(value, true);
/*   32:     */   }
/*   33:     */   
/*   34:     */   public AsciiString(byte[] value, boolean copy)
/*   35:     */   {
/*   36:  87 */     this(value, 0, value.length, copy);
/*   37:     */   }
/*   38:     */   
/*   39:     */   public AsciiString(byte[] value, int start, int length, boolean copy)
/*   40:     */   {
/*   41:  96 */     if (copy)
/*   42:     */     {
/*   43:  97 */       this.value = Arrays.copyOfRange(value, start, start + length);
/*   44:  98 */       this.offset = 0;
/*   45:     */     }
/*   46:     */     else
/*   47:     */     {
/*   48: 100 */       if (MathUtil.isOutOfBounds(start, length, value.length)) {
/*   49: 101 */         throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= value.length(" + value.length + ')');
/*   50:     */       }
/*   51: 104 */       this.value = value;
/*   52: 105 */       this.offset = start;
/*   53:     */     }
/*   54: 107 */     this.length = length;
/*   55:     */   }
/*   56:     */   
/*   57:     */   public AsciiString(ByteBuffer value)
/*   58:     */   {
/*   59: 115 */     this(value, true);
/*   60:     */   }
/*   61:     */   
/*   62:     */   public AsciiString(ByteBuffer value, boolean copy)
/*   63:     */   {
/*   64: 125 */     this(value, value.position(), value.remaining(), copy);
/*   65:     */   }
/*   66:     */   
/*   67:     */   public AsciiString(ByteBuffer value, int start, int length, boolean copy)
/*   68:     */   {
/*   69: 135 */     if (MathUtil.isOutOfBounds(start, length, value.capacity())) {
/*   70: 137 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= value.capacity(" + value.capacity() + ')');
/*   71:     */     }
/*   72: 140 */     if (value.hasArray())
/*   73:     */     {
/*   74: 141 */       if (copy)
/*   75:     */       {
/*   76: 142 */         int bufferOffset = value.arrayOffset() + start;
/*   77: 143 */         this.value = Arrays.copyOfRange(value.array(), bufferOffset, bufferOffset + length);
/*   78: 144 */         this.offset = 0;
/*   79:     */       }
/*   80:     */       else
/*   81:     */       {
/*   82: 146 */         this.value = value.array();
/*   83: 147 */         this.offset = start;
/*   84:     */       }
/*   85:     */     }
/*   86:     */     else
/*   87:     */     {
/*   88: 150 */       this.value = new byte[length];
/*   89: 151 */       int oldPos = value.position();
/*   90: 152 */       value.get(this.value, 0, length);
/*   91: 153 */       value.position(oldPos);
/*   92: 154 */       this.offset = 0;
/*   93:     */     }
/*   94: 156 */     this.length = length;
/*   95:     */   }
/*   96:     */   
/*   97:     */   public AsciiString(char[] value)
/*   98:     */   {
/*   99: 163 */     this(value, 0, value.length);
/*  100:     */   }
/*  101:     */   
/*  102:     */   public AsciiString(char[] value, int start, int length)
/*  103:     */   {
/*  104: 171 */     if (MathUtil.isOutOfBounds(start, length, value.length)) {
/*  105: 172 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= value.length(" + value.length + ')');
/*  106:     */     }
/*  107: 176 */     this.value = new byte[length];
/*  108: 177 */     int i = 0;
/*  109: 177 */     for (int j = start; i < length; j++)
/*  110:     */     {
/*  111: 178 */       this.value[i] = c2b(value[j]);i++;
/*  112:     */     }
/*  113: 180 */     this.offset = 0;
/*  114: 181 */     this.length = length;
/*  115:     */   }
/*  116:     */   
/*  117:     */   public AsciiString(char[] value, Charset charset)
/*  118:     */   {
/*  119: 188 */     this(value, charset, 0, value.length);
/*  120:     */   }
/*  121:     */   
/*  122:     */   public AsciiString(char[] value, Charset charset, int start, int length)
/*  123:     */   {
/*  124: 196 */     CharBuffer cbuf = CharBuffer.wrap(value, start, length);
/*  125: 197 */     CharsetEncoder encoder = CharsetUtil.encoder(charset);
/*  126: 198 */     ByteBuffer nativeBuffer = ByteBuffer.allocate((int)(encoder.maxBytesPerChar() * length));
/*  127: 199 */     encoder.encode(cbuf, nativeBuffer, true);
/*  128: 200 */     int bufferOffset = nativeBuffer.arrayOffset();
/*  129: 201 */     this.value = Arrays.copyOfRange(nativeBuffer.array(), bufferOffset, bufferOffset + nativeBuffer.position());
/*  130: 202 */     this.offset = 0;
/*  131: 203 */     this.length = this.value.length;
/*  132:     */   }
/*  133:     */   
/*  134:     */   public AsciiString(CharSequence value)
/*  135:     */   {
/*  136: 210 */     this(value, 0, value.length());
/*  137:     */   }
/*  138:     */   
/*  139:     */   public AsciiString(CharSequence value, int start, int length)
/*  140:     */   {
/*  141: 218 */     if (MathUtil.isOutOfBounds(start, length, value.length())) {
/*  142: 220 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= value.length(" + value.length() + ')');
/*  143:     */     }
/*  144: 223 */     this.value = new byte[length];
/*  145: 224 */     int i = 0;
/*  146: 224 */     for (int j = start; i < length; j++)
/*  147:     */     {
/*  148: 225 */       this.value[i] = c2b(value.charAt(j));i++;
/*  149:     */     }
/*  150: 227 */     this.offset = 0;
/*  151: 228 */     this.length = length;
/*  152:     */   }
/*  153:     */   
/*  154:     */   public AsciiString(CharSequence value, Charset charset)
/*  155:     */   {
/*  156: 235 */     this(value, charset, 0, value.length());
/*  157:     */   }
/*  158:     */   
/*  159:     */   public AsciiString(CharSequence value, Charset charset, int start, int length)
/*  160:     */   {
/*  161: 243 */     CharBuffer cbuf = CharBuffer.wrap(value, start, start + length);
/*  162: 244 */     CharsetEncoder encoder = CharsetUtil.encoder(charset);
/*  163: 245 */     ByteBuffer nativeBuffer = ByteBuffer.allocate((int)(encoder.maxBytesPerChar() * length));
/*  164: 246 */     encoder.encode(cbuf, nativeBuffer, true);
/*  165: 247 */     int offset = nativeBuffer.arrayOffset();
/*  166: 248 */     this.value = Arrays.copyOfRange(nativeBuffer.array(), offset, offset + nativeBuffer.position());
/*  167: 249 */     this.offset = 0;
/*  168: 250 */     this.length = this.value.length;
/*  169:     */   }
/*  170:     */   
/*  171:     */   public int forEachByte(ByteProcessor visitor)
/*  172:     */     throws Exception
/*  173:     */   {
/*  174: 260 */     return forEachByte0(0, length(), visitor);
/*  175:     */   }
/*  176:     */   
/*  177:     */   public int forEachByte(int index, int length, ByteProcessor visitor)
/*  178:     */     throws Exception
/*  179:     */   {
/*  180: 271 */     if (MathUtil.isOutOfBounds(index, length, length())) {
/*  181: 273 */       throw new IndexOutOfBoundsException("expected: 0 <= index(" + index + ") <= start + length(" + length + ") <= length(" + length() + ')');
/*  182:     */     }
/*  183: 275 */     return forEachByte0(index, length, visitor);
/*  184:     */   }
/*  185:     */   
/*  186:     */   private int forEachByte0(int index, int length, ByteProcessor visitor)
/*  187:     */     throws Exception
/*  188:     */   {
/*  189: 279 */     int len = this.offset + index + length;
/*  190: 280 */     for (int i = this.offset + index; i < len; i++) {
/*  191: 281 */       if (!visitor.process(this.value[i])) {
/*  192: 282 */         return i - this.offset;
/*  193:     */       }
/*  194:     */     }
/*  195: 285 */     return -1;
/*  196:     */   }
/*  197:     */   
/*  198:     */   public int forEachByteDesc(ByteProcessor visitor)
/*  199:     */     throws Exception
/*  200:     */   {
/*  201: 295 */     return forEachByteDesc0(0, length(), visitor);
/*  202:     */   }
/*  203:     */   
/*  204:     */   public int forEachByteDesc(int index, int length, ByteProcessor visitor)
/*  205:     */     throws Exception
/*  206:     */   {
/*  207: 306 */     if (MathUtil.isOutOfBounds(index, length, length())) {
/*  208: 308 */       throw new IndexOutOfBoundsException("expected: 0 <= index(" + index + ") <= start + length(" + length + ") <= length(" + length() + ')');
/*  209:     */     }
/*  210: 310 */     return forEachByteDesc0(index, length, visitor);
/*  211:     */   }
/*  212:     */   
/*  213:     */   private int forEachByteDesc0(int index, int length, ByteProcessor visitor)
/*  214:     */     throws Exception
/*  215:     */   {
/*  216: 314 */     int end = this.offset + index;
/*  217: 315 */     for (int i = this.offset + index + length - 1; i >= end; i--) {
/*  218: 316 */       if (!visitor.process(this.value[i])) {
/*  219: 317 */         return i - this.offset;
/*  220:     */       }
/*  221:     */     }
/*  222: 320 */     return -1;
/*  223:     */   }
/*  224:     */   
/*  225:     */   public byte byteAt(int index)
/*  226:     */   {
/*  227: 326 */     if ((index < 0) || (index >= this.length)) {
/*  228: 327 */       throw new IndexOutOfBoundsException("index: " + index + " must be in the range [0," + this.length + ")");
/*  229:     */     }
/*  230: 330 */     if (PlatformDependent.hasUnsafe()) {
/*  231: 331 */       return PlatformDependent.getByte(this.value, index + this.offset);
/*  232:     */     }
/*  233: 333 */     return this.value[(index + this.offset)];
/*  234:     */   }
/*  235:     */   
/*  236:     */   public boolean isEmpty()
/*  237:     */   {
/*  238: 340 */     return this.length == 0;
/*  239:     */   }
/*  240:     */   
/*  241:     */   public int length()
/*  242:     */   {
/*  243: 348 */     return this.length;
/*  244:     */   }
/*  245:     */   
/*  246:     */   public void arrayChanged()
/*  247:     */   {
/*  248: 356 */     this.string = null;
/*  249: 357 */     this.hash = 0;
/*  250:     */   }
/*  251:     */   
/*  252:     */   public byte[] array()
/*  253:     */   {
/*  254: 368 */     return this.value;
/*  255:     */   }
/*  256:     */   
/*  257:     */   public int arrayOffset()
/*  258:     */   {
/*  259: 377 */     return this.offset;
/*  260:     */   }
/*  261:     */   
/*  262:     */   public boolean isEntireArrayUsed()
/*  263:     */   {
/*  264: 385 */     return (this.offset == 0) && (this.length == this.value.length);
/*  265:     */   }
/*  266:     */   
/*  267:     */   public byte[] toByteArray()
/*  268:     */   {
/*  269: 392 */     return toByteArray(0, length());
/*  270:     */   }
/*  271:     */   
/*  272:     */   public byte[] toByteArray(int start, int end)
/*  273:     */   {
/*  274: 400 */     return Arrays.copyOfRange(this.value, start + this.offset, end + this.offset);
/*  275:     */   }
/*  276:     */   
/*  277:     */   public void copy(int srcIdx, byte[] dst, int dstIdx, int length)
/*  278:     */   {
/*  279: 412 */     if (MathUtil.isOutOfBounds(srcIdx, length, length())) {
/*  280: 414 */       throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + length() + ')');
/*  281:     */     }
/*  282: 417 */     System.arraycopy(this.value, srcIdx + this.offset, ObjectUtil.checkNotNull(dst, "dst"), dstIdx, length);
/*  283:     */   }
/*  284:     */   
/*  285:     */   public char charAt(int index)
/*  286:     */   {
/*  287: 422 */     return b2c(byteAt(index));
/*  288:     */   }
/*  289:     */   
/*  290:     */   public boolean contains(CharSequence cs)
/*  291:     */   {
/*  292: 432 */     return indexOf(cs) >= 0;
/*  293:     */   }
/*  294:     */   
/*  295:     */   public int compareTo(CharSequence string)
/*  296:     */   {
/*  297: 450 */     if (this == string) {
/*  298: 451 */       return 0;
/*  299:     */     }
/*  300: 455 */     int length1 = length();
/*  301: 456 */     int length2 = string.length();
/*  302: 457 */     int minLength = Math.min(length1, length2);
/*  303: 458 */     int i = 0;
/*  304: 458 */     for (int j = arrayOffset(); i < minLength; j++)
/*  305:     */     {
/*  306: 459 */       int result = b2c(this.value[j]) - string.charAt(i);
/*  307: 460 */       if (result != 0) {
/*  308: 461 */         return result;
/*  309:     */       }
/*  310: 458 */       i++;
/*  311:     */     }
/*  312: 465 */     return length1 - length2;
/*  313:     */   }
/*  314:     */   
/*  315:     */   public AsciiString concat(CharSequence string)
/*  316:     */   {
/*  317: 475 */     int thisLen = length();
/*  318: 476 */     int thatLen = string.length();
/*  319: 477 */     if (thatLen == 0) {
/*  320: 478 */       return this;
/*  321:     */     }
/*  322: 481 */     if (string.getClass() == AsciiString.class)
/*  323:     */     {
/*  324: 482 */       AsciiString that = (AsciiString)string;
/*  325: 483 */       if (isEmpty()) {
/*  326: 484 */         return that;
/*  327:     */       }
/*  328: 487 */       byte[] newValue = new byte[thisLen + thatLen];
/*  329: 488 */       System.arraycopy(this.value, arrayOffset(), newValue, 0, thisLen);
/*  330: 489 */       System.arraycopy(that.value, that.arrayOffset(), newValue, thisLen, thatLen);
/*  331: 490 */       return new AsciiString(newValue, false);
/*  332:     */     }
/*  333: 493 */     if (isEmpty()) {
/*  334: 494 */       return new AsciiString(string);
/*  335:     */     }
/*  336: 497 */     byte[] newValue = new byte[thisLen + thatLen];
/*  337: 498 */     System.arraycopy(this.value, arrayOffset(), newValue, 0, thisLen);
/*  338: 499 */     int i = thisLen;
/*  339: 499 */     for (int j = 0; i < newValue.length; j++)
/*  340:     */     {
/*  341: 500 */       newValue[i] = c2b(string.charAt(j));i++;
/*  342:     */     }
/*  343: 503 */     return new AsciiString(newValue, false);
/*  344:     */   }
/*  345:     */   
/*  346:     */   public boolean endsWith(CharSequence suffix)
/*  347:     */   {
/*  348: 514 */     int suffixLen = suffix.length();
/*  349: 515 */     return regionMatches(length() - suffixLen, suffix, 0, suffixLen);
/*  350:     */   }
/*  351:     */   
/*  352:     */   public boolean contentEqualsIgnoreCase(CharSequence string)
/*  353:     */   {
/*  354: 526 */     if ((string == null) || (string.length() != length())) {
/*  355: 527 */       return false;
/*  356:     */     }
/*  357: 530 */     if (string.getClass() == AsciiString.class)
/*  358:     */     {
/*  359: 531 */       AsciiString rhs = (AsciiString)string;
/*  360: 532 */       int i = arrayOffset();
/*  361: 532 */       for (int j = rhs.arrayOffset(); i < length(); j++)
/*  362:     */       {
/*  363: 533 */         if (!equalsIgnoreCase(this.value[i], rhs.value[j])) {
/*  364: 534 */           return false;
/*  365:     */         }
/*  366: 532 */         i++;
/*  367:     */       }
/*  368: 537 */       return true;
/*  369:     */     }
/*  370: 540 */     int i = arrayOffset();
/*  371: 540 */     for (int j = 0; i < length(); j++)
/*  372:     */     {
/*  373: 541 */       if (!equalsIgnoreCase(b2c(this.value[i]), string.charAt(j))) {
/*  374: 542 */         return false;
/*  375:     */       }
/*  376: 540 */       i++;
/*  377:     */     }
/*  378: 545 */     return true;
/*  379:     */   }
/*  380:     */   
/*  381:     */   public char[] toCharArray()
/*  382:     */   {
/*  383: 554 */     return toCharArray(0, length());
/*  384:     */   }
/*  385:     */   
/*  386:     */   public char[] toCharArray(int start, int end)
/*  387:     */   {
/*  388: 563 */     int length = end - start;
/*  389: 564 */     if (length == 0) {
/*  390: 565 */       return EmptyArrays.EMPTY_CHARS;
/*  391:     */     }
/*  392: 568 */     if (MathUtil.isOutOfBounds(start, length, length())) {
/*  393: 570 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= srcIdx + length(" + length + ") <= srcLen(" + length() + ')');
/*  394:     */     }
/*  395: 573 */     char[] buffer = new char[length];
/*  396: 574 */     int i = 0;
/*  397: 574 */     for (int j = start + arrayOffset(); i < length; j++)
/*  398:     */     {
/*  399: 575 */       buffer[i] = b2c(this.value[j]);i++;
/*  400:     */     }
/*  401: 577 */     return buffer;
/*  402:     */   }
/*  403:     */   
/*  404:     */   public void copy(int srcIdx, char[] dst, int dstIdx, int length)
/*  405:     */   {
/*  406: 589 */     if (dst == null) {
/*  407: 590 */       throw new NullPointerException("dst");
/*  408:     */     }
/*  409: 593 */     if (MathUtil.isOutOfBounds(srcIdx, length, length())) {
/*  410: 595 */       throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + length() + ')');
/*  411:     */     }
/*  412: 598 */     int dstEnd = dstIdx + length;
/*  413: 599 */     int i = dstIdx;
/*  414: 599 */     for (int j = srcIdx + arrayOffset(); i < dstEnd; j++)
/*  415:     */     {
/*  416: 600 */       dst[i] = b2c(this.value[j]);i++;
/*  417:     */     }
/*  418:     */   }
/*  419:     */   
/*  420:     */   public AsciiString subSequence(int start)
/*  421:     */   {
/*  422: 611 */     return subSequence(start, length());
/*  423:     */   }
/*  424:     */   
/*  425:     */   public AsciiString subSequence(int start, int end)
/*  426:     */   {
/*  427: 623 */     return subSequence(start, end, true);
/*  428:     */   }
/*  429:     */   
/*  430:     */   public AsciiString subSequence(int start, int end, boolean copy)
/*  431:     */   {
/*  432: 636 */     if (MathUtil.isOutOfBounds(start, end - start, length())) {
/*  433: 638 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= end (" + end + ") <= length(" + length() + ')');
/*  434:     */     }
/*  435: 641 */     if ((start == 0) && (end == length())) {
/*  436: 642 */       return this;
/*  437:     */     }
/*  438: 645 */     if (end == start) {
/*  439: 646 */       return EMPTY_STRING;
/*  440:     */     }
/*  441: 649 */     return new AsciiString(this.value, start + this.offset, end - start, copy);
/*  442:     */   }
/*  443:     */   
/*  444:     */   public int indexOf(CharSequence string)
/*  445:     */   {
/*  446: 662 */     return indexOf(string, 0);
/*  447:     */   }
/*  448:     */   
/*  449:     */   public int indexOf(CharSequence subString, int start)
/*  450:     */   {
/*  451: 676 */     if (start < 0) {
/*  452: 677 */       start = 0;
/*  453:     */     }
/*  454: 680 */     int thisLen = length();
/*  455:     */     
/*  456: 682 */     int subCount = subString.length();
/*  457: 683 */     if (subCount <= 0) {
/*  458: 684 */       return start < thisLen ? start : thisLen;
/*  459:     */     }
/*  460: 686 */     if (subCount > thisLen - start) {
/*  461: 687 */       return -1;
/*  462:     */     }
/*  463: 690 */     char firstChar = subString.charAt(0);
/*  464: 691 */     if (firstChar > 'ÿ') {
/*  465: 692 */       return -1;
/*  466:     */     }
/*  467: 694 */     ByteProcessor IndexOfVisitor = new ByteProcessor.IndexOfProcessor((byte)firstChar);
/*  468:     */     try
/*  469:     */     {
/*  470:     */       for (;;)
/*  471:     */       {
/*  472: 697 */         int i = forEachByte(start, thisLen - start, IndexOfVisitor);
/*  473: 698 */         if ((i == -1) || (subCount + i > thisLen)) {
/*  474: 699 */           return -1;
/*  475:     */         }
/*  476: 701 */         int o1 = i;int o2 = 0;
/*  477:     */         do
/*  478:     */         {
/*  479: 702 */           o2++;
/*  480: 702 */         } while ((o2 < subCount) && (b2c(this.value[(++o1 + arrayOffset())]) == subString.charAt(o2)));
/*  481: 705 */         if (o2 == subCount) {
/*  482: 706 */           return i;
/*  483:     */         }
/*  484: 708 */         start = i + 1;
/*  485:     */       }
/*  486: 712 */       return -1;
/*  487:     */     }
/*  488:     */     catch (Exception e)
/*  489:     */     {
/*  490: 711 */       PlatformDependent.throwException(e);
/*  491:     */     }
/*  492:     */   }
/*  493:     */   
/*  494:     */   public int indexOf(char ch, int start)
/*  495:     */   {
/*  496: 726 */     if (start < 0) {
/*  497: 727 */       start = 0;
/*  498:     */     }
/*  499: 730 */     int thisLen = length();
/*  500: 732 */     if (ch > 'ÿ') {
/*  501: 733 */       return -1;
/*  502:     */     }
/*  503:     */     try
/*  504:     */     {
/*  505: 737 */       return forEachByte(start, thisLen - start, new ByteProcessor.IndexOfProcessor((byte)ch));
/*  506:     */     }
/*  507:     */     catch (Exception e)
/*  508:     */     {
/*  509: 739 */       PlatformDependent.throwException(e);
/*  510:     */     }
/*  511: 740 */     return -1;
/*  512:     */   }
/*  513:     */   
/*  514:     */   public int lastIndexOf(CharSequence string)
/*  515:     */   {
/*  516: 755 */     return lastIndexOf(string, length());
/*  517:     */   }
/*  518:     */   
/*  519:     */   public int lastIndexOf(CharSequence subString, int start)
/*  520:     */   {
/*  521: 769 */     int thisLen = length();
/*  522: 770 */     int subCount = subString.length();
/*  523: 772 */     if ((subCount > thisLen) || (start < 0)) {
/*  524: 773 */       return -1;
/*  525:     */     }
/*  526: 776 */     if (subCount <= 0) {
/*  527: 777 */       return start < thisLen ? start : thisLen;
/*  528:     */     }
/*  529: 780 */     start = Math.min(start, thisLen - subCount);
/*  530:     */     
/*  531:     */ 
/*  532: 783 */     char firstChar = subString.charAt(0);
/*  533: 784 */     if (firstChar > 'ÿ') {
/*  534: 785 */       return -1;
/*  535:     */     }
/*  536: 787 */     ByteProcessor IndexOfVisitor = new ByteProcessor.IndexOfProcessor((byte)firstChar);
/*  537:     */     try
/*  538:     */     {
/*  539:     */       for (;;)
/*  540:     */       {
/*  541: 790 */         int i = forEachByteDesc(start, thisLen - start, IndexOfVisitor);
/*  542: 791 */         if (i == -1) {
/*  543: 792 */           return -1;
/*  544:     */         }
/*  545: 794 */         int o1 = i;int o2 = 0;
/*  546:     */         do
/*  547:     */         {
/*  548: 795 */           o2++;
/*  549: 795 */         } while ((o2 < subCount) && (b2c(this.value[(++o1 + arrayOffset())]) == subString.charAt(o2)));
/*  550: 798 */         if (o2 == subCount) {
/*  551: 799 */           return i;
/*  552:     */         }
/*  553: 801 */         start = i - 1;
/*  554:     */       }
/*  555: 805 */       return -1;
/*  556:     */     }
/*  557:     */     catch (Exception e)
/*  558:     */     {
/*  559: 804 */       PlatformDependent.throwException(e);
/*  560:     */     }
/*  561:     */   }
/*  562:     */   
/*  563:     */   public boolean regionMatches(int thisStart, CharSequence string, int start, int length)
/*  564:     */   {
/*  565: 821 */     if (string == null) {
/*  566: 822 */       throw new NullPointerException("string");
/*  567:     */     }
/*  568: 825 */     if ((start < 0) || (string.length() - start < length)) {
/*  569: 826 */       return false;
/*  570:     */     }
/*  571: 829 */     int thisLen = length();
/*  572: 830 */     if ((thisStart < 0) || (thisLen - thisStart < length)) {
/*  573: 831 */       return false;
/*  574:     */     }
/*  575: 834 */     if (length <= 0) {
/*  576: 835 */       return true;
/*  577:     */     }
/*  578: 838 */     int thatEnd = start + length;
/*  579: 839 */     int i = start;
/*  580: 839 */     for (int j = thisStart + arrayOffset(); i < thatEnd; j++)
/*  581:     */     {
/*  582: 840 */       if (b2c(this.value[j]) != string.charAt(i)) {
/*  583: 841 */         return false;
/*  584:     */       }
/*  585: 839 */       i++;
/*  586:     */     }
/*  587: 844 */     return true;
/*  588:     */   }
/*  589:     */   
/*  590:     */   public boolean regionMatches(boolean ignoreCase, int thisStart, CharSequence string, int start, int length)
/*  591:     */   {
/*  592: 860 */     if (!ignoreCase) {
/*  593: 861 */       return regionMatches(thisStart, string, start, length);
/*  594:     */     }
/*  595: 864 */     if (string == null) {
/*  596: 865 */       throw new NullPointerException("string");
/*  597:     */     }
/*  598: 868 */     int thisLen = length();
/*  599: 869 */     if ((thisStart < 0) || (length > thisLen - thisStart)) {
/*  600: 870 */       return false;
/*  601:     */     }
/*  602: 872 */     if ((start < 0) || (length > string.length() - start)) {
/*  603: 873 */       return false;
/*  604:     */     }
/*  605: 876 */     thisStart += arrayOffset();
/*  606: 877 */     int thisEnd = thisStart + length;
/*  607: 878 */     while (thisStart < thisEnd) {
/*  608: 879 */       if (!equalsIgnoreCase(b2c(this.value[(thisStart++)]), string.charAt(start++))) {
/*  609: 880 */         return false;
/*  610:     */       }
/*  611:     */     }
/*  612: 883 */     return true;
/*  613:     */   }
/*  614:     */   
/*  615:     */   public AsciiString replace(char oldChar, char newChar)
/*  616:     */   {
/*  617: 894 */     if (oldChar > 'ÿ') {
/*  618: 895 */       return this;
/*  619:     */     }
/*  620: 899 */     byte oldCharByte = c2b(oldChar);
/*  621:     */     try
/*  622:     */     {
/*  623: 901 */       index = forEachByte(new ByteProcessor.IndexOfProcessor(oldCharByte));
/*  624:     */     }
/*  625:     */     catch (Exception e)
/*  626:     */     {
/*  627:     */       int index;
/*  628: 903 */       PlatformDependent.throwException(e);
/*  629: 904 */       return this;
/*  630:     */     }
/*  631:     */     int index;
/*  632: 906 */     if (index == -1) {
/*  633: 907 */       return this;
/*  634:     */     }
/*  635: 910 */     byte newCharByte = c2b(newChar);
/*  636: 911 */     byte[] buffer = new byte[length()];
/*  637: 912 */     int i = 0;
/*  638: 912 */     for (int j = arrayOffset(); i < buffer.length; j++)
/*  639:     */     {
/*  640: 913 */       byte b = this.value[j];
/*  641: 914 */       if (b == oldCharByte) {
/*  642: 915 */         b = newCharByte;
/*  643:     */       }
/*  644: 917 */       buffer[i] = b;i++;
/*  645:     */     }
/*  646: 920 */     return new AsciiString(buffer, false);
/*  647:     */   }
/*  648:     */   
/*  649:     */   public boolean startsWith(CharSequence prefix)
/*  650:     */   {
/*  651: 931 */     return startsWith(prefix, 0);
/*  652:     */   }
/*  653:     */   
/*  654:     */   public boolean startsWith(CharSequence prefix, int start)
/*  655:     */   {
/*  656: 945 */     return regionMatches(start, prefix, 0, prefix.length());
/*  657:     */   }
/*  658:     */   
/*  659:     */   public AsciiString toLowerCase()
/*  660:     */   {
/*  661: 954 */     boolean lowercased = true;
/*  662:     */     
/*  663: 956 */     int len = length() + arrayOffset();
/*  664: 957 */     for (int i = arrayOffset(); i < len; i++)
/*  665:     */     {
/*  666: 958 */       byte b = this.value[i];
/*  667: 959 */       if ((b >= 65) && (b <= 90))
/*  668:     */       {
/*  669: 960 */         lowercased = false;
/*  670: 961 */         break;
/*  671:     */       }
/*  672:     */     }
/*  673: 966 */     if (lowercased) {
/*  674: 967 */       return this;
/*  675:     */     }
/*  676: 970 */     byte[] newValue = new byte[length()];
/*  677: 971 */     i = 0;
/*  678: 971 */     for (int j = arrayOffset(); i < newValue.length; j++)
/*  679:     */     {
/*  680: 972 */       newValue[i] = toLowerCase(this.value[j]);i++;
/*  681:     */     }
/*  682: 975 */     return new AsciiString(newValue, false);
/*  683:     */   }
/*  684:     */   
/*  685:     */   public AsciiString toUpperCase()
/*  686:     */   {
/*  687: 984 */     boolean uppercased = true;
/*  688:     */     
/*  689: 986 */     int len = length() + arrayOffset();
/*  690: 987 */     for (int i = arrayOffset(); i < len; i++)
/*  691:     */     {
/*  692: 988 */       byte b = this.value[i];
/*  693: 989 */       if ((b >= 97) && (b <= 122))
/*  694:     */       {
/*  695: 990 */         uppercased = false;
/*  696: 991 */         break;
/*  697:     */       }
/*  698:     */     }
/*  699: 996 */     if (uppercased) {
/*  700: 997 */       return this;
/*  701:     */     }
/*  702:1000 */     byte[] newValue = new byte[length()];
/*  703:1001 */     i = 0;
/*  704:1001 */     for (int j = arrayOffset(); i < newValue.length; j++)
/*  705:     */     {
/*  706:1002 */       newValue[i] = toUpperCase(this.value[j]);i++;
/*  707:     */     }
/*  708:1005 */     return new AsciiString(newValue, false);
/*  709:     */   }
/*  710:     */   
/*  711:     */   public static CharSequence trim(CharSequence c)
/*  712:     */   {
/*  713:1016 */     if (c.getClass() == AsciiString.class) {
/*  714:1017 */       return ((AsciiString)c).trim();
/*  715:     */     }
/*  716:1019 */     if ((c instanceof String)) {
/*  717:1020 */       return ((String)c).trim();
/*  718:     */     }
/*  719:1022 */     int start = 0;int last = c.length() - 1;
/*  720:1023 */     int end = last;
/*  721:1024 */     while ((start <= end) && (c.charAt(start) <= ' ')) {
/*  722:1025 */       start++;
/*  723:     */     }
/*  724:1027 */     while ((end >= start) && (c.charAt(end) <= ' ')) {
/*  725:1028 */       end--;
/*  726:     */     }
/*  727:1030 */     if ((start == 0) && (end == last)) {
/*  728:1031 */       return c;
/*  729:     */     }
/*  730:1033 */     return c.subSequence(start, end);
/*  731:     */   }
/*  732:     */   
/*  733:     */   public AsciiString trim()
/*  734:     */   {
/*  735:1043 */     int start = arrayOffset();int last = arrayOffset() + length() - 1;
/*  736:1044 */     int end = last;
/*  737:1045 */     while ((start <= end) && (this.value[start] <= 32)) {
/*  738:1046 */       start++;
/*  739:     */     }
/*  740:1048 */     while ((end >= start) && (this.value[end] <= 32)) {
/*  741:1049 */       end--;
/*  742:     */     }
/*  743:1051 */     if ((start == 0) && (end == last)) {
/*  744:1052 */       return this;
/*  745:     */     }
/*  746:1054 */     return new AsciiString(this.value, start, end - start + 1, false);
/*  747:     */   }
/*  748:     */   
/*  749:     */   public boolean contentEquals(CharSequence a)
/*  750:     */   {
/*  751:1064 */     if ((a == null) || (a.length() != length())) {
/*  752:1065 */       return false;
/*  753:     */     }
/*  754:1067 */     if (a.getClass() == AsciiString.class) {
/*  755:1068 */       return equals(a);
/*  756:     */     }
/*  757:1071 */     int i = arrayOffset();
/*  758:1071 */     for (int j = 0; j < a.length(); j++)
/*  759:     */     {
/*  760:1072 */       if (b2c(this.value[i]) != a.charAt(j)) {
/*  761:1073 */         return false;
/*  762:     */       }
/*  763:1071 */       i++;
/*  764:     */     }
/*  765:1076 */     return true;
/*  766:     */   }
/*  767:     */   
/*  768:     */   public boolean matches(String expr)
/*  769:     */   {
/*  770:1088 */     return Pattern.matches(expr, this);
/*  771:     */   }
/*  772:     */   
/*  773:     */   public AsciiString[] split(String expr, int max)
/*  774:     */   {
/*  775:1103 */     return toAsciiStringArray(Pattern.compile(expr).split(this, max));
/*  776:     */   }
/*  777:     */   
/*  778:     */   public AsciiString[] split(char delim)
/*  779:     */   {
/*  780:1110 */     List<AsciiString> res = InternalThreadLocalMap.get().arrayList();
/*  781:     */     
/*  782:1112 */     int start = 0;
/*  783:1113 */     int length = length();
/*  784:1114 */     for (int i = start; i < length; i++) {
/*  785:1115 */       if (charAt(i) == delim)
/*  786:     */       {
/*  787:1116 */         if (start == i) {
/*  788:1117 */           res.add(EMPTY_STRING);
/*  789:     */         } else {
/*  790:1119 */           res.add(new AsciiString(this.value, start + arrayOffset(), i - start, false));
/*  791:     */         }
/*  792:1121 */         start = i + 1;
/*  793:     */       }
/*  794:     */     }
/*  795:1125 */     if (start == 0) {
/*  796:1126 */       res.add(this);
/*  797:1128 */     } else if (start != length) {
/*  798:1130 */       res.add(new AsciiString(this.value, start + arrayOffset(), length - start, false));
/*  799:     */     } else {
/*  800:1133 */       for (int i = res.size() - 1; i >= 0; i--)
/*  801:     */       {
/*  802:1134 */         if (!((AsciiString)res.get(i)).isEmpty()) {
/*  803:     */           break;
/*  804:     */         }
/*  805:1135 */         res.remove(i);
/*  806:     */       }
/*  807:     */     }
/*  808:1143 */     return (AsciiString[])res.toArray(new AsciiString[res.size()]);
/*  809:     */   }
/*  810:     */   
/*  811:     */   public int hashCode()
/*  812:     */   {
/*  813:1153 */     int h = this.hash;
/*  814:1154 */     if (h == 0)
/*  815:     */     {
/*  816:1155 */       h = PlatformDependent.hashCodeAscii(this.value, this.offset, this.length);
/*  817:1156 */       this.hash = h;
/*  818:     */     }
/*  819:1158 */     return h;
/*  820:     */   }
/*  821:     */   
/*  822:     */   public boolean equals(Object obj)
/*  823:     */   {
/*  824:1163 */     if ((obj == null) || (obj.getClass() != AsciiString.class)) {
/*  825:1164 */       return false;
/*  826:     */     }
/*  827:1166 */     if (this == obj) {
/*  828:1167 */       return true;
/*  829:     */     }
/*  830:1170 */     AsciiString other = (AsciiString)obj;
/*  831:1171 */     return (length() == other.length()) && 
/*  832:1172 */       (hashCode() == other.hashCode()) && 
/*  833:1173 */       (PlatformDependent.equals(array(), arrayOffset(), other.array(), other.arrayOffset(), length()));
/*  834:     */   }
/*  835:     */   
/*  836:     */   public String toString()
/*  837:     */   {
/*  838:1182 */     String cache = this.string;
/*  839:1183 */     if (cache == null)
/*  840:     */     {
/*  841:1184 */       cache = toString(0);
/*  842:1185 */       this.string = cache;
/*  843:     */     }
/*  844:1187 */     return cache;
/*  845:     */   }
/*  846:     */   
/*  847:     */   public String toString(int start)
/*  848:     */   {
/*  849:1195 */     return toString(start, length());
/*  850:     */   }
/*  851:     */   
/*  852:     */   public String toString(int start, int end)
/*  853:     */   {
/*  854:1202 */     int length = end - start;
/*  855:1203 */     if (length == 0) {
/*  856:1204 */       return "";
/*  857:     */     }
/*  858:1207 */     if (MathUtil.isOutOfBounds(start, length, length())) {
/*  859:1209 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= srcIdx + length(" + length + ") <= srcLen(" + length() + ')');
/*  860:     */     }
/*  861:1213 */     String str = new String(this.value, 0, start + this.offset, length);
/*  862:1214 */     return str;
/*  863:     */   }
/*  864:     */   
/*  865:     */   public boolean parseBoolean()
/*  866:     */   {
/*  867:1218 */     return (this.length >= 1) && (this.value[this.offset] != 0);
/*  868:     */   }
/*  869:     */   
/*  870:     */   public char parseChar()
/*  871:     */   {
/*  872:1222 */     return parseChar(0);
/*  873:     */   }
/*  874:     */   
/*  875:     */   public char parseChar(int start)
/*  876:     */   {
/*  877:1226 */     if (start + 1 >= length()) {
/*  878:1227 */       throw new IndexOutOfBoundsException("2 bytes required to convert to character. index " + start + " would go out of bounds.");
/*  879:     */     }
/*  880:1230 */     int startWithOffset = start + this.offset;
/*  881:1231 */     return (char)(b2c(this.value[startWithOffset]) << '\b' | b2c(this.value[(startWithOffset + 1)]));
/*  882:     */   }
/*  883:     */   
/*  884:     */   public short parseShort()
/*  885:     */   {
/*  886:1235 */     return parseShort(0, length(), 10);
/*  887:     */   }
/*  888:     */   
/*  889:     */   public short parseShort(int radix)
/*  890:     */   {
/*  891:1239 */     return parseShort(0, length(), radix);
/*  892:     */   }
/*  893:     */   
/*  894:     */   public short parseShort(int start, int end)
/*  895:     */   {
/*  896:1243 */     return parseShort(start, end, 10);
/*  897:     */   }
/*  898:     */   
/*  899:     */   public short parseShort(int start, int end, int radix)
/*  900:     */   {
/*  901:1247 */     int intValue = parseInt(start, end, radix);
/*  902:1248 */     short result = (short)intValue;
/*  903:1249 */     if (result != intValue) {
/*  904:1250 */       throw new NumberFormatException(subSequence(start, end, false).toString());
/*  905:     */     }
/*  906:1252 */     return result;
/*  907:     */   }
/*  908:     */   
/*  909:     */   public int parseInt()
/*  910:     */   {
/*  911:1256 */     return parseInt(0, length(), 10);
/*  912:     */   }
/*  913:     */   
/*  914:     */   public int parseInt(int radix)
/*  915:     */   {
/*  916:1260 */     return parseInt(0, length(), radix);
/*  917:     */   }
/*  918:     */   
/*  919:     */   public int parseInt(int start, int end)
/*  920:     */   {
/*  921:1264 */     return parseInt(start, end, 10);
/*  922:     */   }
/*  923:     */   
/*  924:     */   public int parseInt(int start, int end, int radix)
/*  925:     */   {
/*  926:1268 */     if ((radix < 2) || (radix > 36)) {
/*  927:1269 */       throw new NumberFormatException();
/*  928:     */     }
/*  929:1272 */     if (start == end) {
/*  930:1273 */       throw new NumberFormatException();
/*  931:     */     }
/*  932:1276 */     int i = start;
/*  933:1277 */     boolean negative = byteAt(i) == 45;
/*  934:1278 */     if (negative)
/*  935:     */     {
/*  936:1278 */       i++;
/*  937:1278 */       if (i == end) {
/*  938:1279 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/*  939:     */       }
/*  940:     */     }
/*  941:1282 */     return parseInt(i, end, radix, negative);
/*  942:     */   }
/*  943:     */   
/*  944:     */   private int parseInt(int start, int end, int radix, boolean negative)
/*  945:     */   {
/*  946:1286 */     int max = -2147483648 / radix;
/*  947:1287 */     int result = 0;
/*  948:1288 */     int currOffset = start;
/*  949:1289 */     while (currOffset < end)
/*  950:     */     {
/*  951:1290 */       int digit = Character.digit((char)(this.value[(currOffset++ + this.offset)] & 0xFF), radix);
/*  952:1291 */       if (digit == -1) {
/*  953:1292 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/*  954:     */       }
/*  955:1294 */       if (max > result) {
/*  956:1295 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/*  957:     */       }
/*  958:1297 */       int next = result * radix - digit;
/*  959:1298 */       if (next > result) {
/*  960:1299 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/*  961:     */       }
/*  962:1301 */       result = next;
/*  963:     */     }
/*  964:1303 */     if (!negative)
/*  965:     */     {
/*  966:1304 */       result = -result;
/*  967:1305 */       if (result < 0) {
/*  968:1306 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/*  969:     */       }
/*  970:     */     }
/*  971:1309 */     return result;
/*  972:     */   }
/*  973:     */   
/*  974:     */   public long parseLong()
/*  975:     */   {
/*  976:1313 */     return parseLong(0, length(), 10);
/*  977:     */   }
/*  978:     */   
/*  979:     */   public long parseLong(int radix)
/*  980:     */   {
/*  981:1317 */     return parseLong(0, length(), radix);
/*  982:     */   }
/*  983:     */   
/*  984:     */   public long parseLong(int start, int end)
/*  985:     */   {
/*  986:1321 */     return parseLong(start, end, 10);
/*  987:     */   }
/*  988:     */   
/*  989:     */   public long parseLong(int start, int end, int radix)
/*  990:     */   {
/*  991:1325 */     if ((radix < 2) || (radix > 36)) {
/*  992:1326 */       throw new NumberFormatException();
/*  993:     */     }
/*  994:1329 */     if (start == end) {
/*  995:1330 */       throw new NumberFormatException();
/*  996:     */     }
/*  997:1333 */     int i = start;
/*  998:1334 */     boolean negative = byteAt(i) == 45;
/*  999:1335 */     if (negative)
/* 1000:     */     {
/* 1001:1335 */       i++;
/* 1002:1335 */       if (i == end) {
/* 1003:1336 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/* 1004:     */       }
/* 1005:     */     }
/* 1006:1339 */     return parseLong(i, end, radix, negative);
/* 1007:     */   }
/* 1008:     */   
/* 1009:     */   private long parseLong(int start, int end, int radix, boolean negative)
/* 1010:     */   {
/* 1011:1343 */     long max = -9223372036854775808L / radix;
/* 1012:1344 */     long result = 0L;
/* 1013:1345 */     int currOffset = start;
/* 1014:1346 */     while (currOffset < end)
/* 1015:     */     {
/* 1016:1347 */       int digit = Character.digit((char)(this.value[(currOffset++ + this.offset)] & 0xFF), radix);
/* 1017:1348 */       if (digit == -1) {
/* 1018:1349 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/* 1019:     */       }
/* 1020:1351 */       if (max > result) {
/* 1021:1352 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/* 1022:     */       }
/* 1023:1354 */       long next = result * radix - digit;
/* 1024:1355 */       if (next > result) {
/* 1025:1356 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/* 1026:     */       }
/* 1027:1358 */       result = next;
/* 1028:     */     }
/* 1029:1360 */     if (!negative)
/* 1030:     */     {
/* 1031:1361 */       result = -result;
/* 1032:1362 */       if (result < 0L) {
/* 1033:1363 */         throw new NumberFormatException(subSequence(start, end, false).toString());
/* 1034:     */       }
/* 1035:     */     }
/* 1036:1366 */     return result;
/* 1037:     */   }
/* 1038:     */   
/* 1039:     */   public float parseFloat()
/* 1040:     */   {
/* 1041:1370 */     return parseFloat(0, length());
/* 1042:     */   }
/* 1043:     */   
/* 1044:     */   public float parseFloat(int start, int end)
/* 1045:     */   {
/* 1046:1374 */     return Float.parseFloat(toString(start, end));
/* 1047:     */   }
/* 1048:     */   
/* 1049:     */   public double parseDouble()
/* 1050:     */   {
/* 1051:1378 */     return parseDouble(0, length());
/* 1052:     */   }
/* 1053:     */   
/* 1054:     */   public double parseDouble(int start, int end)
/* 1055:     */   {
/* 1056:1382 */     return Double.parseDouble(toString(start, end));
/* 1057:     */   }
/* 1058:     */   
/* 1059:1385 */   public static final HashingStrategy<CharSequence> CASE_INSENSITIVE_HASHER = new HashingStrategy()
/* 1060:     */   {
/* 1061:     */     public int hashCode(CharSequence o)
/* 1062:     */     {
/* 1063:1389 */       return AsciiString.hashCode(o);
/* 1064:     */     }
/* 1065:     */     
/* 1066:     */     public boolean equals(CharSequence a, CharSequence b)
/* 1067:     */     {
/* 1068:1394 */       return AsciiString.contentEqualsIgnoreCase(a, b);
/* 1069:     */     }
/* 1070:     */   };
/* 1071:1398 */   public static final HashingStrategy<CharSequence> CASE_SENSITIVE_HASHER = new HashingStrategy()
/* 1072:     */   {
/* 1073:     */     public int hashCode(CharSequence o)
/* 1074:     */     {
/* 1075:1402 */       return AsciiString.hashCode(o);
/* 1076:     */     }
/* 1077:     */     
/* 1078:     */     public boolean equals(CharSequence a, CharSequence b)
/* 1079:     */     {
/* 1080:1407 */       return AsciiString.contentEquals(a, b);
/* 1081:     */     }
/* 1082:     */   };
/* 1083:     */   
/* 1084:     */   public static AsciiString of(CharSequence string)
/* 1085:     */   {
/* 1086:1416 */     return string.getClass() == AsciiString.class ? (AsciiString)string : new AsciiString(string);
/* 1087:     */   }
/* 1088:     */   
/* 1089:     */   public static AsciiString cached(String string)
/* 1090:     */   {
/* 1091:1426 */     AsciiString asciiString = new AsciiString(string);
/* 1092:1427 */     asciiString.string = string;
/* 1093:1428 */     return asciiString;
/* 1094:     */   }
/* 1095:     */   
/* 1096:     */   public static int hashCode(CharSequence value)
/* 1097:     */   {
/* 1098:1437 */     if (value == null) {
/* 1099:1438 */       return 0;
/* 1100:     */     }
/* 1101:1440 */     if (value.getClass() == AsciiString.class) {
/* 1102:1441 */       return value.hashCode();
/* 1103:     */     }
/* 1104:1444 */     return PlatformDependent.hashCodeAscii(value);
/* 1105:     */   }
/* 1106:     */   
/* 1107:     */   public static boolean contains(CharSequence a, CharSequence b)
/* 1108:     */   {
/* 1109:1451 */     return contains(a, b, DefaultCharEqualityComparator.INSTANCE);
/* 1110:     */   }
/* 1111:     */   
/* 1112:     */   public static boolean containsIgnoreCase(CharSequence a, CharSequence b)
/* 1113:     */   {
/* 1114:1458 */     return contains(a, b, AsciiCaseInsensitiveCharEqualityComparator.INSTANCE);
/* 1115:     */   }
/* 1116:     */   
/* 1117:     */   public static boolean contentEqualsIgnoreCase(CharSequence a, CharSequence b)
/* 1118:     */   {
/* 1119:1466 */     if ((a == null) || (b == null)) {
/* 1120:1467 */       return a == b;
/* 1121:     */     }
/* 1122:1470 */     if (a.getClass() == AsciiString.class) {
/* 1123:1471 */       return ((AsciiString)a).contentEqualsIgnoreCase(b);
/* 1124:     */     }
/* 1125:1473 */     if (b.getClass() == AsciiString.class) {
/* 1126:1474 */       return ((AsciiString)b).contentEqualsIgnoreCase(a);
/* 1127:     */     }
/* 1128:1477 */     if (a.length() != b.length()) {
/* 1129:1478 */       return false;
/* 1130:     */     }
/* 1131:1480 */     int i = 0;
/* 1132:1480 */     for (int j = 0; i < a.length(); j++)
/* 1133:     */     {
/* 1134:1481 */       if (!equalsIgnoreCase(a.charAt(i), b.charAt(j))) {
/* 1135:1482 */         return false;
/* 1136:     */       }
/* 1137:1480 */       i++;
/* 1138:     */     }
/* 1139:1485 */     return true;
/* 1140:     */   }
/* 1141:     */   
/* 1142:     */   public static boolean containsContentEqualsIgnoreCase(Collection<CharSequence> collection, CharSequence value)
/* 1143:     */   {
/* 1144:1498 */     for (CharSequence v : collection) {
/* 1145:1499 */       if (contentEqualsIgnoreCase(value, v)) {
/* 1146:1500 */         return true;
/* 1147:     */       }
/* 1148:     */     }
/* 1149:1503 */     return false;
/* 1150:     */   }
/* 1151:     */   
/* 1152:     */   public static boolean containsAllContentEqualsIgnoreCase(Collection<CharSequence> a, Collection<CharSequence> b)
/* 1153:     */   {
/* 1154:1516 */     for (CharSequence v : b) {
/* 1155:1517 */       if (!containsContentEqualsIgnoreCase(a, v)) {
/* 1156:1518 */         return false;
/* 1157:     */       }
/* 1158:     */     }
/* 1159:1521 */     return true;
/* 1160:     */   }
/* 1161:     */   
/* 1162:     */   public static boolean contentEquals(CharSequence a, CharSequence b)
/* 1163:     */   {
/* 1164:1528 */     if ((a == null) || (b == null)) {
/* 1165:1529 */       return a == b;
/* 1166:     */     }
/* 1167:1532 */     if (a.getClass() == AsciiString.class) {
/* 1168:1533 */       return ((AsciiString)a).contentEquals(b);
/* 1169:     */     }
/* 1170:1536 */     if (b.getClass() == AsciiString.class) {
/* 1171:1537 */       return ((AsciiString)b).contentEquals(a);
/* 1172:     */     }
/* 1173:1540 */     if (a.length() != b.length()) {
/* 1174:1541 */       return false;
/* 1175:     */     }
/* 1176:1543 */     for (int i = 0; i < a.length(); i++) {
/* 1177:1544 */       if (a.charAt(i) != b.charAt(i)) {
/* 1178:1545 */         return false;
/* 1179:     */       }
/* 1180:     */     }
/* 1181:1548 */     return true;
/* 1182:     */   }
/* 1183:     */   
/* 1184:     */   private static AsciiString[] toAsciiStringArray(String[] jdkResult)
/* 1185:     */   {
/* 1186:1552 */     AsciiString[] res = new AsciiString[jdkResult.length];
/* 1187:1553 */     for (int i = 0; i < jdkResult.length; i++) {
/* 1188:1554 */       res[i] = new AsciiString(jdkResult[i]);
/* 1189:     */     }
/* 1190:1556 */     return res;
/* 1191:     */   }
/* 1192:     */   
/* 1193:     */   private static abstract interface CharEqualityComparator
/* 1194:     */   {
/* 1195:     */     public abstract boolean equals(char paramChar1, char paramChar2);
/* 1196:     */   }
/* 1197:     */   
/* 1198:     */   private static final class DefaultCharEqualityComparator
/* 1199:     */     implements AsciiString.CharEqualityComparator
/* 1200:     */   {
/* 1201:1564 */     static final DefaultCharEqualityComparator INSTANCE = new DefaultCharEqualityComparator();
/* 1202:     */     
/* 1203:     */     public boolean equals(char a, char b)
/* 1204:     */     {
/* 1205:1569 */       return a == b;
/* 1206:     */     }
/* 1207:     */   }
/* 1208:     */   
/* 1209:     */   private static final class AsciiCaseInsensitiveCharEqualityComparator
/* 1210:     */     implements AsciiString.CharEqualityComparator
/* 1211:     */   {
/* 1212:1575 */     static final AsciiCaseInsensitiveCharEqualityComparator INSTANCE = new AsciiCaseInsensitiveCharEqualityComparator();
/* 1213:     */     
/* 1214:     */     public boolean equals(char a, char b)
/* 1215:     */     {
/* 1216:1580 */       return AsciiString.equalsIgnoreCase(a, b);
/* 1217:     */     }
/* 1218:     */   }
/* 1219:     */   
/* 1220:     */   private static final class GeneralCaseInsensitiveCharEqualityComparator
/* 1221:     */     implements AsciiString.CharEqualityComparator
/* 1222:     */   {
/* 1223:1586 */     static final GeneralCaseInsensitiveCharEqualityComparator INSTANCE = new GeneralCaseInsensitiveCharEqualityComparator();
/* 1224:     */     
/* 1225:     */     public boolean equals(char a, char b)
/* 1226:     */     {
/* 1227:1592 */       return (Character.toUpperCase(a) == Character.toUpperCase(b)) || 
/* 1228:1593 */         (Character.toLowerCase(a) == Character.toLowerCase(b));
/* 1229:     */     }
/* 1230:     */   }
/* 1231:     */   
/* 1232:     */   private static boolean contains(CharSequence a, CharSequence b, CharEqualityComparator cmp)
/* 1233:     */   {
/* 1234:1598 */     if ((a == null) || (b == null) || (a.length() < b.length())) {
/* 1235:1599 */       return false;
/* 1236:     */     }
/* 1237:1601 */     if (b.length() == 0) {
/* 1238:1602 */       return true;
/* 1239:     */     }
/* 1240:1604 */     int bStart = 0;
/* 1241:1605 */     for (int i = 0; i < a.length(); i++) {
/* 1242:1606 */       if (cmp.equals(b.charAt(bStart), a.charAt(i)))
/* 1243:     */       {
/* 1244:1608 */         bStart++;
/* 1245:1608 */         if (bStart == b.length()) {
/* 1246:1609 */           return true;
/* 1247:     */         }
/* 1248:     */       }
/* 1249:     */       else
/* 1250:     */       {
/* 1251:1611 */         if (a.length() - i < b.length()) {
/* 1252:1613 */           return false;
/* 1253:     */         }
/* 1254:1615 */         bStart = 0;
/* 1255:     */       }
/* 1256:     */     }
/* 1257:1618 */     return false;
/* 1258:     */   }
/* 1259:     */   
/* 1260:     */   private static boolean regionMatchesCharSequences(CharSequence cs, int csStart, CharSequence string, int start, int length, CharEqualityComparator charEqualityComparator)
/* 1261:     */   {
/* 1262:1625 */     if ((csStart < 0) || (length > cs.length() - csStart)) {
/* 1263:1626 */       return false;
/* 1264:     */     }
/* 1265:1628 */     if ((start < 0) || (length > string.length() - start)) {
/* 1266:1629 */       return false;
/* 1267:     */     }
/* 1268:1632 */     int csIndex = csStart;
/* 1269:1633 */     int csEnd = csIndex + length;
/* 1270:1634 */     int stringIndex = start;
/* 1271:1636 */     while (csIndex < csEnd)
/* 1272:     */     {
/* 1273:1637 */       char c1 = cs.charAt(csIndex++);
/* 1274:1638 */       char c2 = string.charAt(stringIndex++);
/* 1275:1640 */       if (!charEqualityComparator.equals(c1, c2)) {
/* 1276:1641 */         return false;
/* 1277:     */       }
/* 1278:     */     }
/* 1279:1644 */     return true;
/* 1280:     */   }
/* 1281:     */   
/* 1282:     */   public static boolean regionMatches(CharSequence cs, boolean ignoreCase, int csStart, CharSequence string, int start, int length)
/* 1283:     */   {
/* 1284:1659 */     if ((cs == null) || (string == null)) {
/* 1285:1660 */       return false;
/* 1286:     */     }
/* 1287:1663 */     if (((cs instanceof String)) && ((string instanceof String))) {
/* 1288:1664 */       return ((String)cs).regionMatches(ignoreCase, csStart, (String)string, start, length);
/* 1289:     */     }
/* 1290:1667 */     if ((cs instanceof AsciiString)) {
/* 1291:1668 */       return ((AsciiString)cs).regionMatches(ignoreCase, csStart, string, start, length);
/* 1292:     */     }
/* 1293:1671 */     return regionMatchesCharSequences(cs, csStart, string, start, length, ignoreCase ? GeneralCaseInsensitiveCharEqualityComparator.INSTANCE : DefaultCharEqualityComparator.INSTANCE);
/* 1294:     */   }
/* 1295:     */   
/* 1296:     */   public static boolean regionMatchesAscii(CharSequence cs, boolean ignoreCase, int csStart, CharSequence string, int start, int length)
/* 1297:     */   {
/* 1298:1688 */     if ((cs == null) || (string == null)) {
/* 1299:1689 */       return false;
/* 1300:     */     }
/* 1301:1692 */     if ((!ignoreCase) && ((cs instanceof String)) && ((string instanceof String))) {
/* 1302:1696 */       return ((String)cs).regionMatches(false, csStart, (String)string, start, length);
/* 1303:     */     }
/* 1304:1699 */     if ((cs instanceof AsciiString)) {
/* 1305:1700 */       return ((AsciiString)cs).regionMatches(ignoreCase, csStart, string, start, length);
/* 1306:     */     }
/* 1307:1703 */     return regionMatchesCharSequences(cs, csStart, string, start, length, ignoreCase ? AsciiCaseInsensitiveCharEqualityComparator.INSTANCE : DefaultCharEqualityComparator.INSTANCE);
/* 1308:     */   }
/* 1309:     */   
/* 1310:     */   public static int indexOfIgnoreCase(CharSequence str, CharSequence searchStr, int startPos)
/* 1311:     */   {
/* 1312:1739 */     if ((str == null) || (searchStr == null)) {
/* 1313:1740 */       return -1;
/* 1314:     */     }
/* 1315:1742 */     if (startPos < 0) {
/* 1316:1743 */       startPos = 0;
/* 1317:     */     }
/* 1318:1745 */     int searchStrLen = searchStr.length();
/* 1319:1746 */     int endLimit = str.length() - searchStrLen + 1;
/* 1320:1747 */     if (startPos > endLimit) {
/* 1321:1748 */       return -1;
/* 1322:     */     }
/* 1323:1750 */     if (searchStrLen == 0) {
/* 1324:1751 */       return startPos;
/* 1325:     */     }
/* 1326:1753 */     for (int i = startPos; i < endLimit; i++) {
/* 1327:1754 */       if (regionMatches(str, true, i, searchStr, 0, searchStrLen)) {
/* 1328:1755 */         return i;
/* 1329:     */       }
/* 1330:     */     }
/* 1331:1758 */     return -1;
/* 1332:     */   }
/* 1333:     */   
/* 1334:     */   public static int indexOfIgnoreCaseAscii(CharSequence str, CharSequence searchStr, int startPos)
/* 1335:     */   {
/* 1336:1792 */     if ((str == null) || (searchStr == null)) {
/* 1337:1793 */       return -1;
/* 1338:     */     }
/* 1339:1795 */     if (startPos < 0) {
/* 1340:1796 */       startPos = 0;
/* 1341:     */     }
/* 1342:1798 */     int searchStrLen = searchStr.length();
/* 1343:1799 */     int endLimit = str.length() - searchStrLen + 1;
/* 1344:1800 */     if (startPos > endLimit) {
/* 1345:1801 */       return -1;
/* 1346:     */     }
/* 1347:1803 */     if (searchStrLen == 0) {
/* 1348:1804 */       return startPos;
/* 1349:     */     }
/* 1350:1806 */     for (int i = startPos; i < endLimit; i++) {
/* 1351:1807 */       if (regionMatchesAscii(str, true, i, searchStr, 0, searchStrLen)) {
/* 1352:1808 */         return i;
/* 1353:     */       }
/* 1354:     */     }
/* 1355:1811 */     return -1;
/* 1356:     */   }
/* 1357:     */   
/* 1358:     */   public static int indexOf(CharSequence cs, char searchChar, int start)
/* 1359:     */   {
/* 1360:1826 */     if ((cs instanceof String)) {
/* 1361:1827 */       return ((String)cs).indexOf(searchChar, start);
/* 1362:     */     }
/* 1363:1828 */     if ((cs instanceof AsciiString)) {
/* 1364:1829 */       return ((AsciiString)cs).indexOf(searchChar, start);
/* 1365:     */     }
/* 1366:1831 */     if (cs == null) {
/* 1367:1832 */       return -1;
/* 1368:     */     }
/* 1369:1834 */     int sz = cs.length();
/* 1370:1835 */     if (start < 0) {
/* 1371:1836 */       start = 0;
/* 1372:     */     }
/* 1373:1838 */     for (int i = start; i < sz; i++) {
/* 1374:1839 */       if (cs.charAt(i) == searchChar) {
/* 1375:1840 */         return i;
/* 1376:     */       }
/* 1377:     */     }
/* 1378:1843 */     return -1;
/* 1379:     */   }
/* 1380:     */   
/* 1381:     */   private static boolean equalsIgnoreCase(byte a, byte b)
/* 1382:     */   {
/* 1383:1847 */     return (a == b) || (toLowerCase(a) == toLowerCase(b));
/* 1384:     */   }
/* 1385:     */   
/* 1386:     */   private static boolean equalsIgnoreCase(char a, char b)
/* 1387:     */   {
/* 1388:1851 */     return (a == b) || (toLowerCase(a) == toLowerCase(b));
/* 1389:     */   }
/* 1390:     */   
/* 1391:     */   private static byte toLowerCase(byte b)
/* 1392:     */   {
/* 1393:1855 */     return isUpperCase(b) ? (byte)(b + 32) : b;
/* 1394:     */   }
/* 1395:     */   
/* 1396:     */   private static char toLowerCase(char c)
/* 1397:     */   {
/* 1398:1859 */     return isUpperCase(c) ? (char)(c + ' ') : c;
/* 1399:     */   }
/* 1400:     */   
/* 1401:     */   private static byte toUpperCase(byte b)
/* 1402:     */   {
/* 1403:1863 */     return isLowerCase(b) ? (byte)(b - 32) : b;
/* 1404:     */   }
/* 1405:     */   
/* 1406:     */   private static boolean isLowerCase(byte value)
/* 1407:     */   {
/* 1408:1867 */     return (value >= 97) && (value <= 122);
/* 1409:     */   }
/* 1410:     */   
/* 1411:     */   public static boolean isUpperCase(byte value)
/* 1412:     */   {
/* 1413:1871 */     return (value >= 65) && (value <= 90);
/* 1414:     */   }
/* 1415:     */   
/* 1416:     */   public static boolean isUpperCase(char value)
/* 1417:     */   {
/* 1418:1875 */     return (value >= 'A') && (value <= 'Z');
/* 1419:     */   }
/* 1420:     */   
/* 1421:     */   public static byte c2b(char c)
/* 1422:     */   {
/* 1423:1879 */     return (byte)(c > 'ÿ' ? '?' : c);
/* 1424:     */   }
/* 1425:     */   
/* 1426:     */   public static char b2c(byte b)
/* 1427:     */   {
/* 1428:1883 */     return (char)(b & 0xFF);
/* 1429:     */   }
/* 1430:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.AsciiString
 * JD-Core Version:    0.7.0.1
 */