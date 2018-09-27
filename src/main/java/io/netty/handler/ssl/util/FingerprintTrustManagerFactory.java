/*   1:    */ package io.netty.handler.ssl.util;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufUtil;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   6:    */ import io.netty.util.internal.EmptyArrays;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import java.security.KeyStore;
/*   9:    */ import java.security.MessageDigest;
/*  10:    */ import java.security.NoSuchAlgorithmException;
/*  11:    */ import java.security.cert.CertificateEncodingException;
/*  12:    */ import java.security.cert.CertificateException;
/*  13:    */ import java.security.cert.X509Certificate;
/*  14:    */ import java.util.ArrayList;
/*  15:    */ import java.util.Arrays;
/*  16:    */ import java.util.List;
/*  17:    */ import java.util.regex.Matcher;
/*  18:    */ import java.util.regex.Pattern;
/*  19:    */ import javax.net.ssl.ManagerFactoryParameters;
/*  20:    */ import javax.net.ssl.TrustManager;
/*  21:    */ import javax.net.ssl.X509TrustManager;
/*  22:    */ 
/*  23:    */ public final class FingerprintTrustManagerFactory
/*  24:    */   extends SimpleTrustManagerFactory
/*  25:    */ {
/*  26: 75 */   private static final Pattern FINGERPRINT_PATTERN = Pattern.compile("^[0-9a-fA-F:]+$");
/*  27: 76 */   private static final Pattern FINGERPRINT_STRIP_PATTERN = Pattern.compile(":");
/*  28:    */   private static final int SHA1_BYTE_LEN = 20;
/*  29:    */   private static final int SHA1_HEX_LEN = 40;
/*  30: 80 */   private static final FastThreadLocal<MessageDigest> tlmd = new FastThreadLocal()
/*  31:    */   {
/*  32:    */     protected MessageDigest initialValue()
/*  33:    */     {
/*  34:    */       try
/*  35:    */       {
/*  36: 84 */         return MessageDigest.getInstance("SHA1");
/*  37:    */       }
/*  38:    */       catch (NoSuchAlgorithmException e)
/*  39:    */       {
/*  40: 87 */         throw new Error(e);
/*  41:    */       }
/*  42:    */     }
/*  43:    */   };
/*  44: 92 */   private final TrustManager tm = new X509TrustManager()
/*  45:    */   {
/*  46:    */     public void checkClientTrusted(X509Certificate[] chain, String s)
/*  47:    */       throws CertificateException
/*  48:    */     {
/*  49: 96 */       checkTrusted("client", chain);
/*  50:    */     }
/*  51:    */     
/*  52:    */     public void checkServerTrusted(X509Certificate[] chain, String s)
/*  53:    */       throws CertificateException
/*  54:    */     {
/*  55:101 */       checkTrusted("server", chain);
/*  56:    */     }
/*  57:    */     
/*  58:    */     private void checkTrusted(String type, X509Certificate[] chain)
/*  59:    */       throws CertificateException
/*  60:    */     {
/*  61:105 */       X509Certificate cert = chain[0];
/*  62:106 */       byte[] fingerprint = fingerprint(cert);
/*  63:107 */       boolean found = false;
/*  64:108 */       for (byte[] allowedFingerprint : FingerprintTrustManagerFactory.this.fingerprints) {
/*  65:109 */         if (Arrays.equals(fingerprint, allowedFingerprint))
/*  66:    */         {
/*  67:110 */           found = true;
/*  68:111 */           break;
/*  69:    */         }
/*  70:    */       }
/*  71:115 */       if (!found) {
/*  72:117 */         throw new CertificateException(type + " certificate with unknown fingerprint: " + cert.getSubjectDN());
/*  73:    */       }
/*  74:    */     }
/*  75:    */     
/*  76:    */     private byte[] fingerprint(X509Certificate cert)
/*  77:    */       throws CertificateEncodingException
/*  78:    */     {
/*  79:122 */       MessageDigest md = (MessageDigest)FingerprintTrustManagerFactory.tlmd.get();
/*  80:123 */       md.reset();
/*  81:124 */       return md.digest(cert.getEncoded());
/*  82:    */     }
/*  83:    */     
/*  84:    */     public X509Certificate[] getAcceptedIssuers()
/*  85:    */     {
/*  86:129 */       return EmptyArrays.EMPTY_X509_CERTIFICATES;
/*  87:    */     }
/*  88:    */   };
/*  89:    */   private final byte[][] fingerprints;
/*  90:    */   
/*  91:    */   public FingerprintTrustManagerFactory(Iterable<String> fingerprints)
/*  92:    */   {
/*  93:141 */     this(toFingerprintArray(fingerprints));
/*  94:    */   }
/*  95:    */   
/*  96:    */   public FingerprintTrustManagerFactory(String... fingerprints)
/*  97:    */   {
/*  98:150 */     this(toFingerprintArray(Arrays.asList(fingerprints)));
/*  99:    */   }
/* 100:    */   
/* 101:    */   public FingerprintTrustManagerFactory(byte[]... fingerprints)
/* 102:    */   {
/* 103:159 */     if (fingerprints == null) {
/* 104:160 */       throw new NullPointerException("fingerprints");
/* 105:    */     }
/* 106:163 */     List<byte[]> list = new ArrayList(fingerprints.length);
/* 107:164 */     for (byte[] f : fingerprints)
/* 108:    */     {
/* 109:165 */       if (f == null) {
/* 110:    */         break;
/* 111:    */       }
/* 112:168 */       if (f.length != 20) {
/* 113:170 */         throw new IllegalArgumentException("malformed fingerprint: " + ByteBufUtil.hexDump(Unpooled.wrappedBuffer(f)) + " (expected: SHA1)");
/* 114:    */       }
/* 115:172 */       list.add(f.clone());
/* 116:    */     }
/* 117:175 */     this.fingerprints = ((byte[][])list.toArray(new byte[list.size()][]));
/* 118:    */   }
/* 119:    */   
/* 120:    */   private static byte[][] toFingerprintArray(Iterable<String> fingerprints)
/* 121:    */   {
/* 122:179 */     if (fingerprints == null) {
/* 123:180 */       throw new NullPointerException("fingerprints");
/* 124:    */     }
/* 125:183 */     List<byte[]> list = new ArrayList();
/* 126:184 */     for (String f : fingerprints)
/* 127:    */     {
/* 128:185 */       if (f == null) {
/* 129:    */         break;
/* 130:    */       }
/* 131:189 */       if (!FINGERPRINT_PATTERN.matcher(f).matches()) {
/* 132:190 */         throw new IllegalArgumentException("malformed fingerprint: " + f);
/* 133:    */       }
/* 134:192 */       f = FINGERPRINT_STRIP_PATTERN.matcher(f).replaceAll("");
/* 135:193 */       if (f.length() != 40) {
/* 136:194 */         throw new IllegalArgumentException("malformed fingerprint: " + f + " (expected: SHA1)");
/* 137:    */       }
/* 138:197 */       list.add(StringUtil.decodeHexDump(f));
/* 139:    */     }
/* 140:200 */     return (byte[][])list.toArray(new byte[list.size()][]);
/* 141:    */   }
/* 142:    */   
/* 143:    */   protected void engineInit(KeyStore keyStore)
/* 144:    */     throws Exception
/* 145:    */   {}
/* 146:    */   
/* 147:    */   protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
/* 148:    */     throws Exception
/* 149:    */   {}
/* 150:    */   
/* 151:    */   protected TrustManager[] engineGetTrustManagers()
/* 152:    */   {
/* 153:211 */     return new TrustManager[] { this.tm };
/* 154:    */   }
/* 155:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.FingerprintTrustManagerFactory
 * JD-Core Version:    0.7.0.1
 */