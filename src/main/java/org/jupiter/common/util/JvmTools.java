package org.jupiter.common.util;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
























public final class JvmTools
{
  public static List<String> jStack()
    throws Exception
  {
    List<String> stackList = new LinkedList();
    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
    for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
      Thread thread = (Thread)entry.getKey();
      StackTraceElement[] stackTraces = (StackTraceElement[])entry.getValue();
      
      stackList.add(String.format("\"%s\" tid=%s isDaemon=%s priority=%s" + JConstants.NEWLINE, new Object[] { thread.getName(), Long.valueOf(thread.getId()), Boolean.valueOf(thread.isDaemon()), Integer.valueOf(thread.getPriority()) }));
      








      stackList.add("java.lang.Thread.State: " + thread.getState() + JConstants.NEWLINE);
      
      if (stackTraces != null) {
        for (StackTraceElement s : stackTraces) {
          stackList.add("    " + s.toString() + JConstants.NEWLINE);
        }
      }
    }
    return stackList;
  }
  

  public static List<String> memoryUsage()
    throws Exception
  {
    MemoryUsage heapMemoryUsage = MXBeanHolder.memoryMxBean.getHeapMemoryUsage();
    MemoryUsage nonHeapMemoryUsage = MXBeanHolder.memoryMxBean.getNonHeapMemoryUsage();
    
    List<String> memoryUsageList = new LinkedList();
    memoryUsageList.add("********************************** Memory Usage **********************************" + JConstants.NEWLINE);
    memoryUsageList.add("Heap Memory Usage: " + heapMemoryUsage.toString() + JConstants.NEWLINE);
    memoryUsageList.add("NonHeap Memory Usage: " + nonHeapMemoryUsage.toString() + JConstants.NEWLINE);
    
    return memoryUsageList;
  }
  

  public static double memoryUsed()
    throws Exception
  {
    MemoryUsage heapMemoryUsage = MXBeanHolder.memoryMxBean.getHeapMemoryUsage();
    return heapMemoryUsage.getUsed() / heapMemoryUsage.getMax();
  }
  




  public static void jMap(String outputFile, boolean live)
    throws Exception
  {
    File file = new File(outputFile);
    if (file.exists()) {
      file.delete();
    }
    MXBeanHolder.hotSpotDiagnosticMxBean.dumpHeap(outputFile, live); }
  
  private JvmTools() {}
  
  private static class MXBeanHolder { static final MemoryMXBean memoryMxBean = ;
    static final HotSpotDiagnosticMXBean hotSpotDiagnosticMxBean = (HotSpotDiagnosticMXBean)ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
    
    private MXBeanHolder() {}
  }
}
