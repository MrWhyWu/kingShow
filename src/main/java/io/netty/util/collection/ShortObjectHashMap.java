/*   1:    */ package io.netty.util.collection;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.MathUtil;
/*   4:    */ import java.util.AbstractCollection;
/*   5:    */ import java.util.AbstractSet;
/*   6:    */ import java.util.Arrays;
/*   7:    */ import java.util.Collection;
/*   8:    */ import java.util.Iterator;
/*   9:    */ import java.util.Map;
/*  10:    */ import java.util.Map.Entry;
/*  11:    */ import java.util.NoSuchElementException;
/*  12:    */ import java.util.Set;
/*  13:    */ 
/*  14:    */ public class ShortObjectHashMap<V>
/*  15:    */   implements ShortObjectMap<V>
/*  16:    */ {
/*  17:    */   public static final int DEFAULT_CAPACITY = 8;
/*  18:    */   public static final float DEFAULT_LOAD_FACTOR = 0.5F;
/*  19: 49 */   private static final Object NULL_VALUE = new Object();
/*  20:    */   private int maxSize;
/*  21:    */   private final float loadFactor;
/*  22:    */   private short[] keys;
/*  23:    */   private V[] values;
/*  24:    */   private int size;
/*  25:    */   private int mask;
/*  26: 62 */   private final Set<Short> keySet = new KeySet(null);
/*  27: 63 */   private final Set<Map.Entry<Short, V>> entrySet = new EntrySet(null);
/*  28: 64 */   private final Iterable<ShortObjectMap.PrimitiveEntry<V>> entries = new Iterable()
/*  29:    */   {
/*  30:    */     public Iterator<ShortObjectMap.PrimitiveEntry<V>> iterator()
/*  31:    */     {
/*  32: 67 */       return new ShortObjectHashMap.PrimitiveIterator(ShortObjectHashMap.this, null);
/*  33:    */     }
/*  34:    */   };
/*  35:    */   
/*  36:    */   public ShortObjectHashMap()
/*  37:    */   {
/*  38: 72 */     this(8, 0.5F);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public ShortObjectHashMap(int initialCapacity)
/*  42:    */   {
/*  43: 76 */     this(initialCapacity, 0.5F);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public ShortObjectHashMap(int initialCapacity, float loadFactor)
/*  47:    */   {
/*  48: 80 */     if ((loadFactor <= 0.0F) || (loadFactor > 1.0F)) {
/*  49: 83 */       throw new IllegalArgumentException("loadFactor must be > 0 and <= 1");
/*  50:    */     }
/*  51: 86 */     this.loadFactor = loadFactor;
/*  52:    */     
/*  53:    */ 
/*  54: 89 */     int capacity = MathUtil.safeFindNextPositivePowerOfTwo(initialCapacity);
/*  55: 90 */     this.mask = (capacity - 1);
/*  56:    */     
/*  57:    */ 
/*  58: 93 */     this.keys = new short[capacity];
/*  59:    */     
/*  60: 95 */     V[] temp = (Object[])new Object[capacity];
/*  61: 96 */     this.values = temp;
/*  62:    */     
/*  63:    */ 
/*  64: 99 */     this.maxSize = calcMaxSize(capacity);
/*  65:    */   }
/*  66:    */   
/*  67:    */   private static <T> T toExternal(T value)
/*  68:    */   {
/*  69:103 */     assert (value != null) : "null is not a legitimate internal value. Concurrent Modification?";
/*  70:104 */     return value == NULL_VALUE ? null : value;
/*  71:    */   }
/*  72:    */   
/*  73:    */   private static <T> T toInternal(T value)
/*  74:    */   {
/*  75:109 */     return value == null ? NULL_VALUE : value;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public V get(short key)
/*  79:    */   {
/*  80:114 */     int index = indexOf(key);
/*  81:115 */     return index == -1 ? null : toExternal(this.values[index]);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public V put(short key, V value)
/*  85:    */   {
/*  86:120 */     int startIndex = hashIndex(key);
/*  87:121 */     int index = startIndex;
/*  88:    */     do
/*  89:    */     {
/*  90:124 */       if (this.values[index] == null)
/*  91:    */       {
/*  92:126 */         this.keys[index] = key;
/*  93:127 */         this.values[index] = toInternal(value);
/*  94:128 */         growSize();
/*  95:129 */         return null;
/*  96:    */       }
/*  97:131 */       if (this.keys[index] == key)
/*  98:    */       {
/*  99:133 */         V previousValue = this.values[index];
/* 100:134 */         this.values[index] = toInternal(value);
/* 101:135 */         return toExternal(previousValue);
/* 102:    */       }
/* 103:139 */     } while ((index = probeNext(index)) != startIndex);
/* 104:141 */     throw new IllegalStateException("Unable to insert");
/* 105:    */   }
/* 106:    */   
/* 107:    */   public void putAll(Map<? extends Short, ? extends V> sourceMap)
/* 108:    */   {
/* 109:    */     ShortObjectHashMap<V> source;
/* 110:148 */     if ((sourceMap instanceof ShortObjectHashMap))
/* 111:    */     {
/* 112:151 */       source = (ShortObjectHashMap)sourceMap;
/* 113:152 */       for (int i = 0; i < source.values.length; i++)
/* 114:    */       {
/* 115:153 */         V sourceValue = source.values[i];
/* 116:154 */         if (sourceValue != null) {
/* 117:155 */           put(source.keys[i], sourceValue);
/* 118:    */         }
/* 119:    */       }
/* 120:158 */       return;
/* 121:    */     }
/* 122:162 */     for (Map.Entry<? extends Short, ? extends V> entry : sourceMap.entrySet()) {
/* 123:163 */       put((Short)entry.getKey(), entry.getValue());
/* 124:    */     }
/* 125:    */   }
/* 126:    */   
/* 127:    */   public V remove(short key)
/* 128:    */   {
/* 129:169 */     int index = indexOf(key);
/* 130:170 */     if (index == -1) {
/* 131:171 */       return null;
/* 132:    */     }
/* 133:174 */     V prev = this.values[index];
/* 134:175 */     removeAt(index);
/* 135:176 */     return toExternal(prev);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public int size()
/* 139:    */   {
/* 140:181 */     return this.size;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean isEmpty()
/* 144:    */   {
/* 145:186 */     return this.size == 0;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public void clear()
/* 149:    */   {
/* 150:191 */     Arrays.fill(this.keys, (short)0);
/* 151:192 */     Arrays.fill(this.values, null);
/* 152:193 */     this.size = 0;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public boolean containsKey(short key)
/* 156:    */   {
/* 157:198 */     return indexOf(key) >= 0;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public boolean containsValue(Object value)
/* 161:    */   {
/* 162:204 */     V v1 = toInternal(value);
/* 163:205 */     for (V v2 : this.values) {
/* 164:207 */       if ((v2 != null) && (v2.equals(v1))) {
/* 165:208 */         return true;
/* 166:    */       }
/* 167:    */     }
/* 168:211 */     return false;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public Iterable<ShortObjectMap.PrimitiveEntry<V>> entries()
/* 172:    */   {
/* 173:216 */     return this.entries;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public Collection<V> values()
/* 177:    */   {
/* 178:221 */     new AbstractCollection()
/* 179:    */     {
/* 180:    */       public Iterator<V> iterator()
/* 181:    */       {
/* 182:224 */         new Iterator()
/* 183:    */         {
/* 184:225 */           final ShortObjectHashMap<V>.PrimitiveIterator iter = new ShortObjectHashMap.PrimitiveIterator(ShortObjectHashMap.this, null);
/* 185:    */           
/* 186:    */           public boolean hasNext()
/* 187:    */           {
/* 188:229 */             return this.iter.hasNext();
/* 189:    */           }
/* 190:    */           
/* 191:    */           public V next()
/* 192:    */           {
/* 193:234 */             return this.iter.next().value();
/* 194:    */           }
/* 195:    */           
/* 196:    */           public void remove()
/* 197:    */           {
/* 198:239 */             throw new UnsupportedOperationException();
/* 199:    */           }
/* 200:    */         };
/* 201:    */       }
/* 202:    */       
/* 203:    */       public int size()
/* 204:    */       {
/* 205:246 */         return ShortObjectHashMap.this.size;
/* 206:    */       }
/* 207:    */     };
/* 208:    */   }
/* 209:    */   
/* 210:    */   public int hashCode()
/* 211:    */   {
/* 212:256 */     int hash = this.size;
/* 213:257 */     for (short key : this.keys) {
/* 214:265 */       hash ^= hashCode(key);
/* 215:    */     }
/* 216:267 */     return hash;
/* 217:    */   }
/* 218:    */   
/* 219:    */   public boolean equals(Object obj)
/* 220:    */   {
/* 221:272 */     if (this == obj) {
/* 222:273 */       return true;
/* 223:    */     }
/* 224:275 */     if (!(obj instanceof ShortObjectMap)) {
/* 225:276 */       return false;
/* 226:    */     }
/* 227:279 */     ShortObjectMap other = (ShortObjectMap)obj;
/* 228:280 */     if (this.size != other.size()) {
/* 229:281 */       return false;
/* 230:    */     }
/* 231:283 */     for (int i = 0; i < this.values.length; i++)
/* 232:    */     {
/* 233:284 */       V value = this.values[i];
/* 234:285 */       if (value != null)
/* 235:    */       {
/* 236:286 */         short key = this.keys[i];
/* 237:287 */         Object otherValue = other.get(key);
/* 238:288 */         if (value == NULL_VALUE)
/* 239:    */         {
/* 240:289 */           if (otherValue != null) {
/* 241:290 */             return false;
/* 242:    */           }
/* 243:    */         }
/* 244:292 */         else if (!value.equals(otherValue)) {
/* 245:293 */           return false;
/* 246:    */         }
/* 247:    */       }
/* 248:    */     }
/* 249:297 */     return true;
/* 250:    */   }
/* 251:    */   
/* 252:    */   public boolean containsKey(Object key)
/* 253:    */   {
/* 254:302 */     return containsKey(objectToKey(key));
/* 255:    */   }
/* 256:    */   
/* 257:    */   public V get(Object key)
/* 258:    */   {
/* 259:307 */     return get(objectToKey(key));
/* 260:    */   }
/* 261:    */   
/* 262:    */   public V put(Short key, V value)
/* 263:    */   {
/* 264:312 */     return put(objectToKey(key), value);
/* 265:    */   }
/* 266:    */   
/* 267:    */   public V remove(Object key)
/* 268:    */   {
/* 269:317 */     return remove(objectToKey(key));
/* 270:    */   }
/* 271:    */   
/* 272:    */   public Set<Short> keySet()
/* 273:    */   {
/* 274:322 */     return this.keySet;
/* 275:    */   }
/* 276:    */   
/* 277:    */   public Set<Map.Entry<Short, V>> entrySet()
/* 278:    */   {
/* 279:327 */     return this.entrySet;
/* 280:    */   }
/* 281:    */   
/* 282:    */   private short objectToKey(Object key)
/* 283:    */   {
/* 284:331 */     return ((Short)key).shortValue();
/* 285:    */   }
/* 286:    */   
/* 287:    */   private int indexOf(short key)
/* 288:    */   {
/* 289:341 */     int startIndex = hashIndex(key);
/* 290:342 */     int index = startIndex;
/* 291:    */     do
/* 292:    */     {
/* 293:345 */       if (this.values[index] == null) {
/* 294:347 */         return -1;
/* 295:    */       }
/* 296:349 */       if (key == this.keys[index]) {
/* 297:350 */         return index;
/* 298:    */       }
/* 299:354 */     } while ((index = probeNext(index)) != startIndex);
/* 300:355 */     return -1;
/* 301:    */   }
/* 302:    */   
/* 303:    */   private int hashIndex(short key)
/* 304:    */   {
/* 305:365 */     return hashCode(key) & this.mask;
/* 306:    */   }
/* 307:    */   
/* 308:    */   private static int hashCode(short key)
/* 309:    */   {
/* 310:372 */     return key;
/* 311:    */   }
/* 312:    */   
/* 313:    */   private int probeNext(int index)
/* 314:    */   {
/* 315:380 */     return index + 1 & this.mask;
/* 316:    */   }
/* 317:    */   
/* 318:    */   private void growSize()
/* 319:    */   {
/* 320:387 */     this.size += 1;
/* 321:389 */     if (this.size > this.maxSize)
/* 322:    */     {
/* 323:390 */       if (this.keys.length == 2147483647) {
/* 324:391 */         throw new IllegalStateException("Max capacity reached at size=" + this.size);
/* 325:    */       }
/* 326:395 */       rehash(this.keys.length << 1);
/* 327:    */     }
/* 328:    */   }
/* 329:    */   
/* 330:    */   private boolean removeAt(int index)
/* 331:    */   {
/* 332:407 */     this.size -= 1;
/* 333:    */     
/* 334:    */ 
/* 335:410 */     this.keys[index] = 0;
/* 336:411 */     this.values[index] = null;
/* 337:    */     
/* 338:    */ 
/* 339:    */ 
/* 340:    */ 
/* 341:    */ 
/* 342:    */ 
/* 343:418 */     int nextFree = index;
/* 344:419 */     int i = probeNext(index);
/* 345:420 */     for (V value = this.values[i]; value != null; value = this.values[(i = probeNext(i))])
/* 346:    */     {
/* 347:421 */       short key = this.keys[i];
/* 348:422 */       int bucket = hashIndex(key);
/* 349:423 */       if (((i < bucket) && ((bucket <= nextFree) || (nextFree <= i))) || ((bucket <= nextFree) && (nextFree <= i)))
/* 350:    */       {
/* 351:426 */         this.keys[nextFree] = key;
/* 352:427 */         this.values[nextFree] = value;
/* 353:    */         
/* 354:429 */         this.keys[i] = 0;
/* 355:430 */         this.values[i] = null;
/* 356:431 */         nextFree = i;
/* 357:    */       }
/* 358:    */     }
/* 359:434 */     return nextFree != index;
/* 360:    */   }
/* 361:    */   
/* 362:    */   private int calcMaxSize(int capacity)
/* 363:    */   {
/* 364:442 */     int upperBound = capacity - 1;
/* 365:443 */     return Math.min(upperBound, (int)(capacity * this.loadFactor));
/* 366:    */   }
/* 367:    */   
/* 368:    */   private void rehash(int newCapacity)
/* 369:    */   {
/* 370:452 */     short[] oldKeys = this.keys;
/* 371:453 */     V[] oldVals = this.values;
/* 372:    */     
/* 373:455 */     this.keys = new short[newCapacity];
/* 374:    */     
/* 375:457 */     V[] temp = (Object[])new Object[newCapacity];
/* 376:458 */     this.values = temp;
/* 377:    */     
/* 378:460 */     this.maxSize = calcMaxSize(newCapacity);
/* 379:461 */     this.mask = (newCapacity - 1);
/* 380:464 */     for (int i = 0; i < oldVals.length; i++)
/* 381:    */     {
/* 382:465 */       V oldVal = oldVals[i];
/* 383:466 */       if (oldVal != null)
/* 384:    */       {
/* 385:469 */         short oldKey = oldKeys[i];
/* 386:470 */         int index = hashIndex(oldKey);
/* 387:    */         for (;;)
/* 388:    */         {
/* 389:473 */           if (this.values[index] == null)
/* 390:    */           {
/* 391:474 */             this.keys[index] = oldKey;
/* 392:475 */             this.values[index] = oldVal;
/* 393:476 */             break;
/* 394:    */           }
/* 395:480 */           index = probeNext(index);
/* 396:    */         }
/* 397:    */       }
/* 398:    */     }
/* 399:    */   }
/* 400:    */   
/* 401:    */   public String toString()
/* 402:    */   {
/* 403:488 */     if (isEmpty()) {
/* 404:489 */       return "{}";
/* 405:    */     }
/* 406:491 */     StringBuilder sb = new StringBuilder(4 * this.size);
/* 407:492 */     sb.append('{');
/* 408:493 */     boolean first = true;
/* 409:494 */     for (int i = 0; i < this.values.length; i++)
/* 410:    */     {
/* 411:495 */       V value = this.values[i];
/* 412:496 */       if (value != null)
/* 413:    */       {
/* 414:497 */         if (!first) {
/* 415:498 */           sb.append(", ");
/* 416:    */         }
/* 417:500 */         sb.append(keyToString(this.keys[i])).append('=').append(value == this ? "(this Map)" : 
/* 418:501 */           toExternal(value));
/* 419:502 */         first = false;
/* 420:    */       }
/* 421:    */     }
/* 422:505 */     return '}';
/* 423:    */   }
/* 424:    */   
/* 425:    */   protected String keyToString(short key)
/* 426:    */   {
/* 427:513 */     return Short.toString(key);
/* 428:    */   }
/* 429:    */   
/* 430:    */   private final class EntrySet
/* 431:    */     extends AbstractSet<Map.Entry<Short, V>>
/* 432:    */   {
/* 433:    */     private EntrySet() {}
/* 434:    */     
/* 435:    */     public Iterator<Map.Entry<Short, V>> iterator()
/* 436:    */     {
/* 437:522 */       return new ShortObjectHashMap.MapIterator(ShortObjectHashMap.this, null);
/* 438:    */     }
/* 439:    */     
/* 440:    */     public int size()
/* 441:    */     {
/* 442:527 */       return ShortObjectHashMap.this.size();
/* 443:    */     }
/* 444:    */   }
/* 445:    */   
/* 446:    */   private final class KeySet
/* 447:    */     extends AbstractSet<Short>
/* 448:    */   {
/* 449:    */     private KeySet() {}
/* 450:    */     
/* 451:    */     public int size()
/* 452:    */     {
/* 453:537 */       return ShortObjectHashMap.this.size();
/* 454:    */     }
/* 455:    */     
/* 456:    */     public boolean contains(Object o)
/* 457:    */     {
/* 458:542 */       return ShortObjectHashMap.this.containsKey(o);
/* 459:    */     }
/* 460:    */     
/* 461:    */     public boolean remove(Object o)
/* 462:    */     {
/* 463:547 */       return ShortObjectHashMap.this.remove(o) != null;
/* 464:    */     }
/* 465:    */     
/* 466:    */     public boolean retainAll(Collection<?> retainedKeys)
/* 467:    */     {
/* 468:552 */       boolean changed = false;
/* 469:553 */       for (Iterator<ShortObjectMap.PrimitiveEntry<V>> iter = ShortObjectHashMap.this.entries().iterator(); iter.hasNext();)
/* 470:    */       {
/* 471:554 */         ShortObjectMap.PrimitiveEntry<V> entry = (ShortObjectMap.PrimitiveEntry)iter.next();
/* 472:555 */         if (!retainedKeys.contains(Short.valueOf(entry.key())))
/* 473:    */         {
/* 474:556 */           changed = true;
/* 475:557 */           iter.remove();
/* 476:    */         }
/* 477:    */       }
/* 478:560 */       return changed;
/* 479:    */     }
/* 480:    */     
/* 481:    */     public void clear()
/* 482:    */     {
/* 483:565 */       ShortObjectHashMap.this.clear();
/* 484:    */     }
/* 485:    */     
/* 486:    */     public Iterator<Short> iterator()
/* 487:    */     {
/* 488:570 */       new Iterator()
/* 489:    */       {
/* 490:571 */         private final Iterator<Map.Entry<Short, V>> iter = ShortObjectHashMap.this.entrySet.iterator();
/* 491:    */         
/* 492:    */         public boolean hasNext()
/* 493:    */         {
/* 494:575 */           return this.iter.hasNext();
/* 495:    */         }
/* 496:    */         
/* 497:    */         public Short next()
/* 498:    */         {
/* 499:580 */           return (Short)((Map.Entry)this.iter.next()).getKey();
/* 500:    */         }
/* 501:    */         
/* 502:    */         public void remove()
/* 503:    */         {
/* 504:585 */           this.iter.remove();
/* 505:    */         }
/* 506:    */       };
/* 507:    */     }
/* 508:    */   }
/* 509:    */   
/* 510:    */   private final class PrimitiveIterator
/* 511:    */     implements Iterator<ShortObjectMap.PrimitiveEntry<V>>, ShortObjectMap.PrimitiveEntry<V>
/* 512:    */   {
/* 513:595 */     private int prevIndex = -1;
/* 514:596 */     private int nextIndex = -1;
/* 515:597 */     private int entryIndex = -1;
/* 516:    */     
/* 517:    */     private PrimitiveIterator() {}
/* 518:    */     
/* 519:    */     private void scanNext()
/* 520:    */     {
/* 521:600 */       while ((++this.nextIndex != ShortObjectHashMap.this.values.length) && (ShortObjectHashMap.this.values[this.nextIndex] == null)) {}
/* 522:    */     }
/* 523:    */     
/* 524:    */     public boolean hasNext()
/* 525:    */     {
/* 526:606 */       if (this.nextIndex == -1) {
/* 527:607 */         scanNext();
/* 528:    */       }
/* 529:609 */       return this.nextIndex != ShortObjectHashMap.this.values.length;
/* 530:    */     }
/* 531:    */     
/* 532:    */     public ShortObjectMap.PrimitiveEntry<V> next()
/* 533:    */     {
/* 534:614 */       if (!hasNext()) {
/* 535:615 */         throw new NoSuchElementException();
/* 536:    */       }
/* 537:618 */       this.prevIndex = this.nextIndex;
/* 538:619 */       scanNext();
/* 539:    */       
/* 540:    */ 
/* 541:622 */       this.entryIndex = this.prevIndex;
/* 542:623 */       return this;
/* 543:    */     }
/* 544:    */     
/* 545:    */     public void remove()
/* 546:    */     {
/* 547:628 */       if (this.prevIndex == -1) {
/* 548:629 */         throw new IllegalStateException("next must be called before each remove.");
/* 549:    */       }
/* 550:631 */       if (ShortObjectHashMap.this.removeAt(this.prevIndex)) {
/* 551:635 */         this.nextIndex = this.prevIndex;
/* 552:    */       }
/* 553:637 */       this.prevIndex = -1;
/* 554:    */     }
/* 555:    */     
/* 556:    */     public short key()
/* 557:    */     {
/* 558:645 */       return ShortObjectHashMap.this.keys[this.entryIndex];
/* 559:    */     }
/* 560:    */     
/* 561:    */     public V value()
/* 562:    */     {
/* 563:650 */       return ShortObjectHashMap.toExternal(ShortObjectHashMap.this.values[this.entryIndex]);
/* 564:    */     }
/* 565:    */     
/* 566:    */     public void setValue(V value)
/* 567:    */     {
/* 568:655 */       ShortObjectHashMap.this.values[this.entryIndex] = ShortObjectHashMap.toInternal(value);
/* 569:    */     }
/* 570:    */   }
/* 571:    */   
/* 572:    */   private final class MapIterator
/* 573:    */     implements Iterator<Map.Entry<Short, V>>
/* 574:    */   {
/* 575:663 */     private final ShortObjectHashMap<V>.PrimitiveIterator iter = new ShortObjectHashMap.PrimitiveIterator(ShortObjectHashMap.this, null);
/* 576:    */     
/* 577:    */     private MapIterator() {}
/* 578:    */     
/* 579:    */     public boolean hasNext()
/* 580:    */     {
/* 581:667 */       return this.iter.hasNext();
/* 582:    */     }
/* 583:    */     
/* 584:    */     public Map.Entry<Short, V> next()
/* 585:    */     {
/* 586:672 */       if (!hasNext()) {
/* 587:673 */         throw new NoSuchElementException();
/* 588:    */       }
/* 589:676 */       this.iter.next();
/* 590:    */       
/* 591:678 */       return new ShortObjectHashMap.MapEntry(ShortObjectHashMap.this, ShortObjectHashMap.PrimitiveIterator.access$1100(this.iter));
/* 592:    */     }
/* 593:    */     
/* 594:    */     public void remove()
/* 595:    */     {
/* 596:683 */       this.iter.remove();
/* 597:    */     }
/* 598:    */   }
/* 599:    */   
/* 600:    */   final class MapEntry
/* 601:    */     implements Map.Entry<Short, V>
/* 602:    */   {
/* 603:    */     private final int entryIndex;
/* 604:    */     
/* 605:    */     MapEntry(int entryIndex)
/* 606:    */     {
/* 607:694 */       this.entryIndex = entryIndex;
/* 608:    */     }
/* 609:    */     
/* 610:    */     public Short getKey()
/* 611:    */     {
/* 612:699 */       verifyExists();
/* 613:700 */       return Short.valueOf(ShortObjectHashMap.this.keys[this.entryIndex]);
/* 614:    */     }
/* 615:    */     
/* 616:    */     public V getValue()
/* 617:    */     {
/* 618:705 */       verifyExists();
/* 619:706 */       return ShortObjectHashMap.toExternal(ShortObjectHashMap.this.values[this.entryIndex]);
/* 620:    */     }
/* 621:    */     
/* 622:    */     public V setValue(V value)
/* 623:    */     {
/* 624:711 */       verifyExists();
/* 625:712 */       V prevValue = ShortObjectHashMap.toExternal(ShortObjectHashMap.this.values[this.entryIndex]);
/* 626:713 */       ShortObjectHashMap.this.values[this.entryIndex] = ShortObjectHashMap.toInternal(value);
/* 627:714 */       return prevValue;
/* 628:    */     }
/* 629:    */     
/* 630:    */     private void verifyExists()
/* 631:    */     {
/* 632:718 */       if (ShortObjectHashMap.this.values[this.entryIndex] == null) {
/* 633:719 */         throw new IllegalStateException("The map entry has been removed");
/* 634:    */       }
/* 635:    */     }
/* 636:    */   }
/* 637:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.collection.ShortObjectHashMap
 * JD-Core Version:    0.7.0.1
 */