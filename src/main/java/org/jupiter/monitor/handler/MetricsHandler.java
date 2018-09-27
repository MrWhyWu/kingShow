package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.monitor.Command.ChildCommand;
import org.jupiter.monitor.metric.MetricsReporter;



















public class MetricsHandler
  implements CommandHandler
{
  public MetricsHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    if (AuthHandler.checkAuth(channel)) {
      if (args.length < 2) {
        channel.writeAndFlush("Need second arg!" + JConstants.NEWLINE);
        return;
      }
      
      Command.ChildCommand child = command.parseChild(args[1]);
      if (child != null) {
        switch (1.$SwitchMap$org$jupiter$monitor$Command$ChildCommand[child.ordinal()]) {
        case 1: 
          channel.writeAndFlush(MetricsReporter.report());
        
        }
        
      } else {
        channel.writeAndFlush("Wrong args denied!" + JConstants.NEWLINE);
      }
    }
  }
}
