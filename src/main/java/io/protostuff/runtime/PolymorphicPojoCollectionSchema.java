/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.GraphInput;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.Output;
/*   6:    */ import io.protostuff.Pipe;
/*   7:    */ import io.protostuff.Pipe.Schema;
/*   8:    */ import io.protostuff.Schema;
/*   9:    */ import io.protostuff.StatefulOutput;
/*  10:    */ import java.io.IOException;
/*  11:    */ 
/*  12:    */ public abstract class PolymorphicPojoCollectionSchema
/*  13:    */   extends PolymorphicSchema
/*  14:    */ {
/*  15: 38 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  16:    */   {
/*  17:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  18:    */       throws IOException
/*  19:    */     {
/*  20: 45 */       PolymorphicPojoCollectionSchema.transferObject(this, pipe, input, output, PolymorphicPojoCollectionSchema.this.strategy);
/*  21:    */     }
/*  22:    */   };
/*  23:    */   
/*  24:    */   public PolymorphicPojoCollectionSchema(IdStrategy strategy)
/*  25:    */   {
/*  26: 51 */     super(strategy);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Pipe.Schema<Object> getPipeSchema()
/*  30:    */   {
/*  31: 57 */     return this.pipeSchema;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public String getFieldName(int number)
/*  35:    */   {
/*  36: 63 */     return number == 127 ? "_" : null;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public int getFieldNumber(String name)
/*  40:    */   {
/*  41: 69 */     if (name.length() != 1) {
/*  42: 70 */       return 0;
/*  43:    */     }
/*  44: 72 */     char c = name.charAt(0);
/*  45: 73 */     return c == '_' ? 127 : PolymorphicCollectionSchema.number(c);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public boolean isInitialized(Object owner)
/*  49:    */   {
/*  50: 79 */     return true;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public String messageFullName()
/*  54:    */   {
/*  55: 85 */     return Object.class.getName();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String messageName()
/*  59:    */   {
/*  60: 91 */     return Object.class.getSimpleName();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Object newMessage()
/*  64:    */   {
/*  65: 98 */     throw new UnsupportedOperationException();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public Class<? super Object> typeClass()
/*  69:    */   {
/*  70:104 */     return Object.class;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void mergeFrom(Input input, Object owner)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76:110 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public void writeTo(Output output, Object value)
/*  80:    */     throws IOException
/*  81:    */   {
/*  82:116 */     writeObjectTo(output, value, this, this.strategy);
/*  83:    */   }
/*  84:    */   
/*  85:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:123 */     HasSchema<Object> hs = strategy.tryWritePojoIdTo(output, 127, value
/*  89:124 */       .getClass(), true);
/*  90:126 */     if (hs == null)
/*  91:    */     {
/*  92:128 */       PolymorphicCollectionSchema.writeObjectTo(output, value, currentSchema, strategy);
/*  93:    */       
/*  94:130 */       return;
/*  95:    */     }
/*  96:133 */     Schema<Object> schema = hs.getSchema();
/*  97:135 */     if ((output instanceof StatefulOutput)) {
/*  98:138 */       ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  99:    */     }
/* 100:141 */     schema.writeTo(output, value);
/* 101:    */   }
/* 102:    */   
/* 103:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 104:    */     throws IOException
/* 105:    */   {
/* 106:147 */     int number = input.readFieldNumber(schema);
/* 107:148 */     if (number != 127) {
/* 108:150 */       return PolymorphicCollectionSchema.readObjectFrom(input, schema, owner, strategy, number);
/* 109:    */     }
/* 110:154 */     return readObjectFrom(input, schema, owner, strategy, number);
/* 111:    */   }
/* 112:    */   
/* 113:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, int number)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:161 */     Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
/* 117:    */     
/* 118:163 */     Object pojo = derivedSchema.newMessage();
/* 119:165 */     if ((input instanceof GraphInput)) {
/* 120:168 */       ((GraphInput)input).updateLast(pojo, owner);
/* 121:    */     }
/* 122:171 */     derivedSchema.mergeFrom(input, pojo);
/* 123:172 */     return pojo;
/* 124:    */   }
/* 125:    */   
/* 126:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:178 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 130:179 */     if (number != 127) {
/* 131:180 */       PolymorphicCollectionSchema.transferObject(pipeSchema, pipe, input, output, strategy, number);
/* 132:    */     } else {
/* 133:182 */       transferObject(pipeSchema, pipe, input, output, strategy, number);
/* 134:    */     }
/* 135:    */   }
/* 136:    */   
/* 137:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, int number)
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:190 */     Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
/* 141:192 */     if ((output instanceof StatefulOutput)) {
/* 142:195 */       ((StatefulOutput)output).updateLast(derivedPipeSchema, pipeSchema);
/* 143:    */     }
/* 144:198 */     Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
/* 145:    */   }
/* 146:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicPojoCollectionSchema
 * JD-Core Version:    0.7.0.1
 */