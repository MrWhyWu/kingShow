/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.NettyRuntime;
/*   4:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   5:    */ import io.netty.util.concurrent.FastThreadLocalThread;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ import java.nio.ByteBuffer;
/*  12:    */ import java.util.ArrayList;
/*  13:    */ import java.util.Collections;
/*  14:    */ import java.util.List;
/*  15:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  16:    */ 
/*  17:    */ public class PooledByteBufAllocator
/*  18:    */   extends AbstractByteBufAllocator
/*  19:    */   implements ByteBufAllocatorMetricProvider
/*  20:    */ {
/*  21:    */   private static final InternalLogger logger;
/*  22:    */   private static final int DEFAULT_NUM_HEAP_ARENA;
/*  23:    */   private static final int DEFAULT_NUM_DIRECT_ARENA;
/*  24:    */   private static final int DEFAULT_PAGE_SIZE;
/*  25:    */   private static final int DEFAULT_MAX_ORDER;
/*  26:    */   private static final int DEFAULT_TINY_CACHE_SIZE;
/*  27:    */   private static final int DEFAULT_SMALL_CACHE_SIZE;
/*  28:    */   private static final int DEFAULT_NORMAL_CACHE_SIZE;
/*  29:    */   private static final int DEFAULT_MAX_CACHED_BUFFER_CAPACITY;
/*  30:    */   private static final int DEFAULT_CACHE_TRIM_INTERVAL;
/*  31:    */   private static final boolean DEFAULT_USE_CACHE_FOR_ALL_THREADS;
/*  32:    */   private static final int DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT;
/*  33:    */   private static final int MIN_PAGE_SIZE = 4096;
/*  34:    */   private static final int MAX_CHUNK_SIZE = 1073741824;
/*  35:    */   
/*  36:    */   static
/*  37:    */   {
/*  38: 35 */     logger = InternalLoggerFactory.getInstance(PooledByteBufAllocator.class);
/*  39:    */     
/*  40:    */ 
/*  41:    */ 
/*  42:    */ 
/*  43:    */ 
/*  44:    */ 
/*  45:    */ 
/*  46:    */ 
/*  47:    */ 
/*  48:    */ 
/*  49:    */ 
/*  50:    */ 
/*  51:    */ 
/*  52:    */ 
/*  53:    */ 
/*  54:    */ 
/*  55:    */ 
/*  56: 53 */     int defaultPageSize = SystemPropertyUtil.getInt("io.netty.allocator.pageSize", 8192);
/*  57: 54 */     Throwable pageSizeFallbackCause = null;
/*  58:    */     try
/*  59:    */     {
/*  60: 56 */       validateAndCalculatePageShifts(defaultPageSize);
/*  61:    */     }
/*  62:    */     catch (Throwable t)
/*  63:    */     {
/*  64: 58 */       pageSizeFallbackCause = t;
/*  65: 59 */       defaultPageSize = 8192;
/*  66:    */     }
/*  67: 61 */     DEFAULT_PAGE_SIZE = defaultPageSize;
/*  68:    */     
/*  69: 63 */     int defaultMaxOrder = SystemPropertyUtil.getInt("io.netty.allocator.maxOrder", 11);
/*  70: 64 */     Throwable maxOrderFallbackCause = null;
/*  71:    */     try
/*  72:    */     {
/*  73: 66 */       validateAndCalculateChunkSize(DEFAULT_PAGE_SIZE, defaultMaxOrder);
/*  74:    */     }
/*  75:    */     catch (Throwable t)
/*  76:    */     {
/*  77: 68 */       maxOrderFallbackCause = t;
/*  78: 69 */       defaultMaxOrder = 11;
/*  79:    */     }
/*  80: 71 */     DEFAULT_MAX_ORDER = defaultMaxOrder;
/*  81:    */     
/*  82:    */ 
/*  83:    */ 
/*  84: 75 */     Runtime runtime = Runtime.getRuntime();
/*  85:    */     
/*  86:    */ 
/*  87:    */ 
/*  88:    */ 
/*  89:    */ 
/*  90:    */ 
/*  91:    */ 
/*  92:    */ 
/*  93: 84 */     int defaultMinNumArena = NettyRuntime.availableProcessors() * 2;
/*  94: 85 */     int defaultChunkSize = DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER;
/*  95: 86 */     DEFAULT_NUM_HEAP_ARENA = Math.max(0, 
/*  96: 87 */       SystemPropertyUtil.getInt("io.netty.allocator.numHeapArenas", 
/*  97:    */       
/*  98: 89 */       (int)Math.min(defaultMinNumArena, runtime
/*  99:    */       
/* 100: 91 */       .maxMemory() / defaultChunkSize / 2L / 3L)));
/* 101: 92 */     DEFAULT_NUM_DIRECT_ARENA = Math.max(0, 
/* 102: 93 */       SystemPropertyUtil.getInt("io.netty.allocator.numDirectArenas", 
/* 103:    */       
/* 104: 95 */       (int)Math.min(defaultMinNumArena, 
/* 105:    */       
/* 106: 97 */       PlatformDependent.maxDirectMemory() / defaultChunkSize / 2L / 3L)));
/* 107:    */     
/* 108:    */ 
/* 109:100 */     DEFAULT_TINY_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.tinyCacheSize", 512);
/* 110:101 */     DEFAULT_SMALL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.smallCacheSize", 256);
/* 111:102 */     DEFAULT_NORMAL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.normalCacheSize", 64);
/* 112:    */     
/* 113:    */ 
/* 114:    */ 
/* 115:106 */     DEFAULT_MAX_CACHED_BUFFER_CAPACITY = SystemPropertyUtil.getInt("io.netty.allocator.maxCachedBufferCapacity", 32768);
/* 116:    */     
/* 117:    */ 
/* 118:    */ 
/* 119:110 */     DEFAULT_CACHE_TRIM_INTERVAL = SystemPropertyUtil.getInt("io.netty.allocator.cacheTrimInterval", 8192);
/* 120:    */     
/* 121:    */ 
/* 122:113 */     DEFAULT_USE_CACHE_FOR_ALL_THREADS = SystemPropertyUtil.getBoolean("io.netty.allocator.useCacheForAllThreads", true);
/* 123:    */     
/* 124:    */ 
/* 125:116 */     DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT = SystemPropertyUtil.getInt("io.netty.allocator.directMemoryCacheAlignment", 0);
/* 126:119 */     if (logger.isDebugEnabled())
/* 127:    */     {
/* 128:120 */       logger.debug("-Dio.netty.allocator.numHeapArenas: {}", Integer.valueOf(DEFAULT_NUM_HEAP_ARENA));
/* 129:121 */       logger.debug("-Dio.netty.allocator.numDirectArenas: {}", Integer.valueOf(DEFAULT_NUM_DIRECT_ARENA));
/* 130:122 */       if (pageSizeFallbackCause == null) {
/* 131:123 */         logger.debug("-Dio.netty.allocator.pageSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE));
/* 132:    */       } else {
/* 133:125 */         logger.debug("-Dio.netty.allocator.pageSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE), pageSizeFallbackCause);
/* 134:    */       }
/* 135:127 */       if (maxOrderFallbackCause == null) {
/* 136:128 */         logger.debug("-Dio.netty.allocator.maxOrder: {}", Integer.valueOf(DEFAULT_MAX_ORDER));
/* 137:    */       } else {
/* 138:130 */         logger.debug("-Dio.netty.allocator.maxOrder: {}", Integer.valueOf(DEFAULT_MAX_ORDER), maxOrderFallbackCause);
/* 139:    */       }
/* 140:132 */       logger.debug("-Dio.netty.allocator.chunkSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER));
/* 141:133 */       logger.debug("-Dio.netty.allocator.tinyCacheSize: {}", Integer.valueOf(DEFAULT_TINY_CACHE_SIZE));
/* 142:134 */       logger.debug("-Dio.netty.allocator.smallCacheSize: {}", Integer.valueOf(DEFAULT_SMALL_CACHE_SIZE));
/* 143:135 */       logger.debug("-Dio.netty.allocator.normalCacheSize: {}", Integer.valueOf(DEFAULT_NORMAL_CACHE_SIZE));
/* 144:136 */       logger.debug("-Dio.netty.allocator.maxCachedBufferCapacity: {}", Integer.valueOf(DEFAULT_MAX_CACHED_BUFFER_CAPACITY));
/* 145:137 */       logger.debug("-Dio.netty.allocator.cacheTrimInterval: {}", Integer.valueOf(DEFAULT_CACHE_TRIM_INTERVAL));
/* 146:138 */       logger.debug("-Dio.netty.allocator.useCacheForAllThreads: {}", Boolean.valueOf(DEFAULT_USE_CACHE_FOR_ALL_THREADS));
/* 147:    */     }
/* 148:    */   }
/* 149:    */   
/* 150:142 */   public static final PooledByteBufAllocator DEFAULT = new PooledByteBufAllocator(
/* 151:143 */     PlatformDependent.directBufferPreferred());
/* 152:    */   private final PoolArena<byte[]>[] heapArenas;
/* 153:    */   private final PoolArena<ByteBuffer>[] directArenas;
/* 154:    */   private final int tinyCacheSize;
/* 155:    */   private final int smallCacheSize;
/* 156:    */   private final int normalCacheSize;
/* 157:    */   private final List<PoolArenaMetric> heapArenaMetrics;
/* 158:    */   private final List<PoolArenaMetric> directArenaMetrics;
/* 159:    */   private final PoolThreadLocalCache threadCache;
/* 160:    */   private final int chunkSize;
/* 161:    */   private final PooledByteBufAllocatorMetric metric;
/* 162:    */   
/* 163:    */   public PooledByteBufAllocator()
/* 164:    */   {
/* 165:157 */     this(false);
/* 166:    */   }
/* 167:    */   
/* 168:    */   public PooledByteBufAllocator(boolean preferDirect)
/* 169:    */   {
/* 170:162 */     this(preferDirect, DEFAULT_NUM_HEAP_ARENA, DEFAULT_NUM_DIRECT_ARENA, DEFAULT_PAGE_SIZE, DEFAULT_MAX_ORDER);
/* 171:    */   }
/* 172:    */   
/* 173:    */   public PooledByteBufAllocator(int nHeapArena, int nDirectArena, int pageSize, int maxOrder)
/* 174:    */   {
/* 175:167 */     this(false, nHeapArena, nDirectArena, pageSize, maxOrder);
/* 176:    */   }
/* 177:    */   
/* 178:    */   @Deprecated
/* 179:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder)
/* 180:    */   {
/* 181:176 */     this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, DEFAULT_TINY_CACHE_SIZE, DEFAULT_SMALL_CACHE_SIZE, DEFAULT_NORMAL_CACHE_SIZE);
/* 182:    */   }
/* 183:    */   
/* 184:    */   @Deprecated
/* 185:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize)
/* 186:    */   {
/* 187:187 */     this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, tinyCacheSize, smallCacheSize, normalCacheSize, DEFAULT_USE_CACHE_FOR_ALL_THREADS, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
/* 188:    */   }
/* 189:    */   
/* 190:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize, boolean useCacheForAllThreads)
/* 191:    */   {
/* 192:195 */     this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, tinyCacheSize, smallCacheSize, normalCacheSize, useCacheForAllThreads, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
/* 193:    */   }
/* 194:    */   
/* 195:    */   public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize, boolean useCacheForAllThreads, int directMemoryCacheAlignment)
/* 196:    */   {
/* 197:203 */     super(preferDirect);
/* 198:204 */     this.threadCache = new PoolThreadLocalCache(useCacheForAllThreads);
/* 199:205 */     this.tinyCacheSize = tinyCacheSize;
/* 200:206 */     this.smallCacheSize = smallCacheSize;
/* 201:207 */     this.normalCacheSize = normalCacheSize;
/* 202:208 */     this.chunkSize = validateAndCalculateChunkSize(pageSize, maxOrder);
/* 203:210 */     if (nHeapArena < 0) {
/* 204:211 */       throw new IllegalArgumentException("nHeapArena: " + nHeapArena + " (expected: >= 0)");
/* 205:    */     }
/* 206:213 */     if (nDirectArena < 0) {
/* 207:214 */       throw new IllegalArgumentException("nDirectArea: " + nDirectArena + " (expected: >= 0)");
/* 208:    */     }
/* 209:217 */     if (directMemoryCacheAlignment < 0) {
/* 210:218 */       throw new IllegalArgumentException("directMemoryCacheAlignment: " + directMemoryCacheAlignment + " (expected: >= 0)");
/* 211:    */     }
/* 212:221 */     if ((directMemoryCacheAlignment > 0) && (!isDirectMemoryCacheAlignmentSupported())) {
/* 213:222 */       throw new IllegalArgumentException("directMemoryCacheAlignment is not supported");
/* 214:    */     }
/* 215:225 */     if ((directMemoryCacheAlignment & -directMemoryCacheAlignment) != directMemoryCacheAlignment) {
/* 216:226 */       throw new IllegalArgumentException("directMemoryCacheAlignment: " + directMemoryCacheAlignment + " (expected: power of two)");
/* 217:    */     }
/* 218:230 */     int pageShifts = validateAndCalculatePageShifts(pageSize);
/* 219:232 */     if (nHeapArena > 0)
/* 220:    */     {
/* 221:233 */       this.heapArenas = newArenaArray(nHeapArena);
/* 222:234 */       List<PoolArenaMetric> metrics = new ArrayList(this.heapArenas.length);
/* 223:235 */       for (int i = 0; i < this.heapArenas.length; i++)
/* 224:    */       {
/* 225:236 */         PoolArena.HeapArena arena = new PoolArena.HeapArena(this, pageSize, maxOrder, pageShifts, this.chunkSize, directMemoryCacheAlignment);
/* 226:    */         
/* 227:    */ 
/* 228:239 */         this.heapArenas[i] = arena;
/* 229:240 */         metrics.add(arena);
/* 230:    */       }
/* 231:242 */       this.heapArenaMetrics = Collections.unmodifiableList(metrics);
/* 232:    */     }
/* 233:    */     else
/* 234:    */     {
/* 235:244 */       this.heapArenas = null;
/* 236:245 */       this.heapArenaMetrics = Collections.emptyList();
/* 237:    */     }
/* 238:248 */     if (nDirectArena > 0)
/* 239:    */     {
/* 240:249 */       this.directArenas = newArenaArray(nDirectArena);
/* 241:250 */       List<PoolArenaMetric> metrics = new ArrayList(this.directArenas.length);
/* 242:251 */       for (int i = 0; i < this.directArenas.length; i++)
/* 243:    */       {
/* 244:252 */         PoolArena.DirectArena arena = new PoolArena.DirectArena(this, pageSize, maxOrder, pageShifts, this.chunkSize, directMemoryCacheAlignment);
/* 245:    */         
/* 246:254 */         this.directArenas[i] = arena;
/* 247:255 */         metrics.add(arena);
/* 248:    */       }
/* 249:257 */       this.directArenaMetrics = Collections.unmodifiableList(metrics);
/* 250:    */     }
/* 251:    */     else
/* 252:    */     {
/* 253:259 */       this.directArenas = null;
/* 254:260 */       this.directArenaMetrics = Collections.emptyList();
/* 255:    */     }
/* 256:262 */     this.metric = new PooledByteBufAllocatorMetric(this);
/* 257:    */   }
/* 258:    */   
/* 259:    */   private static <T> PoolArena<T>[] newArenaArray(int size)
/* 260:    */   {
/* 261:267 */     return new PoolArena[size];
/* 262:    */   }
/* 263:    */   
/* 264:    */   private static int validateAndCalculatePageShifts(int pageSize)
/* 265:    */   {
/* 266:271 */     if (pageSize < 4096) {
/* 267:272 */       throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: " + 4096 + ")");
/* 268:    */     }
/* 269:275 */     if ((pageSize & pageSize - 1) != 0) {
/* 270:276 */       throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: power of 2)");
/* 271:    */     }
/* 272:280 */     return 31 - Integer.numberOfLeadingZeros(pageSize);
/* 273:    */   }
/* 274:    */   
/* 275:    */   private static int validateAndCalculateChunkSize(int pageSize, int maxOrder)
/* 276:    */   {
/* 277:284 */     if (maxOrder > 14) {
/* 278:285 */       throw new IllegalArgumentException("maxOrder: " + maxOrder + " (expected: 0-14)");
/* 279:    */     }
/* 280:289 */     int chunkSize = pageSize;
/* 281:290 */     for (int i = maxOrder; i > 0; i--)
/* 282:    */     {
/* 283:291 */       if (chunkSize > 536870912) {
/* 284:292 */         throw new IllegalArgumentException(String.format("pageSize (%d) << maxOrder (%d) must not exceed %d", new Object[] {
/* 285:293 */           Integer.valueOf(pageSize), Integer.valueOf(maxOrder), Integer.valueOf(1073741824) }));
/* 286:    */       }
/* 287:295 */       chunkSize <<= 1;
/* 288:    */     }
/* 289:297 */     return chunkSize;
/* 290:    */   }
/* 291:    */   
/* 292:    */   protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity)
/* 293:    */   {
/* 294:302 */     PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
/* 295:303 */     PoolArena<byte[]> heapArena = cache.heapArena;
/* 296:    */     ByteBuf buf;
/* 297:    */     ByteBuf buf;
/* 298:306 */     if (heapArena != null) {
/* 299:307 */       buf = heapArena.allocate(cache, initialCapacity, maxCapacity);
/* 300:    */     } else {
/* 301:309 */       buf = PlatformDependent.hasUnsafe() ? new UnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) : new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
/* 302:    */     }
/* 303:314 */     return toLeakAwareBuffer(buf);
/* 304:    */   }
/* 305:    */   
/* 306:    */   protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity)
/* 307:    */   {
/* 308:319 */     PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
/* 309:320 */     PoolArena<ByteBuffer> directArena = cache.directArena;
/* 310:    */     ByteBuf buf;
/* 311:    */     ByteBuf buf;
/* 312:323 */     if (directArena != null) {
/* 313:324 */       buf = directArena.allocate(cache, initialCapacity, maxCapacity);
/* 314:    */     } else {
/* 315:327 */       buf = PlatformDependent.hasUnsafe() ? UnsafeByteBufUtil.newUnsafeDirectByteBuf(this, initialCapacity, maxCapacity) : new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
/* 316:    */     }
/* 317:331 */     return toLeakAwareBuffer(buf);
/* 318:    */   }
/* 319:    */   
/* 320:    */   public static int defaultNumHeapArena()
/* 321:    */   {
/* 322:338 */     return DEFAULT_NUM_HEAP_ARENA;
/* 323:    */   }
/* 324:    */   
/* 325:    */   public static int defaultNumDirectArena()
/* 326:    */   {
/* 327:345 */     return DEFAULT_NUM_DIRECT_ARENA;
/* 328:    */   }
/* 329:    */   
/* 330:    */   public static int defaultPageSize()
/* 331:    */   {
/* 332:352 */     return DEFAULT_PAGE_SIZE;
/* 333:    */   }
/* 334:    */   
/* 335:    */   public static int defaultMaxOrder()
/* 336:    */   {
/* 337:359 */     return DEFAULT_MAX_ORDER;
/* 338:    */   }
/* 339:    */   
/* 340:    */   public static boolean defaultUseCacheForAllThreads()
/* 341:    */   {
/* 342:366 */     return DEFAULT_USE_CACHE_FOR_ALL_THREADS;
/* 343:    */   }
/* 344:    */   
/* 345:    */   public static boolean defaultPreferDirect()
/* 346:    */   {
/* 347:373 */     return PlatformDependent.directBufferPreferred();
/* 348:    */   }
/* 349:    */   
/* 350:    */   public static int defaultTinyCacheSize()
/* 351:    */   {
/* 352:380 */     return DEFAULT_TINY_CACHE_SIZE;
/* 353:    */   }
/* 354:    */   
/* 355:    */   public static int defaultSmallCacheSize()
/* 356:    */   {
/* 357:387 */     return DEFAULT_SMALL_CACHE_SIZE;
/* 358:    */   }
/* 359:    */   
/* 360:    */   public static int defaultNormalCacheSize()
/* 361:    */   {
/* 362:394 */     return DEFAULT_NORMAL_CACHE_SIZE;
/* 363:    */   }
/* 364:    */   
/* 365:    */   public static boolean isDirectMemoryCacheAlignmentSupported()
/* 366:    */   {
/* 367:401 */     return PlatformDependent.hasUnsafe();
/* 368:    */   }
/* 369:    */   
/* 370:    */   public boolean isDirectBufferPooled()
/* 371:    */   {
/* 372:406 */     return this.directArenas != null;
/* 373:    */   }
/* 374:    */   
/* 375:    */   @Deprecated
/* 376:    */   public boolean hasThreadLocalCache()
/* 377:    */   {
/* 378:415 */     return this.threadCache.isSet();
/* 379:    */   }
/* 380:    */   
/* 381:    */   @Deprecated
/* 382:    */   public void freeThreadLocalCache()
/* 383:    */   {
/* 384:423 */     this.threadCache.remove();
/* 385:    */   }
/* 386:    */   
/* 387:    */   final class PoolThreadLocalCache
/* 388:    */     extends FastThreadLocal<PoolThreadCache>
/* 389:    */   {
/* 390:    */     private final boolean useCacheForAllThreads;
/* 391:    */     
/* 392:    */     PoolThreadLocalCache(boolean useCacheForAllThreads)
/* 393:    */     {
/* 394:430 */       this.useCacheForAllThreads = useCacheForAllThreads;
/* 395:    */     }
/* 396:    */     
/* 397:    */     protected synchronized PoolThreadCache initialValue()
/* 398:    */     {
/* 399:435 */       PoolArena<byte[]> heapArena = leastUsedArena(PooledByteBufAllocator.this.heapArenas);
/* 400:436 */       PoolArena<ByteBuffer> directArena = leastUsedArena(PooledByteBufAllocator.this.directArenas);
/* 401:    */       
/* 402:438 */       Thread current = Thread.currentThread();
/* 403:439 */       boolean fastThread = current instanceof FastThreadLocalThread;
/* 404:440 */       if ((this.useCacheForAllThreads) || ((current instanceof FastThreadLocalThread)))
/* 405:    */       {
/* 406:444 */         boolean useTheadWatcher = !((FastThreadLocalThread)current).willCleanupFastThreadLocals();
/* 407:445 */         return new PoolThreadCache(heapArena, directArena, 
/* 408:446 */           PooledByteBufAllocator.this.tinyCacheSize, PooledByteBufAllocator.this.smallCacheSize, PooledByteBufAllocator.this.normalCacheSize, 
/* 409:447 */           PooledByteBufAllocator.DEFAULT_MAX_CACHED_BUFFER_CAPACITY, PooledByteBufAllocator.DEFAULT_CACHE_TRIM_INTERVAL, useTheadWatcher);
/* 410:    */       }
/* 411:450 */       return new PoolThreadCache(heapArena, directArena, 0, 0, 0, 0, 0, false);
/* 412:    */     }
/* 413:    */     
/* 414:    */     protected void onRemoval(PoolThreadCache threadCache)
/* 415:    */     {
/* 416:455 */       threadCache.free();
/* 417:    */     }
/* 418:    */     
/* 419:    */     private <T> PoolArena<T> leastUsedArena(PoolArena<T>[] arenas)
/* 420:    */     {
/* 421:459 */       if ((arenas == null) || (arenas.length == 0)) {
/* 422:460 */         return null;
/* 423:    */       }
/* 424:463 */       PoolArena<T> minArena = arenas[0];
/* 425:464 */       for (int i = 1; i < arenas.length; i++)
/* 426:    */       {
/* 427:465 */         PoolArena<T> arena = arenas[i];
/* 428:466 */         if (arena.numThreadCaches.get() < minArena.numThreadCaches.get()) {
/* 429:467 */           minArena = arena;
/* 430:    */         }
/* 431:    */       }
/* 432:471 */       return minArena;
/* 433:    */     }
/* 434:    */   }
/* 435:    */   
/* 436:    */   public PooledByteBufAllocatorMetric metric()
/* 437:    */   {
/* 438:477 */     return this.metric;
/* 439:    */   }
/* 440:    */   
/* 441:    */   @Deprecated
/* 442:    */   public int numHeapArenas()
/* 443:    */   {
/* 444:487 */     return this.heapArenaMetrics.size();
/* 445:    */   }
/* 446:    */   
/* 447:    */   @Deprecated
/* 448:    */   public int numDirectArenas()
/* 449:    */   {
/* 450:497 */     return this.directArenaMetrics.size();
/* 451:    */   }
/* 452:    */   
/* 453:    */   @Deprecated
/* 454:    */   public List<PoolArenaMetric> heapArenas()
/* 455:    */   {
/* 456:507 */     return this.heapArenaMetrics;
/* 457:    */   }
/* 458:    */   
/* 459:    */   @Deprecated
/* 460:    */   public List<PoolArenaMetric> directArenas()
/* 461:    */   {
/* 462:517 */     return this.directArenaMetrics;
/* 463:    */   }
/* 464:    */   
/* 465:    */   @Deprecated
/* 466:    */   public int numThreadLocalCaches()
/* 467:    */   {
/* 468:527 */     PoolArena<?>[] arenas = this.heapArenas != null ? this.heapArenas : this.directArenas;
/* 469:528 */     if (arenas == null) {
/* 470:529 */       return 0;
/* 471:    */     }
/* 472:532 */     int total = 0;
/* 473:533 */     for (PoolArena<?> arena : arenas) {
/* 474:534 */       total += arena.numThreadCaches.get();
/* 475:    */     }
/* 476:537 */     return total;
/* 477:    */   }
/* 478:    */   
/* 479:    */   @Deprecated
/* 480:    */   public int tinyCacheSize()
/* 481:    */   {
/* 482:547 */     return this.tinyCacheSize;
/* 483:    */   }
/* 484:    */   
/* 485:    */   @Deprecated
/* 486:    */   public int smallCacheSize()
/* 487:    */   {
/* 488:557 */     return this.smallCacheSize;
/* 489:    */   }
/* 490:    */   
/* 491:    */   @Deprecated
/* 492:    */   public int normalCacheSize()
/* 493:    */   {
/* 494:567 */     return this.normalCacheSize;
/* 495:    */   }
/* 496:    */   
/* 497:    */   @Deprecated
/* 498:    */   public final int chunkSize()
/* 499:    */   {
/* 500:577 */     return this.chunkSize;
/* 501:    */   }
/* 502:    */   
/* 503:    */   final long usedHeapMemory()
/* 504:    */   {
/* 505:581 */     return usedMemory(this.heapArenas);
/* 506:    */   }
/* 507:    */   
/* 508:    */   final long usedDirectMemory()
/* 509:    */   {
/* 510:585 */     return usedMemory(this.directArenas);
/* 511:    */   }
/* 512:    */   
/* 513:    */   private static long usedMemory(PoolArena<?>... arenas)
/* 514:    */   {
/* 515:589 */     if (arenas == null) {
/* 516:590 */       return -1L;
/* 517:    */     }
/* 518:592 */     long used = 0L;
/* 519:593 */     for (PoolArena<?> arena : arenas)
/* 520:    */     {
/* 521:594 */       used += arena.numActiveBytes();
/* 522:595 */       if (used < 0L) {
/* 523:596 */         return 9223372036854775807L;
/* 524:    */       }
/* 525:    */     }
/* 526:599 */     return used;
/* 527:    */   }
/* 528:    */   
/* 529:    */   final PoolThreadCache threadCache()
/* 530:    */   {
/* 531:603 */     PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
/* 532:604 */     assert (cache != null);
/* 533:605 */     return cache;
/* 534:    */   }
/* 535:    */   
/* 536:    */   public String dumpStats()
/* 537:    */   {
/* 538:613 */     int heapArenasLen = this.heapArenas == null ? 0 : this.heapArenas.length;
/* 539:    */     
/* 540:    */ 
/* 541:    */ 
/* 542:617 */     StringBuilder buf = new StringBuilder(512).append(heapArenasLen).append(" heap arena(s):").append(StringUtil.NEWLINE);
/* 543:    */     PoolArena<byte[]> localPoolArena1;
/* 544:    */     PoolArena<byte[]> a;
/* 545:618 */     if (heapArenasLen > 0)
/* 546:    */     {
/* 547:619 */       PoolArena[] arrayOfPoolArena1 = this.heapArenas;int i = arrayOfPoolArena1.length;
/* 548:619 */       for (localPoolArena1 = 0; localPoolArena1 < i; localPoolArena1++)
/* 549:    */       {
/* 550:619 */         a = arrayOfPoolArena1[localPoolArena1];
/* 551:620 */         buf.append(a);
/* 552:    */       }
/* 553:    */     }
/* 554:624 */     int directArenasLen = this.directArenas == null ? 0 : this.directArenas.length;
/* 555:    */     
/* 556:626 */     buf.append(directArenasLen)
/* 557:627 */       .append(" direct arena(s):")
/* 558:628 */       .append(StringUtil.NEWLINE);
/* 559:629 */     if (directArenasLen > 0)
/* 560:    */     {
/* 561:630 */       PoolArena[] arrayOfPoolArena2 = this.directArenas;localPoolArena1 = arrayOfPoolArena2.length;
/* 562:630 */       for (a = 0; a < localPoolArena1; a++)
/* 563:    */       {
/* 564:630 */         PoolArena<ByteBuffer> a = arrayOfPoolArena2[a];
/* 565:631 */         buf.append(a);
/* 566:    */       }
/* 567:    */     }
/* 568:635 */     return buf.toString();
/* 569:    */   }
/* 570:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PooledByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */