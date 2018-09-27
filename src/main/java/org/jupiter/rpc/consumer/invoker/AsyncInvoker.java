package org.jupiter.rpc.consumer.invoker;

import java.lang.reflect.Method;
import java.util.List;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.jupiter.common.util.Reflects;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.future.InvokeFuture;
import org.jupiter.rpc.consumer.future.InvokeFutureContext;
import org.jupiter.rpc.model.metadata.ClusterStrategyConfig;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.rpc.model.metadata.ServiceMetadata;































public class AsyncInvoker
  extends AbstractInvoker
{
  public AsyncInvoker(String appName, ServiceMetadata metadata, Dispatcher dispatcher, ClusterStrategyConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs)
  {
    super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
  }
  
  @RuntimeType
  public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
    Class<?> returnType = method.getReturnType();
    
    Object result = doInvoke(method.getName(), args, returnType, false);
    
    InvokeFutureContext.set((InvokeFuture)result);
    
    return Reflects.getTypeDefaultValue(returnType);
  }
}
