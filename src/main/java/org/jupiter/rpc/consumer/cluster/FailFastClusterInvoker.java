package org.jupiter.rpc.consumer.cluster;

import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.future.InvokeFuture;



























public class FailFastClusterInvoker
  implements ClusterInvoker
{
  private final Dispatcher dispatcher;
  
  public FailFastClusterInvoker(Dispatcher dispatcher)
  {
    this.dispatcher = dispatcher;
  }
  
  public ClusterInvoker.Strategy strategy()
  {
    return ClusterInvoker.Strategy.FAIL_FAST;
  }
  
  public <T> InvokeFuture<T> invoke(JRequest request, Class<T> returnType) throws Exception
  {
    return dispatcher.dispatch(request, returnType);
  }
}
