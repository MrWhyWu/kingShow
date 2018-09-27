/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicInteger;
/*  4:   */ 
/*  5:   */ public final class DefaultEventExecutorChooserFactory
/*  6:   */   implements EventExecutorChooserFactory
/*  7:   */ {
/*  8:28 */   public static final DefaultEventExecutorChooserFactory INSTANCE = new DefaultEventExecutorChooserFactory();
/*  9:   */   
/* 10:   */   public EventExecutorChooserFactory.EventExecutorChooser newChooser(EventExecutor[] executors)
/* 11:   */   {
/* 12:35 */     if (isPowerOfTwo(executors.length)) {
/* 13:36 */       return new PowerOfTwoEventExecutorChooser(executors);
/* 14:   */     }
/* 15:38 */     return new GenericEventExecutorChooser(executors);
/* 16:   */   }
/* 17:   */   
/* 18:   */   private static boolean isPowerOfTwo(int val)
/* 19:   */   {
/* 20:43 */     return (val & -val) == val;
/* 21:   */   }
/* 22:   */   
/* 23:   */   private static final class PowerOfTwoEventExecutorChooser
/* 24:   */     implements EventExecutorChooserFactory.EventExecutorChooser
/* 25:   */   {
/* 26:47 */     private final AtomicInteger idx = new AtomicInteger();
/* 27:   */     private final EventExecutor[] executors;
/* 28:   */     
/* 29:   */     PowerOfTwoEventExecutorChooser(EventExecutor[] executors)
/* 30:   */     {
/* 31:51 */       this.executors = executors;
/* 32:   */     }
/* 33:   */     
/* 34:   */     public EventExecutor next()
/* 35:   */     {
/* 36:56 */       return this.executors[(this.idx.getAndIncrement() & this.executors.length - 1)];
/* 37:   */     }
/* 38:   */   }
/* 39:   */   
/* 40:   */   private static final class GenericEventExecutorChooser
/* 41:   */     implements EventExecutorChooserFactory.EventExecutorChooser
/* 42:   */   {
/* 43:61 */     private final AtomicInteger idx = new AtomicInteger();
/* 44:   */     private final EventExecutor[] executors;
/* 45:   */     
/* 46:   */     GenericEventExecutorChooser(EventExecutor[] executors)
/* 47:   */     {
/* 48:65 */       this.executors = executors;
/* 49:   */     }
/* 50:   */     
/* 51:   */     public EventExecutor next()
/* 52:   */     {
/* 53:70 */       return this.executors[java.lang.Math.abs(this.idx.getAndIncrement() % this.executors.length)];
/* 54:   */     }
/* 55:   */   }
/* 56:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultEventExecutorChooserFactory
 * JD-Core Version:    0.7.0.1
 */