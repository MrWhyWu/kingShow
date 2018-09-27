/*  1:   */ package io.opentracing.noop;
/*  2:   */ 
/*  3:   */ public final class NoopTracerFactory
/*  4:   */ {
/*  5:   */   public static NoopTracer create()
/*  6:   */   {
/*  7:19 */     return NoopTracerImpl.INSTANCE;
/*  8:   */   }
/*  9:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.opentracing.noop.NoopTracerFactory
 * JD-Core Version:    0.7.0.1
 */