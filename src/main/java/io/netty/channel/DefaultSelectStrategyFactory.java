/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public final class DefaultSelectStrategyFactory
/*  4:   */   implements SelectStrategyFactory
/*  5:   */ {
/*  6:22 */   public static final SelectStrategyFactory INSTANCE = new DefaultSelectStrategyFactory();
/*  7:   */   
/*  8:   */   public SelectStrategy newSelectStrategy()
/*  9:   */   {
/* 10:28 */     return DefaultSelectStrategy.INSTANCE;
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultSelectStrategyFactory
 * JD-Core Version:    0.7.0.1
 */