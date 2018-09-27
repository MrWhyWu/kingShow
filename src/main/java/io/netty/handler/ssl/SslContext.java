/*    1:     */ package io.netty.handler.ssl;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.buffer.ByteBufInputStream;
/*    6:     */ import io.netty.util.internal.EmptyArrays;
/*    7:     */ import java.io.File;
/*    8:     */ import java.io.IOException;
/*    9:     */ import java.io.InputStream;
/*   10:     */ import java.security.InvalidAlgorithmParameterException;
/*   11:     */ import java.security.InvalidKeyException;
/*   12:     */ import java.security.KeyException;
/*   13:     */ import java.security.KeyFactory;
/*   14:     */ import java.security.KeyStore;
/*   15:     */ import java.security.KeyStoreException;
/*   16:     */ import java.security.NoSuchAlgorithmException;
/*   17:     */ import java.security.PrivateKey;
/*   18:     */ import java.security.Provider;
/*   19:     */ import java.security.Security;
/*   20:     */ import java.security.UnrecoverableKeyException;
/*   21:     */ import java.security.cert.CertificateException;
/*   22:     */ import java.security.cert.CertificateFactory;
/*   23:     */ import java.security.cert.X509Certificate;
/*   24:     */ import java.security.spec.InvalidKeySpecException;
/*   25:     */ import java.security.spec.PKCS8EncodedKeySpec;
/*   26:     */ import java.util.List;
/*   27:     */ import javax.crypto.Cipher;
/*   28:     */ import javax.crypto.EncryptedPrivateKeyInfo;
/*   29:     */ import javax.crypto.NoSuchPaddingException;
/*   30:     */ import javax.crypto.SecretKey;
/*   31:     */ import javax.crypto.SecretKeyFactory;
/*   32:     */ import javax.crypto.spec.PBEKeySpec;
/*   33:     */ import javax.net.ssl.KeyManagerFactory;
/*   34:     */ import javax.net.ssl.SSLEngine;
/*   35:     */ import javax.net.ssl.SSLException;
/*   36:     */ import javax.net.ssl.SSLSessionContext;
/*   37:     */ import javax.net.ssl.TrustManagerFactory;
/*   38:     */ 
/*   39:     */ public abstract class SslContext
/*   40:     */ {
/*   41:     */   static final CertificateFactory X509_CERT_FACTORY;
/*   42:     */   private final boolean startTls;
/*   43:     */   
/*   44:     */   static
/*   45:     */   {
/*   46:     */     try
/*   47:     */     {
/*   48:  91 */       X509_CERT_FACTORY = CertificateFactory.getInstance("X.509");
/*   49:     */     }
/*   50:     */     catch (CertificateException e)
/*   51:     */     {
/*   52:  93 */       throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
/*   53:     */     }
/*   54:     */   }
/*   55:     */   
/*   56:     */   public static SslProvider defaultServerProvider()
/*   57:     */   {
/*   58: 105 */     return defaultProvider();
/*   59:     */   }
/*   60:     */   
/*   61:     */   public static SslProvider defaultClientProvider()
/*   62:     */   {
/*   63: 114 */     return defaultProvider();
/*   64:     */   }
/*   65:     */   
/*   66:     */   private static SslProvider defaultProvider()
/*   67:     */   {
/*   68: 118 */     if (OpenSsl.isAvailable()) {
/*   69: 119 */       return SslProvider.OPENSSL;
/*   70:     */     }
/*   71: 121 */     return SslProvider.JDK;
/*   72:     */   }
/*   73:     */   
/*   74:     */   @Deprecated
/*   75:     */   public static SslContext newServerContext(File certChainFile, File keyFile)
/*   76:     */     throws SSLException
/*   77:     */   {
/*   78: 135 */     return newServerContext(certChainFile, keyFile, null);
/*   79:     */   }
/*   80:     */   
/*   81:     */   @Deprecated
/*   82:     */   public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword)
/*   83:     */     throws SSLException
/*   84:     */   {
/*   85: 151 */     return newServerContext(null, certChainFile, keyFile, keyPassword);
/*   86:     */   }
/*   87:     */   
/*   88:     */   @Deprecated
/*   89:     */   public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*   90:     */     throws SSLException
/*   91:     */   {
/*   92: 178 */     return newServerContext(null, certChainFile, keyFile, keyPassword, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/*   93:     */   }
/*   94:     */   
/*   95:     */   @Deprecated
/*   96:     */   public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*   97:     */     throws SSLException
/*   98:     */   {
/*   99: 206 */     return newServerContext(null, certChainFile, keyFile, keyPassword, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  100:     */   }
/*  101:     */   
/*  102:     */   @Deprecated
/*  103:     */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile)
/*  104:     */     throws SSLException
/*  105:     */   {
/*  106: 224 */     return newServerContext(provider, certChainFile, keyFile, null);
/*  107:     */   }
/*  108:     */   
/*  109:     */   @Deprecated
/*  110:     */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword)
/*  111:     */     throws SSLException
/*  112:     */   {
/*  113: 242 */     return newServerContext(provider, certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
/*  114:     */   }
/*  115:     */   
/*  116:     */   @Deprecated
/*  117:     */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  118:     */     throws SSLException
/*  119:     */   {
/*  120: 272 */     return newServerContext(provider, certChainFile, keyFile, keyPassword, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
/*  121:     */     
/*  122: 274 */       toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
/*  123:     */   }
/*  124:     */   
/*  125:     */   @Deprecated
/*  126:     */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  127:     */     throws SSLException
/*  128:     */   {
/*  129: 307 */     return newServerContext(provider, null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
/*  130:     */     
/*  131:     */ 
/*  132: 310 */       toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
/*  133:     */   }
/*  134:     */   
/*  135:     */   @Deprecated
/*  136:     */   public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  137:     */     throws SSLException
/*  138:     */   {
/*  139: 339 */     return newServerContext(provider, null, null, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  140:     */   }
/*  141:     */   
/*  142:     */   @Deprecated
/*  143:     */   public static SslContext newServerContext(SslProvider provider, File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  144:     */     throws SSLException
/*  145:     */   {
/*  146:     */     try
/*  147:     */     {
/*  148: 384 */       return newServerContextInternal(provider, null, toX509Certificates(trustCertCollectionFile), trustManagerFactory, 
/*  149: 385 */         toX509Certificates(keyCertChainFile), 
/*  150: 386 */         toPrivateKey(keyFile, keyPassword), keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, ClientAuth.NONE, null, false, false);
/*  151:     */     }
/*  152:     */     catch (Exception e)
/*  153:     */     {
/*  154: 390 */       if ((e instanceof SSLException)) {
/*  155: 391 */         throw ((SSLException)e);
/*  156:     */       }
/*  157: 393 */       throw new SSLException("failed to initialize the server-side SSL context", e);
/*  158:     */     }
/*  159:     */   }
/*  160:     */   
/*  161:     */   static SslContext newServerContextInternal(SslProvider provider, Provider sslContextProvider, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/*  162:     */     throws SSLException
/*  163:     */   {
/*  164: 406 */     if (provider == null) {
/*  165: 407 */       provider = defaultServerProvider();
/*  166:     */     }
/*  167: 410 */     switch (1.$SwitchMap$io$netty$handler$ssl$SslProvider[provider.ordinal()])
/*  168:     */     {
/*  169:     */     case 1: 
/*  170: 412 */       if (enableOcsp) {
/*  171: 413 */         throw new IllegalArgumentException("OCSP is not supported with this SslProvider: " + provider);
/*  172:     */       }
/*  173: 415 */       return new JdkSslServerContext(sslContextProvider, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls);
/*  174:     */     case 2: 
/*  175: 420 */       verifyNullSslContextProvider(provider, sslContextProvider);
/*  176: 421 */       return new OpenSslServerContext(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
/*  177:     */     case 3: 
/*  178: 426 */       verifyNullSslContextProvider(provider, sslContextProvider);
/*  179: 427 */       return new ReferenceCountedOpenSslServerContext(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
/*  180:     */     }
/*  181: 432 */     throw new Error(provider.toString());
/*  182:     */   }
/*  183:     */   
/*  184:     */   private static void verifyNullSslContextProvider(SslProvider provider, Provider sslContextProvider)
/*  185:     */   {
/*  186: 437 */     if (sslContextProvider != null) {
/*  187: 438 */       throw new IllegalArgumentException("Java Security Provider unsupported for SslProvider: " + provider);
/*  188:     */     }
/*  189:     */   }
/*  190:     */   
/*  191:     */   @Deprecated
/*  192:     */   public static SslContext newClientContext()
/*  193:     */     throws SSLException
/*  194:     */   {
/*  195: 450 */     return newClientContext(null, null, null);
/*  196:     */   }
/*  197:     */   
/*  198:     */   @Deprecated
/*  199:     */   public static SslContext newClientContext(File certChainFile)
/*  200:     */     throws SSLException
/*  201:     */   {
/*  202: 463 */     return newClientContext(null, certChainFile);
/*  203:     */   }
/*  204:     */   
/*  205:     */   @Deprecated
/*  206:     */   public static SslContext newClientContext(TrustManagerFactory trustManagerFactory)
/*  207:     */     throws SSLException
/*  208:     */   {
/*  209: 478 */     return newClientContext(null, null, trustManagerFactory);
/*  210:     */   }
/*  211:     */   
/*  212:     */   @Deprecated
/*  213:     */   public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory)
/*  214:     */     throws SSLException
/*  215:     */   {
/*  216: 496 */     return newClientContext(null, certChainFile, trustManagerFactory);
/*  217:     */   }
/*  218:     */   
/*  219:     */   @Deprecated
/*  220:     */   public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  221:     */     throws SSLException
/*  222:     */   {
/*  223: 524 */     return newClientContext(null, certChainFile, trustManagerFactory, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
/*  224:     */   }
/*  225:     */   
/*  226:     */   @Deprecated
/*  227:     */   public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  228:     */     throws SSLException
/*  229:     */   {
/*  230: 554 */     return newClientContext(null, certChainFile, trustManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  231:     */   }
/*  232:     */   
/*  233:     */   @Deprecated
/*  234:     */   public static SslContext newClientContext(SslProvider provider)
/*  235:     */     throws SSLException
/*  236:     */   {
/*  237: 570 */     return newClientContext(provider, null, null);
/*  238:     */   }
/*  239:     */   
/*  240:     */   @Deprecated
/*  241:     */   public static SslContext newClientContext(SslProvider provider, File certChainFile)
/*  242:     */     throws SSLException
/*  243:     */   {
/*  244: 586 */     return newClientContext(provider, certChainFile, null);
/*  245:     */   }
/*  246:     */   
/*  247:     */   @Deprecated
/*  248:     */   public static SslContext newClientContext(SslProvider provider, TrustManagerFactory trustManagerFactory)
/*  249:     */     throws SSLException
/*  250:     */   {
/*  251: 604 */     return newClientContext(provider, null, trustManagerFactory);
/*  252:     */   }
/*  253:     */   
/*  254:     */   @Deprecated
/*  255:     */   public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory)
/*  256:     */     throws SSLException
/*  257:     */   {
/*  258: 624 */     return newClientContext(provider, certChainFile, trustManagerFactory, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
/*  259:     */   }
/*  260:     */   
/*  261:     */   @Deprecated
/*  262:     */   public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout)
/*  263:     */     throws SSLException
/*  264:     */   {
/*  265: 656 */     return newClientContext(provider, certChainFile, trustManagerFactory, null, null, null, null, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
/*  266:     */     
/*  267:     */ 
/*  268: 659 */       toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
/*  269:     */   }
/*  270:     */   
/*  271:     */   @Deprecated
/*  272:     */   public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  273:     */     throws SSLException
/*  274:     */   {
/*  275: 691 */     return newClientContext(provider, certChainFile, trustManagerFactory, null, null, null, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
/*  276:     */   }
/*  277:     */   
/*  278:     */   @Deprecated
/*  279:     */   public static SslContext newClientContext(SslProvider provider, File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout)
/*  280:     */     throws SSLException
/*  281:     */   {
/*  282:     */     try
/*  283:     */     {
/*  284: 742 */       return newClientContextInternal(provider, null, 
/*  285: 743 */         toX509Certificates(trustCertCollectionFile), trustManagerFactory, 
/*  286: 744 */         toX509Certificates(keyCertChainFile), toPrivateKey(keyFile, keyPassword), keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, null, sessionCacheSize, sessionTimeout, false);
/*  287:     */     }
/*  288:     */     catch (Exception e)
/*  289:     */     {
/*  290: 748 */       if ((e instanceof SSLException)) {
/*  291: 749 */         throw ((SSLException)e);
/*  292:     */       }
/*  293: 751 */       throw new SSLException("failed to initialize the client-side SSL context", e);
/*  294:     */     }
/*  295:     */   }
/*  296:     */   
/*  297:     */   static SslContext newClientContextInternal(SslProvider provider, Provider sslContextProvider, X509Certificate[] trustCert, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, String[] protocols, long sessionCacheSize, long sessionTimeout, boolean enableOcsp)
/*  298:     */     throws SSLException
/*  299:     */   {
/*  300: 762 */     if (provider == null) {
/*  301: 763 */       provider = defaultClientProvider();
/*  302:     */     }
/*  303: 765 */     switch (1.$SwitchMap$io$netty$handler$ssl$SslProvider[provider.ordinal()])
/*  304:     */     {
/*  305:     */     case 1: 
/*  306: 767 */       if (enableOcsp) {
/*  307: 768 */         throw new IllegalArgumentException("OCSP is not supported with this SslProvider: " + provider);
/*  308:     */       }
/*  309: 770 */       return new JdkSslClientContext(sslContextProvider, trustCert, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize, sessionTimeout);
/*  310:     */     case 2: 
/*  311: 774 */       verifyNullSslContextProvider(provider, sslContextProvider);
/*  312: 775 */       return new OpenSslClientContext(trustCert, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize, sessionTimeout, enableOcsp);
/*  313:     */     case 3: 
/*  314: 780 */       verifyNullSslContextProvider(provider, sslContextProvider);
/*  315: 781 */       return new ReferenceCountedOpenSslClientContext(trustCert, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize, sessionTimeout, enableOcsp);
/*  316:     */     }
/*  317: 786 */     throw new Error(provider.toString());
/*  318:     */   }
/*  319:     */   
/*  320:     */   static ApplicationProtocolConfig toApplicationProtocolConfig(Iterable<String> nextProtocols)
/*  321:     */   {
/*  322:     */     ApplicationProtocolConfig apn;
/*  323:     */     ApplicationProtocolConfig apn;
/*  324: 792 */     if (nextProtocols == null) {
/*  325: 793 */       apn = ApplicationProtocolConfig.DISABLED;
/*  326:     */     } else {
/*  327: 795 */       apn = new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.NPN_AND_ALPN, ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL, ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT, nextProtocols);
/*  328:     */     }
/*  329: 799 */     return apn;
/*  330:     */   }
/*  331:     */   
/*  332:     */   protected SslContext()
/*  333:     */   {
/*  334: 806 */     this(false);
/*  335:     */   }
/*  336:     */   
/*  337:     */   protected SslContext(boolean startTls)
/*  338:     */   {
/*  339: 813 */     this.startTls = startTls;
/*  340:     */   }
/*  341:     */   
/*  342:     */   public final boolean isServer()
/*  343:     */   {
/*  344: 820 */     return !isClient();
/*  345:     */   }
/*  346:     */   
/*  347:     */   @Deprecated
/*  348:     */   public final List<String> nextProtocols()
/*  349:     */   {
/*  350: 848 */     return applicationProtocolNegotiator().protocols();
/*  351:     */   }
/*  352:     */   
/*  353:     */   public final SslHandler newHandler(ByteBufAllocator alloc)
/*  354:     */   {
/*  355: 905 */     return newHandler(alloc, this.startTls);
/*  356:     */   }
/*  357:     */   
/*  358:     */   protected SslHandler newHandler(ByteBufAllocator alloc, boolean startTls)
/*  359:     */   {
/*  360: 913 */     return new SslHandler(newEngine(alloc), startTls);
/*  361:     */   }
/*  362:     */   
/*  363:     */   public final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort)
/*  364:     */   {
/*  365: 943 */     return newHandler(alloc, peerHost, peerPort, this.startTls);
/*  366:     */   }
/*  367:     */   
/*  368:     */   protected SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls)
/*  369:     */   {
/*  370: 951 */     return new SslHandler(newEngine(alloc, peerHost, peerPort), startTls);
/*  371:     */   }
/*  372:     */   
/*  373:     */   protected static PKCS8EncodedKeySpec generateKeySpec(char[] password, byte[] key)
/*  374:     */     throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException
/*  375:     */   {
/*  376: 974 */     if (password == null) {
/*  377: 975 */       return new PKCS8EncodedKeySpec(key);
/*  378:     */     }
/*  379: 978 */     EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
/*  380: 979 */     SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
/*  381: 980 */     PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
/*  382: 981 */     SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
/*  383:     */     
/*  384: 983 */     Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
/*  385: 984 */     cipher.init(2, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());
/*  386:     */     
/*  387: 986 */     return encryptedPrivateKeyInfo.getKeySpec(cipher);
/*  388:     */   }
/*  389:     */   
/*  390:     */   static KeyStore buildKeyStore(X509Certificate[] certChain, PrivateKey key, char[] keyPasswordChars)
/*  391:     */     throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
/*  392:     */   {
/*  393:1001 */     KeyStore ks = KeyStore.getInstance("JKS");
/*  394:1002 */     ks.load(null, null);
/*  395:1003 */     ks.setKeyEntry("key", key, keyPasswordChars, certChain);
/*  396:1004 */     return ks;
/*  397:     */   }
/*  398:     */   
/*  399:     */   static PrivateKey toPrivateKey(File keyFile, String keyPassword)
/*  400:     */     throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException
/*  401:     */   {
/*  402:1011 */     if (keyFile == null) {
/*  403:1012 */       return null;
/*  404:     */     }
/*  405:1014 */     return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyFile), keyPassword);
/*  406:     */   }
/*  407:     */   
/*  408:     */   static PrivateKey toPrivateKey(InputStream keyInputStream, String keyPassword)
/*  409:     */     throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException
/*  410:     */   {
/*  411:1021 */     if (keyInputStream == null) {
/*  412:1022 */       return null;
/*  413:     */     }
/*  414:1024 */     return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyInputStream), keyPassword);
/*  415:     */   }
/*  416:     */   
/*  417:     */   private static PrivateKey getPrivateKeyFromByteBuffer(ByteBuf encodedKeyBuf, String keyPassword)
/*  418:     */     throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException
/*  419:     */   {
/*  420:1031 */     byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
/*  421:1032 */     encodedKeyBuf.readBytes(encodedKey).release();
/*  422:     */     
/*  423:1034 */     PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(keyPassword == null ? null : keyPassword
/*  424:1035 */       .toCharArray(), encodedKey);
/*  425:     */     try
/*  426:     */     {
/*  427:1037 */       return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
/*  428:     */     }
/*  429:     */     catch (InvalidKeySpecException ignore)
/*  430:     */     {
/*  431:     */       try
/*  432:     */       {
/*  433:1040 */         return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
/*  434:     */       }
/*  435:     */       catch (InvalidKeySpecException ignore2)
/*  436:     */       {
/*  437:     */         try
/*  438:     */         {
/*  439:1043 */           return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
/*  440:     */         }
/*  441:     */         catch (InvalidKeySpecException e)
/*  442:     */         {
/*  443:1045 */           throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
/*  444:     */         }
/*  445:     */       }
/*  446:     */     }
/*  447:     */   }
/*  448:     */   
/*  449:     */   @Deprecated
/*  450:     */   protected static TrustManagerFactory buildTrustManagerFactory(File certChainFile, TrustManagerFactory trustManagerFactory)
/*  451:     */     throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException
/*  452:     */   {
/*  453:1061 */     X509Certificate[] x509Certs = toX509Certificates(certChainFile);
/*  454:     */     
/*  455:1063 */     return buildTrustManagerFactory(x509Certs, trustManagerFactory);
/*  456:     */   }
/*  457:     */   
/*  458:     */   static X509Certificate[] toX509Certificates(File file)
/*  459:     */     throws CertificateException
/*  460:     */   {
/*  461:1067 */     if (file == null) {
/*  462:1068 */       return null;
/*  463:     */     }
/*  464:1070 */     return getCertificatesFromBuffers(PemReader.readCertificates(file));
/*  465:     */   }
/*  466:     */   
/*  467:     */   static X509Certificate[] toX509Certificates(InputStream in)
/*  468:     */     throws CertificateException
/*  469:     */   {
/*  470:1074 */     if (in == null) {
/*  471:1075 */       return null;
/*  472:     */     }
/*  473:1077 */     return getCertificatesFromBuffers(PemReader.readCertificates(in));
/*  474:     */   }
/*  475:     */   
/*  476:     */   private static X509Certificate[] getCertificatesFromBuffers(ByteBuf[] certs)
/*  477:     */     throws CertificateException
/*  478:     */   {
/*  479:1081 */     CertificateFactory cf = CertificateFactory.getInstance("X.509");
/*  480:1082 */     x509Certs = new X509Certificate[certs.length];
/*  481:     */     
/*  482:1084 */     int i = 0;
/*  483:     */     try
/*  484:     */     {
/*  485:1086 */       while (i < certs.length)
/*  486:     */       {
/*  487:1087 */         InputStream is = new ByteBufInputStream(certs[i], true);
/*  488:     */         try
/*  489:     */         {
/*  490:1089 */           x509Certs[i] = ((X509Certificate)cf.generateCertificate(is));
/*  491:     */           try
/*  492:     */           {
/*  493:1092 */             is.close();
/*  494:     */           }
/*  495:     */           catch (IOException e)
/*  496:     */           {
/*  497:1095 */             throw new RuntimeException(e);
/*  498:     */           }
/*  499:1086 */           i++;
/*  500:     */         }
/*  501:     */         finally
/*  502:     */         {
/*  503:     */           try
/*  504:     */           {
/*  505:1092 */             is.close();
/*  506:     */           }
/*  507:     */           catch (IOException e)
/*  508:     */           {
/*  509:1095 */             throw new RuntimeException(e);
/*  510:     */           }
/*  511:     */         }
/*  512:     */       }
/*  513:1100 */       for (; i < certs.length; i++) {
/*  514:1101 */         certs[i].release();
/*  515:     */       }
/*  516:1104 */       return x509Certs;
/*  517:     */     }
/*  518:     */     finally
/*  519:     */     {
/*  520:1100 */       for (; i < certs.length; i++) {
/*  521:1101 */         certs[i].release();
/*  522:     */       }
/*  523:     */     }
/*  524:     */   }
/*  525:     */   
/*  526:     */   static TrustManagerFactory buildTrustManagerFactory(X509Certificate[] certCollection, TrustManagerFactory trustManagerFactory)
/*  527:     */     throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException
/*  528:     */   {
/*  529:1110 */     KeyStore ks = KeyStore.getInstance("JKS");
/*  530:1111 */     ks.load(null, null);
/*  531:     */     
/*  532:1113 */     int i = 1;
/*  533:1114 */     for (X509Certificate cert : certCollection)
/*  534:     */     {
/*  535:1115 */       String alias = Integer.toString(i);
/*  536:1116 */       ks.setCertificateEntry(alias, cert);
/*  537:1117 */       i++;
/*  538:     */     }
/*  539:1121 */     if (trustManagerFactory == null) {
/*  540:1122 */       trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
/*  541:     */     }
/*  542:1124 */     trustManagerFactory.init(ks);
/*  543:     */     
/*  544:1126 */     return trustManagerFactory;
/*  545:     */   }
/*  546:     */   
/*  547:     */   static PrivateKey toPrivateKeyInternal(File keyFile, String keyPassword)
/*  548:     */     throws SSLException
/*  549:     */   {
/*  550:     */     try
/*  551:     */     {
/*  552:1131 */       return toPrivateKey(keyFile, keyPassword);
/*  553:     */     }
/*  554:     */     catch (Exception e)
/*  555:     */     {
/*  556:1133 */       throw new SSLException(e);
/*  557:     */     }
/*  558:     */   }
/*  559:     */   
/*  560:     */   static X509Certificate[] toX509CertificatesInternal(File file)
/*  561:     */     throws SSLException
/*  562:     */   {
/*  563:     */     try
/*  564:     */     {
/*  565:1139 */       return toX509Certificates(file);
/*  566:     */     }
/*  567:     */     catch (CertificateException e)
/*  568:     */     {
/*  569:1141 */       throw new SSLException(e);
/*  570:     */     }
/*  571:     */   }
/*  572:     */   
/*  573:     */   static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChain, PrivateKey key, String keyPassword, KeyManagerFactory kmf)
/*  574:     */     throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
/*  575:     */   {
/*  576:1149 */     String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
/*  577:1150 */     if (algorithm == null) {
/*  578:1151 */       algorithm = "SunX509";
/*  579:     */     }
/*  580:1153 */     return buildKeyManagerFactory(certChain, algorithm, key, keyPassword, kmf);
/*  581:     */   }
/*  582:     */   
/*  583:     */   static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChainFile, String keyAlgorithm, PrivateKey key, String keyPassword, KeyManagerFactory kmf)
/*  584:     */     throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableKeyException
/*  585:     */   {
/*  586:1161 */     char[] keyPasswordChars = keyPassword == null ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
/*  587:1162 */     KeyStore ks = buildKeyStore(certChainFile, key, keyPasswordChars);
/*  588:1164 */     if (kmf == null) {
/*  589:1165 */       kmf = KeyManagerFactory.getInstance(keyAlgorithm);
/*  590:     */     }
/*  591:1167 */     kmf.init(ks, keyPasswordChars);
/*  592:     */     
/*  593:1169 */     return kmf;
/*  594:     */   }
/*  595:     */   
/*  596:     */   public abstract boolean isClient();
/*  597:     */   
/*  598:     */   public abstract List<String> cipherSuites();
/*  599:     */   
/*  600:     */   public abstract long sessionCacheSize();
/*  601:     */   
/*  602:     */   public abstract long sessionTimeout();
/*  603:     */   
/*  604:     */   public abstract ApplicationProtocolNegotiator applicationProtocolNegotiator();
/*  605:     */   
/*  606:     */   public abstract SSLEngine newEngine(ByteBufAllocator paramByteBufAllocator);
/*  607:     */   
/*  608:     */   public abstract SSLEngine newEngine(ByteBufAllocator paramByteBufAllocator, String paramString, int paramInt);
/*  609:     */   
/*  610:     */   public abstract SSLSessionContext sessionContext();
/*  611:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SslContext
 * JD-Core Version:    0.7.0.1
 */