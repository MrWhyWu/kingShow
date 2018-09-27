/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.EventExecutor;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ 
/*  6:   */ final class FailedChannelFuture
/*  7:   */   extends CompleteChannelFuture
/*  8:   */ {
/*  9:   */   private final Throwable cause;
/* 10:   */   
/* 11:   */   FailedChannelFuture(Channel channel, EventExecutor executor, Throwable cause)
/* 12:   */   {
/* 13:37 */     super(channel, executor);
/* 14:38 */     if (cause == null) {
/* 15:39 */       throw new NullPointerException("cause");
/* 16:   */     }
/* 17:41 */     this.cause = cause;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Throwable cause()
/* 21:   */   {
/* 22:46 */     return this.cause;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public boolean isSuccess()
/* 26:   */   {
/* 27:51 */     return false;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public ChannelFuture sync()
/* 31:   */   {
/* 32:56 */     PlatformDependent.throwException(this.cause);
/* 33:57 */     return this;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public ChannelFuture syncUninterruptibly()
/* 37:   */   {
/* 38:62 */     PlatformDependent.throwException(this.cause);
/* 39:63 */     return this;
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.FailedChannelFuture
 * JD-Core Version:    0.7.0.1
 */