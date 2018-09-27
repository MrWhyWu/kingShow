package org.jupiter.common.concurrent.collection;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.jupiter.common.util.Maps;
























public final class ConcurrentSet<E>
  extends AbstractSet<E>
  implements Serializable
{
  private static final long serialVersionUID = -6761513279741915432L;
  private final ConcurrentMap<E, Boolean> map;
  
  public ConcurrentSet()
  {
    map = Maps.newConcurrentMap();
  }
  
  public int size()
  {
    return map.size();
  }
  

  public boolean contains(Object o)
  {
    return map.containsKey(o);
  }
  
  public boolean add(E o)
  {
    return map.putIfAbsent(o, Boolean.TRUE) == null;
  }
  
  public boolean remove(Object o)
  {
    return map.remove(o) != null;
  }
  
  public void clear()
  {
    map.clear();
  }
  
  public Iterator<E> iterator()
  {
    return map.keySet().iterator();
  }
}
