/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Input;
/*  4:   */ import io.protostuff.Output;
/*  5:   */ import io.protostuff.Pipe;
/*  6:   */ import io.protostuff.Pipe.Schema;
/*  7:   */ import io.protostuff.Schema;
/*  8:   */ import java.io.IOException;
/*  9:   */ 
/* 10:   */ public final class RuntimePipeSchema<T>
/* 11:   */   extends Pipe.Schema<T>
/* 12:   */ {
/* 13:   */   final FieldMap<T> fieldsMap;
/* 14:   */   
/* 15:   */   public RuntimePipeSchema(Schema<T> schema, FieldMap<T> fieldMap)
/* 16:   */   {
/* 17:37 */     super(schema);
/* 18:   */     
/* 19:39 */     this.fieldsMap = fieldMap;
/* 20:   */   }
/* 21:   */   
/* 22:   */   protected void transfer(Pipe pipe, Input input, Output output)
/* 23:   */     throws IOException
/* 24:   */   {
/* 25:46 */     for (int number = input.readFieldNumber(this.wrappedSchema); number != 0; number = input.readFieldNumber(this.wrappedSchema))
/* 26:   */     {
/* 27:49 */       Field<T> field = this.fieldsMap.getFieldByNumber(number);
/* 28:50 */       if (field == null) {
/* 29:51 */         input.handleUnknownField(number, this.wrappedSchema);
/* 30:   */       } else {
/* 31:53 */         field.transfer(pipe, input, output, field.repeated);
/* 32:   */       }
/* 33:   */     }
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimePipeSchema
 * JD-Core Version:    0.7.0.1
 */