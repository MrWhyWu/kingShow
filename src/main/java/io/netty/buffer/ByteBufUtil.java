/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.AsciiString;
/*    4:     */ import io.netty.util.ByteProcessor;
/*    5:     */ import io.netty.util.ByteProcessor.IndexOfProcessor;
/*    6:     */ import io.netty.util.CharsetUtil;
/*    7:     */ import io.netty.util.Recycler;
/*    8:     */ import io.netty.util.Recycler.Handle;
/*    9:     */ import io.netty.util.concurrent.FastThreadLocal;
/*   10:     */ import io.netty.util.internal.MathUtil;
/*   11:     */ import io.netty.util.internal.ObjectUtil;
/*   12:     */ import io.netty.util.internal.PlatformDependent;
/*   13:     */ import io.netty.util.internal.StringUtil;
/*   14:     */ import io.netty.util.internal.SystemPropertyUtil;
/*   15:     */ import io.netty.util.internal.logging.InternalLogger;
/*   16:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   17:     */ import java.nio.ByteBuffer;
/*   18:     */ import java.nio.ByteOrder;
/*   19:     */ import java.nio.CharBuffer;
/*   20:     */ import java.nio.charset.CharacterCodingException;
/*   21:     */ import java.nio.charset.Charset;
/*   22:     */ import java.nio.charset.CharsetDecoder;
/*   23:     */ import java.nio.charset.CharsetEncoder;
/*   24:     */ import java.nio.charset.CoderResult;
/*   25:     */ import java.nio.charset.CodingErrorAction;
/*   26:     */ import java.util.Arrays;
/*   27:     */ import java.util.Locale;
/*   28:     */ 
/*   29:     */ public final class ByteBufUtil
/*   30:     */ {
/*   31:  53 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ByteBufUtil.class);
/*   32:  54 */   private static final FastThreadLocal<CharBuffer> CHAR_BUFFERS = new FastThreadLocal()
/*   33:     */   {
/*   34:     */     protected CharBuffer initialValue()
/*   35:     */       throws Exception
/*   36:     */     {
/*   37:  57 */       return CharBuffer.allocate(1024);
/*   38:     */     }
/*   39:     */   };
/*   40:     */   private static final byte WRITE_UTF_UNKNOWN = 63;
/*   41:     */   private static final int MAX_CHAR_BUFFER_SIZE;
/*   42:     */   private static final int THREAD_LOCAL_BUFFER_SIZE;
/*   43:  65 */   private static final int MAX_BYTES_PER_CHAR_UTF8 = (int)CharsetUtil.encoder(CharsetUtil.UTF_8).maxBytesPerChar();
/*   44:     */   static final ByteBufAllocator DEFAULT_ALLOCATOR;
/*   45:     */   
/*   46:     */   static
/*   47:     */   {
/*   48:  70 */     String allocType = SystemPropertyUtil.get("io.netty.allocator.type", 
/*   49:  71 */       PlatformDependent.isAndroid() ? "unpooled" : "pooled");
/*   50:  72 */     allocType = allocType.toLowerCase(Locale.US).trim();
/*   51:     */     ByteBufAllocator alloc;
/*   52:  75 */     if ("unpooled".equals(allocType))
/*   53:     */     {
/*   54:  76 */       ByteBufAllocator alloc = UnpooledByteBufAllocator.DEFAULT;
/*   55:  77 */       logger.debug("-Dio.netty.allocator.type: {}", allocType);
/*   56:     */     }
/*   57:  78 */     else if ("pooled".equals(allocType))
/*   58:     */     {
/*   59:  79 */       ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;
/*   60:  80 */       logger.debug("-Dio.netty.allocator.type: {}", allocType);
/*   61:     */     }
/*   62:     */     else
/*   63:     */     {
/*   64:  82 */       alloc = PooledByteBufAllocator.DEFAULT;
/*   65:  83 */       logger.debug("-Dio.netty.allocator.type: pooled (unknown: {})", allocType);
/*   66:     */     }
/*   67:  86 */     DEFAULT_ALLOCATOR = alloc;
/*   68:     */     
/*   69:  88 */     THREAD_LOCAL_BUFFER_SIZE = SystemPropertyUtil.getInt("io.netty.threadLocalDirectBufferSize", 65536);
/*   70:  89 */     logger.debug("-Dio.netty.threadLocalDirectBufferSize: {}", Integer.valueOf(THREAD_LOCAL_BUFFER_SIZE));
/*   71:     */     
/*   72:  91 */     MAX_CHAR_BUFFER_SIZE = SystemPropertyUtil.getInt("io.netty.maxThreadLocalCharBufferSize", 16384);
/*   73:  92 */     logger.debug("-Dio.netty.maxThreadLocalCharBufferSize: {}", Integer.valueOf(MAX_CHAR_BUFFER_SIZE));
/*   74:     */   }
/*   75:     */   
/*   76:     */   public static String hexDump(ByteBuf buffer)
/*   77:     */   {
/*   78: 100 */     return hexDump(buffer, buffer.readerIndex(), buffer.readableBytes());
/*   79:     */   }
/*   80:     */   
/*   81:     */   public static String hexDump(ByteBuf buffer, int fromIndex, int length)
/*   82:     */   {
/*   83: 108 */     return HexUtil.hexDump(buffer, fromIndex, length);
/*   84:     */   }
/*   85:     */   
/*   86:     */   public static String hexDump(byte[] array)
/*   87:     */   {
/*   88: 116 */     return hexDump(array, 0, array.length);
/*   89:     */   }
/*   90:     */   
/*   91:     */   public static String hexDump(byte[] array, int fromIndex, int length)
/*   92:     */   {
/*   93: 124 */     return HexUtil.hexDump(array, fromIndex, length);
/*   94:     */   }
/*   95:     */   
/*   96:     */   public static byte decodeHexByte(CharSequence s, int pos)
/*   97:     */   {
/*   98: 131 */     return StringUtil.decodeHexByte(s, pos);
/*   99:     */   }
/*  100:     */   
/*  101:     */   public static byte[] decodeHexDump(CharSequence hexDump)
/*  102:     */   {
/*  103: 138 */     return StringUtil.decodeHexDump(hexDump, 0, hexDump.length());
/*  104:     */   }
/*  105:     */   
/*  106:     */   public static byte[] decodeHexDump(CharSequence hexDump, int fromIndex, int length)
/*  107:     */   {
/*  108: 145 */     return StringUtil.decodeHexDump(hexDump, fromIndex, length);
/*  109:     */   }
/*  110:     */   
/*  111:     */   public static boolean ensureWritableSuccess(int ensureWritableResult)
/*  112:     */   {
/*  113: 156 */     return (ensureWritableResult == 0) || (ensureWritableResult == 2);
/*  114:     */   }
/*  115:     */   
/*  116:     */   public static int hashCode(ByteBuf buffer)
/*  117:     */   {
/*  118: 164 */     int aLen = buffer.readableBytes();
/*  119: 165 */     int intCount = aLen >>> 2;
/*  120: 166 */     int byteCount = aLen & 0x3;
/*  121:     */     
/*  122: 168 */     int hashCode = 1;
/*  123: 169 */     int arrayIndex = buffer.readerIndex();
/*  124: 170 */     if (buffer.order() == ByteOrder.BIG_ENDIAN) {
/*  125: 171 */       for (int i = intCount; i > 0; i--)
/*  126:     */       {
/*  127: 172 */         hashCode = 31 * hashCode + buffer.getInt(arrayIndex);
/*  128: 173 */         arrayIndex += 4;
/*  129:     */       }
/*  130:     */     } else {
/*  131: 176 */       for (int i = intCount; i > 0; i--)
/*  132:     */       {
/*  133: 177 */         hashCode = 31 * hashCode + swapInt(buffer.getInt(arrayIndex));
/*  134: 178 */         arrayIndex += 4;
/*  135:     */       }
/*  136:     */     }
/*  137: 182 */     for (int i = byteCount; i > 0; i--) {
/*  138: 183 */       hashCode = 31 * hashCode + buffer.getByte(arrayIndex++);
/*  139:     */     }
/*  140: 186 */     if (hashCode == 0) {
/*  141: 187 */       hashCode = 1;
/*  142:     */     }
/*  143: 190 */     return hashCode;
/*  144:     */   }
/*  145:     */   
/*  146:     */   public static int indexOf(ByteBuf needle, ByteBuf haystack)
/*  147:     */   {
/*  148: 198 */     int attempts = haystack.readableBytes() - needle.readableBytes() + 1;
/*  149: 199 */     for (int i = 0; i < attempts; i++) {
/*  150: 200 */       if (equals(needle, needle.readerIndex(), haystack, haystack
/*  151: 201 */         .readerIndex() + i, needle
/*  152: 202 */         .readableBytes())) {
/*  153: 203 */         return haystack.readerIndex() + i;
/*  154:     */       }
/*  155:     */     }
/*  156: 206 */     return -1;
/*  157:     */   }
/*  158:     */   
/*  159:     */   public static boolean equals(ByteBuf a, int aStartIndex, ByteBuf b, int bStartIndex, int length)
/*  160:     */   {
/*  161: 218 */     if ((aStartIndex < 0) || (bStartIndex < 0) || (length < 0)) {
/*  162: 219 */       throw new IllegalArgumentException("All indexes and lengths must be non-negative");
/*  163:     */     }
/*  164: 221 */     if ((a.writerIndex() - length < aStartIndex) || (b.writerIndex() - length < bStartIndex)) {
/*  165: 222 */       return false;
/*  166:     */     }
/*  167: 225 */     int longCount = length >>> 3;
/*  168: 226 */     int byteCount = length & 0x7;
/*  169: 228 */     if (a.order() == b.order()) {
/*  170: 229 */       for (int i = longCount; i > 0; i--)
/*  171:     */       {
/*  172: 230 */         if (a.getLong(aStartIndex) != b.getLong(bStartIndex)) {
/*  173: 231 */           return false;
/*  174:     */         }
/*  175: 233 */         aStartIndex += 8;
/*  176: 234 */         bStartIndex += 8;
/*  177:     */       }
/*  178:     */     } else {
/*  179: 237 */       for (int i = longCount; i > 0; i--)
/*  180:     */       {
/*  181: 238 */         if (a.getLong(aStartIndex) != swapLong(b.getLong(bStartIndex))) {
/*  182: 239 */           return false;
/*  183:     */         }
/*  184: 241 */         aStartIndex += 8;
/*  185: 242 */         bStartIndex += 8;
/*  186:     */       }
/*  187:     */     }
/*  188: 246 */     for (int i = byteCount; i > 0; i--)
/*  189:     */     {
/*  190: 247 */       if (a.getByte(aStartIndex) != b.getByte(bStartIndex)) {
/*  191: 248 */         return false;
/*  192:     */       }
/*  193: 250 */       aStartIndex++;
/*  194: 251 */       bStartIndex++;
/*  195:     */     }
/*  196: 254 */     return true;
/*  197:     */   }
/*  198:     */   
/*  199:     */   public static boolean equals(ByteBuf bufferA, ByteBuf bufferB)
/*  200:     */   {
/*  201: 263 */     int aLen = bufferA.readableBytes();
/*  202: 264 */     if (aLen != bufferB.readableBytes()) {
/*  203: 265 */       return false;
/*  204:     */     }
/*  205: 267 */     return equals(bufferA, bufferA.readerIndex(), bufferB, bufferB.readerIndex(), aLen);
/*  206:     */   }
/*  207:     */   
/*  208:     */   public static int compare(ByteBuf bufferA, ByteBuf bufferB)
/*  209:     */   {
/*  210: 275 */     int aLen = bufferA.readableBytes();
/*  211: 276 */     int bLen = bufferB.readableBytes();
/*  212: 277 */     int minLength = Math.min(aLen, bLen);
/*  213: 278 */     int uintCount = minLength >>> 2;
/*  214: 279 */     int byteCount = minLength & 0x3;
/*  215: 280 */     int aIndex = bufferA.readerIndex();
/*  216: 281 */     int bIndex = bufferB.readerIndex();
/*  217: 283 */     if (uintCount > 0)
/*  218:     */     {
/*  219: 284 */       boolean bufferAIsBigEndian = bufferA.order() == ByteOrder.BIG_ENDIAN;
/*  220:     */       
/*  221: 286 */       int uintCountIncrement = uintCount << 2;
/*  222:     */       long res;
/*  223:     */       long res;
/*  224: 288 */       if (bufferA.order() == bufferB.order()) {
/*  225: 290 */         res = bufferAIsBigEndian ? compareUintBigEndian(bufferA, bufferB, aIndex, bIndex, uintCountIncrement) : compareUintLittleEndian(bufferA, bufferB, aIndex, bIndex, uintCountIncrement);
/*  226:     */       } else {
/*  227: 293 */         res = bufferAIsBigEndian ? compareUintBigEndianA(bufferA, bufferB, aIndex, bIndex, uintCountIncrement) : compareUintBigEndianB(bufferA, bufferB, aIndex, bIndex, uintCountIncrement);
/*  228:     */       }
/*  229: 295 */       if (res != 0L) {
/*  230: 297 */         return (int)Math.min(2147483647L, Math.max(-2147483648L, res));
/*  231:     */       }
/*  232: 299 */       aIndex += uintCountIncrement;
/*  233: 300 */       bIndex += uintCountIncrement;
/*  234:     */     }
/*  235: 303 */     for (int aEnd = aIndex + byteCount; aIndex < aEnd; bIndex++)
/*  236:     */     {
/*  237: 304 */       int comp = bufferA.getUnsignedByte(aIndex) - bufferB.getUnsignedByte(bIndex);
/*  238: 305 */       if (comp != 0) {
/*  239: 306 */         return comp;
/*  240:     */       }
/*  241: 303 */       aIndex++;
/*  242:     */     }
/*  243: 310 */     return aLen - bLen;
/*  244:     */   }
/*  245:     */   
/*  246:     */   private static long compareUintBigEndian(ByteBuf bufferA, ByteBuf bufferB, int aIndex, int bIndex, int uintCountIncrement)
/*  247:     */   {
/*  248: 315 */     for (int aEnd = aIndex + uintCountIncrement; aIndex < aEnd; bIndex += 4)
/*  249:     */     {
/*  250: 316 */       long comp = bufferA.getUnsignedInt(aIndex) - bufferB.getUnsignedInt(bIndex);
/*  251: 317 */       if (comp != 0L) {
/*  252: 318 */         return comp;
/*  253:     */       }
/*  254: 315 */       aIndex += 4;
/*  255:     */     }
/*  256: 321 */     return 0L;
/*  257:     */   }
/*  258:     */   
/*  259:     */   private static long compareUintLittleEndian(ByteBuf bufferA, ByteBuf bufferB, int aIndex, int bIndex, int uintCountIncrement)
/*  260:     */   {
/*  261: 326 */     for (int aEnd = aIndex + uintCountIncrement; aIndex < aEnd; bIndex += 4)
/*  262:     */     {
/*  263: 327 */       long comp = bufferA.getUnsignedIntLE(aIndex) - bufferB.getUnsignedIntLE(bIndex);
/*  264: 328 */       if (comp != 0L) {
/*  265: 329 */         return comp;
/*  266:     */       }
/*  267: 326 */       aIndex += 4;
/*  268:     */     }
/*  269: 332 */     return 0L;
/*  270:     */   }
/*  271:     */   
/*  272:     */   private static long compareUintBigEndianA(ByteBuf bufferA, ByteBuf bufferB, int aIndex, int bIndex, int uintCountIncrement)
/*  273:     */   {
/*  274: 337 */     for (int aEnd = aIndex + uintCountIncrement; aIndex < aEnd; bIndex += 4)
/*  275:     */     {
/*  276: 338 */       long comp = bufferA.getUnsignedInt(aIndex) - bufferB.getUnsignedIntLE(bIndex);
/*  277: 339 */       if (comp != 0L) {
/*  278: 340 */         return comp;
/*  279:     */       }
/*  280: 337 */       aIndex += 4;
/*  281:     */     }
/*  282: 343 */     return 0L;
/*  283:     */   }
/*  284:     */   
/*  285:     */   private static long compareUintBigEndianB(ByteBuf bufferA, ByteBuf bufferB, int aIndex, int bIndex, int uintCountIncrement)
/*  286:     */   {
/*  287: 348 */     for (int aEnd = aIndex + uintCountIncrement; aIndex < aEnd; bIndex += 4)
/*  288:     */     {
/*  289: 349 */       long comp = bufferA.getUnsignedIntLE(aIndex) - bufferB.getUnsignedInt(bIndex);
/*  290: 350 */       if (comp != 0L) {
/*  291: 351 */         return comp;
/*  292:     */       }
/*  293: 348 */       aIndex += 4;
/*  294:     */     }
/*  295: 354 */     return 0L;
/*  296:     */   }
/*  297:     */   
/*  298:     */   public static int indexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value)
/*  299:     */   {
/*  300: 362 */     if (fromIndex <= toIndex) {
/*  301: 363 */       return firstIndexOf(buffer, fromIndex, toIndex, value);
/*  302:     */     }
/*  303: 365 */     return lastIndexOf(buffer, fromIndex, toIndex, value);
/*  304:     */   }
/*  305:     */   
/*  306:     */   public static short swapShort(short value)
/*  307:     */   {
/*  308: 373 */     return Short.reverseBytes(value);
/*  309:     */   }
/*  310:     */   
/*  311:     */   public static int swapMedium(int value)
/*  312:     */   {
/*  313: 380 */     int swapped = value << 16 & 0xFF0000 | value & 0xFF00 | value >>> 16 & 0xFF;
/*  314: 381 */     if ((swapped & 0x800000) != 0) {
/*  315: 382 */       swapped |= 0xFF000000;
/*  316:     */     }
/*  317: 384 */     return swapped;
/*  318:     */   }
/*  319:     */   
/*  320:     */   public static int swapInt(int value)
/*  321:     */   {
/*  322: 391 */     return Integer.reverseBytes(value);
/*  323:     */   }
/*  324:     */   
/*  325:     */   public static long swapLong(long value)
/*  326:     */   {
/*  327: 398 */     return Long.reverseBytes(value);
/*  328:     */   }
/*  329:     */   
/*  330:     */   public static ByteBuf writeShortBE(ByteBuf buf, int shortValue)
/*  331:     */   {
/*  332: 406 */     return buf.order() == ByteOrder.BIG_ENDIAN ? buf.writeShort(shortValue) : buf.writeShortLE(shortValue);
/*  333:     */   }
/*  334:     */   
/*  335:     */   public static ByteBuf setShortBE(ByteBuf buf, int index, int shortValue)
/*  336:     */   {
/*  337: 414 */     return buf.order() == ByteOrder.BIG_ENDIAN ? buf.setShort(index, shortValue) : buf.setShortLE(index, shortValue);
/*  338:     */   }
/*  339:     */   
/*  340:     */   public static ByteBuf writeMediumBE(ByteBuf buf, int mediumValue)
/*  341:     */   {
/*  342: 422 */     return buf.order() == ByteOrder.BIG_ENDIAN ? buf.writeMedium(mediumValue) : buf.writeMediumLE(mediumValue);
/*  343:     */   }
/*  344:     */   
/*  345:     */   public static ByteBuf readBytes(ByteBufAllocator alloc, ByteBuf buffer, int length)
/*  346:     */   {
/*  347: 429 */     boolean release = true;
/*  348: 430 */     ByteBuf dst = alloc.buffer(length);
/*  349:     */     try
/*  350:     */     {
/*  351: 432 */       buffer.readBytes(dst);
/*  352: 433 */       release = false;
/*  353: 434 */       return dst;
/*  354:     */     }
/*  355:     */     finally
/*  356:     */     {
/*  357: 436 */       if (release) {
/*  358: 437 */         dst.release();
/*  359:     */       }
/*  360:     */     }
/*  361:     */   }
/*  362:     */   
/*  363:     */   private static int firstIndexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value)
/*  364:     */   {
/*  365: 443 */     fromIndex = Math.max(fromIndex, 0);
/*  366: 444 */     if ((fromIndex >= toIndex) || (buffer.capacity() == 0)) {
/*  367: 445 */       return -1;
/*  368:     */     }
/*  369: 448 */     return buffer.forEachByte(fromIndex, toIndex - fromIndex, new ByteProcessor.IndexOfProcessor(value));
/*  370:     */   }
/*  371:     */   
/*  372:     */   private static int lastIndexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value)
/*  373:     */   {
/*  374: 452 */     fromIndex = Math.min(fromIndex, buffer.capacity());
/*  375: 453 */     if ((fromIndex < 0) || (buffer.capacity() == 0)) {
/*  376: 454 */       return -1;
/*  377:     */     }
/*  378: 457 */     return buffer.forEachByteDesc(toIndex, fromIndex - toIndex, new ByteProcessor.IndexOfProcessor(value));
/*  379:     */   }
/*  380:     */   
/*  381:     */   public static ByteBuf writeUtf8(ByteBufAllocator alloc, CharSequence seq)
/*  382:     */   {
/*  383: 470 */     ByteBuf buf = alloc.buffer(utf8MaxBytes(seq));
/*  384: 471 */     writeUtf8(buf, seq);
/*  385: 472 */     return buf;
/*  386:     */   }
/*  387:     */   
/*  388:     */   public static int writeUtf8(ByteBuf buf, CharSequence seq)
/*  389:     */   {
/*  390:     */     for (;;)
/*  391:     */     {
/*  392: 483 */       if ((buf instanceof AbstractByteBuf))
/*  393:     */       {
/*  394: 484 */         AbstractByteBuf byteBuf = (AbstractByteBuf)buf;
/*  395: 485 */         byteBuf.ensureWritable0(utf8MaxBytes(seq));
/*  396: 486 */         int written = writeUtf8(byteBuf, byteBuf.writerIndex, seq, seq.length());
/*  397: 487 */         byteBuf.writerIndex += written;
/*  398: 488 */         return written;
/*  399:     */       }
/*  400: 489 */       if (!(buf instanceof WrappedByteBuf)) {
/*  401:     */         break;
/*  402:     */       }
/*  403: 491 */       buf = buf.unwrap();
/*  404:     */     }
/*  405: 493 */     byte[] bytes = seq.toString().getBytes(CharsetUtil.UTF_8);
/*  406: 494 */     buf.writeBytes(bytes);
/*  407: 495 */     return bytes.length;
/*  408:     */   }
/*  409:     */   
/*  410:     */   static int writeUtf8(AbstractByteBuf buffer, int writerIndex, CharSequence seq, int len)
/*  411:     */   {
/*  412: 502 */     int oldWriterIndex = writerIndex;
/*  413: 506 */     for (int i = 0; i < len; i++)
/*  414:     */     {
/*  415: 507 */       char c = seq.charAt(i);
/*  416: 508 */       if (c < '')
/*  417:     */       {
/*  418: 509 */         buffer._setByte(writerIndex++, (byte)c);
/*  419:     */       }
/*  420: 510 */       else if (c < 'ࠀ')
/*  421:     */       {
/*  422: 511 */         buffer._setByte(writerIndex++, (byte)(0xC0 | c >> '\006'));
/*  423: 512 */         buffer._setByte(writerIndex++, (byte)(0x80 | c & 0x3F));
/*  424:     */       }
/*  425: 513 */       else if (StringUtil.isSurrogate(c))
/*  426:     */       {
/*  427: 514 */         if (!Character.isHighSurrogate(c))
/*  428:     */         {
/*  429: 515 */           buffer._setByte(writerIndex++, 63);
/*  430:     */         }
/*  431:     */         else
/*  432:     */         {
/*  433:     */           try
/*  434:     */           {
/*  435: 523 */             c2 = seq.charAt(++i);
/*  436:     */           }
/*  437:     */           catch (IndexOutOfBoundsException e)
/*  438:     */           {
/*  439:     */             char c2;
/*  440: 525 */             buffer._setByte(writerIndex++, 63);
/*  441: 526 */             break;
/*  442:     */           }
/*  443:     */           char c2;
/*  444: 528 */           if (!Character.isLowSurrogate(c2))
/*  445:     */           {
/*  446: 529 */             buffer._setByte(writerIndex++, 63);
/*  447: 530 */             buffer._setByte(writerIndex++, Character.isHighSurrogate(c2) ? '?' : c2);
/*  448:     */           }
/*  449:     */           else
/*  450:     */           {
/*  451: 533 */             int codePoint = Character.toCodePoint(c, c2);
/*  452:     */             
/*  453: 535 */             buffer._setByte(writerIndex++, (byte)(0xF0 | codePoint >> 18));
/*  454: 536 */             buffer._setByte(writerIndex++, (byte)(0x80 | codePoint >> 12 & 0x3F));
/*  455: 537 */             buffer._setByte(writerIndex++, (byte)(0x80 | codePoint >> 6 & 0x3F));
/*  456: 538 */             buffer._setByte(writerIndex++, (byte)(0x80 | codePoint & 0x3F));
/*  457:     */           }
/*  458:     */         }
/*  459:     */       }
/*  460:     */       else
/*  461:     */       {
/*  462: 540 */         buffer._setByte(writerIndex++, (byte)(0xE0 | c >> '\f'));
/*  463: 541 */         buffer._setByte(writerIndex++, (byte)(0x80 | c >> '\006' & 0x3F));
/*  464: 542 */         buffer._setByte(writerIndex++, (byte)(0x80 | c & 0x3F));
/*  465:     */       }
/*  466:     */     }
/*  467: 545 */     return writerIndex - oldWriterIndex;
/*  468:     */   }
/*  469:     */   
/*  470:     */   public static int utf8MaxBytes(CharSequence seq)
/*  471:     */   {
/*  472: 552 */     return seq.length() * MAX_BYTES_PER_CHAR_UTF8;
/*  473:     */   }
/*  474:     */   
/*  475:     */   public static ByteBuf writeAscii(ByteBufAllocator alloc, CharSequence seq)
/*  476:     */   {
/*  477: 565 */     ByteBuf buf = alloc.buffer(seq.length());
/*  478: 566 */     writeAscii(buf, seq);
/*  479: 567 */     return buf;
/*  480:     */   }
/*  481:     */   
/*  482:     */   public static int writeAscii(ByteBuf buf, CharSequence seq)
/*  483:     */   {
/*  484: 578 */     int len = seq.length();
/*  485: 579 */     if ((seq instanceof AsciiString))
/*  486:     */     {
/*  487: 580 */       AsciiString asciiString = (AsciiString)seq;
/*  488: 581 */       buf.writeBytes(asciiString.array(), asciiString.arrayOffset(), len);
/*  489:     */     }
/*  490:     */     else
/*  491:     */     {
/*  492:     */       for (;;)
/*  493:     */       {
/*  494: 584 */         if ((buf instanceof AbstractByteBuf))
/*  495:     */         {
/*  496: 585 */           AbstractByteBuf byteBuf = (AbstractByteBuf)buf;
/*  497: 586 */           byteBuf.ensureWritable0(len);
/*  498: 587 */           int written = writeAscii(byteBuf, byteBuf.writerIndex, seq, len);
/*  499: 588 */           byteBuf.writerIndex += written;
/*  500: 589 */           return written;
/*  501:     */         }
/*  502: 590 */         if (!(buf instanceof WrappedByteBuf)) {
/*  503:     */           break;
/*  504:     */         }
/*  505: 592 */         buf = buf.unwrap();
/*  506:     */       }
/*  507: 594 */       byte[] bytes = seq.toString().getBytes(CharsetUtil.US_ASCII);
/*  508: 595 */       buf.writeBytes(bytes);
/*  509: 596 */       return bytes.length;
/*  510:     */     }
/*  511: 600 */     return len;
/*  512:     */   }
/*  513:     */   
/*  514:     */   static int writeAscii(AbstractByteBuf buffer, int writerIndex, CharSequence seq, int len)
/*  515:     */   {
/*  516: 608 */     for (int i = 0; i < len; i++) {
/*  517: 609 */       buffer._setByte(writerIndex++, AsciiString.c2b(seq.charAt(i)));
/*  518:     */     }
/*  519: 611 */     return len;
/*  520:     */   }
/*  521:     */   
/*  522:     */   public static ByteBuf encodeString(ByteBufAllocator alloc, CharBuffer src, Charset charset)
/*  523:     */   {
/*  524: 619 */     return encodeString0(alloc, false, src, charset, 0);
/*  525:     */   }
/*  526:     */   
/*  527:     */   public static ByteBuf encodeString(ByteBufAllocator alloc, CharBuffer src, Charset charset, int extraCapacity)
/*  528:     */   {
/*  529: 632 */     return encodeString0(alloc, false, src, charset, extraCapacity);
/*  530:     */   }
/*  531:     */   
/*  532:     */   static ByteBuf encodeString0(ByteBufAllocator alloc, boolean enforceHeap, CharBuffer src, Charset charset, int extraCapacity)
/*  533:     */   {
/*  534: 637 */     CharsetEncoder encoder = CharsetUtil.encoder(charset);
/*  535: 638 */     int length = (int)(src.remaining() * encoder.maxBytesPerChar()) + extraCapacity;
/*  536: 639 */     boolean release = true;
/*  537:     */     ByteBuf dst;
/*  538:     */     ByteBuf dst;
/*  539: 641 */     if (enforceHeap) {
/*  540: 642 */       dst = alloc.heapBuffer(length);
/*  541:     */     } else {
/*  542: 644 */       dst = alloc.buffer(length);
/*  543:     */     }
/*  544:     */     try
/*  545:     */     {
/*  546: 647 */       ByteBuffer dstBuf = dst.internalNioBuffer(dst.readerIndex(), length);
/*  547: 648 */       int pos = dstBuf.position();
/*  548: 649 */       CoderResult cr = encoder.encode(src, dstBuf, true);
/*  549: 650 */       if (!cr.isUnderflow()) {
/*  550: 651 */         cr.throwException();
/*  551:     */       }
/*  552: 653 */       cr = encoder.flush(dstBuf);
/*  553: 654 */       if (!cr.isUnderflow()) {
/*  554: 655 */         cr.throwException();
/*  555:     */       }
/*  556: 657 */       dst.writerIndex(dst.writerIndex() + dstBuf.position() - pos);
/*  557: 658 */       release = false;
/*  558: 659 */       return dst;
/*  559:     */     }
/*  560:     */     catch (CharacterCodingException x)
/*  561:     */     {
/*  562: 661 */       throw new IllegalStateException(x);
/*  563:     */     }
/*  564:     */     finally
/*  565:     */     {
/*  566: 663 */       if (release) {
/*  567: 664 */         dst.release();
/*  568:     */       }
/*  569:     */     }
/*  570:     */   }
/*  571:     */   
/*  572:     */   static String decodeString(ByteBuf src, int readerIndex, int len, Charset charset)
/*  573:     */   {
/*  574: 670 */     if (len == 0) {
/*  575: 671 */       return "";
/*  576:     */     }
/*  577: 673 */     CharsetDecoder decoder = CharsetUtil.decoder(charset);
/*  578: 674 */     int maxLength = (int)(len * decoder.maxCharsPerByte());
/*  579: 675 */     CharBuffer dst = (CharBuffer)CHAR_BUFFERS.get();
/*  580: 676 */     if (dst.length() < maxLength)
/*  581:     */     {
/*  582: 677 */       dst = CharBuffer.allocate(maxLength);
/*  583: 678 */       if (maxLength <= MAX_CHAR_BUFFER_SIZE) {
/*  584: 679 */         CHAR_BUFFERS.set(dst);
/*  585:     */       }
/*  586:     */     }
/*  587:     */     else
/*  588:     */     {
/*  589: 682 */       dst.clear();
/*  590:     */     }
/*  591: 684 */     if (src.nioBufferCount() == 1)
/*  592:     */     {
/*  593: 686 */       decodeString(decoder, src.internalNioBuffer(readerIndex, len), dst);
/*  594:     */     }
/*  595:     */     else
/*  596:     */     {
/*  597: 690 */       ByteBuf buffer = src.alloc().heapBuffer(len);
/*  598:     */       try
/*  599:     */       {
/*  600: 692 */         buffer.writeBytes(src, readerIndex, len);
/*  601:     */         
/*  602: 694 */         decodeString(decoder, buffer.internalNioBuffer(buffer.readerIndex(), len), dst);
/*  603:     */       }
/*  604:     */       finally
/*  605:     */       {
/*  606: 697 */         buffer.release();
/*  607:     */       }
/*  608:     */     }
/*  609: 700 */     return dst.flip().toString();
/*  610:     */   }
/*  611:     */   
/*  612:     */   private static void decodeString(CharsetDecoder decoder, ByteBuffer src, CharBuffer dst)
/*  613:     */   {
/*  614:     */     try
/*  615:     */     {
/*  616: 705 */       CoderResult cr = decoder.decode(src, dst, true);
/*  617: 706 */       if (!cr.isUnderflow()) {
/*  618: 707 */         cr.throwException();
/*  619:     */       }
/*  620: 709 */       cr = decoder.flush(dst);
/*  621: 710 */       if (!cr.isUnderflow()) {
/*  622: 711 */         cr.throwException();
/*  623:     */       }
/*  624:     */     }
/*  625:     */     catch (CharacterCodingException x)
/*  626:     */     {
/*  627: 714 */       throw new IllegalStateException(x);
/*  628:     */     }
/*  629:     */   }
/*  630:     */   
/*  631:     */   public static ByteBuf threadLocalDirectBuffer()
/*  632:     */   {
/*  633: 724 */     if (THREAD_LOCAL_BUFFER_SIZE <= 0) {
/*  634: 725 */       return null;
/*  635:     */     }
/*  636: 728 */     if (PlatformDependent.hasUnsafe()) {
/*  637: 729 */       return ThreadLocalUnsafeDirectByteBuf.newInstance();
/*  638:     */     }
/*  639: 731 */     return ThreadLocalDirectByteBuf.newInstance();
/*  640:     */   }
/*  641:     */   
/*  642:     */   public static byte[] getBytes(ByteBuf buf)
/*  643:     */   {
/*  644: 740 */     return getBytes(buf, buf.readerIndex(), buf.readableBytes());
/*  645:     */   }
/*  646:     */   
/*  647:     */   public static byte[] getBytes(ByteBuf buf, int start, int length)
/*  648:     */   {
/*  649: 748 */     return getBytes(buf, start, length, true);
/*  650:     */   }
/*  651:     */   
/*  652:     */   public static byte[] getBytes(ByteBuf buf, int start, int length, boolean copy)
/*  653:     */   {
/*  654: 758 */     if (MathUtil.isOutOfBounds(start, length, buf.capacity())) {
/*  655: 760 */       throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= buf.capacity(" + buf.capacity() + ')');
/*  656:     */     }
/*  657: 763 */     if (buf.hasArray())
/*  658:     */     {
/*  659: 764 */       if ((copy) || (start != 0) || (length != buf.capacity()))
/*  660:     */       {
/*  661: 765 */         int baseOffset = buf.arrayOffset() + start;
/*  662: 766 */         return Arrays.copyOfRange(buf.array(), baseOffset, baseOffset + length);
/*  663:     */       }
/*  664: 768 */       return buf.array();
/*  665:     */     }
/*  666: 772 */     byte[] v = new byte[length];
/*  667: 773 */     buf.getBytes(start, v);
/*  668: 774 */     return v;
/*  669:     */   }
/*  670:     */   
/*  671:     */   public static void copy(AsciiString src, ByteBuf dst)
/*  672:     */   {
/*  673: 784 */     copy(src, 0, dst, src.length());
/*  674:     */   }
/*  675:     */   
/*  676:     */   public static void copy(AsciiString src, int srcIdx, ByteBuf dst, int dstIdx, int length)
/*  677:     */   {
/*  678: 799 */     if (MathUtil.isOutOfBounds(srcIdx, length, src.length())) {
/*  679: 801 */       throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + src.length() + ')');
/*  680:     */     }
/*  681: 804 */     ((ByteBuf)ObjectUtil.checkNotNull(dst, "dst")).setBytes(dstIdx, src.array(), srcIdx + src.arrayOffset(), length);
/*  682:     */   }
/*  683:     */   
/*  684:     */   public static void copy(AsciiString src, int srcIdx, ByteBuf dst, int length)
/*  685:     */   {
/*  686: 816 */     if (MathUtil.isOutOfBounds(srcIdx, length, src.length())) {
/*  687: 818 */       throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + src.length() + ')');
/*  688:     */     }
/*  689: 821 */     ((ByteBuf)ObjectUtil.checkNotNull(dst, "dst")).writeBytes(src.array(), srcIdx + src.arrayOffset(), length);
/*  690:     */   }
/*  691:     */   
/*  692:     */   public static String prettyHexDump(ByteBuf buffer)
/*  693:     */   {
/*  694: 828 */     return prettyHexDump(buffer, buffer.readerIndex(), buffer.readableBytes());
/*  695:     */   }
/*  696:     */   
/*  697:     */   public static String prettyHexDump(ByteBuf buffer, int offset, int length)
/*  698:     */   {
/*  699: 836 */     return HexUtil.prettyHexDump(buffer, offset, length);
/*  700:     */   }
/*  701:     */   
/*  702:     */   public static void appendPrettyHexDump(StringBuilder dump, ByteBuf buf)
/*  703:     */   {
/*  704: 844 */     appendPrettyHexDump(dump, buf, buf.readerIndex(), buf.readableBytes());
/*  705:     */   }
/*  706:     */   
/*  707:     */   public static void appendPrettyHexDump(StringBuilder dump, ByteBuf buf, int offset, int length)
/*  708:     */   {
/*  709: 853 */     HexUtil.appendPrettyHexDump(dump, buf, offset, length);
/*  710:     */   }
/*  711:     */   
/*  712:     */   private static final class HexUtil
/*  713:     */   {
/*  714: 859 */     private static final char[] BYTE2CHAR = new char[256];
/*  715: 860 */     private static final char[] HEXDUMP_TABLE = new char[1024];
/*  716: 861 */     private static final String[] HEXPADDING = new String[16];
/*  717: 862 */     private static final String[] HEXDUMP_ROWPREFIXES = new String[4096];
/*  718: 863 */     private static final String[] BYTE2HEX = new String[256];
/*  719: 864 */     private static final String[] BYTEPADDING = new String[16];
/*  720:     */     
/*  721:     */     static
/*  722:     */     {
/*  723: 867 */       char[] DIGITS = "0123456789abcdef".toCharArray();
/*  724: 868 */       for (int i = 0; i < 256; i++)
/*  725:     */       {
/*  726: 869 */         HEXDUMP_TABLE[(i << 1)] = DIGITS[(i >>> 4 & 0xF)];
/*  727: 870 */         HEXDUMP_TABLE[((i << 1) + 1)] = DIGITS[(i & 0xF)];
/*  728:     */       }
/*  729: 876 */       for (int i = 0; i < HEXPADDING.length; i++)
/*  730:     */       {
/*  731: 877 */         int padding = HEXPADDING.length - i;
/*  732: 878 */         StringBuilder buf = new StringBuilder(padding * 3);
/*  733: 879 */         for (int j = 0; j < padding; j++) {
/*  734: 880 */           buf.append("   ");
/*  735:     */         }
/*  736: 882 */         HEXPADDING[i] = buf.toString();
/*  737:     */       }
/*  738: 886 */       for (i = 0; i < HEXDUMP_ROWPREFIXES.length; i++)
/*  739:     */       {
/*  740: 887 */         StringBuilder buf = new StringBuilder(12);
/*  741: 888 */         buf.append(StringUtil.NEWLINE);
/*  742: 889 */         buf.append(Long.toHexString(i << 4 & 0xFFFFFFFF | 0x0));
/*  743: 890 */         buf.setCharAt(buf.length() - 9, '|');
/*  744: 891 */         buf.append('|');
/*  745: 892 */         HEXDUMP_ROWPREFIXES[i] = buf.toString();
/*  746:     */       }
/*  747: 896 */       for (i = 0; i < BYTE2HEX.length; i++) {
/*  748: 897 */         BYTE2HEX[i] = (' ' + StringUtil.byteToHexStringPadded(i));
/*  749:     */       }
/*  750: 901 */       for (i = 0; i < BYTEPADDING.length; i++)
/*  751:     */       {
/*  752: 902 */         int padding = BYTEPADDING.length - i;
/*  753: 903 */         StringBuilder buf = new StringBuilder(padding);
/*  754: 904 */         for (int j = 0; j < padding; j++) {
/*  755: 905 */           buf.append(' ');
/*  756:     */         }
/*  757: 907 */         BYTEPADDING[i] = buf.toString();
/*  758:     */       }
/*  759: 911 */       for (i = 0; i < BYTE2CHAR.length; i++) {
/*  760: 912 */         if ((i <= 31) || (i >= 127)) {
/*  761: 913 */           BYTE2CHAR[i] = '.';
/*  762:     */         } else {
/*  763: 915 */           BYTE2CHAR[i] = ((char)i);
/*  764:     */         }
/*  765:     */       }
/*  766:     */     }
/*  767:     */     
/*  768:     */     private static String hexDump(ByteBuf buffer, int fromIndex, int length)
/*  769:     */     {
/*  770: 921 */       if (length < 0) {
/*  771: 922 */         throw new IllegalArgumentException("length: " + length);
/*  772:     */       }
/*  773: 924 */       if (length == 0) {
/*  774: 925 */         return "";
/*  775:     */       }
/*  776: 928 */       int endIndex = fromIndex + length;
/*  777: 929 */       char[] buf = new char[length << 1];
/*  778:     */       
/*  779: 931 */       int srcIdx = fromIndex;
/*  780: 932 */       for (int dstIdx = 0; srcIdx < endIndex; dstIdx += 2)
/*  781:     */       {
/*  782: 934 */         System.arraycopy(HEXDUMP_TABLE, buffer
/*  783: 935 */           .getUnsignedByte(srcIdx) << 1, buf, dstIdx, 2);srcIdx++;
/*  784:     */       }
/*  785: 939 */       return new String(buf);
/*  786:     */     }
/*  787:     */     
/*  788:     */     private static String hexDump(byte[] array, int fromIndex, int length)
/*  789:     */     {
/*  790: 943 */       if (length < 0) {
/*  791: 944 */         throw new IllegalArgumentException("length: " + length);
/*  792:     */       }
/*  793: 946 */       if (length == 0) {
/*  794: 947 */         return "";
/*  795:     */       }
/*  796: 950 */       int endIndex = fromIndex + length;
/*  797: 951 */       char[] buf = new char[length << 1];
/*  798:     */       
/*  799: 953 */       int srcIdx = fromIndex;
/*  800: 954 */       for (int dstIdx = 0; srcIdx < endIndex; dstIdx += 2)
/*  801:     */       {
/*  802: 956 */         System.arraycopy(HEXDUMP_TABLE, (array[srcIdx] & 0xFF) << 1, buf, dstIdx, 2);srcIdx++;
/*  803:     */       }
/*  804: 961 */       return new String(buf);
/*  805:     */     }
/*  806:     */     
/*  807:     */     private static String prettyHexDump(ByteBuf buffer, int offset, int length)
/*  808:     */     {
/*  809: 965 */       if (length == 0) {
/*  810: 966 */         return "";
/*  811:     */       }
/*  812: 968 */       int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
/*  813: 969 */       StringBuilder buf = new StringBuilder(rows * 80);
/*  814: 970 */       appendPrettyHexDump(buf, buffer, offset, length);
/*  815: 971 */       return buf.toString();
/*  816:     */     }
/*  817:     */     
/*  818:     */     private static void appendPrettyHexDump(StringBuilder dump, ByteBuf buf, int offset, int length)
/*  819:     */     {
/*  820: 976 */       if (MathUtil.isOutOfBounds(offset, length, buf.capacity())) {
/*  821: 979 */         throw new IndexOutOfBoundsException("expected: 0 <= offset(" + offset + ") <= offset + length(" + length + ") <= buf.capacity(" + buf.capacity() + ')');
/*  822:     */       }
/*  823: 981 */       if (length == 0) {
/*  824: 982 */         return;
/*  825:     */       }
/*  826: 984 */       dump.append("         +-------------------------------------------------+" + StringUtil.NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + StringUtil.NEWLINE + "+--------+-------------------------------------------------+----------------+");
/*  827:     */       
/*  828:     */ 
/*  829:     */ 
/*  830:     */ 
/*  831: 989 */       int startIndex = offset;
/*  832: 990 */       int fullRows = length >>> 4;
/*  833: 991 */       int remainder = length & 0xF;
/*  834: 994 */       for (int row = 0; row < fullRows; row++)
/*  835:     */       {
/*  836: 995 */         int rowStartIndex = (row << 4) + startIndex;
/*  837:     */         
/*  838:     */ 
/*  839: 998 */         appendHexDumpRowPrefix(dump, row, rowStartIndex);
/*  840:     */         
/*  841:     */ 
/*  842:1001 */         int rowEndIndex = rowStartIndex + 16;
/*  843:1002 */         for (int j = rowStartIndex; j < rowEndIndex; j++) {
/*  844:1003 */           dump.append(BYTE2HEX[buf.getUnsignedByte(j)]);
/*  845:     */         }
/*  846:1005 */         dump.append(" |");
/*  847:1008 */         for (int j = rowStartIndex; j < rowEndIndex; j++) {
/*  848:1009 */           dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
/*  849:     */         }
/*  850:1011 */         dump.append('|');
/*  851:     */       }
/*  852:1015 */       if (remainder != 0)
/*  853:     */       {
/*  854:1016 */         int rowStartIndex = (fullRows << 4) + startIndex;
/*  855:1017 */         appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);
/*  856:     */         
/*  857:     */ 
/*  858:1020 */         int rowEndIndex = rowStartIndex + remainder;
/*  859:1021 */         for (int j = rowStartIndex; j < rowEndIndex; j++) {
/*  860:1022 */           dump.append(BYTE2HEX[buf.getUnsignedByte(j)]);
/*  861:     */         }
/*  862:1024 */         dump.append(HEXPADDING[remainder]);
/*  863:1025 */         dump.append(" |");
/*  864:1028 */         for (int j = rowStartIndex; j < rowEndIndex; j++) {
/*  865:1029 */           dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
/*  866:     */         }
/*  867:1031 */         dump.append(BYTEPADDING[remainder]);
/*  868:1032 */         dump.append('|');
/*  869:     */       }
/*  870:1035 */       dump.append(StringUtil.NEWLINE + "+--------+-------------------------------------------------+----------------+");
/*  871:     */     }
/*  872:     */     
/*  873:     */     private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex)
/*  874:     */     {
/*  875:1040 */       if (row < HEXDUMP_ROWPREFIXES.length)
/*  876:     */       {
/*  877:1041 */         dump.append(HEXDUMP_ROWPREFIXES[row]);
/*  878:     */       }
/*  879:     */       else
/*  880:     */       {
/*  881:1043 */         dump.append(StringUtil.NEWLINE);
/*  882:1044 */         dump.append(Long.toHexString(rowStartIndex & 0xFFFFFFFF | 0x0));
/*  883:1045 */         dump.setCharAt(dump.length() - 9, '|');
/*  884:1046 */         dump.append('|');
/*  885:     */       }
/*  886:     */     }
/*  887:     */   }
/*  888:     */   
/*  889:     */   static final class ThreadLocalUnsafeDirectByteBuf
/*  890:     */     extends UnpooledUnsafeDirectByteBuf
/*  891:     */   {
/*  892:1053 */     private static final Recycler<ThreadLocalUnsafeDirectByteBuf> RECYCLER = new Recycler()
/*  893:     */     {
/*  894:     */       protected ByteBufUtil.ThreadLocalUnsafeDirectByteBuf newObject(Recycler.Handle<ByteBufUtil.ThreadLocalUnsafeDirectByteBuf> handle)
/*  895:     */       {
/*  896:1057 */         return new ByteBufUtil.ThreadLocalUnsafeDirectByteBuf(handle, null);
/*  897:     */       }
/*  898:     */     };
/*  899:     */     private final Recycler.Handle<ThreadLocalUnsafeDirectByteBuf> handle;
/*  900:     */     
/*  901:     */     static ThreadLocalUnsafeDirectByteBuf newInstance()
/*  902:     */     {
/*  903:1062 */       ThreadLocalUnsafeDirectByteBuf buf = (ThreadLocalUnsafeDirectByteBuf)RECYCLER.get();
/*  904:1063 */       buf.setRefCnt(1);
/*  905:1064 */       return buf;
/*  906:     */     }
/*  907:     */     
/*  908:     */     private ThreadLocalUnsafeDirectByteBuf(Recycler.Handle<ThreadLocalUnsafeDirectByteBuf> handle)
/*  909:     */     {
/*  910:1070 */       super(256, 2147483647);
/*  911:1071 */       this.handle = handle;
/*  912:     */     }
/*  913:     */     
/*  914:     */     protected void deallocate()
/*  915:     */     {
/*  916:1076 */       if (capacity() > ByteBufUtil.THREAD_LOCAL_BUFFER_SIZE)
/*  917:     */       {
/*  918:1077 */         super.deallocate();
/*  919:     */       }
/*  920:     */       else
/*  921:     */       {
/*  922:1079 */         clear();
/*  923:1080 */         this.handle.recycle(this);
/*  924:     */       }
/*  925:     */     }
/*  926:     */   }
/*  927:     */   
/*  928:     */   static final class ThreadLocalDirectByteBuf
/*  929:     */     extends UnpooledDirectByteBuf
/*  930:     */   {
/*  931:1087 */     private static final Recycler<ThreadLocalDirectByteBuf> RECYCLER = new Recycler()
/*  932:     */     {
/*  933:     */       protected ByteBufUtil.ThreadLocalDirectByteBuf newObject(Recycler.Handle<ByteBufUtil.ThreadLocalDirectByteBuf> handle)
/*  934:     */       {
/*  935:1090 */         return new ByteBufUtil.ThreadLocalDirectByteBuf(handle, null);
/*  936:     */       }
/*  937:     */     };
/*  938:     */     private final Recycler.Handle<ThreadLocalDirectByteBuf> handle;
/*  939:     */     
/*  940:     */     static ThreadLocalDirectByteBuf newInstance()
/*  941:     */     {
/*  942:1095 */       ThreadLocalDirectByteBuf buf = (ThreadLocalDirectByteBuf)RECYCLER.get();
/*  943:1096 */       buf.setRefCnt(1);
/*  944:1097 */       return buf;
/*  945:     */     }
/*  946:     */     
/*  947:     */     private ThreadLocalDirectByteBuf(Recycler.Handle<ThreadLocalDirectByteBuf> handle)
/*  948:     */     {
/*  949:1103 */       super(256, 2147483647);
/*  950:1104 */       this.handle = handle;
/*  951:     */     }
/*  952:     */     
/*  953:     */     protected void deallocate()
/*  954:     */     {
/*  955:1109 */       if (capacity() > ByteBufUtil.THREAD_LOCAL_BUFFER_SIZE)
/*  956:     */       {
/*  957:1110 */         super.deallocate();
/*  958:     */       }
/*  959:     */       else
/*  960:     */       {
/*  961:1112 */         clear();
/*  962:1113 */         this.handle.recycle(this);
/*  963:     */       }
/*  964:     */     }
/*  965:     */   }
/*  966:     */   
/*  967:     */   public static boolean isText(ByteBuf buf, Charset charset)
/*  968:     */   {
/*  969:1126 */     return isText(buf, buf.readerIndex(), buf.readableBytes(), charset);
/*  970:     */   }
/*  971:     */   
/*  972:     */   public static boolean isText(ByteBuf buf, int index, int length, Charset charset)
/*  973:     */   {
/*  974:1141 */     ObjectUtil.checkNotNull(buf, "buf");
/*  975:1142 */     ObjectUtil.checkNotNull(charset, "charset");
/*  976:1143 */     int maxIndex = buf.readerIndex() + buf.readableBytes();
/*  977:1144 */     if ((index < 0) || (length < 0) || (index > maxIndex - length)) {
/*  978:1145 */       throw new IndexOutOfBoundsException("index: " + index + " length: " + length);
/*  979:     */     }
/*  980:1147 */     if (charset.equals(CharsetUtil.UTF_8)) {
/*  981:1148 */       return isUtf8(buf, index, length);
/*  982:     */     }
/*  983:1149 */     if (charset.equals(CharsetUtil.US_ASCII)) {
/*  984:1150 */       return isAscii(buf, index, length);
/*  985:     */     }
/*  986:1152 */     CharsetDecoder decoder = CharsetUtil.decoder(charset, CodingErrorAction.REPORT, CodingErrorAction.REPORT);
/*  987:     */     try
/*  988:     */     {
/*  989:1154 */       if (buf.nioBufferCount() == 1)
/*  990:     */       {
/*  991:1155 */         decoder.decode(buf.internalNioBuffer(index, length));
/*  992:     */       }
/*  993:     */       else
/*  994:     */       {
/*  995:1157 */         ByteBuf heapBuffer = buf.alloc().heapBuffer(length);
/*  996:     */         try
/*  997:     */         {
/*  998:1159 */           heapBuffer.writeBytes(buf, index, length);
/*  999:1160 */           decoder.decode(heapBuffer.internalNioBuffer(heapBuffer.readerIndex(), length));
/* 1000:     */         }
/* 1001:     */         finally
/* 1002:     */         {
/* 1003:1162 */           heapBuffer.release();
/* 1004:     */         }
/* 1005:     */       }
/* 1006:1165 */       return true;
/* 1007:     */     }
/* 1008:     */     catch (CharacterCodingException ignore) {}
/* 1009:1167 */     return false;
/* 1010:     */   }
/* 1011:     */   
/* 1012:1175 */   private static final ByteProcessor FIND_NON_ASCII = new ByteProcessor()
/* 1013:     */   {
/* 1014:     */     public boolean process(byte value)
/* 1015:     */     {
/* 1016:1178 */       return value >= 0;
/* 1017:     */     }
/* 1018:     */   };
/* 1019:     */   
/* 1020:     */   private static boolean isAscii(ByteBuf buf, int index, int length)
/* 1021:     */   {
/* 1022:1191 */     return buf.forEachByte(index, length, FIND_NON_ASCII) == -1;
/* 1023:     */   }
/* 1024:     */   
/* 1025:     */   private static boolean isUtf8(ByteBuf buf, int index, int length)
/* 1026:     */   {
/* 1027:1238 */     int endIndex = index + length;
/* 1028:1239 */     while (index < endIndex)
/* 1029:     */     {
/* 1030:1240 */       byte b1 = buf.getByte(index++);
/* 1031:1242 */       if ((b1 & 0x80) != 0) {
/* 1032:1246 */         if ((b1 & 0xE0) == 192)
/* 1033:     */         {
/* 1034:1252 */           if (index >= endIndex) {
/* 1035:1253 */             return false;
/* 1036:     */           }
/* 1037:1255 */           byte b2 = buf.getByte(index++);
/* 1038:1256 */           if ((b2 & 0xC0) != 128) {
/* 1039:1257 */             return false;
/* 1040:     */           }
/* 1041:1259 */           if ((b1 & 0xFF) < 194) {
/* 1042:1260 */             return false;
/* 1043:     */           }
/* 1044:     */         }
/* 1045:1262 */         else if ((b1 & 0xF0) == 224)
/* 1046:     */         {
/* 1047:1271 */           if (index > endIndex - 2) {
/* 1048:1272 */             return false;
/* 1049:     */           }
/* 1050:1274 */           byte b2 = buf.getByte(index++);
/* 1051:1275 */           byte b3 = buf.getByte(index++);
/* 1052:1276 */           if (((b2 & 0xC0) != 128) || ((b3 & 0xC0) != 128)) {
/* 1053:1277 */             return false;
/* 1054:     */           }
/* 1055:1279 */           if (((b1 & 0xF) == 0) && ((b2 & 0xFF) < 160)) {
/* 1056:1280 */             return false;
/* 1057:     */           }
/* 1058:1282 */           if (((b1 & 0xF) == 13) && ((b2 & 0xFF) > 159)) {
/* 1059:1283 */             return false;
/* 1060:     */           }
/* 1061:     */         }
/* 1062:1285 */         else if ((b1 & 0xF8) == 240)
/* 1063:     */         {
/* 1064:1293 */           if (index > endIndex - 3) {
/* 1065:1294 */             return false;
/* 1066:     */           }
/* 1067:1296 */           byte b2 = buf.getByte(index++);
/* 1068:1297 */           byte b3 = buf.getByte(index++);
/* 1069:1298 */           byte b4 = buf.getByte(index++);
/* 1070:1299 */           if (((b2 & 0xC0) != 128) || ((b3 & 0xC0) != 128) || ((b4 & 0xC0) != 128)) {
/* 1071:1301 */             return false;
/* 1072:     */           }
/* 1073:1303 */           if (((b1 & 0xFF) > 244) || (((b1 & 0xFF) == 240) && ((b2 & 0xFF) < 144)) || (((b1 & 0xFF) == 244) && ((b2 & 0xFF) > 143))) {
/* 1074:1306 */             return false;
/* 1075:     */           }
/* 1076:     */         }
/* 1077:     */         else
/* 1078:     */         {
/* 1079:1309 */           return false;
/* 1080:     */         }
/* 1081:     */       }
/* 1082:     */     }
/* 1083:1312 */     return true;
/* 1084:     */   }
/* 1085:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBufUtil
 * JD-Core Version:    0.7.0.1
 */