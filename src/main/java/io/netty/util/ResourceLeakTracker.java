package io.netty.util;

public abstract interface ResourceLeakTracker<T>
{
  public abstract void record();
  
  public abstract void record(Object paramObject);
  
  public abstract boolean close(T paramT);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ResourceLeakTracker
 * JD-Core Version:    0.7.0.1
 */