/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBufAllocator;
/*  4:   */ import java.security.cert.Certificate;
/*  5:   */ import javax.net.ssl.SSLEngine;
/*  6:   */ import javax.net.ssl.SSLException;
/*  7:   */ 
/*  8:   */ public abstract class OpenSslContext
/*  9:   */   extends ReferenceCountedOpenSslContext
/* 10:   */ {
/* 11:   */   OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apnCfg, long sessionCacheSize, long sessionTimeout, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/* 12:   */     throws SSLException
/* 13:   */   {
/* 14:34 */     super(ciphers, cipherFilter, apnCfg, sessionCacheSize, sessionTimeout, mode, keyCertChain, clientAuth, protocols, startTls, enableOcsp, false);
/* 15:   */   }
/* 16:   */   
/* 17:   */   OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp)
/* 18:   */     throws SSLException
/* 19:   */   {
/* 20:43 */     super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, mode, keyCertChain, clientAuth, protocols, startTls, enableOcsp, false);
/* 21:   */   }
/* 22:   */   
/* 23:   */   final SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode)
/* 24:   */   {
/* 25:49 */     return new OpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode);
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected final void finalize()
/* 29:   */     throws Throwable
/* 30:   */   {
/* 31:55 */     super.finalize();
/* 32:56 */     OpenSsl.releaseIfNeeded(this);
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslContext
 * JD-Core Version:    0.7.0.1
 */