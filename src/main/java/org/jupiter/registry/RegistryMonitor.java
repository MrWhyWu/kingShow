package org.jupiter.registry;

import java.util.List;

public abstract interface RegistryMonitor
{
  public abstract List<String> listPublisherHosts();
  
  public abstract List<String> listSubscriberAddresses();
  
  public abstract List<String> listAddressesByService(String paramString1, String paramString2, String paramString3);
  
  public abstract List<String> listServicesByAddress(String paramString, int paramInt);
}
