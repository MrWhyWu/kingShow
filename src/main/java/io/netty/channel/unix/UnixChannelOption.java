/*  1:   */ package io.netty.channel.unix;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelOption;
/*  4:   */ 
/*  5:   */ public class UnixChannelOption<T>
/*  6:   */   extends ChannelOption<T>
/*  7:   */ {
/*  8:21 */   public static final ChannelOption<Boolean> SO_REUSEPORT = valueOf(UnixChannelOption.class, "SO_REUSEPORT");
/*  9:23 */   public static final ChannelOption<DomainSocketReadMode> DOMAIN_SOCKET_READ_MODE = ChannelOption.valueOf(UnixChannelOption.class, "DOMAIN_SOCKET_READ_MODE");
/* 10:   */   
/* 11:   */   protected UnixChannelOption()
/* 12:   */   {
/* 13:27 */     super(null);
/* 14:   */   }
/* 15:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.unix.UnixChannelOption
 * JD-Core Version:    0.7.0.1
 */