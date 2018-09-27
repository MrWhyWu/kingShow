/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ class ClassLoaderClassResolver
/*  4:   */   implements ClassResolver
/*  5:   */ {
/*  6:   */   private final ClassLoader classLoader;
/*  7:   */   
/*  8:   */   ClassLoaderClassResolver(ClassLoader classLoader)
/*  9:   */   {
/* 10:23 */     this.classLoader = classLoader;
/* 11:   */   }
/* 12:   */   
/* 13:   */   public Class<?> resolve(String className)
/* 14:   */     throws ClassNotFoundException
/* 15:   */   {
/* 16:   */     try
/* 17:   */     {
/* 18:29 */       return this.classLoader.loadClass(className);
/* 19:   */     }
/* 20:   */     catch (ClassNotFoundException ignored) {}
/* 21:31 */     return Class.forName(className, false, this.classLoader);
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ClassLoaderClassResolver
 * JD-Core Version:    0.7.0.1
 */