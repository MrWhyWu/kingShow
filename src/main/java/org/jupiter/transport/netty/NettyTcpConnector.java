package org.jupiter.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.concurrent.ThreadFactory;
import org.jupiter.common.util.JConstants;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.Transporter.Protocol;
import org.jupiter.transport.UnresolvedAddress;






















public abstract class NettyTcpConnector
  extends NettyConnector
{
  private final boolean isNative;
  private final NettyConfig.NettyTcpConfigGroup.ChildConfig childConfig = new NettyConfig.NettyTcpConfigGroup.ChildConfig();
  
  public NettyTcpConnector() {
    super(Transporter.Protocol.TCP);
    isNative = false;
    init();
  }
  
  public NettyTcpConnector(boolean isNative) {
    super(Transporter.Protocol.TCP);
    this.isNative = isNative;
    init();
  }
  
  public NettyTcpConnector(int nWorkers) {
    super(Transporter.Protocol.TCP, nWorkers);
    isNative = false;
    init();
  }
  
  public NettyTcpConnector(int nWorkers, boolean isNative) {
    super(Transporter.Protocol.TCP, nWorkers);
    this.isNative = isNative;
    init();
  }
  
  protected void setOptions()
  {
    super.setOptions();
    
    Bootstrap boot = bootstrap();
    
    NettyConfig.NettyTcpConfigGroup.ChildConfig child = childConfig;
    

    ((Bootstrap)((Bootstrap)((Bootstrap)boot.option(ChannelOption.SO_REUSEADDR, Boolean.valueOf(child.isReuseAddress()))).option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(child.isKeepAlive()))).option(ChannelOption.TCP_NODELAY, Boolean.valueOf(child.isTcpNoDelay()))).option(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.valueOf(child.isAllowHalfClosure()));
    


    if (child.getRcvBuf() > 0) {
      boot.option(ChannelOption.SO_RCVBUF, Integer.valueOf(child.getRcvBuf()));
    }
    if (child.getSndBuf() > 0) {
      boot.option(ChannelOption.SO_SNDBUF, Integer.valueOf(child.getSndBuf()));
    }
    if (child.getLinger() > 0) {
      boot.option(ChannelOption.SO_LINGER, Integer.valueOf(child.getLinger()));
    }
    if (child.getIpTos() > 0) {
      boot.option(ChannelOption.IP_TOS, Integer.valueOf(child.getIpTos()));
    }
    if (child.getConnectTimeoutMillis() > 0) {
      boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf(child.getConnectTimeoutMillis()));
    }
    int bufLowWaterMark = child.getWriteBufferLowWaterMark();
    int bufHighWaterMark = child.getWriteBufferHighWaterMark();
    WriteBufferWaterMark waterMark;
    WriteBufferWaterMark waterMark; if ((bufLowWaterMark >= 0) && (bufHighWaterMark > 0)) {
      waterMark = new WriteBufferWaterMark(bufLowWaterMark, bufHighWaterMark);
    } else {
      waterMark = new WriteBufferWaterMark(524288, 1048576);
    }
    boot.option(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
  }
  
  public JConnection connect(UnresolvedAddress address)
  {
    return (JConnection)connect(address, false);
  }
  
  public JConfig config()
  {
    return childConfig;
  }
  
  public void setIoRatio(int workerIoRatio)
  {
    EventLoopGroup worker = worker();
    if ((worker instanceof EpollEventLoopGroup)) {
      ((EpollEventLoopGroup)worker).setIoRatio(workerIoRatio);
    } else if ((worker instanceof KQueueEventLoopGroup)) {
      ((KQueueEventLoopGroup)worker).setIoRatio(workerIoRatio);
    } else if ((worker instanceof NioEventLoopGroup)) {
      ((NioEventLoopGroup)worker).setIoRatio(workerIoRatio);
    }
  }
  
  protected EventLoopGroup initEventLoopGroup(int nThreads, ThreadFactory tFactory)
  {
    TcpChannelProvider.SocketType socketType = socketType();
    switch (1.$SwitchMap$org$jupiter$transport$netty$TcpChannelProvider$SocketType[socketType.ordinal()]) {
    case 1: 
      return new EpollEventLoopGroup(nThreads, tFactory);
    case 2: 
      return new KQueueEventLoopGroup(nThreads, tFactory);
    case 3: 
      return new NioEventLoopGroup(nThreads, tFactory);
    }
    throw new IllegalStateException("Invalid socket type: " + socketType);
  }
  
  protected void initChannelFactory()
  {
    TcpChannelProvider.SocketType socketType = socketType();
    switch (1.$SwitchMap$org$jupiter$transport$netty$TcpChannelProvider$SocketType[socketType.ordinal()]) {
    case 1: 
      bootstrap().channelFactory(TcpChannelProvider.NATIVE_EPOLL_CONNECTOR);
      break;
    case 2: 
      bootstrap().channelFactory(TcpChannelProvider.NATIVE_KQUEUE_CONNECTOR);
      break;
    case 3: 
      bootstrap().channelFactory(TcpChannelProvider.JAVA_NIO_CONNECTOR);
      break;
    default: 
      throw new IllegalStateException("Invalid socket type: " + socketType);
    }
  }
  
  private TcpChannelProvider.SocketType socketType() {
    if ((isNative) && (NativeSupport.isNativeEPollAvailable()))
    {
      return TcpChannelProvider.SocketType.NATIVE_EPOLL;
    }
    if ((isNative) && (NativeSupport.isNativeKQueueAvailable()))
    {
      return TcpChannelProvider.SocketType.NATIVE_KQUEUE;
    }
    return TcpChannelProvider.SocketType.JAVA_NIO;
  }
  
  public String toString()
  {
    return "Socket type: " + socketType() + JConstants.NEWLINE + bootstrap();
  }
}
