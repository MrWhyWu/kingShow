package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import org.jupiter.monitor.Command;

public abstract interface CommandHandler
{
  public abstract void handle(Channel paramChannel, Command paramCommand, String... paramVarArgs);
}
