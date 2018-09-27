/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.io.ByteArrayOutputStream;
/*  4:   */ import java.io.IOException;
/*  5:   */ import java.io.PrintStream;
/*  6:   */ import java.lang.reflect.InvocationTargetException;
/*  7:   */ import java.lang.reflect.Method;
/*  8:   */ import java.util.List;
/*  9:   */ 
/* 10:   */ public final class ThrowableUtil
/* 11:   */ {
/* 12:27 */   private static final Method addSupressedMethod = ;
/* 13:   */   
/* 14:   */   private static Method getAddSuppressed()
/* 15:   */   {
/* 16:30 */     if (PlatformDependent.javaVersion() < 7) {
/* 17:31 */       return null;
/* 18:   */     }
/* 19:   */     try
/* 20:   */     {
/* 21:35 */       return Throwable.class.getDeclaredMethod("addSuppressed", new Class[] { Throwable.class });
/* 22:   */     }
/* 23:   */     catch (NoSuchMethodException e)
/* 24:   */     {
/* 25:37 */       throw new RuntimeException(e);
/* 26:   */     }
/* 27:   */   }
/* 28:   */   
/* 29:   */   public static <T extends Throwable> T unknownStackTrace(T cause, Class<?> clazz, String method)
/* 30:   */   {
/* 31:47 */     cause.setStackTrace(new StackTraceElement[] { new StackTraceElement(clazz.getName(), method, null, -1) });
/* 32:48 */     return cause;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public static String stackTraceToString(Throwable cause)
/* 36:   */   {
/* 37:58 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 38:59 */     PrintStream pout = new PrintStream(out);
/* 39:60 */     cause.printStackTrace(pout);
/* 40:61 */     pout.flush();
/* 41:   */     try
/* 42:   */     {
/* 43:63 */       return new String(out.toByteArray());
/* 44:   */     }
/* 45:   */     finally
/* 46:   */     {
/* 47:   */       try
/* 48:   */       {
/* 49:66 */         out.close();
/* 50:   */       }
/* 51:   */       catch (IOException localIOException1) {}
/* 52:   */     }
/* 53:   */   }
/* 54:   */   
/* 55:   */   public static boolean haveSuppressed()
/* 56:   */   {
/* 57:74 */     return addSupressedMethod != null;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public static void addSuppressed(Throwable target, Throwable suppressed)
/* 61:   */   {
/* 62:78 */     if (!haveSuppressed()) {
/* 63:79 */       return;
/* 64:   */     }
/* 65:   */     try
/* 66:   */     {
/* 67:82 */       addSupressedMethod.invoke(target, new Object[] { suppressed });
/* 68:   */     }
/* 69:   */     catch (IllegalAccessException e)
/* 70:   */     {
/* 71:84 */       throw new RuntimeException(e);
/* 72:   */     }
/* 73:   */     catch (InvocationTargetException e)
/* 74:   */     {
/* 75:86 */       throw new RuntimeException(e);
/* 76:   */     }
/* 77:   */   }
/* 78:   */   
/* 79:   */   public static void addSuppressedAndClear(Throwable target, List<Throwable> suppressed)
/* 80:   */   {
/* 81:91 */     addSuppressed(target, suppressed);
/* 82:92 */     suppressed.clear();
/* 83:   */   }
/* 84:   */   
/* 85:   */   public static void addSuppressed(Throwable target, List<Throwable> suppressed)
/* 86:   */   {
/* 87:96 */     for (Throwable t : suppressed) {
/* 88:97 */       addSuppressed(target, t);
/* 89:   */     }
/* 90:   */   }
/* 91:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ThrowableUtil
 * JD-Core Version:    0.7.0.1
 */