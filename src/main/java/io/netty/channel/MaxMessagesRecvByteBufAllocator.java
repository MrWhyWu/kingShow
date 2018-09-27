package io.netty.channel;

public abstract interface MaxMessagesRecvByteBufAllocator
  extends RecvByteBufAllocator
{
  public abstract int maxMessagesPerRead();
  
  public abstract MaxMessagesRecvByteBufAllocator maxMessagesPerRead(int paramInt);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.MaxMessagesRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */