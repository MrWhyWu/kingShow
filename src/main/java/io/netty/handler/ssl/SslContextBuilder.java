/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.ObjectUtil;
/*   4:    */ import java.io.File;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.security.PrivateKey;
/*   7:    */ import java.security.Provider;
/*   8:    */ import java.security.cert.X509Certificate;
/*   9:    */ import javax.net.ssl.KeyManagerFactory;
/*  10:    */ import javax.net.ssl.SSLException;
/*  11:    */ import javax.net.ssl.TrustManagerFactory;
/*  12:    */ 
/*  13:    */ public final class SslContextBuilder
/*  14:    */ {
/*  15:    */   private final boolean forServer;
/*  16:    */   private SslProvider provider;
/*  17:    */   private Provider sslContextProvider;
/*  18:    */   private X509Certificate[] trustCertCollection;
/*  19:    */   private TrustManagerFactory trustManagerFactory;
/*  20:    */   private X509Certificate[] keyCertChain;
/*  21:    */   private PrivateKey key;
/*  22:    */   private String keyPassword;
/*  23:    */   private KeyManagerFactory keyManagerFactory;
/*  24:    */   private Iterable<String> ciphers;
/*  25:    */   
/*  26:    */   public static SslContextBuilder forClient()
/*  27:    */   {
/*  28: 43 */     return new SslContextBuilder(false);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public static SslContextBuilder forServer(File keyCertChainFile, File keyFile)
/*  32:    */   {
/*  33: 54 */     return new SslContextBuilder(true).keyManager(keyCertChainFile, keyFile);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public static SslContextBuilder forServer(InputStream keyCertChainInputStream, InputStream keyInputStream)
/*  37:    */   {
/*  38: 65 */     return new SslContextBuilder(true).keyManager(keyCertChainInputStream, keyInputStream);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static SslContextBuilder forServer(PrivateKey key, X509Certificate... keyCertChain)
/*  42:    */   {
/*  43: 76 */     return new SslContextBuilder(true).keyManager(key, keyCertChain);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public static SslContextBuilder forServer(File keyCertChainFile, File keyFile, String keyPassword)
/*  47:    */   {
/*  48: 90 */     return new SslContextBuilder(true).keyManager(keyCertChainFile, keyFile, keyPassword);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static SslContextBuilder forServer(InputStream keyCertChainInputStream, InputStream keyInputStream, String keyPassword)
/*  52:    */   {
/*  53:104 */     return new SslContextBuilder(true).keyManager(keyCertChainInputStream, keyInputStream, keyPassword);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static SslContextBuilder forServer(PrivateKey key, String keyPassword, X509Certificate... keyCertChain)
/*  57:    */   {
/*  58:118 */     return new SslContextBuilder(true).keyManager(key, keyPassword, keyCertChain);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public static SslContextBuilder forServer(KeyManagerFactory keyManagerFactory)
/*  62:    */   {
/*  63:128 */     return new SslContextBuilder(true).keyManager(keyManagerFactory);
/*  64:    */   }
/*  65:    */   
/*  66:141 */   private CipherSuiteFilter cipherFilter = IdentityCipherSuiteFilter.INSTANCE;
/*  67:    */   private ApplicationProtocolConfig apn;
/*  68:    */   private long sessionCacheSize;
/*  69:    */   private long sessionTimeout;
/*  70:145 */   private ClientAuth clientAuth = ClientAuth.NONE;
/*  71:    */   private String[] protocols;
/*  72:    */   private boolean startTls;
/*  73:    */   private boolean enableOcsp;
/*  74:    */   
/*  75:    */   private SslContextBuilder(boolean forServer)
/*  76:    */   {
/*  77:151 */     this.forServer = forServer;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public SslContextBuilder sslProvider(SslProvider provider)
/*  81:    */   {
/*  82:158 */     this.provider = provider;
/*  83:159 */     return this;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public SslContextBuilder sslContextProvider(Provider sslContextProvider)
/*  87:    */   {
/*  88:167 */     this.sslContextProvider = sslContextProvider;
/*  89:168 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public SslContextBuilder trustManager(File trustCertCollectionFile)
/*  93:    */   {
/*  94:    */     try
/*  95:    */     {
/*  96:177 */       return trustManager(SslContext.toX509Certificates(trustCertCollectionFile));
/*  97:    */     }
/*  98:    */     catch (Exception e)
/*  99:    */     {
/* 100:179 */       throw new IllegalArgumentException("File does not contain valid certificates: " + trustCertCollectionFile, e);
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public SslContextBuilder trustManager(InputStream trustCertCollectionInputStream)
/* 105:    */   {
/* 106:    */     try
/* 107:    */     {
/* 108:190 */       return trustManager(SslContext.toX509Certificates(trustCertCollectionInputStream));
/* 109:    */     }
/* 110:    */     catch (Exception e)
/* 111:    */     {
/* 112:192 */       throw new IllegalArgumentException("Input stream does not contain valid certificates.", e);
/* 113:    */     }
/* 114:    */   }
/* 115:    */   
/* 116:    */   public SslContextBuilder trustManager(X509Certificate... trustCertCollection)
/* 117:    */   {
/* 118:200 */     this.trustCertCollection = (trustCertCollection != null ? (X509Certificate[])trustCertCollection.clone() : null);
/* 119:201 */     this.trustManagerFactory = null;
/* 120:202 */     return this;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public SslContextBuilder trustManager(TrustManagerFactory trustManagerFactory)
/* 124:    */   {
/* 125:209 */     this.trustCertCollection = null;
/* 126:210 */     this.trustManagerFactory = trustManagerFactory;
/* 127:211 */     return this;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public SslContextBuilder keyManager(File keyCertChainFile, File keyFile)
/* 131:    */   {
/* 132:222 */     return keyManager(keyCertChainFile, keyFile, null);
/* 133:    */   }
/* 134:    */   
/* 135:    */   public SslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream)
/* 136:    */   {
/* 137:233 */     return keyManager(keyCertChainInputStream, keyInputStream, null);
/* 138:    */   }
/* 139:    */   
/* 140:    */   public SslContextBuilder keyManager(PrivateKey key, X509Certificate... keyCertChain)
/* 141:    */   {
/* 142:244 */     return keyManager(key, null, keyCertChain);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public SslContextBuilder keyManager(File keyCertChainFile, File keyFile, String keyPassword)
/* 146:    */   {
/* 147:    */     try
/* 148:    */     {
/* 149:260 */       keyCertChain = SslContext.toX509Certificates(keyCertChainFile);
/* 150:    */     }
/* 151:    */     catch (Exception e)
/* 152:    */     {
/* 153:    */       X509Certificate[] keyCertChain;
/* 154:262 */       throw new IllegalArgumentException("File does not contain valid certificates: " + keyCertChainFile, e);
/* 155:    */     }
/* 156:    */     X509Certificate[] keyCertChain;
/* 157:    */     try
/* 158:    */     {
/* 159:265 */       key = SslContext.toPrivateKey(keyFile, keyPassword);
/* 160:    */     }
/* 161:    */     catch (Exception e)
/* 162:    */     {
/* 163:    */       PrivateKey key;
/* 164:267 */       throw new IllegalArgumentException("File does not contain valid private key: " + keyFile, e);
/* 165:    */     }
/* 166:    */     PrivateKey key;
/* 167:269 */     return keyManager(key, keyPassword, keyCertChain);
/* 168:    */   }
/* 169:    */   
/* 170:    */   public SslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream, String keyPassword)
/* 171:    */   {
/* 172:    */     try
/* 173:    */     {
/* 174:286 */       keyCertChain = SslContext.toX509Certificates(keyCertChainInputStream);
/* 175:    */     }
/* 176:    */     catch (Exception e)
/* 177:    */     {
/* 178:    */       X509Certificate[] keyCertChain;
/* 179:288 */       throw new IllegalArgumentException("Input stream not contain valid certificates.", e);
/* 180:    */     }
/* 181:    */     X509Certificate[] keyCertChain;
/* 182:    */     try
/* 183:    */     {
/* 184:291 */       key = SslContext.toPrivateKey(keyInputStream, keyPassword);
/* 185:    */     }
/* 186:    */     catch (Exception e)
/* 187:    */     {
/* 188:    */       PrivateKey key;
/* 189:293 */       throw new IllegalArgumentException("Input stream does not contain valid private key.", e);
/* 190:    */     }
/* 191:    */     PrivateKey key;
/* 192:295 */     return keyManager(key, keyPassword, keyCertChain);
/* 193:    */   }
/* 194:    */   
/* 195:    */   public SslContextBuilder keyManager(PrivateKey key, String keyPassword, X509Certificate... keyCertChain)
/* 196:    */   {
/* 197:308 */     if (this.forServer)
/* 198:    */     {
/* 199:309 */       ObjectUtil.checkNotNull(keyCertChain, "keyCertChain required for servers");
/* 200:310 */       if (keyCertChain.length == 0) {
/* 201:311 */         throw new IllegalArgumentException("keyCertChain must be non-empty");
/* 202:    */       }
/* 203:313 */       ObjectUtil.checkNotNull(key, "key required for servers");
/* 204:    */     }
/* 205:315 */     if ((keyCertChain == null) || (keyCertChain.length == 0))
/* 206:    */     {
/* 207:316 */       this.keyCertChain = null;
/* 208:    */     }
/* 209:    */     else
/* 210:    */     {
/* 211:318 */       for (X509Certificate cert : keyCertChain) {
/* 212:319 */         if (cert == null) {
/* 213:320 */           throw new IllegalArgumentException("keyCertChain contains null entry");
/* 214:    */         }
/* 215:    */       }
/* 216:323 */       this.keyCertChain = ((X509Certificate[])keyCertChain.clone());
/* 217:    */     }
/* 218:325 */     this.key = key;
/* 219:326 */     this.keyPassword = keyPassword;
/* 220:327 */     this.keyManagerFactory = null;
/* 221:328 */     return this;
/* 222:    */   }
/* 223:    */   
/* 224:    */   public SslContextBuilder keyManager(KeyManagerFactory keyManagerFactory)
/* 225:    */   {
/* 226:340 */     if (this.forServer) {
/* 227:341 */       ObjectUtil.checkNotNull(keyManagerFactory, "keyManagerFactory required for servers");
/* 228:    */     }
/* 229:343 */     this.keyCertChain = null;
/* 230:344 */     this.key = null;
/* 231:345 */     this.keyPassword = null;
/* 232:346 */     this.keyManagerFactory = keyManagerFactory;
/* 233:347 */     return this;
/* 234:    */   }
/* 235:    */   
/* 236:    */   public SslContextBuilder ciphers(Iterable<String> ciphers)
/* 237:    */   {
/* 238:355 */     return ciphers(ciphers, IdentityCipherSuiteFilter.INSTANCE);
/* 239:    */   }
/* 240:    */   
/* 241:    */   public SslContextBuilder ciphers(Iterable<String> ciphers, CipherSuiteFilter cipherFilter)
/* 242:    */   {
/* 243:364 */     ObjectUtil.checkNotNull(cipherFilter, "cipherFilter");
/* 244:365 */     this.ciphers = ciphers;
/* 245:366 */     this.cipherFilter = cipherFilter;
/* 246:367 */     return this;
/* 247:    */   }
/* 248:    */   
/* 249:    */   public SslContextBuilder applicationProtocolConfig(ApplicationProtocolConfig apn)
/* 250:    */   {
/* 251:374 */     this.apn = apn;
/* 252:375 */     return this;
/* 253:    */   }
/* 254:    */   
/* 255:    */   public SslContextBuilder sessionCacheSize(long sessionCacheSize)
/* 256:    */   {
/* 257:383 */     this.sessionCacheSize = sessionCacheSize;
/* 258:384 */     return this;
/* 259:    */   }
/* 260:    */   
/* 261:    */   public SslContextBuilder sessionTimeout(long sessionTimeout)
/* 262:    */   {
/* 263:392 */     this.sessionTimeout = sessionTimeout;
/* 264:393 */     return this;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public SslContextBuilder clientAuth(ClientAuth clientAuth)
/* 268:    */   {
/* 269:400 */     this.clientAuth = ((ClientAuth)ObjectUtil.checkNotNull(clientAuth, "clientAuth"));
/* 270:401 */     return this;
/* 271:    */   }
/* 272:    */   
/* 273:    */   public SslContextBuilder protocols(String... protocols)
/* 274:    */   {
/* 275:410 */     this.protocols = (protocols == null ? null : (String[])protocols.clone());
/* 276:411 */     return this;
/* 277:    */   }
/* 278:    */   
/* 279:    */   public SslContextBuilder startTls(boolean startTls)
/* 280:    */   {
/* 281:418 */     this.startTls = startTls;
/* 282:419 */     return this;
/* 283:    */   }
/* 284:    */   
/* 285:    */   public SslContextBuilder enableOcsp(boolean enableOcsp)
/* 286:    */   {
/* 287:430 */     this.enableOcsp = enableOcsp;
/* 288:431 */     return this;
/* 289:    */   }
/* 290:    */   
/* 291:    */   public SslContext build()
/* 292:    */     throws SSLException
/* 293:    */   {
/* 294:440 */     if (this.forServer) {
/* 295:441 */       return SslContext.newServerContextInternal(this.provider, this.sslContextProvider, this.trustCertCollection, this.trustManagerFactory, this.keyCertChain, this.key, this.keyPassword, this.keyManagerFactory, this.ciphers, this.cipherFilter, this.apn, this.sessionCacheSize, this.sessionTimeout, this.clientAuth, this.protocols, this.startTls, this.enableOcsp);
/* 296:    */     }
/* 297:446 */     return SslContext.newClientContextInternal(this.provider, this.sslContextProvider, this.trustCertCollection, this.trustManagerFactory, this.keyCertChain, this.key, this.keyPassword, this.keyManagerFactory, this.ciphers, this.cipherFilter, this.apn, this.protocols, this.sessionCacheSize, this.sessionTimeout, this.enableOcsp);
/* 298:    */   }
/* 299:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslContextBuilder
 * JD-Core Version:    0.7.0.1
 */