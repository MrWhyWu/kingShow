/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import java.util.logging.Logger;
/*  4:   */ 
/*  5:   */ public class JdkLoggerFactory
/*  6:   */   extends InternalLoggerFactory
/*  7:   */ {
/*  8:28 */   public static final InternalLoggerFactory INSTANCE = new JdkLoggerFactory();
/*  9:   */   
/* 10:   */   public InternalLogger newInstance(String name)
/* 11:   */   {
/* 12:39 */     return new JdkLogger(Logger.getLogger(name));
/* 13:   */   }
/* 14:   */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.JdkLoggerFactory
 * JD-Core Version:    0.7.0.1
 */