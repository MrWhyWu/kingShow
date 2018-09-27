/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ @Deprecated
/*  4:   */ public class SlicedByteBuf
/*  5:   */   extends AbstractUnpooledSlicedByteBuf
/*  6:   */ {
/*  7:   */   private int length;
/*  8:   */   
/*  9:   */   public SlicedByteBuf(ByteBuf buffer, int index, int length)
/* 10:   */   {
/* 11:32 */     super(buffer, index, length);
/* 12:   */   }
/* 13:   */   
/* 14:   */   final void initLength(int length)
/* 15:   */   {
/* 16:37 */     this.length = length;
/* 17:   */   }
/* 18:   */   
/* 19:   */   final int length()
/* 20:   */   {
/* 21:42 */     return this.length;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public int capacity()
/* 25:   */   {
/* 26:47 */     return this.length;
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.SlicedByteBuf
 * JD-Core Version:    0.7.0.1
 */