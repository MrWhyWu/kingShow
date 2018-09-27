/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import java.math.BigInteger;
/*  4:   */ import java.security.KeyPair;
/*  5:   */ import java.security.PrivateKey;
/*  6:   */ import java.security.SecureRandom;
/*  7:   */ import java.security.cert.CertificateException;
/*  8:   */ import java.util.Date;
/*  9:   */ import sun.security.x509.AlgorithmId;
/* 10:   */ import sun.security.x509.CertificateAlgorithmId;
/* 11:   */ import sun.security.x509.CertificateIssuerName;
/* 12:   */ import sun.security.x509.CertificateSerialNumber;
/* 13:   */ import sun.security.x509.CertificateSubjectName;
/* 14:   */ import sun.security.x509.CertificateValidity;
/* 15:   */ import sun.security.x509.CertificateVersion;
/* 16:   */ import sun.security.x509.CertificateX509Key;
/* 17:   */ import sun.security.x509.X500Name;
/* 18:   */ import sun.security.x509.X509CertImpl;
/* 19:   */ import sun.security.x509.X509CertInfo;
/* 20:   */ 
/* 21:   */ final class OpenJdkSelfSignedCertGenerator
/* 22:   */ {
/* 23:   */   static String[] generate(String fqdn, KeyPair keypair, SecureRandom random, Date notBefore, Date notAfter)
/* 24:   */     throws Exception
/* 25:   */   {
/* 26:47 */     PrivateKey key = keypair.getPrivate();
/* 27:   */     
/* 28:   */ 
/* 29:50 */     X509CertInfo info = new X509CertInfo();
/* 30:51 */     X500Name owner = new X500Name("CN=" + fqdn);
/* 31:52 */     info.set("version", new CertificateVersion(2));
/* 32:53 */     info.set("serialNumber", new CertificateSerialNumber(new BigInteger(64, random)));
/* 33:   */     try
/* 34:   */     {
/* 35:55 */       info.set("subject", new CertificateSubjectName(owner));
/* 36:   */     }
/* 37:   */     catch (CertificateException ignore)
/* 38:   */     {
/* 39:57 */       info.set("subject", owner);
/* 40:   */     }
/* 41:   */     try
/* 42:   */     {
/* 43:60 */       info.set("issuer", new CertificateIssuerName(owner));
/* 44:   */     }
/* 45:   */     catch (CertificateException ignore)
/* 46:   */     {
/* 47:62 */       info.set("issuer", owner);
/* 48:   */     }
/* 49:64 */     info.set("validity", new CertificateValidity(notBefore, notAfter));
/* 50:65 */     info.set("key", new CertificateX509Key(keypair.getPublic()));
/* 51:66 */     info.set("algorithmID", new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));
/* 52:   */     
/* 53:   */ 
/* 54:   */ 
/* 55:70 */     X509CertImpl cert = new X509CertImpl(info);
/* 56:71 */     cert.sign(key, "SHA1withRSA");
/* 57:   */     
/* 58:   */ 
/* 59:74 */     info.set("algorithmID.algorithm", cert.get("x509.algorithm"));
/* 60:75 */     cert = new X509CertImpl(info);
/* 61:76 */     cert.sign(key, "SHA1withRSA");
/* 62:77 */     cert.verify(keypair.getPublic());
/* 63:   */     
/* 64:79 */     return SelfSignedCertificate.newSelfSignedCertificate(fqdn, key, cert);
/* 65:   */   }
/* 66:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.OpenJdkSelfSignedCertGenerator
 * JD-Core Version:    0.7.0.1
 */