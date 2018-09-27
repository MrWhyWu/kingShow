/*    1:     */ package io.protostuff.runtime;
/*    2:     */ 
/*    3:     */ import io.protostuff.CollectionSchema.MessageFactory;
/*    4:     */ import io.protostuff.GraphInput;
/*    5:     */ import io.protostuff.Input;
/*    6:     */ import io.protostuff.Output;
/*    7:     */ import io.protostuff.Pipe;
/*    8:     */ import io.protostuff.Pipe.Schema;
/*    9:     */ import io.protostuff.ProtostuffException;
/*   10:     */ import io.protostuff.Schema;
/*   11:     */ import io.protostuff.StatefulOutput;
/*   12:     */ import java.io.IOException;
/*   13:     */ import java.lang.reflect.Field;
/*   14:     */ import java.util.Collection;
/*   15:     */ import java.util.Collections;
/*   16:     */ import java.util.EnumSet;
/*   17:     */ import java.util.IdentityHashMap;
/*   18:     */ import java.util.List;
/*   19:     */ import java.util.Map;
/*   20:     */ 
/*   21:     */ public abstract class PolymorphicCollectionSchema
/*   22:     */   extends PolymorphicSchema
/*   23:     */ {
/*   24:     */   static final int ID_EMPTY_SET = 1;
/*   25:     */   static final int ID_EMPTY_LIST = 2;
/*   26:     */   static final int ID_SINGLETON_SET = 3;
/*   27:     */   static final int ID_SINGLETON_LIST = 4;
/*   28:     */   static final int ID_SET_FROM_MAP = 5;
/*   29:     */   static final int ID_COPIES_LIST = 6;
/*   30:     */   static final int ID_UNMODIFIABLE_COLLECTION = 7;
/*   31:     */   static final int ID_UNMODIFIABLE_SET = 8;
/*   32:     */   static final int ID_UNMODIFIABLE_SORTED_SET = 9;
/*   33:     */   static final int ID_UNMODIFIABLE_LIST = 10;
/*   34:     */   static final int ID_UNMODIFIABLE_RANDOM_ACCESS_LIST = 11;
/*   35:     */   static final int ID_SYNCHRONIZED_COLLECTION = 12;
/*   36:     */   static final int ID_SYNCHRONIZED_SET = 13;
/*   37:     */   static final int ID_SYNCHRONIZED_SORTED_SET = 14;
/*   38:     */   static final int ID_SYNCHRONIZED_LIST = 15;
/*   39:     */   static final int ID_SYNCHRONIZED_RANDOM_ACCESS_LIST = 16;
/*   40:     */   static final int ID_CHECKED_COLLECTION = 17;
/*   41:     */   static final int ID_CHECKED_SET = 18;
/*   42:     */   static final int ID_CHECKED_SORTED_SET = 19;
/*   43:     */   static final int ID_CHECKED_LIST = 20;
/*   44:     */   static final int ID_CHECKED_RANDOM_ACCESS_LIST = 21;
/*   45:     */   static final String STR_EMPTY_SET = "a";
/*   46:     */   static final String STR_EMPTY_LIST = "b";
/*   47:     */   static final String STR_SINGLETON_SET = "c";
/*   48:     */   static final String STR_SINGLETON_LIST = "d";
/*   49:     */   static final String STR_SET_FROM_MAP = "e";
/*   50:     */   static final String STR_COPIES_LIST = "f";
/*   51:     */   static final String STR_UNMODIFIABLE_COLLECTION = "g";
/*   52:     */   static final String STR_UNMODIFIABLE_SET = "h";
/*   53:     */   static final String STR_UNMODIFIABLE_SORTED_SET = "i";
/*   54:     */   static final String STR_UNMODIFIABLE_LIST = "j";
/*   55:     */   static final String STR_UNMODIFIABLE_RANDOM_ACCESS_LIST = "k";
/*   56:     */   static final String STR_SYNCHRONIZED_COLLECTION = "l";
/*   57:     */   static final String STR_SYNCHRONIZED_SET = "m";
/*   58:     */   static final String STR_SYNCHRONIZED_SORTED_SET = "n";
/*   59:     */   static final String STR_SYNCHRONIZED_LIST = "o";
/*   60:     */   static final String STR_SYNCHRONIZED_RANDOM_ACCESS_LIST = "p";
/*   61:     */   static final String STR_CHECKED_COLLECTION = "q";
/*   62:     */   static final String STR_CHECKED_SET = "r";
/*   63:     */   static final String STR_CHECKED_SORTED_SET = "s";
/*   64:     */   static final String STR_CHECKED_LIST = "t";
/*   65:     */   static final String STR_CHECKED_RANDOM_ACCESS_LIST = "u";
/*   66: 102 */   static final IdentityHashMap<Class<?>, Integer> __nonPublicCollections = new IdentityHashMap();
/*   67:     */   static final Field fSingletonSet_element;
/*   68:     */   static final Field fSingletonList_element;
/*   69:     */   static final Field fUnmodifiableCollection_c;
/*   70:     */   static final Field fUnmodifiableSortedSet_ss;
/*   71:     */   static final Field fUnmodifiableList_list;
/*   72:     */   static final Field fSynchronizedCollection_c;
/*   73:     */   static final Field fSynchronizedSortedSet_ss;
/*   74:     */   static final Field fSynchronizedList_list;
/*   75:     */   static final Field fSynchronizedCollection_mutex;
/*   76:     */   static final Field fCheckedCollection_c;
/*   77:     */   static final Field fCheckedSortedSet_ss;
/*   78:     */   static final Field fCheckedList_list;
/*   79:     */   static final Field fCheckedCollection_type;
/*   80:     */   static final Field fSetFromMap_m;
/*   81:     */   static final Field fSetFromMap_s;
/*   82:     */   static final Field fCopiesList_n;
/*   83:     */   static final Field fCopiesList_element;
/*   84:     */   static final RuntimeEnv.Instantiator<?> iSingletonSet;
/*   85:     */   static final RuntimeEnv.Instantiator<?> iSingletonList;
/*   86:     */   static final RuntimeEnv.Instantiator<?> iUnmodifiableCollection;
/*   87:     */   static final RuntimeEnv.Instantiator<?> iUnmodifiableSet;
/*   88:     */   static final RuntimeEnv.Instantiator<?> iUnmodifiableSortedSet;
/*   89:     */   static final RuntimeEnv.Instantiator<?> iUnmodifiableList;
/*   90:     */   static final RuntimeEnv.Instantiator<?> iUnmodifiableRandomAccessList;
/*   91:     */   static final RuntimeEnv.Instantiator<?> iSynchronizedCollection;
/*   92:     */   static final RuntimeEnv.Instantiator<?> iSynchronizedSet;
/*   93:     */   static final RuntimeEnv.Instantiator<?> iSynchronizedSortedSet;
/*   94:     */   static final RuntimeEnv.Instantiator<?> iSynchronizedList;
/*   95:     */   static final RuntimeEnv.Instantiator<?> iSynchronizedRandomAccessList;
/*   96:     */   static final RuntimeEnv.Instantiator<?> iCheckedCollection;
/*   97:     */   static final RuntimeEnv.Instantiator<?> iCheckedSet;
/*   98:     */   static final RuntimeEnv.Instantiator<?> iCheckedSortedSet;
/*   99:     */   static final RuntimeEnv.Instantiator<?> iCheckedList;
/*  100:     */   static final RuntimeEnv.Instantiator<?> iCheckedRandomAccessList;
/*  101:     */   static final RuntimeEnv.Instantiator<?> iSetFromMap;
/*  102:     */   static final RuntimeEnv.Instantiator<?> iCopiesList;
/*  103:     */   
/*  104:     */   static
/*  105:     */   {
/*  106: 136 */     map("java.util.Collections$EmptySet", 1);
/*  107: 137 */     map("java.util.Collections$EmptyList", 2);
/*  108:     */     
/*  109: 139 */     Class<?> cSingletonSet = map("java.util.Collections$SingletonSet", 3);
/*  110:     */     
/*  111:     */ 
/*  112: 142 */     Class<?> cSingletonList = map("java.util.Collections$SingletonList", 4);
/*  113:     */     
/*  114:     */ 
/*  115: 145 */     Class<?> cSetFromMap = map("java.util.Collections$SetFromMap", 5);
/*  116:     */     
/*  117:     */ 
/*  118: 148 */     Class<?> cCopiesList = map("java.util.Collections$CopiesList", 6);
/*  119:     */     
/*  120:     */ 
/*  121: 151 */     Class<?> cUnmodifiableCollection = map("java.util.Collections$UnmodifiableCollection", 7);
/*  122:     */     
/*  123:     */ 
/*  124: 154 */     Class<?> cUnmodifiableSet = map("java.util.Collections$UnmodifiableSet", 8);
/*  125:     */     
/*  126: 156 */     Class<?> cUnmodifiableSortedSet = map("java.util.Collections$UnmodifiableSortedSet", 9);
/*  127:     */     
/*  128:     */ 
/*  129: 159 */     Class<?> cUnmodifiableList = map("java.util.Collections$UnmodifiableList", 10);
/*  130:     */     
/*  131: 161 */     Class<?> cUnmodifiableRandomAccessList = map("java.util.Collections$UnmodifiableRandomAccessList", 11);
/*  132:     */     
/*  133:     */ 
/*  134:     */ 
/*  135: 165 */     Class<?> cSynchronizedCollection = map("java.util.Collections$SynchronizedCollection", 12);
/*  136:     */     
/*  137:     */ 
/*  138: 168 */     Class<?> cSynchronizedSet = map("java.util.Collections$SynchronizedSet", 13);
/*  139:     */     
/*  140: 170 */     Class<?> cSynchronizedSortedSet = map("java.util.Collections$SynchronizedSortedSet", 14);
/*  141:     */     
/*  142:     */ 
/*  143: 173 */     Class<?> cSynchronizedList = map("java.util.Collections$SynchronizedList", 15);
/*  144:     */     
/*  145: 175 */     Class<?> cSynchronizedRandomAccessList = map("java.util.Collections$SynchronizedRandomAccessList", 16);
/*  146:     */     
/*  147:     */ 
/*  148:     */ 
/*  149: 179 */     Class<?> cCheckedCollection = map("java.util.Collections$CheckedCollection", 17);
/*  150:     */     
/*  151:     */ 
/*  152: 182 */     Class<?> cCheckedSet = map("java.util.Collections$CheckedSet", 18);
/*  153:     */     
/*  154: 184 */     Class<?> cCheckedSortedSet = map("java.util.Collections$CheckedSortedSet", 19);
/*  155:     */     
/*  156: 186 */     Class<?> cCheckedList = map("java.util.Collections$CheckedList", 20);
/*  157:     */     
/*  158: 188 */     Class<?> cCheckedRandomAccessList = map("java.util.Collections$CheckedRandomAccessList", 21);
/*  159:     */     try
/*  160:     */     {
/*  161: 194 */       fSingletonSet_element = cSingletonSet.getDeclaredField("element");
/*  162:     */       
/*  163: 196 */       fSingletonList_element = cSingletonList.getDeclaredField("element");
/*  164:     */       
/*  165: 198 */       fSetFromMap_m = cSetFromMap.getDeclaredField("m");
/*  166: 199 */       fSetFromMap_s = cSetFromMap.getDeclaredField("s");
/*  167:     */       
/*  168: 201 */       fCopiesList_n = cCopiesList.getDeclaredField("n");
/*  169: 202 */       fCopiesList_element = cCopiesList.getDeclaredField("element");
/*  170:     */       
/*  171:     */ 
/*  172: 205 */       fUnmodifiableCollection_c = cUnmodifiableCollection.getDeclaredField("c");
/*  173:     */       
/*  174: 207 */       fUnmodifiableSortedSet_ss = cUnmodifiableSortedSet.getDeclaredField("ss");
/*  175: 208 */       fUnmodifiableList_list = cUnmodifiableList.getDeclaredField("list");
/*  176:     */       
/*  177:     */ 
/*  178: 211 */       fSynchronizedCollection_c = cSynchronizedCollection.getDeclaredField("c");
/*  179:     */       
/*  180: 213 */       fSynchronizedCollection_mutex = cSynchronizedCollection.getDeclaredField("mutex");
/*  181:     */       
/*  182: 215 */       fSynchronizedSortedSet_ss = cSynchronizedSortedSet.getDeclaredField("ss");
/*  183: 216 */       fSynchronizedList_list = cSynchronizedList.getDeclaredField("list");
/*  184:     */       
/*  185: 218 */       fCheckedCollection_c = cCheckedCollection.getDeclaredField("c");
/*  186:     */       
/*  187: 220 */       fCheckedCollection_type = cCheckedCollection.getDeclaredField("type");
/*  188: 221 */       fCheckedSortedSet_ss = cCheckedSortedSet.getDeclaredField("ss");
/*  189: 222 */       fCheckedList_list = cCheckedList.getDeclaredField("list");
/*  190:     */     }
/*  191:     */     catch (Exception e)
/*  192:     */     {
/*  193: 226 */       throw new RuntimeException(e);
/*  194:     */     }
/*  195: 231 */     fSingletonSet_element.setAccessible(true);
/*  196:     */     
/*  197: 233 */     fSingletonList_element.setAccessible(true);
/*  198:     */     
/*  199: 235 */     fSetFromMap_m.setAccessible(true);
/*  200: 236 */     fSetFromMap_s.setAccessible(true);
/*  201:     */     
/*  202: 238 */     fCopiesList_n.setAccessible(true);
/*  203: 239 */     fCopiesList_element.setAccessible(true);
/*  204:     */     
/*  205: 241 */     fUnmodifiableCollection_c.setAccessible(true);
/*  206: 242 */     fUnmodifiableSortedSet_ss.setAccessible(true);
/*  207: 243 */     fUnmodifiableList_list.setAccessible(true);
/*  208:     */     
/*  209: 245 */     fSynchronizedCollection_c.setAccessible(true);
/*  210: 246 */     fSynchronizedCollection_mutex.setAccessible(true);
/*  211: 247 */     fSynchronizedSortedSet_ss.setAccessible(true);
/*  212: 248 */     fSynchronizedList_list.setAccessible(true);
/*  213:     */     
/*  214: 250 */     fCheckedCollection_c.setAccessible(true);
/*  215: 251 */     fCheckedCollection_type.setAccessible(true);
/*  216: 252 */     fCheckedSortedSet_ss.setAccessible(true);
/*  217: 253 */     fCheckedList_list.setAccessible(true);
/*  218:     */     
/*  219:     */ 
/*  220:     */ 
/*  221: 257 */     iSingletonSet = RuntimeEnv.newInstantiator(cSingletonSet);
/*  222:     */     
/*  223: 259 */     iSingletonList = RuntimeEnv.newInstantiator(cSingletonList);
/*  224:     */     
/*  225: 261 */     iSetFromMap = RuntimeEnv.newInstantiator(cSetFromMap);
/*  226:     */     
/*  227: 263 */     iCopiesList = RuntimeEnv.newInstantiator(cCopiesList);
/*  228:     */     
/*  229:     */ 
/*  230: 266 */     iUnmodifiableCollection = RuntimeEnv.newInstantiator(cUnmodifiableCollection);
/*  231: 267 */     iUnmodifiableSet = RuntimeEnv.newInstantiator(cUnmodifiableSet);
/*  232:     */     
/*  233: 269 */     iUnmodifiableSortedSet = RuntimeEnv.newInstantiator(cUnmodifiableSortedSet);
/*  234: 270 */     iUnmodifiableList = RuntimeEnv.newInstantiator(cUnmodifiableList);
/*  235:     */     
/*  236: 272 */     iUnmodifiableRandomAccessList = RuntimeEnv.newInstantiator(cUnmodifiableRandomAccessList);
/*  237:     */     
/*  238:     */ 
/*  239: 275 */     iSynchronizedCollection = RuntimeEnv.newInstantiator(cSynchronizedCollection);
/*  240: 276 */     iSynchronizedSet = RuntimeEnv.newInstantiator(cSynchronizedSet);
/*  241:     */     
/*  242: 278 */     iSynchronizedSortedSet = RuntimeEnv.newInstantiator(cSynchronizedSortedSet);
/*  243: 279 */     iSynchronizedList = RuntimeEnv.newInstantiator(cSynchronizedList);
/*  244:     */     
/*  245: 281 */     iSynchronizedRandomAccessList = RuntimeEnv.newInstantiator(cSynchronizedRandomAccessList);
/*  246:     */     
/*  247: 283 */     iCheckedCollection = RuntimeEnv.newInstantiator(cCheckedCollection);
/*  248: 284 */     iCheckedSet = RuntimeEnv.newInstantiator(cCheckedSet);
/*  249: 285 */     iCheckedSortedSet = RuntimeEnv.newInstantiator(cCheckedSortedSet);
/*  250: 286 */     iCheckedList = RuntimeEnv.newInstantiator(cCheckedList);
/*  251:     */     
/*  252: 288 */     iCheckedRandomAccessList = RuntimeEnv.newInstantiator(cCheckedRandomAccessList);
/*  253:     */   }
/*  254:     */   
/*  255:     */   private static Class<?> map(String className, int id)
/*  256:     */   {
/*  257: 293 */     Class<?> clazz = RuntimeEnv.loadClass(className);
/*  258: 294 */     __nonPublicCollections.put(clazz, Integer.valueOf(id));
/*  259: 295 */     return clazz;
/*  260:     */   }
/*  261:     */   
/*  262:     */   static String name(int number)
/*  263:     */   {
/*  264: 300 */     switch (number)
/*  265:     */     {
/*  266:     */     case 1: 
/*  267: 303 */       return "a";
/*  268:     */     case 2: 
/*  269: 305 */       return "b";
/*  270:     */     case 3: 
/*  271: 307 */       return "c";
/*  272:     */     case 4: 
/*  273: 309 */       return "d";
/*  274:     */     case 5: 
/*  275: 311 */       return "e";
/*  276:     */     case 6: 
/*  277: 313 */       return "f";
/*  278:     */     case 7: 
/*  279: 315 */       return "g";
/*  280:     */     case 8: 
/*  281: 317 */       return "h";
/*  282:     */     case 9: 
/*  283: 319 */       return "i";
/*  284:     */     case 10: 
/*  285: 321 */       return "j";
/*  286:     */     case 11: 
/*  287: 323 */       return "k";
/*  288:     */     case 12: 
/*  289: 325 */       return "l";
/*  290:     */     case 13: 
/*  291: 327 */       return "m";
/*  292:     */     case 14: 
/*  293: 329 */       return "n";
/*  294:     */     case 15: 
/*  295: 331 */       return "o";
/*  296:     */     case 16: 
/*  297: 333 */       return "p";
/*  298:     */     case 17: 
/*  299: 335 */       return "q";
/*  300:     */     case 18: 
/*  301: 337 */       return "r";
/*  302:     */     case 19: 
/*  303: 339 */       return "s";
/*  304:     */     case 20: 
/*  305: 341 */       return "t";
/*  306:     */     case 21: 
/*  307: 343 */       return "u";
/*  308:     */     case 22: 
/*  309: 345 */       return "v";
/*  310:     */     case 24: 
/*  311: 347 */       return "x";
/*  312:     */     case 25: 
/*  313: 349 */       return "y";
/*  314:     */     }
/*  315: 351 */     return null;
/*  316:     */   }
/*  317:     */   
/*  318:     */   static int number(String name)
/*  319:     */   {
/*  320: 357 */     return name.length() != 1 ? 0 : number(name.charAt(0));
/*  321:     */   }
/*  322:     */   
/*  323:     */   static int number(char c)
/*  324:     */   {
/*  325: 362 */     switch (c)
/*  326:     */     {
/*  327:     */     case 'a': 
/*  328: 365 */       return 1;
/*  329:     */     case 'b': 
/*  330: 367 */       return 2;
/*  331:     */     case 'c': 
/*  332: 369 */       return 3;
/*  333:     */     case 'd': 
/*  334: 371 */       return 4;
/*  335:     */     case 'e': 
/*  336: 373 */       return 5;
/*  337:     */     case 'f': 
/*  338: 375 */       return 6;
/*  339:     */     case 'g': 
/*  340: 377 */       return 7;
/*  341:     */     case 'h': 
/*  342: 379 */       return 8;
/*  343:     */     case 'i': 
/*  344: 381 */       return 9;
/*  345:     */     case 'j': 
/*  346: 383 */       return 10;
/*  347:     */     case 'k': 
/*  348: 385 */       return 11;
/*  349:     */     case 'l': 
/*  350: 387 */       return 12;
/*  351:     */     case 'm': 
/*  352: 389 */       return 13;
/*  353:     */     case 'n': 
/*  354: 391 */       return 14;
/*  355:     */     case 'o': 
/*  356: 393 */       return 15;
/*  357:     */     case 'p': 
/*  358: 395 */       return 16;
/*  359:     */     case 'q': 
/*  360: 397 */       return 17;
/*  361:     */     case 'r': 
/*  362: 399 */       return 18;
/*  363:     */     case 's': 
/*  364: 401 */       return 19;
/*  365:     */     case 't': 
/*  366: 403 */       return 20;
/*  367:     */     case 'u': 
/*  368: 405 */       return 21;
/*  369:     */     case 'v': 
/*  370: 407 */       return 22;
/*  371:     */     case 'x': 
/*  372: 409 */       return 24;
/*  373:     */     case 'y': 
/*  374: 411 */       return 25;
/*  375:     */     }
/*  376: 413 */     return 0;
/*  377:     */   }
/*  378:     */   
/*  379: 417 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  380:     */   {
/*  381:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  382:     */       throws IOException
/*  383:     */     {
/*  384: 424 */       PolymorphicCollectionSchema.transferObject(this, pipe, input, output, PolymorphicCollectionSchema.this.strategy);
/*  385:     */     }
/*  386:     */   };
/*  387:     */   
/*  388:     */   public PolymorphicCollectionSchema(IdStrategy strategy)
/*  389:     */   {
/*  390: 430 */     super(strategy);
/*  391:     */   }
/*  392:     */   
/*  393:     */   public Pipe.Schema<Object> getPipeSchema()
/*  394:     */   {
/*  395: 436 */     return this.pipeSchema;
/*  396:     */   }
/*  397:     */   
/*  398:     */   public String getFieldName(int number)
/*  399:     */   {
/*  400: 442 */     return name(number);
/*  401:     */   }
/*  402:     */   
/*  403:     */   public int getFieldNumber(String name)
/*  404:     */   {
/*  405: 448 */     return number(name);
/*  406:     */   }
/*  407:     */   
/*  408:     */   public String messageFullName()
/*  409:     */   {
/*  410: 454 */     return Collection.class.getName();
/*  411:     */   }
/*  412:     */   
/*  413:     */   public String messageName()
/*  414:     */   {
/*  415: 460 */     return Collection.class.getSimpleName();
/*  416:     */   }
/*  417:     */   
/*  418:     */   public void mergeFrom(Input input, Object owner)
/*  419:     */     throws IOException
/*  420:     */   {
/*  421: 466 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  422:     */   }
/*  423:     */   
/*  424:     */   public void writeTo(Output output, Object value)
/*  425:     */     throws IOException
/*  426:     */   {
/*  427: 472 */     writeObjectTo(output, value, this, this.strategy);
/*  428:     */   }
/*  429:     */   
/*  430:     */   static int idFrom(Class<?> clazz)
/*  431:     */   {
/*  432: 477 */     Integer id = (Integer)__nonPublicCollections.get(clazz);
/*  433: 478 */     if (id == null) {
/*  434: 479 */       throw new RuntimeException("Unknown collection: " + clazz);
/*  435:     */     }
/*  436: 481 */     return id.intValue();
/*  437:     */   }
/*  438:     */   
/*  439:     */   static Object instanceFrom(int id)
/*  440:     */   {
/*  441: 486 */     switch (id)
/*  442:     */     {
/*  443:     */     case 1: 
/*  444: 489 */       return Collections.EMPTY_SET;
/*  445:     */     case 2: 
/*  446: 491 */       return Collections.EMPTY_LIST;
/*  447:     */     case 3: 
/*  448: 494 */       return iSingletonSet.newInstance();
/*  449:     */     case 4: 
/*  450: 496 */       return iSingletonList.newInstance();
/*  451:     */     case 5: 
/*  452: 499 */       return iSetFromMap.newInstance();
/*  453:     */     case 6: 
/*  454: 501 */       return iCopiesList.newInstance();
/*  455:     */     case 7: 
/*  456: 504 */       return iUnmodifiableCollection.newInstance();
/*  457:     */     case 8: 
/*  458: 506 */       return iUnmodifiableSet.newInstance();
/*  459:     */     case 9: 
/*  460: 508 */       return iUnmodifiableSortedSet.newInstance();
/*  461:     */     case 10: 
/*  462: 510 */       return iUnmodifiableList.newInstance();
/*  463:     */     case 11: 
/*  464: 512 */       return iUnmodifiableRandomAccessList.newInstance();
/*  465:     */     case 12: 
/*  466: 515 */       return iSynchronizedCollection.newInstance();
/*  467:     */     case 13: 
/*  468: 517 */       return iSynchronizedSet.newInstance();
/*  469:     */     case 14: 
/*  470: 519 */       return iSynchronizedSortedSet.newInstance();
/*  471:     */     case 15: 
/*  472: 521 */       return iSynchronizedList.newInstance();
/*  473:     */     case 16: 
/*  474: 523 */       return iSynchronizedRandomAccessList.newInstance();
/*  475:     */     case 17: 
/*  476: 526 */       return iCheckedCollection.newInstance();
/*  477:     */     case 18: 
/*  478: 528 */       return iCheckedSet.newInstance();
/*  479:     */     case 19: 
/*  480: 530 */       return iCheckedSortedSet.newInstance();
/*  481:     */     case 20: 
/*  482: 532 */       return iCheckedList.newInstance();
/*  483:     */     case 21: 
/*  484: 534 */       return iCheckedRandomAccessList.newInstance();
/*  485:     */     }
/*  486: 537 */     throw new RuntimeException("Unknown id: " + id);
/*  487:     */   }
/*  488:     */   
/*  489:     */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  490:     */     throws IOException
/*  491:     */   {
/*  492: 545 */     if (Collections.class == value.getClass().getDeclaringClass())
/*  493:     */     {
/*  494: 547 */       writeNonPublicCollectionTo(output, value, currentSchema, strategy);
/*  495: 548 */       return;
/*  496:     */     }
/*  497: 551 */     if (EnumSet.class.isAssignableFrom(value.getClass())) {
/*  498: 553 */       strategy.writeEnumIdTo(output, 22, 
/*  499: 554 */         EnumIO.getElementTypeFromEnumSet(value));
/*  500:     */     } else {
/*  501: 560 */       strategy.writeCollectionIdTo(output, 25, value
/*  502: 561 */         .getClass());
/*  503:     */     }
/*  504: 564 */     if ((output instanceof StatefulOutput)) {
/*  505: 567 */       ((StatefulOutput)output).updateLast(strategy.COLLECTION_SCHEMA, currentSchema);
/*  506:     */     }
/*  507: 571 */     strategy.COLLECTION_SCHEMA.writeTo(output, (Collection)value);
/*  508:     */   }
/*  509:     */   
/*  510:     */   static void writeNonPublicCollectionTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  511:     */     throws IOException
/*  512:     */   {
/*  513: 577 */     Integer num = (Integer)__nonPublicCollections.get(value.getClass());
/*  514: 578 */     if (num == null) {
/*  515: 580 */       throw new RuntimeException("Unknown collection: " + value.getClass());
/*  516:     */     }
/*  517: 581 */     int id = num.intValue();
/*  518: 582 */     switch (id)
/*  519:     */     {
/*  520:     */     case 1: 
/*  521: 585 */       output.writeUInt32(id, 0, false);
/*  522: 586 */       break;
/*  523:     */     case 2: 
/*  524: 589 */       output.writeUInt32(id, 0, false);
/*  525: 590 */       break;
/*  526:     */     case 3: 
/*  527: 594 */       output.writeUInt32(id, 0, false);
/*  528:     */       try
/*  529:     */       {
/*  530: 599 */         element = fSingletonSet_element.get(value);
/*  531:     */       }
/*  532:     */       catch (Exception e)
/*  533:     */       {
/*  534:     */         Object element;
/*  535: 603 */         throw new RuntimeException(e);
/*  536:     */       }
/*  537:     */       Object element;
/*  538: 606 */       if (element != null) {
/*  539: 607 */         output.writeObject(1, element, strategy.OBJECT_SCHEMA, false);
/*  540:     */       }
/*  541:     */       break;
/*  542:     */     case 4: 
/*  543: 614 */       output.writeUInt32(id, 0, false);
/*  544:     */       
/*  545:     */ 
/*  546: 617 */       Object element = ((List)value).get(0);
/*  547: 619 */       if (element != null) {
/*  548: 620 */         output.writeObject(1, element, strategy.OBJECT_SCHEMA, false);
/*  549:     */       }
/*  550:     */       break;
/*  551:     */     case 5: 
/*  552:     */       try
/*  553:     */       {
/*  554: 630 */         m = fSetFromMap_m.get(value);
/*  555:     */       }
/*  556:     */       catch (Exception e)
/*  557:     */       {
/*  558:     */         Object m;
/*  559: 634 */         throw new RuntimeException(e);
/*  560:     */       }
/*  561:     */       Object m;
/*  562: 637 */       output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
/*  563:     */       
/*  564: 639 */       break;
/*  565:     */     case 6: 
/*  566: 644 */       output.writeUInt32(id, 0, false);
/*  567:     */       
/*  568: 646 */       int n = ((List)value).size();
/*  569:     */       try
/*  570:     */       {
/*  571: 650 */         element = fCopiesList_element.get(value);
/*  572:     */       }
/*  573:     */       catch (Exception e)
/*  574:     */       {
/*  575:     */         Object element;
/*  576: 654 */         throw new RuntimeException(e);
/*  577:     */       }
/*  578:     */       Object element;
/*  579: 657 */       output.writeUInt32(1, n, false);
/*  580: 659 */       if (element != null) {
/*  581: 660 */         output.writeObject(2, element, strategy.OBJECT_SCHEMA, false);
/*  582:     */       }
/*  583:     */       break;
/*  584:     */     case 7: 
/*  585: 665 */       writeUnmodifiableCollectionTo(output, value, currentSchema, strategy, id);
/*  586:     */       
/*  587: 667 */       break;
/*  588:     */     case 8: 
/*  589: 669 */       writeUnmodifiableCollectionTo(output, value, currentSchema, strategy, id);
/*  590:     */       
/*  591: 671 */       break;
/*  592:     */     case 9: 
/*  593: 673 */       writeUnmodifiableCollectionTo(output, value, currentSchema, strategy, id);
/*  594:     */       
/*  595: 675 */       break;
/*  596:     */     case 10: 
/*  597: 677 */       writeUnmodifiableCollectionTo(output, value, currentSchema, strategy, id);
/*  598:     */       
/*  599: 679 */       break;
/*  600:     */     case 11: 
/*  601: 681 */       writeUnmodifiableCollectionTo(output, value, currentSchema, strategy, id);
/*  602:     */       
/*  603: 683 */       break;
/*  604:     */     case 12: 
/*  605: 686 */       writeSynchronizedCollectionTo(output, value, currentSchema, strategy, id);
/*  606:     */       
/*  607: 688 */       break;
/*  608:     */     case 13: 
/*  609: 690 */       writeSynchronizedCollectionTo(output, value, currentSchema, strategy, id);
/*  610:     */       
/*  611: 692 */       break;
/*  612:     */     case 14: 
/*  613: 694 */       writeSynchronizedCollectionTo(output, value, currentSchema, strategy, id);
/*  614:     */       
/*  615: 696 */       break;
/*  616:     */     case 15: 
/*  617: 698 */       writeSynchronizedCollectionTo(output, value, currentSchema, strategy, id);
/*  618:     */       
/*  619: 700 */       break;
/*  620:     */     case 16: 
/*  621: 702 */       writeSynchronizedCollectionTo(output, value, currentSchema, strategy, id);
/*  622:     */       
/*  623: 704 */       break;
/*  624:     */     case 17: 
/*  625: 707 */       writeCheckedCollectionTo(output, value, currentSchema, strategy, id);
/*  626: 708 */       break;
/*  627:     */     case 18: 
/*  628: 710 */       writeCheckedCollectionTo(output, value, currentSchema, strategy, id);
/*  629: 711 */       break;
/*  630:     */     case 19: 
/*  631: 713 */       writeCheckedCollectionTo(output, value, currentSchema, strategy, id);
/*  632: 714 */       break;
/*  633:     */     case 20: 
/*  634: 716 */       writeCheckedCollectionTo(output, value, currentSchema, strategy, id);
/*  635: 717 */       break;
/*  636:     */     case 21: 
/*  637: 719 */       writeCheckedCollectionTo(output, value, currentSchema, strategy, id);
/*  638: 720 */       break;
/*  639:     */     default: 
/*  640: 723 */       throw new RuntimeException("Should not happen.");
/*  641:     */     }
/*  642:     */   }
/*  643:     */   
/*  644:     */   private static void writeUnmodifiableCollectionTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy, int id)
/*  645:     */     throws IOException
/*  646:     */   {
/*  647:     */     try
/*  648:     */     {
/*  649: 734 */       c = fUnmodifiableCollection_c.get(value);
/*  650:     */     }
/*  651:     */     catch (Exception e)
/*  652:     */     {
/*  653:     */       Object c;
/*  654: 738 */       throw new RuntimeException(e);
/*  655:     */     }
/*  656:     */     Object c;
/*  657: 741 */     output.writeObject(id, c, strategy.POLYMORPHIC_COLLECTION_SCHEMA, false);
/*  658:     */   }
/*  659:     */   
/*  660:     */   private static void writeSynchronizedCollectionTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy, int id)
/*  661:     */     throws IOException
/*  662:     */   {
/*  663:     */     try
/*  664:     */     {
/*  665: 751 */       Object c = fSynchronizedCollection_c.get(value);
/*  666: 752 */       mutex = fSynchronizedCollection_mutex.get(value);
/*  667:     */     }
/*  668:     */     catch (Exception e)
/*  669:     */     {
/*  670:     */       Object mutex;
/*  671: 756 */       throw new RuntimeException(e);
/*  672:     */     }
/*  673:     */     Object mutex;
/*  674:     */     Object c;
/*  675: 759 */     if (mutex != value) {
/*  676: 764 */       throw new RuntimeException("This exception is thrown to fail fast. Synchronized collections with a different mutex would only work if graph format is used, since the reference is retained.");
/*  677:     */     }
/*  678: 770 */     output.writeObject(id, c, strategy.POLYMORPHIC_COLLECTION_SCHEMA, false);
/*  679:     */   }
/*  680:     */   
/*  681:     */   private static void writeCheckedCollectionTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy, int id)
/*  682:     */     throws IOException
/*  683:     */   {
/*  684:     */     try
/*  685:     */     {
/*  686: 780 */       Object c = fCheckedCollection_c.get(value);
/*  687: 781 */       type = fCheckedCollection_type.get(value);
/*  688:     */     }
/*  689:     */     catch (Exception e)
/*  690:     */     {
/*  691:     */       Object type;
/*  692: 785 */       throw new RuntimeException(e);
/*  693:     */     }
/*  694:     */     Object type;
/*  695:     */     Object c;
/*  696: 788 */     output.writeObject(id, c, strategy.POLYMORPHIC_COLLECTION_SCHEMA, false);
/*  697: 789 */     output.writeObject(1, type, strategy.CLASS_SCHEMA, false);
/*  698:     */   }
/*  699:     */   
/*  700:     */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/*  701:     */     throws IOException
/*  702:     */   {
/*  703: 795 */     return readObjectFrom(input, schema, owner, strategy, input
/*  704: 796 */       .readFieldNumber(schema));
/*  705:     */   }
/*  706:     */   
/*  707:     */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, int number)
/*  708:     */     throws IOException
/*  709:     */   {
/*  710: 803 */     boolean graph = input instanceof GraphInput;
/*  711: 804 */     Object ret = null;
/*  712: 805 */     switch (number)
/*  713:     */     {
/*  714:     */     case 1: 
/*  715: 808 */       if (0 != input.readUInt32()) {
/*  716: 809 */         throw new ProtostuffException("Corrupt input.");
/*  717:     */       }
/*  718: 811 */       if (graph) {
/*  719: 814 */         ((GraphInput)input).updateLast(Collections.EMPTY_SET, owner);
/*  720:     */       }
/*  721: 817 */       ret = Collections.EMPTY_SET;
/*  722: 818 */       break;
/*  723:     */     case 2: 
/*  724: 821 */       if (0 != input.readUInt32()) {
/*  725: 822 */         throw new ProtostuffException("Corrupt input.");
/*  726:     */       }
/*  727: 824 */       if (graph) {
/*  728: 827 */         ((GraphInput)input).updateLast(Collections.EMPTY_LIST, owner);
/*  729:     */       }
/*  730: 830 */       ret = Collections.EMPTY_LIST;
/*  731: 831 */       break;
/*  732:     */     case 3: 
/*  733: 835 */       if (0 != input.readUInt32()) {
/*  734: 836 */         throw new ProtostuffException("Corrupt input.");
/*  735:     */       }
/*  736: 838 */       Object collection = iSingletonSet.newInstance();
/*  737: 839 */       if (graph) {
/*  738: 842 */         ((GraphInput)input).updateLast(collection, owner);
/*  739:     */       }
/*  740: 845 */       int next = input.readFieldNumber(schema);
/*  741: 846 */       if (next == 0) {
/*  742: 849 */         return collection;
/*  743:     */       }
/*  744: 852 */       if (next != 1) {
/*  745: 853 */         throw new ProtostuffException("Corrupt input");
/*  746:     */       }
/*  747: 855 */       IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/*  748: 856 */       Object element = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
/*  749: 857 */       if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/*  750: 858 */         element = wrapper.value;
/*  751:     */       }
/*  752:     */       try
/*  753:     */       {
/*  754: 862 */         fSingletonSet_element.set(collection, element);
/*  755:     */       }
/*  756:     */       catch (Exception e)
/*  757:     */       {
/*  758: 866 */         throw new RuntimeException(e);
/*  759:     */       }
/*  760: 869 */       ret = collection;
/*  761: 870 */       break;
/*  762:     */     case 4: 
/*  763: 875 */       if (0 != input.readUInt32()) {
/*  764: 876 */         throw new ProtostuffException("Corrupt input.");
/*  765:     */       }
/*  766: 878 */       Object collection = iSingletonList.newInstance();
/*  767: 879 */       if (graph) {
/*  768: 882 */         ((GraphInput)input).updateLast(collection, owner);
/*  769:     */       }
/*  770: 885 */       int next = input.readFieldNumber(schema);
/*  771: 886 */       if (next == 0) {
/*  772: 889 */         return collection;
/*  773:     */       }
/*  774: 892 */       if (next != 1) {
/*  775: 893 */         throw new ProtostuffException("Corrupt input.");
/*  776:     */       }
/*  777: 895 */       IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/*  778: 896 */       Object element = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
/*  779: 897 */       if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/*  780: 898 */         element = wrapper.value;
/*  781:     */       }
/*  782:     */       try
/*  783:     */       {
/*  784: 902 */         fSingletonList_element.set(collection, element);
/*  785:     */       }
/*  786:     */       catch (Exception e)
/*  787:     */       {
/*  788: 906 */         throw new RuntimeException(e);
/*  789:     */       }
/*  790: 909 */       ret = collection;
/*  791: 910 */       break;
/*  792:     */     case 5: 
/*  793: 915 */       Object collection = iSetFromMap.newInstance();
/*  794: 916 */       if (graph) {
/*  795: 919 */         ((GraphInput)input).updateLast(collection, owner);
/*  796:     */       }
/*  797: 922 */       IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/*  798: 923 */       Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
/*  799: 925 */       if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/*  800: 926 */         m = wrapper.value;
/*  801:     */       }
/*  802:     */       try
/*  803:     */       {
/*  804: 930 */         fSetFromMap_m.set(collection, m);
/*  805: 931 */         fSetFromMap_s.set(collection, ((Map)m).keySet());
/*  806:     */       }
/*  807:     */       catch (Exception e)
/*  808:     */       {
/*  809: 935 */         throw new RuntimeException(e);
/*  810:     */       }
/*  811: 938 */       ret = collection;
/*  812: 939 */       break;
/*  813:     */     case 6: 
/*  814: 944 */       if (0 != input.readUInt32()) {
/*  815: 945 */         throw new ProtostuffException("Corrupt input.");
/*  816:     */       }
/*  817: 947 */       Object collection = iCopiesList.newInstance();
/*  818: 948 */       if (graph) {
/*  819: 951 */         ((GraphInput)input).updateLast(collection, owner);
/*  820:     */       }
/*  821: 954 */       if (1 != input.readFieldNumber(schema)) {
/*  822: 955 */         throw new ProtostuffException("Corrupt input.");
/*  823:     */       }
/*  824: 957 */       int n = input.readUInt32();
/*  825: 958 */       int next = input.readFieldNumber(schema);
/*  826: 960 */       if (next == 0)
/*  827:     */       {
/*  828:     */         try
/*  829:     */         {
/*  830: 965 */           fCopiesList_n.setInt(collection, n);
/*  831:     */         }
/*  832:     */         catch (Exception e)
/*  833:     */         {
/*  834: 969 */           throw new RuntimeException(e);
/*  835:     */         }
/*  836: 972 */         return collection;
/*  837:     */       }
/*  838: 975 */       if (next != 2) {
/*  839: 976 */         throw new ProtostuffException("Corrupt input.");
/*  840:     */       }
/*  841: 978 */       IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/*  842: 979 */       Object element = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
/*  843: 980 */       if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/*  844: 981 */         element = wrapper.value;
/*  845:     */       }
/*  846:     */       try
/*  847:     */       {
/*  848: 985 */         fCopiesList_n.setInt(collection, n);
/*  849: 986 */         fCopiesList_element.set(collection, element);
/*  850:     */       }
/*  851:     */       catch (Exception e)
/*  852:     */       {
/*  853: 990 */         throw new RuntimeException(e);
/*  854:     */       }
/*  855: 993 */       ret = collection;
/*  856: 994 */       break;
/*  857:     */     case 7: 
/*  858: 998 */       ret = readUnmodifiableCollectionFrom(input, schema, owner, strategy, graph, iUnmodifiableCollection
/*  859: 999 */         .newInstance(), false, false);
/*  860:     */       
/*  861:1001 */       break;
/*  862:     */     case 8: 
/*  863:1003 */       ret = readUnmodifiableCollectionFrom(input, schema, owner, strategy, graph, iUnmodifiableSet
/*  864:1004 */         .newInstance(), false, false);
/*  865:     */       
/*  866:1006 */       break;
/*  867:     */     case 9: 
/*  868:1008 */       ret = readUnmodifiableCollectionFrom(input, schema, owner, strategy, graph, iUnmodifiableSortedSet
/*  869:1009 */         .newInstance(), true, false);
/*  870:     */       
/*  871:1011 */       break;
/*  872:     */     case 10: 
/*  873:1013 */       ret = readUnmodifiableCollectionFrom(input, schema, owner, strategy, graph, iUnmodifiableList
/*  874:1014 */         .newInstance(), false, true);
/*  875:     */       
/*  876:1016 */       break;
/*  877:     */     case 11: 
/*  878:1018 */       ret = readUnmodifiableCollectionFrom(input, schema, owner, strategy, graph, iUnmodifiableRandomAccessList
/*  879:     */       
/*  880:1020 */         .newInstance(), false, true);
/*  881:1021 */       break;
/*  882:     */     case 12: 
/*  883:1024 */       ret = readSynchronizedCollectionFrom(input, schema, owner, strategy, graph, iSynchronizedCollection
/*  884:1025 */         .newInstance(), false, false);
/*  885:     */       
/*  886:1027 */       break;
/*  887:     */     case 13: 
/*  888:1029 */       ret = readSynchronizedCollectionFrom(input, schema, owner, strategy, graph, iSynchronizedSet
/*  889:1030 */         .newInstance(), false, false);
/*  890:     */       
/*  891:1032 */       break;
/*  892:     */     case 14: 
/*  893:1034 */       ret = readSynchronizedCollectionFrom(input, schema, owner, strategy, graph, iSynchronizedSortedSet
/*  894:1035 */         .newInstance(), true, false);
/*  895:     */       
/*  896:1037 */       break;
/*  897:     */     case 15: 
/*  898:1039 */       ret = readSynchronizedCollectionFrom(input, schema, owner, strategy, graph, iSynchronizedList
/*  899:1040 */         .newInstance(), false, true);
/*  900:     */       
/*  901:1042 */       break;
/*  902:     */     case 16: 
/*  903:1044 */       ret = readSynchronizedCollectionFrom(input, schema, owner, strategy, graph, iSynchronizedRandomAccessList
/*  904:     */       
/*  905:1046 */         .newInstance(), false, true);
/*  906:1047 */       break;
/*  907:     */     case 17: 
/*  908:1050 */       ret = readCheckedCollectionFrom(input, schema, owner, strategy, graph, iCheckedCollection
/*  909:1051 */         .newInstance(), false, false);
/*  910:1052 */       break;
/*  911:     */     case 18: 
/*  912:1054 */       ret = readCheckedCollectionFrom(input, schema, owner, strategy, graph, iCheckedSet
/*  913:1055 */         .newInstance(), false, false);
/*  914:1056 */       break;
/*  915:     */     case 19: 
/*  916:1058 */       ret = readCheckedCollectionFrom(input, schema, owner, strategy, graph, iCheckedSortedSet
/*  917:1059 */         .newInstance(), true, false);
/*  918:1060 */       break;
/*  919:     */     case 20: 
/*  920:1062 */       ret = readCheckedCollectionFrom(input, schema, owner, strategy, graph, iCheckedList
/*  921:1063 */         .newInstance(), false, true);
/*  922:1064 */       break;
/*  923:     */     case 21: 
/*  924:1066 */       ret = readCheckedCollectionFrom(input, schema, owner, strategy, graph, iCheckedRandomAccessList
/*  925:1067 */         .newInstance(), false, true);
/*  926:1068 */       break;
/*  927:     */     case 22: 
/*  928:1073 */       Collection<?> es = strategy.resolveEnumFrom(input).newEnumSet();
/*  929:1075 */       if (graph) {
/*  930:1078 */         ((GraphInput)input).updateLast(es, owner);
/*  931:     */       }
/*  932:1083 */       strategy.COLLECTION_SCHEMA.mergeFrom(input, es);
/*  933:1084 */       return es;
/*  934:     */     case 25: 
/*  935:1090 */       Collection<Object> collection = strategy.resolveCollectionFrom(input).newMessage();
/*  936:1092 */       if (graph) {
/*  937:1095 */         ((GraphInput)input).updateLast(collection, owner);
/*  938:     */       }
/*  939:1098 */       strategy.COLLECTION_SCHEMA.mergeFrom(input, collection);
/*  940:     */       
/*  941:1100 */       return collection;
/*  942:     */     case 23: 
/*  943:     */     case 24: 
/*  944:     */     default: 
/*  945:1104 */       throw new ProtostuffException("Corrupt input.");
/*  946:     */     }
/*  947:1107 */     if (0 != input.readFieldNumber(schema)) {
/*  948:1108 */       throw new ProtostuffException("Corrupt input.");
/*  949:     */     }
/*  950:1110 */     return ret;
/*  951:     */   }
/*  952:     */   
/*  953:     */   private static Object readUnmodifiableCollectionFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object collection, boolean ss, boolean list)
/*  954:     */     throws IOException
/*  955:     */   {
/*  956:1117 */     if (graph) {
/*  957:1120 */       ((GraphInput)input).updateLast(collection, owner);
/*  958:     */     }
/*  959:1123 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/*  960:1124 */     Object c = input.mergeObject(wrapper, strategy.POLYMORPHIC_COLLECTION_SCHEMA);
/*  961:1126 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/*  962:1127 */       c = wrapper.value;
/*  963:     */     }
/*  964:     */     try
/*  965:     */     {
/*  966:1130 */       fUnmodifiableCollection_c.set(collection, c);
/*  967:1132 */       if (ss) {
/*  968:1133 */         fUnmodifiableSortedSet_ss.set(collection, c);
/*  969:     */       }
/*  970:1135 */       if (list) {
/*  971:1136 */         fUnmodifiableList_list.set(collection, c);
/*  972:     */       }
/*  973:     */     }
/*  974:     */     catch (Exception e)
/*  975:     */     {
/*  976:1140 */       throw new RuntimeException(e);
/*  977:     */     }
/*  978:1143 */     return collection;
/*  979:     */   }
/*  980:     */   
/*  981:     */   private static Object readSynchronizedCollectionFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object collection, boolean ss, boolean list)
/*  982:     */     throws IOException
/*  983:     */   {
/*  984:1150 */     if (graph) {
/*  985:1153 */       ((GraphInput)input).updateLast(collection, owner);
/*  986:     */     }
/*  987:1156 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/*  988:1157 */     Object c = input.mergeObject(wrapper, strategy.POLYMORPHIC_COLLECTION_SCHEMA);
/*  989:1159 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/*  990:1160 */       c = wrapper.value;
/*  991:     */     }
/*  992:     */     try
/*  993:     */     {
/*  994:1163 */       fSynchronizedCollection_c.set(collection, c);
/*  995:     */       
/*  996:1165 */       fSynchronizedCollection_mutex.set(collection, collection);
/*  997:1167 */       if (ss) {
/*  998:1168 */         fSynchronizedSortedSet_ss.set(collection, c);
/*  999:     */       }
/* 1000:1170 */       if (list) {
/* 1001:1171 */         fSynchronizedList_list.set(collection, c);
/* 1002:     */       }
/* 1003:     */     }
/* 1004:     */     catch (Exception e)
/* 1005:     */     {
/* 1006:1175 */       throw new RuntimeException(e);
/* 1007:     */     }
/* 1008:1178 */     return collection;
/* 1009:     */   }
/* 1010:     */   
/* 1011:     */   private static Object readCheckedCollectionFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy, boolean graph, Object collection, boolean ss, boolean list)
/* 1012:     */     throws IOException
/* 1013:     */   {
/* 1014:1185 */     if (graph) {
/* 1015:1188 */       ((GraphInput)input).updateLast(collection, owner);
/* 1016:     */     }
/* 1017:1191 */     IdStrategy.Wrapper wrapper = new IdStrategy.Wrapper();
/* 1018:1192 */     Object c = input.mergeObject(wrapper, strategy.POLYMORPHIC_COLLECTION_SCHEMA);
/* 1019:1194 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 1020:1195 */       c = wrapper.value;
/* 1021:     */     }
/* 1022:1197 */     if (1 != input.readFieldNumber(schema)) {
/* 1023:1198 */       throw new ProtostuffException("Corrupt input.");
/* 1024:     */     }
/* 1025:1200 */     Object type = input.mergeObject(wrapper, strategy.CLASS_SCHEMA);
/* 1026:1201 */     if ((!graph) || (!((GraphInput)input).isCurrentMessageReference())) {
/* 1027:1202 */       type = wrapper.value;
/* 1028:     */     }
/* 1029:     */     try
/* 1030:     */     {
/* 1031:1205 */       fCheckedCollection_c.set(collection, c);
/* 1032:1206 */       fCheckedCollection_type.set(collection, type);
/* 1033:1208 */       if (ss) {
/* 1034:1209 */         fCheckedSortedSet_ss.set(collection, c);
/* 1035:     */       }
/* 1036:1211 */       if (list) {
/* 1037:1212 */         fCheckedList_list.set(collection, c);
/* 1038:     */       }
/* 1039:     */     }
/* 1040:     */     catch (Exception e)
/* 1041:     */     {
/* 1042:1216 */       throw new RuntimeException(e);
/* 1043:     */     }
/* 1044:1219 */     return collection;
/* 1045:     */   }
/* 1046:     */   
/* 1047:     */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/* 1048:     */     throws IOException
/* 1049:     */   {
/* 1050:1225 */     transferObject(pipeSchema, pipe, input, output, strategy, input
/* 1051:1226 */       .readFieldNumber(pipeSchema.wrappedSchema));
/* 1052:     */   }
/* 1053:     */   
/* 1054:     */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, int number)
/* 1055:     */     throws IOException
/* 1056:     */   {
/* 1057:1233 */     switch (number)
/* 1058:     */     {
/* 1059:     */     case 1: 
/* 1060:1236 */       output.writeUInt32(number, input.readUInt32(), false);
/* 1061:1237 */       break;
/* 1062:     */     case 2: 
/* 1063:1240 */       output.writeUInt32(number, input.readUInt32(), false);
/* 1064:1241 */       break;
/* 1065:     */     case 3: 
/* 1066:     */     case 4: 
/* 1067:1246 */       output.writeUInt32(number, input.readUInt32(), false);
/* 1068:     */       
/* 1069:1248 */       int next = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 1070:1249 */       if (next == 0) {
/* 1071:1252 */         return;
/* 1072:     */       }
/* 1073:1255 */       if (next != 1) {
/* 1074:1256 */         throw new ProtostuffException("Corrupt input.");
/* 1075:     */       }
/* 1076:1258 */       output.writeObject(1, pipe, strategy.OBJECT_PIPE_SCHEMA, false);
/* 1077:1259 */       break;
/* 1078:     */     case 5: 
/* 1079:1262 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, false);
/* 1080:     */       
/* 1081:1264 */       break;
/* 1082:     */     case 6: 
/* 1083:1268 */       output.writeUInt32(number, input.readUInt32(), false);
/* 1084:1270 */       if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 1085:1271 */         throw new ProtostuffException("Corrupt input.");
/* 1086:     */       }
/* 1087:1274 */       output.writeUInt32(1, input.readUInt32(), false);
/* 1088:     */       
/* 1089:1276 */       int next = input.readFieldNumber(pipeSchema.wrappedSchema);
/* 1090:1277 */       if (next == 0) {
/* 1091:1280 */         return;
/* 1092:     */       }
/* 1093:1283 */       if (next != 2) {
/* 1094:1284 */         throw new ProtostuffException("Corrupt input.");
/* 1095:     */       }
/* 1096:1286 */       output.writeObject(2, pipe, strategy.OBJECT_PIPE_SCHEMA, false);
/* 1097:1287 */       break;
/* 1098:     */     case 7: 
/* 1099:     */     case 8: 
/* 1100:     */     case 9: 
/* 1101:     */     case 10: 
/* 1102:     */     case 11: 
/* 1103:1294 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, false);
/* 1104:     */       
/* 1105:1296 */       break;
/* 1106:     */     case 12: 
/* 1107:     */     case 13: 
/* 1108:     */     case 14: 
/* 1109:     */     case 15: 
/* 1110:     */     case 16: 
/* 1111:1303 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, false);
/* 1112:     */       
/* 1113:1305 */       break;
/* 1114:     */     case 17: 
/* 1115:     */     case 18: 
/* 1116:     */     case 19: 
/* 1117:     */     case 20: 
/* 1118:     */     case 21: 
/* 1119:1312 */       output.writeObject(number, pipe, strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, false);
/* 1120:1315 */       if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 1121:1316 */         throw new ProtostuffException("Corrupt input.");
/* 1122:     */       }
/* 1123:1318 */       output.writeObject(1, pipe, strategy.CLASS_PIPE_SCHEMA, false);
/* 1124:1319 */       break;
/* 1125:     */     case 22: 
/* 1126:1322 */       strategy.transferEnumId(input, output, number);
/* 1127:1324 */       if ((output instanceof StatefulOutput)) {
/* 1128:1327 */         ((StatefulOutput)output).updateLast(strategy.COLLECTION_PIPE_SCHEMA, pipeSchema);
/* 1129:     */       }
/* 1130:1332 */       Pipe.transferDirect(strategy.COLLECTION_PIPE_SCHEMA, pipe, input, output);
/* 1131:     */       
/* 1132:1334 */       return;
/* 1133:     */     case 25: 
/* 1134:1336 */       strategy.transferCollectionId(input, output, number);
/* 1135:1338 */       if ((output instanceof StatefulOutput)) {
/* 1136:1341 */         ((StatefulOutput)output).updateLast(strategy.COLLECTION_PIPE_SCHEMA, pipeSchema);
/* 1137:     */       }
/* 1138:1345 */       Pipe.transferDirect(strategy.COLLECTION_PIPE_SCHEMA, pipe, input, output);
/* 1139:     */       
/* 1140:1347 */       return;
/* 1141:     */     case 23: 
/* 1142:     */     case 24: 
/* 1143:     */     default: 
/* 1144:1349 */       throw new ProtostuffException("Corrupt input.");
/* 1145:     */     }
/* 1146:1352 */     if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/* 1147:1353 */       throw new ProtostuffException("Corrupt input.");
/* 1148:     */     }
/* 1149:     */   }
/* 1150:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicCollectionSchema
 * JD-Core Version:    0.7.0.1
 */