/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Tag;
/*  4:   */ import io.protostuff.WireFormat.FieldType;
/*  5:   */ 
/*  6:   */ abstract class RuntimeObjectField<T>
/*  7:   */   extends Field<T>
/*  8:   */   implements PolymorphicSchema.Handler
/*  9:   */ {
/* 10:   */   public final PolymorphicSchema schema;
/* 11:   */   
/* 12:   */   public RuntimeObjectField(Class<?> typeClass, WireFormat.FieldType type, int number, String name, boolean repeated, Tag tag, PolymorphicSchema.Factory factory, IdStrategy strategy)
/* 13:   */   {
/* 14:39 */     super(type, number, name, repeated, tag);
/* 15:   */     
/* 16:41 */     this.schema = factory.newSchema(typeClass, strategy, this);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeObjectField
 * JD-Core Version:    0.7.0.1
 */