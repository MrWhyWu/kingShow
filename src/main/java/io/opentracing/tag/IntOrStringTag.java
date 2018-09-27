/*  1:   */ package io.opentracing.tag;
/*  2:   */ 
/*  3:   */ import io.opentracing.Span;
/*  4:   */ 
/*  5:   */ public class IntOrStringTag
/*  6:   */   extends IntTag
/*  7:   */ {
/*  8:   */   public IntOrStringTag(String key)
/*  9:   */   {
/* 10:20 */     super(key);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public void set(Span span, String tagValue)
/* 14:   */   {
/* 15:24 */     span.setTag(this.key, tagValue);
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.tag.IntOrStringTag
 * JD-Core Version:    0.7.0.1
 */