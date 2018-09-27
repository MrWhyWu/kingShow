package org.jupiter.transport.processor;

import org.jupiter.transport.Status;
import org.jupiter.transport.channel.JChannel;
import org.jupiter.transport.payload.JRequestPayload;

public abstract interface ProviderProcessor
{
  public abstract void handleRequest(JChannel paramJChannel, JRequestPayload paramJRequestPayload)
    throws Exception;
  
  public abstract void handleException(JChannel paramJChannel, JRequestPayload paramJRequestPayload, Status paramStatus, Throwable paramThrowable);
  
  public abstract void shutdown();
}
