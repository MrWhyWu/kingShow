/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ 
/*  5:   */ final class UnsafeHeapSwappedByteBuf
/*  6:   */   extends AbstractUnsafeSwappedByteBuf
/*  7:   */ {
/*  8:   */   UnsafeHeapSwappedByteBuf(AbstractByteBuf buf)
/*  9:   */   {
/* 10:27 */     super(buf);
/* 11:   */   }
/* 12:   */   
/* 13:   */   private static int idx(ByteBuf wrapped, int index)
/* 14:   */   {
/* 15:31 */     return wrapped.arrayOffset() + index;
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected long _getLong(AbstractByteBuf wrapped, int index)
/* 19:   */   {
/* 20:36 */     return PlatformDependent.getLong(wrapped.array(), idx(wrapped, index));
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected int _getInt(AbstractByteBuf wrapped, int index)
/* 24:   */   {
/* 25:41 */     return PlatformDependent.getInt(wrapped.array(), idx(wrapped, index));
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected short _getShort(AbstractByteBuf wrapped, int index)
/* 29:   */   {
/* 30:46 */     return PlatformDependent.getShort(wrapped.array(), idx(wrapped, index));
/* 31:   */   }
/* 32:   */   
/* 33:   */   protected void _setShort(AbstractByteBuf wrapped, int index, short value)
/* 34:   */   {
/* 35:51 */     PlatformDependent.putShort(wrapped.array(), idx(wrapped, index), value);
/* 36:   */   }
/* 37:   */   
/* 38:   */   protected void _setInt(AbstractByteBuf wrapped, int index, int value)
/* 39:   */   {
/* 40:56 */     PlatformDependent.putInt(wrapped.array(), idx(wrapped, index), value);
/* 41:   */   }
/* 42:   */   
/* 43:   */   protected void _setLong(AbstractByteBuf wrapped, int index, long value)
/* 44:   */   {
/* 45:61 */     PlatformDependent.putLong(wrapped.array(), idx(wrapped, index), value);
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnsafeHeapSwappedByteBuf
 * JD-Core Version:    0.7.0.1
 */