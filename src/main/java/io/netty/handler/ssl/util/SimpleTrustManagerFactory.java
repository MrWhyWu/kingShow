/*   1:    */ package io.netty.handler.ssl.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.security.InvalidAlgorithmParameterException;
/*   6:    */ import java.security.KeyStore;
/*   7:    */ import java.security.KeyStoreException;
/*   8:    */ import java.security.Provider;
/*   9:    */ import javax.net.ssl.ManagerFactoryParameters;
/*  10:    */ import javax.net.ssl.TrustManager;
/*  11:    */ import javax.net.ssl.TrustManagerFactory;
/*  12:    */ import javax.net.ssl.TrustManagerFactorySpi;
/*  13:    */ import javax.net.ssl.X509ExtendedTrustManager;
/*  14:    */ import javax.net.ssl.X509TrustManager;
/*  15:    */ 
/*  16:    */ public abstract class SimpleTrustManagerFactory
/*  17:    */   extends TrustManagerFactory
/*  18:    */ {
/*  19: 38 */   private static final Provider PROVIDER = new Provider("", 0.0D, "")
/*  20:    */   {
/*  21:    */     private static final long serialVersionUID = -2680540247105807895L;
/*  22:    */   };
/*  23: 50 */   private static final FastThreadLocal<SimpleTrustManagerFactorySpi> CURRENT_SPI = new FastThreadLocal()
/*  24:    */   {
/*  25:    */     protected SimpleTrustManagerFactory.SimpleTrustManagerFactorySpi initialValue()
/*  26:    */     {
/*  27: 54 */       return new SimpleTrustManagerFactory.SimpleTrustManagerFactorySpi();
/*  28:    */     }
/*  29:    */   };
/*  30:    */   
/*  31:    */   protected SimpleTrustManagerFactory()
/*  32:    */   {
/*  33: 62 */     this("");
/*  34:    */   }
/*  35:    */   
/*  36:    */   protected SimpleTrustManagerFactory(String name)
/*  37:    */   {
/*  38: 71 */     super((TrustManagerFactorySpi)CURRENT_SPI.get(), PROVIDER, name);
/*  39: 72 */     ((SimpleTrustManagerFactorySpi)CURRENT_SPI.get()).init(this);
/*  40: 73 */     CURRENT_SPI.remove();
/*  41: 75 */     if (name == null) {
/*  42: 76 */       throw new NullPointerException("name");
/*  43:    */     }
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected abstract void engineInit(KeyStore paramKeyStore)
/*  47:    */     throws Exception;
/*  48:    */   
/*  49:    */   protected abstract void engineInit(ManagerFactoryParameters paramManagerFactoryParameters)
/*  50:    */     throws Exception;
/*  51:    */   
/*  52:    */   protected abstract TrustManager[] engineGetTrustManagers();
/*  53:    */   
/*  54:    */   static final class SimpleTrustManagerFactorySpi
/*  55:    */     extends TrustManagerFactorySpi
/*  56:    */   {
/*  57:    */     private SimpleTrustManagerFactory parent;
/*  58:    */     private volatile TrustManager[] trustManagers;
/*  59:    */     
/*  60:    */     void init(SimpleTrustManagerFactory parent)
/*  61:    */     {
/*  62:107 */       this.parent = parent;
/*  63:    */     }
/*  64:    */     
/*  65:    */     protected void engineInit(KeyStore keyStore)
/*  66:    */       throws KeyStoreException
/*  67:    */     {
/*  68:    */       try
/*  69:    */       {
/*  70:113 */         this.parent.engineInit(keyStore);
/*  71:    */       }
/*  72:    */       catch (KeyStoreException e)
/*  73:    */       {
/*  74:115 */         throw e;
/*  75:    */       }
/*  76:    */       catch (Exception e)
/*  77:    */       {
/*  78:117 */         throw new KeyStoreException(e);
/*  79:    */       }
/*  80:    */     }
/*  81:    */     
/*  82:    */     protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
/*  83:    */       throws InvalidAlgorithmParameterException
/*  84:    */     {
/*  85:    */       try
/*  86:    */       {
/*  87:125 */         this.parent.engineInit(managerFactoryParameters);
/*  88:    */       }
/*  89:    */       catch (InvalidAlgorithmParameterException e)
/*  90:    */       {
/*  91:127 */         throw e;
/*  92:    */       }
/*  93:    */       catch (Exception e)
/*  94:    */       {
/*  95:129 */         throw new InvalidAlgorithmParameterException(e);
/*  96:    */       }
/*  97:    */     }
/*  98:    */     
/*  99:    */     protected TrustManager[] engineGetTrustManagers()
/* 100:    */     {
/* 101:135 */       TrustManager[] trustManagers = this.trustManagers;
/* 102:136 */       if (trustManagers == null)
/* 103:    */       {
/* 104:137 */         trustManagers = this.parent.engineGetTrustManagers();
/* 105:138 */         if (PlatformDependent.javaVersion() >= 7) {
/* 106:139 */           for (int i = 0; i < trustManagers.length; i++)
/* 107:    */           {
/* 108:140 */             TrustManager tm = trustManagers[i];
/* 109:141 */             if (((tm instanceof X509TrustManager)) && (!(tm instanceof X509ExtendedTrustManager))) {
/* 110:142 */               trustManagers[i] = new X509TrustManagerWrapper((X509TrustManager)tm);
/* 111:    */             }
/* 112:    */           }
/* 113:    */         }
/* 114:146 */         this.trustManagers = trustManagers;
/* 115:    */       }
/* 116:148 */       return (TrustManager[])trustManagers.clone();
/* 117:    */     }
/* 118:    */   }
/* 119:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.util.SimpleTrustManagerFactory
 * JD-Core Version:    0.7.0.1
 */