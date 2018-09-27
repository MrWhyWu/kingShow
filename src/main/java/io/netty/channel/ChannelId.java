package io.netty.channel;

import java.io.Serializable;

public abstract interface ChannelId
  extends Serializable, Comparable<ChannelId>
{
  public abstract String asShortText();
  
  public abstract String asLongText();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelId
 * JD-Core Version:    0.7.0.1
 */