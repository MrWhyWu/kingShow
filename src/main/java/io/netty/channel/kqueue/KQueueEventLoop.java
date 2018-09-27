/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.channel.EventLoopGroup;
/*   4:    */ import io.netty.channel.SelectStrategy;
/*   5:    */ import io.netty.channel.SingleThreadEventLoop;
/*   6:    */ import io.netty.channel.unix.FileDescriptor;
/*   7:    */ import io.netty.channel.unix.IovArray;
/*   8:    */ import io.netty.util.IntSupplier;
/*   9:    */ import io.netty.util.concurrent.Future;
/*  10:    */ import io.netty.util.concurrent.RejectedExecutionHandler;
/*  11:    */ import io.netty.util.internal.ObjectUtil;
/*  12:    */ import io.netty.util.internal.PlatformDependent;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  15:    */ import java.io.IOException;
/*  16:    */ import java.util.Queue;
/*  17:    */ import java.util.concurrent.Callable;
/*  18:    */ import java.util.concurrent.Executor;
/*  19:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  20:    */ 
/*  21:    */ final class KQueueEventLoop
/*  22:    */   extends SingleThreadEventLoop
/*  23:    */ {
/*  24:    */   private static final InternalLogger logger;
/*  25:    */   private static final AtomicIntegerFieldUpdater<KQueueEventLoop> WAKEN_UP_UPDATER;
/*  26:    */   private static final int KQUEUE_WAKE_UP_IDENT = 0;
/*  27:    */   private final NativeLongArray jniChannelPointers;
/*  28:    */   private final boolean allowGrowing;
/*  29:    */   private final FileDescriptor kqueueFd;
/*  30:    */   private final KQueueEventArray changeList;
/*  31:    */   private final KQueueEventArray eventList;
/*  32:    */   private final SelectStrategy selectStrategy;
/*  33:    */   
/*  34:    */   static
/*  35:    */   {
/*  36: 45 */     logger = InternalLoggerFactory.getInstance(KQueueEventLoop.class);
/*  37:    */     
/*  38: 47 */     WAKEN_UP_UPDATER = AtomicIntegerFieldUpdater.newUpdater(KQueueEventLoop.class, "wakenUp");
/*  39:    */     
/*  40:    */ 
/*  41:    */ 
/*  42:    */ 
/*  43:    */ 
/*  44: 53 */     KQueue.ensureAvailability();
/*  45:    */   }
/*  46:    */   
/*  47: 62 */   private final IovArray iovArray = new IovArray();
/*  48: 63 */   private final IntSupplier selectNowSupplier = new IntSupplier()
/*  49:    */   {
/*  50:    */     public int get()
/*  51:    */       throws Exception
/*  52:    */     {
/*  53: 66 */       return KQueueEventLoop.this.kqueueWaitNow();
/*  54:    */     }
/*  55:    */   };
/*  56: 69 */   private final Callable<Integer> pendingTasksCallable = new Callable()
/*  57:    */   {
/*  58:    */     public Integer call()
/*  59:    */       throws Exception
/*  60:    */     {
/*  61: 72 */       return Integer.valueOf(KQueueEventLoop.this.pendingTasks());
/*  62:    */     }
/*  63:    */   };
/*  64:    */   private volatile int wakenUp;
/*  65: 77 */   private volatile int ioRatio = 50;
/*  66:    */   
/*  67:    */   KQueueEventLoop(EventLoopGroup parent, Executor executor, int maxEvents, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler)
/*  68:    */   {
/*  69: 81 */     super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
/*  70: 82 */     this.selectStrategy = ((SelectStrategy)ObjectUtil.checkNotNull(strategy, "strategy"));
/*  71: 83 */     this.kqueueFd = Native.newKQueue();
/*  72: 84 */     if (maxEvents == 0)
/*  73:    */     {
/*  74: 85 */       this.allowGrowing = true;
/*  75: 86 */       maxEvents = 4096;
/*  76:    */     }
/*  77:    */     else
/*  78:    */     {
/*  79: 88 */       this.allowGrowing = false;
/*  80:    */     }
/*  81: 90 */     this.changeList = new KQueueEventArray(maxEvents);
/*  82: 91 */     this.eventList = new KQueueEventArray(maxEvents);
/*  83: 92 */     this.jniChannelPointers = new NativeLongArray(4096);
/*  84: 93 */     int result = Native.keventAddUserEvent(this.kqueueFd.intValue(), 0);
/*  85: 94 */     if (result < 0)
/*  86:    */     {
/*  87: 95 */       cleanup();
/*  88: 96 */       throw new IllegalStateException("kevent failed to add user event with errno: " + -result);
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   void evSet(AbstractKQueueChannel ch, short filter, short flags, int fflags)
/*  93:    */   {
/*  94:101 */     this.changeList.evSet(ch, filter, flags, fflags);
/*  95:    */   }
/*  96:    */   
/*  97:    */   void remove(AbstractKQueueChannel ch)
/*  98:    */     throws IOException
/*  99:    */   {
/* 100:105 */     assert (inEventLoop());
/* 101:106 */     if (ch.jniSelfPtr == 0L) {
/* 102:107 */       return;
/* 103:    */     }
/* 104:110 */     this.jniChannelPointers.add(ch.jniSelfPtr);
/* 105:111 */     ch.jniSelfPtr = 0L;
/* 106:    */   }
/* 107:    */   
/* 108:    */   IovArray cleanArray()
/* 109:    */   {
/* 110:118 */     this.iovArray.clear();
/* 111:119 */     return this.iovArray;
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected void wakeup(boolean inEventLoop)
/* 115:    */   {
/* 116:124 */     if ((!inEventLoop) && (WAKEN_UP_UPDATER.compareAndSet(this, 0, 1))) {
/* 117:125 */       wakeup();
/* 118:    */     }
/* 119:    */   }
/* 120:    */   
/* 121:    */   private void wakeup()
/* 122:    */   {
/* 123:130 */     Native.keventTriggerUserEvent(this.kqueueFd.intValue(), 0);
/* 124:    */   }
/* 125:    */   
/* 126:    */   private int kqueueWait(boolean oldWakeup)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:140 */     if ((oldWakeup) && (hasTasks())) {
/* 130:141 */       return kqueueWaitNow();
/* 131:    */     }
/* 132:144 */     long totalDelay = delayNanos(System.nanoTime());
/* 133:145 */     int delaySeconds = (int)Math.min(totalDelay / 1000000000L, 2147483647L);
/* 134:146 */     return kqueueWait(delaySeconds, (int)Math.min(totalDelay - delaySeconds * 1000000000L, 2147483647L));
/* 135:    */   }
/* 136:    */   
/* 137:    */   private int kqueueWaitNow()
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:150 */     return kqueueWait(0, 0);
/* 141:    */   }
/* 142:    */   
/* 143:    */   private int kqueueWait(int timeoutSec, int timeoutNs)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:154 */     deleteJniChannelPointers();
/* 147:155 */     int numEvents = Native.keventWait(this.kqueueFd.intValue(), this.changeList, this.eventList, timeoutSec, timeoutNs);
/* 148:156 */     this.changeList.clear();
/* 149:157 */     return numEvents;
/* 150:    */   }
/* 151:    */   
/* 152:    */   private void deleteJniChannelPointers()
/* 153:    */   {
/* 154:161 */     if (!this.jniChannelPointers.isEmpty())
/* 155:    */     {
/* 156:162 */       KQueueEventArray.deleteGlobalRefs(this.jniChannelPointers.memoryAddress(), this.jniChannelPointers.memoryAddressEnd());
/* 157:163 */       this.jniChannelPointers.clear();
/* 158:    */     }
/* 159:    */   }
/* 160:    */   
/* 161:    */   private void processReady(int ready)
/* 162:    */   {
/* 163:168 */     for (int i = 0; i < ready; i++)
/* 164:    */     {
/* 165:169 */       short filter = this.eventList.filter(i);
/* 166:170 */       short flags = this.eventList.flags(i);
/* 167:171 */       if ((filter == Native.EVFILT_USER) || ((flags & Native.EV_ERROR) != 0))
/* 168:    */       {
/* 169:174 */         if ((!$assertionsDisabled) && (filter == Native.EVFILT_USER) && ((filter != Native.EVFILT_USER) || 
/* 170:175 */           (this.eventList.fd(i) != 0))) {
/* 171:174 */           throw new AssertionError();
/* 172:    */         }
/* 173:    */       }
/* 174:    */       else
/* 175:    */       {
/* 176:179 */         AbstractKQueueChannel channel = this.eventList.channel(i);
/* 177:180 */         if (channel == null)
/* 178:    */         {
/* 179:184 */           logger.warn("events[{}]=[{}, {}] had no channel!", new Object[] { Integer.valueOf(i), Integer.valueOf(this.eventList.fd(i)), Short.valueOf(filter) });
/* 180:    */         }
/* 181:    */         else
/* 182:    */         {
/* 183:188 */           AbstractKQueueChannel.AbstractKQueueUnsafe unsafe = (AbstractKQueueChannel.AbstractKQueueUnsafe)channel.unsafe();
/* 184:191 */           if (filter == Native.EVFILT_WRITE) {
/* 185:192 */             unsafe.writeReady();
/* 186:193 */           } else if (filter == Native.EVFILT_READ) {
/* 187:195 */             unsafe.readReady(this.eventList.data(i));
/* 188:196 */           } else if ((filter == Native.EVFILT_SOCK) && ((this.eventList.fflags(i) & Native.NOTE_RDHUP) != 0)) {
/* 189:197 */             unsafe.readEOF();
/* 190:    */           }
/* 191:203 */           if ((flags & Native.EV_EOF) != 0) {
/* 192:204 */             unsafe.readEOF();
/* 193:    */           }
/* 194:    */         }
/* 195:    */       }
/* 196:    */     }
/* 197:    */   }
/* 198:    */   
/* 199:    */   protected void run()
/* 200:    */   {
/* 201:    */     for (;;)
/* 202:    */     {
/* 203:    */       try
/* 204:    */       {
/* 205:213 */         int strategy = this.selectStrategy.calculateStrategy(this.selectNowSupplier, hasTasks());
/* 206:214 */         switch (strategy)
/* 207:    */         {
/* 208:    */         case -2: 
/* 209:    */           break;
/* 210:    */         case -1: 
/* 211:218 */           strategy = kqueueWait(WAKEN_UP_UPDATER.getAndSet(this, 0) == 1);
/* 212:248 */           if (this.wakenUp == 1) {
/* 213:249 */             wakeup();
/* 214:    */           }
/* 215:    */           break;
/* 216:    */         }
/* 217:255 */         int ioRatio = this.ioRatio;
/* 218:256 */         if (ioRatio == 100) {
/* 219:    */           try
/* 220:    */           {
/* 221:258 */             if (strategy > 0) {
/* 222:259 */               processReady(strategy);
/* 223:    */             }
/* 224:262 */             runAllTasks();
/* 225:    */           }
/* 226:    */           finally
/* 227:    */           {
/* 228:262 */             runAllTasks();
/* 229:    */           }
/* 230:    */         }
/* 231:265 */         long ioStartTime = System.nanoTime();
/* 232:    */         try
/* 233:    */         {
/* 234:268 */           if (strategy > 0) {
/* 235:269 */             processReady(strategy);
/* 236:    */           }
/* 237:    */         }
/* 238:    */         finally
/* 239:    */         {
/* 240:    */           long ioTime;
/* 241:272 */           long ioTime = System.nanoTime() - ioStartTime;
/* 242:273 */           runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
/* 243:    */         }
/* 244:276 */         if ((this.allowGrowing) && (strategy == this.eventList.capacity())) {
/* 245:278 */           this.eventList.realloc(false);
/* 246:    */         }
/* 247:    */       }
/* 248:    */       catch (Throwable t)
/* 249:    */       {
/* 250:281 */         handleLoopException(t);
/* 251:    */       }
/* 252:    */       try
/* 253:    */       {
/* 254:285 */         if (isShuttingDown())
/* 255:    */         {
/* 256:286 */           closeAll();
/* 257:287 */           if (confirmShutdown()) {
/* 258:    */             break;
/* 259:    */           }
/* 260:    */         }
/* 261:    */       }
/* 262:    */       catch (Throwable t)
/* 263:    */       {
/* 264:292 */         handleLoopException(t);
/* 265:    */       }
/* 266:    */     }
/* 267:    */   }
/* 268:    */   
/* 269:    */   protected Queue<Runnable> newTaskQueue(int maxPendingTasks)
/* 270:    */   {
/* 271:300 */     return maxPendingTasks == 2147483647 ? PlatformDependent.newMpscQueue() : 
/* 272:301 */       PlatformDependent.newMpscQueue(maxPendingTasks);
/* 273:    */   }
/* 274:    */   
/* 275:    */   public int pendingTasks()
/* 276:    */   {
/* 277:309 */     return inEventLoop() ? super.pendingTasks() : ((Integer)submit(this.pendingTasksCallable).syncUninterruptibly().getNow()).intValue();
/* 278:    */   }
/* 279:    */   
/* 280:    */   public int getIoRatio()
/* 281:    */   {
/* 282:316 */     return this.ioRatio;
/* 283:    */   }
/* 284:    */   
/* 285:    */   public void setIoRatio(int ioRatio)
/* 286:    */   {
/* 287:324 */     if ((ioRatio <= 0) || (ioRatio > 100)) {
/* 288:325 */       throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
/* 289:    */     }
/* 290:327 */     this.ioRatio = ioRatio;
/* 291:    */   }
/* 292:    */   
/* 293:    */   protected void cleanup()
/* 294:    */   {
/* 295:    */     try
/* 296:    */     {
/* 297:    */       try
/* 298:    */       {
/* 299:334 */         this.kqueueFd.close();
/* 300:    */       }
/* 301:    */       catch (IOException e)
/* 302:    */       {
/* 303:336 */         logger.warn("Failed to close the kqueue fd.", e);
/* 304:    */       }
/* 305:343 */       deleteJniChannelPointers();
/* 306:344 */       this.jniChannelPointers.free();
/* 307:    */       
/* 308:346 */       this.changeList.free();
/* 309:347 */       this.eventList.free();
/* 310:    */     }
/* 311:    */     finally
/* 312:    */     {
/* 313:343 */       deleteJniChannelPointers();
/* 314:344 */       this.jniChannelPointers.free();
/* 315:    */       
/* 316:346 */       this.changeList.free();
/* 317:347 */       this.eventList.free();
/* 318:    */     }
/* 319:    */   }
/* 320:    */   
/* 321:    */   private void closeAll()
/* 322:    */   {
/* 323:    */     try
/* 324:    */     {
/* 325:353 */       kqueueWaitNow();
/* 326:    */     }
/* 327:    */     catch (IOException localIOException) {}
/* 328:    */   }
/* 329:    */   
/* 330:    */   private static void handleLoopException(Throwable t)
/* 331:    */   {
/* 332:360 */     logger.warn("Unexpected exception in the selector loop.", t);
/* 333:    */     try
/* 334:    */     {
/* 335:365 */       Thread.sleep(1000L);
/* 336:    */     }
/* 337:    */     catch (InterruptedException localInterruptedException) {}
/* 338:    */   }
/* 339:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueEventLoop
 * JD-Core Version:    0.7.0.1
 */