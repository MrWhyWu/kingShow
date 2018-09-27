/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import io.netty.util.IllegalReferenceCountException;
/*   8:    */ import io.netty.util.internal.ObjectUtil;
/*   9:    */ import java.math.BigInteger;
/*  10:    */ import java.security.Principal;
/*  11:    */ import java.security.PublicKey;
/*  12:    */ import java.security.cert.CertificateEncodingException;
/*  13:    */ import java.security.cert.X509Certificate;
/*  14:    */ import java.util.Arrays;
/*  15:    */ import java.util.Date;
/*  16:    */ import java.util.Set;
/*  17:    */ 
/*  18:    */ public final class PemX509Certificate
/*  19:    */   extends X509Certificate
/*  20:    */   implements PemEncoded
/*  21:    */ {
/*  22: 49 */   private static final byte[] BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n".getBytes(CharsetUtil.US_ASCII);
/*  23: 50 */   private static final byte[] END_CERT = "\n-----END CERTIFICATE-----\n".getBytes(CharsetUtil.US_ASCII);
/*  24:    */   private final ByteBuf content;
/*  25:    */   
/*  26:    */   static PemEncoded toPEM(ByteBufAllocator allocator, boolean useDirect, X509Certificate... chain)
/*  27:    */     throws CertificateEncodingException
/*  28:    */   {
/*  29: 58 */     if ((chain == null) || (chain.length == 0)) {
/*  30: 59 */       throw new IllegalArgumentException("X.509 certificate chain can't be null or empty");
/*  31:    */     }
/*  32: 67 */     if (chain.length == 1)
/*  33:    */     {
/*  34: 68 */       X509Certificate first = chain[0];
/*  35: 69 */       if ((first instanceof PemEncoded)) {
/*  36: 70 */         return ((PemEncoded)first).retain();
/*  37:    */       }
/*  38:    */     }
/*  39: 74 */     boolean success = false;
/*  40: 75 */     ByteBuf pem = null;
/*  41:    */     try
/*  42:    */     {
/*  43: 77 */       for (X509Certificate cert : chain)
/*  44:    */       {
/*  45: 79 */         if (cert == null) {
/*  46: 80 */           throw new IllegalArgumentException("Null element in chain: " + Arrays.toString(chain));
/*  47:    */         }
/*  48: 83 */         if ((cert instanceof PemEncoded)) {
/*  49: 84 */           pem = append(allocator, useDirect, (PemEncoded)cert, chain.length, pem);
/*  50:    */         } else {
/*  51: 86 */           pem = append(allocator, useDirect, cert, chain.length, pem);
/*  52:    */         }
/*  53:    */       }
/*  54: 90 */       PemValue value = new PemValue(pem, false);
/*  55: 91 */       success = true;
/*  56: 92 */       return value;
/*  57:    */     }
/*  58:    */     finally
/*  59:    */     {
/*  60: 95 */       if ((!success) && (pem != null)) {
/*  61: 96 */         pem.release();
/*  62:    */       }
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:    */   private static ByteBuf append(ByteBufAllocator allocator, boolean useDirect, PemEncoded encoded, int count, ByteBuf pem)
/*  67:    */   {
/*  68:108 */     ByteBuf content = encoded.content();
/*  69:110 */     if (pem == null) {
/*  70:112 */       pem = newBuffer(allocator, useDirect, content.readableBytes() * count);
/*  71:    */     }
/*  72:115 */     pem.writeBytes(content.slice());
/*  73:116 */     return pem;
/*  74:    */   }
/*  75:    */   
/*  76:    */   private static ByteBuf append(ByteBufAllocator allocator, boolean useDirect, X509Certificate cert, int count, ByteBuf pem)
/*  77:    */     throws CertificateEncodingException
/*  78:    */   {
/*  79:126 */     ByteBuf encoded = Unpooled.wrappedBuffer(cert.getEncoded());
/*  80:    */     try
/*  81:    */     {
/*  82:128 */       ByteBuf base64 = SslUtils.toBase64(allocator, encoded);
/*  83:    */       try
/*  84:    */       {
/*  85:130 */         if (pem == null) {
/*  86:134 */           pem = newBuffer(allocator, useDirect, 
/*  87:135 */             (BEGIN_CERT.length + base64.readableBytes() + END_CERT.length) * count);
/*  88:    */         }
/*  89:138 */         pem.writeBytes(BEGIN_CERT);
/*  90:139 */         pem.writeBytes(base64);
/*  91:    */       }
/*  92:    */       finally {}
/*  93:    */     }
/*  94:    */     finally
/*  95:    */     {
/*  96:145 */       encoded.release();
/*  97:    */     }
/*  98:148 */     return pem;
/*  99:    */   }
/* 100:    */   
/* 101:    */   private static ByteBuf newBuffer(ByteBufAllocator allocator, boolean useDirect, int initialCapacity)
/* 102:    */   {
/* 103:152 */     return useDirect ? allocator.directBuffer(initialCapacity) : allocator.buffer(initialCapacity);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public static PemX509Certificate valueOf(byte[] key)
/* 107:    */   {
/* 108:162 */     return valueOf(Unpooled.wrappedBuffer(key));
/* 109:    */   }
/* 110:    */   
/* 111:    */   public static PemX509Certificate valueOf(ByteBuf key)
/* 112:    */   {
/* 113:172 */     return new PemX509Certificate(key);
/* 114:    */   }
/* 115:    */   
/* 116:    */   private PemX509Certificate(ByteBuf content)
/* 117:    */   {
/* 118:178 */     this.content = ((ByteBuf)ObjectUtil.checkNotNull(content, "content"));
/* 119:    */   }
/* 120:    */   
/* 121:    */   public boolean isSensitive()
/* 122:    */   {
/* 123:184 */     return false;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public int refCnt()
/* 127:    */   {
/* 128:189 */     return this.content.refCnt();
/* 129:    */   }
/* 130:    */   
/* 131:    */   public ByteBuf content()
/* 132:    */   {
/* 133:194 */     int count = refCnt();
/* 134:195 */     if (count <= 0) {
/* 135:196 */       throw new IllegalReferenceCountException(count);
/* 136:    */     }
/* 137:199 */     return this.content;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public PemX509Certificate copy()
/* 141:    */   {
/* 142:204 */     return replace(this.content.copy());
/* 143:    */   }
/* 144:    */   
/* 145:    */   public PemX509Certificate duplicate()
/* 146:    */   {
/* 147:209 */     return replace(this.content.duplicate());
/* 148:    */   }
/* 149:    */   
/* 150:    */   public PemX509Certificate retainedDuplicate()
/* 151:    */   {
/* 152:214 */     return replace(this.content.retainedDuplicate());
/* 153:    */   }
/* 154:    */   
/* 155:    */   public PemX509Certificate replace(ByteBuf content)
/* 156:    */   {
/* 157:219 */     return new PemX509Certificate(content);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public PemX509Certificate retain()
/* 161:    */   {
/* 162:224 */     this.content.retain();
/* 163:225 */     return this;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public PemX509Certificate retain(int increment)
/* 167:    */   {
/* 168:230 */     this.content.retain(increment);
/* 169:231 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public PemX509Certificate touch()
/* 173:    */   {
/* 174:236 */     this.content.touch();
/* 175:237 */     return this;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public PemX509Certificate touch(Object hint)
/* 179:    */   {
/* 180:242 */     this.content.touch(hint);
/* 181:243 */     return this;
/* 182:    */   }
/* 183:    */   
/* 184:    */   public boolean release()
/* 185:    */   {
/* 186:248 */     return this.content.release();
/* 187:    */   }
/* 188:    */   
/* 189:    */   public boolean release(int decrement)
/* 190:    */   {
/* 191:253 */     return this.content.release(decrement);
/* 192:    */   }
/* 193:    */   
/* 194:    */   public byte[] getEncoded()
/* 195:    */   {
/* 196:258 */     throw new UnsupportedOperationException();
/* 197:    */   }
/* 198:    */   
/* 199:    */   public boolean hasUnsupportedCriticalExtension()
/* 200:    */   {
/* 201:263 */     throw new UnsupportedOperationException();
/* 202:    */   }
/* 203:    */   
/* 204:    */   public Set<String> getCriticalExtensionOIDs()
/* 205:    */   {
/* 206:268 */     throw new UnsupportedOperationException();
/* 207:    */   }
/* 208:    */   
/* 209:    */   public Set<String> getNonCriticalExtensionOIDs()
/* 210:    */   {
/* 211:273 */     throw new UnsupportedOperationException();
/* 212:    */   }
/* 213:    */   
/* 214:    */   public byte[] getExtensionValue(String oid)
/* 215:    */   {
/* 216:278 */     throw new UnsupportedOperationException();
/* 217:    */   }
/* 218:    */   
/* 219:    */   public void checkValidity()
/* 220:    */   {
/* 221:283 */     throw new UnsupportedOperationException();
/* 222:    */   }
/* 223:    */   
/* 224:    */   public void checkValidity(Date date)
/* 225:    */   {
/* 226:288 */     throw new UnsupportedOperationException();
/* 227:    */   }
/* 228:    */   
/* 229:    */   public int getVersion()
/* 230:    */   {
/* 231:293 */     throw new UnsupportedOperationException();
/* 232:    */   }
/* 233:    */   
/* 234:    */   public BigInteger getSerialNumber()
/* 235:    */   {
/* 236:298 */     throw new UnsupportedOperationException();
/* 237:    */   }
/* 238:    */   
/* 239:    */   public Principal getIssuerDN()
/* 240:    */   {
/* 241:303 */     throw new UnsupportedOperationException();
/* 242:    */   }
/* 243:    */   
/* 244:    */   public Principal getSubjectDN()
/* 245:    */   {
/* 246:308 */     throw new UnsupportedOperationException();
/* 247:    */   }
/* 248:    */   
/* 249:    */   public Date getNotBefore()
/* 250:    */   {
/* 251:313 */     throw new UnsupportedOperationException();
/* 252:    */   }
/* 253:    */   
/* 254:    */   public Date getNotAfter()
/* 255:    */   {
/* 256:318 */     throw new UnsupportedOperationException();
/* 257:    */   }
/* 258:    */   
/* 259:    */   public byte[] getTBSCertificate()
/* 260:    */   {
/* 261:323 */     throw new UnsupportedOperationException();
/* 262:    */   }
/* 263:    */   
/* 264:    */   public byte[] getSignature()
/* 265:    */   {
/* 266:328 */     throw new UnsupportedOperationException();
/* 267:    */   }
/* 268:    */   
/* 269:    */   public String getSigAlgName()
/* 270:    */   {
/* 271:333 */     throw new UnsupportedOperationException();
/* 272:    */   }
/* 273:    */   
/* 274:    */   public String getSigAlgOID()
/* 275:    */   {
/* 276:338 */     throw new UnsupportedOperationException();
/* 277:    */   }
/* 278:    */   
/* 279:    */   public byte[] getSigAlgParams()
/* 280:    */   {
/* 281:343 */     throw new UnsupportedOperationException();
/* 282:    */   }
/* 283:    */   
/* 284:    */   public boolean[] getIssuerUniqueID()
/* 285:    */   {
/* 286:348 */     throw new UnsupportedOperationException();
/* 287:    */   }
/* 288:    */   
/* 289:    */   public boolean[] getSubjectUniqueID()
/* 290:    */   {
/* 291:353 */     throw new UnsupportedOperationException();
/* 292:    */   }
/* 293:    */   
/* 294:    */   public boolean[] getKeyUsage()
/* 295:    */   {
/* 296:358 */     throw new UnsupportedOperationException();
/* 297:    */   }
/* 298:    */   
/* 299:    */   public int getBasicConstraints()
/* 300:    */   {
/* 301:363 */     throw new UnsupportedOperationException();
/* 302:    */   }
/* 303:    */   
/* 304:    */   public void verify(PublicKey key)
/* 305:    */   {
/* 306:368 */     throw new UnsupportedOperationException();
/* 307:    */   }
/* 308:    */   
/* 309:    */   public void verify(PublicKey key, String sigProvider)
/* 310:    */   {
/* 311:373 */     throw new UnsupportedOperationException();
/* 312:    */   }
/* 313:    */   
/* 314:    */   public PublicKey getPublicKey()
/* 315:    */   {
/* 316:378 */     throw new UnsupportedOperationException();
/* 317:    */   }
/* 318:    */   
/* 319:    */   public boolean equals(Object o)
/* 320:    */   {
/* 321:383 */     if (o == this) {
/* 322:384 */       return true;
/* 323:    */     }
/* 324:385 */     if (!(o instanceof PemX509Certificate)) {
/* 325:386 */       return false;
/* 326:    */     }
/* 327:389 */     PemX509Certificate other = (PemX509Certificate)o;
/* 328:390 */     return this.content.equals(other.content);
/* 329:    */   }
/* 330:    */   
/* 331:    */   public int hashCode()
/* 332:    */   {
/* 333:395 */     return this.content.hashCode();
/* 334:    */   }
/* 335:    */   
/* 336:    */   public String toString()
/* 337:    */   {
/* 338:400 */     return this.content.toString(CharsetUtil.UTF_8);
/* 339:    */   }
/* 340:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.PemX509Certificate
 * JD-Core Version:    0.7.0.1
 */