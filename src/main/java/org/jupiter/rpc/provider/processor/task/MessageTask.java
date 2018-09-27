package org.jupiter.rpc.provider.processor.task;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.concurrent.RejectedRunnable;
import org.jupiter.common.util.Pair;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.Reflects;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.UnsafeIntegerFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.DefaultFilterChain;
import org.jupiter.rpc.JFilter;
import org.jupiter.rpc.JFilter.Type;
import org.jupiter.rpc.JFilterChain;
import org.jupiter.rpc.JFilterContext;
import org.jupiter.rpc.JFilterLoader;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.exception.JupiterBadRequestException;
import org.jupiter.rpc.exception.JupiterFlowControlException;
import org.jupiter.rpc.exception.JupiterRemoteException;
import org.jupiter.rpc.exception.JupiterServerBusyException;
import org.jupiter.rpc.exception.JupiterServiceNotFoundException;
import org.jupiter.rpc.flow.control.ControlResult;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.metric.Metrics;
import org.jupiter.rpc.model.metadata.MessageWrapper;
import org.jupiter.rpc.model.metadata.ResultWrapper;
import org.jupiter.rpc.model.metadata.ServiceWrapper;
import org.jupiter.rpc.provider.ProviderInterceptor;
import org.jupiter.rpc.provider.processor.DefaultProviderProcessor;
import org.jupiter.rpc.tracing.TraceId;
import org.jupiter.rpc.tracing.TracingUtil;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerFactory;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.transport.CodecConfig;
import org.jupiter.transport.Status;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.channel.JFutureListener;
import org.jupiter.transport.payload.JRequestPayload;
import org.jupiter.transport.payload.JResponsePayload;










