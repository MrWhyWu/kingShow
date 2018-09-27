/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.List;
/*  5:   */ 
/*  6:   */ final class ApplicationProtocolUtil
/*  7:   */ {
/*  8:   */   private static final int DEFAULT_LIST_SIZE = 2;
/*  9:   */   
/* 10:   */   static List<String> toList(Iterable<String> protocols)
/* 11:   */   {
/* 12:31 */     return toList(2, protocols);
/* 13:   */   }
/* 14:   */   
/* 15:   */   static List<String> toList(int initialListSize, Iterable<String> protocols)
/* 16:   */   {
/* 17:35 */     if (protocols == null) {
/* 18:36 */       return null;
/* 19:   */     }
/* 20:39 */     List<String> result = new ArrayList(initialListSize);
/* 21:40 */     for (String p : protocols)
/* 22:   */     {
/* 23:41 */       if ((p == null) || (p.isEmpty())) {
/* 24:42 */         throw new IllegalArgumentException("protocol cannot be null or empty");
/* 25:   */       }
/* 26:44 */       result.add(p);
/* 27:   */     }
/* 28:47 */     if (result.isEmpty()) {
/* 29:48 */       throw new IllegalArgumentException("protocols cannot empty");
/* 30:   */     }
/* 31:51 */     return result;
/* 32:   */   }
/* 33:   */   
/* 34:   */   static List<String> toList(String... protocols)
/* 35:   */   {
/* 36:55 */     return toList(2, protocols);
/* 37:   */   }
/* 38:   */   
/* 39:   */   static List<String> toList(int initialListSize, String... protocols)
/* 40:   */   {
/* 41:59 */     if (protocols == null) {
/* 42:60 */       return null;
/* 43:   */     }
/* 44:63 */     List<String> result = new ArrayList(initialListSize);
/* 45:64 */     for (String p : protocols)
/* 46:   */     {
/* 47:65 */       if ((p == null) || (p.isEmpty())) {
/* 48:66 */         throw new IllegalArgumentException("protocol cannot be null or empty");
/* 49:   */       }
/* 50:68 */       result.add(p);
/* 51:   */     }
/* 52:71 */     if (result.isEmpty()) {
/* 53:72 */       throw new IllegalArgumentException("protocols cannot empty");
/* 54:   */     }
/* 55:75 */     return result;
/* 56:   */   }
/* 57:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.ssl.ApplicationProtocolUtil
 * JD-Core Version:    0.7.0.1
 */