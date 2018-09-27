package io.netty.util.internal;

import java.util.Queue;

public abstract interface PriorityQueue<T>
  extends Queue<T>
{
  public abstract boolean removeTyped(T paramT);
  
  public abstract boolean containsTyped(T paramT);
  
  public abstract void priorityChanged(T paramT);
  
  public abstract void clearIgnoringIndexes();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.PriorityQueue
 * JD-Core Version:    0.7.0.1
 */