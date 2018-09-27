/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil.IndexedQueue;
/*   5:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
/*   6:    */ import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
/*   7:    */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*   8:    */ import java.util.AbstractQueue;
/*   9:    */ import java.util.Iterator;
/*  10:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*  11:    */ 
/*  12:    */ abstract class AtomicReferenceArrayQueue<E>
/*  13:    */   extends AbstractQueue<E>
/*  14:    */   implements IndexedQueueSizeUtil.IndexedQueue, QueueProgressIndicators, MessagePassingQueue<E>
/*  15:    */ {
/*  16:    */   protected final AtomicReferenceArray<E> buffer;
/*  17:    */   protected final int mask;
/*  18:    */   
/*  19:    */   public AtomicReferenceArrayQueue(int capacity)
/*  20:    */   {
/*  21: 34 */     int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
/*  22: 35 */     this.mask = (actualCapacity - 1);
/*  23: 36 */     this.buffer = new AtomicReferenceArray(actualCapacity);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public Iterator<E> iterator()
/*  27:    */   {
/*  28: 42 */     throw new UnsupportedOperationException();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public String toString()
/*  32:    */   {
/*  33: 48 */     return getClass().getName();
/*  34:    */   }
/*  35:    */   
/*  36:    */   public void clear()
/*  37:    */   {
/*  38: 54 */     while (poll() != null) {}
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected final int calcElementOffset(long index, int mask)
/*  42:    */   {
/*  43: 62 */     return (int)index & mask;
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected final int calcElementOffset(long index)
/*  47:    */   {
/*  48: 67 */     return (int)index & this.mask;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static <E> E lvElement(AtomicReferenceArray<E> buffer, int offset)
/*  52:    */   {
/*  53: 72 */     return buffer.get(offset);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static <E> E lpElement(AtomicReferenceArray<E> buffer, int offset)
/*  57:    */   {
/*  58: 77 */     return buffer.get(offset);
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected final E lpElement(int offset)
/*  62:    */   {
/*  63: 82 */     return this.buffer.get(offset);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static <E> void spElement(AtomicReferenceArray<E> buffer, int offset, E value)
/*  67:    */   {
/*  68: 87 */     buffer.lazySet(offset, value);
/*  69:    */   }
/*  70:    */   
/*  71:    */   protected final void spElement(int offset, E value)
/*  72:    */   {
/*  73: 92 */     this.buffer.lazySet(offset, value);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public static <E> void soElement(AtomicReferenceArray<E> buffer, int offset, E value)
/*  77:    */   {
/*  78: 97 */     buffer.lazySet(offset, value);
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected final void soElement(int offset, E value)
/*  82:    */   {
/*  83:102 */     this.buffer.lazySet(offset, value);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public static <E> void svElement(AtomicReferenceArray<E> buffer, int offset, E value)
/*  87:    */   {
/*  88:107 */     buffer.set(offset, value);
/*  89:    */   }
/*  90:    */   
/*  91:    */   protected final E lvElement(int offset)
/*  92:    */   {
/*  93:112 */     return lvElement(this.buffer, offset);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public final int capacity()
/*  97:    */   {
/*  98:118 */     return this.mask + 1;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public final int size()
/* 102:    */   {
/* 103:128 */     return IndexedQueueSizeUtil.size(this);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public final boolean isEmpty()
/* 107:    */   {
/* 108:134 */     return IndexedQueueSizeUtil.isEmpty(this);
/* 109:    */   }
/* 110:    */   
/* 111:    */   public final long currentProducerIndex()
/* 112:    */   {
/* 113:140 */     return lvProducerIndex();
/* 114:    */   }
/* 115:    */   
/* 116:    */   public final long currentConsumerIndex()
/* 117:    */   {
/* 118:146 */     return lvConsumerIndex();
/* 119:    */   }
/* 120:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicReferenceArrayQueue
 * JD-Core Version:    0.7.0.1
 */