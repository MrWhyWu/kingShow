package io.netty.util.collection;

import java.util.Map;

public abstract interface ByteObjectMap<V>
  extends Map<Byte, V>
{
  public abstract V get(byte paramByte);
  
  public abstract V put(byte paramByte, V paramV);
  
  public abstract V remove(byte paramByte);
  
  public abstract Iterable<PrimitiveEntry<V>> entries();
  
  public abstract boolean containsKey(byte paramByte);
  
  public static abstract interface PrimitiveEntry<V>
  {
    public abstract byte key();
    
    public abstract V value();
    
    public abstract void setValue(V paramV);
  }
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.collection.ByteObjectMap
 * JD-Core Version:    0.7.0.1
 */