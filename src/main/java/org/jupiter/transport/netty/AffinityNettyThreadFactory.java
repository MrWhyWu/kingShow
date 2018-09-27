package org.jupiter.transport.netty;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import net.openhft.affinity.AffinityStrategy;
import org.jupiter.common.concurrent.AffinityNamedThreadFactory;

























public class AffinityNettyThreadFactory
  extends AffinityNamedThreadFactory
{
  public AffinityNettyThreadFactory(String name, AffinityStrategy... strategies)
  {
    this(name, false, 5, strategies);
  }
  
  public AffinityNettyThreadFactory(String name, boolean daemon, AffinityStrategy... strategies) {
    this(name, daemon, 5, strategies);
  }
  
  public AffinityNettyThreadFactory(String name, int priority, AffinityStrategy... strategies) {
    this(name, false, priority, strategies);
  }
  
  public AffinityNettyThreadFactory(String name, boolean daemon, int priority, AffinityStrategy... strategies) {
    super(name, daemon, priority, strategies);
  }
  
  protected Runnable wrapRunnable(Runnable r)
  {
    return new DefaultRunnableDecorator(r);
  }
  
  protected Thread wrapThread(ThreadGroup group, Runnable r, String name)
  {
    return new FastThreadLocalThread(group, r, name);
  }
  
  private static final class DefaultRunnableDecorator implements Runnable
  {
    private final Runnable r;
    
    DefaultRunnableDecorator(Runnable r) {
      this.r = r;
    }
    
    public void run()
    {
      try {
        r.run();
      } finally {
        FastThreadLocal.removeAll();
      }
    }
  }
}
