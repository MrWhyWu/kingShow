/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PriorityQueue;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.util.Queue;
/*   7:    */ import java.util.concurrent.BlockingQueue;
/*   8:    */ import java.util.concurrent.Executors;
/*   9:    */ import java.util.concurrent.LinkedBlockingQueue;
/*  10:    */ import java.util.concurrent.ThreadFactory;
/*  11:    */ import java.util.concurrent.TimeUnit;
/*  12:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*  13:    */ 
/*  14:    */ public final class GlobalEventExecutor
/*  15:    */   extends AbstractScheduledEventExecutor
/*  16:    */ {
/*  17: 37 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(GlobalEventExecutor.class);
/*  18: 39 */   private static final long SCHEDULE_QUIET_PERIOD_INTERVAL = TimeUnit.SECONDS.toNanos(1L);
/*  19: 41 */   public static final GlobalEventExecutor INSTANCE = new GlobalEventExecutor();
/*  20: 43 */   final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue();
/*  21: 44 */   final ScheduledFutureTask<Void> quietPeriodTask = new ScheduledFutureTask(this, 
/*  22: 45 */     Executors.callable(new Runnable()
/*  23:    */   {
/*  24:    */     public void run() {}
/*  25: 45 */   }, null), 
/*  26:    */   
/*  27:    */ 
/*  28:    */ 
/*  29:    */ 
/*  30: 50 */     ScheduledFutureTask.deadlineNanos(SCHEDULE_QUIET_PERIOD_INTERVAL), -SCHEDULE_QUIET_PERIOD_INTERVAL);
/*  31: 56 */   final ThreadFactory threadFactory = new DefaultThreadFactory(
/*  32: 57 */     DefaultThreadFactory.toPoolName(getClass()), false, 5, null);
/*  33: 58 */   private final TaskRunner taskRunner = new TaskRunner();
/*  34: 59 */   private final AtomicBoolean started = new AtomicBoolean();
/*  35:    */   volatile Thread thread;
/*  36: 62 */   private final Future<?> terminationFuture = new FailedFuture(this, new UnsupportedOperationException());
/*  37:    */   
/*  38:    */   private GlobalEventExecutor()
/*  39:    */   {
/*  40: 65 */     scheduledTaskQueue().add(this.quietPeriodTask);
/*  41:    */   }
/*  42:    */   
/*  43:    */   Runnable takeTask()
/*  44:    */   {
/*  45: 74 */     BlockingQueue<Runnable> taskQueue = this.taskQueue;
/*  46:    */     for (;;)
/*  47:    */     {
/*  48: 76 */       ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
/*  49: 77 */       if (scheduledTask == null)
/*  50:    */       {
/*  51: 78 */         Runnable task = null;
/*  52:    */         try
/*  53:    */         {
/*  54: 80 */           task = (Runnable)taskQueue.take();
/*  55:    */         }
/*  56:    */         catch (InterruptedException localInterruptedException1) {}
/*  57: 84 */         return task;
/*  58:    */       }
/*  59: 86 */       long delayNanos = scheduledTask.delayNanos();
/*  60:    */       Runnable task;
/*  61: 88 */       if (delayNanos > 0L) {
/*  62:    */         try
/*  63:    */         {
/*  64: 90 */           task = (Runnable)taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
/*  65:    */         }
/*  66:    */         catch (InterruptedException e)
/*  67:    */         {
/*  68:    */           Runnable task;
/*  69: 93 */           return null;
/*  70:    */         }
/*  71:    */       } else {
/*  72: 96 */         task = (Runnable)taskQueue.poll();
/*  73:    */       }
/*  74: 99 */       if (task == null)
/*  75:    */       {
/*  76:100 */         fetchFromScheduledTaskQueue();
/*  77:101 */         task = (Runnable)taskQueue.poll();
/*  78:    */       }
/*  79:104 */       if (task != null) {
/*  80:105 */         return task;
/*  81:    */       }
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   private void fetchFromScheduledTaskQueue()
/*  86:    */   {
/*  87:112 */     long nanoTime = AbstractScheduledEventExecutor.nanoTime();
/*  88:113 */     Runnable scheduledTask = pollScheduledTask(nanoTime);
/*  89:114 */     while (scheduledTask != null)
/*  90:    */     {
/*  91:115 */       this.taskQueue.add(scheduledTask);
/*  92:116 */       scheduledTask = pollScheduledTask(nanoTime);
/*  93:    */     }
/*  94:    */   }
/*  95:    */   
/*  96:    */   public int pendingTasks()
/*  97:    */   {
/*  98:127 */     return this.taskQueue.size();
/*  99:    */   }
/* 100:    */   
/* 101:    */   private void addTask(Runnable task)
/* 102:    */   {
/* 103:135 */     if (task == null) {
/* 104:136 */       throw new NullPointerException("task");
/* 105:    */     }
/* 106:138 */     this.taskQueue.add(task);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public boolean inEventLoop(Thread thread)
/* 110:    */   {
/* 111:143 */     return thread == this.thread;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 115:    */   {
/* 116:148 */     return terminationFuture();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public Future<?> terminationFuture()
/* 120:    */   {
/* 121:153 */     return this.terminationFuture;
/* 122:    */   }
/* 123:    */   
/* 124:    */   @Deprecated
/* 125:    */   public void shutdown()
/* 126:    */   {
/* 127:159 */     throw new UnsupportedOperationException();
/* 128:    */   }
/* 129:    */   
/* 130:    */   public boolean isShuttingDown()
/* 131:    */   {
/* 132:164 */     return false;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public boolean isShutdown()
/* 136:    */   {
/* 137:169 */     return false;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public boolean isTerminated()
/* 141:    */   {
/* 142:174 */     return false;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 146:    */   {
/* 147:179 */     return false;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public boolean awaitInactivity(long timeout, TimeUnit unit)
/* 151:    */     throws InterruptedException
/* 152:    */   {
/* 153:191 */     if (unit == null) {
/* 154:192 */       throw new NullPointerException("unit");
/* 155:    */     }
/* 156:195 */     Thread thread = this.thread;
/* 157:196 */     if (thread == null) {
/* 158:197 */       throw new IllegalStateException("thread was not started");
/* 159:    */     }
/* 160:199 */     thread.join(unit.toMillis(timeout));
/* 161:200 */     return !thread.isAlive();
/* 162:    */   }
/* 163:    */   
/* 164:    */   public void execute(Runnable task)
/* 165:    */   {
/* 166:205 */     if (task == null) {
/* 167:206 */       throw new NullPointerException("task");
/* 168:    */     }
/* 169:209 */     addTask(task);
/* 170:210 */     if (!inEventLoop()) {
/* 171:211 */       startThread();
/* 172:    */     }
/* 173:    */   }
/* 174:    */   
/* 175:    */   private void startThread()
/* 176:    */   {
/* 177:216 */     if (this.started.compareAndSet(false, true))
/* 178:    */     {
/* 179:217 */       Thread t = this.threadFactory.newThread(this.taskRunner);
/* 180:    */       
/* 181:    */ 
/* 182:    */ 
/* 183:    */ 
/* 184:    */ 
/* 185:223 */       t.setContextClassLoader(null);
/* 186:    */       
/* 187:    */ 
/* 188:    */ 
/* 189:    */ 
/* 190:228 */       this.thread = t;
/* 191:229 */       t.start();
/* 192:    */     }
/* 193:    */   }
/* 194:    */   
/* 195:    */   final class TaskRunner
/* 196:    */     implements Runnable
/* 197:    */   {
/* 198:    */     TaskRunner() {}
/* 199:    */     
/* 200:    */     public void run()
/* 201:    */     {
/* 202:    */       for (;;)
/* 203:    */       {
/* 204:237 */         Runnable task = GlobalEventExecutor.this.takeTask();
/* 205:238 */         if (task != null)
/* 206:    */         {
/* 207:    */           try
/* 208:    */           {
/* 209:240 */             task.run();
/* 210:    */           }
/* 211:    */           catch (Throwable t)
/* 212:    */           {
/* 213:242 */             GlobalEventExecutor.logger.warn("Unexpected exception from the global event executor: ", t);
/* 214:    */           }
/* 215:245 */           if (task != GlobalEventExecutor.this.quietPeriodTask) {}
/* 216:    */         }
/* 217:    */         else
/* 218:    */         {
/* 219:250 */           Queue<ScheduledFutureTask<?>> scheduledTaskQueue = GlobalEventExecutor.this.scheduledTaskQueue;
/* 220:252 */           if ((GlobalEventExecutor.this.taskQueue.isEmpty()) && ((scheduledTaskQueue == null) || (scheduledTaskQueue.size() == 1)))
/* 221:    */           {
/* 222:256 */             boolean stopped = GlobalEventExecutor.this.started.compareAndSet(true, false);
/* 223:257 */             assert (stopped);
/* 224:260 */             if ((GlobalEventExecutor.this.taskQueue.isEmpty()) && ((scheduledTaskQueue == null) || (scheduledTaskQueue.size() == 1))) {
/* 225:    */               break;
/* 226:    */             }
/* 227:269 */             if (!GlobalEventExecutor.this.started.compareAndSet(false, true)) {
/* 228:    */               break;
/* 229:    */             }
/* 230:    */           }
/* 231:    */         }
/* 232:    */       }
/* 233:    */     }
/* 234:    */   }
/* 235:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.GlobalEventExecutor
 * JD-Core Version:    0.7.0.1
 */