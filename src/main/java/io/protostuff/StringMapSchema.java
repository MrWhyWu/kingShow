/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ 
/*   5:    */ public class StringMapSchema<V>
/*   6:    */   extends MapSchema<String, V>
/*   7:    */ {
/*   8: 33 */   public static final StringMapSchema<String> VALUE_STRING = new StringMapSchema(null)
/*   9:    */   {
/*  10:    */     protected void putValueFrom(Input input, MapSchema.MapWrapper<String, String> wrapper, String key)
/*  11:    */       throws IOException
/*  12:    */     {
/*  13: 39 */       wrapper.put(key, input.readString());
/*  14:    */     }
/*  15:    */     
/*  16:    */     protected void writeValueTo(Output output, int fieldNumber, String value, boolean repeated)
/*  17:    */       throws IOException
/*  18:    */     {
/*  19: 46 */       output.writeString(fieldNumber, value, repeated);
/*  20:    */     }
/*  21:    */     
/*  22:    */     protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  23:    */       throws IOException
/*  24:    */     {
/*  25: 53 */       input.transferByteRangeTo(output, true, number, repeated);
/*  26:    */     }
/*  27:    */   };
/*  28:    */   public final Schema<V> vSchema;
/*  29:    */   public final Pipe.Schema<V> vPipeSchema;
/*  30:    */   
/*  31:    */   public StringMapSchema(Schema<V> vSchema)
/*  32:    */   {
/*  33: 68 */     this(vSchema, null);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public StringMapSchema(Schema<V> vSchema, Pipe.Schema<V> vPipeSchema)
/*  37:    */   {
/*  38: 73 */     this.vSchema = vSchema;
/*  39: 74 */     this.vPipeSchema = vPipeSchema;
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected final String readKeyFrom(Input input, MapSchema.MapWrapper<String, V> wrapper)
/*  43:    */     throws IOException
/*  44:    */   {
/*  45: 81 */     return input.readString();
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected void putValueFrom(Input input, MapSchema.MapWrapper<String, V> wrapper, String key)
/*  49:    */     throws IOException
/*  50:    */   {
/*  51: 88 */     wrapper.put(key, input.mergeObject(null, this.vSchema));
/*  52:    */   }
/*  53:    */   
/*  54:    */   protected final void writeKeyTo(Output output, int fieldNumber, String value, boolean repeated)
/*  55:    */     throws IOException
/*  56:    */   {
/*  57: 95 */     output.writeString(fieldNumber, value, repeated);
/*  58:    */   }
/*  59:    */   
/*  60:    */   protected void writeValueTo(Output output, int fieldNumber, V value, boolean repeated)
/*  61:    */     throws IOException
/*  62:    */   {
/*  63:102 */     output.writeObject(fieldNumber, value, this.vSchema, repeated);
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected void transferKey(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  67:    */     throws IOException
/*  68:    */   {
/*  69:109 */     input.transferByteRangeTo(output, true, number, repeated);
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75:116 */     if (this.vPipeSchema == null) {
/*  76:119 */       throw new RuntimeException("No pipe schema for value: " + this.vSchema.typeClass().getName());
/*  77:    */     }
/*  78:122 */     output.writeObject(number, pipe, this.vPipeSchema, repeated);
/*  79:    */   }
/*  80:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.StringMapSchema
 * JD-Core Version:    0.7.0.1
 */