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
/*   16:     */ import sun.misc.Unsafe;
/*   17:     */ 
/*   18:     */ public final class RuntimeUnsafeFieldFactory
/*   19:     */ {
/*   20:  60 */   static final Unsafe us = ;
/*   21:     */   
/*   22:     */   private static Unsafe initUnsafe()
/*   23:     */   {
/*   24:     */     try
/*   25:     */     {
/*   26:  67 */       java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
/*   27:     */       
/*   28:  69 */       f.setAccessible(true);
/*   29:     */       
/*   30:  71 */       return (Unsafe)f.get(null);
/*   31:     */     }
/*   32:     */     catch (Exception localException) {}
/*   33:  86 */     return Unsafe.getUnsafe();
/*   34:     */   }
/*   35:     */   
/*   36:  93 */   public static final RuntimeFieldFactory<Character> CHAR = new RuntimeFieldFactory(3)
/*   37:     */   {
/*   38:     */     public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*   39:     */     {
/*   40: 101 */       final boolean primitive = f.getType().isPrimitive();
/*   41: 102 */       final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*   42: 103 */       new Field(WireFormat.FieldType.UINT32, number, name, 
/*   43: 104 */         (Tag)f.getAnnotation(Tag.class))
/*   44:     */         {
/*   45:     */           public void mergeFrom(Input input, T message)
/*   46:     */             throws IOException
/*   47:     */           {
/*   48: 110 */             if (primitive) {
/*   49: 111 */               RuntimeUnsafeFieldFactory.us.putChar(message, offset, (char)input.readUInt32());
/*   50:     */             } else {
/*   51: 113 */               RuntimeUnsafeFieldFactory.us.putObject(message, offset, 
/*   52: 114 */                 Character.valueOf((char)input.readUInt32()));
/*   53:     */             }
/*   54:     */           }
/*   55:     */           
/*   56:     */           public void writeTo(Output output, T message)
/*   57:     */             throws IOException
/*   58:     */           {
/*   59: 121 */             if (primitive)
/*   60:     */             {
/*   61: 122 */               output.writeUInt32(this.number, RuntimeUnsafeFieldFactory.us.getChar(message, offset), false);
/*   62:     */             }
/*   63:     */             else
/*   64:     */             {
/*   65: 126 */               Character value = (Character)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*   66: 128 */               if (value != null) {
/*   67: 129 */                 output.writeUInt32(this.number, value.charValue(), false);
/*   68:     */               }
/*   69:     */             }
/*   70:     */           }
/*   71:     */           
/*   72:     */           public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*   73:     */             throws IOException
/*   74:     */           {
/*   75: 137 */             output.writeUInt32(this.number, input.readUInt32(), repeated);
/*   76:     */           }
/*   77:     */         };
/*   78:     */       }
/*   79:     */       
/*   80:     */       public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*   81:     */         throws IOException
/*   82:     */       {
/*   83: 146 */         output.writeUInt32(number, input.readUInt32(), repeated);
/*   84:     */       }
/*   85:     */       
/*   86:     */       public Character readFrom(Input input)
/*   87:     */         throws IOException
/*   88:     */       {
/*   89: 152 */         return Character.valueOf((char)input.readUInt32());
/*   90:     */       }
/*   91:     */       
/*   92:     */       public void writeTo(Output output, int number, Character value, boolean repeated)
/*   93:     */         throws IOException
/*   94:     */       {
/*   95: 159 */         output.writeUInt32(number, value.charValue(), repeated);
/*   96:     */       }
/*   97:     */       
/*   98:     */       public WireFormat.FieldType getFieldType()
/*   99:     */       {
/*  100: 165 */         return WireFormat.FieldType.UINT32;
/*  101:     */       }
/*  102:     */       
/*  103:     */       public Class<?> typeClass()
/*  104:     */       {
/*  105: 171 */         return Character.class;
/*  106:     */       }
/*  107:     */     };
/*  108: 175 */     public static final RuntimeFieldFactory<Short> SHORT = new RuntimeFieldFactory(4)
/*  109:     */     {
/*  110:     */       public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  111:     */       {
/*  112: 182 */         final boolean primitive = f.getType().isPrimitive();
/*  113: 183 */         final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  114: 184 */         new Field(WireFormat.FieldType.UINT32, number, name, 
/*  115: 185 */           (Tag)f.getAnnotation(Tag.class))
/*  116:     */           {
/*  117:     */             public void mergeFrom(Input input, T message)
/*  118:     */               throws IOException
/*  119:     */             {
/*  120: 191 */               if (primitive) {
/*  121: 192 */                 RuntimeUnsafeFieldFactory.us.putShort(message, offset, (short)input.readUInt32());
/*  122:     */               } else {
/*  123: 194 */                 RuntimeUnsafeFieldFactory.us.putObject(message, offset, 
/*  124: 195 */                   Short.valueOf((short)input.readUInt32()));
/*  125:     */               }
/*  126:     */             }
/*  127:     */             
/*  128:     */             public void writeTo(Output output, T message)
/*  129:     */               throws IOException
/*  130:     */             {
/*  131: 202 */               if (primitive)
/*  132:     */               {
/*  133: 203 */                 output.writeUInt32(this.number, RuntimeUnsafeFieldFactory.us
/*  134: 204 */                   .getShort(message, offset), false);
/*  135:     */               }
/*  136:     */               else
/*  137:     */               {
/*  138: 207 */                 Short value = (Short)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  139: 208 */                 if (value != null) {
/*  140: 209 */                   output.writeUInt32(this.number, value.shortValue(), false);
/*  141:     */                 }
/*  142:     */               }
/*  143:     */             }
/*  144:     */             
/*  145:     */             public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  146:     */               throws IOException
/*  147:     */             {
/*  148: 218 */               output.writeUInt32(this.number, input.readUInt32(), repeated);
/*  149:     */             }
/*  150:     */           };
/*  151:     */         }
/*  152:     */         
/*  153:     */         public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  154:     */           throws IOException
/*  155:     */         {
/*  156: 227 */           output.writeUInt32(number, input.readUInt32(), repeated);
/*  157:     */         }
/*  158:     */         
/*  159:     */         public Short readFrom(Input input)
/*  160:     */           throws IOException
/*  161:     */         {
/*  162: 233 */           return Short.valueOf((short)input.readUInt32());
/*  163:     */         }
/*  164:     */         
/*  165:     */         public void writeTo(Output output, int number, Short value, boolean repeated)
/*  166:     */           throws IOException
/*  167:     */         {
/*  168: 240 */           output.writeUInt32(number, value.shortValue(), repeated);
/*  169:     */         }
/*  170:     */         
/*  171:     */         public WireFormat.FieldType getFieldType()
/*  172:     */         {
/*  173: 246 */           return WireFormat.FieldType.UINT32;
/*  174:     */         }
/*  175:     */         
/*  176:     */         public Class<?> typeClass()
/*  177:     */         {
/*  178: 252 */           return Short.class;
/*  179:     */         }
/*  180:     */       };
/*  181: 256 */       public static final RuntimeFieldFactory<Byte> BYTE = new RuntimeFieldFactory(2)
/*  182:     */       {
/*  183:     */         public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  184:     */         {
/*  185: 263 */           final boolean primitive = f.getType().isPrimitive();
/*  186: 264 */           final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  187: 265 */           new Field(WireFormat.FieldType.UINT32, number, name, 
/*  188: 266 */             (Tag)f.getAnnotation(Tag.class))
/*  189:     */             {
/*  190:     */               public void mergeFrom(Input input, T message)
/*  191:     */                 throws IOException
/*  192:     */               {
/*  193: 272 */                 if (primitive) {
/*  194: 273 */                   RuntimeUnsafeFieldFactory.us.putByte(message, offset, (byte)input.readUInt32());
/*  195:     */                 } else {
/*  196: 275 */                   RuntimeUnsafeFieldFactory.us.putObject(message, offset, 
/*  197: 276 */                     Byte.valueOf((byte)input.readUInt32()));
/*  198:     */                 }
/*  199:     */               }
/*  200:     */               
/*  201:     */               public void writeTo(Output output, T message)
/*  202:     */                 throws IOException
/*  203:     */               {
/*  204: 283 */                 if (primitive)
/*  205:     */                 {
/*  206: 284 */                   output.writeUInt32(this.number, RuntimeUnsafeFieldFactory.us.getByte(message, offset), false);
/*  207:     */                 }
/*  208:     */                 else
/*  209:     */                 {
/*  210: 288 */                   Byte value = (Byte)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  211: 289 */                   if (value != null) {
/*  212: 290 */                     output.writeUInt32(this.number, value.byteValue(), false);
/*  213:     */                   }
/*  214:     */                 }
/*  215:     */               }
/*  216:     */               
/*  217:     */               public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  218:     */                 throws IOException
/*  219:     */               {
/*  220: 298 */                 output.writeUInt32(this.number, input.readUInt32(), repeated);
/*  221:     */               }
/*  222:     */             };
/*  223:     */           }
/*  224:     */           
/*  225:     */           public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  226:     */             throws IOException
/*  227:     */           {
/*  228: 307 */             output.writeUInt32(number, input.readUInt32(), repeated);
/*  229:     */           }
/*  230:     */           
/*  231:     */           public Byte readFrom(Input input)
/*  232:     */             throws IOException
/*  233:     */           {
/*  234: 313 */             return Byte.valueOf((byte)input.readUInt32());
/*  235:     */           }
/*  236:     */           
/*  237:     */           public void writeTo(Output output, int number, Byte value, boolean repeated)
/*  238:     */             throws IOException
/*  239:     */           {
/*  240: 320 */             output.writeUInt32(number, value.byteValue(), repeated);
/*  241:     */           }
/*  242:     */           
/*  243:     */           public WireFormat.FieldType getFieldType()
/*  244:     */           {
/*  245: 326 */             return WireFormat.FieldType.UINT32;
/*  246:     */           }
/*  247:     */           
/*  248:     */           public Class<?> typeClass()
/*  249:     */           {
/*  250: 332 */             return Byte.class;
/*  251:     */           }
/*  252:     */         };
/*  253: 336 */         public static final RuntimeFieldFactory<Integer> INT32 = new RuntimeFieldFactory(5)
/*  254:     */         {
/*  255:     */           public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  256:     */           {
/*  257: 343 */             final boolean primitive = f.getType().isPrimitive();
/*  258: 344 */             final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  259: 345 */             new Field(WireFormat.FieldType.INT32, number, name, 
/*  260: 346 */               (Tag)f.getAnnotation(Tag.class))
/*  261:     */               {
/*  262:     */                 public void mergeFrom(Input input, T message)
/*  263:     */                   throws IOException
/*  264:     */                 {
/*  265: 352 */                   if (primitive) {
/*  266: 353 */                     RuntimeUnsafeFieldFactory.us.putInt(message, offset, input.readInt32());
/*  267:     */                   } else {
/*  268: 355 */                     RuntimeUnsafeFieldFactory.us.putObject(message, offset, 
/*  269: 356 */                       Integer.valueOf(input.readInt32()));
/*  270:     */                   }
/*  271:     */                 }
/*  272:     */                 
/*  273:     */                 public void writeTo(Output output, T message)
/*  274:     */                   throws IOException
/*  275:     */                 {
/*  276: 363 */                   if (primitive)
/*  277:     */                   {
/*  278: 364 */                     output.writeInt32(this.number, RuntimeUnsafeFieldFactory.us.getInt(message, offset), false);
/*  279:     */                   }
/*  280:     */                   else
/*  281:     */                   {
/*  282: 368 */                     Integer value = (Integer)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  283: 369 */                     if (value != null) {
/*  284: 370 */                       output.writeInt32(this.number, value.intValue(), false);
/*  285:     */                     }
/*  286:     */                   }
/*  287:     */                 }
/*  288:     */                 
/*  289:     */                 public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  290:     */                   throws IOException
/*  291:     */                 {
/*  292: 378 */                   output.writeInt32(this.number, input.readInt32(), repeated);
/*  293:     */                 }
/*  294:     */               };
/*  295:     */             }
/*  296:     */             
/*  297:     */             public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  298:     */               throws IOException
/*  299:     */             {
/*  300: 387 */               output.writeInt32(number, input.readInt32(), repeated);
/*  301:     */             }
/*  302:     */             
/*  303:     */             public Integer readFrom(Input input)
/*  304:     */               throws IOException
/*  305:     */             {
/*  306: 393 */               return Integer.valueOf(input.readInt32());
/*  307:     */             }
/*  308:     */             
/*  309:     */             public void writeTo(Output output, int number, Integer value, boolean repeated)
/*  310:     */               throws IOException
/*  311:     */             {
/*  312: 400 */               output.writeInt32(number, value.intValue(), repeated);
/*  313:     */             }
/*  314:     */             
/*  315:     */             public WireFormat.FieldType getFieldType()
/*  316:     */             {
/*  317: 406 */               return WireFormat.FieldType.INT32;
/*  318:     */             }
/*  319:     */             
/*  320:     */             public Class<?> typeClass()
/*  321:     */             {
/*  322: 412 */               return Integer.class;
/*  323:     */             }
/*  324:     */           };
/*  325: 416 */           public static final RuntimeFieldFactory<Long> INT64 = new RuntimeFieldFactory(6)
/*  326:     */           {
/*  327:     */             public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  328:     */             {
/*  329: 423 */               final boolean primitive = f.getType().isPrimitive();
/*  330: 424 */               final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  331: 425 */               new Field(WireFormat.FieldType.INT64, number, name, 
/*  332: 426 */                 (Tag)f.getAnnotation(Tag.class))
/*  333:     */                 {
/*  334:     */                   public void mergeFrom(Input input, T message)
/*  335:     */                     throws IOException
/*  336:     */                   {
/*  337: 432 */                     if (primitive) {
/*  338: 433 */                       RuntimeUnsafeFieldFactory.us.putLong(message, offset, input.readInt64());
/*  339:     */                     } else {
/*  340: 435 */                       RuntimeUnsafeFieldFactory.us.putObject(message, offset, 
/*  341: 436 */                         Long.valueOf(input.readInt64()));
/*  342:     */                     }
/*  343:     */                   }
/*  344:     */                   
/*  345:     */                   public void writeTo(Output output, T message)
/*  346:     */                     throws IOException
/*  347:     */                   {
/*  348: 443 */                     if (primitive)
/*  349:     */                     {
/*  350: 444 */                       output.writeInt64(this.number, RuntimeUnsafeFieldFactory.us.getLong(message, offset), false);
/*  351:     */                     }
/*  352:     */                     else
/*  353:     */                     {
/*  354: 448 */                       Long value = (Long)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  355: 449 */                       if (value != null) {
/*  356: 450 */                         output.writeInt64(this.number, value.longValue(), false);
/*  357:     */                       }
/*  358:     */                     }
/*  359:     */                   }
/*  360:     */                   
/*  361:     */                   public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  362:     */                     throws IOException
/*  363:     */                   {
/*  364: 458 */                     output.writeInt64(this.number, input.readInt64(), repeated);
/*  365:     */                   }
/*  366:     */                 };
/*  367:     */               }
/*  368:     */               
/*  369:     */               public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  370:     */                 throws IOException
/*  371:     */               {
/*  372: 467 */                 output.writeInt64(number, input.readInt64(), repeated);
/*  373:     */               }
/*  374:     */               
/*  375:     */               public Long readFrom(Input input)
/*  376:     */                 throws IOException
/*  377:     */               {
/*  378: 473 */                 return Long.valueOf(input.readInt64());
/*  379:     */               }
/*  380:     */               
/*  381:     */               public void writeTo(Output output, int number, Long value, boolean repeated)
/*  382:     */                 throws IOException
/*  383:     */               {
/*  384: 480 */                 output.writeInt64(number, value.longValue(), repeated);
/*  385:     */               }
/*  386:     */               
/*  387:     */               public WireFormat.FieldType getFieldType()
/*  388:     */               {
/*  389: 486 */                 return WireFormat.FieldType.INT64;
/*  390:     */               }
/*  391:     */               
/*  392:     */               public Class<?> typeClass()
/*  393:     */               {
/*  394: 492 */                 return Long.class;
/*  395:     */               }
/*  396:     */             };
/*  397: 496 */             public static final RuntimeFieldFactory<Float> FLOAT = new RuntimeFieldFactory(7)
/*  398:     */             {
/*  399:     */               public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  400:     */               {
/*  401: 503 */                 final boolean primitive = f.getType().isPrimitive();
/*  402: 504 */                 final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  403: 505 */                 new Field(WireFormat.FieldType.FLOAT, number, name, 
/*  404: 506 */                   (Tag)f.getAnnotation(Tag.class))
/*  405:     */                   {
/*  406:     */                     public void mergeFrom(Input input, T message)
/*  407:     */                       throws IOException
/*  408:     */                     {
/*  409: 512 */                       if (primitive) {
/*  410: 513 */                         RuntimeUnsafeFieldFactory.us.putFloat(message, offset, input.readFloat());
/*  411:     */                       } else {
/*  412: 515 */                         RuntimeUnsafeFieldFactory.us.putObject(message, offset, new Float(input
/*  413: 516 */                           .readFloat()));
/*  414:     */                       }
/*  415:     */                     }
/*  416:     */                     
/*  417:     */                     public void writeTo(Output output, T message)
/*  418:     */                       throws IOException
/*  419:     */                     {
/*  420: 523 */                       if (primitive)
/*  421:     */                       {
/*  422: 524 */                         output.writeFloat(this.number, RuntimeUnsafeFieldFactory.us.getFloat(message, offset), false);
/*  423:     */                       }
/*  424:     */                       else
/*  425:     */                       {
/*  426: 528 */                         Float value = (Float)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  427: 529 */                         if (value != null) {
/*  428: 530 */                           output.writeFloat(this.number, value.floatValue(), false);
/*  429:     */                         }
/*  430:     */                       }
/*  431:     */                     }
/*  432:     */                     
/*  433:     */                     public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  434:     */                       throws IOException
/*  435:     */                     {
/*  436: 538 */                       output.writeFloat(this.number, input.readFloat(), repeated);
/*  437:     */                     }
/*  438:     */                   };
/*  439:     */                 }
/*  440:     */                 
/*  441:     */                 public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  442:     */                   throws IOException
/*  443:     */                 {
/*  444: 547 */                   output.writeFloat(number, input.readFloat(), repeated);
/*  445:     */                 }
/*  446:     */                 
/*  447:     */                 public Float readFrom(Input input)
/*  448:     */                   throws IOException
/*  449:     */                 {
/*  450: 553 */                   return new Float(input.readFloat());
/*  451:     */                 }
/*  452:     */                 
/*  453:     */                 public void writeTo(Output output, int number, Float value, boolean repeated)
/*  454:     */                   throws IOException
/*  455:     */                 {
/*  456: 560 */                   output.writeFloat(number, value.floatValue(), repeated);
/*  457:     */                 }
/*  458:     */                 
/*  459:     */                 public WireFormat.FieldType getFieldType()
/*  460:     */                 {
/*  461: 566 */                   return WireFormat.FieldType.FLOAT;
/*  462:     */                 }
/*  463:     */                 
/*  464:     */                 public Class<?> typeClass()
/*  465:     */                 {
/*  466: 572 */                   return Float.class;
/*  467:     */                 }
/*  468:     */               };
/*  469: 576 */               public static final RuntimeFieldFactory<Double> DOUBLE = new RuntimeFieldFactory(8)
/*  470:     */               {
/*  471:     */                 public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  472:     */                 {
/*  473: 583 */                   final boolean primitive = f.getType().isPrimitive();
/*  474: 584 */                   final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  475: 585 */                   new Field(WireFormat.FieldType.DOUBLE, number, name, 
/*  476: 586 */                     (Tag)f.getAnnotation(Tag.class))
/*  477:     */                     {
/*  478:     */                       public void mergeFrom(Input input, T message)
/*  479:     */                         throws IOException
/*  480:     */                       {
/*  481: 592 */                         if (primitive) {
/*  482: 593 */                           RuntimeUnsafeFieldFactory.us.putDouble(message, offset, input.readDouble());
/*  483:     */                         } else {
/*  484: 595 */                           RuntimeUnsafeFieldFactory.us.putObject(message, offset, new Double(input
/*  485: 596 */                             .readDouble()));
/*  486:     */                         }
/*  487:     */                       }
/*  488:     */                       
/*  489:     */                       public void writeTo(Output output, T message)
/*  490:     */                         throws IOException
/*  491:     */                       {
/*  492: 603 */                         if (primitive)
/*  493:     */                         {
/*  494: 604 */                           output.writeDouble(this.number, RuntimeUnsafeFieldFactory.us
/*  495: 605 */                             .getDouble(message, offset), false);
/*  496:     */                         }
/*  497:     */                         else
/*  498:     */                         {
/*  499: 608 */                           Double value = (Double)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  500: 609 */                           if (value != null) {
/*  501: 610 */                             output.writeDouble(this.number, value.doubleValue(), false);
/*  502:     */                           }
/*  503:     */                         }
/*  504:     */                       }
/*  505:     */                       
/*  506:     */                       public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  507:     */                         throws IOException
/*  508:     */                       {
/*  509: 619 */                         output.writeDouble(this.number, input.readDouble(), repeated);
/*  510:     */                       }
/*  511:     */                     };
/*  512:     */                   }
/*  513:     */                   
/*  514:     */                   public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  515:     */                     throws IOException
/*  516:     */                   {
/*  517: 628 */                     output.writeDouble(number, input.readDouble(), repeated);
/*  518:     */                   }
/*  519:     */                   
/*  520:     */                   public Double readFrom(Input input)
/*  521:     */                     throws IOException
/*  522:     */                   {
/*  523: 634 */                     return new Double(input.readDouble());
/*  524:     */                   }
/*  525:     */                   
/*  526:     */                   public void writeTo(Output output, int number, Double value, boolean repeated)
/*  527:     */                     throws IOException
/*  528:     */                   {
/*  529: 641 */                     output.writeDouble(number, value.doubleValue(), repeated);
/*  530:     */                   }
/*  531:     */                   
/*  532:     */                   public WireFormat.FieldType getFieldType()
/*  533:     */                   {
/*  534: 647 */                     return WireFormat.FieldType.DOUBLE;
/*  535:     */                   }
/*  536:     */                   
/*  537:     */                   public Class<?> typeClass()
/*  538:     */                   {
/*  539: 653 */                     return Double.class;
/*  540:     */                   }
/*  541:     */                 };
/*  542: 657 */                 public static final RuntimeFieldFactory<Boolean> BOOL = new RuntimeFieldFactory(1)
/*  543:     */                 {
/*  544:     */                   public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  545:     */                   {
/*  546: 664 */                     final boolean primitive = f.getType().isPrimitive();
/*  547: 665 */                     final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  548: 666 */                     new Field(WireFormat.FieldType.BOOL, number, name, 
/*  549: 667 */                       (Tag)f.getAnnotation(Tag.class))
/*  550:     */                       {
/*  551:     */                         public void mergeFrom(Input input, T message)
/*  552:     */                           throws IOException
/*  553:     */                         {
/*  554: 673 */                           if (primitive) {
/*  555: 674 */                             RuntimeUnsafeFieldFactory.us.putBoolean(message, offset, input.readBool());
/*  556:     */                           } else {
/*  557: 676 */                             RuntimeUnsafeFieldFactory.us.putObject(message, offset, input
/*  558: 677 */                               .readBool() ? Boolean.TRUE : Boolean.FALSE);
/*  559:     */                           }
/*  560:     */                         }
/*  561:     */                         
/*  562:     */                         public void writeTo(Output output, T message)
/*  563:     */                           throws IOException
/*  564:     */                         {
/*  565: 684 */                           if (primitive)
/*  566:     */                           {
/*  567: 685 */                             output.writeBool(this.number, RuntimeUnsafeFieldFactory.us
/*  568: 686 */                               .getBoolean(message, offset), false);
/*  569:     */                           }
/*  570:     */                           else
/*  571:     */                           {
/*  572: 689 */                             Boolean value = (Boolean)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  573: 690 */                             if (value != null) {
/*  574: 691 */                               output.writeBool(this.number, value.booleanValue(), false);
/*  575:     */                             }
/*  576:     */                           }
/*  577:     */                         }
/*  578:     */                         
/*  579:     */                         public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  580:     */                           throws IOException
/*  581:     */                         {
/*  582: 700 */                           output.writeBool(this.number, input.readBool(), repeated);
/*  583:     */                         }
/*  584:     */                       };
/*  585:     */                     }
/*  586:     */                     
/*  587:     */                     public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  588:     */                       throws IOException
/*  589:     */                     {
/*  590: 709 */                       output.writeBool(number, input.readBool(), repeated);
/*  591:     */                     }
/*  592:     */                     
/*  593:     */                     public Boolean readFrom(Input input)
/*  594:     */                       throws IOException
/*  595:     */                     {
/*  596: 715 */                       return input.readBool() ? Boolean.TRUE : Boolean.FALSE;
/*  597:     */                     }
/*  598:     */                     
/*  599:     */                     public void writeTo(Output output, int number, Boolean value, boolean repeated)
/*  600:     */                       throws IOException
/*  601:     */                     {
/*  602: 722 */                       output.writeBool(number, value.booleanValue(), repeated);
/*  603:     */                     }
/*  604:     */                     
/*  605:     */                     public WireFormat.FieldType getFieldType()
/*  606:     */                     {
/*  607: 728 */                       return WireFormat.FieldType.BOOL;
/*  608:     */                     }
/*  609:     */                     
/*  610:     */                     public Class<?> typeClass()
/*  611:     */                     {
/*  612: 734 */                       return Boolean.class;
/*  613:     */                     }
/*  614:     */                   };
/*  615: 738 */                   public static final RuntimeFieldFactory<String> STRING = new RuntimeFieldFactory(9)
/*  616:     */                   {
/*  617:     */                     public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  618:     */                     {
/*  619: 745 */                       final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  620: 746 */                       new Field(WireFormat.FieldType.STRING, number, name, 
/*  621: 747 */                         (Tag)f.getAnnotation(Tag.class))
/*  622:     */                         {
/*  623:     */                           public void mergeFrom(Input input, T message)
/*  624:     */                             throws IOException
/*  625:     */                           {
/*  626: 753 */                             RuntimeUnsafeFieldFactory.us.putObject(message, offset, input.readString());
/*  627:     */                           }
/*  628:     */                           
/*  629:     */                           public void writeTo(Output output, T message)
/*  630:     */                             throws IOException
/*  631:     */                           {
/*  632: 760 */                             CharSequence value = (CharSequence)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  633: 761 */                             if (value != null) {
/*  634: 762 */                               output.writeString(this.number, value, false);
/*  635:     */                             }
/*  636:     */                           }
/*  637:     */                           
/*  638:     */                           public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  639:     */                             throws IOException
/*  640:     */                           {
/*  641: 769 */                             input.transferByteRangeTo(output, true, this.number, repeated);
/*  642:     */                           }
/*  643:     */                         };
/*  644:     */                       }
/*  645:     */                       
/*  646:     */                       public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  647:     */                         throws IOException
/*  648:     */                       {
/*  649: 778 */                         input.transferByteRangeTo(output, true, number, repeated);
/*  650:     */                       }
/*  651:     */                       
/*  652:     */                       public String readFrom(Input input)
/*  653:     */                         throws IOException
/*  654:     */                       {
/*  655: 784 */                         return input.readString();
/*  656:     */                       }
/*  657:     */                       
/*  658:     */                       public void writeTo(Output output, int number, String value, boolean repeated)
/*  659:     */                         throws IOException
/*  660:     */                       {
/*  661: 791 */                         output.writeString(number, value, repeated);
/*  662:     */                       }
/*  663:     */                       
/*  664:     */                       public WireFormat.FieldType getFieldType()
/*  665:     */                       {
/*  666: 797 */                         return WireFormat.FieldType.STRING;
/*  667:     */                       }
/*  668:     */                       
/*  669:     */                       public Class<?> typeClass()
/*  670:     */                       {
/*  671: 803 */                         return String.class;
/*  672:     */                       }
/*  673:     */                     };
/*  674: 807 */                     public static final RuntimeFieldFactory<ByteString> BYTES = new RuntimeFieldFactory(10)
/*  675:     */                     {
/*  676:     */                       public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  677:     */                       {
/*  678: 814 */                         final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  679: 815 */                         new Field(WireFormat.FieldType.BYTES, number, name, 
/*  680: 816 */                           (Tag)f.getAnnotation(Tag.class))
/*  681:     */                           {
/*  682:     */                             public void mergeFrom(Input input, T message)
/*  683:     */                               throws IOException
/*  684:     */                             {
/*  685: 822 */                               RuntimeUnsafeFieldFactory.us.putObject(message, offset, input.readBytes());
/*  686:     */                             }
/*  687:     */                             
/*  688:     */                             public void writeTo(Output output, T message)
/*  689:     */                               throws IOException
/*  690:     */                             {
/*  691: 829 */                               ByteString bs = (ByteString)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  692: 830 */                               if (bs != null) {
/*  693: 831 */                                 output.writeBytes(this.number, bs, false);
/*  694:     */                               }
/*  695:     */                             }
/*  696:     */                             
/*  697:     */                             public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  698:     */                               throws IOException
/*  699:     */                             {
/*  700: 838 */                               input.transferByteRangeTo(output, false, this.number, repeated);
/*  701:     */                             }
/*  702:     */                           };
/*  703:     */                         }
/*  704:     */                         
/*  705:     */                         public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  706:     */                           throws IOException
/*  707:     */                         {
/*  708: 847 */                           input.transferByteRangeTo(output, false, number, repeated);
/*  709:     */                         }
/*  710:     */                         
/*  711:     */                         public ByteString readFrom(Input input)
/*  712:     */                           throws IOException
/*  713:     */                         {
/*  714: 853 */                           return input.readBytes();
/*  715:     */                         }
/*  716:     */                         
/*  717:     */                         public void writeTo(Output output, int number, ByteString value, boolean repeated)
/*  718:     */                           throws IOException
/*  719:     */                         {
/*  720: 860 */                           output.writeBytes(number, value, repeated);
/*  721:     */                         }
/*  722:     */                         
/*  723:     */                         public WireFormat.FieldType getFieldType()
/*  724:     */                         {
/*  725: 866 */                           return WireFormat.FieldType.BYTES;
/*  726:     */                         }
/*  727:     */                         
/*  728:     */                         public Class<?> typeClass()
/*  729:     */                         {
/*  730: 872 */                           return ByteString.class;
/*  731:     */                         }
/*  732:     */                       };
/*  733: 876 */                       public static final RuntimeFieldFactory<byte[]> BYTE_ARRAY = new RuntimeFieldFactory(11)
/*  734:     */                       {
/*  735:     */                         public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  736:     */                         {
/*  737: 883 */                           final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  738: 884 */                           new Field(WireFormat.FieldType.BYTES, number, name, 
/*  739: 885 */                             (Tag)f.getAnnotation(Tag.class))
/*  740:     */                             {
/*  741:     */                               public void mergeFrom(Input input, T message)
/*  742:     */                                 throws IOException
/*  743:     */                               {
/*  744: 891 */                                 RuntimeUnsafeFieldFactory.us.putObject(message, offset, input.readByteArray());
/*  745:     */                               }
/*  746:     */                               
/*  747:     */                               public void writeTo(Output output, T message)
/*  748:     */                                 throws IOException
/*  749:     */                               {
/*  750: 898 */                                 byte[] array = (byte[])RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  751: 899 */                                 if (array != null) {
/*  752: 900 */                                   output.writeByteArray(this.number, array, false);
/*  753:     */                                 }
/*  754:     */                               }
/*  755:     */                               
/*  756:     */                               public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  757:     */                                 throws IOException
/*  758:     */                               {
/*  759: 907 */                                 input.transferByteRangeTo(output, false, this.number, repeated);
/*  760:     */                               }
/*  761:     */                             };
/*  762:     */                           }
/*  763:     */                           
/*  764:     */                           public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  765:     */                             throws IOException
/*  766:     */                           {
/*  767: 916 */                             input.transferByteRangeTo(output, false, number, repeated);
/*  768:     */                           }
/*  769:     */                           
/*  770:     */                           public byte[] readFrom(Input input)
/*  771:     */                             throws IOException
/*  772:     */                           {
/*  773: 922 */                             return input.readByteArray();
/*  774:     */                           }
/*  775:     */                           
/*  776:     */                           public void writeTo(Output output, int number, byte[] value, boolean repeated)
/*  777:     */                             throws IOException
/*  778:     */                           {
/*  779: 929 */                             output.writeByteArray(number, value, repeated);
/*  780:     */                           }
/*  781:     */                           
/*  782:     */                           public WireFormat.FieldType getFieldType()
/*  783:     */                           {
/*  784: 935 */                             return WireFormat.FieldType.BYTES;
/*  785:     */                           }
/*  786:     */                           
/*  787:     */                           public Class<?> typeClass()
/*  788:     */                           {
/*  789: 941 */                             return [B.class;
/*  790:     */                           }
/*  791:     */                         };
/*  792: 945 */                         public static final RuntimeFieldFactory<Integer> ENUM = new RuntimeFieldFactory(24)
/*  793:     */                         {
/*  794:     */                           public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, final IdStrategy strategy)
/*  795:     */                           {
/*  796: 953 */                             EnumIO<? extends Enum<?>> eio = strategy.getEnumIO(f
/*  797: 954 */                               .getType());
/*  798: 955 */                             final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  799: 956 */                             new Field(WireFormat.FieldType.ENUM, number, name, 
/*  800: 957 */                               (Tag)f.getAnnotation(Tag.class))
/*  801:     */                               {
/*  802:     */                                 public void mergeFrom(Input input, T message)
/*  803:     */                                   throws IOException
/*  804:     */                                 {
/*  805: 963 */                                   RuntimeUnsafeFieldFactory.us.putObject(message, offset, strategy.readFrom(input));
/*  806:     */                                 }
/*  807:     */                                 
/*  808:     */                                 public void writeTo(Output output, T message)
/*  809:     */                                   throws IOException
/*  810:     */                                 {
/*  811: 970 */                                   Enum<?> existing = (Enum)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  812: 972 */                                   if (existing != null) {
/*  813: 973 */                                     strategy.writeTo(output, this.number, this.repeated, existing);
/*  814:     */                                   }
/*  815:     */                                 }
/*  816:     */                                 
/*  817:     */                                 public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  818:     */                                   throws IOException
/*  819:     */                                 {
/*  820: 980 */                                   EnumIO.transfer(pipe, input, output, this.number, repeated, this.val$strategy);
/*  821:     */                                 }
/*  822:     */                               };
/*  823:     */                             }
/*  824:     */                             
/*  825:     */                             public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  826:     */                               throws IOException
/*  827:     */                             {
/*  828: 989 */                               throw new UnsupportedOperationException();
/*  829:     */                             }
/*  830:     */                             
/*  831:     */                             public Integer readFrom(Input input)
/*  832:     */                               throws IOException
/*  833:     */                             {
/*  834: 995 */                               throw new UnsupportedOperationException();
/*  835:     */                             }
/*  836:     */                             
/*  837:     */                             public void writeTo(Output output, int number, Integer value, boolean repeated)
/*  838:     */                               throws IOException
/*  839:     */                             {
/*  840:1002 */                               throw new UnsupportedOperationException();
/*  841:     */                             }
/*  842:     */                             
/*  843:     */                             public WireFormat.FieldType getFieldType()
/*  844:     */                             {
/*  845:1008 */                               throw new UnsupportedOperationException();
/*  846:     */                             }
/*  847:     */                             
/*  848:     */                             public Class<?> typeClass()
/*  849:     */                             {
/*  850:1014 */                               throw new UnsupportedOperationException();
/*  851:     */                             }
/*  852:     */                           };
/*  853:1020 */                           static final RuntimeFieldFactory<Object> POJO = new RuntimeFieldFactory(127)
/*  854:     */                           {
/*  855:     */                             public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  856:     */                             {
/*  857:1028 */                               Class<Object> type = f.getType();
/*  858:1029 */                               final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  859:1030 */                               new RuntimeMessageField(type, strategy
/*  860:1031 */                                 .getSchemaWrapper(type, true), WireFormat.FieldType.MESSAGE, number, name, false, 
/*  861:1032 */                                 (Tag)f.getAnnotation(Tag.class))
/*  862:     */                                 {
/*  863:     */                                   public void mergeFrom(Input input, T message)
/*  864:     */                                     throws IOException
/*  865:     */                                   {
/*  866:1038 */                                     RuntimeUnsafeFieldFactory.us.putObject(message, offset, input.mergeObject(RuntimeUnsafeFieldFactory.us
/*  867:1039 */                                       .getObject(message, offset), getSchema()));
/*  868:     */                                   }
/*  869:     */                                   
/*  870:     */                                   public void writeTo(Output output, T message)
/*  871:     */                                     throws IOException
/*  872:     */                                   {
/*  873:1046 */                                     Object existing = RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  874:1047 */                                     if (existing != null) {
/*  875:1048 */                                       output.writeObject(this.number, existing, getSchema(), false);
/*  876:     */                                     }
/*  877:     */                                   }
/*  878:     */                                   
/*  879:     */                                   public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  880:     */                                     throws IOException
/*  881:     */                                   {
/*  882:1055 */                                     output.writeObject(this.number, pipe, getPipeSchema(), repeated);
/*  883:     */                                   }
/*  884:     */                                 };
/*  885:     */                               }
/*  886:     */                               
/*  887:     */                               public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  888:     */                                 throws IOException
/*  889:     */                               {
/*  890:1064 */                                 throw new UnsupportedOperationException();
/*  891:     */                               }
/*  892:     */                               
/*  893:     */                               public Object readFrom(Input input)
/*  894:     */                                 throws IOException
/*  895:     */                               {
/*  896:1070 */                                 throw new UnsupportedOperationException();
/*  897:     */                               }
/*  898:     */                               
/*  899:     */                               public void writeTo(Output output, int number, Object value, boolean repeated)
/*  900:     */                                 throws IOException
/*  901:     */                               {
/*  902:1077 */                                 throw new UnsupportedOperationException();
/*  903:     */                               }
/*  904:     */                               
/*  905:     */                               public WireFormat.FieldType getFieldType()
/*  906:     */                               {
/*  907:1083 */                                 throw new UnsupportedOperationException();
/*  908:     */                               }
/*  909:     */                               
/*  910:     */                               public Class<?> typeClass()
/*  911:     */                               {
/*  912:1089 */                                 throw new UnsupportedOperationException();
/*  913:     */                               }
/*  914:     */                             };
/*  915:1093 */                             static final RuntimeFieldFactory<Object> POLYMORPHIC_POJO = new RuntimeFieldFactory(0)
/*  916:     */                             {
/*  917:     */                               public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/*  918:     */                               {
/*  919:1101 */                                 if (pojo(f.getType(), (Morph)f.getAnnotation(Morph.class), strategy)) {
/*  920:1102 */                                   return POJO.create(number, name, f, strategy);
/*  921:     */                                 }
/*  922:1104 */                                 final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/*  923:1105 */                                 new RuntimeDerivativeField(f.getType(), WireFormat.FieldType.MESSAGE, number, name, false, 
/*  924:     */                                 
/*  925:1107 */                                   (Tag)f.getAnnotation(Tag.class), strategy)
/*  926:     */                                   {
/*  927:     */                                     public void mergeFrom(Input input, T message)
/*  928:     */                                       throws IOException
/*  929:     */                                     {
/*  930:1113 */                                       Object value = input.mergeObject(message, this.schema);
/*  931:1114 */                                       if (((input instanceof GraphInput)) && 
/*  932:1115 */                                         (((GraphInput)input).isCurrentMessageReference())) {
/*  933:1118 */                                         RuntimeUnsafeFieldFactory.us.putObject(message, offset, value);
/*  934:     */                                       }
/*  935:     */                                     }
/*  936:     */                                     
/*  937:     */                                     public void writeTo(Output output, T message)
/*  938:     */                                       throws IOException
/*  939:     */                                     {
/*  940:1126 */                                       Object existing = RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  941:1127 */                                       if (existing != null) {
/*  942:1128 */                                         output.writeObject(this.number, existing, this.schema, false);
/*  943:     */                                       }
/*  944:     */                                     }
/*  945:     */                                     
/*  946:     */                                     public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  947:     */                                       throws IOException
/*  948:     */                                     {
/*  949:1135 */                                       output.writeObject(this.number, pipe, this.schema.pipeSchema, false);
/*  950:     */                                     }
/*  951:     */                                     
/*  952:     */                                     public void doMergeFrom(Input input, Schema<Object> schema, Object message)
/*  953:     */                                       throws IOException
/*  954:     */                                     {
/*  955:1142 */                                       Object existing = RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/*  956:     */                                       
/*  957:     */ 
/*  958:     */ 
/*  959:     */ 
/*  960:1147 */                                       Object value = (existing == null) || (existing.getClass() != schema.typeClass()) ? schema.newMessage() : existing;
/*  961:1149 */                                       if ((input instanceof GraphInput)) {
/*  962:1152 */                                         ((GraphInput)input).updateLast(value, message);
/*  963:     */                                       }
/*  964:1155 */                                       schema.mergeFrom(input, value);
/*  965:     */                                       
/*  966:1157 */                                       RuntimeUnsafeFieldFactory.us.putObject(message, offset, value);
/*  967:     */                                     }
/*  968:     */                                   };
/*  969:     */                                 }
/*  970:     */                                 
/*  971:     */                                 public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  972:     */                                   throws IOException
/*  973:     */                                 {
/*  974:1166 */                                   throw new UnsupportedOperationException();
/*  975:     */                                 }
/*  976:     */                                 
/*  977:     */                                 public Object readFrom(Input input)
/*  978:     */                                   throws IOException
/*  979:     */                                 {
/*  980:1172 */                                   throw new UnsupportedOperationException();
/*  981:     */                                 }
/*  982:     */                                 
/*  983:     */                                 public void writeTo(Output output, int number, Object value, boolean repeated)
/*  984:     */                                   throws IOException
/*  985:     */                                 {
/*  986:1179 */                                   throw new UnsupportedOperationException();
/*  987:     */                                 }
/*  988:     */                                 
/*  989:     */                                 public WireFormat.FieldType getFieldType()
/*  990:     */                                 {
/*  991:1185 */                                   throw new UnsupportedOperationException();
/*  992:     */                                 }
/*  993:     */                                 
/*  994:     */                                 public Class<?> typeClass()
/*  995:     */                                 {
/*  996:1191 */                                   throw new UnsupportedOperationException();
/*  997:     */                                 }
/*  998:     */                               };
/*  999:1195 */                               static final RuntimeFieldFactory<Object> OBJECT = new RuntimeFieldFactory(16)
/* 1000:     */                               {
/* 1001:     */                                 public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 1002:     */                                 {
/* 1003:1202 */                                   final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/* 1004:1203 */                                   new RuntimeObjectField(f
/* 1005:1204 */                                     .getType(), WireFormat.FieldType.MESSAGE, number, name, false, 
/* 1006:     */                                     
/* 1007:     */ 
/* 1008:     */ 
/* 1009:     */ 
/* 1010:1209 */                                     (Tag)f.getAnnotation(Tag.class), 
/* 1011:1210 */                                     PolymorphicSchemaFactories.getFactoryFromField(f, strategy), strategy)
/* 1012:     */                                     {
/* 1013:     */                                       public void mergeFrom(Input input, T message)
/* 1014:     */                                         throws IOException
/* 1015:     */                                       {
/* 1016:1217 */                                         Object value = input.mergeObject(message, this.schema);
/* 1017:1218 */                                         if (((input instanceof GraphInput)) && 
/* 1018:1219 */                                           (((GraphInput)input).isCurrentMessageReference())) {
/* 1019:1222 */                                           RuntimeUnsafeFieldFactory.us.putObject(message, offset, value);
/* 1020:     */                                         }
/* 1021:     */                                       }
/* 1022:     */                                       
/* 1023:     */                                       public void writeTo(Output output, T message)
/* 1024:     */                                         throws IOException
/* 1025:     */                                       {
/* 1026:1230 */                                         Object existing = RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/* 1027:1231 */                                         if (existing != null) {
/* 1028:1232 */                                           output.writeObject(this.number, existing, this.schema, false);
/* 1029:     */                                         }
/* 1030:     */                                       }
/* 1031:     */                                       
/* 1032:     */                                       public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1033:     */                                         throws IOException
/* 1034:     */                                       {
/* 1035:1239 */                                         output.writeObject(this.number, pipe, this.schema.getPipeSchema(), false);
/* 1036:     */                                       }
/* 1037:     */                                       
/* 1038:     */                                       public void setValue(Object value, Object message)
/* 1039:     */                                       {
/* 1040:1246 */                                         RuntimeUnsafeFieldFactory.us.putObject(message, offset, value);
/* 1041:     */                                       }
/* 1042:     */                                     };
/* 1043:     */                                   }
/* 1044:     */                                   
/* 1045:     */                                   public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1046:     */                                     throws IOException
/* 1047:     */                                   {
/* 1048:1255 */                                     throw new UnsupportedOperationException();
/* 1049:     */                                   }
/* 1050:     */                                   
/* 1051:     */                                   public Object readFrom(Input input)
/* 1052:     */                                     throws IOException
/* 1053:     */                                   {
/* 1054:1261 */                                     throw new UnsupportedOperationException();
/* 1055:     */                                   }
/* 1056:     */                                   
/* 1057:     */                                   public void writeTo(Output output, int number, Object value, boolean repeated)
/* 1058:     */                                     throws IOException
/* 1059:     */                                   {
/* 1060:1268 */                                     throw new UnsupportedOperationException();
/* 1061:     */                                   }
/* 1062:     */                                   
/* 1063:     */                                   public WireFormat.FieldType getFieldType()
/* 1064:     */                                   {
/* 1065:1274 */                                     return WireFormat.FieldType.MESSAGE;
/* 1066:     */                                   }
/* 1067:     */                                   
/* 1068:     */                                   public Class<?> typeClass()
/* 1069:     */                                   {
/* 1070:1280 */                                     return Object.class;
/* 1071:     */                                   }
/* 1072:     */                                 };
/* 1073:1284 */                                 public static final RuntimeFieldFactory<BigDecimal> BIGDECIMAL = new RuntimeFieldFactory(12)
/* 1074:     */                                 {
/* 1075:     */                                   public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 1076:     */                                   {
/* 1077:1291 */                                     final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/* 1078:1292 */                                     new Field(WireFormat.FieldType.STRING, number, name, 
/* 1079:1293 */                                       (Tag)f.getAnnotation(Tag.class))
/* 1080:     */                                       {
/* 1081:     */                                         public void mergeFrom(Input input, T message)
/* 1082:     */                                           throws IOException
/* 1083:     */                                         {
/* 1084:1299 */                                           RuntimeUnsafeFieldFactory.us.putObject(message, offset, new BigDecimal(input
/* 1085:1300 */                                             .readString()));
/* 1086:     */                                         }
/* 1087:     */                                         
/* 1088:     */                                         public void writeTo(Output output, T message)
/* 1089:     */                                           throws IOException
/* 1090:     */                                         {
/* 1091:1307 */                                           BigDecimal value = (BigDecimal)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/* 1092:1309 */                                           if (value != null) {
/* 1093:1310 */                                             output.writeString(this.number, value.toString(), false);
/* 1094:     */                                           }
/* 1095:     */                                         }
/* 1096:     */                                         
/* 1097:     */                                         public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1098:     */                                           throws IOException
/* 1099:     */                                         {
/* 1100:1317 */                                           input.transferByteRangeTo(output, true, this.number, repeated);
/* 1101:     */                                         }
/* 1102:     */                                       };
/* 1103:     */                                     }
/* 1104:     */                                     
/* 1105:     */                                     public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1106:     */                                       throws IOException
/* 1107:     */                                     {
/* 1108:1326 */                                       input.transferByteRangeTo(output, true, number, repeated);
/* 1109:     */                                     }
/* 1110:     */                                     
/* 1111:     */                                     public BigDecimal readFrom(Input input)
/* 1112:     */                                       throws IOException
/* 1113:     */                                     {
/* 1114:1332 */                                       return new BigDecimal(input.readString());
/* 1115:     */                                     }
/* 1116:     */                                     
/* 1117:     */                                     public void writeTo(Output output, int number, BigDecimal value, boolean repeated)
/* 1118:     */                                       throws IOException
/* 1119:     */                                     {
/* 1120:1339 */                                       output.writeString(number, value.toString(), repeated);
/* 1121:     */                                     }
/* 1122:     */                                     
/* 1123:     */                                     public WireFormat.FieldType getFieldType()
/* 1124:     */                                     {
/* 1125:1345 */                                       return WireFormat.FieldType.STRING;
/* 1126:     */                                     }
/* 1127:     */                                     
/* 1128:     */                                     public Class<?> typeClass()
/* 1129:     */                                     {
/* 1130:1351 */                                       return BigDecimal.class;
/* 1131:     */                                     }
/* 1132:     */                                   };
/* 1133:1355 */                                   public static final RuntimeFieldFactory<BigInteger> BIGINTEGER = new RuntimeFieldFactory(13)
/* 1134:     */                                   {
/* 1135:     */                                     public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 1136:     */                                     {
/* 1137:1362 */                                       final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/* 1138:1363 */                                       new Field(WireFormat.FieldType.BYTES, number, name, 
/* 1139:1364 */                                         (Tag)f.getAnnotation(Tag.class))
/* 1140:     */                                         {
/* 1141:     */                                           public void mergeFrom(Input input, T message)
/* 1142:     */                                             throws IOException
/* 1143:     */                                           {
/* 1144:1370 */                                             RuntimeUnsafeFieldFactory.us.putObject(message, offset, new BigInteger(input
/* 1145:1371 */                                               .readByteArray()));
/* 1146:     */                                           }
/* 1147:     */                                           
/* 1148:     */                                           public void writeTo(Output output, T message)
/* 1149:     */                                             throws IOException
/* 1150:     */                                           {
/* 1151:1378 */                                             BigInteger value = (BigInteger)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/* 1152:1380 */                                             if (value != null) {
/* 1153:1381 */                                               output.writeByteArray(this.number, value.toByteArray(), false);
/* 1154:     */                                             }
/* 1155:     */                                           }
/* 1156:     */                                           
/* 1157:     */                                           public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1158:     */                                             throws IOException
/* 1159:     */                                           {
/* 1160:1389 */                                             input.transferByteRangeTo(output, false, this.number, repeated);
/* 1161:     */                                           }
/* 1162:     */                                         };
/* 1163:     */                                       }
/* 1164:     */                                       
/* 1165:     */                                       public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1166:     */                                         throws IOException
/* 1167:     */                                       {
/* 1168:1398 */                                         input.transferByteRangeTo(output, false, number, repeated);
/* 1169:     */                                       }
/* 1170:     */                                       
/* 1171:     */                                       public BigInteger readFrom(Input input)
/* 1172:     */                                         throws IOException
/* 1173:     */                                       {
/* 1174:1404 */                                         return new BigInteger(input.readByteArray());
/* 1175:     */                                       }
/* 1176:     */                                       
/* 1177:     */                                       public void writeTo(Output output, int number, BigInteger value, boolean repeated)
/* 1178:     */                                         throws IOException
/* 1179:     */                                       {
/* 1180:1411 */                                         output.writeByteArray(number, value.toByteArray(), repeated);
/* 1181:     */                                       }
/* 1182:     */                                       
/* 1183:     */                                       public WireFormat.FieldType getFieldType()
/* 1184:     */                                       {
/* 1185:1417 */                                         return WireFormat.FieldType.BYTES;
/* 1186:     */                                       }
/* 1187:     */                                       
/* 1188:     */                                       public Class<?> typeClass()
/* 1189:     */                                       {
/* 1190:1423 */                                         return BigInteger.class;
/* 1191:     */                                       }
/* 1192:     */                                     };
/* 1193:1427 */                                     public static final RuntimeFieldFactory<Date> DATE = new RuntimeFieldFactory(14)
/* 1194:     */                                     {
/* 1195:     */                                       public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 1196:     */                                       {
/* 1197:1434 */                                         final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/* 1198:1435 */                                         new Field(WireFormat.FieldType.FIXED64, number, name, 
/* 1199:1436 */                                           (Tag)f.getAnnotation(Tag.class))
/* 1200:     */                                           {
/* 1201:     */                                             public void mergeFrom(Input input, T message)
/* 1202:     */                                               throws IOException
/* 1203:     */                                             {
/* 1204:1442 */                                               RuntimeUnsafeFieldFactory.us.putObject(message, offset, new Date(input.readFixed64()));
/* 1205:     */                                             }
/* 1206:     */                                             
/* 1207:     */                                             public void writeTo(Output output, T message)
/* 1208:     */                                               throws IOException
/* 1209:     */                                             {
/* 1210:1449 */                                               Date value = (Date)RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/* 1211:1450 */                                               if (value != null) {
/* 1212:1451 */                                                 output.writeFixed64(this.number, value.getTime(), false);
/* 1213:     */                                               }
/* 1214:     */                                             }
/* 1215:     */                                             
/* 1216:     */                                             public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1217:     */                                               throws IOException
/* 1218:     */                                             {
/* 1219:1458 */                                               output.writeFixed64(this.number, input.readFixed64(), repeated);
/* 1220:     */                                             }
/* 1221:     */                                           };
/* 1222:     */                                         }
/* 1223:     */                                         
/* 1224:     */                                         public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1225:     */                                           throws IOException
/* 1226:     */                                         {
/* 1227:1467 */                                           output.writeFixed64(number, input.readFixed64(), repeated);
/* 1228:     */                                         }
/* 1229:     */                                         
/* 1230:     */                                         public Date readFrom(Input input)
/* 1231:     */                                           throws IOException
/* 1232:     */                                         {
/* 1233:1473 */                                           return new Date(input.readFixed64());
/* 1234:     */                                         }
/* 1235:     */                                         
/* 1236:     */                                         public void writeTo(Output output, int number, Date value, boolean repeated)
/* 1237:     */                                           throws IOException
/* 1238:     */                                         {
/* 1239:1480 */                                           output.writeFixed64(number, value.getTime(), repeated);
/* 1240:     */                                         }
/* 1241:     */                                         
/* 1242:     */                                         public WireFormat.FieldType getFieldType()
/* 1243:     */                                         {
/* 1244:1486 */                                           return WireFormat.FieldType.FIXED64;
/* 1245:     */                                         }
/* 1246:     */                                         
/* 1247:     */                                         public Class<?> typeClass()
/* 1248:     */                                         {
/* 1249:1492 */                                           return Date.class;
/* 1250:     */                                         }
/* 1251:     */                                       };
/* 1252:1496 */                                       public static final RuntimeFieldFactory<Object> DELEGATE = new RuntimeFieldFactory(30)
/* 1253:     */                                       {
/* 1254:     */                                         public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 1255:     */                                         {
/* 1256:1505 */                                           Delegate<Object> delegate = strategy.getDelegate(f.getType());
/* 1257:     */                                           
/* 1258:1507 */                                           final long offset = RuntimeUnsafeFieldFactory.us.objectFieldOffset(f);
/* 1259:1508 */                                           new Field(WireFormat.FieldType.BYTES, number, name, 
/* 1260:1509 */                                             (Tag)f.getAnnotation(Tag.class))
/* 1261:     */                                             {
/* 1262:     */                                               public void mergeFrom(Input input, T message)
/* 1263:     */                                                 throws IOException
/* 1264:     */                                               {
/* 1265:1515 */                                                 RuntimeUnsafeFieldFactory.us.putObject(message, offset, this.val$delegate.readFrom(input));
/* 1266:     */                                               }
/* 1267:     */                                               
/* 1268:     */                                               public void writeTo(Output output, T message)
/* 1269:     */                                                 throws IOException
/* 1270:     */                                               {
/* 1271:1522 */                                                 Object value = RuntimeUnsafeFieldFactory.us.getObject(message, offset);
/* 1272:1523 */                                                 if (value != null) {
/* 1273:1524 */                                                   this.val$delegate.writeTo(output, this.number, value, false);
/* 1274:     */                                                 }
/* 1275:     */                                               }
/* 1276:     */                                               
/* 1277:     */                                               public void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 1278:     */                                                 throws IOException
/* 1279:     */                                               {
/* 1280:1531 */                                                 this.val$delegate.transfer(pipe, input, output, this.number, repeated);
/* 1281:     */                                               }
/* 1282:     */                                             };
/* 1283:     */                                           }
/* 1284:     */                                           
/* 1285:     */                                           public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 1286:     */                                             throws IOException
/* 1287:     */                                           {
/* 1288:1540 */                                             throw new UnsupportedOperationException();
/* 1289:     */                                           }
/* 1290:     */                                           
/* 1291:     */                                           public Object readFrom(Input input)
/* 1292:     */                                             throws IOException
/* 1293:     */                                           {
/* 1294:1546 */                                             throw new UnsupportedOperationException();
/* 1295:     */                                           }
/* 1296:     */                                           
/* 1297:     */                                           public void writeTo(Output output, int number, Object value, boolean repeated)
/* 1298:     */                                             throws IOException
/* 1299:     */                                           {
/* 1300:1553 */                                             throw new UnsupportedOperationException();
/* 1301:     */                                           }
/* 1302:     */                                           
/* 1303:     */                                           public WireFormat.FieldType getFieldType()
/* 1304:     */                                           {
/* 1305:1559 */                                             throw new UnsupportedOperationException();
/* 1306:     */                                           }
/* 1307:     */                                           
/* 1308:     */                                           public Class<?> typeClass()
/* 1309:     */                                           {
/* 1310:1565 */                                             throw new UnsupportedOperationException();
/* 1311:     */                                           }
/* 1312:     */                                         };
/* 1313:     */                                       }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeUnsafeFieldFactory
 * JD-Core Version:    0.7.0.1
 */