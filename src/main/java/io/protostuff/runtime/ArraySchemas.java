/*    1:     */ package io.protostuff.runtime;
/*    2:     */ 
/*    3:     */ import io.protostuff.ByteString;
/*    4:     */ import io.protostuff.GraphInput;
/*    5:     */ import io.protostuff.Input;
/*    6:     */ import io.protostuff.Output;
/*    7:     */ import io.protostuff.Pipe;
/*    8:     */ import io.protostuff.Pipe.Schema;
/*    9:     */ import io.protostuff.ProtostuffException;
/*   10:     */ import io.protostuff.Schema;
/*   11:     */ import java.io.IOException;
/*   12:     */ import java.lang.reflect.Array;
/*   13:     */ import java.math.BigDecimal;
/*   14:     */ import java.math.BigInteger;
/*   15:     */ import java.util.Date;
/*   16:     */ 
/*   17:     */ public final class ArraySchemas
/*   18:     */ {
/*   19:     */   static final int ID_ARRAY_LEN = 1;
/*   20:     */   static final int ID_ARRAY_DATA = 2;
/*   21:     */   static final int ID_ARRAY_NULLCOUNT = 3;
/*   22:     */   static final String STR_ARRAY_LEN = "a";
/*   23:     */   static final String STR_ARRAY_DATA = "b";
/*   24:     */   static final String STR_ARRAY_NULLCOUNT = "c";
/*   25:     */   
/*   26:     */   static boolean isPrimitive(int arrayId)
/*   27:     */   {
/*   28:  72 */     return arrayId < 8;
/*   29:     */   }
/*   30:     */   
/*   31:     */   static int toArrayId(int id, boolean primitive)
/*   32:     */   {
/*   33:  77 */     if (primitive) {
/*   34:  78 */       return id - 1;
/*   35:     */     }
/*   36:  81 */     return id < 9 ? id - 1 | 0x8 : id + 7;
/*   37:     */   }
/*   38:     */   
/*   39:     */   static int toInlineId(int arrayId)
/*   40:     */   {
/*   41:  86 */     if (arrayId < 8) {
/*   42:  87 */       return arrayId + 1;
/*   43:     */     }
/*   44:  89 */     if (arrayId < 16) {
/*   45:  90 */       return 1 + (arrayId & 0x7);
/*   46:     */     }
/*   47:  92 */     return arrayId - 7;
/*   48:     */   }
/*   49:     */   
/*   50:     */   static Base getSchema(int id, boolean primitive, IdStrategy strategy)
/*   51:     */   {
/*   52:  97 */     switch (id)
/*   53:     */     {
/*   54:     */     case 1: 
/*   55: 100 */       return primitive ? strategy.ARRAY_BOOL_PRIMITIVE_SCHEMA : strategy.ARRAY_BOOL_BOXED_SCHEMA;
/*   56:     */     case 3: 
/*   57: 103 */       return primitive ? strategy.ARRAY_CHAR_PRIMITIVE_SCHEMA : strategy.ARRAY_CHAR_BOXED_SCHEMA;
/*   58:     */     case 4: 
/*   59: 106 */       return primitive ? strategy.ARRAY_SHORT_PRIMITIVE_SCHEMA : strategy.ARRAY_SHORT_BOXED_SCHEMA;
/*   60:     */     case 5: 
/*   61: 109 */       return primitive ? strategy.ARRAY_INT32_PRIMITIVE_SCHEMA : strategy.ARRAY_INT32_BOXED_SCHEMA;
/*   62:     */     case 6: 
/*   63: 112 */       return primitive ? strategy.ARRAY_INT64_PRIMITIVE_SCHEMA : strategy.ARRAY_INT64_BOXED_SCHEMA;
/*   64:     */     case 7: 
/*   65: 115 */       return primitive ? strategy.ARRAY_FLOAT_PRIMITIVE_SCHEMA : strategy.ARRAY_FLOAT_BOXED_SCHEMA;
/*   66:     */     case 8: 
/*   67: 118 */       return primitive ? strategy.ARRAY_DOUBLE_PRIMITIVE_SCHEMA : strategy.ARRAY_DOUBLE_BOXED_SCHEMA;
/*   68:     */     case 9: 
/*   69: 121 */       return strategy.ARRAY_STRING_SCHEMA;
/*   70:     */     case 10: 
/*   71: 123 */       return strategy.ARRAY_BYTESTRING_SCHEMA;
/*   72:     */     case 11: 
/*   73: 125 */       return strategy.ARRAY_BYTEARRAY_SCHEMA;
/*   74:     */     case 12: 
/*   75: 127 */       return strategy.ARRAY_BIGDECIMAL_SCHEMA;
/*   76:     */     case 13: 
/*   77: 129 */       return strategy.ARRAY_BIGINTEGER_SCHEMA;
/*   78:     */     case 14: 
/*   79: 131 */       return strategy.ARRAY_DATE_SCHEMA;
/*   80:     */     }
/*   81: 133 */     throw new RuntimeException("Should not happen.");
/*   82:     */   }
/*   83:     */   
/*   84:     */   static Base getGenericElementSchema(int id, IdStrategy strategy)
/*   85:     */   {
/*   86: 139 */     switch (id)
/*   87:     */     {
/*   88:     */     case 1: 
/*   89: 142 */       return strategy.ARRAY_BOOL_DERIVED_SCHEMA;
/*   90:     */     case 3: 
/*   91: 144 */       return strategy.ARRAY_CHAR_DERIVED_SCHEMA;
/*   92:     */     case 4: 
/*   93: 146 */       return strategy.ARRAY_SHORT_DERIVED_SCHEMA;
/*   94:     */     case 5: 
/*   95: 148 */       return strategy.ARRAY_INT32_DERIVED_SCHEMA;
/*   96:     */     case 6: 
/*   97: 150 */       return strategy.ARRAY_INT64_DERIVED_SCHEMA;
/*   98:     */     case 7: 
/*   99: 152 */       return strategy.ARRAY_FLOAT_DERIVED_SCHEMA;
/*  100:     */     case 8: 
/*  101: 154 */       return strategy.ARRAY_DOUBLE_DERIVED_SCHEMA;
/*  102:     */     case 9: 
/*  103: 156 */       return strategy.ARRAY_STRING_SCHEMA;
/*  104:     */     case 10: 
/*  105: 158 */       return strategy.ARRAY_BYTESTRING_SCHEMA;
/*  106:     */     case 11: 
/*  107: 160 */       return strategy.ARRAY_BYTEARRAY_SCHEMA;
/*  108:     */     case 12: 
/*  109: 162 */       return strategy.ARRAY_BIGDECIMAL_SCHEMA;
/*  110:     */     case 13: 
/*  111: 164 */       return strategy.ARRAY_BIGINTEGER_SCHEMA;
/*  112:     */     case 14: 
/*  113: 166 */       return strategy.ARRAY_DATE_SCHEMA;
/*  114:     */     }
/*  115: 168 */     throw new RuntimeException("Should not happen.");
/*  116:     */   }
/*  117:     */   
/*  118:     */   static Base newSchema(int id, Class<?> compontentType, Class<?> typeClass, IdStrategy strategy, PolymorphicSchema.Handler handler)
/*  119:     */   {
/*  120: 175 */     switch (id)
/*  121:     */     {
/*  122:     */     case 1: 
/*  123: 178 */       return new BoolArray(strategy, handler, compontentType.isPrimitive());
/*  124:     */     case 3: 
/*  125: 180 */       return new CharArray(strategy, handler, compontentType.isPrimitive());
/*  126:     */     case 4: 
/*  127: 182 */       return new ShortArray(strategy, handler, compontentType.isPrimitive());
/*  128:     */     case 5: 
/*  129: 184 */       return new Int32Array(strategy, handler, compontentType.isPrimitive());
/*  130:     */     case 6: 
/*  131: 186 */       return new Int64Array(strategy, handler, compontentType.isPrimitive());
/*  132:     */     case 7: 
/*  133: 188 */       return new FloatArray(strategy, handler, compontentType.isPrimitive());
/*  134:     */     case 8: 
/*  135: 190 */       return new DoubleArray(strategy, handler, compontentType.isPrimitive());
/*  136:     */     case 9: 
/*  137: 192 */       return new StringArray(strategy, handler);
/*  138:     */     case 10: 
/*  139: 194 */       return new ByteStringArray(strategy, handler);
/*  140:     */     case 11: 
/*  141: 196 */       return new ByteArrayArray(strategy, handler);
/*  142:     */     case 12: 
/*  143: 198 */       return new BigDecimalArray(strategy, handler);
/*  144:     */     case 13: 
/*  145: 200 */       return new BigIntegerArray(strategy, handler);
/*  146:     */     case 14: 
/*  147: 202 */       return new DateArray(strategy, handler);
/*  148:     */     }
/*  149: 204 */     throw new RuntimeException("Should not happen.");
/*  150:     */   }
/*  151:     */   
/*  152:     */   static String name(int number)
/*  153:     */   {
/*  154: 210 */     switch (number)
/*  155:     */     {
/*  156:     */     case 1: 
/*  157: 213 */       return "a";
/*  158:     */     case 2: 
/*  159: 215 */       return "b";
/*  160:     */     case 3: 
/*  161: 217 */       return "c";
/*  162:     */     }
/*  163: 219 */     return null;
/*  164:     */   }
/*  165:     */   
/*  166:     */   static int number(String name)
/*  167:     */   {
/*  168: 225 */     if (name.length() != 1) {
/*  169: 226 */       return 0;
/*  170:     */     }
/*  171: 228 */     switch (name.charAt(0))
/*  172:     */     {
/*  173:     */     case 'a': 
/*  174: 231 */       return 1;
/*  175:     */     case 'b': 
/*  176: 233 */       return 2;
/*  177:     */     case 'c': 
/*  178: 235 */       return 3;
/*  179:     */     }
/*  180: 237 */     return 0;
/*  181:     */   }
/*  182:     */   
/*  183:     */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy, Delegate<?> delegate)
/*  184:     */     throws IOException
/*  185:     */   {
/*  186: 245 */     if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/*  187: 246 */       throw new ProtostuffException("Corrupt input.");
/*  188:     */     }
/*  189: 248 */     int len = input.readInt32();
/*  190:     */     
/*  191: 250 */     output.writeInt32(1, len, false);
/*  192: 254 */     if (len < 0) {
/*  193: 255 */       len = -len - 1;
/*  194:     */     }
/*  195: 257 */     int i = 0;
/*  196: 257 */     for (int nullCount = 0; i < len;) {
/*  197: 259 */       switch (input.readFieldNumber(pipeSchema.wrappedSchema))
/*  198:     */       {
/*  199:     */       case 2: 
/*  200: 262 */         i++;
/*  201: 263 */         delegate.transfer(pipe, input, output, 2, true);
/*  202: 264 */         break;
/*  203:     */       case 3: 
/*  204: 266 */         nullCount = input.readUInt32();
/*  205: 267 */         i += nullCount;
/*  206: 268 */         output.writeUInt32(3, nullCount, false);
/*  207: 269 */         break;
/*  208:     */       default: 
/*  209: 271 */         throw new ProtostuffException("Corrupt input.");
/*  210:     */       }
/*  211:     */     }
/*  212: 275 */     if (0 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/*  213: 276 */       throw new ProtostuffException("Corrupt input.");
/*  214:     */     }
/*  215:     */   }
/*  216:     */   
/*  217:     */   public static abstract class Base
/*  218:     */     extends PolymorphicSchema
/*  219:     */   {
/*  220:     */     protected final PolymorphicSchema.Handler handler;
/*  221:     */     protected final boolean allowNullArrayElement;
/*  222:     */     
/*  223:     */     public Base(IdStrategy strategy, PolymorphicSchema.Handler handler)
/*  224:     */     {
/*  225: 286 */       super();
/*  226:     */       
/*  227: 288 */       this.handler = handler;
/*  228:     */       
/*  229: 290 */       this.allowNullArrayElement = (0 != (0x4 & strategy.flags));
/*  230:     */     }
/*  231:     */     
/*  232:     */     public String getFieldName(int number)
/*  233:     */     {
/*  234: 296 */       return ArraySchemas.name(number);
/*  235:     */     }
/*  236:     */     
/*  237:     */     public int getFieldNumber(String name)
/*  238:     */     {
/*  239: 302 */       return ArraySchemas.number(name);
/*  240:     */     }
/*  241:     */     
/*  242:     */     public String messageFullName()
/*  243:     */     {
/*  244: 308 */       return Array.class.getName();
/*  245:     */     }
/*  246:     */     
/*  247:     */     public String messageName()
/*  248:     */     {
/*  249: 314 */       return Array.class.getSimpleName();
/*  250:     */     }
/*  251:     */     
/*  252:     */     protected void setValue(Object value, Object owner)
/*  253:     */     {
/*  254: 320 */       this.handler.setValue(value, owner);
/*  255:     */     }
/*  256:     */     
/*  257:     */     public void mergeFrom(Input input, Object owner)
/*  258:     */       throws IOException
/*  259:     */     {
/*  260: 326 */       setValue(readFrom(input, owner), owner);
/*  261:     */     }
/*  262:     */     
/*  263:     */     protected abstract Object readFrom(Input paramInput, Object paramObject)
/*  264:     */       throws IOException;
/*  265:     */   }
/*  266:     */   
/*  267:     */   public static class BoolArray
/*  268:     */     extends ArraySchemas.Base
/*  269:     */   {
/*  270: 335 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  271:     */     {
/*  272:     */       protected void transfer(Pipe pipe, Input input, Output output)
/*  273:     */         throws IOException
/*  274:     */       {
/*  275: 342 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.BoolArray.this.strategy, RuntimeFieldFactory.BOOL);
/*  276:     */       }
/*  277:     */     };
/*  278:     */     final boolean primitive;
/*  279:     */     
/*  280:     */     BoolArray(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/*  281:     */     {
/*  282: 351 */       super(handler);
/*  283: 352 */       this.primitive = primitive;
/*  284:     */     }
/*  285:     */     
/*  286:     */     public Pipe.Schema<Object> getPipeSchema()
/*  287:     */     {
/*  288: 358 */       return this.pipeSchema;
/*  289:     */     }
/*  290:     */     
/*  291:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/*  292:     */       throws IOException
/*  293:     */     {
/*  294: 364 */       boolean[] array = new boolean[len];
/*  295: 365 */       if ((input instanceof GraphInput)) {
/*  296: 368 */         ((GraphInput)input).updateLast(array, owner);
/*  297:     */       }
/*  298: 371 */       for (int i = 0; i < len; i++)
/*  299:     */       {
/*  300: 373 */         if (2 != input.readFieldNumber(this)) {
/*  301: 374 */           throw new ProtostuffException("Corrupt input.");
/*  302:     */         }
/*  303: 376 */         array[i] = input.readBool();
/*  304:     */       }
/*  305: 379 */       if (0 != input.readFieldNumber(this)) {
/*  306: 380 */         throw new ProtostuffException("Corrupt input.");
/*  307:     */       }
/*  308: 382 */       return array;
/*  309:     */     }
/*  310:     */     
/*  311:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/*  312:     */       throws IOException
/*  313:     */     {
/*  314: 388 */       Boolean[] array = new Boolean[len];
/*  315: 389 */       if ((input instanceof GraphInput)) {
/*  316: 392 */         ((GraphInput)input).updateLast(array, owner);
/*  317:     */       }
/*  318: 395 */       for (int i = 0; i < len;) {
/*  319: 397 */         switch (input.readFieldNumber(this))
/*  320:     */         {
/*  321:     */         case 2: 
/*  322: 400 */           array[(i++)] = Boolean.valueOf(input.readBool());
/*  323: 401 */           break;
/*  324:     */         case 3: 
/*  325: 403 */           i += input.readUInt32();
/*  326: 404 */           break;
/*  327:     */         default: 
/*  328: 406 */           throw new ProtostuffException("Corrupt input.");
/*  329:     */         }
/*  330:     */       }
/*  331: 410 */       if (0 != input.readFieldNumber(this)) {
/*  332: 411 */         throw new ProtostuffException("Corrupt input.");
/*  333:     */       }
/*  334: 413 */       return array;
/*  335:     */     }
/*  336:     */     
/*  337:     */     public Object readFrom(Input input, Object owner)
/*  338:     */       throws IOException
/*  339:     */     {
/*  340: 419 */       if (1 != input.readFieldNumber(this)) {
/*  341: 420 */         throw new ProtostuffException("Corrupt input.");
/*  342:     */       }
/*  343: 422 */       int len = input.readInt32();
/*  344: 423 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/*  345: 424 */         readBoxedFrom(input, owner, len);
/*  346:     */     }
/*  347:     */     
/*  348:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  349:     */       throws IOException
/*  350:     */     {
/*  351: 430 */       output.writeInt32(1, len, false);
/*  352:     */     }
/*  353:     */     
/*  354:     */     protected void writeTo(Output output, Object value, boolean primitive)
/*  355:     */       throws IOException
/*  356:     */     {
/*  357: 436 */       if (primitive)
/*  358:     */       {
/*  359: 438 */         boolean[] array = (boolean[])value;
/*  360: 439 */         writeLengthTo(output, array.length, true);
/*  361:     */         
/*  362: 441 */         int i = 0;
/*  363: 441 */         for (int len = array.length; i < len; i++) {
/*  364: 442 */           output.writeBool(2, array[i], true);
/*  365:     */         }
/*  366: 444 */         return;
/*  367:     */       }
/*  368: 447 */       Boolean[] array = (Boolean[])value;
/*  369: 448 */       writeLengthTo(output, array.length, false);
/*  370:     */       
/*  371: 450 */       int nullCount = 0;
/*  372: 451 */       int i = 0;
/*  373: 451 */       for (int len = array.length; i < len; i++)
/*  374:     */       {
/*  375: 453 */         Boolean v = array[i];
/*  376: 454 */         if (v != null)
/*  377:     */         {
/*  378: 456 */           if (nullCount != 0)
/*  379:     */           {
/*  380: 458 */             output.writeUInt32(3, nullCount, false);
/*  381: 459 */             nullCount = 0;
/*  382:     */           }
/*  383: 462 */           output.writeBool(2, v.booleanValue(), true);
/*  384:     */         }
/*  385: 464 */         else if (this.allowNullArrayElement)
/*  386:     */         {
/*  387: 466 */           nullCount++;
/*  388:     */         }
/*  389:     */       }
/*  390: 471 */       if (nullCount != 0) {
/*  391: 472 */         output.writeUInt32(3, nullCount, false);
/*  392:     */       }
/*  393:     */     }
/*  394:     */     
/*  395:     */     public void writeTo(Output output, Object value)
/*  396:     */       throws IOException
/*  397:     */     {
/*  398: 478 */       writeTo(output, value, this.primitive);
/*  399:     */     }
/*  400:     */   }
/*  401:     */   
/*  402:     */   public static class CharArray
/*  403:     */     extends ArraySchemas.Base
/*  404:     */   {
/*  405: 484 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  406:     */     {
/*  407:     */       protected void transfer(Pipe pipe, Input input, Output output)
/*  408:     */         throws IOException
/*  409:     */       {
/*  410: 491 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.CharArray.this.strategy, RuntimeFieldFactory.CHAR);
/*  411:     */       }
/*  412:     */     };
/*  413:     */     final boolean primitive;
/*  414:     */     
/*  415:     */     CharArray(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/*  416:     */     {
/*  417: 500 */       super(handler);
/*  418: 501 */       this.primitive = primitive;
/*  419:     */     }
/*  420:     */     
/*  421:     */     public Pipe.Schema<Object> getPipeSchema()
/*  422:     */     {
/*  423: 507 */       return this.pipeSchema;
/*  424:     */     }
/*  425:     */     
/*  426:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/*  427:     */       throws IOException
/*  428:     */     {
/*  429: 513 */       char[] array = new char[len];
/*  430: 514 */       if ((input instanceof GraphInput)) {
/*  431: 517 */         ((GraphInput)input).updateLast(array, owner);
/*  432:     */       }
/*  433: 520 */       for (int i = 0; i < len; i++)
/*  434:     */       {
/*  435: 522 */         if (2 != input.readFieldNumber(this)) {
/*  436: 523 */           throw new ProtostuffException("Corrupt input.");
/*  437:     */         }
/*  438: 525 */         array[i] = ((char)input.readUInt32());
/*  439:     */       }
/*  440: 528 */       if (0 != input.readFieldNumber(this)) {
/*  441: 529 */         throw new ProtostuffException("Corrupt input.");
/*  442:     */       }
/*  443: 531 */       return array;
/*  444:     */     }
/*  445:     */     
/*  446:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/*  447:     */       throws IOException
/*  448:     */     {
/*  449: 537 */       Character[] array = new Character[len];
/*  450: 538 */       if ((input instanceof GraphInput)) {
/*  451: 541 */         ((GraphInput)input).updateLast(array, owner);
/*  452:     */       }
/*  453: 544 */       for (int i = 0; i < len;) {
/*  454: 546 */         switch (input.readFieldNumber(this))
/*  455:     */         {
/*  456:     */         case 2: 
/*  457: 549 */           array[(i++)] = Character.valueOf((char)input.readUInt32());
/*  458: 550 */           break;
/*  459:     */         case 3: 
/*  460: 552 */           i += input.readUInt32();
/*  461: 553 */           break;
/*  462:     */         default: 
/*  463: 555 */           throw new ProtostuffException("Corrupt input.");
/*  464:     */         }
/*  465:     */       }
/*  466: 559 */       if (0 != input.readFieldNumber(this)) {
/*  467: 560 */         throw new ProtostuffException("Corrupt input.");
/*  468:     */       }
/*  469: 562 */       return array;
/*  470:     */     }
/*  471:     */     
/*  472:     */     public Object readFrom(Input input, Object owner)
/*  473:     */       throws IOException
/*  474:     */     {
/*  475: 568 */       if (1 != input.readFieldNumber(this)) {
/*  476: 569 */         throw new ProtostuffException("Corrupt input.");
/*  477:     */       }
/*  478: 571 */       int len = input.readInt32();
/*  479: 572 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/*  480: 573 */         readBoxedFrom(input, owner, len);
/*  481:     */     }
/*  482:     */     
/*  483:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  484:     */       throws IOException
/*  485:     */     {
/*  486: 579 */       output.writeInt32(1, len, false);
/*  487:     */     }
/*  488:     */     
/*  489:     */     protected void writeTo(Output output, Object value, boolean primitive)
/*  490:     */       throws IOException
/*  491:     */     {
/*  492: 585 */       if (primitive)
/*  493:     */       {
/*  494: 587 */         char[] array = (char[])value;
/*  495: 588 */         writeLengthTo(output, array.length, true);
/*  496:     */         
/*  497: 590 */         int i = 0;
/*  498: 590 */         for (int len = array.length; i < len; i++) {
/*  499: 591 */           output.writeUInt32(2, array[i], true);
/*  500:     */         }
/*  501: 593 */         return;
/*  502:     */       }
/*  503: 596 */       Character[] array = (Character[])value;
/*  504: 597 */       writeLengthTo(output, array.length, false);
/*  505:     */       
/*  506: 599 */       int nullCount = 0;
/*  507: 600 */       int i = 0;
/*  508: 600 */       for (int len = array.length; i < len; i++)
/*  509:     */       {
/*  510: 602 */         Character v = array[i];
/*  511: 603 */         if (v != null)
/*  512:     */         {
/*  513: 605 */           if (nullCount != 0)
/*  514:     */           {
/*  515: 607 */             output.writeUInt32(3, nullCount, false);
/*  516: 608 */             nullCount = 0;
/*  517:     */           }
/*  518: 611 */           output.writeUInt32(2, v.charValue(), true);
/*  519:     */         }
/*  520: 613 */         else if (this.allowNullArrayElement)
/*  521:     */         {
/*  522: 615 */           nullCount++;
/*  523:     */         }
/*  524:     */       }
/*  525: 620 */       if (nullCount != 0) {
/*  526: 621 */         output.writeUInt32(3, nullCount, false);
/*  527:     */       }
/*  528:     */     }
/*  529:     */     
/*  530:     */     public void writeTo(Output output, Object value)
/*  531:     */       throws IOException
/*  532:     */     {
/*  533: 627 */       writeTo(output, value, this.primitive);
/*  534:     */     }
/*  535:     */   }
/*  536:     */   
/*  537:     */   public static class ShortArray
/*  538:     */     extends ArraySchemas.Base
/*  539:     */   {
/*  540: 633 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  541:     */     {
/*  542:     */       protected void transfer(Pipe pipe, Input input, Output output)
/*  543:     */         throws IOException
/*  544:     */       {
/*  545: 640 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.ShortArray.this.strategy, RuntimeFieldFactory.SHORT);
/*  546:     */       }
/*  547:     */     };
/*  548:     */     final boolean primitive;
/*  549:     */     
/*  550:     */     ShortArray(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/*  551:     */     {
/*  552: 649 */       super(handler);
/*  553: 650 */       this.primitive = primitive;
/*  554:     */     }
/*  555:     */     
/*  556:     */     public Pipe.Schema<Object> getPipeSchema()
/*  557:     */     {
/*  558: 656 */       return this.pipeSchema;
/*  559:     */     }
/*  560:     */     
/*  561:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/*  562:     */       throws IOException
/*  563:     */     {
/*  564: 662 */       short[] array = new short[len];
/*  565: 663 */       if ((input instanceof GraphInput)) {
/*  566: 666 */         ((GraphInput)input).updateLast(array, owner);
/*  567:     */       }
/*  568: 669 */       for (int i = 0; i < len; i++)
/*  569:     */       {
/*  570: 671 */         if (2 != input.readFieldNumber(this)) {
/*  571: 672 */           throw new ProtostuffException("Corrupt input.");
/*  572:     */         }
/*  573: 674 */         array[i] = ((short)input.readUInt32());
/*  574:     */       }
/*  575: 677 */       if (0 != input.readFieldNumber(this)) {
/*  576: 678 */         throw new ProtostuffException("Corrupt input.");
/*  577:     */       }
/*  578: 680 */       return array;
/*  579:     */     }
/*  580:     */     
/*  581:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/*  582:     */       throws IOException
/*  583:     */     {
/*  584: 686 */       Short[] array = new Short[len];
/*  585: 687 */       if ((input instanceof GraphInput)) {
/*  586: 690 */         ((GraphInput)input).updateLast(array, owner);
/*  587:     */       }
/*  588: 693 */       for (int i = 0; i < len;) {
/*  589: 695 */         switch (input.readFieldNumber(this))
/*  590:     */         {
/*  591:     */         case 2: 
/*  592: 698 */           array[(i++)] = Short.valueOf((short)input.readUInt32());
/*  593: 699 */           break;
/*  594:     */         case 3: 
/*  595: 701 */           i += input.readUInt32();
/*  596: 702 */           break;
/*  597:     */         default: 
/*  598: 704 */           throw new ProtostuffException("Corrupt input.");
/*  599:     */         }
/*  600:     */       }
/*  601: 708 */       if (0 != input.readFieldNumber(this)) {
/*  602: 709 */         throw new ProtostuffException("Corrupt input.");
/*  603:     */       }
/*  604: 711 */       return array;
/*  605:     */     }
/*  606:     */     
/*  607:     */     public Object readFrom(Input input, Object owner)
/*  608:     */       throws IOException
/*  609:     */     {
/*  610: 717 */       if (1 != input.readFieldNumber(this)) {
/*  611: 718 */         throw new ProtostuffException("Corrupt input.");
/*  612:     */       }
/*  613: 720 */       int len = input.readInt32();
/*  614: 721 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/*  615: 722 */         readBoxedFrom(input, owner, len);
/*  616:     */     }
/*  617:     */     
/*  618:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  619:     */       throws IOException
/*  620:     */     {
/*  621: 728 */       output.writeInt32(1, len, false);
/*  622:     */     }
/*  623:     */     
/*  624:     */     protected void writeTo(Output output, Object value, boolean primitive)
/*  625:     */       throws IOException
/*  626:     */     {
/*  627: 734 */       if (primitive)
/*  628:     */       {
/*  629: 736 */         short[] array = (short[])value;
/*  630: 737 */         writeLengthTo(output, array.length, true);
/*  631:     */         
/*  632: 739 */         int i = 0;
/*  633: 739 */         for (int len = array.length; i < len; i++) {
/*  634: 740 */           output.writeUInt32(2, array[i], true);
/*  635:     */         }
/*  636: 742 */         return;
/*  637:     */       }
/*  638: 745 */       Short[] array = (Short[])value;
/*  639: 746 */       writeLengthTo(output, array.length, false);
/*  640:     */       
/*  641: 748 */       int nullCount = 0;
/*  642: 749 */       int i = 0;
/*  643: 749 */       for (int len = array.length; i < len; i++)
/*  644:     */       {
/*  645: 751 */         Short v = array[i];
/*  646: 752 */         if (v != null)
/*  647:     */         {
/*  648: 754 */           if (nullCount != 0)
/*  649:     */           {
/*  650: 756 */             output.writeUInt32(3, nullCount, false);
/*  651: 757 */             nullCount = 0;
/*  652:     */           }
/*  653: 760 */           output.writeUInt32(2, v.shortValue(), true);
/*  654:     */         }
/*  655: 762 */         else if (this.allowNullArrayElement)
/*  656:     */         {
/*  657: 764 */           nullCount++;
/*  658:     */         }
/*  659:     */       }
/*  660: 769 */       if (nullCount != 0) {
/*  661: 770 */         output.writeUInt32(3, nullCount, false);
/*  662:     */       }
/*  663:     */     }
/*  664:     */     
/*  665:     */     public void writeTo(Output output, Object value)
/*  666:     */       throws IOException
/*  667:     */     {
/*  668: 776 */       writeTo(output, value, this.primitive);
/*  669:     */     }
/*  670:     */   }
/*  671:     */   
/*  672:     */   public static class Int32Array
/*  673:     */     extends ArraySchemas.Base
/*  674:     */   {
/*  675: 782 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  676:     */     {
/*  677:     */       protected void transfer(Pipe pipe, Input input, Output output)
/*  678:     */         throws IOException
/*  679:     */       {
/*  680: 789 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.Int32Array.this.strategy, RuntimeFieldFactory.INT32);
/*  681:     */       }
/*  682:     */     };
/*  683:     */     final boolean primitive;
/*  684:     */     
/*  685:     */     Int32Array(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/*  686:     */     {
/*  687: 798 */       super(handler);
/*  688: 799 */       this.primitive = primitive;
/*  689:     */     }
/*  690:     */     
/*  691:     */     public Pipe.Schema<Object> getPipeSchema()
/*  692:     */     {
/*  693: 805 */       return this.pipeSchema;
/*  694:     */     }
/*  695:     */     
/*  696:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/*  697:     */       throws IOException
/*  698:     */     {
/*  699: 811 */       int[] array = new int[len];
/*  700: 812 */       if ((input instanceof GraphInput)) {
/*  701: 815 */         ((GraphInput)input).updateLast(array, owner);
/*  702:     */       }
/*  703: 818 */       for (int i = 0; i < len; i++)
/*  704:     */       {
/*  705: 820 */         if (2 != input.readFieldNumber(this)) {
/*  706: 821 */           throw new ProtostuffException("Corrupt input.");
/*  707:     */         }
/*  708: 823 */         array[i] = input.readInt32();
/*  709:     */       }
/*  710: 826 */       if (0 != input.readFieldNumber(this)) {
/*  711: 827 */         throw new ProtostuffException("Corrupt input.");
/*  712:     */       }
/*  713: 829 */       return array;
/*  714:     */     }
/*  715:     */     
/*  716:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/*  717:     */       throws IOException
/*  718:     */     {
/*  719: 835 */       Integer[] array = new Integer[len];
/*  720: 836 */       if ((input instanceof GraphInput)) {
/*  721: 839 */         ((GraphInput)input).updateLast(array, owner);
/*  722:     */       }
/*  723: 842 */       for (int i = 0; i < len;) {
/*  724: 844 */         switch (input.readFieldNumber(this))
/*  725:     */         {
/*  726:     */         case 2: 
/*  727: 847 */           array[(i++)] = Integer.valueOf(input.readInt32());
/*  728: 848 */           break;
/*  729:     */         case 3: 
/*  730: 850 */           i += input.readUInt32();
/*  731: 851 */           break;
/*  732:     */         default: 
/*  733: 853 */           throw new ProtostuffException("Corrupt input.");
/*  734:     */         }
/*  735:     */       }
/*  736: 857 */       if (0 != input.readFieldNumber(this)) {
/*  737: 858 */         throw new ProtostuffException("Corrupt input.");
/*  738:     */       }
/*  739: 860 */       return array;
/*  740:     */     }
/*  741:     */     
/*  742:     */     public Object readFrom(Input input, Object owner)
/*  743:     */       throws IOException
/*  744:     */     {
/*  745: 866 */       if (1 != input.readFieldNumber(this)) {
/*  746: 867 */         throw new ProtostuffException("Corrupt input.");
/*  747:     */       }
/*  748: 869 */       int len = input.readInt32();
/*  749: 870 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/*  750: 871 */         readBoxedFrom(input, owner, len);
/*  751:     */     }
/*  752:     */     
/*  753:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  754:     */       throws IOException
/*  755:     */     {
/*  756: 877 */       output.writeInt32(1, len, false);
/*  757:     */     }
/*  758:     */     
/*  759:     */     protected void writeTo(Output output, Object value, boolean primitive)
/*  760:     */       throws IOException
/*  761:     */     {
/*  762: 882 */       if (primitive)
/*  763:     */       {
/*  764: 884 */         int[] array = (int[])value;
/*  765: 885 */         writeLengthTo(output, array.length, true);
/*  766:     */         
/*  767: 887 */         int i = 0;
/*  768: 887 */         for (int len = array.length; i < len; i++) {
/*  769: 888 */           output.writeInt32(2, array[i], true);
/*  770:     */         }
/*  771: 890 */         return;
/*  772:     */       }
/*  773: 893 */       Integer[] array = (Integer[])value;
/*  774: 894 */       writeLengthTo(output, array.length, false);
/*  775:     */       
/*  776: 896 */       int nullCount = 0;
/*  777: 897 */       int i = 0;
/*  778: 897 */       for (int len = array.length; i < len; i++)
/*  779:     */       {
/*  780: 899 */         Integer v = array[i];
/*  781: 900 */         if (v != null)
/*  782:     */         {
/*  783: 902 */           if (nullCount != 0)
/*  784:     */           {
/*  785: 904 */             output.writeUInt32(3, nullCount, false);
/*  786: 905 */             nullCount = 0;
/*  787:     */           }
/*  788: 908 */           output.writeInt32(2, v.intValue(), true);
/*  789:     */         }
/*  790: 910 */         else if (this.allowNullArrayElement)
/*  791:     */         {
/*  792: 912 */           nullCount++;
/*  793:     */         }
/*  794:     */       }
/*  795: 917 */       if (nullCount != 0) {
/*  796: 918 */         output.writeUInt32(3, nullCount, false);
/*  797:     */       }
/*  798:     */     }
/*  799:     */     
/*  800:     */     public void writeTo(Output output, Object value)
/*  801:     */       throws IOException
/*  802:     */     {
/*  803: 924 */       writeTo(output, value, this.primitive);
/*  804:     */     }
/*  805:     */   }
/*  806:     */   
/*  807:     */   public static class Int64Array
/*  808:     */     extends ArraySchemas.Base
/*  809:     */   {
/*  810: 930 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  811:     */     {
/*  812:     */       protected void transfer(Pipe pipe, Input input, Output output)
/*  813:     */         throws IOException
/*  814:     */       {
/*  815: 937 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.Int64Array.this.strategy, RuntimeFieldFactory.INT64);
/*  816:     */       }
/*  817:     */     };
/*  818:     */     final boolean primitive;
/*  819:     */     
/*  820:     */     Int64Array(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/*  821:     */     {
/*  822: 946 */       super(handler);
/*  823: 947 */       this.primitive = primitive;
/*  824:     */     }
/*  825:     */     
/*  826:     */     public Pipe.Schema<Object> getPipeSchema()
/*  827:     */     {
/*  828: 953 */       return this.pipeSchema;
/*  829:     */     }
/*  830:     */     
/*  831:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/*  832:     */       throws IOException
/*  833:     */     {
/*  834: 959 */       long[] array = new long[len];
/*  835: 960 */       if ((input instanceof GraphInput)) {
/*  836: 963 */         ((GraphInput)input).updateLast(array, owner);
/*  837:     */       }
/*  838: 966 */       for (int i = 0; i < len; i++)
/*  839:     */       {
/*  840: 968 */         if (2 != input.readFieldNumber(this)) {
/*  841: 969 */           throw new ProtostuffException("Corrupt input.");
/*  842:     */         }
/*  843: 971 */         array[i] = input.readInt64();
/*  844:     */       }
/*  845: 974 */       if (0 != input.readFieldNumber(this)) {
/*  846: 975 */         throw new ProtostuffException("Corrupt input.");
/*  847:     */       }
/*  848: 977 */       return array;
/*  849:     */     }
/*  850:     */     
/*  851:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/*  852:     */       throws IOException
/*  853:     */     {
/*  854: 983 */       Long[] array = new Long[len];
/*  855: 984 */       if ((input instanceof GraphInput)) {
/*  856: 987 */         ((GraphInput)input).updateLast(array, owner);
/*  857:     */       }
/*  858: 990 */       for (int i = 0; i < len;) {
/*  859: 992 */         switch (input.readFieldNumber(this))
/*  860:     */         {
/*  861:     */         case 2: 
/*  862: 995 */           array[(i++)] = Long.valueOf(input.readInt64());
/*  863: 996 */           break;
/*  864:     */         case 3: 
/*  865: 998 */           i += input.readUInt32();
/*  866: 999 */           break;
/*  867:     */         default: 
/*  868:1001 */           throw new ProtostuffException("Corrupt input.");
/*  869:     */         }
/*  870:     */       }
/*  871:1005 */       if (0 != input.readFieldNumber(this)) {
/*  872:1006 */         throw new ProtostuffException("Corrupt input.");
/*  873:     */       }
/*  874:1008 */       return array;
/*  875:     */     }
/*  876:     */     
/*  877:     */     public Object readFrom(Input input, Object owner)
/*  878:     */       throws IOException
/*  879:     */     {
/*  880:1014 */       if (1 != input.readFieldNumber(this)) {
/*  881:1015 */         throw new ProtostuffException("Corrupt input.");
/*  882:     */       }
/*  883:1017 */       int len = input.readInt32();
/*  884:1018 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/*  885:1019 */         readBoxedFrom(input, owner, len);
/*  886:     */     }
/*  887:     */     
/*  888:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/*  889:     */       throws IOException
/*  890:     */     {
/*  891:1025 */       output.writeInt32(1, len, false);
/*  892:     */     }
/*  893:     */     
/*  894:     */     protected void writeTo(Output output, Object value, boolean primitive)
/*  895:     */       throws IOException
/*  896:     */     {
/*  897:1031 */       if (primitive)
/*  898:     */       {
/*  899:1033 */         long[] array = (long[])value;
/*  900:1034 */         writeLengthTo(output, array.length, true);
/*  901:     */         
/*  902:1036 */         int i = 0;
/*  903:1036 */         for (int len = array.length; i < len; i++) {
/*  904:1037 */           output.writeInt64(2, array[i], true);
/*  905:     */         }
/*  906:1039 */         return;
/*  907:     */       }
/*  908:1042 */       Long[] array = (Long[])value;
/*  909:1043 */       writeLengthTo(output, array.length, false);
/*  910:     */       
/*  911:1045 */       int nullCount = 0;
/*  912:1046 */       int i = 0;
/*  913:1046 */       for (int len = array.length; i < len; i++)
/*  914:     */       {
/*  915:1048 */         Long v = array[i];
/*  916:1049 */         if (v != null)
/*  917:     */         {
/*  918:1051 */           if (nullCount != 0)
/*  919:     */           {
/*  920:1053 */             output.writeUInt32(3, nullCount, false);
/*  921:1054 */             nullCount = 0;
/*  922:     */           }
/*  923:1057 */           output.writeInt64(2, v.longValue(), true);
/*  924:     */         }
/*  925:1059 */         else if (this.allowNullArrayElement)
/*  926:     */         {
/*  927:1061 */           nullCount++;
/*  928:     */         }
/*  929:     */       }
/*  930:1066 */       if (nullCount != 0) {
/*  931:1067 */         output.writeUInt32(3, nullCount, false);
/*  932:     */       }
/*  933:     */     }
/*  934:     */     
/*  935:     */     public void writeTo(Output output, Object value)
/*  936:     */       throws IOException
/*  937:     */     {
/*  938:1073 */       writeTo(output, value, this.primitive);
/*  939:     */     }
/*  940:     */   }
/*  941:     */   
/*  942:     */   public static class FloatArray
/*  943:     */     extends ArraySchemas.Base
/*  944:     */   {
/*  945:1079 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  946:     */     {
/*  947:     */       protected void transfer(Pipe pipe, Input input, Output output)
/*  948:     */         throws IOException
/*  949:     */       {
/*  950:1086 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.FloatArray.this.strategy, RuntimeFieldFactory.FLOAT);
/*  951:     */       }
/*  952:     */     };
/*  953:     */     final boolean primitive;
/*  954:     */     
/*  955:     */     FloatArray(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/*  956:     */     {
/*  957:1095 */       super(handler);
/*  958:1096 */       this.primitive = primitive;
/*  959:     */     }
/*  960:     */     
/*  961:     */     public Pipe.Schema<Object> getPipeSchema()
/*  962:     */     {
/*  963:1102 */       return this.pipeSchema;
/*  964:     */     }
/*  965:     */     
/*  966:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/*  967:     */       throws IOException
/*  968:     */     {
/*  969:1108 */       float[] array = new float[len];
/*  970:1109 */       if ((input instanceof GraphInput)) {
/*  971:1112 */         ((GraphInput)input).updateLast(array, owner);
/*  972:     */       }
/*  973:1115 */       for (int i = 0; i < len; i++)
/*  974:     */       {
/*  975:1117 */         if (2 != input.readFieldNumber(this)) {
/*  976:1118 */           throw new ProtostuffException("Corrupt input.");
/*  977:     */         }
/*  978:1120 */         array[i] = input.readFloat();
/*  979:     */       }
/*  980:1123 */       if (0 != input.readFieldNumber(this)) {
/*  981:1124 */         throw new ProtostuffException("Corrupt input.");
/*  982:     */       }
/*  983:1126 */       return array;
/*  984:     */     }
/*  985:     */     
/*  986:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/*  987:     */       throws IOException
/*  988:     */     {
/*  989:1132 */       Float[] array = new Float[len];
/*  990:1133 */       if ((input instanceof GraphInput)) {
/*  991:1136 */         ((GraphInput)input).updateLast(array, owner);
/*  992:     */       }
/*  993:1139 */       for (int i = 0; i < len;) {
/*  994:1141 */         switch (input.readFieldNumber(this))
/*  995:     */         {
/*  996:     */         case 2: 
/*  997:1144 */           array[(i++)] = Float.valueOf(input.readFloat());
/*  998:1145 */           break;
/*  999:     */         case 3: 
/* 1000:1147 */           i += input.readUInt32();
/* 1001:1148 */           break;
/* 1002:     */         default: 
/* 1003:1150 */           throw new ProtostuffException("Corrupt input.");
/* 1004:     */         }
/* 1005:     */       }
/* 1006:1154 */       if (0 != input.readFieldNumber(this)) {
/* 1007:1155 */         throw new ProtostuffException("Corrupt input.");
/* 1008:     */       }
/* 1009:1157 */       return array;
/* 1010:     */     }
/* 1011:     */     
/* 1012:     */     public Object readFrom(Input input, Object owner)
/* 1013:     */       throws IOException
/* 1014:     */     {
/* 1015:1163 */       if (1 != input.readFieldNumber(this)) {
/* 1016:1164 */         throw new ProtostuffException("Corrupt input.");
/* 1017:     */       }
/* 1018:1166 */       int len = input.readInt32();
/* 1019:1167 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/* 1020:1168 */         readBoxedFrom(input, owner, len);
/* 1021:     */     }
/* 1022:     */     
/* 1023:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1024:     */       throws IOException
/* 1025:     */     {
/* 1026:1174 */       output.writeInt32(1, len, false);
/* 1027:     */     }
/* 1028:     */     
/* 1029:     */     protected void writeTo(Output output, Object value, boolean primitive)
/* 1030:     */       throws IOException
/* 1031:     */     {
/* 1032:1180 */       if (primitive)
/* 1033:     */       {
/* 1034:1182 */         float[] array = (float[])value;
/* 1035:1183 */         writeLengthTo(output, array.length, true);
/* 1036:     */         
/* 1037:1185 */         int i = 0;
/* 1038:1185 */         for (int len = array.length; i < len; i++) {
/* 1039:1186 */           output.writeFloat(2, array[i], true);
/* 1040:     */         }
/* 1041:1188 */         return;
/* 1042:     */       }
/* 1043:1191 */       Float[] array = (Float[])value;
/* 1044:1192 */       writeLengthTo(output, array.length, false);
/* 1045:     */       
/* 1046:1194 */       int nullCount = 0;
/* 1047:1195 */       int i = 0;
/* 1048:1195 */       for (int len = array.length; i < len; i++)
/* 1049:     */       {
/* 1050:1197 */         Float v = array[i];
/* 1051:1198 */         if (v != null)
/* 1052:     */         {
/* 1053:1200 */           if (nullCount != 0)
/* 1054:     */           {
/* 1055:1202 */             output.writeUInt32(3, nullCount, false);
/* 1056:1203 */             nullCount = 0;
/* 1057:     */           }
/* 1058:1206 */           output.writeFloat(2, v.floatValue(), true);
/* 1059:     */         }
/* 1060:1208 */         else if (this.allowNullArrayElement)
/* 1061:     */         {
/* 1062:1210 */           nullCount++;
/* 1063:     */         }
/* 1064:     */       }
/* 1065:1215 */       if (nullCount != 0) {
/* 1066:1216 */         output.writeUInt32(3, nullCount, false);
/* 1067:     */       }
/* 1068:     */     }
/* 1069:     */     
/* 1070:     */     public void writeTo(Output output, Object value)
/* 1071:     */       throws IOException
/* 1072:     */     {
/* 1073:1222 */       writeTo(output, value, this.primitive);
/* 1074:     */     }
/* 1075:     */   }
/* 1076:     */   
/* 1077:     */   public static class DoubleArray
/* 1078:     */     extends ArraySchemas.Base
/* 1079:     */   {
/* 1080:1228 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1081:     */     {
/* 1082:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1083:     */         throws IOException
/* 1084:     */       {
/* 1085:1235 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.DoubleArray.this.strategy, RuntimeFieldFactory.DOUBLE);
/* 1086:     */       }
/* 1087:     */     };
/* 1088:     */     final boolean primitive;
/* 1089:     */     
/* 1090:     */     DoubleArray(IdStrategy strategy, PolymorphicSchema.Handler handler, boolean primitive)
/* 1091:     */     {
/* 1092:1244 */       super(handler);
/* 1093:1245 */       this.primitive = primitive;
/* 1094:     */     }
/* 1095:     */     
/* 1096:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1097:     */     {
/* 1098:1251 */       return this.pipeSchema;
/* 1099:     */     }
/* 1100:     */     
/* 1101:     */     protected Object readPrimitiveFrom(Input input, Object owner, int len)
/* 1102:     */       throws IOException
/* 1103:     */     {
/* 1104:1257 */       double[] array = new double[len];
/* 1105:1258 */       if ((input instanceof GraphInput)) {
/* 1106:1261 */         ((GraphInput)input).updateLast(array, owner);
/* 1107:     */       }
/* 1108:1264 */       for (int i = 0; i < len; i++)
/* 1109:     */       {
/* 1110:1266 */         if (2 != input.readFieldNumber(this)) {
/* 1111:1267 */           throw new ProtostuffException("Corrupt input.");
/* 1112:     */         }
/* 1113:1269 */         array[i] = input.readDouble();
/* 1114:     */       }
/* 1115:1272 */       if (0 != input.readFieldNumber(this)) {
/* 1116:1273 */         throw new ProtostuffException("Corrupt input.");
/* 1117:     */       }
/* 1118:1275 */       return array;
/* 1119:     */     }
/* 1120:     */     
/* 1121:     */     protected Object readBoxedFrom(Input input, Object owner, int len)
/* 1122:     */       throws IOException
/* 1123:     */     {
/* 1124:1281 */       Double[] array = new Double[len];
/* 1125:1282 */       if ((input instanceof GraphInput)) {
/* 1126:1285 */         ((GraphInput)input).updateLast(array, owner);
/* 1127:     */       }
/* 1128:1288 */       for (int i = 0; i < len;) {
/* 1129:1290 */         switch (input.readFieldNumber(this))
/* 1130:     */         {
/* 1131:     */         case 2: 
/* 1132:1293 */           array[(i++)] = Double.valueOf(input.readDouble());
/* 1133:1294 */           break;
/* 1134:     */         case 3: 
/* 1135:1296 */           i += input.readUInt32();
/* 1136:1297 */           break;
/* 1137:     */         default: 
/* 1138:1299 */           throw new ProtostuffException("Corrupt input.");
/* 1139:     */         }
/* 1140:     */       }
/* 1141:1303 */       if (0 != input.readFieldNumber(this)) {
/* 1142:1304 */         throw new ProtostuffException("Corrupt input.");
/* 1143:     */       }
/* 1144:1306 */       return array;
/* 1145:     */     }
/* 1146:     */     
/* 1147:     */     public Object readFrom(Input input, Object owner)
/* 1148:     */       throws IOException
/* 1149:     */     {
/* 1150:1312 */       if (1 != input.readFieldNumber(this)) {
/* 1151:1313 */         throw new ProtostuffException("Corrupt input.");
/* 1152:     */       }
/* 1153:1315 */       int len = input.readInt32();
/* 1154:1316 */       return this.primitive ? readPrimitiveFrom(input, owner, len) : 
/* 1155:1317 */         readBoxedFrom(input, owner, len);
/* 1156:     */     }
/* 1157:     */     
/* 1158:     */     protected void writeLengthTo(Output output, int len, boolean primitive)
/* 1159:     */       throws IOException
/* 1160:     */     {
/* 1161:1323 */       output.writeInt32(1, len, false);
/* 1162:     */     }
/* 1163:     */     
/* 1164:     */     protected void writeTo(Output output, Object value, boolean primitive)
/* 1165:     */       throws IOException
/* 1166:     */     {
/* 1167:1329 */       if (primitive)
/* 1168:     */       {
/* 1169:1331 */         double[] array = (double[])value;
/* 1170:1332 */         writeLengthTo(output, array.length, true);
/* 1171:     */         
/* 1172:1334 */         int i = 0;
/* 1173:1334 */         for (int len = array.length; i < len; i++) {
/* 1174:1335 */           output.writeDouble(2, array[i], true);
/* 1175:     */         }
/* 1176:1337 */         return;
/* 1177:     */       }
/* 1178:1340 */       Double[] array = (Double[])value;
/* 1179:1341 */       writeLengthTo(output, array.length, false);
/* 1180:     */       
/* 1181:1343 */       int nullCount = 0;
/* 1182:1344 */       int i = 0;
/* 1183:1344 */       for (int len = array.length; i < len; i++)
/* 1184:     */       {
/* 1185:1346 */         Double v = array[i];
/* 1186:1347 */         if (v != null)
/* 1187:     */         {
/* 1188:1349 */           if (nullCount != 0)
/* 1189:     */           {
/* 1190:1351 */             output.writeUInt32(3, nullCount, false);
/* 1191:1352 */             nullCount = 0;
/* 1192:     */           }
/* 1193:1355 */           output.writeDouble(2, v.doubleValue(), true);
/* 1194:     */         }
/* 1195:1357 */         else if (this.allowNullArrayElement)
/* 1196:     */         {
/* 1197:1359 */           nullCount++;
/* 1198:     */         }
/* 1199:     */       }
/* 1200:1364 */       if (nullCount != 0) {
/* 1201:1365 */         output.writeUInt32(3, nullCount, false);
/* 1202:     */       }
/* 1203:     */     }
/* 1204:     */     
/* 1205:     */     public void writeTo(Output output, Object value)
/* 1206:     */       throws IOException
/* 1207:     */     {
/* 1208:1371 */       writeTo(output, value, this.primitive);
/* 1209:     */     }
/* 1210:     */   }
/* 1211:     */   
/* 1212:     */   public static class StringArray
/* 1213:     */     extends ArraySchemas.Base
/* 1214:     */   {
/* 1215:1377 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1216:     */     {
/* 1217:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1218:     */         throws IOException
/* 1219:     */       {
/* 1220:1384 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.StringArray.this.strategy, RuntimeFieldFactory.STRING);
/* 1221:     */       }
/* 1222:     */     };
/* 1223:     */     
/* 1224:     */     StringArray(IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 1225:     */     {
/* 1226:1391 */       super(handler);
/* 1227:     */     }
/* 1228:     */     
/* 1229:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1230:     */     {
/* 1231:1397 */       return this.pipeSchema;
/* 1232:     */     }
/* 1233:     */     
/* 1234:     */     public Object readFrom(Input input, Object owner)
/* 1235:     */       throws IOException
/* 1236:     */     {
/* 1237:1403 */       if (1 != input.readFieldNumber(this)) {
/* 1238:1404 */         throw new ProtostuffException("Corrupt input.");
/* 1239:     */       }
/* 1240:1406 */       int len = input.readInt32();
/* 1241:     */       
/* 1242:1408 */       String[] array = new String[len];
/* 1243:1409 */       if ((input instanceof GraphInput)) {
/* 1244:1412 */         ((GraphInput)input).updateLast(array, owner);
/* 1245:     */       }
/* 1246:1415 */       for (int i = 0; i < len;) {
/* 1247:1417 */         switch (input.readFieldNumber(this))
/* 1248:     */         {
/* 1249:     */         case 2: 
/* 1250:1420 */           array[(i++)] = input.readString();
/* 1251:1421 */           break;
/* 1252:     */         case 3: 
/* 1253:1423 */           i += input.readUInt32();
/* 1254:1424 */           break;
/* 1255:     */         default: 
/* 1256:1426 */           throw new ProtostuffException("Corrupt input.");
/* 1257:     */         }
/* 1258:     */       }
/* 1259:1430 */       if (0 != input.readFieldNumber(this)) {
/* 1260:1431 */         throw new ProtostuffException("Corrupt input.");
/* 1261:     */       }
/* 1262:1433 */       return array;
/* 1263:     */     }
/* 1264:     */     
/* 1265:     */     public void writeTo(Output output, Object value)
/* 1266:     */       throws IOException
/* 1267:     */     {
/* 1268:1439 */       CharSequence[] array = (CharSequence[])value;
/* 1269:1440 */       output.writeInt32(1, array.length, false);
/* 1270:     */       
/* 1271:1442 */       int nullCount = 0;
/* 1272:1443 */       int i = 0;
/* 1273:1443 */       for (int len = array.length; i < len; i++)
/* 1274:     */       {
/* 1275:1445 */         CharSequence v = array[i];
/* 1276:1446 */         if (v != null)
/* 1277:     */         {
/* 1278:1448 */           if (nullCount != 0)
/* 1279:     */           {
/* 1280:1450 */             output.writeUInt32(3, nullCount, false);
/* 1281:1451 */             nullCount = 0;
/* 1282:     */           }
/* 1283:1454 */           output.writeString(2, v, true);
/* 1284:     */         }
/* 1285:1456 */         else if (this.allowNullArrayElement)
/* 1286:     */         {
/* 1287:1458 */           nullCount++;
/* 1288:     */         }
/* 1289:     */       }
/* 1290:1463 */       if (nullCount != 0) {
/* 1291:1464 */         output.writeUInt32(3, nullCount, false);
/* 1292:     */       }
/* 1293:     */     }
/* 1294:     */   }
/* 1295:     */   
/* 1296:     */   public static class ByteStringArray
/* 1297:     */     extends ArraySchemas.Base
/* 1298:     */   {
/* 1299:1470 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1300:     */     {
/* 1301:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1302:     */         throws IOException
/* 1303:     */       {
/* 1304:1477 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.ByteStringArray.this.strategy, RuntimeFieldFactory.BYTES);
/* 1305:     */       }
/* 1306:     */     };
/* 1307:     */     
/* 1308:     */     ByteStringArray(IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 1309:     */     {
/* 1310:1484 */       super(handler);
/* 1311:     */     }
/* 1312:     */     
/* 1313:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1314:     */     {
/* 1315:1490 */       return this.pipeSchema;
/* 1316:     */     }
/* 1317:     */     
/* 1318:     */     public Object readFrom(Input input, Object owner)
/* 1319:     */       throws IOException
/* 1320:     */     {
/* 1321:1496 */       if (1 != input.readFieldNumber(this)) {
/* 1322:1497 */         throw new ProtostuffException("Corrupt input.");
/* 1323:     */       }
/* 1324:1499 */       int len = input.readInt32();
/* 1325:     */       
/* 1326:1501 */       ByteString[] array = new ByteString[len];
/* 1327:1502 */       if ((input instanceof GraphInput)) {
/* 1328:1505 */         ((GraphInput)input).updateLast(array, owner);
/* 1329:     */       }
/* 1330:1508 */       for (int i = 0; i < len;) {
/* 1331:1510 */         switch (input.readFieldNumber(this))
/* 1332:     */         {
/* 1333:     */         case 2: 
/* 1334:1513 */           array[(i++)] = input.readBytes();
/* 1335:1514 */           break;
/* 1336:     */         case 3: 
/* 1337:1516 */           i += input.readUInt32();
/* 1338:1517 */           break;
/* 1339:     */         default: 
/* 1340:1519 */           throw new ProtostuffException("Corrupt input.");
/* 1341:     */         }
/* 1342:     */       }
/* 1343:1523 */       if (0 != input.readFieldNumber(this)) {
/* 1344:1524 */         throw new ProtostuffException("Corrupt input.");
/* 1345:     */       }
/* 1346:1526 */       return array;
/* 1347:     */     }
/* 1348:     */     
/* 1349:     */     public void writeTo(Output output, Object value)
/* 1350:     */       throws IOException
/* 1351:     */     {
/* 1352:1532 */       ByteString[] array = (ByteString[])value;
/* 1353:1533 */       output.writeInt32(1, array.length, false);
/* 1354:     */       
/* 1355:1535 */       int nullCount = 0;
/* 1356:1536 */       int i = 0;
/* 1357:1536 */       for (int len = array.length; i < len; i++)
/* 1358:     */       {
/* 1359:1538 */         ByteString v = array[i];
/* 1360:1539 */         if (v != null)
/* 1361:     */         {
/* 1362:1541 */           if (nullCount != 0)
/* 1363:     */           {
/* 1364:1543 */             output.writeUInt32(3, nullCount, false);
/* 1365:1544 */             nullCount = 0;
/* 1366:     */           }
/* 1367:1547 */           output.writeBytes(2, v, true);
/* 1368:     */         }
/* 1369:1549 */         else if (this.allowNullArrayElement)
/* 1370:     */         {
/* 1371:1551 */           nullCount++;
/* 1372:     */         }
/* 1373:     */       }
/* 1374:1556 */       if (nullCount != 0) {
/* 1375:1557 */         output.writeUInt32(3, nullCount, false);
/* 1376:     */       }
/* 1377:     */     }
/* 1378:     */   }
/* 1379:     */   
/* 1380:     */   public static class ByteArrayArray
/* 1381:     */     extends ArraySchemas.Base
/* 1382:     */   {
/* 1383:1563 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1384:     */     {
/* 1385:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1386:     */         throws IOException
/* 1387:     */       {
/* 1388:1570 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.ByteArrayArray.this.strategy, RuntimeFieldFactory.BYTE_ARRAY);
/* 1389:     */       }
/* 1390:     */     };
/* 1391:     */     
/* 1392:     */     ByteArrayArray(IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 1393:     */     {
/* 1394:1577 */       super(handler);
/* 1395:     */     }
/* 1396:     */     
/* 1397:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1398:     */     {
/* 1399:1583 */       return this.pipeSchema;
/* 1400:     */     }
/* 1401:     */     
/* 1402:     */     public Object readFrom(Input input, Object owner)
/* 1403:     */       throws IOException
/* 1404:     */     {
/* 1405:1589 */       if (1 != input.readFieldNumber(this)) {
/* 1406:1590 */         throw new ProtostuffException("Corrupt input.");
/* 1407:     */       }
/* 1408:1592 */       int len = input.readInt32();
/* 1409:     */       
/* 1410:1594 */       byte[][] array = new byte[len][];
/* 1411:1595 */       if ((input instanceof GraphInput)) {
/* 1412:1598 */         ((GraphInput)input).updateLast(array, owner);
/* 1413:     */       }
/* 1414:1601 */       for (int i = 0; i < len;) {
/* 1415:1603 */         switch (input.readFieldNumber(this))
/* 1416:     */         {
/* 1417:     */         case 2: 
/* 1418:1606 */           array[(i++)] = input.readByteArray();
/* 1419:1607 */           break;
/* 1420:     */         case 3: 
/* 1421:1609 */           i += input.readUInt32();
/* 1422:1610 */           break;
/* 1423:     */         default: 
/* 1424:1612 */           throw new ProtostuffException("Corrupt input.");
/* 1425:     */         }
/* 1426:     */       }
/* 1427:1616 */       if (0 != input.readFieldNumber(this)) {
/* 1428:1617 */         throw new ProtostuffException("Corrupt input.");
/* 1429:     */       }
/* 1430:1619 */       return array;
/* 1431:     */     }
/* 1432:     */     
/* 1433:     */     public void writeTo(Output output, Object value)
/* 1434:     */       throws IOException
/* 1435:     */     {
/* 1436:1625 */       byte[][] array = (byte[][])value;
/* 1437:1626 */       output.writeInt32(1, array.length, false);
/* 1438:     */       
/* 1439:1628 */       int nullCount = 0;
/* 1440:1629 */       int i = 0;
/* 1441:1629 */       for (int len = array.length; i < len; i++)
/* 1442:     */       {
/* 1443:1631 */         byte[] v = array[i];
/* 1444:1632 */         if (v != null)
/* 1445:     */         {
/* 1446:1634 */           if (nullCount != 0)
/* 1447:     */           {
/* 1448:1636 */             output.writeUInt32(3, nullCount, false);
/* 1449:1637 */             nullCount = 0;
/* 1450:     */           }
/* 1451:1640 */           output.writeByteArray(2, v, true);
/* 1452:     */         }
/* 1453:1642 */         else if (this.allowNullArrayElement)
/* 1454:     */         {
/* 1455:1644 */           nullCount++;
/* 1456:     */         }
/* 1457:     */       }
/* 1458:1649 */       if (nullCount != 0) {
/* 1459:1650 */         output.writeUInt32(3, nullCount, false);
/* 1460:     */       }
/* 1461:     */     }
/* 1462:     */   }
/* 1463:     */   
/* 1464:     */   public static class BigDecimalArray
/* 1465:     */     extends ArraySchemas.Base
/* 1466:     */   {
/* 1467:1656 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1468:     */     {
/* 1469:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1470:     */         throws IOException
/* 1471:     */       {
/* 1472:1663 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.BigDecimalArray.this.strategy, RuntimeFieldFactory.BIGDECIMAL);
/* 1473:     */       }
/* 1474:     */     };
/* 1475:     */     
/* 1476:     */     BigDecimalArray(IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 1477:     */     {
/* 1478:1670 */       super(handler);
/* 1479:     */     }
/* 1480:     */     
/* 1481:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1482:     */     {
/* 1483:1676 */       return this.pipeSchema;
/* 1484:     */     }
/* 1485:     */     
/* 1486:     */     public Object readFrom(Input input, Object owner)
/* 1487:     */       throws IOException
/* 1488:     */     {
/* 1489:1682 */       if (1 != input.readFieldNumber(this)) {
/* 1490:1683 */         throw new ProtostuffException("Corrupt input.");
/* 1491:     */       }
/* 1492:1685 */       int len = input.readInt32();
/* 1493:     */       
/* 1494:1687 */       BigDecimal[] array = new BigDecimal[len];
/* 1495:1688 */       if ((input instanceof GraphInput)) {
/* 1496:1691 */         ((GraphInput)input).updateLast(array, owner);
/* 1497:     */       }
/* 1498:1694 */       for (int i = 0; i < len;) {
/* 1499:1696 */         switch (input.readFieldNumber(this))
/* 1500:     */         {
/* 1501:     */         case 2: 
/* 1502:1699 */           array[(i++)] = new BigDecimal(input.readString());
/* 1503:1700 */           break;
/* 1504:     */         case 3: 
/* 1505:1702 */           i += input.readUInt32();
/* 1506:1703 */           break;
/* 1507:     */         default: 
/* 1508:1705 */           throw new ProtostuffException("Corrupt input.");
/* 1509:     */         }
/* 1510:     */       }
/* 1511:1709 */       if (0 != input.readFieldNumber(this)) {
/* 1512:1710 */         throw new ProtostuffException("Corrupt input.");
/* 1513:     */       }
/* 1514:1712 */       return array;
/* 1515:     */     }
/* 1516:     */     
/* 1517:     */     public void writeTo(Output output, Object value)
/* 1518:     */       throws IOException
/* 1519:     */     {
/* 1520:1718 */       BigDecimal[] array = (BigDecimal[])value;
/* 1521:1719 */       output.writeInt32(1, array.length, false);
/* 1522:     */       
/* 1523:1721 */       int nullCount = 0;
/* 1524:1722 */       int i = 0;
/* 1525:1722 */       for (int len = array.length; i < len; i++)
/* 1526:     */       {
/* 1527:1724 */         BigDecimal v = array[i];
/* 1528:1725 */         if (v != null)
/* 1529:     */         {
/* 1530:1727 */           if (nullCount != 0)
/* 1531:     */           {
/* 1532:1729 */             output.writeUInt32(3, nullCount, false);
/* 1533:1730 */             nullCount = 0;
/* 1534:     */           }
/* 1535:1733 */           output.writeString(2, v.toString(), true);
/* 1536:     */         }
/* 1537:1735 */         else if (this.allowNullArrayElement)
/* 1538:     */         {
/* 1539:1737 */           nullCount++;
/* 1540:     */         }
/* 1541:     */       }
/* 1542:1742 */       if (nullCount != 0) {
/* 1543:1743 */         output.writeUInt32(3, nullCount, false);
/* 1544:     */       }
/* 1545:     */     }
/* 1546:     */   }
/* 1547:     */   
/* 1548:     */   public static class BigIntegerArray
/* 1549:     */     extends ArraySchemas.Base
/* 1550:     */   {
/* 1551:1749 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1552:     */     {
/* 1553:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1554:     */         throws IOException
/* 1555:     */       {
/* 1556:1756 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.BigIntegerArray.this.strategy, RuntimeFieldFactory.BIGINTEGER);
/* 1557:     */       }
/* 1558:     */     };
/* 1559:     */     
/* 1560:     */     BigIntegerArray(IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 1561:     */     {
/* 1562:1763 */       super(handler);
/* 1563:     */     }
/* 1564:     */     
/* 1565:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1566:     */     {
/* 1567:1769 */       return this.pipeSchema;
/* 1568:     */     }
/* 1569:     */     
/* 1570:     */     public Object readFrom(Input input, Object owner)
/* 1571:     */       throws IOException
/* 1572:     */     {
/* 1573:1775 */       if (1 != input.readFieldNumber(this)) {
/* 1574:1776 */         throw new ProtostuffException("Corrupt input.");
/* 1575:     */       }
/* 1576:1778 */       int len = input.readInt32();
/* 1577:     */       
/* 1578:1780 */       BigInteger[] array = new BigInteger[len];
/* 1579:1781 */       if ((input instanceof GraphInput)) {
/* 1580:1784 */         ((GraphInput)input).updateLast(array, owner);
/* 1581:     */       }
/* 1582:1787 */       for (int i = 0; i < len;) {
/* 1583:1789 */         switch (input.readFieldNumber(this))
/* 1584:     */         {
/* 1585:     */         case 2: 
/* 1586:1792 */           array[(i++)] = new BigInteger(input.readByteArray());
/* 1587:1793 */           break;
/* 1588:     */         case 3: 
/* 1589:1795 */           i += input.readUInt32();
/* 1590:1796 */           break;
/* 1591:     */         default: 
/* 1592:1798 */           throw new ProtostuffException("Corrupt input.");
/* 1593:     */         }
/* 1594:     */       }
/* 1595:1802 */       if (0 != input.readFieldNumber(this)) {
/* 1596:1803 */         throw new ProtostuffException("Corrupt input.");
/* 1597:     */       }
/* 1598:1805 */       return array;
/* 1599:     */     }
/* 1600:     */     
/* 1601:     */     public void writeTo(Output output, Object value)
/* 1602:     */       throws IOException
/* 1603:     */     {
/* 1604:1811 */       BigInteger[] array = (BigInteger[])value;
/* 1605:1812 */       output.writeInt32(1, array.length, false);
/* 1606:     */       
/* 1607:1814 */       int nullCount = 0;
/* 1608:1815 */       int i = 0;
/* 1609:1815 */       for (int len = array.length; i < len; i++)
/* 1610:     */       {
/* 1611:1817 */         BigInteger v = array[i];
/* 1612:1818 */         if (v != null)
/* 1613:     */         {
/* 1614:1820 */           if (nullCount != 0)
/* 1615:     */           {
/* 1616:1822 */             output.writeUInt32(3, nullCount, false);
/* 1617:1823 */             nullCount = 0;
/* 1618:     */           }
/* 1619:1826 */           output.writeByteArray(2, v.toByteArray(), true);
/* 1620:     */         }
/* 1621:1828 */         else if (this.allowNullArrayElement)
/* 1622:     */         {
/* 1623:1830 */           nullCount++;
/* 1624:     */         }
/* 1625:     */       }
/* 1626:1835 */       if (nullCount != 0) {
/* 1627:1836 */         output.writeUInt32(3, nullCount, false);
/* 1628:     */       }
/* 1629:     */     }
/* 1630:     */   }
/* 1631:     */   
/* 1632:     */   public static class DateArray
/* 1633:     */     extends ArraySchemas.Base
/* 1634:     */   {
/* 1635:1842 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1636:     */     {
/* 1637:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1638:     */         throws IOException
/* 1639:     */       {
/* 1640:1849 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.DateArray.this.strategy, RuntimeFieldFactory.DATE);
/* 1641:     */       }
/* 1642:     */     };
/* 1643:     */     
/* 1644:     */     DateArray(IdStrategy strategy, PolymorphicSchema.Handler handler)
/* 1645:     */     {
/* 1646:1856 */       super(handler);
/* 1647:     */     }
/* 1648:     */     
/* 1649:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1650:     */     {
/* 1651:1862 */       return this.pipeSchema;
/* 1652:     */     }
/* 1653:     */     
/* 1654:     */     public Object readFrom(Input input, Object owner)
/* 1655:     */       throws IOException
/* 1656:     */     {
/* 1657:1868 */       if (1 != input.readFieldNumber(this)) {
/* 1658:1869 */         throw new ProtostuffException("Corrupt input.");
/* 1659:     */       }
/* 1660:1871 */       int len = input.readInt32();
/* 1661:     */       
/* 1662:1873 */       Date[] array = new Date[len];
/* 1663:1874 */       if ((input instanceof GraphInput)) {
/* 1664:1877 */         ((GraphInput)input).updateLast(array, owner);
/* 1665:     */       }
/* 1666:1880 */       for (int i = 0; i < len;) {
/* 1667:1882 */         switch (input.readFieldNumber(this))
/* 1668:     */         {
/* 1669:     */         case 2: 
/* 1670:1885 */           array[(i++)] = new Date(input.readFixed64());
/* 1671:1886 */           break;
/* 1672:     */         case 3: 
/* 1673:1888 */           i += input.readUInt32();
/* 1674:1889 */           break;
/* 1675:     */         default: 
/* 1676:1891 */           throw new ProtostuffException("Corrupt input.");
/* 1677:     */         }
/* 1678:     */       }
/* 1679:1895 */       if (0 != input.readFieldNumber(this)) {
/* 1680:1896 */         throw new ProtostuffException("Corrupt input.");
/* 1681:     */       }
/* 1682:1898 */       return array;
/* 1683:     */     }
/* 1684:     */     
/* 1685:     */     public void writeTo(Output output, Object value)
/* 1686:     */       throws IOException
/* 1687:     */     {
/* 1688:1904 */       Date[] array = (Date[])value;
/* 1689:1905 */       output.writeInt32(1, array.length, false);
/* 1690:     */       
/* 1691:1907 */       int nullCount = 0;
/* 1692:1908 */       int i = 0;
/* 1693:1908 */       for (int len = array.length; i < len; i++)
/* 1694:     */       {
/* 1695:1910 */         Date v = array[i];
/* 1696:1911 */         if (v != null)
/* 1697:     */         {
/* 1698:1913 */           if (nullCount != 0)
/* 1699:     */           {
/* 1700:1915 */             output.writeUInt32(3, nullCount, false);
/* 1701:1916 */             nullCount = 0;
/* 1702:     */           }
/* 1703:1919 */           output.writeFixed64(2, v.getTime(), true);
/* 1704:     */         }
/* 1705:1921 */         else if (this.allowNullArrayElement)
/* 1706:     */         {
/* 1707:1923 */           nullCount++;
/* 1708:     */         }
/* 1709:     */       }
/* 1710:1928 */       if (nullCount != 0) {
/* 1711:1929 */         output.writeUInt32(3, nullCount, false);
/* 1712:     */       }
/* 1713:     */     }
/* 1714:     */   }
/* 1715:     */   
/* 1716:     */   public static class DelegateArray
/* 1717:     */     extends ArraySchemas.Base
/* 1718:     */   {
/* 1719:1935 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1720:     */     {
/* 1721:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1722:     */         throws IOException
/* 1723:     */       {
/* 1724:1942 */         ArraySchemas.transferObject(this, pipe, input, output, ArraySchemas.DelegateArray.this.strategy, ArraySchemas.DelegateArray.this.delegate);
/* 1725:     */       }
/* 1726:     */     };
/* 1727:     */     final Delegate<Object> delegate;
/* 1728:     */     
/* 1729:     */     public DelegateArray(IdStrategy strategy, PolymorphicSchema.Handler handler, Delegate<Object> delegate)
/* 1730:     */     {
/* 1731:1950 */       super(handler);
/* 1732:1951 */       this.delegate = delegate;
/* 1733:     */     }
/* 1734:     */     
/* 1735:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1736:     */     {
/* 1737:1957 */       return this.pipeSchema;
/* 1738:     */     }
/* 1739:     */     
/* 1740:     */     public Object readFrom(Input input, Object owner)
/* 1741:     */       throws IOException
/* 1742:     */     {
/* 1743:1963 */       if (1 != input.readFieldNumber(this)) {
/* 1744:1964 */         throw new ProtostuffException("Corrupt input.");
/* 1745:     */       }
/* 1746:1966 */       int len = input.readInt32();
/* 1747:     */       
/* 1748:1968 */       Object array = Array.newInstance(this.delegate.typeClass(), len);
/* 1749:1969 */       if ((input instanceof GraphInput)) {
/* 1750:1972 */         ((GraphInput)input).updateLast(array, owner);
/* 1751:     */       }
/* 1752:1975 */       for (int i = 0; i < len;) {
/* 1753:1977 */         switch (input.readFieldNumber(this))
/* 1754:     */         {
/* 1755:     */         case 2: 
/* 1756:1980 */           Array.set(array, i++, this.delegate.readFrom(input));
/* 1757:1981 */           break;
/* 1758:     */         case 3: 
/* 1759:1983 */           i += input.readUInt32();
/* 1760:1984 */           break;
/* 1761:     */         default: 
/* 1762:1986 */           throw new ProtostuffException("Corrupt input.");
/* 1763:     */         }
/* 1764:     */       }
/* 1765:1990 */       if (0 != input.readFieldNumber(this)) {
/* 1766:1991 */         throw new ProtostuffException("Corrupt input.");
/* 1767:     */       }
/* 1768:1993 */       return array;
/* 1769:     */     }
/* 1770:     */     
/* 1771:     */     public void writeTo(Output output, Object array)
/* 1772:     */       throws IOException
/* 1773:     */     {
/* 1774:1999 */       int len = Array.getLength(array);
/* 1775:     */       
/* 1776:2001 */       output.writeInt32(1, len, false);
/* 1777:     */       
/* 1778:2003 */       int nullCount = 0;
/* 1779:2004 */       for (int i = 0; i < len; i++)
/* 1780:     */       {
/* 1781:2006 */         Object v = Array.get(array, i);
/* 1782:2007 */         if (v != null)
/* 1783:     */         {
/* 1784:2009 */           if (nullCount != 0)
/* 1785:     */           {
/* 1786:2011 */             output.writeUInt32(3, nullCount, false);
/* 1787:2012 */             nullCount = 0;
/* 1788:     */           }
/* 1789:2015 */           this.delegate.writeTo(output, 2, v, true);
/* 1790:     */         }
/* 1791:2017 */         else if (this.allowNullArrayElement)
/* 1792:     */         {
/* 1793:2019 */           nullCount++;
/* 1794:     */         }
/* 1795:     */       }
/* 1796:2024 */       if (nullCount != 0) {
/* 1797:2025 */         output.writeUInt32(3, nullCount, false);
/* 1798:     */       }
/* 1799:     */     }
/* 1800:     */   }
/* 1801:     */   
/* 1802:     */   public static class EnumArray
/* 1803:     */     extends ArraySchemas.Base
/* 1804:     */   {
/* 1805:2031 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1806:     */     {
/* 1807:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1808:     */         throws IOException
/* 1809:     */       {
/* 1810:2039 */         if (1 != input.readFieldNumber(ArraySchemas.EnumArray.this.pipeSchema.wrappedSchema)) {
/* 1811:2040 */           throw new ProtostuffException("Corrupt input.");
/* 1812:     */         }
/* 1813:2042 */         int len = input.readInt32();
/* 1814:     */         
/* 1815:2044 */         output.writeInt32(1, len, false);
/* 1816:     */         
/* 1817:2046 */         int i = 0;
/* 1818:2046 */         for (int nullCount = 0; i < len;) {
/* 1819:2048 */           switch (input.readFieldNumber(ArraySchemas.EnumArray.this.pipeSchema.wrappedSchema))
/* 1820:     */           {
/* 1821:     */           case 2: 
/* 1822:2051 */             i++;
/* 1823:2052 */             EnumIO.transfer(pipe, input, output, 2, true, ArraySchemas.EnumArray.this.eio.strategy);
/* 1824:2053 */             break;
/* 1825:     */           case 3: 
/* 1826:2055 */             nullCount = input.readUInt32();
/* 1827:2056 */             i += nullCount;
/* 1828:2057 */             output.writeUInt32(3, nullCount, false);
/* 1829:2058 */             break;
/* 1830:     */           default: 
/* 1831:2060 */             throw new ProtostuffException("Corrupt input.");
/* 1832:     */           }
/* 1833:     */         }
/* 1834:2064 */         if (0 != input.readFieldNumber(ArraySchemas.EnumArray.this.pipeSchema.wrappedSchema)) {
/* 1835:2065 */           throw new ProtostuffException("Corrupt input.");
/* 1836:     */         }
/* 1837:     */       }
/* 1838:     */     };
/* 1839:     */     final EnumIO<?> eio;
/* 1840:     */     
/* 1841:     */     public EnumArray(IdStrategy strategy, PolymorphicSchema.Handler handler, EnumIO<?> eio)
/* 1842:     */     {
/* 1843:2073 */       super(handler);
/* 1844:2074 */       this.eio = eio;
/* 1845:     */     }
/* 1846:     */     
/* 1847:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1848:     */     {
/* 1849:2080 */       return this.pipeSchema;
/* 1850:     */     }
/* 1851:     */     
/* 1852:     */     public Object readFrom(Input input, Object owner)
/* 1853:     */       throws IOException
/* 1854:     */     {
/* 1855:2086 */       if (1 != input.readFieldNumber(this)) {
/* 1856:2087 */         throw new ProtostuffException("Corrupt input.");
/* 1857:     */       }
/* 1858:2089 */       int len = input.readInt32();
/* 1859:     */       
/* 1860:2091 */       Object array = Array.newInstance(this.eio.enumClass, len);
/* 1861:2092 */       if ((input instanceof GraphInput)) {
/* 1862:2095 */         ((GraphInput)input).updateLast(array, owner);
/* 1863:     */       }
/* 1864:2098 */       for (int i = 0; i < len;) {
/* 1865:2100 */         switch (input.readFieldNumber(this))
/* 1866:     */         {
/* 1867:     */         case 2: 
/* 1868:2103 */           Array.set(array, i++, this.eio.readFrom(input));
/* 1869:2104 */           break;
/* 1870:     */         case 3: 
/* 1871:2106 */           i += input.readUInt32();
/* 1872:2107 */           break;
/* 1873:     */         default: 
/* 1874:2109 */           throw new ProtostuffException("Corrupt input.");
/* 1875:     */         }
/* 1876:     */       }
/* 1877:2113 */       if (0 != input.readFieldNumber(this)) {
/* 1878:2114 */         throw new ProtostuffException("Corrupt input.");
/* 1879:     */       }
/* 1880:2116 */       return array;
/* 1881:     */     }
/* 1882:     */     
/* 1883:     */     public void writeTo(Output output, Object array)
/* 1884:     */       throws IOException
/* 1885:     */     {
/* 1886:2122 */       int len = Array.getLength(array);
/* 1887:     */       
/* 1888:2124 */       output.writeInt32(1, len, false);
/* 1889:     */       
/* 1890:2126 */       int nullCount = 0;
/* 1891:2127 */       for (int i = 0; i < len; i++)
/* 1892:     */       {
/* 1893:2129 */         Enum<?> v = (Enum)Array.get(array, i);
/* 1894:2130 */         if (v != null)
/* 1895:     */         {
/* 1896:2132 */           if (nullCount != 0)
/* 1897:     */           {
/* 1898:2134 */             output.writeUInt32(3, nullCount, false);
/* 1899:2135 */             nullCount = 0;
/* 1900:     */           }
/* 1901:2138 */           this.eio.writeTo(output, 2, true, v);
/* 1902:     */         }
/* 1903:2140 */         else if (this.allowNullArrayElement)
/* 1904:     */         {
/* 1905:2142 */           nullCount++;
/* 1906:     */         }
/* 1907:     */       }
/* 1908:2147 */       if (nullCount != 0) {
/* 1909:2148 */         output.writeUInt32(3, nullCount, false);
/* 1910:     */       }
/* 1911:     */     }
/* 1912:     */   }
/* 1913:     */   
/* 1914:     */   public static class PojoArray
/* 1915:     */     extends ArraySchemas.Base
/* 1916:     */   {
/* 1917:2154 */     protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/* 1918:     */     {
/* 1919:     */       protected void transfer(Pipe pipe, Input input, Output output)
/* 1920:     */         throws IOException
/* 1921:     */       {
/* 1922:2162 */         if (1 != input.readFieldNumber(ArraySchemas.PojoArray.this.pipeSchema.wrappedSchema)) {
/* 1923:2163 */           throw new ProtostuffException("Corrupt input.");
/* 1924:     */         }
/* 1925:2165 */         int len = input.readInt32();
/* 1926:     */         
/* 1927:2167 */         output.writeInt32(1, len, false);
/* 1928:     */         
/* 1929:2169 */         int i = 0;
/* 1930:2169 */         for (int nullCount = 0; i < len;) {
/* 1931:2171 */           switch (input.readFieldNumber(ArraySchemas.PojoArray.this.pipeSchema.wrappedSchema))
/* 1932:     */           {
/* 1933:     */           case 2: 
/* 1934:2174 */             i++;
/* 1935:2175 */             output.writeObject(2, pipe, ArraySchemas.PojoArray.this.hs.getPipeSchema(), true);
/* 1936:     */             
/* 1937:2177 */             break;
/* 1938:     */           case 3: 
/* 1939:2179 */             nullCount = input.readUInt32();
/* 1940:2180 */             i += nullCount;
/* 1941:2181 */             output.writeUInt32(3, nullCount, false);
/* 1942:2182 */             break;
/* 1943:     */           default: 
/* 1944:2184 */             throw new ProtostuffException("Corrupt input.");
/* 1945:     */           }
/* 1946:     */         }
/* 1947:2188 */         if (0 != input.readFieldNumber(ArraySchemas.PojoArray.this.pipeSchema.wrappedSchema)) {
/* 1948:2189 */           throw new ProtostuffException("Corrupt input.");
/* 1949:     */         }
/* 1950:     */       }
/* 1951:     */     };
/* 1952:     */     final HasSchema<Object> hs;
/* 1953:     */     
/* 1954:     */     public PojoArray(IdStrategy strategy, PolymorphicSchema.Handler handler, HasSchema<Object> hs)
/* 1955:     */     {
/* 1956:2197 */       super(handler);
/* 1957:2198 */       this.hs = hs;
/* 1958:     */     }
/* 1959:     */     
/* 1960:     */     public Pipe.Schema<Object> getPipeSchema()
/* 1961:     */     {
/* 1962:2204 */       return this.pipeSchema;
/* 1963:     */     }
/* 1964:     */     
/* 1965:     */     public Object readFrom(Input input, Object owner)
/* 1966:     */       throws IOException
/* 1967:     */     {
/* 1968:2210 */       if (1 != input.readFieldNumber(this)) {
/* 1969:2211 */         throw new ProtostuffException("Corrupt input.");
/* 1970:     */       }
/* 1971:2213 */       int len = input.readInt32();
/* 1972:     */       
/* 1973:2215 */       Object array = Array.newInstance(this.hs.getSchema().typeClass(), len);
/* 1974:2216 */       if ((input instanceof GraphInput)) {
/* 1975:2219 */         ((GraphInput)input).updateLast(array, owner);
/* 1976:     */       }
/* 1977:2222 */       for (int i = 0; i < len;) {
/* 1978:2224 */         switch (input.readFieldNumber(this))
/* 1979:     */         {
/* 1980:     */         case 2: 
/* 1981:2227 */           Array.set(array, i++, input.mergeObject(null, this.hs.getSchema()));
/* 1982:2228 */           break;
/* 1983:     */         case 3: 
/* 1984:2230 */           i += input.readUInt32();
/* 1985:2231 */           break;
/* 1986:     */         default: 
/* 1987:2233 */           throw new ProtostuffException("Corrupt input.");
/* 1988:     */         }
/* 1989:     */       }
/* 1990:2237 */       if (0 != input.readFieldNumber(this)) {
/* 1991:2238 */         throw new ProtostuffException("Corrupt input.");
/* 1992:     */       }
/* 1993:2240 */       return array;
/* 1994:     */     }
/* 1995:     */     
/* 1996:     */     public void writeTo(Output output, Object array)
/* 1997:     */       throws IOException
/* 1998:     */     {
/* 1999:2246 */       int len = Array.getLength(array);
/* 2000:     */       
/* 2001:2248 */       output.writeInt32(1, len, false);
/* 2002:     */       
/* 2003:2250 */       int nullCount = 0;
/* 2004:2251 */       for (int i = 0; i < len; i++)
/* 2005:     */       {
/* 2006:2253 */         Object v = Array.get(array, i);
/* 2007:2254 */         if (v != null)
/* 2008:     */         {
/* 2009:2256 */           if (nullCount != 0)
/* 2010:     */           {
/* 2011:2258 */             output.writeUInt32(3, nullCount, false);
/* 2012:2259 */             nullCount = 0;
/* 2013:     */           }
/* 2014:2262 */           output.writeObject(2, v, this.hs.getSchema(), true);
/* 2015:     */         }
/* 2016:2264 */         else if (this.allowNullArrayElement)
/* 2017:     */         {
/* 2018:2266 */           nullCount++;
/* 2019:     */         }
/* 2020:     */       }
/* 2021:2271 */       if (nullCount != 0) {
/* 2022:2272 */         output.writeUInt32(3, nullCount, false);
/* 2023:     */       }
/* 2024:     */     }
/* 2025:     */   }
/* 2026:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.ArraySchemas
 * JD-Core Version:    0.7.0.1
 */