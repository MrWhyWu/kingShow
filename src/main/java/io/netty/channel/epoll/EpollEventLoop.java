/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel.Unsafe;
/*   4:    */ import io.netty.channel.EventLoopGroup;
/*   5:    */ import io.netty.channel.SelectStrategy;
/*   6:    */ import io.netty.channel.SingleThreadEventLoop;
/*   7:    */ import io.netty.channel.unix.FileDescriptor;
/*   8:    */ import io.netty.channel.unix.IovArray;
/*   9:    */ import io.netty.util.IntSupplier;
/*  10:    */ import io.netty.util.collection.IntObjectHashMap;
/*  11:    */ import io.netty.util.collection.IntObjectMap;
/*  12:    */ import io.netty.util.concurrent.Future;
/*  13:    */ import io.netty.util.concurrent.RejectedExecutionHandler;
/*  14:    */ import io.netty.util.internal.ObjectUtil;
/*  15:    */ import io.netty.util.internal.PlatformDependent;
/*  16:    */ import io.netty.util.internal.logging.InternalLogger;
/*  17:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  18:    */ import java.io.IOException;
/*  19:    */ import java.util.ArrayList;
/*  20:    */ import java.util.Collection;
/*  21:    */ import java.util.Queue;
/*  22:    */ import java.util.concurrent.Callable;
/*  23:    */ import java.util.concurrent.Executor;
/*  24:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  25:    */ 
/*  26:    */ final class EpollEventLoop
/*  27:    */   extends SingleThreadEventLoop
/*  28:    */ {
/*  29:    */   private static final InternalLogger logger;
/*  30:    */   private static final AtomicIntegerFieldUpdater<EpollEventLoop> WAKEN_UP_UPDATER;
/*  31:    */   private final FileDescriptor epollFd;
/*  32:    */   private final FileDescriptor eventFd;
/*  33:    */   private final FileDescriptor timerFd;
/*  34:    */   
/*  35:    */   static
/*  36:    */   {
/*  37: 48 */     logger = InternalLoggerFactory.getInstance(EpollEventLoop.class);
/*  38:    */     
/*  39: 50 */     WAKEN_UP_UPDATER = AtomicIntegerFieldUpdater.newUpdater(EpollEventLoop.class, "wakenUp");
/*  40:    */     
/*  41:    */ 
/*  42:    */ 
/*  43:    */ 
/*  44: 55 */     Epoll.ensureAvailability();
/*  45:    */   }
/*  46:    */   
/*  47: 61 */   private final IntObjectMap<AbstractEpollChannel> channels = new IntObjectHashMap(4096);
/*  48:    */   private final boolean allowGrowing;
/*  49:    */   private final EpollEventArray events;
/*  50: 64 */   private final IovArray iovArray = new IovArray();
/*  51:    */   private final SelectStrategy selectStrategy;
/*  52: 66 */   private final IntSupplier selectNowSupplier = new IntSupplier()
/*  53:    */   {
/*  54:    */     public int get()
/*  55:    */       throws Exception
/*  56:    */     {
/*  57: 69 */       return EpollEventLoop.this.epollWaitNow();
/*  58:    */     }
/*  59:    */   };
/*  60: 72 */   private final Callable<Integer> pendingTasksCallable = new Callable()
/*  61:    */   {
/*  62:    */     public Integer call()
/*  63:    */       throws Exception
/*  64:    */     {
/*  65: 75 */       return Integer.valueOf(EpollEventLoop.this.pendingTasks());
/*  66:    */     }
/*  67:    */   };
/*  68:    */   private volatile int wakenUp;
/*  69: 79 */   private volatile int ioRatio = 50;
/*  70:    */   
/*  71:    */   EpollEventLoop(EventLoopGroup parent, Executor executor, int maxEvents, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler)
/*  72:    */   {
/*  73: 83 */     super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
/*  74: 84 */     this.selectStrategy = ((SelectStrategy)ObjectUtil.checkNotNull(strategy, "strategy"));
/*  75: 85 */     if (maxEvents == 0)
/*  76:    */     {
/*  77: 86 */       this.allowGrowing = true;
/*  78: 87 */       this.events = new EpollEventArray(4096);
/*  79:    */     }
/*  80:    */     else
/*  81:    */     {
/*  82: 89 */       this.allowGrowing = false;
/*  83: 90 */       this.events = new EpollEventArray(maxEvents);
/*  84:    */     }
/*  85: 92 */     boolean success = false;
/*  86: 93 */     FileDescriptor epollFd = null;
/*  87: 94 */     FileDescriptor eventFd = null;
/*  88: 95 */     FileDescriptor timerFd = null;
/*  89:    */     try
/*  90:    */     {
/*  91: 97 */       this.epollFd = (epollFd = Native.newEpollCreate());
/*  92: 98 */       this.eventFd = (eventFd = Native.newEventFd());
/*  93:    */       try
/*  94:    */       {
/*  95:100 */         Native.epollCtlAdd(epollFd.intValue(), eventFd.intValue(), Native.EPOLLIN);
/*  96:    */       }
/*  97:    */       catch (IOException e)
/*  98:    */       {
/*  99:102 */         throw new IllegalStateException("Unable to add eventFd filedescriptor to epoll", e);
/* 100:    */       }
/* 101:104 */       this.timerFd = (timerFd = Native.newTimerFd());
/* 102:    */       try
/* 103:    */       {
/* 104:106 */         Native.epollCtlAdd(epollFd.intValue(), timerFd.intValue(), Native.EPOLLIN | Native.EPOLLET);
/* 105:    */       }
/* 106:    */       catch (IOException e)
/* 107:    */       {
/* 108:108 */         throw new IllegalStateException("Unable to add timerFd filedescriptor to epoll", e);
/* 109:    */       }
/* 110:110 */       success = true; return;
/* 111:    */     }
/* 112:    */     finally
/* 113:    */     {
/* 114:112 */       if (!success)
/* 115:    */       {
/* 116:113 */         if (epollFd != null) {
/* 117:    */           try
/* 118:    */           {
/* 119:115 */             epollFd.close();
/* 120:    */           }
/* 121:    */           catch (Exception localException3) {}
/* 122:    */         }
/* 123:120 */         if (eventFd != null) {
/* 124:    */           try
/* 125:    */           {
/* 126:122 */             eventFd.close();
/* 127:    */           }
/* 128:    */           catch (Exception localException4) {}
/* 129:    */         }
/* 130:127 */         if (timerFd != null) {
/* 131:    */           try
/* 132:    */           {
/* 133:129 */             timerFd.close();
/* 134:    */           }
/* 135:    */           catch (Exception localException5) {}
/* 136:    */         }
/* 137:    */       }
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   IovArray cleanArray()
/* 142:    */   {
/* 143:142 */     this.iovArray.clear();
/* 144:143 */     return this.iovArray;
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected void wakeup(boolean inEventLoop)
/* 148:    */   {
/* 149:148 */     if ((!inEventLoop) && (WAKEN_UP_UPDATER.compareAndSet(this, 0, 1))) {
/* 150:150 */       Native.eventFdWrite(this.eventFd.intValue(), 1L);
/* 151:    */     }
/* 152:    */   }
/* 153:    */   
/* 154:    */   void add(AbstractEpollChannel ch)
/* 155:    */     throws IOException
/* 156:    */   {
/* 157:158 */     assert (inEventLoop());
/* 158:159 */     int fd = ch.socket.intValue();
/* 159:160 */     Native.epollCtlAdd(this.epollFd.intValue(), fd, ch.flags);
/* 160:161 */     this.channels.put(fd, ch);
/* 161:    */   }
/* 162:    */   
/* 163:    */   void modify(AbstractEpollChannel ch)
/* 164:    */     throws IOException
/* 165:    */   {
/* 166:168 */     assert (inEventLoop());
/* 167:169 */     Native.epollCtlMod(this.epollFd.intValue(), ch.socket.intValue(), ch.flags);
/* 168:    */   }
/* 169:    */   
/* 170:    */   void remove(AbstractEpollChannel ch)
/* 171:    */     throws IOException
/* 172:    */   {
/* 173:176 */     assert (inEventLoop());
/* 174:178 */     if (ch.isOpen())
/* 175:    */     {
/* 176:179 */       int fd = ch.socket.intValue();
/* 177:180 */       if (this.channels.remove(fd) != null) {
/* 178:183 */         Native.epollCtlDel(this.epollFd.intValue(), ch.fd().intValue());
/* 179:    */       }
/* 180:    */     }
/* 181:    */   }
/* 182:    */   
/* 183:    */   protected Queue<Runnable> newTaskQueue(int maxPendingTasks)
/* 184:    */   {
/* 185:191 */     return maxPendingTasks == 2147483647 ? PlatformDependent.newMpscQueue() : 
/* 186:192 */       PlatformDependent.newMpscQueue(maxPendingTasks);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public int pendingTasks()
/* 190:    */   {
/* 191:200 */     if (inEventLoop()) {
/* 192:201 */       return super.pendingTasks();
/* 193:    */     }
/* 194:203 */     return ((Integer)submit(this.pendingTasksCallable).syncUninterruptibly().getNow()).intValue();
/* 195:    */   }
/* 196:    */   
/* 197:    */   public int getIoRatio()
/* 198:    */   {
/* 199:210 */     return this.ioRatio;
/* 200:    */   }
/* 201:    */   
/* 202:    */   public void setIoRatio(int ioRatio)
/* 203:    */   {
/* 204:218 */     if ((ioRatio <= 0) || (ioRatio > 100)) {
/* 205:219 */       throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
/* 206:    */     }
/* 207:221 */     this.ioRatio = ioRatio;
/* 208:    */   }
/* 209:    */   
/* 210:    */   private int epollWait(boolean oldWakeup)
/* 211:    */     throws IOException
/* 212:    */   {
/* 213:229 */     if ((oldWakeup) && (hasTasks())) {
/* 214:230 */       return epollWaitNow();
/* 215:    */     }
/* 216:233 */     long totalDelay = delayNanos(System.nanoTime());
/* 217:234 */     int delaySeconds = (int)Math.min(totalDelay / 1000000000L, 2147483647L);
/* 218:235 */     return Native.epollWait(this.epollFd, this.events, this.timerFd, delaySeconds, 
/* 219:236 */       (int)Math.min(totalDelay - delaySeconds * 1000000000L, 2147483647L));
/* 220:    */   }
/* 221:    */   
/* 222:    */   private int epollWaitNow()
/* 223:    */     throws IOException
/* 224:    */   {
/* 225:240 */     return Native.epollWait(this.epollFd, this.events, this.timerFd, 0, 0);
/* 226:    */   }
/* 227:    */   
/* 228:    */   protected void run()
/* 229:    */   {
/* 230:    */     for (;;)
/* 231:    */     {
/* 232:    */       try
/* 233:    */       {
/* 234:247 */         int strategy = this.selectStrategy.calculateStrategy(this.selectNowSupplier, hasTasks());
/* 235:248 */         switch (strategy)
/* 236:    */         {
/* 237:    */         case -2: 
/* 238:    */           break;
/* 239:    */         case -1: 
/* 240:252 */           strategy = epollWait(WAKEN_UP_UPDATER.getAndSet(this, 0) == 1);
/* 241:282 */           if (this.wakenUp == 1) {
/* 242:283 */             Native.eventFdWrite(this.eventFd.intValue(), 1L);
/* 243:    */           }
/* 244:    */           break;
/* 245:    */         }
/* 246:289 */         int ioRatio = this.ioRatio;
/* 247:290 */         if (ioRatio == 100) {
/* 248:    */           try
/* 249:    */           {
/* 250:292 */             if (strategy > 0) {
/* 251:293 */               processReady(this.events, strategy);
/* 252:    */             }
/* 253:297 */             runAllTasks();
/* 254:    */           }
/* 255:    */           finally
/* 256:    */           {
/* 257:297 */             runAllTasks();
/* 258:    */           }
/* 259:    */         }
/* 260:300 */         long ioStartTime = System.nanoTime();
/* 261:    */         try
/* 262:    */         {
/* 263:303 */           if (strategy > 0) {
/* 264:304 */             processReady(this.events, strategy);
/* 265:    */           }
/* 266:    */         }
/* 267:    */         finally
/* 268:    */         {
/* 269:    */           long ioTime;
/* 270:308 */           long ioTime = System.nanoTime() - ioStartTime;
/* 271:309 */           runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
/* 272:    */         }
/* 273:312 */         if ((this.allowGrowing) && (strategy == this.events.length())) {
/* 274:314 */           this.events.increase();
/* 275:    */         }
/* 276:    */       }
/* 277:    */       catch (Throwable t)
/* 278:    */       {
/* 279:317 */         handleLoopException(t);
/* 280:    */       }
/* 281:    */       try
/* 282:    */       {
/* 283:321 */         if (isShuttingDown())
/* 284:    */         {
/* 285:322 */           closeAll();
/* 286:323 */           if (confirmShutdown()) {
/* 287:    */             break;
/* 288:    */           }
/* 289:    */         }
/* 290:    */       }
/* 291:    */       catch (Throwable t)
/* 292:    */       {
/* 293:328 */         handleLoopException(t);
/* 294:    */       }
/* 295:    */     }
/* 296:    */   }
/* 297:    */   
/* 298:    */   private static void handleLoopException(Throwable t)
/* 299:    */   {
/* 300:334 */     logger.warn("Unexpected exception in the selector loop.", t);
/* 301:    */     try
/* 302:    */     {
/* 303:339 */       Thread.sleep(1000L);
/* 304:    */     }
/* 305:    */     catch (InterruptedException localInterruptedException) {}
/* 306:    */   }
/* 307:    */   
/* 308:    */   private void closeAll()
/* 309:    */   {
/* 310:    */     try
/* 311:    */     {
/* 312:347 */       epollWaitNow();
/* 313:    */     }
/* 314:    */     catch (IOException localIOException) {}
/* 315:353 */     Collection<AbstractEpollChannel> array = new ArrayList(this.channels.size());
/* 316:355 */     for (AbstractEpollChannel channel : this.channels.values()) {
/* 317:356 */       array.add(channel);
/* 318:    */     }
/* 319:359 */     for (AbstractEpollChannel ch : array) {
/* 320:360 */       ch.unsafe().close(ch.unsafe().voidPromise());
/* 321:    */     }
/* 322:    */   }
/* 323:    */   
/* 324:    */   private void processReady(EpollEventArray events, int ready)
/* 325:    */   {
/* 326:365 */     for (int i = 0; i < ready; i++)
/* 327:    */     {
/* 328:366 */       int fd = events.fd(i);
/* 329:367 */       if (fd == this.eventFd.intValue())
/* 330:    */       {
/* 331:369 */         Native.eventFdRead(fd);
/* 332:    */       }
/* 333:370 */       else if (fd == this.timerFd.intValue())
/* 334:    */       {
/* 335:372 */         Native.timerFdRead(fd);
/* 336:    */       }
/* 337:    */       else
/* 338:    */       {
/* 339:374 */         long ev = events.events(i);
/* 340:    */         
/* 341:376 */         AbstractEpollChannel ch = (AbstractEpollChannel)this.channels.get(fd);
/* 342:377 */         if (ch != null)
/* 343:    */         {
/* 344:382 */           AbstractEpollChannel.AbstractEpollUnsafe unsafe = (AbstractEpollChannel.AbstractEpollUnsafe)ch.unsafe();
/* 345:392 */           if ((ev & (Native.EPOLLERR | Native.EPOLLOUT)) != 0L) {
/* 346:394 */             unsafe.epollOutReady();
/* 347:    */           }
/* 348:402 */           if ((ev & (Native.EPOLLERR | Native.EPOLLIN)) != 0L) {
/* 349:404 */             unsafe.epollInReady();
/* 350:    */           }
/* 351:410 */           if ((ev & Native.EPOLLRDHUP) != 0L) {
/* 352:411 */             unsafe.epollRdHupReady();
/* 353:    */           }
/* 354:    */         }
/* 355:    */         else
/* 356:    */         {
/* 357:    */           try
/* 358:    */           {
/* 359:416 */             Native.epollCtlDel(this.epollFd.intValue(), fd);
/* 360:    */           }
/* 361:    */           catch (IOException localIOException) {}
/* 362:    */         }
/* 363:    */       }
/* 364:    */     }
/* 365:    */   }
/* 366:    */   
/* 367:    */   protected void cleanup()
/* 368:    */   {
/* 369:    */     try
/* 370:    */     {
/* 371:    */       try
/* 372:    */       {
/* 373:432 */         this.epollFd.close();
/* 374:    */       }
/* 375:    */       catch (IOException e)
/* 376:    */       {
/* 377:434 */         logger.warn("Failed to close the epoll fd.", e);
/* 378:    */       }
/* 379:    */       try
/* 380:    */       {
/* 381:437 */         this.eventFd.close();
/* 382:    */       }
/* 383:    */       catch (IOException e)
/* 384:    */       {
/* 385:439 */         logger.warn("Failed to close the event fd.", e);
/* 386:    */       }
/* 387:    */       try
/* 388:    */       {
/* 389:442 */         this.timerFd.close();
/* 390:    */       }
/* 391:    */       catch (IOException e)
/* 392:    */       {
/* 393:444 */         logger.warn("Failed to close the timer fd.", e);
/* 394:    */       }
/* 395:448 */       this.iovArray.release();
/* 396:449 */       this.events.free();
/* 397:    */     }
/* 398:    */     finally
/* 399:    */     {
/* 400:448 */       this.iovArray.release();
/* 401:449 */       this.events.free();
/* 402:    */     }
/* 403:    */   }
/* 404:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollEventLoop
 * JD-Core Version:    0.7.0.1
 */