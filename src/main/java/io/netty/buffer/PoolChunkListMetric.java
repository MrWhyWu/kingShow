package io.netty.buffer;

public abstract interface PoolChunkListMetric
  extends Iterable<PoolChunkMetric>
{
  public abstract int minUsage();
  
  public abstract int maxUsage();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolChunkListMetric
 * JD-Core Version:    0.7.0.1
 */