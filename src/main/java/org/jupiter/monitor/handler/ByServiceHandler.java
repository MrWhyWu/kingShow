package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.monitor.Command.ChildCommand;
import org.jupiter.registry.RegistryMonitor;



















public class ByServiceHandler
  extends ChildCommandHandler<RegistryHandler>
{
  public ByServiceHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    RegistryMonitor monitor = ((RegistryHandler)getParent()).getRegistryMonitor();
    if (monitor == null) {
      return;
    }
    
    if (args.length < 5) {
      channel.writeAndFlush("Args[2]: group, args[3]: serviceProviderName, args[4]: version" + JConstants.NEWLINE);
      return;
    }
    Command.ChildCommand childGrep = null;
    if (args.length >= 7) {
      childGrep = command.parseChild(args[5]);
    }
    
    for (String a : monitor.listAddressesByService(args[2], args[3], args[4])) {
      if ((childGrep != null) && (childGrep == Command.ChildCommand.GREP)) {
        if (a.contains(args[6])) {
          channel.writeAndFlush(a + JConstants.NEWLINE);
        }
      } else {
        channel.writeAndFlush(a + JConstants.NEWLINE);
      }
    }
  }
}
