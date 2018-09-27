/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.PriorityQueue;
/*   6:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.util.ArrayList;
/*  10:    */ import java.util.Collection;
/*  11:    */ import java.util.LinkedHashSet;
/*  12:    */ import java.util.List;
/*  13:    */ import java.util.Queue;
/*  14:    */ import java.util.Set;
/*  15:    */ import java.util.concurrent.BlockingQueue;
/*  16:    */ import java.util.concurrent.Callable;
/*  17:    */ import java.util.concurrent.ExecutionException;
/*  18:    */ import java.util.concurrent.Executor;
/*  19:    */ import java.util.concurrent.LinkedBlockingQueue;
/*  20:    */ import java.util.concurrent.RejectedExecutionException;
/*  21:    */ import java.util.concurrent.Semaphore;
/*  22:    */ import java.util.concurrent.ThreadFactory;
/*  23:    */ import java.util.concurrent.TimeUnit;
/*  24:    */ import java.util.concurrent.TimeoutException;
/*  25:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  26:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  27:    */ 
/*  28:    */ public abstract class SingleThreadEventExecutor
/*  29:    */   extends AbstractScheduledEventExecutor
/*  30:    */   implements OrderedEventExecutor
/*  31:    */ {
/*  32: 51 */   static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16, 
/*  33: 52 */     SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", 2147483647));
/*  34: 55 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SingleThreadEventExecutor.class);
/*  35:    */   private static final int ST_NOT_STARTED = 1;
/*  36:    */   private static final int ST_STARTED = 2;
/*  37:    */   private static final int ST_SHUTTING_DOWN = 3;
/*  38:    */   private static final int ST_SHUTDOWN = 4;
/*  39:    */   private static final int ST_TERMINATED = 5;
/*  40: 63 */   private static final Runnable WAKEUP_TASK = new Runnable()
/*  41:    */   {
/*  42:    */     public void run() {}
/*  43:    */   };
/*  44: 69 */   private static final Runnable NOOP_TASK = new Runnable()
/*  45:    */   {
/*  46:    */     public void run() {}
/*  47:    */   };
/*  48: 77 */   private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");
/*  49: 79 */   private static final AtomicReferenceFieldUpdater<SingleThreadEventExecutor, ThreadProperties> PROPERTIES_UPDATER = AtomicReferenceFieldUpdater.newUpdater(SingleThreadEventExecutor.class, ThreadProperties.class, "threadProperties");
/*  50:    */   private final Queue<Runnable> taskQueue;
/*  51:    */   private volatile Thread thread;
/*  52:    */   private volatile ThreadProperties threadProperties;
/*  53:    */   private final Executor executor;
/*  54:    */   private volatile boolean interrupted;
/*  55: 90 */   private final Semaphore threadLock = new Semaphore(0);
/*  56: 91 */   private final Set<Runnable> shutdownHooks = new LinkedHashSet();
/*  57:    */   private final boolean addTaskWakesUp;
/*  58:    */   private final int maxPendingTasks;
/*  59:    */   private final RejectedExecutionHandler rejectedExecutionHandler;
/*  60:    */   private long lastExecutionTime;
/*  61: 98 */   private volatile int state = 1;
/*  62:    */   private volatile long gracefulShutdownQuietPeriod;
/*  63:    */   private volatile long gracefulShutdownTimeout;
/*  64:    */   private long gracefulShutdownStartTime;
/*  65:105 */   private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
/*  66:    */   
/*  67:    */   protected SingleThreadEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp)
/*  68:    */   {
/*  69:117 */     this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp);
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected SingleThreadEventExecutor(EventExecutorGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedHandler)
/*  73:    */   {
/*  74:133 */     this(parent, new ThreadPerTaskExecutor(threadFactory), addTaskWakesUp, maxPendingTasks, rejectedHandler);
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp)
/*  78:    */   {
/*  79:145 */     this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_EXECUTOR_TASKS, RejectedExecutionHandlers.reject());
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedHandler)
/*  83:    */   {
/*  84:161 */     super(parent);
/*  85:162 */     this.addTaskWakesUp = addTaskWakesUp;
/*  86:163 */     this.maxPendingTasks = Math.max(16, maxPendingTasks);
/*  87:164 */     this.executor = ((Executor)ObjectUtil.checkNotNull(executor, "executor"));
/*  88:165 */     this.taskQueue = newTaskQueue(this.maxPendingTasks);
/*  89:166 */     this.rejectedExecutionHandler = ((RejectedExecutionHandler)ObjectUtil.checkNotNull(rejectedHandler, "rejectedHandler"));
/*  90:    */   }
/*  91:    */   
/*  92:    */   @Deprecated
/*  93:    */   protected Queue<Runnable> newTaskQueue()
/*  94:    */   {
/*  95:174 */     return newTaskQueue(this.maxPendingTasks);
/*  96:    */   }
/*  97:    */   
/*  98:    */   protected Queue<Runnable> newTaskQueue(int maxPendingTasks)
/*  99:    */   {
/* 100:184 */     return new LinkedBlockingQueue(maxPendingTasks);
/* 101:    */   }
/* 102:    */   
/* 103:    */   protected void interruptThread()
/* 104:    */   {
/* 105:191 */     Thread currentThread = this.thread;
/* 106:192 */     if (currentThread == null) {
/* 107:193 */       this.interrupted = true;
/* 108:    */     } else {
/* 109:195 */       currentThread.interrupt();
/* 110:    */     }
/* 111:    */   }
/* 112:    */   
/* 113:    */   protected Runnable pollTask()
/* 114:    */   {
/* 115:203 */     assert (inEventLoop());
/* 116:204 */     return pollTaskFrom(this.taskQueue);
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected static Runnable pollTaskFrom(Queue<Runnable> taskQueue)
/* 120:    */   {
/* 121:    */     Runnable task;
/* 122:    */     do
/* 123:    */     {
/* 124:209 */       task = (Runnable)taskQueue.poll();
/* 125:210 */     } while (task == WAKEUP_TASK);
/* 126:213 */     return task;
/* 127:    */   }
/* 128:    */   
/* 129:    */   protected Runnable takeTask()
/* 130:    */   {
/* 131:227 */     assert (inEventLoop());
/* 132:228 */     if (!(this.taskQueue instanceof BlockingQueue)) {
/* 133:229 */       throw new UnsupportedOperationException();
/* 134:    */     }
/* 135:232 */     BlockingQueue<Runnable> taskQueue = (BlockingQueue)this.taskQueue;
/* 136:    */     for (;;)
/* 137:    */     {
/* 138:234 */       ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
/* 139:235 */       if (scheduledTask == null)
/* 140:    */       {
/* 141:236 */         Runnable task = null;
/* 142:    */         try
/* 143:    */         {
/* 144:238 */           task = (Runnable)taskQueue.take();
/* 145:239 */           if (task == WAKEUP_TASK) {
/* 146:240 */             task = null;
/* 147:    */           }
/* 148:    */         }
/* 149:    */         catch (InterruptedException localInterruptedException1) {}
/* 150:245 */         return task;
/* 151:    */       }
/* 152:247 */       long delayNanos = scheduledTask.delayNanos();
/* 153:248 */       Runnable task = null;
/* 154:249 */       if (delayNanos > 0L) {
/* 155:    */         try
/* 156:    */         {
/* 157:251 */           task = (Runnable)taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
/* 158:    */         }
/* 159:    */         catch (InterruptedException e)
/* 160:    */         {
/* 161:254 */           return null;
/* 162:    */         }
/* 163:    */       }
/* 164:257 */       if (task == null)
/* 165:    */       {
/* 166:262 */         fetchFromScheduledTaskQueue();
/* 167:263 */         task = (Runnable)taskQueue.poll();
/* 168:    */       }
/* 169:266 */       if (task != null) {
/* 170:267 */         return task;
/* 171:    */       }
/* 172:    */     }
/* 173:    */   }
/* 174:    */   
/* 175:    */   private boolean fetchFromScheduledTaskQueue()
/* 176:    */   {
/* 177:274 */     long nanoTime = AbstractScheduledEventExecutor.nanoTime();
/* 178:275 */     Runnable scheduledTask = pollScheduledTask(nanoTime);
/* 179:276 */     while (scheduledTask != null)
/* 180:    */     {
/* 181:277 */       if (!this.taskQueue.offer(scheduledTask))
/* 182:    */       {
/* 183:279 */         scheduledTaskQueue().add((ScheduledFutureTask)scheduledTask);
/* 184:280 */         return false;
/* 185:    */       }
/* 186:282 */       scheduledTask = pollScheduledTask(nanoTime);
/* 187:    */     }
/* 188:284 */     return true;
/* 189:    */   }
/* 190:    */   
/* 191:    */   protected Runnable peekTask()
/* 192:    */   {
/* 193:291 */     assert (inEventLoop());
/* 194:292 */     return (Runnable)this.taskQueue.peek();
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected boolean hasTasks()
/* 198:    */   {
/* 199:299 */     assert (inEventLoop());
/* 200:300 */     return !this.taskQueue.isEmpty();
/* 201:    */   }
/* 202:    */   
/* 203:    */   public int pendingTasks()
/* 204:    */   {
/* 205:310 */     return this.taskQueue.size();
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected void addTask(Runnable task)
/* 209:    */   {
/* 210:318 */     if (task == null) {
/* 211:319 */       throw new NullPointerException("task");
/* 212:    */     }
/* 213:321 */     if (!offerTask(task)) {
/* 214:322 */       reject(task);
/* 215:    */     }
/* 216:    */   }
/* 217:    */   
/* 218:    */   final boolean offerTask(Runnable task)
/* 219:    */   {
/* 220:327 */     if (isShutdown()) {
/* 221:328 */       reject();
/* 222:    */     }
/* 223:330 */     return this.taskQueue.offer(task);
/* 224:    */   }
/* 225:    */   
/* 226:    */   protected boolean removeTask(Runnable task)
/* 227:    */   {
/* 228:337 */     if (task == null) {
/* 229:338 */       throw new NullPointerException("task");
/* 230:    */     }
/* 231:340 */     return this.taskQueue.remove(task);
/* 232:    */   }
/* 233:    */   
/* 234:    */   protected boolean runAllTasks()
/* 235:    */   {
/* 236:349 */     assert (inEventLoop());
/* 237:    */     
/* 238:351 */     boolean ranAtLeastOne = false;
/* 239:    */     boolean fetchedAll;
/* 240:    */     do
/* 241:    */     {
/* 242:354 */       fetchedAll = fetchFromScheduledTaskQueue();
/* 243:355 */       if (runAllTasksFrom(this.taskQueue)) {
/* 244:356 */         ranAtLeastOne = true;
/* 245:    */       }
/* 246:358 */     } while (!fetchedAll);
/* 247:360 */     if (ranAtLeastOne) {
/* 248:361 */       this.lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 249:    */     }
/* 250:363 */     afterRunningAllTasks();
/* 251:364 */     return ranAtLeastOne;
/* 252:    */   }
/* 253:    */   
/* 254:    */   protected final boolean runAllTasksFrom(Queue<Runnable> taskQueue)
/* 255:    */   {
/* 256:375 */     Runnable task = pollTaskFrom(taskQueue);
/* 257:376 */     if (task == null) {
/* 258:377 */       return false;
/* 259:    */     }
/* 260:    */     do
/* 261:    */     {
/* 262:380 */       safeExecute(task);
/* 263:381 */       task = pollTaskFrom(taskQueue);
/* 264:382 */     } while (task != null);
/* 265:383 */     return true;
/* 266:    */   }
/* 267:    */   
/* 268:    */   protected boolean runAllTasks(long timeoutNanos)
/* 269:    */   {
/* 270:393 */     fetchFromScheduledTaskQueue();
/* 271:394 */     Runnable task = pollTask();
/* 272:395 */     if (task == null)
/* 273:    */     {
/* 274:396 */       afterRunningAllTasks();
/* 275:397 */       return false;
/* 276:    */     }
/* 277:400 */     long deadline = ScheduledFutureTask.nanoTime() + timeoutNanos;
/* 278:401 */     long runTasks = 0L;
/* 279:    */     do
/* 280:    */     {
/* 281:404 */       safeExecute(task);
/* 282:    */       
/* 283:406 */       runTasks += 1L;
/* 284:410 */       if ((runTasks & 0x3F) == 0L)
/* 285:    */       {
/* 286:411 */         long lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 287:412 */         if (lastExecutionTime >= deadline) {
/* 288:    */           break;
/* 289:    */         }
/* 290:    */       }
/* 291:417 */       task = pollTask();
/* 292:418 */     } while (task != null);
/* 293:419 */     long lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 294:    */     
/* 295:    */ 
/* 296:    */ 
/* 297:    */ 
/* 298:424 */     afterRunningAllTasks();
/* 299:425 */     this.lastExecutionTime = lastExecutionTime;
/* 300:426 */     return true;
/* 301:    */   }
/* 302:    */   
/* 303:    */   protected void afterRunningAllTasks() {}
/* 304:    */   
/* 305:    */   protected long delayNanos(long currentTimeNanos)
/* 306:    */   {
/* 307:438 */     ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
/* 308:439 */     if (scheduledTask == null) {
/* 309:440 */       return SCHEDULE_PURGE_INTERVAL;
/* 310:    */     }
/* 311:443 */     return scheduledTask.delayNanos(currentTimeNanos);
/* 312:    */   }
/* 313:    */   
/* 314:    */   protected void updateLastExecutionTime()
/* 315:    */   {
/* 316:454 */     this.lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 317:    */   }
/* 318:    */   
/* 319:    */   protected abstract void run();
/* 320:    */   
/* 321:    */   protected void cleanup() {}
/* 322:    */   
/* 323:    */   protected void wakeup(boolean inEventLoop)
/* 324:    */   {
/* 325:470 */     if ((!inEventLoop) || (this.state == 3)) {
/* 326:473 */       this.taskQueue.offer(WAKEUP_TASK);
/* 327:    */     }
/* 328:    */   }
/* 329:    */   
/* 330:    */   public boolean inEventLoop(Thread thread)
/* 331:    */   {
/* 332:479 */     return thread == this.thread;
/* 333:    */   }
/* 334:    */   
/* 335:    */   public void addShutdownHook(final Runnable task)
/* 336:    */   {
/* 337:486 */     if (inEventLoop()) {
/* 338:487 */       this.shutdownHooks.add(task);
/* 339:    */     } else {
/* 340:489 */       execute(new Runnable()
/* 341:    */       {
/* 342:    */         public void run()
/* 343:    */         {
/* 344:492 */           SingleThreadEventExecutor.this.shutdownHooks.add(task);
/* 345:    */         }
/* 346:    */       });
/* 347:    */     }
/* 348:    */   }
/* 349:    */   
/* 350:    */   public void removeShutdownHook(final Runnable task)
/* 351:    */   {
/* 352:502 */     if (inEventLoop()) {
/* 353:503 */       this.shutdownHooks.remove(task);
/* 354:    */     } else {
/* 355:505 */       execute(new Runnable()
/* 356:    */       {
/* 357:    */         public void run()
/* 358:    */         {
/* 359:508 */           SingleThreadEventExecutor.this.shutdownHooks.remove(task);
/* 360:    */         }
/* 361:    */       });
/* 362:    */     }
/* 363:    */   }
/* 364:    */   
/* 365:    */   private boolean runShutdownHooks()
/* 366:    */   {
/* 367:515 */     boolean ran = false;
/* 368:517 */     while (!this.shutdownHooks.isEmpty())
/* 369:    */     {
/* 370:518 */       List<Runnable> copy = new ArrayList(this.shutdownHooks);
/* 371:519 */       this.shutdownHooks.clear();
/* 372:520 */       for (Runnable task : copy) {
/* 373:    */         try
/* 374:    */         {
/* 375:522 */           task.run();
/* 376:    */         }
/* 377:    */         catch (Throwable t)
/* 378:    */         {
/* 379:524 */           logger.warn("Shutdown hook raised an exception.", t);
/* 380:    */         }
/* 381:    */         finally
/* 382:    */         {
/* 383:526 */           ran = true;
/* 384:    */         }
/* 385:    */       }
/* 386:    */     }
/* 387:531 */     if (ran) {
/* 388:532 */       this.lastExecutionTime = ScheduledFutureTask.nanoTime();
/* 389:    */     }
/* 390:535 */     return ran;
/* 391:    */   }
/* 392:    */   
/* 393:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 394:    */   {
/* 395:540 */     if (quietPeriod < 0L) {
/* 396:541 */       throw new IllegalArgumentException("quietPeriod: " + quietPeriod + " (expected >= 0)");
/* 397:    */     }
/* 398:543 */     if (timeout < quietPeriod) {
/* 399:544 */       throw new IllegalArgumentException("timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
/* 400:    */     }
/* 401:547 */     if (unit == null) {
/* 402:548 */       throw new NullPointerException("unit");
/* 403:    */     }
/* 404:551 */     if (isShuttingDown()) {
/* 405:552 */       return terminationFuture();
/* 406:    */     }
/* 407:555 */     boolean inEventLoop = inEventLoop();
/* 408:    */     boolean wakeup;
/* 409:    */     int oldState;
/* 410:    */     for (;;)
/* 411:    */     {
/* 412:559 */       if (isShuttingDown()) {
/* 413:560 */         return terminationFuture();
/* 414:    */       }
/* 415:563 */       wakeup = true;
/* 416:564 */       oldState = this.state;
/* 417:    */       int newState;
/* 418:    */       int newState;
/* 419:565 */       if (inEventLoop)
/* 420:    */       {
/* 421:566 */         newState = 3;
/* 422:    */       }
/* 423:    */       else
/* 424:    */       {
/* 425:    */         int newState;
/* 426:568 */         switch (oldState)
/* 427:    */         {
/* 428:    */         case 1: 
/* 429:    */         case 2: 
/* 430:571 */           newState = 3;
/* 431:572 */           break;
/* 432:    */         default: 
/* 433:574 */           newState = oldState;
/* 434:575 */           wakeup = false;
/* 435:    */         }
/* 436:    */       }
/* 437:578 */       if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
/* 438:    */         break;
/* 439:    */       }
/* 440:    */     }
/* 441:582 */     this.gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
/* 442:583 */     this.gracefulShutdownTimeout = unit.toNanos(timeout);
/* 443:585 */     if (oldState == 1) {
/* 444:    */       try
/* 445:    */       {
/* 446:587 */         doStartThread();
/* 447:    */       }
/* 448:    */       catch (Throwable cause)
/* 449:    */       {
/* 450:589 */         STATE_UPDATER.set(this, 5);
/* 451:590 */         this.terminationFuture.tryFailure(cause);
/* 452:592 */         if (!(cause instanceof Exception)) {
/* 453:594 */           PlatformDependent.throwException(cause);
/* 454:    */         }
/* 455:596 */         return this.terminationFuture;
/* 456:    */       }
/* 457:    */     }
/* 458:600 */     if (wakeup) {
/* 459:601 */       wakeup(inEventLoop);
/* 460:    */     }
/* 461:604 */     return terminationFuture();
/* 462:    */   }
/* 463:    */   
/* 464:    */   public Future<?> terminationFuture()
/* 465:    */   {
/* 466:609 */     return this.terminationFuture;
/* 467:    */   }
/* 468:    */   
/* 469:    */   @Deprecated
/* 470:    */   public void shutdown()
/* 471:    */   {
/* 472:615 */     if (isShutdown()) {
/* 473:616 */       return;
/* 474:    */     }
/* 475:619 */     boolean inEventLoop = inEventLoop();
/* 476:    */     boolean wakeup;
/* 477:    */     int oldState;
/* 478:    */     for (;;)
/* 479:    */     {
/* 480:623 */       if (isShuttingDown()) {
/* 481:624 */         return;
/* 482:    */       }
/* 483:627 */       wakeup = true;
/* 484:628 */       oldState = this.state;
/* 485:    */       int newState;
/* 486:    */       int newState;
/* 487:629 */       if (inEventLoop)
/* 488:    */       {
/* 489:630 */         newState = 4;
/* 490:    */       }
/* 491:    */       else
/* 492:    */       {
/* 493:    */         int newState;
/* 494:632 */         switch (oldState)
/* 495:    */         {
/* 496:    */         case 1: 
/* 497:    */         case 2: 
/* 498:    */         case 3: 
/* 499:636 */           newState = 4;
/* 500:637 */           break;
/* 501:    */         default: 
/* 502:639 */           newState = oldState;
/* 503:640 */           wakeup = false;
/* 504:    */         }
/* 505:    */       }
/* 506:643 */       if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
/* 507:    */         break;
/* 508:    */       }
/* 509:    */     }
/* 510:648 */     if (oldState == 1) {
/* 511:    */       try
/* 512:    */       {
/* 513:650 */         doStartThread();
/* 514:    */       }
/* 515:    */       catch (Throwable cause)
/* 516:    */       {
/* 517:652 */         STATE_UPDATER.set(this, 5);
/* 518:653 */         this.terminationFuture.tryFailure(cause);
/* 519:655 */         if (!(cause instanceof Exception)) {
/* 520:657 */           PlatformDependent.throwException(cause);
/* 521:    */         }
/* 522:659 */         return;
/* 523:    */       }
/* 524:    */     }
/* 525:663 */     if (wakeup) {
/* 526:664 */       wakeup(inEventLoop);
/* 527:    */     }
/* 528:    */   }
/* 529:    */   
/* 530:    */   public boolean isShuttingDown()
/* 531:    */   {
/* 532:670 */     return this.state >= 3;
/* 533:    */   }
/* 534:    */   
/* 535:    */   public boolean isShutdown()
/* 536:    */   {
/* 537:675 */     return this.state >= 4;
/* 538:    */   }
/* 539:    */   
/* 540:    */   public boolean isTerminated()
/* 541:    */   {
/* 542:680 */     return this.state == 5;
/* 543:    */   }
/* 544:    */   
/* 545:    */   protected boolean confirmShutdown()
/* 546:    */   {
/* 547:687 */     if (!isShuttingDown()) {
/* 548:688 */       return false;
/* 549:    */     }
/* 550:691 */     if (!inEventLoop()) {
/* 551:692 */       throw new IllegalStateException("must be invoked from an event loop");
/* 552:    */     }
/* 553:695 */     cancelScheduledTasks();
/* 554:697 */     if (this.gracefulShutdownStartTime == 0L) {
/* 555:698 */       this.gracefulShutdownStartTime = ScheduledFutureTask.nanoTime();
/* 556:    */     }
/* 557:701 */     if ((runAllTasks()) || (runShutdownHooks()))
/* 558:    */     {
/* 559:702 */       if (isShutdown()) {
/* 560:704 */         return true;
/* 561:    */       }
/* 562:710 */       if (this.gracefulShutdownQuietPeriod == 0L) {
/* 563:711 */         return true;
/* 564:    */       }
/* 565:713 */       wakeup(true);
/* 566:714 */       return false;
/* 567:    */     }
/* 568:717 */     long nanoTime = ScheduledFutureTask.nanoTime();
/* 569:719 */     if ((isShutdown()) || (nanoTime - this.gracefulShutdownStartTime > this.gracefulShutdownTimeout)) {
/* 570:720 */       return true;
/* 571:    */     }
/* 572:723 */     if (nanoTime - this.lastExecutionTime <= this.gracefulShutdownQuietPeriod)
/* 573:    */     {
/* 574:726 */       wakeup(true);
/* 575:    */       try
/* 576:    */       {
/* 577:728 */         Thread.sleep(100L);
/* 578:    */       }
/* 579:    */       catch (InterruptedException localInterruptedException) {}
/* 580:733 */       return false;
/* 581:    */     }
/* 582:738 */     return true;
/* 583:    */   }
/* 584:    */   
/* 585:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 586:    */     throws InterruptedException
/* 587:    */   {
/* 588:743 */     if (unit == null) {
/* 589:744 */       throw new NullPointerException("unit");
/* 590:    */     }
/* 591:747 */     if (inEventLoop()) {
/* 592:748 */       throw new IllegalStateException("cannot await termination of the current thread");
/* 593:    */     }
/* 594:751 */     if (this.threadLock.tryAcquire(timeout, unit)) {
/* 595:752 */       this.threadLock.release();
/* 596:    */     }
/* 597:755 */     return isTerminated();
/* 598:    */   }
/* 599:    */   
/* 600:    */   public void execute(Runnable task)
/* 601:    */   {
/* 602:760 */     if (task == null) {
/* 603:761 */       throw new NullPointerException("task");
/* 604:    */     }
/* 605:764 */     boolean inEventLoop = inEventLoop();
/* 606:765 */     if (inEventLoop)
/* 607:    */     {
/* 608:766 */       addTask(task);
/* 609:    */     }
/* 610:    */     else
/* 611:    */     {
/* 612:768 */       startThread();
/* 613:769 */       addTask(task);
/* 614:770 */       if ((isShutdown()) && (removeTask(task))) {
/* 615:771 */         reject();
/* 616:    */       }
/* 617:    */     }
/* 618:775 */     if ((!this.addTaskWakesUp) && (wakesUpForTask(task))) {
/* 619:776 */       wakeup(inEventLoop);
/* 620:    */     }
/* 621:    */   }
/* 622:    */   
/* 623:    */   public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
/* 624:    */     throws InterruptedException, ExecutionException
/* 625:    */   {
/* 626:782 */     throwIfInEventLoop("invokeAny");
/* 627:783 */     return super.invokeAny(tasks);
/* 628:    */   }
/* 629:    */   
/* 630:    */   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
/* 631:    */     throws InterruptedException, ExecutionException, TimeoutException
/* 632:    */   {
/* 633:789 */     throwIfInEventLoop("invokeAny");
/* 634:790 */     return super.invokeAny(tasks, timeout, unit);
/* 635:    */   }
/* 636:    */   
/* 637:    */   public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
/* 638:    */     throws InterruptedException
/* 639:    */   {
/* 640:796 */     throwIfInEventLoop("invokeAll");
/* 641:797 */     return super.invokeAll(tasks);
/* 642:    */   }
/* 643:    */   
/* 644:    */   public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
/* 645:    */     throws InterruptedException
/* 646:    */   {
/* 647:803 */     throwIfInEventLoop("invokeAll");
/* 648:804 */     return super.invokeAll(tasks, timeout, unit);
/* 649:    */   }
/* 650:    */   
/* 651:    */   private void throwIfInEventLoop(String method)
/* 652:    */   {
/* 653:808 */     if (inEventLoop()) {
/* 654:809 */       throw new RejectedExecutionException("Calling " + method + " from within the EventLoop is not allowed");
/* 655:    */     }
/* 656:    */   }
/* 657:    */   
/* 658:    */   public final ThreadProperties threadProperties()
/* 659:    */   {
/* 660:819 */     ThreadProperties threadProperties = this.threadProperties;
/* 661:820 */     if (threadProperties == null)
/* 662:    */     {
/* 663:821 */       Thread thread = this.thread;
/* 664:822 */       if (thread == null)
/* 665:    */       {
/* 666:823 */         assert (!inEventLoop());
/* 667:824 */         submit(NOOP_TASK).syncUninterruptibly();
/* 668:825 */         thread = this.thread;
/* 669:826 */         assert (thread != null);
/* 670:    */       }
/* 671:829 */       threadProperties = new DefaultThreadProperties(thread);
/* 672:830 */       if (!PROPERTIES_UPDATER.compareAndSet(this, null, threadProperties)) {
/* 673:831 */         threadProperties = this.threadProperties;
/* 674:    */       }
/* 675:    */     }
/* 676:835 */     return threadProperties;
/* 677:    */   }
/* 678:    */   
/* 679:    */   protected boolean wakesUpForTask(Runnable task)
/* 680:    */   {
/* 681:840 */     return true;
/* 682:    */   }
/* 683:    */   
/* 684:    */   protected static void reject()
/* 685:    */   {
/* 686:844 */     throw new RejectedExecutionException("event executor terminated");
/* 687:    */   }
/* 688:    */   
/* 689:    */   protected final void reject(Runnable task)
/* 690:    */   {
/* 691:853 */     this.rejectedExecutionHandler.rejected(task, this);
/* 692:    */   }
/* 693:    */   
/* 694:858 */   private static final long SCHEDULE_PURGE_INTERVAL = TimeUnit.SECONDS.toNanos(1L);
/* 695:    */   
/* 696:    */   private void startThread()
/* 697:    */   {
/* 698:861 */     if ((this.state == 1) && 
/* 699:862 */       (STATE_UPDATER.compareAndSet(this, 1, 2))) {
/* 700:    */       try
/* 701:    */       {
/* 702:864 */         doStartThread();
/* 703:    */       }
/* 704:    */       catch (Throwable cause)
/* 705:    */       {
/* 706:866 */         STATE_UPDATER.set(this, 1);
/* 707:867 */         PlatformDependent.throwException(cause);
/* 708:    */       }
/* 709:    */     }
/* 710:    */   }
/* 711:    */   
/* 712:    */   private void doStartThread()
/* 713:    */   {
/* 714:874 */     assert (this.thread == null);
/* 715:875 */     this.executor.execute(new Runnable()
/* 716:    */     {
/* 717:    */       public void run()
/* 718:    */       {
/* 719:878 */         SingleThreadEventExecutor.this.thread = Thread.currentThread();
/* 720:879 */         if (SingleThreadEventExecutor.this.interrupted) {
/* 721:880 */           SingleThreadEventExecutor.this.thread.interrupt();
/* 722:    */         }
/* 723:883 */         boolean success = false;
/* 724:884 */         SingleThreadEventExecutor.this.updateLastExecutionTime();
/* 725:    */         try
/* 726:    */         {
/* 727:886 */           SingleThreadEventExecutor.this.run();
/* 728:887 */           success = true;
/* 729:    */           for (;;)
/* 730:    */           {
/* 731:892 */             int oldState = SingleThreadEventExecutor.this.state;
/* 732:893 */             if ((oldState >= 3) || (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this, oldState, 3))) {
/* 733:    */               break;
/* 734:    */             }
/* 735:    */           }
/* 736:900 */           if ((success) && (SingleThreadEventExecutor.this.gracefulShutdownStartTime == 0L)) {
/* 737:901 */             SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class
/* 738:902 */               .getSimpleName() + ".confirmShutdown() must be called before run() implementation terminates.");
/* 739:    */           }
/* 740:    */           try
/* 741:    */           {
/* 742:    */             for (;;)
/* 743:    */             {
/* 744:909 */               if (SingleThreadEventExecutor.this.confirmShutdown()) {
/* 745:    */                 break;
/* 746:    */               }
/* 747:    */             }
/* 748:    */           }
/* 749:    */           finally
/* 750:    */           {
/* 751:    */             try
/* 752:    */             {
/* 753:915 */               SingleThreadEventExecutor.this.cleanup();
/* 754:    */               
/* 755:917 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 756:918 */               SingleThreadEventExecutor.this.threadLock.release();
/* 757:919 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 758:920 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + 
/* 759:    */                 
/* 760:922 */                   SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 761:    */               }
/* 762:925 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 763:    */             }
/* 764:    */             finally
/* 765:    */             {
/* 766:917 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 767:918 */               SingleThreadEventExecutor.this.threadLock.release();
/* 768:919 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 769:920 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + 
/* 770:    */                 
/* 771:922 */                   SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 772:    */               }
/* 773:925 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 774:    */             }
/* 775:    */             try
/* 776:    */             {
/* 777:915 */               SingleThreadEventExecutor.this.cleanup();
/* 778:    */             }
/* 779:    */             finally
/* 780:    */             {
/* 781:917 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 782:918 */               SingleThreadEventExecutor.this.threadLock.release();
/* 783:919 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 784:920 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + 
/* 785:    */                 
/* 786:922 */                   SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 787:    */               }
/* 788:925 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 789:    */             }
/* 790:    */           }
/* 791:    */           int oldState;
/* 792:    */           label696:
/* 793:    */           int oldState;
/* 794:    */           label1226:
/* 795:    */           return;
/* 796:    */         }
/* 797:    */         catch (Throwable t)
/* 798:    */         {
/* 799:889 */           SingleThreadEventExecutor.logger.warn("Unexpected exception from an event executor: ", t);
/* 800:    */           for (;;)
/* 801:    */           {
/* 802:892 */             oldState = SingleThreadEventExecutor.this.state;
/* 803:893 */             if ((oldState >= 3) || (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this, oldState, 3))) {
/* 804:    */               break;
/* 805:    */             }
/* 806:    */           }
/* 807:900 */           if ((success) && (SingleThreadEventExecutor.this.gracefulShutdownStartTime == 0L)) {
/* 808:901 */             SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class
/* 809:902 */               .getSimpleName() + ".confirmShutdown() must be called before run() implementation terminates.");
/* 810:    */           }
/* 811:    */           try
/* 812:    */           {
/* 813:909 */             while (!SingleThreadEventExecutor.this.confirmShutdown()) {}
/* 814:    */             break label696;
/* 815:    */           }
/* 816:    */           finally
/* 817:    */           {
/* 818:    */             try
/* 819:    */             {
/* 820:915 */               SingleThreadEventExecutor.this.cleanup();
/* 821:    */             }
/* 822:    */             finally
/* 823:    */             {
/* 824:917 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 825:918 */               SingleThreadEventExecutor.this.threadLock.release();
/* 826:919 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 827:920 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + 
/* 828:    */                 
/* 829:922 */                   SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 830:    */               }
/* 831:925 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 832:    */             }
/* 833:    */           }
/* 834:    */         }
/* 835:    */         finally
/* 836:    */         {
/* 837:    */           for (;;)
/* 838:    */           {
/* 839:892 */             oldState = SingleThreadEventExecutor.this.state;
/* 840:893 */             if ((oldState >= 3) || (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(SingleThreadEventExecutor.this, oldState, 3))) {
/* 841:    */               break;
/* 842:    */             }
/* 843:    */           }
/* 844:900 */           if ((success) && (SingleThreadEventExecutor.this.gracefulShutdownStartTime == 0L)) {
/* 845:901 */             SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class
/* 846:902 */               .getSimpleName() + ".confirmShutdown() must be called before run() implementation terminates.");
/* 847:    */           }
/* 848:    */           try
/* 849:    */           {
/* 850:909 */             while (!SingleThreadEventExecutor.this.confirmShutdown()) {}
/* 851:    */             break label1226;
/* 852:    */           }
/* 853:    */           finally
/* 854:    */           {
/* 855:    */             try
/* 856:    */             {
/* 857:915 */               SingleThreadEventExecutor.this.cleanup();
/* 858:    */             }
/* 859:    */             finally
/* 860:    */             {
/* 861:917 */               SingleThreadEventExecutor.STATE_UPDATER.set(SingleThreadEventExecutor.this, 5);
/* 862:918 */               SingleThreadEventExecutor.this.threadLock.release();
/* 863:919 */               if (!SingleThreadEventExecutor.this.taskQueue.isEmpty()) {
/* 864:920 */                 SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + 
/* 865:    */                 
/* 866:922 */                   SingleThreadEventExecutor.this.taskQueue.size() + ')');
/* 867:    */               }
/* 868:925 */               SingleThreadEventExecutor.this.terminationFuture.setSuccess(null);
/* 869:    */             }
/* 870:    */           }
/* 871:    */         }
/* 872:    */       }
/* 873:    */     });
/* 874:    */   }
/* 875:    */   
/* 876:    */   private static final class DefaultThreadProperties
/* 877:    */     implements ThreadProperties
/* 878:    */   {
/* 879:    */     private final Thread t;
/* 880:    */     
/* 881:    */     DefaultThreadProperties(Thread t)
/* 882:    */     {
/* 883:937 */       this.t = t;
/* 884:    */     }
/* 885:    */     
/* 886:    */     public Thread.State state()
/* 887:    */     {
/* 888:942 */       return this.t.getState();
/* 889:    */     }
/* 890:    */     
/* 891:    */     public int priority()
/* 892:    */     {
/* 893:947 */       return this.t.getPriority();
/* 894:    */     }
/* 895:    */     
/* 896:    */     public boolean isInterrupted()
/* 897:    */     {
/* 898:952 */       return this.t.isInterrupted();
/* 899:    */     }
/* 900:    */     
/* 901:    */     public boolean isDaemon()
/* 902:    */     {
/* 903:957 */       return this.t.isDaemon();
/* 904:    */     }
/* 905:    */     
/* 906:    */     public String name()
/* 907:    */     {
/* 908:962 */       return this.t.getName();
/* 909:    */     }
/* 910:    */     
/* 911:    */     public long id()
/* 912:    */     {
/* 913:967 */       return this.t.getId();
/* 914:    */     }
/* 915:    */     
/* 916:    */     public StackTraceElement[] stackTrace()
/* 917:    */     {
/* 918:972 */       return this.t.getStackTrace();
/* 919:    */     }
/* 920:    */     
/* 921:    */     public boolean isAlive()
/* 922:    */     {
/* 923:977 */       return this.t.isAlive();
/* 924:    */     }
/* 925:    */   }
/* 926:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.SingleThreadEventExecutor
 * JD-Core Version:    0.7.0.1
 */