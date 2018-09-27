package org.jupiter.registry;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.jupiter.common.concurrent.collection.ConcurrentSet;
import org.jupiter.common.util.Lists;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Preconditions;




























public class RegisterInfoContext
{
  private final ConcurrentMap<RegisterMeta.ServiceMeta, ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>>> globalRegisterInfoMap = Maps.newConcurrentMap();
  

  private final ConcurrentMap<RegisterMeta.Address, ConcurrentSet<RegisterMeta.ServiceMeta>> globalServiceMetaMap = Maps.newConcurrentMap();
  
  public RegisterInfoContext() {}
  
  public ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> getRegisterMeta(RegisterMeta.ServiceMeta serviceMeta)
  {
    ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> config = (ConfigWithVersion)globalRegisterInfoMap.get(serviceMeta);
    
    if (config == null) {
      ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> newConfig = ConfigWithVersion.newInstance();
      
      newConfig.setConfig(Maps.newConcurrentMap());
      config = (ConfigWithVersion)globalRegisterInfoMap.putIfAbsent(serviceMeta, newConfig);
      if (config == null) {
        config = newConfig;
      }
    }
    return config;
  }
  
  public ConcurrentSet<RegisterMeta.ServiceMeta> getServiceMeta(RegisterMeta.Address address) {
    ConcurrentSet<RegisterMeta.ServiceMeta> serviceMetaSet = (ConcurrentSet)globalServiceMetaMap.get(address);
    if (serviceMetaSet == null) {
      ConcurrentSet<RegisterMeta.ServiceMeta> newServiceMetaSet = new ConcurrentSet();
      serviceMetaSet = (ConcurrentSet)globalServiceMetaMap.putIfAbsent(address, newServiceMetaSet);
      if (serviceMetaSet == null) {
        serviceMetaSet = newServiceMetaSet;
      }
    }
    return serviceMetaSet;
  }
  
  public Object publishLock(ConfigWithVersion<ConcurrentMap<RegisterMeta.Address, RegisterMeta>> config) {
    return Preconditions.checkNotNull(config, "publish lock");
  }
  

  public List<RegisterMeta.Address> listPublisherHosts()
  {
    return Lists.newArrayList(globalServiceMetaMap.keySet());
  }
  
  public List<RegisterMeta.Address> listAddressesByService(RegisterMeta.ServiceMeta serviceMeta) {
    return Lists.newArrayList(((ConcurrentMap)getRegisterMeta(serviceMeta).getConfig()).keySet());
  }
  
  public List<RegisterMeta.ServiceMeta> listServicesByAddress(RegisterMeta.Address address) {
    return Lists.newArrayList(getServiceMeta(address));
  }
}
