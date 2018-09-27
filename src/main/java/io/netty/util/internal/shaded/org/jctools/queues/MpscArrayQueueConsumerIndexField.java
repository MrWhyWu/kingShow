/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*   4:    */ import sun.misc.Unsafe;
/*   5:    */ 
/*   6:    */ abstract class MpscArrayQueueConsumerIndexField<E>
/*   7:    */   extends MpscArrayQueueL2Pad<E>
/*   8:    */ {
/*   9:    */   private static final long C_INDEX_OFFSET;
/*  10:    */   protected long consumerIndex;
/*  11:    */   
/*  12:    */   static
/*  13:    */   {
/*  14:    */     try
/*  15:    */     {
/*  16:139 */       C_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(MpscArrayQueueConsumerIndexField.class.getDeclaredField("consumerIndex"));
/*  17:    */     }
/*  18:    */     catch (NoSuchFieldException e)
/*  19:    */     {
/*  20:143 */       throw new RuntimeException(e);
/*  21:    */     }
/*  22:    */   }
/*  23:    */   
/*  24:    */   public MpscArrayQueueConsumerIndexField(int capacity)
/*  25:    */   {
/*  26:151 */     super(capacity);
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected final long lpConsumerIndex()
/*  30:    */   {
/*  31:156 */     return this.consumerIndex;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public final long lvConsumerIndex()
/*  35:    */   {
/*  36:162 */     return UnsafeAccess.UNSAFE.getLongVolatile(this, C_INDEX_OFFSET);
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected void soConsumerIndex(long newValue)
/*  40:    */   {
/*  41:167 */     UnsafeAccess.UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, newValue);
/*  42:    */   }
/*  43:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField
 * JD-Core Version:    0.7.0.1
 */