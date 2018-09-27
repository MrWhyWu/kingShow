/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*  4:   */ 
/*  5:   */ final class LinkedAtomicArrayQueueUtil
/*  6:   */ {
/*  7:   */   public static <E> E lvElement(AtomicReferenceArray<E> buffer, int offset)
/*  8:   */   {
/*  9:13 */     return AtomicReferenceArrayQueue.lvElement(buffer, offset);
/* 10:   */   }
/* 11:   */   
/* 12:   */   public static <E> E lpElement(AtomicReferenceArray<E> buffer, int offset)
/* 13:   */   {
/* 14:18 */     return AtomicReferenceArrayQueue.lpElement(buffer, offset);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public static <E> void spElement(AtomicReferenceArray<E> buffer, int offset, E value)
/* 18:   */   {
/* 19:23 */     AtomicReferenceArrayQueue.spElement(buffer, offset, value);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public static <E> void svElement(AtomicReferenceArray<E> buffer, int offset, E value)
/* 23:   */   {
/* 24:28 */     AtomicReferenceArrayQueue.svElement(buffer, offset, value);
/* 25:   */   }
/* 26:   */   
/* 27:   */   static <E> void soElement(AtomicReferenceArray buffer, int offset, Object value)
/* 28:   */   {
/* 29:33 */     buffer.lazySet(offset, value);
/* 30:   */   }
/* 31:   */   
/* 32:   */   static int calcElementOffset(long index, long mask)
/* 33:   */   {
/* 34:38 */     return (int)(index & mask);
/* 35:   */   }
/* 36:   */   
/* 37:   */   static <E> AtomicReferenceArray<E> allocate(int capacity)
/* 38:   */   {
/* 39:43 */     return new AtomicReferenceArray(capacity);
/* 40:   */   }
/* 41:   */   
/* 42:   */   static int length(AtomicReferenceArray<?> buf)
/* 43:   */   {
/* 44:48 */     return buf.length();
/* 45:   */   }
/* 46:   */   
/* 47:   */   static int modifiedCalcElementOffset(long index, long mask)
/* 48:   */   {
/* 49:56 */     return (int)(index & mask) >> 1;
/* 50:   */   }
/* 51:   */   
/* 52:   */   static int nextArrayOffset(AtomicReferenceArray<?> curr)
/* 53:   */   {
/* 54:61 */     return length(curr) - 1;
/* 55:   */   }
/* 56:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedAtomicArrayQueueUtil
 * JD-Core Version:    0.7.0.1
 */