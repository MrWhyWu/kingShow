/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.Collection;
/*  5:   */ import java.util.Collections;
/*  6:   */ import java.util.Iterator;
/*  7:   */ import java.util.List;
/*  8:   */ import javax.net.ssl.SNIHostName;
/*  9:   */ import javax.net.ssl.SNIMatcher;
/* 10:   */ import javax.net.ssl.SNIServerName;
/* 11:   */ import javax.net.ssl.SSLParameters;
/* 12:   */ 
/* 13:   */ final class Java8SslUtils
/* 14:   */ {
/* 15:   */   static List<String> getSniHostNames(SSLParameters sslParameters)
/* 16:   */   {
/* 17:33 */     List<SNIServerName> names = sslParameters.getServerNames();
/* 18:34 */     if ((names == null) || (names.isEmpty())) {
/* 19:35 */       return Collections.emptyList();
/* 20:   */     }
/* 21:37 */     List<String> strings = new ArrayList(names.size());
/* 22:39 */     for (SNIServerName serverName : names) {
/* 23:40 */       if ((serverName instanceof SNIHostName)) {
/* 24:41 */         strings.add(((SNIHostName)serverName).getAsciiName());
/* 25:   */       } else {
/* 26:43 */         throw new IllegalArgumentException("Only " + SNIHostName.class.getName() + " instances are supported, but found: " + serverName);
/* 27:   */       }
/* 28:   */     }
/* 29:47 */     return strings;
/* 30:   */   }
/* 31:   */   
/* 32:   */   static void setSniHostNames(SSLParameters sslParameters, List<String> names)
/* 33:   */   {
/* 34:51 */     List<SNIServerName> sniServerNames = new ArrayList(names.size());
/* 35:52 */     for (String name : names) {
/* 36:53 */       sniServerNames.add(new SNIHostName(name));
/* 37:   */     }
/* 38:55 */     sslParameters.setServerNames(sniServerNames);
/* 39:   */   }
/* 40:   */   
/* 41:   */   static boolean getUseCipherSuitesOrder(SSLParameters sslParameters)
/* 42:   */   {
/* 43:59 */     return sslParameters.getUseCipherSuitesOrder();
/* 44:   */   }
/* 45:   */   
/* 46:   */   static void setUseCipherSuitesOrder(SSLParameters sslParameters, boolean useOrder)
/* 47:   */   {
/* 48:63 */     sslParameters.setUseCipherSuitesOrder(useOrder);
/* 49:   */   }
/* 50:   */   
/* 51:   */   static void setSNIMatchers(SSLParameters sslParameters, Collection<?> matchers)
/* 52:   */   {
/* 53:68 */     sslParameters.setSNIMatchers(matchers);
/* 54:   */   }
/* 55:   */   
/* 56:   */   static boolean checkSniHostnameMatch(Collection<?> matchers, String hostname)
/* 57:   */   {
/* 58:73 */     if ((matchers != null) && (!matchers.isEmpty()))
/* 59:   */     {
/* 60:74 */       SNIHostName name = new SNIHostName(hostname);
/* 61:75 */       Iterator<SNIMatcher> matcherIt = matchers.iterator();
/* 62:76 */       while (matcherIt.hasNext())
/* 63:   */       {
/* 64:77 */         SNIMatcher matcher = (SNIMatcher)matcherIt.next();
/* 65:79 */         if ((matcher.getType() == 0) && (matcher.matches(name))) {
/* 66:80 */           return true;
/* 67:   */         }
/* 68:   */       }
/* 69:83 */       return false;
/* 70:   */     }
/* 71:85 */     return true;
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.Java8SslUtils
 * JD-Core Version:    0.7.0.1
 */