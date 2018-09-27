/*  1:   */ package io.netty.channel.socket.nio;
/*  2:   */ 
/*  3:   */ import io.netty.channel.socket.InternetProtocolFamily;
/*  4:   */ import java.net.ProtocolFamily;
/*  5:   */ import java.net.StandardProtocolFamily;
/*  6:   */ 
/*  7:   */ final class ProtocolFamilyConverter
/*  8:   */ {
/*  9:   */   public static ProtocolFamily convert(InternetProtocolFamily family)
/* 10:   */   {
/* 11:36 */     switch (1.$SwitchMap$io$netty$channel$socket$InternetProtocolFamily[family.ordinal()])
/* 12:   */     {
/* 13:   */     case 1: 
/* 14:38 */       return StandardProtocolFamily.INET;
/* 15:   */     case 2: 
/* 16:40 */       return StandardProtocolFamily.INET6;
/* 17:   */     }
/* 18:42 */     throw new IllegalArgumentException();
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.nio.ProtocolFamilyConverter
 * JD-Core Version:    0.7.0.1
 */