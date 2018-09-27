/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.GraphInput;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.Output;
/*   6:    */ import io.protostuff.Pipe;
/*   7:    */ import io.protostuff.Pipe.Schema;
/*   8:    */ import io.protostuff.ProtostuffException;
/*   9:    */ import io.protostuff.Schema;
/*  10:    */ import io.protostuff.StatefulOutput;
/*  11:    */ import java.io.IOException;
/*  12:    */ 
/*  13:    */ public abstract class PolymorphicPojoSchema
/*  14:    */   extends PolymorphicSchema
/*  15:    */ {
/*  16: 41 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  17:    */   {
/*  18:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  19:    */       throws IOException
/*  20:    */     {
/*  21: 48 */       PolymorphicPojoSchema.transferObject(this, pipe, input, output, PolymorphicPojoSchema.this.strategy);
/*  22:    */     }
/*  23:    */   };
/*  24:    */   
/*  25:    */   public PolymorphicPojoSchema(IdStrategy strategy)
/*  26:    */   {
/*  27: 54 */     super(strategy);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public Pipe.Schema<Object> getPipeSchema()
/*  31:    */   {
/*  32: 60 */     return this.pipeSchema;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public String getFieldName(int number)
/*  36:    */   {
/*  37: 66 */     return number == 127 ? "_" : null;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public int getFieldNumber(String name)
/*  41:    */   {
/*  42: 72 */     return (name.length() == 1) && (name.charAt(0) == '_') ? 127 : 0;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public boolean isInitialized(Object owner)
/*  46:    */   {
/*  47: 78 */     return true;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public String messageFullName()
/*  51:    */   {
/*  52: 84 */     return Object.class.getName();
/*  53:    */   }
/*  54:    */   
/*  55:    */   public String messageName()
/*  56:    */   {
/*  57: 90 */     return Object.class.getSimpleName();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public Object newMessage()
/*  61:    */   {
/*  62: 97 */     throw new UnsupportedOperationException();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public Class<? super Object> typeClass()
/*  66:    */   {
/*  67:103 */     return Object.class;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void mergeFrom(Input input, Object owner)
/*  71:    */     throws IOException
/*  72:    */   {
/*  73:109 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void writeTo(Output output, Object value)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79:115 */     writeObjectTo(output, value, this, this.strategy);
/*  80:    */   }
/*  81:    */   
/*  82:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  83:    */     throws IOException
/*  84:    */   {
/*  85:123 */     Schema<Object> schema = strategy.writePojoIdTo(output, 127, value.getClass()).getSchema();
/*  86:125 */     if ((output instanceof StatefulOutput)) {
/*  87:128 */       ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  88:    */     }
/*  89:131 */     schema.writeTo(output, value);
/*  90:    */   }
/*  91:    */   
/*  92:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/*  93:    */     throws IOException
/*  94:    */   {
/*  95:137 */     int number = input.readFieldNumber(schema);
/*  96:138 */     if (number != 127) {
/*  97:139 */       throw new ProtostuffException("Corrupt input.");
/*  98:    */     }
/*  99:141 */     return readObjectFrom(input, schema, owner, strategy, number);
/* 100:    */   }
/* 101:    */   
/* 102:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, int number)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:148 */     Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
/* 106:    */     
/* 107:150 */     Object pojo = derivedSchema.newMessage();
/* 108:152 */     if ((input instanceof GraphInput)) {
/* 109:155 */       ((GraphInput)input).updateLast(pojo, owner);
/* 110:    */     }
/* 111:158 */     derivedSchema.mergeFrom(input, pojo);
/* 112:159 */     return pojo;
/* 113:    */   }
/* 114:    */   
/* 115:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 116:    */     throws IOException
/* 117:    */   {
/* 118:165 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 119:166 */     if (number != 127) {
/* 120:167 */       throw new ProtostuffException("Corrupt input.");
/* 121:    */     }
/* 122:169 */     transferObject(pipeSchema, pipe, input, output, strategy, number);
/* 123:    */   }
/* 124:    */   
/* 125:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, int number)
/* 126:    */     throws IOException
/* 127:    */   {
/* 128:177 */     Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
/* 129:179 */     if ((output instanceof StatefulOutput)) {
/* 130:182 */       ((StatefulOutput)output).updateLast(derivedPipeSchema, pipeSchema);
/* 131:    */     }
/* 132:185 */     Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
/* 133:    */   }
/* 134:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicPojoSchema
 * JD-Core Version:    0.7.0.1
 */