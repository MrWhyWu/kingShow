/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.AddressedEnvelope;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelMetadata;
/*   8:    */ import io.netty.channel.ChannelOption;
/*   9:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.channel.DefaultAddressedEnvelope;
/*  12:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  13:    */ import io.netty.channel.nio.AbstractNioChannel.NioUnsafe;
/*  14:    */ import io.netty.channel.nio.AbstractNioMessageChannel;
/*  15:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  16:    */ import io.netty.channel.socket.DatagramPacket;
/*  17:    */ import io.netty.channel.socket.InternetProtocolFamily;
/*  18:    */ import io.netty.util.internal.PlatformDependent;
/*  19:    */ import io.netty.util.internal.SocketUtils;
/*  20:    */ import io.netty.util.internal.StringUtil;
/*  21:    */ import java.io.IOException;
/*  22:    */ import java.net.DatagramSocket;
/*  23:    */ import java.net.InetAddress;
/*  24:    */ import java.net.InetSocketAddress;
/*  25:    */ import java.net.NetworkInterface;
/*  26:    */ import java.net.SocketAddress;
/*  27:    */ import java.net.SocketException;
/*  28:    */ import java.nio.ByteBuffer;
/*  29:    */ import java.nio.channels.MembershipKey;
/*  30:    */ import java.nio.channels.spi.SelectorProvider;
/*  31:    */ import java.util.ArrayList;
/*  32:    */ import java.util.HashMap;
/*  33:    */ import java.util.Iterator;
/*  34:    */ import java.util.List;
/*  35:    */ import java.util.Map;
/*  36:    */ 
/*  37:    */ public final class NioDatagramChannel
/*  38:    */   extends AbstractNioMessageChannel
/*  39:    */   implements io.netty.channel.socket.DatagramChannel
/*  40:    */ {
/*  41: 65 */   private static final ChannelMetadata METADATA = new ChannelMetadata(true);
/*  42: 66 */   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
/*  43: 67 */   private static final String EXPECTED_TYPES = " (expected: " + 
/*  44: 68 */     StringUtil.simpleClassName(DatagramPacket.class) + ", " + 
/*  45: 69 */     StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + 
/*  46: 70 */     StringUtil.simpleClassName(ByteBuf.class) + ", " + 
/*  47: 71 */     StringUtil.simpleClassName(SocketAddress.class) + ">, " + 
/*  48: 72 */     StringUtil.simpleClassName(ByteBuf.class) + ')';
/*  49:    */   private final DatagramChannelConfig config;
/*  50:    */   private Map<InetAddress, List<MembershipKey>> memberships;
/*  51:    */   
/*  52:    */   private static java.nio.channels.DatagramChannel newSocket(SelectorProvider provider)
/*  53:    */   {
/*  54:    */     try
/*  55:    */     {
/*  56: 86 */       return provider.openDatagramChannel();
/*  57:    */     }
/*  58:    */     catch (IOException e)
/*  59:    */     {
/*  60: 88 */       throw new ChannelException("Failed to open a socket.", e);
/*  61:    */     }
/*  62:    */   }
/*  63:    */   
/*  64:    */   private static java.nio.channels.DatagramChannel newSocket(SelectorProvider provider, InternetProtocolFamily ipFamily)
/*  65:    */   {
/*  66: 93 */     if (ipFamily == null) {
/*  67: 94 */       return newSocket(provider);
/*  68:    */     }
/*  69: 97 */     checkJavaVersion();
/*  70:    */     try
/*  71:    */     {
/*  72:100 */       return provider.openDatagramChannel(ProtocolFamilyConverter.convert(ipFamily));
/*  73:    */     }
/*  74:    */     catch (IOException e)
/*  75:    */     {
/*  76:102 */       throw new ChannelException("Failed to open a socket.", e);
/*  77:    */     }
/*  78:    */   }
/*  79:    */   
/*  80:    */   private static void checkJavaVersion()
/*  81:    */   {
/*  82:107 */     if (PlatformDependent.javaVersion() < 7) {
/*  83:108 */       throw new UnsupportedOperationException("Only supported on java 7+.");
/*  84:    */     }
/*  85:    */   }
/*  86:    */   
/*  87:    */   public NioDatagramChannel()
/*  88:    */   {
/*  89:116 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER));
/*  90:    */   }
/*  91:    */   
/*  92:    */   public NioDatagramChannel(SelectorProvider provider)
/*  93:    */   {
/*  94:124 */     this(newSocket(provider));
/*  95:    */   }
/*  96:    */   
/*  97:    */   public NioDatagramChannel(InternetProtocolFamily ipFamily)
/*  98:    */   {
/*  99:132 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER, ipFamily));
/* 100:    */   }
/* 101:    */   
/* 102:    */   public NioDatagramChannel(SelectorProvider provider, InternetProtocolFamily ipFamily)
/* 103:    */   {
/* 104:141 */     this(newSocket(provider, ipFamily));
/* 105:    */   }
/* 106:    */   
/* 107:    */   public NioDatagramChannel(java.nio.channels.DatagramChannel socket)
/* 108:    */   {
/* 109:148 */     super(null, socket, 1);
/* 110:149 */     this.config = new NioDatagramChannelConfig(this, socket);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public ChannelMetadata metadata()
/* 114:    */   {
/* 115:154 */     return METADATA;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public DatagramChannelConfig config()
/* 119:    */   {
/* 120:159 */     return this.config;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public boolean isActive()
/* 124:    */   {
/* 125:165 */     java.nio.channels.DatagramChannel ch = javaChannel();
/* 126:166 */     return (ch.isOpen()) && (
/* 127:167 */       ((((Boolean)this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)).booleanValue()) && (isRegistered())) || 
/* 128:168 */       (ch.socket().isBound()));
/* 129:    */   }
/* 130:    */   
/* 131:    */   public boolean isConnected()
/* 132:    */   {
/* 133:173 */     return javaChannel().isConnected();
/* 134:    */   }
/* 135:    */   
/* 136:    */   protected java.nio.channels.DatagramChannel javaChannel()
/* 137:    */   {
/* 138:178 */     return (java.nio.channels.DatagramChannel)super.javaChannel();
/* 139:    */   }
/* 140:    */   
/* 141:    */   protected SocketAddress localAddress0()
/* 142:    */   {
/* 143:183 */     return javaChannel().socket().getLocalSocketAddress();
/* 144:    */   }
/* 145:    */   
/* 146:    */   protected SocketAddress remoteAddress0()
/* 147:    */   {
/* 148:188 */     return javaChannel().socket().getRemoteSocketAddress();
/* 149:    */   }
/* 150:    */   
/* 151:    */   protected void doBind(SocketAddress localAddress)
/* 152:    */     throws Exception
/* 153:    */   {
/* 154:193 */     doBind0(localAddress);
/* 155:    */   }
/* 156:    */   
/* 157:    */   private void doBind0(SocketAddress localAddress)
/* 158:    */     throws Exception
/* 159:    */   {
/* 160:197 */     if (PlatformDependent.javaVersion() >= 7) {
/* 161:198 */       SocketUtils.bind(javaChannel(), localAddress);
/* 162:    */     } else {
/* 163:200 */       javaChannel().socket().bind(localAddress);
/* 164:    */     }
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 168:    */     throws Exception
/* 169:    */   {
/* 170:207 */     if (localAddress != null) {
/* 171:208 */       doBind0(localAddress);
/* 172:    */     }
/* 173:211 */     boolean success = false;
/* 174:    */     try
/* 175:    */     {
/* 176:213 */       javaChannel().connect(remoteAddress);
/* 177:214 */       success = true;
/* 178:215 */       return true;
/* 179:    */     }
/* 180:    */     finally
/* 181:    */     {
/* 182:217 */       if (!success) {
/* 183:218 */         doClose();
/* 184:    */       }
/* 185:    */     }
/* 186:    */   }
/* 187:    */   
/* 188:    */   protected void doFinishConnect()
/* 189:    */     throws Exception
/* 190:    */   {
/* 191:225 */     throw new Error();
/* 192:    */   }
/* 193:    */   
/* 194:    */   protected void doDisconnect()
/* 195:    */     throws Exception
/* 196:    */   {
/* 197:230 */     javaChannel().disconnect();
/* 198:    */   }
/* 199:    */   
/* 200:    */   protected void doClose()
/* 201:    */     throws Exception
/* 202:    */   {
/* 203:235 */     javaChannel().close();
/* 204:    */   }
/* 205:    */   
/* 206:    */   protected int doReadMessages(List<Object> buf)
/* 207:    */     throws Exception
/* 208:    */   {
/* 209:240 */     java.nio.channels.DatagramChannel ch = javaChannel();
/* 210:241 */     DatagramChannelConfig config = config();
/* 211:242 */     RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
/* 212:    */     
/* 213:244 */     ByteBuf data = allocHandle.allocate(config.getAllocator());
/* 214:245 */     allocHandle.attemptedBytesRead(data.writableBytes());
/* 215:246 */     boolean free = true;
/* 216:    */     try
/* 217:    */     {
/* 218:248 */       ByteBuffer nioData = data.internalNioBuffer(data.writerIndex(), data.writableBytes());
/* 219:249 */       pos = nioData.position();
/* 220:250 */       InetSocketAddress remoteAddress = (InetSocketAddress)ch.receive(nioData);
/* 221:    */       int i;
/* 222:251 */       if (remoteAddress == null) {
/* 223:252 */         return 0;
/* 224:    */       }
/* 225:255 */       allocHandle.lastBytesRead(nioData.position() - pos);
/* 226:256 */       buf.add(new DatagramPacket(data.writerIndex(data.writerIndex() + allocHandle.lastBytesRead()), 
/* 227:257 */         localAddress(), remoteAddress));
/* 228:258 */       free = false;
/* 229:259 */       return 1;
/* 230:    */     }
/* 231:    */     catch (Throwable cause)
/* 232:    */     {
/* 233:    */       int pos;
/* 234:261 */       PlatformDependent.throwException(cause);
/* 235:262 */       return -1;
/* 236:    */     }
/* 237:    */     finally
/* 238:    */     {
/* 239:264 */       if (free) {
/* 240:265 */         data.release();
/* 241:    */       }
/* 242:    */     }
/* 243:    */   }
/* 244:    */   
/* 245:    */   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in)
/* 246:    */     throws Exception
/* 247:    */   {
/* 248:    */     ByteBuf data;
/* 249:    */     ByteBuf data;
/* 250:    */     SocketAddress remoteAddress;
/* 251:274 */     if ((msg instanceof AddressedEnvelope))
/* 252:    */     {
/* 253:276 */       AddressedEnvelope<ByteBuf, SocketAddress> envelope = (AddressedEnvelope)msg;
/* 254:277 */       SocketAddress remoteAddress = envelope.recipient();
/* 255:278 */       data = (ByteBuf)envelope.content();
/* 256:    */     }
/* 257:    */     else
/* 258:    */     {
/* 259:280 */       data = (ByteBuf)msg;
/* 260:281 */       remoteAddress = null;
/* 261:    */     }
/* 262:284 */     int dataLen = data.readableBytes();
/* 263:285 */     if (dataLen == 0) {
/* 264:286 */       return true;
/* 265:    */     }
/* 266:289 */     ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), dataLen);
/* 267:    */     int writtenBytes;
/* 268:    */     int writtenBytes;
/* 269:291 */     if (remoteAddress != null) {
/* 270:292 */       writtenBytes = javaChannel().send(nioData, remoteAddress);
/* 271:    */     } else {
/* 272:294 */       writtenBytes = javaChannel().write(nioData);
/* 273:    */     }
/* 274:296 */     return writtenBytes > 0;
/* 275:    */   }
/* 276:    */   
/* 277:    */   protected Object filterOutboundMessage(Object msg)
/* 278:    */   {
/* 279:301 */     if ((msg instanceof DatagramPacket))
/* 280:    */     {
/* 281:302 */       DatagramPacket p = (DatagramPacket)msg;
/* 282:303 */       ByteBuf content = (ByteBuf)p.content();
/* 283:304 */       if (isSingleDirectBuffer(content)) {
/* 284:305 */         return p;
/* 285:    */       }
/* 286:307 */       return new DatagramPacket(newDirectBuffer(p, content), (InetSocketAddress)p.recipient());
/* 287:    */     }
/* 288:310 */     if ((msg instanceof ByteBuf))
/* 289:    */     {
/* 290:311 */       ByteBuf buf = (ByteBuf)msg;
/* 291:312 */       if (isSingleDirectBuffer(buf)) {
/* 292:313 */         return buf;
/* 293:    */       }
/* 294:315 */       return newDirectBuffer(buf);
/* 295:    */     }
/* 296:318 */     if ((msg instanceof AddressedEnvelope))
/* 297:    */     {
/* 298:320 */       AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope)msg;
/* 299:321 */       if ((e.content() instanceof ByteBuf))
/* 300:    */       {
/* 301:322 */         ByteBuf content = (ByteBuf)e.content();
/* 302:323 */         if (isSingleDirectBuffer(content)) {
/* 303:324 */           return e;
/* 304:    */         }
/* 305:326 */         return new DefaultAddressedEnvelope(newDirectBuffer(e, content), e.recipient());
/* 306:    */       }
/* 307:    */     }
/* 308:331 */     throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
/* 309:    */   }
/* 310:    */   
/* 311:    */   private static boolean isSingleDirectBuffer(ByteBuf buf)
/* 312:    */   {
/* 313:339 */     return (buf.isDirect()) && (buf.nioBufferCount() == 1);
/* 314:    */   }
/* 315:    */   
/* 316:    */   protected boolean continueOnWriteError()
/* 317:    */   {
/* 318:347 */     return true;
/* 319:    */   }
/* 320:    */   
/* 321:    */   public InetSocketAddress localAddress()
/* 322:    */   {
/* 323:352 */     return (InetSocketAddress)super.localAddress();
/* 324:    */   }
/* 325:    */   
/* 326:    */   public InetSocketAddress remoteAddress()
/* 327:    */   {
/* 328:357 */     return (InetSocketAddress)super.remoteAddress();
/* 329:    */   }
/* 330:    */   
/* 331:    */   public ChannelFuture joinGroup(InetAddress multicastAddress)
/* 332:    */   {
/* 333:362 */     return joinGroup(multicastAddress, newPromise());
/* 334:    */   }
/* 335:    */   
/* 336:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 337:    */   {
/* 338:    */     try
/* 339:    */     {
/* 340:368 */       return joinGroup(multicastAddress, 
/* 341:    */       
/* 342:370 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 343:    */     }
/* 344:    */     catch (SocketException e)
/* 345:    */     {
/* 346:373 */       promise.setFailure(e);
/* 347:    */     }
/* 348:375 */     return promise;
/* 349:    */   }
/* 350:    */   
/* 351:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 352:    */   {
/* 353:381 */     return joinGroup(multicastAddress, networkInterface, newPromise());
/* 354:    */   }
/* 355:    */   
/* 356:    */   public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 357:    */   {
/* 358:388 */     return joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 359:    */   }
/* 360:    */   
/* 361:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 362:    */   {
/* 363:394 */     return joinGroup(multicastAddress, networkInterface, source, newPromise());
/* 364:    */   }
/* 365:    */   
/* 366:    */   public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 367:    */   {
/* 368:    */     
/* 369:404 */     if (multicastAddress == null) {
/* 370:405 */       throw new NullPointerException("multicastAddress");
/* 371:    */     }
/* 372:408 */     if (networkInterface == null) {
/* 373:409 */       throw new NullPointerException("networkInterface");
/* 374:    */     }
/* 375:    */     try
/* 376:    */     {
/* 377:    */       MembershipKey key;
/* 378:    */       MembershipKey key;
/* 379:414 */       if (source == null) {
/* 380:415 */         key = javaChannel().join(multicastAddress, networkInterface);
/* 381:    */       } else {
/* 382:417 */         key = javaChannel().join(multicastAddress, networkInterface, source);
/* 383:    */       }
/* 384:420 */       synchronized (this)
/* 385:    */       {
/* 386:421 */         List<MembershipKey> keys = null;
/* 387:422 */         if (this.memberships == null) {
/* 388:423 */           this.memberships = new HashMap();
/* 389:    */         } else {
/* 390:425 */           keys = (List)this.memberships.get(multicastAddress);
/* 391:    */         }
/* 392:427 */         if (keys == null)
/* 393:    */         {
/* 394:428 */           keys = new ArrayList();
/* 395:429 */           this.memberships.put(multicastAddress, keys);
/* 396:    */         }
/* 397:431 */         keys.add(key);
/* 398:    */       }
/* 399:434 */       promise.setSuccess();
/* 400:    */     }
/* 401:    */     catch (Throwable e)
/* 402:    */     {
/* 403:436 */       promise.setFailure(e);
/* 404:    */     }
/* 405:439 */     return promise;
/* 406:    */   }
/* 407:    */   
/* 408:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress)
/* 409:    */   {
/* 410:444 */     return leaveGroup(multicastAddress, newPromise());
/* 411:    */   }
/* 412:    */   
/* 413:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise)
/* 414:    */   {
/* 415:    */     try
/* 416:    */     {
/* 417:450 */       return leaveGroup(multicastAddress, 
/* 418:451 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), null, promise);
/* 419:    */     }
/* 420:    */     catch (SocketException e)
/* 421:    */     {
/* 422:453 */       promise.setFailure(e);
/* 423:    */     }
/* 424:455 */     return promise;
/* 425:    */   }
/* 426:    */   
/* 427:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface)
/* 428:    */   {
/* 429:461 */     return leaveGroup(multicastAddress, networkInterface, newPromise());
/* 430:    */   }
/* 431:    */   
/* 432:    */   public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise)
/* 433:    */   {
/* 434:468 */     return leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
/* 435:    */   }
/* 436:    */   
/* 437:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source)
/* 438:    */   {
/* 439:474 */     return leaveGroup(multicastAddress, networkInterface, source, newPromise());
/* 440:    */   }
/* 441:    */   
/* 442:    */   public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise)
/* 443:    */   {
/* 444:    */     
/* 445:483 */     if (multicastAddress == null) {
/* 446:484 */       throw new NullPointerException("multicastAddress");
/* 447:    */     }
/* 448:486 */     if (networkInterface == null) {
/* 449:487 */       throw new NullPointerException("networkInterface");
/* 450:    */     }
/* 451:490 */     synchronized (this)
/* 452:    */     {
/* 453:491 */       if (this.memberships != null)
/* 454:    */       {
/* 455:492 */         List<MembershipKey> keys = (List)this.memberships.get(multicastAddress);
/* 456:493 */         if (keys != null)
/* 457:    */         {
/* 458:494 */           Iterator<MembershipKey> keyIt = keys.iterator();
/* 459:496 */           while (keyIt.hasNext())
/* 460:    */           {
/* 461:497 */             MembershipKey key = (MembershipKey)keyIt.next();
/* 462:498 */             if ((networkInterface.equals(key.networkInterface())) && (
/* 463:499 */               ((source == null) && (key.sourceAddress() == null)) || ((source != null) && 
/* 464:500 */               (source.equals(key.sourceAddress())))))
/* 465:    */             {
/* 466:501 */               key.drop();
/* 467:502 */               keyIt.remove();
/* 468:    */             }
/* 469:    */           }
/* 470:506 */           if (keys.isEmpty()) {
/* 471:507 */             this.memberships.remove(multicastAddress);
/* 472:    */           }
/* 473:    */         }
/* 474:    */       }
/* 475:    */     }
/* 476:513 */     promise.setSuccess();
/* 477:514 */     return promise;
/* 478:    */   }
/* 479:    */   
/* 480:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock)
/* 481:    */   {
/* 482:524 */     return block(multicastAddress, networkInterface, sourceToBlock, newPromise());
/* 483:    */   }
/* 484:    */   
/* 485:    */   public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise)
/* 486:    */   {
/* 487:    */     
/* 488:536 */     if (multicastAddress == null) {
/* 489:537 */       throw new NullPointerException("multicastAddress");
/* 490:    */     }
/* 491:539 */     if (sourceToBlock == null) {
/* 492:540 */       throw new NullPointerException("sourceToBlock");
/* 493:    */     }
/* 494:543 */     if (networkInterface == null) {
/* 495:544 */       throw new NullPointerException("networkInterface");
/* 496:    */     }
/* 497:546 */     synchronized (this)
/* 498:    */     {
/* 499:547 */       if (this.memberships != null)
/* 500:    */       {
/* 501:548 */         List<MembershipKey> keys = (List)this.memberships.get(multicastAddress);
/* 502:549 */         for (MembershipKey key : keys) {
/* 503:550 */           if (networkInterface.equals(key.networkInterface())) {
/* 504:    */             try
/* 505:    */             {
/* 506:552 */               key.block(sourceToBlock);
/* 507:    */             }
/* 508:    */             catch (IOException e)
/* 509:    */             {
/* 510:554 */               promise.setFailure(e);
/* 511:    */             }
/* 512:    */           }
/* 513:    */         }
/* 514:    */       }
/* 515:    */     }
/* 516:560 */     promise.setSuccess();
/* 517:561 */     return promise;
/* 518:    */   }
/* 519:    */   
/* 520:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock)
/* 521:    */   {
/* 522:570 */     return block(multicastAddress, sourceToBlock, newPromise());
/* 523:    */   }
/* 524:    */   
/* 525:    */   public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise)
/* 526:    */   {
/* 527:    */     try
/* 528:    */     {
/* 529:581 */       return block(multicastAddress, 
/* 530:    */       
/* 531:583 */         NetworkInterface.getByInetAddress(localAddress().getAddress()), sourceToBlock, promise);
/* 532:    */     }
/* 533:    */     catch (SocketException e)
/* 534:    */     {
/* 535:586 */       promise.setFailure(e);
/* 536:    */     }
/* 537:588 */     return promise;
/* 538:    */   }
/* 539:    */   
/* 540:    */   @Deprecated
/* 541:    */   protected void setReadPending(boolean readPending)
/* 542:    */   {
/* 543:594 */     super.setReadPending(readPending);
/* 544:    */   }
/* 545:    */   
/* 546:    */   void clearReadPending0()
/* 547:    */   {
/* 548:598 */     clearReadPending();
/* 549:    */   }
/* 550:    */   
/* 551:    */   protected boolean closeOnReadError(Throwable cause)
/* 552:    */   {
/* 553:605 */     if ((cause instanceof SocketException)) {
/* 554:606 */       return false;
/* 555:    */     }
/* 556:608 */     return super.closeOnReadError(cause);
/* 557:    */   }
/* 558:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.nio.NioDatagramChannel
 * JD-Core Version:    0.7.0.1
 */