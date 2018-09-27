/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import javax.net.ssl.SSLEngine;
/*   6:    */ 
/*   7:    */ @Deprecated
/*   8:    */ public final class JdkAlpnApplicationProtocolNegotiator
/*   9:    */   extends JdkBaseApplicationProtocolNegotiator
/*  10:    */ {
/*  11: 30 */   private static final boolean AVAILABLE = (Conscrypt.isAvailable()) || 
/*  12: 31 */     (jdkAlpnSupported()) || 
/*  13: 32 */     (JettyAlpnSslEngine.isAvailable());
/*  14: 34 */   private static final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory ALPN_WRAPPER = AVAILABLE ? new AlpnWrapper(null) : new FailureWrapper(null);
/*  15:    */   
/*  16:    */   public JdkAlpnApplicationProtocolNegotiator(Iterable<String> protocols)
/*  17:    */   {
/*  18: 41 */     this(false, protocols);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public JdkAlpnApplicationProtocolNegotiator(String... protocols)
/*  22:    */   {
/*  23: 49 */     this(false, protocols);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public JdkAlpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, Iterable<String> protocols)
/*  27:    */   {
/*  28: 58 */     this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public JdkAlpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, String... protocols)
/*  32:    */   {
/*  33: 67 */     this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public JdkAlpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols, boolean serverFailIfNoCommonProtocols, Iterable<String> protocols)
/*  37:    */   {
/*  38: 78 */     this(serverFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY, clientFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY, protocols);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkAlpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols, boolean serverFailIfNoCommonProtocols, String... protocols)
/*  42:    */   {
/*  43: 91 */     this(serverFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY, clientFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY, protocols);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public JdkAlpnApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols)
/*  47:    */   {
/*  48:104 */     super(ALPN_WRAPPER, selectorFactory, listenerFactory, protocols);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public JdkAlpnApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, String... protocols)
/*  52:    */   {
/*  53:115 */     super(ALPN_WRAPPER, selectorFactory, listenerFactory, protocols);
/*  54:    */   }
/*  55:    */   
/*  56:    */   private static final class FailureWrapper
/*  57:    */     extends JdkApplicationProtocolNegotiator.AllocatorAwareSslEngineWrapperFactory
/*  58:    */   {
/*  59:    */     public SSLEngine wrapSslEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer)
/*  60:    */     {
/*  61:122 */       throw new RuntimeException("ALPN unsupported. Is your classpath configured correctly? For Conscrypt, add the appropriate Conscrypt JAR to classpath and set the security provider. For Jetty-ALPN, see http://www.eclipse.org/jetty/documentation/current/alpn-chapter.html#alpn-starting");
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   private static final class AlpnWrapper
/*  66:    */     extends JdkApplicationProtocolNegotiator.AllocatorAwareSslEngineWrapperFactory
/*  67:    */   {
/*  68:    */     public SSLEngine wrapSslEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer)
/*  69:    */     {
/*  70:133 */       if (Conscrypt.isEngineSupported(engine)) {
/*  71:134 */         return isServer ? ConscryptAlpnSslEngine.newServerEngine(engine, alloc, applicationNegotiator) : 
/*  72:135 */           ConscryptAlpnSslEngine.newClientEngine(engine, alloc, applicationNegotiator);
/*  73:    */       }
/*  74:137 */       if (JdkAlpnApplicationProtocolNegotiator.jdkAlpnSupported()) {
/*  75:138 */         return new Java9SslEngine(engine, applicationNegotiator, isServer);
/*  76:    */       }
/*  77:140 */       if (JettyAlpnSslEngine.isAvailable()) {
/*  78:141 */         return isServer ? JettyAlpnSslEngine.newServerEngine(engine, applicationNegotiator) : 
/*  79:142 */           JettyAlpnSslEngine.newClientEngine(engine, applicationNegotiator);
/*  80:    */       }
/*  81:144 */       throw new RuntimeException("Unable to wrap SSLEngine of type " + engine.getClass().getName());
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   static boolean jdkAlpnSupported()
/*  86:    */   {
/*  87:149 */     return (PlatformDependent.javaVersion() >= 9) && (Java9SslUtils.supportsAlpn());
/*  88:    */   }
/*  89:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkAlpnApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */