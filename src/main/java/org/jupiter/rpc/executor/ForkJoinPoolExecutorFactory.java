package org.jupiter.rpc.executor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import org.jupiter.common.util.SpiMetadata;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.InternalForkJoinWorkerThread;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;

























@SpiMetadata(name="forkJoin")
public class ForkJoinPoolExecutorFactory
  extends AbstractExecutorFactory
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ForkJoinPoolExecutorFactory.class);
  
  public ForkJoinPoolExecutorFactory() {}
  
  public CloseableExecutor newExecutor(ExecutorFactory.Target target, String name) { final ForkJoinPool executor = new ForkJoinPool(coreWorkers(target), new DefaultForkJoinWorkerThreadFactory(name), new DefaultUncaughtExceptionHandler(null), true);
    



    new CloseableExecutor()
    {
      public void execute(Runnable r)
      {
        executor.execute(r);
      }
      
      public void shutdown()
      {
        ForkJoinPoolExecutorFactory.logger.warn("ForkJoinPoolExecutorFactory#{} shutdown.", executor);
        executor.shutdownNow();
      }
    };
  }
  
  private static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory
  {
    private final AtomicInteger idx = new AtomicInteger();
    private final String namePrefix;
    
    public DefaultForkJoinWorkerThreadFactory(String namePrefix) {
      this.namePrefix = namePrefix;
    }
    

    public ForkJoinWorkerThread newThread(ForkJoinPool pool)
    {
      ForkJoinWorkerThread thread = new InternalForkJoinWorkerThread(pool);
      thread.setName(namePrefix + '-' + idx.getAndIncrement());
      return thread;
    }
  }
  
  private static final class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private DefaultUncaughtExceptionHandler() {}
    
    public void uncaughtException(Thread t, Throwable e) {
      ForkJoinPoolExecutorFactory.logger.error("Uncaught exception in thread[{}], {}.", t.getName(), StackTraceUtil.stackTrace(e));
    }
  }
}
