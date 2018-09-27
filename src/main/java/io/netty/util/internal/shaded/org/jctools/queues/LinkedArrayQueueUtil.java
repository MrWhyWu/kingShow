/*  1:   */ package io.netty.util.internal.shaded.org.jctools.queues;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
/*  4:   */ 
/*  5:   */ final class LinkedArrayQueueUtil
/*  6:   */ {
/*  7:   */   static int length(Object[] buf)
/*  8:   */   {
/*  9:14 */     return buf.length;
/* 10:   */   }
/* 11:   */   
/* 12:   */   static long modifiedCalcElementOffset(long index, long mask)
/* 13:   */   {
/* 14:24 */     return UnsafeRefArrayAccess.REF_ARRAY_BASE + ((index & mask) << UnsafeRefArrayAccess.REF_ELEMENT_SHIFT - 1);
/* 15:   */   }
/* 16:   */   
/* 17:   */   static long nextArrayOffset(Object[] curr)
/* 18:   */   {
/* 19:29 */     return UnsafeRefArrayAccess.REF_ARRAY_BASE + (length(curr) - 1 << UnsafeRefArrayAccess.REF_ELEMENT_SHIFT);
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.LinkedArrayQueueUtil
 * JD-Core Version:    0.7.0.1
 */