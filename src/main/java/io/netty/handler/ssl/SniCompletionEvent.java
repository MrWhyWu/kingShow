/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ public final class SniCompletionEvent
/*  4:   */   extends SslCompletionEvent
/*  5:   */ {
/*  6:   */   private final String hostname;
/*  7:   */   
/*  8:   */   SniCompletionEvent(String hostname)
/*  9:   */   {
/* 10:29 */     this.hostname = hostname;
/* 11:   */   }
/* 12:   */   
/* 13:   */   SniCompletionEvent(String hostname, Throwable cause)
/* 14:   */   {
/* 15:33 */     super(cause);
/* 16:34 */     this.hostname = hostname;
/* 17:   */   }
/* 18:   */   
/* 19:   */   SniCompletionEvent(Throwable cause)
/* 20:   */   {
/* 21:38 */     this(null, cause);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public String hostname()
/* 25:   */   {
/* 26:45 */     return this.hostname;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public String toString()
/* 30:   */   {
/* 31:50 */     Throwable cause = cause();
/* 32:51 */     return 
/* 33:52 */       getClass().getSimpleName() + '(' + cause + ')';
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SniCompletionEvent
 * JD-Core Version:    0.7.0.1
 */