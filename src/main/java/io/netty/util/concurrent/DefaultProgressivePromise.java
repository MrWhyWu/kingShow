/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ public class DefaultProgressivePromise<V>
/*   4:    */   extends DefaultPromise<V>
/*   5:    */   implements ProgressivePromise<V>
/*   6:    */ {
/*   7:    */   public DefaultProgressivePromise(EventExecutor executor)
/*   8:    */   {
/*   9: 30 */     super(executor);
/*  10:    */   }
/*  11:    */   
/*  12:    */   protected DefaultProgressivePromise() {}
/*  13:    */   
/*  14:    */   public ProgressivePromise<V> setProgress(long progress, long total)
/*  15:    */   {
/*  16: 37 */     if (total < 0L)
/*  17:    */     {
/*  18: 39 */       total = -1L;
/*  19: 40 */       if (progress < 0L) {
/*  20: 41 */         throw new IllegalArgumentException("progress: " + progress + " (expected: >= 0)");
/*  21:    */       }
/*  22:    */     }
/*  23: 43 */     else if ((progress < 0L) || (progress > total))
/*  24:    */     {
/*  25: 44 */       throw new IllegalArgumentException("progress: " + progress + " (expected: 0 <= progress <= total (" + total + "))");
/*  26:    */     }
/*  27: 48 */     if (isDone()) {
/*  28: 49 */       throw new IllegalStateException("complete already");
/*  29:    */     }
/*  30: 52 */     notifyProgressiveListeners(progress, total);
/*  31: 53 */     return this;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public boolean tryProgress(long progress, long total)
/*  35:    */   {
/*  36: 58 */     if (total < 0L)
/*  37:    */     {
/*  38: 59 */       total = -1L;
/*  39: 60 */       if ((progress < 0L) || (isDone())) {
/*  40: 61 */         return false;
/*  41:    */       }
/*  42:    */     }
/*  43: 63 */     else if ((progress < 0L) || (progress > total) || (isDone()))
/*  44:    */     {
/*  45: 64 */       return false;
/*  46:    */     }
/*  47: 67 */     notifyProgressiveListeners(progress, total);
/*  48: 68 */     return true;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public ProgressivePromise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener)
/*  52:    */   {
/*  53: 73 */     super.addListener(listener);
/*  54: 74 */     return this;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ProgressivePromise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/*  58:    */   {
/*  59: 79 */     super.addListeners(listeners);
/*  60: 80 */     return this;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public ProgressivePromise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener)
/*  64:    */   {
/*  65: 85 */     super.removeListener(listener);
/*  66: 86 */     return this;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ProgressivePromise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners)
/*  70:    */   {
/*  71: 91 */     super.removeListeners(listeners);
/*  72: 92 */     return this;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public ProgressivePromise<V> sync()
/*  76:    */     throws InterruptedException
/*  77:    */   {
/*  78: 97 */     super.sync();
/*  79: 98 */     return this;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public ProgressivePromise<V> syncUninterruptibly()
/*  83:    */   {
/*  84:103 */     super.syncUninterruptibly();
/*  85:104 */     return this;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ProgressivePromise<V> await()
/*  89:    */     throws InterruptedException
/*  90:    */   {
/*  91:109 */     super.await();
/*  92:110 */     return this;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public ProgressivePromise<V> awaitUninterruptibly()
/*  96:    */   {
/*  97:115 */     super.awaitUninterruptibly();
/*  98:116 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public ProgressivePromise<V> setSuccess(V result)
/* 102:    */   {
/* 103:121 */     super.setSuccess(result);
/* 104:122 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ProgressivePromise<V> setFailure(Throwable cause)
/* 108:    */   {
/* 109:127 */     super.setFailure(cause);
/* 110:128 */     return this;
/* 111:    */   }
/* 112:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.DefaultProgressivePromise
 * JD-Core Version:    0.7.0.1
 */