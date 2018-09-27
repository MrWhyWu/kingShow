/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*   5:    */ import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
/*   6:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
/*   7:    */ import java.util.Iterator;
/*   8:    */ 
/*   9:    */ public abstract class BaseMpscLinkedArrayQueue<E>
/*  10:    */   extends BaseMpscLinkedArrayQueueColdProducerFields<E>
/*  11:    */   implements MessagePassingQueue<E>, QueueProgressIndicators
/*  12:    */ {
/*  13:171 */   private static final Object JUMP = new Object();
/*  14:    */   private static final int CONTINUE_TO_P_INDEX_CAS = 0;
/*  15:    */   private static final int RETRY = 1;
/*  16:    */   private static final int QUEUE_FULL = 2;
/*  17:    */   private static final int QUEUE_RESIZE = 3;
/*  18:    */   
/*  19:    */   public BaseMpscLinkedArrayQueue(int initialCapacity)
/*  20:    */   {
/*  21:184 */     RangeUtil.checkGreaterThanOrEqual(initialCapacity, 2, "initialCapacity");
/*  22:    */     
/*  23:186 */     int p2capacity = Pow2.roundToPowerOfTwo(initialCapacity);
/*  24:    */     
/*  25:188 */     long mask = p2capacity - 1 << 1;
/*  26:    */     
/*  27:190 */     E[] buffer = CircularArrayOffsetCalculator.allocate(p2capacity + 1);
/*  28:191 */     this.producerBuffer = buffer;
/*  29:192 */     this.producerMask = mask;
/*  30:193 */     this.consumerBuffer = buffer;
/*  31:194 */     this.consumerMask = mask;
/*  32:195 */     soProducerLimit(mask);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public final Iterator<E> iterator()
/*  36:    */   {
/*  37:201 */     throw new UnsupportedOperationException();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public final int size()
/*  41:    */   {
/*  42:215 */     long after = lvConsumerIndex();
/*  43:    */     for (;;)
/*  44:    */     {
/*  45:219 */       long before = after;
/*  46:220 */       long currentProducerIndex = lvProducerIndex();
/*  47:221 */       after = lvConsumerIndex();
/*  48:222 */       if (before == after)
/*  49:    */       {
/*  50:224 */         long size = currentProducerIndex - after >> 1;
/*  51:225 */         break;
/*  52:    */       }
/*  53:    */     }
/*  54:    */     long size;
/*  55:230 */     if (size > 2147483647L) {
/*  56:232 */       return 2147483647;
/*  57:    */     }
/*  58:236 */     return (int)size;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public final boolean isEmpty()
/*  62:    */   {
/*  63:247 */     return lvConsumerIndex() == lvProducerIndex();
/*  64:    */   }
/*  65:    */   
/*  66:    */   public String toString()
/*  67:    */   {
/*  68:253 */     return getClass().getName();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public boolean offer(E e)
/*  72:    */   {
/*  73:259 */     if (null == e) {
/*  74:261 */       throw new NullPointerException();
/*  75:    */     }
/*  76:    */     long pIndex;
/*  77:    */     long mask;
/*  78:    */     E[] buffer;
/*  79:    */     for (;;)
/*  80:    */     {
/*  81:270 */       long producerLimit = lvProducerLimit();
/*  82:271 */       pIndex = lvProducerIndex();
/*  83:273 */       if ((pIndex & 1L) != 1L)
/*  84:    */       {
/*  85:280 */         mask = this.producerMask;
/*  86:281 */         buffer = this.producerBuffer;
/*  87:285 */         if (producerLimit <= pIndex)
/*  88:    */         {
/*  89:287 */           int result = offerSlowPath(mask, pIndex, producerLimit);
/*  90:288 */           switch (result)
/*  91:    */           {
/*  92:    */           case 0: 
/*  93:    */             break;
/*  94:    */           case 1: 
/*  95:    */             break;
/*  96:    */           case 2: 
/*  97:295 */             return false;
/*  98:    */           case 3: 
/*  99:297 */             resize(mask, buffer, pIndex, e);
/* 100:298 */             return true;
/* 101:    */           }
/* 102:    */         }
/* 103:    */         else
/* 104:    */         {
/* 105:302 */           if (casProducerIndex(pIndex, pIndex + 2L)) {
/* 106:    */             break;
/* 107:    */           }
/* 108:    */         }
/* 109:    */       }
/* 110:    */     }
/* 111:308 */     long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex, mask);
/* 112:309 */     UnsafeRefArrayAccess.soElement(buffer, offset, e);
/* 113:310 */     return true;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public E poll()
/* 117:    */   {
/* 118:322 */     E[] buffer = this.consumerBuffer;
/* 119:323 */     long index = this.consumerIndex;
/* 120:324 */     long mask = this.consumerMask;
/* 121:    */     
/* 122:326 */     long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 123:327 */     Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 124:328 */     if (e == null) {
/* 125:330 */       if (index != lvProducerIndex()) {
/* 126:    */         do
/* 127:    */         {
/* 128:337 */           e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 129:339 */         } while (e == null);
/* 130:    */       } else {
/* 131:343 */         return null;
/* 132:    */       }
/* 133:    */     }
/* 134:347 */     if (e == JUMP)
/* 135:    */     {
/* 136:349 */       E[] nextBuffer = getNextBuffer(buffer, mask);
/* 137:350 */       return newBufferPoll(nextBuffer, index);
/* 138:    */     }
/* 139:353 */     UnsafeRefArrayAccess.soElement(buffer, offset, null);
/* 140:354 */     soConsumerIndex(index + 2L);
/* 141:355 */     return e;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public E peek()
/* 145:    */   {
/* 146:367 */     E[] buffer = this.consumerBuffer;
/* 147:368 */     long index = this.consumerIndex;
/* 148:369 */     long mask = this.consumerMask;
/* 149:    */     
/* 150:371 */     long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 151:372 */     Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 152:373 */     if ((e == null) && (index != lvProducerIndex())) {
/* 153:    */       do
/* 154:    */       {
/* 155:379 */         e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 156:381 */       } while (e == null);
/* 157:    */     }
/* 158:383 */     if (e == JUMP) {
/* 159:385 */       return newBufferPeek(getNextBuffer(buffer, mask), index);
/* 160:    */     }
/* 161:387 */     return e;
/* 162:    */   }
/* 163:    */   
/* 164:    */   private int offerSlowPath(long mask, long pIndex, long producerLimit)
/* 165:    */   {
/* 166:395 */     long cIndex = lvConsumerIndex();
/* 167:396 */     long bufferCapacity = getCurrentBufferCapacity(mask);
/* 168:398 */     if (cIndex + bufferCapacity > pIndex)
/* 169:    */     {
/* 170:400 */       if (!casProducerLimit(producerLimit, cIndex + bufferCapacity)) {
/* 171:403 */         return 1;
/* 172:    */       }
/* 173:408 */       return 0;
/* 174:    */     }
/* 175:412 */     if (availableInQueue(pIndex, cIndex) <= 0L) {
/* 176:415 */       return 2;
/* 177:    */     }
/* 178:418 */     if (casProducerIndex(pIndex, pIndex + 1L)) {
/* 179:421 */       return 3;
/* 180:    */     }
/* 181:426 */     return 1;
/* 182:    */   }
/* 183:    */   
/* 184:    */   protected abstract long availableInQueue(long paramLong1, long paramLong2);
/* 185:    */   
/* 186:    */   private E[] getNextBuffer(E[] buffer, long mask)
/* 187:    */   {
/* 188:438 */     long offset = nextArrayOffset(mask);
/* 189:439 */     E[] nextBuffer = (Object[])UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 190:440 */     UnsafeRefArrayAccess.soElement(buffer, offset, null);
/* 191:441 */     return nextBuffer;
/* 192:    */   }
/* 193:    */   
/* 194:    */   private long nextArrayOffset(long mask)
/* 195:    */   {
/* 196:446 */     return LinkedArrayQueueUtil.modifiedCalcElementOffset(mask + 2L, 9223372036854775807L);
/* 197:    */   }
/* 198:    */   
/* 199:    */   private E newBufferPoll(E[] nextBuffer, long index)
/* 200:    */   {
/* 201:451 */     long offset = newBufferAndOffset(nextBuffer, index);
/* 202:452 */     E n = UnsafeRefArrayAccess.lvElement(nextBuffer, offset);
/* 203:453 */     if (n == null) {
/* 204:455 */       throw new IllegalStateException("new buffer must have at least one element");
/* 205:    */     }
/* 206:457 */     UnsafeRefArrayAccess.soElement(nextBuffer, offset, null);
/* 207:458 */     soConsumerIndex(index + 2L);
/* 208:459 */     return n;
/* 209:    */   }
/* 210:    */   
/* 211:    */   private E newBufferPeek(E[] nextBuffer, long index)
/* 212:    */   {
/* 213:464 */     long offset = newBufferAndOffset(nextBuffer, index);
/* 214:465 */     E n = UnsafeRefArrayAccess.lvElement(nextBuffer, offset);
/* 215:466 */     if (null == n) {
/* 216:468 */       throw new IllegalStateException("new buffer must have at least one element");
/* 217:    */     }
/* 218:470 */     return n;
/* 219:    */   }
/* 220:    */   
/* 221:    */   private long newBufferAndOffset(E[] nextBuffer, long index)
/* 222:    */   {
/* 223:475 */     this.consumerBuffer = nextBuffer;
/* 224:476 */     this.consumerMask = (LinkedArrayQueueUtil.length(nextBuffer) - 2 << 1);
/* 225:477 */     return LinkedArrayQueueUtil.modifiedCalcElementOffset(index, this.consumerMask);
/* 226:    */   }
/* 227:    */   
/* 228:    */   public long currentProducerIndex()
/* 229:    */   {
/* 230:483 */     return lvProducerIndex() / 2L;
/* 231:    */   }
/* 232:    */   
/* 233:    */   public long currentConsumerIndex()
/* 234:    */   {
/* 235:489 */     return lvConsumerIndex() / 2L;
/* 236:    */   }
/* 237:    */   
/* 238:    */   public abstract int capacity();
/* 239:    */   
/* 240:    */   public boolean relaxedOffer(E e)
/* 241:    */   {
/* 242:498 */     return offer(e);
/* 243:    */   }
/* 244:    */   
/* 245:    */   public E relaxedPoll()
/* 246:    */   {
/* 247:505 */     E[] buffer = this.consumerBuffer;
/* 248:506 */     long index = this.consumerIndex;
/* 249:507 */     long mask = this.consumerMask;
/* 250:    */     
/* 251:509 */     long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 252:510 */     Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 253:511 */     if (e == null) {
/* 254:513 */       return null;
/* 255:    */     }
/* 256:515 */     if (e == JUMP)
/* 257:    */     {
/* 258:517 */       E[] nextBuffer = getNextBuffer(buffer, mask);
/* 259:518 */       return newBufferPoll(nextBuffer, index);
/* 260:    */     }
/* 261:520 */     UnsafeRefArrayAccess.soElement(buffer, offset, null);
/* 262:521 */     soConsumerIndex(index + 2L);
/* 263:522 */     return e;
/* 264:    */   }
/* 265:    */   
/* 266:    */   public E relaxedPeek()
/* 267:    */   {
/* 268:529 */     E[] buffer = this.consumerBuffer;
/* 269:530 */     long index = this.consumerIndex;
/* 270:531 */     long mask = this.consumerMask;
/* 271:    */     
/* 272:533 */     long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 273:534 */     Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 274:535 */     if (e == JUMP) {
/* 275:537 */       return newBufferPeek(getNextBuffer(buffer, mask), index);
/* 276:    */     }
/* 277:539 */     return e;
/* 278:    */   }
/* 279:    */   
/* 280:    */   public int fill(MessagePassingQueue.Supplier<E> s)
/* 281:    */   {
/* 282:545 */     long result = 0L;
/* 283:546 */     int capacity = capacity();
/* 284:    */     do
/* 285:    */     {
/* 286:549 */       int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
/* 287:550 */       if (filled == 0) {
/* 288:552 */         return (int)result;
/* 289:    */       }
/* 290:554 */       result += filled;
/* 291:556 */     } while (result <= capacity);
/* 292:557 */     return (int)result;
/* 293:    */   }
/* 294:    */   
/* 295:    */   public int fill(MessagePassingQueue.Supplier<E> s, int batchSize)
/* 296:    */   {
/* 297:    */     long pIndex;
/* 298:    */     long mask;
/* 299:    */     E[] buffer;
/* 300:    */     for (;;)
/* 301:    */     {
/* 302:569 */       long producerLimit = lvProducerLimit();
/* 303:570 */       pIndex = lvProducerIndex();
/* 304:572 */       if ((pIndex & 1L) != 1L)
/* 305:    */       {
/* 306:581 */         mask = this.producerMask;
/* 307:582 */         buffer = this.producerBuffer;
/* 308:    */         
/* 309:    */ 
/* 310:    */ 
/* 311:586 */         long batchIndex = Math.min(producerLimit, pIndex + 2 * batchSize);
/* 312:588 */         if ((pIndex >= producerLimit) || (producerLimit < batchIndex))
/* 313:    */         {
/* 314:590 */           int result = offerSlowPath(mask, pIndex, producerLimit);
/* 315:591 */           switch (result)
/* 316:    */           {
/* 317:    */           case 0: 
/* 318:    */           case 1: 
/* 319:    */             break;
/* 320:    */           case 2: 
/* 321:598 */             return 0;
/* 322:    */           case 3: 
/* 323:600 */             resize(mask, buffer, pIndex, s.get());
/* 324:601 */             return 1;
/* 325:    */           }
/* 326:    */         }
/* 327:606 */         else if (casProducerIndex(pIndex, batchIndex))
/* 328:    */         {
/* 329:608 */           int claimedSlots = (int)((batchIndex - pIndex) / 2L);
/* 330:609 */           break;
/* 331:    */         }
/* 332:    */       }
/* 333:    */     }
/* 334:    */     int claimedSlots;
/* 335:613 */     for (int i = 0; i < claimedSlots; i++)
/* 336:    */     {
/* 337:615 */       long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex + 2 * i, mask);
/* 338:616 */       UnsafeRefArrayAccess.soElement(buffer, offset, s.get());
/* 339:    */     }
/* 340:618 */     return claimedSlots;
/* 341:    */   }
/* 342:    */   
/* 343:    */   public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 344:    */   {
/* 345:628 */     while (exit.keepRunning()) {
/* 346:630 */       if (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0)
/* 347:    */       {
/* 348:632 */         int idleCounter = 0;
/* 349:633 */         while ((exit.keepRunning()) && (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0)) {
/* 350:635 */           idleCounter = w.idle(idleCounter);
/* 351:    */         }
/* 352:    */       }
/* 353:    */     }
/* 354:    */   }
/* 355:    */   
/* 356:    */   public int drain(MessagePassingQueue.Consumer<E> c)
/* 357:    */   {
/* 358:644 */     return drain(c, capacity());
/* 359:    */   }
/* 360:    */   
/* 361:    */   public int drain(MessagePassingQueue.Consumer<E> c, int limit)
/* 362:    */   {
/* 363:    */     E m;
/* 364:652 */     for (int i = 0; (i < limit) && ((m = relaxedPoll()) != null); i++) {
/* 365:656 */       c.accept(m);
/* 366:    */     }
/* 367:658 */     return i;
/* 368:    */   }
/* 369:    */   
/* 370:    */   public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 371:    */   {
/* 372:664 */     int idleCounter = 0;
/* 373:665 */     while (exit.keepRunning())
/* 374:    */     {
/* 375:667 */       E e = relaxedPoll();
/* 376:668 */       if (e == null)
/* 377:    */       {
/* 378:670 */         idleCounter = w.idle(idleCounter);
/* 379:    */       }
/* 380:    */       else
/* 381:    */       {
/* 382:673 */         idleCounter = 0;
/* 383:674 */         c.accept(e);
/* 384:    */       }
/* 385:    */     }
/* 386:    */   }
/* 387:    */   
/* 388:    */   private void resize(long oldMask, E[] oldBuffer, long pIndex, E e)
/* 389:    */   {
/* 390:680 */     int newBufferLength = getNextBufferSize(oldBuffer);
/* 391:681 */     E[] newBuffer = CircularArrayOffsetCalculator.allocate(newBufferLength);
/* 392:    */     
/* 393:683 */     this.producerBuffer = newBuffer;
/* 394:684 */     int newMask = newBufferLength - 2 << 1;
/* 395:685 */     this.producerMask = newMask;
/* 396:    */     
/* 397:687 */     long offsetInOld = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex, oldMask);
/* 398:688 */     long offsetInNew = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex, newMask);
/* 399:    */     
/* 400:690 */     UnsafeRefArrayAccess.soElement(newBuffer, offsetInNew, e);
/* 401:691 */     UnsafeRefArrayAccess.soElement(oldBuffer, nextArrayOffset(oldMask), newBuffer);
/* 402:    */     
/* 403:    */ 
/* 404:694 */     long cIndex = lvConsumerIndex();
/* 405:695 */     long availableInQueue = availableInQueue(pIndex, cIndex);
/* 406:696 */     RangeUtil.checkPositive(availableInQueue, "availableInQueue");
/* 407:    */     
/* 408:    */ 
/* 409:    */ 
/* 410:700 */     soProducerLimit(pIndex + Math.min(newMask, availableInQueue));
/* 411:    */     
/* 412:    */ 
/* 413:703 */     soProducerIndex(pIndex + 2L);
/* 414:    */     
/* 415:    */ 
/* 416:    */ 
/* 417:    */ 
/* 418:708 */     UnsafeRefArrayAccess.soElement(oldBuffer, offsetInOld, JUMP);
/* 419:    */   }
/* 420:    */   
/* 421:    */   protected abstract int getNextBufferSize(E[] paramArrayOfE);
/* 422:    */   
/* 423:    */   protected abstract long getCurrentBufferCapacity(long paramLong);
/* 424:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueue
 * JD-Core Version:    0.7.0.1
 */