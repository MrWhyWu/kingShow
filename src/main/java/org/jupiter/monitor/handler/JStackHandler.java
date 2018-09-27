package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import java.util.List;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.JvmTools;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.monitor.Command;





















public class JStackHandler
  implements CommandHandler
{
  public JStackHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    try
    {
      List<String> stacks = JvmTools.jStack();
      for (String stack : stacks) {
        channel.writeAndFlush(stack);
      }
      channel.writeAndFlush(JConstants.NEWLINE);
    } catch (Exception e) {
      channel.writeAndFlush(StackTraceUtil.stackTrace(e));
    }
  }
}
