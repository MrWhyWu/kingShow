package org.jupiter.common.concurrent.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUtil;
import sun.misc.Unsafe;












































































public class NonBlockingHashMapLong<TypeV>
  extends AbstractMap<Long, TypeV>
  implements ConcurrentMap<Long, TypeV>, Serializable
{
  private static final long serialVersionUID = 1234123412341234124L;
  private static Unsafe unsafe;
  private static final int REPROBE_LIMIT = 10;
  private static final int _Obase;
  private static final int _Oscale;
  private static final int _Lbase;
  private static final int _Lscale;
  private static final long _chm_offset;
  private static final long _val_1_offset;
  private transient CHM _chm;
  private transient Object _val_1;
  private transient long _last_resize_milli;
  private final boolean _opt_for_space;
  private static final int MIN_SIZE_LOG = 4;
  private static final int MIN_SIZE = 16;
  
  private static long rawIndex(Object[] ary, int idx)
  {
    assert ((idx >= 0) && (idx < ary.length));
    return _Obase + idx * _Oscale;
  }
  


  private static long rawIndex(long[] ary, int idx)
  {
    assert ((idx >= 0) && (idx < ary.length));
    return _Lbase + idx * _Lscale;
  }
  
  static
  {
    unsafe = UnsafeUtil.getUnsafe();
    



    _Obase = unsafe.arrayBaseOffset([Ljava.lang.Object.class);
    _Oscale = unsafe.arrayIndexScale([Ljava.lang.Object.class);
    





    _Lbase = unsafe.arrayBaseOffset([J.class);
    _Lscale = unsafe.arrayIndexScale([J.class);
    










    try
    {
      f = NonBlockingHashMapLong.class.getDeclaredField("_chm");
    } catch (NoSuchFieldException e) { Field f;
      throw new RuntimeException(e); }
    Field f;
    _chm_offset = unsafe.objectFieldOffset(f);
    try
    {
      f = NonBlockingHashMapLong.class.getDeclaredField("_val_1");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    _val_1_offset = unsafe.objectFieldOffset(f);
  }
  
  private final boolean CAS(long offset, Object old, Object nnn) {
    return unsafe.compareAndSwapObject(this, offset, old, nnn);
  }
  
  private static final class Prime
  {
    final Object _V;
    
    Prime(Object V) {
      _V = V;
    }
    
    static Object unbox(Object V) {
      return (V instanceof Prime) ? _V : V;
    }
  }
  





















  private static final Object NO_MATCH_OLD = new Object();
  

  private static final Object MATCH_ANY = new Object();
  

  private static final Object TOMBSTONE = new Object();
  



  private static final Prime TOMBPRIME = new Prime(TOMBSTONE);
  



  private static final long NO_KEY = 0L;
  




  public final void print()
  {
    System.out.println("=========");
    print_impl(-99, 0L, _val_1);
    _chm.print();
    System.out.println("=========");
  }
  
  private static void print_impl(int i, long K, Object V) {
    String p = (V instanceof Prime) ? "prime_" : "";
    Object V2 = Prime.unbox(V);
    String VS = V2 == TOMBSTONE ? "tombstone" : V2.toString();
    System.out.println("[" + i + "]=(" + K + "," + p + VS + ")");
  }
  
  private void print2() {
    System.out.println("=========");
    print2_impl(-99, 0L, _val_1);
    _chm.print();
    System.out.println("=========");
  }
  
  private static void print2_impl(int i, long K, Object V) {
    if ((V != null) && (Prime.unbox(V) != TOMBSTONE)) {
      print_impl(i, K, V);
    }
  }
  



  private static int reprobe_limit(int len)
  {
    return 10 + (len >> 8);
  }
  






  public NonBlockingHashMapLong()
  {
    this(16, true);
  }
  






  public NonBlockingHashMapLong(int initial_sz)
  {
    this(initial_sz, true);
  }
  





  public NonBlockingHashMapLong(boolean opt_for_space)
  {
    this(1, opt_for_space);
  }
  





  public NonBlockingHashMapLong(int initial_sz, boolean opt_for_space)
  {
    _opt_for_space = opt_for_space;
    initialize(initial_sz);
  }
  
  private void initialize(int initial_sz) {
    if (initial_sz < 0) { throw new IllegalArgumentException();
    }
    for (int i = 4; 1 << i < initial_sz; i++) {}
    _chm = new CHM(this, new ConcurrentAutoTable(), i);
    _val_1 = TOMBSTONE;
    _last_resize_milli = System.currentTimeMillis();
  }
  






  public int size()
  {
    return (_val_1 == TOMBSTONE ? 0 : 1) + _chm.size();
  }
  




  public boolean containsKey(long key)
  {
    return get(key) != null;
  }
  










  public boolean contains(Object val)
  {
    return containsValue(val);
  }
  










  public TypeV put(long key, TypeV val)
  {
    return putIfMatch(key, val, NO_MATCH_OLD);
  }
  








  public TypeV putIfAbsent(long key, TypeV val)
  {
    return putIfMatch(key, val, TOMBSTONE);
  }
  






  public TypeV remove(long key)
  {
    return putIfMatch(key, TOMBSTONE, NO_MATCH_OLD);
  }
  





  public boolean remove(long key, Object val)
  {
    return putIfMatch(key, TOMBSTONE, val) == val;
  }
  





  public TypeV replace(long key, TypeV val)
  {
    return putIfMatch(key, val, MATCH_ANY);
  }
  





  public boolean replace(long key, TypeV oldValue, TypeV newValue)
  {
    return putIfMatch(key, newValue, oldValue) == oldValue;
  }
  
  private TypeV putIfMatch(long key, Object newVal, Object oldVal) {
    if ((oldVal == null) || (newVal == null)) throw new NullPointerException();
    if (key == 0L) {
      Object curVal = _val_1;
      if ((oldVal == NO_MATCH_OLD) || (curVal == oldVal) || ((oldVal == MATCH_ANY) && (curVal != TOMBSTONE)) || (oldVal.equals(curVal)))
      {


        if (!CAS(_val_1_offset, curVal, newVal))
          curVal = _val_1;
      }
      return curVal == TOMBSTONE ? null : curVal;
    }
    Object res = _chm.putIfMatch(key, newVal, oldVal);
    assert (!(res instanceof Prime));
    assert (res != null);
    return res == TOMBSTONE ? null : res;
  }
  


  public void clear()
  {
    CHM newchm = new CHM(this, new ConcurrentAutoTable(), 4);
    while (!CAS(_chm_offset, _chm, newchm)) {}
    CAS(_val_1_offset, _val_1, TOMBSTONE);
  }
  








  public boolean containsValue(Object val)
  {
    if (val == null) return false;
    if (val == _val_1) return true;
    for (TypeV V : values())
      if ((V == val) || (V.equals(val)))
        return true;
    return false;
  }
  












  public final TypeV get(long key)
  {
    if (key == 0L) {
      Object V = _val_1;
      return V == TOMBSTONE ? null : V;
    }
    Object V = _chm.get_impl(key);
    assert (!(V instanceof Prime));
    assert (V != TOMBSTONE);
    return V;
  }
  


  public TypeV get(Object key)
  {
    return (key instanceof Long) ? get(((Long)key).longValue()) : null;
  }
  


  public TypeV remove(Object key)
  {
    return (key instanceof Long) ? remove(((Long)key).longValue()) : null;
  }
  


  public boolean remove(Object key, Object Val)
  {
    return ((key instanceof Long)) && (remove(((Long)key).longValue(), Val));
  }
  


  public boolean containsKey(Object key)
  {
    return ((key instanceof Long)) && (containsKey(((Long)key).longValue()));
  }
  


  public TypeV putIfAbsent(Long key, TypeV val)
  {
    return putIfAbsent(key.longValue(), val);
  }
  


  public TypeV replace(Long key, TypeV Val)
  {
    return replace(key.longValue(), Val);
  }
  


  public TypeV put(Long key, TypeV val)
  {
    return put(key.longValue(), val);
  }
  


  public boolean replace(Long key, TypeV oldValue, TypeV newValue)
  {
    return replace(key.longValue(), oldValue, newValue);
  }
  







  private void help_copy()
  {
    CHM topchm = _chm;
    if (_newchm == null) return;
    topchm.help_copy_impl(false);
  }
  

  private static final class CHM
    implements Serializable
  {
    final NonBlockingHashMapLong _nbhml;
    private final ConcurrentAutoTable _size;
    private final ConcurrentAutoTable _slots;
    volatile CHM _newchm;
    
    public int size()
    {
      return (int)_size.get();
    }
    











    public int slots()
    {
      return (int)_slots.get();
    }
    








    private static final AtomicReferenceFieldUpdater<CHM, CHM> _newchmUpdater = AtomicReferenceFieldUpdater.newUpdater(CHM.class, CHM.class, "_newchm");
    volatile long _resizers;
    
    boolean CAS_newchm(CHM newchm)
    {
      return _newchmUpdater.compareAndSet(this, null, newchm);
    }
    













    private static final AtomicLongFieldUpdater<CHM> _resizerUpdater = AtomicLongFieldUpdater.newUpdater(CHM.class, "_resizers");
    final long[] _keys;
    final Object[] _vals;
    
    private boolean CAS_key(int idx, long old, long key)
    {
      return NonBlockingHashMapLong.unsafe.compareAndSwapLong(_keys, NonBlockingHashMapLong.rawIndex(_keys, idx), old, key);
    }
    
    private boolean CAS_val(int idx, Object old, Object val) {
      return NonBlockingHashMapLong.unsafe.compareAndSwapObject(_vals, NonBlockingHashMapLong.rawIndex(_vals, idx), old, val);
    }
    



    CHM(NonBlockingHashMapLong nbhml, ConcurrentAutoTable size, int logsize)
    {
      _nbhml = nbhml;
      _size = size;
      _slots = new ConcurrentAutoTable();
      _keys = new long[1 << logsize];
      _vals = new Object[1 << logsize];
    }
    
    private void print()
    {
      for (int i = 0; i < _keys.length; i++) {
        long K = _keys[i];
        if (K != 0L)
          NonBlockingHashMapLong.print_impl(i, K, _vals[i]);
      }
      CHM newchm = _newchm;
      if (newchm != null) {
        System.out.println("----");
        newchm.print();
      }
    }
    
    private void print2()
    {
      for (int i = 0; i < _keys.length; i++) {
        long K = _keys[i];
        if (K != 0L)
          NonBlockingHashMapLong.print2_impl(i, K, _vals[i]);
      }
      CHM newchm = _newchm;
      if (newchm != null) {
        System.out.println("----");
        newchm.print2();
      }
    }
    

    private Object get_impl(long key)
    {
      int len = _keys.length;
      int idx = (int)(key & len - 1);
      

      int reprobe_cnt = 0;
      for (;;) {
        long K = _keys[idx];
        Object V = _vals[idx];
        if (K == 0L) { return null;
        }
        
        if (key == K)
        {
          if (!(V instanceof NonBlockingHashMapLong.Prime)) {
            if (V == NonBlockingHashMapLong.TOMBSTONE) { return null;
            }
            

            CHM newchm = _newchm;
            return V;
          }
          

          return copy_slot_and_check(idx, Long.valueOf(key)).get_impl(key);
        }
        


        reprobe_cnt++; if (reprobe_cnt >= NonBlockingHashMapLong.reprobe_limit(len)) {
          return _newchm == null ? null : copy_slot_and_check(idx, Long.valueOf(key)).get_impl(key);
        }
        

        idx = idx + 1 & len - 1;
      }
    }
    





    private Object putIfMatch(long key, Object putval, Object expVal)
    {
      assert (putval != null);
      assert (!(putval instanceof NonBlockingHashMapLong.Prime));
      assert (!(expVal instanceof NonBlockingHashMapLong.Prime));
      int len = _keys.length;
      int idx = (int)(key & len - 1);
      


      int reprobe_cnt = 0;
      Object V;
      for (;;)
      {
        V = _vals[idx];
        long K = _keys[idx];
        if (K == 0L)
        {

          if (putval == NonBlockingHashMapLong.TOMBSTONE) { return putval;
          }
          if (CAS_key(idx, 0L, key)) {
            _slots.add(1L);
            break;
          }
          












          K = _keys[idx];
          assert (K != 0L);
        }
        
        if (K == key) {
          break;
        }
        


        reprobe_cnt++; if (reprobe_cnt >= NonBlockingHashMapLong.reprobe_limit(len))
        {


          CHM newchm = resize();
          if (expVal != null) _nbhml.help_copy();
          return newchm.putIfMatch(key, putval, expVal);
        }
        
        idx = idx + 1 & len - 1;
      }
      





      if (putval == V) { return V;
      }
      


      if (((V == null) && (tableFull(reprobe_cnt, len))) || ((V instanceof NonBlockingHashMapLong.Prime)))
      {


        resize();
        return copy_slot_and_check(idx, expVal).putIfMatch(key, putval, expVal);
      }
      









      if ((expVal != NonBlockingHashMapLong.NO_MATCH_OLD) && (V != expVal) && ((expVal != NonBlockingHashMapLong.MATCH_ANY) || (V == NonBlockingHashMapLong.TOMBSTONE) || (V == null)) && ((V != null) || (expVal != NonBlockingHashMapLong.TOMBSTONE)) && ((expVal == null) || (!expVal.equals(V))))
      {



        return V;
      }
      
      if (CAS_val(idx, V, putval))
      {


        if (expVal != null)
        {
          if (((V == null) || (V == NonBlockingHashMapLong.TOMBSTONE)) && (putval != NonBlockingHashMapLong.TOMBSTONE)) _size.add(1L);
          if ((V != null) && (V != NonBlockingHashMapLong.TOMBSTONE) && (putval == NonBlockingHashMapLong.TOMBSTONE)) _size.add(-1L);
        }
      } else {
        V = _vals[idx];
        


        if ((V instanceof NonBlockingHashMapLong.Prime)) {
          return copy_slot_and_check(idx, expVal).putIfMatch(key, putval, expVal);
        }
      }
      

      return (V == null) && (expVal != null) ? NonBlockingHashMapLong.TOMBSTONE : V;
    }
    








    private boolean tableFull(int reprobe_cnt, int len)
    {
      return (reprobe_cnt >= 10) && (_slots.estimate_get() >= NonBlockingHashMapLong.reprobe_limit(len) * 2);
    }
    










    private CHM resize()
    {
      CHM newchm = _newchm;
      if (newchm != null) {
        return newchm;
      }
      
      int oldlen = _keys.length;
      int sz = size();
      int newsz = sz;
      


      if (_nbhml._opt_for_space)
      {
        if (sz >= oldlen >> 1) {
          newsz = oldlen << 1;
        }
      } else if (sz >= oldlen >> 2) {
        newsz = oldlen << 1;
        if (sz >= oldlen >> 1) {
          newsz = oldlen << 2;
        }
      }
      


      long tm = System.currentTimeMillis();
      if ((newsz <= oldlen) && (tm <= _nbhml._last_resize_milli + 10000L))
      {


        newsz = oldlen << 1;
      }
      
      if (newsz < oldlen) { newsz = oldlen;
      }
      


      for (int log2 = 4; 1 << log2 < newsz; log2++) {}
      



      long r = _resizers;
      while (!_resizerUpdater.compareAndSet(this, r, r + 1L)) {
        r = _resizers;
      }
      

      int megs = (1 << log2 << 1) + 4 << 3 >> 20;
      if ((r >= 2L) && (megs > 0)) {
        newchm = _newchm;
        if (newchm != null) {
          return newchm;
        }
        


        try
        {
          Thread.sleep(8 * megs);
        }
        catch (Exception localException) {}
      }
      
      newchm = _newchm;
      if (newchm != null) {
        return newchm;
      }
      
      newchm = new CHM(_nbhml, _size, log2);
      

      if (_newchm != null) {
        return _newchm;
      }
      

      if (!CAS_newchm(newchm))
      {




        newchm = _newchm; }
      return newchm;
    }
    







    volatile long _copyIdx = 0L;
    private static final AtomicLongFieldUpdater<CHM> _copyIdxUpdater = AtomicLongFieldUpdater.newUpdater(CHM.class, "_copyIdx");
    




    volatile long _copyDone = 0L;
    private static final AtomicLongFieldUpdater<CHM> _copyDoneUpdater = AtomicLongFieldUpdater.newUpdater(CHM.class, "_copyDone");
    




    private void help_copy_impl(boolean copy_all)
    {
      CHM newchm = _newchm;
      assert (newchm != null);
      int oldlen = _keys.length;
      int MIN_COPY_WORK = Math.min(oldlen, 1024);
      

      int panic_start = -1;
      int copyidx = 55537;
      while (_copyDone < oldlen)
      {









        if (panic_start == -1) {
          copyidx = (int)_copyIdx;
          while ((copyidx < oldlen << 1) && (!_copyIdxUpdater.compareAndSet(this, copyidx, copyidx + MIN_COPY_WORK)))
          {
            copyidx = (int)_copyIdx; }
          if (copyidx >= oldlen << 1) {
            panic_start = copyidx;
          }
        }
        
        int workdone = 0;
        for (int i = 0; i < MIN_COPY_WORK; i++)
          if (copy_slot(copyidx + i & oldlen - 1))
            workdone++;
        if (workdone > 0) {
          copy_check_and_promote(workdone);
        }
        


        copyidx += MIN_COPY_WORK;
        

        if ((!copy_all) && (panic_start == -1)) {
          return;
        }
      }
      
      copy_check_and_promote(0);
    }
    













    private CHM copy_slot_and_check(int idx, Object should_help)
    {
      assert (_newchm != null);
      if (copy_slot(idx)) {
        copy_check_and_promote(1);
      }
      if (should_help != null) _nbhml.help_copy();
      return _newchm;
    }
    
    private void copy_check_and_promote(int workdone)
    {
      int oldlen = _keys.length;
      
      long copyDone = _copyDone;
      long nowDone = copyDone + workdone;
      assert (nowDone <= oldlen);
      for (; (workdone > 0) && 
            (!_copyDoneUpdater.compareAndSet(this, copyDone, nowDone)); 
          

          throw new AssertionError())
      {
        label43:
        copyDone = _copyDone;
        nowDone = copyDone + workdone;
        if (($assertionsDisabled) || (nowDone <= oldlen)) {
          break label43;
        }
      }
      




      if ((nowDone == oldlen) && (_nbhml._chm == this) && (_nbhml.CAS(NonBlockingHashMapLong._chm_offset, this, _newchm)))
      {


        _nbhml._last_resize_milli = System.currentTimeMillis();
      }
    }
    









    private boolean copy_slot(int idx)
    {
      long key;
      








      while ((key = _keys[idx]) == 0L) {
        CAS_key(idx, 0L, idx + _keys.length);
      }
      


      Object oldval = _vals[idx];
      while (!(oldval instanceof NonBlockingHashMapLong.Prime)) {
        NonBlockingHashMapLong.Prime box = (oldval == null) || (oldval == NonBlockingHashMapLong.TOMBSTONE) ? NonBlockingHashMapLong.TOMBPRIME : new NonBlockingHashMapLong.Prime(oldval);
        if (CAS_val(idx, oldval, box))
        {




          if (box == NonBlockingHashMapLong.TOMBPRIME) {
            return true;
          }
          
          oldval = box;
          break;
        }
        oldval = _vals[idx];
      }
      if (oldval == NonBlockingHashMapLong.TOMBPRIME) { return false;
      }
      






      Object old_unboxed = _V;
      assert (old_unboxed != NonBlockingHashMapLong.TOMBSTONE);
      boolean copied_into_new = _newchm.putIfMatch(key, old_unboxed, null) == null;
      





      while (!CAS_val(idx, oldval, NonBlockingHashMapLong.TOMBPRIME)) {
        oldval = _vals[idx];
      }
      return copied_into_new;
    }
  }
  
  private class SnapshotV implements Iterator<TypeV>, Enumeration<TypeV> { final NonBlockingHashMapLong.CHM _sschm;
    private int _idx;
    private long _nextK;
    private long _prevK;
    private TypeV _nextV;
    private TypeV _prevV;
    
    public SnapshotV() { NonBlockingHashMapLong.CHM topchm;
      for (;;) { topchm = _chm;
        if (_newchm == null) {
          break;
        }
        
        NonBlockingHashMapLong.CHM.access$300(topchm, true);
      }
      


      _sschm = topchm;
      
      _idx = -1;
      next();
    }
    
    int length() {
      return _sschm._keys.length;
    }
    
    long key(int idx) {
      return _sschm._keys[idx];
    }
    



    public boolean hasNext()
    {
      return _nextV != null;
    }
    




    public TypeV next()
    {
      if ((_idx != -1) && (_nextV == null)) throw new NoSuchElementException();
      _prevK = _nextK;
      _prevV = _nextV;
      _nextV = null;
      

      if (_idx == -1) {
        _idx = 0;
        _nextK = 0L;
        if ((this._nextV = get(_nextK)) != null) return _prevV;
      }
      while (_idx < length()) {
        _nextK = key(_idx++);
        if ((_nextK != 0L) && ((this._nextV = get(_nextK)) != null)) {
          break;
        }
      }
      return _prevV;
    }
    
    public void remove() {
      if (_prevV == null) throw new IllegalStateException();
      NonBlockingHashMapLong.CHM.access$100(_sschm, _prevK, NonBlockingHashMapLong.TOMBSTONE, _prevV);
      _prevV = null;
    }
    
    public TypeV nextElement() {
      return next();
    }
    
    public boolean hasMoreElements() {
      return hasNext();
    }
  }
  





  public Enumeration<TypeV> elements()
  {
    return new SnapshotV();
  }
  
















  public Collection<TypeV> values()
  {
    new AbstractCollection() {
      public void clear() {
        NonBlockingHashMapLong.this.clear();
      }
      
      public int size() {
        return NonBlockingHashMapLong.this.size();
      }
      
      public boolean contains(Object v) {
        return containsValue(v);
      }
      
      public Iterator<TypeV> iterator() {
        return new NonBlockingHashMapLong.SnapshotV(NonBlockingHashMapLong.this);
      }
    };
  }
  




  public class IteratorLong
    implements Iterator<Long>, Enumeration<Long>
  {
    private final NonBlockingHashMapLong<TypeV>.SnapshotV _ss;
    



    public IteratorLong()
    {
      _ss = new NonBlockingHashMapLong.SnapshotV(NonBlockingHashMapLong.this);
    }
    


    public void remove()
    {
      _ss.remove();
    }
    


    public Long next()
    {
      _ss.next();
      return Long.valueOf(NonBlockingHashMapLong.SnapshotV.access$2000(_ss));
    }
    


    public long nextLong()
    {
      _ss.next();
      return NonBlockingHashMapLong.SnapshotV.access$2000(_ss);
    }
    


    public boolean hasNext()
    {
      return _ss.hasNext();
    }
    


    public Long nextElement()
    {
      return next();
    }
    


    public boolean hasMoreElements()
    {
      return hasNext();
    }
  }
  






  public Enumeration<Long> keys()
  {
    return new IteratorLong();
  }
  















  public Set<Long> keySet()
  {
    new AbstractSet() {
      public void clear() {
        NonBlockingHashMapLong.this.clear();
      }
      
      public int size() {
        return NonBlockingHashMapLong.this.size();
      }
      
      public boolean contains(Object k) {
        return containsKey(k);
      }
      
      public boolean remove(Object k) {
        return remove(k) != null;
      }
      
      public NonBlockingHashMapLong<TypeV>.IteratorLong iterator() {
        return new NonBlockingHashMapLong.IteratorLong(NonBlockingHashMapLong.this);
      }
    };
  }
  


  public long[] keySetLong()
  {
    long[] dom = new long[size()];
    NonBlockingHashMapLong<TypeV>.IteratorLong i = (IteratorLong)keySet().iterator();
    int j = 0;
    while ((j < dom.length) && (i.hasNext()))
      dom[(j++)] = i.nextLong();
    return dom;
  }
  
  private class NBHMLEntry
    extends AbstractEntry<Long, TypeV>
  {
    NBHMLEntry(TypeV k)
    {
      super(v);
    }
    
    public TypeV setValue(TypeV val) {
      if (val == null) throw new NullPointerException();
      _val = val;
      return put((Long)_key, val);
    }
  }
  
  private class SnapshotE implements Iterator<Map.Entry<Long, TypeV>> {
    final NonBlockingHashMapLong<TypeV>.SnapshotV _ss;
    
    public SnapshotE() {
      _ss = new NonBlockingHashMapLong.SnapshotV(NonBlockingHashMapLong.this);
    }
    
    public void remove() {
      _ss.remove();
    }
    
    public Map.Entry<Long, TypeV> next() {
      _ss.next();
      return new NonBlockingHashMapLong.NBHMLEntry(NonBlockingHashMapLong.this, Long.valueOf(NonBlockingHashMapLong.SnapshotV.access$2000(_ss)), NonBlockingHashMapLong.SnapshotV.access$2100(_ss));
    }
    
    public boolean hasNext() {
      return _ss.hasNext();
    }
  }
  















  public Set<Map.Entry<Long, TypeV>> entrySet()
  {
    new AbstractSet() {
      public void clear() {
        NonBlockingHashMapLong.this.clear();
      }
      
      public int size() {
        return NonBlockingHashMapLong.this.size();
      }
      
      public boolean remove(Object o) {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<?, ?> e = (Map.Entry)o;
        return remove(e.getKey(), e.getValue());
      }
      
      public boolean contains(Object o) {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<?, ?> e = (Map.Entry)o;
        TypeV v = get(e.getKey());
        return v.equals(e.getValue());
      }
      
      public Iterator<Map.Entry<Long, TypeV>> iterator() {
        return new NonBlockingHashMapLong.SnapshotE(NonBlockingHashMapLong.this);
      }
    };
  }
  
  private void writeObject(ObjectOutputStream s)
    throws IOException
  {
    s.defaultWriteObject();
    for (Iterator i$ = keySet().iterator(); i$.hasNext();) { long K = ((Long)i$.next()).longValue();
      Object V = get(K);
      s.writeLong(K);
      s.writeObject(V);
    }
    s.writeLong(0L);
    s.writeObject(null);
  }
  
  private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
  {
    s.defaultReadObject();
    initialize(16);
    for (;;) {
      long K = s.readLong();
      TypeV V = s.readObject();
      if ((K == 0L) && (V == null)) break;
      put(K, V);
    }
  }
}
