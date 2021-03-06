package org.jupiter.rpc.consumer.invoker;

import java.lang.reflect.Method;
import java.util.List;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.model.metadata.ClusterStrategyConfig;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.rpc.model.metadata.ServiceMetadata;






























public class SyncInvoker
  extends AbstractInvoker
{
  public SyncInvoker(String appName, ServiceMetadata metadata, Dispatcher dispatcher, ClusterStrategyConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs)
  {
    super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
  }
  
  @RuntimeType
  public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
    return doInvoke(method.getName(), args, method.getReturnType(), true);
  }
}
