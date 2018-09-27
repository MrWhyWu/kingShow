package org.jupiter.rpc.tracing;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.jupiter.common.util.NetUtil;
import org.jupiter.common.util.StackTraceUtil;
import org.jupiter.common.util.StringBuilderHelper;
import org.jupiter.common.util.SystemClock;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.common.util.internal.InternalThreadLocal;
import org.jupiter.common.util.internal.logging.InternalLogger;
import org.jupiter.common.util.internal.logging.InternalLoggerFactory;































public class TracingUtil
{
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(TracingUtil.class);
  
  private static final boolean TRACING_NEEDED = SystemPropertyUtil.getBoolean("jupiter.tracing.needed", true);
  
  private static final InternalThreadLocal<TraceId> traceThreadLocal = new InternalThreadLocal();
  
  private static final int MAX_PROCESS_ID = 4194304;
  
  private static final char PID_FLAG = 'd';
  private static final String IP_16;
  private static final String PID;
  private static final long ID_BASE = 1000L;
  private static final long ID_MASK = 8191L;
  private static final AtomicLong sequence = new AtomicLong();
  
  static {
    String _ip_16;
    try {
      String ip = SystemPropertyUtil.get("jupiter.local.address", NetUtil.getLocalAddress());
      _ip_16 = getIP_16(ip);
    } catch (Throwable t) { String _ip_16;
      _ip_16 = "ffffffff";
    }
    IP_16 = _ip_16;
    String _pid;
    try
    {
      _pid = getHexProcessId(getProcessId());
    } catch (Throwable t) { String _pid;
      _pid = "0000";
    }
    PID = _pid;
  }
  
  public static boolean isTracingNeeded() {
    return TRACING_NEEDED;
  }
  
  public static String generateTraceId() {
    return getTraceId(IP_16, SystemClock.millisClock().now(), getNextId());
  }
  
  public static TraceId getCurrent() {
    TraceId traceId = null;
    if (TRACING_NEEDED) {
      traceId = (TraceId)traceThreadLocal.get();
    }
    return traceId != null ? traceId : TraceId.NULL_TRACE_ID;
  }
  
  public static void setCurrent(TraceId traceId) {
    if (traceId == null) {
      traceThreadLocal.remove();
    } else {
      traceThreadLocal.set(traceId);
    }
  }
  
  public static TraceId safeGetTraceId(TraceId traceId) {
    return traceId == null ? TraceId.NULL_TRACE_ID : traceId;
  }
  
  public static void clearCurrent() {
    traceThreadLocal.remove();
  }
  
  private static String getHexProcessId(int pid)
  {
    if (pid < 0) {
      pid = 0;
    }
    if (pid > 65535) {
      String strPid = Integer.toString(pid);
      strPid = strPid.substring(strPid.length() - 4, strPid.length());
      pid = Integer.parseInt(strPid);
    }
    StringBuilder buf = new StringBuilder(Integer.toHexString(pid));
    while (buf.length() < 4) {
      buf.insert(0, "0");
    }
    return buf.toString();
  }
  




  private static int getProcessId()
  {
    String value = "";
    try {
      RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
      value = runtime.getName();
    } catch (Throwable t) {
      if (logger.isDebugEnabled()) {
        logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(), {}.", StackTraceUtil.stackTrace(t));
      }
    }
    

    int atIndex = value.indexOf('@');
    if (atIndex >= 0) {
      value = value.substring(0, atIndex);
    }
    
    int pid = -1;
    try {
      pid = Integer.parseInt(value);
    }
    catch (NumberFormatException localNumberFormatException) {}
    

    if ((pid < 0) || (pid > 4194304)) {
      pid = ThreadLocalRandom.current().nextInt(4194305);
      
      logger.warn("Failed to find the current process ID from '{}'; using a random value: {}.", value, Integer.valueOf(pid));
    }
    
    return pid;
  }
  
  private static String getIP_16(String ip) {
    String[] segments = ip.split("\\.");
    StringBuilder buf = StringBuilderHelper.get();
    for (String s : segments) {
      String hex = Integer.toHexString(Integer.parseInt(s));
      if (hex.length() == 1) {
        buf.append('0');
      }
      buf.append(hex);
    }
    return buf.toString();
  }
  
  private static String getTraceId(String ip_16, long timestamp, long nextId) {
    StringBuilder buf = StringBuilderHelper.get().append(ip_16).append(timestamp).append(nextId).append('d').append(PID);
    




    return buf.toString();
  }
  
  private static long getNextId()
  {
    return (sequence.incrementAndGet() & 0x1FFF) + 1000L;
  }
  
  public TracingUtil() {}
}
