/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  4:   */ 
/*  5:   */ abstract class MpscAtomicArrayQueueProducerLimitField<E>
/*  6:   */   extends MpscAtomicArrayQueueMidPad<E>
/*  7:   */ {
/*  8:81 */   private static final AtomicLongFieldUpdater<MpscAtomicArrayQueueProducerLimitField> P_LIMIT_UPDATER = AtomicLongFieldUpdater.newUpdater(MpscAtomicArrayQueueProducerLimitField.class, "producerLimit");
/*  9:   */   private volatile long producerLimit;
/* 10:   */   
/* 11:   */   public MpscAtomicArrayQueueProducerLimitField(int capacity)
/* 12:   */   {
/* 13:87 */     super(capacity);
/* 14:88 */     this.producerLimit = capacity;
/* 15:   */   }
/* 16:   */   
/* 17:   */   protected final long lvProducerLimit()
/* 18:   */   {
/* 19:92 */     return this.producerLimit;
/* 20:   */   }
/* 21:   */   
/* 22:   */   protected final void soProducerLimit(long newValue)
/* 23:   */   {
/* 24:96 */     P_LIMIT_UPDATER.lazySet(this, newValue);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueueProducerLimitField
 * JD-Core Version:    0.7.0.1
 */