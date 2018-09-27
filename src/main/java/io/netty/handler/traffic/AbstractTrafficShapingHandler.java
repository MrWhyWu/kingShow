/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelDuplexHandler;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  11:    */ import io.netty.channel.ChannelPromise;
/*  12:    */ import io.netty.util.Attribute;
/*  13:    */ import io.netty.util.AttributeKey;
/*  14:    */ import io.netty.util.concurrent.EventExecutor;
/*  15:    */ import io.netty.util.internal.logging.InternalLogger;
/*  16:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  17:    */ import java.util.concurrent.TimeUnit;
/*  18:    */ 
/*  19:    */ public abstract class AbstractTrafficShapingHandler
/*  20:    */   extends ChannelDuplexHandler
/*  21:    */ {
/*  22: 50 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractTrafficShapingHandler.class);
/*  23:    */   public static final long DEFAULT_CHECK_INTERVAL = 1000L;
/*  24:    */   public static final long DEFAULT_MAX_TIME = 15000L;
/*  25:    */   static final long DEFAULT_MAX_SIZE = 4194304L;
/*  26:    */   static final long MINIMAL_WAIT = 10L;
/*  27:    */   protected TrafficCounter trafficCounter;
/*  28:    */   private volatile long writeLimit;
/*  29:    */   private volatile long readLimit;
/*  30: 91 */   protected volatile long maxTime = 15000L;
/*  31: 96 */   protected volatile long checkInterval = 1000L;
/*  32: 99 */   static final AttributeKey<Boolean> READ_SUSPENDED = AttributeKey.valueOf(AbstractTrafficShapingHandler.class.getName() + ".READ_SUSPENDED");
/*  33:100 */   static final AttributeKey<Runnable> REOPEN_TASK = AttributeKey.valueOf(AbstractTrafficShapingHandler.class
/*  34:101 */     .getName() + ".REOPEN_TASK");
/*  35:106 */   volatile long maxWriteDelay = 4000L;
/*  36:110 */   volatile long maxWriteSize = 4194304L;
/*  37:    */   final int userDefinedWritabilityIndex;
/*  38:    */   static final int CHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX = 1;
/*  39:    */   static final int GLOBAL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX = 2;
/*  40:    */   static final int GLOBALCHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX = 3;
/*  41:    */   
/*  42:    */   void setTrafficCounter(TrafficCounter newTrafficCounter)
/*  43:    */   {
/*  44:138 */     this.trafficCounter = newTrafficCounter;
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected int userDefinedWritabilityIndex()
/*  48:    */   {
/*  49:149 */     return 1;
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected AbstractTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime)
/*  53:    */   {
/*  54:165 */     if (maxTime <= 0L) {
/*  55:166 */       throw new IllegalArgumentException("maxTime must be positive");
/*  56:    */     }
/*  57:169 */     this.userDefinedWritabilityIndex = userDefinedWritabilityIndex();
/*  58:170 */     this.writeLimit = writeLimit;
/*  59:171 */     this.readLimit = readLimit;
/*  60:172 */     this.checkInterval = checkInterval;
/*  61:173 */     this.maxTime = maxTime;
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected AbstractTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval)
/*  65:    */   {
/*  66:187 */     this(writeLimit, readLimit, checkInterval, 15000L);
/*  67:    */   }
/*  68:    */   
/*  69:    */   protected AbstractTrafficShapingHandler(long writeLimit, long readLimit)
/*  70:    */   {
/*  71:200 */     this(writeLimit, readLimit, 1000L, 15000L);
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected AbstractTrafficShapingHandler()
/*  75:    */   {
/*  76:208 */     this(0L, 0L, 1000L, 15000L);
/*  77:    */   }
/*  78:    */   
/*  79:    */   protected AbstractTrafficShapingHandler(long checkInterval)
/*  80:    */   {
/*  81:220 */     this(0L, 0L, checkInterval, 15000L);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void configure(long newWriteLimit, long newReadLimit, long newCheckInterval)
/*  85:    */   {
/*  86:237 */     configure(newWriteLimit, newReadLimit);
/*  87:238 */     configure(newCheckInterval);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void configure(long newWriteLimit, long newReadLimit)
/*  91:    */   {
/*  92:253 */     this.writeLimit = newWriteLimit;
/*  93:254 */     this.readLimit = newReadLimit;
/*  94:255 */     if (this.trafficCounter != null) {
/*  95:256 */       this.trafficCounter.resetAccounting(TrafficCounter.milliSecondFromNano());
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void configure(long newCheckInterval)
/* 100:    */   {
/* 101:266 */     this.checkInterval = newCheckInterval;
/* 102:267 */     if (this.trafficCounter != null) {
/* 103:268 */       this.trafficCounter.configure(this.checkInterval);
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   public long getWriteLimit()
/* 108:    */   {
/* 109:276 */     return this.writeLimit;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public void setWriteLimit(long writeLimit)
/* 113:    */   {
/* 114:289 */     this.writeLimit = writeLimit;
/* 115:290 */     if (this.trafficCounter != null) {
/* 116:291 */       this.trafficCounter.resetAccounting(TrafficCounter.milliSecondFromNano());
/* 117:    */     }
/* 118:    */   }
/* 119:    */   
/* 120:    */   public long getReadLimit()
/* 121:    */   {
/* 122:299 */     return this.readLimit;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void setReadLimit(long readLimit)
/* 126:    */   {
/* 127:312 */     this.readLimit = readLimit;
/* 128:313 */     if (this.trafficCounter != null) {
/* 129:314 */       this.trafficCounter.resetAccounting(TrafficCounter.milliSecondFromNano());
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   public long getCheckInterval()
/* 134:    */   {
/* 135:322 */     return this.checkInterval;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public void setCheckInterval(long checkInterval)
/* 139:    */   {
/* 140:329 */     this.checkInterval = checkInterval;
/* 141:330 */     if (this.trafficCounter != null) {
/* 142:331 */       this.trafficCounter.configure(checkInterval);
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public void setMaxTimeWait(long maxTime)
/* 147:    */   {
/* 148:347 */     if (maxTime <= 0L) {
/* 149:348 */       throw new IllegalArgumentException("maxTime must be positive");
/* 150:    */     }
/* 151:350 */     this.maxTime = maxTime;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public long getMaxTimeWait()
/* 155:    */   {
/* 156:357 */     return this.maxTime;
/* 157:    */   }
/* 158:    */   
/* 159:    */   public long getMaxWriteDelay()
/* 160:    */   {
/* 161:364 */     return this.maxWriteDelay;
/* 162:    */   }
/* 163:    */   
/* 164:    */   public void setMaxWriteDelay(long maxWriteDelay)
/* 165:    */   {
/* 166:378 */     if (maxWriteDelay <= 0L) {
/* 167:379 */       throw new IllegalArgumentException("maxWriteDelay must be positive");
/* 168:    */     }
/* 169:381 */     this.maxWriteDelay = maxWriteDelay;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public long getMaxWriteSize()
/* 173:    */   {
/* 174:388 */     return this.maxWriteSize;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public void setMaxWriteSize(long maxWriteSize)
/* 178:    */   {
/* 179:404 */     this.maxWriteSize = maxWriteSize;
/* 180:    */   }
/* 181:    */   
/* 182:    */   protected void doAccounting(TrafficCounter counter) {}
/* 183:    */   
/* 184:    */   static final class ReopenReadTimerTask
/* 185:    */     implements Runnable
/* 186:    */   {
/* 187:    */     final ChannelHandlerContext ctx;
/* 188:    */     
/* 189:    */     ReopenReadTimerTask(ChannelHandlerContext ctx)
/* 190:    */     {
/* 191:424 */       this.ctx = ctx;
/* 192:    */     }
/* 193:    */     
/* 194:    */     public void run()
/* 195:    */     {
/* 196:429 */       ChannelConfig config = this.ctx.channel().config();
/* 197:430 */       if ((!config.isAutoRead()) && (AbstractTrafficShapingHandler.isHandlerActive(this.ctx)))
/* 198:    */       {
/* 199:433 */         if (AbstractTrafficShapingHandler.logger.isDebugEnabled()) {
/* 200:434 */           AbstractTrafficShapingHandler.logger.debug("Not unsuspend: " + config.isAutoRead() + ':' + 
/* 201:435 */             AbstractTrafficShapingHandler.isHandlerActive(this.ctx));
/* 202:    */         }
/* 203:437 */         this.ctx.attr(AbstractTrafficShapingHandler.READ_SUSPENDED).set(Boolean.valueOf(false));
/* 204:    */       }
/* 205:    */       else
/* 206:    */       {
/* 207:440 */         if (AbstractTrafficShapingHandler.logger.isDebugEnabled()) {
/* 208:441 */           if ((config.isAutoRead()) && (!AbstractTrafficShapingHandler.isHandlerActive(this.ctx))) {
/* 209:442 */             AbstractTrafficShapingHandler.logger.debug("Unsuspend: " + config.isAutoRead() + ':' + 
/* 210:443 */               AbstractTrafficShapingHandler.isHandlerActive(this.ctx));
/* 211:    */           } else {
/* 212:445 */             AbstractTrafficShapingHandler.logger.debug("Normal unsuspend: " + config.isAutoRead() + ':' + 
/* 213:446 */               AbstractTrafficShapingHandler.isHandlerActive(this.ctx));
/* 214:    */           }
/* 215:    */         }
/* 216:449 */         this.ctx.attr(AbstractTrafficShapingHandler.READ_SUSPENDED).set(Boolean.valueOf(false));
/* 217:450 */         config.setAutoRead(true);
/* 218:451 */         this.ctx.channel().read();
/* 219:    */       }
/* 220:453 */       if (AbstractTrafficShapingHandler.logger.isDebugEnabled()) {
/* 221:454 */         AbstractTrafficShapingHandler.logger.debug("Unsuspend final status => " + config.isAutoRead() + ':' + 
/* 222:455 */           AbstractTrafficShapingHandler.isHandlerActive(this.ctx));
/* 223:    */       }
/* 224:    */     }
/* 225:    */   }
/* 226:    */   
/* 227:    */   void releaseReadSuspended(ChannelHandlerContext ctx)
/* 228:    */   {
/* 229:464 */     ctx.attr(READ_SUSPENDED).set(Boolean.valueOf(false));
/* 230:465 */     ctx.channel().config().setAutoRead(true);
/* 231:    */   }
/* 232:    */   
/* 233:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 234:    */     throws Exception
/* 235:    */   {
/* 236:470 */     long size = calculateSize(msg);
/* 237:471 */     long now = TrafficCounter.milliSecondFromNano();
/* 238:472 */     if (size > 0L)
/* 239:    */     {
/* 240:474 */       long wait = this.trafficCounter.readTimeToWait(size, this.readLimit, this.maxTime, now);
/* 241:475 */       wait = checkWaitReadTime(ctx, wait, now);
/* 242:476 */       if (wait >= 10L)
/* 243:    */       {
/* 244:479 */         ChannelConfig config = ctx.channel().config();
/* 245:480 */         if (logger.isDebugEnabled()) {
/* 246:481 */           logger.debug("Read suspend: " + wait + ':' + config.isAutoRead() + ':' + 
/* 247:482 */             isHandlerActive(ctx));
/* 248:    */         }
/* 249:484 */         if ((config.isAutoRead()) && (isHandlerActive(ctx)))
/* 250:    */         {
/* 251:485 */           config.setAutoRead(false);
/* 252:486 */           ctx.attr(READ_SUSPENDED).set(Boolean.valueOf(true));
/* 253:    */           
/* 254:    */ 
/* 255:489 */           Attribute<Runnable> attr = ctx.attr(REOPEN_TASK);
/* 256:490 */           Runnable reopenTask = (Runnable)attr.get();
/* 257:491 */           if (reopenTask == null)
/* 258:    */           {
/* 259:492 */             reopenTask = new ReopenReadTimerTask(ctx);
/* 260:493 */             attr.set(reopenTask);
/* 261:    */           }
/* 262:495 */           ctx.executor().schedule(reopenTask, wait, TimeUnit.MILLISECONDS);
/* 263:496 */           if (logger.isDebugEnabled()) {
/* 264:497 */             logger.debug("Suspend final status => " + config.isAutoRead() + ':' + 
/* 265:498 */               isHandlerActive(ctx) + " will reopened at: " + wait);
/* 266:    */           }
/* 267:    */         }
/* 268:    */       }
/* 269:    */     }
/* 270:503 */     informReadOperation(ctx, now);
/* 271:504 */     ctx.fireChannelRead(msg);
/* 272:    */   }
/* 273:    */   
/* 274:    */   long checkWaitReadTime(ChannelHandlerContext ctx, long wait, long now)
/* 275:    */   {
/* 276:515 */     return wait;
/* 277:    */   }
/* 278:    */   
/* 279:    */   void informReadOperation(ChannelHandlerContext ctx, long now) {}
/* 280:    */   
/* 281:    */   protected static boolean isHandlerActive(ChannelHandlerContext ctx)
/* 282:    */   {
/* 283:527 */     Boolean suspended = (Boolean)ctx.attr(READ_SUSPENDED).get();
/* 284:528 */     return (suspended == null) || (Boolean.FALSE.equals(suspended));
/* 285:    */   }
/* 286:    */   
/* 287:    */   public void read(ChannelHandlerContext ctx)
/* 288:    */   {
/* 289:533 */     if (isHandlerActive(ctx)) {
/* 290:535 */       ctx.read();
/* 291:    */     }
/* 292:    */   }
/* 293:    */   
/* 294:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 295:    */     throws Exception
/* 296:    */   {
/* 297:542 */     long size = calculateSize(msg);
/* 298:543 */     long now = TrafficCounter.milliSecondFromNano();
/* 299:544 */     if (size > 0L)
/* 300:    */     {
/* 301:546 */       long wait = this.trafficCounter.writeTimeToWait(size, this.writeLimit, this.maxTime, now);
/* 302:547 */       if (wait >= 10L)
/* 303:    */       {
/* 304:548 */         if (logger.isDebugEnabled()) {
/* 305:549 */           logger.debug("Write suspend: " + wait + ':' + ctx.channel().config().isAutoRead() + ':' + 
/* 306:550 */             isHandlerActive(ctx));
/* 307:    */         }
/* 308:552 */         submitWrite(ctx, msg, size, wait, now, promise);
/* 309:553 */         return;
/* 310:    */       }
/* 311:    */     }
/* 312:557 */     submitWrite(ctx, msg, size, 0L, now, promise);
/* 313:    */   }
/* 314:    */   
/* 315:    */   @Deprecated
/* 316:    */   protected void submitWrite(ChannelHandlerContext ctx, Object msg, long delay, ChannelPromise promise)
/* 317:    */   {
/* 318:563 */     submitWrite(ctx, msg, calculateSize(msg), delay, 
/* 319:564 */       TrafficCounter.milliSecondFromNano(), promise);
/* 320:    */   }
/* 321:    */   
/* 322:    */   abstract void submitWrite(ChannelHandlerContext paramChannelHandlerContext, Object paramObject, long paramLong1, long paramLong2, long paramLong3, ChannelPromise paramChannelPromise);
/* 323:    */   
/* 324:    */   public void channelRegistered(ChannelHandlerContext ctx)
/* 325:    */     throws Exception
/* 326:    */   {
/* 327:572 */     setUserDefinedWritability(ctx, true);
/* 328:573 */     super.channelRegistered(ctx);
/* 329:    */   }
/* 330:    */   
/* 331:    */   void setUserDefinedWritability(ChannelHandlerContext ctx, boolean writable)
/* 332:    */   {
/* 333:577 */     ChannelOutboundBuffer cob = ctx.channel().unsafe().outboundBuffer();
/* 334:578 */     if (cob != null) {
/* 335:579 */       cob.setUserDefinedWritability(this.userDefinedWritabilityIndex, writable);
/* 336:    */     }
/* 337:    */   }
/* 338:    */   
/* 339:    */   void checkWriteSuspend(ChannelHandlerContext ctx, long delay, long queueSize)
/* 340:    */   {
/* 341:590 */     if ((queueSize > this.maxWriteSize) || (delay > this.maxWriteDelay)) {
/* 342:591 */       setUserDefinedWritability(ctx, false);
/* 343:    */     }
/* 344:    */   }
/* 345:    */   
/* 346:    */   void releaseWriteSuspended(ChannelHandlerContext ctx)
/* 347:    */   {
/* 348:598 */     setUserDefinedWritability(ctx, true);
/* 349:    */   }
/* 350:    */   
/* 351:    */   public TrafficCounter trafficCounter()
/* 352:    */   {
/* 353:606 */     return this.trafficCounter;
/* 354:    */   }
/* 355:    */   
/* 356:    */   public String toString()
/* 357:    */   {
/* 358:617 */     StringBuilder builder = new StringBuilder(290).append("TrafficShaping with Write Limit: ").append(this.writeLimit).append(" Read Limit: ").append(this.readLimit).append(" CheckInterval: ").append(this.checkInterval).append(" maxDelay: ").append(this.maxWriteDelay).append(" maxSize: ").append(this.maxWriteSize).append(" and Counter: ");
/* 359:618 */     if (this.trafficCounter != null) {
/* 360:619 */       builder.append(this.trafficCounter);
/* 361:    */     } else {
/* 362:621 */       builder.append("none");
/* 363:    */     }
/* 364:623 */     return builder.toString();
/* 365:    */   }
/* 366:    */   
/* 367:    */   protected long calculateSize(Object msg)
/* 368:    */   {
/* 369:634 */     if ((msg instanceof ByteBuf)) {
/* 370:635 */       return ((ByteBuf)msg).readableBytes();
/* 371:    */     }
/* 372:637 */     if ((msg instanceof ByteBufHolder)) {
/* 373:638 */       return ((ByteBufHolder)msg).content().readableBytes();
/* 374:    */     }
/* 375:640 */     return -1L;
/* 376:    */   }
/* 377:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.handler.traffic.AbstractTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */