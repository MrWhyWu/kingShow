package io.netty.buffer;

public abstract interface PoolChunkMetric
{
  public abstract int usage();
  
  public abstract int chunkSize();
  
  public abstract int freeBytes();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolChunkMetric
 * JD-Core Version:    0.7.0.1
 */