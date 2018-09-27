/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public class FixedRecvByteBufAllocator
/*  4:   */   extends DefaultMaxMessagesRecvByteBufAllocator
/*  5:   */ {
/*  6:   */   private final int bufferSize;
/*  7:   */   
/*  8:   */   private final class HandleImpl
/*  9:   */     extends DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle
/* 10:   */   {
/* 11:   */     private final int bufferSize;
/* 12:   */     
/* 13:   */     public HandleImpl(int bufferSize)
/* 14:   */     {
/* 15:29 */       super();
/* 16:30 */       this.bufferSize = bufferSize;
/* 17:   */     }
/* 18:   */     
/* 19:   */     public int guess()
/* 20:   */     {
/* 21:35 */       return this.bufferSize;
/* 22:   */     }
/* 23:   */   }
/* 24:   */   
/* 25:   */   public FixedRecvByteBufAllocator(int bufferSize)
/* 26:   */   {
/* 27:44 */     if (bufferSize <= 0) {
/* 28:45 */       throw new IllegalArgumentException("bufferSize must greater than 0: " + bufferSize);
/* 29:   */     }
/* 30:48 */     this.bufferSize = bufferSize;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public RecvByteBufAllocator.Handle newHandle()
/* 34:   */   {
/* 35:54 */     return new HandleImpl(this.bufferSize);
/* 36:   */   }
/* 37:   */   
/* 38:   */   public FixedRecvByteBufAllocator respectMaybeMoreData(boolean respectMaybeMoreData)
/* 39:   */   {
/* 40:59 */     super.respectMaybeMoreData(respectMaybeMoreData);
/* 41:60 */     return this;
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.FixedRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */