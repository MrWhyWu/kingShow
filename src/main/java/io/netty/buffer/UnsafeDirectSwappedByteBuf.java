/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ 
/*  5:   */ final class UnsafeDirectSwappedByteBuf
/*  6:   */   extends AbstractUnsafeSwappedByteBuf
/*  7:   */ {
/*  8:   */   UnsafeDirectSwappedByteBuf(AbstractByteBuf buf)
/*  9:   */   {
/* 10:27 */     super(buf);
/* 11:   */   }
/* 12:   */   
/* 13:   */   private static long addr(AbstractByteBuf wrapped, int index)
/* 14:   */   {
/* 15:35 */     return wrapped.memoryAddress() + index;
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected long _getLong(AbstractByteBuf wrapped, int index)
/* 19:   */   {
/* 20:40 */     return PlatformDependent.getLong(addr(wrapped, index));
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected int _getInt(AbstractByteBuf wrapped, int index)
/* 24:   */   {
/* 25:45 */     return PlatformDependent.getInt(addr(wrapped, index));
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected short _getShort(AbstractByteBuf wrapped, int index)
/* 29:   */   {
/* 30:50 */     return PlatformDependent.getShort(addr(wrapped, index));
/* 31:   */   }
/* 32:   */   
/* 33:   */   protected void _setShort(AbstractByteBuf wrapped, int index, short value)
/* 34:   */   {
/* 35:55 */     PlatformDependent.putShort(addr(wrapped, index), value);
/* 36:   */   }
/* 37:   */   
/* 38:   */   protected void _setInt(AbstractByteBuf wrapped, int index, int value)
/* 39:   */   {
/* 40:60 */     PlatformDependent.putInt(addr(wrapped, index), value);
/* 41:   */   }
/* 42:   */   
/* 43:   */   protected void _setLong(AbstractByteBuf wrapped, int index, long value)
/* 44:   */   {
/* 45:65 */     PlatformDependent.putLong(addr(wrapped, index), value);
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnsafeDirectSwappedByteBuf
 * JD-Core Version:    0.7.0.1
 */