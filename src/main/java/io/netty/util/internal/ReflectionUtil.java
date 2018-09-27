/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.AccessibleObject;
/*  4:   */ 
/*  5:   */ public final class ReflectionUtil
/*  6:   */ {
/*  7:   */   public static Throwable trySetAccessible(AccessibleObject object)
/*  8:   */   {
/*  9:   */     try
/* 10:   */     {
/* 11:31 */       object.setAccessible(true);
/* 12:32 */       return null;
/* 13:   */     }
/* 14:   */     catch (SecurityException e)
/* 15:   */     {
/* 16:34 */       return e;
/* 17:   */     }
/* 18:   */     catch (RuntimeException e)
/* 19:   */     {
/* 20:36 */       return handleInaccessibleObjectException(e);
/* 21:   */     }
/* 22:   */   }
/* 23:   */   
/* 24:   */   private static RuntimeException handleInaccessibleObjectException(RuntimeException e)
/* 25:   */   {
/* 26:44 */     if ("java.lang.reflect.InaccessibleObjectException".equals(e.getClass().getName())) {
/* 27:45 */       return e;
/* 28:   */     }
/* 29:47 */     throw e;
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ReflectionUtil
 * JD-Core Version:    0.7.0.1
 */