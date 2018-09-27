/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.Executor;
/*  4:   */ import java.util.concurrent.ThreadFactory;
/*  5:   */ 
/*  6:   */ public class DefaultEventLoopGroup
/*  7:   */   extends MultithreadEventLoopGroup
/*  8:   */ {
/*  9:   */   public DefaultEventLoopGroup()
/* 10:   */   {
/* 11:30 */     this(0);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public DefaultEventLoopGroup(int nThreads)
/* 15:   */   {
/* 16:39 */     this(nThreads, (ThreadFactory)null);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public DefaultEventLoopGroup(int nThreads, ThreadFactory threadFactory)
/* 20:   */   {
/* 21:49 */     super(nThreads, threadFactory, new Object[0]);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public DefaultEventLoopGroup(int nThreads, Executor executor)
/* 25:   */   {
/* 26:59 */     super(nThreads, executor, new Object[0]);
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected EventLoop newChild(Executor executor, Object... args)
/* 30:   */     throws Exception
/* 31:   */   {
/* 32:64 */     return new DefaultEventLoop(this, executor);
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */