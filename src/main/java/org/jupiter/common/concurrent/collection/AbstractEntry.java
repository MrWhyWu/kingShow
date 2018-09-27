package org.jupiter.common.concurrent.collection;

import java.util.Map.Entry;




























abstract class AbstractEntry<TypeK, TypeV>
  implements Map.Entry<TypeK, TypeV>
{
  protected final TypeK _key;
  protected TypeV _val;
  
  public AbstractEntry(TypeK key, TypeV val)
  {
    _key = key;
    _val = val;
  }
  
  public AbstractEntry(Map.Entry<TypeK, TypeV> e) {
    _key = e.getKey();
    _val = e.getValue();
  }
  


  public String toString()
  {
    return _key + "=" + _val;
  }
  


  public TypeK getKey()
  {
    return _key;
  }
  


  public TypeV getValue()
  {
    return _val;
  }
  


  public boolean equals(Object o)
  {
    if (!(o instanceof Map.Entry)) return false;
    Map.Entry e = (Map.Entry)o;
    return (eq(_key, e.getKey())) && (eq(_val, e.getValue()));
  }
  


  public int hashCode()
  {
    return (_key == null ? 0 : _key.hashCode()) ^ (_val == null ? 0 : _val.hashCode());
  }
  

  private static boolean eq(Object o1, Object o2)
  {
    return o1 == null ? false : o2 == null ? true : o1.equals(o2);
  }
}
