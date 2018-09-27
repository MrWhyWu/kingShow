/*  1:   */ package io.netty.channel.group;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.ChannelException;
/*  5:   */ import java.util.Collection;
/*  6:   */ import java.util.Collections;
/*  7:   */ import java.util.Iterator;
/*  8:   */ import java.util.Map.Entry;
/*  9:   */ 
/* 10:   */ public class ChannelGroupException
/* 11:   */   extends ChannelException
/* 12:   */   implements Iterable<Map.Entry<Channel, Throwable>>
/* 13:   */ {
/* 14:   */   private static final long serialVersionUID = -4093064295562629453L;
/* 15:   */   private final Collection<Map.Entry<Channel, Throwable>> failed;
/* 16:   */   
/* 17:   */   public ChannelGroupException(Collection<Map.Entry<Channel, Throwable>> causes)
/* 18:   */   {
/* 19:35 */     if (causes == null) {
/* 20:36 */       throw new NullPointerException("causes");
/* 21:   */     }
/* 22:38 */     if (causes.isEmpty()) {
/* 23:39 */       throw new IllegalArgumentException("causes must be non empty");
/* 24:   */     }
/* 25:41 */     this.failed = Collections.unmodifiableCollection(causes);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public Iterator<Map.Entry<Channel, Throwable>> iterator()
/* 29:   */   {
/* 30:50 */     return this.failed.iterator();
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.group.ChannelGroupException
 * JD-Core Version:    0.7.0.1
 */