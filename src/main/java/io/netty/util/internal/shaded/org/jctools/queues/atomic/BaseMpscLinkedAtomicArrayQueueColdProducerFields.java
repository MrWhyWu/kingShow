/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*   4:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*   5:    */ 
/*   6:    */ abstract class BaseMpscLinkedAtomicArrayQueueColdProducerFields<E>
/*   7:    */   extends BaseMpscLinkedAtomicArrayQueuePad3<E>
/*   8:    */ {
/*   9:123 */   private static final AtomicLongFieldUpdater<BaseMpscLinkedAtomicArrayQueueColdProducerFields> P_LIMIT_UPDATER = AtomicLongFieldUpdater.newUpdater(BaseMpscLinkedAtomicArrayQueueColdProducerFields.class, "producerLimit");
/*  10:    */   protected volatile long producerLimit;
/*  11:    */   protected long producerMask;
/*  12:    */   protected AtomicReferenceArray<E> producerBuffer;
/*  13:    */   
/*  14:    */   final long lvProducerLimit()
/*  15:    */   {
/*  16:132 */     return this.producerLimit;
/*  17:    */   }
/*  18:    */   
/*  19:    */   final boolean casProducerLimit(long expect, long newValue)
/*  20:    */   {
/*  21:136 */     return P_LIMIT_UPDATER.compareAndSet(this, expect, newValue);
/*  22:    */   }
/*  23:    */   
/*  24:    */   final void soProducerLimit(long newValue)
/*  25:    */   {
/*  26:140 */     P_LIMIT_UPDATER.lazySet(this, newValue);
/*  27:    */   }
/*  28:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueueColdProducerFields
 * JD-Core Version:    0.7.0.1
 */