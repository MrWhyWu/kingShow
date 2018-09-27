/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import io.netty.util.ThreadDeathWatcher;
/*   6:    */ import io.netty.util.internal.MathUtil;
/*   7:    */ import io.netty.util.internal.PlatformDependent;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.nio.ByteBuffer;
/*  11:    */ import java.util.Queue;
/*  12:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  13:    */ 
/*  14:    */ final class PoolThreadCache
/*  15:    */ {
/*  16: 41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PoolThreadCache.class);
/*  17:    */   final PoolArena<byte[]> heapArena;
/*  18:    */   final PoolArena<ByteBuffer> directArena;
/*  19:    */   private final MemoryRegionCache<byte[]>[] tinySubPageHeapCaches;
/*  20:    */   private final MemoryRegionCache<byte[]>[] smallSubPageHeapCaches;
/*  21:    */   private final MemoryRegionCache<ByteBuffer>[] tinySubPageDirectCaches;
/*  22:    */   private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches;
/*  23:    */   private final MemoryRegionCache<byte[]>[] normalHeapCaches;
/*  24:    */   private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches;
/*  25:    */   private final int numShiftsNormalDirect;
/*  26:    */   private final int numShiftsNormalHeap;
/*  27:    */   private final int freeSweepAllocationThreshold;
/*  28:    */   private final Thread deathWatchThread;
/*  29:    */   private final Runnable freeTask;
/*  30:    */   private int allocations;
/*  31:    */   
/*  32:    */   PoolThreadCache(PoolArena<byte[]> heapArena, PoolArena<ByteBuffer> directArena, int tinyCacheSize, int smallCacheSize, int normalCacheSize, int maxCachedBufferCapacity, int freeSweepAllocationThreshold, boolean useThreadDeathWatcher)
/*  33:    */   {
/*  34: 71 */     if (maxCachedBufferCapacity < 0) {
/*  35: 72 */       throw new IllegalArgumentException("maxCachedBufferCapacity: " + maxCachedBufferCapacity + " (expected: >= 0)");
/*  36:    */     }
/*  37: 75 */     this.freeSweepAllocationThreshold = freeSweepAllocationThreshold;
/*  38: 76 */     this.heapArena = heapArena;
/*  39: 77 */     this.directArena = directArena;
/*  40: 78 */     if (directArena != null)
/*  41:    */     {
/*  42: 79 */       this.tinySubPageDirectCaches = createSubPageCaches(tinyCacheSize, 32, PoolArena.SizeClass.Tiny);
/*  43:    */       
/*  44: 81 */       this.smallSubPageDirectCaches = createSubPageCaches(smallCacheSize, directArena.numSmallSubpagePools, PoolArena.SizeClass.Small);
/*  45:    */       
/*  46:    */ 
/*  47: 84 */       this.numShiftsNormalDirect = log2(directArena.pageSize);
/*  48: 85 */       this.normalDirectCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, directArena);
/*  49:    */       
/*  50:    */ 
/*  51: 88 */       directArena.numThreadCaches.getAndIncrement();
/*  52:    */     }
/*  53:    */     else
/*  54:    */     {
/*  55: 91 */       this.tinySubPageDirectCaches = null;
/*  56: 92 */       this.smallSubPageDirectCaches = null;
/*  57: 93 */       this.normalDirectCaches = null;
/*  58: 94 */       this.numShiftsNormalDirect = -1;
/*  59:    */     }
/*  60: 96 */     if (heapArena != null)
/*  61:    */     {
/*  62: 98 */       this.tinySubPageHeapCaches = createSubPageCaches(tinyCacheSize, 32, PoolArena.SizeClass.Tiny);
/*  63:    */       
/*  64:100 */       this.smallSubPageHeapCaches = createSubPageCaches(smallCacheSize, heapArena.numSmallSubpagePools, PoolArena.SizeClass.Small);
/*  65:    */       
/*  66:    */ 
/*  67:103 */       this.numShiftsNormalHeap = log2(heapArena.pageSize);
/*  68:104 */       this.normalHeapCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, heapArena);
/*  69:    */       
/*  70:    */ 
/*  71:107 */       heapArena.numThreadCaches.getAndIncrement();
/*  72:    */     }
/*  73:    */     else
/*  74:    */     {
/*  75:110 */       this.tinySubPageHeapCaches = null;
/*  76:111 */       this.smallSubPageHeapCaches = null;
/*  77:112 */       this.normalHeapCaches = null;
/*  78:113 */       this.numShiftsNormalHeap = -1;
/*  79:    */     }
/*  80:117 */     if (((this.tinySubPageDirectCaches != null) || (this.smallSubPageDirectCaches != null) || (this.normalDirectCaches != null) || (this.tinySubPageHeapCaches != null) || (this.smallSubPageHeapCaches != null) || (this.normalHeapCaches != null)) && (freeSweepAllocationThreshold < 1)) {
/*  81:120 */       throw new IllegalArgumentException("freeSweepAllocationThreshold: " + freeSweepAllocationThreshold + " (expected: > 0)");
/*  82:    */     }
/*  83:124 */     if (useThreadDeathWatcher)
/*  84:    */     {
/*  85:126 */       this.freeTask = new Runnable()
/*  86:    */       {
/*  87:    */         public void run()
/*  88:    */         {
/*  89:129 */           PoolThreadCache.this.free0();
/*  90:    */         }
/*  91:132 */       };
/*  92:133 */       this.deathWatchThread = Thread.currentThread();
/*  93:    */       
/*  94:    */ 
/*  95:    */ 
/*  96:137 */       ThreadDeathWatcher.watch(this.deathWatchThread, this.freeTask);
/*  97:    */     }
/*  98:    */     else
/*  99:    */     {
/* 100:139 */       this.freeTask = null;
/* 101:140 */       this.deathWatchThread = null;
/* 102:    */     }
/* 103:    */   }
/* 104:    */   
/* 105:    */   private static <T> MemoryRegionCache<T>[] createSubPageCaches(int cacheSize, int numCaches, PoolArena.SizeClass sizeClass)
/* 106:    */   {
/* 107:146 */     if ((cacheSize > 0) && (numCaches > 0))
/* 108:    */     {
/* 109:148 */       MemoryRegionCache<T>[] cache = new MemoryRegionCache[numCaches];
/* 110:149 */       for (int i = 0; i < cache.length; i++) {
/* 111:151 */         cache[i] = new SubPageMemoryRegionCache(cacheSize, sizeClass);
/* 112:    */       }
/* 113:153 */       return cache;
/* 114:    */     }
/* 115:155 */     return null;
/* 116:    */   }
/* 117:    */   
/* 118:    */   private static <T> MemoryRegionCache<T>[] createNormalCaches(int cacheSize, int maxCachedBufferCapacity, PoolArena<T> area)
/* 119:    */   {
/* 120:161 */     if ((cacheSize > 0) && (maxCachedBufferCapacity > 0))
/* 121:    */     {
/* 122:162 */       int max = Math.min(area.chunkSize, maxCachedBufferCapacity);
/* 123:163 */       int arraySize = Math.max(1, log2(max / area.pageSize) + 1);
/* 124:    */       
/* 125:    */ 
/* 126:166 */       MemoryRegionCache<T>[] cache = new MemoryRegionCache[arraySize];
/* 127:167 */       for (int i = 0; i < cache.length; i++) {
/* 128:168 */         cache[i] = new NormalMemoryRegionCache(cacheSize);
/* 129:    */       }
/* 130:170 */       return cache;
/* 131:    */     }
/* 132:172 */     return null;
/* 133:    */   }
/* 134:    */   
/* 135:    */   private static int log2(int val)
/* 136:    */   {
/* 137:177 */     int res = 0;
/* 138:178 */     while (val > 1)
/* 139:    */     {
/* 140:179 */       val >>= 1;
/* 141:180 */       res++;
/* 142:    */     }
/* 143:182 */     return res;
/* 144:    */   }
/* 145:    */   
/* 146:    */   boolean allocateTiny(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity)
/* 147:    */   {
/* 148:189 */     return allocate(cacheForTiny(area, normCapacity), buf, reqCapacity);
/* 149:    */   }
/* 150:    */   
/* 151:    */   boolean allocateSmall(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity)
/* 152:    */   {
/* 153:196 */     return allocate(cacheForSmall(area, normCapacity), buf, reqCapacity);
/* 154:    */   }
/* 155:    */   
/* 156:    */   boolean allocateNormal(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity)
/* 157:    */   {
/* 158:203 */     return allocate(cacheForNormal(area, normCapacity), buf, reqCapacity);
/* 159:    */   }
/* 160:    */   
/* 161:    */   private boolean allocate(MemoryRegionCache<?> cache, PooledByteBuf buf, int reqCapacity)
/* 162:    */   {
/* 163:208 */     if (cache == null) {
/* 164:210 */       return false;
/* 165:    */     }
/* 166:212 */     boolean allocated = cache.allocate(buf, reqCapacity);
/* 167:213 */     if (++this.allocations >= this.freeSweepAllocationThreshold)
/* 168:    */     {
/* 169:214 */       this.allocations = 0;
/* 170:215 */       trim();
/* 171:    */     }
/* 172:217 */     return allocated;
/* 173:    */   }
/* 174:    */   
/* 175:    */   boolean add(PoolArena<?> area, PoolChunk chunk, long handle, int normCapacity, PoolArena.SizeClass sizeClass)
/* 176:    */   {
/* 177:226 */     MemoryRegionCache<?> cache = cache(area, normCapacity, sizeClass);
/* 178:227 */     if (cache == null) {
/* 179:228 */       return false;
/* 180:    */     }
/* 181:230 */     return cache.add(chunk, handle);
/* 182:    */   }
/* 183:    */   
/* 184:    */   private MemoryRegionCache<?> cache(PoolArena<?> area, int normCapacity, PoolArena.SizeClass sizeClass)
/* 185:    */   {
/* 186:234 */     switch (2.$SwitchMap$io$netty$buffer$PoolArena$SizeClass[sizeClass.ordinal()])
/* 187:    */     {
/* 188:    */     case 1: 
/* 189:236 */       return cacheForNormal(area, normCapacity);
/* 190:    */     case 2: 
/* 191:238 */       return cacheForSmall(area, normCapacity);
/* 192:    */     case 3: 
/* 193:240 */       return cacheForTiny(area, normCapacity);
/* 194:    */     }
/* 195:242 */     throw new Error();
/* 196:    */   }
/* 197:    */   
/* 198:    */   void free()
/* 199:    */   {
/* 200:250 */     if (this.freeTask != null)
/* 201:    */     {
/* 202:251 */       assert (this.deathWatchThread != null);
/* 203:252 */       ThreadDeathWatcher.unwatch(this.deathWatchThread, this.freeTask);
/* 204:    */     }
/* 205:254 */     free0();
/* 206:    */   }
/* 207:    */   
/* 208:    */   private void free0()
/* 209:    */   {
/* 210:263 */     int numFreed = free(this.tinySubPageDirectCaches) + free(this.smallSubPageDirectCaches) + free(this.normalDirectCaches) + free(this.tinySubPageHeapCaches) + free(this.smallSubPageHeapCaches) + free(this.normalHeapCaches);
/* 211:265 */     if ((numFreed > 0) && (logger.isDebugEnabled())) {
/* 212:266 */       logger.debug("Freed {} thread-local buffer(s) from thread: {}", Integer.valueOf(numFreed), Thread.currentThread().getName());
/* 213:    */     }
/* 214:269 */     if (this.directArena != null) {
/* 215:270 */       this.directArena.numThreadCaches.getAndDecrement();
/* 216:    */     }
/* 217:273 */     if (this.heapArena != null) {
/* 218:274 */       this.heapArena.numThreadCaches.getAndDecrement();
/* 219:    */     }
/* 220:    */   }
/* 221:    */   
/* 222:    */   private static int free(MemoryRegionCache<?>[] caches)
/* 223:    */   {
/* 224:279 */     if (caches == null) {
/* 225:280 */       return 0;
/* 226:    */     }
/* 227:283 */     int numFreed = 0;
/* 228:284 */     for (MemoryRegionCache<?> c : caches) {
/* 229:285 */       numFreed += free(c);
/* 230:    */     }
/* 231:287 */     return numFreed;
/* 232:    */   }
/* 233:    */   
/* 234:    */   private static int free(MemoryRegionCache<?> cache)
/* 235:    */   {
/* 236:291 */     if (cache == null) {
/* 237:292 */       return 0;
/* 238:    */     }
/* 239:294 */     return cache.free();
/* 240:    */   }
/* 241:    */   
/* 242:    */   void trim()
/* 243:    */   {
/* 244:298 */     trim(this.tinySubPageDirectCaches);
/* 245:299 */     trim(this.smallSubPageDirectCaches);
/* 246:300 */     trim(this.normalDirectCaches);
/* 247:301 */     trim(this.tinySubPageHeapCaches);
/* 248:302 */     trim(this.smallSubPageHeapCaches);
/* 249:303 */     trim(this.normalHeapCaches);
/* 250:    */   }
/* 251:    */   
/* 252:    */   private static void trim(MemoryRegionCache<?>[] caches)
/* 253:    */   {
/* 254:307 */     if (caches == null) {
/* 255:308 */       return;
/* 256:    */     }
/* 257:310 */     for (MemoryRegionCache<?> c : caches) {
/* 258:311 */       trim(c);
/* 259:    */     }
/* 260:    */   }
/* 261:    */   
/* 262:    */   private static void trim(MemoryRegionCache<?> cache)
/* 263:    */   {
/* 264:316 */     if (cache == null) {
/* 265:317 */       return;
/* 266:    */     }
/* 267:319 */     cache.trim();
/* 268:    */   }
/* 269:    */   
/* 270:    */   private MemoryRegionCache<?> cacheForTiny(PoolArena<?> area, int normCapacity)
/* 271:    */   {
/* 272:323 */     int idx = PoolArena.tinyIdx(normCapacity);
/* 273:324 */     if (area.isDirect()) {
/* 274:325 */       return cache(this.tinySubPageDirectCaches, idx);
/* 275:    */     }
/* 276:327 */     return cache(this.tinySubPageHeapCaches, idx);
/* 277:    */   }
/* 278:    */   
/* 279:    */   private MemoryRegionCache<?> cacheForSmall(PoolArena<?> area, int normCapacity)
/* 280:    */   {
/* 281:331 */     int idx = PoolArena.smallIdx(normCapacity);
/* 282:332 */     if (area.isDirect()) {
/* 283:333 */       return cache(this.smallSubPageDirectCaches, idx);
/* 284:    */     }
/* 285:335 */     return cache(this.smallSubPageHeapCaches, idx);
/* 286:    */   }
/* 287:    */   
/* 288:    */   private MemoryRegionCache<?> cacheForNormal(PoolArena<?> area, int normCapacity)
/* 289:    */   {
/* 290:339 */     if (area.isDirect())
/* 291:    */     {
/* 292:340 */       int idx = log2(normCapacity >> this.numShiftsNormalDirect);
/* 293:341 */       return cache(this.normalDirectCaches, idx);
/* 294:    */     }
/* 295:343 */     int idx = log2(normCapacity >> this.numShiftsNormalHeap);
/* 296:344 */     return cache(this.normalHeapCaches, idx);
/* 297:    */   }
/* 298:    */   
/* 299:    */   private static <T> MemoryRegionCache<T> cache(MemoryRegionCache<T>[] cache, int idx)
/* 300:    */   {
/* 301:348 */     if ((cache == null) || (idx > cache.length - 1)) {
/* 302:349 */       return null;
/* 303:    */     }
/* 304:351 */     return cache[idx];
/* 305:    */   }
/* 306:    */   
/* 307:    */   private static final class SubPageMemoryRegionCache<T>
/* 308:    */     extends PoolThreadCache.MemoryRegionCache<T>
/* 309:    */   {
/* 310:    */     SubPageMemoryRegionCache(int size, PoolArena.SizeClass sizeClass)
/* 311:    */     {
/* 312:359 */       super(sizeClass);
/* 313:    */     }
/* 314:    */     
/* 315:    */     protected void initBuf(PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity)
/* 316:    */     {
/* 317:365 */       chunk.initBufWithSubpage(buf, handle, reqCapacity);
/* 318:    */     }
/* 319:    */   }
/* 320:    */   
/* 321:    */   private static final class NormalMemoryRegionCache<T>
/* 322:    */     extends PoolThreadCache.MemoryRegionCache<T>
/* 323:    */   {
/* 324:    */     NormalMemoryRegionCache(int size)
/* 325:    */     {
/* 326:374 */       super(PoolArena.SizeClass.Normal);
/* 327:    */     }
/* 328:    */     
/* 329:    */     protected void initBuf(PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity)
/* 330:    */     {
/* 331:380 */       chunk.initBuf(buf, handle, reqCapacity);
/* 332:    */     }
/* 333:    */   }
/* 334:    */   
/* 335:    */   private static abstract class MemoryRegionCache<T>
/* 336:    */   {
/* 337:    */     private final int size;
/* 338:    */     private final Queue<Entry<T>> queue;
/* 339:    */     private final PoolArena.SizeClass sizeClass;
/* 340:    */     private int allocations;
/* 341:    */     
/* 342:    */     MemoryRegionCache(int size, PoolArena.SizeClass sizeClass)
/* 343:    */     {
/* 344:391 */       this.size = MathUtil.safeFindNextPositivePowerOfTwo(size);
/* 345:392 */       this.queue = PlatformDependent.newFixedMpscQueue(this.size);
/* 346:393 */       this.sizeClass = sizeClass;
/* 347:    */     }
/* 348:    */     
/* 349:    */     protected abstract void initBuf(PoolChunk<T> paramPoolChunk, long paramLong, PooledByteBuf<T> paramPooledByteBuf, int paramInt);
/* 350:    */     
/* 351:    */     public final boolean add(PoolChunk<T> chunk, long handle)
/* 352:    */     {
/* 353:407 */       Entry<T> entry = newEntry(chunk, handle);
/* 354:408 */       boolean queued = this.queue.offer(entry);
/* 355:409 */       if (!queued) {
/* 356:411 */         entry.recycle();
/* 357:    */       }
/* 358:414 */       return queued;
/* 359:    */     }
/* 360:    */     
/* 361:    */     public final boolean allocate(PooledByteBuf<T> buf, int reqCapacity)
/* 362:    */     {
/* 363:421 */       Entry<T> entry = (Entry)this.queue.poll();
/* 364:422 */       if (entry == null) {
/* 365:423 */         return false;
/* 366:    */       }
/* 367:425 */       initBuf(entry.chunk, entry.handle, buf, reqCapacity);
/* 368:426 */       entry.recycle();
/* 369:    */       
/* 370:    */ 
/* 371:429 */       this.allocations += 1;
/* 372:430 */       return true;
/* 373:    */     }
/* 374:    */     
/* 375:    */     public final int free()
/* 376:    */     {
/* 377:437 */       return free(2147483647);
/* 378:    */     }
/* 379:    */     
/* 380:    */     private int free(int max)
/* 381:    */     {
/* 382:441 */       for (int numFreed = 0; numFreed < max; numFreed++)
/* 383:    */       {
/* 384:443 */         Entry<T> entry = (Entry)this.queue.poll();
/* 385:444 */         if (entry != null) {
/* 386:445 */           freeEntry(entry);
/* 387:    */         } else {
/* 388:448 */           return numFreed;
/* 389:    */         }
/* 390:    */       }
/* 391:451 */       return numFreed;
/* 392:    */     }
/* 393:    */     
/* 394:    */     public final void trim()
/* 395:    */     {
/* 396:458 */       int free = this.size - this.allocations;
/* 397:459 */       this.allocations = 0;
/* 398:462 */       if (free > 0) {
/* 399:463 */         free(free);
/* 400:    */       }
/* 401:    */     }
/* 402:    */     
/* 403:    */     private void freeEntry(Entry entry)
/* 404:    */     {
/* 405:469 */       PoolChunk chunk = entry.chunk;
/* 406:470 */       long handle = entry.handle;
/* 407:    */       
/* 408:    */ 
/* 409:473 */       entry.recycle();
/* 410:    */       
/* 411:475 */       chunk.arena.freeChunk(chunk, handle, this.sizeClass);
/* 412:    */     }
/* 413:    */     
/* 414:    */     static final class Entry<T>
/* 415:    */     {
/* 416:    */       final Recycler.Handle<Entry<?>> recyclerHandle;
/* 417:    */       PoolChunk<T> chunk;
/* 418:481 */       long handle = -1L;
/* 419:    */       
/* 420:    */       Entry(Recycler.Handle<Entry<?>> recyclerHandle)
/* 421:    */       {
/* 422:484 */         this.recyclerHandle = recyclerHandle;
/* 423:    */       }
/* 424:    */       
/* 425:    */       void recycle()
/* 426:    */       {
/* 427:488 */         this.chunk = null;
/* 428:489 */         this.handle = -1L;
/* 429:490 */         this.recyclerHandle.recycle(this);
/* 430:    */       }
/* 431:    */     }
/* 432:    */     
/* 433:    */     private static Entry newEntry(PoolChunk<?> chunk, long handle)
/* 434:    */     {
/* 435:496 */       Entry entry = (Entry)RECYCLER.get();
/* 436:497 */       entry.chunk = chunk;
/* 437:498 */       entry.handle = handle;
/* 438:499 */       return entry;
/* 439:    */     }
/* 440:    */     
/* 441:503 */     private static final Recycler<Entry> RECYCLER = new Recycler()
/* 442:    */     {
/* 443:    */       protected PoolThreadCache.MemoryRegionCache.Entry newObject(Recycler.Handle<PoolThreadCache.MemoryRegionCache.Entry> handle)
/* 444:    */       {
/* 445:507 */         return new PoolThreadCache.MemoryRegionCache.Entry(handle);
/* 446:    */       }
/* 447:    */     };
/* 448:    */   }
/* 449:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolThreadCache
 * JD-Core Version:    0.7.0.1
 */