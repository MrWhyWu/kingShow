package org.jupiter.rpc.executor;

import org.jupiter.common.util.SpiMetadata;




















@SpiMetadata(name="callerRuns")
public class CallerRunsExecutorFactory
  extends AbstractExecutorFactory
{
  public CallerRunsExecutorFactory() {}
  
  public CloseableExecutor newExecutor(ExecutorFactory.Target target, String name)
  {
    new CloseableExecutor()
    {
      public void execute(Runnable r)
      {
        r.run();
      }
      
      public void shutdown() {}
    };
  }
}
