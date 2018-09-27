/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.CompositeByteBuf;
/*   6:    */ import io.netty.util.ReferenceCountUtil;
/*   7:    */ import io.netty.util.internal.ObjectUtil;
/*   8:    */ import io.netty.util.internal.PlatformDependent;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ import java.util.ArrayDeque;
/*  12:    */ 
/*  13:    */ public abstract class AbstractCoalescingBufferQueue
/*  14:    */ {
/*  15: 33 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractCoalescingBufferQueue.class);
/*  16:    */   private final ArrayDeque<Object> bufAndListenerPairs;
/*  17:    */   private final PendingBytesTracker tracker;
/*  18:    */   private int readableBytes;
/*  19:    */   
/*  20:    */   protected AbstractCoalescingBufferQueue(Channel channel, int initSize)
/*  21:    */   {
/*  22: 46 */     this.bufAndListenerPairs = new ArrayDeque(initSize);
/*  23: 47 */     this.tracker = (channel == null ? null : PendingBytesTracker.newTracker(channel));
/*  24:    */   }
/*  25:    */   
/*  26:    */   public final void addFirst(ByteBuf buf, ChannelPromise promise)
/*  27:    */   {
/*  28: 57 */     addFirst(buf, toChannelFutureListener(promise));
/*  29:    */   }
/*  30:    */   
/*  31:    */   private void addFirst(ByteBuf buf, ChannelFutureListener listener)
/*  32:    */   {
/*  33: 61 */     if (listener != null) {
/*  34: 62 */       this.bufAndListenerPairs.addFirst(listener);
/*  35:    */     }
/*  36: 64 */     this.bufAndListenerPairs.addFirst(buf);
/*  37: 65 */     incrementReadableBytes(buf.readableBytes());
/*  38:    */   }
/*  39:    */   
/*  40:    */   public final void add(ByteBuf buf)
/*  41:    */   {
/*  42: 72 */     add(buf, (ChannelFutureListener)null);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public final void add(ByteBuf buf, ChannelPromise promise)
/*  46:    */   {
/*  47: 84 */     add(buf, toChannelFutureListener(promise));
/*  48:    */   }
/*  49:    */   
/*  50:    */   public final void add(ByteBuf buf, ChannelFutureListener listener)
/*  51:    */   {
/*  52: 96 */     this.bufAndListenerPairs.add(buf);
/*  53: 97 */     if (listener != null) {
/*  54: 98 */       this.bufAndListenerPairs.add(listener);
/*  55:    */     }
/*  56:100 */     incrementReadableBytes(buf.readableBytes());
/*  57:    */   }
/*  58:    */   
/*  59:    */   public final ByteBuf removeFirst(ChannelPromise aggregatePromise)
/*  60:    */   {
/*  61:109 */     Object entry = this.bufAndListenerPairs.poll();
/*  62:110 */     if (entry == null) {
/*  63:111 */       return null;
/*  64:    */     }
/*  65:113 */     assert ((entry instanceof ByteBuf));
/*  66:114 */     ByteBuf result = (ByteBuf)entry;
/*  67:    */     
/*  68:116 */     decrementReadableBytes(result.readableBytes());
/*  69:    */     
/*  70:118 */     entry = this.bufAndListenerPairs.peek();
/*  71:119 */     if ((entry instanceof ChannelFutureListener))
/*  72:    */     {
/*  73:120 */       aggregatePromise.addListener((ChannelFutureListener)entry);
/*  74:121 */       this.bufAndListenerPairs.poll();
/*  75:    */     }
/*  76:123 */     return result;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public final ByteBuf remove(ByteBufAllocator alloc, int bytes, ChannelPromise aggregatePromise)
/*  80:    */   {
/*  81:138 */     ObjectUtil.checkPositiveOrZero(bytes, "bytes");
/*  82:139 */     ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise");
/*  83:142 */     if (this.bufAndListenerPairs.isEmpty()) {
/*  84:143 */       return removeEmptyValue();
/*  85:    */     }
/*  86:145 */     bytes = Math.min(bytes, this.readableBytes);
/*  87:    */     
/*  88:147 */     ByteBuf toReturn = null;
/*  89:148 */     ByteBuf entryBuffer = null;
/*  90:149 */     int originalBytes = bytes;
/*  91:    */     try
/*  92:    */     {
/*  93:    */       for (;;)
/*  94:    */       {
/*  95:152 */         Object entry = this.bufAndListenerPairs.poll();
/*  96:153 */         if (entry == null) {
/*  97:    */           break;
/*  98:    */         }
/*  99:156 */         if ((entry instanceof ChannelFutureListener))
/* 100:    */         {
/* 101:157 */           aggregatePromise.addListener((ChannelFutureListener)entry);
/* 102:    */         }
/* 103:    */         else
/* 104:    */         {
/* 105:160 */           entryBuffer = (ByteBuf)entry;
/* 106:161 */           if (entryBuffer.readableBytes() > bytes)
/* 107:    */           {
/* 108:163 */             this.bufAndListenerPairs.addFirst(entryBuffer);
/* 109:164 */             if (bytes <= 0) {
/* 110:    */               break;
/* 111:    */             }
/* 112:166 */             entryBuffer = entryBuffer.readRetainedSlice(bytes);
/* 113:    */             
/* 114:168 */             toReturn = toReturn == null ? composeFirst(alloc, entryBuffer) : compose(alloc, toReturn, entryBuffer);
/* 115:169 */             bytes = 0; break;
/* 116:    */           }
/* 117:173 */           bytes -= entryBuffer.readableBytes();
/* 118:    */           
/* 119:175 */           toReturn = toReturn == null ? composeFirst(alloc, entryBuffer) : compose(alloc, toReturn, entryBuffer);
/* 120:    */           
/* 121:177 */           entryBuffer = null;
/* 122:    */         }
/* 123:    */       }
/* 124:    */     }
/* 125:    */     catch (Throwable cause)
/* 126:    */     {
/* 127:180 */       ReferenceCountUtil.safeRelease(entryBuffer);
/* 128:181 */       ReferenceCountUtil.safeRelease(toReturn);
/* 129:182 */       aggregatePromise.setFailure(cause);
/* 130:183 */       PlatformDependent.throwException(cause);
/* 131:    */     }
/* 132:185 */     decrementReadableBytes(originalBytes - bytes);
/* 133:186 */     return toReturn;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public final int readableBytes()
/* 137:    */   {
/* 138:193 */     return this.readableBytes;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public final boolean isEmpty()
/* 142:    */   {
/* 143:200 */     return this.bufAndListenerPairs.isEmpty();
/* 144:    */   }
/* 145:    */   
/* 146:    */   public final void releaseAndFailAll(ChannelOutboundInvoker invoker, Throwable cause)
/* 147:    */   {
/* 148:207 */     releaseAndCompleteAll(invoker.newFailedFuture(cause));
/* 149:    */   }
/* 150:    */   
/* 151:    */   public final void copyTo(AbstractCoalescingBufferQueue dest)
/* 152:    */   {
/* 153:215 */     dest.bufAndListenerPairs.addAll(this.bufAndListenerPairs);
/* 154:216 */     dest.incrementReadableBytes(this.readableBytes);
/* 155:    */   }
/* 156:    */   
/* 157:    */   public final void writeAndRemoveAll(ChannelHandlerContext ctx)
/* 158:    */   {
/* 159:224 */     decrementReadableBytes(this.readableBytes);
/* 160:225 */     Throwable pending = null;
/* 161:226 */     ByteBuf previousBuf = null;
/* 162:    */     for (;;)
/* 163:    */     {
/* 164:228 */       Object entry = this.bufAndListenerPairs.poll();
/* 165:    */       try
/* 166:    */       {
/* 167:230 */         if (entry == null)
/* 168:    */         {
/* 169:231 */           if (previousBuf != null) {
/* 170:232 */             ctx.write(previousBuf, ctx.voidPromise());
/* 171:    */           }
/* 172:234 */           break;
/* 173:    */         }
/* 174:237 */         if ((entry instanceof ByteBuf))
/* 175:    */         {
/* 176:238 */           if (previousBuf != null) {
/* 177:239 */             ctx.write(previousBuf, ctx.voidPromise());
/* 178:    */           }
/* 179:241 */           previousBuf = (ByteBuf)entry;
/* 180:    */         }
/* 181:242 */         else if ((entry instanceof ChannelPromise))
/* 182:    */         {
/* 183:243 */           ctx.write(previousBuf, (ChannelPromise)entry);
/* 184:244 */           previousBuf = null;
/* 185:    */         }
/* 186:    */         else
/* 187:    */         {
/* 188:246 */           ctx.write(previousBuf).addListener((ChannelFutureListener)entry);
/* 189:247 */           previousBuf = null;
/* 190:    */         }
/* 191:    */       }
/* 192:    */       catch (Throwable t)
/* 193:    */       {
/* 194:250 */         if (pending == null) {
/* 195:251 */           pending = t;
/* 196:    */         } else {
/* 197:253 */           logger.info("Throwable being suppressed because Throwable {} is already pending", pending, t);
/* 198:    */         }
/* 199:    */       }
/* 200:    */     }
/* 201:257 */     if (pending != null) {
/* 202:258 */       throw new IllegalStateException(pending);
/* 203:    */     }
/* 204:    */   }
/* 205:    */   
/* 206:    */   protected abstract ByteBuf compose(ByteBufAllocator paramByteBufAllocator, ByteBuf paramByteBuf1, ByteBuf paramByteBuf2);
/* 207:    */   
/* 208:    */   protected final ByteBuf composeIntoComposite(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next)
/* 209:    */   {
/* 210:273 */     CompositeByteBuf composite = alloc.compositeBuffer(size() + 2);
/* 211:    */     try
/* 212:    */     {
/* 213:275 */       composite.addComponent(true, cumulation);
/* 214:276 */       composite.addComponent(true, next);
/* 215:    */     }
/* 216:    */     catch (Throwable cause)
/* 217:    */     {
/* 218:278 */       composite.release();
/* 219:279 */       ReferenceCountUtil.safeRelease(next);
/* 220:280 */       PlatformDependent.throwException(cause);
/* 221:    */     }
/* 222:282 */     return composite;
/* 223:    */   }
/* 224:    */   
/* 225:    */   protected final ByteBuf copyAndCompose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next)
/* 226:    */   {
/* 227:293 */     ByteBuf newCumulation = alloc.ioBuffer(cumulation.readableBytes() + next.readableBytes());
/* 228:    */     try
/* 229:    */     {
/* 230:295 */       newCumulation.writeBytes(cumulation).writeBytes(next);
/* 231:    */     }
/* 232:    */     catch (Throwable cause)
/* 233:    */     {
/* 234:297 */       newCumulation.release();
/* 235:298 */       ReferenceCountUtil.safeRelease(next);
/* 236:299 */       PlatformDependent.throwException(cause);
/* 237:    */     }
/* 238:301 */     cumulation.release();
/* 239:302 */     next.release();
/* 240:303 */     return newCumulation;
/* 241:    */   }
/* 242:    */   
/* 243:    */   protected ByteBuf composeFirst(ByteBufAllocator allocator, ByteBuf first)
/* 244:    */   {
/* 245:311 */     return first;
/* 246:    */   }
/* 247:    */   
/* 248:    */   protected abstract ByteBuf removeEmptyValue();
/* 249:    */   
/* 250:    */   protected final int size()
/* 251:    */   {
/* 252:325 */     return this.bufAndListenerPairs.size();
/* 253:    */   }
/* 254:    */   
/* 255:    */   private void releaseAndCompleteAll(ChannelFuture future)
/* 256:    */   {
/* 257:329 */     decrementReadableBytes(this.readableBytes);
/* 258:330 */     Throwable pending = null;
/* 259:    */     for (;;)
/* 260:    */     {
/* 261:332 */       Object entry = this.bufAndListenerPairs.poll();
/* 262:333 */       if (entry == null) {
/* 263:    */         break;
/* 264:    */       }
/* 265:    */       try
/* 266:    */       {
/* 267:337 */         if ((entry instanceof ByteBuf)) {
/* 268:338 */           ReferenceCountUtil.safeRelease(entry);
/* 269:    */         } else {
/* 270:340 */           ((ChannelFutureListener)entry).operationComplete(future);
/* 271:    */         }
/* 272:    */       }
/* 273:    */       catch (Throwable t)
/* 274:    */       {
/* 275:343 */         if (pending == null) {
/* 276:344 */           pending = t;
/* 277:    */         } else {
/* 278:346 */           logger.info("Throwable being suppressed because Throwable {} is already pending", pending, t);
/* 279:    */         }
/* 280:    */       }
/* 281:    */     }
/* 282:350 */     if (pending != null) {
/* 283:351 */       throw new IllegalStateException(pending);
/* 284:    */     }
/* 285:    */   }
/* 286:    */   
/* 287:    */   private void incrementReadableBytes(int increment)
/* 288:    */   {
/* 289:356 */     int nextReadableBytes = this.readableBytes + increment;
/* 290:357 */     if (nextReadableBytes < this.readableBytes) {
/* 291:358 */       throw new IllegalStateException("buffer queue length overflow: " + this.readableBytes + " + " + increment);
/* 292:    */     }
/* 293:360 */     this.readableBytes = nextReadableBytes;
/* 294:361 */     if (this.tracker != null) {
/* 295:362 */       this.tracker.incrementPendingOutboundBytes(increment);
/* 296:    */     }
/* 297:    */   }
/* 298:    */   
/* 299:    */   private void decrementReadableBytes(int decrement)
/* 300:    */   {
/* 301:367 */     this.readableBytes -= decrement;
/* 302:368 */     assert (this.readableBytes >= 0);
/* 303:369 */     if (this.tracker != null) {
/* 304:370 */       this.tracker.decrementPendingOutboundBytes(decrement);
/* 305:    */     }
/* 306:    */   }
/* 307:    */   
/* 308:    */   private static ChannelFutureListener toChannelFutureListener(ChannelPromise promise)
/* 309:    */   {
/* 310:375 */     return promise.isVoid() ? null : new DelegatingChannelPromiseNotifier(promise);
/* 311:    */   }
/* 312:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.AbstractCoalescingBufferQueue
 * JD-Core Version:    0.7.0.1
 */