package org.jupiter.monitor.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.MD5Util;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.monitor.Command;




















public class AuthHandler
  implements CommandHandler
{
  private static final AttributeKey<Object> AUTH_KEY = AttributeKey.valueOf("auth");
  private static final Object AUTH_OBJECT = new Object();
  private static final String DEFAULT_PASSWORD = MD5Util.getMD5("jupiter");
  
  public AuthHandler() {}
  
  public void handle(Channel channel, Command command, String... args) { if (args.length < 2) {
      channel.writeAndFlush("Need password!" + JConstants.NEWLINE);
      return;
    }
    
    String password = SystemPropertyUtil.get("monitor.server.password");
    if (password == null) {
      password = DEFAULT_PASSWORD;
    }
    
    if (password.equals(MD5Util.getMD5(args[1]))) {
      channel.attr(AUTH_KEY).setIfAbsent(AUTH_OBJECT);
      channel.writeAndFlush("OK" + JConstants.NEWLINE);
    } else {
      channel.writeAndFlush("Permission denied!" + JConstants.NEWLINE).addListener(ChannelFutureListener.CLOSE);
    }
  }
  
  public static boolean checkAuth(Channel channel)
  {
    if (channel.attr(AUTH_KEY).get() == AUTH_OBJECT) {
      return true;
    }
    channel.writeAndFlush("Permission denied" + JConstants.NEWLINE).addListener(ChannelFutureListener.CLOSE);
    
    return false;
  }
}
