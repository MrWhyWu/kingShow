package org.jupiter.monitor.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.ConsoleReporter.Builder;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.internal.UnsafeReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;
import org.jupiter.rpc.metric.Metrics;



























public class MetricsReporter
{
  private static final UnsafeReferenceFieldUpdater<ByteArrayOutputStream, byte[]> bufUpdater = UnsafeUpdater.newReferenceFieldUpdater(ByteArrayOutputStream.class, "buf");
  

  private static final ByteArrayOutputStream buf = new ByteArrayOutputStream();
  private static final PrintStream output = new PrintStream(buf);
  private static final ConsoleReporter reporter = ConsoleReporter.forRegistry(Metrics.metricRegistry()).outputTo(output).build();
  
  public MetricsReporter() {}
  
  public static synchronized String report() {
    reporter.report();
    return consoleOutput();
  }
  
  private static String consoleOutput() {
    String output;
    try {
      String output = buf.toString("UTF-8");
      assert (bufUpdater != null);
      if (((byte[])bufUpdater.get(buf)).length > 65536) {
        bufUpdater.set(buf, new byte[32768]);
      }
    } catch (UnsupportedEncodingException e) {
      output = StackTraceUtil.stackTrace(e);
    }
    return output;
  }
}
