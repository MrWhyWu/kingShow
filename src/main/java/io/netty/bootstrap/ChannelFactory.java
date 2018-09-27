package io.netty.bootstrap;

import io.netty.channel.Channel;

@Deprecated
public abstract interface ChannelFactory<T extends Channel>
{
  public abstract T newChannel();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.bootstrap.ChannelFactory
 * JD-Core Version:    0.7.0.1
 */