package org.jupiter.transport.channel;

import java.util.List;
import org.jupiter.transport.Directory;
import org.jupiter.transport.UnresolvedAddress;

public abstract interface JChannelGroup
{
  public abstract UnresolvedAddress remoteAddress();
  
  public abstract JChannel next();
  
  public abstract List<? extends JChannel> channels();
  
  public abstract boolean isEmpty();
  
  public abstract boolean add(JChannel paramJChannel);
  
  public abstract boolean remove(JChannel paramJChannel);
  
  public abstract int size();
  
  public abstract void setCapacity(int paramInt);
  
  public abstract int getCapacity();
  
  public abstract boolean isConnecting();
  
  public abstract void setConnecting(boolean paramBoolean);
  
  public abstract boolean isAvailable();
  
  public abstract boolean waitForAvailable(long paramLong);
  
  public abstract void onAvailable(Runnable paramRunnable);
  
  public abstract int getWeight(Directory paramDirectory);
  
  public abstract void putWeight(Directory paramDirectory, int paramInt);
  
  public abstract void removeWeight(Directory paramDirectory);
  
  public abstract int getWarmUp();
  
  public abstract void setWarmUp(int paramInt);
  
  public abstract boolean isWarmUpComplete();
  
  public abstract long timestamp();
  
  public abstract long deadlineMillis();
}
