/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import org.apache.log4j.Level;
/*   4:    */ import org.apache.log4j.Logger;
/*   5:    */ 
/*   6:    */ class Log4JLogger
/*   7:    */   extends AbstractInternalLogger
/*   8:    */ {
/*   9:    */   private static final long serialVersionUID = 2851357342488183058L;
/*  10:    */   final transient Logger logger;
/*  11: 59 */   static final String FQCN = Log4JLogger.class.getName();
/*  12:    */   final boolean traceCapable;
/*  13:    */   
/*  14:    */   Log4JLogger(Logger logger)
/*  15:    */   {
/*  16: 66 */     super(logger.getName());
/*  17: 67 */     this.logger = logger;
/*  18: 68 */     this.traceCapable = isTraceCapable();
/*  19:    */   }
/*  20:    */   
/*  21:    */   private boolean isTraceCapable()
/*  22:    */   {
/*  23:    */     try
/*  24:    */     {
/*  25: 73 */       this.logger.isTraceEnabled();
/*  26: 74 */       return true;
/*  27:    */     }
/*  28:    */     catch (NoSuchMethodError ignored) {}
/*  29: 76 */     return false;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public boolean isTraceEnabled()
/*  33:    */   {
/*  34: 87 */     if (this.traceCapable) {
/*  35: 88 */       return this.logger.isTraceEnabled();
/*  36:    */     }
/*  37: 90 */     return this.logger.isDebugEnabled();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void trace(String msg)
/*  41:    */   {
/*  42:102 */     this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, msg, null);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void trace(String format, Object arg)
/*  46:    */   {
/*  47:121 */     if (isTraceEnabled())
/*  48:    */     {
/*  49:122 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  50:123 */       this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft
/*  51:124 */         .getMessage(), ft.getThrowable());
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void trace(String format, Object argA, Object argB)
/*  56:    */   {
/*  57:146 */     if (isTraceEnabled())
/*  58:    */     {
/*  59:147 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  60:148 */       this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft
/*  61:149 */         .getMessage(), ft.getThrowable());
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void trace(String format, Object... arguments)
/*  66:    */   {
/*  67:169 */     if (isTraceEnabled())
/*  68:    */     {
/*  69:170 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/*  70:171 */       this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft
/*  71:172 */         .getMessage(), ft.getThrowable());
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   public void trace(String msg, Throwable t)
/*  76:    */   {
/*  77:186 */     this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, msg, t);
/*  78:    */   }
/*  79:    */   
/*  80:    */   public boolean isDebugEnabled()
/*  81:    */   {
/*  82:196 */     return this.logger.isDebugEnabled();
/*  83:    */   }
/*  84:    */   
/*  85:    */   public void debug(String msg)
/*  86:    */   {
/*  87:207 */     this.logger.log(FQCN, Level.DEBUG, msg, null);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void debug(String format, Object arg)
/*  91:    */   {
/*  92:226 */     if (this.logger.isDebugEnabled())
/*  93:    */     {
/*  94:227 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  95:228 */       this.logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void debug(String format, Object argA, Object argB)
/* 100:    */   {
/* 101:250 */     if (this.logger.isDebugEnabled())
/* 102:    */     {
/* 103:251 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 104:252 */       this.logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
/* 105:    */     }
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void debug(String format, Object... arguments)
/* 109:    */   {
/* 110:271 */     if (this.logger.isDebugEnabled())
/* 111:    */     {
/* 112:272 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 113:273 */       this.logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
/* 114:    */     }
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void debug(String msg, Throwable t)
/* 118:    */   {
/* 119:287 */     this.logger.log(FQCN, Level.DEBUG, msg, t);
/* 120:    */   }
/* 121:    */   
/* 122:    */   public boolean isInfoEnabled()
/* 123:    */   {
/* 124:297 */     return this.logger.isInfoEnabled();
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void info(String msg)
/* 128:    */   {
/* 129:308 */     this.logger.log(FQCN, Level.INFO, msg, null);
/* 130:    */   }
/* 131:    */   
/* 132:    */   public void info(String format, Object arg)
/* 133:    */   {
/* 134:326 */     if (this.logger.isInfoEnabled())
/* 135:    */     {
/* 136:327 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 137:328 */       this.logger.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   public void info(String format, Object argA, Object argB)
/* 142:    */   {
/* 143:350 */     if (this.logger.isInfoEnabled())
/* 144:    */     {
/* 145:351 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 146:352 */       this.logger.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 147:    */     }
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void info(String format, Object... argArray)
/* 151:    */   {
/* 152:372 */     if (this.logger.isInfoEnabled())
/* 153:    */     {
/* 154:373 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 155:374 */       this.logger.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 156:    */     }
/* 157:    */   }
/* 158:    */   
/* 159:    */   public void info(String msg, Throwable t)
/* 160:    */   {
/* 161:389 */     this.logger.log(FQCN, Level.INFO, msg, t);
/* 162:    */   }
/* 163:    */   
/* 164:    */   public boolean isWarnEnabled()
/* 165:    */   {
/* 166:399 */     return this.logger.isEnabledFor(Level.WARN);
/* 167:    */   }
/* 168:    */   
/* 169:    */   public void warn(String msg)
/* 170:    */   {
/* 171:410 */     this.logger.log(FQCN, Level.WARN, msg, null);
/* 172:    */   }
/* 173:    */   
/* 174:    */   public void warn(String format, Object arg)
/* 175:    */   {
/* 176:429 */     if (this.logger.isEnabledFor(Level.WARN))
/* 177:    */     {
/* 178:430 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 179:431 */       this.logger.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
/* 180:    */     }
/* 181:    */   }
/* 182:    */   
/* 183:    */   public void warn(String format, Object argA, Object argB)
/* 184:    */   {
/* 185:453 */     if (this.logger.isEnabledFor(Level.WARN))
/* 186:    */     {
/* 187:454 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 188:455 */       this.logger.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
/* 189:    */     }
/* 190:    */   }
/* 191:    */   
/* 192:    */   public void warn(String format, Object... argArray)
/* 193:    */   {
/* 194:475 */     if (this.logger.isEnabledFor(Level.WARN))
/* 195:    */     {
/* 196:476 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 197:477 */       this.logger.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
/* 198:    */     }
/* 199:    */   }
/* 200:    */   
/* 201:    */   public void warn(String msg, Throwable t)
/* 202:    */   {
/* 203:492 */     this.logger.log(FQCN, Level.WARN, msg, t);
/* 204:    */   }
/* 205:    */   
/* 206:    */   public boolean isErrorEnabled()
/* 207:    */   {
/* 208:502 */     return this.logger.isEnabledFor(Level.ERROR);
/* 209:    */   }
/* 210:    */   
/* 211:    */   public void error(String msg)
/* 212:    */   {
/* 213:513 */     this.logger.log(FQCN, Level.ERROR, msg, null);
/* 214:    */   }
/* 215:    */   
/* 216:    */   public void error(String format, Object arg)
/* 217:    */   {
/* 218:532 */     if (this.logger.isEnabledFor(Level.ERROR))
/* 219:    */     {
/* 220:533 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 221:534 */       this.logger.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
/* 222:    */     }
/* 223:    */   }
/* 224:    */   
/* 225:    */   public void error(String format, Object argA, Object argB)
/* 226:    */   {
/* 227:556 */     if (this.logger.isEnabledFor(Level.ERROR))
/* 228:    */     {
/* 229:557 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 230:558 */       this.logger.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
/* 231:    */     }
/* 232:    */   }
/* 233:    */   
/* 234:    */   public void error(String format, Object... argArray)
/* 235:    */   {
/* 236:578 */     if (this.logger.isEnabledFor(Level.ERROR))
/* 237:    */     {
/* 238:579 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 239:580 */       this.logger.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
/* 240:    */     }
/* 241:    */   }
/* 242:    */   
/* 243:    */   public void error(String msg, Throwable t)
/* 244:    */   {
/* 245:595 */     this.logger.log(FQCN, Level.ERROR, msg, t);
/* 246:    */   }
/* 247:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.Log4JLogger
 * JD-Core Version:    0.7.0.1
 */