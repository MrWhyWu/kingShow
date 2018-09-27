/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Array;
/*   4:    */ import java.lang.reflect.GenericArrayType;
/*   5:    */ import java.lang.reflect.ParameterizedType;
/*   6:    */ import java.lang.reflect.Type;
/*   7:    */ import java.lang.reflect.TypeVariable;
/*   8:    */ import java.util.HashMap;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public abstract class TypeParameterMatcher
/*  12:    */ {
/*  13: 29 */   private static final TypeParameterMatcher NOOP = new TypeParameterMatcher()
/*  14:    */   {
/*  15:    */     public boolean match(Object msg)
/*  16:    */     {
/*  17: 32 */       return true;
/*  18:    */     }
/*  19:    */   };
/*  20:    */   
/*  21:    */   public static TypeParameterMatcher get(Class<?> parameterType)
/*  22:    */   {
/*  23: 38 */     Map<Class<?>, TypeParameterMatcher> getCache = InternalThreadLocalMap.get().typeParameterMatcherGetCache();
/*  24:    */     
/*  25: 40 */     TypeParameterMatcher matcher = (TypeParameterMatcher)getCache.get(parameterType);
/*  26: 41 */     if (matcher == null)
/*  27:    */     {
/*  28: 42 */       if (parameterType == Object.class) {
/*  29: 43 */         matcher = NOOP;
/*  30:    */       } else {
/*  31: 45 */         matcher = new ReflectiveMatcher(parameterType);
/*  32:    */       }
/*  33: 47 */       getCache.put(parameterType, matcher);
/*  34:    */     }
/*  35: 50 */     return matcher;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static TypeParameterMatcher find(Object object, Class<?> parametrizedSuperclass, String typeParamName)
/*  39:    */   {
/*  40: 57 */     Map<Class<?>, Map<String, TypeParameterMatcher>> findCache = InternalThreadLocalMap.get().typeParameterMatcherFindCache();
/*  41: 58 */     Class<?> thisClass = object.getClass();
/*  42:    */     
/*  43: 60 */     Map<String, TypeParameterMatcher> map = (Map)findCache.get(thisClass);
/*  44: 61 */     if (map == null)
/*  45:    */     {
/*  46: 62 */       map = new HashMap();
/*  47: 63 */       findCache.put(thisClass, map);
/*  48:    */     }
/*  49: 66 */     TypeParameterMatcher matcher = (TypeParameterMatcher)map.get(typeParamName);
/*  50: 67 */     if (matcher == null)
/*  51:    */     {
/*  52: 68 */       matcher = get(find0(object, parametrizedSuperclass, typeParamName));
/*  53: 69 */       map.put(typeParamName, matcher);
/*  54:    */     }
/*  55: 72 */     return matcher;
/*  56:    */   }
/*  57:    */   
/*  58:    */   private static Class<?> find0(Object object, Class<?> parametrizedSuperclass, String typeParamName)
/*  59:    */   {
/*  60: 78 */     Class<?> thisClass = object.getClass();
/*  61: 79 */     Class<?> currentClass = thisClass;
/*  62:    */     do
/*  63:    */     {
/*  64: 81 */       while (currentClass.getSuperclass() == parametrizedSuperclass)
/*  65:    */       {
/*  66: 82 */         int typeParamIndex = -1;
/*  67: 83 */         TypeVariable<?>[] typeParams = currentClass.getSuperclass().getTypeParameters();
/*  68: 84 */         for (int i = 0; i < typeParams.length; i++) {
/*  69: 85 */           if (typeParamName.equals(typeParams[i].getName()))
/*  70:    */           {
/*  71: 86 */             typeParamIndex = i;
/*  72: 87 */             break;
/*  73:    */           }
/*  74:    */         }
/*  75: 91 */         if (typeParamIndex < 0) {
/*  76: 92 */           throw new IllegalStateException("unknown type parameter '" + typeParamName + "': " + parametrizedSuperclass);
/*  77:    */         }
/*  78: 96 */         Type genericSuperType = currentClass.getGenericSuperclass();
/*  79: 97 */         if (!(genericSuperType instanceof ParameterizedType)) {
/*  80: 98 */           return Object.class;
/*  81:    */         }
/*  82:101 */         Type[] actualTypeParams = ((ParameterizedType)genericSuperType).getActualTypeArguments();
/*  83:    */         
/*  84:103 */         Type actualTypeParam = actualTypeParams[typeParamIndex];
/*  85:104 */         if ((actualTypeParam instanceof ParameterizedType)) {
/*  86:105 */           actualTypeParam = ((ParameterizedType)actualTypeParam).getRawType();
/*  87:    */         }
/*  88:107 */         if ((actualTypeParam instanceof Class)) {
/*  89:108 */           return (Class)actualTypeParam;
/*  90:    */         }
/*  91:110 */         if ((actualTypeParam instanceof GenericArrayType))
/*  92:    */         {
/*  93:111 */           Type componentType = ((GenericArrayType)actualTypeParam).getGenericComponentType();
/*  94:112 */           if ((componentType instanceof ParameterizedType)) {
/*  95:113 */             componentType = ((ParameterizedType)componentType).getRawType();
/*  96:    */           }
/*  97:115 */           if ((componentType instanceof Class)) {
/*  98:116 */             return Array.newInstance((Class)componentType, 0).getClass();
/*  99:    */           }
/* 100:    */         }
/* 101:119 */         if ((actualTypeParam instanceof TypeVariable))
/* 102:    */         {
/* 103:121 */           TypeVariable<?> v = (TypeVariable)actualTypeParam;
/* 104:122 */           currentClass = thisClass;
/* 105:123 */           if (!(v.getGenericDeclaration() instanceof Class)) {
/* 106:124 */             return Object.class;
/* 107:    */           }
/* 108:127 */           parametrizedSuperclass = (Class)v.getGenericDeclaration();
/* 109:128 */           typeParamName = v.getName();
/* 110:129 */           if (!parametrizedSuperclass.isAssignableFrom(thisClass)) {
/* 111:132 */             return Object.class;
/* 112:    */           }
/* 113:    */         }
/* 114:    */         else
/* 115:    */         {
/* 116:136 */           return fail(thisClass, typeParamName);
/* 117:    */         }
/* 118:    */       }
/* 119:138 */       currentClass = currentClass.getSuperclass();
/* 120:139 */     } while (currentClass != null);
/* 121:140 */     return fail(thisClass, typeParamName);
/* 122:    */   }
/* 123:    */   
/* 124:    */   private static Class<?> fail(Class<?> type, String typeParamName)
/* 125:    */   {
/* 126:146 */     throw new IllegalStateException("cannot determine the type of the type parameter '" + typeParamName + "': " + type);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public abstract boolean match(Object paramObject);
/* 130:    */   
/* 131:    */   private static final class ReflectiveMatcher
/* 132:    */     extends TypeParameterMatcher
/* 133:    */   {
/* 134:    */     private final Class<?> type;
/* 135:    */     
/* 136:    */     ReflectiveMatcher(Class<?> type)
/* 137:    */     {
/* 138:156 */       this.type = type;
/* 139:    */     }
/* 140:    */     
/* 141:    */     public boolean match(Object msg)
/* 142:    */     {
/* 143:161 */       return this.type.isInstance(msg);
/* 144:    */     }
/* 145:    */   }
/* 146:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.TypeParameterMatcher
 * JD-Core Version:    0.7.0.1
 */