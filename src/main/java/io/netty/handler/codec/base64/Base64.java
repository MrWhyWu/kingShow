/*   1:    */ package io.netty.handler.codec.base64;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.util.ByteProcessor;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import java.nio.ByteOrder;
/*   8:    */ 
/*   9:    */ public final class Base64
/*  10:    */ {
/*  11:    */   private static final int MAX_LINE_LENGTH = 76;
/*  12:    */   private static final byte EQUALS_SIGN = 61;
/*  13:    */   private static final byte NEW_LINE = 10;
/*  14:    */   private static final byte WHITE_SPACE_ENC = -5;
/*  15:    */   private static final byte EQUALS_SIGN_ENC = -1;
/*  16:    */   
/*  17:    */   private static byte[] alphabet(Base64Dialect dialect)
/*  18:    */   {
/*  19: 53 */     if (dialect == null) {
/*  20: 54 */       throw new NullPointerException("dialect");
/*  21:    */     }
/*  22: 56 */     return dialect.alphabet;
/*  23:    */   }
/*  24:    */   
/*  25:    */   private static byte[] decodabet(Base64Dialect dialect)
/*  26:    */   {
/*  27: 60 */     if (dialect == null) {
/*  28: 61 */       throw new NullPointerException("dialect");
/*  29:    */     }
/*  30: 63 */     return dialect.decodabet;
/*  31:    */   }
/*  32:    */   
/*  33:    */   private static boolean breakLines(Base64Dialect dialect)
/*  34:    */   {
/*  35: 67 */     if (dialect == null) {
/*  36: 68 */       throw new NullPointerException("dialect");
/*  37:    */     }
/*  38: 70 */     return dialect.breakLinesByDefault;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static ByteBuf encode(ByteBuf src)
/*  42:    */   {
/*  43: 74 */     return encode(src, Base64Dialect.STANDARD);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public static ByteBuf encode(ByteBuf src, Base64Dialect dialect)
/*  47:    */   {
/*  48: 78 */     return encode(src, breakLines(dialect), dialect);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static ByteBuf encode(ByteBuf src, boolean breakLines)
/*  52:    */   {
/*  53: 82 */     return encode(src, breakLines, Base64Dialect.STANDARD);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static ByteBuf encode(ByteBuf src, boolean breakLines, Base64Dialect dialect)
/*  57:    */   {
/*  58: 87 */     if (src == null) {
/*  59: 88 */       throw new NullPointerException("src");
/*  60:    */     }
/*  61: 91 */     ByteBuf dest = encode(src, src.readerIndex(), src.readableBytes(), breakLines, dialect);
/*  62: 92 */     src.readerIndex(src.writerIndex());
/*  63: 93 */     return dest;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static ByteBuf encode(ByteBuf src, int off, int len)
/*  67:    */   {
/*  68: 97 */     return encode(src, off, len, Base64Dialect.STANDARD);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public static ByteBuf encode(ByteBuf src, int off, int len, Base64Dialect dialect)
/*  72:    */   {
/*  73:101 */     return encode(src, off, len, breakLines(dialect), dialect);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public static ByteBuf encode(ByteBuf src, int off, int len, boolean breakLines)
/*  77:    */   {
/*  78:106 */     return encode(src, off, len, breakLines, Base64Dialect.STANDARD);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public static ByteBuf encode(ByteBuf src, int off, int len, boolean breakLines, Base64Dialect dialect)
/*  82:    */   {
/*  83:111 */     return encode(src, off, len, breakLines, dialect, src.alloc());
/*  84:    */   }
/*  85:    */   
/*  86:    */   public static ByteBuf encode(ByteBuf src, int off, int len, boolean breakLines, Base64Dialect dialect, ByteBufAllocator allocator)
/*  87:    */   {
/*  88:116 */     if (src == null) {
/*  89:117 */       throw new NullPointerException("src");
/*  90:    */     }
/*  91:119 */     if (dialect == null) {
/*  92:120 */       throw new NullPointerException("dialect");
/*  93:    */     }
/*  94:123 */     ByteBuf dest = allocator.buffer(encodedBufferSize(len, breakLines)).order(src.order());
/*  95:124 */     byte[] alphabet = alphabet(dialect);
/*  96:125 */     int d = 0;
/*  97:126 */     int e = 0;
/*  98:127 */     int len2 = len - 2;
/*  99:128 */     int lineLength = 0;
/* 100:129 */     for (; d < len2; e += 4)
/* 101:    */     {
/* 102:130 */       encode3to4(src, d + off, 3, dest, e, alphabet);
/* 103:    */       
/* 104:132 */       lineLength += 4;
/* 105:134 */       if ((breakLines) && (lineLength == 76))
/* 106:    */       {
/* 107:135 */         dest.setByte(e + 4, 10);
/* 108:136 */         e++;
/* 109:137 */         lineLength = 0;
/* 110:    */       }
/* 111:129 */       d += 3;
/* 112:    */     }
/* 113:141 */     if (d < len)
/* 114:    */     {
/* 115:142 */       encode3to4(src, d + off, len - d, dest, e, alphabet);
/* 116:143 */       e += 4;
/* 117:    */     }
/* 118:147 */     if ((e > 1) && (dest.getByte(e - 1) == 10)) {
/* 119:148 */       e--;
/* 120:    */     }
/* 121:151 */     return dest.slice(0, e);
/* 122:    */   }
/* 123:    */   
/* 124:    */   private static void encode3to4(ByteBuf src, int srcOffset, int numSigBytes, ByteBuf dest, int destOffset, byte[] alphabet)
/* 125:    */   {
/* 126:167 */     if (src.order() == ByteOrder.BIG_ENDIAN)
/* 127:    */     {
/* 128:    */       int inBuff;
/* 129:    */       int inBuff;
/* 130:    */       int inBuff;
/* 131:169 */       switch (numSigBytes)
/* 132:    */       {
/* 133:    */       case 1: 
/* 134:171 */         inBuff = toInt(src.getByte(srcOffset));
/* 135:172 */         break;
/* 136:    */       case 2: 
/* 137:174 */         inBuff = toIntBE(src.getShort(srcOffset));
/* 138:175 */         break;
/* 139:    */       default: 
/* 140:177 */         inBuff = numSigBytes <= 0 ? 0 : toIntBE(src.getMedium(srcOffset));
/* 141:    */       }
/* 142:180 */       encode3to4BigEndian(inBuff, numSigBytes, dest, destOffset, alphabet);
/* 143:    */     }
/* 144:    */     else
/* 145:    */     {
/* 146:    */       int inBuff;
/* 147:    */       int inBuff;
/* 148:    */       int inBuff;
/* 149:183 */       switch (numSigBytes)
/* 150:    */       {
/* 151:    */       case 1: 
/* 152:185 */         inBuff = toInt(src.getByte(srcOffset));
/* 153:186 */         break;
/* 154:    */       case 2: 
/* 155:188 */         inBuff = toIntLE(src.getShort(srcOffset));
/* 156:189 */         break;
/* 157:    */       default: 
/* 158:191 */         inBuff = numSigBytes <= 0 ? 0 : toIntLE(src.getMedium(srcOffset));
/* 159:    */       }
/* 160:194 */       encode3to4LittleEndian(inBuff, numSigBytes, dest, destOffset, alphabet);
/* 161:    */     }
/* 162:    */   }
/* 163:    */   
/* 164:    */   static int encodedBufferSize(int len, boolean breakLines)
/* 165:    */   {
/* 166:201 */     long len43 = (len << 2) / 3L;
/* 167:    */     
/* 168:    */ 
/* 169:204 */     long ret = len43 + 3L & 0xFFFFFFFC;
/* 170:206 */     if (breakLines) {
/* 171:207 */       ret += len43 / 76L;
/* 172:    */     }
/* 173:210 */     return ret < 2147483647L ? (int)ret : 2147483647;
/* 174:    */   }
/* 175:    */   
/* 176:    */   private static int toInt(byte value)
/* 177:    */   {
/* 178:214 */     return (value & 0xFF) << 16;
/* 179:    */   }
/* 180:    */   
/* 181:    */   private static int toIntBE(short value)
/* 182:    */   {
/* 183:218 */     return (value & 0xFF00) << 8 | (value & 0xFF) << 8;
/* 184:    */   }
/* 185:    */   
/* 186:    */   private static int toIntLE(short value)
/* 187:    */   {
/* 188:222 */     return (value & 0xFF) << 16 | value & 0xFF00;
/* 189:    */   }
/* 190:    */   
/* 191:    */   private static int toIntBE(int mediumValue)
/* 192:    */   {
/* 193:226 */     return mediumValue & 0xFF0000 | mediumValue & 0xFF00 | mediumValue & 0xFF;
/* 194:    */   }
/* 195:    */   
/* 196:    */   private static int toIntLE(int mediumValue)
/* 197:    */   {
/* 198:230 */     return (mediumValue & 0xFF) << 16 | mediumValue & 0xFF00 | (mediumValue & 0xFF0000) >>> 16;
/* 199:    */   }
/* 200:    */   
/* 201:    */   private static void encode3to4BigEndian(int inBuff, int numSigBytes, ByteBuf dest, int destOffset, byte[] alphabet)
/* 202:    */   {
/* 203:236 */     switch (numSigBytes)
/* 204:    */     {
/* 205:    */     case 3: 
/* 206:238 */       dest.setInt(destOffset, alphabet[(inBuff >>> 18)] << 24 | alphabet[(inBuff >>> 12 & 0x3F)] << 16 | alphabet[(inBuff >>> 6 & 0x3F)] << 8 | alphabet[(inBuff & 0x3F)]);
/* 207:    */       
/* 208:    */ 
/* 209:    */ 
/* 210:242 */       break;
/* 211:    */     case 2: 
/* 212:244 */       dest.setInt(destOffset, alphabet[(inBuff >>> 18)] << 24 | alphabet[(inBuff >>> 12 & 0x3F)] << 16 | alphabet[(inBuff >>> 6 & 0x3F)] << 8 | 0x3D);
/* 213:    */       
/* 214:    */ 
/* 215:    */ 
/* 216:248 */       break;
/* 217:    */     case 1: 
/* 218:250 */       dest.setInt(destOffset, alphabet[(inBuff >>> 18)] << 24 | alphabet[(inBuff >>> 12 & 0x3F)] << 16 | 0x3D00 | 0x3D);
/* 219:    */       
/* 220:    */ 
/* 221:    */ 
/* 222:254 */       break;
/* 223:    */     }
/* 224:    */   }
/* 225:    */   
/* 226:    */   private static void encode3to4LittleEndian(int inBuff, int numSigBytes, ByteBuf dest, int destOffset, byte[] alphabet)
/* 227:    */   {
/* 228:264 */     switch (numSigBytes)
/* 229:    */     {
/* 230:    */     case 3: 
/* 231:266 */       dest.setInt(destOffset, alphabet[(inBuff >>> 18)] | alphabet[(inBuff >>> 12 & 0x3F)] << 8 | alphabet[(inBuff >>> 6 & 0x3F)] << 16 | alphabet[(inBuff & 0x3F)] << 24);
/* 232:    */       
/* 233:    */ 
/* 234:    */ 
/* 235:270 */       break;
/* 236:    */     case 2: 
/* 237:272 */       dest.setInt(destOffset, alphabet[(inBuff >>> 18)] | alphabet[(inBuff >>> 12 & 0x3F)] << 8 | alphabet[(inBuff >>> 6 & 0x3F)] << 16 | 0x3D000000);
/* 238:    */       
/* 239:    */ 
/* 240:    */ 
/* 241:276 */       break;
/* 242:    */     case 1: 
/* 243:278 */       dest.setInt(destOffset, alphabet[(inBuff >>> 18)] | alphabet[(inBuff >>> 12 & 0x3F)] << 8 | 0x3D0000 | 0x3D000000);
/* 244:    */       
/* 245:    */ 
/* 246:    */ 
/* 247:282 */       break;
/* 248:    */     }
/* 249:    */   }
/* 250:    */   
/* 251:    */   public static ByteBuf decode(ByteBuf src)
/* 252:    */   {
/* 253:290 */     return decode(src, Base64Dialect.STANDARD);
/* 254:    */   }
/* 255:    */   
/* 256:    */   public static ByteBuf decode(ByteBuf src, Base64Dialect dialect)
/* 257:    */   {
/* 258:294 */     if (src == null) {
/* 259:295 */       throw new NullPointerException("src");
/* 260:    */     }
/* 261:298 */     ByteBuf dest = decode(src, src.readerIndex(), src.readableBytes(), dialect);
/* 262:299 */     src.readerIndex(src.writerIndex());
/* 263:300 */     return dest;
/* 264:    */   }
/* 265:    */   
/* 266:    */   public static ByteBuf decode(ByteBuf src, int off, int len)
/* 267:    */   {
/* 268:305 */     return decode(src, off, len, Base64Dialect.STANDARD);
/* 269:    */   }
/* 270:    */   
/* 271:    */   public static ByteBuf decode(ByteBuf src, int off, int len, Base64Dialect dialect)
/* 272:    */   {
/* 273:310 */     return decode(src, off, len, dialect, src.alloc());
/* 274:    */   }
/* 275:    */   
/* 276:    */   public static ByteBuf decode(ByteBuf src, int off, int len, Base64Dialect dialect, ByteBufAllocator allocator)
/* 277:    */   {
/* 278:315 */     if (src == null) {
/* 279:316 */       throw new NullPointerException("src");
/* 280:    */     }
/* 281:318 */     if (dialect == null) {
/* 282:319 */       throw new NullPointerException("dialect");
/* 283:    */     }
/* 284:323 */     return new Decoder(null).decode(src, off, len, allocator, dialect);
/* 285:    */   }
/* 286:    */   
/* 287:    */   static int decodedBufferSize(int len)
/* 288:    */   {
/* 289:328 */     return len - (len >>> 2);
/* 290:    */   }
/* 291:    */   
/* 292:    */   private static final class Decoder
/* 293:    */     implements ByteProcessor
/* 294:    */   {
/* 295:332 */     private final byte[] b4 = new byte[4];
/* 296:    */     private int b4Posn;
/* 297:    */     private byte sbiCrop;
/* 298:    */     private byte sbiDecode;
/* 299:    */     private byte[] decodabet;
/* 300:    */     private int outBuffPosn;
/* 301:    */     private ByteBuf dest;
/* 302:    */     
/* 303:    */     ByteBuf decode(ByteBuf src, int off, int len, ByteBufAllocator allocator, Base64Dialect dialect)
/* 304:    */     {
/* 305:341 */       this.dest = allocator.buffer(Base64.decodedBufferSize(len)).order(src.order());
/* 306:    */       
/* 307:343 */       this.decodabet = Base64.decodabet(dialect);
/* 308:    */       try
/* 309:    */       {
/* 310:345 */         src.forEachByte(off, len, this);
/* 311:346 */         return this.dest.slice(0, this.outBuffPosn);
/* 312:    */       }
/* 313:    */       catch (Throwable cause)
/* 314:    */       {
/* 315:348 */         this.dest.release();
/* 316:349 */         PlatformDependent.throwException(cause);
/* 317:    */       }
/* 318:350 */       return null;
/* 319:    */     }
/* 320:    */     
/* 321:    */     public boolean process(byte value)
/* 322:    */       throws Exception
/* 323:    */     {
/* 324:356 */       this.sbiCrop = ((byte)(value & 0x7F));
/* 325:357 */       this.sbiDecode = this.decodabet[this.sbiCrop];
/* 326:359 */       if (this.sbiDecode >= -5)
/* 327:    */       {
/* 328:360 */         if (this.sbiDecode >= -1)
/* 329:    */         {
/* 330:361 */           this.b4[(this.b4Posn++)] = this.sbiCrop;
/* 331:362 */           if (this.b4Posn > 3)
/* 332:    */           {
/* 333:363 */             this.outBuffPosn += decode4to3(this.b4, this.dest, this.outBuffPosn, this.decodabet);
/* 334:364 */             this.b4Posn = 0;
/* 335:367 */             if (this.sbiCrop == 61) {
/* 336:368 */               return false;
/* 337:    */             }
/* 338:    */           }
/* 339:    */         }
/* 340:372 */         return true;
/* 341:    */       }
/* 342:374 */       throw new IllegalArgumentException("invalid bad Base64 input character: " + (short)(value & 0xFF) + " (decimal)");
/* 343:    */     }
/* 344:    */     
/* 345:    */     private static int decode4to3(byte[] src, ByteBuf dest, int destOffset, byte[] decodabet)
/* 346:    */     {
/* 347:379 */       byte src0 = src[0];
/* 348:380 */       byte src1 = src[1];
/* 349:381 */       byte src2 = src[2];
/* 350:383 */       if (src2 == 61)
/* 351:    */       {
/* 352:    */         try
/* 353:    */         {
/* 354:386 */           decodedValue = (decodabet[src0] & 0xFF) << 2 | (decodabet[src1] & 0xFF) >>> 4;
/* 355:    */         }
/* 356:    */         catch (IndexOutOfBoundsException ignored)
/* 357:    */         {
/* 358:    */           int decodedValue;
/* 359:388 */           throw new IllegalArgumentException("not encoded in Base64");
/* 360:    */         }
/* 361:    */         int decodedValue;
/* 362:390 */         dest.setByte(destOffset, decodedValue);
/* 363:391 */         return 1;
/* 364:    */       }
/* 365:394 */       byte src3 = src[3];
/* 366:395 */       if (src3 == 61)
/* 367:    */       {
/* 368:397 */         byte b1 = decodabet[src1];
/* 369:    */         try
/* 370:    */         {
/* 371:    */           int decodedValue;
/* 372:400 */           if (dest.order() == ByteOrder.BIG_ENDIAN) {
/* 373:403 */             decodedValue = ((decodabet[src0] & 0x3F) << 2 | (b1 & 0xF0) >> 4) << 8 | (b1 & 0xF) << 4 | (decodabet[src2] & 0xFC) >>> 2;
/* 374:    */           } else {
/* 375:407 */             decodedValue = (decodabet[src0] & 0x3F) << 2 | (b1 & 0xF0) >> 4 | ((b1 & 0xF) << 4 | (decodabet[src2] & 0xFC) >>> 2) << 8;
/* 376:    */           }
/* 377:    */         }
/* 378:    */         catch (IndexOutOfBoundsException ignored)
/* 379:    */         {
/* 380:    */           int decodedValue;
/* 381:411 */           throw new IllegalArgumentException("not encoded in Base64");
/* 382:    */         }
/* 383:    */         int decodedValue;
/* 384:413 */         dest.setShort(destOffset, decodedValue);
/* 385:414 */         return 2;
/* 386:    */       }
/* 387:    */       try
/* 388:    */       {
/* 389:    */         int decodedValue;
/* 390:419 */         if (dest.order() == ByteOrder.BIG_ENDIAN)
/* 391:    */         {
/* 392:420 */           decodedValue = (decodabet[src0] & 0x3F) << 18 | (decodabet[src1] & 0xFF) << 12 | (decodabet[src2] & 0xFF) << 6 | decodabet[src3] & 0xFF;
/* 393:    */         }
/* 394:    */         else
/* 395:    */         {
/* 396:425 */           byte b1 = decodabet[src1];
/* 397:426 */           byte b2 = decodabet[src2];
/* 398:    */           
/* 399:    */ 
/* 400:    */ 
/* 401:    */ 
/* 402:    */ 
/* 403:432 */           decodedValue = (decodabet[src0] & 0x3F) << 2 | (b1 & 0xF) << 12 | (b1 & 0xF0) >>> 4 | (b2 & 0x3) << 22 | (b2 & 0xFC) << 6 | (decodabet[src3] & 0xFF) << 16;
/* 404:    */         }
/* 405:    */       }
/* 406:    */       catch (IndexOutOfBoundsException ignored)
/* 407:    */       {
/* 408:    */         int decodedValue;
/* 409:444 */         throw new IllegalArgumentException("not encoded in Base64");
/* 410:    */       }
/* 411:    */       int decodedValue;
/* 412:446 */       dest.setMedium(destOffset, decodedValue);
/* 413:447 */       return 3;
/* 414:    */     }
/* 415:    */   }
/* 416:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.base64.Base64
 * JD-Core Version:    0.7.0.1
 */