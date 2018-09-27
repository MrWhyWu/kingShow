/*  1:   */ package io.netty.channel.kqueue;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelOption;
/*  4:   */ import io.netty.channel.unix.UnixChannelOption;
/*  5:   */ 
/*  6:   */ public final class KQueueChannelOption<T>
/*  7:   */   extends UnixChannelOption<T>
/*  8:   */ {
/*  9:25 */   public static final ChannelOption<Integer> SO_SNDLOWAT = valueOf(KQueueChannelOption.class, "SO_SNDLOWAT");
/* 10:26 */   public static final ChannelOption<Boolean> TCP_NOPUSH = valueOf(KQueueChannelOption.class, "TCP_NOPUSH");
/* 11:28 */   public static final ChannelOption<AcceptFilter> SO_ACCEPTFILTER = valueOf(KQueueChannelOption.class, "SO_ACCEPTFILTER");
/* 12:34 */   public static final ChannelOption<Boolean> RCV_ALLOC_TRANSPORT_PROVIDES_GUESS = valueOf(KQueueChannelOption.class, "RCV_ALLOC_TRANSPORT_PROVIDES_GUESS");
/* 13:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueChannelOption
 * JD-Core Version:    0.7.0.1
 */