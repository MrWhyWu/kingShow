package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.jupiter.common.util.JConstants;
import org.jupiter.monitor.Command;



















public class QuitHandler
  implements CommandHandler
{
  public QuitHandler() {}
  
  public void handle(Channel channel, Command command, String... args)
  {
    channel.writeAndFlush("Bye bye!" + JConstants.NEWLINE).addListener(ChannelFutureListener.CLOSE);
  }
}
