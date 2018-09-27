/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.IntSupplier;
/*  4:   */ 
/*  5:   */ final class DefaultSelectStrategy
/*  6:   */   implements SelectStrategy
/*  7:   */ {
/*  8:24 */   static final SelectStrategy INSTANCE = new DefaultSelectStrategy();
/*  9:   */   
/* 10:   */   public int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks)
/* 11:   */     throws Exception
/* 12:   */   {
/* 13:30 */     return hasTasks ? selectSupplier.get() : -1;
/* 14:   */   }
/* 15:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultSelectStrategy
 * JD-Core Version:    0.7.0.1
 */