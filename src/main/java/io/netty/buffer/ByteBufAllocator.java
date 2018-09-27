/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ public abstract interface ByteBufAllocator
/*  4:   */ {
/*  5:24 */   public static final ByteBufAllocator DEFAULT = ByteBufUtil.DEFAULT_ALLOCATOR;
/*  6:   */   
/*  7:   */   public abstract ByteBuf buffer();
/*  8:   */   
/*  9:   */   public abstract ByteBuf buffer(int paramInt);
/* 10:   */   
/* 11:   */   public abstract ByteBuf buffer(int paramInt1, int paramInt2);
/* 12:   */   
/* 13:   */   public abstract ByteBuf ioBuffer();
/* 14:   */   
/* 15:   */   public abstract ByteBuf ioBuffer(int paramInt);
/* 16:   */   
/* 17:   */   public abstract ByteBuf ioBuffer(int paramInt1, int paramInt2);
/* 18:   */   
/* 19:   */   public abstract ByteBuf heapBuffer();
/* 20:   */   
/* 21:   */   public abstract ByteBuf heapBuffer(int paramInt);
/* 22:   */   
/* 23:   */   public abstract ByteBuf heapBuffer(int paramInt1, int paramInt2);
/* 24:   */   
/* 25:   */   public abstract ByteBuf directBuffer();
/* 26:   */   
/* 27:   */   public abstract ByteBuf directBuffer(int paramInt);
/* 28:   */   
/* 29:   */   public abstract ByteBuf directBuffer(int paramInt1, int paramInt2);
/* 30:   */   
/* 31:   */   public abstract CompositeByteBuf compositeBuffer();
/* 32:   */   
/* 33:   */   public abstract CompositeByteBuf compositeBuffer(int paramInt);
/* 34:   */   
/* 35:   */   public abstract CompositeByteBuf compositeHeapBuffer();
/* 36:   */   
/* 37:   */   public abstract CompositeByteBuf compositeHeapBuffer(int paramInt);
/* 38:   */   
/* 39:   */   public abstract CompositeByteBuf compositeDirectBuffer();
/* 40:   */   
/* 41:   */   public abstract CompositeByteBuf compositeDirectBuffer(int paramInt);
/* 42:   */   
/* 43:   */   public abstract boolean isDirectBufferPooled();
/* 44:   */   
/* 45:   */   public abstract int calculateNewCapacity(int paramInt1, int paramInt2);
/* 46:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.ByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */