/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.StringUtil;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.util.Collections;
/*   8:    */ import java.util.HashSet;
/*   9:    */ import java.util.Queue;
/*  10:    */ import java.util.Set;
/*  11:    */ import java.util.concurrent.CountDownLatch;
/*  12:    */ import java.util.concurrent.Executors;
/*  13:    */ import java.util.concurrent.RejectedExecutionException;
/*  14:    */ import java.util.concurrent.ThreadFactory;
/*  15:    */ import java.util.concurrent.TimeUnit;
/*  16:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  17:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  18:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  19:    */ import java.util.concurrent.atomic.AtomicLong;
/*  20:    */ 
/*  21:    */ public class HashedWheelTimer
/*  22:    */   implements Timer
/*  23:    */ {
/*  24: 82 */   static final InternalLogger logger = InternalLoggerFactory.getInstance(HashedWheelTimer.class);
/*  25: 84 */   private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
/*  26: 85 */   private static final AtomicBoolean WARNED_TOO_MANY_INSTANCES = new AtomicBoolean();
/*  27:    */   private static final int INSTANCE_COUNT_LIMIT = 64;
/*  28: 87 */   private static final ResourceLeakDetector<HashedWheelTimer> leakDetector = ResourceLeakDetectorFactory.instance()
/*  29: 88 */     .newResourceLeakDetector(HashedWheelTimer.class, 1);
/*  30: 91 */   private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");
/*  31:    */   private final ResourceLeakTracker<HashedWheelTimer> leak;
/*  32: 94 */   private final Worker worker = new Worker(null);
/*  33:    */   private final Thread workerThread;
/*  34:    */   public static final int WORKER_STATE_INIT = 0;
/*  35:    */   public static final int WORKER_STATE_STARTED = 1;
/*  36:    */   public static final int WORKER_STATE_SHUTDOWN = 2;
/*  37:    */   private volatile int workerState;
/*  38:    */   private final long tickDuration;
/*  39:    */   private final HashedWheelBucket[] wheel;
/*  40:    */   private final int mask;
/*  41:106 */   private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
/*  42:107 */   private final Queue<HashedWheelTimeout> timeouts = PlatformDependent.newMpscQueue();
/*  43:108 */   private final Queue<HashedWheelTimeout> cancelledTimeouts = PlatformDependent.newMpscQueue();
/*  44:109 */   private final AtomicLong pendingTimeouts = new AtomicLong(0L);
/*  45:    */   private final long maxPendingTimeouts;
/*  46:    */   private volatile long startTime;
/*  47:    */   
/*  48:    */   public HashedWheelTimer()
/*  49:    */   {
/*  50:120 */     this(Executors.defaultThreadFactory());
/*  51:    */   }
/*  52:    */   
/*  53:    */   public HashedWheelTimer(long tickDuration, TimeUnit unit)
/*  54:    */   {
/*  55:134 */     this(Executors.defaultThreadFactory(), tickDuration, unit);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel)
/*  59:    */   {
/*  60:148 */     this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public HashedWheelTimer(ThreadFactory threadFactory)
/*  64:    */   {
/*  65:161 */     this(threadFactory, 100L, TimeUnit.MILLISECONDS);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit)
/*  69:    */   {
/*  70:177 */     this(threadFactory, tickDuration, unit, 512);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel)
/*  74:    */   {
/*  75:195 */     this(threadFactory, tickDuration, unit, ticksPerWheel, true);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection)
/*  79:    */   {
/*  80:216 */     this(threadFactory, tickDuration, unit, ticksPerWheel, leakDetection, -1L);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection, long maxPendingTimeouts)
/*  84:    */   {
/*  85:244 */     if (threadFactory == null) {
/*  86:245 */       throw new NullPointerException("threadFactory");
/*  87:    */     }
/*  88:247 */     if (unit == null) {
/*  89:248 */       throw new NullPointerException("unit");
/*  90:    */     }
/*  91:250 */     if (tickDuration <= 0L) {
/*  92:251 */       throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
/*  93:    */     }
/*  94:253 */     if (ticksPerWheel <= 0) {
/*  95:254 */       throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
/*  96:    */     }
/*  97:258 */     this.wheel = createWheel(ticksPerWheel);
/*  98:259 */     this.mask = (this.wheel.length - 1);
/*  99:    */     
/* 100:    */ 
/* 101:262 */     this.tickDuration = unit.toNanos(tickDuration);
/* 102:265 */     if (this.tickDuration >= 9223372036854775807L / this.wheel.length) {
/* 103:266 */       throw new IllegalArgumentException(String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", new Object[] {
/* 104:    */       
/* 105:268 */         Long.valueOf(tickDuration), Long.valueOf(9223372036854775807L / this.wheel.length) }));
/* 106:    */     }
/* 107:270 */     this.workerThread = threadFactory.newThread(this.worker);
/* 108:    */     
/* 109:272 */     this.leak = ((leakDetection) || (!this.workerThread.isDaemon()) ? leakDetector.track(this) : null);
/* 110:    */     
/* 111:274 */     this.maxPendingTimeouts = maxPendingTimeouts;
/* 112:276 */     if ((INSTANCE_COUNTER.incrementAndGet() > 64) && 
/* 113:277 */       (WARNED_TOO_MANY_INSTANCES.compareAndSet(false, true))) {
/* 114:278 */       reportTooManyInstances();
/* 115:    */     }
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected void finalize()
/* 119:    */     throws Throwable
/* 120:    */   {
/* 121:    */     try
/* 122:    */     {
/* 123:285 */       super.finalize();
/* 124:289 */       if (WORKER_STATE_UPDATER.getAndSet(this, 2) != 2) {
/* 125:290 */         INSTANCE_COUNTER.decrementAndGet();
/* 126:    */       }
/* 127:    */     }
/* 128:    */     finally
/* 129:    */     {
/* 130:289 */       if (WORKER_STATE_UPDATER.getAndSet(this, 2) != 2) {
/* 131:290 */         INSTANCE_COUNTER.decrementAndGet();
/* 132:    */       }
/* 133:    */     }
/* 134:    */   }
/* 135:    */   
/* 136:    */   private static HashedWheelBucket[] createWheel(int ticksPerWheel)
/* 137:    */   {
/* 138:296 */     if (ticksPerWheel <= 0) {
/* 139:297 */       throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
/* 140:    */     }
/* 141:300 */     if (ticksPerWheel > 1073741824) {
/* 142:301 */       throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
/* 143:    */     }
/* 144:305 */     ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
/* 145:306 */     HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
/* 146:307 */     for (int i = 0; i < wheel.length; i++) {
/* 147:308 */       wheel[i] = new HashedWheelBucket(null);
/* 148:    */     }
/* 149:310 */     return wheel;
/* 150:    */   }
/* 151:    */   
/* 152:    */   private static int normalizeTicksPerWheel(int ticksPerWheel)
/* 153:    */   {
/* 154:314 */     int normalizedTicksPerWheel = 1;
/* 155:315 */     while (normalizedTicksPerWheel < ticksPerWheel) {
/* 156:316 */       normalizedTicksPerWheel <<= 1;
/* 157:    */     }
/* 158:318 */     return normalizedTicksPerWheel;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public void start()
/* 162:    */   {
/* 163:329 */     switch (WORKER_STATE_UPDATER.get(this))
/* 164:    */     {
/* 165:    */     case 0: 
/* 166:331 */       if (WORKER_STATE_UPDATER.compareAndSet(this, 0, 1)) {
/* 167:332 */         this.workerThread.start();
/* 168:    */       }
/* 169:    */       break;
/* 170:    */     case 1: 
/* 171:    */       break;
/* 172:    */     case 2: 
/* 173:338 */       throw new IllegalStateException("cannot be started once stopped");
/* 174:    */     default: 
/* 175:340 */       throw new Error("Invalid WorkerState");
/* 176:    */     }
/* 177:344 */     while (this.startTime == 0L) {
/* 178:    */       try
/* 179:    */       {
/* 180:346 */         this.startTimeInitialized.await();
/* 181:    */       }
/* 182:    */       catch (InterruptedException localInterruptedException) {}
/* 183:    */     }
/* 184:    */   }
/* 185:    */   
/* 186:    */   public Set<Timeout> stop()
/* 187:    */   {
/* 188:355 */     if (Thread.currentThread() == this.workerThread) {
/* 189:359 */       throw new IllegalStateException(HashedWheelTimer.class.getSimpleName() + ".stop() cannot be called from " + TimerTask.class.getSimpleName());
/* 190:    */     }
/* 191:362 */     if (!WORKER_STATE_UPDATER.compareAndSet(this, 1, 2))
/* 192:    */     {
/* 193:364 */       if (WORKER_STATE_UPDATER.getAndSet(this, 2) != 2)
/* 194:    */       {
/* 195:365 */         INSTANCE_COUNTER.decrementAndGet();
/* 196:366 */         if (this.leak != null)
/* 197:    */         {
/* 198:367 */           boolean closed = this.leak.close(this);
/* 199:368 */           assert (closed);
/* 200:    */         }
/* 201:    */       }
/* 202:372 */       return Collections.emptySet();
/* 203:    */     }
/* 204:    */     try
/* 205:    */     {
/* 206:376 */       boolean interrupted = false;
/* 207:377 */       while (this.workerThread.isAlive())
/* 208:    */       {
/* 209:378 */         this.workerThread.interrupt();
/* 210:    */         try
/* 211:    */         {
/* 212:380 */           this.workerThread.join(100L);
/* 213:    */         }
/* 214:    */         catch (InterruptedException ignored)
/* 215:    */         {
/* 216:382 */           interrupted = true;
/* 217:    */         }
/* 218:    */       }
/* 219:386 */       if (interrupted) {
/* 220:387 */         Thread.currentThread().interrupt();
/* 221:    */       }
/* 222:390 */       INSTANCE_COUNTER.decrementAndGet();
/* 223:391 */       if (this.leak != null)
/* 224:    */       {
/* 225:392 */         boolean closed = this.leak.close(this);
/* 226:393 */         if ((!$assertionsDisabled) && (!closed)) {
/* 227:393 */           throw new AssertionError();
/* 228:    */         }
/* 229:    */       }
/* 230:    */     }
/* 231:    */     finally
/* 232:    */     {
/* 233:390 */       INSTANCE_COUNTER.decrementAndGet();
/* 234:391 */       if (this.leak != null)
/* 235:    */       {
/* 236:392 */         boolean closed = this.leak.close(this);
/* 237:393 */         if ((!$assertionsDisabled) && (!closed)) {
/* 238:393 */           throw new AssertionError();
/* 239:    */         }
/* 240:    */       }
/* 241:    */     }
/* 242:396 */     return this.worker.unprocessedTimeouts();
/* 243:    */   }
/* 244:    */   
/* 245:    */   public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit)
/* 246:    */   {
/* 247:401 */     if (task == null) {
/* 248:402 */       throw new NullPointerException("task");
/* 249:    */     }
/* 250:404 */     if (unit == null) {
/* 251:405 */       throw new NullPointerException("unit");
/* 252:    */     }
/* 253:408 */     long pendingTimeoutsCount = this.pendingTimeouts.incrementAndGet();
/* 254:410 */     if ((this.maxPendingTimeouts > 0L) && (pendingTimeoutsCount > this.maxPendingTimeouts))
/* 255:    */     {
/* 256:411 */       this.pendingTimeouts.decrementAndGet();
/* 257:412 */       throw new RejectedExecutionException("Number of pending timeouts (" + pendingTimeoutsCount + ") is greater than or equal to maximum allowed pending timeouts (" + this.maxPendingTimeouts + ")");
/* 258:    */     }
/* 259:417 */     start();
/* 260:    */     
/* 261:    */ 
/* 262:    */ 
/* 263:421 */     long deadline = System.nanoTime() + unit.toNanos(delay) - this.startTime;
/* 264:422 */     HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
/* 265:423 */     this.timeouts.add(timeout);
/* 266:424 */     return timeout;
/* 267:    */   }
/* 268:    */   
/* 269:    */   public long pendingTimeouts()
/* 270:    */   {
/* 271:431 */     return this.pendingTimeouts.get();
/* 272:    */   }
/* 273:    */   
/* 274:    */   private static void reportTooManyInstances()
/* 275:    */   {
/* 276:435 */     String resourceType = StringUtil.simpleClassName(HashedWheelTimer.class);
/* 277:436 */     logger.error("You are creating too many " + resourceType + " instances. " + resourceType + " is a shared resource that must be reused across the JVM,so that only a few instances are created.");
/* 278:    */   }
/* 279:    */   
/* 280:    */   private final class Worker
/* 281:    */     implements Runnable
/* 282:    */   {
/* 283:442 */     private final Set<Timeout> unprocessedTimeouts = new HashSet();
/* 284:    */     private long tick;
/* 285:    */     
/* 286:    */     private Worker() {}
/* 287:    */     
/* 288:    */     public void run()
/* 289:    */     {
/* 290:449 */       HashedWheelTimer.this.startTime = System.nanoTime();
/* 291:450 */       if (HashedWheelTimer.this.startTime == 0L) {
/* 292:452 */         HashedWheelTimer.this.startTime = 1L;
/* 293:    */       }
/* 294:456 */       HashedWheelTimer.this.startTimeInitialized.countDown();
/* 295:    */       long deadline;
/* 296:    */       int idx;
/* 297:    */       do
/* 298:    */       {
/* 299:459 */         deadline = waitForNextTick();
/* 300:460 */         if (deadline > 0L)
/* 301:    */         {
/* 302:461 */           idx = (int)(this.tick & HashedWheelTimer.this.mask);
/* 303:462 */           processCancelledTasks();
/* 304:    */           
/* 305:464 */           HashedWheelTimer.HashedWheelBucket bucket = HashedWheelTimer.this.wheel[idx];
/* 306:465 */           transferTimeoutsToBuckets();
/* 307:466 */           bucket.expireTimeouts(deadline);
/* 308:467 */           this.tick += 1L;
/* 309:    */         }
/* 310:469 */       } while (HashedWheelTimer.WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == 1);
/* 311:472 */       for (HashedWheelTimer.HashedWheelBucket bucket : HashedWheelTimer.this.wheel) {
/* 312:473 */         bucket.clearTimeouts(this.unprocessedTimeouts);
/* 313:    */       }
/* 314:    */       for (;;)
/* 315:    */       {
/* 316:476 */         HashedWheelTimer.HashedWheelTimeout timeout = (HashedWheelTimer.HashedWheelTimeout)HashedWheelTimer.this.timeouts.poll();
/* 317:477 */         if (timeout == null) {
/* 318:    */           break;
/* 319:    */         }
/* 320:480 */         if (!timeout.isCancelled()) {
/* 321:481 */           this.unprocessedTimeouts.add(timeout);
/* 322:    */         }
/* 323:    */       }
/* 324:484 */       processCancelledTasks();
/* 325:    */     }
/* 326:    */     
/* 327:    */     private void transferTimeoutsToBuckets()
/* 328:    */     {
/* 329:490 */       for (int i = 0; i < 100000; i++)
/* 330:    */       {
/* 331:491 */         HashedWheelTimer.HashedWheelTimeout timeout = (HashedWheelTimer.HashedWheelTimeout)HashedWheelTimer.this.timeouts.poll();
/* 332:492 */         if (timeout == null) {
/* 333:    */           break;
/* 334:    */         }
/* 335:496 */         if (timeout.state() != 1)
/* 336:    */         {
/* 337:501 */           long calculated = timeout.deadline / HashedWheelTimer.this.tickDuration;
/* 338:502 */           timeout.remainingRounds = ((calculated - this.tick) / HashedWheelTimer.this.wheel.length);
/* 339:    */           
/* 340:504 */           long ticks = Math.max(calculated, this.tick);
/* 341:505 */           int stopIndex = (int)(ticks & HashedWheelTimer.this.mask);
/* 342:    */           
/* 343:507 */           HashedWheelTimer.HashedWheelBucket bucket = HashedWheelTimer.this.wheel[stopIndex];
/* 344:508 */           bucket.addTimeout(timeout);
/* 345:    */         }
/* 346:    */       }
/* 347:    */     }
/* 348:    */     
/* 349:    */     private void processCancelledTasks()
/* 350:    */     {
/* 351:    */       for (;;)
/* 352:    */       {
/* 353:514 */         HashedWheelTimer.HashedWheelTimeout timeout = (HashedWheelTimer.HashedWheelTimeout)HashedWheelTimer.this.cancelledTimeouts.poll();
/* 354:515 */         if (timeout == null) {
/* 355:    */           break;
/* 356:    */         }
/* 357:    */         try
/* 358:    */         {
/* 359:520 */           timeout.remove();
/* 360:    */         }
/* 361:    */         catch (Throwable t)
/* 362:    */         {
/* 363:522 */           if (HashedWheelTimer.logger.isWarnEnabled()) {
/* 364:523 */             HashedWheelTimer.logger.warn("An exception was thrown while process a cancellation task", t);
/* 365:    */           }
/* 366:    */         }
/* 367:    */       }
/* 368:    */     }
/* 369:    */     
/* 370:    */     private long waitForNextTick()
/* 371:    */     {
/* 372:536 */       long deadline = HashedWheelTimer.this.tickDuration * (this.tick + 1L);
/* 373:    */       for (;;)
/* 374:    */       {
/* 375:539 */         long currentTime = System.nanoTime() - HashedWheelTimer.this.startTime;
/* 376:540 */         long sleepTimeMs = (deadline - currentTime + 999999L) / 1000000L;
/* 377:542 */         if (sleepTimeMs <= 0L)
/* 378:    */         {
/* 379:543 */           if (currentTime == -9223372036854775808L) {
/* 380:544 */             return -9223372036854775807L;
/* 381:    */           }
/* 382:546 */           return currentTime;
/* 383:    */         }
/* 384:555 */         if (PlatformDependent.isWindows()) {
/* 385:556 */           sleepTimeMs = sleepTimeMs / 10L * 10L;
/* 386:    */         }
/* 387:    */         try
/* 388:    */         {
/* 389:560 */           Thread.sleep(sleepTimeMs);
/* 390:    */         }
/* 391:    */         catch (InterruptedException ignored)
/* 392:    */         {
/* 393:562 */           if (HashedWheelTimer.WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == 2) {
/* 394:563 */             return -9223372036854775808L;
/* 395:    */           }
/* 396:    */         }
/* 397:    */       }
/* 398:    */     }
/* 399:    */     
/* 400:    */     public Set<Timeout> unprocessedTimeouts()
/* 401:    */     {
/* 402:570 */       return Collections.unmodifiableSet(this.unprocessedTimeouts);
/* 403:    */     }
/* 404:    */   }
/* 405:    */   
/* 406:    */   private static final class HashedWheelTimeout
/* 407:    */     implements Timeout
/* 408:    */   {
/* 409:    */     private static final int ST_INIT = 0;
/* 410:    */     private static final int ST_CANCELLED = 1;
/* 411:    */     private static final int ST_EXPIRED = 2;
/* 412:580 */     private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");
/* 413:    */     private final HashedWheelTimer timer;
/* 414:    */     private final TimerTask task;
/* 415:    */     private final long deadline;
/* 416:586 */     private volatile int state = 0;
/* 417:    */     long remainingRounds;
/* 418:    */     HashedWheelTimeout next;
/* 419:    */     HashedWheelTimeout prev;
/* 420:    */     HashedWheelTimer.HashedWheelBucket bucket;
/* 421:    */     
/* 422:    */     HashedWheelTimeout(HashedWheelTimer timer, TimerTask task, long deadline)
/* 423:    */     {
/* 424:602 */       this.timer = timer;
/* 425:603 */       this.task = task;
/* 426:604 */       this.deadline = deadline;
/* 427:    */     }
/* 428:    */     
/* 429:    */     public Timer timer()
/* 430:    */     {
/* 431:609 */       return this.timer;
/* 432:    */     }
/* 433:    */     
/* 434:    */     public TimerTask task()
/* 435:    */     {
/* 436:614 */       return this.task;
/* 437:    */     }
/* 438:    */     
/* 439:    */     public boolean cancel()
/* 440:    */     {
/* 441:620 */       if (!compareAndSetState(0, 1)) {
/* 442:621 */         return false;
/* 443:    */       }
/* 444:626 */       this.timer.cancelledTimeouts.add(this);
/* 445:627 */       return true;
/* 446:    */     }
/* 447:    */     
/* 448:    */     void remove()
/* 449:    */     {
/* 450:631 */       HashedWheelTimer.HashedWheelBucket bucket = this.bucket;
/* 451:632 */       if (bucket != null) {
/* 452:633 */         bucket.remove(this);
/* 453:    */       } else {
/* 454:635 */         this.timer.pendingTimeouts.decrementAndGet();
/* 455:    */       }
/* 456:    */     }
/* 457:    */     
/* 458:    */     public boolean compareAndSetState(int expected, int state)
/* 459:    */     {
/* 460:640 */       return STATE_UPDATER.compareAndSet(this, expected, state);
/* 461:    */     }
/* 462:    */     
/* 463:    */     public int state()
/* 464:    */     {
/* 465:644 */       return this.state;
/* 466:    */     }
/* 467:    */     
/* 468:    */     public boolean isCancelled()
/* 469:    */     {
/* 470:649 */       return state() == 1;
/* 471:    */     }
/* 472:    */     
/* 473:    */     public boolean isExpired()
/* 474:    */     {
/* 475:654 */       return state() == 2;
/* 476:    */     }
/* 477:    */     
/* 478:    */     public void expire()
/* 479:    */     {
/* 480:658 */       if (!compareAndSetState(0, 2)) {
/* 481:659 */         return;
/* 482:    */       }
/* 483:    */       try
/* 484:    */       {
/* 485:663 */         this.task.run(this);
/* 486:    */       }
/* 487:    */       catch (Throwable t)
/* 488:    */       {
/* 489:665 */         if (HashedWheelTimer.logger.isWarnEnabled()) {
/* 490:666 */           HashedWheelTimer.logger.warn("An exception was thrown by " + TimerTask.class.getSimpleName() + '.', t);
/* 491:    */         }
/* 492:    */       }
/* 493:    */     }
/* 494:    */     
/* 495:    */     public String toString()
/* 496:    */     {
/* 497:673 */       long currentTime = System.nanoTime();
/* 498:674 */       long remaining = this.deadline - currentTime + this.timer.startTime;
/* 499:    */       
/* 500:    */ 
/* 501:    */ 
/* 502:    */ 
/* 503:679 */       StringBuilder buf = new StringBuilder(192).append(StringUtil.simpleClassName(this)).append('(').append("deadline: ");
/* 504:680 */       if (remaining > 0L) {
/* 505:682 */         buf.append(remaining).append(" ns later");
/* 506:683 */       } else if (remaining < 0L) {
/* 507:685 */         buf.append(-remaining).append(" ns ago");
/* 508:    */       } else {
/* 509:687 */         buf.append("now");
/* 510:    */       }
/* 511:690 */       if (isCancelled()) {
/* 512:691 */         buf.append(", cancelled");
/* 513:    */       }
/* 514:694 */       return 
/* 515:    */       
/* 516:696 */         ", task: " + task() + ')';
/* 517:    */     }
/* 518:    */   }
/* 519:    */   
/* 520:    */   private static final class HashedWheelBucket
/* 521:    */   {
/* 522:    */     private HashedWheelTimer.HashedWheelTimeout head;
/* 523:    */     private HashedWheelTimer.HashedWheelTimeout tail;
/* 524:    */     
/* 525:    */     public void addTimeout(HashedWheelTimer.HashedWheelTimeout timeout)
/* 526:    */     {
/* 527:715 */       assert (timeout.bucket == null);
/* 528:716 */       timeout.bucket = this;
/* 529:717 */       if (this.head == null)
/* 530:    */       {
/* 531:718 */         this.head = (this.tail = timeout);
/* 532:    */       }
/* 533:    */       else
/* 534:    */       {
/* 535:720 */         this.tail.next = timeout;
/* 536:721 */         timeout.prev = this.tail;
/* 537:722 */         this.tail = timeout;
/* 538:    */       }
/* 539:    */     }
/* 540:    */     
/* 541:    */     public void expireTimeouts(long deadline)
/* 542:    */     {
/* 543:730 */       HashedWheelTimer.HashedWheelTimeout timeout = this.head;
/* 544:733 */       while (timeout != null)
/* 545:    */       {
/* 546:734 */         HashedWheelTimer.HashedWheelTimeout next = timeout.next;
/* 547:735 */         if (timeout.remainingRounds <= 0L)
/* 548:    */         {
/* 549:736 */           next = remove(timeout);
/* 550:737 */           if (HashedWheelTimer.HashedWheelTimeout.access$800(timeout) <= deadline) {
/* 551:738 */             timeout.expire();
/* 552:    */           } else {
/* 553:741 */             throw new IllegalStateException(String.format("timeout.deadline (%d) > deadline (%d)", new Object[] {
/* 554:742 */               Long.valueOf(HashedWheelTimer.HashedWheelTimeout.access$800(timeout)), Long.valueOf(deadline) }));
/* 555:    */           }
/* 556:    */         }
/* 557:744 */         else if (timeout.isCancelled())
/* 558:    */         {
/* 559:745 */           next = remove(timeout);
/* 560:    */         }
/* 561:    */         else
/* 562:    */         {
/* 563:747 */           timeout.remainingRounds -= 1L;
/* 564:    */         }
/* 565:749 */         timeout = next;
/* 566:    */       }
/* 567:    */     }
/* 568:    */     
/* 569:    */     public HashedWheelTimer.HashedWheelTimeout remove(HashedWheelTimer.HashedWheelTimeout timeout)
/* 570:    */     {
/* 571:754 */       HashedWheelTimer.HashedWheelTimeout next = timeout.next;
/* 572:756 */       if (timeout.prev != null) {
/* 573:757 */         timeout.prev.next = next;
/* 574:    */       }
/* 575:759 */       if (timeout.next != null) {
/* 576:760 */         timeout.next.prev = timeout.prev;
/* 577:    */       }
/* 578:763 */       if (timeout == this.head)
/* 579:    */       {
/* 580:765 */         if (timeout == this.tail)
/* 581:    */         {
/* 582:766 */           this.tail = null;
/* 583:767 */           this.head = null;
/* 584:    */         }
/* 585:    */         else
/* 586:    */         {
/* 587:769 */           this.head = next;
/* 588:    */         }
/* 589:    */       }
/* 590:771 */       else if (timeout == this.tail) {
/* 591:773 */         this.tail = timeout.prev;
/* 592:    */       }
/* 593:776 */       timeout.prev = null;
/* 594:777 */       timeout.next = null;
/* 595:778 */       timeout.bucket = null;
/* 596:779 */       HashedWheelTimer.HashedWheelTimeout.access$1200(timeout).pendingTimeouts.decrementAndGet();
/* 597:780 */       return next;
/* 598:    */     }
/* 599:    */     
/* 600:    */     public void clearTimeouts(Set<Timeout> set)
/* 601:    */     {
/* 602:    */       for (;;)
/* 603:    */       {
/* 604:788 */         HashedWheelTimer.HashedWheelTimeout timeout = pollTimeout();
/* 605:789 */         if (timeout == null) {
/* 606:790 */           return;
/* 607:    */         }
/* 608:792 */         if ((!timeout.isExpired()) && (!timeout.isCancelled())) {
/* 609:795 */           set.add(timeout);
/* 610:    */         }
/* 611:    */       }
/* 612:    */     }
/* 613:    */     
/* 614:    */     private HashedWheelTimer.HashedWheelTimeout pollTimeout()
/* 615:    */     {
/* 616:800 */       HashedWheelTimer.HashedWheelTimeout head = this.head;
/* 617:801 */       if (head == null) {
/* 618:802 */         return null;
/* 619:    */       }
/* 620:804 */       HashedWheelTimer.HashedWheelTimeout next = head.next;
/* 621:805 */       if (next == null)
/* 622:    */       {
/* 623:806 */         this.tail = (this.head = null);
/* 624:    */       }
/* 625:    */       else
/* 626:    */       {
/* 627:808 */         this.head = next;
/* 628:809 */         next.prev = null;
/* 629:    */       }
/* 630:813 */       head.next = null;
/* 631:814 */       head.prev = null;
/* 632:815 */       head.bucket = null;
/* 633:816 */       return head;
/* 634:    */     }
/* 635:    */   }
/* 636:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.HashedWheelTimer
 * JD-Core Version:    0.7.0.1
 */