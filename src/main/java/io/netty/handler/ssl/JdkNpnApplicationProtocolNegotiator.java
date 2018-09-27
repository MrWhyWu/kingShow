/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import javax.net.ssl.SSLEngine;
/*   4:    */ 
/*   5:    */ @Deprecated
/*   6:    */ public final class JdkNpnApplicationProtocolNegotiator
/*   7:    */   extends JdkBaseApplicationProtocolNegotiator
/*   8:    */ {
/*   9: 27 */   private static final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory NPN_WRAPPER = new JdkApplicationProtocolNegotiator.SslEngineWrapperFactory()
/*  10:    */   {
/*  11:    */     public SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer)
/*  12:    */     {
/*  13: 38 */       return new JettyNpnSslEngine(engine, applicationNegotiator, isServer);
/*  14:    */     }
/*  15:    */   };
/*  16:    */   
/*  17:    */   public JdkNpnApplicationProtocolNegotiator(Iterable<String> protocols)
/*  18:    */   {
/*  19: 47 */     this(false, protocols);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public JdkNpnApplicationProtocolNegotiator(String... protocols)
/*  23:    */   {
/*  24: 55 */     this(false, protocols);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public JdkNpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, Iterable<String> protocols)
/*  28:    */   {
/*  29: 64 */     this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public JdkNpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, String... protocols)
/*  33:    */   {
/*  34: 73 */     this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public JdkNpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols, boolean serverFailIfNoCommonProtocols, Iterable<String> protocols)
/*  38:    */   {
/*  39: 84 */     this(clientFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY, serverFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY, protocols);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public JdkNpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols, boolean serverFailIfNoCommonProtocols, String... protocols)
/*  43:    */   {
/*  44: 97 */     this(clientFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY, serverFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY, protocols);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public JdkNpnApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols)
/*  48:    */   {
/*  49:110 */     super(NPN_WRAPPER, selectorFactory, listenerFactory, protocols);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public JdkNpnApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, String... protocols)
/*  53:    */   {
/*  54:121 */     super(NPN_WRAPPER, selectorFactory, listenerFactory, protocols);
/*  55:    */   }
/*  56:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator
 * JD-Core Version:    0.7.0.1
 */