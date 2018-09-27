/*   1:    */ package io.netty.handler.ssl.util;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.base64.Base64;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.io.File;
/*  11:    */ import java.io.FileInputStream;
/*  12:    */ import java.io.FileOutputStream;
/*  13:    */ import java.io.IOException;
/*  14:    */ import java.io.OutputStream;
/*  15:    */ import java.security.KeyPair;
/*  16:    */ import java.security.KeyPairGenerator;
/*  17:    */ import java.security.NoSuchAlgorithmException;
/*  18:    */ import java.security.PrivateKey;
/*  19:    */ import java.security.SecureRandom;
/*  20:    */ import java.security.cert.CertificateEncodingException;
/*  21:    */ import java.security.cert.CertificateException;
/*  22:    */ import java.security.cert.CertificateFactory;
/*  23:    */ import java.security.cert.X509Certificate;
/*  24:    */ import java.util.Date;
/*  25:    */ 
/*  26:    */ public final class SelfSignedCertificate
/*  27:    */ {
/*  28: 61 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SelfSignedCertificate.class);
/*  29: 64 */   private static final Date DEFAULT_NOT_BEFORE = new Date(SystemPropertyUtil.getLong("io.netty.selfSignedCertificate.defaultNotBefore", 
/*  30: 65 */     System.currentTimeMillis() - 31536000000L));
/*  31: 67 */   private static final Date DEFAULT_NOT_AFTER = new Date(SystemPropertyUtil.getLong("io.netty.selfSignedCertificate.defaultNotAfter", 253402300799000L));
/*  32:    */   private final File certificate;
/*  33:    */   private final File privateKey;
/*  34:    */   private final X509Certificate cert;
/*  35:    */   private final PrivateKey key;
/*  36:    */   
/*  37:    */   public SelfSignedCertificate()
/*  38:    */     throws CertificateException
/*  39:    */   {
/*  40: 79 */     this(DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public SelfSignedCertificate(Date notBefore, Date notAfter)
/*  44:    */     throws CertificateException
/*  45:    */   {
/*  46: 88 */     this("example.com", notBefore, notAfter);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public SelfSignedCertificate(String fqdn)
/*  50:    */     throws CertificateException
/*  51:    */   {
/*  52: 97 */     this(fqdn, DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public SelfSignedCertificate(String fqdn, Date notBefore, Date notAfter)
/*  56:    */     throws CertificateException
/*  57:    */   {
/*  58:110 */     this(fqdn, ThreadLocalInsecureRandom.current(), 1024, notBefore, notAfter);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public SelfSignedCertificate(String fqdn, SecureRandom random, int bits)
/*  62:    */     throws CertificateException
/*  63:    */   {
/*  64:121 */     this(fqdn, random, bits, DEFAULT_NOT_BEFORE, DEFAULT_NOT_AFTER);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public SelfSignedCertificate(String fqdn, SecureRandom random, int bits, Date notBefore, Date notAfter)
/*  68:    */     throws CertificateException
/*  69:    */   {
/*  70:    */     try
/*  71:    */     {
/*  72:138 */       KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
/*  73:139 */       keyGen.initialize(bits, random);
/*  74:140 */       keypair = keyGen.generateKeyPair();
/*  75:    */     }
/*  76:    */     catch (NoSuchAlgorithmException e)
/*  77:    */     {
/*  78:    */       KeyPair keypair;
/*  79:143 */       throw new Error(e);
/*  80:    */     }
/*  81:    */     KeyPair keypair;
/*  82:    */     try
/*  83:    */     {
/*  84:149 */       paths = OpenJdkSelfSignedCertGenerator.generate(fqdn, keypair, random, notBefore, notAfter);
/*  85:    */     }
/*  86:    */     catch (Throwable t)
/*  87:    */     {
/*  88:    */       String[] paths;
/*  89:151 */       logger.debug("Failed to generate a self-signed X.509 certificate using sun.security.x509:", t);
/*  90:    */       try
/*  91:    */       {
/*  92:154 */         paths = BouncyCastleSelfSignedCertGenerator.generate(fqdn, keypair, random, notBefore, notAfter);
/*  93:    */       }
/*  94:    */       catch (Throwable t2)
/*  95:    */       {
/*  96:    */         String[] paths;
/*  97:156 */         logger.debug("Failed to generate a self-signed X.509 certificate using Bouncy Castle:", t2);
/*  98:157 */         throw new CertificateException("No provider succeeded to generate a self-signed certificate. See debug log for the root cause.", t2);
/*  99:    */       }
/* 100:    */     }
/* 101:    */     String[] paths;
/* 102:164 */     this.certificate = new File(paths[0]);
/* 103:165 */     this.privateKey = new File(paths[1]);
/* 104:166 */     this.key = keypair.getPrivate();
/* 105:167 */     FileInputStream certificateInput = null;
/* 106:    */     try
/* 107:    */     {
/* 108:169 */       certificateInput = new FileInputStream(this.certificate);
/* 109:170 */       this.cert = ((X509Certificate)CertificateFactory.getInstance("X509").generateCertificate(certificateInput)); return;
/* 110:    */     }
/* 111:    */     catch (Exception e)
/* 112:    */     {
/* 113:172 */       throw new CertificateEncodingException(e);
/* 114:    */     }
/* 115:    */     finally
/* 116:    */     {
/* 117:174 */       if (certificateInput != null) {
/* 118:    */         try
/* 119:    */         {
/* 120:176 */           certificateInput.close();
/* 121:    */         }
/* 122:    */         catch (IOException e)
/* 123:    */         {
/* 124:178 */           logger.warn("Failed to close a file: " + this.certificate, e);
/* 125:    */         }
/* 126:    */       }
/* 127:    */     }
/* 128:    */   }
/* 129:    */   
/* 130:    */   public File certificate()
/* 131:    */   {
/* 132:188 */     return this.certificate;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public File privateKey()
/* 136:    */   {
/* 137:195 */     return this.privateKey;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public X509Certificate cert()
/* 141:    */   {
/* 142:202 */     return this.cert;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public PrivateKey key()
/* 146:    */   {
/* 147:209 */     return this.key;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void delete()
/* 151:    */   {
/* 152:216 */     safeDelete(this.certificate);
/* 153:217 */     safeDelete(this.privateKey);
/* 154:    */   }
/* 155:    */   
/* 156:    */   static String[] newSelfSignedCertificate(String fqdn, PrivateKey key, X509Certificate cert)
/* 157:    */     throws IOException, CertificateEncodingException
/* 158:    */   {
/* 159:223 */     ByteBuf wrappedBuf = Unpooled.wrappedBuffer(key.getEncoded());
/* 160:    */     try
/* 161:    */     {
/* 162:227 */       ByteBuf encodedBuf = Base64.encode(wrappedBuf, true);
/* 163:    */       String keyText;
/* 164:    */       try
/* 165:    */       {
/* 166:230 */         keyText = "-----BEGIN PRIVATE KEY-----\n" + encodedBuf.toString(CharsetUtil.US_ASCII) + "\n-----END PRIVATE KEY-----\n";
/* 167:    */       }
/* 168:    */       finally {}
/* 169:    */     }
/* 170:    */     finally
/* 171:    */     {
/* 172:    */       String keyText;
/* 173:236 */       wrappedBuf.release();
/* 174:    */     }
/* 175:    */     String keyText;
/* 176:    */     ByteBuf encodedBuf;
/* 177:239 */     File keyFile = File.createTempFile("keyutil_" + fqdn + '_', ".key");
/* 178:240 */     keyFile.deleteOnExit();
/* 179:    */     
/* 180:242 */     Object keyOut = new FileOutputStream(keyFile);
/* 181:    */     try
/* 182:    */     {
/* 183:244 */       ((OutputStream)keyOut).write(keyText.getBytes(CharsetUtil.US_ASCII));
/* 184:245 */       ((OutputStream)keyOut).close();
/* 185:246 */       keyOut = null;
/* 186:    */     }
/* 187:    */     finally
/* 188:    */     {
/* 189:248 */       if (keyOut != null)
/* 190:    */       {
/* 191:249 */         safeClose(keyFile, (OutputStream)keyOut);
/* 192:250 */         safeDelete(keyFile);
/* 193:    */       }
/* 194:    */     }
/* 195:254 */     wrappedBuf = Unpooled.wrappedBuffer(cert.getEncoded());
/* 196:    */     try
/* 197:    */     {
/* 198:257 */       encodedBuf = Base64.encode(wrappedBuf, true);
/* 199:    */       String certText;
/* 200:    */       try
/* 201:    */       {
/* 202:261 */         certText = "-----BEGIN CERTIFICATE-----\n" + encodedBuf.toString(CharsetUtil.US_ASCII) + "\n-----END CERTIFICATE-----\n";
/* 203:    */       }
/* 204:    */       finally {}
/* 205:    */     }
/* 206:    */     finally
/* 207:    */     {
/* 208:    */       String certText;
/* 209:267 */       wrappedBuf.release();
/* 210:    */     }
/* 211:    */     String certText;
/* 212:270 */     File certFile = File.createTempFile("keyutil_" + fqdn + '_', ".crt");
/* 213:271 */     certFile.deleteOnExit();
/* 214:    */     
/* 215:273 */     Object certOut = new FileOutputStream(certFile);
/* 216:    */     try
/* 217:    */     {
/* 218:275 */       ((OutputStream)certOut).write(certText.getBytes(CharsetUtil.US_ASCII));
/* 219:276 */       ((OutputStream)certOut).close();
/* 220:277 */       certOut = null;
/* 221:    */     }
/* 222:    */     finally
/* 223:    */     {
/* 224:279 */       if (certOut != null)
/* 225:    */       {
/* 226:280 */         safeClose(certFile, (OutputStream)certOut);
/* 227:281 */         safeDelete(certFile);
/* 228:282 */         safeDelete(keyFile);
/* 229:    */       }
/* 230:    */     }
/* 231:286 */     return new String[] { certFile.getPath(), keyFile.getPath() };
/* 232:    */   }
/* 233:    */   
/* 234:    */   private static void safeDelete(File certFile)
/* 235:    */   {
/* 236:290 */     if (!certFile.delete()) {
/* 237:291 */       logger.warn("Failed to delete a file: " + certFile);
/* 238:    */     }
/* 239:    */   }
/* 240:    */   
/* 241:    */   private static void safeClose(File keyFile, OutputStream keyOut)
/* 242:    */   {
/* 243:    */     try
/* 244:    */     {
/* 245:297 */       keyOut.close();
/* 246:    */     }
/* 247:    */     catch (IOException e)
/* 248:    */     {
/* 249:299 */       logger.warn("Failed to close a file: " + keyFile, e);
/* 250:    */     }
/* 251:    */   }
/* 252:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.SelfSignedCertificate
 * JD-Core Version:    0.7.0.1
 */