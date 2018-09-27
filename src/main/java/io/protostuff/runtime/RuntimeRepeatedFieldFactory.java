/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import io.protostuff.CollectionSchema.MessageFactory;
/*   4:    */ import io.protostuff.GraphInput;
/*   5:    */ import io.protostuff.Input;
/*   6:    */ import io.protostuff.Message;
/*   7:    */ import io.protostuff.Morph;
/*   8:    */ import io.protostuff.Output;
/*   9:    */ import io.protostuff.Pipe;
/*  10:    */ import io.protostuff.Schema;
/*  11:    */ import io.protostuff.Tag;
/*  12:    */ import io.protostuff.WireFormat.FieldType;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.util.Collection;
/*  15:    */ import java.util.EnumSet;
/*  16:    */ import java.util.Map;
/*  17:    */ 
/*  18:    */ final class RuntimeRepeatedFieldFactory
/*  19:    */ {
/*  20:    */   static RuntimeFieldFactory<Collection<?>> getFactory()
/*  21:    */   {
/*  22: 51 */     return REPEATED;
/*  23:    */   }
/*  24:    */   
/*  25: 54 */   static final Accessor.Factory AF = RuntimeFieldFactory.ACCESSOR_FACTORY;
/*  26:    */   
/*  27:    */   private static <T> Field<T> createCollectionInlineV(int number, String name, java.lang.reflect.Field f, final CollectionSchema.MessageFactory messageFactory, final Delegate<Object> inline)
/*  28:    */   {
/*  29: 60 */     final Accessor accessor = AF.create(f);
/*  30: 61 */     new Field(inline.getFieldType(), number, name, true, 
/*  31: 62 */       (Tag)f.getAnnotation(Tag.class))
/*  32:    */       {
/*  33:    */         protected void mergeFrom(Input input, T message)
/*  34:    */           throws IOException
/*  35:    */         {
/*  36: 67 */           Object value = inline.readFrom(input);
/*  37: 68 */           Collection<Object> existing = (Collection)accessor.get(message);
/*  38: 69 */           if (existing == null) {
/*  39: 70 */             accessor.set(message, existing = messageFactory.newMessage());
/*  40:    */           }
/*  41: 72 */           existing.add(value);
/*  42:    */         }
/*  43:    */         
/*  44:    */         protected void writeTo(Output output, T message)
/*  45:    */           throws IOException
/*  46:    */         {
/*  47: 78 */           Collection<Object> collection = (Collection)accessor.get(message);
/*  48: 79 */           if ((collection != null) && (!collection.isEmpty())) {
/*  49: 81 */             for (Object o : collection) {
/*  50: 83 */               if (o != null) {
/*  51: 84 */                 inline.writeTo(output, this.number, o, true);
/*  52:    */               }
/*  53:    */             }
/*  54:    */           }
/*  55:    */         }
/*  56:    */         
/*  57:    */         protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  58:    */           throws IOException
/*  59:    */         {
/*  60: 93 */           inline.transfer(pipe, input, output, this.number, repeated);
/*  61:    */         }
/*  62:    */       };
/*  63:    */     }
/*  64:    */     
/*  65:    */     private static <T> Field<T> createCollectionEnumV(int number, String name, java.lang.reflect.Field f, final CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, final IdStrategy strategy)
/*  66:    */     {
/*  67:104 */       final EnumIO<?> eio = strategy.getEnumIO(genericType);
/*  68:105 */       final Accessor accessor = AF.create(f);
/*  69:106 */       new Field(WireFormat.FieldType.ENUM, number, name, true, 
/*  70:107 */         (Tag)f.getAnnotation(Tag.class))
/*  71:    */         {
/*  72:    */           protected void mergeFrom(Input input, T message)
/*  73:    */             throws IOException
/*  74:    */           {
/*  75:112 */             Enum<?> value = eio.readFrom(input);
/*  76:113 */             Collection<Enum<?>> existing = (Collection)accessor.get(message);
/*  77:114 */             if (existing == null) {
/*  78:115 */               accessor.set(message, existing = messageFactory.newMessage());
/*  79:    */             }
/*  80:117 */             existing.add(value);
/*  81:    */           }
/*  82:    */           
/*  83:    */           protected void writeTo(Output output, T message)
/*  84:    */             throws IOException
/*  85:    */           {
/*  86:123 */             Collection<Enum<?>> collection = (Collection)accessor.get(message);
/*  87:124 */             if ((collection != null) && (!collection.isEmpty())) {
/*  88:126 */               for (Enum<?> en : collection) {
/*  89:128 */                 if (en != null) {
/*  90:129 */                   eio.writeTo(output, this.number, true, en);
/*  91:    */                 }
/*  92:    */               }
/*  93:    */             }
/*  94:    */           }
/*  95:    */           
/*  96:    */           protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/*  97:    */             throws IOException
/*  98:    */           {
/*  99:138 */             EnumIO.transfer(pipe, input, output, this.number, repeated, strategy);
/* 100:    */           }
/* 101:    */         };
/* 102:    */       }
/* 103:    */       
/* 104:    */       private static <T> Field<T> createCollectionPojoV(int number, String name, java.lang.reflect.Field f, final CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, IdStrategy strategy)
/* 105:    */       {
/* 106:148 */         final Accessor accessor = AF.create(f);
/* 107:149 */         new RuntimeMessageField(genericType, strategy
/* 108:150 */           .getSchemaWrapper(genericType, true), WireFormat.FieldType.MESSAGE, number, name, true, 
/* 109:    */           
/* 110:152 */           (Tag)f.getAnnotation(Tag.class))
/* 111:    */           {
/* 112:    */             protected void mergeFrom(Input input, T message)
/* 113:    */               throws IOException
/* 114:    */             {
/* 115:157 */               Object value = input.mergeObject(null, getSchema());
/* 116:158 */               Collection<Object> existing = (Collection)accessor.get(message);
/* 117:159 */               if (existing == null) {
/* 118:160 */                 accessor.set(message, existing = messageFactory.newMessage());
/* 119:    */               }
/* 120:162 */               existing.add(value);
/* 121:    */             }
/* 122:    */             
/* 123:    */             protected void writeTo(Output output, T message)
/* 124:    */               throws IOException
/* 125:    */             {
/* 126:168 */               Collection<Object> collection = (Collection)accessor.get(message);
/* 127:    */               Schema<Object> schema;
/* 128:169 */               if ((collection != null) && (!collection.isEmpty()))
/* 129:    */               {
/* 130:171 */                 schema = getSchema();
/* 131:172 */                 for (Object o : collection) {
/* 132:174 */                   if (o != null) {
/* 133:175 */                     output.writeObject(this.number, o, schema, true);
/* 134:    */                   }
/* 135:    */                 }
/* 136:    */               }
/* 137:    */             }
/* 138:    */             
/* 139:    */             protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 140:    */               throws IOException
/* 141:    */             {
/* 142:184 */               output.writeObject(this.number, pipe, getPipeSchema(), repeated);
/* 143:    */             }
/* 144:    */           };
/* 145:    */         }
/* 146:    */         
/* 147:    */         private static <T> Field<T> createCollectionPolymorphicV(int number, String name, java.lang.reflect.Field f, final CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, IdStrategy strategy)
/* 148:    */         {
/* 149:194 */           final Accessor accessor = AF.create(f);
/* 150:195 */           new RuntimeDerivativeField(genericType, WireFormat.FieldType.MESSAGE, number, name, true, 
/* 151:196 */             (Tag)f.getAnnotation(Tag.class), strategy)
/* 152:    */             {
/* 153:    */               protected void mergeFrom(Input input, T message)
/* 154:    */                 throws IOException
/* 155:    */               {
/* 156:201 */                 Object value = input.mergeObject(message, this.schema);
/* 157:202 */                 if (((input instanceof GraphInput)) && 
/* 158:203 */                   (((GraphInput)input).isCurrentMessageReference()))
/* 159:    */                 {
/* 160:206 */                   Collection<Object> existing = (Collection)accessor.get(message);
/* 161:207 */                   if (existing == null) {
/* 162:208 */                     accessor.set(message, existing = messageFactory.newMessage());
/* 163:    */                   }
/* 164:210 */                   existing.add(value);
/* 165:    */                 }
/* 166:    */               }
/* 167:    */               
/* 168:    */               protected void writeTo(Output output, T message)
/* 169:    */                 throws IOException
/* 170:    */               {
/* 171:217 */                 Collection<Object> existing = (Collection)accessor.get(message);
/* 172:218 */                 if ((existing != null) && (!existing.isEmpty())) {
/* 173:220 */                   for (Object o : existing) {
/* 174:222 */                     if (o != null) {
/* 175:223 */                       output.writeObject(this.number, o, this.schema, true);
/* 176:    */                     }
/* 177:    */                   }
/* 178:    */                 }
/* 179:    */               }
/* 180:    */               
/* 181:    */               protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 182:    */                 throws IOException
/* 183:    */               {
/* 184:232 */                 output.writeObject(this.number, pipe, this.schema.pipeSchema, repeated);
/* 185:    */               }
/* 186:    */               
/* 187:    */               protected void doMergeFrom(Input input, Schema<Object> schema, Object message)
/* 188:    */                 throws IOException
/* 189:    */               {
/* 190:239 */                 Object value = schema.newMessage();
/* 191:240 */                 if ((input instanceof GraphInput)) {
/* 192:243 */                   ((GraphInput)input).updateLast(value, message);
/* 193:    */                 }
/* 194:246 */                 schema.mergeFrom(input, value);
/* 195:247 */                 Collection<Object> existing = (Collection)accessor.get(message);
/* 196:248 */                 if (existing == null) {
/* 197:249 */                   accessor.set(message, existing = messageFactory.newMessage());
/* 198:    */                 }
/* 199:251 */                 existing.add(value);
/* 200:    */               }
/* 201:    */             };
/* 202:    */           }
/* 203:    */           
/* 204:    */           private static <T> Field<T> createCollectionObjectV(int number, String name, java.lang.reflect.Field f, final CollectionSchema.MessageFactory messageFactory, Class<Object> genericType, PolymorphicSchema.Factory factory, IdStrategy strategy)
/* 205:    */           {
/* 206:261 */             final Accessor accessor = AF.create(f);
/* 207:262 */             new RuntimeObjectField(genericType, WireFormat.FieldType.MESSAGE, number, name, true, 
/* 208:263 */               (Tag)f.getAnnotation(Tag.class), factory, strategy)
/* 209:    */               {
/* 210:    */                 protected void mergeFrom(Input input, T message)
/* 211:    */                   throws IOException
/* 212:    */                 {
/* 213:269 */                   Object value = input.mergeObject(message, this.schema);
/* 214:270 */                   if (((input instanceof GraphInput)) && 
/* 215:271 */                     (((GraphInput)input).isCurrentMessageReference()))
/* 216:    */                   {
/* 217:274 */                     Collection<Object> existing = (Collection)accessor.get(message);
/* 218:275 */                     if (existing == null) {
/* 219:276 */                       accessor.set(message, existing = messageFactory.newMessage());
/* 220:    */                     }
/* 221:278 */                     existing.add(value);
/* 222:    */                   }
/* 223:    */                 }
/* 224:    */                 
/* 225:    */                 protected void writeTo(Output output, T message)
/* 226:    */                   throws IOException
/* 227:    */                 {
/* 228:285 */                   Collection<Object> existing = (Collection)accessor.get(message);
/* 229:286 */                   if ((existing != null) && (!existing.isEmpty())) {
/* 230:288 */                     for (Object o : existing) {
/* 231:290 */                       if (o != null) {
/* 232:291 */                         output.writeObject(this.number, o, this.schema, true);
/* 233:    */                       }
/* 234:    */                     }
/* 235:    */                   }
/* 236:    */                 }
/* 237:    */                 
/* 238:    */                 protected void transfer(Pipe pipe, Input input, Output output, boolean repeated)
/* 239:    */                   throws IOException
/* 240:    */                 {
/* 241:300 */                   output.writeObject(this.number, pipe, this.schema.getPipeSchema(), repeated);
/* 242:    */                 }
/* 243:    */                 
/* 244:    */                 public void setValue(Object value, Object message)
/* 245:    */                 {
/* 246:307 */                   Collection<Object> existing = (Collection)accessor.get(message);
/* 247:308 */                   if (existing == null) {
/* 248:309 */                     accessor.set(message, existing = messageFactory.newMessage());
/* 249:    */                   }
/* 250:311 */                   existing.add(value);
/* 251:    */                 }
/* 252:    */               };
/* 253:    */             }
/* 254:    */             
/* 255:316 */             private static final RuntimeFieldFactory<Collection<?>> REPEATED = new RuntimeFieldFactory(25)
/* 256:    */             {
/* 257:    */               public <T> Field<T> create(int number, String name, java.lang.reflect.Field f, IdStrategy strategy)
/* 258:    */               {
/* 259:324 */                 Class<?> clazz = f.getType();
/* 260:325 */                 Morph morph = (Morph)f.getAnnotation(Morph.class);
/* 261:327 */                 if ((0 != (0x80 & strategy.flags)) && ((morph == null) || 
/* 262:328 */                   (morph.value())))
/* 263:    */                 {
/* 264:330 */                   if ((!clazz.getName().startsWith("java.util")) && 
/* 265:331 */                     (pojo(clazz, morph, strategy))) {
/* 266:333 */                     return POJO.create(number, name, f, strategy);
/* 267:    */                   }
/* 268:336 */                   return OBJECT.create(number, name, f, strategy);
/* 269:    */                 }
/* 270:339 */                 if (morph != null) {
/* 271:347 */                   return RuntimeCollectionFieldFactory.getFactory().create(number, name, f, strategy);
/* 272:    */                 }
/* 273:351 */                 if (EnumSet.class.isAssignableFrom(clazz))
/* 274:    */                 {
/* 275:353 */                   Class<Object> enumType = getGenericType(f, 0);
/* 276:355 */                   if (enumType == null) {
/* 277:359 */                     return RuntimeFieldFactory.OBJECT.create(number, name, f, strategy);
/* 278:    */                   }
/* 279:363 */                   return RuntimeRepeatedFieldFactory.createCollectionEnumV(number, name, f, strategy
/* 280:364 */                     .getEnumIO(enumType).getEnumSetFactory(), enumType, strategy);
/* 281:    */                 }
/* 282:369 */                 CollectionSchema.MessageFactory messageFactory = strategy.getCollectionFactory(clazz);
/* 283:    */                 
/* 284:371 */                 Class<Object> genericType = getGenericType(f, 0);
/* 285:373 */                 if ((genericType == null) || (((Map.class.isAssignableFrom(genericType)) || 
/* 286:374 */                   (Collection.class.isAssignableFrom(genericType))) && 
/* 287:375 */                   (!strategy.isRegistered(genericType)))) {
/* 288:378 */                   return RuntimeRepeatedFieldFactory.createCollectionObjectV(number, name, f, messageFactory, genericType, PolymorphicSchemaFactories.OBJECT, strategy);
/* 289:    */                 }
/* 290:383 */                 Delegate<Object> inline = getDelegateOrInline(genericType, strategy);
/* 291:385 */                 if (inline != null) {
/* 292:386 */                   return RuntimeRepeatedFieldFactory.createCollectionInlineV(number, name, f, messageFactory, inline);
/* 293:    */                 }
/* 294:389 */                 if (Message.class.isAssignableFrom(genericType)) {
/* 295:390 */                   return RuntimeRepeatedFieldFactory.createCollectionPojoV(number, name, f, messageFactory, genericType, strategy);
/* 296:    */                 }
/* 297:393 */                 if (genericType.isEnum()) {
/* 298:394 */                   return RuntimeRepeatedFieldFactory.createCollectionEnumV(number, name, f, messageFactory, genericType, strategy);
/* 299:    */                 }
/* 300:398 */                 PolymorphicSchema.Factory factory = PolymorphicSchemaFactories.getFactoryFromRepeatedValueGenericType(genericType);
/* 301:399 */                 if (factory != null) {
/* 302:401 */                   return RuntimeRepeatedFieldFactory.createCollectionObjectV(number, name, f, messageFactory, genericType, factory, strategy);
/* 303:    */                 }
/* 304:405 */                 if (pojo(genericType, morph, strategy)) {
/* 305:406 */                   return RuntimeRepeatedFieldFactory.createCollectionPojoV(number, name, f, messageFactory, genericType, strategy);
/* 306:    */                 }
/* 307:409 */                 if (genericType.isInterface()) {
/* 308:411 */                   return RuntimeRepeatedFieldFactory.createCollectionObjectV(number, name, f, messageFactory, genericType, PolymorphicSchemaFactories.OBJECT, strategy);
/* 309:    */                 }
/* 310:416 */                 return RuntimeRepeatedFieldFactory.createCollectionPolymorphicV(number, name, f, messageFactory, genericType, strategy);
/* 311:    */               }
/* 312:    */               
/* 313:    */               public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
/* 314:    */                 throws IOException
/* 315:    */               {
/* 316:424 */                 throw new UnsupportedOperationException();
/* 317:    */               }
/* 318:    */               
/* 319:    */               public Collection<?> readFrom(Input input)
/* 320:    */                 throws IOException
/* 321:    */               {
/* 322:430 */                 throw new UnsupportedOperationException();
/* 323:    */               }
/* 324:    */               
/* 325:    */               public void writeTo(Output output, int number, Collection<?> value, boolean repeated)
/* 326:    */                 throws IOException
/* 327:    */               {
/* 328:437 */                 throw new UnsupportedOperationException();
/* 329:    */               }
/* 330:    */               
/* 331:    */               public WireFormat.FieldType getFieldType()
/* 332:    */               {
/* 333:443 */                 throw new UnsupportedOperationException();
/* 334:    */               }
/* 335:    */               
/* 336:    */               public Class<?> typeClass()
/* 337:    */               {
/* 338:449 */                 throw new UnsupportedOperationException();
/* 339:    */               }
/* 340:    */             };
/* 341:    */           }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.RuntimeRepeatedFieldFactory
 * JD-Core Version:    0.7.0.1
 */