/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import java.util.Collections;
/*  4:   */ import java.util.List;
/*  5:   */ import javax.net.ssl.SSLEngine;
/*  6:   */ 
/*  7:   */ final class JdkDefaultApplicationProtocolNegotiator
/*  8:   */   implements JdkApplicationProtocolNegotiator
/*  9:   */ {
/* 10:28 */   public static final JdkDefaultApplicationProtocolNegotiator INSTANCE = new JdkDefaultApplicationProtocolNegotiator();
/* 11:30 */   private static final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory DEFAULT_SSL_ENGINE_WRAPPER_FACTORY = new JdkApplicationProtocolNegotiator.SslEngineWrapperFactory()
/* 12:   */   {
/* 13:   */     public SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer)
/* 14:   */     {
/* 15:34 */       return engine;
/* 16:   */     }
/* 17:   */   };
/* 18:   */   
/* 19:   */   public JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory()
/* 20:   */   {
/* 21:43 */     return DEFAULT_SSL_ENGINE_WRAPPER_FACTORY;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public JdkApplicationProtocolNegotiator.ProtocolSelectorFactory protocolSelectorFactory()
/* 25:   */   {
/* 26:48 */     throw new UnsupportedOperationException("Application protocol negotiation unsupported");
/* 27:   */   }
/* 28:   */   
/* 29:   */   public JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory protocolListenerFactory()
/* 30:   */   {
/* 31:53 */     throw new UnsupportedOperationException("Application protocol negotiation unsupported");
/* 32:   */   }
/* 33:   */   
/* 34:   */   public List<String> protocols()
/* 35:   */   {
/* 36:58 */     return Collections.emptyList();
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkDefaultApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */