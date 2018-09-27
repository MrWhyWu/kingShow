package io.netty.util.internal;

public abstract interface PriorityQueueNode
{
  public static final int INDEX_NOT_IN_QUEUE = -1;
  
  public abstract int priorityQueueIndex(DefaultPriorityQueue<?> paramDefaultPriorityQueue);
  
  public abstract void priorityQueueIndex(DefaultPriorityQueue<?> paramDefaultPriorityQueue, int paramInt);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.PriorityQueueNode
 * JD-Core Version:    0.7.0.1
 */