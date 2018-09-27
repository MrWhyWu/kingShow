/*   1:    */ package io.netty.channel.kqueue;
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
/*  28:    */ public final class KQueueDatagramChannel
/*  29:    */   extends AbstractKQueueChannel
/*  30:    */   implements DatagramChannel
/*  31:    */ {
/*  32: 50 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  33: 51 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  34: 52 */     StringUtil.simpleClassName(DatagramPacket.class) + ", " + 
/*  35: 53 */     StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + 
/*  36: 54 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  37: 55 */     StringUtil.simpleClassName(InetSocketAddress.class) + ">, " + 
/*  38: 56 */     StringUtil.simpleClassName(ByteBuf.class) + ')';
/*  39:    */   private volatile boolean connected;
/*  40:    */   private final KQueueDatagramChannelConfig config;
/*  41:    */   
/*  42:    */   public KQueueDatagramChannel()
/*  43:    */   {
/*  44: 62 */     super(null, BsdSocket.newSocketDgram(), false);
/*  45: 63 */     this.config = new KQueueDatagramChannelConfig(this);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public KQueueDatagramChannel(int fd)
/*  49:    */   {
/*  50: 67 */     this(new BsdSocket(fd), true);
/*  51:    */   }
/*  52:    */   
/*  53:    */   KQueueDatagramChannel(BsdSocket socket, boolean active)
/*  54:    */   {
/*  55: 71 */     super(null, socket, active);
/*  56: 72 */     this.config = new KQueueDatagramChannelConfig(this);
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
/*  99:114 */       promise.setFailure(e);
/* 100:    */     }
/* 101:116 */     return promise;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 105:    */   {
/* 106:122 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/* 107:    */   }
/* 108:    */   
/* 109:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 110:    */   {
/* 111:129 */     return joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 115:    */   {
/* 116:135 */     return joinGroup(multicastAddress, networkInterface, source, newPromise());
/* 117:    */   }
/* 118:    */   
/* 119:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 120:    */   {
/* 121:143 */     if (multicastAddress == null) {
/* 122:144 */       throw new NullPointerException("multicastAddress");
/* 123:    */     }
/* 124:147 */     if (networkInterface == null) {
/* 125:148 */       throw new NullPointerException("networkInterface");
/* 126:    */     }
/* 127:151 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 128:152 */     return promise;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 132:    */   {
/* 133:157 */     return leaveGroup(multicastAddress, newPromise());
/* 134:    */   }
/* 135:    */   
/* 136:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 137:    */   {
/* 138:    */     try
/* 139:    */     {
/* 140:163 */       return leaveGroup(multicastAddress, 
/* 141:164 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 142:    */     }
/* 143:    */     catch (SocketException e)
/* 144:    */     {
/* 145:166 */       promise.setFailure(e);
/* 146:    */     }
/* 147:168 */     return promise;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 151:    */   {
/* 152:174 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 156:    */   {
/* 157:181 */     return leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 161:    */   {
/* 162:187 */     return leaveGroup(multicastAddress, networkInterface, source, newPromise());
/* 163:    */   }
/* 164:    */   
/* 165:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 166:    */   {
/* 167:194 */     if (multicastAddress == null) {
/* 168:195 */       throw new NullPointerException("multicastAddress");
/* 169:    */     }
/* 170:197 */     if (networkInterface == null) {
/* 171:198 */       throw new NullPointerException("networkInterface");
/* 172:    */     }
/* 173:201 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 174:    */     
/* 175:203 */     return promise;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 179:    */   {
/* 180:210 */     return block(multicastAddress, networkInterface, sourceToBlock, newPromise());
/* 181:    */   }
/* 182:    */   
/* 183:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 184:    */   {
/* 185:217 */     if (multicastAddress == null) {
/* 186:218 */       throw new NullPointerException("multicastAddress");
/* 187:    */     }
/* 188:220 */     if (sourceToBlock == null) {
/* 189:221 */       throw new NullPointerException("sourceToBlock");
/* 190:    */     }
/* 191:224 */     if (networkInterface == null) {
/* 192:225 */       throw new NullPointerException("networkInterface");
/* 193:    */     }
/* 194:227 */     promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
/* 195:228 */     return promise;
/* 196:    */   }
/* 197:    */   
/* 198:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 199:    */   {
/* 200:233 */     return block(multicastAddress, sourceToBlock, newPromise());
/* 201:    */   }
/* 202:    */   
/* 203:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 204:    */   {
/* 205:    */     try
/* 206:    */     {
/* 207:240 */       return block(multicastAddress, 
/* 208:    */       
/* 209:242 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), sourceToBlock, promise);
/* 210:    */     }
/* 211:    */     catch (Throwable e)
/* 212:    */     {
/* 213:245 */       promise.setFailure(e);
/* 214:    */     }
/* 215:247 */     return promise;
/* 216:    */   }
/* 217:    */   
/* 218:    */   protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe()
/* 219:    */   {
/* 220:252 */     return new KQueueDatagramChannelUnsafe();
/* 221:    */   }
/* 222:    */   
/* 223:    */   protected void doBind(SocketAddress localAddress)
/* 224:    */     throws Exception
/* 225:    */   {
/* 226:257 */     super.doBind(localAddress);
/* 227:258 */     this.active = true;
/* 228:    */   }
/* 229:    */   
/* 230:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 231:    */     throws Exception
/* 232:    */   {
/* 233:    */     for (;;)
/* 234:    */     {
/* 235:264 */       Object msg = in.current();
/* 236:265 */       if (msg == null)
/* 237:    */       {
/* 238:267 */         writeFilter(false);
/* 239:268 */         break;
/* 240:    */       }
/* 241:    */       try
/* 242:    */       {
/* 243:272 */         boolean done = false;
/* 244:273 */         for (int i = config().getWriteSpinCount(); i > 0; i--) {
/* 245:274 */           if (doWriteMessage(msg))
/* 246:    */           {
/* 247:275 */             done = true;
/* 248:276 */             break;
/* 249:    */           }
/* 250:    */         }
/* 251:280 */         if (done)
/* 252:    */         {
/* 253:281 */           in.remove();
/* 254:    */         }
/* 255:    */         else
/* 256:    */         {
/* 257:284 */           writeFilter(true);
/* 258:285 */           break;
/* 259:    */         }
/* 260:    */       }
/* 261:    */       catch (IOException e)
/* 262:    */       {
/* 263:291 */         in.remove(e);
/* 264:    */       }
/* 265:    */     }
/* 266:    */   }
/* 267:    */   
/* 268:    */   private boolean doWriteMessage(Object msg)
/* 269:    */     throws Exception
/* 270:    */   {
/* 271:    */     InetSocketAddress remoteAddress;
/* 272:    */     ByteBuf data;
/* 273:    */     InetSocketAddress remoteAddress;
/* 274:299 */     if ((msg instanceof AddressedEnvelope))
/* 275:    */     {
/* 276:301 */       AddressedEnvelope<ByteBuf, InetSocketAddress> envelope = (AddressedEnvelope)msg;
/* 277:    */       
/* 278:303 */       ByteBuf data = (ByteBuf)envelope.content();
/* 279:304 */       remoteAddress = (InetSocketAddress)envelope.recipient();
/* 280:    */     }
/* 281:    */     else
/* 282:    */     {
/* 283:306 */       data = (ByteBuf)msg;
/* 284:307 */       remoteAddress = null;
/* 285:    */     }
/* 286:310 */     int dataLen = data.readableBytes();
/* 287:311 */     if (dataLen == 0) {
/* 288:312 */       return true;
/* 289:    */     }
/* 290:    */     long writtenBytes;
/* 291:    */     long writtenBytes;
/* 292:316 */     if (data.hasMemoryAddress())
/* 293:    */     {
/* 294:317 */       long memoryAddress = data.memoryAddress();
/* 295:    */       long writtenBytes;
/* 296:318 */       if (remoteAddress == null) {
/* 297:319 */         writtenBytes = this.socket.writeAddress(memoryAddress, data.readerIndex(), data.writerIndex());
/* 298:    */       } else {
/* 299:321 */         writtenBytes = this.socket.sendToAddress(memoryAddress, data.readerIndex(), data.writerIndex(), remoteAddress
/* 300:322 */           .getAddress(), remoteAddress.getPort());
/* 301:    */       }
/* 302:    */     }
/* 303:    */     else
/* 304:    */     {
/* 305:    */       long writtenBytes;
/* 306:324 */       if (data.nioBufferCount() > 1)
/* 307:    */       {
/* 308:325 */         IovArray array = ((KQueueEventLoop)eventLoop()).cleanArray();
/* 309:326 */         array.add(data);
/* 310:327 */         int cnt = array.count();
/* 311:328 */         assert (cnt != 0);
/* 312:    */         long writtenBytes;
/* 313:330 */         if (remoteAddress == null) {
/* 314:331 */           writtenBytes = this.socket.writevAddresses(array.memoryAddress(0), cnt);
/* 315:    */         } else {
/* 316:333 */           writtenBytes = this.socket.sendToAddresses(array.memoryAddress(0), cnt, remoteAddress
/* 317:334 */             .getAddress(), remoteAddress.getPort());
/* 318:    */         }
/* 319:    */       }
/* 320:    */       else
/* 321:    */       {
/* 322:337 */         ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), data.readableBytes());
/* 323:    */         long writtenBytes;
/* 324:338 */         if (remoteAddress == null) {
/* 325:339 */           writtenBytes = this.socket.write(nioData, nioData.position(), nioData.limit());
/* 326:    */         } else {
/* 327:341 */           writtenBytes = this.socket.sendTo(nioData, nioData.position(), nioData.limit(), remoteAddress
/* 328:342 */             .getAddress(), remoteAddress.getPort());
/* 329:    */         }
/* 330:    */       }
/* 331:    */     }
/* 332:346 */     return writtenBytes > 0L;
/* 333:    */   }
/* 334:    */   
/* 335:    */   protected Object filterOutboundMessage(Object msg)
/* 336:    */   {
/* 337:351 */     if ((msg instanceof DatagramPacket))
/* 338:    */     {
/* 339:352 */       DatagramPacket packet = (DatagramPacket)msg;
/* 340:353 */       ByteBuf content = (ByteBuf)packet.content();
/* 341:354 */       return UnixChannelUtil.isBufferCopyNeededForWrite(content) ? new DatagramPacket(
/* 342:355 */         newDirectBuffer(packet, content), (InetSocketAddress)packet.recipient()) : msg;
/* 343:    */     }
/* 344:358 */     if ((msg instanceof ByteBuf))
/* 345:    */     {
/* 346:359 */       ByteBuf buf = (ByteBuf)msg;
/* 347:360 */       return UnixChannelUtil.isBufferCopyNeededForWrite(buf) ? newDirectBuffer(buf) : buf;
/* 348:    */     }
/* 349:363 */     if ((msg instanceof AddressedEnvelope))
/* 350:    */     {
/* 351:365 */       AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope)msg;
/* 352:366 */       if (((e.content() instanceof ByteBuf)) && (
/* 353:367 */         (e.recipient() == null) || ((e.recipient() instanceof InetSocketAddress))))
/* 354:    */       {
/* 355:369 */         ByteBuf content = (ByteBuf)e.content();
/* 356:370 */         return UnixChannelUtil.isBufferCopyNeededForWrite(content) ? new DefaultAddressedEnvelope(
/* 357:    */         
/* 358:372 */           newDirectBuffer(e, content), (InetSocketAddress)e.recipient()) : e;
/* 359:    */       }
/* 360:    */     }
/* 361:377 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 362:    */   }
/* 363:    */   
/* 364:    */   public KQueueDatagramChannelConfig config()
/* 365:    */   {
/* 366:382 */     return this.config;
/* 367:    */   }
/* 368:    */   
/* 369:    */   protected void doDisconnect()
/* 370:    */     throws Exception
/* 371:    */   {
/* 372:387 */     this.socket.disconnect();
/* 373:388 */     this.connected = (this.active = 0);
/* 374:    */   }
/* 375:    */   
/* 376:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 377:    */     throws Exception
/* 378:    */   {
/* 379:393 */     if (super.doConnect(remoteAddress, localAddress))
/* 380:    */     {
/* 381:394 */       this.connected = true;
/* 382:395 */       return true;
/* 383:    */     }
/* 384:397 */     return false;
/* 385:    */   }
/* 386:    */   
/* 387:    */   protected void doClose()
/* 388:    */     throws Exception
/* 389:    */   {
/* 390:402 */     super.doClose();
/* 391:403 */     this.connected = false;
/* 392:    */   }
/* 393:    */   
/* 394:    */   final class KQueueDatagramChannelUnsafe
/* 395:    */     extends AbstractKQueueChannel.AbstractKQueueUnsafe
/* 396:    */   {
/* 397:    */     KQueueDatagramChannelUnsafe()
/* 398:    */     {
/* 399:406 */       super();
/* 400:    */     }
/* 401:    */     
/* 402:    */     void readReady(KQueueRecvByteAllocatorHandle allocHandle)
/* 403:    */     {
/* 404:410 */       assert (KQueueDatagramChannel.this.eventLoop().inEventLoop());
/* 405:411 */       DatagramChannelConfig config = KQueueDatagramChannel.this.config();
/* 406:412 */       if (KQueueDatagramChannel.this.shouldBreakReadReady(config))
/* 407:    */       {
/* 408:413 */         clearReadFilter0();
/* 409:414 */         return;
/* 410:    */       }
/* 411:416 */       ChannelPipeline pipeline = KQueueDatagramChannel.this.pipeline();
/* 412:417 */       ByteBufAllocator allocator = config.getAllocator();
/* 413:418 */       allocHandle.reset(config);
/* 414:419 */       readReadyBefore();
/* 415:    */       
/* 416:421 */       Throwable exception = null;
/* 417:    */       try
/* 418:    */       {
/* 419:423 */         ByteBuf data = null;
/* 420:    */         try
/* 421:    */         {
/* 422:    */           do
/* 423:    */           {
/* 424:426 */             data = allocHandle.allocate(allocator);
/* 425:427 */             allocHandle.attemptedBytesRead(data.writableBytes());
/* 426:    */             DatagramSocketAddress remoteAddress;
/* 427:    */             DatagramSocketAddress remoteAddress;
/* 428:429 */             if (data.hasMemoryAddress())
/* 429:    */             {
/* 430:431 */               remoteAddress = KQueueDatagramChannel.this.socket.recvFromAddress(data.memoryAddress(), data.writerIndex(), data
/* 431:432 */                 .capacity());
/* 432:    */             }
/* 433:    */             else
/* 434:    */             {
/* 435:434 */               ByteBuffer nioData = data.internalNioBuffer(data.writerIndex(), data.writableBytes());
/* 436:435 */               remoteAddress = KQueueDatagramChannel.this.socket.recvFrom(nioData, nioData.position(), nioData.limit());
/* 437:    */             }
/* 438:438 */             if (remoteAddress == null)
/* 439:    */             {
/* 440:439 */               allocHandle.lastBytesRead(-1);
/* 441:440 */               data.release();
/* 442:441 */               data = null;
/* 443:442 */               break;
/* 444:    */             }
/* 445:445 */             allocHandle.incMessagesRead(1);
/* 446:446 */             allocHandle.lastBytesRead(remoteAddress.receivedAmount());
/* 447:447 */             data.writerIndex(data.writerIndex() + allocHandle.lastBytesRead());
/* 448:    */             
/* 449:449 */             this.readPending = false;
/* 450:450 */             pipeline.fireChannelRead(new DatagramPacket(data, 
/* 451:451 */               (InetSocketAddress)localAddress(), remoteAddress));
/* 452:    */             
/* 453:453 */             data = null;
/* 454:454 */           } while (allocHandle.continueReading());
/* 455:    */         }
/* 456:    */         catch (Throwable t)
/* 457:    */         {
/* 458:456 */           if (data != null) {
/* 459:457 */             data.release();
/* 460:    */           }
/* 461:459 */           exception = t;
/* 462:    */         }
/* 463:462 */         allocHandle.readComplete();
/* 464:463 */         pipeline.fireChannelReadComplete();
/* 465:465 */         if (exception != null) {
/* 466:466 */           pipeline.fireExceptionCaught(exception);
/* 467:    */         }
/* 468:    */       }
/* 469:    */       finally
/* 470:    */       {
/* 471:469 */         readReadyFinally(config);
/* 472:    */       }
/* 473:    */     }
/* 474:    */   }
/* 475:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueDatagramChannel
 * JD-Core Version:    0.7.0.1
 */