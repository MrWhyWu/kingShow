package org.jupiter.rpc.executor;

public abstract interface CloseableExecutor
{
  public abstract void execute(Runnable paramRunnable);
  
  public abstract void shutdown();
}
