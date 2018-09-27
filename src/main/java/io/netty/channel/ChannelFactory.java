package io.netty.channel;

public abstract interface ChannelFactory<T extends Channel>
  extends io.netty.bootstrap.ChannelFactory<T>
{
  public abstract T newChannel();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelFactory
 * JD-Core Version:    0.7.0.1
 */