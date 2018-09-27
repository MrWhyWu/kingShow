/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.ByteString;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.Message;
/*   6:    */ import io.protostuff.Morph;
/*   7:    */ import io.protostuff.Output;
/*   8:    */ import io.protostuff.Pipe;
/*   9:    */ import io.protostuff.WireFormat.FieldType;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.lang.reflect.Array;
/*  12:    */ import java.lang.reflect.GenericArrayType;
/*  13:    */ import java.lang.reflect.Modifier;
/*  14:    */ import java.lang.reflect.ParameterizedType;
/*  15:    */ import java.lang.reflect.Type;
/*  16:    */ import java.math.BigDecimal;
/*  17:    */ import java.math.BigInteger;
/*  18:    */ import java.util.Collection;
/*  19:    */ import java.util.Date;
/*  20:    */ import java.util.HashMap;
/*  21:    */ import java.util.Map;
/*  22:    */ 
/*  23:    */ public abstract class RuntimeFieldFactory<V>
/*  24:    */   implements Delegate<V>
/*  25:    */ {
/*  26:    */   static final int ID_BOOL = 1;
/*  27:    */   static final int ID_BYTE = 2;
/*  28:    */   static final int ID_CHAR = 3;
/*  29:    */   static final int ID_SHORT = 4;
/*  30:    */   static final int ID_INT32 = 5;
/*  31:    */   static final int ID_INT64 = 6;
/*  32:    */   static final int ID_FLOAT = 7;
/*  33:    */   static final int ID_DOUBLE = 8;
/*  34:    */   static final int ID_STRING = 9;
/*  35:    */   static final int ID_BYTES = 10;
/*  36:    */   static final int ID_BYTE_ARRAY = 11;
/*  37:    */   static final int ID_BIGDECIMAL = 12;
/*  38:    */   static final int ID_BIGINTEGER = 13;
/*  39:    */   static final int ID_DATE = 14;
/*  40:    */   static final int ID_ARRAY = 15;
/*  41:    */   static final int ID_OBJECT = 16;
/*  42:    */   static final int ID_ARRAY_MAPPED = 17;
/*  43:    */   static final int ID_CLASS = 18;
/*  44:    */   static final int ID_CLASS_MAPPED = 19;
/*  45:    */   static final int ID_CLASS_ARRAY = 20;
/*  46:    */   static final int ID_CLASS_ARRAY_MAPPED = 21;
/*  47:    */   static final int ID_ENUM_SET = 22;
/*  48:    */   static final int ID_ENUM_MAP = 23;
/*  49:    */   static final int ID_ENUM = 24;
/*  50:    */   static final int ID_COLLECTION = 25;
/*  51:    */   static final int ID_MAP = 26;
/*  52:    */   static final int ID_POLYMORPHIC_COLLECTION = 28;
/*  53:    */   static final int ID_POLYMORPHIC_MAP = 29;
/*  54:    */   static final int ID_DELEGATE = 30;
/*  55:    */   static final int ID_ARRAY_DELEGATE = 32;
/*  56:    */   static final int ID_ARRAY_SCALAR = 33;
/*  57:    */   static final int ID_ARRAY_ENUM = 34;
/*  58:    */   static final int ID_ARRAY_POJO = 35;
/*  59:    */   static final int ID_THROWABLE = 52;
/*  60:    */   static final int ID_POJO = 127;
/*  61:    */   static final String STR_BOOL = "a";
/*  62:    */   static final String STR_BYTE = "b";
/*  63:    */   static final String STR_CHAR = "c";
/*  64:    */   static final String STR_SHORT = "d";
/*  65:    */   static final String STR_INT32 = "e";
/*  66:    */   static final String STR_INT64 = "f";
/*  67:    */   static final String STR_FLOAT = "g";
/*  68:    */   static final String STR_DOUBLE = "h";
/*  69:    */   static final String STR_STRING = "i";
/*  70:    */   static final String STR_BYTES = "j";
/*  71:    */   static final String STR_BYTE_ARRAY = "k";
/*  72:    */   static final String STR_BIGDECIMAL = "l";
/*  73:    */   static final String STR_BIGINTEGER = "m";
/*  74:    */   static final String STR_DATE = "n";
/*  75:    */   static final String STR_ARRAY = "o";
/*  76:    */   static final String STR_OBJECT = "p";
/*  77:    */   static final String STR_ARRAY_MAPPED = "q";
/*  78:    */   static final String STR_CLASS = "r";
/*  79:    */   static final String STR_CLASS_MAPPED = "s";
/*  80:    */   static final String STR_CLASS_ARRAY = "t";
/*  81:    */   static final String STR_CLASS_ARRAY_MAPPED = "u";
/*  82:    */   static final String STR_ENUM_SET = "v";
/*  83:    */   static final String STR_ENUM_MAP = "w";
/*  84:    */   static final String STR_ENUM = "x";
/*  85:    */   static final String STR_COLLECTION = "y";
/*  86:    */   static final String STR_MAP = "z";
/*  87:    */   static final String STR_POLYMORPHIC_COLLECTION = "B";
/*  88:    */   static final String STR_POLYMOPRHIC_MAP = "C";
/*  89:    */   static final String STR_DELEGATE = "D";
/*  90:    */   static final String STR_ARRAY_DELEGATE = "F";
/*  91:    */   static final String STR_ARRAY_SCALAR = "G";
/*  92:    */   static final String STR_ARRAY_ENUM = "H";
/*  93:    */   static final String STR_ARRAY_POJO = "I";
/*  94:    */   static final String STR_THROWABLE = "Z";
/*  95:    */   static final String STR_POJO = "_";
/*  96:101 */   private static final HashMap<String, RuntimeFieldFactory<?>> __inlineValues = new HashMap();
/*  97:    */   static final RuntimeFieldFactory<BigDecimal> BIGDECIMAL;
/*  98:    */   static final RuntimeFieldFactory<BigInteger> BIGINTEGER;
/*  99:    */   static final RuntimeFieldFactory<Boolean> BOOL;
/* 100:    */   static final RuntimeFieldFactory<Byte> BYTE;
/* 101:    */   static final RuntimeFieldFactory<ByteString> BYTES;
/* 102:    */   static final RuntimeFieldFactory<byte[]> BYTE_ARRAY;
/* 103:    */   static final RuntimeFieldFactory<Character> CHAR;
/* 104:    */   static final RuntimeFieldFactory<Date> DATE;
/* 105:    */   static final RuntimeFieldFactory<Double> DOUBLE;
/* 106:    */   static final RuntimeFieldFactory<Float> FLOAT;
/* 107:    */   static final RuntimeFieldFactory<Integer> INT32;
/* 108:    */   static final RuntimeFieldFactory<Long> INT64;
/* 109:    */   static final RuntimeFieldFactory<Short> SHORT;
/* 110:    */   static final RuntimeFieldFactory<String> STRING;
/* 111:    */   static final RuntimeFieldFactory<Integer> ENUM;
/* 112:    */   static final RuntimeFieldFactory<Object> OBJECT;
/* 113:    */   static final RuntimeFieldFactory<Object> POJO;
/* 114:    */   static final RuntimeFieldFactory<Object> POLYMORPHIC_POJO;
/* 115:123 */   static final RuntimeFieldFactory<Collection<?>> COLLECTION = new RuntimeFieldFactory(25)
/* 116:    */   {
/* 117:    */     public <T> Field<T> create(int number, String name, java.lang.reflect.Field field, IdStrategy strategy)
/* 118:    */     {
/* 119:133 */       RuntimeFieldFactory<Collection<?>> factory = 0 != (0x40 & strategy.flags) ? RuntimeCollectionFieldFactory.getFactory() : RuntimeRepeatedFieldFactory.getFactory();
/* 120:    */       
/* 121:135 */       return factory.create(number, name, field, strategy);
/* 122:    */     }
/* 123:    */     
/* 124:    */     public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 125:    */       throws IOException
/* 126:    */     {
/* 127:142 */       throw new UnsupportedOperationException();
/* 128:    */     }
/* 129:    */     
/* 130:    */     public Collection<?> readFrom(Input input)
/* 131:    */       throws IOException
/* 132:    */     {
/* 133:148 */       throw new UnsupportedOperationException();
/* 134:    */     }
/* 135:    */     
/* 136:    */     public void writeTo(Output output, int number, Collection<?> value, boolean repeated)
/* 137:    */       throws IOException
/* 138:    */     {
/* 139:155 */       throw new UnsupportedOperationException();
/* 140:    */     }
/* 141:    */     
/* 142:    */     public WireFormat.FieldType getFieldType()
/* 143:    */     {
/* 144:161 */       throw new UnsupportedOperationException();
/* 145:    */     }
/* 146:    */     
/* 147:    */     public Class<?> typeClass()
/* 148:    */     {
/* 149:167 */       throw new UnsupportedOperationException();
/* 150:    */     }
/* 151:    */   };
/* 152:    */   static final RuntimeFieldFactory<Object> DELEGATE;
/* 153:    */   static final Accessor.Factory ACCESSOR_FACTORY;
/* 154:    */   final int id;
/* 155:    */   
/* 156:    */   static
/* 157:    */   {
/* 158:178 */     if (RuntimeEnv.USE_SUN_MISC_UNSAFE)
/* 159:    */     {
/* 160:180 */       BIGDECIMAL = RuntimeUnsafeFieldFactory.BIGDECIMAL;
/* 161:181 */       BIGINTEGER = RuntimeUnsafeFieldFactory.BIGINTEGER;
/* 162:182 */       BOOL = RuntimeUnsafeFieldFactory.BOOL;
/* 163:183 */       BYTE = RuntimeUnsafeFieldFactory.BYTE;
/* 164:184 */       BYTES = RuntimeUnsafeFieldFactory.BYTES;
/* 165:185 */       BYTE_ARRAY = RuntimeUnsafeFieldFactory.BYTE_ARRAY;
/* 166:186 */       CHAR = RuntimeUnsafeFieldFactory.CHAR;
/* 167:187 */       DATE = RuntimeUnsafeFieldFactory.DATE;
/* 168:188 */       DOUBLE = RuntimeUnsafeFieldFactory.DOUBLE;
/* 169:189 */       FLOAT = RuntimeUnsafeFieldFactory.FLOAT;
/* 170:190 */       INT32 = RuntimeUnsafeFieldFactory.INT32;
/* 171:191 */       INT64 = RuntimeUnsafeFieldFactory.INT64;
/* 172:192 */       SHORT = RuntimeUnsafeFieldFactory.SHORT;
/* 173:193 */       STRING = RuntimeUnsafeFieldFactory.STRING;
/* 174:    */       
/* 175:195 */       ENUM = RuntimeUnsafeFieldFactory.ENUM;
/* 176:196 */       OBJECT = RuntimeUnsafeFieldFactory.OBJECT;
/* 177:197 */       POJO = RuntimeUnsafeFieldFactory.POJO;
/* 178:198 */       POLYMORPHIC_POJO = RuntimeUnsafeFieldFactory.POLYMORPHIC_POJO;
/* 179:    */       
/* 180:200 */       DELEGATE = RuntimeUnsafeFieldFactory.DELEGATE;
/* 181:201 */       ACCESSOR_FACTORY = UnsafeAccessor.FACTORY;
/* 182:    */     }
/* 183:    */     else
/* 184:    */     {
/* 185:205 */       BIGDECIMAL = RuntimeReflectionFieldFactory.BIGDECIMAL;
/* 186:206 */       BIGINTEGER = RuntimeReflectionFieldFactory.BIGINTEGER;
/* 187:207 */       BOOL = RuntimeReflectionFieldFactory.BOOL;
/* 188:208 */       BYTE = RuntimeReflectionFieldFactory.BYTE;
/* 189:209 */       BYTES = RuntimeReflectionFieldFactory.BYTES;
/* 190:210 */       BYTE_ARRAY = RuntimeReflectionFieldFactory.BYTE_ARRAY;
/* 191:211 */       CHAR = RuntimeReflectionFieldFactory.CHAR;
/* 192:212 */       DATE = RuntimeReflectionFieldFactory.DATE;
/* 193:213 */       DOUBLE = RuntimeReflectionFieldFactory.DOUBLE;
/* 194:214 */       FLOAT = RuntimeReflectionFieldFactory.FLOAT;
/* 195:215 */       INT32 = RuntimeReflectionFieldFactory.INT32;
/* 196:216 */       INT64 = RuntimeReflectionFieldFactory.INT64;
/* 197:217 */       SHORT = RuntimeReflectionFieldFactory.SHORT;
/* 198:218 */       STRING = RuntimeReflectionFieldFactory.STRING;
/* 199:    */       
/* 200:220 */       ENUM = RuntimeReflectionFieldFactory.ENUM;
/* 201:221 */       OBJECT = RuntimeReflectionFieldFactory.OBJECT;
/* 202:222 */       POJO = RuntimeReflectionFieldFactory.POJO;
/* 203:223 */       POLYMORPHIC_POJO = RuntimeReflectionFieldFactory.POLYMORPHIC_POJO;
/* 204:    */       
/* 205:225 */       DELEGATE = RuntimeReflectionFieldFactory.DELEGATE;
/* 206:226 */       ACCESSOR_FACTORY = ReflectAccessor.FACTORY;
/* 207:    */     }
/* 208:229 */     __inlineValues.put(Integer.TYPE.getName(), INT32);
/* 209:230 */     __inlineValues.put(Integer.class.getName(), INT32);
/* 210:231 */     __inlineValues.put(Long.TYPE.getName(), INT64);
/* 211:232 */     __inlineValues.put(Long.class.getName(), INT64);
/* 212:233 */     __inlineValues.put(Float.TYPE.getName(), FLOAT);
/* 213:234 */     __inlineValues.put(Float.class.getName(), FLOAT);
/* 214:235 */     __inlineValues.put(Double.TYPE.getName(), DOUBLE);
/* 215:236 */     __inlineValues.put(Double.class.getName(), DOUBLE);
/* 216:237 */     __inlineValues.put(Boolean.TYPE.getName(), BOOL);
/* 217:238 */     __inlineValues.put(Boolean.class.getName(), BOOL);
/* 218:239 */     __inlineValues.put(Character.TYPE.getName(), CHAR);
/* 219:240 */     __inlineValues.put(Character.class.getName(), CHAR);
/* 220:241 */     __inlineValues.put(Short.TYPE.getName(), SHORT);
/* 221:242 */     __inlineValues.put(Short.class.getName(), SHORT);
/* 222:243 */     __inlineValues.put(Byte.TYPE.getName(), BYTE);
/* 223:244 */     __inlineValues.put(Byte.class.getName(), BYTE);
/* 224:245 */     __inlineValues.put(String.class.getName(), STRING);
/* 225:246 */     __inlineValues.put(ByteString.class.getName(), BYTES);
/* 226:247 */     __inlineValues.put([B.class.getName(), BYTE_ARRAY);
/* 227:248 */     __inlineValues.put(BigInteger.class.getName(), BIGINTEGER);
/* 228:249 */     __inlineValues.put(BigDecimal.class.getName(), BIGDECIMAL);
/* 229:250 */     __inlineValues.put(Date.class.getName(), DATE);
/* 230:    */   }
/* 231:    */   
/* 232:    */   public static RuntimeFieldFactory<?> getFieldFactory(Class<?> clazz)
/* 233:    */   {
/* 234:260 */     return getFieldFactory(clazz, RuntimeEnv.ID_STRATEGY);
/* 235:    */   }
/* 236:    */   
/* 237:    */   public static RuntimeFieldFactory<?> getFieldFactory(Class<?> clazz, IdStrategy strategy)
/* 238:    */   {
/* 239:269 */     if (strategy.isDelegateRegistered(clazz)) {
/* 240:270 */       return DELEGATE;
/* 241:    */     }
/* 242:272 */     RuntimeFieldFactory<?> inline = (RuntimeFieldFactory)__inlineValues.get(clazz
/* 243:273 */       .getName());
/* 244:274 */     if (inline != null) {
/* 245:275 */       return inline;
/* 246:    */     }
/* 247:277 */     if (Message.class.isAssignableFrom(clazz)) {
/* 248:278 */       return POJO;
/* 249:    */     }
/* 250:280 */     if (clazz.isEnum()) {
/* 251:281 */       return ENUM;
/* 252:    */     }
/* 253:287 */     if ((clazz.isArray()) || (Object.class == clazz) || (Number.class == clazz) || (Class.class == clazz) || (Enum.class == clazz) || 
/* 254:    */     
/* 255:289 */       (Throwable.class.isAssignableFrom(clazz))) {
/* 256:291 */       return OBJECT;
/* 257:    */     }
/* 258:294 */     if (strategy.isRegistered(clazz)) {
/* 259:295 */       return clazz.isInterface() ? POJO : POLYMORPHIC_POJO;
/* 260:    */     }
/* 261:297 */     if (Map.class.isAssignableFrom(clazz)) {
/* 262:298 */       return RuntimeMapFieldFactory.MAP;
/* 263:    */     }
/* 264:300 */     if (Collection.class.isAssignableFrom(clazz)) {
/* 265:303 */       return COLLECTION;
/* 266:    */     }
/* 267:316 */     if (clazz.isInterface()) {
/* 268:317 */       return OBJECT;
/* 269:    */     }
/* 270:320 */     return POLYMORPHIC_POJO;
/* 271:    */   }
/* 272:    */   
/* 273:    */   static boolean pojo(Class<?> clazz, Morph morph, IdStrategy strategy)
/* 274:    */   {
/* 275:325 */     if (Modifier.isFinal(clazz.getModifiers())) {
/* 276:326 */       return true;
/* 277:    */     }
/* 278:329 */     if (Modifier.isAbstract(clazz.getModifiers())) {
/* 279:330 */       return strategy.isRegistered(clazz);
/* 280:    */     }
/* 281:338 */     if (morph != null) {
/* 282:339 */       return !morph.value();
/* 283:    */     }
/* 284:341 */     return 0 == (0x8 & strategy.flags);
/* 285:    */   }
/* 286:    */   
/* 287:    */   static Class<?> getGenericType(java.lang.reflect.Field f, int index)
/* 288:    */   {
/* 289:    */     try
/* 290:    */     {
/* 291:349 */       Type type = ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[index];
/* 292:350 */       if ((type instanceof GenericArrayType))
/* 293:    */       {
/* 294:352 */         int dimensions = 1;
/* 295:    */         
/* 296:354 */         Type componentType = ((GenericArrayType)type).getGenericComponentType();
/* 297:355 */         while ((componentType instanceof GenericArrayType))
/* 298:    */         {
/* 299:357 */           dimensions++;
/* 300:    */           
/* 301:359 */           componentType = ((GenericArrayType)componentType).getGenericComponentType();
/* 302:    */         }
/* 303:365 */         if (dimensions == 1) {
/* 304:366 */           return 
/* 305:367 */             Array.newInstance((Class)componentType, 0).getClass();
/* 306:    */         }
/* 307:369 */         int[] arg = new int[dimensions];
/* 308:370 */         arg[0] = 0;
/* 309:371 */         return Array.newInstance((Class)componentType, arg)
/* 310:372 */           .getClass();
/* 311:    */       }
/* 312:375 */       if ((type instanceof ParameterizedType))
/* 313:    */       {
/* 314:384 */         Object rawType = ((ParameterizedType)type).getRawType();
/* 315:385 */         if (Class.class == rawType) {
/* 316:386 */           return Class.class;
/* 317:    */         }
/* 318:388 */         if (Enum.class == rawType) {
/* 319:389 */           return Enum.class;
/* 320:    */         }
/* 321:391 */         return null;
/* 322:    */       }
/* 323:394 */       return (Class)type;
/* 324:    */     }
/* 325:    */     catch (Exception e) {}
/* 326:398 */     return null;
/* 327:    */   }
/* 328:    */   
/* 329:    */   static <T> Delegate<T> getDelegateOrInline(Class<T> typeClass, IdStrategy strategy)
/* 330:    */   {
/* 331:406 */     Delegate<T> d = strategy.getDelegate(typeClass);
/* 332:407 */     if (d == null) {
/* 333:409 */       d = (RuntimeFieldFactory)__inlineValues.get(typeClass.getName());
/* 334:    */     }
/* 335:411 */     return d;
/* 336:    */   }
/* 337:    */   
/* 338:    */   public static <T> RuntimeFieldFactory<T> getInline(Class<T> typeClass)
/* 339:    */   {
/* 340:420 */     return (RuntimeFieldFactory)__inlineValues.get(typeClass.getName());
/* 341:    */   }
/* 342:    */   
/* 343:    */   static <T> RuntimeFieldFactory<T> getInline(String className)
/* 344:    */   {
/* 345:429 */     return (RuntimeFieldFactory)__inlineValues.get(className);
/* 346:    */   }
/* 347:    */   
/* 348:    */   public RuntimeFieldFactory(int id)
/* 349:    */   {
/* 350:439 */     this.id = id;
/* 351:    */   }
/* 352:    */   
/* 353:    */   public abstract <T> Field<T> create(int paramInt, String paramString, java.lang.reflect.Field paramField, IdStrategy paramIdStrategy);
/* 354:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeFieldFactory
 * JD-Core Version:    0.7.0.1
 */