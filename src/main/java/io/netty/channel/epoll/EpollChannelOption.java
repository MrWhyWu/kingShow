/*  1:   */ package io.netty.channel.epoll;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelOption;
/*  4:   */ import io.netty.channel.unix.UnixChannelOption;
/*  5:   */ import java.net.InetAddress;
/*  6:   */ import java.util.Map;
/*  7:   */ 
/*  8:   */ public final class EpollChannelOption<T>
/*  9:   */   extends UnixChannelOption<T>
/* 10:   */ {
/* 11:25 */   public static final ChannelOption<Boolean> TCP_CORK = valueOf(EpollChannelOption.class, "TCP_CORK");
/* 12:26 */   public static final ChannelOption<Long> TCP_NOTSENT_LOWAT = valueOf(EpollChannelOption.class, "TCP_NOTSENT_LOWAT");
/* 13:27 */   public static final ChannelOption<Integer> TCP_KEEPIDLE = valueOf(EpollChannelOption.class, "TCP_KEEPIDLE");
/* 14:28 */   public static final ChannelOption<Integer> TCP_KEEPINTVL = valueOf(EpollChannelOption.class, "TCP_KEEPINTVL");
/* 15:29 */   public static final ChannelOption<Integer> TCP_KEEPCNT = valueOf(EpollChannelOption.class, "TCP_KEEPCNT");
/* 16:31 */   public static final ChannelOption<Integer> TCP_USER_TIMEOUT = valueOf(EpollChannelOption.class, "TCP_USER_TIMEOUT");
/* 17:32 */   public static final ChannelOption<Boolean> IP_FREEBIND = valueOf("IP_FREEBIND");
/* 18:33 */   public static final ChannelOption<Boolean> IP_TRANSPARENT = valueOf("IP_TRANSPARENT");
/* 19:34 */   public static final ChannelOption<Integer> TCP_FASTOPEN = valueOf(EpollChannelOption.class, "TCP_FASTOPEN");
/* 20:36 */   public static final ChannelOption<Boolean> TCP_FASTOPEN_CONNECT = valueOf(EpollChannelOption.class, "TCP_FASTOPEN_CONNECT");
/* 21:38 */   public static final ChannelOption<Integer> TCP_DEFER_ACCEPT = ChannelOption.valueOf(EpollChannelOption.class, "TCP_DEFER_ACCEPT");
/* 22:39 */   public static final ChannelOption<Boolean> TCP_QUICKACK = valueOf(EpollChannelOption.class, "TCP_QUICKACK");
/* 23:42 */   public static final ChannelOption<EpollMode> EPOLL_MODE = ChannelOption.valueOf(EpollChannelOption.class, "EPOLL_MODE");
/* 24:44 */   public static final ChannelOption<Map<InetAddress, byte[]>> TCP_MD5SIG = valueOf("TCP_MD5SIG");
/* 25:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollChannelOption
 * JD-Core Version:    0.7.0.1
 */