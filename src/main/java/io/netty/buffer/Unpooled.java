/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ import java.nio.CharBuffer;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public final class Unpooled
/*  12:    */ {
/*  13:    */   private static final ByteBufAllocator ALLOC;
/*  14:    */   public static final ByteOrder BIG_ENDIAN;
/*  15:    */   public static final ByteOrder LITTLE_ENDIAN;
/*  16:    */   public static final ByteBuf EMPTY_BUFFER;
/*  17:    */   
/*  18:    */   static
/*  19:    */   {
/*  20: 73 */     ALLOC = UnpooledByteBufAllocator.DEFAULT;
/*  21:    */     
/*  22:    */ 
/*  23:    */ 
/*  24:    */ 
/*  25: 78 */     BIG_ENDIAN = ByteOrder.BIG_ENDIAN;
/*  26:    */     
/*  27:    */ 
/*  28:    */ 
/*  29:    */ 
/*  30: 83 */     LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;
/*  31:    */     
/*  32:    */ 
/*  33:    */ 
/*  34:    */ 
/*  35: 88 */     EMPTY_BUFFER = ALLOC.buffer(0, 0);
/*  36:    */     
/*  37:    */ 
/*  38: 91 */     assert ((EMPTY_BUFFER instanceof EmptyByteBuf)) : "EMPTY_BUFFER must be an EmptyByteBuf.";
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static ByteBuf buffer()
/*  42:    */   {
/*  43: 99 */     return ALLOC.heapBuffer();
/*  44:    */   }
/*  45:    */   
/*  46:    */   public static ByteBuf directBuffer()
/*  47:    */   {
/*  48:107 */     return ALLOC.directBuffer();
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static ByteBuf buffer(int initialCapacity)
/*  52:    */   {
/*  53:116 */     return ALLOC.heapBuffer(initialCapacity);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static ByteBuf directBuffer(int initialCapacity)
/*  57:    */   {
/*  58:125 */     return ALLOC.directBuffer(initialCapacity);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public static ByteBuf buffer(int initialCapacity, int maxCapacity)
/*  62:    */   {
/*  63:135 */     return ALLOC.heapBuffer(initialCapacity, maxCapacity);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static ByteBuf directBuffer(int initialCapacity, int maxCapacity)
/*  67:    */   {
/*  68:145 */     return ALLOC.directBuffer(initialCapacity, maxCapacity);
/*  69:    */   }
/*  70:    */   
/*  71:    */   public static ByteBuf wrappedBuffer(byte[] array)
/*  72:    */   {
/*  73:154 */     if (array.length == 0) {
/*  74:155 */       return EMPTY_BUFFER;
/*  75:    */     }
/*  76:157 */     return new UnpooledHeapByteBuf(ALLOC, array, array.length);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public static ByteBuf wrappedBuffer(byte[] array, int offset, int length)
/*  80:    */   {
/*  81:166 */     if (length == 0) {
/*  82:167 */       return EMPTY_BUFFER;
/*  83:    */     }
/*  84:170 */     if ((offset == 0) && (length == array.length)) {
/*  85:171 */       return wrappedBuffer(array);
/*  86:    */     }
/*  87:174 */     return wrappedBuffer(array).slice(offset, length);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public static ByteBuf wrappedBuffer(ByteBuffer buffer)
/*  91:    */   {
/*  92:183 */     if (!buffer.hasRemaining()) {
/*  93:184 */       return EMPTY_BUFFER;
/*  94:    */     }
/*  95:186 */     if ((!buffer.isDirect()) && (buffer.hasArray())) {
/*  96:187 */       return 
/*  97:    */       
/*  98:    */ 
/*  99:190 */         wrappedBuffer(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining()).order(buffer.order());
/* 100:    */     }
/* 101:191 */     if (PlatformDependent.hasUnsafe())
/* 102:    */     {
/* 103:192 */       if (buffer.isReadOnly())
/* 104:    */       {
/* 105:193 */         if (buffer.isDirect()) {
/* 106:194 */           return new ReadOnlyUnsafeDirectByteBuf(ALLOC, buffer);
/* 107:    */         }
/* 108:196 */         return new ReadOnlyByteBufferBuf(ALLOC, buffer);
/* 109:    */       }
/* 110:199 */       return new UnpooledUnsafeDirectByteBuf(ALLOC, buffer, buffer.remaining());
/* 111:    */     }
/* 112:202 */     if (buffer.isReadOnly()) {
/* 113:203 */       return new ReadOnlyByteBufferBuf(ALLOC, buffer);
/* 114:    */     }
/* 115:205 */     return new UnpooledDirectByteBuf(ALLOC, buffer, buffer.remaining());
/* 116:    */   }
/* 117:    */   
/* 118:    */   public static ByteBuf wrappedBuffer(long memoryAddress, int size, boolean doFree)
/* 119:    */   {
/* 120:215 */     return new WrappedUnpooledUnsafeDirectByteBuf(ALLOC, memoryAddress, size, doFree);
/* 121:    */   }
/* 122:    */   
/* 123:    */   public static ByteBuf wrappedBuffer(ByteBuf buffer)
/* 124:    */   {
/* 125:227 */     if (buffer.isReadable()) {
/* 126:228 */       return buffer.slice();
/* 127:    */     }
/* 128:230 */     buffer.release();
/* 129:231 */     return EMPTY_BUFFER;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public static ByteBuf wrappedBuffer(byte[]... arrays)
/* 133:    */   {
/* 134:241 */     return wrappedBuffer(16, arrays);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public static ByteBuf wrappedBuffer(ByteBuf... buffers)
/* 138:    */   {
/* 139:252 */     return wrappedBuffer(16, buffers);
/* 140:    */   }
/* 141:    */   
/* 142:    */   public static ByteBuf wrappedBuffer(ByteBuffer... buffers)
/* 143:    */   {
/* 144:261 */     return wrappedBuffer(16, buffers);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public static ByteBuf wrappedBuffer(int maxNumComponents, byte[]... arrays)
/* 148:    */   {
/* 149:270 */     switch (arrays.length)
/* 150:    */     {
/* 151:    */     case 0: 
/* 152:    */       break;
/* 153:    */     case 1: 
/* 154:274 */       if (arrays[0].length != 0) {
/* 155:275 */         return wrappedBuffer(arrays[0]);
/* 156:    */       }
/* 157:    */       break;
/* 158:    */     default: 
/* 159:280 */       List<ByteBuf> components = new ArrayList(arrays.length);
/* 160:281 */       for (byte[] a : arrays)
/* 161:    */       {
/* 162:282 */         if (a == null) {
/* 163:    */           break;
/* 164:    */         }
/* 165:285 */         if (a.length > 0) {
/* 166:286 */           components.add(wrappedBuffer(a));
/* 167:    */         }
/* 168:    */       }
/* 169:290 */       if (!components.isEmpty()) {
/* 170:291 */         return new CompositeByteBuf(ALLOC, false, maxNumComponents, components);
/* 171:    */       }
/* 172:    */       break;
/* 173:    */     }
/* 174:295 */     return EMPTY_BUFFER;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public static ByteBuf wrappedBuffer(int maxNumComponents, ByteBuf... buffers)
/* 178:    */   {
/* 179:308 */     switch (buffers.length)
/* 180:    */     {
/* 181:    */     case 0: 
/* 182:    */       break;
/* 183:    */     case 1: 
/* 184:312 */       ByteBuf buffer = buffers[0];
/* 185:313 */       if (buffer.isReadable()) {
/* 186:314 */         return wrappedBuffer(buffer.order(BIG_ENDIAN));
/* 187:    */       }
/* 188:316 */       buffer.release();
/* 189:    */       
/* 190:318 */       break;
/* 191:    */     default: 
/* 192:320 */       for (int i = 0; i < buffers.length; i++)
/* 193:    */       {
/* 194:321 */         ByteBuf buf = buffers[i];
/* 195:322 */         if (buf.isReadable()) {
/* 196:323 */           return new CompositeByteBuf(ALLOC, false, maxNumComponents, buffers, i, buffers.length);
/* 197:    */         }
/* 198:325 */         buf.release();
/* 199:    */       }
/* 200:    */     }
/* 201:329 */     return EMPTY_BUFFER;
/* 202:    */   }
/* 203:    */   
/* 204:    */   public static ByteBuf wrappedBuffer(int maxNumComponents, ByteBuffer... buffers)
/* 205:    */   {
/* 206:338 */     switch (buffers.length)
/* 207:    */     {
/* 208:    */     case 0: 
/* 209:    */       break;
/* 210:    */     case 1: 
/* 211:342 */       if (buffers[0].hasRemaining()) {
/* 212:343 */         return wrappedBuffer(buffers[0].order(BIG_ENDIAN));
/* 213:    */       }
/* 214:    */       break;
/* 215:    */     default: 
/* 216:348 */       List<ByteBuf> components = new ArrayList(buffers.length);
/* 217:349 */       for (ByteBuffer b : buffers)
/* 218:    */       {
/* 219:350 */         if (b == null) {
/* 220:    */           break;
/* 221:    */         }
/* 222:353 */         if (b.remaining() > 0) {
/* 223:354 */           components.add(wrappedBuffer(b.order(BIG_ENDIAN)));
/* 224:    */         }
/* 225:    */       }
/* 226:358 */       if (!components.isEmpty()) {
/* 227:359 */         return new CompositeByteBuf(ALLOC, false, maxNumComponents, components);
/* 228:    */       }
/* 229:    */       break;
/* 230:    */     }
/* 231:363 */     return EMPTY_BUFFER;
/* 232:    */   }
/* 233:    */   
/* 234:    */   public static CompositeByteBuf compositeBuffer()
/* 235:    */   {
/* 236:370 */     return compositeBuffer(16);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public static CompositeByteBuf compositeBuffer(int maxNumComponents)
/* 240:    */   {
/* 241:377 */     return new CompositeByteBuf(ALLOC, false, maxNumComponents);
/* 242:    */   }
/* 243:    */   
/* 244:    */   public static ByteBuf copiedBuffer(byte[] array)
/* 245:    */   {
/* 246:386 */     if (array.length == 0) {
/* 247:387 */       return EMPTY_BUFFER;
/* 248:    */     }
/* 249:389 */     return wrappedBuffer((byte[])array.clone());
/* 250:    */   }
/* 251:    */   
/* 252:    */   public static ByteBuf copiedBuffer(byte[] array, int offset, int length)
/* 253:    */   {
/* 254:399 */     if (length == 0) {
/* 255:400 */       return EMPTY_BUFFER;
/* 256:    */     }
/* 257:402 */     byte[] copy = new byte[length];
/* 258:403 */     System.arraycopy(array, offset, copy, 0, length);
/* 259:404 */     return wrappedBuffer(copy);
/* 260:    */   }
/* 261:    */   
/* 262:    */   public static ByteBuf copiedBuffer(ByteBuffer buffer)
/* 263:    */   {
/* 264:414 */     int length = buffer.remaining();
/* 265:415 */     if (length == 0) {
/* 266:416 */       return EMPTY_BUFFER;
/* 267:    */     }
/* 268:418 */     byte[] copy = new byte[length];
/* 269:    */     
/* 270:    */ 
/* 271:421 */     ByteBuffer duplicate = buffer.duplicate();
/* 272:422 */     duplicate.get(copy);
/* 273:423 */     return wrappedBuffer(copy).order(duplicate.order());
/* 274:    */   }
/* 275:    */   
/* 276:    */   public static ByteBuf copiedBuffer(ByteBuf buffer)
/* 277:    */   {
/* 278:433 */     int readable = buffer.readableBytes();
/* 279:434 */     if (readable > 0)
/* 280:    */     {
/* 281:435 */       ByteBuf copy = buffer(readable);
/* 282:436 */       copy.writeBytes(buffer, buffer.readerIndex(), readable);
/* 283:437 */       return copy;
/* 284:    */     }
/* 285:439 */     return EMPTY_BUFFER;
/* 286:    */   }
/* 287:    */   
/* 288:    */   public static ByteBuf copiedBuffer(byte[]... arrays)
/* 289:    */   {
/* 290:450 */     switch (arrays.length)
/* 291:    */     {
/* 292:    */     case 0: 
/* 293:452 */       return EMPTY_BUFFER;
/* 294:    */     case 1: 
/* 295:454 */       if (arrays[0].length == 0) {
/* 296:455 */         return EMPTY_BUFFER;
/* 297:    */       }
/* 298:457 */       return copiedBuffer(arrays[0]);
/* 299:    */     }
/* 300:462 */     int length = 0;
/* 301:463 */     for (byte[] a : arrays)
/* 302:    */     {
/* 303:464 */       if (2147483647 - length < a.length) {
/* 304:465 */         throw new IllegalArgumentException("The total length of the specified arrays is too big.");
/* 305:    */       }
/* 306:468 */       length += a.length;
/* 307:    */     }
/* 308:471 */     if (length == 0) {
/* 309:472 */       return EMPTY_BUFFER;
/* 310:    */     }
/* 311:475 */     byte[] mergedArray = new byte[length];
/* 312:476 */     int i = 0;
/* 313:476 */     for (int j = 0; i < arrays.length; i++)
/* 314:    */     {
/* 315:477 */       byte[] a = arrays[i];
/* 316:478 */       System.arraycopy(a, 0, mergedArray, j, a.length);
/* 317:479 */       j += a.length;
/* 318:    */     }
/* 319:482 */     return wrappedBuffer(mergedArray);
/* 320:    */   }
/* 321:    */   
/* 322:    */   public static ByteBuf copiedBuffer(ByteBuf... buffers)
/* 323:    */   {
/* 324:496 */     switch (buffers.length)
/* 325:    */     {
/* 326:    */     case 0: 
/* 327:498 */       return EMPTY_BUFFER;
/* 328:    */     case 1: 
/* 329:500 */       return copiedBuffer(buffers[0]);
/* 330:    */     }
/* 331:504 */     ByteOrder order = null;
/* 332:505 */     int length = 0;
/* 333:506 */     for (ByteBuf b : buffers)
/* 334:    */     {
/* 335:507 */       int bLen = b.readableBytes();
/* 336:508 */       if (bLen > 0)
/* 337:    */       {
/* 338:511 */         if (2147483647 - length < bLen) {
/* 339:512 */           throw new IllegalArgumentException("The total length of the specified buffers is too big.");
/* 340:    */         }
/* 341:515 */         length += bLen;
/* 342:516 */         if (order != null)
/* 343:    */         {
/* 344:517 */           if (!order.equals(b.order())) {
/* 345:518 */             throw new IllegalArgumentException("inconsistent byte order");
/* 346:    */           }
/* 347:    */         }
/* 348:    */         else {
/* 349:521 */           order = b.order();
/* 350:    */         }
/* 351:    */       }
/* 352:    */     }
/* 353:525 */     if (length == 0) {
/* 354:526 */       return EMPTY_BUFFER;
/* 355:    */     }
/* 356:529 */     byte[] mergedArray = new byte[length];
/* 357:530 */     int i = 0;
/* 358:530 */     for (int j = 0; i < buffers.length; i++)
/* 359:    */     {
/* 360:531 */       ByteBuf b = buffers[i];
/* 361:532 */       int bLen = b.readableBytes();
/* 362:533 */       b.getBytes(b.readerIndex(), mergedArray, j, bLen);
/* 363:534 */       j += bLen;
/* 364:    */     }
/* 365:537 */     return wrappedBuffer(mergedArray).order(order);
/* 366:    */   }
/* 367:    */   
/* 368:    */   public static ByteBuf copiedBuffer(ByteBuffer... buffers)
/* 369:    */   {
/* 370:551 */     switch (buffers.length)
/* 371:    */     {
/* 372:    */     case 0: 
/* 373:553 */       return EMPTY_BUFFER;
/* 374:    */     case 1: 
/* 375:555 */       return copiedBuffer(buffers[0]);
/* 376:    */     }
/* 377:559 */     ByteOrder order = null;
/* 378:560 */     int length = 0;
/* 379:561 */     for (ByteBuffer b : buffers)
/* 380:    */     {
/* 381:562 */       int bLen = b.remaining();
/* 382:563 */       if (bLen > 0)
/* 383:    */       {
/* 384:566 */         if (2147483647 - length < bLen) {
/* 385:567 */           throw new IllegalArgumentException("The total length of the specified buffers is too big.");
/* 386:    */         }
/* 387:570 */         length += bLen;
/* 388:571 */         if (order != null)
/* 389:    */         {
/* 390:572 */           if (!order.equals(b.order())) {
/* 391:573 */             throw new IllegalArgumentException("inconsistent byte order");
/* 392:    */           }
/* 393:    */         }
/* 394:    */         else {
/* 395:576 */           order = b.order();
/* 396:    */         }
/* 397:    */       }
/* 398:    */     }
/* 399:580 */     if (length == 0) {
/* 400:581 */       return EMPTY_BUFFER;
/* 401:    */     }
/* 402:584 */     byte[] mergedArray = new byte[length];
/* 403:585 */     int i = 0;
/* 404:585 */     for (int j = 0; i < buffers.length; i++)
/* 405:    */     {
/* 406:588 */       ByteBuffer b = buffers[i].duplicate();
/* 407:589 */       int bLen = b.remaining();
/* 408:590 */       b.get(mergedArray, j, bLen);
/* 409:591 */       j += bLen;
/* 410:    */     }
/* 411:594 */     return wrappedBuffer(mergedArray).order(order);
/* 412:    */   }
/* 413:    */   
/* 414:    */   public static ByteBuf copiedBuffer(CharSequence string, Charset charset)
/* 415:    */   {
/* 416:604 */     if (string == null) {
/* 417:605 */       throw new NullPointerException("string");
/* 418:    */     }
/* 419:608 */     if ((string instanceof CharBuffer)) {
/* 420:609 */       return copiedBuffer((CharBuffer)string, charset);
/* 421:    */     }
/* 422:612 */     return copiedBuffer(CharBuffer.wrap(string), charset);
/* 423:    */   }
/* 424:    */   
/* 425:    */   public static ByteBuf copiedBuffer(CharSequence string, int offset, int length, Charset charset)
/* 426:    */   {
/* 427:623 */     if (string == null) {
/* 428:624 */       throw new NullPointerException("string");
/* 429:    */     }
/* 430:626 */     if (length == 0) {
/* 431:627 */       return EMPTY_BUFFER;
/* 432:    */     }
/* 433:630 */     if ((string instanceof CharBuffer))
/* 434:    */     {
/* 435:631 */       CharBuffer buf = (CharBuffer)string;
/* 436:632 */       if (buf.hasArray()) {
/* 437:633 */         return copiedBuffer(buf
/* 438:634 */           .array(), buf
/* 439:635 */           .arrayOffset() + buf.position() + offset, length, charset);
/* 440:    */       }
/* 441:639 */       buf = buf.slice();
/* 442:640 */       buf.limit(length);
/* 443:641 */       buf.position(offset);
/* 444:642 */       return copiedBuffer(buf, charset);
/* 445:    */     }
/* 446:645 */     return copiedBuffer(CharBuffer.wrap(string, offset, offset + length), charset);
/* 447:    */   }
/* 448:    */   
/* 449:    */   public static ByteBuf copiedBuffer(char[] array, Charset charset)
/* 450:    */   {
/* 451:655 */     if (array == null) {
/* 452:656 */       throw new NullPointerException("array");
/* 453:    */     }
/* 454:658 */     return copiedBuffer(array, 0, array.length, charset);
/* 455:    */   }
/* 456:    */   
/* 457:    */   public static ByteBuf copiedBuffer(char[] array, int offset, int length, Charset charset)
/* 458:    */   {
/* 459:668 */     if (array == null) {
/* 460:669 */       throw new NullPointerException("array");
/* 461:    */     }
/* 462:671 */     if (length == 0) {
/* 463:672 */       return EMPTY_BUFFER;
/* 464:    */     }
/* 465:674 */     return copiedBuffer(CharBuffer.wrap(array, offset, length), charset);
/* 466:    */   }
/* 467:    */   
/* 468:    */   private static ByteBuf copiedBuffer(CharBuffer buffer, Charset charset)
/* 469:    */   {
/* 470:678 */     return ByteBufUtil.encodeString0(ALLOC, true, buffer, charset, 0);
/* 471:    */   }
/* 472:    */   
/* 473:    */   @Deprecated
/* 474:    */   public static ByteBuf unmodifiableBuffer(ByteBuf buffer)
/* 475:    */   {
/* 476:691 */     ByteOrder endianness = buffer.order();
/* 477:692 */     if (endianness == BIG_ENDIAN) {
/* 478:693 */       return new ReadOnlyByteBuf(buffer);
/* 479:    */     }
/* 480:696 */     return new ReadOnlyByteBuf(buffer.order(BIG_ENDIAN)).order(LITTLE_ENDIAN);
/* 481:    */   }
/* 482:    */   
/* 483:    */   public static ByteBuf copyInt(int value)
/* 484:    */   {
/* 485:703 */     ByteBuf buf = buffer(4);
/* 486:704 */     buf.writeInt(value);
/* 487:705 */     return buf;
/* 488:    */   }
/* 489:    */   
/* 490:    */   public static ByteBuf copyInt(int... values)
/* 491:    */   {
/* 492:712 */     if ((values == null) || (values.length == 0)) {
/* 493:713 */       return EMPTY_BUFFER;
/* 494:    */     }
/* 495:715 */     ByteBuf buffer = buffer(values.length * 4);
/* 496:716 */     for (int v : values) {
/* 497:717 */       buffer.writeInt(v);
/* 498:    */     }
/* 499:719 */     return buffer;
/* 500:    */   }
/* 501:    */   
/* 502:    */   public static ByteBuf copyShort(int value)
/* 503:    */   {
/* 504:726 */     ByteBuf buf = buffer(2);
/* 505:727 */     buf.writeShort(value);
/* 506:728 */     return buf;
/* 507:    */   }
/* 508:    */   
/* 509:    */   public static ByteBuf copyShort(short... values)
/* 510:    */   {
/* 511:735 */     if ((values == null) || (values.length == 0)) {
/* 512:736 */       return EMPTY_BUFFER;
/* 513:    */     }
/* 514:738 */     ByteBuf buffer = buffer(values.length * 2);
/* 515:739 */     for (int v : values) {
/* 516:740 */       buffer.writeShort(v);
/* 517:    */     }
/* 518:742 */     return buffer;
/* 519:    */   }
/* 520:    */   
/* 521:    */   public static ByteBuf copyShort(int... values)
/* 522:    */   {
/* 523:749 */     if ((values == null) || (values.length == 0)) {
/* 524:750 */       return EMPTY_BUFFER;
/* 525:    */     }
/* 526:752 */     ByteBuf buffer = buffer(values.length * 2);
/* 527:753 */     for (int v : values) {
/* 528:754 */       buffer.writeShort(v);
/* 529:    */     }
/* 530:756 */     return buffer;
/* 531:    */   }
/* 532:    */   
/* 533:    */   public static ByteBuf copyMedium(int value)
/* 534:    */   {
/* 535:763 */     ByteBuf buf = buffer(3);
/* 536:764 */     buf.writeMedium(value);
/* 537:765 */     return buf;
/* 538:    */   }
/* 539:    */   
/* 540:    */   public static ByteBuf copyMedium(int... values)
/* 541:    */   {
/* 542:772 */     if ((values == null) || (values.length == 0)) {
/* 543:773 */       return EMPTY_BUFFER;
/* 544:    */     }
/* 545:775 */     ByteBuf buffer = buffer(values.length * 3);
/* 546:776 */     for (int v : values) {
/* 547:777 */       buffer.writeMedium(v);
/* 548:    */     }
/* 549:779 */     return buffer;
/* 550:    */   }
/* 551:    */   
/* 552:    */   public static ByteBuf copyLong(long value)
/* 553:    */   {
/* 554:786 */     ByteBuf buf = buffer(8);
/* 555:787 */     buf.writeLong(value);
/* 556:788 */     return buf;
/* 557:    */   }
/* 558:    */   
/* 559:    */   public static ByteBuf copyLong(long... values)
/* 560:    */   {
/* 561:795 */     if ((values == null) || (values.length == 0)) {
/* 562:796 */       return EMPTY_BUFFER;
/* 563:    */     }
/* 564:798 */     ByteBuf buffer = buffer(values.length * 8);
/* 565:799 */     for (long v : values) {
/* 566:800 */       buffer.writeLong(v);
/* 567:    */     }
/* 568:802 */     return buffer;
/* 569:    */   }
/* 570:    */   
/* 571:    */   public static ByteBuf copyBoolean(boolean value)
/* 572:    */   {
/* 573:809 */     ByteBuf buf = buffer(1);
/* 574:810 */     buf.writeBoolean(value);
/* 575:811 */     return buf;
/* 576:    */   }
/* 577:    */   
/* 578:    */   public static ByteBuf copyBoolean(boolean... values)
/* 579:    */   {
/* 580:818 */     if ((values == null) || (values.length == 0)) {
/* 581:819 */       return EMPTY_BUFFER;
/* 582:    */     }
/* 583:821 */     ByteBuf buffer = buffer(values.length);
/* 584:822 */     for (boolean v : values) {
/* 585:823 */       buffer.writeBoolean(v);
/* 586:    */     }
/* 587:825 */     return buffer;
/* 588:    */   }
/* 589:    */   
/* 590:    */   public static ByteBuf copyFloat(float value)
/* 591:    */   {
/* 592:832 */     ByteBuf buf = buffer(4);
/* 593:833 */     buf.writeFloat(value);
/* 594:834 */     return buf;
/* 595:    */   }
/* 596:    */   
/* 597:    */   public static ByteBuf copyFloat(float... values)
/* 598:    */   {
/* 599:841 */     if ((values == null) || (values.length == 0)) {
/* 600:842 */       return EMPTY_BUFFER;
/* 601:    */     }
/* 602:844 */     ByteBuf buffer = buffer(values.length * 4);
/* 603:845 */     for (float v : values) {
/* 604:846 */       buffer.writeFloat(v);
/* 605:    */     }
/* 606:848 */     return buffer;
/* 607:    */   }
/* 608:    */   
/* 609:    */   public static ByteBuf copyDouble(double value)
/* 610:    */   {
/* 611:855 */     ByteBuf buf = buffer(8);
/* 612:856 */     buf.writeDouble(value);
/* 613:857 */     return buf;
/* 614:    */   }
/* 615:    */   
/* 616:    */   public static ByteBuf copyDouble(double... values)
/* 617:    */   {
/* 618:864 */     if ((values == null) || (values.length == 0)) {
/* 619:865 */       return EMPTY_BUFFER;
/* 620:    */     }
/* 621:867 */     ByteBuf buffer = buffer(values.length * 8);
/* 622:868 */     for (double v : values) {
/* 623:869 */       buffer.writeDouble(v);
/* 624:    */     }
/* 625:871 */     return buffer;
/* 626:    */   }
/* 627:    */   
/* 628:    */   public static ByteBuf unreleasableBuffer(ByteBuf buf)
/* 629:    */   {
/* 630:878 */     return new UnreleasableByteBuf(buf);
/* 631:    */   }
/* 632:    */   
/* 633:    */   public static ByteBuf unmodifiableBuffer(ByteBuf... buffers)
/* 634:    */   {
/* 635:886 */     return new FixedCompositeByteBuf(ALLOC, buffers);
/* 636:    */   }
/* 637:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.Unpooled
 * JD-Core Version:    0.7.0.1
 */