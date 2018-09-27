package org.jupiter.common.util.internal;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.jupiter.common.util.SystemPropertyUtil;
























































public final class InternalThreadLocalMap
  extends RhsPadding
{
  private static final UnsafeReferenceFieldUpdater<StringBuilder, char[]> stringBuilderValueUpdater = UnsafeUpdater.newReferenceFieldUpdater(StringBuilder.class.getSuperclass(), "value");
  

  private static final int DEFAULT_STRING_BUILDER_MAX_CAPACITY = SystemPropertyUtil.getInt("jupiter.internal.thread.local.string_builder_max_capacity", 65536);
  
  private static final int DEFAULT_STRING_BUILDER_INITIAL_CAPACITY = SystemPropertyUtil.getInt("jupiter.internal.thread.local.string_builder_initial_capacity", 512);
  

  private static final ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = new ThreadLocal();
  private static final AtomicInteger nextIndex = new AtomicInteger();
  
  public static final Object UNSET = new Object();
  
  public static InternalThreadLocalMap getIfSet() {
    Thread thread = Thread.currentThread();
    if ((thread instanceof InternalThread)) {
      return ((InternalThread)thread).threadLocalMap();
    }
    if ((thread instanceof InternalForkJoinWorkerThread)) {
      return ((InternalForkJoinWorkerThread)thread).threadLocalMap();
    }
    return (InternalThreadLocalMap)slowThreadLocalMap.get();
  }
  
  public static InternalThreadLocalMap get() {
    Thread thread = Thread.currentThread();
    if ((thread instanceof InternalThread)) {
      return fastGet((InternalThread)thread);
    }
    if ((thread instanceof InternalForkJoinWorkerThread)) {
      return fastGet((InternalForkJoinWorkerThread)thread);
    }
    return slowGet();
  }
  
  public static void remove() {
    Thread thread = Thread.currentThread();
    if ((thread instanceof InternalThread)) {
      ((InternalThread)thread).setThreadLocalMap(null);
    } else if ((thread instanceof InternalForkJoinWorkerThread)) {
      ((InternalForkJoinWorkerThread)thread).setThreadLocalMap(null);
    } else {
      slowThreadLocalMap.remove();
    }
  }
  
  public static void destroy() {
    slowThreadLocalMap.remove();
  }
  
  public static int nextVariableIndex() {
    int index = nextIndex.getAndIncrement();
    if (index < 0) {
      nextIndex.decrementAndGet();
      throw new IllegalStateException("Too many thread-local indexed variables");
    }
    return index;
  }
  
  public static int lastVariableIndex() {
    return nextIndex.get() - 1;
  }
  
  private InternalThreadLocalMap() {
    indexedVariables = newIndexedVariableTable();
  }
  
  public Object indexedVariable(int index) {
    Object[] lookup = indexedVariables;
    return index < lookup.length ? lookup[index] : UNSET;
  }
  


  public boolean setIndexedVariable(int index, Object value)
  {
    Object[] lookup = indexedVariables;
    if (index < lookup.length) {
      Object oldValue = lookup[index];
      lookup[index] = value;
      return oldValue == UNSET;
    }
    expandIndexedVariableTableAndSet(index, value);
    return true;
  }
  
  public Object removeIndexedVariable(int index)
  {
    Object[] lookup = indexedVariables;
    if (index < lookup.length) {
      Object v = lookup[index];
      lookup[index] = UNSET;
      return v;
    }
    return UNSET;
  }
  
  public int size()
  {
    int count = 0;
    for (Object o : indexedVariables) {
      if (o != UNSET) {
        count++;
      }
    }
    return count;
  }
  
  public StringBuilder stringBuilder() {
    StringBuilder builder = stringBuilder;
    if (builder == null) {
      stringBuilder = (builder = new StringBuilder(DEFAULT_STRING_BUILDER_INITIAL_CAPACITY));
    } else {
      if (builder.capacity() > DEFAULT_STRING_BUILDER_MAX_CAPACITY)
      {
        stringBuilderValueUpdater.set(builder, new char[DEFAULT_STRING_BUILDER_INITIAL_CAPACITY]);
      }
      builder.setLength(0);
    }
    return builder;
  }
  
  private static Object[] newIndexedVariableTable() {
    Object[] array = new Object[32];
    Arrays.fill(array, UNSET);
    return array;
  }
  
  private static InternalThreadLocalMap fastGet(InternalThread thread) {
    InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
    if (threadLocalMap == null) {
      thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
    }
    return threadLocalMap;
  }
  
  private static InternalThreadLocalMap fastGet(InternalForkJoinWorkerThread thread) {
    InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
    if (threadLocalMap == null) {
      thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
    }
    return threadLocalMap;
  }
  
  private static InternalThreadLocalMap slowGet() {
    ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = slowThreadLocalMap;
    InternalThreadLocalMap ret = (InternalThreadLocalMap)slowThreadLocalMap.get();
    if (ret == null) {
      ret = new InternalThreadLocalMap();
      slowThreadLocalMap.set(ret);
    }
    return ret;
  }
  
  private void expandIndexedVariableTableAndSet(int index, Object value) {
    Object[] oldArray = indexedVariables;
    int oldCapacity = oldArray.length;
    int newCapacity = index;
    newCapacity |= newCapacity >>> 1;
    newCapacity |= newCapacity >>> 2;
    newCapacity |= newCapacity >>> 4;
    newCapacity |= newCapacity >>> 8;
    newCapacity |= newCapacity >>> 16;
    newCapacity++;
    
    Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
    Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
    newArray[index] = value;
    indexedVariables = newArray;
  }
}
