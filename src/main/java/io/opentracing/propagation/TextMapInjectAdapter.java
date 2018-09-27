/*  1:   */ package io.opentracing.propagation;
/*  2:   */ 
/*  3:   */ import java.util.Iterator;
/*  4:   */ import java.util.Map;
/*  5:   */ import java.util.Map.Entry;
/*  6:   */ 
/*  7:   */ public final class TextMapInjectAdapter
/*  8:   */   implements TextMap
/*  9:   */ {
/* 10:   */   private final Map<String, String> map;
/* 11:   */   
/* 12:   */   public TextMapInjectAdapter(Map<String, String> map)
/* 13:   */   {
/* 14:34 */     this.map = map;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public Iterator<Map.Entry<String, String>> iterator()
/* 18:   */   {
/* 19:39 */     throw new UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.inject()");
/* 20:   */   }
/* 21:   */   
/* 22:   */   public void put(String key, String value)
/* 23:   */   {
/* 24:44 */     this.map.put(key, value);
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.propagation.TextMapInjectAdapter
 * JD-Core Version:    0.7.0.1
 */