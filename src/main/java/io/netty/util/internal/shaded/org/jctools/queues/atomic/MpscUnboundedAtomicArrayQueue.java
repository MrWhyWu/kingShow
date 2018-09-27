/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
/*  4:   */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
/*  5:   */ import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
/*  6:   */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*  7:   */ 
/*  8:   */ public class MpscUnboundedAtomicArrayQueue<E>
/*  9:   */   extends BaseMpscLinkedAtomicArrayQueue<E>
/* 10:   */ {
/* 11:   */   long p0;
/* 12:   */   long p1;
/* 13:   */   long p2;
/* 14:   */   long p3;
/* 15:   */   long p4;
/* 16:   */   long p5;
/* 17:   */   long p6;
/* 18:   */   long p7;
/* 19:   */   long p10;
/* 20:   */   long p11;
/* 21:   */   long p12;
/* 22:   */   long p13;
/* 23:   */   long p14;
/* 24:   */   long p15;
/* 25:   */   long p16;
/* 26:   */   long p17;
/* 27:   */   
/* 28:   */   public MpscUnboundedAtomicArrayQueue(int chunkSize)
/* 29:   */   {
/* 30:46 */     super(chunkSize);
/* 31:   */   }
/* 32:   */   
/* 33:   */   protected long availableInQueue(long pIndex, long cIndex)
/* 34:   */   {
/* 35:51 */     return 2147483647L;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public int capacity()
/* 39:   */   {
/* 40:56 */     return -1;
/* 41:   */   }
/* 42:   */   
/* 43:   */   public int drain(MessagePassingQueue.Consumer<E> c)
/* 44:   */   {
/* 45:61 */     return drain(c, 4096);
/* 46:   */   }
/* 47:   */   
/* 48:   */   public int fill(MessagePassingQueue.Supplier<E> s)
/* 49:   */   {
/* 50:67 */     long result = 0L;
/* 51:68 */     int capacity = 4096;
/* 52:   */     do
/* 53:   */     {
/* 54:70 */       int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
/* 55:71 */       if (filled == 0) {
/* 56:72 */         return (int)result;
/* 57:   */       }
/* 58:74 */       result += filled;
/* 59:75 */     } while (result <= 4096L);
/* 60:76 */     return (int)result;
/* 61:   */   }
/* 62:   */   
/* 63:   */   protected int getNextBufferSize(AtomicReferenceArray<E> buffer)
/* 64:   */   {
/* 65:81 */     return LinkedAtomicArrayQueueUtil.length(buffer);
/* 66:   */   }
/* 67:   */   
/* 68:   */   protected long getCurrentBufferCapacity(long mask)
/* 69:   */   {
/* 70:86 */     return mask;
/* 71:   */   }
/* 72:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue
 * JD-Core Version:    0.7.0.1
 */