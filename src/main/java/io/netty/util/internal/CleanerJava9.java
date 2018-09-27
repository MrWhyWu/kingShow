/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.logging.InternalLogger;
/*  4:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  5:   */ import java.lang.reflect.InvocationTargetException;
/*  6:   */ import java.lang.reflect.Method;
/*  7:   */ import java.nio.ByteBuffer;
/*  8:   */ 
/*  9:   */ final class CleanerJava9
/* 10:   */   implements Cleaner
/* 11:   */ {
/* 12:29 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(CleanerJava9.class);
/* 13:   */   private static final Method INVOKE_CLEANER;
/* 14:   */   
/* 15:   */   static
/* 16:   */   {
/* 17:   */     Throwable error;
/* 18:   */     Method method;
/* 19:   */     Throwable error;
/* 20:36 */     if (PlatformDependent0.hasUnsafe())
/* 21:   */     {
/* 22:37 */       ByteBuffer buffer = ByteBuffer.allocateDirect(1);
/* 23:   */       Object maybeInvokeMethod;
/* 24:   */       try
/* 25:   */       {
/* 26:41 */         Method m = PlatformDependent0.UNSAFE.getClass().getDeclaredMethod("invokeCleaner", new Class[] { ByteBuffer.class });
/* 27:42 */         m.invoke(PlatformDependent0.UNSAFE, new Object[] { buffer });
/* 28:43 */         maybeInvokeMethod = m;
/* 29:   */       }
/* 30:   */       catch (NoSuchMethodException e)
/* 31:   */       {
/* 32:   */         Object maybeInvokeMethod;
/* 33:45 */         maybeInvokeMethod = e;
/* 34:   */       }
/* 35:   */       catch (InvocationTargetException e)
/* 36:   */       {
/* 37:   */         Object maybeInvokeMethod;
/* 38:47 */         maybeInvokeMethod = e;
/* 39:   */       }
/* 40:   */       catch (IllegalAccessException e)
/* 41:   */       {
/* 42:   */         Object maybeInvokeMethod;
/* 43:49 */         maybeInvokeMethod = e;
/* 44:   */       }
/* 45:   */       Throwable error;
/* 46:51 */       if ((maybeInvokeMethod instanceof Throwable))
/* 47:   */       {
/* 48:52 */         Method method = null;
/* 49:53 */         error = (Throwable)maybeInvokeMethod;
/* 50:   */       }
/* 51:   */       else
/* 52:   */       {
/* 53:55 */         Method method = (Method)maybeInvokeMethod;
/* 54:56 */         error = null;
/* 55:   */       }
/* 56:   */     }
/* 57:   */     else
/* 58:   */     {
/* 59:59 */       method = null;
/* 60:60 */       error = new UnsupportedOperationException("sun.misc.Unsafe unavailable");
/* 61:   */     }
/* 62:62 */     if (error == null) {
/* 63:63 */       logger.debug("java.nio.ByteBuffer.cleaner(): available");
/* 64:   */     } else {
/* 65:65 */       logger.debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
/* 66:   */     }
/* 67:67 */     INVOKE_CLEANER = method;
/* 68:   */   }
/* 69:   */   
/* 70:   */   static boolean isSupported()
/* 71:   */   {
/* 72:71 */     return INVOKE_CLEANER != null;
/* 73:   */   }
/* 74:   */   
/* 75:   */   public void freeDirectBuffer(ByteBuffer buffer)
/* 76:   */   {
/* 77:   */     try
/* 78:   */     {
/* 79:77 */       INVOKE_CLEANER.invoke(PlatformDependent0.UNSAFE, new Object[] { buffer });
/* 80:   */     }
/* 81:   */     catch (Throwable cause)
/* 82:   */     {
/* 83:79 */       PlatformDependent0.throwException(cause);
/* 84:   */     }
/* 85:   */   }
/* 86:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.CleanerJava9
 * JD-Core Version:    0.7.0.1
 */