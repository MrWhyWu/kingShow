/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.MessageSizeEstimator;
/*   7:    */ import io.netty.channel.RecvByteBufAllocator;
/*   8:    */ import io.netty.channel.WriteBufferWaterMark;
/*   9:    */ import io.netty.channel.socket.SocketChannelConfig;
/*  10:    */ import io.netty.util.internal.PlatformDependent;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.net.InetAddress;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public final class EpollSocketChannelConfig
/*  16:    */   extends EpollChannelConfig
/*  17:    */   implements SocketChannelConfig
/*  18:    */ {
/*  19:    */   private final EpollSocketChannel channel;
/*  20:    */   private volatile boolean allowHalfClosure;
/*  21:    */   
/*  22:    */   EpollSocketChannelConfig(EpollSocketChannel channel)
/*  23:    */   {
/*  24: 48 */     super(channel);
/*  25:    */     
/*  26: 50 */     this.channel = channel;
/*  27: 51 */     if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
/*  28: 52 */       setTcpNoDelay(true);
/*  29:    */     }
/*  30: 54 */     calculateMaxBytesPerGatheringWrite();
/*  31:    */   }
/*  32:    */   
/*  33:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  34:    */   {
/*  35: 59 */     return getOptions(
/*  36: 60 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE, EpollChannelOption.TCP_CORK, EpollChannelOption.TCP_NOTSENT_LOWAT, EpollChannelOption.TCP_KEEPCNT, EpollChannelOption.TCP_KEEPIDLE, EpollChannelOption.TCP_KEEPINTVL, EpollChannelOption.TCP_MD5SIG, EpollChannelOption.TCP_QUICKACK, EpollChannelOption.IP_TRANSPARENT, EpollChannelOption.TCP_FASTOPEN_CONNECT });
/*  37:    */   }
/*  38:    */   
/*  39:    */   public <T> T getOption(ChannelOption<T> option)
/*  40:    */   {
/*  41: 71 */     if (option == ChannelOption.SO_RCVBUF) {
/*  42: 72 */       return Integer.valueOf(getReceiveBufferSize());
/*  43:    */     }
/*  44: 74 */     if (option == ChannelOption.SO_SNDBUF) {
/*  45: 75 */       return Integer.valueOf(getSendBufferSize());
/*  46:    */     }
/*  47: 77 */     if (option == ChannelOption.TCP_NODELAY) {
/*  48: 78 */       return Boolean.valueOf(isTcpNoDelay());
/*  49:    */     }
/*  50: 80 */     if (option == ChannelOption.SO_KEEPALIVE) {
/*  51: 81 */       return Boolean.valueOf(isKeepAlive());
/*  52:    */     }
/*  53: 83 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  54: 84 */       return Boolean.valueOf(isReuseAddress());
/*  55:    */     }
/*  56: 86 */     if (option == ChannelOption.SO_LINGER) {
/*  57: 87 */       return Integer.valueOf(getSoLinger());
/*  58:    */     }
/*  59: 89 */     if (option == ChannelOption.IP_TOS) {
/*  60: 90 */       return Integer.valueOf(getTrafficClass());
/*  61:    */     }
/*  62: 92 */     if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  63: 93 */       return Boolean.valueOf(isAllowHalfClosure());
/*  64:    */     }
/*  65: 95 */     if (option == EpollChannelOption.TCP_CORK) {
/*  66: 96 */       return Boolean.valueOf(isTcpCork());
/*  67:    */     }
/*  68: 98 */     if (option == EpollChannelOption.TCP_NOTSENT_LOWAT) {
/*  69: 99 */       return Long.valueOf(getTcpNotSentLowAt());
/*  70:    */     }
/*  71:101 */     if (option == EpollChannelOption.TCP_KEEPIDLE) {
/*  72:102 */       return Integer.valueOf(getTcpKeepIdle());
/*  73:    */     }
/*  74:104 */     if (option == EpollChannelOption.TCP_KEEPINTVL) {
/*  75:105 */       return Integer.valueOf(getTcpKeepIntvl());
/*  76:    */     }
/*  77:107 */     if (option == EpollChannelOption.TCP_KEEPCNT) {
/*  78:108 */       return Integer.valueOf(getTcpKeepCnt());
/*  79:    */     }
/*  80:110 */     if (option == EpollChannelOption.TCP_USER_TIMEOUT) {
/*  81:111 */       return Integer.valueOf(getTcpUserTimeout());
/*  82:    */     }
/*  83:113 */     if (option == EpollChannelOption.TCP_QUICKACK) {
/*  84:114 */       return Boolean.valueOf(isTcpQuickAck());
/*  85:    */     }
/*  86:116 */     if (option == EpollChannelOption.IP_TRANSPARENT) {
/*  87:117 */       return Boolean.valueOf(isIpTransparent());
/*  88:    */     }
/*  89:119 */     if (option == EpollChannelOption.TCP_FASTOPEN_CONNECT) {
/*  90:120 */       return Boolean.valueOf(isTcpFastOpenConnect());
/*  91:    */     }
/*  92:122 */     return super.getOption(option);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  96:    */   {
/*  97:127 */     validate(option, value);
/*  98:129 */     if (option == ChannelOption.SO_RCVBUF)
/*  99:    */     {
/* 100:130 */       setReceiveBufferSize(((Integer)value).intValue());
/* 101:    */     }
/* 102:131 */     else if (option == ChannelOption.SO_SNDBUF)
/* 103:    */     {
/* 104:132 */       setSendBufferSize(((Integer)value).intValue());
/* 105:    */     }
/* 106:133 */     else if (option == ChannelOption.TCP_NODELAY)
/* 107:    */     {
/* 108:134 */       setTcpNoDelay(((Boolean)value).booleanValue());
/* 109:    */     }
/* 110:135 */     else if (option == ChannelOption.SO_KEEPALIVE)
/* 111:    */     {
/* 112:136 */       setKeepAlive(((Boolean)value).booleanValue());
/* 113:    */     }
/* 114:137 */     else if (option == ChannelOption.SO_REUSEADDR)
/* 115:    */     {
/* 116:138 */       setReuseAddress(((Boolean)value).booleanValue());
/* 117:    */     }
/* 118:139 */     else if (option == ChannelOption.SO_LINGER)
/* 119:    */     {
/* 120:140 */       setSoLinger(((Integer)value).intValue());
/* 121:    */     }
/* 122:141 */     else if (option == ChannelOption.IP_TOS)
/* 123:    */     {
/* 124:142 */       setTrafficClass(((Integer)value).intValue());
/* 125:    */     }
/* 126:143 */     else if (option == ChannelOption.ALLOW_HALF_CLOSURE)
/* 127:    */     {
/* 128:144 */       setAllowHalfClosure(((Boolean)value).booleanValue());
/* 129:    */     }
/* 130:145 */     else if (option == EpollChannelOption.TCP_CORK)
/* 131:    */     {
/* 132:146 */       setTcpCork(((Boolean)value).booleanValue());
/* 133:    */     }
/* 134:147 */     else if (option == EpollChannelOption.TCP_NOTSENT_LOWAT)
/* 135:    */     {
/* 136:148 */       setTcpNotSentLowAt(((Long)value).longValue());
/* 137:    */     }
/* 138:149 */     else if (option == EpollChannelOption.TCP_KEEPIDLE)
/* 139:    */     {
/* 140:150 */       setTcpKeepIdle(((Integer)value).intValue());
/* 141:    */     }
/* 142:151 */     else if (option == EpollChannelOption.TCP_KEEPCNT)
/* 143:    */     {
/* 144:152 */       setTcpKeepCnt(((Integer)value).intValue());
/* 145:    */     }
/* 146:153 */     else if (option == EpollChannelOption.TCP_KEEPINTVL)
/* 147:    */     {
/* 148:154 */       setTcpKeepIntvl(((Integer)value).intValue());
/* 149:    */     }
/* 150:155 */     else if (option == EpollChannelOption.TCP_USER_TIMEOUT)
/* 151:    */     {
/* 152:156 */       setTcpUserTimeout(((Integer)value).intValue());
/* 153:    */     }
/* 154:157 */     else if (option == EpollChannelOption.IP_TRANSPARENT)
/* 155:    */     {
/* 156:158 */       setIpTransparent(((Boolean)value).booleanValue());
/* 157:    */     }
/* 158:159 */     else if (option == EpollChannelOption.TCP_MD5SIG)
/* 159:    */     {
/* 160:161 */       Map<InetAddress, byte[]> m = (Map)value;
/* 161:162 */       setTcpMd5Sig(m);
/* 162:    */     }
/* 163:163 */     else if (option == EpollChannelOption.TCP_QUICKACK)
/* 164:    */     {
/* 165:164 */       setTcpQuickAck(((Boolean)value).booleanValue());
/* 166:    */     }
/* 167:165 */     else if (option == EpollChannelOption.TCP_FASTOPEN_CONNECT)
/* 168:    */     {
/* 169:166 */       setTcpFastOpenConnect(((Boolean)value).booleanValue());
/* 170:    */     }
/* 171:    */     else
/* 172:    */     {
/* 173:168 */       return super.setOption(option, value);
/* 174:    */     }
/* 175:171 */     return true;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public int getReceiveBufferSize()
/* 179:    */   {
/* 180:    */     try
/* 181:    */     {
/* 182:177 */       return this.channel.socket.getReceiveBufferSize();
/* 183:    */     }
/* 184:    */     catch (IOException e)
/* 185:    */     {
/* 186:179 */       throw new ChannelException(e);
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   public int getSendBufferSize()
/* 191:    */   {
/* 192:    */     try
/* 193:    */     {
/* 194:186 */       return this.channel.socket.getSendBufferSize();
/* 195:    */     }
/* 196:    */     catch (IOException e)
/* 197:    */     {
/* 198:188 */       throw new ChannelException(e);
/* 199:    */     }
/* 200:    */   }
/* 201:    */   
/* 202:    */   public int getSoLinger()
/* 203:    */   {
/* 204:    */     try
/* 205:    */     {
/* 206:195 */       return this.channel.socket.getSoLinger();
/* 207:    */     }
/* 208:    */     catch (IOException e)
/* 209:    */     {
/* 210:197 */       throw new ChannelException(e);
/* 211:    */     }
/* 212:    */   }
/* 213:    */   
/* 214:    */   public int getTrafficClass()
/* 215:    */   {
/* 216:    */     try
/* 217:    */     {
/* 218:204 */       return this.channel.socket.getTrafficClass();
/* 219:    */     }
/* 220:    */     catch (IOException e)
/* 221:    */     {
/* 222:206 */       throw new ChannelException(e);
/* 223:    */     }
/* 224:    */   }
/* 225:    */   
/* 226:    */   public boolean isKeepAlive()
/* 227:    */   {
/* 228:    */     try
/* 229:    */     {
/* 230:213 */       return this.channel.socket.isKeepAlive();
/* 231:    */     }
/* 232:    */     catch (IOException e)
/* 233:    */     {
/* 234:215 */       throw new ChannelException(e);
/* 235:    */     }
/* 236:    */   }
/* 237:    */   
/* 238:    */   public boolean isReuseAddress()
/* 239:    */   {
/* 240:    */     try
/* 241:    */     {
/* 242:222 */       return this.channel.socket.isReuseAddress();
/* 243:    */     }
/* 244:    */     catch (IOException e)
/* 245:    */     {
/* 246:224 */       throw new ChannelException(e);
/* 247:    */     }
/* 248:    */   }
/* 249:    */   
/* 250:    */   public boolean isTcpNoDelay()
/* 251:    */   {
/* 252:    */     try
/* 253:    */     {
/* 254:231 */       return this.channel.socket.isTcpNoDelay();
/* 255:    */     }
/* 256:    */     catch (IOException e)
/* 257:    */     {
/* 258:233 */       throw new ChannelException(e);
/* 259:    */     }
/* 260:    */   }
/* 261:    */   
/* 262:    */   public boolean isTcpCork()
/* 263:    */   {
/* 264:    */     try
/* 265:    */     {
/* 266:242 */       return this.channel.socket.isTcpCork();
/* 267:    */     }
/* 268:    */     catch (IOException e)
/* 269:    */     {
/* 270:244 */       throw new ChannelException(e);
/* 271:    */     }
/* 272:    */   }
/* 273:    */   
/* 274:    */   public long getTcpNotSentLowAt()
/* 275:    */   {
/* 276:    */     try
/* 277:    */     {
/* 278:254 */       return this.channel.socket.getTcpNotSentLowAt();
/* 279:    */     }
/* 280:    */     catch (IOException e)
/* 281:    */     {
/* 282:256 */       throw new ChannelException(e);
/* 283:    */     }
/* 284:    */   }
/* 285:    */   
/* 286:    */   public int getTcpKeepIdle()
/* 287:    */   {
/* 288:    */     try
/* 289:    */     {
/* 290:265 */       return this.channel.socket.getTcpKeepIdle();
/* 291:    */     }
/* 292:    */     catch (IOException e)
/* 293:    */     {
/* 294:267 */       throw new ChannelException(e);
/* 295:    */     }
/* 296:    */   }
/* 297:    */   
/* 298:    */   public int getTcpKeepIntvl()
/* 299:    */   {
/* 300:    */     try
/* 301:    */     {
/* 302:276 */       return this.channel.socket.getTcpKeepIntvl();
/* 303:    */     }
/* 304:    */     catch (IOException e)
/* 305:    */     {
/* 306:278 */       throw new ChannelException(e);
/* 307:    */     }
/* 308:    */   }
/* 309:    */   
/* 310:    */   public int getTcpKeepCnt()
/* 311:    */   {
/* 312:    */     try
/* 313:    */     {
/* 314:287 */       return this.channel.socket.getTcpKeepCnt();
/* 315:    */     }
/* 316:    */     catch (IOException e)
/* 317:    */     {
/* 318:289 */       throw new ChannelException(e);
/* 319:    */     }
/* 320:    */   }
/* 321:    */   
/* 322:    */   public int getTcpUserTimeout()
/* 323:    */   {
/* 324:    */     try
/* 325:    */     {
/* 326:298 */       return this.channel.socket.getTcpUserTimeout();
/* 327:    */     }
/* 328:    */     catch (IOException e)
/* 329:    */     {
/* 330:300 */       throw new ChannelException(e);
/* 331:    */     }
/* 332:    */   }
/* 333:    */   
/* 334:    */   public EpollSocketChannelConfig setKeepAlive(boolean keepAlive)
/* 335:    */   {
/* 336:    */     try
/* 337:    */     {
/* 338:307 */       this.channel.socket.setKeepAlive(keepAlive);
/* 339:308 */       return this;
/* 340:    */     }
/* 341:    */     catch (IOException e)
/* 342:    */     {
/* 343:310 */       throw new ChannelException(e);
/* 344:    */     }
/* 345:    */   }
/* 346:    */   
/* 347:    */   public EpollSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 348:    */   {
/* 349:317 */     return this;
/* 350:    */   }
/* 351:    */   
/* 352:    */   public EpollSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 353:    */   {
/* 354:    */     try
/* 355:    */     {
/* 356:323 */       this.channel.socket.setReceiveBufferSize(receiveBufferSize);
/* 357:324 */       return this;
/* 358:    */     }
/* 359:    */     catch (IOException e)
/* 360:    */     {
/* 361:326 */       throw new ChannelException(e);
/* 362:    */     }
/* 363:    */   }
/* 364:    */   
/* 365:    */   public EpollSocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 366:    */   {
/* 367:    */     try
/* 368:    */     {
/* 369:333 */       this.channel.socket.setReuseAddress(reuseAddress);
/* 370:334 */       return this;
/* 371:    */     }
/* 372:    */     catch (IOException e)
/* 373:    */     {
/* 374:336 */       throw new ChannelException(e);
/* 375:    */     }
/* 376:    */   }
/* 377:    */   
/* 378:    */   public EpollSocketChannelConfig setSendBufferSize(int sendBufferSize)
/* 379:    */   {
/* 380:    */     try
/* 381:    */     {
/* 382:343 */       this.channel.socket.setSendBufferSize(sendBufferSize);
/* 383:344 */       calculateMaxBytesPerGatheringWrite();
/* 384:345 */       return this;
/* 385:    */     }
/* 386:    */     catch (IOException e)
/* 387:    */     {
/* 388:347 */       throw new ChannelException(e);
/* 389:    */     }
/* 390:    */   }
/* 391:    */   
/* 392:    */   public EpollSocketChannelConfig setSoLinger(int soLinger)
/* 393:    */   {
/* 394:    */     try
/* 395:    */     {
/* 396:354 */       this.channel.socket.setSoLinger(soLinger);
/* 397:355 */       return this;
/* 398:    */     }
/* 399:    */     catch (IOException e)
/* 400:    */     {
/* 401:357 */       throw new ChannelException(e);
/* 402:    */     }
/* 403:    */   }
/* 404:    */   
/* 405:    */   public EpollSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/* 406:    */   {
/* 407:    */     try
/* 408:    */     {
/* 409:364 */       this.channel.socket.setTcpNoDelay(tcpNoDelay);
/* 410:365 */       return this;
/* 411:    */     }
/* 412:    */     catch (IOException e)
/* 413:    */     {
/* 414:367 */       throw new ChannelException(e);
/* 415:    */     }
/* 416:    */   }
/* 417:    */   
/* 418:    */   public EpollSocketChannelConfig setTcpCork(boolean tcpCork)
/* 419:    */   {
/* 420:    */     try
/* 421:    */     {
/* 422:376 */       this.channel.socket.setTcpCork(tcpCork);
/* 423:377 */       return this;
/* 424:    */     }
/* 425:    */     catch (IOException e)
/* 426:    */     {
/* 427:379 */       throw new ChannelException(e);
/* 428:    */     }
/* 429:    */   }
/* 430:    */   
/* 431:    */   public EpollSocketChannelConfig setTcpNotSentLowAt(long tcpNotSentLowAt)
/* 432:    */   {
/* 433:    */     try
/* 434:    */     {
/* 435:389 */       this.channel.socket.setTcpNotSentLowAt(tcpNotSentLowAt);
/* 436:390 */       return this;
/* 437:    */     }
/* 438:    */     catch (IOException e)
/* 439:    */     {
/* 440:392 */       throw new ChannelException(e);
/* 441:    */     }
/* 442:    */   }
/* 443:    */   
/* 444:    */   public EpollSocketChannelConfig setTrafficClass(int trafficClass)
/* 445:    */   {
/* 446:    */     try
/* 447:    */     {
/* 448:399 */       this.channel.socket.setTrafficClass(trafficClass);
/* 449:400 */       return this;
/* 450:    */     }
/* 451:    */     catch (IOException e)
/* 452:    */     {
/* 453:402 */       throw new ChannelException(e);
/* 454:    */     }
/* 455:    */   }
/* 456:    */   
/* 457:    */   public EpollSocketChannelConfig setTcpKeepIdle(int seconds)
/* 458:    */   {
/* 459:    */     try
/* 460:    */     {
/* 461:411 */       this.channel.socket.setTcpKeepIdle(seconds);
/* 462:412 */       return this;
/* 463:    */     }
/* 464:    */     catch (IOException e)
/* 465:    */     {
/* 466:414 */       throw new ChannelException(e);
/* 467:    */     }
/* 468:    */   }
/* 469:    */   
/* 470:    */   public EpollSocketChannelConfig setTcpKeepIntvl(int seconds)
/* 471:    */   {
/* 472:    */     try
/* 473:    */     {
/* 474:423 */       this.channel.socket.setTcpKeepIntvl(seconds);
/* 475:424 */       return this;
/* 476:    */     }
/* 477:    */     catch (IOException e)
/* 478:    */     {
/* 479:426 */       throw new ChannelException(e);
/* 480:    */     }
/* 481:    */   }
/* 482:    */   
/* 483:    */   @Deprecated
/* 484:    */   public EpollSocketChannelConfig setTcpKeepCntl(int probes)
/* 485:    */   {
/* 486:435 */     return setTcpKeepCnt(probes);
/* 487:    */   }
/* 488:    */   
/* 489:    */   public EpollSocketChannelConfig setTcpKeepCnt(int probes)
/* 490:    */   {
/* 491:    */     try
/* 492:    */     {
/* 493:443 */       this.channel.socket.setTcpKeepCnt(probes);
/* 494:444 */       return this;
/* 495:    */     }
/* 496:    */     catch (IOException e)
/* 497:    */     {
/* 498:446 */       throw new ChannelException(e);
/* 499:    */     }
/* 500:    */   }
/* 501:    */   
/* 502:    */   public EpollSocketChannelConfig setTcpUserTimeout(int milliseconds)
/* 503:    */   {
/* 504:    */     try
/* 505:    */     {
/* 506:455 */       this.channel.socket.setTcpUserTimeout(milliseconds);
/* 507:456 */       return this;
/* 508:    */     }
/* 509:    */     catch (IOException e)
/* 510:    */     {
/* 511:458 */       throw new ChannelException(e);
/* 512:    */     }
/* 513:    */   }
/* 514:    */   
/* 515:    */   public boolean isIpTransparent()
/* 516:    */   {
/* 517:    */     try
/* 518:    */     {
/* 519:468 */       return this.channel.socket.isIpTransparent();
/* 520:    */     }
/* 521:    */     catch (IOException e)
/* 522:    */     {
/* 523:470 */       throw new ChannelException(e);
/* 524:    */     }
/* 525:    */   }
/* 526:    */   
/* 527:    */   public EpollSocketChannelConfig setIpTransparent(boolean transparent)
/* 528:    */   {
/* 529:    */     try
/* 530:    */     {
/* 531:480 */       this.channel.socket.setIpTransparent(transparent);
/* 532:481 */       return this;
/* 533:    */     }
/* 534:    */     catch (IOException e)
/* 535:    */     {
/* 536:483 */       throw new ChannelException(e);
/* 537:    */     }
/* 538:    */   }
/* 539:    */   
/* 540:    */   public EpollSocketChannelConfig setTcpMd5Sig(Map<InetAddress, byte[]> keys)
/* 541:    */   {
/* 542:    */     try
/* 543:    */     {
/* 544:494 */       this.channel.setTcpMd5Sig(keys);
/* 545:495 */       return this;
/* 546:    */     }
/* 547:    */     catch (IOException e)
/* 548:    */     {
/* 549:497 */       throw new ChannelException(e);
/* 550:    */     }
/* 551:    */   }
/* 552:    */   
/* 553:    */   public EpollSocketChannelConfig setTcpQuickAck(boolean quickAck)
/* 554:    */   {
/* 555:    */     try
/* 556:    */     {
/* 557:507 */       this.channel.socket.setTcpQuickAck(quickAck);
/* 558:508 */       return this;
/* 559:    */     }
/* 560:    */     catch (IOException e)
/* 561:    */     {
/* 562:510 */       throw new ChannelException(e);
/* 563:    */     }
/* 564:    */   }
/* 565:    */   
/* 566:    */   public boolean isTcpQuickAck()
/* 567:    */   {
/* 568:    */     try
/* 569:    */     {
/* 570:520 */       return this.channel.socket.isTcpQuickAck();
/* 571:    */     }
/* 572:    */     catch (IOException e)
/* 573:    */     {
/* 574:522 */       throw new ChannelException(e);
/* 575:    */     }
/* 576:    */   }
/* 577:    */   
/* 578:    */   public EpollSocketChannelConfig setTcpFastOpenConnect(boolean fastOpenConnect)
/* 579:    */   {
/* 580:    */     try
/* 581:    */     {
/* 582:534 */       this.channel.socket.setTcpFastOpenConnect(fastOpenConnect);
/* 583:535 */       return this;
/* 584:    */     }
/* 585:    */     catch (IOException e)
/* 586:    */     {
/* 587:537 */       throw new ChannelException(e);
/* 588:    */     }
/* 589:    */   }
/* 590:    */   
/* 591:    */   public boolean isTcpFastOpenConnect()
/* 592:    */   {
/* 593:    */     try
/* 594:    */     {
/* 595:546 */       return this.channel.socket.isTcpFastOpenConnect();
/* 596:    */     }
/* 597:    */     catch (IOException e)
/* 598:    */     {
/* 599:548 */       throw new ChannelException(e);
/* 600:    */     }
/* 601:    */   }
/* 602:    */   
/* 603:    */   public boolean isAllowHalfClosure()
/* 604:    */   {
/* 605:554 */     return this.allowHalfClosure;
/* 606:    */   }
/* 607:    */   
/* 608:    */   public EpollSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 609:    */   {
/* 610:559 */     this.allowHalfClosure = allowHalfClosure;
/* 611:560 */     return this;
/* 612:    */   }
/* 613:    */   
/* 614:    */   public EpollSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 615:    */   {
/* 616:565 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 617:566 */     return this;
/* 618:    */   }
/* 619:    */   
/* 620:    */   @Deprecated
/* 621:    */   public EpollSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 622:    */   {
/* 623:572 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 624:573 */     return this;
/* 625:    */   }
/* 626:    */   
/* 627:    */   public EpollSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 628:    */   {
/* 629:578 */     super.setWriteSpinCount(writeSpinCount);
/* 630:579 */     return this;
/* 631:    */   }
/* 632:    */   
/* 633:    */   public EpollSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 634:    */   {
/* 635:584 */     super.setAllocator(allocator);
/* 636:585 */     return this;
/* 637:    */   }
/* 638:    */   
/* 639:    */   public EpollSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 640:    */   {
/* 641:590 */     super.setRecvByteBufAllocator(allocator);
/* 642:591 */     return this;
/* 643:    */   }
/* 644:    */   
/* 645:    */   public EpollSocketChannelConfig setAutoRead(boolean autoRead)
/* 646:    */   {
/* 647:596 */     super.setAutoRead(autoRead);
/* 648:597 */     return this;
/* 649:    */   }
/* 650:    */   
/* 651:    */   public EpollSocketChannelConfig setAutoClose(boolean autoClose)
/* 652:    */   {
/* 653:602 */     super.setAutoClose(autoClose);
/* 654:603 */     return this;
/* 655:    */   }
/* 656:    */   
/* 657:    */   @Deprecated
/* 658:    */   public EpollSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 659:    */   {
/* 660:609 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 661:610 */     return this;
/* 662:    */   }
/* 663:    */   
/* 664:    */   @Deprecated
/* 665:    */   public EpollSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 666:    */   {
/* 667:616 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 668:617 */     return this;
/* 669:    */   }
/* 670:    */   
/* 671:    */   public EpollSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 672:    */   {
/* 673:622 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 674:623 */     return this;
/* 675:    */   }
/* 676:    */   
/* 677:    */   public EpollSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 678:    */   {
/* 679:628 */     super.setMessageSizeEstimator(estimator);
/* 680:629 */     return this;
/* 681:    */   }
/* 682:    */   
/* 683:    */   public EpollSocketChannelConfig setEpollMode(EpollMode mode)
/* 684:    */   {
/* 685:634 */     super.setEpollMode(mode);
/* 686:635 */     return this;
/* 687:    */   }
/* 688:    */   
/* 689:    */   private void calculateMaxBytesPerGatheringWrite()
/* 690:    */   {
/* 691:640 */     int newSendBufferSize = getSendBufferSize() << 1;
/* 692:641 */     if (newSendBufferSize > 0) {
/* 693:642 */       setMaxBytesPerGatheringWrite(getSendBufferSize() << 1);
/* 694:    */     }
/* 695:    */   }
/* 696:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */