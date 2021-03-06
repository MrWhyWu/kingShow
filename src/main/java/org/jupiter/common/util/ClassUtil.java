package org.jupiter.common.util;

import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;

























public final class ClassUtil
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ClassUtil.class);
  





  public static void initializeClass(String className, long tolerableMillis)
  {
    long start = System.currentTimeMillis();
    try {
      Class.forName(className);
    } catch (Throwable t) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to load class [{}] {}.", className, StackTraceUtil.stackTrace(t));
      }
    }
    
    long duration = System.currentTimeMillis() - start;
    if (duration > tolerableMillis) {
      logger.warn("{}.<clinit> duration: {} millis.", className, Long.valueOf(duration));
    }
  }
  
  public static void checkClass(String className, String message) {
    try {
      Class.forName(className);
    } catch (Throwable t) {
      throw new RuntimeException(message, t);
    }
  }
  
  private ClassUtil() {}
}
