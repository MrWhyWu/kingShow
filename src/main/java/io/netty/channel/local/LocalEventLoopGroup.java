/*  1:   */ package io.netty.channel.local;
/*  2:   */ 
/*  3:   */ import io.netty.channel.DefaultEventLoopGroup;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ @Deprecated
/*  7:   */ public class LocalEventLoopGroup
/*  8:   */   extends DefaultEventLoopGroup
/*  9:   */ {
/* 10:   */   public LocalEventLoopGroup() {}
/* 11:   */   
/* 12:   */   public LocalEventLoopGroup(int nThreads)
/* 13:   */   {
/* 14:39 */     super(nThreads);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public LocalEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/* 18:   */   {
/* 19:49 */     super(nThreads, threadFactory);
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.local.LocalEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */