/*   1:    */ package io.netty.channel.kqueue;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.FixedRecvByteBufAllocator;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*  11:    */ import io.netty.channel.unix.UnixChannelOption;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.net.InetAddress;
/*  14:    */ import java.net.NetworkInterface;
/*  15:    */ import java.util.Map;
/*  16:    */ 
/*  17:    */ public final class KQueueDatagramChannelConfig
/*  18:    */   extends KQueueChannelConfig
/*  19:    */   implements DatagramChannelConfig
/*  20:    */ {
/*  21: 47 */   private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = new FixedRecvByteBufAllocator(2048);
/*  22:    */   private final KQueueDatagramChannel datagramChannel;
/*  23:    */   private boolean activeOnOpen;
/*  24:    */   
/*  25:    */   KQueueDatagramChannelConfig(KQueueDatagramChannel channel)
/*  26:    */   {
/*  27: 52 */     super(channel);
/*  28: 53 */     this.datagramChannel = channel;
/*  29: 54 */     setRecvByteBufAllocator(DEFAULT_RCVBUF_ALLOCATOR);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  33:    */   {
/*  34: 60 */     return getOptions(
/*  35: 61 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_BROADCAST, ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.SO_REUSEADDR, ChannelOption.IP_MULTICAST_LOOP_DISABLED, ChannelOption.IP_MULTICAST_ADDR, ChannelOption.IP_MULTICAST_IF, ChannelOption.IP_MULTICAST_TTL, ChannelOption.IP_TOS, ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, UnixChannelOption.SO_REUSEPORT });
/*  36:    */   }
/*  37:    */   
/*  38:    */   public <T> T getOption(ChannelOption<T> option)
/*  39:    */   {
/*  40: 70 */     if (option == ChannelOption.SO_BROADCAST) {
/*  41: 71 */       return Boolean.valueOf(isBroadcast());
/*  42:    */     }
/*  43: 73 */     if (option == ChannelOption.SO_RCVBUF) {
/*  44: 74 */       return Integer.valueOf(getReceiveBufferSize());
/*  45:    */     }
/*  46: 76 */     if (option == ChannelOption.SO_SNDBUF) {
/*  47: 77 */       return Integer.valueOf(getSendBufferSize());
/*  48:    */     }
/*  49: 79 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  50: 80 */       return Boolean.valueOf(isReuseAddress());
/*  51:    */     }
/*  52: 82 */     if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  53: 83 */       return Boolean.valueOf(isLoopbackModeDisabled());
/*  54:    */     }
/*  55: 85 */     if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  56: 86 */       return getInterface();
/*  57:    */     }
/*  58: 88 */     if (option == ChannelOption.IP_MULTICAST_IF) {
/*  59: 89 */       return getNetworkInterface();
/*  60:    */     }
/*  61: 91 */     if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  62: 92 */       return Integer.valueOf(getTimeToLive());
/*  63:    */     }
/*  64: 94 */     if (option == ChannelOption.IP_TOS) {
/*  65: 95 */       return Integer.valueOf(getTrafficClass());
/*  66:    */     }
/*  67: 97 */     if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  68: 98 */       return Boolean.valueOf(this.activeOnOpen);
/*  69:    */     }
/*  70:100 */     if (option == UnixChannelOption.SO_REUSEPORT) {
/*  71:101 */       return Boolean.valueOf(isReusePort());
/*  72:    */     }
/*  73:103 */     return super.getOption(option);
/*  74:    */   }
/*  75:    */   
/*  76:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  77:    */   {
/*  78:109 */     validate(option, value);
/*  79:111 */     if (option == ChannelOption.SO_BROADCAST) {
/*  80:112 */       setBroadcast(((Boolean)value).booleanValue());
/*  81:113 */     } else if (option == ChannelOption.SO_RCVBUF) {
/*  82:114 */       setReceiveBufferSize(((Integer)value).intValue());
/*  83:115 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  84:116 */       setSendBufferSize(((Integer)value).intValue());
/*  85:117 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  86:118 */       setReuseAddress(((Boolean)value).booleanValue());
/*  87:119 */     } else if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
/*  88:120 */       setLoopbackModeDisabled(((Boolean)value).booleanValue());
/*  89:121 */     } else if (option == ChannelOption.IP_MULTICAST_ADDR) {
/*  90:122 */       setInterface((InetAddress)value);
/*  91:123 */     } else if (option == ChannelOption.IP_MULTICAST_IF) {
/*  92:124 */       setNetworkInterface((NetworkInterface)value);
/*  93:125 */     } else if (option == ChannelOption.IP_MULTICAST_TTL) {
/*  94:126 */       setTimeToLive(((Integer)value).intValue());
/*  95:127 */     } else if (option == ChannelOption.IP_TOS) {
/*  96:128 */       setTrafficClass(((Integer)value).intValue());
/*  97:129 */     } else if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
/*  98:130 */       setActiveOnOpen(((Boolean)value).booleanValue());
/*  99:131 */     } else if (option == UnixChannelOption.SO_REUSEPORT) {
/* 100:132 */       setReusePort(((Boolean)value).booleanValue());
/* 101:    */     } else {
/* 102:134 */       return super.setOption(option, value);
/* 103:    */     }
/* 104:137 */     return true;
/* 105:    */   }
/* 106:    */   
/* 107:    */   private void setActiveOnOpen(boolean activeOnOpen)
/* 108:    */   {
/* 109:141 */     if (this.channel.isRegistered()) {
/* 110:142 */       throw new IllegalStateException("Can only changed before channel was registered");
/* 111:    */     }
/* 112:144 */     this.activeOnOpen = activeOnOpen;
/* 113:    */   }
/* 114:    */   
/* 115:    */   boolean getActiveOnOpen()
/* 116:    */   {
/* 117:148 */     return this.activeOnOpen;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public boolean isReusePort()
/* 121:    */   {
/* 122:    */     try
/* 123:    */     {
/* 124:156 */       return this.datagramChannel.socket.isReusePort();
/* 125:    */     }
/* 126:    */     catch (IOException e)
/* 127:    */     {
/* 128:158 */       throw new ChannelException(e);
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   public KQueueDatagramChannelConfig setReusePort(boolean reusePort)
/* 133:    */   {
/* 134:    */     try
/* 135:    */     {
/* 136:171 */       this.datagramChannel.socket.setReusePort(reusePort);
/* 137:172 */       return this;
/* 138:    */     }
/* 139:    */     catch (IOException e)
/* 140:    */     {
/* 141:174 */       throw new ChannelException(e);
/* 142:    */     }
/* 143:    */   }
/* 144:    */   
/* 145:    */   public KQueueDatagramChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess)
/* 146:    */   {
/* 147:180 */     super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
/* 148:181 */     return this;
/* 149:    */   }
/* 150:    */   
/* 151:    */   public KQueueDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 152:    */   {
/* 153:186 */     super.setMessageSizeEstimator(estimator);
/* 154:187 */     return this;
/* 155:    */   }
/* 156:    */   
/* 157:    */   @Deprecated
/* 158:    */   public KQueueDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 159:    */   {
/* 160:193 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 161:194 */     return this;
/* 162:    */   }
/* 163:    */   
/* 164:    */   @Deprecated
/* 165:    */   public KQueueDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 166:    */   {
/* 167:200 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 168:201 */     return this;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public KQueueDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 172:    */   {
/* 173:206 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 174:207 */     return this;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public KQueueDatagramChannelConfig setAutoClose(boolean autoClose)
/* 178:    */   {
/* 179:212 */     super.setAutoClose(autoClose);
/* 180:213 */     return this;
/* 181:    */   }
/* 182:    */   
/* 183:    */   public KQueueDatagramChannelConfig setAutoRead(boolean autoRead)
/* 184:    */   {
/* 185:218 */     super.setAutoRead(autoRead);
/* 186:219 */     return this;
/* 187:    */   }
/* 188:    */   
/* 189:    */   public KQueueDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 190:    */   {
/* 191:224 */     super.setRecvByteBufAllocator(allocator);
/* 192:225 */     return this;
/* 193:    */   }
/* 194:    */   
/* 195:    */   public KQueueDatagramChannelConfig setWriteSpinCount(int writeSpinCount)
/* 196:    */   {
/* 197:230 */     super.setWriteSpinCount(writeSpinCount);
/* 198:231 */     return this;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public KQueueDatagramChannelConfig setAllocator(ByteBufAllocator allocator)
/* 202:    */   {
/* 203:236 */     super.setAllocator(allocator);
/* 204:237 */     return this;
/* 205:    */   }
/* 206:    */   
/* 207:    */   public KQueueDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 208:    */   {
/* 209:242 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 210:243 */     return this;
/* 211:    */   }
/* 212:    */   
/* 213:    */   @Deprecated
/* 214:    */   public KQueueDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 215:    */   {
/* 216:249 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 217:250 */     return this;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public int getSendBufferSize()
/* 221:    */   {
/* 222:    */     try
/* 223:    */     {
/* 224:256 */       return this.datagramChannel.socket.getSendBufferSize();
/* 225:    */     }
/* 226:    */     catch (IOException e)
/* 227:    */     {
/* 228:258 */       throw new ChannelException(e);
/* 229:    */     }
/* 230:    */   }
/* 231:    */   
/* 232:    */   public KQueueDatagramChannelConfig setSendBufferSize(int sendBufferSize)
/* 233:    */   {
/* 234:    */     try
/* 235:    */     {
/* 236:265 */       this.datagramChannel.socket.setSendBufferSize(sendBufferSize);
/* 237:266 */       return this;
/* 238:    */     }
/* 239:    */     catch (IOException e)
/* 240:    */     {
/* 241:268 */       throw new ChannelException(e);
/* 242:    */     }
/* 243:    */   }
/* 244:    */   
/* 245:    */   public int getReceiveBufferSize()
/* 246:    */   {
/* 247:    */     try
/* 248:    */     {
/* 249:275 */       return this.datagramChannel.socket.getReceiveBufferSize();
/* 250:    */     }
/* 251:    */     catch (IOException e)
/* 252:    */     {
/* 253:277 */       throw new ChannelException(e);
/* 254:    */     }
/* 255:    */   }
/* 256:    */   
/* 257:    */   public KQueueDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 258:    */   {
/* 259:    */     try
/* 260:    */     {
/* 261:284 */       this.datagramChannel.socket.setReceiveBufferSize(receiveBufferSize);
/* 262:285 */       return this;
/* 263:    */     }
/* 264:    */     catch (IOException e)
/* 265:    */     {
/* 266:287 */       throw new ChannelException(e);
/* 267:    */     }
/* 268:    */   }
/* 269:    */   
/* 270:    */   public int getTrafficClass()
/* 271:    */   {
/* 272:    */     try
/* 273:    */     {
/* 274:294 */       return this.datagramChannel.socket.getTrafficClass();
/* 275:    */     }
/* 276:    */     catch (IOException e)
/* 277:    */     {
/* 278:296 */       throw new ChannelException(e);
/* 279:    */     }
/* 280:    */   }
/* 281:    */   
/* 282:    */   public KQueueDatagramChannelConfig setTrafficClass(int trafficClass)
/* 283:    */   {
/* 284:    */     try
/* 285:    */     {
/* 286:303 */       this.datagramChannel.socket.setTrafficClass(trafficClass);
/* 287:304 */       return this;
/* 288:    */     }
/* 289:    */     catch (IOException e)
/* 290:    */     {
/* 291:306 */       throw new ChannelException(e);
/* 292:    */     }
/* 293:    */   }
/* 294:    */   
/* 295:    */   public boolean isReuseAddress()
/* 296:    */   {
/* 297:    */     try
/* 298:    */     {
/* 299:313 */       return this.datagramChannel.socket.isReuseAddress();
/* 300:    */     }
/* 301:    */     catch (IOException e)
/* 302:    */     {
/* 303:315 */       throw new ChannelException(e);
/* 304:    */     }
/* 305:    */   }
/* 306:    */   
/* 307:    */   public KQueueDatagramChannelConfig setReuseAddress(boolean reuseAddress)
/* 308:    */   {
/* 309:    */     try
/* 310:    */     {
/* 311:322 */       this.datagramChannel.socket.setReuseAddress(reuseAddress);
/* 312:323 */       return this;
/* 313:    */     }
/* 314:    */     catch (IOException e)
/* 315:    */     {
/* 316:325 */       throw new ChannelException(e);
/* 317:    */     }
/* 318:    */   }
/* 319:    */   
/* 320:    */   public boolean isBroadcast()
/* 321:    */   {
/* 322:    */     try
/* 323:    */     {
/* 324:332 */       return this.datagramChannel.socket.isBroadcast();
/* 325:    */     }
/* 326:    */     catch (IOException e)
/* 327:    */     {
/* 328:334 */       throw new ChannelException(e);
/* 329:    */     }
/* 330:    */   }
/* 331:    */   
/* 332:    */   public KQueueDatagramChannelConfig setBroadcast(boolean broadcast)
/* 333:    */   {
/* 334:    */     try
/* 335:    */     {
/* 336:341 */       this.datagramChannel.socket.setBroadcast(broadcast);
/* 337:342 */       return this;
/* 338:    */     }
/* 339:    */     catch (IOException e)
/* 340:    */     {
/* 341:344 */       throw new ChannelException(e);
/* 342:    */     }
/* 343:    */   }
/* 344:    */   
/* 345:    */   public boolean isLoopbackModeDisabled()
/* 346:    */   {
/* 347:350 */     return false;
/* 348:    */   }
/* 349:    */   
/* 350:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 351:    */   {
/* 352:355 */     throw new UnsupportedOperationException("Multicast not supported");
/* 353:    */   }
/* 354:    */   
/* 355:    */   public int getTimeToLive()
/* 356:    */   {
/* 357:360 */     return -1;
/* 358:    */   }
/* 359:    */   
/* 360:    */   public KQueueDatagramChannelConfig setTimeToLive(int ttl)
/* 361:    */   {
/* 362:365 */     throw new UnsupportedOperationException("Multicast not supported");
/* 363:    */   }
/* 364:    */   
/* 365:    */   public InetAddress getInterface()
/* 366:    */   {
/* 367:370 */     return null;
/* 368:    */   }
/* 369:    */   
/* 370:    */   public KQueueDatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 371:    */   {
/* 372:375 */     throw new UnsupportedOperationException("Multicast not supported");
/* 373:    */   }
/* 374:    */   
/* 375:    */   public NetworkInterface getNetworkInterface()
/* 376:    */   {
/* 377:380 */     return null;
/* 378:    */   }
/* 379:    */   
/* 380:    */   public KQueueDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 381:    */   {
/* 382:385 */     throw new UnsupportedOperationException("Multicast not supported");
/* 383:    */   }
/* 384:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.kqueue.KQueueDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */