package org.jupiter.rpc.consumer.processor;

import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.executor.CallerRunsExecutorFactory;
import org.jupiter.rpc.executor.CloseableExecutor;
import org.jupiter.rpc.executor.ExecutorFactory;
import org.jupiter.rpc.executor.ExecutorFactory.Target;























public class ConsumerExecutors
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConsumerExecutors.class);
  private static final CloseableExecutor executor;
  
  static
  {
    String factoryName = SystemPropertyUtil.get("jupiter.executor.factory.consumer.factory_name", "callerRuns");
    ExecutorFactory factory;
    try {
      factory = (ExecutorFactory)JServiceLoader.load(ConsumerExecutorFactory.class).find(factoryName);
    } catch (Throwable t) {
      ExecutorFactory factory;
      logger.warn("Failed to load consumer's executor factory [{}], cause: {}, [CallerRunsExecutorFactory] will be used as default.", factoryName, StackTraceUtil.stackTrace(t));
      

      factory = new CallerRunsExecutorFactory();
    }
    
    executor = factory.newExecutor(ExecutorFactory.Target.CONSUMER, "jupiter-consumer-processor");
  }
  
  public static CloseableExecutor executor() {
    return executor;
  }
  
  public static void execute(Runnable r) {
    executor.execute(r);
  }
  
  public ConsumerExecutors() {}
}
