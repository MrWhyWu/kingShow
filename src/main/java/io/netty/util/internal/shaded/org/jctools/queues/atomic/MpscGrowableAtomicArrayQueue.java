/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*  4:   */ import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
/*  5:   */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*  6:   */ 
/*  7:   */ public class MpscGrowableAtomicArrayQueue<E>
/*  8:   */   extends MpscChunkedAtomicArrayQueue<E>
/*  9:   */ {
/* 10:   */   public MpscGrowableAtomicArrayQueue(int maxCapacity)
/* 11:   */   {
/* 12:43 */     super(Math.max(2, Pow2.roundToPowerOfTwo(maxCapacity / 8)), maxCapacity);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public MpscGrowableAtomicArrayQueue(int initialCapacity, int maxCapacity)
/* 16:   */   {
/* 17:54 */     super(initialCapacity, maxCapacity);
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected int getNextBufferSize(AtomicReferenceArray<E> buffer)
/* 21:   */   {
/* 22:59 */     long maxSize = this.maxQueueCapacity / 2L;
/* 23:60 */     RangeUtil.checkLessThanOrEqual(LinkedAtomicArrayQueueUtil.length(buffer), maxSize, "buffer.length");
/* 24:61 */     int newSize = 2 * (LinkedAtomicArrayQueueUtil.length(buffer) - 1);
/* 25:62 */     return newSize + 1;
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected long getCurrentBufferCapacity(long mask)
/* 29:   */   {
/* 30:67 */     return mask + 2L == this.maxQueueCapacity ? this.maxQueueCapacity : mask;
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscGrowableAtomicArrayQueue
 * JD-Core Version:    0.7.0.1
 */