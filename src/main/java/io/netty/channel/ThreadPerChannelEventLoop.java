/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import java.util.Queue;
/*  4:   */ import java.util.Set;
/*  5:   */ 
/*  6:   */ public class ThreadPerChannelEventLoop
/*  7:   */   extends SingleThreadEventLoop
/*  8:   */ {
/*  9:   */   private final ThreadPerChannelEventLoopGroup parent;
/* 10:   */   private Channel ch;
/* 11:   */   
/* 12:   */   public ThreadPerChannelEventLoop(ThreadPerChannelEventLoopGroup parent)
/* 13:   */   {
/* 14:29 */     super(parent, parent.executor, true);
/* 15:30 */     this.parent = parent;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public ChannelFuture register(ChannelPromise promise)
/* 19:   */   {
/* 20:35 */     super.register(promise).addListener(new ChannelFutureListener()
/* 21:   */     {
/* 22:   */       public void operationComplete(ChannelFuture future)
/* 23:   */         throws Exception
/* 24:   */       {
/* 25:38 */         if (future.isSuccess()) {
/* 26:39 */           ThreadPerChannelEventLoop.this.ch = future.channel();
/* 27:   */         } else {
/* 28:41 */           ThreadPerChannelEventLoop.this.deregister();
/* 29:   */         }
/* 30:   */       }
/* 31:   */     });
/* 32:   */   }
/* 33:   */   
/* 34:   */   @Deprecated
/* 35:   */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 36:   */   {
/* 37:50 */     super.register(channel, promise).addListener(new ChannelFutureListener()
/* 38:   */     {
/* 39:   */       public void operationComplete(ChannelFuture future)
/* 40:   */         throws Exception
/* 41:   */       {
/* 42:53 */         if (future.isSuccess()) {
/* 43:54 */           ThreadPerChannelEventLoop.this.ch = future.channel();
/* 44:   */         } else {
/* 45:56 */           ThreadPerChannelEventLoop.this.deregister();
/* 46:   */         }
/* 47:   */       }
/* 48:   */     });
/* 49:   */   }
/* 50:   */   
/* 51:   */   protected void run()
/* 52:   */   {
/* 53:   */     for (;;)
/* 54:   */     {
/* 55:65 */       Runnable task = takeTask();
/* 56:66 */       if (task != null)
/* 57:   */       {
/* 58:67 */         task.run();
/* 59:68 */         updateLastExecutionTime();
/* 60:   */       }
/* 61:71 */       Channel ch = this.ch;
/* 62:72 */       if (isShuttingDown())
/* 63:   */       {
/* 64:73 */         if (ch != null) {
/* 65:74 */           ch.unsafe().close(ch.unsafe().voidPromise());
/* 66:   */         }
/* 67:76 */         if (confirmShutdown()) {
/* 68:   */           break;
/* 69:   */         }
/* 70:   */       }
/* 71:80 */       else if (ch != null)
/* 72:   */       {
/* 73:82 */         if (!ch.isRegistered())
/* 74:   */         {
/* 75:83 */           runAllTasks();
/* 76:84 */           deregister();
/* 77:   */         }
/* 78:   */       }
/* 79:   */     }
/* 80:   */   }
/* 81:   */   
/* 82:   */   protected void deregister()
/* 83:   */   {
/* 84:92 */     this.ch = null;
/* 85:93 */     this.parent.activeChildren.remove(this);
/* 86:94 */     this.parent.idleChildren.add(this);
/* 87:   */   }
/* 88:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ThreadPerChannelEventLoop
 * JD-Core Version:    0.7.0.1
 */