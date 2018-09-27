/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*  4:   */ import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
/*  5:   */ 
/*  6:   */ abstract class MpscChunkedArrayQueueColdProducerFields<E>
/*  7:   */   extends BaseMpscLinkedArrayQueue<E>
/*  8:   */ {
/*  9:   */   protected final long maxQueueCapacity;
/* 10:   */   
/* 11:   */   public MpscChunkedArrayQueueColdProducerFields(int initialCapacity, int maxCapacity)
/* 12:   */   {
/* 13:30 */     super(initialCapacity);
/* 14:31 */     RangeUtil.checkGreaterThanOrEqual(maxCapacity, 4, "maxCapacity");
/* 15:32 */     RangeUtil.checkLessThan(Pow2.roundToPowerOfTwo(initialCapacity), Pow2.roundToPowerOfTwo(maxCapacity), "initialCapacity");
/* 16:   */     
/* 17:34 */     this.maxQueueCapacity = (Pow2.roundToPowerOfTwo(maxCapacity) << 1);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscChunkedArrayQueueColdProducerFields
 * JD-Core Version:    0.7.0.1
 */