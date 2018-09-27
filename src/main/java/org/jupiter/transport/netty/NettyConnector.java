package org.jupiter.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import org.jupiter.common.concurrent.NamedThreadFactory;
import org.jupiter.common.util.ClassUtil;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Maps;
import org.jupiter.common.util.Preconditions;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.transport.Directory;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JConnectionManager;
import org.jupiter.transport.JConnector;
import org.jupiter.transport.JOption;
import org.jupiter.transport.Transporter.Protocol;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.channel.CopyOnWriteGroupList;
import org.jupiter.transport.channel.DirectoryJChannelGroup;
import org.jupiter.transport.channel.JChannelGroup;
import org.jupiter.transport.netty.channel.NettyChannelGroup;
import org.jupiter.transport.netty.estimator.JMessageSizeEstimator;
import org.jupiter.transport.processor.ConsumerProcessor;
















public abstract class NettyConnector
  implements JConnector<JConnection>
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyConnector.class);
  protected final Transporter.Protocol protocol;
  
  static
  {
    ClassUtil.initializeClass("io.netty.channel.DefaultChannelId", 500L);
  }
  

  protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("connector.timer", true));
  
  private final ConcurrentMap<UnresolvedAddress, JChannelGroup> addressGroups = Maps.newConcurrentMap();
  private final DirectoryJChannelGroup directoryGroup = new DirectoryJChannelGroup();
  private final JConnectionManager connectionManager = new JConnectionManager();
  
  private Bootstrap bootstrap;
  private EventLoopGroup worker;
  private int nWorkers;
  private ConsumerProcessor processor;
  
  public NettyConnector(Transporter.Protocol protocol)
  {
    this(protocol, JConstants.AVAILABLE_PROCESSORS << 1);
  }
  
  public NettyConnector(Transporter.Protocol protocol, int nWorkers) {
    this.protocol = protocol;
    this.nWorkers = nWorkers;
  }
  
  protected void init() {
    ThreadFactory workerFactory = workerThreadFactory("jupiter.connector");
    worker = initEventLoopGroup(nWorkers, workerFactory);
    
    bootstrap = ((Bootstrap)new Bootstrap().group(worker));
    
    JConfig child = config();
    child.setOption(JOption.IO_RATIO, Integer.valueOf(100));
    
    doInit();
  }
  

  protected ThreadFactory workerThreadFactory(String name)
  {
    return new DefaultThreadFactory(name, 10);
  }
  
  public Transporter.Protocol protocol()
  {
    return protocol;
  }
  
  public ConsumerProcessor processor()
  {
    return processor;
  }
  
  public void withProcessor(ConsumerProcessor processor)
  {
    setProcessor(this.processor = processor);
  }
  
  public JChannelGroup group(UnresolvedAddress address)
  {
    Preconditions.checkNotNull(address, "address");
    
    JChannelGroup group = (JChannelGroup)addressGroups.get(address);
    if (group == null) {
      JChannelGroup newGroup = channelGroup(address);
      group = (JChannelGroup)addressGroups.putIfAbsent(address, newGroup);
      if (group == null) {
        group = newGroup;
      }
    }
    return group;
  }
  
  public Collection<JChannelGroup> groups()
  {
    return addressGroups.values();
  }
  
  public boolean addChannelGroup(Directory directory, JChannelGroup group)
  {
    CopyOnWriteGroupList groups = directory(directory);
    boolean added = groups.addIfAbsent(group);
    if ((added) && 
      (logger.isInfoEnabled())) {
      logger.info("Added channel group: {} to {}.", group, directory.directoryString());
    }
    
    return added;
  }
  
  public boolean removeChannelGroup(Directory directory, JChannelGroup group)
  {
    CopyOnWriteGroupList groups = directory(directory);
    boolean removed = groups.remove(group);
    if ((removed) && 
      (logger.isWarnEnabled())) {
      logger.warn("Removed channel group: {} in directory: {}.", group, directory.directoryString());
    }
    
    return removed;
  }
  
  public CopyOnWriteGroupList directory(Directory directory)
  {
    return directoryGroup.find(directory);
  }
  
  public boolean isDirectoryAvailable(Directory directory)
  {
    CopyOnWriteGroupList groups = directory(directory);
    JChannelGroup[] snapshot = groups.getSnapshot();
    for (JChannelGroup g : snapshot) {
      if (g.isAvailable()) {
        return true;
      }
    }
    return false;
  }
  
  public DirectoryJChannelGroup directoryGroup()
  {
    return directoryGroup;
  }
  
  public JConnectionManager connectionManager()
  {
    return connectionManager;
  }
  
  public void shutdownGracefully()
  {
    connectionManager.cancelAllAutoReconnect();
    worker.shutdownGracefully().syncUninterruptibly();
    timer.stop();
    if (processor != null) {
      processor.shutdown();
    }
  }
  
  protected void setOptions() {
    JConfig child = config();
    
    setIoRatio(((Integer)child.getOption(JOption.IO_RATIO)).intValue());
    
    bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, JMessageSizeEstimator.DEFAULT);
  }
  



  protected Bootstrap bootstrap()
  {
    return bootstrap;
  }
  


  protected Object bootstrapLock()
  {
    return bootstrap;
  }
  



  protected EventLoopGroup worker()
  {
    return worker;
  }
  


  protected JChannelGroup channelGroup(UnresolvedAddress address)
  {
    return new NettyChannelGroup(address);
  }
  
  protected abstract void doInit();
  
  protected void setProcessor(ConsumerProcessor processor) {}
  
  public abstract void setIoRatio(int paramInt);
  
  protected abstract EventLoopGroup initEventLoopGroup(int paramInt, ThreadFactory paramThreadFactory);
}
