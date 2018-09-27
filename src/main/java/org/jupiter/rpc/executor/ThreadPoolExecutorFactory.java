package org.jupiter.rpc.executor;

import java.lang.reflect.Constructor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.concurrent.RejectedTaskPolicyWithReport;
import org.jupiter.common.util.SpiMetadata;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;






















@SpiMetadata(name="threadPool", priority=1)
public class ThreadPoolExecutorFactory
  extends AbstractExecutorFactory
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadPoolExecutorFactory.class);
  
  public ThreadPoolExecutorFactory() {}
  
  public CloseableExecutor newExecutor(ExecutorFactory.Target target, String name) { final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreWorkers(target), maxWorkers(target), 120L, TimeUnit.SECONDS, workQueue(target), threadFactory(name), createRejectedPolicy(target, name, new RejectedTaskPolicyWithReport(name, "jupiter")));
    







    new CloseableExecutor()
    {
      public void execute(Runnable r)
      {
        executor.execute(r);
      }
      
      public void shutdown()
      {
        ThreadPoolExecutorFactory.logger.warn("ThreadPoolExecutorFactory#{} shutdown.", executor);
        executor.shutdownNow();
      }
    };
  }
  
  private BlockingQueue<Runnable> workQueue(ExecutorFactory.Target target) {
    BlockingQueue<Runnable> workQueue = null;
    WorkQueueType queueType = queueType(target, WorkQueueType.ARRAY_BLOCKING_QUEUE);
    int queueCapacity = queueCapacity(target);
    switch (queueType) {
    case LINKED_BLOCKING_QUEUE: 
      workQueue = new LinkedBlockingQueue(queueCapacity);
      break;
    case ARRAY_BLOCKING_QUEUE: 
      workQueue = new ArrayBlockingQueue(queueCapacity);
    }
    
    
    return workQueue;
  }
  
  private WorkQueueType queueType(ExecutorFactory.Target target, WorkQueueType defaultType) {
    WorkQueueType queueType = null;
    switch (target) {
    case CONSUMER: 
      queueType = WorkQueueType.parse(SystemPropertyUtil.get("jupiter.executor.factory.consumer.queue.type"));
      break;
    case PROVIDER: 
      queueType = WorkQueueType.parse(SystemPropertyUtil.get("jupiter.executor.factory.provider.queue.type"));
    }
    
    
    return queueType == null ? defaultType : queueType;
  }
  
  private RejectedExecutionHandler createRejectedPolicy(ExecutorFactory.Target target, String name, RejectedExecutionHandler defaultHandler) {
    RejectedExecutionHandler handler = null;
    String handlerClass = null;
    switch (target) {
    case CONSUMER: 
      handlerClass = SystemPropertyUtil.get("jupiter.executor.factory.consumer.thread.pool.rejected.handler");
      break;
    case PROVIDER: 
      handlerClass = SystemPropertyUtil.get("jupiter.executor.factory.provider.thread.pool.rejected.handler");
    }
    
    if (Strings.isNotBlank(handlerClass)) {
      try {
        Class<?> cls = Class.forName(handlerClass);
        try {
          Constructor<?> constructor = cls.getConstructor(new Class[] { String.class, String.class });
          handler = (RejectedExecutionHandler)constructor.newInstance(new Object[] { name, "jupiter" });
        } catch (NoSuchMethodException e) {
          handler = (RejectedExecutionHandler)cls.newInstance();
        }
      } catch (Exception e) {
        if (logger.isWarnEnabled()) {
          logger.warn("Construct {} failed, {}.", handlerClass, StackTraceUtil.stackTrace(e));
        }
      }
    }
    
    return handler == null ? defaultHandler : handler;
  }
  
  static enum WorkQueueType {
    LINKED_BLOCKING_QUEUE, 
    ARRAY_BLOCKING_QUEUE;
    
    private WorkQueueType() {}
    static WorkQueueType parse(String name) { for (WorkQueueType type : ) {
        if (type.name().equalsIgnoreCase(name)) {
          return type;
        }
      }
      return null;
    }
  }
}
