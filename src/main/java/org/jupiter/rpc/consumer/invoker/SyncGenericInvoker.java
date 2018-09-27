package org.jupiter.rpc.consumer.invoker;

import java.util.List;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.model.metadata.ClusterStrategyConfig;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.rpc.model.metadata.ServiceMetadata;





























public class SyncGenericInvoker
  extends AbstractInvoker
  implements GenericInvoker
{
  public SyncGenericInvoker(String appName, ServiceMetadata metadata, Dispatcher dispatcher, ClusterStrategyConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs)
  {
    super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
  }
  
  public Object $invoke(String methodName, Object... args) throws Throwable
  {
    return doInvoke(methodName, args, Object.class, true);
  }
}
