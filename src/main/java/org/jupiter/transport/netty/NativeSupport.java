package org.jupiter.transport.netty;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;


























public final class NativeSupport
{
  public NativeSupport() {}
  
  public static boolean isNativeEPollAvailable()
  {
    return Epoll.isAvailable();
  }
  


  public static boolean isNativeKQueueAvailable()
  {
    return KQueue.isAvailable();
  }
}
