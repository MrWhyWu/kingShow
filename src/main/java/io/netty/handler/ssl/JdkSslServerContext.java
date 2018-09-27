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
/*  14:    */ public final class JdkSslServerContext
/*  15:    */   extends JdkSslContext
/*  16:    */ {
/*  17:    */   @Deprecated
/*  18:    */   public JdkSslServerContext(File certChainFile, File keyFile)
/*  19:    */     throws SSLException
/*  20:    */   {
/*  21: 50 */     this(certChainFile, keyFile, null);
/*  22:    */   }
/*  23:    */   
/*  24:    */   @Deprecated
/*  25:    */   public JdkSslServerContext(File certChainFile, File keyFile, String keyPassword)
/*  26:    */     throws SSLException
/*  27:    */   {
/*  28: 64 */     this(certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, 0L, 0L);
/*  29:    */   }
/*  30:    */   
/*  31:    */   @Deprecated
/*  32:    */   public JdkSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  33:    */     throws SSLException
/*  34:    */   {
/*  35: 90 */     this(certChainFile, keyFile, keyPassword, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
/*  36: 91 */       toNegotiator(toApplicationProtocolConfig(nextProtocols), true), sessionCacheSize, sessionTimeout);
/*  37:    */   }
/*  38:    */   
/*  39:    */   @Deprecated
/*  40:    */   public JdkSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  41:    */     throws SSLException
/*  42:    */   {
/*  43:116 */     this(certChainFile, keyFile, keyPassword, ciphers, cipherFilter, 
/*  44:117 */       toNegotiator(apn, true), sessionCacheSize, sessionTimeout);
/*  45:    */   }
/*  46:    */   
/*  47:    */   @Deprecated
/*  48:    */   public JdkSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  49:    */     throws SSLException
/*  50:    */   {
/*  51:142 */     this(null, certChainFile, keyFile, keyPassword, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  52:    */   }
/*  53:    */   
/*  54:    */   JdkSslServerContext(Provider provider, File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  55:    */     throws SSLException
/*  56:    */   {
/*  57:149 */     super(newSSLContext(provider, null, null, 
/*  58:150 */       toX509CertificatesInternal(certChainFile), toPrivateKeyInternal(keyFile, keyPassword), keyPassword, null, sessionCacheSize, sessionTimeout), false, ciphers, cipherFilter, apn, ClientAuth.NONE, null, false);
/*  59:    */   }
/*  60:    */   
/*  61:    */   @Deprecated
/*  62:    */   public JdkSslServerContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  63:    */     throws SSLException
/*  64:    */   {
/*  65:188 */     this(trustCertCollectionFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, 
/*  66:189 */       toNegotiator(apn, true), sessionCacheSize, sessionTimeout);
/*  67:    */   }
/*  68:    */   
/*  69:    */   @Deprecated
/*  70:    */   public JdkSslServerContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  71:    */     throws SSLException
/*  72:    */   {
/*  73:225 */     super(newSSLContext(null, toX509CertificatesInternal(trustCertCollectionFile), trustManagerFactory, 
/*  74:226 */       toX509CertificatesInternal(keyCertChainFile), toPrivateKeyInternal(keyFile, keyPassword), keyPassword, keyManagerFactory, sessionCacheSize, sessionTimeout), false, ciphers, cipherFilter, apn, ClientAuth.NONE, null, false);
/*  75:    */   }
/*  76:    */   
/*  77:    */   JdkSslServerContext(Provider provider, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls)
/*  78:    */     throws SSLException
/*  79:    */   {
/*  80:237 */     super(newSSLContext(provider, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, sessionCacheSize, sessionTimeout), false, ciphers, cipherFilter, 
/*  81:    */     
/*  82:239 */       toNegotiator(apn, true), clientAuth, protocols, startTls);
/*  83:    */   }
/*  84:    */   
/*  85:    */   private static SSLContext newSSLContext(Provider sslContextProvider, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, long sessionCacheSize, long sessionTimeout)
/*  86:    */     throws SSLException
/*  87:    */   {
/*  88:247 */     if ((key == null) && (keyManagerFactory == null)) {
/*  89:248 */       throw new NullPointerException("key, keyManagerFactory");
/*  90:    */     }
/*  91:    */     try
/*  92:    */     {
/*  93:252 */       if (trustCertCollection != null) {
/*  94:253 */         trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
/*  95:    */       }
/*  96:255 */       if (key != null) {
/*  97:256 */         keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory);
/*  98:    */       }
/*  99:261 */       SSLContext ctx = sslContextProvider == null ? SSLContext.getInstance("TLS") : SSLContext.getInstance("TLS", sslContextProvider);
/* 100:262 */       ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory == null ? null : trustManagerFactory
/* 101:263 */         .getTrustManagers(), null);
/* 102:    */       
/* 103:    */ 
/* 104:266 */       SSLSessionContext sessCtx = ctx.getServerSessionContext();
/* 105:267 */       if (sessionCacheSize > 0L) {
/* 106:268 */         sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
/* 107:    */       }
/* 108:270 */       if (sessionTimeout > 0L) {
/* 109:271 */         sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
/* 110:    */       }
/* 111:273 */       return ctx;
/* 112:    */     }
/* 113:    */     catch (Exception e)
/* 114:    */     {
/* 115:275 */       if ((e instanceof SSLException)) {
/* 116:276 */         throw ((SSLException)e);
/* 117:    */       }
/* 118:278 */       throw new SSLException("failed to initialize the server-side SSL context", e);
/* 119:    */     }
/* 120:    */   }
/* 121:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkSslServerContext
 * JD-Core Version:    0.7.0.1
 */