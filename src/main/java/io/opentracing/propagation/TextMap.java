package io.opentracing.propagation;

import java.util.Iterator;
import java.util.Map.Entry;

public abstract interface TextMap
  extends Iterable<Map.Entry<String, String>>
{
  public abstract Iterator<Map.Entry<String, String>> iterator();
  
  public abstract void put(String paramString1, String paramString2);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.propagation.TextMap
 * JD-Core Version:    0.7.0.1
 */