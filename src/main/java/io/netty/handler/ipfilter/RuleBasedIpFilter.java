/*  1:   */ package io.netty.handler.ipfilter;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import java.net.InetSocketAddress;
/*  6:   */ 
/*  7:   */ @ChannelHandler.Sharable
/*  8:   */ public class RuleBasedIpFilter
/*  9:   */   extends AbstractRemoteAddressFilter<InetSocketAddress>
/* 10:   */ {
/* 11:   */   private final IpFilterRule[] rules;
/* 12:   */   
/* 13:   */   public RuleBasedIpFilter(IpFilterRule... rules)
/* 14:   */   {
/* 15:39 */     if (rules == null) {
/* 16:40 */       throw new NullPointerException("rules");
/* 17:   */     }
/* 18:43 */     this.rules = rules;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress)
/* 22:   */     throws Exception
/* 23:   */   {
/* 24:48 */     for (IpFilterRule rule : this.rules)
/* 25:   */     {
/* 26:49 */       if (rule == null) {
/* 27:   */         break;
/* 28:   */       }
/* 29:53 */       if (rule.matches(remoteAddress)) {
/* 30:54 */         return rule.ruleType() == IpFilterRuleType.ACCEPT;
/* 31:   */       }
/* 32:   */     }
/* 33:58 */     return true;
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ipfilter.RuleBasedIpFilter
 * JD-Core Version:    0.7.0.1
 */