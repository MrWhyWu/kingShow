package io.netty.channel.socket;

import java.net.InetSocketAddress;

public abstract interface SocketChannel
  extends DuplexChannel
{
  public abstract ServerSocketChannel parent();
  
  public abstract SocketChannelConfig config();
  
  public abstract InetSocketAddress localAddress();
  
  public abstract InetSocketAddress remoteAddress();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.SocketChannel
 * JD-Core Version:    0.7.0.1
 */