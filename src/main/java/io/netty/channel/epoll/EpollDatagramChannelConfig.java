/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.FixedRecvByteBufAllocator;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  11:    */ import java.io.IOException;
/*  12:    */ import java.net.InetAddress;
/*  13:    */ import java.net.NetworkInterface;
/*  14:    */ import java.util.Map;
/*  15:    */ 
/*  16:    */ public final class EpollDatagramChannelConfig
/*  17:    */   extends EpollChannelConfig
/*  18:    */   implements DatagramChannelConfig
/*  19:    */ {
/*  20: 32 */   private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = new FixedRecvByteBufAllocator(2048);
/*  21:    */   private final EpollDatagramChannel datagramChannel;
/*  22:    */   private boolean activeOnOpen;
/*  23:    */   
/*  24:    */   EpollDatagramChannelConfig(EpollDatagramChannel channel)
/*  25:    */   {
/*  26: 37 */     super(channel);
/*  27: 38 */     this.datagramChannel = channel;
/*  28: 39 */     setRecvByteBufAllocator(DEFAULT_RCVBUF_ALLOCATOR);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  32:    */   {
/*  33: 45 */     return getOptions(
/*  34: 46 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_BROADCAST, ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.SO_REUSEADDR, ChannelOption.IP_MULTICAST_LOOP_DISABLED, ChannelOption.IP_MULTICAST_ADDR, ChannelOption.IP_MULTICAST_IF, ChannelOption.IP_MULTICAST_TTL, ChannelOption.IP_TOS, ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, EpollChannelOption.SO_REUSEPORT });
/*  35:    */   }
/*  36:    */   
/*  37:    */   public <T> T getOption(ChannelOption<T> option)
/*  38:    */   {
/*  39: 57 */     if (option == ChannelOption.SO_BROADCAST) {
/*  40: 58 */       return Boolean.valueOf(isBroadcast());
/*  41:    */     }
/*  42: 60 */     if (option == ChannelOption.SO_RCVBUF) {
/*  43: 61 */       return Integer.valueOf(getReceiveBufferSize());
/*  44:    */     }
/*  45: 63 */     if (option == ChannelOption.SO_SNDBUF) {
/*  46: 64 */       return Integer.valueOf(getSendBufferSize());
/*  47:    */     }
/*  48: 66 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  49: 67 */       return Boolean.valueOf(isReuseAddress());
/*  50:    */     }
/*  51: 69 */     if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  52: 70 */       return Boolean.valueOf(isLoopbackModeDisabled());
/*  53:    */     }
/*  54: 72 */     if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  55: 73 */       return getInterface();
/*  56:    */     }
/*  57: 75 */     if (option == ChannelOption.IP_MULTICAST_IF) {
/*  58: 76 */       return getNetworkInterface();
/*  59:    */     }
/*  60: 78 */     if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  61: 79 */       return Integer.valueOf(getTimeToLive());
/*  62:    */     }
/*  63: 81 */     if (option == ChannelOption.IP_TOS) {
/*  64: 82 */       return Integer.valueOf(getTrafficClass());
/*  65:    */     }
/*  66: 84 */     if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  67: 85 */       return Boolean.valueOf(this.activeOnOpen);
/*  68:    */     }
/*  69: 87 */     if (option == EpollChannelOption.SO_REUSEPORT) {
/*  70: 88 */       return Boolean.valueOf(isReusePort());
/*  71:    */     }
/*  72: 90 */     return super.getOption(option);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  76:    */   {
/*  77: 96 */     validate(option, value);
/*  78: 98 */     if (option == ChannelOption.SO_BROADCAST) {
/*  79: 99 */       setBroadcast(((Boolean)value).booleanValue());
/*  80:100 */     } else if (option == ChannelOption.SO_RCVBUF) {
/*  81:101 */       setReceiveBufferSize(((Integer)value).intValue());
/*  82:102 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  83:103 */       setSendBufferSize(((Integer)value).intValue());
/*  84:104 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  85:105 */       setReuseAddress(((Boolean)value).booleanValue());
/*  86:106 */     } else if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  87:107 */       setLoopbackModeDisabled(((Boolean)value).booleanValue());
/*  88:108 */     } else if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  89:109 */       setInterface((InetAddress)value);
/*  90:110 */     } else if (option == ChannelOption.IP_MULTICAST_IF) {
/*  91:111 */       setNetworkInterface((NetworkInterface)value);
/*  92:112 */     } else if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  93:113 */       setTimeToLive(((Integer)value).intValue());
/*  94:114 */     } else if (option == ChannelOption.IP_TOS) {
/*  95:115 */       setTrafficClass(((Integer)value).intValue());
/*  96:116 */     } else if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  97:117 */       setActiveOnOpen(((Boolean)value).booleanValue());
/*  98:118 */     } else if (option == EpollChannelOption.SO_REUSEPORT) {
/*  99:119 */       setReusePort(((Boolean)value).booleanValue());
/* 100:    */     } else {
/* 101:121 */       return super.setOption(option, value);
/* 102:    */     }
/* 103:124 */     return true;
/* 104:    */   }
/* 105:    */   
/* 106:    */   private void setActiveOnOpen(boolean activeOnOpen)
/* 107:    */   {
/* 108:128 */     if (this.channel.isRegistered()) {
/* 109:129 */       throw new IllegalStateException("Can only changed before channel was registered");
/* 110:    */     }
/* 111:131 */     this.activeOnOpen = activeOnOpen;
/* 112:    */   }
/* 113:    */   
/* 114:    */   boolean getActiveOnOpen()
/* 115:    */   {
/* 116:135 */     return this.activeOnOpen;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public EpollDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 120:    */   {
/* 121:140 */     super.setMessageSizeEstimator(estimator);
/* 122:141 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   @Deprecated
/* 126:    */   public EpollDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 127:    */   {
/* 128:147 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 129:148 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   @Deprecated
/* 133:    */   public EpollDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 134:    */   {
/* 135:154 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 136:155 */     return this;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public EpollDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 140:    */   {
/* 141:160 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 142:161 */     return this;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public EpollDatagramChannelConfig setAutoClose(boolean autoClose)
/* 146:    */   {
/* 147:166 */     super.setAutoClose(autoClose);
/* 148:167 */     return this;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public EpollDatagramChannelConfig setAutoRead(boolean autoRead)
/* 152:    */   {
/* 153:172 */     super.setAutoRead(autoRead);
/* 154:173 */     return this;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public EpollDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 158:    */   {
/* 159:178 */     super.setRecvByteBufAllocator(allocator);
/* 160:179 */     return this;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public EpollDatagramChannelConfig setWriteSpinCount(int writeSpinCount)
/* 164:    */   {
/* 165:184 */     super.setWriteSpinCount(writeSpinCount);
/* 166:185 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public EpollDatagramChannelConfig setAllocator(ByteBufAllocator allocator)
/* 170:    */   {
/* 171:190 */     super.setAllocator(allocator);
/* 172:191 */     return this;
/* 173:    */   }
/* 174:    */   
/* 175:    */   public EpollDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 176:    */   {
/* 177:196 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 178:197 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   @Deprecated
/* 182:    */   public EpollDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 183:    */   {
/* 184:203 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 185:204 */     return this;
/* 186:    */   }
/* 187:    */   
/* 188:    */   public int getSendBufferSize()
/* 189:    */   {
/* 190:    */     try
/* 191:    */     {
/* 192:210 */       return this.datagramChannel.socket.getSendBufferSize();
/* 193:    */     }
/* 194:    */     catch (IOException e)
/* 195:    */     {
/* 196:212 */       throw new ChannelException(e);
/* 197:    */     }
/* 198:    */   }
/* 199:    */   
/* 200:    */   public EpollDatagramChannelConfig setSendBufferSize(int sendBufferSize)
/* 201:    */   {
/* 202:    */     try
/* 203:    */     {
/* 204:219 */       this.datagramChannel.socket.setSendBufferSize(sendBufferSize);
/* 205:220 */       return this;
/* 206:    */     }
/* 207:    */     catch (IOException e)
/* 208:    */     {
/* 209:222 */       throw new ChannelException(e);
/* 210:    */     }
/* 211:    */   }
/* 212:    */   
/* 213:    */   public int getReceiveBufferSize()
/* 214:    */   {
/* 215:    */     try
/* 216:    */     {
/* 217:229 */       return this.datagramChannel.socket.getReceiveBufferSize();
/* 218:    */     }
/* 219:    */     catch (IOException e)
/* 220:    */     {
/* 221:231 */       throw new ChannelException(e);
/* 222:    */     }
/* 223:    */   }
/* 224:    */   
/* 225:    */   public EpollDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 226:    */   {
/* 227:    */     try
/* 228:    */     {
/* 229:238 */       this.datagramChannel.socket.setReceiveBufferSize(receiveBufferSize);
/* 230:239 */       return this;
/* 231:    */     }
/* 232:    */     catch (IOException e)
/* 233:    */     {
/* 234:241 */       throw new ChannelException(e);
/* 235:    */     }
/* 236:    */   }
/* 237:    */   
/* 238:    */   public int getTrafficClass()
/* 239:    */   {
/* 240:    */     try
/* 241:    */     {
/* 242:248 */       return this.datagramChannel.socket.getTrafficClass();
/* 243:    */     }
/* 244:    */     catch (IOException e)
/* 245:    */     {
/* 246:250 */       throw new ChannelException(e);
/* 247:    */     }
/* 248:    */   }
/* 249:    */   
/* 250:    */   public EpollDatagramChannelConfig setTrafficClass(int trafficClass)
/* 251:    */   {
/* 252:    */     try
/* 253:    */     {
/* 254:257 */       this.datagramChannel.socket.setTrafficClass(trafficClass);
/* 255:258 */       return this;
/* 256:    */     }
/* 257:    */     catch (IOException e)
/* 258:    */     {
/* 259:260 */       throw new ChannelException(e);
/* 260:    */     }
/* 261:    */   }
/* 262:    */   
/* 263:    */   public boolean isReuseAddress()
/* 264:    */   {
/* 265:    */     try
/* 266:    */     {
/* 267:267 */       return this.datagramChannel.socket.isReuseAddress();
/* 268:    */     }
/* 269:    */     catch (IOException e)
/* 270:    */     {
/* 271:269 */       throw new ChannelException(e);
/* 272:    */     }
/* 273:    */   }
/* 274:    */   
/* 275:    */   public EpollDatagramChannelConfig setReuseAddress(boolean reuseAddress)
/* 276:    */   {
/* 277:    */     try
/* 278:    */     {
/* 279:276 */       this.datagramChannel.socket.setReuseAddress(reuseAddress);
/* 280:277 */       return this;
/* 281:    */     }
/* 282:    */     catch (IOException e)
/* 283:    */     {
/* 284:279 */       throw new ChannelException(e);
/* 285:    */     }
/* 286:    */   }
/* 287:    */   
/* 288:    */   public boolean isBroadcast()
/* 289:    */   {
/* 290:    */     try
/* 291:    */     {
/* 292:286 */       return this.datagramChannel.socket.isBroadcast();
/* 293:    */     }
/* 294:    */     catch (IOException e)
/* 295:    */     {
/* 296:288 */       throw new ChannelException(e);
/* 297:    */     }
/* 298:    */   }
/* 299:    */   
/* 300:    */   public EpollDatagramChannelConfig setBroadcast(boolean broadcast)
/* 301:    */   {
/* 302:    */     try
/* 303:    */     {
/* 304:295 */       this.datagramChannel.socket.setBroadcast(broadcast);
/* 305:296 */       return this;
/* 306:    */     }
/* 307:    */     catch (IOException e)
/* 308:    */     {
/* 309:298 */       throw new ChannelException(e);
/* 310:    */     }
/* 311:    */   }
/* 312:    */   
/* 313:    */   public boolean isLoopbackModeDisabled()
/* 314:    */   {
/* 315:304 */     return false;
/* 316:    */   }
/* 317:    */   
/* 318:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 319:    */   {
/* 320:309 */     throw new UnsupportedOperationException("Multicast not supported");
/* 321:    */   }
/* 322:    */   
/* 323:    */   public int getTimeToLive()
/* 324:    */   {
/* 325:314 */     return -1;
/* 326:    */   }
/* 327:    */   
/* 328:    */   public EpollDatagramChannelConfig setTimeToLive(int ttl)
/* 329:    */   {
/* 330:319 */     throw new UnsupportedOperationException("Multicast not supported");
/* 331:    */   }
/* 332:    */   
/* 333:    */   public InetAddress getInterface()
/* 334:    */   {
/* 335:324 */     return null;
/* 336:    */   }
/* 337:    */   
/* 338:    */   public EpollDatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 339:    */   {
/* 340:329 */     throw new UnsupportedOperationException("Multicast not supported");
/* 341:    */   }
/* 342:    */   
/* 343:    */   public NetworkInterface getNetworkInterface()
/* 344:    */   {
/* 345:334 */     return null;
/* 346:    */   }
/* 347:    */   
/* 348:    */   public EpollDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 349:    */   {
/* 350:339 */     throw new UnsupportedOperationException("Multicast not supported");
/* 351:    */   }
/* 352:    */   
/* 353:    */   public EpollDatagramChannelConfig setEpollMode(EpollMode mode)
/* 354:    */   {
/* 355:344 */     super.setEpollMode(mode);
/* 356:345 */     return this;
/* 357:    */   }
/* 358:    */   
/* 359:    */   public boolean isReusePort()
/* 360:    */   {
/* 361:    */     try
/* 362:    */     {
/* 363:353 */       return this.datagramChannel.socket.isReusePort();
/* 364:    */     }
/* 365:    */     catch (IOException e)
/* 366:    */     {
/* 367:355 */       throw new ChannelException(e);
/* 368:    */     }
/* 369:    */   }
/* 370:    */   
/* 371:    */   public EpollDatagramChannelConfig setReusePort(boolean reusePort)
/* 372:    */   {
/* 373:    */     try
/* 374:    */     {
/* 375:368 */       this.datagramChannel.socket.setReusePort(reusePort);
/* 376:369 */       return this;
/* 377:    */     }
/* 378:    */     catch (IOException e)
/* 379:    */     {
/* 380:371 */       throw new ChannelException(e);
/* 381:    */     }
/* 382:    */   }
/* 383:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.epoll.EpollDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */