/*  1:   */ package io.netty.handler.ssl.util;
/*  2:   */ 
/*  3:   */ import java.math.BigInteger;
/*  4:   */ import java.security.KeyPair;
/*  5:   */ import java.security.PrivateKey;
/*  6:   */ import java.security.Provider;
/*  7:   */ import java.security.SecureRandom;
/*  8:   */ import java.security.cert.X509Certificate;
/*  9:   */ import java.util.Date;
/* 10:   */ import org.bouncycastle.asn1.x500.X500Name;
/* 11:   */ import org.bouncycastle.cert.X509CertificateHolder;
/* 12:   */ import org.bouncycastle.cert.X509v3CertificateBuilder;
/* 13:   */ import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
/* 14:   */ import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
/* 15:   */ import org.bouncycastle.jce.provider.BouncyCastleProvider;
/* 16:   */ import org.bouncycastle.operator.ContentSigner;
/* 17:   */ import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
/* 18:   */ 
/* 19:   */ final class BouncyCastleSelfSignedCertGenerator
/* 20:   */ {
/* 21:43 */   private static final Provider PROVIDER = new BouncyCastleProvider();
/* 22:   */   
/* 23:   */   static String[] generate(String fqdn, KeyPair keypair, SecureRandom random, Date notBefore, Date notAfter)
/* 24:   */     throws Exception
/* 25:   */   {
/* 26:47 */     PrivateKey key = keypair.getPrivate();
/* 27:   */     
/* 28:   */ 
/* 29:50 */     X500Name owner = new X500Name("CN=" + fqdn);
/* 30:   */     
/* 31:52 */     X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(owner, new BigInteger(64, random), notBefore, notAfter, owner, keypair.getPublic());
/* 32:   */     
/* 33:54 */     ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(key);
/* 34:55 */     X509CertificateHolder certHolder = builder.build(signer);
/* 35:56 */     X509Certificate cert = new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(certHolder);
/* 36:57 */     cert.verify(keypair.getPublic());
/* 37:   */     
/* 38:59 */     return SelfSignedCertificate.newSelfSignedCertificate(fqdn, key, cert);
/* 39:   */   }
/* 40:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.BouncyCastleSelfSignedCertGenerator
 * JD-Core Version:    0.7.0.1
 */