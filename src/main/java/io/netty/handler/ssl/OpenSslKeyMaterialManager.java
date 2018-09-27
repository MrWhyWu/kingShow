/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.internal.tcnative.CertificateRequestedCallback.KeyMaterial;
/*   5:    */ import io.netty.internal.tcnative.SSL;
/*   6:    */ import java.security.PrivateKey;
/*   7:    */ import java.security.cert.X509Certificate;
/*   8:    */ import java.util.HashMap;
/*   9:    */ import java.util.HashSet;
/*  10:    */ import java.util.Map;
/*  11:    */ import java.util.Set;
/*  12:    */ import javax.net.ssl.SSLException;
/*  13:    */ import javax.net.ssl.X509KeyManager;
/*  14:    */ import javax.security.auth.x500.X500Principal;
/*  15:    */ 
/*  16:    */ class OpenSslKeyMaterialManager
/*  17:    */ {
/*  18:    */   static final String KEY_TYPE_RSA = "RSA";
/*  19:    */   static final String KEY_TYPE_DH_RSA = "DH_RSA";
/*  20:    */   static final String KEY_TYPE_EC = "EC";
/*  21:    */   static final String KEY_TYPE_EC_EC = "EC_EC";
/*  22:    */   static final String KEY_TYPE_EC_RSA = "EC_RSA";
/*  23: 54 */   private static final Map<String, String> KEY_TYPES = new HashMap();
/*  24:    */   private final X509KeyManager keyManager;
/*  25:    */   private final String password;
/*  26:    */   
/*  27:    */   static
/*  28:    */   {
/*  29: 56 */     KEY_TYPES.put("RSA", "RSA");
/*  30: 57 */     KEY_TYPES.put("DHE_RSA", "RSA");
/*  31: 58 */     KEY_TYPES.put("ECDHE_RSA", "RSA");
/*  32: 59 */     KEY_TYPES.put("ECDHE_ECDSA", "EC");
/*  33: 60 */     KEY_TYPES.put("ECDH_RSA", "EC_RSA");
/*  34: 61 */     KEY_TYPES.put("ECDH_ECDSA", "EC_EC");
/*  35: 62 */     KEY_TYPES.put("DH_RSA", "DH_RSA");
/*  36:    */   }
/*  37:    */   
/*  38:    */   OpenSslKeyMaterialManager(X509KeyManager keyManager, String password)
/*  39:    */   {
/*  40: 69 */     this.keyManager = keyManager;
/*  41: 70 */     this.password = password;
/*  42:    */   }
/*  43:    */   
/*  44:    */   void setKeyMaterial(ReferenceCountedOpenSslEngine engine)
/*  45:    */     throws SSLException
/*  46:    */   {
/*  47: 74 */     long ssl = engine.sslPointer();
/*  48: 75 */     String[] authMethods = SSL.authenticationMethods(ssl);
/*  49: 76 */     Set<String> aliases = new HashSet(authMethods.length);
/*  50: 77 */     for (String authMethod : authMethods)
/*  51:    */     {
/*  52: 78 */       String type = (String)KEY_TYPES.get(authMethod);
/*  53: 79 */       if (type != null)
/*  54:    */       {
/*  55: 80 */         String alias = chooseServerAlias(engine, type);
/*  56: 81 */         if ((alias != null) && (aliases.add(alias))) {
/*  57: 82 */           setKeyMaterial(ssl, alias);
/*  58:    */         }
/*  59:    */       }
/*  60:    */     }
/*  61:    */   }
/*  62:    */   
/*  63:    */   CertificateRequestedCallback.KeyMaterial keyMaterial(ReferenceCountedOpenSslEngine engine, String[] keyTypes, X500Principal[] issuer)
/*  64:    */     throws SSLException
/*  65:    */   {
/*  66: 90 */     String alias = chooseClientAlias(engine, keyTypes, issuer);
/*  67: 91 */     long keyBio = 0L;
/*  68: 92 */     long keyCertChainBio = 0L;
/*  69: 93 */     long pkey = 0L;
/*  70: 94 */     long certChain = 0L;
/*  71:    */     try
/*  72:    */     {
/*  73: 98 */       X509Certificate[] certificates = this.keyManager.getCertificateChain(alias);
/*  74: 99 */       if ((certificates == null) || (certificates.length == 0)) {
/*  75:100 */         return null;
/*  76:    */       }
/*  77:103 */       PrivateKey key = this.keyManager.getPrivateKey(alias);
/*  78:104 */       keyCertChainBio = ReferenceCountedOpenSslContext.toBIO(certificates);
/*  79:105 */       certChain = SSL.parseX509Chain(keyCertChainBio);
/*  80:106 */       if (key != null)
/*  81:    */       {
/*  82:107 */         keyBio = ReferenceCountedOpenSslContext.toBIO(key);
/*  83:108 */         pkey = SSL.parsePrivateKey(keyBio, this.password);
/*  84:    */       }
/*  85:110 */       CertificateRequestedCallback.KeyMaterial material = new CertificateRequestedCallback.KeyMaterial(certChain, pkey);
/*  86:    */       
/*  87:    */ 
/*  88:    */ 
/*  89:    */ 
/*  90:    */ 
/*  91:116 */       certChain = pkey = 0L;
/*  92:117 */       return material;
/*  93:    */     }
/*  94:    */     catch (SSLException e)
/*  95:    */     {
/*  96:119 */       throw e;
/*  97:    */     }
/*  98:    */     catch (Exception e)
/*  99:    */     {
/* 100:121 */       throw new SSLException(e);
/* 101:    */     }
/* 102:    */     finally
/* 103:    */     {
/* 104:123 */       ReferenceCountedOpenSslContext.freeBio(keyBio);
/* 105:124 */       ReferenceCountedOpenSslContext.freeBio(keyCertChainBio);
/* 106:125 */       SSL.freePrivateKey(pkey);
/* 107:126 */       SSL.freeX509Chain(certChain);
/* 108:    */     }
/* 109:    */   }
/* 110:    */   
/* 111:    */   private void setKeyMaterial(long ssl, String alias)
/* 112:    */     throws SSLException
/* 113:    */   {
/* 114:131 */     long keyBio = 0L;
/* 115:132 */     long keyCertChainBio = 0L;
/* 116:133 */     long keyCertChainBio2 = 0L;
/* 117:    */     try
/* 118:    */     {
/* 119:137 */       X509Certificate[] certificates = this.keyManager.getCertificateChain(alias);
/* 120:138 */       if ((certificates == null) || (certificates.length == 0)) {
/* 121:139 */         return;
/* 122:    */       }
/* 123:142 */       PrivateKey key = this.keyManager.getPrivateKey(alias);
/* 124:    */       
/* 125:    */ 
/* 126:145 */       PemEncoded encoded = PemX509Certificate.toPEM(ByteBufAllocator.DEFAULT, true, certificates);
/* 127:    */       try
/* 128:    */       {
/* 129:147 */         keyCertChainBio = ReferenceCountedOpenSslContext.toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
/* 130:148 */         keyCertChainBio2 = ReferenceCountedOpenSslContext.toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
/* 131:150 */         if (key != null) {
/* 132:151 */           keyBio = ReferenceCountedOpenSslContext.toBIO(key);
/* 133:    */         }
/* 134:153 */         SSL.setCertificateBio(ssl, keyCertChainBio, keyBio, this.password);
/* 135:    */         
/* 136:    */ 
/* 137:156 */         SSL.setCertificateChainBio(ssl, keyCertChainBio2, true);
/* 138:    */       }
/* 139:    */       finally
/* 140:    */       {
/* 141:158 */         encoded.release();
/* 142:    */       }
/* 143:    */     }
/* 144:    */     catch (SSLException e)
/* 145:    */     {
/* 146:161 */       throw e;
/* 147:    */     }
/* 148:    */     catch (Exception e)
/* 149:    */     {
/* 150:163 */       throw new SSLException(e);
/* 151:    */     }
/* 152:    */     finally
/* 153:    */     {
/* 154:165 */       ReferenceCountedOpenSslContext.freeBio(keyBio);
/* 155:166 */       ReferenceCountedOpenSslContext.freeBio(keyCertChainBio);
/* 156:167 */       ReferenceCountedOpenSslContext.freeBio(keyCertChainBio2);
/* 157:    */     }
/* 158:    */   }
/* 159:    */   
/* 160:    */   protected String chooseClientAlias(ReferenceCountedOpenSslEngine engine, String[] keyTypes, X500Principal[] issuer)
/* 161:    */   {
/* 162:173 */     return this.keyManager.chooseClientAlias(keyTypes, issuer, null);
/* 163:    */   }
/* 164:    */   
/* 165:    */   protected String chooseServerAlias(ReferenceCountedOpenSslEngine engine, String type)
/* 166:    */   {
/* 167:177 */     return this.keyManager.chooseServerAlias(type, null, null);
/* 168:    */   }
/* 169:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslKeyMaterialManager
 * JD-Core Version:    0.7.0.1
 */