/*  1:   */ package io.netty.channel.pool;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ import io.netty.util.internal.ReadOnlyIterator;
/*  6:   */ import java.io.Closeable;
/*  7:   */ import java.util.Iterator;
/*  8:   */ import java.util.Map.Entry;
/*  9:   */ import java.util.Set;
/* 10:   */ import java.util.concurrent.ConcurrentMap;
/* 11:   */ 
/* 12:   */ public abstract class AbstractChannelPoolMap<K, P extends ChannelPool>
/* 13:   */   implements ChannelPoolMap<K, P>, Iterable<Map.Entry<K, P>>, Closeable
/* 14:   */ {
/* 15:34 */   private final ConcurrentMap<K, P> map = PlatformDependent.newConcurrentHashMap();
/* 16:   */   
/* 17:   */   public final P get(K key)
/* 18:   */   {
/* 19:38 */     P pool = (ChannelPool)this.map.get(ObjectUtil.checkNotNull(key, "key"));
/* 20:39 */     if (pool == null)
/* 21:   */     {
/* 22:40 */       pool = newPool(key);
/* 23:41 */       P old = (ChannelPool)this.map.putIfAbsent(key, pool);
/* 24:42 */       if (old != null)
/* 25:   */       {
/* 26:44 */         pool.close();
/* 27:45 */         pool = old;
/* 28:   */       }
/* 29:   */     }
/* 30:48 */     return pool;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public final boolean remove(K key)
/* 34:   */   {
/* 35:57 */     P pool = (ChannelPool)this.map.remove(ObjectUtil.checkNotNull(key, "key"));
/* 36:58 */     if (pool != null)
/* 37:   */     {
/* 38:59 */       pool.close();
/* 39:60 */       return true;
/* 40:   */     }
/* 41:62 */     return false;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public final Iterator<Map.Entry<K, P>> iterator()
/* 45:   */   {
/* 46:67 */     return new ReadOnlyIterator(this.map.entrySet().iterator());
/* 47:   */   }
/* 48:   */   
/* 49:   */   public final int size()
/* 50:   */   {
/* 51:74 */     return this.map.size();
/* 52:   */   }
/* 53:   */   
/* 54:   */   public final boolean isEmpty()
/* 55:   */   {
/* 56:81 */     return this.map.isEmpty();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public final boolean contains(K key)
/* 60:   */   {
/* 61:86 */     return this.map.containsKey(ObjectUtil.checkNotNull(key, "key"));
/* 62:   */   }
/* 63:   */   
/* 64:   */   protected abstract P newPool(K paramK);
/* 65:   */   
/* 66:   */   public final void close()
/* 67:   */   {
/* 68:96 */     for (K key : this.map.keySet()) {
/* 69:97 */       remove(key);
/* 70:   */     }
/* 71:   */   }
/* 72:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.pool.AbstractChannelPoolMap
 * JD-Core Version:    0.7.0.1
 */