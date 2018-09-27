package com.kingshow.config;

import com.kingshow.netty.HttpRouterInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;













@Configuration
@ComponentScan({"com.kingshow"})
public class MainConfig
{
  private int bossCount;
  private int workerCount;
  private int port;
  private boolean keepAlive;
  private int backlog;
  
  public MainConfig() {}
  
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer()
  {
    return new PropertySourcesPlaceholderConfigurer();
  }
  
  @Bean(name={"serverBootstrap"})
  @Autowired
  @Qualifier("httpRouterInitializer")
  public ServerBootstrap bootstrap(HttpRouterInitializer httpRouterInitializer)
  {
    ServerBootstrap b = new ServerBootstrap();
    
    ((ServerBootstrap)b.group(bossGroup(), workerGroup()).channel(NioServerSocketChannel.class))
      .childHandler(httpRouterInitializer);
    Map<ChannelOption<?>, Object> channelOptions = channelOptions();
    Set<ChannelOption<?>> keySet = channelOptions.keySet();
    
    for (ChannelOption option : keySet)
    {
      if (option.equals(ChannelOption.SO_BACKLOG)) {
        b.option(option, channelOptions.get(option));
      } else {
        b.childOption(option, channelOptions.get(option));
      }
    }
    
    return b;
  }
  
  @Bean(name={"bossGroup"}, destroyMethod="shutdownGracefully")
  public NioEventLoopGroup bossGroup() {
    bossCount = 2;
    return new NioEventLoopGroup(bossCount);
  }
  
  @Bean(name={"workerGroup"}, destroyMethod="shutdownGracefully")
  public NioEventLoopGroup workerGroup() {
    workerCount = 1;
    return new NioEventLoopGroup(workerCount);
  }
  
  @Bean(name={"channelOptions"})
  public Map<ChannelOption<?>, Object> channelOptions() {
    keepAlive = true;
    backlog = 128;
    
    Map<ChannelOption<?>, Object> options = new HashMap();
    options.put(ChannelOption.SO_BACKLOG, Integer.valueOf(backlog));
    
    options.put(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(keepAlive));
    
    return options;
  }
  
  @Bean(name={"port"})
  public int getPort() {
    port = 9092;
    return port;
  }
  

  @Bean(name={"notFundResponse"})
  public FullHttpResponse notFundResponse()
  {
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
  }
}
