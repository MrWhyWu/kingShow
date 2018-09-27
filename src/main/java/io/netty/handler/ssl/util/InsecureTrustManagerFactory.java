/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.EmptyArrays;
/*  4:   */ import io.netty.util.internal.logging.InternalLogger;
/*  5:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  6:   */ import java.security.KeyStore;
/*  7:   */ import java.security.cert.X509Certificate;
/*  8:   */ import javax.net.ssl.ManagerFactoryParameters;
/*  9:   */ import javax.net.ssl.TrustManager;
/* 10:   */ import javax.net.ssl.TrustManagerFactory;
/* 11:   */ import javax.net.ssl.X509TrustManager;
/* 12:   */ 
/* 13:   */ public final class InsecureTrustManagerFactory
/* 14:   */   extends SimpleTrustManagerFactory
/* 15:   */ {
/* 16:40 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(InsecureTrustManagerFactory.class);
/* 17:42 */   public static final TrustManagerFactory INSTANCE = new InsecureTrustManagerFactory();
/* 18:44 */   private static final TrustManager tm = new X509TrustManager()
/* 19:   */   {
/* 20:   */     public void checkClientTrusted(X509Certificate[] chain, String s)
/* 21:   */     {
/* 22:47 */       InsecureTrustManagerFactory.logger.debug("Accepting a client certificate: " + chain[0].getSubjectDN());
/* 23:   */     }
/* 24:   */     
/* 25:   */     public void checkServerTrusted(X509Certificate[] chain, String s)
/* 26:   */     {
/* 27:52 */       InsecureTrustManagerFactory.logger.debug("Accepting a server certificate: " + chain[0].getSubjectDN());
/* 28:   */     }
/* 29:   */     
/* 30:   */     public X509Certificate[] getAcceptedIssuers()
/* 31:   */     {
/* 32:57 */       return EmptyArrays.EMPTY_X509_CERTIFICATES;
/* 33:   */     }
/* 34:   */   };
/* 35:   */   
/* 36:   */   protected void engineInit(KeyStore keyStore)
/* 37:   */     throws Exception
/* 38:   */   {}
/* 39:   */   
/* 40:   */   protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
/* 41:   */     throws Exception
/* 42:   */   {}
/* 43:   */   
/* 44:   */   protected TrustManager[] engineGetTrustManagers()
/* 45:   */   {
/* 46:71 */     return new TrustManager[] { tm };
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.InsecureTrustManagerFactory
 * JD-Core Version:    0.7.0.1
 */