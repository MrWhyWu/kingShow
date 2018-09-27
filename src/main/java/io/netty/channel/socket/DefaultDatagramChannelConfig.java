/*   1:    */ package io.netty.channel.socket;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelOption;
/*   7:    */ import io.netty.channel.DefaultChannelConfig;
/*   8:    */ import io.netty.channel.FixedRecvByteBufAllocator;
/*   9:    */ import io.netty.channel.MessageSizeEstimator;
/*  10:    */ import io.netty.channel.RecvByteBufAllocator;
/*  11:    */ import io.netty.channel.WriteBufferWaterMark;
/*  12:    */ import io.netty.util.internal.PlatformDependent;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  15:    */ import java.io.IOException;
/*  16:    */ import java.net.DatagramSocket;
/*  17:    */ import java.net.InetAddress;
/*  18:    */ import java.net.MulticastSocket;
/*  19:    */ import java.net.NetworkInterface;
/*  20:    */ import java.net.SocketException;
/*  21:    */ import java.util.Map;
/*  22:    */ 
/*  23:    */ public class DefaultDatagramChannelConfig
/*  24:    */   extends DefaultChannelConfig
/*  25:    */   implements DatagramChannelConfig
/*  26:    */ {
/*  27: 45 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDatagramChannelConfig.class);
/*  28:    */   private final DatagramSocket javaSocket;
/*  29:    */   private volatile boolean activeOnOpen;
/*  30:    */   
/*  31:    */   public DefaultDatagramChannelConfig(DatagramChannel channel, DatagramSocket javaSocket)
/*  32:    */   {
/*  33: 54 */     super(channel, new FixedRecvByteBufAllocator(2048));
/*  34: 55 */     if (javaSocket == null) {
/*  35: 56 */       throw new NullPointerException("javaSocket");
/*  36:    */     }
/*  37: 58 */     this.javaSocket = javaSocket;
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected final DatagramSocket javaSocket()
/*  41:    */   {
/*  42: 62 */     return this.javaSocket;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  46:    */   {
/*  47: 68 */     return getOptions(
/*  48: 69 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_BROADCAST, ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.SO_REUSEADDR, ChannelOption.IP_MULTICAST_LOOP_DISABLED, ChannelOption.IP_MULTICAST_ADDR, ChannelOption.IP_MULTICAST_IF, ChannelOption.IP_MULTICAST_TTL, ChannelOption.IP_TOS, ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION });
/*  49:    */   }
/*  50:    */   
/*  51:    */   public <T> T getOption(ChannelOption<T> option)
/*  52:    */   {
/*  53: 77 */     if (option == ChannelOption.SO_BROADCAST) {
/*  54: 78 */       return Boolean.valueOf(isBroadcast());
/*  55:    */     }
/*  56: 80 */     if (option == ChannelOption.SO_RCVBUF) {
/*  57: 81 */       return Integer.valueOf(getReceiveBufferSize());
/*  58:    */     }
/*  59: 83 */     if (option == ChannelOption.SO_SNDBUF) {
/*  60: 84 */       return Integer.valueOf(getSendBufferSize());
/*  61:    */     }
/*  62: 86 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  63: 87 */       return Boolean.valueOf(isReuseAddress());
/*  64:    */     }
/*  65: 89 */     if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  66: 90 */       return Boolean.valueOf(isLoopbackModeDisabled());
/*  67:    */     }
/*  68: 92 */     if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  69: 93 */       return getInterface();
/*  70:    */     }
/*  71: 95 */     if (option == ChannelOption.IP_MULTICAST_IF) {
/*  72: 96 */       return getNetworkInterface();
/*  73:    */     }
/*  74: 98 */     if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  75: 99 */       return Integer.valueOf(getTimeToLive());
/*  76:    */     }
/*  77:101 */     if (option == ChannelOption.IP_TOS) {
/*  78:102 */       return Integer.valueOf(getTrafficClass());
/*  79:    */     }
/*  80:104 */     if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  81:105 */       return Boolean.valueOf(this.activeOnOpen);
/*  82:    */     }
/*  83:107 */     return super.getOption(option);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  87:    */   {
/*  88:113 */     validate(option, value);
/*  89:115 */     if (option == ChannelOption.SO_BROADCAST) {
/*  90:116 */       setBroadcast(((Boolean)value).booleanValue());
/*  91:117 */     } else if (option == ChannelOption.SO_RCVBUF) {
/*  92:118 */       setReceiveBufferSize(((Integer)value).intValue());
/*  93:119 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  94:120 */       setSendBufferSize(((Integer)value).intValue());
/*  95:121 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  96:122 */       setReuseAddress(((Boolean)value).booleanValue());
/*  97:123 */     } else if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  98:124 */       setLoopbackModeDisabled(((Boolean)value).booleanValue());
/*  99:125 */     } else if (option == ChannelOption.IP_MULTICAST_ADDR) {
/* 100:126 */       setInterface((InetAddress)value);
/* 101:127 */     } else if (option == ChannelOption.IP_MULTICAST_IF) {
/* 102:128 */       setNetworkInterface((NetworkInterface)value);
/* 103:129 */     } else if (option == ChannelOption.IP_MULTICAST_TTL) {
/* 104:130 */       setTimeToLive(((Integer)value).intValue());
/* 105:131 */     } else if (option == ChannelOption.IP_TOS) {
/* 106:132 */       setTrafficClass(((Integer)value).intValue());
/* 107:133 */     } else if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/* 108:134 */       setActiveOnOpen(((Boolean)value).booleanValue());
/* 109:    */     } else {
/* 110:136 */       return super.setOption(option, value);
/* 111:    */     }
/* 112:139 */     return true;
/* 113:    */   }
/* 114:    */   
/* 115:    */   private void setActiveOnOpen(boolean activeOnOpen)
/* 116:    */   {
/* 117:143 */     if (this.channel.isRegistered()) {
/* 118:144 */       throw new IllegalStateException("Can only changed before channel was registered");
/* 119:    */     }
/* 120:146 */     this.activeOnOpen = activeOnOpen;
/* 121:    */   }
/* 122:    */   
/* 123:    */   public boolean isBroadcast()
/* 124:    */   {
/* 125:    */     try
/* 126:    */     {
/* 127:152 */       return this.javaSocket.getBroadcast();
/* 128:    */     }
/* 129:    */     catch (SocketException e)
/* 130:    */     {
/* 131:154 */       throw new ChannelException(e);
/* 132:    */     }
/* 133:    */   }
/* 134:    */   
/* 135:    */   public DatagramChannelConfig setBroadcast(boolean broadcast)
/* 136:    */   {
/* 137:    */     try
/* 138:    */     {
/* 139:162 */       if ((broadcast) && 
/* 140:163 */         (!this.javaSocket.getLocalAddress().isAnyLocalAddress()) && 
/* 141:164 */         (!PlatformDependent.isWindows()) && (!PlatformDependent.maybeSuperUser())) {
/* 142:167 */         logger.warn("A non-root user can't receive a broadcast packet if the socket is not bound to a wildcard address; setting the SO_BROADCAST flag anyway as requested on the socket which is bound to " + this.javaSocket
/* 143:    */         
/* 144:    */ 
/* 145:    */ 
/* 146:171 */           .getLocalSocketAddress() + '.');
/* 147:    */       }
/* 148:174 */       this.javaSocket.setBroadcast(broadcast);
/* 149:    */     }
/* 150:    */     catch (SocketException e)
/* 151:    */     {
/* 152:176 */       throw new ChannelException(e);
/* 153:    */     }
/* 154:178 */     return this;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public InetAddress getInterface()
/* 158:    */   {
/* 159:183 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 160:    */       try
/* 161:    */       {
/* 162:185 */         return ((MulticastSocket)this.javaSocket).getInterface();
/* 163:    */       }
/* 164:    */       catch (SocketException e)
/* 165:    */       {
/* 166:187 */         throw new ChannelException(e);
/* 167:    */       }
/* 168:    */     }
/* 169:190 */     throw new UnsupportedOperationException();
/* 170:    */   }
/* 171:    */   
/* 172:    */   public DatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 173:    */   {
/* 174:196 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 175:    */       try
/* 176:    */       {
/* 177:198 */         ((MulticastSocket)this.javaSocket).setInterface(interfaceAddress);
/* 178:    */       }
/* 179:    */       catch (SocketException e)
/* 180:    */       {
/* 181:200 */         throw new ChannelException(e);
/* 182:    */       }
/* 183:    */     } else {
/* 184:203 */       throw new UnsupportedOperationException();
/* 185:    */     }
/* 186:205 */     return this;
/* 187:    */   }
/* 188:    */   
/* 189:    */   public boolean isLoopbackModeDisabled()
/* 190:    */   {
/* 191:210 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 192:    */       try
/* 193:    */       {
/* 194:212 */         return ((MulticastSocket)this.javaSocket).getLoopbackMode();
/* 195:    */       }
/* 196:    */       catch (SocketException e)
/* 197:    */       {
/* 198:214 */         throw new ChannelException(e);
/* 199:    */       }
/* 200:    */     }
/* 201:217 */     throw new UnsupportedOperationException();
/* 202:    */   }
/* 203:    */   
/* 204:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 205:    */   {
/* 206:223 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 207:    */       try
/* 208:    */       {
/* 209:225 */         ((MulticastSocket)this.javaSocket).setLoopbackMode(loopbackModeDisabled);
/* 210:    */       }
/* 211:    */       catch (SocketException e)
/* 212:    */       {
/* 213:227 */         throw new ChannelException(e);
/* 214:    */       }
/* 215:    */     } else {
/* 216:230 */       throw new UnsupportedOperationException();
/* 217:    */     }
/* 218:232 */     return this;
/* 219:    */   }
/* 220:    */   
/* 221:    */   public NetworkInterface getNetworkInterface()
/* 222:    */   {
/* 223:237 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 224:    */       try
/* 225:    */       {
/* 226:239 */         return ((MulticastSocket)this.javaSocket).getNetworkInterface();
/* 227:    */       }
/* 228:    */       catch (SocketException e)
/* 229:    */       {
/* 230:241 */         throw new ChannelException(e);
/* 231:    */       }
/* 232:    */     }
/* 233:244 */     throw new UnsupportedOperationException();
/* 234:    */   }
/* 235:    */   
/* 236:    */   public DatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 237:    */   {
/* 238:250 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 239:    */       try
/* 240:    */       {
/* 241:252 */         ((MulticastSocket)this.javaSocket).setNetworkInterface(networkInterface);
/* 242:    */       }
/* 243:    */       catch (SocketException e)
/* 244:    */       {
/* 245:254 */         throw new ChannelException(e);
/* 246:    */       }
/* 247:    */     } else {
/* 248:257 */       throw new UnsupportedOperationException();
/* 249:    */     }
/* 250:259 */     return this;
/* 251:    */   }
/* 252:    */   
/* 253:    */   public boolean isReuseAddress()
/* 254:    */   {
/* 255:    */     try
/* 256:    */     {
/* 257:265 */       return this.javaSocket.getReuseAddress();
/* 258:    */     }
/* 259:    */     catch (SocketException e)
/* 260:    */     {
/* 261:267 */       throw new ChannelException(e);
/* 262:    */     }
/* 263:    */   }
/* 264:    */   
/* 265:    */   public DatagramChannelConfig setReuseAddress(boolean reuseAddress)
/* 266:    */   {
/* 267:    */     try
/* 268:    */     {
/* 269:274 */       this.javaSocket.setReuseAddress(reuseAddress);
/* 270:    */     }
/* 271:    */     catch (SocketException e)
/* 272:    */     {
/* 273:276 */       throw new ChannelException(e);
/* 274:    */     }
/* 275:278 */     return this;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public int getReceiveBufferSize()
/* 279:    */   {
/* 280:    */     try
/* 281:    */     {
/* 282:284 */       return this.javaSocket.getReceiveBufferSize();
/* 283:    */     }
/* 284:    */     catch (SocketException e)
/* 285:    */     {
/* 286:286 */       throw new ChannelException(e);
/* 287:    */     }
/* 288:    */   }
/* 289:    */   
/* 290:    */   public DatagramChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 291:    */   {
/* 292:    */     try
/* 293:    */     {
/* 294:293 */       this.javaSocket.setReceiveBufferSize(receiveBufferSize);
/* 295:    */     }
/* 296:    */     catch (SocketException e)
/* 297:    */     {
/* 298:295 */       throw new ChannelException(e);
/* 299:    */     }
/* 300:297 */     return this;
/* 301:    */   }
/* 302:    */   
/* 303:    */   public int getSendBufferSize()
/* 304:    */   {
/* 305:    */     try
/* 306:    */     {
/* 307:303 */       return this.javaSocket.getSendBufferSize();
/* 308:    */     }
/* 309:    */     catch (SocketException e)
/* 310:    */     {
/* 311:305 */       throw new ChannelException(e);
/* 312:    */     }
/* 313:    */   }
/* 314:    */   
/* 315:    */   public DatagramChannelConfig setSendBufferSize(int sendBufferSize)
/* 316:    */   {
/* 317:    */     try
/* 318:    */     {
/* 319:312 */       this.javaSocket.setSendBufferSize(sendBufferSize);
/* 320:    */     }
/* 321:    */     catch (SocketException e)
/* 322:    */     {
/* 323:314 */       throw new ChannelException(e);
/* 324:    */     }
/* 325:316 */     return this;
/* 326:    */   }
/* 327:    */   
/* 328:    */   public int getTimeToLive()
/* 329:    */   {
/* 330:321 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 331:    */       try
/* 332:    */       {
/* 333:323 */         return ((MulticastSocket)this.javaSocket).getTimeToLive();
/* 334:    */       }
/* 335:    */       catch (IOException e)
/* 336:    */       {
/* 337:325 */         throw new ChannelException(e);
/* 338:    */       }
/* 339:    */     }
/* 340:328 */     throw new UnsupportedOperationException();
/* 341:    */   }
/* 342:    */   
/* 343:    */   public DatagramChannelConfig setTimeToLive(int ttl)
/* 344:    */   {
/* 345:334 */     if ((this.javaSocket instanceof MulticastSocket)) {
/* 346:    */       try
/* 347:    */       {
/* 348:336 */         ((MulticastSocket)this.javaSocket).setTimeToLive(ttl);
/* 349:    */       }
/* 350:    */       catch (IOException e)
/* 351:    */       {
/* 352:338 */         throw new ChannelException(e);
/* 353:    */       }
/* 354:    */     } else {
/* 355:341 */       throw new UnsupportedOperationException();
/* 356:    */     }
/* 357:343 */     return this;
/* 358:    */   }
/* 359:    */   
/* 360:    */   public int getTrafficClass()
/* 361:    */   {
/* 362:    */     try
/* 363:    */     {
/* 364:349 */       return this.javaSocket.getTrafficClass();
/* 365:    */     }
/* 366:    */     catch (SocketException e)
/* 367:    */     {
/* 368:351 */       throw new ChannelException(e);
/* 369:    */     }
/* 370:    */   }
/* 371:    */   
/* 372:    */   public DatagramChannelConfig setTrafficClass(int trafficClass)
/* 373:    */   {
/* 374:    */     try
/* 375:    */     {
/* 376:358 */       this.javaSocket.setTrafficClass(trafficClass);
/* 377:    */     }
/* 378:    */     catch (SocketException e)
/* 379:    */     {
/* 380:360 */       throw new ChannelException(e);
/* 381:    */     }
/* 382:362 */     return this;
/* 383:    */   }
/* 384:    */   
/* 385:    */   public DatagramChannelConfig setWriteSpinCount(int writeSpinCount)
/* 386:    */   {
/* 387:367 */     super.setWriteSpinCount(writeSpinCount);
/* 388:368 */     return this;
/* 389:    */   }
/* 390:    */   
/* 391:    */   public DatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 392:    */   {
/* 393:373 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 394:374 */     return this;
/* 395:    */   }
/* 396:    */   
/* 397:    */   @Deprecated
/* 398:    */   public DatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 399:    */   {
/* 400:380 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 401:381 */     return this;
/* 402:    */   }
/* 403:    */   
/* 404:    */   public DatagramChannelConfig setAllocator(ByteBufAllocator allocator)
/* 405:    */   {
/* 406:386 */     super.setAllocator(allocator);
/* 407:387 */     return this;
/* 408:    */   }
/* 409:    */   
/* 410:    */   public DatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 411:    */   {
/* 412:392 */     super.setRecvByteBufAllocator(allocator);
/* 413:393 */     return this;
/* 414:    */   }
/* 415:    */   
/* 416:    */   public DatagramChannelConfig setAutoRead(boolean autoRead)
/* 417:    */   {
/* 418:398 */     super.setAutoRead(autoRead);
/* 419:399 */     return this;
/* 420:    */   }
/* 421:    */   
/* 422:    */   public DatagramChannelConfig setAutoClose(boolean autoClose)
/* 423:    */   {
/* 424:404 */     super.setAutoClose(autoClose);
/* 425:405 */     return this;
/* 426:    */   }
/* 427:    */   
/* 428:    */   public DatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 429:    */   {
/* 430:410 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 431:411 */     return this;
/* 432:    */   }
/* 433:    */   
/* 434:    */   public DatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 435:    */   {
/* 436:416 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 437:417 */     return this;
/* 438:    */   }
/* 439:    */   
/* 440:    */   public DatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 441:    */   {
/* 442:422 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 443:423 */     return this;
/* 444:    */   }
/* 445:    */   
/* 446:    */   public DatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 447:    */   {
/* 448:428 */     super.setMessageSizeEstimator(estimator);
/* 449:429 */     return this;
/* 450:    */   }
/* 451:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.DefaultDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */