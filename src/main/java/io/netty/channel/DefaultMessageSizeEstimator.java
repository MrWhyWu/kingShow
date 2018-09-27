/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufHolder;
/*  5:   */ 
/*  6:   */ public final class DefaultMessageSizeEstimator
/*  7:   */   implements MessageSizeEstimator
/*  8:   */ {
/*  9:   */   private static final class HandleImpl
/* 10:   */     implements MessageSizeEstimator.Handle
/* 11:   */   {
/* 12:   */     private final int unknownSize;
/* 13:   */     
/* 14:   */     private HandleImpl(int unknownSize)
/* 15:   */     {
/* 16:31 */       this.unknownSize = unknownSize;
/* 17:   */     }
/* 18:   */     
/* 19:   */     public int size(Object msg)
/* 20:   */     {
/* 21:36 */       if ((msg instanceof ByteBuf)) {
/* 22:37 */         return ((ByteBuf)msg).readableBytes();
/* 23:   */       }
/* 24:39 */       if ((msg instanceof ByteBufHolder)) {
/* 25:40 */         return ((ByteBufHolder)msg).content().readableBytes();
/* 26:   */       }
/* 27:42 */       if ((msg instanceof FileRegion)) {
/* 28:43 */         return 0;
/* 29:   */       }
/* 30:45 */       return this.unknownSize;
/* 31:   */     }
/* 32:   */   }
/* 33:   */   
/* 34:52 */   public static final MessageSizeEstimator DEFAULT = new DefaultMessageSizeEstimator(8);
/* 35:   */   private final MessageSizeEstimator.Handle handle;
/* 36:   */   
/* 37:   */   public DefaultMessageSizeEstimator(int unknownSize)
/* 38:   */   {
/* 39:62 */     if (unknownSize < 0) {
/* 40:63 */       throw new IllegalArgumentException("unknownSize: " + unknownSize + " (expected: >= 0)");
/* 41:   */     }
/* 42:65 */     this.handle = new HandleImpl(unknownSize, null);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public MessageSizeEstimator.Handle newHandle()
/* 46:   */   {
/* 47:70 */     return this.handle;
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.DefaultMessageSizeEstimator
 * JD-Core Version:    0.7.0.1
 */