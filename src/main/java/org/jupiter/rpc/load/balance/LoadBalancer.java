package org.jupiter.rpc.load.balance;

import org.jupiter.transport.Directory;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.JChannelGroup;

public abstract interface LoadBalancer
{
  public abstract JChannelGroup select(CopyOnWriteGroupList paramCopyOnWriteGroupList, Directory paramDirectory);
}
