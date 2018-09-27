/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.DefaultThreadFactory;
/*  4:   */ import java.util.concurrent.Executor;
/*  5:   */ import java.util.concurrent.ThreadFactory;
/*  6:   */ 
/*  7:   */ public class DefaultEventLoop
/*  8:   */   extends SingleThreadEventLoop
/*  9:   */ {
/* 10:   */   public DefaultEventLoop()
/* 11:   */   {
/* 12:26 */     this((EventLoopGroup)null);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public DefaultEventLoop(ThreadFactory threadFactory)
/* 16:   */   {
/* 17:30 */     this(null, threadFactory);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public DefaultEventLoop(Executor executor)
/* 21:   */   {
/* 22:34 */     this(null, executor);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public DefaultEventLoop(EventLoopGroup parent)
/* 26:   */   {
/* 27:38 */     this(parent, new DefaultThreadFactory(DefaultEventLoop.class));
/* 28:   */   }
/* 29:   */   
/* 30:   */   public DefaultEventLoop(EventLoopGroup parent, ThreadFactory threadFactory)
/* 31:   */   {
/* 32:42 */     super(parent, threadFactory, true);
/* 33:   */   }
/* 34:   */   
/* 35:   */   public DefaultEventLoop(EventLoopGroup parent, Executor executor)
/* 36:   */   {
/* 37:46 */     super(parent, executor, true);
/* 38:   */   }
/* 39:   */   
/* 40:   */   protected void run()
/* 41:   */   {
/* 42:   */     for (;;)
/* 43:   */     {
/* 44:52 */       Runnable task = takeTask();
/* 45:53 */       if (task != null)
/* 46:   */       {
/* 47:54 */         task.run();
/* 48:55 */         updateLastExecutionTime();
/* 49:   */       }
/* 50:58 */       if (confirmShutdown()) {
/* 51:   */         break;
/* 52:   */       }
/* 53:   */     }
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultEventLoop
 * JD-Core Version:    0.7.0.1
 */