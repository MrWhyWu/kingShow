/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ public abstract interface BooleanSupplier
/*  4:   */ {
/*  5:32 */   public static final BooleanSupplier FALSE_SUPPLIER = new BooleanSupplier()
/*  6:   */   {
/*  7:   */     public boolean get()
/*  8:   */     {
/*  9:35 */       return false;
/* 10:   */     }
/* 11:   */   };
/* 12:42 */   public static final BooleanSupplier TRUE_SUPPLIER = new BooleanSupplier()
/* 13:   */   {
/* 14:   */     public boolean get()
/* 15:   */     {
/* 16:45 */       return true;
/* 17:   */     }
/* 18:   */   };
/* 19:   */   
/* 20:   */   public abstract boolean get()
/* 21:   */     throws Exception;
/* 22:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.BooleanSupplier
 * JD-Core Version:    0.7.0.1
 */