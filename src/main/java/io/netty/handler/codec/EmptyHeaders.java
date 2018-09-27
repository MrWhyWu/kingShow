/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import java.util.Collections;
/*   4:    */ import java.util.Iterator;
/*   5:    */ import java.util.List;
/*   6:    */ import java.util.Map.Entry;
/*   7:    */ import java.util.Set;
/*   8:    */ 
/*   9:    */ public class EmptyHeaders<K, V, T extends Headers<K, V, T>>
/*  10:    */   implements Headers<K, V, T>
/*  11:    */ {
/*  12:    */   public V get(K name)
/*  13:    */   {
/*  14: 28 */     return null;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public V get(K name, V defaultValue)
/*  18:    */   {
/*  19: 33 */     return null;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public V getAndRemove(K name)
/*  23:    */   {
/*  24: 38 */     return null;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public V getAndRemove(K name, V defaultValue)
/*  28:    */   {
/*  29: 43 */     return null;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public List<V> getAll(K name)
/*  33:    */   {
/*  34: 48 */     return Collections.emptyList();
/*  35:    */   }
/*  36:    */   
/*  37:    */   public List<V> getAllAndRemove(K name)
/*  38:    */   {
/*  39: 53 */     return Collections.emptyList();
/*  40:    */   }
/*  41:    */   
/*  42:    */   public Boolean getBoolean(K name)
/*  43:    */   {
/*  44: 58 */     return null;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean getBoolean(K name, boolean defaultValue)
/*  48:    */   {
/*  49: 63 */     return defaultValue;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public Byte getByte(K name)
/*  53:    */   {
/*  54: 68 */     return null;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public byte getByte(K name, byte defaultValue)
/*  58:    */   {
/*  59: 73 */     return defaultValue;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public Character getChar(K name)
/*  63:    */   {
/*  64: 78 */     return null;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public char getChar(K name, char defaultValue)
/*  68:    */   {
/*  69: 83 */     return defaultValue;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public Short getShort(K name)
/*  73:    */   {
/*  74: 88 */     return null;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public short getShort(K name, short defaultValue)
/*  78:    */   {
/*  79: 93 */     return defaultValue;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public Integer getInt(K name)
/*  83:    */   {
/*  84: 98 */     return null;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public int getInt(K name, int defaultValue)
/*  88:    */   {
/*  89:103 */     return defaultValue;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Long getLong(K name)
/*  93:    */   {
/*  94:108 */     return null;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public long getLong(K name, long defaultValue)
/*  98:    */   {
/*  99:113 */     return defaultValue;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public Float getFloat(K name)
/* 103:    */   {
/* 104:118 */     return null;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public float getFloat(K name, float defaultValue)
/* 108:    */   {
/* 109:123 */     return defaultValue;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public Double getDouble(K name)
/* 113:    */   {
/* 114:128 */     return null;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public double getDouble(K name, double defaultValue)
/* 118:    */   {
/* 119:133 */     return defaultValue;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public Long getTimeMillis(K name)
/* 123:    */   {
/* 124:138 */     return null;
/* 125:    */   }
/* 126:    */   
/* 127:    */   public long getTimeMillis(K name, long defaultValue)
/* 128:    */   {
/* 129:143 */     return defaultValue;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public Boolean getBooleanAndRemove(K name)
/* 133:    */   {
/* 134:148 */     return null;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public boolean getBooleanAndRemove(K name, boolean defaultValue)
/* 138:    */   {
/* 139:153 */     return defaultValue;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public Byte getByteAndRemove(K name)
/* 143:    */   {
/* 144:158 */     return null;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public byte getByteAndRemove(K name, byte defaultValue)
/* 148:    */   {
/* 149:163 */     return defaultValue;
/* 150:    */   }
/* 151:    */   
/* 152:    */   public Character getCharAndRemove(K name)
/* 153:    */   {
/* 154:168 */     return null;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public char getCharAndRemove(K name, char defaultValue)
/* 158:    */   {
/* 159:173 */     return defaultValue;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public Short getShortAndRemove(K name)
/* 163:    */   {
/* 164:178 */     return null;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public short getShortAndRemove(K name, short defaultValue)
/* 168:    */   {
/* 169:183 */     return defaultValue;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public Integer getIntAndRemove(K name)
/* 173:    */   {
/* 174:188 */     return null;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public int getIntAndRemove(K name, int defaultValue)
/* 178:    */   {
/* 179:193 */     return defaultValue;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public Long getLongAndRemove(K name)
/* 183:    */   {
/* 184:198 */     return null;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public long getLongAndRemove(K name, long defaultValue)
/* 188:    */   {
/* 189:203 */     return defaultValue;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public Float getFloatAndRemove(K name)
/* 193:    */   {
/* 194:208 */     return null;
/* 195:    */   }
/* 196:    */   
/* 197:    */   public float getFloatAndRemove(K name, float defaultValue)
/* 198:    */   {
/* 199:213 */     return defaultValue;
/* 200:    */   }
/* 201:    */   
/* 202:    */   public Double getDoubleAndRemove(K name)
/* 203:    */   {
/* 204:218 */     return null;
/* 205:    */   }
/* 206:    */   
/* 207:    */   public double getDoubleAndRemove(K name, double defaultValue)
/* 208:    */   {
/* 209:223 */     return defaultValue;
/* 210:    */   }
/* 211:    */   
/* 212:    */   public Long getTimeMillisAndRemove(K name)
/* 213:    */   {
/* 214:228 */     return null;
/* 215:    */   }
/* 216:    */   
/* 217:    */   public long getTimeMillisAndRemove(K name, long defaultValue)
/* 218:    */   {
/* 219:233 */     return defaultValue;
/* 220:    */   }
/* 221:    */   
/* 222:    */   public boolean contains(K name)
/* 223:    */   {
/* 224:238 */     return false;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public boolean contains(K name, V value)
/* 228:    */   {
/* 229:243 */     return false;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public boolean containsObject(K name, Object value)
/* 233:    */   {
/* 234:248 */     return false;
/* 235:    */   }
/* 236:    */   
/* 237:    */   public boolean containsBoolean(K name, boolean value)
/* 238:    */   {
/* 239:253 */     return false;
/* 240:    */   }
/* 241:    */   
/* 242:    */   public boolean containsByte(K name, byte value)
/* 243:    */   {
/* 244:258 */     return false;
/* 245:    */   }
/* 246:    */   
/* 247:    */   public boolean containsChar(K name, char value)
/* 248:    */   {
/* 249:263 */     return false;
/* 250:    */   }
/* 251:    */   
/* 252:    */   public boolean containsShort(K name, short value)
/* 253:    */   {
/* 254:268 */     return false;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public boolean containsInt(K name, int value)
/* 258:    */   {
/* 259:273 */     return false;
/* 260:    */   }
/* 261:    */   
/* 262:    */   public boolean containsLong(K name, long value)
/* 263:    */   {
/* 264:278 */     return false;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public boolean containsFloat(K name, float value)
/* 268:    */   {
/* 269:283 */     return false;
/* 270:    */   }
/* 271:    */   
/* 272:    */   public boolean containsDouble(K name, double value)
/* 273:    */   {
/* 274:288 */     return false;
/* 275:    */   }
/* 276:    */   
/* 277:    */   public boolean containsTimeMillis(K name, long value)
/* 278:    */   {
/* 279:293 */     return false;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public int size()
/* 283:    */   {
/* 284:298 */     return 0;
/* 285:    */   }
/* 286:    */   
/* 287:    */   public boolean isEmpty()
/* 288:    */   {
/* 289:303 */     return true;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public Set<K> names()
/* 293:    */   {
/* 294:308 */     return Collections.emptySet();
/* 295:    */   }
/* 296:    */   
/* 297:    */   public T add(K name, V value)
/* 298:    */   {
/* 299:313 */     throw new UnsupportedOperationException("read only");
/* 300:    */   }
/* 301:    */   
/* 302:    */   public T add(K name, Iterable<? extends V> values)
/* 303:    */   {
/* 304:318 */     throw new UnsupportedOperationException("read only");
/* 305:    */   }
/* 306:    */   
/* 307:    */   public T add(K name, V... values)
/* 308:    */   {
/* 309:323 */     throw new UnsupportedOperationException("read only");
/* 310:    */   }
/* 311:    */   
/* 312:    */   public T addObject(K name, Object value)
/* 313:    */   {
/* 314:328 */     throw new UnsupportedOperationException("read only");
/* 315:    */   }
/* 316:    */   
/* 317:    */   public T addObject(K name, Iterable<?> values)
/* 318:    */   {
/* 319:333 */     throw new UnsupportedOperationException("read only");
/* 320:    */   }
/* 321:    */   
/* 322:    */   public T addObject(K name, Object... values)
/* 323:    */   {
/* 324:338 */     throw new UnsupportedOperationException("read only");
/* 325:    */   }
/* 326:    */   
/* 327:    */   public T addBoolean(K name, boolean value)
/* 328:    */   {
/* 329:343 */     throw new UnsupportedOperationException("read only");
/* 330:    */   }
/* 331:    */   
/* 332:    */   public T addByte(K name, byte value)
/* 333:    */   {
/* 334:348 */     throw new UnsupportedOperationException("read only");
/* 335:    */   }
/* 336:    */   
/* 337:    */   public T addChar(K name, char value)
/* 338:    */   {
/* 339:353 */     throw new UnsupportedOperationException("read only");
/* 340:    */   }
/* 341:    */   
/* 342:    */   public T addShort(K name, short value)
/* 343:    */   {
/* 344:358 */     throw new UnsupportedOperationException("read only");
/* 345:    */   }
/* 346:    */   
/* 347:    */   public T addInt(K name, int value)
/* 348:    */   {
/* 349:363 */     throw new UnsupportedOperationException("read only");
/* 350:    */   }
/* 351:    */   
/* 352:    */   public T addLong(K name, long value)
/* 353:    */   {
/* 354:368 */     throw new UnsupportedOperationException("read only");
/* 355:    */   }
/* 356:    */   
/* 357:    */   public T addFloat(K name, float value)
/* 358:    */   {
/* 359:373 */     throw new UnsupportedOperationException("read only");
/* 360:    */   }
/* 361:    */   
/* 362:    */   public T addDouble(K name, double value)
/* 363:    */   {
/* 364:378 */     throw new UnsupportedOperationException("read only");
/* 365:    */   }
/* 366:    */   
/* 367:    */   public T addTimeMillis(K name, long value)
/* 368:    */   {
/* 369:383 */     throw new UnsupportedOperationException("read only");
/* 370:    */   }
/* 371:    */   
/* 372:    */   public T add(Headers<? extends K, ? extends V, ?> headers)
/* 373:    */   {
/* 374:388 */     throw new UnsupportedOperationException("read only");
/* 375:    */   }
/* 376:    */   
/* 377:    */   public T set(K name, V value)
/* 378:    */   {
/* 379:393 */     throw new UnsupportedOperationException("read only");
/* 380:    */   }
/* 381:    */   
/* 382:    */   public T set(K name, Iterable<? extends V> values)
/* 383:    */   {
/* 384:398 */     throw new UnsupportedOperationException("read only");
/* 385:    */   }
/* 386:    */   
/* 387:    */   public T set(K name, V... values)
/* 388:    */   {
/* 389:403 */     throw new UnsupportedOperationException("read only");
/* 390:    */   }
/* 391:    */   
/* 392:    */   public T setObject(K name, Object value)
/* 393:    */   {
/* 394:408 */     throw new UnsupportedOperationException("read only");
/* 395:    */   }
/* 396:    */   
/* 397:    */   public T setObject(K name, Iterable<?> values)
/* 398:    */   {
/* 399:413 */     throw new UnsupportedOperationException("read only");
/* 400:    */   }
/* 401:    */   
/* 402:    */   public T setObject(K name, Object... values)
/* 403:    */   {
/* 404:418 */     throw new UnsupportedOperationException("read only");
/* 405:    */   }
/* 406:    */   
/* 407:    */   public T setBoolean(K name, boolean value)
/* 408:    */   {
/* 409:423 */     throw new UnsupportedOperationException("read only");
/* 410:    */   }
/* 411:    */   
/* 412:    */   public T setByte(K name, byte value)
/* 413:    */   {
/* 414:428 */     throw new UnsupportedOperationException("read only");
/* 415:    */   }
/* 416:    */   
/* 417:    */   public T setChar(K name, char value)
/* 418:    */   {
/* 419:433 */     throw new UnsupportedOperationException("read only");
/* 420:    */   }
/* 421:    */   
/* 422:    */   public T setShort(K name, short value)
/* 423:    */   {
/* 424:438 */     throw new UnsupportedOperationException("read only");
/* 425:    */   }
/* 426:    */   
/* 427:    */   public T setInt(K name, int value)
/* 428:    */   {
/* 429:443 */     throw new UnsupportedOperationException("read only");
/* 430:    */   }
/* 431:    */   
/* 432:    */   public T setLong(K name, long value)
/* 433:    */   {
/* 434:448 */     throw new UnsupportedOperationException("read only");
/* 435:    */   }
/* 436:    */   
/* 437:    */   public T setFloat(K name, float value)
/* 438:    */   {
/* 439:453 */     throw new UnsupportedOperationException("read only");
/* 440:    */   }
/* 441:    */   
/* 442:    */   public T setDouble(K name, double value)
/* 443:    */   {
/* 444:458 */     throw new UnsupportedOperationException("read only");
/* 445:    */   }
/* 446:    */   
/* 447:    */   public T setTimeMillis(K name, long value)
/* 448:    */   {
/* 449:463 */     throw new UnsupportedOperationException("read only");
/* 450:    */   }
/* 451:    */   
/* 452:    */   public T set(Headers<? extends K, ? extends V, ?> headers)
/* 453:    */   {
/* 454:468 */     throw new UnsupportedOperationException("read only");
/* 455:    */   }
/* 456:    */   
/* 457:    */   public T setAll(Headers<? extends K, ? extends V, ?> headers)
/* 458:    */   {
/* 459:473 */     throw new UnsupportedOperationException("read only");
/* 460:    */   }
/* 461:    */   
/* 462:    */   public boolean remove(K name)
/* 463:    */   {
/* 464:478 */     return false;
/* 465:    */   }
/* 466:    */   
/* 467:    */   public T clear()
/* 468:    */   {
/* 469:483 */     return thisT();
/* 470:    */   }
/* 471:    */   
/* 472:    */   public Iterator<V> valueIterator(K name)
/* 473:    */   {
/* 474:492 */     List<V> empty = Collections.emptyList();
/* 475:493 */     return empty.iterator();
/* 476:    */   }
/* 477:    */   
/* 478:    */   public Iterator<Map.Entry<K, V>> iterator()
/* 479:    */   {
/* 480:498 */     List<Map.Entry<K, V>> empty = Collections.emptyList();
/* 481:499 */     return empty.iterator();
/* 482:    */   }
/* 483:    */   
/* 484:    */   public boolean equals(Object o)
/* 485:    */   {
/* 486:504 */     if (!(o instanceof Headers)) {
/* 487:505 */       return false;
/* 488:    */     }
/* 489:508 */     Headers<?, ?, ?> rhs = (Headers)o;
/* 490:509 */     return (isEmpty()) && (rhs.isEmpty());
/* 491:    */   }
/* 492:    */   
/* 493:    */   public int hashCode()
/* 494:    */   {
/* 495:514 */     return -1028477387;
/* 496:    */   }
/* 497:    */   
/* 498:    */   public String toString()
/* 499:    */   {
/* 500:519 */     return getClass().getSimpleName() + '[' + ']';
/* 501:    */   }
/* 502:    */   
/* 503:    */   private T thisT()
/* 504:    */   {
/* 505:524 */     return this;
/* 506:    */   }
/* 507:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.EmptyHeaders
 * JD-Core Version:    0.7.0.1
 */