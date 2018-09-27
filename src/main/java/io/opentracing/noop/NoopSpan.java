/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.Span;
/*  4:   */ 
/*  5:   */ public abstract interface NoopSpan
/*  6:   */   extends Span
/*  7:   */ {
/*  8:22 */   public static final NoopSpan INSTANCE = new NoopSpanImpl();
/*  9:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopSpan
 * JD-Core Version:    0.7.0.1
 */