/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.internal.tcnative.CertificateVerifier;
/*  4:   */ import java.security.cert.CertificateException;
/*  5:   */ 
/*  6:   */ public final class OpenSslCertificateException
/*  7:   */   extends CertificateException
/*  8:   */ {
/*  9:   */   private static final long serialVersionUID = 5542675253797129798L;
/* 10:   */   private final int errorCode;
/* 11:   */   
/* 12:   */   public OpenSslCertificateException(int errorCode)
/* 13:   */   {
/* 14:36 */     this((String)null, errorCode);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public OpenSslCertificateException(String msg, int errorCode)
/* 18:   */   {
/* 19:44 */     super(msg);
/* 20:45 */     this.errorCode = checkErrorCode(errorCode);
/* 21:   */   }
/* 22:   */   
/* 23:   */   public OpenSslCertificateException(String message, Throwable cause, int errorCode)
/* 24:   */   {
/* 25:53 */     super(message, cause);
/* 26:54 */     this.errorCode = checkErrorCode(errorCode);
/* 27:   */   }
/* 28:   */   
/* 29:   */   public OpenSslCertificateException(Throwable cause, int errorCode)
/* 30:   */   {
/* 31:62 */     this(null, cause, errorCode);
/* 32:   */   }
/* 33:   */   
/* 34:   */   public int errorCode()
/* 35:   */   {
/* 36:69 */     return this.errorCode;
/* 37:   */   }
/* 38:   */   
/* 39:   */   private static int checkErrorCode(int errorCode)
/* 40:   */   {
/* 41:73 */     if (!CertificateVerifier.isValid(errorCode)) {
/* 42:74 */       throw new IllegalArgumentException("errorCode '" + errorCode + "' invalid, see https://www.openssl.org/docs/man1.0.2/apps/verify.html.");
/* 43:   */     }
/* 44:77 */     return errorCode;
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslCertificateException
 * JD-Core Version:    0.7.0.1
 */