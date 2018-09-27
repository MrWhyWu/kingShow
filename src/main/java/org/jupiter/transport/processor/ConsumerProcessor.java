package org.jupiter.transport.processor;

import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.payload.JResponsePayload;

public abstract interface ConsumerProcessor
{
  public abstract void handleResponse(JChannel paramJChannel, JResponsePayload paramJResponsePayload)
    throws Exception;
  
  public abstract void shutdown();
}
