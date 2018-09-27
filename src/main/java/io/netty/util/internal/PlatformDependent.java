/*    1:     */ package io.netty.util.internal;
/*    2:     */ 
/*    3:     */ import io.netty.util.internal.logging.InternalLogger;
/*    4:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*    5:     */ import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
/*    6:     */ import io.netty.util.internal.shaded.org.jctools.queues.MpscChunkedArrayQueue;
/*    7:     */ import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;
/*    8:     */ import io.netty.util.internal.shaded.org.jctools.queues.SpscLinkedQueue;
/*    9:     */ import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueue;
/*   10:     */ import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscGrowableAtomicArrayQueue;
/*   11:     */ import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;
/*   12:     */ import io.netty.util.internal.shaded.org.jctools.queues.atomic.SpscLinkedAtomicQueue;
/*   13:     */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*   14:     */ import java.io.File;
/*   15:     */ import java.lang.reflect.Method;
/*   16:     */ import java.nio.ByteBuffer;
/*   17:     */ import java.nio.ByteOrder;
/*   18:     */ import java.security.AccessController;
/*   19:     */ import java.security.PrivilegedAction;
/*   20:     */ import java.util.Deque;
/*   21:     */ import java.util.List;
/*   22:     */ import java.util.Locale;
/*   23:     */ import java.util.Map;
/*   24:     */ import java.util.Queue;
/*   25:     */ import java.util.Random;
/*   26:     */ import java.util.concurrent.ConcurrentHashMap;
/*   27:     */ import java.util.concurrent.ConcurrentLinkedDeque;
/*   28:     */ import java.util.concurrent.ConcurrentMap;
/*   29:     */ import java.util.concurrent.LinkedBlockingDeque;
/*   30:     */ import java.util.concurrent.atomic.AtomicLong;
/*   31:     */ import java.util.regex.Matcher;
/*   32:     */ import java.util.regex.Pattern;
/*   33:     */ 
/*   34:     */ public final class PlatformDependent
/*   35:     */ {
/*   36:     */   private static final InternalLogger logger;
/*   37:     */   private static final Pattern MAX_DIRECT_MEMORY_SIZE_ARG_PATTERN;
/*   38:     */   private static final boolean IS_WINDOWS;
/*   39:     */   private static final boolean IS_OSX;
/*   40:     */   private static final boolean MAYBE_SUPER_USER;
/*   41:     */   private static final boolean CAN_ENABLE_TCP_NODELAY_BY_DEFAULT;
/*   42:     */   private static final boolean HAS_UNSAFE;
/*   43:     */   private static final boolean DIRECT_BUFFER_PREFERRED;
/*   44:     */   private static final long MAX_DIRECT_MEMORY;
/*   45:     */   private static final int MPSC_CHUNK_SIZE = 1024;
/*   46:     */   private static final int MIN_MAX_MPSC_CAPACITY = 2048;
/*   47:     */   private static final int MAX_ALLOWED_MPSC_CAPACITY = 1073741824;
/*   48:     */   private static final long BYTE_ARRAY_BASE_OFFSET;
/*   49:     */   private static final File TMPDIR;
/*   50:     */   private static final int BIT_MODE;
/*   51:     */   private static final String NORMALIZED_ARCH;
/*   52:     */   private static final String NORMALIZED_OS;
/*   53:     */   private static final int ADDRESS_SIZE;
/*   54:     */   private static final boolean USE_DIRECT_BUFFER_NO_CLEANER;
/*   55:     */   private static final AtomicLong DIRECT_MEMORY_COUNTER;
/*   56:     */   private static final long DIRECT_MEMORY_LIMIT;
/*   57:     */   private static final ThreadLocalRandomProvider RANDOM_PROVIDER;
/*   58:     */   private static final Cleaner CLEANER;
/*   59:     */   private static final int UNINITIALIZED_ARRAY_ALLOCATION_THRESHOLD;
/*   60:     */   public static final boolean BIG_ENDIAN_NATIVE_ORDER;
/*   61:     */   private static final Cleaner NOOP;
/*   62:     */   
/*   63:     */   static
/*   64:     */   {
/*   65:  69 */     logger = InternalLoggerFactory.getInstance(PlatformDependent.class);
/*   66:     */     
/*   67:  71 */     MAX_DIRECT_MEMORY_SIZE_ARG_PATTERN = Pattern.compile("\\s*-XX:MaxDirectMemorySize\\s*=\\s*([0-9]+)\\s*([kKmMgG]?)\\s*$");
/*   68:     */     
/*   69:     */ 
/*   70:  74 */     IS_WINDOWS = isWindows0();
/*   71:  75 */     IS_OSX = isOsx0();
/*   72:     */     
/*   73:     */ 
/*   74:     */ 
/*   75:  79 */     CAN_ENABLE_TCP_NODELAY_BY_DEFAULT = !isAndroid();
/*   76:     */     
/*   77:  81 */     HAS_UNSAFE = hasUnsafe0();
/*   78:     */     
/*   79:  83 */     DIRECT_BUFFER_PREFERRED = (HAS_UNSAFE) && (!SystemPropertyUtil.getBoolean("io.netty.noPreferDirect", false));
/*   80:  84 */     MAX_DIRECT_MEMORY = maxDirectMemory0();
/*   81:     */     
/*   82:     */ 
/*   83:     */ 
/*   84:     */ 
/*   85:     */ 
/*   86:  90 */     BYTE_ARRAY_BASE_OFFSET = byteArrayBaseOffset0();
/*   87:     */     
/*   88:  92 */     TMPDIR = tmpdir0();
/*   89:     */     
/*   90:  94 */     BIT_MODE = bitMode0();
/*   91:  95 */     NORMALIZED_ARCH = normalizeArch(SystemPropertyUtil.get("os.arch", ""));
/*   92:  96 */     NORMALIZED_OS = normalizeOs(SystemPropertyUtil.get("os.name", ""));
/*   93:     */     
/*   94:  98 */     ADDRESS_SIZE = addressSize0();
/*   95:     */     
/*   96:     */ 
/*   97:     */ 
/*   98:     */ 
/*   99:     */ 
/*  100:     */ 
/*  101:     */ 
/*  102: 106 */     BIG_ENDIAN_NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
/*  103:     */     
/*  104: 108 */     NOOP = new Cleaner()
/*  105:     */     {
/*  106:     */       public void freeDirectBuffer(ByteBuffer buffer) {}
/*  107:     */     };
/*  108: 116 */     if (javaVersion() >= 7) {
/*  109: 117 */       RANDOM_PROVIDER = new ThreadLocalRandomProvider()
/*  110:     */       {
/*  111:     */         public Random current()
/*  112:     */         {
/*  113: 120 */           return java.util.concurrent.ThreadLocalRandom.current();
/*  114:     */         }
/*  115:     */       };
/*  116:     */     } else {
/*  117: 124 */       RANDOM_PROVIDER = new ThreadLocalRandomProvider()
/*  118:     */       {
/*  119:     */         public Random current()
/*  120:     */         {
/*  121: 127 */           return ThreadLocalRandom.current();
/*  122:     */         }
/*  123:     */       };
/*  124:     */     }
/*  125: 131 */     if (logger.isDebugEnabled()) {
/*  126: 132 */       logger.debug("-Dio.netty.noPreferDirect: {}", Boolean.valueOf(!DIRECT_BUFFER_PREFERRED));
/*  127:     */     }
/*  128: 139 */     if ((!hasUnsafe()) && (!isAndroid()) && (!PlatformDependent0.isExplicitNoUnsafe())) {
/*  129: 140 */       logger.info("Your platform does not provide complete low-level API for accessing direct buffers reliably. Unless explicitly requested, heap buffer will always be preferred to avoid potential system instability.");
/*  130:     */     }
/*  131: 153 */     long maxDirectMemory = SystemPropertyUtil.getLong("io.netty.maxDirectMemory", -1L);
/*  132: 155 */     if ((maxDirectMemory == 0L) || (!hasUnsafe()) || (!PlatformDependent0.hasDirectBufferNoCleanerConstructor()))
/*  133:     */     {
/*  134: 156 */       USE_DIRECT_BUFFER_NO_CLEANER = false;
/*  135: 157 */       DIRECT_MEMORY_COUNTER = null;
/*  136:     */     }
/*  137:     */     else
/*  138:     */     {
/*  139: 159 */       USE_DIRECT_BUFFER_NO_CLEANER = true;
/*  140: 160 */       if (maxDirectMemory < 0L)
/*  141:     */       {
/*  142: 161 */         maxDirectMemory = maxDirectMemory0();
/*  143: 162 */         if (maxDirectMemory <= 0L) {
/*  144: 163 */           DIRECT_MEMORY_COUNTER = null;
/*  145:     */         } else {
/*  146: 165 */           DIRECT_MEMORY_COUNTER = new AtomicLong();
/*  147:     */         }
/*  148:     */       }
/*  149:     */       else
/*  150:     */       {
/*  151: 168 */         DIRECT_MEMORY_COUNTER = new AtomicLong();
/*  152:     */       }
/*  153:     */     }
/*  154: 171 */     DIRECT_MEMORY_LIMIT = maxDirectMemory;
/*  155: 172 */     logger.debug("-Dio.netty.maxDirectMemory: {} bytes", Long.valueOf(maxDirectMemory));
/*  156:     */     
/*  157:     */ 
/*  158: 175 */     int tryAllocateUninitializedArray = SystemPropertyUtil.getInt("io.netty.uninitializedArrayAllocationThreshold", 1024);
/*  159: 176 */     UNINITIALIZED_ARRAY_ALLOCATION_THRESHOLD = (javaVersion() >= 9) && (PlatformDependent0.hasAllocateArrayMethod()) ? tryAllocateUninitializedArray : -1;
/*  160:     */     
/*  161: 178 */     logger.debug("-Dio.netty.uninitializedArrayAllocationThreshold: {}", Integer.valueOf(UNINITIALIZED_ARRAY_ALLOCATION_THRESHOLD));
/*  162:     */     
/*  163: 180 */     MAYBE_SUPER_USER = maybeSuperUser0();
/*  164: 182 */     if ((!isAndroid()) && (hasUnsafe()))
/*  165:     */     {
/*  166: 185 */       if (javaVersion() >= 9) {
/*  167: 186 */         CLEANER = CleanerJava9.isSupported() ? new CleanerJava9() : NOOP;
/*  168:     */       } else {
/*  169: 188 */         CLEANER = CleanerJava6.isSupported() ? new CleanerJava6() : NOOP;
/*  170:     */       }
/*  171:     */     }
/*  172:     */     else {
/*  173: 191 */       CLEANER = NOOP;
/*  174:     */     }
/*  175:     */   }
/*  176:     */   
/*  177:     */   public static boolean hasDirectBufferNoCleanerConstructor()
/*  178:     */   {
/*  179: 196 */     return PlatformDependent0.hasDirectBufferNoCleanerConstructor();
/*  180:     */   }
/*  181:     */   
/*  182:     */   public static byte[] allocateUninitializedArray(int size)
/*  183:     */   {
/*  184: 200 */     return (UNINITIALIZED_ARRAY_ALLOCATION_THRESHOLD < 0) || (UNINITIALIZED_ARRAY_ALLOCATION_THRESHOLD > size) ? new byte[size] : 
/*  185: 201 */       PlatformDependent0.allocateUninitializedArray(size);
/*  186:     */   }
/*  187:     */   
/*  188:     */   public static boolean isAndroid()
/*  189:     */   {
/*  190: 208 */     return PlatformDependent0.isAndroid();
/*  191:     */   }
/*  192:     */   
/*  193:     */   public static boolean isWindows()
/*  194:     */   {
/*  195: 215 */     return IS_WINDOWS;
/*  196:     */   }
/*  197:     */   
/*  198:     */   public static boolean isOsx()
/*  199:     */   {
/*  200: 222 */     return IS_OSX;
/*  201:     */   }
/*  202:     */   
/*  203:     */   public static boolean maybeSuperUser()
/*  204:     */   {
/*  205: 230 */     return MAYBE_SUPER_USER;
/*  206:     */   }
/*  207:     */   
/*  208:     */   public static int javaVersion()
/*  209:     */   {
/*  210: 237 */     return PlatformDependent0.javaVersion();
/*  211:     */   }
/*  212:     */   
/*  213:     */   public static boolean canEnableTcpNoDelayByDefault()
/*  214:     */   {
/*  215: 244 */     return CAN_ENABLE_TCP_NODELAY_BY_DEFAULT;
/*  216:     */   }
/*  217:     */   
/*  218:     */   public static boolean hasUnsafe()
/*  219:     */   {
/*  220: 252 */     return HAS_UNSAFE;
/*  221:     */   }
/*  222:     */   
/*  223:     */   public static Throwable getUnsafeUnavailabilityCause()
/*  224:     */   {
/*  225: 259 */     return PlatformDependent0.getUnsafeUnavailabilityCause();
/*  226:     */   }
/*  227:     */   
/*  228:     */   public static boolean isUnaligned()
/*  229:     */   {
/*  230: 268 */     return PlatformDependent0.isUnaligned();
/*  231:     */   }
/*  232:     */   
/*  233:     */   public static boolean directBufferPreferred()
/*  234:     */   {
/*  235: 276 */     return DIRECT_BUFFER_PREFERRED;
/*  236:     */   }
/*  237:     */   
/*  238:     */   public static long maxDirectMemory()
/*  239:     */   {
/*  240: 283 */     return MAX_DIRECT_MEMORY;
/*  241:     */   }
/*  242:     */   
/*  243:     */   public static File tmpdir()
/*  244:     */   {
/*  245: 290 */     return TMPDIR;
/*  246:     */   }
/*  247:     */   
/*  248:     */   public static int bitMode()
/*  249:     */   {
/*  250: 297 */     return BIT_MODE;
/*  251:     */   }
/*  252:     */   
/*  253:     */   public static int addressSize()
/*  254:     */   {
/*  255: 305 */     return ADDRESS_SIZE;
/*  256:     */   }
/*  257:     */   
/*  258:     */   public static long allocateMemory(long size)
/*  259:     */   {
/*  260: 309 */     return PlatformDependent0.allocateMemory(size);
/*  261:     */   }
/*  262:     */   
/*  263:     */   public static void freeMemory(long address)
/*  264:     */   {
/*  265: 313 */     PlatformDependent0.freeMemory(address);
/*  266:     */   }
/*  267:     */   
/*  268:     */   public static long reallocateMemory(long address, long newSize)
/*  269:     */   {
/*  270: 317 */     return PlatformDependent0.reallocateMemory(address, newSize);
/*  271:     */   }
/*  272:     */   
/*  273:     */   public static void throwException(Throwable t)
/*  274:     */   {
/*  275: 324 */     if (hasUnsafe()) {
/*  276: 325 */       PlatformDependent0.throwException(t);
/*  277:     */     } else {
/*  278: 327 */       throwException0(t);
/*  279:     */     }
/*  280:     */   }
/*  281:     */   
/*  282:     */   private static <E extends Throwable> void throwException0(Throwable t)
/*  283:     */     throws Throwable
/*  284:     */   {
/*  285: 333 */     throw t;
/*  286:     */   }
/*  287:     */   
/*  288:     */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap()
/*  289:     */   {
/*  290: 340 */     return new ConcurrentHashMap();
/*  291:     */   }
/*  292:     */   
/*  293:     */   public static LongCounter newLongCounter()
/*  294:     */   {
/*  295: 347 */     if (javaVersion() >= 8) {
/*  296: 348 */       return new LongAdderCounter();
/*  297:     */     }
/*  298: 350 */     return new AtomicLongCounter(null);
/*  299:     */   }
/*  300:     */   
/*  301:     */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity)
/*  302:     */   {
/*  303: 358 */     return new ConcurrentHashMap(initialCapacity);
/*  304:     */   }
/*  305:     */   
/*  306:     */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity, float loadFactor)
/*  307:     */   {
/*  308: 365 */     return new ConcurrentHashMap(initialCapacity, loadFactor);
/*  309:     */   }
/*  310:     */   
/*  311:     */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel)
/*  312:     */   {
/*  313: 373 */     return new ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
/*  314:     */   }
/*  315:     */   
/*  316:     */   public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(Map<? extends K, ? extends V> map)
/*  317:     */   {
/*  318: 380 */     return new ConcurrentHashMap(map);
/*  319:     */   }
/*  320:     */   
/*  321:     */   public static void freeDirectBuffer(ByteBuffer buffer)
/*  322:     */   {
/*  323: 388 */     CLEANER.freeDirectBuffer(buffer);
/*  324:     */   }
/*  325:     */   
/*  326:     */   public static long directBufferAddress(ByteBuffer buffer)
/*  327:     */   {
/*  328: 392 */     return PlatformDependent0.directBufferAddress(buffer);
/*  329:     */   }
/*  330:     */   
/*  331:     */   public static ByteBuffer directBuffer(long memoryAddress, int size)
/*  332:     */   {
/*  333: 396 */     if (PlatformDependent0.hasDirectBufferNoCleanerConstructor()) {
/*  334: 397 */       return PlatformDependent0.newDirectBuffer(memoryAddress, size);
/*  335:     */     }
/*  336: 399 */     throw new UnsupportedOperationException("sun.misc.Unsafe or java.nio.DirectByteBuffer.<init>(long, int) not available");
/*  337:     */   }
/*  338:     */   
/*  339:     */   public static int getInt(Object object, long fieldOffset)
/*  340:     */   {
/*  341: 404 */     return PlatformDependent0.getInt(object, fieldOffset);
/*  342:     */   }
/*  343:     */   
/*  344:     */   public static byte getByte(long address)
/*  345:     */   {
/*  346: 408 */     return PlatformDependent0.getByte(address);
/*  347:     */   }
/*  348:     */   
/*  349:     */   public static short getShort(long address)
/*  350:     */   {
/*  351: 412 */     return PlatformDependent0.getShort(address);
/*  352:     */   }
/*  353:     */   
/*  354:     */   public static int getInt(long address)
/*  355:     */   {
/*  356: 416 */     return PlatformDependent0.getInt(address);
/*  357:     */   }
/*  358:     */   
/*  359:     */   public static long getLong(long address)
/*  360:     */   {
/*  361: 420 */     return PlatformDependent0.getLong(address);
/*  362:     */   }
/*  363:     */   
/*  364:     */   public static byte getByte(byte[] data, int index)
/*  365:     */   {
/*  366: 424 */     return PlatformDependent0.getByte(data, index);
/*  367:     */   }
/*  368:     */   
/*  369:     */   public static short getShort(byte[] data, int index)
/*  370:     */   {
/*  371: 428 */     return PlatformDependent0.getShort(data, index);
/*  372:     */   }
/*  373:     */   
/*  374:     */   public static int getInt(byte[] data, int index)
/*  375:     */   {
/*  376: 432 */     return PlatformDependent0.getInt(data, index);
/*  377:     */   }
/*  378:     */   
/*  379:     */   public static long getLong(byte[] data, int index)
/*  380:     */   {
/*  381: 436 */     return PlatformDependent0.getLong(data, index);
/*  382:     */   }
/*  383:     */   
/*  384:     */   private static long getLongSafe(byte[] bytes, int offset)
/*  385:     */   {
/*  386: 440 */     if (BIG_ENDIAN_NATIVE_ORDER) {
/*  387: 441 */       return bytes[offset] << 56 | (bytes[(offset + 1)] & 0xFF) << 48 | (bytes[(offset + 2)] & 0xFF) << 40 | (bytes[(offset + 3)] & 0xFF) << 32 | (bytes[(offset + 4)] & 0xFF) << 24 | (bytes[(offset + 5)] & 0xFF) << 16 | (bytes[(offset + 6)] & 0xFF) << 8 | bytes[(offset + 7)] & 0xFF;
/*  388:     */     }
/*  389: 450 */     return bytes[offset] & 0xFF | (bytes[(offset + 1)] & 0xFF) << 8 | (bytes[(offset + 2)] & 0xFF) << 16 | (bytes[(offset + 3)] & 0xFF) << 24 | (bytes[(offset + 4)] & 0xFF) << 32 | (bytes[(offset + 5)] & 0xFF) << 40 | (bytes[(offset + 6)] & 0xFF) << 48 | bytes[(offset + 7)] << 56;
/*  390:     */   }
/*  391:     */   
/*  392:     */   private static int getIntSafe(byte[] bytes, int offset)
/*  393:     */   {
/*  394: 461 */     if (BIG_ENDIAN_NATIVE_ORDER) {
/*  395: 462 */       return bytes[offset] << 24 | (bytes[(offset + 1)] & 0xFF) << 16 | (bytes[(offset + 2)] & 0xFF) << 8 | bytes[(offset + 3)] & 0xFF;
/*  396:     */     }
/*  397: 467 */     return bytes[offset] & 0xFF | (bytes[(offset + 1)] & 0xFF) << 8 | (bytes[(offset + 2)] & 0xFF) << 16 | bytes[(offset + 3)] << 24;
/*  398:     */   }
/*  399:     */   
/*  400:     */   private static short getShortSafe(byte[] bytes, int offset)
/*  401:     */   {
/*  402: 474 */     if (BIG_ENDIAN_NATIVE_ORDER) {
/*  403: 475 */       return (short)(bytes[offset] << 8 | bytes[(offset + 1)] & 0xFF);
/*  404:     */     }
/*  405: 477 */     return (short)(bytes[offset] & 0xFF | bytes[(offset + 1)] << 8);
/*  406:     */   }
/*  407:     */   
/*  408:     */   private static int hashCodeAsciiCompute(CharSequence value, int offset, int hash)
/*  409:     */   {
/*  410: 484 */     if (BIG_ENDIAN_NATIVE_ORDER) {
/*  411: 485 */       return 
/*  412:     */       
/*  413:     */ 
/*  414:     */ 
/*  415: 489 */         hash * -862048943 + hashCodeAsciiSanitizeInt(value, offset + 4) * 461845907 + hashCodeAsciiSanitizeInt(value, offset);
/*  416:     */     }
/*  417: 491 */     return 
/*  418:     */     
/*  419:     */ 
/*  420:     */ 
/*  421: 495 */       hash * -862048943 + hashCodeAsciiSanitizeInt(value, offset) * 461845907 + hashCodeAsciiSanitizeInt(value, offset + 4);
/*  422:     */   }
/*  423:     */   
/*  424:     */   private static int hashCodeAsciiSanitizeInt(CharSequence value, int offset)
/*  425:     */   {
/*  426: 502 */     if (BIG_ENDIAN_NATIVE_ORDER) {
/*  427: 504 */       return 
/*  428:     */       
/*  429:     */ 
/*  430: 507 */         value.charAt(offset + 3) & 0x1F | (value.charAt(offset + 2) & 0x1F) << '\b' | (value.charAt(offset + 1) & 0x1F) << '\020' | (value.charAt(offset) & 0x1F) << '\030';
/*  431:     */     }
/*  432: 509 */     return 
/*  433:     */     
/*  434:     */ 
/*  435: 512 */       (value.charAt(offset + 3) & 0x1F) << '\030' | (value.charAt(offset + 2) & 0x1F) << '\020' | (value.charAt(offset + 1) & 0x1F) << '\b' | value.charAt(offset) & 0x1F;
/*  436:     */   }
/*  437:     */   
/*  438:     */   private static int hashCodeAsciiSanitizeShort(CharSequence value, int offset)
/*  439:     */   {
/*  440: 519 */     if (BIG_ENDIAN_NATIVE_ORDER) {
/*  441: 521 */       return 
/*  442: 522 */         value.charAt(offset + 1) & 0x1F | (value.charAt(offset) & 0x1F) << '\b';
/*  443:     */     }
/*  444: 524 */     return 
/*  445: 525 */       (value.charAt(offset + 1) & 0x1F) << '\b' | value.charAt(offset) & 0x1F;
/*  446:     */   }
/*  447:     */   
/*  448:     */   private static int hashCodeAsciiSanitizeByte(char value)
/*  449:     */   {
/*  450: 532 */     return value & 0x1F;
/*  451:     */   }
/*  452:     */   
/*  453:     */   public static void putByte(long address, byte value)
/*  454:     */   {
/*  455: 536 */     PlatformDependent0.putByte(address, value);
/*  456:     */   }
/*  457:     */   
/*  458:     */   public static void putShort(long address, short value)
/*  459:     */   {
/*  460: 540 */     PlatformDependent0.putShort(address, value);
/*  461:     */   }
/*  462:     */   
/*  463:     */   public static void putInt(long address, int value)
/*  464:     */   {
/*  465: 544 */     PlatformDependent0.putInt(address, value);
/*  466:     */   }
/*  467:     */   
/*  468:     */   public static void putLong(long address, long value)
/*  469:     */   {
/*  470: 548 */     PlatformDependent0.putLong(address, value);
/*  471:     */   }
/*  472:     */   
/*  473:     */   public static void putByte(byte[] data, int index, byte value)
/*  474:     */   {
/*  475: 552 */     PlatformDependent0.putByte(data, index, value);
/*  476:     */   }
/*  477:     */   
/*  478:     */   public static void putShort(byte[] data, int index, short value)
/*  479:     */   {
/*  480: 556 */     PlatformDependent0.putShort(data, index, value);
/*  481:     */   }
/*  482:     */   
/*  483:     */   public static void putInt(byte[] data, int index, int value)
/*  484:     */   {
/*  485: 560 */     PlatformDependent0.putInt(data, index, value);
/*  486:     */   }
/*  487:     */   
/*  488:     */   public static void putLong(byte[] data, int index, long value)
/*  489:     */   {
/*  490: 564 */     PlatformDependent0.putLong(data, index, value);
/*  491:     */   }
/*  492:     */   
/*  493:     */   public static void copyMemory(long srcAddr, long dstAddr, long length)
/*  494:     */   {
/*  495: 568 */     PlatformDependent0.copyMemory(srcAddr, dstAddr, length);
/*  496:     */   }
/*  497:     */   
/*  498:     */   public static void copyMemory(byte[] src, int srcIndex, long dstAddr, long length)
/*  499:     */   {
/*  500: 572 */     PlatformDependent0.copyMemory(src, BYTE_ARRAY_BASE_OFFSET + srcIndex, null, dstAddr, length);
/*  501:     */   }
/*  502:     */   
/*  503:     */   public static void copyMemory(long srcAddr, byte[] dst, int dstIndex, long length)
/*  504:     */   {
/*  505: 576 */     PlatformDependent0.copyMemory(null, srcAddr, dst, BYTE_ARRAY_BASE_OFFSET + dstIndex, length);
/*  506:     */   }
/*  507:     */   
/*  508:     */   public static void setMemory(byte[] dst, int dstIndex, long bytes, byte value)
/*  509:     */   {
/*  510: 580 */     PlatformDependent0.setMemory(dst, BYTE_ARRAY_BASE_OFFSET + dstIndex, bytes, value);
/*  511:     */   }
/*  512:     */   
/*  513:     */   public static void setMemory(long address, long bytes, byte value)
/*  514:     */   {
/*  515: 584 */     PlatformDependent0.setMemory(address, bytes, value);
/*  516:     */   }
/*  517:     */   
/*  518:     */   public static ByteBuffer allocateDirectNoCleaner(int capacity)
/*  519:     */   {
/*  520: 592 */     assert (USE_DIRECT_BUFFER_NO_CLEANER);
/*  521:     */     
/*  522: 594 */     incrementMemoryCounter(capacity);
/*  523:     */     try
/*  524:     */     {
/*  525: 596 */       return PlatformDependent0.allocateDirectNoCleaner(capacity);
/*  526:     */     }
/*  527:     */     catch (Throwable e)
/*  528:     */     {
/*  529: 598 */       decrementMemoryCounter(capacity);
/*  530: 599 */       throwException(e);
/*  531:     */     }
/*  532: 600 */     return null;
/*  533:     */   }
/*  534:     */   
/*  535:     */   public static ByteBuffer reallocateDirectNoCleaner(ByteBuffer buffer, int capacity)
/*  536:     */   {
/*  537: 609 */     assert (USE_DIRECT_BUFFER_NO_CLEANER);
/*  538:     */     
/*  539: 611 */     int len = capacity - buffer.capacity();
/*  540: 612 */     incrementMemoryCounter(len);
/*  541:     */     try
/*  542:     */     {
/*  543: 614 */       return PlatformDependent0.reallocateDirectNoCleaner(buffer, capacity);
/*  544:     */     }
/*  545:     */     catch (Throwable e)
/*  546:     */     {
/*  547: 616 */       decrementMemoryCounter(len);
/*  548: 617 */       throwException(e);
/*  549:     */     }
/*  550: 618 */     return null;
/*  551:     */   }
/*  552:     */   
/*  553:     */   public static void freeDirectNoCleaner(ByteBuffer buffer)
/*  554:     */   {
/*  555: 627 */     assert (USE_DIRECT_BUFFER_NO_CLEANER);
/*  556:     */     
/*  557: 629 */     int capacity = buffer.capacity();
/*  558: 630 */     PlatformDependent0.freeMemory(PlatformDependent0.directBufferAddress(buffer));
/*  559: 631 */     decrementMemoryCounter(capacity);
/*  560:     */   }
/*  561:     */   
/*  562:     */   private static void incrementMemoryCounter(int capacity)
/*  563:     */   {
/*  564: 635 */     if (DIRECT_MEMORY_COUNTER != null) {
/*  565:     */       for (;;)
/*  566:     */       {
/*  567: 637 */         long usedMemory = DIRECT_MEMORY_COUNTER.get();
/*  568: 638 */         long newUsedMemory = usedMemory + capacity;
/*  569: 639 */         if (newUsedMemory > DIRECT_MEMORY_LIMIT) {
/*  570: 640 */           throw new OutOfDirectMemoryError("failed to allocate " + capacity + " byte(s) of direct memory (used: " + usedMemory + ", max: " + DIRECT_MEMORY_LIMIT + ')');
/*  571:     */         }
/*  572: 643 */         if (DIRECT_MEMORY_COUNTER.compareAndSet(usedMemory, newUsedMemory)) {
/*  573:     */           break;
/*  574:     */         }
/*  575:     */       }
/*  576:     */     }
/*  577:     */   }
/*  578:     */   
/*  579:     */   private static void decrementMemoryCounter(int capacity)
/*  580:     */   {
/*  581: 651 */     if (DIRECT_MEMORY_COUNTER != null)
/*  582:     */     {
/*  583: 652 */       long usedMemory = DIRECT_MEMORY_COUNTER.addAndGet(-capacity);
/*  584: 653 */       assert (usedMemory >= 0L);
/*  585:     */     }
/*  586:     */   }
/*  587:     */   
/*  588:     */   public static boolean useDirectBufferNoCleaner()
/*  589:     */   {
/*  590: 658 */     return USE_DIRECT_BUFFER_NO_CLEANER;
/*  591:     */   }
/*  592:     */   
/*  593:     */   public static boolean equals(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length)
/*  594:     */   {
/*  595: 673 */     return (!hasUnsafe()) || (!PlatformDependent0.unalignedAccess()) ? 
/*  596: 674 */       equalsSafe(bytes1, startPos1, bytes2, startPos2, length) : 
/*  597: 675 */       PlatformDependent0.equals(bytes1, startPos1, bytes2, startPos2, length);
/*  598:     */   }
/*  599:     */   
/*  600:     */   public static boolean isZero(byte[] bytes, int startPos, int length)
/*  601:     */   {
/*  602: 686 */     return (!hasUnsafe()) || (!PlatformDependent0.unalignedAccess()) ? 
/*  603: 687 */       isZeroSafe(bytes, startPos, length) : 
/*  604: 688 */       PlatformDependent0.isZero(bytes, startPos, length);
/*  605:     */   }
/*  606:     */   
/*  607:     */   public static int equalsConstantTime(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length)
/*  608:     */   {
/*  609: 713 */     return (!hasUnsafe()) || (!PlatformDependent0.unalignedAccess()) ? 
/*  610: 714 */       ConstantTimeUtils.equalsConstantTime(bytes1, startPos1, bytes2, startPos2, length) : 
/*  611: 715 */       PlatformDependent0.equalsConstantTime(bytes1, startPos1, bytes2, startPos2, length);
/*  612:     */   }
/*  613:     */   
/*  614:     */   public static int hashCodeAscii(byte[] bytes, int startPos, int length)
/*  615:     */   {
/*  616: 728 */     return (!hasUnsafe()) || (!PlatformDependent0.unalignedAccess()) ? 
/*  617: 729 */       hashCodeAsciiSafe(bytes, startPos, length) : 
/*  618: 730 */       PlatformDependent0.hashCodeAscii(bytes, startPos, length);
/*  619:     */   }
/*  620:     */   
/*  621:     */   public static int hashCodeAscii(CharSequence bytes)
/*  622:     */   {
/*  623: 744 */     int hash = -1028477387;
/*  624: 745 */     int remainingBytes = bytes.length() & 0x7;
/*  625: 749 */     switch (bytes.length())
/*  626:     */     {
/*  627:     */     case 24: 
/*  628:     */     case 25: 
/*  629:     */     case 26: 
/*  630:     */     case 27: 
/*  631:     */     case 28: 
/*  632:     */     case 29: 
/*  633:     */     case 30: 
/*  634:     */     case 31: 
/*  635: 758 */       hash = hashCodeAsciiCompute(bytes, bytes.length() - 24, 
/*  636: 759 */         hashCodeAsciiCompute(bytes, bytes.length() - 16, 
/*  637: 760 */         hashCodeAsciiCompute(bytes, bytes.length() - 8, hash)));
/*  638: 761 */       break;
/*  639:     */     case 16: 
/*  640:     */     case 17: 
/*  641:     */     case 18: 
/*  642:     */     case 19: 
/*  643:     */     case 20: 
/*  644:     */     case 21: 
/*  645:     */     case 22: 
/*  646:     */     case 23: 
/*  647: 770 */       hash = hashCodeAsciiCompute(bytes, bytes.length() - 16, 
/*  648: 771 */         hashCodeAsciiCompute(bytes, bytes.length() - 8, hash));
/*  649: 772 */       break;
/*  650:     */     case 8: 
/*  651:     */     case 9: 
/*  652:     */     case 10: 
/*  653:     */     case 11: 
/*  654:     */     case 12: 
/*  655:     */     case 13: 
/*  656:     */     case 14: 
/*  657:     */     case 15: 
/*  658: 781 */       hash = hashCodeAsciiCompute(bytes, bytes.length() - 8, hash);
/*  659: 782 */       break;
/*  660:     */     case 0: 
/*  661:     */     case 1: 
/*  662:     */     case 2: 
/*  663:     */     case 3: 
/*  664:     */     case 4: 
/*  665:     */     case 5: 
/*  666:     */     case 6: 
/*  667:     */     case 7: 
/*  668:     */       break;
/*  669:     */     default: 
/*  670: 793 */       for (int i = bytes.length() - 8; i >= remainingBytes; i -= 8) {
/*  671: 794 */         hash = hashCodeAsciiCompute(bytes, i, hash);
/*  672:     */       }
/*  673:     */     }
/*  674: 798 */     switch (remainingBytes)
/*  675:     */     {
/*  676:     */     case 7: 
/*  677: 800 */       return 
/*  678:     */       
/*  679: 802 */         ((hash * -862048943 + hashCodeAsciiSanitizeByte(bytes.charAt(0))) * 461845907 + hashCodeAsciiSanitizeShort(bytes, 1)) * -862048943 + hashCodeAsciiSanitizeInt(bytes, 3);
/*  680:     */     case 6: 
/*  681: 804 */       return 
/*  682: 805 */         (hash * -862048943 + hashCodeAsciiSanitizeShort(bytes, 0)) * 461845907 + hashCodeAsciiSanitizeInt(bytes, 2);
/*  683:     */     case 5: 
/*  684: 807 */       return 
/*  685: 808 */         (hash * -862048943 + hashCodeAsciiSanitizeByte(bytes.charAt(0))) * 461845907 + hashCodeAsciiSanitizeInt(bytes, 1);
/*  686:     */     case 4: 
/*  687: 810 */       return hash * -862048943 + hashCodeAsciiSanitizeInt(bytes, 0);
/*  688:     */     case 3: 
/*  689: 812 */       return 
/*  690: 813 */         (hash * -862048943 + hashCodeAsciiSanitizeByte(bytes.charAt(0))) * 461845907 + hashCodeAsciiSanitizeShort(bytes, 1);
/*  691:     */     case 2: 
/*  692: 815 */       return hash * -862048943 + hashCodeAsciiSanitizeShort(bytes, 0);
/*  693:     */     case 1: 
/*  694: 817 */       return hash * -862048943 + hashCodeAsciiSanitizeByte(bytes.charAt(0));
/*  695:     */     }
/*  696: 819 */     return hash;
/*  697:     */   }
/*  698:     */   
/*  699:     */   private static final class Mpsc
/*  700:     */   {
/*  701:     */     private static final boolean USE_MPSC_CHUNKED_ARRAY_QUEUE;
/*  702:     */     
/*  703:     */     static
/*  704:     */     {
/*  705: 830 */       Object unsafe = null;
/*  706: 831 */       if (PlatformDependent.hasUnsafe()) {
/*  707: 835 */         unsafe = AccessController.doPrivileged(new PrivilegedAction()
/*  708:     */         {
/*  709:     */           public Object run()
/*  710:     */           {
/*  711: 839 */             return UnsafeAccess.UNSAFE;
/*  712:     */           }
/*  713:     */         });
/*  714:     */       }
/*  715: 844 */       if (unsafe == null)
/*  716:     */       {
/*  717: 845 */         PlatformDependent.logger.debug("org.jctools-core.MpscChunkedArrayQueue: unavailable");
/*  718: 846 */         USE_MPSC_CHUNKED_ARRAY_QUEUE = false;
/*  719:     */       }
/*  720:     */       else
/*  721:     */       {
/*  722: 848 */         PlatformDependent.logger.debug("org.jctools-core.MpscChunkedArrayQueue: available");
/*  723: 849 */         USE_MPSC_CHUNKED_ARRAY_QUEUE = true;
/*  724:     */       }
/*  725:     */     }
/*  726:     */     
/*  727:     */     static <T> Queue<T> newMpscQueue(int maxCapacity)
/*  728:     */     {
/*  729: 857 */       int capacity = Math.max(Math.min(maxCapacity, 1073741824), 2048);
/*  730: 858 */       return USE_MPSC_CHUNKED_ARRAY_QUEUE ? new MpscChunkedArrayQueue(1024, capacity) : new MpscGrowableAtomicArrayQueue(1024, capacity);
/*  731:     */     }
/*  732:     */     
/*  733:     */     static <T> Queue<T> newMpscQueue()
/*  734:     */     {
/*  735: 863 */       return USE_MPSC_CHUNKED_ARRAY_QUEUE ? new MpscUnboundedArrayQueue(1024) : new MpscUnboundedAtomicArrayQueue(1024);
/*  736:     */     }
/*  737:     */   }
/*  738:     */   
/*  739:     */   public static <T> Queue<T> newMpscQueue()
/*  740:     */   {
/*  741: 874 */     return Mpsc.newMpscQueue();
/*  742:     */   }
/*  743:     */   
/*  744:     */   public static <T> Queue<T> newMpscQueue(int maxCapacity)
/*  745:     */   {
/*  746: 882 */     return Mpsc.newMpscQueue(maxCapacity);
/*  747:     */   }
/*  748:     */   
/*  749:     */   public static <T> Queue<T> newSpscQueue()
/*  750:     */   {
/*  751: 890 */     return hasUnsafe() ? new SpscLinkedQueue() : new SpscLinkedAtomicQueue();
/*  752:     */   }
/*  753:     */   
/*  754:     */   public static <T> Queue<T> newFixedMpscQueue(int capacity)
/*  755:     */   {
/*  756: 898 */     return hasUnsafe() ? new MpscArrayQueue(capacity) : new MpscAtomicArrayQueue(capacity);
/*  757:     */   }
/*  758:     */   
/*  759:     */   public static ClassLoader getClassLoader(Class<?> clazz)
/*  760:     */   {
/*  761: 905 */     return PlatformDependent0.getClassLoader(clazz);
/*  762:     */   }
/*  763:     */   
/*  764:     */   public static ClassLoader getContextClassLoader()
/*  765:     */   {
/*  766: 912 */     return PlatformDependent0.getContextClassLoader();
/*  767:     */   }
/*  768:     */   
/*  769:     */   public static ClassLoader getSystemClassLoader()
/*  770:     */   {
/*  771: 919 */     return PlatformDependent0.getSystemClassLoader();
/*  772:     */   }
/*  773:     */   
/*  774:     */   public static <C> Deque<C> newConcurrentDeque()
/*  775:     */   {
/*  776: 926 */     if (javaVersion() < 7) {
/*  777: 927 */       return new LinkedBlockingDeque();
/*  778:     */     }
/*  779: 929 */     return new ConcurrentLinkedDeque();
/*  780:     */   }
/*  781:     */   
/*  782:     */   public static Random threadLocalRandom()
/*  783:     */   {
/*  784: 937 */     return RANDOM_PROVIDER.current();
/*  785:     */   }
/*  786:     */   
/*  787:     */   private static boolean isWindows0()
/*  788:     */   {
/*  789: 941 */     boolean windows = SystemPropertyUtil.get("os.name", "").toLowerCase(Locale.US).contains("win");
/*  790: 942 */     if (windows) {
/*  791: 943 */       logger.debug("Platform: Windows");
/*  792:     */     }
/*  793: 945 */     return windows;
/*  794:     */   }
/*  795:     */   
/*  796:     */   private static boolean isOsx0()
/*  797:     */   {
/*  798: 950 */     String osname = SystemPropertyUtil.get("os.name", "").toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
/*  799: 951 */     boolean osx = (osname.startsWith("macosx")) || (osname.startsWith("osx"));
/*  800: 953 */     if (osx) {
/*  801: 954 */       logger.debug("Platform: MacOS");
/*  802:     */     }
/*  803: 956 */     return osx;
/*  804:     */   }
/*  805:     */   
/*  806:     */   private static boolean maybeSuperUser0()
/*  807:     */   {
/*  808: 960 */     String username = SystemPropertyUtil.get("user.name");
/*  809: 961 */     if (isWindows()) {
/*  810: 962 */       return "Administrator".equals(username);
/*  811:     */     }
/*  812: 965 */     return ("root".equals(username)) || ("toor".equals(username));
/*  813:     */   }
/*  814:     */   
/*  815:     */   private static boolean hasUnsafe0()
/*  816:     */   {
/*  817: 969 */     if (isAndroid())
/*  818:     */     {
/*  819: 970 */       logger.debug("sun.misc.Unsafe: unavailable (Android)");
/*  820: 971 */       return false;
/*  821:     */     }
/*  822: 974 */     if (PlatformDependent0.isExplicitNoUnsafe()) {
/*  823: 975 */       return false;
/*  824:     */     }
/*  825:     */     try
/*  826:     */     {
/*  827: 979 */       boolean hasUnsafe = PlatformDependent0.hasUnsafe();
/*  828: 980 */       logger.debug("sun.misc.Unsafe: {}", hasUnsafe ? "available" : "unavailable");
/*  829: 981 */       return hasUnsafe;
/*  830:     */     }
/*  831:     */     catch (Throwable t)
/*  832:     */     {
/*  833: 983 */       logger.trace("Could not determine if Unsafe is available", t);
/*  834:     */     }
/*  835: 985 */     return false;
/*  836:     */   }
/*  837:     */   
/*  838:     */   private static long maxDirectMemory0()
/*  839:     */   {
/*  840: 990 */     long maxDirectMemory = 0L;
/*  841: 991 */     ClassLoader systemClassLoader = null;
/*  842:     */     try
/*  843:     */     {
/*  844: 994 */       systemClassLoader = getSystemClassLoader();
/*  845: 995 */       Class<?> vmClass = Class.forName("sun.misc.VM", true, systemClassLoader);
/*  846: 996 */       Method m = vmClass.getDeclaredMethod("maxDirectMemory", new Class[0]);
/*  847: 997 */       maxDirectMemory = ((Number)m.invoke(null, new Object[0])).longValue();
/*  848:     */     }
/*  849:     */     catch (Throwable localThrowable) {}
/*  850:1002 */     if (maxDirectMemory > 0L) {
/*  851:1003 */       return maxDirectMemory;
/*  852:     */     }
/*  853:     */     try
/*  854:     */     {
/*  855:1009 */       Class<?> mgmtFactoryClass = Class.forName("java.lang.management.ManagementFactory", true, systemClassLoader);
/*  856:     */       
/*  857:1011 */       Class<?> runtimeClass = Class.forName("java.lang.management.RuntimeMXBean", true, systemClassLoader);
/*  858:     */       
/*  859:     */ 
/*  860:1014 */       Object runtime = mgmtFactoryClass.getDeclaredMethod("getRuntimeMXBean", new Class[0]).invoke(null, new Object[0]);
/*  861:     */       
/*  862:     */ 
/*  863:1017 */       List<String> vmArgs = (List)runtimeClass.getDeclaredMethod("getInputArguments", new Class[0]).invoke(runtime, new Object[0]);
/*  864:1018 */       for (int i = vmArgs.size() - 1; i >= 0; i--)
/*  865:     */       {
/*  866:1019 */         Matcher m = MAX_DIRECT_MEMORY_SIZE_ARG_PATTERN.matcher((CharSequence)vmArgs.get(i));
/*  867:1020 */         if (m.matches())
/*  868:     */         {
/*  869:1024 */           maxDirectMemory = Long.parseLong(m.group(1));
/*  870:1025 */           switch (m.group(2).charAt(0))
/*  871:     */           {
/*  872:     */           case 'K': 
/*  873:     */           case 'k': 
/*  874:1027 */             maxDirectMemory *= 1024L;
/*  875:1028 */             break;
/*  876:     */           case 'M': 
/*  877:     */           case 'm': 
/*  878:1030 */             maxDirectMemory *= 1048576L;
/*  879:1031 */             break;
/*  880:     */           case 'G': 
/*  881:     */           case 'g': 
/*  882:1033 */             maxDirectMemory *= 1073741824L;
/*  883:     */           }
/*  884:1036 */           break;
/*  885:     */         }
/*  886:     */       }
/*  887:     */     }
/*  888:     */     catch (Throwable localThrowable1) {}
/*  889:1042 */     if (maxDirectMemory <= 0L)
/*  890:     */     {
/*  891:1043 */       maxDirectMemory = Runtime.getRuntime().maxMemory();
/*  892:1044 */       logger.debug("maxDirectMemory: {} bytes (maybe)", Long.valueOf(maxDirectMemory));
/*  893:     */     }
/*  894:     */     else
/*  895:     */     {
/*  896:1046 */       logger.debug("maxDirectMemory: {} bytes", Long.valueOf(maxDirectMemory));
/*  897:     */     }
/*  898:1049 */     return maxDirectMemory;
/*  899:     */   }
/*  900:     */   
/*  901:     */   private static File tmpdir0()
/*  902:     */   {
/*  903:     */     try
/*  904:     */     {
/*  905:1055 */       File f = toDirectory(SystemPropertyUtil.get("io.netty.tmpdir"));
/*  906:1056 */       if (f != null)
/*  907:     */       {
/*  908:1057 */         logger.debug("-Dio.netty.tmpdir: {}", f);
/*  909:1058 */         return f;
/*  910:     */       }
/*  911:1061 */       f = toDirectory(SystemPropertyUtil.get("java.io.tmpdir"));
/*  912:1062 */       if (f != null)
/*  913:     */       {
/*  914:1063 */         logger.debug("-Dio.netty.tmpdir: {} (java.io.tmpdir)", f);
/*  915:1064 */         return f;
/*  916:     */       }
/*  917:1068 */       if (isWindows())
/*  918:     */       {
/*  919:1069 */         f = toDirectory(System.getenv("TEMP"));
/*  920:1070 */         if (f != null)
/*  921:     */         {
/*  922:1071 */           logger.debug("-Dio.netty.tmpdir: {} (%TEMP%)", f);
/*  923:1072 */           return f;
/*  924:     */         }
/*  925:1075 */         String userprofile = System.getenv("USERPROFILE");
/*  926:1076 */         if (userprofile != null)
/*  927:     */         {
/*  928:1077 */           f = toDirectory(userprofile + "\\AppData\\Local\\Temp");
/*  929:1078 */           if (f != null)
/*  930:     */           {
/*  931:1079 */             logger.debug("-Dio.netty.tmpdir: {} (%USERPROFILE%\\AppData\\Local\\Temp)", f);
/*  932:1080 */             return f;
/*  933:     */           }
/*  934:1083 */           f = toDirectory(userprofile + "\\Local Settings\\Temp");
/*  935:1084 */           if (f != null)
/*  936:     */           {
/*  937:1085 */             logger.debug("-Dio.netty.tmpdir: {} (%USERPROFILE%\\Local Settings\\Temp)", f);
/*  938:1086 */             return f;
/*  939:     */           }
/*  940:     */         }
/*  941:     */       }
/*  942:     */       else
/*  943:     */       {
/*  944:1090 */         f = toDirectory(System.getenv("TMPDIR"));
/*  945:1091 */         if (f != null)
/*  946:     */         {
/*  947:1092 */           logger.debug("-Dio.netty.tmpdir: {} ($TMPDIR)", f);
/*  948:1093 */           return f;
/*  949:     */         }
/*  950:     */       }
/*  951:     */     }
/*  952:     */     catch (Throwable localThrowable) {}
/*  953:     */     File f;
/*  954:     */     File f;
/*  955:1101 */     if (isWindows()) {
/*  956:1102 */       f = new File("C:\\Windows\\Temp");
/*  957:     */     } else {
/*  958:1104 */       f = new File("/tmp");
/*  959:     */     }
/*  960:1107 */     logger.warn("Failed to get the temporary directory; falling back to: {}", f);
/*  961:1108 */     return f;
/*  962:     */   }
/*  963:     */   
/*  964:     */   private static File toDirectory(String path)
/*  965:     */   {
/*  966:1113 */     if (path == null) {
/*  967:1114 */       return null;
/*  968:     */     }
/*  969:1117 */     File f = new File(path);
/*  970:1118 */     f.mkdirs();
/*  971:1120 */     if (!f.isDirectory()) {
/*  972:1121 */       return null;
/*  973:     */     }
/*  974:     */     try
/*  975:     */     {
/*  976:1125 */       return f.getAbsoluteFile();
/*  977:     */     }
/*  978:     */     catch (Exception ignored) {}
/*  979:1127 */     return f;
/*  980:     */   }
/*  981:     */   
/*  982:     */   private static int bitMode0()
/*  983:     */   {
/*  984:1133 */     int bitMode = SystemPropertyUtil.getInt("io.netty.bitMode", 0);
/*  985:1134 */     if (bitMode > 0)
/*  986:     */     {
/*  987:1135 */       logger.debug("-Dio.netty.bitMode: {}", Integer.valueOf(bitMode));
/*  988:1136 */       return bitMode;
/*  989:     */     }
/*  990:1140 */     bitMode = SystemPropertyUtil.getInt("sun.arch.data.model", 0);
/*  991:1141 */     if (bitMode > 0)
/*  992:     */     {
/*  993:1142 */       logger.debug("-Dio.netty.bitMode: {} (sun.arch.data.model)", Integer.valueOf(bitMode));
/*  994:1143 */       return bitMode;
/*  995:     */     }
/*  996:1145 */     bitMode = SystemPropertyUtil.getInt("com.ibm.vm.bitmode", 0);
/*  997:1146 */     if (bitMode > 0)
/*  998:     */     {
/*  999:1147 */       logger.debug("-Dio.netty.bitMode: {} (com.ibm.vm.bitmode)", Integer.valueOf(bitMode));
/* 1000:1148 */       return bitMode;
/* 1001:     */     }
/* 1002:1152 */     String arch = SystemPropertyUtil.get("os.arch", "").toLowerCase(Locale.US).trim();
/* 1003:1153 */     if (("amd64".equals(arch)) || ("x86_64".equals(arch))) {
/* 1004:1154 */       bitMode = 64;
/* 1005:1155 */     } else if (("i386".equals(arch)) || ("i486".equals(arch)) || ("i586".equals(arch)) || ("i686".equals(arch))) {
/* 1006:1156 */       bitMode = 32;
/* 1007:     */     }
/* 1008:1159 */     if (bitMode > 0) {
/* 1009:1160 */       logger.debug("-Dio.netty.bitMode: {} (os.arch: {})", Integer.valueOf(bitMode), arch);
/* 1010:     */     }
/* 1011:1164 */     String vm = SystemPropertyUtil.get("java.vm.name", "").toLowerCase(Locale.US);
/* 1012:1165 */     Pattern BIT_PATTERN = Pattern.compile("([1-9][0-9]+)-?bit");
/* 1013:1166 */     Matcher m = BIT_PATTERN.matcher(vm);
/* 1014:1167 */     if (m.find()) {
/* 1015:1168 */       return Integer.parseInt(m.group(1));
/* 1016:     */     }
/* 1017:1170 */     return 64;
/* 1018:     */   }
/* 1019:     */   
/* 1020:     */   private static int addressSize0()
/* 1021:     */   {
/* 1022:1175 */     if (!hasUnsafe()) {
/* 1023:1176 */       return -1;
/* 1024:     */     }
/* 1025:1178 */     return PlatformDependent0.addressSize();
/* 1026:     */   }
/* 1027:     */   
/* 1028:     */   private static long byteArrayBaseOffset0()
/* 1029:     */   {
/* 1030:1182 */     if (!hasUnsafe()) {
/* 1031:1183 */       return -1L;
/* 1032:     */     }
/* 1033:1185 */     return PlatformDependent0.byteArrayBaseOffset();
/* 1034:     */   }
/* 1035:     */   
/* 1036:     */   private static boolean equalsSafe(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length)
/* 1037:     */   {
/* 1038:1189 */     int end = startPos1 + length;
/* 1039:1190 */     for (; startPos1 < end; startPos2++)
/* 1040:     */     {
/* 1041:1191 */       if (bytes1[startPos1] != bytes2[startPos2]) {
/* 1042:1192 */         return false;
/* 1043:     */       }
/* 1044:1190 */       startPos1++;
/* 1045:     */     }
/* 1046:1195 */     return true;
/* 1047:     */   }
/* 1048:     */   
/* 1049:     */   private static boolean isZeroSafe(byte[] bytes, int startPos, int length)
/* 1050:     */   {
/* 1051:1199 */     int end = startPos + length;
/* 1052:1200 */     for (; startPos < end; startPos++) {
/* 1053:1201 */       if (bytes[startPos] != 0) {
/* 1054:1202 */         return false;
/* 1055:     */       }
/* 1056:     */     }
/* 1057:1205 */     return true;
/* 1058:     */   }
/* 1059:     */   
/* 1060:     */   static int hashCodeAsciiSafe(byte[] bytes, int startPos, int length)
/* 1061:     */   {
/* 1062:1212 */     int hash = -1028477387;
/* 1063:1213 */     int remainingBytes = length & 0x7;
/* 1064:1214 */     int end = startPos + remainingBytes;
/* 1065:1215 */     for (int i = startPos - 8 + length; i >= end; i -= 8) {
/* 1066:1216 */       hash = PlatformDependent0.hashCodeAsciiCompute(getLongSafe(bytes, i), hash);
/* 1067:     */     }
/* 1068:1218 */     switch (remainingBytes)
/* 1069:     */     {
/* 1070:     */     case 7: 
/* 1071:1220 */       return 
/* 1072:     */       
/* 1073:1222 */         ((hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(bytes[startPos])) * 461845907 + PlatformDependent0.hashCodeAsciiSanitize(getShortSafe(bytes, startPos + 1))) * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(getIntSafe(bytes, startPos + 3));
/* 1074:     */     case 6: 
/* 1075:1224 */       return 
/* 1076:1225 */         (hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(getShortSafe(bytes, startPos))) * 461845907 + PlatformDependent0.hashCodeAsciiSanitize(getIntSafe(bytes, startPos + 2));
/* 1077:     */     case 5: 
/* 1078:1227 */       return 
/* 1079:1228 */         (hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(bytes[startPos])) * 461845907 + PlatformDependent0.hashCodeAsciiSanitize(getIntSafe(bytes, startPos + 1));
/* 1080:     */     case 4: 
/* 1081:1230 */       return hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(getIntSafe(bytes, startPos));
/* 1082:     */     case 3: 
/* 1083:1232 */       return 
/* 1084:1233 */         (hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(bytes[startPos])) * 461845907 + PlatformDependent0.hashCodeAsciiSanitize(getShortSafe(bytes, startPos + 1));
/* 1085:     */     case 2: 
/* 1086:1235 */       return hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(getShortSafe(bytes, startPos));
/* 1087:     */     case 1: 
/* 1088:1237 */       return hash * -862048943 + PlatformDependent0.hashCodeAsciiSanitize(bytes[startPos]);
/* 1089:     */     }
/* 1090:1239 */     return hash;
/* 1091:     */   }
/* 1092:     */   
/* 1093:     */   public static String normalizedArch()
/* 1094:     */   {
/* 1095:1244 */     return NORMALIZED_ARCH;
/* 1096:     */   }
/* 1097:     */   
/* 1098:     */   public static String normalizedOs()
/* 1099:     */   {
/* 1100:1248 */     return NORMALIZED_OS;
/* 1101:     */   }
/* 1102:     */   
/* 1103:     */   private static String normalize(String value)
/* 1104:     */   {
/* 1105:1252 */     return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
/* 1106:     */   }
/* 1107:     */   
/* 1108:     */   private static String normalizeArch(String value)
/* 1109:     */   {
/* 1110:1256 */     value = normalize(value);
/* 1111:1257 */     if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
/* 1112:1258 */       return "x86_64";
/* 1113:     */     }
/* 1114:1260 */     if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
/* 1115:1261 */       return "x86_32";
/* 1116:     */     }
/* 1117:1263 */     if (value.matches("^(ia64|itanium64)$")) {
/* 1118:1264 */       return "itanium_64";
/* 1119:     */     }
/* 1120:1266 */     if (value.matches("^(sparc|sparc32)$")) {
/* 1121:1267 */       return "sparc_32";
/* 1122:     */     }
/* 1123:1269 */     if (value.matches("^(sparcv9|sparc64)$")) {
/* 1124:1270 */       return "sparc_64";
/* 1125:     */     }
/* 1126:1272 */     if (value.matches("^(arm|arm32)$")) {
/* 1127:1273 */       return "arm_32";
/* 1128:     */     }
/* 1129:1275 */     if ("aarch64".equals(value)) {
/* 1130:1276 */       return "aarch_64";
/* 1131:     */     }
/* 1132:1278 */     if (value.matches("^(ppc|ppc32)$")) {
/* 1133:1279 */       return "ppc_32";
/* 1134:     */     }
/* 1135:1281 */     if ("ppc64".equals(value)) {
/* 1136:1282 */       return "ppc_64";
/* 1137:     */     }
/* 1138:1284 */     if ("ppc64le".equals(value)) {
/* 1139:1285 */       return "ppcle_64";
/* 1140:     */     }
/* 1141:1287 */     if ("s390".equals(value)) {
/* 1142:1288 */       return "s390_32";
/* 1143:     */     }
/* 1144:1290 */     if ("s390x".equals(value)) {
/* 1145:1291 */       return "s390_64";
/* 1146:     */     }
/* 1147:1294 */     return "unknown";
/* 1148:     */   }
/* 1149:     */   
/* 1150:     */   private static String normalizeOs(String value)
/* 1151:     */   {
/* 1152:1298 */     value = normalize(value);
/* 1153:1299 */     if (value.startsWith("aix")) {
/* 1154:1300 */       return "aix";
/* 1155:     */     }
/* 1156:1302 */     if (value.startsWith("hpux")) {
/* 1157:1303 */       return "hpux";
/* 1158:     */     }
/* 1159:1305 */     if (value.startsWith("os400")) {
/* 1160:1307 */       if ((value.length() <= 5) || (!Character.isDigit(value.charAt(5)))) {
/* 1161:1308 */         return "os400";
/* 1162:     */       }
/* 1163:     */     }
/* 1164:1311 */     if (value.startsWith("linux")) {
/* 1165:1312 */       return "linux";
/* 1166:     */     }
/* 1167:1314 */     if ((value.startsWith("macosx")) || (value.startsWith("osx"))) {
/* 1168:1315 */       return "osx";
/* 1169:     */     }
/* 1170:1317 */     if (value.startsWith("freebsd")) {
/* 1171:1318 */       return "freebsd";
/* 1172:     */     }
/* 1173:1320 */     if (value.startsWith("openbsd")) {
/* 1174:1321 */       return "openbsd";
/* 1175:     */     }
/* 1176:1323 */     if (value.startsWith("netbsd")) {
/* 1177:1324 */       return "netbsd";
/* 1178:     */     }
/* 1179:1326 */     if ((value.startsWith("solaris")) || (value.startsWith("sunos"))) {
/* 1180:1327 */       return "sunos";
/* 1181:     */     }
/* 1182:1329 */     if (value.startsWith("windows")) {
/* 1183:1330 */       return "windows";
/* 1184:     */     }
/* 1185:1333 */     return "unknown";
/* 1186:     */   }
/* 1187:     */   
/* 1188:     */   private static final class AtomicLongCounter
/* 1189:     */     extends AtomicLong
/* 1190:     */     implements LongCounter
/* 1191:     */   {
/* 1192:     */     private static final long serialVersionUID = 4074772784610639305L;
/* 1193:     */     
/* 1194:     */     public void add(long delta)
/* 1195:     */     {
/* 1196:1341 */       addAndGet(delta);
/* 1197:     */     }
/* 1198:     */     
/* 1199:     */     public void increment()
/* 1200:     */     {
/* 1201:1346 */       incrementAndGet();
/* 1202:     */     }
/* 1203:     */     
/* 1204:     */     public void decrement()
/* 1205:     */     {
/* 1206:1351 */       decrementAndGet();
/* 1207:     */     }
/* 1208:     */     
/* 1209:     */     public long value()
/* 1210:     */     {
/* 1211:1356 */       return get();
/* 1212:     */     }
/* 1213:     */   }
/* 1214:     */   
/* 1215:     */   private static abstract interface ThreadLocalRandomProvider
/* 1216:     */   {
/* 1217:     */     public abstract Random current();
/* 1218:     */   }
/* 1219:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.PlatformDependent
 * JD-Core Version:    0.7.0.1
 */