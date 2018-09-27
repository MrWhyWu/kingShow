/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.util.HashMap;
/*   7:    */ import java.util.Map;
/*   8:    */ import java.util.concurrent.ConcurrentMap;
/*   9:    */ import java.util.regex.Matcher;
/*  10:    */ import java.util.regex.Pattern;
/*  11:    */ 
/*  12:    */ final class CipherSuiteConverter
/*  13:    */ {
/*  14: 36 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(CipherSuiteConverter.class);
/*  15: 52 */   private static final Pattern JAVA_CIPHERSUITE_PATTERN = Pattern.compile("^(?:TLS|SSL)_((?:(?!_WITH_).)+)_WITH_(.*)_(.*)$");
/*  16: 68 */   private static final Pattern OPENSSL_CIPHERSUITE_PATTERN = Pattern.compile("^(?:((?:(?:EXP-)?(?:(?:DHE|EDH|ECDH|ECDHE|SRP|RSA)-(?:DSS|RSA|ECDSA|PSK)|(?:ADH|AECDH|KRB5|PSK|SRP)))|EXP)-)?(.*)-(.*)$");
/*  17: 80 */   private static final Pattern JAVA_AES_CBC_PATTERN = Pattern.compile("^(AES)_([0-9]+)_CBC$");
/*  18: 81 */   private static final Pattern JAVA_AES_PATTERN = Pattern.compile("^(AES)_([0-9]+)_(.*)$");
/*  19: 82 */   private static final Pattern OPENSSL_AES_CBC_PATTERN = Pattern.compile("^(AES)([0-9]+)$");
/*  20: 83 */   private static final Pattern OPENSSL_AES_PATTERN = Pattern.compile("^(AES)([0-9]+)-(.*)$");
/*  21: 89 */   private static final ConcurrentMap<String, String> j2o = PlatformDependent.newConcurrentHashMap();
/*  22: 96 */   private static final ConcurrentMap<String, Map<String, String>> o2j = PlatformDependent.newConcurrentHashMap();
/*  23:    */   
/*  24:    */   static void clearCache()
/*  25:    */   {
/*  26:102 */     j2o.clear();
/*  27:103 */     o2j.clear();
/*  28:    */   }
/*  29:    */   
/*  30:    */   static boolean isJ2OCached(String key, String value)
/*  31:    */   {
/*  32:110 */     return value.equals(j2o.get(key));
/*  33:    */   }
/*  34:    */   
/*  35:    */   static boolean isO2JCached(String key, String protocol, String value)
/*  36:    */   {
/*  37:117 */     Map<String, String> p2j = (Map)o2j.get(key);
/*  38:118 */     if (p2j == null) {
/*  39:119 */       return false;
/*  40:    */     }
/*  41:121 */     return value.equals(p2j.get(protocol));
/*  42:    */   }
/*  43:    */   
/*  44:    */   static String toOpenSsl(Iterable<String> javaCipherSuites)
/*  45:    */   {
/*  46:129 */     StringBuilder buf = new StringBuilder();
/*  47:130 */     for (String c : javaCipherSuites)
/*  48:    */     {
/*  49:131 */       if (c == null) {
/*  50:    */         break;
/*  51:    */       }
/*  52:135 */       String converted = toOpenSsl(c);
/*  53:136 */       if (converted != null) {
/*  54:137 */         c = converted;
/*  55:    */       }
/*  56:140 */       buf.append(c);
/*  57:141 */       buf.append(':');
/*  58:    */     }
/*  59:144 */     if (buf.length() > 0)
/*  60:    */     {
/*  61:145 */       buf.setLength(buf.length() - 1);
/*  62:146 */       return buf.toString();
/*  63:    */     }
/*  64:148 */     return "";
/*  65:    */   }
/*  66:    */   
/*  67:    */   static String toOpenSsl(String javaCipherSuite)
/*  68:    */   {
/*  69:158 */     String converted = (String)j2o.get(javaCipherSuite);
/*  70:159 */     if (converted != null) {
/*  71:160 */       return converted;
/*  72:    */     }
/*  73:162 */     return cacheFromJava(javaCipherSuite);
/*  74:    */   }
/*  75:    */   
/*  76:    */   private static String cacheFromJava(String javaCipherSuite)
/*  77:    */   {
/*  78:167 */     String openSslCipherSuite = toOpenSslUncached(javaCipherSuite);
/*  79:168 */     if (openSslCipherSuite == null) {
/*  80:169 */       return null;
/*  81:    */     }
/*  82:173 */     j2o.putIfAbsent(javaCipherSuite, openSslCipherSuite);
/*  83:    */     
/*  84:    */ 
/*  85:176 */     String javaCipherSuiteSuffix = javaCipherSuite.substring(4);
/*  86:177 */     Map<String, String> p2j = new HashMap(4);
/*  87:178 */     p2j.put("", javaCipherSuiteSuffix);
/*  88:179 */     p2j.put("SSL", "SSL_" + javaCipherSuiteSuffix);
/*  89:180 */     p2j.put("TLS", "TLS_" + javaCipherSuiteSuffix);
/*  90:181 */     o2j.put(openSslCipherSuite, p2j);
/*  91:    */     
/*  92:183 */     logger.debug("Cipher suite mapping: {} => {}", javaCipherSuite, openSslCipherSuite);
/*  93:    */     
/*  94:185 */     return openSslCipherSuite;
/*  95:    */   }
/*  96:    */   
/*  97:    */   static String toOpenSslUncached(String javaCipherSuite)
/*  98:    */   {
/*  99:189 */     Matcher m = JAVA_CIPHERSUITE_PATTERN.matcher(javaCipherSuite);
/* 100:190 */     if (!m.matches()) {
/* 101:191 */       return null;
/* 102:    */     }
/* 103:194 */     String handshakeAlgo = toOpenSslHandshakeAlgo(m.group(1));
/* 104:195 */     String bulkCipher = toOpenSslBulkCipher(m.group(2));
/* 105:196 */     String hmacAlgo = toOpenSslHmacAlgo(m.group(3));
/* 106:197 */     if (handshakeAlgo.isEmpty()) {
/* 107:198 */       return bulkCipher + '-' + hmacAlgo;
/* 108:    */     }
/* 109:199 */     if (bulkCipher.contains("CHACHA20")) {
/* 110:200 */       return handshakeAlgo + '-' + bulkCipher;
/* 111:    */     }
/* 112:202 */     return handshakeAlgo + '-' + bulkCipher + '-' + hmacAlgo;
/* 113:    */   }
/* 114:    */   
/* 115:    */   private static String toOpenSslHandshakeAlgo(String handshakeAlgo)
/* 116:    */   {
/* 117:207 */     boolean export = handshakeAlgo.endsWith("_EXPORT");
/* 118:208 */     if (export) {
/* 119:209 */       handshakeAlgo = handshakeAlgo.substring(0, handshakeAlgo.length() - 7);
/* 120:    */     }
/* 121:212 */     if ("RSA".equals(handshakeAlgo)) {
/* 122:213 */       handshakeAlgo = "";
/* 123:214 */     } else if (handshakeAlgo.endsWith("_anon")) {
/* 124:215 */       handshakeAlgo = 'A' + handshakeAlgo.substring(0, handshakeAlgo.length() - 5);
/* 125:    */     }
/* 126:218 */     if (export) {
/* 127:219 */       if (handshakeAlgo.isEmpty()) {
/* 128:220 */         handshakeAlgo = "EXP";
/* 129:    */       } else {
/* 130:222 */         handshakeAlgo = "EXP-" + handshakeAlgo;
/* 131:    */       }
/* 132:    */     }
/* 133:226 */     return handshakeAlgo.replace('_', '-');
/* 134:    */   }
/* 135:    */   
/* 136:    */   private static String toOpenSslBulkCipher(String bulkCipher)
/* 137:    */   {
/* 138:230 */     if (bulkCipher.startsWith("AES_"))
/* 139:    */     {
/* 140:231 */       Matcher m = JAVA_AES_CBC_PATTERN.matcher(bulkCipher);
/* 141:232 */       if (m.matches()) {
/* 142:233 */         return m.replaceFirst("$1$2");
/* 143:    */       }
/* 144:236 */       m = JAVA_AES_PATTERN.matcher(bulkCipher);
/* 145:237 */       if (m.matches()) {
/* 146:238 */         return m.replaceFirst("$1$2-$3");
/* 147:    */       }
/* 148:    */     }
/* 149:242 */     if ("3DES_EDE_CBC".equals(bulkCipher)) {
/* 150:243 */       return "DES-CBC3";
/* 151:    */     }
/* 152:246 */     if (("RC4_128".equals(bulkCipher)) || ("RC4_40".equals(bulkCipher))) {
/* 153:247 */       return "RC4";
/* 154:    */     }
/* 155:250 */     if (("DES40_CBC".equals(bulkCipher)) || ("DES_CBC_40".equals(bulkCipher))) {
/* 156:251 */       return "DES-CBC";
/* 157:    */     }
/* 158:254 */     if ("RC2_CBC_40".equals(bulkCipher)) {
/* 159:255 */       return "RC2-CBC";
/* 160:    */     }
/* 161:258 */     return bulkCipher.replace('_', '-');
/* 162:    */   }
/* 163:    */   
/* 164:    */   private static String toOpenSslHmacAlgo(String hmacAlgo)
/* 165:    */   {
/* 166:268 */     return hmacAlgo;
/* 167:    */   }
/* 168:    */   
/* 169:    */   static String toJava(String openSslCipherSuite, String protocol)
/* 170:    */   {
/* 171:278 */     Map<String, String> p2j = (Map)o2j.get(openSslCipherSuite);
/* 172:279 */     if (p2j == null)
/* 173:    */     {
/* 174:280 */       p2j = cacheFromOpenSsl(openSslCipherSuite);
/* 175:283 */       if (p2j == null) {
/* 176:284 */         return null;
/* 177:    */       }
/* 178:    */     }
/* 179:288 */     String javaCipherSuite = (String)p2j.get(protocol);
/* 180:289 */     if (javaCipherSuite == null) {
/* 181:290 */       javaCipherSuite = protocol + '_' + (String)p2j.get("");
/* 182:    */     }
/* 183:293 */     return javaCipherSuite;
/* 184:    */   }
/* 185:    */   
/* 186:    */   private static Map<String, String> cacheFromOpenSsl(String openSslCipherSuite)
/* 187:    */   {
/* 188:297 */     String javaCipherSuiteSuffix = toJavaUncached(openSslCipherSuite);
/* 189:298 */     if (javaCipherSuiteSuffix == null) {
/* 190:299 */       return null;
/* 191:    */     }
/* 192:302 */     String javaCipherSuiteSsl = "SSL_" + javaCipherSuiteSuffix;
/* 193:303 */     String javaCipherSuiteTls = "TLS_" + javaCipherSuiteSuffix;
/* 194:    */     
/* 195:    */ 
/* 196:306 */     Map<String, String> p2j = new HashMap(4);
/* 197:307 */     p2j.put("", javaCipherSuiteSuffix);
/* 198:308 */     p2j.put("SSL", javaCipherSuiteSsl);
/* 199:309 */     p2j.put("TLS", javaCipherSuiteTls);
/* 200:310 */     o2j.putIfAbsent(openSslCipherSuite, p2j);
/* 201:    */     
/* 202:    */ 
/* 203:313 */     j2o.putIfAbsent(javaCipherSuiteTls, openSslCipherSuite);
/* 204:314 */     j2o.putIfAbsent(javaCipherSuiteSsl, openSslCipherSuite);
/* 205:    */     
/* 206:316 */     logger.debug("Cipher suite mapping: {} => {}", javaCipherSuiteTls, openSslCipherSuite);
/* 207:317 */     logger.debug("Cipher suite mapping: {} => {}", javaCipherSuiteSsl, openSslCipherSuite);
/* 208:    */     
/* 209:319 */     return p2j;
/* 210:    */   }
/* 211:    */   
/* 212:    */   static String toJavaUncached(String openSslCipherSuite)
/* 213:    */   {
/* 214:323 */     Matcher m = OPENSSL_CIPHERSUITE_PATTERN.matcher(openSslCipherSuite);
/* 215:324 */     if (!m.matches()) {
/* 216:325 */       return null;
/* 217:    */     }
/* 218:328 */     String handshakeAlgo = m.group(1);
/* 219:    */     boolean export;
/* 220:    */     boolean export;
/* 221:330 */     if (handshakeAlgo == null)
/* 222:    */     {
/* 223:331 */       handshakeAlgo = "";
/* 224:332 */       export = false;
/* 225:    */     }
/* 226:    */     else
/* 227:    */     {
/* 228:    */       boolean export;
/* 229:333 */       if (handshakeAlgo.startsWith("EXP-"))
/* 230:    */       {
/* 231:334 */         handshakeAlgo = handshakeAlgo.substring(4);
/* 232:335 */         export = true;
/* 233:    */       }
/* 234:    */       else
/* 235:    */       {
/* 236:    */         boolean export;
/* 237:336 */         if ("EXP".equals(handshakeAlgo))
/* 238:    */         {
/* 239:337 */           handshakeAlgo = "";
/* 240:338 */           export = true;
/* 241:    */         }
/* 242:    */         else
/* 243:    */         {
/* 244:340 */           export = false;
/* 245:    */         }
/* 246:    */       }
/* 247:    */     }
/* 248:343 */     handshakeAlgo = toJavaHandshakeAlgo(handshakeAlgo, export);
/* 249:344 */     String bulkCipher = toJavaBulkCipher(m.group(2), export);
/* 250:345 */     String hmacAlgo = toJavaHmacAlgo(m.group(3));
/* 251:    */     
/* 252:347 */     String javaCipherSuite = handshakeAlgo + "_WITH_" + bulkCipher + '_' + hmacAlgo;
/* 253:    */     
/* 254:    */ 
/* 255:    */ 
/* 256:    */ 
/* 257:352 */     return bulkCipher.contains("CHACHA20") ? javaCipherSuite + "_SHA256" : javaCipherSuite;
/* 258:    */   }
/* 259:    */   
/* 260:    */   private static String toJavaHandshakeAlgo(String handshakeAlgo, boolean export)
/* 261:    */   {
/* 262:356 */     if (handshakeAlgo.isEmpty()) {
/* 263:357 */       handshakeAlgo = "RSA";
/* 264:358 */     } else if ("ADH".equals(handshakeAlgo)) {
/* 265:359 */       handshakeAlgo = "DH_anon";
/* 266:360 */     } else if ("AECDH".equals(handshakeAlgo)) {
/* 267:361 */       handshakeAlgo = "ECDH_anon";
/* 268:    */     }
/* 269:364 */     handshakeAlgo = handshakeAlgo.replace('-', '_');
/* 270:365 */     if (export) {
/* 271:366 */       return handshakeAlgo + "_EXPORT";
/* 272:    */     }
/* 273:368 */     return handshakeAlgo;
/* 274:    */   }
/* 275:    */   
/* 276:    */   private static String toJavaBulkCipher(String bulkCipher, boolean export)
/* 277:    */   {
/* 278:373 */     if (bulkCipher.startsWith("AES"))
/* 279:    */     {
/* 280:374 */       Matcher m = OPENSSL_AES_CBC_PATTERN.matcher(bulkCipher);
/* 281:375 */       if (m.matches()) {
/* 282:376 */         return m.replaceFirst("$1_$2_CBC");
/* 283:    */       }
/* 284:379 */       m = OPENSSL_AES_PATTERN.matcher(bulkCipher);
/* 285:380 */       if (m.matches()) {
/* 286:381 */         return m.replaceFirst("$1_$2_$3");
/* 287:    */       }
/* 288:    */     }
/* 289:385 */     if ("DES-CBC3".equals(bulkCipher)) {
/* 290:386 */       return "3DES_EDE_CBC";
/* 291:    */     }
/* 292:389 */     if ("RC4".equals(bulkCipher))
/* 293:    */     {
/* 294:390 */       if (export) {
/* 295:391 */         return "RC4_40";
/* 296:    */       }
/* 297:393 */       return "RC4_128";
/* 298:    */     }
/* 299:397 */     if ("DES-CBC".equals(bulkCipher))
/* 300:    */     {
/* 301:398 */       if (export) {
/* 302:399 */         return "DES_CBC_40";
/* 303:    */       }
/* 304:401 */       return "DES_CBC";
/* 305:    */     }
/* 306:405 */     if ("RC2-CBC".equals(bulkCipher))
/* 307:    */     {
/* 308:406 */       if (export) {
/* 309:407 */         return "RC2_CBC_40";
/* 310:    */       }
/* 311:409 */       return "RC2_CBC";
/* 312:    */     }
/* 313:413 */     return bulkCipher.replace('-', '_');
/* 314:    */   }
/* 315:    */   
/* 316:    */   private static String toJavaHmacAlgo(String hmacAlgo)
/* 317:    */   {
/* 318:423 */     return hmacAlgo;
/* 319:    */   }
/* 320:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.CipherSuiteConverter
 * JD-Core Version:    0.7.0.1
 */