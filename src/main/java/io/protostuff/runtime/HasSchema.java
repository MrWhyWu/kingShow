/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Pipe.Schema;
/*  4:   */ import io.protostuff.Schema;
/*  5:   */ 
/*  6:   */ public abstract class HasSchema<T>
/*  7:   */   implements PolymorphicSchema.Factory
/*  8:   */ {
/*  9:   */   public final IdStrategy strategy;
/* 10:   */   public final ArraySchemas.Base genericElementSchema;
/* 11:   */   
/* 12:   */   public abstract Schema<T> getSchema();
/* 13:   */   
/* 14:   */   public abstract Pipe.Schema<T> getPipeSchema();
/* 15:   */   
/* 16:   */   protected HasSchema(IdStrategy strategy)
/* 17:   */   {
/* 18:60 */     this.strategy = strategy;
/* 19:   */     
/* 20:62 */     this.genericElementSchema = new ArraySchemas.PojoArray(strategy, null, this);
/* 21:   */   }
/* 22:   */   
/* 23:   */   public PolymorphicSchema newSchema(Class<?> typeClass, IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 24:   */   {
/* 25:71 */     return new ArraySchemas.PojoArray(strategy, handler, this);
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.HasSchema
 * JD-Core Version:    0.7.0.1
 */