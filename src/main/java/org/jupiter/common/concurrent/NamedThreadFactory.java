package org.jupiter.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.internal.InternalThread;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;


























public class NamedThreadFactory
  implements ThreadFactory
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NamedThreadFactory.class);
  
  private final AtomicInteger id = new AtomicInteger();
  private final String name;
  private final boolean daemon;
  private final int priority;
  private final ThreadGroup group;
  
  public NamedThreadFactory(String name) {
    this(name, false, 5);
  }
  
  public NamedThreadFactory(String name, boolean daemon) {
    this(name, daemon, 5);
  }
  
  public NamedThreadFactory(String name, int priority) {
    this(name, false, priority);
  }
  
  public NamedThreadFactory(String name, boolean daemon, int priority) {
    this.name = (name + " #");
    this.daemon = daemon;
    this.priority = priority;
    SecurityManager s = System.getSecurityManager();
    group = (s == null ? Thread.currentThread().getThreadGroup() : s.getThreadGroup());
  }
  
  public Thread newThread(Runnable r)
  {
    Preconditions.checkNotNull(r, "runnable");
    
    String name2 = name + id.getAndIncrement();
    
    Runnable r2 = wrapRunnable(r);
    
    Thread t = wrapThread(group, r2, name2);
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
}
