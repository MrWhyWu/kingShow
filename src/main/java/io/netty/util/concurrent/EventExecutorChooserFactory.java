package io.netty.util.concurrent;

public abstract interface EventExecutorChooserFactory
{
  public abstract EventExecutorChooser newChooser(EventExecutor[] paramArrayOfEventExecutor);
  
  public static abstract interface EventExecutorChooser
  {
    public abstract EventExecutor next();
  }
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.EventExecutorChooserFactory
 * JD-Core Version:    0.7.0.1
 */