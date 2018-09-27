/*  1:   */ package io.protostuff;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.util.Collection;
/*  5:   */ 
/*  6:   */ public final class MessageCollectionSchema<V>
/*  7:   */   extends CollectionSchema<V>
/*  8:   */ {
/*  9:   */   public final Schema<V> schema;
/* 10:   */   public final Pipe.Schema<V> pipeSchema;
/* 11:   */   
/* 12:   */   public MessageCollectionSchema(Schema<V> schema)
/* 13:   */   {
/* 14:41 */     this(schema, null);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public MessageCollectionSchema(Schema<V> schema, Pipe.Schema<V> pipeSchema)
/* 18:   */   {
/* 19:46 */     this.schema = schema;
/* 20:47 */     this.pipeSchema = pipeSchema;
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected void addValueFrom(Input input, Collection<V> collection)
/* 24:   */     throws IOException
/* 25:   */   {
/* 26:53 */     collection.add(input.mergeObject(null, this.schema));
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected void writeValueTo(Output output, int fieldNumber, V value, boolean repeated)
/* 30:   */     throws IOException
/* 31:   */   {
/* 32:59 */     output.writeObject(fieldNumber, value, this.schema, repeated);
/* 33:   */   }
/* 34:   */   
/* 35:   */   protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 36:   */     throws IOException
/* 37:   */   {
/* 38:66 */     if (this.pipeSchema == null) {
/* 39:69 */       throw new RuntimeException("No pipe schema for value: " + this.schema.typeClass().getName());
/* 40:   */     }
/* 41:72 */     output.writeObject(number, pipe, this.pipeSchema, repeated);
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.MessageCollectionSchema
 * JD-Core Version:    0.7.0.1
 */