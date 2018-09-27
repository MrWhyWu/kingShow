/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import org.apache.log4j.Logger;
/*  4:   */ 
/*  5:   */ public class Log4JLoggerFactory
/*  6:   */   extends InternalLoggerFactory
/*  7:   */ {
/*  8:27 */   public static final InternalLoggerFactory INSTANCE = new Log4JLoggerFactory();
/*  9:   */   
/* 10:   */   public InternalLogger newInstance(String name)
/* 11:   */   {
/* 12:38 */     return new Log4JLogger(Logger.getLogger(name));
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.Log4JLoggerFactory
 * JD-Core Version:    0.7.0.1
 */