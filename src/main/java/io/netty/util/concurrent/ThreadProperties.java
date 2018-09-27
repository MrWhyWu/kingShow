package io.netty.util.concurrent;

public abstract interface ThreadProperties
{
  public abstract Thread.State state();
  
  public abstract int priority();
  
  public abstract boolean isInterrupted();
  
  public abstract boolean isDaemon();
  
  public abstract String name();
  
  public abstract long id();
  
  public abstract StackTraceElement[] stackTrace();
  
  public abstract boolean isAlive();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.ThreadProperties
 * JD-Core Version:    0.7.0.1
 */