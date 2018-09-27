package io.netty.channel;

import io.netty.util.concurrent.OrderedEventExecutor;

public abstract interface EventLoop
  extends OrderedEventExecutor, EventLoopGroup
{
  public abstract EventLoopGroup parent();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.EventLoop
 * JD-Core Version:    0.7.0.1
 */