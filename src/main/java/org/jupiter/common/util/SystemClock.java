package org.jupiter.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jupiter.common.util.internal.UnsafeUtil;
import sun.misc.Unsafe;

















































public class SystemClock
  extends RhsTimePadding
{
  private static final long NOW_VALUE_OFFSET;
  
  static
  {
    try
    {
      NOW_VALUE_OFFSET = UnsafeUtil.objectFieldOffset(Time.class.getDeclaredField("now"));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
  
  private static final SystemClock millisClock = new SystemClock(1L);
  private final long precision;
  
  public static SystemClock millisClock()
  {
    return millisClock;
  }
  
  private SystemClock(long precision) {
    now = System.currentTimeMillis();
    this.precision = precision;
    scheduleClockUpdating();
  }
  
  private void scheduleClockUpdating() {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory()
    {
      public Thread newThread(Runnable runnable)
      {
        Thread t = new Thread(runnable, "system.clock");
        t.setDaemon(true);
        return t;
      }
      
    });
    scheduler.scheduleAtFixedRate(new Runnable()
    {


      public void run() {
        UnsafeUtil.getUnsafe().putOrderedLong(SystemClock.this, SystemClock.NOW_VALUE_OFFSET, System.currentTimeMillis()); } }, precision, precision, TimeUnit.MILLISECONDS);
  }
  

  public long now()
  {
    return now;
  }
  
  public long precision() {
    return precision;
  }
}
