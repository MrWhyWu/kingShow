package org.jupiter.rpc.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.ConsoleReporter.Builder;
import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.CsvReporter.Builder;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.Builder;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.codahale.metrics.Timer;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.util.ClassUtil;
import org.jupiter.common.util.JConstants;
import org.jupiter.common.util.Preconditions;














public class Metrics
{
  private static final MetricRegistry metricRegistry = new MetricRegistry();
  private static final ScheduledReporter scheduledReporter;
  
  static {
    ClassUtil.checkClass("org.slf4j.Logger", "Class[" + Metric.class.getName() + "] must rely on SL4J");
    

    if (JConstants.METRIC_CSV_REPORTER) {
      scheduledReporter = CsvReporter.forRegistry(metricRegistry).build(new File(JConstants.METRIC_CSV_REPORTER_DIRECTORY));
    }
    else
    {
      ScheduledReporter _reporter;
      try {
        _reporter = Slf4jReporter.forRegistry(metricRegistry).withLoggingLevel(Slf4jReporter.LoggingLevel.WARN).build();
      }
      catch (NoClassDefFoundError e)
      {
        ScheduledReporter _reporter;
        
        _reporter = ConsoleReporter.forRegistry(metricRegistry).build();
      }
      scheduledReporter = _reporter;
    }
    scheduledReporter.start(JConstants.METRIC_REPORT_PERIOD, TimeUnit.MINUTES);
  }
  


  public static MetricRegistry metricRegistry()
  {
    return metricRegistry;
  }
  



  public static Meter meter(String name)
  {
    return metricRegistry.meter((String)Preconditions.checkNotNull(name, "name"));
  }
  



  public static Meter meter(Class<?> clazz, String... names)
  {
    return metricRegistry.meter(MetricRegistry.name(clazz, names));
  }
  



  public static Timer timer(String name)
  {
    return metricRegistry.timer((String)Preconditions.checkNotNull(name, "name"));
  }
  



  public static Timer timer(Class<?> clazz, String... names)
  {
    return metricRegistry.timer(MetricRegistry.name(clazz, names));
  }
  



  public static Counter counter(String name)
  {
    return metricRegistry.counter((String)Preconditions.checkNotNull(name, "name"));
  }
  



  public static Counter counter(Class<?> clazz, String... names)
  {
    return metricRegistry.counter(MetricRegistry.name(clazz, names));
  }
  



  public static Histogram histogram(String name)
  {
    return metricRegistry.histogram((String)Preconditions.checkNotNull(name, "name"));
  }
  



  public static Histogram histogram(Class<?> clazz, String... names)
  {
    return metricRegistry.histogram(MetricRegistry.name(clazz, names));
  }
  
  private Metrics() {}
}
