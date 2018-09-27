/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*   4:    */ import java.lang.reflect.Field;
/*   5:    */ import sun.misc.Unsafe;
/*   6:    */ 
/*   7:    */ abstract class BaseMpscLinkedArrayQueueConsumerFields<E>
/*   8:    */   extends BaseMpscLinkedArrayQueuePad2<E>
/*   9:    */ {
/*  10:    */   private static final long C_INDEX_OFFSET;
/*  11:    */   protected long consumerMask;
/*  12:    */   protected E[] consumerBuffer;
/*  13:    */   protected long consumerIndex;
/*  14:    */   
/*  15:    */   static
/*  16:    */   {
/*  17:    */     try
/*  18:    */     {
/*  19: 90 */       Field iField = BaseMpscLinkedArrayQueueConsumerFields.class.getDeclaredField("consumerIndex");
/*  20: 91 */       C_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
/*  21:    */     }
/*  22:    */     catch (NoSuchFieldException e)
/*  23:    */     {
/*  24: 95 */       throw new RuntimeException(e);
/*  25:    */     }
/*  26:    */   }
/*  27:    */   
/*  28:    */   public final long lvConsumerIndex()
/*  29:    */   {
/*  30:106 */     return UnsafeAccess.UNSAFE.getLongVolatile(this, C_INDEX_OFFSET);
/*  31:    */   }
/*  32:    */   
/*  33:    */   final void soConsumerIndex(long newValue)
/*  34:    */   {
/*  35:111 */     UnsafeAccess.UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, newValue);
/*  36:    */   }
/*  37:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields
 * JD-Core Version:    0.7.0.1
 */