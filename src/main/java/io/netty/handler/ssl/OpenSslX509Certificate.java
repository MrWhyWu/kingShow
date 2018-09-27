/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import java.io.ByteArrayInputStream;
/*   4:    */ import java.math.BigInteger;
/*   5:    */ import java.security.InvalidKeyException;
/*   6:    */ import java.security.NoSuchAlgorithmException;
/*   7:    */ import java.security.NoSuchProviderException;
/*   8:    */ import java.security.Principal;
/*   9:    */ import java.security.PublicKey;
/*  10:    */ import java.security.SignatureException;
/*  11:    */ import java.security.cert.CertificateEncodingException;
/*  12:    */ import java.security.cert.CertificateException;
/*  13:    */ import java.security.cert.CertificateExpiredException;
/*  14:    */ import java.security.cert.CertificateFactory;
/*  15:    */ import java.security.cert.CertificateNotYetValidException;
/*  16:    */ import java.security.cert.X509Certificate;
/*  17:    */ import java.util.Date;
/*  18:    */ import java.util.Set;
/*  19:    */ 
/*  20:    */ final class OpenSslX509Certificate
/*  21:    */   extends X509Certificate
/*  22:    */ {
/*  23:    */   private final byte[] bytes;
/*  24:    */   private X509Certificate wrapped;
/*  25:    */   
/*  26:    */   public OpenSslX509Certificate(byte[] bytes)
/*  27:    */   {
/*  28: 40 */     this.bytes = bytes;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void checkValidity()
/*  32:    */     throws CertificateExpiredException, CertificateNotYetValidException
/*  33:    */   {
/*  34: 45 */     unwrap().checkValidity();
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void checkValidity(Date date)
/*  38:    */     throws CertificateExpiredException, CertificateNotYetValidException
/*  39:    */   {
/*  40: 50 */     unwrap().checkValidity(date);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public int getVersion()
/*  44:    */   {
/*  45: 55 */     return unwrap().getVersion();
/*  46:    */   }
/*  47:    */   
/*  48:    */   public BigInteger getSerialNumber()
/*  49:    */   {
/*  50: 60 */     return unwrap().getSerialNumber();
/*  51:    */   }
/*  52:    */   
/*  53:    */   public Principal getIssuerDN()
/*  54:    */   {
/*  55: 65 */     return unwrap().getIssuerDN();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public Principal getSubjectDN()
/*  59:    */   {
/*  60: 70 */     return unwrap().getSubjectDN();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Date getNotBefore()
/*  64:    */   {
/*  65: 75 */     return unwrap().getNotBefore();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public Date getNotAfter()
/*  69:    */   {
/*  70: 80 */     return unwrap().getNotAfter();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public byte[] getTBSCertificate()
/*  74:    */     throws CertificateEncodingException
/*  75:    */   {
/*  76: 85 */     return unwrap().getTBSCertificate();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public byte[] getSignature()
/*  80:    */   {
/*  81: 90 */     return unwrap().getSignature();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public String getSigAlgName()
/*  85:    */   {
/*  86: 95 */     return unwrap().getSigAlgName();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public String getSigAlgOID()
/*  90:    */   {
/*  91:100 */     return unwrap().getSigAlgOID();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public byte[] getSigAlgParams()
/*  95:    */   {
/*  96:105 */     return unwrap().getSigAlgParams();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public boolean[] getIssuerUniqueID()
/* 100:    */   {
/* 101:110 */     return unwrap().getIssuerUniqueID();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public boolean[] getSubjectUniqueID()
/* 105:    */   {
/* 106:115 */     return unwrap().getSubjectUniqueID();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public boolean[] getKeyUsage()
/* 110:    */   {
/* 111:120 */     return unwrap().getKeyUsage();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public int getBasicConstraints()
/* 115:    */   {
/* 116:125 */     return unwrap().getBasicConstraints();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public byte[] getEncoded()
/* 120:    */   {
/* 121:130 */     return (byte[])this.bytes.clone();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void verify(PublicKey key)
/* 125:    */     throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
/* 126:    */   {
/* 127:137 */     unwrap().verify(key);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void verify(PublicKey key, String sigProvider)
/* 131:    */     throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
/* 132:    */   {
/* 133:144 */     unwrap().verify(key, sigProvider);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public String toString()
/* 137:    */   {
/* 138:149 */     return unwrap().toString();
/* 139:    */   }
/* 140:    */   
/* 141:    */   public PublicKey getPublicKey()
/* 142:    */   {
/* 143:154 */     return unwrap().getPublicKey();
/* 144:    */   }
/* 145:    */   
/* 146:    */   public boolean hasUnsupportedCriticalExtension()
/* 147:    */   {
/* 148:159 */     return unwrap().hasUnsupportedCriticalExtension();
/* 149:    */   }
/* 150:    */   
/* 151:    */   public Set<String> getCriticalExtensionOIDs()
/* 152:    */   {
/* 153:164 */     return unwrap().getCriticalExtensionOIDs();
/* 154:    */   }
/* 155:    */   
/* 156:    */   public Set<String> getNonCriticalExtensionOIDs()
/* 157:    */   {
/* 158:169 */     return unwrap().getNonCriticalExtensionOIDs();
/* 159:    */   }
/* 160:    */   
/* 161:    */   public byte[] getExtensionValue(String oid)
/* 162:    */   {
/* 163:174 */     return unwrap().getExtensionValue(oid);
/* 164:    */   }
/* 165:    */   
/* 166:    */   private X509Certificate unwrap()
/* 167:    */   {
/* 168:178 */     X509Certificate wrapped = this.wrapped;
/* 169:179 */     if (wrapped == null) {
/* 170:    */       try
/* 171:    */       {
/* 172:181 */         wrapped = this.wrapped = (X509Certificate)SslContext.X509_CERT_FACTORY.generateCertificate(new ByteArrayInputStream(this.bytes));
/* 173:    */       }
/* 174:    */       catch (CertificateException e)
/* 175:    */       {
/* 176:184 */         throw new IllegalStateException(e);
/* 177:    */       }
/* 178:    */     }
/* 179:187 */     return wrapped;
/* 180:    */   }
/* 181:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslX509Certificate
 * JD-Core Version:    0.7.0.1
 */