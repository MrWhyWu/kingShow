package io.netty.buffer;

import java.util.List;

public abstract interface PoolArenaMetric
{
  public abstract int numThreadCaches();
  
  public abstract int numTinySubpages();
  
  public abstract int numSmallSubpages();
  
  public abstract int numChunkLists();
  
  public abstract List<PoolSubpageMetric> tinySubpages();
  
  public abstract List<PoolSubpageMetric> smallSubpages();
  
  public abstract List<PoolChunkListMetric> chunkLists();
  
  public abstract long numAllocations();
  
  public abstract long numTinyAllocations();
  
  public abstract long numSmallAllocations();
  
  public abstract long numNormalAllocations();
  
  public abstract long numHugeAllocations();
  
  public abstract long numDeallocations();
  
  public abstract long numTinyDeallocations();
  
  public abstract long numSmallDeallocations();
  
  public abstract long numNormalDeallocations();
  
  public abstract long numHugeDeallocations();
  
  public abstract long numActiveAllocations();
  
  public abstract long numActiveTinyAllocations();
  
  public abstract long numActiveSmallAllocations();
  
  public abstract long numActiveNormalAllocations();
  
  public abstract long numActiveHugeAllocations();
  
  public abstract long numActiveBytes();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.PoolArenaMetric
 * JD-Core Version:    0.7.0.1
 */