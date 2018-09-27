/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public final class ChannelMetadata
/*  4:   */ {
/*  5:   */   private final boolean hasDisconnect;
/*  6:   */   private final int defaultMaxMessagesPerRead;
/*  7:   */   
/*  8:   */   public ChannelMetadata(boolean hasDisconnect)
/*  9:   */   {
/* 10:36 */     this(hasDisconnect, 1);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public ChannelMetadata(boolean hasDisconnect, int defaultMaxMessagesPerRead)
/* 14:   */   {
/* 15:49 */     if (defaultMaxMessagesPerRead <= 0) {
/* 16:50 */       throw new IllegalArgumentException("defaultMaxMessagesPerRead: " + defaultMaxMessagesPerRead + " (expected > 0)");
/* 17:   */     }
/* 18:53 */     this.hasDisconnect = hasDisconnect;
/* 19:54 */     this.defaultMaxMessagesPerRead = defaultMaxMessagesPerRead;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public boolean hasDisconnect()
/* 23:   */   {
/* 24:63 */     return this.hasDisconnect;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public int defaultMaxMessagesPerRead()
/* 28:   */   {
/* 29:71 */     return this.defaultMaxMessagesPerRead;
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelMetadata
 * JD-Core Version:    0.7.0.1
 */