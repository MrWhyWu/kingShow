package org.jupiter.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityStrategy;
import org.jupiter.common.util.ClassUtil;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.internal.InternalThread;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;




























public class AffinityNamedThreadFactory
  implements ThreadFactory
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AffinityNamedThreadFactory.class);
  
  static
  {
    ClassUtil.checkClass("org.slf4j.Logger", "Class[" + AffinityNamedThreadFactory.class.getName() + "] must rely on SL4J");
  }
  

  private final AtomicInteger id = new AtomicInteger();
  private final String name;
  private final boolean daemon;
  private final int priority;
  private final ThreadGroup group;
  private final AffinityStrategy[] strategies;
  private AffinityLock lastAffinityLock = null;
  
  public AffinityNamedThreadFactory(String name, AffinityStrategy... strategies) {
    this(name, false, 5, strategies);
  }
  
  public AffinityNamedThreadFactory(String name, boolean daemon, AffinityStrategy... strategies) {
    this(name, daemon, 5, strategies);
  }
  
  public AffinityNamedThreadFactory(String name, int priority, AffinityStrategy... strategies) {
    this(name, false, priority, strategies);
  }
  
  public AffinityNamedThreadFactory(String name, boolean daemon, int priority, AffinityStrategy... strategies) {
    this.name = ("affinity." + name + " #");
    this.daemon = daemon;
    this.priority = priority;
    SecurityManager s = System.getSecurityManager();
    group = (s == null ? Thread.currentThread().getThreadGroup() : s.getThreadGroup());
    this.strategies = (strategies.length == 0 ? new AffinityStrategy[] { AffinityStrategies.ANY } : strategies);
  }
  
  public Thread newThread(Runnable r)
  {
    Preconditions.checkNotNull(r, "runnable");
    
    String name2 = name + id.getAndIncrement();
    
    final Runnable r2 = wrapRunnable(r);
    
    Runnable r3 = new Runnable()
    {
      public void run()
      {
        AffinityLock al = AffinityNamedThreadFactory.this.acquireLockBasedOnLast();
        try {
          r2.run();
        } finally {
          al.release();
        }
        
      }
    };
    Thread t = wrapThread(group, r3, name2);
    try
    {
      if (t.isDaemon() != daemon) {
        t.setDaemon(daemon);
      }
      
      if (t.getPriority() != priority) {
        t.setPriority(priority);
      }
    }
    catch (Exception localException) {}
    logger.info("Creates new {}.", t);
    
    return t;
  }
  
  public ThreadGroup getThreadGroup() {
    return group;
  }
  
  protected Runnable wrapRunnable(Runnable r) {
    return r;
  }
  
  protected Thread wrapThread(ThreadGroup group, Runnable r, String name) {
    return new InternalThread(group, r, name);
  }
  
  private synchronized AffinityLock acquireLockBasedOnLast() {
    AffinityLock al = lastAffinityLock == null ? AffinityLock.acquireLock() : lastAffinityLock.acquireLock(strategies);
    if (al.cpuId() >= 0) {
      if (!al.isBound()) {
        al.bind();
      }
      lastAffinityLock = al;
    }
    return al;
  }
}
