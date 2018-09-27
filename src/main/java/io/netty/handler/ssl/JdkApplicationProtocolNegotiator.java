/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBufAllocator;
/*  4:   */ import java.util.List;
/*  5:   */ import java.util.Set;
/*  6:   */ import javax.net.ssl.SSLEngine;
/*  7:   */ 
/*  8:   */ @Deprecated
/*  9:   */ public abstract interface JdkApplicationProtocolNegotiator
/* 10:   */   extends ApplicationProtocolNegotiator
/* 11:   */ {
/* 12:   */   public abstract SslEngineWrapperFactory wrapperFactory();
/* 13:   */   
/* 14:   */   public abstract ProtocolSelectorFactory protocolSelectorFactory();
/* 15:   */   
/* 16:   */   public abstract ProtocolSelectionListenerFactory protocolListenerFactory();
/* 17:   */   
/* 18:   */   public static abstract interface ProtocolSelectionListenerFactory
/* 19:   */   {
/* 20:   */     public abstract JdkApplicationProtocolNegotiator.ProtocolSelectionListener newListener(SSLEngine paramSSLEngine, List<String> paramList);
/* 21:   */   }
/* 22:   */   
/* 23:   */   public static abstract interface ProtocolSelectorFactory
/* 24:   */   {
/* 25:   */     public abstract JdkApplicationProtocolNegotiator.ProtocolSelector newSelector(SSLEngine paramSSLEngine, Set<String> paramSet);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public static abstract interface ProtocolSelectionListener
/* 29:   */   {
/* 30:   */     public abstract void unsupported();
/* 31:   */     
/* 32:   */     public abstract void selected(String paramString)
/* 33:   */       throws Exception;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public static abstract interface ProtocolSelector
/* 37:   */   {
/* 38:   */     public abstract void unsupported();
/* 39:   */     
/* 40:   */     public abstract String select(List<String> paramList)
/* 41:   */       throws Exception;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public static abstract class AllocatorAwareSslEngineWrapperFactory
/* 45:   */     implements JdkApplicationProtocolNegotiator.SslEngineWrapperFactory
/* 46:   */   {
/* 47:   */     public final SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer)
/* 48:   */     {
/* 49:54 */       return wrapSslEngine(engine, ByteBufAllocator.DEFAULT, applicationNegotiator, isServer);
/* 50:   */     }
/* 51:   */     
/* 52:   */     abstract SSLEngine wrapSslEngine(SSLEngine paramSSLEngine, ByteBufAllocator paramByteBufAllocator, JdkApplicationProtocolNegotiator paramJdkApplicationProtocolNegotiator, boolean paramBoolean);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public static abstract interface SslEngineWrapperFactory
/* 56:   */   {
/* 57:   */     public abstract SSLEngine wrapSslEngine(SSLEngine paramSSLEngine, JdkApplicationProtocolNegotiator paramJdkApplicationProtocolNegotiator, boolean paramBoolean);
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */