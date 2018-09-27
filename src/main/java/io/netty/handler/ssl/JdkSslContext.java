/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.io.File;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.security.InvalidAlgorithmParameterException;
/*  10:    */ import java.security.KeyException;
/*  11:    */ import java.security.KeyStoreException;
/*  12:    */ import java.security.NoSuchAlgorithmException;
/*  13:    */ import java.security.Security;
/*  14:    */ import java.security.UnrecoverableKeyException;
/*  15:    */ import java.security.cert.CertificateException;
/*  16:    */ import java.security.spec.InvalidKeySpecException;
/*  17:    */ import java.util.ArrayList;
/*  18:    */ import java.util.Arrays;
/*  19:    */ import java.util.Collections;
/*  20:    */ import java.util.HashSet;
/*  21:    */ import java.util.List;
/*  22:    */ import java.util.Set;
/*  23:    */ import javax.crypto.NoSuchPaddingException;
/*  24:    */ import javax.net.ssl.KeyManagerFactory;
/*  25:    */ import javax.net.ssl.SSLContext;
/*  26:    */ import javax.net.ssl.SSLEngine;
/*  27:    */ import javax.net.ssl.SSLSessionContext;
/*  28:    */ 
/*  29:    */ public class JdkSslContext
/*  30:    */   extends SslContext
/*  31:    */ {
/*  32: 56 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(JdkSslContext.class);
/*  33:    */   static final String PROTOCOL = "TLS";
/*  34:    */   private static final String[] DEFAULT_PROTOCOLS;
/*  35:    */   private static final List<String> DEFAULT_CIPHERS;
/*  36:    */   private static final Set<String> SUPPORTED_CIPHERS;
/*  37:    */   private final String[] protocols;
/*  38:    */   private final String[] cipherSuites;
/*  39:    */   private final List<String> unmodifiableCipherSuites;
/*  40:    */   private final JdkApplicationProtocolNegotiator apn;
/*  41:    */   private final ClientAuth clientAuth;
/*  42:    */   private final SSLContext sslContext;
/*  43:    */   private final boolean isClient;
/*  44:    */   
/*  45:    */   static
/*  46:    */   {
/*  47:    */     try
/*  48:    */     {
/*  49: 67 */       SSLContext context = SSLContext.getInstance("TLS");
/*  50: 68 */       context.init(null, null, null);
/*  51:    */     }
/*  52:    */     catch (Exception e)
/*  53:    */     {
/*  54: 70 */       throw new Error("failed to initialize the default SSL context", e);
/*  55:    */     }
/*  56:    */     SSLContext context;
/*  57: 73 */     SSLEngine engine = context.createSSLEngine();
/*  58:    */     
/*  59:    */ 
/*  60: 76 */     String[] supportedProtocols = engine.getSupportedProtocols();
/*  61: 77 */     Set<String> supportedProtocolsSet = new HashSet(supportedProtocols.length);
/*  62: 78 */     for (int i = 0; i < supportedProtocols.length; i++) {
/*  63: 79 */       supportedProtocolsSet.add(supportedProtocols[i]);
/*  64:    */     }
/*  65: 81 */     List<String> protocols = new ArrayList();
/*  66: 82 */     SslUtils.addIfSupported(supportedProtocolsSet, protocols, new String[] { "TLSv1.2", "TLSv1.1", "TLSv1" });
/*  67: 86 */     if (!protocols.isEmpty()) {
/*  68: 87 */       DEFAULT_PROTOCOLS = (String[])protocols.toArray(new String[protocols.size()]);
/*  69:    */     } else {
/*  70: 89 */       DEFAULT_PROTOCOLS = engine.getEnabledProtocols();
/*  71:    */     }
/*  72: 93 */     String[] supportedCiphers = engine.getSupportedCipherSuites();
/*  73: 94 */     SUPPORTED_CIPHERS = new HashSet(supportedCiphers.length);
/*  74: 95 */     for (i = 0; i < supportedCiphers.length; i++)
/*  75:    */     {
/*  76: 96 */       String supportedCipher = supportedCiphers[i];
/*  77: 97 */       SUPPORTED_CIPHERS.add(supportedCipher);
/*  78:107 */       if (supportedCipher.startsWith("SSL_")) {
/*  79:108 */         SUPPORTED_CIPHERS.add("TLS_" + supportedCipher.substring("SSL_".length()));
/*  80:    */       }
/*  81:    */     }
/*  82:111 */     List<String> ciphers = new ArrayList();
/*  83:112 */     SslUtils.addIfSupported(SUPPORTED_CIPHERS, ciphers, SslUtils.DEFAULT_CIPHER_SUITES);
/*  84:113 */     SslUtils.useFallbackCiphersIfDefaultIsEmpty(ciphers, engine.getEnabledCipherSuites());
/*  85:114 */     DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
/*  86:116 */     if (logger.isDebugEnabled())
/*  87:    */     {
/*  88:117 */       logger.debug("Default protocols (JDK): {} ", Arrays.asList(DEFAULT_PROTOCOLS));
/*  89:118 */       logger.debug("Default cipher suites (JDK): {}", DEFAULT_CIPHERS);
/*  90:    */     }
/*  91:    */   }
/*  92:    */   
/*  93:    */   public JdkSslContext(SSLContext sslContext, boolean isClient, ClientAuth clientAuth)
/*  94:    */   {
/*  95:140 */     this(sslContext, isClient, null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, clientAuth, null, false);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public JdkSslContext(SSLContext sslContext, boolean isClient, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, ClientAuth clientAuth)
/*  99:    */   {
/* 100:157 */     this(sslContext, isClient, ciphers, cipherFilter, toNegotiator(apn, !isClient), clientAuth, null, false);
/* 101:    */   }
/* 102:    */   
/* 103:    */   JdkSslContext(SSLContext sslContext, boolean isClient, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, ClientAuth clientAuth, String[] protocols, boolean startTls)
/* 104:    */   {
/* 105:163 */     super(startTls);
/* 106:164 */     this.apn = ((JdkApplicationProtocolNegotiator)ObjectUtil.checkNotNull(apn, "apn"));
/* 107:165 */     this.clientAuth = ((ClientAuth)ObjectUtil.checkNotNull(clientAuth, "clientAuth"));
/* 108:166 */     this.cipherSuites = ((CipherSuiteFilter)ObjectUtil.checkNotNull(cipherFilter, "cipherFilter")).filterCipherSuites(ciphers, DEFAULT_CIPHERS, SUPPORTED_CIPHERS);
/* 109:    */     
/* 110:168 */     this.protocols = (protocols == null ? DEFAULT_PROTOCOLS : protocols);
/* 111:169 */     this.unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(this.cipherSuites));
/* 112:170 */     this.sslContext = ((SSLContext)ObjectUtil.checkNotNull(sslContext, "sslContext"));
/* 113:171 */     this.isClient = isClient;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public final SSLContext context()
/* 117:    */   {
/* 118:178 */     return this.sslContext;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public final boolean isClient()
/* 122:    */   {
/* 123:183 */     return this.isClient;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public final SSLSessionContext sessionContext()
/* 127:    */   {
/* 128:191 */     if (isServer()) {
/* 129:192 */       return context().getServerSessionContext();
/* 130:    */     }
/* 131:194 */     return context().getClientSessionContext();
/* 132:    */   }
/* 133:    */   
/* 134:    */   public final List<String> cipherSuites()
/* 135:    */   {
/* 136:200 */     return this.unmodifiableCipherSuites;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public final long sessionCacheSize()
/* 140:    */   {
/* 141:205 */     return sessionContext().getSessionCacheSize();
/* 142:    */   }
/* 143:    */   
/* 144:    */   public final long sessionTimeout()
/* 145:    */   {
/* 146:210 */     return sessionContext().getSessionTimeout();
/* 147:    */   }
/* 148:    */   
/* 149:    */   public final SSLEngine newEngine(ByteBufAllocator alloc)
/* 150:    */   {
/* 151:215 */     return configureAndWrapEngine(context().createSSLEngine(), alloc);
/* 152:    */   }
/* 153:    */   
/* 154:    */   public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort)
/* 155:    */   {
/* 156:220 */     return configureAndWrapEngine(context().createSSLEngine(peerHost, peerPort), alloc);
/* 157:    */   }
/* 158:    */   
/* 159:    */   private SSLEngine configureAndWrapEngine(SSLEngine engine, ByteBufAllocator alloc)
/* 160:    */   {
/* 161:225 */     engine.setEnabledCipherSuites(this.cipherSuites);
/* 162:226 */     engine.setEnabledProtocols(this.protocols);
/* 163:227 */     engine.setUseClientMode(isClient());
/* 164:228 */     if (isServer()) {
/* 165:229 */       switch (1.$SwitchMap$io$netty$handler$ssl$ClientAuth[this.clientAuth.ordinal()])
/* 166:    */       {
/* 167:    */       case 1: 
/* 168:231 */         engine.setWantClientAuth(true);
/* 169:232 */         break;
/* 170:    */       case 2: 
/* 171:234 */         engine.setNeedClientAuth(true);
/* 172:235 */         break;
/* 173:    */       case 3: 
/* 174:    */         break;
/* 175:    */       default: 
/* 176:239 */         throw new Error("Unknown auth " + this.clientAuth);
/* 177:    */       }
/* 178:    */     }
/* 179:242 */     JdkApplicationProtocolNegotiator.SslEngineWrapperFactory factory = this.apn.wrapperFactory();
/* 180:243 */     if ((factory instanceof JdkApplicationProtocolNegotiator.AllocatorAwareSslEngineWrapperFactory)) {
/* 181:244 */       return 
/* 182:245 */         ((JdkApplicationProtocolNegotiator.AllocatorAwareSslEngineWrapperFactory)factory).wrapSslEngine(engine, alloc, this.apn, isServer());
/* 183:    */     }
/* 184:247 */     return factory.wrapSslEngine(engine, this.apn, isServer());
/* 185:    */   }
/* 186:    */   
/* 187:    */   public final JdkApplicationProtocolNegotiator applicationProtocolNegotiator()
/* 188:    */   {
/* 189:252 */     return this.apn;
/* 190:    */   }
/* 191:    */   
/* 192:    */   static JdkApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config, boolean isServer)
/* 193:    */   {
/* 194:263 */     if (config == null) {
/* 195:264 */       return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
/* 196:    */     }
/* 197:267 */     switch (1.$SwitchMap$io$netty$handler$ssl$ApplicationProtocolConfig$Protocol[config.protocol().ordinal()])
/* 198:    */     {
/* 199:    */     case 1: 
/* 200:269 */       return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
/* 201:    */     case 2: 
/* 202:271 */       if (isServer)
/* 203:    */       {
/* 204:272 */         switch (config.selectorFailureBehavior())
/* 205:    */         {
/* 206:    */         case FATAL_ALERT: 
/* 207:274 */           return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
/* 208:    */         case NO_ADVERTISE: 
/* 209:276 */           return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
/* 210:    */         }
/* 211:279 */         throw new UnsupportedOperationException("JDK provider does not support " + config.selectorFailureBehavior() + " failure behavior");
/* 212:    */       }
/* 213:282 */       switch (config.selectedListenerFailureBehavior())
/* 214:    */       {
/* 215:    */       case ACCEPT: 
/* 216:284 */         return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
/* 217:    */       case FATAL_ALERT: 
/* 218:286 */         return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
/* 219:    */       }
/* 220:289 */       throw new UnsupportedOperationException("JDK provider does not support " + config.selectedListenerFailureBehavior() + " failure behavior");
/* 221:    */     case 3: 
/* 222:293 */       if (isServer)
/* 223:    */       {
/* 224:294 */         switch (config.selectedListenerFailureBehavior())
/* 225:    */         {
/* 226:    */         case ACCEPT: 
/* 227:296 */           return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
/* 228:    */         case FATAL_ALERT: 
/* 229:298 */           return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
/* 230:    */         }
/* 231:301 */         throw new UnsupportedOperationException("JDK provider does not support " + config.selectedListenerFailureBehavior() + " failure behavior");
/* 232:    */       }
/* 233:304 */       switch (config.selectorFailureBehavior())
/* 234:    */       {
/* 235:    */       case FATAL_ALERT: 
/* 236:306 */         return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
/* 237:    */       case NO_ADVERTISE: 
/* 238:308 */         return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
/* 239:    */       }
/* 240:311 */       throw new UnsupportedOperationException("JDK provider does not support " + config.selectorFailureBehavior() + " failure behavior");
/* 241:    */     }
/* 242:316 */     throw new UnsupportedOperationException("JDK provider does not support " + config.protocol() + " protocol");
/* 243:    */   }
/* 244:    */   
/* 245:    */   @Deprecated
/* 246:    */   protected static KeyManagerFactory buildKeyManagerFactory(File certChainFile, File keyFile, String keyPassword, KeyManagerFactory kmf)
/* 247:    */     throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, CertificateException, KeyException, IOException
/* 248:    */   {
/* 249:336 */     String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
/* 250:337 */     if (algorithm == null) {
/* 251:338 */       algorithm = "SunX509";
/* 252:    */     }
/* 253:340 */     return buildKeyManagerFactory(certChainFile, algorithm, keyFile, keyPassword, kmf);
/* 254:    */   }
/* 255:    */   
/* 256:    */   @Deprecated
/* 257:    */   protected static KeyManagerFactory buildKeyManagerFactory(File certChainFile, String keyAlgorithm, File keyFile, String keyPassword, KeyManagerFactory kmf)
/* 258:    */     throws KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, IOException, CertificateException, KeyException, UnrecoverableKeyException
/* 259:    */   {
/* 260:363 */     return buildKeyManagerFactory(toX509Certificates(certChainFile), keyAlgorithm, 
/* 261:364 */       toPrivateKey(keyFile, keyPassword), keyPassword, kmf);
/* 262:    */   }
/* 263:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.JdkSslContext
 * JD-Core Version:    0.7.0.1
 */