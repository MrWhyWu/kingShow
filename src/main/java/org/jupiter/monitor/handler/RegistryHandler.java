package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.monitor.Command.ChildCommand;
import org.jupiter.registry.RegistryMonitor;


















public class RegistryHandler
  implements CommandHandler
{
  private volatile RegistryMonitor registryMonitor;
  
  public RegistryHandler() {}
  
  public RegistryMonitor getRegistryMonitor()
  {
    return registryMonitor;
  }
  
  public void setRegistryMonitor(RegistryMonitor registryMonitor) {
    this.registryMonitor = registryMonitor;
  }
  

  public void handle(Channel channel, Command command, String... args)
  {
    if (AuthHandler.checkAuth(channel)) {
      if (args.length < 3) {
        channel.writeAndFlush("Need more args!" + JConstants.NEWLINE);
        return;
      }
      
      Command.ChildCommand child = command.parseChild(args[1]);
      if (child != null) {
        CommandHandler childHandler = child.handler();
        if (childHandler == null) {
          return;
        }
        if (((childHandler instanceof ChildCommandHandler)) && 
          (((ChildCommandHandler)childHandler).getParent() == null)) {
          ((ChildCommandHandler)childHandler).setParent(this);
        }
        
        childHandler.handle(channel, command, args);
      } else {
        channel.writeAndFlush("Wrong args denied!" + JConstants.NEWLINE);
      }
    }
  }
}
