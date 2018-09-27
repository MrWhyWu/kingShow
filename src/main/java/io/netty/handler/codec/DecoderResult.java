/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ import io.netty.util.Signal;
/*  4:   */ 
/*  5:   */ public class DecoderResult
/*  6:   */ {
/*  7:22 */   protected static final Signal SIGNAL_UNFINISHED = Signal.valueOf(DecoderResult.class, "UNFINISHED");
/*  8:23 */   protected static final Signal SIGNAL_SUCCESS = Signal.valueOf(DecoderResult.class, "SUCCESS");
/*  9:25 */   public static final DecoderResult UNFINISHED = new DecoderResult(SIGNAL_UNFINISHED);
/* 10:26 */   public static final DecoderResult SUCCESS = new DecoderResult(SIGNAL_SUCCESS);
/* 11:   */   private final Throwable cause;
/* 12:   */   
/* 13:   */   public static DecoderResult failure(Throwable cause)
/* 14:   */   {
/* 15:29 */     if (cause == null) {
/* 16:30 */       throw new NullPointerException("cause");
/* 17:   */     }
/* 18:32 */     return new DecoderResult(cause);
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected DecoderResult(Throwable cause)
/* 22:   */   {
/* 23:38 */     if (cause == null) {
/* 24:39 */       throw new NullPointerException("cause");
/* 25:   */     }
/* 26:41 */     this.cause = cause;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public boolean isFinished()
/* 30:   */   {
/* 31:45 */     return this.cause != SIGNAL_UNFINISHED;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public boolean isSuccess()
/* 35:   */   {
/* 36:49 */     return this.cause == SIGNAL_SUCCESS;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public boolean isFailure()
/* 40:   */   {
/* 41:53 */     return (this.cause != SIGNAL_SUCCESS) && (this.cause != SIGNAL_UNFINISHED);
/* 42:   */   }
/* 43:   */   
/* 44:   */   public Throwable cause()
/* 45:   */   {
/* 46:57 */     if (isFailure()) {
/* 47:58 */       return this.cause;
/* 48:   */     }
/* 49:60 */     return null;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public String toString()
/* 53:   */   {
/* 54:66 */     if (isFinished())
/* 55:   */     {
/* 56:67 */       if (isSuccess()) {
/* 57:68 */         return "success";
/* 58:   */       }
/* 59:71 */       String cause = cause().toString();
/* 60:72 */       return cause.length() + 17 + "failure(" + 
/* 61:73 */         cause + 
/* 62:74 */         ')';
/* 63:   */     }
/* 64:78 */     return "unfinished";
/* 65:   */   }
/* 66:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DecoderResult
 * JD-Core Version:    0.7.0.1
 */