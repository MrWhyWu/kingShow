/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.util.Collection;
/*   6:    */ import java.util.Collections;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.concurrent.AbstractExecutorService;
/*  10:    */ import java.util.concurrent.Callable;
/*  11:    */ import java.util.concurrent.RunnableFuture;
/*  12:    */ import java.util.concurrent.TimeUnit;
/*  13:    */ 
/*  14:    */ public abstract class AbstractEventExecutor
/*  15:    */   extends AbstractExecutorService
/*  16:    */   implements EventExecutor
/*  17:    */ {
/*  18: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractEventExecutor.class);
/*  19:    */   static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2L;
/*  20:    */   static final long DEFAULT_SHUTDOWN_TIMEOUT = 15L;
/*  21:    */   private final EventExecutorGroup parent;
/*  22: 40 */   private final Collection<EventExecutor> selfCollection = Collections.singleton(this);
/*  23:    */   
/*  24:    */   protected AbstractEventExecutor()
/*  25:    */   {
/*  26: 43 */     this(null);
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected AbstractEventExecutor(EventExecutorGroup parent)
/*  30:    */   {
/*  31: 47 */     this.parent = parent;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public EventExecutorGroup parent()
/*  35:    */   {
/*  36: 52 */     return this.parent;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public EventExecutor next()
/*  40:    */   {
/*  41: 57 */     return this;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public boolean inEventLoop()
/*  45:    */   {
/*  46: 62 */     return inEventLoop(Thread.currentThread());
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Iterator<EventExecutor> iterator()
/*  50:    */   {
/*  51: 67 */     return this.selfCollection.iterator();
/*  52:    */   }
/*  53:    */   
/*  54:    */   public Future<?> shutdownGracefully()
/*  55:    */   {
/*  56: 72 */     return shutdownGracefully(2L, 15L, TimeUnit.SECONDS);
/*  57:    */   }
/*  58:    */   
/*  59:    */   @Deprecated
/*  60:    */   public abstract void shutdown();
/*  61:    */   
/*  62:    */   @Deprecated
/*  63:    */   public List<Runnable> shutdownNow()
/*  64:    */   {
/*  65: 88 */     shutdown();
/*  66: 89 */     return Collections.emptyList();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public <V> Promise<V> newPromise()
/*  70:    */   {
/*  71: 94 */     return new DefaultPromise(this);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public <V> ProgressivePromise<V> newProgressivePromise()
/*  75:    */   {
/*  76: 99 */     return new DefaultProgressivePromise(this);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public <V> Future<V> newSucceededFuture(V result)
/*  80:    */   {
/*  81:104 */     return new SucceededFuture(this, result);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public <V> Future<V> newFailedFuture(Throwable cause)
/*  85:    */   {
/*  86:109 */     return new FailedFuture(this, cause);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public Future<?> submit(Runnable task)
/*  90:    */   {
/*  91:114 */     return (Future)super.submit(task);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public <T> Future<T> submit(Runnable task, T result)
/*  95:    */   {
/*  96:119 */     return (Future)super.submit(task, result);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public <T> Future<T> submit(Callable<T> task)
/* 100:    */   {
/* 101:124 */     return (Future)super.submit(task);
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
/* 105:    */   {
/* 106:129 */     return new PromiseTask(this, runnable, value);
/* 107:    */   }
/* 108:    */   
/* 109:    */   protected final <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
/* 110:    */   {
/* 111:134 */     return new PromiseTask(this, callable);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/* 115:    */   {
/* 116:140 */     throw new UnsupportedOperationException();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/* 120:    */   {
/* 121:145 */     throw new UnsupportedOperationException();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/* 125:    */   {
/* 126:150 */     throw new UnsupportedOperationException();
/* 127:    */   }
/* 128:    */   
/* 129:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/* 130:    */   {
/* 131:155 */     throw new UnsupportedOperationException();
/* 132:    */   }
/* 133:    */   
/* 134:    */   protected static void safeExecute(Runnable task)
/* 135:    */   {
/* 136:    */     try
/* 137:    */     {
/* 138:163 */       task.run();
/* 139:    */     }
/* 140:    */     catch (Throwable t)
/* 141:    */     {
/* 142:165 */       logger.warn("A task raised an exception. Task: {}", task, t);
/* 143:    */     }
/* 144:    */   }
/* 145:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.AbstractEventExecutor
 * JD-Core Version:    0.7.0.1
 */