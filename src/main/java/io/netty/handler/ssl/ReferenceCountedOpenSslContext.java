/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.internal.tcnative.CertificateVerifier;
/*   6:    */ import io.netty.internal.tcnative.SSL;
/*   7:    */ import io.netty.internal.tcnative.SSLContext;
/*   8:    */ import io.netty.util.AbstractReferenceCounted;
/*   9:    */ import io.netty.util.ReferenceCounted;
/*  10:    */ import io.netty.util.ResourceLeakDetector;
/*  11:    */ import io.netty.util.ResourceLeakDetectorFactory;
/*  12:    */ import io.netty.util.ResourceLeakTracker;
/*  13:    */ import io.netty.util.internal.ObjectUtil;
/*  14:    */ import io.netty.util.internal.PlatformDependent;
/*  15:    */ import io.netty.util.internal.SystemPropertyUtil;
/*  16:    */ import io.netty.util.internal.logging.InternalLogger;
/*  17:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  18:    */ import java.security.AccessController;
/*  19:    */ import java.security.PrivateKey;
/*  20:    */ import java.security.PrivilegedAction;
/*  21:    */ import java.security.cert.CertPathValidatorException;
/*  22:    */ import java.security.cert.CertPathValidatorException.BasicReason;
/*  23:    */ import java.security.cert.CertPathValidatorException.Reason;
/*  24:    */ import java.security.cert.Certificate;
/*  25:    */ import java.security.cert.CertificateExpiredException;
/*  26:    */ import java.security.cert.CertificateNotYetValidException;
/*  27:    */ import java.security.cert.CertificateRevokedException;
/*  28:    */ import java.security.cert.X509Certificate;
/*  29:    */ import java.util.Arrays;
/*  30:    */ import java.util.Collections;
/*  31:    */ import java.util.List;
/*  32:    */ import java.util.Map;
/*  33:    */ import java.util.concurrent.locks.Lock;
/*  34:    */ import java.util.concurrent.locks.ReadWriteLock;
/*  35:    */ import java.util.concurrent.locks.ReentrantReadWriteLock;
/*  36:    */ import javax.net.ssl.KeyManager;
/*  37:    */ import javax.net.ssl.SSLEngine;
/*  38:    */ import javax.net.ssl.SSLException;
/*  39:    */ import javax.net.ssl.SSLHandshakeException;
/*  40:    */ import javax.net.ssl.TrustManager;
/*  41:    */ import javax.net.ssl.X509ExtendedKeyManager;
/*  42:    */ import javax.net.ssl.X509ExtendedTrustManager;
/*  43:    */ import javax.net.ssl.X509KeyManager;
/*  44:    */ import javax.net.ssl.X509TrustManager;
/*  45:    */ 
/*  46:    */ public abstract class ReferenceCountedOpenSslContext
/*  47:    */   extends SslContext
/*  48:    */   implements ReferenceCounted
/*  49:    */ {
/*  50: 77 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslContext.class);
/*  51: 87 */   private static final boolean JDK_REJECT_CLIENT_INITIATED_RENEGOTIATION = ((Boolean)AccessController.doPrivileged(new PrivilegedAction()
/*  52:    */   {
/*  53:    */     public Boolean run()
/*  54:    */     {
/*  55: 90 */       return Boolean.valueOf(SystemPropertyUtil.getBoolean("jdk.tls.rejectClientInitiatedRenegotiation", false));
/*  56:    */     }
/*  57: 87 */   })).booleanValue();
/*  58: 95 */   private static final int DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE = ((Integer)AccessController.doPrivileged(new PrivilegedAction()
/*  59:    */   {
/*  60:    */     public Integer run()
/*  61:    */     {
/*  62: 98 */       return Integer.valueOf(Math.max(1, 
/*  63: 99 */         SystemPropertyUtil.getInt("io.netty.handler.ssl.openssl.bioNonApplicationBufferSize", 2048)));
/*  64:    */     }
/*  65: 95 */   })).intValue();
/*  66:    */   private static final Integer DH_KEY_LENGTH;
/*  67:106 */   private static final ResourceLeakDetector<ReferenceCountedOpenSslContext> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ReferenceCountedOpenSslContext.class);
/*  68:    */   protected static final int VERIFY_DEPTH = 10;
/*  69:    */   protected long ctx;
/*  70:    */   private final List<String> unmodifiableCiphers;
/*  71:    */   private final long sessionCacheSize;
/*  72:    */   private final long sessionTimeout;
/*  73:    */   private final OpenSslApplicationProtocolNegotiator apn;
/*  74:    */   private final int mode;
/*  75:    */   private final ResourceLeakTracker<ReferenceCountedOpenSslContext> leak;
/*  76:125 */   private final AbstractReferenceCounted refCnt = new AbstractReferenceCounted()
/*  77:    */   {
/*  78:    */     public ReferenceCounted touch(Object hint)
/*  79:    */     {
/*  80:128 */       if (ReferenceCountedOpenSslContext.this.leak != null) {
/*  81:129 */         ReferenceCountedOpenSslContext.this.leak.record(hint);
/*  82:    */       }
/*  83:132 */       return ReferenceCountedOpenSslContext.this;
/*  84:    */     }
/*  85:    */     
/*  86:    */     protected void deallocate()
/*  87:    */     {
/*  88:137 */       ReferenceCountedOpenSslContext.this.destroy();
/*  89:138 */       if (ReferenceCountedOpenSslContext.this.leak != null)
/*  90:    */       {
/*  91:139 */         boolean closed = ReferenceCountedOpenSslContext.this.leak.close(ReferenceCountedOpenSslContext.this);
/*  92:140 */         assert (closed);
/*  93:    */       }
/*  94:    */     }
/*  95:    */   };
/*  96:    */   final Certificate[] keyCertChain;
/*  97:    */   final ClientAuth clientAuth;
/*  98:    */   final String[] protocols;
/*  99:    */   final boolean enableOcsp;
/* 100:149 */   final OpenSslEngineMap engineMap = new DefaultOpenSslEngineMap(null);
/* 101:150 */   final ReadWriteLock ctxLock = new ReentrantReadWriteLock();
/* 102:    */   private volatile boolean rejectRemoteInitiatedRenegotiation;
/* 103:153 */   private volatile int bioNonApplicationBufferSize = DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE;
/* 104:156 */   static final OpenSslApplicationProtocolNegotiator NONE_PROTOCOL_NEGOTIATOR = new OpenSslApplicationProtocolNegotiator()
/* 105:    */   {
/* 106:    */     public ApplicationProtocolConfig.Protocol protocol()
/* 107:    */     {
/* 108:160 */       return ApplicationProtocolConfig.Protocol.NONE;
/* 109:    */     }
/* 110:    */     
/* 111:    */     public List<String> protocols()
/* 112:    */     {
/* 113:165 */       return Collections.emptyList();
/* 114:    */     }
/* 115:    */     
/* 116:    */     public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior()
/* 117:    */     {
/* 118:170 */       return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
/* 119:    */     }
/* 120:    */     
/* 121:    */     public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior()
/* 122:    */     {
/* 123:175 */       return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
/* 124:    */     }
/* 125:    */   };
/* 126:    */   
/* 127:    */   static
/* 128:    */   {
/* 129:180 */     Integer dhLen = null;
/* 130:    */     try
/* 131:    */     {
/* 132:183 */       String dhKeySize = (String)AccessController.doPrivileged(new PrivilegedAction()
/* 133:    */       {
/* 134:    */         public String run()
/* 135:    */         {
/* 136:186 */           return SystemPropertyUtil.get("jdk.tls.ephemeralDHKeySize");
/* 137:    */         }
/* 138:    */       });
/* 139:189 */       if (dhKeySize != null) {
/* 140:    */         try
/* 141:    */         {
/* 142:191 */           dhLen = Integer.valueOf(dhKeySize);
/* 143:    */         }
/* 144:    */         catch (NumberFormatException e)
/* 145:    */         {
/* 146:193 */           logger.debug("ReferenceCountedOpenSslContext supports -Djdk.tls.ephemeralDHKeySize={int}, but got: " + dhKeySize);
/* 147:    */         }
/* 148:    */       }
/* 149:    */     }
/* 150:    */     catch (Throwable localThrowable) {}
/* 151:200 */     DH_KEY_LENGTH = dhLen;
/* 152:    */   }
/* 153:    */   
/* 154:    */   ReferenceCountedOpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apnCfg, long sessionCacheSize, long sessionTimeout, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, boolean leakDetection)
/* 155:    */     throws SSLException
/* 156:    */   {
/* 157:207 */     this(ciphers, cipherFilter, toNegotiator(apnCfg), sessionCacheSize, sessionTimeout, mode, keyCertChain, clientAuth, protocols, startTls, enableOcsp, leakDetection);
/* 158:    */   }
/* 159:    */   
/* 160:    */   ReferenceCountedOpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, boolean leakDetection)
/* 161:    */     throws SSLException
/* 162:    */   {
/* 163:216 */     super(startTls);
/* 164:    */     
/* 165:218 */     OpenSsl.ensureAvailability();
/* 166:220 */     if ((enableOcsp) && (!OpenSsl.isOcspSupported())) {
/* 167:221 */       throw new IllegalStateException("OCSP is not supported.");
/* 168:    */     }
/* 169:224 */     if ((mode != 1) && (mode != 0)) {
/* 170:225 */       throw new IllegalArgumentException("mode most be either SSL.SSL_MODE_SERVER or SSL.SSL_MODE_CLIENT");
/* 171:    */     }
/* 172:227 */     this.leak = (leakDetection ? leakDetector.track(this) : null);
/* 173:228 */     this.mode = mode;
/* 174:229 */     this.clientAuth = (isServer() ? (ClientAuth)ObjectUtil.checkNotNull(clientAuth, "clientAuth") : ClientAuth.NONE);
/* 175:230 */     this.protocols = protocols;
/* 176:231 */     this.enableOcsp = enableOcsp;
/* 177:233 */     if (mode == 1) {
/* 178:234 */       this.rejectRemoteInitiatedRenegotiation = JDK_REJECT_CLIENT_INITIATED_RENEGOTIATION;
/* 179:    */     }
/* 180:237 */     this.keyCertChain = (keyCertChain == null ? null : (Certificate[])keyCertChain.clone());
/* 181:    */     
/* 182:239 */     this.unmodifiableCiphers = Arrays.asList(((CipherSuiteFilter)ObjectUtil.checkNotNull(cipherFilter, "cipherFilter")).filterCipherSuites(ciphers, OpenSsl.DEFAULT_CIPHERS, 
/* 183:240 */       OpenSsl.availableJavaCipherSuites()));
/* 184:    */     
/* 185:242 */     this.apn = ((OpenSslApplicationProtocolNegotiator)ObjectUtil.checkNotNull(apn, "apn"));
/* 186:    */     
/* 187:    */ 
/* 188:245 */     boolean success = false;
/* 189:    */     try
/* 190:    */     {
/* 191:    */       try
/* 192:    */       {
/* 193:248 */         this.ctx = SSLContext.make(31, mode);
/* 194:    */       }
/* 195:    */       catch (Exception e)
/* 196:    */       {
/* 197:250 */         throw new SSLException("failed to create an SSL_CTX", e);
/* 198:    */       }
/* 199:253 */       SSLContext.setOptions(this.ctx, SSLContext.getOptions(this.ctx) | SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_CIPHER_SERVER_PREFERENCE | SSL.SSL_OP_NO_COMPRESSION | SSL.SSL_OP_NO_TICKET);
/* 200:    */       
/* 201:    */ 
/* 202:    */ 
/* 203:    */ 
/* 204:    */ 
/* 205:    */ 
/* 206:    */ 
/* 207:    */ 
/* 208:    */ 
/* 209:    */ 
/* 210:    */ 
/* 211:    */ 
/* 212:    */ 
/* 213:    */ 
/* 214:    */ 
/* 215:    */ 
/* 216:270 */       SSLContext.setMode(this.ctx, SSLContext.getMode(this.ctx) | SSL.SSL_MODE_ACCEPT_MOVING_WRITE_BUFFER);
/* 217:272 */       if (DH_KEY_LENGTH != null) {
/* 218:273 */         SSLContext.setTmpDHLength(this.ctx, DH_KEY_LENGTH.intValue());
/* 219:    */       }
/* 220:    */       try
/* 221:    */       {
/* 222:278 */         SSLContext.setCipherSuite(this.ctx, CipherSuiteConverter.toOpenSsl(this.unmodifiableCiphers));
/* 223:    */       }
/* 224:    */       catch (SSLException e)
/* 225:    */       {
/* 226:280 */         throw e;
/* 227:    */       }
/* 228:    */       catch (Exception e)
/* 229:    */       {
/* 230:282 */         throw new SSLException("failed to set cipher suite: " + this.unmodifiableCiphers, e);
/* 231:    */       }
/* 232:285 */       List<String> nextProtoList = apn.protocols();
/* 233:287 */       if (!nextProtoList.isEmpty())
/* 234:    */       {
/* 235:288 */         String[] appProtocols = (String[])nextProtoList.toArray(new String[nextProtoList.size()]);
/* 236:289 */         int selectorBehavior = opensslSelectorFailureBehavior(apn.selectorFailureBehavior());
/* 237:291 */         switch (6.$SwitchMap$io$netty$handler$ssl$ApplicationProtocolConfig$Protocol[apn.protocol().ordinal()])
/* 238:    */         {
/* 239:    */         case 1: 
/* 240:293 */           SSLContext.setNpnProtos(this.ctx, appProtocols, selectorBehavior);
/* 241:294 */           break;
/* 242:    */         case 2: 
/* 243:296 */           SSLContext.setAlpnProtos(this.ctx, appProtocols, selectorBehavior);
/* 244:297 */           break;
/* 245:    */         case 3: 
/* 246:299 */           SSLContext.setNpnProtos(this.ctx, appProtocols, selectorBehavior);
/* 247:300 */           SSLContext.setAlpnProtos(this.ctx, appProtocols, selectorBehavior);
/* 248:301 */           break;
/* 249:    */         default: 
/* 250:303 */           throw new Error();
/* 251:    */         }
/* 252:    */       }
/* 253:308 */       if (sessionCacheSize > 0L)
/* 254:    */       {
/* 255:309 */         this.sessionCacheSize = sessionCacheSize;
/* 256:310 */         SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
/* 257:    */       }
/* 258:    */       else
/* 259:    */       {
/* 260:313 */         this.sessionCacheSize = (sessionCacheSize = SSLContext.setSessionCacheSize(this.ctx, 20480L));
/* 261:    */         
/* 262:315 */         SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
/* 263:    */       }
/* 264:319 */       if (sessionTimeout > 0L)
/* 265:    */       {
/* 266:320 */         this.sessionTimeout = sessionTimeout;
/* 267:321 */         SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
/* 268:    */       }
/* 269:    */       else
/* 270:    */       {
/* 271:324 */         this.sessionTimeout = (sessionTimeout = SSLContext.setSessionCacheTimeout(this.ctx, 300L));
/* 272:    */         
/* 273:326 */         SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
/* 274:    */       }
/* 275:329 */       if (enableOcsp) {
/* 276:330 */         SSLContext.enableOcsp(this.ctx, isClient());
/* 277:    */       }
/* 278:332 */       success = true;
/* 279:    */     }
/* 280:    */     finally
/* 281:    */     {
/* 282:334 */       if (!success) {
/* 283:335 */         release();
/* 284:    */       }
/* 285:    */     }
/* 286:    */   }
/* 287:    */   
/* 288:    */   private static int opensslSelectorFailureBehavior(ApplicationProtocolConfig.SelectorFailureBehavior behavior)
/* 289:    */   {
/* 290:341 */     switch (behavior)
/* 291:    */     {
/* 292:    */     case NO_ADVERTISE: 
/* 293:343 */       return 0;
/* 294:    */     case CHOOSE_MY_LAST_PROTOCOL: 
/* 295:345 */       return 1;
/* 296:    */     }
/* 297:347 */     throw new Error();
/* 298:    */   }
/* 299:    */   
/* 300:    */   public final List<String> cipherSuites()
/* 301:    */   {
/* 302:353 */     return this.unmodifiableCiphers;
/* 303:    */   }
/* 304:    */   
/* 305:    */   public final long sessionCacheSize()
/* 306:    */   {
/* 307:358 */     return this.sessionCacheSize;
/* 308:    */   }
/* 309:    */   
/* 310:    */   public final long sessionTimeout()
/* 311:    */   {
/* 312:363 */     return this.sessionTimeout;
/* 313:    */   }
/* 314:    */   
/* 315:    */   public ApplicationProtocolNegotiator applicationProtocolNegotiator()
/* 316:    */   {
/* 317:368 */     return this.apn;
/* 318:    */   }
/* 319:    */   
/* 320:    */   public final boolean isClient()
/* 321:    */   {
/* 322:373 */     return this.mode == 0;
/* 323:    */   }
/* 324:    */   
/* 325:    */   public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort)
/* 326:    */   {
/* 327:378 */     return newEngine0(alloc, peerHost, peerPort, true);
/* 328:    */   }
/* 329:    */   
/* 330:    */   protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls)
/* 331:    */   {
/* 332:383 */     return new SslHandler(newEngine0(alloc, null, -1, false), startTls);
/* 333:    */   }
/* 334:    */   
/* 335:    */   protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls)
/* 336:    */   {
/* 337:388 */     return new SslHandler(newEngine0(alloc, peerHost, peerPort, false), startTls);
/* 338:    */   }
/* 339:    */   
/* 340:    */   SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode)
/* 341:    */   {
/* 342:392 */     return new ReferenceCountedOpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode, true);
/* 343:    */   }
/* 344:    */   
/* 345:    */   public final SSLEngine newEngine(ByteBufAllocator alloc)
/* 346:    */   {
/* 347:402 */     return newEngine(alloc, null, -1);
/* 348:    */   }
/* 349:    */   
/* 350:    */   @Deprecated
/* 351:    */   public final long context()
/* 352:    */   {
/* 353:414 */     Lock readerLock = this.ctxLock.readLock();
/* 354:415 */     readerLock.lock();
/* 355:    */     try
/* 356:    */     {
/* 357:417 */       return this.ctx;
/* 358:    */     }
/* 359:    */     finally
/* 360:    */     {
/* 361:419 */       readerLock.unlock();
/* 362:    */     }
/* 363:    */   }
/* 364:    */   
/* 365:    */   @Deprecated
/* 366:    */   public final OpenSslSessionStats stats()
/* 367:    */   {
/* 368:430 */     return sessionContext().stats();
/* 369:    */   }
/* 370:    */   
/* 371:    */   public void setRejectRemoteInitiatedRenegotiation(boolean rejectRemoteInitiatedRenegotiation)
/* 372:    */   {
/* 373:438 */     this.rejectRemoteInitiatedRenegotiation = rejectRemoteInitiatedRenegotiation;
/* 374:    */   }
/* 375:    */   
/* 376:    */   public boolean getRejectRemoteInitiatedRenegotiation()
/* 377:    */   {
/* 378:445 */     return this.rejectRemoteInitiatedRenegotiation;
/* 379:    */   }
/* 380:    */   
/* 381:    */   public void setBioNonApplicationBufferSize(int bioNonApplicationBufferSize)
/* 382:    */   {
/* 383:454 */     this.bioNonApplicationBufferSize = ObjectUtil.checkPositiveOrZero(bioNonApplicationBufferSize, "bioNonApplicationBufferSize");
/* 384:    */   }
/* 385:    */   
/* 386:    */   public int getBioNonApplicationBufferSize()
/* 387:    */   {
/* 388:461 */     return this.bioNonApplicationBufferSize;
/* 389:    */   }
/* 390:    */   
/* 391:    */   @Deprecated
/* 392:    */   public final void setTicketKeys(byte[] keys)
/* 393:    */   {
/* 394:471 */     sessionContext().setTicketKeys(keys);
/* 395:    */   }
/* 396:    */   
/* 397:    */   @Deprecated
/* 398:    */   public final long sslCtxPointer()
/* 399:    */   {
/* 400:486 */     Lock readerLock = this.ctxLock.readLock();
/* 401:487 */     readerLock.lock();
/* 402:    */     try
/* 403:    */     {
/* 404:489 */       return this.ctx;
/* 405:    */     }
/* 406:    */     finally
/* 407:    */     {
/* 408:491 */       readerLock.unlock();
/* 409:    */     }
/* 410:    */   }
/* 411:    */   
/* 412:    */   private void destroy()
/* 413:    */   {
/* 414:499 */     Lock writerLock = this.ctxLock.writeLock();
/* 415:500 */     writerLock.lock();
/* 416:    */     try
/* 417:    */     {
/* 418:502 */       if (this.ctx != 0L)
/* 419:    */       {
/* 420:503 */         if (this.enableOcsp) {
/* 421:504 */           SSLContext.disableOcsp(this.ctx);
/* 422:    */         }
/* 423:507 */         SSLContext.free(this.ctx);
/* 424:508 */         this.ctx = 0L;
/* 425:    */       }
/* 426:511 */       writerLock.unlock();
/* 427:    */     }
/* 428:    */     finally
/* 429:    */     {
/* 430:511 */       writerLock.unlock();
/* 431:    */     }
/* 432:    */   }
/* 433:    */   
/* 434:    */   protected static X509Certificate[] certificates(byte[][] chain)
/* 435:    */   {
/* 436:516 */     X509Certificate[] peerCerts = new X509Certificate[chain.length];
/* 437:517 */     for (int i = 0; i < peerCerts.length; i++) {
/* 438:518 */       peerCerts[i] = new OpenSslX509Certificate(chain[i]);
/* 439:    */     }
/* 440:520 */     return peerCerts;
/* 441:    */   }
/* 442:    */   
/* 443:    */   protected static X509TrustManager chooseTrustManager(TrustManager[] managers)
/* 444:    */   {
/* 445:524 */     for (TrustManager m : managers) {
/* 446:525 */       if ((m instanceof X509TrustManager)) {
/* 447:526 */         return (X509TrustManager)m;
/* 448:    */       }
/* 449:    */     }
/* 450:529 */     throw new IllegalStateException("no X509TrustManager found");
/* 451:    */   }
/* 452:    */   
/* 453:    */   protected static X509KeyManager chooseX509KeyManager(KeyManager[] kms)
/* 454:    */   {
/* 455:533 */     for (KeyManager km : kms) {
/* 456:534 */       if ((km instanceof X509KeyManager)) {
/* 457:535 */         return (X509KeyManager)km;
/* 458:    */       }
/* 459:    */     }
/* 460:538 */     throw new IllegalStateException("no X509KeyManager found");
/* 461:    */   }
/* 462:    */   
/* 463:    */   static OpenSslApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config)
/* 464:    */   {
/* 465:550 */     if (config == null) {
/* 466:551 */       return NONE_PROTOCOL_NEGOTIATOR;
/* 467:    */     }
/* 468:554 */     switch (6.$SwitchMap$io$netty$handler$ssl$ApplicationProtocolConfig$Protocol[config.protocol().ordinal()])
/* 469:    */     {
/* 470:    */     case 4: 
/* 471:556 */       return NONE_PROTOCOL_NEGOTIATOR;
/* 472:    */     case 1: 
/* 473:    */     case 2: 
/* 474:    */     case 3: 
/* 475:560 */       switch (config.selectedListenerFailureBehavior())
/* 476:    */       {
/* 477:    */       case CHOOSE_MY_LAST_PROTOCOL: 
/* 478:    */       case ACCEPT: 
/* 479:563 */         switch (config.selectorFailureBehavior())
/* 480:    */         {
/* 481:    */         case NO_ADVERTISE: 
/* 482:    */         case CHOOSE_MY_LAST_PROTOCOL: 
/* 483:566 */           return new OpenSslDefaultApplicationProtocolNegotiator(config);
/* 484:    */         }
/* 485:572 */         throw new UnsupportedOperationException("OpenSSL provider does not support " + config.selectorFailureBehavior() + " behavior");
/* 486:    */       }
/* 487:578 */       throw new UnsupportedOperationException("OpenSSL provider does not support " + config.selectedListenerFailureBehavior() + " behavior");
/* 488:    */     }
/* 489:581 */     throw new Error();
/* 490:    */   }
/* 491:    */   
/* 492:    */   static boolean useExtendedTrustManager(X509TrustManager trustManager)
/* 493:    */   {
/* 494:586 */     return (PlatformDependent.javaVersion() >= 7) && ((trustManager instanceof X509ExtendedTrustManager));
/* 495:    */   }
/* 496:    */   
/* 497:    */   static boolean useExtendedKeyManager(X509KeyManager keyManager)
/* 498:    */   {
/* 499:590 */     return (PlatformDependent.javaVersion() >= 7) && ((keyManager instanceof X509ExtendedKeyManager));
/* 500:    */   }
/* 501:    */   
/* 502:    */   public final int refCnt()
/* 503:    */   {
/* 504:595 */     return this.refCnt.refCnt();
/* 505:    */   }
/* 506:    */   
/* 507:    */   public final ReferenceCounted retain()
/* 508:    */   {
/* 509:600 */     this.refCnt.retain();
/* 510:601 */     return this;
/* 511:    */   }
/* 512:    */   
/* 513:    */   public final ReferenceCounted retain(int increment)
/* 514:    */   {
/* 515:606 */     this.refCnt.retain(increment);
/* 516:607 */     return this;
/* 517:    */   }
/* 518:    */   
/* 519:    */   public final ReferenceCounted touch()
/* 520:    */   {
/* 521:612 */     this.refCnt.touch();
/* 522:613 */     return this;
/* 523:    */   }
/* 524:    */   
/* 525:    */   public final ReferenceCounted touch(Object hint)
/* 526:    */   {
/* 527:618 */     this.refCnt.touch(hint);
/* 528:619 */     return this;
/* 529:    */   }
/* 530:    */   
/* 531:    */   public final boolean release()
/* 532:    */   {
/* 533:624 */     return this.refCnt.release();
/* 534:    */   }
/* 535:    */   
/* 536:    */   public final boolean release(int decrement)
/* 537:    */   {
/* 538:629 */     return this.refCnt.release(decrement);
/* 539:    */   }
/* 540:    */   
/* 541:    */   static abstract class AbstractCertificateVerifier
/* 542:    */     extends CertificateVerifier
/* 543:    */   {
/* 544:    */     private final OpenSslEngineMap engineMap;
/* 545:    */     
/* 546:    */     AbstractCertificateVerifier(OpenSslEngineMap engineMap)
/* 547:    */     {
/* 548:636 */       this.engineMap = engineMap;
/* 549:    */     }
/* 550:    */     
/* 551:    */     public final int verify(long ssl, byte[][] chain, String auth)
/* 552:    */     {
/* 553:641 */       X509Certificate[] peerCerts = ReferenceCountedOpenSslContext.certificates(chain);
/* 554:642 */       ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
/* 555:    */       try
/* 556:    */       {
/* 557:644 */         verify(engine, peerCerts, auth);
/* 558:645 */         return CertificateVerifier.X509_V_OK;
/* 559:    */       }
/* 560:    */       catch (Throwable cause)
/* 561:    */       {
/* 562:647 */         ReferenceCountedOpenSslContext.logger.debug("verification of certificate failed", cause);
/* 563:648 */         SSLHandshakeException e = new SSLHandshakeException("General OpenSslEngine problem");
/* 564:649 */         e.initCause(cause);
/* 565:650 */         engine.handshakeException = e;
/* 566:653 */         if ((cause instanceof OpenSslCertificateException)) {
/* 567:656 */           return ((OpenSslCertificateException)cause).errorCode();
/* 568:    */         }
/* 569:658 */         if ((cause instanceof CertificateExpiredException)) {
/* 570:659 */           return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED;
/* 571:    */         }
/* 572:661 */         if ((cause instanceof CertificateNotYetValidException)) {
/* 573:662 */           return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID;
/* 574:    */         }
/* 575:664 */         if (PlatformDependent.javaVersion() >= 7)
/* 576:    */         {
/* 577:665 */           if ((cause instanceof CertificateRevokedException)) {
/* 578:666 */             return CertificateVerifier.X509_V_ERR_CERT_REVOKED;
/* 579:    */           }
/* 580:672 */           Throwable wrapped = cause.getCause();
/* 581:673 */           while (wrapped != null)
/* 582:    */           {
/* 583:674 */             if ((wrapped instanceof CertPathValidatorException))
/* 584:    */             {
/* 585:675 */               CertPathValidatorException ex = (CertPathValidatorException)wrapped;
/* 586:676 */               CertPathValidatorException.Reason reason = ex.getReason();
/* 587:677 */               if (reason == CertPathValidatorException.BasicReason.EXPIRED) {
/* 588:678 */                 return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED;
/* 589:    */               }
/* 590:680 */               if (reason == CertPathValidatorException.BasicReason.NOT_YET_VALID) {
/* 591:681 */                 return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID;
/* 592:    */               }
/* 593:683 */               if (reason == CertPathValidatorException.BasicReason.REVOKED) {
/* 594:684 */                 return CertificateVerifier.X509_V_ERR_CERT_REVOKED;
/* 595:    */               }
/* 596:    */             }
/* 597:687 */             wrapped = wrapped.getCause();
/* 598:    */           }
/* 599:    */         }
/* 600:    */       }
/* 601:692 */       return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
/* 602:    */     }
/* 603:    */     
/* 604:    */     abstract void verify(ReferenceCountedOpenSslEngine paramReferenceCountedOpenSslEngine, X509Certificate[] paramArrayOfX509Certificate, String paramString)
/* 605:    */       throws Exception;
/* 606:    */   }
/* 607:    */   
/* 608:    */   private static final class DefaultOpenSslEngineMap
/* 609:    */     implements OpenSslEngineMap
/* 610:    */   {
/* 611:701 */     private final Map<Long, ReferenceCountedOpenSslEngine> engines = PlatformDependent.newConcurrentHashMap();
/* 612:    */     
/* 613:    */     public ReferenceCountedOpenSslEngine remove(long ssl)
/* 614:    */     {
/* 615:705 */       return (ReferenceCountedOpenSslEngine)this.engines.remove(Long.valueOf(ssl));
/* 616:    */     }
/* 617:    */     
/* 618:    */     public void add(ReferenceCountedOpenSslEngine engine)
/* 619:    */     {
/* 620:710 */       this.engines.put(Long.valueOf(engine.sslPointer()), engine);
/* 621:    */     }
/* 622:    */     
/* 623:    */     public ReferenceCountedOpenSslEngine get(long ssl)
/* 624:    */     {
/* 625:715 */       return (ReferenceCountedOpenSslEngine)this.engines.get(Long.valueOf(ssl));
/* 626:    */     }
/* 627:    */   }
/* 628:    */   
/* 629:    */   static void setKeyMaterial(long ctx, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword)
/* 630:    */     throws SSLException
/* 631:    */   {
/* 632:722 */     long keyBio = 0L;
/* 633:723 */     long keyCertChainBio = 0L;
/* 634:724 */     long keyCertChainBio2 = 0L;
/* 635:725 */     PemEncoded encoded = null;
/* 636:    */     try
/* 637:    */     {
/* 638:728 */       encoded = PemX509Certificate.toPEM(ByteBufAllocator.DEFAULT, true, keyCertChain);
/* 639:729 */       keyCertChainBio = toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
/* 640:730 */       keyCertChainBio2 = toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
/* 641:732 */       if (key != null) {
/* 642:733 */         keyBio = toBIO(key);
/* 643:    */       }
/* 644:736 */       SSLContext.setCertificateBio(ctx, keyCertChainBio, keyBio, keyPassword == null ? "" : keyPassword);
/* 645:    */       
/* 646:    */ 
/* 647:    */ 
/* 648:740 */       SSLContext.setCertificateChainBio(ctx, keyCertChainBio2, true);
/* 649:    */     }
/* 650:    */     catch (SSLException e)
/* 651:    */     {
/* 652:742 */       throw e;
/* 653:    */     }
/* 654:    */     catch (Exception e)
/* 655:    */     {
/* 656:744 */       throw new SSLException("failed to set certificate and key", e);
/* 657:    */     }
/* 658:    */     finally
/* 659:    */     {
/* 660:746 */       freeBio(keyBio);
/* 661:747 */       freeBio(keyCertChainBio);
/* 662:748 */       freeBio(keyCertChainBio2);
/* 663:749 */       if (encoded != null) {
/* 664:750 */         encoded.release();
/* 665:    */       }
/* 666:    */     }
/* 667:    */   }
/* 668:    */   
/* 669:    */   static void freeBio(long bio)
/* 670:    */   {
/* 671:756 */     if (bio != 0L) {
/* 672:757 */       SSL.freeBIO(bio);
/* 673:    */     }
/* 674:    */   }
/* 675:    */   
/* 676:    */   static long toBIO(PrivateKey key)
/* 677:    */     throws Exception
/* 678:    */   {
/* 679:766 */     if (key == null) {
/* 680:767 */       return 0L;
/* 681:    */     }
/* 682:770 */     ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
/* 683:771 */     PemEncoded pem = PemPrivateKey.toPEM(allocator, true, key);
/* 684:    */     try
/* 685:    */     {
/* 686:773 */       return toBIO(allocator, pem.retain());
/* 687:    */     }
/* 688:    */     finally
/* 689:    */     {
/* 690:775 */       pem.release();
/* 691:    */     }
/* 692:    */   }
/* 693:    */   
/* 694:    */   static long toBIO(X509Certificate... certChain)
/* 695:    */     throws Exception
/* 696:    */   {
/* 697:784 */     if (certChain == null) {
/* 698:785 */       return 0L;
/* 699:    */     }
/* 700:788 */     if (certChain.length == 0) {
/* 701:789 */       throw new IllegalArgumentException("certChain can't be empty");
/* 702:    */     }
/* 703:792 */     ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
/* 704:793 */     PemEncoded pem = PemX509Certificate.toPEM(allocator, true, certChain);
/* 705:    */     try
/* 706:    */     {
/* 707:795 */       return toBIO(allocator, pem.retain());
/* 708:    */     }
/* 709:    */     finally
/* 710:    */     {
/* 711:797 */       pem.release();
/* 712:    */     }
/* 713:    */   }
/* 714:    */   
/* 715:    */   private static long newBIO(ByteBuf buffer)
/* 716:    */     throws Exception
/* 717:    */   {
/* 718:    */     try
/* 719:    */     {
/* 720:833 */       long bio = SSL.newMemBIO();
/* 721:834 */       int readable = buffer.readableBytes();
/* 722:835 */       if (SSL.bioWrite(bio, OpenSsl.memoryAddress(buffer) + buffer.readerIndex(), readable) != readable)
/* 723:    */       {
/* 724:836 */         SSL.freeBIO(bio);
/* 725:837 */         throw new IllegalStateException("Could not write data to memory BIO");
/* 726:    */       }
/* 727:839 */       return bio;
/* 728:    */     }
/* 729:    */     finally
/* 730:    */     {
/* 731:841 */       buffer.release();
/* 732:    */     }
/* 733:    */   }
/* 734:    */   
/* 735:    */   abstract OpenSslKeyMaterialManager keyMaterialManager();
/* 736:    */   
/* 737:    */   public abstract OpenSslSessionContext sessionContext();
/* 738:    */   
/* 739:    */   /* Error */
/* 740:    */   static long toBIO(ByteBufAllocator allocator, PemEncoded pem)
/* 741:    */     throws Exception
/* 742:    */   {
/* 743:    */     // Byte code:
/* 744:    */     //   0: aload_1
/* 745:    */     //   1: invokeinterface 170 1 0
/* 746:    */     //   6: astore_2
/* 747:    */     //   7: aload_2
/* 748:    */     //   8: invokevirtual 171	io/netty/buffer/ByteBuf:isDirect	()Z
/* 749:    */     //   11: ifeq +20 -> 31
/* 750:    */     //   14: aload_2
/* 751:    */     //   15: invokevirtual 172	io/netty/buffer/ByteBuf:retainedSlice	()Lio/netty/buffer/ByteBuf;
/* 752:    */     //   18: invokestatic 173	io/netty/handler/ssl/ReferenceCountedOpenSslContext:newBIO	(Lio/netty/buffer/ByteBuf;)J
/* 753:    */     //   21: lstore_3
/* 754:    */     //   22: aload_1
/* 755:    */     //   23: invokeinterface 165 1 0
/* 756:    */     //   28: pop
/* 757:    */     //   29: lload_3
/* 758:    */     //   30: lreturn
/* 759:    */     //   31: aload_0
/* 760:    */     //   32: aload_2
/* 761:    */     //   33: invokevirtual 174	io/netty/buffer/ByteBuf:readableBytes	()I
/* 762:    */     //   36: invokeinterface 175 2 0
/* 763:    */     //   41: astore_3
/* 764:    */     //   42: aload_3
/* 765:    */     //   43: aload_2
/* 766:    */     //   44: aload_2
/* 767:    */     //   45: invokevirtual 176	io/netty/buffer/ByteBuf:readerIndex	()I
/* 768:    */     //   48: aload_2
/* 769:    */     //   49: invokevirtual 174	io/netty/buffer/ByteBuf:readableBytes	()I
/* 770:    */     //   52: invokevirtual 177	io/netty/buffer/ByteBuf:writeBytes	(Lio/netty/buffer/ByteBuf;II)Lio/netty/buffer/ByteBuf;
/* 771:    */     //   55: pop
/* 772:    */     //   56: aload_3
/* 773:    */     //   57: invokevirtual 172	io/netty/buffer/ByteBuf:retainedSlice	()Lio/netty/buffer/ByteBuf;
/* 774:    */     //   60: invokestatic 173	io/netty/handler/ssl/ReferenceCountedOpenSslContext:newBIO	(Lio/netty/buffer/ByteBuf;)J
/* 775:    */     //   63: lstore 4
/* 776:    */     //   65: aload_1
/* 777:    */     //   66: invokeinterface 178 1 0
/* 778:    */     //   71: ifeq +7 -> 78
/* 779:    */     //   74: aload_3
/* 780:    */     //   75: invokestatic 179	io/netty/handler/ssl/SslUtils:zeroout	(Lio/netty/buffer/ByteBuf;)V
/* 781:    */     //   78: aload_3
/* 782:    */     //   79: invokevirtual 180	io/netty/buffer/ByteBuf:release	()Z
/* 783:    */     //   82: pop
/* 784:    */     //   83: goto +13 -> 96
/* 785:    */     //   86: astore 6
/* 786:    */     //   88: aload_3
/* 787:    */     //   89: invokevirtual 180	io/netty/buffer/ByteBuf:release	()Z
/* 788:    */     //   92: pop
/* 789:    */     //   93: aload 6
/* 790:    */     //   95: athrow
/* 791:    */     //   96: aload_1
/* 792:    */     //   97: invokeinterface 165 1 0
/* 793:    */     //   102: pop
/* 794:    */     //   103: lload 4
/* 795:    */     //   105: lreturn
/* 796:    */     //   106: astore 7
/* 797:    */     //   108: aload_1
/* 798:    */     //   109: invokeinterface 178 1 0
/* 799:    */     //   114: ifeq +7 -> 121
/* 800:    */     //   117: aload_3
/* 801:    */     //   118: invokestatic 179	io/netty/handler/ssl/SslUtils:zeroout	(Lio/netty/buffer/ByteBuf;)V
/* 802:    */     //   121: aload_3
/* 803:    */     //   122: invokevirtual 180	io/netty/buffer/ByteBuf:release	()Z
/* 804:    */     //   125: pop
/* 805:    */     //   126: goto +13 -> 139
/* 806:    */     //   129: astore 8
/* 807:    */     //   131: aload_3
/* 808:    */     //   132: invokevirtual 180	io/netty/buffer/ByteBuf:release	()Z
/* 809:    */     //   135: pop
/* 810:    */     //   136: aload 8
/* 811:    */     //   138: athrow
/* 812:    */     //   139: aload 7
/* 813:    */     //   141: athrow
/* 814:    */     //   142: astore 9
/* 815:    */     //   144: aload_1
/* 816:    */     //   145: invokeinterface 165 1 0
/* 817:    */     //   150: pop
/* 818:    */     //   151: aload 9
/* 819:    */     //   153: athrow
/* 820:    */     // Line number table:
/* 821:    */     //   Java source line #805	-> byte code offset #0
/* 822:    */     //   Java source line #807	-> byte code offset #7
/* 823:    */     //   Java source line #808	-> byte code offset #14
/* 824:    */     //   Java source line #827	-> byte code offset #22
/* 825:    */     //   Java source line #808	-> byte code offset #29
/* 826:    */     //   Java source line #811	-> byte code offset #31
/* 827:    */     //   Java source line #813	-> byte code offset #42
/* 828:    */     //   Java source line #814	-> byte code offset #56
/* 829:    */     //   Java source line #819	-> byte code offset #65
/* 830:    */     //   Java source line #820	-> byte code offset #74
/* 831:    */     //   Java source line #823	-> byte code offset #78
/* 832:    */     //   Java source line #824	-> byte code offset #83
/* 833:    */     //   Java source line #823	-> byte code offset #86
/* 834:    */     //   Java source line #827	-> byte code offset #96
/* 835:    */     //   Java source line #814	-> byte code offset #103
/* 836:    */     //   Java source line #816	-> byte code offset #106
/* 837:    */     //   Java source line #819	-> byte code offset #108
/* 838:    */     //   Java source line #820	-> byte code offset #117
/* 839:    */     //   Java source line #823	-> byte code offset #121
/* 840:    */     //   Java source line #824	-> byte code offset #126
/* 841:    */     //   Java source line #823	-> byte code offset #129
/* 842:    */     //   Java source line #827	-> byte code offset #142
/* 843:    */     // Local variable table:
/* 844:    */     //   start	length	slot	name	signature
/* 845:    */     //   0	154	0	allocator	ByteBufAllocator
/* 846:    */     //   0	154	1	pem	PemEncoded
/* 847:    */     //   6	43	2	content	ByteBuf
/* 848:    */     //   21	9	3	l1	long
/* 849:    */     //   41	91	3	buffer	ByteBuf
/* 850:    */     //   63	41	4	l2	long
/* 851:    */     //   86	8	6	localObject1	Object
/* 852:    */     //   106	34	7	localObject2	Object
/* 853:    */     //   129	8	8	localObject3	Object
/* 854:    */     //   142	10	9	localObject4	Object
/* 855:    */     // Exception table:
/* 856:    */     //   from	to	target	type
/* 857:    */     //   65	78	86	finally
/* 858:    */     //   86	88	86	finally
/* 859:    */     //   42	65	106	finally
/* 860:    */     //   106	108	106	finally
/* 861:    */     //   108	121	129	finally
/* 862:    */     //   129	131	129	finally
/* 863:    */     //   0	22	142	finally
/* 864:    */     //   31	96	142	finally
/* 865:    */     //   106	144	142	finally
/* 866:    */   }
/* 867:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ReferenceCountedOpenSslContext
 * JD-Core Version:    0.7.0.1
 */