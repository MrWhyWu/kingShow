/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
/*  4:   */ import java.lang.reflect.Field;
/*  5:   */ import sun.misc.Unsafe;
/*  6:   */ 
/*  7:   */ abstract class BaseMpscLinkedArrayQueueProducerFields<E>
/*  8:   */   extends BaseMpscLinkedArrayQueuePad1<E>
/*  9:   */ {
/* 10:   */   private static final long P_INDEX_OFFSET;
/* 11:   */   protected long producerIndex;
/* 12:   */   
/* 13:   */   static
/* 14:   */   {
/* 15:   */     try
/* 16:   */     {
/* 17:47 */       Field iField = BaseMpscLinkedArrayQueueProducerFields.class.getDeclaredField("producerIndex");
/* 18:48 */       P_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
/* 19:   */     }
/* 20:   */     catch (NoSuchFieldException e)
/* 21:   */     {
/* 22:52 */       throw new RuntimeException(e);
/* 23:   */     }
/* 24:   */   }
/* 25:   */   
/* 26:   */   public final long lvProducerIndex()
/* 27:   */   {
/* 28:61 */     return UnsafeAccess.UNSAFE.getLongVolatile(this, P_INDEX_OFFSET);
/* 29:   */   }
/* 30:   */   
/* 31:   */   final void soProducerIndex(long newValue)
/* 32:   */   {
/* 33:66 */     UnsafeAccess.UNSAFE.putOrderedLong(this, P_INDEX_OFFSET, newValue);
/* 34:   */   }
/* 35:   */   
/* 36:   */   final boolean casProducerIndex(long expect, long newValue)
/* 37:   */   {
/* 38:71 */     return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueProducerFields
 * JD-Core Version:    0.7.0.1
 */