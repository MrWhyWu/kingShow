/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.io.File;
/*   4:    */ import java.security.PrivateKey;
/*   5:    */ import java.security.Provider;
/*   6:    */ import java.security.cert.X509Certificate;
/*   7:    */ import javax.net.ssl.KeyManagerFactory;
/*   8:    */ import javax.net.ssl.SSLContext;
/*   9:    */ import javax.net.ssl.SSLException;
/*  10:    */ import javax.net.ssl.SSLSessionContext;
/*  11:    */ import javax.net.ssl.TrustManagerFactory;
/*  12:    */ 
/*  13:    */ @Deprecated
/*  14:    */ public final class JdkSslClientContext
/*  15:    */   extends JdkSslContext
/*  16:    */ {
/*  17:    */   @Deprecated
/*  18:    */   public JdkSslClientContext()
/*  19:    */     throws SSLException
/*  20:    */   {
/*  21: 47 */     this(null, null);
/*  22:    */   }
/*  23:    */   
/*  24:    */   @Deprecated
/*  25:    */   public JdkSslClientContext(File certChainFile)
/*  26:    */     throws SSLException
/*  27:    */   {
/*  28: 59 */     this(certChainFile, null);
/*  29:    */   }
/*  30:    */   
/*  31:    */   @Deprecated
/*  32:    */   public JdkSslClientContext(TrustManagerFactory trustManagerFactory)
/*  33:    */     throws SSLException
/*  34:    */   {
/*  35: 72 */     this(null, trustManagerFactory);
/*  36:    */   }
/*  37:    */   
/*  38:    */   @Deprecated
/*  39:    */   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory)
/*  40:    */     throws SSLException
/*  41:    */   {
/*  42: 87 */     this(certChainFile, trustManagerFactory, null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, 0L, 0L);
/*  43:    */   }
/*  44:    */   
/*  45:    */   @Deprecated
/*  46:    */   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  47:    */     throws SSLException
/*  48:    */   {
/*  49:114 */     this(certChainFile, trustManagerFactory, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
/*  50:115 */       toNegotiator(toApplicationProtocolConfig(nextProtocols), false), sessionCacheSize, sessionTimeout);
/*  51:    */   }
/*  52:    */   
/*  53:    */   @Deprecated
/*  54:    */   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  55:    */     throws SSLException
/*  56:    */   {
/*  57:141 */     this(certChainFile, trustManagerFactory, ciphers, cipherFilter, 
/*  58:142 */       toNegotiator(apn, false), sessionCacheSize, sessionTimeout);
/*  59:    */   }
/*  60:    */   
/*  61:    */   @Deprecated
/*  62:    */   public JdkSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  63:    */     throws SSLException
/*  64:    */   {
/*  65:168 */     this(null, certChainFile, trustManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  66:    */   }
/*  67:    */   
/*  68:    */   JdkSslClientContext(Provider provider, File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  69:    */     throws SSLException
/*  70:    */   {
/*  71:175 */     super(newSSLContext(provider, toX509CertificatesInternal(trustCertCollectionFile), trustManagerFactory, null, null, null, null, sessionCacheSize, sessionTimeout), true, ciphers, cipherFilter, apn, ClientAuth.NONE, null, false);
/*  72:    */   }
/*  73:    */   
/*  74:    */   @Deprecated
/*  75:    */   public JdkSslClientContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  76:    */     throws SSLException
/*  77:    */   {
/*  78:217 */     this(trustCertCollectionFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, 
/*  79:218 */       toNegotiator(apn, false), sessionCacheSize, sessionTimeout);
/*  80:    */   }
/*  81:    */   
/*  82:    */   @Deprecated
/*  83:    */   public JdkSslClientContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  84:    */     throws SSLException
/*  85:    */   {
/*  86:257 */     super(newSSLContext(null, toX509CertificatesInternal(trustCertCollectionFile), trustManagerFactory, 
/*  87:    */     
/*  88:259 */       toX509CertificatesInternal(keyCertChainFile), toPrivateKeyInternal(keyFile, keyPassword), keyPassword, keyManagerFactory, sessionCacheSize, sessionTimeout), true, ciphers, cipherFilter, apn, ClientAuth.NONE, null, false);
/*  89:    */   }
/*  90:    */   
/*  91:    */   JdkSslClientContext(Provider sslContextProvider, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, String[] protocols, long sessionCacheSize, long sessionTimeout)
/*  92:    */     throws SSLException
/*  93:    */   {
/*  94:270 */     super(newSSLContext(sslContextProvider, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, sessionCacheSize, sessionTimeout), true, ciphers, cipherFilter, 
/*  95:    */     
/*  96:272 */       toNegotiator(apn, false), ClientAuth.NONE, protocols, false);
/*  97:    */   }
/*  98:    */   
/*  99:    */   private static SSLContext newSSLContext(Provider sslContextProvider, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, long sessionCacheSize, long sessionTimeout)
/* 100:    */     throws SSLException
/* 101:    */   {
/* 102:    */     try
/* 103:    */     {
/* 104:281 */       if (trustCertCollection != null) {
/* 105:282 */         trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
/* 106:    */       }
/* 107:284 */       if (keyCertChain != null) {
/* 108:285 */         keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory);
/* 109:    */       }
/* 110:288 */       SSLContext ctx = sslContextProvider == null ? SSLContext.getInstance("TLS") : SSLContext.getInstance("TLS", sslContextProvider);
/* 111:289 */       ctx.init(keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(), trustManagerFactory == null ? null : trustManagerFactory
/* 112:290 */         .getTrustManagers(), null);
/* 113:    */       
/* 114:    */ 
/* 115:293 */       SSLSessionContext sessCtx = ctx.getClientSessionContext();
/* 116:294 */       if (sessionCacheSize > 0L) {
/* 117:295 */         sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
/* 118:    */       }
/* 119:297 */       if (sessionTimeout > 0L) {
/* 120:298 */         sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
/* 121:    */       }
/* 122:300 */       return ctx;
/* 123:    */     }
/* 124:    */     catch (Exception e)
/* 125:    */     {
/* 126:302 */       if ((e instanceof SSLException)) {
/* 127:303 */         throw ((SSLException)e);
/* 128:    */       }
/* 129:305 */       throw new SSLException("failed to initialize the client-side SSL context", e);
/* 130:    */     }
/* 131:    */   }
/* 132:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkSslClientContext
 * JD-Core Version:    0.7.0.1
 */