package org.jupiter.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Timer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Preconditions;
import org.jupiter.transport.CodecConfig;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConnection;
import org.jupiter.transport.JOption;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.channel.JChannelGroup;
import org.jupiter.transport.exception.ConnectFailedException;
import org.jupiter.transport.netty.handler.IdleStateChecker;
import org.jupiter.transport.netty.handler.LowCopyProtocolDecoder;
import org.jupiter.transport.netty.handler.LowCopyProtocolEncoder;
import org.jupiter.transport.netty.handler.ProtocolDecoder;
import org.jupiter.transport.netty.handler.ProtocolEncoder;
import org.jupiter.transport.netty.handler.connector.ConnectionWatchdog;
import org.jupiter.transport.netty.handler.connector.ConnectorHandler;
import org.jupiter.transport.netty.handler.connector.ConnectorIdleStateTrigger;
import org.jupiter.transport.processor.ConsumerProcessor;



































































public class JNettyTcpConnector
  extends NettyTcpConnector
{
  private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
  private final ChannelOutboundHandler encoder = CodecConfig.isCodecLowCopy() ? new LowCopyProtocolEncoder() : new ProtocolEncoder();
  
  private final ConnectorHandler handler = new ConnectorHandler();
  

  public JNettyTcpConnector() {}
  
  public JNettyTcpConnector(boolean isNative)
  {
    super(isNative);
  }
  
  public JNettyTcpConnector(int nWorkers) {
    super(nWorkers);
  }
  
  public JNettyTcpConnector(int nWorkers, boolean isNative) {
    super(nWorkers, isNative);
  }
  

  protected void doInit()
  {
    config().setOption(JOption.SO_REUSEADDR, Boolean.valueOf(true));
    config().setOption(JOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf((int)TimeUnit.SECONDS.toMillis(3L)));
    
    initChannelFactory();
  }
  
  protected void setProcessor(ConsumerProcessor processor)
  {
    handler.processor((ConsumerProcessor)Preconditions.checkNotNull(processor, "processor"));
  }
  
  public JConnection connect(UnresolvedAddress address, boolean async)
  {
    setOptions();
    
    Bootstrap boot = bootstrap();
    SocketAddress socketAddress = InetSocketAddress.createUnresolved(address.getHost(), address.getPort());
    JChannelGroup group = group(address);
    

    final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, socketAddress, group)
    {
      public ChannelHandler[] handlers()
      {
        return new ChannelHandler[] { this, new IdleStateChecker(timer, 0, JConstants.WRITER_IDLE_TIME_SECONDS, 0), idleStateTrigger, CodecConfig.isCodecLowCopy() ? new LowCopyProtocolDecoder() : new ProtocolDecoder(), encoder, handler };
      }
    };
    



    try
    {
      ChannelFuture future;
      


      synchronized (bootstrapLock()) {
        boot.handler(new ChannelInitializer()
        {
          protected void initChannel(Channel ch) throws Exception
          {
            ch.pipeline().addLast(watchdog.handlers());
          }
          
        });
        future = boot.connect(socketAddress);
      }
      
      ChannelFuture future;
      if (!async) {
        future.sync();
      }
    } catch (Throwable t) {
      throw new ConnectFailedException("Connects to [" + address + "] fails", t);
    }
    ChannelFuture future;
    new JNettyConnection(address, future)
    {
      public void setReconnect(boolean reconnect)
      {
        if (reconnect) {
          watchdog.start();
        } else {
          watchdog.stop();
        }
      }
    };
  }
}
