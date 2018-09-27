/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.Scope;
/*  4:   */ import io.opentracing.Span;
/*  5:   */ import io.opentracing.SpanContext;
/*  6:   */ import io.opentracing.Tracer.SpanBuilder;
/*  7:   */ import java.util.Collections;
/*  8:   */ import java.util.Map;
/*  9:   */ import java.util.Map.Entry;
/* 10:   */ 
/* 11:   */ final class NoopSpanBuilderImpl
/* 12:   */   implements NoopSpanBuilder
/* 13:   */ {
/* 14:   */   public Tracer.SpanBuilder addReference(String refType, SpanContext referenced)
/* 15:   */   {
/* 16:32 */     return this;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public Tracer.SpanBuilder asChildOf(SpanContext parent)
/* 20:   */   {
/* 21:37 */     return this;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public Tracer.SpanBuilder ignoreActiveSpan()
/* 25:   */   {
/* 26:41 */     return this;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public Tracer.SpanBuilder asChildOf(Span parent)
/* 30:   */   {
/* 31:45 */     return this;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public Tracer.SpanBuilder withTag(String key, String value)
/* 35:   */   {
/* 36:50 */     return this;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public Tracer.SpanBuilder withTag(String key, boolean value)
/* 40:   */   {
/* 41:55 */     return this;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public Tracer.SpanBuilder withTag(String key, Number value)
/* 45:   */   {
/* 46:60 */     return this;
/* 47:   */   }
/* 48:   */   
/* 49:   */   public Tracer.SpanBuilder withStartTimestamp(long microseconds)
/* 50:   */   {
/* 51:65 */     return this;
/* 52:   */   }
/* 53:   */   
/* 54:   */   public Scope startActive(boolean finishOnClose)
/* 55:   */   {
/* 56:70 */     return NoopScopeManager.NoopScope.INSTANCE;
/* 57:   */   }
/* 58:   */   
/* 59:   */   public Span start()
/* 60:   */   {
/* 61:75 */     return startManual();
/* 62:   */   }
/* 63:   */   
/* 64:   */   public Span startManual()
/* 65:   */   {
/* 66:80 */     return NoopSpanImpl.INSTANCE;
/* 67:   */   }
/* 68:   */   
/* 69:   */   public Iterable<Map.Entry<String, String>> baggageItems()
/* 70:   */   {
/* 71:85 */     return Collections.emptyMap().entrySet();
/* 72:   */   }
/* 73:   */   
/* 74:   */   public String toString()
/* 75:   */   {
/* 76:89 */     return NoopSpanBuilder.class.getSimpleName();
/* 77:   */   }
/* 78:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopSpanBuilderImpl
 * JD-Core Version:    0.7.0.1
 */