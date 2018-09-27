package io.netty.util;

public abstract interface Constant<T extends Constant<T>>
  extends Comparable<T>
{
  public abstract int id();
  
  public abstract String name();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.Constant
 * JD-Core Version:    0.7.0.1
 */