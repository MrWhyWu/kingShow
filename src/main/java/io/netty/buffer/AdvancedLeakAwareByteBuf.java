/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.ByteProcessor;
/*   4:    */ import io.netty.util.ResourceLeakDetector;
/*   5:    */ import io.netty.util.ResourceLeakTracker;
/*   6:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.InputStream;
/*  11:    */ import java.io.OutputStream;
/*  12:    */ import java.nio.ByteBuffer;
/*  13:    */ import java.nio.ByteOrder;
/*  14:    */ import java.nio.channels.FileChannel;
/*  15:    */ import java.nio.channels.GatheringByteChannel;
/*  16:    */ import java.nio.channels.ScatteringByteChannel;
/*  17:    */ import java.nio.charset.Charset;
/*  18:    */ 
/*  19:    */ final class AdvancedLeakAwareByteBuf
/*  20:    */   extends SimpleLeakAwareByteBuf
/*  21:    */ {
/*  22:    */   private static final String PROP_ACQUIRE_AND_RELEASE_ONLY = "io.netty.leakDetection.acquireAndReleaseOnly";
/*  23:    */   private static final boolean ACQUIRE_AND_RELEASE_ONLY;
/*  24: 41 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AdvancedLeakAwareByteBuf.class);
/*  25:    */   
/*  26:    */   static
/*  27:    */   {
/*  28: 44 */     ACQUIRE_AND_RELEASE_ONLY = SystemPropertyUtil.getBoolean("io.netty.leakDetection.acquireAndReleaseOnly", false);
/*  29: 46 */     if (logger.isDebugEnabled()) {
/*  30: 47 */       logger.debug("-D{}: {}", "io.netty.leakDetection.acquireAndReleaseOnly", Boolean.valueOf(ACQUIRE_AND_RELEASE_ONLY));
/*  31:    */     }
/*  32: 50 */     ResourceLeakDetector.addExclusions(AdvancedLeakAwareByteBuf.class, new String[] { "touch", "recordLeakNonRefCountingOperation" });
/*  33:    */   }
/*  34:    */   
/*  35:    */   AdvancedLeakAwareByteBuf(ByteBuf buf, ResourceLeakTracker<ByteBuf> leak)
/*  36:    */   {
/*  37: 55 */     super(buf, leak);
/*  38:    */   }
/*  39:    */   
/*  40:    */   AdvancedLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak)
/*  41:    */   {
/*  42: 59 */     super(wrapped, trackedByteBuf, leak);
/*  43:    */   }
/*  44:    */   
/*  45:    */   static void recordLeakNonRefCountingOperation(ResourceLeakTracker<ByteBuf> leak)
/*  46:    */   {
/*  47: 63 */     if (!ACQUIRE_AND_RELEASE_ONLY) {
/*  48: 64 */       leak.record();
/*  49:    */     }
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ByteBuf order(ByteOrder endianness)
/*  53:    */   {
/*  54: 70 */     recordLeakNonRefCountingOperation(this.leak);
/*  55: 71 */     return super.order(endianness);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public ByteBuf slice()
/*  59:    */   {
/*  60: 76 */     recordLeakNonRefCountingOperation(this.leak);
/*  61: 77 */     return super.slice();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public ByteBuf slice(int index, int length)
/*  65:    */   {
/*  66: 82 */     recordLeakNonRefCountingOperation(this.leak);
/*  67: 83 */     return super.slice(index, length);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public ByteBuf retainedSlice()
/*  71:    */   {
/*  72: 88 */     recordLeakNonRefCountingOperation(this.leak);
/*  73: 89 */     return super.retainedSlice();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ByteBuf retainedSlice(int index, int length)
/*  77:    */   {
/*  78: 94 */     recordLeakNonRefCountingOperation(this.leak);
/*  79: 95 */     return super.retainedSlice(index, length);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public ByteBuf retainedDuplicate()
/*  83:    */   {
/*  84:100 */     recordLeakNonRefCountingOperation(this.leak);
/*  85:101 */     return super.retainedDuplicate();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ByteBuf readRetainedSlice(int length)
/*  89:    */   {
/*  90:106 */     recordLeakNonRefCountingOperation(this.leak);
/*  91:107 */     return super.readRetainedSlice(length);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public ByteBuf duplicate()
/*  95:    */   {
/*  96:112 */     recordLeakNonRefCountingOperation(this.leak);
/*  97:113 */     return super.duplicate();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public ByteBuf readSlice(int length)
/* 101:    */   {
/* 102:118 */     recordLeakNonRefCountingOperation(this.leak);
/* 103:119 */     return super.readSlice(length);
/* 104:    */   }
/* 105:    */   
/* 106:    */   public ByteBuf discardReadBytes()
/* 107:    */   {
/* 108:124 */     recordLeakNonRefCountingOperation(this.leak);
/* 109:125 */     return super.discardReadBytes();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ByteBuf discardSomeReadBytes()
/* 113:    */   {
/* 114:130 */     recordLeakNonRefCountingOperation(this.leak);
/* 115:131 */     return super.discardSomeReadBytes();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public ByteBuf ensureWritable(int minWritableBytes)
/* 119:    */   {
/* 120:136 */     recordLeakNonRefCountingOperation(this.leak);
/* 121:137 */     return super.ensureWritable(minWritableBytes);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public int ensureWritable(int minWritableBytes, boolean force)
/* 125:    */   {
/* 126:142 */     recordLeakNonRefCountingOperation(this.leak);
/* 127:143 */     return super.ensureWritable(minWritableBytes, force);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public boolean getBoolean(int index)
/* 131:    */   {
/* 132:148 */     recordLeakNonRefCountingOperation(this.leak);
/* 133:149 */     return super.getBoolean(index);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public byte getByte(int index)
/* 137:    */   {
/* 138:154 */     recordLeakNonRefCountingOperation(this.leak);
/* 139:155 */     return super.getByte(index);
/* 140:    */   }
/* 141:    */   
/* 142:    */   public short getUnsignedByte(int index)
/* 143:    */   {
/* 144:160 */     recordLeakNonRefCountingOperation(this.leak);
/* 145:161 */     return super.getUnsignedByte(index);
/* 146:    */   }
/* 147:    */   
/* 148:    */   public short getShort(int index)
/* 149:    */   {
/* 150:166 */     recordLeakNonRefCountingOperation(this.leak);
/* 151:167 */     return super.getShort(index);
/* 152:    */   }
/* 153:    */   
/* 154:    */   public int getUnsignedShort(int index)
/* 155:    */   {
/* 156:172 */     recordLeakNonRefCountingOperation(this.leak);
/* 157:173 */     return super.getUnsignedShort(index);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public int getMedium(int index)
/* 161:    */   {
/* 162:178 */     recordLeakNonRefCountingOperation(this.leak);
/* 163:179 */     return super.getMedium(index);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public int getUnsignedMedium(int index)
/* 167:    */   {
/* 168:184 */     recordLeakNonRefCountingOperation(this.leak);
/* 169:185 */     return super.getUnsignedMedium(index);
/* 170:    */   }
/* 171:    */   
/* 172:    */   public int getInt(int index)
/* 173:    */   {
/* 174:190 */     recordLeakNonRefCountingOperation(this.leak);
/* 175:191 */     return super.getInt(index);
/* 176:    */   }
/* 177:    */   
/* 178:    */   public long getUnsignedInt(int index)
/* 179:    */   {
/* 180:196 */     recordLeakNonRefCountingOperation(this.leak);
/* 181:197 */     return super.getUnsignedInt(index);
/* 182:    */   }
/* 183:    */   
/* 184:    */   public long getLong(int index)
/* 185:    */   {
/* 186:202 */     recordLeakNonRefCountingOperation(this.leak);
/* 187:203 */     return super.getLong(index);
/* 188:    */   }
/* 189:    */   
/* 190:    */   public char getChar(int index)
/* 191:    */   {
/* 192:208 */     recordLeakNonRefCountingOperation(this.leak);
/* 193:209 */     return super.getChar(index);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public float getFloat(int index)
/* 197:    */   {
/* 198:214 */     recordLeakNonRefCountingOperation(this.leak);
/* 199:215 */     return super.getFloat(index);
/* 200:    */   }
/* 201:    */   
/* 202:    */   public double getDouble(int index)
/* 203:    */   {
/* 204:220 */     recordLeakNonRefCountingOperation(this.leak);
/* 205:221 */     return super.getDouble(index);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public ByteBuf getBytes(int index, ByteBuf dst)
/* 209:    */   {
/* 210:226 */     recordLeakNonRefCountingOperation(this.leak);
/* 211:227 */     return super.getBytes(index, dst);
/* 212:    */   }
/* 213:    */   
/* 214:    */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/* 215:    */   {
/* 216:232 */     recordLeakNonRefCountingOperation(this.leak);
/* 217:233 */     return super.getBytes(index, dst, length);
/* 218:    */   }
/* 219:    */   
/* 220:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 221:    */   {
/* 222:238 */     recordLeakNonRefCountingOperation(this.leak);
/* 223:239 */     return super.getBytes(index, dst, dstIndex, length);
/* 224:    */   }
/* 225:    */   
/* 226:    */   public ByteBuf getBytes(int index, byte[] dst)
/* 227:    */   {
/* 228:244 */     recordLeakNonRefCountingOperation(this.leak);
/* 229:245 */     return super.getBytes(index, dst);
/* 230:    */   }
/* 231:    */   
/* 232:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 233:    */   {
/* 234:250 */     recordLeakNonRefCountingOperation(this.leak);
/* 235:251 */     return super.getBytes(index, dst, dstIndex, length);
/* 236:    */   }
/* 237:    */   
/* 238:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 239:    */   {
/* 240:256 */     recordLeakNonRefCountingOperation(this.leak);
/* 241:257 */     return super.getBytes(index, dst);
/* 242:    */   }
/* 243:    */   
/* 244:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 245:    */     throws IOException
/* 246:    */   {
/* 247:262 */     recordLeakNonRefCountingOperation(this.leak);
/* 248:263 */     return super.getBytes(index, out, length);
/* 249:    */   }
/* 250:    */   
/* 251:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 252:    */     throws IOException
/* 253:    */   {
/* 254:268 */     recordLeakNonRefCountingOperation(this.leak);
/* 255:269 */     return super.getBytes(index, out, length);
/* 256:    */   }
/* 257:    */   
/* 258:    */   public CharSequence getCharSequence(int index, int length, Charset charset)
/* 259:    */   {
/* 260:274 */     recordLeakNonRefCountingOperation(this.leak);
/* 261:275 */     return super.getCharSequence(index, length, charset);
/* 262:    */   }
/* 263:    */   
/* 264:    */   public ByteBuf setBoolean(int index, boolean value)
/* 265:    */   {
/* 266:280 */     recordLeakNonRefCountingOperation(this.leak);
/* 267:281 */     return super.setBoolean(index, value);
/* 268:    */   }
/* 269:    */   
/* 270:    */   public ByteBuf setByte(int index, int value)
/* 271:    */   {
/* 272:286 */     recordLeakNonRefCountingOperation(this.leak);
/* 273:287 */     return super.setByte(index, value);
/* 274:    */   }
/* 275:    */   
/* 276:    */   public ByteBuf setShort(int index, int value)
/* 277:    */   {
/* 278:292 */     recordLeakNonRefCountingOperation(this.leak);
/* 279:293 */     return super.setShort(index, value);
/* 280:    */   }
/* 281:    */   
/* 282:    */   public ByteBuf setMedium(int index, int value)
/* 283:    */   {
/* 284:298 */     recordLeakNonRefCountingOperation(this.leak);
/* 285:299 */     return super.setMedium(index, value);
/* 286:    */   }
/* 287:    */   
/* 288:    */   public ByteBuf setInt(int index, int value)
/* 289:    */   {
/* 290:304 */     recordLeakNonRefCountingOperation(this.leak);
/* 291:305 */     return super.setInt(index, value);
/* 292:    */   }
/* 293:    */   
/* 294:    */   public ByteBuf setLong(int index, long value)
/* 295:    */   {
/* 296:310 */     recordLeakNonRefCountingOperation(this.leak);
/* 297:311 */     return super.setLong(index, value);
/* 298:    */   }
/* 299:    */   
/* 300:    */   public ByteBuf setChar(int index, int value)
/* 301:    */   {
/* 302:316 */     recordLeakNonRefCountingOperation(this.leak);
/* 303:317 */     return super.setChar(index, value);
/* 304:    */   }
/* 305:    */   
/* 306:    */   public ByteBuf setFloat(int index, float value)
/* 307:    */   {
/* 308:322 */     recordLeakNonRefCountingOperation(this.leak);
/* 309:323 */     return super.setFloat(index, value);
/* 310:    */   }
/* 311:    */   
/* 312:    */   public ByteBuf setDouble(int index, double value)
/* 313:    */   {
/* 314:328 */     recordLeakNonRefCountingOperation(this.leak);
/* 315:329 */     return super.setDouble(index, value);
/* 316:    */   }
/* 317:    */   
/* 318:    */   public ByteBuf setBytes(int index, ByteBuf src)
/* 319:    */   {
/* 320:334 */     recordLeakNonRefCountingOperation(this.leak);
/* 321:335 */     return super.setBytes(index, src);
/* 322:    */   }
/* 323:    */   
/* 324:    */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/* 325:    */   {
/* 326:340 */     recordLeakNonRefCountingOperation(this.leak);
/* 327:341 */     return super.setBytes(index, src, length);
/* 328:    */   }
/* 329:    */   
/* 330:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 331:    */   {
/* 332:346 */     recordLeakNonRefCountingOperation(this.leak);
/* 333:347 */     return super.setBytes(index, src, srcIndex, length);
/* 334:    */   }
/* 335:    */   
/* 336:    */   public ByteBuf setBytes(int index, byte[] src)
/* 337:    */   {
/* 338:352 */     recordLeakNonRefCountingOperation(this.leak);
/* 339:353 */     return super.setBytes(index, src);
/* 340:    */   }
/* 341:    */   
/* 342:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 343:    */   {
/* 344:358 */     recordLeakNonRefCountingOperation(this.leak);
/* 345:359 */     return super.setBytes(index, src, srcIndex, length);
/* 346:    */   }
/* 347:    */   
/* 348:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 349:    */   {
/* 350:364 */     recordLeakNonRefCountingOperation(this.leak);
/* 351:365 */     return super.setBytes(index, src);
/* 352:    */   }
/* 353:    */   
/* 354:    */   public int setBytes(int index, InputStream in, int length)
/* 355:    */     throws IOException
/* 356:    */   {
/* 357:370 */     recordLeakNonRefCountingOperation(this.leak);
/* 358:371 */     return super.setBytes(index, in, length);
/* 359:    */   }
/* 360:    */   
/* 361:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 362:    */     throws IOException
/* 363:    */   {
/* 364:376 */     recordLeakNonRefCountingOperation(this.leak);
/* 365:377 */     return super.setBytes(index, in, length);
/* 366:    */   }
/* 367:    */   
/* 368:    */   public ByteBuf setZero(int index, int length)
/* 369:    */   {
/* 370:382 */     recordLeakNonRefCountingOperation(this.leak);
/* 371:383 */     return super.setZero(index, length);
/* 372:    */   }
/* 373:    */   
/* 374:    */   public int setCharSequence(int index, CharSequence sequence, Charset charset)
/* 375:    */   {
/* 376:388 */     recordLeakNonRefCountingOperation(this.leak);
/* 377:389 */     return super.setCharSequence(index, sequence, charset);
/* 378:    */   }
/* 379:    */   
/* 380:    */   public boolean readBoolean()
/* 381:    */   {
/* 382:394 */     recordLeakNonRefCountingOperation(this.leak);
/* 383:395 */     return super.readBoolean();
/* 384:    */   }
/* 385:    */   
/* 386:    */   public byte readByte()
/* 387:    */   {
/* 388:400 */     recordLeakNonRefCountingOperation(this.leak);
/* 389:401 */     return super.readByte();
/* 390:    */   }
/* 391:    */   
/* 392:    */   public short readUnsignedByte()
/* 393:    */   {
/* 394:406 */     recordLeakNonRefCountingOperation(this.leak);
/* 395:407 */     return super.readUnsignedByte();
/* 396:    */   }
/* 397:    */   
/* 398:    */   public short readShort()
/* 399:    */   {
/* 400:412 */     recordLeakNonRefCountingOperation(this.leak);
/* 401:413 */     return super.readShort();
/* 402:    */   }
/* 403:    */   
/* 404:    */   public int readUnsignedShort()
/* 405:    */   {
/* 406:418 */     recordLeakNonRefCountingOperation(this.leak);
/* 407:419 */     return super.readUnsignedShort();
/* 408:    */   }
/* 409:    */   
/* 410:    */   public int readMedium()
/* 411:    */   {
/* 412:424 */     recordLeakNonRefCountingOperation(this.leak);
/* 413:425 */     return super.readMedium();
/* 414:    */   }
/* 415:    */   
/* 416:    */   public int readUnsignedMedium()
/* 417:    */   {
/* 418:430 */     recordLeakNonRefCountingOperation(this.leak);
/* 419:431 */     return super.readUnsignedMedium();
/* 420:    */   }
/* 421:    */   
/* 422:    */   public int readInt()
/* 423:    */   {
/* 424:436 */     recordLeakNonRefCountingOperation(this.leak);
/* 425:437 */     return super.readInt();
/* 426:    */   }
/* 427:    */   
/* 428:    */   public long readUnsignedInt()
/* 429:    */   {
/* 430:442 */     recordLeakNonRefCountingOperation(this.leak);
/* 431:443 */     return super.readUnsignedInt();
/* 432:    */   }
/* 433:    */   
/* 434:    */   public long readLong()
/* 435:    */   {
/* 436:448 */     recordLeakNonRefCountingOperation(this.leak);
/* 437:449 */     return super.readLong();
/* 438:    */   }
/* 439:    */   
/* 440:    */   public char readChar()
/* 441:    */   {
/* 442:454 */     recordLeakNonRefCountingOperation(this.leak);
/* 443:455 */     return super.readChar();
/* 444:    */   }
/* 445:    */   
/* 446:    */   public float readFloat()
/* 447:    */   {
/* 448:460 */     recordLeakNonRefCountingOperation(this.leak);
/* 449:461 */     return super.readFloat();
/* 450:    */   }
/* 451:    */   
/* 452:    */   public double readDouble()
/* 453:    */   {
/* 454:466 */     recordLeakNonRefCountingOperation(this.leak);
/* 455:467 */     return super.readDouble();
/* 456:    */   }
/* 457:    */   
/* 458:    */   public ByteBuf readBytes(int length)
/* 459:    */   {
/* 460:472 */     recordLeakNonRefCountingOperation(this.leak);
/* 461:473 */     return super.readBytes(length);
/* 462:    */   }
/* 463:    */   
/* 464:    */   public ByteBuf readBytes(ByteBuf dst)
/* 465:    */   {
/* 466:478 */     recordLeakNonRefCountingOperation(this.leak);
/* 467:479 */     return super.readBytes(dst);
/* 468:    */   }
/* 469:    */   
/* 470:    */   public ByteBuf readBytes(ByteBuf dst, int length)
/* 471:    */   {
/* 472:484 */     recordLeakNonRefCountingOperation(this.leak);
/* 473:485 */     return super.readBytes(dst, length);
/* 474:    */   }
/* 475:    */   
/* 476:    */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 477:    */   {
/* 478:490 */     recordLeakNonRefCountingOperation(this.leak);
/* 479:491 */     return super.readBytes(dst, dstIndex, length);
/* 480:    */   }
/* 481:    */   
/* 482:    */   public ByteBuf readBytes(byte[] dst)
/* 483:    */   {
/* 484:496 */     recordLeakNonRefCountingOperation(this.leak);
/* 485:497 */     return super.readBytes(dst);
/* 486:    */   }
/* 487:    */   
/* 488:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 489:    */   {
/* 490:502 */     recordLeakNonRefCountingOperation(this.leak);
/* 491:503 */     return super.readBytes(dst, dstIndex, length);
/* 492:    */   }
/* 493:    */   
/* 494:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 495:    */   {
/* 496:508 */     recordLeakNonRefCountingOperation(this.leak);
/* 497:509 */     return super.readBytes(dst);
/* 498:    */   }
/* 499:    */   
/* 500:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 501:    */     throws IOException
/* 502:    */   {
/* 503:514 */     recordLeakNonRefCountingOperation(this.leak);
/* 504:515 */     return super.readBytes(out, length);
/* 505:    */   }
/* 506:    */   
/* 507:    */   public int readBytes(GatheringByteChannel out, int length)
/* 508:    */     throws IOException
/* 509:    */   {
/* 510:520 */     recordLeakNonRefCountingOperation(this.leak);
/* 511:521 */     return super.readBytes(out, length);
/* 512:    */   }
/* 513:    */   
/* 514:    */   public CharSequence readCharSequence(int length, Charset charset)
/* 515:    */   {
/* 516:526 */     recordLeakNonRefCountingOperation(this.leak);
/* 517:527 */     return super.readCharSequence(length, charset);
/* 518:    */   }
/* 519:    */   
/* 520:    */   public ByteBuf skipBytes(int length)
/* 521:    */   {
/* 522:532 */     recordLeakNonRefCountingOperation(this.leak);
/* 523:533 */     return super.skipBytes(length);
/* 524:    */   }
/* 525:    */   
/* 526:    */   public ByteBuf writeBoolean(boolean value)
/* 527:    */   {
/* 528:538 */     recordLeakNonRefCountingOperation(this.leak);
/* 529:539 */     return super.writeBoolean(value);
/* 530:    */   }
/* 531:    */   
/* 532:    */   public ByteBuf writeByte(int value)
/* 533:    */   {
/* 534:544 */     recordLeakNonRefCountingOperation(this.leak);
/* 535:545 */     return super.writeByte(value);
/* 536:    */   }
/* 537:    */   
/* 538:    */   public ByteBuf writeShort(int value)
/* 539:    */   {
/* 540:550 */     recordLeakNonRefCountingOperation(this.leak);
/* 541:551 */     return super.writeShort(value);
/* 542:    */   }
/* 543:    */   
/* 544:    */   public ByteBuf writeMedium(int value)
/* 545:    */   {
/* 546:556 */     recordLeakNonRefCountingOperation(this.leak);
/* 547:557 */     return super.writeMedium(value);
/* 548:    */   }
/* 549:    */   
/* 550:    */   public ByteBuf writeInt(int value)
/* 551:    */   {
/* 552:562 */     recordLeakNonRefCountingOperation(this.leak);
/* 553:563 */     return super.writeInt(value);
/* 554:    */   }
/* 555:    */   
/* 556:    */   public ByteBuf writeLong(long value)
/* 557:    */   {
/* 558:568 */     recordLeakNonRefCountingOperation(this.leak);
/* 559:569 */     return super.writeLong(value);
/* 560:    */   }
/* 561:    */   
/* 562:    */   public ByteBuf writeChar(int value)
/* 563:    */   {
/* 564:574 */     recordLeakNonRefCountingOperation(this.leak);
/* 565:575 */     return super.writeChar(value);
/* 566:    */   }
/* 567:    */   
/* 568:    */   public ByteBuf writeFloat(float value)
/* 569:    */   {
/* 570:580 */     recordLeakNonRefCountingOperation(this.leak);
/* 571:581 */     return super.writeFloat(value);
/* 572:    */   }
/* 573:    */   
/* 574:    */   public ByteBuf writeDouble(double value)
/* 575:    */   {
/* 576:586 */     recordLeakNonRefCountingOperation(this.leak);
/* 577:587 */     return super.writeDouble(value);
/* 578:    */   }
/* 579:    */   
/* 580:    */   public ByteBuf writeBytes(ByteBuf src)
/* 581:    */   {
/* 582:592 */     recordLeakNonRefCountingOperation(this.leak);
/* 583:593 */     return super.writeBytes(src);
/* 584:    */   }
/* 585:    */   
/* 586:    */   public ByteBuf writeBytes(ByteBuf src, int length)
/* 587:    */   {
/* 588:598 */     recordLeakNonRefCountingOperation(this.leak);
/* 589:599 */     return super.writeBytes(src, length);
/* 590:    */   }
/* 591:    */   
/* 592:    */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 593:    */   {
/* 594:604 */     recordLeakNonRefCountingOperation(this.leak);
/* 595:605 */     return super.writeBytes(src, srcIndex, length);
/* 596:    */   }
/* 597:    */   
/* 598:    */   public ByteBuf writeBytes(byte[] src)
/* 599:    */   {
/* 600:610 */     recordLeakNonRefCountingOperation(this.leak);
/* 601:611 */     return super.writeBytes(src);
/* 602:    */   }
/* 603:    */   
/* 604:    */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 605:    */   {
/* 606:616 */     recordLeakNonRefCountingOperation(this.leak);
/* 607:617 */     return super.writeBytes(src, srcIndex, length);
/* 608:    */   }
/* 609:    */   
/* 610:    */   public ByteBuf writeBytes(ByteBuffer src)
/* 611:    */   {
/* 612:622 */     recordLeakNonRefCountingOperation(this.leak);
/* 613:623 */     return super.writeBytes(src);
/* 614:    */   }
/* 615:    */   
/* 616:    */   public int writeBytes(InputStream in, int length)
/* 617:    */     throws IOException
/* 618:    */   {
/* 619:628 */     recordLeakNonRefCountingOperation(this.leak);
/* 620:629 */     return super.writeBytes(in, length);
/* 621:    */   }
/* 622:    */   
/* 623:    */   public int writeBytes(ScatteringByteChannel in, int length)
/* 624:    */     throws IOException
/* 625:    */   {
/* 626:634 */     recordLeakNonRefCountingOperation(this.leak);
/* 627:635 */     return super.writeBytes(in, length);
/* 628:    */   }
/* 629:    */   
/* 630:    */   public ByteBuf writeZero(int length)
/* 631:    */   {
/* 632:640 */     recordLeakNonRefCountingOperation(this.leak);
/* 633:641 */     return super.writeZero(length);
/* 634:    */   }
/* 635:    */   
/* 636:    */   public int indexOf(int fromIndex, int toIndex, byte value)
/* 637:    */   {
/* 638:646 */     recordLeakNonRefCountingOperation(this.leak);
/* 639:647 */     return super.indexOf(fromIndex, toIndex, value);
/* 640:    */   }
/* 641:    */   
/* 642:    */   public int bytesBefore(byte value)
/* 643:    */   {
/* 644:652 */     recordLeakNonRefCountingOperation(this.leak);
/* 645:653 */     return super.bytesBefore(value);
/* 646:    */   }
/* 647:    */   
/* 648:    */   public int bytesBefore(int length, byte value)
/* 649:    */   {
/* 650:658 */     recordLeakNonRefCountingOperation(this.leak);
/* 651:659 */     return super.bytesBefore(length, value);
/* 652:    */   }
/* 653:    */   
/* 654:    */   public int bytesBefore(int index, int length, byte value)
/* 655:    */   {
/* 656:664 */     recordLeakNonRefCountingOperation(this.leak);
/* 657:665 */     return super.bytesBefore(index, length, value);
/* 658:    */   }
/* 659:    */   
/* 660:    */   public int forEachByte(ByteProcessor processor)
/* 661:    */   {
/* 662:670 */     recordLeakNonRefCountingOperation(this.leak);
/* 663:671 */     return super.forEachByte(processor);
/* 664:    */   }
/* 665:    */   
/* 666:    */   public int forEachByte(int index, int length, ByteProcessor processor)
/* 667:    */   {
/* 668:676 */     recordLeakNonRefCountingOperation(this.leak);
/* 669:677 */     return super.forEachByte(index, length, processor);
/* 670:    */   }
/* 671:    */   
/* 672:    */   public int forEachByteDesc(ByteProcessor processor)
/* 673:    */   {
/* 674:682 */     recordLeakNonRefCountingOperation(this.leak);
/* 675:683 */     return super.forEachByteDesc(processor);
/* 676:    */   }
/* 677:    */   
/* 678:    */   public int forEachByteDesc(int index, int length, ByteProcessor processor)
/* 679:    */   {
/* 680:688 */     recordLeakNonRefCountingOperation(this.leak);
/* 681:689 */     return super.forEachByteDesc(index, length, processor);
/* 682:    */   }
/* 683:    */   
/* 684:    */   public ByteBuf copy()
/* 685:    */   {
/* 686:694 */     recordLeakNonRefCountingOperation(this.leak);
/* 687:695 */     return super.copy();
/* 688:    */   }
/* 689:    */   
/* 690:    */   public ByteBuf copy(int index, int length)
/* 691:    */   {
/* 692:700 */     recordLeakNonRefCountingOperation(this.leak);
/* 693:701 */     return super.copy(index, length);
/* 694:    */   }
/* 695:    */   
/* 696:    */   public int nioBufferCount()
/* 697:    */   {
/* 698:706 */     recordLeakNonRefCountingOperation(this.leak);
/* 699:707 */     return super.nioBufferCount();
/* 700:    */   }
/* 701:    */   
/* 702:    */   public ByteBuffer nioBuffer()
/* 703:    */   {
/* 704:712 */     recordLeakNonRefCountingOperation(this.leak);
/* 705:713 */     return super.nioBuffer();
/* 706:    */   }
/* 707:    */   
/* 708:    */   public ByteBuffer nioBuffer(int index, int length)
/* 709:    */   {
/* 710:718 */     recordLeakNonRefCountingOperation(this.leak);
/* 711:719 */     return super.nioBuffer(index, length);
/* 712:    */   }
/* 713:    */   
/* 714:    */   public ByteBuffer[] nioBuffers()
/* 715:    */   {
/* 716:724 */     recordLeakNonRefCountingOperation(this.leak);
/* 717:725 */     return super.nioBuffers();
/* 718:    */   }
/* 719:    */   
/* 720:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 721:    */   {
/* 722:730 */     recordLeakNonRefCountingOperation(this.leak);
/* 723:731 */     return super.nioBuffers(index, length);
/* 724:    */   }
/* 725:    */   
/* 726:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 727:    */   {
/* 728:736 */     recordLeakNonRefCountingOperation(this.leak);
/* 729:737 */     return super.internalNioBuffer(index, length);
/* 730:    */   }
/* 731:    */   
/* 732:    */   public String toString(Charset charset)
/* 733:    */   {
/* 734:742 */     recordLeakNonRefCountingOperation(this.leak);
/* 735:743 */     return super.toString(charset);
/* 736:    */   }
/* 737:    */   
/* 738:    */   public String toString(int index, int length, Charset charset)
/* 739:    */   {
/* 740:748 */     recordLeakNonRefCountingOperation(this.leak);
/* 741:749 */     return super.toString(index, length, charset);
/* 742:    */   }
/* 743:    */   
/* 744:    */   public ByteBuf capacity(int newCapacity)
/* 745:    */   {
/* 746:754 */     recordLeakNonRefCountingOperation(this.leak);
/* 747:755 */     return super.capacity(newCapacity);
/* 748:    */   }
/* 749:    */   
/* 750:    */   public short getShortLE(int index)
/* 751:    */   {
/* 752:760 */     recordLeakNonRefCountingOperation(this.leak);
/* 753:761 */     return super.getShortLE(index);
/* 754:    */   }
/* 755:    */   
/* 756:    */   public int getUnsignedShortLE(int index)
/* 757:    */   {
/* 758:766 */     recordLeakNonRefCountingOperation(this.leak);
/* 759:767 */     return super.getUnsignedShortLE(index);
/* 760:    */   }
/* 761:    */   
/* 762:    */   public int getMediumLE(int index)
/* 763:    */   {
/* 764:772 */     recordLeakNonRefCountingOperation(this.leak);
/* 765:773 */     return super.getMediumLE(index);
/* 766:    */   }
/* 767:    */   
/* 768:    */   public int getUnsignedMediumLE(int index)
/* 769:    */   {
/* 770:778 */     recordLeakNonRefCountingOperation(this.leak);
/* 771:779 */     return super.getUnsignedMediumLE(index);
/* 772:    */   }
/* 773:    */   
/* 774:    */   public int getIntLE(int index)
/* 775:    */   {
/* 776:784 */     recordLeakNonRefCountingOperation(this.leak);
/* 777:785 */     return super.getIntLE(index);
/* 778:    */   }
/* 779:    */   
/* 780:    */   public long getUnsignedIntLE(int index)
/* 781:    */   {
/* 782:790 */     recordLeakNonRefCountingOperation(this.leak);
/* 783:791 */     return super.getUnsignedIntLE(index);
/* 784:    */   }
/* 785:    */   
/* 786:    */   public long getLongLE(int index)
/* 787:    */   {
/* 788:796 */     recordLeakNonRefCountingOperation(this.leak);
/* 789:797 */     return super.getLongLE(index);
/* 790:    */   }
/* 791:    */   
/* 792:    */   public ByteBuf setShortLE(int index, int value)
/* 793:    */   {
/* 794:802 */     recordLeakNonRefCountingOperation(this.leak);
/* 795:803 */     return super.setShortLE(index, value);
/* 796:    */   }
/* 797:    */   
/* 798:    */   public ByteBuf setIntLE(int index, int value)
/* 799:    */   {
/* 800:808 */     recordLeakNonRefCountingOperation(this.leak);
/* 801:809 */     return super.setIntLE(index, value);
/* 802:    */   }
/* 803:    */   
/* 804:    */   public ByteBuf setMediumLE(int index, int value)
/* 805:    */   {
/* 806:814 */     recordLeakNonRefCountingOperation(this.leak);
/* 807:815 */     return super.setMediumLE(index, value);
/* 808:    */   }
/* 809:    */   
/* 810:    */   public ByteBuf setLongLE(int index, long value)
/* 811:    */   {
/* 812:820 */     recordLeakNonRefCountingOperation(this.leak);
/* 813:821 */     return super.setLongLE(index, value);
/* 814:    */   }
/* 815:    */   
/* 816:    */   public short readShortLE()
/* 817:    */   {
/* 818:826 */     recordLeakNonRefCountingOperation(this.leak);
/* 819:827 */     return super.readShortLE();
/* 820:    */   }
/* 821:    */   
/* 822:    */   public int readUnsignedShortLE()
/* 823:    */   {
/* 824:832 */     recordLeakNonRefCountingOperation(this.leak);
/* 825:833 */     return super.readUnsignedShortLE();
/* 826:    */   }
/* 827:    */   
/* 828:    */   public int readMediumLE()
/* 829:    */   {
/* 830:838 */     recordLeakNonRefCountingOperation(this.leak);
/* 831:839 */     return super.readMediumLE();
/* 832:    */   }
/* 833:    */   
/* 834:    */   public int readUnsignedMediumLE()
/* 835:    */   {
/* 836:844 */     recordLeakNonRefCountingOperation(this.leak);
/* 837:845 */     return super.readUnsignedMediumLE();
/* 838:    */   }
/* 839:    */   
/* 840:    */   public int readIntLE()
/* 841:    */   {
/* 842:850 */     recordLeakNonRefCountingOperation(this.leak);
/* 843:851 */     return super.readIntLE();
/* 844:    */   }
/* 845:    */   
/* 846:    */   public long readUnsignedIntLE()
/* 847:    */   {
/* 848:856 */     recordLeakNonRefCountingOperation(this.leak);
/* 849:857 */     return super.readUnsignedIntLE();
/* 850:    */   }
/* 851:    */   
/* 852:    */   public long readLongLE()
/* 853:    */   {
/* 854:862 */     recordLeakNonRefCountingOperation(this.leak);
/* 855:863 */     return super.readLongLE();
/* 856:    */   }
/* 857:    */   
/* 858:    */   public ByteBuf writeShortLE(int value)
/* 859:    */   {
/* 860:868 */     recordLeakNonRefCountingOperation(this.leak);
/* 861:869 */     return super.writeShortLE(value);
/* 862:    */   }
/* 863:    */   
/* 864:    */   public ByteBuf writeMediumLE(int value)
/* 865:    */   {
/* 866:874 */     recordLeakNonRefCountingOperation(this.leak);
/* 867:875 */     return super.writeMediumLE(value);
/* 868:    */   }
/* 869:    */   
/* 870:    */   public ByteBuf writeIntLE(int value)
/* 871:    */   {
/* 872:880 */     recordLeakNonRefCountingOperation(this.leak);
/* 873:881 */     return super.writeIntLE(value);
/* 874:    */   }
/* 875:    */   
/* 876:    */   public ByteBuf writeLongLE(long value)
/* 877:    */   {
/* 878:886 */     recordLeakNonRefCountingOperation(this.leak);
/* 879:887 */     return super.writeLongLE(value);
/* 880:    */   }
/* 881:    */   
/* 882:    */   public int writeCharSequence(CharSequence sequence, Charset charset)
/* 883:    */   {
/* 884:892 */     recordLeakNonRefCountingOperation(this.leak);
/* 885:893 */     return super.writeCharSequence(sequence, charset);
/* 886:    */   }
/* 887:    */   
/* 888:    */   public int getBytes(int index, FileChannel out, long position, int length)
/* 889:    */     throws IOException
/* 890:    */   {
/* 891:898 */     recordLeakNonRefCountingOperation(this.leak);
/* 892:899 */     return super.getBytes(index, out, position, length);
/* 893:    */   }
/* 894:    */   
/* 895:    */   public int setBytes(int index, FileChannel in, long position, int length)
/* 896:    */     throws IOException
/* 897:    */   {
/* 898:904 */     recordLeakNonRefCountingOperation(this.leak);
/* 899:905 */     return super.setBytes(index, in, position, length);
/* 900:    */   }
/* 901:    */   
/* 902:    */   public int readBytes(FileChannel out, long position, int length)
/* 903:    */     throws IOException
/* 904:    */   {
/* 905:910 */     recordLeakNonRefCountingOperation(this.leak);
/* 906:911 */     return super.readBytes(out, position, length);
/* 907:    */   }
/* 908:    */   
/* 909:    */   public int writeBytes(FileChannel in, long position, int length)
/* 910:    */     throws IOException
/* 911:    */   {
/* 912:916 */     recordLeakNonRefCountingOperation(this.leak);
/* 913:917 */     return super.writeBytes(in, position, length);
/* 914:    */   }
/* 915:    */   
/* 916:    */   public ByteBuf asReadOnly()
/* 917:    */   {
/* 918:922 */     recordLeakNonRefCountingOperation(this.leak);
/* 919:923 */     return super.asReadOnly();
/* 920:    */   }
/* 921:    */   
/* 922:    */   public ByteBuf retain()
/* 923:    */   {
/* 924:928 */     this.leak.record();
/* 925:929 */     return super.retain();
/* 926:    */   }
/* 927:    */   
/* 928:    */   public ByteBuf retain(int increment)
/* 929:    */   {
/* 930:934 */     this.leak.record();
/* 931:935 */     return super.retain(increment);
/* 932:    */   }
/* 933:    */   
/* 934:    */   public boolean release()
/* 935:    */   {
/* 936:940 */     this.leak.record();
/* 937:941 */     return super.release();
/* 938:    */   }
/* 939:    */   
/* 940:    */   public boolean release(int decrement)
/* 941:    */   {
/* 942:946 */     this.leak.record();
/* 943:947 */     return super.release(decrement);
/* 944:    */   }
/* 945:    */   
/* 946:    */   public ByteBuf touch()
/* 947:    */   {
/* 948:952 */     this.leak.record();
/* 949:953 */     return this;
/* 950:    */   }
/* 951:    */   
/* 952:    */   public ByteBuf touch(Object hint)
/* 953:    */   {
/* 954:958 */     this.leak.record(hint);
/* 955:959 */     return this;
/* 956:    */   }
/* 957:    */   
/* 958:    */   protected AdvancedLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker)
/* 959:    */   {
/* 960:965 */     return new AdvancedLeakAwareByteBuf(buf, trackedByteBuf, leakTracker);
/* 961:    */   }
/* 962:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.buffer.AdvancedLeakAwareByteBuf
 * JD-Core Version:    0.7.0.1
 */