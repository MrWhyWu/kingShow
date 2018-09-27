/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import java.io.ObjectInputStream;
/*   4:    */ import java.lang.reflect.Constructor;
/*   5:    */ import java.lang.reflect.Method;
/*   6:    */ import java.util.Properties;
/*   7:    */ 
/*   8:    */ public final class RuntimeEnv
/*   9:    */ {
/*  10:    */   public static final boolean ENUMS_BY_NAME;
/*  11:    */   public static final boolean AUTO_LOAD_POLYMORPHIC_CLASSES;
/*  12:    */   public static final boolean ALLOW_NULL_ARRAY_ELEMENT;
/*  13:    */   public static final boolean MORPH_NON_FINAL_POJOS;
/*  14:    */   public static final boolean MORPH_COLLECTION_INTERFACES;
/*  15:    */   public static final boolean MORPH_MAP_INTERFACES;
/*  16:    */   public static final boolean COLLECTION_SCHEMA_ON_REPEATED_FIELDS;
/*  17:    */   public static final boolean POJO_SCHEMA_ON_COLLECTION_FIELDS;
/*  18:    */   public static final boolean POJO_SCHEMA_ON_MAP_FIELDS;
/*  19:    */   public static final boolean USE_SUN_MISC_UNSAFE;
/*  20:    */   public static final boolean ALWAYS_USE_SUN_REFLECTION_FACTORY;
/*  21:    */   static final Method newInstanceFromObjectInputStream;
/*  22:    */   static final Constructor<Object> OBJECT_CONSTRUCTOR;
/*  23:    */   public static final IdStrategy ID_STRATEGY;
/*  24:    */   
/*  25:    */   static
/*  26:    */   {
/*  27:144 */     Constructor<Object> c = null;
/*  28:145 */     Class<?> reflectionFactoryClass = null;
/*  29:    */     try
/*  30:    */     {
/*  31:148 */       c = Object.class.getConstructor((Class[])null);
/*  32:    */       
/*  33:    */ 
/*  34:151 */       reflectionFactoryClass = Thread.currentThread().getContextClassLoader().loadClass("sun.reflect.ReflectionFactory");
/*  35:    */     }
/*  36:    */     catch (Exception localException1) {}
/*  37:158 */     OBJECT_CONSTRUCTOR = (c != null) && (reflectionFactoryClass != null) ? c : null;
/*  38:    */     
/*  39:    */ 
/*  40:161 */     newInstanceFromObjectInputStream = OBJECT_CONSTRUCTOR == null ? getMethodNewInstanceFromObjectInputStream() : null;
/*  41:164 */     if (newInstanceFromObjectInputStream != null) {
/*  42:165 */       newInstanceFromObjectInputStream.setAccessible(true);
/*  43:    */     }
/*  44:168 */     Properties props = OBJECT_CONSTRUCTOR == null ? new Properties() : System.getProperties();
/*  45:    */     
/*  46:170 */     ENUMS_BY_NAME = Boolean.parseBoolean(props.getProperty("protostuff.runtime.enums_by_name", "false"));
/*  47:    */     
/*  48:    */ 
/*  49:173 */     AUTO_LOAD_POLYMORPHIC_CLASSES = Boolean.parseBoolean(props.getProperty("protostuff.runtime.auto_load_polymorphic_classes", "true"));
/*  50:    */     
/*  51:    */ 
/*  52:176 */     ALLOW_NULL_ARRAY_ELEMENT = Boolean.parseBoolean(props.getProperty("protostuff.runtime.allow_null_array_element", "false"));
/*  53:    */     
/*  54:    */ 
/*  55:179 */     MORPH_NON_FINAL_POJOS = Boolean.parseBoolean(props.getProperty("protostuff.runtime.morph_non_final_pojos", "false"));
/*  56:    */     
/*  57:    */ 
/*  58:182 */     MORPH_COLLECTION_INTERFACES = Boolean.parseBoolean(props.getProperty("protostuff.runtime.morph_collection_interfaces", "false"));
/*  59:    */     
/*  60:    */ 
/*  61:185 */     MORPH_MAP_INTERFACES = Boolean.parseBoolean(props.getProperty("protostuff.runtime.morph_map_interfaces", "false"));
/*  62:    */     
/*  63:    */ 
/*  64:188 */     COLLECTION_SCHEMA_ON_REPEATED_FIELDS = Boolean.parseBoolean(props.getProperty("protostuff.runtime.collection_schema_on_repeated_fields", "false"));
/*  65:    */     
/*  66:    */ 
/*  67:    */ 
/*  68:192 */     POJO_SCHEMA_ON_COLLECTION_FIELDS = Boolean.parseBoolean(props.getProperty("protostuff.runtime.pojo_schema_on_collection_fields", "false"));
/*  69:    */     
/*  70:    */ 
/*  71:    */ 
/*  72:196 */     POJO_SCHEMA_ON_MAP_FIELDS = Boolean.parseBoolean(props.getProperty("protostuff.runtime.pojo_schema_on_map_fields", "false"));
/*  73:    */     
/*  74:    */ 
/*  75:    */ 
/*  76:    */ 
/*  77:    */ 
/*  78:202 */     USE_SUN_MISC_UNSAFE = (OBJECT_CONSTRUCTOR != null) && (Boolean.parseBoolean(props.getProperty("protostuff.runtime.use_sun_misc_unsafe", "true")));
/*  79:    */     
/*  80:    */ 
/*  81:    */ 
/*  82:206 */     ALWAYS_USE_SUN_REFLECTION_FACTORY = (OBJECT_CONSTRUCTOR != null) && (Boolean.parseBoolean(props.getProperty("protostuff.runtime.always_use_sun_reflection_factory", "false")));
/*  83:    */     
/*  84:    */ 
/*  85:    */ 
/*  86:    */ 
/*  87:211 */     String factoryProp = props.getProperty("protostuff.runtime.id_strategy_factory");
/*  88:212 */     if (factoryProp == null)
/*  89:    */     {
/*  90:213 */       ID_STRATEGY = new DefaultIdStrategy();
/*  91:    */     }
/*  92:    */     else
/*  93:    */     {
/*  94:    */       try
/*  95:    */       {
/*  96:220 */         factory = (IdStrategy.Factory)loadClass(factoryProp).newInstance();
/*  97:    */       }
/*  98:    */       catch (Exception e)
/*  99:    */       {
/* 100:    */         IdStrategy.Factory factory;
/* 101:224 */         throw new RuntimeException(e);
/* 102:    */       }
/* 103:    */       IdStrategy.Factory factory;
/* 104:227 */       ID_STRATEGY = factory.create();
/* 105:228 */       factory.postCreate();
/* 106:    */     }
/* 107:    */   }
/* 108:    */   
/* 109:    */   private static Method getMethodNewInstanceFromObjectInputStream()
/* 110:    */   {
/* 111:    */     try
/* 112:    */     {
/* 113:236 */       return ObjectInputStream.class.getDeclaredMethod("newInstance", new Class[] { Class.class, Class.class });
/* 114:    */     }
/* 115:    */     catch (Exception e) {}
/* 116:241 */     return null;
/* 117:    */   }
/* 118:    */   
/* 119:    */   static <T> Class<T> loadClass(String className)
/* 120:    */   {
/* 121:    */     try
/* 122:    */     {
/* 123:250 */       return 
/* 124:251 */         Thread.currentThread().getContextClassLoader().loadClass(className);
/* 125:    */     }
/* 126:    */     catch (ClassNotFoundException e)
/* 127:    */     {
/* 128:    */       try
/* 129:    */       {
/* 130:256 */         return Class.forName(className);
/* 131:    */       }
/* 132:    */       catch (ClassNotFoundException e1)
/* 133:    */       {
/* 134:258 */         throw new RuntimeException(e);
/* 135:    */       }
/* 136:    */     }
/* 137:    */   }
/* 138:    */   
/* 139:    */   public static <T> Instantiator<T> newInstantiator(Class<T> clazz)
/* 140:    */   {
/* 141:268 */     Constructor<T> constructor = getConstructor(clazz);
/* 142:269 */     if (constructor == null)
/* 143:    */     {
/* 144:272 */       if (newInstanceFromObjectInputStream == null) {
/* 145:273 */         throw new RuntimeException("Could not resolve constructor for " + clazz);
/* 146:    */       }
/* 147:276 */       return new Android2Instantiator(clazz);
/* 148:    */     }
/* 149:279 */     return new DefaultInstantiator(constructor);
/* 150:    */   }
/* 151:    */   
/* 152:    */   private static <T> Constructor<T> getConstructor(Class<T> clazz)
/* 153:    */   {
/* 154:284 */     if (ALWAYS_USE_SUN_REFLECTION_FACTORY) {
/* 155:285 */       return OnDemandSunReflectionFactory.getConstructor(clazz, OBJECT_CONSTRUCTOR);
/* 156:    */     }
/* 157:    */     try
/* 158:    */     {
/* 159:290 */       return clazz.getDeclaredConstructor((Class[])null);
/* 160:    */     }
/* 161:    */     catch (SecurityException e)
/* 162:    */     {
/* 163:294 */       return OBJECT_CONSTRUCTOR == null ? null : 
/* 164:295 */         OnDemandSunReflectionFactory.getConstructor(clazz, OBJECT_CONSTRUCTOR);
/* 165:    */     }
/* 166:    */     catch (NoSuchMethodException e)
/* 167:    */     {
/* 168:300 */       if (OBJECT_CONSTRUCTOR == null) {
/* 169:300 */         tmpTernaryOp = null;
/* 170:    */       }
/* 171:    */     }
/* 172:300 */     return 
/* 173:301 */       OnDemandSunReflectionFactory.getConstructor(clazz, OBJECT_CONSTRUCTOR);
/* 174:    */   }
/* 175:    */   
/* 176:    */   public static abstract class Instantiator<T>
/* 177:    */   {
/* 178:    */     public abstract T newInstance();
/* 179:    */   }
/* 180:    */   
/* 181:    */   static final class DefaultInstantiator<T>
/* 182:    */     extends RuntimeEnv.Instantiator<T>
/* 183:    */   {
/* 184:    */     final Constructor<T> constructor;
/* 185:    */     
/* 186:    */     DefaultInstantiator(Constructor<T> constructor)
/* 187:    */     {
/* 188:325 */       this.constructor = constructor;
/* 189:326 */       constructor.setAccessible(true);
/* 190:    */     }
/* 191:    */     
/* 192:    */     public T newInstance()
/* 193:    */     {
/* 194:    */       try
/* 195:    */       {
/* 196:334 */         return this.constructor.newInstance((Object[])null);
/* 197:    */       }
/* 198:    */       catch (Exception e)
/* 199:    */       {
/* 200:338 */         throw new RuntimeException(e);
/* 201:    */       }
/* 202:    */     }
/* 203:    */   }
/* 204:    */   
/* 205:    */   static final class Android2Instantiator<T>
/* 206:    */     extends RuntimeEnv.Instantiator<T>
/* 207:    */   {
/* 208:    */     final Class<T> clazz;
/* 209:    */     
/* 210:    */     Android2Instantiator(Class<T> clazz)
/* 211:    */     {
/* 212:350 */       this.clazz = clazz;
/* 213:    */     }
/* 214:    */     
/* 215:    */     public T newInstance()
/* 216:    */     {
/* 217:    */       try
/* 218:    */       {
/* 219:359 */         return RuntimeEnv.newInstanceFromObjectInputStream.invoke(null, new Object[] { this.clazz, Object.class });
/* 220:    */       }
/* 221:    */       catch (Exception e)
/* 222:    */       {
/* 223:364 */         throw new RuntimeException(e);
/* 224:    */       }
/* 225:    */     }
/* 226:    */   }
/* 227:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeEnv
 * JD-Core Version:    0.7.0.1
 */