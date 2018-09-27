/*  1:   */ package io.netty.handler.logging;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.logging.InternalLogLevel;
/*  4:   */ 
/*  5:   */ public enum LogLevel
/*  6:   */ {
/*  7:24 */   TRACE(InternalLogLevel.TRACE),  DEBUG(InternalLogLevel.DEBUG),  INFO(InternalLogLevel.INFO),  WARN(InternalLogLevel.WARN),  ERROR(InternalLogLevel.ERROR);
/*  8:   */   
/*  9:   */   private final InternalLogLevel internalLevel;
/* 10:   */   
/* 11:   */   private LogLevel(InternalLogLevel internalLevel)
/* 12:   */   {
/* 13:33 */     this.internalLevel = internalLevel;
/* 14:   */   }
/* 15:   */   
/* 16:   */   public InternalLogLevel toInternalLevel()
/* 17:   */   {
/* 18:44 */     return this.internalLevel;
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.logging.LogLevel
 * JD-Core Version:    0.7.0.1
 */