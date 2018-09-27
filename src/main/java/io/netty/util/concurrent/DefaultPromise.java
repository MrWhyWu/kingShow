/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.Signal;
/*   4:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   9:    */ import io.netty.util.internal.ThrowableUtil;
/*  10:    */ import io.netty.util.internal.logging.InternalLogger;
/*  11:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  12:    */ import java.util.concurrent.CancellationException;
/*  13:    */ import java.util.concurrent.TimeUnit;
/*  14:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  15:    */ 
/*  16:    */ public class DefaultPromise<V>
/*  17:    */   extends AbstractFuture<V>
/*  18:    */   implements Promise<V>
/*  19:    */ {
/*  20: 35 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultPromise.class);
/*  21: 37 */   private static final InternalLogger rejectedExecutionLogger = InternalLoggerFactory.getInstance(DefaultPromise.class.getName() + ".rejectedExecution");
/*  22: 38 */   private static final int MAX_LISTENER_STACK_DEPTH = Math.min(8, 
/*  23: 39 */     SystemPropertyUtil.getInt("io.netty.defaultPromise.maxListenerStackDepth", 8));
/*  24: 42 */   private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
/*  25: 43 */   private static final Signal SUCCESS = Signal.valueOf(DefaultPromise.class, "SUCCESS");
/*  26: 44 */   private static final Signal UNCANCELLABLE = Signal.valueOf(DefaultPromise.class, "UNCANCELLABLE");
/*  27: 45 */   private static final CauseHolder CANCELLATION_CAUSE_HOLDER = new CauseHolder(ThrowableUtil.unknownStackTrace(new CancellationException(), DefaultPromise.class, "cancel(...)"));
/*  28:    */   private volatile Object result;
/*  29:    */   private final EventExecutor executor;
/*  30:    */   private Object listeners;
/*  31:    */   private short waiters;
/*  32:    */   private boolean notifyingListeners;
/*  33:    */   
/*  34:    */   public DefaultPromise(EventExecutor executor)
/*  35:    */   {
/*  36: 81 */     this.executor = ((EventExecutor)ObjectUtil.checkNotNull(executor, "executor"));
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected DefaultPromise()
/*  40:    */   {
/*  41: 89 */     this.executor = null;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public Promise<V> setSuccess(V result)
/*  45:    */   {
/*  46: 94 */     if (setSuccess0(result))
/*  47:    */     {
/*  48: 95 */       notifyListeners();
/*  49: 96 */       return this;
/*  50:    */     }
/*  51: 98 */     throw new IllegalStateException("complete already: " + this);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public boolean trySuccess(V result)
/*  55:    */   {
/*  56:103 */     if (setSuccess0(result))
/*  57:    */     {
/*  58:104 */       notifyListeners();
/*  59:105 */       return true;
/*  60:    */     }
/*  61:107 */     return false;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public Promise<V> setFailure(Throwable cause)
/*  65:    */   {
/*  66:112 */     if (setFailure0(cause))
/*  67:    */     {
/*  68:113 */       notifyListeners();
/*  69:114 */       return this;
/*  70:    */     }
/*  71:116 */     throw new IllegalStateException("complete already: " + this, cause);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public boolean tryFailure(Throwable cause)
/*  75:    */   {
/*  76:121 */     if (setFailure0(cause))
/*  77:    */     {
/*  78:122 */       notifyListeners();
/*  79:123 */       return true;
/*  80:    */     }
/*  81:125 */     return false;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public boolean setUncancellable()
/*  85:    */   {
/*  86:130 */     if (RESULT_UPDATER.compareAndSet(this, null, UNCANCELLABLE)) {
/*  87:131 */       return true;
/*  88:    */     }
/*  89:133 */     Object result = this.result;
/*  90:134 */     return (!isDone0(result)) || (!isCancelled0(result));
/*  91:    */   }
/*  92:    */   
/*  93:    */   public boolean isSuccess()
/*  94:    */   {
/*  95:139 */     Object result = this.result;
/*  96:140 */     return (result != null) && (result != UNCANCELLABLE) && (!(result instanceof CauseHolder));
/*  97:    */   }
/*  98:    */   
/*  99:    */   public boolean isCancellable()
/* 100:    */   {
/* 101:145 */     return this.result == null;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public Throwable cause()
/* 105:    */   {
/* 106:150 */     Object result = this.result;
/* 107:151 */     return (result instanceof CauseHolder) ? ((CauseHolder)result).cause : null;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener)
/* 111:    */   {
/* 112:156 */     ObjectUtil.checkNotNull(listener, "listener");
/* 113:158 */     synchronized (this)
/* 114:    */     {
/* 115:159 */       addListener0(listener);
/* 116:    */     }
/* 117:162 */     if (isDone()) {
/* 118:163 */       notifyListeners();
/* 119:    */     }
/* 120:166 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/* 124:    */   {
/* 125:171 */     ObjectUtil.checkNotNull(listeners, "listeners");
/* 126:173 */     synchronized (this)
/* 127:    */     {
/* 128:174 */       for (GenericFutureListener<? extends Future<? super V>> listener : listeners)
/* 129:    */       {
/* 130:175 */         if (listener == null) {
/* 131:    */           break;
/* 132:    */         }
/* 133:178 */         addListener0(listener);
/* 134:    */       }
/* 135:    */     }
/* 136:182 */     if (isDone()) {
/* 137:183 */       notifyListeners();
/* 138:    */     }
/* 139:186 */     return this;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener)
/* 143:    */   {
/* 144:191 */     ObjectUtil.checkNotNull(listener, "listener");
/* 145:193 */     synchronized (this)
/* 146:    */     {
/* 147:194 */       removeListener0(listener);
/* 148:    */     }
/* 149:197 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   public Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/* 153:    */   {
/* 154:202 */     ObjectUtil.checkNotNull(listeners, "listeners");
/* 155:204 */     synchronized (this)
/* 156:    */     {
/* 157:205 */       for (GenericFutureListener<? extends Future<? super V>> listener : listeners)
/* 158:    */       {
/* 159:206 */         if (listener == null) {
/* 160:    */           break;
/* 161:    */         }
/* 162:209 */         removeListener0(listener);
/* 163:    */       }
/* 164:    */     }
/* 165:213 */     return this;
/* 166:    */   }
/* 167:    */   
/* 168:    */   public Promise<V> await()
/* 169:    */     throws InterruptedException
/* 170:    */   {
/* 171:218 */     if (isDone()) {
/* 172:219 */       return this;
/* 173:    */     }
/* 174:222 */     if (Thread.interrupted()) {
/* 175:223 */       throw new InterruptedException(toString());
/* 176:    */     }
/* 177:226 */     checkDeadLock();
/* 178:228 */     synchronized (this)
/* 179:    */     {
/* 180:229 */       if (!isDone())
/* 181:    */       {
/* 182:230 */         incWaiters();
/* 183:    */         try
/* 184:    */         {
/* 185:232 */           wait();
/* 186:    */           
/* 187:234 */           decWaiters();
/* 188:    */         }
/* 189:    */         finally
/* 190:    */         {
/* 191:234 */           decWaiters();
/* 192:    */         }
/* 193:    */       }
/* 194:    */     }
/* 195:238 */     return this;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public Promise<V> awaitUninterruptibly()
/* 199:    */   {
/* 200:243 */     if (isDone()) {
/* 201:244 */       return this;
/* 202:    */     }
/* 203:247 */     checkDeadLock();
/* 204:    */     
/* 205:249 */     boolean interrupted = false;
/* 206:250 */     synchronized (this)
/* 207:    */     {
/* 208:251 */       while (!isDone())
/* 209:    */       {
/* 210:252 */         incWaiters();
/* 211:    */         try
/* 212:    */         {
/* 213:254 */           wait();
/* 214:    */         }
/* 215:    */         catch (InterruptedException e)
/* 216:    */         {
/* 217:257 */           interrupted = true;
/* 218:    */         }
/* 219:    */         finally
/* 220:    */         {
/* 221:259 */           decWaiters();
/* 222:    */         }
/* 223:    */       }
/* 224:    */     }
/* 225:264 */     if (interrupted) {
/* 226:265 */       Thread.currentThread().interrupt();
/* 227:    */     }
/* 228:268 */     return this;
/* 229:    */   }
/* 230:    */   
/* 231:    */   public boolean await(long timeout, TimeUnit unit)
/* 232:    */     throws InterruptedException
/* 233:    */   {
/* 234:273 */     return await0(unit.toNanos(timeout), true);
/* 235:    */   }
/* 236:    */   
/* 237:    */   public boolean await(long timeoutMillis)
/* 238:    */     throws InterruptedException
/* 239:    */   {
/* 240:278 */     return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), true);
/* 241:    */   }
/* 242:    */   
/* 243:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/* 244:    */   {
/* 245:    */     try
/* 246:    */     {
/* 247:284 */       return await0(unit.toNanos(timeout), false);
/* 248:    */     }
/* 249:    */     catch (InterruptedException e)
/* 250:    */     {
/* 251:287 */       throw new InternalError();
/* 252:    */     }
/* 253:    */   }
/* 254:    */   
/* 255:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/* 256:    */   {
/* 257:    */     try
/* 258:    */     {
/* 259:294 */       return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), false);
/* 260:    */     }
/* 261:    */     catch (InterruptedException e)
/* 262:    */     {
/* 263:297 */       throw new InternalError();
/* 264:    */     }
/* 265:    */   }
/* 266:    */   
/* 267:    */   public V getNow()
/* 268:    */   {
/* 269:304 */     Object result = this.result;
/* 270:305 */     if (((result instanceof CauseHolder)) || (result == SUCCESS)) {
/* 271:306 */       return null;
/* 272:    */     }
/* 273:308 */     return result;
/* 274:    */   }
/* 275:    */   
/* 276:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 277:    */   {
/* 278:313 */     if (RESULT_UPDATER.compareAndSet(this, null, CANCELLATION_CAUSE_HOLDER))
/* 279:    */     {
/* 280:314 */       checkNotifyWaiters();
/* 281:315 */       notifyListeners();
/* 282:316 */       return true;
/* 283:    */     }
/* 284:318 */     return false;
/* 285:    */   }
/* 286:    */   
/* 287:    */   public boolean isCancelled()
/* 288:    */   {
/* 289:323 */     return isCancelled0(this.result);
/* 290:    */   }
/* 291:    */   
/* 292:    */   public boolean isDone()
/* 293:    */   {
/* 294:328 */     return isDone0(this.result);
/* 295:    */   }
/* 296:    */   
/* 297:    */   public Promise<V> sync()
/* 298:    */     throws InterruptedException
/* 299:    */   {
/* 300:333 */     await();
/* 301:334 */     rethrowIfFailed();
/* 302:335 */     return this;
/* 303:    */   }
/* 304:    */   
/* 305:    */   public Promise<V> syncUninterruptibly()
/* 306:    */   {
/* 307:340 */     awaitUninterruptibly();
/* 308:341 */     rethrowIfFailed();
/* 309:342 */     return this;
/* 310:    */   }
/* 311:    */   
/* 312:    */   public String toString()
/* 313:    */   {
/* 314:347 */     return toStringBuilder().toString();
/* 315:    */   }
/* 316:    */   
/* 317:    */   protected StringBuilder toStringBuilder()
/* 318:    */   {
/* 319:354 */     StringBuilder buf = new StringBuilder(64).append(StringUtil.simpleClassName(this)).append('@').append(Integer.toHexString(hashCode()));
/* 320:    */     
/* 321:356 */     Object result = this.result;
/* 322:357 */     if (result == SUCCESS) {
/* 323:358 */       buf.append("(success)");
/* 324:359 */     } else if (result == UNCANCELLABLE) {
/* 325:360 */       buf.append("(uncancellable)");
/* 326:361 */     } else if ((result instanceof CauseHolder)) {
/* 327:364 */       buf.append("(failure: ").append(((CauseHolder)result).cause).append(')');
/* 328:365 */     } else if (result != null) {
/* 329:368 */       buf.append("(success: ").append(result).append(')');
/* 330:    */     } else {
/* 331:370 */       buf.append("(incomplete)");
/* 332:    */     }
/* 333:373 */     return buf;
/* 334:    */   }
/* 335:    */   
/* 336:    */   protected EventExecutor executor()
/* 337:    */   {
/* 338:385 */     return this.executor;
/* 339:    */   }
/* 340:    */   
/* 341:    */   protected void checkDeadLock()
/* 342:    */   {
/* 343:389 */     EventExecutor e = executor();
/* 344:390 */     if ((e != null) && (e.inEventLoop())) {
/* 345:391 */       throw new BlockingOperationException(toString());
/* 346:    */     }
/* 347:    */   }
/* 348:    */   
/* 349:    */   protected static void notifyListener(EventExecutor eventExecutor, Future<?> future, GenericFutureListener<?> listener)
/* 350:    */   {
/* 351:406 */     ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
/* 352:407 */     ObjectUtil.checkNotNull(future, "future");
/* 353:408 */     ObjectUtil.checkNotNull(listener, "listener");
/* 354:409 */     notifyListenerWithStackOverFlowProtection(eventExecutor, future, listener);
/* 355:    */   }
/* 356:    */   
/* 357:    */   private void notifyListeners()
/* 358:    */   {
/* 359:413 */     EventExecutor executor = executor();
/* 360:414 */     if (executor.inEventLoop())
/* 361:    */     {
/* 362:415 */       InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 363:416 */       int stackDepth = threadLocals.futureListenerStackDepth();
/* 364:417 */       if (stackDepth < MAX_LISTENER_STACK_DEPTH)
/* 365:    */       {
/* 366:418 */         threadLocals.setFutureListenerStackDepth(stackDepth + 1);
/* 367:    */         try
/* 368:    */         {
/* 369:420 */           notifyListenersNow();
/* 370:    */         }
/* 371:    */         finally
/* 372:    */         {
/* 373:422 */           threadLocals.setFutureListenerStackDepth(stackDepth);
/* 374:    */         }
/* 375:424 */         return;
/* 376:    */       }
/* 377:    */     }
/* 378:428 */     safeExecute(executor, new Runnable()
/* 379:    */     {
/* 380:    */       public void run()
/* 381:    */       {
/* 382:431 */         DefaultPromise.this.notifyListenersNow();
/* 383:    */       }
/* 384:    */     });
/* 385:    */   }
/* 386:    */   
/* 387:    */   private static void notifyListenerWithStackOverFlowProtection(EventExecutor executor, Future<?> future, final GenericFutureListener<?> listener)
/* 388:    */   {
/* 389:444 */     if (executor.inEventLoop())
/* 390:    */     {
/* 391:445 */       InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
/* 392:446 */       int stackDepth = threadLocals.futureListenerStackDepth();
/* 393:447 */       if (stackDepth < MAX_LISTENER_STACK_DEPTH)
/* 394:    */       {
/* 395:448 */         threadLocals.setFutureListenerStackDepth(stackDepth + 1);
/* 396:    */         try
/* 397:    */         {
/* 398:450 */           notifyListener0(future, listener);
/* 399:    */         }
/* 400:    */         finally
/* 401:    */         {
/* 402:452 */           threadLocals.setFutureListenerStackDepth(stackDepth);
/* 403:    */         }
/* 404:454 */         return;
/* 405:    */       }
/* 406:    */     }
/* 407:458 */     safeExecute(executor, new Runnable()
/* 408:    */     {
/* 409:    */       public void run()
/* 410:    */       {
/* 411:461 */         DefaultPromise.notifyListener0(this.val$future, listener);
/* 412:    */       }
/* 413:    */     });
/* 414:    */   }
/* 415:    */   
/* 416:    */   private void notifyListenersNow()
/* 417:    */   {
/* 418:468 */     synchronized (this)
/* 419:    */     {
/* 420:470 */       if ((this.notifyingListeners) || (this.listeners == null)) {
/* 421:471 */         return;
/* 422:    */       }
/* 423:473 */       this.notifyingListeners = true;
/* 424:474 */       Object listeners = this.listeners;
/* 425:475 */       this.listeners = null;
/* 426:    */     }
/* 427:    */     for (;;)
/* 428:    */     {
/* 429:    */       Object listeners;
/* 430:478 */       if ((listeners instanceof DefaultFutureListeners)) {
/* 431:479 */         notifyListeners0((DefaultFutureListeners)listeners);
/* 432:    */       } else {
/* 433:481 */         notifyListener0(this, (GenericFutureListener)listeners);
/* 434:    */       }
/* 435:483 */       synchronized (this)
/* 436:    */       {
/* 437:484 */         if (this.listeners == null)
/* 438:    */         {
/* 439:487 */           this.notifyingListeners = false;
/* 440:488 */           return;
/* 441:    */         }
/* 442:490 */         listeners = this.listeners;
/* 443:491 */         this.listeners = null;
/* 444:    */       }
/* 445:    */     }
/* 446:    */   }
/* 447:    */   
/* 448:    */   private void notifyListeners0(DefaultFutureListeners listeners)
/* 449:    */   {
/* 450:497 */     GenericFutureListener<?>[] a = listeners.listeners();
/* 451:498 */     int size = listeners.size();
/* 452:499 */     for (int i = 0; i < size; i++) {
/* 453:500 */       notifyListener0(this, a[i]);
/* 454:    */     }
/* 455:    */   }
/* 456:    */   
/* 457:    */   private static void notifyListener0(Future future, GenericFutureListener l)
/* 458:    */   {
/* 459:    */     try
/* 460:    */     {
/* 461:507 */       l.operationComplete(future);
/* 462:    */     }
/* 463:    */     catch (Throwable t)
/* 464:    */     {
/* 465:509 */       logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationComplete()", t);
/* 466:    */     }
/* 467:    */   }
/* 468:    */   
/* 469:    */   private void addListener0(GenericFutureListener<? extends Future<? super V>> listener)
/* 470:    */   {
/* 471:514 */     if (this.listeners == null) {
/* 472:515 */       this.listeners = listener;
/* 473:516 */     } else if ((this.listeners instanceof DefaultFutureListeners)) {
/* 474:517 */       ((DefaultFutureListeners)this.listeners).add(listener);
/* 475:    */     } else {
/* 476:519 */       this.listeners = new DefaultFutureListeners((GenericFutureListener)this.listeners, listener);
/* 477:    */     }
/* 478:    */   }
/* 479:    */   
/* 480:    */   private void removeListener0(GenericFutureListener<? extends Future<? super V>> listener)
/* 481:    */   {
/* 482:524 */     if ((this.listeners instanceof DefaultFutureListeners)) {
/* 483:525 */       ((DefaultFutureListeners)this.listeners).remove(listener);
/* 484:526 */     } else if (this.listeners == listener) {
/* 485:527 */       this.listeners = null;
/* 486:    */     }
/* 487:    */   }
/* 488:    */   
/* 489:    */   private boolean setSuccess0(V result)
/* 490:    */   {
/* 491:532 */     return setValue0(result == null ? SUCCESS : result);
/* 492:    */   }
/* 493:    */   
/* 494:    */   private boolean setFailure0(Throwable cause)
/* 495:    */   {
/* 496:536 */     return setValue0(new CauseHolder((Throwable)ObjectUtil.checkNotNull(cause, "cause")));
/* 497:    */   }
/* 498:    */   
/* 499:    */   private boolean setValue0(Object objResult)
/* 500:    */   {
/* 501:540 */     if ((RESULT_UPDATER.compareAndSet(this, null, objResult)) || 
/* 502:541 */       (RESULT_UPDATER.compareAndSet(this, UNCANCELLABLE, objResult)))
/* 503:    */     {
/* 504:542 */       checkNotifyWaiters();
/* 505:543 */       return true;
/* 506:    */     }
/* 507:545 */     return false;
/* 508:    */   }
/* 509:    */   
/* 510:    */   private synchronized void checkNotifyWaiters()
/* 511:    */   {
/* 512:549 */     if (this.waiters > 0) {
/* 513:550 */       notifyAll();
/* 514:    */     }
/* 515:    */   }
/* 516:    */   
/* 517:    */   private void incWaiters()
/* 518:    */   {
/* 519:555 */     if (this.waiters == 32767) {
/* 520:556 */       throw new IllegalStateException("too many waiters: " + this);
/* 521:    */     }
/* 522:558 */     this.waiters = ((short)(this.waiters + 1));
/* 523:    */   }
/* 524:    */   
/* 525:    */   private void decWaiters()
/* 526:    */   {
/* 527:562 */     this.waiters = ((short)(this.waiters - 1));
/* 528:    */   }
/* 529:    */   
/* 530:    */   private void rethrowIfFailed()
/* 531:    */   {
/* 532:566 */     Throwable cause = cause();
/* 533:567 */     if (cause == null) {
/* 534:568 */       return;
/* 535:    */     }
/* 536:571 */     PlatformDependent.throwException(cause);
/* 537:    */   }
/* 538:    */   
/* 539:    */   private boolean await0(long timeoutNanos, boolean interruptable)
/* 540:    */     throws InterruptedException
/* 541:    */   {
/* 542:575 */     if (isDone()) {
/* 543:576 */       return true;
/* 544:    */     }
/* 545:579 */     if (timeoutNanos <= 0L) {
/* 546:580 */       return isDone();
/* 547:    */     }
/* 548:583 */     if ((interruptable) && (Thread.interrupted())) {
/* 549:584 */       throw new InterruptedException(toString());
/* 550:    */     }
/* 551:587 */     checkDeadLock();
/* 552:    */     
/* 553:589 */     long startTime = System.nanoTime();
/* 554:590 */     long waitTime = timeoutNanos;
/* 555:591 */     boolean interrupted = false;
/* 556:    */     try
/* 557:    */     {
/* 558:    */       do
/* 559:    */       {
/* 560:594 */         synchronized (this)
/* 561:    */         {
/* 562:595 */           if (isDone()) {
/* 563:596 */             return true;
/* 564:    */           }
/* 565:598 */           incWaiters();
/* 566:    */           try
/* 567:    */           {
/* 568:600 */             wait(waitTime / 1000000L, (int)(waitTime % 1000000L));
/* 569:    */           }
/* 570:    */           catch (InterruptedException e)
/* 571:    */           {
/* 572:602 */             if (interruptable) {
/* 573:603 */               throw e;
/* 574:    */             }
/* 575:605 */             interrupted = true;
/* 576:    */           }
/* 577:    */           finally
/* 578:    */           {
/* 579:608 */             decWaiters();
/* 580:    */           }
/* 581:    */         }
/* 582:611 */         if (isDone()) {
/* 583:612 */           return 1;
/* 584:    */         }
/* 585:614 */         waitTime = timeoutNanos - (System.nanoTime() - startTime);
/* 586:615 */       } while (waitTime > 0L);
/* 587:616 */       return isDone();
/* 588:    */     }
/* 589:    */     finally
/* 590:    */     {
/* 591:621 */       if (interrupted) {
/* 592:622 */         Thread.currentThread().interrupt();
/* 593:    */       }
/* 594:    */     }
/* 595:    */   }
/* 596:    */   
/* 597:    */   void notifyProgressiveListeners(final long progress, long total)
/* 598:    */   {
/* 599:639 */     Object listeners = progressiveListeners();
/* 600:640 */     if (listeners == null) {
/* 601:641 */       return;
/* 602:    */     }
/* 603:644 */     final ProgressiveFuture<V> self = (ProgressiveFuture)this;
/* 604:    */     
/* 605:646 */     EventExecutor executor = executor();
/* 606:647 */     if (executor.inEventLoop())
/* 607:    */     {
/* 608:648 */       if ((listeners instanceof GenericProgressiveFutureListener[])) {
/* 609:649 */         notifyProgressiveListeners0(self, (GenericProgressiveFutureListener[])listeners, progress, total);
/* 610:    */       } else {
/* 611:652 */         notifyProgressiveListener0(self, (GenericProgressiveFutureListener)listeners, progress, total);
/* 612:    */       }
/* 613:    */     }
/* 614:656 */     else if ((listeners instanceof GenericProgressiveFutureListener[]))
/* 615:    */     {
/* 616:657 */       final GenericProgressiveFutureListener<?>[] array = (GenericProgressiveFutureListener[])listeners;
/* 617:    */       
/* 618:659 */       safeExecute(executor, new Runnable()
/* 619:    */       {
/* 620:    */         public void run()
/* 621:    */         {
/* 622:662 */           DefaultPromise.notifyProgressiveListeners0(self, array, progress, this.val$total);
/* 623:    */         }
/* 624:    */       });
/* 625:    */     }
/* 626:    */     else
/* 627:    */     {
/* 628:666 */       final GenericProgressiveFutureListener<ProgressiveFuture<V>> l = (GenericProgressiveFutureListener)listeners;
/* 629:    */       
/* 630:668 */       safeExecute(executor, new Runnable()
/* 631:    */       {
/* 632:    */         public void run()
/* 633:    */         {
/* 634:671 */           DefaultPromise.notifyProgressiveListener0(self, l, progress, this.val$total);
/* 635:    */         }
/* 636:    */       });
/* 637:    */     }
/* 638:    */   }
/* 639:    */   
/* 640:    */   private synchronized Object progressiveListeners()
/* 641:    */   {
/* 642:683 */     Object listeners = this.listeners;
/* 643:684 */     if (listeners == null) {
/* 644:686 */       return null;
/* 645:    */     }
/* 646:689 */     if ((listeners instanceof DefaultFutureListeners))
/* 647:    */     {
/* 648:691 */       DefaultFutureListeners dfl = (DefaultFutureListeners)listeners;
/* 649:692 */       int progressiveSize = dfl.progressiveSize();
/* 650:693 */       switch (progressiveSize)
/* 651:    */       {
/* 652:    */       case 0: 
/* 653:695 */         return null;
/* 654:    */       case 1: 
/* 655:697 */         for (GenericFutureListener<?> l : dfl.listeners()) {
/* 656:698 */           if ((l instanceof GenericProgressiveFutureListener)) {
/* 657:699 */             return l;
/* 658:    */           }
/* 659:    */         }
/* 660:702 */         return null;
/* 661:    */       }
/* 662:705 */       Object array = dfl.listeners();
/* 663:706 */       Object copy = new GenericProgressiveFutureListener[progressiveSize];
/* 664:707 */       int i = 0;
/* 665:707 */       for (int j = 0; j < progressiveSize; i++)
/* 666:    */       {
/* 667:708 */         GenericFutureListener<?> l = array[i];
/* 668:709 */         if ((l instanceof GenericProgressiveFutureListener)) {
/* 669:710 */           copy[(j++)] = ((GenericProgressiveFutureListener)l);
/* 670:    */         }
/* 671:    */       }
/* 672:714 */       return copy;
/* 673:    */     }
/* 674:715 */     if ((listeners instanceof GenericProgressiveFutureListener)) {
/* 675:716 */       return listeners;
/* 676:    */     }
/* 677:719 */     return null;
/* 678:    */   }
/* 679:    */   
/* 680:    */   private static void notifyProgressiveListeners0(ProgressiveFuture<?> future, GenericProgressiveFutureListener<?>[] listeners, long progress, long total)
/* 681:    */   {
/* 682:725 */     for (GenericProgressiveFutureListener<?> l : listeners)
/* 683:    */     {
/* 684:726 */       if (l == null) {
/* 685:    */         break;
/* 686:    */       }
/* 687:729 */       notifyProgressiveListener0(future, l, progress, total);
/* 688:    */     }
/* 689:    */   }
/* 690:    */   
/* 691:    */   private static void notifyProgressiveListener0(ProgressiveFuture future, GenericProgressiveFutureListener l, long progress, long total)
/* 692:    */   {
/* 693:    */     try
/* 694:    */     {
/* 695:737 */       l.operationProgressed(future, progress, total);
/* 696:    */     }
/* 697:    */     catch (Throwable t)
/* 698:    */     {
/* 699:739 */       logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationProgressed()", t);
/* 700:    */     }
/* 701:    */   }
/* 702:    */   
/* 703:    */   private static boolean isCancelled0(Object result)
/* 704:    */   {
/* 705:744 */     return ((result instanceof CauseHolder)) && ((((CauseHolder)result).cause instanceof CancellationException));
/* 706:    */   }
/* 707:    */   
/* 708:    */   private static boolean isDone0(Object result)
/* 709:    */   {
/* 710:748 */     return (result != null) && (result != UNCANCELLABLE);
/* 711:    */   }
/* 712:    */   
/* 713:    */   private static final class CauseHolder
/* 714:    */   {
/* 715:    */     final Throwable cause;
/* 716:    */     
/* 717:    */     CauseHolder(Throwable cause)
/* 718:    */     {
/* 719:754 */       this.cause = cause;
/* 720:    */     }
/* 721:    */   }
/* 722:    */   
/* 723:    */   private static void safeExecute(EventExecutor executor, Runnable task)
/* 724:    */   {
/* 725:    */     try
/* 726:    */     {
/* 727:760 */       executor.execute(task);
/* 728:    */     }
/* 729:    */     catch (Throwable t)
/* 730:    */     {
/* 731:762 */       rejectedExecutionLogger.error("Failed to submit a listener notification task. Event loop shut down?", t);
/* 732:    */     }
/* 733:    */   }
/* 734:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultPromise
 * JD-Core Version:    0.7.0.1
 */