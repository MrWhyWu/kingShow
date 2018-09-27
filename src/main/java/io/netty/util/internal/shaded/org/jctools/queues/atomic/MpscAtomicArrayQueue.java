/*   1:    */ package io.netty.util.internal.shaded.org.jctools.queues.atomic;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
/*   4:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.ExitCondition;
/*   5:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
/*   6:    */ import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.WaitStrategy;
/*   7:    */ import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
/*   8:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*   9:    */ 
/*  10:    */ public class MpscAtomicArrayQueue<E>
/*  11:    */   extends MpscAtomicArrayQueueL3Pad<E>
/*  12:    */ {
/*  13:    */   public MpscAtomicArrayQueue(int capacity)
/*  14:    */   {
/*  15:176 */     super(capacity);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public boolean offerIfBelowThreshold(E e, int threshold)
/*  19:    */   {
/*  20:188 */     if (null == e) {
/*  21:189 */       throw new NullPointerException();
/*  22:    */     }
/*  23:191 */     int mask = this.mask;
/*  24:192 */     long capacity = mask + 1;
/*  25:    */     
/*  26:194 */     long producerLimit = lvProducerLimit();
/*  27:    */     long pIndex;
/*  28:    */     do
/*  29:    */     {
/*  30:198 */       pIndex = lvProducerIndex();
/*  31:199 */       long available = producerLimit - pIndex;
/*  32:200 */       long size = capacity - available;
/*  33:201 */       if (size >= threshold)
/*  34:    */       {
/*  35:203 */         long cIndex = lvConsumerIndex();
/*  36:204 */         size = pIndex - cIndex;
/*  37:205 */         if (size >= threshold) {
/*  38:207 */           return false;
/*  39:    */         }
/*  40:210 */         producerLimit = cIndex + capacity;
/*  41:    */         
/*  42:212 */         soProducerLimit(producerLimit);
/*  43:    */       }
/*  44:215 */     } while (!casProducerIndex(pIndex, pIndex + 1L));
/*  45:221 */     int offset = calcElementOffset(pIndex, mask);
/*  46:    */     
/*  47:223 */     soElement(this.buffer, offset, e);
/*  48:    */     
/*  49:225 */     return true;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public boolean offer(E e)
/*  53:    */   {
/*  54:240 */     if (null == e) {
/*  55:241 */       throw new NullPointerException();
/*  56:    */     }
/*  57:244 */     int mask = this.mask;
/*  58:    */     
/*  59:246 */     long producerLimit = lvProducerLimit();
/*  60:    */     long pIndex;
/*  61:    */     do
/*  62:    */     {
/*  63:250 */       pIndex = lvProducerIndex();
/*  64:251 */       if (pIndex >= producerLimit)
/*  65:    */       {
/*  66:253 */         long cIndex = lvConsumerIndex();
/*  67:254 */         producerLimit = cIndex + mask + 1L;
/*  68:255 */         if (pIndex >= producerLimit) {
/*  69:257 */           return false;
/*  70:    */         }
/*  71:261 */         soProducerLimit(producerLimit);
/*  72:    */       }
/*  73:264 */     } while (!casProducerIndex(pIndex, pIndex + 1L));
/*  74:270 */     int offset = calcElementOffset(pIndex, mask);
/*  75:    */     
/*  76:272 */     soElement(this.buffer, offset, e);
/*  77:    */     
/*  78:274 */     return true;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public final int failFastOffer(E e)
/*  82:    */   {
/*  83:284 */     if (null == e) {
/*  84:285 */       throw new NullPointerException();
/*  85:    */     }
/*  86:287 */     int mask = this.mask;
/*  87:288 */     long capacity = mask + 1;
/*  88:    */     
/*  89:290 */     long pIndex = lvProducerIndex();
/*  90:    */     
/*  91:292 */     long producerLimit = lvProducerLimit();
/*  92:293 */     if (pIndex >= producerLimit)
/*  93:    */     {
/*  94:295 */       long cIndex = lvConsumerIndex();
/*  95:296 */       producerLimit = cIndex + capacity;
/*  96:297 */       if (pIndex >= producerLimit) {
/*  97:299 */         return 1;
/*  98:    */       }
/*  99:303 */       soProducerLimit(producerLimit);
/* 100:    */     }
/* 101:307 */     if (!casProducerIndex(pIndex, pIndex + 1L)) {
/* 102:309 */       return -1;
/* 103:    */     }
/* 104:312 */     int offset = calcElementOffset(pIndex, mask);
/* 105:313 */     soElement(this.buffer, offset, e);
/* 106:    */     
/* 107:315 */     return 0;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public E poll()
/* 111:    */   {
/* 112:329 */     long cIndex = lpConsumerIndex();
/* 113:330 */     int offset = calcElementOffset(cIndex);
/* 114:    */     
/* 115:332 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 116:    */     
/* 117:    */ 
/* 118:335 */     E e = lvElement(buffer, offset);
/* 119:336 */     if (null == e) {
/* 120:342 */       if (cIndex != lvProducerIndex()) {
/* 121:    */         do
/* 122:    */         {
/* 123:344 */           e = lvElement(buffer, offset);
/* 124:345 */         } while (e == null);
/* 125:    */       } else {
/* 126:347 */         return null;
/* 127:    */       }
/* 128:    */     }
/* 129:350 */     spElement(buffer, offset, null);
/* 130:    */     
/* 131:352 */     soConsumerIndex(cIndex + 1L);
/* 132:353 */     return e;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public E peek()
/* 136:    */   {
/* 137:368 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 138:    */     
/* 139:370 */     long cIndex = lpConsumerIndex();
/* 140:371 */     int offset = calcElementOffset(cIndex);
/* 141:372 */     E e = lvElement(buffer, offset);
/* 142:373 */     if (null == e) {
/* 143:379 */       if (cIndex != lvProducerIndex()) {
/* 144:    */         do
/* 145:    */         {
/* 146:381 */           e = lvElement(buffer, offset);
/* 147:382 */         } while (e == null);
/* 148:    */       } else {
/* 149:384 */         return null;
/* 150:    */       }
/* 151:    */     }
/* 152:387 */     return e;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public boolean relaxedOffer(E e)
/* 156:    */   {
/* 157:392 */     return offer(e);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public E relaxedPoll()
/* 161:    */   {
/* 162:397 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 163:398 */     long cIndex = lpConsumerIndex();
/* 164:399 */     int offset = calcElementOffset(cIndex);
/* 165:    */     
/* 166:    */ 
/* 167:402 */     E e = lvElement(buffer, offset);
/* 168:403 */     if (null == e) {
/* 169:404 */       return null;
/* 170:    */     }
/* 171:406 */     spElement(buffer, offset, null);
/* 172:    */     
/* 173:408 */     soConsumerIndex(cIndex + 1L);
/* 174:409 */     return e;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public E relaxedPeek()
/* 178:    */   {
/* 179:414 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 180:415 */     int mask = this.mask;
/* 181:416 */     long cIndex = lpConsumerIndex();
/* 182:417 */     return lvElement(buffer, calcElementOffset(cIndex, mask));
/* 183:    */   }
/* 184:    */   
/* 185:    */   public int drain(MessagePassingQueue.Consumer<E> c)
/* 186:    */   {
/* 187:422 */     return drain(c, capacity());
/* 188:    */   }
/* 189:    */   
/* 190:    */   public int fill(MessagePassingQueue.Supplier<E> s)
/* 191:    */   {
/* 192:428 */     long result = 0L;
/* 193:429 */     int capacity = capacity();
/* 194:    */     do
/* 195:    */     {
/* 196:431 */       int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
/* 197:432 */       if (filled == 0) {
/* 198:433 */         return (int)result;
/* 199:    */       }
/* 200:435 */       result += filled;
/* 201:436 */     } while (result <= capacity);
/* 202:437 */     return (int)result;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public int drain(MessagePassingQueue.Consumer<E> c, int limit)
/* 206:    */   {
/* 207:442 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 208:443 */     int mask = this.mask;
/* 209:444 */     long cIndex = lpConsumerIndex();
/* 210:445 */     for (int i = 0; i < limit; i++)
/* 211:    */     {
/* 212:446 */       long index = cIndex + i;
/* 213:447 */       int offset = calcElementOffset(index, mask);
/* 214:    */       
/* 215:449 */       E e = lvElement(buffer, offset);
/* 216:450 */       if (null == e) {
/* 217:451 */         return i;
/* 218:    */       }
/* 219:453 */       spElement(buffer, offset, null);
/* 220:    */       
/* 221:455 */       soConsumerIndex(index + 1L);
/* 222:456 */       c.accept(e);
/* 223:    */     }
/* 224:458 */     return limit;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public int fill(MessagePassingQueue.Supplier<E> s, int limit)
/* 228:    */   {
/* 229:463 */     int mask = this.mask;
/* 230:464 */     long capacity = mask + 1;
/* 231:    */     
/* 232:466 */     long producerLimit = lvProducerLimit();
/* 233:    */     
/* 234:468 */     int actualLimit = 0;
/* 235:    */     long pIndex;
/* 236:    */     do
/* 237:    */     {
/* 238:471 */       pIndex = lvProducerIndex();
/* 239:472 */       long available = producerLimit - pIndex;
/* 240:473 */       if (available <= 0L)
/* 241:    */       {
/* 242:475 */         long cIndex = lvConsumerIndex();
/* 243:476 */         producerLimit = cIndex + capacity;
/* 244:477 */         available = producerLimit - pIndex;
/* 245:478 */         if (available <= 0L) {
/* 246:480 */           return 0;
/* 247:    */         }
/* 248:484 */         soProducerLimit(producerLimit);
/* 249:    */       }
/* 250:487 */       actualLimit = Math.min((int)available, limit);
/* 251:488 */     } while (!casProducerIndex(pIndex, pIndex + actualLimit));
/* 252:490 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 253:491 */     for (int i = 0; i < actualLimit; i++)
/* 254:    */     {
/* 255:493 */       int offset = calcElementOffset(pIndex + i, mask);
/* 256:494 */       soElement(buffer, offset, s.get());
/* 257:    */     }
/* 258:496 */     return actualLimit;
/* 259:    */   }
/* 260:    */   
/* 261:    */   public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 262:    */   {
/* 263:501 */     AtomicReferenceArray<E> buffer = this.buffer;
/* 264:502 */     int mask = this.mask;
/* 265:503 */     long cIndex = lpConsumerIndex();
/* 266:504 */     int counter = 0;
/* 267:505 */     while (exit.keepRunning()) {
/* 268:506 */       for (int i = 0; i < 4096; i++)
/* 269:    */       {
/* 270:507 */         int offset = calcElementOffset(cIndex, mask);
/* 271:    */         
/* 272:509 */         E e = lvElement(buffer, offset);
/* 273:510 */         if (null == e)
/* 274:    */         {
/* 275:511 */           counter = w.idle(counter);
/* 276:    */         }
/* 277:    */         else
/* 278:    */         {
/* 279:514 */           cIndex += 1L;
/* 280:515 */           counter = 0;
/* 281:516 */           spElement(buffer, offset, null);
/* 282:    */           
/* 283:518 */           soConsumerIndex(cIndex);
/* 284:519 */           c.accept(e);
/* 285:    */         }
/* 286:    */       }
/* 287:    */     }
/* 288:    */   }
/* 289:    */   
/* 290:    */   public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit)
/* 291:    */   {
/* 292:526 */     int idleCounter = 0;
/* 293:527 */     while (exit.keepRunning()) {
/* 294:528 */       if (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0) {
/* 295:529 */         idleCounter = w.idle(idleCounter);
/* 296:    */       } else {
/* 297:532 */         idleCounter = 0;
/* 298:    */       }
/* 299:    */     }
/* 300:    */   }
/* 301:    */   
/* 302:    */   @Deprecated
/* 303:    */   public int weakOffer(E e)
/* 304:    */   {
/* 305:541 */     return failFastOffer(e);
/* 306:    */   }
/* 307:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueue
 * JD-Core Version:    0.7.0.1
 */