/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.PromiseNotifier;
/*  4:   */ 
/*  5:   */ public final class ChannelPromiseNotifier
/*  6:   */   extends PromiseNotifier<Void, ChannelFuture>
/*  7:   */   implements ChannelFutureListener
/*  8:   */ {
/*  9:   */   public ChannelPromiseNotifier(ChannelPromise... promises)
/* 10:   */   {
/* 11:33 */     super(promises);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public ChannelPromiseNotifier(boolean logNotifyFailure, ChannelPromise... promises)
/* 15:   */   {
/* 16:43 */     super(logNotifyFailure, promises);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelPromiseNotifier
 * JD-Core Version:    0.7.0.1
 */