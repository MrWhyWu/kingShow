package io.netty.buffer;

public abstract interface ByteBufAllocatorMetric
{
  public abstract long usedHeapMemory();
  
  public abstract long usedDirectMemory();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBufAllocatorMetric
 * JD-Core Version:    0.7.0.1
 */