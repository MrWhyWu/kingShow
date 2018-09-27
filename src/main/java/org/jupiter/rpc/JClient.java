package org.jupiter.rpc;

import java.util.Collection;
import org.jupiter.registry.NotifyListener;
import org.jupiter.registry.OfflineListener;
import org.jupiter.registry.RegisterMeta;
import org.jupiter.registry.Registry;
import org.jupiter.registry.RegistryService;
import org.jupiter.transport.Directory;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.JConnector.ConnectionWatcher;
import org.jupiter.transport.UnresolvedAddress;

public abstract interface JClient
  extends Registry
{
  public abstract String appName();
  
  public abstract JConnector<JConnection> connector();
  
  public abstract JClient withConnector(JConnector<JConnection> paramJConnector);
  
  public abstract RegistryService registryService();
  
  public abstract Collection<RegisterMeta> lookup(Directory paramDirectory);
  
  public abstract JConnector.ConnectionWatcher watchConnections(Class<?> paramClass);
  
  public abstract JConnector.ConnectionWatcher watchConnections(Class<?> paramClass, String paramString);
  
  public abstract JConnector.ConnectionWatcher watchConnections(Directory paramDirectory);
  
  public abstract boolean awaitConnections(Class<?> paramClass, long paramLong);
  
  public abstract boolean awaitConnections(Class<?> paramClass, String paramString, long paramLong);
  
  public abstract boolean awaitConnections(Directory paramDirectory, long paramLong);
  
  public abstract void subscribe(Directory paramDirectory, NotifyListener paramNotifyListener);
  
  public abstract void offlineListening(UnresolvedAddress paramUnresolvedAddress, OfflineListener paramOfflineListener);
  
  public abstract void shutdownGracefully();
}
