package org.jupiter.transport.channel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.jupiter.common.util.internal.UnsafeUtil;





























public class CopyOnWriteGroupList
{
  private static final JChannelGroup[] EMPTY_GROUP = new JChannelGroup[0];
  private static final Object[] EMPTY_ARRAY = { EMPTY_GROUP, null };
  
  private final transient ReentrantLock lock = new ReentrantLock();
  
  private final DirectoryJChannelGroup parent;
  
  private volatile transient Object[] array;
  

  public CopyOnWriteGroupList(DirectoryJChannelGroup parent)
  {
    this.parent = parent;
    setArray(EMPTY_ARRAY);
  }
  
  public final JChannelGroup[] getSnapshot() {
    return tabAt0(array);
  }
  
  public final Object getWeightArray(JChannelGroup[] snapshot, String directory)
  {
    Object[] array = this.array;
    return tabAt1(array) == null ? null : tabAt0(array) != snapshot ? null : tabAt1(array).get(directory);
  }
  

  public final boolean setWeightArray(JChannelGroup[] snapshot, String directory, Object weightArray)
  {
    if ((weightArray == null) || (snapshot != tabAt0(array))) {
      return false;
    }
    ReentrantLock lock = this.lock;
    boolean locked = lock.tryLock();
    if (locked) {
      try { boolean bool1;
        if (snapshot != tabAt0(array)) {
          return false;
        }
        setWeightArray(directory, weightArray);
        return true;
      } finally {
        lock.unlock();
      }
    }
    return false;
  }
  
  private void setArray(Object[] array) {
    this.array = array;
  }
  
  private void setArray(JChannelGroup[] groups, Object weightArray) {
    array = new Object[] { groups, weightArray };
  }
  
  private void setWeightArray(String directory, Object weightArray)
  {
    Map<String, Object> weightsMap = tabAt1(array);
    if (weightsMap == null) {
      weightsMap = new HashMap();
      setTabAt(array, 1, weightsMap);
    }
    weightsMap.put(directory, weightArray);
  }
  
  public int size() {
    return tabAt0(array).length;
  }
  
  public boolean isEmpty() {
    return size() == 0;
  }
  
  public boolean contains(JChannelGroup o) {
    JChannelGroup[] elements = tabAt0(array);
    return indexOf(o, elements, 0, elements.length) >= 0;
  }
  
  public int indexOf(JChannelGroup o) {
    JChannelGroup[] elements = tabAt0(array);
    return indexOf(o, elements, 0, elements.length);
  }
  
  public int indexOf(JChannelGroup o, int index) {
    JChannelGroup[] elements = tabAt0(array);
    return indexOf(o, elements, index, elements.length);
  }
  
  public JChannelGroup[] toArray() {
    JChannelGroup[] elements = tabAt0(array);
    return (JChannelGroup[])Arrays.copyOf(elements, elements.length);
  }
  
  private JChannelGroup get(JChannelGroup[] array, int index) {
    return array[index];
  }
  
  public JChannelGroup get(int index) {
    return get(tabAt0(array), index);
  }
  












  public boolean remove(JChannelGroup o)
  {
    JChannelGroup[] snapshot = tabAt0(array);
    int index = indexOf(o, snapshot, 0, snapshot.length);
    return (index >= 0) && (remove(o, snapshot, index));
  }
  



  private boolean remove(JChannelGroup o, JChannelGroup[] snapshot, int index)
  {
    ReentrantLock lock = this.lock;
    lock.lock();
    try {
      JChannelGroup[] current = tabAt0(array);
      int len = current.length;
      int i; if (snapshot != current) {
        int prefix = Math.min(index, len);
        for (i = 0; i < prefix; i++) {
          if ((current[i] != snapshot[i]) && (eq(o, current[i]))) {
            index = i;
            break label140;
          }
        }
        if (index >= len) {
          return 0;
        }
        if (current[index] != o)
        {

          index = indexOf(o, current, index, len);
          if (index < 0)
            return 0;
        } }
      label140:
      JChannelGroup[] newElements = new JChannelGroup[len - 1];
      System.arraycopy(current, 0, newElements, 0, index);
      System.arraycopy(current, index + 1, newElements, index, len - index - 1);
      setArray(newElements, null);
      parent.decrementRefCount(o);
      return 1;
    } finally {
      lock.unlock();
    }
  }
  





