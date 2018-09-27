package io.netty.util;

public abstract interface AttributeMap
{
  public abstract <T> Attribute<T> attr(AttributeKey<T> paramAttributeKey);
  
  public abstract <T> boolean hasAttr(AttributeKey<T> paramAttributeKey);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.AttributeMap
 * JD-Core Version:    0.7.0.1
 */