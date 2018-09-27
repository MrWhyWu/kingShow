/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import org.apache.commons.logging.Log;
/*   4:    */ 
/*   5:    */ @Deprecated
/*   6:    */ class CommonsLogger
/*   7:    */   extends AbstractInternalLogger
/*   8:    */ {
/*   9:    */   private static final long serialVersionUID = 8647838678388394885L;
/*  10:    */   private final transient Log logger;
/*  11:    */   
/*  12:    */   CommonsLogger(Log logger, String name)
/*  13:    */   {
/*  14: 59 */     super(name);
/*  15: 60 */     if (logger == null) {
/*  16: 61 */       throw new NullPointerException("logger");
/*  17:    */     }
/*  18: 63 */     this.logger = logger;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public boolean isTraceEnabled()
/*  22:    */   {
/*  23: 72 */     return this.logger.isTraceEnabled();
/*  24:    */   }
/*  25:    */   
/*  26:    */   public void trace(String msg)
/*  27:    */   {
/*  28: 83 */     this.logger.trace(msg);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void trace(String format, Object arg)
/*  32:    */   {
/*  33:102 */     if (this.logger.isTraceEnabled())
/*  34:    */     {
/*  35:103 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  36:104 */       this.logger.trace(ft.getMessage(), ft.getThrowable());
/*  37:    */     }
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void trace(String format, Object argA, Object argB)
/*  41:    */   {
/*  42:126 */     if (this.logger.isTraceEnabled())
/*  43:    */     {
/*  44:127 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  45:128 */       this.logger.trace(ft.getMessage(), ft.getThrowable());
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void trace(String format, Object... arguments)
/*  50:    */   {
/*  51:146 */     if (this.logger.isTraceEnabled())
/*  52:    */     {
/*  53:147 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/*  54:148 */       this.logger.trace(ft.getMessage(), ft.getThrowable());
/*  55:    */     }
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void trace(String msg, Throwable t)
/*  59:    */   {
/*  60:163 */     this.logger.trace(msg, t);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public boolean isDebugEnabled()
/*  64:    */   {
/*  65:172 */     return this.logger.isDebugEnabled();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public void debug(String msg)
/*  69:    */   {
/*  70:185 */     this.logger.debug(msg);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void debug(String format, Object arg)
/*  74:    */   {
/*  75:204 */     if (this.logger.isDebugEnabled())
/*  76:    */     {
/*  77:205 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  78:206 */       this.logger.debug(ft.getMessage(), ft.getThrowable());
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void debug(String format, Object argA, Object argB)
/*  83:    */   {
/*  84:228 */     if (this.logger.isDebugEnabled())
/*  85:    */     {
/*  86:229 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  87:230 */       this.logger.debug(ft.getMessage(), ft.getThrowable());
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void debug(String format, Object... arguments)
/*  92:    */   {
/*  93:248 */     if (this.logger.isDebugEnabled())
/*  94:    */     {
/*  95:249 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/*  96:250 */       this.logger.debug(ft.getMessage(), ft.getThrowable());
/*  97:    */     }
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void debug(String msg, Throwable t)
/* 101:    */   {
/* 102:265 */     this.logger.debug(msg, t);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isInfoEnabled()
/* 106:    */   {
/* 107:274 */     return this.logger.isInfoEnabled();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public void info(String msg)
/* 111:    */   {
/* 112:285 */     this.logger.info(msg);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public void info(String format, Object arg)
/* 116:    */   {
/* 117:305 */     if (this.logger.isInfoEnabled())
/* 118:    */     {
/* 119:306 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 120:307 */       this.logger.info(ft.getMessage(), ft.getThrowable());
/* 121:    */     }
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void info(String format, Object argA, Object argB)
/* 125:    */   {
/* 126:328 */     if (this.logger.isInfoEnabled())
/* 127:    */     {
/* 128:329 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 129:330 */       this.logger.info(ft.getMessage(), ft.getThrowable());
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   public void info(String format, Object... arguments)
/* 134:    */   {
/* 135:348 */     if (this.logger.isInfoEnabled())
/* 136:    */     {
/* 137:349 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 138:350 */       this.logger.info(ft.getMessage(), ft.getThrowable());
/* 139:    */     }
/* 140:    */   }
/* 141:    */   
/* 142:    */   public void info(String msg, Throwable t)
/* 143:    */   {
/* 144:365 */     this.logger.info(msg, t);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public boolean isWarnEnabled()
/* 148:    */   {
/* 149:374 */     return this.logger.isWarnEnabled();
/* 150:    */   }
/* 151:    */   
/* 152:    */   public void warn(String msg)
/* 153:    */   {
/* 154:385 */     this.logger.warn(msg);
/* 155:    */   }
/* 156:    */   
/* 157:    */   public void warn(String format, Object arg)
/* 158:    */   {
/* 159:404 */     if (this.logger.isWarnEnabled())
/* 160:    */     {
/* 161:405 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 162:406 */       this.logger.warn(ft.getMessage(), ft.getThrowable());
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   public void warn(String format, Object argA, Object argB)
/* 167:    */   {
/* 168:428 */     if (this.logger.isWarnEnabled())
/* 169:    */     {
/* 170:429 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 171:430 */       this.logger.warn(ft.getMessage(), ft.getThrowable());
/* 172:    */     }
/* 173:    */   }
/* 174:    */   
/* 175:    */   public void warn(String format, Object... arguments)
/* 176:    */   {
/* 177:448 */     if (this.logger.isWarnEnabled())
/* 178:    */     {
/* 179:449 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 180:450 */       this.logger.warn(ft.getMessage(), ft.getThrowable());
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   public void warn(String msg, Throwable t)
/* 185:    */   {
/* 186:466 */     this.logger.warn(msg, t);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public boolean isErrorEnabled()
/* 190:    */   {
/* 191:475 */     return this.logger.isErrorEnabled();
/* 192:    */   }
/* 193:    */   
/* 194:    */   public void error(String msg)
/* 195:    */   {
/* 196:486 */     this.logger.error(msg);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public void error(String format, Object arg)
/* 200:    */   {
/* 201:505 */     if (this.logger.isErrorEnabled())
/* 202:    */     {
/* 203:506 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 204:507 */       this.logger.error(ft.getMessage(), ft.getThrowable());
/* 205:    */     }
/* 206:    */   }
/* 207:    */   
/* 208:    */   public void error(String format, Object argA, Object argB)
/* 209:    */   {
/* 210:529 */     if (this.logger.isErrorEnabled())
/* 211:    */     {
/* 212:530 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 213:531 */       this.logger.error(ft.getMessage(), ft.getThrowable());
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   public void error(String format, Object... arguments)
/* 218:    */   {
/* 219:549 */     if (this.logger.isErrorEnabled())
/* 220:    */     {
/* 221:550 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 222:551 */       this.logger.error(ft.getMessage(), ft.getThrowable());
/* 223:    */     }
/* 224:    */   }
/* 225:    */   
/* 226:    */   public void error(String msg, Throwable t)
/* 227:    */   {
/* 228:566 */     this.logger.error(msg, t);
/* 229:    */   }
/* 230:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.CommonsLogger
 * JD-Core Version:    0.7.0.1
 */