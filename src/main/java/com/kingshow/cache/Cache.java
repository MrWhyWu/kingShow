package com.kingshow.cache;

import com.kingshow.utils.Time;
import java.util.Map;

public class Cache
{
  public static CacheInter cacheInter;
  public static CacheInter forcedCacheInter;
  
  public Cache() {}
  
  public static void add(String key, Object value, String expiration)
  {
    checkSerializable(value);
    cacheInter.add(key, value, Time.parseDuration(expiration));
  }
  






  public static boolean safeAdd(String key, Object value, String expiration)
  {
    checkSerializable(value);
    return cacheInter.safeAdd(key, value, Time.parseDuration(expiration));
  }
  




  public static void add(String key, Object value)
  {
    checkSerializable(value);
    cacheInter.add(key, value, Time.parseDuration(null));
  }
  





  public static void set(String key, Object value, String expiration)
  {
    checkSerializable(value);
    cacheInter.set(key, value, Time.parseDuration(expiration));
  }
  






  public static boolean safeSet(String key, Object value, String expiration)
  {
    checkSerializable(value);
    return cacheInter.safeSet(key, value, Time.parseDuration(expiration));
  }
  




  public static void set(String key, Object value)
  {
    checkSerializable(value);
    cacheInter.set(key, value, Time.parseDuration(null));
  }
  





  public static void replace(String key, Object value, String expiration)
  {
    checkSerializable(value);
    cacheInter.replace(key, value, Time.parseDuration(expiration));
  }
  






  public static boolean safeReplace(String key, Object value, String expiration)
  {
    checkSerializable(value);
    return cacheInter.safeReplace(key, value, Time.parseDuration(expiration));
  }
  




  public static void replace(String key, Object value)
  {
    checkSerializable(value);
    cacheInter.replace(key, value, Time.parseDuration(null));
  }
  





  public static long incr(String key, int by)
  {
    return cacheInter.incr(key, by);
  }
  




  public static long incr(String key)
  {
    return cacheInter.incr(key, 1);
  }
  





  public static long decr(String key, int by)
  {
    return cacheInter.decr(key, by);
  }
  




  public static long decr(String key)
  {
    return cacheInter.decr(key, 1);
  }
  




  public static Object get(String key)
  {
    return cacheInter.get(key);
  }
  




  public static Map<String, Object> get(String... key)
  {
    return cacheInter.get(key);
  }
  



  public static void delete(String key)
  {
    cacheInter.delete(key);
  }
  




  public static boolean safeDelete(String key)
  {
    return cacheInter.safeDelete(key);
  }
  


  public static void clear()
  {
    if (cacheInter != null) {
      cacheInter.clear();
    }
  }
  







  public static <T> T get(String key, Class<T> clazz)
  {
    return cacheInter.get(key);
  }
  


  public static void init()
  {
    if (forcedCacheInter != null) {
      cacheInter = forcedCacheInter;
      return;
    }
    cacheInter = EhCacheImpl.newInstance();
  }
  


  public static void stop()
  {
    cacheInter.stop();
  }
  




  static void checkSerializable(Object value)
  {
    if (value == null) {
      throw new RuntimeException("要加入缓存的元素不可以为空!");
    }
  }
}
