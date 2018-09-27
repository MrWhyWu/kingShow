package com.kingshow.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;







@Component
@Qualifier("nettyHttpHandler")
@ChannelHandler.Sharable
public class HttpHandler
  extends SimpleChannelInboundHandler<Object>
{
  private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
  
  private WebSocketServerHandshaker handshaker;
  
  private static final String WEBSOCKET_UPGRADE = "websocket";
  
  private static final String WEBSOCKET_CONNECTION = "Upgrade";
  

  public HttpHandler() {}
  

  public void channelReadComplete(ChannelHandlerContext ctx)
  {
    ctx.flush();
  }
  
  private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req)
  {
    logger.warn("uri:" + req.uri());
    
    if (isWebSocketUpgrade(req)) {
      WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
        getWebSocketLocation(req), null, true);
      handshaker = wsFactory.newHandshaker(req);
      if (handshaker == null) {
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
      }
      else {
        handshaker.handshake(ctx.channel(), req);
      }
      
    }
    else if (req.method().equals(HttpMethod.POST)) {
      String data = req.content().toString(CharsetUtil.UTF_8);
      if (data != null) {
        MessageQueue.getInstance().addRequest(new GameRequest(ctx, req, data));
      }
    }
  }
  

  private boolean isWebSocketUpgrade(FullHttpRequest req)
  {
    HttpHeaders headers = req.headers();
    return (req.method().equals(HttpMethod.GET)) && 
      (headers.get(HttpHeaderNames.UPGRADE) != null) && (headers.get(HttpHeaderNames.UPGRADE).contains("websocket")) && 
      (headers.get(HttpHeaderNames.CONNECTION) != null) && (headers.get(HttpHeaderNames.CONNECTION).contains("Upgrade"));
  }
  
  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
  {
    if ((frame instanceof CloseWebSocketFrame)) {
      handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
      return;
    }
    if ((frame instanceof PingWebSocketFrame)) {
      ctx.write(new PongWebSocketFrame(frame.content().retain()));
      return;
    }
    if ((frame instanceof TextWebSocketFrame)) {
      String data = ((TextWebSocketFrame)frame).text();
      SO_ReceiveMessageQueue.getInstance().addRequest(new GameRequest(ctx, null, data));
      return;
    }
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    cause.printStackTrace();
    ctx.close();
    logger.error("HttpHandler exceptionCaught", cause);
  }
  

  private static String getWebSocketLocation(FullHttpRequest req)
  {
    String location = req.headers().get(HttpHeaderNames.HOST) + "/ws/join";
    logger.info(location);
    if (NettyServer.SSL) {
      return "wss://" + location;
    }
    return "ws://" + location;
  }
  



  protected void channelRead0(ChannelHandlerContext ctx, Object msg)
    throws Exception
  {
    if ((msg instanceof WebSocketFrame)) {
      handleWebSocketFrame(ctx, (WebSocketFrame)msg);
    } else if ((msg instanceof FullHttpRequest)) {
      handleHttpRequest(ctx, (FullHttpRequest)msg);
    }
  }
}
