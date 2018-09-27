package org.jupiter.common.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.jupiter.common.util.internal.logging.InternalLogger;























public class RejectedTaskPolicyWithReport
  extends AbstractRejectedExecutionHandler
{
  public RejectedTaskPolicyWithReport(String threadPoolName)
  {
    super(threadPoolName, false, "");
  }
  
  public RejectedTaskPolicyWithReport(String threadPoolName, String dumpPrefixName) {
    super(threadPoolName, true, dumpPrefixName);
  }
  
  public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
  {
    logger.error("Thread pool [{}] is exhausted! {}.", threadPoolName, e.toString());
    
    dumpJvmInfo();
    
    if ((r instanceof RejectedRunnable)) {
      ((RejectedRunnable)r).rejected();
    }
    else if (!e.isShutdown()) {
      BlockingQueue<Runnable> queue = e.getQueue();
      int discardSize = queue.size() >> 1;
      for (int i = 0; i < discardSize; i++) {
        queue.poll();
      }
      try
      {
        queue.put(r);
      }
      catch (InterruptedException localInterruptedException) {}
    }
  }
}
