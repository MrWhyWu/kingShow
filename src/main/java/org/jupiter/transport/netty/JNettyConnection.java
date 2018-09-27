package org.jupiter.transport.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnection.OperationListener;
import org.jupiter.transport.UnresolvedAddress;




















public abstract class JNettyConnection
  extends JConnection
{
  private final ChannelFuture future;
  
  public JNettyConnection(UnresolvedAddress address, ChannelFuture future)
  {
    super(address);
    this.future = future;
  }
  
  public ChannelFuture getFuture() {
    return future;
  }
  
  public void operationComplete(final JConnection.OperationListener operationListener)
  {
    future.addListener(new ChannelFutureListener()
    {
      public void operationComplete(ChannelFuture future) throws Exception
      {
        operationListener.complete(future.isSuccess());
      }
    });
  }
}
