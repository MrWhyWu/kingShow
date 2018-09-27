/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.util.Collection;
/*   4:    */ import java.util.Collections;
/*   5:    */ import java.util.Iterator;
/*   6:    */ import java.util.List;
/*   7:    */ import java.util.NoSuchElementException;
/*   8:    */ 
/*   9:    */ public final class EmptyPriorityQueue<T>
/*  10:    */   implements PriorityQueue<T>
/*  11:    */ {
/*  12: 24 */   private static final PriorityQueue<Object> INSTANCE = new EmptyPriorityQueue();
/*  13:    */   
/*  14:    */   public static <V> EmptyPriorityQueue<V> instance()
/*  15:    */   {
/*  16: 34 */     return (EmptyPriorityQueue)INSTANCE;
/*  17:    */   }
/*  18:    */   
/*  19:    */   public boolean removeTyped(T node)
/*  20:    */   {
/*  21: 39 */     return false;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public boolean containsTyped(T node)
/*  25:    */   {
/*  26: 44 */     return false;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void priorityChanged(T node) {}
/*  30:    */   
/*  31:    */   public int size()
/*  32:    */   {
/*  33: 53 */     return 0;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public boolean isEmpty()
/*  37:    */   {
/*  38: 58 */     return true;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public boolean contains(Object o)
/*  42:    */   {
/*  43: 63 */     return false;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public Iterator<T> iterator()
/*  47:    */   {
/*  48: 68 */     return Collections.emptyList().iterator();
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Object[] toArray()
/*  52:    */   {
/*  53: 73 */     return EmptyArrays.EMPTY_OBJECTS;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public <T1> T1[] toArray(T1[] a)
/*  57:    */   {
/*  58: 78 */     if (a.length > 0) {
/*  59: 79 */       a[0] = null;
/*  60:    */     }
/*  61: 81 */     return a;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean add(T t)
/*  65:    */   {
/*  66: 86 */     return false;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public boolean remove(Object o)
/*  70:    */   {
/*  71: 91 */     return false;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public boolean containsAll(Collection<?> c)
/*  75:    */   {
/*  76: 96 */     return false;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean addAll(Collection<? extends T> c)
/*  80:    */   {
/*  81:101 */     return false;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public boolean removeAll(Collection<?> c)
/*  85:    */   {
/*  86:106 */     return false;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public boolean retainAll(Collection<?> c)
/*  90:    */   {
/*  91:111 */     return false;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void clear() {}
/*  95:    */   
/*  96:    */   public void clearIgnoringIndexes() {}
/*  97:    */   
/*  98:    */   public boolean equals(Object o)
/*  99:    */   {
/* 100:124 */     return ((o instanceof PriorityQueue)) && (((PriorityQueue)o).isEmpty());
/* 101:    */   }
/* 102:    */   
/* 103:    */   public int hashCode()
/* 104:    */   {
/* 105:129 */     return 0;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public boolean offer(T t)
/* 109:    */   {
/* 110:134 */     return false;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public T remove()
/* 114:    */   {
/* 115:139 */     throw new NoSuchElementException();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public T poll()
/* 119:    */   {
/* 120:144 */     return null;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public T element()
/* 124:    */   {
/* 125:149 */     throw new NoSuchElementException();
/* 126:    */   }
/* 127:    */   
/* 128:    */   public T peek()
/* 129:    */   {
/* 130:154 */     return null;
/* 131:    */   }
/* 132:    */   
/* 133:    */   public String toString()
/* 134:    */   {
/* 135:159 */     return EmptyPriorityQueue.class.getSimpleName();
/* 136:    */   }
/* 137:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.EmptyPriorityQueue
 * JD-Core Version:    0.7.0.1
 */