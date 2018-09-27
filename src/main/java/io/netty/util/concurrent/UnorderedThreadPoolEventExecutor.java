/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.util.Collections;
/*   6:    */ import java.util.Iterator;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.Set;
/*   9:    */ import java.util.concurrent.Callable;
/*  10:    */ import java.util.concurrent.Delayed;
/*  11:    */ import java.util.concurrent.RejectedExecutionHandler;
/*  12:    */ import java.util.concurrent.RunnableScheduledFuture;
/*  13:    */ import java.util.concurrent.ScheduledThreadPoolExecutor;
/*  14:    */ import java.util.concurrent.ThreadFactory;
/*  15:    */ import java.util.concurrent.TimeUnit;
/*  16:    */ 
/*  17:    */ public final class UnorderedThreadPoolEventExecutor
/*  18:    */   extends ScheduledThreadPoolExecutor
/*  19:    */   implements EventExecutor
/*  20:    */ {
/*  21: 43 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnorderedThreadPoolEventExecutor.class);
/*  22: 46 */   private final Promise<?> terminationFuture = GlobalEventExecutor.INSTANCE.newPromise();
/*  23: 47 */   private final Set<EventExecutor> executorSet = Collections.singleton(this);
/*  24:    */   
/*  25:    */   public UnorderedThreadPoolEventExecutor(int corePoolSize)
/*  26:    */   {
/*  27: 54 */     this(corePoolSize, new DefaultThreadFactory(UnorderedThreadPoolEventExecutor.class));
/*  28:    */   }
/*  29:    */   
/*  30:    */   public UnorderedThreadPoolEventExecutor(int corePoolSize, ThreadFactory threadFactory)
/*  31:    */   {
/*  32: 61 */     super(corePoolSize, threadFactory);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public UnorderedThreadPoolEventExecutor(int corePoolSize, RejectedExecutionHandler handler)
/*  36:    */   {
/*  37: 69 */     this(corePoolSize, new DefaultThreadFactory(UnorderedThreadPoolEventExecutor.class), handler);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public UnorderedThreadPoolEventExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler)
/*  41:    */   {
/*  42: 77 */     super(corePoolSize, threadFactory, handler);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public EventExecutor next()
/*  46:    */   {
/*  47: 82 */     return this;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public EventExecutorGroup parent()
/*  51:    */   {
/*  52: 87 */     return this;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public boolean inEventLoop()
/*  56:    */   {
/*  57: 92 */     return false;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public boolean inEventLoop(Thread thread)
/*  61:    */   {
/*  62: 97 */     return false;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public <V> Promise<V> newPromise()
/*  66:    */   {
/*  67:102 */     return new DefaultPromise(this);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public <V> ProgressivePromise<V> newProgressivePromise()
/*  71:    */   {
/*  72:107 */     return new DefaultProgressivePromise(this);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public <V> Future<V> newSucceededFuture(V result)
/*  76:    */   {
/*  77:112 */     return new SucceededFuture(this, result);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public <V> Future<V> newFailedFuture(Throwable cause)
/*  81:    */   {
/*  82:117 */     return new FailedFuture(this, cause);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public boolean isShuttingDown()
/*  86:    */   {
/*  87:122 */     return isShutdown();
/*  88:    */   }
/*  89:    */   
/*  90:    */   public List<Runnable> shutdownNow()
/*  91:    */   {
/*  92:127 */     List<Runnable> tasks = super.shutdownNow();
/*  93:128 */     this.terminationFuture.trySuccess(null);
/*  94:129 */     return tasks;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public void shutdown()
/*  98:    */   {
/*  99:134 */     super.shutdown();
/* 100:135 */     this.terminationFuture.trySuccess(null);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public Future<?> shutdownGracefully()
/* 104:    */   {
/* 105:140 */     return shutdownGracefully(2L, 15L, TimeUnit.SECONDS);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 109:    */   {
/* 110:147 */     shutdown();
/* 111:148 */     return terminationFuture();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public Future<?> terminationFuture()
/* 115:    */   {
/* 116:153 */     return this.terminationFuture;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public Iterator<EventExecutor> iterator()
/* 120:    */   {
/* 121:158 */     return this.executorSet.iterator();
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task)
/* 125:    */   {
/* 126:163 */     return (runnable instanceof NonNotifyRunnable) ? task : new RunnableScheduledFutureTask(this, runnable, task);
/* 127:    */   }
/* 128:    */   
/* 129:    */   protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task)
/* 130:    */   {
/* 131:169 */     return new RunnableScheduledFutureTask(this, callable, task);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/* 135:    */   {
/* 136:174 */     return (ScheduledFuture)super.schedule(command, delay, unit);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/* 140:    */   {
/* 141:179 */     return (ScheduledFuture)super.schedule(callable, delay, unit);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 145:    */   {
/* 146:184 */     return (ScheduledFuture)super.scheduleAtFixedRate(command, initialDelay, period, unit);
/* 147:    */   }
/* 148:    */   
/* 149:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 150:    */   {
/* 151:189 */     return (ScheduledFuture)super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
/* 152:    */   }
/* 153:    */   
/* 154:    */   public Future<?> submit(Runnable task)
/* 155:    */   {
/* 156:194 */     return (Future)super.submit(task);
/* 157:    */   }
/* 158:    */   
/* 159:    */   public <T> Future<T> submit(Runnable task, T result)
/* 160:    */   {
/* 161:199 */     return (Future)super.submit(task, result);
/* 162:    */   }
/* 163:    */   
/* 164:    */   public <T> Future<T> submit(Callable<T> task)
/* 165:    */   {
/* 166:204 */     return (Future)super.submit(task);
/* 167:    */   }
/* 168:    */   
/* 169:    */   public void execute(Runnable command)
/* 170:    */   {
/* 171:209 */     super.schedule(new NonNotifyRunnable(command), 0L, TimeUnit.NANOSECONDS);
/* 172:    */   }
/* 173:    */   
/* 174:    */   private static final class RunnableScheduledFutureTask<V>
/* 175:    */     extends PromiseTask<V>
/* 176:    */     implements RunnableScheduledFuture<V>, ScheduledFuture<V>
/* 177:    */   {
/* 178:    */     private final RunnableScheduledFuture<V> future;
/* 179:    */     
/* 180:    */     RunnableScheduledFutureTask(EventExecutor executor, Runnable runnable, RunnableScheduledFuture<V> future)
/* 181:    */     {
/* 182:218 */       super(runnable, null);
/* 183:219 */       this.future = future;
/* 184:    */     }
/* 185:    */     
/* 186:    */     RunnableScheduledFutureTask(EventExecutor executor, Callable<V> callable, RunnableScheduledFuture<V> future)
/* 187:    */     {
/* 188:224 */       super(callable);
/* 189:225 */       this.future = future;
/* 190:    */     }
/* 191:    */     
/* 192:    */     public void run()
/* 193:    */     {
/* 194:230 */       if (!isPeriodic()) {
/* 195:231 */         super.run();
/* 196:232 */       } else if (!isDone()) {
/* 197:    */         try
/* 198:    */         {
/* 199:235 */           this.task.call();
/* 200:    */         }
/* 201:    */         catch (Throwable cause)
/* 202:    */         {
/* 203:237 */           if (!tryFailureInternal(cause)) {
/* 204:238 */             UnorderedThreadPoolEventExecutor.logger.warn("Failure during execution of task", cause);
/* 205:    */           }
/* 206:    */         }
/* 207:    */       }
/* 208:    */     }
/* 209:    */     
/* 210:    */     public boolean isPeriodic()
/* 211:    */     {
/* 212:246 */       return this.future.isPeriodic();
/* 213:    */     }
/* 214:    */     
/* 215:    */     public long getDelay(TimeUnit unit)
/* 216:    */     {
/* 217:251 */       return this.future.getDelay(unit);
/* 218:    */     }
/* 219:    */     
/* 220:    */     public int compareTo(Delayed o)
/* 221:    */     {
/* 222:256 */       return this.future.compareTo(o);
/* 223:    */     }
/* 224:    */   }
/* 225:    */   
/* 226:    */   private static final class NonNotifyRunnable
/* 227:    */     implements Runnable
/* 228:    */   {
/* 229:    */     private final Runnable task;
/* 230:    */     
/* 231:    */     NonNotifyRunnable(Runnable task)
/* 232:    */     {
/* 233:272 */       this.task = task;
/* 234:    */     }
/* 235:    */     
/* 236:    */     public void run()
/* 237:    */     {
/* 238:277 */       this.task.run();
/* 239:    */     }
/* 240:    */   }
/* 241:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.UnorderedThreadPoolEventExecutor
 * JD-Core Version:    0.7.0.1
 */