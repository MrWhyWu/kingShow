package org.jupiter.rpc.consumer.future;

public abstract interface InvokeFutureGroup<V>
  extends InvokeFuture<V>
{
  public abstract InvokeFuture<V>[] futures();
}
