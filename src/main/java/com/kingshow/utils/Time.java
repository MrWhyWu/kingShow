package com.kingshow.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;











public class Time
{
  private static final Pattern p = Pattern.compile("(([0-9]+?)((d|h|mi|min|mn|s)))+?");
  private static final Integer MINUTE = Integer.valueOf(60);
  private static final Integer HOUR = Integer.valueOf(60 * MINUTE.intValue());
  private static final Integer DAY = Integer.valueOf(24 * HOUR.intValue());
  


  public Time() {}
  


  public static int parseDuration(String duration)
  {
    if (duration == null) {
      return 30 * DAY.intValue();
    }
    
    Matcher matcher = p.matcher(duration);
    int seconds = 0;
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid duration pattern : " + duration);
    }
    
    matcher.reset();
    while (matcher.find()) {
      if (matcher.group(3).equals("d")) {
        seconds += Integer.parseInt(matcher.group(2)) * DAY.intValue();
      } else if (matcher.group(3).equals("h")) {
        seconds += Integer.parseInt(matcher.group(2)) * HOUR.intValue();
      } else if ((matcher.group(3).equals("mi")) || (matcher.group(3).equals("min")) || (matcher.group(3).equals("mn"))) {
        seconds += Integer.parseInt(matcher.group(2)) * MINUTE.intValue();
      } else {
        seconds += Integer.parseInt(matcher.group(2));
      }
    }
    
    return seconds;
  }
}
