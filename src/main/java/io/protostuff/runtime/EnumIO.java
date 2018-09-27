/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.CollectionSchema.MessageFactory;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.MapSchema.MessageFactory;
/*   6:    */ import io.protostuff.Output;
/*   7:    */ import io.protostuff.Pipe;
/*   8:    */ import io.protostuff.Tag;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.lang.reflect.Field;
/*  11:    */ import java.util.Collection;
/*  12:    */ import java.util.EnumMap;
/*  13:    */ import java.util.EnumSet;
/*  14:    */ import java.util.HashMap;
/*  15:    */ import java.util.Map;
/*  16:    */ 
/*  17:    */ public abstract class EnumIO<E extends Enum<E>>
/*  18:    */   implements PolymorphicSchema.Factory
/*  19:    */ {
/*  20:    */   private static final Field __keyTypeFromEnumMap;
/*  21:    */   private static final Field __elementTypeFromEnumSet;
/*  22:    */   public final Class<E> enumClass;
/*  23:    */   public final IdStrategy strategy;
/*  24:    */   public final ArraySchemas.Base genericElementSchema;
/*  25:    */   private volatile CollectionSchema.MessageFactory enumSetFactory;
/*  26:    */   private volatile MapSchema.MessageFactory enumMapFactory;
/*  27:    */   private final String[] alias;
/*  28:    */   private final int[] tag;
/*  29:    */   private final Map<String, E> valueByAliasMap;
/*  30:    */   private final Map<Integer, E> valueByTagMap;
/*  31:    */   
/*  32:    */   static
/*  33:    */   {
/*  34: 50 */     boolean success = false;
/*  35: 51 */     Field keyTypeFromMap = null;Field valueTypeFromSet = null;
/*  36:    */     try
/*  37:    */     {
/*  38: 54 */       keyTypeFromMap = EnumMap.class.getDeclaredField("keyType");
/*  39: 55 */       keyTypeFromMap.setAccessible(true);
/*  40: 56 */       valueTypeFromSet = EnumSet.class.getDeclaredField("elementType");
/*  41: 57 */       valueTypeFromSet.setAccessible(true);
/*  42: 58 */       success = true;
/*  43:    */     }
/*  44:    */     catch (Exception localException) {}
/*  45: 65 */     __keyTypeFromEnumMap = success ? keyTypeFromMap : null;
/*  46: 66 */     __elementTypeFromEnumSet = success ? valueTypeFromSet : null;
/*  47:    */   }
/*  48:    */   
/*  49:    */   static Class<?> getKeyTypeFromEnumMap(Object enumMap)
/*  50:    */   {
/*  51: 74 */     if (__keyTypeFromEnumMap == null) {
/*  52: 76 */       throw new RuntimeException("Could not access (reflection) the private field *keyType* (enumClass) from: class java.util.EnumMap");
/*  53:    */     }
/*  54:    */     try
/*  55:    */     {
/*  56: 83 */       return (Class)__keyTypeFromEnumMap.get(enumMap);
/*  57:    */     }
/*  58:    */     catch (Exception e)
/*  59:    */     {
/*  60: 87 */       throw new RuntimeException(e);
/*  61:    */     }
/*  62:    */   }
/*  63:    */   
/*  64:    */   static Class<?> getElementTypeFromEnumSet(Object enumSet)
/*  65:    */   {
/*  66: 96 */     if (__elementTypeFromEnumSet == null) {
/*  67: 98 */       throw new RuntimeException("Could not access (reflection) the private field *elementType* (enumClass) from: class java.util.EnumSet");
/*  68:    */     }
/*  69:    */     try
/*  70:    */     {
/*  71:105 */       return (Class)__elementTypeFromEnumSet.get(enumSet);
/*  72:    */     }
/*  73:    */     catch (Exception e)
/*  74:    */     {
/*  75:109 */       throw new RuntimeException(e);
/*  76:    */     }
/*  77:    */   }
/*  78:    */   
/*  79:    */   static EnumIO<? extends Enum<?>> newEnumIO(Class<?> enumClass, IdStrategy strategy)
/*  80:    */   {
/*  81:116 */     return 0 == (0x1 & strategy.flags) ? new ByNumber(enumClass, strategy) : new ByName(enumClass, strategy);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void writeTo(Output output, int number, boolean repeated, Enum<?> e)
/*  85:    */     throws IOException
/*  86:    */   {
/*  87:126 */     if (0 == (0x1 & this.strategy.flags)) {
/*  88:127 */       output.writeEnum(number, getTag(e), repeated);
/*  89:    */     } else {
/*  90:129 */       output.writeString(number, getAlias(e), repeated);
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   public static void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated, IdStrategy strategy)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:138 */     if (0 == (0x1 & strategy.flags)) {
/*  98:139 */       output.writeEnum(number, input.readEnum(), repeated);
/*  99:    */     } else {
/* 100:141 */       input.transferByteRangeTo(output, true, number, repeated);
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   private static <E extends Enum<E>> CollectionSchema.MessageFactory newEnumSetFactory(EnumIO<E> eio)
/* 105:    */   {
/* 106:147 */     new CollectionSchema.MessageFactory()
/* 107:    */     {
/* 108:    */       public <V> Collection<V> newMessage()
/* 109:    */       {
/* 110:153 */         return this.val$eio.newEnumSet();
/* 111:    */       }
/* 112:    */       
/* 113:    */       public Class<?> typeClass()
/* 114:    */       {
/* 115:159 */         return EnumSet.class;
/* 116:    */       }
/* 117:    */     };
/* 118:    */   }
/* 119:    */   
/* 120:    */   private static <E extends Enum<E>> MapSchema.MessageFactory newEnumMapFactory(EnumIO<E> eio)
/* 121:    */   {
/* 122:167 */     new MapSchema.MessageFactory()
/* 123:    */     {
/* 124:    */       public <K, V> Map<K, V> newMessage()
/* 125:    */       {
/* 126:173 */         return this.val$eio.newEnumMap();
/* 127:    */       }
/* 128:    */       
/* 129:    */       public Class<?> typeClass()
/* 130:    */       {
/* 131:179 */         return EnumMap.class;
/* 132:    */       }
/* 133:    */     };
/* 134:    */   }
/* 135:    */   
/* 136:    */   public EnumIO(Class<E> enumClass, IdStrategy strategy)
/* 137:    */   {
/* 138:202 */     this.enumClass = enumClass;
/* 139:203 */     this.strategy = strategy;
/* 140:204 */     this.genericElementSchema = new ArraySchemas.EnumArray(strategy, null, this);
/* 141:    */     
/* 142:206 */     Field[] fields = enumClass.getFields();
/* 143:207 */     int n = fields.length;
/* 144:208 */     this.alias = new String[n];
/* 145:209 */     this.tag = new int[n];
/* 146:210 */     this.valueByAliasMap = new HashMap(n * 2);
/* 147:211 */     this.valueByTagMap = new HashMap(n * 2);
/* 148:212 */     for (E instance : (Enum[])enumClass.getEnumConstants())
/* 149:    */     {
/* 150:214 */       int ordinal = instance.ordinal();
/* 151:    */       try
/* 152:    */       {
/* 153:217 */         Field field = enumClass.getField(instance.name());
/* 154:218 */         if (field.isAnnotationPresent(Tag.class))
/* 155:    */         {
/* 156:220 */           Tag annotation = (Tag)field.getAnnotation(Tag.class);
/* 157:221 */           this.tag[ordinal] = annotation.value();
/* 158:222 */           this.alias[ordinal] = annotation.alias();
/* 159:223 */           this.valueByTagMap.put(Integer.valueOf(annotation.value()), instance);
/* 160:224 */           this.valueByAliasMap.put(annotation.alias(), instance);
/* 161:    */         }
/* 162:    */         else
/* 163:    */         {
/* 164:228 */           this.tag[ordinal] = ordinal;
/* 165:229 */           this.alias[ordinal] = field.getName();
/* 166:230 */           this.valueByTagMap.put(Integer.valueOf(ordinal), instance);
/* 167:231 */           this.valueByAliasMap.put(field.getName(), instance);
/* 168:    */         }
/* 169:    */       }
/* 170:    */       catch (NoSuchFieldException e)
/* 171:    */       {
/* 172:236 */         throw new IllegalStateException(e);
/* 173:    */       }
/* 174:    */     }
/* 175:    */   }
/* 176:    */   
/* 177:    */   public PolymorphicSchema newSchema(Class<?> typeClass, IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 178:    */   {
/* 179:245 */     return new ArraySchemas.EnumArray(strategy, handler, this);
/* 180:    */   }
/* 181:    */   
/* 182:    */   public int getTag(Enum<?> element)
/* 183:    */   {
/* 184:250 */     return this.tag[element.ordinal()];
/* 185:    */   }
/* 186:    */   
/* 187:    */   public String getAlias(Enum<?> element)
/* 188:    */   {
/* 189:255 */     return this.alias[element.ordinal()];
/* 190:    */   }
/* 191:    */   
/* 192:    */   public E getByTag(int tag)
/* 193:    */   {
/* 194:260 */     return (Enum)this.valueByTagMap.get(Integer.valueOf(tag));
/* 195:    */   }
/* 196:    */   
/* 197:    */   public E getByAlias(String alias)
/* 198:    */   {
/* 199:265 */     return (Enum)this.valueByAliasMap.get(alias);
/* 200:    */   }
/* 201:    */   
/* 202:    */   public CollectionSchema.MessageFactory getEnumSetFactory()
/* 203:    */   {
/* 204:273 */     CollectionSchema.MessageFactory enumSetFactory = this.enumSetFactory;
/* 205:274 */     if (enumSetFactory == null) {
/* 206:276 */       synchronized (this)
/* 207:    */       {
/* 208:278 */         if ((enumSetFactory = this.enumSetFactory) == null) {
/* 209:279 */           this.enumSetFactory = (enumSetFactory = newEnumSetFactory(this));
/* 210:    */         }
/* 211:    */       }
/* 212:    */     }
/* 213:282 */     return enumSetFactory;
/* 214:    */   }
/* 215:    */   
/* 216:    */   public MapSchema.MessageFactory getEnumMapFactory()
/* 217:    */   {
/* 218:290 */     MapSchema.MessageFactory enumMapFactory = this.enumMapFactory;
/* 219:291 */     if (enumMapFactory == null) {
/* 220:293 */       synchronized (this)
/* 221:    */       {
/* 222:295 */         if ((enumMapFactory = this.enumMapFactory) == null) {
/* 223:296 */           this.enumMapFactory = (enumMapFactory = newEnumMapFactory(this));
/* 224:    */         }
/* 225:    */       }
/* 226:    */     }
/* 227:299 */     return enumMapFactory;
/* 228:    */   }
/* 229:    */   
/* 230:    */   public EnumSet<E> newEnumSet()
/* 231:    */   {
/* 232:307 */     return EnumSet.noneOf(this.enumClass);
/* 233:    */   }
/* 234:    */   
/* 235:    */   public <V> EnumMap<E, V> newEnumMap()
/* 236:    */   {
/* 237:315 */     return new EnumMap(this.enumClass);
/* 238:    */   }
/* 239:    */   
/* 240:    */   public abstract E readFrom(Input paramInput)
/* 241:    */     throws IOException;
/* 242:    */   
/* 243:    */   public static final class ByName<E extends Enum<E>>
/* 244:    */     extends EnumIO<E>
/* 245:    */   {
/* 246:    */     public ByName(Class<E> enumClass, IdStrategy strategy)
/* 247:    */     {
/* 248:330 */       super(strategy);
/* 249:    */     }
/* 250:    */     
/* 251:    */     public E readFrom(Input input)
/* 252:    */       throws IOException
/* 253:    */     {
/* 254:336 */       String alias = input.readString();
/* 255:337 */       return getByAlias(alias);
/* 256:    */     }
/* 257:    */   }
/* 258:    */   
/* 259:    */   public static final class ByNumber<E extends Enum<E>>
/* 260:    */     extends EnumIO<E>
/* 261:    */   {
/* 262:    */     public ByNumber(Class<E> enumClass, IdStrategy strategy)
/* 263:    */     {
/* 264:348 */       super(strategy);
/* 265:    */     }
/* 266:    */     
/* 267:    */     public E readFrom(Input input)
/* 268:    */       throws IOException
/* 269:    */     {
/* 270:354 */       int tag = input.readEnum();
/* 271:355 */       return getByTag(tag);
/* 272:    */     }
/* 273:    */   }
/* 274:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.EnumIO
 * JD-Core Version:    0.7.0.1
 */