/*   1:    */ package io.netty.handler.codec.serialization;
/*   2:    */ 
/*   3:    */ import java.lang.ref.Reference;
/*   4:    */ import java.util.Collection;
/*   5:    */ import java.util.Map;
/*   6:    */ import java.util.Map.Entry;
/*   7:    */ import java.util.Set;
/*   8:    */ 
/*   9:    */ abstract class ReferenceMap<K, V>
/*  10:    */   implements Map<K, V>
/*  11:    */ {
/*  12:    */   private final Map<K, Reference<V>> delegate;
/*  13:    */   
/*  14:    */   protected ReferenceMap(Map<K, Reference<V>> delegate)
/*  15:    */   {
/*  16: 28 */     this.delegate = delegate;
/*  17:    */   }
/*  18:    */   
/*  19:    */   abstract Reference<V> fold(V paramV);
/*  20:    */   
/*  21:    */   private V unfold(Reference<V> ref)
/*  22:    */   {
/*  23: 34 */     if (ref == null) {
/*  24: 35 */       return null;
/*  25:    */     }
/*  26: 38 */     return ref.get();
/*  27:    */   }
/*  28:    */   
/*  29:    */   public int size()
/*  30:    */   {
/*  31: 43 */     return this.delegate.size();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public boolean isEmpty()
/*  35:    */   {
/*  36: 48 */     return this.delegate.isEmpty();
/*  37:    */   }
/*  38:    */   
/*  39:    */   public boolean containsKey(Object key)
/*  40:    */   {
/*  41: 53 */     return this.delegate.containsKey(key);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public boolean containsValue(Object value)
/*  45:    */   {
/*  46: 58 */     throw new UnsupportedOperationException();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public V get(Object key)
/*  50:    */   {
/*  51: 63 */     return unfold((Reference)this.delegate.get(key));
/*  52:    */   }
/*  53:    */   
/*  54:    */   public V put(K key, V value)
/*  55:    */   {
/*  56: 68 */     return unfold((Reference)this.delegate.put(key, fold(value)));
/*  57:    */   }
/*  58:    */   
/*  59:    */   public V remove(Object key)
/*  60:    */   {
/*  61: 73 */     return unfold((Reference)this.delegate.remove(key));
/*  62:    */   }
/*  63:    */   
/*  64:    */   public void putAll(Map<? extends K, ? extends V> m)
/*  65:    */   {
/*  66: 78 */     for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
/*  67: 79 */       this.delegate.put(entry.getKey(), fold(entry.getValue()));
/*  68:    */     }
/*  69:    */   }
/*  70:    */   
/*  71:    */   public void clear()
/*  72:    */   {
/*  73: 85 */     this.delegate.clear();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public Set<K> keySet()
/*  77:    */   {
/*  78: 90 */     return this.delegate.keySet();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public Collection<V> values()
/*  82:    */   {
/*  83: 95 */     throw new UnsupportedOperationException();
/*  84:    */   }
/*  85:    */   
/*  86:    */   public Set<Map.Entry<K, V>> entrySet()
/*  87:    */   {
/*  88:100 */     throw new UnsupportedOperationException();
/*  89:    */   }
/*  90:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.serialization.ReferenceMap
 * JD-Core Version:    0.7.0.1
 */