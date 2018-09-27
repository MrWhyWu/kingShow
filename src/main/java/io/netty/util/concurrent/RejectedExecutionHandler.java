package io.netty.util.concurrent;

public abstract interface RejectedExecutionHandler
{
  public abstract void rejected(Runnable paramRunnable, SingleThreadEventExecutor paramSingleThreadEventExecutor);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.RejectedExecutionHandler
 * JD-Core Version:    0.7.0.1
 */