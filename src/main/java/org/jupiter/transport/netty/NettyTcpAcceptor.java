package org.jupiter.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.transport.JConfigGroup;
import org.jupiter.transport.Transporter.Protocol;





















public abstract class NettyTcpAcceptor
  extends NettyAcceptor
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyTcpAcceptor.class);
  
  private final boolean isNative;
  private final NettyConfig.NettyTcpConfigGroup configGroup = new NettyConfig.NettyTcpConfigGroup();
  
  public NettyTcpAcceptor(int port) {
    super(Transporter.Protocol.TCP, new InetSocketAddress(port));
    isNative = false;
    init();
  }
  
  public NettyTcpAcceptor(SocketAddress localAddress) {
    super(Transporter.Protocol.TCP, localAddress);
    isNative = false;
    init();
  }
  
  public NettyTcpAcceptor(int port, int nWorkers) {
    super(Transporter.Protocol.TCP, new InetSocketAddress(port), nWorkers);
    isNative = false;
    init();
  }
  
  public NettyTcpAcceptor(int port, int nBosses, int nWorkers) {
    super(Transporter.Protocol.TCP, new InetSocketAddress(port), nBosses, nWorkers);
    isNative = false;
    init();
  }
  
  public NettyTcpAcceptor(SocketAddress localAddress, int nWorkers) {
    super(Transporter.Protocol.TCP, localAddress, nWorkers);
    isNative = false;
    init();
  }
  
  public NettyTcpAcceptor(SocketAddress localAddress, int nBosses, int nWorkers) {
    super(Transporter.Protocol.TCP, localAddress, nBosses, nWorkers);
    isNative = false;
    init();
  }
  
  public NettyTcpAcceptor(int port, boolean isNative) {
    super(Transporter.Protocol.TCP, new InetSocketAddress(port));
    this.isNative = isNative;
    init();
  }
  
  public NettyTcpAcceptor(SocketAddress localAddress, boolean isNative) {
    super(Transporter.Protocol.TCP, localAddress);
    this.isNative = isNative;
    init();
  }
  
  public NettyTcpAcceptor(int port, int nWorkers, boolean isNative) {
    super(Transporter.Protocol.TCP, new InetSocketAddress(port), nWorkers);
    this.isNative = isNative;
    init();
  }
  
  public NettyTcpAcceptor(int port, int nBosses, int nWorkers, boolean isNative) {
    super(Transporter.Protocol.TCP, new InetSocketAddress(port), nBosses, nWorkers);
    this.isNative = isNative;
    init();
  }
  
  public NettyTcpAcceptor(SocketAddress localAddress, int nWorkers, boolean isNative) {
    super(Transporter.Protocol.TCP, localAddress, nWorkers);
    this.isNative = isNative;
    init();
  }
  
  public NettyTcpAcceptor(SocketAddress localAddress, int nBosses, int nWorkers, boolean isNative) {
    super(Transporter.Protocol.TCP, localAddress, nBosses, nWorkers);
    this.isNative = isNative;
    init();
  }
  
  protected void setOptions()
  {
    super.setOptions();
    
    ServerBootstrap boot = bootstrap();
    

    NettyConfig.NettyTcpConfigGroup.ParentConfig parent = configGroup.parent();
    boot.option(ChannelOption.SO_BACKLOG, Integer.valueOf(parent.getBacklog()));
    boot.option(ChannelOption.SO_REUSEADDR, Boolean.valueOf(parent.isReuseAddress()));
    if (parent.getRcvBuf() > 0) {
      boot.option(ChannelOption.SO_RCVBUF, Integer.valueOf(parent.getRcvBuf()));
    }
    

    NettyConfig.NettyTcpConfigGroup.ChildConfig child = configGroup.child();
    boot.childOption(ChannelOption.SO_REUSEADDR, Boolean.valueOf(child.isReuseAddress())).childOption(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(child.isKeepAlive())).childOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(child.isTcpNoDelay())).childOption(ChannelOption.ALLOW_HALF_CLOSURE, Boolean.valueOf(child.isAllowHalfClosure()));
    


    if (child.getRcvBuf() > 0) {
      boot.childOption(ChannelOption.SO_RCVBUF, Integer.valueOf(child.getRcvBuf()));
    }
    if (child.getSndBuf() > 0) {
      boot.childOption(ChannelOption.SO_SNDBUF, Integer.valueOf(child.getSndBuf()));
    }
    if (child.getLinger() > 0) {
      boot.childOption(ChannelOption.SO_LINGER, Integer.valueOf(child.getLinger()));
    }
    if (child.getIpTos() > 0) {
      boot.childOption(ChannelOption.IP_TOS, Integer.valueOf(child.getIpTos()));
    }
    int bufLowWaterMark = child.getWriteBufferLowWaterMark();
    int bufHighWaterMark = child.getWriteBufferHighWaterMark();
    WriteBufferWaterMark waterMark;
    WriteBufferWaterMark waterMark; if ((bufLowWaterMark >= 0) && (bufHighWaterMark > 0)) {
      waterMark = new WriteBufferWaterMark(bufLowWaterMark, bufHighWaterMark);
    } else {
      waterMark = new WriteBufferWaterMark(524288, 1048576);
    }
    boot.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
  }
  
  public JConfigGroup configGroup()
  {
    return configGroup;
  }
  
  public void start() throws InterruptedException
  {
    start(true);
  }
  
  public void start(boolean sync)
    throws InterruptedException
  {
    ChannelFuture future = bind(localAddress).sync();
    
    if (logger.isInfoEnabled()) {
      logger.info("Jupiter TCP server start" + (sync ? ", and waits until the server socket closed." : ".") + JConstants.NEWLINE + " {}.", toString());
    }
    

    if (sync)
    {
      future.channel().closeFuture().sync();
    }
  }
  
  public void setIoRatio(int bossIoRatio, int workerIoRatio)
  {
    EventLoopGroup boss = boss();
    if ((boss instanceof EpollEventLoopGroup)) {
      ((EpollEventLoopGroup)boss).setIoRatio(bossIoRatio);
    } else if ((boss instanceof KQueueEventLoopGroup)) {
      ((KQueueEventLoopGroup)boss).setIoRatio(bossIoRatio);
    } else if ((boss instanceof NioEventLoopGroup)) {
      ((NioEventLoopGroup)boss).setIoRatio(bossIoRatio);
    }
    
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
      bootstrap().channelFactory(TcpChannelProvider.NATIVE_EPOLL_ACCEPTOR);
      break;
    case 2: 
      bootstrap().channelFactory(TcpChannelProvider.NATIVE_KQUEUE_ACCEPTOR);
      break;
    case 3: 
      bootstrap().channelFactory(TcpChannelProvider.JAVA_NIO_ACCEPTOR);
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
    return "Socket address:[" + localAddress + ']' + ", socket type: " + socketType() + JConstants.NEWLINE + bootstrap();
  }
}
