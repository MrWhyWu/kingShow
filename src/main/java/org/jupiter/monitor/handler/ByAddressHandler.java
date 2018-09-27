package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.monitor.Command.ChildCommand;
import org.jupiter.registry.RegistryMonitor;



















public class ByAddressHandler
  extends ChildCommandHandler<RegistryHandler>
{
  public ByAddressHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    RegistryMonitor monitor = ((RegistryHandler)getParent()).getRegistryMonitor();
    if (monitor == null) {
      return;
    }
    
    if (args.length < 4) {
      channel.writeAndFlush("Args[2]: host, args[3]: port" + JConstants.NEWLINE);
      return;
    }
    Command.ChildCommand childGrep = null;
    if (args.length >= 6) {
      childGrep = command.parseChild(args[4]);
    }
    
    for (String a : monitor.listServicesByAddress(args[2], Integer.parseInt(args[3]))) {
      if ((childGrep != null) && (childGrep == Command.ChildCommand.GREP)) {
        if (a.contains(args[5])) {
          channel.writeAndFlush(a + JConstants.NEWLINE);
        }
      } else {
        channel.writeAndFlush(a + JConstants.NEWLINE);
      }
    }
  }
}
