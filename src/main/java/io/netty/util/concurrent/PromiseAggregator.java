/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import java.util.LinkedHashSet;
/*   4:    */ import java.util.Set;
/*   5:    */ 
/*   6:    */ @Deprecated
/*   7:    */ public class PromiseAggregator<V, F extends Future<V>>
/*   8:    */   implements GenericFutureListener<F>
/*   9:    */ {
/*  10:    */   private final Promise<?> aggregatePromise;
/*  11:    */   private final boolean failPending;
/*  12:    */   private Set<Promise<V>> pendingPromises;
/*  13:    */   
/*  14:    */   public PromiseAggregator(Promise<Void> aggregatePromise, boolean failPending)
/*  15:    */   {
/*  16: 46 */     if (aggregatePromise == null) {
/*  17: 47 */       throw new NullPointerException("aggregatePromise");
/*  18:    */     }
/*  19: 49 */     this.aggregatePromise = aggregatePromise;
/*  20: 50 */     this.failPending = failPending;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public PromiseAggregator(Promise<Void> aggregatePromise)
/*  24:    */   {
/*  25: 58 */     this(aggregatePromise, true);
/*  26:    */   }
/*  27:    */   
/*  28:    */   @SafeVarargs
/*  29:    */   public final PromiseAggregator<V, F> add(Promise<V>... promises)
/*  30:    */   {
/*  31: 66 */     if (promises == null) {
/*  32: 67 */       throw new NullPointerException("promises");
/*  33:    */     }
/*  34: 69 */     if (promises.length == 0) {
/*  35: 70 */       return this;
/*  36:    */     }
/*  37: 72 */     synchronized (this)
/*  38:    */     {
/*  39:    */       int size;
/*  40: 73 */       if (this.pendingPromises == null)
/*  41:    */       {
/*  42:    */         int size;
/*  43: 75 */         if (promises.length > 1) {
/*  44: 76 */           size = promises.length;
/*  45:    */         } else {
/*  46: 78 */           size = 2;
/*  47:    */         }
/*  48: 80 */         this.pendingPromises = new LinkedHashSet(size);
/*  49:    */       }
/*  50: 82 */       for (Promise<V> p : promises) {
/*  51: 83 */         if (p != null)
/*  52:    */         {
/*  53: 86 */           this.pendingPromises.add(p);
/*  54: 87 */           p.addListener(this);
/*  55:    */         }
/*  56:    */       }
/*  57:    */     }
/*  58: 90 */     return this;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public synchronized void operationComplete(F future)
/*  62:    */     throws Exception
/*  63:    */   {
/*  64: 95 */     if (this.pendingPromises == null)
/*  65:    */     {
/*  66: 96 */       this.aggregatePromise.setSuccess(null);
/*  67:    */     }
/*  68:    */     else
/*  69:    */     {
/*  70: 98 */       this.pendingPromises.remove(future);
/*  71:    */       Throwable cause;
/*  72: 99 */       if (!future.isSuccess())
/*  73:    */       {
/*  74:100 */         cause = future.cause();
/*  75:101 */         this.aggregatePromise.setFailure(cause);
/*  76:102 */         if (this.failPending) {
/*  77:103 */           for (Promise<V> pendingFuture : this.pendingPromises) {
/*  78:104 */             pendingFuture.setFailure(cause);
/*  79:    */           }
/*  80:    */         }
/*  81:    */       }
/*  82:108 */       else if (this.pendingPromises.isEmpty())
/*  83:    */       {
/*  84:109 */         this.aggregatePromise.setSuccess(null);
/*  85:    */       }
/*  86:    */     }
/*  87:    */   }
/*  88:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.PromiseAggregator
 * JD-Core Version:    0.7.0.1
 */