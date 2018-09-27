/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.lang.reflect.Method;
/*  5:   */ import javax.net.ssl.SSLEngine;
/*  6:   */ 
/*  7:   */ final class Conscrypt
/*  8:   */ {
/*  9:29 */   private static final Class<?> ENGINES_CLASS = ;
/* 10:   */   
/* 11:   */   static boolean isAvailable()
/* 12:   */   {
/* 13:35 */     return (ENGINES_CLASS != null) && (PlatformDependent.javaVersion() >= 8);
/* 14:   */   }
/* 15:   */   
/* 16:   */   static boolean isEngineSupported(SSLEngine engine)
/* 17:   */   {
/* 18:39 */     return (isAvailable()) && (isConscryptEngine(engine, ENGINES_CLASS));
/* 19:   */   }
/* 20:   */   
/* 21:   */   private static Class<?> getEnginesClass()
/* 22:   */   {
/* 23:   */     try
/* 24:   */     {
/* 25:45 */       Class<?> engineClass = Class.forName("org.conscrypt.Conscrypt$Engines", true, ConscryptAlpnSslEngine.class
/* 26:46 */         .getClassLoader());
/* 27:   */       
/* 28:48 */       getIsConscryptMethod(engineClass);
/* 29:49 */       return engineClass;
/* 30:   */     }
/* 31:   */     catch (Throwable ignore) {}
/* 32:52 */     return null;
/* 33:   */   }
/* 34:   */   
/* 35:   */   private static boolean isConscryptEngine(SSLEngine engine, Class<?> enginesClass)
/* 36:   */   {
/* 37:   */     try
/* 38:   */     {
/* 39:58 */       Method method = getIsConscryptMethod(enginesClass);
/* 40:59 */       return ((Boolean)method.invoke(null, new Object[] { engine })).booleanValue();
/* 41:   */     }
/* 42:   */     catch (Throwable ignore) {}
/* 43:61 */     return false;
/* 44:   */   }
/* 45:   */   
/* 46:   */   private static Method getIsConscryptMethod(Class<?> enginesClass)
/* 47:   */     throws NoSuchMethodException
/* 48:   */   {
/* 49:66 */     return enginesClass.getMethod("isConscrypt", new Class[] { SSLEngine.class });
/* 50:   */   }
/* 51:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.Conscrypt
 * JD-Core Version:    0.7.0.1
 */