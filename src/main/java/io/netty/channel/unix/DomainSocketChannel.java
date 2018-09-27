package io.netty.channel.unix;

import io.netty.channel.socket.DuplexChannel;

public abstract interface DomainSocketChannel
  extends UnixChannel, DuplexChannel
{
  public abstract DomainSocketAddress remoteAddress();
  
  public abstract DomainSocketAddress localAddress();
  
  public abstract DomainSocketChannelConfig config();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.DomainSocketChannel
 * JD-Core Version:    0.7.0.1
 */