package org.jupiter.rpc.consumer;

import org.jupiter.rpc.JRequest;
import org.jupiter.rpc.JResponse;
import org.jupiter.rpc.tracing.TraceId;
import org.jupiter.transport.channel.JChannel;

public abstract interface ConsumerInterceptor
{
  public abstract void beforeInvoke(TraceId paramTraceId, JRequest paramJRequest, JChannel paramJChannel);
  
  public abstract void afterInvoke(TraceId paramTraceId, JResponse paramJResponse, JChannel paramJChannel);
}
