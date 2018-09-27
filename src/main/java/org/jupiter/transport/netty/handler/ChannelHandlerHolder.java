package org.jupiter.transport.netty.handler;

import io.netty.channel.ChannelHandler;

public abstract interface ChannelHandlerHolder
{
  public abstract ChannelHandler[] handlers();
}
