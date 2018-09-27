/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*   4:    */ import java.lang.reflect.Field;
/*   5:    */ import sun.misc.Unsafe;
/*   6:    */ 
/*   7:    */ abstract class BaseMpscLinkedArrayQueueColdProducerFields<E>
/*   8:    */   extends BaseMpscLinkedArrayQueuePad3<E>
/*   9:    */ {
/*  10:    */   private static final long P_LIMIT_OFFSET;
/*  11:    */   private volatile long producerLimit;
/*  12:    */   protected long producerMask;
/*  13:    */   protected E[] producerBuffer;
/*  14:    */   
/*  15:    */   static
/*  16:    */   {
/*  17:    */     try
/*  18:    */     {
/*  19:130 */       Field iField = BaseMpscLinkedArrayQueueColdProducerFields.class.getDeclaredField("producerLimit");
/*  20:131 */       P_LIMIT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
/*  21:    */     }
/*  22:    */     catch (NoSuchFieldException e)
/*  23:    */     {
/*  24:135 */       throw new RuntimeException(e);
/*  25:    */     }
/*  26:    */   }
/*  27:    */   
/*  28:    */   final long lvProducerLimit()
/*  29:    */   {
/*  30:145 */     return this.producerLimit;
/*  31:    */   }
/*  32:    */   
/*  33:    */   final boolean casProducerLimit(long expect, long newValue)
/*  34:    */   {
/*  35:150 */     return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_LIMIT_OFFSET, expect, newValue);
/*  36:    */   }
/*  37:    */   
/*  38:    */   final void soProducerLimit(long newValue)
/*  39:    */   {
/*  40:155 */     UnsafeAccess.UNSAFE.putOrderedLong(this, P_LIMIT_OFFSET, newValue);
/*  41:    */   }
/*  42:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields
 * JD-Core Version:    0.7.0.1
 */