package com.kingshow.netty;

import io.netty.handler.codec.http.FullHttpRequest;

public class GameRequest
{
  private io.netty.channel.ChannelHandlerContext ctx;
  private FullHttpRequest request;
  private String data;
  
  public GameRequest(io.netty.channel.ChannelHandlerContext ctx, FullHttpRequest req, String data) {
    this.ctx = ctx;
    this.data = data;
    request = req;
  }
  
  public io.netty.channel.ChannelHandlerContext getChannelHandlerContext() {
    return ctx;
  }
  
  public String getData()
  {
    return data;
  }
  
  public FullHttpRequest getRequest() {
    return request;
  }
}
