/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import java.util.List;
/*  5:   */ 
/*  6:   */ @Deprecated
/*  7:   */ public final class OpenSslDefaultApplicationProtocolNegotiator
/*  8:   */   implements OpenSslApplicationProtocolNegotiator
/*  9:   */ {
/* 10:   */   private final ApplicationProtocolConfig config;
/* 11:   */   
/* 12:   */   public OpenSslDefaultApplicationProtocolNegotiator(ApplicationProtocolConfig config)
/* 13:   */   {
/* 14:31 */     this.config = ((ApplicationProtocolConfig)ObjectUtil.checkNotNull(config, "config"));
/* 15:   */   }
/* 16:   */   
/* 17:   */   public List<String> protocols()
/* 18:   */   {
/* 19:36 */     return this.config.supportedProtocols();
/* 20:   */   }
/* 21:   */   
/* 22:   */   public ApplicationProtocolConfig.Protocol protocol()
/* 23:   */   {
/* 24:41 */     return this.config.protocol();
/* 25:   */   }
/* 26:   */   
/* 27:   */   public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior()
/* 28:   */   {
/* 29:46 */     return this.config.selectorFailureBehavior();
/* 30:   */   }
/* 31:   */   
/* 32:   */   public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior()
/* 33:   */   {
/* 34:51 */     return this.config.selectedListenerFailureBehavior();
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslDefaultApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */