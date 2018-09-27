package org.jupiter.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

























public class MD5Util
{
  private static final ThreadLocal<MessageDigest> messageDigestHolder = new ThreadLocal();
  

  private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  
  static {
    try {
      MessageDigest message = MessageDigest.getInstance("MD5");
      messageDigestHolder.set(message);
    } catch (NoSuchAlgorithmException e) {
      ThrowUtil.throwException(e);
    }
  }
  
  public static String getMD5(String data) {
    try {
      MessageDigest message = (MessageDigest)messageDigestHolder.get();
      if (message == null) {
        message = MessageDigest.getInstance("MD5");
        messageDigestHolder.set(message);
      }
      message.update(data.getBytes(JConstants.UTF8));
      byte[] b = message.digest();
      
      StringBuilder digestHex = new StringBuilder(32);
      for (int i = 0; i < 16; i++) {
        digestHex.append(byteHEX(b[i]));
      }
      
      return digestHex.toString();
    } catch (Exception e) {
      ThrowUtil.throwException(e);
    }
    return "";
  }
  
  private static String byteHEX(byte ib) {
    char[] ob = new char[2];
    ob[0] = hexDigits[(ib >>> 4 & 0xF)];
    ob[1] = hexDigits[(ib & 0xF)];
    return new String(ob);
  }
  
  private MD5Util() {}
}
