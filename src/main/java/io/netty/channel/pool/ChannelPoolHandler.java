package io.netty.channel.pool;

import io.netty.channel.Channel;

public abstract interface ChannelPoolHandler
{
  public abstract void channelReleased(Channel paramChannel)
    throws Exception;
  
  public abstract void channelAcquired(Channel paramChannel)
    throws Exception;
  
  public abstract void channelCreated(Channel paramChannel)
    throws Exception;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.pool.ChannelPoolHandler
 * JD-Core Version:    0.7.0.1
 */