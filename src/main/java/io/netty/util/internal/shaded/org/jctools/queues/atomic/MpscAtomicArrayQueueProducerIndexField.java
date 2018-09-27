/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  4:   */ 
/*  5:   */ abstract class MpscAtomicArrayQueueProducerIndexField<E>
/*  6:   */   extends MpscAtomicArrayQueueL1Pad<E>
/*  7:   */ {
/*  8:42 */   private static final AtomicLongFieldUpdater<MpscAtomicArrayQueueProducerIndexField> P_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(MpscAtomicArrayQueueProducerIndexField.class, "producerIndex");
/*  9:   */   private volatile long producerIndex;
/* 10:   */   
/* 11:   */   public MpscAtomicArrayQueueProducerIndexField(int capacity)
/* 12:   */   {
/* 13:47 */     super(capacity);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public final long lvProducerIndex()
/* 17:   */   {
/* 18:52 */     return this.producerIndex;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected final boolean casProducerIndex(long expect, long newValue)
/* 22:   */   {
/* 23:56 */     return P_INDEX_UPDATER.compareAndSet(this, expect, newValue);
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueueProducerIndexField
 * JD-Core Version:    0.7.0.1
 */