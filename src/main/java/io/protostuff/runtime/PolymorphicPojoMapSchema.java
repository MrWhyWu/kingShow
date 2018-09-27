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
/*  12:    */ public abstract class PolymorphicPojoMapSchema
/*  13:    */   extends PolymorphicSchema
/*  14:    */ {
/*  15: 36 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  16:    */   {
/*  17:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  18:    */       throws IOException
/*  19:    */     {
/*  20: 43 */       PolymorphicPojoMapSchema.transferObject(this, pipe, input, output, PolymorphicPojoMapSchema.this.strategy);
/*  21:    */     }
/*  22:    */   };
/*  23:    */   
/*  24:    */   public PolymorphicPojoMapSchema(IdStrategy strategy)
/*  25:    */   {
/*  26: 49 */     super(strategy);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Pipe.Schema<Object> getPipeSchema()
/*  30:    */   {
/*  31: 55 */     return this.pipeSchema;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public String getFieldName(int number)
/*  35:    */   {
/*  36: 61 */     return number == 127 ? "_" : null;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public int getFieldNumber(String name)
/*  40:    */   {
/*  41: 67 */     if (name.length() != 1) {
/*  42: 68 */       return 0;
/*  43:    */     }
/*  44: 70 */     char c = name.charAt(0);
/*  45: 71 */     return c == '_' ? 127 : PolymorphicMapSchema.number(c);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public boolean isInitialized(Object owner)
/*  49:    */   {
/*  50: 77 */     return true;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public String messageFullName()
/*  54:    */   {
/*  55: 83 */     return Object.class.getName();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String messageName()
/*  59:    */   {
/*  60: 89 */     return Object.class.getSimpleName();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Object newMessage()
/*  64:    */   {
/*  65: 96 */     throw new UnsupportedOperationException();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public Class<? super Object> typeClass()
/*  69:    */   {
/*  70:102 */     return Object.class;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void mergeFrom(Input input, Object owner)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76:108 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public void writeTo(Output output, Object value)
/*  80:    */     throws IOException
/*  81:    */   {
/*  82:114 */     writeObjectTo(output, value, this, this.strategy);
/*  83:    */   }
/*  84:    */   
/*  85:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:121 */     HasSchema<Object> hs = strategy.tryWritePojoIdTo(output, 127, value
/*  89:122 */       .getClass(), true);
/*  90:124 */     if (hs == null)
/*  91:    */     {
/*  92:126 */       PolymorphicMapSchema.writeObjectTo(output, value, currentSchema, strategy);
/*  93:127 */       return;
/*  94:    */     }
/*  95:130 */     Schema<Object> schema = hs.getSchema();
/*  96:132 */     if ((output instanceof StatefulOutput)) {
/*  97:135 */       ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  98:    */     }
/*  99:138 */     schema.writeTo(output, value);
/* 100:    */   }
/* 101:    */   
/* 102:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:144 */     int number = input.readFieldNumber(schema);
/* 106:145 */     if (number != 127) {
/* 107:147 */       return PolymorphicMapSchema.readObjectFrom(input, schema, owner, strategy, number);
/* 108:    */     }
/* 109:151 */     return readObjectFrom(input, schema, owner, strategy, number);
/* 110:    */   }
/* 111:    */   
/* 112:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, int number)
/* 113:    */     throws IOException
/* 114:    */   {
/* 115:158 */     Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
/* 116:    */     
/* 117:160 */     Object pojo = derivedSchema.newMessage();
/* 118:162 */     if ((input instanceof GraphInput)) {
/* 119:165 */       ((GraphInput)input).updateLast(pojo, owner);
/* 120:    */     }
/* 121:168 */     derivedSchema.mergeFrom(input, pojo);
/* 122:169 */     return pojo;
/* 123:    */   }
/* 124:    */   
/* 125:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 126:    */     throws IOException
/* 127:    */   {
/* 128:175 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 129:176 */     if (number != 127) {
/* 130:177 */       PolymorphicMapSchema.transferObject(pipeSchema, pipe, input, output, strategy, number);
/* 131:    */     } else {
/* 132:179 */       transferObject(pipeSchema, pipe, input, output, strategy, number);
/* 133:    */     }
/* 134:    */   }
/* 135:    */   
/* 136:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, int number)
/* 137:    */     throws IOException
/* 138:    */   {
/* 139:187 */     Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
/* 140:189 */     if ((output instanceof StatefulOutput)) {
/* 141:192 */       ((StatefulOutput)output).updateLast(derivedPipeSchema, pipeSchema);
/* 142:    */     }
/* 143:195 */     Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
/* 144:    */   }
/* 145:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicPojoMapSchema
 * JD-Core Version:    0.7.0.1
 */