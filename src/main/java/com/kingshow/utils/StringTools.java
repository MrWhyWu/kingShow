package com.kingshow.utils;

import java.util.StringTokenizer;


public class StringTools
{
  public static final String IV = "H1K2J3K4T5O6E7R8";
  public static final String BASE_KEY = "B1H2J3D4e5w6q7F8";
  
  public StringTools() {}
  
  public static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
  
  public static String getMac(String uri) {
    StringTokenizer token = new StringTokenizer(uri, "?mac=");
    token.nextElement();
    if (token.hasMoreElements()) {
      return (String)token.nextElement();
    }
    return null;
  }
}
