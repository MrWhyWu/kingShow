/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import org.slf4j.LoggerFactory;
/*  4:   */ import org.slf4j.helpers.NOPLoggerFactory;
/*  5:   */ 
/*  6:   */ public class Slf4JLoggerFactory
/*  7:   */   extends InternalLoggerFactory
/*  8:   */ {
/*  9:29 */   public static final InternalLoggerFactory INSTANCE = new Slf4JLoggerFactory();
/* 10:   */   
/* 11:   */   @Deprecated
/* 12:   */   public Slf4JLoggerFactory() {}
/* 13:   */   
/* 14:   */   Slf4JLoggerFactory(boolean failIfNOP)
/* 15:   */   {
/* 16:39 */     assert (failIfNOP);
/* 17:40 */     if ((LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory)) {
/* 18:41 */       throw new NoClassDefFoundError("NOPLoggerFactory not supported");
/* 19:   */     }
/* 20:   */   }
/* 21:   */   
/* 22:   */   public InternalLogger newInstance(String name)
/* 23:   */   {
/* 24:47 */     return new Slf4JLogger(LoggerFactory.getLogger(name));
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.Slf4JLoggerFactory
 * JD-Core Version:    0.7.0.1
 */