/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*  4:   */ import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
/*  5:   */ 
/*  6:   */ abstract class MpscChunkedAtomicArrayQueueColdProducerFields<E>
/*  7:   */   extends BaseMpscLinkedAtomicArrayQueue<E>
/*  8:   */ {
/*  9:   */   protected final long maxQueueCapacity;
/* 10:   */   
/* 11:   */   public MpscChunkedAtomicArrayQueueColdProducerFields(int initialCapacity, int maxCapacity)
/* 12:   */   {
/* 13:42 */     super(initialCapacity);
/* 14:43 */     RangeUtil.checkGreaterThanOrEqual(maxCapacity, 4, "maxCapacity");
/* 15:44 */     RangeUtil.checkLessThan(Pow2.roundToPowerOfTwo(initialCapacity), Pow2.roundToPowerOfTwo(maxCapacity), "initialCapacity");
/* 16:45 */     this.maxQueueCapacity = (Pow2.roundToPowerOfTwo(maxCapacity) << 1);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscChunkedAtomicArrayQueueColdProducerFields
 * JD-Core Version:    0.7.0.1
 */