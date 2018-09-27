/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import java.util.List;
/*  5:   */ 
/*  6:   */ @Deprecated
/*  7:   */ public final class OpenSslNpnApplicationProtocolNegotiator
/*  8:   */   implements OpenSslApplicationProtocolNegotiator
/*  9:   */ {
/* 10:   */   private final List<String> protocols;
/* 11:   */   
/* 12:   */   public OpenSslNpnApplicationProtocolNegotiator(Iterable<String> protocols)
/* 13:   */   {
/* 14:33 */     this.protocols = ((List)ObjectUtil.checkNotNull(ApplicationProtocolUtil.toList(protocols), "protocols"));
/* 15:   */   }
/* 16:   */   
/* 17:   */   public OpenSslNpnApplicationProtocolNegotiator(String... protocols)
/* 18:   */   {
/* 19:37 */     this.protocols = ((List)ObjectUtil.checkNotNull(ApplicationProtocolUtil.toList(protocols), "protocols"));
/* 20:   */   }
/* 21:   */   
/* 22:   */   public ApplicationProtocolConfig.Protocol protocol()
/* 23:   */   {
/* 24:42 */     return ApplicationProtocolConfig.Protocol.NPN;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public List<String> protocols()
/* 28:   */   {
/* 29:47 */     return this.protocols;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior()
/* 33:   */   {
/* 34:52 */     return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior()
/* 38:   */   {
/* 39:57 */     return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslNpnApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */