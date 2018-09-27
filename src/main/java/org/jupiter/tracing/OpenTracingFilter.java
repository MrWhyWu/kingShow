package org.jupiter.tracing;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.noop.NoopTracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import java.util.Iterator;
import java.util.Map.Entry;
import org.jupiter.common.util.SpiMetadata;
import org.jupiter.rpc.JFilter;
import org.jupiter.rpc.JFilter.Type;
import org.jupiter.rpc.JFilterChain;
import org.jupiter.rpc.JFilterContext;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.model.metadata.MessageWrapper;


























@SpiMetadata(name="openTracing", priority=10)
public class OpenTracingFilter
  implements JFilter
{
  public OpenTracingFilter() {}
  
  public JFilter.Type getType()
  {
    return JFilter.Type.ALL;
  }
  
  public <T extends JFilterContext> void doFilter(JRequest request, T filterCtx, JFilterChain next) throws Throwable
  {
    Tracer tracer = OpenTracingContext.getTracer();
    if ((tracer == null) || ((tracer instanceof NoopTracer))) {
      next.doFilter(request, filterCtx);
      return;
    }
    
    JFilter.Type filterCtxType = filterCtx.getType();
    if (filterCtxType == JFilter.Type.PROVIDER) {
      processProviderTracing(tracer, request, filterCtx, next);
    } else if (filterCtxType == JFilter.Type.CONSUMER) {
      processConsumerTracing(tracer, request, filterCtx, next);
    } else {
      throw new IllegalArgumentException("Illegal filter context type: " + filterCtxType);
    }
  }
  
  private <T extends JFilterContext> void processProviderTracing(Tracer tracer, JRequest request, T filterCtx, JFilterChain next) throws Throwable
  {
    Span span = extractContext(tracer, request);
    try {
      OpenTracingContext.setActiveSpan(span);
      span.setTag("jupiter_traceId", request.getTraceId());
      

      next.doFilter(request, filterCtx);
      
      span.log("request success.");
    } catch (Throwable t) {
      span.log("request fail. " + t.getMessage());
      throw t;
    } finally {
      span.finish();
    }
  }
  
  private <T extends JFilterContext> void processConsumerTracing(Tracer tracer, JRequest request, T filterCtx, JFilterChain next) throws Throwable
  {
    MessageWrapper msg = request.message();
    Tracer.SpanBuilder spanBuilder = tracer.buildSpan(msg != null ? msg.getOperationName() : "null");
    Span activeSpan = OpenTracingContext.getActiveSpan();
    if (activeSpan != null) {
      spanBuilder.asChildOf(activeSpan);
    }
    
    Span span = spanBuilder.start();
    try {
      span.setTag("jupiter_traceId", request.getTraceId());
      injectContext(tracer, span, request);
      

      next.doFilter(request, filterCtx);
      
      span.log("request success.");
    } catch (Throwable t) {
      span.log("request fail. " + t.getMessage());
      throw t;
    } finally {
      span.finish();
    }
  }
  
  private void injectContext(Tracer tracer, Span span, final JRequest request) {
    tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new TextMap()
    {
      public Iterator<Map.Entry<String, String>> iterator()
      {
        throw new UnsupportedOperationException("iterator");
      }
      
      public void put(String key, String value)
      {
        request.putAttachment(key, value);
      }
    });
  }
  
  private Span extractContext(Tracer tracer, JRequest request) {
    MessageWrapper msg = request.message();
    Tracer.SpanBuilder spanBuilder = tracer.buildSpan(msg != null ? msg.getOperationName() : "null");
    try {
      SpanContext spanContext = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(request.getAttachments()));
      
      if (spanContext != null) {
        spanBuilder.asChildOf(spanContext);
      }
    } catch (Throwable t) {
      spanBuilder.withTag("Error", "extract from request failed: " + t.getMessage());
    }
    return spanBuilder.start();
  }
}
