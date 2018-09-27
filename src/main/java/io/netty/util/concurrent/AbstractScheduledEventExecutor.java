/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.DefaultPriorityQueue;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import io.netty.util.internal.PriorityQueue;
/*   6:    */ import java.util.Comparator;
/*   7:    */ import java.util.Queue;
/*   8:    */ import java.util.concurrent.Callable;
/*   9:    */ import java.util.concurrent.Executors;
/*  10:    */ import java.util.concurrent.TimeUnit;
/*  11:    */ 
/*  12:    */ public abstract class AbstractScheduledEventExecutor
/*  13:    */   extends AbstractEventExecutor
/*  14:    */ {
/*  15: 33 */   private static final Comparator<ScheduledFutureTask<?>> SCHEDULED_FUTURE_TASK_COMPARATOR = new Comparator()
/*  16:    */   {
/*  17:    */     public int compare(ScheduledFutureTask<?> o1, ScheduledFutureTask<?> o2)
/*  18:    */     {
/*  19: 37 */       return o1.compareTo(o2);
/*  20:    */     }
/*  21:    */   };
/*  22:    */   PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue;
/*  23:    */   
/*  24:    */   protected AbstractScheduledEventExecutor() {}
/*  25:    */   
/*  26:    */   protected AbstractScheduledEventExecutor(EventExecutorGroup parent)
/*  27:    */   {
/*  28: 47 */     super(parent);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected static long nanoTime()
/*  32:    */   {
/*  33: 51 */     return ScheduledFutureTask.nanoTime();
/*  34:    */   }
/*  35:    */   
/*  36:    */   PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue()
/*  37:    */   {
/*  38: 55 */     if (this.scheduledTaskQueue == null) {
/*  39: 56 */       this.scheduledTaskQueue = new DefaultPriorityQueue(SCHEDULED_FUTURE_TASK_COMPARATOR, 11);
/*  40:    */     }
/*  41: 61 */     return this.scheduledTaskQueue;
/*  42:    */   }
/*  43:    */   
/*  44:    */   private static boolean isNullOrEmpty(Queue<ScheduledFutureTask<?>> queue)
/*  45:    */   {
/*  46: 65 */     return (queue == null) || (queue.isEmpty());
/*  47:    */   }
/*  48:    */   
/*  49:    */   protected void cancelScheduledTasks()
/*  50:    */   {
/*  51: 74 */     assert (inEventLoop());
/*  52: 75 */     PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
/*  53: 76 */     if (isNullOrEmpty(scheduledTaskQueue)) {
/*  54: 77 */       return;
/*  55:    */     }
/*  56: 81 */     ScheduledFutureTask<?>[] scheduledTasks = (ScheduledFutureTask[])scheduledTaskQueue.toArray(new ScheduledFutureTask[scheduledTaskQueue.size()]);
/*  57: 83 */     for (ScheduledFutureTask<?> task : scheduledTasks) {
/*  58: 84 */       task.cancelWithoutRemove(false);
/*  59:    */     }
/*  60: 87 */     scheduledTaskQueue.clearIgnoringIndexes();
/*  61:    */   }
/*  62:    */   
/*  63:    */   protected final Runnable pollScheduledTask()
/*  64:    */   {
/*  65: 94 */     return pollScheduledTask(nanoTime());
/*  66:    */   }
/*  67:    */   
/*  68:    */   protected final Runnable pollScheduledTask(long nanoTime)
/*  69:    */   {
/*  70:102 */     assert (inEventLoop());
/*  71:    */     
/*  72:104 */     Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
/*  73:105 */     ScheduledFutureTask<?> scheduledTask = scheduledTaskQueue == null ? null : (ScheduledFutureTask)scheduledTaskQueue.peek();
/*  74:106 */     if (scheduledTask == null) {
/*  75:107 */       return null;
/*  76:    */     }
/*  77:110 */     if (scheduledTask.deadlineNanos() <= nanoTime)
/*  78:    */     {
/*  79:111 */       scheduledTaskQueue.remove();
/*  80:112 */       return scheduledTask;
/*  81:    */     }
/*  82:114 */     return null;
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected final long nextScheduledTaskNano()
/*  86:    */   {
/*  87:121 */     Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
/*  88:122 */     ScheduledFutureTask<?> scheduledTask = scheduledTaskQueue == null ? null : (ScheduledFutureTask)scheduledTaskQueue.peek();
/*  89:123 */     if (scheduledTask == null) {
/*  90:124 */       return -1L;
/*  91:    */     }
/*  92:126 */     return Math.max(0L, scheduledTask.deadlineNanos() - nanoTime());
/*  93:    */   }
/*  94:    */   
/*  95:    */   final ScheduledFutureTask<?> peekScheduledTask()
/*  96:    */   {
/*  97:130 */     Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
/*  98:131 */     if (scheduledTaskQueue == null) {
/*  99:132 */       return null;
/* 100:    */     }
/* 101:134 */     return (ScheduledFutureTask)scheduledTaskQueue.peek();
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected final boolean hasScheduledTasks()
/* 105:    */   {
/* 106:141 */     Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
/* 107:142 */     ScheduledFutureTask<?> scheduledTask = scheduledTaskQueue == null ? null : (ScheduledFutureTask)scheduledTaskQueue.peek();
/* 108:143 */     return (scheduledTask != null) && (scheduledTask.deadlineNanos() <= nanoTime());
/* 109:    */   }
/* 110:    */   
/* 111:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/* 112:    */   {
/* 113:148 */     ObjectUtil.checkNotNull(command, "command");
/* 114:149 */     ObjectUtil.checkNotNull(unit, "unit");
/* 115:150 */     if (delay < 0L) {
/* 116:151 */       delay = 0L;
/* 117:    */     }
/* 118:153 */     return schedule(new ScheduledFutureTask(this, command, null, 
/* 119:154 */       ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
/* 120:    */   }
/* 121:    */   
/* 122:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/* 123:    */   {
/* 124:159 */     ObjectUtil.checkNotNull(callable, "callable");
/* 125:160 */     ObjectUtil.checkNotNull(unit, "unit");
/* 126:161 */     if (delay < 0L) {
/* 127:162 */       delay = 0L;
/* 128:    */     }
/* 129:164 */     return schedule(new ScheduledFutureTask(this, callable, 
/* 130:165 */       ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
/* 131:    */   }
/* 132:    */   
/* 133:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 134:    */   {
/* 135:170 */     ObjectUtil.checkNotNull(command, "command");
/* 136:171 */     ObjectUtil.checkNotNull(unit, "unit");
/* 137:172 */     if (initialDelay < 0L) {
/* 138:174 */       throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", new Object[] {Long.valueOf(initialDelay) }));
/* 139:    */     }
/* 140:176 */     if (period <= 0L) {
/* 141:178 */       throw new IllegalArgumentException(String.format("period: %d (expected: > 0)", new Object[] {Long.valueOf(period) }));
/* 142:    */     }
/* 143:181 */     return schedule(new ScheduledFutureTask(this, 
/* 144:182 */       Executors.callable(command, null), 
/* 145:183 */       ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), unit.toNanos(period)));
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 149:    */   {
/* 150:188 */     ObjectUtil.checkNotNull(command, "command");
/* 151:189 */     ObjectUtil.checkNotNull(unit, "unit");
/* 152:190 */     if (initialDelay < 0L) {
/* 153:192 */       throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", new Object[] {Long.valueOf(initialDelay) }));
/* 154:    */     }
/* 155:194 */     if (delay <= 0L) {
/* 156:196 */       throw new IllegalArgumentException(String.format("delay: %d (expected: > 0)", new Object[] {Long.valueOf(delay) }));
/* 157:    */     }
/* 158:199 */     return schedule(new ScheduledFutureTask(this, 
/* 159:200 */       Executors.callable(command, null), 
/* 160:201 */       ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), -unit.toNanos(delay)));
/* 161:    */   }
/* 162:    */   
/* 163:    */   <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task)
/* 164:    */   {
/* 165:205 */     if (inEventLoop()) {
/* 166:206 */       scheduledTaskQueue().add(task);
/* 167:    */     } else {
/* 168:208 */       execute(new Runnable()
/* 169:    */       {
/* 170:    */         public void run()
/* 171:    */         {
/* 172:211 */           AbstractScheduledEventExecutor.this.scheduledTaskQueue().add(task);
/* 173:    */         }
/* 174:    */       });
/* 175:    */     }
/* 176:216 */     return task;
/* 177:    */   }
/* 178:    */   
/* 179:    */   final void removeScheduled(final ScheduledFutureTask<?> task)
/* 180:    */   {
/* 181:220 */     if (inEventLoop()) {
/* 182:221 */       scheduledTaskQueue().removeTyped(task);
/* 183:    */     } else {
/* 184:223 */       execute(new Runnable()
/* 185:    */       {
/* 186:    */         public void run()
/* 187:    */         {
/* 188:226 */           AbstractScheduledEventExecutor.this.removeScheduled(task);
/* 189:    */         }
/* 190:    */       });
/* 191:    */     }
/* 192:    */   }
/* 193:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.AbstractScheduledEventExecutor
 * JD-Core Version:    0.7.0.1
 */