package org.jupiter.common.concurrent.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.concurrent.NamedThreadFactory;
import org.jupiter.common.concurrent.RejectedTaskPolicyWithReport;
import org.jupiter.common.util.Pow2;
import org.jupiter.common.util.Preconditions;






































public class TaskDispatcher
  implements Dispatcher<Runnable>, Executor
{
  private static final EventFactory<MessageEvent<Runnable>> eventFactory = new EventFactory()
  {
    public MessageEvent<Runnable> newInstance()
    {
      return new MessageEvent();
    }
  };
  private final Disruptor<MessageEvent<Runnable>> disruptor;
  private final ExecutorService reserveExecutor;
  
  public TaskDispatcher(int numWorkers, ThreadFactory threadFactory)
  {
    this(numWorkers, threadFactory, 32768, 0, WaitStrategyType.BLOCKING_WAIT, null);
  }
  





  public TaskDispatcher(int numWorkers, ThreadFactory threadFactory, int bufSize, int numReserveWorkers, WaitStrategyType waitStrategyType, String dumpPrefixName)
  {
    Preconditions.checkArgument(bufSize > 0, "bufSize must be larger than 0");
    if (!Pow2.isPowerOfTwo(bufSize)) {
      bufSize = Pow2.roundToPowerOfTwo(bufSize);
    }
    
    if (numReserveWorkers > 0) {
      String name = "reserve.processor";
      RejectedExecutionHandler handler;
      RejectedExecutionHandler handler;
      if (dumpPrefixName == null) {
        handler = new RejectedTaskPolicyWithReport(name);
      } else {
        handler = new RejectedTaskPolicyWithReport(name, dumpPrefixName);
      }
      
      reserveExecutor = new ThreadPoolExecutor(0, numReserveWorkers, 60L, TimeUnit.SECONDS, new SynchronousQueue(), new NamedThreadFactory(name), handler);



    }
    else
    {


      reserveExecutor = null; }
    WaitStrategy waitStrategy;
    WaitStrategy waitStrategy;
    WaitStrategy waitStrategy;
    WaitStrategy waitStrategy; WaitStrategy waitStrategy; WaitStrategy waitStrategy; WaitStrategy waitStrategy; WaitStrategy waitStrategy; switch (2.$SwitchMap$org$jupiter$common$concurrent$disruptor$WaitStrategyType[waitStrategyType.ordinal()]) {
    case 1: 
      waitStrategy = new BlockingWaitStrategy();
      break;
    case 2: 
      waitStrategy = new LiteBlockingWaitStrategy();
      break;
    case 3: 
      waitStrategy = new TimeoutBlockingWaitStrategy(1000L, TimeUnit.MILLISECONDS);
      break;
    case 4: 
      waitStrategy = new LiteTimeoutBlockingWaitStrategy(1000L, TimeUnit.MILLISECONDS);
      break;
    case 5: 
      waitStrategy = PhasedBackoffWaitStrategy.withLiteLock(1000L, 1000L, TimeUnit.NANOSECONDS);
      break;
    case 6: 
      waitStrategy = new SleepingWaitStrategy();
      break;
    case 7: 
      waitStrategy = new YieldingWaitStrategy();
      break;
    case 8: 
      waitStrategy = new BusySpinWaitStrategy();
      break;
    default: 
      throw new UnsupportedOperationException(waitStrategyType.toString());
    }
    WaitStrategy waitStrategy;
    if (threadFactory == null) {
      threadFactory = new NamedThreadFactory("disruptor.processor");
    }
    Disruptor<MessageEvent<Runnable>> dr = new Disruptor(eventFactory, bufSize, threadFactory, ProducerType.MULTI, waitStrategy);
    
    dr.setDefaultExceptionHandler(new LoggingExceptionHandler());
    numWorkers = Math.min(Math.abs(numWorkers), MAX_NUM_WORKERS);
    if (numWorkers == 1) {
      dr.handleEventsWith(new EventHandler[] { new TaskHandler() });
    } else {
      TaskHandler[] handlers = new TaskHandler[numWorkers];
      for (int i = 0; i < numWorkers; i++) {
        handlers[i] = new TaskHandler();
      }
      dr.handleEventsWithWorkerPool(handlers);
    }
    
    dr.start();
    disruptor = dr;
  }
  
  public boolean dispatch(Runnable message)
  {
    RingBuffer<MessageEvent<Runnable>> ringBuffer = disruptor.getRingBuffer();
    try {
      long sequence = ringBuffer.tryNext();
      try {
        MessageEvent<Runnable> event = (MessageEvent)ringBuffer.get(sequence);
        event.setMessage(message);
      } finally {
        ringBuffer.publish(sequence);
      }
      return true;
    }
    catch (InsufficientCapacityException e) {}
    return false;
  }
  

  public void execute(Runnable message)
  {
    if (!dispatch(message))
    {
      if (reserveExecutor != null) {
        reserveExecutor.execute(message);
      } else {
        throw new RejectedExecutionException("Ring buffer is full");
      }
    }
  }
  
  public void shutdown()
  {
    disruptor.shutdown();
    if (reserveExecutor != null) {
      reserveExecutor.shutdownNow();
    }
  }
}
