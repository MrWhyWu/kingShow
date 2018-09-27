/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public final class MessageMapSchema<K, V>
/*   6:    */   extends MapSchema<K, V>
/*   7:    */ {
/*   8:    */   public final Schema<K> kSchema;
/*   9:    */   public final Schema<V> vSchema;
/*  10:    */   public final Pipe.Schema<K> kPipeSchema;
/*  11:    */   public final Pipe.Schema<V> vPipeSchema;
/*  12:    */   
/*  13:    */   public MessageMapSchema(Schema<K> kSchema, Schema<V> vSchema)
/*  14:    */   {
/*  15: 49 */     this(kSchema, vSchema, null, null);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public MessageMapSchema(Schema<K> kSchema, Schema<V> vSchema, Pipe.Schema<K> kPipeSchema, Pipe.Schema<V> vPipeSchema)
/*  19:    */   {
/*  20: 55 */     this.kSchema = kSchema;
/*  21: 56 */     this.vSchema = vSchema;
/*  22: 57 */     this.kPipeSchema = kPipeSchema;
/*  23: 58 */     this.vPipeSchema = vPipeSchema;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected K readKeyFrom(Input input, MapSchema.MapWrapper<K, V> wrapper)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 64 */     return input.mergeObject(null, this.kSchema);
/*  30:    */   }
/*  31:    */   
/*  32:    */   protected void putValueFrom(Input input, MapSchema.MapWrapper<K, V> wrapper, K key)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 71 */     wrapper.put(key, input.mergeObject(null, this.vSchema));
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected void writeKeyTo(Output output, int fieldNumber, K value, boolean repeated)
/*  39:    */     throws IOException
/*  40:    */   {
/*  41: 78 */     output.writeObject(fieldNumber, value, this.kSchema, repeated);
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected void writeValueTo(Output output, int fieldNumber, V value, boolean repeated)
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 85 */     output.writeObject(fieldNumber, value, this.vSchema, repeated);
/*  48:    */   }
/*  49:    */   
/*  50:    */   protected void transferKey(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 92 */     if (this.kPipeSchema == null) {
/*  54: 95 */       throw new RuntimeException("No pipe schema for key: " + this.kSchema.typeClass().getName());
/*  55:    */     }
/*  56: 98 */     output.writeObject(number, pipe, this.kPipeSchema, repeated);
/*  57:    */   }
/*  58:    */   
/*  59:    */   protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  60:    */     throws IOException
/*  61:    */   {
/*  62:105 */     if (this.vPipeSchema == null) {
/*  63:108 */       throw new RuntimeException("No pipe schema for value: " + this.vSchema.typeClass().getName());
/*  64:    */     }
/*  65:111 */     output.writeObject(number, pipe, this.vPipeSchema, repeated);
/*  66:    */   }
/*  67:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.MessageMapSchema
 * JD-Core Version:    0.7.0.1
 */