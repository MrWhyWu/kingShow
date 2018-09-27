/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.Attribute;
/*   5:    */ import io.netty.util.AttributeKey;
/*   6:    */ import io.netty.util.concurrent.EventExecutor;
/*   7:    */ import io.netty.util.internal.ThrowableUtil;
/*   8:    */ import io.netty.util.internal.logging.InternalLogger;
/*   9:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  10:    */ import java.net.SocketAddress;
/*  11:    */ 
/*  12:    */ public class CombinedChannelDuplexHandler<I extends ChannelInboundHandler, O extends ChannelOutboundHandler>
/*  13:    */   extends ChannelDuplexHandler
/*  14:    */ {
/*  15: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(CombinedChannelDuplexHandler.class);
/*  16:    */   private DelegatingChannelHandlerContext inboundCtx;
/*  17:    */   private DelegatingChannelHandlerContext outboundCtx;
/*  18:    */   private volatile boolean handlerAdded;
/*  19:    */   private I inboundHandler;
/*  20:    */   private O outboundHandler;
/*  21:    */   
/*  22:    */   protected CombinedChannelDuplexHandler()
/*  23:    */   {
/*  24: 49 */     ensureNotSharable();
/*  25:    */   }
/*  26:    */   
/*  27:    */   public CombinedChannelDuplexHandler(I inboundHandler, O outboundHandler)
/*  28:    */   {
/*  29: 56 */     ensureNotSharable();
/*  30: 57 */     init(inboundHandler, outboundHandler);
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected final void init(I inboundHandler, O outboundHandler)
/*  34:    */   {
/*  35: 69 */     validate(inboundHandler, outboundHandler);
/*  36: 70 */     this.inboundHandler = inboundHandler;
/*  37: 71 */     this.outboundHandler = outboundHandler;
/*  38:    */   }
/*  39:    */   
/*  40:    */   private void validate(I inboundHandler, O outboundHandler)
/*  41:    */   {
/*  42: 75 */     if (this.inboundHandler != null) {
/*  43: 77 */       throw new IllegalStateException("init() can not be invoked if " + CombinedChannelDuplexHandler.class.getSimpleName() + " was constructed with non-default constructor.");
/*  44:    */     }
/*  45: 81 */     if (inboundHandler == null) {
/*  46: 82 */       throw new NullPointerException("inboundHandler");
/*  47:    */     }
/*  48: 84 */     if (outboundHandler == null) {
/*  49: 85 */       throw new NullPointerException("outboundHandler");
/*  50:    */     }
/*  51: 87 */     if ((inboundHandler instanceof ChannelOutboundHandler)) {
/*  52: 90 */       throw new IllegalArgumentException("inboundHandler must not implement " + ChannelOutboundHandler.class.getSimpleName() + " to get combined.");
/*  53:    */     }
/*  54: 92 */     if ((outboundHandler instanceof ChannelInboundHandler)) {
/*  55: 95 */       throw new IllegalArgumentException("outboundHandler must not implement " + ChannelInboundHandler.class.getSimpleName() + " to get combined.");
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   protected final I inboundHandler()
/*  60:    */   {
/*  61:100 */     return this.inboundHandler;
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected final O outboundHandler()
/*  65:    */   {
/*  66:104 */     return this.outboundHandler;
/*  67:    */   }
/*  68:    */   
/*  69:    */   private void checkAdded()
/*  70:    */   {
/*  71:108 */     if (!this.handlerAdded) {
/*  72:109 */       throw new IllegalStateException("handler not added to pipeline yet");
/*  73:    */     }
/*  74:    */   }
/*  75:    */   
/*  76:    */   public final void removeInboundHandler()
/*  77:    */   {
/*  78:117 */     checkAdded();
/*  79:118 */     this.inboundCtx.remove();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public final void removeOutboundHandler()
/*  83:    */   {
/*  84:125 */     checkAdded();
/*  85:126 */     this.outboundCtx.remove();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  89:    */     throws Exception
/*  90:    */   {
/*  91:131 */     if (this.inboundHandler == null) {
/*  92:134 */       throw new IllegalStateException("init() must be invoked before being added to a " + ChannelPipeline.class.getSimpleName() + " if " + CombinedChannelDuplexHandler.class.getSimpleName() + " was constructed with the default constructor.");
/*  93:    */     }
/*  94:138 */     this.outboundCtx = new DelegatingChannelHandlerContext(ctx, this.outboundHandler);
/*  95:139 */     this.inboundCtx = new DelegatingChannelHandlerContext(ctx, this.inboundHandler)
/*  96:    */     {
/*  97:    */       public ChannelHandlerContext fireExceptionCaught(Throwable cause)
/*  98:    */       {
/*  99:143 */         if (!CombinedChannelDuplexHandler.this.outboundCtx.removed) {
/* 100:    */           try
/* 101:    */           {
/* 102:147 */             CombinedChannelDuplexHandler.this.outboundHandler.exceptionCaught(CombinedChannelDuplexHandler.this.outboundCtx, cause);
/* 103:    */           }
/* 104:    */           catch (Throwable error)
/* 105:    */           {
/* 106:149 */             if (CombinedChannelDuplexHandler.logger.isDebugEnabled()) {
/* 107:150 */               CombinedChannelDuplexHandler.logger.debug("An exception {}was thrown by a user handler's exceptionCaught() method while handling the following exception:", 
/* 108:    */               
/* 109:    */ 
/* 110:    */ 
/* 111:154 */                 ThrowableUtil.stackTraceToString(error), cause);
/* 112:155 */             } else if (CombinedChannelDuplexHandler.logger.isWarnEnabled()) {
/* 113:156 */               CombinedChannelDuplexHandler.logger.warn("An exception '{}' [enable DEBUG level for full stacktrace] was thrown by a user handler's exceptionCaught() method while handling the following exception:", error, cause);
/* 114:    */             }
/* 115:    */           }
/* 116:    */         } else {
/* 117:163 */           super.fireExceptionCaught(cause);
/* 118:    */         }
/* 119:165 */         return this;
/* 120:    */       }
/* 121:170 */     };
/* 122:171 */     this.handlerAdded = true;
/* 123:    */     try
/* 124:    */     {
/* 125:174 */       this.inboundHandler.handlerAdded(this.inboundCtx);
/* 126:    */       
/* 127:176 */       this.outboundHandler.handlerAdded(this.outboundCtx);
/* 128:    */     }
/* 129:    */     finally
/* 130:    */     {
/* 131:176 */       this.outboundHandler.handlerAdded(this.outboundCtx);
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/* 136:    */     throws Exception
/* 137:    */   {
/* 138:    */     try
/* 139:    */     {
/* 140:183 */       this.inboundCtx.remove();
/* 141:    */       
/* 142:185 */       this.outboundCtx.remove();
/* 143:    */     }
/* 144:    */     finally
/* 145:    */     {
/* 146:185 */       this.outboundCtx.remove();
/* 147:    */     }
/* 148:    */   }
/* 149:    */   
/* 150:    */   public void channelRegistered(ChannelHandlerContext ctx)
/* 151:    */     throws Exception
/* 152:    */   {
/* 153:191 */     assert (ctx == this.inboundCtx.ctx);
/* 154:192 */     if (!this.inboundCtx.removed) {
/* 155:193 */       this.inboundHandler.channelRegistered(this.inboundCtx);
/* 156:    */     } else {
/* 157:195 */       this.inboundCtx.fireChannelRegistered();
/* 158:    */     }
/* 159:    */   }
/* 160:    */   
/* 161:    */   public void channelUnregistered(ChannelHandlerContext ctx)
/* 162:    */     throws Exception
/* 163:    */   {
/* 164:201 */     assert (ctx == this.inboundCtx.ctx);
/* 165:202 */     if (!this.inboundCtx.removed) {
/* 166:203 */       this.inboundHandler.channelUnregistered(this.inboundCtx);
/* 167:    */     } else {
/* 168:205 */       this.inboundCtx.fireChannelUnregistered();
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   public void channelActive(ChannelHandlerContext ctx)
/* 173:    */     throws Exception
/* 174:    */   {
/* 175:211 */     assert (ctx == this.inboundCtx.ctx);
/* 176:212 */     if (!this.inboundCtx.removed) {
/* 177:213 */       this.inboundHandler.channelActive(this.inboundCtx);
/* 178:    */     } else {
/* 179:215 */       this.inboundCtx.fireChannelActive();
/* 180:    */     }
/* 181:    */   }
/* 182:    */   
/* 183:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 184:    */     throws Exception
/* 185:    */   {
/* 186:221 */     assert (ctx == this.inboundCtx.ctx);
/* 187:222 */     if (!this.inboundCtx.removed) {
/* 188:223 */       this.inboundHandler.channelInactive(this.inboundCtx);
/* 189:    */     } else {
/* 190:225 */       this.inboundCtx.fireChannelInactive();
/* 191:    */     }
/* 192:    */   }
/* 193:    */   
/* 194:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 195:    */     throws Exception
/* 196:    */   {
/* 197:231 */     assert (ctx == this.inboundCtx.ctx);
/* 198:232 */     if (!this.inboundCtx.removed) {
/* 199:233 */       this.inboundHandler.exceptionCaught(this.inboundCtx, cause);
/* 200:    */     } else {
/* 201:235 */       this.inboundCtx.fireExceptionCaught(cause);
/* 202:    */     }
/* 203:    */   }
/* 204:    */   
/* 205:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 206:    */     throws Exception
/* 207:    */   {
/* 208:241 */     assert (ctx == this.inboundCtx.ctx);
/* 209:242 */     if (!this.inboundCtx.removed) {
/* 210:243 */       this.inboundHandler.userEventTriggered(this.inboundCtx, evt);
/* 211:    */     } else {
/* 212:245 */       this.inboundCtx.fireUserEventTriggered(evt);
/* 213:    */     }
/* 214:    */   }
/* 215:    */   
/* 216:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 217:    */     throws Exception
/* 218:    */   {
/* 219:251 */     assert (ctx == this.inboundCtx.ctx);
/* 220:252 */     if (!this.inboundCtx.removed) {
/* 221:253 */       this.inboundHandler.channelRead(this.inboundCtx, msg);
/* 222:    */     } else {
/* 223:255 */       this.inboundCtx.fireChannelRead(msg);
/* 224:    */     }
/* 225:    */   }
/* 226:    */   
/* 227:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 228:    */     throws Exception
/* 229:    */   {
/* 230:261 */     assert (ctx == this.inboundCtx.ctx);
/* 231:262 */     if (!this.inboundCtx.removed) {
/* 232:263 */       this.inboundHandler.channelReadComplete(this.inboundCtx);
/* 233:    */     } else {
/* 234:265 */       this.inboundCtx.fireChannelReadComplete();
/* 235:    */     }
/* 236:    */   }
/* 237:    */   
/* 238:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 239:    */     throws Exception
/* 240:    */   {
/* 241:271 */     assert (ctx == this.inboundCtx.ctx);
/* 242:272 */     if (!this.inboundCtx.removed) {
/* 243:273 */       this.inboundHandler.channelWritabilityChanged(this.inboundCtx);
/* 244:    */     } else {
/* 245:275 */       this.inboundCtx.fireChannelWritabilityChanged();
/* 246:    */     }
/* 247:    */   }
/* 248:    */   
/* 249:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/* 250:    */     throws Exception
/* 251:    */   {
/* 252:283 */     assert (ctx == this.outboundCtx.ctx);
/* 253:284 */     if (!this.outboundCtx.removed) {
/* 254:285 */       this.outboundHandler.bind(this.outboundCtx, localAddress, promise);
/* 255:    */     } else {
/* 256:287 */       this.outboundCtx.bind(localAddress, promise);
/* 257:    */     }
/* 258:    */   }
/* 259:    */   
/* 260:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 261:    */     throws Exception
/* 262:    */   {
/* 263:296 */     assert (ctx == this.outboundCtx.ctx);
/* 264:297 */     if (!this.outboundCtx.removed) {
/* 265:298 */       this.outboundHandler.connect(this.outboundCtx, remoteAddress, localAddress, promise);
/* 266:    */     } else {
/* 267:300 */       this.outboundCtx.connect(localAddress, promise);
/* 268:    */     }
/* 269:    */   }
/* 270:    */   
/* 271:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 272:    */     throws Exception
/* 273:    */   {
/* 274:306 */     assert (ctx == this.outboundCtx.ctx);
/* 275:307 */     if (!this.outboundCtx.removed) {
/* 276:308 */       this.outboundHandler.disconnect(this.outboundCtx, promise);
/* 277:    */     } else {
/* 278:310 */       this.outboundCtx.disconnect(promise);
/* 279:    */     }
/* 280:    */   }
/* 281:    */   
/* 282:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 283:    */     throws Exception
/* 284:    */   {
/* 285:316 */     assert (ctx == this.outboundCtx.ctx);
/* 286:317 */     if (!this.outboundCtx.removed) {
/* 287:318 */       this.outboundHandler.close(this.outboundCtx, promise);
/* 288:    */     } else {
/* 289:320 */       this.outboundCtx.close(promise);
/* 290:    */     }
/* 291:    */   }
/* 292:    */   
/* 293:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 294:    */     throws Exception
/* 295:    */   {
/* 296:326 */     assert (ctx == this.outboundCtx.ctx);
/* 297:327 */     if (!this.outboundCtx.removed) {
/* 298:328 */       this.outboundHandler.deregister(this.outboundCtx, promise);
/* 299:    */     } else {
/* 300:330 */       this.outboundCtx.deregister(promise);
/* 301:    */     }
/* 302:    */   }
/* 303:    */   
/* 304:    */   public void read(ChannelHandlerContext ctx)
/* 305:    */     throws Exception
/* 306:    */   {
/* 307:336 */     assert (ctx == this.outboundCtx.ctx);
/* 308:337 */     if (!this.outboundCtx.removed) {
/* 309:338 */       this.outboundHandler.read(this.outboundCtx);
/* 310:    */     } else {
/* 311:340 */       this.outboundCtx.read();
/* 312:    */     }
/* 313:    */   }
/* 314:    */   
/* 315:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 316:    */     throws Exception
/* 317:    */   {
/* 318:346 */     assert (ctx == this.outboundCtx.ctx);
/* 319:347 */     if (!this.outboundCtx.removed) {
/* 320:348 */       this.outboundHandler.write(this.outboundCtx, msg, promise);
/* 321:    */     } else {
/* 322:350 */       this.outboundCtx.write(msg, promise);
/* 323:    */     }
/* 324:    */   }
/* 325:    */   
/* 326:    */   public void flush(ChannelHandlerContext ctx)
/* 327:    */     throws Exception
/* 328:    */   {
/* 329:356 */     assert (ctx == this.outboundCtx.ctx);
/* 330:357 */     if (!this.outboundCtx.removed) {
/* 331:358 */       this.outboundHandler.flush(this.outboundCtx);
/* 332:    */     } else {
/* 333:360 */       this.outboundCtx.flush();
/* 334:    */     }
/* 335:    */   }
/* 336:    */   
/* 337:    */   private static class DelegatingChannelHandlerContext
/* 338:    */     implements ChannelHandlerContext
/* 339:    */   {
/* 340:    */     private final ChannelHandlerContext ctx;
/* 341:    */     private final ChannelHandler handler;
/* 342:    */     boolean removed;
/* 343:    */     
/* 344:    */     DelegatingChannelHandlerContext(ChannelHandlerContext ctx, ChannelHandler handler)
/* 345:    */     {
/* 346:371 */       this.ctx = ctx;
/* 347:372 */       this.handler = handler;
/* 348:    */     }
/* 349:    */     
/* 350:    */     public Channel channel()
/* 351:    */     {
/* 352:377 */       return this.ctx.channel();
/* 353:    */     }
/* 354:    */     
/* 355:    */     public EventExecutor executor()
/* 356:    */     {
/* 357:382 */       return this.ctx.executor();
/* 358:    */     }
/* 359:    */     
/* 360:    */     public String name()
/* 361:    */     {
/* 362:387 */       return this.ctx.name();
/* 363:    */     }
/* 364:    */     
/* 365:    */     public ChannelHandler handler()
/* 366:    */     {
/* 367:392 */       return this.ctx.handler();
/* 368:    */     }
/* 369:    */     
/* 370:    */     public boolean isRemoved()
/* 371:    */     {
/* 372:397 */       return (this.removed) || (this.ctx.isRemoved());
/* 373:    */     }
/* 374:    */     
/* 375:    */     public ChannelHandlerContext fireChannelRegistered()
/* 376:    */     {
/* 377:402 */       this.ctx.fireChannelRegistered();
/* 378:403 */       return this;
/* 379:    */     }
/* 380:    */     
/* 381:    */     public ChannelHandlerContext fireChannelUnregistered()
/* 382:    */     {
/* 383:408 */       this.ctx.fireChannelUnregistered();
/* 384:409 */       return this;
/* 385:    */     }
/* 386:    */     
/* 387:    */     public ChannelHandlerContext fireChannelActive()
/* 388:    */     {
/* 389:414 */       this.ctx.fireChannelActive();
/* 390:415 */       return this;
/* 391:    */     }
/* 392:    */     
/* 393:    */     public ChannelHandlerContext fireChannelInactive()
/* 394:    */     {
/* 395:420 */       this.ctx.fireChannelInactive();
/* 396:421 */       return this;
/* 397:    */     }
/* 398:    */     
/* 399:    */     public ChannelHandlerContext fireExceptionCaught(Throwable cause)
/* 400:    */     {
/* 401:426 */       this.ctx.fireExceptionCaught(cause);
/* 402:427 */       return this;
/* 403:    */     }
/* 404:    */     
/* 405:    */     public ChannelHandlerContext fireUserEventTriggered(Object event)
/* 406:    */     {
/* 407:432 */       this.ctx.fireUserEventTriggered(event);
/* 408:433 */       return this;
/* 409:    */     }
/* 410:    */     
/* 411:    */     public ChannelHandlerContext fireChannelRead(Object msg)
/* 412:    */     {
/* 413:438 */       this.ctx.fireChannelRead(msg);
/* 414:439 */       return this;
/* 415:    */     }
/* 416:    */     
/* 417:    */     public ChannelHandlerContext fireChannelReadComplete()
/* 418:    */     {
/* 419:444 */       this.ctx.fireChannelReadComplete();
/* 420:445 */       return this;
/* 421:    */     }
/* 422:    */     
/* 423:    */     public ChannelHandlerContext fireChannelWritabilityChanged()
/* 424:    */     {
/* 425:450 */       this.ctx.fireChannelWritabilityChanged();
/* 426:451 */       return this;
/* 427:    */     }
/* 428:    */     
/* 429:    */     public ChannelFuture bind(SocketAddress localAddress)
/* 430:    */     {
/* 431:456 */       return this.ctx.bind(localAddress);
/* 432:    */     }
/* 433:    */     
/* 434:    */     public ChannelFuture connect(SocketAddress remoteAddress)
/* 435:    */     {
/* 436:461 */       return this.ctx.connect(remoteAddress);
/* 437:    */     }
/* 438:    */     
/* 439:    */     public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 440:    */     {
/* 441:466 */       return this.ctx.connect(remoteAddress, localAddress);
/* 442:    */     }
/* 443:    */     
/* 444:    */     public ChannelFuture disconnect()
/* 445:    */     {
/* 446:471 */       return this.ctx.disconnect();
/* 447:    */     }
/* 448:    */     
/* 449:    */     public ChannelFuture close()
/* 450:    */     {
/* 451:476 */       return this.ctx.close();
/* 452:    */     }
/* 453:    */     
/* 454:    */     public ChannelFuture deregister()
/* 455:    */     {
/* 456:481 */       return this.ctx.deregister();
/* 457:    */     }
/* 458:    */     
/* 459:    */     public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise)
/* 460:    */     {
/* 461:486 */       return this.ctx.bind(localAddress, promise);
/* 462:    */     }
/* 463:    */     
/* 464:    */     public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise)
/* 465:    */     {
/* 466:491 */       return this.ctx.connect(remoteAddress, promise);
/* 467:    */     }
/* 468:    */     
/* 469:    */     public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 470:    */     {
/* 471:497 */       return this.ctx.connect(remoteAddress, localAddress, promise);
/* 472:    */     }
/* 473:    */     
/* 474:    */     public ChannelFuture disconnect(ChannelPromise promise)
/* 475:    */     {
/* 476:502 */       return this.ctx.disconnect(promise);
/* 477:    */     }
/* 478:    */     
/* 479:    */     public ChannelFuture close(ChannelPromise promise)
/* 480:    */     {
/* 481:507 */       return this.ctx.close(promise);
/* 482:    */     }
/* 483:    */     
/* 484:    */     public ChannelFuture deregister(ChannelPromise promise)
/* 485:    */     {
/* 486:512 */       return this.ctx.deregister(promise);
/* 487:    */     }
/* 488:    */     
/* 489:    */     public ChannelHandlerContext read()
/* 490:    */     {
/* 491:517 */       this.ctx.read();
/* 492:518 */       return this;
/* 493:    */     }
/* 494:    */     
/* 495:    */     public ChannelFuture write(Object msg)
/* 496:    */     {
/* 497:523 */       return this.ctx.write(msg);
/* 498:    */     }
/* 499:    */     
/* 500:    */     public ChannelFuture write(Object msg, ChannelPromise promise)
/* 501:    */     {
/* 502:528 */       return this.ctx.write(msg, promise);
/* 503:    */     }
/* 504:    */     
/* 505:    */     public ChannelHandlerContext flush()
/* 506:    */     {
/* 507:533 */       this.ctx.flush();
/* 508:534 */       return this;
/* 509:    */     }
/* 510:    */     
/* 511:    */     public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise)
/* 512:    */     {
/* 513:539 */       return this.ctx.writeAndFlush(msg, promise);
/* 514:    */     }
/* 515:    */     
/* 516:    */     public ChannelFuture writeAndFlush(Object msg)
/* 517:    */     {
/* 518:544 */       return this.ctx.writeAndFlush(msg);
/* 519:    */     }
/* 520:    */     
/* 521:    */     public ChannelPipeline pipeline()
/* 522:    */     {
/* 523:549 */       return this.ctx.pipeline();
/* 524:    */     }
/* 525:    */     
/* 526:    */     public ByteBufAllocator alloc()
/* 527:    */     {
/* 528:554 */       return this.ctx.alloc();
/* 529:    */     }
/* 530:    */     
/* 531:    */     public ChannelPromise newPromise()
/* 532:    */     {
/* 533:559 */       return this.ctx.newPromise();
/* 534:    */     }
/* 535:    */     
/* 536:    */     public ChannelProgressivePromise newProgressivePromise()
/* 537:    */     {
/* 538:564 */       return this.ctx.newProgressivePromise();
/* 539:    */     }
/* 540:    */     
/* 541:    */     public ChannelFuture newSucceededFuture()
/* 542:    */     {
/* 543:569 */       return this.ctx.newSucceededFuture();
/* 544:    */     }
/* 545:    */     
/* 546:    */     public ChannelFuture newFailedFuture(Throwable cause)
/* 547:    */     {
/* 548:574 */       return this.ctx.newFailedFuture(cause);
/* 549:    */     }
/* 550:    */     
/* 551:    */     public ChannelPromise voidPromise()
/* 552:    */     {
/* 553:579 */       return this.ctx.voidPromise();
/* 554:    */     }
/* 555:    */     
/* 556:    */     public <T> Attribute<T> attr(AttributeKey<T> key)
/* 557:    */     {
/* 558:584 */       return this.ctx.attr(key);
/* 559:    */     }
/* 560:    */     
/* 561:    */     public <T> boolean hasAttr(AttributeKey<T> key)
/* 562:    */     {
/* 563:589 */       return this.ctx.hasAttr(key);
/* 564:    */     }
/* 565:    */     
/* 566:    */     final void remove()
/* 567:    */     {
/* 568:593 */       EventExecutor executor = executor();
/* 569:594 */       if (executor.inEventLoop()) {
/* 570:595 */         remove0();
/* 571:    */       } else {
/* 572:597 */         executor.execute(new Runnable()
/* 573:    */         {
/* 574:    */           public void run()
/* 575:    */           {
/* 576:600 */             CombinedChannelDuplexHandler.DelegatingChannelHandlerContext.this.remove0();
/* 577:    */           }
/* 578:    */         });
/* 579:    */       }
/* 580:    */     }
/* 581:    */     
/* 582:    */     private void remove0()
/* 583:    */     {
/* 584:607 */       if (!this.removed)
/* 585:    */       {
/* 586:608 */         this.removed = true;
/* 587:    */         try
/* 588:    */         {
/* 589:610 */           this.handler.handlerRemoved(this);
/* 590:    */         }
/* 591:    */         catch (Throwable cause)
/* 592:    */         {
/* 593:612 */           fireExceptionCaught(new ChannelPipelineException(this.handler
/* 594:613 */             .getClass().getName() + ".handlerRemoved() has thrown an exception.", cause));
/* 595:    */         }
/* 596:    */       }
/* 597:    */     }
/* 598:    */   }
/* 599:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.CombinedChannelDuplexHandler
 * JD-Core Version:    0.7.0.1
 */