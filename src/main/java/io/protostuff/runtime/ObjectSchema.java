/*    1:     */ package io.protostuff.runtime;
/*    2:     */ 
/*    3:     */ import io.protostuff.CollectionSchema.MessageFactory;
/*    4:     */ import io.protostuff.GraphInput;
/*    5:     */ import io.protostuff.Input;
/*    6:     */ import io.protostuff.MapSchema.MessageFactory;
/*    7:     */ import io.protostuff.Message;
/*    8:     */ import io.protostuff.Output;
/*    9:     */ import io.protostuff.Pipe;
/*   10:     */ import io.protostuff.Pipe.Schema;
/*   11:     */ import io.protostuff.ProtostuffException;
/*   12:     */ import io.protostuff.Schema;
/*   13:     */ import io.protostuff.StatefulOutput;
/*   14:     */ import java.io.IOException;
/*   15:     */ import java.lang.reflect.Array;
/*   16:     */ import java.util.Collection;
/*   17:     */ import java.util.Collections;
/*   18:     */ import java.util.EnumMap;
/*   19:     */ import java.util.EnumSet;
/*   20:     */ import java.util.Iterator;
/*   21:     */ import java.util.Map;
/*   22:     */ 
/*   23:     */ public abstract class ObjectSchema
/*   24:     */   extends PolymorphicSchema
/*   25:     */ {
/*   26:     */   static final int ID_ENUM_VALUE = 1;
/*   27:     */   static final int ID_ARRAY_LEN = 3;
/*   28:     */   static final int ID_ARRAY_DIMENSION = 2;
/*   29:     */   
/*   30:     */   static String name(int number)
/*   31:     */   {
/*   32: 135 */     switch (number)
/*   33:     */     {
/*   34:     */     case 28: 
/*   35: 138 */       return "B";
/*   36:     */     case 29: 
/*   37: 140 */       return "C";
/*   38:     */     case 30: 
/*   39: 142 */       return "D";
/*   40:     */     case 32: 
/*   41: 145 */       return "F";
/*   42:     */     case 33: 
/*   43: 147 */       return "G";
/*   44:     */     case 34: 
/*   45: 149 */       return "H";
/*   46:     */     case 35: 
/*   47: 151 */       return "I";
/*   48:     */     case 52: 
/*   49: 154 */       return "Z";
/*   50:     */     case 1: 
/*   51: 157 */       return "a";
/*   52:     */     case 2: 
/*   53: 159 */       return "b";
/*   54:     */     case 3: 
/*   55: 161 */       return "c";
/*   56:     */     case 4: 
/*   57: 163 */       return "d";
/*   58:     */     case 5: 
/*   59: 165 */       return "e";
/*   60:     */     case 6: 
/*   61: 167 */       return "f";
/*   62:     */     case 7: 
/*   63: 169 */       return "g";
/*   64:     */     case 8: 
/*   65: 171 */       return "h";
/*   66:     */     case 9: 
/*   67: 173 */       return "i";
/*   68:     */     case 10: 
/*   69: 175 */       return "j";
/*   70:     */     case 11: 
/*   71: 177 */       return "k";
/*   72:     */     case 12: 
/*   73: 179 */       return "l";
/*   74:     */     case 13: 
/*   75: 181 */       return "m";
/*   76:     */     case 14: 
/*   77: 183 */       return "n";
/*   78:     */     case 15: 
/*   79: 185 */       return "o";
/*   80:     */     case 16: 
/*   81: 187 */       return "p";
/*   82:     */     case 17: 
/*   83: 189 */       return "q";
/*   84:     */     case 18: 
/*   85: 191 */       return "r";
/*   86:     */     case 19: 
/*   87: 193 */       return "s";
/*   88:     */     case 20: 
/*   89: 195 */       return "t";
/*   90:     */     case 21: 
/*   91: 197 */       return "u";
/*   92:     */     case 22: 
/*   93: 200 */       return "v";
/*   94:     */     case 23: 
/*   95: 202 */       return "w";
/*   96:     */     case 24: 
/*   97: 204 */       return "x";
/*   98:     */     case 25: 
/*   99: 206 */       return "y";
/*  100:     */     case 26: 
/*  101: 208 */       return "z";
/*  102:     */     case 127: 
/*  103: 211 */       return "_";
/*  104:     */     }
/*  105: 213 */     return null;
/*  106:     */   }
/*  107:     */   
/*  108:     */   static int number(String name)
/*  109:     */   {
/*  110: 219 */     if (name.length() != 1) {
/*  111: 220 */       return 0;
/*  112:     */     }
/*  113: 222 */     switch (name.charAt(0))
/*  114:     */     {
/*  115:     */     case 'B': 
/*  116: 225 */       return 28;
/*  117:     */     case 'C': 
/*  118: 227 */       return 29;
/*  119:     */     case 'D': 
/*  120: 229 */       return 30;
/*  121:     */     case 'F': 
/*  122: 232 */       return 32;
/*  123:     */     case 'G': 
/*  124: 234 */       return 33;
/*  125:     */     case 'H': 
/*  126: 236 */       return 34;
/*  127:     */     case 'I': 
/*  128: 238 */       return 35;
/*  129:     */     case 'Z': 
/*  130: 241 */       return 52;
/*  131:     */     case '_': 
/*  132: 243 */       return 127;
/*  133:     */     case 'a': 
/*  134: 246 */       return 1;
/*  135:     */     case 'b': 
/*  136: 248 */       return 2;
/*  137:     */     case 'c': 
/*  138: 250 */       return 3;
/*  139:     */     case 'd': 
/*  140: 252 */       return 4;
/*  141:     */     case 'e': 
/*  142: 254 */       return 5;
/*  143:     */     case 'f': 
/*  144: 256 */       return 6;
/*  145:     */     case 'g': 
/*  146: 258 */       return 7;
/*  147:     */     case 'h': 
/*  148: 260 */       return 8;
/*  149:     */     case 'i': 
/*  150: 262 */       return 9;
/*  151:     */     case 'j': 
/*  152: 264 */       return 10;
/*  153:     */     case 'k': 
/*  154: 266 */       return 11;
/*  155:     */     case 'l': 
/*  156: 268 */       return 12;
/*  157:     */     case 'm': 
/*  158: 270 */       return 13;
/*  159:     */     case 'n': 
/*  160: 272 */       return 14;
/*  161:     */     case 'o': 
/*  162: 274 */       return 15;
/*  163:     */     case 'p': 
/*  164: 276 */       return 16;
/*  165:     */     case 'q': 
/*  166: 278 */       return 17;
/*  167:     */     case 'r': 
/*  168: 280 */       return 18;
/*  169:     */     case 's': 
/*  170: 282 */       return 19;
/*  171:     */     case 't': 
/*  172: 284 */       return 20;
/*  173:     */     case 'u': 
/*  174: 286 */       return 21;
/*  175:     */     case 'v': 
/*  176: 289 */       return 22;
/*  177:     */     case 'w': 
/*  178: 291 */       return 23;
/*  179:     */     case 'x': 
/*  180: 293 */       return 24;
/*  181:     */     case 'y': 
/*  182: 295 */       return 25;
/*  183:     */     case 'z': 
/*  184: 297 */       return 26;
/*  185:     */     }
/*  186: 299 */     return 0;
/*  187:     */   }
/*  188:     */   
/*  189: 303 */   protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema(this)
/*  190:     */   {
/*  191:     */     protected void transfer(Pipe pipe, Input input, Output output)
/*  192:     */       throws IOException
/*  193:     */     {
/*  194: 308 */       ObjectSchema.transferObject(this, pipe, input, output, ObjectSchema.this.strategy);
/*  195:     */     }
/*  196:     */   };
/*  197:     */   
/*  198:     */   public ObjectSchema(IdStrategy strategy)
/*  199:     */   {
/*  200: 314 */     super(strategy);
/*  201:     */   }
/*  202:     */   
/*  203:     */   public Pipe.Schema<Object> getPipeSchema()
/*  204:     */   {
/*  205: 320 */     return this.pipeSchema;
/*  206:     */   }
/*  207:     */   
/*  208:     */   public String getFieldName(int number)
/*  209:     */   {
/*  210: 326 */     return name(number);
/*  211:     */   }
/*  212:     */   
/*  213:     */   public int getFieldNumber(String name)
/*  214:     */   {
/*  215: 332 */     return number(name);
/*  216:     */   }
/*  217:     */   
/*  218:     */   public String messageFullName()
/*  219:     */   {
/*  220: 338 */     return Object.class.getName();
/*  221:     */   }
/*  222:     */   
/*  223:     */   public String messageName()
/*  224:     */   {
/*  225: 344 */     return Object.class.getSimpleName();
/*  226:     */   }
/*  227:     */   
/*  228:     */   public void mergeFrom(Input input, Object owner)
/*  229:     */     throws IOException
/*  230:     */   {
/*  231: 350 */     setValue(readObjectFrom(input, this, owner, this.strategy), owner);
/*  232:     */   }
/*  233:     */   
/*  234:     */   public void writeTo(Output output, Object value)
/*  235:     */     throws IOException
/*  236:     */   {
/*  237: 356 */     writeObjectTo(output, value, this, this.strategy);
/*  238:     */   }
/*  239:     */   
/*  240:     */   static ArrayWrapper newArrayWrapper(Input input, Schema<?> schema, boolean mapped, IdStrategy strategy)
/*  241:     */     throws IOException
/*  242:     */   {
/*  243: 362 */     Class<?> componentType = strategy.resolveArrayComponentTypeFrom(input, mapped);
/*  244: 365 */     if (input.readFieldNumber(schema) != 3) {
/*  245: 366 */       throw new ProtostuffException("Corrupt input.");
/*  246:     */     }
/*  247: 367 */     int len = input.readUInt32();
/*  248: 369 */     if (input.readFieldNumber(schema) != 2) {
/*  249: 370 */       throw new ProtostuffException("Corrupt input.");
/*  250:     */     }
/*  251: 371 */     int dimensions = input.readUInt32();
/*  252: 373 */     if (dimensions == 1) {
/*  253: 374 */       return new ArrayWrapper(Array.newInstance(componentType, len));
/*  254:     */     }
/*  255: 376 */     int[] arg = new int[dimensions];
/*  256: 377 */     arg[0] = len;
/*  257: 378 */     return new ArrayWrapper(Array.newInstance(componentType, arg));
/*  258:     */   }
/*  259:     */   
/*  260:     */   static void transferArray(Pipe pipe, Input input, Output output, int number, Pipe.Schema<?> pipeSchema, boolean mapped, IdStrategy strategy)
/*  261:     */     throws IOException
/*  262:     */   {
/*  263: 384 */     strategy.transferArrayId(input, output, number, mapped);
/*  264: 386 */     if (input.readFieldNumber(pipeSchema.wrappedSchema) != 3) {
/*  265: 387 */       throw new ProtostuffException("Corrupt input.");
/*  266:     */     }
/*  267: 389 */     output.writeUInt32(3, input.readUInt32(), false);
/*  268: 391 */     if (input.readFieldNumber(pipeSchema.wrappedSchema) != 2) {
/*  269: 392 */       throw new ProtostuffException("Corrupt input.");
/*  270:     */     }
/*  271: 394 */     output.writeUInt32(2, input.readUInt32(), false);
/*  272: 396 */     if ((output instanceof StatefulOutput)) {
/*  273: 399 */       ((StatefulOutput)output).updateLast(strategy.ARRAY_PIPE_SCHEMA, pipeSchema);
/*  274:     */     }
/*  275: 402 */     Pipe.transferDirect(strategy.ARRAY_PIPE_SCHEMA, pipe, input, output);
/*  276:     */   }
/*  277:     */   
/*  278:     */   static void transferClass(Pipe pipe, Input input, Output output, int number, Pipe.Schema<?> pipeSchema, boolean mapped, boolean array, IdStrategy strategy)
/*  279:     */     throws IOException
/*  280:     */   {
/*  281: 409 */     strategy.transferClassId(input, output, number, mapped, array);
/*  282: 411 */     if (array)
/*  283:     */     {
/*  284: 413 */       if (input.readFieldNumber(pipeSchema.wrappedSchema) != 2) {
/*  285: 414 */         throw new ProtostuffException("Corrupt input.");
/*  286:     */       }
/*  287: 416 */       output.writeUInt32(2, input.readUInt32(), false);
/*  288:     */     }
/*  289:     */   }
/*  290:     */   
/*  291:     */   static Class<?> getArrayClass(Input input, Schema<?> schema, Class<?> componentType)
/*  292:     */     throws IOException
/*  293:     */   {
/*  294: 423 */     if (input.readFieldNumber(schema) != 2) {
/*  295: 424 */       throw new ProtostuffException("Corrupt input.");
/*  296:     */     }
/*  297: 425 */     int dimensions = input.readUInt32();
/*  298: 429 */     if (dimensions == 1) {
/*  299: 430 */       return Array.newInstance(componentType, 0).getClass();
/*  300:     */     }
/*  301: 432 */     int[] arg = new int[dimensions];
/*  302: 433 */     arg[0] = 0;
/*  303: 434 */     return Array.newInstance(componentType, arg).getClass();
/*  304:     */   }
/*  305:     */   
/*  306:     */   static Object readObjectFrom(Input input, Schema<?> schema, Object owner, IdStrategy strategy)
/*  307:     */     throws IOException
/*  308:     */   {
/*  309: 441 */     Object value = null;
/*  310: 442 */     int number = input.readFieldNumber(schema);
/*  311: 443 */     switch (number)
/*  312:     */     {
/*  313:     */     case 1: 
/*  314: 446 */       value = RuntimeFieldFactory.BOOL.readFrom(input);
/*  315: 447 */       break;
/*  316:     */     case 2: 
/*  317: 449 */       value = RuntimeFieldFactory.BYTE.readFrom(input);
/*  318: 450 */       break;
/*  319:     */     case 3: 
/*  320: 452 */       value = RuntimeFieldFactory.CHAR.readFrom(input);
/*  321: 453 */       break;
/*  322:     */     case 4: 
/*  323: 455 */       value = RuntimeFieldFactory.SHORT.readFrom(input);
/*  324: 456 */       break;
/*  325:     */     case 5: 
/*  326: 458 */       value = RuntimeFieldFactory.INT32.readFrom(input);
/*  327: 459 */       break;
/*  328:     */     case 6: 
/*  329: 461 */       value = RuntimeFieldFactory.INT64.readFrom(input);
/*  330: 462 */       break;
/*  331:     */     case 7: 
/*  332: 464 */       value = RuntimeFieldFactory.FLOAT.readFrom(input);
/*  333: 465 */       break;
/*  334:     */     case 8: 
/*  335: 467 */       value = RuntimeFieldFactory.DOUBLE.readFrom(input);
/*  336: 468 */       break;
/*  337:     */     case 9: 
/*  338: 470 */       value = RuntimeFieldFactory.STRING.readFrom(input);
/*  339: 471 */       break;
/*  340:     */     case 10: 
/*  341: 473 */       value = RuntimeFieldFactory.BYTES.readFrom(input);
/*  342: 474 */       break;
/*  343:     */     case 11: 
/*  344: 476 */       value = RuntimeFieldFactory.BYTE_ARRAY.readFrom(input);
/*  345: 477 */       break;
/*  346:     */     case 12: 
/*  347: 479 */       value = RuntimeFieldFactory.BIGDECIMAL.readFrom(input);
/*  348: 480 */       break;
/*  349:     */     case 13: 
/*  350: 482 */       value = RuntimeFieldFactory.BIGINTEGER.readFrom(input);
/*  351: 483 */       break;
/*  352:     */     case 14: 
/*  353: 485 */       value = RuntimeFieldFactory.DATE.readFrom(input);
/*  354: 486 */       break;
/*  355:     */     case 15: 
/*  356: 490 */       ArrayWrapper arrayWrapper = newArrayWrapper(input, schema, false, strategy);
/*  357: 493 */       if ((input instanceof GraphInput)) {
/*  358: 496 */         ((GraphInput)input).updateLast(arrayWrapper.array, owner);
/*  359:     */       }
/*  360: 499 */       strategy.COLLECTION_SCHEMA.mergeFrom(input, arrayWrapper);
/*  361:     */       
/*  362: 501 */       return arrayWrapper.array;
/*  363:     */     case 16: 
/*  364: 504 */       if (input.readUInt32() != 0) {
/*  365: 505 */         throw new ProtostuffException("Corrupt input.");
/*  366:     */       }
/*  367: 507 */       value = new Object();
/*  368:     */       
/*  369: 509 */       break;
/*  370:     */     case 17: 
/*  371: 513 */       ArrayWrapper mArrayWrapper = newArrayWrapper(input, schema, true, strategy);
/*  372: 516 */       if ((input instanceof GraphInput)) {
/*  373: 519 */         ((GraphInput)input).updateLast(mArrayWrapper.array, owner);
/*  374:     */       }
/*  375: 522 */       strategy.COLLECTION_SCHEMA.mergeFrom(input, mArrayWrapper);
/*  376:     */       
/*  377: 524 */       return mArrayWrapper.array;
/*  378:     */     case 18: 
/*  379: 527 */       value = strategy.resolveClassFrom(input, false, false);
/*  380: 528 */       break;
/*  381:     */     case 19: 
/*  382: 530 */       value = strategy.resolveClassFrom(input, true, false);
/*  383: 531 */       break;
/*  384:     */     case 20: 
/*  385: 533 */       value = getArrayClass(input, schema, strategy
/*  386: 534 */         .resolveClassFrom(input, false, true));
/*  387: 535 */       break;
/*  388:     */     case 21: 
/*  389: 537 */       value = getArrayClass(input, schema, strategy
/*  390: 538 */         .resolveClassFrom(input, true, true));
/*  391: 539 */       break;
/*  392:     */     case 24: 
/*  393: 543 */       EnumIO<?> eio = strategy.resolveEnumFrom(input);
/*  394: 545 */       if (input.readFieldNumber(schema) != 1) {
/*  395: 546 */         throw new ProtostuffException("Corrupt input.");
/*  396:     */       }
/*  397: 548 */       value = eio.readFrom(input);
/*  398: 549 */       break;
/*  399:     */     case 22: 
/*  400: 553 */       Collection<?> es = strategy.resolveEnumFrom(input).newEnumSet();
/*  401: 555 */       if ((input instanceof GraphInput)) {
/*  402: 558 */         ((GraphInput)input).updateLast(es, owner);
/*  403:     */       }
/*  404: 561 */       strategy.COLLECTION_SCHEMA.mergeFrom(input, es);
/*  405:     */       
/*  406: 563 */       return es;
/*  407:     */     case 23: 
/*  408: 567 */       Map<?, Object> em = strategy.resolveEnumFrom(input).newEnumMap();
/*  409: 569 */       if ((input instanceof GraphInput)) {
/*  410: 572 */         ((GraphInput)input).updateLast(em, owner);
/*  411:     */       }
/*  412: 575 */       strategy.MAP_SCHEMA.mergeFrom(input, em);
/*  413:     */       
/*  414: 577 */       return em;
/*  415:     */     case 25: 
/*  416: 582 */       Collection<Object> collection = strategy.resolveCollectionFrom(input).newMessage();
/*  417: 584 */       if ((input instanceof GraphInput)) {
/*  418: 587 */         ((GraphInput)input).updateLast(collection, owner);
/*  419:     */       }
/*  420: 590 */       strategy.COLLECTION_SCHEMA.mergeFrom(input, collection);
/*  421:     */       
/*  422: 592 */       return collection;
/*  423:     */     case 26: 
/*  424: 597 */       Map<Object, Object> map = strategy.resolveMapFrom(input).newMessage();
/*  425: 599 */       if ((input instanceof GraphInput)) {
/*  426: 602 */         ((GraphInput)input).updateLast(map, owner);
/*  427:     */       }
/*  428: 605 */       strategy.MAP_SCHEMA.mergeFrom(input, map);
/*  429:     */       
/*  430: 607 */       return map;
/*  431:     */     case 28: 
/*  432: 611 */       if (0 != input.readUInt32()) {
/*  433: 612 */         throw new ProtostuffException("Corrupt input.");
/*  434:     */       }
/*  435: 614 */       Object collection = PolymorphicCollectionSchema.readObjectFrom(input, strategy.POLYMORPHIC_COLLECTION_SCHEMA, owner, strategy);
/*  436: 617 */       if ((input instanceof GraphInput)) {
/*  437: 620 */         ((GraphInput)input).updateLast(collection, owner);
/*  438:     */       }
/*  439: 623 */       return collection;
/*  440:     */     case 29: 
/*  441: 627 */       if (0 != input.readUInt32()) {
/*  442: 628 */         throw new ProtostuffException("Corrupt input.");
/*  443:     */       }
/*  444: 630 */       Object map = PolymorphicMapSchema.readObjectFrom(input, strategy.POLYMORPHIC_MAP_SCHEMA, owner, strategy);
/*  445: 633 */       if ((input instanceof GraphInput)) {
/*  446: 636 */         ((GraphInput)input).updateLast(map, owner);
/*  447:     */       }
/*  448: 639 */       return map;
/*  449:     */     case 30: 
/*  450: 643 */       HasDelegate<Object> hd = strategy.resolveDelegateFrom(input);
/*  451: 644 */       if (1 != input.readFieldNumber(schema)) {
/*  452: 645 */         throw new ProtostuffException("Corrupt input.");
/*  453:     */       }
/*  454: 647 */       value = hd.delegate.readFrom(input);
/*  455: 648 */       break;
/*  456:     */     case 32: 
/*  457: 652 */       HasDelegate<Object> hd = strategy.resolveDelegateFrom(input);
/*  458:     */       
/*  459: 654 */       return hd.genericElementSchema.readFrom(input, owner);
/*  460:     */     case 33: 
/*  461: 658 */       int arrayId = input.readUInt32();int id = ArraySchemas.toInlineId(arrayId);
/*  462:     */       
/*  463: 660 */       ArraySchemas.Base arraySchema = ArraySchemas.getSchema(id, 
/*  464: 661 */         ArraySchemas.isPrimitive(arrayId), strategy);
/*  465:     */       
/*  466: 663 */       return arraySchema.readFrom(input, owner);
/*  467:     */     case 34: 
/*  468: 667 */       EnumIO<?> eio = strategy.resolveEnumFrom(input);
/*  469:     */       
/*  470: 669 */       return eio.genericElementSchema.readFrom(input, owner);
/*  471:     */     case 35: 
/*  472: 673 */       HasSchema<Object> hs = strategy.resolvePojoFrom(input, number);
/*  473:     */       
/*  474: 675 */       return hs.genericElementSchema.readFrom(input, owner);
/*  475:     */     case 52: 
/*  476: 678 */       return PolymorphicThrowableSchema.readObjectFrom(input, schema, owner, strategy, number);
/*  477:     */     case 127: 
/*  478: 683 */       Schema<Object> derivedSchema = strategy.resolvePojoFrom(input, number).getSchema();
/*  479:     */       
/*  480: 685 */       Object pojo = derivedSchema.newMessage();
/*  481: 687 */       if ((input instanceof GraphInput)) {
/*  482: 690 */         ((GraphInput)input).updateLast(pojo, owner);
/*  483:     */       }
/*  484: 693 */       derivedSchema.mergeFrom(input, pojo);
/*  485: 694 */       return pojo;
/*  486:     */     case 27: 
/*  487:     */     case 31: 
/*  488:     */     case 36: 
/*  489:     */     case 37: 
/*  490:     */     case 38: 
/*  491:     */     case 39: 
/*  492:     */     case 40: 
/*  493:     */     case 41: 
/*  494:     */     case 42: 
/*  495:     */     case 43: 
/*  496:     */     case 44: 
/*  497:     */     case 45: 
/*  498:     */     case 46: 
/*  499:     */     case 47: 
/*  500:     */     case 48: 
/*  501:     */     case 49: 
/*  502:     */     case 50: 
/*  503:     */     case 51: 
/*  504:     */     case 53: 
/*  505:     */     case 54: 
/*  506:     */     case 55: 
/*  507:     */     case 56: 
/*  508:     */     case 57: 
/*  509:     */     case 58: 
/*  510:     */     case 59: 
/*  511:     */     case 60: 
/*  512:     */     case 61: 
/*  513:     */     case 62: 
/*  514:     */     case 63: 
/*  515:     */     case 64: 
/*  516:     */     case 65: 
/*  517:     */     case 66: 
/*  518:     */     case 67: 
/*  519:     */     case 68: 
/*  520:     */     case 69: 
/*  521:     */     case 70: 
/*  522:     */     case 71: 
/*  523:     */     case 72: 
/*  524:     */     case 73: 
/*  525:     */     case 74: 
/*  526:     */     case 75: 
/*  527:     */     case 76: 
/*  528:     */     case 77: 
/*  529:     */     case 78: 
/*  530:     */     case 79: 
/*  531:     */     case 80: 
/*  532:     */     case 81: 
/*  533:     */     case 82: 
/*  534:     */     case 83: 
/*  535:     */     case 84: 
/*  536:     */     case 85: 
/*  537:     */     case 86: 
/*  538:     */     case 87: 
/*  539:     */     case 88: 
/*  540:     */     case 89: 
/*  541:     */     case 90: 
/*  542:     */     case 91: 
/*  543:     */     case 92: 
/*  544:     */     case 93: 
/*  545:     */     case 94: 
/*  546:     */     case 95: 
/*  547:     */     case 96: 
/*  548:     */     case 97: 
/*  549:     */     case 98: 
/*  550:     */     case 99: 
/*  551:     */     case 100: 
/*  552:     */     case 101: 
/*  553:     */     case 102: 
/*  554:     */     case 103: 
/*  555:     */     case 104: 
/*  556:     */     case 105: 
/*  557:     */     case 106: 
/*  558:     */     case 107: 
/*  559:     */     case 108: 
/*  560:     */     case 109: 
/*  561:     */     case 110: 
/*  562:     */     case 111: 
/*  563:     */     case 112: 
/*  564:     */     case 113: 
/*  565:     */     case 114: 
/*  566:     */     case 115: 
/*  567:     */     case 116: 
/*  568:     */     case 117: 
/*  569:     */     case 118: 
/*  570:     */     case 119: 
/*  571:     */     case 120: 
/*  572:     */     case 121: 
/*  573:     */     case 122: 
/*  574:     */     case 123: 
/*  575:     */     case 124: 
/*  576:     */     case 125: 
/*  577:     */     case 126: 
/*  578:     */     default: 
/*  579: 697 */       throw new ProtostuffException("Corrupt input.  Unknown field number: " + number);
/*  580:     */     }
/*  581: 700 */     if ((input instanceof GraphInput)) {
/*  582: 703 */       ((GraphInput)input).updateLast(value, owner);
/*  583:     */     }
/*  584: 706 */     if (input.readFieldNumber(schema) != 0) {
/*  585: 707 */       throw new ProtostuffException("Corrupt input.");
/*  586:     */     }
/*  587: 709 */     return value;
/*  588:     */   }
/*  589:     */   
/*  590:     */   static void writeObjectTo(Output output, Object value, Schema<?> currentSchema, IdStrategy strategy)
/*  591:     */     throws IOException
/*  592:     */   {
/*  593: 716 */     Class<Object> clazz = value.getClass();
/*  594:     */     
/*  595: 718 */     HasDelegate<Object> hd = strategy.tryWriteDelegateIdTo(output, 30, clazz);
/*  596: 721 */     if (hd != null)
/*  597:     */     {
/*  598: 723 */       hd.delegate.writeTo(output, 1, value, false);
/*  599: 724 */       return;
/*  600:     */     }
/*  601: 727 */     RuntimeFieldFactory<Object> inline = RuntimeFieldFactory.getInline(clazz);
/*  602: 728 */     if (inline != null)
/*  603:     */     {
/*  604: 731 */       inline.writeTo(output, inline.id, value, false);
/*  605: 732 */       return;
/*  606:     */     }
/*  607: 735 */     if (Message.class.isAssignableFrom(clazz))
/*  608:     */     {
/*  609: 737 */       Schema<Object> schema = strategy.writeMessageIdTo(output, 127, (Message)value);
/*  610: 740 */       if ((output instanceof StatefulOutput)) {
/*  611: 743 */         ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  612:     */       }
/*  613: 746 */       schema.writeTo(output, value);
/*  614: 747 */       return;
/*  615:     */     }
/*  616: 750 */     HasSchema<Object> hs = strategy.tryWritePojoIdTo(output, 127, clazz, false);
/*  617: 751 */     if (hs != null)
/*  618:     */     {
/*  619: 753 */       Schema<Object> schema = hs.getSchema();
/*  620: 755 */       if ((output instanceof StatefulOutput)) {
/*  621: 758 */         ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  622:     */       }
/*  623: 761 */       schema.writeTo(output, value);
/*  624: 762 */       return;
/*  625:     */     }
/*  626: 765 */     if (clazz.isEnum())
/*  627:     */     {
/*  628: 767 */       EnumIO<?> eio = strategy.getEnumIO(clazz);
/*  629: 768 */       strategy.writeEnumIdTo(output, 24, clazz);
/*  630: 769 */       eio.writeTo(output, 1, false, (Enum)value);
/*  631: 770 */       return;
/*  632:     */     }
/*  633: 773 */     if ((clazz.getSuperclass() != null) && (clazz.getSuperclass().isEnum()))
/*  634:     */     {
/*  635: 775 */       EnumIO<?> eio = strategy.getEnumIO(clazz.getSuperclass());
/*  636: 776 */       strategy.writeEnumIdTo(output, 24, clazz.getSuperclass());
/*  637: 777 */       eio.writeTo(output, 1, false, (Enum)value);
/*  638: 778 */       return;
/*  639:     */     }
/*  640: 781 */     if (clazz.isArray())
/*  641:     */     {
/*  642: 783 */       Class<?> componentType = clazz.getComponentType();
/*  643:     */       
/*  644: 785 */       HasDelegate<Object> hdArray = strategy.tryWriteDelegateIdTo(output, 32, componentType);
/*  645: 788 */       if (hdArray != null)
/*  646:     */       {
/*  647: 790 */         if ((output instanceof StatefulOutput)) {
/*  648: 793 */           ((StatefulOutput)output).updateLast(hdArray.genericElementSchema, currentSchema);
/*  649:     */         }
/*  650: 797 */         hdArray.genericElementSchema.writeTo(output, value);
/*  651: 798 */         return;
/*  652:     */       }
/*  653: 801 */       RuntimeFieldFactory<?> inlineArray = RuntimeFieldFactory.getInline(componentType);
/*  654: 803 */       if (inlineArray != null)
/*  655:     */       {
/*  656: 806 */         boolean primitive = componentType.isPrimitive();
/*  657: 807 */         ArraySchemas.Base arraySchema = ArraySchemas.getSchema(inlineArray.id, primitive, strategy);
/*  658:     */         
/*  659:     */ 
/*  660: 810 */         output.writeUInt32(33, 
/*  661: 811 */           ArraySchemas.toArrayId(inlineArray.id, primitive), false);
/*  662: 814 */         if ((output instanceof StatefulOutput)) {
/*  663: 817 */           ((StatefulOutput)output).updateLast(arraySchema, currentSchema);
/*  664:     */         }
/*  665: 820 */         arraySchema.writeTo(output, value);
/*  666: 821 */         return;
/*  667:     */       }
/*  668: 824 */       if (componentType.isEnum())
/*  669:     */       {
/*  670: 827 */         EnumIO<?> eio = strategy.getEnumIO(componentType);
/*  671:     */         
/*  672: 829 */         strategy.writeEnumIdTo(output, 34, componentType);
/*  673: 831 */         if ((output instanceof StatefulOutput)) {
/*  674: 834 */           ((StatefulOutput)output).updateLast(eio.genericElementSchema, currentSchema);
/*  675:     */         }
/*  676: 838 */         eio.genericElementSchema.writeTo(output, value);
/*  677: 839 */         return;
/*  678:     */       }
/*  679: 842 */       if ((Message.class.isAssignableFrom(componentType)) || 
/*  680: 843 */         (strategy.isRegistered(componentType)))
/*  681:     */       {
/*  682: 846 */         hs = strategy.writePojoIdTo(output, 35, componentType);
/*  683: 849 */         if ((output instanceof StatefulOutput)) {
/*  684: 852 */           ((StatefulOutput)output).updateLast(hs.genericElementSchema, currentSchema);
/*  685:     */         }
/*  686: 856 */         hs.genericElementSchema.writeTo(output, value);
/*  687: 857 */         return;
/*  688:     */       }
/*  689: 869 */       int dimensions = 1;
/*  690: 870 */       while (componentType.isArray())
/*  691:     */       {
/*  692: 872 */         dimensions++;
/*  693: 873 */         componentType = componentType.getComponentType();
/*  694:     */       }
/*  695: 876 */       strategy.writeArrayIdTo(output, componentType);
/*  696:     */       
/*  697: 878 */       output.writeUInt32(3, Array.getLength(value), false);
/*  698:     */       
/*  699: 880 */       output.writeUInt32(2, dimensions, false);
/*  700: 882 */       if ((output instanceof StatefulOutput)) {
/*  701: 885 */         ((StatefulOutput)output).updateLast(strategy.ARRAY_SCHEMA, currentSchema);
/*  702:     */       }
/*  703: 888 */       strategy.ARRAY_SCHEMA.writeTo(output, value);
/*  704: 889 */       return;
/*  705:     */     }
/*  706: 892 */     if (Object.class == clazz)
/*  707:     */     {
/*  708: 894 */       output.writeUInt32(16, 0, false);
/*  709: 895 */       return;
/*  710:     */     }
/*  711: 898 */     if (Class.class == value.getClass())
/*  712:     */     {
/*  713: 901 */       Class<?> c = (Class)value;
/*  714: 902 */       if (c.isArray())
/*  715:     */       {
/*  716: 904 */         int dimensions = 1;
/*  717: 905 */         Class<?> componentType = c.getComponentType();
/*  718: 906 */         while (componentType.isArray())
/*  719:     */         {
/*  720: 908 */           dimensions++;
/*  721: 909 */           componentType = componentType.getComponentType();
/*  722:     */         }
/*  723: 912 */         strategy.writeClassIdTo(output, componentType, true);
/*  724:     */         
/*  725: 914 */         output.writeUInt32(2, dimensions, false);
/*  726: 915 */         return;
/*  727:     */       }
/*  728: 918 */       strategy.writeClassIdTo(output, c, false);
/*  729: 919 */       return;
/*  730:     */     }
/*  731: 922 */     if (Throwable.class.isAssignableFrom(clazz))
/*  732:     */     {
/*  733: 925 */       PolymorphicThrowableSchema.writeObjectTo(output, value, currentSchema, strategy);
/*  734:     */       
/*  735: 927 */       return;
/*  736:     */     }
/*  737: 930 */     if (strategy.isRegistered(clazz))
/*  738:     */     {
/*  739: 934 */       Schema<Object> schema = strategy.writePojoIdTo(output, 127, clazz).getSchema();
/*  740: 936 */       if ((output instanceof StatefulOutput)) {
/*  741: 939 */         ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  742:     */       }
/*  743: 942 */       schema.writeTo(output, value);
/*  744: 943 */       return;
/*  745:     */     }
/*  746: 946 */     if (Map.class.isAssignableFrom(clazz))
/*  747:     */     {
/*  748: 948 */       if (Collections.class == clazz.getDeclaringClass())
/*  749:     */       {
/*  750: 950 */         output.writeUInt32(29, 0, false);
/*  751: 952 */         if ((output instanceof StatefulOutput)) {
/*  752: 955 */           ((StatefulOutput)output).updateLast(strategy.POLYMORPHIC_MAP_SCHEMA, currentSchema);
/*  753:     */         }
/*  754: 959 */         PolymorphicMapSchema.writeNonPublicMapTo(output, value, strategy.POLYMORPHIC_MAP_SCHEMA, strategy);
/*  755:     */         
/*  756: 961 */         return;
/*  757:     */       }
/*  758: 964 */       if (EnumMap.class.isAssignableFrom(clazz)) {
/*  759: 966 */         strategy.writeEnumIdTo(output, 23, 
/*  760: 967 */           EnumIO.getKeyTypeFromEnumMap(value));
/*  761:     */       } else {
/*  762: 971 */         strategy.writeMapIdTo(output, 26, clazz);
/*  763:     */       }
/*  764: 974 */       if ((output instanceof StatefulOutput)) {
/*  765: 977 */         ((StatefulOutput)output).updateLast(strategy.MAP_SCHEMA, currentSchema);
/*  766:     */       }
/*  767: 980 */       strategy.MAP_SCHEMA.writeTo(output, (Map)value);
/*  768: 981 */       return;
/*  769:     */     }
/*  770: 984 */     if (Collection.class.isAssignableFrom(clazz))
/*  771:     */     {
/*  772: 986 */       if (Collections.class == clazz.getDeclaringClass())
/*  773:     */       {
/*  774: 988 */         output.writeUInt32(28, 0, false);
/*  775: 990 */         if ((output instanceof StatefulOutput)) {
/*  776: 993 */           ((StatefulOutput)output).updateLast(strategy.POLYMORPHIC_COLLECTION_SCHEMA, currentSchema);
/*  777:     */         }
/*  778: 997 */         PolymorphicCollectionSchema.writeNonPublicCollectionTo(output, value, strategy.POLYMORPHIC_COLLECTION_SCHEMA, strategy);
/*  779:     */         
/*  780: 999 */         return;
/*  781:     */       }
/*  782:1002 */       if (EnumSet.class.isAssignableFrom(clazz)) {
/*  783:1004 */         strategy.writeEnumIdTo(output, 22, 
/*  784:1005 */           EnumIO.getElementTypeFromEnumSet(value));
/*  785:     */       } else {
/*  786:1009 */         strategy.writeCollectionIdTo(output, 25, clazz);
/*  787:     */       }
/*  788:1012 */       if ((output instanceof StatefulOutput)) {
/*  789:1015 */         ((StatefulOutput)output).updateLast(strategy.COLLECTION_SCHEMA, currentSchema);
/*  790:     */       }
/*  791:1018 */       strategy.COLLECTION_SCHEMA.writeTo(output, (Collection)value);
/*  792:1019 */       return;
/*  793:     */     }
/*  794:1024 */     Schema<Object> schema = strategy.writePojoIdTo(output, 127, clazz).getSchema();
/*  795:1026 */     if ((output instanceof StatefulOutput)) {
/*  796:1029 */       ((StatefulOutput)output).updateLast(schema, currentSchema);
/*  797:     */     }
/*  798:1032 */     schema.writeTo(output, value);
/*  799:     */   }
/*  800:     */   
/*  801:     */   static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe, Input input, Output output, IdStrategy strategy)
/*  802:     */     throws IOException
/*  803:     */   {
/*  804:1038 */     int number = input.readFieldNumber(pipeSchema.wrappedSchema);
/*  805:1039 */     switch (number)
/*  806:     */     {
/*  807:     */     case 1: 
/*  808:1042 */       RuntimeFieldFactory.BOOL.transfer(pipe, input, output, number, false);
/*  809:1043 */       break;
/*  810:     */     case 2: 
/*  811:1045 */       RuntimeFieldFactory.BYTE.transfer(pipe, input, output, number, false);
/*  812:1046 */       break;
/*  813:     */     case 3: 
/*  814:1048 */       RuntimeFieldFactory.CHAR.transfer(pipe, input, output, number, false);
/*  815:1049 */       break;
/*  816:     */     case 4: 
/*  817:1051 */       RuntimeFieldFactory.SHORT.transfer(pipe, input, output, number, false);
/*  818:1052 */       break;
/*  819:     */     case 5: 
/*  820:1054 */       RuntimeFieldFactory.INT32.transfer(pipe, input, output, number, false);
/*  821:1055 */       break;
/*  822:     */     case 6: 
/*  823:1057 */       RuntimeFieldFactory.INT64.transfer(pipe, input, output, number, false);
/*  824:1058 */       break;
/*  825:     */     case 7: 
/*  826:1060 */       RuntimeFieldFactory.FLOAT.transfer(pipe, input, output, number, false);
/*  827:1061 */       break;
/*  828:     */     case 8: 
/*  829:1063 */       RuntimeFieldFactory.DOUBLE.transfer(pipe, input, output, number, false);
/*  830:1064 */       break;
/*  831:     */     case 9: 
/*  832:1066 */       RuntimeFieldFactory.STRING.transfer(pipe, input, output, number, false);
/*  833:1067 */       break;
/*  834:     */     case 10: 
/*  835:1069 */       RuntimeFieldFactory.BYTES.transfer(pipe, input, output, number, false);
/*  836:1070 */       break;
/*  837:     */     case 11: 
/*  838:1072 */       RuntimeFieldFactory.BYTE_ARRAY.transfer(pipe, input, output, number, false);
/*  839:1073 */       break;
/*  840:     */     case 12: 
/*  841:1075 */       RuntimeFieldFactory.BIGDECIMAL.transfer(pipe, input, output, number, false);
/*  842:1076 */       break;
/*  843:     */     case 13: 
/*  844:1078 */       RuntimeFieldFactory.BIGINTEGER.transfer(pipe, input, output, number, false);
/*  845:1079 */       break;
/*  846:     */     case 14: 
/*  847:1081 */       RuntimeFieldFactory.DATE.transfer(pipe, input, output, number, false);
/*  848:1082 */       break;
/*  849:     */     case 15: 
/*  850:1084 */       transferArray(pipe, input, output, number, pipeSchema, false, strategy);
/*  851:1085 */       return;
/*  852:     */     case 16: 
/*  853:1087 */       output.writeUInt32(number, input.readUInt32(), false);
/*  854:1088 */       break;
/*  855:     */     case 17: 
/*  856:1090 */       transferArray(pipe, input, output, number, pipeSchema, true, strategy);
/*  857:1091 */       return;
/*  858:     */     case 18: 
/*  859:1093 */       transferClass(pipe, input, output, number, pipeSchema, false, false, strategy);
/*  860:1094 */       break;
/*  861:     */     case 19: 
/*  862:1096 */       transferClass(pipe, input, output, number, pipeSchema, true, false, strategy);
/*  863:1097 */       break;
/*  864:     */     case 20: 
/*  865:1099 */       transferClass(pipe, input, output, number, pipeSchema, false, true, strategy);
/*  866:1100 */       break;
/*  867:     */     case 21: 
/*  868:1102 */       transferClass(pipe, input, output, number, pipeSchema, true, true, strategy);
/*  869:1103 */       break;
/*  870:     */     case 24: 
/*  871:1107 */       strategy.transferEnumId(input, output, number);
/*  872:1109 */       if (input.readFieldNumber(pipeSchema.wrappedSchema) != 1) {
/*  873:1110 */         throw new ProtostuffException("Corrupt input.");
/*  874:     */       }
/*  875:1111 */       EnumIO.transfer(pipe, input, output, 1, false, strategy);
/*  876:1112 */       break;
/*  877:     */     case 22: 
/*  878:1117 */       strategy.transferEnumId(input, output, number);
/*  879:1119 */       if ((output instanceof StatefulOutput)) {
/*  880:1122 */         ((StatefulOutput)output).updateLast(strategy.COLLECTION_PIPE_SCHEMA, pipeSchema);
/*  881:     */       }
/*  882:1125 */       Pipe.transferDirect(strategy.COLLECTION_PIPE_SCHEMA, pipe, input, output);
/*  883:1126 */       return;
/*  884:     */     case 23: 
/*  885:1131 */       strategy.transferEnumId(input, output, number);
/*  886:1133 */       if ((output instanceof StatefulOutput)) {
/*  887:1136 */         ((StatefulOutput)output).updateLast(strategy.MAP_PIPE_SCHEMA, pipeSchema);
/*  888:     */       }
/*  889:1139 */       Pipe.transferDirect(strategy.MAP_PIPE_SCHEMA, pipe, input, output);
/*  890:1140 */       return;
/*  891:     */     case 25: 
/*  892:1145 */       strategy.transferCollectionId(input, output, number);
/*  893:1147 */       if ((output instanceof StatefulOutput)) {
/*  894:1150 */         ((StatefulOutput)output).updateLast(strategy.COLLECTION_PIPE_SCHEMA, pipeSchema);
/*  895:     */       }
/*  896:1153 */       Pipe.transferDirect(strategy.COLLECTION_PIPE_SCHEMA, pipe, input, output);
/*  897:1154 */       return;
/*  898:     */     case 26: 
/*  899:1159 */       strategy.transferMapId(input, output, number);
/*  900:1161 */       if ((output instanceof StatefulOutput)) {
/*  901:1164 */         ((StatefulOutput)output).updateLast(strategy.MAP_PIPE_SCHEMA, pipeSchema);
/*  902:     */       }
/*  903:1167 */       Pipe.transferDirect(strategy.MAP_PIPE_SCHEMA, pipe, input, output);
/*  904:1168 */       return;
/*  905:     */     case 28: 
/*  906:1173 */       if (0 != input.readUInt32()) {
/*  907:1174 */         throw new ProtostuffException("Corrupt input.");
/*  908:     */       }
/*  909:1175 */       output.writeUInt32(number, 0, false);
/*  910:1177 */       if ((output instanceof StatefulOutput)) {
/*  911:1180 */         ((StatefulOutput)output).updateLast(strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, pipeSchema);
/*  912:     */       }
/*  913:1184 */       Pipe.transferDirect(strategy.POLYMORPHIC_COLLECTION_PIPE_SCHEMA, pipe, input, output);
/*  914:     */       
/*  915:1186 */       return;
/*  916:     */     case 29: 
/*  917:1191 */       if (0 != input.readUInt32()) {
/*  918:1192 */         throw new ProtostuffException("Corrupt input.");
/*  919:     */       }
/*  920:1193 */       output.writeUInt32(number, 0, false);
/*  921:1195 */       if ((output instanceof StatefulOutput)) {
/*  922:1198 */         ((StatefulOutput)output).updateLast(strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, pipeSchema);
/*  923:     */       }
/*  924:1202 */       Pipe.transferDirect(strategy.POLYMORPHIC_MAP_PIPE_SCHEMA, pipe, input, output);
/*  925:     */       
/*  926:1204 */       return;
/*  927:     */     case 30: 
/*  928:1209 */       HasDelegate<Object> hd = strategy.transferDelegateId(input, output, number);
/*  929:1211 */       if (1 != input.readFieldNumber(pipeSchema.wrappedSchema)) {
/*  930:1212 */         throw new ProtostuffException("Corrupt input.");
/*  931:     */       }
/*  932:1214 */       hd.delegate.transfer(pipe, input, output, 1, false);
/*  933:1215 */       break;
/*  934:     */     case 32: 
/*  935:1220 */       HasDelegate<Object> hd = strategy.transferDelegateId(input, output, number);
/*  936:1223 */       if ((output instanceof StatefulOutput)) {
/*  937:1226 */         ((StatefulOutput)output).updateLast(hd.genericElementSchema
/*  938:1227 */           .getPipeSchema(), pipeSchema);
/*  939:     */       }
/*  940:1231 */       Pipe.transferDirect(hd.genericElementSchema.getPipeSchema(), pipe, input, output);
/*  941:     */       
/*  942:1233 */       return;
/*  943:     */     case 33: 
/*  944:1238 */       int arrayId = input.readUInt32();int id = ArraySchemas.toInlineId(arrayId);
/*  945:     */       
/*  946:1240 */       ArraySchemas.Base arraySchema = ArraySchemas.getSchema(id, 
/*  947:1241 */         ArraySchemas.isPrimitive(arrayId), strategy);
/*  948:     */       
/*  949:1243 */       output.writeUInt32(number, arrayId, false);
/*  950:1245 */       if ((output instanceof StatefulOutput)) {
/*  951:1248 */         ((StatefulOutput)output).updateLast(arraySchema.getPipeSchema(), pipeSchema);
/*  952:     */       }
/*  953:1252 */       Pipe.transferDirect(arraySchema.getPipeSchema(), pipe, input, output);
/*  954:1253 */       return;
/*  955:     */     case 34: 
/*  956:1258 */       EnumIO<?> eio = strategy.resolveEnumFrom(input);
/*  957:     */       
/*  958:1260 */       strategy.writeEnumIdTo(output, number, eio.enumClass);
/*  959:1262 */       if ((output instanceof StatefulOutput)) {
/*  960:1265 */         ((StatefulOutput)output).updateLast(eio.genericElementSchema
/*  961:1266 */           .getPipeSchema(), pipeSchema);
/*  962:     */       }
/*  963:1270 */       Pipe.transferDirect(eio.genericElementSchema.getPipeSchema(), pipe, input, output);
/*  964:     */       
/*  965:1272 */       return;
/*  966:     */     case 35: 
/*  967:1277 */       HasSchema<Object> hs = strategy.transferPojoId(input, output, number);
/*  968:1280 */       if ((output instanceof StatefulOutput)) {
/*  969:1283 */         ((StatefulOutput)output).updateLast(hs.genericElementSchema
/*  970:1284 */           .getPipeSchema(), pipeSchema);
/*  971:     */       }
/*  972:1288 */       Pipe.transferDirect(hs.genericElementSchema.getPipeSchema(), pipe, input, output);
/*  973:     */       
/*  974:1290 */       return;
/*  975:     */     case 52: 
/*  976:1294 */       PolymorphicThrowableSchema.transferObject(pipeSchema, pipe, input, output, strategy, number);
/*  977:     */       
/*  978:1296 */       return;
/*  979:     */     case 127: 
/*  980:1300 */       Pipe.Schema<Object> derivedPipeSchema = strategy.transferPojoId(input, output, number).getPipeSchema();
/*  981:1302 */       if ((output instanceof StatefulOutput)) {
/*  982:1305 */         ((StatefulOutput)output).updateLast(derivedPipeSchema, pipeSchema);
/*  983:     */       }
/*  984:1308 */       Pipe.transferDirect(derivedPipeSchema, pipe, input, output);
/*  985:1309 */       return;
/*  986:     */     case 27: 
/*  987:     */     case 31: 
/*  988:     */     case 36: 
/*  989:     */     case 37: 
/*  990:     */     case 38: 
/*  991:     */     case 39: 
/*  992:     */     case 40: 
/*  993:     */     case 41: 
/*  994:     */     case 42: 
/*  995:     */     case 43: 
/*  996:     */     case 44: 
/*  997:     */     case 45: 
/*  998:     */     case 46: 
/*  999:     */     case 47: 
/* 1000:     */     case 48: 
/* 1001:     */     case 49: 
/* 1002:     */     case 50: 
/* 1003:     */     case 51: 
/* 1004:     */     case 53: 
/* 1005:     */     case 54: 
/* 1006:     */     case 55: 
/* 1007:     */     case 56: 
/* 1008:     */     case 57: 
/* 1009:     */     case 58: 
/* 1010:     */     case 59: 
/* 1011:     */     case 60: 
/* 1012:     */     case 61: 
/* 1013:     */     case 62: 
/* 1014:     */     case 63: 
/* 1015:     */     case 64: 
/* 1016:     */     case 65: 
/* 1017:     */     case 66: 
/* 1018:     */     case 67: 
/* 1019:     */     case 68: 
/* 1020:     */     case 69: 
/* 1021:     */     case 70: 
/* 1022:     */     case 71: 
/* 1023:     */     case 72: 
/* 1024:     */     case 73: 
/* 1025:     */     case 74: 
/* 1026:     */     case 75: 
/* 1027:     */     case 76: 
/* 1028:     */     case 77: 
/* 1029:     */     case 78: 
/* 1030:     */     case 79: 
/* 1031:     */     case 80: 
/* 1032:     */     case 81: 
/* 1033:     */     case 82: 
/* 1034:     */     case 83: 
/* 1035:     */     case 84: 
/* 1036:     */     case 85: 
/* 1037:     */     case 86: 
/* 1038:     */     case 87: 
/* 1039:     */     case 88: 
/* 1040:     */     case 89: 
/* 1041:     */     case 90: 
/* 1042:     */     case 91: 
/* 1043:     */     case 92: 
/* 1044:     */     case 93: 
/* 1045:     */     case 94: 
/* 1046:     */     case 95: 
/* 1047:     */     case 96: 
/* 1048:     */     case 97: 
/* 1049:     */     case 98: 
/* 1050:     */     case 99: 
/* 1051:     */     case 100: 
/* 1052:     */     case 101: 
/* 1053:     */     case 102: 
/* 1054:     */     case 103: 
/* 1055:     */     case 104: 
/* 1056:     */     case 105: 
/* 1057:     */     case 106: 
/* 1058:     */     case 107: 
/* 1059:     */     case 108: 
/* 1060:     */     case 109: 
/* 1061:     */     case 110: 
/* 1062:     */     case 111: 
/* 1063:     */     case 112: 
/* 1064:     */     case 113: 
/* 1065:     */     case 114: 
/* 1066:     */     case 115: 
/* 1067:     */     case 116: 
/* 1068:     */     case 117: 
/* 1069:     */     case 118: 
/* 1070:     */     case 119: 
/* 1071:     */     case 120: 
/* 1072:     */     case 121: 
/* 1073:     */     case 122: 
/* 1074:     */     case 123: 
/* 1075:     */     case 124: 
/* 1076:     */     case 125: 
/* 1077:     */     case 126: 
/* 1078:     */     default: 
/* 1079:1311 */       throw new ProtostuffException("Corrupt input.  Unknown field number: " + number);
/* 1080:     */     }
/* 1081:1314 */     if (input.readFieldNumber(pipeSchema.wrappedSchema) != 0) {
/* 1082:1315 */       throw new ProtostuffException("Corrupt input.");
/* 1083:     */     }
/* 1084:     */   }
/* 1085:     */   
/* 1086:     */   static final class ArrayWrapper
/* 1087:     */     implements Collection<Object>
/* 1088:     */   {
/* 1089:     */     final Object array;
/* 1090:1324 */     int offset = 0;
/* 1091:     */     
/* 1092:     */     ArrayWrapper(Object array)
/* 1093:     */     {
/* 1094:1328 */       this.array = array;
/* 1095:     */     }
/* 1096:     */     
/* 1097:     */     public boolean add(Object value)
/* 1098:     */     {
/* 1099:1334 */       Array.set(this.array, this.offset++, value);
/* 1100:1335 */       return true;
/* 1101:     */     }
/* 1102:     */     
/* 1103:     */     public boolean addAll(Collection<? extends Object> arg0)
/* 1104:     */     {
/* 1105:1341 */       throw new UnsupportedOperationException();
/* 1106:     */     }
/* 1107:     */     
/* 1108:     */     public void clear()
/* 1109:     */     {
/* 1110:1347 */       throw new UnsupportedOperationException();
/* 1111:     */     }
/* 1112:     */     
/* 1113:     */     public boolean contains(Object arg0)
/* 1114:     */     {
/* 1115:1353 */       throw new UnsupportedOperationException();
/* 1116:     */     }
/* 1117:     */     
/* 1118:     */     public boolean containsAll(Collection<?> arg0)
/* 1119:     */     {
/* 1120:1359 */       throw new UnsupportedOperationException();
/* 1121:     */     }
/* 1122:     */     
/* 1123:     */     public boolean isEmpty()
/* 1124:     */     {
/* 1125:1365 */       throw new UnsupportedOperationException();
/* 1126:     */     }
/* 1127:     */     
/* 1128:     */     public Iterator<Object> iterator()
/* 1129:     */     {
/* 1130:1371 */       throw new UnsupportedOperationException();
/* 1131:     */     }
/* 1132:     */     
/* 1133:     */     public boolean remove(Object arg0)
/* 1134:     */     {
/* 1135:1377 */       throw new UnsupportedOperationException();
/* 1136:     */     }
/* 1137:     */     
/* 1138:     */     public boolean removeAll(Collection<?> arg0)
/* 1139:     */     {
/* 1140:1383 */       throw new UnsupportedOperationException();
/* 1141:     */     }
/* 1142:     */     
/* 1143:     */     public boolean retainAll(Collection<?> arg0)
/* 1144:     */     {
/* 1145:1389 */       throw new UnsupportedOperationException();
/* 1146:     */     }
/* 1147:     */     
/* 1148:     */     public int size()
/* 1149:     */     {
/* 1150:1395 */       throw new UnsupportedOperationException();
/* 1151:     */     }
/* 1152:     */     
/* 1153:     */     public Object[] toArray()
/* 1154:     */     {
/* 1155:1401 */       throw new UnsupportedOperationException();
/* 1156:     */     }
/* 1157:     */     
/* 1158:     */     public <T> T[] toArray(T[] arg0)
/* 1159:     */     {
/* 1160:1407 */       throw new UnsupportedOperationException();
/* 1161:     */     }
/* 1162:     */   }
/* 1163:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.ObjectSchema
 * JD-Core Version:    0.7.0.1
 */