/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
/*   5:    */ 
/*   6:    */ public class MpscArrayQueue<E>
/*   7:    */   extends MpscArrayQueueL3Pad<E>
/*   8:    */ {
/*   9:    */   public MpscArrayQueue(int capacity)
/*  10:    */   {
/*  11:199 */     super(capacity);
/*  12:    */   }
/*  13:    */   
/*  14:    */   public boolean offerIfBelowThreshold(E e, int threshold)
/*  15:    */   {
/*  16:212 */     if (null == e) {
/*  17:214 */       throw new NullPointerException();
/*  18:    */     }
/*  19:216 */     long mask = this.mask;
/*  20:217 */     long capacity = mask + 1L;
/*  21:    */     
/*  22:219 */     long producerLimit = lvProducerLimit();
/*  23:    */     long pIndex;
/*  24:    */     do
/*  25:    */     {
/*  26:223 */       pIndex = lvProducerIndex();
/*  27:224 */       long available = producerLimit - pIndex;
/*  28:225 */       long size = capacity - available;
/*  29:226 */       if (size >= threshold)
/*  30:    */       {
/*  31:228 */         long cIndex = lvConsumerIndex();
/*  32:229 */         size = pIndex - cIndex;
/*  33:230 */         if (size >= threshold) {
/*  34:232 */           return false;
/*  35:    */         }
/*  36:237 */         producerLimit = cIndex + capacity;
/*  37:    */         
/*  38:    */ 
/*  39:240 */         soProducerLimit(producerLimit);
/*  40:    */       }
/*  41:244 */     } while (!casProducerIndex(pIndex, pIndex + 1L));
/*  42:251 */     long offset = calcElementOffset(pIndex, mask);
/*  43:252 */     UnsafeRefArrayAccess.soElement(this.buffer, offset, e);
/*  44:253 */     return true;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean offer(E e)
/*  48:    */   {
/*  49:269 */     if (null == e) {
/*  50:271 */       throw new NullPointerException();
/*  51:    */     }
/*  52:275 */     long mask = this.mask;
/*  53:276 */     long producerLimit = lvProducerLimit();
/*  54:    */     long pIndex;
/*  55:    */     do
/*  56:    */     {
/*  57:280 */       pIndex = lvProducerIndex();
/*  58:281 */       if (pIndex >= producerLimit)
/*  59:    */       {
/*  60:283 */         long cIndex = lvConsumerIndex();
/*  61:284 */         producerLimit = cIndex + mask + 1L;
/*  62:286 */         if (pIndex >= producerLimit) {
/*  63:288 */           return false;
/*  64:    */         }
/*  65:294 */         soProducerLimit(producerLimit);
/*  66:    */       }
/*  67:298 */     } while (!casProducerIndex(pIndex, pIndex + 1L));
/*  68:305 */     long offset = calcElementOffset(pIndex, mask);
/*  69:306 */     UnsafeRefArrayAccess.soElement(this.buffer, offset, e);
/*  70:307 */     return true;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public final int failFastOffer(E e)
/*  74:    */   {
/*  75:318 */     if (null == e) {
/*  76:320 */       throw new NullPointerException();
/*  77:    */     }
/*  78:322 */     long mask = this.mask;
/*  79:323 */     long capacity = mask + 1L;
/*  80:324 */     long pIndex = lvProducerIndex();
/*  81:325 */     long producerLimit = lvProducerLimit();
/*  82:326 */     if (pIndex >= producerLimit)
/*  83:    */     {
/*  84:328 */       long cIndex = lvConsumerIndex();
/*  85:329 */       producerLimit = cIndex + capacity;
/*  86:330 */       if (pIndex >= producerLimit) {
/*  87:332 */         return 1;
/*  88:    */       }
/*  89:337 */       soProducerLimit(producerLimit);
/*  90:    */     }
/*  91:342 */     if (!casProducerIndex(pIndex, pIndex + 1L)) {
/*  92:344 */       return -1;
/*  93:    */     }
/*  94:348 */     long offset = calcElementOffset(pIndex, mask);
/*  95:349 */     UnsafeRefArrayAccess.soElement(this.buffer, offset, e);
/*  96:350 */     return 0;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public E poll()
/* 100:    */   {
/* 101:365 */     long cIndex = lpConsumerIndex();
/* 102:366 */     long offset = calcElementOffset(cIndex);
/* 103:    */     
/* 104:368 */     E[] buffer = this.buffer;
/* 105:    */     
/* 106:    */ 
/* 107:371 */     E e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 108:372 */     if (null == e) {
/* 109:379 */       if (cIndex != lvProducerIndex()) {
/* 110:    */         do
/* 111:    */         {
/* 112:383 */           e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 113:385 */         } while (e == null);
/* 114:    */       } else {
/* 115:389 */         return null;
/* 116:    */       }
/* 117:    */     }
/* 118:393 */     UnsafeRefArrayAccess.spElement(buffer, offset, null);
/* 119:394 */     soConsumerIndex(cIndex + 1L);
/* 120:395 */     return e;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public E peek()
/* 124:    */   {
/* 125:411 */     E[] buffer = this.buffer;
/* 126:    */     
/* 127:413 */     long cIndex = lpConsumerIndex();
/* 128:414 */     long offset = calcElementOffset(cIndex);
/* 129:415 */     E e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 130:416 */     if (null == e) {
/* 131:423 */       if (cIndex != lvProducerIndex()) {
/* 132:    */         do
/* 133:    */         {
/* 134:427 */           e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 135:429 */         } while (e == null);
/* 136:    */       } else {
/* 137:433 */         return null;
/* 138:    */       }
/* 139:    */     }
/* 140:436 */     return e;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean relaxedOffer(E e)
/* 144:    */   {
/* 145:442 */     return offer(e);
/* 146:    */   }
/* 147:    */   
/* 148:    */   public E relaxedPoll()
/* 149:    */   {
/* 150:448 */     E[] buffer = this.buffer;
/* 151:449 */     long cIndex = lpConsumerIndex();
/* 152:450 */     long offset = calcElementOffset(cIndex);
/* 153:    */     
/* 154:    */ 
/* 155:453 */     E e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 156:454 */     if (null == e) {
/* 157:456 */       return null;
/* 158:    */     }
/* 159:459 */     UnsafeRefArrayAccess.spElement(buffer, offset, null);
/* 160:460 */     soConsumerIndex(cIndex + 1L);
/* 161:461 */     return e;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public E relaxedPeek()
/* 165:    */   {
/* 166:467 */     E[] buffer = this.buffer;
/* 167:468 */     long mask = this.mask;
/* 168:469 */     long cIndex = lpConsumerIndex();
/* 169:470 */     return UnsafeRefArrayAccess.lvElement(buffer, calcElementOffset(cIndex, mask));
/* 170:    */   }
/* 171:    */   
/* 172:    */   public int drain(MessagePassingQueue.Consumer<E> c)
/* 173:    */   {
/* 174:476 */     return drain(c, capacity());
/* 175:    */   }
/* 176:    */   
/* 177:    */   public int fill(MessagePassingQueue.Supplier<E> s)
/* 178:    */   {
/* 179:482 */     long result = 0L;
/* 180:483 */     int capacity = capacity();
/* 181:    */     do
/* 182:    */     {
/* 183:486 */       int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
/* 184:487 */       if (filled == 0) {
/* 185:489 */         return (int)result;
/* 186:    */       }
/* 187:491 */       result += filled;
/* 188:493 */     } while (result <= capacity);
/* 189:494 */     return (int)result;
/* 190:    */   }
/* 191:    */   
/* 192:    */   public int drain(MessagePassingQueue.Consumer<E> c, int limit)
/* 193:    */   {
/* 194:500 */     E[] buffer = this.buffer;
/* 195:501 */     long mask = this.mask;
/* 196:502 */     long cIndex = lpConsumerIndex();
/* 197:504 */     for (int i = 0; i < limit; i++)
/* 198:    */     {
/* 199:506 */       long index = cIndex + i;
/* 200:507 */       long offset = calcElementOffset(index, mask);
/* 201:508 */       E e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 202:509 */       if (null == e) {
/* 203:511 */         return i;
/* 204:    */       }
/* 205:513 */       UnsafeRefArrayAccess.spElement(buffer, offset, null);
/* 206:514 */       soConsumerIndex(index + 1L);
/* 207:515 */       c.accept(e);
/* 208:    */     }
/* 209:517 */     return limit;
/* 210:    */   }
/* 211:    */   
/* 212:    */   public int fill(MessagePassingQueue.Supplier<E> s, int limit)
/* 213:    */   {
/* 214:523 */     long mask = this.mask;
/* 215:524 */     long capacity = mask + 1L;
/* 216:525 */     long producerLimit = lvProducerLimit();
/* 217:    */     
/* 218:527 */     int actualLimit = 0;
/* 219:    */     long pIndex;
/* 220:    */     do
/* 221:    */     {
/* 222:530 */       pIndex = lvProducerIndex();
/* 223:531 */       long available = producerLimit - pIndex;
/* 224:532 */       if (available <= 0L)
/* 225:    */       {
/* 226:534 */         long cIndex = lvConsumerIndex();
/* 227:535 */         producerLimit = cIndex + capacity;
/* 228:536 */         available = producerLimit - pIndex;
/* 229:537 */         if (available <= 0L) {
/* 230:539 */           return 0;
/* 231:    */         }
/* 232:544 */         soProducerLimit(producerLimit);
/* 233:    */       }
/* 234:547 */       actualLimit = Math.min((int)available, limit);
/* 235:549 */     } while (!casProducerIndex(pIndex, pIndex + actualLimit));
/* 236:551 */     E[] buffer = this.buffer;
/* 237:552 */     for (int i = 0; i < actualLimit; i++)
/* 238:    */     {
/* 239:555 */       long offset = calcElementOffset(pIndex + i, mask);
/* 240:556 */       UnsafeRefArrayAccess.soElement(buffer, offset, s.get());
/* 241:    */     }
/* 242:558 */     return actualLimit;
/* 243:    */   }
/* 244:    */   
/* 245:    */   public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 246:    */   {
/* 247:564 */     E[] buffer = this.buffer;
/* 248:565 */     long mask = this.mask;
/* 249:566 */     long cIndex = lpConsumerIndex();
/* 250:    */     
/* 251:568 */     int counter = 0;
/* 252:569 */     while (exit.keepRunning()) {
/* 253:571 */       for (int i = 0; i < 4096; i++)
/* 254:    */       {
/* 255:573 */         long offset = calcElementOffset(cIndex, mask);
/* 256:574 */         E e = UnsafeRefArrayAccess.lvElement(buffer, offset);
/* 257:575 */         if (null == e)
/* 258:    */         {
/* 259:577 */           counter = w.idle(counter);
/* 260:    */         }
/* 261:    */         else
/* 262:    */         {
/* 263:580 */           cIndex += 1L;
/* 264:581 */           counter = 0;
/* 265:582 */           UnsafeRefArrayAccess.spElement(buffer, offset, null);
/* 266:583 */           soConsumerIndex(cIndex);
/* 267:584 */           c.accept(e);
/* 268:    */         }
/* 269:    */       }
/* 270:    */     }
/* 271:    */   }
/* 272:    */   
/* 273:    */   public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 274:    */   {
/* 275:592 */     int idleCounter = 0;
/* 276:593 */     while (exit.keepRunning()) {
/* 277:595 */       if (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0) {
/* 278:597 */         idleCounter = w.idle(idleCounter);
/* 279:    */       } else {
/* 280:600 */         idleCounter = 0;
/* 281:    */       }
/* 282:    */     }
/* 283:    */   }
/* 284:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue
 * JD-Core Version:    0.7.0.1
 */