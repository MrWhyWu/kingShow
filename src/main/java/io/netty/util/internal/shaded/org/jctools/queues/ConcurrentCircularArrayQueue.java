/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*   4:    */ import java.util.Iterator;
/*   5:    */ 
/*   6:    */ public abstract class ConcurrentCircularArrayQueue<E>
/*   7:    */   extends ConcurrentCircularArrayQueueL0Pad<E>
/*   8:    */ {
/*   9:    */   protected final long mask;
/*  10:    */   protected final E[] buffer;
/*  11:    */   
/*  12:    */   public ConcurrentCircularArrayQueue(int capacity)
/*  13:    */   {
/*  14: 43 */     int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
/*  15: 44 */     this.mask = (actualCapacity - 1);
/*  16: 45 */     this.buffer = CircularArrayOffsetCalculator.allocate(actualCapacity);
/*  17:    */   }
/*  18:    */   
/*  19:    */   protected static long calcElementOffset(long index, long mask)
/*  20:    */   {
/*  21: 55 */     return CircularArrayOffsetCalculator.calcElementOffset(index, mask);
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected final long calcElementOffset(long index)
/*  25:    */   {
/*  26: 64 */     return calcElementOffset(index, this.mask);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public Iterator<E> iterator()
/*  30:    */   {
/*  31: 70 */     throw new UnsupportedOperationException();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public final int size()
/*  35:    */   {
/*  36: 76 */     return IndexedQueueSizeUtil.size(this);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public final boolean isEmpty()
/*  40:    */   {
/*  41: 82 */     return IndexedQueueSizeUtil.isEmpty(this);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public String toString()
/*  45:    */   {
/*  46: 88 */     return getClass().getName();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void clear()
/*  50:    */   {
/*  51: 94 */     while (poll() != null) {}
/*  52:    */   }
/*  53:    */   
/*  54:    */   public int capacity()
/*  55:    */   {
/*  56:103 */     return (int)(this.mask + 1L);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public final long currentProducerIndex()
/*  60:    */   {
/*  61:109 */     return lvProducerIndex();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public final long currentConsumerIndex()
/*  65:    */   {
/*  66:115 */     return lvConsumerIndex();
/*  67:    */   }
/*  68:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.ConcurrentCircularArrayQueue
 * JD-Core Version:    0.7.0.1
 */