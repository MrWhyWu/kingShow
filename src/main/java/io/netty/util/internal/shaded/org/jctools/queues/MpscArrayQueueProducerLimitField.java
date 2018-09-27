/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*   4:    */ import sun.misc.Unsafe;
/*   5:    */ 
/*   6:    */ abstract class MpscArrayQueueProducerLimitField<E>
/*   7:    */   extends MpscArrayQueueMidPad<E>
/*   8:    */ {
/*   9:    */   private static final long P_LIMIT_OFFSET;
/*  10:    */   private volatile long producerLimit;
/*  11:    */   
/*  12:    */   static
/*  13:    */   {
/*  14:    */     try
/*  15:    */     {
/*  16: 90 */       P_LIMIT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(MpscArrayQueueProducerLimitField.class.getDeclaredField("producerLimit"));
/*  17:    */     }
/*  18:    */     catch (NoSuchFieldException e)
/*  19:    */     {
/*  20: 94 */       throw new RuntimeException(e);
/*  21:    */     }
/*  22:    */   }
/*  23:    */   
/*  24:    */   public MpscArrayQueueProducerLimitField(int capacity)
/*  25:    */   {
/*  26:103 */     super(capacity);
/*  27:104 */     this.producerLimit = capacity;
/*  28:    */   }
/*  29:    */   
/*  30:    */   protected final long lvProducerLimit()
/*  31:    */   {
/*  32:109 */     return this.producerLimit;
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected final void soProducerLimit(long newValue)
/*  36:    */   {
/*  37:114 */     UnsafeAccess.UNSAFE.putOrderedLong(this, P_LIMIT_OFFSET, newValue);
/*  38:    */   }
/*  39:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerLimitField
 * JD-Core Version:    0.7.0.1
 */