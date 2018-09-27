package org.jupiter.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import java.net.SocketAddress;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Preconditions;
import org.jupiter.transport.CodecConfig;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JConfigGroup;
import org.jupiter.transport.JOption;
import org.jupiter.transport.netty.handler.IdleStateChecker;
import org.jupiter.transport.netty.handler.LowCopyProtocolDecoder;
import org.jupiter.transport.netty.handler.LowCopyProtocolEncoder;
import org.jupiter.transport.netty.handler.ProtocolDecoder;
import org.jupiter.transport.netty.handler.ProtocolEncoder;
import org.jupiter.transport.netty.handler.acceptor.AcceptorHandler;
import org.jupiter.transport.netty.handler.acceptor.AcceptorIdleStateTrigger;
import org.jupiter.transport.processor.ProviderProcessor;




























































public class JNettyTcpAcceptor
  extends NettyTcpAcceptor
{
  public static final int DEFAULT_ACCEPTOR_PORT = 18090;
  private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
  private final ChannelOutboundHandler encoder = CodecConfig.isCodecLowCopy() ? new LowCopyProtocolEncoder() : new ProtocolEncoder();
  
  private final AcceptorHandler handler = new AcceptorHandler();
  
  public JNettyTcpAcceptor() {
    super(18090);
  }
  
  public JNettyTcpAcceptor(int port) {
    super(port);
  }
  
  public JNettyTcpAcceptor(SocketAddress localAddress) {
    super(localAddress);
  }
  
  public JNettyTcpAcceptor(int port, int nWorkers) {
    super(port, nWorkers);
  }
  
  public JNettyTcpAcceptor(SocketAddress localAddress, int nWorkers) {
    super(localAddress, nWorkers);
  }
  
  public JNettyTcpAcceptor(int port, boolean isNative) {
    super(port, isNative);
  }
  
  public JNettyTcpAcceptor(SocketAddress localAddress, boolean isNative) {
    super(localAddress, isNative);
  }
  
  public JNettyTcpAcceptor(int port, int nWorkers, boolean isNative) {
    super(port, nWorkers, isNative);
  }
  
  public JNettyTcpAcceptor(SocketAddress localAddress, int nWorkers, boolean isNative) {
    super(localAddress, nWorkers, isNative);
  }
  
  protected void init()
  {
    super.init();
    

    JConfig parent = configGroup().parent();
    parent.setOption(JOption.SO_BACKLOG, Integer.valueOf(32768));
    parent.setOption(JOption.SO_REUSEADDR, Boolean.valueOf(true));
    

    JConfig child = configGroup().child();
    child.setOption(JOption.SO_REUSEADDR, Boolean.valueOf(true));
  }
  
  public ChannelFuture bind(SocketAddress localAddress)
  {
    ServerBootstrap boot = bootstrap();
    
    initChannelFactory();
    
    boot.childHandler(new ChannelInitializer()
    {
      protected void initChannel(Channel ch) throws Exception
      {
        ch.pipeline().addLast(new ChannelHandler[] { new IdleStateChecker(timer, JConstants.READER_IDLE_TIME_SECONDS, 0, 0), idleStateTrigger, CodecConfig.isCodecLowCopy() ? new LowCopyProtocolDecoder() : new ProtocolDecoder(), encoder, handler });


      }
      



    });
    setOptions();
    
    return boot.bind(localAddress);
  }
  
  protected void setProcessor(ProviderProcessor processor)
  {
    handler.processor((ProviderProcessor)Preconditions.checkNotNull(processor, "processor"));
  }
}
