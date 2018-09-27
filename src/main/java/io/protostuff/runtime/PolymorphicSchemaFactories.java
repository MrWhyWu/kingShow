/*   1:    */ package io.protostuff.runtime;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Field;
/*   4:    */ import java.util.Collection;
/*   5:    */ import java.util.Map;
/*   6:    */ 
/*   7:    */ public enum PolymorphicSchemaFactories
/*   8:    */   implements PolymorphicSchema.Factory
/*   9:    */ {
/*  10: 45 */   ARRAY,  NUMBER,  CLASS,  ENUM,  COLLECTION,  MAP,  THROWABLE,  POJO,  POJO_MAP,  POJO_COLLECTION,  OBJECT;
/*  11:    */   
/*  12:    */   private PolymorphicSchemaFactories() {}
/*  13:    */   
/*  14:    */   public static PolymorphicSchema.Factory getFactoryFromField(Field f, IdStrategy strategy)
/*  15:    */   {
/*  16:256 */     Class<?> clazz = f.getType();
/*  17:258 */     if (clazz.isArray()) {
/*  18:259 */       return ARRAY;
/*  19:    */     }
/*  20:261 */     if (Number.class == clazz) {
/*  21:262 */       return NUMBER;
/*  22:    */     }
/*  23:264 */     if (Class.class == clazz) {
/*  24:265 */       return CLASS;
/*  25:    */     }
/*  26:267 */     if (Enum.class == clazz) {
/*  27:268 */       return ENUM;
/*  28:    */     }
/*  29:270 */     if (Map.class.isAssignableFrom(clazz)) {
/*  30:272 */       return 0 != (0x100 & strategy.flags) ? POJO_MAP : MAP;
/*  31:    */     }
/*  32:276 */     if (Collection.class.isAssignableFrom(clazz)) {
/*  33:278 */       return 0 != (0x80 & strategy.flags) ? POJO_COLLECTION : COLLECTION;
/*  34:    */     }
/*  35:282 */     if (Throwable.class.isAssignableFrom(clazz)) {
/*  36:283 */       return THROWABLE;
/*  37:    */     }
/*  38:285 */     return OBJECT;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static PolymorphicSchema.Factory getFactoryFromRepeatedValueGenericType(Class<?> clazz)
/*  42:    */   {
/*  43:291 */     if (clazz.isArray()) {
/*  44:292 */       return ARRAY;
/*  45:    */     }
/*  46:294 */     if (Number.class == clazz) {
/*  47:295 */       return NUMBER;
/*  48:    */     }
/*  49:297 */     if (Class.class == clazz) {
/*  50:298 */       return CLASS;
/*  51:    */     }
/*  52:300 */     if (Enum.class == clazz) {
/*  53:301 */       return ENUM;
/*  54:    */     }
/*  55:303 */     if (Throwable.class.isAssignableFrom(clazz)) {
/*  56:304 */       return THROWABLE;
/*  57:    */     }
/*  58:306 */     if (Object.class == clazz) {
/*  59:307 */       return OBJECT;
/*  60:    */     }
/*  61:309 */     return null;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public static PolymorphicSchema getSchemaFromCollectionOrMapGenericType(Class<?> clazz, IdStrategy strategy)
/*  65:    */   {
/*  66:315 */     if (clazz.isArray())
/*  67:    */     {
/*  68:318 */       Class<Object> ct = clazz.getComponentType();
/*  69:    */       
/*  70:320 */       RuntimeFieldFactory<?> rff = RuntimeFieldFactory.getFieldFactory(ct, strategy);
/*  71:323 */       if (rff == RuntimeFieldFactory.DELEGATE) {
/*  72:326 */         return strategy.getDelegateWrapper(ct).genericElementSchema;
/*  73:    */       }
/*  74:329 */       if ((rff.id > 0) && (rff.id < 15)) {
/*  75:332 */         return ArraySchemas.getGenericElementSchema(rff.id, strategy);
/*  76:    */       }
/*  77:335 */       if (ct.isEnum()) {
/*  78:338 */         return strategy.getEnumIO(ct).genericElementSchema;
/*  79:    */       }
/*  80:341 */       if (rff != RuntimeFieldFactory.POJO)
/*  81:    */       {
/*  82:341 */         if (rff == RuntimeFieldFactory.POLYMORPHIC_POJO) {
/*  83:343 */           if (!RuntimeFieldFactory.pojo(ct, null, strategy)) {}
/*  84:    */         }
/*  85:    */       }
/*  86:    */       else {
/*  87:346 */         return strategy.getSchemaWrapper(ct, true).genericElementSchema;
/*  88:    */       }
/*  89:349 */       return strategy.ARRAY_ELEMENT_SCHEMA;
/*  90:    */     }
/*  91:352 */     if (Number.class == clazz) {
/*  92:353 */       return strategy.NUMBER_ELEMENT_SCHEMA;
/*  93:    */     }
/*  94:355 */     if (Class.class == clazz) {
/*  95:356 */       return strategy.CLASS_ELEMENT_SCHEMA;
/*  96:    */     }
/*  97:358 */     if (Enum.class == clazz) {
/*  98:359 */       return strategy.POLYMORPHIC_ENUM_ELEMENT_SCHEMA;
/*  99:    */     }
/* 100:361 */     if (Throwable.class.isAssignableFrom(clazz)) {
/* 101:362 */       return strategy.POLYMORPHIC_THROWABLE_ELEMENT_SCHEMA;
/* 102:    */     }
/* 103:364 */     if (Object.class == clazz) {
/* 104:365 */       return strategy.OBJECT_ELEMENT_SCHEMA;
/* 105:    */     }
/* 106:367 */     return null;
/* 107:    */   }
/* 108:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.PolymorphicSchemaFactories
 * JD-Core Version:    0.7.0.1
 */