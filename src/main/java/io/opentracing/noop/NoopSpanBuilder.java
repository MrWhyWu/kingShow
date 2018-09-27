/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.Tracer.SpanBuilder;
/*  4:   */ 
/*  5:   */ public abstract interface NoopSpanBuilder
/*  6:   */   extends Tracer.SpanBuilder, NoopSpanContext
/*  7:   */ {
/*  8:25 */   public static final NoopSpanBuilder INSTANCE = new NoopSpanBuilderImpl();
/*  9:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopSpanBuilder
 * JD-Core Version:    0.7.0.1
 */