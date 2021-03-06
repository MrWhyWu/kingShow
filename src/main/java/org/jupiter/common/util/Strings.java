package org.jupiter.common.util;

import java.util.ArrayList;
import java.util.List;




























public final class Strings
{
  public static String nullToEmpty(String string)
  {
    return string == null ? "" : string;
  }
  


  public static String emptyToNull(String string)
  {
    return isNullOrEmpty(string) ? null : string;
  }
  


  public static boolean isNullOrEmpty(String str)
  {
    return (str == null) || (str.length() == 0);
  }
  




  public static boolean isBlank(String str)
  {
    int strLen;
    



    if ((str != null) && ((strLen = str.length()) != 0)) {
      for (int i = 0; i < strLen; i++) {
        if (!Character.isWhitespace(str.charAt(i))) {
          return false;
        }
      }
    }
    return true;
  }
  








  public static boolean isNotBlank(String str)
  {
    return !isBlank(str);
  }
  











  public static String[] split(String str, char separator)
  {
    return split(str, separator, false);
  }
  


















  public static String[] split(String str, char separator, boolean preserveAllTokens)
  {
    if (str == null) {
      return null;
    }
    int len = str.length();
    if (len == 0) {
      return EMPTY_STRING_ARRAY;
    }
    List<String> list = new ArrayList();
    int i = 0;int start = 0;
    boolean match = false;
    while (i < len)
      if (str.charAt(i) == separator) {
        if ((match) || (preserveAllTokens)) {
          list.add(str.substring(start, i));
          match = false;
        }
        i++;start = i;
      }
      else {
        match = true;
        i++;
      }
    if ((match) || (preserveAllTokens)) {
      list.add(str.substring(start, i));
    }
    return (String[])list.toArray(new String[list.size()]);
  }
  
  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  
  private Strings() {}
}
