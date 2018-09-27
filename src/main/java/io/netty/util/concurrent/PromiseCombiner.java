/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ 
/*   5:    */ public final class PromiseCombiner
/*   6:    */ {
/*   7:    */   private int expectedCount;
/*   8:    */   private int doneCount;
/*   9:    */   private boolean doneAdding;
/*  10:    */   private Promise<Void> aggregatePromise;
/*  11:    */   private Throwable cause;
/*  12: 38 */   private final GenericFutureListener<Future<?>> listener = new GenericFutureListener()
/*  13:    */   {
/*  14:    */     public void operationComplete(Future<?> future)
/*  15:    */       throws Exception
/*  16:    */     {
/*  17: 41 */       PromiseCombiner.access$004(PromiseCombiner.this);
/*  18: 42 */       if ((!future.isSuccess()) && (PromiseCombiner.this.cause == null)) {
/*  19: 43 */         PromiseCombiner.this.cause = future.cause();
/*  20:    */       }
/*  21: 45 */       if ((PromiseCombiner.this.doneCount == PromiseCombiner.this.expectedCount) && (PromiseCombiner.this.doneAdding)) {
/*  22: 46 */         PromiseCombiner.this.tryPromise();
/*  23:    */       }
/*  24:    */     }
/*  25:    */   };
/*  26:    */   
/*  27:    */   @Deprecated
/*  28:    */   public void add(Promise promise)
/*  29:    */   {
/*  30: 61 */     add(promise);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void add(Future future)
/*  34:    */   {
/*  35: 72 */     checkAddAllowed();
/*  36: 73 */     this.expectedCount += 1;
/*  37: 74 */     future.addListener(this.listener);
/*  38:    */   }
/*  39:    */   
/*  40:    */   @Deprecated
/*  41:    */   public void addAll(Promise... promises)
/*  42:    */   {
/*  43: 87 */     addAll((Future[])promises);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void addAll(Future... futures)
/*  47:    */   {
/*  48: 98 */     for (Future future : futures) {
/*  49: 99 */       add(future);
/*  50:    */     }
/*  51:    */   }
/*  52:    */   
/*  53:    */   public void finish(Promise<Void> aggregatePromise)
/*  54:    */   {
/*  55:115 */     if (this.doneAdding) {
/*  56:116 */       throw new IllegalStateException("Already finished");
/*  57:    */     }
/*  58:118 */     this.doneAdding = true;
/*  59:119 */     this.aggregatePromise = ((Promise)ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise"));
/*  60:120 */     if (this.doneCount == this.expectedCount) {
/*  61:121 */       tryPromise();
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   private boolean tryPromise()
/*  66:    */   {
/*  67:126 */     return this.cause == null ? this.aggregatePromise.trySuccess(null) : this.aggregatePromise.tryFailure(this.cause);
/*  68:    */   }
/*  69:    */   
/*  70:    */   private void checkAddAllowed()
/*  71:    */   {
/*  72:130 */     if (this.doneAdding) {
/*  73:131 */       throw new IllegalStateException("Adding promises is not allowed after finished adding");
/*  74:    */     }
/*  75:    */   }
/*  76:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.PromiseCombiner
 * JD-Core Version:    0.7.0.1
 */