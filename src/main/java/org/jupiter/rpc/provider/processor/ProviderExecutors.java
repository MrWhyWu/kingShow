package org.jupiter.rpc.provider.processor;

import org.jupiter.common.util.JServiceLoader;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.consumer.processor.ConsumerExecutors;
import org.jupiter.rpc.executor.CloseableExecutor;
import org.jupiter.rpc.executor.ExecutorFactory;
import org.jupiter.rpc.executor.ExecutorFactory.Target;
import org.jupiter.rpc.executor.ThreadPoolExecutorFactory;























public class ProviderExecutors
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConsumerExecutors.class);
  private static final CloseableExecutor executor;
  
  static
  {
    String factoryName = SystemPropertyUtil.get("jupiter.executor.factory.provider.factory_name", "threadPool");
    ExecutorFactory factory;
    try {
      factory = (ExecutorFactory)JServiceLoader.load(ProviderExecutorFactory.class).find(factoryName);
    } catch (Throwable t) {
      ExecutorFactory factory;
      logger.warn("Failed to load provider's executor factory [{}], cause: {}, [ThreadPoolExecutorFactory] will be used as default.", factoryName, StackTraceUtil.stackTrace(t));
      

      factory = new ThreadPoolExecutorFactory();
    }
    
    executor = factory.newExecutor(ExecutorFactory.Target.PROVIDER, "jupiter-provider-processor");
  }
  
  public static CloseableExecutor executor() {
    return executor;
  }
  
  public static void execute(Runnable r) {
    executor.execute(r);
  }
  
  public ProviderExecutors() {}
}
