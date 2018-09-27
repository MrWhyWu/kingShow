/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Pipe.Schema;
/*  4:   */ import io.protostuff.Schema;
/*  5:   */ 
/*  6:   */ public abstract class PolymorphicSchema
/*  7:   */   implements Schema<Object>
/*  8:   */ {
/*  9:   */   public final IdStrategy strategy;
/* 10:   */   
/* 11:   */   public PolymorphicSchema(IdStrategy strategy)
/* 12:   */   {
/* 13:65 */     this.strategy = strategy;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public boolean isInitialized(Object message)
/* 17:   */   {
/* 18:71 */     return true;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public Object newMessage()
/* 22:   */   {
/* 23:78 */     throw new UnsupportedOperationException();
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Class<? super Object> typeClass()
/* 27:   */   {
/* 28:84 */     return Object.class;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public abstract Pipe.Schema<Object> getPipeSchema();
/* 32:   */   
/* 33:   */   protected abstract void setValue(Object paramObject1, Object paramObject2);
/* 34:   */   
/* 35:   */   public static abstract interface Factory
/* 36:   */   {
/* 37:   */     public abstract PolymorphicSchema newSchema(Class<?> paramClass, IdStrategy paramIdStrategy, PolymorphicSchema.Handler paramHandler);
/* 38:   */   }
/* 39:   */   
/* 40:   */   public static abstract interface Handler
/* 41:   */   {
/* 42:   */     public abstract void setValue(Object paramObject1, Object paramObject2);
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicSchema
 * JD-Core Version:    0.7.0.1
 */