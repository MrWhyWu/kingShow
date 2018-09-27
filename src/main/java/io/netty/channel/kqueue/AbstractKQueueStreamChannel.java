/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   6:    */ import io.netty.channel.Channel;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelFutureListener;
/*  10:    */ import io.netty.channel.ChannelMetadata;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.ChannelPipeline;
/*  13:    */ import io.netty.channel.ChannelPromise;
/*  14:    */ import io.netty.channel.DefaultFileRegion;
/*  15:    */ import io.netty.channel.EventLoop;
/*  16:    */ import io.netty.channel.FileRegion;
/*  17:    */ import io.netty.channel.socket.DuplexChannel;
/*  18:    */ import io.netty.channel.unix.IovArray;
/*  19:    */ import io.netty.channel.unix.SocketWritableByteChannel;
/*  20:    */ import io.netty.channel.unix.UnixChannelUtil;
/*  21:    */ import io.netty.util.internal.PlatformDependent;
/*  22:    */ import io.netty.util.internal.StringUtil;
/*  23:    */ import io.netty.util.internal.logging.InternalLogger;
/*  24:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  25:    */ import java.io.IOException;
/*  26:    */ import java.net.SocketAddress;
/*  27:    */ import java.nio.ByteBuffer;
/*  28:    */ import java.nio.channels.WritableByteChannel;
/*  29:    */ import java.util.concurrent.Executor;
/*  30:    */ 
/*  31:    */ public abstract class AbstractKQueueStreamChannel
/*  32:    */   extends AbstractKQueueChannel
/*  33:    */   implements DuplexChannel
/*  34:    */ {
/*  35: 53 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractKQueueStreamChannel.class);
/*  36: 54 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
/*  37: 55 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  38: 56 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  39: 57 */     StringUtil.simpleClassName(DefaultFileRegion.class) + ')';
/*  40:    */   private WritableByteChannel byteChannel;
/*  41: 59 */   private final Runnable flushTask = new Runnable()
/*  42:    */   {
/*  43:    */     public void run()
/*  44:    */     {
/*  45: 62 */       AbstractKQueueStreamChannel.this.flush();
/*  46:    */     }
/*  47:    */   };
/*  48:    */   
/*  49:    */   AbstractKQueueStreamChannel(Channel parent, BsdSocket fd, boolean active)
/*  50:    */   {
/*  51: 67 */     super(parent, fd, active, true);
/*  52:    */   }
/*  53:    */   
/*  54:    */   AbstractKQueueStreamChannel(Channel parent, BsdSocket fd, SocketAddress remote)
/*  55:    */   {
/*  56: 71 */     super(parent, fd, remote);
/*  57:    */   }
/*  58:    */   
/*  59:    */   AbstractKQueueStreamChannel(BsdSocket fd)
/*  60:    */   {
/*  61: 75 */     this(null, fd, isSoErrorZero(fd));
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe()
/*  65:    */   {
/*  66: 80 */     return new KQueueStreamUnsafe();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ChannelMetadata metadata()
/*  70:    */   {
/*  71: 85 */     return METADATA;
/*  72:    */   }
/*  73:    */   
/*  74:    */   private int writeBytes(ChannelOutboundBuffer in, ByteBuf buf)
/*  75:    */     throws Exception
/*  76:    */   {
/*  77:103 */     int readableBytes = buf.readableBytes();
/*  78:104 */     if (readableBytes == 0)
/*  79:    */     {
/*  80:105 */       in.remove();
/*  81:106 */       return 0;
/*  82:    */     }
/*  83:109 */     if ((buf.hasMemoryAddress()) || (buf.nioBufferCount() == 1)) {
/*  84:110 */       return doWriteBytes(in, buf);
/*  85:    */     }
/*  86:112 */     ByteBuffer[] nioBuffers = buf.nioBuffers();
/*  87:113 */     return writeBytesMultiple(in, nioBuffers, nioBuffers.length, readableBytes, 
/*  88:114 */       config().getMaxBytesPerGatheringWrite());
/*  89:    */   }
/*  90:    */   
/*  91:    */   private void adjustMaxBytesPerGatheringWrite(long attempted, long written, long oldMaxBytesPerGatheringWrite)
/*  92:    */   {
/*  93:122 */     if (attempted == written)
/*  94:    */     {
/*  95:123 */       if (attempted << 1 > oldMaxBytesPerGatheringWrite) {
/*  96:124 */         config().setMaxBytesPerGatheringWrite(attempted << 1);
/*  97:    */       }
/*  98:    */     }
/*  99:126 */     else if ((attempted > 4096L) && (written < attempted >>> 1)) {
/* 100:127 */       config().setMaxBytesPerGatheringWrite(attempted >>> 1);
/* 101:    */     }
/* 102:    */   }
/* 103:    */   
/* 104:    */   private int writeBytesMultiple(ChannelOutboundBuffer in, IovArray array)
/* 105:    */     throws IOException
/* 106:    */   {
/* 107:147 */     long expectedWrittenBytes = array.size();
/* 108:148 */     assert (expectedWrittenBytes != 0L);
/* 109:149 */     int cnt = array.count();
/* 110:150 */     assert (cnt != 0);
/* 111:    */     
/* 112:152 */     long localWrittenBytes = this.socket.writevAddresses(array.memoryAddress(0), cnt);
/* 113:153 */     if (localWrittenBytes > 0L)
/* 114:    */     {
/* 115:154 */       adjustMaxBytesPerGatheringWrite(expectedWrittenBytes, localWrittenBytes, array.maxBytes());
/* 116:155 */       in.removeBytes(localWrittenBytes);
/* 117:156 */       return 1;
/* 118:    */     }
/* 119:158 */     return 2147483647;
/* 120:    */   }
/* 121:    */   
/* 122:    */   private int writeBytesMultiple(ChannelOutboundBuffer in, ByteBuffer[] nioBuffers, int nioBufferCnt, long expectedWrittenBytes, long maxBytesPerGatheringWrite)
/* 123:    */     throws IOException
/* 124:    */   {
/* 125:182 */     assert (expectedWrittenBytes != 0L);
/* 126:183 */     if (expectedWrittenBytes > maxBytesPerGatheringWrite) {
/* 127:184 */       expectedWrittenBytes = maxBytesPerGatheringWrite;
/* 128:    */     }
/* 129:187 */     long localWrittenBytes = this.socket.writev(nioBuffers, 0, nioBufferCnt, expectedWrittenBytes);
/* 130:188 */     if (localWrittenBytes > 0L)
/* 131:    */     {
/* 132:189 */       adjustMaxBytesPerGatheringWrite(expectedWrittenBytes, localWrittenBytes, maxBytesPerGatheringWrite);
/* 133:190 */       in.removeBytes(localWrittenBytes);
/* 134:191 */       return 1;
/* 135:    */     }
/* 136:193 */     return 2147483647;
/* 137:    */   }
/* 138:    */   
/* 139:    */   private int writeDefaultFileRegion(ChannelOutboundBuffer in, DefaultFileRegion region)
/* 140:    */     throws Exception
/* 141:    */   {
/* 142:211 */     long regionCount = region.count();
/* 143:212 */     if (region.transferred() >= regionCount)
/* 144:    */     {
/* 145:213 */       in.remove();
/* 146:214 */       return 0;
/* 147:    */     }
/* 148:217 */     long offset = region.transferred();
/* 149:218 */     long flushedAmount = this.socket.sendFile(region, region.position(), offset, regionCount - offset);
/* 150:219 */     if (flushedAmount > 0L)
/* 151:    */     {
/* 152:220 */       in.progress(flushedAmount);
/* 153:221 */       if (region.transferred() >= regionCount) {
/* 154:222 */         in.remove();
/* 155:    */       }
/* 156:224 */       return 1;
/* 157:    */     }
/* 158:226 */     return 2147483647;
/* 159:    */   }
/* 160:    */   
/* 161:    */   private int writeFileRegion(ChannelOutboundBuffer in, FileRegion region)
/* 162:    */     throws Exception
/* 163:    */   {
/* 164:244 */     if (region.transferred() >= region.count())
/* 165:    */     {
/* 166:245 */       in.remove();
/* 167:246 */       return 0;
/* 168:    */     }
/* 169:249 */     if (this.byteChannel == null) {
/* 170:250 */       this.byteChannel = new KQueueSocketWritableByteChannel();
/* 171:    */     }
/* 172:252 */     long flushedAmount = region.transferTo(this.byteChannel, region.transferred());
/* 173:253 */     if (flushedAmount > 0L)
/* 174:    */     {
/* 175:254 */       in.progress(flushedAmount);
/* 176:255 */       if (region.transferred() >= region.count()) {
/* 177:256 */         in.remove();
/* 178:    */       }
/* 179:258 */       return 1;
/* 180:    */     }
/* 181:260 */     return 2147483647;
/* 182:    */   }
/* 183:    */   
/* 184:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 185:    */     throws Exception
/* 186:    */   {
/* 187:265 */     int writeSpinCount = config().getWriteSpinCount();
/* 188:    */     do
/* 189:    */     {
/* 190:267 */       int msgCount = in.size();
/* 191:269 */       if ((msgCount > 1) && ((in.current() instanceof ByteBuf)))
/* 192:    */       {
/* 193:270 */         writeSpinCount -= doWriteMultiple(in);
/* 194:    */       }
/* 195:    */       else
/* 196:    */       {
/* 197:271 */         if (msgCount == 0)
/* 198:    */         {
/* 199:273 */           writeFilter(false);
/* 200:    */           
/* 201:275 */           return;
/* 202:    */         }
/* 203:277 */         writeSpinCount -= doWriteSingle(in);
/* 204:    */       }
/* 205:283 */     } while (writeSpinCount > 0);
/* 206:285 */     if (writeSpinCount == 0) {
/* 207:287 */       eventLoop().execute(this.flushTask);
/* 208:    */     } else {
/* 209:291 */       writeFilter(true);
/* 210:    */     }
/* 211:    */   }
/* 212:    */   
/* 213:    */   protected int doWriteSingle(ChannelOutboundBuffer in)
/* 214:    */     throws Exception
/* 215:    */   {
/* 216:311 */     Object msg = in.current();
/* 217:312 */     if ((msg instanceof ByteBuf)) {
/* 218:313 */       return writeBytes(in, (ByteBuf)msg);
/* 219:    */     }
/* 220:314 */     if ((msg instanceof DefaultFileRegion)) {
/* 221:315 */       return writeDefaultFileRegion(in, (DefaultFileRegion)msg);
/* 222:    */     }
/* 223:316 */     if ((msg instanceof FileRegion)) {
/* 224:317 */       return writeFileRegion(in, (FileRegion)msg);
/* 225:    */     }
/* 226:320 */     throw new Error();
/* 227:    */   }
/* 228:    */   
/* 229:    */   private int doWriteMultiple(ChannelOutboundBuffer in)
/* 230:    */     throws Exception
/* 231:    */   {
/* 232:339 */     long maxBytesPerGatheringWrite = config().getMaxBytesPerGatheringWrite();
/* 233:340 */     if (PlatformDependent.hasUnsafe())
/* 234:    */     {
/* 235:341 */       IovArray array = ((KQueueEventLoop)eventLoop()).cleanArray();
/* 236:342 */       array.maxBytes(maxBytesPerGatheringWrite);
/* 237:343 */       in.forEachFlushedMessage(array);
/* 238:345 */       if (array.count() >= 1) {
/* 239:347 */         return writeBytesMultiple(in, array);
/* 240:    */       }
/* 241:    */     }
/* 242:    */     else
/* 243:    */     {
/* 244:350 */       ByteBuffer[] buffers = in.nioBuffers();
/* 245:351 */       int cnt = in.nioBufferCount();
/* 246:352 */       if (cnt >= 1) {
/* 247:354 */         return writeBytesMultiple(in, buffers, cnt, in.nioBufferSize(), maxBytesPerGatheringWrite);
/* 248:    */       }
/* 249:    */     }
/* 250:358 */     in.removeBytes(0L);
/* 251:359 */     return 0;
/* 252:    */   }
/* 253:    */   
/* 254:    */   protected Object filterOutboundMessage(Object msg)
/* 255:    */   {
/* 256:364 */     if ((msg instanceof ByteBuf))
/* 257:    */     {
/* 258:365 */       ByteBuf buf = (ByteBuf)msg;
/* 259:366 */       return UnixChannelUtil.isBufferCopyNeededForWrite(buf) ? newDirectBuffer(buf) : buf;
/* 260:    */     }
/* 261:369 */     if ((msg instanceof FileRegion)) {
/* 262:370 */       return msg;
/* 263:    */     }
/* 264:374 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 265:    */   }
/* 266:    */   
/* 267:    */   protected final void doShutdownOutput()
/* 268:    */     throws Exception
/* 269:    */   {
/* 270:380 */     this.socket.shutdown(false, true);
/* 271:    */   }
/* 272:    */   
/* 273:    */   public boolean isOutputShutdown()
/* 274:    */   {
/* 275:385 */     return this.socket.isOutputShutdown();
/* 276:    */   }
/* 277:    */   
/* 278:    */   public boolean isInputShutdown()
/* 279:    */   {
/* 280:390 */     return this.socket.isInputShutdown();
/* 281:    */   }
/* 282:    */   
/* 283:    */   public boolean isShutdown()
/* 284:    */   {
/* 285:395 */     return this.socket.isShutdown();
/* 286:    */   }
/* 287:    */   
/* 288:    */   public ChannelFuture shutdownOutput()
/* 289:    */   {
/* 290:400 */     return shutdownOutput(newPromise());
/* 291:    */   }
/* 292:    */   
/* 293:    */   public ChannelFuture shutdownOutput(final ChannelPromise promise)
/* 294:    */   {
/* 295:405 */     EventLoop loop = eventLoop();
/* 296:406 */     if (loop.inEventLoop()) {
/* 297:407 */       ((AbstractChannel.AbstractUnsafe)unsafe()).shutdownOutput(promise);
/* 298:    */     } else {
/* 299:409 */       loop.execute(new Runnable()
/* 300:    */       {
/* 301:    */         public void run()
/* 302:    */         {
/* 303:412 */           ((AbstractChannel.AbstractUnsafe)AbstractKQueueStreamChannel.this.unsafe()).shutdownOutput(promise);
/* 304:    */         }
/* 305:    */       });
/* 306:    */     }
/* 307:416 */     return promise;
/* 308:    */   }
/* 309:    */   
/* 310:    */   public ChannelFuture shutdownInput()
/* 311:    */   {
/* 312:421 */     return shutdownInput(newPromise());
/* 313:    */   }
/* 314:    */   
/* 315:    */   public ChannelFuture shutdownInput(final ChannelPromise promise)
/* 316:    */   {
/* 317:426 */     EventLoop loop = eventLoop();
/* 318:427 */     if (loop.inEventLoop()) {
/* 319:428 */       shutdownInput0(promise);
/* 320:    */     } else {
/* 321:430 */       loop.execute(new Runnable()
/* 322:    */       {
/* 323:    */         public void run()
/* 324:    */         {
/* 325:433 */           AbstractKQueueStreamChannel.this.shutdownInput0(promise);
/* 326:    */         }
/* 327:    */       });
/* 328:    */     }
/* 329:437 */     return promise;
/* 330:    */   }
/* 331:    */   
/* 332:    */   private void shutdownInput0(ChannelPromise promise)
/* 333:    */   {
/* 334:    */     try
/* 335:    */     {
/* 336:442 */       this.socket.shutdown(true, false);
/* 337:    */     }
/* 338:    */     catch (Throwable cause)
/* 339:    */     {
/* 340:444 */       promise.setFailure(cause);
/* 341:445 */       return;
/* 342:    */     }
/* 343:447 */     promise.setSuccess();
/* 344:    */   }
/* 345:    */   
/* 346:    */   public ChannelFuture shutdown()
/* 347:    */   {
/* 348:452 */     return shutdown(newPromise());
/* 349:    */   }
/* 350:    */   
/* 351:    */   public ChannelFuture shutdown(final ChannelPromise promise)
/* 352:    */   {
/* 353:457 */     ChannelFuture shutdownOutputFuture = shutdownOutput();
/* 354:458 */     if (shutdownOutputFuture.isDone()) {
/* 355:459 */       shutdownOutputDone(shutdownOutputFuture, promise);
/* 356:    */     } else {
/* 357:461 */       shutdownOutputFuture.addListener(new ChannelFutureListener()
/* 358:    */       {
/* 359:    */         public void operationComplete(ChannelFuture shutdownOutputFuture)
/* 360:    */           throws Exception
/* 361:    */         {
/* 362:464 */           AbstractKQueueStreamChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
/* 363:    */         }
/* 364:    */       });
/* 365:    */     }
/* 366:468 */     return promise;
/* 367:    */   }
/* 368:    */   
/* 369:    */   private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture, final ChannelPromise promise)
/* 370:    */   {
/* 371:472 */     ChannelFuture shutdownInputFuture = shutdownInput();
/* 372:473 */     if (shutdownInputFuture.isDone()) {
/* 373:474 */       shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/* 374:    */     } else {
/* 375:476 */       shutdownInputFuture.addListener(new ChannelFutureListener()
/* 376:    */       {
/* 377:    */         public void operationComplete(ChannelFuture shutdownInputFuture)
/* 378:    */           throws Exception
/* 379:    */         {
/* 380:479 */           AbstractKQueueStreamChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/* 381:    */         }
/* 382:    */       });
/* 383:    */     }
/* 384:    */   }
/* 385:    */   
/* 386:    */   private static void shutdownDone(ChannelFuture shutdownOutputFuture, ChannelFuture shutdownInputFuture, ChannelPromise promise)
/* 387:    */   {
/* 388:488 */     Throwable shutdownOutputCause = shutdownOutputFuture.cause();
/* 389:489 */     Throwable shutdownInputCause = shutdownInputFuture.cause();
/* 390:490 */     if (shutdownOutputCause != null)
/* 391:    */     {
/* 392:491 */       if (shutdownInputCause != null) {
/* 393:492 */         logger.debug("Exception suppressed because a previous exception occurred.", shutdownInputCause);
/* 394:    */       }
/* 395:495 */       promise.setFailure(shutdownOutputCause);
/* 396:    */     }
/* 397:496 */     else if (shutdownInputCause != null)
/* 398:    */     {
/* 399:497 */       promise.setFailure(shutdownInputCause);
/* 400:    */     }
/* 401:    */     else
/* 402:    */     {
/* 403:499 */       promise.setSuccess();
/* 404:    */     }
/* 405:    */   }
/* 406:    */   
/* 407:    */   class KQueueStreamUnsafe
/* 408:    */     extends AbstractKQueueChannel.AbstractKQueueUnsafe
/* 409:    */   {
/* 410:    */     KQueueStreamUnsafe()
/* 411:    */     {
/* 412:503 */       super();
/* 413:    */     }
/* 414:    */     
/* 415:    */     protected Executor prepareToClose()
/* 416:    */     {
/* 417:507 */       return super.prepareToClose();
/* 418:    */     }
/* 419:    */     
/* 420:    */     void readReady(KQueueRecvByteAllocatorHandle allocHandle)
/* 421:    */     {
/* 422:512 */       ChannelConfig config = AbstractKQueueStreamChannel.this.config();
/* 423:513 */       if (AbstractKQueueStreamChannel.this.shouldBreakReadReady(config))
/* 424:    */       {
/* 425:514 */         clearReadFilter0();
/* 426:515 */         return;
/* 427:    */       }
/* 428:517 */       ChannelPipeline pipeline = AbstractKQueueStreamChannel.this.pipeline();
/* 429:518 */       ByteBufAllocator allocator = config.getAllocator();
/* 430:519 */       allocHandle.reset(config);
/* 431:520 */       readReadyBefore();
/* 432:    */       
/* 433:522 */       ByteBuf byteBuf = null;
/* 434:523 */       boolean close = false;
/* 435:    */       try
/* 436:    */       {
/* 437:    */         do
/* 438:    */         {
/* 439:528 */           byteBuf = allocHandle.allocate(allocator);
/* 440:529 */           allocHandle.lastBytesRead(AbstractKQueueStreamChannel.this.doReadBytes(byteBuf));
/* 441:530 */           if (allocHandle.lastBytesRead() <= 0)
/* 442:    */           {
/* 443:532 */             byteBuf.release();
/* 444:533 */             byteBuf = null;
/* 445:534 */             close = allocHandle.lastBytesRead() < 0;
/* 446:535 */             if (!close) {
/* 447:    */               break;
/* 448:    */             }
/* 449:537 */             this.readPending = false; break;
/* 450:    */           }
/* 451:541 */           allocHandle.incMessagesRead(1);
/* 452:542 */           this.readPending = false;
/* 453:543 */           pipeline.fireChannelRead(byteBuf);
/* 454:544 */           byteBuf = null;
/* 455:546 */         } while ((!AbstractKQueueStreamChannel.this.shouldBreakReadReady(config)) && 
/* 456:    */         
/* 457:    */ 
/* 458:    */ 
/* 459:    */ 
/* 460:    */ 
/* 461:    */ 
/* 462:    */ 
/* 463:    */ 
/* 464:    */ 
/* 465:    */ 
/* 466:    */ 
/* 467:    */ 
/* 468:    */ 
/* 469:560 */           (allocHandle.continueReading()));
/* 470:562 */         allocHandle.readComplete();
/* 471:563 */         pipeline.fireChannelReadComplete();
/* 472:565 */         if (close) {
/* 473:566 */           shutdownInput(false);
/* 474:    */         }
/* 475:    */       }
/* 476:    */       catch (Throwable t)
/* 477:    */       {
/* 478:569 */         handleReadException(pipeline, byteBuf, t, close, allocHandle);
/* 479:    */       }
/* 480:    */       finally
/* 481:    */       {
/* 482:571 */         readReadyFinally(config);
/* 483:    */       }
/* 484:    */     }
/* 485:    */     
/* 486:    */     private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, KQueueRecvByteAllocatorHandle allocHandle)
/* 487:    */     {
/* 488:577 */       if (byteBuf != null) {
/* 489:578 */         if (byteBuf.isReadable())
/* 490:    */         {
/* 491:579 */           this.readPending = false;
/* 492:580 */           pipeline.fireChannelRead(byteBuf);
/* 493:    */         }
/* 494:    */         else
/* 495:    */         {
/* 496:582 */           byteBuf.release();
/* 497:    */         }
/* 498:    */       }
/* 499:585 */       allocHandle.readComplete();
/* 500:586 */       pipeline.fireChannelReadComplete();
/* 501:587 */       pipeline.fireExceptionCaught(cause);
/* 502:588 */       if ((close) || ((cause instanceof IOException))) {
/* 503:589 */         shutdownInput(false);
/* 504:    */       }
/* 505:    */     }
/* 506:    */   }
/* 507:    */   
/* 508:    */   private final class KQueueSocketWritableByteChannel
/* 509:    */     extends SocketWritableByteChannel
/* 510:    */   {
/* 511:    */     KQueueSocketWritableByteChannel()
/* 512:    */     {
/* 513:596 */       super();
/* 514:    */     }
/* 515:    */     
/* 516:    */     protected ByteBufAllocator alloc()
/* 517:    */     {
/* 518:601 */       return AbstractKQueueStreamChannel.this.alloc();
/* 519:    */     }
/* 520:    */   }
/* 521:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.AbstractKQueueStreamChannel
 * JD-Core Version:    0.7.0.1
 */