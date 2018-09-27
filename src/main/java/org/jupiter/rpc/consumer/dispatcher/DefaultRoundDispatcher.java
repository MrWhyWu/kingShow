package org.jupiter.rpc.consumer.dispatcher;

import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.future.InvokeFuture;
import org.jupiter.rpc.load.balance.LoadBalancer;
import org.jupiter.rpc.model.metadata.MessageWrapper;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerType;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.transport.CodecConfig;
import org.jupiter.transport.channel.JChannel;
























public class DefaultRoundDispatcher
  extends AbstractDispatcher
{
  public DefaultRoundDispatcher(JClient client, LoadBalancer loadBalancer, SerializerType serializerType)
  {
    super(client, loadBalancer, serializerType);
  }
  

  public <T> InvokeFuture<T> dispatch(JRequest request, Class<T> returnType)
  {
    Serializer _serializer = serializer();
    MessageWrapper message = request.message();
    

    JChannel channel = select(message.getMetadata());
    
    byte s_code = _serializer.code();
    
    if (CodecConfig.isCodecLowCopy()) {
      OutputBuf outputBuf = _serializer.writeObject(channel.allocOutputBuf(), message);
      
      request.outputBuf(s_code, outputBuf);
    } else {
      byte[] bytes = _serializer.writeObject(message);
      request.bytes(s_code, bytes);
    }
    
    return write(channel, request, returnType, DispatchType.ROUND);
  }
}
