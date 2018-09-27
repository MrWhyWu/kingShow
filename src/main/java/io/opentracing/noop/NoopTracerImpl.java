/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.ScopeManager;
/*  4:   */ import io.opentracing.Span;
/*  5:   */ import io.opentracing.SpanContext;
/*  6:   */ import io.opentracing.Tracer.SpanBuilder;
/*  7:   */ import io.opentracing.propagation.Format;
/*  8:   */ 
/*  9:   */ final class NoopTracerImpl
/* 10:   */   implements NoopTracer
/* 11:   */ {
/* 12:26 */   static final NoopTracer INSTANCE = new NoopTracerImpl();
/* 13:   */   
/* 14:   */   public ScopeManager scopeManager()
/* 15:   */   {
/* 16:30 */     return NoopScopeManager.INSTANCE;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public Span activeSpan()
/* 20:   */   {
/* 21:35 */     return null;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public Tracer.SpanBuilder buildSpan(String operationName)
/* 25:   */   {
/* 26:39 */     return NoopSpanBuilderImpl.INSTANCE;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {}
/* 30:   */   
/* 31:   */   public <C> SpanContext extract(Format<C> format, C carrier)
/* 32:   */   {
/* 33:45 */     return NoopSpanBuilderImpl.INSTANCE;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public String toString()
/* 37:   */   {
/* 38:48 */     return NoopTracer.class.getSimpleName();
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopTracerImpl
 * JD-Core Version:    0.7.0.1
 */