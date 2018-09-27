package org.jupiter.rpc.consumer.future;

public abstract interface InvokeFuture<V>
  extends ListenableFuture<V>
{
  public abstract Class<V> returnType();
  
  public abstract V getResult()
    throws Throwable;
}
