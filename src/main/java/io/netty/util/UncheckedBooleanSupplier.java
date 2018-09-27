/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ public abstract interface UncheckedBooleanSupplier
/*  4:   */   extends BooleanSupplier
/*  5:   */ {
/*  6:32 */   public static final UncheckedBooleanSupplier FALSE_SUPPLIER = new UncheckedBooleanSupplier()
/*  7:   */   {
/*  8:   */     public boolean get()
/*  9:   */     {
/* 10:35 */       return false;
/* 11:   */     }
/* 12:   */   };
/* 13:42 */   public static final UncheckedBooleanSupplier TRUE_SUPPLIER = new UncheckedBooleanSupplier()
/* 14:   */   {
/* 15:   */     public boolean get()
/* 16:   */     {
/* 17:45 */       return true;
/* 18:   */     }
/* 19:   */   };
/* 20:   */   
/* 21:   */   public abstract boolean get();
/* 22:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.UncheckedBooleanSupplier
 * JD-Core Version:    0.7.0.1
 */