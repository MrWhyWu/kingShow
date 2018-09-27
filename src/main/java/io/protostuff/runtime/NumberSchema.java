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
/*  13:    */ public abstract class NumberSchema
/*  14:    */   extends PolymorphicSchema
/*  15:    */ {
/*  16:    */   static String name(int number)
/*  17:    */   {
/*  18: 79 */     switch (number)
/*  19:    */     {
/*  20:    */     case 2: 
/*  21: 82 */       return "b";
/*  22:    */     case 4: 
/*  23: 84 */       return "d";
/*  24:    */     case 5: 
/*  25: 86 */       return "e";
/*  26:    */     case 6: 
/*  27: 88 */       return "f";
/*  28:    */     case 7: 
/*  29: 90 */       return "g";
/*  30:    */     case 8: 
/*  31: 92 */       return "h";
/*  32:    */     case 12: 
/*  33: 94 */       return "l";
/*  34:    */     case 13: 
/*  35: 96 */       return "m";
/*  36:    */     case 127: 
/*  37: 99 */       return "_";
/*  38:    */     }
/*  39:101 */     return null;
/*  40:    */   }
/*  41:    */   
/*  42:    */   static int number(String name)
/*  43:    */   {
/*  44:107 */     if (name.length() != 1) {
/*  45:108 */       return 0;
/*  46:    */     }
/*  47:110 */     switch (name.charAt(0))
/*  48:    */     {
/*  49:    */     case '_': 
/*  50:113 */       return 127;
/*  51:    */     case 'b': 
/*  52:115 */       return 2;
/*  53:    */     case 'd': 
/*  54:117 */       return 4;
/*  55:    */     case 'e': 
/*  56:119 */       return 5;
/*  57:    */     case 'f': 
/*  58:121 */       return 6;
/*  59:    */     case 'g': 
/*  60:123 */       return 7;
/*  61:    */     case 'h': 
/*  62:125 */       return 8;
/*  63:    */     case 'l': 
/*  64:127 */       return 12;
/*  65:    */     case 'm': 
/*  66:129 */       return 13;
/*  67:    */     }
/*  68:131 */     return 0;
/*  69:    */   }
/*  70:    */   
/*  71:135 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  72:    */   {
/*  73:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  74:    */       throws IOException
/*  75:    */     {
/*  76:142 */       NumberSchema.transferObject(this, pipe, input, output, NumberSchema.this.strategy);
/*  77:    */     }
/*  78:    */   };
/*  79:    */   
/*  80:    */   public NumberSchema(IdStrategy strategy)
/*  81:    */   {
/*  82:148 */     super(strategy);
/*  83:    */   }
/*  84:    */   
/*  85:    */   public Pipe.Schema<Object> getPipeSchema()
/*  86:    */   {
/*  87:154 */     return this.pipeSchema;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public String getFieldName(int number)
/*  91:    */   {
/*  92:160 */     return name(number);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public int getFieldNumber(String name)
/*  96:    */   {
/*  97:166 */     return number(name);
/*  98:    */   }
/*  99:    */   
/* 100:    */   public String messageFullName()
/* 101:    */   {
/* 102:172 */     return Number.class.getName();
/* 103:    */   }
/* 104:    */   
/* 105:    */   public String messageName()
/* 106:    */   {
/* 107:178 */     return Number.class.getSimpleName();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public void mergeFrom(Input input, Object owner)
/* 111:    */     throws IOException
/* 112:    */   {
/* 113:184 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void writeTo(Output output, Object value)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:190 */     writeObjectTo(output, value, this, this.strategy);
/* 120:    */   }
/* 121:    */   
/* 122:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/* 123:    */     throws IOException
/* 124:    */   {
/* 125:197 */     Class<Object> clazz = value.getClass();
/* 126:    */     
/* 127:    */ 
/* 128:200 */     RuntimeFieldFactory<Object> inline = RuntimeFieldFactory.getInline(clazz);
/* 129:201 */     if (inline != null)
/* 130:    */     {
/* 131:204 */       inline.writeTo(output, inline.id, value, false);
/* 132:205 */       return;
/* 133:    */     }
/* 134:210 */     Schema<Object> schema = strategy.writePojoIdTo(output, 127, clazz).getSchema();
/* 135:212 */     if ((output instanceof StatefulOutput)) {
/* 136:215 */       ((StatefulOutput)output).updateLast(schema, currentSchema);
/* 137:    */     }
/* 138:218 */     schema.writeTo(output, value);
/* 139:    */   }
/* 140:    */   
/* 141:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 142:    */     throws IOException
/* 143:    */   {
/* 144:224 */     int number = input.readFieldNumber(schema);
/* 145:226 */     if (number == 127)
/* 146:    */     {
/* 147:230 */       Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
/* 148:    */       
/* 149:232 */       Object pojo = derivedSchema.newMessage();
/* 150:234 */       if ((input instanceof GraphInput)) {
/* 151:237 */         ((GraphInput)input).updateLast(pojo, owner);
/* 152:    */       }
/* 153:240 */       derivedSchema.mergeFrom(input, pojo);
/* 154:241 */       return pojo;
/* 155:    */     }
/* 156:    */     Object value;
/* 157:    */     Object value;
/* 158:    */     Object value;
/* 159:    */     Object value;
/* 160:    */     Object value;
/* 161:    */     Object value;
/* 162:    */     Object value;
/* 163:    */     Object value;
/* 164:245 */     switch (number)
/* 165:    */     {
/* 166:    */     case 2: 
/* 167:248 */       value = RuntimeFieldFactory.BYTE.readFrom(input);
/* 168:249 */       break;
/* 169:    */     case 4: 
/* 170:251 */       value = RuntimeFieldFactory.SHORT.readFrom(input);
/* 171:252 */       break;
/* 172:    */     case 5: 
/* 173:254 */       value = RuntimeFieldFactory.INT32.readFrom(input);
/* 174:255 */       break;
/* 175:    */     case 6: 
/* 176:257 */       value = RuntimeFieldFactory.INT64.readFrom(input);
/* 177:258 */       break;
/* 178:    */     case 7: 
/* 179:260 */       value = RuntimeFieldFactory.FLOAT.readFrom(input);
/* 180:261 */       break;
/* 181:    */     case 8: 
/* 182:263 */       value = RuntimeFieldFactory.DOUBLE.readFrom(input);
/* 183:264 */       break;
/* 184:    */     case 12: 
/* 185:266 */       value = RuntimeFieldFactory.BIGDECIMAL.readFrom(input);
/* 186:267 */       break;
/* 187:    */     case 13: 
/* 188:269 */       value = RuntimeFieldFactory.BIGINTEGER.readFrom(input);
/* 189:270 */       break;
/* 190:    */     case 3: 
/* 191:    */     case 9: 
/* 192:    */     case 10: 
/* 193:    */     case 11: 
/* 194:    */     default: 
/* 195:272 */       throw new ProtostuffException("Corrupt input.");
/* 196:    */     }
/* 197:    */     Object value;
/* 198:275 */     if ((input instanceof GraphInput)) {
/* 199:278 */       ((GraphInput)input).updateLast(value, owner);
/* 200:    */     }
/* 201:281 */     if (0 != input.readFieldNumber(schema)) {
/* 202:282 */       throw new ProtostuffException("Corrupt input.");
/* 203:    */     }
/* 204:284 */     return value;
/* 205:    */   }
/* 206:    */   
/* 207:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 208:    */     throws IOException
/* 209:    */   {
/* 210:290 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 211:291 */     if (number == 127)
/* 212:    */     {
/* 213:295 */       Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
/* 214:297 */       if ((output instanceof StatefulOutput)) {
/* 215:300 */         ((StatefulOutput)output).updateLast(derivedPipeSchema, pipeSchema);
/* 216:    */       }
/* 217:304 */       Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
/* 218:305 */       return;
/* 219:    */     }
/* 220:308 */     switch (number)
/* 221:    */     {
/* 222:    */     case 2: 
/* 223:311 */       RuntimeFieldFactory.BYTE.transfer(pipe, input, output, number, false);
/* 224:312 */       break;
/* 225:    */     case 4: 
/* 226:314 */       RuntimeFieldFactory.SHORT.transfer(pipe, input, output, number, false);
/* 227:315 */       break;
/* 228:    */     case 5: 
/* 229:317 */       RuntimeFieldFactory.INT32.transfer(pipe, input, output, number, false);
/* 230:318 */       break;
/* 231:    */     case 6: 
/* 232:320 */       RuntimeFieldFactory.INT64.transfer(pipe, input, output, number, false);
/* 233:321 */       break;
/* 234:    */     case 7: 
/* 235:323 */       RuntimeFieldFactory.FLOAT.transfer(pipe, input, output, number, false);
/* 236:324 */       break;
/* 237:    */     case 8: 
/* 238:326 */       RuntimeFieldFactory.DOUBLE.transfer(pipe, input, output, number, false);
/* 239:327 */       break;
/* 240:    */     case 12: 
/* 241:329 */       RuntimeFieldFactory.BIGDECIMAL.transfer(pipe, input, output, number, false);
/* 242:330 */       break;
/* 243:    */     case 13: 
/* 244:332 */       RuntimeFieldFactory.BIGINTEGER.transfer(pipe, input, output, number, false);
/* 245:333 */       break;
/* 246:    */     case 3: 
/* 247:    */     case 9: 
/* 248:    */     case 10: 
/* 249:    */     case 11: 
/* 250:    */     default: 
/* 251:335 */       throw new ProtostuffException("Corrupt input.");
/* 252:    */     }
/* 253:    */   }
/* 254:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.NumberSchema
 * JD-Core Version:    0.7.0.1
 */