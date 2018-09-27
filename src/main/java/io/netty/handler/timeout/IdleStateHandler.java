/*   1:    */ package io.netty.handler.timeout;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelDuplexHandler;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.util.concurrent.EventExecutor;
/*  12:    */ import java.util.concurrent.ScheduledFuture;
/*  13:    */ import java.util.concurrent.TimeUnit;
/*  14:    */ 
/*  15:    */ public class IdleStateHandler
/*  16:    */   extends ChannelDuplexHandler
/*  17:    */ {
/*  18: 99 */   private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
/*  19:102 */   private final ChannelFutureListener writeListener = new ChannelFutureListener()
/*  20:    */   {
/*  21:    */     public void operationComplete(ChannelFuture future)
/*  22:    */       throws Exception
/*  23:    */     {
/*  24:105 */       IdleStateHandler.this.lastWriteTime = IdleStateHandler.this.ticksInNanos();
/*  25:106 */       IdleStateHandler.this.firstWriterIdleEvent = IdleStateHandler.access$202(IdleStateHandler.this, true);
/*  26:    */     }
/*  27:    */   };
/*  28:    */   private final boolean observeOutput;
/*  29:    */   private final long readerIdleTimeNanos;
/*  30:    */   private final long writerIdleTimeNanos;
/*  31:    */   private final long allIdleTimeNanos;
/*  32:    */   private ScheduledFuture<?> readerIdleTimeout;
/*  33:    */   private long lastReadTime;
/*  34:117 */   private boolean firstReaderIdleEvent = true;
/*  35:    */   private ScheduledFuture<?> writerIdleTimeout;
/*  36:    */   private long lastWriteTime;
/*  37:121 */   private boolean firstWriterIdleEvent = true;
/*  38:    */   private ScheduledFuture<?> allIdleTimeout;
/*  39:124 */   private boolean firstAllIdleEvent = true;
/*  40:    */   private byte state;
/*  41:    */   private boolean reading;
/*  42:    */   private long lastChangeCheckTimeStamp;
/*  43:    */   private int lastMessageHashCode;
/*  44:    */   private long lastPendingWriteBytes;
/*  45:    */   
/*  46:    */   public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds)
/*  47:    */   {
/*  48:154 */     this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit)
/*  52:    */   {
/*  53:164 */     this(false, readerIdleTime, writerIdleTime, allIdleTime, unit);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public IdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit)
/*  57:    */   {
/*  58:192 */     if (unit == null) {
/*  59:193 */       throw new NullPointerException("unit");
/*  60:    */     }
/*  61:196 */     this.observeOutput = observeOutput;
/*  62:198 */     if (readerIdleTime <= 0L) {
/*  63:199 */       this.readerIdleTimeNanos = 0L;
/*  64:    */     } else {
/*  65:201 */       this.readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
/*  66:    */     }
/*  67:203 */     if (writerIdleTime <= 0L) {
/*  68:204 */       this.writerIdleTimeNanos = 0L;
/*  69:    */     } else {
/*  70:206 */       this.writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
/*  71:    */     }
/*  72:208 */     if (allIdleTime <= 0L) {
/*  73:209 */       this.allIdleTimeNanos = 0L;
/*  74:    */     } else {
/*  75:211 */       this.allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
/*  76:    */     }
/*  77:    */   }
/*  78:    */   
/*  79:    */   public long getReaderIdleTimeInMillis()
/*  80:    */   {
/*  81:220 */     return TimeUnit.NANOSECONDS.toMillis(this.readerIdleTimeNanos);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public long getWriterIdleTimeInMillis()
/*  85:    */   {
/*  86:228 */     return TimeUnit.NANOSECONDS.toMillis(this.writerIdleTimeNanos);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public long getAllIdleTimeInMillis()
/*  90:    */   {
/*  91:236 */     return TimeUnit.NANOSECONDS.toMillis(this.allIdleTimeNanos);
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  95:    */     throws Exception
/*  96:    */   {
/*  97:241 */     if ((ctx.channel().isActive()) && (ctx.channel().isRegistered())) {
/*  98:244 */       initialize(ctx);
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 103:    */     throws Exception
/* 104:    */   {
/* 105:253 */     destroy();
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void channelRegistered(ChannelHandlerContext ctx)
/* 109:    */     throws Exception
/* 110:    */   {
/* 111:259 */     if (ctx.channel().isActive()) {
/* 112:260 */       initialize(ctx);
/* 113:    */     }
/* 114:262 */     super.channelRegistered(ctx);
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void channelActive(ChannelHandlerContext ctx)
/* 118:    */     throws Exception
/* 119:    */   {
/* 120:270 */     initialize(ctx);
/* 121:271 */     super.channelActive(ctx);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 125:    */     throws Exception
/* 126:    */   {
/* 127:276 */     destroy();
/* 128:277 */     super.channelInactive(ctx);
/* 129:    */   }
/* 130:    */   
/* 131:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 132:    */     throws Exception
/* 133:    */   {
/* 134:282 */     if ((this.readerIdleTimeNanos > 0L) || (this.allIdleTimeNanos > 0L))
/* 135:    */     {
/* 136:283 */       this.reading = true;
/* 137:284 */       this.firstReaderIdleEvent = (this.firstAllIdleEvent = 1);
/* 138:    */     }
/* 139:286 */     ctx.fireChannelRead(msg);
/* 140:    */   }
/* 141:    */   
/* 142:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 143:    */     throws Exception
/* 144:    */   {
/* 145:291 */     if (((this.readerIdleTimeNanos > 0L) || (this.allIdleTimeNanos > 0L)) && (this.reading))
/* 146:    */     {
/* 147:292 */       this.lastReadTime = ticksInNanos();
/* 148:293 */       this.reading = false;
/* 149:    */     }
/* 150:295 */     ctx.fireChannelReadComplete();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:301 */     if ((this.writerIdleTimeNanos > 0L) || (this.allIdleTimeNanos > 0L)) {
/* 157:302 */       ctx.write(msg, promise.unvoid()).addListener(this.writeListener);
/* 158:    */     } else {
/* 159:304 */       ctx.write(msg, promise);
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   private void initialize(ChannelHandlerContext ctx)
/* 164:    */   {
/* 165:311 */     switch (this.state)
/* 166:    */     {
/* 167:    */     case 1: 
/* 168:    */     case 2: 
/* 169:314 */       return;
/* 170:    */     }
/* 171:317 */     this.state = 1;
/* 172:318 */     initOutputChanged(ctx);
/* 173:    */     
/* 174:320 */     this.lastReadTime = (this.lastWriteTime = ticksInNanos());
/* 175:321 */     if (this.readerIdleTimeNanos > 0L) {
/* 176:322 */       this.readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx), this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 177:    */     }
/* 178:325 */     if (this.writerIdleTimeNanos > 0L) {
/* 179:326 */       this.writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx), this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 180:    */     }
/* 181:329 */     if (this.allIdleTimeNanos > 0L) {
/* 182:330 */       this.allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx), this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 183:    */     }
/* 184:    */   }
/* 185:    */   
/* 186:    */   long ticksInNanos()
/* 187:    */   {
/* 188:339 */     return System.nanoTime();
/* 189:    */   }
/* 190:    */   
/* 191:    */   ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit)
/* 192:    */   {
/* 193:346 */     return ctx.executor().schedule(task, delay, unit);
/* 194:    */   }
/* 195:    */   
/* 196:    */   private void destroy()
/* 197:    */   {
/* 198:350 */     this.state = 2;
/* 199:352 */     if (this.readerIdleTimeout != null)
/* 200:    */     {
/* 201:353 */       this.readerIdleTimeout.cancel(false);
/* 202:354 */       this.readerIdleTimeout = null;
/* 203:    */     }
/* 204:356 */     if (this.writerIdleTimeout != null)
/* 205:    */     {
/* 206:357 */       this.writerIdleTimeout.cancel(false);
/* 207:358 */       this.writerIdleTimeout = null;
/* 208:    */     }
/* 209:360 */     if (this.allIdleTimeout != null)
/* 210:    */     {
/* 211:361 */       this.allIdleTimeout.cancel(false);
/* 212:362 */       this.allIdleTimeout = null;
/* 213:    */     }
/* 214:    */   }
/* 215:    */   
/* 216:    */   protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
/* 217:    */     throws Exception
/* 218:    */   {
/* 219:371 */     ctx.fireUserEventTriggered(evt);
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first)
/* 223:    */   {
/* 224:378 */     switch (2.$SwitchMap$io$netty$handler$timeout$IdleState[state.ordinal()])
/* 225:    */     {
/* 226:    */     case 1: 
/* 227:380 */       return first ? IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT : IdleStateEvent.ALL_IDLE_STATE_EVENT;
/* 228:    */     case 2: 
/* 229:382 */       return first ? IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT : IdleStateEvent.READER_IDLE_STATE_EVENT;
/* 230:    */     case 3: 
/* 231:384 */       return first ? IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT : IdleStateEvent.WRITER_IDLE_STATE_EVENT;
/* 232:    */     }
/* 233:386 */     throw new IllegalArgumentException("Unhandled: state=" + state + ", first=" + first);
/* 234:    */   }
/* 235:    */   
/* 236:    */   private void initOutputChanged(ChannelHandlerContext ctx)
/* 237:    */   {
/* 238:394 */     if (this.observeOutput)
/* 239:    */     {
/* 240:395 */       Channel channel = ctx.channel();
/* 241:396 */       Channel.Unsafe unsafe = channel.unsafe();
/* 242:397 */       ChannelOutboundBuffer buf = unsafe.outboundBuffer();
/* 243:399 */       if (buf != null)
/* 244:    */       {
/* 245:400 */         this.lastMessageHashCode = System.identityHashCode(buf.current());
/* 246:401 */         this.lastPendingWriteBytes = buf.totalPendingWriteBytes();
/* 247:    */       }
/* 248:    */     }
/* 249:    */   }
/* 250:    */   
/* 251:    */   private boolean hasOutputChanged(ChannelHandlerContext ctx, boolean first)
/* 252:    */   {
/* 253:414 */     if (this.observeOutput)
/* 254:    */     {
/* 255:421 */       if (this.lastChangeCheckTimeStamp != this.lastWriteTime)
/* 256:    */       {
/* 257:422 */         this.lastChangeCheckTimeStamp = this.lastWriteTime;
/* 258:425 */         if (!first) {
/* 259:426 */           return true;
/* 260:    */         }
/* 261:    */       }
/* 262:430 */       Channel channel = ctx.channel();
/* 263:431 */       Channel.Unsafe unsafe = channel.unsafe();
/* 264:432 */       ChannelOutboundBuffer buf = unsafe.outboundBuffer();
/* 265:434 */       if (buf != null)
/* 266:    */       {
/* 267:435 */         int messageHashCode = System.identityHashCode(buf.current());
/* 268:436 */         long pendingWriteBytes = buf.totalPendingWriteBytes();
/* 269:438 */         if ((messageHashCode != this.lastMessageHashCode) || (pendingWriteBytes != this.lastPendingWriteBytes))
/* 270:    */         {
/* 271:439 */           this.lastMessageHashCode = messageHashCode;
/* 272:440 */           this.lastPendingWriteBytes = pendingWriteBytes;
/* 273:442 */           if (!first) {
/* 274:443 */             return true;
/* 275:    */           }
/* 276:    */         }
/* 277:    */       }
/* 278:    */     }
/* 279:449 */     return false;
/* 280:    */   }
/* 281:    */   
/* 282:    */   private static abstract class AbstractIdleTask
/* 283:    */     implements Runnable
/* 284:    */   {
/* 285:    */     private final ChannelHandlerContext ctx;
/* 286:    */     
/* 287:    */     AbstractIdleTask(ChannelHandlerContext ctx)
/* 288:    */     {
/* 289:457 */       this.ctx = ctx;
/* 290:    */     }
/* 291:    */     
/* 292:    */     public void run()
/* 293:    */     {
/* 294:462 */       if (!this.ctx.channel().isOpen()) {
/* 295:463 */         return;
/* 296:    */       }
/* 297:466 */       run(this.ctx);
/* 298:    */     }
/* 299:    */     
/* 300:    */     protected abstract void run(ChannelHandlerContext paramChannelHandlerContext);
/* 301:    */   }
/* 302:    */   
/* 303:    */   private final class ReaderIdleTimeoutTask
/* 304:    */     extends IdleStateHandler.AbstractIdleTask
/* 305:    */   {
/* 306:    */     ReaderIdleTimeoutTask(ChannelHandlerContext ctx)
/* 307:    */     {
/* 308:475 */       super();
/* 309:    */     }
/* 310:    */     
/* 311:    */     protected void run(ChannelHandlerContext ctx)
/* 312:    */     {
/* 313:480 */       long nextDelay = IdleStateHandler.this.readerIdleTimeNanos;
/* 314:481 */       if (!IdleStateHandler.this.reading) {
/* 315:482 */         nextDelay -= IdleStateHandler.this.ticksInNanos() - IdleStateHandler.this.lastReadTime;
/* 316:    */       }
/* 317:485 */       if (nextDelay <= 0L)
/* 318:    */       {
/* 319:487 */         IdleStateHandler.this.readerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, IdleStateHandler.this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 320:    */         
/* 321:489 */         boolean first = IdleStateHandler.this.firstReaderIdleEvent;
/* 322:490 */         IdleStateHandler.this.firstReaderIdleEvent = false;
/* 323:    */         try
/* 324:    */         {
/* 325:493 */           IdleStateEvent event = IdleStateHandler.this.newIdleStateEvent(IdleState.READER_IDLE, first);
/* 326:494 */           IdleStateHandler.this.channelIdle(ctx, event);
/* 327:    */         }
/* 328:    */         catch (Throwable t)
/* 329:    */         {
/* 330:496 */           ctx.fireExceptionCaught(t);
/* 331:    */         }
/* 332:    */       }
/* 333:    */       else
/* 334:    */       {
/* 335:500 */         IdleStateHandler.this.readerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
/* 336:    */       }
/* 337:    */     }
/* 338:    */   }
/* 339:    */   
/* 340:    */   private final class WriterIdleTimeoutTask
/* 341:    */     extends IdleStateHandler.AbstractIdleTask
/* 342:    */   {
/* 343:    */     WriterIdleTimeoutTask(ChannelHandlerContext ctx)
/* 344:    */     {
/* 345:508 */       super();
/* 346:    */     }
/* 347:    */     
/* 348:    */     protected void run(ChannelHandlerContext ctx)
/* 349:    */     {
/* 350:514 */       long lastWriteTime = IdleStateHandler.this.lastWriteTime;
/* 351:515 */       long nextDelay = IdleStateHandler.this.writerIdleTimeNanos - (IdleStateHandler.this.ticksInNanos() - lastWriteTime);
/* 352:516 */       if (nextDelay <= 0L)
/* 353:    */       {
/* 354:518 */         IdleStateHandler.this.writerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, IdleStateHandler.this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 355:    */         
/* 356:520 */         boolean first = IdleStateHandler.this.firstWriterIdleEvent;
/* 357:521 */         IdleStateHandler.this.firstWriterIdleEvent = false;
/* 358:    */         try
/* 359:    */         {
/* 360:524 */           if (IdleStateHandler.this.hasOutputChanged(ctx, first)) {
/* 361:525 */             return;
/* 362:    */           }
/* 363:528 */           IdleStateEvent event = IdleStateHandler.this.newIdleStateEvent(IdleState.WRITER_IDLE, first);
/* 364:529 */           IdleStateHandler.this.channelIdle(ctx, event);
/* 365:    */         }
/* 366:    */         catch (Throwable t)
/* 367:    */         {
/* 368:531 */           ctx.fireExceptionCaught(t);
/* 369:    */         }
/* 370:    */       }
/* 371:    */       else
/* 372:    */       {
/* 373:535 */         IdleStateHandler.this.writerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
/* 374:    */       }
/* 375:    */     }
/* 376:    */   }
/* 377:    */   
/* 378:    */   private final class AllIdleTimeoutTask
/* 379:    */     extends IdleStateHandler.AbstractIdleTask
/* 380:    */   {
/* 381:    */     AllIdleTimeoutTask(ChannelHandlerContext ctx)
/* 382:    */     {
/* 383:543 */       super();
/* 384:    */     }
/* 385:    */     
/* 386:    */     protected void run(ChannelHandlerContext ctx)
/* 387:    */     {
/* 388:549 */       long nextDelay = IdleStateHandler.this.allIdleTimeNanos;
/* 389:550 */       if (!IdleStateHandler.this.reading) {
/* 390:551 */         nextDelay -= IdleStateHandler.this.ticksInNanos() - Math.max(IdleStateHandler.this.lastReadTime, IdleStateHandler.this.lastWriteTime);
/* 391:    */       }
/* 392:553 */       if (nextDelay <= 0L)
/* 393:    */       {
/* 394:556 */         IdleStateHandler.this.allIdleTimeout = IdleStateHandler.this.schedule(ctx, this, IdleStateHandler.this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
/* 395:    */         
/* 396:558 */         boolean first = IdleStateHandler.this.firstAllIdleEvent;
/* 397:559 */         IdleStateHandler.this.firstAllIdleEvent = false;
/* 398:    */         try
/* 399:    */         {
/* 400:562 */           if (IdleStateHandler.this.hasOutputChanged(ctx, first)) {
/* 401:563 */             return;
/* 402:    */           }
/* 403:566 */           IdleStateEvent event = IdleStateHandler.this.newIdleStateEvent(IdleState.ALL_IDLE, first);
/* 404:567 */           IdleStateHandler.this.channelIdle(ctx, event);
/* 405:    */         }
/* 406:    */         catch (Throwable t)
/* 407:    */         {
/* 408:569 */           ctx.fireExceptionCaught(t);
/* 409:    */         }
/* 410:    */       }
/* 411:    */       else
/* 412:    */       {
/* 413:574 */         IdleStateHandler.this.allIdleTimeout = IdleStateHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
/* 414:    */       }
/* 415:    */     }
/* 416:    */   }
/* 417:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.timeout.IdleStateHandler
 * JD-Core Version:    0.7.0.1
 */