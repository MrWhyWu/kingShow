/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  4:   */ 
/*  5:   */ abstract class BaseMpscLinkedAtomicArrayQueueProducerFields<E>
/*  6:   */   extends BaseMpscLinkedAtomicArrayQueuePad1<E>
/*  7:   */ {
/*  8:53 */   private static final AtomicLongFieldUpdater<BaseMpscLinkedAtomicArrayQueueProducerFields> P_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(BaseMpscLinkedAtomicArrayQueueProducerFields.class, "producerIndex");
/*  9:   */   protected volatile long producerIndex;
/* 10:   */   
/* 11:   */   public final long lvProducerIndex()
/* 12:   */   {
/* 13:59 */     return this.producerIndex;
/* 14:   */   }
/* 15:   */   
/* 16:   */   final void soProducerIndex(long newValue)
/* 17:   */   {
/* 18:63 */     P_INDEX_UPDATER.lazySet(this, newValue);
/* 19:   */   }
/* 20:   */   
/* 21:   */   final boolean casProducerIndex(long expect, long newValue)
/* 22:   */   {
/* 23:67 */     return P_INDEX_UPDATER.compareAndSet(this, expect, newValue);
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueueProducerFields
 * JD-Core Version:    0.7.0.1
 */