public class MessageTask
  implements RejectedRunnable
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageTask.class);
  
  private static final boolean METRIC_NEEDED = SystemPropertyUtil.getBoolean("jupiter.metric.needed", false);
  
  private static final Signal INVOKE_ERROR = Signal.valueOf(MessageTask.class, "INVOKE_ERROR");
  
  private static final UnsafeIntegerFieldUpdater<TraceId> traceNodeUpdater = UnsafeUpdater.newIntegerFieldUpdater(TraceId.class, "node");
  
  private final DefaultProviderProcessor processor;
  private final JChannel channel;
  private final JRequest request;
  
  public MessageTask(DefaultProviderProcessor processor, JChannel channel, JRequest request)
  {
    this.processor = processor;
    this.channel = channel;
    this.request = request;
  }
  

  public void run()
  {
    DefaultProviderProcessor _processor = processor;
    JRequest _request = request;
    

    ControlResult ctrl = _processor.flowControl(_request);
    if (!ctrl.isAllowed()) {
      rejected(Status.APP_FLOW_CONTROL, new JupiterFlowControlException(String.valueOf(ctrl)));
      return;
    }
    
    try
    {
      JRequestPayload _requestPayload = _request.payload();
      
      byte s_code = _requestPayload.serializerCode();
      Serializer serializer = SerializerFactory.getSerializer(s_code);
      MessageWrapper msg;
      MessageWrapper msg;
      if (CodecConfig.isCodecLowCopy()) {
        InputBuf inputBuf = _requestPayload.inputBuf();
        msg = (MessageWrapper)serializer.readObject(inputBuf, MessageWrapper.class);
      } else {
        byte[] bytes = _requestPayload.bytes();
        msg = (MessageWrapper)serializer.readObject(bytes, MessageWrapper.class);
      }
      _requestPayload.clear();
      
      _request.message(msg);
    } catch (Throwable t) {
      rejected(Status.BAD_REQUEST, new JupiterBadRequestException("reading request failed", t)); return;
    }
    
    MessageWrapper msg;
    
    final ServiceWrapper service = _processor.lookupService(msg.getMetadata());
    if (service == null) {
      rejected(Status.SERVICE_NOT_FOUND, new JupiterServiceNotFoundException(String.valueOf(msg)));
      return;
    }
    

    FlowController<JRequest> childController = service.getFlowController();
    if (childController != null) {
      ctrl = childController.flowControl(_request);
      if (!ctrl.isAllowed()) {
        rejected(Status.PROVIDER_FLOW_CONTROL, new JupiterFlowControlException(String.valueOf(ctrl)));
        return;
      }
    }
    

    Executor childExecutor = service.getExecutor();
    if (childExecutor == null) {
      process(service);
    }
    else {
      childExecutor.execute(new Runnable()
      {
        public void run()
        {
          MessageTask.this.process(service);
        }
      });
    }
  }
  
  public void rejected()
  {
    rejected(Status.SERVER_BUSY, new JupiterServerBusyException(String.valueOf(request)));
  }
  
  private void rejected(Status status, JupiterRemoteException cause) {
    if (METRIC_NEEDED) {
      MetricsHolder.rejectionMeter.mark();
    }
    



    processor.handleRejected(channel, request, status, cause);
  }
  
  private void process(ServiceWrapper service)
  {
    JRequest _request = request;
    
    Context invokeCtx = new Context(service);
    
    if (TracingUtil.isTracingNeeded()) {
      setCurrentTraceId(_request.message().getTraceId());
    }
    try
    {
      Object invokeResult = ((Context)Chains.invoke(_request, invokeCtx)).getResult();
      

      ResultWrapper result = new ResultWrapper();
      result.setResult(invokeResult);
      byte s_code = _request.serializerCode();
      Serializer serializer = SerializerFactory.getSerializer(s_code);
      
      JResponsePayload responsePayload = new JResponsePayload(_request.invokeId());
      
      if (CodecConfig.isCodecLowCopy()) {
        OutputBuf outputBuf = serializer.writeObject(channel.allocOutputBuf(), result);
        
        responsePayload.outputBuf(s_code, outputBuf);
      } else {
        byte[] bytes = serializer.writeObject(result);
        responsePayload.bytes(s_code, bytes);
      }
      
      responsePayload.status(Status.OK.value());
      
      handleWriteResponse(responsePayload);
    } catch (Throwable t) {
      if (INVOKE_ERROR == t)
      {
        handleException(invokeCtx.getExpectCauseTypes(), invokeCtx.getCause());
      } else {
        processor.handleException(channel, _request, Status.SERVER_ERROR, t);
      }
    } finally {
      if (TracingUtil.isTracingNeeded()) {
        TracingUtil.clearCurrent();
      }
    }
  }
  
  private void handleWriteResponse(JResponsePayload response) {
    channel.write(response, new JFutureListener()
    {
      public void operationSuccess(JChannel channel) throws Exception
      {
        if (MessageTask.METRIC_NEEDED) {
          long duration = SystemClock.millisClock().now() - request.timestamp();
          MessageTask.MetricsHolder.processingTimer.update(duration, TimeUnit.MILLISECONDS);
        }
      }
      
      public void operationFailure(JChannel channel, Throwable cause) throws Exception
      {
        long duration = SystemClock.millisClock().now() - request.timestamp();
        MessageTask.logger.error("Response sent failed, trace: {}, duration: {} millis, channel: {}, cause: {}.", new Object[] { request.getTraceId(), Long.valueOf(duration), channel, cause });
      }
    });
  }
  
  private void handleException(Class<?>[] exceptionTypes, Throwable failCause)
  {
    if ((exceptionTypes != null) && (exceptionTypes.length > 0)) {
      Class<?> failType = failCause.getClass();
      for (Class<?> eType : exceptionTypes)
      {
        if (eType.isAssignableFrom(failType))
        {
          processor.handleException(channel, request, Status.SERVICE_EXPECTED_ERROR, failCause);
          return;
        }
      }
    }
    

    processor.handleException(channel, request, Status.SERVICE_UNEXPECTED_ERROR, failCause);
  }
  
  private static Object invoke(MessageWrapper msg, Context invokeCtx) throws Signal {
    ServiceWrapper service = invokeCtx.getService();
    Object provider = service.getServiceProvider();
    String methodName = msg.getMethodName();
    Object[] args = msg.getArgs();
    
    Timer.Context timerCtx = null;
    if (METRIC_NEEDED) {
      timerCtx = Metrics.timer(msg.getOperationName()).time();
    }
    
    Class<?>[] expectCauseTypes = null;
    try {
      List<Pair<Class<?>[], Class<?>[]>> methodExtension = service.getMethodExtension(methodName);
      if (methodExtension == null) {
        throw new NoSuchMethodException(methodName);
      }
      

      Pair<Class<?>[], Class<?>[]> bestMatch = Reflects.findMatchingParameterTypesExt(methodExtension, args);
      Class<?>[] parameterTypes = (Class[])bestMatch.getFirst();
      expectCauseTypes = (Class[])bestMatch.getSecond();
      
      return Reflects.fastInvoke(provider, methodName, parameterTypes, args);
    } catch (Throwable t) {
      invokeCtx.setCauseAndExpectTypes(t, expectCauseTypes);
      throw INVOKE_ERROR;
    } finally {
      if (METRIC_NEEDED) {
        timerCtx.stop();
      }
    }
  }
  





  private static void handleBeforeInvoke(ProviderInterceptor[] interceptors, TraceId traceId, Object provider, String methodName, Object[] args)
  {
    for (int i = 0; i < interceptors.length; i++) {
      try {
        interceptors[i].beforeInvoke(traceId, provider, methodName, args);
      } catch (Throwable t) {
        logger.error("Interceptor[{}#beforeInvoke]: {}.", Reflects.simpleClassName(interceptors[i]), StackTraceUtil.stackTrace(t));
      }
    }
  }
  







  private static void handleAfterInvoke(ProviderInterceptor[] interceptors, TraceId traceId, Object provider, String methodName, Object[] args, Object invokeResult, Throwable failCause)
  {
    for (int i = interceptors.length - 1; i >= 0; i--) {
      try {
        interceptors[i].afterInvoke(traceId, provider, methodName, args, invokeResult, failCause);
      } catch (Throwable t) {
        logger.error("Interceptor[{}#afterInvoke]: {}.", Reflects.simpleClassName(interceptors[i]), StackTraceUtil.stackTrace(t));
      }
    }
  }
  
  private static void setCurrentTraceId(TraceId traceId) {
    if ((traceId != null) && (traceId != TraceId.NULL_TRACE_ID)) {
      assert (traceNodeUpdater != null);
      traceNodeUpdater.set(traceId, traceId.getNode() + 1);
    }
    TracingUtil.setCurrent(traceId);
  }
  
  public static class Context implements JFilterContext
  {
    private final ServiceWrapper service;
    private Object result;
    private Throwable cause;
    private Class<?>[] expectCauseTypes;
    
    public Context(ServiceWrapper service)
    {
      this.service = ((ServiceWrapper)Preconditions.checkNotNull(service, "service"));
    }
    
    public ServiceWrapper getService() {
      return service;
    }
    
    public Object getResult() {
      return result;
    }
    
    public void setResult(Object result) {
      this.result = result;
    }
    
    public Throwable getCause() {
      return cause;
    }
    
    public Class<?>[] getExpectCauseTypes() {
      return expectCauseTypes;
    }
    
    public void setCauseAndExpectTypes(Throwable cause, Class<?>[] expectCauseTypes) {
      this.cause = cause;
      this.expectCauseTypes = expectCauseTypes;
    }
    
    public JFilter.Type getType()
    {
      return JFilter.Type.PROVIDER;
    }
  }
  
  static class InterceptorsFilter implements JFilter {
    InterceptorsFilter() {}
    
    public JFilter.Type getType() {
      return JFilter.Type.PROVIDER;
    }
    
    public <T extends JFilterContext> void doFilter(JRequest request, T filterCtx, JFilterChain next) throws Throwable
    {
      MessageTask.Context invokeCtx = (MessageTask.Context)filterCtx;
      ServiceWrapper service = invokeCtx.getService();
      
      ProviderInterceptor[] interceptors = service.getInterceptors();
      
      if ((interceptors == null) || (interceptors.length == 0)) {
        next.doFilter(request, filterCtx);
      } else {
        TraceId traceId = TracingUtil.getCurrent();
        Object provider = service.getServiceProvider();
        
        MessageWrapper msg = request.message();
        String methodName = msg.getMethodName();
        Object[] args = msg.getArgs();
        
        MessageTask.handleBeforeInvoke(interceptors, traceId, provider, methodName, args);
        try {
          next.doFilter(request, filterCtx);
        } finally {
          MessageTask.handleAfterInvoke(interceptors, traceId, provider, methodName, args, invokeCtx.getResult(), invokeCtx.getCause());
        }
      }
    }
  }
  
  static class InvokeFilter implements JFilter
  {
    InvokeFilter() {}
    
    public JFilter.Type getType() {
      return JFilter.Type.PROVIDER;
    }
    
    public <T extends JFilterContext> void doFilter(JRequest request, T filterCtx, JFilterChain next) throws Throwable
    {
      MessageWrapper msg = request.message();
      MessageTask.Context invokeCtx = (MessageTask.Context)filterCtx;
      
      Object invokeResult = MessageTask.invoke(msg, invokeCtx);
      
      invokeCtx.setResult(invokeResult);
    }
  }
  
  static class Chains
  {
    private static final JFilterChain headChain;
    
    static {
      JFilterChain invokeChain = new DefaultFilterChain(new MessageTask.InvokeFilter(), null);
      JFilterChain interceptChain = new DefaultFilterChain(new MessageTask.InterceptorsFilter(), invokeChain);
      headChain = JFilterLoader.loadExtFilters(interceptChain, JFilter.Type.PROVIDER);
    }
    
    static <T extends JFilterContext> T invoke(JRequest request, T invokeCtx) throws Throwable {
      headChain.doFilter(request, invokeCtx);
      return invokeCtx;
    }
    
    Chains() {}
  }
  
  static class MetricsHolder {
    static final Timer processingTimer = Metrics.timer("processing");
    
    static final Meter rejectionMeter = Metrics.meter("rejection");
    
    MetricsHolder() {}
  }
}
