package com.kingshow.utils;

import com.alibaba.fastjson.JSONObject;
import com.kingshow.cache.Cache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import java.io.PrintStream;







public class HttpTools
{
  private static final String ALLOW_URL = "*";
  private static final String Access_Control_Allow = "Access-Control-Allow-Origin";
  
  public HttpTools() {}
  
  public static void sendCorrectResp(ChannelHandlerContext ctx, FullHttpRequest req, JSONObject obj)
    throws Exception
  {
    if (obj == null) {
      return;
    }
    
    ByteBuf content = Convert.str2Buf(obj.toJSONString());
    if (content == null) {
      return;
    }
    FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
    res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    HttpUtil.setContentLength(res, content.readableBytes());
    

    execute(ctx, req, res);
  }
  







  public static void sendSocketCorrectResp(ChannelHandlerContext ctx, Channel channel, JSONObject obj)
    throws Exception
  {
    if (obj == null) {
      return;
    }
    String data = null;
    KeyInfo keyInfo = (KeyInfo)Cache.get(channel.id().asLongText(), KeyInfo.class);
    if (keyInfo == null) {
      data = AES.Encrypt(obj.toJSONString(), "B1H2J3D4e5w6q7F8", "H1K2J3K4T5O6E7R8");
    } else {
      data = AES.Encrypt(obj.toJSONString(), keyInfo.getKEY(), keyInfo.getIV());
    }
    
    keyInfo = null;
    
    TextWebSocketFrame res = new TextWebSocketFrame(data);
    channel.writeAndFlush(res);
    res = null;
    
    data = null;
  }
  







  public static void sendCorrectRespDefault(ChannelHandlerContext ctx, FullHttpRequest req, JSONObject obj)
    throws Exception
  {
    if (obj == null) {
      return;
    }
    System.out.println("sendCorrectRespDefault==" + obj.toJSONString());
    String data = AES.Encrypt(obj.toJSONString(), "B1H2J3D4e5w6q7F8", "H1K2J3K4T5O6E7R8");
    
    ByteBuf content = Convert.str2Buf(data);
    if (content == null) {
      return;
    }
    FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
    res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    HttpUtil.setContentLength(res, content.readableBytes());
    execute(ctx, req, res);
    
    data = null;
  }
  

















  public static void sendWrongResp(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res)
  {
    ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
    res.content().writeBytes(buf);
    HttpUtil.setContentLength(res, res.content().readableBytes());
    execute(ctx, req, res);
    buf.release();
  }
  
  public static String getIp(HttpRequest request) {
    HttpHeaders headers = request.headers();
    String[] ips = proxyIP(headers);
    if ((ips.length > 0) && (ips[0] != "")) {
      return ips[0].split(":")[0];
    }
    CharSequence realIPChar = headers.get("X-Real-IP");
    if (realIPChar != null) {
      String[] realIP = realIPChar.toString().split(":");
      if ((realIP.length > 0) && 
        (realIP[0] != "[")) {
        return realIP[0];
      }
    }
    
    return "127.0.0.1";
  }
  
  private static String[] proxyIP(HttpHeaders headers) {
    CharSequence ip = headers.get("X-Forwarded-For");
    if (ip == null) {
      return new String[0];
    }
    return ip.toString().split(",");
  }
  
  private static void execute(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
    if (HttpUtil.isKeepAlive(req)) {
      res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      res.headers().set("Access-Control-Allow-Origin", "*");
      ctx.write(res);
      ctx.flush();
    } else {
      ctx.write(res);
      ctx.flush();
      ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }
  }
}
