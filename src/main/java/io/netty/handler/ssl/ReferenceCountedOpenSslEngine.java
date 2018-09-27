/*    1:     */ package io.netty.handler.ssl;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.internal.tcnative.Buffer;
/*    6:     */ import io.netty.internal.tcnative.SSL;
/*    7:     */ import io.netty.util.AbstractReferenceCounted;
/*    8:     */ import io.netty.util.ReferenceCounted;
/*    9:     */ import io.netty.util.ResourceLeakDetector;
/*   10:     */ import io.netty.util.ResourceLeakDetectorFactory;
/*   11:     */ import io.netty.util.ResourceLeakTracker;
/*   12:     */ import io.netty.util.internal.EmptyArrays;
/*   13:     */ import io.netty.util.internal.ObjectUtil;
/*   14:     */ import io.netty.util.internal.PlatformDependent;
/*   15:     */ import io.netty.util.internal.ThrowableUtil;
/*   16:     */ import io.netty.util.internal.logging.InternalLogger;
/*   17:     */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   18:     */ import java.nio.ByteBuffer;
/*   19:     */ import java.nio.ReadOnlyBufferException;
/*   20:     */ import java.security.Principal;
/*   21:     */ import java.security.cert.Certificate;
/*   22:     */ import java.util.ArrayList;
/*   23:     */ import java.util.Arrays;
/*   24:     */ import java.util.Collection;
/*   25:     */ import java.util.HashMap;
/*   26:     */ import java.util.List;
/*   27:     */ import java.util.Map;
/*   28:     */ import java.util.Set;
/*   29:     */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*   30:     */ import java.util.concurrent.locks.Lock;
/*   31:     */ import java.util.concurrent.locks.ReadWriteLock;
/*   32:     */ import javax.net.ssl.SSLEngine;
/*   33:     */ import javax.net.ssl.SSLEngineResult;
/*   34:     */ import javax.net.ssl.SSLEngineResult.HandshakeStatus;
/*   35:     */ import javax.net.ssl.SSLEngineResult.Status;
/*   36:     */ import javax.net.ssl.SSLException;
/*   37:     */ import javax.net.ssl.SSLHandshakeException;
/*   38:     */ import javax.net.ssl.SSLParameters;
/*   39:     */ import javax.net.ssl.SSLPeerUnverifiedException;
/*   40:     */ import javax.net.ssl.SSLSession;
/*   41:     */ import javax.net.ssl.SSLSessionBindingEvent;
/*   42:     */ import javax.net.ssl.SSLSessionBindingListener;
/*   43:     */ import javax.net.ssl.SSLSessionContext;
/*   44:     */ 
/*   45:     */ public class ReferenceCountedOpenSslEngine
/*   46:     */   extends SSLEngine
/*   47:     */   implements ReferenceCounted, ApplicationProtocolAccessor
/*   48:     */ {
/*   49:  95 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslEngine.class);
/*   50:  97 */   private static final SSLException BEGIN_HANDSHAKE_ENGINE_CLOSED = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("engine closed"), ReferenceCountedOpenSslEngine.class, "beginHandshake()");
/*   51:  99 */   private static final SSLException HANDSHAKE_ENGINE_CLOSED = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("engine closed"), ReferenceCountedOpenSslEngine.class, "handshake()");
/*   52: 101 */   private static final SSLException RENEGOTIATION_UNSUPPORTED = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("renegotiation unsupported"), ReferenceCountedOpenSslEngine.class, "beginHandshake()");
/*   53: 104 */   private static final ResourceLeakDetector<ReferenceCountedOpenSslEngine> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ReferenceCountedOpenSslEngine.class);
/*   54:     */   private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_SSLV2 = 0;
/*   55:     */   private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_SSLV3 = 1;
/*   56:     */   private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1 = 2;
/*   57:     */   private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1_1 = 3;
/*   58:     */   private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1_2 = 4;
/*   59: 110 */   private static final int[] OPENSSL_OP_NO_PROTOCOLS = { SSL.SSL_OP_NO_SSLv2, SSL.SSL_OP_NO_SSLv3, SSL.SSL_OP_NO_TLSv1, SSL.SSL_OP_NO_TLSv1_1, SSL.SSL_OP_NO_TLSv1_2 };
/*   60:     */   private static final int DEFAULT_HOSTNAME_VALIDATION_FLAGS = 0;
/*   61: 125 */   static final int MAX_PLAINTEXT_LENGTH = SSL.SSL_MAX_PLAINTEXT_LENGTH;
/*   62: 129 */   private static final int MAX_RECORD_SIZE = SSL.SSL_MAX_RECORD_LENGTH;
/*   63: 132 */   private static final AtomicIntegerFieldUpdater<ReferenceCountedOpenSslEngine> DESTROYED_UPDATER = AtomicIntegerFieldUpdater.newUpdater(ReferenceCountedOpenSslEngine.class, "destroyed");
/*   64:     */   private static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";
/*   65: 135 */   private static final SSLEngineResult NEED_UNWRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
/*   66: 136 */   private static final SSLEngineResult NEED_UNWRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
/*   67: 137 */   private static final SSLEngineResult NEED_WRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
/*   68: 138 */   private static final SSLEngineResult NEED_WRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
/*   69: 139 */   private static final SSLEngineResult CLOSED_NOT_HANDSHAKING = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
/*   70:     */   private long ssl;
/*   71:     */   private long networkBIO;
/*   72:     */   private boolean certificateSet;
/*   73:     */   
/*   74:     */   private static enum HandshakeState
/*   75:     */   {
/*   76: 150 */     NOT_STARTED,  STARTED_IMPLICITLY,  STARTED_EXPLICITLY,  FINISHED;
/*   77:     */     
/*   78:     */     private HandshakeState() {}
/*   79:     */   }
/*   80:     */   
/*   81: 165 */   private HandshakeState handshakeState = HandshakeState.NOT_STARTED;
/*   82:     */   private boolean renegotiationPending;
/*   83:     */   private boolean receivedShutdown;
/*   84:     */   private volatile int destroyed;
/*   85:     */   private volatile String applicationProtocol;
/*   86:     */   private final ResourceLeakTracker<ReferenceCountedOpenSslEngine> leak;
/*   87: 173 */   private final AbstractReferenceCounted refCnt = new AbstractReferenceCounted()
/*   88:     */   {
/*   89:     */     public ReferenceCounted touch(Object hint)
/*   90:     */     {
/*   91: 176 */       if (ReferenceCountedOpenSslEngine.this.leak != null) {
/*   92: 177 */         ReferenceCountedOpenSslEngine.this.leak.record(hint);
/*   93:     */       }
/*   94: 180 */       return ReferenceCountedOpenSslEngine.this;
/*   95:     */     }
/*   96:     */     
/*   97:     */     protected void deallocate()
/*   98:     */     {
/*   99: 185 */       ReferenceCountedOpenSslEngine.this.shutdown();
/*  100: 186 */       if (ReferenceCountedOpenSslEngine.this.leak != null)
/*  101:     */       {
/*  102: 187 */         boolean closed = ReferenceCountedOpenSslEngine.this.leak.close(ReferenceCountedOpenSslEngine.this);
/*  103: 188 */         assert (closed);
/*  104:     */       }
/*  105:     */     }
/*  106:     */   };
/*  107: 193 */   private volatile ClientAuth clientAuth = ClientAuth.NONE;
/*  108: 196 */   private volatile long lastAccessed = -1L;
/*  109:     */   private String endPointIdentificationAlgorithm;
/*  110:     */   private Object algorithmConstraints;
/*  111:     */   private List<String> sniHostNames;
/*  112:     */   private volatile Collection<?> matchers;
/*  113:     */   private boolean isInboundDone;
/*  114:     */   private boolean outboundClosed;
/*  115:     */   final boolean jdkCompatibilityMode;
/*  116:     */   private final boolean clientMode;
/*  117:     */   private final ByteBufAllocator alloc;
/*  118:     */   private final OpenSslEngineMap engineMap;
/*  119:     */   private final OpenSslApplicationProtocolNegotiator apn;
/*  120:     */   private final boolean rejectRemoteInitiatedRenegotiation;
/*  121:     */   private final OpenSslSession session;
/*  122:     */   private final Certificate[] localCerts;
/*  123: 219 */   private final ByteBuffer[] singleSrcBuffer = new ByteBuffer[1];
/*  124: 220 */   private final ByteBuffer[] singleDstBuffer = new ByteBuffer[1];
/*  125:     */   private final OpenSslKeyMaterialManager keyMaterialManager;
/*  126:     */   private final boolean enableOcsp;
/*  127:     */   private int maxWrapOverhead;
/*  128:     */   private int maxWrapBufferSize;
/*  129:     */   SSLHandshakeException handshakeException;
/*  130:     */   
/*  131:     */   ReferenceCountedOpenSslEngine(ReferenceCountedOpenSslContext context, ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode, boolean leakDetection)
/*  132:     */   {
/*  133: 244 */     super(peerHost, peerPort);
/*  134: 245 */     OpenSsl.ensureAvailability();
/*  135: 246 */     this.alloc = ((ByteBufAllocator)ObjectUtil.checkNotNull(alloc, "alloc"));
/*  136: 247 */     this.apn = ((OpenSslApplicationProtocolNegotiator)context.applicationProtocolNegotiator());
/*  137: 248 */     this.session = new OpenSslSession(context.sessionContext());
/*  138: 249 */     this.clientMode = context.isClient();
/*  139: 250 */     this.engineMap = context.engineMap;
/*  140: 251 */     this.rejectRemoteInitiatedRenegotiation = context.getRejectRemoteInitiatedRenegotiation();
/*  141: 252 */     this.localCerts = context.keyCertChain;
/*  142: 253 */     this.keyMaterialManager = context.keyMaterialManager();
/*  143: 254 */     this.enableOcsp = context.enableOcsp;
/*  144: 255 */     this.jdkCompatibilityMode = jdkCompatibilityMode;
/*  145: 256 */     Lock readerLock = context.ctxLock.readLock();
/*  146: 257 */     readerLock.lock();
/*  147:     */     try
/*  148:     */     {
/*  149: 260 */       finalSsl = SSL.newSSL(context.ctx, !context.isClient());
/*  150:     */     }
/*  151:     */     finally
/*  152:     */     {
/*  153:     */       long finalSsl;
/*  154: 262 */       readerLock.unlock();
/*  155:     */     }
/*  156: 264 */     synchronized (this)
/*  157:     */     {
/*  158:     */       long finalSsl;
/*  159: 265 */       this.ssl = finalSsl;
/*  160:     */       try
/*  161:     */       {
/*  162: 267 */         this.networkBIO = SSL.bioNewByteBuffer(this.ssl, context.getBioNonApplicationBufferSize());
/*  163:     */         
/*  164:     */ 
/*  165:     */ 
/*  166: 271 */         setClientAuth(this.clientMode ? ClientAuth.NONE : context.clientAuth);
/*  167: 273 */         if (context.protocols != null) {
/*  168: 274 */           setEnabledProtocols(context.protocols);
/*  169:     */         }
/*  170: 279 */         if ((this.clientMode) && (peerHost != null)) {
/*  171: 280 */           SSL.setTlsExtHostName(this.ssl, peerHost);
/*  172:     */         }
/*  173: 283 */         if (this.enableOcsp) {
/*  174: 284 */           SSL.enableOcsp(this.ssl);
/*  175:     */         }
/*  176: 287 */         if (!jdkCompatibilityMode) {
/*  177: 288 */           SSL.setMode(this.ssl, SSL.getMode(this.ssl) | SSL.SSL_MODE_ENABLE_PARTIAL_WRITE);
/*  178:     */         }
/*  179: 292 */         calculateMaxWrapOverhead();
/*  180:     */       }
/*  181:     */       catch (Throwable cause)
/*  182:     */       {
/*  183: 294 */         SSL.freeSSL(this.ssl);
/*  184: 295 */         PlatformDependent.throwException(cause);
/*  185:     */       }
/*  186:     */     }
/*  187: 301 */     this.leak = (leakDetection ? leakDetector.track(this) : null);
/*  188:     */   }
/*  189:     */   
/*  190:     */   public void setOcspResponse(byte[] response)
/*  191:     */   {
/*  192: 309 */     if (!this.enableOcsp) {
/*  193: 310 */       throw new IllegalStateException("OCSP stapling is not enabled");
/*  194:     */     }
/*  195: 313 */     if (this.clientMode) {
/*  196: 314 */       throw new IllegalStateException("Not a server SSLEngine");
/*  197:     */     }
/*  198: 317 */     synchronized (this)
/*  199:     */     {
/*  200: 318 */       SSL.setOcspResponse(this.ssl, response);
/*  201:     */     }
/*  202:     */   }
/*  203:     */   
/*  204:     */   public byte[] getOcspResponse()
/*  205:     */   {
/*  206: 327 */     if (!this.enableOcsp) {
/*  207: 328 */       throw new IllegalStateException("OCSP stapling is not enabled");
/*  208:     */     }
/*  209: 331 */     if (!this.clientMode) {
/*  210: 332 */       throw new IllegalStateException("Not a client SSLEngine");
/*  211:     */     }
/*  212: 335 */     synchronized (this)
/*  213:     */     {
/*  214: 336 */       return SSL.getOcspResponse(this.ssl);
/*  215:     */     }
/*  216:     */   }
/*  217:     */   
/*  218:     */   public final int refCnt()
/*  219:     */   {
/*  220: 342 */     return this.refCnt.refCnt();
/*  221:     */   }
/*  222:     */   
/*  223:     */   public final ReferenceCounted retain()
/*  224:     */   {
/*  225: 347 */     this.refCnt.retain();
/*  226: 348 */     return this;
/*  227:     */   }
/*  228:     */   
/*  229:     */   public final ReferenceCounted retain(int increment)
/*  230:     */   {
/*  231: 353 */     this.refCnt.retain(increment);
/*  232: 354 */     return this;
/*  233:     */   }
/*  234:     */   
/*  235:     */   public final ReferenceCounted touch()
/*  236:     */   {
/*  237: 359 */     this.refCnt.touch();
/*  238: 360 */     return this;
/*  239:     */   }
/*  240:     */   
/*  241:     */   public final ReferenceCounted touch(Object hint)
/*  242:     */   {
/*  243: 365 */     this.refCnt.touch(hint);
/*  244: 366 */     return this;
/*  245:     */   }
/*  246:     */   
/*  247:     */   public final boolean release()
/*  248:     */   {
/*  249: 371 */     return this.refCnt.release();
/*  250:     */   }
/*  251:     */   
/*  252:     */   public final boolean release(int decrement)
/*  253:     */   {
/*  254: 376 */     return this.refCnt.release(decrement);
/*  255:     */   }
/*  256:     */   
/*  257:     */   public final synchronized SSLSession getHandshakeSession()
/*  258:     */   {
/*  259: 385 */     switch (this.handshakeState)
/*  260:     */     {
/*  261:     */     case NOT_STARTED: 
/*  262:     */     case FINISHED: 
/*  263: 388 */       return null;
/*  264:     */     }
/*  265: 390 */     return this.session;
/*  266:     */   }
/*  267:     */   
/*  268:     */   public final synchronized long sslPointer()
/*  269:     */   {
/*  270: 400 */     return this.ssl;
/*  271:     */   }
/*  272:     */   
/*  273:     */   public final synchronized void shutdown()
/*  274:     */   {
/*  275: 407 */     if (DESTROYED_UPDATER.compareAndSet(this, 0, 1))
/*  276:     */     {
/*  277: 408 */       this.engineMap.remove(this.ssl);
/*  278: 409 */       SSL.freeSSL(this.ssl);
/*  279: 410 */       this.ssl = (this.networkBIO = 0L);
/*  280:     */       
/*  281: 412 */       this.isInboundDone = (this.outboundClosed = 1);
/*  282:     */     }
/*  283: 416 */     SSL.clearError();
/*  284:     */   }
/*  285:     */   
/*  286:     */   private int writePlaintextData(ByteBuffer src, int len)
/*  287:     */   {
/*  288: 425 */     int pos = src.position();
/*  289: 426 */     int limit = src.limit();
/*  290: 429 */     if (src.isDirect())
/*  291:     */     {
/*  292: 430 */       int sslWrote = SSL.writeToSSL(this.ssl, Buffer.address(src) + pos, len);
/*  293: 431 */       if (sslWrote > 0) {
/*  294: 432 */         src.position(pos + sslWrote);
/*  295:     */       }
/*  296:     */     }
/*  297:     */     else
/*  298:     */     {
/*  299: 435 */       ByteBuf buf = this.alloc.directBuffer(len);
/*  300:     */       try
/*  301:     */       {
/*  302: 437 */         src.limit(pos + len);
/*  303:     */         
/*  304: 439 */         buf.setBytes(0, src);
/*  305: 440 */         src.limit(limit);
/*  306:     */         
/*  307: 442 */         int sslWrote = SSL.writeToSSL(this.ssl, OpenSsl.memoryAddress(buf), len);
/*  308: 443 */         if (sslWrote > 0) {
/*  309: 444 */           src.position(pos + sslWrote);
/*  310:     */         } else {
/*  311: 446 */           src.position(pos);
/*  312:     */         }
/*  313:     */       }
/*  314:     */       finally
/*  315:     */       {
/*  316: 449 */         buf.release();
/*  317:     */       }
/*  318:     */     }
/*  319:     */     int sslWrote;
/*  320: 452 */     return sslWrote;
/*  321:     */   }
/*  322:     */   
/*  323:     */   private ByteBuf writeEncryptedData(ByteBuffer src, int len)
/*  324:     */   {
/*  325: 459 */     int pos = src.position();
/*  326: 460 */     if (src.isDirect())
/*  327:     */     {
/*  328: 461 */       SSL.bioSetByteBuffer(this.networkBIO, Buffer.address(src) + pos, len, false);
/*  329:     */     }
/*  330:     */     else
/*  331:     */     {
/*  332: 463 */       ByteBuf buf = this.alloc.directBuffer(len);
/*  333:     */       try
/*  334:     */       {
/*  335: 465 */         int limit = src.limit();
/*  336: 466 */         src.limit(pos + len);
/*  337: 467 */         buf.writeBytes(src);
/*  338:     */         
/*  339: 469 */         src.position(pos);
/*  340: 470 */         src.limit(limit);
/*  341:     */         
/*  342: 472 */         SSL.bioSetByteBuffer(this.networkBIO, OpenSsl.memoryAddress(buf), len, false);
/*  343: 473 */         return buf;
/*  344:     */       }
/*  345:     */       catch (Throwable cause)
/*  346:     */       {
/*  347: 475 */         buf.release();
/*  348: 476 */         PlatformDependent.throwException(cause);
/*  349:     */       }
/*  350:     */     }
/*  351: 479 */     return null;
/*  352:     */   }
/*  353:     */   
/*  354:     */   private int readPlaintextData(ByteBuffer dst)
/*  355:     */   {
/*  356: 487 */     int pos = dst.position();
/*  357: 488 */     if (dst.isDirect())
/*  358:     */     {
/*  359: 489 */       int sslRead = SSL.readFromSSL(this.ssl, Buffer.address(dst) + pos, dst.limit() - pos);
/*  360: 490 */       if (sslRead > 0) {
/*  361: 491 */         dst.position(pos + sslRead);
/*  362:     */       }
/*  363:     */     }
/*  364:     */     else
/*  365:     */     {
/*  366: 494 */       int limit = dst.limit();
/*  367: 495 */       int len = Math.min(maxEncryptedPacketLength0(), limit - pos);
/*  368: 496 */       ByteBuf buf = this.alloc.directBuffer(len);
/*  369:     */       try
/*  370:     */       {
/*  371: 498 */         int sslRead = SSL.readFromSSL(this.ssl, OpenSsl.memoryAddress(buf), len);
/*  372: 499 */         if (sslRead > 0)
/*  373:     */         {
/*  374: 500 */           dst.limit(pos + sslRead);
/*  375: 501 */           buf.getBytes(buf.readerIndex(), dst);
/*  376: 502 */           dst.limit(limit);
/*  377:     */         }
/*  378:     */       }
/*  379:     */       finally
/*  380:     */       {
/*  381: 505 */         buf.release();
/*  382:     */       }
/*  383:     */     }
/*  384:     */     int sslRead;
/*  385: 509 */     return sslRead;
/*  386:     */   }
/*  387:     */   
/*  388:     */   final synchronized int maxWrapOverhead()
/*  389:     */   {
/*  390: 516 */     return this.maxWrapOverhead;
/*  391:     */   }
/*  392:     */   
/*  393:     */   final synchronized int maxEncryptedPacketLength()
/*  394:     */   {
/*  395: 523 */     return maxEncryptedPacketLength0();
/*  396:     */   }
/*  397:     */   
/*  398:     */   final int maxEncryptedPacketLength0()
/*  399:     */   {
/*  400: 531 */     return this.maxWrapOverhead + MAX_PLAINTEXT_LENGTH;
/*  401:     */   }
/*  402:     */   
/*  403:     */   final int calculateMaxLengthForWrap(int plaintextLength, int numComponents)
/*  404:     */   {
/*  405: 540 */     return (int)Math.min(this.maxWrapBufferSize, plaintextLength + this.maxWrapOverhead * numComponents);
/*  406:     */   }
/*  407:     */   
/*  408:     */   final synchronized int sslPending()
/*  409:     */   {
/*  410: 544 */     return sslPending0();
/*  411:     */   }
/*  412:     */   
/*  413:     */   private void calculateMaxWrapOverhead()
/*  414:     */   {
/*  415: 551 */     this.maxWrapOverhead = SSL.getMaxWrapOverhead(this.ssl);
/*  416:     */     
/*  417:     */ 
/*  418:     */ 
/*  419:     */ 
/*  420: 556 */     this.maxWrapBufferSize = (this.jdkCompatibilityMode ? maxEncryptedPacketLength0() : maxEncryptedPacketLength0() << 4);
/*  421:     */   }
/*  422:     */   
/*  423:     */   private int sslPending0()
/*  424:     */   {
/*  425: 564 */     return this.handshakeState != HandshakeState.FINISHED ? 0 : SSL.sslPending(this.ssl);
/*  426:     */   }
/*  427:     */   
/*  428:     */   private boolean isBytesAvailableEnoughForWrap(int bytesAvailable, int plaintextLength, int numComponents)
/*  429:     */   {
/*  430: 568 */     return bytesAvailable - this.maxWrapOverhead * numComponents >= plaintextLength;
/*  431:     */   }
/*  432:     */   
/*  433:     */   public final SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst)
/*  434:     */     throws SSLException
/*  435:     */   {
/*  436: 575 */     if (srcs == null) {
/*  437: 576 */       throw new IllegalArgumentException("srcs is null");
/*  438:     */     }
/*  439: 578 */     if (dst == null) {
/*  440: 579 */       throw new IllegalArgumentException("dst is null");
/*  441:     */     }
/*  442: 582 */     if ((offset >= srcs.length) || (offset + length > srcs.length)) {
/*  443: 583 */       throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
/*  444:     */     }
/*  445: 588 */     if (dst.isReadOnly()) {
/*  446: 589 */       throw new ReadOnlyBufferException();
/*  447:     */     }
/*  448: 592 */     synchronized (this)
/*  449:     */     {
/*  450: 593 */       if (isOutboundDone()) {
/*  451: 595 */         return (isInboundDone()) || (isDestroyed()) ? CLOSED_NOT_HANDSHAKING : NEED_UNWRAP_CLOSED;
/*  452:     */       }
/*  453: 598 */       int bytesProduced = 0;
/*  454: 599 */       ByteBuf bioReadCopyBuf = null;
/*  455:     */       try
/*  456:     */       {
/*  457: 602 */         if (dst.isDirect())
/*  458:     */         {
/*  459: 603 */           SSL.bioSetByteBuffer(this.networkBIO, Buffer.address(dst) + dst.position(), dst.remaining(), true);
/*  460:     */         }
/*  461:     */         else
/*  462:     */         {
/*  463: 606 */           bioReadCopyBuf = this.alloc.directBuffer(dst.remaining());
/*  464: 607 */           SSL.bioSetByteBuffer(this.networkBIO, OpenSsl.memoryAddress(bioReadCopyBuf), bioReadCopyBuf.writableBytes(), true);
/*  465:     */         }
/*  466: 611 */         int bioLengthBefore = SSL.bioLengthByteBuffer(this.networkBIO);
/*  467: 614 */         if (this.outboundClosed)
/*  468:     */         {
/*  469: 617 */           bytesProduced = SSL.bioFlushByteBuffer(this.networkBIO);
/*  470: 618 */           if (bytesProduced <= 0)
/*  471:     */           {
/*  472: 619 */             localSSLEngineResult1 = newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
/*  473:     */             
/*  474:     */ 
/*  475:     */ 
/*  476:     */ 
/*  477:     */ 
/*  478:     */ 
/*  479:     */ 
/*  480:     */ 
/*  481:     */ 
/*  482:     */ 
/*  483:     */ 
/*  484:     */ 
/*  485:     */ 
/*  486:     */ 
/*  487:     */ 
/*  488:     */ 
/*  489:     */ 
/*  490:     */ 
/*  491:     */ 
/*  492:     */ 
/*  493:     */ 
/*  494:     */ 
/*  495:     */ 
/*  496:     */ 
/*  497:     */ 
/*  498:     */ 
/*  499:     */ 
/*  500:     */ 
/*  501:     */ 
/*  502:     */ 
/*  503:     */ 
/*  504:     */ 
/*  505:     */ 
/*  506:     */ 
/*  507:     */ 
/*  508:     */ 
/*  509:     */ 
/*  510:     */ 
/*  511:     */ 
/*  512:     */ 
/*  513:     */ 
/*  514:     */ 
/*  515:     */ 
/*  516:     */ 
/*  517:     */ 
/*  518:     */ 
/*  519:     */ 
/*  520:     */ 
/*  521:     */ 
/*  522:     */ 
/*  523:     */ 
/*  524:     */ 
/*  525:     */ 
/*  526:     */ 
/*  527:     */ 
/*  528:     */ 
/*  529:     */ 
/*  530:     */ 
/*  531:     */ 
/*  532:     */ 
/*  533:     */ 
/*  534:     */ 
/*  535:     */ 
/*  536:     */ 
/*  537:     */ 
/*  538:     */ 
/*  539:     */ 
/*  540:     */ 
/*  541:     */ 
/*  542:     */ 
/*  543:     */ 
/*  544:     */ 
/*  545:     */ 
/*  546:     */ 
/*  547:     */ 
/*  548:     */ 
/*  549:     */ 
/*  550:     */ 
/*  551:     */ 
/*  552:     */ 
/*  553:     */ 
/*  554:     */ 
/*  555:     */ 
/*  556:     */ 
/*  557:     */ 
/*  558:     */ 
/*  559:     */ 
/*  560:     */ 
/*  561:     */ 
/*  562:     */ 
/*  563:     */ 
/*  564:     */ 
/*  565:     */ 
/*  566:     */ 
/*  567:     */ 
/*  568:     */ 
/*  569:     */ 
/*  570:     */ 
/*  571:     */ 
/*  572:     */ 
/*  573:     */ 
/*  574:     */ 
/*  575:     */ 
/*  576:     */ 
/*  577:     */ 
/*  578:     */ 
/*  579:     */ 
/*  580:     */ 
/*  581:     */ 
/*  582:     */ 
/*  583:     */ 
/*  584:     */ 
/*  585:     */ 
/*  586:     */ 
/*  587:     */ 
/*  588:     */ 
/*  589:     */ 
/*  590:     */ 
/*  591:     */ 
/*  592:     */ 
/*  593:     */ 
/*  594:     */ 
/*  595:     */ 
/*  596:     */ 
/*  597:     */ 
/*  598:     */ 
/*  599:     */ 
/*  600:     */ 
/*  601:     */ 
/*  602:     */ 
/*  603:     */ 
/*  604:     */ 
/*  605:     */ 
/*  606:     */ 
/*  607:     */ 
/*  608:     */ 
/*  609:     */ 
/*  610:     */ 
/*  611:     */ 
/*  612:     */ 
/*  613:     */ 
/*  614:     */ 
/*  615:     */ 
/*  616:     */ 
/*  617:     */ 
/*  618:     */ 
/*  619:     */ 
/*  620:     */ 
/*  621:     */ 
/*  622:     */ 
/*  623:     */ 
/*  624:     */ 
/*  625:     */ 
/*  626:     */ 
/*  627:     */ 
/*  628:     */ 
/*  629:     */ 
/*  630:     */ 
/*  631:     */ 
/*  632:     */ 
/*  633:     */ 
/*  634:     */ 
/*  635:     */ 
/*  636:     */ 
/*  637:     */ 
/*  638:     */ 
/*  639:     */ 
/*  640:     */ 
/*  641:     */ 
/*  642:     */ 
/*  643:     */ 
/*  644:     */ 
/*  645:     */ 
/*  646:     */ 
/*  647:     */ 
/*  648:     */ 
/*  649:     */ 
/*  650:     */ 
/*  651:     */ 
/*  652:     */ 
/*  653:     */ 
/*  654:     */ 
/*  655:     */ 
/*  656:     */ 
/*  657:     */ 
/*  658:     */ 
/*  659:     */ 
/*  660:     */ 
/*  661:     */ 
/*  662:     */ 
/*  663: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/*  664: 811 */             if (bioReadCopyBuf == null)
/*  665:     */             {
/*  666: 812 */               dst.position(dst.position() + bytesProduced);
/*  667:     */             }
/*  668:     */             else
/*  669:     */             {
/*  670: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/*  671:     */               
/*  672: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/*  673: 817 */               bioReadCopyBuf.release();
/*  674:     */             }
/*  675: 619 */             return localSSLEngineResult1;
/*  676:     */           }
/*  677: 624 */           if (!doSSLShutdown())
/*  678:     */           {
/*  679: 625 */             localSSLEngineResult1 = newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, bytesProduced);
/*  680:     */             
/*  681:     */ 
/*  682:     */ 
/*  683:     */ 
/*  684:     */ 
/*  685:     */ 
/*  686:     */ 
/*  687:     */ 
/*  688:     */ 
/*  689:     */ 
/*  690:     */ 
/*  691:     */ 
/*  692:     */ 
/*  693:     */ 
/*  694:     */ 
/*  695:     */ 
/*  696:     */ 
/*  697:     */ 
/*  698:     */ 
/*  699:     */ 
/*  700:     */ 
/*  701:     */ 
/*  702:     */ 
/*  703:     */ 
/*  704:     */ 
/*  705:     */ 
/*  706:     */ 
/*  707:     */ 
/*  708:     */ 
/*  709:     */ 
/*  710:     */ 
/*  711:     */ 
/*  712:     */ 
/*  713:     */ 
/*  714:     */ 
/*  715:     */ 
/*  716:     */ 
/*  717:     */ 
/*  718:     */ 
/*  719:     */ 
/*  720:     */ 
/*  721:     */ 
/*  722:     */ 
/*  723:     */ 
/*  724:     */ 
/*  725:     */ 
/*  726:     */ 
/*  727:     */ 
/*  728:     */ 
/*  729:     */ 
/*  730:     */ 
/*  731:     */ 
/*  732:     */ 
/*  733:     */ 
/*  734:     */ 
/*  735:     */ 
/*  736:     */ 
/*  737:     */ 
/*  738:     */ 
/*  739:     */ 
/*  740:     */ 
/*  741:     */ 
/*  742:     */ 
/*  743:     */ 
/*  744:     */ 
/*  745:     */ 
/*  746:     */ 
/*  747:     */ 
/*  748:     */ 
/*  749:     */ 
/*  750:     */ 
/*  751:     */ 
/*  752:     */ 
/*  753:     */ 
/*  754:     */ 
/*  755:     */ 
/*  756:     */ 
/*  757:     */ 
/*  758:     */ 
/*  759:     */ 
/*  760:     */ 
/*  761:     */ 
/*  762:     */ 
/*  763:     */ 
/*  764:     */ 
/*  765:     */ 
/*  766:     */ 
/*  767:     */ 
/*  768:     */ 
/*  769:     */ 
/*  770:     */ 
/*  771:     */ 
/*  772:     */ 
/*  773:     */ 
/*  774:     */ 
/*  775:     */ 
/*  776:     */ 
/*  777:     */ 
/*  778:     */ 
/*  779:     */ 
/*  780:     */ 
/*  781:     */ 
/*  782:     */ 
/*  783:     */ 
/*  784:     */ 
/*  785:     */ 
/*  786:     */ 
/*  787:     */ 
/*  788:     */ 
/*  789:     */ 
/*  790:     */ 
/*  791:     */ 
/*  792:     */ 
/*  793:     */ 
/*  794:     */ 
/*  795:     */ 
/*  796:     */ 
/*  797:     */ 
/*  798:     */ 
/*  799:     */ 
/*  800:     */ 
/*  801:     */ 
/*  802:     */ 
/*  803:     */ 
/*  804:     */ 
/*  805:     */ 
/*  806:     */ 
/*  807:     */ 
/*  808:     */ 
/*  809:     */ 
/*  810:     */ 
/*  811:     */ 
/*  812:     */ 
/*  813:     */ 
/*  814:     */ 
/*  815:     */ 
/*  816:     */ 
/*  817:     */ 
/*  818:     */ 
/*  819:     */ 
/*  820:     */ 
/*  821:     */ 
/*  822:     */ 
/*  823:     */ 
/*  824:     */ 
/*  825:     */ 
/*  826:     */ 
/*  827:     */ 
/*  828:     */ 
/*  829:     */ 
/*  830:     */ 
/*  831:     */ 
/*  832:     */ 
/*  833:     */ 
/*  834:     */ 
/*  835:     */ 
/*  836:     */ 
/*  837:     */ 
/*  838:     */ 
/*  839:     */ 
/*  840:     */ 
/*  841:     */ 
/*  842:     */ 
/*  843:     */ 
/*  844:     */ 
/*  845:     */ 
/*  846:     */ 
/*  847:     */ 
/*  848:     */ 
/*  849:     */ 
/*  850:     */ 
/*  851:     */ 
/*  852:     */ 
/*  853:     */ 
/*  854:     */ 
/*  855:     */ 
/*  856:     */ 
/*  857:     */ 
/*  858:     */ 
/*  859:     */ 
/*  860:     */ 
/*  861:     */ 
/*  862:     */ 
/*  863:     */ 
/*  864: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/*  865: 811 */             if (bioReadCopyBuf == null)
/*  866:     */             {
/*  867: 812 */               dst.position(dst.position() + bytesProduced);
/*  868:     */             }
/*  869:     */             else
/*  870:     */             {
/*  871: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/*  872:     */               
/*  873: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/*  874: 817 */               bioReadCopyBuf.release();
/*  875:     */             }
/*  876: 625 */             return localSSLEngineResult1;
/*  877:     */           }
/*  878: 627 */           bytesProduced = bioLengthBefore - SSL.bioLengthByteBuffer(this.networkBIO);
/*  879: 628 */           SSLEngineResult localSSLEngineResult1 = newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, bytesProduced);
/*  880:     */           
/*  881:     */ 
/*  882:     */ 
/*  883:     */ 
/*  884:     */ 
/*  885:     */ 
/*  886:     */ 
/*  887:     */ 
/*  888:     */ 
/*  889:     */ 
/*  890:     */ 
/*  891:     */ 
/*  892:     */ 
/*  893:     */ 
/*  894:     */ 
/*  895:     */ 
/*  896:     */ 
/*  897:     */ 
/*  898:     */ 
/*  899:     */ 
/*  900:     */ 
/*  901:     */ 
/*  902:     */ 
/*  903:     */ 
/*  904:     */ 
/*  905:     */ 
/*  906:     */ 
/*  907:     */ 
/*  908:     */ 
/*  909:     */ 
/*  910:     */ 
/*  911:     */ 
/*  912:     */ 
/*  913:     */ 
/*  914:     */ 
/*  915:     */ 
/*  916:     */ 
/*  917:     */ 
/*  918:     */ 
/*  919:     */ 
/*  920:     */ 
/*  921:     */ 
/*  922:     */ 
/*  923:     */ 
/*  924:     */ 
/*  925:     */ 
/*  926:     */ 
/*  927:     */ 
/*  928:     */ 
/*  929:     */ 
/*  930:     */ 
/*  931:     */ 
/*  932:     */ 
/*  933:     */ 
/*  934:     */ 
/*  935:     */ 
/*  936:     */ 
/*  937:     */ 
/*  938:     */ 
/*  939:     */ 
/*  940:     */ 
/*  941:     */ 
/*  942:     */ 
/*  943:     */ 
/*  944:     */ 
/*  945:     */ 
/*  946:     */ 
/*  947:     */ 
/*  948:     */ 
/*  949:     */ 
/*  950:     */ 
/*  951:     */ 
/*  952:     */ 
/*  953:     */ 
/*  954:     */ 
/*  955:     */ 
/*  956:     */ 
/*  957:     */ 
/*  958:     */ 
/*  959:     */ 
/*  960:     */ 
/*  961:     */ 
/*  962:     */ 
/*  963:     */ 
/*  964:     */ 
/*  965:     */ 
/*  966:     */ 
/*  967:     */ 
/*  968:     */ 
/*  969:     */ 
/*  970:     */ 
/*  971:     */ 
/*  972:     */ 
/*  973:     */ 
/*  974:     */ 
/*  975:     */ 
/*  976:     */ 
/*  977:     */ 
/*  978:     */ 
/*  979:     */ 
/*  980:     */ 
/*  981:     */ 
/*  982:     */ 
/*  983:     */ 
/*  984:     */ 
/*  985:     */ 
/*  986:     */ 
/*  987:     */ 
/*  988:     */ 
/*  989:     */ 
/*  990:     */ 
/*  991:     */ 
/*  992:     */ 
/*  993:     */ 
/*  994:     */ 
/*  995:     */ 
/*  996:     */ 
/*  997:     */ 
/*  998:     */ 
/*  999:     */ 
/* 1000:     */ 
/* 1001:     */ 
/* 1002:     */ 
/* 1003:     */ 
/* 1004:     */ 
/* 1005:     */ 
/* 1006:     */ 
/* 1007:     */ 
/* 1008:     */ 
/* 1009:     */ 
/* 1010:     */ 
/* 1011:     */ 
/* 1012:     */ 
/* 1013:     */ 
/* 1014:     */ 
/* 1015:     */ 
/* 1016:     */ 
/* 1017:     */ 
/* 1018:     */ 
/* 1019:     */ 
/* 1020:     */ 
/* 1021:     */ 
/* 1022:     */ 
/* 1023:     */ 
/* 1024:     */ 
/* 1025:     */ 
/* 1026:     */ 
/* 1027:     */ 
/* 1028:     */ 
/* 1029:     */ 
/* 1030:     */ 
/* 1031:     */ 
/* 1032:     */ 
/* 1033:     */ 
/* 1034:     */ 
/* 1035:     */ 
/* 1036:     */ 
/* 1037:     */ 
/* 1038:     */ 
/* 1039:     */ 
/* 1040:     */ 
/* 1041:     */ 
/* 1042:     */ 
/* 1043:     */ 
/* 1044:     */ 
/* 1045:     */ 
/* 1046:     */ 
/* 1047:     */ 
/* 1048:     */ 
/* 1049:     */ 
/* 1050:     */ 
/* 1051:     */ 
/* 1052:     */ 
/* 1053:     */ 
/* 1054:     */ 
/* 1055:     */ 
/* 1056:     */ 
/* 1057:     */ 
/* 1058:     */ 
/* 1059:     */ 
/* 1060:     */ 
/* 1061: 810 */           SSL.bioClearByteBuffer(this.networkBIO);
/* 1062: 811 */           if (bioReadCopyBuf == null)
/* 1063:     */           {
/* 1064: 812 */             dst.position(dst.position() + bytesProduced);
/* 1065:     */           }
/* 1066:     */           else
/* 1067:     */           {
/* 1068: 814 */             assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1069:     */             
/* 1070: 816 */             dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1071: 817 */             bioReadCopyBuf.release();
/* 1072:     */           }
/* 1073: 628 */           return localSSLEngineResult1;
/* 1074:     */         }
/* 1075: 632 */         SSLEngineResult.HandshakeStatus status = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 1076: 634 */         if (this.handshakeState != HandshakeState.FINISHED)
/* 1077:     */         {
/* 1078: 635 */           if (this.handshakeState != HandshakeState.STARTED_EXPLICITLY) {
/* 1079: 637 */             this.handshakeState = HandshakeState.STARTED_IMPLICITLY;
/* 1080:     */           }
/* 1081: 641 */           bytesProduced = SSL.bioFlushByteBuffer(this.networkBIO);
/* 1082:     */           SSLEngineResult localSSLEngineResult2;
/* 1083: 643 */           if ((bytesProduced > 0) && (this.handshakeException != null))
/* 1084:     */           {
/* 1085: 652 */             localSSLEngineResult2 = newResult(SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, bytesProduced);
/* 1086:     */             
/* 1087:     */ 
/* 1088:     */ 
/* 1089:     */ 
/* 1090:     */ 
/* 1091:     */ 
/* 1092:     */ 
/* 1093:     */ 
/* 1094:     */ 
/* 1095:     */ 
/* 1096:     */ 
/* 1097:     */ 
/* 1098:     */ 
/* 1099:     */ 
/* 1100:     */ 
/* 1101:     */ 
/* 1102:     */ 
/* 1103:     */ 
/* 1104:     */ 
/* 1105:     */ 
/* 1106:     */ 
/* 1107:     */ 
/* 1108:     */ 
/* 1109:     */ 
/* 1110:     */ 
/* 1111:     */ 
/* 1112:     */ 
/* 1113:     */ 
/* 1114:     */ 
/* 1115:     */ 
/* 1116:     */ 
/* 1117:     */ 
/* 1118:     */ 
/* 1119:     */ 
/* 1120:     */ 
/* 1121:     */ 
/* 1122:     */ 
/* 1123:     */ 
/* 1124:     */ 
/* 1125:     */ 
/* 1126:     */ 
/* 1127:     */ 
/* 1128:     */ 
/* 1129:     */ 
/* 1130:     */ 
/* 1131:     */ 
/* 1132:     */ 
/* 1133:     */ 
/* 1134:     */ 
/* 1135:     */ 
/* 1136:     */ 
/* 1137:     */ 
/* 1138:     */ 
/* 1139:     */ 
/* 1140:     */ 
/* 1141:     */ 
/* 1142:     */ 
/* 1143:     */ 
/* 1144:     */ 
/* 1145:     */ 
/* 1146:     */ 
/* 1147:     */ 
/* 1148:     */ 
/* 1149:     */ 
/* 1150:     */ 
/* 1151:     */ 
/* 1152:     */ 
/* 1153:     */ 
/* 1154:     */ 
/* 1155:     */ 
/* 1156:     */ 
/* 1157:     */ 
/* 1158:     */ 
/* 1159:     */ 
/* 1160:     */ 
/* 1161:     */ 
/* 1162:     */ 
/* 1163:     */ 
/* 1164:     */ 
/* 1165:     */ 
/* 1166:     */ 
/* 1167:     */ 
/* 1168:     */ 
/* 1169:     */ 
/* 1170:     */ 
/* 1171:     */ 
/* 1172:     */ 
/* 1173:     */ 
/* 1174:     */ 
/* 1175:     */ 
/* 1176:     */ 
/* 1177:     */ 
/* 1178:     */ 
/* 1179:     */ 
/* 1180:     */ 
/* 1181:     */ 
/* 1182:     */ 
/* 1183:     */ 
/* 1184:     */ 
/* 1185:     */ 
/* 1186:     */ 
/* 1187:     */ 
/* 1188:     */ 
/* 1189:     */ 
/* 1190:     */ 
/* 1191:     */ 
/* 1192:     */ 
/* 1193:     */ 
/* 1194:     */ 
/* 1195:     */ 
/* 1196:     */ 
/* 1197:     */ 
/* 1198:     */ 
/* 1199:     */ 
/* 1200:     */ 
/* 1201:     */ 
/* 1202:     */ 
/* 1203:     */ 
/* 1204:     */ 
/* 1205:     */ 
/* 1206:     */ 
/* 1207:     */ 
/* 1208:     */ 
/* 1209:     */ 
/* 1210:     */ 
/* 1211:     */ 
/* 1212:     */ 
/* 1213:     */ 
/* 1214:     */ 
/* 1215:     */ 
/* 1216:     */ 
/* 1217:     */ 
/* 1218:     */ 
/* 1219:     */ 
/* 1220:     */ 
/* 1221:     */ 
/* 1222:     */ 
/* 1223:     */ 
/* 1224:     */ 
/* 1225:     */ 
/* 1226:     */ 
/* 1227:     */ 
/* 1228:     */ 
/* 1229:     */ 
/* 1230:     */ 
/* 1231:     */ 
/* 1232:     */ 
/* 1233:     */ 
/* 1234:     */ 
/* 1235:     */ 
/* 1236:     */ 
/* 1237:     */ 
/* 1238:     */ 
/* 1239:     */ 
/* 1240:     */ 
/* 1241:     */ 
/* 1242:     */ 
/* 1243: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/* 1244: 811 */             if (bioReadCopyBuf == null)
/* 1245:     */             {
/* 1246: 812 */               dst.position(dst.position() + bytesProduced);
/* 1247:     */             }
/* 1248:     */             else
/* 1249:     */             {
/* 1250: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1251:     */               
/* 1252: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1253: 817 */               bioReadCopyBuf.release();
/* 1254:     */             }
/* 1255: 652 */             return localSSLEngineResult2;
/* 1256:     */           }
/* 1257: 655 */           status = handshake();
/* 1258: 657 */           if ((this.renegotiationPending) && (status == SSLEngineResult.HandshakeStatus.FINISHED))
/* 1259:     */           {
/* 1260: 662 */             this.renegotiationPending = false;
/* 1261: 663 */             SSL.setState(this.ssl, SSL.SSL_ST_ACCEPT);
/* 1262: 664 */             this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
/* 1263: 665 */             status = handshake();
/* 1264:     */           }
/* 1265: 670 */           bytesProduced = bioLengthBefore - SSL.bioLengthByteBuffer(this.networkBIO);
/* 1266: 672 */           if (bytesProduced > 0)
/* 1267:     */           {
/* 1268: 676 */             localSSLEngineResult2 = newResult(mayFinishHandshake(status != SSLEngineResult.HandshakeStatus.FINISHED ? 
/* 1269:     */             
/* 1270: 678 */               getHandshakeStatus(SSL.bioLengthNonApplication(this.networkBIO)) : bytesProduced == bioLengthBefore ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.FINISHED), 0, bytesProduced);
/* 1271:     */             
/* 1272:     */ 
/* 1273:     */ 
/* 1274:     */ 
/* 1275:     */ 
/* 1276:     */ 
/* 1277:     */ 
/* 1278:     */ 
/* 1279:     */ 
/* 1280:     */ 
/* 1281:     */ 
/* 1282:     */ 
/* 1283:     */ 
/* 1284:     */ 
/* 1285:     */ 
/* 1286:     */ 
/* 1287:     */ 
/* 1288:     */ 
/* 1289:     */ 
/* 1290:     */ 
/* 1291:     */ 
/* 1292:     */ 
/* 1293:     */ 
/* 1294:     */ 
/* 1295:     */ 
/* 1296:     */ 
/* 1297:     */ 
/* 1298:     */ 
/* 1299:     */ 
/* 1300:     */ 
/* 1301:     */ 
/* 1302:     */ 
/* 1303:     */ 
/* 1304:     */ 
/* 1305:     */ 
/* 1306:     */ 
/* 1307:     */ 
/* 1308:     */ 
/* 1309:     */ 
/* 1310:     */ 
/* 1311:     */ 
/* 1312:     */ 
/* 1313:     */ 
/* 1314:     */ 
/* 1315:     */ 
/* 1316:     */ 
/* 1317:     */ 
/* 1318:     */ 
/* 1319:     */ 
/* 1320:     */ 
/* 1321:     */ 
/* 1322:     */ 
/* 1323:     */ 
/* 1324:     */ 
/* 1325:     */ 
/* 1326:     */ 
/* 1327:     */ 
/* 1328:     */ 
/* 1329:     */ 
/* 1330:     */ 
/* 1331:     */ 
/* 1332:     */ 
/* 1333:     */ 
/* 1334:     */ 
/* 1335:     */ 
/* 1336:     */ 
/* 1337:     */ 
/* 1338:     */ 
/* 1339:     */ 
/* 1340:     */ 
/* 1341:     */ 
/* 1342:     */ 
/* 1343:     */ 
/* 1344:     */ 
/* 1345:     */ 
/* 1346:     */ 
/* 1347:     */ 
/* 1348:     */ 
/* 1349:     */ 
/* 1350:     */ 
/* 1351:     */ 
/* 1352:     */ 
/* 1353:     */ 
/* 1354:     */ 
/* 1355:     */ 
/* 1356:     */ 
/* 1357:     */ 
/* 1358:     */ 
/* 1359:     */ 
/* 1360:     */ 
/* 1361:     */ 
/* 1362:     */ 
/* 1363:     */ 
/* 1364:     */ 
/* 1365:     */ 
/* 1366:     */ 
/* 1367:     */ 
/* 1368:     */ 
/* 1369:     */ 
/* 1370:     */ 
/* 1371:     */ 
/* 1372:     */ 
/* 1373:     */ 
/* 1374:     */ 
/* 1375:     */ 
/* 1376:     */ 
/* 1377:     */ 
/* 1378:     */ 
/* 1379:     */ 
/* 1380:     */ 
/* 1381:     */ 
/* 1382:     */ 
/* 1383:     */ 
/* 1384:     */ 
/* 1385:     */ 
/* 1386:     */ 
/* 1387:     */ 
/* 1388:     */ 
/* 1389:     */ 
/* 1390:     */ 
/* 1391:     */ 
/* 1392:     */ 
/* 1393:     */ 
/* 1394:     */ 
/* 1395:     */ 
/* 1396:     */ 
/* 1397:     */ 
/* 1398:     */ 
/* 1399:     */ 
/* 1400:     */ 
/* 1401:     */ 
/* 1402: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/* 1403: 811 */             if (bioReadCopyBuf == null)
/* 1404:     */             {
/* 1405: 812 */               dst.position(dst.position() + bytesProduced);
/* 1406:     */             }
/* 1407:     */             else
/* 1408:     */             {
/* 1409: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1410:     */               
/* 1411: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1412: 817 */               bioReadCopyBuf.release();
/* 1413:     */             }
/* 1414: 676 */             return localSSLEngineResult2;
/* 1415:     */           }
/* 1416: 682 */           if (status == SSLEngineResult.HandshakeStatus.NEED_UNWRAP)
/* 1417:     */           {
/* 1418: 684 */             localSSLEngineResult2 = isOutboundDone() ? NEED_UNWRAP_CLOSED : NEED_UNWRAP_OK;
/* 1419:     */             
/* 1420:     */ 
/* 1421:     */ 
/* 1422:     */ 
/* 1423:     */ 
/* 1424:     */ 
/* 1425:     */ 
/* 1426:     */ 
/* 1427:     */ 
/* 1428:     */ 
/* 1429:     */ 
/* 1430:     */ 
/* 1431:     */ 
/* 1432:     */ 
/* 1433:     */ 
/* 1434:     */ 
/* 1435:     */ 
/* 1436:     */ 
/* 1437:     */ 
/* 1438:     */ 
/* 1439:     */ 
/* 1440:     */ 
/* 1441:     */ 
/* 1442:     */ 
/* 1443:     */ 
/* 1444:     */ 
/* 1445:     */ 
/* 1446:     */ 
/* 1447:     */ 
/* 1448:     */ 
/* 1449:     */ 
/* 1450:     */ 
/* 1451:     */ 
/* 1452:     */ 
/* 1453:     */ 
/* 1454:     */ 
/* 1455:     */ 
/* 1456:     */ 
/* 1457:     */ 
/* 1458:     */ 
/* 1459:     */ 
/* 1460:     */ 
/* 1461:     */ 
/* 1462:     */ 
/* 1463:     */ 
/* 1464:     */ 
/* 1465:     */ 
/* 1466:     */ 
/* 1467:     */ 
/* 1468:     */ 
/* 1469:     */ 
/* 1470:     */ 
/* 1471:     */ 
/* 1472:     */ 
/* 1473:     */ 
/* 1474:     */ 
/* 1475:     */ 
/* 1476:     */ 
/* 1477:     */ 
/* 1478:     */ 
/* 1479:     */ 
/* 1480:     */ 
/* 1481:     */ 
/* 1482:     */ 
/* 1483:     */ 
/* 1484:     */ 
/* 1485:     */ 
/* 1486:     */ 
/* 1487:     */ 
/* 1488:     */ 
/* 1489:     */ 
/* 1490:     */ 
/* 1491:     */ 
/* 1492:     */ 
/* 1493:     */ 
/* 1494:     */ 
/* 1495:     */ 
/* 1496:     */ 
/* 1497:     */ 
/* 1498:     */ 
/* 1499:     */ 
/* 1500:     */ 
/* 1501:     */ 
/* 1502:     */ 
/* 1503:     */ 
/* 1504:     */ 
/* 1505:     */ 
/* 1506:     */ 
/* 1507:     */ 
/* 1508:     */ 
/* 1509:     */ 
/* 1510:     */ 
/* 1511:     */ 
/* 1512:     */ 
/* 1513:     */ 
/* 1514:     */ 
/* 1515:     */ 
/* 1516:     */ 
/* 1517:     */ 
/* 1518:     */ 
/* 1519:     */ 
/* 1520:     */ 
/* 1521:     */ 
/* 1522:     */ 
/* 1523:     */ 
/* 1524:     */ 
/* 1525:     */ 
/* 1526:     */ 
/* 1527:     */ 
/* 1528:     */ 
/* 1529:     */ 
/* 1530:     */ 
/* 1531:     */ 
/* 1532:     */ 
/* 1533:     */ 
/* 1534:     */ 
/* 1535:     */ 
/* 1536:     */ 
/* 1537:     */ 
/* 1538:     */ 
/* 1539:     */ 
/* 1540:     */ 
/* 1541:     */ 
/* 1542:     */ 
/* 1543:     */ 
/* 1544: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/* 1545: 811 */             if (bioReadCopyBuf == null)
/* 1546:     */             {
/* 1547: 812 */               dst.position(dst.position() + bytesProduced);
/* 1548:     */             }
/* 1549:     */             else
/* 1550:     */             {
/* 1551: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1552:     */               
/* 1553: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1554: 817 */               bioReadCopyBuf.release();
/* 1555:     */             }
/* 1556: 684 */             return localSSLEngineResult2;
/* 1557:     */           }
/* 1558: 689 */           if (this.outboundClosed)
/* 1559:     */           {
/* 1560: 690 */             bytesProduced = SSL.bioFlushByteBuffer(this.networkBIO);
/* 1561: 691 */             localSSLEngineResult2 = newResultMayFinishHandshake(status, 0, bytesProduced);
/* 1562:     */             
/* 1563:     */ 
/* 1564:     */ 
/* 1565:     */ 
/* 1566:     */ 
/* 1567:     */ 
/* 1568:     */ 
/* 1569:     */ 
/* 1570:     */ 
/* 1571:     */ 
/* 1572:     */ 
/* 1573:     */ 
/* 1574:     */ 
/* 1575:     */ 
/* 1576:     */ 
/* 1577:     */ 
/* 1578:     */ 
/* 1579:     */ 
/* 1580:     */ 
/* 1581:     */ 
/* 1582:     */ 
/* 1583:     */ 
/* 1584:     */ 
/* 1585:     */ 
/* 1586:     */ 
/* 1587:     */ 
/* 1588:     */ 
/* 1589:     */ 
/* 1590:     */ 
/* 1591:     */ 
/* 1592:     */ 
/* 1593:     */ 
/* 1594:     */ 
/* 1595:     */ 
/* 1596:     */ 
/* 1597:     */ 
/* 1598:     */ 
/* 1599:     */ 
/* 1600:     */ 
/* 1601:     */ 
/* 1602:     */ 
/* 1603:     */ 
/* 1604:     */ 
/* 1605:     */ 
/* 1606:     */ 
/* 1607:     */ 
/* 1608:     */ 
/* 1609:     */ 
/* 1610:     */ 
/* 1611:     */ 
/* 1612:     */ 
/* 1613:     */ 
/* 1614:     */ 
/* 1615:     */ 
/* 1616:     */ 
/* 1617:     */ 
/* 1618:     */ 
/* 1619:     */ 
/* 1620:     */ 
/* 1621:     */ 
/* 1622:     */ 
/* 1623:     */ 
/* 1624:     */ 
/* 1625:     */ 
/* 1626:     */ 
/* 1627:     */ 
/* 1628:     */ 
/* 1629:     */ 
/* 1630:     */ 
/* 1631:     */ 
/* 1632:     */ 
/* 1633:     */ 
/* 1634:     */ 
/* 1635:     */ 
/* 1636:     */ 
/* 1637:     */ 
/* 1638:     */ 
/* 1639:     */ 
/* 1640:     */ 
/* 1641:     */ 
/* 1642:     */ 
/* 1643:     */ 
/* 1644:     */ 
/* 1645:     */ 
/* 1646:     */ 
/* 1647:     */ 
/* 1648:     */ 
/* 1649:     */ 
/* 1650:     */ 
/* 1651:     */ 
/* 1652:     */ 
/* 1653:     */ 
/* 1654:     */ 
/* 1655:     */ 
/* 1656:     */ 
/* 1657:     */ 
/* 1658:     */ 
/* 1659:     */ 
/* 1660:     */ 
/* 1661:     */ 
/* 1662:     */ 
/* 1663:     */ 
/* 1664:     */ 
/* 1665:     */ 
/* 1666:     */ 
/* 1667:     */ 
/* 1668:     */ 
/* 1669:     */ 
/* 1670:     */ 
/* 1671:     */ 
/* 1672:     */ 
/* 1673:     */ 
/* 1674:     */ 
/* 1675:     */ 
/* 1676:     */ 
/* 1677:     */ 
/* 1678:     */ 
/* 1679:     */ 
/* 1680: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/* 1681: 811 */             if (bioReadCopyBuf == null)
/* 1682:     */             {
/* 1683: 812 */               dst.position(dst.position() + bytesProduced);
/* 1684:     */             }
/* 1685:     */             else
/* 1686:     */             {
/* 1687: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1688:     */               
/* 1689: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1690: 817 */               bioReadCopyBuf.release();
/* 1691:     */             }
/* 1692: 691 */             return localSSLEngineResult2;
/* 1693:     */           }
/* 1694:     */         }
/* 1695: 695 */         int endOffset = offset + length;
/* 1696: 696 */         if (this.jdkCompatibilityMode)
/* 1697:     */         {
/* 1698: 697 */           int srcsLen = 0;
/* 1699: 698 */           for (int i = offset; i < endOffset; i++)
/* 1700:     */           {
/* 1701: 699 */             ByteBuffer src = srcs[i];
/* 1702: 700 */             if (src == null) {
/* 1703: 701 */               throw new IllegalArgumentException("srcs[" + i + "] is null");
/* 1704:     */             }
/* 1705: 703 */             if (srcsLen != MAX_PLAINTEXT_LENGTH)
/* 1706:     */             {
/* 1707: 707 */               srcsLen += src.remaining();
/* 1708: 708 */               if ((srcsLen > MAX_PLAINTEXT_LENGTH) || (srcsLen < 0)) {
/* 1709: 712 */                 srcsLen = MAX_PLAINTEXT_LENGTH;
/* 1710:     */               }
/* 1711:     */             }
/* 1712:     */           }
/* 1713: 718 */           if (!isBytesAvailableEnoughForWrap(dst.remaining(), srcsLen, 1))
/* 1714:     */           {
/* 1715: 719 */             i = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, getHandshakeStatus(), 0, 0);
/* 1716:     */             
/* 1717:     */ 
/* 1718:     */ 
/* 1719:     */ 
/* 1720:     */ 
/* 1721:     */ 
/* 1722:     */ 
/* 1723:     */ 
/* 1724:     */ 
/* 1725:     */ 
/* 1726:     */ 
/* 1727:     */ 
/* 1728:     */ 
/* 1729:     */ 
/* 1730:     */ 
/* 1731:     */ 
/* 1732:     */ 
/* 1733:     */ 
/* 1734:     */ 
/* 1735:     */ 
/* 1736:     */ 
/* 1737:     */ 
/* 1738:     */ 
/* 1739:     */ 
/* 1740:     */ 
/* 1741:     */ 
/* 1742:     */ 
/* 1743:     */ 
/* 1744:     */ 
/* 1745:     */ 
/* 1746:     */ 
/* 1747:     */ 
/* 1748:     */ 
/* 1749:     */ 
/* 1750:     */ 
/* 1751:     */ 
/* 1752:     */ 
/* 1753:     */ 
/* 1754:     */ 
/* 1755:     */ 
/* 1756:     */ 
/* 1757:     */ 
/* 1758:     */ 
/* 1759:     */ 
/* 1760:     */ 
/* 1761:     */ 
/* 1762:     */ 
/* 1763:     */ 
/* 1764:     */ 
/* 1765:     */ 
/* 1766:     */ 
/* 1767:     */ 
/* 1768:     */ 
/* 1769:     */ 
/* 1770:     */ 
/* 1771:     */ 
/* 1772:     */ 
/* 1773:     */ 
/* 1774:     */ 
/* 1775:     */ 
/* 1776:     */ 
/* 1777:     */ 
/* 1778:     */ 
/* 1779:     */ 
/* 1780:     */ 
/* 1781:     */ 
/* 1782:     */ 
/* 1783:     */ 
/* 1784:     */ 
/* 1785:     */ 
/* 1786:     */ 
/* 1787:     */ 
/* 1788:     */ 
/* 1789:     */ 
/* 1790:     */ 
/* 1791:     */ 
/* 1792:     */ 
/* 1793:     */ 
/* 1794:     */ 
/* 1795:     */ 
/* 1796:     */ 
/* 1797:     */ 
/* 1798:     */ 
/* 1799:     */ 
/* 1800:     */ 
/* 1801:     */ 
/* 1802:     */ 
/* 1803:     */ 
/* 1804:     */ 
/* 1805:     */ 
/* 1806: 810 */             SSL.bioClearByteBuffer(this.networkBIO);
/* 1807: 811 */             if (bioReadCopyBuf == null)
/* 1808:     */             {
/* 1809: 812 */               dst.position(dst.position() + bytesProduced);
/* 1810:     */             }
/* 1811:     */             else
/* 1812:     */             {
/* 1813: 814 */               assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1814:     */               
/* 1815: 816 */               dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1816: 817 */               bioReadCopyBuf.release();
/* 1817:     */             }
/* 1818: 719 */             return i;
/* 1819:     */           }
/* 1820:     */         }
/* 1821: 724 */         int bytesConsumed = 0;
/* 1822:     */         
/* 1823: 726 */         bytesProduced = SSL.bioFlushByteBuffer(this.networkBIO);
/* 1824: 727 */         for (; offset < endOffset; offset++)
/* 1825:     */         {
/* 1826: 728 */           src = srcs[offset];
/* 1827: 729 */           int remaining = src.remaining();
/* 1828: 730 */           if (remaining != 0)
/* 1829:     */           {
/* 1830:     */             int bytesWritten;
/* 1831:     */             SSLEngineResult localSSLEngineResult3;
/* 1832:     */             int bytesWritten;
/* 1833: 735 */             if (this.jdkCompatibilityMode)
/* 1834:     */             {
/* 1835: 739 */               bytesWritten = writePlaintextData(src, Math.min(remaining, MAX_PLAINTEXT_LENGTH - bytesConsumed));
/* 1836:     */             }
/* 1837:     */             else
/* 1838:     */             {
/* 1839: 744 */               int availableCapacityForWrap = dst.remaining() - bytesProduced - this.maxWrapOverhead;
/* 1840: 745 */               if (availableCapacityForWrap <= 0)
/* 1841:     */               {
/* 1842: 746 */                 localSSLEngineResult3 = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, getHandshakeStatus(), bytesConsumed, bytesProduced);
/* 1843:     */                 
/* 1844:     */ 
/* 1845:     */ 
/* 1846:     */ 
/* 1847:     */ 
/* 1848:     */ 
/* 1849:     */ 
/* 1850:     */ 
/* 1851:     */ 
/* 1852:     */ 
/* 1853:     */ 
/* 1854:     */ 
/* 1855:     */ 
/* 1856:     */ 
/* 1857:     */ 
/* 1858:     */ 
/* 1859:     */ 
/* 1860:     */ 
/* 1861:     */ 
/* 1862:     */ 
/* 1863:     */ 
/* 1864:     */ 
/* 1865:     */ 
/* 1866:     */ 
/* 1867:     */ 
/* 1868:     */ 
/* 1869:     */ 
/* 1870:     */ 
/* 1871:     */ 
/* 1872:     */ 
/* 1873:     */ 
/* 1874:     */ 
/* 1875:     */ 
/* 1876:     */ 
/* 1877:     */ 
/* 1878:     */ 
/* 1879:     */ 
/* 1880:     */ 
/* 1881:     */ 
/* 1882:     */ 
/* 1883:     */ 
/* 1884:     */ 
/* 1885:     */ 
/* 1886:     */ 
/* 1887:     */ 
/* 1888:     */ 
/* 1889:     */ 
/* 1890:     */ 
/* 1891:     */ 
/* 1892:     */ 
/* 1893:     */ 
/* 1894:     */ 
/* 1895:     */ 
/* 1896:     */ 
/* 1897:     */ 
/* 1898:     */ 
/* 1899:     */ 
/* 1900:     */ 
/* 1901:     */ 
/* 1902:     */ 
/* 1903:     */ 
/* 1904:     */ 
/* 1905:     */ 
/* 1906: 810 */                 SSL.bioClearByteBuffer(this.networkBIO);
/* 1907: 811 */                 if (bioReadCopyBuf == null)
/* 1908:     */                 {
/* 1909: 812 */                   dst.position(dst.position() + bytesProduced);
/* 1910:     */                 }
/* 1911:     */                 else
/* 1912:     */                 {
/* 1913: 814 */                   assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1914:     */                   
/* 1915: 816 */                   dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1916: 817 */                   bioReadCopyBuf.release();
/* 1917:     */                 }
/* 1918: 746 */                 return localSSLEngineResult3;
/* 1919:     */               }
/* 1920: 749 */               bytesWritten = writePlaintextData(src, Math.min(remaining, availableCapacityForWrap));
/* 1921:     */             }
/* 1922: 752 */             if (bytesWritten > 0)
/* 1923:     */             {
/* 1924: 753 */               bytesConsumed += bytesWritten;
/* 1925:     */               
/* 1926:     */ 
/* 1927: 756 */               int pendingNow = SSL.bioLengthByteBuffer(this.networkBIO);
/* 1928: 757 */               bytesProduced += bioLengthBefore - pendingNow;
/* 1929: 758 */               bioLengthBefore = pendingNow;
/* 1930: 760 */               if ((this.jdkCompatibilityMode) || (bytesProduced == dst.remaining()))
/* 1931:     */               {
/* 1932: 761 */                 localSSLEngineResult3 = newResultMayFinishHandshake(status, bytesConsumed, bytesProduced);
/* 1933:     */                 
/* 1934:     */ 
/* 1935:     */ 
/* 1936:     */ 
/* 1937:     */ 
/* 1938:     */ 
/* 1939:     */ 
/* 1940:     */ 
/* 1941:     */ 
/* 1942:     */ 
/* 1943:     */ 
/* 1944:     */ 
/* 1945:     */ 
/* 1946:     */ 
/* 1947:     */ 
/* 1948:     */ 
/* 1949:     */ 
/* 1950:     */ 
/* 1951:     */ 
/* 1952:     */ 
/* 1953:     */ 
/* 1954:     */ 
/* 1955:     */ 
/* 1956:     */ 
/* 1957:     */ 
/* 1958:     */ 
/* 1959:     */ 
/* 1960:     */ 
/* 1961:     */ 
/* 1962:     */ 
/* 1963:     */ 
/* 1964:     */ 
/* 1965:     */ 
/* 1966:     */ 
/* 1967:     */ 
/* 1968:     */ 
/* 1969:     */ 
/* 1970:     */ 
/* 1971:     */ 
/* 1972:     */ 
/* 1973:     */ 
/* 1974:     */ 
/* 1975:     */ 
/* 1976:     */ 
/* 1977:     */ 
/* 1978:     */ 
/* 1979:     */ 
/* 1980:     */ 
/* 1981: 810 */                 SSL.bioClearByteBuffer(this.networkBIO);
/* 1982: 811 */                 if (bioReadCopyBuf == null)
/* 1983:     */                 {
/* 1984: 812 */                   dst.position(dst.position() + bytesProduced);
/* 1985:     */                 }
/* 1986:     */                 else
/* 1987:     */                 {
/* 1988: 814 */                   assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 1989:     */                   
/* 1990: 816 */                   dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 1991: 817 */                   bioReadCopyBuf.release();
/* 1992:     */                 }
/* 1993: 761 */                 return localSSLEngineResult3;
/* 1994:     */               }
/* 1995:     */             }
/* 1996:     */             else
/* 1997:     */             {
/* 1998: 764 */               int sslError = SSL.getError(this.ssl, bytesWritten);
/* 1999:     */               Object hs;
/* 2000: 765 */               if (sslError == SSL.SSL_ERROR_ZERO_RETURN)
/* 2001:     */               {
/* 2002: 767 */                 if (!this.receivedShutdown)
/* 2003:     */                 {
/* 2004: 768 */                   closeAll();
/* 2005:     */                   
/* 2006: 770 */                   bytesProduced += bioLengthBefore - SSL.bioLengthByteBuffer(this.networkBIO);
/* 2007:     */                   
/* 2008:     */ 
/* 2009:     */ 
/* 2010:     */ 
/* 2011: 775 */                   hs = mayFinishHandshake(status != SSLEngineResult.HandshakeStatus.FINISHED ? 
/* 2012:     */                   
/* 2013: 777 */                     getHandshakeStatus(SSL.bioLengthNonApplication(this.networkBIO)) : bytesProduced == dst.remaining() ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.FINISHED);
/* 2014:     */                   
/* 2015: 779 */                   SSLEngineResult localSSLEngineResult4 = newResult((SSLEngineResult.HandshakeStatus)hs, bytesConsumed, bytesProduced);
/* 2016:     */                   
/* 2017:     */ 
/* 2018:     */ 
/* 2019:     */ 
/* 2020:     */ 
/* 2021:     */ 
/* 2022:     */ 
/* 2023:     */ 
/* 2024:     */ 
/* 2025:     */ 
/* 2026:     */ 
/* 2027:     */ 
/* 2028:     */ 
/* 2029:     */ 
/* 2030:     */ 
/* 2031:     */ 
/* 2032:     */ 
/* 2033:     */ 
/* 2034:     */ 
/* 2035:     */ 
/* 2036:     */ 
/* 2037:     */ 
/* 2038:     */ 
/* 2039:     */ 
/* 2040:     */ 
/* 2041:     */ 
/* 2042:     */ 
/* 2043:     */ 
/* 2044:     */ 
/* 2045:     */ 
/* 2046: 810 */                   SSL.bioClearByteBuffer(this.networkBIO);
/* 2047: 811 */                   if (bioReadCopyBuf == null)
/* 2048:     */                   {
/* 2049: 812 */                     dst.position(dst.position() + bytesProduced);
/* 2050:     */                   }
/* 2051:     */                   else
/* 2052:     */                   {
/* 2053: 814 */                     assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 2054:     */                     
/* 2055: 816 */                     dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 2056: 817 */                     bioReadCopyBuf.release();
/* 2057:     */                   }
/* 2058: 779 */                   return localSSLEngineResult4;
/* 2059:     */                 }
/* 2060: 782 */                 hs = newResult(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, bytesConsumed, bytesProduced);
/* 2061:     */                 
/* 2062:     */ 
/* 2063:     */ 
/* 2064:     */ 
/* 2065:     */ 
/* 2066:     */ 
/* 2067:     */ 
/* 2068:     */ 
/* 2069:     */ 
/* 2070:     */ 
/* 2071:     */ 
/* 2072:     */ 
/* 2073:     */ 
/* 2074:     */ 
/* 2075:     */ 
/* 2076:     */ 
/* 2077:     */ 
/* 2078:     */ 
/* 2079:     */ 
/* 2080:     */ 
/* 2081:     */ 
/* 2082:     */ 
/* 2083:     */ 
/* 2084:     */ 
/* 2085:     */ 
/* 2086:     */ 
/* 2087:     */ 
/* 2088: 810 */                 SSL.bioClearByteBuffer(this.networkBIO);
/* 2089: 811 */                 if (bioReadCopyBuf == null)
/* 2090:     */                 {
/* 2091: 812 */                   dst.position(dst.position() + bytesProduced);
/* 2092:     */                 }
/* 2093:     */                 else
/* 2094:     */                 {
/* 2095: 814 */                   assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 2096:     */                   
/* 2097: 816 */                   dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 2098: 817 */                   bioReadCopyBuf.release();
/* 2099:     */                 }
/* 2100: 782 */                 return hs;
/* 2101:     */               }
/* 2102: 783 */               if (sslError == SSL.SSL_ERROR_WANT_READ)
/* 2103:     */               {
/* 2104: 787 */                 hs = newResult(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, bytesConsumed, bytesProduced);
/* 2105:     */                 
/* 2106:     */ 
/* 2107:     */ 
/* 2108:     */ 
/* 2109:     */ 
/* 2110:     */ 
/* 2111:     */ 
/* 2112:     */ 
/* 2113:     */ 
/* 2114:     */ 
/* 2115:     */ 
/* 2116:     */ 
/* 2117:     */ 
/* 2118:     */ 
/* 2119:     */ 
/* 2120:     */ 
/* 2121:     */ 
/* 2122:     */ 
/* 2123:     */ 
/* 2124:     */ 
/* 2125:     */ 
/* 2126:     */ 
/* 2127: 810 */                 SSL.bioClearByteBuffer(this.networkBIO);
/* 2128: 811 */                 if (bioReadCopyBuf == null)
/* 2129:     */                 {
/* 2130: 812 */                   dst.position(dst.position() + bytesProduced);
/* 2131:     */                 }
/* 2132:     */                 else
/* 2133:     */                 {
/* 2134: 814 */                   assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 2135:     */                   
/* 2136: 816 */                   dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 2137: 817 */                   bioReadCopyBuf.release();
/* 2138:     */                 }
/* 2139: 787 */                 return hs;
/* 2140:     */               }
/* 2141: 788 */               if (sslError == SSL.SSL_ERROR_WANT_WRITE)
/* 2142:     */               {
/* 2143: 801 */                 hs = newResult(SSLEngineResult.Status.BUFFER_OVERFLOW, status, bytesConsumed, bytesProduced);
/* 2144:     */                 
/* 2145:     */ 
/* 2146:     */ 
/* 2147:     */ 
/* 2148:     */ 
/* 2149:     */ 
/* 2150:     */ 
/* 2151:     */ 
/* 2152: 810 */                 SSL.bioClearByteBuffer(this.networkBIO);
/* 2153: 811 */                 if (bioReadCopyBuf == null)
/* 2154:     */                 {
/* 2155: 812 */                   dst.position(dst.position() + bytesProduced);
/* 2156:     */                 }
/* 2157:     */                 else
/* 2158:     */                 {
/* 2159: 814 */                   assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 2160:     */                   
/* 2161: 816 */                   dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 2162: 817 */                   bioReadCopyBuf.release();
/* 2163:     */                 }
/* 2164: 801 */                 return hs;
/* 2165:     */               }
/* 2166: 804 */               throw shutdownWithError("SSL_write");
/* 2167:     */             }
/* 2168:     */           }
/* 2169:     */         }
/* 2170: 808 */         ByteBuffer src = newResultMayFinishHandshake(status, bytesConsumed, bytesProduced);
/* 2171:     */         
/* 2172: 810 */         SSL.bioClearByteBuffer(this.networkBIO);
/* 2173: 811 */         if (bioReadCopyBuf == null)
/* 2174:     */         {
/* 2175: 812 */           dst.position(dst.position() + bytesProduced);
/* 2176:     */         }
/* 2177:     */         else
/* 2178:     */         {
/* 2179: 814 */           assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 2180:     */           
/* 2181: 816 */           dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 2182: 817 */           bioReadCopyBuf.release();
/* 2183:     */         }
/* 2184: 808 */         return src;
/* 2185:     */       }
/* 2186:     */       finally
/* 2187:     */       {
/* 2188: 810 */         SSL.bioClearByteBuffer(this.networkBIO);
/* 2189: 811 */         if (bioReadCopyBuf == null)
/* 2190:     */         {
/* 2191: 812 */           dst.position(dst.position() + bytesProduced);
/* 2192:     */         }
/* 2193:     */         else
/* 2194:     */         {
/* 2195: 814 */           assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : ("The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf);
/* 2196:     */           
/* 2197: 816 */           dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
/* 2198: 817 */           bioReadCopyBuf.release();
/* 2199:     */         }
/* 2200:     */       }
/* 2201:     */     }
/* 2202:     */   }
/* 2203:     */   
/* 2204:     */   private SSLEngineResult newResult(SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced)
/* 2205:     */   {
/* 2206: 824 */     return newResult(SSLEngineResult.Status.OK, hs, bytesConsumed, bytesProduced);
/* 2207:     */   }
/* 2208:     */   
/* 2209:     */   private SSLEngineResult newResult(SSLEngineResult.Status status, SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced)
/* 2210:     */   {
/* 2211: 832 */     if (isOutboundDone())
/* 2212:     */     {
/* 2213: 833 */       if (isInboundDone())
/* 2214:     */       {
/* 2215: 835 */         hs = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 2216:     */         
/* 2217:     */ 
/* 2218: 838 */         shutdown();
/* 2219:     */       }
/* 2220: 840 */       return new SSLEngineResult(SSLEngineResult.Status.CLOSED, hs, bytesConsumed, bytesProduced);
/* 2221:     */     }
/* 2222: 842 */     return new SSLEngineResult(status, hs, bytesConsumed, bytesProduced);
/* 2223:     */   }
/* 2224:     */   
/* 2225:     */   private SSLEngineResult newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced)
/* 2226:     */     throws SSLException
/* 2227:     */   {
/* 2228: 847 */     return newResult(mayFinishHandshake(hs != SSLEngineResult.HandshakeStatus.FINISHED ? getHandshakeStatus() : SSLEngineResult.HandshakeStatus.FINISHED), bytesConsumed, bytesProduced);
/* 2229:     */   }
/* 2230:     */   
/* 2231:     */   private SSLEngineResult newResultMayFinishHandshake(SSLEngineResult.Status status, SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced)
/* 2232:     */     throws SSLException
/* 2233:     */   {
/* 2234: 854 */     return newResult(status, mayFinishHandshake(hs != SSLEngineResult.HandshakeStatus.FINISHED ? getHandshakeStatus() : SSLEngineResult.HandshakeStatus.FINISHED), bytesConsumed, bytesProduced);
/* 2235:     */   }
/* 2236:     */   
/* 2237:     */   private SSLException shutdownWithError(String operations)
/* 2238:     */   {
/* 2239: 862 */     String err = SSL.getLastError();
/* 2240: 863 */     return shutdownWithError(operations, err);
/* 2241:     */   }
/* 2242:     */   
/* 2243:     */   private SSLException shutdownWithError(String operation, String err)
/* 2244:     */   {
/* 2245: 867 */     if (logger.isDebugEnabled()) {
/* 2246: 868 */       logger.debug("{} failed: OpenSSL error: {}", operation, err);
/* 2247:     */     }
/* 2248: 872 */     shutdown();
/* 2249: 873 */     if (this.handshakeState == HandshakeState.FINISHED) {
/* 2250: 874 */       return new SSLException(err);
/* 2251:     */     }
/* 2252: 876 */     return new SSLHandshakeException(err);
/* 2253:     */   }
/* 2254:     */   
/* 2255:     */   public final SSLEngineResult unwrap(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength)
/* 2256:     */     throws SSLException
/* 2257:     */   {
/* 2258: 884 */     if (srcs == null) {
/* 2259: 885 */       throw new NullPointerException("srcs");
/* 2260:     */     }
/* 2261: 887 */     if ((srcsOffset >= srcs.length) || (srcsOffset + srcsLength > srcs.length)) {
/* 2262: 889 */       throw new IndexOutOfBoundsException("offset: " + srcsOffset + ", length: " + srcsLength + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
/* 2263:     */     }
/* 2264: 893 */     if (dsts == null) {
/* 2265: 894 */       throw new IllegalArgumentException("dsts is null");
/* 2266:     */     }
/* 2267: 896 */     if ((dstsOffset >= dsts.length) || (dstsOffset + dstsLength > dsts.length)) {
/* 2268: 897 */       throw new IndexOutOfBoundsException("offset: " + dstsOffset + ", length: " + dstsLength + " (expected: offset <= offset + length <= dsts.length (" + dsts.length + "))");
/* 2269:     */     }
/* 2270: 901 */     long capacity = 0L;
/* 2271: 902 */     int dstsEndOffset = dstsOffset + dstsLength;
/* 2272: 903 */     for (int i = dstsOffset; i < dstsEndOffset; i++)
/* 2273:     */     {
/* 2274: 904 */       ByteBuffer dst = dsts[i];
/* 2275: 905 */       if (dst == null) {
/* 2276: 906 */         throw new IllegalArgumentException("dsts[" + i + "] is null");
/* 2277:     */       }
/* 2278: 908 */       if (dst.isReadOnly()) {
/* 2279: 909 */         throw new ReadOnlyBufferException();
/* 2280:     */       }
/* 2281: 911 */       capacity += dst.remaining();
/* 2282:     */     }
/* 2283: 914 */     int srcsEndOffset = srcsOffset + srcsLength;
/* 2284: 915 */     long len = 0L;
/* 2285: 916 */     for (int i = srcsOffset; i < srcsEndOffset; i++)
/* 2286:     */     {
/* 2287: 917 */       ByteBuffer src = srcs[i];
/* 2288: 918 */       if (src == null) {
/* 2289: 919 */         throw new IllegalArgumentException("srcs[" + i + "] is null");
/* 2290:     */       }
/* 2291: 921 */       len += src.remaining();
/* 2292:     */     }
/* 2293: 924 */     synchronized (this)
/* 2294:     */     {
/* 2295: 925 */       if (isInboundDone()) {
/* 2296: 926 */         return (isOutboundDone()) || (isDestroyed()) ? CLOSED_NOT_HANDSHAKING : NEED_WRAP_CLOSED;
/* 2297:     */       }
/* 2298: 929 */       SSLEngineResult.HandshakeStatus status = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 2299: 931 */       if (this.handshakeState != HandshakeState.FINISHED)
/* 2300:     */       {
/* 2301: 932 */         if (this.handshakeState != HandshakeState.STARTED_EXPLICITLY) {
/* 2302: 934 */           this.handshakeState = HandshakeState.STARTED_IMPLICITLY;
/* 2303:     */         }
/* 2304: 937 */         status = handshake();
/* 2305: 938 */         if (status == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
/* 2306: 939 */           return NEED_WRAP_OK;
/* 2307:     */         }
/* 2308: 942 */         if (this.isInboundDone) {
/* 2309: 943 */           return NEED_WRAP_CLOSED;
/* 2310:     */         }
/* 2311:     */       }
/* 2312: 947 */       int sslPending = sslPending0();
/* 2313:     */       int packetLength;
/* 2314: 953 */       if (this.jdkCompatibilityMode)
/* 2315:     */       {
/* 2316: 954 */         if (len < 5L) {
/* 2317: 955 */           return newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_UNDERFLOW, status, 0, 0);
/* 2318:     */         }
/* 2319: 958 */         int packetLength = SslUtils.getEncryptedPacketLength(srcs, srcsOffset);
/* 2320: 959 */         if (packetLength == -2) {
/* 2321: 960 */           throw new NotSslRecordException("not an SSL/TLS record");
/* 2322:     */         }
/* 2323: 963 */         int packetLengthDataOnly = packetLength - 5;
/* 2324: 964 */         if (packetLengthDataOnly > capacity)
/* 2325:     */         {
/* 2326: 967 */           if (packetLengthDataOnly > MAX_RECORD_SIZE) {
/* 2327: 974 */             throw new SSLException("Illegal packet length: " + packetLengthDataOnly + " > " + this.session.getApplicationBufferSize());
/* 2328:     */           }
/* 2329: 976 */           this.session.tryExpandApplicationBufferSize(packetLengthDataOnly);
/* 2330:     */           
/* 2331: 978 */           return newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_OVERFLOW, status, 0, 0);
/* 2332:     */         }
/* 2333: 981 */         if (len < packetLength) {
/* 2334: 984 */           return newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_UNDERFLOW, status, 0, 0);
/* 2335:     */         }
/* 2336:     */       }
/* 2337:     */       else
/* 2338:     */       {
/* 2339: 986 */         if ((len == 0L) && (sslPending <= 0)) {
/* 2340: 987 */           return newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_UNDERFLOW, status, 0, 0);
/* 2341:     */         }
/* 2342: 988 */         if (capacity == 0L) {
/* 2343: 989 */           return newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_OVERFLOW, status, 0, 0);
/* 2344:     */         }
/* 2345: 991 */         packetLength = (int)Math.min(2147483647L, len);
/* 2346:     */       }
/* 2347: 995 */       assert (srcsOffset < srcsEndOffset);
/* 2348:     */       
/* 2349:     */ 
/* 2350: 998 */       assert (capacity > 0L);
/* 2351:     */       
/* 2352:     */ 
/* 2353:1001 */       int bytesProduced = 0;
/* 2354:1002 */       int bytesConsumed = 0;
/* 2355:     */       try
/* 2356:     */       {
/* 2357:     */         for (;;)
/* 2358:     */         {
/* 2359:1006 */           ByteBuffer src = srcs[srcsOffset];
/* 2360:1007 */           int remaining = src.remaining();
/* 2361:     */           int pendingEncryptedBytes;
/* 2362:     */           ByteBuf bioWriteCopyBuf;
/* 2363:1010 */           if (remaining == 0)
/* 2364:     */           {
/* 2365:1011 */             if (sslPending <= 0)
/* 2366:     */             {
/* 2367:1014 */               srcsOffset++;
/* 2368:1014 */               if (srcsOffset < srcsEndOffset) {
/* 2369:     */                 continue;
/* 2370:     */               }
/* 2371:1015 */               break;
/* 2372:     */             }
/* 2373:1019 */             ByteBuf bioWriteCopyBuf = null;
/* 2374:1020 */             int pendingEncryptedBytes = SSL.bioLengthByteBuffer(this.networkBIO);
/* 2375:     */           }
/* 2376:     */           else
/* 2377:     */           {
/* 2378:1025 */             pendingEncryptedBytes = Math.min(packetLength, remaining);
/* 2379:1026 */             bioWriteCopyBuf = writeEncryptedData(src, pendingEncryptedBytes);
/* 2380:     */           }
/* 2381:     */           try
/* 2382:     */           {
/* 2383:     */             for (;;)
/* 2384:     */             {
/* 2385:1030 */               ByteBuffer dst = dsts[dstsOffset];
/* 2386:1031 */               if (!dst.hasRemaining())
/* 2387:     */               {
/* 2388:1033 */                 dstsOffset++;
/* 2389:1033 */                 if (dstsOffset >= dstsEndOffset)
/* 2390:     */                 {
/* 2391:1090 */                   if (bioWriteCopyBuf == null) {
/* 2392:     */                     break label1242;
/* 2393:     */                   }
/* 2394:1091 */                   bioWriteCopyBuf.release();
/* 2395:     */                   break label1242;
/* 2396:     */                 }
/* 2397:     */               }
/* 2398:     */               else
/* 2399:     */               {
/* 2400:1039 */                 int bytesRead = readPlaintextData(dst);
/* 2401:     */                 
/* 2402:     */ 
/* 2403:     */ 
/* 2404:1043 */                 int localBytesConsumed = pendingEncryptedBytes - SSL.bioLengthByteBuffer(this.networkBIO);
/* 2405:1044 */                 bytesConsumed += localBytesConsumed;
/* 2406:1045 */                 packetLength -= localBytesConsumed;
/* 2407:1046 */                 pendingEncryptedBytes -= localBytesConsumed;
/* 2408:1047 */                 src.position(src.position() + localBytesConsumed);
/* 2409:1049 */                 if (bytesRead > 0)
/* 2410:     */                 {
/* 2411:1050 */                   bytesProduced += bytesRead;
/* 2412:1052 */                   if (!dst.hasRemaining())
/* 2413:     */                   {
/* 2414:1053 */                     sslPending = sslPending0();
/* 2415:     */                     
/* 2416:1055 */                     dstsOffset++;
/* 2417:1055 */                     if (dstsOffset >= dstsEndOffset)
/* 2418:     */                     {
/* 2419:1058 */                       SSLEngineResult localSSLEngineResult1 = sslPending > 0 ? newResult(SSLEngineResult.Status.BUFFER_OVERFLOW, status, bytesConsumed, bytesProduced) : newResultMayFinishHandshake(isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, status, bytesConsumed, bytesProduced);
/* 2420:1090 */                       if (bioWriteCopyBuf != null) {
/* 2421:1091 */                         bioWriteCopyBuf.release();
/* 2422:     */                       }
/* 2423:1096 */                       SSL.bioClearByteBuffer(this.networkBIO);
/* 2424:1097 */                       rejectRemoteInitiatedRenegotiation();return localSSLEngineResult1;
/* 2425:     */                     }
/* 2426:     */                   }
/* 2427:1061 */                   else if ((packetLength == 0) || (this.jdkCompatibilityMode))
/* 2428:     */                   {
/* 2429:1090 */                     if (bioWriteCopyBuf == null) {
/* 2430:     */                       break label1242;
/* 2431:     */                     }
/* 2432:1091 */                     bioWriteCopyBuf.release();
/* 2433:     */                     break label1242;
/* 2434:     */                   }
/* 2435:     */                 }
/* 2436:     */                 else
/* 2437:     */                 {
/* 2438:1067 */                   int sslError = SSL.getError(this.ssl, bytesRead);
/* 2439:1068 */                   if ((sslError == SSL.SSL_ERROR_WANT_READ) || (sslError == SSL.SSL_ERROR_WANT_WRITE)) {
/* 2440:     */                     break;
/* 2441:     */                   }
/* 2442:1072 */                   if (sslError == SSL.SSL_ERROR_ZERO_RETURN)
/* 2443:     */                   {
/* 2444:1074 */                     if (!this.receivedShutdown) {
/* 2445:1075 */                       closeAll();
/* 2446:     */                     }
/* 2447:1077 */                     localSSLEngineResult2 = newResultMayFinishHandshake(isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, status, bytesConsumed, bytesProduced);
/* 2448:1090 */                     if (bioWriteCopyBuf != null) {
/* 2449:1091 */                       bioWriteCopyBuf.release();
/* 2450:     */                     }
/* 2451:1096 */                     SSL.bioClearByteBuffer(this.networkBIO);
/* 2452:1097 */                     rejectRemoteInitiatedRenegotiation();return localSSLEngineResult2;
/* 2453:     */                   }
/* 2454:1080 */                   SSLEngineResult localSSLEngineResult2 = sslReadErrorResult(SSL.getLastErrorNumber(), bytesConsumed, bytesProduced);
/* 2455:1090 */                   if (bioWriteCopyBuf != null) {
/* 2456:1091 */                     bioWriteCopyBuf.release();
/* 2457:     */                   }
/* 2458:1096 */                   SSL.bioClearByteBuffer(this.networkBIO);
/* 2459:1097 */                   rejectRemoteInitiatedRenegotiation();return localSSLEngineResult2;
/* 2460:     */                 }
/* 2461:     */               }
/* 2462:     */             }
/* 2463:1086 */             srcsOffset++;
/* 2464:1086 */             if (srcsOffset >= srcsEndOffset)
/* 2465:     */             {
/* 2466:1090 */               if (bioWriteCopyBuf == null) {
/* 2467:     */                 break;
/* 2468:     */               }
/* 2469:1091 */               bioWriteCopyBuf.release(); break;
/* 2470:     */             }
/* 2471:     */           }
/* 2472:     */           finally
/* 2473:     */           {
/* 2474:1090 */             if (bioWriteCopyBuf != null) {
/* 2475:1091 */               bioWriteCopyBuf.release();
/* 2476:     */             }
/* 2477:     */           }
/* 2478:     */         }
/* 2479:     */       }
/* 2480:     */       finally
/* 2481:     */       {
/* 2482:     */         label1242:
/* 2483:1096 */         SSL.bioClearByteBuffer(this.networkBIO);
/* 2484:1097 */         rejectRemoteInitiatedRenegotiation();
/* 2485:     */       }
/* 2486:1101 */       if ((!this.receivedShutdown) && ((SSL.getShutdown(this.ssl) & SSL.SSL_RECEIVED_SHUTDOWN) == SSL.SSL_RECEIVED_SHUTDOWN)) {
/* 2487:1102 */         closeAll();
/* 2488:     */       }
/* 2489:1105 */       return newResultMayFinishHandshake(isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, status, bytesConsumed, bytesProduced);
/* 2490:     */     }
/* 2491:     */   }
/* 2492:     */   
/* 2493:     */   private SSLEngineResult sslReadErrorResult(int err, int bytesConsumed, int bytesProduced)
/* 2494:     */     throws SSLException
/* 2495:     */   {
/* 2496:1110 */     String errStr = SSL.getErrorString(err);
/* 2497:1116 */     if (SSL.bioLengthNonApplication(this.networkBIO) > 0)
/* 2498:     */     {
/* 2499:1117 */       if ((this.handshakeException == null) && (this.handshakeState != HandshakeState.FINISHED)) {
/* 2500:1120 */         this.handshakeException = new SSLHandshakeException(errStr);
/* 2501:     */       }
/* 2502:1122 */       return new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, bytesConsumed, bytesProduced);
/* 2503:     */     }
/* 2504:1124 */     throw shutdownWithError("SSL_read", errStr);
/* 2505:     */   }
/* 2506:     */   
/* 2507:     */   private void closeAll()
/* 2508:     */     throws SSLException
/* 2509:     */   {
/* 2510:1128 */     this.receivedShutdown = true;
/* 2511:1129 */     closeOutbound();
/* 2512:1130 */     closeInbound();
/* 2513:     */   }
/* 2514:     */   
/* 2515:     */   private void rejectRemoteInitiatedRenegotiation()
/* 2516:     */     throws SSLHandshakeException
/* 2517:     */   {
/* 2518:1137 */     if ((this.rejectRemoteInitiatedRenegotiation) && (!isDestroyed()) && (SSL.getHandshakeCount(this.ssl) > 1))
/* 2519:     */     {
/* 2520:1140 */       shutdown();
/* 2521:1141 */       throw new SSLHandshakeException("remote-initiated renegotiation not allowed");
/* 2522:     */     }
/* 2523:     */   }
/* 2524:     */   
/* 2525:     */   public final SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dsts)
/* 2526:     */     throws SSLException
/* 2527:     */   {
/* 2528:1146 */     return unwrap(srcs, 0, srcs.length, dsts, 0, dsts.length);
/* 2529:     */   }
/* 2530:     */   
/* 2531:     */   private ByteBuffer[] singleSrcBuffer(ByteBuffer src)
/* 2532:     */   {
/* 2533:1150 */     this.singleSrcBuffer[0] = src;
/* 2534:1151 */     return this.singleSrcBuffer;
/* 2535:     */   }
/* 2536:     */   
/* 2537:     */   private void resetSingleSrcBuffer()
/* 2538:     */   {
/* 2539:1155 */     this.singleSrcBuffer[0] = null;
/* 2540:     */   }
/* 2541:     */   
/* 2542:     */   private ByteBuffer[] singleDstBuffer(ByteBuffer src)
/* 2543:     */   {
/* 2544:1159 */     this.singleDstBuffer[0] = src;
/* 2545:1160 */     return this.singleDstBuffer;
/* 2546:     */   }
/* 2547:     */   
/* 2548:     */   private void resetSingleDstBuffer()
/* 2549:     */   {
/* 2550:1164 */     this.singleDstBuffer[0] = null;
/* 2551:     */   }
/* 2552:     */   
/* 2553:     */   public final synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length)
/* 2554:     */     throws SSLException
/* 2555:     */   {
/* 2556:     */     try
/* 2557:     */     {
/* 2558:1171 */       return unwrap(singleSrcBuffer(src), 0, 1, dsts, offset, length);
/* 2559:     */     }
/* 2560:     */     finally
/* 2561:     */     {
/* 2562:1173 */       resetSingleSrcBuffer();
/* 2563:     */     }
/* 2564:     */   }
/* 2565:     */   
/* 2566:     */   public final synchronized SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst)
/* 2567:     */     throws SSLException
/* 2568:     */   {
/* 2569:     */     try
/* 2570:     */     {
/* 2571:1180 */       return wrap(singleSrcBuffer(src), dst);
/* 2572:     */     }
/* 2573:     */     finally
/* 2574:     */     {
/* 2575:1182 */       resetSingleSrcBuffer();
/* 2576:     */     }
/* 2577:     */   }
/* 2578:     */   
/* 2579:     */   public final synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst)
/* 2580:     */     throws SSLException
/* 2581:     */   {
/* 2582:     */     try
/* 2583:     */     {
/* 2584:1189 */       return unwrap(singleSrcBuffer(src), singleDstBuffer(dst));
/* 2585:     */     }
/* 2586:     */     finally
/* 2587:     */     {
/* 2588:1191 */       resetSingleSrcBuffer();
/* 2589:1192 */       resetSingleDstBuffer();
/* 2590:     */     }
/* 2591:     */   }
/* 2592:     */   
/* 2593:     */   public final synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts)
/* 2594:     */     throws SSLException
/* 2595:     */   {
/* 2596:     */     try
/* 2597:     */     {
/* 2598:1199 */       return unwrap(singleSrcBuffer(src), dsts);
/* 2599:     */     }
/* 2600:     */     finally
/* 2601:     */     {
/* 2602:1201 */       resetSingleSrcBuffer();
/* 2603:     */     }
/* 2604:     */   }
/* 2605:     */   
/* 2606:     */   public final Runnable getDelegatedTask()
/* 2607:     */   {
/* 2608:1209 */     return null;
/* 2609:     */   }
/* 2610:     */   
/* 2611:     */   public final synchronized void closeInbound()
/* 2612:     */     throws SSLException
/* 2613:     */   {
/* 2614:1214 */     if (this.isInboundDone) {
/* 2615:1215 */       return;
/* 2616:     */     }
/* 2617:1218 */     this.isInboundDone = true;
/* 2618:1220 */     if (isOutboundDone()) {
/* 2619:1223 */       shutdown();
/* 2620:     */     }
/* 2621:1226 */     if ((this.handshakeState != HandshakeState.NOT_STARTED) && (!this.receivedShutdown)) {
/* 2622:1227 */       throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
/* 2623:     */     }
/* 2624:     */   }
/* 2625:     */   
/* 2626:     */   public final synchronized boolean isInboundDone()
/* 2627:     */   {
/* 2628:1234 */     return this.isInboundDone;
/* 2629:     */   }
/* 2630:     */   
/* 2631:     */   public final synchronized void closeOutbound()
/* 2632:     */   {
/* 2633:1239 */     if (this.outboundClosed) {
/* 2634:1240 */       return;
/* 2635:     */     }
/* 2636:1243 */     this.outboundClosed = true;
/* 2637:1245 */     if ((this.handshakeState != HandshakeState.NOT_STARTED) && (!isDestroyed()))
/* 2638:     */     {
/* 2639:1246 */       int mode = SSL.getShutdown(this.ssl);
/* 2640:1247 */       if ((mode & SSL.SSL_SENT_SHUTDOWN) != SSL.SSL_SENT_SHUTDOWN) {
/* 2641:1248 */         doSSLShutdown();
/* 2642:     */       }
/* 2643:     */     }
/* 2644:     */     else
/* 2645:     */     {
/* 2646:1252 */       shutdown();
/* 2647:     */     }
/* 2648:     */   }
/* 2649:     */   
/* 2650:     */   private boolean doSSLShutdown()
/* 2651:     */   {
/* 2652:1261 */     if (SSL.isInInit(this.ssl) != 0) {
/* 2653:1266 */       return false;
/* 2654:     */     }
/* 2655:1268 */     int err = SSL.shutdownSSL(this.ssl);
/* 2656:1269 */     if (err < 0)
/* 2657:     */     {
/* 2658:1270 */       int sslErr = SSL.getError(this.ssl, err);
/* 2659:1271 */       if ((sslErr == SSL.SSL_ERROR_SYSCALL) || (sslErr == SSL.SSL_ERROR_SSL))
/* 2660:     */       {
/* 2661:1272 */         if (logger.isDebugEnabled()) {
/* 2662:1273 */           logger.debug("SSL_shutdown failed: OpenSSL error: {}", SSL.getLastError());
/* 2663:     */         }
/* 2664:1276 */         shutdown();
/* 2665:1277 */         return false;
/* 2666:     */       }
/* 2667:1279 */       SSL.clearError();
/* 2668:     */     }
/* 2669:1281 */     return true;
/* 2670:     */   }
/* 2671:     */   
/* 2672:     */   public final synchronized boolean isOutboundDone()
/* 2673:     */   {
/* 2674:1288 */     return (this.outboundClosed) && ((this.networkBIO == 0L) || (SSL.bioLengthNonApplication(this.networkBIO) == 0));
/* 2675:     */   }
/* 2676:     */   
/* 2677:     */   public final String[] getSupportedCipherSuites()
/* 2678:     */   {
/* 2679:1293 */     return (String[])OpenSsl.AVAILABLE_CIPHER_SUITES.toArray(new String[OpenSsl.AVAILABLE_CIPHER_SUITES.size()]);
/* 2680:     */   }
/* 2681:     */   
/* 2682:     */   public final String[] getEnabledCipherSuites()
/* 2683:     */   {
/* 2684:     */     String[] enabled;
/* 2685:1299 */     synchronized (this)
/* 2686:     */     {
/* 2687:     */       String[] enabled;
/* 2688:1300 */       if (!isDestroyed()) {
/* 2689:1301 */         enabled = SSL.getCiphers(this.ssl);
/* 2690:     */       } else {
/* 2691:1303 */         return EmptyArrays.EMPTY_STRINGS;
/* 2692:     */       }
/* 2693:     */     }
/* 2694:     */     String[] enabled;
/* 2695:1306 */     if (enabled == null) {
/* 2696:1307 */       return EmptyArrays.EMPTY_STRINGS;
/* 2697:     */     }
/* 2698:1309 */     synchronized (this)
/* 2699:     */     {
/* 2700:1310 */       for (int i = 0; i < enabled.length; i++)
/* 2701:     */       {
/* 2702:1311 */         String mapped = toJavaCipherSuite(enabled[i]);
/* 2703:1312 */         if (mapped != null) {
/* 2704:1313 */           enabled[i] = mapped;
/* 2705:     */         }
/* 2706:     */       }
/* 2707:     */     }
/* 2708:1317 */     return enabled;
/* 2709:     */   }
/* 2710:     */   
/* 2711:     */   public final void setEnabledCipherSuites(String[] cipherSuites)
/* 2712:     */   {
/* 2713:1323 */     ObjectUtil.checkNotNull(cipherSuites, "cipherSuites");
/* 2714:     */     
/* 2715:1325 */     StringBuilder buf = new StringBuilder();
/* 2716:1326 */     for (String c : cipherSuites)
/* 2717:     */     {
/* 2718:1327 */       if (c == null) {
/* 2719:     */         break;
/* 2720:     */       }
/* 2721:1331 */       String converted = CipherSuiteConverter.toOpenSsl(c);
/* 2722:1332 */       if (converted == null) {
/* 2723:1333 */         converted = c;
/* 2724:     */       }
/* 2725:1336 */       if (!OpenSsl.isCipherSuiteAvailable(converted)) {
/* 2726:1337 */         throw new IllegalArgumentException("unsupported cipher suite: " + c + '(' + converted + ')');
/* 2727:     */       }
/* 2728:1340 */       buf.append(converted);
/* 2729:1341 */       buf.append(':');
/* 2730:     */     }
/* 2731:1344 */     if (buf.length() == 0) {
/* 2732:1345 */       throw new IllegalArgumentException("empty cipher suites");
/* 2733:     */     }
/* 2734:1347 */     buf.setLength(buf.length() - 1);
/* 2735:     */     
/* 2736:1349 */     String cipherSuiteSpec = buf.toString();
/* 2737:1351 */     synchronized (this)
/* 2738:     */     {
/* 2739:1352 */       if (!isDestroyed()) {
/* 2740:     */         try
/* 2741:     */         {
/* 2742:1354 */           SSL.setCipherSuites(this.ssl, cipherSuiteSpec);
/* 2743:     */         }
/* 2744:     */         catch (Exception e)
/* 2745:     */         {
/* 2746:1356 */           throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec, e);
/* 2747:     */         }
/* 2748:     */       } else {
/* 2749:1359 */         throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec);
/* 2750:     */       }
/* 2751:     */     }
/* 2752:     */   }
/* 2753:     */   
/* 2754:     */   public final String[] getSupportedProtocols()
/* 2755:     */   {
/* 2756:1366 */     return (String[])OpenSsl.SUPPORTED_PROTOCOLS_SET.toArray(new String[OpenSsl.SUPPORTED_PROTOCOLS_SET.size()]);
/* 2757:     */   }
/* 2758:     */   
/* 2759:     */   public final String[] getEnabledProtocols()
/* 2760:     */   {
/* 2761:1371 */     List<String> enabled = new ArrayList(6);
/* 2762:     */     
/* 2763:1373 */     enabled.add("SSLv2Hello");
/* 2764:     */     int opts;
/* 2765:1376 */     synchronized (this)
/* 2766:     */     {
/* 2767:     */       int opts;
/* 2768:1377 */       if (!isDestroyed()) {
/* 2769:1378 */         opts = SSL.getOptions(this.ssl);
/* 2770:     */       } else {
/* 2771:1380 */         return (String[])enabled.toArray(new String[1]);
/* 2772:     */       }
/* 2773:     */     }
/* 2774:     */     int opts;
/* 2775:1383 */     if (isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1, "TLSv1")) {
/* 2776:1384 */       enabled.add("TLSv1");
/* 2777:     */     }
/* 2778:1386 */     if (isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1_1, "TLSv1.1")) {
/* 2779:1387 */       enabled.add("TLSv1.1");
/* 2780:     */     }
/* 2781:1389 */     if (isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1_2, "TLSv1.2")) {
/* 2782:1390 */       enabled.add("TLSv1.2");
/* 2783:     */     }
/* 2784:1392 */     if (isProtocolEnabled(opts, SSL.SSL_OP_NO_SSLv2, "SSLv2")) {
/* 2785:1393 */       enabled.add("SSLv2");
/* 2786:     */     }
/* 2787:1395 */     if (isProtocolEnabled(opts, SSL.SSL_OP_NO_SSLv3, "SSLv3")) {
/* 2788:1396 */       enabled.add("SSLv3");
/* 2789:     */     }
/* 2790:1398 */     return (String[])enabled.toArray(new String[enabled.size()]);
/* 2791:     */   }
/* 2792:     */   
/* 2793:     */   private static boolean isProtocolEnabled(int opts, int disableMask, String protocolString)
/* 2794:     */   {
/* 2795:1404 */     return ((opts & disableMask) == 0) && (OpenSsl.SUPPORTED_PROTOCOLS_SET.contains(protocolString));
/* 2796:     */   }
/* 2797:     */   
/* 2798:     */   public final void setEnabledProtocols(String[] protocols)
/* 2799:     */   {
/* 2800:1418 */     if (protocols == null) {
/* 2801:1420 */       throw new IllegalArgumentException();
/* 2802:     */     }
/* 2803:1422 */     int minProtocolIndex = OPENSSL_OP_NO_PROTOCOLS.length;
/* 2804:1423 */     int maxProtocolIndex = 0;
/* 2805:1424 */     for (String p : protocols)
/* 2806:     */     {
/* 2807:1425 */       if (!OpenSsl.SUPPORTED_PROTOCOLS_SET.contains(p)) {
/* 2808:1426 */         throw new IllegalArgumentException("Protocol " + p + " is not supported.");
/* 2809:     */       }
/* 2810:1428 */       if (p.equals("SSLv2"))
/* 2811:     */       {
/* 2812:1429 */         if (minProtocolIndex > 0) {
/* 2813:1430 */           minProtocolIndex = 0;
/* 2814:     */         }
/* 2815:1432 */         if (maxProtocolIndex < 0) {
/* 2816:1433 */           maxProtocolIndex = 0;
/* 2817:     */         }
/* 2818:     */       }
/* 2819:1435 */       else if (p.equals("SSLv3"))
/* 2820:     */       {
/* 2821:1436 */         if (minProtocolIndex > 1) {
/* 2822:1437 */           minProtocolIndex = 1;
/* 2823:     */         }
/* 2824:1439 */         if (maxProtocolIndex < 1) {
/* 2825:1440 */           maxProtocolIndex = 1;
/* 2826:     */         }
/* 2827:     */       }
/* 2828:1442 */       else if (p.equals("TLSv1"))
/* 2829:     */       {
/* 2830:1443 */         if (minProtocolIndex > 2) {
/* 2831:1444 */           minProtocolIndex = 2;
/* 2832:     */         }
/* 2833:1446 */         if (maxProtocolIndex < 2) {
/* 2834:1447 */           maxProtocolIndex = 2;
/* 2835:     */         }
/* 2836:     */       }
/* 2837:1449 */       else if (p.equals("TLSv1.1"))
/* 2838:     */       {
/* 2839:1450 */         if (minProtocolIndex > 3) {
/* 2840:1451 */           minProtocolIndex = 3;
/* 2841:     */         }
/* 2842:1453 */         if (maxProtocolIndex < 3) {
/* 2843:1454 */           maxProtocolIndex = 3;
/* 2844:     */         }
/* 2845:     */       }
/* 2846:1456 */       else if (p.equals("TLSv1.2"))
/* 2847:     */       {
/* 2848:1457 */         if (minProtocolIndex > 4) {
/* 2849:1458 */           minProtocolIndex = 4;
/* 2850:     */         }
/* 2851:1460 */         if (maxProtocolIndex < 4) {
/* 2852:1461 */           maxProtocolIndex = 4;
/* 2853:     */         }
/* 2854:     */       }
/* 2855:     */     }
/* 2856:1465 */     synchronized (this)
/* 2857:     */     {
/* 2858:1466 */       if (!isDestroyed())
/* 2859:     */       {
/* 2860:1468 */         SSL.clearOptions(this.ssl, SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_NO_TLSv1 | SSL.SSL_OP_NO_TLSv1_1 | SSL.SSL_OP_NO_TLSv1_2);
/* 2861:     */         
/* 2862:     */ 
/* 2863:1471 */         int opts = 0;
/* 2864:1472 */         for (int i = 0; i < minProtocolIndex; i++) {
/* 2865:1473 */           opts |= OPENSSL_OP_NO_PROTOCOLS[i];
/* 2866:     */         }
/* 2867:1475 */         assert (maxProtocolIndex != 2147483647);
/* 2868:1476 */         for (int i = maxProtocolIndex + 1; i < OPENSSL_OP_NO_PROTOCOLS.length; i++) {
/* 2869:1477 */           opts |= OPENSSL_OP_NO_PROTOCOLS[i];
/* 2870:     */         }
/* 2871:1481 */         SSL.setOptions(this.ssl, opts);
/* 2872:     */       }
/* 2873:     */       else
/* 2874:     */       {
/* 2875:1483 */         throw new IllegalStateException("failed to enable protocols: " + Arrays.asList(protocols));
/* 2876:     */       }
/* 2877:     */     }
/* 2878:     */   }
/* 2879:     */   
/* 2880:     */   public final SSLSession getSession()
/* 2881:     */   {
/* 2882:1490 */     return this.session;
/* 2883:     */   }
/* 2884:     */   
/* 2885:     */   public final synchronized void beginHandshake()
/* 2886:     */     throws SSLException
/* 2887:     */   {
/* 2888:1495 */     switch (2.$SwitchMap$io$netty$handler$ssl$ReferenceCountedOpenSslEngine$HandshakeState[this.handshakeState.ordinal()])
/* 2889:     */     {
/* 2890:     */     case 3: 
/* 2891:1497 */       checkEngineClosed(BEGIN_HANDSHAKE_ENGINE_CLOSED);
/* 2892:     */       
/* 2893:     */ 
/* 2894:     */ 
/* 2895:     */ 
/* 2896:     */ 
/* 2897:     */ 
/* 2898:     */ 
/* 2899:1505 */       this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
/* 2900:1506 */       calculateMaxWrapOverhead();
/* 2901:     */       
/* 2902:1508 */       break;
/* 2903:     */     case 4: 
/* 2904:     */       break;
/* 2905:     */     case 2: 
/* 2906:1513 */       if (this.clientMode) {
/* 2907:1515 */         throw RENEGOTIATION_UNSUPPORTED;
/* 2908:     */       }
/* 2909:     */       int status;
/* 2910:1531 */       if (((status = SSL.renegotiate(this.ssl)) != 1) || ((status = SSL.doHandshake(this.ssl)) != 1))
/* 2911:     */       {
/* 2912:1532 */         int err = SSL.getError(this.ssl, status);
/* 2913:1533 */         if ((err == SSL.SSL_ERROR_WANT_READ) || (err == SSL.SSL_ERROR_WANT_WRITE))
/* 2914:     */         {
/* 2915:1536 */           this.renegotiationPending = true;
/* 2916:1537 */           this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
/* 2917:1538 */           this.lastAccessed = System.currentTimeMillis();
/* 2918:1539 */           return;
/* 2919:     */         }
/* 2920:1541 */         throw shutdownWithError("renegotiation failed");
/* 2921:     */       }
/* 2922:1545 */       SSL.setState(this.ssl, SSL.SSL_ST_ACCEPT);
/* 2923:     */       
/* 2924:1547 */       this.lastAccessed = System.currentTimeMillis();
/* 2925:     */     case 1: 
/* 2926:1551 */       this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
/* 2927:1552 */       handshake();
/* 2928:1553 */       calculateMaxWrapOverhead();
/* 2929:1554 */       break;
/* 2930:     */     default: 
/* 2931:1556 */       throw new Error();
/* 2932:     */     }
/* 2933:     */   }
/* 2934:     */   
/* 2935:     */   private void checkEngineClosed(SSLException cause)
/* 2936:     */     throws SSLException
/* 2937:     */   {
/* 2938:1561 */     if (isDestroyed()) {
/* 2939:1562 */       throw cause;
/* 2940:     */     }
/* 2941:     */   }
/* 2942:     */   
/* 2943:     */   private static SSLEngineResult.HandshakeStatus pendingStatus(int pendingStatus)
/* 2944:     */   {
/* 2945:1568 */     return pendingStatus > 0 ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
/* 2946:     */   }
/* 2947:     */   
/* 2948:     */   private static boolean isEmpty(Object[] arr)
/* 2949:     */   {
/* 2950:1572 */     return (arr == null) || (arr.length == 0);
/* 2951:     */   }
/* 2952:     */   
/* 2953:     */   private static boolean isEmpty(byte[] cert)
/* 2954:     */   {
/* 2955:1576 */     return (cert == null) || (cert.length == 0);
/* 2956:     */   }
/* 2957:     */   
/* 2958:     */   private SSLEngineResult.HandshakeStatus handshake()
/* 2959:     */     throws SSLException
/* 2960:     */   {
/* 2961:1580 */     if (this.handshakeState == HandshakeState.FINISHED) {
/* 2962:1581 */       return SSLEngineResult.HandshakeStatus.FINISHED;
/* 2963:     */     }
/* 2964:1583 */     checkEngineClosed(HANDSHAKE_ENGINE_CLOSED);
/* 2965:     */     
/* 2966:     */ 
/* 2967:     */ 
/* 2968:     */ 
/* 2969:     */ 
/* 2970:1589 */     SSLHandshakeException exception = this.handshakeException;
/* 2971:1590 */     if (exception != null)
/* 2972:     */     {
/* 2973:1591 */       if (SSL.bioLengthNonApplication(this.networkBIO) > 0) {
/* 2974:1593 */         return SSLEngineResult.HandshakeStatus.NEED_WRAP;
/* 2975:     */       }
/* 2976:1597 */       this.handshakeException = null;
/* 2977:1598 */       shutdown();
/* 2978:1599 */       throw exception;
/* 2979:     */     }
/* 2980:1603 */     this.engineMap.add(this);
/* 2981:1604 */     if (this.lastAccessed == -1L) {
/* 2982:1605 */       this.lastAccessed = System.currentTimeMillis();
/* 2983:     */     }
/* 2984:1608 */     if ((!this.certificateSet) && (this.keyMaterialManager != null))
/* 2985:     */     {
/* 2986:1609 */       this.certificateSet = true;
/* 2987:1610 */       this.keyMaterialManager.setKeyMaterial(this);
/* 2988:     */     }
/* 2989:1613 */     int code = SSL.doHandshake(this.ssl);
/* 2990:1614 */     if (code <= 0)
/* 2991:     */     {
/* 2992:1617 */       if (this.handshakeException != null)
/* 2993:     */       {
/* 2994:1618 */         exception = this.handshakeException;
/* 2995:1619 */         this.handshakeException = null;
/* 2996:1620 */         shutdown();
/* 2997:1621 */         throw exception;
/* 2998:     */       }
/* 2999:1624 */       int sslError = SSL.getError(this.ssl, code);
/* 3000:1625 */       if ((sslError == SSL.SSL_ERROR_WANT_READ) || (sslError == SSL.SSL_ERROR_WANT_WRITE)) {
/* 3001:1626 */         return pendingStatus(SSL.bioLengthNonApplication(this.networkBIO));
/* 3002:     */       }
/* 3003:1629 */       throw shutdownWithError("SSL_do_handshake");
/* 3004:     */     }
/* 3005:1633 */     this.session.handshakeFinished();
/* 3006:1634 */     this.engineMap.remove(this.ssl);
/* 3007:1635 */     return SSLEngineResult.HandshakeStatus.FINISHED;
/* 3008:     */   }
/* 3009:     */   
/* 3010:     */   private SSLEngineResult.HandshakeStatus mayFinishHandshake(SSLEngineResult.HandshakeStatus status)
/* 3011:     */     throws SSLException
/* 3012:     */   {
/* 3013:1640 */     if ((status == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) && (this.handshakeState != HandshakeState.FINISHED)) {
/* 3014:1643 */       return handshake();
/* 3015:     */     }
/* 3016:1645 */     return status;
/* 3017:     */   }
/* 3018:     */   
/* 3019:     */   public final synchronized SSLEngineResult.HandshakeStatus getHandshakeStatus()
/* 3020:     */   {
/* 3021:1651 */     return needPendingStatus() ? pendingStatus(SSL.bioLengthNonApplication(this.networkBIO)) : SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 3022:     */   }
/* 3023:     */   
/* 3024:     */   private SSLEngineResult.HandshakeStatus getHandshakeStatus(int pending)
/* 3025:     */   {
/* 3026:1656 */     return needPendingStatus() ? pendingStatus(pending) : SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
/* 3027:     */   }
/* 3028:     */   
/* 3029:     */   private boolean needPendingStatus()
/* 3030:     */   {
/* 3031:1660 */     return (this.handshakeState != HandshakeState.NOT_STARTED) && (!isDestroyed()) && ((this.handshakeState != HandshakeState.FINISHED) || 
/* 3032:1661 */       (isInboundDone()) || (isOutboundDone()));
/* 3033:     */   }
/* 3034:     */   
/* 3035:     */   private String toJavaCipherSuite(String openSslCipherSuite)
/* 3036:     */   {
/* 3037:1668 */     if (openSslCipherSuite == null) {
/* 3038:1669 */       return null;
/* 3039:     */     }
/* 3040:1672 */     String prefix = toJavaCipherSuitePrefix(SSL.getVersion(this.ssl));
/* 3041:1673 */     return CipherSuiteConverter.toJava(openSslCipherSuite, prefix);
/* 3042:     */   }
/* 3043:     */   
/* 3044:     */   private static String toJavaCipherSuitePrefix(String protocolVersion)
/* 3045:     */   {
/* 3046:     */     char c;
/* 3047:     */     char c;
/* 3048:1681 */     if ((protocolVersion == null) || (protocolVersion.isEmpty())) {
/* 3049:1682 */       c = '\000';
/* 3050:     */     } else {
/* 3051:1684 */       c = protocolVersion.charAt(0);
/* 3052:     */     }
/* 3053:1687 */     switch (c)
/* 3054:     */     {
/* 3055:     */     case 'T': 
/* 3056:1689 */       return "TLS";
/* 3057:     */     case 'S': 
/* 3058:1691 */       return "SSL";
/* 3059:     */     }
/* 3060:1693 */     return "UNKNOWN";
/* 3061:     */   }
/* 3062:     */   
/* 3063:     */   public final void setUseClientMode(boolean clientMode)
/* 3064:     */   {
/* 3065:1699 */     if (clientMode != this.clientMode) {
/* 3066:1700 */       throw new UnsupportedOperationException();
/* 3067:     */     }
/* 3068:     */   }
/* 3069:     */   
/* 3070:     */   public final boolean getUseClientMode()
/* 3071:     */   {
/* 3072:1706 */     return this.clientMode;
/* 3073:     */   }
/* 3074:     */   
/* 3075:     */   public final void setNeedClientAuth(boolean b)
/* 3076:     */   {
/* 3077:1711 */     setClientAuth(b ? ClientAuth.REQUIRE : ClientAuth.NONE);
/* 3078:     */   }
/* 3079:     */   
/* 3080:     */   public final boolean getNeedClientAuth()
/* 3081:     */   {
/* 3082:1716 */     return this.clientAuth == ClientAuth.REQUIRE;
/* 3083:     */   }
/* 3084:     */   
/* 3085:     */   public final void setWantClientAuth(boolean b)
/* 3086:     */   {
/* 3087:1721 */     setClientAuth(b ? ClientAuth.OPTIONAL : ClientAuth.NONE);
/* 3088:     */   }
/* 3089:     */   
/* 3090:     */   public final boolean getWantClientAuth()
/* 3091:     */   {
/* 3092:1726 */     return this.clientAuth == ClientAuth.OPTIONAL;
/* 3093:     */   }
/* 3094:     */   
/* 3095:     */   public final synchronized void setVerify(int verifyMode, int depth)
/* 3096:     */   {
/* 3097:1735 */     SSL.setVerify(this.ssl, verifyMode, depth);
/* 3098:     */   }
/* 3099:     */   
/* 3100:     */   private void setClientAuth(ClientAuth mode)
/* 3101:     */   {
/* 3102:1739 */     if (this.clientMode) {
/* 3103:1740 */       return;
/* 3104:     */     }
/* 3105:1742 */     synchronized (this)
/* 3106:     */     {
/* 3107:1743 */       if (this.clientAuth == mode) {
/* 3108:1745 */         return;
/* 3109:     */       }
/* 3110:1747 */       switch (2.$SwitchMap$io$netty$handler$ssl$ClientAuth[mode.ordinal()])
/* 3111:     */       {
/* 3112:     */       case 1: 
/* 3113:1749 */         SSL.setVerify(this.ssl, 0, 10);
/* 3114:1750 */         break;
/* 3115:     */       case 2: 
/* 3116:1752 */         SSL.setVerify(this.ssl, 2, 10);
/* 3117:1753 */         break;
/* 3118:     */       case 3: 
/* 3119:1755 */         SSL.setVerify(this.ssl, 1, 10);
/* 3120:1756 */         break;
/* 3121:     */       default: 
/* 3122:1758 */         throw new Error(mode.toString());
/* 3123:     */       }
/* 3124:1760 */       this.clientAuth = mode;
/* 3125:     */     }
/* 3126:     */   }
/* 3127:     */   
/* 3128:     */   public final void setEnableSessionCreation(boolean b)
/* 3129:     */   {
/* 3130:1766 */     if (b) {
/* 3131:1767 */       throw new UnsupportedOperationException();
/* 3132:     */     }
/* 3133:     */   }
/* 3134:     */   
/* 3135:     */   public final boolean getEnableSessionCreation()
/* 3136:     */   {
/* 3137:1773 */     return false;
/* 3138:     */   }
/* 3139:     */   
/* 3140:     */   public final synchronized SSLParameters getSSLParameters()
/* 3141:     */   {
/* 3142:1778 */     SSLParameters sslParameters = super.getSSLParameters();
/* 3143:     */     
/* 3144:1780 */     int version = PlatformDependent.javaVersion();
/* 3145:1781 */     if (version >= 7)
/* 3146:     */     {
/* 3147:1782 */       sslParameters.setEndpointIdentificationAlgorithm(this.endPointIdentificationAlgorithm);
/* 3148:1783 */       Java7SslParametersUtils.setAlgorithmConstraints(sslParameters, this.algorithmConstraints);
/* 3149:1784 */       if (version >= 8)
/* 3150:     */       {
/* 3151:1785 */         if (this.sniHostNames != null) {
/* 3152:1786 */           Java8SslUtils.setSniHostNames(sslParameters, this.sniHostNames);
/* 3153:     */         }
/* 3154:1788 */         if (!isDestroyed()) {
/* 3155:1789 */           Java8SslUtils.setUseCipherSuitesOrder(sslParameters, 
/* 3156:1790 */             (SSL.getOptions(this.ssl) & SSL.SSL_OP_CIPHER_SERVER_PREFERENCE) != 0);
/* 3157:     */         }
/* 3158:1793 */         Java8SslUtils.setSNIMatchers(sslParameters, this.matchers);
/* 3159:     */       }
/* 3160:     */     }
/* 3161:1796 */     return sslParameters;
/* 3162:     */   }
/* 3163:     */   
/* 3164:     */   public final synchronized void setSSLParameters(SSLParameters sslParameters)
/* 3165:     */   {
/* 3166:1801 */     int version = PlatformDependent.javaVersion();
/* 3167:1802 */     if (version >= 7)
/* 3168:     */     {
/* 3169:1803 */       if (sslParameters.getAlgorithmConstraints() != null) {
/* 3170:1804 */         throw new IllegalArgumentException("AlgorithmConstraints are not supported.");
/* 3171:     */       }
/* 3172:1807 */       if (version >= 8)
/* 3173:     */       {
/* 3174:1808 */         if (!isDestroyed())
/* 3175:     */         {
/* 3176:1809 */           if (this.clientMode)
/* 3177:     */           {
/* 3178:1810 */             List<String> sniHostNames = Java8SslUtils.getSniHostNames(sslParameters);
/* 3179:1811 */             for (String name : sniHostNames) {
/* 3180:1812 */               SSL.setTlsExtHostName(this.ssl, name);
/* 3181:     */             }
/* 3182:1814 */             this.sniHostNames = sniHostNames;
/* 3183:     */           }
/* 3184:1816 */           if (Java8SslUtils.getUseCipherSuitesOrder(sslParameters)) {
/* 3185:1817 */             SSL.setOptions(this.ssl, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
/* 3186:     */           } else {
/* 3187:1819 */             SSL.clearOptions(this.ssl, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
/* 3188:     */           }
/* 3189:     */         }
/* 3190:1822 */         this.matchers = sslParameters.getSNIMatchers();
/* 3191:     */       }
/* 3192:1825 */       String endPointIdentificationAlgorithm = sslParameters.getEndpointIdentificationAlgorithm();
/* 3193:     */       
/* 3194:1827 */       boolean endPointVerificationEnabled = (endPointIdentificationAlgorithm != null) && (!endPointIdentificationAlgorithm.isEmpty());
/* 3195:1828 */       SSL.setHostNameValidation(this.ssl, 0, endPointVerificationEnabled ? 
/* 3196:1829 */         getPeerHost() : null);
/* 3197:1832 */       if ((this.clientMode) && (endPointVerificationEnabled)) {
/* 3198:1833 */         SSL.setVerify(this.ssl, 2, -1);
/* 3199:     */       }
/* 3200:1836 */       this.endPointIdentificationAlgorithm = endPointIdentificationAlgorithm;
/* 3201:1837 */       this.algorithmConstraints = sslParameters.getAlgorithmConstraints();
/* 3202:     */     }
/* 3203:1839 */     super.setSSLParameters(sslParameters);
/* 3204:     */   }
/* 3205:     */   
/* 3206:     */   private boolean isDestroyed()
/* 3207:     */   {
/* 3208:1843 */     return this.destroyed != 0;
/* 3209:     */   }
/* 3210:     */   
/* 3211:     */   final boolean checkSniHostnameMatch(String hostname)
/* 3212:     */   {
/* 3213:1847 */     return Java8SslUtils.checkSniHostnameMatch(this.matchers, hostname);
/* 3214:     */   }
/* 3215:     */   
/* 3216:     */   public String getNegotiatedApplicationProtocol()
/* 3217:     */   {
/* 3218:1852 */     return this.applicationProtocol;
/* 3219:     */   }
/* 3220:     */   
/* 3221:     */   private final class OpenSslSession
/* 3222:     */     implements SSLSession
/* 3223:     */   {
/* 3224:     */     private final OpenSslSessionContext sessionContext;
/* 3225:     */     private javax.security.cert.X509Certificate[] x509PeerCerts;
/* 3226:     */     private Certificate[] peerCerts;
/* 3227:     */     private String protocol;
/* 3228:     */     private String cipher;
/* 3229:     */     private byte[] id;
/* 3230:     */     private long creationTime;
/* 3231:1866 */     private volatile int applicationBufferSize = ReferenceCountedOpenSslEngine.MAX_PLAINTEXT_LENGTH;
/* 3232:     */     private Map<String, Object> values;
/* 3233:     */     
/* 3234:     */     OpenSslSession(OpenSslSessionContext sessionContext)
/* 3235:     */     {
/* 3236:1872 */       this.sessionContext = sessionContext;
/* 3237:     */     }
/* 3238:     */     
/* 3239:     */     public byte[] getId()
/* 3240:     */     {
/* 3241:1877 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3242:     */       {
/* 3243:1878 */         if (this.id == null) {
/* 3244:1879 */           return EmptyArrays.EMPTY_BYTES;
/* 3245:     */         }
/* 3246:1881 */         return (byte[])this.id.clone();
/* 3247:     */       }
/* 3248:     */     }
/* 3249:     */     
/* 3250:     */     public SSLSessionContext getSessionContext()
/* 3251:     */     {
/* 3252:1887 */       return this.sessionContext;
/* 3253:     */     }
/* 3254:     */     
/* 3255:     */     public long getCreationTime()
/* 3256:     */     {
/* 3257:1892 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3258:     */       {
/* 3259:1893 */         if ((this.creationTime == 0L) && (!ReferenceCountedOpenSslEngine.this.isDestroyed())) {
/* 3260:1894 */           this.creationTime = (SSL.getTime(ReferenceCountedOpenSslEngine.this.ssl) * 1000L);
/* 3261:     */         }
/* 3262:     */       }
/* 3263:1897 */       return this.creationTime;
/* 3264:     */     }
/* 3265:     */     
/* 3266:     */     public long getLastAccessedTime()
/* 3267:     */     {
/* 3268:1902 */       long lastAccessed = ReferenceCountedOpenSslEngine.this.lastAccessed;
/* 3269:     */       
/* 3270:1904 */       return lastAccessed == -1L ? getCreationTime() : lastAccessed;
/* 3271:     */     }
/* 3272:     */     
/* 3273:     */     public void invalidate()
/* 3274:     */     {
/* 3275:1909 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3276:     */       {
/* 3277:1910 */         if (!ReferenceCountedOpenSslEngine.this.isDestroyed()) {
/* 3278:1911 */           SSL.setTimeout(ReferenceCountedOpenSslEngine.this.ssl, 0L);
/* 3279:     */         }
/* 3280:     */       }
/* 3281:     */     }
/* 3282:     */     
/* 3283:     */     public boolean isValid()
/* 3284:     */     {
/* 3285:1918 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3286:     */       {
/* 3287:1919 */         if (!ReferenceCountedOpenSslEngine.this.isDestroyed()) {
/* 3288:1920 */           return System.currentTimeMillis() - SSL.getTimeout(ReferenceCountedOpenSslEngine.this.ssl) * 1000L < SSL.getTime(ReferenceCountedOpenSslEngine.this.ssl) * 1000L;
/* 3289:     */         }
/* 3290:     */       }
/* 3291:1923 */       return false;
/* 3292:     */     }
/* 3293:     */     
/* 3294:     */     public void putValue(String name, Object value)
/* 3295:     */     {
/* 3296:1928 */       if (name == null) {
/* 3297:1929 */         throw new NullPointerException("name");
/* 3298:     */       }
/* 3299:1931 */       if (value == null) {
/* 3300:1932 */         throw new NullPointerException("value");
/* 3301:     */       }
/* 3302:1934 */       Map<String, Object> values = this.values;
/* 3303:1935 */       if (values == null) {
/* 3304:1937 */         values = this.values = new HashMap(2);
/* 3305:     */       }
/* 3306:1939 */       Object old = values.put(name, value);
/* 3307:1940 */       if ((value instanceof SSLSessionBindingListener)) {
/* 3308:1941 */         ((SSLSessionBindingListener)value).valueBound(new SSLSessionBindingEvent(this, name));
/* 3309:     */       }
/* 3310:1943 */       notifyUnbound(old, name);
/* 3311:     */     }
/* 3312:     */     
/* 3313:     */     public Object getValue(String name)
/* 3314:     */     {
/* 3315:1948 */       if (name == null) {
/* 3316:1949 */         throw new NullPointerException("name");
/* 3317:     */       }
/* 3318:1951 */       if (this.values == null) {
/* 3319:1952 */         return null;
/* 3320:     */       }
/* 3321:1954 */       return this.values.get(name);
/* 3322:     */     }
/* 3323:     */     
/* 3324:     */     public void removeValue(String name)
/* 3325:     */     {
/* 3326:1959 */       if (name == null) {
/* 3327:1960 */         throw new NullPointerException("name");
/* 3328:     */       }
/* 3329:1962 */       Map<String, Object> values = this.values;
/* 3330:1963 */       if (values == null) {
/* 3331:1964 */         return;
/* 3332:     */       }
/* 3333:1966 */       Object old = values.remove(name);
/* 3334:1967 */       notifyUnbound(old, name);
/* 3335:     */     }
/* 3336:     */     
/* 3337:     */     public String[] getValueNames()
/* 3338:     */     {
/* 3339:1972 */       Map<String, Object> values = this.values;
/* 3340:1973 */       if ((values == null) || (values.isEmpty())) {
/* 3341:1974 */         return EmptyArrays.EMPTY_STRINGS;
/* 3342:     */       }
/* 3343:1976 */       return (String[])values.keySet().toArray(new String[values.size()]);
/* 3344:     */     }
/* 3345:     */     
/* 3346:     */     private void notifyUnbound(Object value, String name)
/* 3347:     */     {
/* 3348:1980 */       if ((value instanceof SSLSessionBindingListener)) {
/* 3349:1981 */         ((SSLSessionBindingListener)value).valueUnbound(new SSLSessionBindingEvent(this, name));
/* 3350:     */       }
/* 3351:     */     }
/* 3352:     */     
/* 3353:     */     void handshakeFinished()
/* 3354:     */       throws SSLException
/* 3355:     */     {
/* 3356:1990 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3357:     */       {
/* 3358:1991 */         if (!ReferenceCountedOpenSslEngine.this.isDestroyed())
/* 3359:     */         {
/* 3360:1992 */           this.id = SSL.getSessionId(ReferenceCountedOpenSslEngine.this.ssl);
/* 3361:1993 */           this.cipher = ReferenceCountedOpenSslEngine.this.toJavaCipherSuite(SSL.getCipherForSSL(ReferenceCountedOpenSslEngine.this.ssl));
/* 3362:1994 */           this.protocol = SSL.getVersion(ReferenceCountedOpenSslEngine.this.ssl);
/* 3363:     */           
/* 3364:1996 */           initPeerCerts();
/* 3365:1997 */           selectApplicationProtocol();
/* 3366:1998 */           ReferenceCountedOpenSslEngine.this.calculateMaxWrapOverhead();
/* 3367:     */           
/* 3368:2000 */           ReferenceCountedOpenSslEngine.this.handshakeState = ReferenceCountedOpenSslEngine.HandshakeState.FINISHED;
/* 3369:     */         }
/* 3370:     */         else
/* 3371:     */         {
/* 3372:2002 */           throw new SSLException("Already closed");
/* 3373:     */         }
/* 3374:     */       }
/* 3375:     */     }
/* 3376:     */     
/* 3377:     */     private void initPeerCerts()
/* 3378:     */     {
/* 3379:2013 */       byte[][] chain = SSL.getPeerCertChain(ReferenceCountedOpenSslEngine.this.ssl);
/* 3380:2014 */       if (ReferenceCountedOpenSslEngine.this.clientMode)
/* 3381:     */       {
/* 3382:2015 */         if (ReferenceCountedOpenSslEngine.isEmpty(chain))
/* 3383:     */         {
/* 3384:2016 */           this.peerCerts = EmptyArrays.EMPTY_CERTIFICATES;
/* 3385:2017 */           this.x509PeerCerts = EmptyArrays.EMPTY_JAVAX_X509_CERTIFICATES;
/* 3386:     */         }
/* 3387:     */         else
/* 3388:     */         {
/* 3389:2019 */           this.peerCerts = new Certificate[chain.length];
/* 3390:2020 */           this.x509PeerCerts = new javax.security.cert.X509Certificate[chain.length];
/* 3391:2021 */           initCerts(chain, 0);
/* 3392:     */         }
/* 3393:     */       }
/* 3394:     */       else
/* 3395:     */       {
/* 3396:2029 */         byte[] clientCert = SSL.getPeerCertificate(ReferenceCountedOpenSslEngine.this.ssl);
/* 3397:2030 */         if (ReferenceCountedOpenSslEngine.isEmpty(clientCert))
/* 3398:     */         {
/* 3399:2031 */           this.peerCerts = EmptyArrays.EMPTY_CERTIFICATES;
/* 3400:2032 */           this.x509PeerCerts = EmptyArrays.EMPTY_JAVAX_X509_CERTIFICATES;
/* 3401:     */         }
/* 3402:2034 */         else if (ReferenceCountedOpenSslEngine.isEmpty(chain))
/* 3403:     */         {
/* 3404:2035 */           this.peerCerts = new Certificate[] { new OpenSslX509Certificate(clientCert) };
/* 3405:2036 */           this.x509PeerCerts = new javax.security.cert.X509Certificate[] { new OpenSslJavaxX509Certificate(clientCert) };
/* 3406:     */         }
/* 3407:     */         else
/* 3408:     */         {
/* 3409:2038 */           this.peerCerts = new Certificate[chain.length + 1];
/* 3410:2039 */           this.x509PeerCerts = new javax.security.cert.X509Certificate[chain.length + 1];
/* 3411:2040 */           this.peerCerts[0] = new OpenSslX509Certificate(clientCert);
/* 3412:2041 */           this.x509PeerCerts[0] = new OpenSslJavaxX509Certificate(clientCert);
/* 3413:2042 */           initCerts(chain, 1);
/* 3414:     */         }
/* 3415:     */       }
/* 3416:     */     }
/* 3417:     */     
/* 3418:     */     private void initCerts(byte[][] chain, int startPos)
/* 3419:     */     {
/* 3420:2049 */       for (int i = 0; i < chain.length; i++)
/* 3421:     */       {
/* 3422:2050 */         int certPos = startPos + i;
/* 3423:2051 */         this.peerCerts[certPos] = new OpenSslX509Certificate(chain[i]);
/* 3424:2052 */         this.x509PeerCerts[certPos] = new OpenSslJavaxX509Certificate(chain[i]);
/* 3425:     */       }
/* 3426:     */     }
/* 3427:     */     
/* 3428:     */     private void selectApplicationProtocol()
/* 3429:     */       throws SSLException
/* 3430:     */     {
/* 3431:2060 */       ApplicationProtocolConfig.SelectedListenerFailureBehavior behavior = ReferenceCountedOpenSslEngine.this.apn.selectedListenerFailureBehavior();
/* 3432:2061 */       List<String> protocols = ReferenceCountedOpenSslEngine.this.apn.protocols();
/* 3433:2063 */       switch (ReferenceCountedOpenSslEngine.2.$SwitchMap$io$netty$handler$ssl$ApplicationProtocolConfig$Protocol[ReferenceCountedOpenSslEngine.this.apn.protocol().ordinal()])
/* 3434:     */       {
/* 3435:     */       case 1: 
/* 3436:     */         break;
/* 3437:     */       case 2: 
/* 3438:2069 */         String applicationProtocol = SSL.getAlpnSelected(ReferenceCountedOpenSslEngine.this.ssl);
/* 3439:2070 */         if (applicationProtocol != null) {
/* 3440:2071 */           ReferenceCountedOpenSslEngine.this.applicationProtocol = selectApplicationProtocol(protocols, behavior, applicationProtocol);
/* 3441:     */         }
/* 3442:     */         break;
/* 3443:     */       case 3: 
/* 3444:2076 */         String applicationProtocol = SSL.getNextProtoNegotiated(ReferenceCountedOpenSslEngine.this.ssl);
/* 3445:2077 */         if (applicationProtocol != null) {
/* 3446:2078 */           ReferenceCountedOpenSslEngine.this.applicationProtocol = selectApplicationProtocol(protocols, behavior, applicationProtocol);
/* 3447:     */         }
/* 3448:     */         break;
/* 3449:     */       case 4: 
/* 3450:2083 */         String applicationProtocol = SSL.getAlpnSelected(ReferenceCountedOpenSslEngine.this.ssl);
/* 3451:2084 */         if (applicationProtocol == null) {
/* 3452:2085 */           applicationProtocol = SSL.getNextProtoNegotiated(ReferenceCountedOpenSslEngine.this.ssl);
/* 3453:     */         }
/* 3454:2087 */         if (applicationProtocol != null) {
/* 3455:2088 */           ReferenceCountedOpenSslEngine.this.applicationProtocol = selectApplicationProtocol(protocols, behavior, applicationProtocol);
/* 3456:     */         }
/* 3457:     */         break;
/* 3458:     */       default: 
/* 3459:2093 */         throw new Error();
/* 3460:     */       }
/* 3461:     */     }
/* 3462:     */     
/* 3463:     */     private String selectApplicationProtocol(List<String> protocols, ApplicationProtocolConfig.SelectedListenerFailureBehavior behavior, String applicationProtocol)
/* 3464:     */       throws SSLException
/* 3465:     */     {
/* 3466:2100 */       if (behavior == ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT) {
/* 3467:2101 */         return applicationProtocol;
/* 3468:     */       }
/* 3469:2103 */       int size = protocols.size();
/* 3470:2104 */       assert (size > 0);
/* 3471:2105 */       if (protocols.contains(applicationProtocol)) {
/* 3472:2106 */         return applicationProtocol;
/* 3473:     */       }
/* 3474:2108 */       if (behavior == ApplicationProtocolConfig.SelectedListenerFailureBehavior.CHOOSE_MY_LAST_PROTOCOL) {
/* 3475:2109 */         return (String)protocols.get(size - 1);
/* 3476:     */       }
/* 3477:2111 */       throw new SSLException("unknown protocol " + applicationProtocol);
/* 3478:     */     }
/* 3479:     */     
/* 3480:     */     public Certificate[] getPeerCertificates()
/* 3481:     */       throws SSLPeerUnverifiedException
/* 3482:     */     {
/* 3483:2119 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3484:     */       {
/* 3485:2120 */         if (ReferenceCountedOpenSslEngine.isEmpty(this.peerCerts)) {
/* 3486:2121 */           throw new SSLPeerUnverifiedException("peer not verified");
/* 3487:     */         }
/* 3488:2123 */         return (Certificate[])this.peerCerts.clone();
/* 3489:     */       }
/* 3490:     */     }
/* 3491:     */     
/* 3492:     */     public Certificate[] getLocalCertificates()
/* 3493:     */     {
/* 3494:2129 */       if (ReferenceCountedOpenSslEngine.this.localCerts == null) {
/* 3495:2130 */         return null;
/* 3496:     */       }
/* 3497:2132 */       return (Certificate[])ReferenceCountedOpenSslEngine.this.localCerts.clone();
/* 3498:     */     }
/* 3499:     */     
/* 3500:     */     public javax.security.cert.X509Certificate[] getPeerCertificateChain()
/* 3501:     */       throws SSLPeerUnverifiedException
/* 3502:     */     {
/* 3503:2137 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3504:     */       {
/* 3505:2138 */         if (ReferenceCountedOpenSslEngine.isEmpty(this.x509PeerCerts)) {
/* 3506:2139 */           throw new SSLPeerUnverifiedException("peer not verified");
/* 3507:     */         }
/* 3508:2141 */         return (javax.security.cert.X509Certificate[])this.x509PeerCerts.clone();
/* 3509:     */       }
/* 3510:     */     }
/* 3511:     */     
/* 3512:     */     public Principal getPeerPrincipal()
/* 3513:     */       throws SSLPeerUnverifiedException
/* 3514:     */     {
/* 3515:2147 */       Certificate[] peer = getPeerCertificates();
/* 3516:     */       
/* 3517:     */ 
/* 3518:2150 */       return ((java.security.cert.X509Certificate)peer[0]).getSubjectX500Principal();
/* 3519:     */     }
/* 3520:     */     
/* 3521:     */     public Principal getLocalPrincipal()
/* 3522:     */     {
/* 3523:2155 */       Certificate[] local = ReferenceCountedOpenSslEngine.this.localCerts;
/* 3524:2156 */       if ((local == null) || (local.length == 0)) {
/* 3525:2157 */         return null;
/* 3526:     */       }
/* 3527:2159 */       return ((java.security.cert.X509Certificate)local[0]).getIssuerX500Principal();
/* 3528:     */     }
/* 3529:     */     
/* 3530:     */     public String getCipherSuite()
/* 3531:     */     {
/* 3532:2164 */       synchronized (ReferenceCountedOpenSslEngine.this)
/* 3533:     */       {
/* 3534:2165 */         if (this.cipher == null) {
/* 3535:2166 */           return "SSL_NULL_WITH_NULL_NULL";
/* 3536:     */         }
/* 3537:2168 */         return this.cipher;
/* 3538:     */       }
/* 3539:     */     }
/* 3540:     */     
/* 3541:     */     public String getProtocol()
/* 3542:     */     {
/* 3543:2174 */       String protocol = this.protocol;
/* 3544:2175 */       if (protocol == null) {
/* 3545:2176 */         synchronized (ReferenceCountedOpenSslEngine.this)
/* 3546:     */         {
/* 3547:2177 */           if (!ReferenceCountedOpenSslEngine.this.isDestroyed()) {
/* 3548:2178 */             protocol = SSL.getVersion(ReferenceCountedOpenSslEngine.this.ssl);
/* 3549:     */           } else {
/* 3550:2180 */             protocol = "";
/* 3551:     */           }
/* 3552:     */         }
/* 3553:     */       }
/* 3554:2184 */       return protocol;
/* 3555:     */     }
/* 3556:     */     
/* 3557:     */     public String getPeerHost()
/* 3558:     */     {
/* 3559:2189 */       return ReferenceCountedOpenSslEngine.this.getPeerHost();
/* 3560:     */     }
/* 3561:     */     
/* 3562:     */     public int getPeerPort()
/* 3563:     */     {
/* 3564:2194 */       return ReferenceCountedOpenSslEngine.this.getPeerPort();
/* 3565:     */     }
/* 3566:     */     
/* 3567:     */     public int getPacketBufferSize()
/* 3568:     */     {
/* 3569:2199 */       return ReferenceCountedOpenSslEngine.this.maxEncryptedPacketLength();
/* 3570:     */     }
/* 3571:     */     
/* 3572:     */     public int getApplicationBufferSize()
/* 3573:     */     {
/* 3574:2204 */       return this.applicationBufferSize;
/* 3575:     */     }
/* 3576:     */     
/* 3577:     */     void tryExpandApplicationBufferSize(int packetLengthDataOnly)
/* 3578:     */     {
/* 3579:2214 */       if ((packetLengthDataOnly > ReferenceCountedOpenSslEngine.MAX_PLAINTEXT_LENGTH) && (this.applicationBufferSize != ReferenceCountedOpenSslEngine.MAX_RECORD_SIZE)) {
/* 3580:2215 */         this.applicationBufferSize = ReferenceCountedOpenSslEngine.MAX_RECORD_SIZE;
/* 3581:     */       }
/* 3582:     */     }
/* 3583:     */   }
/* 3584:     */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ReferenceCountedOpenSslEngine
 * JD-Core Version:    0.7.0.1
 */