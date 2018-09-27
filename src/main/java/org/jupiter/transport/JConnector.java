package org.jupiter.transport;

import java.util.Collection;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.DirectoryJChannelGroup;
import org.jupiter.transport.channel.JChannelGroup;
import org.jupiter.transport.processor.ConsumerProcessor;

public abstract interface JConnector<C>
  extends Transporter
{
  public abstract JConfig config();
  
  public abstract ConsumerProcessor processor();
  
  public abstract void withProcessor(ConsumerProcessor paramConsumerProcessor);
  
  public abstract C connect(UnresolvedAddress paramUnresolvedAddress);
  
  public abstract C connect(UnresolvedAddress paramUnresolvedAddress, boolean paramBoolean);
  
  public abstract JChannelGroup group(UnresolvedAddress paramUnresolvedAddress);
  
  public abstract Collection<JChannelGroup> groups();
  
  public abstract boolean addChannelGroup(Directory paramDirectory, JChannelGroup paramJChannelGroup);
  
  public abstract boolean removeChannelGroup(Directory paramDirectory, JChannelGroup paramJChannelGroup);
  
  public abstract CopyOnWriteGroupList directory(Directory paramDirectory);
  
  public abstract boolean isDirectoryAvailable(Directory paramDirectory);
  
  public abstract DirectoryJChannelGroup directoryGroup();
  
  public abstract JConnectionManager connectionManager();
  
  public abstract void shutdownGracefully();
  
  public static abstract interface ConnectionWatcher
  {
    public abstract void start();
    
    public abstract boolean waitForAvailable(long paramLong);
  }
}
