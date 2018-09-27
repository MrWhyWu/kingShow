package com.kingshow.utils;

import java.security.MessageDigest;
import java.util.Random;




public class Md5Tools
{
  public Md5Tools() {}
  
  private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5", 
    "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
  
  private static String byteToHexString(byte b) {
    int n = b;
    if (n < 0)
      n += 256;
    int d1 = n / 16;
    int d2 = n % 16;
    return hexDigits[d1] + hexDigits[d2];
  }
  
  private static String byteArrayToHexString(byte[] b) { StringBuffer resultSb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      resultSb.append(byteToHexString(b[i]));
    }
    return resultSb.toString();
  }
  
  public static String MD5(String origin) {
    String resultString = null;
    try {
      resultString = origin;
      MessageDigest md = MessageDigest.getInstance("MD5");
      resultString = byteArrayToHexString(md.digest(resultString
        .getBytes("utf-8")));
    }
    catch (Exception localException) {}
    return resultString;
  }
  
  public static final String generalCode(int num) {
    int legth = StringTools.hexDigits.length;
    
    long seed = (System.currentTimeMillis() * Math.random());
    Random rand = new Random(seed);
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < num; i++) {
      sb.append(StringTools.hexDigits[rand.nextInt(legth)]);
    }
    rand = null;
    
    String code = sb.toString();
    sb = null;
    
    return code;
  }
  
  public static final String generalKey(String keys) {
    char[] keyInfos = keys.toCharArray();
    int le = keyInfos.length;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < le; i++) {
      sb.append(keyInfos[i]);
      sb.append(keyInfos[(++i)]);
      i++;
      i++;
      if (sb.length() >= 16) {
        break;
      }
    }
    String key = sb.toString();
    sb = null;
    keyInfos = null;
    
    return key;
  }
}
