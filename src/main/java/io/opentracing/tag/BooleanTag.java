/*  1:   */ package io.opentracing.tag;
/*  2:   */ 
/*  3:   */ import io.opentracing.Span;
/*  4:   */ 
/*  5:   */ public class BooleanTag
/*  6:   */   extends AbstractTag<Boolean>
/*  7:   */ {
/*  8:   */   public BooleanTag(String key)
/*  9:   */   {
/* 10:20 */     super(key);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public void set(Span span, Boolean tagValue)
/* 14:   */   {
/* 15:25 */     span.setTag(this.key, tagValue.booleanValue());
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.tag.BooleanTag
 * JD-Core Version:    0.7.0.1
 */