/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.CollectionSchema;
/*   4:    */ import io.protostuff.CollectionSchema.MessageFactory;
/*   5:    */ import io.protostuff.GraphInput;
/*   6:    */ import io.protostuff.Input;
/*   7:    */ import io.protostuff.Message;
/*   8:    */ import io.protostuff.Morph;
/*   9:    */ import io.protostuff.Output;
/*  10:    */ import io.protostuff.Pipe;
/*  11:    */ import io.protostuff.Pipe.Schema;
/*  12:    */ import io.protostuff.Schema;
/*  13:    */ import io.protostuff.Tag;
/*  14:    */ import io.protostuff.WireFormat.FieldType;
/*  15:    */ import java.io.IOException;
/*  16:    */ import java.lang.reflect.Modifier;
/*  17:    */ import java.util.Collection;
/*  18:    */ import java.util.EnumSet;
/*  19:    */ import java.util.Map;
/*  20:    */ 
/*  21:    */ final class RuntimeCollectionFieldFactory
/*  22:    */ {
/*  23:    */   static RuntimeFieldFactory<Collection<?>> getFactory()
/*  24:    */   {
/*  25: 52 */     return COLLECTION;
/*  26:    */   }
/*  27:    */   
/*  28: 55 */   static final Accessor.Factory AF = RuntimeFieldFactory.ACCESSOR_FACTORY;
/*  29:    */   
/*  30:    */   private static <T> Field<T> createCollectionInlineV(int number, String name, java.lang.reflect.Field f, CollectionSchema.MessageFactory messageFactory, final Delegate<Object> inline)
/*  31:    */   {
/*  32: 81 */     final Accessor accessor = AF.create(f);
/*  33: 82 */     new RuntimeCollectionField(inline.getFieldType(), number, name, 
/*  34: 83 */       (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  35:    */       {
/*  36:    */         protected void mergeFrom(Input input, T message)
/*  37:    */           throws IOException
/*  38:    */         {
/*  39: 88 */           accessor.set(message, input.mergeObject(accessor
/*  40: 89 */             .get(message), this.schema));
/*  41:    */         }
/*  42:    */         
/*  43:    */         protected void writeTo(Output output, T message)
/*  44:    */           throws IOException
/*  45:    */         {
/*  46: 95 */           Collection<Object> existing = (Collection)accessor.get(message);
/*  47: 96 */           if (existing != null) {
/*  48: 97 */             output.writeObject(this.number, existing, this.schema, false);
/*  49:    */           }
/*  50:    */         }
/*  51:    */         
/*  52:    */         protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  53:    */           throws IOException
/*  54:    */         {
/*  55:104 */           output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/*  56:    */         }
/*  57:    */         
/*  58:    */         protected void addValueFrom(Input input, Collection<Object> collection)
/*  59:    */           throws IOException
/*  60:    */         {
/*  61:111 */           collection.add(inline.readFrom(input));
/*  62:    */         }
/*  63:    */         
/*  64:    */         protected void writeValueTo(Output output, int fieldNumber, Object value, boolean repeated)
/*  65:    */           throws IOException
/*  66:    */         {
/*  67:118 */           inline.writeTo(output, fieldNumber, value, repeated);
/*  68:    */         }
/*  69:    */         
/*  70:    */         protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/*  71:    */           throws IOException
/*  72:    */         {
/*  73:125 */           inline.transfer(pipe, input, output, number, repeated);
/*  74:    */         }
/*  75:    */       };
/*  76:    */     }
/*  77:    */     
/*  78:    */     private static <T> Field<T> createCollectionEnumV(int number, String name, java.lang.reflect.Field f, CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, final IdStrategy strategy)
/*  79:    */     {
/*  80:134 */       final EnumIO<?> eio = strategy.getEnumIO(genericType);
/*  81:135 */       final Accessor accessor = AF.create(f);
/*  82:136 */       new RuntimeCollectionField(WireFormat.FieldType.ENUM, number, name, 
/*  83:137 */         (Tag)f.getAnnotation(Tag.class), messageFactory)
/*  84:    */         {
/*  85:    */           protected void mergeFrom(Input input, T message)
/*  86:    */             throws IOException
/*  87:    */           {
/*  88:142 */             accessor.set(message, input.mergeObject(accessor
/*  89:143 */               .get(message), this.schema));
/*  90:    */           }
/*  91:    */           
/*  92:    */           protected void writeTo(Output output, T message)
/*  93:    */             throws IOException
/*  94:    */           {
/*  95:149 */             Collection<Enum<?>> existing = (Collection)accessor.get(message);
/*  96:150 */             if (existing != null) {
/*  97:151 */               output.writeObject(this.number, existing, this.schema, false);
/*  98:    */             }
/*  99:    */           }
/* 100:    */           
/* 101:    */           protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 102:    */             throws IOException
/* 103:    */           {
/* 104:158 */             output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 105:    */           }
/* 106:    */           
/* 107:    */           protected void addValueFrom(Input input, Collection<Enum<?>> collection)
/* 108:    */             throws IOException
/* 109:    */           {
/* 110:165 */             collection.add(eio.readFrom(input));
/* 111:    */           }
/* 112:    */           
/* 113:    */           protected void writeValueTo(Output output, int fieldNumber, Enum<?> value, boolean repeated)
/* 114:    */             throws IOException
/* 115:    */           {
/* 116:172 */             eio.writeTo(output, fieldNumber, repeated, value);
/* 117:    */           }
/* 118:    */           
/* 119:    */           protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 120:    */             throws IOException
/* 121:    */           {
/* 122:179 */             EnumIO.transfer(pipe, input, output, number, repeated, strategy);
/* 123:    */           }
/* 124:    */         };
/* 125:    */       }
/* 126:    */       
/* 127:    */       private static <T> Field<T> createCollectionPojoV(int number, String name, java.lang.reflect.Field f, CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, IdStrategy strategy)
/* 128:    */       {
/* 129:188 */         final HasSchema<Object> schemaV = strategy.getSchemaWrapper(genericType, true);
/* 130:    */         
/* 131:190 */         final Accessor accessor = AF.create(f);
/* 132:191 */         new RuntimeCollectionField(WireFormat.FieldType.MESSAGE, number, name, 
/* 133:192 */           (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 134:    */           {
/* 135:    */             protected void mergeFrom(Input input, T message)
/* 136:    */               throws IOException
/* 137:    */             {
/* 138:197 */               accessor.set(message, input.mergeObject(accessor
/* 139:198 */                 .get(message), this.schema));
/* 140:    */             }
/* 141:    */             
/* 142:    */             protected void writeTo(Output output, T message)
/* 143:    */               throws IOException
/* 144:    */             {
/* 145:204 */               Collection<Object> existing = (Collection)accessor.get(message);
/* 146:205 */               if (existing != null) {
/* 147:206 */                 output.writeObject(this.number, existing, this.schema, false);
/* 148:    */               }
/* 149:    */             }
/* 150:    */             
/* 151:    */             protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 152:    */               throws IOException
/* 153:    */             {
/* 154:213 */               output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 155:    */             }
/* 156:    */             
/* 157:    */             protected void addValueFrom(Input input, Collection<Object> collection)
/* 158:    */               throws IOException
/* 159:    */             {
/* 160:220 */               collection.add(input.mergeObject(null, schemaV.getSchema()));
/* 161:    */             }
/* 162:    */             
/* 163:    */             protected void writeValueTo(Output output, int fieldNumber, Object value, boolean repeated)
/* 164:    */               throws IOException
/* 165:    */             {
/* 166:227 */               output.writeObject(fieldNumber, value, schemaV.getSchema(), repeated);
/* 167:    */             }
/* 168:    */             
/* 169:    */             protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 170:    */               throws IOException
/* 171:    */             {
/* 172:235 */               output.writeObject(number, pipe, schemaV.getPipeSchema(), repeated);
/* 173:    */             }
/* 174:    */           };
/* 175:    */         }
/* 176:    */         
/* 177:    */         private static <T> Field<T> createCollectionPolymorphicV(int number, String name, java.lang.reflect.Field f, CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, final IdStrategy strategy)
/* 178:    */         {
/* 179:246 */           final Accessor accessor = AF.create(f);
/* 180:247 */           new RuntimeCollectionField(WireFormat.FieldType.MESSAGE, number, name, 
/* 181:248 */             (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 182:    */             {
/* 183:    */               protected void mergeFrom(Input input, T message)
/* 184:    */                 throws IOException
/* 185:    */               {
/* 186:253 */                 accessor.set(message, input.mergeObject(accessor
/* 187:254 */                   .get(message), this.schema));
/* 188:    */               }
/* 189:    */               
/* 190:    */               protected void writeTo(Output output, T message)
/* 191:    */                 throws IOException
/* 192:    */               {
/* 193:260 */                 Collection<Object> existing = (Collection)accessor.get(message);
/* 194:261 */                 if (existing != null) {
/* 195:262 */                   output.writeObject(this.number, existing, this.schema, false);
/* 196:    */                 }
/* 197:    */               }
/* 198:    */               
/* 199:    */               protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 200:    */                 throws IOException
/* 201:    */               {
/* 202:269 */                 output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 203:    */               }
/* 204:    */               
/* 205:    */               protected void addValueFrom(Input input, Collection<Object> collection)
/* 206:    */                 throws IOException
/* 207:    */               {
/* 208:276 */                 Object value = input.mergeObject(collection, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA);
/* 209:279 */                 if (((input instanceof GraphInput)) && 
/* 210:280 */                   (((GraphInput)input).isCurrentMessageReference())) {
/* 211:282 */                   collection.add(value);
/* 212:    */                 }
/* 213:    */               }
/* 214:    */               
/* 215:    */               protected void writeValueTo(Output output, int fieldNumber, Object value, boolean repeated)
/* 216:    */                 throws IOException
/* 217:    */               {
/* 218:290 */                 output.writeObject(fieldNumber, value, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA, repeated);
/* 219:    */               }
/* 220:    */               
/* 221:    */               protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 222:    */                 throws IOException
/* 223:    */               {
/* 224:298 */                 output.writeObject(number, pipe, strategy.POLYMORPHIC_POJO_ELEMENT_SCHEMA.pipeSchema, repeated);
/* 225:    */               }
/* 226:    */             };
/* 227:    */           }
/* 228:    */           
/* 229:    */           private static <T> Field<T> createCollectionObjectV(int number, String name, java.lang.reflect.Field f, CollectionSchema.MessageFactory messageFactory, final Schema<Object> valueSchema, final Pipe.Schema<Object> valuePipeSchema, IdStrategy strategy)
/* 230:    */           {
/* 231:310 */             final Accessor accessor = AF.create(f);
/* 232:311 */             new RuntimeCollectionField(WireFormat.FieldType.MESSAGE, number, name, 
/* 233:312 */               (Tag)f.getAnnotation(Tag.class), messageFactory)
/* 234:    */               {
/* 235:    */                 protected void mergeFrom(Input input, T message)
/* 236:    */                   throws IOException
/* 237:    */                 {
/* 238:317 */                   accessor.set(message, input.mergeObject(accessor
/* 239:318 */                     .get(message), this.schema));
/* 240:    */                 }
/* 241:    */                 
/* 242:    */                 protected void writeTo(Output output, T message)
/* 243:    */                   throws IOException
/* 244:    */                 {
/* 245:324 */                   Collection<Object> existing = (Collection)accessor.get(message);
/* 246:325 */                   if (existing != null) {
/* 247:326 */                     output.writeObject(this.number, existing, this.schema, false);
/* 248:    */                   }
/* 249:    */                 }
/* 250:    */                 
/* 251:    */                 protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 252:    */                   throws IOException
/* 253:    */                 {
/* 254:333 */                   output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 255:    */                 }
/* 256:    */                 
/* 257:    */                 protected void addValueFrom(Input input, Collection<Object> collection)
/* 258:    */                   throws IOException
/* 259:    */                 {
/* 260:340 */                   Object value = input.mergeObject(collection, valueSchema);
/* 261:342 */                   if (((input instanceof GraphInput)) && 
/* 262:343 */                     (((GraphInput)input).isCurrentMessageReference())) {
/* 263:345 */                     collection.add(value);
/* 264:    */                   }
/* 265:    */                 }
/* 266:    */                 
/* 267:    */                 protected void writeValueTo(Output output, int fieldNumber, Object value, boolean repeated)
/* 268:    */                   throws IOException
/* 269:    */                 {
/* 270:353 */                   output.writeObject(fieldNumber, value, valueSchema, repeated);
/* 271:    */                 }
/* 272:    */                 
/* 273:    */                 protected void transferValue(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 274:    */                   throws IOException
/* 275:    */                 {
/* 276:360 */                   output.writeObject(number, pipe, valuePipeSchema, repeated);
/* 277:    */                 }
/* 278:    */               };
/* 279:    */             }
/* 280:    */             
/* 281:365 */             private static final RuntimeFieldFactory<Collection<?>> COLLECTION = new RuntimeFieldFactory(25)
/* 282:    */             {
/* 283:    */               public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 284:    */               {
/* 285:373 */                 Class<?> clazz = f.getType();
/* 286:374 */                 Morph morph = (Morph)f.getAnnotation(Morph.class);
/* 287:376 */                 if ((0 != (0x80 & strategy.flags)) && ((morph == null) || 
/* 288:377 */                   (morph.value())))
/* 289:    */                 {
/* 290:379 */                   if ((!clazz.getName().startsWith("java.util")) && 
/* 291:380 */                     (pojo(clazz, morph, strategy))) {
/* 292:382 */                     return POJO.create(number, name, f, strategy);
/* 293:    */                   }
/* 294:385 */                   return OBJECT.create(number, name, f, strategy);
/* 295:    */                 }
/* 296:388 */                 if (Modifier.isAbstract(clazz.getModifiers()))
/* 297:    */                 {
/* 298:390 */                   if (!clazz.isInterface()) {
/* 299:393 */                     return OBJECT.create(number, name, f, strategy);
/* 300:    */                   }
/* 301:396 */                   if (morph == null)
/* 302:    */                   {
/* 303:398 */                     if (0 != (0x10 & strategy.flags)) {
/* 304:399 */                       return OBJECT.create(number, name, f, strategy);
/* 305:    */                     }
/* 306:    */                   }
/* 307:401 */                   else if (morph.value()) {
/* 308:402 */                     return OBJECT.create(number, name, f, strategy);
/* 309:    */                   }
/* 310:    */                 }
/* 311:405 */                 if (EnumSet.class.isAssignableFrom(f.getType()))
/* 312:    */                 {
/* 313:407 */                   Class<Object> enumType = getGenericType(f, 0);
/* 314:409 */                   if (enumType == null) {
/* 315:413 */                     return RuntimeFieldFactory.OBJECT.create(number, name, f, strategy);
/* 316:    */                   }
/* 317:418 */                   return RuntimeCollectionFieldFactory.createCollectionEnumV(number, name, f, strategy
/* 318:419 */                     .getEnumIO(enumType).getEnumSetFactory(), enumType, strategy);
/* 319:    */                 }
/* 320:424 */                 CollectionSchema.MessageFactory messageFactory = strategy.getCollectionFactory(f.getType());
/* 321:    */                 
/* 322:426 */                 Class<Object> genericType = getGenericType(f, 0);
/* 323:428 */                 if ((genericType == null) || (((Map.class.isAssignableFrom(genericType)) || 
/* 324:429 */                   (Collection.class.isAssignableFrom(genericType))) && 
/* 325:430 */                   (!strategy.isRegistered(genericType)))) {
/* 326:433 */                   return RuntimeCollectionFieldFactory.createCollectionObjectV(number, name, f, messageFactory, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 327:    */                 }
/* 328:438 */                 Delegate<Object> inline = getDelegateOrInline(genericType, strategy);
/* 329:440 */                 if (inline != null) {
/* 330:441 */                   return RuntimeCollectionFieldFactory.createCollectionInlineV(number, name, f, messageFactory, inline);
/* 331:    */                 }
/* 332:444 */                 if (Message.class.isAssignableFrom(genericType)) {
/* 333:445 */                   return RuntimeCollectionFieldFactory.createCollectionPojoV(number, name, f, messageFactory, genericType, strategy);
/* 334:    */                 }
/* 335:448 */                 if (genericType.isEnum()) {
/* 336:449 */                   return RuntimeCollectionFieldFactory.createCollectionEnumV(number, name, f, messageFactory, genericType, strategy);
/* 337:    */                 }
/* 338:453 */                 PolymorphicSchema ps = PolymorphicSchemaFactories.getSchemaFromCollectionOrMapGenericType(genericType, strategy);
/* 339:455 */                 if (ps != null) {
/* 340:457 */                   return RuntimeCollectionFieldFactory.createCollectionObjectV(number, name, f, messageFactory, ps, ps
/* 341:458 */                     .getPipeSchema(), strategy);
/* 342:    */                 }
/* 343:461 */                 if (pojo(genericType, morph, strategy)) {
/* 344:462 */                   return RuntimeCollectionFieldFactory.createCollectionPojoV(number, name, f, messageFactory, genericType, strategy);
/* 345:    */                 }
/* 346:465 */                 if (genericType.isInterface()) {
/* 347:467 */                   return RuntimeCollectionFieldFactory.createCollectionObjectV(number, name, f, messageFactory, strategy.OBJECT_ELEMENT_SCHEMA, strategy.OBJECT_ELEMENT_SCHEMA.pipeSchema, strategy);
/* 348:    */                 }
/* 349:472 */                 return RuntimeCollectionFieldFactory.createCollectionPolymorphicV(number, name, f, messageFactory, genericType, strategy);
/* 350:    */               }
/* 351:    */               
/* 352:    */               public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 353:    */                 throws IOException
/* 354:    */               {
/* 355:480 */                 throw new UnsupportedOperationException();
/* 356:    */               }
/* 357:    */               
/* 358:    */               public Collection<?> readFrom(Input input)
/* 359:    */                 throws IOException
/* 360:    */               {
/* 361:486 */                 throw new UnsupportedOperationException();
/* 362:    */               }
/* 363:    */               
/* 364:    */               public void writeTo(Output output, int number, Collection<?> value, boolean repeated)
/* 365:    */                 throws IOException
/* 366:    */               {
/* 367:493 */                 throw new UnsupportedOperationException();
/* 368:    */               }
/* 369:    */               
/* 370:    */               public WireFormat.FieldType getFieldType()
/* 371:    */               {
/* 372:499 */                 throw new UnsupportedOperationException();
/* 373:    */               }
/* 374:    */               
/* 375:    */               public Class<?> typeClass()
/* 376:    */               {
/* 377:505 */                 throw new UnsupportedOperationException();
/* 378:    */               }
/* 379:    */             };
/* 380:    */           }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeCollectionFieldFactory
 * JD-Core Version:    0.7.0.1
 */