package org.jupiter.rpc.consumer.processor.task;

import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.JResponse;
import org.jupiter.rpc.consumer.future.DefaultInvokeFuture;
import org.jupiter.rpc.exception.JupiterSerializationException;
import org.jupiter.rpc.model.metadata.ResultWrapper;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerFactory;
import org.jupiter.serialization.io.InputBuf;
import org.jupiter.transport.CodecConfig;
import org.jupiter.transport.Status;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.payload.JResponsePayload;























public class MessageTask
  implements Runnable
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageTask.class);
  private final JChannel channel;
  private final JResponse response;
  
  public MessageTask(JChannel channel, JResponse response)
  {
    this.channel = channel;
    this.response = response;
  }
  

  public void run()
  {
    JResponse _response = response;
    JResponsePayload _responsePayload = _response.payload();
    
    byte s_code = _response.serializerCode();
    
    Serializer serializer = SerializerFactory.getSerializer(s_code);
    ResultWrapper wrapper;
    try { ResultWrapper wrapper;
      ResultWrapper wrapper; if (CodecConfig.isCodecLowCopy()) {
        InputBuf inputBuf = _responsePayload.inputBuf();
        wrapper = (ResultWrapper)serializer.readObject(inputBuf, ResultWrapper.class);
      } else {
        byte[] bytes = _responsePayload.bytes();
        wrapper = (ResultWrapper)serializer.readObject(bytes, ResultWrapper.class);
      }
      _responsePayload.clear();
    } catch (Throwable t) {
      logger.error("Deserialize object failed: {}, {}.", channel.remoteAddress(), StackTraceUtil.stackTrace(t));
      
      _response.status(Status.DESERIALIZATION_FAIL);
      wrapper = new ResultWrapper();
      wrapper.setError(new JupiterSerializationException(t));
    }
    _response.result(wrapper);
    
    DefaultInvokeFuture.received(channel, _response);
  }
}
