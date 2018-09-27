package org.jupiter.registry;

import java.util.Collection;
import java.util.Map;


















































public abstract interface RegistryService
  extends Registry
{
  public abstract void register(RegisterMeta paramRegisterMeta);
  
  public abstract void unregister(RegisterMeta paramRegisterMeta);
  
  public abstract void subscribe(RegisterMeta.ServiceMeta paramServiceMeta, NotifyListener paramNotifyListener);
  
  public abstract Collection<RegisterMeta> lookup(RegisterMeta.ServiceMeta paramServiceMeta);
  
  public abstract Map<RegisterMeta.ServiceMeta, Integer> consumers();
  
  public abstract Map<RegisterMeta, RegisterState> providers();
  
  public abstract boolean isShutdown();
  
  public abstract void shutdownGracefully();
  
  public static enum RegistryType
  {
    DEFAULT("default"), 
    ZOOKEEPER("zookeeper");
    
    private final String value;
    
    private RegistryType(String value) {
      this.value = value;
    }
    
    public String getValue() {
      return value;
    }
    
    public static RegistryType parse(String name) {
      for (RegistryType s : ) {
        if (s.name().equalsIgnoreCase(name)) {
          return s;
        }
      }
      return null;
    }
  }
  
  public static enum RegisterState {
    PREPARE, 
    DONE;
    
    private RegisterState() {}
  }
}
