package org.jupiter.rpc.executor;

import org.jupiter.rpc.consumer.processor.ConsumerExecutorFactory;
import org.jupiter.rpc.provider.processor.ProviderExecutorFactory;









public abstract interface ExecutorFactory
  extends ConsumerExecutorFactory, ProviderExecutorFactory
{
  public static final String CONSUMER_EXECUTOR_CORE_WORKERS = "jupiter.executor.factory.consumer.core.workers";
  public static final String PROVIDER_EXECUTOR_CORE_WORKERS = "jupiter.executor.factory.provider.core.workers";
  public static final String CONSUMER_EXECUTOR_MAX_WORKERS = "jupiter.executor.factory.consumer.max.workers";
  public static final String PROVIDER_EXECUTOR_MAX_WORKERS = "jupiter.executor.factory.provider.max.workers";
  public static final String CONSUMER_EXECUTOR_QUEUE_TYPE = "jupiter.executor.factory.consumer.queue.type";
  public static final String PROVIDER_EXECUTOR_QUEUE_TYPE = "jupiter.executor.factory.provider.queue.type";
  public static final String CONSUMER_EXECUTOR_QUEUE_CAPACITY = "jupiter.executor.factory.consumer.queue.capacity";
  public static final String PROVIDER_EXECUTOR_QUEUE_CAPACITY = "jupiter.executor.factory.provider.queue.capacity";
  public static final String CONSUMER_DISRUPTOR_WAIT_STRATEGY_TYPE = "jupiter.executor.factory.consumer.disruptor.wait.strategy.type";
  public static final String PROVIDER_DISRUPTOR_WAIT_STRATEGY_TYPE = "jupiter.executor.factory.provider.disruptor.wait.strategy.type";
  public static final String CONSUMER_THREAD_POOL_REJECTED_HANDLER = "jupiter.executor.factory.consumer.thread.pool.rejected.handler";
  public static final String PROVIDER_THREAD_POOL_REJECTED_HANDLER = "jupiter.executor.factory.provider.thread.pool.rejected.handler";
  public static final String EXECUTOR_AFFINITY_THREAD = "jupiter.executor.factory.affinity.thread";
  
  public abstract CloseableExecutor newExecutor(Target paramTarget, String paramString);
  
  public static enum Target
  {
    CONSUMER, 
    PROVIDER;
    
    private Target() {}
  }
}
