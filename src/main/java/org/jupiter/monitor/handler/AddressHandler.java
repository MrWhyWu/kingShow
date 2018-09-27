package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import java.util.List;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.monitor.Command.ChildCommand;
import org.jupiter.registry.RegistryMonitor;




















public class AddressHandler
  extends ChildCommandHandler<RegistryHandler>
{
  public AddressHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    RegistryMonitor monitor = ((RegistryHandler)getParent()).getRegistryMonitor();
    if (monitor == null) {
      return;
    }
    
    Command.ChildCommand target = command.parseChild(args[2]);
    if (target == null) {
      channel.writeAndFlush("Wrong args denied!" + JConstants.NEWLINE); return;
    }
    
    List<String> addresses;
    List<String> addresses;
    switch (1.$SwitchMap$org$jupiter$monitor$Command$ChildCommand[target.ordinal()]) {
    case 1: 
      addresses = monitor.listPublisherHosts();
      
      break;
    case 2: 
      addresses = monitor.listSubscriberAddresses();
      
      break;
    default: 
      return; }
    List<String> addresses;
    Command.ChildCommand childGrep = null;
    if (args.length >= 5) {
      childGrep = command.parseChild(args[3]);
    }
    for (String a : addresses) {
      if ((childGrep != null) && (childGrep == Command.ChildCommand.GREP)) {
        if (a.contains(args[4])) {
          channel.writeAndFlush(a + JConstants.NEWLINE);
        }
      } else {
        channel.writeAndFlush(a + JConstants.NEWLINE);
      }
    }
  }
}
