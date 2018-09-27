package org.jupiter.rpc.provider.processor;

import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.ThrowUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.executor.CloseableExecutor;
import org.jupiter.rpc.flow.control.FlowController;
import org.jupiter.rpc.model.metadata.ResultWrapper;
import org.jupiter.rpc.provider.LookupService;
import org.jupiter.rpc.provider.processor.task.MessageTask;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerFactory;
import org.jupiter.transport.Status;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.channel.JFutureListener;
import org.jupiter.transport.payload.JRequestPayload;
import org.jupiter.transport.payload.JResponsePayload;
import org.jupiter.transport.processor.ProviderProcessor;























public abstract class DefaultProviderProcessor
  implements ProviderProcessor, LookupService, FlowController<JRequest>
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProviderProcessor.class);
  private final CloseableExecutor executor;
  
  public DefaultProviderProcessor()
  {
    this(ProviderExecutors.executor());
  }
  
  public DefaultProviderProcessor(CloseableExecutor executor) {
    this.executor = executor;
  }
  
  public void handleRequest(JChannel channel, JRequestPayload requestPayload) throws Exception
  {
    MessageTask task = new MessageTask(this, channel, new JRequest(requestPayload));
    if (executor == null) {
      task.run();
    } else {
      executor.execute(task);
    }
  }
  
  public void handleException(JChannel channel, JRequestPayload request, Status status, Throwable cause)
  {
    logger.error("An exception was caught while processing request: {}, {}.", channel.remoteAddress(), StackTraceUtil.stackTrace(cause));
    

    doHandleException(channel, request.invokeId(), request.serializerCode(), status.value(), cause, false);
  }
  

  public void shutdown()
  {
    if (executor != null) {
      executor.shutdown();
    }
  }
  
  public void handleException(JChannel channel, JRequest request, Status status, Throwable cause) {
    logger.error("An exception was caught while processing request: {}, {}.", channel.remoteAddress(), StackTraceUtil.stackTrace(cause));
    

    doHandleException(channel, request.invokeId(), request.serializerCode(), status.value(), cause, false);
  }
  
  public void handleRejected(JChannel channel, JRequest request, Status status, Throwable cause)
  {
    if (logger.isWarnEnabled()) {
      logger.warn("Service rejected: {}, {}.", channel.remoteAddress(), StackTraceUtil.stackTrace(cause));
    }
    
    doHandleException(channel, request.invokeId(), request.serializerCode(), status.value(), cause, true);
  }
  


  private void doHandleException(JChannel channel, long invokeId, byte s_code, byte status, Throwable cause, boolean closeChannel)
  {
    ResultWrapper result = new ResultWrapper();
    
    cause = ThrowUtil.cutCause(cause);
    result.setError(cause);
    
    Serializer serializer = SerializerFactory.getSerializer(s_code);
    byte[] bytes = serializer.writeObject(result);
    
    JResponsePayload response = new JResponsePayload(invokeId);
    response.status(status);
    response.bytes(s_code, bytes);
    
    if (closeChannel) {
      channel.write(response, JChannel.CLOSE);
    } else {
      channel.write(response, new JFutureListener()
      {
        public void operationSuccess(JChannel channel) throws Exception
        {
          DefaultProviderProcessor.logger.debug("Service error message sent out: {}.", channel);
        }
        
        public void operationFailure(JChannel channel, Throwable cause) throws Exception
        {
          if (DefaultProviderProcessor.logger.isWarnEnabled()) {
            DefaultProviderProcessor.logger.warn("Service error message sent failed: {}, {}.", channel, StackTraceUtil.stackTrace(cause));
          }
        }
      });
    }
  }
}
