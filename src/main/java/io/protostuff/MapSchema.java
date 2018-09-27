/*   1:    */ package io.protostuff;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Hashtable;
/*   6:    */ import java.util.IdentityHashMap;
/*   7:    */ import java.util.LinkedHashMap;
/*   8:    */ import java.util.Map;
/*   9:    */ import java.util.Map.Entry;
/*  10:    */ import java.util.Properties;
/*  11:    */ import java.util.TreeMap;
/*  12:    */ import java.util.WeakHashMap;
/*  13:    */ import java.util.concurrent.ConcurrentHashMap;
/*  14:    */ import java.util.concurrent.ConcurrentSkipListMap;
/*  15:    */ 
/*  16:    */ public abstract class MapSchema<K, V>
/*  17:    */   implements Schema<Map<K, V>>
/*  18:    */ {
/*  19:    */   public static final String FIELD_NAME_ENTRY = "e";
/*  20:    */   public static final String FIELD_NAME_KEY = "k";
/*  21:    */   public static final String FIELD_NAME_VALUE = "v";
/*  22:    */   public final MessageFactory messageFactory;
/*  23:    */   
/*  24:    */   public static abstract interface MessageFactory
/*  25:    */   {
/*  26:    */     public abstract <K, V> Map<K, V> newMessage();
/*  27:    */     
/*  28:    */     public abstract Class<?> typeClass();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public static abstract enum MessageFactories
/*  32:    */     implements MapSchema.MessageFactory
/*  33:    */   {
/*  34: 57 */     Map(HashMap.class),  SortedMap(TreeMap.class),  NavigableMap(TreeMap.class),  HashMap(HashMap.class),  LinkedHashMap(LinkedHashMap.class),  TreeMap(TreeMap.class),  WeakHashMap(WeakHashMap.class),  IdentityHashMap(IdentityHashMap.class),  Hashtable(Hashtable.class),  ConcurrentMap(ConcurrentHashMap.class),  ConcurrentHashMap(ConcurrentHashMap.class),  ConcurrentNavigableMap(ConcurrentSkipListMap.class),  ConcurrentSkipListMap(ConcurrentSkipListMap.class),  Properties(Properties.class);
/*  35:    */     
/*  36:    */     public final Class<?> typeClass;
/*  37:    */     
/*  38:    */     private MessageFactories(Class<?> typeClass)
/*  39:    */     {
/*  40:179 */       this.typeClass = typeClass;
/*  41:    */     }
/*  42:    */     
/*  43:    */     public Class<?> typeClass()
/*  44:    */     {
/*  45:185 */       return this.typeClass;
/*  46:    */     }
/*  47:    */     
/*  48:    */     public static MessageFactories getFactory(Class<? extends Map<?, ?>> mapType)
/*  49:    */     {
/*  50:193 */       return mapType.getName().startsWith("java.util") ? 
/*  51:194 */         valueOf(mapType.getSimpleName()) : null;
/*  52:    */     }
/*  53:    */     
/*  54:    */     public static MessageFactories getFactory(String name)
/*  55:    */     {
/*  56:203 */       return valueOf(name);
/*  57:    */     }
/*  58:    */   }
/*  59:    */   
/*  60:    */   public MapSchema()
/*  61:    */   {
/*  62:227 */     this(MessageFactories.HashMap);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public MapSchema(MessageFactory messageFactory)
/*  66:    */   {
/*  67:232 */     this.messageFactory = messageFactory;
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected abstract K readKeyFrom(Input paramInput, MapWrapper<K, V> paramMapWrapper)
/*  71:    */     throws IOException;
/*  72:    */   
/*  73:    */   protected abstract void putValueFrom(Input paramInput, MapWrapper<K, V> paramMapWrapper, K paramK)
/*  74:    */     throws IOException;
/*  75:    */   
/*  76:    */   protected abstract void writeKeyTo(Output paramOutput, int paramInt, K paramK, boolean paramBoolean)
/*  77:    */     throws IOException;
/*  78:    */   
/*  79:    */   protected abstract void writeValueTo(Output paramOutput, int paramInt, V paramV, boolean paramBoolean)
/*  80:    */     throws IOException;
/*  81:    */   
/*  82:    */   protected abstract void transferKey(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/*  83:    */     throws IOException;
/*  84:    */   
/*  85:    */   protected abstract void transferValue(Pipe paramPipe, Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/*  86:    */     throws IOException;
/*  87:    */   
/*  88:    */   public final String getFieldName(int number)
/*  89:    */   {
/*  90:276 */     return number == 1 ? "e" : null;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public final int getFieldNumber(String name)
/*  94:    */   {
/*  95:282 */     return (name.length() == 1) && (name.charAt(0) == 'e') ? 1 : 0;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public final boolean isInitialized(Map<K, V> map)
/*  99:    */   {
/* 100:288 */     return true;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public final String messageFullName()
/* 104:    */   {
/* 105:294 */     return Map.class.getName();
/* 106:    */   }
/* 107:    */   
/* 108:    */   public final String messageName()
/* 109:    */   {
/* 110:300 */     return Map.class.getSimpleName();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public final Class<? super Map<K, V>> typeClass()
/* 114:    */   {
/* 115:306 */     return Map.class;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public final Map<K, V> newMessage()
/* 119:    */   {
/* 120:312 */     return this.messageFactory.newMessage();
/* 121:    */   }
/* 122:    */   
/* 123:    */   public final void mergeFrom(Input input, Map<K, V> map)
/* 124:    */     throws IOException
/* 125:    */   {
/* 126:318 */     MapWrapper<K, V> entry = null;
/* 127:319 */     for (int number = input.readFieldNumber(this);; number = input.readFieldNumber(this)) {
/* 128:321 */       switch (number)
/* 129:    */       {
/* 130:    */       case 0: 
/* 131:326 */         return;
/* 132:    */       case 1: 
/* 133:328 */         if (entry == null) {
/* 134:331 */           entry = new MapWrapper(map);
/* 135:    */         }
/* 136:334 */         if (entry != input.mergeObject(entry, this.entrySchema)) {
/* 137:340 */           throw new IllegalStateException("A Map.Entry will always be unique, hence it cannot be a reference obtained from " + input.getClass().getName());
/* 138:    */         }
/* 139:    */         break;
/* 140:    */       default: 
/* 141:344 */         throw new ProtostuffException("The map was incorrectly serialized.");
/* 142:    */       }
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public final void writeTo(Output output, Map<K, V> map)
/* 147:    */     throws IOException
/* 148:    */   {
/* 149:352 */     for (Map.Entry<K, V> entry : map.entrySet()) {
/* 150:355 */       output.writeObject(1, entry, this.entrySchema, true);
/* 151:    */     }
/* 152:    */   }
/* 153:    */   
/* 154:362 */   public final Pipe.Schema<Map<K, V>> pipeSchema = new Pipe.Schema(this)
/* 155:    */   {
/* 156:    */     protected void transfer(Pipe pipe, Input input, Output output)
/* 157:    */       throws IOException
/* 158:    */     {
/* 159:368 */       int number = input.readFieldNumber(MapSchema.this);
/* 160:369 */       for (;; number = input.readFieldNumber(MapSchema.this)) {
/* 161:371 */         switch (number)
/* 162:    */         {
/* 163:    */         case 0: 
/* 164:374 */           return;
/* 165:    */         case 1: 
/* 166:376 */           output.writeObject(number, pipe, MapSchema.this.entryPipeSchema, true);
/* 167:377 */           break;
/* 168:    */         default: 
/* 169:379 */           throw new ProtostuffException("The map was incorrectly serialized.");
/* 170:    */         }
/* 171:    */       }
/* 172:    */     }
/* 173:    */   };
/* 174:387 */   private final Schema<Map.Entry<K, V>> entrySchema = new Schema()
/* 175:    */   {
/* 176:    */     public final String getFieldName(int number)
/* 177:    */     {
/* 178:393 */       switch (number)
/* 179:    */       {
/* 180:    */       case 1: 
/* 181:396 */         return "k";
/* 182:    */       case 2: 
/* 183:398 */         return "v";
/* 184:    */       }
/* 185:400 */       return null;
/* 186:    */     }
/* 187:    */     
/* 188:    */     public final int getFieldNumber(String name)
/* 189:    */     {
/* 190:407 */       if (name.length() != 1) {
/* 191:408 */         return 0;
/* 192:    */       }
/* 193:410 */       switch (name.charAt(0))
/* 194:    */       {
/* 195:    */       case 'k': 
/* 196:413 */         return 1;
/* 197:    */       case 'v': 
/* 198:415 */         return 2;
/* 199:    */       }
/* 200:417 */       return 0;
/* 201:    */     }
/* 202:    */     
/* 203:    */     public boolean isInitialized(Map.Entry<K, V> message)
/* 204:    */     {
/* 205:424 */       return true;
/* 206:    */     }
/* 207:    */     
/* 208:    */     public String messageFullName()
/* 209:    */     {
/* 210:430 */       return Map.Entry.class.getName();
/* 211:    */     }
/* 212:    */     
/* 213:    */     public String messageName()
/* 214:    */     {
/* 215:436 */       return Map.Entry.class.getSimpleName();
/* 216:    */     }
/* 217:    */     
/* 218:    */     public Map.Entry<K, V> newMessage()
/* 219:    */     {
/* 220:442 */       throw new UnsupportedOperationException();
/* 221:    */     }
/* 222:    */     
/* 223:    */     public Class<? super Map.Entry<K, V>> typeClass()
/* 224:    */     {
/* 225:448 */       return Map.Entry.class;
/* 226:    */     }
/* 227:    */     
/* 228:    */     public void mergeFrom(Input input, Map.Entry<K, V> message)
/* 229:    */       throws IOException
/* 230:    */     {
/* 231:455 */       MapSchema.MapWrapper<K, V> wrapper = (MapSchema.MapWrapper)message;
/* 232:    */       
/* 233:457 */       K key = null;
/* 234:458 */       boolean valueRetrieved = false;
/* 235:459 */       int number = input.readFieldNumber(this);
/* 236:460 */       for (;; number = input.readFieldNumber(this)) {
/* 237:462 */         switch (number)
/* 238:    */         {
/* 239:    */         case 0: 
/* 240:465 */           if (key == null) {
/* 241:468 */             wrapper.map.put(null, valueRetrieved ? wrapper.value : null);
/* 242:470 */           } else if (!valueRetrieved) {
/* 243:473 */             wrapper.map.put(key, null);
/* 244:    */           }
/* 245:475 */           return;
/* 246:    */         case 1: 
/* 247:477 */           if (key != null) {
/* 248:479 */             throw new ProtostuffException("The map was incorrectly serialized.");
/* 249:    */           }
/* 250:482 */           key = MapSchema.this.readKeyFrom(input, wrapper);
/* 251:483 */           if ((!$assertionsDisabled) && (key == null)) {
/* 252:483 */             throw new AssertionError();
/* 253:    */           }
/* 254:    */           break;
/* 255:    */         case 2: 
/* 256:486 */           if (valueRetrieved) {
/* 257:488 */             throw new ProtostuffException("The map was incorrectly serialized.");
/* 258:    */           }
/* 259:491 */           valueRetrieved = true;
/* 260:    */           
/* 261:493 */           MapSchema.this.putValueFrom(input, wrapper, key);
/* 262:494 */           break;
/* 263:    */         default: 
/* 264:496 */           throw new ProtostuffException("The map was incorrectly serialized.");
/* 265:    */         }
/* 266:    */       }
/* 267:    */     }
/* 268:    */     
/* 269:    */     public void writeTo(Output output, Map.Entry<K, V> message)
/* 270:    */       throws IOException
/* 271:    */     {
/* 272:505 */       if (message.getKey() != null) {
/* 273:506 */         MapSchema.this.writeKeyTo(output, 1, message.getKey(), false);
/* 274:    */       }
/* 275:508 */       if (message.getValue() != null) {
/* 276:509 */         MapSchema.this.writeValueTo(output, 2, message.getValue(), false);
/* 277:    */       }
/* 278:    */     }
/* 279:    */   };
/* 280:514 */   private final Pipe.Schema<Map.Entry<K, V>> entryPipeSchema = new Pipe.Schema(this.entrySchema)
/* 281:    */   {
/* 282:    */     protected void transfer(Pipe pipe, Input input, Output output)
/* 283:    */       throws IOException
/* 284:    */     {
/* 285:521 */       int number = input.readFieldNumber(MapSchema.this.entrySchema);
/* 286:522 */       for (;; number = input.readFieldNumber(MapSchema.this.entrySchema)) {
/* 287:524 */         switch (number)
/* 288:    */         {
/* 289:    */         case 0: 
/* 290:527 */           return;
/* 291:    */         case 1: 
/* 292:529 */           MapSchema.this.transferKey(pipe, input, output, 1, false);
/* 293:530 */           break;
/* 294:    */         case 2: 
/* 295:532 */           MapSchema.this.transferValue(pipe, input, output, 2, false);
/* 296:533 */           break;
/* 297:    */         default: 
/* 298:535 */           throw new ProtostuffException("The map was incorrectly serialized.");
/* 299:    */         }
/* 300:    */       }
/* 301:    */     }
/* 302:    */   };
/* 303:    */   
/* 304:    */   public static final class MapWrapper<K, V>
/* 305:    */     implements Map.Entry<K, V>
/* 306:    */   {
/* 307:    */     final Map<K, V> map;
/* 308:    */     V value;
/* 309:    */     
/* 310:    */     MapWrapper(Map<K, V> map)
/* 311:    */     {
/* 312:561 */       this.map = map;
/* 313:    */     }
/* 314:    */     
/* 315:    */     public K getKey()
/* 316:    */     {
/* 317:570 */       return null;
/* 318:    */     }
/* 319:    */     
/* 320:    */     public V getValue()
/* 321:    */     {
/* 322:579 */       return this.value;
/* 323:    */     }
/* 324:    */     
/* 325:    */     public V setValue(V value)
/* 326:    */     {
/* 327:589 */       V last = this.value;
/* 328:590 */       this.value = value;
/* 329:591 */       return last;
/* 330:    */     }
/* 331:    */     
/* 332:    */     public void put(K key, V value)
/* 333:    */     {
/* 334:601 */       if (key == null) {
/* 335:602 */         this.value = value;
/* 336:    */       } else {
/* 337:604 */         this.map.put(key, value);
/* 338:    */       }
/* 339:    */     }
/* 340:    */   }
/* 341:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.MapSchema
 * JD-Core Version:    0.7.0.1
 */