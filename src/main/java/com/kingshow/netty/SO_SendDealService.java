package com.kingshow.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;





public class SO_SendDealService
{
  public SO_SendDealService() {}
  
  public static void sendData(Channel channel, String jsonStr)
    throws Exception
  {
    TextWebSocketFrame res = new TextWebSocketFrame(jsonStr);
    channel.writeAndFlush(res);
  }
}
