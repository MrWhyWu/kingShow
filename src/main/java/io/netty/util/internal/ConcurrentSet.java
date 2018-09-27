/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.io.Serializable;
/*  4:   */ import java.util.AbstractSet;
/*  5:   */ import java.util.Iterator;
/*  6:   */ import java.util.Set;
/*  7:   */ import java.util.concurrent.ConcurrentMap;
/*  8:   */ 
/*  9:   */ public final class ConcurrentSet<E>
/* 10:   */   extends AbstractSet<E>
/* 11:   */   implements Serializable
/* 12:   */ {
/* 13:   */   private static final long serialVersionUID = -6761513279741915432L;
/* 14:   */   private final ConcurrentMap<E, Boolean> map;
/* 15:   */   
/* 16:   */   public ConcurrentSet()
/* 17:   */   {
/* 18:33 */     this.map = PlatformDependent.newConcurrentHashMap();
/* 19:   */   }
/* 20:   */   
/* 21:   */   public int size()
/* 22:   */   {
/* 23:38 */     return this.map.size();
/* 24:   */   }
/* 25:   */   
/* 26:   */   public boolean contains(Object o)
/* 27:   */   {
/* 28:43 */     return this.map.containsKey(o);
/* 29:   */   }
/* 30:   */   
/* 31:   */   public boolean add(E o)
/* 32:   */   {
/* 33:48 */     return this.map.putIfAbsent(o, Boolean.TRUE) == null;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public boolean remove(Object o)
/* 37:   */   {
/* 38:53 */     return this.map.remove(o) != null;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void clear()
/* 42:   */   {
/* 43:58 */     this.map.clear();
/* 44:   */   }
/* 45:   */   
/* 46:   */   public Iterator<E> iterator()
/* 47:   */   {
/* 48:63 */     return this.map.keySet().iterator();
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.ConcurrentSet
 * JD-Core Version:    0.7.0.1
 */