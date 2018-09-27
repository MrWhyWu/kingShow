package io.netty.buffer;

public abstract interface PoolSubpageMetric
{
  public abstract int maxNumElements();
  
  public abstract int numAvailable();
  
  public abstract int elementSize();
  
  public abstract int pageSize();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolSubpageMetric
 * JD-Core Version:    0.7.0.1
 */