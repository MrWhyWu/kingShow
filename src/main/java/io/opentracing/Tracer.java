package io.opentracing;

import io.opentracing.propagation.Format;

public abstract interface Tracer
{
  public abstract ScopeManager scopeManager();
  
  public abstract Span activeSpan();
  
  public abstract SpanBuilder buildSpan(String paramString);
  
  public abstract <C> void inject(SpanContext paramSpanContext, Format<C> paramFormat, C paramC);
  
  public abstract <C> SpanContext extract(Format<C> paramFormat, C paramC);
  
  public static abstract interface SpanBuilder
  {
    public abstract SpanBuilder asChildOf(SpanContext paramSpanContext);
    
    public abstract SpanBuilder asChildOf(Span paramSpan);
    
    public abstract SpanBuilder addReference(String paramString, SpanContext paramSpanContext);
    
    public abstract SpanBuilder ignoreActiveSpan();
    
    public abstract SpanBuilder withTag(String paramString1, String paramString2);
    
    public abstract SpanBuilder withTag(String paramString, boolean paramBoolean);
    
    public abstract SpanBuilder withTag(String paramString, Number paramNumber);
    
    public abstract SpanBuilder withStartTimestamp(long paramLong);
    
    public abstract Scope startActive(boolean paramBoolean);
    
    @Deprecated
    public abstract Span startManual();
    
    public abstract Span start();
  }
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.Tracer
 * JD-Core Version:    0.7.0.1
 */