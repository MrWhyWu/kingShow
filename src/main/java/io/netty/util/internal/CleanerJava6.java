/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.logging.InternalLogger;
/*  4:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  5:   */ import java.lang.reflect.Field;
/*  6:   */ import java.lang.reflect.Method;
/*  7:   */ import java.nio.ByteBuffer;
/*  8:   */ 
/*  9:   */ final class CleanerJava6
/* 10:   */   implements Cleaner
/* 11:   */ {
/* 12:   */   private static final long CLEANER_FIELD_OFFSET;
/* 13:   */   private static final Method CLEAN_METHOD;
/* 14:36 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(CleanerJava6.class);
/* 15:   */   
/* 16:   */   static
/* 17:   */   {
/* 18:39 */     long fieldOffset = -1L;
/* 19:40 */     Method clean = null;
/* 20:41 */     Throwable error = null;
/* 21:42 */     if (PlatformDependent0.hasUnsafe())
/* 22:   */     {
/* 23:43 */       ByteBuffer direct = ByteBuffer.allocateDirect(1);
/* 24:   */       try
/* 25:   */       {
/* 26:45 */         Field cleanerField = direct.getClass().getDeclaredField("cleaner");
/* 27:46 */         fieldOffset = PlatformDependent0.objectFieldOffset(cleanerField);
/* 28:47 */         Object cleaner = PlatformDependent0.getObject(direct, fieldOffset);
/* 29:48 */         clean = cleaner.getClass().getDeclaredMethod("clean", new Class[0]);
/* 30:49 */         clean.invoke(cleaner, new Object[0]);
/* 31:   */       }
/* 32:   */       catch (Throwable t)
/* 33:   */       {
/* 34:52 */         fieldOffset = -1L;
/* 35:53 */         clean = null;
/* 36:54 */         error = t;
/* 37:   */       }
/* 38:   */     }
/* 39:   */     else
/* 40:   */     {
/* 41:57 */       error = new UnsupportedOperationException("sun.misc.Unsafe unavailable");
/* 42:   */     }
/* 43:59 */     if (error == null) {
/* 44:60 */       logger.debug("java.nio.ByteBuffer.cleaner(): available");
/* 45:   */     } else {
/* 46:62 */       logger.debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
/* 47:   */     }
/* 48:64 */     CLEANER_FIELD_OFFSET = fieldOffset;
/* 49:65 */     CLEAN_METHOD = clean;
/* 50:   */   }
/* 51:   */   
/* 52:   */   static boolean isSupported()
/* 53:   */   {
/* 54:69 */     return CLEANER_FIELD_OFFSET != -1L;
/* 55:   */   }
/* 56:   */   
/* 57:   */   public void freeDirectBuffer(ByteBuffer buffer)
/* 58:   */   {
/* 59:74 */     if (!buffer.isDirect()) {
/* 60:75 */       return;
/* 61:   */     }
/* 62:   */     try
/* 63:   */     {
/* 64:78 */       Object cleaner = PlatformDependent0.getObject(buffer, CLEANER_FIELD_OFFSET);
/* 65:79 */       if (cleaner != null) {
/* 66:80 */         CLEAN_METHOD.invoke(cleaner, new Object[0]);
/* 67:   */       }
/* 68:   */     }
/* 69:   */     catch (Throwable cause)
/* 70:   */     {
/* 71:83 */       PlatformDependent0.throwException(cause);
/* 72:   */     }
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.CleanerJava6
 * JD-Core Version:    0.7.0.1
 */