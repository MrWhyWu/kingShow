package io.opentracing;

import java.io.Closeable;

public abstract interface Scope
  extends Closeable
{
  public abstract void close();
  
  public abstract Span span();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.Scope
 * JD-Core Version:    0.7.0.1
 */