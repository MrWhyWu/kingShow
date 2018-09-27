/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.io.ObjectStreamException;
/*   5:    */ import java.io.Serializable;
/*   6:    */ 
/*   7:    */ public abstract class AbstractInternalLogger
/*   8:    */   implements InternalLogger, Serializable
/*   9:    */ {
/*  10:    */   private static final long serialVersionUID = -6382972526573193470L;
/*  11:    */   private static final String EXCEPTION_MESSAGE = "Unexpected exception:";
/*  12:    */   private final String name;
/*  13:    */   
/*  14:    */   protected AbstractInternalLogger(String name)
/*  15:    */   {
/*  16: 40 */     if (name == null) {
/*  17: 41 */       throw new NullPointerException("name");
/*  18:    */     }
/*  19: 43 */     this.name = name;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public String name()
/*  23:    */   {
/*  24: 48 */     return this.name;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public boolean isEnabled(InternalLogLevel level)
/*  28:    */   {
/*  29: 53 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  30:    */     {
/*  31:    */     case 1: 
/*  32: 55 */       return isTraceEnabled();
/*  33:    */     case 2: 
/*  34: 57 */       return isDebugEnabled();
/*  35:    */     case 3: 
/*  36: 59 */       return isInfoEnabled();
/*  37:    */     case 4: 
/*  38: 61 */       return isWarnEnabled();
/*  39:    */     case 5: 
/*  40: 63 */       return isErrorEnabled();
/*  41:    */     }
/*  42: 65 */     throw new Error();
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void trace(Throwable t)
/*  46:    */   {
/*  47: 71 */     trace("Unexpected exception:", t);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void debug(Throwable t)
/*  51:    */   {
/*  52: 76 */     debug("Unexpected exception:", t);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void info(Throwable t)
/*  56:    */   {
/*  57: 81 */     info("Unexpected exception:", t);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void warn(Throwable t)
/*  61:    */   {
/*  62: 86 */     warn("Unexpected exception:", t);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void error(Throwable t)
/*  66:    */   {
/*  67: 91 */     error("Unexpected exception:", t);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void log(InternalLogLevel level, String msg, Throwable cause)
/*  71:    */   {
/*  72: 96 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  73:    */     {
/*  74:    */     case 1: 
/*  75: 98 */       trace(msg, cause);
/*  76: 99 */       break;
/*  77:    */     case 2: 
/*  78:101 */       debug(msg, cause);
/*  79:102 */       break;
/*  80:    */     case 3: 
/*  81:104 */       info(msg, cause);
/*  82:105 */       break;
/*  83:    */     case 4: 
/*  84:107 */       warn(msg, cause);
/*  85:108 */       break;
/*  86:    */     case 5: 
/*  87:110 */       error(msg, cause);
/*  88:111 */       break;
/*  89:    */     default: 
/*  90:113 */       throw new Error();
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void log(InternalLogLevel level, Throwable cause)
/*  95:    */   {
/*  96:119 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  97:    */     {
/*  98:    */     case 1: 
/*  99:121 */       trace(cause);
/* 100:122 */       break;
/* 101:    */     case 2: 
/* 102:124 */       debug(cause);
/* 103:125 */       break;
/* 104:    */     case 3: 
/* 105:127 */       info(cause);
/* 106:128 */       break;
/* 107:    */     case 4: 
/* 108:130 */       warn(cause);
/* 109:131 */       break;
/* 110:    */     case 5: 
/* 111:133 */       error(cause);
/* 112:134 */       break;
/* 113:    */     default: 
/* 114:136 */       throw new Error();
/* 115:    */     }
/* 116:    */   }
/* 117:    */   
/* 118:    */   public void log(InternalLogLevel level, String msg)
/* 119:    */   {
/* 120:142 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/* 121:    */     {
/* 122:    */     case 1: 
/* 123:144 */       trace(msg);
/* 124:145 */       break;
/* 125:    */     case 2: 
/* 126:147 */       debug(msg);
/* 127:148 */       break;
/* 128:    */     case 3: 
/* 129:150 */       info(msg);
/* 130:151 */       break;
/* 131:    */     case 4: 
/* 132:153 */       warn(msg);
/* 133:154 */       break;
/* 134:    */     case 5: 
/* 135:156 */       error(msg);
/* 136:157 */       break;
/* 137:    */     default: 
/* 138:159 */       throw new Error();
/* 139:    */     }
/* 140:    */   }
/* 141:    */   
/* 142:    */   public void log(InternalLogLevel level, String format, Object arg)
/* 143:    */   {
/* 144:165 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/* 145:    */     {
/* 146:    */     case 1: 
/* 147:167 */       trace(format, arg);
/* 148:168 */       break;
/* 149:    */     case 2: 
/* 150:170 */       debug(format, arg);
/* 151:171 */       break;
/* 152:    */     case 3: 
/* 153:173 */       info(format, arg);
/* 154:174 */       break;
/* 155:    */     case 4: 
/* 156:176 */       warn(format, arg);
/* 157:177 */       break;
/* 158:    */     case 5: 
/* 159:179 */       error(format, arg);
/* 160:180 */       break;
/* 161:    */     default: 
/* 162:182 */       throw new Error();
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   public void log(InternalLogLevel level, String format, Object argA, Object argB)
/* 167:    */   {
/* 168:188 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/* 169:    */     {
/* 170:    */     case 1: 
/* 171:190 */       trace(format, argA, argB);
/* 172:191 */       break;
/* 173:    */     case 2: 
/* 174:193 */       debug(format, argA, argB);
/* 175:194 */       break;
/* 176:    */     case 3: 
/* 177:196 */       info(format, argA, argB);
/* 178:197 */       break;
/* 179:    */     case 4: 
/* 180:199 */       warn(format, argA, argB);
/* 181:200 */       break;
/* 182:    */     case 5: 
/* 183:202 */       error(format, argA, argB);
/* 184:203 */       break;
/* 185:    */     default: 
/* 186:205 */       throw new Error();
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   public void log(InternalLogLevel level, String format, Object... arguments)
/* 191:    */   {
/* 192:211 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/* 193:    */     {
/* 194:    */     case 1: 
/* 195:213 */       trace(format, arguments);
/* 196:214 */       break;
/* 197:    */     case 2: 
/* 198:216 */       debug(format, arguments);
/* 199:217 */       break;
/* 200:    */     case 3: 
/* 201:219 */       info(format, arguments);
/* 202:220 */       break;
/* 203:    */     case 4: 
/* 204:222 */       warn(format, arguments);
/* 205:223 */       break;
/* 206:    */     case 5: 
/* 207:225 */       error(format, arguments);
/* 208:226 */       break;
/* 209:    */     default: 
/* 210:228 */       throw new Error();
/* 211:    */     }
/* 212:    */   }
/* 213:    */   
/* 214:    */   protected Object readResolve()
/* 215:    */     throws ObjectStreamException
/* 216:    */   {
/* 217:233 */     return InternalLoggerFactory.getInstance(name());
/* 218:    */   }
/* 219:    */   
/* 220:    */   public String toString()
/* 221:    */   {
/* 222:238 */     return StringUtil.simpleClassName(this) + '(' + name() + ')';
/* 223:    */   }
/* 224:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.logging.AbstractInternalLogger
 * JD-Core Version:    0.7.0.1
 */