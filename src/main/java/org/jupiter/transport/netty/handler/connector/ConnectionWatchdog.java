package org.jupiter.transport.netty.handler.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;
import org.jupiter.transport.channel.JChannelGroup;
import org.jupiter.transport.netty.channel.NettyChannel;
import org.jupiter.transport.netty.handler.ChannelHandlerHolder;

















@ChannelHandler.Sharable
public abstract class ConnectionWatchdog
  extends ChannelInboundHandlerAdapter
  implements TimerTask, ChannelHandlerHolder
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConnectionWatchdog.class);
  
  private static final int ST_STARTED = 1;
  
  private static final int ST_STOPPED = 2;
  
  private final Bootstrap bootstrap;
  private final Timer timer;
  private final SocketAddress remoteAddress;
  private final JChannelGroup group;
  private volatile int state = 1;
  private int attempts;
  
  public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, SocketAddress remoteAddress, JChannelGroup group) {
    this.bootstrap = bootstrap;
    this.timer = timer;
    this.remoteAddress = remoteAddress;
    this.group = group;
  }
  
  public boolean isStarted() {
    return state == 1;
  }
  
  public void start() {
    state = 1;
  }
  
  public void stop() {
    state = 2;
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    Channel ch = ctx.channel();
    
    if (group != null) {
      group.add(NettyChannel.attachChannel(ch));
    }
    
    attempts = 0;
    
    logger.info("Connects with {}.", ch);
    
    ctx.fireChannelActive();
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    boolean doReconnect = isReconnectNeeded();
    if (doReconnect) {
      if (attempts < 12) {
        attempts += 1;
      }
      long timeout = 2 << attempts;
      timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
    }
    
    logger.warn("Disconnects with {}, address: {}, reconnect: {}.", new Object[] { ctx.channel(), remoteAddress, Boolean.valueOf(doReconnect) });
    
    ctx.fireChannelInactive();
  }
  
  public void run(Timeout timeout) throws Exception
  {
    if (!isReconnectNeeded()) {
      logger.warn("Cancel reconnecting with {}.", remoteAddress); return;
    }
    
    ChannelFuture future;
    
    synchronized (bootstrap) {
      bootstrap.handler(new ChannelInitializer()
      {
        protected void initChannel(Channel ch) throws Exception
        {
          ch.pipeline().addLast(handlers());
        }
      });
      future = bootstrap.connect(remoteAddress);
    }
    ChannelFuture future;
    future.addListener(new ChannelFutureListener()
    {
      public void operationComplete(ChannelFuture f) throws Exception
      {
        boolean succeed = f.isSuccess();
        
        ConnectionWatchdog.logger.warn("Reconnects with {}, {}.", remoteAddress, succeed ? "succeed" : "failed");
        
        if (!succeed) {
          f.channel().pipeline().fireChannelInactive();
        }
      }
    });
  }
  
  private boolean isReconnectNeeded() {
    return (isStarted()) && ((group == null) || (group.size() < group.getCapacity()));
  }
}
