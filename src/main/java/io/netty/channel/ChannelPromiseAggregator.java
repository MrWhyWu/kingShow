/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.util.concurrent.PromiseAggregator;
/*  4:   */ 
/*  5:   */ @Deprecated
/*  6:   */ public final class ChannelPromiseAggregator
/*  7:   */   extends PromiseAggregator<Void, ChannelFuture>
/*  8:   */   implements ChannelFutureListener
/*  9:   */ {
/* 10:   */   public ChannelPromiseAggregator(ChannelPromise aggregatePromise)
/* 11:   */   {
/* 12:35 */     super(aggregatePromise);
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelPromiseAggregator
 * JD-Core Version:    0.7.0.1
 */