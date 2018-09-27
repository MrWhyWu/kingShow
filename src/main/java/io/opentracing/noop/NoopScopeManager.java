/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ import io.opentracing.Scope;
/*  4:   */ import io.opentracing.ScopeManager;
/*  5:   */ 
/*  6:   */ public abstract interface NoopScopeManager
/*  7:   */   extends ScopeManager
/*  8:   */ {
/*  9:21 */   public static final NoopScopeManager INSTANCE = new NoopScopeManagerImpl();
/* 10:   */   
/* 11:   */   public static abstract interface NoopScope
/* 12:   */     extends Scope
/* 13:   */   {
/* 14:24 */     public static final NoopScope INSTANCE = new NoopScopeManagerImpl.NoopScopeImpl();
/* 15:   */   }
/* 16:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopScopeManager
 * JD-Core Version:    0.7.0.1
 */