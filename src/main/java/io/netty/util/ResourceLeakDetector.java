/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.EmptyArrays;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.lang.ref.ReferenceQueue;
/*  10:    */ import java.lang.ref.WeakReference;
/*  11:    */ import java.lang.reflect.Method;
/*  12:    */ import java.util.Arrays;
/*  13:    */ import java.util.HashSet;
/*  14:    */ import java.util.Random;
/*  15:    */ import java.util.Set;
/*  16:    */ import java.util.concurrent.ConcurrentMap;
/*  17:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  18:    */ import java.util.concurrent.atomic.AtomicReference;
/*  19:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  20:    */ 
/*  21:    */ public class ResourceLeakDetector<T>
/*  22:    */ {
/*  23:    */   private static final String PROP_LEVEL_OLD = "io.netty.leakDetectionLevel";
/*  24:    */   private static final String PROP_LEVEL = "io.netty.leakDetection.level";
/*  25: 44 */   private static final Level DEFAULT_LEVEL = Level.SIMPLE;
/*  26:    */   private static final String PROP_TARGET_RECORDS = "io.netty.leakDetection.targetRecords";
/*  27:    */   private static final int DEFAULT_TARGET_RECORDS = 4;
/*  28:    */   private static final int TARGET_RECORDS;
/*  29:    */   private static Level level;
/*  30:    */   
/*  31:    */   public static enum Level
/*  32:    */   {
/*  33: 58 */     DISABLED,  SIMPLE,  ADVANCED,  PARANOID;
/*  34:    */     
/*  35:    */     private Level() {}
/*  36:    */     
/*  37:    */     static Level parseLevel(String levelStr)
/*  38:    */     {
/*  39: 82 */       String trimmedLevelStr = levelStr.trim();
/*  40: 83 */       for (Level l : values()) {
/*  41: 84 */         if ((trimmedLevelStr.equalsIgnoreCase(l.name())) || (trimmedLevelStr.equals(String.valueOf(l.ordinal())))) {
/*  42: 85 */           return l;
/*  43:    */         }
/*  44:    */       }
/*  45: 88 */       return ResourceLeakDetector.DEFAULT_LEVEL;
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49: 94 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLeakDetector.class);
/*  50:    */   static final int DEFAULT_SAMPLING_INTERVAL = 128;
/*  51:    */   
/*  52:    */   static
/*  53:    */   {
/*  54:    */     boolean disabled;
/*  55: 98 */     if (SystemPropertyUtil.get("io.netty.noResourceLeakDetection") != null)
/*  56:    */     {
/*  57: 99 */       boolean disabled = SystemPropertyUtil.getBoolean("io.netty.noResourceLeakDetection", false);
/*  58:100 */       logger.debug("-Dio.netty.noResourceLeakDetection: {}", Boolean.valueOf(disabled));
/*  59:101 */       logger.warn("-Dio.netty.noResourceLeakDetection is deprecated. Use '-D{}={}' instead.", "io.netty.leakDetection.level", DEFAULT_LEVEL
/*  60:    */       
/*  61:103 */         .name().toLowerCase());
/*  62:    */     }
/*  63:    */     else
/*  64:    */     {
/*  65:105 */       disabled = false;
/*  66:    */     }
/*  67:108 */     Level defaultLevel = disabled ? Level.DISABLED : DEFAULT_LEVEL;
/*  68:    */     
/*  69:    */ 
/*  70:111 */     String levelStr = SystemPropertyUtil.get("io.netty.leakDetectionLevel", defaultLevel.name());
/*  71:    */     
/*  72:    */ 
/*  73:114 */     levelStr = SystemPropertyUtil.get("io.netty.leakDetection.level", levelStr);
/*  74:115 */     Level level = Level.parseLevel(levelStr);
/*  75:    */     
/*  76:117 */     TARGET_RECORDS = SystemPropertyUtil.getInt("io.netty.leakDetection.targetRecords", 4);
/*  77:    */     
/*  78:119 */     level = level;
/*  79:120 */     if (logger.isDebugEnabled())
/*  80:    */     {
/*  81:121 */       logger.debug("-D{}: {}", "io.netty.leakDetection.level", level.name().toLowerCase());
/*  82:122 */       logger.debug("-D{}: {}", "io.netty.leakDetection.targetRecords", Integer.valueOf(TARGET_RECORDS));
/*  83:    */     }
/*  84:    */   }
/*  85:    */   
/*  86:    */   @Deprecated
/*  87:    */   public static void setEnabled(boolean enabled)
/*  88:    */   {
/*  89:134 */     setLevel(enabled ? Level.SIMPLE : Level.DISABLED);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static boolean isEnabled()
/*  93:    */   {
/*  94:141 */     return getLevel().ordinal() > Level.DISABLED.ordinal();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public static void setLevel(Level level)
/*  98:    */   {
/*  99:148 */     if (level == null) {
/* 100:149 */       throw new NullPointerException("level");
/* 101:    */     }
/* 102:151 */     level = level;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public static Level getLevel()
/* 106:    */   {
/* 107:158 */     return level;
/* 108:    */   }
/* 109:    */   
/* 110:162 */   private final ConcurrentMap<DefaultResourceLeak<?>, LeakEntry> allLeaks = PlatformDependent.newConcurrentHashMap();
/* 111:164 */   private final ReferenceQueue<Object> refQueue = new ReferenceQueue();
/* 112:165 */   private final ConcurrentMap<String, Boolean> reportedLeaks = PlatformDependent.newConcurrentHashMap();
/* 113:    */   private final String resourceType;
/* 114:    */   private final int samplingInterval;
/* 115:    */   
/* 116:    */   @Deprecated
/* 117:    */   public ResourceLeakDetector(Class<?> resourceType)
/* 118:    */   {
/* 119:175 */     this(StringUtil.simpleClassName(resourceType));
/* 120:    */   }
/* 121:    */   
/* 122:    */   @Deprecated
/* 123:    */   public ResourceLeakDetector(String resourceType)
/* 124:    */   {
/* 125:183 */     this(resourceType, 128, 9223372036854775807L);
/* 126:    */   }
/* 127:    */   
/* 128:    */   @Deprecated
/* 129:    */   public ResourceLeakDetector(Class<?> resourceType, int samplingInterval, long maxActive)
/* 130:    */   {
/* 131:197 */     this(resourceType, samplingInterval);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ResourceLeakDetector(Class<?> resourceType, int samplingInterval)
/* 135:    */   {
/* 136:207 */     this(StringUtil.simpleClassName(resourceType), samplingInterval, 9223372036854775807L);
/* 137:    */   }
/* 138:    */   
/* 139:    */   @Deprecated
/* 140:    */   public ResourceLeakDetector(String resourceType, int samplingInterval, long maxActive)
/* 141:    */   {
/* 142:217 */     if (resourceType == null) {
/* 143:218 */       throw new NullPointerException("resourceType");
/* 144:    */     }
/* 145:221 */     this.resourceType = resourceType;
/* 146:222 */     this.samplingInterval = samplingInterval;
/* 147:    */   }
/* 148:    */   
/* 149:    */   @Deprecated
/* 150:    */   public final ResourceLeak open(T obj)
/* 151:    */   {
/* 152:234 */     return track0(obj);
/* 153:    */   }
/* 154:    */   
/* 155:    */   public final ResourceLeakTracker<T> track(T obj)
/* 156:    */   {
/* 157:245 */     return track0(obj);
/* 158:    */   }
/* 159:    */   
/* 160:    */   private DefaultResourceLeak track0(T obj)
/* 161:    */   {
/* 162:250 */     Level level = level;
/* 163:251 */     if (level == Level.DISABLED) {
/* 164:252 */       return null;
/* 165:    */     }
/* 166:255 */     if (level.ordinal() < Level.PARANOID.ordinal())
/* 167:    */     {
/* 168:256 */       if (PlatformDependent.threadLocalRandom().nextInt(this.samplingInterval) == 0)
/* 169:    */       {
/* 170:257 */         reportLeak();
/* 171:258 */         return new DefaultResourceLeak(obj, this.refQueue, this.allLeaks);
/* 172:    */       }
/* 173:260 */       return null;
/* 174:    */     }
/* 175:262 */     reportLeak();
/* 176:263 */     return new DefaultResourceLeak(obj, this.refQueue, this.allLeaks);
/* 177:    */   }
/* 178:    */   
/* 179:    */   private void clearRefQueue()
/* 180:    */   {
/* 181:    */     for (;;)
/* 182:    */     {
/* 183:269 */       DefaultResourceLeak ref = (DefaultResourceLeak)this.refQueue.poll();
/* 184:270 */       if (ref == null) {
/* 185:    */         break;
/* 186:    */       }
/* 187:273 */       ref.dispose();
/* 188:    */     }
/* 189:    */   }
/* 190:    */   
/* 191:    */   private void reportLeak()
/* 192:    */   {
/* 193:278 */     if (!logger.isErrorEnabled()) {
/* 194:279 */       clearRefQueue();
/* 195:    */     } else {
/* 196:    */       for (;;)
/* 197:    */       {
/* 198:286 */         DefaultResourceLeak ref = (DefaultResourceLeak)this.refQueue.poll();
/* 199:287 */         if (ref == null) {
/* 200:    */           break;
/* 201:    */         }
/* 202:291 */         if (ref.dispose())
/* 203:    */         {
/* 204:295 */           String records = ref.toString();
/* 205:296 */           if (this.reportedLeaks.putIfAbsent(records, Boolean.TRUE) == null) {
/* 206:297 */             if (records.isEmpty()) {
/* 207:298 */               reportUntracedLeak(this.resourceType);
/* 208:    */             } else {
/* 209:300 */               reportTracedLeak(this.resourceType, records);
/* 210:    */             }
/* 211:    */           }
/* 212:    */         }
/* 213:    */       }
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected void reportTracedLeak(String resourceType, String records)
/* 218:    */   {
/* 219:311 */     logger.error("LEAK: {}.release() was not called before it's garbage-collected. See http://netty.io/wiki/reference-counted-objects.html for more information.{}", resourceType, records);
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected void reportUntracedLeak(String resourceType)
/* 223:    */   {
/* 224:322 */     logger.error("LEAK: {}.release() was not called before it's garbage-collected. Enable advanced leak reporting to find out where the leak occurred. To enable advanced leak reporting, specify the JVM option '-D{}={}' or call {}.setLevel() See http://netty.io/wiki/reference-counted-objects.html for more information.", new Object[] { resourceType, "io.netty.leakDetection.level", Level.ADVANCED
/* 225:    */     
/* 226:    */ 
/* 227:    */ 
/* 228:    */ 
/* 229:327 */       .name().toLowerCase(), StringUtil.simpleClassName(this) });
/* 230:    */   }
/* 231:    */   
/* 232:    */   private static final class DefaultResourceLeak<T>
/* 233:    */     extends WeakReference<Object>
/* 234:    */     implements ResourceLeakTracker<T>, ResourceLeak
/* 235:    */   {
/* 236:344 */     private static final AtomicReferenceFieldUpdater<DefaultResourceLeak<?>, ResourceLeakDetector.Record> headUpdater = AtomicReferenceFieldUpdater.newUpdater(DefaultResourceLeak.class, ResourceLeakDetector.Record.class, "head");
/* 237:349 */     private static final AtomicIntegerFieldUpdater<DefaultResourceLeak<?>> droppedRecordsUpdater = AtomicIntegerFieldUpdater.newUpdater(DefaultResourceLeak.class, "droppedRecords");
/* 238:    */     private volatile ResourceLeakDetector.Record head;
/* 239:    */     private volatile int droppedRecords;
/* 240:    */     private final ConcurrentMap<DefaultResourceLeak<?>, ResourceLeakDetector.LeakEntry> allLeaks;
/* 241:    */     private final int trackedHash;
/* 242:    */     
/* 243:    */     DefaultResourceLeak(Object referent, ReferenceQueue<Object> refQueue, ConcurrentMap<DefaultResourceLeak<?>, ResourceLeakDetector.LeakEntry> allLeaks)
/* 244:    */     {
/* 245:363 */       super(refQueue);
/* 246:    */       
/* 247:365 */       assert (referent != null);
/* 248:    */       
/* 249:    */ 
/* 250:    */ 
/* 251:    */ 
/* 252:370 */       this.trackedHash = System.identityHashCode(referent);
/* 253:371 */       allLeaks.put(this, ResourceLeakDetector.LeakEntry.INSTANCE);
/* 254:372 */       headUpdater.set(this, ResourceLeakDetector.Record.BOTTOM);
/* 255:373 */       this.allLeaks = allLeaks;
/* 256:    */     }
/* 257:    */     
/* 258:    */     public void record()
/* 259:    */     {
/* 260:378 */       record0(null);
/* 261:    */     }
/* 262:    */     
/* 263:    */     public void record(Object hint)
/* 264:    */     {
/* 265:383 */       record0(hint);
/* 266:    */     }
/* 267:    */     
/* 268:    */     private void record0(Object hint)
/* 269:    */     {
/* 270:414 */       if (ResourceLeakDetector.TARGET_RECORDS > 0)
/* 271:    */       {
/* 272:    */         ResourceLeakDetector.Record oldHead;
/* 273:    */         boolean dropped;
/* 274:    */         ResourceLeakDetector.Record newHead;
/* 275:    */         do
/* 276:    */         {
/* 277:    */           ResourceLeakDetector.Record prevHead;
/* 278:420 */           if ((prevHead = oldHead = (ResourceLeakDetector.Record)headUpdater.get(this)) == null) {
/* 279:422 */             return;
/* 280:    */           }
/* 281:424 */           int numElements = oldHead.pos + 1;
/* 282:425 */           if (numElements >= ResourceLeakDetector.TARGET_RECORDS)
/* 283:    */           {
/* 284:426 */             int backOffFactor = Math.min(numElements - ResourceLeakDetector.TARGET_RECORDS, 30);
/* 285:    */             boolean dropped;
/* 286:427 */             if ((dropped = PlatformDependent.threadLocalRandom().nextInt(1 << backOffFactor) != 0 ? 1 : 0) != 0) {
/* 287:428 */               prevHead = oldHead.next;
/* 288:    */             }
/* 289:    */           }
/* 290:    */           else
/* 291:    */           {
/* 292:431 */             dropped = false;
/* 293:    */           }
/* 294:433 */           newHead = hint != null ? new ResourceLeakDetector.Record(prevHead, hint) : new ResourceLeakDetector.Record(prevHead);
/* 295:434 */         } while (!headUpdater.compareAndSet(this, oldHead, newHead));
/* 296:435 */         if (dropped) {
/* 297:436 */           droppedRecordsUpdater.incrementAndGet(this);
/* 298:    */         }
/* 299:    */       }
/* 300:    */     }
/* 301:    */     
/* 302:    */     boolean dispose()
/* 303:    */     {
/* 304:442 */       clear();
/* 305:443 */       return this.allLeaks.remove(this, ResourceLeakDetector.LeakEntry.INSTANCE);
/* 306:    */     }
/* 307:    */     
/* 308:    */     public boolean close()
/* 309:    */     {
/* 310:449 */       if (this.allLeaks.remove(this, ResourceLeakDetector.LeakEntry.INSTANCE))
/* 311:    */       {
/* 312:451 */         clear();
/* 313:452 */         headUpdater.set(this, null);
/* 314:453 */         return true;
/* 315:    */       }
/* 316:455 */       return false;
/* 317:    */     }
/* 318:    */     
/* 319:    */     public boolean close(T trackedObject)
/* 320:    */     {
/* 321:461 */       assert (this.trackedHash == System.identityHashCode(trackedObject));
/* 322:    */       
/* 323:    */ 
/* 324:    */ 
/* 325:    */ 
/* 326:    */ 
/* 327:467 */       return (close()) && (trackedObject != null);
/* 328:    */     }
/* 329:    */     
/* 330:    */     public String toString()
/* 331:    */     {
/* 332:472 */       ResourceLeakDetector.Record oldHead = (ResourceLeakDetector.Record)headUpdater.getAndSet(this, null);
/* 333:473 */       if (oldHead == null) {
/* 334:475 */         return "";
/* 335:    */       }
/* 336:478 */       int dropped = droppedRecordsUpdater.get(this);
/* 337:479 */       int duped = 0;
/* 338:    */       
/* 339:481 */       int present = oldHead.pos + 1;
/* 340:    */       
/* 341:483 */       StringBuilder buf = new StringBuilder(present * 2048).append(StringUtil.NEWLINE);
/* 342:484 */       buf.append("Recent access records: ").append(StringUtil.NEWLINE);
/* 343:    */       
/* 344:486 */       int i = 1;
/* 345:487 */       Set<String> seen = new HashSet(present);
/* 346:488 */       for (; oldHead != ResourceLeakDetector.Record.BOTTOM; oldHead = oldHead.next)
/* 347:    */       {
/* 348:489 */         String s = oldHead.toString();
/* 349:490 */         if (seen.add(s))
/* 350:    */         {
/* 351:491 */           if (oldHead.next == ResourceLeakDetector.Record.BOTTOM) {
/* 352:492 */             buf.append("Created at:").append(StringUtil.NEWLINE).append(s);
/* 353:    */           } else {
/* 354:494 */             buf.append('#').append(i++).append(':').append(StringUtil.NEWLINE).append(s);
/* 355:    */           }
/* 356:    */         }
/* 357:    */         else {
/* 358:497 */           duped++;
/* 359:    */         }
/* 360:    */       }
/* 361:501 */       if (duped > 0) {
/* 362:505 */         buf.append(": ").append(dropped).append(" leak records were discarded because they were duplicates").append(StringUtil.NEWLINE);
/* 363:    */       }
/* 364:508 */       if (dropped > 0) {
/* 365:516 */         buf.append(": ").append(dropped).append(" leak records were discarded because the leak record count is targeted to ").append(ResourceLeakDetector.TARGET_RECORDS).append(". Use system property ").append("io.netty.leakDetection.targetRecords").append(" to increase the limit.").append(StringUtil.NEWLINE);
/* 366:    */       }
/* 367:519 */       buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 368:520 */       return buf.toString();
/* 369:    */     }
/* 370:    */   }
/* 371:    */   
/* 372:524 */   private static final AtomicReference<String[]> excludedMethods = new AtomicReference(EmptyArrays.EMPTY_STRINGS);
/* 373:    */   
/* 374:    */   public static void addExclusions(Class clz, String... methodNames)
/* 375:    */   {
/* 376:528 */     Set<String> nameSet = new HashSet(Arrays.asList(methodNames));
/* 377:531 */     for (Method method : clz.getDeclaredMethods()) {
/* 378:532 */       if ((nameSet.remove(method.getName())) && (nameSet.isEmpty())) {
/* 379:    */         break;
/* 380:    */       }
/* 381:    */     }
/* 382:536 */     if (!nameSet.isEmpty()) {
/* 383:537 */       throw new IllegalArgumentException("Can't find '" + nameSet + "' in " + clz.getName());
/* 384:    */     }
/* 385:    */     String[] oldMethods;
/* 386:    */     String[] newMethods;
/* 387:    */     do
/* 388:    */     {
/* 389:542 */       oldMethods = (String[])excludedMethods.get();
/* 390:543 */       newMethods = (String[])Arrays.copyOf(oldMethods, oldMethods.length + 2 * methodNames.length);
/* 391:544 */       for (int i = 0; i < methodNames.length; i++)
/* 392:    */       {
/* 393:545 */         newMethods[(oldMethods.length + i * 2)] = clz.getName();
/* 394:546 */         newMethods[(oldMethods.length + i * 2 + 1)] = methodNames[i];
/* 395:    */       }
/* 396:548 */     } while (!excludedMethods.compareAndSet(oldMethods, newMethods));
/* 397:    */   }
/* 398:    */   
/* 399:    */   @Deprecated
/* 400:    */   protected void reportInstancesLeak(String resourceType) {}
/* 401:    */   
/* 402:    */   private static final class Record
/* 403:    */     extends Throwable
/* 404:    */   {
/* 405:    */     private static final long serialVersionUID = 6065153674892850720L;
/* 406:554 */     private static final Record BOTTOM = new Record();
/* 407:    */     private final String hintString;
/* 408:    */     private final Record next;
/* 409:    */     private final int pos;
/* 410:    */     
/* 411:    */     Record(Record next, Object hint)
/* 412:    */     {
/* 413:562 */       this.hintString = ((hint instanceof ResourceLeakHint) ? ((ResourceLeakHint)hint).toHintString() : hint.toString());
/* 414:563 */       this.next = next;
/* 415:564 */       next.pos += 1;
/* 416:    */     }
/* 417:    */     
/* 418:    */     Record(Record next)
/* 419:    */     {
/* 420:568 */       this.hintString = null;
/* 421:569 */       this.next = next;
/* 422:570 */       next.pos += 1;
/* 423:    */     }
/* 424:    */     
/* 425:    */     private Record()
/* 426:    */     {
/* 427:575 */       this.hintString = null;
/* 428:576 */       this.next = null;
/* 429:577 */       this.pos = -1;
/* 430:    */     }
/* 431:    */     
/* 432:    */     public String toString()
/* 433:    */     {
/* 434:582 */       StringBuilder buf = new StringBuilder(2048);
/* 435:583 */       if (this.hintString != null) {
/* 436:584 */         buf.append("\tHint: ").append(this.hintString).append(StringUtil.NEWLINE);
/* 437:    */       }
/* 438:588 */       StackTraceElement[] array = getStackTrace();
/* 439:    */       label146:
/* 440:590 */       for (int i = 3; i < array.length; i++)
/* 441:    */       {
/* 442:591 */         StackTraceElement element = array[i];
/* 443:    */         
/* 444:593 */         String[] exclusions = (String[])ResourceLeakDetector.excludedMethods.get();
/* 445:594 */         for (int k = 0; k < exclusions.length; k += 2) {
/* 446:595 */           if ((exclusions[k].equals(element.getClassName())) && 
/* 447:596 */             (exclusions[(k + 1)].equals(element.getMethodName()))) {
/* 448:    */             break label146;
/* 449:    */           }
/* 450:    */         }
/* 451:601 */         buf.append('\t');
/* 452:602 */         buf.append(element.toString());
/* 453:603 */         buf.append(StringUtil.NEWLINE);
/* 454:    */       }
/* 455:605 */       return buf.toString();
/* 456:    */     }
/* 457:    */   }
/* 458:    */   
/* 459:    */   private static final class LeakEntry
/* 460:    */   {
/* 461:610 */     static final LeakEntry INSTANCE = new LeakEntry();
/* 462:611 */     private static final int HASH = System.identityHashCode(INSTANCE);
/* 463:    */     
/* 464:    */     public int hashCode()
/* 465:    */     {
/* 466:618 */       return HASH;
/* 467:    */     }
/* 468:    */     
/* 469:    */     public boolean equals(Object obj)
/* 470:    */     {
/* 471:623 */       return obj == this;
/* 472:    */     }
/* 473:    */   }
/* 474:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ResourceLeakDetector
 * JD-Core Version:    0.7.0.1
 */