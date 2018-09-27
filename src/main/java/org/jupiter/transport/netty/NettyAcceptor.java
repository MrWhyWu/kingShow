package org.jupiter.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import org.jupiter.common.concurrent.NamedThreadFactory;
import org.jupiter.common.util.JConstants;
import org.jupiter.transport.JAcceptor;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConfigGroup;
import org.jupiter.transport.JOption;
import org.jupiter.transport.Transporter.Protocol;
import org.jupiter.transport.netty.estimator.JMessageSizeEstimator;
import org.jupiter.transport.processor.ProviderProcessor;





















public abstract class NettyAcceptor
  implements JAcceptor
{
  protected final Transporter.Protocol protocol;
  protected final SocketAddress localAddress;
  protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("acceptor.timer", true));
  
  private final int nBosses;
  
  private final int nWorkers;
  private ServerBootstrap bootstrap;
  private EventLoopGroup boss;
  private EventLoopGroup worker;
  private ProviderProcessor processor;
  
  public NettyAcceptor(Transporter.Protocol protocol, SocketAddress localAddress)
  {
    this(protocol, localAddress, JConstants.AVAILABLE_PROCESSORS << 1);
  }
  
  public NettyAcceptor(Transporter.Protocol protocol, SocketAddress localAddress, int nWorkers) {
    this(protocol, localAddress, 1, nWorkers);
  }
  
  public NettyAcceptor(Transporter.Protocol protocol, SocketAddress localAddress, int nBosses, int nWorkers) {
    this.protocol = protocol;
    this.localAddress = localAddress;
    this.nBosses = nBosses;
    this.nWorkers = nWorkers;
  }
  
  protected void init() {
    ThreadFactory bossFactory = bossThreadFactory("jupiter.acceptor.boss");
    ThreadFactory workerFactory = workerThreadFactory("jupiter.acceptor.worker");
    boss = initEventLoopGroup(nBosses, bossFactory);
    worker = initEventLoopGroup(nWorkers, workerFactory);
    
    bootstrap = new ServerBootstrap().group(boss, worker);
    

    JConfig parent = configGroup().parent();
    parent.setOption(JOption.IO_RATIO, Integer.valueOf(100));
    

    JConfig child = configGroup().child();
    child.setOption(JOption.IO_RATIO, Integer.valueOf(100));
  }
  
  public Transporter.Protocol protocol()
  {
    return protocol;
  }
  
  public SocketAddress localAddress()
  {
    return localAddress;
  }
  
  public int boundPort()
  {
    if (!(localAddress instanceof InetSocketAddress)) {
      throw new UnsupportedOperationException("Unsupported address type to get port");
    }
    return ((InetSocketAddress)localAddress).getPort();
  }
  
  public ProviderProcessor processor()
  {
    return processor;
  }
  
  public void withProcessor(ProviderProcessor processor)
  {
    setProcessor(this.processor = processor);
  }
  
  public void shutdownGracefully()
  {
    boss.shutdownGracefully().syncUninterruptibly();
    worker.shutdownGracefully().syncUninterruptibly();
    timer.stop();
    if (processor != null) {
      processor.shutdown();
    }
  }
  
  protected ThreadFactory bossThreadFactory(String name) {
    return new DefaultThreadFactory(name, 10);
  }
  
  protected ThreadFactory workerThreadFactory(String name) {
    return new DefaultThreadFactory(name, 10);
  }
  
  protected void setOptions() {
    JConfig parent = configGroup().parent();
    JConfig child = configGroup().child();
    
    setIoRatio(((Integer)parent.getOption(JOption.IO_RATIO)).intValue(), ((Integer)child.getOption(JOption.IO_RATIO)).intValue());
    
    bootstrap.childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, JMessageSizeEstimator.DEFAULT);
  }
  


  protected ServerBootstrap bootstrap()
  {
    return bootstrap;
  }
  



  protected EventLoopGroup boss()
  {
    return boss;
  }
  



  protected EventLoopGroup worker()
  {
    return worker;
  }
  
  protected void setProcessor(ProviderProcessor processor) {}
  
  public abstract void setIoRatio(int paramInt1, int paramInt2);
  
  protected abstract ChannelFuture bind(SocketAddress paramSocketAddress);
  
  protected abstract EventLoopGroup initEventLoopGroup(int paramInt, ThreadFactory paramThreadFactory);
}
