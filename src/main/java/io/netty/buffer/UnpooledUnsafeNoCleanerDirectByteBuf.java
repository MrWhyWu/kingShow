/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.nio.ByteBuffer;
/*  5:   */ 
/*  6:   */ class UnpooledUnsafeNoCleanerDirectByteBuf
/*  7:   */   extends UnpooledUnsafeDirectByteBuf
/*  8:   */ {
/*  9:   */   UnpooledUnsafeNoCleanerDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/* 10:   */   {
/* 11:25 */     super(alloc, initialCapacity, maxCapacity);
/* 12:   */   }
/* 13:   */   
/* 14:   */   protected ByteBuffer allocateDirect(int initialCapacity)
/* 15:   */   {
/* 16:30 */     return PlatformDependent.allocateDirectNoCleaner(initialCapacity);
/* 17:   */   }
/* 18:   */   
/* 19:   */   ByteBuffer reallocateDirect(ByteBuffer oldBuffer, int initialCapacity)
/* 20:   */   {
/* 21:34 */     return PlatformDependent.reallocateDirectNoCleaner(oldBuffer, initialCapacity);
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected void freeDirect(ByteBuffer buffer)
/* 25:   */   {
/* 26:39 */     PlatformDependent.freeDirectNoCleaner(buffer);
/* 27:   */   }
/* 28:   */   
/* 29:   */   public ByteBuf capacity(int newCapacity)
/* 30:   */   {
/* 31:44 */     checkNewCapacity(newCapacity);
/* 32:   */     
/* 33:46 */     int oldCapacity = capacity();
/* 34:47 */     if (newCapacity == oldCapacity) {
/* 35:48 */       return this;
/* 36:   */     }
/* 37:51 */     ByteBuffer newBuffer = reallocateDirect(this.buffer, newCapacity);
/* 38:53 */     if (newCapacity < oldCapacity) {
/* 39:54 */       if (readerIndex() < newCapacity)
/* 40:   */       {
/* 41:55 */         if (writerIndex() > newCapacity) {
/* 42:56 */           writerIndex(newCapacity);
/* 43:   */         }
/* 44:   */       }
/* 45:   */       else {
/* 46:59 */         setIndex(newCapacity, newCapacity);
/* 47:   */       }
/* 48:   */     }
/* 49:62 */     setByteBuffer(newBuffer, false);
/* 50:63 */     return this;
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnpooledUnsafeNoCleanerDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */