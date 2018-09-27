package org.jupiter.common.util;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Formatter;






















public final class JConstants
{
  public static final String NEWLINE;
  public static final String UTF8_CHARSET = "UTF-8";
  public static final Charset UTF8;
  
  private JConstants() {}
  
  static
  {
    String newLine;
    try
    {
      newLine = new Formatter().format("%n", new Object[0]).toString();
    } catch (Exception e) { String newLine;
      newLine = "\n";
    }
    NEWLINE = newLine;
    
    Charset charset = null;
    try {
      charset = Charset.forName("UTF-8");
    } catch (UnsupportedCharsetException localUnsupportedCharsetException) {}
    UTF8 = charset;
  }
  

  public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
  

  public static final String UNKNOWN_APP_NAME = "UNKNOWN";
  
  public static final String DEFAULT_GROUP = "Jupiter";
  
  public static final String DEFAULT_VERSION = "1.0.0";
  
  public static final long DEFAULT_TIMEOUT = SystemPropertyUtil.getInt("jupiter.rpc.invoke.timeout", 3000);
  

  public static final int READER_IDLE_TIME_SECONDS = SystemPropertyUtil.getInt("jupiter.io.reader.idle.time.seconds", 60);
  

  public static final int WRITER_IDLE_TIME_SECONDS = SystemPropertyUtil.getInt("jupiter.io.writer.idle.time.seconds", 30);
  


  public static final int DEFAULT_WARM_UP = SystemPropertyUtil.getInt("jupiter.rpc.load-balancer.warm-up", 600000);
  

  public static final int DEFAULT_WEIGHT = SystemPropertyUtil.getInt("jupiter.rpc.load-balancer.default.weight", 50);
  

  public static final int MAX_WEIGHT = SystemPropertyUtil.getInt("jupiter.rpc.load-balancer.max.weight", 100);
  


  public static final int SUGGESTED_CONNECTION_COUNT = SystemPropertyUtil.getInt("jupiter.rpc.suggest.connection.count", Math.min(AVAILABLE_PROCESSORS, 4));
  


  public static final boolean METRIC_CSV_REPORTER = SystemPropertyUtil.getBoolean("jupiter.metric.csv.reporter", false);
  

  public static final String METRIC_CSV_REPORTER_DIRECTORY = SystemPropertyUtil.get("jupiter.metric.csv.reporter.directory", SystemPropertyUtil.get("user.dir"));
  

  public static final int METRIC_REPORT_PERIOD = SystemPropertyUtil.getInt("jupiter.metric.report.period", 15);
}
