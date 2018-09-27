/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ public final class IndexedQueueSizeUtil
/*  4:   */ {
/*  5:   */   public static int size(IndexedQueue iq)
/*  6:   */   {
/*  7:29 */     long after = iq.lvConsumerIndex();
/*  8:   */     for (;;)
/*  9:   */     {
/* 10:33 */       long before = after;
/* 11:34 */       long currentProducerIndex = iq.lvProducerIndex();
/* 12:35 */       after = iq.lvConsumerIndex();
/* 13:36 */       if (before == after)
/* 14:   */       {
/* 15:38 */         long size = currentProducerIndex - after;
/* 16:39 */         break;
/* 17:   */       }
/* 18:   */     }
/* 19:   */     long size;
/* 20:44 */     if (size > 2147483647L) {
/* 21:46 */       return 2147483647;
/* 22:   */     }
/* 23:50 */     return (int)size;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public static boolean isEmpty(IndexedQueue iq)
/* 27:   */   {
/* 28:60 */     return iq.lvConsumerIndex() == iq.lvProducerIndex();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public static abstract interface IndexedQueue
/* 32:   */   {
/* 33:   */     public abstract long lvConsumerIndex();
/* 34:   */     
/* 35:   */     public abstract long lvProducerIndex();
/* 36:   */   }
/* 37:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil
 * JD-Core Version:    0.7.0.1
 */