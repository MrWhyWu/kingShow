/*   1:    */ package io.netty.util.internal.shaded.org.jctools.util;
/*   2:    */ 
/*   3:    */ import sun.misc.Unsafe;
/*   4:    */ 
/*   5:    */ public final class UnsafeRefArrayAccess
/*   6:    */ {
/*   7:    */   static
/*   8:    */   {
/*   9: 40 */     int scale = UnsafeAccess.UNSAFE.arrayIndexScale([Ljava.lang.Object.class);
/*  10: 41 */     if (4 == scale) {
/*  11: 43 */       REF_ELEMENT_SHIFT = 2;
/*  12: 45 */     } else if (8 == scale) {
/*  13: 47 */       REF_ELEMENT_SHIFT = 3;
/*  14:    */     } else {
/*  15: 51 */       throw new IllegalStateException("Unknown pointer size");
/*  16:    */     }
/*  17:    */   }
/*  18:    */   
/*  19: 53 */   public static final long REF_ARRAY_BASE = UnsafeAccess.UNSAFE.arrayBaseOffset([Ljava.lang.Object.class);
/*  20:    */   public static final int REF_ELEMENT_SHIFT;
/*  21:    */   
/*  22:    */   public static <E> void spElement(E[] buffer, long offset, E e)
/*  23:    */   {
/*  24: 65 */     UnsafeAccess.UNSAFE.putObject(buffer, offset, e);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public static <E> void soElement(E[] buffer, long offset, E e)
/*  28:    */   {
/*  29: 77 */     UnsafeAccess.UNSAFE.putOrderedObject(buffer, offset, e);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public static <E> E lpElement(E[] buffer, long offset)
/*  33:    */   {
/*  34: 90 */     return UnsafeAccess.UNSAFE.getObject(buffer, offset);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public static <E> E lvElement(E[] buffer, long offset)
/*  38:    */   {
/*  39:103 */     return UnsafeAccess.UNSAFE.getObjectVolatile(buffer, offset);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public static long calcElementOffset(long index)
/*  43:    */   {
/*  44:112 */     return REF_ARRAY_BASE + (index << REF_ELEMENT_SHIFT);
/*  45:    */   }
/*  46:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess
 * JD-Core Version:    0.7.0.1
 */