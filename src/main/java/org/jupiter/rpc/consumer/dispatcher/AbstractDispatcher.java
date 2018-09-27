package org.jupiter.rpc.consumer.dispatcher;

import java.util.List;
import java.util.Map;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.JResponse;
import org.jupiter.rpc.consumer.ConsumerInterceptor;
import org.jupiter.rpc.consumer.future.DefaultInvokeFuture;
import org.jupiter.rpc.exception.JupiterRemoteException;
import org.jupiter.rpc.load.balance.LoadBalancer;
import org.jupiter.rpc.model.metadata.MessageWrapper;
import org.jupiter.rpc.model.metadata.MethodSpecialConfig;
import org.jupiter.rpc.model.metadata.ResultWrapper;
import org.jupiter.rpc.model.metadata.ServiceMetadata;
import org.jupiter.rpc.tracing.TraceId;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerFactory;
import org.jupiter.serialization.SerializerType;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.Status;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.channel.JChannelGroup;
import org.jupiter.transport.channel.JFutureListener;
import org.jupiter.transport.payload.JRequestPayload;























abstract class AbstractDispatcher
  implements Dispatcher
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractDispatcher.class);
  
  private final JClient client;
  private final LoadBalancer loadBalancer;
  private final Serializer serializerImpl;
  private ConsumerInterceptor[] interceptors;
  private long timeoutMillis = JConstants.DEFAULT_TIMEOUT;
  
  private Map<String, Long> methodSpecialTimeoutMapping = Maps.newHashMap();
  
  public AbstractDispatcher(JClient client, SerializerType serializerType) {
    this(client, null, serializerType);
  }
  
  public AbstractDispatcher(JClient client, LoadBalancer loadBalancer, SerializerType serializerType) {
    this.client = client;
    this.loadBalancer = loadBalancer;
    serializerImpl = SerializerFactory.getSerializer(serializerType.value());
  }
  
  public Serializer serializer() {
    return serializerImpl;
  }
  
  public ConsumerInterceptor[] interceptors() {
    return interceptors;
  }
  
  public Dispatcher interceptors(List<ConsumerInterceptor> interceptors)
  {
    if ((interceptors != null) && (!interceptors.isEmpty())) {
      this.interceptors = ((ConsumerInterceptor[])interceptors.toArray(new ConsumerInterceptor[interceptors.size()]));
    }
    return this;
  }
  
  public Dispatcher timeoutMillis(long timeoutMillis)
  {
    if (timeoutMillis > 0L) {
      this.timeoutMillis = timeoutMillis;
    }
    return this;
  }
  
  public Dispatcher methodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs)
  {
    if (!methodSpecialConfigs.isEmpty()) {
      for (MethodSpecialConfig config : methodSpecialConfigs) {
        long timeoutMillis = config.getTimeoutMillis();
        if (timeoutMillis > 0L) {
          methodSpecialTimeoutMapping.put(config.getMethodName(), Long.valueOf(timeoutMillis));
        }
      }
    }
    return this;
  }
  
  protected long getMethodSpecialTimeoutMillis(String methodName) {
    Long methodTimeoutMillis = (Long)methodSpecialTimeoutMapping.get(methodName);
    if ((methodTimeoutMillis != null) && (methodTimeoutMillis.longValue() > 0L)) {
      return methodTimeoutMillis.longValue();
    }
    return timeoutMillis;
  }
  
  protected JChannel select(ServiceMetadata metadata) {
    CopyOnWriteGroupList groups = client.connector().directory(metadata);
    

    JChannelGroup group = loadBalancer.select(groups, metadata);
    
    if (group != null) {
      if (group.isAvailable()) {
        return group.next();
      }
      

      long deadline = group.deadlineMillis();
      if ((deadline > 0L) && (SystemClock.millisClock().now() > deadline)) {
        boolean removed = groups.remove(group);
        if ((removed) && 
          (logger.isWarnEnabled())) {
          logger.warn("Removed channel group: {} in directory: {} on [select].", group, metadata.directoryString());
        }
        
      }
      

    }
    else if (!client.awaitConnections(metadata, 3000L)) {
      throw new IllegalStateException("No connections");
    }
    

    JChannelGroup[] snapshot = groups.getSnapshot();
    for (JChannelGroup g : snapshot) {
      if (g.isAvailable()) {
        return g.next();
      }
    }
    
    throw new IllegalStateException("No channel");
  }
  
  protected JChannelGroup[] groups(ServiceMetadata metadata) {
    return client.connector().directory(metadata).getSnapshot();
  }
  



  protected <T> DefaultInvokeFuture<T> write(JChannel channel, final JRequest request, Class<T> returnType, final DispatchType dispatchType)
  {
    MessageWrapper message = request.message();
    long timeoutMillis = getMethodSpecialTimeoutMillis(message.getMethodName());
    ConsumerInterceptor[] interceptors = interceptors();
    TraceId traceId = message.getTraceId();
    final DefaultInvokeFuture<T> future = DefaultInvokeFuture.with(request.invokeId(), channel, timeoutMillis, returnType, dispatchType).interceptors(interceptors).traceId(traceId);
    



    if (interceptors != null) {
      for (int i = 0; i < interceptors.length; i++) {
        interceptors[i].beforeInvoke(traceId, request, channel);
      }
    }
    
    final JRequestPayload payload = request.payload();
    
    channel.write(payload, new JFutureListener()
    {
      public void operationSuccess(JChannel channel)
        throws Exception
      {
        future.markSent();
        
        if (dispatchType == DispatchType.ROUND) {
          payload.clear();
        }
      }
      
      public void operationFailure(JChannel channel, Throwable cause) throws Exception
      {
        if (dispatchType == DispatchType.ROUND) {
          payload.clear();
        }
        
        if (AbstractDispatcher.logger.isWarnEnabled()) {
          AbstractDispatcher.logger.warn("Writes {} fail on {}, {}.", new Object[] { request, channel, StackTraceUtil.stackTrace(cause) });
        }
        
        ResultWrapper result = new ResultWrapper();
        result.setError(new JupiterRemoteException(cause));
        
        JResponse response = new JResponse(payload.invokeId());
        response.status(Status.CLIENT_ERROR);
        response.result(result);
        
        DefaultInvokeFuture.fakeReceived(channel, response, dispatchType);
      }
      
    });
    return future;
  }
}
