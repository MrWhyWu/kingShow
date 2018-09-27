package org.jupiter.rpc.consumer.future;

import org.jupiter.common.util.Preconditions;
import org.jupiter.rpc.JListener;

























public class DefaultInvokeFutureGroup<V>
  implements InvokeFutureGroup<V>
{
  private final InvokeFuture<V>[] futures;
  
  public static <T> DefaultInvokeFutureGroup<T> with(InvokeFuture<T>[] futures)
  {
    return new DefaultInvokeFutureGroup(futures);
  }
  
  private DefaultInvokeFutureGroup(InvokeFuture<V>[] futures) {
    Preconditions.checkArgument((futures != null) && (futures.length > 0), "empty futures");
    this.futures = futures;
  }
  
  public Class<V> returnType()
  {
    return futures[0].returnType();
  }
  
  public V getResult() throws Throwable
  {
    throw new UnsupportedOperationException();
  }
  
  public InvokeFuture<V>[] futures()
  {
    return futures;
  }
  
  public InvokeFutureGroup<V> addListener(JListener<V> listener)
  {
    for (InvokeFuture<V> f : futures) {
      f.addListener(listener);
    }
    return this;
  }
  
  public InvokeFutureGroup<V> addListeners(JListener<V>... listeners)
  {
    for (InvokeFuture<V> f : futures) {
      f.addListeners(listeners);
    }
    return this;
  }
  
  public InvokeFutureGroup<V> removeListener(JListener<V> listener)
  {
    for (InvokeFuture<V> f : futures) {
      f.removeListener(listener);
    }
    return this;
  }
  
  public InvokeFutureGroup<V> removeListeners(JListener<V>... listeners)
  {
    for (InvokeFuture<V> f : futures) {
      f.removeListeners(listeners);
    }
    return this;
  }
}
