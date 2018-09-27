package org.jupiter.rpc.consumer.future;

import org.jupiter.common.util.Reflects;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.JListener;






























public class FailSafeInvokeFuture<V>
  implements InvokeFuture<V>
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(FailSafeInvokeFuture.class);
  private final InvokeFuture<V> future;
  
  public static <T> FailSafeInvokeFuture<T> with(InvokeFuture<T> future)
  {
    return new FailSafeInvokeFuture(future);
  }
  
  private FailSafeInvokeFuture(InvokeFuture<V> future) {
    this.future = future;
  }
  
  public Class<V> returnType()
  {
    return future.returnType();
  }
  
  public V getResult() throws Throwable
  {
    try {
      return future.getResult();
    } catch (Throwable t) {
      if (logger.isWarnEnabled()) {
        logger.warn("Ignored exception on [Fail-safe]: {}.", StackTraceUtil.stackTrace(t));
      }
    }
    return Reflects.getTypeDefaultValue(returnType());
  }
  
  public InvokeFuture<V> addListener(JListener<V> listener)
  {
    future.addListener(listener);
    return this;
  }
  
  public InvokeFuture<V> addListeners(JListener<V>... listeners)
  {
    future.addListeners(listeners);
    return this;
  }
  
  public InvokeFuture<V> removeListener(JListener<V> listener)
  {
    future.removeListener(listener);
    return this;
  }
  
  public InvokeFuture<V> removeListeners(JListener<V>... listeners)
  {
    future.removeListeners(listeners);
    return this;
  }
  
  public InvokeFuture<V> future() {
    return future;
  }
}
