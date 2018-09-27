package org.jupiter.rpc.consumer.cluster;

import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.future.FailSafeInvokeFuture;
import org.jupiter.rpc.consumer.future.InvokeFuture;



























public class FailSafeClusterInvoker
  implements ClusterInvoker
{
  private final Dispatcher dispatcher;
  
  public FailSafeClusterInvoker(Dispatcher dispatcher)
  {
    this.dispatcher = dispatcher;
  }
  
  public ClusterInvoker.Strategy strategy()
  {
    return ClusterInvoker.Strategy.FAIL_SAFE;
  }
  
  public <T> InvokeFuture<T> invoke(JRequest request, Class<T> returnType) throws Exception
  {
    InvokeFuture<T> future = dispatcher.dispatch(request, returnType);
    return FailSafeInvokeFuture.with(future);
  }
}
