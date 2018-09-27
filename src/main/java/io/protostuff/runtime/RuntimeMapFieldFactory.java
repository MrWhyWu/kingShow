/*    1:     */ package io.protostuff.runtime;
/*    2:     */ 
/*    3:     */ import io.protostuff.GraphInput;
/*    4:     */ import io.protostuff.Input;
/*    5:     */ import io.protostuff.MapSchema;
/*    6:     */ import io.protostuff.MapSchema.MapWrapper;
/*    7:     */ import io.protostuff.MapSchema.MessageFactory;
/*    8:     */ import io.protostuff.Message;
/*    9:     */ import io.protostuff.Morph;
/*   10:     */ import io.protostuff.Output;
/*   11:     */ import io.protostuff.Pipe;
/*   12:     */ import io.protostuff.Pipe.Schema;
/*   13:     */ import io.protostuff.Schema;
/*   14:     */ import io.protostuff.Tag;
/*   15:     */ import io.protostuff.WireFormat.FieldType;
/*   16:     */ import java.io.IOException;
/*   17:     */ import java.lang.reflect.Modifier;
/*   18:     */ import java.util.Collection;
/*   19:     */ import java.util.EnumMap;
/*   20:     */ import java.util.Map;
/*   21:     */ 
/*   22:     */ final class RuntimeMapFieldFactory
/*   23:     */ {
/*   24:     */   private static <T> Field<T> createMapInlineKEnumV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, final Delegate<Object> inlineK, Class<Object> clazzV, final IdStrategy strategy)
/*   25:     */   {
/*   26:  73 */     final EnumIO<?> eioV = strategy.getEnumIO(clazzV);
/*   27:     */     
/*   28:  75 */     new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*   29:  76 */       (Tag)f.getAnnotation(Tag.class), messageFactory)
/*   30:     */       {
/*   31:     */         protected void mergeFrom(Input input, T message)
/*   32:     */           throws IOException
/*   33:     */         {
/*   34:     */           try
/*   35:     */           {
/*   36:  88 */             f.set(message, input.mergeObject(
/*   37:  89 */               (Map)f.get(message), this.schema));
/*   38:     */           }
/*   39:     */           catch (Exception e)
/*   40:     */           {
/*   41:  93 */             throw new RuntimeException(e);
/*   42:     */           }
/*   43:     */         }
/*   44:     */         
/*   45:     */         protected void writeTo(Output output, T message)
/*   46:     */           throws IOException
/*   47:     */         {
/*   48:     */           try
/*   49:     */           {
/*   50: 104 */             existing = (Map)f.get(message);
/*   51:     */           }
/*   52:     */           catch (Exception e)
/*   53:     */           {
/*   54:     */             Map<Object, Enum<?>> existing;
/*   55: 108 */             throw new RuntimeException(e);
/*   56:     */           }
/*   57:     */           Map<Object, Enum<?>> existing;
/*   58: 111 */           if (existing != null) {
/*   59: 112 */             output.writeObject(this.number, existing, this.schema, false);
/*   60:     */           }
/*   61:     */         }
/*   62:     */         
/*   63:     */         protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*   64:     */           throws IOException
/*   65:     */         {
/*   66: 119 */           output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*   67:     */         }
/*   68:     */         
/*   69:     */         protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Enum<?>> wrapper)
/*   70:     */           throws IOException
/*   71:     */         {
/*   72: 126 */           return inlineK.readFrom(input);
/*   73:     */         }
/*   74:     */         
/*   75:     */         protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/*   76:     */           throws IOException
/*   77:     */         {
/*   78: 133 */           inlineK.writeTo(output, fieldNumber, key, repeated);
/*   79:     */         }
/*   80:     */         
/*   81:     */         protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*   82:     */           throws IOException
/*   83:     */         {
/*   84: 140 */           inlineK.transfer(pipe, input, output, number, repeated);
/*   85:     */         }
/*   86:     */         
/*   87:     */         protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Enum<?>> wrapper, Object key)
/*   88:     */           throws IOException
/*   89:     */         {
/*   90: 148 */           wrapper.put(key, eioV.readFrom(input));
/*   91:     */         }
/*   92:     */         
/*   93:     */         protected void vTo(Output output, int fieldNumber, Enum<?> val, boolean repeated)
/*   94:     */           throws IOException
/*   95:     */         {
/*   96: 155 */           eioV.writeTo(output, fieldNumber, repeated, val);
/*   97:     */         }
/*   98:     */         
/*   99:     */         protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  100:     */           throws IOException
/*  101:     */         {
/*  102: 162 */           EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  103:     */         }
/*  104:     */       };
/*  105:     */     }
/*  106:     */     
/*  107:     */     private static <T> Field<T> createMapInlineKInlineV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, final Delegate<Object> inlineK, final Delegate<Object> inlineV)
/*  108:     */     {
/*  109: 172 */       new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  110: 173 */         (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  111:     */         {
/*  112:     */           protected void mergeFrom(Input input, T message)
/*  113:     */             throws IOException
/*  114:     */           {
/*  115:     */             try
/*  116:     */             {
/*  117: 185 */               f.set(message, input.mergeObject(
/*  118: 186 */                 (Map)f.get(message), this.schema));
/*  119:     */             }
/*  120:     */             catch (Exception e)
/*  121:     */             {
/*  122: 190 */               throw new RuntimeException(e);
/*  123:     */             }
/*  124:     */           }
/*  125:     */           
/*  126:     */           protected void writeTo(Output output, T message)
/*  127:     */             throws IOException
/*  128:     */           {
/*  129:     */             try
/*  130:     */             {
/*  131: 201 */               existing = (Map)f.get(message);
/*  132:     */             }
/*  133:     */             catch (Exception e)
/*  134:     */             {
/*  135:     */               Map<Object, Object> existing;
/*  136: 205 */               throw new RuntimeException(e);
/*  137:     */             }
/*  138:     */             Map<Object, Object> existing;
/*  139: 208 */             if (existing != null) {
/*  140: 209 */               output.writeObject(this.number, existing, this.schema, false);
/*  141:     */             }
/*  142:     */           }
/*  143:     */           
/*  144:     */           protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  145:     */             throws IOException
/*  146:     */           {
/*  147: 216 */             output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  148:     */           }
/*  149:     */           
/*  150:     */           protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/*  151:     */             throws IOException
/*  152:     */           {
/*  153: 223 */             return inlineK.readFrom(input);
/*  154:     */           }
/*  155:     */           
/*  156:     */           protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/*  157:     */             throws IOException
/*  158:     */           {
/*  159: 230 */             inlineK.writeTo(output, fieldNumber, key, repeated);
/*  160:     */           }
/*  161:     */           
/*  162:     */           protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  163:     */             throws IOException
/*  164:     */           {
/*  165: 237 */             inlineK.transfer(pipe, input, output, number, repeated);
/*  166:     */           }
/*  167:     */           
/*  168:     */           protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/*  169:     */             throws IOException
/*  170:     */           {
/*  171: 245 */             wrapper.put(key, inlineV.readFrom(input));
/*  172:     */           }
/*  173:     */           
/*  174:     */           protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  175:     */             throws IOException
/*  176:     */           {
/*  177: 252 */             inlineV.writeTo(output, fieldNumber, val, repeated);
/*  178:     */           }
/*  179:     */           
/*  180:     */           protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  181:     */             throws IOException
/*  182:     */           {
/*  183: 259 */             inlineV.transfer(pipe, input, output, number, repeated);
/*  184:     */           }
/*  185:     */         };
/*  186:     */       }
/*  187:     */       
/*  188:     */       private static <T> Field<T> createMapInlineKPojoV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, final Delegate<Object> inlineK, Class<Object> clazzV, IdStrategy strategy)
/*  189:     */       {
/*  190: 269 */         final HasSchema<Object> schemaV = strategy.getSchemaWrapper(clazzV, true);
/*  191:     */         
/*  192:     */ 
/*  193: 272 */         new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  194: 273 */           (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  195:     */           {
/*  196:     */             protected void mergeFrom(Input input, T message)
/*  197:     */               throws IOException
/*  198:     */             {
/*  199:     */               try
/*  200:     */               {
/*  201: 285 */                 f.set(message, input.mergeObject(
/*  202: 286 */                   (Map)f.get(message), this.schema));
/*  203:     */               }
/*  204:     */               catch (Exception e)
/*  205:     */               {
/*  206: 290 */                 throw new RuntimeException(e);
/*  207:     */               }
/*  208:     */             }
/*  209:     */             
/*  210:     */             protected void writeTo(Output output, T message)
/*  211:     */               throws IOException
/*  212:     */             {
/*  213:     */               try
/*  214:     */               {
/*  215: 301 */                 existing = (Map)f.get(message);
/*  216:     */               }
/*  217:     */               catch (Exception e)
/*  218:     */               {
/*  219:     */                 Map<Object, Object> existing;
/*  220: 305 */                 throw new RuntimeException(e);
/*  221:     */               }
/*  222:     */               Map<Object, Object> existing;
/*  223: 308 */               if (existing != null) {
/*  224: 309 */                 output.writeObject(this.number, existing, this.schema, false);
/*  225:     */               }
/*  226:     */             }
/*  227:     */             
/*  228:     */             protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  229:     */               throws IOException
/*  230:     */             {
/*  231: 316 */               output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  232:     */             }
/*  233:     */             
/*  234:     */             protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/*  235:     */               throws IOException
/*  236:     */             {
/*  237: 323 */               return inlineK.readFrom(input);
/*  238:     */             }
/*  239:     */             
/*  240:     */             protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/*  241:     */               throws IOException
/*  242:     */             {
/*  243: 330 */               inlineK.writeTo(output, fieldNumber, key, repeated);
/*  244:     */             }
/*  245:     */             
/*  246:     */             protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  247:     */               throws IOException
/*  248:     */             {
/*  249: 337 */               inlineK.transfer(pipe, input, output, number, repeated);
/*  250:     */             }
/*  251:     */             
/*  252:     */             protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/*  253:     */               throws IOException
/*  254:     */             {
/*  255: 345 */               wrapper.put(key, input.mergeObject(null, schemaV.getSchema()));
/*  256:     */             }
/*  257:     */             
/*  258:     */             protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  259:     */               throws IOException
/*  260:     */             {
/*  261: 352 */               output.writeObject(fieldNumber, val, schemaV.getSchema(), repeated);
/*  262:     */             }
/*  263:     */             
/*  264:     */             protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  265:     */               throws IOException
/*  266:     */             {
/*  267: 360 */               output.writeObject(number, pipe, schemaV.getPipeSchema(), repeated);
/*  268:     */             }
/*  269:     */           };
/*  270:     */         }
/*  271:     */         
/*  272:     */         private static <T> Field<T> createMapInlineKPolymorphicV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, final Delegate<Object> inlineK, Class<Object> clazzV, final IdStrategy strategy)
/*  273:     */         {
/*  274: 371 */           new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  275: 372 */             (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  276:     */             {
/*  277:     */               protected void mergeFrom(Input input, T message)
/*  278:     */                 throws IOException
/*  279:     */               {
/*  280:     */                 try
/*  281:     */                 {
/*  282: 385 */                   f.set(message, input.mergeObject(
/*  283: 386 */                     (Map)f.get(message), this.schema));
/*  284:     */                 }
/*  285:     */                 catch (Exception e)
/*  286:     */                 {
/*  287: 390 */                   throw new RuntimeException(e);
/*  288:     */                 }
/*  289:     */               }
/*  290:     */               
/*  291:     */               protected void writeTo(Output output, T message)
/*  292:     */                 throws IOException
/*  293:     */               {
/*  294:     */                 try
/*  295:     */                 {
/*  296: 401 */                   existing = (Map)f.get(message);
/*  297:     */                 }
/*  298:     */                 catch (Exception e)
/*  299:     */                 {
/*  300:     */                   Map<Object, Object> existing;
/*  301: 405 */                   throw new RuntimeException(e);
/*  302:     */                 }
/*  303:     */                 Map<Object, Object> existing;
/*  304: 408 */                 if (existing != null) {
/*  305: 409 */                   output.writeObject(this.number, existing, this.schema, false);
/*  306:     */                 }
/*  307:     */               }
/*  308:     */               
/*  309:     */               protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  310:     */                 throws IOException
/*  311:     */               {
/*  312: 416 */                 output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  313:     */               }
/*  314:     */               
/*  315:     */               protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/*  316:     */                 throws IOException
/*  317:     */               {
/*  318: 423 */                 return inlineK.readFrom(input);
/*  319:     */               }
/*  320:     */               
/*  321:     */               protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/*  322:     */                 throws IOException
/*  323:     */               {
/*  324: 430 */                 inlineK.writeTo(output, fieldNumber, key, repeated);
/*  325:     */               }
/*  326:     */               
/*  327:     */               protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  328:     */                 throws IOException
/*  329:     */               {
/*  330: 437 */                 inlineK.transfer(pipe, input, output, number, repeated);
/*  331:     */               }
/*  332:     */               
/*  333:     */               protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/*  334:     */                 throws IOException
/*  335:     */               {
/*  336: 445 */                 Object value = input.mergeObject(wrapper, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA);
/*  337: 447 */                 if (value != wrapper)
/*  338:     */                 {
/*  339: 451 */                   ((GraphInput)input).updateLast(value, wrapper);
/*  340:     */                   
/*  341: 453 */                   wrapper.put(key, value);
/*  342: 454 */                   return;
/*  343:     */                 }
/*  344: 457 */                 if (key != null) {
/*  345: 460 */                   wrapper.put(key, wrapper.setValue(null));
/*  346:     */                 }
/*  347:     */               }
/*  348:     */               
/*  349:     */               protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  350:     */                 throws IOException
/*  351:     */               {
/*  352: 468 */                 output.writeObject(fieldNumber, val, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA, repeated);
/*  353:     */               }
/*  354:     */               
/*  355:     */               protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  356:     */                 throws IOException
/*  357:     */               {
/*  358: 476 */                 output.writeObject(number, pipe, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA.pipeSchema, repeated);
/*  359:     */               }
/*  360:     */             };
/*  361:     */           }
/*  362:     */           
/*  363:     */           private static <T> Field<T> createMapInlineKObjectV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, final Delegate<Object> inlineK, final Schema<Object> valueSchema, final Pipe.Schema<Object> valuePipeSchema, IdStrategy strategy)
/*  364:     */           {
/*  365: 489 */             new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  366: 490 */               (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  367:     */               {
/*  368:     */                 protected void mergeFrom(Input input, T message)
/*  369:     */                   throws IOException
/*  370:     */                 {
/*  371:     */                   try
/*  372:     */                   {
/*  373: 503 */                     f.set(message, input.mergeObject(
/*  374: 504 */                       (Map)f.get(message), this.schema));
/*  375:     */                   }
/*  376:     */                   catch (Exception e)
/*  377:     */                   {
/*  378: 508 */                     throw new RuntimeException(e);
/*  379:     */                   }
/*  380:     */                 }
/*  381:     */                 
/*  382:     */                 protected void writeTo(Output output, T message)
/*  383:     */                   throws IOException
/*  384:     */                 {
/*  385:     */                   try
/*  386:     */                   {
/*  387: 519 */                     existing = (Map)f.get(message);
/*  388:     */                   }
/*  389:     */                   catch (Exception e)
/*  390:     */                   {
/*  391:     */                     Map<Object, Object> existing;
/*  392: 523 */                     throw new RuntimeException(e);
/*  393:     */                   }
/*  394:     */                   Map<Object, Object> existing;
/*  395: 526 */                   if (existing != null) {
/*  396: 527 */                     output.writeObject(this.number, existing, this.schema, false);
/*  397:     */                   }
/*  398:     */                 }
/*  399:     */                 
/*  400:     */                 protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  401:     */                   throws IOException
/*  402:     */                 {
/*  403: 534 */                   output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  404:     */                 }
/*  405:     */                 
/*  406:     */                 protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/*  407:     */                   throws IOException
/*  408:     */                 {
/*  409: 541 */                   return inlineK.readFrom(input);
/*  410:     */                 }
/*  411:     */                 
/*  412:     */                 protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/*  413:     */                   throws IOException
/*  414:     */                 {
/*  415: 548 */                   inlineK.writeTo(output, fieldNumber, key, repeated);
/*  416:     */                 }
/*  417:     */                 
/*  418:     */                 protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  419:     */                   throws IOException
/*  420:     */                 {
/*  421: 555 */                   inlineK.transfer(pipe, input, output, number, repeated);
/*  422:     */                 }
/*  423:     */                 
/*  424:     */                 protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/*  425:     */                   throws IOException
/*  426:     */                 {
/*  427: 563 */                   Object value = input.mergeObject(wrapper, valueSchema);
/*  428: 564 */                   if (value != wrapper)
/*  429:     */                   {
/*  430: 568 */                     ((GraphInput)input).updateLast(value, wrapper);
/*  431:     */                     
/*  432: 570 */                     wrapper.put(key, value);
/*  433: 571 */                     return;
/*  434:     */                   }
/*  435: 574 */                   if (key != null) {
/*  436: 577 */                     wrapper.put(key, wrapper.setValue(null));
/*  437:     */                   }
/*  438:     */                 }
/*  439:     */                 
/*  440:     */                 protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  441:     */                   throws IOException
/*  442:     */                 {
/*  443: 585 */                   output.writeObject(fieldNumber, val, valueSchema, repeated);
/*  444:     */                 }
/*  445:     */                 
/*  446:     */                 protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  447:     */                   throws IOException
/*  448:     */                 {
/*  449: 592 */                   output.writeObject(number, pipe, valuePipeSchema, repeated);
/*  450:     */                 }
/*  451:     */               };
/*  452:     */             }
/*  453:     */             
/*  454:     */             private static <T> Field<T> createMapEnumKEnumV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, Class<Object> clazzV, final IdStrategy strategy)
/*  455:     */             {
/*  456: 602 */               final EnumIO<?> eioK = strategy.getEnumIO(clazzK);
/*  457: 603 */               final EnumIO<?> eioV = strategy.getEnumIO(clazzV);
/*  458:     */               
/*  459: 605 */               new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  460: 606 */                 (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  461:     */                 {
/*  462:     */                   protected void mergeFrom(Input input, T message)
/*  463:     */                     throws IOException
/*  464:     */                   {
/*  465:     */                     try
/*  466:     */                     {
/*  467: 618 */                       f.set(message, input.mergeObject(
/*  468: 619 */                         (Map)f.get(message), this.schema));
/*  469:     */                     }
/*  470:     */                     catch (Exception e)
/*  471:     */                     {
/*  472: 623 */                       throw new RuntimeException(e);
/*  473:     */                     }
/*  474:     */                   }
/*  475:     */                   
/*  476:     */                   protected void writeTo(Output output, T message)
/*  477:     */                     throws IOException
/*  478:     */                   {
/*  479:     */                     try
/*  480:     */                     {
/*  481: 634 */                       existing = (Map)f.get(message);
/*  482:     */                     }
/*  483:     */                     catch (Exception e)
/*  484:     */                     {
/*  485:     */                       Map<Enum<?>, Enum<?>> existing;
/*  486: 638 */                       throw new RuntimeException(e);
/*  487:     */                     }
/*  488:     */                     Map<Enum<?>, Enum<?>> existing;
/*  489: 641 */                     if (existing != null) {
/*  490: 642 */                       output.writeObject(this.number, existing, this.schema, false);
/*  491:     */                     }
/*  492:     */                   }
/*  493:     */                   
/*  494:     */                   protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  495:     */                     throws IOException
/*  496:     */                   {
/*  497: 649 */                     output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  498:     */                   }
/*  499:     */                   
/*  500:     */                   protected Enum<?> kFrom(Input input, MapSchema.MapWrapper<Enum<?>, Enum<?>> wrapper)
/*  501:     */                     throws IOException
/*  502:     */                   {
/*  503: 656 */                     return eioK.readFrom(input);
/*  504:     */                   }
/*  505:     */                   
/*  506:     */                   protected void kTo(Output output, int fieldNumber, Enum<?> key, boolean repeated)
/*  507:     */                     throws IOException
/*  508:     */                   {
/*  509: 663 */                     eioK.writeTo(output, fieldNumber, repeated, key);
/*  510:     */                   }
/*  511:     */                   
/*  512:     */                   protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  513:     */                     throws IOException
/*  514:     */                   {
/*  515: 670 */                     EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  516:     */                   }
/*  517:     */                   
/*  518:     */                   protected void vPutFrom(Input input, MapSchema.MapWrapper<Enum<?>, Enum<?>> wrapper, Enum<?> key)
/*  519:     */                     throws IOException
/*  520:     */                   {
/*  521: 678 */                     wrapper.put(key, eioV.readFrom(input));
/*  522:     */                   }
/*  523:     */                   
/*  524:     */                   protected void vTo(Output output, int fieldNumber, Enum<?> val, boolean repeated)
/*  525:     */                     throws IOException
/*  526:     */                   {
/*  527: 685 */                     eioV.writeTo(output, fieldNumber, repeated, val);
/*  528:     */                   }
/*  529:     */                   
/*  530:     */                   protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  531:     */                     throws IOException
/*  532:     */                   {
/*  533: 692 */                     EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  534:     */                   }
/*  535:     */                 };
/*  536:     */               }
/*  537:     */               
/*  538:     */               private static <T> Field<T> createMapEnumKInlineV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, final Delegate<Object> inlineV, final IdStrategy strategy)
/*  539:     */               {
/*  540: 702 */                 final EnumIO<?> eioK = strategy.getEnumIO(clazzK);
/*  541:     */                 
/*  542: 704 */                 new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  543: 705 */                   (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  544:     */                   {
/*  545:     */                     protected void mergeFrom(Input input, T message)
/*  546:     */                       throws IOException
/*  547:     */                     {
/*  548:     */                       try
/*  549:     */                       {
/*  550: 717 */                         f.set(message, input.mergeObject(
/*  551: 718 */                           (Map)f.get(message), this.schema));
/*  552:     */                       }
/*  553:     */                       catch (Exception e)
/*  554:     */                       {
/*  555: 722 */                         throw new RuntimeException(e);
/*  556:     */                       }
/*  557:     */                     }
/*  558:     */                     
/*  559:     */                     protected void writeTo(Output output, T message)
/*  560:     */                       throws IOException
/*  561:     */                     {
/*  562:     */                       try
/*  563:     */                       {
/*  564: 733 */                         existing = (Map)f.get(message);
/*  565:     */                       }
/*  566:     */                       catch (Exception e)
/*  567:     */                       {
/*  568:     */                         Map<Enum<?>, Object> existing;
/*  569: 737 */                         throw new RuntimeException(e);
/*  570:     */                       }
/*  571:     */                       Map<Enum<?>, Object> existing;
/*  572: 740 */                       if (existing != null) {
/*  573: 741 */                         output.writeObject(this.number, existing, this.schema, false);
/*  574:     */                       }
/*  575:     */                     }
/*  576:     */                     
/*  577:     */                     protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  578:     */                       throws IOException
/*  579:     */                     {
/*  580: 748 */                       output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  581:     */                     }
/*  582:     */                     
/*  583:     */                     protected Enum<?> kFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper)
/*  584:     */                       throws IOException
/*  585:     */                     {
/*  586: 755 */                       return eioK.readFrom(input);
/*  587:     */                     }
/*  588:     */                     
/*  589:     */                     protected void kTo(Output output, int fieldNumber, Enum<?> key, boolean repeated)
/*  590:     */                       throws IOException
/*  591:     */                     {
/*  592: 762 */                       eioK.writeTo(output, fieldNumber, repeated, key);
/*  593:     */                     }
/*  594:     */                     
/*  595:     */                     protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  596:     */                       throws IOException
/*  597:     */                     {
/*  598: 769 */                       EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  599:     */                     }
/*  600:     */                     
/*  601:     */                     protected void vPutFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper, Enum<?> key)
/*  602:     */                       throws IOException
/*  603:     */                     {
/*  604: 777 */                       wrapper.put(key, inlineV.readFrom(input));
/*  605:     */                     }
/*  606:     */                     
/*  607:     */                     protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  608:     */                       throws IOException
/*  609:     */                     {
/*  610: 784 */                       inlineV.writeTo(output, fieldNumber, val, repeated);
/*  611:     */                     }
/*  612:     */                     
/*  613:     */                     protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  614:     */                       throws IOException
/*  615:     */                     {
/*  616: 791 */                       inlineV.transfer(pipe, input, output, number, repeated);
/*  617:     */                     }
/*  618:     */                   };
/*  619:     */                 }
/*  620:     */                 
/*  621:     */                 private static <T> Field<T> createMapEnumKPojoV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, Class<Object> clazzV, final IdStrategy strategy)
/*  622:     */                 {
/*  623: 801 */                   final EnumIO<?> eioK = strategy.getEnumIO(clazzK);
/*  624: 802 */                   final HasSchema<Object> schemaV = strategy.getSchemaWrapper(clazzV, true);
/*  625:     */                   
/*  626:     */ 
/*  627: 805 */                   new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  628: 806 */                     (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  629:     */                     {
/*  630:     */                       protected void mergeFrom(Input input, T message)
/*  631:     */                         throws IOException
/*  632:     */                       {
/*  633:     */                         try
/*  634:     */                         {
/*  635: 818 */                           f.set(message, input.mergeObject(
/*  636: 819 */                             (Map)f.get(message), this.schema));
/*  637:     */                         }
/*  638:     */                         catch (Exception e)
/*  639:     */                         {
/*  640: 823 */                           throw new RuntimeException(e);
/*  641:     */                         }
/*  642:     */                       }
/*  643:     */                       
/*  644:     */                       protected void writeTo(Output output, T message)
/*  645:     */                         throws IOException
/*  646:     */                       {
/*  647:     */                         try
/*  648:     */                         {
/*  649: 834 */                           existing = (Map)f.get(message);
/*  650:     */                         }
/*  651:     */                         catch (Exception e)
/*  652:     */                         {
/*  653:     */                           Map<Enum<?>, Object> existing;
/*  654: 838 */                           throw new RuntimeException(e);
/*  655:     */                         }
/*  656:     */                         Map<Enum<?>, Object> existing;
/*  657: 841 */                         if (existing != null) {
/*  658: 842 */                           output.writeObject(this.number, existing, this.schema, false);
/*  659:     */                         }
/*  660:     */                       }
/*  661:     */                       
/*  662:     */                       protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  663:     */                         throws IOException
/*  664:     */                       {
/*  665: 849 */                         output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  666:     */                       }
/*  667:     */                       
/*  668:     */                       protected Enum<?> kFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper)
/*  669:     */                         throws IOException
/*  670:     */                       {
/*  671: 856 */                         return eioK.readFrom(input);
/*  672:     */                       }
/*  673:     */                       
/*  674:     */                       protected void kTo(Output output, int fieldNumber, Enum<?> key, boolean repeated)
/*  675:     */                         throws IOException
/*  676:     */                       {
/*  677: 863 */                         eioK.writeTo(output, fieldNumber, repeated, key);
/*  678:     */                       }
/*  679:     */                       
/*  680:     */                       protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  681:     */                         throws IOException
/*  682:     */                       {
/*  683: 870 */                         EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  684:     */                       }
/*  685:     */                       
/*  686:     */                       protected void vPutFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper, Enum<?> key)
/*  687:     */                         throws IOException
/*  688:     */                       {
/*  689: 878 */                         wrapper.put(key, input.mergeObject(null, schemaV.getSchema()));
/*  690:     */                       }
/*  691:     */                       
/*  692:     */                       protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  693:     */                         throws IOException
/*  694:     */                       {
/*  695: 885 */                         output.writeObject(fieldNumber, val, schemaV.getSchema(), repeated);
/*  696:     */                       }
/*  697:     */                       
/*  698:     */                       protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  699:     */                         throws IOException
/*  700:     */                       {
/*  701: 893 */                         output.writeObject(number, pipe, schemaV.getPipeSchema(), repeated);
/*  702:     */                       }
/*  703:     */                     };
/*  704:     */                   }
/*  705:     */                   
/*  706:     */                   private static <T> Field<T> createMapEnumKPolymorphicV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, Class<Object> clazzV, final IdStrategy strategy)
/*  707:     */                   {
/*  708: 904 */                     final EnumIO<?> eioK = strategy.getEnumIO(clazzK);
/*  709:     */                     
/*  710: 906 */                     new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  711: 907 */                       (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  712:     */                       {
/*  713:     */                         protected void mergeFrom(Input input, T message)
/*  714:     */                           throws IOException
/*  715:     */                         {
/*  716:     */                           try
/*  717:     */                           {
/*  718: 920 */                             f.set(message, input.mergeObject(
/*  719: 921 */                               (Map)f.get(message), this.schema));
/*  720:     */                           }
/*  721:     */                           catch (Exception e)
/*  722:     */                           {
/*  723: 925 */                             throw new RuntimeException(e);
/*  724:     */                           }
/*  725:     */                         }
/*  726:     */                         
/*  727:     */                         protected void writeTo(Output output, T message)
/*  728:     */                           throws IOException
/*  729:     */                         {
/*  730:     */                           try
/*  731:     */                           {
/*  732: 936 */                             existing = (Map)f.get(message);
/*  733:     */                           }
/*  734:     */                           catch (Exception e)
/*  735:     */                           {
/*  736:     */                             Map<Enum<?>, Object> existing;
/*  737: 940 */                             throw new RuntimeException(e);
/*  738:     */                           }
/*  739:     */                           Map<Enum<?>, Object> existing;
/*  740: 943 */                           if (existing != null) {
/*  741: 944 */                             output.writeObject(this.number, existing, this.schema, false);
/*  742:     */                           }
/*  743:     */                         }
/*  744:     */                         
/*  745:     */                         protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  746:     */                           throws IOException
/*  747:     */                         {
/*  748: 951 */                           output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  749:     */                         }
/*  750:     */                         
/*  751:     */                         protected Enum<?> kFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper)
/*  752:     */                           throws IOException
/*  753:     */                         {
/*  754: 958 */                           return eioK.readFrom(input);
/*  755:     */                         }
/*  756:     */                         
/*  757:     */                         protected void kTo(Output output, int fieldNumber, Enum<?> key, boolean repeated)
/*  758:     */                           throws IOException
/*  759:     */                         {
/*  760: 965 */                           eioK.writeTo(output, fieldNumber, repeated, key);
/*  761:     */                         }
/*  762:     */                         
/*  763:     */                         protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  764:     */                           throws IOException
/*  765:     */                         {
/*  766: 972 */                           EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  767:     */                         }
/*  768:     */                         
/*  769:     */                         protected void vPutFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper, Enum<?> key)
/*  770:     */                           throws IOException
/*  771:     */                         {
/*  772: 980 */                           Object value = input.mergeObject(wrapper, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA);
/*  773: 982 */                           if (value != wrapper)
/*  774:     */                           {
/*  775: 986 */                             ((GraphInput)input).updateLast(value, wrapper);
/*  776:     */                             
/*  777: 988 */                             wrapper.put(key, value);
/*  778: 989 */                             return;
/*  779:     */                           }
/*  780: 992 */                           if (key != null) {
/*  781: 995 */                             wrapper.put(key, wrapper.setValue(null));
/*  782:     */                           }
/*  783:     */                         }
/*  784:     */                         
/*  785:     */                         protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  786:     */                           throws IOException
/*  787:     */                         {
/*  788:1003 */                           output.writeObject(fieldNumber, val, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA, repeated);
/*  789:     */                         }
/*  790:     */                         
/*  791:     */                         protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  792:     */                           throws IOException
/*  793:     */                         {
/*  794:1011 */                           output.writeObject(number, pipe, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA.pipeSchema, repeated);
/*  795:     */                         }
/*  796:     */                       };
/*  797:     */                     }
/*  798:     */                     
/*  799:     */                     private static <T> Field<T> createMapEnumKObjectV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, final Schema<Object> valueSchema, final Pipe.Schema<Object> valuePipeSchema, final IdStrategy strategy)
/*  800:     */                     {
/*  801:1023 */                       final EnumIO<?> eioK = strategy.getEnumIO(clazzK);
/*  802:     */                       
/*  803:1025 */                       new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  804:1026 */                         (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  805:     */                         {
/*  806:     */                           protected void mergeFrom(Input input, T message)
/*  807:     */                             throws IOException
/*  808:     */                           {
/*  809:     */                             try
/*  810:     */                             {
/*  811:1039 */                               f.set(message, input.mergeObject(
/*  812:1040 */                                 (Map)f.get(message), this.schema));
/*  813:     */                             }
/*  814:     */                             catch (Exception e)
/*  815:     */                             {
/*  816:1044 */                               throw new RuntimeException(e);
/*  817:     */                             }
/*  818:     */                           }
/*  819:     */                           
/*  820:     */                           protected void writeTo(Output output, T message)
/*  821:     */                             throws IOException
/*  822:     */                           {
/*  823:     */                             try
/*  824:     */                             {
/*  825:1055 */                               existing = (Map)f.get(message);
/*  826:     */                             }
/*  827:     */                             catch (Exception e)
/*  828:     */                             {
/*  829:     */                               Map<Enum<?>, Object> existing;
/*  830:1059 */                               throw new RuntimeException(e);
/*  831:     */                             }
/*  832:     */                             Map<Enum<?>, Object> existing;
/*  833:1062 */                             if (existing != null) {
/*  834:1063 */                               output.writeObject(this.number, existing, this.schema, false);
/*  835:     */                             }
/*  836:     */                           }
/*  837:     */                           
/*  838:     */                           protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  839:     */                             throws IOException
/*  840:     */                           {
/*  841:1070 */                             output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  842:     */                           }
/*  843:     */                           
/*  844:     */                           protected Enum<?> kFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper)
/*  845:     */                             throws IOException
/*  846:     */                           {
/*  847:1077 */                             return eioK.readFrom(input);
/*  848:     */                           }
/*  849:     */                           
/*  850:     */                           protected void kTo(Output output, int fieldNumber, Enum<?> key, boolean repeated)
/*  851:     */                             throws IOException
/*  852:     */                           {
/*  853:1084 */                             eioK.writeTo(output, fieldNumber, repeated, key);
/*  854:     */                           }
/*  855:     */                           
/*  856:     */                           protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  857:     */                             throws IOException
/*  858:     */                           {
/*  859:1091 */                             EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  860:     */                           }
/*  861:     */                           
/*  862:     */                           protected void vPutFrom(Input input, MapSchema.MapWrapper<Enum<?>, Object> wrapper, Enum<?> key)
/*  863:     */                             throws IOException
/*  864:     */                           {
/*  865:1099 */                             Object value = input.mergeObject(wrapper, valueSchema);
/*  866:1100 */                             if (value != wrapper)
/*  867:     */                             {
/*  868:1104 */                               ((GraphInput)input).updateLast(value, wrapper);
/*  869:     */                               
/*  870:1106 */                               wrapper.put(key, value);
/*  871:1107 */                               return;
/*  872:     */                             }
/*  873:1110 */                             if (key != null) {
/*  874:1113 */                               wrapper.put(key, wrapper.setValue(null));
/*  875:     */                             }
/*  876:     */                           }
/*  877:     */                           
/*  878:     */                           protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/*  879:     */                             throws IOException
/*  880:     */                           {
/*  881:1121 */                             output.writeObject(fieldNumber, val, valueSchema, repeated);
/*  882:     */                           }
/*  883:     */                           
/*  884:     */                           protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  885:     */                             throws IOException
/*  886:     */                           {
/*  887:1128 */                             output.writeObject(number, pipe, valuePipeSchema, repeated);
/*  888:     */                           }
/*  889:     */                         };
/*  890:     */                       }
/*  891:     */                       
/*  892:     */                       private static <T> Field<T> createMapPojoKEnumV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, Class<Object> clazzV, final IdStrategy strategy)
/*  893:     */                       {
/*  894:1138 */                         final HasSchema<Object> schemaK = strategy.getSchemaWrapper(clazzK, true);
/*  895:     */                         
/*  896:1140 */                         final EnumIO<?> eioV = strategy.getEnumIO(clazzV);
/*  897:     */                         
/*  898:1142 */                         new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  899:1143 */                           (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  900:     */                           {
/*  901:     */                             protected void mergeFrom(Input input, T message)
/*  902:     */                               throws IOException
/*  903:     */                             {
/*  904:     */                               try
/*  905:     */                               {
/*  906:1155 */                                 f.set(message, input.mergeObject(
/*  907:1156 */                                   (Map)f.get(message), this.schema));
/*  908:     */                               }
/*  909:     */                               catch (Exception e)
/*  910:     */                               {
/*  911:1160 */                                 throw new RuntimeException(e);
/*  912:     */                               }
/*  913:     */                             }
/*  914:     */                             
/*  915:     */                             protected void writeTo(Output output, T message)
/*  916:     */                               throws IOException
/*  917:     */                             {
/*  918:     */                               try
/*  919:     */                               {
/*  920:1171 */                                 existing = (Map)f.get(message);
/*  921:     */                               }
/*  922:     */                               catch (Exception e)
/*  923:     */                               {
/*  924:     */                                 Map<Object, Enum<?>> existing;
/*  925:1175 */                                 throw new RuntimeException(e);
/*  926:     */                               }
/*  927:     */                               Map<Object, Enum<?>> existing;
/*  928:1178 */                               if (existing != null) {
/*  929:1179 */                                 output.writeObject(this.number, existing, this.schema, false);
/*  930:     */                               }
/*  931:     */                             }
/*  932:     */                             
/*  933:     */                             protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  934:     */                               throws IOException
/*  935:     */                             {
/*  936:1186 */                               output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  937:     */                             }
/*  938:     */                             
/*  939:     */                             protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Enum<?>> wrapper)
/*  940:     */                               throws IOException
/*  941:     */                             {
/*  942:1193 */                               return input.mergeObject(null, schemaK.getSchema());
/*  943:     */                             }
/*  944:     */                             
/*  945:     */                             protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/*  946:     */                               throws IOException
/*  947:     */                             {
/*  948:1200 */                               output.writeObject(fieldNumber, key, schemaK.getSchema(), repeated);
/*  949:     */                             }
/*  950:     */                             
/*  951:     */                             protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  952:     */                               throws IOException
/*  953:     */                             {
/*  954:1208 */                               output.writeObject(number, pipe, schemaK.getPipeSchema(), repeated);
/*  955:     */                             }
/*  956:     */                             
/*  957:     */                             protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Enum<?>> wrapper, Object key)
/*  958:     */                               throws IOException
/*  959:     */                             {
/*  960:1217 */                               wrapper.put(key, eioV.readFrom(input));
/*  961:     */                             }
/*  962:     */                             
/*  963:     */                             protected void vTo(Output output, int fieldNumber, Enum<?> val, boolean repeated)
/*  964:     */                               throws IOException
/*  965:     */                             {
/*  966:1224 */                               eioV.writeTo(output, fieldNumber, repeated, val);
/*  967:     */                             }
/*  968:     */                             
/*  969:     */                             protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  970:     */                               throws IOException
/*  971:     */                             {
/*  972:1231 */                               EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/*  973:     */                             }
/*  974:     */                           };
/*  975:     */                         }
/*  976:     */                         
/*  977:     */                         private static <T> Field<T> createMapPojoKInlineV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, final Delegate<Object> inlineV, IdStrategy strategy)
/*  978:     */                         {
/*  979:1241 */                           final HasSchema<Object> schemaK = strategy.getSchemaWrapper(clazzK, true);
/*  980:     */                           
/*  981:     */ 
/*  982:1244 */                           new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/*  983:1245 */                             (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  984:     */                             {
/*  985:     */                               protected void mergeFrom(Input input, T message)
/*  986:     */                                 throws IOException
/*  987:     */                               {
/*  988:     */                                 try
/*  989:     */                                 {
/*  990:1257 */                                   f.set(message, input.mergeObject(
/*  991:1258 */                                     (Map)f.get(message), this.schema));
/*  992:     */                                 }
/*  993:     */                                 catch (Exception e)
/*  994:     */                                 {
/*  995:1262 */                                   throw new RuntimeException(e);
/*  996:     */                                 }
/*  997:     */                               }
/*  998:     */                               
/*  999:     */                               protected void writeTo(Output output, T message)
/* 1000:     */                                 throws IOException
/* 1001:     */                               {
/* 1002:     */                                 try
/* 1003:     */                                 {
/* 1004:1273 */                                   existing = (Map)f.get(message);
/* 1005:     */                                 }
/* 1006:     */                                 catch (Exception e)
/* 1007:     */                                 {
/* 1008:     */                                   Map<Object, Object> existing;
/* 1009:1277 */                                   throw new RuntimeException(e);
/* 1010:     */                                 }
/* 1011:     */                                 Map<Object, Object> existing;
/* 1012:1280 */                                 if (existing != null) {
/* 1013:1281 */                                   output.writeObject(this.number, existing, this.schema, false);
/* 1014:     */                                 }
/* 1015:     */                               }
/* 1016:     */                               
/* 1017:     */                               protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1018:     */                                 throws IOException
/* 1019:     */                               {
/* 1020:1288 */                                 output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 1021:     */                               }
/* 1022:     */                               
/* 1023:     */                               protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/* 1024:     */                                 throws IOException
/* 1025:     */                               {
/* 1026:1295 */                                 return input.mergeObject(null, schemaK.getSchema());
/* 1027:     */                               }
/* 1028:     */                               
/* 1029:     */                               protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/* 1030:     */                                 throws IOException
/* 1031:     */                               {
/* 1032:1302 */                                 output.writeObject(fieldNumber, key, schemaK.getSchema(), repeated);
/* 1033:     */                               }
/* 1034:     */                               
/* 1035:     */                               protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1036:     */                                 throws IOException
/* 1037:     */                               {
/* 1038:1310 */                                 output.writeObject(number, pipe, schemaK.getPipeSchema(), repeated);
/* 1039:     */                               }
/* 1040:     */                               
/* 1041:     */                               protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/* 1042:     */                                 throws IOException
/* 1043:     */                               {
/* 1044:1319 */                                 wrapper.put(key, inlineV.readFrom(input));
/* 1045:     */                               }
/* 1046:     */                               
/* 1047:     */                               protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/* 1048:     */                                 throws IOException
/* 1049:     */                               {
/* 1050:1326 */                                 inlineV.writeTo(output, fieldNumber, val, repeated);
/* 1051:     */                               }
/* 1052:     */                               
/* 1053:     */                               protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1054:     */                                 throws IOException
/* 1055:     */                               {
/* 1056:1333 */                                 inlineV.transfer(pipe, input, output, number, repeated);
/* 1057:     */                               }
/* 1058:     */                             };
/* 1059:     */                           }
/* 1060:     */                           
/* 1061:     */                           private static <T> Field<T> createMapPojoKPojoV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, Class<Object> clazzV, IdStrategy strategy)
/* 1062:     */                           {
/* 1063:1343 */                             final HasSchema<Object> schemaK = strategy.getSchemaWrapper(clazzK, true);
/* 1064:     */                             
/* 1065:1345 */                             final HasSchema<Object> schemaV = strategy.getSchemaWrapper(clazzV, true);
/* 1066:     */                             
/* 1067:     */ 
/* 1068:1348 */                             new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/* 1069:1349 */                               (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 1070:     */                               {
/* 1071:     */                                 protected void mergeFrom(Input input, T message)
/* 1072:     */                                   throws IOException
/* 1073:     */                                 {
/* 1074:     */                                   try
/* 1075:     */                                   {
/* 1076:1361 */                                     f.set(message, input.mergeObject(
/* 1077:1362 */                                       (Map)f.get(message), this.schema));
/* 1078:     */                                   }
/* 1079:     */                                   catch (Exception e)
/* 1080:     */                                   {
/* 1081:1366 */                                     throw new RuntimeException(e);
/* 1082:     */                                   }
/* 1083:     */                                 }
/* 1084:     */                                 
/* 1085:     */                                 protected void writeTo(Output output, T message)
/* 1086:     */                                   throws IOException
/* 1087:     */                                 {
/* 1088:     */                                   try
/* 1089:     */                                   {
/* 1090:1377 */                                     existing = (Map)f.get(message);
/* 1091:     */                                   }
/* 1092:     */                                   catch (Exception e)
/* 1093:     */                                   {
/* 1094:     */                                     Map<Object, Object> existing;
/* 1095:1381 */                                     throw new RuntimeException(e);
/* 1096:     */                                   }
/* 1097:     */                                   Map<Object, Object> existing;
/* 1098:1384 */                                   if (existing != null) {
/* 1099:1385 */                                     output.writeObject(this.number, existing, this.schema, false);
/* 1100:     */                                   }
/* 1101:     */                                 }
/* 1102:     */                                 
/* 1103:     */                                 protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1104:     */                                   throws IOException
/* 1105:     */                                 {
/* 1106:1392 */                                   output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 1107:     */                                 }
/* 1108:     */                                 
/* 1109:     */                                 protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/* 1110:     */                                   throws IOException
/* 1111:     */                                 {
/* 1112:1399 */                                   return input.mergeObject(null, schemaK.getSchema());
/* 1113:     */                                 }
/* 1114:     */                                 
/* 1115:     */                                 protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/* 1116:     */                                   throws IOException
/* 1117:     */                                 {
/* 1118:1406 */                                   output.writeObject(fieldNumber, key, schemaK.getSchema(), repeated);
/* 1119:     */                                 }
/* 1120:     */                                 
/* 1121:     */                                 protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1122:     */                                   throws IOException
/* 1123:     */                                 {
/* 1124:1414 */                                   output.writeObject(number, pipe, schemaK.getPipeSchema(), repeated);
/* 1125:     */                                 }
/* 1126:     */                                 
/* 1127:     */                                 protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/* 1128:     */                                   throws IOException
/* 1129:     */                                 {
/* 1130:1423 */                                   wrapper.put(key, input.mergeObject(null, schemaV.getSchema()));
/* 1131:     */                                 }
/* 1132:     */                                 
/* 1133:     */                                 protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/* 1134:     */                                   throws IOException
/* 1135:     */                                 {
/* 1136:1430 */                                   output.writeObject(fieldNumber, val, schemaV.getSchema(), repeated);
/* 1137:     */                                 }
/* 1138:     */                                 
/* 1139:     */                                 protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1140:     */                                   throws IOException
/* 1141:     */                                 {
/* 1142:1438 */                                   output.writeObject(number, pipe, schemaV.getPipeSchema(), repeated);
/* 1143:     */                                 }
/* 1144:     */                               };
/* 1145:     */                             }
/* 1146:     */                             
/* 1147:     */                             private static <T> Field<T> createMapPojoKPolymorphicV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, Class<Object> clazzV, final IdStrategy strategy)
/* 1148:     */                             {
/* 1149:1449 */                               final HasSchema<Object> schemaK = strategy.getSchemaWrapper(clazzK, true);
/* 1150:     */                               
/* 1151:     */ 
/* 1152:1452 */                               new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/* 1153:1453 */                                 (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 1154:     */                                 {
/* 1155:     */                                   protected void mergeFrom(Input input, T message)
/* 1156:     */                                     throws IOException
/* 1157:     */                                   {
/* 1158:     */                                     try
/* 1159:     */                                     {
/* 1160:1466 */                                       f.set(message, input.mergeObject(
/* 1161:1467 */                                         (Map)f.get(message), this.schema));
/* 1162:     */                                     }
/* 1163:     */                                     catch (Exception e)
/* 1164:     */                                     {
/* 1165:1471 */                                       throw new RuntimeException(e);
/* 1166:     */                                     }
/* 1167:     */                                   }
/* 1168:     */                                   
/* 1169:     */                                   protected void writeTo(Output output, T message)
/* 1170:     */                                     throws IOException
/* 1171:     */                                   {
/* 1172:     */                                     try
/* 1173:     */                                     {
/* 1174:1482 */                                       existing = (Map)f.get(message);
/* 1175:     */                                     }
/* 1176:     */                                     catch (Exception e)
/* 1177:     */                                     {
/* 1178:     */                                       Map<Object, Object> existing;
/* 1179:1486 */                                       throw new RuntimeException(e);
/* 1180:     */                                     }
/* 1181:     */                                     Map<Object, Object> existing;
/* 1182:1489 */                                     if (existing != null) {
/* 1183:1490 */                                       output.writeObject(this.number, existing, this.schema, false);
/* 1184:     */                                     }
/* 1185:     */                                   }
/* 1186:     */                                   
/* 1187:     */                                   protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1188:     */                                     throws IOException
/* 1189:     */                                   {
/* 1190:1497 */                                     output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 1191:     */                                   }
/* 1192:     */                                   
/* 1193:     */                                   protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/* 1194:     */                                     throws IOException
/* 1195:     */                                   {
/* 1196:1504 */                                     return input.mergeObject(null, schemaK.getSchema());
/* 1197:     */                                   }
/* 1198:     */                                   
/* 1199:     */                                   protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1200:     */                                     throws IOException
/* 1201:     */                                   {
/* 1202:1511 */                                     output.writeObject(number, pipe, schemaK.getPipeSchema(), repeated);
/* 1203:     */                                   }
/* 1204:     */                                   
/* 1205:     */                                   protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/* 1206:     */                                     throws IOException
/* 1207:     */                                   {
/* 1208:1519 */                                     output.writeObject(fieldNumber, key, schemaK.getSchema(), repeated);
/* 1209:     */                                   }
/* 1210:     */                                   
/* 1211:     */                                   protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/* 1212:     */                                     throws IOException
/* 1213:     */                                   {
/* 1214:1528 */                                     Object value = input.mergeObject(wrapper, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA);
/* 1215:1530 */                                     if (value != wrapper)
/* 1216:     */                                     {
/* 1217:1534 */                                       ((GraphInput)input).updateLast(value, wrapper);
/* 1218:     */                                       
/* 1219:1536 */                                       wrapper.put(key, value);
/* 1220:1537 */                                       return;
/* 1221:     */                                     }
/* 1222:1540 */                                     if (key != null) {
/* 1223:1543 */                                       wrapper.put(key, wrapper.setValue(null));
/* 1224:     */                                     }
/* 1225:     */                                   }
/* 1226:     */                                   
/* 1227:     */                                   protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/* 1228:     */                                     throws IOException
/* 1229:     */                                   {
/* 1230:1551 */                                     output.writeObject(fieldNumber, val, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA, repeated);
/* 1231:     */                                   }
/* 1232:     */                                   
/* 1233:     */                                   protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1234:     */                                     throws IOException
/* 1235:     */                                   {
/* 1236:1559 */                                     output.writeObject(number, pipe, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA.pipeSchema, repeated);
/* 1237:     */                                   }
/* 1238:     */                                 };
/* 1239:     */                               }
/* 1240:     */                               
/* 1241:     */                               private static <T> Field<T> createMapPojoKObjectV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, Class<Object> clazzK, final Schema<Object> valueSchema, final Pipe.Schema<Object> valuePipeSchema, IdStrategy strategy)
/* 1242:     */                               {
/* 1243:1571 */                                 final HasSchema<Object> schemaK = strategy.getSchemaWrapper(clazzK, true);
/* 1244:     */                                 
/* 1245:     */ 
/* 1246:1574 */                                 new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/* 1247:1575 */                                   (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 1248:     */                                   {
/* 1249:     */                                     protected void mergeFrom(Input input, T message)
/* 1250:     */                                       throws IOException
/* 1251:     */                                     {
/* 1252:     */                                       try
/* 1253:     */                                       {
/* 1254:1588 */                                         f.set(message, input.mergeObject(
/* 1255:1589 */                                           (Map)f.get(message), this.schema));
/* 1256:     */                                       }
/* 1257:     */                                       catch (Exception e)
/* 1258:     */                                       {
/* 1259:1593 */                                         throw new RuntimeException(e);
/* 1260:     */                                       }
/* 1261:     */                                     }
/* 1262:     */                                     
/* 1263:     */                                     protected void writeTo(Output output, T message)
/* 1264:     */                                       throws IOException
/* 1265:     */                                     {
/* 1266:     */                                       try
/* 1267:     */                                       {
/* 1268:1604 */                                         existing = (Map)f.get(message);
/* 1269:     */                                       }
/* 1270:     */                                       catch (Exception e)
/* 1271:     */                                       {
/* 1272:     */                                         Map<Object, Object> existing;
/* 1273:1608 */                                         throw new RuntimeException(e);
/* 1274:     */                                       }
/* 1275:     */                                       Map<Object, Object> existing;
/* 1276:1611 */                                       if (existing != null) {
/* 1277:1612 */                                         output.writeObject(this.number, existing, this.schema, false);
/* 1278:     */                                       }
/* 1279:     */                                     }
/* 1280:     */                                     
/* 1281:     */                                     protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1282:     */                                       throws IOException
/* 1283:     */                                     {
/* 1284:1619 */                                       output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 1285:     */                                     }
/* 1286:     */                                     
/* 1287:     */                                     protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/* 1288:     */                                       throws IOException
/* 1289:     */                                     {
/* 1290:1626 */                                       return input.mergeObject(null, schemaK.getSchema());
/* 1291:     */                                     }
/* 1292:     */                                     
/* 1293:     */                                     protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1294:     */                                       throws IOException
/* 1295:     */                                     {
/* 1296:1633 */                                       output.writeObject(number, pipe, schemaK.getPipeSchema(), repeated);
/* 1297:     */                                     }
/* 1298:     */                                     
/* 1299:     */                                     protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/* 1300:     */                                       throws IOException
/* 1301:     */                                     {
/* 1302:1641 */                                       output.writeObject(fieldNumber, key, schemaK.getSchema(), repeated);
/* 1303:     */                                     }
/* 1304:     */                                     
/* 1305:     */                                     protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/* 1306:     */                                       throws IOException
/* 1307:     */                                     {
/* 1308:1650 */                                       Object value = input.mergeObject(wrapper, valueSchema);
/* 1309:1651 */                                       if (value != wrapper)
/* 1310:     */                                       {
/* 1311:1655 */                                         ((GraphInput)input).updateLast(value, wrapper);
/* 1312:     */                                         
/* 1313:1657 */                                         wrapper.put(key, value);
/* 1314:1658 */                                         return;
/* 1315:     */                                       }
/* 1316:1661 */                                       if (key != null) {
/* 1317:1664 */                                         wrapper.put(key, wrapper.setValue(null));
/* 1318:     */                                       }
/* 1319:     */                                     }
/* 1320:     */                                     
/* 1321:     */                                     protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/* 1322:     */                                       throws IOException
/* 1323:     */                                     {
/* 1324:1672 */                                       output.writeObject(fieldNumber, val, valueSchema, repeated);
/* 1325:     */                                     }
/* 1326:     */                                     
/* 1327:     */                                     protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1328:     */                                       throws IOException
/* 1329:     */                                     {
/* 1330:1679 */                                       output.writeObject(number, pipe, valuePipeSchema, repeated);
/* 1331:     */                                     }
/* 1332:     */                                   };
/* 1333:     */                                 }
/* 1334:     */                                 
/* 1335:     */                                 private static <T> Field<T> createMapObjectKObjectV(int number, String name, final java.lang.reflect.Field f, MapSchema.MessageFactory messageFactory, final Schema<Object> keySchema, final Pipe.Schema<Object> keyPipeSchema, final Schema<Object> valueSchema, final Pipe.Schema<Object> valuePipeSchema, IdStrategy strategy)
/* 1336:     */                                 {
/* 1337:1691 */                                   new RuntimeMapField(WireFormat.FieldType.MESSAGE, number, name, 
/* 1338:1692 */                                     (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 1339:     */                                     {
/* 1340:     */                                       protected void mergeFrom(Input input, T message)
/* 1341:     */                                         throws IOException
/* 1342:     */                                       {
/* 1343:     */                                         try
/* 1344:     */                                         {
/* 1345:1705 */                                           f.set(message, input.mergeObject(
/* 1346:1706 */                                             (Map)f.get(message), this.schema));
/* 1347:     */                                         }
/* 1348:     */                                         catch (Exception e)
/* 1349:     */                                         {
/* 1350:1710 */                                           throw new RuntimeException(e);
/* 1351:     */                                         }
/* 1352:     */                                       }
/* 1353:     */                                       
/* 1354:     */                                       protected void writeTo(Output output, T message)
/* 1355:     */                                         throws IOException
/* 1356:     */                                       {
/* 1357:     */                                         try
/* 1358:     */                                         {
/* 1359:1721 */                                           existing = (Map)f.get(message);
/* 1360:     */                                         }
/* 1361:     */                                         catch (Exception e)
/* 1362:     */                                         {
/* 1363:     */                                           Map<Object, Object> existing;
/* 1364:1725 */                                           throw new RuntimeException(e);
/* 1365:     */                                         }
/* 1366:     */                                         Map<Object, Object> existing;
/* 1367:1728 */                                         if (existing != null) {
/* 1368:1729 */                                           output.writeObject(this.number, existing, this.schema, false);
/* 1369:     */                                         }
/* 1370:     */                                       }
/* 1371:     */                                       
/* 1372:     */                                       protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1373:     */                                         throws IOException
/* 1374:     */                                       {
/* 1375:1736 */                                         output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 1376:     */                                       }
/* 1377:     */                                       
/* 1378:     */                                       protected Object kFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper)
/* 1379:     */                                         throws IOException
/* 1380:     */                                       {
/* 1381:1743 */                                         Object value = input.mergeObject(wrapper, keySchema);
/* 1382:1744 */                                         if (value != wrapper)
/* 1383:     */                                         {
/* 1384:1748 */                                           ((GraphInput)input).updateLast(value, wrapper);
/* 1385:1749 */                                           return value;
/* 1386:     */                                         }
/* 1387:1752 */                                         return wrapper.setValue(null);
/* 1388:     */                                       }
/* 1389:     */                                       
/* 1390:     */                                       protected void kTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1391:     */                                         throws IOException
/* 1392:     */                                       {
/* 1393:1759 */                                         output.writeObject(number, pipe, keyPipeSchema, repeated);
/* 1394:     */                                       }
/* 1395:     */                                       
/* 1396:     */                                       protected void kTo(Output output, int fieldNumber, Object key, boolean repeated)
/* 1397:     */                                         throws IOException
/* 1398:     */                                       {
/* 1399:1766 */                                         output.writeObject(fieldNumber, key, keySchema, repeated);
/* 1400:     */                                       }
/* 1401:     */                                       
/* 1402:     */                                       protected void vPutFrom(Input input, MapSchema.MapWrapper<Object, Object> wrapper, Object key)
/* 1403:     */                                         throws IOException
/* 1404:     */                                       {
/* 1405:1774 */                                         Object value = input.mergeObject(wrapper, valueSchema);
/* 1406:1775 */                                         if (value != wrapper)
/* 1407:     */                                         {
/* 1408:1779 */                                           ((GraphInput)input).updateLast(value, wrapper);
/* 1409:     */                                           
/* 1410:1781 */                                           wrapper.put(key, value);
/* 1411:1782 */                                           return;
/* 1412:     */                                         }
/* 1413:1785 */                                         if (key != null) {
/* 1414:1788 */                                           wrapper.put(key, wrapper.setValue(null));
/* 1415:     */                                         }
/* 1416:     */                                       }
/* 1417:     */                                       
/* 1418:     */                                       protected void vTo(Output output, int fieldNumber, Object val, boolean repeated)
/* 1419:     */                                         throws IOException
/* 1420:     */                                       {
/* 1421:1796 */                                         output.writeObject(fieldNumber, val, valueSchema, repeated);
/* 1422:     */                                       }
/* 1423:     */                                       
/* 1424:     */                                       protected void vTransfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1425:     */                                         throws IOException
/* 1426:     */                                       {
/* 1427:1803 */                                         output.writeObject(number, pipe, valuePipeSchema, repeated);
/* 1428:     */                                       }
/* 1429:     */                                     };
/* 1430:     */                                   }
/* 1431:     */                                   
/* 1432:1808 */                                   static final RuntimeFieldFactory<Map<?, ?>> MAP = new RuntimeFieldFactory(26)
/* 1433:     */                                   {
/* 1434:     */                                     public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 1435:     */                                     {
/* 1436:1817 */                                       Class<?> clazz = f.getType();
/* 1437:1818 */                                       Morph morph = (Morph)f.getAnnotation(Morph.class);
/* 1438:1820 */                                       if ((0 != (0x100 & strategy.flags)) && ((morph == null) || 
/* 1439:1821 */                                         (morph.value())))
/* 1440:     */                                       {
/* 1441:1823 */                                         if ((!clazz.getName().startsWith("java.util")) && 
/* 1442:1824 */                                           (pojo(clazz, morph, strategy))) {
/* 1443:1826 */                                           return POJO.create(number, name, f, strategy);
/* 1444:     */                                         }
/* 1445:1829 */                                         return OBJECT.create(number, name, f, strategy);
/* 1446:     */                                       }
/* 1447:1832 */                                       if (Modifier.isAbstract(clazz.getModifiers()))
/* 1448:     */                                       {
/* 1449:1834 */                                         if (!clazz.isInterface()) {
/* 1450:1837 */                                           return OBJECT.create(number, name, f, strategy);
/* 1451:     */                                         }
/* 1452:1840 */                                         if (morph == null)
/* 1453:     */                                         {
/* 1454:1842 */                                           if (0 != (0x20 & strategy.flags)) {
/* 1455:1843 */                                             return OBJECT.create(number, name, f, strategy);
/* 1456:     */                                           }
/* 1457:     */                                         }
/* 1458:1845 */                                         else if (morph.value()) {
/* 1459:1846 */                                           return OBJECT.create(number, name, f, strategy);
/* 1460:     */                                         }
/* 1461:     */                                       }
/* 1462:     */                                       MapSchema.MessageFactory messageFactory;
/* 1463:     */                                       MapSchema.MessageFactory messageFactory;
/* 1464:1850 */                                       if (EnumMap.class.isAssignableFrom(f.getType()))
/* 1465:     */                                       {
/* 1466:1852 */                                         Class<Object> enumType = getGenericType(f, 0);
/* 1467:1854 */                                         if (enumType == null) {
/* 1468:1858 */                                           return RuntimeFieldFactory.OBJECT.create(number, name, f, strategy);
/* 1469:     */                                         }
/* 1470:1863 */                                         messageFactory = strategy.getEnumIO(enumType).getEnumMapFactory();
/* 1471:     */                                       }
/* 1472:     */                                       else
/* 1473:     */                                       {
/* 1474:1867 */                                         messageFactory = strategy.getMapFactory(f.getType());
/* 1475:     */                                       }
/* 1476:1870 */                                       Class<Object> clazzK = getGenericType(f, 0);
/* 1477:1871 */                                       if ((clazzK == null) || (((Map.class.isAssignableFrom(clazzK)) || 
/* 1478:1872 */                                         (Collection.class.isAssignableFrom(clazzK))) && 
/* 1479:1873 */                                         (!strategy.isRegistered(clazzK)))) {
/* 1480:1876 */                                         return RuntimeMapFieldFactory.createMapObjectKObjectV(number, name, f, messageFactory, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1481:     */                                       }
/* 1482:1883 */                                       Class<Object> clazzV = getGenericType(f, 1);
/* 1483:1884 */                                       if ((clazzV == null) || (((Map.class.isAssignableFrom(clazzV)) || 
/* 1484:1885 */                                         (Collection.class.isAssignableFrom(clazzV))) && 
/* 1485:1886 */                                         (!strategy.isRegistered(clazzV))))
/* 1486:     */                                       {
/* 1487:1889 */                                         Delegate<Object> inlineK = getDelegateOrInline(clazzK, strategy);
/* 1488:1891 */                                         if (inlineK != null) {
/* 1489:1893 */                                           return RuntimeMapFieldFactory.createMapInlineKObjectV(number, name, f, messageFactory, inlineK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1490:     */                                         }
/* 1491:1899 */                                         if (Message.class.isAssignableFrom(clazzK)) {
/* 1492:1901 */                                           return RuntimeMapFieldFactory.createMapPojoKObjectV(number, name, f, messageFactory, clazzK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1493:     */                                         }
/* 1494:1907 */                                         if (clazzK.isEnum()) {
/* 1495:1909 */                                           return RuntimeMapFieldFactory.createMapEnumKObjectV(number, name, f, messageFactory, clazzK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1496:     */                                         }
/* 1497:1916 */                                         PolymorphicSchema psK = PolymorphicSchemaFactories.getSchemaFromCollectionOrMapGenericType(clazzK, strategy);
/* 1498:1919 */                                         if (psK != null) {
/* 1499:1921 */                                           return RuntimeMapFieldFactory.createMapObjectKObjectV(number, name, f, messageFactory, psK, psK
/* 1500:1922 */                                             .getPipeSchema(), strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1501:     */                                         }
/* 1502:1927 */                                         if (pojo(clazzK, morph, strategy)) {
/* 1503:1929 */                                           return RuntimeMapFieldFactory.createMapPojoKObjectV(number, name, f, messageFactory, clazzK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1504:     */                                         }
/* 1505:1943 */                                         return RuntimeMapFieldFactory.createMapObjectKObjectV(number, name, f, messageFactory, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1506:     */                                       }
/* 1507:1950 */                                       Delegate<Object> inlineK = getDelegateOrInline(clazzK, strategy);
/* 1508:1953 */                                       if (inlineK != null)
/* 1509:     */                                       {
/* 1510:1955 */                                         Delegate<Object> inlineV = getDelegateOrInline(clazzV, strategy);
/* 1511:1957 */                                         if (inlineV != null) {
/* 1512:1958 */                                           return RuntimeMapFieldFactory.createMapInlineKInlineV(number, name, f, messageFactory, inlineK, inlineV);
/* 1513:     */                                         }
/* 1514:1961 */                                         if (Message.class.isAssignableFrom(clazzV)) {
/* 1515:1962 */                                           return RuntimeMapFieldFactory.createMapInlineKPojoV(number, name, f, messageFactory, inlineK, clazzV, strategy);
/* 1516:     */                                         }
/* 1517:1965 */                                         if (clazzV.isEnum()) {
/* 1518:1966 */                                           return RuntimeMapFieldFactory.createMapInlineKEnumV(number, name, f, messageFactory, inlineK, clazzV, strategy);
/* 1519:     */                                         }
/* 1520:1970 */                                         PolymorphicSchema psV = PolymorphicSchemaFactories.getSchemaFromCollectionOrMapGenericType(clazzV, strategy);
/* 1521:1972 */                                         if (psV != null) {
/* 1522:1974 */                                           return RuntimeMapFieldFactory.createMapInlineKObjectV(number, name, f, messageFactory, inlineK, psV, psV
/* 1523:1975 */                                             .getPipeSchema(), strategy);
/* 1524:     */                                         }
/* 1525:1979 */                                         if (pojo(clazzV, (Morph)f.getAnnotation(Morph.class), strategy)) {
/* 1526:1980 */                                           return RuntimeMapFieldFactory.createMapInlineKPojoV(number, name, f, messageFactory, inlineK, clazzV, strategy);
/* 1527:     */                                         }
/* 1528:1983 */                                         if (clazzV.isInterface()) {
/* 1529:1985 */                                           return RuntimeMapFieldFactory.createMapInlineKObjectV(number, name, f, messageFactory, inlineK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1530:     */                                         }
/* 1531:1991 */                                         return RuntimeMapFieldFactory.createMapInlineKPolymorphicV(number, name, f, messageFactory, inlineK, clazzV, strategy);
/* 1532:     */                                       }
/* 1533:1995 */                                       if (clazzK.isEnum())
/* 1534:     */                                       {
/* 1535:1997 */                                         Delegate<Object> inlineV = getDelegateOrInline(clazzV, strategy);
/* 1536:1999 */                                         if (inlineV != null) {
/* 1537:2000 */                                           return RuntimeMapFieldFactory.createMapEnumKInlineV(number, name, f, messageFactory, clazzK, inlineV, strategy);
/* 1538:     */                                         }
/* 1539:2003 */                                         if (Message.class.isAssignableFrom(clazzV)) {
/* 1540:2004 */                                           return RuntimeMapFieldFactory.createMapEnumKPojoV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1541:     */                                         }
/* 1542:2007 */                                         if (clazzV.isEnum()) {
/* 1543:2008 */                                           return RuntimeMapFieldFactory.createMapEnumKEnumV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1544:     */                                         }
/* 1545:2012 */                                         PolymorphicSchema psV = PolymorphicSchemaFactories.getSchemaFromCollectionOrMapGenericType(clazzV, strategy);
/* 1546:2014 */                                         if (psV != null) {
/* 1547:2016 */                                           return RuntimeMapFieldFactory.createMapEnumKObjectV(number, name, f, messageFactory, clazzK, psV, psV
/* 1548:2017 */                                             .getPipeSchema(), strategy);
/* 1549:     */                                         }
/* 1550:2021 */                                         if (pojo(clazzV, (Morph)f.getAnnotation(Morph.class), strategy)) {
/* 1551:2022 */                                           return RuntimeMapFieldFactory.createMapEnumKPojoV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1552:     */                                         }
/* 1553:2025 */                                         if (clazzV.isInterface()) {
/* 1554:2027 */                                           return RuntimeMapFieldFactory.createMapEnumKObjectV(number, name, f, messageFactory, clazzK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1555:     */                                         }
/* 1556:2033 */                                         return RuntimeMapFieldFactory.createMapEnumKPolymorphicV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1557:     */                                       }
/* 1558:2038 */                                       PolymorphicSchema psK = PolymorphicSchemaFactories.getSchemaFromCollectionOrMapGenericType(clazzK, strategy);
/* 1559:2039 */                                       if (psK != null) {
/* 1560:2041 */                                         return RuntimeMapFieldFactory.createMapObjectKObjectV(number, name, f, messageFactory, psK, psK
/* 1561:2042 */                                           .getPipeSchema(), strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1562:     */                                       }
/* 1563:2047 */                                       if (pojo(clazzK, (Morph)f.getAnnotation(Morph.class), strategy))
/* 1564:     */                                       {
/* 1565:2049 */                                         Delegate<Object> inlineV = getDelegateOrInline(clazzV, strategy);
/* 1566:2051 */                                         if (inlineV != null) {
/* 1567:2052 */                                           return RuntimeMapFieldFactory.createMapPojoKInlineV(number, name, f, messageFactory, clazzK, inlineV, strategy);
/* 1568:     */                                         }
/* 1569:2055 */                                         if (Message.class.isAssignableFrom(clazzV)) {
/* 1570:2056 */                                           return RuntimeMapFieldFactory.createMapPojoKPojoV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1571:     */                                         }
/* 1572:2059 */                                         if (clazzV.isEnum()) {
/* 1573:2060 */                                           return RuntimeMapFieldFactory.createMapPojoKEnumV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1574:     */                                         }
/* 1575:2064 */                                         PolymorphicSchema psV = PolymorphicSchemaFactories.getSchemaFromCollectionOrMapGenericType(clazzV, strategy);
/* 1576:2066 */                                         if (psV != null) {
/* 1577:2068 */                                           return RuntimeMapFieldFactory.createMapPojoKObjectV(number, name, f, messageFactory, clazzK, psV, psV
/* 1578:2069 */                                             .getPipeSchema(), strategy);
/* 1579:     */                                         }
/* 1580:2073 */                                         if (pojo(clazzV, (Morph)f.getAnnotation(Morph.class), strategy)) {
/* 1581:2074 */                                           return RuntimeMapFieldFactory.createMapPojoKPojoV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1582:     */                                         }
/* 1583:2077 */                                         if (clazzV.isInterface()) {
/* 1584:2079 */                                           return RuntimeMapFieldFactory.createMapPojoKObjectV(number, name, f, messageFactory, clazzK, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1585:     */                                         }
/* 1586:2085 */                                         return RuntimeMapFieldFactory.createMapPojoKPolymorphicV(number, name, f, messageFactory, clazzK, clazzV, strategy);
/* 1587:     */                                       }
/* 1588:2089 */                                       return RuntimeMapFieldFactory.createMapObjectKObjectV(number, name, f, messageFactory, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 1589:     */                                     }
/* 1590:     */                                     
/* 1591:     */                                     public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1592:     */                                       throws IOException
/* 1593:     */                                     {
/* 1594:2100 */                                       throw new UnsupportedOperationException();
/* 1595:     */                                     }
/* 1596:     */                                     
/* 1597:     */                                     public Map<?, ?> readFrom(Input input)
/* 1598:     */                                       throws IOException
/* 1599:     */                                     {
/* 1600:2106 */                                       throw new UnsupportedOperationException();
/* 1601:     */                                     }
/* 1602:     */                                     
/* 1603:     */                                     public void writeTo(Output output, int number, Map<?, ?> value, boolean repeated)
/* 1604:     */                                       throws IOException
/* 1605:     */                                     {
/* 1606:2113 */                                       throw new UnsupportedOperationException();
/* 1607:     */                                     }
/* 1608:     */                                     
/* 1609:     */                                     public WireFormat.FieldType getFieldType()
/* 1610:     */                                     {
/* 1611:2119 */                                       throw new UnsupportedOperationException();
/* 1612:     */                                     }
/* 1613:     */                                     
/* 1614:     */                                     public Class<?> typeClass()
/* 1615:     */                                     {
/* 1616:2125 */                                       throw new UnsupportedOperationException();
/* 1617:     */                                     }
/* 1618:     */                                   };
/* 1619:     */                                 }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeMapFieldFactory
 * JD-Core Version:    0.7.0.1
 */