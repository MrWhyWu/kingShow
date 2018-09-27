/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*  4:   */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*  5:   */ 
/*  6:   */ public class MpscChunkedAtomicArrayQueue<E>
/*  7:   */   extends MpscChunkedAtomicArrayQueueColdProducerFields<E>
/*  8:   */ {
/*  9:   */   long p0;
/* 10:   */   long p1;
/* 11:   */   long p2;
/* 12:   */   long p3;
/* 13:   */   long p4;
/* 14:   */   long p5;
/* 15:   */   long p6;
/* 16:   */   long p7;
/* 17:   */   long p10;
/* 18:   */   long p11;
/* 19:   */   long p12;
/* 20:   */   long p13;
/* 21:   */   long p14;
/* 22:   */   long p15;
/* 23:   */   long p16;
/* 24:   */   long p17;
/* 25:   */   
/* 26:   */   public MpscChunkedAtomicArrayQueue(int maxCapacity)
/* 27:   */   {
/* 28:66 */     super(Math.max(2, Math.min(1024, Pow2.roundToPowerOfTwo(maxCapacity / 8))), maxCapacity);
/* 29:   */   }
/* 30:   */   
/* 31:   */   public MpscChunkedAtomicArrayQueue(int initialCapacity, int maxCapacity)
/* 32:   */   {
/* 33:77 */     super(initialCapacity, maxCapacity);
/* 34:   */   }
/* 35:   */   
/* 36:   */   protected long availableInQueue(long pIndex, long cIndex)
/* 37:   */   {
/* 38:82 */     return this.maxQueueCapacity - (pIndex - cIndex);
/* 39:   */   }
/* 40:   */   
/* 41:   */   public int capacity()
/* 42:   */   {
/* 43:87 */     return (int)(this.maxQueueCapacity / 2L);
/* 44:   */   }
/* 45:   */   
/* 46:   */   protected int getNextBufferSize(AtomicReferenceArray<E> buffer)
/* 47:   */   {
/* 48:92 */     return LinkedAtomicArrayQueueUtil.length(buffer);
/* 49:   */   }
/* 50:   */   
/* 51:   */   protected long getCurrentBufferCapacity(long mask)
/* 52:   */   {
/* 53:97 */     return mask;
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscChunkedAtomicArrayQueue
 * JD-Core Version:    0.7.0.1
 */