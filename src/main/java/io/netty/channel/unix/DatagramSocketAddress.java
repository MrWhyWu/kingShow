/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ import java.net.InetSocketAddress;
/*  4:   */ 
/*  5:   */ public final class DatagramSocketAddress
/*  6:   */   extends InetSocketAddress
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 3094819287843178401L;
/*  9:   */   private final int receivedAmount;
/* 10:   */   
/* 11:   */   DatagramSocketAddress(String addr, int port, int receivedAmount)
/* 12:   */   {
/* 13:33 */     super(addr, port);
/* 14:34 */     this.receivedAmount = receivedAmount;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public int receivedAmount()
/* 18:   */   {
/* 19:38 */     return this.receivedAmount;
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.DatagramSocketAddress
 * JD-Core Version:    0.7.0.1
 */