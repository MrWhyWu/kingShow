/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
/*   5:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.ExitCondition;
/*   6:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
/*   7:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.WaitStrategy;
/*   8:    */ import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
/*   9:    */ import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
/*  10:    */ import io.netty.util.internal.shaded.org.jctools.util.Pow2;
/*  11:    */ import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
/*  12:    */ import java.util.Iterator;
/*  13:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*  14:    */ 
/*  15:    */ public abstract class BaseMpscLinkedAtomicArrayQueue<E>
/*  16:    */   extends BaseMpscLinkedAtomicArrayQueueColdProducerFields<E>
/*  17:    */   implements MessagePassingQueue<E>, QueueProgressIndicators
/*  18:    */ {
/*  19:157 */   private static final Object JUMP = new Object();
/*  20:    */   
/*  21:    */   public BaseMpscLinkedAtomicArrayQueue(int initialCapacity)
/*  22:    */   {
/*  23:164 */     RangeUtil.checkGreaterThanOrEqual(initialCapacity, 2, "initialCapacity");
/*  24:165 */     int p2capacity = Pow2.roundToPowerOfTwo(initialCapacity);
/*  25:    */     
/*  26:167 */     long mask = p2capacity - 1 << 1;
/*  27:    */     
/*  28:169 */     AtomicReferenceArray<E> buffer = LinkedAtomicArrayQueueUtil.allocate(p2capacity + 1);
/*  29:170 */     this.producerBuffer = buffer;
/*  30:171 */     this.producerMask = mask;
/*  31:172 */     this.consumerBuffer = buffer;
/*  32:173 */     this.consumerMask = mask;
/*  33:    */     
/*  34:175 */     soProducerLimit(mask);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public final Iterator<E> iterator()
/*  38:    */   {
/*  39:180 */     throw new UnsupportedOperationException();
/*  40:    */   }
/*  41:    */   
/*  42:    */   public final int size()
/*  43:    */   {
/*  44:192 */     long after = lvConsumerIndex();
/*  45:    */     for (;;)
/*  46:    */     {
/*  47:195 */       long before = after;
/*  48:196 */       long currentProducerIndex = lvProducerIndex();
/*  49:197 */       after = lvConsumerIndex();
/*  50:198 */       if (before == after)
/*  51:    */       {
/*  52:199 */         long size = currentProducerIndex - after >> 1;
/*  53:200 */         break;
/*  54:    */       }
/*  55:    */     }
/*  56:    */     long size;
/*  57:204 */     if (size > 2147483647L) {
/*  58:205 */       return 2147483647;
/*  59:    */     }
/*  60:207 */     return (int)size;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public final boolean isEmpty()
/*  64:    */   {
/*  65:214 */     return lvConsumerIndex() == lvProducerIndex();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public String toString()
/*  69:    */   {
/*  70:219 */     return getClass().getName();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean offer(E e)
/*  74:    */   {
/*  75:224 */     if (null == e) {
/*  76:225 */       throw new NullPointerException();
/*  77:    */     }
/*  78:    */     long pIndex;
/*  79:    */     long mask;
/*  80:    */     AtomicReferenceArray<E> buffer;
/*  81:    */     for (;;)
/*  82:    */     {
/*  83:231 */       long producerLimit = lvProducerLimit();
/*  84:232 */       pIndex = lvProducerIndex();
/*  85:234 */       if ((pIndex & 1L) != 1L)
/*  86:    */       {
/*  87:239 */         mask = this.producerMask;
/*  88:240 */         buffer = this.producerBuffer;
/*  89:242 */         if (producerLimit <= pIndex)
/*  90:    */         {
/*  91:243 */           int result = offerSlowPath(mask, pIndex, producerLimit);
/*  92:244 */           switch (result)
/*  93:    */           {
/*  94:    */           case 0: 
/*  95:    */             break;
/*  96:    */           case 1: 
/*  97:    */             break;
/*  98:    */           case 2: 
/*  99:250 */             return false;
/* 100:    */           case 3: 
/* 101:252 */             resize(mask, buffer, pIndex, e);
/* 102:253 */             return true;
/* 103:    */           }
/* 104:    */         }
/* 105:    */         else
/* 106:    */         {
/* 107:256 */           if (casProducerIndex(pIndex, pIndex + 2L)) {
/* 108:    */             break;
/* 109:    */           }
/* 110:    */         }
/* 111:    */       }
/* 112:    */     }
/* 113:261 */     int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex, mask);
/* 114:262 */     LinkedAtomicArrayQueueUtil.soElement(buffer, offset, e);
/* 115:263 */     return true;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public E poll()
/* 119:    */   {
/* 120:274 */     AtomicReferenceArray<E> buffer = this.consumerBuffer;
/* 121:275 */     long index = this.consumerIndex;
/* 122:276 */     long mask = this.consumerMask;
/* 123:277 */     int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 124:    */     
/* 125:279 */     Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
/* 126:280 */     if (e == null) {
/* 127:281 */       if (index != lvProducerIndex()) {
/* 128:    */         do
/* 129:    */         {
/* 130:284 */           e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
/* 131:285 */         } while (e == null);
/* 132:    */       } else {
/* 133:287 */         return null;
/* 134:    */       }
/* 135:    */     }
/* 136:290 */     if (e == JUMP)
/* 137:    */     {
/* 138:291 */       AtomicReferenceArray<E> nextBuffer = getNextBuffer(buffer, mask);
/* 139:292 */       return newBufferPoll(nextBuffer, index);
/* 140:    */     }
/* 141:294 */     LinkedAtomicArrayQueueUtil.soElement(buffer, offset, null);
/* 142:295 */     soConsumerIndex(index + 2L);
/* 143:296 */     return e;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public E peek()
/* 147:    */   {
/* 148:307 */     AtomicReferenceArray<E> buffer = this.consumerBuffer;
/* 149:308 */     long index = this.consumerIndex;
/* 150:309 */     long mask = this.consumerMask;
/* 151:310 */     int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 152:    */     
/* 153:312 */     Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
/* 154:313 */     while ((e == null) && (index != lvProducerIndex()) && 
/* 155:    */     
/* 156:315 */       ((e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset)) == null)) {}
/* 157:318 */     if (e == JUMP) {
/* 158:319 */       return newBufferPeek(getNextBuffer(buffer, mask), index);
/* 159:    */     }
/* 160:321 */     return e;
/* 161:    */   }
/* 162:    */   
/* 163:    */   private int offerSlowPath(long mask, long pIndex, long producerLimit)
/* 164:    */   {
/* 165:329 */     long cIndex = lvConsumerIndex();
/* 166:330 */     long bufferCapacity = getCurrentBufferCapacity(mask);
/* 167:    */     
/* 168:332 */     int result = 0;
/* 169:333 */     if (cIndex + bufferCapacity > pIndex)
/* 170:    */     {
/* 171:334 */       if (!casProducerLimit(producerLimit, cIndex + bufferCapacity)) {
/* 172:336 */         result = 1;
/* 173:    */       }
/* 174:    */     }
/* 175:339 */     else if (availableInQueue(pIndex, cIndex) <= 0L) {
/* 176:341 */       result = 2;
/* 177:343 */     } else if (casProducerIndex(pIndex, pIndex + 1L)) {
/* 178:345 */       result = 3;
/* 179:    */     } else {
/* 180:348 */       result = 1;
/* 181:    */     }
/* 182:350 */     return result;
/* 183:    */   }
/* 184:    */   
/* 185:    */   protected abstract long availableInQueue(long paramLong1, long paramLong2);
/* 186:    */   
/* 187:    */   private AtomicReferenceArray<E> getNextBuffer(AtomicReferenceArray<E> buffer, long mask)
/* 188:    */   {
/* 189:360 */     int offset = nextArrayOffset(mask);
/* 190:361 */     AtomicReferenceArray<E> nextBuffer = (AtomicReferenceArray)LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
/* 191:362 */     LinkedAtomicArrayQueueUtil.soElement(buffer, offset, null);
/* 192:363 */     return nextBuffer;
/* 193:    */   }
/* 194:    */   
/* 195:    */   private int nextArrayOffset(long mask)
/* 196:    */   {
/* 197:367 */     return LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(mask + 2L, 9223372036854775807L);
/* 198:    */   }
/* 199:    */   
/* 200:    */   private E newBufferPoll(AtomicReferenceArray<E> nextBuffer, long index)
/* 201:    */   {
/* 202:371 */     int offset = newBufferAndOffset(nextBuffer, index);
/* 203:    */     
/* 204:373 */     E n = LinkedAtomicArrayQueueUtil.lvElement(nextBuffer, offset);
/* 205:374 */     if (n == null) {
/* 206:375 */       throw new IllegalStateException("new buffer must have at least one element");
/* 207:    */     }
/* 208:378 */     LinkedAtomicArrayQueueUtil.soElement(nextBuffer, offset, null);
/* 209:379 */     soConsumerIndex(index + 2L);
/* 210:380 */     return n;
/* 211:    */   }
/* 212:    */   
/* 213:    */   private E newBufferPeek(AtomicReferenceArray<E> nextBuffer, long index)
/* 214:    */   {
/* 215:384 */     int offset = newBufferAndOffset(nextBuffer, index);
/* 216:    */     
/* 217:386 */     E n = LinkedAtomicArrayQueueUtil.lvElement(nextBuffer, offset);
/* 218:387 */     if (null == n) {
/* 219:388 */       throw new IllegalStateException("new buffer must have at least one element");
/* 220:    */     }
/* 221:390 */     return n;
/* 222:    */   }
/* 223:    */   
/* 224:    */   private int newBufferAndOffset(AtomicReferenceArray<E> nextBuffer, long index)
/* 225:    */   {
/* 226:394 */     this.consumerBuffer = nextBuffer;
/* 227:395 */     this.consumerMask = (LinkedAtomicArrayQueueUtil.length(nextBuffer) - 2 << 1);
/* 228:396 */     int offsetInNew = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, this.consumerMask);
/* 229:397 */     return offsetInNew;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public long currentProducerIndex()
/* 233:    */   {
/* 234:402 */     return lvProducerIndex() / 2L;
/* 235:    */   }
/* 236:    */   
/* 237:    */   public long currentConsumerIndex()
/* 238:    */   {
/* 239:407 */     return lvConsumerIndex() / 2L;
/* 240:    */   }
/* 241:    */   
/* 242:    */   public abstract int capacity();
/* 243:    */   
/* 244:    */   public boolean relaxedOffer(E e)
/* 245:    */   {
/* 246:415 */     return offer(e);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public E relaxedPoll()
/* 250:    */   {
/* 251:421 */     AtomicReferenceArray<E> buffer = this.consumerBuffer;
/* 252:422 */     long index = this.consumerIndex;
/* 253:423 */     long mask = this.consumerMask;
/* 254:424 */     int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 255:    */     
/* 256:426 */     Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
/* 257:427 */     if (e == null) {
/* 258:428 */       return null;
/* 259:    */     }
/* 260:430 */     if (e == JUMP)
/* 261:    */     {
/* 262:431 */       AtomicReferenceArray<E> nextBuffer = getNextBuffer(buffer, mask);
/* 263:432 */       return newBufferPoll(nextBuffer, index);
/* 264:    */     }
/* 265:434 */     LinkedAtomicArrayQueueUtil.soElement(buffer, offset, null);
/* 266:435 */     soConsumerIndex(index + 2L);
/* 267:436 */     return e;
/* 268:    */   }
/* 269:    */   
/* 270:    */   public E relaxedPeek()
/* 271:    */   {
/* 272:442 */     AtomicReferenceArray<E> buffer = this.consumerBuffer;
/* 273:443 */     long index = this.consumerIndex;
/* 274:444 */     long mask = this.consumerMask;
/* 275:445 */     int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
/* 276:    */     
/* 277:447 */     Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
/* 278:448 */     if (e == JUMP) {
/* 279:449 */       return newBufferPeek(getNextBuffer(buffer, mask), index);
/* 280:    */     }
/* 281:451 */     return e;
/* 282:    */   }
/* 283:    */   
/* 284:    */   public int fill(MessagePassingQueue.Supplier<E> s)
/* 285:    */   {
/* 286:457 */     long result = 0L;
/* 287:458 */     int capacity = capacity();
/* 288:    */     do
/* 289:    */     {
/* 290:460 */       int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
/* 291:461 */       if (filled == 0) {
/* 292:462 */         return (int)result;
/* 293:    */       }
/* 294:464 */       result += filled;
/* 295:465 */     } while (result <= capacity);
/* 296:466 */     return (int)result;
/* 297:    */   }
/* 298:    */   
/* 299:    */   public int fill(MessagePassingQueue.Supplier<E> s, int batchSize)
/* 300:    */   {
/* 301:    */     long pIndex;
/* 302:    */     long mask;
/* 303:    */     AtomicReferenceArray<E> buffer;
/* 304:    */     for (;;)
/* 305:    */     {
/* 306:476 */       long producerLimit = lvProducerLimit();
/* 307:477 */       pIndex = lvProducerIndex();
/* 308:479 */       if ((pIndex & 1L) != 1L)
/* 309:    */       {
/* 310:486 */         mask = this.producerMask;
/* 311:487 */         buffer = this.producerBuffer;
/* 312:    */         
/* 313:    */ 
/* 314:490 */         long batchIndex = Math.min(producerLimit, pIndex + 2 * batchSize);
/* 315:491 */         if ((pIndex == producerLimit) || (producerLimit < batchIndex))
/* 316:    */         {
/* 317:492 */           int result = offerSlowPath(mask, pIndex, producerLimit);
/* 318:493 */           switch (result)
/* 319:    */           {
/* 320:    */           case 1: 
/* 321:    */             break;
/* 322:    */           case 2: 
/* 323:497 */             return 0;
/* 324:    */           case 3: 
/* 325:499 */             resize(mask, buffer, pIndex, s.get());
/* 326:500 */             return 1;
/* 327:    */           }
/* 328:    */         }
/* 329:504 */         else if (casProducerIndex(pIndex, batchIndex))
/* 330:    */         {
/* 331:505 */           int claimedSlots = (int)((batchIndex - pIndex) / 2L);
/* 332:506 */           break;
/* 333:    */         }
/* 334:    */       }
/* 335:    */     }
/* 336:    */     int claimedSlots;
/* 337:509 */     int i = 0;
/* 338:510 */     for (i = 0; i < claimedSlots; i++)
/* 339:    */     {
/* 340:511 */       int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex + 2 * i, mask);
/* 341:512 */       LinkedAtomicArrayQueueUtil.soElement(buffer, offset, s.get());
/* 342:    */     }
/* 343:514 */     return claimedSlots;
/* 344:    */   }
/* 345:    */   
/* 346:    */   public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 347:    */   {
/* 348:519 */     while (exit.keepRunning())
/* 349:    */     {
/* 350:520 */       while ((fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) != 0) && (exit.keepRunning())) {}
/* 351:523 */       int idleCounter = 0;
/* 352:524 */       while ((exit.keepRunning()) && (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0)) {
/* 353:525 */         idleCounter = w.idle(idleCounter);
/* 354:    */       }
/* 355:    */     }
/* 356:    */   }
/* 357:    */   
/* 358:    */   public int drain(MessagePassingQueue.Consumer<E> c)
/* 359:    */   {
/* 360:532 */     return drain(c, capacity());
/* 361:    */   }
/* 362:    */   
/* 363:    */   public int drain(MessagePassingQueue.Consumer<E> c, int limit)
/* 364:    */   {
/* 365:    */     E m;
/* 366:539 */     for (int i = 0; (i < limit) && ((m = relaxedPoll()) != null); i++) {
/* 367:542 */       c.accept(m);
/* 368:    */     }
/* 369:544 */     return i;
/* 370:    */   }
/* 371:    */   
/* 372:    */   public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 373:    */   {
/* 374:549 */     int idleCounter = 0;
/* 375:550 */     while (exit.keepRunning())
/* 376:    */     {
/* 377:551 */       E e = relaxedPoll();
/* 378:552 */       if (e == null)
/* 379:    */       {
/* 380:553 */         idleCounter = w.idle(idleCounter);
/* 381:    */       }
/* 382:    */       else
/* 383:    */       {
/* 384:556 */         idleCounter = 0;
/* 385:557 */         c.accept(e);
/* 386:    */       }
/* 387:    */     }
/* 388:    */   }
/* 389:    */   
/* 390:    */   private void resize(long oldMask, AtomicReferenceArray<E> oldBuffer, long pIndex, E e)
/* 391:    */   {
/* 392:562 */     int newBufferLength = getNextBufferSize(oldBuffer);
/* 393:563 */     AtomicReferenceArray<E> newBuffer = LinkedAtomicArrayQueueUtil.allocate(newBufferLength);
/* 394:564 */     this.producerBuffer = newBuffer;
/* 395:565 */     int newMask = newBufferLength - 2 << 1;
/* 396:566 */     this.producerMask = newMask;
/* 397:567 */     int offsetInOld = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex, oldMask);
/* 398:568 */     int offsetInNew = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex, newMask);
/* 399:    */     
/* 400:570 */     LinkedAtomicArrayQueueUtil.soElement(newBuffer, offsetInNew, e);
/* 401:    */     
/* 402:572 */     LinkedAtomicArrayQueueUtil.soElement(oldBuffer, nextArrayOffset(oldMask), newBuffer);
/* 403:    */     
/* 404:574 */     long cIndex = lvConsumerIndex();
/* 405:575 */     long availableInQueue = availableInQueue(pIndex, cIndex);
/* 406:576 */     RangeUtil.checkPositive(availableInQueue, "availableInQueue");
/* 407:    */     
/* 408:    */ 
/* 409:579 */     soProducerLimit(pIndex + Math.min(newMask, availableInQueue));
/* 410:    */     
/* 411:581 */     soProducerIndex(pIndex + 2L);
/* 412:    */     
/* 413:    */ 
/* 414:584 */     LinkedAtomicArrayQueueUtil.soElement(oldBuffer, offsetInOld, JUMP);
/* 415:    */   }
/* 416:    */   
/* 417:    */   protected abstract int getNextBufferSize(AtomicReferenceArray<E> paramAtomicReferenceArray);
/* 418:    */   
/* 419:    */   protected abstract long getCurrentBufferCapacity(long paramLong);
/* 420:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueue
 * JD-Core Version:    0.7.0.1
 */