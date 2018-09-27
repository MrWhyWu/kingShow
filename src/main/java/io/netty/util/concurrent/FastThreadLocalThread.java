/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.InternalThreadLocalMap;
/*  4:   */ 
/*  5:   */ public class FastThreadLocalThread
/*  6:   */   extends Thread
/*  7:   */ {
/*  8:   */   private final boolean cleanupFastThreadLocals;
/*  9:   */   private InternalThreadLocalMap threadLocalMap;
/* 10:   */   
/* 11:   */   public FastThreadLocalThread()
/* 12:   */   {
/* 13:31 */     this.cleanupFastThreadLocals = false;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public FastThreadLocalThread(Runnable target)
/* 17:   */   {
/* 18:35 */     super(FastThreadLocalRunnable.wrap(target));
/* 19:36 */     this.cleanupFastThreadLocals = true;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public FastThreadLocalThread(ThreadGroup group, Runnable target)
/* 23:   */   {
/* 24:40 */     super(group, FastThreadLocalRunnable.wrap(target));
/* 25:41 */     this.cleanupFastThreadLocals = true;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public FastThreadLocalThread(String name)
/* 29:   */   {
/* 30:45 */     super(name);
/* 31:46 */     this.cleanupFastThreadLocals = false;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public FastThreadLocalThread(ThreadGroup group, String name)
/* 35:   */   {
/* 36:50 */     super(group, name);
/* 37:51 */     this.cleanupFastThreadLocals = false;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public FastThreadLocalThread(Runnable target, String name)
/* 41:   */   {
/* 42:55 */     super(FastThreadLocalRunnable.wrap(target), name);
/* 43:56 */     this.cleanupFastThreadLocals = true;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public FastThreadLocalThread(ThreadGroup group, Runnable target, String name)
/* 47:   */   {
/* 48:60 */     super(group, FastThreadLocalRunnable.wrap(target), name);
/* 49:61 */     this.cleanupFastThreadLocals = true;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public FastThreadLocalThread(ThreadGroup group, Runnable target, String name, long stackSize)
/* 53:   */   {
/* 54:65 */     super(group, FastThreadLocalRunnable.wrap(target), name, stackSize);
/* 55:66 */     this.cleanupFastThreadLocals = true;
/* 56:   */   }
/* 57:   */   
/* 58:   */   public final InternalThreadLocalMap threadLocalMap()
/* 59:   */   {
/* 60:74 */     return this.threadLocalMap;
/* 61:   */   }
/* 62:   */   
/* 63:   */   public final void setThreadLocalMap(InternalThreadLocalMap threadLocalMap)
/* 64:   */   {
/* 65:82 */     this.threadLocalMap = threadLocalMap;
/* 66:   */   }
/* 67:   */   
/* 68:   */   public boolean willCleanupFastThreadLocals()
/* 69:   */   {
/* 70:90 */     return this.cleanupFastThreadLocals;
/* 71:   */   }
/* 72:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.FastThreadLocalThread
 * JD-Core Version:    0.7.0.1
 */