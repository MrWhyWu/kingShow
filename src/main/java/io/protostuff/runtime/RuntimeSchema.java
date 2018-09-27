/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.Exclude;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.Message;
/*   6:    */ import io.protostuff.Output;
/*   7:    */ import io.protostuff.Pipe.Schema;
/*   8:    */ import io.protostuff.Schema;
/*   9:    */ import io.protostuff.Tag;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.lang.reflect.Constructor;
/*  12:    */ import java.lang.reflect.Method;
/*  13:    */ import java.lang.reflect.Modifier;
/*  14:    */ import java.util.ArrayList;
/*  15:    */ import java.util.Collection;
/*  16:    */ import java.util.Collections;
/*  17:    */ import java.util.HashSet;
/*  18:    */ import java.util.LinkedHashMap;
/*  19:    */ import java.util.List;
/*  20:    */ import java.util.Map;
/*  21:    */ import java.util.Map.Entry;
/*  22:    */ import java.util.Set;
/*  23:    */ 
/*  24:    */ public final class RuntimeSchema<T>
/*  25:    */   implements Schema<T>, FieldMap<T>
/*  26:    */ {
/*  27:    */   public static final int MIN_TAG_VALUE = 1;
/*  28:    */   public static final int MAX_TAG_VALUE = 536870911;
/*  29:    */   public static final String ERROR_TAG_VALUE = "Invalid tag number (value must be in range [1, 2^29-1])";
/*  30: 54 */   private static final Set<String> NO_EXCLUSIONS = ;
/*  31:    */   public static final int MIN_TAG_FOR_HASH_FIELD_MAP = 100;
/*  32:    */   private final Pipe.Schema<T> pipeSchema;
/*  33:    */   private final FieldMap<T> fieldMap;
/*  34:    */   private final Class<T> typeClass;
/*  35:    */   public final RuntimeEnv.Instantiator<T> instantiator;
/*  36:    */   
/*  37:    */   public static <T> boolean map(Class<? super T> baseClass, Class<T> typeClass)
/*  38:    */   {
/*  39: 77 */     if ((RuntimeEnv.ID_STRATEGY instanceof DefaultIdStrategy)) {
/*  40: 78 */       return ((DefaultIdStrategy)RuntimeEnv.ID_STRATEGY).map(baseClass, typeClass);
/*  41:    */     }
/*  42: 80 */     throw new RuntimeException("RuntimeSchema.map is only supported on DefaultIdStrategy");
/*  43:    */   }
/*  44:    */   
/*  45:    */   public static <T> boolean register(Class<T> typeClass, Schema<T> schema)
/*  46:    */   {
/*  47: 92 */     if ((RuntimeEnv.ID_STRATEGY instanceof DefaultIdStrategy)) {
/*  48: 93 */       return ((DefaultIdStrategy)RuntimeEnv.ID_STRATEGY).registerPojo(typeClass, schema);
/*  49:    */     }
/*  50: 96 */     throw new RuntimeException("RuntimeSchema.register is only supported on DefaultIdStrategy");
/*  51:    */   }
/*  52:    */   
/*  53:    */   public static <T> boolean register(Class<T> typeClass)
/*  54:    */   {
/*  55:108 */     if ((RuntimeEnv.ID_STRATEGY instanceof DefaultIdStrategy)) {
/*  56:109 */       return ((DefaultIdStrategy)RuntimeEnv.ID_STRATEGY).registerPojo(typeClass);
/*  57:    */     }
/*  58:111 */     throw new RuntimeException("RuntimeSchema.register is only supported on DefaultIdStrategy");
/*  59:    */   }
/*  60:    */   
/*  61:    */   public static boolean isRegistered(Class<?> typeClass)
/*  62:    */   {
/*  63:122 */     return isRegistered(typeClass, RuntimeEnv.ID_STRATEGY);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static boolean isRegistered(Class<?> typeClass, IdStrategy strategy)
/*  67:    */   {
/*  68:130 */     return strategy.isRegistered(typeClass);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public static <T> Schema<T> getSchema(Class<T> typeClass)
/*  72:    */   {
/*  73:140 */     return getSchema(typeClass, RuntimeEnv.ID_STRATEGY);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public static <T> Schema<T> getSchema(Class<T> typeClass, IdStrategy strategy)
/*  77:    */   {
/*  78:149 */     return strategy.getSchemaWrapper(typeClass, true).getSchema();
/*  79:    */   }
/*  80:    */   
/*  81:    */   static <T> HasSchema<T> getSchemaWrapper(Class<T> typeClass)
/*  82:    */   {
/*  83:159 */     return getSchemaWrapper(typeClass, RuntimeEnv.ID_STRATEGY);
/*  84:    */   }
/*  85:    */   
/*  86:    */   static <T> HasSchema<T> getSchemaWrapper(Class<T> typeClass, IdStrategy strategy)
/*  87:    */   {
/*  88:168 */     return strategy.getSchemaWrapper(typeClass, true);
/*  89:    */   }
/*  90:    */   
/*  91:    */   public static <T> RuntimeSchema<T> createFrom(Class<T> typeClass)
/*  92:    */   {
/*  93:178 */     return createFrom(typeClass, NO_EXCLUSIONS, RuntimeEnv.ID_STRATEGY);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public static <T> RuntimeSchema<T> createFrom(Class<T> typeClass, IdStrategy strategy)
/*  97:    */   {
/*  98:187 */     return createFrom(typeClass, NO_EXCLUSIONS, strategy);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public static <T> RuntimeSchema<T> createFrom(Class<T> typeClass, String[] exclusions, IdStrategy strategy)
/* 102:    */   {
/* 103:196 */     HashSet<String> set = new HashSet();
/* 104:197 */     for (String exclusion : exclusions) {
/* 105:198 */       set.add(exclusion);
/* 106:    */     }
/* 107:200 */     return createFrom(typeClass, set, strategy);
/* 108:    */   }
/* 109:    */   
/* 110:    */   public static <T> RuntimeSchema<T> createFrom(Class<T> typeClass, Set<String> exclusions, IdStrategy strategy)
/* 111:    */   {
/* 112:209 */     if ((typeClass.isInterface()) || 
/* 113:210 */       (Modifier.isAbstract(typeClass.getModifiers()))) {
/* 114:214 */       throw new RuntimeException("The root object can neither be an abstract class nor interface: \"" + typeClass.getName());
/* 115:    */     }
/* 116:217 */     Map<String, java.lang.reflect.Field> fieldMap = findInstanceFields(typeClass);
/* 117:    */     
/* 118:219 */     ArrayList<Field<T>> fields = new ArrayList(fieldMap.size());
/* 119:220 */     int i = 0;
/* 120:221 */     boolean annotated = false;
/* 121:222 */     for (java.lang.reflect.Field f : fieldMap.values()) {
/* 122:224 */       if (!exclusions.contains(f.getName())) {
/* 123:226 */         if (f.getAnnotation(Deprecated.class) != null)
/* 124:    */         {
/* 125:230 */           i++;
/* 126:    */         }
/* 127:    */         else
/* 128:    */         {
/* 129:234 */           Tag tag = (Tag)f.getAnnotation(Tag.class);
/* 130:    */           String name;
/* 131:    */           int fieldMapping;
/* 132:    */           String name;
/* 133:237 */           if (tag == null)
/* 134:    */           {
/* 135:241 */             if (annotated)
/* 136:    */             {
/* 137:243 */               String className = typeClass.getCanonicalName();
/* 138:244 */               String fieldName = f.getName();
/* 139:245 */               String message = String.format("%s#%s is not annotated with @Tag", new Object[] { className, fieldName });
/* 140:246 */               throw new RuntimeException(message);
/* 141:    */             }
/* 142:248 */             i++;int fieldMapping = i;
/* 143:    */             
/* 144:250 */             name = f.getName();
/* 145:    */           }
/* 146:    */           else
/* 147:    */           {
/* 148:256 */             if ((!annotated) && (!fields.isEmpty())) {
/* 149:261 */               throw new RuntimeException("When using annotation-based mapping, all fields must be annotated with @" + Tag.class.getSimpleName());
/* 150:    */             }
/* 151:263 */             annotated = true;
/* 152:264 */             fieldMapping = tag.value();
/* 153:266 */             if ((fieldMapping < 1) || (fieldMapping > 536870911)) {
/* 154:268 */               throw new IllegalArgumentException("Invalid tag number (value must be in range [1, 2^29-1]): " + fieldMapping + " on " + typeClass);
/* 155:    */             }
/* 156:271 */             name = tag.alias().isEmpty() ? f.getName() : tag.alias();
/* 157:    */           }
/* 158:275 */           Field<T> field = RuntimeFieldFactory.getFieldFactory(f.getType(), strategy).create(fieldMapping, name, f, strategy);
/* 159:    */           
/* 160:277 */           fields.add(field);
/* 161:    */         }
/* 162:    */       }
/* 163:    */     }
/* 164:281 */     return new RuntimeSchema(typeClass, fields, 
/* 165:282 */       RuntimeEnv.newInstantiator(typeClass));
/* 166:    */   }
/* 167:    */   
/* 168:    */   public static <T> RuntimeSchema<T> createFrom(Class<T> typeClass, Map<String, String> declaredFields, IdStrategy strategy)
/* 169:    */   {
/* 170:292 */     if ((typeClass.isInterface()) || 
/* 171:293 */       (Modifier.isAbstract(typeClass.getModifiers()))) {
/* 172:297 */       throw new RuntimeException("The root object can neither be an abstract class nor interface: \"" + typeClass.getName());
/* 173:    */     }
/* 174:301 */     ArrayList<Field<T>> fields = new ArrayList(declaredFields.size());
/* 175:302 */     int i = 0;
/* 176:303 */     for (Map.Entry<String, String> entry : declaredFields.entrySet())
/* 177:    */     {
/* 178:    */       try
/* 179:    */       {
/* 180:308 */         f = typeClass.getDeclaredField((String)entry.getKey());
/* 181:    */       }
/* 182:    */       catch (Exception e)
/* 183:    */       {
/* 184:    */         java.lang.reflect.Field f;
/* 185:313 */         throw new IllegalArgumentException("Exception on field: " + (String)entry.getKey(), e);
/* 186:    */       }
/* 187:    */       java.lang.reflect.Field f;
/* 188:316 */       int mod = f.getModifiers();
/* 189:317 */       if ((!Modifier.isStatic(mod)) && (!Modifier.isTransient(mod)) && (f.getAnnotation(Exclude.class) == null))
/* 190:    */       {
/* 191:320 */         Field<T> field = RuntimeFieldFactory.getFieldFactory(f.getType(), strategy).create(++i, (String)entry.getValue(), f, strategy);
/* 192:    */         
/* 193:322 */         fields.add(field);
/* 194:    */       }
/* 195:    */     }
/* 196:325 */     return new RuntimeSchema(typeClass, fields, RuntimeEnv.newInstantiator(typeClass));
/* 197:    */   }
/* 198:    */   
/* 199:    */   static Map<String, java.lang.reflect.Field> findInstanceFields(Class<?> typeClass)
/* 200:    */   {
/* 201:331 */     LinkedHashMap<String, java.lang.reflect.Field> fieldMap = new LinkedHashMap();
/* 202:332 */     fill(fieldMap, typeClass);
/* 203:333 */     return fieldMap;
/* 204:    */   }
/* 205:    */   
/* 206:    */   static void fill(Map<String, java.lang.reflect.Field> fieldMap, Class<?> typeClass)
/* 207:    */   {
/* 208:339 */     if (Object.class != typeClass.getSuperclass()) {
/* 209:340 */       fill(fieldMap, typeClass.getSuperclass());
/* 210:    */     }
/* 211:342 */     for (java.lang.reflect.Field f : typeClass.getDeclaredFields())
/* 212:    */     {
/* 213:344 */       int mod = f.getModifiers();
/* 214:345 */       if ((!Modifier.isStatic(mod)) && (!Modifier.isTransient(mod)) && (f.getAnnotation(Exclude.class) == null)) {
/* 215:346 */         fieldMap.put(f.getName(), f);
/* 216:    */       }
/* 217:    */     }
/* 218:    */   }
/* 219:    */   
/* 220:    */   public RuntimeSchema(Class<T> typeClass, Collection<Field<T>> fields, Constructor<T> constructor)
/* 221:    */   {
/* 222:354 */     this(typeClass, fields, new RuntimeEnv.DefaultInstantiator(constructor));
/* 223:    */   }
/* 224:    */   
/* 225:    */   public RuntimeSchema(Class<T> typeClass, Collection<Field<T>> fields, RuntimeEnv.Instantiator<T> instantiator)
/* 226:    */   {
/* 227:360 */     this.fieldMap = createFieldMap(fields);
/* 228:361 */     this.pipeSchema = new RuntimePipeSchema(this, this.fieldMap);
/* 229:362 */     this.instantiator = instantiator;
/* 230:363 */     this.typeClass = typeClass;
/* 231:    */   }
/* 232:    */   
/* 233:    */   private FieldMap<T> createFieldMap(Collection<Field<T>> fields)
/* 234:    */   {
/* 235:368 */     int lastFieldNumber = 0;
/* 236:369 */     for (Field<T> field : fields) {
/* 237:371 */       if (field.number > lastFieldNumber) {
/* 238:373 */         lastFieldNumber = field.number;
/* 239:    */       }
/* 240:    */     }
/* 241:376 */     if (preferHashFieldMap(fields, lastFieldNumber)) {
/* 242:378 */       return new HashFieldMap(fields);
/* 243:    */     }
/* 244:381 */     return new ArrayFieldMap(fields, lastFieldNumber);
/* 245:    */   }
/* 246:    */   
/* 247:    */   private boolean preferHashFieldMap(Collection<Field<T>> fields, int lastFieldNumber)
/* 248:    */   {
/* 249:386 */     return (lastFieldNumber > 100) && (lastFieldNumber >= 2 * fields.size());
/* 250:    */   }
/* 251:    */   
/* 252:    */   public Pipe.Schema<T> getPipeSchema()
/* 253:    */   {
/* 254:394 */     return this.pipeSchema;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public Field<T> getFieldByNumber(int n)
/* 258:    */   {
/* 259:400 */     return this.fieldMap.getFieldByNumber(n);
/* 260:    */   }
/* 261:    */   
/* 262:    */   public Field<T> getFieldByName(String fieldName)
/* 263:    */   {
/* 264:406 */     return this.fieldMap.getFieldByName(fieldName);
/* 265:    */   }
/* 266:    */   
/* 267:    */   public int getFieldCount()
/* 268:    */   {
/* 269:412 */     return this.fieldMap.getFieldCount();
/* 270:    */   }
/* 271:    */   
/* 272:    */   public List<Field<T>> getFields()
/* 273:    */   {
/* 274:418 */     return this.fieldMap.getFields();
/* 275:    */   }
/* 276:    */   
/* 277:    */   public Class<T> typeClass()
/* 278:    */   {
/* 279:424 */     return this.typeClass;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public String messageName()
/* 283:    */   {
/* 284:430 */     return this.typeClass.getSimpleName();
/* 285:    */   }
/* 286:    */   
/* 287:    */   public String messageFullName()
/* 288:    */   {
/* 289:436 */     return this.typeClass.getName();
/* 290:    */   }
/* 291:    */   
/* 292:    */   public String getFieldName(int number)
/* 293:    */   {
/* 294:443 */     Field<T> field = getFieldByNumber(number);
/* 295:444 */     return field == null ? null : field.name;
/* 296:    */   }
/* 297:    */   
/* 298:    */   public int getFieldNumber(String name)
/* 299:    */   {
/* 300:450 */     Field<T> field = getFieldByName(name);
/* 301:451 */     return field == null ? 0 : field.number;
/* 302:    */   }
/* 303:    */   
/* 304:    */   public final void mergeFrom(Input input, T message)
/* 305:    */     throws IOException
/* 306:    */   {
/* 307:457 */     for (int n = input.readFieldNumber(this); n != 0; n = input.readFieldNumber(this))
/* 308:    */     {
/* 309:459 */       Field<T> field = getFieldByNumber(n);
/* 310:460 */       if (field == null) {
/* 311:462 */         input.handleUnknownField(n, this);
/* 312:    */       } else {
/* 313:466 */         field.mergeFrom(input, message);
/* 314:    */       }
/* 315:    */     }
/* 316:    */   }
/* 317:    */   
/* 318:    */   public final void writeTo(Output output, T message)
/* 319:    */     throws IOException
/* 320:    */   {
/* 321:474 */     for (Field<T> f : getFields()) {
/* 322:475 */       f.writeTo(output, message);
/* 323:    */     }
/* 324:    */   }
/* 325:    */   
/* 326:    */   public boolean isInitialized(T message)
/* 327:    */   {
/* 328:484 */     return true;
/* 329:    */   }
/* 330:    */   
/* 331:    */   public T newMessage()
/* 332:    */   {
/* 333:490 */     return this.instantiator.newInstance();
/* 334:    */   }
/* 335:    */   
/* 336:    */   static <T> Pipe.Schema<T> resolvePipeSchema(Schema<T> schema, Class<? super T> clazz, boolean throwIfNone)
/* 337:    */   {
/* 338:500 */     if (Message.class.isAssignableFrom(clazz)) {
/* 339:    */       try
/* 340:    */       {
/* 341:505 */         Method m = clazz.getDeclaredMethod("getPipeSchema", new Class[0]);
/* 342:506 */         return (Pipe.Schema)m.invoke(null, new Object[0]);
/* 343:    */       }
/* 344:    */       catch (Exception localException) {}
/* 345:    */     }
/* 346:514 */     if (RuntimeSchema.class.isAssignableFrom(schema.getClass())) {
/* 347:515 */       return ((RuntimeSchema)schema).getPipeSchema();
/* 348:    */     }
/* 349:517 */     if (throwIfNone) {
/* 350:518 */       throw new RuntimeException("No pipe schema for: " + clazz);
/* 351:    */     }
/* 352:520 */     return null;
/* 353:    */   }
/* 354:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeSchema
 * JD-Core Version:    0.7.0.1
 */