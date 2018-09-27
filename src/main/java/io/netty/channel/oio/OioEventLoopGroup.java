/*  1:   */ package io.netty.channel.oio;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ThreadPerChannelEventLoopGroup;
/*  4:   */ import java.util.concurrent.Executor;
/*  5:   */ import java.util.concurrent.Executors;
/*  6:   */ import java.util.concurrent.ThreadFactory;
/*  7:   */ 
/*  8:   */ public class OioEventLoopGroup
/*  9:   */   extends ThreadPerChannelEventLoopGroup
/* 10:   */ {
/* 11:   */   public OioEventLoopGroup()
/* 12:   */   {
/* 13:40 */     this(0);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public OioEventLoopGroup(int maxChannels)
/* 17:   */   {
/* 18:53 */     this(maxChannels, Executors.defaultThreadFactory());
/* 19:   */   }
/* 20:   */   
/* 21:   */   public OioEventLoopGroup(int maxChannels, Executor executor)
/* 22:   */   {
/* 23:68 */     super(maxChannels, executor, new Object[0]);
/* 24:   */   }
/* 25:   */   
/* 26:   */   public OioEventLoopGroup(int maxChannels, ThreadFactory threadFactory)
/* 27:   */   {
/* 28:83 */     super(maxChannels, threadFactory, new Object[0]);
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.oio.OioEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */