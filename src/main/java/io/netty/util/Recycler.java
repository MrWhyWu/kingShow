/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   4:    */ import io.netty.util.internal.MathUtil;
/*   5:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.lang.ref.WeakReference;
/*   9:    */ import java.util.Arrays;
/*  10:    */ import java.util.Map;
/*  11:    */ import java.util.WeakHashMap;
/*  12:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  13:    */ 
/*  14:    */ public abstract class Recycler<T>
/*  15:    */ {
/*  16: 41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);
/*  17: 44 */   private static final Handle NOOP_HANDLE = new Handle()
/*  18:    */   {
/*  19:    */     public void recycle(Object object) {}
/*  20:    */   };
/*  21: 50 */   private static final AtomicInteger ID_GENERATOR = new AtomicInteger(-2147483648);
/*  22: 51 */   private static final int OWN_THREAD_ID = ID_GENERATOR.getAndIncrement();
/*  23:    */   private static final int DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD = 32768;
/*  24:    */   private static final int DEFAULT_MAX_CAPACITY_PER_THREAD;
/*  25:    */   
/*  26:    */   static
/*  27:    */   {
/*  28: 64 */     int maxCapacityPerThread = SystemPropertyUtil.getInt("io.netty.recycler.maxCapacityPerThread", 
/*  29: 65 */       SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity", 32768));
/*  30: 66 */     if (maxCapacityPerThread < 0) {
/*  31: 67 */       maxCapacityPerThread = 32768;
/*  32:    */     }
/*  33: 70 */     DEFAULT_MAX_CAPACITY_PER_THREAD = maxCapacityPerThread;
/*  34:    */     
/*  35: 72 */     MAX_SHARED_CAPACITY_FACTOR = Math.max(2, 
/*  36: 73 */       SystemPropertyUtil.getInt("io.netty.recycler.maxSharedCapacityFactor", 2));
/*  37:    */     
/*  38:    */ 
/*  39: 76 */     MAX_DELAYED_QUEUES_PER_THREAD = Math.max(0, 
/*  40: 77 */       SystemPropertyUtil.getInt("io.netty.recycler.maxDelayedQueuesPerThread", 
/*  41:    */       
/*  42: 79 */       NettyRuntime.availableProcessors() * 2));
/*  43:    */     
/*  44: 81 */     LINK_CAPACITY = MathUtil.safeFindNextPositivePowerOfTwo(
/*  45: 82 */       Math.max(SystemPropertyUtil.getInt("io.netty.recycler.linkCapacity", 16), 16));
/*  46:    */     
/*  47:    */ 
/*  48:    */ 
/*  49:    */ 
/*  50: 87 */     RATIO = MathUtil.safeFindNextPositivePowerOfTwo(SystemPropertyUtil.getInt("io.netty.recycler.ratio", 8));
/*  51: 89 */     if (logger.isDebugEnabled()) {
/*  52: 90 */       if (DEFAULT_MAX_CAPACITY_PER_THREAD == 0)
/*  53:    */       {
/*  54: 91 */         logger.debug("-Dio.netty.recycler.maxCapacityPerThread: disabled");
/*  55: 92 */         logger.debug("-Dio.netty.recycler.maxSharedCapacityFactor: disabled");
/*  56: 93 */         logger.debug("-Dio.netty.recycler.linkCapacity: disabled");
/*  57: 94 */         logger.debug("-Dio.netty.recycler.ratio: disabled");
/*  58:    */       }
/*  59:    */       else
/*  60:    */       {
/*  61: 96 */         logger.debug("-Dio.netty.recycler.maxCapacityPerThread: {}", Integer.valueOf(DEFAULT_MAX_CAPACITY_PER_THREAD));
/*  62: 97 */         logger.debug("-Dio.netty.recycler.maxSharedCapacityFactor: {}", Integer.valueOf(MAX_SHARED_CAPACITY_FACTOR));
/*  63: 98 */         logger.debug("-Dio.netty.recycler.linkCapacity: {}", Integer.valueOf(LINK_CAPACITY));
/*  64: 99 */         logger.debug("-Dio.netty.recycler.ratio: {}", Integer.valueOf(RATIO));
/*  65:    */       }
/*  66:    */     }
/*  67:    */   }
/*  68:    */   
/*  69:103 */   private static final int INITIAL_CAPACITY = Math.min(DEFAULT_MAX_CAPACITY_PER_THREAD, 256);
/*  70:    */   private static final int MAX_SHARED_CAPACITY_FACTOR;
/*  71:    */   private static final int MAX_DELAYED_QUEUES_PER_THREAD;
/*  72:    */   private static final int LINK_CAPACITY;
/*  73:    */   private static final int RATIO;
/*  74:    */   private final int maxCapacityPerThread;
/*  75:    */   private final int maxSharedCapacityFactor;
/*  76:    */   private final int ratioMask;
/*  77:    */   private final int maxDelayedQueuesPerThread;
/*  78:111 */   private final FastThreadLocal<Stack<T>> threadLocal = new FastThreadLocal()
/*  79:    */   {
/*  80:    */     protected Recycler.Stack<T> initialValue()
/*  81:    */     {
/*  82:114 */       return new Recycler.Stack(Recycler.this, Thread.currentThread(), Recycler.this.maxCapacityPerThread, Recycler.this.maxSharedCapacityFactor, 
/*  83:115 */         Recycler.this.ratioMask, Recycler.this.maxDelayedQueuesPerThread);
/*  84:    */     }
/*  85:    */   };
/*  86:    */   
/*  87:    */   protected Recycler()
/*  88:    */   {
/*  89:120 */     this(DEFAULT_MAX_CAPACITY_PER_THREAD);
/*  90:    */   }
/*  91:    */   
/*  92:    */   protected Recycler(int maxCapacityPerThread)
/*  93:    */   {
/*  94:124 */     this(maxCapacityPerThread, MAX_SHARED_CAPACITY_FACTOR);
/*  95:    */   }
/*  96:    */   
/*  97:    */   protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor)
/*  98:    */   {
/*  99:128 */     this(maxCapacityPerThread, maxSharedCapacityFactor, RATIO, MAX_DELAYED_QUEUES_PER_THREAD);
/* 100:    */   }
/* 101:    */   
/* 102:    */   protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor, int ratio, int maxDelayedQueuesPerThread)
/* 103:    */   {
/* 104:133 */     this.ratioMask = (MathUtil.safeFindNextPositivePowerOfTwo(ratio) - 1);
/* 105:134 */     if (maxCapacityPerThread <= 0)
/* 106:    */     {
/* 107:135 */       this.maxCapacityPerThread = 0;
/* 108:136 */       this.maxSharedCapacityFactor = 1;
/* 109:137 */       this.maxDelayedQueuesPerThread = 0;
/* 110:    */     }
/* 111:    */     else
/* 112:    */     {
/* 113:139 */       this.maxCapacityPerThread = maxCapacityPerThread;
/* 114:140 */       this.maxSharedCapacityFactor = Math.max(1, maxSharedCapacityFactor);
/* 115:141 */       this.maxDelayedQueuesPerThread = Math.max(0, maxDelayedQueuesPerThread);
/* 116:    */     }
/* 117:    */   }
/* 118:    */   
/* 119:    */   public final T get()
/* 120:    */   {
/* 121:147 */     if (this.maxCapacityPerThread == 0) {
/* 122:148 */       return newObject(NOOP_HANDLE);
/* 123:    */     }
/* 124:150 */     Stack<T> stack = (Stack)this.threadLocal.get();
/* 125:151 */     DefaultHandle<T> handle = stack.pop();
/* 126:152 */     if (handle == null)
/* 127:    */     {
/* 128:153 */       handle = stack.newHandle();
/* 129:154 */       handle.value = newObject(handle);
/* 130:    */     }
/* 131:156 */     return handle.value;
/* 132:    */   }
/* 133:    */   
/* 134:    */   @Deprecated
/* 135:    */   public final boolean recycle(T o, Handle<T> handle)
/* 136:    */   {
/* 137:164 */     if (handle == NOOP_HANDLE) {
/* 138:165 */       return false;
/* 139:    */     }
/* 140:168 */     DefaultHandle<T> h = (DefaultHandle)handle;
/* 141:169 */     if (h.stack.parent != this) {
/* 142:170 */       return false;
/* 143:    */     }
/* 144:173 */     h.recycle(o);
/* 145:174 */     return true;
/* 146:    */   }
/* 147:    */   
/* 148:    */   final int threadLocalCapacity()
/* 149:    */   {
/* 150:178 */     return ((Stack)this.threadLocal.get()).elements.length;
/* 151:    */   }
/* 152:    */   
/* 153:    */   final int threadLocalSize()
/* 154:    */   {
/* 155:182 */     return ((Stack)this.threadLocal.get()).size;
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected abstract T newObject(Handle<T> paramHandle);
/* 159:    */   
/* 160:    */   public static abstract interface Handle<T>
/* 161:    */   {
/* 162:    */     public abstract void recycle(T paramT);
/* 163:    */   }
/* 164:    */   
/* 165:    */   static final class DefaultHandle<T>
/* 166:    */     implements Recycler.Handle<T>
/* 167:    */   {
/* 168:    */     private int lastRecycledId;
/* 169:    */     private int recycleId;
/* 170:    */     boolean hasBeenRecycled;
/* 171:    */     private Recycler.Stack<?> stack;
/* 172:    */     private Object value;
/* 173:    */     
/* 174:    */     DefaultHandle(Recycler.Stack<?> stack)
/* 175:    */     {
/* 176:201 */       this.stack = stack;
/* 177:    */     }
/* 178:    */     
/* 179:    */     public void recycle(Object object)
/* 180:    */     {
/* 181:206 */       if (object != this.value) {
/* 182:207 */         throw new IllegalArgumentException("object does not belong to handle");
/* 183:    */       }
/* 184:209 */       this.stack.push(this);
/* 185:    */     }
/* 186:    */   }
/* 187:    */   
/* 188:213 */   private static final FastThreadLocal<Map<Stack<?>, WeakOrderQueue>> DELAYED_RECYCLED = new FastThreadLocal()
/* 189:    */   {
/* 190:    */     protected Map<Recycler.Stack<?>, Recycler.WeakOrderQueue> initialValue()
/* 191:    */     {
/* 192:217 */       return new WeakHashMap();
/* 193:    */     }
/* 194:    */   };
/* 195:    */   
/* 196:    */   private static final class WeakOrderQueue
/* 197:    */   {
/* 198:225 */     static final WeakOrderQueue DUMMY = new WeakOrderQueue();
/* 199:    */     private Link head;
/* 200:    */     private Link tail;
/* 201:    */     private WeakOrderQueue next;
/* 202:    */     private final WeakReference<Thread> owner;
/* 203:    */     
/* 204:    */     private static final class Link
/* 205:    */       extends AtomicInteger
/* 206:    */     {
/* 207:230 */       private final Recycler.DefaultHandle<?>[] elements = new Recycler.DefaultHandle[Recycler.LINK_CAPACITY];
/* 208:    */       private int readIndex;
/* 209:    */       private Link next;
/* 210:    */     }
/* 211:    */     
/* 212:241 */     private final int id = Recycler.ID_GENERATOR.getAndIncrement();
/* 213:    */     private final AtomicInteger availableSharedCapacity;
/* 214:    */     
/* 215:    */     private WeakOrderQueue()
/* 216:    */     {
/* 217:245 */       this.owner = null;
/* 218:246 */       this.availableSharedCapacity = null;
/* 219:    */     }
/* 220:    */     
/* 221:    */     private WeakOrderQueue(Recycler.Stack<?> stack, Thread thread)
/* 222:    */     {
/* 223:250 */       this.head = (this.tail = new Link(null));
/* 224:251 */       this.owner = new WeakReference(thread);
/* 225:    */       
/* 226:    */ 
/* 227:    */ 
/* 228:    */ 
/* 229:256 */       this.availableSharedCapacity = stack.availableSharedCapacity;
/* 230:    */     }
/* 231:    */     
/* 232:    */     static WeakOrderQueue newQueue(Recycler.Stack<?> stack, Thread thread)
/* 233:    */     {
/* 234:260 */       WeakOrderQueue queue = new WeakOrderQueue(stack, thread);
/* 235:    */       
/* 236:    */ 
/* 237:263 */       stack.setHead(queue);
/* 238:264 */       return queue;
/* 239:    */     }
/* 240:    */     
/* 241:    */     private void setNext(WeakOrderQueue next)
/* 242:    */     {
/* 243:268 */       assert (next != this);
/* 244:269 */       this.next = next;
/* 245:    */     }
/* 246:    */     
/* 247:    */     static WeakOrderQueue allocate(Recycler.Stack<?> stack, Thread thread)
/* 248:    */     {
/* 249:277 */       return reserveSpace(stack.availableSharedCapacity, Recycler.LINK_CAPACITY) ? 
/* 250:278 */         newQueue(stack, thread) : null;
/* 251:    */     }
/* 252:    */     
/* 253:    */     private static boolean reserveSpace(AtomicInteger availableSharedCapacity, int space)
/* 254:    */     {
/* 255:282 */       assert (space >= 0);
/* 256:    */       for (;;)
/* 257:    */       {
/* 258:284 */         int available = availableSharedCapacity.get();
/* 259:285 */         if (available < space) {
/* 260:286 */           return false;
/* 261:    */         }
/* 262:288 */         if (availableSharedCapacity.compareAndSet(available, available - space)) {
/* 263:289 */           return true;
/* 264:    */         }
/* 265:    */       }
/* 266:    */     }
/* 267:    */     
/* 268:    */     private void reclaimSpace(int space)
/* 269:    */     {
/* 270:295 */       assert (space >= 0);
/* 271:296 */       this.availableSharedCapacity.addAndGet(space);
/* 272:    */     }
/* 273:    */     
/* 274:    */     void add(Recycler.DefaultHandle<?> handle)
/* 275:    */     {
/* 276:300 */       Recycler.DefaultHandle.access$1102(handle, this.id);
/* 277:    */       
/* 278:302 */       Link tail = this.tail;
/* 279:    */       int writeIndex;
/* 280:304 */       if ((writeIndex = tail.get()) == Recycler.LINK_CAPACITY)
/* 281:    */       {
/* 282:305 */         if (!reserveSpace(this.availableSharedCapacity, Recycler.LINK_CAPACITY)) {
/* 283:307 */           return;
/* 284:    */         }
/* 285:310 */         this.tail = (tail = tail.next = new Link(null));
/* 286:    */         
/* 287:312 */         writeIndex = tail.get();
/* 288:    */       }
/* 289:314 */       tail.elements[writeIndex] = handle;
/* 290:315 */       Recycler.DefaultHandle.access$502(handle, null);
/* 291:    */       
/* 292:    */ 
/* 293:318 */       tail.lazySet(writeIndex + 1);
/* 294:    */     }
/* 295:    */     
/* 296:    */     boolean hasFinalData()
/* 297:    */     {
/* 298:322 */       return this.tail.readIndex != this.tail.get();
/* 299:    */     }
/* 300:    */     
/* 301:    */     boolean transfer(Recycler.Stack<?> dst)
/* 302:    */     {
/* 303:328 */       Link head = this.head;
/* 304:329 */       if (head == null) {
/* 305:330 */         return false;
/* 306:    */       }
/* 307:333 */       if (head.readIndex == Recycler.LINK_CAPACITY)
/* 308:    */       {
/* 309:334 */         if (head.next == null) {
/* 310:335 */           return false;
/* 311:    */         }
/* 312:337 */         this.head = (head = head.next);
/* 313:    */       }
/* 314:340 */       int srcStart = head.readIndex;
/* 315:341 */       int srcEnd = head.get();
/* 316:342 */       int srcSize = srcEnd - srcStart;
/* 317:343 */       if (srcSize == 0) {
/* 318:344 */         return false;
/* 319:    */       }
/* 320:347 */       int dstSize = dst.size;
/* 321:348 */       int expectedCapacity = dstSize + srcSize;
/* 322:350 */       if (expectedCapacity > dst.elements.length)
/* 323:    */       {
/* 324:351 */         int actualCapacity = dst.increaseCapacity(expectedCapacity);
/* 325:352 */         srcEnd = Math.min(srcStart + actualCapacity - dstSize, srcEnd);
/* 326:    */       }
/* 327:355 */       if (srcStart != srcEnd)
/* 328:    */       {
/* 329:356 */         Recycler.DefaultHandle[] srcElems = head.elements;
/* 330:357 */         Recycler.DefaultHandle[] dstElems = dst.elements;
/* 331:358 */         int newDstSize = dstSize;
/* 332:359 */         for (int i = srcStart; i < srcEnd; i++)
/* 333:    */         {
/* 334:360 */           Recycler.DefaultHandle element = srcElems[i];
/* 335:361 */           if (Recycler.DefaultHandle.access$1500(element) == 0) {
/* 336:362 */             Recycler.DefaultHandle.access$1502(element, Recycler.DefaultHandle.access$1100(element));
/* 337:363 */           } else if (Recycler.DefaultHandle.access$1500(element) != Recycler.DefaultHandle.access$1100(element)) {
/* 338:364 */             throw new IllegalStateException("recycled already");
/* 339:    */           }
/* 340:366 */           srcElems[i] = null;
/* 341:368 */           if (!dst.dropHandle(element))
/* 342:    */           {
/* 343:372 */             Recycler.DefaultHandle.access$502(element, dst);
/* 344:373 */             dstElems[(newDstSize++)] = element;
/* 345:    */           }
/* 346:    */         }
/* 347:376 */         if ((srcEnd == Recycler.LINK_CAPACITY) && (head.next != null))
/* 348:    */         {
/* 349:378 */           reclaimSpace(Recycler.LINK_CAPACITY);
/* 350:    */           
/* 351:380 */           this.head = head.next;
/* 352:    */         }
/* 353:383 */         head.readIndex = srcEnd;
/* 354:384 */         if (dst.size == newDstSize) {
/* 355:385 */           return false;
/* 356:    */         }
/* 357:387 */         dst.size = newDstSize;
/* 358:388 */         return true;
/* 359:    */       }
/* 360:391 */       return false;
/* 361:    */     }
/* 362:    */     
/* 363:    */     protected void finalize()
/* 364:    */       throws Throwable
/* 365:    */     {
/* 366:    */       try
/* 367:    */       {
/* 368:398 */         super.finalize();
/* 369:    */         
/* 370:    */ 
/* 371:    */ 
/* 372:    */ 
/* 373:403 */         Link link = this.head;
/* 374:404 */         while (link != null)
/* 375:    */         {
/* 376:405 */           reclaimSpace(Recycler.LINK_CAPACITY);
/* 377:406 */           link = link.next;
/* 378:    */         }
/* 379:    */       }
/* 380:    */       finally
/* 381:    */       {
/* 382:403 */         Link link = this.head;
/* 383:404 */         while (link != null)
/* 384:    */         {
/* 385:405 */           reclaimSpace(Recycler.LINK_CAPACITY);
/* 386:406 */           link = link.next;
/* 387:    */         }
/* 388:    */       }
/* 389:    */     }
/* 390:    */   }
/* 391:    */   
/* 392:    */   static final class Stack<T>
/* 393:    */   {
/* 394:    */     final Recycler<T> parent;
/* 395:    */     final WeakReference<Thread> threadRef;
/* 396:    */     final AtomicInteger availableSharedCapacity;
/* 397:    */     final int maxDelayedQueues;
/* 398:    */     private final int maxCapacity;
/* 399:    */     private final int ratioMask;
/* 400:    */     private Recycler.DefaultHandle<?>[] elements;
/* 401:    */     private int size;
/* 402:434 */     private int handleRecycleCount = -1;
/* 403:    */     private Recycler.WeakOrderQueue cursor;
/* 404:    */     private Recycler.WeakOrderQueue prev;
/* 405:    */     private volatile Recycler.WeakOrderQueue head;
/* 406:    */     
/* 407:    */     Stack(Recycler<T> parent, Thread thread, int maxCapacity, int maxSharedCapacityFactor, int ratioMask, int maxDelayedQueues)
/* 408:    */     {
/* 409:440 */       this.parent = parent;
/* 410:441 */       this.threadRef = new WeakReference(thread);
/* 411:442 */       this.maxCapacity = maxCapacity;
/* 412:443 */       this.availableSharedCapacity = new AtomicInteger(Math.max(maxCapacity / maxSharedCapacityFactor, Recycler.LINK_CAPACITY));
/* 413:444 */       this.elements = new Recycler.DefaultHandle[Math.min(Recycler.INITIAL_CAPACITY, maxCapacity)];
/* 414:445 */       this.ratioMask = ratioMask;
/* 415:446 */       this.maxDelayedQueues = maxDelayedQueues;
/* 416:    */     }
/* 417:    */     
/* 418:    */     synchronized void setHead(Recycler.WeakOrderQueue queue)
/* 419:    */     {
/* 420:451 */       Recycler.WeakOrderQueue.access$1700(queue, this.head);
/* 421:452 */       this.head = queue;
/* 422:    */     }
/* 423:    */     
/* 424:    */     int increaseCapacity(int expectedCapacity)
/* 425:    */     {
/* 426:456 */       int newCapacity = this.elements.length;
/* 427:457 */       int maxCapacity = this.maxCapacity;
/* 428:    */       do
/* 429:    */       {
/* 430:459 */         newCapacity <<= 1;
/* 431:460 */       } while ((newCapacity < expectedCapacity) && (newCapacity < maxCapacity));
/* 432:462 */       newCapacity = Math.min(newCapacity, maxCapacity);
/* 433:463 */       if (newCapacity != this.elements.length) {
/* 434:464 */         this.elements = ((Recycler.DefaultHandle[])Arrays.copyOf(this.elements, newCapacity));
/* 435:    */       }
/* 436:467 */       return newCapacity;
/* 437:    */     }
/* 438:    */     
/* 439:    */     Recycler.DefaultHandle<T> pop()
/* 440:    */     {
/* 441:472 */       int size = this.size;
/* 442:473 */       if (size == 0)
/* 443:    */       {
/* 444:474 */         if (!scavenge()) {
/* 445:475 */           return null;
/* 446:    */         }
/* 447:477 */         size = this.size;
/* 448:    */       }
/* 449:479 */       size--;
/* 450:480 */       Recycler.DefaultHandle ret = this.elements[size];
/* 451:481 */       this.elements[size] = null;
/* 452:482 */       if (Recycler.DefaultHandle.access$1100(ret) != Recycler.DefaultHandle.access$1500(ret)) {
/* 453:483 */         throw new IllegalStateException("recycled multiple times");
/* 454:    */       }
/* 455:485 */       Recycler.DefaultHandle.access$1502(ret, 0);
/* 456:486 */       Recycler.DefaultHandle.access$1102(ret, 0);
/* 457:487 */       this.size = size;
/* 458:488 */       return ret;
/* 459:    */     }
/* 460:    */     
/* 461:    */     boolean scavenge()
/* 462:    */     {
/* 463:493 */       if (scavengeSome()) {
/* 464:494 */         return true;
/* 465:    */       }
/* 466:498 */       this.prev = null;
/* 467:499 */       this.cursor = this.head;
/* 468:500 */       return false;
/* 469:    */     }
/* 470:    */     
/* 471:    */     boolean scavengeSome()
/* 472:    */     {
/* 473:505 */       Recycler.WeakOrderQueue cursor = this.cursor;
/* 474:    */       Recycler.WeakOrderQueue prev;
/* 475:506 */       if (cursor == null)
/* 476:    */       {
/* 477:507 */         Recycler.WeakOrderQueue prev = null;
/* 478:508 */         cursor = this.head;
/* 479:509 */         if (cursor == null) {
/* 480:510 */           return false;
/* 481:    */         }
/* 482:    */       }
/* 483:    */       else
/* 484:    */       {
/* 485:513 */         prev = this.prev;
/* 486:    */       }
/* 487:516 */       boolean success = false;
/* 488:    */       do
/* 489:    */       {
/* 490:518 */         if (cursor.transfer(this))
/* 491:    */         {
/* 492:519 */           success = true;
/* 493:520 */           break;
/* 494:    */         }
/* 495:522 */         Recycler.WeakOrderQueue next = Recycler.WeakOrderQueue.access$1800(cursor);
/* 496:523 */         if (Recycler.WeakOrderQueue.access$1900(cursor).get() == null)
/* 497:    */         {
/* 498:527 */           if (cursor.hasFinalData()) {
/* 499:529 */             while (cursor.transfer(this)) {
/* 500:530 */               success = true;
/* 501:    */             }
/* 502:    */           }
/* 503:537 */           if (prev != null) {
/* 504:538 */             Recycler.WeakOrderQueue.access$1700(prev, next);
/* 505:    */           }
/* 506:    */         }
/* 507:    */         else
/* 508:    */         {
/* 509:541 */           prev = cursor;
/* 510:    */         }
/* 511:544 */         cursor = next;
/* 512:546 */       } while ((cursor != null) && (!success));
/* 513:548 */       this.prev = prev;
/* 514:549 */       this.cursor = cursor;
/* 515:550 */       return success;
/* 516:    */     }
/* 517:    */     
/* 518:    */     void push(Recycler.DefaultHandle<?> item)
/* 519:    */     {
/* 520:554 */       Thread currentThread = Thread.currentThread();
/* 521:555 */       if (this.threadRef.get() == currentThread) {
/* 522:557 */         pushNow(item);
/* 523:    */       } else {
/* 524:562 */         pushLater(item, currentThread);
/* 525:    */       }
/* 526:    */     }
/* 527:    */     
/* 528:    */     private void pushNow(Recycler.DefaultHandle<?> item)
/* 529:    */     {
/* 530:567 */       if ((Recycler.DefaultHandle.access$1500(item) | Recycler.DefaultHandle.access$1100(item)) != 0) {
/* 531:568 */         throw new IllegalStateException("recycled already");
/* 532:    */       }
/* 533:570 */       Recycler.DefaultHandle.access$1502(item, Recycler.DefaultHandle.access$1102(item, Recycler.OWN_THREAD_ID));
/* 534:    */       
/* 535:572 */       int size = this.size;
/* 536:573 */       if ((size >= this.maxCapacity) || (dropHandle(item))) {
/* 537:575 */         return;
/* 538:    */       }
/* 539:577 */       if (size == this.elements.length) {
/* 540:578 */         this.elements = ((Recycler.DefaultHandle[])Arrays.copyOf(this.elements, Math.min(size << 1, this.maxCapacity)));
/* 541:    */       }
/* 542:581 */       this.elements[size] = item;
/* 543:582 */       this.size = (size + 1);
/* 544:    */     }
/* 545:    */     
/* 546:    */     private void pushLater(Recycler.DefaultHandle<?> item, Thread thread)
/* 547:    */     {
/* 548:589 */       Map<Stack<?>, Recycler.WeakOrderQueue> delayedRecycled = (Map)Recycler.DELAYED_RECYCLED.get();
/* 549:590 */       Recycler.WeakOrderQueue queue = (Recycler.WeakOrderQueue)delayedRecycled.get(this);
/* 550:591 */       if (queue == null)
/* 551:    */       {
/* 552:592 */         if (delayedRecycled.size() >= this.maxDelayedQueues)
/* 553:    */         {
/* 554:594 */           delayedRecycled.put(this, Recycler.WeakOrderQueue.DUMMY);
/* 555:595 */           return;
/* 556:    */         }
/* 557:598 */         if ((queue = Recycler.WeakOrderQueue.allocate(this, thread)) == null) {
/* 558:600 */           return;
/* 559:    */         }
/* 560:602 */         delayedRecycled.put(this, queue);
/* 561:    */       }
/* 562:603 */       else if (queue == Recycler.WeakOrderQueue.DUMMY)
/* 563:    */       {
/* 564:605 */         return;
/* 565:    */       }
/* 566:608 */       queue.add(item);
/* 567:    */     }
/* 568:    */     
/* 569:    */     boolean dropHandle(Recycler.DefaultHandle<?> handle)
/* 570:    */     {
/* 571:612 */       if (!handle.hasBeenRecycled)
/* 572:    */       {
/* 573:613 */         if ((++this.handleRecycleCount & this.ratioMask) != 0) {
/* 574:615 */           return true;
/* 575:    */         }
/* 576:617 */         handle.hasBeenRecycled = true;
/* 577:    */       }
/* 578:619 */       return false;
/* 579:    */     }
/* 580:    */     
/* 581:    */     Recycler.DefaultHandle<T> newHandle()
/* 582:    */     {
/* 583:623 */       return new Recycler.DefaultHandle(this);
/* 584:    */     }
/* 585:    */   }
/* 586:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.Recycler
 * JD-Core Version:    0.7.0.1
 */