/*    1:     */ package io.protostuff.runtime;
/*    2:     */ 
/*    3:     */ import io.protostuff.ByteString;
/*    4:     */ import io.protostuff.GraphInput;
/*    5:     */ import io.protostuff.Input;
/*    6:     */ import io.protostuff.Morph;
/*    7:     */ import io.protostuff.Output;
/*    8:     */ import io.protostuff.Pipe;
/*    9:     */ import io.protostuff.Schema;
/*   10:     */ import io.protostuff.Tag;
/*   11:     */ import io.protostuff.WireFormat.FieldType;
/*   12:     */ import java.io.IOException;
/*   13:     */ import java.math.BigDecimal;
/*   14:     */ import java.math.BigInteger;
/*   15:     */ import java.util.Date;
/*   16:     */ 
/*   17:     */ public final class RuntimeReflectionFieldFactory
/*   18:     */ {
/*   19:  64 */   public static final RuntimeFieldFactory<Character> CHAR = new RuntimeFieldFactory(3)
/*   20:     */   {
/*   21:     */     public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*   22:     */     {
/*   23:  71 */       final boolean primitive = f.getType().isPrimitive();
/*   24:  72 */       new Field(WireFormat.FieldType.UINT32, number, name, 
/*   25:  73 */         (Tag)f.getAnnotation(Tag.class))
/*   26:     */         {
/*   27:     */           public void mergeFrom(Input input, T message)
/*   28:     */             throws IOException
/*   29:     */           {
/*   30:     */             try
/*   31:     */             {
/*   32:  84 */               if (primitive) {
/*   33:  85 */                 f.setChar(message, (char)input.readUInt32());
/*   34:     */               } else {
/*   35:  87 */                 f.set(message, Character.valueOf(
/*   36:  88 */                   (char)input.readUInt32()));
/*   37:     */               }
/*   38:     */             }
/*   39:     */             catch (Exception e)
/*   40:     */             {
/*   41:  92 */               throw new RuntimeException(e);
/*   42:     */             }
/*   43:     */           }
/*   44:     */           
/*   45:     */           public void writeTo(Output output, T message)
/*   46:     */             throws IOException
/*   47:     */           {
/*   48:     */             try
/*   49:     */             {
/*   50: 101 */               if (primitive)
/*   51:     */               {
/*   52: 102 */                 output.writeUInt32(this.number, f.getChar(message), false);
/*   53:     */               }
/*   54:     */               else
/*   55:     */               {
/*   56: 106 */                 Character value = (Character)f.get(message);
/*   57: 107 */                 if (value != null) {
/*   58: 108 */                   output.writeUInt32(this.number, value.charValue(), false);
/*   59:     */                 }
/*   60:     */               }
/*   61:     */             }
/*   62:     */             catch (Exception e)
/*   63:     */             {
/*   64: 114 */               throw new RuntimeException(e);
/*   65:     */             }
/*   66:     */           }
/*   67:     */           
/*   68:     */           public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*   69:     */             throws IOException
/*   70:     */           {
/*   71: 121 */             output.writeUInt32(this.number, input.readUInt32(), repeated);
/*   72:     */           }
/*   73:     */         };
/*   74:     */       }
/*   75:     */       
/*   76:     */       public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*   77:     */         throws IOException
/*   78:     */       {
/*   79: 130 */         output.writeUInt32(number, input.readUInt32(), repeated);
/*   80:     */       }
/*   81:     */       
/*   82:     */       public Character readFrom(Input input)
/*   83:     */         throws IOException
/*   84:     */       {
/*   85: 136 */         return Character.valueOf((char)input.readUInt32());
/*   86:     */       }
/*   87:     */       
/*   88:     */       public void writeTo(Output output, int number, Character value, boolean repeated)
/*   89:     */         throws IOException
/*   90:     */       {
/*   91: 143 */         output.writeUInt32(number, value.charValue(), repeated);
/*   92:     */       }
/*   93:     */       
/*   94:     */       public WireFormat.FieldType getFieldType()
/*   95:     */       {
/*   96: 149 */         return WireFormat.FieldType.UINT32;
/*   97:     */       }
/*   98:     */       
/*   99:     */       public Class<?> typeClass()
/*  100:     */       {
/*  101: 155 */         return Character.class;
/*  102:     */       }
/*  103:     */     };
/*  104: 159 */     public static final RuntimeFieldFactory<Short> SHORT = new RuntimeFieldFactory(4)
/*  105:     */     {
/*  106:     */       public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  107:     */       {
/*  108: 166 */         final boolean primitive = f.getType().isPrimitive();
/*  109: 167 */         new Field(WireFormat.FieldType.UINT32, number, name, 
/*  110: 168 */           (Tag)f.getAnnotation(Tag.class))
/*  111:     */           {
/*  112:     */             public void mergeFrom(Input input, T message)
/*  113:     */               throws IOException
/*  114:     */             {
/*  115:     */               try
/*  116:     */               {
/*  117: 180 */                 if (primitive) {
/*  118: 181 */                   f.setShort(message, (short)input.readUInt32());
/*  119:     */                 } else {
/*  120: 183 */                   f.set(message, 
/*  121: 184 */                     Short.valueOf((short)input.readUInt32()));
/*  122:     */                 }
/*  123:     */               }
/*  124:     */               catch (Exception e)
/*  125:     */               {
/*  126: 188 */                 throw new RuntimeException(e);
/*  127:     */               }
/*  128:     */             }
/*  129:     */             
/*  130:     */             public void writeTo(Output output, T message)
/*  131:     */               throws IOException
/*  132:     */             {
/*  133:     */               try
/*  134:     */               {
/*  135: 198 */                 if (primitive)
/*  136:     */                 {
/*  137: 199 */                   output.writeUInt32(this.number, f.getShort(message), false);
/*  138:     */                 }
/*  139:     */                 else
/*  140:     */                 {
/*  141: 203 */                   Short value = (Short)f.get(message);
/*  142: 204 */                   if (value != null) {
/*  143: 205 */                     output.writeUInt32(this.number, value.shortValue(), false);
/*  144:     */                   }
/*  145:     */                 }
/*  146:     */               }
/*  147:     */               catch (Exception e)
/*  148:     */               {
/*  149: 211 */                 throw new RuntimeException(e);
/*  150:     */               }
/*  151:     */             }
/*  152:     */             
/*  153:     */             public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  154:     */               throws IOException
/*  155:     */             {
/*  156: 219 */               output.writeUInt32(this.number, input.readUInt32(), repeated);
/*  157:     */             }
/*  158:     */           };
/*  159:     */         }
/*  160:     */         
/*  161:     */         public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  162:     */           throws IOException
/*  163:     */         {
/*  164: 228 */           output.writeUInt32(number, input.readUInt32(), repeated);
/*  165:     */         }
/*  166:     */         
/*  167:     */         public Short readFrom(Input input)
/*  168:     */           throws IOException
/*  169:     */         {
/*  170: 234 */           return Short.valueOf((short)input.readUInt32());
/*  171:     */         }
/*  172:     */         
/*  173:     */         public void writeTo(Output output, int number, Short value, boolean repeated)
/*  174:     */           throws IOException
/*  175:     */         {
/*  176: 241 */           output.writeUInt32(number, value.shortValue(), repeated);
/*  177:     */         }
/*  178:     */         
/*  179:     */         public WireFormat.FieldType getFieldType()
/*  180:     */         {
/*  181: 247 */           return WireFormat.FieldType.UINT32;
/*  182:     */         }
/*  183:     */         
/*  184:     */         public Class<?> typeClass()
/*  185:     */         {
/*  186: 253 */           return Short.class;
/*  187:     */         }
/*  188:     */       };
/*  189: 257 */       public static final RuntimeFieldFactory<Byte> BYTE = new RuntimeFieldFactory(2)
/*  190:     */       {
/*  191:     */         public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  192:     */         {
/*  193: 264 */           final boolean primitive = f.getType().isPrimitive();
/*  194: 265 */           new Field(WireFormat.FieldType.UINT32, number, name, 
/*  195: 266 */             (Tag)f.getAnnotation(Tag.class))
/*  196:     */             {
/*  197:     */               public void mergeFrom(Input input, T message)
/*  198:     */                 throws IOException
/*  199:     */               {
/*  200:     */                 try
/*  201:     */                 {
/*  202: 277 */                   if (primitive) {
/*  203: 278 */                     f.setByte(message, (byte)input.readUInt32());
/*  204:     */                   } else {
/*  205: 280 */                     f.set(message, 
/*  206: 281 */                       Byte.valueOf((byte)input.readUInt32()));
/*  207:     */                   }
/*  208:     */                 }
/*  209:     */                 catch (Exception e)
/*  210:     */                 {
/*  211: 285 */                   throw new RuntimeException(e);
/*  212:     */                 }
/*  213:     */               }
/*  214:     */               
/*  215:     */               public void writeTo(Output output, T message)
/*  216:     */                 throws IOException
/*  217:     */               {
/*  218:     */                 try
/*  219:     */                 {
/*  220: 294 */                   if (primitive)
/*  221:     */                   {
/*  222: 295 */                     output.writeUInt32(this.number, f.getByte(message), false);
/*  223:     */                   }
/*  224:     */                   else
/*  225:     */                   {
/*  226: 299 */                     Byte value = (Byte)f.get(message);
/*  227: 300 */                     if (value != null) {
/*  228: 301 */                       output.writeUInt32(this.number, value.byteValue(), false);
/*  229:     */                     }
/*  230:     */                   }
/*  231:     */                 }
/*  232:     */                 catch (Exception e)
/*  233:     */                 {
/*  234: 307 */                   throw new RuntimeException(e);
/*  235:     */                 }
/*  236:     */               }
/*  237:     */               
/*  238:     */               public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  239:     */                 throws IOException
/*  240:     */               {
/*  241: 314 */                 output.writeUInt32(this.number, input.readUInt32(), repeated);
/*  242:     */               }
/*  243:     */             };
/*  244:     */           }
/*  245:     */           
/*  246:     */           public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  247:     */             throws IOException
/*  248:     */           {
/*  249: 323 */             output.writeUInt32(number, input.readUInt32(), repeated);
/*  250:     */           }
/*  251:     */           
/*  252:     */           public Byte readFrom(Input input)
/*  253:     */             throws IOException
/*  254:     */           {
/*  255: 329 */             return Byte.valueOf((byte)input.readUInt32());
/*  256:     */           }
/*  257:     */           
/*  258:     */           public void writeTo(Output output, int number, Byte value, boolean repeated)
/*  259:     */             throws IOException
/*  260:     */           {
/*  261: 336 */             output.writeUInt32(number, value.byteValue(), repeated);
/*  262:     */           }
/*  263:     */           
/*  264:     */           public WireFormat.FieldType getFieldType()
/*  265:     */           {
/*  266: 342 */             return WireFormat.FieldType.UINT32;
/*  267:     */           }
/*  268:     */           
/*  269:     */           public Class<?> typeClass()
/*  270:     */           {
/*  271: 348 */             return Byte.class;
/*  272:     */           }
/*  273:     */         };
/*  274: 352 */         public static final RuntimeFieldFactory<Integer> INT32 = new RuntimeFieldFactory(5)
/*  275:     */         {
/*  276:     */           public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  277:     */           {
/*  278: 359 */             final boolean primitive = f.getType().isPrimitive();
/*  279: 360 */             new Field(WireFormat.FieldType.INT32, number, name, 
/*  280: 361 */               (Tag)f.getAnnotation(Tag.class))
/*  281:     */               {
/*  282:     */                 public void mergeFrom(Input input, T message)
/*  283:     */                   throws IOException
/*  284:     */                 {
/*  285:     */                   try
/*  286:     */                   {
/*  287: 372 */                     if (primitive) {
/*  288: 373 */                       f.setInt(message, input.readInt32());
/*  289:     */                     } else {
/*  290: 375 */                       f.set(message, Integer.valueOf(input.readInt32()));
/*  291:     */                     }
/*  292:     */                   }
/*  293:     */                   catch (Exception e)
/*  294:     */                   {
/*  295: 379 */                     throw new RuntimeException(e);
/*  296:     */                   }
/*  297:     */                 }
/*  298:     */                 
/*  299:     */                 public void writeTo(Output output, T message)
/*  300:     */                   throws IOException
/*  301:     */                 {
/*  302:     */                   try
/*  303:     */                   {
/*  304: 388 */                     if (primitive)
/*  305:     */                     {
/*  306: 389 */                       output.writeInt32(this.number, f.getInt(message), false);
/*  307:     */                     }
/*  308:     */                     else
/*  309:     */                     {
/*  310: 392 */                       Integer value = (Integer)f.get(message);
/*  311: 393 */                       if (value != null) {
/*  312: 394 */                         output.writeInt32(this.number, value.intValue(), false);
/*  313:     */                       }
/*  314:     */                     }
/*  315:     */                   }
/*  316:     */                   catch (Exception e)
/*  317:     */                   {
/*  318: 400 */                     throw new RuntimeException(e);
/*  319:     */                   }
/*  320:     */                 }
/*  321:     */                 
/*  322:     */                 public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  323:     */                   throws IOException
/*  324:     */                 {
/*  325: 407 */                   output.writeInt32(this.number, input.readInt32(), repeated);
/*  326:     */                 }
/*  327:     */               };
/*  328:     */             }
/*  329:     */             
/*  330:     */             public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  331:     */               throws IOException
/*  332:     */             {
/*  333: 416 */               output.writeInt32(number, input.readInt32(), repeated);
/*  334:     */             }
/*  335:     */             
/*  336:     */             public Integer readFrom(Input input)
/*  337:     */               throws IOException
/*  338:     */             {
/*  339: 422 */               return Integer.valueOf(input.readInt32());
/*  340:     */             }
/*  341:     */             
/*  342:     */             public void writeTo(Output output, int number, Integer value, boolean repeated)
/*  343:     */               throws IOException
/*  344:     */             {
/*  345: 429 */               output.writeInt32(number, value.intValue(), repeated);
/*  346:     */             }
/*  347:     */             
/*  348:     */             public WireFormat.FieldType getFieldType()
/*  349:     */             {
/*  350: 435 */               return WireFormat.FieldType.INT32;
/*  351:     */             }
/*  352:     */             
/*  353:     */             public Class<?> typeClass()
/*  354:     */             {
/*  355: 441 */               return Integer.class;
/*  356:     */             }
/*  357:     */           };
/*  358: 445 */           public static final RuntimeFieldFactory<Long> INT64 = new RuntimeFieldFactory(6)
/*  359:     */           {
/*  360:     */             public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  361:     */             {
/*  362: 452 */               final boolean primitive = f.getType().isPrimitive();
/*  363: 453 */               new Field(WireFormat.FieldType.INT64, number, name, 
/*  364: 454 */                 (Tag)f.getAnnotation(Tag.class))
/*  365:     */                 {
/*  366:     */                   public void mergeFrom(Input input, T message)
/*  367:     */                     throws IOException
/*  368:     */                   {
/*  369:     */                     try
/*  370:     */                     {
/*  371: 465 */                       if (primitive) {
/*  372: 466 */                         f.setLong(message, input.readInt64());
/*  373:     */                       } else {
/*  374: 468 */                         f.set(message, Long.valueOf(input.readInt64()));
/*  375:     */                       }
/*  376:     */                     }
/*  377:     */                     catch (Exception e)
/*  378:     */                     {
/*  379: 472 */                       throw new RuntimeException(e);
/*  380:     */                     }
/*  381:     */                   }
/*  382:     */                   
/*  383:     */                   public void writeTo(Output output, T message)
/*  384:     */                     throws IOException
/*  385:     */                   {
/*  386:     */                     try
/*  387:     */                     {
/*  388: 481 */                       if (primitive)
/*  389:     */                       {
/*  390: 482 */                         output.writeInt64(this.number, f.getLong(message), false);
/*  391:     */                       }
/*  392:     */                       else
/*  393:     */                       {
/*  394: 485 */                         Long value = (Long)f.get(message);
/*  395: 486 */                         if (value != null) {
/*  396: 487 */                           output.writeInt64(this.number, value.longValue(), false);
/*  397:     */                         }
/*  398:     */                       }
/*  399:     */                     }
/*  400:     */                     catch (Exception e)
/*  401:     */                     {
/*  402: 493 */                       throw new RuntimeException(e);
/*  403:     */                     }
/*  404:     */                   }
/*  405:     */                   
/*  406:     */                   public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  407:     */                     throws IOException
/*  408:     */                   {
/*  409: 500 */                     output.writeInt64(this.number, input.readInt64(), repeated);
/*  410:     */                   }
/*  411:     */                 };
/*  412:     */               }
/*  413:     */               
/*  414:     */               public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  415:     */                 throws IOException
/*  416:     */               {
/*  417: 509 */                 output.writeInt64(number, input.readInt64(), repeated);
/*  418:     */               }
/*  419:     */               
/*  420:     */               public Long readFrom(Input input)
/*  421:     */                 throws IOException
/*  422:     */               {
/*  423: 515 */                 return Long.valueOf(input.readInt64());
/*  424:     */               }
/*  425:     */               
/*  426:     */               public void writeTo(Output output, int number, Long value, boolean repeated)
/*  427:     */                 throws IOException
/*  428:     */               {
/*  429: 522 */                 output.writeInt64(number, value.longValue(), repeated);
/*  430:     */               }
/*  431:     */               
/*  432:     */               public WireFormat.FieldType getFieldType()
/*  433:     */               {
/*  434: 528 */                 return WireFormat.FieldType.INT64;
/*  435:     */               }
/*  436:     */               
/*  437:     */               public Class<?> typeClass()
/*  438:     */               {
/*  439: 534 */                 return Long.class;
/*  440:     */               }
/*  441:     */             };
/*  442: 538 */             public static final RuntimeFieldFactory<Float> FLOAT = new RuntimeFieldFactory(7)
/*  443:     */             {
/*  444:     */               public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  445:     */               {
/*  446: 545 */                 final boolean primitive = f.getType().isPrimitive();
/*  447: 546 */                 new Field(WireFormat.FieldType.FLOAT, number, name, 
/*  448: 547 */                   (Tag)f.getAnnotation(Tag.class))
/*  449:     */                   {
/*  450:     */                     public void mergeFrom(Input input, T message)
/*  451:     */                       throws IOException
/*  452:     */                     {
/*  453:     */                       try
/*  454:     */                       {
/*  455: 558 */                         if (primitive) {
/*  456: 559 */                           f.setFloat(message, input.readFloat());
/*  457:     */                         } else {
/*  458: 561 */                           f.set(message, new Float(input.readFloat()));
/*  459:     */                         }
/*  460:     */                       }
/*  461:     */                       catch (Exception e)
/*  462:     */                       {
/*  463: 565 */                         throw new RuntimeException(e);
/*  464:     */                       }
/*  465:     */                     }
/*  466:     */                     
/*  467:     */                     public void writeTo(Output output, T message)
/*  468:     */                       throws IOException
/*  469:     */                     {
/*  470:     */                       try
/*  471:     */                       {
/*  472: 574 */                         if (primitive)
/*  473:     */                         {
/*  474: 575 */                           output.writeFloat(this.number, f.getFloat(message), false);
/*  475:     */                         }
/*  476:     */                         else
/*  477:     */                         {
/*  478: 579 */                           Float value = (Float)f.get(message);
/*  479: 580 */                           if (value != null) {
/*  480: 581 */                             output.writeFloat(this.number, value.floatValue(), false);
/*  481:     */                           }
/*  482:     */                         }
/*  483:     */                       }
/*  484:     */                       catch (Exception e)
/*  485:     */                       {
/*  486: 587 */                         throw new RuntimeException(e);
/*  487:     */                       }
/*  488:     */                     }
/*  489:     */                     
/*  490:     */                     public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  491:     */                       throws IOException
/*  492:     */                     {
/*  493: 594 */                       output.writeFloat(this.number, input.readFloat(), repeated);
/*  494:     */                     }
/*  495:     */                   };
/*  496:     */                 }
/*  497:     */                 
/*  498:     */                 public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  499:     */                   throws IOException
/*  500:     */                 {
/*  501: 603 */                   output.writeFloat(number, input.readFloat(), repeated);
/*  502:     */                 }
/*  503:     */                 
/*  504:     */                 public Float readFrom(Input input)
/*  505:     */                   throws IOException
/*  506:     */                 {
/*  507: 609 */                   return new Float(input.readFloat());
/*  508:     */                 }
/*  509:     */                 
/*  510:     */                 public void writeTo(Output output, int number, Float value, boolean repeated)
/*  511:     */                   throws IOException
/*  512:     */                 {
/*  513: 616 */                   output.writeFloat(number, value.floatValue(), repeated);
/*  514:     */                 }
/*  515:     */                 
/*  516:     */                 public WireFormat.FieldType getFieldType()
/*  517:     */                 {
/*  518: 622 */                   return WireFormat.FieldType.FLOAT;
/*  519:     */                 }
/*  520:     */                 
/*  521:     */                 public Class<?> typeClass()
/*  522:     */                 {
/*  523: 628 */                   return Float.class;
/*  524:     */                 }
/*  525:     */               };
/*  526: 632 */               public static final RuntimeFieldFactory<Double> DOUBLE = new RuntimeFieldFactory(8)
/*  527:     */               {
/*  528:     */                 public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  529:     */                 {
/*  530: 639 */                   final boolean primitive = f.getType().isPrimitive();
/*  531: 640 */                   new Field(WireFormat.FieldType.DOUBLE, number, name, 
/*  532: 641 */                     (Tag)f.getAnnotation(Tag.class))
/*  533:     */                     {
/*  534:     */                       public void mergeFrom(Input input, T message)
/*  535:     */                         throws IOException
/*  536:     */                       {
/*  537:     */                         try
/*  538:     */                         {
/*  539: 652 */                           if (primitive) {
/*  540: 653 */                             f.setDouble(message, input.readDouble());
/*  541:     */                           } else {
/*  542: 655 */                             f.set(message, new Double(input.readDouble()));
/*  543:     */                           }
/*  544:     */                         }
/*  545:     */                         catch (Exception e)
/*  546:     */                         {
/*  547: 659 */                           throw new RuntimeException(e);
/*  548:     */                         }
/*  549:     */                       }
/*  550:     */                       
/*  551:     */                       public void writeTo(Output output, T message)
/*  552:     */                         throws IOException
/*  553:     */                       {
/*  554:     */                         try
/*  555:     */                         {
/*  556: 668 */                           if (primitive)
/*  557:     */                           {
/*  558: 669 */                             output.writeDouble(this.number, f.getDouble(message), false);
/*  559:     */                           }
/*  560:     */                           else
/*  561:     */                           {
/*  562: 673 */                             Double value = (Double)f.get(message);
/*  563: 674 */                             if (value != null) {
/*  564: 675 */                               output.writeDouble(this.number, value.doubleValue(), false);
/*  565:     */                             }
/*  566:     */                           }
/*  567:     */                         }
/*  568:     */                         catch (Exception e)
/*  569:     */                         {
/*  570: 681 */                           throw new RuntimeException(e);
/*  571:     */                         }
/*  572:     */                       }
/*  573:     */                       
/*  574:     */                       public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  575:     */                         throws IOException
/*  576:     */                       {
/*  577: 688 */                         output.writeDouble(this.number, input.readDouble(), repeated);
/*  578:     */                       }
/*  579:     */                     };
/*  580:     */                   }
/*  581:     */                   
/*  582:     */                   public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  583:     */                     throws IOException
/*  584:     */                   {
/*  585: 697 */                     output.writeDouble(number, input.readDouble(), repeated);
/*  586:     */                   }
/*  587:     */                   
/*  588:     */                   public Double readFrom(Input input)
/*  589:     */                     throws IOException
/*  590:     */                   {
/*  591: 703 */                     return new Double(input.readDouble());
/*  592:     */                   }
/*  593:     */                   
/*  594:     */                   public void writeTo(Output output, int number, Double value, boolean repeated)
/*  595:     */                     throws IOException
/*  596:     */                   {
/*  597: 710 */                     output.writeDouble(number, value.doubleValue(), repeated);
/*  598:     */                   }
/*  599:     */                   
/*  600:     */                   public WireFormat.FieldType getFieldType()
/*  601:     */                   {
/*  602: 716 */                     return WireFormat.FieldType.DOUBLE;
/*  603:     */                   }
/*  604:     */                   
/*  605:     */                   public Class<?> typeClass()
/*  606:     */                   {
/*  607: 722 */                     return Double.class;
/*  608:     */                   }
/*  609:     */                 };
/*  610: 726 */                 public static final RuntimeFieldFactory<Boolean> BOOL = new RuntimeFieldFactory(1)
/*  611:     */                 {
/*  612:     */                   public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  613:     */                   {
/*  614: 733 */                     final boolean primitive = f.getType().isPrimitive();
/*  615: 734 */                     new Field(WireFormat.FieldType.BOOL, number, name, 
/*  616: 735 */                       (Tag)f.getAnnotation(Tag.class))
/*  617:     */                       {
/*  618:     */                         public void mergeFrom(Input input, T message)
/*  619:     */                           throws IOException
/*  620:     */                         {
/*  621:     */                           try
/*  622:     */                           {
/*  623: 746 */                             if (primitive) {
/*  624: 747 */                               f.setBoolean(message, input.readBool());
/*  625:     */                             } else {
/*  626: 749 */                               f.set(message, input.readBool() ? Boolean.TRUE : Boolean.FALSE);
/*  627:     */                             }
/*  628:     */                           }
/*  629:     */                           catch (Exception e)
/*  630:     */                           {
/*  631: 754 */                             throw new RuntimeException(e);
/*  632:     */                           }
/*  633:     */                         }
/*  634:     */                         
/*  635:     */                         public void writeTo(Output output, T message)
/*  636:     */                           throws IOException
/*  637:     */                         {
/*  638:     */                           try
/*  639:     */                           {
/*  640: 763 */                             if (primitive)
/*  641:     */                             {
/*  642: 764 */                               output.writeBool(this.number, f.getBoolean(message), false);
/*  643:     */                             }
/*  644:     */                             else
/*  645:     */                             {
/*  646: 768 */                               Boolean value = (Boolean)f.get(message);
/*  647: 769 */                               if (value != null) {
/*  648: 770 */                                 output.writeBool(this.number, value.booleanValue(), false);
/*  649:     */                               }
/*  650:     */                             }
/*  651:     */                           }
/*  652:     */                           catch (Exception e)
/*  653:     */                           {
/*  654: 776 */                             throw new RuntimeException(e);
/*  655:     */                           }
/*  656:     */                         }
/*  657:     */                         
/*  658:     */                         public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  659:     */                           throws IOException
/*  660:     */                         {
/*  661: 783 */                           output.writeBool(this.number, input.readBool(), repeated);
/*  662:     */                         }
/*  663:     */                       };
/*  664:     */                     }
/*  665:     */                     
/*  666:     */                     public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  667:     */                       throws IOException
/*  668:     */                     {
/*  669: 792 */                       output.writeBool(number, input.readBool(), repeated);
/*  670:     */                     }
/*  671:     */                     
/*  672:     */                     public Boolean readFrom(Input input)
/*  673:     */                       throws IOException
/*  674:     */                     {
/*  675: 798 */                       return input.readBool() ? Boolean.TRUE : Boolean.FALSE;
/*  676:     */                     }
/*  677:     */                     
/*  678:     */                     public void writeTo(Output output, int number, Boolean value, boolean repeated)
/*  679:     */                       throws IOException
/*  680:     */                     {
/*  681: 805 */                       output.writeBool(number, value.booleanValue(), repeated);
/*  682:     */                     }
/*  683:     */                     
/*  684:     */                     public WireFormat.FieldType getFieldType()
/*  685:     */                     {
/*  686: 811 */                       return WireFormat.FieldType.BOOL;
/*  687:     */                     }
/*  688:     */                     
/*  689:     */                     public Class<?> typeClass()
/*  690:     */                     {
/*  691: 817 */                       return Boolean.class;
/*  692:     */                     }
/*  693:     */                   };
/*  694: 821 */                   public static final RuntimeFieldFactory<String> STRING = new RuntimeFieldFactory(9)
/*  695:     */                   {
/*  696:     */                     public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  697:     */                     {
/*  698: 828 */                       new Field(WireFormat.FieldType.STRING, number, name, 
/*  699: 829 */                         (Tag)f.getAnnotation(Tag.class))
/*  700:     */                         {
/*  701:     */                           public void mergeFrom(Input input, T message)
/*  702:     */                             throws IOException
/*  703:     */                           {
/*  704:     */                             try
/*  705:     */                             {
/*  706: 840 */                               f.set(message, input.readString());
/*  707:     */                             }
/*  708:     */                             catch (Exception e)
/*  709:     */                             {
/*  710: 844 */                               throw new RuntimeException(e);
/*  711:     */                             }
/*  712:     */                           }
/*  713:     */                           
/*  714:     */                           public void writeTo(Output output, T message)
/*  715:     */                             throws IOException
/*  716:     */                           {
/*  717:     */                             try
/*  718:     */                             {
/*  719: 853 */                               CharSequence value = (CharSequence)f.get(message);
/*  720: 854 */                               if (value != null) {
/*  721: 855 */                                 output.writeString(this.number, value, false);
/*  722:     */                               }
/*  723:     */                             }
/*  724:     */                             catch (Exception e)
/*  725:     */                             {
/*  726: 859 */                               throw new RuntimeException(e);
/*  727:     */                             }
/*  728:     */                           }
/*  729:     */                           
/*  730:     */                           public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  731:     */                             throws IOException
/*  732:     */                           {
/*  733: 866 */                             input.transferByteRangeTo(output, true, this.number, repeated);
/*  734:     */                           }
/*  735:     */                         };
/*  736:     */                       }
/*  737:     */                       
/*  738:     */                       public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  739:     */                         throws IOException
/*  740:     */                       {
/*  741: 875 */                         input.transferByteRangeTo(output, true, number, repeated);
/*  742:     */                       }
/*  743:     */                       
/*  744:     */                       public String readFrom(Input input)
/*  745:     */                         throws IOException
/*  746:     */                       {
/*  747: 881 */                         return input.readString();
/*  748:     */                       }
/*  749:     */                       
/*  750:     */                       public void writeTo(Output output, int number, String value, boolean repeated)
/*  751:     */                         throws IOException
/*  752:     */                       {
/*  753: 888 */                         output.writeString(number, value, repeated);
/*  754:     */                       }
/*  755:     */                       
/*  756:     */                       public WireFormat.FieldType getFieldType()
/*  757:     */                       {
/*  758: 894 */                         return WireFormat.FieldType.STRING;
/*  759:     */                       }
/*  760:     */                       
/*  761:     */                       public Class<?> typeClass()
/*  762:     */                       {
/*  763: 900 */                         return String.class;
/*  764:     */                       }
/*  765:     */                     };
/*  766: 904 */                     public static final RuntimeFieldFactory<ByteString> BYTES = new RuntimeFieldFactory(10)
/*  767:     */                     {
/*  768:     */                       public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  769:     */                       {
/*  770: 911 */                         new Field(WireFormat.FieldType.BYTES, number, name, 
/*  771: 912 */                           (Tag)f.getAnnotation(Tag.class))
/*  772:     */                           {
/*  773:     */                             public void mergeFrom(Input input, T message)
/*  774:     */                               throws IOException
/*  775:     */                             {
/*  776:     */                               try
/*  777:     */                               {
/*  778: 924 */                                 f.set(message, input.readBytes());
/*  779:     */                               }
/*  780:     */                               catch (Exception e)
/*  781:     */                               {
/*  782: 928 */                                 throw new RuntimeException(e);
/*  783:     */                               }
/*  784:     */                             }
/*  785:     */                             
/*  786:     */                             public void writeTo(Output output, T message)
/*  787:     */                               throws IOException
/*  788:     */                             {
/*  789:     */                               try
/*  790:     */                               {
/*  791: 938 */                                 ByteString bs = (ByteString)f.get(message);
/*  792: 939 */                                 if (bs != null) {
/*  793: 940 */                                   output.writeBytes(this.number, bs, false);
/*  794:     */                                 }
/*  795:     */                               }
/*  796:     */                               catch (Exception e)
/*  797:     */                               {
/*  798: 944 */                                 throw new RuntimeException(e);
/*  799:     */                               }
/*  800:     */                             }
/*  801:     */                             
/*  802:     */                             public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  803:     */                               throws IOException
/*  804:     */                             {
/*  805: 952 */                               input.transferByteRangeTo(output, false, this.number, repeated);
/*  806:     */                             }
/*  807:     */                           };
/*  808:     */                         }
/*  809:     */                         
/*  810:     */                         public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  811:     */                           throws IOException
/*  812:     */                         {
/*  813: 961 */                           input.transferByteRangeTo(output, false, number, repeated);
/*  814:     */                         }
/*  815:     */                         
/*  816:     */                         public ByteString readFrom(Input input)
/*  817:     */                           throws IOException
/*  818:     */                         {
/*  819: 967 */                           return input.readBytes();
/*  820:     */                         }
/*  821:     */                         
/*  822:     */                         public void writeTo(Output output, int number, ByteString value, boolean repeated)
/*  823:     */                           throws IOException
/*  824:     */                         {
/*  825: 974 */                           output.writeBytes(number, value, repeated);
/*  826:     */                         }
/*  827:     */                         
/*  828:     */                         public WireFormat.FieldType getFieldType()
/*  829:     */                         {
/*  830: 980 */                           return WireFormat.FieldType.BYTES;
/*  831:     */                         }
/*  832:     */                         
/*  833:     */                         public Class<?> typeClass()
/*  834:     */                         {
/*  835: 986 */                           return ByteString.class;
/*  836:     */                         }
/*  837:     */                       };
/*  838: 990 */                       public static final RuntimeFieldFactory<byte[]> BYTE_ARRAY = new RuntimeFieldFactory(11)
/*  839:     */                       {
/*  840:     */                         public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  841:     */                         {
/*  842: 997 */                           new Field(WireFormat.FieldType.BYTES, number, name, 
/*  843: 998 */                             (Tag)f.getAnnotation(Tag.class))
/*  844:     */                             {
/*  845:     */                               public void mergeFrom(Input input, T message)
/*  846:     */                                 throws IOException
/*  847:     */                               {
/*  848:     */                                 try
/*  849:     */                                 {
/*  850:1009 */                                   f.set(message, input.readByteArray());
/*  851:     */                                 }
/*  852:     */                                 catch (Exception e)
/*  853:     */                                 {
/*  854:1013 */                                   throw new RuntimeException(e);
/*  855:     */                                 }
/*  856:     */                               }
/*  857:     */                               
/*  858:     */                               public void writeTo(Output output, T message)
/*  859:     */                                 throws IOException
/*  860:     */                               {
/*  861:     */                                 try
/*  862:     */                                 {
/*  863:1022 */                                   byte[] array = (byte[])f.get(message);
/*  864:1023 */                                   if (array != null) {
/*  865:1024 */                                     output.writeByteArray(this.number, array, false);
/*  866:     */                                   }
/*  867:     */                                 }
/*  868:     */                                 catch (Exception e)
/*  869:     */                                 {
/*  870:1028 */                                   throw new RuntimeException(e);
/*  871:     */                                 }
/*  872:     */                               }
/*  873:     */                               
/*  874:     */                               public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  875:     */                                 throws IOException
/*  876:     */                               {
/*  877:1035 */                                 input.transferByteRangeTo(output, false, this.number, repeated);
/*  878:     */                               }
/*  879:     */                             };
/*  880:     */                           }
/*  881:     */                           
/*  882:     */                           public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  883:     */                             throws IOException
/*  884:     */                           {
/*  885:1044 */                             input.transferByteRangeTo(output, false, number, repeated);
/*  886:     */                           }
/*  887:     */                           
/*  888:     */                           public byte[] readFrom(Input input)
/*  889:     */                             throws IOException
/*  890:     */                           {
/*  891:1050 */                             return input.readByteArray();
/*  892:     */                           }
/*  893:     */                           
/*  894:     */                           public void writeTo(Output output, int number, byte[] value, boolean repeated)
/*  895:     */                             throws IOException
/*  896:     */                           {
/*  897:1057 */                             output.writeByteArray(number, value, repeated);
/*  898:     */                           }
/*  899:     */                           
/*  900:     */                           public WireFormat.FieldType getFieldType()
/*  901:     */                           {
/*  902:1063 */                             return WireFormat.FieldType.BYTES;
/*  903:     */                           }
/*  904:     */                           
/*  905:     */                           public Class<?> typeClass()
/*  906:     */                           {
/*  907:1069 */                             return [B.class;
/*  908:     */                           }
/*  909:     */                         };
/*  910:1073 */                         public static final RuntimeFieldFactory<Integer> ENUM = new RuntimeFieldFactory(24)
/*  911:     */                         {
/*  912:     */                           public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, final IdStrategy strategy)
/*  913:     */                           {
/*  914:1081 */                             final EnumIO<? extends Enum<?>> eio = strategy.getEnumIO(f
/*  915:1082 */                               .getType());
/*  916:1083 */                             new Field(WireFormat.FieldType.ENUM, number, name, 
/*  917:1084 */                               (Tag)f.getAnnotation(Tag.class))
/*  918:     */                               {
/*  919:     */                                 public void mergeFrom(Input input, T message)
/*  920:     */                                   throws IOException
/*  921:     */                                 {
/*  922:     */                                   try
/*  923:     */                                   {
/*  924:1095 */                                     f.set(message, eio.readFrom(input));
/*  925:     */                                   }
/*  926:     */                                   catch (Exception e)
/*  927:     */                                   {
/*  928:1099 */                                     throw new RuntimeException(e);
/*  929:     */                                   }
/*  930:     */                                 }
/*  931:     */                                 
/*  932:     */                                 public void writeTo(Output output, T message)
/*  933:     */                                   throws IOException
/*  934:     */                                 {
/*  935:     */                                   try
/*  936:     */                                   {
/*  937:1109 */                                     existing = (Enum)f.get(message);
/*  938:     */                                   }
/*  939:     */                                   catch (Exception e)
/*  940:     */                                   {
/*  941:     */                                     Enum<?> existing;
/*  942:1113 */                                     throw new RuntimeException(e);
/*  943:     */                                   }
/*  944:     */                                   Enum<?> existing;
/*  945:1116 */                                   if (existing != null) {
/*  946:1117 */                                     eio.writeTo(output, this.number, this.repeated, existing);
/*  947:     */                                   }
/*  948:     */                                 }
/*  949:     */                                 
/*  950:     */                                 public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  951:     */                                   throws IOException
/*  952:     */                                 {
/*  953:1123 */                                   EnumIO.transfer(pipe, input, output, this.number, repeated, strategy);
/*  954:     */                                 }
/*  955:     */                               };
/*  956:     */                             }
/*  957:     */                             
/*  958:     */                             public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  959:     */                               throws IOException
/*  960:     */                             {
/*  961:1132 */                               throw new UnsupportedOperationException();
/*  962:     */                             }
/*  963:     */                             
/*  964:     */                             public Integer readFrom(Input input)
/*  965:     */                               throws IOException
/*  966:     */                             {
/*  967:1138 */                               throw new UnsupportedOperationException();
/*  968:     */                             }
/*  969:     */                             
/*  970:     */                             public void writeTo(Output output, int number, Integer value, boolean repeated)
/*  971:     */                               throws IOException
/*  972:     */                             {
/*  973:1145 */                               throw new UnsupportedOperationException();
/*  974:     */                             }
/*  975:     */                             
/*  976:     */                             public WireFormat.FieldType getFieldType()
/*  977:     */                             {
/*  978:1151 */                               throw new UnsupportedOperationException();
/*  979:     */                             }
/*  980:     */                             
/*  981:     */                             public Class<?> typeClass()
/*  982:     */                             {
/*  983:1157 */                               throw new UnsupportedOperationException();
/*  984:     */                             }
/*  985:     */                           };
/*  986:1163 */                           static final RuntimeFieldFactory<Object> POJO = new RuntimeFieldFactory(127)
/*  987:     */                           {
/*  988:     */                             public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/*  989:     */                             {
/*  990:1171 */                               Class<Object> type = f.getType();
/*  991:1172 */                               new RuntimeMessageField(type, strategy
/*  992:1173 */                                 .getSchemaWrapper(type, true), WireFormat.FieldType.MESSAGE, number, name, false, 
/*  993:1174 */                                 (Tag)f.getAnnotation(Tag.class))
/*  994:     */                                 {
/*  995:     */                                   public void mergeFrom(Input input, T message)
/*  996:     */                                     throws IOException
/*  997:     */                                   {
/*  998:     */                                     try
/*  999:     */                                     {
/* 1000:1185 */                                       f.set(message, input
/* 1001:1186 */                                         .mergeObject(f.get(message), getSchema()));
/* 1002:     */                                     }
/* 1003:     */                                     catch (Exception e)
/* 1004:     */                                     {
/* 1005:1190 */                                       throw new RuntimeException(e);
/* 1006:     */                                     }
/* 1007:     */                                   }
/* 1008:     */                                   
/* 1009:     */                                   public void writeTo(Output output, T message)
/* 1010:     */                                     throws IOException
/* 1011:     */                                   {
/* 1012:     */                                     try
/* 1013:     */                                     {
/* 1014:1200 */                                       existing = f.get(message);
/* 1015:     */                                     }
/* 1016:     */                                     catch (Exception e)
/* 1017:     */                                     {
/* 1018:     */                                       Object existing;
/* 1019:1204 */                                       throw new RuntimeException(e);
/* 1020:     */                                     }
/* 1021:     */                                     Object existing;
/* 1022:1207 */                                     if (existing != null) {
/* 1023:1208 */                                       output.writeObject(this.number, existing, getSchema(), false);
/* 1024:     */                                     }
/* 1025:     */                                   }
/* 1026:     */                                   
/* 1027:     */                                   public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1028:     */                                     throws IOException
/* 1029:     */                                   {
/* 1030:1214 */                                     output.writeObject(this.number, pipe, getPipeSchema(), repeated);
/* 1031:     */                                   }
/* 1032:     */                                 };
/* 1033:     */                               }
/* 1034:     */                               
/* 1035:     */                               public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1036:     */                                 throws IOException
/* 1037:     */                               {
/* 1038:1223 */                                 throw new UnsupportedOperationException();
/* 1039:     */                               }
/* 1040:     */                               
/* 1041:     */                               public Object readFrom(Input input)
/* 1042:     */                                 throws IOException
/* 1043:     */                               {
/* 1044:1229 */                                 throw new UnsupportedOperationException();
/* 1045:     */                               }
/* 1046:     */                               
/* 1047:     */                               public void writeTo(Output output, int number, Object value, boolean repeated)
/* 1048:     */                                 throws IOException
/* 1049:     */                               {
/* 1050:1236 */                                 throw new UnsupportedOperationException();
/* 1051:     */                               }
/* 1052:     */                               
/* 1053:     */                               public WireFormat.FieldType getFieldType()
/* 1054:     */                               {
/* 1055:1242 */                                 throw new UnsupportedOperationException();
/* 1056:     */                               }
/* 1057:     */                               
/* 1058:     */                               public Class<?> typeClass()
/* 1059:     */                               {
/* 1060:1248 */                                 throw new UnsupportedOperationException();
/* 1061:     */                               }
/* 1062:     */                             };
/* 1063:1252 */                             static final RuntimeFieldFactory<Object> POLYMORPHIC_POJO = new RuntimeFieldFactory(0)
/* 1064:     */                             {
/* 1065:     */                               public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/* 1066:     */                               {
/* 1067:1260 */                                 if (pojo(f.getType(), (Morph)f.getAnnotation(Morph.class), strategy)) {
/* 1068:1261 */                                   return POJO.create(number, name, f, strategy);
/* 1069:     */                                 }
/* 1070:1263 */                                 new RuntimeDerivativeField(f.getType(), WireFormat.FieldType.MESSAGE, number, name, false, 
/* 1071:     */                                 
/* 1072:1265 */                                   (Tag)f.getAnnotation(Tag.class), strategy)
/* 1073:     */                                   {
/* 1074:     */                                     public void mergeFrom(Input input, T message)
/* 1075:     */                                       throws IOException
/* 1076:     */                                     {
/* 1077:1274 */                                       Object value = input.mergeObject(message, this.schema);
/* 1078:1275 */                                       if (((input instanceof GraphInput)) && 
/* 1079:1276 */                                         (((GraphInput)input).isCurrentMessageReference())) {
/* 1080:     */                                         try
/* 1081:     */                                         {
/* 1082:1281 */                                           f.set(message, value);
/* 1083:     */                                         }
/* 1084:     */                                         catch (Exception e)
/* 1085:     */                                         {
/* 1086:1285 */                                           throw new RuntimeException(e);
/* 1087:     */                                         }
/* 1088:     */                                       }
/* 1089:     */                                     }
/* 1090:     */                                     
/* 1091:     */                                     public void writeTo(Output output, T message)
/* 1092:     */                                       throws IOException
/* 1093:     */                                     {
/* 1094:     */                                       try
/* 1095:     */                                       {
/* 1096:1296 */                                         existing = f.get(message);
/* 1097:     */                                       }
/* 1098:     */                                       catch (Exception e)
/* 1099:     */                                       {
/* 1100:     */                                         Object existing;
/* 1101:1300 */                                         throw new RuntimeException(e);
/* 1102:     */                                       }
/* 1103:     */                                       Object existing;
/* 1104:1303 */                                       if (existing != null) {
/* 1105:1304 */                                         output.writeObject(this.number, existing, this.schema, false);
/* 1106:     */                                       }
/* 1107:     */                                     }
/* 1108:     */                                     
/* 1109:     */                                     public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1110:     */                                       throws IOException
/* 1111:     */                                     {
/* 1112:1310 */                                       output.writeObject(this.number, pipe, this.schema.pipeSchema, false);
/* 1113:     */                                     }
/* 1114:     */                                     
/* 1115:     */                                     public void doMergeFrom(Input input, Schema<Object> schema, Object message)
/* 1116:     */                                       throws IOException
/* 1117:     */                                     {
/* 1118:     */                                       try
/* 1119:     */                                       {
/* 1120:1318 */                                         Object existing = f.get(message);
/* 1121:     */                                         
/* 1122:     */ 
/* 1123:     */ 
/* 1124:     */ 
/* 1125:1323 */                                         Object value = (existing == null) || (existing.getClass() != schema.typeClass()) ? schema.newMessage() : existing;
/* 1126:1325 */                                         if ((input instanceof GraphInput)) {
/* 1127:1328 */                                           ((GraphInput)input).updateLast(value, message);
/* 1128:     */                                         }
/* 1129:1331 */                                         schema.mergeFrom(input, value);
/* 1130:1332 */                                         f.set(message, value);
/* 1131:     */                                       }
/* 1132:     */                                       catch (Exception e)
/* 1133:     */                                       {
/* 1134:1336 */                                         throw new RuntimeException(e);
/* 1135:     */                                       }
/* 1136:     */                                     }
/* 1137:     */                                   };
/* 1138:     */                                 }
/* 1139:     */                                 
/* 1140:     */                                 public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1141:     */                                   throws IOException
/* 1142:     */                                 {
/* 1143:1346 */                                   throw new UnsupportedOperationException();
/* 1144:     */                                 }
/* 1145:     */                                 
/* 1146:     */                                 public Object readFrom(Input input)
/* 1147:     */                                   throws IOException
/* 1148:     */                                 {
/* 1149:1352 */                                   throw new UnsupportedOperationException();
/* 1150:     */                                 }
/* 1151:     */                                 
/* 1152:     */                                 public void writeTo(Output output, int number, Object value, boolean repeated)
/* 1153:     */                                   throws IOException
/* 1154:     */                                 {
/* 1155:1359 */                                   throw new UnsupportedOperationException();
/* 1156:     */                                 }
/* 1157:     */                                 
/* 1158:     */                                 public WireFormat.FieldType getFieldType()
/* 1159:     */                                 {
/* 1160:1365 */                                   throw new UnsupportedOperationException();
/* 1161:     */                                 }
/* 1162:     */                                 
/* 1163:     */                                 public Class<?> typeClass()
/* 1164:     */                                 {
/* 1165:1371 */                                   throw new UnsupportedOperationException();
/* 1166:     */                                 }
/* 1167:     */                               };
/* 1168:1375 */                               static final RuntimeFieldFactory<Object> OBJECT = new RuntimeFieldFactory(16)
/* 1169:     */                               {
/* 1170:     */                                 public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/* 1171:     */                                 {
/* 1172:1382 */                                   new RuntimeObjectField(f
/* 1173:1383 */                                     .getType(), WireFormat.FieldType.MESSAGE, number, name, false, 
/* 1174:     */                                     
/* 1175:     */ 
/* 1176:     */ 
/* 1177:     */ 
/* 1178:1388 */                                     (Tag)f.getAnnotation(Tag.class), 
/* 1179:1389 */                                     PolymorphicSchemaFactories.getFactoryFromField(f, strategy), strategy)
/* 1180:     */                                     {
/* 1181:     */                                       public void mergeFrom(Input input, T message)
/* 1182:     */                                         throws IOException
/* 1183:     */                                       {
/* 1184:1399 */                                         Object value = input.mergeObject(message, this.schema);
/* 1185:1400 */                                         if (((input instanceof GraphInput)) && 
/* 1186:1401 */                                           (((GraphInput)input).isCurrentMessageReference())) {
/* 1187:     */                                           try
/* 1188:     */                                           {
/* 1189:1406 */                                             f.set(message, value);
/* 1190:     */                                           }
/* 1191:     */                                           catch (Exception e)
/* 1192:     */                                           {
/* 1193:1410 */                                             throw new RuntimeException(e);
/* 1194:     */                                           }
/* 1195:     */                                         }
/* 1196:     */                                       }
/* 1197:     */                                       
/* 1198:     */                                       public void writeTo(Output output, T message)
/* 1199:     */                                         throws IOException
/* 1200:     */                                       {
/* 1201:     */                                         try
/* 1202:     */                                         {
/* 1203:1421 */                                           existing = f.get(message);
/* 1204:     */                                         }
/* 1205:     */                                         catch (Exception e)
/* 1206:     */                                         {
/* 1207:     */                                           Object existing;
/* 1208:1425 */                                           throw new RuntimeException(e);
/* 1209:     */                                         }
/* 1210:     */                                         Object existing;
/* 1211:1428 */                                         if (existing != null) {
/* 1212:1429 */                                           output.writeObject(this.number, existing, this.schema, false);
/* 1213:     */                                         }
/* 1214:     */                                       }
/* 1215:     */                                       
/* 1216:     */                                       public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1217:     */                                         throws IOException
/* 1218:     */                                       {
/* 1219:1435 */                                         output.writeObject(this.number, pipe, this.schema.getPipeSchema(), false);
/* 1220:     */                                       }
/* 1221:     */                                       
/* 1222:     */                                       public void setValue(Object value, Object message)
/* 1223:     */                                       {
/* 1224:     */                                         try
/* 1225:     */                                         {
/* 1226:1443 */                                           f.set(message, value);
/* 1227:     */                                         }
/* 1228:     */                                         catch (Exception e)
/* 1229:     */                                         {
/* 1230:1447 */                                           throw new RuntimeException(e);
/* 1231:     */                                         }
/* 1232:     */                                       }
/* 1233:     */                                     };
/* 1234:     */                                   }
/* 1235:     */                                   
/* 1236:     */                                   public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1237:     */                                     throws IOException
/* 1238:     */                                   {
/* 1239:1457 */                                     throw new UnsupportedOperationException();
/* 1240:     */                                   }
/* 1241:     */                                   
/* 1242:     */                                   public Object readFrom(Input input)
/* 1243:     */                                     throws IOException
/* 1244:     */                                   {
/* 1245:1463 */                                     throw new UnsupportedOperationException();
/* 1246:     */                                   }
/* 1247:     */                                   
/* 1248:     */                                   public void writeTo(Output output, int number, Object value, boolean repeated)
/* 1249:     */                                     throws IOException
/* 1250:     */                                   {
/* 1251:1470 */                                     throw new UnsupportedOperationException();
/* 1252:     */                                   }
/* 1253:     */                                   
/* 1254:     */                                   public WireFormat.FieldType getFieldType()
/* 1255:     */                                   {
/* 1256:1476 */                                     return WireFormat.FieldType.MESSAGE;
/* 1257:     */                                   }
/* 1258:     */                                   
/* 1259:     */                                   public Class<?> typeClass()
/* 1260:     */                                   {
/* 1261:1482 */                                     return Object.class;
/* 1262:     */                                   }
/* 1263:     */                                 };
/* 1264:1486 */                                 public static final RuntimeFieldFactory<BigDecimal> BIGDECIMAL = new RuntimeFieldFactory(12)
/* 1265:     */                                 {
/* 1266:     */                                   public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/* 1267:     */                                   {
/* 1268:1493 */                                     new Field(WireFormat.FieldType.STRING, number, name, 
/* 1269:1494 */                                       (Tag)f.getAnnotation(Tag.class))
/* 1270:     */                                       {
/* 1271:     */                                         public void mergeFrom(Input input, T message)
/* 1272:     */                                           throws IOException
/* 1273:     */                                         {
/* 1274:     */                                           try
/* 1275:     */                                           {
/* 1276:1505 */                                             f.set(message, new BigDecimal(input.readString()));
/* 1277:     */                                           }
/* 1278:     */                                           catch (Exception e)
/* 1279:     */                                           {
/* 1280:1509 */                                             throw new RuntimeException(e);
/* 1281:     */                                           }
/* 1282:     */                                         }
/* 1283:     */                                         
/* 1284:     */                                         public void writeTo(Output output, T message)
/* 1285:     */                                           throws IOException
/* 1286:     */                                         {
/* 1287:     */                                           try
/* 1288:     */                                           {
/* 1289:1518 */                                             BigDecimal value = (BigDecimal)f.get(message);
/* 1290:1519 */                                             if (value != null) {
/* 1291:1520 */                                               output.writeString(this.number, value.toString(), false);
/* 1292:     */                                             }
/* 1293:     */                                           }
/* 1294:     */                                           catch (Exception e)
/* 1295:     */                                           {
/* 1296:1524 */                                             throw new RuntimeException(e);
/* 1297:     */                                           }
/* 1298:     */                                         }
/* 1299:     */                                         
/* 1300:     */                                         public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1301:     */                                           throws IOException
/* 1302:     */                                         {
/* 1303:1531 */                                           input.transferByteRangeTo(output, true, this.number, repeated);
/* 1304:     */                                         }
/* 1305:     */                                       };
/* 1306:     */                                     }
/* 1307:     */                                     
/* 1308:     */                                     public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1309:     */                                       throws IOException
/* 1310:     */                                     {
/* 1311:1540 */                                       input.transferByteRangeTo(output, true, number, repeated);
/* 1312:     */                                     }
/* 1313:     */                                     
/* 1314:     */                                     public BigDecimal readFrom(Input input)
/* 1315:     */                                       throws IOException
/* 1316:     */                                     {
/* 1317:1546 */                                       return new BigDecimal(input.readString());
/* 1318:     */                                     }
/* 1319:     */                                     
/* 1320:     */                                     public void writeTo(Output output, int number, BigDecimal value, boolean repeated)
/* 1321:     */                                       throws IOException
/* 1322:     */                                     {
/* 1323:1553 */                                       output.writeString(number, value.toString(), repeated);
/* 1324:     */                                     }
/* 1325:     */                                     
/* 1326:     */                                     public WireFormat.FieldType getFieldType()
/* 1327:     */                                     {
/* 1328:1559 */                                       return WireFormat.FieldType.STRING;
/* 1329:     */                                     }
/* 1330:     */                                     
/* 1331:     */                                     public Class<?> typeClass()
/* 1332:     */                                     {
/* 1333:1565 */                                       return BigDecimal.class;
/* 1334:     */                                     }
/* 1335:     */                                   };
/* 1336:1569 */                                   public static final RuntimeFieldFactory<BigInteger> BIGINTEGER = new RuntimeFieldFactory(13)
/* 1337:     */                                   {
/* 1338:     */                                     public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/* 1339:     */                                     {
/* 1340:1576 */                                       new Field(WireFormat.FieldType.BYTES, number, name, 
/* 1341:1577 */                                         (Tag)f.getAnnotation(Tag.class))
/* 1342:     */                                         {
/* 1343:     */                                           public void mergeFrom(Input input, T message)
/* 1344:     */                                             throws IOException
/* 1345:     */                                           {
/* 1346:     */                                             try
/* 1347:     */                                             {
/* 1348:1588 */                                               f.set(message, new BigInteger(input.readByteArray()));
/* 1349:     */                                             }
/* 1350:     */                                             catch (Exception e)
/* 1351:     */                                             {
/* 1352:1592 */                                               throw new RuntimeException(e);
/* 1353:     */                                             }
/* 1354:     */                                           }
/* 1355:     */                                           
/* 1356:     */                                           public void writeTo(Output output, T message)
/* 1357:     */                                             throws IOException
/* 1358:     */                                           {
/* 1359:     */                                             try
/* 1360:     */                                             {
/* 1361:1601 */                                               BigInteger value = (BigInteger)f.get(message);
/* 1362:1602 */                                               if (value != null) {
/* 1363:1603 */                                                 output.writeByteArray(this.number, value.toByteArray(), false);
/* 1364:     */                                               }
/* 1365:     */                                             }
/* 1366:     */                                             catch (Exception e)
/* 1367:     */                                             {
/* 1368:1608 */                                               throw new RuntimeException(e);
/* 1369:     */                                             }
/* 1370:     */                                           }
/* 1371:     */                                           
/* 1372:     */                                           public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1373:     */                                             throws IOException
/* 1374:     */                                           {
/* 1375:1615 */                                             input.transferByteRangeTo(output, false, this.number, repeated);
/* 1376:     */                                           }
/* 1377:     */                                         };
/* 1378:     */                                       }
/* 1379:     */                                       
/* 1380:     */                                       public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1381:     */                                         throws IOException
/* 1382:     */                                       {
/* 1383:1624 */                                         input.transferByteRangeTo(output, false, number, repeated);
/* 1384:     */                                       }
/* 1385:     */                                       
/* 1386:     */                                       public BigInteger readFrom(Input input)
/* 1387:     */                                         throws IOException
/* 1388:     */                                       {
/* 1389:1630 */                                         return new BigInteger(input.readByteArray());
/* 1390:     */                                       }
/* 1391:     */                                       
/* 1392:     */                                       public void writeTo(Output output, int number, BigInteger value, boolean repeated)
/* 1393:     */                                         throws IOException
/* 1394:     */                                       {
/* 1395:1637 */                                         output.writeByteArray(number, value.toByteArray(), repeated);
/* 1396:     */                                       }
/* 1397:     */                                       
/* 1398:     */                                       public WireFormat.FieldType getFieldType()
/* 1399:     */                                       {
/* 1400:1643 */                                         return WireFormat.FieldType.BYTES;
/* 1401:     */                                       }
/* 1402:     */                                       
/* 1403:     */                                       public Class<?> typeClass()
/* 1404:     */                                       {
/* 1405:1649 */                                         return BigInteger.class;
/* 1406:     */                                       }
/* 1407:     */                                     };
/* 1408:1653 */                                     public static final RuntimeFieldFactory<Date> DATE = new RuntimeFieldFactory(14)
/* 1409:     */                                     {
/* 1410:     */                                       public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/* 1411:     */                                       {
/* 1412:1660 */                                         new Field(WireFormat.FieldType.FIXED64, number, name, 
/* 1413:1661 */                                           (Tag)f.getAnnotation(Tag.class))
/* 1414:     */                                           {
/* 1415:     */                                             public void mergeFrom(Input input, T message)
/* 1416:     */                                               throws IOException
/* 1417:     */                                             {
/* 1418:     */                                               try
/* 1419:     */                                               {
/* 1420:1672 */                                                 f.set(message, new Date(input.readFixed64()));
/* 1421:     */                                               }
/* 1422:     */                                               catch (Exception e)
/* 1423:     */                                               {
/* 1424:1676 */                                                 throw new RuntimeException(e);
/* 1425:     */                                               }
/* 1426:     */                                             }
/* 1427:     */                                             
/* 1428:     */                                             public void writeTo(Output output, T message)
/* 1429:     */                                               throws IOException
/* 1430:     */                                             {
/* 1431:     */                                               try
/* 1432:     */                                               {
/* 1433:1685 */                                                 Date value = (Date)f.get(message);
/* 1434:1686 */                                                 if (value != null) {
/* 1435:1687 */                                                   output.writeFixed64(this.number, value.getTime(), false);
/* 1436:     */                                                 }
/* 1437:     */                                               }
/* 1438:     */                                               catch (Exception e)
/* 1439:     */                                               {
/* 1440:1691 */                                                 throw new RuntimeException(e);
/* 1441:     */                                               }
/* 1442:     */                                             }
/* 1443:     */                                             
/* 1444:     */                                             public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1445:     */                                               throws IOException
/* 1446:     */                                             {
/* 1447:1698 */                                               output.writeFixed64(this.number, input.readFixed64(), repeated);
/* 1448:     */                                             }
/* 1449:     */                                           };
/* 1450:     */                                         }
/* 1451:     */                                         
/* 1452:     */                                         public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1453:     */                                           throws IOException
/* 1454:     */                                         {
/* 1455:1707 */                                           output.writeFixed64(number, input.readFixed64(), repeated);
/* 1456:     */                                         }
/* 1457:     */                                         
/* 1458:     */                                         public Date readFrom(Input input)
/* 1459:     */                                           throws IOException
/* 1460:     */                                         {
/* 1461:1713 */                                           return new Date(input.readFixed64());
/* 1462:     */                                         }
/* 1463:     */                                         
/* 1464:     */                                         public void writeTo(Output output, int number, Date value, boolean repeated)
/* 1465:     */                                           throws IOException
/* 1466:     */                                         {
/* 1467:1720 */                                           output.writeFixed64(number, value.getTime(), repeated);
/* 1468:     */                                         }
/* 1469:     */                                         
/* 1470:     */                                         public WireFormat.FieldType getFieldType()
/* 1471:     */                                         {
/* 1472:1726 */                                           return WireFormat.FieldType.FIXED64;
/* 1473:     */                                         }
/* 1474:     */                                         
/* 1475:     */                                         public Class<?> typeClass()
/* 1476:     */                                         {
/* 1477:1732 */                                           return Date.class;
/* 1478:     */                                         }
/* 1479:     */                                       };
/* 1480:1736 */                                       public static final RuntimeFieldFactory<Object> DELEGATE = new RuntimeFieldFactory(30)
/* 1481:     */                                       {
/* 1482:     */                                         public <T> Field<T> create(int number, String name, final java.lang.reflect.Field f, IdStrategy strategy)
/* 1483:     */                                         {
/* 1484:1745 */                                           final Delegate<Object> delegate = strategy.getDelegate(f.getType());
/* 1485:     */                                           
/* 1486:1747 */                                           new Field(WireFormat.FieldType.BYTES, number, name, 
/* 1487:1748 */                                             (Tag)f.getAnnotation(Tag.class))
/* 1488:     */                                             {
/* 1489:     */                                               public void mergeFrom(Input input, T message)
/* 1490:     */                                                 throws IOException
/* 1491:     */                                               {
/* 1492:1758 */                                                 Object value = delegate.readFrom(input);
/* 1493:     */                                                 try
/* 1494:     */                                                 {
/* 1495:1762 */                                                   f.set(message, value);
/* 1496:     */                                                 }
/* 1497:     */                                                 catch (Exception e)
/* 1498:     */                                                 {
/* 1499:1766 */                                                   throw new RuntimeException(e);
/* 1500:     */                                                 }
/* 1501:     */                                               }
/* 1502:     */                                               
/* 1503:     */                                               public void writeTo(Output output, T message)
/* 1504:     */                                                 throws IOException
/* 1505:     */                                               {
/* 1506:     */                                                 try
/* 1507:     */                                                 {
/* 1508:1777 */                                                   value = f.get(message);
/* 1509:     */                                                 }
/* 1510:     */                                                 catch (Exception e)
/* 1511:     */                                                 {
/* 1512:     */                                                   Object value;
/* 1513:1781 */                                                   throw new RuntimeException(e);
/* 1514:     */                                                 }
/* 1515:     */                                                 Object value;
/* 1516:1784 */                                                 if (value != null) {
/* 1517:1785 */                                                   delegate.writeTo(output, this.number, value, false);
/* 1518:     */                                                 }
/* 1519:     */                                               }
/* 1520:     */                                               
/* 1521:     */                                               public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1522:     */                                                 throws IOException
/* 1523:     */                                               {
/* 1524:1792 */                                                 delegate.transfer(pipe, input, output, this.number, repeated);
/* 1525:     */                                               }
/* 1526:     */                                             };
/* 1527:     */                                           }
/* 1528:     */                                           
/* 1529:     */                                           public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1530:     */                                             throws IOException
/* 1531:     */                                           {
/* 1532:1801 */                                             throw new UnsupportedOperationException();
/* 1533:     */                                           }
/* 1534:     */                                           
/* 1535:     */                                           public Object readFrom(Input input)
/* 1536:     */                                             throws IOException
/* 1537:     */                                           {
/* 1538:1807 */                                             throw new UnsupportedOperationException();
/* 1539:     */                                           }
/* 1540:     */                                           
/* 1541:     */                                           public void writeTo(Output output, int number, Object value, boolean repeated)
/* 1542:     */                                             throws IOException
/* 1543:     */                                           {
/* 1544:1814 */                                             throw new UnsupportedOperationException();
/* 1545:     */                                           }
/* 1546:     */                                           
/* 1547:     */                                           public WireFormat.FieldType getFieldType()
/* 1548:     */                                           {
/* 1549:1820 */                                             throw new UnsupportedOperationException();
/* 1550:     */                                           }
/* 1551:     */                                           
/* 1552:     */                                           public Class<?> typeClass()
/* 1553:     */                                           {
/* 1554:1826 */                                             throw new UnsupportedOperationException();
/* 1555:     */                                           }
/* 1556:     */                                         };
/* 1557:     */                                       }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeReflectionFieldFactory
 * JD-Core Version:    0.7.0.1
 */