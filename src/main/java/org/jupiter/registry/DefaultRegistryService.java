package org.jupiter.registry;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.SpiMetadata;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnectionManager;
import org.jupiter.transport.UnresolvedAddress;


























@SpiMetadata(name="default")
public class DefaultRegistryService
  extends AbstractRegistryService
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistryService.class);
  
  private final ConcurrentMap<UnresolvedAddress, DefaultRegistry> clients = Maps.newConcurrentMap();
  
  public DefaultRegistryService() {}
  
  protected void doSubscribe(RegisterMeta.ServiceMeta serviceMeta) { Collection<DefaultRegistry> allClients = clients.values();
    Preconditions.checkArgument(!allClients.isEmpty(), "init needed");
    
    logger.info("Subscribe: {}.", serviceMeta);
    
    for (DefaultRegistry c : allClients) {
      c.doSubscribe(serviceMeta);
    }
  }
  
  protected void doRegister(RegisterMeta meta)
  {
    Collection<DefaultRegistry> allClients = clients.values();
    Preconditions.checkArgument(!allClients.isEmpty(), "init needed");
    
    logger.info("Register: {}.", meta);
    
    for (DefaultRegistry c : allClients) {
      c.doRegister(meta);
    }
    getRegisterMetaMap().put(meta, RegistryService.RegisterState.DONE);
  }
  

  protected void doUnregister(RegisterMeta meta)
  {
    Collection<DefaultRegistry> allClients = clients.values();
    Preconditions.checkArgument(!allClients.isEmpty(), "init needed");
    
    logger.info("Unregister: {}.", meta);
    
    for (DefaultRegistry c : allClients) {
      c.doUnregister(meta);
    }
  }
  


  protected void doCheckRegisterNodeStatus() {}
  

  public void connectToRegistryServer(String connectString)
  {
    Preconditions.checkNotNull(connectString, "connectString");
    
    String[] array = Strings.split(connectString, ',');
    for (String s : array) {
      String[] addressStr = Strings.split(s, ':');
      String host = addressStr[0];
      int port = Integer.parseInt(addressStr[1]);
      UnresolvedAddress address = new UnresolvedAddress(host, port);
      DefaultRegistry client = (DefaultRegistry)clients.get(address);
      if (client == null) {
        DefaultRegistry newClient = new DefaultRegistry(this);
        client = (DefaultRegistry)clients.putIfAbsent(address, newClient);
        if (client == null) {
          client = newClient;
          JConnection connection = client.connect(address);
          client.connectionManager().manage(connection);
        } else {
          newClient.shutdownGracefully();
        }
      }
    }
  }
  
  public void destroy()
  {
    for (DefaultRegistry c : clients.values()) {
      c.shutdownGracefully();
    }
  }
}
