/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.lang.reflect.Constructor;
/*   6:    */ import java.lang.reflect.Field;
/*   7:    */ import java.lang.reflect.InvocationTargetException;
/*   8:    */ import java.lang.reflect.Method;
/*   9:    */ import java.nio.Buffer;
/*  10:    */ import java.nio.ByteBuffer;
/*  11:    */ import java.security.AccessController;
/*  12:    */ import java.security.PrivilegedAction;
/*  13:    */ import sun.misc.Unsafe;
/*  14:    */ 
/*  15:    */ final class PlatformDependent0
/*  16:    */ {
/*  17:    */   private static final InternalLogger logger;
/*  18:    */   private static final long ADDRESS_FIELD_OFFSET;
/*  19:    */   private static final long BYTE_ARRAY_BASE_OFFSET;
/*  20:    */   private static final Constructor<?> DIRECT_BUFFER_CONSTRUCTOR;
/*  21:    */   private static final boolean IS_EXPLICIT_NO_UNSAFE;
/*  22:    */   private static final Method ALLOCATE_ARRAY_METHOD;
/*  23:    */   private static final int JAVA_VERSION;
/*  24:    */   private static final boolean IS_ANDROID;
/*  25:    */   private static final Throwable UNSAFE_UNAVAILABILITY_CAUSE;
/*  26:    */   private static final Object INTERNAL_UNSAFE;
/*  27:    */   static final Unsafe UNSAFE;
/*  28:    */   static final int HASH_CODE_ASCII_SEED = -1028477387;
/*  29:    */   static final int HASH_CODE_C1 = -862048943;
/*  30:    */   static final int HASH_CODE_C2 = 461845907;
/*  31:    */   private static final long UNSAFE_COPY_THRESHOLD = 1048576L;
/*  32:    */   private static final boolean UNALIGNED;
/*  33:    */   
/*  34:    */   static
/*  35:    */   {
/*  36: 38 */     logger = InternalLoggerFactory.getInstance(PlatformDependent0.class);
/*  37:    */     
/*  38:    */ 
/*  39:    */ 
/*  40: 42 */     IS_EXPLICIT_NO_UNSAFE = explicitNoUnsafe0();
/*  41:    */     
/*  42: 44 */     JAVA_VERSION = javaVersion0();
/*  43: 45 */     IS_ANDROID = isAndroid0();
/*  44:    */     
/*  45:    */ 
/*  46:    */ 
/*  47:    */ 
/*  48:    */ 
/*  49:    */ 
/*  50:    */ 
/*  51:    */ 
/*  52:    */ 
/*  53:    */ 
/*  54:    */ 
/*  55:    */ 
/*  56:    */ 
/*  57:    */ 
/*  58:    */ 
/*  59:    */ 
/*  60:    */ 
/*  61:    */ 
/*  62:    */ 
/*  63:    */ 
/*  64: 66 */     Field addressField = null;
/*  65: 67 */     Method allocateArrayMethod = null;
/*  66: 68 */     Throwable unsafeUnavailabilityCause = null;
/*  67:    */     
/*  68: 70 */     Object internalUnsafe = null;
/*  69:    */     final ByteBuffer direct;
/*  70:    */     Unsafe unsafe;
/*  71: 72 */     if (isExplicitNoUnsafe())
/*  72:    */     {
/*  73: 73 */       ByteBuffer direct = null;
/*  74: 74 */       addressField = null;
/*  75: 75 */       unsafeUnavailabilityCause = new UnsupportedOperationException("Unsafe explicitly disabled");
/*  76: 76 */       Unsafe unsafe = null;
/*  77: 77 */       internalUnsafe = null;
/*  78:    */     }
/*  79:    */     else
/*  80:    */     {
/*  81: 79 */       direct = ByteBuffer.allocateDirect(1);
/*  82:    */       
/*  83:    */ 
/*  84: 82 */       Object maybeUnsafe = AccessController.doPrivileged(new PrivilegedAction()
/*  85:    */       {
/*  86:    */         public Object run()
/*  87:    */         {
/*  88:    */           try
/*  89:    */           {
/*  90: 86 */             Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
/*  91: 87 */             Throwable cause = ReflectionUtil.trySetAccessible(unsafeField);
/*  92: 88 */             if (cause != null) {
/*  93: 89 */               return cause;
/*  94:    */             }
/*  95: 92 */             return unsafeField.get(null);
/*  96:    */           }
/*  97:    */           catch (NoSuchFieldException e)
/*  98:    */           {
/*  99: 94 */             return e;
/* 100:    */           }
/* 101:    */           catch (SecurityException e)
/* 102:    */           {
/* 103: 96 */             return e;
/* 104:    */           }
/* 105:    */           catch (IllegalAccessException e)
/* 106:    */           {
/* 107: 98 */             return e;
/* 108:    */           }
/* 109:    */           catch (NoClassDefFoundError e)
/* 110:    */           {
/* 111:102 */             return e;
/* 112:    */           }
/* 113:    */         }
/* 114:    */       });
/* 115:111 */       if ((maybeUnsafe instanceof Throwable))
/* 116:    */       {
/* 117:112 */         Unsafe unsafe = null;
/* 118:113 */         unsafeUnavailabilityCause = (Throwable)maybeUnsafe;
/* 119:114 */         logger.debug("sun.misc.Unsafe.theUnsafe: unavailable", (Throwable)maybeUnsafe);
/* 120:    */       }
/* 121:    */       else
/* 122:    */       {
/* 123:116 */         unsafe = (Unsafe)maybeUnsafe;
/* 124:117 */         logger.debug("sun.misc.Unsafe.theUnsafe: available");
/* 125:    */       }
/* 126:123 */       if (unsafe != null)
/* 127:    */       {
/* 128:124 */         Unsafe finalUnsafe = unsafe;
/* 129:125 */         Object maybeException = AccessController.doPrivileged(new PrivilegedAction()
/* 130:    */         {
/* 131:    */           public Object run()
/* 132:    */           {
/* 133:    */             try
/* 134:    */             {
/* 135:129 */               this.val$finalUnsafe.getClass().getDeclaredMethod("copyMemory", new Class[] { Object.class, Long.TYPE, Object.class, Long.TYPE, Long.TYPE });
/* 136:    */               
/* 137:131 */               return null;
/* 138:    */             }
/* 139:    */             catch (NoSuchMethodException e)
/* 140:    */             {
/* 141:133 */               return e;
/* 142:    */             }
/* 143:    */             catch (SecurityException e)
/* 144:    */             {
/* 145:135 */               return e;
/* 146:    */             }
/* 147:    */           }
/* 148:    */         });
/* 149:140 */         if (maybeException == null)
/* 150:    */         {
/* 151:141 */           logger.debug("sun.misc.Unsafe.copyMemory: available");
/* 152:    */         }
/* 153:    */         else
/* 154:    */         {
/* 155:144 */           unsafe = null;
/* 156:145 */           unsafeUnavailabilityCause = (Throwable)maybeException;
/* 157:146 */           logger.debug("sun.misc.Unsafe.copyMemory: unavailable", (Throwable)maybeException);
/* 158:    */         }
/* 159:    */       }
/* 160:150 */       if (unsafe != null)
/* 161:    */       {
/* 162:151 */         Unsafe finalUnsafe = unsafe;
/* 163:    */         
/* 164:    */ 
/* 165:154 */         Object maybeAddressField = AccessController.doPrivileged(new PrivilegedAction()
/* 166:    */         {
/* 167:    */           public Object run()
/* 168:    */           {
/* 169:    */             try
/* 170:    */             {
/* 171:158 */               Field field = Buffer.class.getDeclaredField("address");
/* 172:    */               
/* 173:    */ 
/* 174:161 */               long offset = this.val$finalUnsafe.objectFieldOffset(field);
/* 175:162 */               long address = this.val$finalUnsafe.getLong(direct, offset);
/* 176:165 */               if (address == 0L) {
/* 177:166 */                 return null;
/* 178:    */               }
/* 179:168 */               return field;
/* 180:    */             }
/* 181:    */             catch (NoSuchFieldException e)
/* 182:    */             {
/* 183:170 */               return e;
/* 184:    */             }
/* 185:    */             catch (SecurityException e)
/* 186:    */             {
/* 187:172 */               return e;
/* 188:    */             }
/* 189:    */           }
/* 190:    */         });
/* 191:177 */         if ((maybeAddressField instanceof Field))
/* 192:    */         {
/* 193:178 */           addressField = (Field)maybeAddressField;
/* 194:179 */           logger.debug("java.nio.Buffer.address: available");
/* 195:    */         }
/* 196:    */         else
/* 197:    */         {
/* 198:181 */           unsafeUnavailabilityCause = (Throwable)maybeAddressField;
/* 199:182 */           logger.debug("java.nio.Buffer.address: unavailable", (Throwable)maybeAddressField);
/* 200:    */           
/* 201:    */ 
/* 202:    */ 
/* 203:186 */           unsafe = null;
/* 204:    */         }
/* 205:    */       }
/* 206:190 */       if (unsafe != null)
/* 207:    */       {
/* 208:193 */         long byteArrayIndexScale = unsafe.arrayIndexScale([B.class);
/* 209:194 */         if (byteArrayIndexScale != 1L)
/* 210:    */         {
/* 211:195 */           logger.debug("unsafe.arrayIndexScale is {} (expected: 1). Not using unsafe.", Long.valueOf(byteArrayIndexScale));
/* 212:196 */           unsafeUnavailabilityCause = new UnsupportedOperationException("Unexpected unsafe.arrayIndexScale");
/* 213:197 */           unsafe = null;
/* 214:    */         }
/* 215:    */       }
/* 216:    */     }
/* 217:201 */     UNSAFE_UNAVAILABILITY_CAUSE = unsafeUnavailabilityCause;
/* 218:202 */     UNSAFE = unsafe;
/* 219:204 */     if (unsafe == null)
/* 220:    */     {
/* 221:205 */       ADDRESS_FIELD_OFFSET = -1L;
/* 222:206 */       BYTE_ARRAY_BASE_OFFSET = -1L;
/* 223:207 */       UNALIGNED = false;
/* 224:208 */       DIRECT_BUFFER_CONSTRUCTOR = null;
/* 225:209 */       ALLOCATE_ARRAY_METHOD = null;
/* 226:    */     }
/* 227:    */     else
/* 228:    */     {
/* 229:212 */       long address = -1L;
/* 230:    */       try
/* 231:    */       {
/* 232:215 */         Object maybeDirectBufferConstructor = AccessController.doPrivileged(new PrivilegedAction()
/* 233:    */         {
/* 234:    */           public Object run()
/* 235:    */           {
/* 236:    */             try
/* 237:    */             {
/* 238:220 */               Constructor<?> constructor = this.val$direct.getClass().getDeclaredConstructor(new Class[] { Long.TYPE, Integer.TYPE });
/* 239:221 */               Throwable cause = ReflectionUtil.trySetAccessible(constructor);
/* 240:222 */               if (cause != null) {
/* 241:223 */                 return cause;
/* 242:    */               }
/* 243:225 */               return constructor;
/* 244:    */             }
/* 245:    */             catch (NoSuchMethodException e)
/* 246:    */             {
/* 247:227 */               return e;
/* 248:    */             }
/* 249:    */             catch (SecurityException e)
/* 250:    */             {
/* 251:229 */               return e;
/* 252:    */             }
/* 253:    */           }
/* 254:    */         });
/* 255:    */         Constructor<?> directBufferConstructor;
/* 256:234 */         if ((maybeDirectBufferConstructor instanceof Constructor))
/* 257:    */         {
/* 258:235 */           address = UNSAFE.allocateMemory(1L);
/* 259:    */           Constructor<?> directBufferConstructor;
/* 260:    */           try
/* 261:    */           {
/* 262:238 */             ((Constructor)maybeDirectBufferConstructor).newInstance(new Object[] { Long.valueOf(address), Integer.valueOf(1) });
/* 263:239 */             Constructor<?> directBufferConstructor = (Constructor)maybeDirectBufferConstructor;
/* 264:240 */             logger.debug("direct buffer constructor: available");
/* 265:    */           }
/* 266:    */           catch (InstantiationException e)
/* 267:    */           {
/* 268:242 */             directBufferConstructor = null;
/* 269:    */           }
/* 270:    */           catch (IllegalAccessException e)
/* 271:    */           {
/* 272:    */             Constructor<?> directBufferConstructor;
/* 273:244 */             directBufferConstructor = null;
/* 274:    */           }
/* 275:    */           catch (InvocationTargetException e)
/* 276:    */           {
/* 277:    */             Constructor<?> directBufferConstructor;
/* 278:246 */             directBufferConstructor = null;
/* 279:    */           }
/* 280:    */         }
/* 281:    */         else
/* 282:    */         {
/* 283:249 */           logger.debug("direct buffer constructor: unavailable", (Throwable)maybeDirectBufferConstructor);
/* 284:    */           
/* 285:    */ 
/* 286:252 */           directBufferConstructor = null;
/* 287:    */         }
/* 288:255 */         if (address != -1L) {
/* 289:256 */           UNSAFE.freeMemory(address);
/* 290:    */         }
/* 291:    */       }
/* 292:    */       finally
/* 293:    */       {
/* 294:255 */         if (address != -1L) {
/* 295:256 */           UNSAFE.freeMemory(address);
/* 296:    */         }
/* 297:    */       }
/* 298:    */       Constructor<?> directBufferConstructor;
/* 299:259 */       DIRECT_BUFFER_CONSTRUCTOR = directBufferConstructor;
/* 300:260 */       ADDRESS_FIELD_OFFSET = objectFieldOffset(addressField);
/* 301:261 */       BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset([B.class);
/* 302:    */       
/* 303:263 */       Object maybeUnaligned = AccessController.doPrivileged(new PrivilegedAction()
/* 304:    */       {
/* 305:    */         public Object run()
/* 306:    */         {
/* 307:    */           try
/* 308:    */           {
/* 309:268 */             Class<?> bitsClass = Class.forName("java.nio.Bits", false, PlatformDependent0.getSystemClassLoader());
/* 310:269 */             Method unalignedMethod = bitsClass.getDeclaredMethod("unaligned", new Class[0]);
/* 311:270 */             Throwable cause = ReflectionUtil.trySetAccessible(unalignedMethod);
/* 312:271 */             if (cause != null) {
/* 313:272 */               return cause;
/* 314:    */             }
/* 315:274 */             return unalignedMethod.invoke(null, new Object[0]);
/* 316:    */           }
/* 317:    */           catch (NoSuchMethodException e)
/* 318:    */           {
/* 319:276 */             return e;
/* 320:    */           }
/* 321:    */           catch (SecurityException e)
/* 322:    */           {
/* 323:278 */             return e;
/* 324:    */           }
/* 325:    */           catch (IllegalAccessException e)
/* 326:    */           {
/* 327:280 */             return e;
/* 328:    */           }
/* 329:    */           catch (ClassNotFoundException e)
/* 330:    */           {
/* 331:282 */             return e;
/* 332:    */           }
/* 333:    */           catch (InvocationTargetException e)
/* 334:    */           {
/* 335:284 */             return e;
/* 336:    */           }
/* 337:    */         }
/* 338:    */       });
/* 339:    */       boolean unaligned;
/* 340:289 */       if ((maybeUnaligned instanceof Boolean))
/* 341:    */       {
/* 342:290 */         boolean unaligned = ((Boolean)maybeUnaligned).booleanValue();
/* 343:291 */         logger.debug("java.nio.Bits.unaligned: available, {}", Boolean.valueOf(unaligned));
/* 344:    */       }
/* 345:    */       else
/* 346:    */       {
/* 347:293 */         String arch = SystemPropertyUtil.get("os.arch", "");
/* 348:    */         
/* 349:295 */         unaligned = arch.matches("^(i[3-6]86|x86(_64)?|x64|amd64)$");
/* 350:296 */         Throwable t = (Throwable)maybeUnaligned;
/* 351:297 */         logger.debug("java.nio.Bits.unaligned: unavailable {}", Boolean.valueOf(unaligned), t);
/* 352:    */       }
/* 353:300 */       UNALIGNED = unaligned;
/* 354:302 */       if (javaVersion() >= 9)
/* 355:    */       {
/* 356:303 */         Object maybeException = AccessController.doPrivileged(new PrivilegedAction()
/* 357:    */         {
/* 358:    */           public Object run()
/* 359:    */           {
/* 360:    */             try
/* 361:    */             {
/* 362:310 */               Class<?> internalUnsafeClass = PlatformDependent0.getClassLoader(PlatformDependent0.class).loadClass("jdk.internal.misc.Unsafe");
/* 363:311 */               Method method = internalUnsafeClass.getDeclaredMethod("getUnsafe", new Class[0]);
/* 364:312 */               return method.invoke(null, new Object[0]);
/* 365:    */             }
/* 366:    */             catch (Throwable e)
/* 367:    */             {
/* 368:314 */               return e;
/* 369:    */             }
/* 370:    */           }
/* 371:    */         });
/* 372:318 */         if (!(maybeException instanceof Throwable))
/* 373:    */         {
/* 374:319 */           internalUnsafe = maybeException;
/* 375:320 */           Object finalInternalUnsafe = internalUnsafe;
/* 376:321 */           maybeException = AccessController.doPrivileged(new PrivilegedAction()
/* 377:    */           {
/* 378:    */             public Object run()
/* 379:    */             {
/* 380:    */               try
/* 381:    */               {
/* 382:325 */                 return this.val$finalInternalUnsafe.getClass().getDeclaredMethod("allocateUninitializedArray", new Class[] { Class.class, Integer.TYPE });
/* 383:    */               }
/* 384:    */               catch (NoSuchMethodException e)
/* 385:    */               {
/* 386:328 */                 return e;
/* 387:    */               }
/* 388:    */               catch (SecurityException e)
/* 389:    */               {
/* 390:330 */                 return e;
/* 391:    */               }
/* 392:    */             }
/* 393:    */           });
/* 394:335 */           if ((maybeException instanceof Method)) {
/* 395:    */             try
/* 396:    */             {
/* 397:337 */               Method m = (Method)maybeException;
/* 398:338 */               byte[] bytes = (byte[])m.invoke(finalInternalUnsafe, new Object[] { Byte.TYPE, Integer.valueOf(8) });
/* 399:339 */               assert (bytes.length == 8);
/* 400:340 */               allocateArrayMethod = m;
/* 401:    */             }
/* 402:    */             catch (IllegalAccessException e)
/* 403:    */             {
/* 404:342 */               maybeException = e;
/* 405:    */             }
/* 406:    */             catch (InvocationTargetException e)
/* 407:    */             {
/* 408:344 */               maybeException = e;
/* 409:    */             }
/* 410:    */           }
/* 411:    */         }
/* 412:349 */         if ((maybeException instanceof Throwable)) {
/* 413:350 */           logger.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable", (Throwable)maybeException);
/* 414:    */         } else {
/* 415:353 */           logger.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): available");
/* 416:    */         }
/* 417:    */       }
/* 418:    */       else
/* 419:    */       {
/* 420:356 */         logger.debug("jdk.internal.misc.Unsafe.allocateUninitializedArray(int): unavailable prior to Java9");
/* 421:    */       }
/* 422:358 */       ALLOCATE_ARRAY_METHOD = allocateArrayMethod;
/* 423:    */     }
/* 424:361 */     INTERNAL_UNSAFE = internalUnsafe;
/* 425:    */     
/* 426:363 */     logger.debug("java.nio.DirectByteBuffer.<init>(long, int): {}", DIRECT_BUFFER_CONSTRUCTOR != null ? "available" : "unavailable");
/* 427:    */   }
/* 428:    */   
/* 429:    */   static boolean isExplicitNoUnsafe()
/* 430:    */   {
/* 431:368 */     return IS_EXPLICIT_NO_UNSAFE;
/* 432:    */   }
/* 433:    */   
/* 434:    */   private static boolean explicitNoUnsafe0()
/* 435:    */   {
/* 436:372 */     boolean noUnsafe = SystemPropertyUtil.getBoolean("io.netty.noUnsafe", false);
/* 437:373 */     logger.debug("-Dio.netty.noUnsafe: {}", Boolean.valueOf(noUnsafe));
/* 438:375 */     if (noUnsafe)
/* 439:    */     {
/* 440:376 */       logger.debug("sun.misc.Unsafe: unavailable (io.netty.noUnsafe)");
/* 441:377 */       return true;
/* 442:    */     }
/* 443:    */     boolean tryUnsafe;
/* 444:    */     boolean tryUnsafe;
/* 445:382 */     if (SystemPropertyUtil.contains("io.netty.tryUnsafe")) {
/* 446:383 */       tryUnsafe = SystemPropertyUtil.getBoolean("io.netty.tryUnsafe", true);
/* 447:    */     } else {
/* 448:385 */       tryUnsafe = SystemPropertyUtil.getBoolean("org.jboss.netty.tryUnsafe", true);
/* 449:    */     }
/* 450:388 */     if (!tryUnsafe)
/* 451:    */     {
/* 452:389 */       logger.debug("sun.misc.Unsafe: unavailable (io.netty.tryUnsafe/org.jboss.netty.tryUnsafe)");
/* 453:390 */       return true;
/* 454:    */     }
/* 455:393 */     return false;
/* 456:    */   }
/* 457:    */   
/* 458:    */   static boolean isUnaligned()
/* 459:    */   {
/* 460:397 */     return UNALIGNED;
/* 461:    */   }
/* 462:    */   
/* 463:    */   static boolean hasUnsafe()
/* 464:    */   {
/* 465:401 */     return UNSAFE != null;
/* 466:    */   }
/* 467:    */   
/* 468:    */   static Throwable getUnsafeUnavailabilityCause()
/* 469:    */   {
/* 470:405 */     return UNSAFE_UNAVAILABILITY_CAUSE;
/* 471:    */   }
/* 472:    */   
/* 473:    */   static boolean unalignedAccess()
/* 474:    */   {
/* 475:409 */     return UNALIGNED;
/* 476:    */   }
/* 477:    */   
/* 478:    */   static void throwException(Throwable cause)
/* 479:    */   {
/* 480:414 */     UNSAFE.throwException((Throwable)ObjectUtil.checkNotNull(cause, "cause"));
/* 481:    */   }
/* 482:    */   
/* 483:    */   static boolean hasDirectBufferNoCleanerConstructor()
/* 484:    */   {
/* 485:418 */     return DIRECT_BUFFER_CONSTRUCTOR != null;
/* 486:    */   }
/* 487:    */   
/* 488:    */   static ByteBuffer reallocateDirectNoCleaner(ByteBuffer buffer, int capacity)
/* 489:    */   {
/* 490:422 */     return newDirectBuffer(UNSAFE.reallocateMemory(directBufferAddress(buffer), capacity), capacity);
/* 491:    */   }
/* 492:    */   
/* 493:    */   static ByteBuffer allocateDirectNoCleaner(int capacity)
/* 494:    */   {
/* 495:426 */     return newDirectBuffer(UNSAFE.allocateMemory(capacity), capacity);
/* 496:    */   }
/* 497:    */   
/* 498:    */   static boolean hasAllocateArrayMethod()
/* 499:    */   {
/* 500:430 */     return ALLOCATE_ARRAY_METHOD != null;
/* 501:    */   }
/* 502:    */   
/* 503:    */   static byte[] allocateUninitializedArray(int size)
/* 504:    */   {
/* 505:    */     try
/* 506:    */     {
/* 507:435 */       return (byte[])ALLOCATE_ARRAY_METHOD.invoke(INTERNAL_UNSAFE, new Object[] { Byte.TYPE, Integer.valueOf(size) });
/* 508:    */     }
/* 509:    */     catch (IllegalAccessException e)
/* 510:    */     {
/* 511:437 */       throw new Error(e);
/* 512:    */     }
/* 513:    */     catch (InvocationTargetException e)
/* 514:    */     {
/* 515:439 */       throw new Error(e);
/* 516:    */     }
/* 517:    */   }
/* 518:    */   
/* 519:    */   static ByteBuffer newDirectBuffer(long address, int capacity)
/* 520:    */   {
/* 521:444 */     ObjectUtil.checkPositiveOrZero(capacity, "capacity");
/* 522:    */     try
/* 523:    */     {
/* 524:447 */       return (ByteBuffer)DIRECT_BUFFER_CONSTRUCTOR.newInstance(new Object[] { Long.valueOf(address), Integer.valueOf(capacity) });
/* 525:    */     }
/* 526:    */     catch (Throwable cause)
/* 527:    */     {
/* 528:450 */       if ((cause instanceof Error)) {
/* 529:451 */         throw ((Error)cause);
/* 530:    */       }
/* 531:453 */       throw new Error(cause);
/* 532:    */     }
/* 533:    */   }
/* 534:    */   
/* 535:    */   static long directBufferAddress(ByteBuffer buffer)
/* 536:    */   {
/* 537:458 */     return getLong(buffer, ADDRESS_FIELD_OFFSET);
/* 538:    */   }
/* 539:    */   
/* 540:    */   static long byteArrayBaseOffset()
/* 541:    */   {
/* 542:462 */     return BYTE_ARRAY_BASE_OFFSET;
/* 543:    */   }
/* 544:    */   
/* 545:    */   static Object getObject(Object object, long fieldOffset)
/* 546:    */   {
/* 547:466 */     return UNSAFE.getObject(object, fieldOffset);
/* 548:    */   }
/* 549:    */   
/* 550:    */   static int getInt(Object object, long fieldOffset)
/* 551:    */   {
/* 552:470 */     return UNSAFE.getInt(object, fieldOffset);
/* 553:    */   }
/* 554:    */   
/* 555:    */   private static long getLong(Object object, long fieldOffset)
/* 556:    */   {
/* 557:474 */     return UNSAFE.getLong(object, fieldOffset);
/* 558:    */   }
/* 559:    */   
/* 560:    */   static long objectFieldOffset(Field field)
/* 561:    */   {
/* 562:478 */     return UNSAFE.objectFieldOffset(field);
/* 563:    */   }
/* 564:    */   
/* 565:    */   static byte getByte(long address)
/* 566:    */   {
/* 567:482 */     return UNSAFE.getByte(address);
/* 568:    */   }
/* 569:    */   
/* 570:    */   static short getShort(long address)
/* 571:    */   {
/* 572:486 */     return UNSAFE.getShort(address);
/* 573:    */   }
/* 574:    */   
/* 575:    */   static int getInt(long address)
/* 576:    */   {
/* 577:490 */     return UNSAFE.getInt(address);
/* 578:    */   }
/* 579:    */   
/* 580:    */   static long getLong(long address)
/* 581:    */   {
/* 582:494 */     return UNSAFE.getLong(address);
/* 583:    */   }
/* 584:    */   
/* 585:    */   static byte getByte(byte[] data, int index)
/* 586:    */   {
/* 587:498 */     return UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index);
/* 588:    */   }
/* 589:    */   
/* 590:    */   static short getShort(byte[] data, int index)
/* 591:    */   {
/* 592:502 */     return UNSAFE.getShort(data, BYTE_ARRAY_BASE_OFFSET + index);
/* 593:    */   }
/* 594:    */   
/* 595:    */   static int getInt(byte[] data, int index)
/* 596:    */   {
/* 597:506 */     return UNSAFE.getInt(data, BYTE_ARRAY_BASE_OFFSET + index);
/* 598:    */   }
/* 599:    */   
/* 600:    */   static long getLong(byte[] data, int index)
/* 601:    */   {
/* 602:510 */     return UNSAFE.getLong(data, BYTE_ARRAY_BASE_OFFSET + index);
/* 603:    */   }
/* 604:    */   
/* 605:    */   static void putByte(long address, byte value)
/* 606:    */   {
/* 607:514 */     UNSAFE.putByte(address, value);
/* 608:    */   }
/* 609:    */   
/* 610:    */   static void putShort(long address, short value)
/* 611:    */   {
/* 612:518 */     UNSAFE.putShort(address, value);
/* 613:    */   }
/* 614:    */   
/* 615:    */   static void putInt(long address, int value)
/* 616:    */   {
/* 617:522 */     UNSAFE.putInt(address, value);
/* 618:    */   }
/* 619:    */   
/* 620:    */   static void putLong(long address, long value)
/* 621:    */   {
/* 622:526 */     UNSAFE.putLong(address, value);
/* 623:    */   }
/* 624:    */   
/* 625:    */   static void putByte(byte[] data, int index, byte value)
/* 626:    */   {
/* 627:530 */     UNSAFE.putByte(data, BYTE_ARRAY_BASE_OFFSET + index, value);
/* 628:    */   }
/* 629:    */   
/* 630:    */   static void putShort(byte[] data, int index, short value)
/* 631:    */   {
/* 632:534 */     UNSAFE.putShort(data, BYTE_ARRAY_BASE_OFFSET + index, value);
/* 633:    */   }
/* 634:    */   
/* 635:    */   static void putInt(byte[] data, int index, int value)
/* 636:    */   {
/* 637:538 */     UNSAFE.putInt(data, BYTE_ARRAY_BASE_OFFSET + index, value);
/* 638:    */   }
/* 639:    */   
/* 640:    */   static void putLong(byte[] data, int index, long value)
/* 641:    */   {
/* 642:542 */     UNSAFE.putLong(data, BYTE_ARRAY_BASE_OFFSET + index, value);
/* 643:    */   }
/* 644:    */   
/* 645:    */   static void copyMemory(long srcAddr, long dstAddr, long length)
/* 646:    */   {
/* 647:547 */     while (length > 0L)
/* 648:    */     {
/* 649:548 */       long size = Math.min(length, 1048576L);
/* 650:549 */       UNSAFE.copyMemory(srcAddr, dstAddr, size);
/* 651:550 */       length -= size;
/* 652:551 */       srcAddr += size;
/* 653:552 */       dstAddr += size;
/* 654:    */     }
/* 655:    */   }
/* 656:    */   
/* 657:    */   static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length)
/* 658:    */   {
/* 659:558 */     while (length > 0L)
/* 660:    */     {
/* 661:559 */       long size = Math.min(length, 1048576L);
/* 662:560 */       UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, size);
/* 663:561 */       length -= size;
/* 664:562 */       srcOffset += size;
/* 665:563 */       dstOffset += size;
/* 666:    */     }
/* 667:    */   }
/* 668:    */   
/* 669:    */   static void setMemory(long address, long bytes, byte value)
/* 670:    */   {
/* 671:568 */     UNSAFE.setMemory(address, bytes, value);
/* 672:    */   }
/* 673:    */   
/* 674:    */   static void setMemory(Object o, long offset, long bytes, byte value)
/* 675:    */   {
/* 676:572 */     UNSAFE.setMemory(o, offset, bytes, value);
/* 677:    */   }
/* 678:    */   
/* 679:    */   static boolean equals(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length)
/* 680:    */   {
/* 681:576 */     if (length <= 0) {
/* 682:577 */       return true;
/* 683:    */     }
/* 684:579 */     long baseOffset1 = BYTE_ARRAY_BASE_OFFSET + startPos1;
/* 685:580 */     long baseOffset2 = BYTE_ARRAY_BASE_OFFSET + startPos2;
/* 686:581 */     int remainingBytes = length & 0x7;
/* 687:582 */     long end = baseOffset1 + remainingBytes;
/* 688:583 */     long i = baseOffset1 - 8L + length;
/* 689:583 */     for (long j = baseOffset2 - 8L + length; i >= end; j -= 8L)
/* 690:    */     {
/* 691:584 */       if (UNSAFE.getLong(bytes1, i) != UNSAFE.getLong(bytes2, j)) {
/* 692:585 */         return false;
/* 693:    */       }
/* 694:583 */       i -= 8L;
/* 695:    */     }
/* 696:589 */     if (remainingBytes >= 4)
/* 697:    */     {
/* 698:590 */       remainingBytes -= 4;
/* 699:592 */       if (UNSAFE.getInt(bytes1, baseOffset1 + remainingBytes) != UNSAFE.getInt(bytes2, baseOffset2 + remainingBytes)) {
/* 700:593 */         return false;
/* 701:    */       }
/* 702:    */     }
/* 703:596 */     if (remainingBytes >= 2) {
/* 704:597 */       return (UNSAFE.getChar(bytes1, baseOffset1) == UNSAFE.getChar(bytes2, baseOffset2)) && ((remainingBytes == 2) || (bytes1[(startPos1 + 2)] == bytes2[(startPos2 + 2)]));
/* 705:    */     }
/* 706:600 */     return bytes1[startPos1] == bytes2[startPos2];
/* 707:    */   }
/* 708:    */   
/* 709:    */   static int equalsConstantTime(byte[] bytes1, int startPos1, byte[] bytes2, int startPos2, int length)
/* 710:    */   {
/* 711:604 */     long result = 0L;
/* 712:605 */     long baseOffset1 = BYTE_ARRAY_BASE_OFFSET + startPos1;
/* 713:606 */     long baseOffset2 = BYTE_ARRAY_BASE_OFFSET + startPos2;
/* 714:607 */     int remainingBytes = length & 0x7;
/* 715:608 */     long end = baseOffset1 + remainingBytes;
/* 716:609 */     long i = baseOffset1 - 8L + length;
/* 717:609 */     for (long j = baseOffset2 - 8L + length; i >= end; j -= 8L)
/* 718:    */     {
/* 719:610 */       result |= UNSAFE.getLong(bytes1, i) ^ UNSAFE.getLong(bytes2, j);i -= 8L;
/* 720:    */     }
/* 721:612 */     switch (remainingBytes)
/* 722:    */     {
/* 723:    */     case 7: 
/* 724:614 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 725:615 */         .getInt(bytes1, baseOffset1 + 3L) ^ UNSAFE.getInt(bytes2, baseOffset2 + 3L) | UNSAFE
/* 726:616 */         .getChar(bytes1, baseOffset1 + 1L) ^ UNSAFE.getChar(bytes2, baseOffset2 + 1L) | UNSAFE
/* 727:617 */         .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2), 0L);
/* 728:    */     case 6: 
/* 729:619 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 730:620 */         .getInt(bytes1, baseOffset1 + 2L) ^ UNSAFE.getInt(bytes2, baseOffset2 + 2L) | UNSAFE
/* 731:621 */         .getChar(bytes1, baseOffset1) ^ UNSAFE.getChar(bytes2, baseOffset2), 0L);
/* 732:    */     case 5: 
/* 733:623 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 734:624 */         .getInt(bytes1, baseOffset1 + 1L) ^ UNSAFE.getInt(bytes2, baseOffset2 + 1L) | UNSAFE
/* 735:625 */         .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2), 0L);
/* 736:    */     case 4: 
/* 737:627 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 738:628 */         .getInt(bytes1, baseOffset1) ^ UNSAFE.getInt(bytes2, baseOffset2), 0L);
/* 739:    */     case 3: 
/* 740:630 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 741:631 */         .getChar(bytes1, baseOffset1 + 1L) ^ UNSAFE.getChar(bytes2, baseOffset2 + 1L) | UNSAFE
/* 742:632 */         .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2), 0L);
/* 743:    */     case 2: 
/* 744:634 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 745:635 */         .getChar(bytes1, baseOffset1) ^ UNSAFE.getChar(bytes2, baseOffset2), 0L);
/* 746:    */     case 1: 
/* 747:637 */       return ConstantTimeUtils.equalsConstantTime(result | UNSAFE
/* 748:638 */         .getByte(bytes1, baseOffset1) ^ UNSAFE.getByte(bytes2, baseOffset2), 0L);
/* 749:    */     }
/* 750:640 */     return ConstantTimeUtils.equalsConstantTime(result, 0L);
/* 751:    */   }
/* 752:    */   
/* 753:    */   static boolean isZero(byte[] bytes, int startPos, int length)
/* 754:    */   {
/* 755:645 */     if (length <= 0) {
/* 756:646 */       return true;
/* 757:    */     }
/* 758:648 */     long baseOffset = BYTE_ARRAY_BASE_OFFSET + startPos;
/* 759:649 */     int remainingBytes = length & 0x7;
/* 760:650 */     long end = baseOffset + remainingBytes;
/* 761:651 */     for (long i = baseOffset - 8L + length; i >= end; i -= 8L) {
/* 762:652 */       if (UNSAFE.getLong(bytes, i) != 0L) {
/* 763:653 */         return false;
/* 764:    */       }
/* 765:    */     }
/* 766:657 */     if (remainingBytes >= 4)
/* 767:    */     {
/* 768:658 */       remainingBytes -= 4;
/* 769:659 */       if (UNSAFE.getInt(bytes, baseOffset + remainingBytes) != 0) {
/* 770:660 */         return false;
/* 771:    */       }
/* 772:    */     }
/* 773:663 */     if (remainingBytes >= 2) {
/* 774:664 */       return (UNSAFE.getChar(bytes, baseOffset) == 0) && ((remainingBytes == 2) || (bytes[(startPos + 2)] == 0));
/* 775:    */     }
/* 776:667 */     return bytes[startPos] == 0;
/* 777:    */   }
/* 778:    */   
/* 779:    */   static int hashCodeAscii(byte[] bytes, int startPos, int length)
/* 780:    */   {
/* 781:671 */     int hash = -1028477387;
/* 782:672 */     long baseOffset = BYTE_ARRAY_BASE_OFFSET + startPos;
/* 783:673 */     int remainingBytes = length & 0x7;
/* 784:674 */     long end = baseOffset + remainingBytes;
/* 785:675 */     for (long i = baseOffset - 8L + length; i >= end; i -= 8L) {
/* 786:676 */       hash = hashCodeAsciiCompute(UNSAFE.getLong(bytes, i), hash);
/* 787:    */     }
/* 788:678 */     switch (remainingBytes)
/* 789:    */     {
/* 790:    */     case 7: 
/* 791:680 */       return 
/* 792:    */       
/* 793:682 */         ((hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset))) * 461845907 + hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset + 1L))) * -862048943 + hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset + 3L));
/* 794:    */     case 6: 
/* 795:684 */       return 
/* 796:685 */         (hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset))) * 461845907 + hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset + 2L));
/* 797:    */     case 5: 
/* 798:687 */       return 
/* 799:688 */         (hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset))) * 461845907 + hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset + 1L));
/* 800:    */     case 4: 
/* 801:690 */       return hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getInt(bytes, baseOffset));
/* 802:    */     case 3: 
/* 803:692 */       return 
/* 804:693 */         (hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset))) * 461845907 + hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset + 1L));
/* 805:    */     case 2: 
/* 806:695 */       return hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getShort(bytes, baseOffset));
/* 807:    */     case 1: 
/* 808:697 */       return hash * -862048943 + hashCodeAsciiSanitize(UNSAFE.getByte(bytes, baseOffset));
/* 809:    */     }
/* 810:699 */     return hash;
/* 811:    */   }
/* 812:    */   
/* 813:    */   static int hashCodeAsciiCompute(long value, int hash)
/* 814:    */   {
/* 815:706 */     return 
/* 816:    */     
/* 817:708 */       hash * -862048943 + hashCodeAsciiSanitize((int)value) * 461845907 + (int)((value & 0x0) >>> 32);
/* 818:    */   }
/* 819:    */   
/* 820:    */   static int hashCodeAsciiSanitize(int value)
/* 821:    */   {
/* 822:714 */     return value & 0x1F1F1F1F;
/* 823:    */   }
/* 824:    */   
/* 825:    */   static int hashCodeAsciiSanitize(short value)
/* 826:    */   {
/* 827:718 */     return value & 0x1F1F;
/* 828:    */   }
/* 829:    */   
/* 830:    */   static int hashCodeAsciiSanitize(byte value)
/* 831:    */   {
/* 832:722 */     return value & 0x1F;
/* 833:    */   }
/* 834:    */   
/* 835:    */   static ClassLoader getClassLoader(Class<?> clazz)
/* 836:    */   {
/* 837:726 */     if (System.getSecurityManager() == null) {
/* 838:727 */       return clazz.getClassLoader();
/* 839:    */     }
/* 840:729 */     (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
/* 841:    */     {
/* 842:    */       public ClassLoader run()
/* 843:    */       {
/* 844:732 */         return this.val$clazz.getClassLoader();
/* 845:    */       }
/* 846:    */     });
/* 847:    */   }
/* 848:    */   
/* 849:    */   static ClassLoader getContextClassLoader()
/* 850:    */   {
/* 851:739 */     if (System.getSecurityManager() == null) {
/* 852:740 */       return Thread.currentThread().getContextClassLoader();
/* 853:    */     }
/* 854:742 */     (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
/* 855:    */     {
/* 856:    */       public ClassLoader run()
/* 857:    */       {
/* 858:745 */         return Thread.currentThread().getContextClassLoader();
/* 859:    */       }
/* 860:    */     });
/* 861:    */   }
/* 862:    */   
/* 863:    */   static ClassLoader getSystemClassLoader()
/* 864:    */   {
/* 865:752 */     if (System.getSecurityManager() == null) {
/* 866:753 */       return ClassLoader.getSystemClassLoader();
/* 867:    */     }
/* 868:755 */     (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
/* 869:    */     {
/* 870:    */       public ClassLoader run()
/* 871:    */       {
/* 872:758 */         return ClassLoader.getSystemClassLoader();
/* 873:    */       }
/* 874:    */     });
/* 875:    */   }
/* 876:    */   
/* 877:    */   static int addressSize()
/* 878:    */   {
/* 879:765 */     return UNSAFE.addressSize();
/* 880:    */   }
/* 881:    */   
/* 882:    */   static long allocateMemory(long size)
/* 883:    */   {
/* 884:769 */     return UNSAFE.allocateMemory(size);
/* 885:    */   }
/* 886:    */   
/* 887:    */   static void freeMemory(long address)
/* 888:    */   {
/* 889:773 */     UNSAFE.freeMemory(address);
/* 890:    */   }
/* 891:    */   
/* 892:    */   static long reallocateMemory(long address, long newSize)
/* 893:    */   {
/* 894:777 */     return UNSAFE.reallocateMemory(address, newSize);
/* 895:    */   }
/* 896:    */   
/* 897:    */   static boolean isAndroid()
/* 898:    */   {
/* 899:781 */     return IS_ANDROID;
/* 900:    */   }
/* 901:    */   
/* 902:    */   private static boolean isAndroid0()
/* 903:    */   {
/* 904:    */     boolean android;
/* 905:    */     try
/* 906:    */     {
/* 907:787 */       Class.forName("android.app.Application", false, getSystemClassLoader());
/* 908:788 */       android = true;
/* 909:    */     }
/* 910:    */     catch (Throwable ignored)
/* 911:    */     {
/* 912:    */       boolean android;
/* 913:791 */       android = false;
/* 914:    */     }
/* 915:794 */     if (android) {
/* 916:795 */       logger.debug("Platform: Android");
/* 917:    */     }
/* 918:797 */     return android;
/* 919:    */   }
/* 920:    */   
/* 921:    */   static int javaVersion()
/* 922:    */   {
/* 923:801 */     return JAVA_VERSION;
/* 924:    */   }
/* 925:    */   
/* 926:    */   private static int javaVersion0()
/* 927:    */   {
/* 928:    */     int majorVersion;
/* 929:    */     int majorVersion;
/* 930:807 */     if (isAndroid0()) {
/* 931:808 */       majorVersion = 6;
/* 932:    */     } else {
/* 933:810 */       majorVersion = majorVersionFromJavaSpecificationVersion();
/* 934:    */     }
/* 935:813 */     logger.debug("Java version: {}", Integer.valueOf(majorVersion));
/* 936:    */     
/* 937:815 */     return majorVersion;
/* 938:    */   }
/* 939:    */   
/* 940:    */   static int majorVersionFromJavaSpecificationVersion()
/* 941:    */   {
/* 942:820 */     return majorVersion(SystemPropertyUtil.get("java.specification.version", "1.6"));
/* 943:    */   }
/* 944:    */   
/* 945:    */   static int majorVersion(String javaSpecVersion)
/* 946:    */   {
/* 947:825 */     String[] components = javaSpecVersion.split("\\.");
/* 948:826 */     int[] version = new int[components.length];
/* 949:827 */     for (int i = 0; i < components.length; i++) {
/* 950:828 */       version[i] = Integer.parseInt(components[i]);
/* 951:    */     }
/* 952:831 */     if (version[0] == 1)
/* 953:    */     {
/* 954:832 */       assert (version[1] >= 6);
/* 955:833 */       return version[1];
/* 956:    */     }
/* 957:835 */     return version[0];
/* 958:    */   }
/* 959:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.PlatformDependent0
 * JD-Core Version:    0.7.0.1
 */