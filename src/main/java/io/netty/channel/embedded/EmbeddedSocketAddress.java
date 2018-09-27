/*  1:   */ package io.netty.channel.embedded;
/*  2:   */ 
/*  3:   */ import java.net.SocketAddress;
/*  4:   */ 
/*  5:   */ final class EmbeddedSocketAddress
/*  6:   */   extends SocketAddress
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 1400788804624980619L;
/*  9:   */   
/* 10:   */   public String toString()
/* 11:   */   {
/* 12:25 */     return "embedded";
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.embedded.EmbeddedSocketAddress
 * JD-Core Version:    0.7.0.1
 */