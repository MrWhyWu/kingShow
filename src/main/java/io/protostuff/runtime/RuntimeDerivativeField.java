/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Input;
/*  4:   */ import io.protostuff.Schema;
/*  5:   */ import io.protostuff.Tag;
/*  6:   */ import io.protostuff.WireFormat.FieldType;
/*  7:   */ import java.io.IOException;
/*  8:   */ 
/*  9:   */ abstract class RuntimeDerivativeField<T>
/* 10:   */   extends Field<T>
/* 11:   */ {
/* 12:   */   public final DerivativeSchema schema;
/* 13:   */   public final Class<Object> typeClass;
/* 14:   */   
/* 15:   */   public RuntimeDerivativeField(Class<Object> typeClass, WireFormat.FieldType type, int number, String name, boolean repeated, Tag tag, IdStrategy strategy)
/* 16:   */   {
/* 17:53 */     super(type, number, name, repeated, tag);
/* 18:54 */     this.typeClass = typeClass;
/* 19:   */     
/* 20:56 */     this.schema = new DerivativeSchema(strategy)
/* 21:   */     {
/* 22:   */       protected void doMergeFrom(Input input, Schema<Object> derivedSchema, Object owner)
/* 23:   */         throws IOException
/* 24:   */       {
/* 25:63 */         RuntimeDerivativeField.this.doMergeFrom(input, derivedSchema, owner);
/* 26:   */       }
/* 27:   */     };
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected abstract void doMergeFrom(Input paramInput, Schema<Object> paramSchema, Object paramObject)
/* 31:   */     throws IOException;
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeDerivativeField
 * JD-Core Version:    0.7.0.1
 */