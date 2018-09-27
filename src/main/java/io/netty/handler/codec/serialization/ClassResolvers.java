/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.util.HashMap;
/*  5:   */ 
/*  6:   */ public final class ClassResolvers
/*  7:   */ {
/*  8:   */   public static ClassResolver cacheDisabled(ClassLoader classLoader)
/*  9:   */   {
/* 10:31 */     return new ClassLoaderClassResolver(defaultClassLoader(classLoader));
/* 11:   */   }
/* 12:   */   
/* 13:   */   public static ClassResolver weakCachingResolver(ClassLoader classLoader)
/* 14:   */   {
/* 15:42 */     return new CachingClassResolver(new ClassLoaderClassResolver(
/* 16:43 */       defaultClassLoader(classLoader)), new WeakReferenceMap(new HashMap()));
/* 17:   */   }
/* 18:   */   
/* 19:   */   public static ClassResolver softCachingResolver(ClassLoader classLoader)
/* 20:   */   {
/* 21:55 */     return new CachingClassResolver(new ClassLoaderClassResolver(
/* 22:56 */       defaultClassLoader(classLoader)), new SoftReferenceMap(new HashMap()));
/* 23:   */   }
/* 24:   */   
/* 25:   */   public static ClassResolver weakCachingConcurrentResolver(ClassLoader classLoader)
/* 26:   */   {
/* 27:68 */     return new CachingClassResolver(new ClassLoaderClassResolver(
/* 28:69 */       defaultClassLoader(classLoader)), new WeakReferenceMap(
/* 29:   */       
/* 30:71 */       PlatformDependent.newConcurrentHashMap()));
/* 31:   */   }
/* 32:   */   
/* 33:   */   public static ClassResolver softCachingConcurrentResolver(ClassLoader classLoader)
/* 34:   */   {
/* 35:82 */     return new CachingClassResolver(new ClassLoaderClassResolver(
/* 36:83 */       defaultClassLoader(classLoader)), new SoftReferenceMap(
/* 37:   */       
/* 38:85 */       PlatformDependent.newConcurrentHashMap()));
/* 39:   */   }
/* 40:   */   
/* 41:   */   static ClassLoader defaultClassLoader(ClassLoader classLoader)
/* 42:   */   {
/* 43:89 */     if (classLoader != null) {
/* 44:90 */       return classLoader;
/* 45:   */     }
/* 46:93 */     ClassLoader contextClassLoader = PlatformDependent.getContextClassLoader();
/* 47:94 */     if (contextClassLoader != null) {
/* 48:95 */       return contextClassLoader;
/* 49:   */     }
/* 50:98 */     return PlatformDependent.getClassLoader(ClassResolvers.class);
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ClassResolvers
 * JD-Core Version:    0.7.0.1
 */