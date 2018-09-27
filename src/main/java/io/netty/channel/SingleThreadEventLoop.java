/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.RejectedExecutionHandler;
/*   4:    */ import io.netty.util.concurrent.RejectedExecutionHandlers;
/*   5:    */ import io.netty.util.concurrent.SingleThreadEventExecutor;
/*   6:    */ import io.netty.util.internal.ObjectUtil;
/*   7:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   8:    */ import java.util.Queue;
/*   9:    */ import java.util.concurrent.Executor;
/*  10:    */ import java.util.concurrent.ThreadFactory;
/*  11:    */ 
/*  12:    */ public abstract class SingleThreadEventLoop
/*  13:    */   extends SingleThreadEventExecutor
/*  14:    */   implements EventLoop
/*  15:    */ {
/*  16: 35 */   protected static final int DEFAULT_MAX_PENDING_TASKS = Math.max(16, 
/*  17: 36 */     SystemPropertyUtil.getInt("io.netty.eventLoop.maxPendingTasks", 2147483647));
/*  18:    */   private final Queue<Runnable> tailTasks;
/*  19:    */   
/*  20:    */   protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp)
/*  21:    */   {
/*  22: 41 */     this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
/*  23:    */   }
/*  24:    */   
/*  25:    */   protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp)
/*  26:    */   {
/*  27: 45 */     this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
/*  28:    */   }
/*  29:    */   
/*  30:    */   protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler)
/*  31:    */   {
/*  32: 51 */     super(parent, threadFactory, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
/*  33: 52 */     this.tailTasks = newTaskQueue(maxPendingTasks);
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler)
/*  37:    */   {
/*  38: 58 */     super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
/*  39: 59 */     this.tailTasks = newTaskQueue(maxPendingTasks);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public EventLoopGroup parent()
/*  43:    */   {
/*  44: 64 */     return (EventLoopGroup)super.parent();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public EventLoop next()
/*  48:    */   {
/*  49: 69 */     return (EventLoop)super.next();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ChannelFuture register(Channel channel)
/*  53:    */   {
/*  54: 74 */     return register(new DefaultChannelPromise(channel, this));
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ChannelFuture register(ChannelPromise promise)
/*  58:    */   {
/*  59: 79 */     ObjectUtil.checkNotNull(promise, "promise");
/*  60: 80 */     promise.channel().unsafe().register(this, promise);
/*  61: 81 */     return promise;
/*  62:    */   }
/*  63:    */   
/*  64:    */   @Deprecated
/*  65:    */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/*  66:    */   {
/*  67: 87 */     if (channel == null) {
/*  68: 88 */       throw new NullPointerException("channel");
/*  69:    */     }
/*  70: 90 */     if (promise == null) {
/*  71: 91 */       throw new NullPointerException("promise");
/*  72:    */     }
/*  73: 94 */     channel.unsafe().register(this, promise);
/*  74: 95 */     return promise;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public final void executeAfterEventLoopIteration(Runnable task)
/*  78:    */   {
/*  79:105 */     ObjectUtil.checkNotNull(task, "task");
/*  80:106 */     if (isShutdown()) {
/*  81:107 */       reject();
/*  82:    */     }
/*  83:110 */     if (!this.tailTasks.offer(task)) {
/*  84:111 */       reject(task);
/*  85:    */     }
/*  86:114 */     if (wakesUpForTask(task)) {
/*  87:115 */       wakeup(inEventLoop());
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   final boolean removeAfterEventLoopIterationTask(Runnable task)
/*  92:    */   {
/*  93:128 */     return this.tailTasks.remove(ObjectUtil.checkNotNull(task, "task"));
/*  94:    */   }
/*  95:    */   
/*  96:    */   protected boolean wakesUpForTask(Runnable task)
/*  97:    */   {
/*  98:133 */     return !(task instanceof NonWakeupRunnable);
/*  99:    */   }
/* 100:    */   
/* 101:    */   protected void afterRunningAllTasks()
/* 102:    */   {
/* 103:138 */     runAllTasksFrom(this.tailTasks);
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected boolean hasTasks()
/* 107:    */   {
/* 108:143 */     return (super.hasTasks()) || (!this.tailTasks.isEmpty());
/* 109:    */   }
/* 110:    */   
/* 111:    */   public int pendingTasks()
/* 112:    */   {
/* 113:148 */     return super.pendingTasks() + this.tailTasks.size();
/* 114:    */   }
/* 115:    */   
/* 116:    */   static abstract interface NonWakeupRunnable
/* 117:    */     extends Runnable
/* 118:    */   {}
/* 119:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.SingleThreadEventLoop
 * JD-Core Version:    0.7.0.1
 */