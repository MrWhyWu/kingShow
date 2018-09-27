/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.internal.tcnative.SSLContext;
/*   4:    */ import io.netty.internal.tcnative.SniHostNameMatcher;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.security.KeyStore;
/*  10:    */ import java.security.PrivateKey;
/*  11:    */ import java.security.cert.X509Certificate;
/*  12:    */ import javax.net.ssl.KeyManagerFactory;
/*  13:    */ import javax.net.ssl.SSLException;
/*  14:    */ import javax.net.ssl.TrustManagerFactory;
/*  15:    */ import javax.net.ssl.X509ExtendedKeyManager;
/*  16:    */ import javax.net.ssl.X509ExtendedTrustManager;
/*  17:    */ import javax.net.ssl.X509KeyManager;
/*  18:    */ import javax.net.ssl.X509TrustManager;
/*  19:    */ 
/*  20:    */ public final class ReferenceCountedOpenSslServerContext
/*  21:    */   extends ReferenceCountedOpenSslContext
/*  22:    */ {
/*  23: 48 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslServerContext.class);
/*  24: 49 */   private static final byte[] ID = { 110, 101, 116, 116, 121 };
/*  25:    */   private final OpenSslServerSessionContext sessionContext;
/*  26:    */   private final OpenSslKeyMaterialManager keyMaterialManager;
/*  27:    */   
/*  28:    */   ReferenceCountedOpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/*  29:    */     throws SSLException
/*  30:    */   {
/*  31: 59 */     this(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, 
/*  32: 60 */       toNegotiator(apn), sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
/*  33:    */   }
/*  34:    */   
/*  35:    */   private ReferenceCountedOpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/*  36:    */     throws SSLException
/*  37:    */   {
/*  38: 70 */     super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 1, keyCertChain, clientAuth, protocols, startTls, enableOcsp, true);
/*  39:    */     
/*  40:    */ 
/*  41: 73 */     boolean success = false;
/*  42:    */     try
/*  43:    */     {
/*  44: 75 */       ServerContext context = newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory);
/*  45:    */       
/*  46: 77 */       this.sessionContext = context.sessionContext;
/*  47: 78 */       this.keyMaterialManager = context.keyMaterialManager;
/*  48: 79 */       success = true;
/*  49:    */     }
/*  50:    */     finally
/*  51:    */     {
/*  52: 81 */       if (!success) {
/*  53: 82 */         release();
/*  54:    */       }
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   public OpenSslServerSessionContext sessionContext()
/*  59:    */   {
/*  60: 89 */     return this.sessionContext;
/*  61:    */   }
/*  62:    */   
/*  63:    */   OpenSslKeyMaterialManager keyMaterialManager()
/*  64:    */   {
/*  65: 94 */     return this.keyMaterialManager;
/*  66:    */   }
/*  67:    */   
/*  68:    */   static ServerContext newSessionContext(ReferenceCountedOpenSslContext thiz, long ctx, OpenSslEngineMap engineMap, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory)
/*  69:    */     throws SSLException
/*  70:    */   {
/*  71:108 */     ServerContext result = new ServerContext();
/*  72:    */     try
/*  73:    */     {
/*  74:110 */       SSLContext.setVerify(ctx, 0, 10);
/*  75:111 */       if (!OpenSsl.useKeyManagerFactory())
/*  76:    */       {
/*  77:112 */         if (keyManagerFactory != null) {
/*  78:113 */           throw new IllegalArgumentException("KeyManagerFactory not supported");
/*  79:    */         }
/*  80:116 */         ObjectUtil.checkNotNull(keyCertChain, "keyCertChain");
/*  81:    */         
/*  82:118 */         setKeyMaterial(ctx, keyCertChain, key, keyPassword);
/*  83:    */       }
/*  84:    */       else
/*  85:    */       {
/*  86:122 */         if (keyManagerFactory == null) {
/*  87:123 */           keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory);
/*  88:    */         }
/*  89:126 */         X509KeyManager keyManager = chooseX509KeyManager(keyManagerFactory.getKeyManagers());
/*  90:127 */         result.keyMaterialManager = (useExtendedKeyManager(keyManager) ? new OpenSslExtendedKeyMaterialManager((X509ExtendedKeyManager)keyManager, keyPassword) : new OpenSslKeyMaterialManager(keyManager, keyPassword));
/*  91:    */       }
/*  92:    */     }
/*  93:    */     catch (Exception e)
/*  94:    */     {
/*  95:133 */       throw new SSLException("failed to set certificate and key", e);
/*  96:    */     }
/*  97:    */     try
/*  98:    */     {
/*  99:136 */       if (trustCertCollection != null)
/* 100:    */       {
/* 101:137 */         trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
/* 102:    */       }
/* 103:138 */       else if (trustManagerFactory == null)
/* 104:    */       {
/* 105:140 */         trustManagerFactory = TrustManagerFactory.getInstance(
/* 106:141 */           TrustManagerFactory.getDefaultAlgorithm());
/* 107:142 */         trustManagerFactory.init((KeyStore)null);
/* 108:    */       }
/* 109:145 */       X509TrustManager manager = chooseTrustManager(trustManagerFactory.getTrustManagers());
/* 110:154 */       if (useExtendedTrustManager(manager)) {
/* 111:155 */         SSLContext.setCertVerifyCallback(ctx, new ExtendedTrustManagerVerifyCallback(engineMap, (X509ExtendedTrustManager)manager));
/* 112:    */       } else {
/* 113:158 */         SSLContext.setCertVerifyCallback(ctx, new TrustManagerVerifyCallback(engineMap, manager));
/* 114:    */       }
/* 115:161 */       X509Certificate[] issuers = manager.getAcceptedIssuers();
/* 116:162 */       if ((issuers != null) && (issuers.length > 0))
/* 117:    */       {
/* 118:163 */         long bio = 0L;
/* 119:    */         try
/* 120:    */         {
/* 121:165 */           bio = toBIO(issuers);
/* 122:166 */           if (!SSLContext.setCACertificateBio(ctx, bio)) {
/* 123:167 */             throw new SSLException("unable to setup accepted issuers for trustmanager " + manager);
/* 124:    */           }
/* 125:    */         }
/* 126:    */         finally
/* 127:    */         {
/* 128:170 */           freeBio(bio);
/* 129:    */         }
/* 130:    */       }
/* 131:174 */       if (PlatformDependent.javaVersion() >= 8) {
/* 132:179 */         SSLContext.setSniHostnameMatcher(ctx, new OpenSslSniHostnameMatcher(engineMap));
/* 133:    */       }
/* 134:    */     }
/* 135:    */     catch (SSLException e)
/* 136:    */     {
/* 137:182 */       throw e;
/* 138:    */     }
/* 139:    */     catch (Exception e)
/* 140:    */     {
/* 141:184 */       throw new SSLException("unable to setup trustmanager", e);
/* 142:    */     }
/* 143:187 */     result.sessionContext = new OpenSslServerSessionContext(thiz);
/* 144:188 */     result.sessionContext.setSessionIdContext(ID);
/* 145:189 */     return result;
/* 146:    */   }
/* 147:    */   
/* 148:    */   static final class ServerContext
/* 149:    */   {
/* 150:    */     OpenSslServerSessionContext sessionContext;
/* 151:    */     OpenSslKeyMaterialManager keyMaterialManager;
/* 152:    */   }
/* 153:    */   
/* 154:    */   private static final class TrustManagerVerifyCallback
/* 155:    */     extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier
/* 156:    */   {
/* 157:    */     private final X509TrustManager manager;
/* 158:    */     
/* 159:    */     TrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509TrustManager manager)
/* 160:    */     {
/* 161:196 */       super();
/* 162:197 */       this.manager = manager;
/* 163:    */     }
/* 164:    */     
/* 165:    */     void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth)
/* 166:    */       throws Exception
/* 167:    */     {
/* 168:203 */       this.manager.checkClientTrusted(peerCerts, auth);
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   private static final class ExtendedTrustManagerVerifyCallback
/* 173:    */     extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier
/* 174:    */   {
/* 175:    */     private final X509ExtendedTrustManager manager;
/* 176:    */     
/* 177:    */     ExtendedTrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509ExtendedTrustManager manager)
/* 178:    */     {
/* 179:211 */       super();
/* 180:212 */       this.manager = manager;
/* 181:    */     }
/* 182:    */     
/* 183:    */     void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth)
/* 184:    */       throws Exception
/* 185:    */     {
/* 186:218 */       this.manager.checkClientTrusted(peerCerts, auth, engine);
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   private static final class OpenSslSniHostnameMatcher
/* 191:    */     implements SniHostNameMatcher
/* 192:    */   {
/* 193:    */     private final OpenSslEngineMap engineMap;
/* 194:    */     
/* 195:    */     OpenSslSniHostnameMatcher(OpenSslEngineMap engineMap)
/* 196:    */     {
/* 197:226 */       this.engineMap = engineMap;
/* 198:    */     }
/* 199:    */     
/* 200:    */     public boolean match(long ssl, String hostname)
/* 201:    */     {
/* 202:231 */       ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
/* 203:232 */       if (engine != null) {
/* 204:233 */         return engine.checkSniHostnameMatch(hostname);
/* 205:    */       }
/* 206:235 */       ReferenceCountedOpenSslServerContext.logger.warn("No ReferenceCountedOpenSslEngine found for SSL pointer: {}", Long.valueOf(ssl));
/* 207:236 */       return false;
/* 208:    */     }
/* 209:    */   }
/* 210:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ReferenceCountedOpenSslServerContext
 * JD-Core Version:    0.7.0.1
 */