package com.kingshow.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;














@Component
@Qualifier("httpRouterInitializer")
public class HttpRouterInitializer
  extends ChannelInitializer<SocketChannel>
{
  @Autowired
  @Qualifier("nettyHttpHandler")
  private HttpHandler handler;
  
  public HttpRouterInitializer() {}
  
  protected void initChannel(SocketChannel ch)
    throws Exception
  {
    ChannelPipeline pipeline = ch.pipeline();
    





    pipeline.addLast("respDecoder-reqEncoder", new HttpServerCodec())
      .addLast("http-aggregator", new HttpObjectAggregator(65536))
      .addLast(new ChannelHandler[] {new ChunkedWriteHandler() })
      .addLast("action-handler", handler);
  }
}
