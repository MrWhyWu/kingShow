/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.CollectionSchema;
/*  4:   */ import io.protostuff.CollectionSchema.MessageFactory;
/*  5:   */ import io.protostuff.Input;
/*  6:   */ import io.protostuff.Output;
/*  7:   */ import io.protostuff.Pipe;
/*  8:   */ import io.protostuff.Tag;
/*  9:   */ import io.protostuff.WireFormat.FieldType;
/* 10:   */ import java.io.IOException;
/* 11:   */ import java.util.Collection;
/* 12:   */ 
/* 13:   */ abstract class RuntimeCollectionField<T, V>
/* 14:   */   extends Field<T>
/* 15:   */ {
/* 16:   */   protected final CollectionSchema<V> schema;
/* 17:   */   
/* 18:   */   public RuntimeCollectionField(WireFormat.FieldType type, int number, String name, Tag tag, CollectionSchema.MessageFactory messageFactory)
/* 19:   */   {
/* 20:46 */     super(type, number, name, false, tag);
/* 21:47 */     this.schema = new CollectionSchema(messageFactory)
/* 22:   */     {
/* 23:   */       protected void addValueFrom(Input input, Collection<V> collection)
/* 24:   */         throws IOException
/* 25:   */       {
/* 26:53 */         RuntimeCollectionField.this.addValueFrom(input, collection);
/* 27:   */       }
/* 28:   */       
/* 29:   */       protected void writeValueTo(Output output, int fieldNumber, V value, boolean repeated)
/* 30:   */         throws IOException
/* 31:   */       {
/* 32:60 */         RuntimeCollectionField.this.writeValueTo(output, fieldNumber, value, repeated);
/* 33:   */       }
/* 34:   */       
/* 35:   */       protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 36:   */         throws IOException
/* 37:   */       {
/* 38:68 */         RuntimeCollectionField.this.transferValue(pipe, input, output, number, repeated);
/* 39:   */       }
/* 40:   */     };
/* 41:   */   }
/* 42:   */   
/* 43:   */   protected abstract void addValueFrom(Input paramInput, Collection<V> paramCollection)
/* 44:   */     throws IOException;
/* 45:   */   
/* 46:   */   protected abstract void writeValueTo(Output paramOutput, int paramInt, V paramV, boolean paramBoolean)
/* 47:   */     throws IOException;
/* 48:   */   
/* 49:   */   protected abstract void transferValue(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/* 50:   */     throws IOException;
/* 51:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeCollectionField
 * JD-Core Version:    0.7.0.1
 */