/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.Collection;
/*   6:    */ import java.util.Iterator;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.Queue;
/*   9:    */ import java.util.concurrent.Callable;
/*  10:    */ import java.util.concurrent.ExecutionException;
/*  11:    */ import java.util.concurrent.RejectedExecutionException;
/*  12:    */ import java.util.concurrent.TimeUnit;
/*  13:    */ import java.util.concurrent.TimeoutException;
/*  14:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  15:    */ 
/*  16:    */ public final class NonStickyEventExecutorGroup
/*  17:    */   implements EventExecutorGroup
/*  18:    */ {
/*  19:    */   private final EventExecutorGroup group;
/*  20:    */   private final int maxTaskExecutePerRun;
/*  21:    */   
/*  22:    */   public NonStickyEventExecutorGroup(EventExecutorGroup group)
/*  23:    */   {
/*  24: 50 */     this(group, 1024);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public NonStickyEventExecutorGroup(EventExecutorGroup group, int maxTaskExecutePerRun)
/*  28:    */   {
/*  29: 58 */     this.group = verify(group);
/*  30: 59 */     this.maxTaskExecutePerRun = ObjectUtil.checkPositive(maxTaskExecutePerRun, "maxTaskExecutePerRun");
/*  31:    */   }
/*  32:    */   
/*  33:    */   private static EventExecutorGroup verify(EventExecutorGroup group)
/*  34:    */   {
/*  35: 63 */     Iterator<EventExecutor> executors = ((EventExecutorGroup)ObjectUtil.checkNotNull(group, "group")).iterator();
/*  36: 64 */     while (executors.hasNext())
/*  37:    */     {
/*  38: 65 */       EventExecutor executor = (EventExecutor)executors.next();
/*  39: 66 */       if ((executor instanceof OrderedEventExecutor)) {
/*  40: 67 */         throw new IllegalArgumentException("EventExecutorGroup " + group + " contains OrderedEventExecutors: " + executor);
/*  41:    */       }
/*  42:    */     }
/*  43: 71 */     return group;
/*  44:    */   }
/*  45:    */   
/*  46:    */   private NonStickyOrderedEventExecutor newExecutor(EventExecutor executor)
/*  47:    */   {
/*  48: 75 */     return new NonStickyOrderedEventExecutor(executor, this.maxTaskExecutePerRun);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public boolean isShuttingDown()
/*  52:    */   {
/*  53: 80 */     return this.group.isShuttingDown();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public Future<?> shutdownGracefully()
/*  57:    */   {
/*  58: 85 */     return this.group.shutdownGracefully();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/*  62:    */   {
/*  63: 90 */     return this.group.shutdownGracefully(quietPeriod, timeout, unit);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public Future<?> terminationFuture()
/*  67:    */   {
/*  68: 95 */     return this.group.terminationFuture();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void shutdown()
/*  72:    */   {
/*  73:101 */     this.group.shutdown();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public List<Runnable> shutdownNow()
/*  77:    */   {
/*  78:107 */     return this.group.shutdownNow();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public EventExecutor next()
/*  82:    */   {
/*  83:112 */     return newExecutor(this.group.next());
/*  84:    */   }
/*  85:    */   
/*  86:    */   public Iterator<EventExecutor> iterator()
/*  87:    */   {
/*  88:117 */     final Iterator<EventExecutor> itr = this.group.iterator();
/*  89:118 */     new Iterator()
/*  90:    */     {
/*  91:    */       public boolean hasNext()
/*  92:    */       {
/*  93:121 */         return itr.hasNext();
/*  94:    */       }
/*  95:    */       
/*  96:    */       public EventExecutor next()
/*  97:    */       {
/*  98:126 */         return NonStickyEventExecutorGroup.this.newExecutor((EventExecutor)itr.next());
/*  99:    */       }
/* 100:    */       
/* 101:    */       public void remove()
/* 102:    */       {
/* 103:131 */         itr.remove();
/* 104:    */       }
/* 105:    */     };
/* 106:    */   }
/* 107:    */   
/* 108:    */   public Future<?> submit(Runnable task)
/* 109:    */   {
/* 110:138 */     return this.group.submit(task);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public <T> Future<T> submit(Runnable task, T result)
/* 114:    */   {
/* 115:143 */     return this.group.submit(task, result);
/* 116:    */   }
/* 117:    */   
/* 118:    */   public <T> Future<T> submit(Callable<T> task)
/* 119:    */   {
/* 120:148 */     return this.group.submit(task);
/* 121:    */   }
/* 122:    */   
/* 123:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/* 124:    */   {
/* 125:153 */     return this.group.schedule(command, delay, unit);
/* 126:    */   }
/* 127:    */   
/* 128:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/* 129:    */   {
/* 130:158 */     return this.group.schedule(callable, delay, unit);
/* 131:    */   }
/* 132:    */   
/* 133:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 134:    */   {
/* 135:163 */     return this.group.scheduleAtFixedRate(command, initialDelay, period, unit);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 139:    */   {
/* 140:168 */     return this.group.scheduleWithFixedDelay(command, initialDelay, delay, unit);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean isShutdown()
/* 144:    */   {
/* 145:173 */     return this.group.isShutdown();
/* 146:    */   }
/* 147:    */   
/* 148:    */   public boolean isTerminated()
/* 149:    */   {
/* 150:178 */     return this.group.isTerminated();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 154:    */     throws InterruptedException
/* 155:    */   {
/* 156:183 */     return this.group.awaitTermination(timeout, unit);
/* 157:    */   }
/* 158:    */   
/* 159:    */   public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
/* 160:    */     throws InterruptedException
/* 161:    */   {
/* 162:189 */     return this.group.invokeAll(tasks);
/* 163:    */   }
/* 164:    */   
/* 165:    */   public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
/* 166:    */     throws InterruptedException
/* 167:    */   {
/* 168:195 */     return this.group.invokeAll(tasks, timeout, unit);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
/* 172:    */     throws InterruptedException, ExecutionException
/* 173:    */   {
/* 174:200 */     return this.group.invokeAny(tasks);
/* 175:    */   }
/* 176:    */   
/* 177:    */   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
/* 178:    */     throws InterruptedException, ExecutionException, TimeoutException
/* 179:    */   {
/* 180:206 */     return this.group.invokeAny(tasks, timeout, unit);
/* 181:    */   }
/* 182:    */   
/* 183:    */   public void execute(Runnable command)
/* 184:    */   {
/* 185:211 */     this.group.execute(command);
/* 186:    */   }
/* 187:    */   
/* 188:    */   private static final class NonStickyOrderedEventExecutor
/* 189:    */     extends AbstractEventExecutor
/* 190:    */     implements Runnable, OrderedEventExecutor
/* 191:    */   {
/* 192:    */     private final EventExecutor executor;
/* 193:217 */     private final Queue<Runnable> tasks = PlatformDependent.newMpscQueue();
/* 194:    */     private static final int NONE = 0;
/* 195:    */     private static final int SUBMITTED = 1;
/* 196:    */     private static final int RUNNING = 2;
/* 197:223 */     private final AtomicInteger state = new AtomicInteger();
/* 198:    */     private final int maxTaskExecutePerRun;
/* 199:    */     
/* 200:    */     NonStickyOrderedEventExecutor(EventExecutor executor, int maxTaskExecutePerRun)
/* 201:    */     {
/* 202:227 */       super();
/* 203:228 */       this.executor = executor;
/* 204:229 */       this.maxTaskExecutePerRun = maxTaskExecutePerRun;
/* 205:    */     }
/* 206:    */     
/* 207:    */     public void run()
/* 208:    */     {
/* 209:234 */       if (!this.state.compareAndSet(1, 2)) {
/* 210:235 */         return;
/* 211:    */       }
/* 212:    */       for (;;)
/* 213:    */       {
/* 214:238 */         int i = 0;
/* 215:    */         try
/* 216:    */         {
/* 217:240 */           for (; i < this.maxTaskExecutePerRun; i++)
/* 218:    */           {
/* 219:241 */             Runnable task = (Runnable)this.tasks.poll();
/* 220:242 */             if (task == null) {
/* 221:    */               break;
/* 222:    */             }
/* 223:245 */             safeExecute(task);
/* 224:    */           }
/* 225:248 */           if (i == this.maxTaskExecutePerRun) {
/* 226:    */             try
/* 227:    */             {
/* 228:250 */               this.state.set(1);
/* 229:251 */               this.executor.execute(this); return;
/* 230:    */             }
/* 231:    */             catch (Throwable ignore)
/* 232:    */             {
/* 233:255 */               this.state.set(2);
/* 234:    */             }
/* 235:    */           }
/* 236:261 */           this.state.set(0); return;
/* 237:    */         }
/* 238:    */         finally
/* 239:    */         {
/* 240:248 */           if (i == this.maxTaskExecutePerRun)
/* 241:    */           {
/* 242:    */             try
/* 243:    */             {
/* 244:250 */               this.state.set(1);
/* 245:251 */               this.executor.execute(this);
/* 246:252 */               return;
/* 247:    */             }
/* 248:    */             catch (Throwable ignore)
/* 249:    */             {
/* 250:255 */               this.state.set(2);
/* 251:    */             }
/* 252:    */           }
/* 253:    */           else
/* 254:    */           {
/* 255:261 */             this.state.set(0);
/* 256:262 */             return;
/* 257:    */           }
/* 258:    */         }
/* 259:262 */         throw localObject;
/* 260:    */       }
/* 261:    */     }
/* 262:    */     
/* 263:    */     public boolean inEventLoop(Thread thread)
/* 264:    */     {
/* 265:270 */       return false;
/* 266:    */     }
/* 267:    */     
/* 268:    */     public boolean inEventLoop()
/* 269:    */     {
/* 270:275 */       return false;
/* 271:    */     }
/* 272:    */     
/* 273:    */     public boolean isShuttingDown()
/* 274:    */     {
/* 275:280 */       return this.executor.isShutdown();
/* 276:    */     }
/* 277:    */     
/* 278:    */     public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/* 279:    */     {
/* 280:285 */       return this.executor.shutdownGracefully(quietPeriod, timeout, unit);
/* 281:    */     }
/* 282:    */     
/* 283:    */     public Future<?> terminationFuture()
/* 284:    */     {
/* 285:290 */       return this.executor.terminationFuture();
/* 286:    */     }
/* 287:    */     
/* 288:    */     public void shutdown()
/* 289:    */     {
/* 290:295 */       this.executor.shutdown();
/* 291:    */     }
/* 292:    */     
/* 293:    */     public boolean isShutdown()
/* 294:    */     {
/* 295:300 */       return this.executor.isShutdown();
/* 296:    */     }
/* 297:    */     
/* 298:    */     public boolean isTerminated()
/* 299:    */     {
/* 300:305 */       return this.executor.isTerminated();
/* 301:    */     }
/* 302:    */     
/* 303:    */     public boolean awaitTermination(long timeout, TimeUnit unit)
/* 304:    */       throws InterruptedException
/* 305:    */     {
/* 306:310 */       return this.executor.awaitTermination(timeout, unit);
/* 307:    */     }
/* 308:    */     
/* 309:    */     public void execute(Runnable command)
/* 310:    */     {
/* 311:315 */       if (!this.tasks.offer(command)) {
/* 312:316 */         throw new RejectedExecutionException();
/* 313:    */       }
/* 314:318 */       if (this.state.compareAndSet(0, 1)) {
/* 315:    */         try
/* 316:    */         {
/* 317:322 */           this.executor.execute(this);
/* 318:    */         }
/* 319:    */         catch (Throwable e)
/* 320:    */         {
/* 321:325 */           this.tasks.remove(command);
/* 322:326 */           PlatformDependent.throwException(e);
/* 323:    */         }
/* 324:    */       }
/* 325:    */     }
/* 326:    */   }
/* 327:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.NonStickyEventExecutorGroup
 * JD-Core Version:    0.7.0.1
 */