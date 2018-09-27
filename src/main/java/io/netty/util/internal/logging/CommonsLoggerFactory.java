/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import org.apache.commons.logging.LogFactory;
/*  4:   */ 
/*  5:   */ @Deprecated
/*  6:   */ public class CommonsLoggerFactory
/*  7:   */   extends InternalLoggerFactory
/*  8:   */ {
/*  9:32 */   public static final InternalLoggerFactory INSTANCE = new CommonsLoggerFactory();
/* 10:   */   
/* 11:   */   public InternalLogger newInstance(String name)
/* 12:   */   {
/* 13:43 */     return new CommonsLogger(LogFactory.getLog(name), name);
/* 14:   */   }
/* 15:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.CommonsLoggerFactory
 * JD-Core Version:    0.7.0.1
 */