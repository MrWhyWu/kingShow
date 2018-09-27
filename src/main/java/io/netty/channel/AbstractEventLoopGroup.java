package io.netty.channel;

import io.netty.util.concurrent.AbstractEventExecutorGroup;

public abstract class AbstractEventLoopGroup
  extends AbstractEventExecutorGroup
  implements EventLoopGroup
{
  public abstract EventLoop next();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AbstractEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */