package org.jupiter.common.concurrent.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.TimeoutHandler;
import com.lmax.disruptor.WorkHandler;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;

























public class TaskHandler
  implements EventHandler<MessageEvent<Runnable>>, WorkHandler<MessageEvent<Runnable>>, TimeoutHandler, LifecycleAware
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(TaskHandler.class);
  
  public TaskHandler() {}
  
  public void onEvent(MessageEvent<Runnable> event, long sequence, boolean endOfBatch) throws Exception { ((Runnable)event.getMessage()).run(); }
  
  public void onEvent(MessageEvent<Runnable> event)
    throws Exception
  {
    ((Runnable)event.getMessage()).run();
  }
  
  public void onTimeout(long sequence) throws Exception
  {
    if (logger.isWarnEnabled()) {
      logger.warn("Task timeout on: {}, sequence: {}.", Thread.currentThread().getName(), Long.valueOf(sequence));
    }
  }
  
  public void onStart()
  {
    if (logger.isWarnEnabled()) {
      logger.warn("Task handler on start: {}.", Thread.currentThread().getName());
    }
  }
  
  public void onShutdown()
  {
    if (logger.isWarnEnabled()) {
      logger.warn("Task handler on shutdown: {}.", Thread.currentThread().getName());
    }
  }
}
