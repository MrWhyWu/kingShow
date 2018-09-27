package org.jupiter.rpc.consumer.invoker;

import java.util.List;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.future.InvokeFuture;
import org.jupiter.rpc.consumer.future.InvokeFutureContext;
import org.jupiter.rpc.model.metadata.ClusterStrategyConfig;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.rpc.model.metadata.ServiceMetadata;





























public class AsyncGenericInvoker
  extends AbstractInvoker
  implements GenericInvoker
{
  public AsyncGenericInvoker(String appName, ServiceMetadata metadata, Dispatcher dispatcher, ClusterStrategyConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs)
  {
    super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
  }
  
  public Object $invoke(String methodName, Object... args) throws Throwable
  {
    Object result = doInvoke(methodName, args, Object.class, false);
    
    InvokeFutureContext.set((InvokeFuture)result);
    
    return null;
  }
}
