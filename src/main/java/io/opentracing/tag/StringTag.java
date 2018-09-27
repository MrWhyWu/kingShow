/*  1:   */ package io.opentracing.tag;
/*  2:   */ 
/*  3:   */ import io.opentracing.Span;
/*  4:   */ 
/*  5:   */ public class StringTag
/*  6:   */   extends AbstractTag<String>
/*  7:   */ {
/*  8:   */   public StringTag(String key)
/*  9:   */   {
/* 10:20 */     super(key);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public void set(Span span, String tagValue)
/* 14:   */   {
/* 15:25 */     span.setTag(this.key, tagValue);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public void set(Span span, StringTag tag)
/* 19:   */   {
/* 20:29 */     span.setTag(this.key, tag.key);
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.tag.StringTag
 * JD-Core Version:    0.7.0.1
 */