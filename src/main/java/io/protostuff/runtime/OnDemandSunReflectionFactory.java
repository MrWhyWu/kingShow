/*  1:   */ package io.protostuff.runtime;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Constructor;
/*  4:   */ import sun.reflect.ReflectionFactory;
/*  5:   */ 
/*  6:   */ final class OnDemandSunReflectionFactory
/*  7:   */ {
/*  8:   */   static <T> Constructor<T> getConstructor(Class<T> clazz, Constructor<Object> constructor)
/*  9:   */   {
/* 10:40 */     return 
/* 11:41 */       ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, constructor);
/* 12:   */   }
/* 13:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.runtime.OnDemandSunReflectionFactory
 * JD-Core Version:    0.7.0.1
 */