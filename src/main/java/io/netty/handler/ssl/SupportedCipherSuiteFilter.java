/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.List;
/*  5:   */ import java.util.Set;
/*  6:   */ 
/*  7:   */ public final class SupportedCipherSuiteFilter
/*  8:   */   implements CipherSuiteFilter
/*  9:   */ {
/* 10:27 */   public static final SupportedCipherSuiteFilter INSTANCE = new SupportedCipherSuiteFilter();
/* 11:   */   
/* 12:   */   public String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers, Set<String> supportedCiphers)
/* 13:   */   {
/* 14:34 */     if (defaultCiphers == null) {
/* 15:35 */       throw new NullPointerException("defaultCiphers");
/* 16:   */     }
/* 17:37 */     if (supportedCiphers == null) {
/* 18:38 */       throw new NullPointerException("supportedCiphers");
/* 19:   */     }
/* 20:   */     List<String> newCiphers;
/* 21:42 */     if (ciphers == null)
/* 22:   */     {
/* 23:43 */       List<String> newCiphers = new ArrayList(defaultCiphers.size());
/* 24:44 */       ciphers = defaultCiphers;
/* 25:   */     }
/* 26:   */     else
/* 27:   */     {
/* 28:46 */       newCiphers = new ArrayList(supportedCiphers.size());
/* 29:   */     }
/* 30:48 */     for (String c : ciphers)
/* 31:   */     {
/* 32:49 */       if (c == null) {
/* 33:   */         break;
/* 34:   */       }
/* 35:52 */       if (supportedCiphers.contains(c)) {
/* 36:53 */         newCiphers.add(c);
/* 37:   */       }
/* 38:   */     }
/* 39:56 */     return (String[])newCiphers.toArray(new String[newCiphers.size()]);
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.SupportedCipherSuiteFilter
 * JD-Core Version:    0.7.0.1
 */