package org.jupiter.common.util.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.jupiter.common.util.ThrowUtil;
























public class InternalThreadLocal<V>
{
  private static final int variablesToRemoveIndex = ;
  

  private final int index;
  


  public static void removeAll()
  {
    InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
    if (threadLocalMap == null) {
      return;
    }
    try
    {
      Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
      if ((v != null) && (v != InternalThreadLocalMap.UNSET)) {
        Set<InternalThreadLocal<?>> variablesToRemove = (Set)v;
        for (InternalThreadLocal<?> tlv : variablesToRemove) {
          tlv.remove(threadLocalMap);
        }
      }
    } finally {
      InternalThreadLocalMap.remove();
    }
  }
  


  public static int size()
  {
    InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
    if (threadLocalMap == null) {
      return 0;
    }
    return threadLocalMap.size();
  }
  


  public static void destroy() {}
  

  private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, InternalThreadLocal<?> variable)
  {
    Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
    Set<InternalThreadLocal<?>> variablesToRemove;
    if ((v == InternalThreadLocalMap.UNSET) || (v == null)) {
      Set<InternalThreadLocal<?>> variablesToRemove = Collections.newSetFromMap(new IdentityHashMap());
      threadLocalMap.setIndexedVariable(variablesToRemoveIndex, variablesToRemove);
    } else {
      variablesToRemove = (Set)v;
    }
    
    variablesToRemove.add(variable);
  }
  

  private static void removeFromVariablesToRemove(InternalThreadLocalMap threadLocalMap, InternalThreadLocal<?> variable)
  {
    Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
    
    if ((v == InternalThreadLocalMap.UNSET) || (v == null)) {
      return;
    }
    
    Set<InternalThreadLocal<?>> variablesToRemove = (Set)v;
    variablesToRemove.remove(variable);
  }
  

  public InternalThreadLocal()
  {
    index = InternalThreadLocalMap.nextVariableIndex();
  }
  



  public final V get()
  {
    InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
    Object v = threadLocalMap.indexedVariable(index);
    if (v != InternalThreadLocalMap.UNSET) {
      return v;
    }
    
    return initialize(threadLocalMap);
  }
  
  private V initialize(InternalThreadLocalMap threadLocalMap) {
    V v = null;
    try {
      v = initialValue();
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    
    threadLocalMap.setIndexedVariable(index, v);
    addToVariablesToRemove(threadLocalMap, this);
    return v;
  }
  


  public final void set(V value)
  {
    if ((value == null) || (value == InternalThreadLocalMap.UNSET)) {
      remove();
    } else {
      InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
      if (threadLocalMap.setIndexedVariable(index, value)) {
        addToVariablesToRemove(threadLocalMap, this);
      }
    }
  }
  



  public final void remove()
  {
    remove(InternalThreadLocalMap.getIfSet());
  }
  





  public final void remove(InternalThreadLocalMap threadLocalMap)
  {
    if (threadLocalMap == null) {
      return;
    }
    
    Object v = threadLocalMap.removeIndexedVariable(index);
    removeFromVariablesToRemove(threadLocalMap, this);
    
    if (v != InternalThreadLocalMap.UNSET) {
      try {
        onRemoval(v);
      } catch (Exception e) {
        ThrowUtil.throwException(e);
      }
    }
  }
  

  protected V initialValue()
    throws Exception
  {
    return null;
  }
  
  protected void onRemoval(V value)
    throws Exception
  {}
}
