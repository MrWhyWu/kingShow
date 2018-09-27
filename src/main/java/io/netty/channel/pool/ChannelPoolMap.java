package io.netty.channel.pool;

public abstract interface ChannelPoolMap<K, P extends ChannelPool>
{
  public abstract P get(K paramK);
  
  public abstract boolean contains(K paramK);
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.pool.ChannelPoolMap
 * JD-Core Version:    0.7.0.1
 */