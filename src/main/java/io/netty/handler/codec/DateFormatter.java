/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.util.AsciiString;
/*   4:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   5:    */ import io.netty.util.internal.ObjectUtil;
/*   6:    */ import java.util.BitSet;
/*   7:    */ import java.util.Date;
/*   8:    */ import java.util.GregorianCalendar;
/*   9:    */ import java.util.TimeZone;
/*  10:    */ 
/*  11:    */ public final class DateFormatter
/*  12:    */ {
/*  13: 48 */   private static final BitSet DELIMITERS = new BitSet();
/*  14:    */   
/*  15:    */   static
/*  16:    */   {
/*  17: 50 */     DELIMITERS.set(9);
/*  18: 51 */     for (char c = ' '; c <= '/'; c = (char)(c + '\001')) {
/*  19: 52 */       DELIMITERS.set(c);
/*  20:    */     }
/*  21: 54 */     for (char c = ';'; c <= '@'; c = (char)(c + '\001')) {
/*  22: 55 */       DELIMITERS.set(c);
/*  23:    */     }
/*  24: 57 */     for (char c = '['; c <= '`'; c = (char)(c + '\001')) {
/*  25: 58 */       DELIMITERS.set(c);
/*  26:    */     }
/*  27: 60 */     for (char c = '{'; c <= '~'; c = (char)(c + '\001')) {
/*  28: 61 */       DELIMITERS.set(c);
/*  29:    */     }
/*  30:    */   }
/*  31:    */   
/*  32: 65 */   private static final String[] DAY_OF_WEEK_TO_SHORT_NAME = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
/*  33: 68 */   private static final String[] CALENDAR_MONTH_TO_SHORT_NAME = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
/*  34: 71 */   private static final FastThreadLocal<DateFormatter> INSTANCES = new FastThreadLocal()
/*  35:    */   {
/*  36:    */     protected DateFormatter initialValue()
/*  37:    */     {
/*  38: 75 */       return new DateFormatter(null);
/*  39:    */     }
/*  40:    */   };
/*  41:    */   
/*  42:    */   public static Date parseHttpDate(CharSequence txt)
/*  43:    */   {
/*  44: 85 */     return parseHttpDate(txt, 0, txt.length());
/*  45:    */   }
/*  46:    */   
/*  47:    */   public static Date parseHttpDate(CharSequence txt, int start, int end)
/*  48:    */   {
/*  49: 96 */     int length = end - start;
/*  50: 97 */     if (length == 0) {
/*  51: 98 */       return null;
/*  52:    */     }
/*  53: 99 */     if (length < 0) {
/*  54:100 */       throw new IllegalArgumentException("Can't have end < start");
/*  55:    */     }
/*  56:101 */     if (length > 64) {
/*  57:102 */       throw new IllegalArgumentException("Can't parse more than 64 chars,looks like a user error or a malformed header");
/*  58:    */     }
/*  59:105 */     return formatter().parse0((CharSequence)ObjectUtil.checkNotNull(txt, "txt"), start, end);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public static String format(Date date)
/*  63:    */   {
/*  64:114 */     return formatter().format0((Date)ObjectUtil.checkNotNull(date, "date"));
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static StringBuilder append(Date date, StringBuilder sb)
/*  68:    */   {
/*  69:124 */     return formatter().append0((Date)ObjectUtil.checkNotNull(date, "date"), (StringBuilder)ObjectUtil.checkNotNull(sb, "sb"));
/*  70:    */   }
/*  71:    */   
/*  72:    */   private static DateFormatter formatter()
/*  73:    */   {
/*  74:128 */     DateFormatter formatter = (DateFormatter)INSTANCES.get();
/*  75:129 */     formatter.reset();
/*  76:130 */     return formatter;
/*  77:    */   }
/*  78:    */   
/*  79:    */   private static boolean isDelim(char c)
/*  80:    */   {
/*  81:135 */     return DELIMITERS.get(c);
/*  82:    */   }
/*  83:    */   
/*  84:    */   private static boolean isDigit(char c)
/*  85:    */   {
/*  86:139 */     return (c >= '0') && (c <= '9');
/*  87:    */   }
/*  88:    */   
/*  89:    */   private static int getNumericalValue(char c)
/*  90:    */   {
/*  91:143 */     return c - '0';
/*  92:    */   }
/*  93:    */   
/*  94:146 */   private final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
/*  95:147 */   private final StringBuilder sb = new StringBuilder(29);
/*  96:    */   private boolean timeFound;
/*  97:    */   private int hours;
/*  98:    */   private int minutes;
/*  99:    */   private int seconds;
/* 100:    */   private boolean dayOfMonthFound;
/* 101:    */   private int dayOfMonth;
/* 102:    */   private boolean monthFound;
/* 103:    */   private int month;
/* 104:    */   private boolean yearFound;
/* 105:    */   private int year;
/* 106:    */   
/* 107:    */   private DateFormatter()
/* 108:    */   {
/* 109:160 */     reset();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public void reset()
/* 113:    */   {
/* 114:164 */     this.timeFound = false;
/* 115:165 */     this.hours = -1;
/* 116:166 */     this.minutes = -1;
/* 117:167 */     this.seconds = -1;
/* 118:168 */     this.dayOfMonthFound = false;
/* 119:169 */     this.dayOfMonth = -1;
/* 120:170 */     this.monthFound = false;
/* 121:171 */     this.month = -1;
/* 122:172 */     this.yearFound = false;
/* 123:173 */     this.year = -1;
/* 124:174 */     this.cal.clear();
/* 125:175 */     this.sb.setLength(0);
/* 126:    */   }
/* 127:    */   
/* 128:    */   private boolean tryParseTime(CharSequence txt, int tokenStart, int tokenEnd)
/* 129:    */   {
/* 130:179 */     int len = tokenEnd - tokenStart;
/* 131:182 */     if ((len < 5) || (len > 8)) {
/* 132:183 */       return false;
/* 133:    */     }
/* 134:186 */     int localHours = -1;
/* 135:187 */     int localMinutes = -1;
/* 136:188 */     int localSeconds = -1;
/* 137:189 */     int currentPartNumber = 0;
/* 138:190 */     int currentPartValue = 0;
/* 139:191 */     int numDigits = 0;
/* 140:193 */     for (int i = tokenStart; i < tokenEnd; i++)
/* 141:    */     {
/* 142:194 */       char c = txt.charAt(i);
/* 143:195 */       if (isDigit(c))
/* 144:    */       {
/* 145:196 */         currentPartValue = currentPartValue * 10 + getNumericalValue(c);
/* 146:197 */         numDigits++;
/* 147:197 */         if (numDigits > 2) {
/* 148:198 */           return false;
/* 149:    */         }
/* 150:    */       }
/* 151:200 */       else if (c == ':')
/* 152:    */       {
/* 153:201 */         if (numDigits == 0) {
/* 154:203 */           return false;
/* 155:    */         }
/* 156:205 */         switch (currentPartNumber)
/* 157:    */         {
/* 158:    */         case 0: 
/* 159:208 */           localHours = currentPartValue;
/* 160:209 */           break;
/* 161:    */         case 1: 
/* 162:212 */           localMinutes = currentPartValue;
/* 163:213 */           break;
/* 164:    */         default: 
/* 165:216 */           return false;
/* 166:    */         }
/* 167:218 */         currentPartValue = 0;
/* 168:219 */         currentPartNumber++;
/* 169:220 */         numDigits = 0;
/* 170:    */       }
/* 171:    */       else
/* 172:    */       {
/* 173:223 */         return false;
/* 174:    */       }
/* 175:    */     }
/* 176:227 */     if (numDigits > 0) {
/* 177:229 */       localSeconds = currentPartValue;
/* 178:    */     }
/* 179:232 */     if ((localHours >= 0) && (localMinutes >= 0) && (localSeconds >= 0))
/* 180:    */     {
/* 181:233 */       this.hours = localHours;
/* 182:234 */       this.minutes = localMinutes;
/* 183:235 */       this.seconds = localSeconds;
/* 184:236 */       return true;
/* 185:    */     }
/* 186:239 */     return false;
/* 187:    */   }
/* 188:    */   
/* 189:    */   private boolean tryParseDayOfMonth(CharSequence txt, int tokenStart, int tokenEnd)
/* 190:    */   {
/* 191:243 */     int len = tokenEnd - tokenStart;
/* 192:245 */     if (len == 1)
/* 193:    */     {
/* 194:246 */       char c0 = txt.charAt(tokenStart);
/* 195:247 */       if (isDigit(c0))
/* 196:    */       {
/* 197:248 */         this.dayOfMonth = getNumericalValue(c0);
/* 198:249 */         return true;
/* 199:    */       }
/* 200:    */     }
/* 201:252 */     else if (len == 2)
/* 202:    */     {
/* 203:253 */       char c0 = txt.charAt(tokenStart);
/* 204:254 */       char c1 = txt.charAt(tokenStart + 1);
/* 205:255 */       if ((isDigit(c0)) && (isDigit(c1)))
/* 206:    */       {
/* 207:256 */         this.dayOfMonth = (getNumericalValue(c0) * 10 + getNumericalValue(c1));
/* 208:257 */         return true;
/* 209:    */       }
/* 210:    */     }
/* 211:261 */     return false;
/* 212:    */   }
/* 213:    */   
/* 214:    */   private static boolean matchMonth(String month, CharSequence txt, int tokenStart)
/* 215:    */   {
/* 216:265 */     return AsciiString.regionMatchesAscii(month, true, 0, txt, tokenStart, 3);
/* 217:    */   }
/* 218:    */   
/* 219:    */   private boolean tryParseMonth(CharSequence txt, int tokenStart, int tokenEnd)
/* 220:    */   {
/* 221:269 */     int len = tokenEnd - tokenStart;
/* 222:271 */     if (len != 3) {
/* 223:272 */       return false;
/* 224:    */     }
/* 225:275 */     if (matchMonth("Jan", txt, tokenStart)) {
/* 226:276 */       this.month = 0;
/* 227:277 */     } else if (matchMonth("Feb", txt, tokenStart)) {
/* 228:278 */       this.month = 1;
/* 229:279 */     } else if (matchMonth("Mar", txt, tokenStart)) {
/* 230:280 */       this.month = 2;
/* 231:281 */     } else if (matchMonth("Apr", txt, tokenStart)) {
/* 232:282 */       this.month = 3;
/* 233:283 */     } else if (matchMonth("May", txt, tokenStart)) {
/* 234:284 */       this.month = 4;
/* 235:285 */     } else if (matchMonth("Jun", txt, tokenStart)) {
/* 236:286 */       this.month = 5;
/* 237:287 */     } else if (matchMonth("Jul", txt, tokenStart)) {
/* 238:288 */       this.month = 6;
/* 239:289 */     } else if (matchMonth("Aug", txt, tokenStart)) {
/* 240:290 */       this.month = 7;
/* 241:291 */     } else if (matchMonth("Sep", txt, tokenStart)) {
/* 242:292 */       this.month = 8;
/* 243:293 */     } else if (matchMonth("Oct", txt, tokenStart)) {
/* 244:294 */       this.month = 9;
/* 245:295 */     } else if (matchMonth("Nov", txt, tokenStart)) {
/* 246:296 */       this.month = 10;
/* 247:297 */     } else if (matchMonth("Dec", txt, tokenStart)) {
/* 248:298 */       this.month = 11;
/* 249:    */     } else {
/* 250:300 */       return false;
/* 251:    */     }
/* 252:303 */     return true;
/* 253:    */   }
/* 254:    */   
/* 255:    */   private boolean tryParseYear(CharSequence txt, int tokenStart, int tokenEnd)
/* 256:    */   {
/* 257:307 */     int len = tokenEnd - tokenStart;
/* 258:309 */     if (len == 2)
/* 259:    */     {
/* 260:310 */       char c0 = txt.charAt(tokenStart);
/* 261:311 */       char c1 = txt.charAt(tokenStart + 1);
/* 262:312 */       if ((isDigit(c0)) && (isDigit(c1)))
/* 263:    */       {
/* 264:313 */         this.year = (getNumericalValue(c0) * 10 + getNumericalValue(c1));
/* 265:314 */         return true;
/* 266:    */       }
/* 267:    */     }
/* 268:317 */     else if (len == 4)
/* 269:    */     {
/* 270:318 */       char c0 = txt.charAt(tokenStart);
/* 271:319 */       char c1 = txt.charAt(tokenStart + 1);
/* 272:320 */       char c2 = txt.charAt(tokenStart + 2);
/* 273:321 */       char c3 = txt.charAt(tokenStart + 3);
/* 274:322 */       if ((isDigit(c0)) && (isDigit(c1)) && (isDigit(c2)) && (isDigit(c3)))
/* 275:    */       {
/* 276:326 */         this.year = (getNumericalValue(c0) * 1000 + getNumericalValue(c1) * 100 + getNumericalValue(c2) * 10 + getNumericalValue(c3));
/* 277:327 */         return true;
/* 278:    */       }
/* 279:    */     }
/* 280:331 */     return false;
/* 281:    */   }
/* 282:    */   
/* 283:    */   private boolean parseToken(CharSequence txt, int tokenStart, int tokenEnd)
/* 284:    */   {
/* 285:336 */     if (!this.timeFound)
/* 286:    */     {
/* 287:337 */       this.timeFound = tryParseTime(txt, tokenStart, tokenEnd);
/* 288:338 */       if (this.timeFound) {
/* 289:339 */         return (this.dayOfMonthFound) && (this.monthFound) && (this.yearFound);
/* 290:    */       }
/* 291:    */     }
/* 292:343 */     if (!this.dayOfMonthFound)
/* 293:    */     {
/* 294:344 */       this.dayOfMonthFound = tryParseDayOfMonth(txt, tokenStart, tokenEnd);
/* 295:345 */       if (this.dayOfMonthFound) {
/* 296:346 */         return (this.timeFound) && (this.monthFound) && (this.yearFound);
/* 297:    */       }
/* 298:    */     }
/* 299:350 */     if (!this.monthFound)
/* 300:    */     {
/* 301:351 */       this.monthFound = tryParseMonth(txt, tokenStart, tokenEnd);
/* 302:352 */       if (this.monthFound) {
/* 303:353 */         return (this.timeFound) && (this.dayOfMonthFound) && (this.yearFound);
/* 304:    */       }
/* 305:    */     }
/* 306:357 */     if (!this.yearFound) {
/* 307:358 */       this.yearFound = tryParseYear(txt, tokenStart, tokenEnd);
/* 308:    */     }
/* 309:360 */     return (this.timeFound) && (this.dayOfMonthFound) && (this.monthFound) && (this.yearFound);
/* 310:    */   }
/* 311:    */   
/* 312:    */   private Date parse0(CharSequence txt, int start, int end)
/* 313:    */   {
/* 314:364 */     boolean allPartsFound = parse1(txt, start, end);
/* 315:365 */     return (allPartsFound) && (normalizeAndValidate()) ? computeDate() : null;
/* 316:    */   }
/* 317:    */   
/* 318:    */   private boolean parse1(CharSequence txt, int start, int end)
/* 319:    */   {
/* 320:370 */     int tokenStart = -1;
/* 321:372 */     for (int i = start; i < end; i++)
/* 322:    */     {
/* 323:373 */       char c = txt.charAt(i);
/* 324:375 */       if (isDelim(c))
/* 325:    */       {
/* 326:376 */         if (tokenStart != -1)
/* 327:    */         {
/* 328:378 */           if (parseToken(txt, tokenStart, i)) {
/* 329:379 */             return true;
/* 330:    */           }
/* 331:381 */           tokenStart = -1;
/* 332:    */         }
/* 333:    */       }
/* 334:383 */       else if (tokenStart == -1) {
/* 335:385 */         tokenStart = i;
/* 336:    */       }
/* 337:    */     }
/* 338:390 */     return (tokenStart != -1) && (parseToken(txt, tokenStart, txt.length()));
/* 339:    */   }
/* 340:    */   
/* 341:    */   private boolean normalizeAndValidate()
/* 342:    */   {
/* 343:394 */     if ((this.dayOfMonth < 1) || (this.dayOfMonth > 31) || (this.hours > 23) || (this.minutes > 59) || (this.seconds > 59)) {
/* 344:399 */       return false;
/* 345:    */     }
/* 346:402 */     if ((this.year >= 70) && (this.year <= 99)) {
/* 347:403 */       this.year += 1900;
/* 348:404 */     } else if ((this.year >= 0) && (this.year < 70)) {
/* 349:405 */       this.year += 2000;
/* 350:406 */     } else if (this.year < 1601) {
/* 351:408 */       return false;
/* 352:    */     }
/* 353:410 */     return true;
/* 354:    */   }
/* 355:    */   
/* 356:    */   private Date computeDate()
/* 357:    */   {
/* 358:414 */     this.cal.set(5, this.dayOfMonth);
/* 359:415 */     this.cal.set(2, this.month);
/* 360:416 */     this.cal.set(1, this.year);
/* 361:417 */     this.cal.set(11, this.hours);
/* 362:418 */     this.cal.set(12, this.minutes);
/* 363:419 */     this.cal.set(13, this.seconds);
/* 364:420 */     return this.cal.getTime();
/* 365:    */   }
/* 366:    */   
/* 367:    */   private String format0(Date date)
/* 368:    */   {
/* 369:424 */     append0(date, this.sb);
/* 370:425 */     return this.sb.toString();
/* 371:    */   }
/* 372:    */   
/* 373:    */   private StringBuilder append0(Date date, StringBuilder sb)
/* 374:    */   {
/* 375:429 */     this.cal.setTime(date);
/* 376:    */     
/* 377:431 */     sb.append(DAY_OF_WEEK_TO_SHORT_NAME[(this.cal.get(7) - 1)]).append(", ");
/* 378:432 */     sb.append(this.cal.get(5)).append(' ');
/* 379:433 */     sb.append(CALENDAR_MONTH_TO_SHORT_NAME[this.cal.get(2)]).append(' ');
/* 380:434 */     sb.append(this.cal.get(1)).append(' ');
/* 381:435 */     appendZeroLeftPadded(this.cal.get(11), sb).append(':');
/* 382:436 */     appendZeroLeftPadded(this.cal.get(12), sb).append(':');
/* 383:437 */     return appendZeroLeftPadded(this.cal.get(13), sb).append(" GMT");
/* 384:    */   }
/* 385:    */   
/* 386:    */   private static StringBuilder appendZeroLeftPadded(int value, StringBuilder sb)
/* 387:    */   {
/* 388:441 */     if (value < 10) {
/* 389:442 */       sb.append('0');
/* 390:    */     }
/* 391:444 */     return sb.append(value);
/* 392:    */   }
/* 393:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.DateFormatter
 * JD-Core Version:    0.7.0.1
 */