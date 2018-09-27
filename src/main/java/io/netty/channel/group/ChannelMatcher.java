package io.netty.channel.group;

import io.netty.channel.Channel;

public abstract interface ChannelMatcher
{
  public abstract boolean matches(Channel paramChannel);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.ChannelMatcher
 * JD-Core Version:    0.7.0.1
 */