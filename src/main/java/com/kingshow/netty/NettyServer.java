package com.kingshow.netty;

import com.kingshow.cache.Cache;
import com.kingshow.regedit.ClientFactory;
import com.kingshow.utils.CacheTools;
import com.kingshow.utils.UserCache;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.io.PrintStream;
import javax.annotation.PostConstruct;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;






@Component
public class NettyServer
{
  private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
  static final boolean SSL = System.getProperty("ssl") != null;
  @Autowired
  @Qualifier("serverBootstrap")
  private ServerBootstrap b;
  
  public NettyServer() {}
  
  @PostConstruct
  public void start() throws Exception {
    init();
    logger.info("Starting server at " + port);
    System.out.println("Starting server at " + port);
    b.bind(port).sync().channel().closeFuture().sync().channel();
    System.out.println("started");
  }
  
  private void init() throws SchedulerException { ClientFactory.regeditClient();
    

    Cache.init();
    UserCache.getInstance();
    CacheTools.getInstance();
    
    HandlerDispatcher handlerDispatcher = new HandlerDispatcher();
    handlerDispatcher.start();
    
    SO_HandlerDispatcher websocketHandler = new SO_HandlerDispatcher();
    websocketHandler.start();
    
    SO_ReceiveHandlerDispatcher receiveService = new SO_ReceiveHandlerDispatcher();
    receiveService.start();
    
    SchedulerJobService jobService = new SchedulerJobService();
    jobService.start();
  }
  
  @Autowired
  @Qualifier("port")
  private int port;
}
