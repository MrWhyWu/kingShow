/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.io.File;
/*   4:    */ import java.security.PrivateKey;
/*   5:    */ import java.security.cert.X509Certificate;
/*   6:    */ import javax.net.ssl.KeyManagerFactory;
/*   7:    */ import javax.net.ssl.SSLException;
/*   8:    */ import javax.net.ssl.TrustManagerFactory;
/*   9:    */ 
/*  10:    */ public final class OpenSslServerContext
/*  11:    */   extends OpenSslContext
/*  12:    */ {
/*  13:    */   private final OpenSslServerSessionContext sessionContext;
/*  14:    */   private final OpenSslKeyMaterialManager keyMaterialManager;
/*  15:    */   
/*  16:    */   @Deprecated
/*  17:    */   public OpenSslServerContext(File certChainFile, File keyFile)
/*  18:    */     throws SSLException
/*  19:    */   {
/*  20: 51 */     this(certChainFile, keyFile, null);
/*  21:    */   }
/*  22:    */   
/*  23:    */   @Deprecated
/*  24:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword)
/*  25:    */     throws SSLException
/*  26:    */   {
/*  27: 65 */     this(certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE, ApplicationProtocolConfig.DISABLED, 0L, 0L);
/*  28:    */   }
/*  29:    */   
/*  30:    */   @Deprecated
/*  31:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  32:    */     throws SSLException
/*  33:    */   {
/*  34: 90 */     this(certChainFile, keyFile, keyPassword, ciphers, IdentityCipherSuiteFilter.INSTANCE, apn, sessionCacheSize, sessionTimeout);
/*  35:    */   }
/*  36:    */   
/*  37:    */   @Deprecated
/*  38:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  39:    */     throws SSLException
/*  40:    */   {
/*  41:116 */     this(certChainFile, keyFile, keyPassword, ciphers, 
/*  42:117 */       toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
/*  43:    */   }
/*  44:    */   
/*  45:    */   @Deprecated
/*  46:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, ApplicationProtocolConfig config, long sessionCacheSize, long sessionTimeout)
/*  47:    */     throws SSLException
/*  48:    */   {
/*  49:141 */     this(certChainFile, keyFile, keyPassword, trustManagerFactory, ciphers, 
/*  50:142 */       toNegotiator(config), sessionCacheSize, sessionTimeout);
/*  51:    */   }
/*  52:    */   
/*  53:    */   @Deprecated
/*  54:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  55:    */     throws SSLException
/*  56:    */   {
/*  57:166 */     this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, null, apn, sessionCacheSize, sessionTimeout);
/*  58:    */   }
/*  59:    */   
/*  60:    */   @Deprecated
/*  61:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  62:    */     throws SSLException
/*  63:    */   {
/*  64:192 */     this(null, null, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  65:    */   }
/*  66:    */   
/*  67:    */   @Deprecated
/*  68:    */   public OpenSslServerContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config, long sessionCacheSize, long sessionTimeout)
/*  69:    */     throws SSLException
/*  70:    */   {
/*  71:231 */     this(trustCertCollectionFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, 
/*  72:232 */       toNegotiator(config), sessionCacheSize, sessionTimeout);
/*  73:    */   }
/*  74:    */   
/*  75:    */   @Deprecated
/*  76:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig config, long sessionCacheSize, long sessionTimeout)
/*  77:    */     throws SSLException
/*  78:    */   {
/*  79:257 */     this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, 
/*  80:258 */       toNegotiator(config), sessionCacheSize, sessionTimeout);
/*  81:    */   }
/*  82:    */   
/*  83:    */   @Deprecated
/*  84:    */   public OpenSslServerContext(File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  85:    */     throws SSLException
/*  86:    */   {
/*  87:283 */     this(null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  88:    */   }
/*  89:    */   
/*  90:    */   @Deprecated
/*  91:    */   public OpenSslServerContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout)
/*  92:    */     throws SSLException
/*  93:    */   {
/*  94:323 */     this(toX509CertificatesInternal(trustCertCollectionFile), trustManagerFactory, 
/*  95:324 */       toX509CertificatesInternal(keyCertChainFile), toPrivateKeyInternal(keyFile, keyPassword), keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, ClientAuth.NONE, null, false, false);
/*  96:    */   }
/*  97:    */   
/*  98:    */   OpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/*  99:    */     throws SSLException
/* 100:    */   {
/* 101:335 */     this(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, 
/* 102:336 */       toNegotiator(apn), sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
/* 103:    */   }
/* 104:    */   
/* 105:    */   private OpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/* 106:    */     throws SSLException
/* 107:    */   {
/* 108:347 */     super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 1, keyCertChain, clientAuth, protocols, startTls, enableOcsp);
/* 109:    */     
/* 110:    */ 
/* 111:350 */     boolean success = false;
/* 112:    */     try
/* 113:    */     {
/* 114:352 */       ReferenceCountedOpenSslServerContext.ServerContext context = ReferenceCountedOpenSslServerContext.newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory);
/* 115:    */       
/* 116:354 */       this.sessionContext = context.sessionContext;
/* 117:355 */       this.keyMaterialManager = context.keyMaterialManager;
/* 118:356 */       success = true;
/* 119:    */     }
/* 120:    */     finally
/* 121:    */     {
/* 122:358 */       if (!success) {
/* 123:359 */         release();
/* 124:    */       }
/* 125:    */     }
/* 126:    */   }
/* 127:    */   
/* 128:    */   public OpenSslServerSessionContext sessionContext()
/* 129:    */   {
/* 130:366 */     return this.sessionContext;
/* 131:    */   }
/* 132:    */   
/* 133:    */   OpenSslKeyMaterialManager keyMaterialManager()
/* 134:    */   {
/* 135:371 */     return this.keyMaterialManager;
/* 136:    */   }
/* 137:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslServerContext
 * JD-Core Version:    0.7.0.1
 */