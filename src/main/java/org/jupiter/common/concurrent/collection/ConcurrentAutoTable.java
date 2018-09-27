package org.jupiter.common.concurrent.collection;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUtil;
import sun.misc.Unsafe;













































public class ConcurrentAutoTable
  implements Serializable
{
  private static final long serialVersionUID = -754466836461919739L;
  private static Unsafe unsafe = ;
  



  private volatile CAT _cat;
  



  public void add(long x)
  {
    add_if(x);
  }
  


  public void decrement()
  {
    add_if(-1L);
  }
  


  public void increment()
  {
    add_if(1L);
  }
  



  public void set(long x)
  {
    CAT newcat = new CAT(null, 4, x);
    
    while (!CAS_cat(_cat, newcat)) {}
  }
  




  public long get()
  {
    return _cat.sum();
  }
  


  public int intValue()
  {
    return (int)_cat.sum();
  }
  


  public long longValue()
  {
    return _cat.sum();
  }
  



  public long estimate_get()
  {
    return _cat.estimate_sum();
  }
  


  public String toString()
  {
    return _cat.toString();
  }
  



  public void print()
  {
    _cat.print();
  }
  



  public int internal_size()
  {
    return _cat._t.length;
  }
  


  private long add_if(long x)
  {
    return _cat.add_if(x, hash(), this);
  }
  

  public ConcurrentAutoTable() { _cat = new CAT(null, 16, 0L); }
  private static AtomicReferenceFieldUpdater<ConcurrentAutoTable, CAT> _catUpdater = AtomicReferenceFieldUpdater.newUpdater(ConcurrentAutoTable.class, CAT.class, "_cat");
  
  private boolean CAS_cat(CAT oldcat, CAT newcat)
  {
    return _catUpdater.compareAndSet(this, oldcat, newcat);
  }
  

  private static int hash()
  {
    int h = System.identityHashCode(Thread.currentThread());
    return h << 3;
  }
  

  private static class CAT
    implements Serializable
  {
    private static final int _Lbase = ConcurrentAutoTable.unsafe.arrayBaseOffset([J.class);
    private static final int _Lscale = ConcurrentAutoTable.unsafe.arrayIndexScale([J.class);
    private final CAT _next;
    
    private static long rawIndex(long[] ary, int i) { assert ((i >= 0) && (i < ary.length));
      return _Lbase + i * _Lscale;
    }
    
    private static boolean CAS(long[] A, int idx, long old, long nnn) {
      return ConcurrentAutoTable.unsafe.compareAndSwapLong(A, rawIndex(A, idx), old, nnn);
    }
    

    private volatile long _fuzzy_sum_cache;
    
    private volatile long _fuzzy_time;
    
    private static final int MAX_SPIN = 1;
    
    private final long[] _t;
    
    CAT(CAT next, int sz, long init)
    {
      _next = next;
      _t = new long[sz];
      _t[0] = init;
    }
    


    public long add_if(long x, int hash, ConcurrentAutoTable master)
    {
      long[] t = _t;
      int idx = hash & t.length - 1;
      
      long old = t[idx];
      boolean ok = CAS(t, idx, old, old + x);
      if (ok) { return old;
      }
      int cnt = 0;
      for (;;) {
        old = t[idx];
        if (CAS(t, idx, old, old + x)) break;
        cnt++;
      }
      if (cnt < 1) return old;
      if (t.length >= 1048576) { return old;
      }
      





      if (_cat != this) { return old;
      }
      







      CAT newcat = new CAT(this, t.length * 2, 0L);
      


      while ((_cat == this) && (!master.CAS_cat(this, newcat))) {}
      return old;
    }
    


    public long sum()
    {
      long sum = _next == null ? 0L : _next.sum();
      long[] t = _t;
      for (long cnt : t) sum += cnt;
      return sum;
    }
    


    public long estimate_sum()
    {
      if (_t.length <= 64) { return sum();
      }
      long millis = System.currentTimeMillis();
      if (_fuzzy_time != millis) {
        _fuzzy_sum_cache = sum();
        _fuzzy_time = millis;
      }
      return _fuzzy_sum_cache;
    }
    
    public String toString() {
      return Long.toString(sum());
    }
    
    public void print() {
      long[] t = _t;
      System.out.print("[" + t[0]);
      for (int i = 1; i < t.length; i++)
        System.out.print("," + t[i]);
      System.out.print("]");
      if (_next != null) _next.print();
    }
  }
}
