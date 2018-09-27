/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*   4:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*   5:    */ 
/*   6:    */ abstract class BaseMpscLinkedAtomicArrayQueueConsumerFields<E>
/*   7:    */   extends BaseMpscLinkedAtomicArrayQueuePad2<E>
/*   8:    */ {
/*   9: 88 */   private static final AtomicLongFieldUpdater<BaseMpscLinkedAtomicArrayQueueConsumerFields> C_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(BaseMpscLinkedAtomicArrayQueueConsumerFields.class, "consumerIndex");
/*  10:    */   protected long consumerMask;
/*  11:    */   protected AtomicReferenceArray<E> consumerBuffer;
/*  12:    */   protected volatile long consumerIndex;
/*  13:    */   
/*  14:    */   public final long lvConsumerIndex()
/*  15:    */   {
/*  16: 98 */     return this.consumerIndex;
/*  17:    */   }
/*  18:    */   
/*  19:    */   final void soConsumerIndex(long newValue)
/*  20:    */   {
/*  21:102 */     C_INDEX_UPDATER.lazySet(this, newValue);
/*  22:    */   }
/*  23:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueueConsumerFields
 * JD-Core Version:    0.7.0.1
 */