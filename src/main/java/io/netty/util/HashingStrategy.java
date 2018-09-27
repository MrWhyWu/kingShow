/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ public abstract interface HashingStrategy<T>
/*  4:   */ {
/*  5:62 */   public static final HashingStrategy JAVA_HASHER = new HashingStrategy()
/*  6:   */   {
/*  7:   */     public int hashCode(Object obj)
/*  8:   */     {
/*  9:65 */       return obj != null ? obj.hashCode() : 0;
/* 10:   */     }
/* 11:   */     
/* 12:   */     public boolean equals(Object a, Object b)
/* 13:   */     {
/* 14:70 */       return (a == b) || ((a != null) && (a.equals(b)));
/* 15:   */     }
/* 16:   */   };
/* 17:   */   
/* 18:   */   public abstract int hashCode(T paramT);
/* 19:   */   
/* 20:   */   public abstract boolean equals(T paramT1, T paramT2);
/* 21:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.HashingStrategy
 * JD-Core Version:    0.7.0.1
 */