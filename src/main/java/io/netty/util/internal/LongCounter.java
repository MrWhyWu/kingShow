package io.netty.util.internal;

public abstract interface LongCounter
{
  public abstract void add(long paramLong);
  
  public abstract void increment();
  
  public abstract void decrement();
  
  public abstract long value();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.LongCounter
 * JD-Core Version:    0.7.0.1
 */