/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.channel.unix.FileDescriptor;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ 
/*  6:   */ public final class KQueue
/*  7:   */ {
/*  8:   */   private static final Throwable UNAVAILABILITY_CAUSE;
/*  9:   */   
/* 10:   */   static
/* 11:   */   {
/* 12:30 */     Throwable cause = null;
/* 13:31 */     FileDescriptor kqueueFd = null;
/* 14:   */     try
/* 15:   */     {
/* 16:33 */       kqueueFd = Native.newKQueue();
/* 17:37 */       if (kqueueFd != null) {
/* 18:   */         try
/* 19:   */         {
/* 20:39 */           kqueueFd.close();
/* 21:   */         }
/* 22:   */         catch (Exception localException) {}
/* 23:   */       }
/* 24:46 */       if (cause == null) {
/* 25:   */         break label68;
/* 26:   */       }
/* 27:   */     }
/* 28:   */     catch (Throwable t)
/* 29:   */     {
/* 30:35 */       cause = t;
/* 31:   */     }
/* 32:   */     finally
/* 33:   */     {
/* 34:37 */       if (kqueueFd != null) {
/* 35:   */         try
/* 36:   */         {
/* 37:39 */           kqueueFd.close();
/* 38:   */         }
/* 39:   */         catch (Exception localException2) {}
/* 40:   */       }
/* 41:   */     }
/* 42:47 */     UNAVAILABILITY_CAUSE = cause; return;
/* 43:   */     label68:
/* 44:53 */     UNAVAILABILITY_CAUSE = PlatformDependent.hasUnsafe() ? null : new IllegalStateException("sun.misc.Unsafe not available", PlatformDependent.getUnsafeUnavailabilityCause());
/* 45:   */   }
/* 46:   */   
/* 47:   */   public static boolean isAvailable()
/* 48:   */   {
/* 49:62 */     return UNAVAILABILITY_CAUSE == null;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public static void ensureAvailability()
/* 53:   */   {
/* 54:72 */     if (UNAVAILABILITY_CAUSE != null) {
/* 55:74 */       throw ((Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(UNAVAILABILITY_CAUSE));
/* 56:   */     }
/* 57:   */   }
/* 58:   */   
/* 59:   */   public static Throwable unavailabilityCause()
/* 60:   */   {
/* 61:85 */     return UNAVAILABILITY_CAUSE;
/* 62:   */   }
/* 63:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueue
 * JD-Core Version:    0.7.0.1
 */