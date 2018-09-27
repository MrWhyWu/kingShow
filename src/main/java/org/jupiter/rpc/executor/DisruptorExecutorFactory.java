package org.jupiter.rpc.executor;

import org.jupiter.common.concurrent.disruptor.TaskDispatcher;
import org.jupiter.common.concurrent.disruptor.WaitStrategyType;
import org.jupiter.common.util.SpiMetadata;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
























@SpiMetadata(name="disruptor")
public class DisruptorExecutorFactory
  extends AbstractExecutorFactory
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DisruptorExecutorFactory.class);
  
  public DisruptorExecutorFactory() {}
  
  public CloseableExecutor newExecutor(ExecutorFactory.Target target, String name) { final TaskDispatcher executor = new TaskDispatcher(coreWorkers(target), threadFactory(name), queueCapacity(target), maxWorkers(target), waitStrategyType(target, WaitStrategyType.LITE_BLOCKING_WAIT), "jupiter");
    






    new CloseableExecutor()
    {
      public void execute(Runnable r)
      {
        executor.execute(r);
      }
      
      public void shutdown()
      {
        DisruptorExecutorFactory.logger.warn("DisruptorExecutorFactory#{} shutdown.", executor);
        executor.shutdown();
      }
    };
  }
  
  private WaitStrategyType waitStrategyType(ExecutorFactory.Target target, WaitStrategyType defaultType) {
    WaitStrategyType strategyType = null;
    switch (2.$SwitchMap$org$jupiter$rpc$executor$ExecutorFactory$Target[target.ordinal()]) {
    case 1: 
      strategyType = WaitStrategyType.parse(SystemPropertyUtil.get("jupiter.executor.factory.consumer.disruptor.wait.strategy.type"));
      break;
    case 2: 
      strategyType = WaitStrategyType.parse(SystemPropertyUtil.get("jupiter.executor.factory.provider.disruptor.wait.strategy.type"));
    }
    
    
    return strategyType == null ? defaultType : strategyType;
  }
}
