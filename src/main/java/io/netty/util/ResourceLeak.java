package io.netty.util;

@Deprecated
public abstract interface ResourceLeak
{
  public abstract void record();
  
  public abstract void record(Object paramObject);
  
  public abstract boolean close();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.ResourceLeak
 * JD-Core Version:    0.7.0.1
 */