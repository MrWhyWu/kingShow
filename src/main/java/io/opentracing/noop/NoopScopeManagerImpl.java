/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.Scope;
/*  4:   */ import io.opentracing.Span;
/*  5:   */ 
/*  6:   */ class NoopScopeManagerImpl
/*  7:   */   implements NoopScopeManager
/*  8:   */ {
/*  9:   */   public Scope activate(Span span, boolean finishOnClose)
/* 10:   */   {
/* 11:34 */     return NoopScopeManager.NoopScope.INSTANCE;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public Scope active()
/* 15:   */   {
/* 16:39 */     return null;
/* 17:   */   }
/* 18:   */   
/* 19:   */   static class NoopScopeImpl
/* 20:   */     implements NoopScopeManager.NoopScope
/* 21:   */   {
/* 22:   */     public void close() {}
/* 23:   */     
/* 24:   */     public Span span()
/* 25:   */     {
/* 26:48 */       return NoopSpan.INSTANCE;
/* 27:   */     }
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopScopeManagerImpl
 * JD-Core Version:    0.7.0.1
 */