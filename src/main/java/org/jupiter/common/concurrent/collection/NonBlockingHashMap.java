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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUtil;
import sun.misc.Unsafe;









































































public class NonBlockingHashMap<TypeK, TypeV>
  extends AbstractMap<TypeK, TypeV>
  implements ConcurrentMap<TypeK, TypeV>, Cloneable, Serializable
{
  private static final long serialVersionUID = 1234123412341234123L;
  private static Unsafe unsafe;
  private static final int REPROBE_LIMIT = 10;
  private static final int _Obase;
  private static final int _Oscale;
  private static final int _Olog;
  private static final long _kvs_offset;
  private transient Object[] _kvs;
  private transient long _last_resize_milli;
  private static final int MIN_SIZE_LOG = 3;
  private static final int MIN_SIZE = 8;
  
  private static long rawIndex(Object[] ary, int idx)
  {
    assert ((idx >= 0) && (idx < ary.length));
    

    return _Obase + (idx << _Olog);
  }
  
  static
  {
    unsafe = UnsafeUtil.getUnsafe();
    



    _Obase = unsafe.arrayBaseOffset([Ljava.lang.Object.class);
    _Oscale = unsafe.arrayIndexScale([Ljava.lang.Object.class);
    _Olog = _Oscale == 8 ? 3 : _Oscale == 4 ? 2 : 9999;
    











    Field f = null;
    try {
      f = NonBlockingHashMap.class.getDeclaredField("_kvs");
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    _kvs_offset = unsafe.objectFieldOffset(f);
  }
  
  private final boolean CAS_kvs(Object[] oldkvs, Object[] newkvs) {
    return unsafe.compareAndSwapObject(this, _kvs_offset, oldkvs, newkvs);
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
  

  private static final int hash(Object key)
  {
    int h = key.hashCode();
    h ^= h >>> 20 ^ h >>> 12;
    h ^= h >>> 7 ^ h >>> 4;
    return h;
  }
  













  private static final CHM chm(Object[] kvs)
  {
    return (CHM)kvs[0];
  }
  
  private static final int[] hashes(Object[] kvs) {
    return (int[])kvs[1];
  }
  
  private static final int len(Object[] kvs)
  {
    return kvs.length - 2 >> 1;
  }
  












  private static final Object NO_MATCH_OLD = new Object();
  

  private static final Object MATCH_ANY = new Object();
  

  public static final Object TOMBSTONE = new Object();
  



  private static final Prime TOMBPRIME = new Prime(TOMBSTONE);
  






  private static final Object key(Object[] kvs, int idx)
  {
    return kvs[((idx << 1) + 2)];
  }
  
  private static final Object val(Object[] kvs, int idx) {
    return kvs[((idx << 1) + 3)];
  }
  
  private static final boolean CAS_key(Object[] kvs, int idx, Object old, Object key) {
    return unsafe.compareAndSwapObject(kvs, rawIndex(kvs, (idx << 1) + 2), old, key);
  }
  
  private static final boolean CAS_val(Object[] kvs, int idx, Object old, Object val) {
    return unsafe.compareAndSwapObject(kvs, rawIndex(kvs, (idx << 1) + 3), old, val);
  }
  





  public final void print()
  {
    System.out.println("=========");
    print2(_kvs);
    System.out.println("=========");
  }
  
  private final void print(Object[] kvs)
  {
    for (int i = 0; i < len(kvs); i++) {
      Object K = key(kvs, i);
      if (K != null) {
        String KS = K == TOMBSTONE ? "XXX" : K.toString();
        Object V = val(kvs, i);
        Object U = Prime.unbox(V);
        String p = V == U ? "" : "prime_";
        String US = U == TOMBSTONE ? "tombstone" : U.toString();
        System.out.println("" + i + " (" + KS + "," + p + US + ")");
      }
    }
    Object[] newkvs = chm_newkvs;
    if (newkvs != null) {
      System.out.println("----");
      print(newkvs);
    }
  }
  
  private final void print2(Object[] kvs)
  {
    for (int i = 0; i < len(kvs); i++) {
      Object key = key(kvs, i);
      Object val = val(kvs, i);
      Object U = Prime.unbox(val);
      if ((key != null) && (key != TOMBSTONE) && (val != null) && (U != TOMBSTONE))
      {
        String p = val == U ? "" : "prime_";
        System.out.println("" + i + " (" + key + "," + p + val + ")");
      }
    }
    Object[] newkvs = chm_newkvs;
    if (newkvs != null) {
      System.out.println("----");
      print2(newkvs);
    }
  }
  

  private transient ConcurrentAutoTable _reprobes = new ConcurrentAutoTable();
  


  static volatile int DUMMY_VOLATILE;
  



  public long reprobes()
  {
    long r = _reprobes.get();
    _reprobes = new ConcurrentAutoTable();
    return r;
  }
  





  private static final int reprobe_limit(int len)
  {
    return 10 + (len >> 4);
  }
  






  public NonBlockingHashMap()
  {
    this(8);
  }
  






  public NonBlockingHashMap(int initial_sz)
  {
    initialize(initial_sz);
  }
  
  private final void initialize(int initial_sz) {
    if (initial_sz < 0) { throw new IllegalArgumentException();
    }
    if (initial_sz > 1048576) initial_sz = 1048576;
    for (int i = 3; 1 << i < initial_sz << 2; i++) {}
    
    _kvs = new Object[(1 << i << 1) + 2];
    _kvs[0] = new CHM(new ConcurrentAutoTable());
    _kvs[1] = new int[1 << i];
    _last_resize_milli = System.currentTimeMillis();
  }
  
  protected final void initialize()
  {
    initialize(8);
  }
  







  public int size()
  {
    return chm(_kvs).size();
  }
  





  public boolean isEmpty()
  {
    return size() == 0;
  }
  






  public boolean containsKey(Object key)
  {
    return get(key) != null;
  }
  










  public boolean contains(Object val)
  {
    return containsValue(val);
  }
  












  public TypeV put(TypeK key, TypeV val)
  {
    return putIfMatch(key, val, NO_MATCH_OLD);
  }
  








  public TypeV putIfAbsent(TypeK key, TypeV val)
  {
    return putIfMatch(key, val, TOMBSTONE);
  }
  








  public TypeV remove(Object key)
  {
    return putIfMatch(key, TOMBSTONE, NO_MATCH_OLD);
  }
  





  public boolean remove(Object key, Object val)
  {
    return putIfMatch(key, TOMBSTONE, val) == val;
  }
  





  public TypeV replace(TypeK key, TypeV val)
  {
    return putIfMatch(key, val, MATCH_ANY);
  }
  





  public boolean replace(TypeK key, TypeV oldValue, TypeV newValue)
  {
    return putIfMatch(key, newValue, oldValue) == oldValue;
  }
  




  public final TypeV putIfMatchAllowNull(Object key, Object newVal, Object oldVal)
  {
    if (oldVal == null) oldVal = TOMBSTONE;
    if (newVal == null) newVal = TOMBSTONE;
    TypeV res = putIfMatch(this, _kvs, key, newVal, oldVal);
    assert (!(res instanceof Prime));
    
    return res == TOMBSTONE ? null : res;
  }
  
  private final TypeV putIfMatch(Object key, Object newVal, Object oldVal) {
    if ((oldVal == null) || (newVal == null)) throw new NullPointerException();
    Object res = putIfMatch(this, _kvs, key, newVal, oldVal);
    assert (!(res instanceof Prime));
    assert (res != null);
    return res == TOMBSTONE ? null : res;
  }
  







  public void putAll(Map<? extends TypeK, ? extends TypeV> m)
  {
    for (Map.Entry<? extends TypeK, ? extends TypeV> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }
  


  public void clear()
  {
    Object[] newkvs = NonBlockingHashMap8_kvs;
    while (!CAS_kvs(_kvs, newkvs)) {}
  }
  










  public boolean containsValue(Object val)
  {
    if (val == null) throw new NullPointerException();
    for (TypeV V : values())
      if ((V == val) || (V.equals(val)))
        return true;
    return false;
  }
  














  public Object clone()
  {
    try
    {
      NonBlockingHashMap<TypeK, TypeV> t = (NonBlockingHashMap)super.clone();
      




      t.clear();
      
      for (TypeK K : keySet()) {
        TypeV V = get(K);
        t.put(K, V);
      }
      return t;
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  












  public String toString()
  {
    Iterator<Map.Entry<TypeK, TypeV>> i = entrySet().iterator();
    if (!i.hasNext()) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (;;) {
      Map.Entry<TypeK, TypeV> e = (Map.Entry)i.next();
      TypeK key = e.getKey();
      TypeV value = e.getValue();
      sb.append(key == this ? "(this Map)" : key);
      sb.append('=');
      sb.append(value == this ? "(this Map)" : value);
      if (!i.hasNext())
        return '}';
      sb.append(", ");
    }
  }
  



  private static boolean keyeq(Object K, Object key, int[] hashes, int hash, int fullhash)
  {
    return (K == key) || (((hashes[hash] == 0) || (hashes[hash] == fullhash)) && (K != TOMBSTONE) && (key.equals(K)));
  }
  



























  public TypeV get(Object key)
  {
    Object V = get_impl(this, _kvs, key);
    assert (!(V instanceof Prime));
    assert (V != TOMBSTONE);
    return V;
  }
  
  private static final Object get_impl(NonBlockingHashMap topmap, Object[] kvs, Object key) {
    int fullhash = hash(key);
    int len = len(kvs);
    CHM chm = chm(kvs);
    int[] hashes = hashes(kvs);
    
    int idx = fullhash & len - 1;
    

    int reprobe_cnt = 0;
    
    for (;;)
    {
      Object K = key(kvs, idx);
      Object V = val(kvs, idx);
      if (K == null) { return null;
      }
      








      Object[] newkvs = _newkvs;
      

      if (keyeq(K, key, hashes, idx, fullhash))
      {
        if (!(V instanceof Prime)) {
          return V == TOMBSTONE ? null : V;
        }
        
        return get_impl(topmap, chm.copy_slot_and_check(topmap, kvs, idx, key), key);
      }
      


      reprobe_cnt++; if ((reprobe_cnt >= reprobe_limit(len)) || (K == TOMBSTONE))
      {
        return newkvs == null ? null : get_impl(topmap, topmap.help_copy(newkvs), key);
      }
      idx = idx + 1 & len - 1;
    }
  }
  








  public TypeK getk(TypeK key)
  {
    return getk_impl(this, _kvs, key);
  }
  
  private static final Object getk_impl(NonBlockingHashMap topmap, Object[] kvs, Object key) {
    int fullhash = hash(key);
    int len = len(kvs);
    CHM chm = chm(kvs);
    int[] hashes = hashes(kvs);
    
    int idx = fullhash & len - 1;
    

    int reprobe_cnt = 0;
    for (;;)
    {
      Object K = key(kvs, idx);
      if (K == null) { return null;
      }
      








      Object[] newkvs = _newkvs;
      

      if (keyeq(K, key, hashes, idx, fullhash)) {
        return K;
      }
      


      reprobe_cnt++; if ((reprobe_cnt >= reprobe_limit(len)) || (K == TOMBSTONE))
      {
        return newkvs == null ? null : getk_impl(topmap, topmap.help_copy(newkvs), key);
      }
      
      idx = idx + 1 & len - 1;
    }
  }
  







  private static final Object putIfMatch(NonBlockingHashMap topmap, Object[] kvs, Object key, Object putval, Object expVal)
  {
    assert (putval != null);
    assert (!(putval instanceof Prime));
    assert (!(expVal instanceof Prime));
    int fullhash = hash(key);
    int len = len(kvs);
    CHM chm = chm(kvs);
    int[] hashes = hashes(kvs);
    int idx = fullhash & len - 1;
    


    int reprobe_cnt = 0;
    Object K = null;Object V = null;
    Object[] newkvs = null;
    for (;;) {
      V = val(kvs, idx);
      K = key(kvs, idx);
      int dummy; if (K == null)
      {

        if (putval == TOMBSTONE) return putval;
        if (expVal == MATCH_ANY) { return null;
        }
        if (CAS_key(kvs, idx, null, key)) {
          _slots.add(1L);
          hashes[idx] = fullhash;
          break;
        }
        
















        dummy = DUMMY_VOLATILE;



      }
      else
      {


        newkvs = _newkvs;
        
        if (keyeq(K, key, hashes, idx, fullhash)) {
          break;
        }
        


        reprobe_cnt++; if ((reprobe_cnt >= reprobe_limit(len)) || (K == TOMBSTONE))
        {



          newkvs = chm.resize(topmap, kvs);
          if (expVal != null) topmap.help_copy(newkvs);
          return putIfMatch(topmap, newkvs, key, putval, expVal);
        }
        
        idx = idx + 1 & len - 1;
      }
    }
    




    if (putval == V) { return V;
    }
    




    if ((newkvs == null) && (((V == null) && (chm.tableFull(reprobe_cnt, len))) || ((V instanceof Prime))))
    {









      newkvs = chm.resize(topmap, kvs);
    }
    
    if (newkvs != null) {
      return putIfMatch(topmap, chm.copy_slot_and_check(topmap, kvs, idx, expVal), key, putval, expVal);
    }
    

    assert (!(V instanceof Prime));
    





    if ((expVal != NO_MATCH_OLD) && (V != expVal) && ((expVal != MATCH_ANY) || (V == TOMBSTONE) || (V == null)) && ((V != null) || (expVal != TOMBSTONE)) && ((expVal == null) || (!expVal.equals(V))))
    {



      return V;
    }
    
    if (CAS_val(kvs, idx, V, putval))
    {


      if (expVal != null)
      {
        if (((V == null) || (V == TOMBSTONE)) && (putval != TOMBSTONE)) _size.add(1L);
        if ((V != null) && (V != TOMBSTONE) && (putval == TOMBSTONE)) _size.add(-1L);
      }
    } else {
      V = val(kvs, idx);
      


      if ((V instanceof Prime)) {
        return putIfMatch(topmap, chm.copy_slot_and_check(topmap, kvs, idx, expVal), key, putval, expVal);
      }
    }
    

    return (V == null) && (expVal != null) ? TOMBSTONE : V;
  }
  







  private final Object[] help_copy(Object[] helper)
  {
    Object[] topkvs = _kvs;
    CHM topchm = chm(topkvs);
    if (_newkvs == null) return helper;
    topchm.help_copy_impl(this, topkvs, false);
    return helper;
  }
  
  private static final class CHM<TypeK, TypeV>
  {
    private final ConcurrentAutoTable _size;
    private final ConcurrentAutoTable _slots;
    volatile Object[] _newkvs;
    
    public int size()
    {
      return (int)_size.get();
    }
    











    public int slots()
    {
      return (int)_slots.get();
    }
    








    private final AtomicReferenceFieldUpdater<CHM, Object[]> _newkvsUpdater = AtomicReferenceFieldUpdater.newUpdater(CHM.class, [Ljava.lang.Object.class, "_newkvs");
    volatile long _resizers;
    
    boolean CAS_newkvs(Object[] newkvs)
    {
      while (_newkvs == null)
        if (_newkvsUpdater.compareAndSet(this, null, newkvs))
          return true;
      return false;
    }
    













    private static final AtomicLongFieldUpdater<CHM> _resizerUpdater = AtomicLongFieldUpdater.newUpdater(CHM.class, "_resizers");
    


    CHM(ConcurrentAutoTable size)
    {
      _size = size;
      _slots = new ConcurrentAutoTable();
    }
    








    private final boolean tableFull(int reprobe_cnt, int len)
    {
      return (reprobe_cnt >= 10) && ((reprobe_cnt >= NonBlockingHashMap.reprobe_limit(len)) || (_slots.estimate_get() >= len >> 1));
    }
    










    private final Object[] resize(NonBlockingHashMap topmap, Object[] kvs)
    {
      assert (NonBlockingHashMap.chm(kvs) == this);
      

      Object[] newkvs = _newkvs;
      if (newkvs != null) {
        return newkvs;
      }
      
      int oldlen = NonBlockingHashMap.len(kvs);
      int sz = size();
      int newsz = sz;
      


      if (sz >= oldlen >> 2) {
        newsz = oldlen << 1;
        

        if (4L * sz >= (oldlen >> 20 != 0 ? 3L : 2L) * oldlen) {
          newsz = oldlen << 2;
        }
      }
      





      long tm = System.currentTimeMillis();
      long q = 0L;
      if ((newsz <= oldlen) && ((tm <= _last_resize_milli + 10000L) || ((q = _slots.estimate_get()) >= sz << 1)))
      {

        newsz = oldlen << 1;
      }
      
      if (newsz < oldlen) { newsz = oldlen;
      }
      

      for (int log2 = 3; 1 << log2 < newsz; log2++) {}
      long len = (1L << log2 << 1) + 2L;
      

      if ((int)len != len) {
        log2 = 30;
        len = (1L << log2) + 2L;
        if (sz > (len >> 2) + (len >> 1)) { throw new RuntimeException("Table is full.");
        }
      }
      


      long r = _resizers;
      while (!_resizerUpdater.compareAndSet(this, r, r + 1L)) {
        r = _resizers;
      }
      

      long megs = (1L << log2 << 1) + 8L << 3 >> 20;
      if ((r >= 2L) && (megs > 0L)) {
        newkvs = _newkvs;
        if (newkvs != null) {
          return newkvs;
        }
        


        try
        {
          Thread.sleep(megs);
        }
        catch (Exception localException) {}
      }
      

      newkvs = _newkvs;
      if (newkvs != null) {
        return newkvs;
      }
      
      newkvs = new Object[(int)len];
      newkvs[0] = new CHM(_size);
      newkvs[1] = new int[1 << log2];
      

      if (_newkvs != null) {
        return _newkvs;
      }
      

      if (CAS_newkvs(newkvs))
      {



        topmap.rehash();
      } else
        newkvs = _newkvs;
      return newkvs;
    }
    







    volatile long _copyIdx = 0L;
    private static final AtomicLongFieldUpdater<CHM> _copyIdxUpdater = AtomicLongFieldUpdater.newUpdater(CHM.class, "_copyIdx");
    




    volatile long _copyDone = 0L;
    private static final AtomicLongFieldUpdater<CHM> _copyDoneUpdater = AtomicLongFieldUpdater.newUpdater(CHM.class, "_copyDone");
    




    private final void help_copy_impl(NonBlockingHashMap topmap, Object[] oldkvs, boolean copy_all)
    {
      assert (NonBlockingHashMap.chm(oldkvs) == this);
      Object[] newkvs = _newkvs;
      assert (newkvs != null);
      int oldlen = NonBlockingHashMap.len(oldkvs);
      int MIN_COPY_WORK = Math.min(oldlen, 1024);
      

      int panic_start = -1;
      int copyidx = 55537;
      while (_copyDone < oldlen)
      {









        if (panic_start == -1) {
          copyidx = (int)_copyIdx;
          while (!_copyIdxUpdater.compareAndSet(this, copyidx, copyidx + MIN_COPY_WORK))
            copyidx = (int)_copyIdx;
          if (copyidx >= oldlen << 1) {
            panic_start = copyidx;
          }
        }
        
        int workdone = 0;
        for (int i = 0; i < MIN_COPY_WORK; i++)
          if (copy_slot(topmap, copyidx + i & oldlen - 1, oldkvs, newkvs))
            workdone++;
        if (workdone > 0) {
          copy_check_and_promote(topmap, oldkvs, workdone);
        }
        


        copyidx += MIN_COPY_WORK;
        

        if ((!copy_all) && (panic_start == -1)) {
          return;
        }
      }
      
      copy_check_and_promote(topmap, oldkvs, 0);
    }
    











    private final Object[] copy_slot_and_check(NonBlockingHashMap topmap, Object[] oldkvs, int idx, Object should_help)
    {
      assert (NonBlockingHashMap.chm(oldkvs) == this);
      Object[] newkvs = _newkvs;
      

      assert (newkvs != null);
      if (copy_slot(topmap, idx, oldkvs, _newkvs)) {
        copy_check_and_promote(topmap, oldkvs, 1);
      }
      return should_help == null ? newkvs : topmap.help_copy(newkvs);
    }
    
    private final void copy_check_and_promote(NonBlockingHashMap topmap, Object[] oldkvs, int workdone)
    {
      assert (NonBlockingHashMap.chm(oldkvs) == this);
      int oldlen = NonBlockingHashMap.len(oldkvs);
      
      long copyDone = _copyDone;
      assert (copyDone + workdone <= oldlen);
      for (; (workdone > 0) && 
            (!_copyDoneUpdater.compareAndSet(this, copyDone, copyDone + workdone)); 
          
          throw new AssertionError())
      {
        label64:
        copyDone = _copyDone;
        if (($assertionsDisabled) || (copyDone + workdone <= oldlen)) {
          break label64;
        }
      }
      




      if ((copyDone + workdone == oldlen) && (_kvs == oldkvs) && (topmap.CAS_kvs(oldkvs, _newkvs)))
      {


        _last_resize_milli = System.currentTimeMillis();
      }
    }
    









    private boolean copy_slot(NonBlockingHashMap topmap, int idx, Object[] oldkvs, Object[] newkvs)
    {
      Object key;
      








      while ((key = NonBlockingHashMap.key(oldkvs, idx)) == null) {
        NonBlockingHashMap.CAS_key(oldkvs, idx, null, NonBlockingHashMap.TOMBSTONE);
      }
      


      Object oldval = NonBlockingHashMap.val(oldkvs, idx);
      while (!(oldval instanceof NonBlockingHashMap.Prime)) {
        NonBlockingHashMap.Prime box = (oldval == null) || (oldval == NonBlockingHashMap.TOMBSTONE) ? NonBlockingHashMap.TOMBPRIME : new NonBlockingHashMap.Prime(oldval);
        if (NonBlockingHashMap.CAS_val(oldkvs, idx, oldval, box))
        {





          if (box == NonBlockingHashMap.TOMBPRIME) {
            return true;
          }
          
          oldval = box;
          break;
        }
        oldval = NonBlockingHashMap.val(oldkvs, idx);
      }
      if (oldval == NonBlockingHashMap.TOMBPRIME) { return false;
      }
      






      Object old_unboxed = _V;
      assert (old_unboxed != NonBlockingHashMap.TOMBSTONE);
      boolean copied_into_new = NonBlockingHashMap.putIfMatch(topmap, newkvs, key, old_unboxed, null) == null;
      





      while ((oldval != NonBlockingHashMap.TOMBPRIME) && (!NonBlockingHashMap.CAS_val(oldkvs, idx, oldval, NonBlockingHashMap.TOMBPRIME))) {
        oldval = NonBlockingHashMap.val(oldkvs, idx);
      }
      return copied_into_new;
    }
  }
  
  private class SnapshotV implements Iterator<TypeV>, Enumeration<TypeV> {
    final Object[] _sskvs;
    private int _idx;
    private Object _nextK;
    private Object _prevK;
    private TypeV _nextV;
    private TypeV _prevV;
    
    public SnapshotV() { Object[] topkvs;
      for (;;) { topkvs = _kvs;
        NonBlockingHashMap.CHM topchm = NonBlockingHashMap.chm(topkvs);
        if (_newkvs == null)
        {


          _sskvs = topkvs;
          break;
        }
        

        NonBlockingHashMap.CHM.access$500(topchm, NonBlockingHashMap.this, topkvs, true);
      }
      
      next();
    }
    
    int length() {
      return NonBlockingHashMap.len(_sskvs);
    }
    
    Object key(int idx) {
      return NonBlockingHashMap.key(_sskvs, idx);
    }
    



    public boolean hasNext()
    {
      return _nextV != null;
    }
    




    public TypeV next()
    {
      if ((_idx != 0) && (_nextV == null)) throw new NoSuchElementException();
      _prevK = _nextK;
      _prevV = _nextV;
      _nextV = null;
      

      while (_idx < length()) {
        _nextK = key(_idx++);
        if ((_nextK != null) && (_nextK != NonBlockingHashMap.TOMBSTONE) && ((this._nextV = get(_nextK)) != null)) {
          break;
        }
      }
      
      return _prevV;
    }
    
    public void remove() {
      if (_prevV == null) throw new IllegalStateException();
      NonBlockingHashMap.putIfMatch(NonBlockingHashMap.this, _sskvs, _prevK, NonBlockingHashMap.TOMBSTONE, _prevV);
      _prevV = null;
    }
    
    public TypeV nextElement() {
      return next();
    }
    
    public boolean hasMoreElements() {
      return hasNext();
    }
  }
  
  public Object[] raw_array() {
    return SnapshotV_sskvs;
  }
  





  public Enumeration<TypeV> elements()
  {
    return new SnapshotV();
  }
  

















  public Collection<TypeV> values()
  {
    new AbstractCollection()
    {
      public void clear() {
        NonBlockingHashMap.this.clear();
      }
      
      public int size()
      {
        return NonBlockingHashMap.this.size();
      }
      
      public boolean contains(Object v)
      {
        return containsValue(v);
      }
      
      public Iterator<TypeV> iterator()
      {
        return new NonBlockingHashMap.SnapshotV(NonBlockingHashMap.this);
      }
    };
  }
  
  private class SnapshotK implements Iterator<TypeK>, Enumeration<TypeK>
  {
    final NonBlockingHashMap<TypeK, TypeV>.SnapshotV _ss;
    
    public SnapshotK() {
      _ss = new NonBlockingHashMap.SnapshotV(NonBlockingHashMap.this);
    }
    
    public void remove() {
      _ss.remove();
    }
    
    public TypeK next() {
      _ss.next();
      return NonBlockingHashMap.SnapshotV.access$1900(_ss);
    }
    
    public boolean hasNext() {
      return _ss.hasNext();
    }
    
    public TypeK nextElement() {
      return next();
    }
    
    public boolean hasMoreElements() {
      return hasNext();
    }
  }
  





  public Enumeration<TypeK> keys()
  {
    return new SnapshotK();
  }
  















  public Set<TypeK> keySet()
  {
    new AbstractSet()
    {
      public void clear() {
        NonBlockingHashMap.this.clear();
      }
      
      public int size()
      {
        return NonBlockingHashMap.this.size();
      }
      
      public boolean contains(Object k)
      {
        return containsKey(k);
      }
      
      public boolean remove(Object k)
      {
        return remove(k) != null;
      }
      
      public Iterator<TypeK> iterator()
      {
        return new NonBlockingHashMap.SnapshotK(NonBlockingHashMap.this);
      }
    };
  }
  
  private class NBHMEntry
    extends AbstractEntry<TypeK, TypeV>
  {
    NBHMEntry(TypeV k)
    {
      super(v);
    }
    
    public TypeV setValue(TypeV val) {
      if (val == null) throw new NullPointerException();
      _val = val;
      return put(_key, val);
    }
  }
  
  private class SnapshotE implements Iterator<Map.Entry<TypeK, TypeV>> {
    final NonBlockingHashMap<TypeK, TypeV>.SnapshotV _ss;
    
    public SnapshotE() {
      _ss = new NonBlockingHashMap.SnapshotV(NonBlockingHashMap.this);
    }
    
    public void remove() {
      _ss.remove();
    }
    
    public Map.Entry<TypeK, TypeV> next() {
      _ss.next();
      return new NonBlockingHashMap.NBHMEntry(NonBlockingHashMap.this, NonBlockingHashMap.SnapshotV.access$1900(_ss), NonBlockingHashMap.SnapshotV.access$2000(_ss));
    }
    
    public boolean hasNext() {
      return _ss.hasNext();
    }
  }
  






















  public Set<Map.Entry<TypeK, TypeV>> entrySet()
  {
    new AbstractSet()
    {
      public void clear() {
        NonBlockingHashMap.this.clear();
      }
      
      public int size()
      {
        return NonBlockingHashMap.this.size();
      }
      
      public boolean remove(Object o)
      {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<?, ?> e = (Map.Entry)o;
        return remove(e.getKey(), e.getValue());
      }
      
      public boolean contains(Object o)
      {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<?, ?> e = (Map.Entry)o;
        TypeV v = get(e.getKey());
        return v.equals(e.getValue());
      }
      
      public Iterator<Map.Entry<TypeK, TypeV>> iterator()
      {
        return new NonBlockingHashMap.SnapshotE(NonBlockingHashMap.this);
      }
    };
  }
  
  private void writeObject(ObjectOutputStream s)
    throws IOException
  {
    s.defaultWriteObject();
    for (Object K : keySet()) {
      Object V = get(K);
      s.writeObject(K);
      s.writeObject(V);
    }
    s.writeObject(null);
    s.writeObject(null);
  }
  
  private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
  {
    s.defaultReadObject();
    initialize(8);
    for (;;) {
      TypeK K = s.readObject();
      TypeV V = s.readObject();
      if (K == null) break;
      put(K, V);
    }
  }
  
  protected void rehash() {}
}
