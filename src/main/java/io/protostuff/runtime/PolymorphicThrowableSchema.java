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
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public abstract class PolymorphicThrowableSchema
/*  15:    */   extends PolymorphicSchema
/*  16:    */ {
/*  17:    */   static final java.lang.reflect.Field __cause;
/*  18:    */   
/*  19:    */   static
/*  20:    */   {
/*  21:    */     java.lang.reflect.Field cause;
/*  22:    */     try
/*  23:    */     {
/*  24: 60 */       java.lang.reflect.Field cause = Throwable.class.getDeclaredField("cause");
/*  25: 61 */       cause.setAccessible(true);
/*  26:    */     }
/*  27:    */     catch (Exception e)
/*  28:    */     {
/*  29: 65 */       cause = null;
/*  30:    */     }
/*  31: 67 */     __cause = cause;
/*  32:    */   }
/*  33:    */   
/*  34:    */   static String name(int number)
/*  35:    */   {
/*  36: 72 */     return number == 52 ? "Z" : null;
/*  37:    */   }
/*  38:    */   
/*  39:    */   static int number(String name)
/*  40:    */   {
/*  41: 77 */     return (name.length() == 1) && (name.charAt(0) == 'Z') ? 52 : 0;
/*  42:    */   }
/*  43:    */   
/*  44: 80 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  45:    */   {
/*  46:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  47:    */       throws IOException
/*  48:    */     {
/*  49: 87 */       PolymorphicThrowableSchema.transferObject(this, pipe, input, output, PolymorphicThrowableSchema.this.strategy);
/*  50:    */     }
/*  51:    */   };
/*  52:    */   
/*  53:    */   public PolymorphicThrowableSchema(IdStrategy strategy)
/*  54:    */   {
/*  55: 93 */     super(strategy);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public Pipe.Schema<Object> getPipeSchema()
/*  59:    */   {
/*  60: 99 */     return this.pipeSchema;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public String getFieldName(int number)
/*  64:    */   {
/*  65:105 */     return name(number);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int getFieldNumber(String name)
/*  69:    */   {
/*  70:111 */     return number(name);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public String messageFullName()
/*  74:    */   {
/*  75:117 */     return Throwable.class.getName();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public String messageName()
/*  79:    */   {
/*  80:123 */     return Throwable.class.getSimpleName();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public void mergeFrom(Input input, Object owner)
/*  84:    */     throws IOException
/*  85:    */   {
/*  86:129 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void writeTo(Output output, Object value)
/*  90:    */     throws IOException
/*  91:    */   {
/*  92:135 */     writeObjectTo(output, value, this, this.strategy);
/*  93:    */   }
/*  94:    */   
/*  95:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  96:    */     throws IOException
/*  97:    */   {
/*  98:143 */     Schema<Object> schema = strategy.writePojoIdTo(output, 52, value.getClass()).getSchema();
/*  99:145 */     if ((output instanceof StatefulOutput)) {
/* 100:148 */       ((StatefulOutput)output).updateLast(schema, currentSchema);
/* 101:    */     }
/* 102:151 */     if (tryWriteWithoutCause(output, value, schema)) {
/* 103:152 */       return;
/* 104:    */     }
/* 105:154 */     schema.writeTo(output, value);
/* 106:    */   }
/* 107:    */   
/* 108:    */   static boolean tryWriteWithoutCause(Output output, Object value, Schema<Object> schema)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:160 */     if (((schema instanceof RuntimeSchema)) && (__cause != null))
/* 112:    */     {
/* 113:163 */       RuntimeSchema<Object> ms = (RuntimeSchema)schema;
/* 114:164 */       if ((ms.getFieldCount() > 1) && (((Field)ms.getFields().get(1)).name.equals("cause")))
/* 115:    */       {
/* 116:    */         try
/* 117:    */         {
/* 118:169 */           cause = __cause.get(value);
/* 119:    */         }
/* 120:    */         catch (Exception e)
/* 121:    */         {
/* 122:    */           Object cause;
/* 123:173 */           throw new RuntimeException(e);
/* 124:    */         }
/* 125:    */         Object cause;
/* 126:176 */         if (cause == value)
/* 127:    */         {
/* 128:179 */           ((Field)ms.getFields().get(0)).writeTo(output, value);
/* 129:    */           
/* 130:181 */           int i = 2;
/* 131:181 */           for (int len = ms.getFieldCount(); i < len; i++) {
/* 132:182 */             ((Field)ms.getFields().get(i)).writeTo(output, value);
/* 133:    */           }
/* 134:184 */           return true;
/* 135:    */         }
/* 136:    */       }
/* 137:    */     }
/* 138:189 */     return false;
/* 139:    */   }
/* 140:    */   
/* 141:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 142:    */     throws IOException
/* 143:    */   {
/* 144:195 */     int number = input.readFieldNumber(schema);
/* 145:196 */     if (number != 52) {
/* 146:197 */       throw new ProtostuffException("Corrupt input.");
/* 147:    */     }
/* 148:199 */     return readObjectFrom(input, schema, owner, strategy, number);
/* 149:    */   }
/* 150:    */   
/* 151:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, int number)
/* 152:    */     throws IOException
/* 153:    */   {
/* 154:206 */     Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
/* 155:    */     
/* 156:208 */     Object pojo = derivedSchema.newMessage();
/* 157:210 */     if ((input instanceof GraphInput)) {
/* 158:213 */       ((GraphInput)input).updateLast(pojo, owner);
/* 159:    */     }
/* 160:216 */     if (__cause != null)
/* 161:    */     {
/* 162:    */       try
/* 163:    */       {
/* 164:221 */         cause = __cause.get(pojo);
/* 165:    */       }
/* 166:    */       catch (Exception e)
/* 167:    */       {
/* 168:    */         Object cause;
/* 169:225 */         throw new RuntimeException(e);
/* 170:    */       }
/* 171:    */       Object cause;
/* 172:228 */       if (cause == null) {
/* 173:    */         try
/* 174:    */         {
/* 175:234 */           __cause.set(pojo, cause);
/* 176:    */         }
/* 177:    */         catch (Exception e)
/* 178:    */         {
/* 179:238 */           throw new RuntimeException(e);
/* 180:    */         }
/* 181:    */       }
/* 182:    */     }
/* 183:243 */     derivedSchema.mergeFrom(input, pojo);
/* 184:244 */     return pojo;
/* 185:    */   }
/* 186:    */   
/* 187:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 188:    */     throws IOException
/* 189:    */   {
/* 190:250 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 191:251 */     if (number != 52) {
/* 192:252 */       throw new ProtostuffException("Corrupt input.");
/* 193:    */     }
/* 194:254 */     transferObject(pipeSchema, pipe, input, output, strategy, number);
/* 195:    */   }
/* 196:    */   
/* 197:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, int number)
/* 198:    */     throws IOException
/* 199:    */   {
/* 200:262 */     Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
/* 201:264 */     if ((output instanceof StatefulOutput)) {
/* 202:267 */       ((StatefulOutput)output).updateLast(derivedPipeSchema, pipeSchema);
/* 203:    */     }
/* 204:270 */     Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
/* 205:    */   }
/* 206:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicThrowableSchema
 * JD-Core Version:    0.7.0.1
 */