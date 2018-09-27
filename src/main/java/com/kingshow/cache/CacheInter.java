package com.kingshow.cache;

import java.util.Map;

public abstract interface CacheInter
{
  public abstract void add(String paramString, Object paramObject, int paramInt);
  
  public abstract boolean safeAdd(String paramString, Object paramObject, int paramInt);
  
  public abstract void set(String paramString, Object paramObject, int paramInt);
  
  public abstract boolean safeSet(String paramString, Object paramObject, int paramInt);
  
  public abstract void replace(String paramString, Object paramObject, int paramInt);
  
  public abstract boolean safeReplace(String paramString, Object paramObject, int paramInt);
  
  public abstract Object get(String paramString);
  
  public abstract Map<String, Object> get(String[] paramArrayOfString);
  
  public abstract long incr(String paramString, int paramInt);
  
  public abstract long decr(String paramString, int paramInt);
  
  public abstract void clear();
  
  public abstract void delete(String paramString);
  
  public abstract boolean safeDelete(String paramString);
  
  public abstract void stop();
}
