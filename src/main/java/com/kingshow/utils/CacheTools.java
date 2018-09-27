package com.kingshow.utils;

import java.util.concurrent.ConcurrentHashMap;

public class CacheTools {
  private ConcurrentHashMap<String, String> clazzActionMap;
  private ConcurrentHashMap<String, String> clazzBaseMap;
  private ConcurrentHashMap<String, Class> serviceMap;
  
  public CacheTools() { clazzActionMap = new ConcurrentHashMap();
    clazzBaseMap = new ConcurrentHashMap();
    serviceMap = new ConcurrentHashMap();
  }
  
  private static class SingletonHolder
  {
    static CacheTools instance = new CacheTools();
    
    private SingletonHolder() {} }
  
  public static CacheTools getInstance() { return SingletonHolder.instance; }
  








  public ConcurrentHashMap<String, String> getClazzActionMap()
  {
    return clazzActionMap;
  }
  
  public ConcurrentHashMap<String, String> getClazzBaseMap() {
    return clazzBaseMap;
  }
  
  public ConcurrentHashMap<String, Class> getServiceMap() {
    return serviceMap;
  }
}
