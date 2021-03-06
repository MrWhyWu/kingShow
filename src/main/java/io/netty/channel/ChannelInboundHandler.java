package io.netty.channel;

public abstract interface ChannelInboundHandler
  extends ChannelHandler
{
  public abstract void channelRegistered(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void channelUnregistered(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void channelActive(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void channelInactive(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void channelRead(ChannelHandlerContext paramChannelHandlerContext, Object paramObject)
    throws Exception;
  
  public abstract void channelReadComplete(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void userEventTriggered(ChannelHandlerContext paramChannelHandlerContext, Object paramObject)
    throws Exception;
  
  public abstract void channelWritabilityChanged(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable paramThrowable)
    throws Exception;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelInboundHandler
 * JD-Core Version:    0.7.0.1
 */