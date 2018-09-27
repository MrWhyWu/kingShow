/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.handler.ssl.util.SelfSignedCertificate;
/*   5:    */ import io.netty.internal.tcnative.Buffer;
/*   6:    */ import io.netty.internal.tcnative.Library;
/*   7:    */ import io.netty.internal.tcnative.SSL;
/*   8:    */ import io.netty.internal.tcnative.SSLContext;
/*   9:    */ import io.netty.util.ReferenceCountUtil;
/*  10:    */ import io.netty.util.ReferenceCounted;
/*  11:    */ import io.netty.util.internal.NativeLibraryLoader;
/*  12:    */ import io.netty.util.internal.PlatformDependent;
/*  13:    */ import io.netty.util.internal.SystemPropertyUtil;
/*  14:    */ import io.netty.util.internal.logging.InternalLogger;
/*  15:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  16:    */ import java.security.AccessController;
/*  17:    */ import java.security.PrivilegedAction;
/*  18:    */ import java.security.cert.X509Certificate;
/*  19:    */ import java.util.ArrayList;
/*  20:    */ import java.util.Arrays;
/*  21:    */ import java.util.Collections;
/*  22:    */ import java.util.LinkedHashSet;
/*  23:    */ import java.util.List;
/*  24:    */ import java.util.Set;
/*  25:    */ 
/*  26:    */ public final class OpenSsl
/*  27:    */ {
/*  28:    */   private static final InternalLogger logger;
/*  29:    */   private static final Throwable UNAVAILABILITY_CAUSE;
/*  30:    */   static final List<String> DEFAULT_CIPHERS;
/*  31:    */   static final Set<String> AVAILABLE_CIPHER_SUITES;
/*  32:    */   private static final Set<String> AVAILABLE_OPENSSL_CIPHER_SUITES;
/*  33:    */   private static final Set<String> AVAILABLE_JAVA_CIPHER_SUITES;
/*  34:    */   private static final boolean SUPPORTS_KEYMANAGER_FACTORY;
/*  35:    */   private static final boolean SUPPORTS_HOSTNAME_VALIDATION;
/*  36:    */   private static final boolean USE_KEYMANAGER_FACTORY;
/*  37:    */   private static final boolean SUPPORTS_OCSP;
/*  38:    */   static final Set<String> SUPPORTED_PROTOCOLS_SET;
/*  39:    */   
/*  40:    */   static
/*  41:    */   {
/*  42: 58 */     logger = InternalLoggerFactory.getInstance(OpenSsl.class);
/*  43:    */     
/*  44:    */ 
/*  45:    */ 
/*  46:    */ 
/*  47:    */ 
/*  48:    */ 
/*  49:    */ 
/*  50:    */ 
/*  51:    */ 
/*  52:    */ 
/*  53:    */ 
/*  54:    */ 
/*  55:    */ 
/*  56:    */ 
/*  57: 73 */     Throwable cause = null;
/*  58:    */     try
/*  59:    */     {
/*  60: 77 */       Class.forName("io.netty.internal.tcnative.SSL", false, OpenSsl.class.getClassLoader());
/*  61:    */     }
/*  62:    */     catch (ClassNotFoundException t)
/*  63:    */     {
/*  64: 79 */       cause = t;
/*  65: 80 */       logger.debug("netty-tcnative not in the classpath; " + OpenSslEngine.class
/*  66:    */       
/*  67: 82 */         .getSimpleName() + " will be unavailable.");
/*  68:    */     }
/*  69: 86 */     if (cause == null)
/*  70:    */     {
/*  71:    */       try
/*  72:    */       {
/*  73: 89 */         loadTcNative();
/*  74:    */       }
/*  75:    */       catch (Throwable t)
/*  76:    */       {
/*  77: 91 */         cause = t;
/*  78: 92 */         logger.debug("Failed to load netty-tcnative; " + OpenSslEngine.class
/*  79:    */         
/*  80: 94 */           .getSimpleName() + " will be unavailable, unless the application has already loaded the symbols by some other means. See http://netty.io/wiki/forked-tomcat-native.html for more information.", t);
/*  81:    */       }
/*  82:    */       try
/*  83:    */       {
/*  84:100 */         initializeTcNative();
/*  85:    */         
/*  86:    */ 
/*  87:    */ 
/*  88:    */ 
/*  89:105 */         cause = null;
/*  90:    */       }
/*  91:    */       catch (Throwable t)
/*  92:    */       {
/*  93:107 */         if (cause == null) {
/*  94:108 */           cause = t;
/*  95:    */         }
/*  96:110 */         logger.debug("Failed to initialize netty-tcnative; " + OpenSslEngine.class
/*  97:    */         
/*  98:112 */           .getSimpleName() + " will be unavailable. See http://netty.io/wiki/forked-tomcat-native.html for more information.", t);
/*  99:    */       }
/* 100:    */     }
/* 101:117 */     UNAVAILABILITY_CAUSE = cause;
/* 102:119 */     if (cause == null)
/* 103:    */     {
/* 104:120 */       logger.debug("netty-tcnative using native library: {}", SSL.versionString());
/* 105:    */       
/* 106:122 */       List<String> defaultCiphers = new ArrayList();
/* 107:123 */       Set<String> availableOpenSslCipherSuites = new LinkedHashSet(128);
/* 108:124 */       boolean supportsKeyManagerFactory = false;
/* 109:125 */       boolean useKeyManagerFactory = false;
/* 110:126 */       boolean supportsHostNameValidation = false;
/* 111:    */       try
/* 112:    */       {
/* 113:128 */         long sslCtx = SSLContext.make(31, 1);
/* 114:129 */         long certBio = 0L;
/* 115:130 */         SelfSignedCertificate cert = null;
/* 116:    */         try
/* 117:    */         {
/* 118:132 */           SSLContext.setCipherSuite(sslCtx, "ALL");
/* 119:133 */           long ssl = SSL.newSSL(sslCtx, true);
/* 120:    */           try
/* 121:    */           {
/* 122:135 */             for (String c : SSL.getCiphers(ssl)) {
/* 123:137 */               if ((c != null) && (!c.isEmpty()) && (!availableOpenSslCipherSuites.contains(c))) {
/* 124:140 */                 availableOpenSslCipherSuites.add(c);
/* 125:    */               }
/* 126:    */             }
/* 127:    */             try
/* 128:    */             {
/* 129:144 */               SSL.setHostNameValidation(ssl, 0, "netty.io");
/* 130:145 */               supportsHostNameValidation = true;
/* 131:    */             }
/* 132:    */             catch (Throwable ignore)
/* 133:    */             {
/* 134:147 */               logger.debug("Hostname Verification not supported.");
/* 135:    */             }
/* 136:    */             try
/* 137:    */             {
/* 138:150 */               cert = new SelfSignedCertificate();
/* 139:151 */               certBio = ReferenceCountedOpenSslContext.toBIO(new X509Certificate[] { cert.cert() });
/* 140:152 */               SSL.setCertificateChainBio(ssl, certBio, false);
/* 141:153 */               supportsKeyManagerFactory = true;
/* 142:    */               try
/* 143:    */               {
/* 144:155 */                 useKeyManagerFactory = ((Boolean)AccessController.doPrivileged(new PrivilegedAction()
/* 145:    */                 {
/* 146:    */                   public Boolean run()
/* 147:    */                   {
/* 148:158 */                     return Boolean.valueOf(SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.useKeyManagerFactory", true));
/* 149:    */                   }
/* 150:    */                 })).booleanValue();
/* 151:    */               }
/* 152:    */               catch (Throwable ignore)
/* 153:    */               {
/* 154:163 */                 logger.debug("Failed to get useKeyManagerFactory system property.");
/* 155:    */               }
/* 156:    */             }
/* 157:    */             catch (Throwable ignore)
/* 158:    */             {
/* 159:166 */               logger.debug("KeyManagerFactory not supported.");
/* 160:    */             }
/* 161:    */           }
/* 162:    */           finally
/* 163:    */           {
/* 164:169 */             SSL.freeSSL(ssl);
/* 165:170 */             if (certBio != 0L) {
/* 166:171 */               SSL.freeBIO(certBio);
/* 167:    */             }
/* 168:173 */             if (cert != null) {
/* 169:174 */               cert.delete();
/* 170:    */             }
/* 171:    */           }
/* 172:    */         }
/* 173:    */         finally
/* 174:    */         {
/* 175:178 */           SSLContext.free(sslCtx);
/* 176:    */         }
/* 177:    */       }
/* 178:    */       catch (Exception e)
/* 179:    */       {
/* 180:181 */         logger.warn("Failed to get the list of available OpenSSL cipher suites.", e);
/* 181:    */       }
/* 182:183 */       AVAILABLE_OPENSSL_CIPHER_SUITES = Collections.unmodifiableSet(availableOpenSslCipherSuites);
/* 183:    */       
/* 184:185 */       Set<String> availableJavaCipherSuites = new LinkedHashSet(AVAILABLE_OPENSSL_CIPHER_SUITES.size() * 2);
/* 185:186 */       for (String cipher : AVAILABLE_OPENSSL_CIPHER_SUITES)
/* 186:    */       {
/* 187:188 */         availableJavaCipherSuites.add(CipherSuiteConverter.toJava(cipher, "TLS"));
/* 188:189 */         availableJavaCipherSuites.add(CipherSuiteConverter.toJava(cipher, "SSL"));
/* 189:    */       }
/* 190:192 */       SslUtils.addIfSupported(availableJavaCipherSuites, defaultCiphers, SslUtils.DEFAULT_CIPHER_SUITES);
/* 191:193 */       SslUtils.useFallbackCiphersIfDefaultIsEmpty(defaultCiphers, availableJavaCipherSuites);
/* 192:194 */       DEFAULT_CIPHERS = Collections.unmodifiableList(defaultCiphers);
/* 193:    */       
/* 194:196 */       AVAILABLE_JAVA_CIPHER_SUITES = Collections.unmodifiableSet(availableJavaCipherSuites);
/* 195:    */       
/* 196:    */ 
/* 197:199 */       Object availableCipherSuites = new LinkedHashSet(AVAILABLE_OPENSSL_CIPHER_SUITES.size() + AVAILABLE_JAVA_CIPHER_SUITES.size());
/* 198:200 */       ((Set)availableCipherSuites).addAll(AVAILABLE_OPENSSL_CIPHER_SUITES);
/* 199:201 */       ((Set)availableCipherSuites).addAll(AVAILABLE_JAVA_CIPHER_SUITES);
/* 200:    */       
/* 201:203 */       AVAILABLE_CIPHER_SUITES = (Set)availableCipherSuites;
/* 202:204 */       SUPPORTS_KEYMANAGER_FACTORY = supportsKeyManagerFactory;
/* 203:205 */       SUPPORTS_HOSTNAME_VALIDATION = supportsHostNameValidation;
/* 204:206 */       USE_KEYMANAGER_FACTORY = useKeyManagerFactory;
/* 205:    */       
/* 206:208 */       Set<String> protocols = new LinkedHashSet(6);
/* 207:    */       
/* 208:210 */       protocols.add("SSLv2Hello");
/* 209:211 */       if (doesSupportProtocol(1)) {
/* 210:212 */         protocols.add("SSLv2");
/* 211:    */       }
/* 212:214 */       if (doesSupportProtocol(2)) {
/* 213:215 */         protocols.add("SSLv3");
/* 214:    */       }
/* 215:217 */       if (doesSupportProtocol(4)) {
/* 216:218 */         protocols.add("TLSv1");
/* 217:    */       }
/* 218:220 */       if (doesSupportProtocol(8)) {
/* 219:221 */         protocols.add("TLSv1.1");
/* 220:    */       }
/* 221:223 */       if (doesSupportProtocol(16)) {
/* 222:224 */         protocols.add("TLSv1.2");
/* 223:    */       }
/* 224:227 */       SUPPORTED_PROTOCOLS_SET = Collections.unmodifiableSet(protocols);
/* 225:228 */       SUPPORTS_OCSP = doesSupportOcsp();
/* 226:230 */       if (logger.isDebugEnabled())
/* 227:    */       {
/* 228:231 */         logger.debug("Supported protocols (OpenSSL): {} ", Arrays.asList(new Set[] { SUPPORTED_PROTOCOLS_SET }));
/* 229:232 */         logger.debug("Default cipher suites (OpenSSL): {}", DEFAULT_CIPHERS);
/* 230:    */       }
/* 231:    */     }
/* 232:    */     else
/* 233:    */     {
/* 234:235 */       DEFAULT_CIPHERS = Collections.emptyList();
/* 235:236 */       AVAILABLE_OPENSSL_CIPHER_SUITES = Collections.emptySet();
/* 236:237 */       AVAILABLE_JAVA_CIPHER_SUITES = Collections.emptySet();
/* 237:238 */       AVAILABLE_CIPHER_SUITES = Collections.emptySet();
/* 238:239 */       SUPPORTS_KEYMANAGER_FACTORY = false;
/* 239:240 */       SUPPORTS_HOSTNAME_VALIDATION = false;
/* 240:241 */       USE_KEYMANAGER_FACTORY = false;
/* 241:242 */       SUPPORTED_PROTOCOLS_SET = Collections.emptySet();
/* 242:243 */       SUPPORTS_OCSP = false;
/* 243:    */     }
/* 244:    */   }
/* 245:    */   
/* 246:    */   private static boolean doesSupportOcsp()
/* 247:    */   {
/* 248:248 */     boolean supportsOcsp = false;
/* 249:249 */     if (version() >= 268443648L)
/* 250:    */     {
/* 251:250 */       long sslCtx = -1L;
/* 252:    */       try
/* 253:    */       {
/* 254:252 */         sslCtx = SSLContext.make(16, 1);
/* 255:253 */         SSLContext.enableOcsp(sslCtx, false);
/* 256:254 */         supportsOcsp = true;
/* 257:258 */         if (sslCtx != -1L) {
/* 258:259 */           SSLContext.free(sslCtx);
/* 259:    */         }
/* 260:    */       }
/* 261:    */       catch (Exception localException)
/* 262:    */       {
/* 263:258 */         if (sslCtx != -1L) {
/* 264:259 */           SSLContext.free(sslCtx);
/* 265:    */         }
/* 266:    */       }
/* 267:    */       finally
/* 268:    */       {
/* 269:258 */         if (sslCtx != -1L) {
/* 270:259 */           SSLContext.free(sslCtx);
/* 271:    */         }
/* 272:    */       }
/* 273:    */     }
/* 274:263 */     return supportsOcsp;
/* 275:    */   }
/* 276:    */   
/* 277:    */   private static boolean doesSupportProtocol(int protocol)
/* 278:    */   {
/* 279:266 */     long sslCtx = -1L;
/* 280:    */     try
/* 281:    */     {
/* 282:268 */       sslCtx = SSLContext.make(protocol, 2);
/* 283:269 */       return true;
/* 284:    */     }
/* 285:    */     catch (Exception ignore)
/* 286:    */     {
/* 287:271 */       return false;
/* 288:    */     }
/* 289:    */     finally
/* 290:    */     {
/* 291:273 */       if (sslCtx != -1L) {
/* 292:274 */         SSLContext.free(sslCtx);
/* 293:    */       }
/* 294:    */     }
/* 295:    */   }
/* 296:    */   
/* 297:    */   public static boolean isAvailable()
/* 298:    */   {
/* 299:285 */     return UNAVAILABILITY_CAUSE == null;
/* 300:    */   }
/* 301:    */   
/* 302:    */   public static boolean isAlpnSupported()
/* 303:    */   {
/* 304:293 */     return version() >= 268443648L;
/* 305:    */   }
/* 306:    */   
/* 307:    */   public static boolean isOcspSupported()
/* 308:    */   {
/* 309:300 */     return SUPPORTS_OCSP;
/* 310:    */   }
/* 311:    */   
/* 312:    */   public static int version()
/* 313:    */   {
/* 314:308 */     return isAvailable() ? SSL.version() : -1;
/* 315:    */   }
/* 316:    */   
/* 317:    */   public static String versionString()
/* 318:    */   {
/* 319:316 */     return isAvailable() ? SSL.versionString() : null;
/* 320:    */   }
/* 321:    */   
/* 322:    */   public static void ensureAvailability()
/* 323:    */   {
/* 324:326 */     if (UNAVAILABILITY_CAUSE != null) {
/* 325:328 */       throw ((Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(UNAVAILABILITY_CAUSE));
/* 326:    */     }
/* 327:    */   }
/* 328:    */   
/* 329:    */   public static Throwable unavailabilityCause()
/* 330:    */   {
/* 331:339 */     return UNAVAILABILITY_CAUSE;
/* 332:    */   }
/* 333:    */   
/* 334:    */   @Deprecated
/* 335:    */   public static Set<String> availableCipherSuites()
/* 336:    */   {
/* 337:347 */     return availableOpenSslCipherSuites();
/* 338:    */   }
/* 339:    */   
/* 340:    */   public static Set<String> availableOpenSslCipherSuites()
/* 341:    */   {
/* 342:355 */     return AVAILABLE_OPENSSL_CIPHER_SUITES;
/* 343:    */   }
/* 344:    */   
/* 345:    */   public static Set<String> availableJavaCipherSuites()
/* 346:    */   {
/* 347:363 */     return AVAILABLE_JAVA_CIPHER_SUITES;
/* 348:    */   }
/* 349:    */   
/* 350:    */   public static boolean isCipherSuiteAvailable(String cipherSuite)
/* 351:    */   {
/* 352:371 */     String converted = CipherSuiteConverter.toOpenSsl(cipherSuite);
/* 353:372 */     if (converted != null) {
/* 354:373 */       cipherSuite = converted;
/* 355:    */     }
/* 356:375 */     return AVAILABLE_OPENSSL_CIPHER_SUITES.contains(cipherSuite);
/* 357:    */   }
/* 358:    */   
/* 359:    */   public static boolean supportsKeyManagerFactory()
/* 360:    */   {
/* 361:382 */     return SUPPORTS_KEYMANAGER_FACTORY;
/* 362:    */   }
/* 363:    */   
/* 364:    */   public static boolean supportsHostnameValidation()
/* 365:    */   {
/* 366:390 */     return SUPPORTS_HOSTNAME_VALIDATION;
/* 367:    */   }
/* 368:    */   
/* 369:    */   static boolean useKeyManagerFactory()
/* 370:    */   {
/* 371:394 */     return USE_KEYMANAGER_FACTORY;
/* 372:    */   }
/* 373:    */   
/* 374:    */   static long memoryAddress(ByteBuf buf)
/* 375:    */   {
/* 376:398 */     assert (buf.isDirect());
/* 377:399 */     return buf.hasMemoryAddress() ? buf.memoryAddress() : Buffer.address(buf.nioBuffer());
/* 378:    */   }
/* 379:    */   
/* 380:    */   private static void loadTcNative()
/* 381:    */     throws Exception
/* 382:    */   {
/* 383:405 */     String os = PlatformDependent.normalizedOs();
/* 384:406 */     String arch = PlatformDependent.normalizedArch();
/* 385:    */     
/* 386:408 */     Set<String> libNames = new LinkedHashSet(4);
/* 387:409 */     String staticLibName = "netty_tcnative";
/* 388:    */     
/* 389:    */ 
/* 390:    */ 
/* 391:413 */     libNames.add(staticLibName + "_" + os + '_' + arch);
/* 392:414 */     if ("linux".equalsIgnoreCase(os)) {
/* 393:416 */       libNames.add(staticLibName + "_" + os + '_' + arch + "_fedora");
/* 394:    */     }
/* 395:418 */     libNames.add(staticLibName + "_" + arch);
/* 396:419 */     libNames.add(staticLibName);
/* 397:    */     
/* 398:421 */     NativeLibraryLoader.loadFirstAvailable(SSL.class.getClassLoader(), 
/* 399:422 */       (String[])libNames.toArray(new String[libNames.size()]));
/* 400:    */   }
/* 401:    */   
/* 402:    */   private static boolean initializeTcNative()
/* 403:    */     throws Exception
/* 404:    */   {
/* 405:426 */     return Library.initialize();
/* 406:    */   }
/* 407:    */   
/* 408:    */   static void releaseIfNeeded(ReferenceCounted counted)
/* 409:    */   {
/* 410:430 */     if (counted.refCnt() > 0) {
/* 411:431 */       ReferenceCountUtil.safeRelease(counted);
/* 412:    */     }
/* 413:    */   }
/* 414:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSsl
 * JD-Core Version:    0.7.0.1
 */