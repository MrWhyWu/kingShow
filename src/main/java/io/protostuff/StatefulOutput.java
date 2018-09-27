package io.protostuff;

public abstract interface StatefulOutput
  extends Output
{
  public abstract void updateLast(Schema<?> paramSchema1, Schema<?> paramSchema2);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.protostuff.StatefulOutput
 * JD-Core Version:    0.7.0.1
 */