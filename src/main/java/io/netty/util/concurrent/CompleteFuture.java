/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.TimeUnit;
/*   4:    */ 
/*   5:    */ public abstract class CompleteFuture<V>
/*   6:    */   extends AbstractFuture<V>
/*   7:    */ {
/*   8:    */   private final EventExecutor executor;
/*   9:    */   
/*  10:    */   protected CompleteFuture(EventExecutor executor)
/*  11:    */   {
/*  12: 34 */     this.executor = executor;
/*  13:    */   }
/*  14:    */   
/*  15:    */   protected EventExecutor executor()
/*  16:    */   {
/*  17: 41 */     return this.executor;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener)
/*  21:    */   {
/*  22: 46 */     if (listener == null) {
/*  23: 47 */       throw new NullPointerException("listener");
/*  24:    */     }
/*  25: 49 */     DefaultPromise.notifyListener(executor(), this, listener);
/*  26: 50 */     return this;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Future<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/*  30:    */   {
/*  31: 55 */     if (listeners == null) {
/*  32: 56 */       throw new NullPointerException("listeners");
/*  33:    */     }
/*  34: 58 */     for (GenericFutureListener<? extends Future<? super V>> l : listeners)
/*  35:    */     {
/*  36: 59 */       if (l == null) {
/*  37:    */         break;
/*  38:    */       }
/*  39: 62 */       DefaultPromise.notifyListener(executor(), this, l);
/*  40:    */     }
/*  41: 64 */     return this;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener)
/*  45:    */   {
/*  46: 70 */     return this;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Future<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/*  50:    */   {
/*  51: 76 */     return this;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public Future<V> await()
/*  55:    */     throws InterruptedException
/*  56:    */   {
/*  57: 81 */     if (Thread.interrupted()) {
/*  58: 82 */       throw new InterruptedException();
/*  59:    */     }
/*  60: 84 */     return this;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public boolean await(long timeout, TimeUnit unit)
/*  64:    */     throws InterruptedException
/*  65:    */   {
/*  66: 89 */     if (Thread.interrupted()) {
/*  67: 90 */       throw new InterruptedException();
/*  68:    */     }
/*  69: 92 */     return true;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public Future<V> sync()
/*  73:    */     throws InterruptedException
/*  74:    */   {
/*  75: 97 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public Future<V> syncUninterruptibly()
/*  79:    */   {
/*  80:102 */     return this;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public boolean await(long timeoutMillis)
/*  84:    */     throws InterruptedException
/*  85:    */   {
/*  86:107 */     if (Thread.interrupted()) {
/*  87:108 */       throw new InterruptedException();
/*  88:    */     }
/*  89:110 */     return true;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Future<V> awaitUninterruptibly()
/*  93:    */   {
/*  94:115 */     return this;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/*  98:    */   {
/*  99:120 */     return true;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/* 103:    */   {
/* 104:125 */     return true;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isDone()
/* 108:    */   {
/* 109:130 */     return true;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public boolean isCancellable()
/* 113:    */   {
/* 114:135 */     return false;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public boolean isCancelled()
/* 118:    */   {
/* 119:140 */     return false;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 123:    */   {
/* 124:145 */     return false;
/* 125:    */   }
/* 126:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.CompleteFuture
 * JD-Core Version:    0.7.0.1
 */