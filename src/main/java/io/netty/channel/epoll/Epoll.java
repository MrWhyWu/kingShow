/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ import io.netty.channel.unix.FileDescriptor;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ 
/*  6:   */ public final class Epoll
/*  7:   */ {
/*  8:   */   private static final Throwable UNAVAILABILITY_CAUSE;
/*  9:   */   
/* 10:   */   static
/* 11:   */   {
/* 12:29 */     Throwable cause = null;
/* 13:30 */     FileDescriptor epollFd = null;
/* 14:31 */     FileDescriptor eventFd = null;
/* 15:   */     try
/* 16:   */     {
/* 17:33 */       epollFd = Native.newEpollCreate();
/* 18:34 */       eventFd = Native.newEventFd();
/* 19:38 */       if (epollFd != null) {
/* 20:   */         try
/* 21:   */         {
/* 22:40 */           epollFd.close();
/* 23:   */         }
/* 24:   */         catch (Exception localException) {}
/* 25:   */       }
/* 26:45 */       if (eventFd != null) {
/* 27:   */         try
/* 28:   */         {
/* 29:47 */           eventFd.close();
/* 30:   */         }
/* 31:   */         catch (Exception localException1) {}
/* 32:   */       }
/* 33:54 */       if (cause == null) {
/* 34:   */         break label113;
/* 35:   */       }
/* 36:   */     }
/* 37:   */     catch (Throwable t)
/* 38:   */     {
/* 39:36 */       cause = t;
/* 40:   */     }
/* 41:   */     finally
/* 42:   */     {
/* 43:38 */       if (epollFd != null) {
/* 44:   */         try
/* 45:   */         {
/* 46:40 */           epollFd.close();
/* 47:   */         }
/* 48:   */         catch (Exception localException4) {}
/* 49:   */       }
/* 50:45 */       if (eventFd != null) {
/* 51:   */         try
/* 52:   */         {
/* 53:47 */           eventFd.close();
/* 54:   */         }
/* 55:   */         catch (Exception localException5) {}
/* 56:   */       }
/* 57:   */     }
/* 58:55 */     UNAVAILABILITY_CAUSE = cause; return;
/* 59:   */     label113:
/* 60:61 */     UNAVAILABILITY_CAUSE = PlatformDependent.hasUnsafe() ? null : new IllegalStateException("sun.misc.Unsafe not available", PlatformDependent.getUnsafeUnavailabilityCause());
/* 61:   */   }
/* 62:   */   
/* 63:   */   public static boolean isAvailable()
/* 64:   */   {
/* 65:70 */     return UNAVAILABILITY_CAUSE == null;
/* 66:   */   }
/* 67:   */   
/* 68:   */   public static void ensureAvailability()
/* 69:   */   {
/* 70:80 */     if (UNAVAILABILITY_CAUSE != null) {
/* 71:82 */       throw ((Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(UNAVAILABILITY_CAUSE));
/* 72:   */     }
/* 73:   */   }
/* 74:   */   
/* 75:   */   public static Throwable unavailabilityCause()
/* 76:   */   {
/* 77:93 */     return UNAVAILABILITY_CAUSE;
/* 78:   */   }
/* 79:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.Epoll
 * JD-Core Version:    0.7.0.1
 */