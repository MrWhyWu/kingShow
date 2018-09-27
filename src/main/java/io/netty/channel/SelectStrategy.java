package io.netty.channel;

import io.netty.util.IntSupplier;

public abstract interface SelectStrategy
{
  public static final int SELECT = -1;
  public static final int CONTINUE = -2;
  
  public abstract int calculateStrategy(IntSupplier paramIntSupplier, boolean paramBoolean)
    throws Exception;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.SelectStrategy
 * JD-Core Version:    0.7.0.1
 */