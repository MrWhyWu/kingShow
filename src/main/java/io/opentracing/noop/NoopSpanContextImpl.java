/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import java.util.Collections;
/*  4:   */ import java.util.Map.Entry;
/*  5:   */ 
/*  6:   */ final class NoopSpanContextImpl
/*  7:   */   implements NoopSpanContext
/*  8:   */ {
/*  9:26 */   static final NoopSpanContextImpl INSTANCE = new NoopSpanContextImpl();
/* 10:   */   
/* 11:   */   public Iterable<Map.Entry<String, String>> baggageItems()
/* 12:   */   {
/* 13:30 */     return Collections.emptyList();
/* 14:   */   }
/* 15:   */   
/* 16:   */   public String toString()
/* 17:   */   {
/* 18:34 */     return NoopSpanContext.class.getSimpleName();
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopSpanContextImpl
 * JD-Core Version:    0.7.0.1
 */