/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import org.slf4j.Logger;
/*   4:    */ 
/*   5:    */ class Slf4JLogger
/*   6:    */   extends AbstractInternalLogger
/*   7:    */ {
/*   8:    */   private static final long serialVersionUID = 108038972685130825L;
/*   9:    */   private final transient Logger logger;
/*  10:    */   
/*  11:    */   Slf4JLogger(Logger logger)
/*  12:    */   {
/*  13: 30 */     super(logger.getName());
/*  14: 31 */     this.logger = logger;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public boolean isTraceEnabled()
/*  18:    */   {
/*  19: 36 */     return this.logger.isTraceEnabled();
/*  20:    */   }
/*  21:    */   
/*  22:    */   public void trace(String msg)
/*  23:    */   {
/*  24: 41 */     this.logger.trace(msg);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public void trace(String format, Object arg)
/*  28:    */   {
/*  29: 46 */     this.logger.trace(format, arg);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void trace(String format, Object argA, Object argB)
/*  33:    */   {
/*  34: 51 */     this.logger.trace(format, argA, argB);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void trace(String format, Object... argArray)
/*  38:    */   {
/*  39: 56 */     this.logger.trace(format, argArray);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public void trace(String msg, Throwable t)
/*  43:    */   {
/*  44: 61 */     this.logger.trace(msg, t);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean isDebugEnabled()
/*  48:    */   {
/*  49: 66 */     return this.logger.isDebugEnabled();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void debug(String msg)
/*  53:    */   {
/*  54: 71 */     this.logger.debug(msg);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void debug(String format, Object arg)
/*  58:    */   {
/*  59: 76 */     this.logger.debug(format, arg);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void debug(String format, Object argA, Object argB)
/*  63:    */   {
/*  64: 81 */     this.logger.debug(format, argA, argB);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void debug(String format, Object... argArray)
/*  68:    */   {
/*  69: 86 */     this.logger.debug(format, argArray);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void debug(String msg, Throwable t)
/*  73:    */   {
/*  74: 91 */     this.logger.debug(msg, t);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public boolean isInfoEnabled()
/*  78:    */   {
/*  79: 96 */     return this.logger.isInfoEnabled();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void info(String msg)
/*  83:    */   {
/*  84:101 */     this.logger.info(msg);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void info(String format, Object arg)
/*  88:    */   {
/*  89:106 */     this.logger.info(format, arg);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public void info(String format, Object argA, Object argB)
/*  93:    */   {
/*  94:111 */     this.logger.info(format, argA, argB);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public void info(String format, Object... argArray)
/*  98:    */   {
/*  99:116 */     this.logger.info(format, argArray);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void info(String msg, Throwable t)
/* 103:    */   {
/* 104:121 */     this.logger.info(msg, t);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isWarnEnabled()
/* 108:    */   {
/* 109:126 */     return this.logger.isWarnEnabled();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public void warn(String msg)
/* 113:    */   {
/* 114:131 */     this.logger.warn(msg);
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void warn(String format, Object arg)
/* 118:    */   {
/* 119:136 */     this.logger.warn(format, arg);
/* 120:    */   }
/* 121:    */   
/* 122:    */   public void warn(String format, Object... argArray)
/* 123:    */   {
/* 124:141 */     this.logger.warn(format, argArray);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void warn(String format, Object argA, Object argB)
/* 128:    */   {
/* 129:146 */     this.logger.warn(format, argA, argB);
/* 130:    */   }
/* 131:    */   
/* 132:    */   public void warn(String msg, Throwable t)
/* 133:    */   {
/* 134:151 */     this.logger.warn(msg, t);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public boolean isErrorEnabled()
/* 138:    */   {
/* 139:156 */     return this.logger.isErrorEnabled();
/* 140:    */   }
/* 141:    */   
/* 142:    */   public void error(String msg)
/* 143:    */   {
/* 144:161 */     this.logger.error(msg);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public void error(String format, Object arg)
/* 148:    */   {
/* 149:166 */     this.logger.error(format, arg);
/* 150:    */   }
/* 151:    */   
/* 152:    */   public void error(String format, Object argA, Object argB)
/* 153:    */   {
/* 154:171 */     this.logger.error(format, argA, argB);
/* 155:    */   }
/* 156:    */   
/* 157:    */   public void error(String format, Object... argArray)
/* 158:    */   {
/* 159:176 */     this.logger.error(format, argArray);
/* 160:    */   }
/* 161:    */   
/* 162:    */   public void error(String msg, Throwable t)
/* 163:    */   {
/* 164:181 */     this.logger.error(msg, t);
/* 165:    */   }
/* 166:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.Slf4JLogger
 * JD-Core Version:    0.7.0.1
 */