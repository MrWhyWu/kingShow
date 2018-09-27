/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*   4:    */ 
/*   5:    */ abstract class MpscAtomicArrayQueueConsumerIndexField<E>
/*   6:    */   extends MpscAtomicArrayQueueL2Pad<E>
/*   7:    */ {
/*   8:121 */   private static final AtomicLongFieldUpdater<MpscAtomicArrayQueueConsumerIndexField> C_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(MpscAtomicArrayQueueConsumerIndexField.class, "consumerIndex");
/*   9:    */   protected volatile long consumerIndex;
/*  10:    */   
/*  11:    */   public MpscAtomicArrayQueueConsumerIndexField(int capacity)
/*  12:    */   {
/*  13:126 */     super(capacity);
/*  14:    */   }
/*  15:    */   
/*  16:    */   protected final long lpConsumerIndex()
/*  17:    */   {
/*  18:130 */     return this.consumerIndex;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public final long lvConsumerIndex()
/*  22:    */   {
/*  23:135 */     return this.consumerIndex;
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected void soConsumerIndex(long newValue)
/*  27:    */   {
/*  28:139 */     C_INDEX_UPDATER.lazySet(this, newValue);
/*  29:    */   }
/*  30:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueueConsumerIndexField
 * JD-Core Version:    0.7.0.1
 */