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
/*  12:    */ import java.lang.reflect.Array;
/*  13:    */ 
/*  14:    */ public abstract class ArraySchema
/*  15:    */   extends PolymorphicSchema
/*  16:    */ {
/*  17:    */   static final int ID_ARRAY_LEN = 3;
/*  18:    */   static final int ID_ARRAY_DIMENSION = 2;
/*  19:    */   static final String STR_ARRAY_LEN = "c";
/*  20:    */   static final String STR_ARRAY_DIMENSION = "b";
/*  21:    */   
/*  22:    */   static String name(int number)
/*  23:    */   {
/*  24: 65 */     switch (number)
/*  25:    */     {
/*  26:    */     case 2: 
/*  27: 68 */       return "b";
/*  28:    */     case 3: 
/*  29: 70 */       return "c";
/*  30:    */     case 15: 
/*  31: 72 */       return "o";
/*  32:    */     case 17: 
/*  33: 74 */       return "q";
/*  34:    */     }
/*  35: 76 */     return null;
/*  36:    */   }
/*  37:    */   
/*  38:    */   static int number(String name)
/*  39:    */   {
/*  40: 82 */     if (name.length() != 1) {
/*  41: 83 */       return 0;
/*  42:    */     }
/*  43: 85 */     switch (name.charAt(0))
/*  44:    */     {
/*  45:    */     case 'b': 
/*  46: 88 */       return 2;
/*  47:    */     case 'c': 
/*  48: 90 */       return 3;
/*  49:    */     case 'o': 
/*  50: 92 */       return 15;
/*  51:    */     case 'q': 
/*  52: 94 */       return 17;
/*  53:    */     }
/*  54: 96 */     return 0;
/*  55:    */   }
/*  56:    */   
/*  57:100 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  58:    */   {
/*  59:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  60:    */       throws IOException
/*  61:    */     {
/*  62:107 */       ArraySchema.transferObject(this, pipe, input, output, ArraySchema.this.strategy);
/*  63:    */     }
/*  64:    */   };
/*  65:    */   
/*  66:    */   public ArraySchema(IdStrategy strategy)
/*  67:    */   {
/*  68:113 */     super(strategy);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public Pipe.Schema<Object> getPipeSchema()
/*  72:    */   {
/*  73:119 */     return this.pipeSchema;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public String getFieldName(int number)
/*  77:    */   {
/*  78:125 */     return name(number);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public int getFieldNumber(String name)
/*  82:    */   {
/*  83:131 */     return number(name);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public String messageFullName()
/*  87:    */   {
/*  88:137 */     return Array.class.getName();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public String messageName()
/*  92:    */   {
/*  93:143 */     return Array.class.getSimpleName();
/*  94:    */   }
/*  95:    */   
/*  96:    */   public void mergeFrom(Input input, Object owner)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:149 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void writeTo(Output output, Object value)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:155 */     writeObjectTo(output, value, this, this.strategy);
/* 106:    */   }
/* 107:    */   
/* 108:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:161 */     Class<?> clazz = value.getClass();
/* 112:162 */     int dimensions = 1;
/* 113:163 */     Class<?> componentType = clazz.getComponentType();
/* 114:164 */     while (componentType.isArray())
/* 115:    */     {
/* 116:166 */       dimensions++;
/* 117:167 */       componentType = componentType.getComponentType();
/* 118:    */     }
/* 119:170 */     strategy.writeArrayIdTo(output, componentType);
/* 120:    */     
/* 121:172 */     output.writeUInt32(3, Array.getLength(value), false);
/* 122:    */     
/* 123:174 */     output.writeUInt32(2, dimensions, false);
/* 124:176 */     if ((output instanceof StatefulOutput)) {
/* 125:179 */       ((StatefulOutput)output).updateLast(strategy.ARRAY_SCHEMA, currentSchema);
/* 126:    */     }
/* 127:183 */     strategy.ARRAY_SCHEMA.writeTo(output, value);
/* 128:    */   }
/* 129:    */   
/* 130:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:189 */     int number = input.readFieldNumber(schema);
/* 134:    */     boolean mapped;
/* 135:    */     boolean mapped;
/* 136:191 */     switch (number)
/* 137:    */     {
/* 138:    */     case 15: 
/* 139:194 */       mapped = false;
/* 140:195 */       break;
/* 141:    */     case 17: 
/* 142:198 */       mapped = true;
/* 143:199 */       break;
/* 144:    */     default: 
/* 145:202 */       throw new ProtostuffException("Corrupt input.");
/* 146:    */     }
/* 147:    */     boolean mapped;
/* 148:205 */     ObjectSchema.ArrayWrapper mArrayWrapper = ObjectSchema.newArrayWrapper(input, schema, mapped, strategy);
/* 149:208 */     if ((input instanceof GraphInput)) {
/* 150:211 */       ((GraphInput)input).updateLast(mArrayWrapper.array, owner);
/* 151:    */     }
/* 152:214 */     strategy.COLLECTION_SCHEMA.mergeFrom(input, mArrayWrapper);
/* 153:    */     
/* 154:216 */     return mArrayWrapper.array;
/* 155:    */   }
/* 156:    */   
/* 157:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 158:    */     throws IOException
/* 159:    */   {
/* 160:222 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 161:223 */     switch (number)
/* 162:    */     {
/* 163:    */     case 15: 
/* 164:226 */       ObjectSchema.transferArray(pipe, input, output, number, pipeSchema, false, strategy);
/* 165:    */       
/* 166:228 */       return;
/* 167:    */     case 17: 
/* 168:231 */       ObjectSchema.transferArray(pipe, input, output, number, pipeSchema, true, strategy);
/* 169:    */       
/* 170:233 */       return;
/* 171:    */     }
/* 172:236 */     throw new ProtostuffException("Corrupt input.");
/* 173:    */   }
/* 174:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.ArraySchema
 * JD-Core Version:    0.7.0.1
 */