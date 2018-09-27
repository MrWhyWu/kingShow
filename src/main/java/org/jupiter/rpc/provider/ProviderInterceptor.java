package org.jupiter.rpc.provider;

import org.jupiter.rpc.tracing.TraceId;

public abstract interface ProviderInterceptor
{
  public abstract void beforeInvoke(TraceId paramTraceId, Object paramObject, String paramString, Object[] paramArrayOfObject);
  
  public abstract void afterInvoke(TraceId paramTraceId, Object paramObject1, String paramString, Object[] paramArrayOfObject, Object paramObject2, Throwable paramThrowable);
}
