/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.GraphInput;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.Output;
/*   6:    */ import io.protostuff.Pipe;
/*   7:    */ import io.protostuff.Pipe.Schema;
/*   8:    */ import io.protostuff.ProtostuffException;
/*   9:    */ import io.protostuff.Schema;
/*  10:    */ import java.io.IOException;
/*  11:    */ 
/*  12:    */ public abstract class ClassSchema
/*  13:    */   extends PolymorphicSchema
/*  14:    */ {
/*  15:    */   static final int ID_ARRAY_DIMENSION = 2;
/*  16:    */   static final String STR_ARRAY_DIMENSION = "b";
/*  17:    */   
/*  18:    */   static String name(int number)
/*  19:    */   {
/*  20: 63 */     switch (number)
/*  21:    */     {
/*  22:    */     case 2: 
/*  23: 66 */       return "b";
/*  24:    */     case 18: 
/*  25: 68 */       return "r";
/*  26:    */     case 19: 
/*  27: 70 */       return "s";
/*  28:    */     case 20: 
/*  29: 72 */       return "t";
/*  30:    */     case 21: 
/*  31: 74 */       return "u";
/*  32:    */     }
/*  33: 76 */     return null;
/*  34:    */   }
/*  35:    */   
/*  36:    */   static int number(String name)
/*  37:    */   {
/*  38: 82 */     if (name.length() != 1) {
/*  39: 83 */       return 0;
/*  40:    */     }
/*  41: 85 */     switch (name.charAt(0))
/*  42:    */     {
/*  43:    */     case 'b': 
/*  44: 88 */       return 2;
/*  45:    */     case 'r': 
/*  46: 90 */       return 18;
/*  47:    */     case 's': 
/*  48: 92 */       return 19;
/*  49:    */     case 't': 
/*  50: 94 */       return 20;
/*  51:    */     case 'u': 
/*  52: 96 */       return 21;
/*  53:    */     }
/*  54: 98 */     return 0;
/*  55:    */   }
/*  56:    */   
/*  57:102 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  58:    */   {
/*  59:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  60:    */       throws IOException
/*  61:    */     {
/*  62:109 */       ClassSchema.transferObject(this, pipe, input, output, ClassSchema.this.strategy);
/*  63:    */     }
/*  64:    */   };
/*  65:    */   
/*  66:    */   public ClassSchema(IdStrategy strategy)
/*  67:    */   {
/*  68:115 */     super(strategy);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public Pipe.Schema<Object> getPipeSchema()
/*  72:    */   {
/*  73:121 */     return this.pipeSchema;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public String getFieldName(int number)
/*  77:    */   {
/*  78:127 */     return name(number);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public int getFieldNumber(String name)
/*  82:    */   {
/*  83:133 */     return number(name);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public String messageFullName()
/*  87:    */   {
/*  88:139 */     return Class.class.getName();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public String messageName()
/*  92:    */   {
/*  93:145 */     return Class.class.getSimpleName();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public void mergeFrom(Input input, Object owner)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:151 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void writeTo(Output output, Object value)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:157 */     writeObjectTo(output, value, this, this.strategy);
/* 106:    */   }
/* 107:    */   
/* 108:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:163 */     Class<?> c = (Class)value;
/* 112:164 */     if (c.isArray())
/* 113:    */     {
/* 114:166 */       int dimensions = 1;
/* 115:167 */       Class<?> componentType = c.getComponentType();
/* 116:168 */       while (componentType.isArray())
/* 117:    */       {
/* 118:170 */         dimensions++;
/* 119:171 */         componentType = componentType.getComponentType();
/* 120:    */       }
/* 121:174 */       strategy.writeClassIdTo(output, componentType, true);
/* 122:    */       
/* 123:176 */       output.writeUInt32(2, dimensions, false);
/* 124:177 */       return;
/* 125:    */     }
/* 126:180 */     strategy.writeClassIdTo(output, c, false);
/* 127:    */   }
/* 128:    */   
/* 129:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 130:    */     throws IOException
/* 131:    */   {
/* 132:186 */     int number = input.readFieldNumber(schema);
/* 133:    */     Object value;
/* 134:    */     Object value;
/* 135:    */     Object value;
/* 136:    */     Object value;
/* 137:188 */     switch (number)
/* 138:    */     {
/* 139:    */     case 18: 
/* 140:191 */       value = strategy.resolveClassFrom(input, false, false);
/* 141:192 */       break;
/* 142:    */     case 19: 
/* 143:195 */       value = strategy.resolveClassFrom(input, true, false);
/* 144:196 */       break;
/* 145:    */     case 20: 
/* 146:199 */       value = ObjectSchema.getArrayClass(input, schema, strategy
/* 147:200 */         .resolveClassFrom(input, false, true));
/* 148:201 */       break;
/* 149:    */     case 21: 
/* 150:204 */       value = ObjectSchema.getArrayClass(input, schema, strategy
/* 151:205 */         .resolveClassFrom(input, true, true));
/* 152:206 */       break;
/* 153:    */     default: 
/* 154:209 */       throw new ProtostuffException("Corrupt input.");
/* 155:    */     }
/* 156:    */     Object value;
/* 157:212 */     if ((input instanceof GraphInput)) {
/* 158:215 */       ((GraphInput)input).updateLast(value, owner);
/* 159:    */     }
/* 160:218 */     if (0 != input.readFieldNumber(schema)) {
/* 161:219 */       throw new ProtostuffException("Corrupt input.");
/* 162:    */     }
/* 163:221 */     return value;
/* 164:    */   }
/* 165:    */   
/* 166:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 167:    */     throws IOException
/* 168:    */   {
/* 169:227 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 170:228 */     switch (number)
/* 171:    */     {
/* 172:    */     case 18: 
/* 173:231 */       ObjectSchema.transferClass(pipe, input, output, number, pipeSchema, false, false, strategy);
/* 174:    */       
/* 175:233 */       break;
/* 176:    */     case 19: 
/* 177:236 */       ObjectSchema.transferClass(pipe, input, output, number, pipeSchema, true, false, strategy);
/* 178:    */       
/* 179:238 */       break;
/* 180:    */     case 20: 
/* 181:241 */       ObjectSchema.transferClass(pipe, input, output, number, pipeSchema, false, true, strategy);
/* 182:    */       
/* 183:243 */       break;
/* 184:    */     case 21: 
/* 185:246 */       ObjectSchema.transferClass(pipe, input, output, number, pipeSchema, true, true, strategy);
/* 186:    */       
/* 187:248 */       break;
/* 188:    */     default: 
/* 189:251 */       throw new ProtostuffException("Corrupt input.");
/* 190:    */     }
/* 191:254 */     if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 192:255 */       throw new ProtostuffException("Corrupt input.");
/* 193:    */     }
/* 194:    */   }
/* 195:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.ClassSchema
 * JD-Core Version:    0.7.0.1
 */