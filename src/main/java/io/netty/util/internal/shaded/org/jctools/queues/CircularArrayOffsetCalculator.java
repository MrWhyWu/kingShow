/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
/*  4:   */ 
/*  5:   */ public final class CircularArrayOffsetCalculator
/*  6:   */ {
/*  7:   */   public static <E> E[] allocate(int capacity)
/*  8:   */   {
/*  9:14 */     return new Object[capacity];
/* 10:   */   }
/* 11:   */   
/* 12:   */   public static long calcElementOffset(long index, long mask)
/* 13:   */   {
/* 14:24 */     return UnsafeRefArrayAccess.REF_ARRAY_BASE + ((index & mask) << UnsafeRefArrayAccess.REF_ELEMENT_SHIFT);
/* 15:   */   }
/* 16:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.CircularArrayOffsetCalculator
 * JD-Core Version:    0.7.0.1
 */