/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.internal.tcnative.CertificateRequestedCallback;
/*   4:    */ import io.netty.internal.tcnative.CertificateRequestedCallback.KeyMaterial;
/*   5:    */ import io.netty.internal.tcnative.SSLContext;
/*   6:    */ import io.netty.util.internal.logging.InternalLogger;
/*   7:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   8:    */ import java.security.KeyStore;
/*   9:    */ import java.security.PrivateKey;
/*  10:    */ import java.security.cert.X509Certificate;
/*  11:    */ import java.util.HashSet;
/*  12:    */ import java.util.Set;
/*  13:    */ import javax.net.ssl.KeyManagerFactory;
/*  14:    */ import javax.net.ssl.SSLException;
/*  15:    */ import javax.net.ssl.SSLHandshakeException;
/*  16:    */ import javax.net.ssl.TrustManagerFactory;
/*  17:    */ import javax.net.ssl.X509ExtendedKeyManager;
/*  18:    */ import javax.net.ssl.X509ExtendedTrustManager;
/*  19:    */ import javax.net.ssl.X509KeyManager;
/*  20:    */ import javax.net.ssl.X509TrustManager;
/*  21:    */ import javax.security.auth.x500.X500Principal;
/*  22:    */ 
/*  23:    */ public final class ReferenceCountedOpenSslClientContext
/*  24:    */   extends ReferenceCountedOpenSslContext
/*  25:    */ {
/*  26: 50 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslClientContext.class);
/*  27:    */   private final OpenSslSessionContext sessionContext;
/*  28:    */   
/*  29:    */   ReferenceCountedOpenSslClientContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, String[] protocols, long sessionCacheSize, long sessionTimeout, boolean enableOcsp)
/*  30:    */     throws SSLException
/*  31:    */   {
/*  32: 59 */     super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 0, keyCertChain, ClientAuth.NONE, protocols, false, enableOcsp, true);
/*  33:    */     
/*  34: 61 */     boolean success = false;
/*  35:    */     try
/*  36:    */     {
/*  37: 63 */       this.sessionContext = newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory);
/*  38:    */       
/*  39: 65 */       success = true;
/*  40:    */     }
/*  41:    */     finally
/*  42:    */     {
/*  43: 67 */       if (!success) {
/*  44: 68 */         release();
/*  45:    */       }
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49:    */   OpenSslKeyMaterialManager keyMaterialManager()
/*  50:    */   {
/*  51: 75 */     return null;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public OpenSslSessionContext sessionContext()
/*  55:    */   {
/*  56: 80 */     return this.sessionContext;
/*  57:    */   }
/*  58:    */   
/*  59:    */   static OpenSslSessionContext newSessionContext(ReferenceCountedOpenSslContext thiz, long ctx, OpenSslEngineMap engineMap, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory)
/*  60:    */     throws SSLException
/*  61:    */   {
/*  62: 89 */     if (((key == null) && (keyCertChain != null)) || ((key != null) && (keyCertChain == null))) {
/*  63: 90 */       throw new IllegalArgumentException("Either both keyCertChain and key needs to be null or none of them");
/*  64:    */     }
/*  65:    */     try
/*  66:    */     {
/*  67: 94 */       if (!OpenSsl.useKeyManagerFactory())
/*  68:    */       {
/*  69: 95 */         if (keyManagerFactory != null) {
/*  70: 96 */           throw new IllegalArgumentException("KeyManagerFactory not supported");
/*  71:    */         }
/*  72: 99 */         if (keyCertChain != null) {
/*  73:100 */           setKeyMaterial(ctx, keyCertChain, key, keyPassword);
/*  74:    */         }
/*  75:    */       }
/*  76:    */       else
/*  77:    */       {
/*  78:104 */         if ((keyManagerFactory == null) && (keyCertChain != null)) {
/*  79:105 */           keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory);
/*  80:    */         }
/*  81:109 */         if (keyManagerFactory != null)
/*  82:    */         {
/*  83:110 */           X509KeyManager keyManager = chooseX509KeyManager(keyManagerFactory.getKeyManagers());
/*  84:111 */           OpenSslKeyMaterialManager materialManager = useExtendedKeyManager(keyManager) ? new OpenSslExtendedKeyMaterialManager((X509ExtendedKeyManager)keyManager, keyPassword) : new OpenSslKeyMaterialManager(keyManager, keyPassword);
/*  85:    */           
/*  86:    */ 
/*  87:    */ 
/*  88:115 */           SSLContext.setCertRequestedCallback(ctx, new OpenSslCertificateRequestedCallback(engineMap, materialManager));
/*  89:    */         }
/*  90:    */       }
/*  91:    */     }
/*  92:    */     catch (Exception e)
/*  93:    */     {
/*  94:120 */       throw new SSLException("failed to set certificate and key", e);
/*  95:    */     }
/*  96:123 */     SSLContext.setVerify(ctx, 0, 10);
/*  97:    */     try
/*  98:    */     {
/*  99:126 */       if (trustCertCollection != null)
/* 100:    */       {
/* 101:127 */         trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
/* 102:    */       }
/* 103:128 */       else if (trustManagerFactory == null)
/* 104:    */       {
/* 105:129 */         trustManagerFactory = TrustManagerFactory.getInstance(
/* 106:130 */           TrustManagerFactory.getDefaultAlgorithm());
/* 107:131 */         trustManagerFactory.init((KeyStore)null);
/* 108:    */       }
/* 109:133 */       X509TrustManager manager = chooseTrustManager(trustManagerFactory.getTrustManagers());
/* 110:142 */       if (useExtendedTrustManager(manager)) {
/* 111:143 */         SSLContext.setCertVerifyCallback(ctx, new ExtendedTrustManagerVerifyCallback(engineMap, (X509ExtendedTrustManager)manager));
/* 112:    */       } else {
/* 113:146 */         SSLContext.setCertVerifyCallback(ctx, new TrustManagerVerifyCallback(engineMap, manager));
/* 114:    */       }
/* 115:    */     }
/* 116:    */     catch (Exception e)
/* 117:    */     {
/* 118:149 */       throw new SSLException("unable to setup trustmanager", e);
/* 119:    */     }
/* 120:151 */     return new OpenSslClientSessionContext(thiz);
/* 121:    */   }
/* 122:    */   
/* 123:    */   static final class OpenSslClientSessionContext
/* 124:    */     extends OpenSslSessionContext
/* 125:    */   {
/* 126:    */     OpenSslClientSessionContext(ReferenceCountedOpenSslContext context)
/* 127:    */     {
/* 128:157 */       super();
/* 129:    */     }
/* 130:    */     
/* 131:    */     public void setSessionTimeout(int seconds)
/* 132:    */     {
/* 133:162 */       if (seconds < 0) {
/* 134:163 */         throw new IllegalArgumentException();
/* 135:    */       }
/* 136:    */     }
/* 137:    */     
/* 138:    */     public int getSessionTimeout()
/* 139:    */     {
/* 140:169 */       return 0;
/* 141:    */     }
/* 142:    */     
/* 143:    */     public void setSessionCacheSize(int size)
/* 144:    */     {
/* 145:174 */       if (size < 0) {
/* 146:175 */         throw new IllegalArgumentException();
/* 147:    */       }
/* 148:    */     }
/* 149:    */     
/* 150:    */     public int getSessionCacheSize()
/* 151:    */     {
/* 152:181 */       return 0;
/* 153:    */     }
/* 154:    */     
/* 155:    */     public void setSessionCacheEnabled(boolean enabled) {}
/* 156:    */     
/* 157:    */     public boolean isSessionCacheEnabled()
/* 158:    */     {
/* 159:191 */       return false;
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   private static final class TrustManagerVerifyCallback
/* 164:    */     extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier
/* 165:    */   {
/* 166:    */     private final X509TrustManager manager;
/* 167:    */     
/* 168:    */     TrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509TrustManager manager)
/* 169:    */     {
/* 170:199 */       super();
/* 171:200 */       this.manager = manager;
/* 172:    */     }
/* 173:    */     
/* 174:    */     void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth)
/* 175:    */       throws Exception
/* 176:    */     {
/* 177:206 */       this.manager.checkServerTrusted(peerCerts, auth);
/* 178:    */     }
/* 179:    */   }
/* 180:    */   
/* 181:    */   private static final class ExtendedTrustManagerVerifyCallback
/* 182:    */     extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier
/* 183:    */   {
/* 184:    */     private final X509ExtendedTrustManager manager;
/* 185:    */     
/* 186:    */     ExtendedTrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509ExtendedTrustManager manager)
/* 187:    */     {
/* 188:214 */       super();
/* 189:215 */       this.manager = manager;
/* 190:    */     }
/* 191:    */     
/* 192:    */     void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth)
/* 193:    */       throws Exception
/* 194:    */     {
/* 195:221 */       this.manager.checkServerTrusted(peerCerts, auth, engine);
/* 196:    */     }
/* 197:    */   }
/* 198:    */   
/* 199:    */   private static final class OpenSslCertificateRequestedCallback
/* 200:    */     implements CertificateRequestedCallback
/* 201:    */   {
/* 202:    */     private final OpenSslEngineMap engineMap;
/* 203:    */     private final OpenSslKeyMaterialManager keyManagerHolder;
/* 204:    */     
/* 205:    */     OpenSslCertificateRequestedCallback(OpenSslEngineMap engineMap, OpenSslKeyMaterialManager keyManagerHolder)
/* 206:    */     {
/* 207:230 */       this.engineMap = engineMap;
/* 208:231 */       this.keyManagerHolder = keyManagerHolder;
/* 209:    */     }
/* 210:    */     
/* 211:    */     public CertificateRequestedCallback.KeyMaterial requested(long ssl, byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals)
/* 212:    */     {
/* 213:236 */       ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
/* 214:    */       try
/* 215:    */       {
/* 216:238 */         Set<String> keyTypesSet = supportedClientKeyTypes(keyTypeBytes);
/* 217:239 */         String[] keyTypes = (String[])keyTypesSet.toArray(new String[keyTypesSet.size()]);
/* 218:    */         X500Principal[] issuers;
/* 219:    */         X500Principal[] issuers;
/* 220:241 */         if (asn1DerEncodedPrincipals == null)
/* 221:    */         {
/* 222:242 */           issuers = null;
/* 223:    */         }
/* 224:    */         else
/* 225:    */         {
/* 226:244 */           issuers = new X500Principal[asn1DerEncodedPrincipals.length];
/* 227:245 */           for (int i = 0; i < asn1DerEncodedPrincipals.length; i++) {
/* 228:246 */             issuers[i] = new X500Principal(asn1DerEncodedPrincipals[i]);
/* 229:    */           }
/* 230:    */         }
/* 231:249 */         return this.keyManagerHolder.keyMaterial(engine, keyTypes, issuers);
/* 232:    */       }
/* 233:    */       catch (Throwable cause)
/* 234:    */       {
/* 235:251 */         ReferenceCountedOpenSslClientContext.logger.debug("request of key failed", cause);
/* 236:252 */         SSLHandshakeException e = new SSLHandshakeException("General OpenSslEngine problem");
/* 237:253 */         e.initCause(cause);
/* 238:254 */         engine.handshakeException = e;
/* 239:    */       }
/* 240:255 */       return null;
/* 241:    */     }
/* 242:    */     
/* 243:    */     private static Set<String> supportedClientKeyTypes(byte[] clientCertificateTypes)
/* 244:    */     {
/* 245:268 */       Set<String> result = new HashSet(clientCertificateTypes.length);
/* 246:269 */       for (byte keyTypeCode : clientCertificateTypes)
/* 247:    */       {
/* 248:270 */         String keyType = clientKeyType(keyTypeCode);
/* 249:271 */         if (keyType != null) {
/* 250:275 */           result.add(keyType);
/* 251:    */         }
/* 252:    */       }
/* 253:277 */       return result;
/* 254:    */     }
/* 255:    */     
/* 256:    */     private static String clientKeyType(byte clientCertificateType)
/* 257:    */     {
/* 258:282 */       switch (clientCertificateType)
/* 259:    */       {
/* 260:    */       case 1: 
/* 261:284 */         return "RSA";
/* 262:    */       case 3: 
/* 263:286 */         return "DH_RSA";
/* 264:    */       case 64: 
/* 265:288 */         return "EC";
/* 266:    */       case 65: 
/* 267:290 */         return "EC_RSA";
/* 268:    */       case 66: 
/* 269:292 */         return "EC_EC";
/* 270:    */       }
/* 271:294 */       return null;
/* 272:    */     }
/* 273:    */   }
/* 274:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ReferenceCountedOpenSslClientContext
 * JD-Core Version:    0.7.0.1
 */