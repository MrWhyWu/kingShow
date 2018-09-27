/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.CollectionSchema.MessageFactories;
/*   4:    */ import io.protostuff.CollectionSchema.MessageFactory;
/*   5:    */ import io.protostuff.Input;
/*   6:    */ import io.protostuff.MapSchema.MessageFactories;
/*   7:    */ import io.protostuff.MapSchema.MessageFactory;
/*   8:    */ import io.protostuff.Message;
/*   9:    */ import io.protostuff.Output;
/*  10:    */ import io.protostuff.Pipe.Schema;
/*  11:    */ import io.protostuff.ProtostuffException;
/*  12:    */ import io.protostuff.Schema;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.lang.reflect.Modifier;
/*  15:    */ import java.util.Collection;
/*  16:    */ import java.util.Map;
/*  17:    */ import java.util.concurrent.ConcurrentHashMap;
/*  18:    */ 
/*  19:    */ public final class DefaultIdStrategy
/*  20:    */   extends IdStrategy
/*  21:    */ {
/*  22: 36 */   final ConcurrentHashMap<String, HasSchema<?>> pojoMapping = new ConcurrentHashMap();
/*  23: 38 */   final ConcurrentHashMap<String, EnumIO<?>> enumMapping = new ConcurrentHashMap();
/*  24: 40 */   final ConcurrentHashMap<String, CollectionSchema.MessageFactory> collectionMapping = new ConcurrentHashMap();
/*  25: 42 */   final ConcurrentHashMap<String, MapSchema.MessageFactory> mapMapping = new ConcurrentHashMap();
/*  26: 44 */   final ConcurrentHashMap<String, HasDelegate<?>> delegateMapping = new ConcurrentHashMap();
/*  27:    */   
/*  28:    */   public DefaultIdStrategy()
/*  29:    */   {
/*  30: 48 */     super(DEFAULT_FLAGS, null, 0);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public DefaultIdStrategy(IdStrategy primaryGroup, int groupId)
/*  34:    */   {
/*  35: 53 */     super(DEFAULT_FLAGS, primaryGroup, groupId);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public DefaultIdStrategy(int flags, IdStrategy primaryGroup, int groupId)
/*  39:    */   {
/*  40: 58 */     super(flags, primaryGroup, groupId);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public <T> boolean registerPojo(Class<T> typeClass, Schema<T> schema)
/*  44:    */   {
/*  45: 67 */     assert ((typeClass != null) && (schema != null));
/*  46:    */     
/*  47: 69 */     HasSchema<?> last = (HasSchema)this.pojoMapping.putIfAbsent(typeClass.getName(), new Registered(schema, this));
/*  48:    */     
/*  49:    */ 
/*  50: 72 */     return (last == null) || (((last instanceof Registered)) && (((Registered)last).schema == schema));
/*  51:    */   }
/*  52:    */   
/*  53:    */   public <T> boolean registerPojo(Class<T> typeClass)
/*  54:    */   {
/*  55: 82 */     assert (typeClass != null);
/*  56:    */     
/*  57: 84 */     HasSchema<?> last = (HasSchema)this.pojoMapping.putIfAbsent(typeClass.getName(), new LazyRegister(typeClass, this));
/*  58:    */     
/*  59:    */ 
/*  60: 87 */     return (last == null) || ((last instanceof LazyRegister));
/*  61:    */   }
/*  62:    */   
/*  63:    */   public <T extends Enum<T>> boolean registerEnum(Class<T> enumClass)
/*  64:    */   {
/*  65: 95 */     return null == this.enumMapping.putIfAbsent(enumClass.getName(), 
/*  66: 96 */       EnumIO.newEnumIO(enumClass, this));
/*  67:    */   }
/*  68:    */   
/*  69:    */   public <T> boolean registerDelegate(Delegate<T> delegate)
/*  70:    */   {
/*  71:104 */     return null == this.delegateMapping.putIfAbsent(delegate.typeClass()
/*  72:105 */       .getName(), new HasDelegate(delegate, this));
/*  73:    */   }
/*  74:    */   
/*  75:    */   public boolean registerCollection(CollectionSchema.MessageFactory factory)
/*  76:    */   {
/*  77:113 */     return null == this.collectionMapping.putIfAbsent(factory.typeClass()
/*  78:114 */       .getName(), factory);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean registerMap(MapSchema.MessageFactory factory)
/*  82:    */   {
/*  83:122 */     return null == this.mapMapping.putIfAbsent(factory.typeClass().getName(), factory);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public <T> boolean map(Class<? super T> baseClass, Class<T> typeClass)
/*  87:    */   {
/*  88:131 */     assert ((baseClass != null) && (typeClass != null));
/*  89:133 */     if ((typeClass.isInterface()) || 
/*  90:134 */       (Modifier.isAbstract(typeClass.getModifiers()))) {
/*  91:136 */       throw new IllegalArgumentException(typeClass + " cannot be an interface/abstract class.");
/*  92:    */     }
/*  93:140 */     HasSchema<?> last = (HasSchema)this.pojoMapping.putIfAbsent(baseClass.getName(), new Mapped(baseClass, typeClass, this));
/*  94:    */     
/*  95:    */ 
/*  96:143 */     return (last == null) || (((last instanceof Mapped)) && (((Mapped)last).typeClass == typeClass));
/*  97:    */   }
/*  98:    */   
/*  99:    */   public boolean isDelegateRegistered(Class<?> typeClass)
/* 100:    */   {
/* 101:150 */     return this.delegateMapping.containsKey(typeClass.getName());
/* 102:    */   }
/* 103:    */   
/* 104:    */   public <T> HasDelegate<T> getDelegateWrapper(Class<? super T> typeClass)
/* 105:    */   {
/* 106:157 */     return (HasDelegate)this.delegateMapping.get(typeClass.getName());
/* 107:    */   }
/* 108:    */   
/* 109:    */   public <T> Delegate<T> getDelegate(Class<? super T> typeClass)
/* 110:    */   {
/* 111:165 */     HasDelegate<T> last = (HasDelegate)this.delegateMapping.get(typeClass.getName());
/* 112:166 */     return last == null ? null : last.delegate;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public boolean isRegistered(Class<?> typeClass)
/* 116:    */   {
/* 117:172 */     HasSchema<?> last = (HasSchema)this.pojoMapping.get(typeClass.getName());
/* 118:173 */     return (last != null) && (!(last instanceof Lazy));
/* 119:    */   }
/* 120:    */   
/* 121:    */   private <T> HasSchema<T> getSchemaWrapper(String className, boolean load)
/* 122:    */   {
/* 123:179 */     HasSchema<T> hs = (HasSchema)this.pojoMapping.get(className);
/* 124:180 */     if (hs == null)
/* 125:    */     {
/* 126:182 */       if (!load) {
/* 127:183 */         return null;
/* 128:    */       }
/* 129:185 */       Class<T> typeClass = RuntimeEnv.loadClass(className);
/* 130:    */       
/* 131:187 */       hs = new Lazy(typeClass, this);
/* 132:188 */       HasSchema<T> last = (HasSchema)this.pojoMapping.putIfAbsent(typeClass
/* 133:189 */         .getName(), hs);
/* 134:190 */       if (last != null) {
/* 135:191 */         hs = last;
/* 136:    */       }
/* 137:    */     }
/* 138:194 */     return hs;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public <T> HasSchema<T> getSchemaWrapper(Class<T> typeClass, boolean create)
/* 142:    */   {
/* 143:201 */     HasSchema<T> hs = (HasSchema)this.pojoMapping.get(typeClass.getName());
/* 144:202 */     if ((hs == null) && (create))
/* 145:    */     {
/* 146:204 */       hs = new Lazy(typeClass, this);
/* 147:205 */       HasSchema<T> last = (HasSchema)this.pojoMapping.putIfAbsent(typeClass
/* 148:206 */         .getName(), hs);
/* 149:207 */       if (last != null) {
/* 150:208 */         hs = last;
/* 151:    */       }
/* 152:    */     }
/* 153:211 */     return hs;
/* 154:    */   }
/* 155:    */   
/* 156:    */   private EnumIO<? extends Enum<?>> getEnumIO(String className, boolean load)
/* 157:    */   {
/* 158:216 */     EnumIO<?> eio = (EnumIO)this.enumMapping.get(className);
/* 159:217 */     if (eio == null)
/* 160:    */     {
/* 161:219 */       if (!load) {
/* 162:220 */         return null;
/* 163:    */       }
/* 164:222 */       Class<?> enumClass = RuntimeEnv.loadClass(className);
/* 165:    */       
/* 166:224 */       eio = EnumIO.newEnumIO(enumClass, this);
/* 167:    */       
/* 168:226 */       EnumIO<?> existing = (EnumIO)this.enumMapping.putIfAbsent(enumClass
/* 169:227 */         .getName(), eio);
/* 170:228 */       if (existing != null) {
/* 171:229 */         eio = existing;
/* 172:    */       }
/* 173:    */     }
/* 174:232 */     return eio;
/* 175:    */   }
/* 176:    */   
/* 177:    */   protected EnumIO<? extends Enum<?>> getEnumIO(Class<?> enumClass)
/* 178:    */   {
/* 179:238 */     EnumIO<?> eio = (EnumIO)this.enumMapping.get(enumClass.getName());
/* 180:239 */     if (eio == null)
/* 181:    */     {
/* 182:241 */       eio = EnumIO.newEnumIO(enumClass, this);
/* 183:    */       
/* 184:243 */       EnumIO<?> existing = (EnumIO)this.enumMapping.putIfAbsent(enumClass
/* 185:244 */         .getName(), eio);
/* 186:245 */       if (existing != null) {
/* 187:246 */         eio = existing;
/* 188:    */       }
/* 189:    */     }
/* 190:249 */     return eio;
/* 191:    */   }
/* 192:    */   
/* 193:    */   protected CollectionSchema.MessageFactory getCollectionFactory(Class<?> clazz)
/* 194:    */   {
/* 195:256 */     String className = clazz.getName();
/* 196:    */     
/* 197:258 */     CollectionSchema.MessageFactory factory = (CollectionSchema.MessageFactory)this.collectionMapping.get(className);
/* 198:259 */     if (factory == null) {
/* 199:261 */       if (className.startsWith("java.util"))
/* 200:    */       {
/* 201:263 */         factory = CollectionSchema.MessageFactories.valueOf(clazz
/* 202:264 */           .getSimpleName());
/* 203:    */       }
/* 204:    */       else
/* 205:    */       {
/* 206:268 */         factory = new RuntimeCollectionFactory(clazz);
/* 207:    */         
/* 208:270 */         CollectionSchema.MessageFactory f = (CollectionSchema.MessageFactory)this.collectionMapping.putIfAbsent(className, factory);
/* 209:271 */         if (f != null) {
/* 210:272 */           factory = f;
/* 211:    */         }
/* 212:    */       }
/* 213:    */     }
/* 214:276 */     return factory;
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected MapSchema.MessageFactory getMapFactory(Class<?> clazz)
/* 218:    */   {
/* 219:282 */     String className = clazz.getName();
/* 220:283 */     MapSchema.MessageFactory factory = (MapSchema.MessageFactory)this.mapMapping.get(className);
/* 221:284 */     if (factory == null) {
/* 222:286 */       if (className.startsWith("java.util"))
/* 223:    */       {
/* 224:288 */         factory = MapSchema.MessageFactories.valueOf(clazz
/* 225:289 */           .getSimpleName());
/* 226:    */       }
/* 227:    */       else
/* 228:    */       {
/* 229:293 */         factory = new RuntimeMapFactory(clazz);
/* 230:294 */         MapSchema.MessageFactory f = (MapSchema.MessageFactory)this.mapMapping.putIfAbsent(className, factory);
/* 231:296 */         if (f != null) {
/* 232:297 */           factory = f;
/* 233:    */         }
/* 234:    */       }
/* 235:    */     }
/* 236:301 */     return factory;
/* 237:    */   }
/* 238:    */   
/* 239:    */   protected void writeCollectionIdTo(Output output, int fieldNumber, Class<?> clazz)
/* 240:    */     throws IOException
/* 241:    */   {
/* 242:309 */     CollectionSchema.MessageFactory factory = (CollectionSchema.MessageFactory)this.collectionMapping.get(clazz);
/* 243:310 */     if ((factory == null) && (clazz.getName().startsWith("java.util"))) {
/* 244:315 */       output.writeString(fieldNumber, clazz.getSimpleName(), false);
/* 245:    */     } else {
/* 246:319 */       output.writeString(fieldNumber, clazz.getName(), false);
/* 247:    */     }
/* 248:    */   }
/* 249:    */   
/* 250:    */   protected void transferCollectionId(Input input, Output output, int fieldNumber)
/* 251:    */     throws IOException
/* 252:    */   {
/* 253:327 */     input.transferByteRangeTo(output, true, fieldNumber, false);
/* 254:    */   }
/* 255:    */   
/* 256:    */   protected CollectionSchema.MessageFactory resolveCollectionFrom(Input input)
/* 257:    */     throws IOException
/* 258:    */   {
/* 259:334 */     String className = input.readString();
/* 260:    */     
/* 261:336 */     CollectionSchema.MessageFactory factory = (CollectionSchema.MessageFactory)this.collectionMapping.get(className);
/* 262:337 */     if (factory == null) {
/* 263:339 */       if (className.indexOf('.') == -1)
/* 264:    */       {
/* 265:341 */         factory = CollectionSchema.MessageFactories.valueOf(className);
/* 266:    */       }
/* 267:    */       else
/* 268:    */       {
/* 269:346 */         factory = new RuntimeCollectionFactory(RuntimeEnv.loadClass(className));
/* 270:    */         
/* 271:348 */         CollectionSchema.MessageFactory f = (CollectionSchema.MessageFactory)this.collectionMapping.putIfAbsent(className, factory);
/* 272:349 */         if (f != null) {
/* 273:350 */           factory = f;
/* 274:    */         }
/* 275:    */       }
/* 276:    */     }
/* 277:354 */     return factory;
/* 278:    */   }
/* 279:    */   
/* 280:    */   protected void writeMapIdTo(Output output, int fieldNumber, Class<?> clazz)
/* 281:    */     throws IOException
/* 282:    */   {
/* 283:361 */     MapSchema.MessageFactory factory = (MapSchema.MessageFactory)this.mapMapping.get(clazz);
/* 284:362 */     if ((factory == null) && (clazz.getName().startsWith("java.util"))) {
/* 285:367 */       output.writeString(fieldNumber, clazz.getSimpleName(), false);
/* 286:    */     } else {
/* 287:371 */       output.writeString(fieldNumber, clazz.getName(), false);
/* 288:    */     }
/* 289:    */   }
/* 290:    */   
/* 291:    */   protected void transferMapId(Input input, Output output, int fieldNumber)
/* 292:    */     throws IOException
/* 293:    */   {
/* 294:379 */     input.transferByteRangeTo(output, true, fieldNumber, false);
/* 295:    */   }
/* 296:    */   
/* 297:    */   protected MapSchema.MessageFactory resolveMapFrom(Input input)
/* 298:    */     throws IOException
/* 299:    */   {
/* 300:386 */     String className = input.readString();
/* 301:387 */     MapSchema.MessageFactory factory = (MapSchema.MessageFactory)this.mapMapping.get(className);
/* 302:388 */     if (factory == null) {
/* 303:390 */       if (className.indexOf('.') == -1)
/* 304:    */       {
/* 305:392 */         factory = MapSchema.MessageFactories.valueOf(className);
/* 306:    */       }
/* 307:    */       else
/* 308:    */       {
/* 309:396 */         factory = new RuntimeMapFactory(RuntimeEnv.loadClass(className));
/* 310:397 */         MapSchema.MessageFactory f = (MapSchema.MessageFactory)this.mapMapping.putIfAbsent(className, factory);
/* 311:399 */         if (f != null) {
/* 312:400 */           factory = f;
/* 313:    */         }
/* 314:    */       }
/* 315:    */     }
/* 316:404 */     return factory;
/* 317:    */   }
/* 318:    */   
/* 319:    */   protected void writeEnumIdTo(Output output, int fieldNumber, Class<?> clazz)
/* 320:    */     throws IOException
/* 321:    */   {
/* 322:411 */     output.writeString(fieldNumber, clazz.getName(), false);
/* 323:    */   }
/* 324:    */   
/* 325:    */   protected void transferEnumId(Input input, Output output, int fieldNumber)
/* 326:    */     throws IOException
/* 327:    */   {
/* 328:418 */     input.transferByteRangeTo(output, true, fieldNumber, false);
/* 329:    */   }
/* 330:    */   
/* 331:    */   protected EnumIO<?> resolveEnumFrom(Input input)
/* 332:    */     throws IOException
/* 333:    */   {
/* 334:424 */     return getEnumIO(input.readString(), true);
/* 335:    */   }
/* 336:    */   
/* 337:    */   protected <T> HasDelegate<T> tryWriteDelegateIdTo(Output output, int fieldNumber, Class<T> clazz)
/* 338:    */     throws IOException
/* 339:    */   {
/* 340:432 */     HasDelegate<T> hd = (HasDelegate)this.delegateMapping.get(clazz
/* 341:433 */       .getName());
/* 342:434 */     if (hd == null) {
/* 343:435 */       return null;
/* 344:    */     }
/* 345:437 */     output.writeString(fieldNumber, clazz.getName(), false);
/* 346:    */     
/* 347:439 */     return hd;
/* 348:    */   }
/* 349:    */   
/* 350:    */   protected <T> HasDelegate<T> transferDelegateId(Input input, Output output, int fieldNumber)
/* 351:    */     throws IOException
/* 352:    */   {
/* 353:447 */     String className = input.readString();
/* 354:    */     
/* 355:    */ 
/* 356:450 */     HasDelegate<T> hd = (HasDelegate)this.delegateMapping.get(className);
/* 357:451 */     if (hd == null) {
/* 358:452 */       throw new IdStrategy.UnknownTypeException("delegate: " + className + " (Outdated registry)");
/* 359:    */     }
/* 360:455 */     output.writeString(fieldNumber, className, false);
/* 361:    */     
/* 362:457 */     return hd;
/* 363:    */   }
/* 364:    */   
/* 365:    */   protected <T> HasDelegate<T> resolveDelegateFrom(Input input)
/* 366:    */     throws IOException
/* 367:    */   {
/* 368:465 */     String className = input.readString();
/* 369:    */     
/* 370:    */ 
/* 371:468 */     HasDelegate<T> hd = (HasDelegate)this.delegateMapping.get(className);
/* 372:469 */     if (hd == null) {
/* 373:470 */       throw new IdStrategy.UnknownTypeException("delegate: " + className + " (Outdated registry)");
/* 374:    */     }
/* 375:473 */     return hd;
/* 376:    */   }
/* 377:    */   
/* 378:    */   protected <T> HasSchema<T> tryWritePojoIdTo(Output output, int fieldNumber, Class<T> clazz, boolean registered)
/* 379:    */     throws IOException
/* 380:    */   {
/* 381:480 */     HasSchema<T> hs = getSchemaWrapper(clazz, false);
/* 382:481 */     if ((hs == null) || ((registered) && ((hs instanceof Lazy)))) {
/* 383:482 */       return null;
/* 384:    */     }
/* 385:484 */     output.writeString(fieldNumber, clazz.getName(), false);
/* 386:    */     
/* 387:486 */     return hs;
/* 388:    */   }
/* 389:    */   
/* 390:    */   protected <T> HasSchema<T> writePojoIdTo(Output output, int fieldNumber, Class<T> clazz)
/* 391:    */     throws IOException
/* 392:    */   {
/* 393:493 */     output.writeString(fieldNumber, clazz.getName(), false);
/* 394:    */     
/* 395:    */ 
/* 396:496 */     return getSchemaWrapper(clazz, true);
/* 397:    */   }
/* 398:    */   
/* 399:    */   protected <T> HasSchema<T> transferPojoId(Input input, Output output, int fieldNumber)
/* 400:    */     throws IOException
/* 401:    */   {
/* 402:503 */     String className = input.readString();
/* 403:    */     
/* 404:505 */     HasSchema<T> wrapper = getSchemaWrapper(className, 0 != (0x2 & this.flags));
/* 405:507 */     if (wrapper == null) {
/* 406:509 */       throw new ProtostuffException("polymorphic pojo not registered: " + className);
/* 407:    */     }
/* 408:513 */     output.writeString(fieldNumber, className, false);
/* 409:    */     
/* 410:515 */     return wrapper;
/* 411:    */   }
/* 412:    */   
/* 413:    */   protected <T> HasSchema<T> resolvePojoFrom(Input input, int fieldNumber)
/* 414:    */     throws IOException
/* 415:    */   {
/* 416:522 */     String className = input.readString();
/* 417:    */     
/* 418:524 */     HasSchema<T> wrapper = getSchemaWrapper(className, 0 != (0x2 & this.flags));
/* 419:526 */     if (wrapper == null) {
/* 420:527 */       throw new ProtostuffException("polymorphic pojo not registered: " + className);
/* 421:    */     }
/* 422:530 */     return wrapper;
/* 423:    */   }
/* 424:    */   
/* 425:    */   protected <T> Schema<T> writeMessageIdTo(Output output, int fieldNumber, Message<T> message)
/* 426:    */     throws IOException
/* 427:    */   {
/* 428:537 */     output.writeString(fieldNumber, message.getClass().getName(), false);
/* 429:    */     
/* 430:539 */     return message.cachedSchema();
/* 431:    */   }
/* 432:    */   
/* 433:    */   protected void writeArrayIdTo(Output output, Class<?> componentType)
/* 434:    */     throws IOException
/* 435:    */   {
/* 436:546 */     output.writeString(15, componentType
/* 437:547 */       .getName(), false);
/* 438:    */   }
/* 439:    */   
/* 440:    */   protected void transferArrayId(Input input, Output output, int fieldNumber, boolean mapped)
/* 441:    */     throws IOException
/* 442:    */   {
/* 443:554 */     input.transferByteRangeTo(output, true, fieldNumber, false);
/* 444:    */   }
/* 445:    */   
/* 446:    */   protected Class<?> resolveArrayComponentTypeFrom(Input input, boolean mapped)
/* 447:    */     throws IOException
/* 448:    */   {
/* 449:561 */     return resolveClass(input.readString());
/* 450:    */   }
/* 451:    */   
/* 452:    */   static Class<?> resolveClass(String className)
/* 453:    */   {
/* 454:567 */     RuntimeFieldFactory<Object> inline = RuntimeFieldFactory.getInline(className);
/* 455:569 */     if (inline == null) {
/* 456:570 */       return RuntimeEnv.loadClass(className);
/* 457:    */     }
/* 458:572 */     if (className.indexOf('.') != -1) {
/* 459:573 */       return inline.typeClass();
/* 460:    */     }
/* 461:575 */     switch (inline.id)
/* 462:    */     {
/* 463:    */     case 1: 
/* 464:578 */       return Boolean.TYPE;
/* 465:    */     case 2: 
/* 466:580 */       return Byte.TYPE;
/* 467:    */     case 3: 
/* 468:582 */       return Character.TYPE;
/* 469:    */     case 4: 
/* 470:584 */       return Short.TYPE;
/* 471:    */     case 5: 
/* 472:586 */       return Integer.TYPE;
/* 473:    */     case 6: 
/* 474:588 */       return Long.TYPE;
/* 475:    */     case 7: 
/* 476:590 */       return Float.TYPE;
/* 477:    */     case 8: 
/* 478:592 */       return Double.TYPE;
/* 479:    */     }
/* 480:594 */     throw new RuntimeException("Should never happen.");
/* 481:    */   }
/* 482:    */   
/* 483:    */   protected void writeClassIdTo(Output output, Class<?> componentType, boolean array)
/* 484:    */     throws IOException
/* 485:    */   {
/* 486:602 */     int id = array ? 20 : 18;
/* 487:    */     
/* 488:    */ 
/* 489:605 */     output.writeString(id, componentType.getName(), false);
/* 490:    */   }
/* 491:    */   
/* 492:    */   protected void transferClassId(Input input, Output output, int fieldNumber, boolean mapped, boolean array)
/* 493:    */     throws IOException
/* 494:    */   {
/* 495:612 */     input.transferByteRangeTo(output, true, fieldNumber, false);
/* 496:    */   }
/* 497:    */   
/* 498:    */   protected Class<?> resolveClassFrom(Input input, boolean mapped, boolean array)
/* 499:    */     throws IOException
/* 500:    */   {
/* 501:619 */     return resolveClass(input.readString());
/* 502:    */   }
/* 503:    */   
/* 504:    */   static final class RuntimeCollectionFactory
/* 505:    */     implements CollectionSchema.MessageFactory
/* 506:    */   {
/* 507:    */     final Class<?> collectionClass;
/* 508:    */     final RuntimeEnv.Instantiator<?> instantiator;
/* 509:    */     
/* 510:    */     public RuntimeCollectionFactory(Class<?> collectionClass)
/* 511:    */     {
/* 512:631 */       this.collectionClass = collectionClass;
/* 513:632 */       this.instantiator = RuntimeEnv.newInstantiator(collectionClass);
/* 514:    */     }
/* 515:    */     
/* 516:    */     public <V> Collection<V> newMessage()
/* 517:    */     {
/* 518:639 */       return (Collection)this.instantiator.newInstance();
/* 519:    */     }
/* 520:    */     
/* 521:    */     public Class<?> typeClass()
/* 522:    */     {
/* 523:645 */       return this.collectionClass;
/* 524:    */     }
/* 525:    */   }
/* 526:    */   
/* 527:    */   static final class RuntimeMapFactory
/* 528:    */     implements MapSchema.MessageFactory
/* 529:    */   {
/* 530:    */     final Class<?> mapClass;
/* 531:    */     final RuntimeEnv.Instantiator<?> instantiator;
/* 532:    */     
/* 533:    */     public RuntimeMapFactory(Class<?> mapClass)
/* 534:    */     {
/* 535:657 */       this.mapClass = mapClass;
/* 536:658 */       this.instantiator = RuntimeEnv.newInstantiator(mapClass);
/* 537:    */     }
/* 538:    */     
/* 539:    */     public <K, V> Map<K, V> newMessage()
/* 540:    */     {
/* 541:665 */       return (Map)this.instantiator.newInstance();
/* 542:    */     }
/* 543:    */     
/* 544:    */     public Class<?> typeClass()
/* 545:    */     {
/* 546:671 */       return this.mapClass;
/* 547:    */     }
/* 548:    */   }
/* 549:    */   
/* 550:    */   static final class Lazy<T>
/* 551:    */     extends HasSchema<T>
/* 552:    */   {
/* 553:    */     final Class<T> typeClass;
/* 554:    */     private volatile Schema<T> schema;
/* 555:    */     private volatile Pipe.Schema<T> pipeSchema;
/* 556:    */     
/* 557:    */     Lazy(Class<T> typeClass, IdStrategy strategy)
/* 558:    */     {
/* 559:684 */       super();
/* 560:685 */       this.typeClass = typeClass;
/* 561:    */     }
/* 562:    */     
/* 563:    */     public Schema<T> getSchema()
/* 564:    */     {
/* 565:692 */       Schema<T> schema = this.schema;
/* 566:693 */       if (schema == null) {
/* 567:695 */         synchronized (this)
/* 568:    */         {
/* 569:697 */           if ((schema = this.schema) == null) {
/* 570:699 */             if (Message.class.isAssignableFrom(this.typeClass))
/* 571:    */             {
/* 572:702 */               Message<T> m = (Message)IdStrategy.createMessageInstance(this.typeClass);
/* 573:703 */               this.schema = (schema = m.cachedSchema());
/* 574:    */             }
/* 575:    */             else
/* 576:    */             {
/* 577:709 */               this.schema = (schema = this.strategy.newSchema(this.typeClass));
/* 578:    */             }
/* 579:    */           }
/* 580:    */         }
/* 581:    */       }
/* 582:715 */       return schema;
/* 583:    */     }
/* 584:    */     
/* 585:    */     public Pipe.Schema<T> getPipeSchema()
/* 586:    */     {
/* 587:721 */       Pipe.Schema<T> pipeSchema = this.pipeSchema;
/* 588:722 */       if (pipeSchema == null) {
/* 589:724 */         synchronized (this)
/* 590:    */         {
/* 591:726 */           if ((pipeSchema = this.pipeSchema) == null) {
/* 592:729 */             this.pipeSchema = (pipeSchema = RuntimeSchema.resolvePipeSchema(getSchema(), this.typeClass, true));
/* 593:    */           }
/* 594:    */         }
/* 595:    */       }
/* 596:733 */       return pipeSchema;
/* 597:    */     }
/* 598:    */   }
/* 599:    */   
/* 600:    */   static final class Mapped<T>
/* 601:    */     extends HasSchema<T>
/* 602:    */   {
/* 603:    */     final Class<? super T> baseClass;
/* 604:    */     final Class<T> typeClass;
/* 605:    */     private volatile HasSchema<T> wrapper;
/* 606:    */     
/* 607:    */     Mapped(Class<? super T> baseClass, Class<T> typeClass, IdStrategy strategy)
/* 608:    */     {
/* 609:747 */       super();
/* 610:748 */       this.baseClass = baseClass;
/* 611:749 */       this.typeClass = typeClass;
/* 612:    */     }
/* 613:    */     
/* 614:    */     public Schema<T> getSchema()
/* 615:    */     {
/* 616:755 */       HasSchema<T> wrapper = this.wrapper;
/* 617:756 */       if (wrapper == null) {
/* 618:758 */         synchronized (this)
/* 619:    */         {
/* 620:760 */           if ((wrapper = this.wrapper) == null) {
/* 621:762 */             this.wrapper = (wrapper = this.strategy.getSchemaWrapper(this.typeClass, true));
/* 622:    */           }
/* 623:    */         }
/* 624:    */       }
/* 625:768 */       return wrapper.getSchema();
/* 626:    */     }
/* 627:    */     
/* 628:    */     public Pipe.Schema<T> getPipeSchema()
/* 629:    */     {
/* 630:774 */       HasSchema<T> wrapper = this.wrapper;
/* 631:775 */       if (wrapper == null) {
/* 632:777 */         synchronized (this)
/* 633:    */         {
/* 634:779 */           if ((wrapper = this.wrapper) == null) {
/* 635:781 */             this.wrapper = (wrapper = this.strategy.getSchemaWrapper(this.typeClass, true));
/* 636:    */           }
/* 637:    */         }
/* 638:    */       }
/* 639:787 */       return wrapper.getPipeSchema();
/* 640:    */     }
/* 641:    */   }
/* 642:    */   
/* 643:    */   static final class LazyRegister<T>
/* 644:    */     extends HasSchema<T>
/* 645:    */   {
/* 646:    */     final Class<T> typeClass;
/* 647:    */     private volatile Schema<T> schema;
/* 648:    */     private volatile Pipe.Schema<T> pipeSchema;
/* 649:    */     
/* 650:    */     LazyRegister(Class<T> typeClass, IdStrategy strategy)
/* 651:    */     {
/* 652:800 */       super();
/* 653:801 */       this.typeClass = typeClass;
/* 654:    */     }
/* 655:    */     
/* 656:    */     public Schema<T> getSchema()
/* 657:    */     {
/* 658:807 */       Schema<T> schema = this.schema;
/* 659:808 */       if (schema == null) {
/* 660:810 */         synchronized (this)
/* 661:    */         {
/* 662:812 */           if ((schema = this.schema) == null) {
/* 663:815 */             this.schema = (schema = this.strategy.newSchema(this.typeClass));
/* 664:    */           }
/* 665:    */         }
/* 666:    */       }
/* 667:820 */       return schema;
/* 668:    */     }
/* 669:    */     
/* 670:    */     public Pipe.Schema<T> getPipeSchema()
/* 671:    */     {
/* 672:826 */       Pipe.Schema<T> pipeSchema = this.pipeSchema;
/* 673:827 */       if (pipeSchema == null) {
/* 674:829 */         synchronized (this)
/* 675:    */         {
/* 676:831 */           if ((pipeSchema = this.pipeSchema) == null) {
/* 677:834 */             this.pipeSchema = (pipeSchema = RuntimeSchema.resolvePipeSchema(getSchema(), this.typeClass, true));
/* 678:    */           }
/* 679:    */         }
/* 680:    */       }
/* 681:839 */       return pipeSchema;
/* 682:    */     }
/* 683:    */   }
/* 684:    */   
/* 685:    */   static final class Registered<T>
/* 686:    */     extends HasSchema<T>
/* 687:    */   {
/* 688:    */     final Schema<T> schema;
/* 689:    */     private volatile Pipe.Schema<T> pipeSchema;
/* 690:    */     
/* 691:    */     Registered(Schema<T> schema, IdStrategy strategy)
/* 692:    */     {
/* 693:850 */       super();
/* 694:851 */       this.schema = schema;
/* 695:    */     }
/* 696:    */     
/* 697:    */     public Schema<T> getSchema()
/* 698:    */     {
/* 699:857 */       return this.schema;
/* 700:    */     }
/* 701:    */     
/* 702:    */     public Pipe.Schema<T> getPipeSchema()
/* 703:    */     {
/* 704:863 */       Pipe.Schema<T> pipeSchema = this.pipeSchema;
/* 705:864 */       if (pipeSchema == null) {
/* 706:866 */         synchronized (this)
/* 707:    */         {
/* 708:868 */           if ((pipeSchema = this.pipeSchema) == null) {
/* 709:871 */             this.pipeSchema = (pipeSchema = RuntimeSchema.resolvePipeSchema(this.schema, this.schema.typeClass(), true));
/* 710:    */           }
/* 711:    */         }
/* 712:    */       }
/* 713:876 */       return pipeSchema;
/* 714:    */     }
/* 715:    */   }
/* 716:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.DefaultIdStrategy
 * JD-Core Version:    0.7.0.1
 */