package io.netty.channel.pool;

import io.netty.channel.Channel;

public abstract class AbstractChannelPoolHandler
  implements ChannelPoolHandler
{
  public void channelAcquired(Channel ch)
    throws Exception
  {}
  
  public void channelReleased(Channel ch)
    throws Exception
  {}
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.pool.AbstractChannelPoolHandler
 * JD-Core Version:    0.7.0.1
 */