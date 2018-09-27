/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import java.net.ConnectException;
/*  4:   */ 
/*  5:   */ public class ConnectTimeoutException
/*  6:   */   extends ConnectException
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 2317065249988317463L;
/*  9:   */   
/* 10:   */   public ConnectTimeoutException(String msg)
/* 11:   */   {
/* 12:28 */     super(msg);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ConnectTimeoutException() {}
/* 16:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ConnectTimeoutException
 * JD-Core Version:    0.7.0.1
 */