/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import org.apache.logging.log4j.Level;
/*   4:    */ import org.apache.logging.log4j.Logger;
/*   5:    */ import org.apache.logging.log4j.spi.ExtendedLogger;
/*   6:    */ import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
/*   7:    */ 
/*   8:    */ class Log4J2Logger
/*   9:    */   extends ExtendedLoggerWrapper
/*  10:    */   implements InternalLogger
/*  11:    */ {
/*  12:    */   private static final long serialVersionUID = 5485418394879791397L;
/*  13:    */   private static final String EXCEPTION_MESSAGE = "Unexpected exception:";
/*  14:    */   
/*  15:    */   Log4J2Logger(Logger logger)
/*  16:    */   {
/*  17: 32 */     super((ExtendedLogger)logger, logger.getName(), logger.getMessageFactory());
/*  18:    */   }
/*  19:    */   
/*  20:    */   public String name()
/*  21:    */   {
/*  22: 37 */     return getName();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public void trace(Throwable t)
/*  26:    */   {
/*  27: 42 */     log(Level.TRACE, "Unexpected exception:", t);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public void debug(Throwable t)
/*  31:    */   {
/*  32: 47 */     log(Level.DEBUG, "Unexpected exception:", t);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public void info(Throwable t)
/*  36:    */   {
/*  37: 52 */     log(Level.INFO, "Unexpected exception:", t);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void warn(Throwable t)
/*  41:    */   {
/*  42: 57 */     log(Level.WARN, "Unexpected exception:", t);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void error(Throwable t)
/*  46:    */   {
/*  47: 62 */     log(Level.ERROR, "Unexpected exception:", t);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public boolean isEnabled(InternalLogLevel level)
/*  51:    */   {
/*  52: 67 */     return isEnabled(toLevel(level));
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void log(InternalLogLevel level, String msg)
/*  56:    */   {
/*  57: 72 */     log(toLevel(level), msg);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void log(InternalLogLevel level, String format, Object arg)
/*  61:    */   {
/*  62: 77 */     log(toLevel(level), format, arg);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void log(InternalLogLevel level, String format, Object argA, Object argB)
/*  66:    */   {
/*  67: 82 */     log(toLevel(level), format, argA, argB);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void log(InternalLogLevel level, String format, Object... arguments)
/*  71:    */   {
/*  72: 87 */     log(toLevel(level), format, arguments);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public void log(InternalLogLevel level, String msg, Throwable t)
/*  76:    */   {
/*  77: 92 */     log(toLevel(level), msg, t);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void log(InternalLogLevel level, Throwable t)
/*  81:    */   {
/*  82: 97 */     log(toLevel(level), "Unexpected exception:", t);
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected Level toLevel(InternalLogLevel level)
/*  86:    */   {
/*  87:101 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  88:    */     {
/*  89:    */     case 1: 
/*  90:103 */       return Level.INFO;
/*  91:    */     case 2: 
/*  92:105 */       return Level.DEBUG;
/*  93:    */     case 3: 
/*  94:107 */       return Level.WARN;
/*  95:    */     case 4: 
/*  96:109 */       return Level.ERROR;
/*  97:    */     case 5: 
/*  98:111 */       return Level.TRACE;
/*  99:    */     }
/* 100:113 */     throw new Error();
/* 101:    */   }
/* 102:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.Log4J2Logger
 * JD-Core Version:    0.7.0.1
 */