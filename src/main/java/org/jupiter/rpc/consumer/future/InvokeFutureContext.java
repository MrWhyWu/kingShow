package org.jupiter.rpc.consumer.future;

import org.jupiter.common.util.Preconditions;


























public class InvokeFutureContext
{
  private static final ThreadLocal<InvokeFuture<?>> futureThreadLocal = new ThreadLocal();
  
  public InvokeFutureContext() {}
  
  public static InvokeFuture<?> future()
  {
    InvokeFuture<?> future = (InvokeFuture)Preconditions.checkNotNull(futureThreadLocal.get(), "future");
    futureThreadLocal.remove();
    return future;
  }
  


  public static <V> InvokeFuture<V> future(Class<V> expectReturnType)
  {
    InvokeFuture<?> f = future();
    checkReturnType(f.returnType(), expectReturnType);
    
    return f;
  }
  


  public static <V> InvokeFutureGroup<V> futureBroadcast(Class<V> expectReturnType)
  {
    InvokeFuture<?> f = future();
    checkReturnType(f.returnType(), expectReturnType);
    
    if ((f instanceof InvokeFutureGroup))
      return (InvokeFutureGroup)f;
    if ((f instanceof FailSafeInvokeFuture)) {
      InvokeFuture real_f = ((FailSafeInvokeFuture)f).future();
      if ((real_f instanceof InvokeFutureGroup)) {
        return (InvokeFutureGroup)real_f;
      }
    }
    throw new UnsupportedOperationException("broadcast");
  }
  
  public static void set(InvokeFuture<?> future) {
    futureThreadLocal.set(future);
  }
  
  private static void checkReturnType(Class<?> realType, Class<?> expectType) {
    if (!expectType.isAssignableFrom(realType)) {
      throw new IllegalArgumentException("illegal returnType, expect type is [" + expectType.getName() + "], but real type is [" + realType.getName() + "]");
    }
  }
}
