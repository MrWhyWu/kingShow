package org.jupiter.monitor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.Strings;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.monitor.handler.CommandHandler;
import org.jupiter.monitor.handler.LsHandler;
import org.jupiter.monitor.handler.RegistryHandler;
import org.jupiter.registry.RegistryMonitor;
import org.jupiter.registry.RegistryService;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.JServer;
import org.jupiter.transport.netty.NettyTcpAcceptor;









































public class MonitorServer
  extends NettyTcpAcceptor
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(MonitorServer.class);
  

  private static final int DEFAULT_PORT = 19999;
  
  private final TelnetHandler handler = new TelnetHandler();
  private final StringEncoder encoder = new StringEncoder(JConstants.UTF8);
  private volatile RegistryMonitor registryMonitor;
  private volatile JServer jupiterServer;
  private volatile JClient jupiterClient;
  
  public MonitorServer()
  {
    this(19999);
  }
  
  public MonitorServer(int port) {
    super(port, 1, false);
  }
  
  public ChannelFuture bind(SocketAddress localAddress)
  {
    ServerBootstrap boot = bootstrap();
    
    initChannelFactory();
    
    boot.childHandler(new ChannelInitializer()
    {
      protected void initChannel(Channel ch) throws Exception
      {
        ch.pipeline().addLast(new ChannelHandler[] { new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()), new StringDecoder(JConstants.UTF8), encoder, handler });


      }
      


    });
    setOptions();
    
    return boot.bind(localAddress);
  }
  
  public void start() throws InterruptedException
  {
    super.start(false);
  }
  


  public void setRegistryMonitor(RegistryMonitor registryMonitor)
  {
    this.registryMonitor = registryMonitor;
  }
  
  public void setJupiterServer(JServer jupiterServer) {
    this.jupiterServer = jupiterServer;
  }
  
  public void setJupiterClient(JClient jupiterClient) {
    this.jupiterClient = jupiterClient;
  }
  
  @ChannelHandler.Sharable
  class TelnetHandler extends ChannelInboundHandlerAdapter {
    TelnetHandler() {}
    
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      Channel ch = ctx.channel();
      
      if ((msg instanceof String)) {
        String[] args = Strings.split(((String)msg).replace("\r\n", ""), ' ');
        if ((args == null) || (args.length == 0)) {
          return;
        }
        
        Command command = Command.parse(args[0]);
        if (command == null) {
          ch.writeAndFlush("invalid command!" + JConstants.NEWLINE);
          return;
        }
        
        CommandHandler handler = command.handler();
        if (((handler instanceof RegistryHandler)) && 
          (((RegistryHandler)handler).getRegistryMonitor() != registryMonitor)) {
          ((RegistryHandler)handler).setRegistryMonitor(registryMonitor);
        }
        
        if ((handler instanceof LsHandler)) {
          RegistryService serverRegisterService = jupiterServer == null ? null : jupiterServer.registryService();
          
          if (((LsHandler)handler).getServerRegisterService() != serverRegisterService) {
            ((LsHandler)handler).setServerRegisterService(serverRegisterService);
          }
          RegistryService clientRegisterService = jupiterClient == null ? null : jupiterClient.registryService();
          
          if (((LsHandler)handler).getClientRegisterService() != clientRegisterService) {
            ((LsHandler)handler).setClientRegisterService(clientRegisterService);
          }
        }
        handler.handle(ch, command, args);
      } else {
        MonitorServer.logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
        
        ReferenceCountUtil.release(msg);
      }
    }
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
      ctx.writeAndFlush(JConstants.NEWLINE + "Welcome to jupiter monitor! Please auth with password." + JConstants.NEWLINE);
      Command command = Command.parse("help");
      CommandHandler handler = command.handler();
      handler.handle(ctx.channel(), command, new String[0]);
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
      MonitorServer.logger.error("An exception was caught: {}, channel {}.", StackTraceUtil.stackTrace(cause), ctx.channel());
      
      ctx.close();
    }
  }
}
