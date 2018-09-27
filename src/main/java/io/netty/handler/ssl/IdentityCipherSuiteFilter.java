/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.List;
/*  5:   */ import java.util.Set;
/*  6:   */ 
/*  7:   */ public final class IdentityCipherSuiteFilter
/*  8:   */   implements CipherSuiteFilter
/*  9:   */ {
/* 10:26 */   public static final IdentityCipherSuiteFilter INSTANCE = new IdentityCipherSuiteFilter();
/* 11:   */   
/* 12:   */   public String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers, Set<String> supportedCiphers)
/* 13:   */   {
/* 14:33 */     if (ciphers == null) {
/* 15:34 */       return (String[])defaultCiphers.toArray(new String[defaultCiphers.size()]);
/* 16:   */     }
/* 17:36 */     List<String> newCiphers = new ArrayList(supportedCiphers.size());
/* 18:37 */     for (String c : ciphers)
/* 19:   */     {
/* 20:38 */       if (c == null) {
/* 21:   */         break;
/* 22:   */       }
/* 23:41 */       newCiphers.add(c);
/* 24:   */     }
/* 25:43 */     return (String[])newCiphers.toArray(new String[newCiphers.size()]);
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.IdentityCipherSuiteFilter
 * JD-Core Version:    0.7.0.1
 */