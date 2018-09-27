package org.jupiter.tracing;

import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import java.util.Iterator;
import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;


























public abstract interface TracerFactory
{
  public static final TracerFactory DEFAULT = new DefaultTracerFactory();
  

  public abstract Tracer getTracer();
  

  public static class DefaultTracerFactory
    implements TracerFactory
  {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultTracerFactory.class);
    
    private static Tracer tracer = loadTracer();
    
    public DefaultTracerFactory() {}
    
    private static Tracer loadTracer() { try { Iterator<Tracer> implementations = JServiceLoader.load(Tracer.class).iterator();
        if (implementations.hasNext()) {
          Tracer first = (Tracer)implementations.next();
          if (!implementations.hasNext()) {
            return first;
          }
          
          logger.warn("More than one tracer is found, NoopTracer will be used as default.");
          
          return NoopTracerFactory.create();
        }
      } catch (Throwable t) {
        logger.error("Load tracer failed: {}.", StackTraceUtil.stackTrace(t));
      }
      return NoopTracerFactory.create();
    }
    
    public Tracer getTracer()
    {
      return tracer;
    }
  }
}
