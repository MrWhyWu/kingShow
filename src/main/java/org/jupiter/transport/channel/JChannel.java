package org.jupiter.transport.channel;

import java.net.SocketAddress;
import org.jupiter.serialization.io.OutputBuf;






























public abstract interface JChannel
{
  public static final JFutureListener<JChannel> CLOSE = new JFutureListener()
  {
    public void operationSuccess(JChannel channel) throws Exception
    {
      channel.close();
    }
    
    public void operationFailure(JChannel channel, Throwable cause) throws Exception
    {
      channel.close();
    }
  };
  
  public abstract String id();
  
  public abstract boolean isActive();
  
  public abstract boolean inIoThread();
  
  public abstract SocketAddress localAddress();
  
  public abstract SocketAddress remoteAddress();
  
  public abstract boolean isWritable();
  
  public abstract boolean isMarkedReconnect();
  
  public abstract boolean isAutoRead();
  
  public abstract void setAutoRead(boolean paramBoolean);
  
  public abstract JChannel close();
  
  public abstract JChannel close(JFutureListener<JChannel> paramJFutureListener);
  
  public abstract JChannel write(Object paramObject);
  
  public abstract JChannel write(Object paramObject, JFutureListener<JChannel> paramJFutureListener);
  
  public abstract OutputBuf allocOutputBuf();
}
