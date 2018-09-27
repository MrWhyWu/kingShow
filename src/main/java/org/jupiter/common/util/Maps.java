package org.jupiter.common.util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jupiter.common.concurrent.collection.NonBlockingHashMap;
import org.jupiter.common.concurrent.collection.NonBlockingHashMapLong;

























public final class Maps
{
  private static final boolean USE_NON_BLOCKING_HASH = SystemPropertyUtil.getBoolean("jupiter.use.non_blocking_hash", false);
  


  public static <K, V> HashMap<K, V> newHashMap()
  {
    return new HashMap();
  }
  



  public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize)
  {
    return new HashMap(capacity(expectedSize));
  }
  


  public static <K, V> IdentityHashMap<K, V> newIdentityHashMap()
  {
    return new IdentityHashMap();
  }
  



  public static <K, V> IdentityHashMap<K, V> newIdentityHashMapWithExpectedSize(int expectedSize)
  {
    return new IdentityHashMap(capacity(expectedSize));
  }
  


  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap()
  {
    return new LinkedHashMap();
  }
  


  public static <K extends Comparable, V> TreeMap<K, V> newTreeMap()
  {
    return new TreeMap();
  }
  


  public static <K, V> ConcurrentMap<K, V> newConcurrentMap()
  {
    if (USE_NON_BLOCKING_HASH) {
      return new NonBlockingHashMap();
    }
    return new ConcurrentHashMap();
  }
  



  public static <K, V> ConcurrentMap<K, V> newConcurrentMap(int initialCapacity)
  {
    if (USE_NON_BLOCKING_HASH) {
      return new NonBlockingHashMap(initialCapacity);
    }
    return new ConcurrentHashMap(initialCapacity);
  }
  


  public static <V> ConcurrentMap<Long, V> newConcurrentMapLong()
  {
    return new NonBlockingHashMapLong();
  }
  



  public static <V> ConcurrentMap<Long, V> newConcurrentMapLong(int initialCapacity)
  {
    return new NonBlockingHashMapLong(initialCapacity);
  }
  




  private static int capacity(int expectedSize)
  {
    if (expectedSize < 3) {
      Preconditions.checkArgument(expectedSize >= 0, "expectedSize cannot be negative but was: " + expectedSize);
      return expectedSize + 1;
    }
    if (expectedSize < 1073741824) {
      return expectedSize + expectedSize / 3;
    }
    return Integer.MAX_VALUE;
  }
  
  private Maps() {}
}
