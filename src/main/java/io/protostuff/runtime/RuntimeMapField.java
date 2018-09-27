/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import io.protostuff.Input;
/*  4:   */ import io.protostuff.MapSchema;
/*  5:   */ import io.protostuff.MapSchema.MapWrapper;
/*  6:   */ import io.protostuff.MapSchema.MessageFactory;
/*  7:   */ import io.protostuff.Output;
/*  8:   */ import io.protostuff.Pipe;
/*  9:   */ import io.protostuff.Tag;
/* 10:   */ import io.protostuff.WireFormat.FieldType;
/* 11:   */ import java.io.IOException;
/* 12:   */ 
/* 13:   */ abstract class RuntimeMapField<T, K, V>
/* 14:   */   extends Field<T>
/* 15:   */ {
/* 16:   */   protected final MapSchema<K, V> schema;
/* 17:   */   
/* 18:   */   public RuntimeMapField(WireFormat.FieldType type, int number, String name, Tag tag, MapSchema.MessageFactory messageFactory)
/* 19:   */   {
/* 20:47 */     super(type, number, name, false, tag);
/* 21:48 */     this.schema = new MapSchema(messageFactory)
/* 22:   */     {
/* 23:   */       protected K readKeyFrom(Input input, MapSchema.MapWrapper<K, V> wrapper)
/* 24:   */         throws IOException
/* 25:   */       {
/* 26:54 */         return RuntimeMapField.this.kFrom(input, wrapper);
/* 27:   */       }
/* 28:   */       
/* 29:   */       protected void putValueFrom(Input input, MapSchema.MapWrapper<K, V> wrapper, K key)
/* 30:   */         throws IOException
/* 31:   */       {
/* 32:61 */         RuntimeMapField.this.vPutFrom(input, wrapper, key);
/* 33:   */       }
/* 34:   */       
/* 35:   */       protected void writeKeyTo(Output output, int fieldNumber, K key, boolean repeated)
/* 36:   */         throws IOException
/* 37:   */       {
/* 38:68 */         RuntimeMapField.this.kTo(output, fieldNumber, key, repeated);
/* 39:   */       }
/* 40:   */       
/* 41:   */       protected void writeValueTo(Output output, int fieldNumber, V val, boolean repeated)
/* 42:   */         throws IOException
/* 43:   */       {
/* 44:75 */         RuntimeMapField.this.vTo(output, fieldNumber, val, repeated);
/* 45:   */       }
/* 46:   */       
/* 47:   */       protected void transferKey(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 48:   */         throws IOException
/* 49:   */       {
/* 50:82 */         RuntimeMapField.this.kTransfer(pipe, input, output, number, repeated);
/* 51:   */       }
/* 52:   */       
/* 53:   */       protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 54:   */         throws IOException
/* 55:   */       {
/* 56:89 */         RuntimeMapField.this.vTransfer(pipe, input, output, number, repeated);
/* 57:   */       }
/* 58:   */     };
/* 59:   */   }
/* 60:   */   
/* 61:   */   protected abstract K kFrom(Input paramInput, MapSchema.MapWrapper<K, V> paramMapWrapper)
/* 62:   */     throws IOException;
/* 63:   */   
/* 64:   */   protected abstract void vPutFrom(Input paramInput, MapSchema.MapWrapper<K, V> paramMapWrapper, K paramK)
/* 65:   */     throws IOException;
/* 66:   */   
/* 67:   */   protected abstract void kTo(Output paramOutput, int paramInt, K paramK, boolean paramBoolean)
/* 68:   */     throws IOException;
/* 69:   */   
/* 70:   */   protected abstract void vTo(Output paramOutput, int paramInt, V paramV, boolean paramBoolean)
/* 71:   */     throws IOException;
/* 72:   */   
/* 73:   */   protected abstract void kTransfer(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/* 74:   */     throws IOException;
/* 75:   */   
/* 76:   */   protected abstract void vTransfer(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/* 77:   */     throws IOException;
/* 78:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeMapField
 * JD-Core Version:    0.7.0.1
 */