package org.jupiter.common.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.jupiter.common.util.internal.logging.InternalLogger;





















public class BlockingProducersPolicyWithReport
  extends AbstractRejectedExecutionHandler
{
  public BlockingProducersPolicyWithReport(String threadPoolName)
  {
    super(threadPoolName, false, "");
  }
  
  public BlockingProducersPolicyWithReport(String threadPoolName, String dumpPrefixName) {
    super(threadPoolName, true, dumpPrefixName);
  }
  
  public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
  {
    logger.error("Thread pool [{}] is exhausted! {}.", threadPoolName, e.toString());
    
    dumpJvmInfo();
    
    if (!e.isShutdown()) {
      try {
        e.getQueue().put(r);
      }
      catch (InterruptedException localInterruptedException) {}
    }
  }
}
