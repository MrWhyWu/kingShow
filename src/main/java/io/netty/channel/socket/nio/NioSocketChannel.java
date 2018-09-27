/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelException;
/*   7:    */ import io.netty.channel.ChannelFuture;
/*   8:    */ import io.netty.channel.ChannelFutureListener;
/*   9:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.channel.EventLoop;
/*  12:    */ import io.netty.channel.FileRegion;
/*  13:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  14:    */ import io.netty.channel.nio.AbstractNioByteChannel;
/*  15:    */ import io.netty.channel.nio.AbstractNioByteChannel.NioByteUnsafe;
/*  16:    */ import io.netty.channel.nio.AbstractNioChannel.AbstractNioUnsafe;
/*  17:    */ import io.netty.channel.nio.AbstractNioChannel.NioUnsafe;
/*  18:    */ import io.netty.channel.socket.DefaultSocketChannelConfig;
/*  19:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  20:    */ import io.netty.channel.socket.SocketChannelConfig;
/*  21:    */ import io.netty.util.concurrent.GlobalEventExecutor;
/*  22:    */ import io.netty.util.internal.PlatformDependent;
/*  23:    */ import io.netty.util.internal.SocketUtils;
/*  24:    */ import io.netty.util.internal.logging.InternalLogger;
/*  25:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  26:    */ import java.io.IOException;
/*  27:    */ import java.net.InetSocketAddress;
/*  28:    */ import java.net.Socket;
/*  29:    */ import java.net.SocketAddress;
/*  30:    */ import java.nio.ByteBuffer;
/*  31:    */ import java.nio.channels.SelectionKey;
/*  32:    */ import java.nio.channels.spi.SelectorProvider;
/*  33:    */ import java.util.concurrent.Executor;
/*  34:    */ 
/*  35:    */ public class NioSocketChannel
/*  36:    */   extends AbstractNioByteChannel
/*  37:    */   implements io.netty.channel.socket.SocketChannel
/*  38:    */ {
/*  39: 55 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioSocketChannel.class);
/*  40: 56 */   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
/*  41:    */   private final SocketChannelConfig config;
/*  42:    */   
/*  43:    */   private static java.nio.channels.SocketChannel newSocket(SelectorProvider provider)
/*  44:    */   {
/*  45:    */     try
/*  46:    */     {
/*  47: 66 */       return provider.openSocketChannel();
/*  48:    */     }
/*  49:    */     catch (IOException e)
/*  50:    */     {
/*  51: 68 */       throw new ChannelException("Failed to open a socket.", e);
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   public NioSocketChannel()
/*  56:    */   {
/*  57: 78 */     this(DEFAULT_SELECTOR_PROVIDER);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public NioSocketChannel(SelectorProvider provider)
/*  61:    */   {
/*  62: 85 */     this(newSocket(provider));
/*  63:    */   }
/*  64:    */   
/*  65:    */   public NioSocketChannel(java.nio.channels.SocketChannel socket)
/*  66:    */   {
/*  67: 92 */     this(null, socket);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public NioSocketChannel(Channel parent, java.nio.channels.SocketChannel socket)
/*  71:    */   {
/*  72:102 */     super(parent, socket);
/*  73:103 */     this.config = new NioSocketChannelConfig(this, socket.socket(), null);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public ServerSocketChannel parent()
/*  77:    */   {
/*  78:108 */     return (ServerSocketChannel)super.parent();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public SocketChannelConfig config()
/*  82:    */   {
/*  83:113 */     return this.config;
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected java.nio.channels.SocketChannel javaChannel()
/*  87:    */   {
/*  88:118 */     return (java.nio.channels.SocketChannel)super.javaChannel();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public boolean isActive()
/*  92:    */   {
/*  93:123 */     java.nio.channels.SocketChannel ch = javaChannel();
/*  94:124 */     return (ch.isOpen()) && (ch.isConnected());
/*  95:    */   }
/*  96:    */   
/*  97:    */   public boolean isOutputShutdown()
/*  98:    */   {
/*  99:129 */     return (javaChannel().socket().isOutputShutdown()) || (!isActive());
/* 100:    */   }
/* 101:    */   
/* 102:    */   public boolean isInputShutdown()
/* 103:    */   {
/* 104:134 */     return (javaChannel().socket().isInputShutdown()) || (!isActive());
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isShutdown()
/* 108:    */   {
/* 109:139 */     Socket socket = javaChannel().socket();
/* 110:140 */     return ((socket.isInputShutdown()) && (socket.isOutputShutdown())) || (!isActive());
/* 111:    */   }
/* 112:    */   
/* 113:    */   public InetSocketAddress localAddress()
/* 114:    */   {
/* 115:145 */     return (InetSocketAddress)super.localAddress();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public InetSocketAddress remoteAddress()
/* 119:    */   {
/* 120:150 */     return (InetSocketAddress)super.remoteAddress();
/* 121:    */   }
/* 122:    */   
/* 123:    */   protected final void doShutdownOutput()
/* 124:    */     throws Exception
/* 125:    */   {
/* 126:156 */     if (PlatformDependent.javaVersion() >= 7) {
/* 127:157 */       javaChannel().shutdownOutput();
/* 128:    */     } else {
/* 129:159 */       javaChannel().socket().shutdownOutput();
/* 130:    */     }
/* 131:    */   }
/* 132:    */   
/* 133:    */   public ChannelFuture shutdownOutput()
/* 134:    */   {
/* 135:165 */     return shutdownOutput(newPromise());
/* 136:    */   }
/* 137:    */   
/* 138:    */   public ChannelFuture shutdownOutput(final ChannelPromise promise)
/* 139:    */   {
/* 140:170 */     EventLoop loop = eventLoop();
/* 141:171 */     if (loop.inEventLoop()) {
/* 142:172 */       ((AbstractChannel.AbstractUnsafe)unsafe()).shutdownOutput(promise);
/* 143:    */     } else {
/* 144:174 */       loop.execute(new Runnable()
/* 145:    */       {
/* 146:    */         public void run()
/* 147:    */         {
/* 148:177 */           ((AbstractChannel.AbstractUnsafe)NioSocketChannel.this.unsafe()).shutdownOutput(promise);
/* 149:    */         }
/* 150:    */       });
/* 151:    */     }
/* 152:181 */     return promise;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ChannelFuture shutdownInput()
/* 156:    */   {
/* 157:186 */     return shutdownInput(newPromise());
/* 158:    */   }
/* 159:    */   
/* 160:    */   protected boolean isInputShutdown0()
/* 161:    */   {
/* 162:191 */     return isInputShutdown();
/* 163:    */   }
/* 164:    */   
/* 165:    */   public ChannelFuture shutdownInput(final ChannelPromise promise)
/* 166:    */   {
/* 167:196 */     EventLoop loop = eventLoop();
/* 168:197 */     if (loop.inEventLoop()) {
/* 169:198 */       shutdownInput0(promise);
/* 170:    */     } else {
/* 171:200 */       loop.execute(new Runnable()
/* 172:    */       {
/* 173:    */         public void run()
/* 174:    */         {
/* 175:203 */           NioSocketChannel.this.shutdownInput0(promise);
/* 176:    */         }
/* 177:    */       });
/* 178:    */     }
/* 179:207 */     return promise;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public ChannelFuture shutdown()
/* 183:    */   {
/* 184:212 */     return shutdown(newPromise());
/* 185:    */   }
/* 186:    */   
/* 187:    */   public ChannelFuture shutdown(final ChannelPromise promise)
/* 188:    */   {
/* 189:217 */     ChannelFuture shutdownOutputFuture = shutdownOutput();
/* 190:218 */     if (shutdownOutputFuture.isDone()) {
/* 191:219 */       shutdownOutputDone(shutdownOutputFuture, promise);
/* 192:    */     } else {
/* 193:221 */       shutdownOutputFuture.addListener(new ChannelFutureListener()
/* 194:    */       {
/* 195:    */         public void operationComplete(ChannelFuture shutdownOutputFuture)
/* 196:    */           throws Exception
/* 197:    */         {
/* 198:224 */           NioSocketChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
/* 199:    */         }
/* 200:    */       });
/* 201:    */     }
/* 202:228 */     return promise;
/* 203:    */   }
/* 204:    */   
/* 205:    */   private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture, final ChannelPromise promise)
/* 206:    */   {
/* 207:232 */     ChannelFuture shutdownInputFuture = shutdownInput();
/* 208:233 */     if (shutdownInputFuture.isDone()) {
/* 209:234 */       shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/* 210:    */     } else {
/* 211:236 */       shutdownInputFuture.addListener(new ChannelFutureListener()
/* 212:    */       {
/* 213:    */         public void operationComplete(ChannelFuture shutdownInputFuture)
/* 214:    */           throws Exception
/* 215:    */         {
/* 216:239 */           NioSocketChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
/* 217:    */         }
/* 218:    */       });
/* 219:    */     }
/* 220:    */   }
/* 221:    */   
/* 222:    */   private static void shutdownDone(ChannelFuture shutdownOutputFuture, ChannelFuture shutdownInputFuture, ChannelPromise promise)
/* 223:    */   {
/* 224:248 */     Throwable shutdownOutputCause = shutdownOutputFuture.cause();
/* 225:249 */     Throwable shutdownInputCause = shutdownInputFuture.cause();
/* 226:250 */     if (shutdownOutputCause != null)
/* 227:    */     {
/* 228:251 */       if (shutdownInputCause != null) {
/* 229:252 */         logger.debug("Exception suppressed because a previous exception occurred.", shutdownInputCause);
/* 230:    */       }
/* 231:255 */       promise.setFailure(shutdownOutputCause);
/* 232:    */     }
/* 233:256 */     else if (shutdownInputCause != null)
/* 234:    */     {
/* 235:257 */       promise.setFailure(shutdownInputCause);
/* 236:    */     }
/* 237:    */     else
/* 238:    */     {
/* 239:259 */       promise.setSuccess();
/* 240:    */     }
/* 241:    */   }
/* 242:    */   
/* 243:    */   private void shutdownInput0(ChannelPromise promise)
/* 244:    */   {
/* 245:    */     try
/* 246:    */     {
/* 247:264 */       shutdownInput0();
/* 248:265 */       promise.setSuccess();
/* 249:    */     }
/* 250:    */     catch (Throwable t)
/* 251:    */     {
/* 252:267 */       promise.setFailure(t);
/* 253:    */     }
/* 254:    */   }
/* 255:    */   
/* 256:    */   private void shutdownInput0()
/* 257:    */     throws Exception
/* 258:    */   {
/* 259:272 */     if (PlatformDependent.javaVersion() >= 7) {
/* 260:273 */       javaChannel().shutdownInput();
/* 261:    */     } else {
/* 262:275 */       javaChannel().socket().shutdownInput();
/* 263:    */     }
/* 264:    */   }
/* 265:    */   
/* 266:    */   protected SocketAddress localAddress0()
/* 267:    */   {
/* 268:281 */     return javaChannel().socket().getLocalSocketAddress();
/* 269:    */   }
/* 270:    */   
/* 271:    */   protected SocketAddress remoteAddress0()
/* 272:    */   {
/* 273:286 */     return javaChannel().socket().getRemoteSocketAddress();
/* 274:    */   }
/* 275:    */   
/* 276:    */   protected void doBind(SocketAddress localAddress)
/* 277:    */     throws Exception
/* 278:    */   {
/* 279:291 */     doBind0(localAddress);
/* 280:    */   }
/* 281:    */   
/* 282:    */   private void doBind0(SocketAddress localAddress)
/* 283:    */     throws Exception
/* 284:    */   {
/* 285:295 */     if (PlatformDependent.javaVersion() >= 7) {
/* 286:296 */       SocketUtils.bind(javaChannel(), localAddress);
/* 287:    */     } else {
/* 288:298 */       SocketUtils.bind(javaChannel().socket(), localAddress);
/* 289:    */     }
/* 290:    */   }
/* 291:    */   
/* 292:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 293:    */     throws Exception
/* 294:    */   {
/* 295:304 */     if (localAddress != null) {
/* 296:305 */       doBind0(localAddress);
/* 297:    */     }
/* 298:308 */     boolean success = false;
/* 299:    */     try
/* 300:    */     {
/* 301:310 */       boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
/* 302:311 */       if (!connected) {
/* 303:312 */         selectionKey().interestOps(8);
/* 304:    */       }
/* 305:314 */       success = true;
/* 306:315 */       return connected;
/* 307:    */     }
/* 308:    */     finally
/* 309:    */     {
/* 310:317 */       if (!success) {
/* 311:318 */         doClose();
/* 312:    */       }
/* 313:    */     }
/* 314:    */   }
/* 315:    */   
/* 316:    */   protected void doFinishConnect()
/* 317:    */     throws Exception
/* 318:    */   {
/* 319:325 */     if (!javaChannel().finishConnect()) {
/* 320:326 */       throw new Error();
/* 321:    */     }
/* 322:    */   }
/* 323:    */   
/* 324:    */   protected void doDisconnect()
/* 325:    */     throws Exception
/* 326:    */   {
/* 327:332 */     doClose();
/* 328:    */   }
/* 329:    */   
/* 330:    */   protected void doClose()
/* 331:    */     throws Exception
/* 332:    */   {
/* 333:337 */     super.doClose();
/* 334:338 */     javaChannel().close();
/* 335:    */   }
/* 336:    */   
/* 337:    */   protected int doReadBytes(ByteBuf byteBuf)
/* 338:    */     throws Exception
/* 339:    */   {
/* 340:343 */     RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
/* 341:344 */     allocHandle.attemptedBytesRead(byteBuf.writableBytes());
/* 342:345 */     return byteBuf.writeBytes(javaChannel(), allocHandle.attemptedBytesRead());
/* 343:    */   }
/* 344:    */   
/* 345:    */   protected int doWriteBytes(ByteBuf buf)
/* 346:    */     throws Exception
/* 347:    */   {
/* 348:350 */     int expectedWrittenBytes = buf.readableBytes();
/* 349:351 */     return buf.readBytes(javaChannel(), expectedWrittenBytes);
/* 350:    */   }
/* 351:    */   
/* 352:    */   protected long doWriteFileRegion(FileRegion region)
/* 353:    */     throws Exception
/* 354:    */   {
/* 355:356 */     long position = region.transferred();
/* 356:357 */     return region.transferTo(javaChannel(), position);
/* 357:    */   }
/* 358:    */   
/* 359:    */   private void adjustMaxBytesPerGatheringWrite(int attempted, int written, int oldMaxBytesPerGatheringWrite)
/* 360:    */   {
/* 361:364 */     if (attempted == written)
/* 362:    */     {
/* 363:365 */       if (attempted << 1 > oldMaxBytesPerGatheringWrite) {
/* 364:366 */         ((NioSocketChannelConfig)this.config).setMaxBytesPerGatheringWrite(attempted << 1);
/* 365:    */       }
/* 366:    */     }
/* 367:368 */     else if ((attempted > 4096) && (written < attempted >>> 1)) {
/* 368:369 */       ((NioSocketChannelConfig)this.config).setMaxBytesPerGatheringWrite(attempted >>> 1);
/* 369:    */     }
/* 370:    */   }
/* 371:    */   
/* 372:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 373:    */     throws Exception
/* 374:    */   {
/* 375:375 */     java.nio.channels.SocketChannel ch = javaChannel();
/* 376:376 */     int writeSpinCount = config().getWriteSpinCount();
/* 377:    */     do
/* 378:    */     {
/* 379:378 */       if (in.isEmpty())
/* 380:    */       {
/* 381:380 */         clearOpWrite();
/* 382:    */         
/* 383:382 */         return;
/* 384:    */       }
/* 385:386 */       int maxBytesPerGatheringWrite = ((NioSocketChannelConfig)this.config).getMaxBytesPerGatheringWrite();
/* 386:387 */       ByteBuffer[] nioBuffers = in.nioBuffers(1024, maxBytesPerGatheringWrite);
/* 387:388 */       int nioBufferCnt = in.nioBufferCount();
/* 388:392 */       switch (nioBufferCnt)
/* 389:    */       {
/* 390:    */       case 0: 
/* 391:395 */         writeSpinCount -= doWrite0(in);
/* 392:396 */         break;
/* 393:    */       case 1: 
/* 394:401 */         ByteBuffer buffer = nioBuffers[0];
/* 395:402 */         int attemptedBytes = buffer.remaining();
/* 396:403 */         int localWrittenBytes = ch.write(buffer);
/* 397:404 */         if (localWrittenBytes <= 0)
/* 398:    */         {
/* 399:405 */           incompleteWrite(true);
/* 400:406 */           return;
/* 401:    */         }
/* 402:408 */         adjustMaxBytesPerGatheringWrite(attemptedBytes, localWrittenBytes, maxBytesPerGatheringWrite);
/* 403:409 */         in.removeBytes(localWrittenBytes);
/* 404:410 */         writeSpinCount--;
/* 405:411 */         break;
/* 406:    */       default: 
/* 407:417 */         long attemptedBytes = in.nioBufferSize();
/* 408:418 */         long localWrittenBytes = ch.write(nioBuffers, 0, nioBufferCnt);
/* 409:419 */         if (localWrittenBytes <= 0L)
/* 410:    */         {
/* 411:420 */           incompleteWrite(true);
/* 412:421 */           return;
/* 413:    */         }
/* 414:424 */         adjustMaxBytesPerGatheringWrite((int)attemptedBytes, (int)localWrittenBytes, maxBytesPerGatheringWrite);
/* 415:    */         
/* 416:426 */         in.removeBytes(localWrittenBytes);
/* 417:427 */         writeSpinCount--;
/* 418:    */       }
/* 419:431 */     } while (writeSpinCount > 0);
/* 420:433 */     incompleteWrite(writeSpinCount < 0);
/* 421:    */   }
/* 422:    */   
/* 423:    */   protected AbstractNioChannel.AbstractNioUnsafe newUnsafe()
/* 424:    */   {
/* 425:438 */     return new NioSocketChannelUnsafe(null);
/* 426:    */   }
/* 427:    */   
/* 428:    */   private final class NioSocketChannelUnsafe
/* 429:    */     extends AbstractNioByteChannel.NioByteUnsafe
/* 430:    */   {
/* 431:    */     private NioSocketChannelUnsafe()
/* 432:    */     {
/* 433:441 */       super();
/* 434:    */     }
/* 435:    */     
/* 436:    */     protected Executor prepareToClose()
/* 437:    */     {
/* 438:    */       try
/* 439:    */       {
/* 440:445 */         if ((NioSocketChannel.this.javaChannel().isOpen()) && (NioSocketChannel.this.config().getSoLinger() > 0))
/* 441:    */         {
/* 442:450 */           NioSocketChannel.this.doDeregister();
/* 443:451 */           return GlobalEventExecutor.INSTANCE;
/* 444:    */         }
/* 445:    */       }
/* 446:    */       catch (Throwable localThrowable) {}
/* 447:458 */       return null;
/* 448:    */     }
/* 449:    */   }
/* 450:    */   
/* 451:    */   private final class NioSocketChannelConfig
/* 452:    */     extends DefaultSocketChannelConfig
/* 453:    */   {
/* 454:463 */     private volatile int maxBytesPerGatheringWrite = 2147483647;
/* 455:    */     
/* 456:    */     private NioSocketChannelConfig(NioSocketChannel channel, Socket javaSocket)
/* 457:    */     {
/* 458:466 */       super(javaSocket);
/* 459:467 */       calculateMaxBytesPerGatheringWrite();
/* 460:    */     }
/* 461:    */     
/* 462:    */     protected void autoReadCleared()
/* 463:    */     {
/* 464:472 */       NioSocketChannel.this.clearReadPending();
/* 465:    */     }
/* 466:    */     
/* 467:    */     public NioSocketChannelConfig setSendBufferSize(int sendBufferSize)
/* 468:    */     {
/* 469:477 */       super.setSendBufferSize(sendBufferSize);
/* 470:478 */       calculateMaxBytesPerGatheringWrite();
/* 471:479 */       return this;
/* 472:    */     }
/* 473:    */     
/* 474:    */     void setMaxBytesPerGatheringWrite(int maxBytesPerGatheringWrite)
/* 475:    */     {
/* 476:483 */       this.maxBytesPerGatheringWrite = maxBytesPerGatheringWrite;
/* 477:    */     }
/* 478:    */     
/* 479:    */     int getMaxBytesPerGatheringWrite()
/* 480:    */     {
/* 481:487 */       return this.maxBytesPerGatheringWrite;
/* 482:    */     }
/* 483:    */     
/* 484:    */     private void calculateMaxBytesPerGatheringWrite()
/* 485:    */     {
/* 486:492 */       int newSendBufferSize = getSendBufferSize() << 1;
/* 487:493 */       if (newSendBufferSize > 0) {
/* 488:494 */         setMaxBytesPerGatheringWrite(getSendBufferSize() << 1);
/* 489:    */       }
/* 490:    */     }
/* 491:    */   }
/* 492:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.nio.NioSocketChannel
 * JD-Core Version:    0.7.0.1
 */