package org.jupiter.common.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;


























public final class SystemPropertyUtil
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(SystemPropertyUtil.class);
  



  public static boolean contains(String key)
  {
    return get(key) != null;
  }
  





  public static String get(String key)
  {
    return get(key, null);
  }
  








  public static String get(String key, String def)
  {
    if (key == null) {
      throw new NullPointerException("key");
    }
    if (key.isEmpty()) {
      throw new IllegalArgumentException("key must not be empty.");
    }
    
    String value = null;
    try {
      if (System.getSecurityManager() == null) {
        value = System.getProperty(key);
      } else {
        value = (String)AccessController.doPrivileged(new PrivilegedAction()
        {
          public String run()
          {
            return System.getProperty(val$key);
          }
        });
      }
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Unable to retrieve a system property '{}'; default values will be used, {}.", key, StackTraceUtil.stackTrace(e));
      }
    }
    
    if (value == null) {
      return def;
    }
    
    return value;
  }
  








  public static boolean getBoolean(String key, boolean def)
  {
    String value = get(key);
    if (value == null) {
      return def;
    }
    
    value = value.trim().toLowerCase();
    if (value.isEmpty()) {
      return true;
    }
    
    if (("true".equals(value)) || ("yes".equals(value)) || ("1".equals(value))) {
      return true;
    }
    
    if (("false".equals(value)) || ("no".equals(value)) || ("0".equals(value))) {
      return false;
    }
    
    logger.warn("Unable to parse the boolean system property '{}':{} - using the default value: {}.", new Object[] { key, value, Boolean.valueOf(def) });
    
    return def;
  }
  
  private static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");
  








  public static int getInt(String key, int def)
  {
    String value = get(key);
    if (value == null) {
      return def;
    }
    
    value = value.trim().toLowerCase();
    if (INTEGER_PATTERN.matcher(value).matches()) {
      try {
        return Integer.parseInt(value);
      }
      catch (Exception localException) {}
    }
    logger.warn("Unable to parse the integer system property '{}':{} - using the default value: {}.", new Object[] { key, value, Integer.valueOf(def) });
    
    return def;
  }
  








  public static long getLong(String key, long def)
  {
    String value = get(key);
    if (value == null) {
      return def;
    }
    
    value = value.trim().toLowerCase();
    if (INTEGER_PATTERN.matcher(value).matches()) {
      try {
        return Long.parseLong(value);
      }
      catch (Exception localException) {}
    }
    logger.warn("Unable to parse the long integer system property '{}':{} - using the default value: {}.", new Object[] { key, value, Long.valueOf(def) });
    
    return def;
  }
  


  public static Object setProperty(String key, String value)
  {
    return System.getProperties().setProperty(key, value);
  }
  
  private SystemPropertyUtil() {}
}
