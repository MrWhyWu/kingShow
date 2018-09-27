package io.netty.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract interface Timer
{
  public abstract Timeout newTimeout(TimerTask paramTimerTask, long paramLong, TimeUnit paramTimeUnit);
  
  public abstract Set<Timeout> stop();
}


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.Timer
 * JD-Core Version:    0.7.0.1
 */