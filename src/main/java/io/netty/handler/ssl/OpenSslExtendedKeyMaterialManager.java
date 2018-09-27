/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import javax.net.ssl.X509ExtendedKeyManager;
/*  4:   */ import javax.security.auth.x500.X500Principal;
/*  5:   */ 
/*  6:   */ final class OpenSslExtendedKeyMaterialManager
/*  7:   */   extends OpenSslKeyMaterialManager
/*  8:   */ {
/*  9:   */   private final X509ExtendedKeyManager keyManager;
/* 10:   */   
/* 11:   */   OpenSslExtendedKeyMaterialManager(X509ExtendedKeyManager keyManager, String password)
/* 12:   */   {
/* 13:26 */     super(keyManager, password);
/* 14:27 */     this.keyManager = keyManager;
/* 15:   */   }
/* 16:   */   
/* 17:   */   protected String chooseClientAlias(ReferenceCountedOpenSslEngine engine, String[] keyTypes, X500Principal[] issuer)
/* 18:   */   {
/* 19:33 */     return this.keyManager.chooseEngineClientAlias(keyTypes, issuer, engine);
/* 20:   */   }
/* 21:   */   
/* 22:   */   protected String chooseServerAlias(ReferenceCountedOpenSslEngine engine, String type)
/* 23:   */   {
/* 24:38 */     return this.keyManager.chooseEngineServerAlias(type, null, engine);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.OpenSslExtendedKeyMaterialManager
 * JD-Core Version:    0.7.0.1
 */