/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ public final class SslCloseCompletionEvent
/*  4:   */   extends SslCompletionEvent
/*  5:   */ {
/*  6:23 */   public static final SslCloseCompletionEvent SUCCESS = new SslCloseCompletionEvent();
/*  7:   */   
/*  8:   */   private SslCloseCompletionEvent() {}
/*  9:   */   
/* 10:   */   public SslCloseCompletionEvent(Throwable cause)
/* 11:   */   {
/* 12:35 */     super(cause);
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslCloseCompletionEvent
 * JD-Core Version:    0.7.0.1
 */