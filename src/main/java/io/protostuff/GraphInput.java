package io.protostuff;

public abstract interface GraphInput
  extends Input
{
  public abstract void updateLast(Object paramObject1, Object paramObject2);
  
  public abstract boolean isCurrentMessageReference();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.GraphInput
 * JD-Core Version:    0.7.0.1
 */