  public boolean addIfAbsent(JChannelGroup o)
  {
    JChannelGroup[] snapshot = tabAt0(array);
    return (indexOf(o, snapshot, 0, snapshot.length) < 0) && (addIfAbsent(o, snapshot));
  }
  



  private boolean addIfAbsent(JChannelGroup o, JChannelGroup[] snapshot)
  {
    ReentrantLock lock = this.lock;
    lock.lock();
    try {
      JChannelGroup[] current = tabAt0(array);
      int len = current.length;
      int i; if (snapshot != current)
      {
        int common = Math.min(snapshot.length, len);
        for (i = 0; i < common; i++) {
          if ((current[i] != snapshot[i]) && (eq(o, current[i]))) {
            return false;
          }
        }
        if (indexOf(o, current, common, len) >= 0) {
          return 0;
        }
      }
      JChannelGroup[] newElements = (JChannelGroup[])Arrays.copyOf(current, len + 1);
      newElements[len] = o;
      setArray(newElements, null);
      parent.incrementRefCount(o);
      return 1;
    } finally {
      lock.unlock();
    }
  }
  
  public boolean containsAll(Collection<? extends JChannelGroup> c) {
    JChannelGroup[] elements = tabAt0(array);
    int len = elements.length;
    for (JChannelGroup e : c) {
      if (indexOf(e, elements, 0, len) < 0) {
        return false;
      }
    }
    return true;
  }
  
  void clear() {
    ReentrantLock lock = this.lock;
    lock.lock();
    try {
      setArray(EMPTY_ARRAY);
    } finally {
      lock.unlock();
    }
  }
  
  public String toString()
  {
    return Arrays.toString(tabAt0(array));
  }
  
  public boolean equals(Object o)
  {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CopyOnWriteGroupList)) {
      return false;
    }
    
    CopyOnWriteGroupList other = (CopyOnWriteGroupList)o;
    
    JChannelGroup[] elements = tabAt0(array);
    JChannelGroup[] otherElements = tabAt0(array);
    int len = elements.length;
    int otherLen = otherElements.length;
    
    if (len != otherLen) {
      return false;
    }
    
    for (int i = 0; i < len; i++) {
      if (!eq(elements[i], otherElements[i])) {
        return false;
      }
    }
    return true;
  }
  

  public int hashCode()
  {
    int hashCode = 1;
    JChannelGroup[] elements = tabAt0(array);
    int i = 0; for (int len = elements.length; i < len; i++) {
      JChannelGroup o = elements[i];
      hashCode = 31 * hashCode + (o == null ? 0 : o.hashCode());
    }
    return hashCode;
  }
  
  private boolean eq(JChannelGroup o1, JChannelGroup o2) {
    return o1 == null ? false : o2 == null ? true : o1.equals(o2);
  }
  
  private int indexOf(JChannelGroup o, JChannelGroup[] elements, int index, int fence) {
    if (o == null) {
      for (int i = index; i < fence; i++) {
        if (elements[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = index; i < fence; i++) {
        if (o.equals(elements[i])) {
          return i;
        }
      }
    }
    return -1;
  }
  
  private static JChannelGroup[] tabAt0(Object[] array) {
    return (JChannelGroup[])tabAt(array, 0);
  }
  
  private static Map<String, Object> tabAt1(Object[] array)
  {
    return (Map)tabAt(array, 1);
  }
  
  private static Object tabAt(Object[] array, int index) {
    return UnsafeUtil.getObjectVolatile(array, index);
  }
  
  private static void setTabAt(Object[] array, int index, Object value) {
    UnsafeUtil.putObjectVolatile(array, index, value);
  }
}
