/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.LongCounter;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import java.nio.Buffer;
/*   7:    */ import java.nio.ByteBuffer;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.Collections;
/*  10:    */ import java.util.List;
/*  11:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  12:    */ 
/*  13:    */ abstract class PoolArena<T>
/*  14:    */   implements PoolArenaMetric
/*  15:    */ {
/*  16: 32 */   static final boolean HAS_UNSAFE = PlatformDependent.hasUnsafe();
/*  17:    */   static final int numTinySubpagePools = 32;
/*  18:    */   final PooledByteBufAllocator parent;
/*  19:    */   private final int maxOrder;
/*  20:    */   final int pageSize;
/*  21:    */   final int pageShifts;
/*  22:    */   final int chunkSize;
/*  23:    */   final int subpageOverflowMask;
/*  24:    */   final int numSmallSubpagePools;
/*  25:    */   final int directMemoryCacheAlignment;
/*  26:    */   final int directMemoryCacheAlignmentMask;
/*  27:    */   private final PoolSubpage<T>[] tinySubpagePools;
/*  28:    */   private final PoolSubpage<T>[] smallSubpagePools;
/*  29:    */   private final PoolChunkList<T> q050;
/*  30:    */   private final PoolChunkList<T> q025;
/*  31:    */   private final PoolChunkList<T> q000;
/*  32:    */   private final PoolChunkList<T> qInit;
/*  33:    */   private final PoolChunkList<T> q075;
/*  34:    */   private final PoolChunkList<T> q100;
/*  35:    */   private final List<PoolChunkListMetric> chunkListMetrics;
/*  36:    */   private long allocationsNormal;
/*  37:    */   
/*  38:    */   static enum SizeClass
/*  39:    */   {
/*  40: 35 */     Tiny,  Small,  Normal;
/*  41:    */     
/*  42:    */     private SizeClass() {}
/*  43:    */   }
/*  44:    */   
/*  45: 67 */   private final LongCounter allocationsTiny = PlatformDependent.newLongCounter();
/*  46: 68 */   private final LongCounter allocationsSmall = PlatformDependent.newLongCounter();
/*  47: 69 */   private final LongCounter allocationsHuge = PlatformDependent.newLongCounter();
/*  48: 70 */   private final LongCounter activeBytesHuge = PlatformDependent.newLongCounter();
/*  49:    */   private long deallocationsTiny;
/*  50:    */   private long deallocationsSmall;
/*  51:    */   private long deallocationsNormal;
/*  52: 77 */   private final LongCounter deallocationsHuge = PlatformDependent.newLongCounter();
/*  53: 80 */   final AtomicInteger numThreadCaches = new AtomicInteger();
/*  54:    */   
/*  55:    */   protected PoolArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int cacheAlignment)
/*  56:    */   {
/*  57: 87 */     this.parent = parent;
/*  58: 88 */     this.pageSize = pageSize;
/*  59: 89 */     this.maxOrder = maxOrder;
/*  60: 90 */     this.pageShifts = pageShifts;
/*  61: 91 */     this.chunkSize = chunkSize;
/*  62: 92 */     this.directMemoryCacheAlignment = cacheAlignment;
/*  63: 93 */     this.directMemoryCacheAlignmentMask = (cacheAlignment - 1);
/*  64: 94 */     this.subpageOverflowMask = (pageSize - 1 ^ 0xFFFFFFFF);
/*  65: 95 */     this.tinySubpagePools = newSubpagePoolArray(32);
/*  66: 96 */     for (int i = 0; i < this.tinySubpagePools.length; i++) {
/*  67: 97 */       this.tinySubpagePools[i] = newSubpagePoolHead(pageSize);
/*  68:    */     }
/*  69:100 */     this.numSmallSubpagePools = (pageShifts - 9);
/*  70:101 */     this.smallSubpagePools = newSubpagePoolArray(this.numSmallSubpagePools);
/*  71:102 */     for (int i = 0; i < this.smallSubpagePools.length; i++) {
/*  72:103 */       this.smallSubpagePools[i] = newSubpagePoolHead(pageSize);
/*  73:    */     }
/*  74:106 */     this.q100 = new PoolChunkList(this, null, 100, 2147483647, chunkSize);
/*  75:107 */     this.q075 = new PoolChunkList(this, this.q100, 75, 100, chunkSize);
/*  76:108 */     this.q050 = new PoolChunkList(this, this.q075, 50, 100, chunkSize);
/*  77:109 */     this.q025 = new PoolChunkList(this, this.q050, 25, 75, chunkSize);
/*  78:110 */     this.q000 = new PoolChunkList(this, this.q025, 1, 50, chunkSize);
/*  79:111 */     this.qInit = new PoolChunkList(this, this.q000, -2147483648, 25, chunkSize);
/*  80:    */     
/*  81:113 */     this.q100.prevList(this.q075);
/*  82:114 */     this.q075.prevList(this.q050);
/*  83:115 */     this.q050.prevList(this.q025);
/*  84:116 */     this.q025.prevList(this.q000);
/*  85:117 */     this.q000.prevList(null);
/*  86:118 */     this.qInit.prevList(this.qInit);
/*  87:    */     
/*  88:120 */     List<PoolChunkListMetric> metrics = new ArrayList(6);
/*  89:121 */     metrics.add(this.qInit);
/*  90:122 */     metrics.add(this.q000);
/*  91:123 */     metrics.add(this.q025);
/*  92:124 */     metrics.add(this.q050);
/*  93:125 */     metrics.add(this.q075);
/*  94:126 */     metrics.add(this.q100);
/*  95:127 */     this.chunkListMetrics = Collections.unmodifiableList(metrics);
/*  96:    */   }
/*  97:    */   
/*  98:    */   private PoolSubpage<T> newSubpagePoolHead(int pageSize)
/*  99:    */   {
/* 100:131 */     PoolSubpage<T> head = new PoolSubpage(pageSize);
/* 101:132 */     head.prev = head;
/* 102:133 */     head.next = head;
/* 103:134 */     return head;
/* 104:    */   }
/* 105:    */   
/* 106:    */   private PoolSubpage<T>[] newSubpagePoolArray(int size)
/* 107:    */   {
/* 108:139 */     return new PoolSubpage[size];
/* 109:    */   }
/* 110:    */   
/* 111:    */   abstract boolean isDirect();
/* 112:    */   
/* 113:    */   PooledByteBuf<T> allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity)
/* 114:    */   {
/* 115:145 */     PooledByteBuf<T> buf = newByteBuf(maxCapacity);
/* 116:146 */     allocate(cache, buf, reqCapacity);
/* 117:147 */     return buf;
/* 118:    */   }
/* 119:    */   
/* 120:    */   static int tinyIdx(int normCapacity)
/* 121:    */   {
/* 122:151 */     return normCapacity >>> 4;
/* 123:    */   }
/* 124:    */   
/* 125:    */   static int smallIdx(int normCapacity)
/* 126:    */   {
/* 127:155 */     int tableIdx = 0;
/* 128:156 */     int i = normCapacity >>> 10;
/* 129:157 */     while (i != 0)
/* 130:    */     {
/* 131:158 */       i >>>= 1;
/* 132:159 */       tableIdx++;
/* 133:    */     }
/* 134:161 */     return tableIdx;
/* 135:    */   }
/* 136:    */   
/* 137:    */   boolean isTinyOrSmall(int normCapacity)
/* 138:    */   {
/* 139:166 */     return (normCapacity & this.subpageOverflowMask) == 0;
/* 140:    */   }
/* 141:    */   
/* 142:    */   static boolean isTiny(int normCapacity)
/* 143:    */   {
/* 144:171 */     return (normCapacity & 0xFFFFFE00) == 0;
/* 145:    */   }
/* 146:    */   
/* 147:    */   private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, int reqCapacity)
/* 148:    */   {
/* 149:175 */     int normCapacity = normalizeCapacity(reqCapacity);
/* 150:176 */     if (isTinyOrSmall(normCapacity))
/* 151:    */     {
/* 152:179 */       boolean tiny = isTiny(normCapacity);
/* 153:    */       PoolSubpage<T>[] table;
/* 154:    */       int tableIdx;
/* 155:    */       PoolSubpage<T>[] table;
/* 156:180 */       if (tiny)
/* 157:    */       {
/* 158:181 */         if (cache.allocateTiny(this, buf, reqCapacity, normCapacity)) {
/* 159:183 */           return;
/* 160:    */         }
/* 161:185 */         int tableIdx = tinyIdx(normCapacity);
/* 162:186 */         table = this.tinySubpagePools;
/* 163:    */       }
/* 164:    */       else
/* 165:    */       {
/* 166:188 */         if (cache.allocateSmall(this, buf, reqCapacity, normCapacity)) {
/* 167:190 */           return;
/* 168:    */         }
/* 169:192 */         tableIdx = smallIdx(normCapacity);
/* 170:193 */         table = this.smallSubpagePools;
/* 171:    */       }
/* 172:196 */       PoolSubpage<T> head = table[tableIdx];
/* 173:202 */       synchronized (head)
/* 174:    */       {
/* 175:203 */         PoolSubpage<T> s = head.next;
/* 176:204 */         if (s != head)
/* 177:    */         {
/* 178:205 */           assert ((s.doNotDestroy) && (s.elemSize == normCapacity));
/* 179:206 */           long handle = s.allocate();
/* 180:207 */           assert (handle >= 0L);
/* 181:208 */           s.chunk.initBufWithSubpage(buf, handle, reqCapacity);
/* 182:209 */           incTinySmallAllocation(tiny);
/* 183:210 */           return;
/* 184:    */         }
/* 185:    */       }
/* 186:213 */       synchronized (this)
/* 187:    */       {
/* 188:214 */         allocateNormal(buf, reqCapacity, normCapacity);
/* 189:    */       }
/* 190:217 */       incTinySmallAllocation(tiny);
/* 191:218 */       return;
/* 192:    */     }
/* 193:220 */     if (normCapacity <= this.chunkSize)
/* 194:    */     {
/* 195:221 */       if (cache.allocateNormal(this, buf, reqCapacity, normCapacity)) {
/* 196:223 */         return;
/* 197:    */       }
/* 198:225 */       synchronized (this)
/* 199:    */       {
/* 200:226 */         allocateNormal(buf, reqCapacity, normCapacity);
/* 201:227 */         this.allocationsNormal += 1L;
/* 202:    */       }
/* 203:    */     }
/* 204:    */     else
/* 205:    */     {
/* 206:231 */       allocateHuge(buf, reqCapacity);
/* 207:    */     }
/* 208:    */   }
/* 209:    */   
/* 210:    */   private void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity)
/* 211:    */   {
/* 212:237 */     if ((this.q050.allocate(buf, reqCapacity, normCapacity)) || (this.q025.allocate(buf, reqCapacity, normCapacity)) || 
/* 213:238 */       (this.q000.allocate(buf, reqCapacity, normCapacity)) || (this.qInit.allocate(buf, reqCapacity, normCapacity)) || 
/* 214:239 */       (this.q075.allocate(buf, reqCapacity, normCapacity))) {
/* 215:240 */       return;
/* 216:    */     }
/* 217:244 */     PoolChunk<T> c = newChunk(this.pageSize, this.maxOrder, this.pageShifts, this.chunkSize);
/* 218:245 */     long handle = c.allocate(normCapacity);
/* 219:246 */     assert (handle > 0L);
/* 220:247 */     c.initBuf(buf, handle, reqCapacity);
/* 221:248 */     this.qInit.add(c);
/* 222:    */   }
/* 223:    */   
/* 224:    */   private void incTinySmallAllocation(boolean tiny)
/* 225:    */   {
/* 226:252 */     if (tiny) {
/* 227:253 */       this.allocationsTiny.increment();
/* 228:    */     } else {
/* 229:255 */       this.allocationsSmall.increment();
/* 230:    */     }
/* 231:    */   }
/* 232:    */   
/* 233:    */   private void allocateHuge(PooledByteBuf<T> buf, int reqCapacity)
/* 234:    */   {
/* 235:260 */     PoolChunk<T> chunk = newUnpooledChunk(reqCapacity);
/* 236:261 */     this.activeBytesHuge.add(chunk.chunkSize());
/* 237:262 */     buf.initUnpooled(chunk, reqCapacity);
/* 238:263 */     this.allocationsHuge.increment();
/* 239:    */   }
/* 240:    */   
/* 241:    */   void free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache)
/* 242:    */   {
/* 243:267 */     if (chunk.unpooled)
/* 244:    */     {
/* 245:268 */       int size = chunk.chunkSize();
/* 246:269 */       destroyChunk(chunk);
/* 247:270 */       this.activeBytesHuge.add(-size);
/* 248:271 */       this.deallocationsHuge.increment();
/* 249:    */     }
/* 250:    */     else
/* 251:    */     {
/* 252:273 */       SizeClass sizeClass = sizeClass(normCapacity);
/* 253:274 */       if ((cache != null) && (cache.add(this, chunk, handle, normCapacity, sizeClass))) {
/* 254:276 */         return;
/* 255:    */       }
/* 256:279 */       freeChunk(chunk, handle, sizeClass);
/* 257:    */     }
/* 258:    */   }
/* 259:    */   
/* 260:    */   private SizeClass sizeClass(int normCapacity)
/* 261:    */   {
/* 262:284 */     if (!isTinyOrSmall(normCapacity)) {
/* 263:285 */       return SizeClass.Normal;
/* 264:    */     }
/* 265:287 */     return isTiny(normCapacity) ? SizeClass.Tiny : SizeClass.Small;
/* 266:    */   }
/* 267:    */   
/* 268:    */   void freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass)
/* 269:    */   {
/* 270:    */     boolean destroyChunk;
/* 271:292 */     synchronized (this)
/* 272:    */     {
/* 273:293 */       switch (1.$SwitchMap$io$netty$buffer$PoolArena$SizeClass[sizeClass.ordinal()])
/* 274:    */       {
/* 275:    */       case 1: 
/* 276:295 */         this.deallocationsNormal += 1L;
/* 277:296 */         break;
/* 278:    */       case 2: 
/* 279:298 */         this.deallocationsSmall += 1L;
/* 280:299 */         break;
/* 281:    */       case 3: 
/* 282:301 */         this.deallocationsTiny += 1L;
/* 283:302 */         break;
/* 284:    */       default: 
/* 285:304 */         throw new Error();
/* 286:    */       }
/* 287:306 */       destroyChunk = !chunk.parent.free(chunk, handle);
/* 288:    */     }
/* 289:    */     boolean destroyChunk;
/* 290:308 */     if (destroyChunk) {
/* 291:310 */       destroyChunk(chunk);
/* 292:    */     }
/* 293:    */   }
/* 294:    */   
/* 295:    */   PoolSubpage<T> findSubpagePoolHead(int elemSize)
/* 296:    */   {
/* 297:    */     PoolSubpage<T>[] table;
/* 298:    */     int tableIdx;
/* 299:    */     PoolSubpage<T>[] table;
/* 300:317 */     if (isTiny(elemSize))
/* 301:    */     {
/* 302:318 */       int tableIdx = elemSize >>> 4;
/* 303:319 */       table = this.tinySubpagePools;
/* 304:    */     }
/* 305:    */     else
/* 306:    */     {
/* 307:321 */       tableIdx = 0;
/* 308:322 */       elemSize >>>= 10;
/* 309:323 */       while (elemSize != 0)
/* 310:    */       {
/* 311:324 */         elemSize >>>= 1;
/* 312:325 */         tableIdx++;
/* 313:    */       }
/* 314:327 */       table = this.smallSubpagePools;
/* 315:    */     }
/* 316:330 */     return table[tableIdx];
/* 317:    */   }
/* 318:    */   
/* 319:    */   int normalizeCapacity(int reqCapacity)
/* 320:    */   {
/* 321:334 */     if (reqCapacity < 0) {
/* 322:335 */       throw new IllegalArgumentException("capacity: " + reqCapacity + " (expected: 0+)");
/* 323:    */     }
/* 324:338 */     if (reqCapacity >= this.chunkSize) {
/* 325:339 */       return this.directMemoryCacheAlignment == 0 ? reqCapacity : alignCapacity(reqCapacity);
/* 326:    */     }
/* 327:342 */     if (!isTiny(reqCapacity))
/* 328:    */     {
/* 329:345 */       int normalizedCapacity = reqCapacity;
/* 330:346 */       normalizedCapacity--;
/* 331:347 */       normalizedCapacity |= normalizedCapacity >>> 1;
/* 332:348 */       normalizedCapacity |= normalizedCapacity >>> 2;
/* 333:349 */       normalizedCapacity |= normalizedCapacity >>> 4;
/* 334:350 */       normalizedCapacity |= normalizedCapacity >>> 8;
/* 335:351 */       normalizedCapacity |= normalizedCapacity >>> 16;
/* 336:352 */       normalizedCapacity++;
/* 337:354 */       if (normalizedCapacity < 0) {
/* 338:355 */         normalizedCapacity >>>= 1;
/* 339:    */       }
/* 340:357 */       assert ((this.directMemoryCacheAlignment == 0) || ((normalizedCapacity & this.directMemoryCacheAlignmentMask) == 0));
/* 341:    */       
/* 342:359 */       return normalizedCapacity;
/* 343:    */     }
/* 344:362 */     if (this.directMemoryCacheAlignment > 0) {
/* 345:363 */       return alignCapacity(reqCapacity);
/* 346:    */     }
/* 347:367 */     if ((reqCapacity & 0xF) == 0) {
/* 348:368 */       return reqCapacity;
/* 349:    */     }
/* 350:371 */     return (reqCapacity & 0xFFFFFFF0) + 16;
/* 351:    */   }
/* 352:    */   
/* 353:    */   int alignCapacity(int reqCapacity)
/* 354:    */   {
/* 355:375 */     int delta = reqCapacity & this.directMemoryCacheAlignmentMask;
/* 356:376 */     return delta == 0 ? reqCapacity : reqCapacity + this.directMemoryCacheAlignment - delta;
/* 357:    */   }
/* 358:    */   
/* 359:    */   void reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemory)
/* 360:    */   {
/* 361:380 */     if ((newCapacity < 0) || (newCapacity > buf.maxCapacity())) {
/* 362:381 */       throw new IllegalArgumentException("newCapacity: " + newCapacity);
/* 363:    */     }
/* 364:384 */     int oldCapacity = buf.length;
/* 365:385 */     if (oldCapacity == newCapacity) {
/* 366:386 */       return;
/* 367:    */     }
/* 368:389 */     PoolChunk<T> oldChunk = buf.chunk;
/* 369:390 */     long oldHandle = buf.handle;
/* 370:391 */     T oldMemory = buf.memory;
/* 371:392 */     int oldOffset = buf.offset;
/* 372:393 */     int oldMaxLength = buf.maxLength;
/* 373:394 */     int readerIndex = buf.readerIndex();
/* 374:395 */     int writerIndex = buf.writerIndex();
/* 375:    */     
/* 376:397 */     allocate(this.parent.threadCache(), buf, newCapacity);
/* 377:398 */     if (newCapacity > oldCapacity) {
/* 378:399 */       memoryCopy(oldMemory, oldOffset, buf.memory, buf.offset, oldCapacity);
/* 379:402 */     } else if (newCapacity < oldCapacity) {
/* 380:403 */       if (readerIndex < newCapacity)
/* 381:    */       {
/* 382:404 */         if (writerIndex > newCapacity) {
/* 383:405 */           writerIndex = newCapacity;
/* 384:    */         }
/* 385:407 */         memoryCopy(oldMemory, oldOffset + readerIndex, buf.memory, buf.offset + readerIndex, writerIndex - readerIndex);
/* 386:    */       }
/* 387:    */       else
/* 388:    */       {
/* 389:411 */         readerIndex = writerIndex = newCapacity;
/* 390:    */       }
/* 391:    */     }
/* 392:415 */     buf.setIndex(readerIndex, writerIndex);
/* 393:417 */     if (freeOldMemory) {
/* 394:418 */       free(oldChunk, oldHandle, oldMaxLength, buf.cache);
/* 395:    */     }
/* 396:    */   }
/* 397:    */   
/* 398:    */   public int numThreadCaches()
/* 399:    */   {
/* 400:424 */     return this.numThreadCaches.get();
/* 401:    */   }
/* 402:    */   
/* 403:    */   public int numTinySubpages()
/* 404:    */   {
/* 405:429 */     return this.tinySubpagePools.length;
/* 406:    */   }
/* 407:    */   
/* 408:    */   public int numSmallSubpages()
/* 409:    */   {
/* 410:434 */     return this.smallSubpagePools.length;
/* 411:    */   }
/* 412:    */   
/* 413:    */   public int numChunkLists()
/* 414:    */   {
/* 415:439 */     return this.chunkListMetrics.size();
/* 416:    */   }
/* 417:    */   
/* 418:    */   public List<PoolSubpageMetric> tinySubpages()
/* 419:    */   {
/* 420:444 */     return subPageMetricList(this.tinySubpagePools);
/* 421:    */   }
/* 422:    */   
/* 423:    */   public List<PoolSubpageMetric> smallSubpages()
/* 424:    */   {
/* 425:449 */     return subPageMetricList(this.smallSubpagePools);
/* 426:    */   }
/* 427:    */   
/* 428:    */   public List<PoolChunkListMetric> chunkLists()
/* 429:    */   {
/* 430:454 */     return this.chunkListMetrics;
/* 431:    */   }
/* 432:    */   
/* 433:    */   private static List<PoolSubpageMetric> subPageMetricList(PoolSubpage<?>[] pages)
/* 434:    */   {
/* 435:458 */     List<PoolSubpageMetric> metrics = new ArrayList();
/* 436:459 */     for (PoolSubpage<?> head : pages) {
/* 437:460 */       if (head.next != head)
/* 438:    */       {
/* 439:463 */         PoolSubpage<?> s = head.next;
/* 440:    */         for (;;)
/* 441:    */         {
/* 442:465 */           metrics.add(s);
/* 443:466 */           s = s.next;
/* 444:467 */           if (s == head) {
/* 445:    */             break;
/* 446:    */           }
/* 447:    */         }
/* 448:    */       }
/* 449:    */     }
/* 450:472 */     return metrics;
/* 451:    */   }
/* 452:    */   
/* 453:    */   public long numAllocations()
/* 454:    */   {
/* 455:    */     long allocsNormal;
/* 456:478 */     synchronized (this)
/* 457:    */     {
/* 458:479 */       allocsNormal = this.allocationsNormal;
/* 459:    */     }
/* 460:    */     long allocsNormal;
/* 461:481 */     return this.allocationsTiny.value() + this.allocationsSmall.value() + allocsNormal + this.allocationsHuge.value();
/* 462:    */   }
/* 463:    */   
/* 464:    */   public long numTinyAllocations()
/* 465:    */   {
/* 466:486 */     return this.allocationsTiny.value();
/* 467:    */   }
/* 468:    */   
/* 469:    */   public long numSmallAllocations()
/* 470:    */   {
/* 471:491 */     return this.allocationsSmall.value();
/* 472:    */   }
/* 473:    */   
/* 474:    */   public synchronized long numNormalAllocations()
/* 475:    */   {
/* 476:496 */     return this.allocationsNormal;
/* 477:    */   }
/* 478:    */   
/* 479:    */   public long numDeallocations()
/* 480:    */   {
/* 481:    */     long deallocs;
/* 482:502 */     synchronized (this)
/* 483:    */     {
/* 484:503 */       deallocs = this.deallocationsTiny + this.deallocationsSmall + this.deallocationsNormal;
/* 485:    */     }
/* 486:    */     long deallocs;
/* 487:505 */     return deallocs + this.deallocationsHuge.value();
/* 488:    */   }
/* 489:    */   
/* 490:    */   public synchronized long numTinyDeallocations()
/* 491:    */   {
/* 492:510 */     return this.deallocationsTiny;
/* 493:    */   }
/* 494:    */   
/* 495:    */   public synchronized long numSmallDeallocations()
/* 496:    */   {
/* 497:515 */     return this.deallocationsSmall;
/* 498:    */   }
/* 499:    */   
/* 500:    */   public synchronized long numNormalDeallocations()
/* 501:    */   {
/* 502:520 */     return this.deallocationsNormal;
/* 503:    */   }
/* 504:    */   
/* 505:    */   public long numHugeAllocations()
/* 506:    */   {
/* 507:525 */     return this.allocationsHuge.value();
/* 508:    */   }
/* 509:    */   
/* 510:    */   public long numHugeDeallocations()
/* 511:    */   {
/* 512:530 */     return this.deallocationsHuge.value();
/* 513:    */   }
/* 514:    */   
/* 515:    */   public long numActiveAllocations()
/* 516:    */   {
/* 517:536 */     long val = this.allocationsTiny.value() + this.allocationsSmall.value() + this.allocationsHuge.value() - this.deallocationsHuge.value();
/* 518:537 */     synchronized (this)
/* 519:    */     {
/* 520:538 */       val += this.allocationsNormal - (this.deallocationsTiny + this.deallocationsSmall + this.deallocationsNormal);
/* 521:    */     }
/* 522:540 */     return Math.max(val, 0L);
/* 523:    */   }
/* 524:    */   
/* 525:    */   public long numActiveTinyAllocations()
/* 526:    */   {
/* 527:545 */     return Math.max(numTinyAllocations() - numTinyDeallocations(), 0L);
/* 528:    */   }
/* 529:    */   
/* 530:    */   public long numActiveSmallAllocations()
/* 531:    */   {
/* 532:550 */     return Math.max(numSmallAllocations() - numSmallDeallocations(), 0L);
/* 533:    */   }
/* 534:    */   
/* 535:    */   public long numActiveNormalAllocations()
/* 536:    */   {
/* 537:    */     long val;
/* 538:556 */     synchronized (this)
/* 539:    */     {
/* 540:557 */       val = this.allocationsNormal - this.deallocationsNormal;
/* 541:    */     }
/* 542:    */     long val;
/* 543:559 */     return Math.max(val, 0L);
/* 544:    */   }
/* 545:    */   
/* 546:    */   public long numActiveHugeAllocations()
/* 547:    */   {
/* 548:564 */     return Math.max(numHugeAllocations() - numHugeDeallocations(), 0L);
/* 549:    */   }
/* 550:    */   
/* 551:    */   public long numActiveBytes()
/* 552:    */   {
/* 553:569 */     long val = this.activeBytesHuge.value();
/* 554:570 */     synchronized (this)
/* 555:    */     {
/* 556:571 */       for (int i = 0; i < this.chunkListMetrics.size(); i++) {
/* 557:572 */         for (PoolChunkMetric m : (PoolChunkListMetric)this.chunkListMetrics.get(i)) {
/* 558:573 */           val += m.chunkSize();
/* 559:    */         }
/* 560:    */       }
/* 561:    */     }
/* 562:577 */     return Math.max(0L, val);
/* 563:    */   }
/* 564:    */   
/* 565:    */   protected abstract PoolChunk<T> newChunk(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
/* 566:    */   
/* 567:    */   protected abstract PoolChunk<T> newUnpooledChunk(int paramInt);
/* 568:    */   
/* 569:    */   protected abstract PooledByteBuf<T> newByteBuf(int paramInt);
/* 570:    */   
/* 571:    */   protected abstract void memoryCopy(T paramT1, int paramInt1, T paramT2, int paramInt2, int paramInt3);
/* 572:    */   
/* 573:    */   protected abstract void destroyChunk(PoolChunk<T> paramPoolChunk);
/* 574:    */   
/* 575:    */   public synchronized String toString()
/* 576:    */   {
/* 577:613 */     StringBuilder buf = new StringBuilder().append("Chunk(s) at 0~25%:").append(StringUtil.NEWLINE).append(this.qInit).append(StringUtil.NEWLINE).append("Chunk(s) at 0~50%:").append(StringUtil.NEWLINE).append(this.q000).append(StringUtil.NEWLINE).append("Chunk(s) at 25~75%:").append(StringUtil.NEWLINE).append(this.q025).append(StringUtil.NEWLINE).append("Chunk(s) at 50~100%:").append(StringUtil.NEWLINE).append(this.q050).append(StringUtil.NEWLINE).append("Chunk(s) at 75~100%:").append(StringUtil.NEWLINE).append(this.q075).append(StringUtil.NEWLINE).append("Chunk(s) at 100%:").append(StringUtil.NEWLINE).append(this.q100).append(StringUtil.NEWLINE).append("tiny subpages:");
/* 578:614 */     appendPoolSubPages(buf, this.tinySubpagePools);
/* 579:615 */     buf.append(StringUtil.NEWLINE)
/* 580:616 */       .append("small subpages:");
/* 581:617 */     appendPoolSubPages(buf, this.smallSubpagePools);
/* 582:618 */     buf.append(StringUtil.NEWLINE);
/* 583:    */     
/* 584:620 */     return buf.toString();
/* 585:    */   }
/* 586:    */   
/* 587:    */   private static void appendPoolSubPages(StringBuilder buf, PoolSubpage<?>[] subpages)
/* 588:    */   {
/* 589:624 */     for (int i = 0; i < subpages.length; i++)
/* 590:    */     {
/* 591:625 */       PoolSubpage<?> head = subpages[i];
/* 592:626 */       if (head.next != head)
/* 593:    */       {
/* 594:632 */         buf.append(StringUtil.NEWLINE).append(i).append(": ");
/* 595:633 */         PoolSubpage<?> s = head.next;
/* 596:    */         for (;;)
/* 597:    */         {
/* 598:635 */           buf.append(s);
/* 599:636 */           s = s.next;
/* 600:637 */           if (s == head) {
/* 601:    */             break;
/* 602:    */           }
/* 603:    */         }
/* 604:    */       }
/* 605:    */     }
/* 606:    */   }
/* 607:    */   
/* 608:    */   protected final void finalize()
/* 609:    */     throws Throwable
/* 610:    */   {
/* 611:    */     try
/* 612:    */     {
/* 613:647 */       super.finalize();
/* 614:    */       
/* 615:649 */       destroyPoolSubPages(this.smallSubpagePools);
/* 616:650 */       destroyPoolSubPages(this.tinySubpagePools);
/* 617:651 */       destroyPoolChunkLists(new PoolChunkList[] { this.qInit, this.q000, this.q025, this.q050, this.q075, this.q100 });
/* 618:    */     }
/* 619:    */     finally
/* 620:    */     {
/* 621:649 */       destroyPoolSubPages(this.smallSubpagePools);
/* 622:650 */       destroyPoolSubPages(this.tinySubpagePools);
/* 623:651 */       destroyPoolChunkLists(new PoolChunkList[] { this.qInit, this.q000, this.q025, this.q050, this.q075, this.q100 });
/* 624:    */     }
/* 625:    */   }
/* 626:    */   
/* 627:    */   private static void destroyPoolSubPages(PoolSubpage<?>[] pages)
/* 628:    */   {
/* 629:656 */     for (PoolSubpage<?> page : pages) {
/* 630:657 */       page.destroy();
/* 631:    */     }
/* 632:    */   }
/* 633:    */   
/* 634:    */   private void destroyPoolChunkLists(PoolChunkList<T>... chunkLists)
/* 635:    */   {
/* 636:662 */     for (PoolChunkList<T> chunkList : chunkLists) {
/* 637:663 */       chunkList.destroy(this);
/* 638:    */     }
/* 639:    */   }
/* 640:    */   
/* 641:    */   static final class HeapArena
/* 642:    */     extends PoolArena<byte[]>
/* 643:    */   {
/* 644:    */     HeapArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int directMemoryCacheAlignment)
/* 645:    */     {
/* 646:671 */       super(pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
/* 647:    */     }
/* 648:    */     
/* 649:    */     private static byte[] newByteArray(int size)
/* 650:    */     {
/* 651:676 */       return PlatformDependent.allocateUninitializedArray(size);
/* 652:    */     }
/* 653:    */     
/* 654:    */     boolean isDirect()
/* 655:    */     {
/* 656:681 */       return false;
/* 657:    */     }
/* 658:    */     
/* 659:    */     protected PoolChunk<byte[]> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize)
/* 660:    */     {
/* 661:686 */       return new PoolChunk(this, newByteArray(chunkSize), pageSize, maxOrder, pageShifts, chunkSize, 0);
/* 662:    */     }
/* 663:    */     
/* 664:    */     protected PoolChunk<byte[]> newUnpooledChunk(int capacity)
/* 665:    */     {
/* 666:691 */       return new PoolChunk(this, newByteArray(capacity), capacity, 0);
/* 667:    */     }
/* 668:    */     
/* 669:    */     protected void destroyChunk(PoolChunk<byte[]> chunk) {}
/* 670:    */     
/* 671:    */     protected PooledByteBuf<byte[]> newByteBuf(int maxCapacity)
/* 672:    */     {
/* 673:701 */       return HAS_UNSAFE ? PooledUnsafeHeapByteBuf.newUnsafeInstance(maxCapacity) : 
/* 674:702 */         PooledHeapByteBuf.newInstance(maxCapacity);
/* 675:    */     }
/* 676:    */     
/* 677:    */     protected void memoryCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length)
/* 678:    */     {
/* 679:707 */       if (length == 0) {
/* 680:708 */         return;
/* 681:    */       }
/* 682:711 */       System.arraycopy(src, srcOffset, dst, dstOffset, length);
/* 683:    */     }
/* 684:    */   }
/* 685:    */   
/* 686:    */   static final class DirectArena
/* 687:    */     extends PoolArena<ByteBuffer>
/* 688:    */   {
/* 689:    */     DirectArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int directMemoryCacheAlignment)
/* 690:    */     {
/* 691:719 */       super(pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
/* 692:    */     }
/* 693:    */     
/* 694:    */     boolean isDirect()
/* 695:    */     {
/* 696:725 */       return true;
/* 697:    */     }
/* 698:    */     
/* 699:    */     private int offsetCacheLine(ByteBuffer memory)
/* 700:    */     {
/* 701:731 */       return HAS_UNSAFE ? 
/* 702:732 */         (int)(PlatformDependent.directBufferAddress(memory) & this.directMemoryCacheAlignmentMask) : 0;
/* 703:    */     }
/* 704:    */     
/* 705:    */     protected PoolChunk<ByteBuffer> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize)
/* 706:    */     {
/* 707:738 */       if (this.directMemoryCacheAlignment == 0) {
/* 708:739 */         return new PoolChunk(this, 
/* 709:740 */           allocateDirect(chunkSize), pageSize, maxOrder, pageShifts, chunkSize, 0);
/* 710:    */       }
/* 711:743 */       ByteBuffer memory = allocateDirect(chunkSize + this.directMemoryCacheAlignment);
/* 712:    */       
/* 713:745 */       return new PoolChunk(this, memory, pageSize, maxOrder, pageShifts, chunkSize, 
/* 714:    */       
/* 715:747 */         offsetCacheLine(memory));
/* 716:    */     }
/* 717:    */     
/* 718:    */     protected PoolChunk<ByteBuffer> newUnpooledChunk(int capacity)
/* 719:    */     {
/* 720:752 */       if (this.directMemoryCacheAlignment == 0) {
/* 721:753 */         return new PoolChunk(this, 
/* 722:754 */           allocateDirect(capacity), capacity, 0);
/* 723:    */       }
/* 724:756 */       ByteBuffer memory = allocateDirect(capacity + this.directMemoryCacheAlignment);
/* 725:    */       
/* 726:758 */       return new PoolChunk(this, memory, capacity, 
/* 727:759 */         offsetCacheLine(memory));
/* 728:    */     }
/* 729:    */     
/* 730:    */     private static ByteBuffer allocateDirect(int capacity)
/* 731:    */     {
/* 732:763 */       return PlatformDependent.useDirectBufferNoCleaner() ? 
/* 733:764 */         PlatformDependent.allocateDirectNoCleaner(capacity) : ByteBuffer.allocateDirect(capacity);
/* 734:    */     }
/* 735:    */     
/* 736:    */     protected void destroyChunk(PoolChunk<ByteBuffer> chunk)
/* 737:    */     {
/* 738:769 */       if (PlatformDependent.useDirectBufferNoCleaner()) {
/* 739:770 */         PlatformDependent.freeDirectNoCleaner((ByteBuffer)chunk.memory);
/* 740:    */       } else {
/* 741:772 */         PlatformDependent.freeDirectBuffer((ByteBuffer)chunk.memory);
/* 742:    */       }
/* 743:    */     }
/* 744:    */     
/* 745:    */     protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity)
/* 746:    */     {
/* 747:778 */       if (HAS_UNSAFE) {
/* 748:779 */         return PooledUnsafeDirectByteBuf.newInstance(maxCapacity);
/* 749:    */       }
/* 750:781 */       return PooledDirectByteBuf.newInstance(maxCapacity);
/* 751:    */     }
/* 752:    */     
/* 753:    */     protected void memoryCopy(ByteBuffer src, int srcOffset, ByteBuffer dst, int dstOffset, int length)
/* 754:    */     {
/* 755:787 */       if (length == 0) {
/* 756:788 */         return;
/* 757:    */       }
/* 758:791 */       if (HAS_UNSAFE)
/* 759:    */       {
/* 760:792 */         PlatformDependent.copyMemory(
/* 761:793 */           PlatformDependent.directBufferAddress(src) + srcOffset, 
/* 762:794 */           PlatformDependent.directBufferAddress(dst) + dstOffset, length);
/* 763:    */       }
/* 764:    */       else
/* 765:    */       {
/* 766:797 */         src = src.duplicate();
/* 767:798 */         dst = dst.duplicate();
/* 768:799 */         src.position(srcOffset).limit(srcOffset + length);
/* 769:800 */         dst.position(dstOffset);
/* 770:801 */         dst.put(src);
/* 771:    */       }
/* 772:    */     }
/* 773:    */   }
/* 774:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolArena
 * JD-Core Version:    0.7.0.1
 */