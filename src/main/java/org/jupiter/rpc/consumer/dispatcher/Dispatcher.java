package org.jupiter.rpc.consumer.dispatcher;

import java.util.List;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.ConsumerInterceptor;
import org.jupiter.rpc.consumer.future.InvokeFuture;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;

public abstract interface Dispatcher
{
  public abstract <T> InvokeFuture<T> dispatch(JRequest paramJRequest, Class<T> paramClass);
  
  public abstract Dispatcher interceptors(List<ConsumerInterceptor> paramList);
  
  public abstract Dispatcher timeoutMillis(long paramLong);
  
  public abstract Dispatcher methodSpecialConfigs(List<MethodSpecialConfig> paramList);
}
