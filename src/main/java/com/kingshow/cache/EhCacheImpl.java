package com.kingshow.cache;

import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;

public class EhCacheImpl implements CacheInter
{
  public static final Logger logger = org.slf4j.LoggerFactory.getLogger(EhCacheImpl.class);
  
  private static EhCacheImpl uniqueInstance;
  
  CacheManager cacheManager;
  
  Cache cache;
  private static final String cacheName = "deviceSession";
  
  private EhCacheImpl()
  {
    cacheManager = CacheManager.create();
    cacheManager.addCache("deviceSession");
    cache = cacheManager.getCache("deviceSession");
  }
  
  public static EhCacheImpl getInstance() {
    return uniqueInstance;
  }
  
  public static EhCacheImpl newInstance() {
    uniqueInstance = new EhCacheImpl();
    return uniqueInstance;
  }
  
  public void add(String key, Object value, int expiration)
  {
    if (cache.get(key) != null) {
      return;
    }
    Element element = new Element(key, value);
    element.setTimeToLive(expiration);
    cache.put(element);
  }
  
  public void clear()
  {
    cache.removeAll();
  }
  
  public synchronized long decr(String key, int by)
  {
    Element e = cache.get(key);
    if (e == null) {
      return -1L;
    }
    long newValue = ((Number)e.getValue()).longValue() - by;
    Element newE = new Element(key, Long.valueOf(newValue));
    newE.setTimeToLive(e.getTimeToLive());
    cache.put(newE);
    return newValue;
  }
  
  public void delete(String key)
  {
    cache.remove(key);
  }
  
  public Object get(String key)
  {
    Element e = cache.get(key);
    return e == null ? null : e.getValue();
  }
  
  public Map<String, Object> get(String[] keys)
  {
    Map<String, Object> result = new java.util.HashMap(keys.length);
    for (String key : keys) {
      result.put(key, get(key));
    }
    return result;
  }
  
  public synchronized long incr(String key, int by)
  {
    Element e = cache.get(key);
    if (e == null) {
      return -1L;
    }
    long newValue = ((Number)e.getValue()).longValue() + by;
    Element newE = new Element(key, Long.valueOf(newValue));
    newE.setTimeToLive(e.getTimeToLive());
    cache.put(newE);
    return newValue;
  }
  

  public void replace(String key, Object value, int expiration)
  {
    if (cache.get(key) == null) {
      return;
    }
    Element element = new Element(key, value);
    element.setTimeToLive(expiration);
    cache.put(element);
  }
  
  public boolean safeAdd(String key, Object value, int expiration)
  {
    try {
      add(key, value, expiration);
      return true;
    } catch (Exception e) {}
    return false;
  }
  
  public boolean safeDelete(String key)
  {
    try
    {
      delete(key);
      return true;
    } catch (Exception e) {
      logger.error(e.toString()); }
    return false;
  }
  
  public boolean safeReplace(String key, Object value, int expiration)
  {
    try
    {
      replace(key, value, expiration);
      return true;
    } catch (Exception e) {
      logger.error(e.toString()); }
    return false;
  }
  
  public boolean safeSet(String key, Object value, int expiration)
  {
    try
    {
      set(key, value, expiration);
      return true;
    } catch (Exception e) {
      logger.error(e.toString()); }
    return false;
  }
  

  public void set(String key, Object value, int expiration)
  {
    Element element = new Element(key, value);
    element.setTimeToLive(expiration);
    cache.put(element);
  }
  
  public void stop()
  {
    cacheManager.shutdown();
  }
}
