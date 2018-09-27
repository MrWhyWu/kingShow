package com.kingshow.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Convert
{
  private static final Logger logger = LoggerFactory.getLogger(Convert.class);
  

  public Convert() {}
  

  public static String buf2Str(ByteBuf buf)
  {
    byte[] bytes = new byte[buf.readableBytes()];
    buf.readBytes(bytes);
    try
    {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }
  





  public static ByteBuf str2Buf(String str)
  {
    if (str == null) { return null;
    }
    return Unpooled.copiedBuffer(str, 
      CharsetUtil.UTF_8);
  }
}
