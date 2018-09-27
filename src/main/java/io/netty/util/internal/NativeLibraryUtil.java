/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ final class NativeLibraryUtil
/*  4:   */ {
/*  5:   */   public static void loadLibrary(String libName, boolean absolute)
/*  6:   */   {
/*  7:35 */     if (absolute) {
/*  8:36 */       System.load(libName);
/*  9:   */     } else {
/* 10:38 */       System.loadLibrary(libName);
/* 11:   */     }
/* 12:   */   }
/* 13:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.NativeLibraryUtil
 * JD-Core Version:    0.7.0.1
 */