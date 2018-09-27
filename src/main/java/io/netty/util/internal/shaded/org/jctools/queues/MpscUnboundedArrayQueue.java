/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
/*  4:   */ 
/*  5:   */ public class MpscUnboundedArrayQueue<E>
/*  6:   */   extends BaseMpscLinkedArrayQueue<E>
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
/* 25:   */   public MpscUnboundedArrayQueue(int chunkSize)
/* 26:   */   {
/* 27:34 */     super(chunkSize);
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected long availableInQueue(long pIndex, long cIndex)
/* 31:   */   {
/* 32:41 */     return 2147483647L;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int capacity()
/* 36:   */   {
/* 37:47 */     return -1;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public int drain(MessagePassingQueue.Consumer<E> c)
/* 41:   */   {
/* 42:53 */     return drain(c, 4096);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public int fill(MessagePassingQueue.Supplier<E> s)
/* 46:   */   {
/* 47:59 */     long result = 0L;
/* 48:60 */     int capacity = 4096;
/* 49:   */     do
/* 50:   */     {
/* 51:63 */       int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
/* 52:64 */       if (filled == 0) {
/* 53:66 */         return (int)result;
/* 54:   */       }
/* 55:68 */       result += filled;
/* 56:70 */     } while (result <= 4096L);
/* 57:71 */     return (int)result;
/* 58:   */   }
/* 59:   */   
/* 60:   */   protected int getNextBufferSize(E[] buffer)
/* 61:   */   {
/* 62:77 */     return LinkedArrayQueueUtil.length(buffer);
/* 63:   */   }
/* 64:   */   
/* 65:   */   protected long getCurrentBufferCapacity(long mask)
/* 66:   */   {
/* 67:83 */     return mask;
/* 68:   */   }
/* 69:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue
 * JD-Core Version:    0.7.0.1
 */