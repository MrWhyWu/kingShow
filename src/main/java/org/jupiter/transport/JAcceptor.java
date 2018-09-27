package org.jupiter.transport;

import java.net.SocketAddress;
import org.jupiter.transport.processor.ProviderProcessor;

public abstract interface JAcceptor
  extends Transporter
{
  public abstract SocketAddress localAddress();
  
  public abstract int boundPort();
  
  public abstract JConfigGroup configGroup();
  
  public abstract ProviderProcessor processor();
  
  public abstract void withProcessor(ProviderProcessor paramProviderProcessor);
  
  public abstract void start()
    throws InterruptedException;
  
  public abstract void start(boolean paramBoolean)
    throws InterruptedException;
  
  public abstract void shutdownGracefully();
}
