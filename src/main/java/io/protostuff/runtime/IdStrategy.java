/*    1:     */ package io.protostuff.runtime;
/*    2:     */ 
/*    3:     */ import io.protostuff.CollectionSchema.MessageFactory;
/*    4:     */ import io.protostuff.GraphInput;
/*    5:     */ import io.protostuff.Input;
/*    6:     */ import io.protostuff.MapSchema.MapWrapper;
/*    7:     */ import io.protostuff.MapSchema.MessageFactory;
/*    8:     */ import io.protostuff.Message;
/*    9:     */ import io.protostuff.Output;
/*   10:     */ import io.protostuff.Pipe;
/*   11:     */ import io.protostuff.Pipe.Schema;
/*   12:     */ import io.protostuff.ProtostuffException;
/*   13:     */ import io.protostuff.Schema;
/*   14:     */ import java.io.IOException;
/*   15:     */ import java.lang.reflect.Array;
/*   16:     */ import java.lang.reflect.Constructor;
/*   17:     */ import java.util.ArrayList;
/*   18:     */ import java.util.Collection;
/*   19:     */ import java.util.Map;
/*   20:     */ import java.util.Map.Entry;
/*   21:     */ 
/*   22:     */ public abstract class IdStrategy
/*   23:     */ {
/*   24:     */   public static final int ENUMS_BY_NAME = 1;
/*   25:     */   public static final int AUTO_LOAD_POLYMORPHIC_CLASSES = 2;
/*   26:     */   public static final int ALLOW_NULL_ARRAY_ELEMENT = 4;
/*   27:     */   public static final int MORPH_NON_FINAL_POJOS = 8;
/*   28:     */   public static final int MORPH_COLLECTION_INTERFACES = 16;
/*   29:     */   public static final int MORPH_MAP_INTERFACES = 32;
/*   30:     */   public static final int COLLECTION_SCHEMA_ON_REPEATED_FIELDS = 64;
/*   31:     */   public static final int POJO_SCHEMA_ON_COLLECTION_FIELDS = 128;
/*   32:     */   public static final int POJO_SCHEMA_ON_MAP_FIELDS = 256;
/*   33:     */   public static final int DEFAULT_FLAGS;
/*   34:     */   public final int flags;
/*   35:     */   public final IdStrategy primaryGroup;
/*   36:     */   public final int groupId;
/*   37:     */   
/*   38:     */   static
/*   39:     */   {
/*   40:  52 */     int flags = 0;
/*   41:  54 */     if (RuntimeEnv.ENUMS_BY_NAME) {
/*   42:  55 */       flags |= 0x1;
/*   43:     */     }
/*   44:  57 */     if (RuntimeEnv.AUTO_LOAD_POLYMORPHIC_CLASSES) {
/*   45:  58 */       flags |= 0x2;
/*   46:     */     }
/*   47:  60 */     if (RuntimeEnv.ALLOW_NULL_ARRAY_ELEMENT) {
/*   48:  61 */       flags |= 0x4;
/*   49:     */     }
/*   50:  63 */     if (RuntimeEnv.MORPH_NON_FINAL_POJOS) {
/*   51:  64 */       flags |= 0x8;
/*   52:     */     }
/*   53:  66 */     if (RuntimeEnv.MORPH_COLLECTION_INTERFACES) {
/*   54:  67 */       flags |= 0x10;
/*   55:     */     }
/*   56:  69 */     if (RuntimeEnv.MORPH_MAP_INTERFACES) {
/*   57:  70 */       flags |= 0x20;
/*   58:     */     }
/*   59:  72 */     if (RuntimeEnv.COLLECTION_SCHEMA_ON_REPEATED_FIELDS) {
/*   60:  73 */       flags |= 0x40;
/*   61:     */     }
/*   62:  75 */     if (RuntimeEnv.POJO_SCHEMA_ON_COLLECTION_FIELDS) {
/*   63:  76 */       flags |= 0x80;
/*   64:     */     }
/*   65:  78 */     if (RuntimeEnv.POJO_SCHEMA_ON_MAP_FIELDS) {
/*   66:  79 */       flags |= 0x100;
/*   67:     */     }
/*   68:  81 */     DEFAULT_FLAGS = flags;
/*   69:     */   }
/*   70:     */   
/*   71:     */   protected IdStrategy(int flags, IdStrategy primaryGroup, int groupId)
/*   72:     */   {
/*   73:  90 */     if (primaryGroup != null)
/*   74:     */     {
/*   75:  92 */       if ((groupId <= 0) || (0 != (groupId & groupId - 1))) {
/*   76:  94 */         throw new RuntimeException("The groupId must be a power of two (1,2,4,8,etc).");
/*   77:     */       }
/*   78:     */     }
/*   79:  98 */     else if (groupId != 0) {
/*   80: 100 */       throw new RuntimeException("An IdStrategy without a primaryGroup (standalone) must have a groupId of zero.");
/*   81:     */     }
/*   82: 104 */     this.flags = flags;
/*   83: 105 */     this.primaryGroup = primaryGroup;
/*   84: 106 */     this.groupId = groupId;
/*   85:     */   }
/*   86:     */   
/*   87:     */   protected <T> Schema<T> newSchema(Class<T> typeClass)
/*   88:     */   {
/*   89: 115 */     if (this.primaryGroup == null) {
/*   90: 116 */       return RuntimeSchema.createFrom(typeClass, this);
/*   91:     */     }
/*   92: 118 */     Schema<T> s = this.primaryGroup.getSchemaWrapper(typeClass, true).getSchema();
/*   93: 121 */     if (!(s instanceof RuntimeSchema)) {
/*   94: 122 */       return s;
/*   95:     */     }
/*   96: 124 */     RuntimeSchema<T> rs = (RuntimeSchema)s;
/*   97: 127 */     if (rs.getFieldCount() == 0) {
/*   98: 128 */       return rs;
/*   99:     */     }
/*  100: 130 */     ArrayList<Field<T>> fields = new ArrayList(rs.getFieldCount());
/*  101: 132 */     for (Field<T> f : rs.getFields())
/*  102:     */     {
/*  103: 134 */       int groupFilter = f.groupFilter;
/*  104: 135 */       if (groupFilter != 0)
/*  105:     */       {
/*  106:     */         int set;
/*  107:     */         int set;
/*  108: 138 */         if (groupFilter > 0) {
/*  109: 141 */           set = (groupFilter ^ 0xFFFFFFFF) & 0x7FFFFFFF;
/*  110:     */         } else {
/*  111: 146 */           set = -groupFilter;
/*  112:     */         }
/*  113: 149 */         if (0 != (this.groupId & set)) {}
/*  114:     */       }
/*  115:     */       else
/*  116:     */       {
/*  117: 156 */         fields.add(f);
/*  118:     */       }
/*  119:     */     }
/*  120: 167 */     return fields.size() == rs.getFieldCount() ? rs : new RuntimeSchema(typeClass, fields, rs.instantiator);
/*  121:     */   }
/*  122:     */   
/*  123:     */   public static class UnknownTypeException
/*  124:     */     extends RuntimeException
/*  125:     */   {
/*  126:     */     private static final long serialVersionUID = 1L;
/*  127:     */     
/*  128:     */     public UnknownTypeException(String msg)
/*  129:     */     {
/*  130: 180 */       super();
/*  131:     */     }
/*  132:     */   }
/*  133:     */   
/*  134: 336 */   final DerivativeSchema POLYMORPHIC_POJO_ELEMENT_SCHEMA = new DerivativeSchema(this)
/*  135:     */   {
/*  136:     */     protected void doMergeFrom(Input input, Schema<Object> derivedSchema, Object owner)
/*  137:     */       throws IOException
/*  138:     */     {
/*  139: 344 */       Object value = derivedSchema.newMessage();
/*  140: 346 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  141: 347 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  142:     */       } else {
/*  143: 349 */         ((Collection)owner).add(value);
/*  144:     */       }
/*  145: 351 */       if ((input instanceof GraphInput)) {
/*  146: 354 */         ((GraphInput)input).updateLast(value, owner);
/*  147:     */       }
/*  148: 357 */       derivedSchema.mergeFrom(input, value);
/*  149:     */     }
/*  150:     */   };
/*  151: 363 */   final ArraySchema ARRAY_ELEMENT_SCHEMA = new ArraySchema(this)
/*  152:     */   {
/*  153:     */     protected void setValue(Object value, Object owner)
/*  154:     */     {
/*  155: 369 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  156: 370 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  157:     */       } else {
/*  158: 372 */         ((Collection)owner).add(value);
/*  159:     */       }
/*  160:     */     }
/*  161:     */   };
/*  162: 376 */   final NumberSchema NUMBER_ELEMENT_SCHEMA = new NumberSchema(this)
/*  163:     */   {
/*  164:     */     protected void setValue(Object value, Object owner)
/*  165:     */     {
/*  166: 382 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  167: 383 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  168:     */       } else {
/*  169: 385 */         ((Collection)owner).add(value);
/*  170:     */       }
/*  171:     */     }
/*  172:     */   };
/*  173: 389 */   final ClassSchema CLASS_ELEMENT_SCHEMA = new ClassSchema(this)
/*  174:     */   {
/*  175:     */     protected void setValue(Object value, Object owner)
/*  176:     */     {
/*  177: 395 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  178: 396 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  179:     */       } else {
/*  180: 398 */         ((Collection)owner).add(value);
/*  181:     */       }
/*  182:     */     }
/*  183:     */   };
/*  184: 402 */   final PolymorphicEnumSchema POLYMORPHIC_ENUM_ELEMENT_SCHEMA = new PolymorphicEnumSchema(this)
/*  185:     */   {
/*  186:     */     protected void setValue(Object value, Object owner)
/*  187:     */     {
/*  188: 409 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  189: 410 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  190:     */       } else {
/*  191: 412 */         ((Collection)owner).add(value);
/*  192:     */       }
/*  193:     */     }
/*  194:     */   };
/*  195: 416 */   final PolymorphicThrowableSchema POLYMORPHIC_THROWABLE_ELEMENT_SCHEMA = new PolymorphicThrowableSchema(this)
/*  196:     */   {
/*  197:     */     protected void setValue(Object value, Object owner)
/*  198:     */     {
/*  199: 423 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  200: 424 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  201:     */       } else {
/*  202: 426 */         ((Collection)owner).add(value);
/*  203:     */       }
/*  204:     */     }
/*  205:     */   };
/*  206: 430 */   final ObjectSchema OBJECT_ELEMENT_SCHEMA = new ObjectSchema(this)
/*  207:     */   {
/*  208:     */     protected void setValue(Object value, Object owner)
/*  209:     */     {
/*  210: 436 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  211: 437 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  212:     */       } else {
/*  213: 439 */         ((Collection)owner).add(value);
/*  214:     */       }
/*  215:     */     }
/*  216:     */   };
/*  217: 445 */   final Schema<Object> DYNAMIC_VALUE_SCHEMA = new Schema()
/*  218:     */   {
/*  219:     */     public String getFieldName(int number)
/*  220:     */     {
/*  221: 450 */       return ObjectSchema.name(number);
/*  222:     */     }
/*  223:     */     
/*  224:     */     public int getFieldNumber(String name)
/*  225:     */     {
/*  226: 456 */       return ObjectSchema.number(name);
/*  227:     */     }
/*  228:     */     
/*  229:     */     public boolean isInitialized(Object owner)
/*  230:     */     {
/*  231: 462 */       return true;
/*  232:     */     }
/*  233:     */     
/*  234:     */     public String messageFullName()
/*  235:     */     {
/*  236: 468 */       return Object.class.getName();
/*  237:     */     }
/*  238:     */     
/*  239:     */     public String messageName()
/*  240:     */     {
/*  241: 474 */       return Object.class.getSimpleName();
/*  242:     */     }
/*  243:     */     
/*  244:     */     public Object newMessage()
/*  245:     */     {
/*  246: 481 */       throw new UnsupportedOperationException();
/*  247:     */     }
/*  248:     */     
/*  249:     */     public Class<? super Object> typeClass()
/*  250:     */     {
/*  251: 487 */       return Object.class;
/*  252:     */     }
/*  253:     */     
/*  254:     */     public void mergeFrom(Input input, Object owner)
/*  255:     */       throws IOException
/*  256:     */     {
/*  257: 494 */       if (IdStrategy.PMapWrapper.class == owner.getClass()) {
/*  258: 497 */         ((IdStrategy.PMapWrapper)owner).setValue(ObjectSchema.readObjectFrom(input, this, owner, IdStrategy.this));
/*  259:     */       } else {
/*  260: 503 */         ((Collection)owner).add(ObjectSchema.readObjectFrom(input, this, owner, IdStrategy.this));
/*  261:     */       }
/*  262:     */     }
/*  263:     */     
/*  264:     */     public void writeTo(Output output, Object message)
/*  265:     */       throws IOException
/*  266:     */     {
/*  267: 511 */       ObjectSchema.writeObjectTo(output, message, this, IdStrategy.this);
/*  268:     */     }
/*  269:     */   };
/*  270: 515 */   final Pipe.Schema<Object> DYNAMIC_VALUE_PIPE_SCHEMA = new Pipe.Schema(this.DYNAMIC_VALUE_SCHEMA)
/*  271:     */   {
/*  272:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  273:     */       throws IOException
/*  274:     */     {
/*  275: 522 */       ObjectSchema.transferObject(this, pipe, input, output, IdStrategy.this);
/*  276:     */     }
/*  277:     */   };
/*  278: 527 */   final Schema<Collection<Object>> COLLECTION_SCHEMA = new Schema()
/*  279:     */   {
/*  280:     */     public String getFieldName(int number)
/*  281:     */     {
/*  282: 532 */       return number == 1 ? "v" : null;
/*  283:     */     }
/*  284:     */     
/*  285:     */     public int getFieldNumber(String name)
/*  286:     */     {
/*  287: 538 */       return (name.length() == 1) && (name.charAt(0) == 'v') ? 1 : 0;
/*  288:     */     }
/*  289:     */     
/*  290:     */     public boolean isInitialized(Collection<Object> owner)
/*  291:     */     {
/*  292: 544 */       return true;
/*  293:     */     }
/*  294:     */     
/*  295:     */     public String messageFullName()
/*  296:     */     {
/*  297: 550 */       return Collection.class.getName();
/*  298:     */     }
/*  299:     */     
/*  300:     */     public String messageName()
/*  301:     */     {
/*  302: 556 */       return Collection.class.getSimpleName();
/*  303:     */     }
/*  304:     */     
/*  305:     */     public Collection<Object> newMessage()
/*  306:     */     {
/*  307: 562 */       throw new UnsupportedOperationException();
/*  308:     */     }
/*  309:     */     
/*  310:     */     public Class<? super Collection<Object>> typeClass()
/*  311:     */     {
/*  312: 568 */       return Collection.class;
/*  313:     */     }
/*  314:     */     
/*  315:     */     public void mergeFrom(Input input, Collection<Object> message)
/*  316:     */       throws IOException
/*  317:     */     {
/*  318: 575 */       int number = input.readFieldNumber(this);
/*  319: 576 */       for (;; number = input.readFieldNumber(this)) {
/*  320: 578 */         switch (number)
/*  321:     */         {
/*  322:     */         case 0: 
/*  323: 581 */           return;
/*  324:     */         case 1: 
/*  325: 583 */           Object value = input.mergeObject(message, IdStrategy.this.DYNAMIC_VALUE_SCHEMA);
/*  326: 585 */           if (((input instanceof GraphInput)) && 
/*  327: 586 */             (((GraphInput)input).isCurrentMessageReference())) {
/*  328: 589 */             message.add(value);
/*  329:     */           }
/*  330:     */           break;
/*  331:     */         default: 
/*  332: 593 */           throw new ProtostuffException("Corrupt input.");
/*  333:     */         }
/*  334:     */       }
/*  335:     */     }
/*  336:     */     
/*  337:     */     public void writeTo(Output output, Collection<Object> message)
/*  338:     */       throws IOException
/*  339:     */     {
/*  340: 602 */       for (Object value : message) {
/*  341: 604 */         if (value != null) {
/*  342: 605 */           output.writeObject(1, value, IdStrategy.this.DYNAMIC_VALUE_SCHEMA, true);
/*  343:     */         }
/*  344:     */       }
/*  345:     */     }
/*  346:     */   };
/*  347: 610 */   final Pipe.Schema<Collection<Object>> COLLECTION_PIPE_SCHEMA = new Pipe.Schema(this.COLLECTION_SCHEMA)
/*  348:     */   {
/*  349:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  350:     */       throws IOException
/*  351:     */     {
/*  352: 617 */       int number = input.readFieldNumber(this.wrappedSchema);
/*  353: 618 */       for (;; number = input.readFieldNumber(this.wrappedSchema)) {
/*  354: 620 */         switch (number)
/*  355:     */         {
/*  356:     */         case 0: 
/*  357: 623 */           return;
/*  358:     */         case 1: 
/*  359: 625 */           output.writeObject(number, pipe, IdStrategy.this.DYNAMIC_VALUE_PIPE_SCHEMA, true);
/*  360:     */           
/*  361: 627 */           break;
/*  362:     */         default: 
/*  363: 629 */           throw new ProtostuffException("The collection was incorrectly serialized.");
/*  364:     */         }
/*  365:     */       }
/*  366:     */     }
/*  367:     */   };
/*  368: 636 */   final Schema<Object> ARRAY_SCHEMA = new Schema()
/*  369:     */   {
/*  370:     */     public String getFieldName(int number)
/*  371:     */     {
/*  372: 641 */       return number == 1 ? "v" : null;
/*  373:     */     }
/*  374:     */     
/*  375:     */     public int getFieldNumber(String name)
/*  376:     */     {
/*  377: 647 */       return (name.length() == 1) && (name.charAt(0) == 'v') ? 1 : 0;
/*  378:     */     }
/*  379:     */     
/*  380:     */     public boolean isInitialized(Object owner)
/*  381:     */     {
/*  382: 653 */       return true;
/*  383:     */     }
/*  384:     */     
/*  385:     */     public String messageFullName()
/*  386:     */     {
/*  387: 659 */       return Array.class.getName();
/*  388:     */     }
/*  389:     */     
/*  390:     */     public String messageName()
/*  391:     */     {
/*  392: 665 */       return Array.class.getSimpleName();
/*  393:     */     }
/*  394:     */     
/*  395:     */     public Object newMessage()
/*  396:     */     {
/*  397: 671 */       throw new UnsupportedOperationException();
/*  398:     */     }
/*  399:     */     
/*  400:     */     public Class<? super Object> typeClass()
/*  401:     */     {
/*  402: 677 */       return Object.class;
/*  403:     */     }
/*  404:     */     
/*  405:     */     public void mergeFrom(Input input, Object message)
/*  406:     */       throws IOException
/*  407:     */     {
/*  408: 684 */       throw new UnsupportedOperationException();
/*  409:     */     }
/*  410:     */     
/*  411:     */     public void writeTo(Output output, Object message)
/*  412:     */       throws IOException
/*  413:     */     {
/*  414: 690 */       int i = 0;
/*  415: 690 */       for (int len = Array.getLength(message); i < len; i++)
/*  416:     */       {
/*  417: 692 */         Object value = Array.get(message, i);
/*  418: 693 */         if (value != null) {
/*  419: 695 */           output.writeObject(1, value, IdStrategy.this.DYNAMIC_VALUE_SCHEMA, true);
/*  420:     */         }
/*  421:     */       }
/*  422:     */     }
/*  423:     */   };
/*  424: 701 */   final Pipe.Schema<Object> ARRAY_PIPE_SCHEMA = new Pipe.Schema(this.ARRAY_SCHEMA)
/*  425:     */   {
/*  426:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  427:     */       throws IOException
/*  428:     */     {
/*  429: 708 */       int number = input.readFieldNumber(this.wrappedSchema);
/*  430: 709 */       for (;; number = input.readFieldNumber(this.wrappedSchema)) {
/*  431: 711 */         switch (number)
/*  432:     */         {
/*  433:     */         case 0: 
/*  434: 714 */           return;
/*  435:     */         case 1: 
/*  436: 716 */           output.writeObject(number, pipe, IdStrategy.this.DYNAMIC_VALUE_PIPE_SCHEMA, true);
/*  437:     */           
/*  438: 718 */           break;
/*  439:     */         default: 
/*  440: 720 */           throw new ProtostuffException("The array was incorrectly serialized.");
/*  441:     */         }
/*  442:     */       }
/*  443:     */     }
/*  444:     */   };
/*  445: 727 */   final Schema<Map<Object, Object>> MAP_SCHEMA = new Schema()
/*  446:     */   {
/*  447:     */     public final String getFieldName(int number)
/*  448:     */     {
/*  449: 732 */       return number == 1 ? "e" : null;
/*  450:     */     }
/*  451:     */     
/*  452:     */     public final int getFieldNumber(String name)
/*  453:     */     {
/*  454: 738 */       return (name.length() == 1) && (name.charAt(0) == 'e') ? 1 : 0;
/*  455:     */     }
/*  456:     */     
/*  457:     */     public boolean isInitialized(Map<Object, Object> owner)
/*  458:     */     {
/*  459: 744 */       return true;
/*  460:     */     }
/*  461:     */     
/*  462:     */     public String messageFullName()
/*  463:     */     {
/*  464: 750 */       return Map.class.getName();
/*  465:     */     }
/*  466:     */     
/*  467:     */     public String messageName()
/*  468:     */     {
/*  469: 756 */       return Map.class.getSimpleName();
/*  470:     */     }
/*  471:     */     
/*  472:     */     public Map<Object, Object> newMessage()
/*  473:     */     {
/*  474: 762 */       throw new UnsupportedOperationException();
/*  475:     */     }
/*  476:     */     
/*  477:     */     public Class<? super Map<Object, Object>> typeClass()
/*  478:     */     {
/*  479: 768 */       return Map.class;
/*  480:     */     }
/*  481:     */     
/*  482:     */     public void mergeFrom(Input input, Map<Object, Object> message)
/*  483:     */       throws IOException
/*  484:     */     {
/*  485: 775 */       IdStrategy.PMapWrapper entry = null;
/*  486: 776 */       int number = input.readFieldNumber(this);
/*  487: 777 */       for (;; number = input.readFieldNumber(this)) {
/*  488: 779 */         switch (number)
/*  489:     */         {
/*  490:     */         case 0: 
/*  491: 782 */           return;
/*  492:     */         case 1: 
/*  493: 784 */           if (entry == null) {
/*  494: 787 */             entry = new IdStrategy.PMapWrapper(message);
/*  495:     */           }
/*  496: 790 */           if (entry != input.mergeObject(entry, IdStrategy.this.ENTRY_SCHEMA)) {
/*  497: 798 */             throw new IllegalStateException("A Map.Entry will always be unique, hence it cannot be a reference obtained from " + input.getClass().getName());
/*  498:     */           }
/*  499:     */           break;
/*  500:     */         default: 
/*  501: 802 */           throw new ProtostuffException("The map was incorrectly serialized.");
/*  502:     */         }
/*  503:     */       }
/*  504:     */     }
/*  505:     */     
/*  506:     */     public void writeTo(Output output, Map<Object, Object> message)
/*  507:     */       throws IOException
/*  508:     */     {
/*  509: 812 */       for (Map.Entry<Object, Object> entry : message.entrySet()) {
/*  510: 814 */         output.writeObject(1, entry, IdStrategy.this.ENTRY_SCHEMA, true);
/*  511:     */       }
/*  512:     */     }
/*  513:     */   };
/*  514: 819 */   final Pipe.Schema<Map<Object, Object>> MAP_PIPE_SCHEMA = new Pipe.Schema(this.MAP_SCHEMA)
/*  515:     */   {
/*  516:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  517:     */       throws IOException
/*  518:     */     {
/*  519: 826 */       int number = input.readFieldNumber(this.wrappedSchema);
/*  520: 827 */       for (;; number = input.readFieldNumber(this.wrappedSchema)) {
/*  521: 829 */         switch (number)
/*  522:     */         {
/*  523:     */         case 0: 
/*  524: 832 */           return;
/*  525:     */         case 1: 
/*  526: 834 */           output.writeObject(number, pipe, IdStrategy.this.ENTRY_PIPE_SCHEMA, true);
/*  527: 835 */           break;
/*  528:     */         default: 
/*  529: 837 */           throw new ProtostuffException("The map was incorrectly serialized.");
/*  530:     */         }
/*  531:     */       }
/*  532:     */     }
/*  533:     */   };
/*  534: 844 */   final Schema<Map.Entry<Object, Object>> ENTRY_SCHEMA = new Schema()
/*  535:     */   {
/*  536:     */     public final String getFieldName(int number)
/*  537:     */     {
/*  538: 849 */       switch (number)
/*  539:     */       {
/*  540:     */       case 1: 
/*  541: 852 */         return "k";
/*  542:     */       case 2: 
/*  543: 854 */         return "v";
/*  544:     */       }
/*  545: 856 */       return null;
/*  546:     */     }
/*  547:     */     
/*  548:     */     public final int getFieldNumber(String name)
/*  549:     */     {
/*  550: 863 */       if (name.length() != 1) {
/*  551: 864 */         return 0;
/*  552:     */       }
/*  553: 866 */       switch (name.charAt(0))
/*  554:     */       {
/*  555:     */       case 'k': 
/*  556: 869 */         return 1;
/*  557:     */       case 'v': 
/*  558: 871 */         return 2;
/*  559:     */       }
/*  560: 873 */       return 0;
/*  561:     */     }
/*  562:     */     
/*  563:     */     public boolean isInitialized(Map.Entry<Object, Object> message)
/*  564:     */     {
/*  565: 880 */       return true;
/*  566:     */     }
/*  567:     */     
/*  568:     */     public String messageFullName()
/*  569:     */     {
/*  570: 886 */       return Map.Entry.class.getName();
/*  571:     */     }
/*  572:     */     
/*  573:     */     public String messageName()
/*  574:     */     {
/*  575: 892 */       return Map.Entry.class.getSimpleName();
/*  576:     */     }
/*  577:     */     
/*  578:     */     public Map.Entry<Object, Object> newMessage()
/*  579:     */     {
/*  580: 898 */       throw new UnsupportedOperationException();
/*  581:     */     }
/*  582:     */     
/*  583:     */     public Class<? super Map.Entry<Object, Object>> typeClass()
/*  584:     */     {
/*  585: 904 */       return Map.Entry.class;
/*  586:     */     }
/*  587:     */     
/*  588:     */     public void mergeFrom(Input input, Map.Entry<Object, Object> message)
/*  589:     */       throws IOException
/*  590:     */     {
/*  591: 912 */       IdStrategy.PMapWrapper entry = (IdStrategy.PMapWrapper)message;
/*  592:     */       
/*  593: 914 */       Object key = null;Object value = null;
/*  594: 915 */       int number = input.readFieldNumber(this);
/*  595: 916 */       for (;; number = input.readFieldNumber(this)) {
/*  596: 918 */         switch (number)
/*  597:     */         {
/*  598:     */         case 0: 
/*  599: 921 */           entry.map.put(key, value);
/*  600: 922 */           return;
/*  601:     */         case 1: 
/*  602: 924 */           if (key != null) {
/*  603: 926 */             throw new ProtostuffException("The map was incorrectly serialized.");
/*  604:     */           }
/*  605: 929 */           key = input.mergeObject(entry, IdStrategy.this.DYNAMIC_VALUE_SCHEMA);
/*  606: 930 */           if (entry != key)
/*  607:     */           {
/*  608: 933 */             if ((!$assertionsDisabled) && (key == null)) {
/*  609: 933 */               throw new AssertionError();
/*  610:     */             }
/*  611:     */           }
/*  612:     */           else
/*  613:     */           {
/*  614: 938 */             key = entry.setValue(null);
/*  615: 939 */             if ((!$assertionsDisabled) && (key == null)) {
/*  616: 939 */               throw new AssertionError();
/*  617:     */             }
/*  618:     */           }
/*  619:     */           break;
/*  620:     */         case 2: 
/*  621: 943 */           if (value != null) {
/*  622: 945 */             throw new ProtostuffException("The map was incorrectly serialized.");
/*  623:     */           }
/*  624: 948 */           value = input.mergeObject(entry, IdStrategy.this.DYNAMIC_VALUE_SCHEMA);
/*  625: 949 */           if (entry != value)
/*  626:     */           {
/*  627: 952 */             if ((!$assertionsDisabled) && (value == null)) {
/*  628: 952 */               throw new AssertionError();
/*  629:     */             }
/*  630:     */           }
/*  631:     */           else
/*  632:     */           {
/*  633: 957 */             value = entry.setValue(null);
/*  634: 958 */             if ((!$assertionsDisabled) && (value == null)) {
/*  635: 958 */               throw new AssertionError();
/*  636:     */             }
/*  637:     */           }
/*  638:     */           break;
/*  639:     */         default: 
/*  640: 962 */           throw new ProtostuffException("The map was incorrectly serialized.");
/*  641:     */         }
/*  642:     */       }
/*  643:     */     }
/*  644:     */     
/*  645:     */     public void writeTo(Output output, Map.Entry<Object, Object> entry)
/*  646:     */       throws IOException
/*  647:     */     {
/*  648: 972 */       if (entry.getKey() != null) {
/*  649: 973 */         output.writeObject(1, entry.getKey(), IdStrategy.this.DYNAMIC_VALUE_SCHEMA, false);
/*  650:     */       }
/*  651: 976 */       if (entry.getValue() != null) {
/*  652: 977 */         output.writeObject(2, entry.getValue(), IdStrategy.this.DYNAMIC_VALUE_SCHEMA, false);
/*  653:     */       }
/*  654:     */     }
/*  655:     */   };
/*  656: 982 */   final Pipe.Schema<Map.Entry<Object, Object>> ENTRY_PIPE_SCHEMA = new Pipe.Schema(this.ENTRY_SCHEMA)
/*  657:     */   {
/*  658:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  659:     */       throws IOException
/*  660:     */     {
/*  661: 989 */       int number = input.readFieldNumber(this.wrappedSchema);
/*  662: 990 */       for (;; number = input.readFieldNumber(this.wrappedSchema)) {
/*  663: 992 */         switch (number)
/*  664:     */         {
/*  665:     */         case 0: 
/*  666: 995 */           return;
/*  667:     */         case 1: 
/*  668: 997 */           output.writeObject(number, pipe, IdStrategy.this.DYNAMIC_VALUE_PIPE_SCHEMA, false);
/*  669:     */           
/*  670: 999 */           break;
/*  671:     */         case 2: 
/*  672:1001 */           output.writeObject(number, pipe, IdStrategy.this.DYNAMIC_VALUE_PIPE_SCHEMA, false);
/*  673:     */           
/*  674:1003 */           break;
/*  675:     */         default: 
/*  676:1005 */           throw new ProtostuffException("The map was incorrectly serialized.");
/*  677:     */         }
/*  678:     */       }
/*  679:     */     }
/*  680:     */   };
/*  681:1012 */   final Schema<Object> OBJECT_SCHEMA = new Schema()
/*  682:     */   {
/*  683:     */     public String getFieldName(int number)
/*  684:     */     {
/*  685:1017 */       return ObjectSchema.name(number);
/*  686:     */     }
/*  687:     */     
/*  688:     */     public int getFieldNumber(String name)
/*  689:     */     {
/*  690:1023 */       return ObjectSchema.number(name);
/*  691:     */     }
/*  692:     */     
/*  693:     */     public boolean isInitialized(Object owner)
/*  694:     */     {
/*  695:1029 */       return true;
/*  696:     */     }
/*  697:     */     
/*  698:     */     public String messageFullName()
/*  699:     */     {
/*  700:1035 */       return Object.class.getName();
/*  701:     */     }
/*  702:     */     
/*  703:     */     public String messageName()
/*  704:     */     {
/*  705:1041 */       return Object.class.getSimpleName();
/*  706:     */     }
/*  707:     */     
/*  708:     */     public Object newMessage()
/*  709:     */     {
/*  710:1048 */       throw new UnsupportedOperationException();
/*  711:     */     }
/*  712:     */     
/*  713:     */     public Class<? super Object> typeClass()
/*  714:     */     {
/*  715:1054 */       return Object.class;
/*  716:     */     }
/*  717:     */     
/*  718:     */     public void mergeFrom(Input input, Object owner)
/*  719:     */       throws IOException
/*  720:     */     {
/*  721:1060 */       ((IdStrategy.Wrapper)owner).value = ObjectSchema.readObjectFrom(input, this, owner, IdStrategy.this);
/*  722:     */     }
/*  723:     */     
/*  724:     */     public void writeTo(Output output, Object message)
/*  725:     */       throws IOException
/*  726:     */     {
/*  727:1067 */       ObjectSchema.writeObjectTo(output, message, this, IdStrategy.this);
/*  728:     */     }
/*  729:     */   };
/*  730:1071 */   final Pipe.Schema<Object> OBJECT_PIPE_SCHEMA = new Pipe.Schema(this.OBJECT_SCHEMA)
/*  731:     */   {
/*  732:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  733:     */       throws IOException
/*  734:     */     {
/*  735:1078 */       ObjectSchema.transferObject(this, pipe, input, output, IdStrategy.this);
/*  736:     */     }
/*  737:     */   };
/*  738:1083 */   final Schema<Object> CLASS_SCHEMA = new Schema()
/*  739:     */   {
/*  740:     */     public String getFieldName(int number)
/*  741:     */     {
/*  742:1088 */       return ClassSchema.name(number);
/*  743:     */     }
/*  744:     */     
/*  745:     */     public int getFieldNumber(String name)
/*  746:     */     {
/*  747:1094 */       return ClassSchema.number(name);
/*  748:     */     }
/*  749:     */     
/*  750:     */     public boolean isInitialized(Object owner)
/*  751:     */     {
/*  752:1100 */       return true;
/*  753:     */     }
/*  754:     */     
/*  755:     */     public String messageFullName()
/*  756:     */     {
/*  757:1106 */       return Class.class.getName();
/*  758:     */     }
/*  759:     */     
/*  760:     */     public String messageName()
/*  761:     */     {
/*  762:1112 */       return Class.class.getSimpleName();
/*  763:     */     }
/*  764:     */     
/*  765:     */     public Object newMessage()
/*  766:     */     {
/*  767:1119 */       throw new UnsupportedOperationException();
/*  768:     */     }
/*  769:     */     
/*  770:     */     public Class<? super Object> typeClass()
/*  771:     */     {
/*  772:1125 */       return Object.class;
/*  773:     */     }
/*  774:     */     
/*  775:     */     public void mergeFrom(Input input, Object owner)
/*  776:     */       throws IOException
/*  777:     */     {
/*  778:1131 */       ((IdStrategy.Wrapper)owner).value = ClassSchema.readObjectFrom(input, this, owner, IdStrategy.this);
/*  779:     */     }
/*  780:     */     
/*  781:     */     public void writeTo(Output output, Object message)
/*  782:     */       throws IOException
/*  783:     */     {
/*  784:1138 */       ClassSchema.writeObjectTo(output, message, this, IdStrategy.this);
/*  785:     */     }
/*  786:     */   };
/*  787:1142 */   final Pipe.Schema<Object> CLASS_PIPE_SCHEMA = new Pipe.Schema(this.CLASS_SCHEMA)
/*  788:     */   {
/*  789:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  790:     */       throws IOException
/*  791:     */     {
/*  792:1149 */       ClassSchema.transferObject(this, pipe, input, output, IdStrategy.this);
/*  793:     */     }
/*  794:     */   };
/*  795:1154 */   final Schema<Object> POLYMORPHIC_COLLECTION_SCHEMA = new Schema()
/*  796:     */   {
/*  797:     */     public String getFieldName(int number)
/*  798:     */     {
/*  799:1159 */       return PolymorphicCollectionSchema.name(number);
/*  800:     */     }
/*  801:     */     
/*  802:     */     public int getFieldNumber(String name)
/*  803:     */     {
/*  804:1165 */       return PolymorphicCollectionSchema.number(name);
/*  805:     */     }
/*  806:     */     
/*  807:     */     public boolean isInitialized(Object owner)
/*  808:     */     {
/*  809:1171 */       return true;
/*  810:     */     }
/*  811:     */     
/*  812:     */     public String messageFullName()
/*  813:     */     {
/*  814:1177 */       return Collection.class.getName();
/*  815:     */     }
/*  816:     */     
/*  817:     */     public String messageName()
/*  818:     */     {
/*  819:1183 */       return Collection.class.getSimpleName();
/*  820:     */     }
/*  821:     */     
/*  822:     */     public Object newMessage()
/*  823:     */     {
/*  824:1190 */       throw new UnsupportedOperationException();
/*  825:     */     }
/*  826:     */     
/*  827:     */     public Class<? super Object> typeClass()
/*  828:     */     {
/*  829:1196 */       return Object.class;
/*  830:     */     }
/*  831:     */     
/*  832:     */     public void mergeFrom(Input input, Object owner)
/*  833:     */       throws IOException
/*  834:     */     {
/*  835:1203 */       ((IdStrategy.Wrapper)owner).value = PolymorphicCollectionSchema.readObjectFrom(input, this, owner, IdStrategy.this);
/*  836:     */     }
/*  837:     */     
/*  838:     */     public void writeTo(Output output, Object message)
/*  839:     */       throws IOException
/*  840:     */     {
/*  841:1209 */       PolymorphicCollectionSchema.writeObjectTo(output, message, this, IdStrategy.this);
/*  842:     */     }
/*  843:     */   };
/*  844:1214 */   final Pipe.Schema<Object> POLYMORPHIC_COLLECTION_PIPE_SCHEMA = new Pipe.Schema(this.POLYMORPHIC_COLLECTION_SCHEMA)
/*  845:     */   {
/*  846:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  847:     */       throws IOException
/*  848:     */     {
/*  849:1221 */       PolymorphicCollectionSchema.transferObject(this, pipe, input, output, IdStrategy.this);
/*  850:     */     }
/*  851:     */   };
/*  852:1226 */   final Schema<Object> POLYMORPHIC_MAP_SCHEMA = new Schema()
/*  853:     */   {
/*  854:     */     public String getFieldName(int number)
/*  855:     */     {
/*  856:1231 */       return PolymorphicMapSchema.name(number);
/*  857:     */     }
/*  858:     */     
/*  859:     */     public int getFieldNumber(String name)
/*  860:     */     {
/*  861:1237 */       return PolymorphicMapSchema.number(name);
/*  862:     */     }
/*  863:     */     
/*  864:     */     public boolean isInitialized(Object owner)
/*  865:     */     {
/*  866:1243 */       return true;
/*  867:     */     }
/*  868:     */     
/*  869:     */     public String messageFullName()
/*  870:     */     {
/*  871:1249 */       return Map.class.getName();
/*  872:     */     }
/*  873:     */     
/*  874:     */     public String messageName()
/*  875:     */     {
/*  876:1255 */       return Map.class.getSimpleName();
/*  877:     */     }
/*  878:     */     
/*  879:     */     public Object newMessage()
/*  880:     */     {
/*  881:1262 */       throw new UnsupportedOperationException();
/*  882:     */     }
/*  883:     */     
/*  884:     */     public Class<? super Object> typeClass()
/*  885:     */     {
/*  886:1268 */       return Object.class;
/*  887:     */     }
/*  888:     */     
/*  889:     */     public void mergeFrom(Input input, Object owner)
/*  890:     */       throws IOException
/*  891:     */     {
/*  892:1274 */       ((IdStrategy.Wrapper)owner).value = PolymorphicMapSchema.readObjectFrom(input, this, owner, IdStrategy.this);
/*  893:     */     }
/*  894:     */     
/*  895:     */     public void writeTo(Output output, Object message)
/*  896:     */       throws IOException
/*  897:     */     {
/*  898:1281 */       PolymorphicMapSchema.writeObjectTo(output, message, this, IdStrategy.this);
/*  899:     */     }
/*  900:     */   };
/*  901:1286 */   final Pipe.Schema<Object> POLYMORPHIC_MAP_PIPE_SCHEMA = new Pipe.Schema(this.POLYMORPHIC_MAP_SCHEMA)
/*  902:     */   {
/*  903:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  904:     */       throws IOException
/*  905:     */     {
/*  906:1293 */       PolymorphicMapSchema.transferObject(this, pipe, input, output, IdStrategy.this);
/*  907:     */     }
/*  908:     */   };
/*  909:1300 */   final ArraySchemas.BoolArray ARRAY_BOOL_PRIMITIVE_SCHEMA = new ArraySchemas.BoolArray(this, null, true);
/*  910:1302 */   final ArraySchemas.BoolArray ARRAY_BOOL_BOXED_SCHEMA = new ArraySchemas.BoolArray(this, null, false);
/*  911:1304 */   final ArraySchemas.BoolArray ARRAY_BOOL_DERIVED_SCHEMA = new ArraySchemas.BoolArray(this, null, false)
/*  912:     */   {
/*  913:     */     public Object readFrom(Input input, Object owner)
/*  914:     */       throws IOException
/*  915:     */     {
/*  916:1310 */       if (1 != input.readFieldNumber(this)) {
/*  917:1311 */         throw new ProtostuffException("Corrupt input.");
/*  918:     */       }
/*  919:1313 */       int len = input.readInt32();
/*  920:1314 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/*  921:1315 */         readBoxedFrom(input, owner, -len - 1);
/*  922:     */     }
/*  923:     */     
/*  924:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  925:     */       throws IOException
/*  926:     */     {
/*  927:1322 */       if (primitive) {
/*  928:1323 */         output.writeInt32(1, len, false);
/*  929:     */       } else {
/*  930:1325 */         output.writeInt32(1, -(len + 1), false);
/*  931:     */       }
/*  932:     */     }
/*  933:     */     
/*  934:     */     public void writeTo(Output output, Object value)
/*  935:     */       throws IOException
/*  936:     */     {
/*  937:1331 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/*  938:     */     }
/*  939:     */     
/*  940:     */     protected void setValue(Object value, Object owner)
/*  941:     */     {
/*  942:1338 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  943:1339 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  944:     */       } else {
/*  945:1341 */         ((Collection)owner).add(value);
/*  946:     */       }
/*  947:     */     }
/*  948:     */   };
/*  949:1345 */   final ArraySchemas.CharArray ARRAY_CHAR_PRIMITIVE_SCHEMA = new ArraySchemas.CharArray(this, null, true);
/*  950:1347 */   final ArraySchemas.CharArray ARRAY_CHAR_BOXED_SCHEMA = new ArraySchemas.CharArray(this, null, false);
/*  951:1349 */   final ArraySchemas.CharArray ARRAY_CHAR_DERIVED_SCHEMA = new ArraySchemas.CharArray(this, null, false)
/*  952:     */   {
/*  953:     */     public Object readFrom(Input input, Object owner)
/*  954:     */       throws IOException
/*  955:     */     {
/*  956:1355 */       if (1 != input.readFieldNumber(this)) {
/*  957:1356 */         throw new ProtostuffException("Corrupt input.");
/*  958:     */       }
/*  959:1358 */       int len = input.readInt32();
/*  960:1359 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/*  961:1360 */         readBoxedFrom(input, owner, -len - 1);
/*  962:     */     }
/*  963:     */     
/*  964:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  965:     */       throws IOException
/*  966:     */     {
/*  967:1367 */       if (primitive) {
/*  968:1368 */         output.writeInt32(1, len, false);
/*  969:     */       } else {
/*  970:1370 */         output.writeInt32(1, -(len + 1), false);
/*  971:     */       }
/*  972:     */     }
/*  973:     */     
/*  974:     */     public void writeTo(Output output, Object value)
/*  975:     */       throws IOException
/*  976:     */     {
/*  977:1376 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/*  978:     */     }
/*  979:     */     
/*  980:     */     protected void setValue(Object value, Object owner)
/*  981:     */     {
/*  982:1383 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/*  983:1384 */         ((MapSchema.MapWrapper)owner).setValue(value);
/*  984:     */       } else {
/*  985:1386 */         ((Collection)owner).add(value);
/*  986:     */       }
/*  987:     */     }
/*  988:     */   };
/*  989:1390 */   final ArraySchemas.ShortArray ARRAY_SHORT_PRIMITIVE_SCHEMA = new ArraySchemas.ShortArray(this, null, true);
/*  990:1392 */   final ArraySchemas.ShortArray ARRAY_SHORT_BOXED_SCHEMA = new ArraySchemas.ShortArray(this, null, false);
/*  991:1394 */   final ArraySchemas.ShortArray ARRAY_SHORT_DERIVED_SCHEMA = new ArraySchemas.ShortArray(this, null, false)
/*  992:     */   {
/*  993:     */     public Object readFrom(Input input, Object owner)
/*  994:     */       throws IOException
/*  995:     */     {
/*  996:1400 */       if (1 != input.readFieldNumber(this)) {
/*  997:1401 */         throw new ProtostuffException("Corrupt input.");
/*  998:     */       }
/*  999:1403 */       int len = input.readInt32();
/* 1000:1404 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/* 1001:1405 */         readBoxedFrom(input, owner, -len - 1);
/* 1002:     */     }
/* 1003:     */     
/* 1004:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1005:     */       throws IOException
/* 1006:     */     {
/* 1007:1412 */       if (primitive) {
/* 1008:1413 */         output.writeInt32(1, len, false);
/* 1009:     */       } else {
/* 1010:1415 */         output.writeInt32(1, -(len + 1), false);
/* 1011:     */       }
/* 1012:     */     }
/* 1013:     */     
/* 1014:     */     public void writeTo(Output output, Object value)
/* 1015:     */       throws IOException
/* 1016:     */     {
/* 1017:1421 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/* 1018:     */     }
/* 1019:     */     
/* 1020:     */     protected void setValue(Object value, Object owner)
/* 1021:     */     {
/* 1022:1428 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1023:1429 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1024:     */       } else {
/* 1025:1431 */         ((Collection)owner).add(value);
/* 1026:     */       }
/* 1027:     */     }
/* 1028:     */   };
/* 1029:1435 */   final ArraySchemas.Int32Array ARRAY_INT32_PRIMITIVE_SCHEMA = new ArraySchemas.Int32Array(this, null, true);
/* 1030:1437 */   final ArraySchemas.Int32Array ARRAY_INT32_BOXED_SCHEMA = new ArraySchemas.Int32Array(this, null, false);
/* 1031:1439 */   final ArraySchemas.Int32Array ARRAY_INT32_DERIVED_SCHEMA = new ArraySchemas.Int32Array(this, null, false)
/* 1032:     */   {
/* 1033:     */     public Object readFrom(Input input, Object owner)
/* 1034:     */       throws IOException
/* 1035:     */     {
/* 1036:1445 */       if (1 != input.readFieldNumber(this)) {
/* 1037:1446 */         throw new ProtostuffException("Corrupt input.");
/* 1038:     */       }
/* 1039:1448 */       int len = input.readInt32();
/* 1040:1449 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/* 1041:1450 */         readBoxedFrom(input, owner, -len - 1);
/* 1042:     */     }
/* 1043:     */     
/* 1044:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1045:     */       throws IOException
/* 1046:     */     {
/* 1047:1457 */       if (primitive) {
/* 1048:1458 */         output.writeInt32(1, len, false);
/* 1049:     */       } else {
/* 1050:1460 */         output.writeInt32(1, -(len + 1), false);
/* 1051:     */       }
/* 1052:     */     }
/* 1053:     */     
/* 1054:     */     public void writeTo(Output output, Object value)
/* 1055:     */       throws IOException
/* 1056:     */     {
/* 1057:1466 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/* 1058:     */     }
/* 1059:     */     
/* 1060:     */     protected void setValue(Object value, Object owner)
/* 1061:     */     {
/* 1062:1473 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1063:1474 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1064:     */       } else {
/* 1065:1476 */         ((Collection)owner).add(value);
/* 1066:     */       }
/* 1067:     */     }
/* 1068:     */   };
/* 1069:1480 */   final ArraySchemas.Int64Array ARRAY_INT64_PRIMITIVE_SCHEMA = new ArraySchemas.Int64Array(this, null, true);
/* 1070:1482 */   final ArraySchemas.Int64Array ARRAY_INT64_BOXED_SCHEMA = new ArraySchemas.Int64Array(this, null, false);
/* 1071:1484 */   final ArraySchemas.Int64Array ARRAY_INT64_DERIVED_SCHEMA = new ArraySchemas.Int64Array(this, null, false)
/* 1072:     */   {
/* 1073:     */     public Object readFrom(Input input, Object owner)
/* 1074:     */       throws IOException
/* 1075:     */     {
/* 1076:1490 */       if (1 != input.readFieldNumber(this)) {
/* 1077:1491 */         throw new ProtostuffException("Corrupt input.");
/* 1078:     */       }
/* 1079:1493 */       int len = input.readInt32();
/* 1080:1494 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/* 1081:1495 */         readBoxedFrom(input, owner, -len - 1);
/* 1082:     */     }
/* 1083:     */     
/* 1084:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1085:     */       throws IOException
/* 1086:     */     {
/* 1087:1502 */       if (primitive) {
/* 1088:1503 */         output.writeInt32(1, len, false);
/* 1089:     */       } else {
/* 1090:1505 */         output.writeInt32(1, -(len + 1), false);
/* 1091:     */       }
/* 1092:     */     }
/* 1093:     */     
/* 1094:     */     public void writeTo(Output output, Object value)
/* 1095:     */       throws IOException
/* 1096:     */     {
/* 1097:1511 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/* 1098:     */     }
/* 1099:     */     
/* 1100:     */     protected void setValue(Object value, Object owner)
/* 1101:     */     {
/* 1102:1518 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1103:1519 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1104:     */       } else {
/* 1105:1521 */         ((Collection)owner).add(value);
/* 1106:     */       }
/* 1107:     */     }
/* 1108:     */   };
/* 1109:1525 */   final ArraySchemas.FloatArray ARRAY_FLOAT_PRIMITIVE_SCHEMA = new ArraySchemas.FloatArray(this, null, true);
/* 1110:1527 */   final ArraySchemas.FloatArray ARRAY_FLOAT_BOXED_SCHEMA = new ArraySchemas.FloatArray(this, null, false);
/* 1111:1529 */   final ArraySchemas.FloatArray ARRAY_FLOAT_DERIVED_SCHEMA = new ArraySchemas.FloatArray(this, null, false)
/* 1112:     */   {
/* 1113:     */     public Object readFrom(Input input, Object owner)
/* 1114:     */       throws IOException
/* 1115:     */     {
/* 1116:1535 */       if (1 != input.readFieldNumber(this)) {
/* 1117:1536 */         throw new ProtostuffException("Corrupt input.");
/* 1118:     */       }
/* 1119:1538 */       int len = input.readInt32();
/* 1120:1539 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/* 1121:1540 */         readBoxedFrom(input, owner, -len - 1);
/* 1122:     */     }
/* 1123:     */     
/* 1124:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1125:     */       throws IOException
/* 1126:     */     {
/* 1127:1547 */       if (primitive) {
/* 1128:1548 */         output.writeInt32(1, len, false);
/* 1129:     */       } else {
/* 1130:1550 */         output.writeInt32(1, -(len + 1), false);
/* 1131:     */       }
/* 1132:     */     }
/* 1133:     */     
/* 1134:     */     public void writeTo(Output output, Object value)
/* 1135:     */       throws IOException
/* 1136:     */     {
/* 1137:1556 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/* 1138:     */     }
/* 1139:     */     
/* 1140:     */     protected void setValue(Object value, Object owner)
/* 1141:     */     {
/* 1142:1563 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1143:1564 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1144:     */       } else {
/* 1145:1566 */         ((Collection)owner).add(value);
/* 1146:     */       }
/* 1147:     */     }
/* 1148:     */   };
/* 1149:1570 */   final ArraySchemas.DoubleArray ARRAY_DOUBLE_PRIMITIVE_SCHEMA = new ArraySchemas.DoubleArray(this, null, true);
/* 1150:1572 */   final ArraySchemas.DoubleArray ARRAY_DOUBLE_BOXED_SCHEMA = new ArraySchemas.DoubleArray(this, null, false);
/* 1151:1574 */   final ArraySchemas.DoubleArray ARRAY_DOUBLE_DERIVED_SCHEMA = new ArraySchemas.DoubleArray(this, null, false)
/* 1152:     */   {
/* 1153:     */     public Object readFrom(Input input, Object owner)
/* 1154:     */       throws IOException
/* 1155:     */     {
/* 1156:1580 */       if (1 != input.readFieldNumber(this)) {
/* 1157:1581 */         throw new ProtostuffException("Corrupt input.");
/* 1158:     */       }
/* 1159:1583 */       int len = input.readInt32();
/* 1160:1584 */       return len >= 0 ? readPrimitiveFrom(input, owner, len) : 
/* 1161:1585 */         readBoxedFrom(input, owner, -len - 1);
/* 1162:     */     }
/* 1163:     */     
/* 1164:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1165:     */       throws IOException
/* 1166:     */     {
/* 1167:1592 */       if (primitive) {
/* 1168:1593 */         output.writeInt32(1, len, false);
/* 1169:     */       } else {
/* 1170:1595 */         output.writeInt32(1, -(len + 1), false);
/* 1171:     */       }
/* 1172:     */     }
/* 1173:     */     
/* 1174:     */     public void writeTo(Output output, Object value)
/* 1175:     */       throws IOException
/* 1176:     */     {
/* 1177:1601 */       writeTo(output, value, value.getClass().getComponentType().isPrimitive());
/* 1178:     */     }
/* 1179:     */     
/* 1180:     */     protected void setValue(Object value, Object owner)
/* 1181:     */     {
/* 1182:1608 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1183:1609 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1184:     */       } else {
/* 1185:1611 */         ((Collection)owner).add(value);
/* 1186:     */       }
/* 1187:     */     }
/* 1188:     */   };
/* 1189:1615 */   final ArraySchemas.StringArray ARRAY_STRING_SCHEMA = new ArraySchemas.StringArray(this, null)
/* 1190:     */   {
/* 1191:     */     protected void setValue(Object value, Object owner)
/* 1192:     */     {
/* 1193:1622 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1194:1623 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1195:     */       } else {
/* 1196:1625 */         ((Collection)owner).add(value);
/* 1197:     */       }
/* 1198:     */     }
/* 1199:     */   };
/* 1200:1629 */   final ArraySchemas.ByteStringArray ARRAY_BYTESTRING_SCHEMA = new ArraySchemas.ByteStringArray(this, null)
/* 1201:     */   {
/* 1202:     */     protected void setValue(Object value, Object owner)
/* 1203:     */     {
/* 1204:1636 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1205:1637 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1206:     */       } else {
/* 1207:1639 */         ((Collection)owner).add(value);
/* 1208:     */       }
/* 1209:     */     }
/* 1210:     */   };
/* 1211:1643 */   final ArraySchemas.ByteArrayArray ARRAY_BYTEARRAY_SCHEMA = new ArraySchemas.ByteArrayArray(this, null)
/* 1212:     */   {
/* 1213:     */     protected void setValue(Object value, Object owner)
/* 1214:     */     {
/* 1215:1650 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1216:1651 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1217:     */       } else {
/* 1218:1653 */         ((Collection)owner).add(value);
/* 1219:     */       }
/* 1220:     */     }
/* 1221:     */   };
/* 1222:1657 */   final ArraySchemas.BigDecimalArray ARRAY_BIGDECIMAL_SCHEMA = new ArraySchemas.BigDecimalArray(this, null)
/* 1223:     */   {
/* 1224:     */     protected void setValue(Object value, Object owner)
/* 1225:     */     {
/* 1226:1664 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1227:1665 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1228:     */       } else {
/* 1229:1667 */         ((Collection)owner).add(value);
/* 1230:     */       }
/* 1231:     */     }
/* 1232:     */   };
/* 1233:1671 */   final ArraySchemas.BigIntegerArray ARRAY_BIGINTEGER_SCHEMA = new ArraySchemas.BigIntegerArray(this, null)
/* 1234:     */   {
/* 1235:     */     protected void setValue(Object value, Object owner)
/* 1236:     */     {
/* 1237:1678 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1238:1679 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1239:     */       } else {
/* 1240:1681 */         ((Collection)owner).add(value);
/* 1241:     */       }
/* 1242:     */     }
/* 1243:     */   };
/* 1244:1685 */   final ArraySchemas.DateArray ARRAY_DATE_SCHEMA = new ArraySchemas.DateArray(this, null)
/* 1245:     */   {
/* 1246:     */     protected void setValue(Object value, Object owner)
/* 1247:     */     {
/* 1248:1692 */       if (MapSchema.MapWrapper.class == owner.getClass()) {
/* 1249:1693 */         ((MapSchema.MapWrapper)owner).setValue(value);
/* 1250:     */       } else {
/* 1251:1695 */         ((Collection)owner).add(value);
/* 1252:     */       }
/* 1253:     */     }
/* 1254:     */   };
/* 1255:     */   
/* 1256:     */   public static abstract interface Factory
/* 1257:     */   {
/* 1258:     */     public abstract IdStrategy create();
/* 1259:     */     
/* 1260:     */     public abstract void postCreate();
/* 1261:     */   }
/* 1262:     */   
/* 1263:     */   private static final class PMapWrapper
/* 1264:     */     implements Map.Entry<Object, Object>
/* 1265:     */   {
/* 1266:     */     final Map<Object, Object> map;
/* 1267:     */     private Object value;
/* 1268:     */     
/* 1269:     */     PMapWrapper(Map<Object, Object> map)
/* 1270:     */     {
/* 1271:1707 */       this.map = map;
/* 1272:     */     }
/* 1273:     */     
/* 1274:     */     public Object getKey()
/* 1275:     */     {
/* 1276:1713 */       throw new UnsupportedOperationException();
/* 1277:     */     }
/* 1278:     */     
/* 1279:     */     public Object getValue()
/* 1280:     */     {
/* 1281:1719 */       return this.value;
/* 1282:     */     }
/* 1283:     */     
/* 1284:     */     public Object setValue(Object value)
/* 1285:     */     {
/* 1286:1725 */       Object last = this.value;
/* 1287:1726 */       this.value = value;
/* 1288:1727 */       return last;
/* 1289:     */     }
/* 1290:     */   }
/* 1291:     */   
/* 1292:     */   protected static <T> T createMessageInstance(Class<T> clazz)
/* 1293:     */   {
/* 1294:     */     try
/* 1295:     */     {
/* 1296:1741 */       return clazz.newInstance();
/* 1297:     */     }
/* 1298:     */     catch (IllegalAccessException e)
/* 1299:     */     {
/* 1300:     */       try
/* 1301:     */       {
/* 1302:1747 */         Constructor<T> constructor = clazz.getDeclaredConstructor(new Class[0]);
/* 1303:1748 */         constructor.setAccessible(true);
/* 1304:1749 */         return constructor.newInstance(new Object[0]);
/* 1305:     */       }
/* 1306:     */       catch (Exception e1)
/* 1307:     */       {
/* 1308:1753 */         throw new RuntimeException(e);
/* 1309:     */       }
/* 1310:     */     }
/* 1311:     */     catch (InstantiationException e)
/* 1312:     */     {
/* 1313:1758 */       throw new RuntimeException(e);
/* 1314:     */     }
/* 1315:     */   }
/* 1316:     */   
/* 1317:     */   public abstract boolean isDelegateRegistered(Class<?> paramClass);
/* 1318:     */   
/* 1319:     */   public abstract <T> HasDelegate<T> getDelegateWrapper(Class<? super T> paramClass);
/* 1320:     */   
/* 1321:     */   public abstract <T> Delegate<T> getDelegate(Class<? super T> paramClass);
/* 1322:     */   
/* 1323:     */   public abstract boolean isRegistered(Class<?> paramClass);
/* 1324:     */   
/* 1325:     */   public abstract <T> HasSchema<T> getSchemaWrapper(Class<T> paramClass, boolean paramBoolean);
/* 1326:     */   
/* 1327:     */   protected abstract EnumIO<? extends Enum<?>> getEnumIO(Class<?> paramClass);
/* 1328:     */   
/* 1329:     */   protected abstract CollectionSchema.MessageFactory getCollectionFactory(Class<?> paramClass);
/* 1330:     */   
/* 1331:     */   protected abstract MapSchema.MessageFactory getMapFactory(Class<?> paramClass);
/* 1332:     */   
/* 1333:     */   protected abstract void writeCollectionIdTo(Output paramOutput, int paramInt, Class<?> paramClass)
/* 1334:     */     throws IOException;
/* 1335:     */   
/* 1336:     */   protected abstract void transferCollectionId(Input paramInput, Output paramOutput, int paramInt)
/* 1337:     */     throws IOException;
/* 1338:     */   
/* 1339:     */   protected abstract CollectionSchema.MessageFactory resolveCollectionFrom(Input paramInput)
/* 1340:     */     throws IOException;
/* 1341:     */   
/* 1342:     */   protected abstract void writeMapIdTo(Output paramOutput, int paramInt, Class<?> paramClass)
/* 1343:     */     throws IOException;
/* 1344:     */   
/* 1345:     */   protected abstract void transferMapId(Input paramInput, Output paramOutput, int paramInt)
/* 1346:     */     throws IOException;
/* 1347:     */   
/* 1348:     */   protected abstract MapSchema.MessageFactory resolveMapFrom(Input paramInput)
/* 1349:     */     throws IOException;
/* 1350:     */   
/* 1351:     */   protected abstract void writeEnumIdTo(Output paramOutput, int paramInt, Class<?> paramClass)
/* 1352:     */     throws IOException;
/* 1353:     */   
/* 1354:     */   protected abstract void transferEnumId(Input paramInput, Output paramOutput, int paramInt)
/* 1355:     */     throws IOException;
/* 1356:     */   
/* 1357:     */   protected abstract EnumIO<?> resolveEnumFrom(Input paramInput)
/* 1358:     */     throws IOException;
/* 1359:     */   
/* 1360:     */   protected abstract <T> HasSchema<T> tryWritePojoIdTo(Output paramOutput, int paramInt, Class<T> paramClass, boolean paramBoolean)
/* 1361:     */     throws IOException;
/* 1362:     */   
/* 1363:     */   protected abstract <T> HasSchema<T> writePojoIdTo(Output paramOutput, int paramInt, Class<T> paramClass)
/* 1364:     */     throws IOException;
/* 1365:     */   
/* 1366:     */   protected abstract <T> HasSchema<T> transferPojoId(Input paramInput, Output paramOutput, int paramInt)
/* 1367:     */     throws IOException;
/* 1368:     */   
/* 1369:     */   protected abstract <T> HasSchema<T> resolvePojoFrom(Input paramInput, int paramInt)
/* 1370:     */     throws IOException;
/* 1371:     */   
/* 1372:     */   protected abstract <T> Schema<T> writeMessageIdTo(Output paramOutput, int paramInt, Message<T> paramMessage)
/* 1373:     */     throws IOException;
/* 1374:     */   
/* 1375:     */   protected abstract <T> HasDelegate<T> tryWriteDelegateIdTo(Output paramOutput, int paramInt, Class<T> paramClass)
/* 1376:     */     throws IOException;
/* 1377:     */   
/* 1378:     */   protected abstract <T> HasDelegate<T> transferDelegateId(Input paramInput, Output paramOutput, int paramInt)
/* 1379:     */     throws IOException;
/* 1380:     */   
/* 1381:     */   protected abstract <T> HasDelegate<T> resolveDelegateFrom(Input paramInput)
/* 1382:     */     throws IOException;
/* 1383:     */   
/* 1384:     */   protected abstract void writeArrayIdTo(Output paramOutput, Class<?> paramClass)
/* 1385:     */     throws IOException;
/* 1386:     */   
/* 1387:     */   protected abstract void transferArrayId(Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean)
/* 1388:     */     throws IOException;
/* 1389:     */   
/* 1390:     */   protected abstract Class<?> resolveArrayComponentTypeFrom(Input paramInput, boolean paramBoolean)
/* 1391:     */     throws IOException;
/* 1392:     */   
/* 1393:     */   protected abstract void writeClassIdTo(Output paramOutput, Class<?> paramClass, boolean paramBoolean)
/* 1394:     */     throws IOException;
/* 1395:     */   
/* 1396:     */   protected abstract void transferClassId(Input paramInput, Output paramOutput, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
/* 1397:     */     throws IOException;
/* 1398:     */   
/* 1399:     */   protected abstract Class<?> resolveClassFrom(Input paramInput, boolean paramBoolean1, boolean paramBoolean2)
/* 1400:     */     throws IOException;
/* 1401:     */   
/* 1402:     */   static final class Wrapper
/* 1403:     */   {
/* 1404:     */     Object value;
/* 1405:     */   }
/* 1406:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.IdStrategy
 * JD-Core Version:    0.7.0.1
 */