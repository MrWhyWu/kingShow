/*  1:   */ package io.netty.channel.embedded;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelId;
/*  4:   */ 
/*  5:   */ final class EmbeddedChannelId
/*  6:   */   implements ChannelId
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = -251711922203466130L;
/*  9:28 */   static final ChannelId INSTANCE = new EmbeddedChannelId();
/* 10:   */   
/* 11:   */   public String asShortText()
/* 12:   */   {
/* 13:34 */     return toString();
/* 14:   */   }
/* 15:   */   
/* 16:   */   public String asLongText()
/* 17:   */   {
/* 18:39 */     return toString();
/* 19:   */   }
/* 20:   */   
/* 21:   */   public int compareTo(ChannelId o)
/* 22:   */   {
/* 23:44 */     if ((o instanceof EmbeddedChannelId)) {
/* 24:45 */       return 0;
/* 25:   */     }
/* 26:48 */     return asLongText().compareTo(o.asLongText());
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int hashCode()
/* 30:   */   {
/* 31:53 */     return 0;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public boolean equals(Object obj)
/* 35:   */   {
/* 36:58 */     return obj instanceof EmbeddedChannelId;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public String toString()
/* 40:   */   {
/* 41:63 */     return "embedded";
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.embedded.EmbeddedChannelId
 * JD-Core Version:    0.7.0.1
 */