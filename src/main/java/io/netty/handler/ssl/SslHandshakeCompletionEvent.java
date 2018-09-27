/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ public final class SslHandshakeCompletionEvent
/*  4:   */   extends SslCompletionEvent
/*  5:   */ {
/*  6:25 */   public static final SslHandshakeCompletionEvent SUCCESS = new SslHandshakeCompletionEvent();
/*  7:   */   
/*  8:   */   private SslHandshakeCompletionEvent() {}
/*  9:   */   
/* 10:   */   public SslHandshakeCompletionEvent(Throwable cause)
/* 11:   */   {
/* 12:37 */     super(cause);
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslHandshakeCompletionEvent
 * JD-Core Version:    0.7.0.1
 */