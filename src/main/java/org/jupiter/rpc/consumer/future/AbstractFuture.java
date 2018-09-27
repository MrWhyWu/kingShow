package org.jupiter.rpc.consumer.future;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.jupiter.common.util.Signal;
import org.jupiter.common.util.internal.UnsafeUtil;
import sun.misc.Unsafe;
























public abstract class AbstractFuture<V>
{
  protected static final Signal TIMEOUT = Signal.valueOf(AbstractFuture.class, "TIMEOUT");
  

  protected static final long SPIN_FOR_TIMEOUT_THRESHOLD = 1000L;
  

  private volatile int state;
  

  protected static final int NEW = 0;
  

  protected static final int COMPLETING = 1;
  

  protected static final int NORMAL = 2;
  

  protected static final int EXCEPTIONAL = 3;
  
  private Object outcome;
  
  private volatile WaitNode waiters;
  

  public AbstractFuture()
  {
    state = 0;
  }
  


  public boolean isDone()
  {
    return state > 1;
  }
  
  protected int state() {
    return state;
  }
  


  protected Object outcome()
  {
    return outcome;
  }
  
  protected V get() throws Throwable {
    int s = state;
    if (s <= 1) {
      s = awaitDone(false, 0L);
    }
    return report(s);
  }
  
  protected V get(long timeout, TimeUnit unit) throws Throwable {
    if (unit == null) {
      throw new NullPointerException("unit");
    }
    int s = state;
    if ((s <= 1) && ((s = awaitDone(true, unit.toNanos(timeout))) <= 1)) {
      throw TIMEOUT;
    }
    return report(s);
  }
  
  protected void set(V v) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, 0, 1)) {
      outcome = v;
      
      UNSAFE.putOrderedInt(this, stateOffset, 2);
      finishCompletion(v);
    }
  }
  
  protected void setException(Throwable t) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, 0, 1)) {
      outcome = t;
      
      UNSAFE.putOrderedInt(this, stateOffset, 3);
      finishCompletion(t);
    }
  }
  


  protected abstract void done(int paramInt, Object paramObject);
  


  private V report(int s)
    throws Throwable
  {
    Object x = outcome;
    if (s == 2) {
      return x;
    }
    throw ((Throwable)x);
  }
  


  private void finishCompletion(Object x)
  {
    WaitNode q;
    
    while ((q = waiters) != null) {
      if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
        for (;;) {
          Thread t = thread;
          if (t != null) {
            thread = null;
            LockSupport.unpark(t);
          }
          WaitNode next = next;
          if (next == null) {
            break;
          }
          next = null;
          q = next;
        }
      }
    }
    

    done(state, x);
  }
  








  private int awaitDone(boolean timed, long nanos)
    throws InterruptedException
  {
    long startTime = 0L;
    WaitNode q = null;
    boolean queued = false;
    for (;;) {
      int s = state;
      if (s > 1) {
        if (q != null) {
          thread = null;
        }
        return s; }
      if (s == 1) {
        Thread.yield();
      } else { if (Thread.interrupted()) {
          removeWaiter(q);
          throw new InterruptedException(); }
        if (q == null) {
          if ((timed) && (nanos <= 0L)) {
            return s;
          }
          q = new WaitNode();
        } else if (!queued) {
          queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
        } else if (timed) { long parkNanos;
          long parkNanos;
          if (startTime == 0L) {
            startTime = System.nanoTime();
            if (startTime == 0L) {
              startTime = 1L;
            }
            parkNanos = nanos;
          } else {
            long elapsed = System.nanoTime() - startTime;
            if (elapsed >= nanos) {
              removeWaiter(q);
              return state;
            }
            parkNanos = nanos - elapsed;
          }
          

          if ((parkNanos > 1000L) && (state < 1))
          {
            LockSupport.parkNanos(this, parkNanos);
          }
        } else {
          LockSupport.park(this);
        }
      }
    }
  }
  
  private void removeWaiter(WaitNode node) { if (node != null) {
      thread = null;
      


      WaitNode pred = null; WaitNode s; for (WaitNode q = waiters;; q = s) { if (q == null) return;
        s = next;
        if (thread != null) {
          pred = q;
        } else { if (pred != null) {
            next = s;
            if (thread != null) continue;
            break;
          }
          if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s)) {
            break;
          }
        }
      }
    }
  }
  


  static final class WaitNode
  {
    volatile Thread thread;
    

    volatile WaitNode next;
    
    WaitNode() { thread = Thread.currentThread(); }
  }
  
  public String toString() {
    String status;
    String status;
    String status;
    switch (state) {
    case 2: 
      status = "[Completed normally]";
      break;
    case 3: 
      status = "[Completed exceptionally: " + outcome + "]";
      break;
    default: 
      status = "[Not completed]";
    }
    return super.toString() + status;
  }
  

  private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();
  private static final long stateOffset;
  private static final long waitersOffset;
  
  static {
    try {
      Class<?> k = AbstractFuture.class;
      stateOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("state"));
      waitersOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("waiters"));
    } catch (Exception e) {
      throw new Error(e);
    }
  }
}
