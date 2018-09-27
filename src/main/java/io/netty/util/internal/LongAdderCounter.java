/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.LongAdder;
/*  4:   */ 
/*  5:   */ final class LongAdderCounter
/*  6:   */   extends LongAdder
/*  7:   */   implements LongCounter
/*  8:   */ {
/*  9:   */   public long value()
/* 10:   */   {
/* 11:24 */     return longValue();
/* 12:   */   }
/* 13:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.LongAdderCounter
 * JD-Core Version:    0.7.0.1
 */