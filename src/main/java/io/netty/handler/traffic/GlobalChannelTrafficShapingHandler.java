/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.util.Attribute;
/*  10:    */ import io.netty.util.concurrent.EventExecutor;
/*  11:    */ import io.netty.util.internal.PlatformDependent;
/*  12:    */ import io.netty.util.internal.logging.InternalLogger;
/*  13:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  14:    */ import java.util.AbstractCollection;
/*  15:    */ import java.util.ArrayDeque;
/*  16:    */ import java.util.Collection;
/*  17:    */ import java.util.Iterator;
/*  18:    */ import java.util.concurrent.ConcurrentMap;
/*  19:    */ import java.util.concurrent.ScheduledExecutorService;
/*  20:    */ import java.util.concurrent.TimeUnit;
/*  21:    */ import java.util.concurrent.atomic.AtomicLong;
/*  22:    */ 
/*  23:    */ @ChannelHandler.Sharable
/*  24:    */ public class GlobalChannelTrafficShapingHandler
/*  25:    */   extends AbstractTrafficShapingHandler
/*  26:    */ {
/*  27: 89 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(GlobalChannelTrafficShapingHandler.class);
/*  28: 93 */   final ConcurrentMap<Integer, PerChannel> channelQueues = PlatformDependent.newConcurrentHashMap();
/*  29: 98 */   private final AtomicLong queuesSize = new AtomicLong();
/*  30:103 */   private final AtomicLong cumulativeWrittenBytes = new AtomicLong();
/*  31:108 */   private final AtomicLong cumulativeReadBytes = new AtomicLong();
/*  32:114 */   volatile long maxGlobalWriteSize = 419430400L;
/*  33:    */   private volatile long writeChannelLimit;
/*  34:    */   private volatile long readChannelLimit;
/*  35:    */   private static final float DEFAULT_DEVIATION = 0.1F;
/*  36:    */   private static final float MAX_DEVIATION = 0.4F;
/*  37:    */   private static final float DEFAULT_SLOWDOWN = 0.4F;
/*  38:    */   private static final float DEFAULT_ACCELERATION = -0.1F;
/*  39:    */   private volatile float maxDeviation;
/*  40:    */   private volatile float accelerationFactor;
/*  41:    */   private volatile float slowDownFactor;
/*  42:    */   private volatile boolean readDeviationActive;
/*  43:    */   private volatile boolean writeDeviationActive;
/*  44:    */   
/*  45:    */   void createGlobalTrafficCounter(ScheduledExecutorService executor)
/*  46:    */   {
/*  47:149 */     setMaxDeviation(0.1F, 0.4F, -0.1F);
/*  48:150 */     if (executor == null) {
/*  49:151 */       throw new IllegalArgumentException("Executor must not be null");
/*  50:    */     }
/*  51:153 */     TrafficCounter tc = new GlobalChannelTrafficCounter(this, executor, "GlobalChannelTC", this.checkInterval);
/*  52:154 */     setTrafficCounter(tc);
/*  53:155 */     tc.start();
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected int userDefinedWritabilityIndex()
/*  57:    */   {
/*  58:160 */     return 3;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit, long writeChannelLimit, long readChannelLimit, long checkInterval, long maxTime)
/*  62:    */   {
/*  63:186 */     super(writeGlobalLimit, readGlobalLimit, checkInterval, maxTime);
/*  64:187 */     createGlobalTrafficCounter(executor);
/*  65:188 */     this.writeChannelLimit = writeChannelLimit;
/*  66:189 */     this.readChannelLimit = readChannelLimit;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit, long writeChannelLimit, long readChannelLimit, long checkInterval)
/*  70:    */   {
/*  71:213 */     super(writeGlobalLimit, readGlobalLimit, checkInterval);
/*  72:214 */     this.writeChannelLimit = writeChannelLimit;
/*  73:215 */     this.readChannelLimit = readChannelLimit;
/*  74:216 */     createGlobalTrafficCounter(executor);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long writeGlobalLimit, long readGlobalLimit, long writeChannelLimit, long readChannelLimit)
/*  78:    */   {
/*  79:236 */     super(writeGlobalLimit, readGlobalLimit);
/*  80:237 */     this.writeChannelLimit = writeChannelLimit;
/*  81:238 */     this.readChannelLimit = readChannelLimit;
/*  82:239 */     createGlobalTrafficCounter(executor);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor, long checkInterval)
/*  86:    */   {
/*  87:252 */     super(checkInterval);
/*  88:253 */     createGlobalTrafficCounter(executor);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public GlobalChannelTrafficShapingHandler(ScheduledExecutorService executor)
/*  92:    */   {
/*  93:263 */     createGlobalTrafficCounter(executor);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public float maxDeviation()
/*  97:    */   {
/*  98:270 */     return this.maxDeviation;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public float accelerationFactor()
/* 102:    */   {
/* 103:277 */     return this.accelerationFactor;
/* 104:    */   }
/* 105:    */   
/* 106:    */   public float slowDownFactor()
/* 107:    */   {
/* 108:284 */     return this.slowDownFactor;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public void setMaxDeviation(float maxDeviation, float slowDownFactor, float accelerationFactor)
/* 112:    */   {
/* 113:299 */     if (maxDeviation > 0.4F) {
/* 114:300 */       throw new IllegalArgumentException("maxDeviation must be <= 0.4");
/* 115:    */     }
/* 116:302 */     if (slowDownFactor < 0.0F) {
/* 117:303 */       throw new IllegalArgumentException("slowDownFactor must be >= 0");
/* 118:    */     }
/* 119:305 */     if (accelerationFactor > 0.0F) {
/* 120:306 */       throw new IllegalArgumentException("accelerationFactor must be <= 0");
/* 121:    */     }
/* 122:308 */     this.maxDeviation = maxDeviation;
/* 123:309 */     this.accelerationFactor = (1.0F + accelerationFactor);
/* 124:310 */     this.slowDownFactor = (1.0F + slowDownFactor);
/* 125:    */   }
/* 126:    */   
/* 127:    */   private void computeDeviationCumulativeBytes()
/* 128:    */   {
/* 129:315 */     long maxWrittenBytes = 0L;
/* 130:316 */     long maxReadBytes = 0L;
/* 131:317 */     long minWrittenBytes = 9223372036854775807L;
/* 132:318 */     long minReadBytes = 9223372036854775807L;
/* 133:319 */     for (PerChannel perChannel : this.channelQueues.values())
/* 134:    */     {
/* 135:320 */       long value = perChannel.channelTrafficCounter.cumulativeWrittenBytes();
/* 136:321 */       if (maxWrittenBytes < value) {
/* 137:322 */         maxWrittenBytes = value;
/* 138:    */       }
/* 139:324 */       if (minWrittenBytes > value) {
/* 140:325 */         minWrittenBytes = value;
/* 141:    */       }
/* 142:327 */       value = perChannel.channelTrafficCounter.cumulativeReadBytes();
/* 143:328 */       if (maxReadBytes < value) {
/* 144:329 */         maxReadBytes = value;
/* 145:    */       }
/* 146:331 */       if (minReadBytes > value) {
/* 147:332 */         minReadBytes = value;
/* 148:    */       }
/* 149:    */     }
/* 150:335 */     boolean multiple = this.channelQueues.size() > 1;
/* 151:336 */     this.readDeviationActive = ((multiple) && (minReadBytes < maxReadBytes / 2L));
/* 152:337 */     this.writeDeviationActive = ((multiple) && (minWrittenBytes < maxWrittenBytes / 2L));
/* 153:338 */     this.cumulativeWrittenBytes.set(maxWrittenBytes);
/* 154:339 */     this.cumulativeReadBytes.set(maxReadBytes);
/* 155:    */   }
/* 156:    */   
/* 157:    */   protected void doAccounting(TrafficCounter counter)
/* 158:    */   {
/* 159:344 */     computeDeviationCumulativeBytes();
/* 160:345 */     super.doAccounting(counter);
/* 161:    */   }
/* 162:    */   
/* 163:    */   private long computeBalancedWait(float maxLocal, float maxGlobal, long wait)
/* 164:    */   {
/* 165:349 */     if (maxGlobal == 0.0F) {
/* 166:351 */       return wait;
/* 167:    */     }
/* 168:353 */     float ratio = maxLocal / maxGlobal;
/* 169:355 */     if (ratio > this.maxDeviation)
/* 170:    */     {
/* 171:356 */       if (ratio < 1.0F - this.maxDeviation) {
/* 172:357 */         return wait;
/* 173:    */       }
/* 174:359 */       ratio = this.slowDownFactor;
/* 175:360 */       if (wait < 10L) {
/* 176:361 */         wait = 10L;
/* 177:    */       }
/* 178:    */     }
/* 179:    */     else
/* 180:    */     {
/* 181:365 */       ratio = this.accelerationFactor;
/* 182:    */     }
/* 183:367 */     return ((float)wait * ratio);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public long getMaxGlobalWriteSize()
/* 187:    */   {
/* 188:374 */     return this.maxGlobalWriteSize;
/* 189:    */   }
/* 190:    */   
/* 191:    */   public void setMaxGlobalWriteSize(long maxGlobalWriteSize)
/* 192:    */   {
/* 193:388 */     if (maxGlobalWriteSize <= 0L) {
/* 194:389 */       throw new IllegalArgumentException("maxGlobalWriteSize must be positive");
/* 195:    */     }
/* 196:391 */     this.maxGlobalWriteSize = maxGlobalWriteSize;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public long queuesSize()
/* 200:    */   {
/* 201:398 */     return this.queuesSize.get();
/* 202:    */   }
/* 203:    */   
/* 204:    */   public void configureChannel(long newWriteLimit, long newReadLimit)
/* 205:    */   {
/* 206:406 */     this.writeChannelLimit = newWriteLimit;
/* 207:407 */     this.readChannelLimit = newReadLimit;
/* 208:408 */     long now = TrafficCounter.milliSecondFromNano();
/* 209:409 */     for (PerChannel perChannel : this.channelQueues.values()) {
/* 210:410 */       perChannel.channelTrafficCounter.resetAccounting(now);
/* 211:    */     }
/* 212:    */   }
/* 213:    */   
/* 214:    */   public long getWriteChannelLimit()
/* 215:    */   {
/* 216:418 */     return this.writeChannelLimit;
/* 217:    */   }
/* 218:    */   
/* 219:    */   public void setWriteChannelLimit(long writeLimit)
/* 220:    */   {
/* 221:425 */     this.writeChannelLimit = writeLimit;
/* 222:426 */     long now = TrafficCounter.milliSecondFromNano();
/* 223:427 */     for (PerChannel perChannel : this.channelQueues.values()) {
/* 224:428 */       perChannel.channelTrafficCounter.resetAccounting(now);
/* 225:    */     }
/* 226:    */   }
/* 227:    */   
/* 228:    */   public long getReadChannelLimit()
/* 229:    */   {
/* 230:436 */     return this.readChannelLimit;
/* 231:    */   }
/* 232:    */   
/* 233:    */   public void setReadChannelLimit(long readLimit)
/* 234:    */   {
/* 235:443 */     this.readChannelLimit = readLimit;
/* 236:444 */     long now = TrafficCounter.milliSecondFromNano();
/* 237:445 */     for (PerChannel perChannel : this.channelQueues.values()) {
/* 238:446 */       perChannel.channelTrafficCounter.resetAccounting(now);
/* 239:    */     }
/* 240:    */   }
/* 241:    */   
/* 242:    */   public final void release()
/* 243:    */   {
/* 244:454 */     this.trafficCounter.stop();
/* 245:    */   }
/* 246:    */   
/* 247:    */   private PerChannel getOrSetPerChannel(ChannelHandlerContext ctx)
/* 248:    */   {
/* 249:459 */     Channel channel = ctx.channel();
/* 250:460 */     Integer key = Integer.valueOf(channel.hashCode());
/* 251:461 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 252:462 */     if (perChannel == null)
/* 253:    */     {
/* 254:463 */       perChannel = new PerChannel();
/* 255:464 */       perChannel.messagesQueue = new ArrayDeque();
/* 256:    */       
/* 257:    */ 
/* 258:467 */       perChannel.channelTrafficCounter = new TrafficCounter(this, null, "ChannelTC" + ctx.channel().hashCode(), this.checkInterval);
/* 259:468 */       perChannel.queueSize = 0L;
/* 260:469 */       perChannel.lastReadTimestamp = TrafficCounter.milliSecondFromNano();
/* 261:470 */       perChannel.lastWriteTimestamp = perChannel.lastReadTimestamp;
/* 262:471 */       this.channelQueues.put(key, perChannel);
/* 263:    */     }
/* 264:473 */     return perChannel;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 268:    */     throws Exception
/* 269:    */   {
/* 270:478 */     getOrSetPerChannel(ctx);
/* 271:479 */     this.trafficCounter.resetCumulativeTime();
/* 272:480 */     super.handlerAdded(ctx);
/* 273:    */   }
/* 274:    */   
/* 275:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 276:    */     throws Exception
/* 277:    */   {
/* 278:485 */     this.trafficCounter.resetCumulativeTime();
/* 279:486 */     Channel channel = ctx.channel();
/* 280:487 */     Integer key = Integer.valueOf(channel.hashCode());
/* 281:488 */     PerChannel perChannel = (PerChannel)this.channelQueues.remove(key);
/* 282:489 */     if (perChannel != null) {
/* 283:491 */       synchronized (perChannel)
/* 284:    */       {
/* 285:492 */         if (channel.isActive())
/* 286:    */         {
/* 287:493 */           for (ToSend toSend : perChannel.messagesQueue)
/* 288:    */           {
/* 289:494 */             long size = calculateSize(toSend.toSend);
/* 290:495 */             this.trafficCounter.bytesRealWriteFlowControl(size);
/* 291:496 */             perChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
/* 292:497 */             perChannel.queueSize -= size;
/* 293:498 */             this.queuesSize.addAndGet(-size);
/* 294:499 */             ctx.write(toSend.toSend, toSend.promise);
/* 295:    */           }
/* 296:    */         }
/* 297:    */         else
/* 298:    */         {
/* 299:502 */           this.queuesSize.addAndGet(-perChannel.queueSize);
/* 300:503 */           for (ToSend toSend : perChannel.messagesQueue) {
/* 301:504 */             if ((toSend.toSend instanceof ByteBuf)) {
/* 302:505 */               ((ByteBuf)toSend.toSend).release();
/* 303:    */             }
/* 304:    */           }
/* 305:    */         }
/* 306:509 */         perChannel.messagesQueue.clear();
/* 307:    */       }
/* 308:    */     }
/* 309:512 */     releaseWriteSuspended(ctx);
/* 310:513 */     releaseReadSuspended(ctx);
/* 311:514 */     super.handlerRemoved(ctx);
/* 312:    */   }
/* 313:    */   
/* 314:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 315:    */     throws Exception
/* 316:    */   {
/* 317:519 */     long size = calculateSize(msg);
/* 318:520 */     long now = TrafficCounter.milliSecondFromNano();
/* 319:521 */     if (size > 0L)
/* 320:    */     {
/* 321:523 */       long waitGlobal = this.trafficCounter.readTimeToWait(size, getReadLimit(), this.maxTime, now);
/* 322:524 */       Integer key = Integer.valueOf(ctx.channel().hashCode());
/* 323:525 */       PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 324:526 */       long wait = 0L;
/* 325:527 */       if (perChannel != null)
/* 326:    */       {
/* 327:528 */         wait = perChannel.channelTrafficCounter.readTimeToWait(size, this.readChannelLimit, this.maxTime, now);
/* 328:529 */         if (this.readDeviationActive)
/* 329:    */         {
/* 330:532 */           long maxLocalRead = perChannel.channelTrafficCounter.cumulativeReadBytes();
/* 331:533 */           long maxGlobalRead = this.cumulativeReadBytes.get();
/* 332:534 */           if (maxLocalRead <= 0L) {
/* 333:535 */             maxLocalRead = 0L;
/* 334:    */           }
/* 335:537 */           if (maxGlobalRead < maxLocalRead) {
/* 336:538 */             maxGlobalRead = maxLocalRead;
/* 337:    */           }
/* 338:540 */           wait = computeBalancedWait((float)maxLocalRead, (float)maxGlobalRead, wait);
/* 339:    */         }
/* 340:    */       }
/* 341:543 */       if (wait < waitGlobal) {
/* 342:544 */         wait = waitGlobal;
/* 343:    */       }
/* 344:546 */       wait = checkWaitReadTime(ctx, wait, now);
/* 345:547 */       if (wait >= 10L)
/* 346:    */       {
/* 347:550 */         ChannelConfig config = ctx.channel().config();
/* 348:551 */         if (logger.isDebugEnabled()) {
/* 349:552 */           logger.debug("Read Suspend: " + wait + ':' + config.isAutoRead() + ':' + 
/* 350:553 */             isHandlerActive(ctx));
/* 351:    */         }
/* 352:555 */         if ((config.isAutoRead()) && (isHandlerActive(ctx)))
/* 353:    */         {
/* 354:556 */           config.setAutoRead(false);
/* 355:557 */           ctx.attr(READ_SUSPENDED).set(Boolean.valueOf(true));
/* 356:    */           
/* 357:    */ 
/* 358:560 */           Attribute<Runnable> attr = ctx.attr(REOPEN_TASK);
/* 359:561 */           Runnable reopenTask = (Runnable)attr.get();
/* 360:562 */           if (reopenTask == null)
/* 361:    */           {
/* 362:563 */             reopenTask = new AbstractTrafficShapingHandler.ReopenReadTimerTask(ctx);
/* 363:564 */             attr.set(reopenTask);
/* 364:    */           }
/* 365:566 */           ctx.executor().schedule(reopenTask, wait, TimeUnit.MILLISECONDS);
/* 366:567 */           if (logger.isDebugEnabled()) {
/* 367:568 */             logger.debug("Suspend final status => " + config.isAutoRead() + ':' + 
/* 368:569 */               isHandlerActive(ctx) + " will reopened at: " + wait);
/* 369:    */           }
/* 370:    */         }
/* 371:    */       }
/* 372:    */     }
/* 373:574 */     informReadOperation(ctx, now);
/* 374:575 */     ctx.fireChannelRead(msg);
/* 375:    */   }
/* 376:    */   
/* 377:    */   protected long checkWaitReadTime(ChannelHandlerContext ctx, long wait, long now)
/* 378:    */   {
/* 379:580 */     Integer key = Integer.valueOf(ctx.channel().hashCode());
/* 380:581 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 381:582 */     if ((perChannel != null) && 
/* 382:583 */       (wait > this.maxTime) && (now + wait - perChannel.lastReadTimestamp > this.maxTime)) {
/* 383:584 */       wait = this.maxTime;
/* 384:    */     }
/* 385:587 */     return wait;
/* 386:    */   }
/* 387:    */   
/* 388:    */   protected void informReadOperation(ChannelHandlerContext ctx, long now)
/* 389:    */   {
/* 390:592 */     Integer key = Integer.valueOf(ctx.channel().hashCode());
/* 391:593 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 392:594 */     if (perChannel != null) {
/* 393:595 */       perChannel.lastReadTimestamp = now;
/* 394:    */     }
/* 395:    */   }
/* 396:    */   
/* 397:    */   private static final class ToSend
/* 398:    */   {
/* 399:    */     final long relativeTimeAction;
/* 400:    */     final Object toSend;
/* 401:    */     final ChannelPromise promise;
/* 402:    */     final long size;
/* 403:    */     
/* 404:    */     private ToSend(long delay, Object toSend, long size, ChannelPromise promise)
/* 405:    */     {
/* 406:606 */       this.relativeTimeAction = delay;
/* 407:607 */       this.toSend = toSend;
/* 408:608 */       this.size = size;
/* 409:609 */       this.promise = promise;
/* 410:    */     }
/* 411:    */   }
/* 412:    */   
/* 413:    */   protected long maximumCumulativeWrittenBytes()
/* 414:    */   {
/* 415:614 */     return this.cumulativeWrittenBytes.get();
/* 416:    */   }
/* 417:    */   
/* 418:    */   protected long maximumCumulativeReadBytes()
/* 419:    */   {
/* 420:618 */     return this.cumulativeReadBytes.get();
/* 421:    */   }
/* 422:    */   
/* 423:    */   public Collection<TrafficCounter> channelTrafficCounters()
/* 424:    */   {
/* 425:626 */     new AbstractCollection()
/* 426:    */     {
/* 427:    */       public Iterator<TrafficCounter> iterator()
/* 428:    */       {
/* 429:629 */         new Iterator()
/* 430:    */         {
/* 431:630 */           final Iterator<GlobalChannelTrafficShapingHandler.PerChannel> iter = GlobalChannelTrafficShapingHandler.this.channelQueues.values().iterator();
/* 432:    */           
/* 433:    */           public boolean hasNext()
/* 434:    */           {
/* 435:633 */             return this.iter.hasNext();
/* 436:    */           }
/* 437:    */           
/* 438:    */           public TrafficCounter next()
/* 439:    */           {
/* 440:637 */             return ((GlobalChannelTrafficShapingHandler.PerChannel)this.iter.next()).channelTrafficCounter;
/* 441:    */           }
/* 442:    */           
/* 443:    */           public void remove()
/* 444:    */           {
/* 445:641 */             throw new UnsupportedOperationException();
/* 446:    */           }
/* 447:    */         };
/* 448:    */       }
/* 449:    */       
/* 450:    */       public int size()
/* 451:    */       {
/* 452:647 */         return GlobalChannelTrafficShapingHandler.this.channelQueues.size();
/* 453:    */       }
/* 454:    */     };
/* 455:    */   }
/* 456:    */   
/* 457:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 458:    */     throws Exception
/* 459:    */   {
/* 460:655 */     long size = calculateSize(msg);
/* 461:656 */     long now = TrafficCounter.milliSecondFromNano();
/* 462:657 */     if (size > 0L)
/* 463:    */     {
/* 464:659 */       long waitGlobal = this.trafficCounter.writeTimeToWait(size, getWriteLimit(), this.maxTime, now);
/* 465:660 */       Integer key = Integer.valueOf(ctx.channel().hashCode());
/* 466:661 */       PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 467:662 */       long wait = 0L;
/* 468:663 */       if (perChannel != null)
/* 469:    */       {
/* 470:664 */         wait = perChannel.channelTrafficCounter.writeTimeToWait(size, this.writeChannelLimit, this.maxTime, now);
/* 471:665 */         if (this.writeDeviationActive)
/* 472:    */         {
/* 473:668 */           long maxLocalWrite = perChannel.channelTrafficCounter.cumulativeWrittenBytes();
/* 474:669 */           long maxGlobalWrite = this.cumulativeWrittenBytes.get();
/* 475:670 */           if (maxLocalWrite <= 0L) {
/* 476:671 */             maxLocalWrite = 0L;
/* 477:    */           }
/* 478:673 */           if (maxGlobalWrite < maxLocalWrite) {
/* 479:674 */             maxGlobalWrite = maxLocalWrite;
/* 480:    */           }
/* 481:676 */           wait = computeBalancedWait((float)maxLocalWrite, (float)maxGlobalWrite, wait);
/* 482:    */         }
/* 483:    */       }
/* 484:679 */       if (wait < waitGlobal) {
/* 485:680 */         wait = waitGlobal;
/* 486:    */       }
/* 487:682 */       if (wait >= 10L)
/* 488:    */       {
/* 489:683 */         if (logger.isDebugEnabled()) {
/* 490:684 */           logger.debug("Write suspend: " + wait + ':' + ctx.channel().config().isAutoRead() + ':' + 
/* 491:685 */             isHandlerActive(ctx));
/* 492:    */         }
/* 493:687 */         submitWrite(ctx, msg, size, wait, now, promise);
/* 494:688 */         return;
/* 495:    */       }
/* 496:    */     }
/* 497:692 */     submitWrite(ctx, msg, size, 0L, now, promise);
/* 498:    */   }
/* 499:    */   
/* 500:    */   protected void submitWrite(final ChannelHandlerContext ctx, Object msg, long size, long writedelay, long now, ChannelPromise promise)
/* 501:    */   {
/* 502:699 */     Channel channel = ctx.channel();
/* 503:700 */     Integer key = Integer.valueOf(channel.hashCode());
/* 504:701 */     PerChannel perChannel = (PerChannel)this.channelQueues.get(key);
/* 505:702 */     if (perChannel == null) {
/* 506:705 */       perChannel = getOrSetPerChannel(ctx);
/* 507:    */     }
/* 508:708 */     long delay = writedelay;
/* 509:709 */     boolean globalSizeExceeded = false;
/* 510:711 */     synchronized (perChannel)
/* 511:    */     {
/* 512:712 */       if ((writedelay == 0L) && (perChannel.messagesQueue.isEmpty()))
/* 513:    */       {
/* 514:713 */         this.trafficCounter.bytesRealWriteFlowControl(size);
/* 515:714 */         perChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
/* 516:715 */         ctx.write(msg, promise);
/* 517:716 */         perChannel.lastWriteTimestamp = now;
/* 518:717 */         return;
/* 519:    */       }
/* 520:719 */       if ((delay > this.maxTime) && (now + delay - perChannel.lastWriteTimestamp > this.maxTime)) {
/* 521:720 */         delay = this.maxTime;
/* 522:    */       }
/* 523:722 */       ToSend newToSend = new ToSend(delay + now, msg, size, promise, null);
/* 524:723 */       perChannel.messagesQueue.addLast(newToSend);
/* 525:724 */       perChannel.queueSize += size;
/* 526:725 */       this.queuesSize.addAndGet(size);
/* 527:726 */       checkWriteSuspend(ctx, delay, perChannel.queueSize);
/* 528:727 */       if (this.queuesSize.get() > this.maxGlobalWriteSize) {
/* 529:728 */         globalSizeExceeded = true;
/* 530:    */       }
/* 531:    */     }
/* 532:    */     ToSend newToSend;
/* 533:731 */     if (globalSizeExceeded) {
/* 534:732 */       setUserDefinedWritability(ctx, false);
/* 535:    */     }
/* 536:734 */     final long futureNow = newToSend.relativeTimeAction;
/* 537:735 */     final PerChannel forSchedule = perChannel;
/* 538:736 */     ctx.executor().schedule(new Runnable()
/* 539:    */     {
/* 540:    */       public void run()
/* 541:    */       {
/* 542:739 */         GlobalChannelTrafficShapingHandler.this.sendAllValid(ctx, forSchedule, futureNow);
/* 543:    */       }
/* 544:739 */     }, delay, TimeUnit.MILLISECONDS);
/* 545:    */   }
/* 546:    */   
/* 547:    */   private void sendAllValid(ChannelHandlerContext ctx, PerChannel perChannel, long now)
/* 548:    */   {
/* 549:746 */     synchronized (perChannel)
/* 550:    */     {
/* 551:747 */       for (ToSend newToSend = (ToSend)perChannel.messagesQueue.pollFirst(); newToSend != null; newToSend = (ToSend)perChannel.messagesQueue.pollFirst()) {
/* 552:749 */         if (newToSend.relativeTimeAction <= now)
/* 553:    */         {
/* 554:750 */           long size = newToSend.size;
/* 555:751 */           this.trafficCounter.bytesRealWriteFlowControl(size);
/* 556:752 */           perChannel.channelTrafficCounter.bytesRealWriteFlowControl(size);
/* 557:753 */           perChannel.queueSize -= size;
/* 558:754 */           this.queuesSize.addAndGet(-size);
/* 559:755 */           ctx.write(newToSend.toSend, newToSend.promise);
/* 560:756 */           perChannel.lastWriteTimestamp = now;
/* 561:    */         }
/* 562:    */         else
/* 563:    */         {
/* 564:758 */           perChannel.messagesQueue.addFirst(newToSend);
/* 565:759 */           break;
/* 566:    */         }
/* 567:    */       }
/* 568:762 */       if (perChannel.messagesQueue.isEmpty()) {
/* 569:763 */         releaseWriteSuspended(ctx);
/* 570:    */       }
/* 571:    */     }
/* 572:766 */     ctx.flush();
/* 573:    */   }
/* 574:    */   
/* 575:    */   public String toString()
/* 576:    */   {
/* 577:771 */     return 
/* 578:    */     
/* 579:773 */       340 + super.toString() + " Write Channel Limit: " + this.writeChannelLimit + " Read Channel Limit: " + this.readChannelLimit;
/* 580:    */   }
/* 581:    */   
/* 582:    */   static final class PerChannel
/* 583:    */   {
/* 584:    */     ArrayDeque<GlobalChannelTrafficShapingHandler.ToSend> messagesQueue;
/* 585:    */     TrafficCounter channelTrafficCounter;
/* 586:    */     long queueSize;
/* 587:    */     long lastWriteTimestamp;
/* 588:    */     long lastReadTimestamp;
/* 589:    */   }
/* 590:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.traffic.GlobalChannelTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */