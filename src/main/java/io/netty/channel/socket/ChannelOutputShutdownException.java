/*  1:   */ package io.netty.channel.socket;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ 
/*  5:   */ public final class ChannelOutputShutdownException
/*  6:   */   extends IOException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 6712549938359321378L;
/*  9:   */   
/* 10:   */   public ChannelOutputShutdownException(String msg)
/* 11:   */   {
/* 12:32 */     super(msg);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ChannelOutputShutdownException(String msg, Throwable cause)
/* 16:   */   {
/* 17:36 */     super(msg, cause);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.ChannelOutputShutdownException
 * JD-Core Version:    0.7.0.1
 */