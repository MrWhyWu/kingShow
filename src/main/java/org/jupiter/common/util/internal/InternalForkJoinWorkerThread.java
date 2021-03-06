package org.jupiter.common.util.internal;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;





















public class InternalForkJoinWorkerThread
  extends ForkJoinWorkerThread
{
  private InternalThreadLocalMap threadLocalMap;
  
  public InternalForkJoinWorkerThread(ForkJoinPool pool)
  {
    super(pool);
  }
  



  public final InternalThreadLocalMap threadLocalMap()
  {
    return threadLocalMap;
  }
  



  public final void setThreadLocalMap(InternalThreadLocalMap threadLocalMap)
  {
    this.threadLocalMap = threadLocalMap;
  }
}
