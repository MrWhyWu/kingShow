package io.netty.util.concurrent;

import java.util.EventListener;

public abstract interface GenericFutureListener<F extends Future<?>>
  extends EventListener
{
  public abstract void operationComplete(F paramF)
    throws Exception;
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.concurrent.GenericFutureListener
 * JD-Core Version:    0.7.0.1
 */