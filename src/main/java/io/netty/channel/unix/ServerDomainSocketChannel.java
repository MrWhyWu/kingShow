package io.netty.channel.unix;

import io.netty.channel.ServerChannel;

public abstract interface ServerDomainSocketChannel
  extends ServerChannel, UnixChannel
{
  public abstract DomainSocketAddress remoteAddress();
  
  public abstract DomainSocketAddress localAddress();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.ServerDomainSocketChannel
 * JD-Core Version:    0.7.0.1
 */