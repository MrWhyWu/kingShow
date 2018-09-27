/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.DefaultPriorityQueue;
/*   4:    */ import io.netty.util.internal.PriorityQueueNode;
/*   5:    */ import java.util.Queue;
/*   6:    */ import java.util.concurrent.Callable;
/*   7:    */ import java.util.concurrent.Delayed;
/*   8:    */ import java.util.concurrent.TimeUnit;
/*   9:    */ import java.util.concurrent.atomic.AtomicLong;
/*  10:    */ 
/*  11:    */ final class ScheduledFutureTask<V>
/*  12:    */   extends PromiseTask<V>
/*  13:    */   implements ScheduledFuture<V>, PriorityQueueNode
/*  14:    */ {
/*  15: 30 */   private static final AtomicLong nextTaskId = new AtomicLong();
/*  16: 31 */   private static final long START_TIME = System.nanoTime();
/*  17:    */   
/*  18:    */   static long nanoTime()
/*  19:    */   {
/*  20: 34 */     return System.nanoTime() - START_TIME;
/*  21:    */   }
/*  22:    */   
/*  23:    */   static long deadlineNanos(long delay)
/*  24:    */   {
/*  25: 38 */     return nanoTime() + delay;
/*  26:    */   }
/*  27:    */   
/*  28: 41 */   private final long id = nextTaskId.getAndIncrement();
/*  29:    */   private long deadlineNanos;
/*  30:    */   private final long periodNanos;
/*  31: 46 */   private int queueIndex = -1;
/*  32:    */   
/*  33:    */   ScheduledFutureTask(AbstractScheduledEventExecutor executor, Runnable runnable, V result, long nanoTime)
/*  34:    */   {
/*  35: 52 */     this(executor, toCallable(runnable, result), nanoTime);
/*  36:    */   }
/*  37:    */   
/*  38:    */   ScheduledFutureTask(AbstractScheduledEventExecutor executor, Callable<V> callable, long nanoTime, long period)
/*  39:    */   {
/*  40: 59 */     super(executor, callable);
/*  41: 60 */     if (period == 0L) {
/*  42: 61 */       throw new IllegalArgumentException("period: 0 (expected: != 0)");
/*  43:    */     }
/*  44: 63 */     this.deadlineNanos = nanoTime;
/*  45: 64 */     this.periodNanos = period;
/*  46:    */   }
/*  47:    */   
/*  48:    */   ScheduledFutureTask(AbstractScheduledEventExecutor executor, Callable<V> callable, long nanoTime)
/*  49:    */   {
/*  50: 71 */     super(executor, callable);
/*  51: 72 */     this.deadlineNanos = nanoTime;
/*  52: 73 */     this.periodNanos = 0L;
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected EventExecutor executor()
/*  56:    */   {
/*  57: 78 */     return super.executor();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public long deadlineNanos()
/*  61:    */   {
/*  62: 82 */     return this.deadlineNanos;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public long delayNanos()
/*  66:    */   {
/*  67: 86 */     return Math.max(0L, deadlineNanos() - nanoTime());
/*  68:    */   }
/*  69:    */   
/*  70:    */   public long delayNanos(long currentTimeNanos)
/*  71:    */   {
/*  72: 90 */     return Math.max(0L, deadlineNanos() - (currentTimeNanos - START_TIME));
/*  73:    */   }
/*  74:    */   
/*  75:    */   public long getDelay(TimeUnit unit)
/*  76:    */   {
/*  77: 95 */     return unit.convert(delayNanos(), TimeUnit.NANOSECONDS);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public int compareTo(Delayed o)
/*  81:    */   {
/*  82:100 */     if (this == o) {
/*  83:101 */       return 0;
/*  84:    */     }
/*  85:104 */     ScheduledFutureTask<?> that = (ScheduledFutureTask)o;
/*  86:105 */     long d = deadlineNanos() - that.deadlineNanos();
/*  87:106 */     if (d < 0L) {
/*  88:107 */       return -1;
/*  89:    */     }
/*  90:108 */     if (d > 0L) {
/*  91:109 */       return 1;
/*  92:    */     }
/*  93:110 */     if (this.id < that.id) {
/*  94:111 */       return -1;
/*  95:    */     }
/*  96:112 */     if (this.id == that.id) {
/*  97:113 */       throw new Error();
/*  98:    */     }
/*  99:115 */     return 1;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void run()
/* 103:    */   {
/* 104:121 */     assert (executor().inEventLoop());
/* 105:    */     try
/* 106:    */     {
/* 107:123 */       if (this.periodNanos == 0L)
/* 108:    */       {
/* 109:124 */         if (setUncancellableInternal())
/* 110:    */         {
/* 111:125 */           V result = this.task.call();
/* 112:126 */           setSuccessInternal(result);
/* 113:    */         }
/* 114:    */       }
/* 115:130 */       else if (!isCancelled())
/* 116:    */       {
/* 117:131 */         this.task.call();
/* 118:132 */         if (!executor().isShutdown())
/* 119:    */         {
/* 120:133 */           long p = this.periodNanos;
/* 121:134 */           if (p > 0L) {
/* 122:135 */             this.deadlineNanos += p;
/* 123:    */           } else {
/* 124:137 */             this.deadlineNanos = (nanoTime() - p);
/* 125:    */           }
/* 126:139 */           if (!isCancelled())
/* 127:    */           {
/* 128:142 */             Queue<ScheduledFutureTask<?>> scheduledTaskQueue = ((AbstractScheduledEventExecutor)executor()).scheduledTaskQueue;
/* 129:143 */             assert (scheduledTaskQueue != null);
/* 130:144 */             scheduledTaskQueue.add(this);
/* 131:    */           }
/* 132:    */         }
/* 133:    */       }
/* 134:    */     }
/* 135:    */     catch (Throwable cause)
/* 136:    */     {
/* 137:150 */       setFailureInternal(cause);
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 142:    */   {
/* 143:156 */     boolean canceled = super.cancel(mayInterruptIfRunning);
/* 144:157 */     if (canceled) {
/* 145:158 */       ((AbstractScheduledEventExecutor)executor()).removeScheduled(this);
/* 146:    */     }
/* 147:160 */     return canceled;
/* 148:    */   }
/* 149:    */   
/* 150:    */   boolean cancelWithoutRemove(boolean mayInterruptIfRunning)
/* 151:    */   {
/* 152:164 */     return super.cancel(mayInterruptIfRunning);
/* 153:    */   }
/* 154:    */   
/* 155:    */   protected StringBuilder toStringBuilder()
/* 156:    */   {
/* 157:169 */     StringBuilder buf = super.toStringBuilder();
/* 158:170 */     buf.setCharAt(buf.length() - 1, ',');
/* 159:    */     
/* 160:172 */     return buf.append(" id: ")
/* 161:173 */       .append(this.id)
/* 162:174 */       .append(", deadline: ")
/* 163:175 */       .append(this.deadlineNanos)
/* 164:176 */       .append(", period: ")
/* 165:177 */       .append(this.periodNanos)
/* 166:178 */       .append(')');
/* 167:    */   }
/* 168:    */   
/* 169:    */   public int priorityQueueIndex(DefaultPriorityQueue<?> queue)
/* 170:    */   {
/* 171:183 */     return this.queueIndex;
/* 172:    */   }
/* 173:    */   
/* 174:    */   public void priorityQueueIndex(DefaultPriorityQueue<?> queue, int i)
/* 175:    */   {
/* 176:188 */     this.queueIndex = i;
/* 177:    */   }
/* 178:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.ScheduledFutureTask
 * JD-Core Version:    0.7.0.1
 */