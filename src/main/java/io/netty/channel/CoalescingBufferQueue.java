/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufAllocator;
/*  5:   */ import io.netty.buffer.CompositeByteBuf;
/*  6:   */ import io.netty.buffer.Unpooled;
/*  7:   */ import io.netty.util.internal.ObjectUtil;
/*  8:   */ 
/*  9:   */ public final class CoalescingBufferQueue
/* 10:   */   extends AbstractCoalescingBufferQueue
/* 11:   */ {
/* 12:   */   private final Channel channel;
/* 13:   */   
/* 14:   */   public CoalescingBufferQueue(Channel channel)
/* 15:   */   {
/* 16:40 */     this(channel, 4);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public CoalescingBufferQueue(Channel channel, int initSize)
/* 20:   */   {
/* 21:44 */     this(channel, initSize, false);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public CoalescingBufferQueue(Channel channel, int initSize, boolean updateWritability)
/* 25:   */   {
/* 26:48 */     super(updateWritability ? channel : null, initSize);
/* 27:49 */     this.channel = ((Channel)ObjectUtil.checkNotNull(channel, "channel"));
/* 28:   */   }
/* 29:   */   
/* 30:   */   public ByteBuf remove(int bytes, ChannelPromise aggregatePromise)
/* 31:   */   {
/* 32:63 */     return remove(this.channel.alloc(), bytes, aggregatePromise);
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void releaseAndFailAll(Throwable cause)
/* 36:   */   {
/* 37:70 */     releaseAndFailAll(this.channel, cause);
/* 38:   */   }
/* 39:   */   
/* 40:   */   protected ByteBuf compose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next)
/* 41:   */   {
/* 42:75 */     if ((cumulation instanceof CompositeByteBuf))
/* 43:   */     {
/* 44:76 */       CompositeByteBuf composite = (CompositeByteBuf)cumulation;
/* 45:77 */       composite.addComponent(true, next);
/* 46:78 */       return composite;
/* 47:   */     }
/* 48:80 */     return composeIntoComposite(alloc, cumulation, next);
/* 49:   */   }
/* 50:   */   
/* 51:   */   protected ByteBuf removeEmptyValue()
/* 52:   */   {
/* 53:85 */     return Unpooled.EMPTY_BUFFER;
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.CoalescingBufferQueue
 * JD-Core Version:    0.7.0.1
 */