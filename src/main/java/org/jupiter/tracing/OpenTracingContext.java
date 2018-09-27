package org.jupiter.tracing;

import io.opentracing.Span;
import io.opentracing.Tracer;























public class OpenTracingContext
{
  private static final ThreadLocal<Span> spanThreadLocal = new ThreadLocal();
  

  private static TracerFactory tracerFactory = TracerFactory.DEFAULT;
  
  public OpenTracingContext() {}
  
  public static void setTracerFactory(TracerFactory tracerFactory)
  {
    tracerFactory = tracerFactory;
  }
  
  public static Tracer getTracer() {
    return tracerFactory.getTracer();
  }
  
  public static Span getActiveSpan() {
    return (Span)spanThreadLocal.get();
  }
  
  public static void setActiveSpan(Span span) {
    spanThreadLocal.set(span);
  }
}
