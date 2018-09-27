/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*  4:   */ 
/*  5:   */ public class MpscChunkedArrayQueue<E>
/*  6:   */   extends MpscChunkedArrayQueueColdProducerFields<E>
/*  7:   */ {
/*  8:   */   long p0;
/*  9:   */   long p1;
/* 10:   */   long p2;
/* 11:   */   long p3;
/* 12:   */   long p4;
/* 13:   */   long p5;
/* 14:   */   long p6;
/* 15:   */   long p7;
/* 16:   */   long p10;
/* 17:   */   long p11;
/* 18:   */   long p12;
/* 19:   */   long p13;
/* 20:   */   long p14;
/* 21:   */   long p15;
/* 22:   */   long p16;
/* 23:   */   long p17;
/* 24:   */   
/* 25:   */   public MpscChunkedArrayQueue(int maxCapacity)
/* 26:   */   {
/* 27:52 */     super(Math.max(2, Math.min(1024, Pow2.roundToPowerOfTwo(maxCapacity / 8))), maxCapacity);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public MpscChunkedArrayQueue(int initialCapacity, int maxCapacity)
/* 31:   */   {
/* 32:64 */     super(initialCapacity, maxCapacity);
/* 33:   */   }
/* 34:   */   
/* 35:   */   protected long availableInQueue(long pIndex, long cIndex)
/* 36:   */   {
/* 37:70 */     return this.maxQueueCapacity - (pIndex - cIndex);
/* 38:   */   }
/* 39:   */   
/* 40:   */   public int capacity()
/* 41:   */   {
/* 42:76 */     return (int)(this.maxQueueCapacity / 2L);
/* 43:   */   }
/* 44:   */   
/* 45:   */   protected int getNextBufferSize(E[] buffer)
/* 46:   */   {
/* 47:82 */     return LinkedArrayQueueUtil.length(buffer);
/* 48:   */   }
/* 49:   */   
/* 50:   */   protected long getCurrentBufferCapacity(long mask)
/* 51:   */   {
/* 52:88 */     return mask;
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscChunkedArrayQueue
 * JD-Core Version:    0.7.0.1
 */