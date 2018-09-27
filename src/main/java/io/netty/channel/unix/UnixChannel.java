package io.netty.channel.unix;

import io.netty.channel.Channel;

public abstract interface UnixChannel
  extends Channel
{
  public abstract FileDescriptor fd();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.UnixChannel
 * JD-Core Version:    0.7.0.1
 */