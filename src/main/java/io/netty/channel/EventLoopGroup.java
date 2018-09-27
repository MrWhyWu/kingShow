package io.netty.channel;

import io.netty.util.concurrent.EventExecutorGroup;

public abstract interface EventLoopGroup
  extends EventExecutorGroup
{
  public abstract EventLoop next();
  
  public abstract ChannelFuture register(Channel paramChannel);
  
  public abstract ChannelFuture register(ChannelPromise paramChannelPromise);
  
  @Deprecated
  public abstract ChannelFuture register(Channel paramChannel, ChannelPromise paramChannelPromise);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.EventLoopGroup
 * JD-Core Version:    0.7.0.1
 */