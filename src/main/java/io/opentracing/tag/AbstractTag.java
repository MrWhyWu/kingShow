/*  1:   */ package io.opentracing.tag;
/*  2:   */ 
/*  3:   */ import io.opentracing.Span;
/*  4:   */ 
/*  5:   */ public abstract class AbstractTag<T>
/*  6:   */ {
/*  7:   */   protected final String key;
/*  8:   */   
/*  9:   */   public AbstractTag(String tagKey)
/* 10:   */   {
/* 11:22 */     this.key = tagKey;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public String getKey()
/* 15:   */   {
/* 16:26 */     return this.key;
/* 17:   */   }
/* 18:   */   
/* 19:   */   protected abstract void set(Span paramSpan, T paramT);
/* 20:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.tag.AbstractTag
 * JD-Core Version:    0.7.0.1
 */