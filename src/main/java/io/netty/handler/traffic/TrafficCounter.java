/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.util.concurrent.ScheduledExecutorService;
/*   6:    */ import java.util.concurrent.ScheduledFuture;
/*   7:    */ import java.util.concurrent.TimeUnit;
/*   8:    */ import java.util.concurrent.atomic.AtomicLong;
/*   9:    */ 
/*  10:    */ public class TrafficCounter
/*  11:    */ {
/*  12: 38 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(TrafficCounter.class);
/*  13:    */   
/*  14:    */   public static long milliSecondFromNano()
/*  15:    */   {
/*  16: 44 */     return System.nanoTime() / 1000000L;
/*  17:    */   }
/*  18:    */   
/*  19: 50 */   private final AtomicLong currentWrittenBytes = new AtomicLong();
/*  20: 55 */   private final AtomicLong currentReadBytes = new AtomicLong();
/*  21:    */   private long writingTime;
/*  22:    */   private long readingTime;
/*  23: 70 */   private final AtomicLong cumulativeWrittenBytes = new AtomicLong();
/*  24: 75 */   private final AtomicLong cumulativeReadBytes = new AtomicLong();
/*  25:    */   private long lastCumulativeTime;
/*  26:    */   private long lastWriteThroughput;
/*  27:    */   private long lastReadThroughput;
/*  28: 95 */   final AtomicLong lastTime = new AtomicLong();
/*  29:    */   private volatile long lastWrittenBytes;
/*  30:    */   private volatile long lastReadBytes;
/*  31:    */   private volatile long lastWritingTime;
/*  32:    */   private volatile long lastReadingTime;
/*  33:120 */   private final AtomicLong realWrittenBytes = new AtomicLong();
/*  34:    */   private long realWriteThroughput;
/*  35:130 */   final AtomicLong checkInterval = new AtomicLong(1000L);
/*  36:    */   final String name;
/*  37:    */   final AbstractTrafficShapingHandler trafficShapingHandler;
/*  38:    */   final ScheduledExecutorService executor;
/*  39:    */   Runnable monitor;
/*  40:    */   volatile ScheduledFuture<?> scheduledFuture;
/*  41:    */   volatile boolean monitorActive;
/*  42:    */   
/*  43:    */   private final class TrafficMonitoringTask
/*  44:    */     implements Runnable
/*  45:    */   {
/*  46:    */     private TrafficMonitoringTask() {}
/*  47:    */     
/*  48:    */     public void run()
/*  49:    */     {
/*  50:170 */       if (!TrafficCounter.this.monitorActive) {
/*  51:171 */         return;
/*  52:    */       }
/*  53:173 */       TrafficCounter.this.resetAccounting(TrafficCounter.milliSecondFromNano());
/*  54:174 */       if (TrafficCounter.this.trafficShapingHandler != null) {
/*  55:175 */         TrafficCounter.this.trafficShapingHandler.doAccounting(TrafficCounter.this);
/*  56:    */       }
/*  57:177 */       TrafficCounter.this.scheduledFuture = TrafficCounter.this.executor.schedule(this, TrafficCounter.this.checkInterval.get(), TimeUnit.MILLISECONDS);
/*  58:    */     }
/*  59:    */   }
/*  60:    */   
/*  61:    */   public synchronized void start()
/*  62:    */   {
/*  63:185 */     if (this.monitorActive) {
/*  64:186 */       return;
/*  65:    */     }
/*  66:188 */     this.lastTime.set(milliSecondFromNano());
/*  67:189 */     long localCheckInterval = this.checkInterval.get();
/*  68:191 */     if ((localCheckInterval > 0L) && (this.executor != null))
/*  69:    */     {
/*  70:192 */       this.monitorActive = true;
/*  71:193 */       this.monitor = new TrafficMonitoringTask(null);
/*  72:    */       
/*  73:195 */       this.scheduledFuture = this.executor.schedule(this.monitor, localCheckInterval, TimeUnit.MILLISECONDS);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   public synchronized void stop()
/*  78:    */   {
/*  79:203 */     if (!this.monitorActive) {
/*  80:204 */       return;
/*  81:    */     }
/*  82:206 */     this.monitorActive = false;
/*  83:207 */     resetAccounting(milliSecondFromNano());
/*  84:208 */     if (this.trafficShapingHandler != null) {
/*  85:209 */       this.trafficShapingHandler.doAccounting(this);
/*  86:    */     }
/*  87:211 */     if (this.scheduledFuture != null) {
/*  88:212 */       this.scheduledFuture.cancel(true);
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   synchronized void resetAccounting(long newLastTime)
/*  93:    */   {
/*  94:222 */     long interval = newLastTime - this.lastTime.getAndSet(newLastTime);
/*  95:223 */     if (interval == 0L) {
/*  96:225 */       return;
/*  97:    */     }
/*  98:227 */     if ((logger.isDebugEnabled()) && (interval > checkInterval() << 1)) {
/*  99:228 */       logger.debug("Acct schedule not ok: " + interval + " > 2*" + checkInterval() + " from " + this.name);
/* 100:    */     }
/* 101:230 */     this.lastReadBytes = this.currentReadBytes.getAndSet(0L);
/* 102:231 */     this.lastWrittenBytes = this.currentWrittenBytes.getAndSet(0L);
/* 103:232 */     this.lastReadThroughput = (this.lastReadBytes * 1000L / interval);
/* 104:    */     
/* 105:234 */     this.lastWriteThroughput = (this.lastWrittenBytes * 1000L / interval);
/* 106:    */     
/* 107:236 */     this.realWriteThroughput = (this.realWrittenBytes.getAndSet(0L) * 1000L / interval);
/* 108:237 */     this.lastWritingTime = Math.max(this.lastWritingTime, this.writingTime);
/* 109:238 */     this.lastReadingTime = Math.max(this.lastReadingTime, this.readingTime);
/* 110:    */   }
/* 111:    */   
/* 112:    */   public TrafficCounter(ScheduledExecutorService executor, String name, long checkInterval)
/* 113:    */   {
/* 114:254 */     if (name == null) {
/* 115:255 */       throw new NullPointerException("name");
/* 116:    */     }
/* 117:258 */     this.trafficShapingHandler = null;
/* 118:259 */     this.executor = executor;
/* 119:260 */     this.name = name;
/* 120:    */     
/* 121:262 */     init(checkInterval);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public TrafficCounter(AbstractTrafficShapingHandler trafficShapingHandler, ScheduledExecutorService executor, String name, long checkInterval)
/* 125:    */   {
/* 126:283 */     if (trafficShapingHandler == null) {
/* 127:284 */       throw new IllegalArgumentException("trafficShapingHandler");
/* 128:    */     }
/* 129:286 */     if (name == null) {
/* 130:287 */       throw new NullPointerException("name");
/* 131:    */     }
/* 132:290 */     this.trafficShapingHandler = trafficShapingHandler;
/* 133:291 */     this.executor = executor;
/* 134:292 */     this.name = name;
/* 135:    */     
/* 136:294 */     init(checkInterval);
/* 137:    */   }
/* 138:    */   
/* 139:    */   private void init(long checkInterval)
/* 140:    */   {
/* 141:299 */     this.lastCumulativeTime = System.currentTimeMillis();
/* 142:300 */     this.writingTime = milliSecondFromNano();
/* 143:301 */     this.readingTime = this.writingTime;
/* 144:302 */     this.lastWritingTime = this.writingTime;
/* 145:303 */     this.lastReadingTime = this.writingTime;
/* 146:304 */     configure(checkInterval);
/* 147:    */   }
/* 148:    */   
/* 149:    */   public void configure(long newCheckInterval)
/* 150:    */   {
/* 151:313 */     long newInterval = newCheckInterval / 10L * 10L;
/* 152:314 */     if (this.checkInterval.getAndSet(newInterval) != newInterval) {
/* 153:315 */       if (newInterval <= 0L)
/* 154:    */       {
/* 155:316 */         stop();
/* 156:    */         
/* 157:318 */         this.lastTime.set(milliSecondFromNano());
/* 158:    */       }
/* 159:    */       else
/* 160:    */       {
/* 161:321 */         start();
/* 162:    */       }
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   void bytesRecvFlowControl(long recv)
/* 167:    */   {
/* 168:333 */     this.currentReadBytes.addAndGet(recv);
/* 169:334 */     this.cumulativeReadBytes.addAndGet(recv);
/* 170:    */   }
/* 171:    */   
/* 172:    */   void bytesWriteFlowControl(long write)
/* 173:    */   {
/* 174:344 */     this.currentWrittenBytes.addAndGet(write);
/* 175:345 */     this.cumulativeWrittenBytes.addAndGet(write);
/* 176:    */   }
/* 177:    */   
/* 178:    */   void bytesRealWriteFlowControl(long write)
/* 179:    */   {
/* 180:355 */     this.realWrittenBytes.addAndGet(write);
/* 181:    */   }
/* 182:    */   
/* 183:    */   public long checkInterval()
/* 184:    */   {
/* 185:363 */     return this.checkInterval.get();
/* 186:    */   }
/* 187:    */   
/* 188:    */   public long lastReadThroughput()
/* 189:    */   {
/* 190:370 */     return this.lastReadThroughput;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public long lastWriteThroughput()
/* 194:    */   {
/* 195:377 */     return this.lastWriteThroughput;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public long lastReadBytes()
/* 199:    */   {
/* 200:384 */     return this.lastReadBytes;
/* 201:    */   }
/* 202:    */   
/* 203:    */   public long lastWrittenBytes()
/* 204:    */   {
/* 205:391 */     return this.lastWrittenBytes;
/* 206:    */   }
/* 207:    */   
/* 208:    */   public long currentReadBytes()
/* 209:    */   {
/* 210:398 */     return this.currentReadBytes.get();
/* 211:    */   }
/* 212:    */   
/* 213:    */   public long currentWrittenBytes()
/* 214:    */   {
/* 215:405 */     return this.currentWrittenBytes.get();
/* 216:    */   }
/* 217:    */   
/* 218:    */   public long lastTime()
/* 219:    */   {
/* 220:412 */     return this.lastTime.get();
/* 221:    */   }
/* 222:    */   
/* 223:    */   public long cumulativeWrittenBytes()
/* 224:    */   {
/* 225:419 */     return this.cumulativeWrittenBytes.get();
/* 226:    */   }
/* 227:    */   
/* 228:    */   public long cumulativeReadBytes()
/* 229:    */   {
/* 230:426 */     return this.cumulativeReadBytes.get();
/* 231:    */   }
/* 232:    */   
/* 233:    */   public long lastCumulativeTime()
/* 234:    */   {
/* 235:434 */     return this.lastCumulativeTime;
/* 236:    */   }
/* 237:    */   
/* 238:    */   public AtomicLong getRealWrittenBytes()
/* 239:    */   {
/* 240:441 */     return this.realWrittenBytes;
/* 241:    */   }
/* 242:    */   
/* 243:    */   public long getRealWriteThroughput()
/* 244:    */   {
/* 245:448 */     return this.realWriteThroughput;
/* 246:    */   }
/* 247:    */   
/* 248:    */   public void resetCumulativeTime()
/* 249:    */   {
/* 250:456 */     this.lastCumulativeTime = System.currentTimeMillis();
/* 251:457 */     this.cumulativeReadBytes.set(0L);
/* 252:458 */     this.cumulativeWrittenBytes.set(0L);
/* 253:    */   }
/* 254:    */   
/* 255:    */   public String name()
/* 256:    */   {
/* 257:465 */     return this.name;
/* 258:    */   }
/* 259:    */   
/* 260:    */   @Deprecated
/* 261:    */   public long readTimeToWait(long size, long limitTraffic, long maxTime)
/* 262:    */   {
/* 263:482 */     return readTimeToWait(size, limitTraffic, maxTime, milliSecondFromNano());
/* 264:    */   }
/* 265:    */   
/* 266:    */   public long readTimeToWait(long size, long limitTraffic, long maxTime, long now)
/* 267:    */   {
/* 268:499 */     bytesRecvFlowControl(size);
/* 269:500 */     if ((size == 0L) || (limitTraffic == 0L)) {
/* 270:501 */       return 0L;
/* 271:    */     }
/* 272:503 */     long lastTimeCheck = this.lastTime.get();
/* 273:504 */     long sum = this.currentReadBytes.get();
/* 274:505 */     long localReadingTime = this.readingTime;
/* 275:506 */     long lastRB = this.lastReadBytes;
/* 276:507 */     long interval = now - lastTimeCheck;
/* 277:508 */     long pastDelay = Math.max(this.lastReadingTime - lastTimeCheck, 0L);
/* 278:509 */     if (interval > 10L)
/* 279:    */     {
/* 280:511 */       long time = sum * 1000L / limitTraffic - interval + pastDelay;
/* 281:512 */       if (time > 10L)
/* 282:    */       {
/* 283:513 */         if (logger.isDebugEnabled()) {
/* 284:514 */           logger.debug("Time: " + time + ':' + sum + ':' + interval + ':' + pastDelay);
/* 285:    */         }
/* 286:516 */         if ((time > maxTime) && (now + time - localReadingTime > maxTime)) {
/* 287:517 */           time = maxTime;
/* 288:    */         }
/* 289:519 */         this.readingTime = Math.max(localReadingTime, now + time);
/* 290:520 */         return time;
/* 291:    */       }
/* 292:522 */       this.readingTime = Math.max(localReadingTime, now);
/* 293:523 */       return 0L;
/* 294:    */     }
/* 295:526 */     long lastsum = sum + lastRB;
/* 296:527 */     long lastinterval = interval + this.checkInterval.get();
/* 297:528 */     long time = lastsum * 1000L / limitTraffic - lastinterval + pastDelay;
/* 298:529 */     if (time > 10L)
/* 299:    */     {
/* 300:530 */       if (logger.isDebugEnabled()) {
/* 301:531 */         logger.debug("Time: " + time + ':' + lastsum + ':' + lastinterval + ':' + pastDelay);
/* 302:    */       }
/* 303:533 */       if ((time > maxTime) && (now + time - localReadingTime > maxTime)) {
/* 304:534 */         time = maxTime;
/* 305:    */       }
/* 306:536 */       this.readingTime = Math.max(localReadingTime, now + time);
/* 307:537 */       return time;
/* 308:    */     }
/* 309:539 */     this.readingTime = Math.max(localReadingTime, now);
/* 310:540 */     return 0L;
/* 311:    */   }
/* 312:    */   
/* 313:    */   @Deprecated
/* 314:    */   public long writeTimeToWait(long size, long limitTraffic, long maxTime)
/* 315:    */   {
/* 316:557 */     return writeTimeToWait(size, limitTraffic, maxTime, milliSecondFromNano());
/* 317:    */   }
/* 318:    */   
/* 319:    */   public long writeTimeToWait(long size, long limitTraffic, long maxTime, long now)
/* 320:    */   {
/* 321:574 */     bytesWriteFlowControl(size);
/* 322:575 */     if ((size == 0L) || (limitTraffic == 0L)) {
/* 323:576 */       return 0L;
/* 324:    */     }
/* 325:578 */     long lastTimeCheck = this.lastTime.get();
/* 326:579 */     long sum = this.currentWrittenBytes.get();
/* 327:580 */     long lastWB = this.lastWrittenBytes;
/* 328:581 */     long localWritingTime = this.writingTime;
/* 329:582 */     long pastDelay = Math.max(this.lastWritingTime - lastTimeCheck, 0L);
/* 330:583 */     long interval = now - lastTimeCheck;
/* 331:584 */     if (interval > 10L)
/* 332:    */     {
/* 333:586 */       long time = sum * 1000L / limitTraffic - interval + pastDelay;
/* 334:587 */       if (time > 10L)
/* 335:    */       {
/* 336:588 */         if (logger.isDebugEnabled()) {
/* 337:589 */           logger.debug("Time: " + time + ':' + sum + ':' + interval + ':' + pastDelay);
/* 338:    */         }
/* 339:591 */         if ((time > maxTime) && (now + time - localWritingTime > maxTime)) {
/* 340:592 */           time = maxTime;
/* 341:    */         }
/* 342:594 */         this.writingTime = Math.max(localWritingTime, now + time);
/* 343:595 */         return time;
/* 344:    */       }
/* 345:597 */       this.writingTime = Math.max(localWritingTime, now);
/* 346:598 */       return 0L;
/* 347:    */     }
/* 348:601 */     long lastsum = sum + lastWB;
/* 349:602 */     long lastinterval = interval + this.checkInterval.get();
/* 350:603 */     long time = lastsum * 1000L / limitTraffic - lastinterval + pastDelay;
/* 351:604 */     if (time > 10L)
/* 352:    */     {
/* 353:605 */       if (logger.isDebugEnabled()) {
/* 354:606 */         logger.debug("Time: " + time + ':' + lastsum + ':' + lastinterval + ':' + pastDelay);
/* 355:    */       }
/* 356:608 */       if ((time > maxTime) && (now + time - localWritingTime > maxTime)) {
/* 357:609 */         time = maxTime;
/* 358:    */       }
/* 359:611 */       this.writingTime = Math.max(localWritingTime, now + time);
/* 360:612 */       return time;
/* 361:    */     }
/* 362:614 */     this.writingTime = Math.max(localWritingTime, now);
/* 363:615 */     return 0L;
/* 364:    */   }
/* 365:    */   
/* 366:    */   public String toString()
/* 367:    */   {
/* 368:620 */     return 
/* 369:    */     
/* 370:    */ 
/* 371:    */ 
/* 372:    */ 
/* 373:    */ 
/* 374:626 */       165 + "Monitor " + this.name + " Current Speed Read: " + (this.lastReadThroughput >> 10) + " KB/s, " + "Asked Write: " + (this.lastWriteThroughput >> 10) + " KB/s, " + "Real Write: " + (this.realWriteThroughput >> 10) + " KB/s, " + "Current Read: " + (this.currentReadBytes.get() >> 10) + " KB, " + "Current asked Write: " + (this.currentWrittenBytes.get() >> 10) + " KB, " + "Current real Write: " + (this.realWrittenBytes.get() >> 10) + " KB";
/* 375:    */   }
/* 376:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.traffic.TrafficCounter
 * JD-Core Version:    0.7.0.1
 */