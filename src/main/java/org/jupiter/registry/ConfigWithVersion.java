package org.jupiter.registry;

import java.util.concurrent.atomic.AtomicLong;
























public class ConfigWithVersion<T>
{
  public static <T> ConfigWithVersion<T> newInstance()
  {
    return new ConfigWithVersion();
  }
  


  private AtomicLong version = new AtomicLong(0L);
  
  private ConfigWithVersion() {}
  
  public long getVersion() { return version.get(); }
  
  private T config;
  public long newVersion() {
    return version.incrementAndGet();
  }
  
  public T getConfig() {
    return config;
  }
  
  public void setConfig(T config) {
    this.config = config;
  }
}
