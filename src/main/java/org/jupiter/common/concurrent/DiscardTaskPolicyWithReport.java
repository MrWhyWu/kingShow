package org.jupiter.common.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.jupiter.common.util.internal.logging.InternalLogger;






















public class DiscardTaskPolicyWithReport
  extends AbstractRejectedExecutionHandler
{
  public DiscardTaskPolicyWithReport(String threadPoolName)
  {
    super(threadPoolName, false, "");
  }
  
  public DiscardTaskPolicyWithReport(String threadPoolName, String dumpPrefixName) {
    super(threadPoolName, true, dumpPrefixName);
  }
  
  public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
  {
    logger.error("Thread pool [{}] is exhausted! {}.", threadPoolName, e.toString());
    
    dumpJvmInfo();
    
    if (!e.isShutdown()) {
      BlockingQueue<Runnable> queue = e.getQueue();
      int discardSize = queue.size() >> 1;
      for (int i = 0; i < discardSize; i++) {
        queue.poll();
      }
      queue.offer(r);
    }
  }
}
