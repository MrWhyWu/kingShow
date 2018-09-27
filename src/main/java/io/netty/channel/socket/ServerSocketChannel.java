package io.netty.channel.socket;

import io.netty.channel.ServerChannel;
import java.net.InetSocketAddress;

public abstract interface ServerSocketChannel
  extends ServerChannel
{
  public abstract ServerSocketChannelConfig config();
  
  public abstract InetSocketAddress localAddress();
  
  public abstract InetSocketAddress remoteAddress();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.ServerSocketChannel
 * JD-Core Version:    0.7.0.1
 */