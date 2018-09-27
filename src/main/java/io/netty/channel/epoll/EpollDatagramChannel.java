/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.AddressedEnvelope;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelMetadata;
/*   8:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   9:    */ import io.netty.channel.ChannelPipeline;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.channel.DefaultAddressedEnvelope;
/*  12:    */ import io.netty.channel.EventLoop;
/*  13:    */ import io.netty.channel.socket.DatagramChannel;
/*  14:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  15:    */ import io.netty.channel.socket.DatagramPacket;
/*  16:    */ import io.netty.channel.unix.DatagramSocketAddress;
/*  17:    */ import io.netty.channel.unix.IovArray;
/*  18:    */ import io.netty.channel.unix.UnixChannelUtil;
/*  19:    */ import io.netty.util.internal.StringUtil;
/*  20:    */ import java.io.IOException;
/*  21:    */ import java.net.InetAddress;
/*  22:    */ import java.net.InetSocketAddress;
/*  23:    */ import java.net.NetworkInterface;
/*  24:    */ import java.net.SocketAddress;
/*  25:    */ import java.net.SocketException;
/*  26:    */ import java.nio.ByteBuffer;
/*  27:    */ 
/*  28:    */ public final class EpollDatagramChannel
/*  29:    */   extends AbstractEpollChannel
/*  30:    */   implements DatagramChannel
/*  31:    */ {
/*  32: 50 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  33: 51 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  34: 52 */     StringUtil.simpleClassName(DatagramPacket.class) + ", " + 
/*  35: 53 */     StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + 
/*  36: 54 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  37: 55 */     StringUtil.simpleClassName(InetSocketAddress.class) + ">, " + 
/*  38: 56 */     StringUtil.simpleClassName(ByteBuf.class) + ')';
/*  39:    */   private final EpollDatagramChannelConfig config;
/*  40:    */   private volatile boolean connected;
/*  41:    */   
/*  42:    */   public EpollDatagramChannel()
/*  43:    */   {
/*  44: 62 */     super(LinuxSocket.newSocketDgram(), Native.EPOLLIN);
/*  45: 63 */     this.config = new EpollDatagramChannelConfig(this);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public EpollDatagramChannel(int fd)
/*  49:    */   {
/*  50: 67 */     this(new LinuxSocket(fd));
/*  51:    */   }
/*  52:    */   
/*  53:    */   EpollDatagramChannel(LinuxSocket fd)
/*  54:    */   {
/*  55: 71 */     super(null, fd, Native.EPOLLIN, true);
/*  56: 72 */     this.config = new EpollDatagramChannelConfig(this);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public InetSocketAddress remoteAddress()
/*  60:    */   {
/*  61: 77 */     return (InetSocketAddress)super.remoteAddress();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public InetSocketAddress localAddress()
/*  65:    */   {
/*  66: 82 */     return (InetSocketAddress)super.localAddress();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ChannelMetadata metadata()
/*  70:    */   {
/*  71: 87 */     return METADATA;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public boolean isActive()
/*  75:    */   {
/*  76: 93 */     return (this.socket.isOpen()) && (((this.config.getActiveOnOpen()) && (isRegistered())) || (this.active));
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean isConnected()
/*  80:    */   {
/*  81: 98 */     return this.connected;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public ChannelFuture joinGroup(InetAddress multicastAddress)
/*  85:    */   {
/*  86:103 */     return joinGroup(multicastAddress, newPromise());
/*  87:    */   }
/*  88:    */   
/*  89:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise)
/*  90:    */   {
/*  91:    */     try
/*  92:    */     {
/*  93:109 */       return joinGroup(multicastAddress, 
/*  94:    */       
/*  95:111 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/*  96:    */     }
/*  97:    */     catch (SocketException e)
/*  98:    */     {
/*  99:113 */       promise.setFailure(e);
/* 100:    */     }
/* 101:115 */     return promise;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 105:    */   {
/* 106:121 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 110:    */   {
/* 111:128 */     return joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 115:    */   {
/* 116:134 */     return joinGroup(multicastAddress, networkInterface, source, newPromise());
/* 117:    */   }
/* 118:    */   
/* 119:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 120:    */   {
/* 121:142 */     if (multicastAddress == null) {
/* 122:143 */       throw new NullPointerException("multicastAddress");
/* 123:    */     }
/* 124:146 */     if (networkInterface == null) {
/* 125:147 */       throw new NullPointerException("networkInterface");
/* 126:    */     }
/* 127:150 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 128:151 */     return promise;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 132:    */   {
/* 133:156 */     return leaveGroup(multicastAddress, newPromise());
/* 134:    */   }
/* 135:    */   
/* 136:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 137:    */   {
/* 138:    */     try
/* 139:    */     {
/* 140:162 */       return leaveGroup(multicastAddress, 
/* 141:163 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 142:    */     }
/* 143:    */     catch (SocketException e)
/* 144:    */     {
/* 145:165 */       promise.setFailure(e);
/* 146:    */     }
/* 147:167 */     return promise;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 151:    */   {
/* 152:173 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 156:    */   {
/* 157:180 */     return leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 161:    */   {
/* 162:186 */     return leaveGroup(multicastAddress, networkInterface, source, newPromise());
/* 163:    */   }
/* 164:    */   
/* 165:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 166:    */   {
/* 167:193 */     if (multicastAddress == null) {
/* 168:194 */       throw new NullPointerException("multicastAddress");
/* 169:    */     }
/* 170:196 */     if (networkInterface == null) {
/* 171:197 */       throw new NullPointerException("networkInterface");
/* 172:    */     }
/* 173:200 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 174:    */     
/* 175:202 */     return promise;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 179:    */   {
/* 180:209 */     return block(multicastAddress, networkInterface, sourceToBlock, newPromise());
/* 181:    */   }
/* 182:    */   
/* 183:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 184:    */   {
/* 185:216 */     if (multicastAddress == null) {
/* 186:217 */       throw new NullPointerException("multicastAddress");
/* 187:    */     }
/* 188:219 */     if (sourceToBlock == null) {
/* 189:220 */       throw new NullPointerException("sourceToBlock");
/* 190:    */     }
/* 191:223 */     if (networkInterface == null) {
/* 192:224 */       throw new NullPointerException("networkInterface");
/* 193:    */     }
/* 194:226 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 195:227 */     return promise;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 199:    */   {
/* 200:232 */     return block(multicastAddress, sourceToBlock, newPromise());
/* 201:    */   }
/* 202:    */   
/* 203:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 204:    */   {
/* 205:    */     try
/* 206:    */     {
/* 207:239 */       return block(multicastAddress, 
/* 208:    */       
/* 209:241 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), sourceToBlock, promise);
/* 210:    */     }
/* 211:    */     catch (Throwable e)
/* 212:    */     {
/* 213:244 */       promise.setFailure(e);
/* 214:    */     }
/* 215:246 */     return promise;
/* 216:    */   }
/* 217:    */   
/* 218:    */   protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe()
/* 219:    */   {
/* 220:251 */     return new EpollDatagramChannelUnsafe();
/* 221:    */   }
/* 222:    */   
/* 223:    */   protected void doBind(SocketAddress localAddress)
/* 224:    */     throws Exception
/* 225:    */   {
/* 226:256 */     super.doBind(localAddress);
/* 227:257 */     this.active = true;
/* 228:    */   }
/* 229:    */   
/* 230:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 231:    */     throws Exception
/* 232:    */   {
/* 233:    */     for (;;)
/* 234:    */     {
/* 235:263 */       Object msg = in.current();
/* 236:264 */       if (msg == null)
/* 237:    */       {
/* 238:266 */         clearFlag(Native.EPOLLOUT);
/* 239:267 */         break;
/* 240:    */       }
/* 241:    */       try
/* 242:    */       {
/* 243:272 */         if ((Native.IS_SUPPORTING_SENDMMSG) && (in.size() > 1))
/* 244:    */         {
/* 245:273 */           NativeDatagramPacketArray array = NativeDatagramPacketArray.getInstance(in);
/* 246:274 */           int cnt = array.count();
/* 247:276 */           if (cnt >= 1)
/* 248:    */           {
/* 249:278 */             int offset = 0;
/* 250:279 */             NativeDatagramPacketArray.NativeDatagramPacket[] packets = array.packets();
/* 251:281 */             while (cnt > 0)
/* 252:    */             {
/* 253:282 */               int send = Native.sendmmsg(this.socket.intValue(), packets, offset, cnt);
/* 254:283 */               if (send == 0)
/* 255:    */               {
/* 256:285 */                 setFlag(Native.EPOLLOUT);
/* 257:286 */                 return;
/* 258:    */               }
/* 259:288 */               for (int i = 0; i < send; i++) {
/* 260:289 */                 in.remove();
/* 261:    */               }
/* 262:291 */               cnt -= send;
/* 263:292 */               offset += send;
/* 264:    */             }
/* 265:294 */             continue;
/* 266:    */           }
/* 267:    */         }
/* 268:297 */         boolean done = false;
/* 269:298 */         for (int i = config().getWriteSpinCount(); i > 0; i--) {
/* 270:299 */           if (doWriteMessage(msg))
/* 271:    */           {
/* 272:300 */             done = true;
/* 273:301 */             break;
/* 274:    */           }
/* 275:    */         }
/* 276:305 */         if (done)
/* 277:    */         {
/* 278:306 */           in.remove();
/* 279:    */         }
/* 280:    */         else
/* 281:    */         {
/* 282:309 */           setFlag(Native.EPOLLOUT);
/* 283:310 */           break;
/* 284:    */         }
/* 285:    */       }
/* 286:    */       catch (IOException e)
/* 287:    */       {
/* 288:316 */         in.remove(e);
/* 289:    */       }
/* 290:    */     }
/* 291:    */   }
/* 292:    */   
/* 293:    */   private boolean doWriteMessage(Object msg)
/* 294:    */     throws Exception
/* 295:    */   {
/* 296:    */     InetSocketAddress remoteAddress;
/* 297:    */     ByteBuf data;
/* 298:    */     InetSocketAddress remoteAddress;
/* 299:324 */     if ((msg instanceof AddressedEnvelope))
/* 300:    */     {
/* 301:326 */       AddressedEnvelope<ByteBuf, InetSocketAddress> envelope = (AddressedEnvelope)msg;
/* 302:    */       
/* 303:328 */       ByteBuf data = (ByteBuf)envelope.content();
/* 304:329 */       remoteAddress = (InetSocketAddress)envelope.recipient();
/* 305:    */     }
/* 306:    */     else
/* 307:    */     {
/* 308:331 */       data = (ByteBuf)msg;
/* 309:332 */       remoteAddress = null;
/* 310:    */     }
/* 311:335 */     int dataLen = data.readableBytes();
/* 312:336 */     if (dataLen == 0) {
/* 313:337 */       return true;
/* 314:    */     }
/* 315:    */     long writtenBytes;
/* 316:    */     long writtenBytes;
/* 317:341 */     if (data.hasMemoryAddress())
/* 318:    */     {
/* 319:342 */       long memoryAddress = data.memoryAddress();
/* 320:    */       long writtenBytes;
/* 321:343 */       if (remoteAddress == null) {
/* 322:344 */         writtenBytes = this.socket.writeAddress(memoryAddress, data.readerIndex(), data.writerIndex());
/* 323:    */       } else {
/* 324:346 */         writtenBytes = this.socket.sendToAddress(memoryAddress, data.readerIndex(), data.writerIndex(), remoteAddress
/* 325:347 */           .getAddress(), remoteAddress.getPort());
/* 326:    */       }
/* 327:    */     }
/* 328:    */     else
/* 329:    */     {
/* 330:    */       long writtenBytes;
/* 331:349 */       if (data.nioBufferCount() > 1)
/* 332:    */       {
/* 333:350 */         IovArray array = ((EpollEventLoop)eventLoop()).cleanArray();
/* 334:351 */         array.add(data);
/* 335:352 */         int cnt = array.count();
/* 336:353 */         assert (cnt != 0);
/* 337:    */         long writtenBytes;
/* 338:355 */         if (remoteAddress == null) {
/* 339:356 */           writtenBytes = this.socket.writevAddresses(array.memoryAddress(0), cnt);
/* 340:    */         } else {
/* 341:358 */           writtenBytes = this.socket.sendToAddresses(array.memoryAddress(0), cnt, remoteAddress
/* 342:359 */             .getAddress(), remoteAddress.getPort());
/* 343:    */         }
/* 344:    */       }
/* 345:    */       else
/* 346:    */       {
/* 347:362 */         ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), data.readableBytes());
/* 348:    */         long writtenBytes;
/* 349:363 */         if (remoteAddress == null) {
/* 350:364 */           writtenBytes = this.socket.write(nioData, nioData.position(), nioData.limit());
/* 351:    */         } else {
/* 352:366 */           writtenBytes = this.socket.sendTo(nioData, nioData.position(), nioData.limit(), remoteAddress
/* 353:367 */             .getAddress(), remoteAddress.getPort());
/* 354:    */         }
/* 355:    */       }
/* 356:    */     }
/* 357:371 */     return writtenBytes > 0L;
/* 358:    */   }
/* 359:    */   
/* 360:    */   protected Object filterOutboundMessage(Object msg)
/* 361:    */   {
/* 362:376 */     if ((msg instanceof DatagramPacket))
/* 363:    */     {
/* 364:377 */       DatagramPacket packet = (DatagramPacket)msg;
/* 365:378 */       ByteBuf content = (ByteBuf)packet.content();
/* 366:379 */       return UnixChannelUtil.isBufferCopyNeededForWrite(content) ? new DatagramPacket(
/* 367:380 */         newDirectBuffer(packet, content), (InetSocketAddress)packet.recipient()) : msg;
/* 368:    */     }
/* 369:383 */     if ((msg instanceof ByteBuf))
/* 370:    */     {
/* 371:384 */       ByteBuf buf = (ByteBuf)msg;
/* 372:385 */       return UnixChannelUtil.isBufferCopyNeededForWrite(buf) ? newDirectBuffer(buf) : buf;
/* 373:    */     }
/* 374:388 */     if ((msg instanceof AddressedEnvelope))
/* 375:    */     {
/* 376:390 */       AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope)msg;
/* 377:391 */       if (((e.content() instanceof ByteBuf)) && (
/* 378:392 */         (e.recipient() == null) || ((e.recipient() instanceof InetSocketAddress))))
/* 379:    */       {
/* 380:394 */         ByteBuf content = (ByteBuf)e.content();
/* 381:395 */         return UnixChannelUtil.isBufferCopyNeededForWrite(content) ? new DefaultAddressedEnvelope(
/* 382:    */         
/* 383:397 */           newDirectBuffer(e, content), (InetSocketAddress)e.recipient()) : e;
/* 384:    */       }
/* 385:    */     }
/* 386:402 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 387:    */   }
/* 388:    */   
/* 389:    */   public EpollDatagramChannelConfig config()
/* 390:    */   {
/* 391:407 */     return this.config;
/* 392:    */   }
/* 393:    */   
/* 394:    */   protected void doDisconnect()
/* 395:    */     throws Exception
/* 396:    */   {
/* 397:412 */     this.socket.disconnect();
/* 398:413 */     this.connected = (this.active = 0);
/* 399:    */   }
/* 400:    */   
/* 401:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 402:    */     throws Exception
/* 403:    */   {
/* 404:418 */     if (super.doConnect(remoteAddress, localAddress))
/* 405:    */     {
/* 406:419 */       this.connected = true;
/* 407:420 */       return true;
/* 408:    */     }
/* 409:422 */     return false;
/* 410:    */   }
/* 411:    */   
/* 412:    */   protected void doClose()
/* 413:    */     throws Exception
/* 414:    */   {
/* 415:427 */     super.doClose();
/* 416:428 */     this.connected = false;
/* 417:    */   }
/* 418:    */   
/* 419:    */   final class EpollDatagramChannelUnsafe
/* 420:    */     extends AbstractEpollChannel.AbstractEpollUnsafe
/* 421:    */   {
/* 422:    */     EpollDatagramChannelUnsafe()
/* 423:    */     {
/* 424:431 */       super();
/* 425:    */     }
/* 426:    */     
/* 427:    */     void epollInReady()
/* 428:    */     {
/* 429:435 */       assert (EpollDatagramChannel.this.eventLoop().inEventLoop());
/* 430:436 */       DatagramChannelConfig config = EpollDatagramChannel.this.config();
/* 431:437 */       if (EpollDatagramChannel.this.shouldBreakEpollInReady(config))
/* 432:    */       {
/* 433:438 */         clearEpollIn0();
/* 434:439 */         return;
/* 435:    */       }
/* 436:441 */       EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
/* 437:442 */       allocHandle.edgeTriggered(EpollDatagramChannel.this.isFlagSet(Native.EPOLLET));
/* 438:    */       
/* 439:444 */       ChannelPipeline pipeline = EpollDatagramChannel.this.pipeline();
/* 440:445 */       ByteBufAllocator allocator = config.getAllocator();
/* 441:446 */       allocHandle.reset(config);
/* 442:447 */       epollInBefore();
/* 443:    */       
/* 444:449 */       Throwable exception = null;
/* 445:    */       try
/* 446:    */       {
/* 447:451 */         ByteBuf data = null;
/* 448:    */         try
/* 449:    */         {
/* 450:    */           do
/* 451:    */           {
/* 452:454 */             data = allocHandle.allocate(allocator);
/* 453:455 */             allocHandle.attemptedBytesRead(data.writableBytes());
/* 454:    */             DatagramSocketAddress remoteAddress;
/* 455:    */             DatagramSocketAddress remoteAddress;
/* 456:457 */             if (data.hasMemoryAddress())
/* 457:    */             {
/* 458:459 */               remoteAddress = EpollDatagramChannel.this.socket.recvFromAddress(data.memoryAddress(), data.writerIndex(), data
/* 459:460 */                 .capacity());
/* 460:    */             }
/* 461:    */             else
/* 462:    */             {
/* 463:462 */               ByteBuffer nioData = data.internalNioBuffer(data.writerIndex(), data.writableBytes());
/* 464:463 */               remoteAddress = EpollDatagramChannel.this.socket.recvFrom(nioData, nioData.position(), nioData.limit());
/* 465:    */             }
/* 466:466 */             if (remoteAddress == null)
/* 467:    */             {
/* 468:467 */               allocHandle.lastBytesRead(-1);
/* 469:468 */               data.release();
/* 470:469 */               data = null;
/* 471:470 */               break;
/* 472:    */             }
/* 473:473 */             allocHandle.incMessagesRead(1);
/* 474:474 */             allocHandle.lastBytesRead(remoteAddress.receivedAmount());
/* 475:475 */             data.writerIndex(data.writerIndex() + allocHandle.lastBytesRead());
/* 476:    */             
/* 477:477 */             this.readPending = false;
/* 478:478 */             pipeline.fireChannelRead(new DatagramPacket(data, 
/* 479:479 */               (InetSocketAddress)localAddress(), remoteAddress));
/* 480:    */             
/* 481:481 */             data = null;
/* 482:482 */           } while (allocHandle.continueReading());
/* 483:    */         }
/* 484:    */         catch (Throwable t)
/* 485:    */         {
/* 486:484 */           if (data != null) {
/* 487:485 */             data.release();
/* 488:    */           }
/* 489:487 */           exception = t;
/* 490:    */         }
/* 491:490 */         allocHandle.readComplete();
/* 492:491 */         pipeline.fireChannelReadComplete();
/* 493:493 */         if (exception != null) {
/* 494:494 */           pipeline.fireExceptionCaught(exception);
/* 495:    */         }
/* 496:    */       }
/* 497:    */       finally
/* 498:    */       {
/* 499:497 */         epollInFinally(config);
/* 500:    */       }
/* 501:    */     }
/* 502:    */   }
/* 503:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollDatagramChannel
 * JD-Core Version:    0.7.0.1
 */