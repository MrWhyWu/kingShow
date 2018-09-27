package org.jupiter.rpc.consumer.future;

import org.jupiter.rpc.JListener;

public abstract interface ListenableFuture<V>
{
  public abstract ListenableFuture<V> addListener(JListener<V> paramJListener);
  
  public abstract ListenableFuture<V> addListeners(JListener<V>... paramVarArgs);
  
  public abstract ListenableFuture<V> removeListener(JListener<V> paramJListener);
  
  public abstract ListenableFuture<V> removeListeners(JListener<V>... paramVarArgs);
}
