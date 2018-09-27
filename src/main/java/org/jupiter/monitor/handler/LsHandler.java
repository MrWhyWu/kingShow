package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.Map.Entry;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.registry.RegisterMeta;
import org.jupiter.registry.RegisterMeta.ServiceMeta;
import org.jupiter.registry.RegistryService;
import org.jupiter.registry.RegistryService.RegisterState;





















public class LsHandler
  implements CommandHandler
{
  private volatile RegistryService serverRegisterService;
  private volatile RegistryService clientRegisterService;
  
  public LsHandler() {}
  
  public RegistryService getServerRegisterService()
  {
    return serverRegisterService;
  }
  
  public void setServerRegisterService(RegistryService serverRegisterService) {
    this.serverRegisterService = serverRegisterService;
  }
  
  public RegistryService getClientRegisterService() {
    return clientRegisterService;
  }
  
  public void setClientRegisterService(RegistryService clientRegisterService) {
    this.clientRegisterService = clientRegisterService;
  }
  
  public void handle(Channel channel, Command command, String... args)
  {
    if (AuthHandler.checkAuth(channel))
    {
      if (serverRegisterService != null) {
        channel.writeAndFlush("Provider side: " + JConstants.NEWLINE);
        channel.writeAndFlush("--------------------------------------------------------------------------------" + JConstants.NEWLINE);
        
        Map<RegisterMeta, RegistryService.RegisterState> providers = serverRegisterService.providers();
        for (Map.Entry<RegisterMeta, RegistryService.RegisterState> entry : providers.entrySet()) {
          channel.writeAndFlush(entry.getKey() + " | " + ((RegistryService.RegisterState)entry.getValue()).toString() + JConstants.NEWLINE);
        }
      }
      

      if (clientRegisterService != null) {
        channel.writeAndFlush("Consumer side: " + JConstants.NEWLINE);
        channel.writeAndFlush("--------------------------------------------------------------------------------" + JConstants.NEWLINE);
        
        Map<RegisterMeta.ServiceMeta, Integer> consumers = clientRegisterService.consumers();
        for (Map.Entry<RegisterMeta.ServiceMeta, Integer> entry : consumers.entrySet()) {
          channel.writeAndFlush(entry.getKey() + " | address_size=" + entry.getValue() + JConstants.NEWLINE);
        }
      }
    }
  }
}
