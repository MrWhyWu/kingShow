/*   1:    */ package io.netty.channel.socket;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelOption;
/*   6:    */ import io.netty.channel.DefaultChannelConfig;
/*   7:    */ import io.netty.channel.MessageSizeEstimator;
/*   8:    */ import io.netty.channel.RecvByteBufAllocator;
/*   9:    */ import io.netty.channel.WriteBufferWaterMark;
/*  10:    */ import io.netty.util.internal.PlatformDependent;
/*  11:    */ import java.net.Socket;
/*  12:    */ import java.net.SocketException;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public class DefaultSocketChannelConfig
/*  16:    */   extends DefaultChannelConfig
/*  17:    */   implements SocketChannelConfig
/*  18:    */ {
/*  19:    */   protected final Socket javaSocket;
/*  20:    */   private volatile boolean allowHalfClosure;
/*  21:    */   
/*  22:    */   public DefaultSocketChannelConfig(SocketChannel channel, Socket javaSocket)
/*  23:    */   {
/*  24: 46 */     super(channel);
/*  25: 47 */     if (javaSocket == null) {
/*  26: 48 */       throw new NullPointerException("javaSocket");
/*  27:    */     }
/*  28: 50 */     this.javaSocket = javaSocket;
/*  29: 53 */     if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
/*  30:    */       try
/*  31:    */       {
/*  32: 55 */         setTcpNoDelay(true);
/*  33:    */       }
/*  34:    */       catch (Exception localException) {}
/*  35:    */     }
/*  36:    */   }
/*  37:    */   
/*  38:    */   public Map<ChannelOption<?>, Object> getOptions()
/*  39:    */   {
/*  40: 64 */     return getOptions(
/*  41: 65 */       super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE });
/*  42:    */   }
/*  43:    */   
/*  44:    */   public <T> T getOption(ChannelOption<T> option)
/*  45:    */   {
/*  46: 73 */     if (option == ChannelOption.SO_RCVBUF) {
/*  47: 74 */       return Integer.valueOf(getReceiveBufferSize());
/*  48:    */     }
/*  49: 76 */     if (option == ChannelOption.SO_SNDBUF) {
/*  50: 77 */       return Integer.valueOf(getSendBufferSize());
/*  51:    */     }
/*  52: 79 */     if (option == ChannelOption.TCP_NODELAY) {
/*  53: 80 */       return Boolean.valueOf(isTcpNoDelay());
/*  54:    */     }
/*  55: 82 */     if (option == ChannelOption.SO_KEEPALIVE) {
/*  56: 83 */       return Boolean.valueOf(isKeepAlive());
/*  57:    */     }
/*  58: 85 */     if (option == ChannelOption.SO_REUSEADDR) {
/*  59: 86 */       return Boolean.valueOf(isReuseAddress());
/*  60:    */     }
/*  61: 88 */     if (option == ChannelOption.SO_LINGER) {
/*  62: 89 */       return Integer.valueOf(getSoLinger());
/*  63:    */     }
/*  64: 91 */     if (option == ChannelOption.IP_TOS) {
/*  65: 92 */       return Integer.valueOf(getTrafficClass());
/*  66:    */     }
/*  67: 94 */     if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  68: 95 */       return Boolean.valueOf(isAllowHalfClosure());
/*  69:    */     }
/*  70: 98 */     return super.getOption(option);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public <T> boolean setOption(ChannelOption<T> option, T value)
/*  74:    */   {
/*  75:103 */     validate(option, value);
/*  76:105 */     if (option == ChannelOption.SO_RCVBUF) {
/*  77:106 */       setReceiveBufferSize(((Integer)value).intValue());
/*  78:107 */     } else if (option == ChannelOption.SO_SNDBUF) {
/*  79:108 */       setSendBufferSize(((Integer)value).intValue());
/*  80:109 */     } else if (option == ChannelOption.TCP_NODELAY) {
/*  81:110 */       setTcpNoDelay(((Boolean)value).booleanValue());
/*  82:111 */     } else if (option == ChannelOption.SO_KEEPALIVE) {
/*  83:112 */       setKeepAlive(((Boolean)value).booleanValue());
/*  84:113 */     } else if (option == ChannelOption.SO_REUSEADDR) {
/*  85:114 */       setReuseAddress(((Boolean)value).booleanValue());
/*  86:115 */     } else if (option == ChannelOption.SO_LINGER) {
/*  87:116 */       setSoLinger(((Integer)value).intValue());
/*  88:117 */     } else if (option == ChannelOption.IP_TOS) {
/*  89:118 */       setTrafficClass(((Integer)value).intValue());
/*  90:119 */     } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
/*  91:120 */       setAllowHalfClosure(((Boolean)value).booleanValue());
/*  92:    */     } else {
/*  93:122 */       return super.setOption(option, value);
/*  94:    */     }
/*  95:125 */     return true;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public int getReceiveBufferSize()
/*  99:    */   {
/* 100:    */     try
/* 101:    */     {
/* 102:131 */       return this.javaSocket.getReceiveBufferSize();
/* 103:    */     }
/* 104:    */     catch (SocketException e)
/* 105:    */     {
/* 106:133 */       throw new ChannelException(e);
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   public int getSendBufferSize()
/* 111:    */   {
/* 112:    */     try
/* 113:    */     {
/* 114:140 */       return this.javaSocket.getSendBufferSize();
/* 115:    */     }
/* 116:    */     catch (SocketException e)
/* 117:    */     {
/* 118:142 */       throw new ChannelException(e);
/* 119:    */     }
/* 120:    */   }
/* 121:    */   
/* 122:    */   public int getSoLinger()
/* 123:    */   {
/* 124:    */     try
/* 125:    */     {
/* 126:149 */       return this.javaSocket.getSoLinger();
/* 127:    */     }
/* 128:    */     catch (SocketException e)
/* 129:    */     {
/* 130:151 */       throw new ChannelException(e);
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   public int getTrafficClass()
/* 135:    */   {
/* 136:    */     try
/* 137:    */     {
/* 138:158 */       return this.javaSocket.getTrafficClass();
/* 139:    */     }
/* 140:    */     catch (SocketException e)
/* 141:    */     {
/* 142:160 */       throw new ChannelException(e);
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public boolean isKeepAlive()
/* 147:    */   {
/* 148:    */     try
/* 149:    */     {
/* 150:167 */       return this.javaSocket.getKeepAlive();
/* 151:    */     }
/* 152:    */     catch (SocketException e)
/* 153:    */     {
/* 154:169 */       throw new ChannelException(e);
/* 155:    */     }
/* 156:    */   }
/* 157:    */   
/* 158:    */   public boolean isReuseAddress()
/* 159:    */   {
/* 160:    */     try
/* 161:    */     {
/* 162:176 */       return this.javaSocket.getReuseAddress();
/* 163:    */     }
/* 164:    */     catch (SocketException e)
/* 165:    */     {
/* 166:178 */       throw new ChannelException(e);
/* 167:    */     }
/* 168:    */   }
/* 169:    */   
/* 170:    */   public boolean isTcpNoDelay()
/* 171:    */   {
/* 172:    */     try
/* 173:    */     {
/* 174:185 */       return this.javaSocket.getTcpNoDelay();
/* 175:    */     }
/* 176:    */     catch (SocketException e)
/* 177:    */     {
/* 178:187 */       throw new ChannelException(e);
/* 179:    */     }
/* 180:    */   }
/* 181:    */   
/* 182:    */   public SocketChannelConfig setKeepAlive(boolean keepAlive)
/* 183:    */   {
/* 184:    */     try
/* 185:    */     {
/* 186:194 */       this.javaSocket.setKeepAlive(keepAlive);
/* 187:    */     }
/* 188:    */     catch (SocketException e)
/* 189:    */     {
/* 190:196 */       throw new ChannelException(e);
/* 191:    */     }
/* 192:198 */     return this;
/* 193:    */   }
/* 194:    */   
/* 195:    */   public SocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth)
/* 196:    */   {
/* 197:204 */     this.javaSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
/* 198:205 */     return this;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public SocketChannelConfig setReceiveBufferSize(int receiveBufferSize)
/* 202:    */   {
/* 203:    */     try
/* 204:    */     {
/* 205:211 */       this.javaSocket.setReceiveBufferSize(receiveBufferSize);
/* 206:    */     }
/* 207:    */     catch (SocketException e)
/* 208:    */     {
/* 209:213 */       throw new ChannelException(e);
/* 210:    */     }
/* 211:215 */     return this;
/* 212:    */   }
/* 213:    */   
/* 214:    */   public SocketChannelConfig setReuseAddress(boolean reuseAddress)
/* 215:    */   {
/* 216:    */     try
/* 217:    */     {
/* 218:221 */       this.javaSocket.setReuseAddress(reuseAddress);
/* 219:    */     }
/* 220:    */     catch (SocketException e)
/* 221:    */     {
/* 222:223 */       throw new ChannelException(e);
/* 223:    */     }
/* 224:225 */     return this;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public SocketChannelConfig setSendBufferSize(int sendBufferSize)
/* 228:    */   {
/* 229:    */     try
/* 230:    */     {
/* 231:231 */       this.javaSocket.setSendBufferSize(sendBufferSize);
/* 232:    */     }
/* 233:    */     catch (SocketException e)
/* 234:    */     {
/* 235:233 */       throw new ChannelException(e);
/* 236:    */     }
/* 237:235 */     return this;
/* 238:    */   }
/* 239:    */   
/* 240:    */   public SocketChannelConfig setSoLinger(int soLinger)
/* 241:    */   {
/* 242:    */     try
/* 243:    */     {
/* 244:241 */       if (soLinger < 0) {
/* 245:242 */         this.javaSocket.setSoLinger(false, 0);
/* 246:    */       } else {
/* 247:244 */         this.javaSocket.setSoLinger(true, soLinger);
/* 248:    */       }
/* 249:    */     }
/* 250:    */     catch (SocketException e)
/* 251:    */     {
/* 252:247 */       throw new ChannelException(e);
/* 253:    */     }
/* 254:249 */     return this;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public SocketChannelConfig setTcpNoDelay(boolean tcpNoDelay)
/* 258:    */   {
/* 259:    */     try
/* 260:    */     {
/* 261:255 */       this.javaSocket.setTcpNoDelay(tcpNoDelay);
/* 262:    */     }
/* 263:    */     catch (SocketException e)
/* 264:    */     {
/* 265:257 */       throw new ChannelException(e);
/* 266:    */     }
/* 267:259 */     return this;
/* 268:    */   }
/* 269:    */   
/* 270:    */   public SocketChannelConfig setTrafficClass(int trafficClass)
/* 271:    */   {
/* 272:    */     try
/* 273:    */     {
/* 274:265 */       this.javaSocket.setTrafficClass(trafficClass);
/* 275:    */     }
/* 276:    */     catch (SocketException e)
/* 277:    */     {
/* 278:267 */       throw new ChannelException(e);
/* 279:    */     }
/* 280:269 */     return this;
/* 281:    */   }
/* 282:    */   
/* 283:    */   public boolean isAllowHalfClosure()
/* 284:    */   {
/* 285:274 */     return this.allowHalfClosure;
/* 286:    */   }
/* 287:    */   
/* 288:    */   public SocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure)
/* 289:    */   {
/* 290:279 */     this.allowHalfClosure = allowHalfClosure;
/* 291:280 */     return this;
/* 292:    */   }
/* 293:    */   
/* 294:    */   public SocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
/* 295:    */   {
/* 296:285 */     super.setConnectTimeoutMillis(connectTimeoutMillis);
/* 297:286 */     return this;
/* 298:    */   }
/* 299:    */   
/* 300:    */   @Deprecated
/* 301:    */   public SocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead)
/* 302:    */   {
/* 303:292 */     super.setMaxMessagesPerRead(maxMessagesPerRead);
/* 304:293 */     return this;
/* 305:    */   }
/* 306:    */   
/* 307:    */   public SocketChannelConfig setWriteSpinCount(int writeSpinCount)
/* 308:    */   {
/* 309:298 */     super.setWriteSpinCount(writeSpinCount);
/* 310:299 */     return this;
/* 311:    */   }
/* 312:    */   
/* 313:    */   public SocketChannelConfig setAllocator(ByteBufAllocator allocator)
/* 314:    */   {
/* 315:304 */     super.setAllocator(allocator);
/* 316:305 */     return this;
/* 317:    */   }
/* 318:    */   
/* 319:    */   public SocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator)
/* 320:    */   {
/* 321:310 */     super.setRecvByteBufAllocator(allocator);
/* 322:311 */     return this;
/* 323:    */   }
/* 324:    */   
/* 325:    */   public SocketChannelConfig setAutoRead(boolean autoRead)
/* 326:    */   {
/* 327:316 */     super.setAutoRead(autoRead);
/* 328:317 */     return this;
/* 329:    */   }
/* 330:    */   
/* 331:    */   public SocketChannelConfig setAutoClose(boolean autoClose)
/* 332:    */   {
/* 333:322 */     super.setAutoClose(autoClose);
/* 334:323 */     return this;
/* 335:    */   }
/* 336:    */   
/* 337:    */   public SocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark)
/* 338:    */   {
/* 339:328 */     super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
/* 340:329 */     return this;
/* 341:    */   }
/* 342:    */   
/* 343:    */   public SocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark)
/* 344:    */   {
/* 345:334 */     super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
/* 346:335 */     return this;
/* 347:    */   }
/* 348:    */   
/* 349:    */   public SocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark)
/* 350:    */   {
/* 351:340 */     super.setWriteBufferWaterMark(writeBufferWaterMark);
/* 352:341 */     return this;
/* 353:    */   }
/* 354:    */   
/* 355:    */   public SocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator)
/* 356:    */   {
/* 357:346 */     super.setMessageSizeEstimator(estimator);
/* 358:347 */     return this;
/* 359:    */   }
/* 360:    */ }


/* Location:           C:\Users\LX\Desktop\新建文件夹 (2)\
 * Qualified Name:     io.netty.channel.socket.DefaultSocketChannelConfig
 * JD-Core Version:    0.7.0.1
 */