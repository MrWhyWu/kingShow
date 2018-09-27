package org.jupiter.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;






















public final class TcpChannelProvider<T extends Channel>
  implements ChannelFactory<T>
{
  public static final ChannelFactory<ServerChannel> JAVA_NIO_ACCEPTOR = new TcpChannelProvider(SocketType.JAVA_NIO, ChannelType.ACCEPTOR);
  public static final ChannelFactory<ServerChannel> NATIVE_EPOLL_ACCEPTOR = new TcpChannelProvider(SocketType.NATIVE_EPOLL, ChannelType.ACCEPTOR);
  public static final ChannelFactory<ServerChannel> NATIVE_KQUEUE_ACCEPTOR = new TcpChannelProvider(SocketType.NATIVE_KQUEUE, ChannelType.ACCEPTOR);
  
  public static final ChannelFactory<Channel> JAVA_NIO_CONNECTOR = new TcpChannelProvider(SocketType.JAVA_NIO, ChannelType.CONNECTOR);
  public static final ChannelFactory<Channel> NATIVE_EPOLL_CONNECTOR = new TcpChannelProvider(SocketType.NATIVE_EPOLL, ChannelType.CONNECTOR);
  public static final ChannelFactory<Channel> NATIVE_KQUEUE_CONNECTOR = new TcpChannelProvider(SocketType.NATIVE_KQUEUE, ChannelType.CONNECTOR);
  private final SocketType socketType;
  
  public TcpChannelProvider(SocketType socketType, ChannelType channelType) { this.socketType = socketType;
    this.channelType = channelType;
  }
  


  private final ChannelType channelType;
  
  public T newChannel()
  {
    switch (channelType) {
    case ACCEPTOR: 
      switch (1.$SwitchMap$org$jupiter$transport$netty$TcpChannelProvider$SocketType[socketType.ordinal()]) {
      case 1: 
        return new NioServerSocketChannel();
      case 2: 
        return new EpollServerSocketChannel();
      case 3: 
        return new KQueueServerSocketChannel();
      }
      throw new IllegalStateException("Invalid socket type: " + socketType);
    
    case CONNECTOR: 
      switch (1.$SwitchMap$org$jupiter$transport$netty$TcpChannelProvider$SocketType[socketType.ordinal()]) {
      case 1: 
        return new NioSocketChannel();
      case 2: 
        return new EpollSocketChannel();
      case 3: 
        return new KQueueSocketChannel();
      }
      throw new IllegalStateException("Invalid socket type: " + socketType);
    }
    
    throw new IllegalStateException("Invalid channel type: " + channelType);
  }
  
  public static enum SocketType
  {
    JAVA_NIO, 
    NATIVE_EPOLL, 
    NATIVE_KQUEUE;
    
    private SocketType() {} }
  
  public static enum ChannelType { ACCEPTOR, 
    CONNECTOR;
    
    private ChannelType() {}
  }
}
