package org.jupiter.common.concurrent;

import java.util.concurrent.ThreadPoolExecutor;
import org.jupiter.common.util.internal.logging.InternalLogger;






















public class CallerRunsPolicyWithReport
  extends AbstractRejectedExecutionHandler
{
  public CallerRunsPolicyWithReport(String threadPoolName)
  {
    super(threadPoolName, false, "");
  }
  
  public CallerRunsPolicyWithReport(String threadPoolName, String dumpPrefixName) {
    super(threadPoolName, true, dumpPrefixName);
  }
  
  public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
  {
    logger.error("Thread pool [{}] is exhausted! {}.", threadPoolName, e.toString());
    
    dumpJvmInfo();
    
    if (!e.isShutdown()) {
      r.run();
    }
  }
}
