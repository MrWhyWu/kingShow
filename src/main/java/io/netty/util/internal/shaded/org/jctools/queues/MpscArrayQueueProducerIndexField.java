/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*  4:   */ import sun.misc.Unsafe;
/*  5:   */ 
/*  6:   */ abstract class MpscArrayQueueProducerIndexField<E>
/*  7:   */   extends MpscArrayQueueL1Pad<E>
/*  8:   */ {
/*  9:   */   private static final long P_INDEX_OFFSET;
/* 10:   */   private volatile long producerIndex;
/* 11:   */   
/* 12:   */   static
/* 13:   */   {
/* 14:   */     try
/* 15:   */     {
/* 16:42 */       P_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(MpscArrayQueueProducerIndexField.class.getDeclaredField("producerIndex"));
/* 17:   */     }
/* 18:   */     catch (NoSuchFieldException e)
/* 19:   */     {
/* 20:46 */       throw new RuntimeException(e);
/* 21:   */     }
/* 22:   */   }
/* 23:   */   
/* 24:   */   public MpscArrayQueueProducerIndexField(int capacity)
/* 25:   */   {
/* 26:54 */     super(capacity);
/* 27:   */   }
/* 28:   */   
/* 29:   */   public final long lvProducerIndex()
/* 30:   */   {
/* 31:60 */     return this.producerIndex;
/* 32:   */   }
/* 33:   */   
/* 34:   */   protected final boolean casProducerIndex(long expect, long newValue)
/* 35:   */   {
/* 36:65 */     return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField
 * JD-Core Version:    0.7.0.1
 */