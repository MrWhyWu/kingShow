/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import java.util.logging.Level;
/*   4:    */ import java.util.logging.LogRecord;
/*   5:    */ import java.util.logging.Logger;
/*   6:    */ 
/*   7:    */ class JdkLogger
/*   8:    */   extends AbstractInternalLogger
/*   9:    */ {
/*  10:    */   private static final long serialVersionUID = -1767272577989225979L;
/*  11:    */   final transient Logger logger;
/*  12:    */   
/*  13:    */   JdkLogger(Logger logger)
/*  14:    */   {
/*  15: 57 */     super(logger.getName());
/*  16: 58 */     this.logger = logger;
/*  17:    */   }
/*  18:    */   
/*  19:    */   public boolean isTraceEnabled()
/*  20:    */   {
/*  21: 68 */     return this.logger.isLoggable(Level.FINEST);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public void trace(String msg)
/*  25:    */   {
/*  26: 79 */     if (this.logger.isLoggable(Level.FINEST)) {
/*  27: 80 */       log(SELF, Level.FINEST, msg, null);
/*  28:    */     }
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void trace(String format, Object arg)
/*  32:    */   {
/*  33:100 */     if (this.logger.isLoggable(Level.FINEST))
/*  34:    */     {
/*  35:101 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  36:102 */       log(SELF, Level.FINEST, ft.getMessage(), ft.getThrowable());
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void trace(String format, Object argA, Object argB)
/*  41:    */   {
/*  42:124 */     if (this.logger.isLoggable(Level.FINEST))
/*  43:    */     {
/*  44:125 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  45:126 */       log(SELF, Level.FINEST, ft.getMessage(), ft.getThrowable());
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void trace(String format, Object... argArray)
/*  50:    */   {
/*  51:146 */     if (this.logger.isLoggable(Level.FINEST))
/*  52:    */     {
/*  53:147 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/*  54:148 */       log(SELF, Level.FINEST, ft.getMessage(), ft.getThrowable());
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void trace(String msg, Throwable t)
/*  59:    */   {
/*  60:162 */     if (this.logger.isLoggable(Level.FINEST)) {
/*  61:163 */       log(SELF, Level.FINEST, msg, t);
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean isDebugEnabled()
/*  66:    */   {
/*  67:174 */     return this.logger.isLoggable(Level.FINE);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void debug(String msg)
/*  71:    */   {
/*  72:185 */     if (this.logger.isLoggable(Level.FINE)) {
/*  73:186 */       log(SELF, Level.FINE, msg, null);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   public void debug(String format, Object arg)
/*  78:    */   {
/*  79:205 */     if (this.logger.isLoggable(Level.FINE))
/*  80:    */     {
/*  81:206 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  82:207 */       log(SELF, Level.FINE, ft.getMessage(), ft.getThrowable());
/*  83:    */     }
/*  84:    */   }
/*  85:    */   
/*  86:    */   public void debug(String format, Object argA, Object argB)
/*  87:    */   {
/*  88:229 */     if (this.logger.isLoggable(Level.FINE))
/*  89:    */     {
/*  90:230 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  91:231 */       log(SELF, Level.FINE, ft.getMessage(), ft.getThrowable());
/*  92:    */     }
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void debug(String format, Object... argArray)
/*  96:    */   {
/*  97:251 */     if (this.logger.isLoggable(Level.FINE))
/*  98:    */     {
/*  99:252 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 100:253 */       log(SELF, Level.FINE, ft.getMessage(), ft.getThrowable());
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void debug(String msg, Throwable t)
/* 105:    */   {
/* 106:267 */     if (this.logger.isLoggable(Level.FINE)) {
/* 107:268 */       log(SELF, Level.FINE, msg, t);
/* 108:    */     }
/* 109:    */   }
/* 110:    */   
/* 111:    */   public boolean isInfoEnabled()
/* 112:    */   {
/* 113:279 */     return this.logger.isLoggable(Level.INFO);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void info(String msg)
/* 117:    */   {
/* 118:290 */     if (this.logger.isLoggable(Level.INFO)) {
/* 119:291 */       log(SELF, Level.INFO, msg, null);
/* 120:    */     }
/* 121:    */   }
/* 122:    */   
/* 123:    */   public void info(String format, Object arg)
/* 124:    */   {
/* 125:310 */     if (this.logger.isLoggable(Level.INFO))
/* 126:    */     {
/* 127:311 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 128:312 */       log(SELF, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   public void info(String format, Object argA, Object argB)
/* 133:    */   {
/* 134:334 */     if (this.logger.isLoggable(Level.INFO))
/* 135:    */     {
/* 136:335 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 137:336 */       log(SELF, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   public void info(String format, Object... argArray)
/* 142:    */   {
/* 143:356 */     if (this.logger.isLoggable(Level.INFO))
/* 144:    */     {
/* 145:357 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 146:358 */       log(SELF, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 147:    */     }
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void info(String msg, Throwable t)
/* 151:    */   {
/* 152:373 */     if (this.logger.isLoggable(Level.INFO)) {
/* 153:374 */       log(SELF, Level.INFO, msg, t);
/* 154:    */     }
/* 155:    */   }
/* 156:    */   
/* 157:    */   public boolean isWarnEnabled()
/* 158:    */   {
/* 159:386 */     return this.logger.isLoggable(Level.WARNING);
/* 160:    */   }
/* 161:    */   
/* 162:    */   public void warn(String msg)
/* 163:    */   {
/* 164:397 */     if (this.logger.isLoggable(Level.WARNING)) {
/* 165:398 */       log(SELF, Level.WARNING, msg, null);
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   public void warn(String format, Object arg)
/* 170:    */   {
/* 171:418 */     if (this.logger.isLoggable(Level.WARNING))
/* 172:    */     {
/* 173:419 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 174:420 */       log(SELF, Level.WARNING, ft.getMessage(), ft.getThrowable());
/* 175:    */     }
/* 176:    */   }
/* 177:    */   
/* 178:    */   public void warn(String format, Object argA, Object argB)
/* 179:    */   {
/* 180:442 */     if (this.logger.isLoggable(Level.WARNING))
/* 181:    */     {
/* 182:443 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 183:444 */       log(SELF, Level.WARNING, ft.getMessage(), ft.getThrowable());
/* 184:    */     }
/* 185:    */   }
/* 186:    */   
/* 187:    */   public void warn(String format, Object... argArray)
/* 188:    */   {
/* 189:464 */     if (this.logger.isLoggable(Level.WARNING))
/* 190:    */     {
/* 191:465 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 192:466 */       log(SELF, Level.WARNING, ft.getMessage(), ft.getThrowable());
/* 193:    */     }
/* 194:    */   }
/* 195:    */   
/* 196:    */   public void warn(String msg, Throwable t)
/* 197:    */   {
/* 198:481 */     if (this.logger.isLoggable(Level.WARNING)) {
/* 199:482 */       log(SELF, Level.WARNING, msg, t);
/* 200:    */     }
/* 201:    */   }
/* 202:    */   
/* 203:    */   public boolean isErrorEnabled()
/* 204:    */   {
/* 205:493 */     return this.logger.isLoggable(Level.SEVERE);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public void error(String msg)
/* 209:    */   {
/* 210:504 */     if (this.logger.isLoggable(Level.SEVERE)) {
/* 211:505 */       log(SELF, Level.SEVERE, msg, null);
/* 212:    */     }
/* 213:    */   }
/* 214:    */   
/* 215:    */   public void error(String format, Object arg)
/* 216:    */   {
/* 217:525 */     if (this.logger.isLoggable(Level.SEVERE))
/* 218:    */     {
/* 219:526 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 220:527 */       log(SELF, Level.SEVERE, ft.getMessage(), ft.getThrowable());
/* 221:    */     }
/* 222:    */   }
/* 223:    */   
/* 224:    */   public void error(String format, Object argA, Object argB)
/* 225:    */   {
/* 226:549 */     if (this.logger.isLoggable(Level.SEVERE))
/* 227:    */     {
/* 228:550 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 229:551 */       log(SELF, Level.SEVERE, ft.getMessage(), ft.getThrowable());
/* 230:    */     }
/* 231:    */   }
/* 232:    */   
/* 233:    */   public void error(String format, Object... arguments)
/* 234:    */   {
/* 235:571 */     if (this.logger.isLoggable(Level.SEVERE))
/* 236:    */     {
/* 237:572 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 238:573 */       log(SELF, Level.SEVERE, ft.getMessage(), ft.getThrowable());
/* 239:    */     }
/* 240:    */   }
/* 241:    */   
/* 242:    */   public void error(String msg, Throwable t)
/* 243:    */   {
/* 244:588 */     if (this.logger.isLoggable(Level.SEVERE)) {
/* 245:589 */       log(SELF, Level.SEVERE, msg, t);
/* 246:    */     }
/* 247:    */   }
/* 248:    */   
/* 249:    */   private void log(String callerFQCN, Level level, String msg, Throwable t)
/* 250:    */   {
/* 251:602 */     LogRecord record = new LogRecord(level, msg);
/* 252:603 */     record.setLoggerName(name());
/* 253:604 */     record.setThrown(t);
/* 254:605 */     fillCallerData(callerFQCN, record);
/* 255:606 */     this.logger.log(record);
/* 256:    */   }
/* 257:    */   
/* 258:609 */   static final String SELF = JdkLogger.class.getName();
/* 259:610 */   static final String SUPER = AbstractInternalLogger.class.getName();
/* 260:    */   
/* 261:    */   private static void fillCallerData(String callerFQCN, LogRecord record)
/* 262:    */   {
/* 263:619 */     StackTraceElement[] steArray = new Throwable().getStackTrace();
/* 264:    */     
/* 265:621 */     int selfIndex = -1;
/* 266:622 */     for (int i = 0; i < steArray.length; i++)
/* 267:    */     {
/* 268:623 */       String className = steArray[i].getClassName();
/* 269:624 */       if ((className.equals(callerFQCN)) || (className.equals(SUPER)))
/* 270:    */       {
/* 271:625 */         selfIndex = i;
/* 272:626 */         break;
/* 273:    */       }
/* 274:    */     }
/* 275:630 */     int found = -1;
/* 276:631 */     for (int i = selfIndex + 1; i < steArray.length; i++)
/* 277:    */     {
/* 278:632 */       String className = steArray[i].getClassName();
/* 279:633 */       if ((!className.equals(callerFQCN)) && (!className.equals(SUPER)))
/* 280:    */       {
/* 281:634 */         found = i;
/* 282:635 */         break;
/* 283:    */       }
/* 284:    */     }
/* 285:639 */     if (found != -1)
/* 286:    */     {
/* 287:640 */       StackTraceElement ste = steArray[found];
/* 288:    */       
/* 289:    */ 
/* 290:643 */       record.setSourceClassName(ste.getClassName());
/* 291:644 */       record.setSourceMethodName(ste.getMethodName());
/* 292:    */     }
/* 293:    */   }
/* 294:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.JdkLogger
 * JD-Core Version:    0.7.0.1
 */