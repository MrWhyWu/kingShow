package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;
import org.jupiter.monitor.Command.ChildCommand;



















public class HelpHandler
  implements CommandHandler
{
  public HelpHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("-- Help ------------------------------------------------------------------------").append(JConstants.NEWLINE);
    
    for (Command parent : Command.values()) {
      buf.append(String.format("%1$-32s", new Object[] { parent.name().toLowerCase() })).append(parent.description()).append(JConstants.NEWLINE);
      


      for (Command.ChildCommand child : parent.children()) {
        buf.append(String.format("%1$36s", new Object[] { "-" })).append(child.name().toLowerCase()).append(' ').append(child.description()).append(JConstants.NEWLINE);
      }
      




      buf.append(JConstants.NEWLINE);
    }
    channel.writeAndFlush(buf.toString());
  }
}
