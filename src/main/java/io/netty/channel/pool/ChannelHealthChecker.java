/*  1:   */ package io.netty.channel.pool;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.EventLoop;
/*  5:   */ import io.netty.util.concurrent.Future;
/*  6:   */ 
/*  7:   */ public abstract interface ChannelHealthChecker
/*  8:   */ {
/*  9:32 */   public static final ChannelHealthChecker ACTIVE = new ChannelHealthChecker()
/* 10:   */   {
/* 11:   */     public Future<Boolean> isHealthy(Channel channel)
/* 12:   */     {
/* 13:35 */       EventLoop loop = channel.eventLoop();
/* 14:36 */       return channel.isActive() ? loop.newSucceededFuture(Boolean.TRUE) : loop.newSucceededFuture(Boolean.FALSE);
/* 15:   */     }
/* 16:   */   };
/* 17:   */   
/* 18:   */   public abstract Future<Boolean> isHealthy(Channel paramChannel);
/* 19:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.pool.ChannelHealthChecker
 * JD-Core Version:    0.7.0.1
 */