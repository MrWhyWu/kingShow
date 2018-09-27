package io.netty.channel;

public abstract interface ChannelInboundInvoker
{
  public abstract ChannelInboundInvoker fireChannelRegistered();
  
  public abstract ChannelInboundInvoker fireChannelUnregistered();
  
  public abstract ChannelInboundInvoker fireChannelActive();
  
  public abstract ChannelInboundInvoker fireChannelInactive();
  
  public abstract ChannelInboundInvoker fireExceptionCaught(Throwable paramThrowable);
  
  public abstract ChannelInboundInvoker fireUserEventTriggered(Object paramObject);
  
  public abstract ChannelInboundInvoker fireChannelRead(Object paramObject);
  
  public abstract ChannelInboundInvoker fireChannelReadComplete();
  
  public abstract ChannelInboundInvoker fireChannelWritabilityChanged();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelInboundInvoker
 * JD-Core Version:    0.7.0.1
 */