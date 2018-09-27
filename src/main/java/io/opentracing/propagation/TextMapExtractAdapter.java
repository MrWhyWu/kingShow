/*  1:   */ package io.opentracing.propagation;
/*  2:   */ 
/*  3:   */ import java.util.Iterator;
/*  4:   */ import java.util.Map;
/*  5:   */ import java.util.Map.Entry;
/*  6:   */ import java.util.Set;
/*  7:   */ 
/*  8:   */ public final class TextMapExtractAdapter
/*  9:   */   implements TextMap
/* 10:   */ {
/* 11:   */   private final Map<String, String> map;
/* 12:   */   
/* 13:   */   public TextMapExtractAdapter(Map<String, String> map)
/* 14:   */   {
/* 15:33 */     this.map = map;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public Iterator<Map.Entry<String, String>> iterator()
/* 19:   */   {
/* 20:38 */     return this.map.entrySet().iterator();
/* 21:   */   }
/* 22:   */   
/* 23:   */   public void put(String key, String value)
/* 24:   */   {
/* 25:43 */     throw new UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.extract()");
/* 26:   */   }
/* 27:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.propagation.TextMapExtractAdapter
 * JD-Core Version:    0.7.0.1
 */