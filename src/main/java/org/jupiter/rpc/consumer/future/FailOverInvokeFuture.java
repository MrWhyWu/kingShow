package org.jupiter.rpc.consumer.future;

import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.rpc.JListener;



























public class FailOverInvokeFuture<V>
  extends AbstractListenableFuture<V>
  implements InvokeFuture<V>
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(FailOverInvokeFuture.class);
  private final Class<V> returnType;
  
  public static <T> FailOverInvokeFuture<T> with(Class<T> returnType)
  {
    return new FailOverInvokeFuture(returnType);
  }
  
  private FailOverInvokeFuture(Class<V> returnType) {
    this.returnType = returnType;
  }
  
  public void setSuccess(V result) {
    set(result);
  }
  
  public void setFailure(Throwable cause) {
    setException(cause);
  }
  
  public Class<V> returnType()
  {
    return returnType;
  }
  
  public V getResult() throws Throwable
  {
    return get();
  }
  
  protected void notifyListener0(JListener<V> listener, int state, Object x)
  {
    try
    {
      if (state == 2) {
        listener.complete(x);
      } else {
        listener.failure((Throwable)x);
      }
    } catch (Throwable t) {
      logger.error("An exception was thrown by {}.{}, {}.", new Object[] { listener.getClass().getName(), state == 2 ? "complete()" : "failure()", StackTraceUtil.stackTrace(t) });
    }
  }
}
