/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import java.util.Map;
/*  4:   */ 
/*  5:   */ class CachingClassResolver
/*  6:   */   implements ClassResolver
/*  7:   */ {
/*  8:   */   private final Map<String, Class<?>> classCache;
/*  9:   */   private final ClassResolver delegate;
/* 10:   */   
/* 11:   */   CachingClassResolver(ClassResolver delegate, Map<String, Class<?>> classCache)
/* 12:   */   {
/* 13:26 */     this.delegate = delegate;
/* 14:27 */     this.classCache = classCache;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public Class<?> resolve(String className)
/* 18:   */     throws ClassNotFoundException
/* 19:   */   {
/* 20:34 */     Class<?> clazz = (Class)this.classCache.get(className);
/* 21:35 */     if (clazz != null) {
/* 22:36 */       return clazz;
/* 23:   */     }
/* 24:40 */     clazz = this.delegate.resolve(className);
/* 25:   */     
/* 26:42 */     this.classCache.put(className, clazz);
/* 27:43 */     return clazz;
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.CachingClassResolver
 * JD-Core Version:    0.7.0.1
 */