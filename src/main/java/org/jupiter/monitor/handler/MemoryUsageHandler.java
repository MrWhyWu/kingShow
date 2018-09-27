package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import java.util.List;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.JvmTools;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.monitor.Command;





















public class MemoryUsageHandler
  implements CommandHandler
{
  public MemoryUsageHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    try
    {
      List<String> memoryUsageList = JvmTools.memoryUsage();
      for (String usage : memoryUsageList) {
        channel.writeAndFlush(usage);
      }
      channel.writeAndFlush(JConstants.NEWLINE);
    } catch (Exception e) {
      channel.writeAndFlush(StackTraceUtil.stackTrace(e));
    }
  }
}
