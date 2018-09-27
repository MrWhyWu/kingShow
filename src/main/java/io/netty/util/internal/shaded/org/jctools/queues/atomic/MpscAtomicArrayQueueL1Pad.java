/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ abstract class MpscAtomicArrayQueueL1Pad<E>
/*  4:   */   extends AtomicReferenceArrayQueue<E>
/*  5:   */ {
/*  6:   */   long p00;
/*  7:   */   long p01;
/*  8:   */   long p02;
/*  9:   */   long p03;
/* 10:   */   long p04;
/* 11:   */   long p05;
/* 12:   */   long p06;
/* 13:   */   long p07;
/* 14:   */   long p10;
/* 15:   */   long p11;
/* 16:   */   long p12;
/* 17:   */   long p13;
/* 18:   */   long p14;
/* 19:   */   long p15;
/* 20:   */   long p16;
/* 21:   */   
/* 22:   */   public MpscAtomicArrayQueueL1Pad(int capacity)
/* 23:   */   {
/* 24:32 */     super(capacity);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueueL1Pad
 * JD-Core Version:    0.7.0.1
 */