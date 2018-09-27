/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.nio.ByteBuffer;
/*  5:   */ 
/*  6:   */ final class WrappedUnpooledUnsafeDirectByteBuf
/*  7:   */   extends UnpooledUnsafeDirectByteBuf
/*  8:   */ {
/*  9:   */   WrappedUnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, long memoryAddress, int size, boolean doFree)
/* 10:   */   {
/* 11:25 */     super(alloc, PlatformDependent.directBuffer(memoryAddress, size), size, doFree);
/* 12:   */   }
/* 13:   */   
/* 14:   */   protected void freeDirect(ByteBuffer buffer)
/* 15:   */   {
/* 16:30 */     PlatformDependent.freeMemory(this.memoryAddress);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.WrappedUnpooledUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */