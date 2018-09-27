/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.EventLoop;
/*   5:    */ import io.netty.channel.EventLoopException;
/*   6:    */ import io.netty.channel.SelectStrategy;
/*   7:    */ import io.netty.channel.SingleThreadEventLoop;
/*   8:    */ import io.netty.util.IntSupplier;
/*   9:    */ import io.netty.util.concurrent.Future;
/*  10:    */ import io.netty.util.concurrent.RejectedExecutionHandler;
/*  11:    */ import io.netty.util.internal.PlatformDependent;
/*  12:    */ import io.netty.util.internal.ReflectionUtil;
/*  13:    */ import io.netty.util.internal.SystemPropertyUtil;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.io.IOException;
/*  17:    */ import java.lang.reflect.Field;
/*  18:    */ import java.nio.channels.CancelledKeyException;
/*  19:    */ import java.nio.channels.SelectableChannel;
/*  20:    */ import java.nio.channels.SelectionKey;
/*  21:    */ import java.nio.channels.Selector;
/*  22:    */ import java.nio.channels.spi.SelectorProvider;
/*  23:    */ import java.security.AccessController;
/*  24:    */ import java.security.PrivilegedAction;
/*  25:    */ import java.util.ArrayList;
/*  26:    */ import java.util.Collection;
/*  27:    */ import java.util.Iterator;
/*  28:    */ import java.util.Queue;
/*  29:    */ import java.util.Set;
/*  30:    */ import java.util.concurrent.Callable;
/*  31:    */ import java.util.concurrent.Executor;
/*  32:    */ import java.util.concurrent.TimeUnit;
/*  33:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  34:    */ 
/*  35:    */ public final class NioEventLoop
/*  36:    */   extends SingleThreadEventLoop
/*  37:    */ {
/*  38: 58 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioEventLoop.class);
/*  39:    */   private static final int CLEANUP_INTERVAL = 256;
/*  40: 63 */   private static final boolean DISABLE_KEYSET_OPTIMIZATION = SystemPropertyUtil.getBoolean("io.netty.noKeySetOptimization", false);
/*  41:    */   private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
/*  42:    */   private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;
/*  43: 68 */   private final IntSupplier selectNowSupplier = new IntSupplier()
/*  44:    */   {
/*  45:    */     public int get()
/*  46:    */       throws Exception
/*  47:    */     {
/*  48: 71 */       return NioEventLoop.this.selectNow();
/*  49:    */     }
/*  50:    */   };
/*  51: 74 */   private final Callable<Integer> pendingTasksCallable = new Callable()
/*  52:    */   {
/*  53:    */     public Integer call()
/*  54:    */       throws Exception
/*  55:    */     {
/*  56: 77 */       return Integer.valueOf(NioEventLoop.this.pendingTasks());
/*  57:    */     }
/*  58:    */   };
/*  59:    */   private Selector selector;
/*  60:    */   private Selector unwrappedSelector;
/*  61:    */   private SelectedSelectionKeySet selectedKeys;
/*  62:    */   private final SelectorProvider provider;
/*  63:    */   
/*  64:    */   static
/*  65:    */   {
/*  66: 87 */     String key = "sun.nio.ch.bugLevel";
/*  67: 88 */     String buglevel = SystemPropertyUtil.get("sun.nio.ch.bugLevel");
/*  68: 89 */     if (buglevel == null) {
/*  69:    */       try
/*  70:    */       {
/*  71: 91 */         AccessController.doPrivileged(new PrivilegedAction()
/*  72:    */         {
/*  73:    */           public Void run()
/*  74:    */           {
/*  75: 94 */             System.setProperty("sun.nio.ch.bugLevel", "");
/*  76: 95 */             return null;
/*  77:    */           }
/*  78:    */         });
/*  79:    */       }
/*  80:    */       catch (SecurityException e)
/*  81:    */       {
/*  82: 99 */         logger.debug("Unable to get/set System Property: sun.nio.ch.bugLevel", e);
/*  83:    */       }
/*  84:    */     }
/*  85:103 */     int selectorAutoRebuildThreshold = SystemPropertyUtil.getInt("io.netty.selectorAutoRebuildThreshold", 512);
/*  86:104 */     if (selectorAutoRebuildThreshold < 3) {
/*  87:105 */       selectorAutoRebuildThreshold = 0;
/*  88:    */     }
/*  89:108 */     SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;
/*  90:110 */     if (logger.isDebugEnabled())
/*  91:    */     {
/*  92:111 */       logger.debug("-Dio.netty.noKeySetOptimization: {}", Boolean.valueOf(DISABLE_KEYSET_OPTIMIZATION));
/*  93:112 */       logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", Integer.valueOf(SELECTOR_AUTO_REBUILD_THRESHOLD));
/*  94:    */     }
/*  95:    */   }
/*  96:    */   
/*  97:131 */   private final AtomicBoolean wakenUp = new AtomicBoolean();
/*  98:    */   private final SelectStrategy selectStrategy;
/*  99:135 */   private volatile int ioRatio = 50;
/* 100:    */   private int cancelledKeys;
/* 101:    */   private boolean needsToSelectAgain;
/* 102:    */   
/* 103:    */   NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler)
/* 104:    */   {
/* 105:141 */     super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
/* 106:142 */     if (selectorProvider == null) {
/* 107:143 */       throw new NullPointerException("selectorProvider");
/* 108:    */     }
/* 109:145 */     if (strategy == null) {
/* 110:146 */       throw new NullPointerException("selectStrategy");
/* 111:    */     }
/* 112:148 */     this.provider = selectorProvider;
/* 113:149 */     SelectorTuple selectorTuple = openSelector();
/* 114:150 */     this.selector = selectorTuple.selector;
/* 115:151 */     this.unwrappedSelector = selectorTuple.unwrappedSelector;
/* 116:152 */     this.selectStrategy = strategy;
/* 117:    */   }
/* 118:    */   
/* 119:    */   private static final class SelectorTuple
/* 120:    */   {
/* 121:    */     final Selector unwrappedSelector;
/* 122:    */     final Selector selector;
/* 123:    */     
/* 124:    */     SelectorTuple(Selector unwrappedSelector)
/* 125:    */     {
/* 126:160 */       this.unwrappedSelector = unwrappedSelector;
/* 127:161 */       this.selector = unwrappedSelector;
/* 128:    */     }
/* 129:    */     
/* 130:    */     SelectorTuple(Selector unwrappedSelector, Selector selector)
/* 131:    */     {
/* 132:165 */       this.unwrappedSelector = unwrappedSelector;
/* 133:166 */       this.selector = selector;
/* 134:    */     }
/* 135:    */   }
/* 136:    */   
/* 137:    */   private SelectorTuple openSelector()
/* 138:    */   {
/* 139:    */     try
/* 140:    */     {
/* 141:173 */       unwrappedSelector = this.provider.openSelector();
/* 142:    */     }
/* 143:    */     catch (IOException e)
/* 144:    */     {
/* 145:    */       Selector unwrappedSelector;
/* 146:175 */       throw new ChannelException("failed to open a new selector", e);
/* 147:    */     }
/* 148:    */     final Selector unwrappedSelector;
/* 149:178 */     if (DISABLE_KEYSET_OPTIMIZATION) {
/* 150:179 */       return new SelectorTuple(unwrappedSelector);
/* 151:    */     }
/* 152:182 */     final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();
/* 153:    */     
/* 154:184 */     Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction()
/* 155:    */     {
/* 156:    */       public Object run()
/* 157:    */       {
/* 158:    */         try
/* 159:    */         {
/* 160:188 */           return Class.forName("sun.nio.ch.SelectorImpl", false, 
/* 161:    */           
/* 162:    */ 
/* 163:191 */             PlatformDependent.getSystemClassLoader());
/* 164:    */         }
/* 165:    */         catch (Throwable cause)
/* 166:    */         {
/* 167:193 */           return cause;
/* 168:    */         }
/* 169:    */       }
/* 170:    */     });
/* 171:198 */     if ((!(maybeSelectorImplClass instanceof Class)) || 
/* 172:    */     
/* 173:200 */       (!((Class)maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())))
/* 174:    */     {
/* 175:201 */       if ((maybeSelectorImplClass instanceof Throwable))
/* 176:    */       {
/* 177:202 */         Throwable t = (Throwable)maybeSelectorImplClass;
/* 178:203 */         logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
/* 179:    */       }
/* 180:205 */       return new SelectorTuple(unwrappedSelector);
/* 181:    */     }
/* 182:208 */     final Class<?> selectorImplClass = (Class)maybeSelectorImplClass;
/* 183:    */     
/* 184:210 */     Object maybeException = AccessController.doPrivileged(new PrivilegedAction()
/* 185:    */     {
/* 186:    */       public Object run()
/* 187:    */       {
/* 188:    */         try
/* 189:    */         {
/* 190:214 */           Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
/* 191:215 */           Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
/* 192:    */           
/* 193:217 */           Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField);
/* 194:218 */           if (cause != null) {
/* 195:219 */             return cause;
/* 196:    */           }
/* 197:221 */           cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField);
/* 198:222 */           if (cause != null) {
/* 199:223 */             return cause;
/* 200:    */           }
/* 201:226 */           selectedKeysField.set(unwrappedSelector, selectedKeySet);
/* 202:227 */           publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
/* 203:228 */           return null;
/* 204:    */         }
/* 205:    */         catch (NoSuchFieldException e)
/* 206:    */         {
/* 207:230 */           return e;
/* 208:    */         }
/* 209:    */         catch (IllegalAccessException e)
/* 210:    */         {
/* 211:232 */           return e;
/* 212:    */         }
/* 213:    */       }
/* 214:    */     });
/* 215:237 */     if ((maybeException instanceof Exception))
/* 216:    */     {
/* 217:238 */       this.selectedKeys = null;
/* 218:239 */       Exception e = (Exception)maybeException;
/* 219:240 */       logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
/* 220:241 */       return new SelectorTuple(unwrappedSelector);
/* 221:    */     }
/* 222:243 */     this.selectedKeys = selectedKeySet;
/* 223:244 */     logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);
/* 224:245 */     return new SelectorTuple(unwrappedSelector, new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
/* 225:    */   }
/* 226:    */   
/* 227:    */   public SelectorProvider selectorProvider()
/* 228:    */   {
/* 229:253 */     return this.provider;
/* 230:    */   }
/* 231:    */   
/* 232:    */   protected Queue<Runnable> newTaskQueue(int maxPendingTasks)
/* 233:    */   {
/* 234:259 */     return maxPendingTasks == 2147483647 ? PlatformDependent.newMpscQueue() : 
/* 235:260 */       PlatformDependent.newMpscQueue(maxPendingTasks);
/* 236:    */   }
/* 237:    */   
/* 238:    */   public int pendingTasks()
/* 239:    */   {
/* 240:268 */     if (inEventLoop()) {
/* 241:269 */       return super.pendingTasks();
/* 242:    */     }
/* 243:271 */     return ((Integer)submit(this.pendingTasksCallable).syncUninterruptibly().getNow()).intValue();
/* 244:    */   }
/* 245:    */   
/* 246:    */   public void register(SelectableChannel ch, int interestOps, NioTask<?> task)
/* 247:    */   {
/* 248:281 */     if (ch == null) {
/* 249:282 */       throw new NullPointerException("ch");
/* 250:    */     }
/* 251:284 */     if (interestOps == 0) {
/* 252:285 */       throw new IllegalArgumentException("interestOps must be non-zero.");
/* 253:    */     }
/* 254:287 */     if ((interestOps & (ch.validOps() ^ 0xFFFFFFFF)) != 0) {
/* 255:289 */       throw new IllegalArgumentException("invalid interestOps: " + interestOps + "(validOps: " + ch.validOps() + ')');
/* 256:    */     }
/* 257:291 */     if (task == null) {
/* 258:292 */       throw new NullPointerException("task");
/* 259:    */     }
/* 260:295 */     if (isShutdown()) {
/* 261:296 */       throw new IllegalStateException("event loop shut down");
/* 262:    */     }
/* 263:    */     try
/* 264:    */     {
/* 265:300 */       ch.register(this.selector, interestOps, task);
/* 266:    */     }
/* 267:    */     catch (Exception e)
/* 268:    */     {
/* 269:302 */       throw new EventLoopException("failed to register a channel", e);
/* 270:    */     }
/* 271:    */   }
/* 272:    */   
/* 273:    */   public int getIoRatio()
/* 274:    */   {
/* 275:310 */     return this.ioRatio;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public void setIoRatio(int ioRatio)
/* 279:    */   {
/* 280:318 */     if ((ioRatio <= 0) || (ioRatio > 100)) {
/* 281:319 */       throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
/* 282:    */     }
/* 283:321 */     this.ioRatio = ioRatio;
/* 284:    */   }
/* 285:    */   
/* 286:    */   public void rebuildSelector()
/* 287:    */   {
/* 288:329 */     if (!inEventLoop())
/* 289:    */     {
/* 290:330 */       execute(new Runnable()
/* 291:    */       {
/* 292:    */         public void run()
/* 293:    */         {
/* 294:333 */           NioEventLoop.this.rebuildSelector0();
/* 295:    */         }
/* 296:335 */       });
/* 297:336 */       return;
/* 298:    */     }
/* 299:338 */     rebuildSelector0();
/* 300:    */   }
/* 301:    */   
/* 302:    */   private void rebuildSelector0()
/* 303:    */   {
/* 304:342 */     Selector oldSelector = this.selector;
/* 305:345 */     if (oldSelector == null) {
/* 306:346 */       return;
/* 307:    */     }
/* 308:    */     try
/* 309:    */     {
/* 310:350 */       newSelectorTuple = openSelector();
/* 311:    */     }
/* 312:    */     catch (Exception e)
/* 313:    */     {
/* 314:    */       SelectorTuple newSelectorTuple;
/* 315:352 */       logger.warn("Failed to create a new Selector.", e); return;
/* 316:    */     }
/* 317:    */     SelectorTuple newSelectorTuple;
/* 318:357 */     int nChannels = 0;
/* 319:358 */     for (SelectionKey key : oldSelector.keys())
/* 320:    */     {
/* 321:359 */       Object a = key.attachment();
/* 322:    */       try
/* 323:    */       {
/* 324:361 */         if ((!key.isValid()) || (key.channel().keyFor(newSelectorTuple.unwrappedSelector) == null))
/* 325:    */         {
/* 326:365 */           int interestOps = key.interestOps();
/* 327:366 */           key.cancel();
/* 328:367 */           SelectionKey newKey = key.channel().register(newSelectorTuple.unwrappedSelector, interestOps, a);
/* 329:368 */           if ((a instanceof AbstractNioChannel)) {
/* 330:370 */             ((AbstractNioChannel)a).selectionKey = newKey;
/* 331:    */           }
/* 332:372 */           nChannels++;
/* 333:    */         }
/* 334:    */       }
/* 335:    */       catch (Exception e)
/* 336:    */       {
/* 337:374 */         logger.warn("Failed to re-register a Channel to the new Selector.", e);
/* 338:375 */         if ((a instanceof AbstractNioChannel))
/* 339:    */         {
/* 340:376 */           AbstractNioChannel ch = (AbstractNioChannel)a;
/* 341:377 */           ch.unsafe().close(ch.unsafe().voidPromise());
/* 342:    */         }
/* 343:    */         else
/* 344:    */         {
/* 345:380 */           NioTask<SelectableChannel> task = (NioTask)a;
/* 346:381 */           invokeChannelUnregistered(task, key, e);
/* 347:    */         }
/* 348:    */       }
/* 349:    */     }
/* 350:386 */     this.selector = newSelectorTuple.selector;
/* 351:387 */     this.unwrappedSelector = newSelectorTuple.unwrappedSelector;
/* 352:    */     try
/* 353:    */     {
/* 354:391 */       oldSelector.close();
/* 355:    */     }
/* 356:    */     catch (Throwable t)
/* 357:    */     {
/* 358:393 */       if (logger.isWarnEnabled()) {
/* 359:394 */         logger.warn("Failed to close the old Selector.", t);
/* 360:    */       }
/* 361:    */     }
/* 362:398 */     logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
/* 363:    */   }
/* 364:    */   
/* 365:    */   protected void run()
/* 366:    */   {
/* 367:    */     for (;;)
/* 368:    */     {
/* 369:    */       try
/* 370:    */       {
/* 371:405 */         switch (this.selectStrategy.calculateStrategy(this.selectNowSupplier, hasTasks()))
/* 372:    */         {
/* 373:    */         case -2: 
/* 374:    */           break;
/* 375:    */         case -1: 
/* 376:409 */           select(this.wakenUp.getAndSet(false));
/* 377:439 */           if (this.wakenUp.get()) {
/* 378:440 */             this.selector.wakeup();
/* 379:    */           }
/* 380:    */           break;
/* 381:    */         }
/* 382:446 */         this.cancelledKeys = 0;
/* 383:447 */         this.needsToSelectAgain = false;
/* 384:448 */         int ioRatio = this.ioRatio;
/* 385:449 */         if (ioRatio == 100) {
/* 386:    */           try
/* 387:    */           {
/* 388:451 */             processSelectedKeys();
/* 389:    */             
/* 390:    */ 
/* 391:454 */             runAllTasks();
/* 392:    */           }
/* 393:    */           finally
/* 394:    */           {
/* 395:454 */             runAllTasks();
/* 396:    */           }
/* 397:    */         }
/* 398:457 */         long ioStartTime = System.nanoTime();
/* 399:    */         try
/* 400:    */         {
/* 401:459 */           processSelectedKeys();
/* 402:    */         }
/* 403:    */         finally
/* 404:    */         {
/* 405:    */           long ioTime;
/* 406:462 */           long ioTime = System.nanoTime() - ioStartTime;
/* 407:463 */           runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
/* 408:    */         }
/* 409:    */       }
/* 410:    */       catch (Throwable t)
/* 411:    */       {
/* 412:467 */         handleLoopException(t);
/* 413:    */       }
/* 414:    */       try
/* 415:    */       {
/* 416:471 */         if (isShuttingDown())
/* 417:    */         {
/* 418:472 */           closeAll();
/* 419:473 */           if (confirmShutdown()) {
/* 420:474 */             return;
/* 421:    */           }
/* 422:    */         }
/* 423:    */       }
/* 424:    */       catch (Throwable t)
/* 425:    */       {
/* 426:478 */         handleLoopException(t);
/* 427:    */       }
/* 428:    */     }
/* 429:    */   }
/* 430:    */   
/* 431:    */   private static void handleLoopException(Throwable t)
/* 432:    */   {
/* 433:484 */     logger.warn("Unexpected exception in the selector loop.", t);
/* 434:    */     try
/* 435:    */     {
/* 436:489 */       Thread.sleep(1000L);
/* 437:    */     }
/* 438:    */     catch (InterruptedException localInterruptedException) {}
/* 439:    */   }
/* 440:    */   
/* 441:    */   private void processSelectedKeys()
/* 442:    */   {
/* 443:496 */     if (this.selectedKeys != null) {
/* 444:497 */       processSelectedKeysOptimized();
/* 445:    */     } else {
/* 446:499 */       processSelectedKeysPlain(this.selector.selectedKeys());
/* 447:    */     }
/* 448:    */   }
/* 449:    */   
/* 450:    */   protected void cleanup()
/* 451:    */   {
/* 452:    */     try
/* 453:    */     {
/* 454:506 */       this.selector.close();
/* 455:    */     }
/* 456:    */     catch (IOException e)
/* 457:    */     {
/* 458:508 */       logger.warn("Failed to close a selector.", e);
/* 459:    */     }
/* 460:    */   }
/* 461:    */   
/* 462:    */   void cancel(SelectionKey key)
/* 463:    */   {
/* 464:513 */     key.cancel();
/* 465:514 */     this.cancelledKeys += 1;
/* 466:515 */     if (this.cancelledKeys >= 256)
/* 467:    */     {
/* 468:516 */       this.cancelledKeys = 0;
/* 469:517 */       this.needsToSelectAgain = true;
/* 470:    */     }
/* 471:    */   }
/* 472:    */   
/* 473:    */   protected Runnable pollTask()
/* 474:    */   {
/* 475:523 */     Runnable task = super.pollTask();
/* 476:524 */     if (this.needsToSelectAgain) {
/* 477:525 */       selectAgain();
/* 478:    */     }
/* 479:527 */     return task;
/* 480:    */   }
/* 481:    */   
/* 482:    */   private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys)
/* 483:    */   {
/* 484:534 */     if (selectedKeys.isEmpty()) {
/* 485:535 */       return;
/* 486:    */     }
/* 487:538 */     Iterator<SelectionKey> i = selectedKeys.iterator();
/* 488:    */     for (;;)
/* 489:    */     {
/* 490:540 */       SelectionKey k = (SelectionKey)i.next();
/* 491:541 */       Object a = k.attachment();
/* 492:542 */       i.remove();
/* 493:544 */       if ((a instanceof AbstractNioChannel))
/* 494:    */       {
/* 495:545 */         processSelectedKey(k, (AbstractNioChannel)a);
/* 496:    */       }
/* 497:    */       else
/* 498:    */       {
/* 499:548 */         NioTask<SelectableChannel> task = (NioTask)a;
/* 500:549 */         processSelectedKey(k, task);
/* 501:    */       }
/* 502:552 */       if (!i.hasNext()) {
/* 503:    */         break;
/* 504:    */       }
/* 505:556 */       if (this.needsToSelectAgain)
/* 506:    */       {
/* 507:557 */         selectAgain();
/* 508:558 */         selectedKeys = this.selector.selectedKeys();
/* 509:561 */         if (selectedKeys.isEmpty()) {
/* 510:    */           break;
/* 511:    */         }
/* 512:564 */         i = selectedKeys.iterator();
/* 513:    */       }
/* 514:    */     }
/* 515:    */   }
/* 516:    */   
/* 517:    */   private void processSelectedKeysOptimized()
/* 518:    */   {
/* 519:571 */     for (int i = 0; i < this.selectedKeys.size; i++)
/* 520:    */     {
/* 521:572 */       SelectionKey k = this.selectedKeys.keys[i];
/* 522:    */       
/* 523:    */ 
/* 524:575 */       this.selectedKeys.keys[i] = null;
/* 525:    */       
/* 526:577 */       Object a = k.attachment();
/* 527:579 */       if ((a instanceof AbstractNioChannel))
/* 528:    */       {
/* 529:580 */         processSelectedKey(k, (AbstractNioChannel)a);
/* 530:    */       }
/* 531:    */       else
/* 532:    */       {
/* 533:583 */         NioTask<SelectableChannel> task = (NioTask)a;
/* 534:584 */         processSelectedKey(k, task);
/* 535:    */       }
/* 536:587 */       if (this.needsToSelectAgain)
/* 537:    */       {
/* 538:590 */         this.selectedKeys.reset(i + 1);
/* 539:    */         
/* 540:592 */         selectAgain();
/* 541:593 */         i = -1;
/* 542:    */       }
/* 543:    */     }
/* 544:    */   }
/* 545:    */   
/* 546:    */   private void processSelectedKey(SelectionKey k, AbstractNioChannel ch)
/* 547:    */   {
/* 548:599 */     AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
/* 549:600 */     if (!k.isValid())
/* 550:    */     {
/* 551:    */       try
/* 552:    */       {
/* 553:603 */         eventLoop = ch.eventLoop();
/* 554:    */       }
/* 555:    */       catch (Throwable ignored)
/* 556:    */       {
/* 557:    */         EventLoop eventLoop;
/* 558:    */         return;
/* 559:    */       }
/* 560:    */       EventLoop eventLoop;
/* 561:614 */       if ((eventLoop != this) || (eventLoop == null)) {
/* 562:615 */         return;
/* 563:    */       }
/* 564:618 */       unsafe.close(unsafe.voidPromise());
/* 565:619 */       return;
/* 566:    */     }
/* 567:    */     try
/* 568:    */     {
/* 569:623 */       int readyOps = k.readyOps();
/* 570:626 */       if ((readyOps & 0x8) != 0)
/* 571:    */       {
/* 572:629 */         int ops = k.interestOps();
/* 573:630 */         ops &= 0xFFFFFFF7;
/* 574:631 */         k.interestOps(ops);
/* 575:    */         
/* 576:633 */         unsafe.finishConnect();
/* 577:    */       }
/* 578:637 */       if ((readyOps & 0x4) != 0) {
/* 579:639 */         ch.unsafe().forceFlush();
/* 580:    */       }
/* 581:644 */       if (((readyOps & 0x11) != 0) || (readyOps == 0)) {
/* 582:645 */         unsafe.read();
/* 583:    */       }
/* 584:    */     }
/* 585:    */     catch (CancelledKeyException ignored)
/* 586:    */     {
/* 587:648 */       unsafe.close(unsafe.voidPromise());
/* 588:    */     }
/* 589:    */   }
/* 590:    */   
/* 591:    */   private static void processSelectedKey(SelectionKey k, NioTask<SelectableChannel> task)
/* 592:    */   {
/* 593:653 */     int state = 0;
/* 594:    */     try
/* 595:    */     {
/* 596:655 */       task.channelReady(k.channel(), k);
/* 597:656 */       state = 1;
/* 598:    */     }
/* 599:    */     catch (Exception e)
/* 600:    */     {
/* 601:658 */       k.cancel();
/* 602:659 */       invokeChannelUnregistered(task, k, e);
/* 603:660 */       state = 2;
/* 604:    */     }
/* 605:    */     finally
/* 606:    */     {
/* 607:662 */       switch (state)
/* 608:    */       {
/* 609:    */       case 0: 
/* 610:664 */         k.cancel();
/* 611:665 */         invokeChannelUnregistered(task, k, null);
/* 612:666 */         break;
/* 613:    */       case 1: 
/* 614:668 */         if (!k.isValid()) {
/* 615:669 */           invokeChannelUnregistered(task, k, null);
/* 616:    */         }
/* 617:    */         break;
/* 618:    */       }
/* 619:    */     }
/* 620:    */   }
/* 621:    */   
/* 622:    */   private void closeAll()
/* 623:    */   {
/* 624:677 */     selectAgain();
/* 625:678 */     Set<SelectionKey> keys = this.selector.keys();
/* 626:679 */     Collection<AbstractNioChannel> channels = new ArrayList(keys.size());
/* 627:680 */     for (SelectionKey k : keys)
/* 628:    */     {
/* 629:681 */       Object a = k.attachment();
/* 630:682 */       if ((a instanceof AbstractNioChannel))
/* 631:    */       {
/* 632:683 */         channels.add((AbstractNioChannel)a);
/* 633:    */       }
/* 634:    */       else
/* 635:    */       {
/* 636:685 */         k.cancel();
/* 637:    */         
/* 638:687 */         NioTask<SelectableChannel> task = (NioTask)a;
/* 639:688 */         invokeChannelUnregistered(task, k, null);
/* 640:    */       }
/* 641:    */     }
/* 642:692 */     for (AbstractNioChannel ch : channels) {
/* 643:693 */       ch.unsafe().close(ch.unsafe().voidPromise());
/* 644:    */     }
/* 645:    */   }
/* 646:    */   
/* 647:    */   private static void invokeChannelUnregistered(NioTask<SelectableChannel> task, SelectionKey k, Throwable cause)
/* 648:    */   {
/* 649:    */     try
/* 650:    */     {
/* 651:699 */       task.channelUnregistered(k.channel(), cause);
/* 652:    */     }
/* 653:    */     catch (Exception e)
/* 654:    */     {
/* 655:701 */       logger.warn("Unexpected exception while running NioTask.channelUnregistered()", e);
/* 656:    */     }
/* 657:    */   }
/* 658:    */   
/* 659:    */   protected void wakeup(boolean inEventLoop)
/* 660:    */   {
/* 661:707 */     if ((!inEventLoop) && (this.wakenUp.compareAndSet(false, true))) {
/* 662:708 */       this.selector.wakeup();
/* 663:    */     }
/* 664:    */   }
/* 665:    */   
/* 666:    */   Selector unwrappedSelector()
/* 667:    */   {
/* 668:713 */     return this.unwrappedSelector;
/* 669:    */   }
/* 670:    */   
/* 671:    */   int selectNow()
/* 672:    */     throws IOException
/* 673:    */   {
/* 674:    */     try
/* 675:    */     {
/* 676:718 */       return this.selector.selectNow();
/* 677:    */     }
/* 678:    */     finally
/* 679:    */     {
/* 680:721 */       if (this.wakenUp.get()) {
/* 681:722 */         this.selector.wakeup();
/* 682:    */       }
/* 683:    */     }
/* 684:    */   }
/* 685:    */   
/* 686:    */   private void select(boolean oldWakenUp)
/* 687:    */     throws IOException
/* 688:    */   {
/* 689:728 */     Selector selector = this.selector;
/* 690:    */     try
/* 691:    */     {
/* 692:730 */       int selectCnt = 0;
/* 693:731 */       long currentTimeNanos = System.nanoTime();
/* 694:732 */       long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);
/* 695:    */       for (;;)
/* 696:    */       {
/* 697:734 */         long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
/* 698:735 */         if (timeoutMillis <= 0L)
/* 699:    */         {
/* 700:736 */           if (selectCnt != 0) {
/* 701:    */             break;
/* 702:    */           }
/* 703:737 */           selector.selectNow();
/* 704:738 */           selectCnt = 1; break;
/* 705:    */         }
/* 706:747 */         if ((hasTasks()) && (this.wakenUp.compareAndSet(false, true)))
/* 707:    */         {
/* 708:748 */           selector.selectNow();
/* 709:749 */           selectCnt = 1;
/* 710:750 */           break;
/* 711:    */         }
/* 712:753 */         int selectedKeys = selector.select(timeoutMillis);
/* 713:754 */         selectCnt++;
/* 714:756 */         if ((selectedKeys != 0) || (oldWakenUp) || (this.wakenUp.get()) || (hasTasks()) || (hasScheduledTasks())) {
/* 715:    */           break;
/* 716:    */         }
/* 717:763 */         if (Thread.interrupted())
/* 718:    */         {
/* 719:769 */           if (logger.isDebugEnabled()) {
/* 720:770 */             logger.debug("Selector.select() returned prematurely because Thread.currentThread().interrupt() was called. Use NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
/* 721:    */           }
/* 722:774 */           selectCnt = 1;
/* 723:775 */           break;
/* 724:    */         }
/* 725:778 */         long time = System.nanoTime();
/* 726:779 */         if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos)
/* 727:    */         {
/* 728:781 */           selectCnt = 1;
/* 729:    */         }
/* 730:782 */         else if ((SELECTOR_AUTO_REBUILD_THRESHOLD > 0) && (selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD))
/* 731:    */         {
/* 732:786 */           logger.warn("Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.", 
/* 733:    */           
/* 734:788 */             Integer.valueOf(selectCnt), selector);
/* 735:    */           
/* 736:790 */           rebuildSelector();
/* 737:791 */           selector = this.selector;
/* 738:    */           
/* 739:    */ 
/* 740:794 */           selector.selectNow();
/* 741:795 */           selectCnt = 1;
/* 742:796 */           break;
/* 743:    */         }
/* 744:799 */         currentTimeNanos = time;
/* 745:    */       }
/* 746:802 */       if ((selectCnt > 3) && 
/* 747:803 */         (logger.isDebugEnabled())) {
/* 748:804 */         logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.", 
/* 749:805 */           Integer.valueOf(selectCnt - 1), selector);
/* 750:    */       }
/* 751:    */     }
/* 752:    */     catch (CancelledKeyException e)
/* 753:    */     {
/* 754:809 */       if (logger.isDebugEnabled()) {
/* 755:810 */         logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?", selector, e);
/* 756:    */       }
/* 757:    */     }
/* 758:    */   }
/* 759:    */   
/* 760:    */   private void selectAgain()
/* 761:    */   {
/* 762:818 */     this.needsToSelectAgain = false;
/* 763:    */     try
/* 764:    */     {
/* 765:820 */       this.selector.selectNow();
/* 766:    */     }
/* 767:    */     catch (Throwable t)
/* 768:    */     {
/* 769:822 */       logger.warn("Failed to update SelectionKeys.", t);
/* 770:    */     }
/* 771:    */   }
/* 772:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.nio.NioEventLoop
 * JD-Core Version:    0.7.0.1
 */