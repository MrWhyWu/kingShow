/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.io.File;
/*   4:    */ import java.security.PrivateKey;
/*   5:    */ import java.security.cert.X509Certificate;
/*   6:    */ import javax.net.ssl.KeyManagerFactory;
/*   7:    */ import javax.net.ssl.SSLException;
/*   8:    */ import javax.net.ssl.TrustManagerFactory;
/*   9:    */ 
/*  10:    */ public final class OpenSslClientContext
/*  11:    */   extends OpenSslContext
/*  12:    */ {
/*  13:    */   private final OpenSslSessionContext sessionContext;
/*  14:    */   
/*  15:    */   @Deprecated
/*  16:    */   public OpenSslClientContext()
/*  17:    */     throws SSLException
/*  18:    */   {
/*  19: 45 */     this((File)null, null, null, null, null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
/*  20:    */   }
/*  21:    */   
/*  22:    */   @Deprecated
/*  23:    */   public OpenSslClientContext(File certChainFile)
/*  24:    */     throws SSLException
/*  25:    */   {
/*  26: 57 */     this(certChainFile, null);
/*  27:    */   }
/*  28:    */   
/*  29:    */   @Deprecated
/*  30:    */   public OpenSslClientContext(TrustManagerFactory trustManagerFactory)
/*  31:    */     throws SSLException
/*  32:    */   {
/*  33: 70 */     this(null, trustManagerFactory);
/*  34:    */   }
/*  35:    */   
/*  36:    */   @Deprecated
/*  37:    */   public OpenSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory)
/*  38:    */     throws SSLException
/*  39:    */   {
/*  40: 85 */     this(certChainFile, trustManagerFactory, null, null, null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
/*  41:    */   }
/*  42:    */   
/*  43:    */   @Deprecated
/*  44:    */   public OpenSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  45:    */     throws SSLException
/*  46:    */   {
/*  47:109 */     this(certChainFile, trustManagerFactory, null, null, null, null, ciphers, IdentityCipherSuiteFilter.INSTANCE, apn, sessionCacheSize, sessionTimeout);
/*  48:    */   }
/*  49:    */   
/*  50:    */   @Deprecated
/*  51:    */   public OpenSslClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  52:    */     throws SSLException
/*  53:    */   {
/*  54:134 */     this(certChainFile, trustManagerFactory, null, null, null, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  55:    */   }
/*  56:    */   
/*  57:    */   @Deprecated
/*  58:    */   public OpenSslClientContext(File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  59:    */     throws SSLException
/*  60:    */   {
/*  61:176 */     this(toX509CertificatesInternal(trustCertCollectionFile), trustManagerFactory, 
/*  62:177 */       toX509CertificatesInternal(keyCertChainFile), toPrivateKeyInternal(keyFile, keyPassword), keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, null, sessionCacheSize, sessionTimeout, false);
/*  63:    */   }
/*  64:    */   
/*  65:    */   OpenSslClientContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, String[] protocols, long sessionCacheSize, long sessionTimeout, boolean enableOcsp)
/*  66:    */     throws SSLException
/*  67:    */   {
/*  68:188 */     super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 0, keyCertChain, ClientAuth.NONE, protocols, false, enableOcsp);
/*  69:    */     
/*  70:190 */     boolean success = false;
/*  71:    */     try
/*  72:    */     {
/*  73:192 */       this.sessionContext = ReferenceCountedOpenSslClientContext.newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory);
/*  74:    */       
/*  75:194 */       success = true;
/*  76:    */     }
/*  77:    */     finally
/*  78:    */     {
/*  79:196 */       if (!success) {
/*  80:197 */         release();
/*  81:    */       }
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   public OpenSslSessionContext sessionContext()
/*  86:    */   {
/*  87:204 */     return this.sessionContext;
/*  88:    */   }
/*  89:    */   
/*  90:    */   OpenSslKeyMaterialManager keyMaterialManager()
/*  91:    */   {
/*  92:209 */     return null;
/*  93:    */   }
/*  94:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslClientContext
 * JD-Core Version:    0.7.0.1
 */