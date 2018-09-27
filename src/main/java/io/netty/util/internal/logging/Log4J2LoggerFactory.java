/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import org.apache.logging.log4j.LogManager;
/*  4:   */ 
/*  5:   */ public final class Log4J2LoggerFactory
/*  6:   */   extends InternalLoggerFactory
/*  7:   */ {
/*  8:22 */   public static final InternalLoggerFactory INSTANCE = new Log4J2LoggerFactory();
/*  9:   */   
/* 10:   */   public InternalLogger newInstance(String name)
/* 11:   */   {
/* 12:33 */     return new Log4J2Logger(LogManager.getLogger(name));
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.Log4J2LoggerFactory
 * JD-Core Version:    0.7.0.1
 */