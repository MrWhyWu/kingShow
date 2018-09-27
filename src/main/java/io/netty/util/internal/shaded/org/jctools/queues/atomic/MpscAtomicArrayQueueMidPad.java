/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ abstract class MpscAtomicArrayQueueMidPad<E>
/*  4:   */   extends MpscAtomicArrayQueueProducerIndexField<E>
/*  5:   */ {
/*  6:   */   long p01;
/*  7:   */   long p02;
/*  8:   */   long p03;
/*  9:   */   long p04;
/* 10:   */   long p05;
/* 11:   */   long p06;
/* 12:   */   long p07;
/* 13:   */   long p10;
/* 14:   */   long p11;
/* 15:   */   long p12;
/* 16:   */   long p13;
/* 17:   */   long p14;
/* 18:   */   long p15;
/* 19:   */   long p16;
/* 20:   */   long p17;
/* 21:   */   
/* 22:   */   public MpscAtomicArrayQueueMidPad(int capacity)
/* 23:   */   {
/* 24:71 */     super(capacity);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueueMidPad
 * JD-Core Version:    0.7.0.1
 */