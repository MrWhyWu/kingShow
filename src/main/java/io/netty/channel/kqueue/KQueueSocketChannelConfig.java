/*   1:    */ package io.netty.channel.kqueue;
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
/*  12:    */ import java.util.Map;
/*  13:    */ 
/*  14:    */ public final class KQueueSocketChannelConfig
/*  15:    */   extends KQueueChannelConfig
/*  16:    */   implements SocketChannelConfig
/*  17:    */ {
/*  18:    */   private final KQueueSocketChannel channel;
/*  19:    */   private volatile boolean allowHalfClosure;
/*  20:    */   
/*  21:    */   KQueueSocketChannelConfig(KQueueSocketChannel channel)
/*  22:    */   {
/*  23: 48 */     super(channel);
/*  24: 49 */     this.channel = channel;
/*  25: 50 */     if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
/*  26: 51 */       setTcpNoDelay(true);
/*  27:    */     }
/*  28: 53 */     calculateMaxBytesPerGatheringWrite();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  32:    */   {
/*  33: 58 */     return getOptions(
/*  34: 59 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE, KQueueChannelOption.SO_SNDLOWAT, KQueueChannelOption.TCP_NOPUSH });
/*  35:    */   }
/*  36:    */   
/*  37:    */   public <T> T getOption(ChannelOption<T> option)
/*  38:    */   {
/*  39: 67 */     if (option == ChannelOption.SO_RCVBUF) {
/*  40: 68 */       return Integer.valueOf(getReceiveBufferSize());
/*  41:    */     }
/*  42: 70 */     if (option == ChannelOption.SO_SNDBUF) {
/*  43: 71 */       return Integer.valueOf(getSendBufferSize());
/*  44:    */     }
/*  45: 73 */     if (option == ChannelOption.TCP_NODELAY) {
/*  46: 74 */       return Boolean.valueOf(isTcpNoDelay());
/*  47:    */     }
/*  48: 76 */     if (option == ChannelOption.SO_KEEPALIVE) {
/*  49: 77 */       return Boolean.valueOf(isKeepAlive());
/*  50:    */     }
/*  51: 79 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  52: 80 */       return Boolean.valueOf(isReuseAddress());
/*  53:    */     }
/*  54: 82 */     if (option == ChannelOption.SO_LINGER) {
/*  55: 83 */       return Integer.valueOf(getSoLinger());
/*  56:    */     }
/*  57: 85 */     if (option == ChannelOption.IP_TOS) {
/*  58: 86 */       return Integer.valueOf(getTrafficClass());
/*  59:    */     }
/*  60: 88 */     if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  61: 89 */       return Boolean.valueOf(isAllowHalfClosure());
/*  62:    */     }
/*  63: 91 */     if (option == KQueueChannelOption.SO_SNDLOWAT) {
/*  64: 92 */       return Integer.valueOf(getSndLowAt());
/*  65:    */     }
/*  66: 94 */     if (option == KQueueChannelOption.TCP_NOPUSH) {
/*  67: 95 */       return Boolean.valueOf(isTcpNoPush());
/*  68:    */     }
/*  69: 97 */     return super.getOption(option);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  73:    */   {
/*  74:102 */     validate(option, value);
/*  75:104 */     if (option == ChannelOption.SO_RCVBUF) {
/*  76:105 */       setReceiveBufferSize(((Integer)value).intValue());
/*  77:106 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  78:107 */       setSendBufferSize(((Integer)value).intValue());
/*  79:108 */     } else if (option == ChannelOption.TCP_NODELAY) {
/*  80:109 */       setTcpNoDelay(((Boolean)value).booleanValue());
/*  81:110 */     } else if (option == ChannelOption.SO_KEEPALIVE) {
/*  82:111 */       setKeepAlive(((Boolean)value).booleanValue());
/*  83:112 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  84:113 */       setReuseAddress(((Boolean)value).booleanValue());
/*  85:114 */     } else if (option == ChannelOption.SO_LINGER) {
/*  86:115 */       setSoLinger(((Integer)value).intValue());
/*  87:116 */     } else if (option == ChannelOption.IP_TOS) {
/*  88:117 */       setTrafficClass(((Integer)value).intValue());
/*  89:118 */     } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  90:119 */       setAllowHalfClosure(((Boolean)value).booleanValue());
/*  91:120 */     } else if (option == KQueueChannelOption.SO_SNDLOWAT) {
/*  92:121 */       setSndLowAt(((Integer)value).intValue());
/*  93:122 */     } else if (option == KQueueChannelOption.TCP_NOPUSH) {
/*  94:123 */       setTcpNoPush(((Boolean)value).booleanValue());
/*  95:    */     } else {
/*  96:125 */       return super.setOption(option, value);
/*  97:    */     }
/*  98:128 */     return true;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public int getReceiveBufferSize()
/* 102:    */   {
/* 103:    */     try
/* 104:    */     {
/* 105:134 */       return this.channel.socket.getReceiveBufferSize();
/* 106:    */     }
/* 107:    */     catch (IOException e)
/* 108:    */     {
/* 109:136 */       throw new ChannelException(e);
/* 110:    */     }
/* 111:    */   }
/* 112:    */   
/* 113:    */   public int getSendBufferSize()
/* 114:    */   {
/* 115:    */     try
/* 116:    */     {
/* 117:143 */       return this.channel.socket.getSendBufferSize();
/* 118:    */     }
/* 119:    */     catch (IOException e)
/* 120:    */     {
/* 121:145 */       throw new ChannelException(e);
/* 122:    */     }
/* 123:    */   }
/* 124:    */   
/* 125:    */   public int getSoLinger()
/* 126:    */   {
/* 127:    */     try
/* 128:    */     {
/* 129:152 */       return this.channel.socket.getSoLinger();
/* 130:    */     }
/* 131:    */     catch (IOException e)
/* 132:    */     {
/* 133:154 */       throw new ChannelException(e);
/* 134:    */     }
/* 135:    */   }
/* 136:    */   
/* 137:    */   public int getTrafficClass()
/* 138:    */   {
/* 139:    */     try
/* 140:    */     {
/* 141:161 */       return this.channel.socket.getTrafficClass();
/* 142:    */     }
/* 143:    */     catch (IOException e)
/* 144:    */     {
/* 145:163 */       throw new ChannelException(e);
/* 146:    */     }
/* 147:    */   }
/* 148:    */   
/* 149:    */   public boolean isKeepAlive()
/* 150:    */   {
/* 151:    */     try
/* 152:    */     {
/* 153:170 */       return this.channel.socket.isKeepAlive();
/* 154:    */     }
/* 155:    */     catch (IOException e)
/* 156:    */     {
/* 157:172 */       throw new ChannelException(e);
/* 158:    */     }
/* 159:    */   }
/* 160:    */   
/* 161:    */   public boolean isReuseAddress()
/* 162:    */   {
/* 163:    */     try
/* 164:    */     {
/* 165:179 */       return this.channel.socket.isReuseAddress();
/* 166:    */     }
/* 167:    */     catch (IOException e)
/* 168:    */     {
/* 169:181 */       throw new ChannelException(e);
/* 170:    */     }
/* 171:    */   }
/* 172:    */   
/* 173:    */   public boolean isTcpNoDelay()
/* 174:    */   {
/* 175:    */     try
/* 176:    */     {
/* 177:188 */       return this.channel.socket.isTcpNoDelay();
/* 178:    */     }
/* 179:    */     catch (IOException e)
/* 180:    */     {
/* 181:190 */       throw new ChannelException(e);
/* 182:    */     }
/* 183:    */   }
/* 184:    */   
/* 185:    */   public int getSndLowAt()
/* 186:    */   {
/* 187:    */     try
/* 188:    */     {
/* 189:196 */       return this.channel.socket.getSndLowAt();
/* 190:    */     }
/* 191:    */     catch (IOException e)
/* 192:    */     {
/* 193:198 */       throw new ChannelException(e);
/* 194:    */     }
/* 195:    */   }
/* 196:    */   
/* 197:    */   public void setSndLowAt(int sndLowAt)
/* 198:    */   {
/* 199:    */     try
/* 200:    */     {
/* 201:204 */       this.channel.socket.setSndLowAt(sndLowAt);
/* 202:    */     }
/* 203:    */     catch (IOException e)
/* 204:    */     {
/* 205:206 */       throw new ChannelException(e);
/* 206:    */     }
/* 207:    */   }
/* 208:    */   
/* 209:    */   public boolean isTcpNoPush()
/* 210:    */   {
/* 211:    */     try
/* 212:    */     {
/* 213:212 */       return this.channel.socket.isTcpNoPush();
/* 214:    */     }
/* 215:    */     catch (IOException e)
/* 216:    */     {
/* 217:214 */       throw new ChannelException(e);
/* 218:    */     }
/* 219:    */   }
/* 220:    */   
/* 221:    */   public void setTcpNoPush(boolean tcpNoPush)
/* 222:    */   {
/* 223:    */     try
/* 224:    */     {
/* 225:220 */       this.channel.socket.setTcpNoPush(tcpNoPush);
/* 226:    */     }
/* 227:    */     catch (IOException e)
/* 228:    */     {
/* 229:222 */       throw new ChannelException(e);
/* 230:    */     }
/* 231:    */   }
/* 232:    */   
/* 233:    */   public KQueueSocketChannelConfig setKeepAlive(boolean keepAlive)
/* 234:    */   {
/* 235:    */     try
/* 236:    */     {
/* 237:229 */       this.channel.socket.setKeepAlive(keepAlive);
/* 238:230 */       return this;
/* 239:    */     }
/* 240:    */     catch (IOException e)
/* 241:    */     {
/* 242:232 */       throw new ChannelException(e);
/* 243:    */     }
/* 244:    */   }
/* 245:    */   
/* 246:    */   public KQueueSocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 247:    */   {
/* 248:    */     try
/* 249:    */     {
/* 250:239 */       this.channel.socket.setReceiveBufferSize(receiveBufferSize);
/* 251:240 */       return this;
/* 252:    */     }
/* 253:    */     catch (IOException e)
/* 254:    */     {
/* 255:242 */       throw new ChannelException(e);
/* 256:    */     }
/* 257:    */   }
/* 258:    */   
/* 259:    */   public KQueueSocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 260:    */   {
/* 261:    */     try
/* 262:    */     {
/* 263:249 */       this.channel.socket.setReuseAddress(reuseAddress);
/* 264:250 */       return this;
/* 265:    */     }
/* 266:    */     catch (IOException e)
/* 267:    */     {
/* 268:252 */       throw new ChannelException(e);
/* 269:    */     }
/* 270:    */   }
/* 271:    */   
/* 272:    */   public KQueueSocketChannelConfig setSendBufferSize(int sendBufferSize)
/* 273:    */   {
/* 274:    */     try
/* 275:    */     {
/* 276:259 */       this.channel.socket.setSendBufferSize(sendBufferSize);
/* 277:260 */       calculateMaxBytesPerGatheringWrite();
/* 278:261 */       return this;
/* 279:    */     }
/* 280:    */     catch (IOException e)
/* 281:    */     {
/* 282:263 */       throw new ChannelException(e);
/* 283:    */     }
/* 284:    */   }
/* 285:    */   
/* 286:    */   public KQueueSocketChannelConfig setSoLinger(int soLinger)
/* 287:    */   {
/* 288:    */     try
/* 289:    */     {
/* 290:270 */       this.channel.socket.setSoLinger(soLinger);
/* 291:271 */       return this;
/* 292:    */     }
/* 293:    */     catch (IOException e)
/* 294:    */     {
/* 295:273 */       throw new ChannelException(e);
/* 296:    */     }
/* 297:    */   }
/* 298:    */   
/* 299:    */   public KQueueSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/* 300:    */   {
/* 301:    */     try
/* 302:    */     {
/* 303:280 */       this.channel.socket.setTcpNoDelay(tcpNoDelay);
/* 304:281 */       return this;
/* 305:    */     }
/* 306:    */     catch (IOException e)
/* 307:    */     {
/* 308:283 */       throw new ChannelException(e);
/* 309:    */     }
/* 310:    */   }
/* 311:    */   
/* 312:    */   public KQueueSocketChannelConfig setTrafficClass(int trafficClass)
/* 313:    */   {
/* 314:    */     try
/* 315:    */     {
/* 316:290 */       this.channel.socket.setTrafficClass(trafficClass);
/* 317:291 */       return this;
/* 318:    */     }
/* 319:    */     catch (IOException e)
/* 320:    */     {
/* 321:293 */       throw new ChannelException(e);
/* 322:    */     }
/* 323:    */   }
/* 324:    */   
/* 325:    */   public boolean isAllowHalfClosure()
/* 326:    */   {
/* 327:299 */     return this.allowHalfClosure;
/* 328:    */   }
/* 329:    */   
/* 330:    */   public KQueueSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess)
/* 331:    */   {
/* 332:304 */     super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
/* 333:305 */     return this;
/* 334:    */   }
/* 335:    */   
/* 336:    */   public KQueueSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 337:    */   {
/* 338:311 */     return this;
/* 339:    */   }
/* 340:    */   
/* 341:    */   public KQueueSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 342:    */   {
/* 343:316 */     this.allowHalfClosure = allowHalfClosure;
/* 344:317 */     return this;
/* 345:    */   }
/* 346:    */   
/* 347:    */   public KQueueSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 348:    */   {
/* 349:322 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 350:323 */     return this;
/* 351:    */   }
/* 352:    */   
/* 353:    */   @Deprecated
/* 354:    */   public KQueueSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 355:    */   {
/* 356:329 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 357:330 */     return this;
/* 358:    */   }
/* 359:    */   
/* 360:    */   public KQueueSocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 361:    */   {
/* 362:335 */     super.setWriteSpinCount(writeSpinCount);
/* 363:336 */     return this;
/* 364:    */   }
/* 365:    */   
/* 366:    */   public KQueueSocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 367:    */   {
/* 368:341 */     super.setAllocator(allocator);
/* 369:342 */     return this;
/* 370:    */   }
/* 371:    */   
/* 372:    */   public KQueueSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 373:    */   {
/* 374:347 */     super.setRecvByteBufAllocator(allocator);
/* 375:348 */     return this;
/* 376:    */   }
/* 377:    */   
/* 378:    */   public KQueueSocketChannelConfig setAutoRead(boolean autoRead)
/* 379:    */   {
/* 380:353 */     super.setAutoRead(autoRead);
/* 381:354 */     return this;
/* 382:    */   }
/* 383:    */   
/* 384:    */   public KQueueSocketChannelConfig setAutoClose(boolean autoClose)
/* 385:    */   {
/* 386:359 */     super.setAutoClose(autoClose);
/* 387:360 */     return this;
/* 388:    */   }
/* 389:    */   
/* 390:    */   @Deprecated
/* 391:    */   public KQueueSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 392:    */   {
/* 393:366 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 394:367 */     return this;
/* 395:    */   }
/* 396:    */   
/* 397:    */   @Deprecated
/* 398:    */   public KQueueSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 399:    */   {
/* 400:373 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 401:374 */     return this;
/* 402:    */   }
/* 403:    */   
/* 404:    */   public KQueueSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 405:    */   {
/* 406:379 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 407:380 */     return this;
/* 408:    */   }
/* 409:    */   
/* 410:    */   public KQueueSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 411:    */   {
/* 412:385 */     super.setMessageSizeEstimator(estimator);
/* 413:386 */     return this;
/* 414:    */   }
/* 415:    */   
/* 416:    */   private void calculateMaxBytesPerGatheringWrite()
/* 417:    */   {
/* 418:391 */     int newSendBufferSize = getSendBufferSize() << 1;
/* 419:392 */     if (newSendBufferSize > 0) {
/* 420:393 */       setMaxBytesPerGatheringWrite(getSendBufferSize() << 1);
/* 421:    */     }
/* 422:    */   }
/* 423:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */