package org.jupiter.rpc.consumer.dispatcher;

import org.jupiter.rpc.DispatchType;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.consumer.future.DefaultInvokeFuture;
import org.jupiter.rpc.consumer.future.DefaultInvokeFutureGroup;
import org.jupiter.rpc.consumer.future.InvokeFuture;
import org.jupiter.rpc.model.metadata.MessageWrapper;
import org.jupiter.serialization.Serializer;
import org.jupiter.serialization.SerializerType;
import org.jupiter.serialization.io.OutputBuf;
import org.jupiter.transport.CodecConfig;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.channel.JChannelGroup;























public class DefaultBroadcastDispatcher
  extends AbstractDispatcher
{
  public DefaultBroadcastDispatcher(JClient client, SerializerType serializerType)
  {
    super(client, serializerType);
  }
  


  public <T> InvokeFuture<T> dispatch(JRequest request, Class<T> returnType)
  {
    Serializer _serializer = serializer();
    MessageWrapper message = request.message();
    
    JChannelGroup[] groups = groups(message.getMetadata());
    JChannel[] channels = new JChannel[groups.length];
    for (int i = 0; i < groups.length; i++) {
      channels[i] = groups[i].next();
    }
    
    byte s_code = _serializer.code();
    
    boolean isLowCopy = CodecConfig.isCodecLowCopy();
    if (!isLowCopy) {
      byte[] bytes = _serializer.writeObject(message);
      request.bytes(s_code, bytes);
    }
    
    InvokeFuture<T>[] futures = new DefaultInvokeFuture[channels.length];
    for (int i = 0; i < channels.length; i++) {
      JChannel channel = channels[i];
      if (isLowCopy) {
        OutputBuf outputBuf = _serializer.writeObject(channel.allocOutputBuf(), message);
        
        request.outputBuf(s_code, outputBuf);
      }
      futures[i] = write(channel, request, returnType, DispatchType.BROADCAST);
    }
    
    return DefaultInvokeFutureGroup.with(futures);
  }
}
