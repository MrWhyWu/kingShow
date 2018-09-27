/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ public class HasDelegate<T>
/*  4:   */   implements PolymorphicSchema.Factory
/*  5:   */ {
/*  6:   */   public final Delegate<T> delegate;
/*  7:   */   public final IdStrategy strategy;
/*  8:   */   public final ArraySchemas.Base genericElementSchema;
/*  9:   */   
/* 10:   */   public HasDelegate(Delegate<T> delegate, IdStrategy strategy)
/* 11:   */   {
/* 12:36 */     this.delegate = delegate;
/* 13:37 */     this.strategy = strategy;
/* 14:   */     
/* 15:39 */     this.genericElementSchema = new ArraySchemas.DelegateArray(strategy, null, delegate);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public final Delegate<T> getDelegate()
/* 19:   */   {
/* 20:48 */     return this.delegate;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public final PolymorphicSchema newSchema(Class<?> typeClass, IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 24:   */   {
/* 25:56 */     return new ArraySchemas.DelegateArray(strategy, handler, this.delegate);
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.HasDelegate
 * JD-Core Version:    0.7.0.1
 */