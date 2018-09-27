/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.math.BigInteger;
/*   4:    */ import java.security.InvalidKeyException;
/*   5:    */ import java.security.NoSuchAlgorithmException;
/*   6:    */ import java.security.NoSuchProviderException;
/*   7:    */ import java.security.Principal;
/*   8:    */ import java.security.PublicKey;
/*   9:    */ import java.security.SignatureException;
/*  10:    */ import java.util.Date;
/*  11:    */ import javax.security.cert.CertificateException;
/*  12:    */ import javax.security.cert.CertificateExpiredException;
/*  13:    */ import javax.security.cert.CertificateNotYetValidException;
/*  14:    */ import javax.security.cert.X509Certificate;
/*  15:    */ 
/*  16:    */ final class OpenSslJavaxX509Certificate
/*  17:    */   extends X509Certificate
/*  18:    */ {
/*  19:    */   private final byte[] bytes;
/*  20:    */   private X509Certificate wrapped;
/*  21:    */   
/*  22:    */   public OpenSslJavaxX509Certificate(byte[] bytes)
/*  23:    */   {
/*  24: 36 */     this.bytes = bytes;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public void checkValidity()
/*  28:    */     throws CertificateExpiredException, CertificateNotYetValidException
/*  29:    */   {
/*  30: 41 */     unwrap().checkValidity();
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void checkValidity(Date date)
/*  34:    */     throws CertificateExpiredException, CertificateNotYetValidException
/*  35:    */   {
/*  36: 46 */     unwrap().checkValidity(date);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public int getVersion()
/*  40:    */   {
/*  41: 51 */     return unwrap().getVersion();
/*  42:    */   }
/*  43:    */   
/*  44:    */   public BigInteger getSerialNumber()
/*  45:    */   {
/*  46: 56 */     return unwrap().getSerialNumber();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Principal getIssuerDN()
/*  50:    */   {
/*  51: 61 */     return unwrap().getIssuerDN();
/*  52:    */   }
/*  53:    */   
/*  54:    */   public Principal getSubjectDN()
/*  55:    */   {
/*  56: 66 */     return unwrap().getSubjectDN();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public Date getNotBefore()
/*  60:    */   {
/*  61: 71 */     return unwrap().getNotBefore();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public Date getNotAfter()
/*  65:    */   {
/*  66: 76 */     return unwrap().getNotAfter();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public String getSigAlgName()
/*  70:    */   {
/*  71: 81 */     return unwrap().getSigAlgName();
/*  72:    */   }
/*  73:    */   
/*  74:    */   public String getSigAlgOID()
/*  75:    */   {
/*  76: 86 */     return unwrap().getSigAlgOID();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public byte[] getSigAlgParams()
/*  80:    */   {
/*  81: 91 */     return unwrap().getSigAlgParams();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public byte[] getEncoded()
/*  85:    */   {
/*  86: 96 */     return (byte[])this.bytes.clone();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void verify(PublicKey key)
/*  90:    */     throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
/*  91:    */   {
/*  92:103 */     unwrap().verify(key);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void verify(PublicKey key, String sigProvider)
/*  96:    */     throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
/*  97:    */   {
/*  98:110 */     unwrap().verify(key, sigProvider);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public String toString()
/* 102:    */   {
/* 103:115 */     return unwrap().toString();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public PublicKey getPublicKey()
/* 107:    */   {
/* 108:120 */     return unwrap().getPublicKey();
/* 109:    */   }
/* 110:    */   
/* 111:    */   private X509Certificate unwrap()
/* 112:    */   {
/* 113:124 */     X509Certificate wrapped = this.wrapped;
/* 114:125 */     if (wrapped == null) {
/* 115:    */       try
/* 116:    */       {
/* 117:127 */         wrapped = this.wrapped = X509Certificate.getInstance(this.bytes);
/* 118:    */       }
/* 119:    */       catch (CertificateException e)
/* 120:    */       {
/* 121:129 */         throw new IllegalStateException(e);
/* 122:    */       }
/* 123:    */     }
/* 124:132 */     return wrapped;
/* 125:    */   }
/* 126:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslJavaxX509Certificate
 * JD-Core Version:    0.7.0.1
 */