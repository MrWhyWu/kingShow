/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ final class FastLz
/*   4:    */ {
/*   5:    */   private static final int MAX_DISTANCE = 8191;
/*   6:    */   private static final int MAX_FARDISTANCE = 73725;
/*   7:    */   private static final int HASH_LOG = 13;
/*   8:    */   private static final int HASH_SIZE = 8192;
/*   9:    */   private static final int HASH_MASK = 8191;
/*  10:    */   private static final int MAX_COPY = 32;
/*  11:    */   private static final int MAX_LEN = 264;
/*  12:    */   private static final int MIN_RECOMENDED_LENGTH_FOR_LEVEL_2 = 65536;
/*  13:    */   static final int MAGIC_NUMBER = 4607066;
/*  14:    */   static final byte BLOCK_TYPE_NON_COMPRESSED = 0;
/*  15:    */   static final byte BLOCK_TYPE_COMPRESSED = 1;
/*  16:    */   static final byte BLOCK_WITHOUT_CHECKSUM = 0;
/*  17:    */   static final byte BLOCK_WITH_CHECKSUM = 16;
/*  18:    */   static final int OPTIONS_OFFSET = 3;
/*  19:    */   static final int CHECKSUM_OFFSET = 4;
/*  20:    */   static final int MAX_CHUNK_LENGTH = 65535;
/*  21:    */   static final int MIN_LENGTH_TO_COMPRESSION = 32;
/*  22:    */   static final int LEVEL_AUTO = 0;
/*  23:    */   static final int LEVEL_1 = 1;
/*  24:    */   static final int LEVEL_2 = 2;
/*  25:    */   
/*  26:    */   static int calculateOutputBufferLength(int inputLength)
/*  27:    */   {
/*  28: 83 */     int outputLength = (int)(inputLength * 1.06D);
/*  29: 84 */     return Math.max(outputLength, 66);
/*  30:    */   }
/*  31:    */   
/*  32:    */   static int compress(byte[] input, int inOffset, int inLength, byte[] output, int outOffset, int proposedLevel)
/*  33:    */   {
/*  34:    */     int level;
/*  35:    */     int level;
/*  36: 97 */     if (proposedLevel == 0) {
/*  37: 98 */       level = inLength < 65536 ? 1 : 2;
/*  38:    */     } else {
/*  39:100 */       level = proposedLevel;
/*  40:    */     }
/*  41:103 */     int ip = 0;
/*  42:104 */     int ipBound = ip + inLength - 2;
/*  43:105 */     int ipLimit = ip + inLength - 12;
/*  44:    */     
/*  45:107 */     int op = 0;
/*  46:    */     
/*  47:    */ 
/*  48:110 */     int[] htab = new int[8192];
/*  49:121 */     if (inLength < 4)
/*  50:    */     {
/*  51:122 */       if (inLength != 0)
/*  52:    */       {
/*  53:124 */         output[(outOffset + op++)] = ((byte)(inLength - 1));
/*  54:125 */         ipBound++;
/*  55:126 */         while (ip <= ipBound) {
/*  56:127 */           output[(outOffset + op++)] = input[(inOffset + ip++)];
/*  57:    */         }
/*  58:129 */         return inLength + 1;
/*  59:    */       }
/*  60:132 */       return 0;
/*  61:    */     }
/*  62:137 */     for (int hslot = 0; hslot < 8192; hslot++) {
/*  63:139 */       htab[hslot] = ip;
/*  64:    */     }
/*  65:143 */     int copy = 2;
/*  66:144 */     output[(outOffset + op++)] = 31;
/*  67:145 */     output[(outOffset + op++)] = input[(inOffset + ip++)];
/*  68:146 */     output[(outOffset + op++)] = input[(inOffset + ip++)];
/*  69:149 */     while (ip < ipLimit)
/*  70:    */     {
/*  71:150 */       int ref = 0;
/*  72:    */       
/*  73:152 */       long distance = 0L;
/*  74:    */       
/*  75:    */ 
/*  76:    */ 
/*  77:    */ 
/*  78:157 */       int len = 3;
/*  79:    */       
/*  80:    */ 
/*  81:160 */       int anchor = ip;
/*  82:    */       
/*  83:162 */       boolean matchLabel = false;
/*  84:165 */       if (level == 2) {
/*  85:167 */         if ((input[(inOffset + ip)] == input[(inOffset + ip - 1)]) && 
/*  86:168 */           (readU16(input, inOffset + ip - 1) == readU16(input, inOffset + ip + 1)))
/*  87:    */         {
/*  88:169 */           distance = 1L;
/*  89:170 */           ip += 3;
/*  90:171 */           ref = anchor - 1 + 3;
/*  91:    */           
/*  92:    */ 
/*  93:    */ 
/*  94:    */ 
/*  95:176 */           matchLabel = true;
/*  96:    */         }
/*  97:    */       }
/*  98:179 */       if (!matchLabel)
/*  99:    */       {
/* 100:182 */         int hval = hashFunction(input, inOffset + ip);
/* 101:    */         
/* 102:184 */         hslot = hval;
/* 103:    */         
/* 104:186 */         ref = htab[hval];
/* 105:    */         
/* 106:    */ 
/* 107:189 */         distance = anchor - ref;
/* 108:    */         
/* 109:    */ 
/* 110:    */ 
/* 111:193 */         htab[hslot] = anchor;
/* 112:196 */         if ((distance != 0L) && (level == 1 ? distance < 8191L : distance < 73725L))
/* 113:    */         {
/* 114:196 */           if ((input[(inOffset + ref++)] == input[(inOffset + ip++)]) && (input[(inOffset + ref++)] == input[(inOffset + ip++)]) && (input[(inOffset + ref++)] == input[(inOffset + ip++)])) {}
/* 115:    */         }
/* 116:    */         else
/* 117:    */         {
/* 118:204 */           output[(outOffset + op++)] = input[(inOffset + anchor++)];
/* 119:205 */           ip = anchor;
/* 120:206 */           copy++;
/* 121:207 */           if (copy != 32) {
/* 122:    */             continue;
/* 123:    */           }
/* 124:208 */           copy = 0;
/* 125:209 */           output[(outOffset + op++)] = 31; continue;
/* 126:    */         }
/* 127:214 */         if (level == 2) {
/* 128:216 */           if (distance >= 8191L)
/* 129:    */           {
/* 130:217 */             if ((input[(inOffset + ip++)] != input[(inOffset + ref++)]) || (input[(inOffset + ip++)] != input[(inOffset + ref++)]))
/* 131:    */             {
/* 132:222 */               output[(outOffset + op++)] = input[(inOffset + anchor++)];
/* 133:223 */               ip = anchor;
/* 134:224 */               copy++;
/* 135:225 */               if (copy != 32) {
/* 136:    */                 continue;
/* 137:    */               }
/* 138:226 */               copy = 0;
/* 139:227 */               output[(outOffset + op++)] = 31; continue;
/* 140:    */             }
/* 141:231 */             len += 2;
/* 142:    */           }
/* 143:    */         }
/* 144:    */       }
/* 145:239 */       ip = anchor + len;
/* 146:    */       
/* 147:    */ 
/* 148:242 */       distance -= 1L;
/* 149:244 */       if (distance == 0L)
/* 150:    */       {
/* 151:247 */         byte x = input[(inOffset + ip - 1)];
/* 152:248 */         while ((ip < ipBound) && 
/* 153:249 */           (input[(inOffset + ref++)] == x)) {
/* 154:252 */           ip++;
/* 155:    */         }
/* 156:    */       }
/* 157:258 */       else if (input[(inOffset + ref++)] == input[(inOffset + ip++)])
/* 158:    */       {
/* 159:261 */         if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 160:264 */           if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 161:267 */             if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 162:270 */               if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 163:273 */                 if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 164:276 */                   if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 165:279 */                     if (input[(inOffset + ref++)] == input[(inOffset + ip++)]) {
/* 166:282 */                       while (ip < ipBound) {
/* 167:283 */                         if (input[(inOffset + ref++)] != input[(inOffset + ip++)]) {
/* 168:    */                           break;
/* 169:    */                         }
/* 170:    */                       }
/* 171:    */                     }
/* 172:    */                   }
/* 173:    */                 }
/* 174:    */               }
/* 175:    */             }
/* 176:    */           }
/* 177:    */         }
/* 178:    */       }
/* 179:292 */       if (copy != 0) {
/* 180:295 */         output[(outOffset + op - copy - 1)] = ((byte)(copy - 1));
/* 181:    */       } else {
/* 182:298 */         op--;
/* 183:    */       }
/* 184:302 */       copy = 0;
/* 185:    */       
/* 186:    */ 
/* 187:305 */       ip -= 3;
/* 188:306 */       len = ip - anchor;
/* 189:309 */       if (level == 2)
/* 190:    */       {
/* 191:310 */         if (distance < 8191L)
/* 192:    */         {
/* 193:311 */           if (len < 7)
/* 194:    */           {
/* 195:312 */             output[(outOffset + op++)] = ((byte)(int)((len << 5) + (distance >>> 8)));
/* 196:313 */             output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 197:    */           }
/* 198:    */           else
/* 199:    */           {
/* 200:315 */             output[(outOffset + op++)] = ((byte)(int)(224L + (distance >>> 8)));
/* 201:316 */             for (len -= 7; len >= 255; len -= 255) {
/* 202:317 */               output[(outOffset + op++)] = -1;
/* 203:    */             }
/* 204:319 */             output[(outOffset + op++)] = ((byte)len);
/* 205:320 */             output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 206:    */           }
/* 207:    */         }
/* 208:324 */         else if (len < 7)
/* 209:    */         {
/* 210:325 */           distance -= 8191L;
/* 211:326 */           output[(outOffset + op++)] = ((byte)((len << 5) + 31));
/* 212:327 */           output[(outOffset + op++)] = -1;
/* 213:328 */           output[(outOffset + op++)] = ((byte)(int)(distance >>> 8));
/* 214:329 */           output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 215:    */         }
/* 216:    */         else
/* 217:    */         {
/* 218:331 */           distance -= 8191L;
/* 219:332 */           output[(outOffset + op++)] = -1;
/* 220:333 */           for (len -= 7; len >= 255; len -= 255) {
/* 221:334 */             output[(outOffset + op++)] = -1;
/* 222:    */           }
/* 223:336 */           output[(outOffset + op++)] = ((byte)len);
/* 224:337 */           output[(outOffset + op++)] = -1;
/* 225:338 */           output[(outOffset + op++)] = ((byte)(int)(distance >>> 8));
/* 226:339 */           output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 227:    */         }
/* 228:    */       }
/* 229:    */       else
/* 230:    */       {
/* 231:343 */         if (len > 262) {
/* 232:344 */           while (len > 262)
/* 233:    */           {
/* 234:345 */             output[(outOffset + op++)] = ((byte)(int)(224L + (distance >>> 8)));
/* 235:346 */             output[(outOffset + op++)] = -3;
/* 236:347 */             output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 237:348 */             len -= 262;
/* 238:    */           }
/* 239:    */         }
/* 240:352 */         if (len < 7)
/* 241:    */         {
/* 242:353 */           output[(outOffset + op++)] = ((byte)(int)((len << 5) + (distance >>> 8)));
/* 243:354 */           output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 244:    */         }
/* 245:    */         else
/* 246:    */         {
/* 247:356 */           output[(outOffset + op++)] = ((byte)(int)(224L + (distance >>> 8)));
/* 248:357 */           output[(outOffset + op++)] = ((byte)(len - 7));
/* 249:358 */           output[(outOffset + op++)] = ((byte)(int)(distance & 0xFF));
/* 250:    */         }
/* 251:    */       }
/* 252:364 */       int hval = hashFunction(input, inOffset + ip);
/* 253:365 */       htab[hval] = (ip++);
/* 254:    */       
/* 255:    */ 
/* 256:368 */       hval = hashFunction(input, inOffset + ip);
/* 257:369 */       htab[hval] = (ip++);
/* 258:    */       
/* 259:    */ 
/* 260:372 */       output[(outOffset + op++)] = 31;
/* 261:    */     }
/* 262:391 */     ipBound++;
/* 263:392 */     while (ip <= ipBound)
/* 264:    */     {
/* 265:393 */       output[(outOffset + op++)] = input[(inOffset + ip++)];
/* 266:394 */       copy++;
/* 267:395 */       if (copy == 32)
/* 268:    */       {
/* 269:396 */         copy = 0;
/* 270:397 */         output[(outOffset + op++)] = 31;
/* 271:    */       }
/* 272:    */     }
/* 273:402 */     if (copy != 0) {
/* 274:404 */       output[(outOffset + op - copy - 1)] = ((byte)(copy - 1));
/* 275:    */     } else {
/* 276:406 */       op--;
/* 277:    */     }
/* 278:409 */     if (level == 2)
/* 279:    */     {
/* 280:411 */       int tmp1583_1581 = outOffset; byte[] tmp1583_1580 = output;tmp1583_1580[tmp1583_1581] = ((byte)(tmp1583_1580[tmp1583_1581] | 0x20));
/* 281:    */     }
/* 282:414 */     return op;
/* 283:    */   }
/* 284:    */   
/* 285:    */   static int decompress(byte[] input, int inOffset, int inLength, byte[] output, int outOffset, int outLength)
/* 286:    */   {
/* 287:428 */     int level = (input[inOffset] >> 5) + 1;
/* 288:429 */     if ((level != 1) && (level != 2)) {
/* 289:430 */       throw new DecompressionException(String.format("invalid level: %d (expected: %d or %d)", new Object[] {
/* 290:431 */         Integer.valueOf(level), Integer.valueOf(1), Integer.valueOf(2) }));
/* 291:    */     }
/* 292:436 */     int ip = 0;
/* 293:    */     
/* 294:438 */     int op = 0;
/* 295:    */     
/* 296:440 */     long ctrl = input[(inOffset + ip++)] & 0x1F;
/* 297:    */     
/* 298:442 */     int loop = 1;
/* 299:    */     do
/* 300:    */     {
/* 301:445 */       int ref = op;
/* 302:    */       
/* 303:447 */       long len = ctrl >> 5;
/* 304:    */       
/* 305:449 */       long ofs = (ctrl & 0x1F) << 8;
/* 306:451 */       if (ctrl >= 32L)
/* 307:    */       {
/* 308:452 */         len -= 1L;
/* 309:    */         
/* 310:454 */         ref = (int)(ref - ofs);
/* 311:457 */         if (len == 6L) {
/* 312:458 */           if (level == 1)
/* 313:    */           {
/* 314:460 */             len += (input[(inOffset + ip++)] & 0xFF);
/* 315:    */           }
/* 316:    */           else
/* 317:    */           {
/* 318:    */             int code;
/* 319:    */             do
/* 320:    */             {
/* 321:463 */               code = input[(inOffset + ip++)] & 0xFF;
/* 322:464 */               len += code;
/* 323:465 */             } while (code == 255);
/* 324:    */           }
/* 325:    */         }
/* 326:468 */         if (level == 1)
/* 327:    */         {
/* 328:470 */           ref -= (input[(inOffset + ip++)] & 0xFF);
/* 329:    */         }
/* 330:    */         else
/* 331:    */         {
/* 332:472 */           int code = input[(inOffset + ip++)] & 0xFF;
/* 333:473 */           ref -= code;
/* 334:478 */           if ((code == 255) && (ofs == 7936L))
/* 335:    */           {
/* 336:479 */             ofs = (input[(inOffset + ip++)] & 0xFF) << 8;
/* 337:480 */             ofs += (input[(inOffset + ip++)] & 0xFF);
/* 338:    */             
/* 339:482 */             ref = (int)(op - ofs - 8191L);
/* 340:    */           }
/* 341:    */         }
/* 342:487 */         if (op + len + 3L > outLength) {
/* 343:488 */           return 0;
/* 344:    */         }
/* 345:494 */         if (ref - 1 < 0) {
/* 346:495 */           return 0;
/* 347:    */         }
/* 348:498 */         if (ip < inLength) {
/* 349:499 */           ctrl = input[(inOffset + ip++)] & 0xFF;
/* 350:    */         } else {
/* 351:501 */           loop = 0;
/* 352:    */         }
/* 353:504 */         if (ref == op)
/* 354:    */         {
/* 355:507 */           byte b = output[(outOffset + ref - 1)];
/* 356:508 */           output[(outOffset + op++)] = b;
/* 357:509 */           output[(outOffset + op++)] = b;
/* 358:510 */           output[(outOffset + op++)] = b;
/* 359:511 */           while (len != 0L)
/* 360:    */           {
/* 361:512 */             output[(outOffset + op++)] = b;
/* 362:513 */             len -= 1L;
/* 363:    */           }
/* 364:    */         }
/* 365:    */         else
/* 366:    */         {
/* 367:517 */           ref--;
/* 368:    */           
/* 369:    */ 
/* 370:520 */           output[(outOffset + op++)] = output[(outOffset + ref++)];
/* 371:521 */           output[(outOffset + op++)] = output[(outOffset + ref++)];
/* 372:522 */           output[(outOffset + op++)] = output[(outOffset + ref++)];
/* 373:524 */           while (len != 0L)
/* 374:    */           {
/* 375:525 */             output[(outOffset + op++)] = output[(outOffset + ref++)];
/* 376:526 */             len -= 1L;
/* 377:    */           }
/* 378:    */         }
/* 379:    */       }
/* 380:    */       else
/* 381:    */       {
/* 382:530 */         ctrl += 1L;
/* 383:532 */         if (op + ctrl > outLength) {
/* 384:533 */           return 0;
/* 385:    */         }
/* 386:535 */         if (ip + ctrl > inLength) {
/* 387:536 */           return 0;
/* 388:    */         }
/* 389:540 */         output[(outOffset + op++)] = input[(inOffset + ip++)];
/* 390:542 */         for (ctrl -= 1L; ctrl != 0L; ctrl -= 1L) {
/* 391:544 */           output[(outOffset + op++)] = input[(inOffset + ip++)];
/* 392:    */         }
/* 393:547 */         loop = ip < inLength ? 1 : 0;
/* 394:548 */         if (loop != 0) {
/* 395:550 */           ctrl = input[(inOffset + ip++)] & 0xFF;
/* 396:    */         }
/* 397:    */       }
/* 398:555 */     } while (loop != 0);
/* 399:558 */     return op;
/* 400:    */   }
/* 401:    */   
/* 402:    */   private static int hashFunction(byte[] p, int offset)
/* 403:    */   {
/* 404:562 */     int v = readU16(p, offset);
/* 405:563 */     v ^= readU16(p, offset + 1) ^ v >> 3;
/* 406:564 */     v &= 0x1FFF;
/* 407:565 */     return v;
/* 408:    */   }
/* 409:    */   
/* 410:    */   private static int readU16(byte[] data, int offset)
/* 411:    */   {
/* 412:569 */     if (offset + 1 >= data.length) {
/* 413:570 */       return data[offset] & 0xFF;
/* 414:    */     }
/* 415:572 */     return (data[(offset + 1)] & 0xFF) << 8 | data[offset] & 0xFF;
/* 416:    */   }
/* 417:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.codec.compression.FastLz
 * JD-Core Version:    0.7.0.1
 */