/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Pipe.Schema;
/*  4:   */ import io.protostuff.Schema;
/*  5:   */ import io.protostuff.Tag;
/*  6:   */ import io.protostuff.WireFormat.FieldType;
/*  7:   */ 
/*  8:   */ abstract class RuntimeMessageField<T, P>
/*  9:   */   extends Field<T>
/* 10:   */ {
/* 11:   */   public final Class<P> typeClass;
/* 12:   */   final HasSchema<P> hasSchema;
/* 13:   */   
/* 14:   */   public RuntimeMessageField(Class<P> typeClass, HasSchema<P> hasSchema, WireFormat.FieldType type, int number, String name, boolean repeated, Tag tag)
/* 15:   */   {
/* 16:41 */     super(type, number, name, repeated, tag);
/* 17:42 */     this.typeClass = typeClass;
/* 18:43 */     this.hasSchema = hasSchema;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public Schema<P> getSchema()
/* 22:   */   {
/* 23:51 */     return this.hasSchema.getSchema();
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Pipe.Schema<P> getPipeSchema()
/* 27:   */   {
/* 28:59 */     return this.hasSchema.getPipeSchema();
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeMessageField
 * JD-Core Version:    0.7.0.1
 */