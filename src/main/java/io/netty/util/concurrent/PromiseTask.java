/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.Callable;
/*   4:    */ import java.util.concurrent.RunnableFuture;
/*   5:    */ 
/*   6:    */ class PromiseTask<V>
/*   7:    */   extends DefaultPromise<V>
/*   8:    */   implements RunnableFuture<V>
/*   9:    */ {
/*  10:    */   protected final Callable<V> task;
/*  11:    */   
/*  12:    */   static <T> Callable<T> toCallable(Runnable runnable, T result)
/*  13:    */   {
/*  14: 24 */     return new RunnableAdapter(runnable, result);
/*  15:    */   }
/*  16:    */   
/*  17:    */   private static final class RunnableAdapter<T>
/*  18:    */     implements Callable<T>
/*  19:    */   {
/*  20:    */     final Runnable task;
/*  21:    */     final T result;
/*  22:    */     
/*  23:    */     RunnableAdapter(Runnable task, T result)
/*  24:    */     {
/*  25: 32 */       this.task = task;
/*  26: 33 */       this.result = result;
/*  27:    */     }
/*  28:    */     
/*  29:    */     public T call()
/*  30:    */     {
/*  31: 38 */       this.task.run();
/*  32: 39 */       return this.result;
/*  33:    */     }
/*  34:    */     
/*  35:    */     public String toString()
/*  36:    */     {
/*  37: 44 */       return "Callable(task: " + this.task + ", result: " + this.result + ')';
/*  38:    */     }
/*  39:    */   }
/*  40:    */   
/*  41:    */   PromiseTask(EventExecutor executor, Runnable runnable, V result)
/*  42:    */   {
/*  43: 51 */     this(executor, toCallable(runnable, result));
/*  44:    */   }
/*  45:    */   
/*  46:    */   PromiseTask(EventExecutor executor, Callable<V> callable)
/*  47:    */   {
/*  48: 55 */     super(executor);
/*  49: 56 */     this.task = callable;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public final int hashCode()
/*  53:    */   {
/*  54: 61 */     return System.identityHashCode(this);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public final boolean equals(Object obj)
/*  58:    */   {
/*  59: 66 */     return this == obj;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void run()
/*  63:    */   {
/*  64:    */     try
/*  65:    */     {
/*  66: 72 */       if (setUncancellableInternal())
/*  67:    */       {
/*  68: 73 */         V result = this.task.call();
/*  69: 74 */         setSuccessInternal(result);
/*  70:    */       }
/*  71:    */     }
/*  72:    */     catch (Throwable e)
/*  73:    */     {
/*  74: 77 */       setFailureInternal(e);
/*  75:    */     }
/*  76:    */   }
/*  77:    */   
/*  78:    */   public final Promise<V> setFailure(Throwable cause)
/*  79:    */   {
/*  80: 83 */     throw new IllegalStateException();
/*  81:    */   }
/*  82:    */   
/*  83:    */   protected final Promise<V> setFailureInternal(Throwable cause)
/*  84:    */   {
/*  85: 87 */     super.setFailure(cause);
/*  86: 88 */     return this;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public final boolean tryFailure(Throwable cause)
/*  90:    */   {
/*  91: 93 */     return false;
/*  92:    */   }
/*  93:    */   
/*  94:    */   protected final boolean tryFailureInternal(Throwable cause)
/*  95:    */   {
/*  96: 97 */     return super.tryFailure(cause);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public final Promise<V> setSuccess(V result)
/* 100:    */   {
/* 101:102 */     throw new IllegalStateException();
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected final Promise<V> setSuccessInternal(V result)
/* 105:    */   {
/* 106:106 */     super.setSuccess(result);
/* 107:107 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public final boolean trySuccess(V result)
/* 111:    */   {
/* 112:112 */     return false;
/* 113:    */   }
/* 114:    */   
/* 115:    */   protected final boolean trySuccessInternal(V result)
/* 116:    */   {
/* 117:116 */     return super.trySuccess(result);
/* 118:    */   }
/* 119:    */   
/* 120:    */   public final boolean setUncancellable()
/* 121:    */   {
/* 122:121 */     throw new IllegalStateException();
/* 123:    */   }
/* 124:    */   
/* 125:    */   protected final boolean setUncancellableInternal()
/* 126:    */   {
/* 127:125 */     return super.setUncancellable();
/* 128:    */   }
/* 129:    */   
/* 130:    */   protected StringBuilder toStringBuilder()
/* 131:    */   {
/* 132:130 */     StringBuilder buf = super.toStringBuilder();
/* 133:131 */     buf.setCharAt(buf.length() - 1, ',');
/* 134:    */     
/* 135:133 */     return buf.append(" task: ")
/* 136:134 */       .append(this.task)
/* 137:135 */       .append(')');
/* 138:    */   }
/* 139:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.PromiseTask
 * JD-Core Version:    0.7.0.1
 */