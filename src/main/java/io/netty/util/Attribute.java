package io.netty.util;

public abstract interface Attribute<T>
{
  public abstract AttributeKey<T> key();
  
  public abstract T get();
  
  public abstract void set(T paramT);
  
  public abstract T getAndSet(T paramT);
  
  public abstract T setIfAbsent(T paramT);
  
  @Deprecated
  public abstract T getAndRemove();
  
  public abstract boolean compareAndSet(T paramT1, T paramT2);
  
  @Deprecated
  public abstract void remove();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.Attribute
 * JD-Core Version:    0.7.0.1
 */