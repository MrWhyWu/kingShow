/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ import java.util.Arrays;
/*  4:   */ 
/*  5:   */ @Deprecated
/*  6:   */ public class ResourceLeakException
/*  7:   */   extends RuntimeException
/*  8:   */ {
/*  9:   */   private static final long serialVersionUID = 7186453858343358280L;
/* 10:   */   private final StackTraceElement[] cachedStackTrace;
/* 11:   */   
/* 12:   */   public ResourceLeakException()
/* 13:   */   {
/* 14:32 */     this.cachedStackTrace = getStackTrace();
/* 15:   */   }
/* 16:   */   
/* 17:   */   public ResourceLeakException(String message)
/* 18:   */   {
/* 19:36 */     super(message);
/* 20:37 */     this.cachedStackTrace = getStackTrace();
/* 21:   */   }
/* 22:   */   
/* 23:   */   public ResourceLeakException(String message, Throwable cause)
/* 24:   */   {
/* 25:41 */     super(message, cause);
/* 26:42 */     this.cachedStackTrace = getStackTrace();
/* 27:   */   }
/* 28:   */   
/* 29:   */   public ResourceLeakException(Throwable cause)
/* 30:   */   {
/* 31:46 */     super(cause);
/* 32:47 */     this.cachedStackTrace = getStackTrace();
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int hashCode()
/* 36:   */   {
/* 37:52 */     StackTraceElement[] trace = this.cachedStackTrace;
/* 38:53 */     int hashCode = 0;
/* 39:54 */     for (StackTraceElement e : trace) {
/* 40:55 */       hashCode = hashCode * 31 + e.hashCode();
/* 41:   */     }
/* 42:57 */     return hashCode;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public boolean equals(Object o)
/* 46:   */   {
/* 47:62 */     if (!(o instanceof ResourceLeakException)) {
/* 48:63 */       return false;
/* 49:   */     }
/* 50:65 */     if (o == this) {
/* 51:66 */       return true;
/* 52:   */     }
/* 53:69 */     return Arrays.equals(this.cachedStackTrace, ((ResourceLeakException)o).cachedStackTrace);
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ResourceLeakException
 * JD-Core Version:    0.7.0.1
 */