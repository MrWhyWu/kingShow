package org.jupiter.rpc.executor;

import java.util.concurrent.ThreadFactory;
import net.openhft.affinity.AffinityStrategy;
import org.jupiter.common.concurrent.AffinityNamedThreadFactory;
import org.jupiter.common.concurrent.NamedThreadFactory;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.SystemPropertyUtil;



















public abstract class AbstractExecutorFactory
  implements ExecutorFactory
{
  public AbstractExecutorFactory() {}
  
  protected ThreadFactory threadFactory(String name)
  {
    boolean affinity = SystemPropertyUtil.getBoolean("jupiter.executor.factory.affinity.thread", false);
    if (affinity) {
      return new AffinityNamedThreadFactory(name, new AffinityStrategy[0]);
    }
    return new NamedThreadFactory(name);
  }
  
  protected int coreWorkers(ExecutorFactory.Target target)
  {
    switch (1.$SwitchMap$org$jupiter$rpc$executor$ExecutorFactory$Target[target.ordinal()]) {
    case 1: 
      return SystemPropertyUtil.getInt("jupiter.executor.factory.consumer.core.workers", JConstants.AVAILABLE_PROCESSORS << 1);
    case 2: 
      return SystemPropertyUtil.getInt("jupiter.executor.factory.provider.core.workers", JConstants.AVAILABLE_PROCESSORS << 1);
    }
    throw new IllegalArgumentException(String.valueOf(target));
  }
  
  protected int maxWorkers(ExecutorFactory.Target target)
  {
    switch (1.$SwitchMap$org$jupiter$rpc$executor$ExecutorFactory$Target[target.ordinal()]) {
    case 1: 
      return SystemPropertyUtil.getInt("jupiter.executor.factory.consumer.max.workers", 32);
    case 2: 
      return SystemPropertyUtil.getInt("jupiter.executor.factory.provider.max.workers", 512);
    }
    throw new IllegalArgumentException(String.valueOf(target));
  }
  
  protected int queueCapacity(ExecutorFactory.Target target)
  {
    switch (1.$SwitchMap$org$jupiter$rpc$executor$ExecutorFactory$Target[target.ordinal()]) {
    case 1: 
      return SystemPropertyUtil.getInt("jupiter.executor.factory.consumer.queue.capacity", 32768);
    case 2: 
      return SystemPropertyUtil.getInt("jupiter.executor.factory.provider.queue.capacity", 32768);
    }
    throw new IllegalArgumentException(String.valueOf(target));
  }
}
