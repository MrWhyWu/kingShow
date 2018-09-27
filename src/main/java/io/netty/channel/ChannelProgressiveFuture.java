package io.netty.channel;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ProgressiveFuture;

public abstract interface ChannelProgressiveFuture
  extends ChannelFuture, ProgressiveFuture<Void>
{
  public abstract ChannelProgressiveFuture addListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  public abstract ChannelProgressiveFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  public abstract ChannelProgressiveFuture removeListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  public abstract ChannelProgressiveFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  public abstract ChannelProgressiveFuture sync()
    throws InterruptedException;
  
  public abstract ChannelProgressiveFuture syncUninterruptibly();
  
  public abstract ChannelProgressiveFuture await()
    throws InterruptedException;
  
  public abstract ChannelProgressiveFuture awaitUninterruptibly();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelProgressiveFuture
 * JD-Core Version:    0.7.0.1
 */