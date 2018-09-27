/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.SpanContext;
/*  4:   */ import java.util.Map;
/*  5:   */ 
/*  6:   */ final class NoopSpanImpl
/*  7:   */   implements NoopSpan
/*  8:   */ {
/*  9:   */   public SpanContext context()
/* 10:   */   {
/* 11:28 */     return NoopSpanContextImpl.INSTANCE;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public void finish() {}
/* 15:   */   
/* 16:   */   public void finish(long finishMicros) {}
/* 17:   */   
/* 18:   */   public NoopSpan setTag(String key, String value)
/* 19:   */   {
/* 20:37 */     return this;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public NoopSpan setTag(String key, boolean value)
/* 24:   */   {
/* 25:40 */     return this;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public NoopSpan setTag(String key, Number value)
/* 29:   */   {
/* 30:43 */     return this;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public NoopSpan log(Map<String, ?> fields)
/* 34:   */   {
/* 35:46 */     return this;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public NoopSpan log(long timestampMicroseconds, Map<String, ?> fields)
/* 39:   */   {
/* 40:49 */     return this;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public NoopSpan log(String event)
/* 44:   */   {
/* 45:52 */     return this;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public NoopSpan log(long timestampMicroseconds, String event)
/* 49:   */   {
/* 50:55 */     return this;
/* 51:   */   }
/* 52:   */   
/* 53:   */   public NoopSpan setBaggageItem(String key, String value)
/* 54:   */   {
/* 55:58 */     return this;
/* 56:   */   }
/* 57:   */   
/* 58:   */   public String getBaggageItem(String key)
/* 59:   */   {
/* 60:61 */     return null;
/* 61:   */   }
/* 62:   */   
/* 63:   */   public NoopSpan setOperationName(String operationName)
/* 64:   */   {
/* 65:64 */     return this;
/* 66:   */   }
/* 67:   */   
/* 68:   */   public String toString()
/* 69:   */   {
/* 70:67 */     return NoopSpan.class.getSimpleName();
/* 71:   */   }
/* 72:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopSpanImpl
 * JD-Core Version:    0.7.0.1
 */