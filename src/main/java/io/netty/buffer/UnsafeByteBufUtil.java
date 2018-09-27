/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.MathUtil;
/*   4:    */ import io.netty.util.internal.ObjectUtil;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ import java.io.OutputStream;
/*   9:    */ import java.nio.ByteBuffer;
/*  10:    */ import java.nio.ReadOnlyBufferException;
/*  11:    */ 
/*  12:    */ final class UnsafeByteBufUtil
/*  13:    */ {
/*  14: 35 */   private static final boolean UNALIGNED = ;
/*  15:    */   private static final byte ZERO = 0;
/*  16:    */   
/*  17:    */   static byte getByte(long address)
/*  18:    */   {
/*  19: 39 */     return PlatformDependent.getByte(address);
/*  20:    */   }
/*  21:    */   
/*  22:    */   static short getShort(long address)
/*  23:    */   {
/*  24: 43 */     if (UNALIGNED)
/*  25:    */     {
/*  26: 44 */       short v = PlatformDependent.getShort(address);
/*  27: 45 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Short.reverseBytes(v);
/*  28:    */     }
/*  29: 47 */     return (short)(PlatformDependent.getByte(address) << 8 | PlatformDependent.getByte(address + 1L) & 0xFF);
/*  30:    */   }
/*  31:    */   
/*  32:    */   static short getShortLE(long address)
/*  33:    */   {
/*  34: 51 */     if (UNALIGNED)
/*  35:    */     {
/*  36: 52 */       short v = PlatformDependent.getShort(address);
/*  37: 53 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(v) : v;
/*  38:    */     }
/*  39: 55 */     return (short)(PlatformDependent.getByte(address) & 0xFF | PlatformDependent.getByte(address + 1L) << 8);
/*  40:    */   }
/*  41:    */   
/*  42:    */   static int getUnsignedMedium(long address)
/*  43:    */   {
/*  44: 59 */     if (UNALIGNED) {
/*  45: 60 */       return 
/*  46:    */       
/*  47: 62 */         (PlatformDependent.getByte(address) & 0xFF) << 16 | (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? PlatformDependent.getShort(address + 1L) : Short.reverseBytes(PlatformDependent.getShort(address + 1L))) & 0xFFFF;
/*  48:    */     }
/*  49: 64 */     return 
/*  50:    */     
/*  51: 66 */       (PlatformDependent.getByte(address) & 0xFF) << 16 | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | PlatformDependent.getByte(address + 2L) & 0xFF;
/*  52:    */   }
/*  53:    */   
/*  54:    */   static int getUnsignedMediumLE(long address)
/*  55:    */   {
/*  56: 70 */     if (UNALIGNED) {
/*  57: 71 */       return 
/*  58:    */       
/*  59: 73 */         PlatformDependent.getByte(address) & 0xFF | ((PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(PlatformDependent.getShort(address + 1L)) : PlatformDependent.getShort(address + 1L)) & 0xFFFF) << 8;
/*  60:    */     }
/*  61: 75 */     return 
/*  62:    */     
/*  63: 77 */       PlatformDependent.getByte(address) & 0xFF | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 16;
/*  64:    */   }
/*  65:    */   
/*  66:    */   static int getInt(long address)
/*  67:    */   {
/*  68: 81 */     if (UNALIGNED)
/*  69:    */     {
/*  70: 82 */       int v = PlatformDependent.getInt(address);
/*  71: 83 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
/*  72:    */     }
/*  73: 85 */     return 
/*  74:    */     
/*  75:    */ 
/*  76: 88 */       PlatformDependent.getByte(address) << 24 | (PlatformDependent.getByte(address + 1L) & 0xFF) << 16 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 8 | PlatformDependent.getByte(address + 3L) & 0xFF;
/*  77:    */   }
/*  78:    */   
/*  79:    */   static int getIntLE(long address)
/*  80:    */   {
/*  81: 92 */     if (UNALIGNED)
/*  82:    */     {
/*  83: 93 */       int v = PlatformDependent.getInt(address);
/*  84: 94 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(v) : v;
/*  85:    */     }
/*  86: 96 */     return 
/*  87:    */     
/*  88:    */ 
/*  89: 99 */       PlatformDependent.getByte(address) & 0xFF | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 16 | PlatformDependent.getByte(address + 3L) << 24;
/*  90:    */   }
/*  91:    */   
/*  92:    */   static long getLong(long address)
/*  93:    */   {
/*  94:103 */     if (UNALIGNED)
/*  95:    */     {
/*  96:104 */       long v = PlatformDependent.getLong(address);
/*  97:105 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Long.reverseBytes(v);
/*  98:    */     }
/*  99:107 */     return 
/* 100:    */     
/* 101:    */ 
/* 102:    */ 
/* 103:    */ 
/* 104:    */ 
/* 105:    */ 
/* 106:114 */       PlatformDependent.getByte(address) << 56 | (PlatformDependent.getByte(address + 1L) & 0xFF) << 48 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 40 | (PlatformDependent.getByte(address + 3L) & 0xFF) << 32 | (PlatformDependent.getByte(address + 4L) & 0xFF) << 24 | (PlatformDependent.getByte(address + 5L) & 0xFF) << 16 | (PlatformDependent.getByte(address + 6L) & 0xFF) << 8 | PlatformDependent.getByte(address + 7L) & 0xFF;
/* 107:    */   }
/* 108:    */   
/* 109:    */   static long getLongLE(long address)
/* 110:    */   {
/* 111:118 */     if (UNALIGNED)
/* 112:    */     {
/* 113:119 */       long v = PlatformDependent.getLong(address);
/* 114:120 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(v) : v;
/* 115:    */     }
/* 116:122 */     return 
/* 117:    */     
/* 118:    */ 
/* 119:    */ 
/* 120:    */ 
/* 121:    */ 
/* 122:    */ 
/* 123:129 */       PlatformDependent.getByte(address) & 0xFF | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 16 | (PlatformDependent.getByte(address + 3L) & 0xFF) << 24 | (PlatformDependent.getByte(address + 4L) & 0xFF) << 32 | (PlatformDependent.getByte(address + 5L) & 0xFF) << 40 | (PlatformDependent.getByte(address + 6L) & 0xFF) << 48 | PlatformDependent.getByte(address + 7L) << 56;
/* 124:    */   }
/* 125:    */   
/* 126:    */   static void setByte(long address, int value)
/* 127:    */   {
/* 128:133 */     PlatformDependent.putByte(address, (byte)value);
/* 129:    */   }
/* 130:    */   
/* 131:    */   static void setShort(long address, int value)
/* 132:    */   {
/* 133:137 */     if (UNALIGNED)
/* 134:    */     {
/* 135:138 */       PlatformDependent.putShort(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : 
/* 136:139 */         Short.reverseBytes((short)value));
/* 137:    */     }
/* 138:    */     else
/* 139:    */     {
/* 140:141 */       PlatformDependent.putByte(address, (byte)(value >>> 8));
/* 141:142 */       PlatformDependent.putByte(address + 1L, (byte)value);
/* 142:    */     }
/* 143:    */   }
/* 144:    */   
/* 145:    */   static void setShortLE(long address, int value)
/* 146:    */   {
/* 147:147 */     if (UNALIGNED)
/* 148:    */     {
/* 149:148 */       PlatformDependent.putShort(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? 
/* 150:149 */         Short.reverseBytes((short)value) : (short)value);
/* 151:    */     }
/* 152:    */     else
/* 153:    */     {
/* 154:151 */       PlatformDependent.putByte(address, (byte)value);
/* 155:152 */       PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
/* 156:    */     }
/* 157:    */   }
/* 158:    */   
/* 159:    */   static void setMedium(long address, int value)
/* 160:    */   {
/* 161:157 */     PlatformDependent.putByte(address, (byte)(value >>> 16));
/* 162:158 */     if (UNALIGNED)
/* 163:    */     {
/* 164:159 */       PlatformDependent.putShort(address + 1L, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : 
/* 165:160 */         Short.reverseBytes((short)value));
/* 166:    */     }
/* 167:    */     else
/* 168:    */     {
/* 169:162 */       PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
/* 170:163 */       PlatformDependent.putByte(address + 2L, (byte)value);
/* 171:    */     }
/* 172:    */   }
/* 173:    */   
/* 174:    */   static void setMediumLE(long address, int value)
/* 175:    */   {
/* 176:168 */     PlatformDependent.putByte(address, (byte)value);
/* 177:169 */     if (UNALIGNED)
/* 178:    */     {
/* 179:170 */       PlatformDependent.putShort(address + 1L, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes((short)(value >>> 8)) : (short)(value >>> 8));
/* 180:    */     }
/* 181:    */     else
/* 182:    */     {
/* 183:173 */       PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
/* 184:174 */       PlatformDependent.putByte(address + 2L, (byte)(value >>> 16));
/* 185:    */     }
/* 186:    */   }
/* 187:    */   
/* 188:    */   static void setInt(long address, int value)
/* 189:    */   {
/* 190:179 */     if (UNALIGNED)
/* 191:    */     {
/* 192:180 */       PlatformDependent.putInt(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value));
/* 193:    */     }
/* 194:    */     else
/* 195:    */     {
/* 196:182 */       PlatformDependent.putByte(address, (byte)(value >>> 24));
/* 197:183 */       PlatformDependent.putByte(address + 1L, (byte)(value >>> 16));
/* 198:184 */       PlatformDependent.putByte(address + 2L, (byte)(value >>> 8));
/* 199:185 */       PlatformDependent.putByte(address + 3L, (byte)value);
/* 200:    */     }
/* 201:    */   }
/* 202:    */   
/* 203:    */   static void setIntLE(long address, int value)
/* 204:    */   {
/* 205:190 */     if (UNALIGNED)
/* 206:    */     {
/* 207:191 */       PlatformDependent.putInt(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(value) : value);
/* 208:    */     }
/* 209:    */     else
/* 210:    */     {
/* 211:193 */       PlatformDependent.putByte(address, (byte)value);
/* 212:194 */       PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
/* 213:195 */       PlatformDependent.putByte(address + 2L, (byte)(value >>> 16));
/* 214:196 */       PlatformDependent.putByte(address + 3L, (byte)(value >>> 24));
/* 215:    */     }
/* 216:    */   }
/* 217:    */   
/* 218:    */   static void setLong(long address, long value)
/* 219:    */   {
/* 220:201 */     if (UNALIGNED)
/* 221:    */     {
/* 222:202 */       PlatformDependent.putLong(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Long.reverseBytes(value));
/* 223:    */     }
/* 224:    */     else
/* 225:    */     {
/* 226:204 */       PlatformDependent.putByte(address, (byte)(int)(value >>> 56));
/* 227:205 */       PlatformDependent.putByte(address + 1L, (byte)(int)(value >>> 48));
/* 228:206 */       PlatformDependent.putByte(address + 2L, (byte)(int)(value >>> 40));
/* 229:207 */       PlatformDependent.putByte(address + 3L, (byte)(int)(value >>> 32));
/* 230:208 */       PlatformDependent.putByte(address + 4L, (byte)(int)(value >>> 24));
/* 231:209 */       PlatformDependent.putByte(address + 5L, (byte)(int)(value >>> 16));
/* 232:210 */       PlatformDependent.putByte(address + 6L, (byte)(int)(value >>> 8));
/* 233:211 */       PlatformDependent.putByte(address + 7L, (byte)(int)value);
/* 234:    */     }
/* 235:    */   }
/* 236:    */   
/* 237:    */   static void setLongLE(long address, long value)
/* 238:    */   {
/* 239:216 */     if (UNALIGNED)
/* 240:    */     {
/* 241:217 */       PlatformDependent.putLong(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(value) : value);
/* 242:    */     }
/* 243:    */     else
/* 244:    */     {
/* 245:219 */       PlatformDependent.putByte(address, (byte)(int)value);
/* 246:220 */       PlatformDependent.putByte(address + 1L, (byte)(int)(value >>> 8));
/* 247:221 */       PlatformDependent.putByte(address + 2L, (byte)(int)(value >>> 16));
/* 248:222 */       PlatformDependent.putByte(address + 3L, (byte)(int)(value >>> 24));
/* 249:223 */       PlatformDependent.putByte(address + 4L, (byte)(int)(value >>> 32));
/* 250:224 */       PlatformDependent.putByte(address + 5L, (byte)(int)(value >>> 40));
/* 251:225 */       PlatformDependent.putByte(address + 6L, (byte)(int)(value >>> 48));
/* 252:226 */       PlatformDependent.putByte(address + 7L, (byte)(int)(value >>> 56));
/* 253:    */     }
/* 254:    */   }
/* 255:    */   
/* 256:    */   static byte getByte(byte[] array, int index)
/* 257:    */   {
/* 258:231 */     return PlatformDependent.getByte(array, index);
/* 259:    */   }
/* 260:    */   
/* 261:    */   static short getShort(byte[] array, int index)
/* 262:    */   {
/* 263:235 */     if (UNALIGNED)
/* 264:    */     {
/* 265:236 */       short v = PlatformDependent.getShort(array, index);
/* 266:237 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Short.reverseBytes(v);
/* 267:    */     }
/* 268:239 */     return 
/* 269:240 */       (short)(PlatformDependent.getByte(array, index) << 8 | PlatformDependent.getByte(array, index + 1) & 0xFF);
/* 270:    */   }
/* 271:    */   
/* 272:    */   static short getShortLE(byte[] array, int index)
/* 273:    */   {
/* 274:244 */     if (UNALIGNED)
/* 275:    */     {
/* 276:245 */       short v = PlatformDependent.getShort(array, index);
/* 277:246 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(v) : v;
/* 278:    */     }
/* 279:248 */     return 
/* 280:249 */       (short)(PlatformDependent.getByte(array, index) & 0xFF | PlatformDependent.getByte(array, index + 1) << 8);
/* 281:    */   }
/* 282:    */   
/* 283:    */   static int getUnsignedMedium(byte[] array, int index)
/* 284:    */   {
/* 285:253 */     if (UNALIGNED) {
/* 286:254 */       return 
/* 287:    */       
/* 288:256 */         (PlatformDependent.getByte(array, index) & 0xFF) << 16 | (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? PlatformDependent.getShort(array, index + 1) : Short.reverseBytes(PlatformDependent.getShort(array, index + 1))) & 0xFFFF;
/* 289:    */     }
/* 290:259 */     return 
/* 291:    */     
/* 292:261 */       (PlatformDependent.getByte(array, index) & 0xFF) << 16 | (PlatformDependent.getByte(array, index + 1) & 0xFF) << 8 | PlatformDependent.getByte(array, index + 2) & 0xFF;
/* 293:    */   }
/* 294:    */   
/* 295:    */   static int getUnsignedMediumLE(byte[] array, int index)
/* 296:    */   {
/* 297:265 */     if (UNALIGNED) {
/* 298:266 */       return 
/* 299:    */       
/* 300:268 */         PlatformDependent.getByte(array, index) & 0xFF | ((PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(PlatformDependent.getShort(array, index + 1)) : PlatformDependent.getShort(array, index + 1)) & 0xFFFF) << 8;
/* 301:    */     }
/* 302:270 */     return 
/* 303:    */     
/* 304:272 */       PlatformDependent.getByte(array, index) & 0xFF | (PlatformDependent.getByte(array, index + 1) & 0xFF) << 8 | (PlatformDependent.getByte(array, index + 2) & 0xFF) << 16;
/* 305:    */   }
/* 306:    */   
/* 307:    */   static int getInt(byte[] array, int index)
/* 308:    */   {
/* 309:276 */     if (UNALIGNED)
/* 310:    */     {
/* 311:277 */       int v = PlatformDependent.getInt(array, index);
/* 312:278 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
/* 313:    */     }
/* 314:280 */     return 
/* 315:    */     
/* 316:    */ 
/* 317:283 */       PlatformDependent.getByte(array, index) << 24 | (PlatformDependent.getByte(array, index + 1) & 0xFF) << 16 | (PlatformDependent.getByte(array, index + 2) & 0xFF) << 8 | PlatformDependent.getByte(array, index + 3) & 0xFF;
/* 318:    */   }
/* 319:    */   
/* 320:    */   static int getIntLE(byte[] array, int index)
/* 321:    */   {
/* 322:287 */     if (UNALIGNED)
/* 323:    */     {
/* 324:288 */       int v = PlatformDependent.getInt(array, index);
/* 325:289 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(v) : v;
/* 326:    */     }
/* 327:291 */     return 
/* 328:    */     
/* 329:    */ 
/* 330:294 */       PlatformDependent.getByte(array, index) & 0xFF | (PlatformDependent.getByte(array, index + 1) & 0xFF) << 8 | (PlatformDependent.getByte(array, index + 2) & 0xFF) << 16 | PlatformDependent.getByte(array, index + 3) << 24;
/* 331:    */   }
/* 332:    */   
/* 333:    */   static long getLong(byte[] array, int index)
/* 334:    */   {
/* 335:298 */     if (UNALIGNED)
/* 336:    */     {
/* 337:299 */       long v = PlatformDependent.getLong(array, index);
/* 338:300 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Long.reverseBytes(v);
/* 339:    */     }
/* 340:302 */     return 
/* 341:    */     
/* 342:    */ 
/* 343:    */ 
/* 344:    */ 
/* 345:    */ 
/* 346:    */ 
/* 347:309 */       PlatformDependent.getByte(array, index) << 56 | (PlatformDependent.getByte(array, index + 1) & 0xFF) << 48 | (PlatformDependent.getByte(array, index + 2) & 0xFF) << 40 | (PlatformDependent.getByte(array, index + 3) & 0xFF) << 32 | (PlatformDependent.getByte(array, index + 4) & 0xFF) << 24 | (PlatformDependent.getByte(array, index + 5) & 0xFF) << 16 | (PlatformDependent.getByte(array, index + 6) & 0xFF) << 8 | PlatformDependent.getByte(array, index + 7) & 0xFF;
/* 348:    */   }
/* 349:    */   
/* 350:    */   static long getLongLE(byte[] array, int index)
/* 351:    */   {
/* 352:313 */     if (UNALIGNED)
/* 353:    */     {
/* 354:314 */       long v = PlatformDependent.getLong(array, index);
/* 355:315 */       return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(v) : v;
/* 356:    */     }
/* 357:317 */     return 
/* 358:    */     
/* 359:    */ 
/* 360:    */ 
/* 361:    */ 
/* 362:    */ 
/* 363:    */ 
/* 364:324 */       PlatformDependent.getByte(array, index) & 0xFF | (PlatformDependent.getByte(array, index + 1) & 0xFF) << 8 | (PlatformDependent.getByte(array, index + 2) & 0xFF) << 16 | (PlatformDependent.getByte(array, index + 3) & 0xFF) << 24 | (PlatformDependent.getByte(array, index + 4) & 0xFF) << 32 | (PlatformDependent.getByte(array, index + 5) & 0xFF) << 40 | (PlatformDependent.getByte(array, index + 6) & 0xFF) << 48 | PlatformDependent.getByte(array, index + 7) << 56;
/* 365:    */   }
/* 366:    */   
/* 367:    */   static void setByte(byte[] array, int index, int value)
/* 368:    */   {
/* 369:328 */     PlatformDependent.putByte(array, index, (byte)value);
/* 370:    */   }
/* 371:    */   
/* 372:    */   static void setShort(byte[] array, int index, int value)
/* 373:    */   {
/* 374:332 */     if (UNALIGNED)
/* 375:    */     {
/* 376:333 */       PlatformDependent.putShort(array, index, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : 
/* 377:334 */         Short.reverseBytes((short)value));
/* 378:    */     }
/* 379:    */     else
/* 380:    */     {
/* 381:336 */       PlatformDependent.putByte(array, index, (byte)(value >>> 8));
/* 382:337 */       PlatformDependent.putByte(array, index + 1, (byte)value);
/* 383:    */     }
/* 384:    */   }
/* 385:    */   
/* 386:    */   static void setShortLE(byte[] array, int index, int value)
/* 387:    */   {
/* 388:342 */     if (UNALIGNED)
/* 389:    */     {
/* 390:343 */       PlatformDependent.putShort(array, index, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? 
/* 391:344 */         Short.reverseBytes((short)value) : (short)value);
/* 392:    */     }
/* 393:    */     else
/* 394:    */     {
/* 395:346 */       PlatformDependent.putByte(array, index, (byte)value);
/* 396:347 */       PlatformDependent.putByte(array, index + 1, (byte)(value >>> 8));
/* 397:    */     }
/* 398:    */   }
/* 399:    */   
/* 400:    */   static void setMedium(byte[] array, int index, int value)
/* 401:    */   {
/* 402:352 */     PlatformDependent.putByte(array, index, (byte)(value >>> 16));
/* 403:353 */     if (UNALIGNED)
/* 404:    */     {
/* 405:354 */       PlatformDependent.putShort(array, index + 1, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : 
/* 406:    */       
/* 407:356 */         Short.reverseBytes((short)value));
/* 408:    */     }
/* 409:    */     else
/* 410:    */     {
/* 411:358 */       PlatformDependent.putByte(array, index + 1, (byte)(value >>> 8));
/* 412:359 */       PlatformDependent.putByte(array, index + 2, (byte)value);
/* 413:    */     }
/* 414:    */   }
/* 415:    */   
/* 416:    */   static void setMediumLE(byte[] array, int index, int value)
/* 417:    */   {
/* 418:364 */     PlatformDependent.putByte(array, index, (byte)value);
/* 419:365 */     if (UNALIGNED)
/* 420:    */     {
/* 421:366 */       PlatformDependent.putShort(array, index + 1, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? 
/* 422:367 */         Short.reverseBytes((short)(value >>> 8)) : (short)(value >>> 8));
/* 423:    */     }
/* 424:    */     else
/* 425:    */     {
/* 426:370 */       PlatformDependent.putByte(array, index + 1, (byte)(value >>> 8));
/* 427:371 */       PlatformDependent.putByte(array, index + 2, (byte)(value >>> 16));
/* 428:    */     }
/* 429:    */   }
/* 430:    */   
/* 431:    */   static void setInt(byte[] array, int index, int value)
/* 432:    */   {
/* 433:376 */     if (UNALIGNED)
/* 434:    */     {
/* 435:377 */       PlatformDependent.putInt(array, index, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value));
/* 436:    */     }
/* 437:    */     else
/* 438:    */     {
/* 439:379 */       PlatformDependent.putByte(array, index, (byte)(value >>> 24));
/* 440:380 */       PlatformDependent.putByte(array, index + 1, (byte)(value >>> 16));
/* 441:381 */       PlatformDependent.putByte(array, index + 2, (byte)(value >>> 8));
/* 442:382 */       PlatformDependent.putByte(array, index + 3, (byte)value);
/* 443:    */     }
/* 444:    */   }
/* 445:    */   
/* 446:    */   static void setIntLE(byte[] array, int index, int value)
/* 447:    */   {
/* 448:387 */     if (UNALIGNED)
/* 449:    */     {
/* 450:388 */       PlatformDependent.putInt(array, index, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(value) : value);
/* 451:    */     }
/* 452:    */     else
/* 453:    */     {
/* 454:390 */       PlatformDependent.putByte(array, index, (byte)value);
/* 455:391 */       PlatformDependent.putByte(array, index + 1, (byte)(value >>> 8));
/* 456:392 */       PlatformDependent.putByte(array, index + 2, (byte)(value >>> 16));
/* 457:393 */       PlatformDependent.putByte(array, index + 3, (byte)(value >>> 24));
/* 458:    */     }
/* 459:    */   }
/* 460:    */   
/* 461:    */   static void setLong(byte[] array, int index, long value)
/* 462:    */   {
/* 463:398 */     if (UNALIGNED)
/* 464:    */     {
/* 465:399 */       PlatformDependent.putLong(array, index, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Long.reverseBytes(value));
/* 466:    */     }
/* 467:    */     else
/* 468:    */     {
/* 469:401 */       PlatformDependent.putByte(array, index, (byte)(int)(value >>> 56));
/* 470:402 */       PlatformDependent.putByte(array, index + 1, (byte)(int)(value >>> 48));
/* 471:403 */       PlatformDependent.putByte(array, index + 2, (byte)(int)(value >>> 40));
/* 472:404 */       PlatformDependent.putByte(array, index + 3, (byte)(int)(value >>> 32));
/* 473:405 */       PlatformDependent.putByte(array, index + 4, (byte)(int)(value >>> 24));
/* 474:406 */       PlatformDependent.putByte(array, index + 5, (byte)(int)(value >>> 16));
/* 475:407 */       PlatformDependent.putByte(array, index + 6, (byte)(int)(value >>> 8));
/* 476:408 */       PlatformDependent.putByte(array, index + 7, (byte)(int)value);
/* 477:    */     }
/* 478:    */   }
/* 479:    */   
/* 480:    */   static void setLongLE(byte[] array, int index, long value)
/* 481:    */   {
/* 482:413 */     if (UNALIGNED)
/* 483:    */     {
/* 484:414 */       PlatformDependent.putLong(array, index, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(value) : value);
/* 485:    */     }
/* 486:    */     else
/* 487:    */     {
/* 488:416 */       PlatformDependent.putByte(array, index, (byte)(int)value);
/* 489:417 */       PlatformDependent.putByte(array, index + 1, (byte)(int)(value >>> 8));
/* 490:418 */       PlatformDependent.putByte(array, index + 2, (byte)(int)(value >>> 16));
/* 491:419 */       PlatformDependent.putByte(array, index + 3, (byte)(int)(value >>> 24));
/* 492:420 */       PlatformDependent.putByte(array, index + 4, (byte)(int)(value >>> 32));
/* 493:421 */       PlatformDependent.putByte(array, index + 5, (byte)(int)(value >>> 40));
/* 494:422 */       PlatformDependent.putByte(array, index + 6, (byte)(int)(value >>> 48));
/* 495:423 */       PlatformDependent.putByte(array, index + 7, (byte)(int)(value >>> 56));
/* 496:    */     }
/* 497:    */   }
/* 498:    */   
/* 499:    */   static void setZero(byte[] array, int index, int length)
/* 500:    */   {
/* 501:428 */     if (length == 0) {
/* 502:429 */       return;
/* 503:    */     }
/* 504:431 */     PlatformDependent.setMemory(array, index, length, (byte)0);
/* 505:    */   }
/* 506:    */   
/* 507:    */   static ByteBuf copy(AbstractByteBuf buf, long addr, int index, int length)
/* 508:    */   {
/* 509:435 */     buf.checkIndex(index, length);
/* 510:436 */     ByteBuf copy = buf.alloc().directBuffer(length, buf.maxCapacity());
/* 511:437 */     if (length != 0) {
/* 512:438 */       if (copy.hasMemoryAddress())
/* 513:    */       {
/* 514:439 */         PlatformDependent.copyMemory(addr, copy.memoryAddress(), length);
/* 515:440 */         copy.setIndex(0, length);
/* 516:    */       }
/* 517:    */       else
/* 518:    */       {
/* 519:442 */         copy.writeBytes(buf, index, length);
/* 520:    */       }
/* 521:    */     }
/* 522:445 */     return copy;
/* 523:    */   }
/* 524:    */   
/* 525:    */   static int setBytes(AbstractByteBuf buf, long addr, int index, InputStream in, int length)
/* 526:    */     throws IOException
/* 527:    */   {
/* 528:449 */     buf.checkIndex(index, length);
/* 529:450 */     ByteBuf tmpBuf = buf.alloc().heapBuffer(length);
/* 530:    */     try
/* 531:    */     {
/* 532:452 */       byte[] tmp = tmpBuf.array();
/* 533:453 */       int offset = tmpBuf.arrayOffset();
/* 534:454 */       int readBytes = in.read(tmp, offset, length);
/* 535:455 */       if (readBytes > 0) {
/* 536:456 */         PlatformDependent.copyMemory(tmp, offset, addr, readBytes);
/* 537:    */       }
/* 538:458 */       return readBytes;
/* 539:    */     }
/* 540:    */     finally
/* 541:    */     {
/* 542:460 */       tmpBuf.release();
/* 543:    */     }
/* 544:    */   }
/* 545:    */   
/* 546:    */   static void getBytes(AbstractByteBuf buf, long addr, int index, ByteBuf dst, int dstIndex, int length)
/* 547:    */   {
/* 548:465 */     buf.checkIndex(index, length);
/* 549:466 */     ObjectUtil.checkNotNull(dst, "dst");
/* 550:467 */     if (MathUtil.isOutOfBounds(dstIndex, length, dst.capacity())) {
/* 551:468 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/* 552:    */     }
/* 553:471 */     if (dst.hasMemoryAddress()) {
/* 554:472 */       PlatformDependent.copyMemory(addr, dst.memoryAddress() + dstIndex, length);
/* 555:473 */     } else if (dst.hasArray()) {
/* 556:474 */       PlatformDependent.copyMemory(addr, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 557:    */     } else {
/* 558:476 */       dst.setBytes(dstIndex, buf, index, length);
/* 559:    */     }
/* 560:    */   }
/* 561:    */   
/* 562:    */   static void getBytes(AbstractByteBuf buf, long addr, int index, byte[] dst, int dstIndex, int length)
/* 563:    */   {
/* 564:481 */     buf.checkIndex(index, length);
/* 565:482 */     ObjectUtil.checkNotNull(dst, "dst");
/* 566:483 */     if (MathUtil.isOutOfBounds(dstIndex, length, dst.length)) {
/* 567:484 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/* 568:    */     }
/* 569:486 */     if (length != 0) {
/* 570:487 */       PlatformDependent.copyMemory(addr, dst, dstIndex, length);
/* 571:    */     }
/* 572:    */   }
/* 573:    */   
/* 574:    */   static void getBytes(AbstractByteBuf buf, long addr, int index, ByteBuffer dst)
/* 575:    */   {
/* 576:492 */     buf.checkIndex(index, dst.remaining());
/* 577:493 */     if (dst.remaining() == 0) {
/* 578:494 */       return;
/* 579:    */     }
/* 580:497 */     if (dst.isDirect())
/* 581:    */     {
/* 582:498 */       if (dst.isReadOnly()) {
/* 583:500 */         throw new ReadOnlyBufferException();
/* 584:    */       }
/* 585:503 */       long dstAddress = PlatformDependent.directBufferAddress(dst);
/* 586:504 */       PlatformDependent.copyMemory(addr, dstAddress + dst.position(), dst.remaining());
/* 587:505 */       dst.position(dst.position() + dst.remaining());
/* 588:    */     }
/* 589:506 */     else if (dst.hasArray())
/* 590:    */     {
/* 591:508 */       PlatformDependent.copyMemory(addr, dst.array(), dst.arrayOffset() + dst.position(), dst.remaining());
/* 592:509 */       dst.position(dst.position() + dst.remaining());
/* 593:    */     }
/* 594:    */     else
/* 595:    */     {
/* 596:511 */       dst.put(buf.nioBuffer());
/* 597:    */     }
/* 598:    */   }
/* 599:    */   
/* 600:    */   static void setBytes(AbstractByteBuf buf, long addr, int index, ByteBuf src, int srcIndex, int length)
/* 601:    */   {
/* 602:516 */     buf.checkIndex(index, length);
/* 603:517 */     ObjectUtil.checkNotNull(src, "src");
/* 604:518 */     if (MathUtil.isOutOfBounds(srcIndex, length, src.capacity())) {
/* 605:519 */       throw new IndexOutOfBoundsException("srcIndex: " + srcIndex);
/* 606:    */     }
/* 607:522 */     if (length != 0) {
/* 608:523 */       if (src.hasMemoryAddress()) {
/* 609:524 */         PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, addr, length);
/* 610:525 */       } else if (src.hasArray()) {
/* 611:526 */         PlatformDependent.copyMemory(src.array(), src.arrayOffset() + srcIndex, addr, length);
/* 612:    */       } else {
/* 613:528 */         src.getBytes(srcIndex, buf, index, length);
/* 614:    */       }
/* 615:    */     }
/* 616:    */   }
/* 617:    */   
/* 618:    */   static void setBytes(AbstractByteBuf buf, long addr, int index, byte[] src, int srcIndex, int length)
/* 619:    */   {
/* 620:534 */     buf.checkIndex(index, length);
/* 621:535 */     if (length != 0) {
/* 622:536 */       PlatformDependent.copyMemory(src, srcIndex, addr, length);
/* 623:    */     }
/* 624:    */   }
/* 625:    */   
/* 626:    */   static void setBytes(AbstractByteBuf buf, long addr, int index, ByteBuffer src)
/* 627:    */   {
/* 628:541 */     buf.checkIndex(index, src.remaining());
/* 629:    */     
/* 630:543 */     int length = src.remaining();
/* 631:544 */     if (length == 0) {
/* 632:545 */       return;
/* 633:    */     }
/* 634:548 */     if (src.isDirect())
/* 635:    */     {
/* 636:550 */       long srcAddress = PlatformDependent.directBufferAddress(src);
/* 637:551 */       PlatformDependent.copyMemory(srcAddress + src.position(), addr, src.remaining());
/* 638:552 */       src.position(src.position() + length);
/* 639:    */     }
/* 640:553 */     else if (src.hasArray())
/* 641:    */     {
/* 642:555 */       PlatformDependent.copyMemory(src.array(), src.arrayOffset() + src.position(), addr, length);
/* 643:556 */       src.position(src.position() + length);
/* 644:    */     }
/* 645:    */     else
/* 646:    */     {
/* 647:558 */       ByteBuf tmpBuf = buf.alloc().heapBuffer(length);
/* 648:    */       try
/* 649:    */       {
/* 650:560 */         byte[] tmp = tmpBuf.array();
/* 651:561 */         src.get(tmp, tmpBuf.arrayOffset(), length);
/* 652:562 */         PlatformDependent.copyMemory(tmp, tmpBuf.arrayOffset(), addr, length);
/* 653:    */       }
/* 654:    */       finally
/* 655:    */       {
/* 656:564 */         tmpBuf.release();
/* 657:    */       }
/* 658:    */     }
/* 659:    */   }
/* 660:    */   
/* 661:    */   static void getBytes(AbstractByteBuf buf, long addr, int index, OutputStream out, int length)
/* 662:    */     throws IOException
/* 663:    */   {
/* 664:570 */     buf.checkIndex(index, length);
/* 665:571 */     if (length != 0)
/* 666:    */     {
/* 667:572 */       ByteBuf tmpBuf = buf.alloc().heapBuffer(length);
/* 668:    */       try
/* 669:    */       {
/* 670:574 */         byte[] tmp = tmpBuf.array();
/* 671:575 */         int offset = tmpBuf.arrayOffset();
/* 672:576 */         PlatformDependent.copyMemory(addr, tmp, offset, length);
/* 673:577 */         out.write(tmp, offset, length);
/* 674:    */       }
/* 675:    */       finally
/* 676:    */       {
/* 677:579 */         tmpBuf.release();
/* 678:    */       }
/* 679:    */     }
/* 680:    */   }
/* 681:    */   
/* 682:    */   static void setZero(long addr, int length)
/* 683:    */   {
/* 684:585 */     if (length == 0) {
/* 685:586 */       return;
/* 686:    */     }
/* 687:589 */     PlatformDependent.setMemory(addr, length, (byte)0);
/* 688:    */   }
/* 689:    */   
/* 690:    */   static UnpooledUnsafeDirectByteBuf newUnsafeDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/* 691:    */   {
/* 692:594 */     if (PlatformDependent.useDirectBufferNoCleaner()) {
/* 693:595 */       return new UnpooledUnsafeNoCleanerDirectByteBuf(alloc, initialCapacity, maxCapacity);
/* 694:    */     }
/* 695:597 */     return new UnpooledUnsafeDirectByteBuf(alloc, initialCapacity, maxCapacity);
/* 696:    */   }
/* 697:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.UnsafeByteBufUtil
 * JD-Core Version:    0.7.0.1
 */