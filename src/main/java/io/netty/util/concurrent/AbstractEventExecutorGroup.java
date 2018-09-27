/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.Collection;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.List;
/*   6:    */ import java.util.concurrent.Callable;
/*   7:    */ import java.util.concurrent.ExecutionException;
/*   8:    */ import java.util.concurrent.TimeUnit;
/*   9:    */ import java.util.concurrent.TimeoutException;
/*  10:    */ 
/*  11:    */ public abstract class AbstractEventExecutorGroup
/*  12:    */   implements EventExecutorGroup
/*  13:    */ {
/*  14:    */   public Future<?> submit(Runnable task)
/*  15:    */   {
/*  16: 35 */     return next().submit(task);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public <T> Future<T> submit(Runnable task, T result)
/*  20:    */   {
/*  21: 40 */     return next().submit(task, result);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public <T> Future<T> submit(Callable<T> task)
/*  25:    */   {
/*  26: 45 */     return next().submit(task);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
/*  30:    */   {
/*  31: 50 */     return next().schedule(command, delay, unit);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
/*  35:    */   {
/*  36: 55 */     return next().schedule(callable, delay, unit);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
/*  40:    */   {
/*  41: 60 */     return next().scheduleAtFixedRate(command, initialDelay, period, unit);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
/*  45:    */   {
/*  46: 65 */     return next().scheduleWithFixedDelay(command, initialDelay, delay, unit);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Future<?> shutdownGracefully()
/*  50:    */   {
/*  51: 70 */     return shutdownGracefully(2L, 15L, TimeUnit.SECONDS);
/*  52:    */   }
/*  53:    */   
/*  54:    */   @Deprecated
/*  55:    */   public abstract void shutdown();
/*  56:    */   
/*  57:    */   @Deprecated
/*  58:    */   public List<Runnable> shutdownNow()
/*  59:    */   {
/*  60: 86 */     shutdown();
/*  61: 87 */     return Collections.emptyList();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
/*  65:    */     throws InterruptedException
/*  66:    */   {
/*  67: 93 */     return next().invokeAll(tasks);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
/*  71:    */     throws InterruptedException
/*  72:    */   {
/*  73: 99 */     return next().invokeAll(tasks, timeout, unit);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
/*  77:    */     throws InterruptedException, ExecutionException
/*  78:    */   {
/*  79:104 */     return next().invokeAny(tasks);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
/*  83:    */     throws InterruptedException, ExecutionException, TimeoutException
/*  84:    */   {
/*  85:110 */     return next().invokeAny(tasks, timeout, unit);
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void execute(Runnable command)
/*  89:    */   {
/*  90:115 */     next().execute(command);
/*  91:    */   }
/*  92:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.AbstractEventExecutorGroup
 * JD-Core Version:    0.7.0.1
 */