/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.AbstractEventExecutor;
/*  4:   */ 
/*  5:   */ public abstract class AbstractEventLoop
/*  6:   */   extends AbstractEventExecutor
/*  7:   */   implements EventLoop
/*  8:   */ {
/*  9:   */   protected AbstractEventLoop() {}
/* 10:   */   
/* 11:   */   protected AbstractEventLoop(EventLoopGroup parent)
/* 12:   */   {
/* 13:29 */     super(parent);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public EventLoopGroup parent()
/* 17:   */   {
/* 18:34 */     return (EventLoopGroup)super.parent();
/* 19:   */   }
/* 20:   */   
/* 21:   */   public EventLoop next()
/* 22:   */   {
/* 23:39 */     return (EventLoop)super.next();
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AbstractEventLoop
 * JD-Core Version:    0.7.0.1
 */