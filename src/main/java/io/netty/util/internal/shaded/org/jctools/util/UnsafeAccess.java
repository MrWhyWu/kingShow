/*  1:   */ package io.netty.util.internal.shaded.org.jctools.util;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Constructor;
/*  4:   */ import java.lang.reflect.Field;
/*  5:   */ import sun.misc.Unsafe;
/*  6:   */ 
/*  7:   */ public class UnsafeAccess
/*  8:   */ {
/*  9:   */   public static final boolean SUPPORTS_GET_AND_SET;
/* 10:   */   public static final Unsafe UNSAFE;
/* 11:   */   
/* 12:   */   static
/* 13:   */   {
/* 14:   */     try
/* 15:   */     {
/* 16:46 */       Field field = Unsafe.class.getDeclaredField("theUnsafe");
/* 17:47 */       field.setAccessible(true);
/* 18:48 */       instance = (Unsafe)field.get(null);
/* 19:   */     }
/* 20:   */     catch (Exception ignored)
/* 21:   */     {
/* 22:   */       try
/* 23:   */       {
/* 24:   */         Unsafe instance;
/* 25:58 */         Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor(new Class[0]);
/* 26:59 */         c.setAccessible(true);
/* 27:60 */         instance = (Unsafe)c.newInstance(new Object[0]);
/* 28:   */       }
/* 29:   */       catch (Exception e)
/* 30:   */       {
/* 31:   */         Unsafe instance;
/* 32:64 */         SUPPORTS_GET_AND_SET = false;
/* 33:65 */         throw new RuntimeException(e);
/* 34:   */       }
/* 35:   */     }
/* 36:   */     Unsafe instance;
/* 37:69 */     boolean getAndSetSupport = false;
/* 38:   */     try
/* 39:   */     {
/* 40:72 */       Unsafe.class.getMethod("getAndSetObject", new Class[] { Object.class, Long.TYPE, Object.class });
/* 41:73 */       getAndSetSupport = true;
/* 42:   */     }
/* 43:   */     catch (Exception localException1) {}
/* 44:79 */     UNSAFE = instance;
/* 45:80 */     SUPPORTS_GET_AND_SET = getAndSetSupport;
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess
 * JD-Core Version:    0.7.0.1
 */