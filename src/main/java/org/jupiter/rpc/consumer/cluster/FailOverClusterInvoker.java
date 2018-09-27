package org.jupiter.rpc.consumer.cluster;

import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Reflects;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.JListener;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.dispatcher.DefaultRoundDispatcher;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.future.DefaultInvokeFuture;
import org.jupiter.rpc.consumer.future.FailOverInvokeFuture;
import org.jupiter.rpc.consumer.future.InvokeFuture;
import org.jupiter.rpc.model.metadata.MessageWrapper;
import org.jupiter.transport.channel.JChannel;
































public class FailOverClusterInvoker
  implements ClusterInvoker
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(FailOverClusterInvoker.class);
  private final Dispatcher dispatcher;
  private final int retries;
  
  public FailOverClusterInvoker(Dispatcher dispatcher, int retries)
  {
    Preconditions.checkArgument(dispatcher instanceof DefaultRoundDispatcher, Reflects.simpleClassName(dispatcher) + " is unsupported [FailOverClusterInvoker]");
    



    this.dispatcher = dispatcher;
    if (retries >= 0) {
      this.retries = retries;
    } else {
      this.retries = 2;
    }
  }
  
  public ClusterInvoker.Strategy strategy()
  {
    return ClusterInvoker.Strategy.FAIL_OVER;
  }
  
  public <T> InvokeFuture<T> invoke(JRequest request, Class<T> returnType) throws Exception
  {
    FailOverInvokeFuture<T> future = FailOverInvokeFuture.with(returnType);
    
    int tryCount = retries + 1;
    invoke0(request, returnType, tryCount, future, null);
    
    return future;
  }
  




  private <T> void invoke0(final JRequest request, final Class<T> returnType, final int tryCount, final FailOverInvokeFuture<T> failOverFuture, Throwable lastCause)
  {
    if (tryCount > 0) {
      final InvokeFuture<T> future = dispatcher.dispatch(request, returnType);
      
      future.addListener(new JListener()
      {
        public void complete(T result)
        {
          failOverFuture.setSuccess(result);
        }
        
        public void failure(Throwable cause)
        {
          if (FailOverClusterInvoker.logger.isWarnEnabled()) {
            MessageWrapper message = request.message();
            JChannel channel = (future instanceof DefaultInvokeFuture) ? ((DefaultInvokeFuture)future).channel() : null;
            

            FailOverClusterInvoker.logger.warn("[{}]: [Fail-over] retry, [{}] attempts left, [method: {}], [metadata: {}], {}.", new Object[] { channel, Integer.valueOf(tryCount - 1), message.getMethodName(), message.getMetadata(), StackTraceUtil.stackTrace(cause) });
          }
          










          FailOverClusterInvoker.this.invoke0(request, returnType, tryCount - 1, failOverFuture, cause);
        }
      });
    } else {
      failOverFuture.setFailure(lastCause);
    }
  }
}
