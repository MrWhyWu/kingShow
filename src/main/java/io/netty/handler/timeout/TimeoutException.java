/*  1:   */ package io.netty.handler.timeout;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelException;
/*  4:   */ 
/*  5:   */ public class TimeoutException
/*  6:   */   extends ChannelException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 4673641882869672533L;
/*  9:   */   
/* 10:   */   public Throwable fillInStackTrace()
/* 11:   */   {
/* 12:32 */     return this;
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.timeout.TimeoutException
 * JD-Core Version:    0.7.0.1
 */