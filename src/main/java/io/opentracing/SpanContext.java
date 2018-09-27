package io.opentracing;

import java.util.Map.Entry;

public abstract interface SpanContext
{
  public abstract Iterable<Map.Entry<String, String>> baggageItems();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.SpanContext
 * JD-Core Version:    0.7.0.1
 */