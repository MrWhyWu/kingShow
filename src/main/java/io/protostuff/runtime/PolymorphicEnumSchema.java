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
/*  12:    */ public abstract class PolymorphicEnumSchema
/*  13:    */   extends PolymorphicSchema
/*  14:    */ {
/*  15:    */   static final int ID_ENUM_VALUE = 1;
/*  16:    */   static final String STR_ENUM_VALUE = "a";
/*  17:    */   
/*  18:    */   static String name(int number)
/*  19:    */   {
/*  20: 57 */     switch (number)
/*  21:    */     {
/*  22:    */     case 1: 
/*  23: 60 */       return "a";
/*  24:    */     case 24: 
/*  25: 62 */       return "x";
/*  26:    */     }
/*  27: 64 */     return null;
/*  28:    */   }
/*  29:    */   
/*  30:    */   static int number(String name)
/*  31:    */   {
/*  32: 70 */     if (name.length() != 1) {
/*  33: 71 */       return 0;
/*  34:    */     }
/*  35: 73 */     switch (name.charAt(0))
/*  36:    */     {
/*  37:    */     case 'a': 
/*  38: 76 */       return 1;
/*  39:    */     case 'x': 
/*  40: 78 */       return 24;
/*  41:    */     }
/*  42: 80 */     return 0;
/*  43:    */   }
/*  44:    */   
/*  45: 84 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  46:    */   {
/*  47:    */     protected void transfer(Pipe pipe, Input input, Output output)
/*  48:    */       throws IOException
/*  49:    */     {
/*  50: 91 */       PolymorphicEnumSchema.transferObject(this, pipe, input, output, PolymorphicEnumSchema.this.strategy);
/*  51:    */     }
/*  52:    */   };
/*  53:    */   
/*  54:    */   public PolymorphicEnumSchema(IdStrategy strategy)
/*  55:    */   {
/*  56: 97 */     super(strategy);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public Pipe.Schema<Object> getPipeSchema()
/*  60:    */   {
/*  61:103 */     return this.pipeSchema;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public String getFieldName(int number)
/*  65:    */   {
/*  66:109 */     return name(number);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public int getFieldNumber(String name)
/*  70:    */   {
/*  71:115 */     return number(name);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public String messageFullName()
/*  75:    */   {
/*  76:121 */     return Enum.class.getName();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public String messageName()
/*  80:    */   {
/*  81:127 */     return Enum.class.getSimpleName();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void mergeFrom(Input input, Object owner)
/*  85:    */     throws IOException
/*  86:    */   {
/*  87:133 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void writeTo(Output output, Object value)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93:139 */     writeObjectTo(output, value, this, this.strategy);
/*  94:    */   }
/*  95:    */   
/*  96:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:145 */     Class<?> clazz = value.getClass();
/* 100:146 */     if ((clazz.getSuperclass() != null) && (clazz.getSuperclass().isEnum()))
/* 101:    */     {
/* 102:148 */       EnumIO<?> eio = strategy.getEnumIO(clazz.getSuperclass());
/* 103:149 */       strategy.writeEnumIdTo(output, 24, clazz.getSuperclass());
/* 104:150 */       eio.writeTo(output, 1, false, (Enum)value);
/* 105:    */     }
/* 106:    */     else
/* 107:    */     {
/* 108:154 */       EnumIO<?> eio = strategy.getEnumIO(clazz);
/* 109:155 */       strategy.writeEnumIdTo(output, 24, clazz);
/* 110:156 */       eio.writeTo(output, 1, false, (Enum)value);
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:163 */     if (24 != input.readFieldNumber(schema)) {
/* 118:164 */       throw new ProtostuffException("Corrupt input.");
/* 119:    */     }
/* 120:166 */     EnumIO<?> eio = strategy.resolveEnumFrom(input);
/* 121:168 */     if (1 != input.readFieldNumber(schema)) {
/* 122:169 */       throw new ProtostuffException("Corrupt input.");
/* 123:    */     }
/* 124:171 */     Object value = eio.readFrom(input);
/* 125:173 */     if ((input instanceof GraphInput)) {
/* 126:176 */       ((GraphInput)input).updateLast(value, owner);
/* 127:    */     }
/* 128:179 */     if (0 != input.readFieldNumber(schema)) {
/* 129:180 */       throw new ProtostuffException("Corrupt input.");
/* 130:    */     }
/* 131:182 */     return value;
/* 132:    */   }
/* 133:    */   
/* 134:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 135:    */     throws IOException
/* 136:    */   {
/* 137:188 */     if (24 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 138:189 */       throw new ProtostuffException("Corrupt input.");
/* 139:    */     }
/* 140:191 */     strategy.transferEnumId(input, output, 24);
/* 141:193 */     if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 142:194 */       throw new ProtostuffException("Corrupt input.");
/* 143:    */     }
/* 144:196 */     EnumIO.transfer(pipe, input, output, 1, false, strategy);
/* 145:198 */     if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 146:199 */       throw new ProtostuffException("Corrupt input.");
/* 147:    */     }
/* 148:    */   }
/* 149:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicEnumSchema
 * JD-Core Version:    0.7.0.1
 */