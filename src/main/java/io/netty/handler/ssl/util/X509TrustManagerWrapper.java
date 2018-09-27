/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.ObjectUtil;
/*  4:   */ import java.net.Socket;
/*  5:   */ import java.security.cert.CertificateException;
/*  6:   */ import java.security.cert.X509Certificate;
/*  7:   */ import javax.net.ssl.SSLEngine;
/*  8:   */ import javax.net.ssl.X509ExtendedTrustManager;
/*  9:   */ import javax.net.ssl.X509TrustManager;
/* 10:   */ 
/* 11:   */ final class X509TrustManagerWrapper
/* 12:   */   extends X509ExtendedTrustManager
/* 13:   */ {
/* 14:   */   private final X509TrustManager delegate;
/* 15:   */   
/* 16:   */   X509TrustManagerWrapper(X509TrustManager delegate)
/* 17:   */   {
/* 18:32 */     this.delegate = ((X509TrustManager)ObjectUtil.checkNotNull(delegate, "delegate"));
/* 19:   */   }
/* 20:   */   
/* 21:   */   public void checkClientTrusted(X509Certificate[] chain, String s)
/* 22:   */     throws CertificateException
/* 23:   */   {
/* 24:37 */     this.delegate.checkClientTrusted(chain, s);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public void checkClientTrusted(X509Certificate[] chain, String s, Socket socket)
/* 28:   */     throws CertificateException
/* 29:   */   {
/* 30:43 */     this.delegate.checkClientTrusted(chain, s);
/* 31:   */   }
/* 32:   */   
/* 33:   */   public void checkClientTrusted(X509Certificate[] chain, String s, SSLEngine sslEngine)
/* 34:   */     throws CertificateException
/* 35:   */   {
/* 36:49 */     this.delegate.checkClientTrusted(chain, s);
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void checkServerTrusted(X509Certificate[] chain, String s)
/* 40:   */     throws CertificateException
/* 41:   */   {
/* 42:54 */     this.delegate.checkServerTrusted(chain, s);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public void checkServerTrusted(X509Certificate[] chain, String s, Socket socket)
/* 46:   */     throws CertificateException
/* 47:   */   {
/* 48:60 */     this.delegate.checkServerTrusted(chain, s);
/* 49:   */   }
/* 50:   */   
/* 51:   */   public void checkServerTrusted(X509Certificate[] chain, String s, SSLEngine sslEngine)
/* 52:   */     throws CertificateException
/* 53:   */   {
/* 54:66 */     this.delegate.checkServerTrusted(chain, s);
/* 55:   */   }
/* 56:   */   
/* 57:   */   public X509Certificate[] getAcceptedIssuers()
/* 58:   */   {
/* 59:71 */     return this.delegate.getAcceptedIssuers();
/* 60:   */   }
/* 61:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.X509TrustManagerWrapper
 * JD-Core Version:    0.7.0.1
 */