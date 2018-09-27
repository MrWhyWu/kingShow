/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.GraphInput;
/*   4:    */ import io.protostuff.Input;
/*   5:    */ import io.protostuff.MapSchema.MessageFactory;
/*   6:    */ import io.protostuff.Output;
/*   7:    */ import io.protostuff.Pipe;
/*   8:    */ import io.protostuff.Pipe.Schema;
/*   9:    */ import io.protostuff.ProtostuffException;
/*  10:    */ import io.protostuff.Schema;
/*  11:    */ import io.protostuff.StatefulOutput;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.lang.reflect.Field;
/*  14:    */ import java.util.Collection;
/*  15:    */ import java.util.Collections;
/*  16:    */ import java.util.EnumMap;
/*  17:    */ import java.util.IdentityHashMap;
/*  18:    */ import java.util.Map;
/*  19:    */ 
/*  20:    */ public abstract class PolymorphicMapSchema
/*  21:    */   extends PolymorphicSchema
/*  22:    */ {
/*  23:    */   static final int ID_EMPTY_MAP = 1;
/*  24:    */   static final int ID_SINGLETON_MAP = 2;
/*  25:    */   static final int ID_UNMODIFIABLE_MAP = 3;
/*  26:    */   static final int ID_UNMODIFIABLE_SORTED_MAP = 4;
/*  27:    */   static final int ID_SYNCHRONIZED_MAP = 5;
/*  28:    */   static final int ID_SYNCHRONIZED_SORTED_MAP = 6;
/*  29:    */   static final int ID_CHECKED_MAP = 7;
/*  30:    */   static final int ID_CHECKED_SORTED_MAP = 8;
/*  31:    */   static final String STR_EMPTY_MAP = "a";
/*  32:    */   static final String STR_SINGLETON_MAP = "b";
/*  33:    */   static final String STR_UNMODIFIABLE_MAP = "c";
/*  34:    */   static final String STR_UNMODIFIABLE_SORTED_MAP = "d";
/*  35:    */   static final String STR_SYNCHRONIZED_MAP = "e";
/*  36:    */   static final String STR_SYNCHRONIZED_SORTED_MAP = "f";
/*  37:    */   static final String STR_CHECKED_MAP = "g";
/*  38:    */   static final String STR_CHECKED_SORTED_MAP = "h";
/*  39: 73 */   static final IdentityHashMap<Class<?>, Integer> __nonPublicMaps = new IdentityHashMap();
/*  40:    */   static final Field fSingletonMap_k;
/*  41:    */   static final Field fSingletonMap_v;
/*  42:    */   static final Field fUnmodifiableMap_m;
/*  43:    */   static final Field fUnmodifiableSortedMap_sm;
/*  44:    */   static final Field fSynchronizedMap_m;
/*  45:    */   static final Field fSynchronizedSortedMap_sm;
/*  46:    */   static final Field fSynchronizedMap_mutex;
/*  47:    */   static final Field fCheckedMap_m;
/*  48:    */   static final Field fCheckedSortedMap_sm;
/*  49:    */   static final Field fCheckedMap_keyType;
/*  50:    */   static final Field fCheckedMap_valueType;
/*  51:    */   static final RuntimeEnv.Instantiator<?> iSingletonMap;
/*  52:    */   static final RuntimeEnv.Instantiator<?> iUnmodifiableMap;
/*  53:    */   static final RuntimeEnv.Instantiator<?> iUnmodifiableSortedMap;
/*  54:    */   static final RuntimeEnv.Instantiator<?> iSynchronizedMap;
/*  55:    */   static final RuntimeEnv.Instantiator<?> iSynchronizedSortedMap;
/*  56:    */   static final RuntimeEnv.Instantiator<?> iCheckedMap;
/*  57:    */   static final RuntimeEnv.Instantiator<?> iCheckedSortedMap;
/*  58:    */   
/*  59:    */   static
/*  60:    */   {
/*  61:100 */     map("java.util.Collections$EmptyMap", 1);
/*  62:    */     
/*  63:102 */     Class<?> cSingletonMap = map("java.util.Collections$SingletonMap", 2);
/*  64:    */     
/*  65:    */ 
/*  66:105 */     Class<?> cUnmodifiableMap = map("java.util.Collections$UnmodifiableMap", 3);
/*  67:    */     
/*  68:    */ 
/*  69:108 */     Class<?> cUnmodifiableSortedMap = map("java.util.Collections$UnmodifiableSortedMap", 4);
/*  70:    */     
/*  71:    */ 
/*  72:    */ 
/*  73:112 */     Class<?> cSynchronizedMap = map("java.util.Collections$SynchronizedMap", 5);
/*  74:    */     
/*  75:    */ 
/*  76:115 */     Class<?> cSynchronizedSortedMap = map("java.util.Collections$SynchronizedSortedMap", 6);
/*  77:    */     
/*  78:    */ 
/*  79:    */ 
/*  80:119 */     Class<?> cCheckedMap = map("java.util.Collections$CheckedMap", 7);
/*  81:    */     
/*  82:    */ 
/*  83:122 */     Class<?> cCheckedSortedMap = map("java.util.Collections$CheckedSortedMap", 8);
/*  84:    */     try
/*  85:    */     {
/*  86:127 */       fSingletonMap_k = cSingletonMap.getDeclaredField("k");
/*  87:128 */       fSingletonMap_v = cSingletonMap.getDeclaredField("v");
/*  88:    */       
/*  89:130 */       fUnmodifiableMap_m = cUnmodifiableMap.getDeclaredField("m");
/*  90:    */       
/*  91:132 */       fUnmodifiableSortedMap_sm = cUnmodifiableSortedMap.getDeclaredField("sm");
/*  92:    */       
/*  93:134 */       fSynchronizedMap_m = cSynchronizedMap.getDeclaredField("m");
/*  94:    */       
/*  95:136 */       fSynchronizedSortedMap_sm = cSynchronizedSortedMap.getDeclaredField("sm");
/*  96:137 */       fSynchronizedMap_mutex = cSynchronizedMap.getDeclaredField("mutex");
/*  97:    */       
/*  98:139 */       fCheckedMap_m = cCheckedMap.getDeclaredField("m");
/*  99:140 */       fCheckedSortedMap_sm = cCheckedSortedMap.getDeclaredField("sm");
/* 100:141 */       fCheckedMap_keyType = cCheckedMap.getDeclaredField("keyType");
/* 101:142 */       fCheckedMap_valueType = cCheckedMap.getDeclaredField("valueType");
/* 102:    */       
/* 103:144 */       iSingletonMap = RuntimeEnv.newInstantiator(cSingletonMap);
/* 104:    */       
/* 105:146 */       iUnmodifiableMap = RuntimeEnv.newInstantiator(cUnmodifiableMap);
/* 106:    */       
/* 107:148 */       iUnmodifiableSortedMap = RuntimeEnv.newInstantiator(cUnmodifiableSortedMap);
/* 108:    */       
/* 109:150 */       iSynchronizedMap = RuntimeEnv.newInstantiator(cSynchronizedMap);
/* 110:    */       
/* 111:152 */       iSynchronizedSortedMap = RuntimeEnv.newInstantiator(cSynchronizedSortedMap);
/* 112:    */       
/* 113:154 */       iCheckedMap = RuntimeEnv.newInstantiator(cCheckedMap);
/* 114:155 */       iCheckedSortedMap = RuntimeEnv.newInstantiator(cCheckedSortedMap);
/* 115:    */     }
/* 116:    */     catch (Exception e)
/* 117:    */     {
/* 118:159 */       throw new RuntimeException(e);
/* 119:    */     }
/* 120:162 */     fSingletonMap_k.setAccessible(true);
/* 121:163 */     fSingletonMap_v.setAccessible(true);
/* 122:    */     
/* 123:165 */     fUnmodifiableMap_m.setAccessible(true);
/* 124:166 */     fUnmodifiableSortedMap_sm.setAccessible(true);
/* 125:    */     
/* 126:168 */     fSynchronizedMap_m.setAccessible(true);
/* 127:169 */     fSynchronizedSortedMap_sm.setAccessible(true);
/* 128:170 */     fSynchronizedMap_mutex.setAccessible(true);
/* 129:    */     
/* 130:172 */     fCheckedMap_m.setAccessible(true);
/* 131:173 */     fCheckedSortedMap_sm.setAccessible(true);
/* 132:174 */     fCheckedMap_keyType.setAccessible(true);
/* 133:175 */     fCheckedMap_valueType.setAccessible(true);
/* 134:    */   }
/* 135:    */   
/* 136:    */   private static Class<?> map(String className, int id)
/* 137:    */   {
/* 138:180 */     Class<?> clazz = RuntimeEnv.loadClass(className);
/* 139:181 */     __nonPublicMaps.put(clazz, Integer.valueOf(id));
/* 140:182 */     return clazz;
/* 141:    */   }
/* 142:    */   
/* 143:    */   static String name(int number)
/* 144:    */   {
/* 145:187 */     switch (number)
/* 146:    */     {
/* 147:    */     case 1: 
/* 148:190 */       return "a";
/* 149:    */     case 2: 
/* 150:192 */       return "b";
/* 151:    */     case 3: 
/* 152:194 */       return "c";
/* 153:    */     case 4: 
/* 154:196 */       return "d";
/* 155:    */     case 5: 
/* 156:198 */       return "e";
/* 157:    */     case 6: 
/* 158:200 */       return "f";
/* 159:    */     case 7: 
/* 160:202 */       return "g";
/* 161:    */     case 8: 
/* 162:204 */       return "h";
/* 163:    */     case 23: 
/* 164:206 */       return "w";
/* 165:    */     case 26: 
/* 166:208 */       return "z";
/* 167:    */     }
/* 168:210 */     return null;
/* 169:    */   }
/* 170:    */   
/* 171:    */   static int number(String name)
/* 172:    */   {
/* 173:216 */     return name.length() != 1 ? 0 : number(name.charAt(0));
/* 174:    */   }
/* 175:    */   
/* 176:    */   static int number(char c)
/* 177:    */   {
/* 178:221 */     switch (c)
/* 179:    */     {
/* 180:    */     case 'a': 
/* 181:224 */       return 1;
/* 182:    */     case 'b': 
/* 183:226 */       return 2;
/* 184:    */     case 'c': 
/* 185:228 */       return 3;
/* 186:    */     case 'd': 
/* 187:230 */       return 4;
/* 188:    */     case 'e': 
/* 189:232 */       return 5;
/* 190:    */     case 'f': 
/* 191:234 */       return 6;
/* 192:    */     case 'g': 
/* 193:236 */       return 7;
/* 194:    */     case 'h': 
/* 195:238 */       return 8;
/* 196:    */     case 'w': 
/* 197:240 */       return 23;
/* 198:    */     case 'z': 
/* 199:242 */       return 26;
/* 200:    */     }
/* 201:244 */     return 0;
/* 202:    */   }
/* 203:    */   
/* 204:248 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 205:    */   {
/* 206:    */     protected void transfer(Pipe pipe, Input input, Output output)
/* 207:    */       throws IOException
/* 208:    */     {
/* 209:255 */       PolymorphicMapSchema.transferObject(this, pipe, input, output, PolymorphicMapSchema.this.strategy);
/* 210:    */     }
/* 211:    */   };
/* 212:    */   
/* 213:    */   public PolymorphicMapSchema(IdStrategy strategy)
/* 214:    */   {
/* 215:261 */     super(strategy);
/* 216:    */   }
/* 217:    */   
/* 218:    */   public Pipe.Schema<Object> getPipeSchema()
/* 219:    */   {
/* 220:267 */     return this.pipeSchema;
/* 221:    */   }
/* 222:    */   
/* 223:    */   public String getFieldName(int number)
/* 224:    */   {
/* 225:273 */     return name(number);
/* 226:    */   }
/* 227:    */   
/* 228:    */   public int getFieldNumber(String name)
/* 229:    */   {
/* 230:279 */     return number(name);
/* 231:    */   }
/* 232:    */   
/* 233:    */   public String messageFullName()
/* 234:    */   {
/* 235:285 */     return Collection.class.getName();
/* 236:    */   }
/* 237:    */   
/* 238:    */   public String messageName()
/* 239:    */   {
/* 240:291 */     return Collection.class.getSimpleName();
/* 241:    */   }
/* 242:    */   
/* 243:    */   public void mergeFrom(Input input, Object owner)
/* 244:    */     throws IOException
/* 245:    */   {
/* 246:297 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public void writeTo(Output output, Object value)
/* 250:    */     throws IOException
/* 251:    */   {
/* 252:303 */     writeObjectTo(output, value, this, this.strategy);
/* 253:    */   }
/* 254:    */   
/* 255:    */   static int idFrom(Class<?> clazz)
/* 256:    */   {
/* 257:308 */     Integer id = (Integer)__nonPublicMaps.get(clazz);
/* 258:309 */     if (id == null) {
/* 259:310 */       throw new RuntimeException("Unknown map: " + clazz);
/* 260:    */     }
/* 261:312 */     return id.intValue();
/* 262:    */   }
/* 263:    */   
/* 264:    */   static Object instanceFrom(int id)
/* 265:    */   {
/* 266:317 */     switch (id)
/* 267:    */     {
/* 268:    */     case 1: 
/* 269:320 */       return Collections.EMPTY_MAP;
/* 270:    */     case 2: 
/* 271:323 */       return iSingletonMap.newInstance();
/* 272:    */     case 3: 
/* 273:326 */       return iUnmodifiableMap.newInstance();
/* 274:    */     case 4: 
/* 275:328 */       return iUnmodifiableSortedMap.newInstance();
/* 276:    */     case 5: 
/* 277:331 */       return iSynchronizedMap.newInstance();
/* 278:    */     case 6: 
/* 279:333 */       return iSynchronizedSortedMap.newInstance();
/* 280:    */     case 7: 
/* 281:336 */       return iCheckedMap.newInstance();
/* 282:    */     case 8: 
/* 283:338 */       return iCheckedSortedMap.newInstance();
/* 284:    */     }
/* 285:341 */     throw new RuntimeException("Unknown id: " + id);
/* 286:    */   }
/* 287:    */   
/* 288:    */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/* 289:    */     throws IOException
/* 290:    */   {
/* 291:349 */     if (Collections.class == value.getClass().getDeclaringClass())
/* 292:    */     {
/* 293:351 */       writeNonPublicMapTo(output, value, currentSchema, strategy);
/* 294:352 */       return;
/* 295:    */     }
/* 296:355 */     Class<Object> clazz = value.getClass();
/* 297:356 */     if (EnumMap.class.isAssignableFrom(clazz)) {
/* 298:358 */       strategy.writeEnumIdTo(output, 23, 
/* 299:359 */         EnumIO.getKeyTypeFromEnumMap(value));
/* 300:    */     } else {
/* 301:365 */       strategy.writeMapIdTo(output, 26, clazz);
/* 302:    */     }
/* 303:368 */     if ((output instanceof StatefulOutput)) {
/* 304:371 */       ((StatefulOutput)output).updateLast(strategy.MAP_SCHEMA, currentSchema);
/* 305:    */     }
/* 306:375 */     strategy.MAP_SCHEMA.writeTo(output, (Map)value);
/* 307:    */   }
/* 308:    */   
/* 309:    */   static void writeNonPublicMapTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/* 310:    */     throws IOException
/* 311:    */   {
/* 312:381 */     Integer n = (Integer)__nonPublicMaps.get(value.getClass());
/* 313:382 */     if (n == null) {
/* 314:384 */       throw new RuntimeException("Unknown collection: " + value.getClass());
/* 315:    */     }
/* 316:385 */     int id = n.intValue();
/* 317:386 */     switch (id)
/* 318:    */     {
/* 319:    */     case 1: 
/* 320:389 */       output.writeUInt32(id, 0, false);
/* 321:390 */       break;
/* 322:    */     case 2: 
/* 323:    */       try
/* 324:    */       {
/* 325:397 */         Object k = fSingletonMap_k.get(value);
/* 326:398 */         v = fSingletonMap_v.get(value);
/* 327:    */       }
/* 328:    */       catch (Exception e)
/* 329:    */       {
/* 330:    */         Object v;
/* 331:402 */         throw new RuntimeException(e);
/* 332:    */       }
/* 333:    */       Object v;
/* 334:    */       Object k;
/* 335:405 */       output.writeUInt32(id, 0, false);
/* 336:406 */       if (k != null) {
/* 337:407 */         output.writeObject(1, k, strategy.OBJECT_SCHEMA, false);
/* 338:    */       }
/* 339:408 */       if (v != null) {
/* 340:409 */         output.writeObject(3, v, strategy.OBJECT_SCHEMA, false);
/* 341:    */       }
/* 342:    */       break;
/* 343:    */     case 3: 
/* 344:414 */       writeUnmodifiableMapTo(output, value, currentSchema, strategy, id);
/* 345:415 */       break;
/* 346:    */     case 4: 
/* 347:418 */       writeUnmodifiableMapTo(output, value, currentSchema, strategy, id);
/* 348:419 */       break;
/* 349:    */     case 5: 
/* 350:422 */       writeSynchronizedMapTo(output, value, currentSchema, strategy, id);
/* 351:423 */       break;
/* 352:    */     case 6: 
/* 353:426 */       writeSynchronizedMapTo(output, value, currentSchema, strategy, id);
/* 354:427 */       break;
/* 355:    */     case 7: 
/* 356:430 */       writeCheckedMapTo(output, value, currentSchema, strategy, id);
/* 357:431 */       break;
/* 358:    */     case 8: 
/* 359:434 */       writeCheckedMapTo(output, value, currentSchema, strategy, id);
/* 360:435 */       break;
/* 361:    */     default: 
/* 362:438 */       throw new RuntimeException("Should not happen.");
/* 363:    */     }
/* 364:    */   }
/* 365:    */   
/* 366:    */   private static void writeUnmodifiableMapTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy, int id)
/* 367:    */     throws IOException
/* 368:    */   {
/* 369:    */     try
/* 370:    */     {
/* 371:449 */       m = fUnmodifiableMap_m.get(value);
/* 372:    */     }
/* 373:    */     catch (Exception e)
/* 374:    */     {
/* 375:    */       Object m;
/* 376:453 */       throw new RuntimeException(e);
/* 377:    */     }
/* 378:    */     Object m;
/* 379:456 */     output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
/* 380:    */   }
/* 381:    */   
/* 382:    */   private static void writeSynchronizedMapTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy, int id)
/* 383:    */     throws IOException
/* 384:    */   {
/* 385:    */     try
/* 386:    */     {
/* 387:466 */       Object m = fSynchronizedMap_m.get(value);
/* 388:467 */       mutex = fSynchronizedMap_mutex.get(value);
/* 389:    */     }
/* 390:    */     catch (Exception e)
/* 391:    */     {
/* 392:    */       Object mutex;
/* 393:471 */       throw new RuntimeException(e);
/* 394:    */     }
/* 395:    */     Object mutex;
/* 396:    */     Object m;
/* 397:474 */     if (mutex != value) {
/* 398:479 */       throw new RuntimeException("This exception is thrown to fail fast. Synchronized collections with a different mutex would only work if graph format is used, since the reference is retained.");
/* 399:    */     }
/* 400:485 */     output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
/* 401:    */   }
/* 402:    */   
/* 403:    */   private static void writeCheckedMapTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy, int id)
/* 404:    */     throws IOException
/* 405:    */   {
/* 406:    */     try
/* 407:    */     {
/* 408:495 */       Object m = fCheckedMap_m.get(value);
/* 409:496 */       Object keyType = fCheckedMap_keyType.get(value);
/* 410:497 */       valueType = fCheckedMap_valueType.get(value);
/* 411:    */     }
/* 412:    */     catch (Exception e)
/* 413:    */     {
/* 414:    */       Object valueType;
/* 415:501 */       throw new RuntimeException(e);
/* 416:    */     }
/* 417:    */     Object valueType;
/* 418:    */     Object keyType;
/* 419:    */     Object m;
/* 420:504 */     output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
/* 421:505 */     output.writeObject(1, keyType, strategy.CLASS_SCHEMA, false);
/* 422:506 */     output.writeObject(2, valueType, strategy.CLASS_SCHEMA, false);
/* 423:    */   }
/* 424:    */   
/* 425:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/* 426:    */     throws IOException
/* 427:    */   {
/* 428:512 */     return readObjectFrom(input, schema, owner, strategy, input
/* 429:513 */       .readFieldNumber(schema));
/* 430:    */   }
/* 431:    */   
/* 432:    */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, int number)
/* 433:    */     throws IOException
/* 434:    */   {
/* 435:520 */     boolean graph = input instanceof GraphInput;
/* 436:521 */     Object ret = null;
/* 437:522 */     switch (number)
/* 438:    */     {
/* 439:    */     case 1: 
/* 440:525 */       if (graph) {
/* 441:528 */         ((GraphInput)input).updateLast(Collections.EMPTY_MAP, owner);
/* 442:    */       }
/* 443:531 */       if (0 != input.readUInt32()) {
/* 444:532 */         throw new ProtostuffException("Corrupt input.");
/* 445:    */       }
/* 446:534 */       ret = Collections.EMPTY_MAP;
/* 447:535 */       break;
/* 448:    */     case 2: 
/* 449:539 */       Object map = iSingletonMap.newInstance();
/* 450:540 */       if (graph) {
/* 451:543 */         ((GraphInput)input).updateLast(map, owner);
/* 452:    */       }
/* 453:546 */       if (0 != input.readUInt32()) {
/* 454:547 */         throw new ProtostuffException("Corrupt input.");
/* 455:    */       }
/* 456:549 */       return fillSingletonMapFrom(input, schema, owner, strategy, graph, map);
/* 457:    */     case 3: 
/* 458:554 */       ret = readUnmodifiableMapFrom(input, schema, owner, strategy, graph, iUnmodifiableMap
/* 459:555 */         .newInstance(), false);
/* 460:556 */       break;
/* 461:    */     case 4: 
/* 462:559 */       ret = readUnmodifiableMapFrom(input, schema, owner, strategy, graph, iUnmodifiableSortedMap
/* 463:560 */         .newInstance(), true);
/* 464:561 */       break;
/* 465:    */     case 5: 
/* 466:564 */       ret = readSynchronizedMapFrom(input, schema, owner, strategy, graph, iSynchronizedMap
/* 467:565 */         .newInstance(), false);
/* 468:566 */       break;
/* 469:    */     case 6: 
/* 470:569 */       ret = readSynchronizedMapFrom(input, schema, owner, strategy, graph, iSynchronizedSortedMap
/* 471:570 */         .newInstance(), true);
/* 472:571 */       break;
/* 473:    */     case 7: 
/* 474:574 */       ret = readCheckedMapFrom(input, schema, owner, strategy, graph, iCheckedMap
/* 475:575 */         .newInstance(), false);
/* 476:576 */       break;
/* 477:    */     case 8: 
/* 478:579 */       ret = readCheckedMapFrom(input, schema, owner, strategy, graph, iCheckedSortedMap
/* 479:580 */         .newInstance(), true);
/* 480:581 */       break;
/* 481:    */     case 23: 
/* 482:586 */       Map<?, Object> em = strategy.resolveEnumFrom(input).newEnumMap();
/* 483:588 */       if ((input instanceof GraphInput)) {
/* 484:591 */         ((GraphInput)input).updateLast(em, owner);
/* 485:    */       }
/* 486:594 */       strategy.MAP_SCHEMA.mergeFrom(input, em);
/* 487:    */       
/* 488:596 */       return em;
/* 489:    */     case 26: 
/* 490:601 */       Map<Object, Object> map = strategy.resolveMapFrom(input).newMessage();
/* 491:603 */       if ((input instanceof GraphInput)) {
/* 492:606 */         ((GraphInput)input).updateLast(map, owner);
/* 493:    */       }
/* 494:609 */       strategy.MAP_SCHEMA.mergeFrom(input, map);
/* 495:    */       
/* 496:611 */       return map;
/* 497:    */     case 9: 
/* 498:    */     case 10: 
/* 499:    */     case 11: 
/* 500:    */     case 12: 
/* 501:    */     case 13: 
/* 502:    */     case 14: 
/* 503:    */     case 15: 
/* 504:    */     case 16: 
/* 505:    */     case 17: 
/* 506:    */     case 18: 
/* 507:    */     case 19: 
/* 508:    */     case 20: 
/* 509:    */     case 21: 
/* 510:    */     case 22: 
/* 511:    */     case 24: 
/* 512:    */     case 25: 
/* 513:    */     default: 
/* 514:615 */       throw new ProtostuffException("Corrupt input.");
/* 515:    */     }
/* 516:618 */     if (0 != input.readFieldNumber(schema)) {
/* 517:619 */       throw new ProtostuffException("Corrupt input.");
/* 518:    */     }
/* 519:621 */     return ret;
/* 520:    */   }
/* 521:    */   
/* 522:    */   private static Object fillSingletonMapFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object map)
/* 523:    */     throws IOException
/* 524:    */   {
/* 525:631 */     switch (input.readFieldNumber(schema))
/* 526:    */     {
/* 527:    */     case 0: 
/* 528:635 */       return map;
/* 529:    */     case 1: 
/* 530:    */       break;
/* 531:    */     case 3: 
/* 532:644 */       IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/* 533:645 */       Object v = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
/* 534:646 */       if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 535:647 */         v = wrapper.value;
/* 536:    */       }
/* 537:    */       try
/* 538:    */       {
/* 539:651 */         fSingletonMap_v.set(map, v);
/* 540:    */       }
/* 541:    */       catch (Exception e)
/* 542:    */       {
/* 543:655 */         throw new RuntimeException(e);
/* 544:    */       }
/* 545:658 */       if (0 != input.readFieldNumber(schema)) {
/* 546:659 */         throw new ProtostuffException("Corrupt input.");
/* 547:    */       }
/* 548:661 */       return map;
/* 549:    */     case 2: 
/* 550:    */     default: 
/* 551:664 */       throw new ProtostuffException("Corrupt input.");
/* 552:    */     }
/* 553:667 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/* 554:668 */     Object k = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
/* 555:669 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 556:670 */       k = wrapper.value;
/* 557:    */     }
/* 558:672 */     switch (input.readFieldNumber(schema))
/* 559:    */     {
/* 560:    */     case 0: 
/* 561:    */       try
/* 562:    */       {
/* 563:678 */         fSingletonMap_k.set(map, k);
/* 564:    */       }
/* 565:    */       catch (Exception e)
/* 566:    */       {
/* 567:682 */         throw new RuntimeException(e);
/* 568:    */       }
/* 569:685 */       return map;
/* 570:    */     case 3: 
/* 571:    */       break;
/* 572:    */     default: 
/* 573:690 */       throw new ProtostuffException("Corrupt input.");
/* 574:    */     }
/* 575:693 */     Object v = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
/* 576:694 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 577:695 */       v = wrapper.value;
/* 578:    */     }
/* 579:    */     try
/* 580:    */     {
/* 581:699 */       fSingletonMap_k.set(map, k);
/* 582:700 */       fSingletonMap_v.set(map, v);
/* 583:    */     }
/* 584:    */     catch (Exception e)
/* 585:    */     {
/* 586:704 */       throw new RuntimeException(e);
/* 587:    */     }
/* 588:707 */     if (0 != input.readFieldNumber(schema)) {
/* 589:708 */       throw new ProtostuffException("Corrupt input.");
/* 590:    */     }
/* 591:710 */     return map;
/* 592:    */   }
/* 593:    */   
/* 594:    */   private static Object readUnmodifiableMapFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object map, boolean sm)
/* 595:    */     throws IOException
/* 596:    */   {
/* 597:717 */     if (graph) {
/* 598:720 */       ((GraphInput)input).updateLast(map, owner);
/* 599:    */     }
/* 600:723 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/* 601:724 */     Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
/* 602:725 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 603:726 */       m = wrapper.value;
/* 604:    */     }
/* 605:    */     try
/* 606:    */     {
/* 607:729 */       fUnmodifiableMap_m.set(map, m);
/* 608:731 */       if (sm) {
/* 609:732 */         fUnmodifiableSortedMap_sm.set(map, m);
/* 610:    */       }
/* 611:    */     }
/* 612:    */     catch (Exception e)
/* 613:    */     {
/* 614:736 */       throw new RuntimeException(e);
/* 615:    */     }
/* 616:739 */     return map;
/* 617:    */   }
/* 618:    */   
/* 619:    */   private static Object readSynchronizedMapFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object map, boolean sm)
/* 620:    */     throws IOException
/* 621:    */   {
/* 622:746 */     if (graph) {
/* 623:749 */       ((GraphInput)input).updateLast(map, owner);
/* 624:    */     }
/* 625:752 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/* 626:753 */     Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
/* 627:754 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 628:755 */       m = wrapper.value;
/* 629:    */     }
/* 630:    */     try
/* 631:    */     {
/* 632:758 */       fSynchronizedMap_m.set(map, m);
/* 633:759 */       fSynchronizedMap_mutex.set(map, map);
/* 634:761 */       if (sm) {
/* 635:762 */         fSynchronizedSortedMap_sm.set(map, m);
/* 636:    */       }
/* 637:    */     }
/* 638:    */     catch (Exception e)
/* 639:    */     {
/* 640:766 */       throw new RuntimeException(e);
/* 641:    */     }
/* 642:769 */     return map;
/* 643:    */   }
/* 644:    */   
/* 645:    */   private static Object readCheckedMapFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object map, boolean sm)
/* 646:    */     throws IOException
/* 647:    */   {
/* 648:776 */     if (graph) {
/* 649:779 */       ((GraphInput)input).updateLast(map, owner);
/* 650:    */     }
/* 651:782 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/* 652:783 */     Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
/* 653:784 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 654:785 */       m = wrapper.value;
/* 655:    */     }
/* 656:787 */     if (1 != input.readFieldNumber(schema)) {
/* 657:788 */       throw new ProtostuffException("Corrupt input.");
/* 658:    */     }
/* 659:790 */     Object keyType = input.mergeObject(wrapper, strategy.CLASS_SCHEMA);
/* 660:791 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 661:792 */       keyType = wrapper.value;
/* 662:    */     }
/* 663:794 */     if (2 != input.readFieldNumber(schema)) {
/* 664:795 */       throw new ProtostuffException("Corrupt input.");
/* 665:    */     }
/* 666:797 */     Object valueType = input.mergeObject(wrapper, strategy.CLASS_SCHEMA);
/* 667:798 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 668:799 */       valueType = wrapper.value;
/* 669:    */     }
/* 670:    */     try
/* 671:    */     {
/* 672:803 */       fCheckedMap_m.set(map, m);
/* 673:804 */       fCheckedMap_keyType.set(map, keyType);
/* 674:805 */       fCheckedMap_valueType.set(map, valueType);
/* 675:807 */       if (sm) {
/* 676:808 */         fCheckedSortedMap_sm.set(map, m);
/* 677:    */       }
/* 678:    */     }
/* 679:    */     catch (Exception e)
/* 680:    */     {
/* 681:812 */       throw new RuntimeException(e);
/* 682:    */     }
/* 683:815 */     return map;
/* 684:    */   }
/* 685:    */   
/* 686:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 687:    */     throws IOException
/* 688:    */   {
/* 689:821 */     transferObject(pipeSchema, pipe, input, output, strategy, input
/* 690:822 */       .readFieldNumber(pipeSchema.wrappedSchema));
/* 691:    */   }
/* 692:    */   
/* 693:    */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, int number)
/* 694:    */     throws IOException
/* 695:    */   {
/* 696:829 */     switch (number)
/* 697:    */     {
/* 698:    */     case 1: 
/* 699:832 */       output.writeUInt32(number, input.readUInt32(), false);
/* 700:833 */       break;
/* 701:    */     case 2: 
/* 702:836 */       if (0 != input.readUInt32()) {
/* 703:837 */         throw new ProtostuffException("Corrupt input.");
/* 704:    */       }
/* 705:839 */       output.writeUInt32(number, 0, false);
/* 706:    */       
/* 707:841 */       transferSingletonMap(pipeSchema, pipe, input, output, strategy);
/* 708:842 */       return;
/* 709:    */     case 3: 
/* 710:845 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 711:    */       
/* 712:847 */       break;
/* 713:    */     case 4: 
/* 714:850 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 715:    */       
/* 716:852 */       break;
/* 717:    */     case 5: 
/* 718:855 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 719:    */       
/* 720:857 */       break;
/* 721:    */     case 6: 
/* 722:860 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 723:    */       
/* 724:862 */       break;
/* 725:    */     case 7: 
/* 726:865 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 727:868 */       if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 728:869 */         throw new ProtostuffException("Corrupt input.");
/* 729:    */       }
/* 730:871 */       output.writeObject(1, pipe, strategy.CLASS_PIPE_SCHEMA, false);
/* 731:873 */       if (2 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 732:874 */         throw new ProtostuffException("Corrupt input.");
/* 733:    */       }
/* 734:876 */       output.writeObject(2, pipe, strategy.CLASS_PIPE_SCHEMA, false);
/* 735:877 */       break;
/* 736:    */     case 8: 
/* 737:880 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 738:883 */       if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 739:884 */         throw new ProtostuffException("Corrupt input.");
/* 740:    */       }
/* 741:886 */       output.writeObject(1, pipe, strategy.CLASS_PIPE_SCHEMA, false);
/* 742:888 */       if (2 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 743:889 */         throw new ProtostuffException("Corrupt input.");
/* 744:    */       }
/* 745:891 */       output.writeObject(2, pipe, strategy.CLASS_PIPE_SCHEMA, false);
/* 746:892 */       break;
/* 747:    */     case 23: 
/* 748:895 */       strategy.transferEnumId(input, output, number);
/* 749:897 */       if ((output instanceof StatefulOutput)) {
/* 750:900 */         ((StatefulOutput)output).updateLast(strategy.MAP_PIPE_SCHEMA, pipeSchema);
/* 751:    */       }
/* 752:904 */       Pipe.transferDirect(strategy.MAP_PIPE_SCHEMA, pipe, input, output);
/* 753:905 */       return;
/* 754:    */     case 26: 
/* 755:907 */       strategy.transferMapId(input, output, number);
/* 756:909 */       if ((output instanceof StatefulOutput)) {
/* 757:912 */         ((StatefulOutput)output).updateLast(strategy.MAP_PIPE_SCHEMA, pipeSchema);
/* 758:    */       }
/* 759:916 */       Pipe.transferDirect(strategy.MAP_PIPE_SCHEMA, pipe, input, output);
/* 760:917 */       return;
/* 761:    */     case 9: 
/* 762:    */     case 10: 
/* 763:    */     case 11: 
/* 764:    */     case 12: 
/* 765:    */     case 13: 
/* 766:    */     case 14: 
/* 767:    */     case 15: 
/* 768:    */     case 16: 
/* 769:    */     case 17: 
/* 770:    */     case 18: 
/* 771:    */     case 19: 
/* 772:    */     case 20: 
/* 773:    */     case 21: 
/* 774:    */     case 22: 
/* 775:    */     case 24: 
/* 776:    */     case 25: 
/* 777:    */     default: 
/* 778:920 */       throw new ProtostuffException("Corrupt input.");
/* 779:    */     }
/* 780:923 */     if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 781:924 */       throw new ProtostuffException("Corrupt input.");
/* 782:    */     }
/* 783:    */   }
/* 784:    */   
/* 785:    */   static void transferSingletonMap(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 786:    */     throws IOException
/* 787:    */   {
/* 788:930 */     switch (input.readFieldNumber(pipeSchema.wrappedSchema))
/* 789:    */     {
/* 790:    */     case 0: 
/* 791:    */       return;
/* 792:    */     case 1: 
/* 793:    */       break;
/* 794:    */     case 3: 
/* 795:943 */       output.writeObject(3, pipe, strategy.OBJECT_PIPE_SCHEMA, false);
/* 796:945 */       if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 797:946 */         throw new ProtostuffException("Corrupt input.");
/* 798:    */       }
/* 799:948 */       return;
/* 800:    */     case 2: 
/* 801:    */     default: 
/* 802:951 */       throw new ProtostuffException("Corrupt input.");
/* 803:    */     }
/* 804:954 */     output.writeObject(1, pipe, strategy.OBJECT_PIPE_SCHEMA, false);
/* 805:956 */     switch (input.readFieldNumber(pipeSchema.wrappedSchema))
/* 806:    */     {
/* 807:    */     case 0: 
/* 808:    */       return;
/* 809:    */     case 3: 
/* 810:    */       break;
/* 811:    */     default: 
/* 812:965 */       throw new ProtostuffException("Corrupt input.");
/* 813:    */     }
/* 814:968 */     output.writeObject(3, pipe, strategy.OBJECT_PIPE_SCHEMA, false);
/* 815:970 */     if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 816:971 */       throw new ProtostuffException("Corrupt input.");
/* 817:    */     }
/* 818:    */   }
/* 819:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicMapSchema
 * JD-Core Version:    0.7.0.1
 */