package org.jupiter.common.util.internal;

import org.jupiter.common.util.Preconditions;





















public class InternalThreadLocalRunnable
  implements Runnable
{
  private final Runnable runnable;
  
  private InternalThreadLocalRunnable(Runnable runnable)
  {
    this.runnable = ((Runnable)Preconditions.checkNotNull(runnable, "runnable"));
  }
  
  public void run()
  {
    try {
      runnable.run();
    } finally {
      InternalThreadLocal.removeAll();
    }
  }
  
  public static Runnable wrap(Runnable runnable) {
    return (runnable instanceof InternalThreadLocalRunnable) ? runnable : new InternalThreadLocalRunnable(runnable);
  }
}
