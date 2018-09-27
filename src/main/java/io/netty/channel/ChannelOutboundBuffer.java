/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.util.Recycler;
/*   7:    */ import io.netty.util.Recycler.Handle;
/*   8:    */ import io.netty.util.ReferenceCountUtil;
/*   9:    */ import io.netty.util.concurrent.FastThreadLocal;
/*  10:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*  11:    */ import io.netty.util.internal.PromiseNotificationUtil;
/*  12:    */ import io.netty.util.internal.SystemPropertyUtil;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  15:    */ import java.nio.ByteBuffer;
/*  16:    */ import java.nio.channels.ClosedChannelException;
/*  17:    */ import java.util.Arrays;
/*  18:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  19:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  20:    */ 
/*  21:    */ public final class ChannelOutboundBuffer
/*  22:    */ {
/*  23: 61 */   static final int CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD = SystemPropertyUtil.getInt("io.netty.transport.outboundBufferEntrySizeOverhead", 96);
/*  24: 63 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelOutboundBuffer.class);
/*  25: 65 */   private static final FastThreadLocal<ByteBuffer[]> NIO_BUFFERS = new FastThreadLocal()
/*  26:    */   {
/*  27:    */     protected ByteBuffer[] initialValue()
/*  28:    */       throws Exception
/*  29:    */     {
/*  30: 68 */       return new ByteBuffer[1024];
/*  31:    */     }
/*  32:    */   };
/*  33:    */   private final Channel channel;
/*  34:    */   private Entry flushedEntry;
/*  35:    */   private Entry unflushedEntry;
/*  36:    */   private Entry tailEntry;
/*  37:    */   private int flushed;
/*  38:    */   private int nioBufferCount;
/*  39:    */   private long nioBufferSize;
/*  40:    */   private boolean inFail;
/*  41: 91 */   private static final AtomicLongFieldUpdater<ChannelOutboundBuffer> TOTAL_PENDING_SIZE_UPDATER = AtomicLongFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
/*  42:    */   private volatile long totalPendingSize;
/*  43: 97 */   private static final AtomicIntegerFieldUpdater<ChannelOutboundBuffer> UNWRITABLE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "unwritable");
/*  44:    */   private volatile int unwritable;
/*  45:    */   private volatile Runnable fireChannelWritabilityChangedTask;
/*  46:    */   
/*  47:    */   ChannelOutboundBuffer(AbstractChannel channel)
/*  48:    */   {
/*  49:105 */     this.channel = channel;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void addMessage(Object msg, int size, ChannelPromise promise)
/*  53:    */   {
/*  54:113 */     Entry entry = Entry.newInstance(msg, size, total(msg), promise);
/*  55:114 */     if (this.tailEntry == null)
/*  56:    */     {
/*  57:115 */       this.flushedEntry = null;
/*  58:116 */       this.tailEntry = entry;
/*  59:    */     }
/*  60:    */     else
/*  61:    */     {
/*  62:118 */       Entry tail = this.tailEntry;
/*  63:119 */       tail.next = entry;
/*  64:120 */       this.tailEntry = entry;
/*  65:    */     }
/*  66:122 */     if (this.unflushedEntry == null) {
/*  67:123 */       this.unflushedEntry = entry;
/*  68:    */     }
/*  69:128 */     incrementPendingOutboundBytes(entry.pendingSize, false);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void addFlush()
/*  73:    */   {
/*  74:140 */     Entry entry = this.unflushedEntry;
/*  75:141 */     if (entry != null)
/*  76:    */     {
/*  77:142 */       if (this.flushedEntry == null) {
/*  78:144 */         this.flushedEntry = entry;
/*  79:    */       }
/*  80:    */       do
/*  81:    */       {
/*  82:147 */         this.flushed += 1;
/*  83:148 */         if (!entry.promise.setUncancellable())
/*  84:    */         {
/*  85:150 */           int pending = entry.cancel();
/*  86:151 */           decrementPendingOutboundBytes(pending, false, true);
/*  87:    */         }
/*  88:153 */         entry = entry.next;
/*  89:154 */       } while (entry != null);
/*  90:157 */       this.unflushedEntry = null;
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   void incrementPendingOutboundBytes(long size)
/*  95:    */   {
/*  96:166 */     incrementPendingOutboundBytes(size, true);
/*  97:    */   }
/*  98:    */   
/*  99:    */   private void incrementPendingOutboundBytes(long size, boolean invokeLater)
/* 100:    */   {
/* 101:170 */     if (size == 0L) {
/* 102:171 */       return;
/* 103:    */     }
/* 104:174 */     long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
/* 105:175 */     if (newWriteBufferSize > this.channel.config().getWriteBufferHighWaterMark()) {
/* 106:176 */       setUnwritable(invokeLater);
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   void decrementPendingOutboundBytes(long size)
/* 111:    */   {
/* 112:185 */     decrementPendingOutboundBytes(size, true, true);
/* 113:    */   }
/* 114:    */   
/* 115:    */   private void decrementPendingOutboundBytes(long size, boolean invokeLater, boolean notifyWritability)
/* 116:    */   {
/* 117:189 */     if (size == 0L) {
/* 118:190 */       return;
/* 119:    */     }
/* 120:193 */     long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
/* 121:194 */     if ((notifyWritability) && (newWriteBufferSize < this.channel.config().getWriteBufferLowWaterMark())) {
/* 122:195 */       setWritable(invokeLater);
/* 123:    */     }
/* 124:    */   }
/* 125:    */   
/* 126:    */   private static long total(Object msg)
/* 127:    */   {
/* 128:200 */     if ((msg instanceof ByteBuf)) {
/* 129:201 */       return ((ByteBuf)msg).readableBytes();
/* 130:    */     }
/* 131:203 */     if ((msg instanceof FileRegion)) {
/* 132:204 */       return ((FileRegion)msg).count();
/* 133:    */     }
/* 134:206 */     if ((msg instanceof ByteBufHolder)) {
/* 135:207 */       return ((ByteBufHolder)msg).content().readableBytes();
/* 136:    */     }
/* 137:209 */     return -1L;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public Object current()
/* 141:    */   {
/* 142:216 */     Entry entry = this.flushedEntry;
/* 143:217 */     if (entry == null) {
/* 144:218 */       return null;
/* 145:    */     }
/* 146:221 */     return entry.msg;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public void progress(long amount)
/* 150:    */   {
/* 151:228 */     Entry e = this.flushedEntry;
/* 152:229 */     assert (e != null);
/* 153:230 */     ChannelPromise p = e.promise;
/* 154:231 */     if ((p instanceof ChannelProgressivePromise))
/* 155:    */     {
/* 156:232 */       long progress = e.progress + amount;
/* 157:233 */       e.progress = progress;
/* 158:234 */       ((ChannelProgressivePromise)p).tryProgress(progress, e.total);
/* 159:    */     }
/* 160:    */   }
/* 161:    */   
/* 162:    */   public boolean remove()
/* 163:    */   {
/* 164:244 */     Entry e = this.flushedEntry;
/* 165:245 */     if (e == null)
/* 166:    */     {
/* 167:246 */       clearNioBuffers();
/* 168:247 */       return false;
/* 169:    */     }
/* 170:249 */     Object msg = e.msg;
/* 171:    */     
/* 172:251 */     ChannelPromise promise = e.promise;
/* 173:252 */     int size = e.pendingSize;
/* 174:    */     
/* 175:254 */     removeEntry(e);
/* 176:256 */     if (!e.cancelled)
/* 177:    */     {
/* 178:258 */       ReferenceCountUtil.safeRelease(msg);
/* 179:259 */       safeSuccess(promise);
/* 180:260 */       decrementPendingOutboundBytes(size, false, true);
/* 181:    */     }
/* 182:264 */     e.recycle();
/* 183:    */     
/* 184:266 */     return true;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public boolean remove(Throwable cause)
/* 188:    */   {
/* 189:275 */     return remove0(cause, true);
/* 190:    */   }
/* 191:    */   
/* 192:    */   private boolean remove0(Throwable cause, boolean notifyWritability)
/* 193:    */   {
/* 194:279 */     Entry e = this.flushedEntry;
/* 195:280 */     if (e == null)
/* 196:    */     {
/* 197:281 */       clearNioBuffers();
/* 198:282 */       return false;
/* 199:    */     }
/* 200:284 */     Object msg = e.msg;
/* 201:    */     
/* 202:286 */     ChannelPromise promise = e.promise;
/* 203:287 */     int size = e.pendingSize;
/* 204:    */     
/* 205:289 */     removeEntry(e);
/* 206:291 */     if (!e.cancelled)
/* 207:    */     {
/* 208:293 */       ReferenceCountUtil.safeRelease(msg);
/* 209:    */       
/* 210:295 */       safeFail(promise, cause);
/* 211:296 */       decrementPendingOutboundBytes(size, false, notifyWritability);
/* 212:    */     }
/* 213:300 */     e.recycle();
/* 214:    */     
/* 215:302 */     return true;
/* 216:    */   }
/* 217:    */   
/* 218:    */   private void removeEntry(Entry e)
/* 219:    */   {
/* 220:306 */     if (--this.flushed == 0)
/* 221:    */     {
/* 222:308 */       this.flushedEntry = null;
/* 223:309 */       if (e == this.tailEntry)
/* 224:    */       {
/* 225:310 */         this.tailEntry = null;
/* 226:311 */         this.unflushedEntry = null;
/* 227:    */       }
/* 228:    */     }
/* 229:    */     else
/* 230:    */     {
/* 231:314 */       this.flushedEntry = e.next;
/* 232:    */     }
/* 233:    */   }
/* 234:    */   
/* 235:    */   public void removeBytes(long writtenBytes)
/* 236:    */   {
/* 237:    */     for (;;)
/* 238:    */     {
/* 239:324 */       Object msg = current();
/* 240:325 */       if (!(msg instanceof ByteBuf))
/* 241:    */       {
/* 242:326 */         if (($assertionsDisabled) || (writtenBytes == 0L)) {
/* 243:    */           break;
/* 244:    */         }
/* 245:326 */         throw new AssertionError();
/* 246:    */       }
/* 247:330 */       ByteBuf buf = (ByteBuf)msg;
/* 248:331 */       int readerIndex = buf.readerIndex();
/* 249:332 */       int readableBytes = buf.writerIndex() - readerIndex;
/* 250:334 */       if (readableBytes <= writtenBytes)
/* 251:    */       {
/* 252:335 */         if (writtenBytes != 0L)
/* 253:    */         {
/* 254:336 */           progress(readableBytes);
/* 255:337 */           writtenBytes -= readableBytes;
/* 256:    */         }
/* 257:339 */         remove();
/* 258:    */       }
/* 259:    */       else
/* 260:    */       {
/* 261:341 */         if (writtenBytes == 0L) {
/* 262:    */           break;
/* 263:    */         }
/* 264:342 */         buf.readerIndex(readerIndex + (int)writtenBytes);
/* 265:343 */         progress(writtenBytes); break;
/* 266:    */       }
/* 267:    */     }
/* 268:348 */     clearNioBuffers();
/* 269:    */   }
/* 270:    */   
/* 271:    */   private void clearNioBuffers()
/* 272:    */   {
/* 273:354 */     int count = this.nioBufferCount;
/* 274:355 */     if (count > 0)
/* 275:    */     {
/* 276:356 */       this.nioBufferCount = 0;
/* 277:357 */       Arrays.fill((Object[])NIO_BUFFERS.get(), 0, count, null);
/* 278:    */     }
/* 279:    */   }
/* 280:    */   
/* 281:    */   public ByteBuffer[] nioBuffers()
/* 282:    */   {
/* 283:372 */     return nioBuffers(2147483647, 2147483647L);
/* 284:    */   }
/* 285:    */   
/* 286:    */   public ByteBuffer[] nioBuffers(int maxCount, long maxBytes)
/* 287:    */   {
/* 288:390 */     assert (maxCount > 0);
/* 289:391 */     assert (maxBytes > 0L);
/* 290:392 */     long nioBufferSize = 0L;
/* 291:393 */     int nioBufferCount = 0;
/* 292:394 */     InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
/* 293:395 */     ByteBuffer[] nioBuffers = (ByteBuffer[])NIO_BUFFERS.get(threadLocalMap);
/* 294:396 */     Entry entry = this.flushedEntry;
/* 295:397 */     while ((isFlushedEntry(entry)) && ((entry.msg instanceof ByteBuf)))
/* 296:    */     {
/* 297:398 */       if (!entry.cancelled)
/* 298:    */       {
/* 299:399 */         ByteBuf buf = (ByteBuf)entry.msg;
/* 300:400 */         int readerIndex = buf.readerIndex();
/* 301:401 */         int readableBytes = buf.writerIndex() - readerIndex;
/* 302:403 */         if (readableBytes > 0)
/* 303:    */         {
/* 304:404 */           if ((maxBytes - readableBytes < nioBufferSize) && (nioBufferCount != 0)) {
/* 305:    */             break;
/* 306:    */           }
/* 307:418 */           nioBufferSize += readableBytes;
/* 308:419 */           int count = entry.count;
/* 309:420 */           if (count == -1) {
/* 310:422 */             entry.count = (count = buf.nioBufferCount());
/* 311:    */           }
/* 312:424 */           int neededSpace = Math.min(maxCount, nioBufferCount + count);
/* 313:425 */           if (neededSpace > nioBuffers.length)
/* 314:    */           {
/* 315:426 */             nioBuffers = expandNioBufferArray(nioBuffers, neededSpace, nioBufferCount);
/* 316:427 */             NIO_BUFFERS.set(threadLocalMap, nioBuffers);
/* 317:    */           }
/* 318:429 */           if (count == 1)
/* 319:    */           {
/* 320:430 */             ByteBuffer nioBuf = entry.buf;
/* 321:431 */             if (nioBuf == null) {
/* 322:434 */               entry.buf = (nioBuf = buf.internalNioBuffer(readerIndex, readableBytes));
/* 323:    */             }
/* 324:436 */             nioBuffers[(nioBufferCount++)] = nioBuf;
/* 325:    */           }
/* 326:    */           else
/* 327:    */           {
/* 328:438 */             ByteBuffer[] nioBufs = entry.bufs;
/* 329:439 */             if (nioBufs == null) {
/* 330:442 */               entry.bufs = (nioBufs = buf.nioBuffers());
/* 331:    */             }
/* 332:444 */             for (int i = 0; (i < nioBufs.length) && (nioBufferCount < maxCount); i++)
/* 333:    */             {
/* 334:445 */               ByteBuffer nioBuf = nioBufs[i];
/* 335:446 */               if (nioBuf == null) {
/* 336:    */                 break;
/* 337:    */               }
/* 338:448 */               if (nioBuf.hasRemaining()) {
/* 339:451 */                 nioBuffers[(nioBufferCount++)] = nioBuf;
/* 340:    */               }
/* 341:    */             }
/* 342:    */           }
/* 343:454 */           if (nioBufferCount == maxCount) {
/* 344:    */             break;
/* 345:    */           }
/* 346:    */         }
/* 347:    */       }
/* 348:459 */       entry = entry.next;
/* 349:    */     }
/* 350:461 */     this.nioBufferCount = nioBufferCount;
/* 351:462 */     this.nioBufferSize = nioBufferSize;
/* 352:    */     
/* 353:464 */     return nioBuffers;
/* 354:    */   }
/* 355:    */   
/* 356:    */   private static ByteBuffer[] expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size)
/* 357:    */   {
/* 358:468 */     int newCapacity = array.length;
/* 359:    */     do
/* 360:    */     {
/* 361:472 */       newCapacity <<= 1;
/* 362:474 */       if (newCapacity < 0) {
/* 363:475 */         throw new IllegalStateException();
/* 364:    */       }
/* 365:478 */     } while (neededSpace > newCapacity);
/* 366:480 */     ByteBuffer[] newArray = new ByteBuffer[newCapacity];
/* 367:481 */     System.arraycopy(array, 0, newArray, 0, size);
/* 368:    */     
/* 369:483 */     return newArray;
/* 370:    */   }
/* 371:    */   
/* 372:    */   public int nioBufferCount()
/* 373:    */   {
/* 374:492 */     return this.nioBufferCount;
/* 375:    */   }
/* 376:    */   
/* 377:    */   public long nioBufferSize()
/* 378:    */   {
/* 379:501 */     return this.nioBufferSize;
/* 380:    */   }
/* 381:    */   
/* 382:    */   public boolean isWritable()
/* 383:    */   {
/* 384:511 */     return this.unwritable == 0;
/* 385:    */   }
/* 386:    */   
/* 387:    */   public boolean getUserDefinedWritability(int index)
/* 388:    */   {
/* 389:519 */     return (this.unwritable & writabilityMask(index)) == 0;
/* 390:    */   }
/* 391:    */   
/* 392:    */   public void setUserDefinedWritability(int index, boolean writable)
/* 393:    */   {
/* 394:526 */     if (writable) {
/* 395:527 */       setUserDefinedWritability(index);
/* 396:    */     } else {
/* 397:529 */       clearUserDefinedWritability(index);
/* 398:    */     }
/* 399:    */   }
/* 400:    */   
/* 401:    */   private void setUserDefinedWritability(int index)
/* 402:    */   {
/* 403:534 */     int mask = writabilityMask(index) ^ 0xFFFFFFFF;
/* 404:    */     for (;;)
/* 405:    */     {
/* 406:536 */       int oldValue = this.unwritable;
/* 407:537 */       int newValue = oldValue & mask;
/* 408:538 */       if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue))
/* 409:    */       {
/* 410:539 */         if ((oldValue == 0) || (newValue != 0)) {
/* 411:    */           break;
/* 412:    */         }
/* 413:540 */         fireChannelWritabilityChanged(true); break;
/* 414:    */       }
/* 415:    */     }
/* 416:    */   }
/* 417:    */   
/* 418:    */   private void clearUserDefinedWritability(int index)
/* 419:    */   {
/* 420:548 */     int mask = writabilityMask(index);
/* 421:    */     for (;;)
/* 422:    */     {
/* 423:550 */       int oldValue = this.unwritable;
/* 424:551 */       int newValue = oldValue | mask;
/* 425:552 */       if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue))
/* 426:    */       {
/* 427:553 */         if ((oldValue != 0) || (newValue == 0)) {
/* 428:    */           break;
/* 429:    */         }
/* 430:554 */         fireChannelWritabilityChanged(true); break;
/* 431:    */       }
/* 432:    */     }
/* 433:    */   }
/* 434:    */   
/* 435:    */   private static int writabilityMask(int index)
/* 436:    */   {
/* 437:562 */     if ((index < 1) || (index > 31)) {
/* 438:563 */       throw new IllegalArgumentException("index: " + index + " (expected: 1~31)");
/* 439:    */     }
/* 440:565 */     return 1 << index;
/* 441:    */   }
/* 442:    */   
/* 443:    */   private void setWritable(boolean invokeLater)
/* 444:    */   {
/* 445:    */     for (;;)
/* 446:    */     {
/* 447:570 */       int oldValue = this.unwritable;
/* 448:571 */       int newValue = oldValue & 0xFFFFFFFE;
/* 449:572 */       if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue))
/* 450:    */       {
/* 451:573 */         if ((oldValue == 0) || (newValue != 0)) {
/* 452:    */           break;
/* 453:    */         }
/* 454:574 */         fireChannelWritabilityChanged(invokeLater); break;
/* 455:    */       }
/* 456:    */     }
/* 457:    */   }
/* 458:    */   
/* 459:    */   private void setUnwritable(boolean invokeLater)
/* 460:    */   {
/* 461:    */     for (;;)
/* 462:    */     {
/* 463:583 */       int oldValue = this.unwritable;
/* 464:584 */       int newValue = oldValue | 0x1;
/* 465:585 */       if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue))
/* 466:    */       {
/* 467:586 */         if ((oldValue != 0) || (newValue == 0)) {
/* 468:    */           break;
/* 469:    */         }
/* 470:587 */         fireChannelWritabilityChanged(invokeLater); break;
/* 471:    */       }
/* 472:    */     }
/* 473:    */   }
/* 474:    */   
/* 475:    */   private void fireChannelWritabilityChanged(boolean invokeLater)
/* 476:    */   {
/* 477:595 */     final ChannelPipeline pipeline = this.channel.pipeline();
/* 478:596 */     if (invokeLater)
/* 479:    */     {
/* 480:597 */       Runnable task = this.fireChannelWritabilityChangedTask;
/* 481:598 */       if (task == null) {
/* 482:599 */         this.fireChannelWritabilityChangedTask = (task = new Runnable()
/* 483:    */         {
/* 484:    */           public void run()
/* 485:    */           {
/* 486:602 */             pipeline.fireChannelWritabilityChanged();
/* 487:    */           }
/* 488:    */         });
/* 489:    */       }
/* 490:606 */       this.channel.eventLoop().execute(task);
/* 491:    */     }
/* 492:    */     else
/* 493:    */     {
/* 494:608 */       pipeline.fireChannelWritabilityChanged();
/* 495:    */     }
/* 496:    */   }
/* 497:    */   
/* 498:    */   public int size()
/* 499:    */   {
/* 500:616 */     return this.flushed;
/* 501:    */   }
/* 502:    */   
/* 503:    */   public boolean isEmpty()
/* 504:    */   {
/* 505:624 */     return this.flushed == 0;
/* 506:    */   }
/* 507:    */   
/* 508:    */   void failFlushed(Throwable cause, boolean notify)
/* 509:    */   {
/* 510:633 */     if (this.inFail) {
/* 511:634 */       return;
/* 512:    */     }
/* 513:    */     try
/* 514:    */     {
/* 515:638 */       this.inFail = true;
/* 516:    */       for (;;)
/* 517:    */       {
/* 518:640 */         if (!remove0(cause, notify)) {
/* 519:    */           break;
/* 520:    */         }
/* 521:    */       }
/* 522:645 */       this.inFail = false;
/* 523:    */     }
/* 524:    */     finally
/* 525:    */     {
/* 526:645 */       this.inFail = false;
/* 527:    */     }
/* 528:    */   }
/* 529:    */   
/* 530:    */   void close(final Throwable cause, final boolean allowChannelOpen)
/* 531:    */   {
/* 532:650 */     if (this.inFail)
/* 533:    */     {
/* 534:651 */       this.channel.eventLoop().execute(new Runnable()
/* 535:    */       {
/* 536:    */         public void run()
/* 537:    */         {
/* 538:654 */           ChannelOutboundBuffer.this.close(cause, allowChannelOpen);
/* 539:    */         }
/* 540:656 */       });
/* 541:657 */       return;
/* 542:    */     }
/* 543:660 */     this.inFail = true;
/* 544:662 */     if ((!allowChannelOpen) && (this.channel.isOpen())) {
/* 545:663 */       throw new IllegalStateException("close() must be invoked after the channel is closed.");
/* 546:    */     }
/* 547:666 */     if (!isEmpty()) {
/* 548:667 */       throw new IllegalStateException("close() must be invoked after all flushed writes are handled.");
/* 549:    */     }
/* 550:    */     try
/* 551:    */     {
/* 552:672 */       Entry e = this.unflushedEntry;
/* 553:673 */       while (e != null)
/* 554:    */       {
/* 555:675 */         int size = e.pendingSize;
/* 556:676 */         TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
/* 557:678 */         if (!e.cancelled)
/* 558:    */         {
/* 559:679 */           ReferenceCountUtil.safeRelease(e.msg);
/* 560:680 */           safeFail(e.promise, cause);
/* 561:    */         }
/* 562:682 */         e = e.recycleAndGetNext();
/* 563:    */       }
/* 564:    */     }
/* 565:    */     finally
/* 566:    */     {
/* 567:685 */       this.inFail = false;
/* 568:    */     }
/* 569:687 */     clearNioBuffers();
/* 570:    */   }
/* 571:    */   
/* 572:    */   void close(ClosedChannelException cause)
/* 573:    */   {
/* 574:691 */     close(cause, false);
/* 575:    */   }
/* 576:    */   
/* 577:    */   private static void safeSuccess(ChannelPromise promise)
/* 578:    */   {
/* 579:697 */     PromiseNotificationUtil.trySuccess(promise, null, (promise instanceof VoidChannelPromise) ? null : logger);
/* 580:    */   }
/* 581:    */   
/* 582:    */   private static void safeFail(ChannelPromise promise, Throwable cause)
/* 583:    */   {
/* 584:703 */     PromiseNotificationUtil.tryFailure(promise, cause, (promise instanceof VoidChannelPromise) ? null : logger);
/* 585:    */   }
/* 586:    */   
/* 587:    */   @Deprecated
/* 588:    */   public void recycle() {}
/* 589:    */   
/* 590:    */   public long totalPendingWriteBytes()
/* 591:    */   {
/* 592:712 */     return this.totalPendingSize;
/* 593:    */   }
/* 594:    */   
/* 595:    */   public long bytesBeforeUnwritable()
/* 596:    */   {
/* 597:720 */     long bytes = this.channel.config().getWriteBufferHighWaterMark() - this.totalPendingSize;
/* 598:724 */     if (bytes > 0L) {
/* 599:725 */       return isWritable() ? bytes : 0L;
/* 600:    */     }
/* 601:727 */     return 0L;
/* 602:    */   }
/* 603:    */   
/* 604:    */   public long bytesBeforeWritable()
/* 605:    */   {
/* 606:735 */     long bytes = this.totalPendingSize - this.channel.config().getWriteBufferLowWaterMark();
/* 607:739 */     if (bytes > 0L) {
/* 608:740 */       return isWritable() ? 0L : bytes;
/* 609:    */     }
/* 610:742 */     return 0L;
/* 611:    */   }
/* 612:    */   
/* 613:    */   public void forEachFlushedMessage(MessageProcessor processor)
/* 614:    */     throws Exception
/* 615:    */   {
/* 616:751 */     if (processor == null) {
/* 617:752 */       throw new NullPointerException("processor");
/* 618:    */     }
/* 619:755 */     Entry entry = this.flushedEntry;
/* 620:756 */     if (entry == null) {
/* 621:    */       return;
/* 622:    */     }
/* 623:    */     do
/* 624:    */     {
/* 625:761 */       if ((!entry.cancelled) && 
/* 626:762 */         (!processor.processMessage(entry.msg))) {
/* 627:763 */         return;
/* 628:    */       }
/* 629:766 */       entry = entry.next;
/* 630:767 */     } while (isFlushedEntry(entry));
/* 631:    */   }
/* 632:    */   
/* 633:    */   private boolean isFlushedEntry(Entry e)
/* 634:    */   {
/* 635:771 */     return (e != null) && (e != this.unflushedEntry);
/* 636:    */   }
/* 637:    */   
/* 638:    */   static final class Entry
/* 639:    */   {
/* 640:783 */     private static final Recycler<Entry> RECYCLER = new Recycler()
/* 641:    */     {
/* 642:    */       protected ChannelOutboundBuffer.Entry newObject(Recycler.Handle<ChannelOutboundBuffer.Entry> handle)
/* 643:    */       {
/* 644:786 */         return new ChannelOutboundBuffer.Entry(handle, null);
/* 645:    */       }
/* 646:    */     };
/* 647:    */     private final Recycler.Handle<Entry> handle;
/* 648:    */     Entry next;
/* 649:    */     Object msg;
/* 650:    */     ByteBuffer[] bufs;
/* 651:    */     ByteBuffer buf;
/* 652:    */     ChannelPromise promise;
/* 653:    */     long progress;
/* 654:    */     long total;
/* 655:    */     int pendingSize;
/* 656:799 */     int count = -1;
/* 657:    */     boolean cancelled;
/* 658:    */     
/* 659:    */     private Entry(Recycler.Handle<Entry> handle)
/* 660:    */     {
/* 661:803 */       this.handle = handle;
/* 662:    */     }
/* 663:    */     
/* 664:    */     static Entry newInstance(Object msg, int size, long total, ChannelPromise promise)
/* 665:    */     {
/* 666:807 */       Entry entry = (Entry)RECYCLER.get();
/* 667:808 */       entry.msg = msg;
/* 668:809 */       entry.pendingSize = (size + ChannelOutboundBuffer.CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD);
/* 669:810 */       entry.total = total;
/* 670:811 */       entry.promise = promise;
/* 671:812 */       return entry;
/* 672:    */     }
/* 673:    */     
/* 674:    */     int cancel()
/* 675:    */     {
/* 676:816 */       if (!this.cancelled)
/* 677:    */       {
/* 678:817 */         this.cancelled = true;
/* 679:818 */         int pSize = this.pendingSize;
/* 680:    */         
/* 681:    */ 
/* 682:821 */         ReferenceCountUtil.safeRelease(this.msg);
/* 683:822 */         this.msg = Unpooled.EMPTY_BUFFER;
/* 684:    */         
/* 685:824 */         this.pendingSize = 0;
/* 686:825 */         this.total = 0L;
/* 687:826 */         this.progress = 0L;
/* 688:827 */         this.bufs = null;
/* 689:828 */         this.buf = null;
/* 690:829 */         return pSize;
/* 691:    */       }
/* 692:831 */       return 0;
/* 693:    */     }
/* 694:    */     
/* 695:    */     void recycle()
/* 696:    */     {
/* 697:835 */       this.next = null;
/* 698:836 */       this.bufs = null;
/* 699:837 */       this.buf = null;
/* 700:838 */       this.msg = null;
/* 701:839 */       this.promise = null;
/* 702:840 */       this.progress = 0L;
/* 703:841 */       this.total = 0L;
/* 704:842 */       this.pendingSize = 0;
/* 705:843 */       this.count = -1;
/* 706:844 */       this.cancelled = false;
/* 707:845 */       this.handle.recycle(this);
/* 708:    */     }
/* 709:    */     
/* 710:    */     Entry recycleAndGetNext()
/* 711:    */     {
/* 712:849 */       Entry next = this.next;
/* 713:850 */       recycle();
/* 714:851 */       return next;
/* 715:    */     }
/* 716:    */   }
/* 717:    */   
/* 718:    */   public static abstract interface MessageProcessor
/* 719:    */   {
/* 720:    */     public abstract boolean processMessage(Object paramObject)
/* 721:    */       throws Exception;
/* 722:    */   }
/* 723:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.ChannelOutboundBuffer
 * JD-Core Version:    0.7.0.1
 */