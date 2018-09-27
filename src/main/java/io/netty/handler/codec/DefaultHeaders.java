/*    1:     */ package io.netty.handler.codec;
/*    2:     */ 
/*    3:     */ import io.netty.util.HashingStrategy;
/*    4:     */ import io.netty.util.internal.MathUtil;
/*    5:     */ import io.netty.util.internal.ObjectUtil;
/*    6:     */ import java.util.Arrays;
/*    7:     */ import java.util.Collections;
/*    8:     */ import java.util.Iterator;
/*    9:     */ import java.util.LinkedHashSet;
/*   10:     */ import java.util.LinkedList;
/*   11:     */ import java.util.List;
/*   12:     */ import java.util.Map.Entry;
/*   13:     */ import java.util.NoSuchElementException;
/*   14:     */ import java.util.Set;
/*   15:     */ 
/*   16:     */ public class DefaultHeaders<K, V, T extends Headers<K, V, T>>
/*   17:     */   implements Headers<K, V, T>
/*   18:     */ {
/*   19:     */   static final int HASH_CODE_SEED = -1028477387;
/*   20:     */   private final HeaderEntry<K, V>[] entries;
/*   21:     */   protected final HeaderEntry<K, V> head;
/*   22:     */   private final byte hashMask;
/*   23:     */   private final ValueConverter<V> valueConverter;
/*   24:     */   private final NameValidator<K> nameValidator;
/*   25:     */   private final HashingStrategy<K> hashingStrategy;
/*   26:     */   int size;
/*   27:     */   
/*   28:     */   public static abstract interface NameValidator<K>
/*   29:     */   {
/*   30:  67 */     public static final NameValidator NOT_NULL = new NameValidator()
/*   31:     */     {
/*   32:     */       public void validateName(Object name)
/*   33:     */       {
/*   34:  70 */         ObjectUtil.checkNotNull(name, "name");
/*   35:     */       }
/*   36:     */     };
/*   37:     */     
/*   38:     */     public abstract void validateName(K paramK);
/*   39:     */   }
/*   40:     */   
/*   41:     */   public DefaultHeaders(ValueConverter<V> valueConverter)
/*   42:     */   {
/*   43:  77 */     this(HashingStrategy.JAVA_HASHER, valueConverter);
/*   44:     */   }
/*   45:     */   
/*   46:     */   public DefaultHeaders(ValueConverter<V> valueConverter, NameValidator<K> nameValidator)
/*   47:     */   {
/*   48:  82 */     this(HashingStrategy.JAVA_HASHER, valueConverter, nameValidator);
/*   49:     */   }
/*   50:     */   
/*   51:     */   public DefaultHeaders(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter)
/*   52:     */   {
/*   53:  87 */     this(nameHashingStrategy, valueConverter, NameValidator.NOT_NULL);
/*   54:     */   }
/*   55:     */   
/*   56:     */   public DefaultHeaders(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter, NameValidator<K> nameValidator)
/*   57:     */   {
/*   58:  92 */     this(nameHashingStrategy, valueConverter, nameValidator, 16);
/*   59:     */   }
/*   60:     */   
/*   61:     */   public DefaultHeaders(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter, NameValidator<K> nameValidator, int arraySizeHint)
/*   62:     */   {
/*   63: 106 */     this.valueConverter = ((ValueConverter)ObjectUtil.checkNotNull(valueConverter, "valueConverter"));
/*   64: 107 */     this.nameValidator = ((NameValidator)ObjectUtil.checkNotNull(nameValidator, "nameValidator"));
/*   65: 108 */     this.hashingStrategy = ((HashingStrategy)ObjectUtil.checkNotNull(nameHashingStrategy, "nameHashingStrategy"));
/*   66:     */     
/*   67:     */ 
/*   68: 111 */     this.entries = new HeaderEntry[MathUtil.findNextPositivePowerOfTwo(Math.max(2, Math.min(arraySizeHint, 128)))];
/*   69: 112 */     this.hashMask = ((byte)(this.entries.length - 1));
/*   70: 113 */     this.head = new HeaderEntry();
/*   71:     */   }
/*   72:     */   
/*   73:     */   public V get(K name)
/*   74:     */   {
/*   75: 118 */     ObjectUtil.checkNotNull(name, "name");
/*   76:     */     
/*   77: 120 */     int h = this.hashingStrategy.hashCode(name);
/*   78: 121 */     int i = index(h);
/*   79: 122 */     HeaderEntry<K, V> e = this.entries[i];
/*   80: 123 */     V value = null;
/*   81: 125 */     while (e != null)
/*   82:     */     {
/*   83: 126 */       if ((e.hash == h) && (this.hashingStrategy.equals(name, e.key))) {
/*   84: 127 */         value = e.value;
/*   85:     */       }
/*   86: 130 */       e = e.next;
/*   87:     */     }
/*   88: 132 */     return value;
/*   89:     */   }
/*   90:     */   
/*   91:     */   public V get(K name, V defaultValue)
/*   92:     */   {
/*   93: 137 */     V value = get(name);
/*   94: 138 */     if (value == null) {
/*   95: 139 */       return defaultValue;
/*   96:     */     }
/*   97: 141 */     return value;
/*   98:     */   }
/*   99:     */   
/*  100:     */   public V getAndRemove(K name)
/*  101:     */   {
/*  102: 146 */     int h = this.hashingStrategy.hashCode(name);
/*  103: 147 */     return remove0(h, index(h), ObjectUtil.checkNotNull(name, "name"));
/*  104:     */   }
/*  105:     */   
/*  106:     */   public V getAndRemove(K name, V defaultValue)
/*  107:     */   {
/*  108: 152 */     V value = getAndRemove(name);
/*  109: 153 */     if (value == null) {
/*  110: 154 */       return defaultValue;
/*  111:     */     }
/*  112: 156 */     return value;
/*  113:     */   }
/*  114:     */   
/*  115:     */   public List<V> getAll(K name)
/*  116:     */   {
/*  117: 161 */     ObjectUtil.checkNotNull(name, "name");
/*  118:     */     
/*  119: 163 */     LinkedList<V> values = new LinkedList();
/*  120:     */     
/*  121: 165 */     int h = this.hashingStrategy.hashCode(name);
/*  122: 166 */     int i = index(h);
/*  123: 167 */     HeaderEntry<K, V> e = this.entries[i];
/*  124: 168 */     while (e != null)
/*  125:     */     {
/*  126: 169 */       if ((e.hash == h) && (this.hashingStrategy.equals(name, e.key))) {
/*  127: 170 */         values.addFirst(e.getValue());
/*  128:     */       }
/*  129: 172 */       e = e.next;
/*  130:     */     }
/*  131: 174 */     return values;
/*  132:     */   }
/*  133:     */   
/*  134:     */   public Iterator<V> valueIterator(K name)
/*  135:     */   {
/*  136: 183 */     return new ValueIterator(name);
/*  137:     */   }
/*  138:     */   
/*  139:     */   public List<V> getAllAndRemove(K name)
/*  140:     */   {
/*  141: 188 */     List<V> all = getAll(name);
/*  142: 189 */     remove(name);
/*  143: 190 */     return all;
/*  144:     */   }
/*  145:     */   
/*  146:     */   public boolean contains(K name)
/*  147:     */   {
/*  148: 195 */     return get(name) != null;
/*  149:     */   }
/*  150:     */   
/*  151:     */   public boolean containsObject(K name, Object value)
/*  152:     */   {
/*  153: 200 */     return contains(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
/*  154:     */   }
/*  155:     */   
/*  156:     */   public boolean containsBoolean(K name, boolean value)
/*  157:     */   {
/*  158: 205 */     return contains(name, this.valueConverter.convertBoolean(value));
/*  159:     */   }
/*  160:     */   
/*  161:     */   public boolean containsByte(K name, byte value)
/*  162:     */   {
/*  163: 210 */     return contains(name, this.valueConverter.convertByte(value));
/*  164:     */   }
/*  165:     */   
/*  166:     */   public boolean containsChar(K name, char value)
/*  167:     */   {
/*  168: 215 */     return contains(name, this.valueConverter.convertChar(value));
/*  169:     */   }
/*  170:     */   
/*  171:     */   public boolean containsShort(K name, short value)
/*  172:     */   {
/*  173: 220 */     return contains(name, this.valueConverter.convertShort(value));
/*  174:     */   }
/*  175:     */   
/*  176:     */   public boolean containsInt(K name, int value)
/*  177:     */   {
/*  178: 225 */     return contains(name, this.valueConverter.convertInt(value));
/*  179:     */   }
/*  180:     */   
/*  181:     */   public boolean containsLong(K name, long value)
/*  182:     */   {
/*  183: 230 */     return contains(name, this.valueConverter.convertLong(value));
/*  184:     */   }
/*  185:     */   
/*  186:     */   public boolean containsFloat(K name, float value)
/*  187:     */   {
/*  188: 235 */     return contains(name, this.valueConverter.convertFloat(value));
/*  189:     */   }
/*  190:     */   
/*  191:     */   public boolean containsDouble(K name, double value)
/*  192:     */   {
/*  193: 240 */     return contains(name, this.valueConverter.convertDouble(value));
/*  194:     */   }
/*  195:     */   
/*  196:     */   public boolean containsTimeMillis(K name, long value)
/*  197:     */   {
/*  198: 245 */     return contains(name, this.valueConverter.convertTimeMillis(value));
/*  199:     */   }
/*  200:     */   
/*  201:     */   public boolean contains(K name, V value)
/*  202:     */   {
/*  203: 251 */     return contains(name, value, HashingStrategy.JAVA_HASHER);
/*  204:     */   }
/*  205:     */   
/*  206:     */   public final boolean contains(K name, V value, HashingStrategy<? super V> valueHashingStrategy)
/*  207:     */   {
/*  208: 255 */     ObjectUtil.checkNotNull(name, "name");
/*  209:     */     
/*  210: 257 */     int h = this.hashingStrategy.hashCode(name);
/*  211: 258 */     int i = index(h);
/*  212: 259 */     HeaderEntry<K, V> e = this.entries[i];
/*  213: 260 */     while (e != null)
/*  214:     */     {
/*  215: 261 */       if ((e.hash == h) && (this.hashingStrategy.equals(name, e.key)) && (valueHashingStrategy.equals(value, e.value))) {
/*  216: 262 */         return true;
/*  217:     */       }
/*  218: 264 */       e = e.next;
/*  219:     */     }
/*  220: 266 */     return false;
/*  221:     */   }
/*  222:     */   
/*  223:     */   public int size()
/*  224:     */   {
/*  225: 271 */     return this.size;
/*  226:     */   }
/*  227:     */   
/*  228:     */   public boolean isEmpty()
/*  229:     */   {
/*  230: 276 */     return this.head == this.head.after;
/*  231:     */   }
/*  232:     */   
/*  233:     */   public Set<K> names()
/*  234:     */   {
/*  235: 281 */     if (isEmpty()) {
/*  236: 282 */       return Collections.emptySet();
/*  237:     */     }
/*  238: 284 */     Set<K> names = new LinkedHashSet(size());
/*  239: 285 */     HeaderEntry<K, V> e = this.head.after;
/*  240: 286 */     while (e != this.head)
/*  241:     */     {
/*  242: 287 */       names.add(e.getKey());
/*  243: 288 */       e = e.after;
/*  244:     */     }
/*  245: 290 */     return names;
/*  246:     */   }
/*  247:     */   
/*  248:     */   public T add(K name, V value)
/*  249:     */   {
/*  250: 295 */     this.nameValidator.validateName(name);
/*  251: 296 */     ObjectUtil.checkNotNull(value, "value");
/*  252: 297 */     int h = this.hashingStrategy.hashCode(name);
/*  253: 298 */     int i = index(h);
/*  254: 299 */     add0(h, i, name, value);
/*  255: 300 */     return thisT();
/*  256:     */   }
/*  257:     */   
/*  258:     */   public T add(K name, Iterable<? extends V> values)
/*  259:     */   {
/*  260: 305 */     this.nameValidator.validateName(name);
/*  261: 306 */     int h = this.hashingStrategy.hashCode(name);
/*  262: 307 */     int i = index(h);
/*  263: 308 */     for (V v : values) {
/*  264: 309 */       add0(h, i, name, v);
/*  265:     */     }
/*  266: 311 */     return thisT();
/*  267:     */   }
/*  268:     */   
/*  269:     */   public T add(K name, V... values)
/*  270:     */   {
/*  271: 316 */     this.nameValidator.validateName(name);
/*  272: 317 */     int h = this.hashingStrategy.hashCode(name);
/*  273: 318 */     int i = index(h);
/*  274: 319 */     for (V v : values) {
/*  275: 320 */       add0(h, i, name, v);
/*  276:     */     }
/*  277: 322 */     return thisT();
/*  278:     */   }
/*  279:     */   
/*  280:     */   public T addObject(K name, Object value)
/*  281:     */   {
/*  282: 327 */     return add(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
/*  283:     */   }
/*  284:     */   
/*  285:     */   public T addObject(K name, Iterable<?> values)
/*  286:     */   {
/*  287: 332 */     for (Object value : values) {
/*  288: 333 */       addObject(name, value);
/*  289:     */     }
/*  290: 335 */     return thisT();
/*  291:     */   }
/*  292:     */   
/*  293:     */   public T addObject(K name, Object... values)
/*  294:     */   {
/*  295: 340 */     for (Object value : values) {
/*  296: 341 */       addObject(name, value);
/*  297:     */     }
/*  298: 343 */     return thisT();
/*  299:     */   }
/*  300:     */   
/*  301:     */   public T addInt(K name, int value)
/*  302:     */   {
/*  303: 348 */     return add(name, this.valueConverter.convertInt(value));
/*  304:     */   }
/*  305:     */   
/*  306:     */   public T addLong(K name, long value)
/*  307:     */   {
/*  308: 353 */     return add(name, this.valueConverter.convertLong(value));
/*  309:     */   }
/*  310:     */   
/*  311:     */   public T addDouble(K name, double value)
/*  312:     */   {
/*  313: 358 */     return add(name, this.valueConverter.convertDouble(value));
/*  314:     */   }
/*  315:     */   
/*  316:     */   public T addTimeMillis(K name, long value)
/*  317:     */   {
/*  318: 363 */     return add(name, this.valueConverter.convertTimeMillis(value));
/*  319:     */   }
/*  320:     */   
/*  321:     */   public T addChar(K name, char value)
/*  322:     */   {
/*  323: 368 */     return add(name, this.valueConverter.convertChar(value));
/*  324:     */   }
/*  325:     */   
/*  326:     */   public T addBoolean(K name, boolean value)
/*  327:     */   {
/*  328: 373 */     return add(name, this.valueConverter.convertBoolean(value));
/*  329:     */   }
/*  330:     */   
/*  331:     */   public T addFloat(K name, float value)
/*  332:     */   {
/*  333: 378 */     return add(name, this.valueConverter.convertFloat(value));
/*  334:     */   }
/*  335:     */   
/*  336:     */   public T addByte(K name, byte value)
/*  337:     */   {
/*  338: 383 */     return add(name, this.valueConverter.convertByte(value));
/*  339:     */   }
/*  340:     */   
/*  341:     */   public T addShort(K name, short value)
/*  342:     */   {
/*  343: 388 */     return add(name, this.valueConverter.convertShort(value));
/*  344:     */   }
/*  345:     */   
/*  346:     */   public T add(Headers<? extends K, ? extends V, ?> headers)
/*  347:     */   {
/*  348: 393 */     if (headers == this) {
/*  349: 394 */       throw new IllegalArgumentException("can't add to itself.");
/*  350:     */     }
/*  351: 396 */     addImpl(headers);
/*  352: 397 */     return thisT();
/*  353:     */   }
/*  354:     */   
/*  355:     */   protected void addImpl(Headers<? extends K, ? extends V, ?> headers)
/*  356:     */   {
/*  357:     */     DefaultHeaders<? extends K, ? extends V, T> defaultHeaders;
/*  358: 401 */     if ((headers instanceof DefaultHeaders))
/*  359:     */     {
/*  360: 403 */       defaultHeaders = (DefaultHeaders)headers;
/*  361:     */       
/*  362: 405 */       HeaderEntry<? extends K, ? extends V> e = defaultHeaders.head.after;
/*  363: 406 */       if ((defaultHeaders.hashingStrategy == this.hashingStrategy) && (defaultHeaders.nameValidator == this.nameValidator)) {}
/*  364: 409 */       while (e != defaultHeaders.head)
/*  365:     */       {
/*  366: 410 */         add0(e.hash, index(e.hash), e.key, e.value);
/*  367: 411 */         e = e.after; continue;
/*  368: 415 */         while (e != defaultHeaders.head)
/*  369:     */         {
/*  370: 416 */           add(e.key, e.value);
/*  371: 417 */           e = e.after;
/*  372:     */         }
/*  373:     */       }
/*  374:     */     }
/*  375:     */     else
/*  376:     */     {
/*  377: 422 */       for (Map.Entry<? extends K, ? extends V> header : headers) {
/*  378: 423 */         add(header.getKey(), header.getValue());
/*  379:     */       }
/*  380:     */     }
/*  381:     */   }
/*  382:     */   
/*  383:     */   public T set(K name, V value)
/*  384:     */   {
/*  385: 430 */     this.nameValidator.validateName(name);
/*  386: 431 */     ObjectUtil.checkNotNull(value, "value");
/*  387: 432 */     int h = this.hashingStrategy.hashCode(name);
/*  388: 433 */     int i = index(h);
/*  389: 434 */     remove0(h, i, name);
/*  390: 435 */     add0(h, i, name, value);
/*  391: 436 */     return thisT();
/*  392:     */   }
/*  393:     */   
/*  394:     */   public T set(K name, Iterable<? extends V> values)
/*  395:     */   {
/*  396: 441 */     this.nameValidator.validateName(name);
/*  397: 442 */     ObjectUtil.checkNotNull(values, "values");
/*  398:     */     
/*  399: 444 */     int h = this.hashingStrategy.hashCode(name);
/*  400: 445 */     int i = index(h);
/*  401:     */     
/*  402: 447 */     remove0(h, i, name);
/*  403: 448 */     for (V v : values)
/*  404:     */     {
/*  405: 449 */       if (v == null) {
/*  406:     */         break;
/*  407:     */       }
/*  408: 452 */       add0(h, i, name, v);
/*  409:     */     }
/*  410: 455 */     return thisT();
/*  411:     */   }
/*  412:     */   
/*  413:     */   public T set(K name, V... values)
/*  414:     */   {
/*  415: 460 */     this.nameValidator.validateName(name);
/*  416: 461 */     ObjectUtil.checkNotNull(values, "values");
/*  417:     */     
/*  418: 463 */     int h = this.hashingStrategy.hashCode(name);
/*  419: 464 */     int i = index(h);
/*  420:     */     
/*  421: 466 */     remove0(h, i, name);
/*  422: 467 */     for (V v : values)
/*  423:     */     {
/*  424: 468 */       if (v == null) {
/*  425:     */         break;
/*  426:     */       }
/*  427: 471 */       add0(h, i, name, v);
/*  428:     */     }
/*  429: 474 */     return thisT();
/*  430:     */   }
/*  431:     */   
/*  432:     */   public T setObject(K name, Object value)
/*  433:     */   {
/*  434: 479 */     ObjectUtil.checkNotNull(value, "value");
/*  435: 480 */     V convertedValue = ObjectUtil.checkNotNull(this.valueConverter.convertObject(value), "convertedValue");
/*  436: 481 */     return set(name, convertedValue);
/*  437:     */   }
/*  438:     */   
/*  439:     */   public T setObject(K name, Iterable<?> values)
/*  440:     */   {
/*  441: 486 */     this.nameValidator.validateName(name);
/*  442:     */     
/*  443: 488 */     int h = this.hashingStrategy.hashCode(name);
/*  444: 489 */     int i = index(h);
/*  445:     */     
/*  446: 491 */     remove0(h, i, name);
/*  447: 492 */     for (Object v : values)
/*  448:     */     {
/*  449: 493 */       if (v == null) {
/*  450:     */         break;
/*  451:     */       }
/*  452: 496 */       add0(h, i, name, this.valueConverter.convertObject(v));
/*  453:     */     }
/*  454: 499 */     return thisT();
/*  455:     */   }
/*  456:     */   
/*  457:     */   public T setObject(K name, Object... values)
/*  458:     */   {
/*  459: 504 */     this.nameValidator.validateName(name);
/*  460:     */     
/*  461: 506 */     int h = this.hashingStrategy.hashCode(name);
/*  462: 507 */     int i = index(h);
/*  463:     */     
/*  464: 509 */     remove0(h, i, name);
/*  465: 510 */     for (Object v : values)
/*  466:     */     {
/*  467: 511 */       if (v == null) {
/*  468:     */         break;
/*  469:     */       }
/*  470: 514 */       add0(h, i, name, this.valueConverter.convertObject(v));
/*  471:     */     }
/*  472: 517 */     return thisT();
/*  473:     */   }
/*  474:     */   
/*  475:     */   public T setInt(K name, int value)
/*  476:     */   {
/*  477: 522 */     return set(name, this.valueConverter.convertInt(value));
/*  478:     */   }
/*  479:     */   
/*  480:     */   public T setLong(K name, long value)
/*  481:     */   {
/*  482: 527 */     return set(name, this.valueConverter.convertLong(value));
/*  483:     */   }
/*  484:     */   
/*  485:     */   public T setDouble(K name, double value)
/*  486:     */   {
/*  487: 532 */     return set(name, this.valueConverter.convertDouble(value));
/*  488:     */   }
/*  489:     */   
/*  490:     */   public T setTimeMillis(K name, long value)
/*  491:     */   {
/*  492: 537 */     return set(name, this.valueConverter.convertTimeMillis(value));
/*  493:     */   }
/*  494:     */   
/*  495:     */   public T setFloat(K name, float value)
/*  496:     */   {
/*  497: 542 */     return set(name, this.valueConverter.convertFloat(value));
/*  498:     */   }
/*  499:     */   
/*  500:     */   public T setChar(K name, char value)
/*  501:     */   {
/*  502: 547 */     return set(name, this.valueConverter.convertChar(value));
/*  503:     */   }
/*  504:     */   
/*  505:     */   public T setBoolean(K name, boolean value)
/*  506:     */   {
/*  507: 552 */     return set(name, this.valueConverter.convertBoolean(value));
/*  508:     */   }
/*  509:     */   
/*  510:     */   public T setByte(K name, byte value)
/*  511:     */   {
/*  512: 557 */     return set(name, this.valueConverter.convertByte(value));
/*  513:     */   }
/*  514:     */   
/*  515:     */   public T setShort(K name, short value)
/*  516:     */   {
/*  517: 562 */     return set(name, this.valueConverter.convertShort(value));
/*  518:     */   }
/*  519:     */   
/*  520:     */   public T set(Headers<? extends K, ? extends V, ?> headers)
/*  521:     */   {
/*  522: 567 */     if (headers != this)
/*  523:     */     {
/*  524: 568 */       clear();
/*  525: 569 */       addImpl(headers);
/*  526:     */     }
/*  527: 571 */     return thisT();
/*  528:     */   }
/*  529:     */   
/*  530:     */   public T setAll(Headers<? extends K, ? extends V, ?> headers)
/*  531:     */   {
/*  532: 576 */     if (headers != this)
/*  533:     */     {
/*  534: 577 */       for (K key : headers.names()) {
/*  535: 578 */         remove(key);
/*  536:     */       }
/*  537: 580 */       addImpl(headers);
/*  538:     */     }
/*  539: 582 */     return thisT();
/*  540:     */   }
/*  541:     */   
/*  542:     */   public boolean remove(K name)
/*  543:     */   {
/*  544: 587 */     return getAndRemove(name) != null;
/*  545:     */   }
/*  546:     */   
/*  547:     */   public T clear()
/*  548:     */   {
/*  549: 592 */     Arrays.fill(this.entries, null);
/*  550: 593 */     this.head.before = (this.head.after = this.head);
/*  551: 594 */     this.size = 0;
/*  552: 595 */     return thisT();
/*  553:     */   }
/*  554:     */   
/*  555:     */   public Iterator<Map.Entry<K, V>> iterator()
/*  556:     */   {
/*  557: 600 */     return new HeaderIterator(null);
/*  558:     */   }
/*  559:     */   
/*  560:     */   public Boolean getBoolean(K name)
/*  561:     */   {
/*  562: 605 */     V v = get(name);
/*  563: 606 */     return v != null ? Boolean.valueOf(this.valueConverter.convertToBoolean(v)) : null;
/*  564:     */   }
/*  565:     */   
/*  566:     */   public boolean getBoolean(K name, boolean defaultValue)
/*  567:     */   {
/*  568: 611 */     Boolean v = getBoolean(name);
/*  569: 612 */     return v != null ? v.booleanValue() : defaultValue;
/*  570:     */   }
/*  571:     */   
/*  572:     */   public Byte getByte(K name)
/*  573:     */   {
/*  574: 617 */     V v = get(name);
/*  575: 618 */     return v != null ? Byte.valueOf(this.valueConverter.convertToByte(v)) : null;
/*  576:     */   }
/*  577:     */   
/*  578:     */   public byte getByte(K name, byte defaultValue)
/*  579:     */   {
/*  580: 623 */     Byte v = getByte(name);
/*  581: 624 */     return v != null ? v.byteValue() : defaultValue;
/*  582:     */   }
/*  583:     */   
/*  584:     */   public Character getChar(K name)
/*  585:     */   {
/*  586: 629 */     V v = get(name);
/*  587: 630 */     return v != null ? Character.valueOf(this.valueConverter.convertToChar(v)) : null;
/*  588:     */   }
/*  589:     */   
/*  590:     */   public char getChar(K name, char defaultValue)
/*  591:     */   {
/*  592: 635 */     Character v = getChar(name);
/*  593: 636 */     return v != null ? v.charValue() : defaultValue;
/*  594:     */   }
/*  595:     */   
/*  596:     */   public Short getShort(K name)
/*  597:     */   {
/*  598: 641 */     V v = get(name);
/*  599: 642 */     return v != null ? Short.valueOf(this.valueConverter.convertToShort(v)) : null;
/*  600:     */   }
/*  601:     */   
/*  602:     */   public short getShort(K name, short defaultValue)
/*  603:     */   {
/*  604: 647 */     Short v = getShort(name);
/*  605: 648 */     return v != null ? v.shortValue() : defaultValue;
/*  606:     */   }
/*  607:     */   
/*  608:     */   public Integer getInt(K name)
/*  609:     */   {
/*  610: 653 */     V v = get(name);
/*  611: 654 */     return v != null ? Integer.valueOf(this.valueConverter.convertToInt(v)) : null;
/*  612:     */   }
/*  613:     */   
/*  614:     */   public int getInt(K name, int defaultValue)
/*  615:     */   {
/*  616: 659 */     Integer v = getInt(name);
/*  617: 660 */     return v != null ? v.intValue() : defaultValue;
/*  618:     */   }
/*  619:     */   
/*  620:     */   public Long getLong(K name)
/*  621:     */   {
/*  622: 665 */     V v = get(name);
/*  623: 666 */     return v != null ? Long.valueOf(this.valueConverter.convertToLong(v)) : null;
/*  624:     */   }
/*  625:     */   
/*  626:     */   public long getLong(K name, long defaultValue)
/*  627:     */   {
/*  628: 671 */     Long v = getLong(name);
/*  629: 672 */     return v != null ? v.longValue() : defaultValue;
/*  630:     */   }
/*  631:     */   
/*  632:     */   public Float getFloat(K name)
/*  633:     */   {
/*  634: 677 */     V v = get(name);
/*  635: 678 */     return v != null ? Float.valueOf(this.valueConverter.convertToFloat(v)) : null;
/*  636:     */   }
/*  637:     */   
/*  638:     */   public float getFloat(K name, float defaultValue)
/*  639:     */   {
/*  640: 683 */     Float v = getFloat(name);
/*  641: 684 */     return v != null ? v.floatValue() : defaultValue;
/*  642:     */   }
/*  643:     */   
/*  644:     */   public Double getDouble(K name)
/*  645:     */   {
/*  646: 689 */     V v = get(name);
/*  647: 690 */     return v != null ? Double.valueOf(this.valueConverter.convertToDouble(v)) : null;
/*  648:     */   }
/*  649:     */   
/*  650:     */   public double getDouble(K name, double defaultValue)
/*  651:     */   {
/*  652: 695 */     Double v = getDouble(name);
/*  653: 696 */     return v != null ? v.doubleValue() : defaultValue;
/*  654:     */   }
/*  655:     */   
/*  656:     */   public Long getTimeMillis(K name)
/*  657:     */   {
/*  658: 701 */     V v = get(name);
/*  659: 702 */     return v != null ? Long.valueOf(this.valueConverter.convertToTimeMillis(v)) : null;
/*  660:     */   }
/*  661:     */   
/*  662:     */   public long getTimeMillis(K name, long defaultValue)
/*  663:     */   {
/*  664: 707 */     Long v = getTimeMillis(name);
/*  665: 708 */     return v != null ? v.longValue() : defaultValue;
/*  666:     */   }
/*  667:     */   
/*  668:     */   public Boolean getBooleanAndRemove(K name)
/*  669:     */   {
/*  670: 713 */     V v = getAndRemove(name);
/*  671: 714 */     return v != null ? Boolean.valueOf(this.valueConverter.convertToBoolean(v)) : null;
/*  672:     */   }
/*  673:     */   
/*  674:     */   public boolean getBooleanAndRemove(K name, boolean defaultValue)
/*  675:     */   {
/*  676: 719 */     Boolean v = getBooleanAndRemove(name);
/*  677: 720 */     return v != null ? v.booleanValue() : defaultValue;
/*  678:     */   }
/*  679:     */   
/*  680:     */   public Byte getByteAndRemove(K name)
/*  681:     */   {
/*  682: 725 */     V v = getAndRemove(name);
/*  683: 726 */     return v != null ? Byte.valueOf(this.valueConverter.convertToByte(v)) : null;
/*  684:     */   }
/*  685:     */   
/*  686:     */   public byte getByteAndRemove(K name, byte defaultValue)
/*  687:     */   {
/*  688: 731 */     Byte v = getByteAndRemove(name);
/*  689: 732 */     return v != null ? v.byteValue() : defaultValue;
/*  690:     */   }
/*  691:     */   
/*  692:     */   public Character getCharAndRemove(K name)
/*  693:     */   {
/*  694: 737 */     V v = getAndRemove(name);
/*  695: 738 */     if (v == null) {
/*  696: 739 */       return null;
/*  697:     */     }
/*  698:     */     try
/*  699:     */     {
/*  700: 742 */       return Character.valueOf(this.valueConverter.convertToChar(v));
/*  701:     */     }
/*  702:     */     catch (Throwable ignored) {}
/*  703: 744 */     return null;
/*  704:     */   }
/*  705:     */   
/*  706:     */   public char getCharAndRemove(K name, char defaultValue)
/*  707:     */   {
/*  708: 750 */     Character v = getCharAndRemove(name);
/*  709: 751 */     return v != null ? v.charValue() : defaultValue;
/*  710:     */   }
/*  711:     */   
/*  712:     */   public Short getShortAndRemove(K name)
/*  713:     */   {
/*  714: 756 */     V v = getAndRemove(name);
/*  715: 757 */     return v != null ? Short.valueOf(this.valueConverter.convertToShort(v)) : null;
/*  716:     */   }
/*  717:     */   
/*  718:     */   public short getShortAndRemove(K name, short defaultValue)
/*  719:     */   {
/*  720: 762 */     Short v = getShortAndRemove(name);
/*  721: 763 */     return v != null ? v.shortValue() : defaultValue;
/*  722:     */   }
/*  723:     */   
/*  724:     */   public Integer getIntAndRemove(K name)
/*  725:     */   {
/*  726: 768 */     V v = getAndRemove(name);
/*  727: 769 */     return v != null ? Integer.valueOf(this.valueConverter.convertToInt(v)) : null;
/*  728:     */   }
/*  729:     */   
/*  730:     */   public int getIntAndRemove(K name, int defaultValue)
/*  731:     */   {
/*  732: 774 */     Integer v = getIntAndRemove(name);
/*  733: 775 */     return v != null ? v.intValue() : defaultValue;
/*  734:     */   }
/*  735:     */   
/*  736:     */   public Long getLongAndRemove(K name)
/*  737:     */   {
/*  738: 780 */     V v = getAndRemove(name);
/*  739: 781 */     return v != null ? Long.valueOf(this.valueConverter.convertToLong(v)) : null;
/*  740:     */   }
/*  741:     */   
/*  742:     */   public long getLongAndRemove(K name, long defaultValue)
/*  743:     */   {
/*  744: 786 */     Long v = getLongAndRemove(name);
/*  745: 787 */     return v != null ? v.longValue() : defaultValue;
/*  746:     */   }
/*  747:     */   
/*  748:     */   public Float getFloatAndRemove(K name)
/*  749:     */   {
/*  750: 792 */     V v = getAndRemove(name);
/*  751: 793 */     return v != null ? Float.valueOf(this.valueConverter.convertToFloat(v)) : null;
/*  752:     */   }
/*  753:     */   
/*  754:     */   public float getFloatAndRemove(K name, float defaultValue)
/*  755:     */   {
/*  756: 798 */     Float v = getFloatAndRemove(name);
/*  757: 799 */     return v != null ? v.floatValue() : defaultValue;
/*  758:     */   }
/*  759:     */   
/*  760:     */   public Double getDoubleAndRemove(K name)
/*  761:     */   {
/*  762: 804 */     V v = getAndRemove(name);
/*  763: 805 */     return v != null ? Double.valueOf(this.valueConverter.convertToDouble(v)) : null;
/*  764:     */   }
/*  765:     */   
/*  766:     */   public double getDoubleAndRemove(K name, double defaultValue)
/*  767:     */   {
/*  768: 810 */     Double v = getDoubleAndRemove(name);
/*  769: 811 */     return v != null ? v.doubleValue() : defaultValue;
/*  770:     */   }
/*  771:     */   
/*  772:     */   public Long getTimeMillisAndRemove(K name)
/*  773:     */   {
/*  774: 816 */     V v = getAndRemove(name);
/*  775: 817 */     return v != null ? Long.valueOf(this.valueConverter.convertToTimeMillis(v)) : null;
/*  776:     */   }
/*  777:     */   
/*  778:     */   public long getTimeMillisAndRemove(K name, long defaultValue)
/*  779:     */   {
/*  780: 822 */     Long v = getTimeMillisAndRemove(name);
/*  781: 823 */     return v != null ? v.longValue() : defaultValue;
/*  782:     */   }
/*  783:     */   
/*  784:     */   public boolean equals(Object o)
/*  785:     */   {
/*  786: 829 */     if (!(o instanceof Headers)) {
/*  787: 830 */       return false;
/*  788:     */     }
/*  789: 833 */     return equals((Headers)o, HashingStrategy.JAVA_HASHER);
/*  790:     */   }
/*  791:     */   
/*  792:     */   public int hashCode()
/*  793:     */   {
/*  794: 839 */     return hashCode(HashingStrategy.JAVA_HASHER);
/*  795:     */   }
/*  796:     */   
/*  797:     */   public final boolean equals(Headers<K, V, ?> h2, HashingStrategy<V> valueHashingStrategy)
/*  798:     */   {
/*  799: 850 */     if (h2.size() != size()) {
/*  800: 851 */       return false;
/*  801:     */     }
/*  802: 854 */     if (this == h2) {
/*  803: 855 */       return true;
/*  804:     */     }
/*  805: 858 */     for (K name : names())
/*  806:     */     {
/*  807: 859 */       List<V> otherValues = h2.getAll(name);
/*  808: 860 */       List<V> values = getAll(name);
/*  809: 861 */       if (otherValues.size() != values.size()) {
/*  810: 862 */         return false;
/*  811:     */       }
/*  812: 864 */       for (int i = 0; i < otherValues.size(); i++) {
/*  813: 865 */         if (!valueHashingStrategy.equals(otherValues.get(i), values.get(i))) {
/*  814: 866 */           return false;
/*  815:     */         }
/*  816:     */       }
/*  817:     */     }
/*  818: 870 */     return true;
/*  819:     */   }
/*  820:     */   
/*  821:     */   public final int hashCode(HashingStrategy<V> valueHashingStrategy)
/*  822:     */   {
/*  823: 879 */     int result = -1028477387;
/*  824: 880 */     for (K name : names())
/*  825:     */     {
/*  826: 881 */       result = 31 * result + this.hashingStrategy.hashCode(name);
/*  827: 882 */       List<V> values = getAll(name);
/*  828: 883 */       for (int i = 0; i < values.size(); i++) {
/*  829: 884 */         result = 31 * result + valueHashingStrategy.hashCode(values.get(i));
/*  830:     */       }
/*  831:     */     }
/*  832: 887 */     return result;
/*  833:     */   }
/*  834:     */   
/*  835:     */   public String toString()
/*  836:     */   {
/*  837: 892 */     return HeadersUtils.toString(getClass(), iterator(), size());
/*  838:     */   }
/*  839:     */   
/*  840:     */   protected HeaderEntry<K, V> newHeaderEntry(int h, K name, V value, HeaderEntry<K, V> next)
/*  841:     */   {
/*  842: 896 */     return new HeaderEntry(h, name, value, next, this.head);
/*  843:     */   }
/*  844:     */   
/*  845:     */   protected ValueConverter<V> valueConverter()
/*  846:     */   {
/*  847: 900 */     return this.valueConverter;
/*  848:     */   }
/*  849:     */   
/*  850:     */   private int index(int hash)
/*  851:     */   {
/*  852: 904 */     return hash & this.hashMask;
/*  853:     */   }
/*  854:     */   
/*  855:     */   private void add0(int h, int i, K name, V value)
/*  856:     */   {
/*  857: 909 */     this.entries[i] = newHeaderEntry(h, name, value, this.entries[i]);
/*  858: 910 */     this.size += 1;
/*  859:     */   }
/*  860:     */   
/*  861:     */   private V remove0(int h, int i, K name)
/*  862:     */   {
/*  863: 917 */     HeaderEntry<K, V> e = this.entries[i];
/*  864: 918 */     if (e == null) {
/*  865: 919 */       return null;
/*  866:     */     }
/*  867: 922 */     V value = null;
/*  868: 923 */     HeaderEntry<K, V> next = e.next;
/*  869: 924 */     while (next != null)
/*  870:     */     {
/*  871: 925 */       if ((next.hash == h) && (this.hashingStrategy.equals(name, next.key)))
/*  872:     */       {
/*  873: 926 */         value = next.value;
/*  874: 927 */         e.next = next.next;
/*  875: 928 */         next.remove();
/*  876: 929 */         this.size -= 1;
/*  877:     */       }
/*  878:     */       else
/*  879:     */       {
/*  880: 931 */         e = next;
/*  881:     */       }
/*  882: 934 */       next = e.next;
/*  883:     */     }
/*  884: 937 */     e = this.entries[i];
/*  885: 938 */     if ((e.hash == h) && (this.hashingStrategy.equals(name, e.key)))
/*  886:     */     {
/*  887: 939 */       if (value == null) {
/*  888: 940 */         value = e.value;
/*  889:     */       }
/*  890: 942 */       this.entries[i] = e.next;
/*  891: 943 */       e.remove();
/*  892: 944 */       this.size -= 1;
/*  893:     */     }
/*  894: 947 */     return value;
/*  895:     */   }
/*  896:     */   
/*  897:     */   private T thisT()
/*  898:     */   {
/*  899: 952 */     return this;
/*  900:     */   }
/*  901:     */   
/*  902:     */   private final class HeaderIterator
/*  903:     */     implements Iterator<Map.Entry<K, V>>
/*  904:     */   {
/*  905: 956 */     private DefaultHeaders.HeaderEntry<K, V> current = DefaultHeaders.this.head;
/*  906:     */     
/*  907:     */     private HeaderIterator() {}
/*  908:     */     
/*  909:     */     public boolean hasNext()
/*  910:     */     {
/*  911: 960 */       return this.current.after != DefaultHeaders.this.head;
/*  912:     */     }
/*  913:     */     
/*  914:     */     public Map.Entry<K, V> next()
/*  915:     */     {
/*  916: 965 */       this.current = this.current.after;
/*  917: 967 */       if (this.current == DefaultHeaders.this.head) {
/*  918: 968 */         throw new NoSuchElementException();
/*  919:     */       }
/*  920: 971 */       return this.current;
/*  921:     */     }
/*  922:     */     
/*  923:     */     public void remove()
/*  924:     */     {
/*  925: 976 */       throw new UnsupportedOperationException("read only");
/*  926:     */     }
/*  927:     */   }
/*  928:     */   
/*  929:     */   private final class ValueIterator
/*  930:     */     implements Iterator<V>
/*  931:     */   {
/*  932:     */     private final K name;
/*  933:     */     private final int hash;
/*  934:     */     private DefaultHeaders.HeaderEntry<K, V> next;
/*  935:     */     
/*  936:     */     ValueIterator()
/*  937:     */     {
/*  938: 986 */       this.name = ObjectUtil.checkNotNull(name, "name");
/*  939: 987 */       this.hash = DefaultHeaders.this.hashingStrategy.hashCode(name);
/*  940: 988 */       calculateNext(DefaultHeaders.this.entries[DefaultHeaders.this.index(this.hash)]);
/*  941:     */     }
/*  942:     */     
/*  943:     */     public boolean hasNext()
/*  944:     */     {
/*  945: 993 */       return this.next != null;
/*  946:     */     }
/*  947:     */     
/*  948:     */     public V next()
/*  949:     */     {
/*  950: 998 */       if (!hasNext()) {
/*  951: 999 */         throw new NoSuchElementException();
/*  952:     */       }
/*  953:1001 */       DefaultHeaders.HeaderEntry<K, V> current = this.next;
/*  954:1002 */       calculateNext(this.next.next);
/*  955:1003 */       return current.value;
/*  956:     */     }
/*  957:     */     
/*  958:     */     public void remove()
/*  959:     */     {
/*  960:1008 */       throw new UnsupportedOperationException("read only");
/*  961:     */     }
/*  962:     */     
/*  963:     */     private void calculateNext(DefaultHeaders.HeaderEntry<K, V> entry)
/*  964:     */     {
/*  965:1012 */       while (entry != null)
/*  966:     */       {
/*  967:1013 */         if ((entry.hash == this.hash) && (DefaultHeaders.this.hashingStrategy.equals(this.name, entry.key)))
/*  968:     */         {
/*  969:1014 */           this.next = entry;
/*  970:1015 */           return;
/*  971:     */         }
/*  972:1017 */         entry = entry.next;
/*  973:     */       }
/*  974:1019 */       this.next = null;
/*  975:     */     }
/*  976:     */   }
/*  977:     */   
/*  978:     */   protected static class HeaderEntry<K, V>
/*  979:     */     implements Map.Entry<K, V>
/*  980:     */   {
/*  981:     */     protected final int hash;
/*  982:     */     protected final K key;
/*  983:     */     protected V value;
/*  984:     */     protected HeaderEntry<K, V> next;
/*  985:     */     protected HeaderEntry<K, V> before;
/*  986:     */     protected HeaderEntry<K, V> after;
/*  987:     */     
/*  988:     */     protected HeaderEntry(int hash, K key)
/*  989:     */     {
/*  990:1037 */       this.hash = hash;
/*  991:1038 */       this.key = key;
/*  992:     */     }
/*  993:     */     
/*  994:     */     HeaderEntry(int hash, K key, V value, HeaderEntry<K, V> next, HeaderEntry<K, V> head)
/*  995:     */     {
/*  996:1042 */       this.hash = hash;
/*  997:1043 */       this.key = key;
/*  998:1044 */       this.value = value;
/*  999:1045 */       this.next = next;
/* 1000:     */       
/* 1001:1047 */       this.after = head;
/* 1002:1048 */       this.before = head.before;
/* 1003:1049 */       pointNeighborsToThis();
/* 1004:     */     }
/* 1005:     */     
/* 1006:     */     HeaderEntry()
/* 1007:     */     {
/* 1008:1053 */       this.hash = -1;
/* 1009:1054 */       this.key = null;
/* 1010:1055 */       this.after = this;this.before = this;
/* 1011:     */     }
/* 1012:     */     
/* 1013:     */     protected final void pointNeighborsToThis()
/* 1014:     */     {
/* 1015:1059 */       this.before.after = this;
/* 1016:1060 */       this.after.before = this;
/* 1017:     */     }
/* 1018:     */     
/* 1019:     */     public final HeaderEntry<K, V> before()
/* 1020:     */     {
/* 1021:1064 */       return this.before;
/* 1022:     */     }
/* 1023:     */     
/* 1024:     */     public final HeaderEntry<K, V> after()
/* 1025:     */     {
/* 1026:1068 */       return this.after;
/* 1027:     */     }
/* 1028:     */     
/* 1029:     */     protected void remove()
/* 1030:     */     {
/* 1031:1072 */       this.before.after = this.after;
/* 1032:1073 */       this.after.before = this.before;
/* 1033:     */     }
/* 1034:     */     
/* 1035:     */     public final K getKey()
/* 1036:     */     {
/* 1037:1078 */       return this.key;
/* 1038:     */     }
/* 1039:     */     
/* 1040:     */     public final V getValue()
/* 1041:     */     {
/* 1042:1083 */       return this.value;
/* 1043:     */     }
/* 1044:     */     
/* 1045:     */     public final V setValue(V value)
/* 1046:     */     {
/* 1047:1088 */       ObjectUtil.checkNotNull(value, "value");
/* 1048:1089 */       V oldValue = this.value;
/* 1049:1090 */       this.value = value;
/* 1050:1091 */       return oldValue;
/* 1051:     */     }
/* 1052:     */     
/* 1053:     */     public final String toString()
/* 1054:     */     {
/* 1055:1096 */       return this.key.toString() + '=' + this.value.toString();
/* 1056:     */     }
/* 1057:     */   }
/* 1058:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DefaultHeaders
 * JD-Core Version:    0.7.0.1
 */